package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class Scoreboard {
    public static final String HIDDEN_SCORE_PREFIX = "#";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Object2ObjectMap<String, Objective> objectivesByName = new Object2ObjectOpenHashMap<>(16, 0.5F);
    private final Reference2ObjectMap<ObjectiveCriteria, List<Objective>> objectivesByCriteria = new Reference2ObjectOpenHashMap<>();
    private final Map<String, PlayerScores> playerScores = new Object2ObjectOpenHashMap<>(16, 0.5F);
    private final Map<DisplaySlot, Objective> displayObjectives = new EnumMap<>(DisplaySlot.class);
    private final Object2ObjectMap<String, PlayerTeam> teamsByName = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, PlayerTeam> teamsByPlayer = new Object2ObjectOpenHashMap<>();

    @Nullable
    public Objective getObjective(@Nullable String pName) {
        return this.objectivesByName.get(pName);
    }

    public Objective addObjective(
        String pName,
        ObjectiveCriteria pCriteria,
        Component pDisplayName,
        ObjectiveCriteria.RenderType pRenderType,
        boolean pDisplayAutoUpdate,
        @Nullable NumberFormat pNumberFormat
    ) {
        if (this.objectivesByName.containsKey(pName)) {
            throw new IllegalArgumentException("An objective with the name '" + pName + "' already exists!");
        } else {
            Objective objective = new Objective(this, pName, pCriteria, pDisplayName, pRenderType, pDisplayAutoUpdate, pNumberFormat);
            this.objectivesByCriteria.computeIfAbsent(pCriteria, p_310953_ -> Lists.newArrayList()).add(objective);
            this.objectivesByName.put(pName, objective);
            this.onObjectiveAdded(objective);
            return objective;
        }
    }

    public final void forAllObjectives(ObjectiveCriteria pCriteria, ScoreHolder pScoreHolder, Consumer<ScoreAccess> pAction) {
        this.objectivesByCriteria.getOrDefault(pCriteria, Collections.emptyList()).forEach(p_309370_ -> pAction.accept(this.getOrCreatePlayerScore(pScoreHolder, p_309370_, true)));
    }

    private PlayerScores getOrCreatePlayerInfo(String pUsername) {
        return this.playerScores.computeIfAbsent(pUsername, p_309376_ -> new PlayerScores());
    }

    public ScoreAccess getOrCreatePlayerScore(ScoreHolder pScoreHolder, Objective pObjective) {
        return this.getOrCreatePlayerScore(pScoreHolder, pObjective, false);
    }

    public ScoreAccess getOrCreatePlayerScore(final ScoreHolder pScoreHolder, final Objective pObjective, boolean pReadOnly) {
        final boolean flag = pReadOnly || !pObjective.getCriteria().isReadOnly();
        PlayerScores playerscores = this.getOrCreatePlayerInfo(pScoreHolder.getScoreboardName());
        final MutableBoolean mutableboolean = new MutableBoolean();
        final Score score = playerscores.getOrCreate(pObjective, p_309375_ -> mutableboolean.setTrue());
        return new ScoreAccess() {
            @Override
            public int get() {
                return score.value();
            }

            @Override
            public void set(int p_312858_) {
                if (!flag) {
                    throw new IllegalStateException("Cannot modify read-only score");
                } else {
                    boolean flag1 = mutableboolean.isTrue();
                    if (pObjective.displayAutoUpdate()) {
                        Component component = pScoreHolder.getDisplayName();
                        if (component != null && !component.equals(score.display())) {
                            score.display(component);
                            flag1 = true;
                        }
                    }

                    if (p_312858_ != score.value()) {
                        score.value(p_312858_);
                        flag1 = true;
                    }

                    if (flag1) {
                        this.sendScoreToPlayers();
                    }
                }
            }

            @Nullable
            @Override
            public Component display() {
                return score.display();
            }

            @Override
            public void display(@Nullable Component p_309551_) {
                if (mutableboolean.isTrue() || !Objects.equals(p_309551_, score.display())) {
                    score.display(p_309551_);
                    this.sendScoreToPlayers();
                }
            }

            @Override
            public void numberFormatOverride(@Nullable NumberFormat p_312257_) {
                score.numberFormat(p_312257_);
                this.sendScoreToPlayers();
            }

            @Override
            public boolean locked() {
                return score.isLocked();
            }

            @Override
            public void unlock() {
                this.setLocked(false);
            }

            @Override
            public void lock() {
                this.setLocked(true);
            }

            private void setLocked(boolean p_311228_) {
                score.setLocked(p_311228_);
                if (mutableboolean.isTrue()) {
                    this.sendScoreToPlayers();
                }

                Scoreboard.this.onScoreLockChanged(pScoreHolder, pObjective);
            }

            private void sendScoreToPlayers() {
                Scoreboard.this.onScoreChanged(pScoreHolder, pObjective, score);
                mutableboolean.setFalse();
            }
        };
    }

    @Nullable
    public ReadOnlyScoreInfo getPlayerScoreInfo(ScoreHolder pScoreHolder, Objective pObjective) {
        PlayerScores playerscores = this.playerScores.get(pScoreHolder.getScoreboardName());
        return playerscores != null ? playerscores.get(pObjective) : null;
    }

    public Collection<PlayerScoreEntry> listPlayerScores(Objective pObjective) {
        List<PlayerScoreEntry> list = new ArrayList<>();
        this.playerScores.forEach((p_309362_, p_309363_) -> {
            Score score = p_309363_.get(pObjective);
            if (score != null) {
                list.add(new PlayerScoreEntry(p_309362_, score.value(), score.display(), score.numberFormat()));
            }
        });
        return list;
    }

    public Collection<Objective> getObjectives() {
        return this.objectivesByName.values();
    }

    public Collection<String> getObjectiveNames() {
        return this.objectivesByName.keySet();
    }

    public Collection<ScoreHolder> getTrackedPlayers() {
        return this.playerScores.keySet().stream().map(ScoreHolder::forNameOnly).toList();
    }

    public void resetAllPlayerScores(ScoreHolder pScoreHolder) {
        PlayerScores playerscores = this.playerScores.remove(pScoreHolder.getScoreboardName());
        if (playerscores != null) {
            this.onPlayerRemoved(pScoreHolder);
        }
    }

    public void resetSinglePlayerScore(ScoreHolder pScoreHolder, Objective pObjective) {
        PlayerScores playerscores = this.playerScores.get(pScoreHolder.getScoreboardName());
        if (playerscores != null) {
            boolean flag = playerscores.remove(pObjective);
            if (!playerscores.hasScores()) {
                PlayerScores playerscores1 = this.playerScores.remove(pScoreHolder.getScoreboardName());
                if (playerscores1 != null) {
                    this.onPlayerRemoved(pScoreHolder);
                }
            } else if (flag) {
                this.onPlayerScoreRemoved(pScoreHolder, pObjective);
            }
        }
    }

    public Object2IntMap<Objective> listPlayerScores(ScoreHolder pScoreHolder) {
        PlayerScores playerscores = this.playerScores.get(pScoreHolder.getScoreboardName());
        return playerscores != null ? playerscores.listScores() : Object2IntMaps.emptyMap();
    }

    public void removeObjective(Objective pObjective) {
        this.objectivesByName.remove(pObjective.getName());

        for (DisplaySlot displayslot : DisplaySlot.values()) {
            if (this.getDisplayObjective(displayslot) == pObjective) {
                this.setDisplayObjective(displayslot, null);
            }
        }

        List<Objective> list = this.objectivesByCriteria.get(pObjective.getCriteria());
        if (list != null) {
            list.remove(pObjective);
        }

        for (PlayerScores playerscores : this.playerScores.values()) {
            playerscores.remove(pObjective);
        }

        this.onObjectiveRemoved(pObjective);
    }

    public void setDisplayObjective(DisplaySlot pSlot, @Nullable Objective pObjective) {
        this.displayObjectives.put(pSlot, pObjective);
    }

    @Nullable
    public Objective getDisplayObjective(DisplaySlot pSlot) {
        return this.displayObjectives.get(pSlot);
    }

    @Nullable
    public PlayerTeam getPlayerTeam(String pTeamName) {
        return this.teamsByName.get(pTeamName);
    }

    public PlayerTeam addPlayerTeam(String pName) {
        PlayerTeam playerteam = this.getPlayerTeam(pName);
        if (playerteam != null) {
            LOGGER.warn("Requested creation of existing team '{}'", pName);
            return playerteam;
        } else {
            playerteam = new PlayerTeam(this, pName);
            this.teamsByName.put(pName, playerteam);
            this.onTeamAdded(playerteam);
            return playerteam;
        }
    }

    public void removePlayerTeam(PlayerTeam pPlayerTeam) {
        this.teamsByName.remove(pPlayerTeam.getName());

        for (String s : pPlayerTeam.getPlayers()) {
            this.teamsByPlayer.remove(s);
        }

        this.onTeamRemoved(pPlayerTeam);
    }

    public boolean addPlayerToTeam(String pPlayerName, PlayerTeam pTeam) {
        if (this.getPlayersTeam(pPlayerName) != null) {
            this.removePlayerFromTeam(pPlayerName);
        }

        this.teamsByPlayer.put(pPlayerName, pTeam);
        return pTeam.getPlayers().add(pPlayerName);
    }

    public boolean removePlayerFromTeam(String pPlayerName) {
        PlayerTeam playerteam = this.getPlayersTeam(pPlayerName);
        if (playerteam != null) {
            this.removePlayerFromTeam(pPlayerName, playerteam);
            return true;
        } else {
            return false;
        }
    }

    public void removePlayerFromTeam(String pUsername, PlayerTeam pPlayerTeam) {
        if (this.getPlayersTeam(pUsername) != pPlayerTeam) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + pPlayerTeam.getName() + "'.");
        } else {
            this.teamsByPlayer.remove(pUsername);
            pPlayerTeam.getPlayers().remove(pUsername);
        }
    }

    public Collection<String> getTeamNames() {
        return this.teamsByName.keySet();
    }

    public Collection<PlayerTeam> getPlayerTeams() {
        return this.teamsByName.values();
    }

    @Nullable
    public PlayerTeam getPlayersTeam(String pUsername) {
        return this.teamsByPlayer.get(pUsername);
    }

    public void onObjectiveAdded(Objective pObjective) {
    }

    public void onObjectiveChanged(Objective pObjective) {
    }

    public void onObjectiveRemoved(Objective pObjective) {
    }

    protected void onScoreChanged(ScoreHolder pScoreHolder, Objective pObjective, Score pScore) {
    }

    protected void onScoreLockChanged(ScoreHolder pScoreHolder, Objective pObjective) {
    }

    public void onPlayerRemoved(ScoreHolder pScoreHolder) {
    }

    public void onPlayerScoreRemoved(ScoreHolder pScoreHolder, Objective pObjective) {
    }

    public void onTeamAdded(PlayerTeam pPlayerTeam) {
    }

    public void onTeamChanged(PlayerTeam pPlayerTeam) {
    }

    public void onTeamRemoved(PlayerTeam pPlayerTeam) {
    }

    public void entityRemoved(Entity pEntity) {
        if (!(pEntity instanceof Player) && !pEntity.isAlive()) {
            this.resetAllPlayerScores(pEntity);
            this.removePlayerFromTeam(pEntity.getScoreboardName());
        }
    }

    protected ListTag savePlayerScores(HolderLookup.Provider pLevelRegistry) {
        ListTag listtag = new ListTag();
        this.playerScores.forEach((p_327666_, p_327667_) -> p_327667_.listRawScores().forEach((p_327662_, p_327663_) -> {
                CompoundTag compoundtag = p_327663_.write(pLevelRegistry);
                compoundtag.putString("Name", p_327666_);
                compoundtag.putString("Objective", p_327662_.getName());
                listtag.add(compoundtag);
            }));
        return listtag;
    }

    protected void loadPlayerScores(ListTag pTag, HolderLookup.Provider pLevelRegistry) {
        for (int i = 0; i < pTag.size(); i++) {
            CompoundTag compoundtag = pTag.getCompound(i);
            Score score = Score.read(compoundtag, pLevelRegistry);
            String s = compoundtag.getString("Name");
            String s1 = compoundtag.getString("Objective");
            Objective objective = this.getObjective(s1);
            if (objective == null) {
                LOGGER.error("Unknown objective {} for name {}, ignoring", s1, s);
            } else {
                this.getOrCreatePlayerInfo(s).setScore(objective, score);
            }
        }
    }
}
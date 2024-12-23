package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
    private static final DynamicCommandExceptionType ERROR_NO_ACTION_PERFORMED = new DynamicCommandExceptionType(p_308608_ -> (Component)p_308608_);
    private static final Dynamic2CommandExceptionType ERROR_CRITERION_NOT_FOUND = new Dynamic2CommandExceptionType(
        (p_341132_, p_341133_) -> Component.translatableEscape("commands.advancement.criterionNotFound", p_341132_, p_341133_)
    );
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (p_136344_, p_136345_) -> {
        Collection<AdvancementHolder> collection = p_136344_.getSource().getServer().getAdvancements().getAllAdvancements();
        return SharedSuggestionProvider.suggestResource(collection.stream().map(AdvancementHolder::id), p_136345_);
    };

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(
            Commands.literal("advancement")
                .requires(p_136318_ -> p_136318_.hasPermission(2))
                .then(
                    Commands.literal("grant")
                        .then(
                            Commands.argument("targets", EntityArgument.players())
                                .then(
                                    Commands.literal("only")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    p_296473_ -> perform(
                                                            p_296473_.getSource(),
                                                            EntityArgument.getPlayers(p_296473_, "targets"),
                                                            AdvancementCommands.Action.GRANT,
                                                            getAdvancements(
                                                                p_296473_,
                                                                ResourceLocationArgument.getAdvancement(p_296473_, "advancement"),
                                                                AdvancementCommands.Mode.ONLY
                                                            )
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("criterion", StringArgumentType.greedyString())
                                                        .suggests(
                                                            (p_296457_, p_296458_) -> SharedSuggestionProvider.suggest(
                                                                    ResourceLocationArgument.getAdvancement(p_296457_, "advancement")
                                                                        .value()
                                                                        .criteria()
                                                                        .keySet(),
                                                                    p_296458_
                                                                )
                                                        )
                                                        .executes(
                                                            p_296456_ -> performCriterion(
                                                                    p_296456_.getSource(),
                                                                    EntityArgument.getPlayers(p_296456_, "targets"),
                                                                    AdvancementCommands.Action.GRANT,
                                                                    ResourceLocationArgument.getAdvancement(p_296456_, "advancement"),
                                                                    StringArgumentType.getString(p_296456_, "criterion")
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("from")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    p_296455_ -> perform(
                                                            p_296455_.getSource(),
                                                            EntityArgument.getPlayers(p_296455_, "targets"),
                                                            AdvancementCommands.Action.GRANT,
                                                            getAdvancements(
                                                                p_296455_,
                                                                ResourceLocationArgument.getAdvancement(p_296455_, "advancement"),
                                                                AdvancementCommands.Mode.FROM
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("until")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    p_296472_ -> perform(
                                                            p_296472_.getSource(),
                                                            EntityArgument.getPlayers(p_296472_, "targets"),
                                                            AdvancementCommands.Action.GRANT,
                                                            getAdvancements(
                                                                p_296472_,
                                                                ResourceLocationArgument.getAdvancement(p_296472_, "advancement"),
                                                                AdvancementCommands.Mode.UNTIL
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("through")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    p_296459_ -> perform(
                                                            p_296459_.getSource(),
                                                            EntityArgument.getPlayers(p_296459_, "targets"),
                                                            AdvancementCommands.Action.GRANT,
                                                            getAdvancements(
                                                                p_296459_,
                                                                ResourceLocationArgument.getAdvancement(p_296459_, "advancement"),
                                                                AdvancementCommands.Mode.THROUGH
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("everything")
                                        .executes(
                                            p_136353_ -> perform(
                                                    p_136353_.getSource(),
                                                    EntityArgument.getPlayers(p_136353_, "targets"),
                                                    AdvancementCommands.Action.GRANT,
                                                    p_136353_.getSource().getServer().getAdvancements().getAllAdvancements()
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("revoke")
                        .then(
                            Commands.argument("targets", EntityArgument.players())
                                .then(
                                    Commands.literal("only")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    p_296460_ -> perform(
                                                            p_296460_.getSource(),
                                                            EntityArgument.getPlayers(p_296460_, "targets"),
                                                            AdvancementCommands.Action.REVOKE,
                                                            getAdvancements(
                                                                p_296460_,
                                                                ResourceLocationArgument.getAdvancement(p_296460_, "advancement"),
                                                                AdvancementCommands.Mode.ONLY
                                                            )
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("criterion", StringArgumentType.greedyString())
                                                        .suggests(
                                                            (p_296480_, p_296481_) -> SharedSuggestionProvider.suggest(
                                                                    ResourceLocationArgument.getAdvancement(p_296480_, "advancement")
                                                                        .value()
                                                                        .criteria()
                                                                        .keySet(),
                                                                    p_296481_
                                                                )
                                                        )
                                                        .executes(
                                                            p_296468_ -> performCriterion(
                                                                    p_296468_.getSource(),
                                                                    EntityArgument.getPlayers(p_296468_, "targets"),
                                                                    AdvancementCommands.Action.REVOKE,
                                                                    ResourceLocationArgument.getAdvancement(p_296468_, "advancement"),
                                                                    StringArgumentType.getString(p_296468_, "criterion")
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("from")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    p_296474_ -> perform(
                                                            p_296474_.getSource(),
                                                            EntityArgument.getPlayers(p_296474_, "targets"),
                                                            AdvancementCommands.Action.REVOKE,
                                                            getAdvancements(
                                                                p_296474_,
                                                                ResourceLocationArgument.getAdvancement(p_296474_, "advancement"),
                                                                AdvancementCommands.Mode.FROM
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("until")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    p_296475_ -> perform(
                                                            p_296475_.getSource(),
                                                            EntityArgument.getPlayers(p_296475_, "targets"),
                                                            AdvancementCommands.Action.REVOKE,
                                                            getAdvancements(
                                                                p_296475_,
                                                                ResourceLocationArgument.getAdvancement(p_296475_, "advancement"),
                                                                AdvancementCommands.Mode.UNTIL
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("through")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    p_296451_ -> perform(
                                                            p_296451_.getSource(),
                                                            EntityArgument.getPlayers(p_296451_, "targets"),
                                                            AdvancementCommands.Action.REVOKE,
                                                            getAdvancements(
                                                                p_296451_,
                                                                ResourceLocationArgument.getAdvancement(p_296451_, "advancement"),
                                                                AdvancementCommands.Mode.THROUGH
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("everything")
                                        .executes(
                                            p_136313_ -> perform(
                                                    p_136313_.getSource(),
                                                    EntityArgument.getPlayers(p_136313_, "targets"),
                                                    AdvancementCommands.Action.REVOKE,
                                                    p_136313_.getSource().getServer().getAdvancements().getAllAdvancements()
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int perform(
        CommandSourceStack pSource, Collection<ServerPlayer> pTargets, AdvancementCommands.Action pAction, Collection<AdvancementHolder> pAdvancements
    ) throws CommandSyntaxException {
        int i = 0;

        for (ServerPlayer serverplayer : pTargets) {
            i += pAction.perform(serverplayer, pAdvancements);
        }

        if (i == 0) {
            if (pAdvancements.size() == 1) {
                if (pTargets.size() == 1) {
                    throw ERROR_NO_ACTION_PERFORMED.create(
                        Component.translatable(
                            pAction.getKey() + ".one.to.one.failure",
                            Advancement.name(pAdvancements.iterator().next()),
                            pTargets.iterator().next().getDisplayName()
                        )
                    );
                } else {
                    throw ERROR_NO_ACTION_PERFORMED.create(
                        Component.translatable(
                            pAction.getKey() + ".one.to.many.failure", Advancement.name(pAdvancements.iterator().next()), pTargets.size()
                        )
                    );
                }
            } else if (pTargets.size() == 1) {
                throw ERROR_NO_ACTION_PERFORMED.create(
                    Component.translatable(pAction.getKey() + ".many.to.one.failure", pAdvancements.size(), pTargets.iterator().next().getDisplayName())
                );
            } else {
                throw ERROR_NO_ACTION_PERFORMED.create(Component.translatable(pAction.getKey() + ".many.to.many.failure", pAdvancements.size(), pTargets.size()));
            }
        } else {
            if (pAdvancements.size() == 1) {
                if (pTargets.size() == 1) {
                    pSource.sendSuccess(
                        () -> Component.translatable(
                                pAction.getKey() + ".one.to.one.success",
                                Advancement.name(pAdvancements.iterator().next()),
                                pTargets.iterator().next().getDisplayName()
                            ),
                        true
                    );
                } else {
                    pSource.sendSuccess(
                        () -> Component.translatable(
                                pAction.getKey() + ".one.to.many.success", Advancement.name(pAdvancements.iterator().next()), pTargets.size()
                            ),
                        true
                    );
                }
            } else if (pTargets.size() == 1) {
                pSource.sendSuccess(
                    () -> Component.translatable(pAction.getKey() + ".many.to.one.success", pAdvancements.size(), pTargets.iterator().next().getDisplayName()), true
                );
            } else {
                pSource.sendSuccess(() -> Component.translatable(pAction.getKey() + ".many.to.many.success", pAdvancements.size(), pTargets.size()), true);
            }

            return i;
        }
    }

    private static int performCriterion(
        CommandSourceStack pSource, Collection<ServerPlayer> pTargets, AdvancementCommands.Action pAction, AdvancementHolder pAdvancement, String pCriterionName
    ) throws CommandSyntaxException {
        int i = 0;
        Advancement advancement = pAdvancement.value();
        if (!advancement.criteria().containsKey(pCriterionName)) {
            throw ERROR_CRITERION_NOT_FOUND.create(Advancement.name(pAdvancement), pCriterionName);
        } else {
            for (ServerPlayer serverplayer : pTargets) {
                if (pAction.performCriterion(serverplayer, pAdvancement, pCriterionName)) {
                    i++;
                }
            }

            if (i == 0) {
                if (pTargets.size() == 1) {
                    throw ERROR_NO_ACTION_PERFORMED.create(
                        Component.translatable(
                            pAction.getKey() + ".criterion.to.one.failure",
                            pCriterionName,
                            Advancement.name(pAdvancement),
                            pTargets.iterator().next().getDisplayName()
                        )
                    );
                } else {
                    throw ERROR_NO_ACTION_PERFORMED.create(
                        Component.translatable(pAction.getKey() + ".criterion.to.many.failure", pCriterionName, Advancement.name(pAdvancement), pTargets.size())
                    );
                }
            } else {
                if (pTargets.size() == 1) {
                    pSource.sendSuccess(
                        () -> Component.translatable(
                                pAction.getKey() + ".criterion.to.one.success",
                                pCriterionName,
                                Advancement.name(pAdvancement),
                                pTargets.iterator().next().getDisplayName()
                            ),
                        true
                    );
                } else {
                    pSource.sendSuccess(
                        () -> Component.translatable(
                                pAction.getKey() + ".criterion.to.many.success", pCriterionName, Advancement.name(pAdvancement), pTargets.size()
                            ),
                        true
                    );
                }

                return i;
            }
        }
    }

    private static List<AdvancementHolder> getAdvancements(
        CommandContext<CommandSourceStack> pContext, AdvancementHolder pAdvancement, AdvancementCommands.Mode pMode
    ) {
        AdvancementTree advancementtree = pContext.getSource().getServer().getAdvancements().tree();
        AdvancementNode advancementnode = advancementtree.get(pAdvancement);
        if (advancementnode == null) {
            return List.of(pAdvancement);
        } else {
            List<AdvancementHolder> list = new ArrayList<>();
            if (pMode.parents) {
                for (AdvancementNode advancementnode1 = advancementnode.parent(); advancementnode1 != null; advancementnode1 = advancementnode1.parent()) {
                    list.add(advancementnode1.holder());
                }
            }

            list.add(pAdvancement);
            if (pMode.children) {
                addChildren(advancementnode, list);
            }

            return list;
        }
    }

    private static void addChildren(AdvancementNode pNode, List<AdvancementHolder> pOutput) {
        for (AdvancementNode advancementnode : pNode.children()) {
            pOutput.add(advancementnode.holder());
            addChildren(advancementnode, pOutput);
        }
    }

    static enum Action {
        GRANT("grant") {
            @Override
            protected boolean perform(ServerPlayer p_136395_, AdvancementHolder p_299481_) {
                AdvancementProgress advancementprogress = p_136395_.getAdvancements().getOrStartProgress(p_299481_);
                if (advancementprogress.isDone()) {
                    return false;
                } else {
                    for (String s : advancementprogress.getRemainingCriteria()) {
                        p_136395_.getAdvancements().award(p_299481_, s);
                    }

                    return true;
                }
            }

            @Override
            protected boolean performCriterion(ServerPlayer p_136398_, AdvancementHolder p_300422_, String p_136400_) {
                return p_136398_.getAdvancements().award(p_300422_, p_136400_);
            }
        },
        REVOKE("revoke") {
            @Override
            protected boolean perform(ServerPlayer p_136406_, AdvancementHolder p_301329_) {
                AdvancementProgress advancementprogress = p_136406_.getAdvancements().getOrStartProgress(p_301329_);
                if (!advancementprogress.hasProgress()) {
                    return false;
                } else {
                    for (String s : advancementprogress.getCompletedCriteria()) {
                        p_136406_.getAdvancements().revoke(p_301329_, s);
                    }

                    return true;
                }
            }

            @Override
            protected boolean performCriterion(ServerPlayer p_136409_, AdvancementHolder p_299512_, String p_136411_) {
                return p_136409_.getAdvancements().revoke(p_299512_, p_136411_);
            }
        };

        private final String key;

        Action(final String pKey) {
            this.key = "commands.advancement." + pKey;
        }

        public int perform(ServerPlayer pPlayer, Iterable<AdvancementHolder> pAdvancements) {
            int i = 0;

            for (AdvancementHolder advancementholder : pAdvancements) {
                if (this.perform(pPlayer, advancementholder)) {
                    i++;
                }
            }

            return i;
        }

        protected abstract boolean perform(ServerPlayer pPlayer, AdvancementHolder pAdvancement);

        protected abstract boolean performCriterion(ServerPlayer pPlayer, AdvancementHolder pAdvancement, String pCriterionName);

        protected String getKey() {
            return this.key;
        }
    }

    static enum Mode {
        ONLY(false, false),
        THROUGH(true, true),
        FROM(false, true),
        UNTIL(true, false),
        EVERYTHING(true, true);

        final boolean parents;
        final boolean children;

        private Mode(final boolean pParents, final boolean pChildren) {
            this.parents = pParents;
            this.children = pChildren;
        }
    }
}
package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public record JukeboxPlayable(EitherHolder<JukeboxSong> song, boolean showInTooltip) implements TooltipProvider {
    public static final Codec<JukeboxPlayable> CODEC = RecordCodecBuilder.create(
        p_342357_ -> p_342357_.group(
                    EitherHolder.codec(Registries.JUKEBOX_SONG, JukeboxSong.CODEC).fieldOf("song").forGetter(JukeboxPlayable::song),
                    Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(JukeboxPlayable::showInTooltip)
                )
                .apply(p_342357_, JukeboxPlayable::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxPlayable> STREAM_CODEC = StreamCodec.composite(
        EitherHolder.streamCodec(Registries.JUKEBOX_SONG, JukeboxSong.STREAM_CODEC),
        JukeboxPlayable::song,
        ByteBufCodecs.BOOL,
        JukeboxPlayable::showInTooltip,
        JukeboxPlayable::new
    );

    @Override
    public void addToTooltip(Item.TooltipContext pContext, Consumer<Component> pTooltipAdder, TooltipFlag pTooltipFlag) {
        HolderLookup.Provider holderlookup$provider = pContext.registries();
        if (this.showInTooltip && holderlookup$provider != null) {
            this.song.unwrap(holderlookup$provider).ifPresent(p_343443_ -> {
                MutableComponent mutablecomponent = p_343443_.value().description().copy();
                ComponentUtils.mergeStyles(mutablecomponent, Style.EMPTY.withColor(ChatFormatting.GRAY));
                pTooltipAdder.accept(mutablecomponent);
            });
        }
    }

    public JukeboxPlayable withTooltip(boolean pShowInTooltip) {
        return new JukeboxPlayable(this.song, pShowInTooltip);
    }

    public static ItemInteractionResult tryInsertIntoJukebox(Level pLevel, BlockPos pPos, ItemStack pStack, Player pPlayer) {
        JukeboxPlayable jukeboxplayable = pStack.get(DataComponents.JUKEBOX_PLAYABLE);
        if (jukeboxplayable == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            BlockState blockstate = pLevel.getBlockState(pPos);
            if (blockstate.is(Blocks.JUKEBOX) && !blockstate.getValue(JukeboxBlock.HAS_RECORD)) {
                if (!pLevel.isClientSide) {
                    ItemStack itemstack = pStack.consumeAndReturn(1, pPlayer);
                    if (pLevel.getBlockEntity(pPos) instanceof JukeboxBlockEntity jukeboxblockentity) {
                        jukeboxblockentity.setTheItem(itemstack);
                        pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pPlayer, blockstate));
                    }

                    pPlayer.awardStat(Stats.PLAY_RECORD);
                }

                return ItemInteractionResult.sidedSuccess(pLevel.isClientSide);
            } else {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        }
    }
}
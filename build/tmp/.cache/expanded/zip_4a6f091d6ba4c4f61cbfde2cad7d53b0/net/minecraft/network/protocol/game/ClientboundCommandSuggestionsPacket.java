package net.minecraft.network.protocol.game;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundCommandSuggestionsPacket(int id, int start, int length, List<ClientboundCommandSuggestionsPacket.Entry> suggestions)
    implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCommandSuggestionsPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ClientboundCommandSuggestionsPacket::id,
        ByteBufCodecs.VAR_INT,
        ClientboundCommandSuggestionsPacket::start,
        ByteBufCodecs.VAR_INT,
        ClientboundCommandSuggestionsPacket::length,
        ClientboundCommandSuggestionsPacket.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
        ClientboundCommandSuggestionsPacket::suggestions,
        ClientboundCommandSuggestionsPacket::new
    );

    public ClientboundCommandSuggestionsPacket(int pId, Suggestions pSuggestions) {
        this(
            pId,
            pSuggestions.getRange().getStart(),
            pSuggestions.getRange().getLength(),
            pSuggestions.getList()
                .stream()
                .map(
                    p_326097_ -> new ClientboundCommandSuggestionsPacket.Entry(
                            p_326097_.getText(), Optional.ofNullable(p_326097_.getTooltip()).map(ComponentUtils::fromMessage)
                        )
                )
                .toList()
        );
    }

    @Override
    public PacketType<ClientboundCommandSuggestionsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_COMMAND_SUGGESTIONS;
    }

    public void handle(ClientGamePacketListener pHandler) {
        pHandler.handleCommandSuggestions(this);
    }

    public Suggestions toSuggestions() {
        StringRange stringrange = StringRange.between(this.start, this.start + this.length);
        return new Suggestions(
            stringrange,
            this.suggestions.stream().map(p_326096_ -> new Suggestion(stringrange, p_326096_.text(), p_326096_.tooltip().orElse(null))).toList()
        );
    }

    public static record Entry(String text, Optional<Component> tooltip) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCommandSuggestionsPacket.Entry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ClientboundCommandSuggestionsPacket.Entry::text,
            ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC,
            ClientboundCommandSuggestionsPacket.Entry::tooltip,
            ClientboundCommandSuggestionsPacket.Entry::new
        );
    }
}
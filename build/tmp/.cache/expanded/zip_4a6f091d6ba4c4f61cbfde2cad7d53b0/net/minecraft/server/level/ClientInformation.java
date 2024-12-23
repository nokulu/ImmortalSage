package net.minecraft.server.level;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;

public record ClientInformation(
    String language, int viewDistance, ChatVisiblity chatVisibility, boolean chatColors, int modelCustomisation, HumanoidArm mainHand, boolean textFilteringEnabled, boolean allowsListing
) {
    public static final int MAX_LANGUAGE_LENGTH = 16;

    public ClientInformation(FriendlyByteBuf pBuffer) {
        this(
            pBuffer.readUtf(16),
            pBuffer.readByte(),
            pBuffer.readEnum(ChatVisiblity.class),
            pBuffer.readBoolean(),
            pBuffer.readUnsignedByte(),
            pBuffer.readEnum(HumanoidArm.class),
            pBuffer.readBoolean(),
            pBuffer.readBoolean()
        );
    }

    public void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeUtf(this.language);
        pBuffer.writeByte(this.viewDistance);
        pBuffer.writeEnum(this.chatVisibility);
        pBuffer.writeBoolean(this.chatColors);
        pBuffer.writeByte(this.modelCustomisation);
        pBuffer.writeEnum(this.mainHand);
        pBuffer.writeBoolean(this.textFilteringEnabled);
        pBuffer.writeBoolean(this.allowsListing);
    }

    public static ClientInformation createDefault() {
        return new ClientInformation("en_us", 2, ChatVisiblity.FULL, true, 0, Player.DEFAULT_MAIN_HAND, false, false);
    }
}
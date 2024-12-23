package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record BlockElementFace(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv, @Nullable net.minecraftforge.client.model.ForgeFaceData data) {
    public static final int NO_TINT = -1;

    public BlockElementFace(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv) {
        this(cullForDirection, tintIndex, texture, uv, null);
    }

    @Override
    public net.minecraftforge.client.model.ForgeFaceData data() {
        return this.data == null ? net.minecraftforge.client.model.ForgeFaceData.DEFAULT : this.data;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<BlockElementFace> {
        private static final int DEFAULT_TINT_INDEX = -1;

        public BlockElementFace deserialize(JsonElement pJson, Type pType, JsonDeserializationContext pContext) throws JsonParseException {
            JsonObject jsonobject = pJson.getAsJsonObject();
            Direction direction = this.getCullFacing(jsonobject);
            int i = this.getTintIndex(jsonobject);
            String s = this.getTexture(jsonobject);
            BlockFaceUV blockfaceuv = pContext.deserialize(jsonobject, BlockFaceUV.class);
            return new BlockElementFace(direction, i, s, blockfaceuv, net.minecraftforge.client.model.ForgeFaceData.read(jsonobject.get("forge_data"), null));
        }

        protected int getTintIndex(JsonObject pJson) {
            return GsonHelper.getAsInt(pJson, "tintindex", -1);
        }

        private String getTexture(JsonObject pJson) {
            return GsonHelper.getAsString(pJson, "texture");
        }

        @Nullable
        private Direction getCullFacing(JsonObject pJson) {
            String s = GsonHelper.getAsString(pJson, "cullface", "");
            return Direction.byName(s);
        }
    }
}

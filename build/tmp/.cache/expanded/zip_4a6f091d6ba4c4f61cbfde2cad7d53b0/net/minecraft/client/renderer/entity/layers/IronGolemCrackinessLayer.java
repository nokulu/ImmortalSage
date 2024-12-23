package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemCrackinessLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
    private static final Map<Crackiness.Level, ResourceLocation> resourceLocations = ImmutableMap.of(
        Crackiness.Level.LOW,
        ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_low.png"),
        Crackiness.Level.MEDIUM,
        ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_medium.png"),
        Crackiness.Level.HIGH,
        ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_high.png")
    );

    public IronGolemCrackinessLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> pRenderer) {
        super(pRenderer);
    }

    public void render(
        PoseStack pPoseStack,
        MultiBufferSource pBuffer,
        int pPackedLight,
        IronGolem pLivingEntity,
        float pLimbSwing,
        float pLimbSwingAmount,
        float pPartialTicks,
        float pAgeInTicks,
        float pNetHeadYaw,
        float pHeadPitch
    ) {
        if (!pLivingEntity.isInvisible()) {
            Crackiness.Level crackiness$level = pLivingEntity.getCrackiness();
            if (crackiness$level != Crackiness.Level.NONE) {
                ResourceLocation resourcelocation = resourceLocations.get(crackiness$level);
                renderColoredCutoutModel(this.getParentModel(), resourcelocation, pPoseStack, pBuffer, pPackedLight, pLivingEntity, -1);
            }
        }
    }
}
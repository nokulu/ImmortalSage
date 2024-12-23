package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerProfessionLayer<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T> & VillagerHeadModel> extends RenderLayer<T, M> {
    private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap<>(), p_340946_ -> {
        p_340946_.put(1, ResourceLocation.withDefaultNamespace("stone"));
        p_340946_.put(2, ResourceLocation.withDefaultNamespace("iron"));
        p_340946_.put(3, ResourceLocation.withDefaultNamespace("gold"));
        p_340946_.put(4, ResourceLocation.withDefaultNamespace("emerald"));
        p_340946_.put(5, ResourceLocation.withDefaultNamespace("diamond"));
    });
    private final Object2ObjectMap<VillagerType, VillagerMetaDataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<VillagerProfession, VillagerMetaDataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap<>();
    private final ResourceManager resourceManager;
    private final String path;

    public VillagerProfessionLayer(RenderLayerParent<T, M> pRenderer, ResourceManager pResourceManager, String pPath) {
        super(pRenderer);
        this.resourceManager = pResourceManager;
        this.path = pPath;
    }

    public void render(
        PoseStack pPoseStack,
        MultiBufferSource pBuffer,
        int pPackedLight,
        T pLivingEntity,
        float pLimbSwing,
        float pLimbSwingAmount,
        float pPartialTicks,
        float pAgeInTicks,
        float pNetHeadYaw,
        float pHeadPitch
    ) {
        if (!pLivingEntity.isInvisible()) {
            VillagerData villagerdata = pLivingEntity.getVillagerData();
            VillagerType villagertype = villagerdata.getType();
            VillagerProfession villagerprofession = villagerdata.getProfession();
            VillagerMetaDataSection.Hat villagermetadatasection$hat = this.getHatData(this.typeHatCache, "type", BuiltInRegistries.VILLAGER_TYPE, villagertype);
            VillagerMetaDataSection.Hat villagermetadatasection$hat1 = this.getHatData(
                this.professionHatCache, "profession", BuiltInRegistries.VILLAGER_PROFESSION, villagerprofession
            );
            M m = this.getParentModel();
            m.hatVisible(
                villagermetadatasection$hat1 == VillagerMetaDataSection.Hat.NONE
                    || villagermetadatasection$hat1 == VillagerMetaDataSection.Hat.PARTIAL && villagermetadatasection$hat != VillagerMetaDataSection.Hat.FULL
            );
            ResourceLocation resourcelocation = this.getResourceLocation("type", BuiltInRegistries.VILLAGER_TYPE.getKey(villagertype));
            renderColoredCutoutModel(m, resourcelocation, pPoseStack, pBuffer, pPackedLight, pLivingEntity, -1);
            m.hatVisible(true);
            if (villagerprofession != VillagerProfession.NONE && !pLivingEntity.isBaby()) {
                ResourceLocation resourcelocation1 = this.getResourceLocation("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(villagerprofession));
                renderColoredCutoutModel(m, resourcelocation1, pPoseStack, pBuffer, pPackedLight, pLivingEntity, -1);
                if (villagerprofession != VillagerProfession.NITWIT) {
                    ResourceLocation resourcelocation2 = this.getResourceLocation(
                        "profession_level", LEVEL_LOCATIONS.get(Mth.clamp(villagerdata.getLevel(), 1, LEVEL_LOCATIONS.size()))
                    );
                    renderColoredCutoutModel(m, resourcelocation2, pPoseStack, pBuffer, pPackedLight, pLivingEntity, -1);
                }
            }
        }
    }

    private ResourceLocation getResourceLocation(String pFolder, ResourceLocation pLocation) {
        return pLocation.withPath(p_247944_ -> "textures/entity/" + this.path + "/" + pFolder + "/" + p_247944_ + ".png");
    }

    public <K> VillagerMetaDataSection.Hat getHatData(
        Object2ObjectMap<K, VillagerMetaDataSection.Hat> pCache, String pFolder, DefaultedRegistry<K> pVillagerTypeRegistry, K pKey
    ) {
        return pCache.computeIfAbsent(
            pKey, p_258159_ -> this.resourceManager.getResource(this.getResourceLocation(pFolder, pVillagerTypeRegistry.getKey(pKey))).flatMap(p_234875_ -> {
                    try {
                        return p_234875_.metadata().getSection(VillagerMetaDataSection.SERIALIZER).map(VillagerMetaDataSection::getHat);
                    } catch (IOException ioexception) {
                        return Optional.empty();
                    }
                }).orElse(VillagerMetaDataSection.Hat.NONE)
        );
    }
}
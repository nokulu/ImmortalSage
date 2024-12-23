package net.minecraft.world.entity.decoration;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class PaintingVariants {
    public static final ResourceKey<PaintingVariant> KEBAB = create("kebab");
    public static final ResourceKey<PaintingVariant> AZTEC = create("aztec");
    public static final ResourceKey<PaintingVariant> ALBAN = create("alban");
    public static final ResourceKey<PaintingVariant> AZTEC2 = create("aztec2");
    public static final ResourceKey<PaintingVariant> BOMB = create("bomb");
    public static final ResourceKey<PaintingVariant> PLANT = create("plant");
    public static final ResourceKey<PaintingVariant> WASTELAND = create("wasteland");
    public static final ResourceKey<PaintingVariant> POOL = create("pool");
    public static final ResourceKey<PaintingVariant> COURBET = create("courbet");
    public static final ResourceKey<PaintingVariant> SEA = create("sea");
    public static final ResourceKey<PaintingVariant> SUNSET = create("sunset");
    public static final ResourceKey<PaintingVariant> CREEBET = create("creebet");
    public static final ResourceKey<PaintingVariant> WANDERER = create("wanderer");
    public static final ResourceKey<PaintingVariant> GRAHAM = create("graham");
    public static final ResourceKey<PaintingVariant> MATCH = create("match");
    public static final ResourceKey<PaintingVariant> BUST = create("bust");
    public static final ResourceKey<PaintingVariant> STAGE = create("stage");
    public static final ResourceKey<PaintingVariant> VOID = create("void");
    public static final ResourceKey<PaintingVariant> SKULL_AND_ROSES = create("skull_and_roses");
    public static final ResourceKey<PaintingVariant> WITHER = create("wither");
    public static final ResourceKey<PaintingVariant> FIGHTERS = create("fighters");
    public static final ResourceKey<PaintingVariant> POINTER = create("pointer");
    public static final ResourceKey<PaintingVariant> PIGSCENE = create("pigscene");
    public static final ResourceKey<PaintingVariant> BURNING_SKULL = create("burning_skull");
    public static final ResourceKey<PaintingVariant> SKELETON = create("skeleton");
    public static final ResourceKey<PaintingVariant> DONKEY_KONG = create("donkey_kong");
    public static final ResourceKey<PaintingVariant> EARTH = create("earth");
    public static final ResourceKey<PaintingVariant> WIND = create("wind");
    public static final ResourceKey<PaintingVariant> WATER = create("water");
    public static final ResourceKey<PaintingVariant> FIRE = create("fire");
    public static final ResourceKey<PaintingVariant> BAROQUE = create("baroque");
    public static final ResourceKey<PaintingVariant> HUMBLE = create("humble");
    public static final ResourceKey<PaintingVariant> MEDITATIVE = create("meditative");
    public static final ResourceKey<PaintingVariant> PRAIRIE_RIDE = create("prairie_ride");
    public static final ResourceKey<PaintingVariant> UNPACKED = create("unpacked");
    public static final ResourceKey<PaintingVariant> BACKYARD = create("backyard");
    public static final ResourceKey<PaintingVariant> BOUQUET = create("bouquet");
    public static final ResourceKey<PaintingVariant> CAVEBIRD = create("cavebird");
    public static final ResourceKey<PaintingVariant> CHANGING = create("changing");
    public static final ResourceKey<PaintingVariant> COTAN = create("cotan");
    public static final ResourceKey<PaintingVariant> ENDBOSS = create("endboss");
    public static final ResourceKey<PaintingVariant> FERN = create("fern");
    public static final ResourceKey<PaintingVariant> FINDING = create("finding");
    public static final ResourceKey<PaintingVariant> LOWMIST = create("lowmist");
    public static final ResourceKey<PaintingVariant> ORB = create("orb");
    public static final ResourceKey<PaintingVariant> OWLEMONS = create("owlemons");
    public static final ResourceKey<PaintingVariant> PASSAGE = create("passage");
    public static final ResourceKey<PaintingVariant> POND = create("pond");
    public static final ResourceKey<PaintingVariant> SUNFLOWERS = create("sunflowers");
    public static final ResourceKey<PaintingVariant> TIDES = create("tides");

    public static void bootstrap(BootstrapContext<PaintingVariant> pContext) {
        register(pContext, KEBAB, 1, 1);
        register(pContext, AZTEC, 1, 1);
        register(pContext, ALBAN, 1, 1);
        register(pContext, AZTEC2, 1, 1);
        register(pContext, BOMB, 1, 1);
        register(pContext, PLANT, 1, 1);
        register(pContext, WASTELAND, 1, 1);
        register(pContext, POOL, 2, 1);
        register(pContext, COURBET, 2, 1);
        register(pContext, SEA, 2, 1);
        register(pContext, SUNSET, 2, 1);
        register(pContext, CREEBET, 2, 1);
        register(pContext, WANDERER, 1, 2);
        register(pContext, GRAHAM, 1, 2);
        register(pContext, MATCH, 2, 2);
        register(pContext, BUST, 2, 2);
        register(pContext, STAGE, 2, 2);
        register(pContext, VOID, 2, 2);
        register(pContext, SKULL_AND_ROSES, 2, 2);
        register(pContext, WITHER, 2, 2);
        register(pContext, FIGHTERS, 4, 2);
        register(pContext, POINTER, 4, 4);
        register(pContext, PIGSCENE, 4, 4);
        register(pContext, BURNING_SKULL, 4, 4);
        register(pContext, SKELETON, 4, 3);
        register(pContext, EARTH, 2, 2);
        register(pContext, WIND, 2, 2);
        register(pContext, WATER, 2, 2);
        register(pContext, FIRE, 2, 2);
        register(pContext, DONKEY_KONG, 4, 3);
        register(pContext, BAROQUE, 2, 2);
        register(pContext, HUMBLE, 2, 2);
        register(pContext, MEDITATIVE, 1, 1);
        register(pContext, PRAIRIE_RIDE, 1, 2);
        register(pContext, UNPACKED, 4, 4);
        register(pContext, BACKYARD, 3, 4);
        register(pContext, BOUQUET, 3, 3);
        register(pContext, CAVEBIRD, 3, 3);
        register(pContext, CHANGING, 4, 2);
        register(pContext, COTAN, 3, 3);
        register(pContext, ENDBOSS, 3, 3);
        register(pContext, FERN, 3, 3);
        register(pContext, FINDING, 4, 2);
        register(pContext, LOWMIST, 4, 2);
        register(pContext, ORB, 4, 4);
        register(pContext, OWLEMONS, 3, 3);
        register(pContext, PASSAGE, 4, 2);
        register(pContext, POND, 3, 4);
        register(pContext, SUNFLOWERS, 3, 3);
        register(pContext, TIDES, 3, 3);
    }

    private static void register(BootstrapContext<PaintingVariant> pContext, ResourceKey<PaintingVariant> pKey, int pWidth, int pHeight) {
        pContext.register(pKey, new PaintingVariant(pWidth, pHeight, pKey.location()));
    }

    private static ResourceKey<PaintingVariant> create(String pName) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, ResourceLocation.withDefaultNamespace(pName));
    }
}
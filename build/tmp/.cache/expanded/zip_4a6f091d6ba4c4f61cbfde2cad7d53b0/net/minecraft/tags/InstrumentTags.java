package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Instrument;

public interface InstrumentTags {
    TagKey<Instrument> REGULAR_GOAT_HORNS = create("regular_goat_horns");
    TagKey<Instrument> SCREAMING_GOAT_HORNS = create("screaming_goat_horns");
    TagKey<Instrument> GOAT_HORNS = create("goat_horns");

    private static TagKey<Instrument> create(String pName) {
        return TagKey.create(Registries.INSTRUMENT, ResourceLocation.withDefaultNamespace(pName));
    }

    public static TagKey<Instrument> create(String namepsace, String path) {
        return create(ResourceLocation.fromNamespaceAndPath(namepsace, path));
    }

    public static TagKey<Instrument> create(ResourceLocation name) {
        return TagKey.create(Registries.INSTRUMENT, name);
    }
}

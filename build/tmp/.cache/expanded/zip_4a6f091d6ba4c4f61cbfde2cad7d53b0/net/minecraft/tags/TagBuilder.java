package net.minecraft.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class TagBuilder implements net.minecraftforge.common.extensions.IForgeRawTagBuilder {
    private final List<TagEntry> entries = new ArrayList<>();

    public static TagBuilder create() {
        return new TagBuilder();
    }

    public List<TagEntry> build() {
        return List.copyOf(this.entries);
    }

    public TagBuilder add(TagEntry pEntry) {
        this.entries.add(pEntry);
        return this;
    }

    public TagBuilder addElement(ResourceLocation pElementLocation) {
        return this.add(TagEntry.element(pElementLocation));
    }

    public TagBuilder addOptionalElement(ResourceLocation pElementLocation) {
        return this.add(TagEntry.optionalElement(pElementLocation));
    }

    public TagBuilder addTag(ResourceLocation pTagLocation) {
        return this.add(TagEntry.tag(pTagLocation));
    }

    public TagBuilder addOptionalTag(ResourceLocation pTagLocation) {
        return this.add(TagEntry.optionalTag(pTagLocation));
    }

    /** Forge: Used for datagen */
    private final List<TagEntry> removeEntries = new ArrayList<>();
    private boolean replace = false;

    public java.util.stream.Stream<TagEntry> getRemoveEntries() { // should this return the List instead? Might end up with mem leaks from unterminated streams otherwise -Paint_Ninja
        return this.removeEntries.stream();
    }

    /** Forge: Add an entry to be removed from this tag in datagen */
    public TagBuilder remove(TagEntry entry) {
        this.removeEntries.add(entry);
        return this;
    }

    /** Forge: Set the replace property of this tag */
    public TagBuilder replace(boolean value) {
        this.replace = value;
        return this;
    }

    /** Forge: Is this tag set to replace or not? */
    public boolean isReplace() {
        return this.replace;
    }
}

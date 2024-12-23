package net.minecraft.world.item;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;

public class CreativeModeTab {
    static final ResourceLocation DEFAULT_BACKGROUND = createTextureLocation("items");
    private final Component displayName;
    ResourceLocation backgroundTexture = DEFAULT_BACKGROUND;
    boolean canScroll = true;
    boolean showTitle = true;
    boolean alignedRight = false;
    private final CreativeModeTab.Row row;
    private final int column;
    private final CreativeModeTab.Type type;
    @Nullable
    private ItemStack iconItemStack;
    private Collection<ItemStack> displayItems = ItemStackLinkedSet.createTypeAndComponentsSet();
    private Set<ItemStack> displayItemsSearchTab = ItemStackLinkedSet.createTypeAndComponentsSet();
    private final Supplier<ItemStack> iconGenerator;
    private final CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;
    private final boolean hasSearchBar;
    private final int searchBarWidth;
    private final int labelColor;
    private final int slotColor;
    public final java.util.List<net.minecraft.resources.ResourceLocation> tabsBefore;
    public final java.util.List<net.minecraft.resources.ResourceLocation> tabsAfter;

    CreativeModeTab(
        CreativeModeTab.Row pRow,
        int pColumn,
        CreativeModeTab.Type pType,
        Component pDisplayName,
        Supplier<ItemStack> pIconGenerator,
        CreativeModeTab.DisplayItemsGenerator pDisplayItemGenerator,
        Builder builder
    ) {
        this.row = pRow;
        this.column = pColumn;
        this.displayName = pDisplayName;
        this.iconGenerator = pIconGenerator;
        this.displayItemsGenerator = pDisplayItemGenerator;
        this.type = pType;
        this.hasSearchBar = builder.hasSearchBar;
        this.searchBarWidth = builder.searchBarWidth;
        this.labelColor = builder.labelColor;
        this.slotColor = builder.slotColor;
        this.tabsBefore = java.util.List.copyOf(builder.tabsBefore);
        this.tabsAfter = java.util.List.copyOf(builder.tabsAfter);
    }

    protected CreativeModeTab(CreativeModeTab.Builder builder) {
       this(builder.row, builder.column, builder.type, builder.displayName, builder.iconGenerator, builder.displayItemsGenerator, builder);
    }

    public static CreativeModeTab.Builder builder() {
       return new CreativeModeTab.Builder(Row.TOP, 0);
    }

    public static ResourceLocation createTextureLocation(String pName) {
        return ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_" + pName + ".png");
    }

    /** @deprecated Forge: use {@link #builder()} **/
    public static CreativeModeTab.Builder builder(CreativeModeTab.Row pRow, int pColumn) {
        return new CreativeModeTab.Builder(pRow, pColumn);
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack == null) {
            this.iconItemStack = this.iconGenerator.get();
        }

        return this.iconItemStack;
    }

    public ResourceLocation getBackgroundTexture() {
        return this.backgroundTexture;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public int column() {
        return this.column;
    }

    public CreativeModeTab.Row row() {
        return this.row;
    }

    public boolean hasAnyItems() {
        return !this.displayItems.isEmpty();
    }

    public boolean shouldDisplay() {
        return this.type != CreativeModeTab.Type.CATEGORY || this.hasAnyItems();
    }

    public boolean isAlignedRight() {
        return this.alignedRight;
    }

    public CreativeModeTab.Type getType() {
        return this.type;
    }

    public void buildContents(CreativeModeTab.ItemDisplayParameters pParameters) {
        CreativeModeTab.ItemDisplayBuilder creativemodetab$itemdisplaybuilder = new CreativeModeTab.ItemDisplayBuilder(this, pParameters.enabledFeatures);
        ResourceKey<CreativeModeTab> resourcekey = BuiltInRegistries.CREATIVE_MODE_TAB
            .getResourceKey(this)
            .orElseThrow(() -> new IllegalStateException("Unregistered creative tab: " + this));
        net.minecraftforge.common.ForgeHooks.onCreativeModeTabBuildContents(this, resourcekey, this.displayItemsGenerator, pParameters, creativemodetab$itemdisplaybuilder);
        this.displayItems = creativemodetab$itemdisplaybuilder.tabContents;
        this.displayItemsSearchTab = creativemodetab$itemdisplaybuilder.searchTabContents;
    }

    public Collection<ItemStack> getDisplayItems() {
        return this.displayItems;
    }

    public Collection<ItemStack> getSearchTabDisplayItems() {
        return this.displayItemsSearchTab;
    }

    public boolean contains(ItemStack pStack) {
        return this.displayItemsSearchTab.contains(pStack);
    }

    public boolean hasSearchBar() {
        return this.hasSearchBar;
    }

    public int getSearchBarWidth() {
        return searchBarWidth;
    }

    public int getLabelColor() {
        return labelColor;
    }

    public int getSlotColor() {
        return slotColor;
    }

    public static class Builder {
        private static final CreativeModeTab.DisplayItemsGenerator EMPTY_GENERATOR = (p_270422_, p_259433_) -> {
        };
        private final CreativeModeTab.Row row;
        private final int column;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator = EMPTY_GENERATOR;
        private boolean canScroll = true;
        private boolean showTitle = true;
        private boolean alignedRight = false;
        private CreativeModeTab.Type type = CreativeModeTab.Type.CATEGORY;
        private ResourceLocation backgroundTexture = CreativeModeTab.DEFAULT_BACKGROUND;
        private boolean hasSearchBar = false;
        private int searchBarWidth = 89;
        private int labelColor = 4210752;
        private int slotColor = -2130706433;
        private java.util.function.Function<CreativeModeTab.Builder, CreativeModeTab> tabFactory =
            builder -> new CreativeModeTab(builder.row, builder.column, builder.type, builder.displayName, builder.iconGenerator, builder.displayItemsGenerator, builder);
        private final java.util.List<net.minecraft.resources.ResourceLocation> tabsBefore = new java.util.ArrayList<>();
        private final java.util.List<net.minecraft.resources.ResourceLocation> tabsAfter = new java.util.ArrayList<>();

        public Builder(CreativeModeTab.Row pRow, int pColumn) {
            this.row = pRow;
            this.column = pColumn;
        }

        public CreativeModeTab.Builder title(Component pTitle) {
            this.displayName = pTitle;
            return this;
        }

        public CreativeModeTab.Builder icon(Supplier<ItemStack> pIcon) {
            this.iconGenerator = pIcon;
            return this;
        }

        public CreativeModeTab.Builder displayItems(CreativeModeTab.DisplayItemsGenerator pDisplayItemsGenerator) {
            this.displayItemsGenerator = pDisplayItemsGenerator;
            return this;
        }

        public CreativeModeTab.Builder alignedRight() {
            this.alignedRight = true;
            return this;
        }

        public CreativeModeTab.Builder hideTitle() {
            this.showTitle = false;
            return this;
        }

        public CreativeModeTab.Builder noScrollBar() {
            this.canScroll = false;
            return this;
        }

        protected CreativeModeTab.Builder type(CreativeModeTab.Type pType) {
            this.type = pType;
            if (pType == Type.SEARCH) {
                return this.withSearchBar();
            }
            return this;
        }

        public CreativeModeTab.Builder backgroundTexture(ResourceLocation pBackgroundTexture) {
            this.backgroundTexture = pBackgroundTexture;
            return this;
        }

        /**
         * Gives this tab a search bar.
         * <p>Note that, if using a custom {@link #withBackgroundLocation(net.minecraft.resources.ResourceLocation) background image}, you will need to make sure that your image contains the input box and the scroll bar.</p>
         */
        public CreativeModeTab.Builder withSearchBar() {
            this.hasSearchBar = true;
            if (this.backgroundTexture == CreativeModeTab.DEFAULT_BACKGROUND)
                return this.backgroundTexture(createTextureLocation("item_search"));
            return this;
        }

        /**
         * Gives this tab a search bar, with a specific width.
         * @param searchBarWidth the width of the search bar
         */
        public CreativeModeTab.Builder withSearchBar(int searchBarWidth) {
            this.searchBarWidth = searchBarWidth;
            return withSearchBar();
        }

        /**
         * Sets the color of the tab label.
         */
        public CreativeModeTab.Builder withLabelColor(int labelColor) {
            this.labelColor = labelColor;
            return this;
        }

        /**
         * Sets the color of tab's slots.
         */
        public CreativeModeTab.Builder withSlotColor(int slotColor) {
            this.slotColor = slotColor;
            return this;
        }

        public CreativeModeTab.Builder withTabFactory(java.util.function.Function<CreativeModeTab.Builder, CreativeModeTab> tabFactory) {
            this.tabFactory = tabFactory;
            return this;
        }

        /** Define tabs that should come <i>before</i> this tab. This tab will be placed <strong>after</strong> the {@code tabs}. **/
        public CreativeModeTab.Builder withTabsBefore(net.minecraft.resources.ResourceLocation... tabs) {
            this.tabsBefore.addAll(java.util.List.of(tabs));
            return this;
        }

        /** Define tabs that should come <i>after</i> this tab. This tab will be placed <strong>before</strong> the {@code tabs}.**/
        public CreativeModeTab.Builder withTabsAfter(net.minecraft.resources.ResourceLocation... tabs) {
            this.tabsAfter.addAll(java.util.List.of(tabs));
            return this;
        }

        /** Define tabs that should come <i>before</i> this tab. This tab will be placed <strong>after</strong> the {@code tabs}. **/
        @SafeVarargs
        public final CreativeModeTab.Builder withTabsBefore(net.minecraft.resources.ResourceKey<CreativeModeTab>... tabs) {
            java.util.stream.Stream.of(tabs).map(net.minecraft.resources.ResourceKey::location).forEach(this.tabsBefore::add);
            return this;
        }

        /** Define tabs that should come <i>after</i> this tab. This tab will be placed <strong>before</strong> the {@code tabs}.**/
        @SafeVarargs
        public final CreativeModeTab.Builder withTabsAfter(net.minecraft.resources.ResourceKey<CreativeModeTab>... tabs) {
            java.util.stream.Stream.of(tabs).map(net.minecraft.resources.ResourceKey::location).forEach(this.tabsAfter::add);
            return this;
        }

        public CreativeModeTab build() {
            if ((this.type == CreativeModeTab.Type.HOTBAR || this.type == CreativeModeTab.Type.INVENTORY) && this.displayItemsGenerator != EMPTY_GENERATOR) {
                throw new IllegalStateException("Special tabs can't have display items");
            } else {
                CreativeModeTab creativemodetab = this.tabFactory.apply(this);
                creativemodetab.alignedRight = this.alignedRight;
                creativemodetab.showTitle = this.showTitle;
                creativemodetab.canScroll = this.canScroll;
                creativemodetab.backgroundTexture = this.backgroundTexture;
                return creativemodetab;
            }
        }
    }

    @FunctionalInterface
    public interface DisplayItemsGenerator {
        void accept(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput);
    }

    static class ItemDisplayBuilder implements CreativeModeTab.Output {
        public final Collection<ItemStack> tabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        public final Set<ItemStack> searchTabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        private final CreativeModeTab tab;
        private final FeatureFlagSet featureFlagSet;

        public ItemDisplayBuilder(CreativeModeTab pTab, FeatureFlagSet pFeatureFlagSet) {
            this.tab = pTab;
            this.featureFlagSet = pFeatureFlagSet;
        }

        @Override
        public void accept(ItemStack pStack, CreativeModeTab.TabVisibility pTabVisibility) {
            if (pStack.getCount() != 1) {
                throw new IllegalArgumentException("Stack size must be exactly 1");
            } else {
                boolean flag = this.tabContents.contains(pStack) && pTabVisibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY;
                if (flag) {
                    throw new IllegalStateException(
                        "Accidentally adding the same item stack twice "
                            + pStack.getDisplayName().getString()
                            + " to a Creative Mode Tab: "
                            + this.tab.getDisplayName().getString()
                    );
                } else {
                    if (pStack.getItem().isEnabled(this.featureFlagSet)) {
                        switch (pTabVisibility) {
                            case PARENT_AND_SEARCH_TABS:
                                this.tabContents.add(pStack);
                                this.searchTabContents.add(pStack);
                                break;
                            case PARENT_TAB_ONLY:
                                this.tabContents.add(pStack);
                                break;
                            case SEARCH_TAB_ONLY:
                                this.searchTabContents.add(pStack);
                        }
                    }
                }
            }
        }
    }

    public static record ItemDisplayParameters(FeatureFlagSet enabledFeatures, boolean hasPermissions, HolderLookup.Provider holders) {
        public boolean needsUpdate(FeatureFlagSet pEnabledFeatures, boolean pHasPermissions, HolderLookup.Provider pHolders) {
            return !this.enabledFeatures.equals(pEnabledFeatures) || this.hasPermissions != pHasPermissions || this.holders != pHolders;
        }
    }

    public interface Output {
        void accept(ItemStack pStack, CreativeModeTab.TabVisibility pTabVisibility);

        default void accept(ItemStack pStack) {
            this.accept(pStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default void accept(ItemLike pItem, CreativeModeTab.TabVisibility pTabVisibility) {
            this.accept(new ItemStack(pItem), pTabVisibility);
        }

        default void accept(ItemLike pItem) {
            this.accept(new ItemStack(pItem), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default void acceptAll(Collection<ItemStack> pStacks, CreativeModeTab.TabVisibility pTabVisibility) {
            pStacks.forEach(p_252337_ -> this.accept(p_252337_, pTabVisibility));
        }

        default void acceptAll(Collection<ItemStack> pStacks) {
            this.acceptAll(pStacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    public static enum Row {
        TOP,
        BOTTOM;
    }

    public static enum TabVisibility {
        PARENT_AND_SEARCH_TABS,
        PARENT_TAB_ONLY,
        SEARCH_TAB_ONLY;
    }

    public static enum Type {
        CATEGORY,
        INVENTORY,
        HOTBAR,
        SEARCH;
    }
}

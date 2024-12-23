package net.minecraft.client.gui.screens.achievement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StatsScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.stats");
    static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    static final ResourceLocation HEADER_SPRITE = ResourceLocation.withDefaultNamespace("statistics/header");
    static final ResourceLocation SORT_UP_SPRITE = ResourceLocation.withDefaultNamespace("statistics/sort_up");
    static final ResourceLocation SORT_DOWN_SPRITE = ResourceLocation.withDefaultNamespace("statistics/sort_down");
    private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
    static final Component NO_VALUE_DISPLAY = Component.translatable("stats.none");
    private static final Component GENERAL_BUTTON = Component.translatable("stat.generalButton");
    private static final Component ITEMS_BUTTON = Component.translatable("stat.itemsButton");
    private static final Component MOBS_BUTTON = Component.translatable("stat.mobsButton");
    protected final Screen lastScreen;
    private static final int LIST_WIDTH = 280;
    private static final int PADDING = 5;
    private static final int FOOTER_HEIGHT = 58;
    private HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 58);
    @Nullable
    private StatsScreen.GeneralStatisticsList statsList;
    @Nullable
    StatsScreen.ItemStatisticsList itemStatsList;
    @Nullable
    private StatsScreen.MobsStatisticsList mobsStatsList;
    final StatsCounter stats;
    @Nullable
    private ObjectSelectionList<?> activeList;
    private boolean isLoading = true;

    public StatsScreen(Screen pLastScreen, StatsCounter pStats) {
        super(TITLE);
        this.lastScreen = pLastScreen;
        this.stats = pStats;
    }

    @Override
    protected void init() {
        this.layout.addToContents(new LoadingDotsWidget(this.font, PENDING_TEXT));
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    public void initLists() {
        this.statsList = new StatsScreen.GeneralStatisticsList(this.minecraft);
        this.itemStatsList = new StatsScreen.ItemStatisticsList(this.minecraft);
        this.mobsStatsList = new StatsScreen.MobsStatisticsList(this.minecraft);
    }

    public void initButtons() {
        HeaderAndFooterLayout headerandfooterlayout = new HeaderAndFooterLayout(this, 33, 58);
        headerandfooterlayout.addTitleHeader(TITLE, this.font);
        LinearLayout linearlayout = headerandfooterlayout.addToFooter(LinearLayout.vertical()).spacing(5);
        linearlayout.defaultCellSetting().alignHorizontallyCenter();
        LinearLayout linearlayout1 = linearlayout.addChild(LinearLayout.horizontal()).spacing(5);
        linearlayout1.addChild(Button.builder(GENERAL_BUTTON, p_96963_ -> this.setActiveList(this.statsList)).width(120).build());
        Button button = linearlayout1.addChild(Button.builder(ITEMS_BUTTON, p_96959_ -> this.setActiveList(this.itemStatsList)).width(120).build());
        Button button1 = linearlayout1.addChild(Button.builder(MOBS_BUTTON, p_96949_ -> this.setActiveList(this.mobsStatsList)).width(120).build());
        linearlayout.addChild(Button.builder(CommonComponents.GUI_DONE, p_325372_ -> this.onClose()).width(200).build());
        if (this.itemStatsList != null && this.itemStatsList.children().isEmpty()) {
            button.active = false;
        }

        if (this.mobsStatsList != null && this.mobsStatsList.children().isEmpty()) {
            button1.active = false;
        }

        this.layout = headerandfooterlayout;
        this.layout.visitWidgets(p_325374_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_325374_);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.activeList != null) {
            this.activeList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    public void onStatsUpdated() {
        if (this.isLoading) {
            this.initLists();
            this.setActiveList(this.statsList);
            this.initButtons();
            this.setInitialFocus();
            this.isLoading = false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return !this.isLoading;
    }

    public void setActiveList(@Nullable ObjectSelectionList<?> pActiveList) {
        if (this.activeList != null) {
            this.removeWidget(this.activeList);
        }

        if (pActiveList != null) {
            this.addRenderableWidget(pActiveList);
            this.activeList = pActiveList;
            this.repositionElements();
        }
    }

    static String getTranslationKey(Stat<ResourceLocation> pStat) {
        return "stat." + pStat.getValue().toString().replace(':', '.');
    }

    @OnlyIn(Dist.CLIENT)
    class GeneralStatisticsList extends ObjectSelectionList<StatsScreen.GeneralStatisticsList.Entry> {
        public GeneralStatisticsList(final Minecraft pMinecraft) {
            super(pMinecraft, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 14);
            ObjectArrayList<Stat<ResourceLocation>> objectarraylist = new ObjectArrayList<>(Stats.CUSTOM.iterator());
            objectarraylist.sort(Comparator.comparing(p_96997_ -> I18n.get(StatsScreen.getTranslationKey((Stat<ResourceLocation>)p_96997_))));

            for (Stat<ResourceLocation> stat : objectarraylist) {
                this.addEntry(new StatsScreen.GeneralStatisticsList.Entry(stat));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ObjectSelectionList.Entry<StatsScreen.GeneralStatisticsList.Entry> {
            private final Stat<ResourceLocation> stat;
            private final Component statDisplay;

            Entry(final Stat<ResourceLocation> pStat) {
                this.stat = pStat;
                this.statDisplay = Component.translatable(StatsScreen.getTranslationKey(pStat));
            }

            private String getValueText() {
                return this.stat.format(StatsScreen.this.stats.getValue(this.stat));
            }

            @Override
            public void render(
                GuiGraphics pGuiGraphics,
                int pIndex,
                int pTop,
                int pLeft,
                int pWidth,
                int pHeight,
                int pMouseX,
                int pMouseY,
                boolean pHovering,
                float pPartialTick
            ) {
                int i = pTop + pHeight / 2 - 9 / 2;
                int j = pIndex % 2 == 0 ? -1 : -4539718;
                pGuiGraphics.drawString(StatsScreen.this.font, this.statDisplay, pLeft + 2, i, j);
                String s = this.getValueText();
                pGuiGraphics.drawString(StatsScreen.this.font, s, pLeft + pWidth - StatsScreen.this.font.width(s) - 4, i, j);
            }

            @Override
            public Component getNarration() {
                return Component.translatable(
                    "narrator.select", Component.empty().append(this.statDisplay).append(CommonComponents.SPACE).append(this.getValueText())
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ItemStatisticsList extends ObjectSelectionList<StatsScreen.ItemStatisticsList.ItemRow> {
        private static final int SLOT_BG_SIZE = 18;
        private static final int SLOT_STAT_HEIGHT = 22;
        private static final int SLOT_BG_Y = 1;
        private static final int SORT_NONE = 0;
        private static final int SORT_DOWN = -1;
        private static final int SORT_UP = 1;
        private final ResourceLocation[] iconSprites = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("statistics/block_mined"),
            ResourceLocation.withDefaultNamespace("statistics/item_broken"),
            ResourceLocation.withDefaultNamespace("statistics/item_crafted"),
            ResourceLocation.withDefaultNamespace("statistics/item_used"),
            ResourceLocation.withDefaultNamespace("statistics/item_picked_up"),
            ResourceLocation.withDefaultNamespace("statistics/item_dropped")
        };
        protected final List<StatType<Block>> blockColumns;
        protected final List<StatType<Item>> itemColumns;
        protected final Comparator<StatsScreen.ItemStatisticsList.ItemRow> itemStatSorter = new StatsScreen.ItemStatisticsList.ItemRowComparator();
        @Nullable
        protected StatType<?> sortColumn;
        protected int headerPressed = -1;
        protected int sortOrder;

        public ItemStatisticsList(final Minecraft pMinecraft) {
            super(pMinecraft, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 22);
            this.blockColumns = Lists.newArrayList();
            this.blockColumns.add(Stats.BLOCK_MINED);
            this.itemColumns = Lists.newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);
            this.setRenderHeader(true, 22);
            Set<Item> set = Sets.newIdentityHashSet();

            for (Item item : BuiltInRegistries.ITEM) {
                boolean flag = false;

                for (StatType<Item> stattype : this.itemColumns) {
                    if (stattype.contains(item) && StatsScreen.this.stats.getValue(stattype.get(item)) > 0) {
                        flag = true;
                    }
                }

                if (flag) {
                    set.add(item);
                }
            }

            for (Block block : BuiltInRegistries.BLOCK) {
                boolean flag1 = false;

                for (StatType<Block> stattype1 : this.blockColumns) {
                    if (stattype1.contains(block) && StatsScreen.this.stats.getValue(stattype1.get(block)) > 0) {
                        flag1 = true;
                    }
                }

                if (flag1) {
                    set.add(block.asItem());
                }
            }

            set.remove(Items.AIR);

            for (Item item1 : set) {
                this.addEntry(new StatsScreen.ItemStatisticsList.ItemRow(item1));
            }
        }

        int getColumnX(int pIndex) {
            return 75 + 40 * pIndex;
        }

        @Override
        protected void renderHeader(GuiGraphics pGuiGraphics, int pX, int pY) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.headerPressed = -1;
            }

            for (int i = 0; i < this.iconSprites.length; i++) {
                ResourceLocation resourcelocation = this.headerPressed == i ? StatsScreen.SLOT_SPRITE : StatsScreen.HEADER_SPRITE;
                pGuiGraphics.blitSprite(resourcelocation, pX + this.getColumnX(i) - 18, pY + 1, 0, 18, 18);
            }

            if (this.sortColumn != null) {
                int j = this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
                ResourceLocation resourcelocation1 = this.sortOrder == 1 ? StatsScreen.SORT_UP_SPRITE : StatsScreen.SORT_DOWN_SPRITE;
                pGuiGraphics.blitSprite(resourcelocation1, pX + j, pY + 1, 0, 18, 18);
            }

            for (int k = 0; k < this.iconSprites.length; k++) {
                int l = this.headerPressed == k ? 1 : 0;
                pGuiGraphics.blitSprite(this.iconSprites[k], pX + this.getColumnX(k) - 18 + l, pY + 1 + l, 0, 18, 18);
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Override
        protected boolean clickedHeader(int pX, int pY) {
            this.headerPressed = -1;

            for (int i = 0; i < this.iconSprites.length; i++) {
                int j = pX - this.getColumnX(i);
                if (j >= -36 && j <= 0) {
                    this.headerPressed = i;
                    break;
                }
            }

            if (this.headerPressed >= 0) {
                this.sortByColumn(this.getColumn(this.headerPressed));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else {
                return super.clickedHeader(pX, pY);
            }
        }

        private StatType<?> getColumn(int pIndex) {
            return pIndex < this.blockColumns.size() ? this.blockColumns.get(pIndex) : this.itemColumns.get(pIndex - this.blockColumns.size());
        }

        private int getColumnIndex(StatType<?> pStatType) {
            int i = this.blockColumns.indexOf(pStatType);
            if (i >= 0) {
                return i;
            } else {
                int j = this.itemColumns.indexOf(pStatType);
                return j >= 0 ? j + this.blockColumns.size() : -1;
            }
        }

        @Override
        protected void renderDecorations(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
            if (pMouseY >= this.getY() && pMouseY <= this.getBottom()) {
                StatsScreen.ItemStatisticsList.ItemRow statsscreen$itemstatisticslist$itemrow = this.getHovered();
                int i = this.getRowLeft();
                if (statsscreen$itemstatisticslist$itemrow != null) {
                    if (pMouseX < i || pMouseX > i + 18) {
                        return;
                    }

                    Item item = statsscreen$itemstatisticslist$itemrow.getItem();
                    pGuiGraphics.renderTooltip(StatsScreen.this.font, item.getDescription(), pMouseX, pMouseY);
                } else {
                    Component component = null;
                    int j = pMouseX - i;

                    for (int k = 0; k < this.iconSprites.length; k++) {
                        int l = this.getColumnX(k);
                        if (j >= l - 18 && j <= l) {
                            component = this.getColumn(k).getDisplayName();
                            break;
                        }
                    }

                    if (component != null) {
                        pGuiGraphics.renderTooltip(StatsScreen.this.font, component, pMouseX, pMouseY);
                    }
                }
            }
        }

        protected void sortByColumn(StatType<?> pStatType) {
            if (pStatType != this.sortColumn) {
                this.sortColumn = pStatType;
                this.sortOrder = -1;
            } else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            } else {
                this.sortColumn = null;
                this.sortOrder = 0;
            }

            this.children().sort(this.itemStatSorter);
        }

        @OnlyIn(Dist.CLIENT)
        class ItemRow extends ObjectSelectionList.Entry<StatsScreen.ItemStatisticsList.ItemRow> {
            private final Item item;

            ItemRow(final Item pItem) {
                this.item = pItem;
            }

            public Item getItem() {
                return this.item;
            }

            @Override
            public void render(
                GuiGraphics pGuiGraphics,
                int pIndex,
                int pTop,
                int pLeft,
                int pWidth,
                int pHeight,
                int pMouseX,
                int pMouseY,
                boolean pHovering,
                float pPartialTick
            ) {
                pGuiGraphics.blitSprite(StatsScreen.SLOT_SPRITE, pLeft, pTop, 0, 18, 18);
                pGuiGraphics.renderFakeItem(this.item.getDefaultInstance(), pLeft + 1, pTop + 1);
                if (StatsScreen.this.itemStatsList != null) {
                    for (int i = 0; i < StatsScreen.this.itemStatsList.blockColumns.size(); i++) {
                        Stat<Block> stat;
                        if (this.item instanceof BlockItem blockitem) {
                            stat = StatsScreen.this.itemStatsList.blockColumns.get(i).get(blockitem.getBlock());
                        } else {
                            stat = null;
                        }

                        this.renderStat(pGuiGraphics, stat, pLeft + ItemStatisticsList.this.getColumnX(i), pTop + pHeight / 2 - 9 / 2, pIndex % 2 == 0);
                    }

                    for (int j = 0; j < StatsScreen.this.itemStatsList.itemColumns.size(); j++) {
                        this.renderStat(
                            pGuiGraphics,
                            StatsScreen.this.itemStatsList.itemColumns.get(j).get(this.item),
                            pLeft + ItemStatisticsList.this.getColumnX(j + StatsScreen.this.itemStatsList.blockColumns.size()),
                            pTop + pHeight / 2 - 9 / 2,
                            pIndex % 2 == 0
                        );
                    }
                }
            }

            protected void renderStat(GuiGraphics pGuiGraphics, @Nullable Stat<?> pStat, int pX, int pY, boolean pEvenRow) {
                Component component = (Component)(pStat == null
                    ? StatsScreen.NO_VALUE_DISPLAY
                    : Component.literal(pStat.format(StatsScreen.this.stats.getValue(pStat))));
                pGuiGraphics.drawString(
                    StatsScreen.this.font, component, pX - StatsScreen.this.font.width(component), pY, pEvenRow ? -1 : -4539718
                );
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.item.getDescription());
            }
        }

        @OnlyIn(Dist.CLIENT)
        class ItemRowComparator implements Comparator<StatsScreen.ItemStatisticsList.ItemRow> {
            public int compare(StatsScreen.ItemStatisticsList.ItemRow pRow1, StatsScreen.ItemStatisticsList.ItemRow pRow2) {
                Item item = pRow1.getItem();
                Item item1 = pRow2.getItem();
                int i;
                int j;
                if (ItemStatisticsList.this.sortColumn == null) {
                    i = 0;
                    j = 0;
                } else if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
                    StatType<Block> stattype = (StatType<Block>)ItemStatisticsList.this.sortColumn;
                    i = item instanceof BlockItem ? StatsScreen.this.stats.getValue(stattype, ((BlockItem)item).getBlock()) : -1;
                    j = item1 instanceof BlockItem ? StatsScreen.this.stats.getValue(stattype, ((BlockItem)item1).getBlock()) : -1;
                } else {
                    StatType<Item> stattype1 = (StatType<Item>)ItemStatisticsList.this.sortColumn;
                    i = StatsScreen.this.stats.getValue(stattype1, item);
                    j = StatsScreen.this.stats.getValue(stattype1, item1);
                }

                return i == j
                    ? ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(item), Item.getId(item1))
                    : ItemStatisticsList.this.sortOrder * Integer.compare(i, j);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class MobsStatisticsList extends ObjectSelectionList<StatsScreen.MobsStatisticsList.MobRow> {
        public MobsStatisticsList(final Minecraft pMinecraft) {
            super(pMinecraft, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 9 * 4);

            for (EntityType<?> entitytype : BuiltInRegistries.ENTITY_TYPE) {
                if (StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entitytype)) > 0
                    || StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entitytype)) > 0) {
                    this.addEntry(new StatsScreen.MobsStatisticsList.MobRow(entitytype));
                }
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @OnlyIn(Dist.CLIENT)
        class MobRow extends ObjectSelectionList.Entry<StatsScreen.MobsStatisticsList.MobRow> {
            private final Component mobName;
            private final Component kills;
            private final Component killedBy;
            private final boolean hasKills;
            private final boolean wasKilledBy;

            public MobRow(final EntityType<?> pEntityType) {
                this.mobName = pEntityType.getDescription();
                int i = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(pEntityType));
                if (i == 0) {
                    this.kills = Component.translatable("stat_type.minecraft.killed.none", this.mobName);
                    this.hasKills = false;
                } else {
                    this.kills = Component.translatable("stat_type.minecraft.killed", i, this.mobName);
                    this.hasKills = true;
                }

                int j = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(pEntityType));
                if (j == 0) {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by.none", this.mobName);
                    this.wasKilledBy = false;
                } else {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by", this.mobName, j);
                    this.wasKilledBy = true;
                }
            }

            @Override
            public void render(
                GuiGraphics pGuiGraphics,
                int pIndex,
                int pTop,
                int pLeft,
                int pWidth,
                int pHeight,
                int pMouseX,
                int pMouseY,
                boolean pHovering,
                float pPartialTick
            ) {
                pGuiGraphics.drawString(StatsScreen.this.font, this.mobName, pLeft + 2, pTop + 1, -1);
                pGuiGraphics.drawString(StatsScreen.this.font, this.kills, pLeft + 2 + 10, pTop + 1 + 9, this.hasKills ? -4539718 : -8355712);
                pGuiGraphics.drawString(StatsScreen.this.font, this.killedBy, pLeft + 2 + 10, pTop + 1 + 9 * 2, this.wasKilledBy ? -4539718 : -8355712);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
            }
        }
    }
}
package net.minecraft.client.gui.screens.options.controls;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public class KeyBindsList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
    private static final int ITEM_HEIGHT = 20;
    final KeyBindsScreen keyBindsScreen;
    private int maxNameWidth;

    public KeyBindsList(KeyBindsScreen pKeyBindsScreen, Minecraft pMinecraft) {
        super(pMinecraft, pKeyBindsScreen.width, pKeyBindsScreen.layout.getContentHeight(), pKeyBindsScreen.layout.getHeaderHeight(), 20);
        this.keyBindsScreen = pKeyBindsScreen;
        KeyMapping[] akeymapping = ArrayUtils.clone((KeyMapping[])pMinecraft.options.keyMappings);
        Arrays.sort((Object[])akeymapping);
        String s = null;

        for (KeyMapping keymapping : akeymapping) {
            String s1 = keymapping.getCategory();
            if (!s1.equals(s)) {
                s = s1;
                this.addEntry(new KeyBindsList.CategoryEntry(Component.translatable(s1)));
            }

            Component component = Component.translatable(keymapping.getName());
            int i = pMinecraft.font.width(component);
            if (i > this.maxNameWidth) {
                this.maxNameWidth = i;
            }

            this.addEntry(new KeyBindsList.KeyEntry(keymapping, component));
        }
    }

    public void resetMappingAndUpdateButtons() {
        KeyMapping.resetMapping();
        this.refreshEntries();
    }

    public void refreshEntries() {
        this.children().forEach(KeyBindsList.Entry::refreshEntry);
    }

    @Override
    public int getRowWidth() {
        return 340;
    }

    @Override
    protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 20;
    }

    @OnlyIn(Dist.CLIENT)
    public class CategoryEntry extends KeyBindsList.Entry {
        final Component name;
        private final int width;

        public CategoryEntry(final Component pName) {
            this.name = pName;
            this.width = KeyBindsList.this.minecraft.font.width(this.name);
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
            pGuiGraphics.drawString(
                KeyBindsList.this.minecraft.font,
                this.name,
                KeyBindsList.this.width / 2 - this.width / 2,
                pTop + pHeight - 9 - 1,
                -1,
                false
            );
        }

        @Nullable
        @Override
        public ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput p_343060_) {
                    p_343060_.add(NarratedElementType.TITLE, CategoryEntry.this.name);
                }
            });
        }

        @Override
        protected void refreshEntry() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<KeyBindsList.Entry> {
        abstract void refreshEntry();
    }

    @OnlyIn(Dist.CLIENT)
    public class KeyEntry extends KeyBindsList.Entry {
        private static final Component RESET_BUTTON_TITLE = Component.translatable("controls.reset");
        private static final int PADDING = 10;
        private final KeyMapping key;
        private final Component name;
        private final Button changeButton;
        private final Button resetButton;
        private boolean hasCollision = false;

        KeyEntry(final KeyMapping pKey, final Component pName) {
            this.key = pKey;
            this.name = pName;
            this.changeButton = Button.builder(pName, p_342196_ -> {
                    KeyBindsList.this.keyBindsScreen.selectedKey = pKey;
                    pKey.setKeyModifierAndCode(null, com.mojang.blaze3d.platform.InputConstants.UNKNOWN);
                    KeyBindsList.this.resetMappingAndUpdateButtons();
                })
                .bounds(0, 0, 75 + 20 /* Forge: Add space */, 20)
                .createNarration(
                    p_342179_ -> pKey.isUnbound()
                            ? Component.translatable("narrator.controls.unbound", pName)
                            : Component.translatable("narrator.controls.bound", pName, p_342179_.get())
                )
                .build();
            this.resetButton = Button.builder(RESET_BUTTON_TITLE, p_343650_ -> {
                this.key.setToDefault();
                KeyBindsList.this.minecraft.options.setKey(pKey, pKey.getDefaultKey());
                KeyBindsList.this.resetMappingAndUpdateButtons();
            }).bounds(0, 0, 50, 20).createNarration(p_344192_ -> Component.translatable("narrator.controls.reset", pName)).build();
            this.refreshEntry();
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
            int i = KeyBindsList.this.getScrollbarPosition() - this.resetButton.getWidth() - 10;
            int j = pTop - 2;
            this.resetButton.setPosition(i, j);
            this.resetButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            int k = i - 5 - this.changeButton.getWidth();
            this.changeButton.setPosition(k, j);
            this.changeButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            pGuiGraphics.drawString(KeyBindsList.this.minecraft.font, this.name, pLeft, pTop + pHeight / 2 - 9 / 2, -1);
            if (this.hasCollision) {
                int l = 3;
                int i1 = this.changeButton.getX() - 6;
                pGuiGraphics.fill(i1, pTop - 1, i1 + 3, pTop + pHeight, -65536);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        @Override
        protected void refreshEntry() {
            this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
            this.resetButton.active = !this.key.isDefault();
            this.hasCollision = false;
            MutableComponent mutablecomponent = Component.empty();
            if (!this.key.isUnbound()) {
                for (KeyMapping keymapping : KeyBindsList.this.minecraft.options.keyMappings) {
                    if ((keymapping != this.key && this.key.same(keymapping)) || keymapping.hasKeyModifierConflict(this.key)) { // FORGE: gracefully handle conflicts like SHIFT vs SHIFT+G
                        if (this.hasCollision) {
                            mutablecomponent.append(", ");
                        }

                        this.hasCollision = true;
                        mutablecomponent.append(Component.translatable(keymapping.getName()));
                    }
                }
            }

            if (this.hasCollision) {
                this.changeButton
                    .setMessage(
                        Component.literal("[ ")
                            .append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE))
                            .append(" ]")
                            .withStyle(ChatFormatting.RED)
                    );
                this.changeButton.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", mutablecomponent)));
            } else {
                this.changeButton.setTooltip(null);
            }

            if (KeyBindsList.this.keyBindsScreen.selectedKey == this.key) {
                this.changeButton
                    .setMessage(
                        Component.literal("> ")
                            .append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                            .append(" <")
                            .withStyle(ChatFormatting.YELLOW)
                    );
            }
        }
    }
}

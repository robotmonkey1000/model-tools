package studio.robotmonkey.model_tools.gui.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import studio.robotmonkey.model_tools.ModelToolsClient;

public class ToggleItemWidget extends ImageButton {

    private final Minecraft minecraft;
    private final int offsetX = 2;
    private final int offsetY = 2;
    private final ItemStack itemStack;
    private final boolean decorations;
    private final boolean tooltip;

    protected final OnPress onPress;

    private ToggleGroup group;

    public ToggleItemWidget(Minecraft minecraft, int x, int y, int width, int height, Component tooltip, ItemStack itemStack, boolean hasDecorations, boolean showTooltip, OnPress onPress) {
        super(x, y, width, height, new WidgetSprites(Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"), Identifier.withDefaultNamespace("widget/button_highlighted")), onPress);
        this.minecraft = minecraft;
//        this.offsetX = i;
//        this.offsetY = j;
        this.itemStack = itemStack;
        this.setTooltip(Tooltip.create(tooltip));
        this.decorations = hasDecorations;
        this.tooltip = showTooltip;
        setTooltip(Tooltip.create(tooltip));

        this.onPress = onPress;

//        WidgetSprites sprites = new WidgetSprites(Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"), Identifier.withDefaultNamespace("widget/button_highlighted"));
//        initTextureValues(sprites);
    }

    public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderContents(guiGraphics, i, j, f);
        guiGraphics.renderItem(this.itemStack, this.getX() + this.offsetX, this.getY() + this.offsetY, 0);
//        if (this.decorations) {
//            guiGraphics.renderItemDecorations(this.minecraft.font, this.itemStack, this.getX() + this.offsetX, this.getY() + this.offsetY, (String)null);
//        }
//
//        if (this.isFocused()) {
//            guiGraphics.submitOutline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), -1);
//        }
//
//        if (this.tooltip && this.isHoveredOrFocused()) {
//            this.renderTooltip(guiGraphics, i, j);
//        }

    }

    protected void renderTooltip(GuiGraphics guiGraphics, int i, int j) {
        guiGraphics.setTooltipForNextFrame(this.minecraft.font, this.itemStack, i, j);
    }

    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narration.item", new Object[]{this.itemStack.getHoverName()}));
    }

    public void setToggleGroup(ToggleGroup group) {
        this.group = group;
    }

    public void onPress(InputWithModifiers inputWithModifiers) {
        if(!this.isActive()) return;
        if(group != null)
        {
            group.WidgetPressed(this);
        }

        this.onPress.onPress(this);
    }

    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
        this.onPress(mouseButtonEvent);
    }

//    @Environment(EnvType.CLIENT)
//    public interface OnPress extends Button.OnPress {
//        void onPress(ToggleItemWidget button);
//    }
}

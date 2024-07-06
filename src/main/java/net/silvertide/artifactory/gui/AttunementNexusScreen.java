package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.silvertide.artifactory.Artifactory;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class AttunementNexusScreen extends AbstractContainerScreen<AttunementNexusMenu> {
    private static final int ATTUNE_BUTTON_X = 61;
    private static final int ATTUNE_BUTTON_Y = 65;
    private static final int ATTUNE_BUTTON_WIDTH = 54;
    private static final int ATTUNE_BUTTON_HEIGHT = 12;
    private boolean buttonDown = false;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/attunement_nexus.png");

    public AttunementNexusScreen(AttunementNexusMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        // Move the label to get rid of it
        this.inventoryLabelY = 10000;
        this.inventoryLabelX = 10000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderButtons(guiGraphics, mouseX, mouseY);

        renderCostTooltip(guiGraphics, mouseX, mouseY);
//        renderProgressGraphic(guiGraphics, x, y);
    }

//    private void renderProgressGraphic(GuiGraphics guiGraphics, int x, int y) {
//        if(menu.getProgress() > 0) {
//            guiGraphics.blit(TEXTURE, x + 85, y + 30, 176, 0, 8, menu.getScaledProgress());
//        }
//    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = leftPos + ATTUNE_BUTTON_X;
        int buttonY = topPos + ATTUNE_BUTTON_Y;

        int buttonOffset = getButtonOffsetToRender(mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 177, buttonOffset, ATTUNE_BUTTON_WIDTH, ATTUNE_BUTTON_HEIGHT);

        int buttonTextX = buttonX + ATTUNE_BUTTON_WIDTH / 2;
        int buttonTextY = buttonY + ATTUNE_BUTTON_HEIGHT / 2;
        Component buttonTextComp = Component.literal(getButtonText());

        guiGraphics.drawWordWrap(this.font, buttonTextComp, buttonTextX - this.font.width(buttonTextComp)/2, buttonTextY - this.font.lineHeight/2, ATTUNE_BUTTON_WIDTH, 0xFFFFFF);
    }

    private String getButtonText() {
        if(menu.getProgress() > 0) {
            return "Cancel";
        } else if (menu.canItemAscend()) {
            return "Attune";
        } else {
            return "";
        }
    }

    private String getButtonTextColor() {
        if(menu.getProgress() > 0) {
            return "Cancel";
        } else if (menu.canItemAscend()) {
            return "Attune";
        } else {
            return "";
        }
    }

    private void renderCostTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(isHoveringAttuneButton(mouseX, mouseY)) {
            List<Component> list = Lists.newArrayList();
            if(menu.canItemAscend()) {
                list.add(Component.translatable("screen.tooltip.artifactory.xp_level_threshold", menu.getThreshold()));
                list.add(Component.translatable("screen.tooltip.artifactory.xp_levels_consumed", menu.getCost()));
            } else {
                list.add(Component.literal("Place an item to be attuned or ascended."));
            }

//        int currentAttunementLevel = attunementRenderContext.getAttunementLevel();
//        if(currentAttunementLevel < 0) {
//            list.add(Component.translatable("screen.tooltip.artifactory.attuned_to_other_player"));
//        } else if (currentAttunementLevel == 0) {
//            list.add(Component.translatable("screen.tooltip.artifactory.able_to_attune"));
//        } else {
//            //TODO: Check if ascension is allowed for rendering here.
//            list.add(Component.translatable("screen.tooltip.artifactory.ascend", currentAttunementLevel + 1));
//            list.add(Component.translatable("screen.tooltip.artifactory.max_attunement_reached"));
//        }

            guiGraphics.renderComponentTooltip(this.font, list, mouseX, mouseY);
        }
    }

    private int getButtonOffsetToRender(int mouseX, int mouseY) {
        if(!menu.canItemAscend()) {
            return 39;
        }
        else if(buttonDown) {
            return 26;
        }
        else if (isHoveringAttuneButton(mouseX, mouseY)) {
            return 13;
        }
        else {
            return 0;
        }
    }

    private boolean isHoveringAttuneButton(double mouseX, double mouseY) {
        return isHovering(ATTUNE_BUTTON_X, ATTUNE_BUTTON_Y, ATTUNE_BUTTON_WIDTH, ATTUNE_BUTTON_HEIGHT, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        buttonDown = false;
        if(isHoveringAttuneButton(mouseX, mouseY)) {
            if(this.minecraft != null && this.minecraft.gameMode != null && menu.canItemAscend()) {
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isHoveringAttuneButton(mouseX, mouseY)) {
            buttonDown = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

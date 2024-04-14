package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.silvertide.artifactory.Artifactory;

public class AttunementNexusScreen extends AbstractContainerScreen<AttunementNexusMenu> {
    private static final int ATTUNE_BUTTON_X = 61;
    private static final int ATTUNE_BUTTON_Y = 55;
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        buttonDown = false;
        if(clickedOnButton(mouseX, mouseY) && this.menu.attunementCanBegin()){
            if(this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(clickedOnButton(mouseX, mouseY)) {
            buttonDown = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickedOnButton(double mouseX, double mouseY) {
        int startX = leftPos + ATTUNE_BUTTON_X;
        int finishX = startX + ATTUNE_BUTTON_WIDTH;

        int startY = topPos + ATTUNE_BUTTON_Y;
        int finishY = startY + ATTUNE_BUTTON_HEIGHT;

        boolean inButtonX = mouseX >= startX && mouseX <= finishX;
        boolean inButtonY = mouseY >= startY && mouseY <= finishY;
        return inButtonY && inButtonX;
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
//        renderProgressArrow(guiGraphics, x, y);
    }

//    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
//        if(menu.isCrafting()) {
//            guiGraphics.blit(TEXTURE, x + 85, y + 30, 176, 0, 8, menu.getScaledProgress());
//        }
//    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = leftPos + ATTUNE_BUTTON_X;
        int buttonY = topPos + ATTUNE_BUTTON_Y;

        int buttonOffset = getButtonOffsetToRender(mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 177, buttonOffset, ATTUNE_BUTTON_WIDTH, ATTUNE_BUTTON_HEIGHT);
    }

    private int getButtonOffsetToRender(int mouseX, int mouseY) {
        if(!this.menu.attunementCanBegin()) {
            return 39;
        }
        else if(buttonDown) {
            return 26;
        }
        else if (isHovering(ATTUNE_BUTTON_X, ATTUNE_BUTTON_Y, ATTUNE_BUTTON_WIDTH, ATTUNE_BUTTON_HEIGHT, mouseX, mouseY)) {
            return 13;
        }
        else {
            return 0;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

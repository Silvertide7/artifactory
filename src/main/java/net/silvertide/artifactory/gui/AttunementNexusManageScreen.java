package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.silvertide.artifactory.Artifactory;

public class AttunementNexusManageScreen extends AbstractContainerScreen<AttunementNexusManageMenu> {
    private static final int MANAGE_BUTTON_X = 130;
    private static final int MANAGE_BUTTON_Y = 65;
    private static final int MANAGE_BUTTON_WIDTH = 18;
    private static final int MANAGE_BUTTON_HEIGHT = 12;
    private boolean buttonDown = false;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_manage.png");

    public AttunementNexusManageScreen(AttunementNexusManageMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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

//        int x = (width - imageWidth) / 2;
//        int y = (height - imageHeight) / 2;

//        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

//        renderButtons(guiGraphics, mouseX, mouseY);

//        renderCostTooltip(guiGraphics, mouseX, mouseY);
    }

//    @Override
//    public boolean mouseReleased(double mouseX, double mouseY, int button) {
//        buttonDown = false;
//        if(isHoveringAttuneButton(mouseX, mouseY)) {
//            if(this.minecraft != null && this.minecraft.gameMode != null && menu.canItemAscend()) {
//                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
//            }
//            return true;
//        }
//        return super.mouseReleased(mouseX, mouseY, button);
//    }
//
//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        if(isHoveringAttuneButton(mouseX, mouseY)) {
//            buttonDown = true;
//            return true;
//        }
//        return super.mouseClicked(mouseX, mouseY, button);
//    }
}

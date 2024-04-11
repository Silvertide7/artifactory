package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.silvertide.artifactory.Artifactory;

public class AttunementNexusScreen extends AbstractContainerScreen<AttunementNexusMenu> {
    private static final int ATTUNE_BUTTON_X = 60;
    private static final int ATTUNE_BUTTON_Y = 50;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/attunement_nexus.png");
    protected Button attuneButton;

    public AttunementNexusScreen(AttunementNexusMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        attuneButton = this.addWidget(
                Button.builder(Component.literal("Attune"), (p_169820_) -> this.startAttune()).bounds(0, 0, 45, 11).build()
        );
        // Move the label to get rid of it
        this.inventoryLabelY = 10000;
        this.inventoryLabelX = 10000;
    }

    private void startAttune() {
        this.attuneButton.setFocused(true);
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
        attuneButton.setX(leftPos + ATTUNE_BUTTON_X);
        attuneButton.setY(topPos + ATTUNE_BUTTON_Y);
        if (attuneButton.active) {
            if (isHovering(attuneButton.getX(), attuneButton.getY(), 45, 11, mouseX, mouseY)) {
                //highlighted
                guiGraphics.blit(TEXTURE, attuneButton.getX(), attuneButton.getY(), 1, 166, 45, 11);
            } else {
                //regular
                guiGraphics.blit(TEXTURE, attuneButton.getX(), attuneButton.getY(), 48, 166, 45, 11);
            }
        } else {
            //disabled
            guiGraphics.blit(TEXTURE, attuneButton.getX(), attuneButton.getY(), 95, 166, 45, 11);
        }

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.network.PacketHandler;
import net.silvertide.artifactory.network.SB_RemoveAttunedItem;

import java.util.UUID;

public class AttunementNexusConfirmationScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_manage.png");
    //BUTTON CONSTANTS
    private static final int CANCEL_BUTTON_X = 147;
    private static final int CANCEL_BUTTON_Y = 19;
    private static final int CONFIRM_BUTTON_X = 147;
    private static final int CONFIRM_BUTTON_Y = 19;
    private static final int BUTTON_WIDTH = 18;
    private static final int BUTTON_HEIGHT = 12;


    // Instance Variables
    private AttunementNexusAttuneScreen parent;
    private UUID itemToDeleteUUID;
    private boolean cancelButtonDown = false;
    private boolean confirmButtonDown = false;

    protected AttunementNexusConfirmationScreen(AttunementNexusAttuneScreen parent, UUID itemToDeleteUUID) {
        super(Component.literal(""));
        this.parent = parent;
        this.itemToDeleteUUID = itemToDeleteUUID;
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, partialTicks, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
//        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    protected void renderBackground(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.parent.width - this.parent.screenWidth) / 2;
        int y = (this.parent.height - this.parent.screenHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.parent.screenWidth, this.parent.screenHeight);

        renderButtons(guiGraphics, mouseX, mouseY);
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderCancelButton(guiGraphics, mouseX, mouseY);
        renderConfirmButton(guiGraphics, mouseX, mouseY);
    }

    private void renderCancelButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = parent.screenLeftPos + CANCEL_BUTTON_X;
        int buttonY = parent.screenTopPos + CANCEL_BUTTON_Y;

        boolean isHovering = isHoveringCancelButton(mouseX, mouseY);
        int buttonOffset = getButtonOffsetToRender(cancelButtonDown, isHovering, mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 177, buttonOffset, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private void renderConfirmButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = parent.screenLeftPos + CONFIRM_BUTTON_X;
        int buttonY = parent.screenTopPos + CONFIRM_BUTTON_Y;

        boolean isHovering = isHoveringConfirmButton(mouseX, mouseY);
        int buttonOffset = getButtonOffsetToRender(confirmButtonDown, isHovering, mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 177, buttonOffset, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private int getButtonOffsetToRender(boolean isButtonDown, boolean isHoveringButton, int mouseX, int mouseY) {
        if(isButtonDown) {
            return 58;
        }
        else if (isHoveringButton) {
            return 45;
        }
        else {
            return 32;
        }
    }

    private boolean isHoveringCancelButton(double mouseX, double mouseY) {
        return isHovering(CANCEL_BUTTON_X, CANCEL_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY);
    }

    private boolean isHoveringConfirmButton(double mouseX, double mouseY) {
        return isHovering(CONFIRM_BUTTON_X, CONFIRM_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY);
    }

    private boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.parent.screenLeftPos;
        int j = this.parent.screenTopPos;
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= (double)(pX - 1) && pMouseX < (double)(pX + pWidth + 1) && pMouseY >= (double)(pY - 1) && pMouseY < (double)(pY + pHeight + 1);
    }

    @Override
    public void setFocused(boolean pFocused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(cancelButtonDown && isHoveringCancelButton(mouseX, mouseY)) {
            this.onClose();
            return true;
        } else if(confirmButtonDown && isHoveringConfirmButton(mouseX, mouseY)){
            PacketHandler.sendToServer(new SB_RemoveAttunedItem(itemToDeleteUUID));
            this.onClose();
            return true;
        }
        cancelButtonDown = false;
        confirmButtonDown = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isHoveringCancelButton(mouseX, mouseY)) {
            cancelButtonDown = true;
            return true;
        } else if (isHoveringConfirmButton(mouseX, mouseY)){
            confirmButtonDown = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.network.PacketHandler;
import net.silvertide.artifactory.network.SB_RemoveAttunedItem;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.GUIUtil;

@OnlyIn(Dist.CLIENT)
public class DeleteConfirmationScreen extends Screen {
    private static final float TEXT_SCALE = 0.85F;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_confirmation.png");
    private static final int SCREEN_WIDTH = 146;
    private static final int SCREEN_HEIGHT = 81;
    //BUTTON CONSTANTS
    private static final int BUTTON_Y = 58;
    private static final int BUTTON_WIDTH = 50;
    private static final int BUTTON_HEIGHT = 12;

    // Instance Variables
    private final ManageAttunementsScreen manageScreen;
    private final AttunedItem itemToDelete;
    private boolean cancelButtonDown = false;
    private boolean confirmButtonDown = false;

    protected DeleteConfirmationScreen(ManageAttunementsScreen manageScreen, AttunedItem itemToDelete) {
        super(Component.literal(""));
        this.manageScreen = manageScreen;
        this.itemToDelete = itemToDelete;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, partialTicks, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    protected void renderBackground(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = getScreenStartX();
        int y = getScreenStartY();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0F, 0F, 5000F);
        guiGraphics.blit(TEXTURE, x, y, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        renderDeleteText(guiGraphics);
        renderButtons(guiGraphics, mouseX, mouseY);

        guiGraphics.pose().popPose();
    }

    private int getScreenStartX() {
        return (this.width - SCREEN_WIDTH) / 2;
    }

    private int getScreenStartY() {
        return (this.height - SCREEN_HEIGHT) / 2;
    }

    private void renderDeleteText(GuiGraphics guiGraphics) {
        Component deleteText = Component.translatable("screen.text.artifactory.confirmation.delete_text", itemToDelete.getDisplayName());
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, TEXT_SCALE, this.font, deleteText, getScreenStartX() + SCREEN_WIDTH / 2, getScreenStartY() + 18, SCREEN_WIDTH * 8 / 10, 0xFFFFFF);
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderConfirmButton(guiGraphics, mouseX, mouseY);
        renderCancelButton(guiGraphics, mouseX, mouseY);
    }

    private void renderConfirmButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = getConfirmButtonX();
        int buttonY = getButtonY();

        boolean isHovering = isHoveringConfirmButton(mouseX, mouseY);
        int buttonOffset = getButtonOffsetToRender(confirmButtonDown, isHovering, mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 147, buttonOffset, BUTTON_WIDTH, BUTTON_HEIGHT);

        Component buttonTextComp = Component.translatable("screen.button.artifactory.confirmation.confirm");
        int buttonTextX = buttonX + BUTTON_WIDTH / 2;
        int buttonTextY = buttonY + BUTTON_HEIGHT / 2;

        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, TEXT_SCALE, this.font, buttonTextComp, buttonTextX, buttonTextY, BUTTON_WIDTH, 0xFFFFFF);
    }

    private int getConfirmButtonX() {
        return getScreenStartX() + getButtonSpacing();
    }

    private void renderCancelButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = getCancelButtonX();
        int buttonY = getButtonY();

        boolean isHovering = isHoveringCancelButton(mouseX, mouseY);
        int buttonOffset = getButtonOffsetToRender(cancelButtonDown, isHovering, mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 147, buttonOffset, BUTTON_WIDTH, BUTTON_HEIGHT);

        int buttonTextX = buttonX + BUTTON_WIDTH / 2;
        int buttonTextY = buttonY + BUTTON_HEIGHT / 2;
        Component buttonTextComp = Component.translatable("screen.button.artifactory.confirmation.cancel");

        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, TEXT_SCALE, this.font, buttonTextComp, buttonTextX, buttonTextY, BUTTON_WIDTH, 0xFFFFFF);
    }

    private int getCancelButtonX() {
        return getScreenStartX() + getButtonSpacing() * 2 + BUTTON_WIDTH ;
    }

    private int getButtonSpacing() {
        return (SCREEN_WIDTH - BUTTON_WIDTH * 2) / 3;
    }

    private int getButtonY() {
        return getScreenStartY() + BUTTON_Y;
    }

    private int getButtonOffsetToRender(boolean isButtonDown, boolean isHoveringButton, int mouseX, int mouseY) {
        if(isButtonDown) {
            return 26;
        }
        else if (isHoveringButton) {
            return 13;
        }
        else {
            return 0;
        }
    }

    private boolean isHoveringCancelButton(double mouseX, double mouseY) {
        return GUIUtil.isHovering(getCancelButtonX(), getButtonY(), BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY);
    }

    private boolean isHoveringConfirmButton(double mouseX, double mouseY) {
        return GUIUtil.isHovering(getConfirmButtonX(), getButtonY(), BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.manageScreen.createAttunementCards();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(cancelButtonDown && isHoveringCancelButton(mouseX, mouseY)) {
            this.onClose();
            return true;
        } else if(confirmButtonDown && isHoveringConfirmButton(mouseX, mouseY)){
            PacketHandler.sendToServer(new SB_RemoveAttunedItem(itemToDelete.getItemUUID()));
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.utils.ClientAttunedItems;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.List;

public class AttunementNexusManageScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_manage.png");

    //CLOSE BUTTON CONSTANTS
    private static final int CLOSE_BUTTON_X = 147;
    private static final int CLOSE_BUTTON_Y = 19;
    private static final int CLOSE_BUTTON_WIDTH = 18;
    private static final int CLOSE_BUTTON_HEIGHT = 12;

    // ATTUNEMENT CARRD CONSTANTS
    private static final int ATTUNEMENT_CARD_X = 23;
    private static final int ATTUNEMENT_CARD_Y = 20;
    private static final int ATTUNEMENT_CARD_WIDTH = 104;
    private static final int ATTUNEMENT_CARD_HEIGHT = 22;

    // SLIDER CONSTANTS
    private static final int SLIDER_BASE_X = 130;
    private static final int SLIDER_BASE_Y = 20;
    private static final int SLIDER_MAX_DISTANCE_Y = 111;
    private static final int SLIDER_WIDTH = 12;
    private static final int SLIDER_HEIGHT = 15;


    // Instance Variables
    private AttunementNexusAttuneScreen parent;
    private LocalPlayer player;
    private boolean closeButtonDown = false;
    private float sliderProgress = 0.0F;

    protected AttunementNexusManageScreen(AttunementNexusAttuneScreen parent) {
        super(Component.literal(""));
        this.parent = parent;
        this.player = parent.getMinecraft().player;
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
        renderSlider(guiGraphics);
        renderAttunementCards(guiGraphics);
//        renderCostTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderCloseButton(guiGraphics, mouseX, mouseY);
    }

    private void renderCloseButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = parent.screenLeftPos + CLOSE_BUTTON_X;
        int buttonY = parent.screenTopPos + CLOSE_BUTTON_Y;

        int buttonOffset = getCloseButtonOffsetToRender(mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 177, buttonOffset, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT);
    }

    private void renderSlider(GuiGraphics guiGraphics) {
        int sliderX = parent.screenLeftPos + SLIDER_BASE_X;
        int sliderY = parent.screenTopPos + SLIDER_BASE_Y;

        guiGraphics.blit(TEXTURE, sliderX, sliderY, 177, 0, SLIDER_WIDTH, SLIDER_HEIGHT);

    }


    private void renderAttunementCards(GuiGraphics guiGraphics) {
        List<AttunedItem> attunedItems = ClientAttunedItems.getAttunedItemsAsList(this.player.getUUID());
        for(int i = 0; i < attunedItems.size(); i++) {
            AttunedItem attunedItem = attunedItems.get(i);
            renderAttunementCard(guiGraphics, attunedItem, i);
        }
    }

    private void renderAttunementCard(GuiGraphics guiGraphics, AttunedItem attunedItem, int cardIndex) {
        int attunementCardX = parent.screenLeftPos + ATTUNEMENT_CARD_X;
        int attunementCardY = parent.screenTopPos + ATTUNEMENT_CARD_Y + cardIndex * ATTUNEMENT_CARD_HEIGHT;

        guiGraphics.blit(TEXTURE, attunementCardX, attunementCardY, 0, 167, ATTUNEMENT_CARD_WIDTH, ATTUNEMENT_CARD_HEIGHT);
    }

    private int getCloseButtonOffsetToRender(int mouseX, int mouseY) {
        if(closeButtonDown) {
            return 58;
        }
        else if (isHoveringCloseButton(mouseX, mouseY)) {
            return 45;
        }
        else {
            return 32;
        }
    }
    private boolean canScroll()
    {
        return !ClientAttunedItems.getAttunedItemsAsList(this.player.getUUID()).isEmpty();
    }


    private boolean isHoveringCloseButton(double mouseX, double mouseY) {
        return isHovering(CLOSE_BUTTON_X, CLOSE_BUTTON_Y, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT, mouseX, mouseY);
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
        closeButtonDown = false;
        if(isHoveringCloseButton(mouseX, mouseY)) {
            this.onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isHoveringCloseButton(mouseX, mouseY)) {
            closeButtonDown = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

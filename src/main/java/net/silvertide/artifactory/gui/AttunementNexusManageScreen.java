package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.utils.ClientAttunedItems;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.GUIUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AttunementNexusManageScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_manage.png");

    //CLOSE BUTTON CONSTANTS
    private static final int CLOSE_BUTTON_X = 141;
    private static final int CLOSE_BUTTON_Y = 8;
    private static final int CLOSE_BUTTON_WIDTH = 12;
    private static final int CLOSE_BUTTON_HEIGHT = 12;

    // SLIDER CONSTANTS
    private static final int SLIDER_BASE_X = 141;
    private static final int SLIDER_BASE_Y = 23;
    private static final int SLIDER_MAX_DISTANCE_Y = 105;
    private static final int SLIDER_WIDTH = 12;
    private static final int SLIDER_HEIGHT = 15;


    // Instance Variables
    private AttunementNexusAttuneScreen attuneScreen;
    private LocalPlayer player;
    private boolean closeButtonDown = false;
    private boolean sliderButtonDown = false;
    private float sliderProgress = 0.0F;
    private final List<AttunementCard> attunementCards = new ArrayList<>();

    protected AttunementNexusManageScreen(AttunementNexusAttuneScreen parent) {
        super(Component.literal(""));
        this.attuneScreen = parent;
        this.player = parent.getMinecraft().player;
    }

    @Override
    protected void init() {
        super.init();
        createAttunementCards();
    }

    public void createAttunementCards() {
        attunementCards.clear();
        List<AttunedItem> attunedItems = ClientAttunedItems.getAttunedItemsAsList();
        attunedItems.sort(Comparator.comparingInt(AttunedItem::order));
        for(int i = 0; i < attunedItems.size(); i++) {
            AttunedItem attunedItem = attunedItems.get(i);
            Optional<ItemAttunementData> attunementData = DataPackUtil.getAttunementData(attunedItem.resourceLocation());
            if(attunementData.isPresent()) {
                attunementCards.add(new AttunementCard(i, attunedItems.get(i), attunementData.get(), this));
            }
        }
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

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0F, 0F, 1000F);

        renderScrollAreaBackground(guiGraphics);
        renderAttunementCards(guiGraphics, mouseX, mouseY);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0F, 0F, 5000F);

        renderScreenBackground(guiGraphics);
        renderButtons(guiGraphics, mouseX, mouseY);
        renderSlider(guiGraphics, mouseX, mouseY);

        guiGraphics.pose().popPose();
    }

    private void renderScrollAreaBackground(GuiGraphics guiGraphics) {
        int scrollAreaX = attuneScreen.getScreenLeftPos() + 23;
        int scrollAreaY = attuneScreen.getScreenTopPos() + 23;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0F, 0F ,-2000F);

        guiGraphics.blit(TEXTURE, scrollAreaX, scrollAreaY, 0, 190, 115, 60);
        guiGraphics.blit(TEXTURE, scrollAreaX, scrollAreaY + 60, 0, 190, 115, 60);
        guiGraphics.pose().popPose();
    }

    private void renderAttunementCards(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        for(AttunementCard attunementCard : attunementCards) {
            attunementCard.render(guiGraphics, mouseX, mouseY, sliderProgress, this.attunementCards.size());
        }
    }

    private void renderScreenBackground(GuiGraphics guiGraphics) {
        int x = this.attuneScreen.getScreenLeftPos();
        int y = this.attuneScreen.getScreenTopPos();

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.attuneScreen.getImageWidth(), this.attuneScreen.getImageHeight());
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderCloseButton(guiGraphics, mouseX, mouseY);
    }

    private void renderCloseButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = attuneScreen.getScreenLeftPos() + CLOSE_BUTTON_X;
        int buttonY = attuneScreen.getScreenTopPos() + CLOSE_BUTTON_Y;

        int buttonOffset = getCloseButtonOffsetToRender(mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 177, buttonOffset, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT);
    }

    private void renderSlider(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int sliderX = attuneScreen.getScreenLeftPos() + SLIDER_BASE_X;
        int sliderY = attuneScreen.getScreenTopPos() + getCurrentSliderY();

        guiGraphics.blit(TEXTURE, sliderX, sliderY, 190, getSliderOffsetToRender(mouseX, mouseY), SLIDER_WIDTH, SLIDER_HEIGHT);
    }

    private int getSliderOffsetToRender(int mouseX, int mouseY) {
        if(!this.canScroll()) {
            return 48;
        } else if(sliderButtonDown) {
            return 32;
        } else if(isHoveringSlider(mouseX, mouseY)) {
            return 16;
        } else {
            return 0;
        }
    }

    private int getCurrentSliderY() {
        if(!this.canScroll()) return SLIDER_BASE_Y;
        return SLIDER_BASE_Y + (int) (sliderProgress * SLIDER_MAX_DISTANCE_Y);
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
    private boolean canScroll() {
        return attunementCards.size() >= 6;
    }

    private boolean isHoveringCloseButton(double mouseX, double mouseY) {
        return isHovering(CLOSE_BUTTON_X, CLOSE_BUTTON_Y, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT, mouseX, mouseY);
    }

    private boolean isHoveringSlider(double mouseX, double mouseY) {
        return isHovering(SLIDER_BASE_X, getCurrentSliderY(), SLIDER_WIDTH, SLIDER_HEIGHT, mouseX, mouseY);
    }

    private boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return GUIUtil.isHovering(this.attuneScreen.getScreenLeftPos(), this.attuneScreen.getScreenTopPos(), pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(closeButtonDown && isHoveringCloseButton(mouseX, mouseY)) {
            this.onClose();
            return true;
        }  else {
            for(AttunementCard attunementCard : attunementCards) {
                attunementCard.mouseReleased(mouseX, mouseY);
            }
        }
        closeButtonDown = false;
        sliderButtonDown = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isHoveringCloseButton(mouseX, mouseY)) {
            closeButtonDown = true;
            return true;
        } else if (isHoveringSlider(mouseX, mouseY)) {
            sliderButtonDown = true;
            return true;
        } else {
            for(AttunementCard attunementCard : attunementCards) {
                attunementCard.mouseClicked(mouseX, mouseY);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (sliderButtonDown &&  this.canScroll()) {
            int i = this.attuneScreen.getScreenTopPos() + 24 + (int) (SLIDER_HEIGHT*sliderProgress);
            int j = i + SLIDER_MAX_DISTANCE_Y;
            this.sliderProgress = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.sliderProgress = Mth.clamp(this.sliderProgress, 0.0F, 1.0F);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    public AttunementNexusAttuneScreen getAttuneScreen() {
        return attuneScreen;
    }

    private class AttunementCard{
        private static final int CARD_WINDOW_HEIGHT = 120;
        private static final int ATTUNEMENT_CARD_X = 23;
        private static final int ATTUNEMENT_CARD_Y = 23;
        private static final int ATTUNEMENT_CARD_WIDTH = 115;
        private static final int ATTUNEMENT_CARD_HEIGHT = 22;
        private final int DELETE_BUTTON_X= 101;
        private final int DELETE_BUTTON_Y = 11;
        private final int DELETE_BUTTON_WIDTH = 12;
        private final int DELETE_BUTTON_HEIGHT = 9;
        private final int INFORMATION_ICON_X = 25;
        private final int INFORMATION_ICON_Y = 10;
        private final int INFORMATION_ICON_WIDTH = 10;
        private final int INFORMATION_ICON_HEIGHT = 10;

        private int index;
        private final AttunedItem attunedItem;
        private final ItemAttunementData attunementData;
        private final ItemStack itemToRender;
        private List<String> modificationDescPerLevel;
        private boolean isDeleteButtonDown = false;
        private boolean isDeleteButtonDisabled = false;
        private float distanceScrolledY = 0;
        private boolean isOffScreen = false;
        AttunementNexusManageScreen manageScreen;

        private AttunementCard(int index, AttunedItem attunedItem, ItemAttunementData attunementData, AttunementNexusManageScreen manageScreen) {
            this.index = index;
            this.attunedItem = attunedItem;
            this.attunementData = attunementData;
            this.manageScreen = manageScreen;
            this.modificationDescPerLevel = ClientAttunedItems.getModifications(this.attunedItem.resourceLocation());
            this.itemToRender = ResourceLocationUtil.getItemStackFromResourceLocation(attunedItem.resourceLocation());
        }

        public void render(GuiGraphics guiGraphics, double mouseX, double mouseY, float sliderProgress, int numCards) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0F, 0F, 2000F);

            // If 6 or more cards then the window will be scrollable / have overlap.
            if(numCards >= 6) {
                int maxDistanceScrollableY = numCards * ATTUNEMENT_CARD_HEIGHT - CARD_WINDOW_HEIGHT;
                this.distanceScrolledY = maxDistanceScrollableY*sliderProgress;
                this.isOffScreen = isOffScreenAboveWindow() || isOffscreenBelowWindow();

                // Check if the card is above or below the scroll area entirely (offscreen) and don't render if so.
                if (this.isOffScreen) {
                    guiGraphics.pose().popPose();
                    return;
                }

                guiGraphics.pose().translate(0, -this.distanceScrolledY, 0F);
            } else {
                this.distanceScrolledY = 0;
                this.isOffScreen = false;
            }

            renderBackground(guiGraphics);
            renderItemImage(guiGraphics);
            renderDisplayName(guiGraphics);
            renderAttunementLevel(guiGraphics);
            renderSlotsUsed(guiGraphics);
            renderInformationIcon(guiGraphics, mouseX, mouseY);
            renderInformationTooltip(guiGraphics, mouseX, mouseY);
            renderDeleteButton(guiGraphics, mouseX, mouseY);

            guiGraphics.pose().popPose();
        }

        public int getTopOfCardY() {
            return this.index * ATTUNEMENT_CARD_HEIGHT;
        }

        public int getBottomOfCardY() {
            return getTopOfCardY() + ATTUNEMENT_CARD_HEIGHT;
        }

        public boolean isOffScreenAboveWindow() {
            return this.distanceScrolledY >= getBottomOfCardY();
        }

        public boolean isOffscreenBelowWindow() {
            return getTopOfCardY() > CARD_WINDOW_HEIGHT + this.distanceScrolledY;
        }

        private void renderBackground(GuiGraphics guiGraphics) {
            guiGraphics.blit(TEXTURE, this.getAttunementCardX(), this.getAttunementCardY(), 0, 167, ATTUNEMENT_CARD_WIDTH, ATTUNEMENT_CARD_HEIGHT);
        }
        public void renderItemImage(GuiGraphics guiGraphics) {
            guiGraphics.renderItem(this.itemToRender, this.getAttunementCardX() + 3, this.getAttunementCardY() + 3);
        }

        private void renderDisplayName(GuiGraphics guiGraphics) {
            Component displayName = Component.literal(GUIUtil.prettifyName(this.attunedItem.displayName()));
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.57F, manageScreen.font, displayName, getAttunementCardX() + 28, getAttunementCardY() + 3, ATTUNEMENT_CARD_WIDTH * 7 / 10, 0xC1EFEF);
        }

        private void renderInformationIcon(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            guiGraphics.blit(TEXTURE, this.getAttunementCardX() + INFORMATION_ICON_X, this.getAttunementCardY() + INFORMATION_ICON_Y, 177, getInformationIconOffsetToRender(mouseX, mouseY), INFORMATION_ICON_WIDTH, INFORMATION_ICON_HEIGHT);
        }


        private void renderInformationTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            if(isHoveringInformationIcon(mouseX, mouseY)) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0F, 0F, 10000F);

                List<Component> list = Lists.newArrayList();
                for(String levelDescription : this.modificationDescPerLevel) {
                    list.add(Component.literal(levelDescription));
                }
                guiGraphics.renderComponentTooltip(this.manageScreen.font, list, (int) mouseX, (int) mouseY);

                guiGraphics.pose().popPose();
            }
        }

        private void renderAttunementLevel(GuiGraphics guiGraphics) {
            Component slotsUsed = Component.translatable("screen.text.artifactory.manage.attunement_level", this.attunedItem.attunementLevel());
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.48F, manageScreen.font, slotsUsed, getAttunementCardX() + 40, getAttunementCardY() + 11, ATTUNEMENT_CARD_WIDTH * 7 / 10, 0x949094);
        }

        private void renderSlotsUsed(GuiGraphics guiGraphics) {
            Component slotsUsed = Component.translatable("screen.text.artifactory.manage.slots_used", this.attunementData.getAttunementSlotsUsed());
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.48F, manageScreen.font, slotsUsed, getAttunementCardX() + 40, getAttunementCardY() + 16, ATTUNEMENT_CARD_WIDTH * 7 / 10, 0x949094);
        }

        private void renderDeleteButton(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            if(isHovering(0, 0, ATTUNEMENT_CARD_WIDTH, ATTUNEMENT_CARD_HEIGHT, mouseX, mouseY)){
                guiGraphics.blit(TEXTURE, this.getAttunementCardX() + DELETE_BUTTON_X, this.getAttunementCardY() + DELETE_BUTTON_Y, 177, getDeleteButtonOffsetToRender(mouseX, mouseY), DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT);
            }
        }

        private int getDeleteButtonOffsetToRender(double mouseX, double mouseY) {
            if(isDeleteButtonDisabled) {
                return 101;
            } else if(isDeleteButtonDown) {
                return 91;
            } else if(isHoveringDeleteButton(mouseX, mouseY)) {
                return 81;
            } else {
                return 71;
            }
        }

        private int getInformationIconOffsetToRender(double mouseX, double mouseY) {
            if(isHoveringInformationIcon(mouseX, mouseY)) {
                return 122;
            } else {
                return 111;
            }
        }

        private boolean isHoveringInformationIcon(double mouseX, double mouseY) {
            return isHovering(INFORMATION_ICON_X, INFORMATION_ICON_Y, INFORMATION_ICON_WIDTH, INFORMATION_ICON_HEIGHT, mouseX, mouseY);
        }

        public boolean isHoveringDeleteButton(double mouseX, double mouseY) {
            return isHovering(DELETE_BUTTON_X, DELETE_BUTTON_Y, DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT, mouseX, mouseY);
        }

        private boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
            return GUIUtil.isHovering(getAttunementCardX(), getAttunementCardY() - (int) this.distanceScrolledY, pX, pY, pWidth, pHeight, pMouseX, pMouseY);
        }

        public int getAttunementCardX() {
            return attuneScreen.getScreenLeftPos() + ATTUNEMENT_CARD_X;
        }

        public int getAttunementCardY() {
            return attuneScreen.getScreenTopPos() + ATTUNEMENT_CARD_Y + index * ATTUNEMENT_CARD_HEIGHT;
        }

        public void mouseReleased(double mouseX, double mouseY) {
            if(isDeleteButtonDown && isHoveringDeleteButton(mouseX, mouseY)) {
                handleDeleteButtonPress();
            }
            this.isDeleteButtonDown = false;
        }

        private void handleDeleteButtonPress() {
            Minecraft minecraft = this.manageScreen.getAttuneScreen().getMinecraft();
            if(minecraft.gameMode != null) {
                minecraft.pushGuiLayer(new AttunementNexusConfirmationScreen(this.manageScreen, attunedItem));
            }
        }

        public void mouseClicked(double mouseX, double mouseY) {
            if(isHoveringDeleteButton(mouseX, mouseY)) {
                this.isDeleteButtonDown = true;
            }
        }
    }
}

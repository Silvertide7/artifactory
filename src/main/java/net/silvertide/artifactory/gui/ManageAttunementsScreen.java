package net.silvertide.artifactory.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunedItems;
import net.silvertide.artifactory.client.state.ClientItemAttunementData;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.GUIUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ManageAttunementsScreen extends Screen {
    private static final int BUTTON_TEXT_COLOR = 0xFFFFFF;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_manage.png");
    private static final int SCREEN_WIDTH = 176;
    private static final int SCREEN_HEIGHT = 166;

    //CLOSE BUTTON CONSTANTS
    private static final int CLOSE_BUTTON_X = 156;
    private static final int CLOSE_BUTTON_Y = 8;
    private static final int CLOSE_BUTTON_WIDTH = 12;
    private static final int CLOSE_BUTTON_HEIGHT = 12;

    // SLIDER CONSTANTS
    private static final int SLIDER_BASE_X = 141;
    private static final int SLIDER_BASE_Y = 23;
    private static final int SLIDER_MAX_DISTANCE_Y = 105;
    private static final int SLIDER_WIDTH = 12;
    private static final int SLIDER_HEIGHT = 15;

    // SLOT INFO CONSTANTS
    private static final int SLOT_INFO_X = 6;
    private static final int SLOT_INFO_Y = 22;
    private static final int UNIQUE_INFO_X = 6;
    private static final int UNIQUE_INFO_Y = 47;
    private static final int SLOT_INFO_WIDTH = 15;
    private static final int SLOT_INFO_HEIGHT = 23;

    // Instance Variables
    private boolean closeButtonDown = false;
    private boolean sliderButtonDown = false;
    private float sliderProgress = 0.0F;
    private int numSlotsUsed = 0;
    private final int numUniqueAttunementsAllowed;
    private int numUniqueAttunements;
    private final List<AttunementCard> attunementCards = new ArrayList<>();
    private final LocalPlayer player;

    public ManageAttunementsScreen(int numUniqueAttunementsAllowed) {
        super(Component.literal(""));
        this.player = Minecraft.getInstance().player;
        this.numUniqueAttunementsAllowed = numUniqueAttunementsAllowed;
    }

    @Override
    protected void init() {
        super.init();
        createAttunementCards();
    }

    // Need to allow no attunement data
    public void createAttunementCards() {
        numSlotsUsed = 0;
        numUniqueAttunements = 0;
        attunementCards.clear();

        List<AttunedItem> attunedItems = ClientAttunedItems.getAttunedItemsAsList();
        attunedItems.sort(Comparator.comparingInt(AttunedItem::getOrder));
        for(int i = 0; i < attunedItems.size(); i++) {
            AttunedItem attunedItem = attunedItems.get(i);
            Optional<AttunementDataSource> attunementData = ClientItemAttunementData.getSyncedAttunementDataSource(attunedItem.getResourceLocation());

            attunementCards.add(new AttunementCard(i, attunedItems.get(i), attunementData.orElse(null), this));

            if(attunementData.isPresent() && attunementData.get().unique()) numUniqueAttunements++;
            numSlotsUsed += attunementData.map(AttunementDataSource::attunementSlotsUsed).orElse(0);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        try {
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        } catch (Exception ignore) {
            onClose();
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0F, 0F, 200F);

        renderScrollAreaBackground(guiGraphics);
        renderAttunementCards(guiGraphics, mouseX, mouseY);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0F, 0F, 500F);

        renderScreenBackground(guiGraphics);
        renderTitle(guiGraphics);
        renderButtons(guiGraphics, mouseX, mouseY);
        renderSlider(guiGraphics, mouseX, mouseY);
        renderSlotInformation(guiGraphics, mouseX, mouseY);
        renderUniqueInformation(guiGraphics, mouseX, mouseY);

        guiGraphics.pose().popPose();
    }

    private void renderTitle(GuiGraphics guiGraphics) {
        //TODO: Make this translatable
        Component buttonTextComp = Component.literal("Manage Attunements");
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.85F, this.font, buttonTextComp, this.getScreenLeftPos() + SCREEN_WIDTH / 2, this.getScreenTopPos() + 13, 100, BUTTON_TEXT_COLOR);
    }

    private void renderScrollAreaBackground(GuiGraphics guiGraphics) {
        int scrollAreaX = this.getScreenLeftPos() + 23;
        int scrollAreaY = this.getScreenTopPos() + 23;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0F, 0F ,-200F);

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
        int x = this.getScreenLeftPos();
        int y = this.getScreenTopPos();

        guiGraphics.blit(TEXTURE, x, y, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderCloseButton(guiGraphics, mouseX, mouseY);
    }

    private void renderCloseButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = this.getScreenLeftPos() + CLOSE_BUTTON_X;
        int buttonY = this.getScreenTopPos() + CLOSE_BUTTON_Y;

        int buttonOffset = getCloseButtonOffsetToRender(mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 177, buttonOffset, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT);
    }

    private void renderSlider(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int sliderX = this.getScreenLeftPos() + SLIDER_BASE_X;
        int sliderY = this.getScreenTopPos() + getCurrentSliderY();

        guiGraphics.blit(TEXTURE, sliderX, sliderY, 190, getSliderOffsetToRender(mouseX, mouseY), SLIDER_WIDTH, SLIDER_HEIGHT);
    }

    private void renderSlotInformation(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int infoPanelX = getScreenLeftPos() + SLOT_INFO_X;
        int infoPanelY = getScreenTopPos() + SLOT_INFO_Y;

        guiGraphics.blit(TEXTURE, infoPanelX, infoPanelY, 190, 64, SLOT_INFO_WIDTH, SLOT_INFO_HEIGHT);
        guiGraphics.blit(TEXTURE, infoPanelX + 2, infoPanelY + 11, 190, 88, 11, 1);

        Component numerator = Component.literal(String.valueOf(numSlotsUsed));
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.5F, this.font, numerator, infoPanelX + 8, infoPanelY + 7, 40, 0xD6FFFE);

        Component denominator = Component.literal(String.valueOf(AttunementUtil.getMaxAttunementSlots(this.player)));
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.5F, this.font, denominator, infoPanelX + 8, infoPanelY + 15, 40, 0xD6FFFE);

        if(isHovering(SLOT_INFO_X, SLOT_INFO_Y, SLOT_INFO_WIDTH, SLOT_INFO_HEIGHT, mouseX, mouseY)) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0F, 0F, 500F);

            guiGraphics.renderComponentTooltip(this.font, List.of(Component.translatable("screen.text.artifactory.manage.slots")), mouseX, mouseY);
            guiGraphics.pose().popPose();
        }
    }

    private void renderUniqueInformation(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(numUniqueAttunementsAllowed > 0) {
            int infoPanelX = getScreenLeftPos() + UNIQUE_INFO_X;
            int infoPanelY = getScreenTopPos() + UNIQUE_INFO_Y;

            guiGraphics.blit(TEXTURE, infoPanelX, infoPanelY, 190, 64, SLOT_INFO_WIDTH, SLOT_INFO_HEIGHT);
            guiGraphics.blit(TEXTURE, infoPanelX + 2, infoPanelY + 11, 190, 90, 11, 1);

            Component numerator = Component.literal(String.valueOf(numUniqueAttunements));
            GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.5F, this.font, numerator, infoPanelX + 8, infoPanelY + 7, 40, 0xFFAA00);

            Component denominator = Component.literal(String.valueOf(numUniqueAttunementsAllowed));
            GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.5F, this.font, denominator, infoPanelX + 8, infoPanelY + 15, 40, 0xFFAA00);

            if(isHovering(UNIQUE_INFO_X, UNIQUE_INFO_Y, SLOT_INFO_WIDTH, SLOT_INFO_HEIGHT, mouseX, mouseY)) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0F, 0F, 500F);

                guiGraphics.renderComponentTooltip(this.font, List.of(Component.translatable("screen.text.artifactory.manage.unique_attunements")), mouseX, mouseY);
                guiGraphics.pose().popPose();
            }
        }
    }

    private int getScreenLeftPos() {
        return (this.width - SCREEN_WIDTH) / 2;
    }

    private int getScreenTopPos() {
        return (this.height - SCREEN_HEIGHT) / 2;
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
        return GUIUtil.isHovering(this.getScreenLeftPos(), this.getScreenTopPos(), pX, pY, pWidth, pHeight, pMouseX, pMouseY);
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
            int i = this.getScreenTopPos() + 24 + (int) (SLIDER_HEIGHT*sliderProgress);
            int j = i + SLIDER_MAX_DISTANCE_Y;
            this.sliderProgress = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.sliderProgress = Mth.clamp(this.sliderProgress, 0.0F, 1.0F);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class AttunementCard {
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
        private final AttunementDataSource attunementData;
        private final ItemStack itemToRender;
        private final List<String> modificationDescPerLevel;
        private boolean isDeleteButtonDown = false;
        private float distanceScrolledY = 0;
        private boolean isOffScreen = false;
        ManageAttunementsScreen manageScreen;

        private AttunementCard(int index, AttunedItem attunedItem, AttunementDataSource attunementData, ManageAttunementsScreen manageScreen) {
            this.index = index;
            this.attunedItem = attunedItem;
            this.attunementData = attunementData;
            this.manageScreen = manageScreen;
            this.modificationDescPerLevel = ClientAttunedItems.getModifications(this.attunedItem.getResourceLocation());
            this.itemToRender = ResourceLocationUtil.getItemStackFromResourceLocation(this.attunedItem.getResourceLocation());
        }

        public void render(GuiGraphics guiGraphics, double mouseX, double mouseY, float sliderProgress, int numCards) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0F, 0F, 100F);

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
            renderDisplayName(guiGraphics, (int) mouseX, (int) mouseY);

            if(attunementData != null) {
                renderAttunementLevel(guiGraphics);
                renderSlotsUsed(guiGraphics);
                renderInformationIcon(guiGraphics, mouseX, mouseY);
                renderInformationTooltip(guiGraphics, mouseX, mouseY);
            } else {
                renderNoAttunementDataWarning(guiGraphics);
            }

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
            renderItemBorder(guiGraphics);
            guiGraphics.renderItem(this.itemToRender, this.getAttunementCardX() + 3, this.getAttunementCardY() + 3);
        }

        private void renderItemBorder(GuiGraphics guiGraphics) {
            guiGraphics.blit(TEXTURE, this.getAttunementCardX() + 2, this.getAttunementCardY() + 2, 116, getItemBorderOffset(), 18, 18);
        }

        private int getItemBorderOffset() {
            if(attunementData != null) {
                double progress = (double) attunedItem.getAttunementLevel() / attunementData.attunementLevels().size();
                if(progress >= 0.0 && progress < 0.33) {
                    return 169;
                } else if (progress >= 0.33 && progress < 0.65) {
                    return 188;
                } else if (progress >= 0.66 && progress < 1.00) {
                    return 207;
                } else if (attunedItem.getAttunementLevel() == attunementData.attunementLevels().size()) {
                    return 226;
                } else {
                    return 169;
                }
            } else {
                return 169;
            }
        }

        private void renderDisplayName(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            int textOffsetX = 28;
            int textOffsetY = 3;
            float textScale = 0.65F;

            String trimmedText = GUIUtil.trimTextToWidth(this.attunedItem.getDisplayName(), manageScreen.font, 135);
            GUIUtil.drawScaledString(guiGraphics, textScale, manageScreen.font, trimmedText, getAttunementCardX() + textOffsetX, getAttunementCardY() + textOffsetY, 0xC1EFEF);

            if(!trimmedText.equals(this.attunedItem.getDisplayName()) && isHovering(textOffsetX, textOffsetY, (int) (manageScreen.font.width(trimmedText) * textScale), (int) (manageScreen.font.lineHeight * textScale), mouseX, mouseY)) {
                guiGraphics.renderComponentTooltip(manageScreen.font, List.of(Component.literal(this.attunedItem.getDisplayName())), getAttunementCardX() + textOffsetX, getAttunementCardY() + textOffsetY);
            }
        }

        private void renderInformationIcon(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            guiGraphics.blit(TEXTURE, this.getAttunementCardX() + INFORMATION_ICON_X, this.getAttunementCardY() + INFORMATION_ICON_Y, 177, getInformationIconOffsetToRender(mouseX, mouseY), INFORMATION_ICON_WIDTH, INFORMATION_ICON_HEIGHT);
        }

        private void renderInformationTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            if(isHoveringInformationIcon(mouseX, mouseY)) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0F, 0F, 700F);

                List<Component> list = Lists.newArrayList();
                for(int i = 0; i < this.modificationDescPerLevel.size(); i++) {
                    MutableComponent modDesc = Component.literal(this.modificationDescPerLevel.get(i));
                    if(i < this.attunedItem.getAttunementLevel()) {
                        modDesc.withStyle(ChatFormatting.GREEN);
                    } else {
                        modDesc.withStyle(ChatFormatting.GRAY);
                    }
                    list.add(modDesc);
                }
                guiGraphics.renderComponentTooltip(this.manageScreen.font, list, (int) mouseX, (int) mouseY);
                guiGraphics.pose().popPose();
            }
        }

        private void renderAttunementLevel(GuiGraphics guiGraphics) {
            MutableComponent attunementLevel = Component.translatable("screen.text.artifactory.manage.attunement_level", this.attunedItem.getAttunementLevel());

            if(this.attunedItem.getAttunementLevel() == attunementData.attunementLevels().size()) {
                attunementLevel.append(Component.translatable("screen.text.artifactory.manage.attunement_level_max"));
            }
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.48F, manageScreen.font, attunementLevel, getAttunementCardX() + 40, getAttunementCardY() + 11, ATTUNEMENT_CARD_WIDTH * 7 / 10, 0xE1E1E1);
        }

        private void renderSlotsUsed(GuiGraphics guiGraphics) {
            MutableComponent slotsUsed = Component.translatable("screen.text.artifactory.manage.slots_used", this.attunementData.getAttunementSlotsUsed());
                if(attunementData.unique()) {
                    slotsUsed.append(Component.translatable("screen.text.artifactory.manage.slots_used_unique"));
                }
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.48F, manageScreen.font, slotsUsed, getAttunementCardX() + 40, getAttunementCardY() + 16, ATTUNEMENT_CARD_WIDTH * 7 / 10, 0xE1E1E1);
        }

        private void renderNoAttunementDataWarning(GuiGraphics guiGraphics) {
            MutableComponent noAttunementData = Component.translatable("screen.text.artifactory.manage.attunement_data_not_found");
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.48F, manageScreen.font, noAttunementData, getAttunementCardX() + 28, getAttunementCardY() + 11, ATTUNEMENT_CARD_WIDTH * 7 / 10, 0xAD2626);

        }

        private void renderDeleteButton(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            if(isHovering(0, 0, ATTUNEMENT_CARD_WIDTH, ATTUNEMENT_CARD_HEIGHT, mouseX, mouseY)){
                guiGraphics.blit(TEXTURE, this.getAttunementCardX() + DELETE_BUTTON_X, this.getAttunementCardY() + DELETE_BUTTON_Y, 177, getDeleteButtonOffsetToRender(mouseX, mouseY), DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT);
            }
        }

        private int getDeleteButtonOffsetToRender(double mouseX, double mouseY) {
            if(isDeleteButtonDown) {
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
            return manageScreen.getScreenLeftPos() + ATTUNEMENT_CARD_X;
        }

        public int getAttunementCardY() {
            return manageScreen.getScreenTopPos() + ATTUNEMENT_CARD_Y + index * ATTUNEMENT_CARD_HEIGHT;
        }

        public void mouseReleased(double mouseX, double mouseY) {
            if(isDeleteButtonDown && isHoveringDeleteButton(mouseX, mouseY)) {
                handleDeleteButtonPress();
            }
            this.isDeleteButtonDown = false;
        }

        private void handleDeleteButtonPress() {
            Minecraft minecraft = Minecraft.getInstance();
            if(minecraft.gameMode != null) {
                minecraft.pushGuiLayer(new DeleteConfirmationScreen(this.manageScreen, attunedItem));
            }
        }

        public void mouseClicked(double mouseX, double mouseY) {
            if(isHoveringDeleteButton(mouseX, mouseY)) {
                this.isDeleteButtonDown = true;
            }
        }
    }
}

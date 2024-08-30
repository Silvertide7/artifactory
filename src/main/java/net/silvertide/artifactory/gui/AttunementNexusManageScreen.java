package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.utils.ClientAttunedItems;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.GUIUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AttunementNexusManageScreen extends Screen {
    private static final float TEXT_SCALE = 0.85F;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_manage.png");

    //CLOSE BUTTON CONSTANTS
    private static final int CLOSE_BUTTON_X = 147;
    private static final int CLOSE_BUTTON_Y = 19;
    private static final int CLOSE_BUTTON_WIDTH = 18;
    private static final int CLOSE_BUTTON_HEIGHT = 12;

    // SLIDER CONSTANTS
    private static final int SLIDER_BASE_X = 130;
    private static final int SLIDER_BASE_Y = 20;
    private static final int SLIDER_MAX_DISTANCE_Y = 111;
    private static final int SLIDER_WIDTH = 12;
    private static final int SLIDER_HEIGHT = 15;


    // Instance Variables
    private AttunementNexusAttuneScreen attuneScreen;
    private LocalPlayer player;
    private boolean closeButtonDown = false;
    private float sliderProgress = 0.0F;
    private List<AttunementCard> attunementCards = new ArrayList<>();

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
        List<AttunedItem> attunedItems = ClientAttunedItems.getAttunedItemsAsList(this.player.getUUID());
        attunedItems.sort(Comparator.comparingInt(AttunedItem::order));
        for(int i = 0; i < attunedItems.size(); i++) {
            AttunedItem attunedItem = attunedItems.get(i);
            Optional<ItemAttunementData> attunementData = DataPackUtil.getAttunementData(ResourceLocationUtil.getResourceLocation(attunedItem.resourceLocation()));
            if(attunementData.isPresent()) {
                attunementCards.add(new AttunementCard(i, attunedItems.get(i), attunementData.get(), this));
            }
        }
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

        int x = this.attuneScreen.getScreenLeftPos();
        int y = this.attuneScreen.getScreenTopPos();

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.attuneScreen.getImageWidth(), this.attuneScreen.getImageHeight());

        renderButtons(guiGraphics, mouseX, mouseY);
        renderSlider(guiGraphics);
        renderAttunementCards(guiGraphics, mouseX, mouseY);
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

    private void renderSlider(GuiGraphics guiGraphics) {
        int sliderX = attuneScreen.getScreenLeftPos() + SLIDER_BASE_X;
        int sliderY = attuneScreen.getScreenTopPos() + SLIDER_BASE_Y;

        guiGraphics.blit(TEXTURE, sliderX, sliderY, 177, 0, SLIDER_WIDTH, SLIDER_HEIGHT);
    }


    private void renderAttunementCards(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        for(AttunementCard attunementCard : attunementCards) {
            attunementCard.render(guiGraphics, mouseX, mouseY);
        }
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
        int i = this.attuneScreen.getScreenLeftPos();
        int j = this.attuneScreen.getScreenTopPos();
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= (double)(pX - 1) && pMouseX < (double)(pX + pWidth + 1) && pMouseY >= (double)(pY - 1) && pMouseY < (double)(pY + pHeight + 1);
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
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isHoveringCloseButton(mouseX, mouseY)) {
            closeButtonDown = true;
            return true;
        } else {
            for(AttunementCard attunementCard : attunementCards) {
                attunementCard.mouseClicked(mouseX, mouseY);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public AttunementNexusAttuneScreen getAttuneScreen() {
        return attuneScreen;
    }

    private class AttunementCard {
        private static final int ATTUNEMENT_CARD_X = 23;
        private static final int ATTUNEMENT_CARD_Y = 20;
        private static final int ATTUNEMENT_CARD_WIDTH = 104;
        private static final int ATTUNEMENT_CARD_HEIGHT = 22;
        private final int DELETE_BUTTON_X= 86;
        private final int DELETE_BUTTON_Y = 11;
        private final int DELETE_BUTTON_WIDTH = 12;
        private final int DELETE_BUTTON_HEIGHT = 9;

        private int index;
        private final AttunedItem attunedItem;
        private final ItemAttunementData attunementData;
        private final ItemStack itemToRender;
        private int deltaY;
        private boolean isDeleteButtonDown = false;
        private boolean isDeleteButtonDisabled = false;
        private int slotsUsed = 1;
        AttunementNexusManageScreen manageScreen;

        public AttunementCard(int index, AttunedItem attunedItem, ItemAttunementData attunementData, AttunementNexusManageScreen manageScreen) {
            this.index = index;
            this.attunedItem = attunedItem;
            this.attunementData = attunementData;
            this.manageScreen = manageScreen;
            deltaY = 0;

            Item baseItem = ResourceLocationUtil.getItemFromResourceLocation(attunedItem.resourceLocation());
            this.itemToRender = new ItemStack(baseItem);

        }

        public void render(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            renderBackground(guiGraphics);
            renderItemImage(guiGraphics);
            renderDisplayName(guiGraphics);
            renderSlotsUsed(guiGraphics);
            renderAttunementLevel(guiGraphics);
            renderDeleteButton(guiGraphics, mouseX, mouseY);
        }


        private void renderBackground(GuiGraphics guiGraphics) {
            guiGraphics.blit(TEXTURE, this.getAttunementCardX(), this.getAttunementCardY(), 0, 167, ATTUNEMENT_CARD_WIDTH, ATTUNEMENT_CARD_HEIGHT);
        }
        public void renderItemImage(GuiGraphics guiGraphics) {
            // TODO This is a black screen. Not sure how to get the items texture yet
            guiGraphics.renderItem(this.itemToRender, this.getAttunementCardX() + 3, this.getAttunementCardY() + 3);
        }

        private void renderDisplayName(GuiGraphics guiGraphics) {
            Component displayName = Component.literal(GUIUtil.prettifyName(this.attunedItem.displayName()));
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.57F, manageScreen.font, displayName, getAttunementCardX() + 28, getAttunementCardY() + 3, ATTUNEMENT_CARD_WIDTH * 7 / 10, 0xC1EFEF);
        }

        private void renderSlotsUsed(GuiGraphics guiGraphics) {
            Component slotsUsed = Component.translatable("screen.text.artifactory.manage.slots_used", this.attunementData.getAttunementSlotsUsed());
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.48F, manageScreen.font, slotsUsed, getAttunementCardX() + 29, getAttunementCardY() + 11, ATTUNEMENT_CARD_WIDTH * 7 / 10, 0x949094);
        }

        private void renderAttunementLevel(GuiGraphics guiGraphics) {
            Component slotsUsed = Component.translatable("screen.text.artifactory.manage.attunement_level", this.attunedItem.attunementLevel());
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.48F, manageScreen.font, slotsUsed, getAttunementCardX() + 29, getAttunementCardY() + 16, ATTUNEMENT_CARD_WIDTH * 7 / 10, 0x949094);
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

        public boolean isHoveringDeleteButton(double mouseX, double mouseY) {
            return isHovering(DELETE_BUTTON_X, DELETE_BUTTON_Y, DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT, mouseX, mouseY);
        }

        private boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
            int i = getAttunementCardX();
            int j = getAttunementCardY();
            pMouseX -= i;
            pMouseY -= j;
            return pMouseX >= (double)(pX - 1) && pMouseX < (double)(pX + pWidth + 1) && pMouseY >= (double)(pY - 1) && pMouseY < (double)(pY + pHeight + 1);
        }

        public int getAttunementCardX() {
            return attuneScreen.getScreenLeftPos() + ATTUNEMENT_CARD_X;
        }

        public int getAttunementCardY() {
            return attuneScreen.getScreenTopPos() + ATTUNEMENT_CARD_Y + index * ATTUNEMENT_CARD_HEIGHT + deltaY;
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

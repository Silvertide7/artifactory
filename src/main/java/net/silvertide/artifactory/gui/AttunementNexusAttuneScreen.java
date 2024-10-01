package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.utils.ClientAttunementNexusSlotInformation;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;
import net.silvertide.artifactory.util.GUIUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class AttunementNexusAttuneScreen extends AbstractContainerScreen<AttunementNexusAttuneMenu> implements ClientAttunementNexusSlotInformation.ClientSlotInformationListener {
    private static float TEXT_SCALE = 0.85F;
    private static int BUTTON_TEXT_COLOR = 0xFFFFFF;
    private static final int ATTUNE_BUTTON_X = 61;
    private static final int ATTUNE_BUTTON_Y = 65;
    private static final int ATTUNE_BUTTON_WIDTH = 54;
    private static final int ATTUNE_BUTTON_HEIGHT = 12;
    private static final int MANAGE_BUTTON_X = 141;
    private static final int MANAGE_BUTTON_Y = 8;
    private static final int MANAGE_BUTTON_WIDTH = 12;
    private static final int MANAGE_BUTTON_HEIGHT = 12;
    private boolean attuneButtonDown = false;
    private boolean manageButtonDown = false;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_attune.png");
    private ItemRequirementSlotRenderer itemRequirementSlotOneRenderer = null;
    private ItemRequirementSlotRenderer itemRequirementSlotTwoRenderer = null;
    private ItemRequirementSlotRenderer itemRequirementSlotThreeRenderer = null;

    public AttunementNexusAttuneScreen(AttunementNexusAttuneMenu pMenu, Inventory playerInventory, Component pTitle) {
        super(pMenu, playerInventory, pTitle);
        ClientAttunementNexusSlotInformation.registerListener(this);
    }

    @Override
    protected void init() {
        super.init();
        // Move the label to get rid of it
        this.titleLabelX = 10000;
        this.titleLabelY = 10000;
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

        int backgroundX = getBackgroundX();
        int backgroundY = getBackgroundY();

        guiGraphics.blit(TEXTURE, backgroundX, backgroundY, 0, 0, imageWidth, imageHeight);

        renderTitle(guiGraphics, backgroundX, backgroundY);
        renderItemRequirementSlots(guiGraphics, mouseX, mouseY);
        renderButtons(guiGraphics, mouseX, mouseY);
        renderTooltips(guiGraphics, mouseX, mouseY);
        renderProgressGraphic(guiGraphics, backgroundX, backgroundY);
        renderAttunementInformation(guiGraphics, backgroundX, backgroundY);
    }

    private void renderTitle(GuiGraphics guiGraphics, int x, int y) {
        Component buttonTextComp = Component.literal("Attune Gear");
        guiGraphics.drawWordWrap(this.font, buttonTextComp, x - this.font.width(buttonTextComp)/2 + this.imageWidth / 2, y - this.font.lineHeight/2 + 13, 100, BUTTON_TEXT_COLOR);
    }


    private void renderItemRequirementSlots(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(this.itemRequirementSlotOneRenderer != null && menu.getItemRequirementOneState() != ItemRequirementState.NOT_REQUIRED.getValue()) {
            this.itemRequirementSlotOneRenderer.render(guiGraphics, mouseX, mouseY);
        }
        if(this.itemRequirementSlotTwoRenderer != null && menu.getItemRequirementOneState() != ItemRequirementState.NOT_REQUIRED.getValue()) {
            this.itemRequirementSlotTwoRenderer.render(guiGraphics, mouseX, mouseY);
        }
        if(this.itemRequirementSlotThreeRenderer != null && menu.getItemRequirementOneState() != ItemRequirementState.NOT_REQUIRED.getValue()) {
            this.itemRequirementSlotThreeRenderer.render(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderAttuneButton(guiGraphics, mouseX, mouseY);
        renderManageButton(guiGraphics, mouseX, mouseY);
    }

    private void renderAttuneButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = leftPos + ATTUNE_BUTTON_X;
        int buttonY = topPos + ATTUNE_BUTTON_Y;

        int buttonOffset = getAttuneButtonOffsetToRender(mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 177, buttonOffset, ATTUNE_BUTTON_WIDTH, ATTUNE_BUTTON_HEIGHT);

        Component buttonTextComp = getAttuneButtonText();
        int buttonTextX = buttonX + ATTUNE_BUTTON_WIDTH / 2;
        int buttonTextY = buttonY + ATTUNE_BUTTON_HEIGHT / 2;

        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, TEXT_SCALE, this.font, buttonTextComp, buttonTextX, buttonTextY, ATTUNE_BUTTON_WIDTH, BUTTON_TEXT_COLOR);
    }

    private void renderManageButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int buttonX = leftPos + MANAGE_BUTTON_X;
        int buttonY = topPos + MANAGE_BUTTON_Y;

        int buttonOffset = getManageButtonOffsetToRender(mouseX, mouseY);
        guiGraphics.blit(TEXTURE, buttonX, buttonY, 177, buttonOffset, MANAGE_BUTTON_WIDTH, MANAGE_BUTTON_HEIGHT);
    }

    private Component getAttuneButtonText() {
        if(getMenu().getProgress() > 0) {
            return Component.translatable("screen.button.artifactory.attune.attune_in_progress");
        } else if (!getMenu().isItemAtMaxLevel()) {
            return Component.translatable("screen.button.artifactory.attune.attune_not_in_progress");
        } else if (getMenu().hasAttunableItemInSlot() && getMenu().isItemAtMaxLevel()) {
            return Component.translatable("screen.button.artifactory.attune.max_attunement_reached");
        } else {
            return Component.literal("");
        }
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderCostTooltip(guiGraphics, mouseX, mouseY);
        renderManageTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderManageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(isHoveringManageButton(mouseX, mouseY)) {
            List<Component> list = Lists.newArrayList();
            list.add(Component.translatable("screen.tooltip.artifactory.manage_button"));
            guiGraphics.renderComponentTooltip(this.font, list, mouseX, mouseY);
        }
    }

    private void renderCostTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(isHoveringAttuneButton(mouseX, mouseY)) {
            List<Component> list = Lists.newArrayList();
            if(getMenu().hasAttunableItemInSlot() && !getMenu().isItemAtMaxLevel() && ClientAttunementNexusSlotInformation.getSlotInformation() != null) {
                AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
                list.add(Component.translatable("screen.tooltip.artifactory.xp_level_threshold", slotInformation.xpThreshold()));
                list.add(Component.translatable("screen.tooltip.artifactory.xp_levels_consumed", slotInformation.xpConsumed()));
            } else if (getMenu().hasAttunableItemInSlot() && getMenu().isItemAtMaxLevel()) {
                list.add(Component.translatable("screen.tooltip.artifactory.item_in_slot_is_max_level"));
            } else {
                list.add(Component.translatable("screen.tooltip.artifactory.no_item_in_slot"));
            }
            guiGraphics.renderComponentTooltip(this.font, list, mouseX, mouseY);
        }
    }

    private void renderAttunementInformation(GuiGraphics guiGraphics, int x, int y) {
        AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
        if(slotInformation != null && this.getMenu().hasAttunableItemInSlot()){
            Component attunementLevelComponent = Component.literal(String.valueOf(slotInformation.levelAchieved()));
            guiGraphics.drawWordWrap(this.font, attunementLevelComponent, x - this.font.width(attunementLevelComponent)/2 + this.imageWidth / 8, y - this.font.lineHeight/2 + 40, 100, BUTTON_TEXT_COLOR);

            if(!getMenu().isItemAtMaxLevel()) {
                if (slotInformation.xpConsumed() > 0) {
                    Component levelCostComponent = Component.literal(String.valueOf(slotInformation.xpConsumed()));
                    guiGraphics.drawWordWrap(this.font, levelCostComponent, x - this.font.width(levelCostComponent) / 2 + this.imageWidth / 8, y - this.font.lineHeight / 2 + 50, 100, BUTTON_TEXT_COLOR);
                }

                if (slotInformation.xpThreshold() > 0) {
                    Component levelThresholdComponent = Component.literal(String.valueOf(slotInformation.xpThreshold()));
                    guiGraphics.drawWordWrap(this.font, levelThresholdComponent, x - this.font.width(levelThresholdComponent) / 2 + this.imageWidth / 8, y - this.font.lineHeight / 2 + 60, 100, BUTTON_TEXT_COLOR);
                }
            }
        }
    }

    private void renderProgressGraphic(GuiGraphics guiGraphics, int x, int y) {
        if(getMenu().getProgress() > 0) {
            guiGraphics.blit(TEXTURE, x + 79, y + 22, 177, 104, 18, getMenu().getScaledProgress() / 2);
            guiGraphics.blit(TEXTURE, x + 97, y + 40, 195, 122, -18, -1 * getMenu().getScaledProgress() / 2);
        }
    }

    @Override
    public void onSlotInformationUpdated(AttunementNexusSlotInformation newSlotInformation) {
        if(newSlotInformation.hasItemRequirement(0)) {
            this.itemRequirementSlotOneRenderer = new ItemRequirementSlotRenderer(0);
        } else {
            this.itemRequirementSlotOneRenderer = null;
        }

        if(newSlotInformation.hasItemRequirement(1)) {
            this.itemRequirementSlotTwoRenderer = new ItemRequirementSlotRenderer(1);
        } else {
            this.itemRequirementSlotTwoRenderer = null;
        }

        if(newSlotInformation.hasItemRequirement(2)) {
            this.itemRequirementSlotThreeRenderer = new ItemRequirementSlotRenderer(2);
        } else {
            this.itemRequirementSlotThreeRenderer = null;
        }
    }

    // HELPERS
    private int getAttuneButtonOffsetToRender(int mouseX, int mouseY) {
        if(!getMenu().ascensionCanStart()) {
            return 39;
        }
        else if(attuneButtonDown) {
            return 26;
        }
        else if (isHoveringAttuneButton(mouseX, mouseY)) {
            return 13;
        }
        else {
            return 0;
        }
    }

    private int getManageButtonOffsetToRender(int mouseX, int mouseY) {
        if(!getMenu().playerHasAttunedItem()) {
            return 91;
        }
        else if(manageButtonDown) {
            return 78;
        }
        else if (isHoveringManageButton(mouseX, mouseY)) {
            return 65;
        }
        else {
            return 52;
        }
    }

    private boolean isHoveringAttuneButton(double mouseX, double mouseY) {
        return isHovering(ATTUNE_BUTTON_X, ATTUNE_BUTTON_Y, ATTUNE_BUTTON_WIDTH, ATTUNE_BUTTON_HEIGHT, mouseX, mouseY);
    }

    private boolean isHoveringManageButton(double mouseX, double mouseY) {
        return isHovering(MANAGE_BUTTON_X, MANAGE_BUTTON_Y, MANAGE_BUTTON_WIDTH, MANAGE_BUTTON_HEIGHT, mouseX, mouseY);
    }

    private void handleAttuneButtonPress() {
        if(this.minecraft != null && this.minecraft.gameMode != null && !getMenu().isItemAtMaxLevel() && getMenu().ascensionCanStart()) {
            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
        }
    }

    private void handleManageButtonPress() {
        if(this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.pushGuiLayer(new AttunementNexusManageScreen(this));
            if(menu.getIsActive()) {
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
            }
        }
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public int getScreenLeftPos() {
        return this.leftPos;
    }

    public int getScreenTopPos() {
        return this.topPos;
    }

    public int getBackgroundX() {
        return (width - imageWidth) / 2;
    }

    public int getBackgroundY() {
        return (height - imageHeight) / 2;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(attuneButtonDown && isHoveringAttuneButton(mouseX, mouseY)) {
            handleAttuneButtonPress();
        } else if(manageButtonDown && isHoveringManageButton(mouseX, mouseY)) {
            handleManageButtonPress();
        }
        manageButtonDown = false;
        attuneButtonDown = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isHoveringAttuneButton(mouseX, mouseY)) {
            attuneButtonDown = true;
            return true;
        } else if (isHoveringManageButton(mouseX, mouseY) && getMenu().playerHasAttunedItem()) {
            manageButtonDown = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public void onClose() {
        super.onClose();
        ClientAttunementNexusSlotInformation.removeListener(this);
    }

    private class ItemRequirementSlotRenderer {
        private final int SLOT_WIDTH = 18;
        private final int SLOT_HEIGHT = 18;
        ItemStack itemToRender = null;
        int index;
        public ItemRequirementSlotRenderer(int index) {
            this.index = index;
        }

        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            int itemRequirementState = getItemRequirementState();
            if(itemRequirementState != ItemRequirementState.NOT_REQUIRED.getValue()) {
                int backgroundX = getBackgroundX();
                int backgroundY = getBackgroundY();

                // Render background for item
                guiGraphics.blit(TEXTURE, backgroundX + getItemRequirementSlotOffsetX(index), backgroundY + getItemRequirementSlotOffsetY(index), 177, getBackgroundOffsetY(), SLOT_WIDTH, SLOT_HEIGHT);

                // Render the required item if no item is in the slot
                if(itemToRender != null && getItemRequirementState() == ItemRequirementState.EMPTY.getValue()) {
                    renderItem(guiGraphics, backgroundX, backgroundY);
                } else {
                    if(itemToRender == null) updateItemToRender();
                }

                renderSlotTooltip(guiGraphics, mouseX, mouseY);
            }
        }

        private int getBackgroundOffsetY() {
            int itemRequirementState = getItemRequirementState();
            if(itemRequirementState == ItemRequirementState.FULFILLED.getValue()) {
                return 161;
            } else if (itemRequirementState == ItemRequirementState.PARTIAL.getValue()) {
                return 142;
            } else {
                return 123;
            }
        }

        private void renderItem(GuiGraphics guiGraphics, int backgroundX, int backgroundY) {
            int itemX = backgroundX + this.getItemRequirementSlotOffsetX(index) + 1;
            int itemY = backgroundY + this.getItemRequirementSlotOffsetY(index) + 1;

            guiGraphics.renderFakeItem(itemToRender, itemX, itemY);
            guiGraphics.renderItemDecorations(font, itemToRender, itemX, itemY);
            guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), itemX, itemY, itemX + 16, itemY + 16, 0x80888888);
        }

        public int getItemRequirementState() {
            return switch (index) {
                case 0 -> getMenu().getItemRequirementOneState();
                case 1 -> getMenu().getItemRequirementTwoState();
                case 2 -> getMenu().getItemRequirementThreeState();
                default -> 0;
            };
        }


        private void updateItemToRender() {
            AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
            if(slotInformation != null && slotInformation.hasItemRequirement(index)) {
                try {
                    this.itemToRender = ResourceLocationUtil.getItemStackFromResourceLocation(ClientAttunementNexusSlotInformation.getSlotInformation().getItemRequirement(index));
                    if(!this.itemToRender.isEmpty()) this.itemToRender.setCount(slotInformation.getItemRequirementQuantity(index));
                } catch (ResourceLocationException exception) {
                    Artifactory.LOGGER.warn("Artifactory - Attunement Nexus - Couldn't create resource location from item requirement string " + ClientAttunementNexusSlotInformation.getSlotInformation().getItemRequirement(index));
                }
            }
        }


        private void renderSlotTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if(isHoveringItemRequirementSlot(mouseX, mouseY)) {
                AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
                if(slotInformation != null) {
                    List<Component> list = Lists.newArrayList();
                    list.add(Component.literal("Requires " + slotInformation.getItemRequirementText(index)));
                    guiGraphics.renderComponentTooltip(font, list, mouseX, mouseY);
                }
            }
        }

        private int getItemRequirementSlotOffsetX(int requirementSlotIndex) {
            int[] xOffsetArray = new int[] { GUIConstants.ITEM_REQ_SLOT_ONE_X, GUIConstants.ITEM_REQ_SLOT_TWO_X, GUIConstants.ITEM_REQ_SLOT_THREE_X };
            return xOffsetArray[requirementSlotIndex];
        }

        private int getItemRequirementSlotOffsetY(int requirementSlotIndex) {
            int[] yOffsetArray = new int[] { GUIConstants.ITEM_REQ_SLOT_ONE_Y, GUIConstants.ITEM_REQ_SLOT_TWO_Y, GUIConstants.ITEM_REQ_SLOT_THREE_Y };
            return yOffsetArray[requirementSlotIndex];
        }

        private boolean isHoveringItemRequirementSlot(int mouseX, int mouseY) {
            return GUIUtil.isHovering(getBackgroundX(), getBackgroundY(), getItemRequirementSlotOffsetX(index), getItemRequirementSlotOffsetY(index), SLOT_WIDTH, SLOT_HEIGHT, mouseX, mouseY);
        }

        public void clearItem() {
            this.itemToRender = null;
        }
    }
}

package net.silvertide.artifactory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunementNexusSlotInformation;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.GUIUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;
import net.silvertide.artifactory.util.UniqueStatus;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.List;

public class AttunementScreen extends AbstractContainerScreen<AttunementMenu> implements ClientAttunementNexusSlotInformation.ClientSlotInformationListener {
    private static final float TEXT_SCALE = 0.85F;
    private static final int BUTTON_TEXT_COLOR = 0xFFFFFF;
    private static final int ATTUNE_BUTTON_X = 22;
    private static final int ATTUNE_BUTTON_Y = 64;
    private static final int ATTUNE_BUTTON_WIDTH = 54;
    private static final int ATTUNE_BUTTON_HEIGHT = 12;
    private static final int MANAGE_BUTTON_X = 156;
    private static final int MANAGE_BUTTON_Y = 8;
    private static final int MANAGE_BUTTON_WIDTH = 12;
    private static final int MANAGE_BUTTON_HEIGHT = 12;
    private static final int INFORMATION_ICON_X = 9;
    private static final int INFORMATION_ICON_Y = 65;
    private static final int INFORMATION_ICON_WIDTH = 10;
    private static final int INFORMATION_ICON_HEIGHT = 10;

    private List<Component> requirementsList;

    private boolean attuneButtonDown = false;
    private boolean manageButtonDown = false;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_attune.png");
    private ItemRequirementSlotRenderer itemRequirementSlotOneRenderer = null;
    private ItemRequirementSlotRenderer itemRequirementSlotTwoRenderer = null;
    private ItemRequirementSlotRenderer itemRequirementSlotThreeRenderer = null;
    private final LocalPlayer player;
    public AttunementScreen(AttunementMenu pMenu, Inventory playerInventory, Component pTitle) {
        super(pMenu, playerInventory, pTitle);
        ClientAttunementNexusSlotInformation.registerListener(this);
        this.player = Minecraft.getInstance().player;
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
        updateRequirementsList();

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

        renderTitle(guiGraphics);
        renderItemRequirementSlots(guiGraphics, mouseX, mouseY);
        renderButtons(guiGraphics, mouseX, mouseY);
        renderManageTooltip(guiGraphics, mouseX, mouseY);
        renderInformationIcon(guiGraphics, mouseX, mouseY);
        renderAttunementInformation(guiGraphics, backgroundX, backgroundY);
        renderProgressGraphic(guiGraphics, backgroundX, backgroundY);
    }

    private void renderTitle(GuiGraphics guiGraphics) {
        Component titleComp = Component.translatable("screen.text.artifactory.attunement.title");
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.7F, this.font, titleComp, leftPos + 119, topPos + 13, 100, BUTTON_TEXT_COLOR);
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
        }

        if(getMenu().hasAttunableItemInSlot()) {
            AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
            if(slotInformation != null && slotInformation.isPlayerAtMaxAttuneLevel()) {
                return Component.translatable("screen.button.artifactory.attune.max_attunement_reached");
            } else {
                return Component.translatable("screen.button.artifactory.attune.attune_not_in_progress");
            }
        }
        return Component.literal("");
    }

    private void renderManageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(isHoveringManageButton(mouseX, mouseY)) {
            List<Component> list = Lists.newArrayList();
            list.add(Component.translatable("screen.tooltip.artifactory.manage_button"));
            guiGraphics.renderComponentTooltip(this.font, list, mouseX, mouseY);
        }
    }

    private void renderInformationIcon(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(requirementsList != null && !requirementsList.isEmpty()) {
            int iconX = leftPos + INFORMATION_ICON_X;
            int iconY = topPos + INFORMATION_ICON_Y;

            int buttonOffset = 52;
            if(isHoveringInformationIcon(mouseX, mouseY)) {
                buttonOffset = 63;
                guiGraphics.renderComponentTooltip(this.font, requirementsList, iconX, iconY);
            }
            guiGraphics.blit(TEXTURE, iconX, iconY, 190, buttonOffset, INFORMATION_ICON_WIDTH, INFORMATION_ICON_HEIGHT);
        }
    }

    private void renderAttunementInformation(GuiGraphics guiGraphics, int backgroundX, int backgroundY) {
        AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
        if(slotInformation != null && this.getMenu().hasAttunableItemInSlot()){
            renderItemName(guiGraphics, backgroundX, backgroundY, slotInformation);
//            renderAttunementSlotRatio(guiGraphics, backgroundX, backgroundY, slotInformation);
//            renderCurrentAttunementLevel(guiGraphics, backgroundX, backgroundY, slotInformation);
//            renderUniqueInfo(guiGraphics, backgroundX, backgroundY, slotInformation);

            if(!slotInformation.isPlayerAtMaxAttuneLevel()) {
//                renderLevelCost(guiGraphics, backgroundX, backgroundY, slotInformation);
//                renderXpThreshold(guiGraphics, backgroundX, backgroundY, slotInformation);
            }
        }
    }

    private void renderItemName(GuiGraphics guiGraphics, int backgroundX, int backgroundY, AttunementNexusSlotInformation slotInformation) {
        GUIUtil.drawScaledWordWrap(guiGraphics, 0.7F, this.font, Component.literal(slotInformation.itemName()), backgroundX + 86, backgroundY + 22, 85, BUTTON_TEXT_COLOR);
    }


    private void renderAttunementSlotRatio(GuiGraphics guiGraphics, int backgroundX, int backgroundY, AttunementNexusSlotInformation slotInformation) {
        int numAttunementSlotsUsedByPlayer = slotInformation.numSlotsUsedByPlayer();
        int totalAttunementSlots = AttunementUtil.getMaxAttunementSlots(this.player);
        int levelAchievedByPlayer = slotInformation.levelAchievedByPlayer();

        String attunementSlotNumerator = String.valueOf(numAttunementSlotsUsedByPlayer);
        String attunementSlotDenominator = String.valueOf(totalAttunementSlots);
        if(levelAchievedByPlayer == 0) {
            attunementSlotNumerator = numAttunementSlotsUsedByPlayer + " + " + slotInformation.slotsUsed() + " (" + (numAttunementSlotsUsedByPlayer + slotInformation.slotsUsed()) + ")";
        }

        MutableComponent numerator = Component.literal(attunementSlotNumerator);
        if(levelAchievedByPlayer == 0 && numAttunementSlotsUsedByPlayer + slotInformation.slotsUsed() > totalAttunementSlots) {
            numerator.withStyle(ChatFormatting.RED);
        }

        Component denominator = Component.literal(attunementSlotDenominator);
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.7F, this.font, Component.translatable("screen.text.artifactory.attunement.slots"), backgroundX + 6 * this.imageWidth / 8, backgroundY + 20, 75, BUTTON_TEXT_COLOR);
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.7F, this.font, numerator, backgroundX + 6 * this.imageWidth / 8, backgroundY + 25, 75, BUTTON_TEXT_COLOR);
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.7F, this.font, denominator, backgroundX + 6 * this.imageWidth / 8, backgroundY + 35, 75, BUTTON_TEXT_COLOR);
    }

    private void renderCurrentAttunementLevel(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        Component attunementLevelComponent = Component.literal(String.valueOf(slotInformation.levelAchievedByPlayer()));
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.7F, this.font, attunementLevelComponent, x + 6 * this.imageWidth / 8, y + 45, 75, BUTTON_TEXT_COLOR);
    }


    private void renderUniqueInfo(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        if(slotInformation.uniqueStatus() != null && !"".equals(slotInformation.uniqueStatus())) {
            Component attunementLevelComponent = Component.translatable("screen.text.artifactory.attunement.unique");
            GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.7F, this.font, attunementLevelComponent, x + 6 * this.imageWidth / 8, y + 70, 75, BUTTON_TEXT_COLOR);
        }
    }

    private void renderLevelCost(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        if (slotInformation.xpConsumed() > 0) {
            Component levelCostComponent = Component.literal(String.valueOf(slotInformation.xpConsumed()));
            GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.7F, this.font, levelCostComponent, x + 6 * this.imageWidth / 8, y + 50, 75, BUTTON_TEXT_COLOR);
        }
    }

    private void renderXpThreshold(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        if (slotInformation.xpThreshold() > 0) {
            Component levelThresholdComponent = Component.literal(String.valueOf(slotInformation.xpThreshold()));
            GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.7F, this.font, levelThresholdComponent, x + 6 * this.imageWidth / 8, y + 60, 75, BUTTON_TEXT_COLOR);
        }
    }

    private void renderProgressGraphic(GuiGraphics guiGraphics, int x, int y) {
        if(getMenu().getProgress() > 0) {
//            guiGraphics.blit(TEXTURE, x + 79, y + 22, 177, 104, 18, getMenu().getScaledProgress() / 2);
//            guiGraphics.blit(TEXTURE, x + 97, y + 40, 195, 122, -18, -1 * getMenu().getScaledProgress() / 2);
        }
    }

    private void updateRequirementsList() {
        if(requirementsList == null && getMenu().hasAttunableItemInSlot()) {
            requirementsList = new ArrayList<>();

            AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
            if(slotInformation != null) {
                if(slotInformation.isPlayerAtMaxAttuneLevel()) {
                    requirementsList.add(Component.translatable("screen.tooltip.artifactory.item_in_slot_is_max_level"));
                } else if(!slotInformation.uniqueStatus().isEmpty()) {
                    if (UniqueStatus.ALREADY_ATTUNED_BY_THIS_PLAYER.equals(slotInformation.uniqueStatus())) {
                        requirementsList.add(Component.translatable("screen.tooltip.artifactory.unique_owner.self"));
                    } else if (UniqueStatus.REACHED_UNIQUE_CAPACITY.equals(slotInformation.uniqueStatus())) {
                        requirementsList.add(Component.translatable("screen.tooltip.artifactory.unique_owner.reached_unique_limit"));
                    } else {
                        requirementsList.add(Component.translatable("screen.tooltip.artifactory.unique_owner.known", slotInformation.uniqueStatus()));
                    }
                }
                else {
                    requirementsList.add(Component.translatable("screen.tooltip.artifactory.xp_level_threshold", slotInformation.xpThreshold()));
                    requirementsList.add(Component.translatable("screen.tooltip.artifactory.xp_levels_consumed", slotInformation.xpConsumed()));
                }
            }
        } else if (!getMenu().hasAttunableItemInSlot() && requirementsList != null) {
            requirementsList = null;
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
        if(!getMenu().canAscensionStart()) {
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
        if(!getMenu().playerHasAnAttunedItem()) {
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

    private boolean isHoveringInformationIcon(double mouseX, double mouseY) {
        return isHovering(INFORMATION_ICON_X, INFORMATION_ICON_Y, INFORMATION_ICON_WIDTH, INFORMATION_ICON_HEIGHT, mouseX, mouseY);
    }

    private boolean isHoveringManageButton(double mouseX, double mouseY) {
        return isHovering(MANAGE_BUTTON_X, MANAGE_BUTTON_Y, MANAGE_BUTTON_WIDTH, MANAGE_BUTTON_HEIGHT, mouseX, mouseY);
    }

    private void handleAttuneButtonPress() {
        if(this.minecraft != null && this.minecraft.gameMode != null && getMenu().canAscensionStart()) {
            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
        }
    }

    private void handleManageButtonPress() {
        if(this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 2);
            if(menu.getIsActive()) {
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
            }
        }
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
        } else if (isHoveringManageButton(mouseX, mouseY) && getMenu().playerHasAnAttunedItem()) {
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
            if(getItemRequirementState() == ItemRequirementState.FULFILLED.getValue()) {
                return 123;
            } else {
                return 104;
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
    }
}

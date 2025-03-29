package net.silvertide.artifactory.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.GUIUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AttunementScreen extends AbstractContainerScreen<AttunementMenu> {
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
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_attune.png");
    private final ItemRequirementSlotRenderer itemRequirementSlotOneRenderer;
    private final ItemRequirementSlotRenderer itemRequirementSlotTwoRenderer;
    private final ItemRequirementSlotRenderer itemRequirementSlotThreeRenderer;
    private final LocalPlayer player;
    private AttunementNexusSlotInformation slotInformation = null;

    public AttunementScreen(AttunementMenu attunementMenu, Inventory playerInventory, Component title) {
        super(attunementMenu, playerInventory, title);
        this.player = Minecraft.getInstance().player;
        itemRequirementSlotOneRenderer = new ItemRequirementSlotRenderer(0);
        itemRequirementSlotTwoRenderer = new ItemRequirementSlotRenderer(1);
        itemRequirementSlotThreeRenderer = new ItemRequirementSlotRenderer(2);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 10000;
        this.titleLabelY = 10000;
        this.inventoryLabelY = 10000;
        this.inventoryLabelX = 10000;

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        try {
            updateRequirementsList();
            renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            renderTooltip(guiGraphics, mouseX, mouseY);
        } catch (Exception ignore) {
            onClose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.slotInformation = getMenu().getAttunementNexusSlotInformation().orElse(null);

        int backgroundX = getBackgroundX();
        int backgroundY = getBackgroundY();
        guiGraphics.blit(TEXTURE, backgroundX, backgroundY, 0, 0, imageWidth, imageHeight);
        renderTitle(guiGraphics);

        if(this.slotInformation != null && this.slotInformation.levelAchievedByPlayer() == 0 && !"".equals(this.slotInformation.attunedToName())) {
            renderAttunedToAnotherPlayerText(guiGraphics, backgroundX, backgroundY, slotInformation);
        } else {
            renderItemRequirementSlots(guiGraphics, mouseX, mouseY);
            renderAttunementInformation(guiGraphics, backgroundX, backgroundY, mouseX, mouseY);
        }

        renderButtons(guiGraphics, mouseX, mouseY);
        renderManageTooltip(guiGraphics, mouseX, mouseY);
        renderInformationIcon(guiGraphics, mouseX, mouseY);
        renderProgressGraphic(guiGraphics, backgroundX, backgroundY);
    }

    private void renderTitle(GuiGraphics guiGraphics) {
        Component titleComp = Component.translatable("screen.text.artifactory.attunement.title");
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, 0.7F, this.font, titleComp, leftPos + 119, topPos + 13, 100, BUTTON_TEXT_COLOR);
    }

    private void renderItemRequirementSlots(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        this.itemRequirementSlotOneRenderer.render(guiGraphics, mouseX, mouseY);
        this.itemRequirementSlotTwoRenderer.render(guiGraphics, mouseX, mouseY);
        this.itemRequirementSlotThreeRenderer.render(guiGraphics, mouseX, mouseY);
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
            if(this.slotInformation != null) {
                if(this.slotInformation.isPlayerAtMaxAttuneLevel()) {
                    return Component.translatable("screen.button.artifactory.attune.max_attunement_reached");
                } else if(this.slotInformation.levelAchievedByPlayer() > 0){
                    return Component.translatable("screen.button.artifactory.attune.ascend_not_in_progress");
                } else {
                    return Component.translatable("screen.button.artifactory.attune.attune_not_in_progress");
                }
            }
            return Component.translatable("screen.button.artifactory.attune.slot_information_not_available");

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

    private void renderAttunementInformation(GuiGraphics guiGraphics, int backgroundX, int backgroundY, int mouseX, int mouseY) {
        if(this.slotInformation != null && this.getMenu().hasAttunableItemInSlot()) {
            renderItemName(guiGraphics, backgroundX, backgroundY, mouseX, mouseY, slotInformation);
            renderCurrentAttunementLevel(guiGraphics, backgroundX, backgroundY, slotInformation);
            renderAttunementSlotInfo(guiGraphics, backgroundX, backgroundY, slotInformation);
            renderAttunementSlotRatio(guiGraphics, backgroundX, backgroundY, mouseX, mouseY, slotInformation);

            if(!slotInformation.attunedByAnotherPlayer() && !slotInformation.isPlayerAtMaxAttuneLevel()) {
                if(slotInformation.xpConsumed() > 0 || slotInformation.xpThreshold() > 0) {
                    renderRequirementText(guiGraphics, backgroundX, backgroundY);
                    renderLevelCost(guiGraphics, backgroundX, backgroundY, slotInformation);
                    renderXpThreshold(guiGraphics, backgroundX, backgroundY, slotInformation);
                }
            }
        }
    }

    private void renderItemName(GuiGraphics guiGraphics, int backgroundX, int backgroundY, int mouseX, int mouseY, AttunementNexusSlotInformation slotInformation) {
        int textOffsetX = 90;
        int textOffsetY = 24;
        float textScale = 0.6F;

        String trimmedText = GUIUtil.trimTextToWidth(slotInformation.itemName(), this.font, 135);
        GUIUtil.drawScaledString(guiGraphics, textScale, this.font, trimmedText, backgroundX + textOffsetX, backgroundY + textOffsetY, 0xC1EFEF);
        guiGraphics.blit(TEXTURE, backgroundX + 88, backgroundY + 29, 0, 167, 78, 1);

        if(!trimmedText.equals(slotInformation.itemName()) && isHovering(textOffsetX, textOffsetY, (int) (this.font.width(trimmedText) * textScale), (int) (this.font.lineHeight * textScale), mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(Component.literal(slotInformation.itemName())), backgroundX + textOffsetX, backgroundY + textOffsetY);
        }
    }

    private void renderCurrentAttunementLevel(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        MutableComponent attunementLevel;
        if(slotInformation.levelAchievedByPlayer() == 0){
            attunementLevel = Component.translatable("screen.text.artifactory.attunement.not_attuned");
        } else {
            attunementLevel = Component.translatable("screen.text.artifactory.attunement.current_level", String.valueOf(slotInformation.levelAchievedByPlayer()));
        }
        GUIUtil.drawScaledWordWrap(guiGraphics, 0.5F, this.font, attunementLevel, x + 92, y + 32, 75, 0x9dbef2);
    }

    private void renderAttunementSlotInfo(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        if(slotInformation.slotsUsed() > 0) {
            Component slotsRequired = Component.translatable("screen.text.artifactory.attunement.slots_required", slotInformation.slotsUsed());
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.5F, this.font, slotsRequired, x + 92, y + 38, 75, 0x9dbef2);
        }
    }

    private void renderRequirementText(GuiGraphics guiGraphics, int x, int y) {
        Component requirementText = Component.translatable("screen.text.artifactory.attunement.requirement_text");
        GUIUtil.drawScaledWordWrap(guiGraphics, 0.55F, this.font, requirementText, x + 90, y + 60, 75, 0xF8B3B0);
    }

    private void renderLevelCost(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        if (slotInformation.xpConsumed() > 0) {
            Component experienceCost = Component.translatable("screen.text.artifactory.manage.requirement_cost");
            GUIUtil.drawLeftAlignedScaledWordWrap(guiGraphics, 0.5F, this.font, experienceCost, x + 136, y + 66, 75, BUTTON_TEXT_COLOR);

            Component levelCostComponent = Component.literal(String.valueOf(slotInformation.xpConsumed()));
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.5F, this.font, levelCostComponent, x + 138, y + 66, 75, BUTTON_TEXT_COLOR);
        }
    }

    private void renderXpThreshold(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        if (slotInformation.xpThreshold() > 0 && slotInformation.xpThreshold() > slotInformation.xpConsumed()) {
            Component experienceThreshold = Component.translatable("screen.text.artifactory.manage.requirement_threshold");
            GUIUtil.drawLeftAlignedScaledWordWrap(guiGraphics, 0.5F, this.font, experienceThreshold, x + 136, y + 72, 75, BUTTON_TEXT_COLOR);

            Component levelThresholdComponent = Component.literal(String.valueOf(slotInformation.xpThreshold()));
            GUIUtil.drawScaledWordWrap(guiGraphics, 0.5F, this.font, levelThresholdComponent, x + 138, y + 72, 75, BUTTON_TEXT_COLOR);
        }
    }

    private void renderAttunementSlotRatio(GuiGraphics guiGraphics, int backgroundX, int backgroundY, int mouseX, int mouseY, AttunementNexusSlotInformation slotInformation) {
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

        int textX = 126;
        int numeratorY = 46;
        int denominatorY = 54;
        float textScale = 0.5F;

        Component denominator = Component.literal(attunementSlotDenominator);
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, textScale, this.font, numerator, backgroundX + textX, backgroundY + numeratorY, 75, BUTTON_TEXT_COLOR);
        GUIUtil.drawScaledCenteredWordWrap(guiGraphics, textScale, this.font, denominator, backgroundX + textX, backgroundY + denominatorY, 75, BUTTON_TEXT_COLOR);

        // Draw title
        Component slotTitle = Component.translatable("screen.text.artifactory.attunement.slot_text");
        GUIUtil.drawLeftAlignedScaledWordWrap(guiGraphics, 0.5F, this.font, slotTitle, backgroundX + 105, backgroundY + 50 - this.font.lineHeight / 4, 75, BUTTON_TEXT_COLOR);

        // Draw dividing line
        guiGraphics.blit(TEXTURE, backgroundX + 109, backgroundY + 50, 0, 169, 34, 1);

        int yBound = (int) (denominatorY - numeratorY + (this.font.lineHeight * textScale));
        if(isHovering(109, numeratorY, 34, yBound, mouseX, mouseY)) {
            List<Component> slotTooltips = new ArrayList<>();
            int slotsUsedByPlayer = slotInformation.numSlotsUsedByPlayer();
            int maxSlots = AttunementUtil.getMaxAttunementSlots(this.player);
            int slotsAvailable = Math.max(maxSlots - slotsUsedByPlayer, 0);

            if(!slotInformation.attunedByAnotherPlayer()) {
                if(slotInformation.levelAchievedByPlayer() == 0) {
                    if(slotsAvailable < slotInformation.slotsUsed()) {
                        slotTooltips.add(Component.translatable("screen.tooltip.artifactory.attunement.not_enough_slots"));
                    }
                    if(slotInformation.slotsUsed() == 1) {
                        slotTooltips.add(Component.translatable("screen.tooltip.artifactory.attunement.one_item_slot_required"));
                    } else if (slotInformation.slotsUsed() > 0) {
                        slotTooltips.add(Component.translatable("screen.tooltip.artifactory.attunement.item_slots_required", slotInformation.slotsUsed()));
                    }
                } else {
                    if(slotInformation.slotsUsed() == 1) {
                        slotTooltips.add(Component.translatable("screen.tooltip.artifactory.attunement.one_item_slot_reserved"));
                    } else if (slotInformation.slotsUsed() > 0) {
                        slotTooltips.add(Component.translatable("screen.tooltip.artifactory.attunement.item_slots_reserved", slotInformation.slotsUsed()));
                    }
                }
            } else {
                slotTooltips.add(Component.translatable("screen.tooltip.artifactory.attuned_to_other_player"));
            }

            if(slotsAvailable == 1) {
                slotTooltips.add(Component.translatable("screen.tooltip.artifactory.attunement.one_slot_available"));
            } else {
                slotTooltips.add(Component.translatable("screen.tooltip.artifactory.attunement.slots_available", slotsAvailable));
            }
            slotTooltips.add(Component.translatable("screen.tooltip.artifactory.attunement.slots_ratio", slotsUsedByPlayer, maxSlots));

            guiGraphics.renderComponentTooltip(this.font, slotTooltips, mouseX, mouseY);
        }
    }


    private void renderAttunedToAnotherPlayerText(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        Component attunedToInfo = Component.translatable("screen.text.artifactory.attunement.attuned_to_other", slotInformation.attunedToName());
        GUIUtil.drawScaledWordWrap(guiGraphics, 0.5F, this.font, attunedToInfo, x + 92, y + 32, 75, 0x9dbef2);
    }

    private void renderProgressGraphic(GuiGraphics guiGraphics, int x, int y) {
        int progress = getMenu().getProgress();
        if(progress > 0) {
            double ratio = (double) progress / getMenu().MAX_PROGRESS % 0.33 / 0.33;
            if(progress >= 40 && progress < 80) {
                // Phase 2
                renderPhaseOne(guiGraphics, x, y, 1D);
                renderPhaseTwo(guiGraphics, x, y, ratio);
            } else if (progress >= 80 && progress < 119) {
                // Phase 3
                renderPhaseOne(guiGraphics, x, y, 1D);
                renderPhaseTwo(guiGraphics, x, y, 1D);
                renderPhaseThree(guiGraphics, x, y, ratio);
            } else if(progress >= 119) {
                // Phase 4
                renderPhaseFour(guiGraphics, x, y);
            } else {
                // Phase 1
                renderPhaseOne(guiGraphics, x, y, ratio);
            }
        }
    }


    private void renderPhaseOne(GuiGraphics guiGraphics, int x, int y, double ratio) {
        int verticalDraw = (int) (ratio * 34);
        guiGraphics.blit(TEXTURE, x + 32, y + 27, 201, 52, 34, verticalDraw);
    }

    private void renderPhaseTwo(GuiGraphics guiGraphics, int x, int y, double ratio) {
        int drawDistance = (int) (ratio * 7);
        renderPhaseTwoNorth(guiGraphics, x, y, drawDistance);
        renderPhaseTwoEast(guiGraphics, x, y, drawDistance);
        renderPhaseTwoSouth(guiGraphics, x, y, drawDistance);
        renderPhaseTwoWest(guiGraphics, x, y, drawDistance);
    }

    private void renderPhaseTwoNorth(GuiGraphics guiGraphics, int x, int y, int drawDistance) {
        guiGraphics.blit(TEXTURE, x + 47, y + 28, 190, 74, 4, drawDistance);
    }

    private void renderPhaseTwoEast(GuiGraphics guiGraphics, int x, int y, int drawDistance) {
        guiGraphics.blit(TEXTURE, x + 65, y + 46, 197, 94, -1 * drawDistance, -4);
    }

    private void renderPhaseTwoSouth(GuiGraphics guiGraphics, int x, int y, int drawDistance) {
        guiGraphics.blit(TEXTURE, x + 51, y + 60, 194, 89, -4, -1 * drawDistance);
    }

    private void renderPhaseTwoWest(GuiGraphics guiGraphics, int x, int y, int drawDistance) {
        guiGraphics.blit(TEXTURE, x + 33, y + 42, 190, 95, drawDistance, 4);
    }

    private void renderPhaseThree(GuiGraphics guiGraphics, int x, int y, double ratio) {
        int drawDistance = (int) (ratio * 8);
        renderPhaseThreeNorth(guiGraphics, x, y, drawDistance);
        renderPhaseThreeEast(guiGraphics, x, y, drawDistance);
        renderPhaseThreeSouth(guiGraphics, x, y, drawDistance);
        renderPhaseThreeWest(guiGraphics, x, y, drawDistance);
    }

    private void renderPhaseThreeNorth(GuiGraphics guiGraphics, int x, int y, int drawDistance) {
        guiGraphics.blit(TEXTURE, x + 51, y + 34, 189, 142, drawDistance, 1);
        guiGraphics.blit(TEXTURE, x + 47, y + 35, 185, 143, -drawDistance, -1);
    }

    private void renderPhaseThreeEast(GuiGraphics guiGraphics, int x, int y, int drawDistance) {
        guiGraphics.blit(TEXTURE, x + 58, y + 46, 196, 154, 1, drawDistance);
        guiGraphics.blit(TEXTURE, x + 59, y + 42, 197, 150, -1, -drawDistance);
    }

    private void renderPhaseThreeSouth(GuiGraphics guiGraphics, int x, int y, int drawDistance) {
        guiGraphics.blit(TEXTURE, x + 51, y + 53, 189, 161, drawDistance, 1);
        guiGraphics.blit(TEXTURE, x + 47, y + 54, 185, 162, -drawDistance, -1);
    }

    private void renderPhaseThreeWest(GuiGraphics guiGraphics, int x, int y, int drawDistance) {
        guiGraphics.blit(TEXTURE, x + 39, y + 46, 177, 154, 1, drawDistance);
        guiGraphics.blit(TEXTURE, x + 40, y + 42, 178, 150, -1, -drawDistance);
    }

    private void renderPhaseFour(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(TEXTURE, x + 40, y + 35, 198, 142, 18, 18);
    }

    private void updateRequirementsList() {
        if(requirementsList != null) requirementsList = null;

        if(getMenu().hasAttunableItemInSlot()) {
            requirementsList = new ArrayList<>();

            if(this.slotInformation != null) {
                if(slotInformation.attunedByAnotherPlayer()) {
                    requirementsList.add(Component.translatable("screen.tooltip.artifactory.attuned_by_another_player", slotInformation.attunedToName()));
                } else if(slotInformation.isPlayerAtMaxAttuneLevel()) {
                    requirementsList.add(Component.translatable("screen.tooltip.artifactory.item_in_slot_is_max_level"));
                } else {
                    if(slotInformation.levelAchievedByPlayer() == 0 && slotInformation.numSlotsUsedByPlayer() + slotInformation.slotsUsed() > AttunementUtil.getMaxAttunementSlots(this.player)) {
                        requirementsList.add(Component.translatable("screen.tooltip.artifactory.not_enough_slots_available"));

                    } else {
                        if(this.player.experienceLevel < slotInformation.xpConsumed()) {
                            requirementsList.add(Component.translatable("screen.tooltip.artifactory.xp_levels_consumed", slotInformation.xpConsumed()));
                        }
                        if(slotInformation.xpThreshold() > slotInformation.xpConsumed() && this.player.experienceLevel < slotInformation.xpThreshold()){
                            requirementsList.add(Component.translatable("screen.tooltip.artifactory.xp_level_threshold", slotInformation.xpThreshold()));
                        }
                        boolean itemOneSlotNeedsItems = slotInformation.hasItemRequirement(0) && getMenu().getItemRequirementOneState() != ItemRequirementState.FULFILLED.getValue();
                        boolean itemTwoSlotNeedsItems = slotInformation.hasItemRequirement(1) && getMenu().getItemRequirementTwoState() != ItemRequirementState.FULFILLED.getValue();
                        boolean itemThreeSlotNeedsItems = slotInformation.hasItemRequirement(2) && getMenu().getItemRequirementThreeState() != ItemRequirementState.FULFILLED.getValue();

                        if(itemOneSlotNeedsItems || itemTwoSlotNeedsItems || itemThreeSlotNeedsItems) {
                            requirementsList.add(Component.translatable("screen.tooltip.artifactory.provide_items"));
                        }
                    }
                }
            }
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
            this.minecraft.gameMode.handleInventoryButtonClick((getMenu()).containerId, 1);
        }
    }

    private void handleManageButtonPress() {
        if(this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick((getMenu()).containerId, 2);
            if(getMenu().getIsActive()) {
                this.minecraft.gameMode.handleInventoryButtonClick((getMenu()).containerId, 1);
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
        this.slotInformation = null;
    }

    private class ItemRequirementSlotRenderer {
        private final int SLOT_WIDTH = 18;
        private final int SLOT_HEIGHT = 18;
        ItemStack itemToRender = ItemStack.EMPTY;
        int itemRequirementState;
        int index;
        public ItemRequirementSlotRenderer(int index) {
            this.index = index;
            updateItemRequirementState();
        }

        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            updateItemRequirementState();
            if(this.itemRequirementState != ItemRequirementState.NOT_REQUIRED.getValue()) {
                updateItemToRender();

                int backgroundX = getBackgroundX();
                int backgroundY = getBackgroundY();

                renderBackground(guiGraphics, backgroundX, backgroundY);
                renderItem(guiGraphics, backgroundX, backgroundY);
                renderSlotTooltip(guiGraphics, mouseX, mouseY);
            }
        }

        private void renderBackground(GuiGraphics guiGraphics, int backgroundX, int backgroundY) {
            guiGraphics.blit(TEXTURE, backgroundX + getItemRequirementSlotOffsetX(index), backgroundY + getItemRequirementSlotOffsetY(index), 177, getBackgroundOffsetY(), SLOT_WIDTH, SLOT_HEIGHT);
        }

        private int getBackgroundOffsetY() {
            if(this.itemRequirementState == ItemRequirementState.FULFILLED.getValue()) {
                return 123;
            } else {
                return 104;
            }
        }

        private void renderItem(GuiGraphics guiGraphics, int backgroundX, int backgroundY) {
            if(this.itemRequirementState == ItemRequirementState.EMPTY.getValue()) {
                int itemX = backgroundX + this.getItemRequirementSlotOffsetX(index) + 1;
                int itemY = backgroundY + this.getItemRequirementSlotOffsetY(index) + 1;

                guiGraphics.renderFakeItem(itemToRender, itemX, itemY);
                guiGraphics.renderItemDecorations(font, itemToRender, itemX, itemY);
                guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), itemX, itemY, itemX + 16, itemY + 16, 0x80888888);
            }
        }

        private void renderSlotTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if(isHoveringItemRequirementSlot(mouseX, mouseY)) {
                if(slotInformation != null) {
                    List<Component> list = Lists.newArrayList();
                    list.add(Component.literal("Requires " + slotInformation.getItemRequirementText(index)));
                    guiGraphics.renderComponentTooltip(font, list, mouseX, mouseY);
                }
            }
        }

        private void updateItemRequirementState() {
            itemRequirementState = getItemRequirementState();
        }

        private void updateItemToRender() {
            if(slotInformation != null) {
                this.itemToRender = slotInformation.getItemRequirement(index);
            } else {
                this.itemToRender = ItemStack.EMPTY;
            }
        }

        private int getItemRequirementState() {
            return switch (index) {
                case 0 -> getMenu().getItemRequirementOneState();
                case 1 -> getMenu().getItemRequirementTwoState();
                case 2 -> getMenu().getItemRequirementThreeState();
                default -> 0;
            };
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

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
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "textures/gui/gui_attunement_nexus_attune.png");
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
        this.titleLabelX = 10000;
        this.titleLabelY = 10000;
        this.inventoryLabelY = 10000;
        this.inventoryLabelX = 10000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        updateRequirementsList();
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int backgroundX = getBackgroundX();
        int backgroundY = getBackgroundY();

        guiGraphics.blit(TEXTURE, backgroundX, backgroundY, 0, 0, imageWidth, imageHeight);

        renderTitle(guiGraphics);

        AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
        if(slotInformation != null && slotInformation.levelAchievedByPlayer() == 0 && !"".equals(slotInformation.attunedToName())) {
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
        if(this.itemRequirementSlotOneRenderer != null && getMenu().getItemRequirementOneState() != ItemRequirementState.NOT_REQUIRED.getValue()) {
            this.itemRequirementSlotOneRenderer.render(guiGraphics, mouseX, mouseY);
        }
        if(this.itemRequirementSlotTwoRenderer != null && getMenu().getItemRequirementOneState() != ItemRequirementState.NOT_REQUIRED.getValue()) {
            this.itemRequirementSlotTwoRenderer.render(guiGraphics, mouseX, mouseY);
        }
        if(this.itemRequirementSlotThreeRenderer != null && getMenu().getItemRequirementOneState() != ItemRequirementState.NOT_REQUIRED.getValue()) {
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
            if(slotInformation != null) {
                if(slotInformation.isPlayerAtMaxAttuneLevel()) {
                    return Component.translatable("screen.button.artifactory.attune.max_attunement_reached");
                } else if(slotInformation.levelAchievedByPlayer() > 0){
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
        AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
        if(slotInformation != null && this.getMenu().hasAttunableItemInSlot()) {
            renderItemName(guiGraphics, backgroundX, backgroundY, mouseX, mouseY, slotInformation);
            renderCurrentAttunementLevel(guiGraphics, backgroundX, backgroundY, slotInformation);
            renderUniqueInfo(guiGraphics, backgroundX, backgroundY, slotInformation);
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


    private void renderUniqueInfo(GuiGraphics guiGraphics, int x, int y, AttunementNexusSlotInformation slotInformation) {
        if(slotInformation.uniqueStatus() != null && !"".equals(slotInformation.uniqueStatus())) {
            Component attunementLevelComponent = Component.translatable("screen.text.artifactory.attunement.unique");
            GUIUtil.drawLeftAlignedScaledWordWrap(guiGraphics, 0.5F, this.font, attunementLevelComponent, x + 165, y + 32, 30, 0xFFAA00);
        }
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

            AttunementNexusSlotInformation slotInformation = ClientAttunementNexusSlotInformation.getSlotInformation();
            if(slotInformation != null) {
                if(slotInformation.attunedByAnotherPlayer()) {
                    requirementsList.add(Component.translatable("screen.tooltip.artifactory.attuned_by_another_player", slotInformation.attunedToName()));
                } else if(slotInformation.isPlayerAtMaxAttuneLevel()) {
                    requirementsList.add(Component.translatable("screen.tooltip.artifactory.item_in_slot_is_max_level"));
                } else if(!slotInformation.uniqueStatus().isEmpty()) {
                    if (UniqueStatus.ALREADY_ATTUNED_BY_THIS_PLAYER.equals(slotInformation.uniqueStatus())) {
                        requirementsList.add(Component.translatable("screen.tooltip.artifactory.unique_owner.self"));
                    } else if (UniqueStatus.ATTUNED_BY_ANOTHER_PLAYER.equals(slotInformation.uniqueStatus())) {
                        requirementsList.add(Component.translatable("screen.tooltip.artifactory.unique_owner.known", slotInformation.attunedToName()));
                    } else if(UniqueStatus.REACHED_UNIQUE_CAPACITY.equals(slotInformation.uniqueStatus())){
                        requirementsList.add(Component.translatable("screen.tooltip.artifactory.unique_owner.reached_unique_limit"));
                    }
                }
                else {
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
                        boolean itemTwoSlotNeedsItems = slotInformation.hasItemRequirement(0) && getMenu().getItemRequirementOneState() != ItemRequirementState.FULFILLED.getValue();
                        boolean itemThreeSlotNeedsItems = slotInformation.hasItemRequirement(0) && getMenu().getItemRequirementOneState() != ItemRequirementState.FULFILLED.getValue();

                        if(itemOneSlotNeedsItems || itemTwoSlotNeedsItems || itemThreeSlotNeedsItems) {
                            requirementsList.add(Component.translatable("screen.tooltip.artifactory.provide_items"));
                        }
                    }
                }
            }
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
        ClientAttunementNexusSlotInformation.removeListener(this);
        ClientAttunementNexusSlotInformation.clearSlotInformation();
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

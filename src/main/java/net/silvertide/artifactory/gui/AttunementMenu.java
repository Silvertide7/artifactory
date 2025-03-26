package net.silvertide.artifactory.gui;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.events.custom.AttuneEvent;
import net.silvertide.artifactory.network.client_packets.CB_OpenManageAttunementsScreen;
import net.silvertide.artifactory.registry.BlockRegistry;
import net.silvertide.artifactory.registry.MenuRegistry;
import net.silvertide.artifactory.services.AttunementService;
import net.silvertide.artifactory.services.ModificationService;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;
import net.silvertide.artifactory.util.*;
import org.jetbrains.annotations.NotNull;

public class AttunementMenu extends AbstractContainerMenu {
    public final int MAX_PROGRESS = 120;
    private final ContainerLevelAccess access;
    private final Player player;
    private AttunementNexusSlotInformation attunementNexusSlotInformation= null;

    // Data Slot Fields
    protected final SimpleContainerData data;

    // Data Slot Accessor Indices
    private final int PROGRESS_INDEX = 0;
    private final int IS_ACTIVE_INDEX = 1;
    private final int ASCENSION_CAN_START_INDEX = 2;
    private final int PLAYER_HAS_ATTUNED_ITEM_INDEX = 3;
    private final int ITEM_REQUIREMENT_ONE_STATE_INDEX = 4;
    private final int ITEM_REQUIREMENT_TWO_STATE_INDEX = 5;
    private final int ITEM_REQUIREMENT_THREE_STATE_INDEX = 6;

    // Slots
    private final Slot attunementInputSlot;
    protected final Container attunementInputContainer = new SimpleContainer(1);

    private final ItemRequirementSlot itemRequirementOneSlot;
    protected final Container itemRequirementOneContainer = new SimpleContainer(1);

    private final ItemRequirementSlot itemRequirementTwoSlot;
    protected final Container itemRequirementTwoContainer = new SimpleContainer(1);

    private final ItemRequirementSlot itemRequirementThreeSlot;
    protected final Container itemRequirementThreeContainer = new SimpleContainer(1);

    // Client Constructor
    public AttunementMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    // Server Constructor
    public AttunementMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(MenuRegistry.ATTUNEMENT_NEXUS_MENU.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        checkContainerSize(playerInventory, 1);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        this.data = new SimpleContainerData(8);
        this.addDataSlots(this.data);

        this.attunementInputSlot = getAttunementInputSlot();
        this.addSlot(attunementInputSlot);

        this.itemRequirementOneSlot = getItemRequirementOneSlot();
        this.addSlot(itemRequirementOneSlot);

        this.itemRequirementTwoSlot = getItemRequirementTwoSlot();
        this.addSlot(itemRequirementTwoSlot);

        this.itemRequirementThreeSlot = getItemRequirementThreeSlot();
        this.addSlot(itemRequirementThreeSlot);

        // Setup initial container data
        setProgress(0);
        setIsActive(false);
        setCanAscensionStart(false);
        setPlayerHasAttunedItem(!ArtifactorySavedData.get().getAttunedItems(player.getUUID()).isEmpty());
        setItemRequirementOneState(0);
        setItemRequirementTwoState(0);
        setItemRequirementThreeState(0);
    }

    // Container Data Getters / Setters
    public int getProgress() { return this.data.get(PROGRESS_INDEX); }
    private void setProgress(int value) { this.data.set(PROGRESS_INDEX, value); }
    public boolean getIsActive() { return this.data.get(IS_ACTIVE_INDEX) > 0; }
    private void setIsActive(boolean value) { this.data.set(IS_ACTIVE_INDEX, value ? 1 : 0); }
    public boolean canAscensionStart() { return this.data.get(ASCENSION_CAN_START_INDEX) > 0; }
    private void setCanAscensionStart(boolean value) { this.data.set(ASCENSION_CAN_START_INDEX, value ? 1 : 0); }
    public boolean playerHasAnAttunedItem() { return this.data.get(PLAYER_HAS_ATTUNED_ITEM_INDEX) > 0; }
    private void setPlayerHasAttunedItem(boolean hasAttunedItem) { this.data.set(PLAYER_HAS_ATTUNED_ITEM_INDEX, hasAttunedItem ? 1 : 0); }
    public int getItemRequirementOneState() { return this.data.get(ITEM_REQUIREMENT_ONE_STATE_INDEX); }
    private void setItemRequirementOneState(int value) { this.data.set(ITEM_REQUIREMENT_ONE_STATE_INDEX, value); }
    public int getItemRequirementTwoState() { return this.data.get(ITEM_REQUIREMENT_TWO_STATE_INDEX); }
    private void setItemRequirementTwoState(int value) { this.data.set(ITEM_REQUIREMENT_TWO_STATE_INDEX, value); }
    public int getItemRequirementThreeState() { return this.data.get(ITEM_REQUIREMENT_THREE_STATE_INDEX); }
    private void setItemRequirementThreeState(int value) { this.data.set(ITEM_REQUIREMENT_THREE_STATE_INDEX, value); }

    // Slots
    private Slot getAttunementInputSlot() {
        return new Slot(attunementInputContainer, 0, 41, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if(getIsActive()) return false;
                boolean isValidAttunementItem = AttunementUtil.isValidAttunementItem(stack);

                // Check if the item is unbreakable from artifactory but it is no longer a
                // valid attunement item and remove unbreakable if so. We have to do it here
                // instead of updateAttunementItemState because only valid attunement items can
                // be placed in the slot and have that method run.
                if(!isValidAttunementItem && DataComponentUtil.isUnbreakable(stack) && DataComponentUtil.getPlayerAttunementData(stack).map(PlayerAttunementData::isUnbreakable).orElse(false)) {
                    DataComponentUtil.removeUnbreakable(stack);
                }
                AttunementService.clearBrokenAttunementIfExists(attunementInputSlot.getItem());


                return isValidAttunementItem;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                clearItemDataSlotData();
                clearAllContainers();
                super.onTake(player, stack);
            }

            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                if(!stack.isEmpty()) {
                    updateAttunementItemDataComponent();
                    ArtifactorySavedData.get().updateDisplayName(stack);
                    AttunementMenu.this.updateAttunementState();
                }
            }

            @Override
            public boolean mayPickup(Player player) {
                if(AttunementMenu.this.getIsActive()) return false;
                return super.mayPickup(player);
            }
        };
    }


    // This method looks at the item in the attunement slot and syncs attunement data
    // from data packs or updates the items NBT if the attunement has been broken
    public void updateAttunementItemDataComponent() {
        if(attunementInputSlot.hasItem()) {
            ItemStack attunementItemStack = attunementInputSlot.getItem();
            ModificationService.applyAttunementModifications(attunementItemStack);
        }
    }

    private ItemRequirementSlot getItemRequirementOneSlot() {
        return new ItemRequirementSlot(itemRequirementOneContainer, 0, GUIConstants.ITEM_REQ_SLOT_ONE_X + 1, GUIConstants.ITEM_REQ_SLOT_ONE_Y + 1) {

            @Override
            public boolean isAttunementActive() {
                return getIsActive();
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if(!player.level().isClientSide()) {
                    AttunementMenu.this.updateItemRequirementDataSlots();
                }
                super.onTake(player, stack);
            }

            @Override
            public void setChanged() {
                if(!player.level().isClientSide()) {
                    AttunementMenu.this.updateItemRequirementDataSlots();
                }
                super.setChanged();
            }
        };
    }

    private ItemRequirementSlot getItemRequirementTwoSlot() {
        return new ItemRequirementSlot(itemRequirementTwoContainer, 0, GUIConstants.ITEM_REQ_SLOT_TWO_X + 1, GUIConstants.ITEM_REQ_SLOT_TWO_Y + 1) {

            @Override
            public boolean isAttunementActive() {
                return getIsActive();
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if(!player.level().isClientSide()) {
                    AttunementMenu.this.updateItemRequirementDataSlots();
                }
                super.onTake(player, stack);
            }

            @Override
            public void setChanged() {
                if(!player.level().isClientSide()) {
                    AttunementMenu.this.updateItemRequirementDataSlots();
                }
                super.setChanged();
            }
        };
    }

    private ItemRequirementSlot getItemRequirementThreeSlot() {
        return new ItemRequirementSlot(itemRequirementThreeContainer, 0, GUIConstants.ITEM_REQ_SLOT_THREE_X + 1, GUIConstants.ITEM_REQ_SLOT_THREE_Y + 1) {

            @Override
            public boolean isAttunementActive() {
                return getIsActive();
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if(!player.level().isClientSide()) {
                    AttunementMenu.this.updateItemRequirementDataSlots();
                }
                super.onTake(player, stack);
            }

            @Override
            public void setChanged() {
                if(!player.level().isClientSide()) {
                    AttunementMenu.this.updateItemRequirementDataSlots();
                }
                super.setChanged();
            }
        };
    }


    @Override
    public boolean clickMenuButton(@NotNull Player player, int pId) {
        if(pId == 1 && attunementInputSlot.hasItem()) {
            if(getProgress() > 0) {
                setProgress(0);
                setIsActive(false);
            } else {
                setIsActive(true);
            }
        } else if (pId == 2 && player instanceof ServerPlayer serverPlayer && AttunementUtil.doesPlayerHaveAttunedItem(serverPlayer)) {
            clearAllContainers();
            clearItemDataSlotData();
            setProgress(0);
            setIsActive(false);

            int numUnique = ServerConfigs.NUMBER_UNIQUE_ATTUNEMENTS_PER_PLAYER.get();
            PacketDistributor.sendToPlayer(serverPlayer, new CB_OpenManageAttunementsScreen(numUnique));
        }
        return super.clickMenuButton(player, pId);
    }


    @Override
    public void broadcastChanges() {
        if(player instanceof ServerPlayer serverPlayer && getIsActive()) {
            if(getProgress() < MAX_PROGRESS) {
                if(getProgress() == 1) {
                    playStartEffects(serverPlayer);
                } else if(getProgress() % 3 == 0) {
                    playProgressEffects(serverPlayer);
                }
                setProgress(getProgress() + 1);
            } else {
                if(this.canAscensionStart()) {
                    ItemStack stack = this.attunementInputSlot.getItem();
                    if(!NeoForge.EVENT_BUS.post(new AttuneEvent.Pre(player, stack)).isCanceled()) {
                        handleAttunement(stack);
                        playAttuneEffects(serverPlayer);
                        NeoForge.EVENT_BUS.post(new AttuneEvent.Post(player, stack));
                    }
                }
                setIsActive(false);
                setProgress(0);
            }
        }
        super.broadcastChanges();
    }

    private void playStartEffects(ServerPlayer player) {
        GUIUtil.playSound(player.serverLevel(), player, SoundEvents.BEACON_ACTIVATE);
    }

    private void playProgressEffects(ServerPlayer player) {
        GUIUtil.spawnParticals(player.serverLevel(), player, ParticleTypes.ENCHANT, getProgress() / 3);
        GUIUtil.playSound(player.serverLevel(), player, SoundEvents.BEACON_AMBIENT);
    }

    private void playAttuneEffects(ServerPlayer player) {
        GUIUtil.spawnParticals(player.serverLevel(), player, ParticleTypes.ENCHANT, getProgress() / 2);
        GUIUtil.playSound(player.serverLevel(), player, SoundEvents.BEACON_DEACTIVATE);
    }

    private boolean meetsRequirementsToAttune() {
        if(this.attunementNexusSlotInformation == null) return false;

        if(!this.itemRequirementOneSlot.hasRequiredItems()) return false;
        if(!this.itemRequirementTwoSlot.hasRequiredItems()) return false;
        if(!this.itemRequirementThreeSlot.hasRequiredItems()) return false;

        int cost = this.attunementNexusSlotInformation.xpConsumed();
        int threshold = this.attunementNexusSlotInformation.xpThreshold();

        int playerLevel = player.experienceLevel;
        if(threshold > 0 && playerLevel < threshold){
            return false;
        }
        if(cost > 0 && playerLevel < cost){
            return false;
        }

        return true;
    }

    private void clearAllContainers() {
        this.clearContainer(player, this.attunementInputContainer);
        this.clearContainer(player, this.itemRequirementOneContainer);
        this.clearContainer(player, this.itemRequirementTwoContainer);
        this.clearContainer(player, this.itemRequirementThreeContainer);
    }

    private void payCostForAttunement() {
        int cost = this.attunementNexusSlotInformation.xpConsumed();
        if(cost > 0) player.giveExperienceLevels(-cost);

        itemRequirementOneSlot.consumeRequiredItems();
        itemRequirementTwoSlot.consumeRequiredItems();
        itemRequirementThreeSlot.consumeRequiredItems();
    }

    private void handleAttunement(ItemStack stackToAttune) {
        if(player instanceof ServerPlayer serverPlayer) {
            AttunementService.increaseLevelOfAttunement(serverPlayer, stackToAttune);

            if(!player.getAbilities().instabuild) this.payCostForAttunement();
            attunementInputSlot.setChanged();
            this.access.execute((level, blockPos) -> {
                this.clearContainer(player, this.itemRequirementOneContainer);
                this.clearContainer(player, this.itemRequirementTwoContainer);
                this.clearContainer(player, this.itemRequirementThreeContainer);
            });
        }
        updateAttunementState();
    }

    public void updateAttunementState() {
        if(this.player.level().isClientSide()) return;
        clearItemDataSlotData();
        setPlayerHasAttunedItem(!ArtifactorySavedData.get().getAttunedItems(this.player.getUUID()).isEmpty());

        if(!attunementInputContainer.isEmpty()) {
            ItemStack stack = attunementInputContainer.getItem(0);
            if(this.player instanceof ServerPlayer serverPlayer) {
                this.attunementNexusSlotInformation = AttunementNexusSlotInformation.createAttunementNexusSlotInformation(serverPlayer, stack);
                if(this.attunementNexusSlotInformation != null) {
                    updateItemSlotRequirements(this.attunementNexusSlotInformation);
                    NetworkUtil.syncClientAttunementNexusSlotInformation(serverPlayer, this.attunementNexusSlotInformation);
                }
            }
        }
        updateAscensionCanStart();
    }

    private void updateAscensionCanStart() {
        boolean ascensionCanStart = false;
        if(this.attunementInputSlot.hasItem()) {
            boolean meetsRequirementsToAttune = player.getAbilities().instabuild || this.meetsRequirementsToAttune();
            ascensionCanStart = AttunementUtil.canIncreaseAttunementLevel(this.player, this.attunementInputSlot.getItem())
                    && meetsRequirementsToAttune;
        }
        setCanAscensionStart(ascensionCanStart);
    }

    private void updateItemSlotRequirements(AttunementNexusSlotInformation attunementNexusSlotInformation) {
        if(attunementNexusSlotInformation.hasItemRequirement(0)) {
            this.itemRequirementOneSlot.setItemRequired(attunementNexusSlotInformation.getItemRequirement(0), attunementNexusSlotInformation.getItemRequirementQuantity(0));
        } else {
            this.itemRequirementOneSlot.clearItemRequired();
        }

        if(attunementNexusSlotInformation.hasItemRequirement(1)) {
            this.itemRequirementTwoSlot.setItemRequired(attunementNexusSlotInformation.getItemRequirement(1), attunementNexusSlotInformation.getItemRequirementQuantity(1));
        } else {
            this.itemRequirementTwoSlot.clearItemRequired();
        }

        if(attunementNexusSlotInformation.hasItemRequirement(2)) {
            this.itemRequirementThreeSlot.setItemRequired(attunementNexusSlotInformation.getItemRequirement(2), attunementNexusSlotInformation.getItemRequirementQuantity(2));
        } else {
            this.itemRequirementThreeSlot.clearItemRequired();
        }
        updateItemRequirementDataSlots();
    }


    private void updateItemRequirementDataSlots() {
        setItemRequirementOneState(itemRequirementOneSlot.getItemRequirementState().getValue());
        setItemRequirementTwoState(itemRequirementTwoSlot.getItemRequirementState().getValue());
        setItemRequirementThreeState(itemRequirementThreeSlot.getItemRequirementState().getValue());
        updateAscensionCanStart();
    }


    public void clearItemDataSlotData() {
        setCanAscensionStart(false);

        setItemRequirementOneState(ItemRequirementState.NOT_REQUIRED.getValue());
        setItemRequirementTwoState(ItemRequirementState.NOT_REQUIRED.getValue());
        setItemRequirementThreeState(ItemRequirementState.NOT_REQUIRED.getValue());

        this.attunementNexusSlotInformation = null;

        this.itemRequirementOneSlot.clearItemRequired();
        this.itemRequirementTwoSlot.clearItemRequired();
        this.itemRequirementThreeSlot.clearItemRequired();
    }

    public boolean hasAttunableItemInSlot() {
        return !this.attunementInputContainer.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, blockPos) -> level.getBlockState(blockPos).is(BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get()) && player.distanceToSqr((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D) <= 64.0D, true);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                int slot = j + i * 9 + 9;
                int x = 8 + j * 18;
                int y = 84 + i * 18;
                this.addSlot(new Slot(playerInventory, slot, x, y));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void removed(Player player) {
        clearItemDataSlotData();
        setPlayerHasAttunedItem(false);
        setProgress(0);
        setIsActive(false);

        super.removed(player);
        this.access.execute((level, blockPos) -> {
            clearAllContainers();
        });
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 4;  // must be the number of slots you have!
    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }
}

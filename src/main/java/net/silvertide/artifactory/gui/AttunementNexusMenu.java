package net.silvertide.artifactory.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.registry.BlockRegistry;
import net.silvertide.artifactory.registry.MenuRegistry;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataPackUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AttunementNexusMenu extends AbstractContainerMenu {
    public final int MAX_PROGRESS = 40;
    private final ContainerLevelAccess access;
    private final Player player;
    private final Slot attunementSlot;

    protected final ContainerData data;
    private int progress = 0;
    private int isActive = 0;
    private int canItemAscend = 0;
    private int levelAttunementAchieved = 0;
    private int cost = -1;
    private int threshold = -1;
    private final int PROGRESS_INDEX = 0;
    private final int IS_ACTIVE_INDEX = 1;
    private final int CAN_ITEM_ASCEND_INDEX = 2;
    private final int LEVEL_ATTUNEMENT_ACHIEVED_INDEX = 3;
    private final int COST_INDEX = 4;
    private final int THRESHOLD_INDEX = 5;

    protected final Container inputSlot = new SimpleContainer(1) {
        /**
         * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void setChanged() {
            super.setChanged();
            AttunementNexusMenu.this.slotsChanged(this);
        }
    };
    public AttunementNexusMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public AttunementNexusMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(MenuRegistry.ATTUNEMENT_NEXUS_MENU.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        checkContainerSize(playerInventory, 1);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch(index) {
                    case 0 -> AttunementNexusMenu.this.progress;
                    case 1 -> AttunementNexusMenu.this.isActive;
                    case 2 -> AttunementNexusMenu.this.canItemAscend;
                    case 3 -> AttunementNexusMenu.this.levelAttunementAchieved;
                    case 4 -> AttunementNexusMenu.this.cost;
                    case 5 -> AttunementNexusMenu.this.threshold;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch(index) {
                    case 0 -> AttunementNexusMenu.this.progress = value;
                    case 1 -> AttunementNexusMenu.this.isActive = value;
                    case 2 -> AttunementNexusMenu.this.canItemAscend = value;
                    case 3 -> AttunementNexusMenu.this.levelAttunementAchieved = value;
                    case 4 -> AttunementNexusMenu.this.cost = value;
                    case 5 -> AttunementNexusMenu.this.threshold = value;
                }
            }

            @Override
            public int getCount() {
                return 6;
            }
        };

        this.addDataSlots(this.data);

        attunementSlot = new Slot(inputSlot, 0, 80, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DataPackUtil.getAttunementData(stack).map(attunementData -> AttunementUtil.isAttunementAllowed(player, stack, attunementData)).orElse(false);
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                setCost(-1);
                setThreshold(-1);
                setLevelAttunementAchieved(0);
                setCanItemAscend(0);
                if(getIsActive()) setIsActive(0);
                if(getProgress() > 0) setProgress(0);
                super.onTake(player, stack);
            }
        };

        this.addSlot(attunementSlot);
    }

    // Block Data Methods
    public int getProgress() { return this.data.get(PROGRESS_INDEX); }
    public void setProgress(int value) { this.data.set(PROGRESS_INDEX, value); }
    public boolean getIsActive() { return this.data.get(IS_ACTIVE_INDEX) > 0; }
    public void setIsActive(int value) { this.data.set(IS_ACTIVE_INDEX, value); }
    public boolean canItemAscend() { return this.data.get(CAN_ITEM_ASCEND_INDEX) > 0; }
    public void setCanItemAscend(int value) { this.data.set(CAN_ITEM_ASCEND_INDEX, value); }
    public int getLevelAttunementAchieved() { return this.data.get(LEVEL_ATTUNEMENT_ACHIEVED_INDEX); }
    public void setLevelAttunementAchieved(int value) { this.data.set(LEVEL_ATTUNEMENT_ACHIEVED_INDEX, value); }
    public int getCost() { return this.data.get(COST_INDEX); }
    public void setCost(int value) { this.data.set(COST_INDEX, value); }
    public int getThreshold() { return this.data.get(THRESHOLD_INDEX); }
    public void setThreshold(int value) { this.data.set(THRESHOLD_INDEX, value); }


//    public void handleItemRemoved() {
//        this.blockEntity.clearPlayerToAttuneToUUID();
//        setCanAttune(false);
//    }
    
    @Override
    public boolean clickMenuButton(@NotNull Player player, int pId) {
        if(pId == 1 && attunementSlot.hasItem()) {
            if(progress > 0) {
                setProgress(0);
                setIsActive(0);
            } else {
                setIsActive(1);
            }
        }
        return super.clickMenuButton(player, pId);
    }

    @Override
    public void slotsChanged(Container pContainer) {
        super.slotsChanged(pContainer);
        updateAttunementState();
    }

    @Override
    public void broadcastChanges() {
        if(getIsActive()) {
            if(getProgress() < MAX_PROGRESS) {
                setProgress(getProgress() + 1);
            } else {
                if(this.attunementSlot.hasItem()) {
                    AttunementUtil.attuneItemAndPlayer(this.player, this.attunementSlot.getItem());
                }
                setIsActive(0);
                setProgress(0);
            }
        }
        super.broadcastChanges();
    }

    private void updateAttunementState() {
        if(this.player.level().isClientSide()) return;

        if(!inputSlot.isEmpty()) {
            ItemStack attuneableItemStack = inputSlot.getItem(0);
            int nextLevelOfAttunement = AttunementUtil.getLevelOfAttunementAchieved(attuneableItemStack) + 1;

            // Check if this is the first attunement, if not then make sure ascension exists
            if(nextLevelOfAttunement == 1 || DataPackUtil.getAttunementLevel(attuneableItemStack, nextLevelOfAttunement).isPresent()) {
                updateAttunementRequirements(attuneableItemStack, nextLevelOfAttunement);
                setCanItemAscend(1);
            } else {
                if(this.canItemAscend != 0) setCanItemAscend(0);
            }
        } else {
            if(this.canItemAscend != 0) setCanItemAscend(0);
        }
    }

    private void updateAttunementRequirements(ItemStack attuneableItemStack, int nextLevelOfAttunement) {
        DataPackUtil.getAttunementRequirements(attuneableItemStack, nextLevelOfAttunement).ifPresentOrElse(
            attunementRequirements -> {
                if(attunementRequirements.xpLevelsConsumed() >= 0) {
                    setCost(attunementRequirements.xpLevelsConsumed());
                } else {
                    setCost(Config.XP_LEVELS_TO_ATTUNE_CONSUMED.get());
                }

                if(attunementRequirements.xpLevelThreshold() >= 0) {
                    setThreshold(attunementRequirements.xpLevelThreshold());
                } else {
                    setThreshold(Config.XP_LEVELS_TO_ATTUNE_THRESHOLD.get());
                }
            },
            () -> {
                setCost(Config.XP_LEVELS_TO_ATTUNE_CONSUMED.get());
                setThreshold(Config.XP_LEVELS_TO_ATTUNE_THRESHOLD.get());
            }
        );
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
        super.removed(player);
        this.access.execute((p_39796_, p_39797_) -> {
            this.clearContainer(player, this.inputSlot);
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
    private static final int TE_INVENTORY_SLOT_COUNT = 1;  // must be the number of slots you have!
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

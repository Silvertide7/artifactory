package net.silvertide.artifactory.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.config.codecs.AttunementRequirements;
import net.silvertide.artifactory.registry.BlockRegistry;
import net.silvertide.artifactory.registry.MenuRegistry;
import net.silvertide.artifactory.util.ArtifactUtil;
import net.silvertide.artifactory.util.AttunementDataUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AttunementNexusMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Player player;
    private final Slot attunementSlot;
    private final DataSlot cost = DataSlot.standalone();
    private final DataSlot threshold = DataSlot.standalone();
    protected final Container inputSlots = new SimpleContainer(1) {
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

        this.addDataSlot(this.cost);
        this.addDataSlot(this.threshold);
        attunementSlot = new Slot(inputSlots, 0, 80, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AttunementDataUtil.getAttunementData(stack).map(attunementData -> ArtifactUtil.isAttunementAllowed(player, stack, attunementData)).orElse(false);
            }
        };

        this.addSlot(attunementSlot);

//        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
//            Slot customInputSlot = new SlotItemHandler(iItemHandler, 0, 80, 23) {
//                @Override
//                public boolean mayPlace(@NotNull ItemStack stack) {
//                    return AttunementDataUtil.getAttunementData(stack).map(attunementData -> ArtifactUtil.isAttunementAllowed(player, stack, attunementData)).orElse(super.mayPlace(stack));
//                }
//
//                @Override
//                public void setChanged() {
//                    Artifactory.LOGGER.info("Side: " + (player.level().isClientSide() ? "Client" : "Server"));
//                    if(this.hasItem()) {
//                        handleItemPlaced(this.getItem(), player);
//                    } else {
//                        handleItemRemoved();
//                    }
//                    super.setChanged();
//                }
//            };
//
//            this.addSlot(customInputSlot);
//        });
    }


//    public void handleItemPlaced(ItemStack stack, Player player) {
//        int levelToAttuneTo = ArtifactUtil.getLevelOfAttunementAchieved(stack) + 1;
//
//        if(ArtifactUtil.isAvailableToAttune(stack)) {
//            Optional<AttunementRequirements> attunementRequirements = AttunementDataUtil.getAttunementRequirements(stack, levelToAttuneTo);
//            if(attunementRequirements.isPresent()) {
//
//                if(attunementRequirements.get().xpLevelThreshold() >= 0) {
//                    setXPThreshold(attunementRequirements.get().xpLevelThreshold());
//                } else {
//                    setXPThreshold(Config.XP_LEVELS_TO_ATTUNE_THRESHOLD.get());
//                }
//
//                if(attunementRequirements.get().xpLevelsConsumed() >= 0) {
//                    setXPConsumed(attunementRequirements.get().xpLevelsConsumed());
//                } else {
//                    setXPConsumed(Config.XP_LEVELS_TO_ATTUNE_CONSUMED.get());
//                }
//            } else {
//                setXPThreshold(Config.XP_LEVELS_TO_ATTUNE_THRESHOLD.get());
//                setXPThreshold(Config.XP_LEVELS_TO_ATTUNE_CONSUMED.get());
//            }
//
//            setCanAttune(true);
//        }
//    }

//    public void handleItemRemoved() {
//        this.blockEntity.clearPlayerToAttuneToUUID();
//        setCanAttune(false);
//    }
    
//    @Override
//    public boolean clickMenuButton(@NotNull Player player, int pId) {
//        if(pId == 1) {
//            if (this.blockEntity.getData().get(2) == 1) {
//                this.blockEntity.getData().set(2, 0);
//            } else {
//                this.blockEntity.getData().set(2, 1);
//            }
//        }
//        return super.clickMenuButton(player, pId);
//    }

//    @Override
//    public boolean stillValid(Player player) {
//        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get());
//    }

    @Override
    public void slotsChanged(Container pContainer) {
        super.slotsChanged(pContainer);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, blockPos) -> {
            return !level.getBlockState(blockPos).is(BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get()) ? false : player.distanceToSqr((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D) <= 64.0D;
        }, true);
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
//
//    // Block Entity Data Methods
//    public int getScaledProgress() {
//        int progress = this.blockEntity.getData().get(0);
//        int maxProgress = this.blockEntity.getData().get(1);
//        int progressArrowSize = 26;
//
//        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
//    }
//
//    public boolean itemCanStartAttuning() {
//        return this.blockEntity.getData().get(3) == 1;
//    }
//
//    public boolean isCrafting() {
//        return this.blockEntity.getData().get(0) > 0;
//    }
//
//    public void setCanAttune(boolean canAttune) {
//        if(canAttune) {
//            this.blockEntity.getData().set(3, 1);
//        } else {
//            this.blockEntity.getData().set(3, 0);
//        }
//    }
//
//    public int getXPConsumed() {
//        return this.blockEntity.getData().get(4);
//    }
//
//    public void setXPConsumed(int xpConsumed) {
//        this.blockEntity.getData().set(4, xpConsumed);
//    }
//
//    public int getXPThreshold() {
//        return this.blockEntity.getData().get(5);
//    }
//
//    public void setXPThreshold(int xpThreshold) {
//        this.blockEntity.getData().set(5, xpThreshold);
//    }


    public int getCost() { return this.cost.get(); }
    public int getThreshold() { return this.threshold.get(); }

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

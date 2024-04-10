package net.silvertide.artifactory.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.blocks.entity.AttunementNexusBlockEntity;
import net.silvertide.artifactory.registry.BlockRegistry;
import net.silvertide.artifactory.registry.MenuRegistry;
import net.silvertide.artifactory.util.ArtifactUtil;
import org.jetbrains.annotations.NotNull;

public class AttunementNexusMenu extends AbstractContainerMenu {
    public final AttunementNexusBlockEntity blockEntity;
    private final Level level;
    private final Player player;
    private final ContainerData data;

    public AttunementNexusMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public AttunementNexusMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData containerData) {
        super(MenuRegistry.ATTUNEMENT_NEXUS_MENU.get(), containerId);
        checkContainerSize(inv, 2);
        this.blockEntity = (AttunementNexusBlockEntity) blockEntity;
        this.player = inv.player;
        level = inv.player.level();
        this.data = containerData;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);



        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            Slot customInputSlot = new SlotItemHandler(iItemHandler, 0, 80, 11) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return ArtifactUtil.getAttunementData(stack).map(attunementData -> ArtifactUtil.isAttunementAllowed(player, stack, attunementData)).orElse(super.mayPlace(stack));
                }

                @Override
                public void setChanged() {
                    // TODO: Make sure the player itemUUID reference is cleared when the item is removed.
                    if(this.hasItem()){
                        ((AttunementNexusBlockEntity) blockEntity).setPlayerToAttuneUUID(player);
                    } else {
                        ((AttunementNexusBlockEntity) blockEntity).clearPlayerToAttuneToUUID();
                    }
                    super.setChanged();
                }
            };
            this.addSlot(customInputSlot);

            Slot customOutputSlot = new SlotItemHandler(iItemHandler, 1, 80, 59) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            };
            this.addSlot(customOutputSlot);
        });

        addDataSlots(data);
//        addItemAddedListener(inv.player);
    }

//    private void addItemAddedListener(Player player) {
//        ContainerListener slotListener = new ContainerListener() {
//            @Override
//            public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
//                if(pDataSlotIndex == 36 && ArtifactUtil.isAttuneable(pContainerToSend.slots.get(36).getItem())) {
//                    Artifactory.LOGGER.info("Added player uuid to block entity");
//                    blockEntity.setPlayerToAttuneToUUID(player);
//                }
//            }
//
//            @Override
//            public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {
//
//            }
//        };
//
//        this.addSlotListener(slotListener);
//    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);
        int progressArrowSize = 26;

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
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
    private static final int TE_INVENTORY_SLOT_COUNT = 2;  // must be the number of slots you have!
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

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get());
    }

    @Override
    public void slotsChanged(Container pContainer) {
        Artifactory.LOGGER.info("container changed:" + pContainer);
        Artifactory.LOGGER.info("Slots changed brah");
//        if(pDataSlotIndex == 36 && ArtifactUtil.isAttuneable(pContainerToSend.slots.get(36).getItem())) {
//            Artifactory.LOGGER.info("Added player uuid to block entity");
//            blockEntity.setPlayerToAttuneToUUID(player);
//        }
        super.slotsChanged(pContainer);
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
}

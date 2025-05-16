package net.silvertide.artifactory.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class ItemRequirementSlot extends Slot {
    private ItemStack stackRequired = ItemStack.EMPTY;
    public ItemRequirementSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    public abstract boolean isAttunementActive();

    public void setItemRequired(ItemStack stack) {
        this.stackRequired = stack;
    }

    public boolean hasRequiredItems() {
        if(this.stackRequired.isEmpty()) return true;

        if(this.hasItem()) {
            ItemStack itemInSlot = this.getItem();
            boolean isSameItem = itemInSlot.is(this.stackRequired.getItem());
            boolean hasRequiredCount = itemInSlot.getCount() >= this.stackRequired.getCount();
            return isSameItem && hasRequiredCount;
        }
        return false;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if(this.isAttunementActive()) return false;
        if(this.stackRequired.isEmpty()) return false;
        return stack.is(this.stackRequired.getItem());
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        if(this.isAttunementActive()) return false;
        return super.mayPickup(player);
    }

    public void clearItemRequired() {
        this.stackRequired = ItemStack.EMPTY;
    }

    public ItemRequirementState getItemRequirementState() {
        if(this.stackRequired != null && !this.stackRequired.isEmpty()) {
            if(this.hasItem()) {
                if(this.hasRequiredItems()) {
                    return ItemRequirementState.FULFILLED;
                } else {
                    return ItemRequirementState.PARTIAL;
                }
            } else {
                return ItemRequirementState.EMPTY;
            }
        } else {
            return ItemRequirementState.NOT_REQUIRED;
        }
    }

    public void consumeRequiredItems() {
        if(this.hasItem() && this.hasRequiredItems()){
            ItemStack itemInSlot = this.getItem();
            itemInSlot.shrink(this.stackRequired.getCount());
        }
    }
}

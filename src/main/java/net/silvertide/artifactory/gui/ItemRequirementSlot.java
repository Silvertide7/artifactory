package net.silvertide.artifactory.gui;

import net.minecraft.ResourceLocationException;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.util.ResourceLocationUtil;

public abstract class ItemRequirementSlot extends Slot {

    private ItemStack stackRequired = ItemStack.EMPTY;
    public ItemRequirementSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    public abstract boolean isAttunementActive();

    public void setItemRequired(String resourceLocation, int quantity) {
        if(quantity > 0) {
            try {
                Item item = ResourceLocationUtil.getItemFromResourceLocation(resourceLocation);
                ItemStack stack = new ItemStack(item);
                stack.setCount(quantity);
                this.stackRequired = stack;
            } catch( ResourceLocationException exception){
                this.stackRequired = ItemStack.EMPTY;
            }
        } else {
            this.stackRequired = ItemStack.EMPTY;
        }
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
    public boolean mayPlace(ItemStack stack) {
        if(this.isAttunementActive()) return false;
        if(this.stackRequired.isEmpty()) return false;
        return stack.is(this.stackRequired.getItem());
    }

    @Override
    public boolean mayPickup(Player player) {
        if(this.isAttunementActive()) return false;
        return super.mayPickup(player);
    }

    public void clearItemRequired() {
        this.stackRequired = ItemStack.EMPTY;
    }

    public ItemRequirementState getItemRequirementState() {
        if(this.stackRequired != null && this.stackRequired != ItemStack.EMPTY) {
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

package net.silvertide.artifactory.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;

import java.util.Optional;
import java.util.UUID;

public class StackNBTUtil {
    private static final String ITEM_ATTUNEMENT_UUID_NBT_KEY = "attunement_uuid";
    private static final String ATTUNED_TO_UUID_NBT_KEY = "attuned_to_uuid";
    private static final String ATTUNED_TO_NAME_NBT_KEY = "attuned_to_name";
    private static final String MODIFICATION_SOULBOUND_NBT_KEY = "soulbound";
    private static final String MODIFICATION_INVULNERABLE_NBT_KEY = "invulnerable";

    // BASIC TAG FUNCTIONS

    public static void setBoolean(ItemStack stack, String key, boolean value) {
        stack.getOrCreateTag().putBoolean(key, value);
    }

    // ATTUNEMENT TAG FUNCTIONS
    public static void putPlayerDataInArtifactoryTag(Player player, ItemStack stack) {
        setAttunedToUUID(stack, player.getUUID());
        setAttunedToName(stack, player.getDisplayName().getString());
    }

    public static void setItemAttunementUUID(ItemStack stack, UUID attunementUUID) {
        setArtifactoryUUID(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY, attunementUUID);
    }

    public static void setSoulbound(ItemStack stack) {
        setArtifactoryBoolean(stack, MODIFICATION_SOULBOUND_NBT_KEY, true);
    }

    public static boolean isSoulbound(ItemStack stack){
        return stackArtifactoryTagContainsTag(stack, MODIFICATION_SOULBOUND_NBT_KEY) && getArtifactoryBoolean(stack, MODIFICATION_SOULBOUND_NBT_KEY);
    }

    public static void setInvulnerable(ItemStack stack) {
        setArtifactoryBoolean(stack, MODIFICATION_INVULNERABLE_NBT_KEY, true);
    }

    public static boolean isInvulnerable(ItemStack stack){
        return stackArtifactoryTagContainsTag(stack, MODIFICATION_INVULNERABLE_NBT_KEY) && getArtifactoryBoolean(stack, MODIFICATION_INVULNERABLE_NBT_KEY);
    }

    public static void setUnbreakable(ItemStack stack) {
        stack.setDamageValue(0);
        setBoolean(stack, "Unbreakable", true);
    }

    public static Optional<UUID> getItemAttunementUUID(ItemStack stack) {
        if(stackArtifactoryTagContainsTag(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY)) {
            return Optional.of(getArtifactoryUUID(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY));
        } else {
            return Optional.empty();
        }
    }

    public static boolean containsAttunedToUUID(ItemStack stack) {
        return stackArtifactoryTagContainsTag(stack, ATTUNED_TO_UUID_NBT_KEY);
    }

    public static Optional<UUID> getAttunedToUUID(ItemStack stack) {
        if(containsAttunedToUUID(stack)) {
            return Optional.of(getArtifactoryUUID(stack, ATTUNED_TO_UUID_NBT_KEY));
        } else {
            return Optional.empty();
        }
    }

    public static void setAttunedToUUID(ItemStack stack, UUID attunedToUUID) {
        setArtifactoryUUID(stack, ATTUNED_TO_UUID_NBT_KEY, attunedToUUID);
    }

    public static Optional<String> getAttunedToName(ItemStack stack) {
        if(stackArtifactoryTagContainsTag(stack, ATTUNED_TO_NAME_NBT_KEY)){
            return Optional.of(getArtifactoryString(stack, ATTUNED_TO_NAME_NBT_KEY));
        } else {
            return Optional.empty();
        }
    }

    public static void setAttunedToName(ItemStack stack, String name) {
        setArtifactoryString(stack, ATTUNED_TO_NAME_NBT_KEY, name);
    }

    // Artifactory String Methods
    private static String getArtifactoryString(ItemStack stack, String tag) {
        return getOrCreateArtifactoryCompoundTag(stack).getString(tag);
    }

    private static void setArtifactoryString(ItemStack stack, String tag, String value) {
        CompoundTag artifactoryCT = getOrCreateArtifactoryCompoundTag(stack);
        artifactoryCT.putString(tag, value);
    }

    private static boolean getArtifactoryBoolean(ItemStack stack, String tag) {
        return getOrCreateArtifactoryCompoundTag(stack).getBoolean(tag);
    }

    private static void setArtifactoryBoolean(ItemStack stack, String tag, boolean value) {
        CompoundTag artifactoryCT = getOrCreateArtifactoryCompoundTag(stack);
        artifactoryCT.putBoolean(tag, value);
    }

    // Artifactory UUID Methods
    private static CompoundTag getOrCreateArtifactoryCompoundTag(ItemStack stack) {
        if(stackContainsTag(stack, Artifactory.MOD_ID)) return stack.getOrCreateTag().getCompound(Artifactory.MOD_ID);

        CompoundTag artifactoryTag = new CompoundTag();

        replaceArtifactoryTag(stack, artifactoryTag);
        return artifactoryTag;
    }

    private static UUID getArtifactoryUUID(ItemStack stack, String tag) {
        return getOrCreateArtifactoryCompoundTag(stack).getUUID(tag);
    }

    private static void setArtifactoryUUID(ItemStack stack, String tag, UUID value) {
        CompoundTag artifactoryCT = getOrCreateArtifactoryCompoundTag(stack);
        artifactoryCT.putUUID(tag, value);
    }

    // Artifactory Tag Methods
    private static boolean stackArtifactoryTagContainsTag(ItemStack stack, String tag) {
        if(!stackContainsTag(stack, Artifactory.MOD_ID)) return false;
        return getOrCreateArtifactoryCompoundTag(stack).contains(tag);
    }

    private static void replaceArtifactoryTag(ItemStack stack, CompoundTag artifactoryTag) {
        stack.getOrCreateTag().put(Artifactory.MOD_ID, artifactoryTag);
    }

    public static void removeArtifactoryTag(ItemStack stack) {
        if(artifactoryTagExists(stack)) stack.getOrCreateTag().remove(Artifactory.MOD_ID);
    }

    public static boolean artifactoryTagExists(ItemStack stack) {
        return stackContainsTag(stack, Artifactory.MOD_ID);
    }

    // Helper Methods
    private static boolean stackContainsTag(ItemStack stack, String tag) {
        return !stack.isEmpty() && stack.hasTag() && stack.getOrCreateTag().contains(tag);
    }

}
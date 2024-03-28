package net.silvertide.artifactory.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;

import java.util.UUID;

public class NBTUtil {
    public static final String ITEM_ATTUNEMENT_UUID_NBT_KEY = "attunement_id";
    public static final String ATTUNED_TO_UUID_NBT_KEY = "attuned_to_uuid";
    public static final String ATTUNED_TO_NAME_NBT_KEY = "attuned_to_name";

    public static UUID getOrCreateItemAttunementUUID(ItemStack stack) {
        // Get the existing tag if it's there
        if(hasArtifactoryTag(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY)) return getArtifactoryUUID(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY);

        // Otherwise generate a new one
        UUID newItemUUID = UUID.randomUUID();
        setArtifactoryUUID(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY, newItemUUID);
        return newItemUUID;
    }

    // SET BASIC TAGS
    public static void setBoolean(ItemStack stack, String tag, boolean value) {
        stack.getOrCreateTag().putBoolean(tag, value);
    }

    public static void setString(ItemStack stack, String tag, String value) {
        stack.getOrCreateTag().putString(tag, value);
    }

    public static void setUUID(ItemStack stack, String tag, UUID value) {
        stack.getOrCreateTag().putUUID(tag, value);
    }

    public static void getUUID(ItemStack stack, String tag, UUID value) {
        stack.getOrCreateTag().putUUID(tag, value);
    }

    // GET BASIC TAGS
    public static boolean getBoolean(ItemStack stack, String tag, boolean defaultValue) {
        return verifyTagExists(stack, tag) ? stack.getOrCreateTag().getBoolean(tag) : defaultValue;
    }

    // SET ARTIFACTORY COMPOUNDTAG TAGS
    public static void setArtifactoryString(ItemStack stack, String tag, String value) {
        CompoundTag artifactoryCT = getOrCreateArtifactoryCompoundTag(stack);
        artifactoryCT.putString(tag, value);
    }

    public static void setArtifactoryUUID(ItemStack stack, String tag, UUID value) {
        CompoundTag artifactoryCT = getOrCreateArtifactoryCompoundTag(stack);
        artifactoryCT.putUUID(tag, value);
    }

    // GET ARTIFACTORY COMPOUNDTAG TAGS
    public static String getArtifactoryString(ItemStack stack, String tag, String defaultValue) {
        if(!stackContainsTag(stack, Artifactory.MOD_ID)) return defaultValue;

        CompoundTag artifactoryCT = getOrCreateArtifactoryCompoundTag(stack);
        return artifactoryCT.contains(tag) ? artifactoryCT.getString(tag) : defaultValue;
    }

    public static UUID getArtifactoryUUID(ItemStack stack, String tag) {
        return getOrCreateArtifactoryCompoundTag(stack).getUUID(tag);
    }

    public static boolean hasArtifactoryTag(ItemStack stack, String tag) {
        if(!stackContainsTag(stack, Artifactory.MOD_ID)) return false;

        CompoundTag artifactoryCT = getOrCreateArtifactoryCompoundTag(stack);
        return artifactoryCT.contains(tag);
    }

    // HELPERS
    public static boolean stackContainsTag(ItemStack stack, String tag) {
        return !stack.isEmpty() && stack.hasTag() && stack.getOrCreateTag().contains(tag);
    }
    private static CompoundTag getOrCreateArtifactoryCompoundTag(ItemStack stack) {
        if(stackContainsTag(stack, Artifactory.MOD_ID)) return stack.getOrCreateTag().getCompound(Artifactory.MOD_ID);

        CompoundTag artifactoryCT = new CompoundTag();
        stack.getOrCreateTag().put(Artifactory.MOD_ID, artifactoryCT);
        return artifactoryCT;
    }
    private static boolean verifyTagExists(ItemStack stack, String tag) {
        return !stack.isEmpty() && stack.hasTag() && stack.getOrCreateTag().contains(tag);
    }
}
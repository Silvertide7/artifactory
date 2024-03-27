package net.silvertide.artifactory.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;

public class NBTUtil {



    // SET BASIC TAGS
    public static void setBoolean(ItemStack stack, String tag, boolean value) {
        stack.getOrCreateTag().putBoolean(tag, value);
    }

    public static void setString(ItemStack stack, String tag, String value) {
        stack.getOrCreateTag().putString(tag, value);
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

    // GET ARTIFACTORY COMPOUNDTAG TAGS
    public static String getArtifactoryString(ItemStack stack, String tag, String defaultValue) {
        if(!stackContainsTag(stack, Artifactory.MOD_ID)) return defaultValue;

        CompoundTag artifactoryCT = getOrCreateArtifactoryCompoundTag(stack);
        return artifactoryCT.contains(tag) ? artifactoryCT.getString(tag) : defaultValue;
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
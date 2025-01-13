package net.silvertide.artifactory.util;

public final class StackNBTUtil {

//    public static void setupStackToAttune(ItemStack stack) {
//        if (StackNBTUtil.artifactoryNBTExists(stack)) StackNBTUtil.removeArtifactoryNBT(stack);
//        StackNBTUtil.setItemAttunementUUID(stack, UUID.randomUUID());
//    }

    // BASIC TAG FUNCTIONS
//    public static void setBoolean(ItemStack stack, String key, boolean value) {
//        stack.getOrCreateTag().putBoolean(key, value);
//    }

//    public static boolean getBoolean(ItemStack stack, String key) {
//        return stack.getOrCreateTag().getBoolean(key);
//    }

    // ATTUNEMENT TAG FUNCTIONS
//    public static void putPlayerDataInArtifactoryTag(Player player, ItemStack stack) {
//        setAttunedToUUID(stack, player.getUUID());
//        setAttunedToName(stack, player.getDisplayName().getString());
//    }
//
//    public static void setItemAttunementUUID(ItemStack stack, UUID attunementUUID) {
//        setArtifactoryUUID(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY, attunementUUID);
//    }

//    public static void setSoulbound(ItemStack stack) {
//        setArtifactoryBoolean(stack, MODIFICATION_SOULBOUND_NBT_KEY, true);
//    }

//    public static boolean isSoulbound(ItemStack stack){
//        return artifactoryNBTContainsTag(stack, MODIFICATION_SOULBOUND_NBT_KEY) && getArtifactoryBoolean(stack, MODIFICATION_SOULBOUND_NBT_KEY);
//    }

//    public static void removeSoulbound(ItemStack stack) {
//        removeArtifactoryTag(stack, MODIFICATION_SOULBOUND_NBT_KEY);
//    }

//    public static void setInvulnerable(ItemStack stack) {
//        setArtifactoryBoolean(stack, MODIFICATION_INVULNERABLE_NBT_KEY, true);
//    }

//    public static void removeInvulnerable(ItemStack stack) {
//        removeArtifactoryTag(stack, MODIFICATION_INVULNERABLE_NBT_KEY);
//    }

//    public static boolean isInvulnerable(ItemStack stack){
//        return artifactoryNBTContainsTag(stack, MODIFICATION_INVULNERABLE_NBT_KEY) && getArtifactoryBoolean(stack, MODIFICATION_INVULNERABLE_NBT_KEY);
//    }

//    public static void makeUnbreakable(ItemStack stack) {
//        stack.setDamageValue(0);
//        setBoolean(stack, "Unbreakable", true);
//        setArtifactoryBoolean(stack, MODIFICATION_UNBREAKABLE_NBT_KEY, true);
//    }

//    public static boolean isUnbreakable(ItemStack stack) {
//        return stackContainsTag(stack, "Unbreakable") && getBoolean(stack, "Unbreakable");
//    }

//    public static boolean isUnbreakableFromArtifactory(ItemStack stack){
//        return artifactoryNBTContainsTag(stack, MODIFICATION_UNBREAKABLE_NBT_KEY) && getArtifactoryBoolean(stack, MODIFICATION_UNBREAKABLE_NBT_KEY);
//    }
//    public static void removeUnbreakable(ItemStack stack) {
//        CompoundTag stackNBT = stack.getOrCreateTag();
//        if(stackNBT.contains("Unbreakable")) stackNBT.remove("Unbreakable");
//    }
//
//    public static void removeArtifactoryUnbreakable(ItemStack stack) {
//        removeArtifactoryTag(stack, MODIFICATION_UNBREAKABLE_NBT_KEY);
//    }
//
//    public static Optional<String> getDisplayNameFromNBT(ItemStack stack) {
//        CompoundTag stackNBT = stack.getOrCreateTag();
//        if(stackNBT.contains("display", Tag.TAG_COMPOUND)) {
//            CompoundTag stackDisplayNBT = stackNBT.getCompound("display");
//            if(stackDisplayNBT.contains("Name", Tag.TAG_STRING)) {
//                Component nameComponent = Component.Serializer.fromJson(stackDisplayNBT.getString("Name"));
//                if(nameComponent != null) {
//                    String name = nameComponent.getString();
//                    if(!"".equals(name)) return Optional.of(name);
//                }
//            }
//        }
//        return Optional.empty();
//    }

//    public static Optional<UUID> getItemAttunementUUID(ItemStack stack) {
//        if(artifactoryNBTContainsTag(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY)) {
//            return Optional.of(getArtifactoryUUID(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY));
//        } else {
//            return Optional.empty();
//        }
//    }

//    public static boolean containsAttunedToUUID(ItemStack stack) {
//        return artifactoryNBTContainsTag(stack, ATTUNED_TO_UUID_NBT_KEY);
//    }

//    public static boolean containsItemAttunementUUID(ItemStack stack) {
//        return artifactoryNBTContainsTag(stack, ITEM_ATTUNEMENT_UUID_NBT_KEY);
//    }
//
//    public static Optional<UUID> getAttunedToUUID(ItemStack stack) {
//        if(containsAttunedToUUID(stack)) {
//            return Optional.of(getArtifactoryUUID(stack, ATTUNED_TO_UUID_NBT_KEY));
//        } else {
//            return Optional.empty();
//        }
//    }
//
//    public static void setAttunedToUUID(ItemStack stack, UUID attunedToUUID) {
//        setArtifactoryUUID(stack, ATTUNED_TO_UUID_NBT_KEY, attunedToUUID);
//    }

//    public static Optional<String> getAttunedToName(ItemStack stack) {
//        if(artifactoryNBTContainsTag(stack, ATTUNED_TO_NAME_NBT_KEY)){
//            return Optional.of(getArtifactoryString(stack, ATTUNED_TO_NAME_NBT_KEY));
//        } else {
//            return Optional.empty();
//        }
//    }

//    public static void setAttunedToName(ItemStack stack, String name) {
//        setArtifactoryString(stack, ATTUNED_TO_NAME_NBT_KEY, name);
//    }


    // Artifactory Tag Methods
//    private static String getArtifactoryString(ItemStack stack, String tag) {
//        return getOrCreateArtifactoryNBT(stack).getString(tag);
//    }

//    private static void setArtifactoryString(ItemStack stack, String tag, String value) {
//        CompoundTag artifactoryNBT = getOrCreateArtifactoryNBT(stack);
//        artifactoryNBT.putString(tag, value);
//    }

//    private static boolean getArtifactoryBoolean(ItemStack stack, String tag) {
//        return getOrCreateArtifactoryNBT(stack).getBoolean(tag);
//    }
//
//    private static void setArtifactoryBoolean(ItemStack stack, String tag, boolean value) {
//        CompoundTag artifactoryNBT = getOrCreateArtifactoryNBT(stack);
//        artifactoryNBT.putBoolean(tag, value);
//    }

//    private static UUID getArtifactoryUUID(ItemStack stack, String tag) {
//        return getOrCreateArtifactoryNBT(stack).getUUID(tag);
//    }
//
//    private static void setArtifactoryUUID(ItemStack stack, String tag, UUID value) {
//        CompoundTag artifactoryNBT = getOrCreateArtifactoryNBT(stack);
//        artifactoryNBT.putUUID(tag, value);
//    }
    
//    private static void removeArtifactoryTag(ItemStack stack, String tag) {
//        CompoundTag artifactoryNBT = getOrCreateArtifactoryNBT(stack);
//        if(artifactoryNBT.contains(tag)) artifactoryNBT.remove(tag);
//    }

    // Modification NBT Methods
//    public static void addAttributeModificaftionTag(ItemStack stack, UUID attributeUUID, CompoundTag attributeModificationTag) {
//        CompoundTag attributeModificationsCompoundTag = getOrCreateAttributeModificationNBT(stack);
//        attributeModificationsCompoundTag.put(attributeUUID.toString(), attributeModificationTag);
//    }

//    public static CompoundTag getOrCreateAttributeModificationNBT(ItemStack stack) {
//        CompoundTag artifactoryNBT = getOrCreateArtifactoryNBT(stack);
//        if(!artifactoryNBT.contains(ATTRIBUTE_MODIFICATION_NBT_KEY)) {
//            artifactoryNBT.put(ATTRIBUTE_MODIFICATION_NBT_KEY, new CompoundTag());
//        }
//        return artifactoryNBT.getCompound(ATTRIBUTE_MODIFICATION_NBT_KEY);
//    }

//    public static void removeAttributeModifications(ItemStack stack) {
//        removeArtifactoryTag(stack, ATTRIBUTE_MODIFICATION_NBT_KEY);
//    }


    // Artifactory NBT Methods
//    public static CompoundTag getOrCreateArtifactoryNBT(ItemStack stack) {
//        if(stackContainsTag(stack, Artifactory.MOD_ID)) return stack.getOrCreateTag().getCompound(Artifactory.MOD_ID);
//
//        CompoundTag artifactoryTag = new CompoundTag();
//
//        replaceArtifactoryNBT(stack, artifactoryTag);
//        return artifactoryTag;
//    }
//    private static boolean artifactoryNBTContainsTag(ItemStack stack, String tag) {
//        if(!stackContainsTag(stack, Artifactory.MOD_ID)) return false;
//        return getOrCreateArtifactoryNBT(stack).contains(tag);
//    }

//    private static void replaceArtifactoryNBT(ItemStack stack, CompoundTag artifactoryTag) {
//        stack.getOrCreateTag().put(Artifactory.MOD_ID, artifactoryTag);
//    }

//    public static void removeArtifactoryNBT(ItemStack stack) {
//        if(artifactoryNBTExists(stack)) stack.getOrCreateTag().remove(Artifactory.MOD_ID);
//    }

//    public static boolean artifactoryNBTExists(ItemStack stack) {
//        return stackContainsTag(stack, Artifactory.MOD_ID);
//    }



    // Helper Methods ---------
//    private static boolean stackContainsTag(ItemStack stack, String tag) {
//        return !stack.isEmpty() && stack.hasTag() && stack.getOrCreateTag().contains(tag, Tag.TAG_COMPOUND);
//    }

//    public static boolean containsAttributeModifications(ItemStack stack) {
//        return artifactoryNBTExists(stack) && getOrCreateArtifactoryNBT(stack).contains(ATTRIBUTE_MODIFICATION_NBT_KEY);
//    }

//    public static boolean attemptToAddToExistingAttributeUUID(ItemStack stack, AttributeModification attributeModification) {
//        if(!containsAttributeModifications(stack)) return false;
//
//        CompoundTag attributeModificationsTag = getOrCreateAttributeModificationNBT(stack);
//        for(String attributeKey : attributeModificationsTag.getAllKeys()) {
//            CompoundTag attributeNBT = attributeModificationsTag.getCompound(attributeKey);
//            if(!attributeNBT.isEmpty()) {
//                boolean sameAttribute = attributeNBT.getString(AttributeModification.ATTIBUTE_KEY).equals(attributeModification.getAttribute());
//                boolean sameOperation = attributeNBT.contains(AttributeModification.OPERATION_KEY) && attributeNBT.getInt(AttributeModification.OPERATION_KEY) == attributeModification.getOperation();
//                boolean sameSlot = attributeNBT.getString(AttributeModification.EQUIPMENT_SLOT_KEY).equals(attributeModification.getSlotName());
//                if(sameAttribute && sameOperation && sameSlot) {
//                    double currentValue = attributeNBT.getDouble(AttributeModification.VALUE_KEY);
//                    attributeNBT.putDouble(AttributeModification.VALUE_KEY, currentValue + attributeModification.getValue());
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
}
package net.silvertide.artifactory.client.state;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.ResourceLocationUtil;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class ItemRequirements {
    List<ItemRequirement> requirements = new ArrayList<>();
    public void addRequirements(List<String> rawRequirements) {
        List<String> itemRequirements = new ArrayList<>(rawRequirements);
        // There are only 3 available slots for items so this will only take
        // the first three in the list.
        for(int i = 0; i < Math.min(itemRequirements.size(), 3); i++) {
            Pair<String, Integer> parsedResults = parseItemRequirementInformation(itemRequirements.get(i));
            if(parsedResults != null) {
                requirements.add(new ItemRequirement(parsedResults.getA(), parsedResults.getB()));
            } else {
                Artifactory.LOGGER.warn("Artifactory - ItemRequirement not valid, invalid item - " + itemRequirements.get(i));
            }
        }
    }

    public static Pair<String, Integer> parseItemRequirementInformation(String itemRequirementString) {
        String pathResult = itemRequirementString;
        int quantity = 1;

        // If the requirements has a custom quantity attached to it. modid:item_name#quantity
        if(pathResult.contains("#")) {
            String[] itemParts = pathResult.split("#");
            if(itemParts.length > 2) {
                return null;
            }

            pathResult = itemParts[0];
            quantity = Integer.parseInt(itemParts[1]);
        }

        ItemStack stack = ResourceLocationUtil.getItemStackFromResourceLocation(pathResult);
        if(stack != null && !stack.isEmpty()) {
            if (quantity <= 0) return null;

            if (quantity > stack.getMaxStackSize()) {
                quantity = stack.getMaxStackSize();
            }
        } else {
            return null;
        }

        return new Pair<>(pathResult, quantity);
    }

    public void addRequirement(ItemRequirement itemRequirement) {
        this.requirements.add(itemRequirement);
    }

    public String getItemResourceLocation(int index) {
        if(index >= requirements.size()) return "";
        return requirements.get(index).description;
    }

    public int getItemQuantity(int index) {
        if(index >= requirements.size()) return 0;
        return requirements.get(index).quantity;
    }

    public static void encode(FriendlyByteBuf buf, ItemRequirements itemRequirements) {
        buf.writeUtf(itemRequirements.getItemResourceLocation(0));
        buf.writeInt(itemRequirements.getItemQuantity(0));
        buf.writeUtf(itemRequirements.getItemResourceLocation(1));
        buf.writeInt(itemRequirements.getItemQuantity(1));
        buf.writeUtf(itemRequirements.getItemResourceLocation(2));
        buf.writeInt(itemRequirements.getItemQuantity(2));
    }

    public static ItemRequirements decode(FriendlyByteBuf buf) {
        ItemRequirements itemRequirements = new ItemRequirements();
        itemRequirements.addRequirement(new ItemRequirement(buf.readUtf(), buf.readInt()));
        itemRequirements.addRequirement(new ItemRequirement(buf.readUtf(), buf.readInt()));
        itemRequirements.addRequirement(new ItemRequirement(buf.readUtf(), buf.readInt()));
        return itemRequirements;
    }

    public record ItemRequirement(String description, int quantity){};
}

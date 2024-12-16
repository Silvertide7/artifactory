package net.silvertide.artifactory.client.util;

import net.minecraft.client.Minecraft;
import net.silvertide.artifactory.client.state.ClientAttunedItems;
import net.silvertide.artifactory.client.state.ClientAttunementNexusSlotInformation;
import net.silvertide.artifactory.gui.AttunementScreen;
import net.silvertide.artifactory.gui.ManageAttunementsScreen;

import java.util.UUID;

public class ClientUtil {
    private ClientUtil() {}

    public static void openManageScreen(int numUniquesAllowed) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof AttunementScreen) {
            ClientAttunementNexusSlotInformation.clearSlotInformation();
            minecraft.pushGuiLayer(new ManageAttunementsScreen(numUniquesAllowed));
        } else if(!(minecraft.screen instanceof ManageAttunementsScreen)) {
            minecraft.setScreen(new ManageAttunementsScreen(numUniquesAllowed));
        }
    }

    public static void removeAttunedItem(UUID itemUUIDToRemove) {
        ClientAttunedItems.removeAttunedItem(itemUUIDToRemove);

        // Update screen of minecraft player if open
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.screen instanceof ManageAttunementsScreen manageAttunementsScreen) {
            manageAttunementsScreen.createAttunementCards();
        }
    }
}

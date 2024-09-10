package net.silvertide.artifactory.client.utils;

import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;

public class ClientAttunementNexusSlotInformation {
    private ClientAttunementNexusSlotInformation(){};
    private static AttunementNexusSlotInformation slotInformation = null;
    public static void setSlotInformation(AttunementNexusSlotInformation attunedNexusSlotInformation) {
        slotInformation = attunedNexusSlotInformation;
        Artifactory.LOGGER.info("Client side slot information updated! \n" + slotInformation.toString());
    }

    public static AttunementNexusSlotInformation getSlotInformation() {
        return slotInformation;
    }

    public static void clearSlotInformation() { slotInformation = null; }
}

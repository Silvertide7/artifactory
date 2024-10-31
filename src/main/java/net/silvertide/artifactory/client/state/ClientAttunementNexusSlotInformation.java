package net.silvertide.artifactory.client.state;

import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;

import java.util.ArrayList;
import java.util.List;

public class ClientAttunementNexusSlotInformation {
    private static AttunementNexusSlotInformation slotInformation = null;
    private static List<ClientSlotInformationListener> listeners = new ArrayList<>();
    private ClientAttunementNexusSlotInformation() {};

    public static void setSlotInformation(AttunementNexusSlotInformation attunedNexusSlotInformation) {
        slotInformation = attunedNexusSlotInformation;
        notifyListeners();
        Artifactory.LOGGER.info("Client side slot information updated! \n" + slotInformation.toString());
    }

    public static AttunementNexusSlotInformation getSlotInformation() {
        return slotInformation;
    }

    public static void clearSlotInformation() { slotInformation = null; }

    // Listener Implementation
    public static void registerListener(ClientSlotInformationListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(ClientSlotInformationListener listener) {
        listeners.remove(listener);
    }
    private static void notifyListeners() {
        for(ClientSlotInformationListener listener : listeners) {
            listener.onSlotInformationUpdated(slotInformation);
        }
    }
    public interface ClientSlotInformationListener {
        void onSlotInformationUpdated(AttunementNexusSlotInformation newSlotInformation);
    }
}

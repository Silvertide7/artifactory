package net.silvertide.artifactory.client.state;

import net.silvertide.artifactory.config.ServerConfigs;

public class ClientSyncedConfig {
    private ClientSyncedConfig() {}
    private static int xpAttuneThreshold = 30;
    private static int xpAttuneConsumed = 20;

    public static void updateConfigs(int threshold, int consumed) {
        xpAttuneThreshold = threshold;
        xpAttuneConsumed = consumed;
    }

    public static int getXpAttuneThreshold(){ return xpAttuneThreshold;}
    public static int getXpAttuneConsumed(){ return xpAttuneConsumed;}
}

package net.silvertide.artifactory.client.state;

public class ClientSyncedConfig {
    private ClientSyncedConfig() {}
    private static final int DEFAULT_XP_ATTUNE_THRESHOLD = 30;
    private static final int DEFAULT_XP_ATTUNE_CONSUMED = 20;
    private static int xpAttuneThreshold = DEFAULT_XP_ATTUNE_THRESHOLD;
    private static int xpAttuneConsumed = DEFAULT_XP_ATTUNE_CONSUMED;

    public static void updateConfigs(int threshold, int consumed) {
        xpAttuneThreshold = threshold;
        xpAttuneConsumed = consumed;
    }

    public static void reset() {
        xpAttuneThreshold = DEFAULT_XP_ATTUNE_THRESHOLD;
        xpAttuneConsumed = DEFAULT_XP_ATTUNE_CONSUMED;
    }

    public static int getXpAttuneThreshold(){ return xpAttuneThreshold;}
    public static int getXpAttuneConsumed(){ return xpAttuneConsumed;}
}

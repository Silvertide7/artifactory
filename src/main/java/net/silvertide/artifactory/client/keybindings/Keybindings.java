package net.silvertide.artifactory.client.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.silvertide.artifactory.Artifactory;

public class Keybindings {
    public static final Keybindings INSTANCE = new Keybindings();
    private Keybindings() {}

    private static final String CATEGORY = "key.categories." + Artifactory.MOD_ID;

    public final KeyMapping useOpenManageAttunementsKey = new KeyMapping(
            "key." + Artifactory.MOD_ID + ".useOpenManageAttunementsKey",
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_O, -1),
            CATEGORY
    );

}

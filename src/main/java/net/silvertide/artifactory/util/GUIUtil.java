package net.silvertide.artifactory.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GUIUtil {
    private GUIUtil() {}
    public static boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return isHovering(0, 0, pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }
    public static boolean isHovering(int i, int j, int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= (double)(pX - 1) && pMouseX < (double)(pX + pWidth + 1) && pMouseY >= (double)(pY - 1) && pMouseY < (double)(pY + pHeight + 1);
    }

    public static void drawScaledWordWrap(GuiGraphics guiGraphics, float textScale, Font font, Component buttonTextComp, int textX, int textY, int lineWidth, int color) {
        if("".equals(buttonTextComp.getString())) return;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, textScale);

        int scaledTextX = (int) (textX / textScale);
        int scaledTextY = (int) (textY / textScale);
        guiGraphics.drawWordWrap(font, buttonTextComp, scaledTextX, scaledTextY, (int) (lineWidth / textScale), color);

        guiGraphics.pose().popPose();
    }

    public static void drawCenteredWordWrap(GuiGraphics guiGraphics, Font font, Component buttonTextComp, int textX, int textY, int lineWidth, int color) {
        int fontWidth = font.width(buttonTextComp);
        int fontHeight = font.lineHeight;
        guiGraphics.drawWordWrap(font, buttonTextComp, textX - fontWidth / 2, textY - fontHeight / 2, lineWidth, color);
    }

    public static void drawScaledCenteredWordWrap(GuiGraphics guiGraphics, float textScale, Font font, Component buttonTextComp, int textX, int textY, int lineWidth, int color) {
        if("".equals(buttonTextComp.getString())) return;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, textScale);

        float fontWidth = font.width(buttonTextComp) * textScale;
        float fontHeight = font.lineHeight * textScale;

        float xOffset = fontWidth < lineWidth ? fontWidth : lineWidth;

        int scaledTextX = (int) ((textX - xOffset / 2) / textScale);
        int scaledTextY = (int) ((textY - fontHeight / 3) / textScale);
        guiGraphics.drawWordWrap(font, buttonTextComp, scaledTextX, scaledTextY, (int) (lineWidth / textScale), color);

        guiGraphics.pose().popPose();
    }

    public static String prettifyName(String resourceLocation) {
        String prettifiedLocation = resourceLocation;
        // Split the path by modid:moditem
        if(prettifiedLocation.contains(":")) {
            prettifiedLocation = resourceLocation.split(":")[1];
        }

        // Added this to deal with attributes like general.movement_speed
        if(prettifiedLocation.contains(".")) {
            String[] periodSeperated = prettifiedLocation.split("\\.");
            prettifiedLocation = periodSeperated[periodSeperated.length - 1];
        }

        // Separate into multiple words
        if(prettifiedLocation.contains("_")) {
            StringBuilder result = new StringBuilder();
            String[] words = prettifiedLocation.split("_");
            for (int i = 0; i < words.length; i++) {
                // Capitalize the first letter and make the rest lowercase
                String capitalizedWord = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
                // Append the capitalized word to the result
                result.append(capitalizedWord);
                // Add a space if this is not the last word
                if (i < words.length - 1) {
                    result.append(" ");
                }
            }
            prettifiedLocation = result.toString();
        } else {
            prettifiedLocation = prettifiedLocation.substring(0,1).toUpperCase() + prettifiedLocation.substring(1).toLowerCase();
        }

        return prettifiedLocation;
    }
}

package net.silvertide.artifactory.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class GUIUtil {
    private GUIUtil() {}
    public static boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return isHovering(0, 0, pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }
    public static boolean isHovering(int i, int j, int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= (double)(pX - 1) && pMouseX < (double)(pX + pWidth + 1) && pMouseY >= (double)(pY - 1) && pMouseY < (double)(pY + pHeight + 1);
    }

    public static void drawScaledString(GuiGraphics guiGraphics, float textScale, Font font, String text, int textX, int textY, int color) {
        if(text == null || textScale == 0.0F || text.isEmpty()) return;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, textScale);

        int scaledTextX = (int) (textX / textScale);
        int scaledTextY = (int) (textY / textScale);
        guiGraphics.drawString(font, text, scaledTextX, scaledTextY, color);

        guiGraphics.pose().popPose();
    }

    public static String trimTextToWidth(String text, Font font, int width) {
        String result = text;
        while(font.width(result) > width) {
            float ratio = (float) width / font.width(result);
            int stringLength = (int) (result.length() * ratio);
            if(stringLength <= 3) {
                return "...";
            }
            result = result.substring(0, stringLength - 3);
            result += "...";
        }
        return result;
    }

    public static void drawScaledWordWrap(GuiGraphics guiGraphics, float textScale, Font font, Component buttonTextComp, int textX, int textY, int lineWidth, int color) {
        if(buttonTextComp.getString().isEmpty()) return;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, textScale);

        int scaledTextX = (int) (textX / textScale);
        int scaledTextY = (int) (textY / textScale);
        guiGraphics.drawWordWrap(font, buttonTextComp, scaledTextX, scaledTextY, (int) (lineWidth / textScale), color);

        guiGraphics.pose().popPose();
    }

    public static void drawScaledCenteredWordWrap(GuiGraphics guiGraphics, float textScale, Font font, Component buttonTextComp, int textX, int textY, int lineWidth, int color) {
        if(buttonTextComp.getString().isEmpty()) return;

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

    public static String convertToPercentage(double percent) {
        return Math.round(percent * 100D) + "%";
    }
}

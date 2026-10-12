package net.silvertide.artifactory.component.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class UnreadableAttunementCondition implements AttunementCondition {
    public static final String TYPE = "artifactory:unreadable";
    public static final UnreadableAttunementCondition INSTANCE = new UnreadableAttunementCondition();
    public static final MapCodec<UnreadableAttunementCondition> MAP_CODEC = MapCodec.unit(INSTANCE);

    private UnreadableAttunementCondition() {}

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public boolean isMetBy(ItemStack stack) {
        return false;
    }

    @Override
    public Component failureDescription() {
        return Component.translatable("artifactory.condition.unreadable");
    }
}

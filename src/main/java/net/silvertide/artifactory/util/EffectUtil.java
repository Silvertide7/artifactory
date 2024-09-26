package net.silvertide.artifactory.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class EffectUtil {

    private EffectUtil() {}

    public static List<MobEffectInstance> getMobEffectInstancesFromConfig(String configEffectString) {
        String[] serializedEffects = configEffectString.split(";");

        List<MobEffectInstance> mobEffects = new ArrayList<>();

        for (String serializedEffect : serializedEffects) {
            String[] effectComponents = serializedEffect.split("/");
            if(effectComponents.length == 2) {
                String effectName = effectComponents[0];
                int effectLevel = Integer.parseInt(effectComponents[1]);

                MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(effectName));
                if(effect != null && effectLevel > 0) {
                    mobEffects.add(new MobEffectInstance(effect, 20, effectLevel-1, false, false));
                }
            }
        }

        return mobEffects;
    }

    public static void applyMobEffectInstancesToPlayer(Player player, String serializedMobEffects) {
        List<MobEffectInstance> mobEffectInstances = getMobEffectInstancesFromConfig(serializedMobEffects);
        for(MobEffectInstance mobEffectInstance : mobEffectInstances) {
            if (!player.hasEffect(mobEffectInstance.getEffect()) || player.getEffect(mobEffectInstance.getEffect()).getDuration() < 18) {
                player.addEffect(mobEffectInstance);
            }
        }
    }
}

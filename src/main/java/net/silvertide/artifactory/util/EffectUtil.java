package net.silvertide.artifactory.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import net.silvertide.artifactory.Artifactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EffectUtil {
    private static final int EFFECT_DURATION_TICKS = 20;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 18;
    private static final Map<String, List<ParsedEffect>> PARSED_EFFECT_CACHE = new HashMap<>();

    private EffectUtil() {}

    private record ParsedEffect(Holder<MobEffect> effect, int amplifier) {}

    private static List<ParsedEffect> getParsedEffects(String configEffectString) {
        return PARSED_EFFECT_CACHE.computeIfAbsent(configEffectString, EffectUtil::parseEffects);
    }

    private static List<ParsedEffect> parseEffects(String configEffectString) {
        List<ParsedEffect> parsedEffects = new ArrayList<>();
        for (String serializedEffect : configEffectString.split(";")) {
            String[] effectComponents = serializedEffect.split("/");
            if(effectComponents.length != 2) {
                Artifactory.LOGGER.warn("Artifactory - Malformed effect config entry: {}", serializedEffect);
                continue;
            }

            int effectLevel;
            try {
                effectLevel = Integer.parseInt(effectComponents[1]);
            } catch (NumberFormatException e) {
                Artifactory.LOGGER.warn("Artifactory - Invalid effect level in config: {}", serializedEffect);
                continue;
            }

            ResourceLocation effectId = ResourceLocation.tryParse(effectComponents[0]);
            if(effectId == null) {
                Artifactory.LOGGER.warn("Artifactory - Invalid effect id in config: {}", serializedEffect);
                continue;
            }

            BuiltInRegistries.MOB_EFFECT.getHolder(effectId).ifPresentOrElse(
                    effect -> parsedEffects.add(new ParsedEffect(effect, effectLevel - 1)),
                    () -> Artifactory.LOGGER.warn("Artifactory - Unknown effect id in config: {}", serializedEffect));
        }
        return parsedEffects;
    }

    public static void applyMobEffectInstancesToPlayer(Player player, String serializedMobEffects) {
        for(ParsedEffect parsedEffect : getParsedEffects(serializedMobEffects)) {
            MobEffectInstance instanceOnPlayer = player.getEffect(parsedEffect.effect());
            if (!player.hasEffect(parsedEffect.effect()) || (instanceOnPlayer != null && instanceOnPlayer.getDuration() < EFFECT_REFRESH_THRESHOLD_TICKS)) {
                player.addEffect(new MobEffectInstance(parsedEffect.effect(), EFFECT_DURATION_TICKS, parsedEffect.amplifier(), false, false));
            }
        }
    }
}

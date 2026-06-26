package net.silvertide.artifactory.config.codecs;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AttunableItems {
    public static final MergeableCodecDataManager DATA_LOADER = new MergeableCodecDataManager(
            "artifactory",
            AttunementDataSource.CODEC,
            AttunableItems::processItemAttunements);

    private static volatile Map<ResourceLocation, AttunementDataSource> activeData = Map.of();

    public static void setActiveData(Map<ResourceLocation, AttunementDataSource> data) {
        activeData = Map.copyOf(data);
    }

    public static Map<ResourceLocation, AttunementDataSource> getActiveData() {
        return activeData;
    }

    public static Optional<AttunementDataSource> getActiveData(ResourceLocation resourceLocation) {
        return Optional.ofNullable(activeData.get(resourceLocation));
    }

    public static AttunementDataSource processItemAttunements(final List<AttunementDataSource> raws) {
        // Simple merger function. Takes the latest AttunementDataSource with replace = true;
        AttunementDataSource result = raws.getFirst();
        if(raws.size() > 1) {
            for(int i = 1; i < raws.size(); i++) {
                AttunementDataSource curr = raws.get(i);
                if(curr.replace()) result = curr;
            }
        }
        return result;
    }
}

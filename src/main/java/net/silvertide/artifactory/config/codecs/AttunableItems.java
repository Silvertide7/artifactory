package net.silvertide.artifactory.config.codecs;

import java.util.List;

public class AttunableItems {
    public static final MergeableCodecDataManager DATA_LOADER = new MergeableCodecDataManager(
            "artifactory",
            ItemAttunementData.CODEC,
            AttunableItems::processItemAttunements);

    public static ItemAttunementData processItemAttunements(final List<ItemAttunementData> raws) {
        // Simple merger function. Takes the latest ItemAttunementData with replace = true;
        ItemAttunementData result = raws.get(0);
        if(raws.size() > 1) {
            for(int i = 1; i < raws.size(); i++) {
                ItemAttunementData curr = raws.get(i);
                if(curr.replace()) result = curr;
            }
        }
        return result;
    }
}

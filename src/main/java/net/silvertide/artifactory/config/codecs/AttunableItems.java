package net.silvertide.artifactory.config.codecs;

import java.util.List;

public class AttunableItems {
    public static final MergeableCodecDataManager DATA_LOADER = new MergeableCodecDataManager(
            "artifactory",
            AttunementDataSource.CODEC,
            AttunableItems::processItemAttunements);

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

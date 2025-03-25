package net.silvertide.artifactory.storage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.AttunementData;
import net.silvertide.artifactory.component.AttunementDataSource;
import net.silvertide.artifactory.util.DataComponentUtil;
import net.silvertide.artifactory.util.DataPackUtil;

import java.util.Optional;

public class AttunementManager {
    private final ItemStack stack;
    private final ServerPlayer player;
    private final Optional<AttunementDataSource> dataSource;
    private final Optional<AttunementData> attunementData;
    private final Optional<AttunedItem> attunedItem;
    private int levelOfAttunement;

    public AttunementManager(ServerPlayer player, ItemStack stack) {
        this.stack = stack;
        this.player = player;
        this.dataSource = DataPackUtil.getAttunementData(stack);
        this.attunementData = DataComponentUtil.getAttunementData(stack);
        this.attunedItem = attunementData.flatMap(data -> ArtifactorySavedData.get().getAttunedItem(player.getUUID(), data.attunedToUUID()));

        this.levelOfAttunement = attunedItem.map(AttunedItem::getAttunementLevel).orElse(0);
    }

    public Optional<AttunementDataSource> getDataSource() {
        return this.dataSource;
    }

    public Optional<AttunementData> getAttunementData() {
        return this.attunementData;
    }

    public Optional<AttunedItem> getAttunedItem() {
        return this.attunedItem;
    }

    public int getLevelOfAttunementAchieved() {
        return this.levelOfAttunement;
    }
}

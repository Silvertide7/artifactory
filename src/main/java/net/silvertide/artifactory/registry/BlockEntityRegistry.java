package net.silvertide.artifactory.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.blocks.entity.AttunementNexusBlockEntity;

public class BlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Artifactory.MOD_ID);

    public static final RegistryObject<BlockEntityType<AttunementNexusBlockEntity>> ATTUNEMENT_NEXUS_BLOCK_ENTITY = BLOCK_ENTITIES.register("attunement_nexus_block_entity",
            () -> BlockEntityType.Builder.of(AttunementNexusBlockEntity::new,
                    BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

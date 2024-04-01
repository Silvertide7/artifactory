package net.silvertide.artifactory.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.silvertide.artifactory.blocks.entity.AttunementNexusBlockEntity;
import net.silvertide.artifactory.capabilities.AttunedItems;
import net.silvertide.artifactory.registry.BlockEntityRegistry;
import net.silvertide.artifactory.util.ArtifactUtil;
import net.silvertide.artifactory.util.CapabilityUtil;
import net.silvertide.artifactory.util.PlayerMessenger;
import org.jetbrains.annotations.Nullable;

public class AttunementNexusBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(0,0,0,16,12,16);
    public AttunementNexusBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof AttunementNexusBlockEntity attunementNexusBlockEntity) {
                attunementNexusBlockEntity.drops();
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide()) {



            if(pPlayer.isCrouching()) {
                if(pPlayer.getMainHandItem().isEmpty()) {
                    CapabilityUtil.getAttunedItems(pPlayer).ifPresent(attunedItems -> {
                        PlayerMessenger.sendSystemMessage(pPlayer, "You have attuned " + attunedItems.getNumAttunedItems() + " out of " + ArtifactUtil.getMaxAttunementSlots(pPlayer) + " Breaking them.");
                        CapabilityUtil.getAttunedItems(pPlayer).ifPresent(AttunedItems::breakAllAttunements);
                    });
                }
                return InteractionResult.SUCCESS;
            } else {
                BlockEntity entity = pLevel.getBlockEntity(pPos);
                if(entity instanceof AttunementNexusBlockEntity attunementNexusBlockEntity) {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, attunementNexusBlockEntity, pPos);
                } else {
                    throw new IllegalStateException("Out Container provider is missing!");
                }
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }



    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide()) return null;

        return createTickerHelper(pBlockEntityType, BlockEntityRegistry.ATTUNEMENT_NEXUS_BLOCK_ENTITY.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AttunementNexusBlockEntity(pPos, pState);
    }
}

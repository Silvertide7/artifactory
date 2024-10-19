package net.silvertide.artifactory.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.silvertide.artifactory.gui.AttunementNexusAttuneMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class AttunementNexusBlock extends Block {
    public static final VoxelShape SHAPE = Block.box(0,0,0,16,15,16);

    public AttunementNexusBlock(Properties properties) {
        super(BlockBehaviour.Properties.of().strength(5.0F).sound(SoundType.ANCIENT_DEBRIS).noOcclusion().lightLevel(value -> 14));
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((i, inventory, player) ->
                new AttunementNexusAttuneMenu(i, inventory, ContainerLevelAccess.create(level, pos)), Component.translatable("block.artifactory.attunement_nexus"));
    }

    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(blockState.getMenuProvider(level, pos));
            return InteractionResult.CONSUME;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }


}

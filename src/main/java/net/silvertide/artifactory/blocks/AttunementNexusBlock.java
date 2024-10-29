package net.silvertide.artifactory.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.silvertide.artifactory.gui.AttunementNexusAttuneMenu;

import javax.annotation.Nullable;

public class AttunementNexusBlock extends Block {
    public static final VoxelShape BASE = Block.box(3.0D, 0D, 3.0D, 13.0D, 1.0D, 13.0D);
    public static final VoxelShape MIDDLE_X_AXIS = Block.box(5.0D, 1.0D, 4.0D, 11.0D, 10.0D, 12.0D);
    public static final VoxelShape MIDDLE_Z_AXIS = Block.box(4.0D, 1.0D, 5.0D, 12.0D, 10.0D, 11.0D);
    public static final VoxelShape ANVIL_X_AXIS= Block.box(5.0D, 10.0D, 1.0D, 11.0D, 14.0D, 15.0D);
    public static final VoxelShape ANVIL_Z_AXIS= Block.box(1.0D, 10.0D, 5.0D, 15.0D, 14.0D, 11.0D);
    public static final VoxelShape SHAPE_X_AXIS = Shapes.or(BASE, MIDDLE_X_AXIS, ANVIL_X_AXIS);
    public static final VoxelShape SHAPE_Z_AXIS = Shapes.or(BASE, MIDDLE_Z_AXIS, ANVIL_Z_AXIS);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Component CONTAINER_TITLE = Component.translatable("container.artifactory.attunement_nexus");

    public AttunementNexusBlock(Properties properties) {
        super(Properties.copy(Blocks.ENCHANTING_TABLE).noOcclusion().sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((i, inventory, player) ->
                new AttunementNexusAttuneMenu(i, inventory, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE);
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
        Direction direction = state.getValue(FACING);
        return direction.getAxis() == Direction.Axis.X ? SHAPE_X_AXIS : SHAPE_Z_AXIS;
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }
}
package com.amongfox.russiansdelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class AbstractFoodBlock extends Block {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 6);

	public AbstractFoodBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(SERVINGS, getMaxServings()));
	}

	public IntegerProperty getServingsProperty() {
		return SERVINGS;
	}

	protected abstract int getMaxServings();
	protected abstract Item getFoodItem();
	protected abstract VoxelShape getShape();
	protected abstract SoundEvent getTakeServingSoundEvent();
	protected abstract SoundEvent getAddServingSoundEvent();
	protected abstract SoundEvent getBreakSoundEvent();
	protected abstract List<ItemStack> getLeftoverDrops();

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext placementContext) {
		BlockState blockState = defaultBlockState().setValue(FACING, placementContext.getHorizontalDirection().getOpposite());

		ItemStack itemStack = placementContext.getItemInHand();
		if (itemStack.has(DataComponents.CUSTOM_DATA)) {
			CompoundTag nbt = Objects.requireNonNull(itemStack.get(DataComponents.CUSTOM_DATA)).copyTag();
			if (nbt.contains("servings")) {
				return blockState.setValue(getServingsProperty(), nbt.getInt("servings").orElse(0));
			}
		}

		return blockState.setValue(getServingsProperty(), getMaxServings());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, getServingsProperty());
	}

	@Override
	public @NotNull BlockState playerWillDestroy(Level world, BlockPos blockPos, BlockState blockState, Player player) {
		int servings = blockState.getValue(getServingsProperty());

		ItemStack blockItem = new ItemStack(this);

		if (!player.isCreative()) {
			if (servings != getMaxServings()) {
				CompoundTag nbt = new CompoundTag();
				nbt.putInt("servings", servings);
				blockItem.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbt));
			}

			Block.popResource(world, blockPos, blockItem);
		}

		return super.playerWillDestroy(world, blockPos, blockState, player);
	}

	@Override
	public @NotNull InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (world.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	public @NotNull BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		return super.updateShape(blockState, level, scheduledTickAccess, blockPos, direction, neighborPos, neighborState, random);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos blockPos) {
		return blockState.getValue(getServingsProperty());
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState blockState, BlockGetter blockView, BlockPos blockPos, CollisionContext shapeContext) {
		return getShape();
	}

	public ItemStack getServingStack() {
		return new ItemStack(getFoodItem());
	}

	public InteractionResult addServing(Level world, BlockPos blockPos, BlockState blockState, Player player, InteractionHand hand) {
		int servings = blockState.getValue(getServingsProperty());

		ItemStack heldItem = player.getItemInHand(hand);

		if (heldItem.is(getFoodItem())) {
			world.setBlock(blockPos, blockState.setValue(getServingsProperty(), servings + 1), 3);
			world.playSound(null, blockPos, getAddServingSoundEvent(), SoundSource.PLAYERS, 0.8F, 0.8F);

			if (!player.getAbilities().instabuild) {
				heldItem.shrink(1);
				ItemStack bowl = new ItemStack(Items.BOWL);
				if (!player.getInventory().add(bowl)) {
					player.drop(bowl, false);
				}
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	public InteractionResult eatDirectly(Level world, BlockPos blockPos, BlockState blockState, Player player, InteractionHand hand) {
		int servings = blockState.getValue(getServingsProperty());

		ItemStack serving = getServingStack();

		if (player.canEat(false)) {
			world.setBlock(blockPos, blockState.setValue(getServingsProperty(), servings - 1), 3);
			world.playSound(null, blockPos, SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 0.8F, 1.0F);

			player.getFoodData().eat(serving.get(DataComponents.FOOD));

			player.swing(hand);

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	public InteractionResult pickupLeftovers(Level world, BlockPos blockPos, Player player) {
		world.playSound(null, blockPos, getBreakSoundEvent(), SoundSource.PLAYERS, 0.8F, 0.8F);
		world.destroyBlock(blockPos, false, player);

		List<ItemStack> drops = getLeftoverDrops();

		for (ItemStack stack : drops) {
			Containers.dropItemStack(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), stack);
		}

		return InteractionResult.SUCCESS;
	}
}

package com.amongfox.russiansdelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPotBlock extends AbstractFoodBlock {
	public AbstractPotBlock(Properties settings) {
		super(settings);
	}

	@Override
	public @NotNull ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		int servings = blockState.getValue(getServingsProperty());

		if (itemStack.is(Items.BOWL) && servings > 0) {
			if (world.isClientSide()) return ItemInteractionResult.SUCCESS;
			return takeServing(world, blockPos, blockState, player, hand);
		}

		if (itemStack.is(getFoodItem()) && servings < getMaxServings()) {
			if (world.isClientSide()) return ItemInteractionResult.SUCCESS;
			return addServing(world, blockPos, blockState, player, hand);
		}

		if (servings <= 0 && itemStack.isEmpty()) {
			if (world.isClientSide()) return ItemInteractionResult.SUCCESS;
			return pickupLeftovers(world, blockPos, player);
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	public ItemInteractionResult takeServing(Level world, BlockPos blockPos, BlockState blockState, Player player, InteractionHand hand) {
		int servings = blockState.getValue(getServingsProperty());

		ItemStack serving = getServingStack();
		ItemStack itemStack = player.getItemInHand(hand);

		if (itemStack.is(Items.BOWL)) {
			world.setBlock(blockPos, blockState.setValue(getServingsProperty(), servings - 1), 3);
			world.playSound(null, blockPos, getTakeServingSoundEvent(), SoundSource.PLAYERS, 0.8F, 0.8F);

			if (!player.getAbilities().instabuild) {
				itemStack.shrink(1);
			}

			if (!player.getInventory().add(serving)) {
				player.drop(serving, false);
			}

			return ItemInteractionResult.SUCCESS;
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
}

package com.amongfox.russiansdelight.block;

import com.amongfox.russiansdelight.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.BuddingBushBlock;

public class BuddingCucumberBlock extends BuddingBushBlock implements BonemealableBlock {
	public BuddingCucumberBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return pState.is(vectorwing.farmersdelight.common.registry.ModBlocks.RICH_SOIL_FARMLAND.get()) || pState.is(Blocks.FARMLAND);
	}

	public @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
		if (state.getValue(AGE) == 4) {
			((LevelAccessor) level).setBlock(currentPos, ModBlocks.CUCUMBER_CROP.get().defaultBlockState(), 3);
		}

		return super.updateShape(state, level, scheduledTickAccess, currentPos, facing, facingPos, facingState, random);
	}

	public boolean canGrowPastMaxAge() {
		return true;
	}

	public void growPastMaxAge(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		level.setBlockAndUpdate(pos, ModBlocks.CUCUMBER_CROP.get().defaultBlockState());
	}

	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return true;
	}

	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	protected int getBonemealAgeIncrease(Level level) {
		return Mth.nextInt(level.random, 1, 4);
	}

	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		int maxAge = this.getMaxAge();
		int ageGrowth = Math.min(this.getAge(state) + this.getBonemealAgeIncrease(level), 7);
		if (ageGrowth <= maxAge) {
			level.setBlockAndUpdate(pos, state.setValue(AGE, ageGrowth));
		} else {
			int remainingGrowth = ageGrowth - maxAge - 1;
			level.setBlockAndUpdate(pos, ModBlocks.CUCUMBER_CROP.get().defaultBlockState().setValue(CucumberVineBlock.VINE_AGE, remainingGrowth));
		}

	}
}

package com.amongfox.russiansdelight.item;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class PotBlockItem extends BlockItem implements FabricItem {
    private final Item remainder;

    public PotBlockItem(Block block, Item remainder, Item.Properties properties) {
        super(block, properties);
        this.remainder = remainder;
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return remainder != null ? new ItemStack(remainder) : ItemStack.EMPTY;
    }
}

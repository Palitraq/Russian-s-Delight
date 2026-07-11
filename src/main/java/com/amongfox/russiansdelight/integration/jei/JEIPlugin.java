package com.amongfox.russiansdelight.integration.jei;

import com.amongfox.russiansdelight.registry.ModItems;
import com.amongfox.russiansdelight.RussiansDelight;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation(RussiansDelight.MOD_ID, "jei_plugin");

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
            List.of(new ItemStack(ModItems.WILD_CUCUMBER.get()), new ItemStack(ModItems.CUCUMBER.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("russiansdelight.jei.info.wild_cucumber")
        );
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}

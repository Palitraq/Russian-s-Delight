package com.amongfox.russiansdelight.mixin;

import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Arrays;

@Mixin(SearchRecipeBookCategory.class)
public class SearchRecipeBookCategoryMixin {

    @Shadow
    private static SearchRecipeBookCategory[] $VALUES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void russiansDelight$addCookingCategory(CallbackInfo ci) {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);

            SearchRecipeBookCategory newConstant = (SearchRecipeBookCategory)
                    unsafeClass.getMethod("allocateInstance", Class.class)
                            .invoke(unsafe, SearchRecipeBookCategory.class);

            Field nameField = Enum.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(newConstant, "FARMERSDELIGHT_COOKING");

            Field ordinalField = Enum.class.getDeclaredField("ordinal");
            ordinalField.setAccessible(true);
            ordinalField.setInt(newConstant, $VALUES.length);

            SearchRecipeBookCategory[] newValues = Arrays.copyOf($VALUES, $VALUES.length + 1);
            newValues[$VALUES.length] = newConstant;
            $VALUES = newValues;
        } catch (Exception e) {
            // ignore
        }
    }
}

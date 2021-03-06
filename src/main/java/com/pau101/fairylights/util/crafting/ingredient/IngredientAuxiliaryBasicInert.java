package com.pau101.fairylights.util.crafting.ingredient;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class IngredientAuxiliaryBasicInert extends IngredientAuxiliaryBasic<Void> {
	public IngredientAuxiliaryBasicInert(Item item, boolean isRequired, int limit) {
		super(item, isRequired, limit);
	}

	public IngredientAuxiliaryBasicInert(Item item, int metadata, boolean isRequired, int limit) {
		super(item, metadata, isRequired, limit);
	}

	public IngredientAuxiliaryBasicInert(Block block, boolean isRequired, int limit) {
		super(block, isRequired, limit);
	}

	public IngredientAuxiliaryBasicInert(ItemStack stack, boolean isRequired, int limit) {
		super(stack, isRequired, limit);
	}

	public IngredientAuxiliaryBasicInert(ItemStack stack) {
		super(stack, true, Integer.MAX_VALUE);
	}

	@Nullable
	@Override
	public final Void accumulator() {
		return null;
	}

	@Override
	public final void consume(Void v, ItemStack ingredient) {}

	@Override
	public final boolean finish(Void v, ItemStack stack) {
		return false;
	}
}

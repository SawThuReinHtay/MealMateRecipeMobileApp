package com.strh.mealmate.ui.addrecipe;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RecipePagerAdapter extends FragmentStateAdapter {
    private final IngredientsFragment ingredientsFragment = new IngredientsFragment();
    private final RecipeFragment recipeFragment = new RecipeFragment();

    public RecipePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? ingredientsFragment : recipeFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    // ✅ Add getters
    public IngredientsFragment getIngredientsFragment() {
        return ingredientsFragment;
    }

    public RecipeFragment getRecipeFragment() {
        return recipeFragment;
    }
}


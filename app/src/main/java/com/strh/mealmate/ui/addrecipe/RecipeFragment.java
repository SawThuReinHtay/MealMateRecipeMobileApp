package com.strh.mealmate.ui.addrecipe;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.strh.mealmate.R;

public class RecipeFragment extends Fragment {
    private EditText recipeText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);
        recipeText = view.findViewById(R.id.recipe_input);
        return view;
    }

    public String getRecipeText() {
        if (recipeText != null) {
            return recipeText.getText().toString().trim();
        }
        return "";
    }
}

package com.strh.mealmate.ui.addrecipe;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.strh.mealmate.R;

import java.util.ArrayList;

public class IngredientsFragment extends Fragment {

    private LinearLayout ingredientsContainer;
    private Button addIngredientButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_ingredients, container, false);

        ingredientsContainer = root.findViewById(R.id.ingredients_container);
        addIngredientButton = root.findViewById(R.id.button_add_ingredient);

        addIngredientButton.setOnClickListener(v -> addIngredientRow());
        addIngredientButton.setBackgroundTintList(null);

        // Start with one ingredient input by default
        addIngredientRow();

        return root;
    }

    private void addIngredientRow() {
        // Create a horizontal layout to hold the EditText and delete button
        LinearLayout ingredientRow = new LinearLayout(getContext());
        ingredientRow.setOrientation(LinearLayout.HORIZONTAL);
        ingredientRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ingredientRow.setPadding(0, 8, 0, 8);


        // Create the EditText
        EditText editText = new EditText(getContext());
        editText.setId(R.id.ingredient_edit_text);
        editText.setHint("Enter ingredient");
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.edittext_color));
        editText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.edittext_color));
        editText.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.edittext_color)));
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1)); // Weight 1 to fill space

        // Create the delete button
        ImageButton deleteButton = new ImageButton(getContext());
        deleteButton.setImageResource(R.drawable.delete_24px);
        deleteButton.setBackgroundColor(Color.TRANSPARENT);
        deleteButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red));
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Set click listener to delete this row
        deleteButton.setOnClickListener(v -> ingredientsContainer.removeView(ingredientRow));

        // Add EditText and delete button to the horizontal layout
        ingredientRow.addView(editText);
        ingredientRow.addView(deleteButton);

        // Add the row to the container (you can change 0 to -1 to add at the bottom)
        ingredientsContainer.addView(ingredientRow, 0);
    }

    public ArrayList<String> getIngredientsList() {
        ArrayList<String> ingredients = new ArrayList<>();
        LinearLayout container = getView().findViewById(R.id.ingredients_container); // Adjust ID if needed
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                EditText et = child.findViewById(R.id.ingredient_edit_text); // Adjust ID if needed
                if (et != null && !et.getText().toString().trim().isEmpty()) {
                    ingredients.add(et.getText().toString().trim());
                }
            }
        }
        return ingredients;
    }

}

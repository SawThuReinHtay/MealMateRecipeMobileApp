package com.strh.mealmate.ui.mealdetail;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.strh.mealmate.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MealIngredientAdapter extends RecyclerView.Adapter<MealIngredientAdapter.ViewHolder> {
    private List<String> ingredients;
    private Set<String> checkedIngredients;
    private String mealId, userId;


    public MealIngredientAdapter(List<String> ingredients, Set<String> checkedIngredients, String mealId, String userId) {
        this.ingredients = ingredients;
        this.checkedIngredients = checkedIngredients;
        this.mealId = mealId;
        this.userId = userId;
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_ingredient);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String ingredient = ingredients.get(position);
        holder.checkBox.setText(ingredient);

        // Remove any existing listener to prevent it from triggering when we setChecked
        holder.checkBox.setOnCheckedChangeListener(null);

        boolean isChecked = checkedIngredients.contains(ingredient);
        holder.checkBox.setChecked(isChecked);  // Set state silently
        applyStrikeThrough(holder.checkBox, isChecked);  // Strikethrough logic

        // Now set the real listener
        holder.checkBox.setOnCheckedChangeListener((btn, isNowChecked) -> {
            if (isNowChecked) {
                checkedIngredients.add(ingredient);
            } else {
                checkedIngredients.remove(ingredient);
            }
            applyStrikeThrough(holder.checkBox, isNowChecked);
            saveCheckedIngredientsToFirestore();
        });
    }


    private void applyStrikeThrough(CheckBox checkBox, boolean isChecked) {
        if (isChecked) {
            checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            checkBox.setPaintFlags(checkBox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }


    private void saveCheckedIngredientsToFirestore() {
        Map<String, Object> data = new HashMap<>();
        data.put("mealId", mealId);
        data.put("userId", userId);
        data.put("checkedIngredients", new ArrayList<>(checkedIngredients));

        FirebaseFirestore.getInstance()
                .collection("bought_ingredients")
                .document(userId + "_" + mealId)
                .set(data);
    }


    @Override public int getItemCount() { return ingredients.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }
}


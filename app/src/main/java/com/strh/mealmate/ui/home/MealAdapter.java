package com.strh.mealmate.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.strh.mealmate.R;
import com.strh.mealmate.model.Meal;

import java.util.List;




public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
    private List<Meal> mealList;
    private Context context;
    private OnMealClickListener listener;

    private boolean deleteMode = false;

    public interface OnMealClickListener {
        void onMealClick(Meal meal);
        void onMealDelete(Meal meal);
    }

    public void setDeleteMode(boolean enabled) {
        deleteMode = enabled;
        notifyDataSetChanged();
        Toast.makeText(context, enabled ? "Delete mode enabled. Double tap to disable." : "Delete mode disabled", Toast.LENGTH_SHORT).show();
    }


    public MealAdapter(Context context, List<Meal> mealList,OnMealClickListener listener) {
        this.context = context;
        this.mealList = mealList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.name.setText(meal.getName());
        holder.serving.setText(meal.getServing());
        ImageView deleteIcon = holder.itemView.findViewById(R.id.icon_delete);

        Glide.with(context)
                .load(meal.getImageUrl())
                .circleCrop()
                .into(holder.image);

        //Create gesture detector
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (deleteMode) {
                    setDeleteMode(false);
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                setDeleteMode(true);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (!deleteMode && listener != null) {
                    listener.onMealClick(meal);
                }
                return true;
            }
        });

        //Attaching gesture detector
        holder.itemView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        //Show or hide delete icon
        if (deleteMode) {
            deleteIcon.setVisibility(View.VISIBLE);
            deleteIcon.setOnClickListener(v -> {
                showDeleteConfirmation(holder.itemView.getContext(), meal);
            });
        } else {
            deleteIcon.setVisibility(View.GONE);
        }
    }


    private void showDeleteConfirmation(Context context, Meal meal) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Meal")
                .setMessage("Are you sure you want to delete this meal?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (listener != null) {
                        listener.onMealDelete(meal);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }




    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;

        TextView serving;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_meal);
            name = itemView.findViewById(R.id.meal_name);
            serving = itemView.findViewById(R.id.meal_serving);
        }


    }
}


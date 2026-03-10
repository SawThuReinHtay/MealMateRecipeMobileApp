package com.strh.mealmate.ui.mealdetail;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.strh.mealmate.R;
import com.strh.mealmate.ui.mealdetail.MealIngredientAdapter;
import com.strh.mealmate.databinding.ActivityMealDetailBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MealDetailActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorListener;

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private long lastShakeTime = 0;

    private ActivityMealDetailBinding binding;
    private String mealId;
    private String userId;
    FloatingActionButton fabMain, fabEdit, fabDelete;
    boolean isFabOpen = false;
    private List<String> ingredients = new ArrayList<>();
    private boolean isEditMode = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMealDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float gX = x / SensorManager.GRAVITY_EARTH;
                float gY = y / SensorManager.GRAVITY_EARTH;
                float gZ = z / SensorManager.GRAVITY_EARTH;

                float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

                if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                    final long now = System.currentTimeMillis();
                    if (lastShakeTime + SHAKE_SLOP_TIME_MS > now) {
                        return;
                    }
                    lastShakeTime = now;

                    // For vibration
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(150);
                        }
                    }

                    // Toggle edit mode
                    if (isEditMode) {
                        exitEditMode();
                        Toast.makeText(MealDetailActivity.this, "Exited edit mode", Toast.LENGTH_SHORT).show();
                    } else {
                        toggleEditMode();
                        Toast.makeText(MealDetailActivity.this, "Entered edit mode", Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };


        mealId = getIntent().getStringExtra("mealId");
        userId = FirebaseAuth.getInstance().getUid();

        loadMealDetails();
        setupToggleListeners();

        fabMain = findViewById(R.id.fab_main);
        fabEdit = findViewById(R.id.fab_edit);
        fabDelete = findViewById(R.id.fab_delete);

        fabMain.setOnClickListener(v -> toggleFabMenu());

        fabEdit.setOnClickListener(v -> toggleEditMode());

        fabDelete.setOnClickListener(v -> showDeleteConfirmation());

        binding.btnAddIngredient.setOnClickListener(v -> addEditableIngredient(null));

        binding.btnAddIngredient.setBackgroundTintList(null);

        binding.btnShareIngredients.setOnClickListener(v -> shareIngredients());

        binding.btnSaveChanges.setOnClickListener(v -> saveChangesToFirestore());


    }

    private void setupToggleListeners() {
        binding.ingredientsToggle.setOnClickListener(v -> {
            toggleExpandCollapse(binding.ingredientContainer, binding.ingredientsArrow, binding.ingredientsToggle);
        });

        binding.recipeToggle.setOnClickListener(v -> {
            toggleExpandCollapse(binding.recipeContainer, binding.recipeArrow, binding.recipeToggle);


        });
    }

    private void addEditableIngredient(@Nullable String prefillText) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_ingredient_editable, binding.editIngredientsContainer, false);

        EditText ingredientEditText = itemView.findViewById(R.id.edit_ingredient);
        ImageView deleteButton = itemView.findViewById(R.id.delete_ingredient);

        if (ingredientEditText == null || deleteButton == null || binding.editIngredientsContainer == null) {
            Toast.makeText(this, "Error loading ingredient layout", Toast.LENGTH_SHORT).show();
            return;
        }

        if (prefillText != null) {
            ingredientEditText.setText(prefillText);
        }

        deleteButton.setOnClickListener(v -> {
            binding.editIngredientsContainer.removeView(itemView);
            binding.editIngredientsContainer.requestLayout();
        });

        binding.editIngredientsContainer.addView(itemView);
        binding.editIngredientsContainer.requestLayout();
    }





    private void toggleEditMode() {
        isEditMode = true;

        // Swap TextViews with EditTexts
        binding.mealName.setVisibility(View.GONE);
        binding.editMealName.setVisibility(View.VISIBLE);
        binding.editMealName.setText(binding.mealName.getText());

        binding.mealDescription.setVisibility(View.GONE);
        binding.editMealDescription.setVisibility(View.VISIBLE);
        binding.editMealDescription.setText(binding.mealDescription.getText());

        binding.recipeScrollView.setVisibility(View.GONE);
        binding.editRecipeScrollView.setVisibility(View.VISIBLE);
        binding.editRecipeText.setVisibility(View.VISIBLE);
        binding.editRecipeText.setText(binding.recipeText.getText());

        // Show Save button, hide Share button
        binding.btnShareIngredients.setVisibility(View.GONE);
        binding.btnSaveChanges.setVisibility(View.VISIBLE);

        // Replace checkboxes with editable fields
        showEditableIngredients();
    }

    private void showEditableIngredients() {
        binding.ingredientsRecycler.setVisibility(View.GONE);
        binding.editIngredientsScrollView.setVisibility(View.VISIBLE);
        binding.editIngredientsContainer.setVisibility(View.VISIBLE); // a LinearLayout
        binding.btnAddIngredient.setVisibility(View.VISIBLE);



        binding.editIngredientsContainer.removeAllViews();

        for (String ingredient : ingredients) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_ingredient_editable, binding.editIngredientsContainer, false);
            EditText editText = item.findViewById(R.id.edit_ingredient);
            ImageView deleteIcon = item.findViewById(R.id.delete_ingredient);

            editText.setText(ingredient);

            deleteIcon.setOnClickListener(v -> {
                binding.editIngredientsContainer.removeView(item);
            });

            binding.editIngredientsContainer.addView(item);
        }
    }

    private void exitEditMode() {
        isEditMode  = false;
        binding.mealName.setVisibility(View.VISIBLE);
        binding.mealDescription.setVisibility(View.VISIBLE);
        binding.recipeScrollView.setVisibility(View.VISIBLE);
        binding.recipeText.setVisibility(View.VISIBLE);
        binding.ingredientsRecycler.setVisibility(View.VISIBLE);
        binding.btnShareIngredients.setVisibility(View.VISIBLE);

        binding.editMealName.setVisibility(View.GONE);
        binding.editMealDescription.setVisibility(View.GONE);
        binding.editRecipeScrollView.setVisibility(View.GONE);
        binding.editRecipeText.setVisibility(View.GONE);
        binding.editIngredientsContainer.setVisibility(View.GONE);
        binding.editIngredientsScrollView.setVisibility(View.GONE);
        binding.btnAddIngredient.setVisibility(View.GONE);
        binding.btnSaveChanges.setVisibility(View.GONE);
    }


    private void saveChangesToFirestore() {
        String updatedName = binding.editMealName.getText().toString().trim();
        String updatedDescription = binding.editMealDescription.getText().toString().trim();
        String updatedRecipe = binding.editRecipeText.getText().toString().trim();

        // Collect ingredients from all EditTexts in the editable container
        List<String> updatedIngredients = new ArrayList<>();
        for (int i = 0; i < binding.editIngredientsContainer.getChildCount(); i++) {
            View item = binding.editIngredientsContainer.getChildAt(i);
            EditText editText = item.findViewById(R.id.edit_ingredient);
            if (editText != null) {
                String ingredient = editText.getText().toString().trim();
                if (!ingredient.isEmpty()) {
                    updatedIngredients.add(ingredient);
                }
            }
        }

        // Prepare update map
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", updatedName);
        updatedData.put("description", updatedDescription);
        updatedData.put("recipe", updatedRecipe);
        updatedData.put("ingredients", updatedIngredients);

        // Update Firestore
        FirebaseFirestore.getInstance().collection("meals").document(mealId)
                .update(updatedData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
                    exitEditMode();
                    loadMealDetails(); // Refresh the UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                });
    }


    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Meal")
                .setMessage("Are you sure you want to delete this meal?")
                .setPositiveButton("Yes", (dialog, which) -> deleteMealFromFirestore())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMealFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("meals").document(mealId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Meal deleted successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete meal", Toast.LENGTH_SHORT).show();
                });
    }



    private void toggleFabMenu() {
        if (isFabOpen) {
            fabEdit.animate().translationY(0).alpha(0f).setDuration(200).withEndAction(() -> fabEdit.setVisibility(View.GONE));
            fabDelete.animate().translationY(0).alpha(0f).setDuration(200).withEndAction(() -> fabDelete.setVisibility(View.GONE));
            fabMain.animate().rotation(0).setDuration(200);
        } else {
            fabEdit.setVisibility(View.VISIBLE);
            fabEdit.setAlpha(0f);
            fabEdit.animate().translationY(-80).alpha(1f).setDuration(200);

            fabDelete.setVisibility(View.VISIBLE);
            fabDelete.setAlpha(0f);
            fabDelete.animate().translationY(-140).alpha(1f).setDuration(200);

            fabMain.animate().rotation(90).setDuration(200);
        }
        isFabOpen = !isFabOpen;
    }


    private void loadMealDetails() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("meals").document(mealId).get().addOnSuccessListener(mealDoc -> {
            if (mealDoc.exists()) {
                String name = mealDoc.getString("name");
                String description = mealDoc.getString("description");
                String recipe = mealDoc.getString("recipe");
                String imageUrl = mealDoc.getString("imageUrl");
                ingredients = (List<String>) mealDoc.get("ingredients");

                binding.mealName.setText(name);
                binding.mealDescription.setText(description);
                binding.recipeText.setText(recipe);
                Glide.with(this)
                        .load(imageUrl)
                        .into(binding.mealImage);

                db.collection("bought_ingredients").document(userId + "_" + mealId).get()
                        .addOnSuccessListener(boughtDoc -> {
                            Set<String> checked = new HashSet<>();
                            if (boughtDoc.exists()) {
                                checked.addAll((List<String>) boughtDoc.get("checkedIngredients"));
                            }
                            setupIngredientsRecycler(ingredients, checked);
                        });
            }
        });
    }


    private void toggleExpandCollapse(View contentView, View arrowView, View toggleHeader) {
        boolean isExpanding = contentView.getVisibility() == View.GONE;

        float radiusAll = getResources().getDimension(R.dimen.corner_radius); // e.g. 12dp
        float radiusZero = 0f;

        // Build ShapeAppearanceModel with top corners always rounded, bottom changes based on expand
        // Create initial shape
        ShapeAppearanceModel shapeModel = new ShapeAppearanceModel().toBuilder()
                .setTopLeftCornerSize(radiusAll)
                .setTopRightCornerSize(radiusAll)
                .setBottomLeftCornerSize(isExpanding ? radiusAll : radiusZero)
                .setBottomRightCornerSize(isExpanding ? radiusAll : radiusZero)
                .build();

        MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeModel);
        shapeDrawable.setFillColor(ContextCompat.getColorStateList(this, R.color.accent));
        shapeDrawable.setStroke(1f, ContextCompat.getColor(this, R.color.secondary));
        toggleHeader.setBackground(shapeDrawable);

        // transition animation
        ValueAnimator cornerAnimator = ValueAnimator.ofFloat(
                isExpanding ? radiusAll : radiusZero,
                isExpanding ? radiusZero : radiusAll
        );
        cornerAnimator.setDuration(300);
        cornerAnimator.addUpdateListener(anim -> {
            float animatedRadius = (float) anim.getAnimatedValue();

            shapeDrawable.setShapeAppearanceModel(
                    shapeDrawable.getShapeAppearanceModel().toBuilder()
                            .setTopLeftCornerSize(radiusAll)
                            .setTopRightCornerSize(radiusAll)
                            .setBottomLeftCornerSize(animatedRadius)
                            .setBottomRightCornerSize(animatedRadius)
                            .build()
            );
        });
        cornerAnimator.start();


        // Animate content expand/collapse
        if (isExpanding) {
            contentView.setAlpha(0f);
            contentView.setVisibility(View.VISIBLE);
            int targetHeight;
            if (contentView.getId() == R.id.recipe_container) {
                targetHeight = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()
                );
            } else {
                contentView.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                targetHeight = contentView.getMeasuredHeight();
            }

            contentView.getLayoutParams().height = 0;

            ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
            animator.addUpdateListener(animation -> {
                contentView.getLayoutParams().height = (int) animation.getAnimatedValue();
                contentView.requestLayout();
                contentView.setAlpha(animation.getAnimatedFraction());
            });
            animator.setDuration(300);
            animator.start();

            arrowView.animate().rotation(180f).setDuration(300).start();

        } else {
            int initialHeight = contentView.getMeasuredHeight();

            ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
            animator.addUpdateListener(animation -> {
                contentView.getLayoutParams().height = (int) animation.getAnimatedValue();
                contentView.requestLayout();
                contentView.setAlpha(1f - animation.getAnimatedFraction());
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    contentView.setVisibility(View.GONE);
                }
            });
            animator.setDuration(300);
            animator.start();

            arrowView.animate().rotation(0f).setDuration(300).start();
        }
    }

    private void shareIngredients() {
        if (ingredients == null || ingredients.isEmpty()) {
            Toast.makeText(this, "No ingredients to share", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder shareText = new StringBuilder("Shopping List:\n");
        for (String ingredient : ingredients) {
            shareText.append("• ").append(ingredient).append("\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share via");
        startActivity(shareIntent);
    }


    private void setupIngredientsRecycler(List<String> ingredients, Set<String> checked) {
        MealIngredientAdapter adapter = new MealIngredientAdapter(ingredients, checked, mealId, userId);
        binding.ingredientsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.ingredientsRecycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
        }
    }

}

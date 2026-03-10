package com.strh.mealmate.ui.addrecipe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.strh.mealmate.R;
import com.strh.mealmate.databinding.FragmentAddrecipeBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddRecipeFragment extends Fragment {

    private RecipePagerAdapter adapter;
    private FragmentAddrecipeBinding binding;
    private FirebaseFirestore db;
    private CollectionReference mealsRef;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAddrecipeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.buttonSaveRecipe.setBackgroundTintList(null);

        db = FirebaseFirestore.getInstance();
        mealsRef = db.collection("meals");

        adapter = new RecipePagerAdapter(requireActivity());
        binding.viewPager.setAdapter(adapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Ingredients" : "Recipe")).attach();

        binding.imagePicker.setOnClickListener(v -> openImagePicker());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.serving_array,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        binding.mealServing.setAdapter(adapter);

        binding.buttonSaveRecipe.setOnClickListener(v -> saveMealToFirestore());

        return root;
    }

    public void uploadImageToImgur(Uri imageUri, String mealId, Map<String, Object> meal) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            String accessToken = "843fe7616985d144e868193bc5a0a4647dc684e3";

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", base64Image)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            String imageUrl = new JSONObject(responseData)
                                    .getJSONObject("data")
                                    .getString("link");

                            meal.put("imageUrl", imageUrl);

                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Uploaded to Imgur!", Toast.LENGTH_SHORT).show();
                                uploadMealData(mealId, meal);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("IMGUR_UPLOAD", "Error response: " + responseData);
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Imgur error: " + responseData, Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            binding.imagePicker.setImageURI(selectedImageUri);
        }
    }

    private void saveMealToFirestore() {
        String name = binding.mealName.getText().toString().trim();
        String description = binding.mealDescription.getText().toString().trim();
        String serving = binding.mealServing.getSelectedItem().toString();

        IngredientsFragment ingredientsFragment = adapter.getIngredientsFragment();
        ArrayList<String> ingredients = ingredientsFragment.getIngredientsList();

        RecipeFragment recipeFragment = adapter.getRecipeFragment();
        String recipe = recipeFragment.getRecipeText();

        boolean hasError = false;

        if (name.isEmpty()) {
            binding.mealName.setBackgroundResource(R.drawable.edittext_error);
            binding.mealName.setHintTextColor(Color.RED);
            hasError = true;
        } else {
            binding.mealName.setBackgroundResource(R.drawable.edittext_frosted);
        }

        if (description.isEmpty()) {
            binding.mealDescription.setBackgroundResource(R.drawable.edittext_error);
            binding.mealDescription.setHintTextColor(Color.RED);
            hasError = true;
        } else {
            binding.mealDescription.setBackgroundResource(R.drawable.edittext_frosted);
        }

        if (ingredients.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            hasError = true;
        }

        if (recipe.isEmpty()) {
            Toast.makeText(getContext(), "Please provide the recipe steps", Toast.LENGTH_SHORT).show();
            hasError = true;
        }

        if (hasError) return;

        Map<String, Object> meal = new HashMap<>();
        meal.put("name", name);
        meal.put("description", description);
        meal.put("serving", serving);
        meal.put("ingredients", ingredients);
        meal.put("recipe", recipe);

        mealsRef.get().addOnSuccessListener(querySnapshot -> {
            int maxId = -1;
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                try {
                    int currentId = Integer.parseInt(doc.getId());
                    if (currentId > maxId) maxId = currentId;
                } catch (NumberFormatException ignored) {}
            }
            String newId = String.format("%04d", maxId + 1);

            if (selectedImageUri != null) {
                uploadImageToImgur(selectedImageUri, newId, meal);
            } else {
                Toast.makeText(getContext(), "Please select an image for the meal", Toast.LENGTH_SHORT).show();
                return;
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to fetch meals: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void uploadMealData(String mealId, Map<String, Object> mealData) {
        mealsRef.document(mealId).set(mealData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Meal saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

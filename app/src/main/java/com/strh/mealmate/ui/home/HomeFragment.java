package com.strh.mealmate.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.strh.mealmate.databinding.FragmentHomeBinding;
import com.strh.mealmate.model.Meal;
import com.strh.mealmate.ui.mealdetail.MealDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements MealAdapter.OnMealClickListener{

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private List<Meal> mealList = new ArrayList<>();
    private MealAdapter mealAdapter;
    private ActivityResultLauncher<Intent> mealDetailLauncher;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        binding.swipeRefresh.setOnRefreshListener(() -> {
            fetchMeals();
        });

        mealAdapter = new MealAdapter(getContext(), mealList,this);
        binding.mealRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.mealRecyclerView.setAdapter(mealAdapter);

        mealDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        fetchMeals(); // refresh meal list
                    }
                });


        fetchMeals();

        return root;
    }

    private void fetchMeals() {
        db.collection("meals").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mealList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        String serving = doc.getString("serving");
                        String imageUrl = doc.getString("imageUrl");

                        mealList.add(new Meal(id, name, serving, imageUrl));
                    }
                    mealAdapter.notifyDataSetChanged();
                    binding.swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching meals", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMealClick(Meal meal) {
        Intent intent = new Intent(getContext(), MealDetailActivity.class);
        intent.putExtra("mealId", meal.getId());
        mealDetailLauncher.launch(intent);
    }

    @Override
    public void onMealDelete(Meal meal) {
        FirebaseFirestore.getInstance().collection("meals")
                .document(meal.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Meal deleted", Toast.LENGTH_SHORT).show();
                    fetchMeals(); // refresh list
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete meal", Toast.LENGTH_SHORT).show());
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            fetchMeals(); // Refresh the list
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

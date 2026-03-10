package com.strh.mealmate.ui.addrecipe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddRecipeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AddRecipeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Add Receipe Fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
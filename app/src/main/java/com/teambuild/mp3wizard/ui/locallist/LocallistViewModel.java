package com.teambuild.mp3wizard.ui.locallist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LocallistViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LocallistViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Local Books");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
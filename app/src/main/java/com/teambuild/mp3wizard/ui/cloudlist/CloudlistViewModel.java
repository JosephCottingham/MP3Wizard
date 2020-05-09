package com.teambuild.mp3wizard.ui.cloudlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CloudlistViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CloudlistViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Cloud Books");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
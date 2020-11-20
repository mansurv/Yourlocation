package com.netmontools.lookatnet.ui.log_view;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LogViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private String fileName = "log.dat";
    public LogViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue(MainActivity.veryLongString.toString());
    }

    public LiveData<String> getText() {
        return mText;
    }
}
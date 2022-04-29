package com.example.android.cleancity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<dustbin>> dustbinsList;

    public MainViewModel() {
        dustbinsList = new MutableLiveData<>();
    }

    public void setDustbinsList(ArrayList<dustbin> dustbins){
        dustbinsList.setValue(dustbins);
    }

    public LiveData<ArrayList<dustbin>> getDustbinsList(){
        return dustbinsList;
    }

}

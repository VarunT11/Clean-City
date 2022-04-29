package com.example.android.cleancity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DustbinsListFragment extends DialogFragment {

    private MainViewModel mainViewModel;
    private RecyclerView rcvDustbinsList;

    public DustbinsListFragment(){

    }

    public static DustbinsListFragment newInstance(){
        return new DustbinsListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dustbins_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        rcvDustbinsList = view.findViewById(R.id.rcvDustbinsList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcvDustbinsList.setLayoutManager(layoutManager);

        mainViewModel.getDustbinsList().observe(getViewLifecycleOwner(), dustbins -> {
            dustbinAdapter adapter = new dustbinAdapter(dustbins, requireActivity());
            rcvDustbinsList.setAdapter(adapter);
        });
    }
}
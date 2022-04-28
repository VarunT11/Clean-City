package com.example.android.cleancity;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.Vector;

public class CustomRecyclerDialog extends Dialog {

    public CustomRecyclerDialog(@NonNull Context context, Vector<dustbin> dustbins) {
        super(context);
        WindowManager.LayoutParams wlmp = getWindow().getAttributes();
        wlmp.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(wlmp);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        View view = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null);
        setContentView(view);
    }
}





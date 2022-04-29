package com.example.android.cleancity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Vector;

public class dustbinAdapter extends RecyclerView.Adapter<dustbinAdapter.ViewHolder> {
    Context context;
    ArrayList<dustbin> dustbins;

    public dustbinAdapter(ArrayList<dustbin> dustbins, Context context) {
        this.dustbins = dustbins;
        this.context = context;
    }

    @NonNull
    @Override
    public dustbinAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.dustbin_sample,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull dustbinAdapter.ViewHolder holder, int position) {

        final dustbin dus=dustbins.get(position);

        holder.id.setText(Long.toString(dus.getId()));
        holder.latitude.setText(Double.toString(dus.getLatitude()));
        holder.longitude.setText(Double.toString(dus.getLongitude()));
        long lvl=dus.getLevel();
        String s1="";
        if(lvl==0){
            s1="<50%";
        }
        else if(lvl==1){
            s1="50%";
        }
        else if(lvl==2){
            s1="75%";
        }
        else{
            s1="100%";
        }
        holder.level.setText(s1);


    }

    @Override
    public int getItemCount() {
        return dustbins.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView id,level,latitude,longitude;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            id=itemView.findViewById(R.id.txt_id);
            level=itemView.findViewById(R.id.txt_level);
            latitude=itemView.findViewById(R.id.txt_latitude);
            longitude=itemView.findViewById(R.id.txt_longitude);
        }
    }
}

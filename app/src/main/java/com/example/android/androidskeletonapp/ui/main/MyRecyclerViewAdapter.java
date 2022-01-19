package com.example.android.androidskeletonapp.ui.main;


import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.androidskeletonapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<BluetoothDevice> mDevices;
    private RecyclerViewClickListener listener;

    public MyRecyclerViewAdapter(ArrayList<BluetoothDevice> mDevices, RecyclerViewClickListener listener){
        this.mDevices = mDevices;
        this.listener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView deviceName;
        private TextView deviceAddress;

        public MyViewHolder(final View view){
            super(view);
            deviceName = view.findViewById(R.id.tvDeviceName);
            deviceAddress = view.findViewById(R.id.tvDeviceAddress);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }

    @NotNull
    @Override
    public MyRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewAdapter.MyViewHolder holder, int position) {
        String name = mDevices.get(position).getAddress().substring(0,11);
        String address = mDevices.get(position).getAddress();
        holder.deviceName.setText(name);
        holder.deviceAddress.setText(address);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public interface RecyclerViewClickListener{
        void onClick(View v , int position);
    }

}
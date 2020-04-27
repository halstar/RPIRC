package com.e.rpirc;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DevicesListAdapter extends RecyclerView.Adapter<DevicesListAdapter.ViewHolder> {
    private List<BluetoothDevice> devicesList;
    private int                   selectedPosition;

    public DevicesListAdapter(List<BluetoothDevice> initDevicesList) {
        devicesList      = initDevicesList;
        selectedPosition = RecyclerView.NO_POSITION;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        public TextView itemTextView;

        public ViewHolder(View view) {
            super(view);
            itemTextView = view.findViewById(R.id.devices_list_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }
            notifyItemChanged(selectedPosition);
            selectedPosition = getAdapterPosition();
            notifyItemChanged(selectedPosition);
        }
    }

    @Override
    public DevicesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.devices_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView        textView = holder.itemTextView;
        BluetoothDevice device   = devicesList.get(position);
        textView.setText(device.getName() + " - " + device.getAddress());
        holder.itemView.setBackgroundColor(selectedPosition == position ? Color.LTGRAY : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }

    public void replaceItems(List<BluetoothDevice> replacementDevicesList) {
        devicesList = replacementDevicesList;
        notifyDataSetChanged();
    }

    public BluetoothDevice getSelectedItem() {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            return devicesList.get(selectedPosition);
        } else {
            return null;
        }
    }
}

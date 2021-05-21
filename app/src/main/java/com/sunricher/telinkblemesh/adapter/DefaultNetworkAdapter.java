package com.sunricher.telinkblemesh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemesh.model.MyDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.telink.Arrays;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DefaultNetworkAdapter extends RecyclerView.Adapter<DefaultNetworkAdapter.ViewHolder> {

    private ArrayList<MyDevice> devices;
    private OnClickListener clickListener;

    public DefaultNetworkAdapter() {
        this.devices = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_detail_vertical_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = vh.getAdapterPosition();
                if (clickListener != null) {
                    clickListener.onItemClick(vh, position, devices.get(position));
                }
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull DefaultNetworkAdapter.ViewHolder holder, int position) {

        MyDevice device = devices.get(position);
        holder.getTextLabel().setText(device.getTitle());
        holder.getDetailLabel().setText(device.getDetail());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textLabel;
        private final TextView detailLabel;

        public ViewHolder(View view) {
            super(view);

            textLabel = (TextView) view.findViewById(R.id.text_label);
            detailLabel = (TextView) view.findViewById(R.id.detail_label);
        }

        public TextView getTextLabel() {
            return textLabel;
        }

        public TextView getDetailLabel() {
            return detailLabel;
        }

    }

    /**
     *
     * @param device
     * @return isUpdate?
     */
    public Boolean addOrUpdate(MyDevice device) {

        Boolean result = false;

        if (devices.contains(device)) {

            int index = devices.indexOf(device);
            devices.get(index).setMeshDevice(device.getMeshDevice());
            result = true;

        } else {

            devices.add(device);
        }

        this.notifyDataSetChanged();
        return result;
    }

    public void updateDeviceType(int deviceAddress, MeshDeviceType deviceType, byte[] macData) {

        for (MyDevice device : devices) {

            if (device.getMeshDevice().getAddress() == deviceAddress) {
                device.setDeviceType(deviceType);
                device.setMacData(macData);
            }
        }

        this.notifyDataSetChanged();
    }

    public interface OnClickListener {
        void onItemClick(ViewHolder holder, int position, MyDevice device);
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ArrayList<MyDevice> getDevices() {
        return devices;
    }
}

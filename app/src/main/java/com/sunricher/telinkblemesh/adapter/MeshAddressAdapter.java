package com.sunricher.telinkblemesh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sunricher.telinkblemesh.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MeshAddressAdapter extends RecyclerView.Adapter<MeshAddressAdapter.ViewHolder> {

    private OnClickListener clickListener;
    private List<Integer> availableAddressList;
    private List<Integer> existAddressList;

    public MeshAddressAdapter() {

        this.availableAddressList = new ArrayList<>();
        this.existAddressList = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mesh_addres_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MeshAddressAdapter.ViewHolder holder, int position) {

        int address = getAddress(position);
        boolean isExists = isExistsAddress(position);

        holder.getAddressLabel().setText("" + address);
        holder.getRemoveButton().setVisibility(isExists ? View.VISIBLE : View.INVISIBLE);
        holder.getRemoveButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null) {
                    clickListener.onItemClick(holder, position, address);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return existAddressList.size() + availableAddressList.size();
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void removeAt(int position) {
        if (isExistsAddress(position)) {
            int address = existAddressList.get(position);
            existAddressList.remove(position);
            availableAddressList.add(address);
        }
    }

    public void setAvailableAddressList(List<Integer> availableAddressList) {
        this.availableAddressList = availableAddressList;
    }

    public void setExistAddressList(List<Integer> existAddressList) {
        this.existAddressList = existAddressList;
    }

    private int getAddress(int position) {
        int address = 0;
        if (!isExistsAddress(position)) {
            address = availableAddressList.get(position - existAddressList.size());
        } else {
            address = existAddressList.get(position);
        }
        return address;
    }

    private boolean isExistsAddress(int position) {
        return position < existAddressList.size();
    }

    public interface OnClickListener {

        void onItemClick(ViewHolder holder, int position, int address);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView addressLabel;
        private final Button removeButton;

        public ViewHolder(View itemView) {
            super(itemView);

            addressLabel = itemView.findViewById(R.id.address_tv);
            removeButton = itemView.findViewById(R.id.remove_btn);
        }

        public TextView getAddressLabel() {
            return addressLabel;
        }

        public Button getRemoveButton() {
            return removeButton;
        }
    }
}

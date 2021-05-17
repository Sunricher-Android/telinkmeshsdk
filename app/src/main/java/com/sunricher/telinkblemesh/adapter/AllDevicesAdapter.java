package com.sunricher.telinkblemesh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemeshlib.MeshNode;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class AllDevicesAdapter extends RecyclerView.Adapter<AllDevicesAdapter.ViewHolder> {

    private ArrayList<MeshNode> nodes;
    private OnClickListener clickListener;

    public AllDevicesAdapter() {

        this.nodes = new ArrayList<>();
    }

    public void clear() {
        nodes.clear();
        this.notifyDataSetChanged();
    }

    public void addNode(MeshNode node) {

        if (nodes.contains(node)) {
            return;
        }

        nodes.add(node);
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_detail_vertical_item, parent, false);

        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (clickListener != null) {
                    clickListener.onItemClick(holder, position);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(AllDevicesAdapter.ViewHolder holder, int position) {

        MeshNode node = nodes.get(position);

        String text = "" + node.getName() + " " + String.format("[0x%02X] [0x%04X]", node.getShortAddress(), node.getProductId());
        holder.getTextLabel().setText(text);
        holder.getDetailLabel().setText(node.getMacAddress());
    }

    @Override
    public int getItemCount() {

        return nodes.size();
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

    public interface OnClickListener {
        void onItemClick(ViewHolder holder, int position);
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }
}

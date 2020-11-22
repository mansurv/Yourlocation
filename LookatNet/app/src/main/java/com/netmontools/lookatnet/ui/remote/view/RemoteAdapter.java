package com.netmontools.lookatnet.ui.remote.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netmontools.lookatnet.R;
import com.netmontools.lookatnet.ui.remote.model.RemoteModel;

import java.util.ArrayList;
import java.util.List;

public class RemoteAdapter extends RecyclerView.Adapter<com.netmontools.lookatnet.ui.remote.view.RemoteAdapter.RemoteHolder> {
    private List<RemoteModel> hosts = new ArrayList<>();
    private com.netmontools.lookatnet.ui.remote.view.RemoteAdapter.OnItemClickListener listener;

    @NonNull
    @Override
    public com.netmontools.lookatnet.ui.remote.view.RemoteAdapter.RemoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.remote_item, parent, false);
        return new com.netmontools.lookatnet.ui.remote.view.RemoteAdapter.RemoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull com.netmontools.lookatnet.ui.remote.view.RemoteAdapter.RemoteHolder holder, int position) {
        RemoteModel currentPoint = hosts.get(position);
        holder.textViewName.setText(currentPoint.getName());
        holder.textViewAddress.setText(currentPoint.getAddr());
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }
    public void setHosts(List<RemoteModel> hosts) {
        this.hosts = hosts;
        notifyDataSetChanged();
    }

    public RemoteModel getHostAt(int position) {
        return hosts.get(position);
    }

    class RemoteHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewAddress;

        public RemoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewAddress = itemView.findViewById(R.id.text_view_addr);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(hosts.get(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RemoteModel point);
    }

    public void setOnItemClickListener(com.netmontools.lookatnet.ui.remote.view.RemoteAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}

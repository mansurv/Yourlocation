package com.netmontools.lookatnet.ui.point.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netmontools.lookatnet.R;
import com.netmontools.lookatnet.ui.point.model.DataModel;

import java.util.ArrayList;
import java.util.List;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.PointHolder> {
    private List<DataModel> points = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public PointHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.point_item, parent, false);

        final PointHolder holder = new PointHolder(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(points.get(position));
                }
            }
        });
        //return new PointHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PointHolder holder, int position) {
        DataModel currentPoint = points.get(position);
        holder.textViewTitle.setText(currentPoint.getName());
        holder.textViewDescription.setText(currentPoint.getBssid());
        holder.textViewLatitude.setText(String.valueOf(currentPoint.getLat()));
        holder.textViewLongitude.setText(String.valueOf(currentPoint.getLon()));
    }

    @Override
    public int getItemCount() {
        return points.size();
    }
    public void setPoints(List<DataModel> points) {
        this.points = points;
        notifyDataSetChanged();
    }

    public DataModel getPointAt(int position) {
        return points.get(position);
    }

    class PointHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewLatitude;
        private TextView textViewLongitude;

        public PointHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewLatitude = itemView.findViewById(R.id.text_view_lat);
            textViewLongitude = itemView.findViewById(R.id.text_view_lon);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DataModel point);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}


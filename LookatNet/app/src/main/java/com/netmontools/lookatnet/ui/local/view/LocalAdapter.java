package com.netmontools.lookatnet.ui.local.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netmontools.lookatnet.R;
import com.netmontools.lookatnet.ui.local.model.Folder;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;

public class LocalAdapter extends RecyclerView.Adapter<LocalAdapter.LocalHolder> {

    private static final int TYPE_FILE = 0;
    private static final int TYPE_FILE_CHECKED = 1;
    private List<Folder> points = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    @NonNull
    @Override
    public LocalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

       if (viewType == TYPE_FILE){
            itemView = layoutInflater.inflate(R.layout.local_item, parent, false);
        } else{
            itemView = layoutInflater.inflate(R.layout.local_checked_item, parent, false);
        }


        final LocalHolder holder = new LocalHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(points.get(position));
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                if (longClickListener != null && position != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(points.get(position));
                }
                return false;
            }
        });

        //return new LocalHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull com.netmontools.lookatnet.ui.local.view.LocalAdapter.LocalHolder holder, int position) {
        Folder currentPoint = points.get(position);

        holder.textViewTitle.setText(currentPoint.getName());
        holder.textViewSize.setText(currentPoint.getSize() == 0 ? "" : valueOf(currentPoint.getSize()));
        holder.imageView.setImageDrawable(currentPoint.getImage());
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (points.get(position).isChecked)
            return TYPE_FILE_CHECKED;
        else
            return TYPE_FILE;
    }

    public void setPoints(List<Folder> points) {
        this.points = points;
        notifyDataSetChanged();
    }

    public Folder getPointAt(int position) {
        return points.get(position);
    }

    class LocalHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewSize;
        private ImageView imageView;

        public LocalHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewSize = itemView.findViewById(R.id.text_view_size);
            imageView = itemView.findViewById(R.id.local_image_view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Folder point);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Folder point);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
}



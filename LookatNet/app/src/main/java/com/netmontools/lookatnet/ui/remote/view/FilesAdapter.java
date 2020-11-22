package com.netmontools.lookatnet.ui.remote.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netmontools.lookatnet.R;
import com.netmontools.lookatnet.ui.remote.model.RemoteFolder;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesHolder> {

    private static final int TYPE_FILE = 0;
    private static final int TYPE_FILE_CHECKED = 1;
    private List<RemoteFolder> points = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    @NonNull
    @Override
    public FilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_FILE){
            itemView = layoutInflater.inflate(R.layout.local_item, parent, false);
        } else{
            itemView = layoutInflater.inflate(R.layout.local_checked_item, parent, false);
        }


        final FilesHolder holder = new FilesHolder(itemView);
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

        //return new FilesHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull com.netmontools.lookatnet.ui.remote.view.FilesAdapter.FilesHolder holder, int position) {
        RemoteFolder currentPoint = points.get(position);

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

    public void setPoints(List<RemoteFolder> points) {
        this.points = points;
        notifyDataSetChanged();
    }

    public RemoteFolder getPointAt(int position) {
        return points.get(position);
    }

    class FilesHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewSize;
        private ImageView imageView;

        public FilesHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewSize = itemView.findViewById(R.id.text_view_size);
            imageView = itemView.findViewById(R.id.local_image_view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RemoteFolder point);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(RemoteFolder point);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
}

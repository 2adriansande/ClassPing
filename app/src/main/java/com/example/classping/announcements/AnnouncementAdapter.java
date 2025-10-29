package com.example.classping.announcements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.classping.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {
    private Context context;
    private List<Announcement> list;
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDelete(String id);
    }

    public AnnouncementAdapter(Context context, List<Announcement> list, OnDeleteClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Announcement a = list.get(position);
        holder.tvTitle.setText(a.getTitle());
        holder.tvMessage.setText(a.getMessage());
        String date = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(a.getTimestamp());
        holder.tvMeta.setText(a.getAuthor() + " | " + a.getDepartment() + " | " + date);
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(a.getId()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvMeta;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

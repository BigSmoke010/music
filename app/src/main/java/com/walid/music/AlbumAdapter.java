package com.walid.music;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private List<Album> albums;
    private List<String> albumNames;
    private OnItemClickListener onItemClickListener;


    public AlbumAdapter(List<Album> albums,OnItemClickListener listener) {
        this.albums = albums;
        this.onItemClickListener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album data = albums.get(position);
        holder.albumNameTextView.setText(data.getName());
        holder.albumArtistTextView.setText(data.getArtist());
        Uri albumArt = data.getAlbumArtUri();
        Picasso.get().load(albumArt).error(R.mipmap.music).into(holder.albumCoverImageView);
        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(data);
            }
        });

    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView albumCoverImageView;
        TextView albumNameTextView;
        TextView albumArtistTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumCoverImageView = itemView.findViewById(R.id.albumCoverImageView);
            albumNameTextView = itemView.findViewById(R.id.albumNameTextView);
            albumArtistTextView = itemView.findViewById(R.id.albumArtistTextView);
        }
    }
}
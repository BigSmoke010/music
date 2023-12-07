package com.walid.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private final MainActivity mainActivity;

    private List<Song> dataList;
    private final Context context;

    private Song currentSong;

    public RecyclerAdapter(Context context, List<Song> dataList, MainActivity mainactivity) {
        this.context = context;
        this.dataList = dataList;
        this.mainActivity = mainactivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song item = dataList.get(position);
        holder.title.setText(item.getTitle());
        holder.title.setSelected(true);
        holder.artist.setText(item.getArtist());
        loadAlbumArt(Integer.parseInt(String.valueOf(item.getId())), holder.cover);
        holder.itemView.setOnClickListener(view -> {
            mainActivity.currentSongIndex = 0;
            currentSong = item;
            List<Song> queue = generateQueue();
            mainActivity.playAudioFile(item.getData(), queue);
            mainActivity.showBottomSheet(item.getArtist(), item.getTitle(), Uri.parse("content://media/external/audio/media/" + item.getId() + "/albumart"), queue, false);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size() + 2;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView cover;
        public TextView artist;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
            cover = itemView.findViewById(R.id.cover);
            artist = itemView.findViewById(R.id.artist_name);
        }
    }

    public List<Song> generateQueue() {
        List<Song> queue = new ArrayList<>();
        queue.add(currentSong);
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            int randomint = random.nextInt(dataList.size());
            queue.add(dataList.get(randomint));
        }
        return queue;
    }
    public void updateData(List<Song> updatedSongs) {
        this.dataList = updatedSongs;
        notifyDataSetChanged();
    }
    public void loadAlbumArt(int songId, ImageView view) {
        Uri artworkUri = Uri.parse("content://media/external/audio/media/" + songId + "/albumart");
        Picasso.get().load(artworkUri).error(R.mipmap.music).into(view);
    }

}
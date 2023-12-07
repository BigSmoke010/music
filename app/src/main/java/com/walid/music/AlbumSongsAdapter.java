package com.walid.music;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class AlbumSongsAdapter extends RecyclerView.Adapter<AlbumSongsAdapter.ViewHolder> {

    private List<Song> albumSongs;
    private Context context;
    private MainActivity mainActivity;
    public AlbumSongsAdapter(List<Song> albumSongs, Context context, MainActivity mainActivity) {
        this.albumSongs = albumSongs;
        this.context = context;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = albumSongs.get(position);
        holder.SongNumber.setText(String.valueOf(position + 1));
        holder.SongTitle.setText(song.getTitle());
        holder.itemView.setOnClickListener(view -> {
            mainActivity.currentSongIndex = 0;
            List<Song> queue = albumSongs.subList(albumSongs.indexOf(song), albumSongs.size());
            mainActivity.playAudioFile(song.getData(), queue);
            mainActivity.showBottomSheet(song.getArtist(), song.getTitle(), Uri.parse("content://media/external/audio/media/" + song.getId() + "/albumart"), queue, false);

        });
    }

    @Override
    public int getItemCount() {
        return albumSongs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView SongNumber;
        public TextView SongTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            SongNumber = itemView.findViewById(R.id.number);
            SongTitle = itemView.findViewById(R.id.songTitle);
        }
    }
}

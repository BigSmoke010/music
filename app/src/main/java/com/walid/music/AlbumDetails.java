package com.walid.music;

import static com.google.android.material.color.MaterialColors.isColorLight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlbumDetails extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AlbumSongsAdapter adapter;
    private List<Song> allSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        Album album = getIntent().getParcelableExtra("albumData");
        AudioHelper audioHelper = new AudioHelper();
        allSongs = audioHelper.getAllAudioFiles(this);
        ImageView albumCover = findViewById(R.id.albumAlbumCover);
        recyclerView = findViewById(R.id.albumSongsRecyclerView);
        TextView albumName = findViewById(R.id.albumName);
        TextView albumArtist = findViewById(R.id.albumArtistName);
        TextView albumDate = findViewById(R.id.albumDate);
        Button shuffleBtn = findViewById(R.id.shuffleButton);
        assert album != null;
        Uri albumArt = album.getAlbumArtUri();
        Bitmap mipmapBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.music);

        Drawable mipmapDrawable = new BitmapDrawable(getResources(), mipmapBitmap);

        Picasso.get().load(albumArt).error(mipmapDrawable).into(albumCover);
        albumArtist.setText(album.getArtist());
        albumName.setText(album.getName());
        albumDate.setText(album.getYear());
        List<Song> albumSongs = album.getSongs();
        AppBarLayout appBarLayout = findViewById(R.id.appbarlay);
        AppBarLayout.LiftOnScrollListener liftOnScrollListener = (elevation, backgroundColor) -> appBarLayout.setBackgroundColor(backgroundColor);
        Bitmap albumCoverBitmap;
        try {
            albumCoverBitmap = ((BitmapDrawable) albumCover.getDrawable()).getBitmap();
        } catch (Exception e) {
            albumCoverBitmap = mipmapBitmap;
        }
        Palette.from(albumCoverBitmap).generate(palette -> {
            assert palette != null;
            int averageColor = palette.getDominantColor(ContextCompat.getColor(AlbumDetails.this, R.color.default_color));
            int secondaryColor = palette.getVibrantColor(ContextCompat.getColor(AlbumDetails.this, R.color.default_secondary_color));
            int textColor = isColorDark(averageColor) ? Color.WHITE : Color.BLACK;
            boolean isLightBackground = isColorLight(averageColor);

            Window window = getWindow();
            View decorView = window.getDecorView();
            liftOnScrollListener.onUpdate(0, averageColor);
            window.setStatusBarColor(averageColor);
            albumName.setTextColor(textColor);
            albumArtist.setTextColor(textColor);
            albumDate.setTextColor(textColor);
            if (secondaryColor == averageColor) {
                shuffleBtn.setBackgroundColor(Color.parseColor("#52A0FF"));
                shuffleBtn.setTextColor(textColor);
            } else {
                shuffleBtn.setBackgroundColor(secondaryColor);
                shuffleBtn.setTextColor(averageColor);
            }
            if (isLightBackground) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(0);
            }
        });

        appBarLayout.addLiftOnScrollListener(liftOnScrollListener);

        albumSongs.sort(Comparator.comparing(song -> {
            String data = Objects.requireNonNull(song.getData(), "0");
            String[] parts = data.split("/");
            String filename = parts[parts.length - 1];
            Matcher matcher = Pattern.compile("\\d+").matcher(filename);

            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            } else {
                return 0;
            }
        }));
        shuffleBtn.setOnClickListener(view -> {
            Random random = new Random();
            int randomInt = random.nextInt(albumSongs.size());
            Song chosenSong = albumSongs.get(randomInt);
            List<Song> chosenQueue= MainActivity.getInstance().generateQueue(chosenSong);
            MainActivity.getInstance().playAudioFile(chosenSong.getData(), chosenQueue);
            MainActivity.getInstance().showBottomSheet(chosenSong.getArtist(), chosenSong.getTitle(), Uri.parse("content://media/external/audio/media/" + chosenSong.getId() + "/albumart"), chosenQueue, false);
        });
        adapter = new AlbumSongsAdapter(albumSongs, this, MainActivity.getInstance());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }
}
package com.walid.music;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MediaPlaybackService extends Service {
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private MediaPlayer nextMediaPlayer;
    private MediaSession mediaSession;
    private int currentSongIndex;
    private PendingIntent playPausePendingIntent;
    private List<Song> serviceQueue;
    private List<Song> allSongs;
    private NotificationCompat.Builder builder;
    private Song currentSong;
    private Handler handler;
    private final String CHANNEL_ID = "musicapp";
    public static final String ACTION_SKIP_SONG = "com.walid.music.ACTION_SKIP_SONG";
    public static final String ACTION_PREVIOUS_SONG = "com.walid.music.ACTION_PREVIOUS_SONG";
    public static final String ACTION_TOGGLE_PLAY_PAUSE = "com.walid.music.ACTION_TOGGLE_PLAY_PAUSE";

    public class LocalBinder extends Binder {
        MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        mediaSession = new MediaSession(this, "MusicTag");
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPause() {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            }

            @Override
            public void onSkipToNext() {
                if (serviceQueue != null) {
                    skipSong(serviceQueue);
                    preloadSong(serviceQueue);
                    createNotification(true, currentSong, serviceQueue);
                }
            }

            @Override
            public void onSkipToPrevious() {
                if (serviceQueue != null) {
                    previousSong(serviceQueue);
                    preloadSong(serviceQueue);
                    createNotification(true, currentSong, serviceQueue);
                }
            }

            @Override
            public void onSeekTo(long seek) {
                mediaPlayer.seekTo((int) seek);
            }
        });

    }

    private Intent getPlayPauseIntent() {
        Intent intent = new Intent(this, MediaPlaybackService.class);
        intent.setAction(mediaPlayer.isPlaying() ? "PAUSE" : "RESUME");
        return intent;
    }

    @SuppressLint("RestrictedApi")
    private void updatePlayPauseAction(boolean isPlaying) {
        if (builder != null) {
            int playPauseIcon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
            String playPauseTitle = isPlaying ? "Pause" : "Play";
            builder.mActions.set(1, new NotificationCompat.Action.Builder(
                    playPauseIcon,
                    playPauseTitle,
                    playPausePendingIntent
            ).build());
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.notify(1, builder.build());
        }
    }

    @SuppressLint("NewApi")
    private void createNotification(boolean isPlaying, Song currentSong, List<Song> queue) {
        int playPauseIcon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
        String playPauseTitle = isPlaying ? "Pause" : "Play";
        playPausePendingIntent = PendingIntent.getService(
                this,
                0,
                getPlayPauseIntent(),
                PendingIntent.FLAG_MUTABLE
        );
        Gson gson = new Gson();
        String jsonQueue = gson.toJson(queue);
        Intent previousIntent = new Intent(this, MediaPlaybackService.class);
        previousIntent.setAction("PREVIOUS");
        previousIntent.putExtra("QUEUE", jsonQueue);

        PendingIntent previousPendingIntent = PendingIntent.getService(this, 1, previousIntent, PendingIntent.FLAG_MUTABLE);

        Intent nextIntent = new Intent(this, MediaPlaybackService.class);
        nextIntent.setAction("SKIP");
        nextIntent.putExtra("QUEUE", jsonQueue);

        Intent closeIntent = new Intent(this, MediaPlaybackService.class);
        closeIntent.setAction("CLOSE");
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_MUTABLE);
        PendingIntent closePendingIntent = PendingIntent.getService(this, 3, closeIntent, PendingIntent.FLAG_MUTABLE);
        Bitmap image;

        try {
            byte[] imgArt = currentSong.getArt();
            if (imgArt != null) {
                image = BitmapFactory.decodeByteArray(imgArt, 0, imgArt.length);
            } else {
                image = BitmapFactory.decodeResource(getResources(), R.mipmap.music);
            }
        } catch (Exception ignored) {
            image = BitmapFactory.decodeResource(getResources(), R.mipmap.music);
        }
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.music_logo)
                .setContentTitle(currentSong.getTitle())
                .setContentText(currentSong.getArtist())
                .setLargeIcon(image)
                .addAction(R.drawable.ic_noti_previous, "Previous", previousPendingIntent)
                .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
                .addAction(R.drawable.ic_noti_next, "Next", nextPendingIntent)
                .addAction(R.drawable.ic_close, "Close", closePendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken()))
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        startForeground(1, notification);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            "musicchannel",
                            NotificationManager.IMPORTANCE_LOW
                    );
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);
                }
                boolean fromGui = intent.getBooleanExtra("from_gui", false);
                switch (action) {
                    case "PLAY": {
                        String filePath = intent.getStringExtra("FILE_PATH");
                        String jsonQueue = intent.getStringExtra("QUEUE");
                        String jsonAllSongs = intent.getStringExtra("ALL_SONGS");
                        if (jsonQueue != null) {
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<Song>>() {
                            }.getType();
                            serviceQueue = gson.fromJson(jsonQueue, type);
                            allSongs = gson.fromJson(jsonAllSongs, type);
                            currentSongIndex = 0;
                            currentSong = serviceQueue.get(0);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                playAudioFile(filePath);
                            }
                            createNotification(true, currentSong, serviceQueue);
                            preloadSong(serviceQueue);
                        }
                        break;
                    }
                    case "PAUSE":
                        pausePlayback();
                        if (!fromGui) {
                            Intent pause = new Intent(ACTION_TOGGLE_PLAY_PAUSE);
                            sendBroadcast(pause);
                        }
                        updatePlayPauseAction(mediaPlayer.isPlaying());
                        break;
                    case "RESUME":
                        resumePlayback();
                        if (!fromGui) {
                            Intent resume = new Intent(ACTION_TOGGLE_PLAY_PAUSE);
                            sendBroadcast(resume);
                        }
                        updatePlayPauseAction(mediaPlayer.isPlaying());
                        break;
                    case "UPDATE_PROG":
                        int progress = intent.getIntExtra("PROGRESS", 0);
                        mediaPlayer.seekTo(progress);
                        break;
                    case "SKIP": {
                        if (serviceQueue != null) {
                            if (!fromGui) {
                                Intent skipIntent = new Intent(ACTION_SKIP_SONG);
                                Gson gson = new Gson();
                                String jsonQueue = gson.toJson(serviceQueue);
                                skipIntent.putExtra("QUEUE", jsonQueue);
                                sendBroadcast(skipIntent);
                            }
                            currentSong = serviceQueue.get(currentSongIndex);
                            skipSong(serviceQueue);
                            preloadSong(serviceQueue);
                            createNotification(true, currentSong, serviceQueue);
                        }
                        break;
                    }
                    case "PREVIOUS": {
                        if (serviceQueue != null) {
                            if (!fromGui) {
                                Gson gson = new Gson();
                                String jsonQueue = gson.toJson(serviceQueue);
                                Intent previousIntent = new Intent(ACTION_PREVIOUS_SONG);
                                previousIntent.putExtra("QUEUE", jsonQueue);
                                sendBroadcast(previousIntent);
                            }
                            currentSong = serviceQueue.get(currentSongIndex);
                            previousSong(serviceQueue);
                            preloadSong(serviceQueue);
                            updatePlaybackState(true);
                            createNotification(true, currentSong, serviceQueue);
                        }
                        break;
                    }
                    case "CLOSE": {
                        System.out.println("closed!");
                        stopService(new Intent(this, MediaPlaybackService.class));
                        break;
                    }

                }
            }
        }
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void updatePlaybackState(boolean isPlaying) {
        int state = isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;
        MediaMetadata.Builder metadata = new MediaMetadata.Builder();
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(currentSong.getData());
            metadata.putString(MediaMetadata.METADATA_KEY_TITLE, retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            metadata.putString(MediaMetadata.METADATA_KEY_ARTIST, retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            metadata.putString(MediaMetadata.METADATA_KEY_ALBUM, retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            byte[] albumArtBytes = retriever.getEmbeddedPicture();
            Bitmap albumArt;

            if (albumArtBytes != null) {
                albumArt = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length);
            } else {
                albumArt = BitmapFactory.decodeResource(getResources(), R.mipmap.music);
            }

            metadata.putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))));
            metadata.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt);

            retriever.release();
        } catch (Exception e) {
            Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.mipmap.music);
            metadata.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt);
        }
        if (metadata.build() != null) {
            mediaSession.setMetadata(metadata.build());
        }

        PlaybackState.Builder playbackStateBuilder = new PlaybackState.Builder();
        playbackStateBuilder.setState(state, mediaPlayer.getCurrentPosition(), 1);
        playbackStateBuilder.setActions(PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SEEK_TO);

        PlaybackState playbackState = playbackStateBuilder.build();

        mediaSession.setPlaybackState(playbackState);

        if (builder != null) {
            builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken()))
                    .setShowActionsInCompactView(0, 1, 2));
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int totalDuration = mediaPlayer.getDuration();
            Intent intent = new Intent("UPDATE_PROGRESS");
            intent.putExtra("CURRENT_POSITION", currentPosition);
            intent.putExtra("TOTAL_DURATION", totalDuration);
            sendBroadcast(intent);
        }
        handler.postDelayed(this::updateSeekBar, 1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void playAudioFile(String filePath) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (Exception ignored) {
        }
        Bitmap image;

        try {
            byte[] imgArt = currentSong.getArt();
            if (imgArt != null) {
                image = BitmapFactory.decodeByteArray(imgArt, 0, imgArt.length);
            } else {
                image = BitmapFactory.decodeResource(getResources(), R.mipmap.music);
            }
        } catch (Exception ignored) {
            image = BitmapFactory.decodeResource(getResources(), R.mipmap.music);
        }

        MediaMetadata metadata = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentSong.getTitle())
                .putString(MediaMetadata.METADATA_KEY_ARTIST, currentSong.getArtist())
                .putString(MediaMetadata.METADATA_KEY_ALBUM, currentSong.getAlbum())
                .putLong(MediaMetadata.METADATA_KEY_DURATION, currentSong.getDuration())
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, image).build();

        PlaybackState.Builder playbackStateBuilder = new PlaybackState.Builder();
        playbackStateBuilder.setState(PlaybackState.STATE_PLAYING, 0, 1);
        playbackStateBuilder.setActions(PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SEEK_TO);
        PlaybackState playbackState = playbackStateBuilder.build();
        mediaSession.setMetadata(metadata);
        mediaSession.setPlaybackState(playbackState);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
                focusChange -> {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            // Handle audio focus gain
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            // Handle audio focus loss
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            // Handle transient audio focus loss
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            // Handle transient audio focus loss with ducking
                            break;
                    }
                };

        int result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mediaPlayer1 -> skipSong(serviceQueue));
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        }
        mediaSession.setActive(true);
        updateSeekBar();

    }

    public void pausePlayback() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                updatePlaybackState(false);
            }
        }
    }

    public void resumePlayback() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                updatePlaybackState(true);
            }
        }
    }

    public void skipSong(List<Song> queue) {

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying() || !mediaPlayer.isPlaying()) {
                mediaPlayer.reset();
            }
            mediaPlayer.release();
        }

        mediaPlayer = nextMediaPlayer;
        mediaPlayer.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            playAudioFile(queue.get(currentSongIndex).getData());
        }
        currentSong = queue.get(currentSongIndex);
        Random random = new Random();
        int randomint = random.nextInt(allSongs.size());
        serviceQueue.add(allSongs.get(randomint));
    }

    private void preloadSong(List<Song> queue) {
        nextMediaPlayer = new MediaPlayer();
        if (queue.size() > 0) {
            try {
                currentSongIndex += 1;
                if (currentSongIndex < queue.size()) {
                    nextMediaPlayer.setDataSource(queue.get(currentSongIndex).getData());
                    nextMediaPlayer.prepareAsync();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert mediaPlayer != null;
            mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
                Intent playIntent = new Intent(this, MediaPlaybackService.class);
                playIntent.setAction("SKIP");
                Gson gson = new Gson();
                String jsonQueue = gson.toJson(serviceQueue);
                playIntent.putExtra("QUEUE", jsonQueue);
                playIntent.putExtra("from_gui", false);
                startService(playIntent);
            });
        }
    }

    private void previousSong(List<Song> queue) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }

        currentSongIndex -= 2;

        if (currentSongIndex < 0) {
            currentSongIndex = 0;
        }
        mediaPlayer = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            playAudioFile(queue.get(currentSongIndex).getData());
        }
        currentSong = queue.get(currentSongIndex);
    }

    @Override
    public void onDestroy() {
        System.out.println("destroyoing!!!!!!");
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaSession.setActive(false);
    }
}

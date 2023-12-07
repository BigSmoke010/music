package com.walid.music;

import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.splashscreen.SplashScreen;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ViewPager2 viewPager2;
    TabLayout tab;
    FragmentViewAdapter viewadapter;
    private AnimatedVectorDrawableCompat avd;
    private AnimatedVectorDrawableCompat avd2;
    private ImageView miniCover;
    private TextView miniTitle;
    private ImageView pause;
    private ImageView coverart;
    private CardView cardView;
    private TextView song_titlebot;
    private TextView artistName;
    private ImageView playBackPause;
    private ImageView playBackPauseBg;
    private ImageView skip;
    private ImageView previous;
    int currentSongIndex = 0;
    private static MainActivity instance;
    private FloatingActionButton shuffle;
    private TextView durationText;
    private TextView progressText;
    private ConstraintLayout playBackPauseLayout;
    private SeekBar seekBar;
    private List<Song> allSongs;
    private boolean songPaused = true;
    public static final String ACTION_SKIP_SONG = "com.walid.music.ACTION_SKIP_SONG";
    public static final String ACTION_PREVIOUS_SONG = "com.walid.music.ACTION_PREVIOUS_SONG";
    public static final String ACTION_TOGGLE_PLAY_PAUSE = "com.walid.music.ACTION_TOGGLE_PLAY_PAUSE";

    private final BroadcastReceiver serviceActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                String jsonQueue = intent.getStringExtra("QUEUE");
                Gson gson = new Gson();
                Type type = new TypeToken<List<Song>>() {
                }.getType();
                List<Song> serviceQueue = gson.fromJson(jsonQueue, type);
                switch (intent.getAction()) {
                    case ACTION_SKIP_SONG:
                        assert serviceQueue != null;
                        skipSong(serviceQueue, true);
                        break;
                    case ACTION_PREVIOUS_SONG:
                        previousSong(serviceQueue, true);
                        break;
                    case ACTION_TOGGLE_PLAY_PAUSE:
                        togglePlayPause();
                        break;
                }
            }
        }
    };
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            MediaPlaybackService myService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private final BroadcastReceiver playbackUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("UPDATE_PROGRESS")) {
                int currentPosition = intent.getIntExtra("CURRENT_POSITION", 0);
                int totalDuration = intent.getIntExtra("TOTAL_DURATION", 1);
                seekBar.setProgress(currentPosition);
                seekBar.setMax(totalDuration);
                progressText.setText(formatTime(currentPosition / 1000));
                durationText.setText(formatTime(totalDuration / 1000));
            }
        }
    };
    private final String[] names = {"Home", "Albums"};

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        viewPager2 = findViewById(R.id.pager);
        tab = findViewById(R.id.tabLayout);
        skip = findViewById(R.id.skip);
        previous = findViewById(R.id.previous);
        miniCover = findViewById(R.id.miniCover);
        miniTitle = findViewById(R.id.miniTitle);
        shuffle = findViewById(R.id.shuffleButton);
        pause = findViewById(R.id.minipause);
        tab.setTabTextColors(Color.parseColor("#808080"), Color.parseColor("#00BCD4"));
        tab.setSelectedTabIndicatorColor(Color.parseColor("#00BCD4"));
        cardView = findViewById(R.id.cardView);
        pause.setVisibility(View.GONE);
        playBackPause = findViewById(R.id.PlaybackPause);
        playBackPauseBg = findViewById(R.id.PlaybackPauseBg);
        durationText = findViewById(R.id.duration);
        artistName = findViewById(R.id.artistName);
        coverart = findViewById(R.id.coverart);
        seekBar = findViewById(R.id.seekBar);
        progressText = findViewById(R.id.currentProgress);
        playBackPauseLayout = findViewById(R.id.playbackPauseLayout);
        song_titlebot = findViewById(R.id.song_titlebot);
        song_titlebot.setSelected(true);
        viewadapter = new FragmentViewAdapter(this);
        viewPager2.setAdapter(viewadapter);
        AudioHelper audioHelper = new AudioHelper();
        allSongs = audioHelper.getAllAudioFiles(this);
        Intent intent = new Intent(this, MediaPlaybackService.class);
        if (isMyServiceRunning()) {
            retrieveDataFromSharedPreferences();
        } else {
            showBottomSheet("", "", Uri.parse(""), new ArrayList<>(), false);
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SKIP_SONG);
        filter.addAction(ACTION_PREVIOUS_SONG);
        filter.addAction(ACTION_TOGGLE_PLAY_PAUSE);
        registerReceiver(serviceActionsReceiver, filter);
        IntentFilter progressfilter = new IntentFilter("UPDATE_PROGRESS");
        registerReceiver(playbackUpdateReceiver, progressfilter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        shuffle.setOnClickListener(view -> {
            Random random = new Random();
            int randomInt = random.nextInt(allSongs.size());
            Song chosenSong = allSongs.get(randomInt);
            List<Song> chosenQueue= generateQueue(chosenSong);
            playAudioFile(chosenSong.getData(), chosenQueue);
            showBottomSheet(chosenSong.getArtist(), chosenSong.getTitle(), Uri.parse("content://media/external/audio/media/" + chosenSong.getId() + "/albumart"), chosenQueue, false);
        });
        Objects.requireNonNull(getSupportActionBar()).setTitle("Music");
        toolbar.inflateMenu(R.menu.menu_main);
        new TabLayoutMediator(tab, viewPager2, ((tab, position) -> tab.setText(names[position]))).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public void handleSongAction(Song chosenSong, List<Song> chosenQueue, boolean isRandom) {
        if (isRandom) {
            playAudioFile(chosenSong.getData(), chosenQueue);
            showBottomSheet(chosenSong.getArtist(), chosenSong.getTitle(), Uri.parse("content://media/external/audio/media/" + chosenSong.getId() + "/albumart"), chosenQueue, false);
        } else {
            currentSongIndex = 0;
            List<Song> queue = albumSongs.subList(albumSongs.indexOf(chosenSong), albumSongs.size());
            playAudioFile(chosenSong.getData(), queue);
            showBottomSheet(chosenSong.getArtist(), chosenSong.getTitle(), Uri.parse("content://media/external/audio/media/" + chosenSong.getId() + "/albumart"), queue, false);
        }
    }
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%d:%02d", minutes, remainingSeconds);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MediaPlaybackService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void moveHorizontal(Float xdelta, ImageView view) {
        Animation moveRight = new TranslateAnimation(0, xdelta, 0, 0);
        moveRight.setDuration(200);
        moveRight.setRepeatCount(1);
        moveRight.setRepeatMode(Animation.REVERSE);

        moveRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setTranslationX(0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(moveRight);
    }

    public void showBottomSheet(String artist, String name, Uri cover, List<Song> queue, boolean skiporprevious) {
        MediaPlayer nextMediaPlayer = new MediaPlayer();
        avd2 = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_circletosquare);
        avd = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_playtopause);
        pause.setImageDrawable(avd);
        playBackPauseBg.setImageDrawable(avd2);
        playBackPause.setImageDrawable(avd);
        if (avd != null && avd2 != null) {
            avd.start();
            avd2.start();
        }
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
            nextMediaPlayer.setOnPreparedListener(mediaPlayer -> {

            });
        }
        skip.setOnClickListener(view -> skipSong(queue, false));
        previous.setOnClickListener(view -> previousSong(queue, false));
        ConstraintLayout bottomSheet = findViewById(R.id.bottom_sheet);
        if (Objects.equals(artist, "")) {
            bottomSheet.setVisibility(View.GONE);
        } else if (!skiporprevious) {
            Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            bottomSheet.setVisibility(View.VISIBLE);
            bottomSheet.startAnimation(slideUpAnimation);
            float scale = getResources().getDisplayMetrics().density;
            int bottomMarginDp = 65;
            int bottomMarginPx = (int) (bottomMarginDp * scale + 0.5f);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) viewPager2.getLayoutParams();
            layoutParams.bottomMargin = bottomMarginPx;
            viewPager2.setLayoutParams(layoutParams);
        } else {
            bottomSheet.setVisibility(View.VISIBLE);
        }
        BottomSheetBehavior<ConstraintLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheet.setOnClickListener(view -> behavior.setState(BottomSheetBehavior.STATE_EXPANDED));
        behavior.setPeekHeight(200);
        int state = behavior.getState();
        if (state == BottomSheetBehavior.STATE_COLLAPSED) {
            showCollapsedItems();
        } else if (state == BottomSheetBehavior.STATE_EXPANDED) {
            showExpandedItems();
        }
        pause.setOnClickListener(view -> togglePlayPause());
        playBackPauseLayout.setOnClickListener(view -> togglePlayPause());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent updateProgressIntent = new Intent(MainActivity.this, MediaPlaybackService.class);
                    updateProgressIntent.setAction("UPDATE_PROG");
                    updateProgressIntent.putExtra("PROGRESS", progress);
                    startService(updateProgressIntent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        if (!skiporprevious) {
            Picasso.get().load(cover).error(R.mipmap.music).into(miniCover);
            Picasso.get().load(cover).error(R.mipmap.music).into(coverart);
        }
        miniTitle.setText(name);
        song_titlebot.setText(name);
        artistName.setText(artist);

        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    showCollapsedItems();
                    showOverlay();

                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    showExpandedItems();
                    hideOverlay();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                updateAlpha(slideOffset);
            }
        });
        saveDataToSharedPreferences(artist, name, cover, queue);

    }

    public void playAudioFile(String filepath, List<Song> queue) {
        Intent playIntent = new Intent(this, MediaPlaybackService.class);
        playIntent.setAction("PLAY");
        playIntent.putExtra("FILE_PATH", filepath);
        Gson gson = new Gson();
        String jsonQueue = gson.toJson(queue);
        String allSongsjson = gson.toJson(allSongs);
        playIntent.putExtra("QUEUE", jsonQueue);
        playIntent.putExtra("ALL_SONGS", allSongsjson);
        startService(playIntent);
        songPaused = false;
        currentSongIndex = 0;
        togglePlayPause();
    }
    public List<Song> generateQueue(Song currentSong) {
        List<Song> queue = new ArrayList<>();
        queue.add(currentSong);
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            int randomint = random.nextInt(allSongs.size());
            queue.add(allSongs.get(randomint));
        }
        return queue;
    }
    private void saveDataToSharedPreferences(String artist, String name, Uri cover, List<Song> queue) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("artist", artist);
        editor.putString("name", name);
        editor.putString("coverUri", cover.toString());
        editor.putString("queue", new Gson().toJson(queue));
        editor.putInt("currentSongindex", currentSongIndex - 1);
        editor.apply();
    }

    private void retrieveDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String artist = sharedPreferences.getString("artist", "");
        String name = sharedPreferences.getString("name", "");
        String coverUriString = sharedPreferences.getString("coverUri", "");
        currentSongIndex = sharedPreferences.getInt("currentSongindex", 0);
        songPaused = sharedPreferences.getBoolean("paused", false);
        Uri cover = Uri.parse(coverUriString);
        System.out.println(cover);
        String queueJson = sharedPreferences.getString("queue", "");
        List<Song> queue = new Gson().fromJson(queueJson, new TypeToken<List<Song>>() {
        }.getType());
        showBottomSheet(artist, name, cover, queue, false);
        if (!songPaused) {
            avd2 = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_squaretocircle);
            avd = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_pausetoplay);
        } else {
            avd2 = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_circletosquare);
            avd = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_playtopause);
        }

        pause.setImageDrawable(avd);
        playBackPauseBg.setImageDrawable(avd2);
        playBackPause.setImageDrawable(avd);
        if (avd != null && avd2 != null) {
            avd.start();
            avd2.start();
        }
    }

    private void previousSong(List<Song> queue, boolean updateguiOnly) {
        moveHorizontal(-10f, previous);
        songPaused = false;
        togglePlayPause();
        moveHorizontal(-10f, previous);
        if (!updateguiOnly) {
            Intent playIntent = new Intent(this, MediaPlaybackService.class);
            playIntent.setAction("PREVIOUS");
            Gson gson = new Gson();
            String jsonQueue = gson.toJson(queue);
            playIntent.putExtra("QUEUE", jsonQueue);
            playIntent.putExtra("from_gui", true);
            startService(playIntent);
        }
        currentSongIndex -= 2;

        if (currentSongIndex < 0) {
            currentSongIndex = 0;
        }
        showBottomSheet(queue.get(currentSongIndex).getArtist(), queue.get(currentSongIndex).getTitle(), Uri.parse("content://media/external/audio/media/" + queue.get(currentSongIndex).getId() + "/albumart"), queue, true);
        pause.setImageDrawable(avd);
        playBackPauseBg.setImageDrawable(avd2);
        playBackPause.setImageDrawable(avd);
        Animation previousAnim = AnimationUtils.loadAnimation(this, R.anim.previous);
        Animation previousAnim2 = AnimationUtils.loadAnimation(this, R.anim.previous2);
        cardView.startAnimation(previousAnim);

        previousAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.startAnimation(previousAnim2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        previousAnim2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                int previousSongIndex = currentSongIndex - 1;
                if (previousSongIndex >= 0) {
                    Uri previousCoverUri = Uri.parse("content://media/external/audio/media/" + queue.get(previousSongIndex).getId() + "/albumart");
                    Picasso.get().load(previousCoverUri).error(R.mipmap.music).into(miniCover);
                    Picasso.get().load(previousCoverUri).error(R.mipmap.music).into(coverart);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void skipSong(List<Song> queue, boolean updateguiOnly) {
        if (currentSongIndex < queue.size()) {
        moveHorizontal(10f, skip);
        songPaused = false;
        togglePlayPause();
        Uri nextCover = Uri.parse("content://media/external/audio/media/" + queue.get(currentSongIndex).getId() + "/albumart");

        showBottomSheet(queue.get(currentSongIndex).getArtist(), queue.get(currentSongIndex).getTitle(), nextCover, queue, true);
        Animation skipAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.skip);
        Animation skipAnim2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.skip2);
        cardView.startAnimation(skipAnim);
        if (!updateguiOnly) {
            Intent playIntent = new Intent(this, MediaPlaybackService.class);
            playIntent.setAction("SKIP");
            Gson gson = new Gson();
            Random random = new Random();
            int randomint = random.nextInt(allSongs.size());
            queue.add(allSongs.get(randomint));
            String jsonQueue = gson.toJson(queue);
            System.out.println(queue.size());
            playIntent.putExtra("QUEUE", jsonQueue);
            playIntent.putExtra("from_gui", true);
            startService(playIntent);
        }
        int nextSongIndex = currentSongIndex + 1;
        if (nextSongIndex < queue.size()) {
            Uri nextCoverUri = Uri.parse("content://media/external/audio/media/" + queue.get(nextSongIndex).getId() + "/albumart");
            Picasso.get().load(nextCoverUri).error(R.mipmap.music).fetch();
        }
        skipAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.startAnimation(skipAnim2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        skipAnim2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Picasso.get().load(nextCover).error(R.mipmap.music).into(miniCover);
                Picasso.get().load(nextCover).error(R.mipmap.music).into(coverart);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });}
    }

    private void showOverlay() {
        View overlayView = findViewById(R.id.overlayView);
        overlayView.setAlpha(0.6f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(overlayView, View.ALPHA, 1f);
        fadeIn.setDuration(300);
        fadeIn.start();
    }

    private void hideOverlay() {
        View overlayView = findViewById(R.id.overlayView);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(overlayView, View.ALPHA, 0.6f);
        fadeOut.setDuration(300);
        fadeOut.start();
    }

    private void togglePlayPause() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        songPaused = !songPaused;
        editor.putBoolean("paused", songPaused);
        editor.apply();
        if (!songPaused) {
            avd2 = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_squaretocircle);
            avd = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_pausetoplay);
            Intent pauseIntent = new Intent(this, MediaPlaybackService.class);
            pauseIntent.setAction("PAUSE");
            pauseIntent.putExtra("from_gui", true);
            startService(pauseIntent);
        } else {
            avd2 = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_circletosquare);
            avd = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_playtopause);
            Intent pauseIntent = new Intent(this, MediaPlaybackService.class);
            pauseIntent.setAction("RESUME");
            pauseIntent.putExtra("from_gui", true);
            startService(pauseIntent);
        }

        pause.setImageDrawable(avd);
        playBackPauseBg.setImageDrawable(avd2);
        playBackPause.setImageDrawable(avd);
        if (avd != null && avd2 != null) {
            avd.start();
            avd2.start();
        }

    }

    private void updateAlpha(float slideOffset) {
        float alphaCollapsed = 1 - slideOffset;
        miniCover.setAlpha(alphaCollapsed);
        miniTitle.setAlpha(alphaCollapsed);
        playBackPauseBg.setAlpha(slideOffset);
        playBackPause.setAlpha(slideOffset);
        coverart.setAlpha(slideOffset);
        cardView.setAlpha(slideOffset);
        pause.setAlpha(alphaCollapsed);
        song_titlebot.setAlpha(slideOffset);
        playBackPauseBg.setAlpha(slideOffset);
        playBackPause.setAlpha(slideOffset);
        skip.setAlpha(slideOffset);
        previous.setAlpha(slideOffset);
    }

    private void showCollapsedItems() {
        miniCover.setVisibility(View.VISIBLE);
        miniTitle.setVisibility(View.VISIBLE);
        pause.setVisibility(View.VISIBLE);
        coverart.setVisibility(View.GONE);
        playBackPauseBg.setVisibility(View.GONE);
        playBackPause.setVisibility(View.GONE);
        cardView.setVisibility(View.GONE);
        seekBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        durationText.setVisibility(View.GONE);
        playBackPauseBg.setVisibility(View.GONE);
        playBackPause.setVisibility(View.GONE);
        skip.setVisibility(View.GONE);
        previous.setVisibility(View.GONE);
        artistName.setVisibility(View.GONE);
        song_titlebot.setVisibility(View.GONE);
    }

    private void showExpandedItems() {
        miniCover.setVisibility(View.GONE);
        miniTitle.setVisibility(View.GONE);
        cardView.setVisibility(View.VISIBLE);
        pause.setVisibility(View.GONE);
        playBackPauseBg.setVisibility(View.VISIBLE);
        playBackPause.setVisibility(View.VISIBLE);
        coverart.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        durationText.setVisibility(View.VISIBLE);
        playBackPauseBg.setVisibility(View.VISIBLE);
        playBackPause.setVisibility(View.VISIBLE);
        skip.setVisibility(View.VISIBLE);
        previous.setVisibility(View.VISIBLE);
        artistName.setVisibility(View.VISIBLE);
        song_titlebot.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

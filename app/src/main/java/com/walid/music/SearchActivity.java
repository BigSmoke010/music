package com.walid.music;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerAdapter songAdapter;
    private List<Song> allSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AudioHelper audioHelper = new AudioHelper();
        allSongs = audioHelper.getAllAudioFiles(this);
        List<Song> emptyList = new ArrayList<>();
        songAdapter = new RecyclerAdapter(this, emptyList, MainActivity.getInstance());
        recyclerView.setAdapter(songAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        AppCompatImageButton backBtn = findViewById(R.id.backBtn);
        EditText search = findViewById(R.id.searchEditText);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            filterSongs(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        search.requestFocus();
    }
    private void filterSongs(String query) {
        List<Song> filteredSongs = new ArrayList<>();
        if (!query.isEmpty()) {
            for (Song song : allSongs) {
                if (Objects.requireNonNull(song.getTitle()).toLowerCase().contains(query.toLowerCase())) {
                    filteredSongs.add(song);
                }
            }
        }
        songAdapter.updateData(filteredSongs);
    }
    public static void hideKeyboard(Context context, View view) {
        if (context == null || view == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard(this, recyclerView);

        finishAfterTransition();
    }
}

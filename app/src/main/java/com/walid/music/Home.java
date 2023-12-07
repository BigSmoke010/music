package com.walid.music;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Home extends Fragment {


    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = rootView.findViewById(R.id.songListRecyclerView);
        AudioHelper audioHelper = new AudioHelper();
        List<Song> allSongs = audioHelper.getAllAudioFiles(requireContext());
        System.out.println(allSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerAdapter(getContext(), allSongs, (MainActivity) getActivity());
        recyclerView.setAdapter(adapter);

        return rootView;
    }

}
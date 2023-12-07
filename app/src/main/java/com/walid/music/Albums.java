package com.walid.music;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.AppBarLayout;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Albums extends Fragment implements OnItemClickListener {

    private List<Song> allSongs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        AudioHelper audioHelper = new AudioHelper();
        allSongs = audioHelper.getAllAudioFiles(requireContext());
        List<Album> albums = audioHelper.getAllAlbums(requireContext());
        RecyclerView albumRecyclerView = view.findViewById(R.id.albumRecyclerView);
        AlbumAdapter albumAdapter = new AlbumAdapter(albums, this);
        albumRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        albumRecyclerView.setAdapter(albumAdapter);
        return view;
    }

    @Override
    public void onItemClick(Album desiredAlbumData) {
        Intent intent = new Intent(requireActivity(), AlbumDetails.class);
        intent.putExtra("albumData", desiredAlbumData);
        startActivity(intent);
    }
}
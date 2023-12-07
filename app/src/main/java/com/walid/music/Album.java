package com.walid.music;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Album implements Parcelable {
    private String name;
    private Uri albumArtUri;
    private String artist;
    private String year;
    private List<Song> songs;

    public Album( String name, Uri albumArtUri, String artist, String year) {
        this.name = name;
        this.albumArtUri = albumArtUri;
        this.artist = artist;
        this.songs = new ArrayList<>();
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getAlbumArtUri() {
        return albumArtUri;
    }

    public String getArtist() {
        return artist;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    protected Album(Parcel in) {
        name = in.readString();
        albumArtUri = in.readParcelable(Uri.class.getClassLoader());
        artist = in.readString();
        songs = new ArrayList<>();
        in.readList(songs, Song.class.getClassLoader());
        year = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(albumArtUri, flags);
        dest.writeString(artist);
        dest.writeList(songs);
        dest.writeString(year);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}

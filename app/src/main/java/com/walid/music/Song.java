package com.walid.music;

import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.io.IOException;

public class Song implements Parcelable {
    private long id;
    private String title;
    private String artist;
    private String album;
    private String data;
    private String albumArtist;
    private String albumId;
    private String trackNum;
    private long duration;

    public Song(long id, String title, String artist, String album, String data, String albumArtist, String albumId, String trackNum, long duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.data = data;
        this.albumArtist = albumArtist;
        this.albumId = albumId;
        this.trackNum = trackNum;
        this.duration = duration;
    }

    public void setId(long id) {
        this.id = id;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public byte[] getArt() throws IOException {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(this.data);
            return retriever.getEmbeddedPicture();
        }
    }

    public long getDuration() {
        return duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public void setTrackNum(String trackNum) {
        this.trackNum = trackNum;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getData() {
        return data;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getTrackNum() {
        return trackNum;
    }

    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        data = in.readString();
        albumArtist = in.readString();
        albumId = in.readString();
        trackNum = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(data);
        dest.writeString(albumArtist);
        dest.writeString(albumId);
        dest.writeString(trackNum);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}

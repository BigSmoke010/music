package com.walid.music;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.util.Collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AudioHelper {

    public List<Song> getAllAudioFiles(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        List<Song> songList = new ArrayList<>();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.ALBUM_ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DURATION
        };
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        try (Cursor cursor = contentResolver.query(
                audioUri,
                projection,
                null,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    long duration =  cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String albumArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST));
                    String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                    String tracknum = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));

                    Song song = new Song(id, title, artist, album, data, albumArtist, albumId, tracknum, duration);
                    songList.add(song);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songList;
    }
    public List<Album> getAllAlbums(Context context) {
        List<Album> albums = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ARTIST,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.NUM_TRACKS,
                MediaStore.Audio.Media.CD_TRACK_NUMBER,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "=1";
        String sortOrder = MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC";

        Cursor cursor = contentResolver.query(uri, projection, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String songArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String artistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST));
                Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + albumId);
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String tracknum = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));
                String CdTrack = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.CD_TRACK_NUMBER));
                String year = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));

                Album existingAlbum = null;
                for (Album album : albums) {
                    if (album.getName().equals(albumName)) {
                        existingAlbum = album;
                        break;
                    }
                }
                if (artistName == null) {
                    artistName = songArtist;
                }
                if (existingAlbum == null) {
                    Album album = new Album(albumName, albumArtUri, artistName, year);
                    Song song = new Song(id, title, songArtist, album.getName(), data, artistName, String.valueOf(albumId), tracknum, duration);
                    album.addSong(song);
                    albums.add(album);
                } else {
                    Song song = new Song(id, title, songArtist, existingAlbum.getName(), data, artistName, String.valueOf(albumId), tracknum, duration);
                    existingAlbum.addSong(song);
                }
            }
            cursor.close();
        }

        return albums;
    }

}
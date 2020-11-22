package com.netmontools.lookatnet.ui.remote.model;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class RemoteFolder {

    private UUID id;
    private String name;
    private String addr;
    private String path;
    private String bssid;
    private long size;
    private Drawable image;
    public boolean isFile;
    public boolean isChecked;
    public boolean isHost;

    private ArrayList<RemoteFolder> folders;

    public RemoteFolder() {
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {this.id = id; }

    public long getSize() {
        return size;
    }
    public void setSize(long size) {this.size = size; }

    @Nullable
    public String getName() {
        return name;
    }
    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getAddr() {
        return addr;
    }
    public void setAddr(@Nullable String addr) {
        this.addr = addr;
    }

    @Nullable
    public String getPath() {
        return path;
    }
    public void setPath(@Nullable String path) {
        this.path = path;
    }

    @Nullable
    public String getBssid() {
        return bssid;
    }
    public void setBssid(@Nullable String bssid) {
        this.bssid = bssid;
    }

    public void addFolder(RemoteFolder f) {
        folders.add(f);
    }

    @Nullable
    public ArrayList<RemoteFolder> getFolders() {
        return folders;
    }

    @Nullable
    public RemoteFolder getFolder(UUID id) {
        for (RemoteFolder f : folders) {
            if (f.getId() == id)
                return f;
        }
        return null;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}


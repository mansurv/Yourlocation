package com.netmontools.lookatnet.ui.local.model;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class Folder {

    private UUID id;
    private String name;
    private String path;
    private long size;
    private Drawable image;
    public boolean isFile;
    public boolean isChecked;
    private ArrayList<Folder> folders;

    public Folder() {
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
    public String getPath() {
        return path;
    }
    public void setPath(@Nullable String path) {
        this.path = path;
    }

    public void addFolder(Folder f) {
        folders.add(f);
    }

    @Nullable
    public ArrayList<Folder> getFolders() {
        return folders;
    }

    @Nullable
    public Folder getFolder(UUID id) {
        for (Folder f : folders) {
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


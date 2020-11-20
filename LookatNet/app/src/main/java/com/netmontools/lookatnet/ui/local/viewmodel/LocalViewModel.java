package com.netmontools.lookatnet.ui.local.viewmodel;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.netmontools.lookatnet.ui.local.LocalRepository;
import com.netmontools.lookatnet.ui.local.model.Folder;

import java.util.List;
import java.util.UUID;

public class LocalViewModel extends AndroidViewModel {

    private Folder folder;
    private LocalRepository repository;
    private LiveData<List<Folder>> allPoints;

    public LocalViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalRepository(application);
        allPoints = repository.getAll();
    }

    public void delete(Folder folder) {
        repository.delete(folder);
    }

    public void update(Folder folder) {
        repository.update(folder);
    }

    public void check(Folder folder) {
        repository.check(folder);
    }

    public UUID getId() {
        return folder.getId();
    }

    public String getName() {
        return !TextUtils.isEmpty(folder.getName()) ? folder.getName() : "";
    }

    public String getPath() {
        return !TextUtils.isEmpty(folder.getPath()) ? folder.getPath() : "";
    }

    public long getSize() {
        return folder.getSize();
    }

    public Drawable getImage() {
        return folder.getImage();
    }

    public LiveData<List<Folder>> getAllPoints() {
        return allPoints;
    }
}
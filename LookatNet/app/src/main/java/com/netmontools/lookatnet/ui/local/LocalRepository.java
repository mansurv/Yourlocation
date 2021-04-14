package com.netmontools.lookatnet.ui.local;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.netmontools.lookatnet.ui.local.model.Folder;
import com.netmontools.lookatnet.App;
import java.io.File;
import java.util.List;

public class  LocalRepository {

    private static LiveData<List<Folder>> allPoints;
    private static MutableLiveData<List<Folder>> liveData;

    public LocalRepository(Application application) {
        liveData = new MutableLiveData<>();
        try {
            liveData.setValue(App.folders);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        allPoints = liveData;
    }

    public void delete(Folder point) {
        new DeletePointAsyncTask().execute(point);
    }

    public void update(Folder point) {
        new UpdateListAsyncTask().execute(point);
    }

    public void check(Folder point) {
        new CheckPointAsyncTask().execute(point);
    }

    public LiveData<List<Folder>> getAll() {
        return allPoints;
    }

    private static class DeletePointAsyncTask extends AsyncTask<Folder, Void, Void> {
        @Override
        protected Void doInBackground(Folder... points) {
            //TODO pointDao.delete(points[0]);
            return null;
        }
    }

    private static class UpdateListAsyncTask extends AsyncTask<Folder, Void, Void> {
        @Override
        protected Void doInBackground(Folder... points) {
            App.folders.clear();
            try {
                Folder fd;
                File file = new File(points[0].getPath());
                App.previousPath = file.getPath();
                if ((file.exists()) && (file.isDirectory())) {
                    for (File f : file.listFiles()) {
                        if (f.exists()) {
                            fd = new Folder();
                            if (f.isDirectory()) {
                                fd.setName(f.getName());
                                fd.setPath(f.getPath());
                                fd.isFile = false;
                                fd.isChecked = false;
                                fd.setSize(0L);
                                fd.setImage(App.folder_image);
                                App.folders.add(fd);
                            }
                        }
                    }
                    for (File f : file.listFiles()) {
                        if (f.exists()) {
                            fd = new Folder();
                            if (f.isFile()) {
                                //App.imageSelector(f);
                                fd.setName(f.getName());
                                fd.setPath(f.getPath());
                                fd.isFile = true;
                                fd.isChecked = false;
                                fd.setSize(f.length());
                                fd.setImage(App.file_image);
                                App.folders.add(fd);
                            }
                        }
                    }
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            liveData.setValue(App.folders);
            allPoints = liveData;
        }
    }

    private static class CheckPointAsyncTask extends AsyncTask<Folder, Void, Void> {
        @Override
        protected Void doInBackground(Folder... points) {
            if (points[0].isChecked) {
                points[0].isChecked = false;
            } else {
                points[0].isChecked = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            liveData.setValue(App.folders);
            allPoints = liveData;
        }
    }
}

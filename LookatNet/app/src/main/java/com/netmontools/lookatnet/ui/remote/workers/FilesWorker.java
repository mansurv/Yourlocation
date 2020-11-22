package com.netmontools.lookatnet.ui.remote.workers;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.netmontools.lookatnet.App;
import com.netmontools.lookatnet.ui.remote.model.RemoteFolder;
import com.netmontools.lookatnet.ui.remote.repository.FilesRepository;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import static androidx.work.ListenableWorker.Result.failure;
import static androidx.work.ListenableWorker.Result.success;

public class FilesWorker extends Worker {

    public FilesWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @SuppressLint("WrongThread")
    @NonNull
    @Override
    public Result doWork() {
        String address = getInputData().getString("address");
        String url = "smb://" + address + "/";
        FilesRepository.remoteRootPath = url;
        startSmb(url);
        onPostExecute(App.share, url);
        return success();
    }

    private void startSmb(String url) {
        String user = "";
        String pass = "";

        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, user, pass);
        SmbFile dir = null;
        try {
            dir = new SmbFile(url, auth);
            if (dir.isFile()) {
                App.share[0] = "Error: " + dir.getParent();
                return;
            }
        } catch (MalformedURLException mue) {
            App.share[0] = "Error: startSmb MalformedURLException. " + mue.getMessage();
            return;
        } catch (SmbException se) {
            App.share[0] = "Error: startSmb SmbException. " + se.getMessage();
            return;
        }

        try {
            App.share = null;
            for (SmbFile f : dir.listFiles()) {
                App.share = dir.list();
            }
            if (App.share == null) {
                App.share = new String[1];
                App.share[0] = "Error: folder is empty!";
                return;
            }
            String url_back = url;
            for (int i = 0; i < App.share.length; i++) {
                url = url_back + App.share[i]  + "/";
                dir = new SmbFile(url, auth);

                if (dir.isDirectory()) {
                    App.share[i] = App.share[i] + " 1";
                } else {
                    App.share[i] = App.share[i] + " 0";
                }
            }
        } catch (MalformedURLException mue) {
            if (App.share == null) App.share = new String[1];
            App.share[0] = "Error: startSmb MalformedURLException. " + mue.getMessage();
        } catch (SmbException se) {
            if (App.share == null) App.share = new String[1];
            App.share[0] = "Error: startSmb SmbException. " + se.getMessage();
        }
    }

    private Result onPostExecute(String[] str, String url) {
        if ((str != null) && (str[0].contains("Error:"))) {
            Data outputData = new Data.Builder()
                    .putString("error", str[0])
                    .build();
            return failure(outputData);
        }  else {
            App.remoteFolders.clear();
            assert str != null;
            for (int i = 0; i < str.length; i++) {
                String[] splitted = str[i].split(" +");
                int len = splitted.length;
                RemoteFolder fd = new RemoteFolder();
                if (splitted[len - 1].equals("1")) {
                    fd.setImage(App.folder_image);
                    fd.isFile = false;
                    fd.setBssid("");
                    App.remoteFolders.add(fd);
                } else continue;

                StringBuilder sb = new  StringBuilder();
                for (int j = 0; j < (len - 1); j++) {
                    sb.append(splitted[j]);
                    if (j != (len - 2)) {
                        sb.append(" ");
                    }
                }
                fd.setName(sb.toString());
                fd.setPath(url);
                fd.setBssid("");
                App.remoteFolders.add(fd);
            }
            for (int k = 0; k < str.length; k++) {
                String[] splitted = str[k].split(" +");
                int len = splitted.length;
                RemoteFolder fd = new RemoteFolder();
                if (splitted[len - 1].equals("0")) {
                    fd.setImage(App.file_image);
                    fd.isFile = true;
                    fd.setBssid("");
                    App.remoteFolders.add(fd);
                } else continue;

                StringBuilder sb = new StringBuilder();
                for (int l = 0; l < (len - 1); l++) {
                    sb.append(splitted[l]);
                    if (l != (len - 2)) {
                        sb.append(" ");
                    }
                }
                fd.setName(sb.toString());
                fd.setPath(url);
                fd.setBssid("");
                App.remoteFolders.add(fd);
            }
        }
        return null;
    }
}

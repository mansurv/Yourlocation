package com.netmontools.lookatnet.ui.remote.workers;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.netmontools.lookatnet.App;
import com.netmontools.lookatnet.AppDatabase;
import com.netmontools.lookatnet.BuildConfig;
import com.netmontools.lookatnet.ui.remote.model.RemoteFolder;
import com.netmontools.lookatnet.ui.remote.model.RemoteModel;
import com.netmontools.lookatnet.ui.remote.model.RemoteModelDao;
import com.netmontools.lookatnet.utils.LogSystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Objects;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import static androidx.work.ListenableWorker.Result.failure;
import static androidx.work.ListenableWorker.Result.success;

public class AddWorker  extends Worker {

    private static final String TAG = "AddWorker";
    public AppDatabase db;

    public AddWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @SuppressLint("WrongThread")
    @NonNull
    @Override
    public Result doWork() {

        String currentBssid = getInputData().getString("bssid");
        String currentIP = getInputData().getString("addr");
        String currentName = getInputData().getString("name");
        String login = getInputData().getString("login");
        String pass = getInputData().getString("pass");
        int subnetIP = getInputData().getInt("subnet", 0);

        int count = 0;
        db = App.getInstance().getDatabase();
        RemoteModelDao remoteDao = db.remoteModelDao();
        RemoteModel remote;
        RemoteFolder folder;
        App.remoteFolders.clear();
        String url = "smb://";

        String s = "";
        try {
            s = (scanLan(currentIP, login, pass));
            if (!s.equals("")) {
                String[] splitted = s.split(" +");
                int len = splitted.length;
                if (splitted[0].equals("-1")) {
                    Data outputData = new Data.Builder()
                            .putString("error", splitted[1])
                            .build();

                    return failure(outputData);
                }
                if (splitted[len - 1].equals("0")) {
                    remote = new RemoteModel();
                    remote.setBssid(currentBssid);
                    remote.setName(splitted[1]);
                    remote.setAddr(splitted[0]);
                    remote.setLogin("");
                    remote.setPass("");
                    remote.isPass = false;
                    remoteDao.insert(remote);
                    count++;
                    folder = new RemoteFolder();
                    folder.isFile = false;
                    folder.isHost = true;
                    folder.setImage(App.host_image);
                    folder.setName(splitted[1]);
                    folder.setPath(url + splitted[0] + "/");
                    App.remoteFolders.add(folder);
                } else if (splitted[len - 1].equals("1")) {
                    remote = new RemoteModel();
                    remote.setBssid(currentBssid);
                    remote.setName(splitted[1]);
                    remote.setAddr(splitted[0]);
                    remote.setLogin(login);
                    remote.setPass(pass);
                    remote.isPass = true;
                    remoteDao.insert(remote);
                    count++;
                    folder = new RemoteFolder();
                    folder.isFile = false;
                    folder.isHost = true;
                    folder.setImage(App.host_image);
                    folder.setName(splitted[1]);
                    folder.setPath(url + splitted[0] + "/");
                    folder.setBssid(currentBssid);
                    App.remoteFolders.add(folder);
                }
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            Data outputData = new Data.Builder()
                    .putString("error", ie.getMessage())
                    .build();

            return failure(outputData);
        }

        Data outputData = new Data.Builder()
                .putInt("count", count)
                .build();

        return success(outputData);
    }

    private String scanLan(String host, String login, String pass) throws InterruptedException {
        if (host.equalsIgnoreCase(""))
            return "";
        String share[] = null;
        String hostname = null;
        try {
            InetAddress address = InetAddress.getByName(host);
            hostname = address.getHostName();

            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, login, pass);
            SmbFile dir = null;
            try {
                dir = new SmbFile("smb://" + host + "/", auth);
            } catch (MalformedURLException mue) {
                if (BuildConfig.USE_LOG) {
                    LogSystem.logInFile(TAG, "  scanLan(), NtlmPasswordAuthentication\n  MalformedURLException: " + mue.getMessage());
                }
                return host + " " + host + " -1";
            }
            try {
                for (SmbFile f : dir.listFiles()) {
                    share = dir.list();
                }
                if (share != null) {
                    return host + " " + hostname + " 0";
                } else return host + " " + hostname + " -1";
            } catch (SmbException se) {
                if ((Objects.requireNonNull(se.getMessage()).contains("Logon failure: unknown user name or bad password")) ||
                        (se.getMessage().equalsIgnoreCase("0xc000009a"))) {
                    if (BuildConfig.USE_LOG) {
                        LogSystem.logInFile(TAG, "  scanLan(), NtlmPasswordAuthentication\n  SmbException: " + se.getMessage() + "\n  host " + host);
                    }
                    return host + " " + hostname + " 1";
                } else {
                    return host + " " + hostname + " -1";
                }
            }
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
            if (BuildConfig.USE_LOG) {
                LogSystem.logInFile(TAG, "  scanLan(), global\n  UnknownHostException:  " + uhe.getMessage());
            }
        }
        return "";
    }
}


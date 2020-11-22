package com.netmontools.lookatnet.ui.remote.workers;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
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

public class RemoteWorker extends Worker {
    private static final String TAG = "RemoteWorker";
    public AppDatabase db;

    public RemoteWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @SuppressLint("WrongThread")
    @NonNull
    @Override
    public Result doWork() {

        String currentBssid = getInputData().getString("bssid");
        int subnetIP = getInputData().getInt("subnet", 0);
        int broadcastIP = getInputData().getInt("broadcast", 0);

        int range = ntol(broadcastIP - subnetIP);
        String currentSubnetIP = String.format(Locale.US,"%d.%d.%d.%d", (subnetIP & 0xff), (subnetIP >> 8 & 0xff),
                (subnetIP >> 16 & 0xff), (subnetIP >> 24 & 0xff));
        int startIP;
        int count = 0;
        db = App.getInstance().getDatabase();
        RemoteModelDao remoteDao = db.remoteModelDao();
        RemoteModel remote;
        RemoteFolder folder;
        App.remoteFolders.clear();
        String url = "smb://";

        String s = "";
        try {
            for (int i = 1; i <= range; i++) {
                startIP = lton((ntol(subnetIP)) + i);
                s = (scanLan(startIP));
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
                        //remote.setLogin("");
                        //remote.setPass("");
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
                        //remote.setLogin("");
                        //remote.setPass("");
                        remote.isPass = true;
                        remoteDao.insert(remote);
                        count++;
                        folder = new RemoteFolder();
                        folder.isFile = false;
                        folder.isHost = true;
                        folder.setImage(App.host_image);
                        folder.setName(splitted[1]);
                        folder.setAddr(splitted[0]);
                        folder.setPath(url + splitted[0] + "/");
                        folder.setBssid(currentBssid);
                        App.remoteFolders.add(folder);
                    }
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

    private String scanLan(int ip) throws InterruptedException {
        if (ip == 0)
            return "";
        String share[] = null;
        String mac = null;
        String hostname = null;
        int port = 58317;
        byte[] bufPacket = new byte[2];
        String host = String.format(Locale.US,"%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
                (ip >> 16 & 0xff), (ip >> 24 & 0xff));
        try {
            DatagramSocket s = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);
            hostname = address.getHostName();
            DatagramPacket packet = new DatagramPacket(bufPacket, bufPacket.length, address, port);
            s.send(packet);
            mac = getMacFromArpCache(host);
            if (mac == null) {
                s.send(packet);
                mac = getMacFromArpCache(host);
                if (mac == null) {
                    s.send(packet);
                    mac = getMacFromArpCache(host);
                    if (mac == null) {
                        s.close();
                        if ((hostname == null) || (hostname.equalsIgnoreCase(host)))
                            return "";
                    }
                    s.close();
                }
            }

            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, null, null);
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
        } catch (SocketException se) {
            se.printStackTrace();
            if (BuildConfig.USE_LOG) {
                LogSystem.logInFile(TAG, "  scanLan(), global\n  SocketException:  " + se.getMessage());
            }
            return "-1" + se.getMessage();
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
            if (BuildConfig.USE_LOG) {
                LogSystem.logInFile(TAG, "  scanLan(), global\n  UnknownHostException:  " + uhe.getMessage());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            if (BuildConfig.USE_LOG) {
                LogSystem.logInFile(TAG, "  scanLan(), global\n  IOException:  " + ioe.getMessage());
            }
        }
        return "";
    }

    private String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted.length >= 4 && ip.equals(splitted[0])) {
                    String mac = splitted[3];
                    if ((mac.matches("..:..:..:..:..:..")) && (!(mac.matches("00:00:00:00:00:00")))) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException | NullPointerException ioe) {
                ioe.printStackTrace();
            }
        }
        return null;
    }

    private static int lton(int c) {
        return ((c >> 0 & 0xFF) << 24) | ((c >> 8 & 0xFF) << 16)
                | ((c >> 16 & 0xFF) << 8) | ((c >> 24 & 0xFF) << 0);
    }
    // Flips the bytes from BIG ENDIAN to LITTLE. For example 0x04030201 becomes 0x01020304.
    private static int ntol(int c) {
        return ((c >> 24 & 0xFF) << 0) | ((c >> 16 & 0xFF) << 8)
                | ((c >> 8 & 0xFF) << 16) | ((c >> 0 & 0xFF) << 24);
    }
}

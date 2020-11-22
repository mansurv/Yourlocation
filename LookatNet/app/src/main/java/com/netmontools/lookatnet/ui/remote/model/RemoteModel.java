package com.netmontools.lookatnet.ui.remote.model;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"addr", "bssid"}, unique = true)})
public class RemoteModel {

    public RemoteModel() {
    }

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String bssid;
    private String name;
    private String addr;
    private String login;
    private String pass;
    @Ignore
    public boolean isPass;

    public long getId() {
        return id;
    }
    public void setId(long id) {this.id = id; }

    @Nullable
    public String getName() { return name; }
    public void setName(@Nullable String name){
        this.name = name;
    }

    @Nullable
    public String getBssid() {
        return bssid;
    }
    public void setBssid(@Nullable String bssid){
        this.bssid = bssid;
    }

    @Nullable
    public String getAddr() {
        return addr;
    }
    public void setAddr(@Nullable String addr) {
        this.addr = addr;
    }

    @Nullable
    public String getLogin() {
        return login;
    }
    public void setLogin(@Nullable String login) {
        this.login = login;
    }

    @Nullable
    public String getPass() {
        return pass;
    }
    public void setPass(@Nullable String pass) {
        this.pass = pass;
    }
}

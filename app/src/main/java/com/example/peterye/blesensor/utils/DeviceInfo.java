package com.example.peterye.blesensor.utils;

/**
 * 存储外设的Mac地址，连接状态等信息
 * Created by PeterYe on 2017/4/3.
 */

public class DeviceInfo {

    private String name;
    private String address;
    private boolean connected;

    public DeviceInfo(){

    }
    public DeviceInfo(String n,String a){
        this.name = n;
        this.address = a;
        connected = false;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

}

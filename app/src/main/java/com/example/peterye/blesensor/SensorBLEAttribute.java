package com.example.peterye.blesensor;

import java.util.HashMap;

/**
 * 定义Gatt Service ，Gatt Characteristic
 * UUID的作用名称
 * Created by PeterYe on 2017/4/1.
 */

public class SensorBLEAttribute {

    private static HashMap<String,String> attributes =
            new HashMap<String,String>();

    static {
        // Gatt Service
        attributes.put(BLESensorGatt.UUID_DEVINFO_SERV.toString(),"设备信息服务");
        attributes.put(BLESensorGatt.UUID_BAR_SERV.toString(),"气压测量服务");
        attributes.put(BLESensorGatt.UUID_ACC_SERV.toString(),"加速计服务");
        attributes.put(BLESensorGatt.UUID_HUM_SERV.toString(),"湿度服务");
        attributes.put(BLESensorGatt.UUID_IRT_SERV.toString(),"温度服务");
        attributes.put(BLESensorGatt.UUID_MOV_SERV.toString(),"移动服务");


        // Gatt Characteristic
        attributes.put(BLESensorGatt.UUID_DEVINFO_FWREV.toString(),"固件版本");
        attributes.put(BLESensorGatt.UUID_BAR_DATA.toString(),"气压强度");
    }

    /**
     * 通过UUID查看对应的属性名字
     */
    public static  String loopUp(String uuid,String defaultName){
        String name = attributes.get(uuid);
        return  name == null ? defaultName : name;
    }
}

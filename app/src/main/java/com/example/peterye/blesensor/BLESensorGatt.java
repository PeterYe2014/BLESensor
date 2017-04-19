package com.example.peterye.blesensor;

import java.util.UUID;

import static java.util.UUID.fromString;

/**
 * Define the UUID of the System
 * Created by PeterYe on 2017/3/31.
 */

public class BLESensorGatt {
    public final static UUID
            UUID_DEVINFO_SERV = fromString("0000180a-0000-1000-8000-00805f9b34fb"),
            UUID_DEVINFO_FWREV = fromString("00002A26-0000-1000-8000-00805f9b34fb"),

            UUID_IRT_SERV = fromString("f000aa00-0451-4000-b000-000000000000"),
            UUID_IRT_DATA = fromString("f000aa01-0451-4000-b000-000000000000"),
            UUID_IRT_CONF = fromString("f000aa02-0451-4000-b000-000000000000"), // 0: disable, 1: enable
            UUID_IRT_PERI = fromString("f000aa03-0451-4000-b000-000000000000"), // Period in tens of milliseconds

            UUID_ACC_SERV = fromString("f000aa10-0451-4000-b000-000000000000"), // Sensortag2 里面没有
            UUID_ACC_DATA = fromString("f000aa11-0451-4000-b000-000000000000"),
            UUID_ACC_CONF = fromString("f000aa12-0451-4000-b000-000000000000"), // 0: disable, 1: enable
            UUID_ACC_PERI = fromString("f000aa13-0451-4000-b000-000000000000"), // Period in tens of milliseconds

            UUID_HUM_SERV = fromString("f000aa20-0451-4000-b000-000000000000"),
            UUID_HUM_DATA = fromString("f000aa21-0451-4000-b000-000000000000"),
            UUID_HUM_CONF = fromString("f000aa22-0451-4000-b000-000000000000"), // 0: disable, 1: enable
            UUID_HUM_PERI = fromString("f000aa23-0451-4000-b000-000000000000"), // Period in tens of milliseconds

            UUID_MAG_SERV = fromString("f000aa30-0451-4000-b000-000000000000"), // Sensortag2 里面没有
            UUID_MAG_DATA = fromString("f000aa31-0451-4000-b000-000000000000"),
            UUID_MAG_CONF = fromString("f000aa32-0451-4000-b000-000000000000"), // 0: disable, 1: enable
            UUID_MAG_PERI = fromString("f000aa33-0451-4000-b000-000000000000"), // Period in tens of milliseconds

            UUID_OPT_SERV = fromString("f000aa70-0451-4000-b000-000000000000"),
            UUID_OPT_DATA = fromString("f000aa71-0451-4000-b000-000000000000"),
            UUID_OPT_CONF = fromString("f000aa72-0451-4000-b000-000000000000"), // 0: disable, 1: enable
            UUID_OPT_PERI = fromString("f000aa73-0451-4000-b000-000000000000"), // Period in tens of milliseconds

            UUID_BAR_SERV = fromString("f000aa40-0451-4000-b000-000000000000"),
            UUID_BAR_DATA = fromString("f000aa41-0451-4000-b000-000000000000"),
            UUID_BAR_CONF = fromString("f000aa42-0451-4000-b000-000000000000"), // 0: disable, 1: enable
            UUID_BAR_CALI = fromString("f000aa43-0451-4000-b000-000000000000"), // Calibration characteristic
            UUID_BAR_PERI = fromString("f000aa44-0451-4000-b000-000000000000"), // Period in tens of milliseconds

            UUID_GYR_SERV = fromString("f000aa50-0451-4000-b000-000000000000"), // 没有数据
            UUID_GYR_DATA = fromString("f000aa51-0451-4000-b000-000000000000"),
            UUID_GYR_CONF = fromString("f000aa52-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
            UUID_GYR_PERI = fromString("f000aa53-0451-4000-b000-000000000000"), // Period in tens of milliseconds

            UUID_MOV_SERV = fromString("f000aa80-0451-4000-b000-000000000000"), // 三轴加速度计、三轴陀螺仪、三轴磁强计,共九轴
            UUID_MOV_DATA = fromString("f000aa81-0451-4000-b000-000000000000"),
            UUID_MOV_CONF = fromString("f000aa82-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
            UUID_MOV_PERI = fromString("f000aa83-0451-4000-b000-000000000000"), // Period in tens of milliseconds


            UUID_KEY_SERV = fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
            UUID_KEY_DATA = fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    // 设备特性UUID
    private static final String dISService_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String dISSystemID_UUID = "00002a23-0000-1000-8000-00805f9b34fb";
    private static final String dISModelNR_UUID = "00002a24-0000-1000-8000-00805f9b34fb";
    private static final String dISSerialNR_UUID = "00002a25-0000-1000-8000-00805f9b34fb";
    private static final String dISFirmwareREV_UUID = "00002a26-0000-1000-8000-00805f9b34fb";
    private static final String dISHardwareREV_UUID = "00002a27-0000-1000-8000-00805f9b34fb";
    private static final String dISSoftwareREV_UUID = "00002a28-0000-1000-8000-00805f9b34fb";
    private static final String dISManifacturerNAME_UUID = "00002a29-0000-1000-8000-00805f9b34fb";

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /**
     * senortag里面存在的Service
     * 00001800-0000-1000-8000-00805f9b34fb
     00001801-0000-1000-8000-00805f9b34fb
     0000180a-0000-1000-8000-00805f9b34fb
     0000180f-0000-1000-8000-00805f9b34fb
     f000aa00-0451-4000-b000-000000000000
     f000aa20-0451-4000-b000-000000000000
     f000aa40-0451-4000-b000-000000000000
     f000aa80-0451-4000-b000-000000000000
     f000aa70-0451-4000-b000-000000000000
     0000ffe0-0000-1000-8000-00805f9b34fb
     f000aa64-0451-4000-b000-000000000000
     f000ac00-0451-4000-b000-000000000000
     f000ccc0-0451-4000-b000-000000000000
     f000ffc0-0451-4000-b000-000000000000
     */

}

package com.example.peterye.blesensor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * 管理Ble连接建立，读取character，写入character的操作，
 * 管理两个设备的Service，通过多个BleGatt来实现
 */

public class BluetoothService extends Service {
    private final static String TAG = BluetoothService.class.getName();


    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt BleGatt;
    private String mBLEAddress;
    private List<String> connectedAddresss = new ArrayList<String>();
    private HashMap<String,BluetoothGatt> connectedGatts = new HashMap<String,BluetoothGatt>();
    boolean isHeightCalibrated = false; // 气压高度是否矫正
    // 连接状态
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int mConnectionState = STATE_DISCONNECTED;
    // 定义服务状态，方便通知主Activity
    public final static String ACTION_GATT_CONNECTED =
            "com.example.peterye.blesensor.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.peterye.blesensor.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.peterye.blesensor.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.peterye.blesensor.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.peterye.blesensor.EXTRA_DATA";
    public final static String DEVICE_TAG=
            "com.example.peterye.blesensor.DEVICE_TAG";
    public final static String DATA_UUID=
            "com.example.peterye.blesensor.DATA_UUID";

    // 定义BLE 异步操作队列
    private Queue<BluetoothGattDescriptor> descriptorsWriteQueue =
            new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattCharacteristic> characteristicReadQueue =
            new LinkedList<BluetoothGattCharacteristic>();
    private Queue<BluetoothGattCharacteristic> characteristicWriteQueue =
            new LinkedList<BluetoothGattCharacteristic>();

    public BluetoothService() {

    }

    // 定义Gatt回调函数
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            String address = gatt.getDevice().getAddress();
            if(newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction,address);
                Log.d(TAG,"连接到Gatt server");
                Log.d(TAG,"启动Gatt Service 发现：" + BleGatt.discoverServices());
            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.d(TAG,"连接已断开");
                broadcastUpdate(intentAction,address);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            String address = gatt.getDevice().getAddress();
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,address);
            }
            else{
                Log.w(TAG,"onServicesDiscovered:"+status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String address = gatt.getDevice().getAddress();
            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic,address);
            Log.d(TAG, "notify。。。。。");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String address = gatt.getDevice().getAddress();
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic,address);
                Log.d(TAG, "read。。。。。");
                characteristicReadQueue.remove();
            }
            if(characteristicReadQueue.size() > 0 )
                BleGatt.readCharacteristic(characteristicReadQueue.element());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if(status == BluetoothGatt.GATT_SUCCESS){
                characteristicWriteQueue.remove();
                Log.i("CHRA_WRITE","写入特征成功");
            }
            else{
                Log.i("CHRA_WRITE","写入特征失败");
            }
            if(characteristicWriteQueue.size() > 0)
                BleGatt.writeCharacteristic(characteristicWriteQueue.element());
            else if(descriptorsWriteQueue.size() > 0)
                BleGatt.writeDescriptor(descriptorsWriteQueue.element());
            else if(characteristicReadQueue.size() > 0)
                BleGatt.readCharacteristic(characteristicReadQueue.element());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.i("DES_WRITE","写入Descriptor成功:"+descriptorsWriteQueue.size());
                descriptorsWriteQueue.remove();
            }
            else{
                Log.i("DES_WRITE","写入Descriptor失败:"+descriptorsWriteQueue.size());
            }
            if(descriptorsWriteQueue.size() > 0)
                BleGatt.writeDescriptor(descriptorsWriteQueue.element());
            else if(characteristicReadQueue.size() > 0)
                BleGatt.readCharacteristic(characteristicReadQueue.element());
        }
    };

    /**
     * 发送通知给DeviceControlActivity
     * @param action
     */
    private void broadcastUpdate(final String action,String address){
        final Intent intent = new Intent(action);
        intent.putExtra(DEVICE_TAG, address);
        sendBroadcast(intent);
    }

    /**
     * 如何解析character数据并且发送出去
     * @param action
     * @param characteristic
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic,
                                 final  String address){

        final  Intent intent = new Intent(action);
        /**
         * 传送Characteristic数据给控制Activity，先用气压数据
         */
        if(characteristic.getUuid().toString().equals(BLESensorGatt.UUID_BAR_DATA.toString())){

            double heightCalibration = 0.0;
            final  byte[] c = characteristic.getValue();
            Point3D v = Sensor.BAROMETER.convert(c);
            if(! isHeightCalibrated){
                heightCalibration = v.x;
                isHeightCalibrated = true;
            }
           double h = (v.x - heightCalibration)
                    / 12.0;
            h = (double) Math.round(-h * 10.0) / 10.0;
            String data = String.format("%.1f mBar %.1f meter", v.x / 100, h);
            Log.i("BAR_DATA","气压计数据"+data);
            intent.putExtra(EXTRA_DATA, data);
        }
        else if(characteristic.getUuid().toString().equals(BLESensorGatt.UUID_ACC_DATA.toString())){
            final byte[] c = characteristic.getValue();
            Point3D v = Sensor.ACCELEROMETER.convert(c);
            Map<String,String> map = new HashMap<String, String>();
            map.put("acc_x", String.format("%.2f", v.x));
            map.put("acc_y",String.format("%.2f",v.y));
            map.put("acc_z",String.format("%.2f",v.z));
            String data = "acc_x:+"+String.format("%.2f", v.x)+"\n"+
                          "acc_y:" +String.format("%.2f", v.y)+"\n"+
                          "acc_z:" +String.format("%.2f", v.z);
            Log.i("ACC_DATA","加速度数据"+data);
            intent.putExtra(EXTRA_DATA, data);
        }
        else if(characteristic.getUuid().toString().equals(BLESensorGatt.UUID_MOV_DATA.toString())){
            final  byte[] c = characteristic.getValue();
            Point3D v = Sensor.MOVEMENT_ACC.convert(c);
            String accData = String.format("X:%.2fG, Y:%.2fG, Z:%.2fG", v.x,v.y,v.z);
            v = Sensor.MOVEMENT_GYRO.convert(c);
            String  gyroData = String.format("X:%.2f'/s, Y:%.2f'/s, Z:%.2f'/s", v.x,v.y,v.z);
            v = Sensor.MOVEMENT_MAG.convert(c);
            String magData = String.format("X:%.2fuT, Y:%.2fuT, Z:%.2fuT", v.x,v.y,v.z);
            String data =  accData + "\n" + gyroData + "\n" +magData;
            Log.i("MOV_DATA","九轴运动数据"+data);
            intent.putExtra(EXTRA_DATA, data);
        }
        else{
            final byte[] data = characteristic.getValue();
            if(data != null && data.length > 0) {
                final StringBuilder builder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    builder.append(String.format("%02X", byteChar));
                }
                try {
                    intent.putExtra(EXTRA_DATA, new String(data,"ISO-8859-1")+ "\n" +builder.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        intent.putExtra(DEVICE_TAG,address);
        intent.putExtra(DATA_UUID,characteristic.getUuid().toString());
        sendBroadcast(intent);
    }

    // 定义自己的IBiner用于Service通信,返回当前Service,多次绑定服务返回同一个Service
    public class LocalBinder extends Binder {
        BluetoothService getService(){
            return BluetoothService.this;
        }
    }

    private final LocalBinder mBinder  = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
       // Toast.makeText(getApplicationContext(),"Service Bind ",Toast.LENGTH_SHORT).show();
        return mBinder;
    }

    @Override
    public void onCreate() {
        //Toast.makeText(getApplicationContext(),"Service Created ",Toast.LENGTH_SHORT).show();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // Toast.makeText(getApplicationContext(),"Service OnStart ",Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化Bluetooth Adapter
     */

    public boolean initialize(){
        if(bluetoothManager == null){
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null)
                return  false;
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null){
            return false;
        }
        return  true;
    }
    /**
     * 连接到GATT Server
     */
    public boolean connect(final String address){
        if(bluetoothAdapter==null || address == null){
            return false;
        }
        // 已经连接过了，就重新连接
        if(connectedAddresss.size() != 0 && connectedGatts.size() !=0 && connectedAddresss.contains(address)){
            BleGatt = connectedGatts.get(address);
            if(BleGatt.connect()){
                mConnectionState = STATE_CONNECTING;
                return  true;
            }
            else {
                return false;
            }
        }
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if(device == null){
            return  false;
        }
        BleGatt = device.connectGatt(this,true,gattCallback);

        // 添加数据
        connectedAddresss.add(address);
        connectedGatts.put(address,BleGatt);
        mBLEAddress = address;
        mConnectionState = STATE_CONNECTING;
        return  true;

    }
    /**
     * 断开连接
     */
    public void disconnect(){
        if(bluetoothAdapter == null || BleGatt == null){
            return;
        }
        BleGatt.disconnect();
    }
    /**
     * 回收资源
     */
    public void close(){
        if(BleGatt == null){
            return;
        }
        BleGatt.close();
        BleGatt = null;
    }
    /**
     * 请求读取队列中一个Characteristic
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        if(bluetoothAdapter == null || BleGatt == null){
            return;
        }
        characteristicReadQueue.add(characteristic);
        if((characteristicReadQueue.size() == 1) &&
                (descriptorsWriteQueue.size() == 0) &&
                characteristicWriteQueue.size() == 0)
            BleGatt.readCharacteristic(characteristic);
    }
    /**
     * 请求写队列中的一个Descriptor
     */
    public void  writeDescriptor(BluetoothGattDescriptor descriptor){

        descriptorsWriteQueue.add(descriptor);
        if(descriptorsWriteQueue.size() == 1 && characteristicWriteQueue.size() == 0)
            BleGatt.writeDescriptor(descriptor);
    }
    /**
     * 请求写队列中characteristic
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
        if(bluetoothAdapter == null || BleGatt == null){
            return;
        }
        characteristicWriteQueue.add(characteristic);
        if(characteristicWriteQueue.size() == 1)
            BleGatt.writeCharacteristic(characteristicWriteQueue.element());
    }
    /**
     * 启用/停用Characteristic通知,写Descriptor
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enable){
        if(bluetoothAdapter == null || BleGatt == null){
            return;
        }
        BleGatt.setCharacteristicNotification(characteristic,enable);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                BLESensorGatt.CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            writeDescriptor(descriptor);

    }
    /**
     * 获取BLE设备的BLE 服务
     */
    public List<BluetoothGattService> getSupportedGattServices(){
        if(BleGatt == null){
            return  null;
        }
        return  BleGatt.getServices();
    }
}

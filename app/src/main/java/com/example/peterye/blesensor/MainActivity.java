package com.example.peterye.blesensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import com.example.peterye.blesensor.ui.DeviceView;
import com.example.peterye.blesensor.utils.DeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 连接两个BLE设备,并且获取数据：
 * 通过两个BluetoothService来管理连接，
 * 通过设备Mac地址来标识发回来的请求
 */
public class MainActivity extends AppCompatActivity {
    private  final static  String TAG = MainActivity.class.getName();
    private BluetoothAdapter mBluetoothAdapter;

    // 设备的名字和特点
    public static final  String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private LinearLayout content_main;

    private String  mDeviceName;
    private String mDeviceAddress; // 当前最新设备地址
    private  final static int MAX_CONNECT = 2;
    private int connectNum = 0; // 已经连接上的设备数目
    // 连接的设备以及设备的显示布局
    private HashMap<String,DeviceInfo> deviceInfos= new HashMap<String,DeviceInfo>();
    private HashMap<String,DeviceView> deviceViews= new HashMap<String,DeviceView>();

    private HashMap<String,Integer> uuidMapRow = new HashMap<String, Integer>();


    /**
     * 一个服务，管理多个BLE
     */
    private BluetoothService mBluetoothService;

    /**
     * 是否连接两个设备
     */
    private  boolean  mConnected = false;
    private  boolean  mConnected_two = false;

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private  boolean isSensorTag2;

    /**
     * 每个服务的Characteristic都存成一个链表
     */
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacterristic =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private final String LIST_NAME = "NAME";
    private final String List_UUID = "UUID";

    /**
     * 服务的连接回调函数，连接1
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /**
             * 第一个设备没有连接
             */

                mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
                if (!mBluetoothService.initialize()) {
                    finish();
                }

                mBluetoothService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };


    /**
     *  创建BroadcastReceiver接收BLE服务传递过来的状态信息；通过设备地址区分是哪一个设备发的数据
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String address = intent.getStringExtra(BluetoothService.DEVICE_TAG);

            if(BluetoothService.ACTION_GATT_CONNECTED.equals(action)){
                /**
                 *
                 * 设置设备已经连接
                 */
                DeviceInfo info = deviceInfos.get(address);
                info.setConnected(true);
                deviceInfos.remove(address);
                deviceInfos.put(address,info);
                updateConnectionState(R.string.connected,address);
                connectNum ++;
                invalidateOptionsMenu();
            }
            else if(BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
                // 更新view
                updateConnectionState(R.string.disconnected,address);
                invalidateOptionsMenu();
                clearUI();
                connectNum --;
                /**
                 * 设置该设备未连接
                 */
                DeviceInfo info = deviceInfos.get(address);
                info.setConnected(false);
                deviceInfos.remove(address);
                deviceInfos.put(address,info);

            }
            else if(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                // 发现服务的时候，初始化配置传感器，开启传感器、开启通知等操作
                //displayGattServices(mBluetoothService.getSupportedGattServices());
                getAllGattCharaters(mBluetoothService.getSupportedGattServices());
            }
            else if(BluetoothService.ACTION_DATA_AVAILABLE.equals(action)){
                /**
                 * 当数据来了，我们要创建数据显示区域，并且把数据uuid和row绑定
                 */
                final String uuid = intent.getStringExtra(BluetoothService.DATA_UUID);

                displayData(intent.getStringExtra(BluetoothService.EXTRA_DATA),
                        address,uuid);
            }

        }
    };
    /**
     * list 项目的监听器，用于开启 'Read' and 'Notify' 特性.
     */
    private final ExpandableListView.OnChildClickListener  servicesListClicListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    if(mGattCharacterristic != null){
                        final BluetoothGattCharacteristic gattCharacteristic =
                                mGattCharacterristic.get(groupPosition).get(childPosition);
                        final int charaProp = gattCharacteristic.getProperties();


                        if((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0){
                            mBluetoothService.readCharacteristic(gattCharacteristic);
                            Log.i(TAG,"支持读");
                            System.out.println("支持读");
                        }
                        // 支持通知
                        if((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                            mNotifyCharacteristic = gattCharacteristic;
                            mBluetoothService.setCharacteristicNotification(gattCharacteristic,true);
                            Log.i(TAG,"支持通知");
                            System.out.println("支持通知");
                        }
                        return  true;
                    }
                    return false;

                }
            };

    // 启动Activity，Result常量
    private static int REQUEST_ENABLE_BT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isSensorTag2 = false;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // 蓝牙的打开
        final BluetoothManager bluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        // 初始化UI控件
        content_main = (LinearLayout) findViewById(R.id.content_main);

        // 获取传递过来的数据
        processIntent();
    }

    @Override
    protected void onNewIntent(Intent intent1) {
        super.onNewIntent(intent1);
        setIntent(intent1);
        processIntent();

    }
    private  void processIntent(){
        // 获取传递过过来的Intent信息
        final Intent intent = getIntent();
        if (intent != null) {
            mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
            mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
            // 启动Service
            if (mDeviceName != null && mDeviceAddress != null) {
                Toast.makeText(this,"Address: "+mDeviceAddress,Toast.LENGTH_SHORT).show();

                if ((mDeviceName.equals("SensorTag2")) || (mDeviceName.equals("CC2650 SensorTag"))) {
                    isSensorTag2 = true;
                }

                Intent startService = new Intent(this, BluetoothService.class);
                /**
                 * 曾经连接过，就直接启动设备然后连接，
                 * 没有连接过，
                 * 看设备数目是否已经达到最大，没有最大就创建
                 * 看是否存在未连接的设备，
                 */
                if(deviceInfos.containsKey(mDeviceAddress)){
                    bindService(startService, mServiceConnection, BIND_AUTO_CREATE);
                }
                /**
                 * 设备数量未满，创建新的device和deviceView
                 */
                else if(deviceInfos.size() < MAX_CONNECT){
                    deviceInfos.put(mDeviceAddress,new DeviceInfo(mDeviceAddress,mDeviceName));
                    DeviceView deviceView = new DeviceView(this);
                    deviceViews.put(mDeviceAddress,deviceView);
                    // 设置设备名字和mac地址
                    deviceView.setDeviceName(mDeviceName);
                    deviceView.setDeviceMac(mDeviceAddress);
                    // 添加到当前的父布局里面
                    content_main.addView(deviceView);
                    bindService(startService, mServiceConnection, BIND_AUTO_CREATE);

                }
                /**
                 * 设备数量满了，但是有未连接的，新设备来了，更换两个链表的address
                 */
                else if(deviceInfos.size() > connectNum){
                    String unconnectAddress = getUnconnectDeviceMac();
                    deviceInfos.remove(unconnectAddress);
                    deviceInfos.put(mDeviceAddress,new DeviceInfo(mDeviceName,mDeviceName));
                    DeviceView unconnectView = deviceViews.get(unconnectAddress);
                    deviceViews.remove(unconnectAddress);
                    deviceViews.put(mDeviceAddress,unconnectView);
                    bindService(startService, mServiceConnection, BIND_AUTO_CREATE);
                }
            } else {

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       // Toast.makeText(this, TAG+"onresume",Toast.LENGTH_SHORT).show();

        registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());

        if(mBluetoothService != null){
            for(String key:deviceInfos.keySet()){
                mBluetoothService.connect(key);
            }

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(this, TAG+"onpause",Toast.LENGTH_SHORT).show();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  Toast.makeText(this, TAG+"onDestroy",Toast.LENGTH_SHORT).show();
        unbindService(mServiceConnection);
        mBluetoothService = null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.scan_devices){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, DeviceListActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * 获取所有的Characteristic
     */

    private void getAllGattCharaters(List<BluetoothGattService> gattServices) {

        /**
         * Gatt Service 遍历获取UUID信息
         */
        for (BluetoothGattService gattService : gattServices) {
            /**
             * 获取服务的Characteristic
             */
            Log.i("UUID_SERVICE",gattService.getUuid().toString());
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                String uuid = gattCharacteristic.getUuid().toString();
                // 开启气压计服务和通知
                if(uuid.equals(BLESensorGatt.UUID_BAR_DATA.toString())){
                    Log.i("UUID_INIT","气压计服务设置");
                    BluetoothGattCharacteristic configChara =
                            gattService.getCharacteristic(BLESensorGatt.UUID_BAR_CONF);
                    configChara.setValue(new byte[]{0x01});
                    mBluetoothService.writeCharacteristic(configChara);
                    mBluetoothService.setCharacteristicNotification(gattCharacteristic,true);
                }
                // 开启加速计服务和通知
                else if(uuid.equals(BLESensorGatt.UUID_ACC_DATA.toString())){
                    Log.i("UUID_INIT","加速计服务设置");
                    BluetoothGattCharacteristic configChara =
                            gattService.getCharacteristic(BLESensorGatt.UUID_ACC_CONF);
                    configChara.setValue(new byte[]{0x01});
                    mBluetoothService.writeCharacteristic(configChara);
                    mBluetoothService.setCharacteristicNotification(gattCharacteristic,true);
                }
                // 运动加速器
                else if(uuid.equals(BLESensorGatt.UUID_MOV_DATA.toString())){
                    Log.i("UUID_INIT","加速计服务设置");
                    BluetoothGattCharacteristic configChara =
                            gattService.getCharacteristic(BLESensorGatt.UUID_MOV_CONF);
                    configChara.setValue(new byte[]{0x7F,0x02});
                    mBluetoothService.writeCharacteristic(configChara);
                    mBluetoothService.setCharacteristicNotification(gattCharacteristic,true);
                }
            }
            mGattCharacterristic.add(charas);
        }
    }
    /**
     * 显示GATT 服务列表
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if(gattServices == null) return;
        String unknowService = getResources().getString(R.string.unknown_service);
        String unkonwCharacter = getResources().getString(R.string.unknown_character);
        String uuid = null;
        ArrayList<HashMap<String,String>> gattServiceData = new
                ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String,String>>> gattCharacterData =
                new ArrayList<ArrayList<HashMap<String,String>>>();

        /**
         * Gatt Service 遍历获取UUID信息
         */
        for(BluetoothGattService gattService: gattServices){
            HashMap<String,String> currentServiceData = new HashMap<String,String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME,SensorBLEAttribute.loopUp(uuid,unknowService));
            currentServiceData.put(List_UUID,uuid);
            gattServiceData.add(currentServiceData);

            /**
             * 获取服务的Characteristic
             */
            ArrayList<HashMap<String,String>> gattCharaGroupData = new
                    ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
            for(BluetoothGattCharacteristic gattCharacteristic: gattCharacteristics){
                charas.add(gattCharacteristic);
                // Characteristic对应的UUID信息
                uuid = gattCharacteristic.getUuid().toString();
                // 开启气压计服务和通知
                if(uuid.equals(BLESensorGatt.UUID_BAR_DATA.toString())){
                    BluetoothGattCharacteristic configChara =
                            gattService.getCharacteristic(BLESensorGatt.UUID_BAR_CONF);
                    configChara.setValue(new byte[]{0x01});
                    mBluetoothService.writeCharacteristic(configChara);
                    mBluetoothService.setCharacteristicNotification(gattCharacteristic,true);
                }
                HashMap<String,String> currentCharaData =  new HashMap<String,String>();
                currentCharaData.put(LIST_NAME, SensorBLEAttribute.loopUp(uuid,unkonwCharacter));
                currentCharaData.put(List_UUID,uuid);
                gattCharaGroupData.add(currentCharaData);
            }
            gattCharacterData.add(gattCharaGroupData);
            mGattCharacterristic.add(charas);

            /**
             * 显示到数据列表里面
             */
            SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                    this,
                    gattServiceData,
                    android.R.layout.simple_expandable_list_item_2,
                    new String[]{LIST_NAME,List_UUID},
                    new int[]{android.R.id.text1,android.R.id.text2},
                    gattCharacterData,
                    android.R.layout.simple_expandable_list_item_2,
                    new String[]{LIST_NAME,List_UUID},
                    new int[]{android.R.id.text1,android.R.id.text2}
            );
           // mGattServiceList.setAdapter(gattServiceAdapter);
        }

    }

    /**
     * 更新连接状态
     *
     */
    public void updateConnectionState(final  int resid,final String address){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DeviceInfo info = deviceInfos.get(address);
                info.setConnected(true);
                deviceInfos.remove(address);
                deviceInfos.put(address,info);
                connectNum ++;
                // 显示更新
                DeviceView deviceView = deviceViews.get(address);
                deviceView.status.setText(resid);
            }
        });
    }
    /**
     * 设置数据
     */
    private void displayData(final String data,final String address,final String uuid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (data != null) {
                    DeviceView deviceView = deviceViews.get(address);
                    if(uuid.equals(BLESensorGatt.UUID_BAR_DATA.toString()))
                    deviceView.sensorRows.get(0).value.setText(data);
                    else if(uuid.equals(BLESensorGatt.UUID_MOV_DATA.toString())){
                        deviceView.sensorRows.get(1).value.setText(data);
                    }
                }

            }
        });

    }
    /**
     * 清除UI
     */
    private void clearUI() {
        // mGattServiceList.setAdapter((SimpleExpandableListAdapter) null);

    }
    /**
     * Receiver 过滤器
     */
    private static IntentFilter makeGattUpdateIntentFilter(){
        final  IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        return  intentFilter;
    };
    /**
     * 获取未连接的设备的mac
     */
    public String getUnconnectDeviceMac(){
        for(String key: deviceInfos.keySet()){
            DeviceInfo info = deviceInfos.get(key);
            if(info.isConnected() == false){
                return info.getAddress();
            }
        }
        return  null;
    }

}

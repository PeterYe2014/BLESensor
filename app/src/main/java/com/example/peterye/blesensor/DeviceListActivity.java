package com.example.peterye.blesensor;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 *  扫描BLE设备，并且显示在列表里面
 */
public class DeviceListActivity extends AppCompatActivity {
    private final static String TAG = DeviceListActivity.class.getName();
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mhandler;
    private LeDeviceListAdapter mLeDeviceListAdatper;
    private ListView mListView;


    // 扫描的周期
    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        Log.i("TEST", "---------------create -----------------");
       // Toast.makeText(this, TAG + "oncreate", Toast.LENGTH_SHORT).show();
        mListView = (ListView) findViewById(R.id.mlist);
        mhandler = new Handler();
        // 设置点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = mLeDeviceListAdatper.getDevice(position);
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                Intent intent = new Intent(DeviceListActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, deviceName);
                intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallBack);
                    mScanning = false;
                }
                startActivity(intent);

            }
        });

        // 检测是否有BLE特性
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // 获取蓝牙适配器
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
          //  Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    // 菜单提供停止和启动扫描UI


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        if (!mScanning) {
            menu.findItem(R.id.scan_stop).setVisible(false);
            menu.findItem(R.id.scan_start).setVisible(true);
            menu.findItem(R.id.scan_refresh).setActionView(null);
        } else {

            menu.findItem(R.id.scan_stop).setVisible(true);
            menu.findItem(R.id.scan_start).setVisible(false);
            menu.findItem(R.id.scan_refresh).setVisible(true);
        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.scan_start:
                mLeDeviceListAdatper.clear();
                scanBLEDevice(true);
                break;
            case R.id.scan_stop:
                scanBLEDevice(false);
                break;
            case R.id.to_main:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            case R.id.to_test:
                Intent intent1 = new Intent(this, TestActivity.class);
                startActivity(intent1);

        }
        return true;

    }

    // 扫描回调函数，扫到了设备就回调
    private BluetoothAdapter.LeScanCallback mLeScanCallBack =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdatper.addDevice(device);
                            mLeDeviceListAdatper.notifyDataSetChanged();
                        }
                    });
                }
            };

    // 停止或者启动扫描
    private void scanBLEDevice(final boolean enable) {
        if (enable) {
            mhandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallBack);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallBack);

        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallBack);

        }
        invalidateOptionsMenu();

    }

    /**
     * 判断是否打开地理信息
     */
    public static boolean isLocationOpen(final Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //gps定位
        boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider || isNetWorkProvider;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, TAG + "resume", Toast.LENGTH_SHORT).show();
        // android 6.0 动态权限申请 要打开地理位置才能搜索
        //开启位置服务，支持获取ble蓝牙扫描结果
        if (Build.VERSION.SDK_INT >= 23 && !isLocationOpen(getApplicationContext())) {
            Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableLocate, REQUEST_LOCATION_PERMISSION);
        }
        // 判断蓝牙是否打开，请求打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        // 启动扫描
        mLeDeviceListAdatper = new LeDeviceListAdapter();
        mListView.setAdapter(mLeDeviceListAdatper);
        scanBLEDevice(true);


    }

    @Override
    protected void onPause() {
        super.onPause();
//        Toast.makeText(this, TAG + "pause", Toast.LENGTH_SHORT).show();
        scanBLEDevice(false);
        mLeDeviceListAdatper.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 拒绝打开蓝牙
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (isLocationOpen(getApplicationContext())) {
                //Android6.0需要动态申请权限
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    //请求权限
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    }
                }

            } else {
                //若未开启位置信息功能，则退出该应用
                finish();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     *  构造该list的适配器，从而显示列表内容
     *
      */

    private class LeDeviceListAdapter extends BaseAdapter{
        private ArrayList<BluetoothDevice> mLeDevices;
        // 用于解析xml的布局文件
        private LayoutInflater mInflator;

        public LeDeviceListAdapter(){
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceListActivity.this.getLayoutInflater();
        }
        public void addDevice(BluetoothDevice device){
            if(!mLeDevices.contains(device))
              mLeDevices.add(device);
        }
        public BluetoothDevice getDevice(int position){
            return mLeDevices.get(position);
        }
        public void clear(){
            mLeDevices.clear();
        }

        // 列表绘制item数目
        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if(view == null){
                view = mInflator.inflate(R.layout.listitem_device,null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                view.setTag(viewHolder);// 绑定viewHolder到view
            }
            else{
                viewHolder = (ViewHolder) view.getTag();
            }
            // 设置view的viewHolder的内容
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if(device !=null && deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    /**
     * 存放一个列表项目的控件
     */
    static  class ViewHolder{
            TextView deviceName;
            TextView deviceAddress;
    }
}

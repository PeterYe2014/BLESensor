package com.example.peterye.blesensor.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.peterye.blesensor.R;

import java.util.ArrayList;

/**
 * 每个设备的View，需要添加到ACtivity里面
 * Created by PeterYe on 2017/4/3.
 */

public class DeviceView extends LinearLayout{

    public TextView deviceName;
    public TextView deviceAddress;
    public TextView status;
    private Context context;
    public ArrayList<GerneralRow> sensorRows = new ArrayList<GerneralRow>();

    public DeviceView(Context context) {
        super(context);
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
        this.deviceName = new TextView(context){
            {
                setId(R.id.dn);
                setTextSize(20);
                setTypeface(null, Typeface.BOLD);
                setText("设备");
            }
        };
        this.deviceAddress = new TextView(context){
            {
                setId(R.id.da);
                setTextSize(15);
                setText("MAC:");
            }
        };
        this.status = new TextView(context){
            {
                setId(R.id.ds);
                setTextSize(15);
                setText("未连接");
            }
        };
        /**
         * MAc地址布局
         */
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        layoutParams.leftMargin = 10;
        deviceAddress.setLayoutParams(layoutParams);
        /**
         * 状态布局
         */
        layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        layoutParams.topMargin = 10;
        status.setLayoutParams(layoutParams);

        addView(deviceName);
        addView(deviceAddress);
        addView(status);
        // 添加默认row
        addSensorRow();
        addSensorRow();
    }
    public void addSensorRow(){
        GerneralRow gerneralRow = new GerneralRow(context);
        addSensorRow(gerneralRow);
    }
    public void addSensorRow(GerneralRow row){
        /**
         * 分配id
         */
        row.setId(sensorRows.size());
        /**
         * 为数据域布局
         */
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        layoutParams.topMargin = 10;
        row.setLayoutParams(layoutParams);
        sensorRows.add(row);
        addView(row);
    }
    public void setDeviceName(String name){
        deviceName.setText(name);
    }
    public void setDeviceMac(String mac){
        deviceAddress.setText(mac);
    }

}

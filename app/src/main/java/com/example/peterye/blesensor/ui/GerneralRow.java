package com.example.peterye.blesensor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.peterye.blesensor.R;

/**
 * 每一个传感器数据显示的主类，可以继承来扩展显示功能
 * Created by PeterYe on 2017/4/3.
 */

public class GerneralRow extends RelativeLayout{

    public  TextView title;
    public  TextView value;
    private   Context context;

    public GerneralRow(Context context){
        super(context);
        this.context = context;
        /**
         * 数据的标题和值
         */
        title = new TextView(context){
            {
                setTextSize(TypedValue.COMPLEX_UNIT_PT,10.0f);
                setTypeface(null,Typeface.BOLD);
                setText("数据名称");
                setId(R.id.t1);
            }
        };
        value = new TextView(context){
            {
                setTextSize(TypedValue.COMPLEX_UNIT_PT,8.0f);

                setText("数据值");
                setId(R.id.t2);
            }
        };
        /**
         * 定义布局
         */
        RelativeLayout.LayoutParams tmpParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        tmpParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,TRUE);
        title.setLayoutParams(tmpParams);

        tmpParams =  new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        tmpParams.addRule(RelativeLayout.RIGHT_OF,title.getId());
        tmpParams.addRule(RelativeLayout.ALIGN_BOTTOM,title.getId());
        tmpParams.leftMargin = 15;
        value.setLayoutParams(tmpParams);

        addView(title);
        addView(value);

    }

    public GerneralRow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GerneralRow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}

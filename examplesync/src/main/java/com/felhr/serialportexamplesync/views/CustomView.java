package com.felhr.serialportexamplesync.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.felhr.serialportexamplesync.Device;
import com.felhr.serialportexamplesync.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CustomView extends View {
    // WeakReference m; // DEBUG

    // Scaling Variables
    double topleftY, topleftX, bottomleftY, toprightX;
    double scaleX, scaleY;

    // top right: -7.26700, 112.81129
    // bottom right: -7.29466,112.81124
    // bottom left: -7.29456, 112.78328
    // top left: -7.26707, 112.78335

    // Drawing Variables
    private Bitmap mImage;
    private Paint mPaintRef, mPaintDevices, mPaintMyDevice;

    // Devices Variables
    private List<Device> refList, devicesList;
    private Device myDevice;

    // Constructors

    public CustomView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet set) {
        // m = new WeakReference(context); // DEBUG

        // Set Paints
        mPaintRef = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRef.setColor(Color.RED);

        mPaintMyDevice = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMyDevice.setColor(Color.GREEN);

        mPaintDevices = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDevices.setColor(Color.BLUE);

        // Set References Position
        refList = new ArrayList<Device>();

        refList.add(new Device("REF1", -7.275435, 112.798698));
        refList.add(new Device("REF2", -7.281127, 112.791019));
        refList.add(new Device("REF3", -7.283580, 112.798641));

        // Set My Position
        myDevice = new Device("null", 0.0, 0.0);

        // Set Devices Position
        devicesList = new ArrayList<Device>();

        // DEBUG
        /*
        Toast.makeText((Context) m.get(),
            "scale x: " + String.valueOf(scaleX) + ", scale y: " + String.valueOf(scaleY)+
            ", cx: " + String.valueOf(cx) + ", cy: " + String.valueOf(cy),
            Toast.LENGTH_SHORT).show();
        */
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Make view width same as view height
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);

        // Set scaling
        topleftY = -7.26707;
        topleftX = 112.78335;

        bottomleftY = -7.29456;
        toprightX = 112.81129;

        scaleY = width / (topleftY - bottomleftY);
        scaleX = width / (toprightX - topleftX);

        // Set map
        mImage = BitmapFactory.decodeResource(getResources(), R.drawable.map_its_2);
        mImage = getResizedBitmap(mImage, width, width);
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        Matrix matrix = new Matrix();

        RectF src = new RectF(0,0, bitmap.getWidth(), bitmap.getHeight());
        RectF dst = new RectF(0,0, reqWidth, reqHeight);

        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mImage, 0, 0, null);

        drawPins(canvas, refList, mPaintRef);
        drawPins(canvas, devicesList, mPaintDevices);
        drawPin(canvas, myDevice.getLongitude(), myDevice.getLatitude(), mPaintMyDevice);
    }

    // Drawing Methods

    private void drawPins(Canvas canvas, List<Device> devices, Paint paint) {
        int len = devices.size();

        for(int i = 0; i < len; i++) {
            Device tmp = devices.get(i);
            drawPin(canvas, tmp.getLongitude(), tmp.getLatitude(), paint);
        }
    }

    private void drawPin(Canvas canvas, double longitude, double latitude, Paint paint) {
        double cx, cy, r;

        cx = (longitude - topleftX) * scaleX;
        cy = (topleftY - latitude) * scaleY;
        r = 10;

        canvas.drawCircle((float) cx, (float) cy, (float) r, paint);
    }

    // Public Methods

    public void updateMyPosition(Device device) {
        myDevice = device;
        postInvalidate();
    }

    public void updateDevicesPosition(List<Device> devices) {
        devicesList = devices;
        postInvalidate();
    }
}

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
    // TODO: draw device object's name in a label

    // WeakReference m; // DEBUG

    double topleftY, topleftX, bottomleftY, toprightX;
    // top left: -7.26707, 112.78335
    // bottom left: -7.29456, 112.78328
    // bottom right: -7.29466,112.81124
    // top right: -7.26700, 112.81129
    double scaleX, scaleY;

    private Bitmap mImage;

    private List<Device> refList, devicesList;
    private Device myDevice;
    private Paint mPaintRef, mPaintDevices, mPaintMyDevice;

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

    // Methods

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Make view width same as view height
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);

        // Set constants
        scaleY = width / (topleftY - bottomleftY);
        scaleX = width / (toprightX - topleftX);

        // Resize ITS Map
        mImage = getResizedBitmap(mImage, width, width);
    }

    private void init(Context context, @Nullable AttributeSet set) {
        // m = new WeakReference(context); // DEBUG

        // Set constants
        topleftY = -7.26707;
        topleftX = 112.78335;

        bottomleftY = -7.29456;
        toprightX = 112.81129;

        // Set Paints
        mPaintRef = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRef.setColor(Color.RED);

        mPaintMyDevice = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMyDevice.setColor(Color.GREEN);

        mPaintDevices = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDevices.setColor(Color.BLUE);

        // Resize ITS map
        mImage = BitmapFactory.decodeResource(getResources(), R.drawable.map_its_2);

        // Set References Position
        refList = new ArrayList<Device>();

        refList.add(new Device("REF1", -7.275435, 112.798698));
        refList.add(new Device("REF2", -7.281127, 112.791019));
        refList.add(new Device("REF3", -7.283580, 112.798641));

        // Set My Position
        // myDevice = new Device("TRI1", -7.27961, 112.79794); // TC
        myDevice = new Device("null", 0.0, 0.0);

        // Set Devices Position
        devicesList = new ArrayList<Device>();

        /*
        Toast.makeText((Context) m.get(),
            "scale x: " + String.valueOf(scaleX) + ", scale y: " + String.valueOf(scaleY)+
            ", cx: " + String.valueOf(cx) + ", cy: " + String.valueOf(cy),
            Toast.LENGTH_SHORT).show();
        */
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

        drawPin(canvas, refList, mPaintRef);
        drawPin(canvas, devicesList, mPaintDevices);
        drawMyPin(canvas);
    }

    // Drawing Methods

    private void drawPin(Canvas canvas, List<Device> devices, Paint paint) {
        int len = devices.size();

        for(int i = 0; i < len; i++) {
            Device tmp = devices.get(i);
            double cx, cy, r;

            cx = (tmp.getLongitude() - topleftX) * scaleX;
            cy = (topleftY - tmp.getLatitude()) * scaleY;
            r = 10;

            canvas.drawCircle((float) cx, (float) cy, (float) r, paint);
        }
    }

    private void drawMyPin(Canvas canvas) {
        double cx, cy, r;

        cx = (myDevice.getLongitude() - topleftX) * scaleX;
        cy = (topleftY - myDevice.getLatitude()) * scaleY;
        r = 10;

        canvas.drawCircle((float) cx, (float) cy, (float) r, mPaintMyDevice);
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

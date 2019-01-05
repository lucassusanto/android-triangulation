package com.felhr.serialportexamplesync.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.felhr.serialportexamplesync.Device;
import com.felhr.serialportexamplesync.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class CustomView extends View {
     WeakReference m; // DEBUG

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
         m = new WeakReference(context); // DEBUG

        // Set Paints
        mPaintRef = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRef.setColor(Color.RED);

        mPaintMyDevice = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMyDevice.setColor(Color.GREEN);

        mPaintDevices = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDevices.setColor(Color.BLUE);

        // Set References Position
        refList = new ArrayList<Device>();

        refList.add(new Device("REF1", -7.274922, 112.800401));
        refList.add(new Device("REF2", -7.283147, 112.790550));
        refList.add(new Device("REF3", -7.286468, 112.804709));

        // Set My Position
        myDevice = new Device("null", 0.0, 0.0);

        // Set Devices Position
        devicesList = new ArrayList<Device>();
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

//        drawRadius(canvas); // DEBUG
//        drawIntersectionPoints(canvas); // DEBUG

        drawPins(canvas, refList, mPaintRef);
        drawPins(canvas, devicesList, mPaintDevices);
        drawPin(canvas, myDevice.getLongitude(), myDevice.getLatitude(), mPaintMyDevice);
    }

    // Triangulation

    private class Point {
        public double x, y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // DEBUG
    /*
    Device dummyDev1;
    Device dummyDev2;
    double[] drawRadius = new double[3];
    */

    // Main Calculation
    private Device getTriangulatedPosition(Device device) {
        int refCount = refList.size();

        double[]
            dist = new double[3],
            rLon = new double[3],
            rLat = new double[3];

        double
            dLon = device.getLongitude(),
            dLat = device.getLatitude();

        // Calc Distances
        for(int i = 0; i < refCount; i++) {
            Device tmp = refList.get(i);

            rLon[i] = tmp.getLongitude();
            rLat[i] = tmp.getLatitude();

            dist[i] = distance(rLon[i], rLat[i], dLon, dLat);
            // drawRadius[i] = dist[i]; // DEBUG
        }

        // Calc Device Point
        Point point = calcPoint(
            rLon[0], rLat[0], dist[0],
            rLon[1], rLat[1], dist[1],
            rLon[2], rLat[2], dist[2]);

        // Return
        Device newDevice = new Device(device.getName(), point.y, point.x);
        return newDevice;
    }

    // Get True Point
    private Point calcPoint(double x1, double y1, double r1,
                          double x2, double y2, double r2,
                          double x3, double y3, double r3) {

        // Calc intersection points
        double
            a = x1 * x1 - x2 * x2 - r1 * r1 + r2 * r2 + y1 * y1 - y2 * y2,
            b = 2 * (x1 - x2),
            c = y2 - y1,
            bPow = Math.pow(b, 2),
            cPow = Math.pow(c, 2),
            d = bPow + 4 * cPow,
            dd = 1 / (2 * d),
            e = 4 * a * c,
            f = 2 * bPow * y1,
            g = 4 * b * c * x1,
            h = 2 * a * b * x1;

        double
            y_1 = dd * (Math.sqrt(Math.pow((e - f - g), 2) - 4 * d *
                (Math.pow(a, 2) - h - bPow * Math.pow(r1, 2) + bPow * Math.pow(x1, 2) + bPow * Math.pow(y1, 2)))
                - e + f + g),
            x_1 = calcX(y_1, x1, x2, y1, y2, r1, r2),

            y_2 = dd * (-Math.sqrt(Math.pow((e - f - g), 2) - 4 * d *
                (Math.pow(a, 2) - h - bPow * Math.pow(r1, 2) + bPow * Math.pow(x1, 2) + bPow * Math.pow(y1, 2)))
                - e + f + g),
            x_2 = calcX(y_2, x1, x2, y1, y2, r1, r2);

        // Find the right point
        double
            dX1 = (x_1 - x3), dY1 = (y_1 - y3),
            dX2 = (x_2 - x3), dY2 = (y_2 - y3);

        double
            rad1 = Math.sqrt((dX1 * dX1 + dY1 * dY1)),
            rad2 = Math.sqrt((dX2 * dX2 + dY2 * dY2));

        double
            dR1 = Math.abs(r3 - rad1),
            dR2 = Math.abs(r3 - rad2);

        // DEBUG
        /*
        dummyDev1 = new Device("tes1", y_1, x_1);
        dummyDev2 = new Device("tes2", y_2, x_2);
        */

        // Return
        if(dR1 < dR2) return new Point(x_1, y_1);
        return new Point(x_2, y_2);
    }

    // Distance between (x1, y1) and (x2, y2)
    private double distance(double x1, double y1, double x2, double y2) {
        double
            dX = (x2 - x1),
            dY = (y2 - y1);

        double
            result = Math.sqrt(((dX * dX) + (dY * dY)));

        return result;
    }

    // Get x position from y
    private double calcX(double y, double x1, double x2, double y1, double y2, double r1, double r2) {
        return (- r1 * r1 + r2 * r2 + x1 * x1 - x2 * x2 - 2 * y * y1 + 2 * y * y2 + y1 * y1 - y2 * y2) / (2*(x1 - x2));
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

    // DEBUG
    /*
    private void drawRadius(Canvas canvas) {
        double cx, cy, r;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if(drawRadius[2] != 0.0) {
            cx = (112.804709 - topleftX) * scaleX;
            cy = (topleftY + 7.286468) * scaleY;
            r = drawRadius[2] * scaleX;
            paint.setColor(Color.MAGENTA);
            canvas.drawCircle((float) cx, (float) cy, (float) r, paint);
        }

        if(drawRadius[1] != 0.0) {
            cx = (112.790550 - topleftX) * scaleX;
            cy = (topleftY + 7.283147) * scaleY;
            r = drawRadius[1] * scaleX;
            paint.setColor(Color.CYAN);
            canvas.drawCircle((float) cx, (float) cy, (float) r, paint);
        }

        if(drawRadius[0] != 0.0) {
            cx = (112.800401 - topleftX) * scaleX;
            cy = (topleftY + 7.274922) * scaleY;
            r = drawRadius[0] * scaleX;

            paint.setColor(Color.YELLOW);

            canvas.drawCircle((float) cx, (float) cy, (float) r, paint);
        }
    }

    private void drawIntersectionPoints(Canvas canvas) {
        Paint dummyPaint = new Paint();
        dummyPaint.setColor(Color.BLUE);

        drawPin(canvas, dummyDev1.getLongitude(), dummyDev1.getLatitude(), dummyPaint);
        drawPin(canvas, dummyDev2.getLongitude(), dummyDev2.getLatitude(), dummyPaint);
    }
    */

    // Public Methods

    public void updateMyPosition(Device device) {
        myDevice = getTriangulatedPosition(device);
        postInvalidate();
    }

    public void updateDevicesPosition(List<Device> devices) {
        int nLen = devices.size();

        devicesList.clear();
        for(int i = 0; i < nLen; i++) {
            devicesList.add(getTriangulatedPosition(devices.get(i)));
        }

        postInvalidate();
    }
}

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
import android.widget.Toast;

import com.felhr.serialportexamplesync.Device;
import com.felhr.serialportexamplesync.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class CustomView extends View {
    private WeakReference mActivity;

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

    // Debug
    private boolean debug = false;

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
        mActivity = new WeakReference(context);

        // Set Paints
        mPaintRef = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRef.setColor(Color.RED);

        mPaintMyDevice = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMyDevice.setColor(Color.GREEN);

        mPaintDevices = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDevices.setColor(Color.BLUE);

        // Set References Position
        initReferences();

        // Set My Position
        myDevice = new Device("null", 0.0, 0.0);

        // Set Devices Position
        devicesList = new ArrayList<Device>();
    }

    private void initReferences() {
        refList = new ArrayList<Device>();

        refList.add(new Device("REF1", -7.274922, 112.800401));
        refList.add(new Device("REF2", -7.283147, 112.790550));
        refList.add(new Device("REF3", -7.286468, 112.804709));
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

    // Triangulation

    private class Point {
        public double x, y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // Main Calculation
    private Device getTriangulatedPosition(Device device) {
        double[] dist = getDistances(device.getLongitude(), device.getLatitude());

        Point[] points = getIntersectionPoints(
                refList.get(0).getLongitude(), refList.get(0).getLatitude(),dist[0],
                refList.get(1).getLongitude(), refList.get(1).getLatitude(),dist[1]);

        Point point = getTruePoint(points, refList.get(2).getLongitude(), refList.get(2).getLatitude(), dist[2]);

        if(debug) {
            double errorDist = distanceInMeter(device.getLongitude(), device.getLatitude(),
                    point.x, point.y);

            Toast.makeText((Context) mActivity.get(), device.getName() + " Position Error: " + String.valueOf(errorDist) +
                    " meter", Toast.LENGTH_SHORT).show();
        }

        return new Device(device.getName(), point.y, point.x);
    }

    // Distance between (x, y) to all reference points
    private double[] getDistances(double x, double y) {
        double[] dist = new double[3];

        dist[0] = distance(x, y, refList.get(0).getLongitude(), refList.get(0).getLatitude());
        dist[1] = distance(x, y, refList.get(1).getLongitude(), refList.get(1).getLatitude());
        dist[2] = distance(x, y, refList.get(2).getLongitude(), refList.get(2).getLatitude());

        return dist;
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

    // Intersection Points between (x1, y1, r1) and (x2, y2, r2) circles
    private Point[] getIntersectionPoints(double x1, double y1, double r1,
                                        double x2, double y2, double r2) {
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
            h = 2 * a * b * x1,
            i = Math.pow(a, 2),
            j = bPow * Math.pow(r1, 2),
            k = bPow * Math.pow(x1, 2),
            l = bPow * Math.pow(y1, 2),
            m = Math.pow((e - f - g), 2),
            n = (i - h - j + k + l),
            o = f + g - e,
            p = Math.sqrt(m - 4 * d * n);

        double
            y_1 = dd * (o + p),
            x_1 = calcX(y_1, x1, x2, y1, y2, r1, r2),

            y_2 = dd * (o - p),
            x_2 = calcX(y_2, x1, x2, y1, y2, r1, r2);

        Point[] points = new Point[2];

        points[0] = new Point(x_1, y_1);
        points[1] = new Point(x_2, y_2);

        return points;
    }

    // Get x position from y
    private double calcX(double y, double x1, double x2, double y1, double y2, double r1, double r2) {
        double result = (- r1 * r1 + r2 * r2 + x1 * x1 - x2 * x2 - 2 * y * y1 + 2 * y * y2 + y1 * y1 - y2 * y2)
                / (2*(x1 - x2));
        return result;
    }

    // Get Right Point between 2 Intersection Points
    private Point getTruePoint(Point[] points, double x, double y, double r) {
        double
            x1 = points[0].x, y1 = points[0].y,
            x2 = points[1].x, y2 = points[1].y;

        double
            dX1 = (x1 - x), dY1 = (y1 - y),
            dX2 = (x2 - x), dY2 = (y2 - y);

        double
            rad1 = Math.sqrt((dX1 * dX1 + dY1 * dY1)),
            rad2 = Math.sqrt((dX2 * dX2 + dY2 * dY2));

        double
            dR1 = Math.abs(r - rad1),
            dR2 = Math.abs(r - rad2);

        if(dR1 < dR2) {
            return points[0];
        }

        return points[1];
    }

    // Distance between (x1, y1) and (x2, y2) in meter
    private double distanceInMeter(double x1, double y1, double x2, double y2) {
        double R = 6378.137;
        double dLat = y2 * Math.PI / 180 - y1 * Math.PI / 180;
        double dLon = x2 * Math.PI / 180 - x1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(y1 * Math.PI / 180) * Math.cos(y2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // centi meters
    }

    private double round(double val, int decimals) {
        double div = Math.pow(10, decimals);
        return Math.round(val * div) / div;
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

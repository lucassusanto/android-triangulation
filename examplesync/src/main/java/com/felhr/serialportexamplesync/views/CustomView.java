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

public class CustomView extends View {
    private WeakReference mActivity;

    // top right: -7.26700, 112.81129
    // bottom right: -7.29466, 112.81124
    // bottom left: -7.29456, 112.78328
    // top left: -7.26707, 112.78335

    // Scaling Variables
    private double topleftY, topleftX, bottomleftY, toprightX;
    private double scaleX, scaleY;

    // Revisi: Scaling Variables
    private double mapLenInPixel, mapLenInMeter;

    // Drawing Variables
    private Bitmap mImage;
    private Paint mPaintRef, mPaintDevices, mPaintMyDevice;

    // Devices Variables
    private List<Device> refList, devicesList;
    private Device myDevice;

    // Revisi: Devices Variables
    private List<Point> refPoints, refPixels, devicesList2;
    private Point myDevice2;

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

        // Init Variable
        devicesList = new ArrayList<Device>();

        // Revisi: Init Variable
        devicesList2 = new ArrayList<Point>();

        // Set My Position
        myDevice = new Device("null", 0.0, 0.0);

        // Revisi: My Position
        myDevice2 = new Point(-1, -1);
    }

    private void initReferences() {
        refList = new ArrayList<Device>();

        refList.add(new Device("REF1", -7.274922, 112.800401));
        refList.add(new Device("REF2", -7.283147, 112.790550));
        refList.add(new Device("REF3", -7.286468, 112.804709));

        // Revisi: Ref Points
        refPoints = new ArrayList<Point>();

        refPoints.add(new Point(112.800401, -7.274922));    // <- Edit this
        refPoints.add(new Point(112.790550, -7.283147));    // <- Edit this
        refPoints.add(new Point(112.804709, -7.286468));    // <- Edit this

        // Revisi: Ref Pixels
        refPixels = new ArrayList<Point>();

        refPixels.add(new Point(439.39, 205.65));   // <- Edit this
        refPixels.add(new Point(185.54, 421.07));   // <- Edit this
        refPixels.add(new Point(550.41, 508.05));   // <- Edit this
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Make view width same as view height
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);

        // Set Scaling
        topleftY = -7.26707;
        topleftX = 112.78335;

        bottomleftY = -7.29456;
        toprightX = 112.81129;

        scaleY = width / (topleftY - bottomleftY);
        scaleX = width / (toprightX - topleftX);

        // Revisi: Scaling
        mapLenInMeter = 3085.3; // <- Edit this value in meters
        mapLenInPixel = width;

        // Set Map
        mImage = BitmapFactory.decodeResource(getResources(), R.drawable.map_its_2);
        mImage = getResizedBitmap(mImage, width, width);

        // Revisi: My Position
        myDevice2 = getTriangulatedPosition(new Point(112.797578, -7.279922)); // TC
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

//        drawPins(canvas, refList, mPaintRef);
//        drawPins(canvas, devicesList, mPaintDevices);
//        drawPin(canvas, myDevice.getLongitude(), myDevice.getLatitude(), mPaintMyDevice);

        // Revisi

        drawPins2(canvas, refPixels, mPaintRef);
        drawPins2(canvas, devicesList2, mPaintDevices);
        drawPin2(canvas, myDevice2, mPaintMyDevice);
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
    private Point getTriangulatedPosition(Point point) {
        double[] dist = getDistances(point);

        Point[] points = getIntersectionPoints(
            refPixels.get(0), dist[0],
            refPixels.get(1), dist[1]
        );

        return getTruePoint(points, refPixels.get(2), dist[2]);
    }

    private Point getTriangulatedPosition(Device device) {
        return getTriangulatedPosition(new Point(device.getLongitude(), device.getLatitude()));
    }

    private double[] getDistances(Point point) {
        double[] dist = new double[3];

        dist[0] = squashDistance(distanceToPixel(distance(point, refPoints.get(0))));
        dist[1] = squashDistance(distanceToPixel(distance(point, refPoints.get(1))));
        dist[2] = squashDistance(distanceToPixel(distance(point, refPoints.get(2))));

        return dist;
    }

    private Point[] getIntersectionPoints(Point equation1, double r1,
                                          Point equation2, double r2) {
        double
            x1 = equation1.x, y1 = equation1.y,
            x2 = equation2.x, y2 = equation2.y;

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

    private double calcX(double y, double x1, double x2, double y1, double y2, double r1, double r2) {
        return (- r1 * r1 + r2 * r2 + x1 * x1 - x2 * x2 - 2 * y * y1 + 2 * y * y2 + y1 * y1 - y2 * y2) / (2*(x1 - x2));
    }

    private Point getTruePoint(Point[] points, Point equation3, double r3) {
        double
            x1 = points[0].x, y1 = points[0].y,
            x2 = points[1].x, y2 = points[1].y;

        double
            dX1 = (x1 - equation3.x), dY1 = (y1 - equation3.y),
            dX2 = (x2 - equation3.x), dY2 = (y2 - equation3.y);

        double
            rad1 = Math.sqrt((dX1 * dX1 + dY1 * dY1)),
            rad2 = Math.sqrt((dX2 * dX2 + dY2 * dY2));

        double
            dR1 = Math.abs(r3 - rad1),
            dR2 = Math.abs(r3 - rad2);

        if(dR1 < dR2) return points[0];
        return points[1];
    }

    private double distanceInMeter(double x1, double y1, double x2, double y2) {
        double R = 6378.137;
        double dLat = y2 * Math.PI / 180 - y1 * Math.PI / 180;
        double dLon = x2 * Math.PI / 180 - x1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(y1 * Math.PI / 180) * Math.cos(y2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
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

    // Revisi: Drawing Methods
    
    private void drawPins2(Canvas canvas, List<Point> points, Paint paint) {
        int len = points.size();

        for(int i = 0; i < len; i++) {
            Point point = points.get(i);
            drawPin2(canvas, point, paint);
        }
    }

    private void drawPin2(Canvas canvas, Point point, Paint paint) {
        canvas.drawCircle((float) point.x, (float) point.y, 10.0f, paint);
    }

    // Distance Methods

    private Point distance(Point point1, Point point2) {
        return new Point(Math.abs(point1.x - point2.x), Math.abs(point1.y - point2.y));
    }

    private Point distanceToPixel(Point point) {
        // Convert to meter
        double
            x = point.x * 110570,
            y = point.y * 111320;
        
        // Convert to pixel
        x = x * mapLenInPixel / mapLenInMeter;
        y = y * mapLenInPixel / mapLenInMeter;
        
        return new Point(x, y);
    }

    private double squashDistance(Point point) {
        return Math.sqrt(point.x * point.x + point.y * point.y);
    }

    // Public Methods

    public void updateMyPosition(Device device) {
        myDevice2 = getTriangulatedPosition(device);
        postInvalidate();
    }

    public void updateDevicesPosition(List<Device> devices) {
        int nLen = devices.size();

        devicesList.clear();
        for(int i = 0; i < nLen; i++) {
             devicesList2.add(getTriangulatedPosition(devices.get(i)));
        }

        postInvalidate();
    }
}

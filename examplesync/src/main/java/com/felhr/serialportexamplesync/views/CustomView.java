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
    // Variables

    WeakReference m; // DEBUG

    // Constructors

    public CustomView(Context context) {
        super(context);
        init(context, null); // Is this line needed?
    }
    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs); // Is this line needed?
    }
    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs); // Is this line needed?
    }
    public CustomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs); // Is this line needed?
    }

    // Methods

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Make view square
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);

        // Set scale constants

    }

    private void init(Context context, @Nullable AttributeSet set) {
        m = new WeakReference(context); // DEBUG

        /*
        Toast.makeText((Context) m.get(),
            "width: " + String.valueOf(getWidth()) + ", height: " + String.valueOf(getHeight()),
            Toast.LENGTH_SHORT).show();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                mImage = getResizedBitmap(mImage, getWidth(), getHeight());
            }
        });
        */
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw ITS Map
        Bitmap mImage;

        mImage = BitmapFactory.decodeResource(getResources(), R.drawable.map_its_2);
        mImage = getResizedBitmap(mImage, getWidth(), getHeight());

        canvas.drawBitmap(mImage, 0, 0, null);

        // Draw References Position
        List<Device> list = new ArrayList<Device>();

        list.add(new Device("REF1", ));
        list.add(new Device("REF2", ));
        list.add(new Device("REF3", ));
        list.add(new Device("REF4", ));

        drawReferencesPin(list);

        // ---------------------------- DEBUGGING ----------------------------
        // Draw My Device

        // Draw Other Devices

    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        Matrix matrix = new Matrix();

        RectF src = new RectF(0,0, bitmap.getWidth(), bitmap.getHeight());
        RectF dst = new RectF(0,0, reqWidth, reqHeight);

        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Drawing Methods

    private void drawReferencesPin(List<Device> referencesList) {
        int len = referencesList.size();

        for(int i = 0; i < len; i++) {

        }
    }

    public void drawMyPin(Device myDevice) {

    }

    public void drawDevicesPin(List<Device> devicesList) {
        int len = devicesList.size();

        for(int i = 0; i < len; i++) {

        }
    }
}

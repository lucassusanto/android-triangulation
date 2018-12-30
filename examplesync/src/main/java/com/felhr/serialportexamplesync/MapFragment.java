package com.felhr.serialportexamplesync;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.felhr.serialportexamplesync.views.CustomView;

import java.lang.ref.WeakReference;
import java.util.List;

public class MapFragment extends Fragment {
    private WeakReference mActivity;
    private CustomView mapView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.cv_map);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = new WeakReference(context);
    }

    // Update Map Section

    public void updateMyPosition(Device device) {
        mapView.updateMyPosition(device);
    }

    public void updateDevicesPosition(List<Device> devices) {
        mapView.updateDevicesPosition(devices);
        Toast.makeText((MainActivity) mActivity.get(), "Map is updated!", Toast.LENGTH_SHORT).show();
    }
}

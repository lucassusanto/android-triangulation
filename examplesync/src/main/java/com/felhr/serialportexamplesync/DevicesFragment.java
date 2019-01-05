package com.felhr.serialportexamplesync;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class DevicesFragment extends Fragment {
    private WeakReference mActivity;

    private ListView lvDevice;
    private Button btnClear;

    OnDevicesMessageListener devicesMessageListener;

    public interface OnDevicesMessageListener {
        public void onClearDevicesInvoked();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        lvDevice = view.findViewById(R.id.lv_devices);
        btnClear = view.findViewById(R.id.btnClear);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devicesMessageListener.onClearDevicesInvoked();
                Toast.makeText((MainActivity) mActivity.get(), "Devices were cleared!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        mActivity = new WeakReference(context);

        try {
            devicesMessageListener = (OnDevicesMessageListener) activity;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must override onClearDevicesInvoked");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDevicesList();
    }

    public void updateDevicesList() {
        lvDevice.setAdapter(((MainActivity) mActivity.get()).devAdapter);
    }
}

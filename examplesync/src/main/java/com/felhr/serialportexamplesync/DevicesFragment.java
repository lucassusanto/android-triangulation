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
import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends Fragment {
    private WeakReference mActivity;

    private ListView lvDevice;
    private Button btnRefresh;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        lvDevice = view.findViewById(R.id.lv_devices);
        btnRefresh = view.findViewById(R.id.btnRefresh);

        MainActivity m = (MainActivity) mActivity.get();
        lvDevice.setAdapter(m.devAdapter);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity m = (MainActivity) mActivity.get();
                lvDevice.setAdapter(m.devAdapter);

                Toast.makeText(m, "Device list was updated!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = new WeakReference(context);
    }
}

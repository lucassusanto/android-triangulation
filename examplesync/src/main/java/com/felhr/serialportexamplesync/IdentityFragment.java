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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class IdentityFragment extends Fragment {
    private WeakReference mActivity;

    private EditText txMyName;
    private TextView txMyLat;
    private TextView txMyLon;
    private TextView txVerbose;

    private Button btnChangeDevName;
    private Button btnChangeVerbose;
    private Button btnRefresh;

    // MainActivity <- ConsoleFragment
    OnIdentityMessageListener identityMessageListener;

    public interface OnIdentityMessageListener {
        public void onDeviceNameChanged(String message);
        public void onVerboseLevelChanged(int level);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_identity, container, false);

        txMyName = view.findViewById(R.id.txMyName);
        txMyLat = view.findViewById(R.id.txMyLat);
        txMyLon = view.findViewById(R.id.txMyLon);
        txVerbose = view.findViewById(R.id.txVerbose);

        btnChangeDevName = view.findViewById(R.id.btnChangeDevName);
        btnChangeVerbose = view.findViewById(R.id.btnChangeVerbose);
        btnRefresh = view.findViewById(R.id.btnRefresh);

        btnChangeDevName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            MainActivity m = (MainActivity) mActivity.get();
            String newName = txMyName.getText().toString();

            if(!newName.equals("")) {
                identityMessageListener.onDeviceNameChanged(newName);
                Toast.makeText(m, "Device name change was requested", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(m, "Device name cannot be empty!", Toast.LENGTH_SHORT).show();
            }
            }
        });

        btnChangeVerbose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            MainActivity m = (MainActivity) mActivity.get();
            int newVal = Integer.parseInt(txVerbose.getText().toString());

            identityMessageListener.onVerboseLevelChanged(newVal);
            Toast.makeText(m, "Verbose level was changed!", Toast.LENGTH_SHORT).show();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            MainActivity m = (MainActivity) mActivity.get();

            txMyName.setText(m.getMyName());
            txVerbose.setText(String.valueOf(m.getVerbose()));
            IdentityFragment.this.updateMyPosition();

            Toast.makeText(m, "All value has been refreshed", Toast.LENGTH_SHORT).show();
            }
        });

        // DEBUGGING
        Button btnInsertDev = view.findViewById(R.id.btnID);
        Button btnInsertDev2 = view.findViewById(R.id.btnID2);

        btnInsertDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            MainActivity m = (MainActivity) mActivity.get();

            m.updateDevicePosition("nRF24L01< TRI3 -7.285421 112.799283");
            m.updateDevicePosition("nRF24L01< TRI4 -7.282406 112.794129");
            m.updateDevicePosition("nRF24L01< TRI3 -7.285421 112.799284");
            m.updateDevicePosition("nRF24L01< TRI3 -7.285591 112.79827");
            m.updateDevicePosition("nRF24L01< TRI4 -7.285410 112.799311");
            }
        });

        btnInsertDev2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            MainActivity m = (MainActivity) mActivity.get();

            m.updateDevicePosition("nRF24L01< TRI3 -7.280578 112.792413");
            m.updateDevicePosition("nRF24L01< TRI4 -7.282068 112.793818");
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
            identityMessageListener = (OnIdentityMessageListener) activity;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must override onDeviceNameChanged");
        }
    }

    public void updateMyPosition() {
        MainActivity m = (MainActivity) mActivity.get();

        txMyLat.setText(String.valueOf(round(m.getMyLat(), 6)));
        txMyLon.setText(String.valueOf(round(m.getMyLon(), 6)));
    }

    private double round(double val, int decimals) {
        double div = Math.pow(10, decimals);
        return Math.round(val * div) / div;
    }
}

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

    /*
     *  MainActivity <- ConsoleFragment
     */
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
                    Toast.makeText(m, "Device name was changed!", Toast.LENGTH_SHORT).show();
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
                txMyLat.setText(String.valueOf(m.getMyLat()));
                txMyLon.setText(String.valueOf(m.getMyLon()));
                txVerbose.setText(m.getVerbose());

                Toast.makeText(m, "All value was refreshed", Toast.LENGTH_SHORT).show();
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
}
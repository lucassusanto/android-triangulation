package com.felhr.serialportexamplesync;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class IdentityFragment extends Fragment {
    private EditText txMyName;
    private Button btnChange;
    private TextView txMyLat;
    private TextView txMyLon;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_identity, container, false);

        txMyName = view.findViewById(R.id.txMyName);
        btnChange = view.findViewById(R.id.btnChange);
        txMyLat = view.findViewById(R.id.txMyLat);
        txMyLon = view.findViewById(R.id.txMyLon);

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }
}

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

import java.lang.ref.WeakReference;
import java.util.List;

public class MapFragment extends Fragment {
    private WeakReference mActivity;
    private View mapView;

    private Button btnRefresh;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        btnRefresh = view.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                MapFragment.this.updateMap();
                Toast.makeText((MainActivity) mActivity.get(), "Map was updated!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = new WeakReference(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMap();
    }

    // Update Map Section

    public void updateMap() {
        // 1. Get all devices
        // 2. Draw position
    }

    private class UpdateTask extends AsyncTask<Integer, Integer, String> {
        // private List<Device> coordinates;
        private List<Device> devicesList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Integer... integers) {
            return null;
        }
    }
}

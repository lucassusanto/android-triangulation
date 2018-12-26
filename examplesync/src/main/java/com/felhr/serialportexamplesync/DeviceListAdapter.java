package com.felhr.serialportexamplesync;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter {
    private Context mContext;
    private List<Device> mDeviceList;

    public DeviceListAdapter(Context mContext, List<Device> mDeviceList) {
        this.mContext = mContext;
        this.mDeviceList = mDeviceList;
    }

    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(mContext, R.layout.item_device, null);
        Device dev = mDeviceList.get(position);

        TextView txDevName = view.findViewById(R.id.devName);
        TextView txDevLat = view.findViewById(R.id.devLat);
        TextView txDevLon = view.findViewById(R.id.devLon);

        txDevName.setText(dev.getName());
        txDevLat.setText(String.valueOf(dev.getLatitude()));
        txDevLon.setText(String.valueOf(dev.getLongitude()));

        view.setTag(dev.getName());

        return view;
    }
}

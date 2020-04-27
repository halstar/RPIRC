package com.e.rpirc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DevicesList extends AppCompatActivity {

    private Button                     listDevices, connectDevice;
    private RecyclerView               listedDevices;
    private RecyclerView.Adapter       adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<BluetoothDevice>      btDevices = (new ArrayList<BluetoothDevice>());
    private BluetoothAdapter           btAdapter;

    private static final int     BT_ENABLE_REQUEST = 10;
    private static final UUID    DEVICE_UUID       = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public  static final String  EXTRA_BT_DEVICE   = "com.e.rpirc.device";
    public  static final String  EXTRA_DEVICE_UUID = "com.e.rpirc.device_uuid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listDevices   = findViewById (R.id.listDevicesButton );
        connectDevice = findViewById(R.id.connectDeviceButton);
        listedDevices = findViewById(R.id.devicesList        );

        connectDevice.setEnabled     (false);
        listedDevices.setHasFixedSize(true );

        layoutManager = new LinearLayoutManager(this);
        listedDevices.setLayoutManager(layoutManager);

        adapter = new com.e.rpirc.DevicesListAdapter(btDevices);
        listedDevices.setAdapter(adapter);

        listDevices.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                btAdapter = BluetoothAdapter.getDefaultAdapter();

                if (btAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
                } else if (btAdapter.isEnabled() == false) {
                    Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBt, BT_ENABLE_REQUEST);
                } else {
                    new SearchDevices().execute();
                }
            }
        });

        connectDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                BluetoothDevice device = ((DevicesListAdapter)(listedDevices.getAdapter())).getSelectedItem();
                if (device != null) {
                    Intent intent = new Intent(getApplicationContext(), ControlRobot.class);
                    intent.putExtra(EXTRA_BT_DEVICE  , device);
                    intent.putExtra(EXTRA_DEVICE_UUID, DEVICE_UUID.toString());
                    startActivity(intent);
                }
            }
        });
    }

    private void msg(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>> {

        @Override
        protected List<BluetoothDevice> doInBackground(Void... params) {
            Set<BluetoothDevice>  pairedDevices = btAdapter.getBondedDevices();
            List<BluetoothDevice> listOfDevices = new ArrayList<BluetoothDevice>();
            for (BluetoothDevice device : pairedDevices) {
                listOfDevices.add(device);
            }
            return listOfDevices;
        }

        @Override
        protected void onPostExecute(List<BluetoothDevice> listOfDevices) {
            super.onPostExecute(listOfDevices);
            if (listOfDevices.size() > 0) {
                DevicesListAdapter adapter = (DevicesListAdapter)listedDevices.getAdapter();
                adapter.replaceItems(listOfDevices);
                connectDevice.setEnabled(true);
            } else {
                msg("No paired devices found. Please pair your BT device from Android and try again");
            }
        }
    }
}



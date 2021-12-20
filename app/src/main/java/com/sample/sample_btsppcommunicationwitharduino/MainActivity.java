package com.sample.sample_btsppcommunicationwitharduino;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String CONNECT_DEVICE_NONE = "NONE";
    private static final String CONNECT_STATUS_CONNECT = "接続";
    private static final String CONNECT_DISCONNECT_CONNECT = "未接続";

    private BluetoothAdapter mBluetoothAdapter;
    private ActivityResultLauncher<Intent> mActivityResultLauncher;

    private Button mAddDeviceButton;
    private Spinner mConnectDeviceSpinner;
    private ArrayAdapter<String> mConnectDeviceAdapter;

    private TextView mConnectStatusTextView;
    private Button mConnectButton;
    BluetoothSocket mBluetoothSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkDeviceSupportBluetooth();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        int resultCode = result.getResultCode();

                        switch (resultCode) {
                            case RESULT_OK:
                                break;
                            case RESULT_CANCELED:
                                showBluetoothDisabledAlertDialogAndFinishApp();
                                break;
                            case RESULT_FIRST_USER:
                            default:
                                break;
                        }
                    }
                });

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivityResultLauncher.launch(enableBtIntent);
        }

        mAddDeviceButton = findViewById(R.id.add_device_button);
        mAddDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });

        mConnectDeviceSpinner = findViewById(R.id.connect_device_spinner);

        mConnectStatusTextView = findViewById(R.id.connect_status_text_view);

        mConnectButton = findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedItemPosition = mConnectDeviceSpinner.getSelectedItemPosition();
                String deviceName = mConnectDeviceAdapter.getItem(selectedItemPosition);

                if(deviceName.equals(CONNECT_DEVICE_NONE)) {
                    Log.e(TAG,"device is NONE");
                } else {
                    connectDevice(deviceName);
                }
            }
        });
    }

    private void connectDevice(String connectDeviceName) {
        BluetoothDevice connectBluetoothDevice = null;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice bluetoothDevice : pairedDevices) {
                if (connectDeviceName.equals(bluetoothDevice.getName())) {
                    connectBluetoothDevice = bluetoothDevice;
                    break;
                }
            }
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try {
            mBluetoothSocket = connectBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.connect();
                mConnectStatusTextView.setText(CONNECT_STATUS_CONNECT);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mBluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                mBluetoothSocket = null;
                Log.e(TAG, "Bluetooth connect failed.");
            }
        }
    }

    private void checkDeviceSupportBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth.");
            finish();
        }
    }

    private void showBluetoothDisabledAlertDialogAndFinishApp() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("BluetoothがOFFになっています。")
                .setMessage("BluetoothをONにしてアプリを再起動してください。")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initConnectDeviceSpinner();
    }

    private void initConnectDeviceSpinner() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices != null) {
            mConnectDeviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);

            if(pairedDevices.isEmpty()) {
                mConnectDeviceAdapter.add(CONNECT_DEVICE_NONE);
            } else {
                for (BluetoothDevice bluetoothDevice : pairedDevices) {
                    String deviceName = bluetoothDevice.getName();
                    mConnectDeviceAdapter.add(deviceName);
                }
            }
        }
        mConnectDeviceSpinner.setAdapter(mConnectDeviceAdapter);
    }
}
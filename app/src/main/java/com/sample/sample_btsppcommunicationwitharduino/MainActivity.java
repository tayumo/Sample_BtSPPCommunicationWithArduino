package com.sample.sample_btsppcommunicationwitharduino;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private ActivityResultLauncher<Intent> mActivityResultLauncher;

    private Button mAddDeviceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddDeviceButton = findViewById(R.id.add_device_button);
        mAddDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });

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
}
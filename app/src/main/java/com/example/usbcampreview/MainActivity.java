package com.example.usbcampreview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "USB_CAMERA_APP_LOGS2";

    private Button btnHello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnHello = findViewById(R.id.btnHello);

        btnHello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Button clicked yo!!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "THIS IS A NEW LOGGGGGGGGGGGG");
//                Log.d(TAG, "---------------------------------------------------------------------------------------------");
//                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
//                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
////        UsbDevice device = deviceList.get("deviceName");
//                for (Map.Entry<String, UsbDevice> entry : deviceList.entrySet()) {
//                    String key = entry.getKey();
//                    UsbDevice value = entry.getValue();
//
//                    Log.d(TAG, key + ":" + value);
//                    Log.d(TAG, "---------------------------------------------------------------------------------------------");
//                }
            }
        });
//        Intent intent = new Intent();
//        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//        String deviceName = device.getDeviceName();
//        Log.d(TAG, "DEVICE DETECTED : " + deviceName);


    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d(TAG, "---------------------------------------------------------------------------------------------");
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//        for (Map.Entry<String, UsbDevice> entry : deviceList.entrySet()) {
//            String key = entry.getKey();
//            UsbDevice value = entry.getValue();
//
//            Log.d(TAG, key + ":" + value);
//            Log.d(TAG, "---------------------------------------------------------------------------------------------");
//        }
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
            String deviceName = device.getDeviceName();
            int deviceId = device.getDeviceId();
            Log.d(TAG, "---------------------------------------------------------------------------------------------");
            Log.d(TAG, "DEVICE DETECTED : " + deviceName);
            Log.d(TAG, "DEVICE ID : " + Integer.toString(deviceId));
            Log.d(TAG, "---------------------------------------------------------------------------------------------");
        }
    }
}
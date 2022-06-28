package com.example.usbcampreview;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/*
* Steps followed : When the 'GET' button is clicked the following things happen(reference : https://developer.android.com/guide/topics/connectivity/usb/host)
* 1) Enumerate USB devices : Found the camera. Details like name etc were logged successfully
* 2) Request user to communicate with camera : For now, explicitly enable camera permissions in the phone. Will later deal with doing this in code. For now, explicitly give camera permission on phone
* 3) Communicate with the external camera(Here, error is showing up. See lines 128 and 129)
* */


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "USB_CAMERA_APP_LOGS";
    private static final String ACTION_USB_PERMISSION = "com.example.usbcampreview.USB_PERMISSION";

    private Button btnGet;

    private UsbDevice device;
    private UsbManager manager;
    private UsbDeviceConnection connection;
    private UsbInterface intf;
    private UsbEndpoint endpoint;

    //TODO : Change length of bytes array later
    byte[] readBytes = new byte[64];

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    //TODO : Make user accept camera permission. For now I did it deliberately in phone's app permission settings
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.d(TAG, "permission accepted for device");
                            //Once user's permission is obtained, communicate with the camera
                            communicateWithCamera();
                        }
                    } else {
                        Log.d(TAG, "permission denied for device");
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        btnGet = findViewById(R.id.btnGet);

        handlePermissions(new String[]{
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        });

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the USB camera
                device = enumerateUSBDevices(manager);

                //Log info about the device : device name, product name, class, subclass, protocol
                logDeviceInfo(device);

                //Get user's permission to communicate with the USB device
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                registerReceiver(mUsbReceiver, filter);
                manager.requestPermission(device, mPermissionIntent);
            }
        });
    }


    //TODO : Enumerates all connected USB devices but should only return the external camera
    private UsbDevice enumerateUSBDevices(UsbManager manager) {
        //Gets a list of all USB devices attached
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        UsbDevice device = null;
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
        }
        return device;
    }

    @SuppressLint("NewApi")
    private void logDeviceInfo(UsbDevice device) {
        if(device != null){
            //Logs device name, product name, class, subclass, protocol of the USB device
            Log.d(TAG, device.getDeviceName());
            Log.d(TAG, device.getProductName());
            Log.d(TAG, String.valueOf(device.getDeviceClass()));
            Log.d(TAG, String.valueOf(device.getDeviceSubclass()));
            Log.d(TAG, String.valueOf(device.getDeviceProtocol()));
        }else{
            //Logs device name, product name, class, subclass, protocol of the USB device
            Log.d(TAG, "No USB device connected");
        }
    }


    //TODO : Deal with the following error
    private void communicateWithCamera(){
        intf = device.getInterface(0);
        endpoint = intf.getEndpoint(0);
        connection = manager.openDevice(device); // GIVES THIS ERROR : "E/usbhost: usb_device_new read returned 2521 errno 0" Not sure what it means
        //Above error has some code on google but didn't understand : https://android.googlesource.com/platform/system/core/+/master/libusbhost/usbhost.c
        connection.claimInterface(intf, true);
    }

    private void handlePermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> notGrantedPerms = new ArrayList<>();
            for (String p : permissions) {
                if (this.checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED)
                    notGrantedPerms.add(p);
            }
            permissions = notGrantedPerms.toArray(new String[0]);
            if (permissions != null && permissions.length > 0)
                this.requestPermissions(permissions, 701);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 701) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (String p : permissions) {
                    String msg = "";
                    if (this.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED) {
                        msg = "Permission Granted for " + p;
                        btnGet.setEnabled(true);
                    }
                    else{
                        msg = "Permission not Granted for " + p;
                        btnGet.setEnabled(false);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}



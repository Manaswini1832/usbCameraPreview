package com.example.usbcampreview;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "USB_CAMERA_APP_LOGS";
    private static final String ACTION_USB_PERMISSION = "com.example.usbcampreview.USB_PERMISSION";

    private Button btnGet;

    private UsbDevice device;
    private UsbManager manager;

    //TODO : Change length of bytes array later
    private byte[] bytes = new byte[51];
    private static int TIMEOUT = 1000; // Collects info from external camera for 1second
    private boolean forceClaim = true;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //To get permission to communicate with the USB device
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    //TODO : Make user accept camera permission. For now I did it deliberately in phone's app permission settings
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            Log.d(TAG, "permission accepted for device");
                            //Once user's permission is obtained, communicate with the camera
                            communicateWithCamera();
                        }
                    }
                    else {
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
    private UsbDevice enumerateUSBDevices(UsbManager manager){
        //Gets a list of all USB devices attached
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        UsbDevice device = null;
        while(deviceIterator.hasNext()){
            device = deviceIterator.next();
        }
        return device;
    }

    @SuppressLint("NewApi")
    private void logDeviceInfo(UsbDevice device){
        //Logs device name, product name, class, subclass, protocol of the USB device
        Log.d(TAG, device.getDeviceName());
        Log.d(TAG, device.getProductName());
        Log.d(TAG, String.valueOf(device.getDeviceClass()));
        Log.d(TAG, String.valueOf(device.getDeviceSubclass()));
        Log.d(TAG, String.valueOf(device.getDeviceProtocol()));
    }

    //Gets info from the camera
    private void communicateWithCamera(){
        Thread camThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Got all the following code from USB host api docs : https://developer.android.com/guide/topics/connectivity/usb/host#communicating-d
                UsbInterface intf = device.getInterface(0);
                UsbEndpoint endpoint = intf.getEndpoint(0);
                UsbDeviceConnection connection = manager.openDevice(device);
                connection.claimInterface(intf, forceClaim);
                connection.bulkTransfer(endpoint,
                        bytes,
                        bytes.length,
                        TIMEOUT);
            }
        });
        camThread.start();

    }
}




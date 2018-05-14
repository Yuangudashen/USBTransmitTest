package com.yuangudashen.usbtransmit;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;

public class USBTransmit {
    private static final String ACTION_USB_PERMISSION = "com.yuangudashen.USB_PERMISSION";
    public boolean usbAttached = false;
    USBTransmit.DeviceConnectStateChanged deviceConnectStateChanged = null;
    public USBDevice usbDevice;
    private int USB_VID = 1155;
    private int USB_PID = 22288;
    public static int EP0 = 0;
    public static int EP1_IN = 129;
    public static int EP1_OUT = 1;

    private void Init(Context context) {
        UsbManager usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filter.setPriority(2147483647);
        USBTransmit.UsbDeviceDetachedBroadcastReceiver usbStateReceiver = new USBTransmit.UsbDeviceDetachedBroadcastReceiver();
        context.registerReceiver(usbStateReceiver, filter);
        this.usbDevice = new USBDevice(usbManager, pendingIntent);
    }

    public USBTransmit(Context context) {
        this.Init(context);
    }

    public USBTransmit(Context context, int usb_pid, int usb_vid) {
        this.USB_PID = usb_pid;
        this.USB_VID = usb_vid;
        this.Init(context);
    }

    public USBTransmit(Context context, USBTransmit.DeviceConnectStateChanged deviceConnectStateChanged) {
        this.deviceConnectStateChanged = deviceConnectStateChanged;
        this.Init(context);
    }

    public USBTransmit(Context context, USBTransmit.DeviceConnectStateChanged deviceConnectStateChanged, int usb_pid, int usb_vid) {
        this.deviceConnectStateChanged = deviceConnectStateChanged;
        this.USB_PID = usb_pid;
        this.USB_VID = usb_vid;
        this.Init(context);
    }

    class UsbDeviceDetachedBroadcastReceiver extends BroadcastReceiver {
        UsbDeviceDetachedBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
                USBTransmit.this.usbAttached = false;
                if(USBTransmit.this.deviceConnectStateChanged != null) {
                    USBTransmit.this.deviceConnectStateChanged.stateChanged(USBTransmit.this.usbAttached);
                }
            } else if(intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                USBTransmit.this.usbDevice.USB_ScanDevice(USBTransmit.this.USB_PID, USBTransmit.this.USB_VID);
                USBTransmit.this.usbAttached = true;
                if(USBTransmit.this.deviceConnectStateChanged != null) {
                    USBTransmit.this.deviceConnectStateChanged.stateChanged(USBTransmit.this.usbAttached);
                }
            }

        }
    }

    public interface DeviceConnectStateChanged {
        void stateChanged(boolean var1);
    }
}
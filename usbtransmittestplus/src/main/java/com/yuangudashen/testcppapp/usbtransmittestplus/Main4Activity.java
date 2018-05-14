package com.yuangudashen.testcppapp.usbtransmittestplus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.usbxyz.usb2xxx.Transmit;
import com.usbxyz.usbtransmit.USBTransmit;

public class Main4Activity extends AppCompatActivity {

    private static final String TAG = "Main4Activity";
    byte count = 0x00;
    private USBTransmit mUsbTransmit;
    private Button get_card_id_btn;
    private TextView card_id_tv;
    private Button connect_device_btn;

    int VendorId = 1155;
    int ProductId = 22288;// 31000

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);


        mUsbTransmit = new USBTransmit(Main4Activity.this, deviceConnectStateChanged,ProductId,VendorId);

        // connectUsb();

        findViews();

        setOnClick();



    }

    private void connectUsb() {
        int devIndex = 0;

        //扫描设备连接数
        int devNum = mUsbTransmit.usbDevice.USBScanDevice();

        if (devNum <= 0) {
            return;
        } else {
            Log.e(TAG,"设备连接数为：" + String.format("%d", devNum));
        }

        //打开设备
        if (!mUsbTransmit.usbDevice.USBOpenDevice(devIndex)) {
            return;
        } else {
            Log.e(TAG," 设备打开成功");
        }
    }

    private void setOnClick() {

        connect_device_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectUsb();
            }
        });


        get_card_id_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] data = new byte[]{0x20, 0x17, 0x10, count++};

                Transmit.USB_MSG usb_msg = new Transmit.USB_MSG();

                usb_msg.Message = 1;
                usb_msg.ParamSize = 5;
                usb_msg.pParam = data;

                Transmit.MSG_Send(mUsbTransmit.EP1_OUT, usb_msg, 100);



                Transmit.MSG_Get(mUsbTransmit.EP1_IN,usb_msg,100);

                byte[] dataGet = usb_msg.pParam;
                Log.e(TAG,""+String.format("%02x,%02x,%02x,%02x",dataGet[0],dataGet[1],dataGet[2],dataGet[3]));

                card_id_tv.setText("卡号："+String.format("%02x,%02x,%02x,%02x",dataGet[0],dataGet[1],dataGet[2],dataGet[3]));
            }
        });

    }

    private void findViews() {
        connect_device_btn = (Button) findViewById(R.id.connect_device_btn);
        get_card_id_btn = (Button) findViewById(R.id.get_card_id_btn);
        card_id_tv = (TextView) findViewById(R.id.card_id_tv);
    }

    USBTransmit.DeviceConnectStateChanged deviceConnectStateChanged = new USBTransmit.DeviceConnectStateChanged() {
        @Override
        public void stateChanged(boolean connected) {
            if (connected) {
                Log.e(TAG, "设备已连接");
            } else {
                Log.e(TAG, "设备已断开连接");
            }
        }
    };
}

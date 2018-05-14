package com.yuangudashen.testcppapp.usbtransmittestplus;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.usbxyz.usbtransmit.USBTransmit;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends AppCompatActivity {

    public static final String TAG = "Main2Activity";

    USBTransmit mUSBTransmit;
    TextView textView;
    private ProgressBar progressbar;

    private Button link_and_open_device_btn;


    private int devIndex;
    private int DataNum;
    private int PacketSize;
    private int DataNumIndex;
    private int ret;
    private byte[] WriteDataBuffer;
    private boolean State;


    private long startTime;
    private long consumingTime;
    private ScrollView id_data_scroll;

    public class ConnectStateChanged implements USBTransmit.DeviceConnectStateChanged {
        @Override
        public void stateChanged(boolean connected) {
            if (connected) {
                Toast.makeText(Main2Activity.this, "设备已连接", Toast.LENGTH_SHORT).show();
                link_and_open_device_btn.setEnabled(true);
            } else {
                Toast.makeText(Main2Activity.this, "设备已断开连接", Toast.LENGTH_SHORT).show();
                textView.append("");
                link_and_open_device_btn.setEnabled(false);
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            Bundle bundle = null;
            synchronized (this) {

                switch (what) {
                    case 0:
                        bundle = msg.getData();
                        double writeByteCount = bundle.getDouble("writeByteCount");
                        double writeDuration = bundle.getDouble("writeDuration");
                        double writeSpeed = bundle.getDouble("writeSpeed");
                        textView.setText("");
                        textView.append(String.format("发送数据字节数: %.5f MBytes\n", writeByteCount));
                        textView.append(String.format("发送数据消耗时间: %.6f s\n", writeDuration));
                        textView.append(String.format("发送数据速度: %.5f MByte/s\n", writeSpeed));
                        textView.invalidate();
                        scrollToBottom(id_data_scroll);
                        bundle = null;
                        System.gc();
                        break;

                    case 1:
                        bundle = msg.getData();
                        byte[] readResult = bundle.getByteArray("readResult");
                        double readByteCount = bundle.getDouble("readByteCount");
                        double readDuration = bundle.getDouble("readDuration");
                        double readSpeed = bundle.getDouble("readSpeed");
                        textView.setText("");
                        textView.append(String.format("%x,%x,%x,%x\n", readResult[0], readResult[1], readResult[2], readResult[3]));
                        textView.append(String.format("读取数据字节数: %.5f MBytes\n", readByteCount));
                        textView.append(String.format("读取数据消耗时间: %.6f s\n", readDuration));
                        textView.append(String.format("读取数据速度: %.5f MByte/s\n", readSpeed));
                        textView.invalidate();
                        scrollToBottom(id_data_scroll);
                        bundle = null;
                        System.gc();
                        break;
                }
            }
        }
    };


    Timer timer = new Timer("timerTask");

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            // writeDataToUsb();
//            USBApplication.getInstance().executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    // handler.sendMessage(msg);
//                }
//            });

            readDataFormUsb();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //以下两种方式均可，第二种方式可以实时监测设备连接状态
        // mUSBTransmit = new USBTransmit(this);//不监视设备插拔时间
        mUSBTransmit = new USBTransmit(this, new ConnectStateChanged());//需要监视设备插拔事件
//        mUSBTransmit = new USBTransmit(this, new ConnectStateChanged(), 5002, 471);
//
//        mUSBTransmit.usbDevice.USBScanDevice(5002,471);


        findViews();

        initData();

        setOnClickListener();


    }

    private byte cnt = 0;

    private boolean initSendData() {
        //准备发送数据
        DataNumIndex = DataNum;
        //告诉设备即将要读取的数据包数
        WriteDataBuffer[0] = (byte) (DataNum >> 24);
        WriteDataBuffer[1] = (byte) (DataNum >> 16);
        WriteDataBuffer[2] = (byte) (DataNum >> 8);
        WriteDataBuffer[3] = (byte) (DataNum >> 0);
        //高速设备每包数据的长度
        WriteDataBuffer[4] = (byte) (PacketSize >> 24);
        WriteDataBuffer[5] = (byte) (PacketSize >> 16);
        WriteDataBuffer[6] = (byte) (PacketSize >> 8);
        WriteDataBuffer[7] = (byte) (PacketSize >> 0);

        WriteDataBuffer[8] = 0x20;
        WriteDataBuffer[9] = 0x17;
        WriteDataBuffer[10] = 0x10;
        WriteDataBuffer[11] = cnt++;

        State = mUSBTransmit.usbDevice.USBBulkWriteData(devIndex, mUSBTransmit.EP1_OUT, WriteDataBuffer, 12, 100);

        return State;
    }

    private void initData() {
        devIndex = 0;
        //单位为包 5000
        DataNum = 1;//500

        //每次传输的数据字节数，该参数必须和单片机中的参数完全匹配，该参数不能大于或等于64K
        PacketSize = 256;// 16
        DataNumIndex = DataNum;
        State = false;
        WriteDataBuffer = new byte[PacketSize - 1];
    }

    private void setOnClickListener() {
        link_and_open_device_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                link_and_open_device_btn.setEnabled(false);
                progressbar.setVisibility(View.VISIBLE);

                textView.setText("正在连接设备...\n");
                //扫描设备连接数
                int devNum = mUSBTransmit.usbDevice.USBScanDevice();
                debug(TAG + "设备连接数：" + devNum, true);
                if (devNum <= 0) {
                    textView.append("无设备连接!\n");
                    link_and_open_device_btn.setEnabled(true);
                    return;
                } else {
                    textView.append("设备连接数为：" + String.format("%d", devNum) + "\n");
                }
                textView.invalidate();
                //打开设备
                if (!mUSBTransmit.usbDevice.USBOpenDevice(devIndex)) {
                    textView.append("打开设备失败!\n");
                    link_and_open_device_btn.setEnabled(true);
                    return;
                } else {
                    textView.append("打开设备成功!\n");

                    writeAndReadData();
                }
                progressbar.setVisibility(View.GONE);
                textView.invalidate();
            }
        });
    }

    private void writeAndReadData() {
        while (true){
            readDataFormUsb_1();
        }
    }

    public void writeDataToUsb() {

        Bundle bundle = null;

        //循环发送数据
        DataNumIndex = DataNum;

        for (int i = 0; i < WriteDataBuffer.length; i++) {
            WriteDataBuffer[i] = (byte) i;
        }

        startTime = System.nanoTime();  //開始時間
        do {
            boolean state = mUSBTransmit.usbDevice.USBBulkWriteData(devIndex, mUSBTransmit.EP1_OUT, WriteDataBuffer, WriteDataBuffer.length, 100);
            if (!state) {
                break;
            } else {
                DataNumIndex--;
            }
        } while (DataNumIndex > 0);
        consumingTime = (System.nanoTime() - startTime) / (1000 * 1000); //消耗時間
        debug("consumingTime = " + consumingTime + "ms", false);

        Message message = Message.obtain();
        bundle = new Bundle();
        double writeByteCount = (DataNum - DataNumIndex) * WriteDataBuffer.length / (1024 * 1024.0);
        double writeDuration = consumingTime / 1000.0;
        double writeSpeed = (DataNum - DataNumIndex) * WriteDataBuffer.length / (consumingTime / 1000.0) / (1024 * 1024.0);

        bundle.putDouble("writeByteCount", writeByteCount);
        bundle.putDouble("writeDuration", writeDuration);
        bundle.putDouble("writeSpeed", writeSpeed);
        message.setData(bundle);
        message.what = 0;
        handler.sendMessage(message);
    }

    public void readDataFormUsb() {
        Bundle bundle = null;
        boolean hasinitSendData = initSendData();
        //循环读取数据
        byte[] ReadDataBuffer = new byte[PacketSize];

        //開始時間
        startTime = System.nanoTime();
        do {
            ret = mUSBTransmit.usbDevice.USBBulkReadData(devIndex, mUSBTransmit.EP1_IN, ReadDataBuffer, PacketSize, 100);
            if (ret != PacketSize) {
                break;
            } else {
                DataNumIndex--;
            }
        } while (DataNumIndex > 0);
        //消耗時間
        consumingTime = (System.nanoTime() - startTime) / (1000 * 1000);
        debug("consumingTime = " + consumingTime + "ms", false);

        Message message = Message.obtain();
        bundle = new Bundle();

        byte[] readResult = new byte[4];
        for (int i = 0; i < readResult.length; i++) {
            readResult[i] = ReadDataBuffer[i];
        }

        double readByteCount = (DataNum - DataNumIndex) * PacketSize / (1024 * 1024.0);
        double readDuration = consumingTime / 1000.0;
        double readSpeed = (DataNum - DataNumIndex) * PacketSize / (consumingTime / 1000.0) / (1024 * 1024.0);

        bundle.putByteArray("readResult", readResult);
        bundle.putDouble("readByteCount", readByteCount);
        bundle.putDouble("readDuration", readDuration);
        bundle.putDouble("readSpeed", readSpeed);
        message.setData(bundle);
        message.what = 1;
        handler.sendMessage(message);
    }

    public void writeDataToUsb_1() {
        progressbar.setVisibility(View.VISIBLE);
        progressbar.invalidate();

        textView.append("正在测试写数据速度，请稍候...\n");
        textView.invalidate();
        //循环发送数据
        DataNumIndex = DataNum;

        for (int i = 0; i < WriteDataBuffer.length; i++) {
            WriteDataBuffer[i] = (byte) i;
        }

        startTime = System.nanoTime();  //開始時間
        do {
            boolean state = mUSBTransmit.usbDevice.USBBulkWriteData(devIndex, mUSBTransmit.EP1_OUT, WriteDataBuffer, WriteDataBuffer.length, 100);
            if (!state) {
                break;
            } else {
                DataNumIndex--;
            }
        } while (DataNumIndex > 0);
        consumingTime = (System.nanoTime() - startTime) / (1000 * 1000); //消耗時間
        debug("consumingTime = " + consumingTime + "ms", false);
        // Print the write data speed information
        textView.append("----------------------------------\n");
        // textView.append("DataNum= "+DataNum+" ,DataNumIndex= "+DataNumIndex);
        textView.append(String.format("发送数据字节数: %.3f MBytes\n", (DataNum - DataNumIndex) * WriteDataBuffer.length / (1024 * 1024.0)));
        textView.append(String.format("发送数据消耗时间: %f s\n", consumingTime / 1000.0));
        textView.append(String.format("发送数据速度: %.3f MByte/s\n", (DataNum - DataNumIndex) * WriteDataBuffer.length / (consumingTime / 1000.0) / (1024 * 1024.0)));
        textView.append("-----------------------------------\n");

        if (DataNumIndex > 0) {
            textView.append("发送数据失败！\n");
        } else {
            textView.append("发送数据成功！\n");
        }
        textView.invalidate();
        textView.append(Arrays.toString(WriteDataBuffer).substring(0, WriteDataBuffer.length) + "\n");
        textView.invalidate();

        scrollToBottom(id_data_scroll);
        progressbar.setVisibility(View.GONE);
        debug(TAG + "数据写测试成功!", true);
    }

    public void readDataFormUsb_1() {
        progressbar.setVisibility(View.VISIBLE);
        progressbar.invalidate();
        boolean hasinitSendData = initSendData();

        textView.append("正在测试读数据速度，请稍候...\n");
        textView.invalidate();
        //循环读取数据
        byte[] ReadDataBuffer = new byte[PacketSize];


        //開始時間
        startTime = System.nanoTime();
        do {
            ret = mUSBTransmit.usbDevice.USBBulkReadData(devIndex, mUSBTransmit.EP1_IN, ReadDataBuffer, PacketSize, 100);
            if (ret != PacketSize) {
                break;
            } else {
                DataNumIndex--;
            }
        } while (DataNumIndex > 0);
        //消耗時間
        consumingTime = (System.nanoTime() - startTime) / (1000 * 1000);
        debug("consumingTime = " + consumingTime + "ms",false);
        byte[] readResult = new byte[4];
        for (int i = 0; i < readResult.length; i++) {
            readResult[i] = ReadDataBuffer[i];
        }
        // Print the write data speed information
        textView.append("----------------------------------\n");
        textView.append(String.format("%x,%x,%x,%x\n", readResult[0], readResult[1], readResult[2], readResult[3]));
        textView.append(String.format("读数据字节数: %.3f MBytes\n", (DataNum - DataNumIndex) * PacketSize / (1024 * 1024.0)));
        textView.append(String.format("读数据消耗时间: %f s\n", consumingTime / 1000.0));
        textView.append(String.format("读数据速度: %.3f MByte/s\n", (DataNum - DataNumIndex) * PacketSize / (consumingTime / 1000.0) / (1024 * 1024.0)));
        textView.append("-----------------------------------\n");
        if (DataNumIndex > 0) {
            textView.append("读数据失败！\n");
        } else {
            textView.append("读数据成功！\n");
        }

        textView.append(Arrays.toString(ReadDataBuffer).substring(0, PacketSize) + "\n");

        textView.invalidate();

        scrollToBottom(id_data_scroll);

        progressbar.setVisibility(View.GONE);
        debug(TAG + "数据读测试成功!", true);

    }


    private void scrollToBottom(final ScrollView scrollView) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void findViews() {
        id_data_scroll = (ScrollView) findViewById(R.id.id_data_scroll);
        textView = (TextView) findViewById(R.id.textView);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        link_and_open_device_btn = (Button) findViewById(R.id.link_and_open_device_btn);
    }


    public void logE(String logDStr) {
        Log.e(TAG, logDStr);
    }

    public void toastShort(Context context, String toastStr) {
        Toast.makeText(context, toastStr, Toast.LENGTH_SHORT).show();
    }

    public void debug(String msg, boolean showToast) {
        logE(msg);
        if (showToast) {
            toastShort(getBaseContext(), msg);
        }
    }
}
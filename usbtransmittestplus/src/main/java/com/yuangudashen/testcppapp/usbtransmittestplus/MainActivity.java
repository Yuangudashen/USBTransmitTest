package com.yuangudashen.testcppapp.usbtransmittestplus;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "--------";

    USBTransmit mUSBTransmit;
    TextView textView;
    private ProgressBar progressbar;

    private Button link_and_open_device_btn;
    private Button read_data_btn;
    private Button write_data_btn;


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
                Toast.makeText(MainActivity.this, "设备已连接", Toast.LENGTH_SHORT).show();
                link_and_open_device_btn.setEnabled(true);
                read_data_btn.setEnabled(true);
                write_data_btn.setEnabled(true);
            } else {
                Toast.makeText(MainActivity.this, "设备已断开连接", Toast.LENGTH_SHORT).show();
                textView.append("");
                link_and_open_device_btn.setEnabled(false);
                read_data_btn.setEnabled(false);
                write_data_btn.setEnabled(false);
            }
        }
    }

    Handler handler = new Handler();




    Timer timer = new Timer("timerTask");

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            debug("--------",false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //以下两种方式均可，第二种方式可以实时监测设备连接状态
        // mUSBTransmit = new USBTransmit(this);//不监视设备插拔时间
        mUSBTransmit = new USBTransmit(this, new ConnectStateChanged());//需要监视设备插拔事件
//        mUSBTransmit = new USBTransmit(this, new ConnectStateChanged(), 5002, 471);
//
//        mUSBTransmit.usbDevice.USBScanDevice(5002,471);


        findViews();

        initData();

        setOnClickListener();



        timer.schedule(timerTask,0,10);



    }

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

//        //告诉设备即将要读取的数据包数
//        WriteDataBuffer[0] = (byte) (2);
//        WriteDataBuffer[1] = (byte) (1);
//        WriteDataBuffer[2] = (byte) (4);
//        WriteDataBuffer[3] = (byte) (0);
//        //高速设备每包数据的长度
//        WriteDataBuffer[4] = (byte) (3);
//        WriteDataBuffer[5] = (byte) (1);
//        WriteDataBuffer[6] = (byte) (3);
//        WriteDataBuffer[7] = (byte) (4);

        State = mUSBTransmit.usbDevice.USBBulkWriteData(devIndex, mUSBTransmit.EP1_OUT, WriteDataBuffer, 8, 100);
        if (State) {
            textView.append("告诉设备即将要读取的数据包数及高速设备每包数据的长度-成功！\n");
            read_data_btn.setEnabled(true);
        } else {
            textView.append("告诉设备即将要读取的数据包数及高速设备每包数据的长度-失败！\n");
            read_data_btn.setEnabled(false);
        }
        textView.invalidate();
        return State;
    }

    private void initData() {
        devIndex = 0;
        //单位为包 5000
        DataNum = 2;//500

        //每次传输的数据字节数，该参数必须和单片机中的参数完全匹配，该参数不能大于或等于64K
        PacketSize = 1 * 1024;// 16
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
                }
                progressbar.setVisibility(View.GONE);
                textView.invalidate();
            }
        });

        // 测试读数据
        read_data_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressbar.setVisibility(View.VISIBLE);
                progressbar.invalidate();
                boolean hasinitSendData = initSendData();

                read_data_btn.setEnabled(false);

                textView.append("正在测试读数据速度，请稍候...\n");
                textView.invalidate();
                //循环读取数据
                byte[] ReadDataBuffer = new byte[PacketSize];


                //開始時間
                startTime = System.nanoTime();
                do {
                    ret = mUSBTransmit.usbDevice.USBBulkReadData(devIndex, mUSBTransmit.EP1_IN, ReadDataBuffer, PacketSize, 1000);
                    if (ret != PacketSize) {
                        break;
                    } else {
                        DataNumIndex--;
                    }
                } while (DataNumIndex > 0);
                //消耗時間
                consumingTime = (System.nanoTime() - startTime) / (1000 * 1000);
                System.out.println("consumingTime = " + consumingTime + "ms");
                // Print the write data speed information
                textView.append("----------------------------------\n");
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
                read_data_btn.setEnabled(true);

            }
        });

        // 测试写数据
        write_data_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressbar.setVisibility(View.VISIBLE);
                progressbar.invalidate();

                write_data_btn.setEnabled(false);

                textView.append("正在测试写数据速度，请稍候...\n");
                textView.invalidate();
                //循环发送数据
                DataNumIndex = DataNum;

                for (int i = 0; i < WriteDataBuffer.length; i++) {
                    WriteDataBuffer[i] = (byte) i;
                }

                startTime = System.nanoTime();  //開始時間
                do {
                    boolean state = mUSBTransmit.usbDevice.USBBulkWriteData(devIndex, mUSBTransmit.EP1_OUT, WriteDataBuffer, WriteDataBuffer.length, 1000);
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
                write_data_btn.setEnabled(true);
            }
        });
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
        read_data_btn = (Button) findViewById(R.id.read_data_btn);
        write_data_btn = (Button) findViewById(R.id.write_data_btn);
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
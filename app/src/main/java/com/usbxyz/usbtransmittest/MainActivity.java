package com.usbxyz.usbtransmittest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.usbxyz.usbtransmit.USBTransmit;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "--------";

    USBTransmit mUSBTransmit;
    TextView textView;
    private ProgressBar progressbar;

    public class ConnectStateChanged implements USBTransmit.DeviceConnectStateChanged {
        @Override
        public void stateChanged(boolean connected) {
            if (connected) {
                Toast.makeText(MainActivity.this, "设备已连接", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "设备断开连接", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void logD(String logDStr) {
        Log.d(TAG, logDStr);
    }

    void toastShort(Context context, String toastStr) {
        Toast.makeText(context, toastStr, Toast.LENGTH_SHORT).show();
    }

    void debug(String msg, boolean showToast) {
        logD(msg);
        if (showToast) {
            toastShort(getBaseContext(), msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        final Button startButton = (Button) findViewById(R.id.button);
        //以下两种方式均可，第二种方式可以实时监测设备连接状态
        //mUSBTransmit = new USBTransmit(this);//不监视设备插拔时间
        mUSBTransmit = new USBTransmit(this, new ConnectStateChanged());//需要监视设备插拔事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setEnabled(false);
                progressbar.setVisibility(View.VISIBLE);
                textView.setText("正在连接设备...\n");
                int devIndex = 0;
                int DataNum = 500;//单位为包 5000
                int PacketSize = 16 * 1024;//每次传输的数据字节数，该参数必须和单片机中的参数完全匹配，该参数不能大于或等于64K
                int DataNumIndex = DataNum;
                int ret;
                boolean State;
                byte[] WriteDataBuffer = new byte[PacketSize - 1];
                //扫描设备连接数
                int devNum = mUSBTransmit.usbDevice.USBScanDevice();
                debug(TAG+"设备连接数："+ devNum, true);
                if (devNum <= 0) {
                    textView.append("无设备连接!\n");
                    return;
                } else {
                    textView.append("设备连接数为：" + String.format("%d", devNum) + "\n");
                }
                //打开设备
                if (!mUSBTransmit.usbDevice.USBOpenDevice(devIndex)) {
                    textView.append("打开设备失败!\n");
                    return;
                } else {
                    textView.append("打开设备成功!\n");
                }

                while (true){
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

                    State = mUSBTransmit.usbDevice.USBBulkWriteData(devIndex, mUSBTransmit.EP1_OUT, WriteDataBuffer, 8, 100);
                    if (State) {
                        textView.append("写数据成功！\n");
                    } else {
                        textView.append("写数据失败！\n");
                        return;
                    }

                    progressbar.setVisibility(View.VISIBLE);

                    textView.append("正在测试读数据速度，请稍候...\n");
                    textView.invalidate();
                    //循环读取数据
                    byte[] ReadDataBuffer = new byte[PacketSize];
                    long startTime = System.nanoTime();  //開始時間
                    do {
                        ret = mUSBTransmit.usbDevice.USBBulkReadData(devIndex, mUSBTransmit.EP1_IN, ReadDataBuffer, PacketSize, 1000);
                        if (ret != PacketSize) {
                            break;
                        } else {
                            DataNumIndex--;
                        }
                    } while (DataNumIndex > 0);
                    long consumingTime = (System.nanoTime() - startTime) / (1000 * 1000); //消耗時間
                    System.out.println("consumingTime = " + consumingTime + "ms");
                    // Print the write data speed information
                    textView.append("----------------------------------\n");
                    textView.append(String.format("读数据字节数: %d MBytes\n", (DataNum - DataNumIndex) * PacketSize / (1024 * 1024)));
                    textView.append(String.format("读数据消耗时间: %f s\n", consumingTime / 1000.0));
                    textView.append(String.format("读数据速度: %.3f MByte/s\n", (DataNum - DataNumIndex) * PacketSize / (consumingTime / 1000.0) / (1024 * 1024.0)));
                    textView.append("-----------------------------------\n");
                    if (DataNumIndex > 0) {
                        textView.append("读数据失败！\n");
                    } else {
                        textView.append("读数据成功！\n");
                    }

                    progressbar.setVisibility(View.VISIBLE);
                    textView.append("正在测试写数据速度，请稍候...\n");
                    textView.invalidate();
                    //循环发送数据
                    DataNumIndex = DataNum;
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
                    textView.append(String.format("发送数据字节数: %d MBytes\n", (DataNum - DataNumIndex) * WriteDataBuffer.length / (1024 * 1024)));
                    textView.append(String.format("发送数据消耗时间: %f s\n", consumingTime / 1000.0));
                    textView.append(String.format("发送数据速度: %.3f MByte/s\n", (DataNum - DataNumIndex) * WriteDataBuffer.length / (consumingTime / 1000.0) / (1024 * 1024.0)));
                    textView.append("-----------------------------------\n");
                    if (DataNumIndex > 0) {
                        textView.append("发送数据失败！\n");
                    } else {
                        textView.append("发送数据成功！\n");
                    }
                    textView.invalidate();

                    debug(TAG+"数据读写测试成功!",true);
                    progressbar.setVisibility(View.GONE);
                    startButton.setEnabled(true);
                }



            }
        });


    }
}

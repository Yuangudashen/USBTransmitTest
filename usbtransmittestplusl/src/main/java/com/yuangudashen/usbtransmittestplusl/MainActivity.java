package com.yuangudashen.usbtransmittestplusl;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yuangudashen.usbtransmit.USBDevice;
import com.yuangudashen.usbtransmit.USBTransmit;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    USBTransmit mUSBTransmit;
    TextView textView;
    private ProgressBar progressbar;
    private Button link_and_open_device_btn;
    private ScrollView id_data_scroll;
    private AppCompatButton read_data_btn;

    private AppCompatButton stop_read_data_btn;

    private Context mContext = USBApplication.applicationContext;


    private int devIndex;
    private Button show_key_4_tv;
    private Button show_key_1_tv;
    private Button show_key_2_tv;
    private Button show_key_3_tv;
    private UsbMsgThread usbMsgThread;

    private HandlerThread handlerThread;


    public class ConnectStateChanged implements USBTransmit.DeviceConnectStateChanged {
        @Override
        public void stateChanged(boolean connected) {
            if (connected) {
                Toast.makeText(mContext, "设备已连接", Toast.LENGTH_SHORT).show();
                link_and_open_device_btn.setEnabled(true);
            } else {
                Toast.makeText(mContext, "设备已断开连接", Toast.LENGTH_SHORT).show();
                textView.append("");
                link_and_open_device_btn.setEnabled(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUSBTransmit = new USBTransmit(this, new ConnectStateChanged());//需要监视设备插拔事件
        handlerThread = new HandlerThread("UsbAppHandlerThread");
        handlerThread.start();
        usbHandler = new Handler();
        findViews();

        initData();

        setOnClickListener();

    }

    private void initData() {
        devIndex = 0;
    }

    private void setOnClickListener() {
        link_and_open_device_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                link_and_open_device_btn.setEnabled(false);
                progressbar.setVisibility(View.VISIBLE);

                textView.setText("正在连接设备...\n");
                //扫描设备连接数
                int devNum = mUSBTransmit.usbDevice.USB_ScanDevice();
                // debug("设备连接数：" + devNum, true);
                if (devNum <= 0) {
                    textView.append("无设备连接!\n");
                    link_and_open_device_btn.setEnabled(true);
                    return;
                } else {
                    textView.append("设备连接数为：" + String.format("%d", devNum) + "\n");
                }
                textView.invalidate();
                //打开设备
                if (!mUSBTransmit.usbDevice.USB_OpenDevice(devIndex)) {
                    textView.append("打开设备失败!\n");
                    link_and_open_device_btn.setEnabled(true);
                    return;
                } else {
                    textView.append("打开设备成功!\n");
                    progressbar.setVisibility(View.GONE);

                }
                textView.invalidate();
            }
        });

        read_data_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                SendMsg = setUSbSendMsg(DataNum,PacketSize,CMD_01,FARA_01);
//
//                mUSBTransmit.usbDevice.MSG_Send(devIndex, SendMsg, 100);

//                textView.setText("");
//                 SendMsg = setUSbSendMsg(DataNum, PacketSize, CMD_01, FARA_01);
//                 readFromUsb();
                // readUsb();

                // usbHandler.postDelayed(readFromUsbRunable,0);

                // USBApplication.executorService.execute(readFromUsbRun);

                // usbHandler.postDelayed(readFromUsbRun,0);

                // new ReadRun().run();

//                LCD_STATE = LCD_STATE_PREPARE;
//                SendMsg = setUSbSendMsg(DataNum, PacketSize);
//                readFromUsb();

//                LCD_STATE = LCD_STATE_CONFIRM;
//                SendMsg = setUSbSendMsg(DataNum, PacketSize);
//                readFromUsb();
                //readFromUsb_1();

                // readFromUsb_2();

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

                WriteDataBuffer[8] = 0;//0状态  1数据（计时）
                WriteDataBuffer[9] = 1;//1按键 2手指灵活 3旋钮 4九洞
                WriteDataBuffer[10] = 'T';//按键1
                WriteDataBuffer[11] = 'H';
                WriteDataBuffer[12] = 'E';
                WriteDataBuffer[13] = 'H';//按键4
                WriteDataBuffer[14] = 1;//脚踏键1
                WriteDataBuffer[15] = 1;//脚踏键2
                WriteDataBuffer[16] = 0;//1按键图片显示 0按键文字显示
                //WriteDataBuffer[17] = cnt++;//开始命令
                WriteDataBuffer[17] = 1;//开始命令
                WriteDataBuffer[18] = 0;//结束命令
                WriteDataBuffer[19] = 1;//1小LCD显示准备 2收到清标志
                WriteDataBuffer[20] = 0;//0-最多4个按键  2-最多2个按键

                usbHandler.post(readFromUsb_2_Run);


            }
        });

        stop_read_data_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // usbHandler.removeCallbacks(readFromUsbRunable);
                // USBApplication.executorService.
                //usbHandler.removeCallbacks(readFromUsb_2_Run);


                if (flag_1) {
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

                    WriteDataBuffer[8] = 0;//0状态  1数据（计时）
                    WriteDataBuffer[9] = 1;//1按键 2手指灵活 3旋钮 4九洞
                    WriteDataBuffer[10] = 'H';//按键1
                    WriteDataBuffer[11] = 'T';//按键2
                    WriteDataBuffer[12] = 'E';//按键3
                    WriteDataBuffer[13] = 'T';//按键4
                    WriteDataBuffer[14] = 1;//脚踏键1
                    WriteDataBuffer[15] = 1;//脚踏键2
                    WriteDataBuffer[16] = 0;//1按键图片显示 0按键文字显示

                    WriteDataBuffer[17] = 1;//开始命令
                    WriteDataBuffer[18] = 0;//结束命令
                    WriteDataBuffer[19] = 2;//1小LCD显示准备 2收到清标志
                    WriteDataBuffer[20] = 0;//0-最多4个按键  2-最多2个按键
                    flag_1 = false;
                } else {
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

                    WriteDataBuffer[8] = 0;//0状态  1数据（计时）
                    WriteDataBuffer[9] = 1;//1按键 2手指灵活 3旋钮 4九洞
                    WriteDataBuffer[10] = 'H';//按键1
                    WriteDataBuffer[11] = 'T';//按键2
                    WriteDataBuffer[12] = 'E';//按键3
                    WriteDataBuffer[13] = 'T';//按键4
                    WriteDataBuffer[14] = 1;//脚踏键1
                    WriteDataBuffer[15] = 1;//脚踏键2
                    WriteDataBuffer[16] = 1;//1按键图片显示 0按键文字显示

                    WriteDataBuffer[17] = 1;//开始命令
                    WriteDataBuffer[18] = 0;//结束命令
                    WriteDataBuffer[19] = 1;//1小LCD显示准备 2收到清标志
                    WriteDataBuffer[20] = 0;//0-最多4个按键  2-最多2个按键
                }


            }
        });

    }

    boolean flag_1 = true;

    public int DataNum = 1;
    public int PacketSize = 512;

    private int DataNumIndex = 0;
    byte[] WriteDataBuffer = new byte[PacketSize];
    byte[] ReadDataBuffer;
    int ret;
    boolean state;

    private void readFromUsb_1() {

        textView.setText("");
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

        WriteDataBuffer[8] = 0;//0状态  1数据（计时）
        WriteDataBuffer[9] = 1;//1按键 2手指灵活 3旋钮 4九洞
        WriteDataBuffer[10] = 'H';//按键1
        WriteDataBuffer[11] = 'T';
        WriteDataBuffer[12] = 'E';
        WriteDataBuffer[13] = 'T';//按键4
        WriteDataBuffer[14] = 1;//脚踏键1
        WriteDataBuffer[15] = 1;//脚踏键2
        WriteDataBuffer[16] = 0;//1按键图片显示 0按键文字显示
        //WriteDataBuffer[17] = cnt++;//开始命令
        WriteDataBuffer[17] = 1;//开始命令
        WriteDataBuffer[18] = 0;//结束命令
        WriteDataBuffer[19] = 1;//1小LCD显示准备 2收到清标志
        WriteDataBuffer[20] = 0;//0-最多4个按键  2-最多2个按键

        state = mUSBTransmit.usbDevice.USB_BulkWriteData(devIndex, mUSBTransmit.EP1_OUT, WriteDataBuffer, PacketSize, 100);
        if (state) {
            textView.append("写命令成功！\n");
        } else {
            textView.append("写命令失败！\n");
            return;
        }
        textView.invalidate();


        ReadDataBuffer = new byte[PacketSize];
        do {
            ret = mUSBTransmit.usbDevice.USB_BulkReadData(devIndex, mUSBTransmit.EP1_IN, ReadDataBuffer, PacketSize, 100);
            if (ret != PacketSize) {
                break;
            } else {
                DataNumIndex--;
            }
        } while (DataNumIndex > 0);
        if (DataNumIndex > 0) {
            textView.append("读数据失败！\n");
            return;
        } else {
//                        textView.append("读数据成功！\n");
//                        textView.append(Arrays.toString(ReadDataBuffer) + "\n");
            textView.setText(Arrays.toString(ReadDataBuffer) + "\n");

//            Message msg = Message.obtain();
//            Bundle bundle = new Bundle();
//            bundle.putByteArray("pParam", ReadDataBuffer);
//            msg.setData(bundle);
//            handler.sendMessage(msg);

        }
        textView.invalidate();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // mUSBTransmit.usbDevice.
        finish();
    }

    Thread usThread = new Thread() {

        @Override
        public void run() {
            // super.run();

            while (flag != 0) {

                //if (Thread.currentThread().isInterrupted()) {
                //RemindUtils.logE(TAG, "Interrupted ...");
                //    break;
                //}

                SendMsg = new USBDevice.USB_MSG();
                byte[] send = new byte[256 - 8];

                for (int i = 0; i < send.length; i++) {
                    send[i] = 0x00;
                }

                send[0] = 0x00;
                send[1] = 0x01;
                send[2] = 0x00;
                send[3] = 0x01;
                SendMsg.Message = 1;
                SendMsg.ParamSize = 256;
                SendMsg.pParam = send;

                mUSBTransmit.usbDevice.MSG_Send(devIndex, SendMsg, 50);


                GetMsg = new USBDevice.USB_MSG();

                GetMsg.pParam = new byte[256];

                mUSBTransmit.usbDevice.MSG_Get_1(devIndex, GetMsg, 50);

                byte[] pParam = GetMsg.pParam;

                RemindUtils.logE(TAG, "--------" + Arrays.toString(GetMsg.pParam));

                Bundle bundle = new Bundle();
                bundle.putByteArray("pParam", GetMsg.pParam);
                Message msg = Message.obtain();
                msg.setData(bundle);

                handler.sendMessage(msg);

            }
        }
    };

    public byte[] CMD_01 = new byte[]{0x00, 0x01};

    public byte[] FARA_01 = new byte[]{0x00, 0x01};

    public byte TF_ADDR;

    public byte ATTR_ASC = 0x00;
    public byte ATTR_IMG = 0x01;
    public byte FONT;
    public byte COLOR;


    public byte STATE_0 = 0x00;
    public byte DATA_1 = 0x01;

    public byte TYPE_0 = 0x00;
    public byte TYPE_1 = 0x01;

    public byte KEY_1 = 45;
    public byte KEY_2 = 0x00;
    public byte KEY_3 = 0x00;
    public byte KEY_4 = 0x00;
    public byte KEY_5 = 0x00;
    public byte KEY_6 = 0x00;

    public byte SHOW_TYPE_ASC = 0x00;
    public byte SHOW_TYPE_IMG = 0x01;

    public byte LCD_STATE = 0x01;

    public byte LCD_STATE_PREPARE = 0x01;
    public byte LCD_STATE_CONFIRM = 0x02;

    public USBDevice.USB_MSG setUSbSendMsg(int DataNum, int PacketSize) {
        USBDevice.USB_MSG UsbSendMsg = new USBDevice.USB_MSG();
        byte[] temp = new byte[PacketSize];
        for (int i = 0; i < temp.length; i++) {
            if (i == 0) {
                temp[0] = STATE_0;
            } else if (i == 1) {
                temp[1] = TYPE_1;
            } else if (i == 2) {
                temp[2] = KEY_1;
            } else if (i == 3) {
                temp[3] = KEY_2;
            } else if (i == 4) {
                temp[3] = KEY_3;
            } else if (i == 5) {
                temp[3] = KEY_4;
            } else if (i == 6) {
                temp[3] = KEY_5;
            } else if (i == 7) {
                temp[3] = KEY_6;
            } else if (i == 8) {
                temp[3] = SHOW_TYPE_IMG;
            } else if (i == 9) {
                temp[3] = 0x00;
            } else if (i == 10) {
                temp[3] = 0x00;
            } else if (i == 11) {
                temp[3] = LCD_STATE;
            } else if (i == 12) {
                temp[3] = 0x00;
            } else {
                temp[i] = 0x00;
            }

        }
        UsbSendMsg.Message = DataNum;
        UsbSendMsg.ParamSize = PacketSize;
        UsbSendMsg.pParam = temp;
        return UsbSendMsg;
    }


    public USBDevice.USB_MSG setUSbSendMsg(int DataNum, int PacketSize, byte[] CMD, byte[] FARA) {
        USBDevice.USB_MSG UsbSendMsg = new USBDevice.USB_MSG();
        byte[] temp = new byte[PacketSize];
        for (int i = 0; i < temp.length; i++) {
            if (i == 0) {
                temp[0] = CMD[0];
            } else if (i == 1) {
                temp[1] = CMD[1];
            } else if (i == 2) {
                temp[2] = FARA[0];
            } else if (i == 3) {
                temp[3] = FARA[1];
            } else {
                temp[i] = 0x00;
            }

        }
        UsbSendMsg.Message = DataNum;
        UsbSendMsg.ParamSize = PacketSize;
        UsbSendMsg.pParam = temp;
        return UsbSendMsg;
    }

    class UsbMsgThread extends Thread {

        private int devIndex;
        private USBTransmit mUSBTransmit;
        private Handler msgHandler;

        public UsbMsgThread(int devIndex, USBTransmit mUSBTransmit, Handler msgHandler) {
            this.devIndex = devIndex;
            this.mUSBTransmit = mUSBTransmit;
            this.msgHandler = msgHandler;
        }

        public void run() {
            USBDevice.USB_MSG GetMsg = new USBDevice.USB_MSG();

            GetMsg.pParam = new byte[256];

            while (!Thread.interrupted()) {
                int state = mUSBTransmit.usbDevice.MSG_Get_1(devIndex, GetMsg, 100);
                if (state < 0) {
                    continue;
                }

                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putByteArray("pParam", GetMsg.pParam);
                msg.setData(bundle);
                msgHandler.sendMessage(msg);

            }
        }
    }

    byte[] pParam = null;


    Runnable readFromUsbRun = new Runnable() {

        @Override
        public void run() {

            USBApplication.executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {

                        mUSBTransmit.usbDevice.MSG_Send(devIndex, SendMsg, 100);

                        GetMsg = new USBDevice.USB_MSG();

                        GetMsg.pParam = new byte[256];

                        mUSBTransmit.usbDevice.MSG_Get_1(devIndex, GetMsg, 100);

                        pParam = GetMsg.pParam;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(Arrays.toString(pParam));

                                if (pParam[8] == 0x01 || pParam[9] == 0x01 || pParam[10] == 0x01 || pParam[11] == 0x01) {
                                    if (pParam[8] == 0x01 && !(pParam[9] == 0x01 || pParam[10] == 0x01 || pParam[11] == 0x01)) {
                                        show_key_1_tv.setEnabled(false);
                                        show_key_2_tv.setEnabled(true);
                                        show_key_3_tv.setEnabled(true);
                                        show_key_4_tv.setEnabled(true);
                                    }
                                    if (pParam[9] == 0x01 && !(pParam[8] == 0x01 || pParam[10] == 0x01 || pParam[11] == 0x01)) {
                                        show_key_1_tv.setEnabled(true);
                                        show_key_2_tv.setEnabled(false);
                                        show_key_3_tv.setEnabled(true);
                                        show_key_4_tv.setEnabled(true);
                                    }
                                    if (pParam[10] == 0x01 && !(pParam[8] == 0x01 || pParam[9] == 0x01 || pParam[11] == 0x01)) {
                                        show_key_1_tv.setEnabled(true);
                                        show_key_2_tv.setEnabled(true);
                                        show_key_3_tv.setEnabled(false);
                                        show_key_4_tv.setEnabled(true);
                                    }
                                    if (pParam[11] == 0x01 && !(pParam[9] == 0x01 || pParam[10] == 0x01 || pParam[8] == 0x01)) {
                                        show_key_1_tv.setEnabled(true);
                                        show_key_2_tv.setEnabled(true);
                                        show_key_3_tv.setEnabled(true);
                                        show_key_4_tv.setEnabled(false);
                                    }

                                } else {
                                    show_key_1_tv.setEnabled(true);
                                    show_key_2_tv.setEnabled(true);
                                    show_key_3_tv.setEnabled(true);
                                    show_key_4_tv.setEnabled(true);
                                }
                            }
                        });

                        //RemindUtils.logE(TAG,"--------1--------");
                        //textView.setText(Arrays.toString(GetMsg.pParam));
                        // if (pParam[4] == 0x01 || pParam[5] == 0x01 || pParam[6] == 0x01 || pParam[7] == 0x01) {
                        // RemindUtils.logE(TAG,"--------2--------");
                        // textView.setText(Arrays.toString(GetMsg.pParam));

//                        Message msg = Message.obtain();
//                        Bundle bundle = new Bundle();
//                        bundle.putByteArray("pParam", GetMsg.pParam);
//
//                        msg.setData(bundle);
//                        handler.sendMessage(msg);

                        // }
                    }
                }
            });


        }
    };


    Runnable readFromUsbRunable = new Runnable() {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                mUSBTransmit.usbDevice.MSG_Send(devIndex, SendMsg, 100);

                GetMsg = new USBDevice.USB_MSG();

                GetMsg.pParam = new byte[256];

                mUSBTransmit.usbDevice.MSG_Get_1(devIndex, GetMsg, 100);

                byte[] pParam = GetMsg.pParam;
                //RemindUtils.logE(TAG,"--------1--------");

                textView.setText(Arrays.toString(GetMsg.pParam));

                if (pParam[8] == 0x01 || pParam[9] == 0x01 || pParam[10] == 0x01 || pParam[11] == 0x01) {
                    if (pParam[8] == 0x01 && !(pParam[9] == 0x01 || pParam[10] == 0x01 || pParam[11] == 0x01)) {
                        show_key_1_tv.setEnabled(false);
                        show_key_2_tv.setEnabled(true);
                        show_key_3_tv.setEnabled(true);
                        show_key_4_tv.setEnabled(true);
                    }
                    if (pParam[9] == 0x01 && !(pParam[8] == 0x01 || pParam[10] == 0x01 || pParam[11] == 0x01)) {
                        show_key_1_tv.setEnabled(true);
                        show_key_2_tv.setEnabled(false);
                        show_key_3_tv.setEnabled(true);
                        show_key_4_tv.setEnabled(true);
                    }
                    if (pParam[10] == 0x01 && !(pParam[8] == 0x01 || pParam[9] == 0x01 || pParam[11] == 0x01)) {
                        show_key_1_tv.setEnabled(true);
                        show_key_2_tv.setEnabled(true);
                        show_key_3_tv.setEnabled(false);
                        show_key_4_tv.setEnabled(true);
                    }
                    if (pParam[11] == 0x01 && !(pParam[9] == 0x01 || pParam[10] == 0x01 || pParam[8] == 0x01)) {
                        show_key_1_tv.setEnabled(true);
                        show_key_2_tv.setEnabled(true);
                        show_key_3_tv.setEnabled(true);
                        show_key_4_tv.setEnabled(false);
                    }

                } else {
                    show_key_1_tv.setEnabled(true);
                    show_key_2_tv.setEnabled(true);
                    show_key_3_tv.setEnabled(true);
                    show_key_4_tv.setEnabled(true);
                }

            }
        }
    };


    byte cnt = 0;

    public void readFromUsb_2() {

    }

    Runnable readFromUsb_2_Run = new Runnable() {
        @Override
        public void run() {
            USBApplication.executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {


                        //textView.setText("");
                        DataNumIndex = DataNum;
                        // WriteDataBuffer = ;

                        state = mUSBTransmit.usbDevice.USB_BulkWriteData(devIndex, mUSBTransmit.EP1_OUT, WriteDataBuffer, PacketSize, 100);
                        if (state) {
                            //textView.append("写命令成功！\n");
                        } else {
                            //textView.append("写命令失败！\n");
                            return;
                        }
                        // textView.invalidate();


                        ReadDataBuffer = new byte[PacketSize];
                        do {
                            ret = mUSBTransmit.usbDevice.USB_BulkReadData(devIndex, mUSBTransmit.EP1_IN, ReadDataBuffer, PacketSize, 100);
                            if (ret != PacketSize) {
                                break;
                            } else {
                                DataNumIndex--;
                            }
                        } while (DataNumIndex > 0);

                        if (DataNumIndex > 0) {
                            //textView.append("读数据失败！\n");
                            //textView.append(Arrays.toString(ReadDataBuffer) + "\n");
                            return;
                        } else {
                            //textView.append("读数据成功！\n");
                            //textView.setText(Arrays.toString(ReadDataBuffer) + "\n");
                            // RemindUtils.logE(TAG, Arrays.toString(ReadDataBuffer));

                            Message msg = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putByteArray("pParam", ReadDataBuffer);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }

                    //textView.invalidate();
                }
            });

        }
    };


    public void readFromUsb() {
        textView.setText("");

        USBApplication.executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    mUSBTransmit.usbDevice.MSG_Send(devIndex, SendMsg, 100);

                    GetMsg = new USBDevice.USB_MSG();

                    GetMsg.pParam = new byte[256];

                    mUSBTransmit.usbDevice.MSG_Get_1(devIndex, GetMsg, 100);


                    byte[] pParam = GetMsg.pParam;
                    RemindUtils.logE(TAG, Arrays.toString(GetMsg.pParam));
                    // textView.setText(Arrays.toString(GetMsg.pParam));


                    Message msg = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("pParam", GetMsg.pParam);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        });


    }

    public void readUsb() {
        textView.setText("");
        SendMsg = setUSbSendMsg(DataNum, PacketSize, CMD_01, FARA_01);

        while (true) {

            mUSBTransmit.usbDevice.MSG_Send(devIndex, SendMsg, 100);

            GetMsg = new USBDevice.USB_MSG();

            GetMsg.pParam = new byte[256];

            mUSBTransmit.usbDevice.MSG_Get_1(devIndex, GetMsg, 100);

            byte[] pParam = GetMsg.pParam;
            textView.setText(Arrays.toString(pParam));
            if (pParam[1] == 0x01 || pParam[2] == 0x01 || pParam[3] == 0x01 || pParam[4] == 0x01) {
                if (pParam[4] == 0x01) {
                    show_key_1_tv.setEnabled(false);
                } else {
                    show_key_1_tv.setEnabled(true);
                }
                if (pParam[5] == 0x01) {
                    show_key_2_tv.setEnabled(false);
                } else {
                    show_key_2_tv.setEnabled(true);
                }
                if (pParam[6] == 0x01) {
                    show_key_3_tv.setEnabled(false);
                } else {
                    show_key_3_tv.setEnabled(true);
                }
                if (pParam[7] == 0x01) {
                    show_key_4_tv.setEnabled(false);
                } else {
                    show_key_4_tv.setEnabled(true);
                }

            } else {
                show_key_1_tv.setEnabled(true);
                show_key_2_tv.setEnabled(true);
                show_key_3_tv.setEnabled(true);
                show_key_4_tv.setEnabled(true);
            }


        }
    }

    USBDevice.USB_MSG GetMsg = new USBDevice.USB_MSG();
    USBDevice.USB_MSG SendMsg = new USBDevice.USB_MSG();
    int flag = -3;

    public void readDataFromUsb() {

    }

    Handler usbHandler;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            byte[] pParam = bundle.getByteArray("pParam");
            textView.setText(Arrays.toString(pParam));
            if (pParam[1] == 0x01 || pParam[2] == 0x01 || pParam[3] == 0x01 || pParam[4] == 0x01) {
//                if (pParam[4] == 0x01 && !(pParam[5] == 0x01 || pParam[6] == 0x01 || pParam[7] == 0x01)) {
//                    show_key_1_tv.setEnabled(false);
//                    show_key_2_tv.setEnabled(true);
//                    show_key_3_tv.setEnabled(true);
//                    show_key_4_tv.setEnabled(true);
//                }
//                if (pParam[5] == 0x01 && !(pParam[4] == 0x01 || pParam[6] == 0x01 || pParam[7] == 0x01)) {
//                    show_key_1_tv.setEnabled(true);
//                    show_key_2_tv.setEnabled(false);
//                    show_key_3_tv.setEnabled(true);
//                    show_key_4_tv.setEnabled(true);
//                }
//                if (pParam[6] == 0x01 && !(pParam[4] == 0x01 || pParam[5] == 0x01 || pParam[7] == 0x01)) {
//                    show_key_1_tv.setEnabled(true);
//                    show_key_2_tv.setEnabled(true);
//                    show_key_3_tv.setEnabled(false);
//                    show_key_4_tv.setEnabled(true);
//                }
//                if (pParam[7] == 0x01 && !(pParam[5] == 0x01 || pParam[6] == 0x01 || pParam[4] == 0x01)) {
//                    show_key_1_tv.setEnabled(true);
//                    show_key_2_tv.setEnabled(true);
//                    show_key_3_tv.setEnabled(true);
//                    show_key_4_tv.setEnabled(false);
//                }

                if (pParam[1] == 0x01) {
                    show_key_1_tv.setEnabled(false);
                } else {
                    show_key_1_tv.setEnabled(true);
                }
                if (pParam[2] == 0x01) {
                    show_key_2_tv.setEnabled(false);
                } else {
                    show_key_2_tv.setEnabled(true);
                }
                if (pParam[3] == 0x01) {
                    show_key_3_tv.setEnabled(false);
                } else {
                    show_key_3_tv.setEnabled(true);
                }
                if (pParam[4] == 0x01) {
                    show_key_4_tv.setEnabled(false);
                } else {
                    show_key_4_tv.setEnabled(true);
                }

            } else {
                show_key_1_tv.setEnabled(true);
                show_key_2_tv.setEnabled(true);
                show_key_3_tv.setEnabled(true);
                show_key_4_tv.setEnabled(true);
            }

        }
    };

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
        read_data_btn = (AppCompatButton) findViewById(R.id.read_data_btn);
        stop_read_data_btn = (AppCompatButton) findViewById(R.id.stop_read_data_btn);

        show_key_4_tv = (Button) findViewById(R.id.show_key_4_tv);
        show_key_1_tv = (Button) findViewById(R.id.show_key_1_tv);
        show_key_2_tv = (Button) findViewById(R.id.show_key_2_tv);
        show_key_3_tv = (Button) findViewById(R.id.show_key_3_tv);
    }
}

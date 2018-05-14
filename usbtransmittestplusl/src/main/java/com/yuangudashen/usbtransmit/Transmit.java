package com.yuangudashen.usbtransmit;

import com.yuangudashen.usbtransmit.USBDevice;
import com.yuangudashen.usbtransmit.USBDevice.UsbDev;

public class Transmit {
    public Transmit() {
    }

    public static int MSG_Send(int DevIndex, Transmit.USB_MSG msg, int TimeOut) {
        byte[] writebuffer = new byte[msg.ParamSize];
        writebuffer[0] = (byte)(msg.Message >> 24);
        writebuffer[1] = (byte)(msg.Message >> 16);
        writebuffer[2] = (byte)(msg.Message >> 8);
        writebuffer[3] = (byte)(msg.Message >> 0);
        writebuffer[4] = (byte)(msg.ParamSize >> 24);
        writebuffer[5] = (byte)(msg.ParamSize >> 16);
        writebuffer[6] = (byte)(msg.ParamSize >> 8);
        writebuffer[7] = (byte)(msg.ParamSize >> 0);

        int count;
        for(count = 8; count < msg.ParamSize; ++count) {
            writebuffer[count] = msg.pParam[count-8];
        }

        count = USBDevice.connection.bulkTransfer(((UsbDev)USBDevice.usbDeviceList.get(DevIndex)).BulkOutEndpoint, writebuffer, writebuffer.length, TimeOut);
        if(count != writebuffer.length) {
            return -2;
        } else {
            if(writebuffer.length % 64 == 0) {
                USBDevice.connection.bulkTransfer(((UsbDev)USBDevice.usbDeviceList.get(DevIndex)).BulkOutEndpoint, writebuffer, 0, TimeOut);
            }

            return 0;
        }
    }

    public static int MSG_Get(int DevIndex, Transmit.USB_MSG msg, int TimeOut) {
        byte[] readbuffer = new byte[8];
        int count = USBDevice.connection.bulkTransfer(((UsbDev)USBDevice.usbDeviceList.get(DevIndex)).BulkInEndpoint, readbuffer, readbuffer.length, TimeOut);
        if(count != 8) {
            return -3;
        } else {
            msg.Message = readbuffer[0] << 24 | readbuffer[1] << 16 | readbuffer[2] << 8 | readbuffer[3];
            msg.ParamSize = readbuffer[4] << 24 | readbuffer[5] << 16 | readbuffer[6] << 8 | readbuffer[7];
            if(msg.ParamSize > 0) {
                readbuffer = new byte[msg.ParamSize];
                count = USBDevice.connection.bulkTransfer(((UsbDev)USBDevice.usbDeviceList.get(DevIndex)).BulkInEndpoint, readbuffer, readbuffer.length, TimeOut);
                if(count != msg.ParamSize) {
                    return -3;
                }
            }

            for(int i = 0; i < msg.ParamSize; ++i) {
                msg.pParam[i] = readbuffer[i];
            }

            return 0;
        }
    }

    public static class USB_MSG {
        public int Message;
        public int ParamSize;
        public byte[] pParam;

        public USB_MSG() {
        }
    }

    public class MSG_STATE {
        static final int MSG_TRAN_SUCCESS = 0;
        static final int MSG_TRAN_FAILD = -1;
        static final int MSG_STATE_USB_WRITE_ERROR = -2;
        static final int MSG_STATE_USB_READ_ERROR = -3;
        static final int MSG_STATE_MALLOC_ERROR = -4;

        public MSG_STATE() {
        }
    }

    static class MSG_ENUM {
        static int MSG_BOOTLOADER_GET_FW_INFO = 256;
        static int MSG_BOOTLOADER_TRAN_DATA;
        static int MSG_BOOTLOADER_EXECUTE_FW;
        static int MSG_BOOTLOADER_ERASE_FLASH;
        static int MSG_BOOTLOADER_VERIFY;
        static int MSG_BOOTLOADER_CHECK_MD5;
        static int MSG_BOOTLOADER_GET_FUNCSTR;
        static int MSG_SPI_INIT;
        static int MSG_SPI_WRITE_BYTES;
        static int MSG_SPI_ASYNC_WRITE_BYTES;
        static int MSG_SPI_READ_BYTES;
        static int MSG_SPI_WRITE_READ_BYTES;
        static int MSG_SPI_SLAVE_WRITE_BYTES;
        static int MSG_SPI_SLAVE_READ_BYTES;
        static int MSG_SPI_FLASH_INIT;
        static int MSG_SPI_FLASH_READ_ID;
        static int MSG_SPI_FLASH_WRITE;
        static int MSG_SPI_FLASH_READ;
        static int MSG_SPI_FLASH_ERASE_SECTORS;
        static int MSG_SPI_FLASH_READ_FAST;
        static int MSG_SPI_BLOCK_WRITE;
        static int MSG_SPI_BLOCK_READ;
        static int MSG_SPI_BLOCK_WRITE_READ;
        static int MSG_SPI_WRITE_BITS;
        static int MSG_SPI_READ_BITS;
        static int MSG_SPI_WRITE_READ_BITS;
        static int MSG_NAND_INIT;
        static int MSG_NAND_GET_ID;
        static int MSG_NAND_SET_MEM_INFO;
        static int MSG_NAND_WRITE_PAGE;
        static int MSG_NAND_READ_PAGE;
        static int MSG_NAND_WRITE_SPARE_AREA;
        static int MSG_NAND_READ_SPARE_AREA;
        static int MSG_NAND_ERASE_BLOCK;
        static int MSG_SNIFFER_INIT;
        static int MSG_SNIFFER_START_READ;
        static int MSG_SNIFFER_STOP_READ;
        static int MSG_SNIFFER_READ_DATA;
        static int MSG_SNIFFER_WRITE_DATA;
        static int MSG_SNIFFER_CONTINUE_WRITE_DATA;
        static int MSG_SNIFFER_STOP_CONTINUE_WRITE;
        static int MSG_ADC_INIT;
        static int MSG_ADC_READ;
        static int MSG_ADC_START_CONTINUE_READ;
        static int MSG_ADC_STOP_CONTINUE_READ;
        static int MSG_ADC_GET_DATA;
        static int MSG_GPIO_SET_INPUT;
        static int MSG_GPIO_SET_OUTPUT;
        static int MSG_GPIO_SET_OPEN_DRAIN;
        static int MSG_GPIO_WRITE;
        static int MSG_GPIO_READ;
        static int MSG_IIC_INIT;
        static int MSG_IIC_WRITE;
        static int MSG_IIC_READ;
        static int MSG_IIC_WRITE_READ;
        static int MSG_IIC_SLAVE_WRITE;
        static int MSG_IIC_SLAVE_READ;
        static int MSG_IIC_SLAVE_WRITE_REMAIN;
        static int MSG_PWM_INIT;
        static int MSG_PWM_START;
        static int MSG_PWM_STOP;

        MSG_ENUM() {
        }

        static {
            MSG_BOOTLOADER_TRAN_DATA = MSG_BOOTLOADER_GET_FW_INFO + 1;
            MSG_BOOTLOADER_EXECUTE_FW = MSG_BOOTLOADER_GET_FW_INFO + 2;
            MSG_BOOTLOADER_ERASE_FLASH = MSG_BOOTLOADER_GET_FW_INFO + 3;
            MSG_BOOTLOADER_VERIFY = MSG_BOOTLOADER_GET_FW_INFO + 4;
            MSG_BOOTLOADER_CHECK_MD5 = MSG_BOOTLOADER_GET_FW_INFO + 5;
            MSG_BOOTLOADER_GET_FUNCSTR = MSG_BOOTLOADER_GET_FW_INFO + 6;
            MSG_SPI_INIT = 512;
            MSG_SPI_WRITE_BYTES = MSG_SPI_INIT + 1;
            MSG_SPI_ASYNC_WRITE_BYTES = MSG_SPI_INIT + 2;
            MSG_SPI_READ_BYTES = MSG_SPI_INIT + 3;
            MSG_SPI_WRITE_READ_BYTES = MSG_SPI_INIT + 4;
            MSG_SPI_SLAVE_WRITE_BYTES = MSG_SPI_INIT + 5;
            MSG_SPI_SLAVE_READ_BYTES = MSG_SPI_INIT + 6;
            MSG_SPI_FLASH_INIT = MSG_SPI_INIT + 7;
            MSG_SPI_FLASH_READ_ID = MSG_SPI_INIT + 8;
            MSG_SPI_FLASH_WRITE = MSG_SPI_INIT + 9;
            MSG_SPI_FLASH_READ = MSG_SPI_INIT + 10;
            MSG_SPI_FLASH_ERASE_SECTORS = MSG_SPI_INIT + 11;
            MSG_SPI_FLASH_READ_FAST = MSG_SPI_INIT + 12;
            MSG_SPI_BLOCK_WRITE = MSG_SPI_INIT + 13;
            MSG_SPI_BLOCK_READ = MSG_SPI_INIT + 14;
            MSG_SPI_BLOCK_WRITE_READ = MSG_SPI_INIT + 15;
            MSG_SPI_WRITE_BITS = MSG_SPI_INIT + 16;
            MSG_SPI_READ_BITS = MSG_SPI_INIT + 17;
            MSG_SPI_WRITE_READ_BITS = MSG_SPI_INIT + 18;
            MSG_NAND_INIT = 768;
            MSG_NAND_GET_ID = MSG_NAND_INIT + 1;
            MSG_NAND_SET_MEM_INFO = MSG_NAND_INIT + 2;
            MSG_NAND_WRITE_PAGE = MSG_NAND_INIT + 3;
            MSG_NAND_READ_PAGE = MSG_NAND_INIT + 4;
            MSG_NAND_WRITE_SPARE_AREA = MSG_NAND_INIT + 5;
            MSG_NAND_READ_SPARE_AREA = MSG_NAND_INIT + 6;
            MSG_NAND_ERASE_BLOCK = MSG_NAND_INIT + 7;
            MSG_SNIFFER_INIT = 1024;
            MSG_SNIFFER_START_READ = MSG_SNIFFER_INIT + 1;
            MSG_SNIFFER_STOP_READ = MSG_SNIFFER_INIT + 2;
            MSG_SNIFFER_READ_DATA = MSG_SNIFFER_INIT + 3;
            MSG_SNIFFER_WRITE_DATA = MSG_SNIFFER_INIT + 4;
            MSG_SNIFFER_CONTINUE_WRITE_DATA = MSG_SNIFFER_INIT + 5;
            MSG_SNIFFER_STOP_CONTINUE_WRITE = MSG_SNIFFER_INIT + 6;
            MSG_ADC_INIT = 1280;
            MSG_ADC_READ = MSG_ADC_INIT + 1;
            MSG_ADC_START_CONTINUE_READ = MSG_ADC_INIT + 2;
            MSG_ADC_STOP_CONTINUE_READ = MSG_ADC_INIT + 3;
            MSG_ADC_GET_DATA = MSG_ADC_INIT + 4;
            MSG_GPIO_SET_INPUT = 1536;
            MSG_GPIO_SET_OUTPUT = MSG_GPIO_SET_INPUT + 1;
            MSG_GPIO_SET_OPEN_DRAIN = MSG_GPIO_SET_INPUT + 2;
            MSG_GPIO_WRITE = MSG_GPIO_SET_INPUT + 3;
            MSG_GPIO_READ = MSG_GPIO_SET_INPUT + 4;
            MSG_IIC_INIT = 1792;
            MSG_IIC_WRITE = MSG_IIC_INIT + 1;
            MSG_IIC_READ = MSG_IIC_INIT + 2;
            MSG_IIC_WRITE_READ = MSG_IIC_INIT + 3;
            MSG_IIC_SLAVE_WRITE = MSG_IIC_INIT + 4;
            MSG_IIC_SLAVE_READ = MSG_IIC_INIT + 5;
            MSG_IIC_SLAVE_WRITE_REMAIN = MSG_IIC_INIT + 6;
            MSG_PWM_INIT = 2048;
            MSG_PWM_START = MSG_PWM_INIT + 1;
            MSG_PWM_STOP = MSG_PWM_INIT + 2;
        }
    }
}
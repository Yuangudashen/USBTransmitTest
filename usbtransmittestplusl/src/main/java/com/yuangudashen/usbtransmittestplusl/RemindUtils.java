package com.yuangudashen.usbtransmittestplusl;

import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 2017/11/28/028.
 */

public class RemindUtils {

    private Context mContext = USBApplication.applicationContext;

    public static void logE(String tag,String msg){
        Log.e(tag,msg);
    }

}

package com.xiaochi.player.util

import android.os.Handler
import android.os.Looper


/**
 * ClassName:ThreadUtil
 * Description:
 */
object ThreadUtil {
    val handler = Handler(Looper.getMainLooper());
    /**
     * 运行在主线程中
     */
    fun runOnMainThread(runnable:Runnable){
        handler.post(runnable)
    }

}
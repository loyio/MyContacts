package me.loyio.mycontacts.utils;

import android.os.Handler;

public class ThreadUtils {
    //定义主线程一个Handler对象
    public static Handler mhandler=new Handler();
    //UI线程下的
    public static void runInUIThread(Runnable task){
        //post分发消息
        mhandler.post(task);
    }
    //普通线程
    public static void runInThread(Runnable task){
        new Thread(task).start();
    }

}

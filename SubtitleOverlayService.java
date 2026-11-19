package com.example.deutschlivesubtitles;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SubtitleOverlayService extends Service {
    private static WindowManager wm;
    private static TextView de,en;
    private static WindowManager.LayoutParams params;
    @Override public void onCreate() {
        super.onCreate();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        LinearLayout box=new LinearLayout(this); box.setOrientation(1); box.setPadding(20,12,20,12);
        box.setBackgroundColor(0xCC101010);
        de=new TextView(this); de.setTextColor(Color.WHITE); de.setTextSize(20); de.setGravity(Gravity.CENTER);
        en=new TextView(this); en.setTextColor(0xFFFFD54F); en.setTextSize(17); en.setGravity(Gravity.CENTER);
        box.addView(de); box.addView(en);
        int type= android.os.Build.VERSION.SDK_INT>=26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        params=new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT,type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL; params.y=100;
        try { wm.addView(box,params); } catch(Exception ignored){}
    }
    public static void setGerman(String s){ if(de!=null)de.post(()->de.setText(s)); }
    public static void setEnglish(String s){ if(en!=null)en.post(()->en.setText(s)); }
    @Override public int onStartCommand(Intent i,int f,int id){return START_NOT_STICKY;}
    @Override public void onDestroy(){ if(wm!=null && de!=null){ try{wm.removeView(de.getParent() instanceof android.view.View ? (android.view.View)de.getParent():null);}catch(Exception ignored){} } de=null; en=null; super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}

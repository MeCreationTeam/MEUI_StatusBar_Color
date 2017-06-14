package com.meui.systemui.statusbar;
import android.app.*;
import android.content.*;
import android.util.*;
import android.widget.*;

public class StatusBarView extends FrameLayout
{
	public StatusBarView(Context context,AttributeSet attrs){
		super(context,attrs);
		this.setBackgroundColor(0xff000000);
		ActivityManager am = (ActivityManager)context.getSystemService(Context. ACTIVITY_SERVICE);
		ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
		String foregroundTaskPackageName = foregroundTaskInfo.topActivity .getPackageName();
		Toast.makeText(context,foregroundTaskPackageName,Toast.LENGTH_SHORT).show();
		
	}
	public StatusBarView(Context context,AttributeSet attrs,int defStyle){
		this(context,attrs);
	}
	
}

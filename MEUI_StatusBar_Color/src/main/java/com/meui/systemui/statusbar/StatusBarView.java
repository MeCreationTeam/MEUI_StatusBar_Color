package com.meui.systemui.statusbar;
import android.app.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.widget.*;

/**
 * http://www.cnblogs.com/xiaoxiaoshen/p/5191186.html
 * https://blog.csdn.net/u011386173/article/details/87880920
 * https://blog.csdn.net/u012259618/article/details/52195675
 * A custom view that is used for simulating tinted status bar.
 * @author zhaozihanzzh
 */

public class StatusBarView extends FrameLayout {
    private final Uri CONTENT_URI = Uri.parse("content://com.meui.RomCtrl/BarColors");
    private final ContentResolver resolver;
    private final MeSettingsObserver mObserver;
    private int mDefaultColor = 0x4D000000;
    private final Context mContext;
	private IActivityController.Stub controller;

    public StatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        resolver = context.getContentResolver();
        mObserver = new MeSettingsObserver();

        mDefaultColor = Settings.System.getInt(resolver, "sb_default_color", mDefaultColor);
    }
    public StatusBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        resolver = context.getContentResolver();
        mObserver = new MeSettingsObserver();

        mDefaultColor = Settings.System.getInt(resolver, "sb_default_color", mDefaultColor);
        
    }
	
	/**
     * Used to query background settings from the custom ContentProvider and set color.
     * @return foregroundTaskPackageName Get the package name of the current foreground app.
	 * @author zhaozihanzzh
	 */
	private final void tint(final String foregroundTaskPackageName) {
        final String where = "packageName=\"" + foregroundTaskPackageName + "\"";
        final Cursor mCursor = resolver.query(CONTENT_URI, null, where, null, null);
        if (mCursor != null && mCursor.moveToFirst()) {
            final boolean hasColor = mCursor.getInt(mCursor.getColumnIndex("hasColor")) == 1;
            final int color;
            if (hasColor) {
                color = mCursor.getInt(mCursor.getColumnIndex("color"));
            } else color = mDefaultColor;
            this.setBackgroundColor(color);
            mCursor.close();

        } else setBackgroundColor(mDefaultColor);
    }
	
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        resolver.registerContentObserver(Settings.System.getUriFor("sb_default_color"), true, mObserver);
        final Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				tint(msg.getData().getString("pkgName"));
			}
		};
		controller = new IActivityController.Stub() {

			@Override
			public boolean activityResuming(String pkg) {
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("pkgName", pkg);
				msg.setData(bundle);
				mHandler.sendMessage(msg);
				return true;
			}

			@Override
			public boolean activityStarting(Intent intent, String pkg) {
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("pkgName", pkg);
				msg.setData(bundle);
				mHandler.sendMessage(msg);
				return true;
			}

			@Override
			public boolean appCrashed(String processName, int pid, String shortMsg,
			String longMsg, long timeMillis, String stackTrace) {
				return false;
			}

			@Override
			public int appEarlyNotResponding(String processName, int pid, String annotation) {
				return 0;
			}

			@Override
			public int appNotResponding(String processName, int pid, String processStats) {
				return 0;
			}
		};
		try {
			ActivityManagerNative.getDefault().setActivityController(controller);
		}
		catch (RemoteException e) {}
    }
	
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mObserver != null) resolver.unregisterContentObserver(mObserver);
    }
	
	
    // https://my.oschina.net/fengcunhan/blog/151398
    private class MeSettingsObserver extends ContentObserver {
        public MeSettingsObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mDefaultColor = Settings.System.getInt(resolver, "sb_default_color", 0X4D000000);
        }
    }
}

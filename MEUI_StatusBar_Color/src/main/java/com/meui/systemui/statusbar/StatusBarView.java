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
 * A custom view that is used for simulating tinted status bar.
 * @author zhaozihanzzh
 */

public class StatusBarView extends FrameLayout {
    private Runnable update;
    private final Uri CONTENT_URI=Uri.parse("content://com.meui.RomCtrl/BarColors");
    private final ContentResolver resolver;
    private final MeSettingsObserver mObserver;
    private int mDelay=3000;
    private int mDefaultColor=0x4D000000;
    private final Context mContext;

    public StatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        resolver = context.getContentResolver();
        mObserver = new MeSettingsObserver();

        mDelay = Settings.System.getInt(resolver, "sb_check_delay", mDelay);
        mDefaultColor = Settings.System.getInt(resolver, "sb_default_color", mDefaultColor);
    }
    public StatusBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        resolver = context.getContentResolver();
        mObserver = new MeSettingsObserver();

        mDelay = Settings.System.getInt(resolver, "sb_check_delay", mDelay);
        mDefaultColor = Settings.System.getInt(resolver, "sb_default_color", mDefaultColor);
        
    }

    /**
     * Used to query background settings from the custom Content Provider and set color.
     * @return mDelay This is used for getting the value of field mDelay in an inner class,
     * since we can't make mDelay final.
     * @author zhaozihanzzh
     */
    private final int tint(final ActivityManager am) {
        final ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
        final String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
        final String where="packageName=\"" + foregroundTaskPackageName + "\"";
        final Cursor mCursor=resolver.query(CONTENT_URI, null, where, null, null);
        if (mCursor != null && mCursor.moveToFirst()) {
            final boolean hasColor=mCursor.getInt(mCursor.getColumnIndex("hasColor")) == 1;
            final int color;
            if (hasColor) {
                color = mCursor.getInt(mCursor.getColumnIndex("color"));
            } else color = mDefaultColor;
            this.setBackgroundColor(color);
            mCursor.close();

        } else setBackgroundColor(mDefaultColor);
        // Toast.makeText(mContext,foregroundTaskPackageName+mDelay,Toast.LENGTH_SHORT).show();
        return mDelay;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final Handler mHandler=new Handler();
        resolver.registerContentObserver(Settings.System.getUriFor("sb_check_delay"), true, mObserver);
        resolver.registerContentObserver(Settings.System.getUriFor("sb_default_color"), true, mObserver);
        final ActivityManager am = (ActivityManager)mContext.getSystemService(Context. ACTIVITY_SERVICE);
        tint(am);

        update = new Runnable(){
            @Override
            public void run() {
                mHandler.postDelayed(update, tint(am));
            }
        };
        mHandler.removeCallbacks(update);
        mHandler.postDelayed(update, mDelay);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mObserver != null)resolver.unregisterContentObserver(mObserver);
        update = null; // TODO: 需要进一步验证是否有效
    }
    // https://my.oschina.net/fengcunhan/blog/151398

    private class MeSettingsObserver extends ContentObserver {
        public MeSettingsObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mDelay = Settings.System.getInt(resolver, "sb_check_delay", 3000);
            mDefaultColor = Settings.System.getInt(resolver, "sb_default_color", 0X4D000000);
        }
    }
}

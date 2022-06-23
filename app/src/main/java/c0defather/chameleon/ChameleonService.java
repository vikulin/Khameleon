package c0defather.chameleon;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by Vadym Vikulin on 6/23/22.
 */
public class ChameleonService extends Service {

    private WindowManager.LayoutParams topParams;
    private WindowManager windowManager;
    private RelativeLayout topView;
    private View topGrab;

    public static boolean isRunning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        isRunning = true;
        initScreenUtils();
        initViews();
        initOnClicks();
        initOnTouches();
    }

    private void initViews() {
        topView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.top, null);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        topGrab = topView.findViewById(R.id.grab);
        topParams = new WindowManager.LayoutParams(
                ScreenUtils.width,
                ScreenUtils.convertDpToPx(ChameleonService.this, 50),
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        topParams.x = 0;
        topParams.y = 0;
        topParams.gravity = Gravity.TOP | Gravity.RIGHT;
        windowManager.addView(topView, topParams);
    }

    private void initScreenUtils() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        ScreenUtils.width = metrics.widthPixels;
        ScreenUtils.height = metrics.heightPixels - statusBarHeight;
    }

    private void initOnClicks() {
        topView.findViewById(R.id.webButton).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                stopSelf();
                return true;
            }
        });
    }

    private void initOnTouches() {

        topGrab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        topParams.y = Math.max((int) motionEvent.getRawY(), ScreenUtils.convertDpToPx(ChameleonService.this, 50));
                        windowManager.updateViewLayout(topView, topParams);
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (topView != null) windowManager.removeView(topView);
    }
}

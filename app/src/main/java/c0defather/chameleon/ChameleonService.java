package c0defather.chameleon;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by c0defather on 3/29/18.
 */

public class ChameleonService extends Service {

    public static boolean isRunning;

    private WindowManager.LayoutParams topParams;
    private WindowManager.LayoutParams edgeParams;
    private RelativeLayout topView;
    private View topGrab;
    private View edge;
    private WindowManager windowManager;
    private GestureDetectorCompat gestureDetector;

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

        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (topParams.width == 0) {
                    topParams.width = ScreenUtils.width;
                    topView.setVisibility(View.VISIBLE);
                    windowManager.updateViewLayout(topView, topParams);
                } else {
                    topParams.width = 0;
                    windowManager.updateViewLayout(topView, topParams);
                    topView.setVisibility(View.GONE);
                }
                return true;
            }
        });
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
                ScreenUtils.height/2,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        topParams.x = 0;
        topParams.y = 0;
        topParams.gravity = Gravity.TOP | Gravity.RIGHT;
        windowManager.addView(topView, topParams);


        edge = new View(getApplicationContext());
        edgeParams = new WindowManager.LayoutParams(
                ScreenUtils.width/20,
                ScreenUtils.height,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        edgeParams.gravity = Gravity.RIGHT;
        windowManager.addView(edge, edgeParams);
    }

    private void initScreenUtils() {
        final Display display = windowManager.getDefaultDisplay();
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ScreenUtils.width = display.getWidth();
        ScreenUtils.height = display.getHeight() - statusBarHeight;
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

        edge.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
        topGrab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        topParams.height = Math.max((int) motionEvent.getRawY(), ScreenUtils.convertDpToPx(ChameleonService.this, 50));
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
        if (edge != null) windowManager.removeView(edge);
    }
}

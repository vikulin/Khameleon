package org.rivchain.overlay_app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.View.OnTouchListener
import android.widget.RelativeLayout

/**
 * Created by Vadym Vikulin on 6/23/22.
 */
class OverlayService : Service() {
    private var topParams: WindowManager.LayoutParams? = null
    private var windowManager: WindowManager? = null
    private var topView: RelativeLayout? = null
    private var topGrab: View? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        isRunning = true
        initScreenUtils()
        initViews()
        initOnClicks()
        initOnTouches()
    }

    private fun initViews() {
        topView = LayoutInflater.from(this).inflate(R.layout.top, null) as RelativeLayout
        val LAYOUT_FLAG: Int
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        topGrab = topView!!.findViewById(R.id.grab)
        topParams = WindowManager.LayoutParams(
            ScreenUtils.width,
            ScreenUtils.convertDpToPx(this@OverlayService, 50),
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        topParams!!.x = 0
        topParams!!.y = 0
        topParams!!.gravity = Gravity.TOP or Gravity.RIGHT
        windowManager!!.addView(topView, topParams)
    }

    private fun initScreenUtils() {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        val metrics = this.resources.displayMetrics
        ScreenUtils.width = metrics.widthPixels
        ScreenUtils.height = metrics.heightPixels - statusBarHeight
    }

    private fun initOnClicks() {
        topView!!.findViewById<View>(R.id.webButton).setOnLongClickListener {
            stopSelf()
            true
        }
    }

    private fun initOnTouches() {
        topGrab!!.setOnTouchListener(OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> return@OnTouchListener true
                MotionEvent.ACTION_MOVE -> {
                    topParams!!.y = Math.max(
                        motionEvent.rawY.toInt(),
                        ScreenUtils.convertDpToPx(this@OverlayService, 50)
                    )
                    windowManager!!.updateViewLayout(topView, topParams)
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_UP -> return@OnTouchListener true
            }
            true
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (topView != null) windowManager!!.removeView(topView)
    }

    companion object {
        var isRunning = false
    }
}
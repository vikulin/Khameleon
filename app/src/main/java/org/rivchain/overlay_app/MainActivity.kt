package org.rivchain.overlay_app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Vadym Vikulin on 6/23/22.
 */
class MainActivity : AppCompatActivity() {
    private var chameleon: ImageButton? = null
    private var service: Intent? = null
    private var foreground = false
    private val statusHandler = Handler()
    private val statusChecker: Runnable = object : Runnable {
        override fun run() {
            if (foreground) chameleon!!.setImageResource(if (OverlayService.isRunning) R.mipmap.chameleon_on else R.mipmap.chameleon_off)
            statusHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        service = Intent(this, OverlayService::class.java)
        service!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION)
        } else {
            initializeView()
            changeStatus(true)
            finish()
        }
    }

    /**
     * Set and initialize the view elements.
     */
    private fun initializeView() {
        chameleon = findViewById<View>(R.id.chameleon) as ImageButton
        chameleon!!.setOnClickListener { view: View? ->
            changeStatus(
                !OverlayService.isRunning
            )
        }
        statusHandler.post(statusChecker)
    }

    private fun changeStatus(status: Boolean) {
        chameleon!!.setImageResource(if (status) R.mipmap.chameleon_on else R.mipmap.chameleon_off)
        if (status) {
            startService(service)
        } else {
            stopService(service)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            // Settings activity never returns proper value so instead check with following method
            if (Settings.canDrawOverlays(this)) {
                initializeView()
                changeStatus(true)
                finish()
            } else { //Permission is not available
                Toast.makeText(
                    this,
                    "Draw over other app permission not available. Closing the application",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    public override fun onResume() {
        super.onResume()
        foreground = true
    }

    public override fun onPause() {
        super.onPause()
        foreground = false
    }

    companion object {
        private const val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 1404
    }
}
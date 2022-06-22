package c0defather.chameleon;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Toast;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by c0defather on 3/29/18.
 */

public class MainActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 1404;
    private ImageButton chameleon;
    private Intent service;
    private boolean foreground = false;
    private final Handler statusHandler = new Handler();
    private final Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            if (foreground)
                chameleon.setImageResource(ChameleonService.isRunning ? R.mipmap.chameleon_on : R.mipmap.chameleon_off);
            statusHandler.postDelayed(this, 500);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = new Intent(this, ChameleonService.class);
        service.setFlags(FLAG_ACTIVITY_NEW_TASK);

        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));

            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {

            initializeView();
            changeStatus(true);
            finish();
        }
    }

    /**
     * Set and initialize the view elements.
     */
    private void initializeView() {
        chameleon = (ImageButton) findViewById(R.id.chameleon);
        chameleon.setOnClickListener(view -> changeStatus(!ChameleonService.isRunning));
        statusHandler.post(statusChecker);
    }

    private void changeStatus(boolean status) {
        chameleon.setImageResource(status? R.mipmap.chameleon_on : R.mipmap.chameleon_off);
        if (status) {
            startService(service);
        } else {
            stopService(service);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            // Settings activity never returns proper value so instead check with following method
            if (Settings.canDrawOverlays(this)) {
                initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        foreground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        foreground = false;
    }
}

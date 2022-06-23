package org.rivchain;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by Vadym Vikulin on 6/23/22.
 */
public class ScreenUtils {
    public static int height;
    public static int width;
    public static int convertDpToPx(Context context, int dp){
        return Math.round(dp*(context.getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }
}

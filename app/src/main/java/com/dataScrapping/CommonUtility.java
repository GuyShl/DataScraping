package com.dataScrapping;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.view.View;

import com.datScrapping.R;

public class CommonUtility {
    public static ProgressDialog progress;
    public static SharedPreferences sharedPreferences;

    public static void setCurrentUrl(Context context, String currentUrl) {
        if (sharedPreferences == null) {
            initializeSharedPreference(context);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.kKeyForCurrentUrl, currentUrl);
        editor.commit();
    }

    public static String getCurrentUrl(Context context) {
        if (sharedPreferences == null) {
            initializeSharedPreference(context);
        }
        return sharedPreferences.getString(Constants.kKeyForCurrentUrl, "");
    }

    public static void initializeSharedPreference(Context context) {

        CommonUtility.sharedPreferences = context.getSharedPreferences(null, Context.MODE_PRIVATE);

    }

    public static void showProgressDailog(Context context)

    {
        if(context!=null)
        {
            if (progress == null) {
                progress = new ProgressDialog(context);
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
            }
        }


    }

    public static void hideProgressDailog(Activity activity) {
        if (progress != null && progress.isShowing() && !activity.isFinishing()) {
            progress.dismiss();
            progress = null;
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
    public static void showSimpleDialog(Context context,String message) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.Ok_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });
        builder.create().show();
    }

}




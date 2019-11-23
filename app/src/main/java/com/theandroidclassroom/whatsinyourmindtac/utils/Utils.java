package com.theandroidclassroom.whatsinyourmindtac.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theandroidclassroom.whatsinyourmindtac.R;
import com.theandroidclassroom.whatsinyourmindtac.infrastructure.MyApplication;

public class Utils {

    public static boolean isNetworkAvailable(){
        boolean isNetworkAvailable = false;
        ConnectivityManager manager = (ConnectivityManager) MyApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()){
            isNetworkAvailable = true;
        }
        return isNetworkAvailable;
    }

    public static void showSnackbar(View parentView,String message){
        Snackbar snackbar = Snackbar.make(parentView,message,Snackbar.LENGTH_LONG);
        ViewGroup  viewGroup = (ViewGroup)snackbar.getView();
        viewGroup.setBackgroundColor(ContextCompat.getColor(MyApplication.getContext(),R.color.colorAccent));
        snackbar.show();
    }


    public static AlertDialog getAlertDialog(Activity activity, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.layout_for_pd,null,false);
        TextView messageTv = view.findViewById(R.id.message);
        messageTv.setText(message);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        return dialog;
    }


}

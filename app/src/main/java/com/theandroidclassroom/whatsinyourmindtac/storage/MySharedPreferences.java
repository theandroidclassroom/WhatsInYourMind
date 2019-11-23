package com.theandroidclassroom.whatsinyourmindtac.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {
    private static Context mContext;
    private static MySharedPreferences mInstance;
    private static SharedPreferences mPrefs;
    private static String PREFS_NAME = "WYM";

   public MySharedPreferences (){
       mPrefs = mContext.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
   }

   public static MySharedPreferences getInstance(Context context){
       mContext = context;
       if (mInstance==null){
           mInstance = new MySharedPreferences();
       }
       return mInstance;
   }

   public void setLogin(String value){
       mPrefs.edit().putString("login",value).apply();
   }

   public String getLogin(){
       String login = mPrefs.getString("login","");
       return login;
   }

   public void setUserID(String value){
       mPrefs.edit().putString("user_id",value).apply();
   }
   public String getUserID(){
       return mPrefs.getString("user_id","");
   }

   public void setUserData(String key,String value){
       mPrefs.edit().putString(key,value).apply();
   }
   public String getUserData(String key){
       return  mPrefs.getString(key,"");
   }
}

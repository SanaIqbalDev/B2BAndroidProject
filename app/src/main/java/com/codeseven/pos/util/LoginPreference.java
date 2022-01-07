package com.codeseven.pos.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.codeseven.pos.MainApplication;

public class LoginPreference {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public LoginPreference() {
        this.sharedPreferences = MainApplication.getContext().getSharedPreferences("24SevenLoginPreference",Context.MODE_PRIVATE) ;
        this.editor = sharedPreferences.edit();
    }


    public void AddLoginPreferences(String key, String value)
    {
        editor.putString(key,value).commit();
    }

    public String GetLoginPreference(String key){
        return sharedPreferences.getString(key,"");
    }
    public void SetKeepLoggedIn(Boolean istrue)
    {
        editor.putBoolean("keep_logged_in",istrue).commit();
    }

    public Boolean getKeepLoggedIn()
    {
        return sharedPreferences.getBoolean("keep_logged_in",false);
    }


}

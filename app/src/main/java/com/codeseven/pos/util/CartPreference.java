package com.codeseven.pos.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.codeseven.pos.MainApplication;

public class CartPreference {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public CartPreference() {
        this.sharedPreferences = MainApplication.getContext().getSharedPreferences("24SevenCartPreference",Context.MODE_PRIVATE) ;
        this.editor = sharedPreferences.edit();
    }


    public void AddCartId(String key, String value)
    {
        editor.putString(key,value).commit();
    }

    public String GetCartId(String key){
        return sharedPreferences.getString(key,"");
    }

}

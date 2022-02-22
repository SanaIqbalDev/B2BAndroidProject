package com.codeseven.pos.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.codeseven.pos.MainApplication;

public class OrderPreference {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public OrderPreference() {
        this.sharedPreferences = MainApplication.getContext().getSharedPreferences("24SevenOrderPreference",Context.MODE_PRIVATE) ;
        this.editor = sharedPreferences.edit();
    }


    public void AddOrderId(String value)
    {
        editor.putString("order_id",value).commit();
    }

    public String GetOrderId(){
        return sharedPreferences.getString("order_id","");
    }
}

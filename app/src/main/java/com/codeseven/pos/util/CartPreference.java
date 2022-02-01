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



    public void AddRemainingWalletAmount(String value){
        editor.putString("remaining_wallet_amount",value).commit();
    }
    public String GetRemainingWalletAmount(){
        return sharedPreferences.getString("remaining_wallet_amount","");
    }

    public void AddNeedToPay(String value)
    {
        editor.putString("need_to_pay",value).commit();
    }
    public String GetNeedToPay(){
        return sharedPreferences.getString("need_to_pay","");
    }

    public void AddOrderTotal(String value){
        editor.putString("order_total",value).commit();
    }
    public String GetOrderTotal(){
        return sharedPreferences.getString("order_total","");

    }
    public void SetCartWalletAmount(String val){
        editor.putString("cart_wallet_amount",val);
    }
    public String GetCartWalletAmount(){
        return sharedPreferences.getString("cart_wallet_amount","");
    }


}

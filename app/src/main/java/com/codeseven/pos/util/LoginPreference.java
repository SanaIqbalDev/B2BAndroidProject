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

    public void IsFirstTimePreference(Boolean value){
        editor.putBoolean("isFirst",value).commit();
    }
    public Boolean GetFirstTimePreference(){
        return sharedPreferences.getBoolean("isFirst", true);
    }
    public void SetCategoryLastItem(String item){
        editor.putString("lastitem",item).commit();
    }

    public String GetCategoryLastItem(){
        return sharedPreferences.getString("lastitem","2");
    }

    public void setLastPageItemsCount(int itemsCount){
        editor.putInt("itemcount",itemsCount).commit();
    }

    public int GetLastpageItemCount(){
        return sharedPreferences.getInt("itemcount",0);
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

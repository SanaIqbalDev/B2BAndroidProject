package com.codeseven.pos.ui;

import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.ActivityNewBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BaseActivity extends AppCompatActivity {

    ActivityNewBinding activityNewBinding;
    NavController navController;
    AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //Added Databinding
        activityNewBinding = DataBindingUtil.setContentView(this,R.layout.activity_new);

        //Adding Navigation Graph Implementation...
//        navController = Navigation.findNavController(this,R.id.fragments_container);
    }

}
package com.codeseven.pos.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
    TextView textCartItemCount;
    public static int mCartItemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //Added Databinding
        activityNewBinding = DataBindingUtil.setContentView(this, R.layout.activity_new);


        //Adding Navigation Graph Implementation...
//        navController = Navigation.findNavController(this,R.id.fragments_container);
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.search_menu_catalog, menu);
//
//        // Associate searchable configuration with the SearchView
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.search).getActionView();
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(getComponentName()));
//
//        return true;
//    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.cart_menu, menu);
//
//        final MenuItem menuItem = menu.findItem(R.id.menu_cart);
//
//        View actionView = menuItem.getActionView();
//        textCartItemCount = (TextView) actionView.findViewById(R.id.cartItemCount);
//
//        setupCartBadge();
//
//        actionView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onOptionsItemSelected(menuItem);
//            }
//        });
//
//        return true;
//    }
//
//    private void setupCartBadge() {
//
//        if (textCartItemCount != null) {
//            if (mCartItemCount == 0) {
//                if (textCartItemCount.getVisibility() != View.GONE) {
//                    textCartItemCount.setVisibility(View.GONE);
//                }
//            } else {
//                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
//                if (textCartItemCount.getVisibility() != View.VISIBLE) {
//                    textCartItemCount.setVisibility(View.VISIBLE);
//                }
//            }
//        }
//    }

}
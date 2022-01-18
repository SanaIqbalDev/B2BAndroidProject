package com.codeseven.pos.util;

import android.view.View;

import com.codeseven.pos.model.CatalogItem;

public interface CartItemClickListener {
    public void onItemCLicked(View view, CatalogItem catalogItem);
}

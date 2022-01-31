package com.codeseven.pos.util;

import android.view.View;

import com.codeseven.pos.model.CatalogItem;

public interface AddressItemClickListener {
    public void onItemClicked(View view, boolean isAddNew, int pos);
}

package com.codeseven.pos.util;

import com.codeseven.pos.model.AddressItem;

public interface AddressUpdateListener {
    public void onAddressUpdates(Integer id, AddressItem item);
}

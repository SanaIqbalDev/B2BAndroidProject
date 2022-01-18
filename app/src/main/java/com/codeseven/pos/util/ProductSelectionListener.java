package com.codeseven.pos.util;

import apollo.pos.GetAutocompleteResultsQuery;

public interface ProductSelectionListener {
    public default void onProductClicked(GetAutocompleteResultsQuery.Item item) {

    }
}

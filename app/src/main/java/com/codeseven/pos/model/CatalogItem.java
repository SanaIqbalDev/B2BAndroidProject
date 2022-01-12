package com.codeseven.pos.model;

import android.os.Parcel;
import android.os.Parcelable;


public class CatalogItem implements Parcelable {

    private String itemSku;
    private String itemName;
    private String itemPrice;
    private String itemImage;
    private String itemDescription;
    private String itemQuantity;
    private String itemUid;

    public CatalogItem(String itemSku, String itemName, String itemPrice, String itemImage, String description) {
        this.itemSku = itemSku;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;
        this.itemDescription = description;
    }
    public CatalogItem(String itemSku, String itemName, String itemPrice, String itemImage, String quantity, String uid) {
        this.itemSku = itemSku;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;
        this.itemQuantity = quantity;
        this.itemUid = uid;

    }

    protected CatalogItem(Parcel in) {
        itemSku = in.readString();
        itemName = in.readString();
        itemPrice = in.readString();
        itemImage = in.readString();
        itemDescription = in.readString();
    }

    public static final Creator<CatalogItem> CREATOR = new Creator<CatalogItem>() {
        @Override
        public CatalogItem createFromParcel(Parcel in) {
            return new CatalogItem(in);
        }

        @Override
        public CatalogItem[] newArray(int size) {
            return new CatalogItem[size];
        }
    };

    public String getItemSku() {
        return itemSku;
    }

    public void setItemSku(String itemSku) {
        this.itemSku = itemSku;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getItemUid() {
        return itemUid;
    }

    public void setItemUid(String itemUid) {
        this.itemUid = itemUid;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(itemSku);
        parcel.writeString(itemName);
        parcel.writeString(itemPrice);
        parcel.writeString(itemImage);
        parcel.writeString(itemDescription);
    }
}

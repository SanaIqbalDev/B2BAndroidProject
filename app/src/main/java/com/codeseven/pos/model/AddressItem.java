package com.codeseven.pos.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AddressItem implements Parcelable {
    private String firstName;
    private String lastName;
    private String city;
    private String country;
    private String address;
    private String address_optional;
    private String telephone;

    public AddressItem(String firstName, String lastName, String city, String country, String address, String address_optional, String telephone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.country = country;
        this.address = address;
        this.address_optional = address_optional;
        this.telephone = telephone;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress_optional() {
        return address_optional;
    }

    public void setAddress_optional(String address_optional) {
        this.address_optional = address_optional;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}


package com.sina.book.data;

import android.os.Parcel;
import android.os.Parcelable;

public class PaymentMonthPurchased implements Parcelable {
    
    private int payId;
    private String payOpen;
    
    public PaymentMonthPurchased() {
        
    }
    
    public PaymentMonthPurchased(Parcel source) {
        payId = source.readInt();
        payOpen = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(payId);
        dest.writeString(payOpen);
    }
    
    public int getPayId() {
        return payId;
    }
    
    public void setPayId(int payId) {
        this.payId = payId;
    }
    
    public String getPayOpen() {
        return payOpen;
    }
    
    public void setPayOpen(String payOpen) {
        this.payOpen = payOpen;
    }
    
    public static final Parcelable.Creator<PaymentMonthPurchased> CREATOR = new Parcelable.Creator<PaymentMonthPurchased>(){
        public PaymentMonthPurchased createFromParcel(Parcel source){
            return new PaymentMonthPurchased(source);
        }
        public PaymentMonthPurchased[] newArray(int size){
            return new PaymentMonthPurchased[size];
        }
    };
    public static final Parcelable.Creator<PaymentMonthPurchased> getCreator(){
        return CREATOR;
    }
    
}
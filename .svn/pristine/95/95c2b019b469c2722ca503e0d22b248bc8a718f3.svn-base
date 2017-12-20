package com.sina.book.data;


import android.os.Parcel;
import android.os.Parcelable;

public class PaymentMonthDetail implements Parcelable {
    
    
    private String payId;
    private String payType;
    private String payOpen;
    private String payDetail;
    private long beginTime;
    private long endTime;
    
    public PaymentMonthDetail() {
        
    }
    
    public PaymentMonthDetail(Parcel source) {
        payId = source.readString();
        payType = source.readString();
        payOpen = source.readString();
        payDetail = source.readString();
        beginTime = source.readLong();
        endTime = source.readLong();
    }
    
    public String getPayId() {
        return payId;
    }
    
    public void setPayId(String payId) {
        this.payId = payId;
    }
    
    public String getPayType() {
        return payType;
    }
    
    public void setPayType(String payType) {
        this.payType = payType;
    }
    
    public String getPayOpen() {
        return payOpen;
    }
    
    public void setPayOpen(String payOpen) {
        this.payOpen = payOpen;
    }
    
    public String getPayDetail() {
        return payDetail;
    }
    
    public void setPayDetail(String payDetail) {
        this.payDetail = payDetail;
    }
    
    public long getBeginTime() {
        return beginTime;
    }
    
    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(payId);
        dest.writeString(payType);
        dest.writeString(payOpen);
        dest.writeString(payDetail);   
        dest.writeLong(beginTime);
        dest.writeLong(endTime);
    }
    
    public static final Parcelable.Creator<PaymentMonthDetail> CREATOR = new Parcelable.Creator<PaymentMonthDetail>(){
        public PaymentMonthDetail createFromParcel(Parcel source){
            return new PaymentMonthDetail(source);
        }
        public PaymentMonthDetail[] newArray(int size){
            return new PaymentMonthDetail[size];
        }
    };
    public static final Parcelable.Creator<PaymentMonthDetail> getCreator(){
        return CREATOR;
    }
    
}
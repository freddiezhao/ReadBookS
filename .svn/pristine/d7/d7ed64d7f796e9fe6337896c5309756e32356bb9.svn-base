package com.sina.book.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.sina.book.util.Util;

public class PaymentMonthMine implements Parcelable {

    private int payId;
    private String payType;
    private String payOpen;
    private String beginTime;
    private String endTime;
    private String timeRemain;
    
    public PaymentMonthMine() {

    }

    public PaymentMonthMine(Parcel source) {
        payId = source.readInt();
        payType = source.readString();
        payOpen = source.readString();
        beginTime = source.readString();
        endTime = source.readString();
        timeRemain = source.readString();
    }

    public int getPayId() {
        return payId;
    }

    public void setPayId(int payId) {
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

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public String getTimeRemain() {
        return timeRemain;
    }
    
    public void setTimeRemain(String timeRemain) {
        this.timeRemain = timeRemain;
    }
    
    public boolean isLocalVaild() {
        try {
            long cur = System.currentTimeMillis();
            if (!Util.isNullOrEmpty(beginTime) && !Util.isNullOrEmpty(endTime)) {
                int lengthDif = String.valueOf(cur).length()
                        - beginTime.length();
                if (lengthDif != 0) {                   
                    while (lengthDif > 0) {
                        beginTime = beginTime + "0";
                        endTime = endTime + "0";
                        lengthDif--;
                    }
                }
                long begin = Long.parseLong(beginTime);
                long end = Long.parseLong(endTime);

                if (cur >= begin && cur <= end) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(payId);
        dest.writeString(payType);
        dest.writeString(payOpen);
        dest.writeString(beginTime);
        dest.writeString(endTime);
        dest.writeString(timeRemain);
    }

    public static final Parcelable.Creator<PaymentMonthMine> CREATOR = new Parcelable.Creator<PaymentMonthMine>() {
        public PaymentMonthMine createFromParcel(Parcel source) {
            return new PaymentMonthMine(source);
        }

        public PaymentMonthMine[] newArray(int size) {
            return new PaymentMonthMine[size];
        }
    };

    public static final Parcelable.Creator<PaymentMonthMine> getCreator() {
        return CREATOR;
    }
    
    @Override
    public String toString() {
        return "PaymentMonth [payId=" + payId + ", payType=" + payType
                + ", payOpen=" + payOpen + ", beginTime=" + beginTime
                + ", endTime=" + endTime + ", timeRemain=" + timeRemain +"]";
    }
}
package com.sina.book.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * 充值选择金额和充值方式的实体类
 * 
 * @author Tsimle
 * 
 */
public class RechargeBean {
    
    /** 移动手机话费类型. */
    public static final String PHONE_TYPE = "5";
    
    private int userVb;
    private ArrayList<Amount> amounts = new ArrayList<Amount>();
    private ArrayList<PayType> payTypes = new ArrayList<PayType>();

    public int getUserVb() {
        return userVb;
    }

    public void setUserVb(int userVb) {
        this.userVb = userVb;
    }

    public void addAmount(Amount amount) {
        amounts.add(amount);
    }

    public void addPayType(PayType payType) {
        payTypes.add(payType);
    }
    
    public void setPayTypes(ArrayList<PayType> types) {
        if (null == types) {
            return;
        }
        if (null == payTypes) {
            payTypes = new ArrayList<RechargeBean.PayType>();
        }
        payTypes.addAll(types);
    }

    public ArrayList<Amount> getAmounts() {
        return amounts;
    }

    public ArrayList<PayType> getPayTypes() {
        return payTypes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (amounts != null) {
            sb.append("[amounts]:{");
            for (Amount amount : amounts) {
                sb.append("\n");
                sb.append("money:").append(amount.money);
                sb.append("moneyFen:").append(amount.moneyFen);
            }
            sb.append("}");
        }

        if (payTypes != null) {
            sb.append("[payTypes]:{");
            for (PayType paytype : payTypes) {
                sb.append("\n");
                sb.append("type:").append(paytype.type);
                sb.append("desc:").append(paytype.desc);
                sb.append("maxMoneyFen:").append(paytype.maxMoneyFen);
            }
            sb.append("}");
        }
        return sb.toString();
    }

    public static class Amount implements Parcelable{
        public int moneyFen;
        public int money;
        
        public static final Parcelable.Creator<Amount> CREATOR = new Parcelable.Creator<Amount>() {

            public Amount createFromParcel(Parcel source) {
                return new Amount(source);
            }

            public Amount[] newArray(int size) {
                return new Amount[size];
            }

        };
        
        public Amount() {
        }
        
        public Amount(Parcel source) {
            moneyFen = source.readInt();
            money = source.readInt();
        }
        
        @Override
        public int describeContents() {
            return 0;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(moneyFen);
            dest.writeInt(money);
        }
    }

    public static class PayType implements Parcelable {
        public String type;
        public String desc;
        public int maxMoneyFen;
        public int maxMoney;

        public static final Parcelable.Creator<PayType> CREATOR = new Parcelable.Creator<PayType>() {

            public PayType createFromParcel(Parcel source) {
                return new PayType(source);
            }

            public PayType[] newArray(int size) {
                return new PayType[size];
            }

        };

        public PayType() {
        }

        public PayType(Parcel source) {
            type = source.readString();
            desc = source.readString();
            maxMoneyFen = source.readInt();
            maxMoney = source.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(type);
            dest.writeString(desc);
            dest.writeInt(maxMoneyFen);
            dest.writeInt(maxMoney);
        }
    }
}

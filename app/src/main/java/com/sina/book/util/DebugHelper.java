package com.sina.book.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 时间debug类
 * 
 * @author Tsimle
 * 
 */
public class DebugHelper {
    private static final String TAG = "DebugHelper";
    private String debugName;
    private long lastTime = 0;
    private long curTime = 0;
    private long startTime = 0;
    private long endTime = 0;

    private HashMap<String, Long> mTimes = new HashMap<String, Long>();

    public void start(String name) {
        if (!LogUtil.gLogFlag) {
            return;
        }
        reset();
        debugName = name;
        startTime = System.currentTimeMillis();
        curTime = startTime;
    }

    public void mark(String name) {
        if (!LogUtil.gLogFlag) {
            return;
        }
        lastTime = curTime;
        curTime = System.currentTimeMillis();
        long interval = curTime - lastTime;
        mTimes.put(name, interval);
    }

    public void end() {
        if (!LogUtil.gLogFlag) {
            return;
        }
        endTime = System.currentTimeMillis();
        long interval = endTime - startTime;
        mTimes.put("total", interval);
        print();
    }

    private void reset() {
        startTime = 0;
        endTime = 0;
        lastTime = 0;
        curTime = 0;
        debugName = "";
        mTimes.clear();
    }

    private void print() {
        LogUtil.i(TAG, getLogString());
    }

    private String getLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append(debugName + " Time Debuger [\n");
        for (DebugProfile p : getProfile()) {
            sb.append("TAG: ");
            sb.append(p.tag);
            sb.append("\t INC: ");
            sb.append(p.inc);
            sb.append("\t INCP: ");
            sb.append(p.incPercent);
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private DebugProfile[] getProfile() {
        DebugProfile[] profile = new DebugProfile[mTimes.size()];

        long total = mTimes.get("total");

        int i = 0;
        for (String key : mTimes.keySet()) {
            long time = mTimes.get(key);
            profile[i] = new DebugProfile(key, time, time / (total * 1.0));
            i++;
        }
        try {
            Arrays.sort(profile);
        } catch (NullPointerException e) {
        }
        return profile;
    }
}

class DebugProfile implements Comparable<DebugProfile> {
    private static NumberFormat percent = NumberFormat.getPercentInstance();

    public String tag;
    public long inc;
    public String incPercent;

    public DebugProfile(String tag, long inc, double incPercent) {
        this.tag = tag;
        this.inc = inc;

        percent = new DecimalFormat("0.00#%");
        this.incPercent = percent.format(incPercent);
    }

    @Override
    public int compareTo(DebugProfile o) {
        return (int) (o.inc - this.inc);
    }

}

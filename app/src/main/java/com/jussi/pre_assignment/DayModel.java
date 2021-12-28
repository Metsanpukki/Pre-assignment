package com.jussi.pre_assignment;

import java.util.Vector;

public class DayModel {
    private Vector<Long> mTimestamps;
    private Vector<Double> mPrices;
    private Vector<Double> mMarketCaps;
    private Vector<Double> mTotalVolumes;


    public DayModel() {
        mTimestamps = new Vector<>();
        mPrices = new Vector<>();
        mMarketCaps = new Vector<>();
        mTotalVolumes = new Vector<>();
    }

    public void addData(long timestamp, double price, double marketCap, double totalVolume) {
        mTimestamps.add(timestamp);
        mPrices.add(price);
        mMarketCaps.add(marketCap);
        mTotalVolumes.add(totalVolume);

    }


    public Vector<Long> getTimestamps() {
        return mTimestamps;
    }

    public Vector<Double> getPrices() {
        return mPrices;
    }

    public Vector<Double> getTotalVolumes() {
        return mTotalVolumes;
    }

    public Vector<Double> getMarketCaps() {
        return mMarketCaps;
    }

}

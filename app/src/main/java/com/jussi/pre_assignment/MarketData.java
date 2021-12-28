package com.jussi.pre_assignment;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Vector;

public class MarketData {
    private TimeZone mTimeZone;
    private Vector<DayModel> mDayModels;

    public MarketData(String json, TimeZone timeZone) throws JSONException {

        mDayModels = new Vector<>();

        mTimeZone = timeZone;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
        simpleDateFormat.setTimeZone(mTimeZone);


        JSONObject jsonObject = new JSONObject(json);
        JSONArray prices = jsonObject.getJSONArray("prices");
        JSONArray marketCaps = jsonObject.getJSONArray("market_caps");
        JSONArray totalVolumes = jsonObject.getJSONArray("total_volumes");

        long timestamp = prices.getJSONArray(0).getLong(0);
        double price = prices.getJSONArray(0).getDouble(1);
        double marketCap = marketCaps.getJSONArray(0).getDouble(1);
        double totalVolume = totalVolumes.getJSONArray((0)).getDouble(1);

        DayModel dm = new DayModel();
        dm.addData(timestamp, price, marketCap, totalVolume);
        mDayModels.add(dm);

        for (int i = 1; i < prices.length(); i++) {

            timestamp = prices.getJSONArray(i).getLong(0);
            price = prices.getJSONArray(i).getDouble(1);
            marketCap = marketCaps.getJSONArray(i).getDouble(1);
            totalVolume = totalVolumes.getJSONArray((i)).getDouble(1);

            if (getDayFromTimestamp(mDayModels.lastElement().getTimestamps().lastElement()) == getDayFromTimestamp(timestamp)) {
                mDayModels.lastElement().addData(timestamp, price, marketCap, totalVolume);
            } else {
                dm = new DayModel();
                dm.addData(timestamp, price, marketCap, totalVolume);
                mDayModels.add(dm);
            }

        }


    }

    public Vector<DayModel> getDayModels() {
        return mDayModels;
    }

    private int getDayFromTimestamp(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
        simpleDateFormat.setTimeZone(mTimeZone);
        return Integer.parseInt(simpleDateFormat.format(timestamp));
    }


    public int getLongestBearishTrendInDays() {
        int count = 0, longest = 0;
        for (int i = 1; i < mDayModels.size(); i++) {
            double priceYesterday = mDayModels.get(i - 1).getPrices().firstElement();
            double priceToday = mDayModels.get(i).getPrices().firstElement();
            if (priceToday < priceYesterday) {
                count++;
            } else {
                count = 0;
            }

            if (count > longest) {
                longest = count;
            }

        }
        return longest;
    }


    public DayModel getHighestVolumeDayModel() {

        DayModel maxVolume = mDayModels.get(0);

        for (int i = 1; i < mDayModels.size(); i++) {
            double volume = mDayModels.get(i).getTotalVolumes().firstElement();

            if (volume > maxVolume.getTotalVolumes().firstElement()) {
                maxVolume = mDayModels.get(i);
            }
        }

        return maxVolume;

    }



}

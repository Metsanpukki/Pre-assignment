package com.jussi.pre_assignment;

import android.os.Bundle;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;


public class TimeMachineFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private EditText mEditTextStartDate;
    private EditText mEditTextEndDate;
    private Button mButton;
    private Date mStartDate;
    private Date mEndDate;
    private TextView mTextViewBestDaySell;
    private TextView mTextViewBestDayBuy;
    private DateFormat mDateFormatter;
    private TimeZone mTimeZone;


    public TimeMachineFragment() {

    }

    public static TimeMachineFragment newInstance(String param1, String param2) {
        TimeMachineFragment fragment = new TimeMachineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_machine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTimeZone = TimeZone.getTimeZone(getString(R.string.timezone));
        mDateFormatter = new SimpleDateFormat(getString(R.string.datepattern));
        mDateFormatter.setTimeZone(mTimeZone);
        mDateFormatter.setLenient(false);

        mEditTextStartDate = (EditText) view.findViewById(R.id.time_Machine_Fragment_EditText_StartDate);
        mEditTextEndDate = (EditText) view.findViewById(R.id.time_Machine_Fragment_EditText_EndDate);

        mTextViewBestDayBuy = (TextView) view.findViewById(R.id.time_Machine_Fragment_Textview_BuyDate);
        mTextViewBestDaySell = (TextView) view.findViewById(R.id.time_Machine_Fragment_Textview_SellDate);

        mButton = (Button) view.findViewById(R.id.time_Machine_Fragment_Button_TimeTravel);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mStartDate = mDateFormatter.parse(mEditTextStartDate.getText().toString());
                    mEndDate = mDateFormatter.parse(mEditTextEndDate.getText().toString());

                    new TimeMachineDataFetch().start();
                } catch (ParseException e) {
                    Toast.makeText(getActivity(), String.format("%s %s", getString(R.string.date_error_message), getString(R.string.datepattern)),
                            Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    class TimeMachineDataFetch extends Thread {
        private String mData = "";
        private MarketData mMarketData;

        @Override
        public void run() {
            try {
                String line;
                String urlStr = String.format(getResources().getString(R.string.apiurl),mStartDate.getTime() / 1000, (mEndDate.getTime() + 3600000) / 1000);
                URL url = new URL(urlStr);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                InputStream inputStream = httpsURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                while ((line = bufferedReader.readLine()) != null) {
                    mData += line;
                }

                if (!mData.isEmpty()) {
                    mMarketData = new MarketData(mData, mTimeZone);
                    calcBestProfit();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), String.format("%s %s", getString(R.string.date_error_message), getString(R.string.datepattern)),
                                Toast.LENGTH_LONG).show();
                    }
                });

                e.printStackTrace();
            }
        }

        private void calcBestProfit() {

            Vector<DayModel> dayModels = mMarketData.getDayModels();
            long buyTimestamp = 0;
            long sellTimestamp = 0;
            if (mMarketData.getDayModels().size() > 1) {
                double profit = 0;
                for (int i = 0; i < dayModels.size(); i++) {
                    double start = dayModels.get(i).getPrices().firstElement();
                    for (int j = i + 1; j < dayModels.size(); j++) {
                        double end = dayModels.get(j).getPrices().firstElement();
                        if ((end - start) > profit) {
                            profit = end - start;
                            buyTimestamp = dayModels.get(i).getTimestamps().firstElement();
                            sellTimestamp = dayModels.get(j).getTimestamps().firstElement();
                        }

                    }
                }

                long finalBuyTimestamp = buyTimestamp;
                long finalSellTimestamp = sellTimestamp;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.datepattern));
                        simpleDateFormat.setTimeZone(mTimeZone);
                        if (finalBuyTimestamp > 0) {
                            mTextViewBestDayBuy.setText(String.format("%s", simpleDateFormat.format(finalBuyTimestamp)));
                            mTextViewBestDaySell.setText(String.format("%s", simpleDateFormat.format(finalSellTimestamp)));
                        } else {
                            mTextViewBestDayBuy.setText(R.string.fragment_time_machine_no_profit_message);
                            mTextViewBestDaySell.setText(R.string.fragment_time_machine_no_profit_message);
                        }
                    }
                });

                //Debug
                //Log.d("PROFIT:", String.format("profit: %f  buyStamp: %d sellStamp: %d", profit, buyTimestamp, sellTimestamp));
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ELSE", "elsessä!");
                        mTextViewBestDayBuy.setText(R.string.fragment_time_machine_no_profit_message);
                        mTextViewBestDaySell.setText(R.string.fragment_time_machine_no_profit_message);
                    }
                });

            }

        }
    }
}
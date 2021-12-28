package com.jussi.pre_assignment;


import android.os.Bundle;

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

import javax.net.ssl.HttpsURLConnection;


public class HistoryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private EditText mEditTextStartDate;
    private EditText mEditTextEndDate;
    private TextView mTextviewLongestDownwardDays;
    private TextView mTextviewHighestVolumeValue;
    private TextView mTextviewHighestVolumeDate;
    private Button mButton;
    private Date mStartDate;
    private Date mEndDate;
    private DateFormat mDateFormatter;
    private TimeZone mTimeZone;

    public HistoryFragment() {
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTimeZone = TimeZone.getTimeZone(getString(R.string.timezone));
        mDateFormatter = new SimpleDateFormat(getString(R.string.datepattern));
        mDateFormatter.setTimeZone(mTimeZone);
        mDateFormatter.setLenient(false);

        mEditTextStartDate = (EditText) view.findViewById(R.id.historyFragment_EditText_StartDate);
        mEditTextEndDate = (EditText) view.findViewById(R.id.historyFragment_EditText_EndDate);
        mTextviewLongestDownwardDays = (TextView) view.findViewById((R.id.historyFragment_Textview_LongestDownwardDays));
        mTextviewHighestVolumeValue = (TextView) view.findViewById((R.id.historyFragment_Textview_HighestVolumeValue));
        mTextviewHighestVolumeDate = (TextView) view.findViewById((R.id.historyFragment_Textview_HighestVolumeDate));


        mButton = (Button) view.findViewById(R.id.historyFragment_Button_Analyze);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mStartDate = mDateFormatter.parse(mEditTextStartDate.getText().toString());
                    mEndDate = mDateFormatter.parse(mEditTextEndDate.getText().toString());

                    new DataFetch().start();
                } catch (ParseException e) {
                    Toast.makeText(getActivity(), String.format("%s %s", getString(R.string.date_error_message), getString(R.string.datepattern)),
                            Toast.LENGTH_LONG).show();

                }

            }
        });
    }


    class DataFetch extends Thread {
        private String mData = "";

        @Override
        public void run() {
            try {
                String line;
                String urlStr = String.format(getResources().getString(R.string.apiurl), mStartDate.getTime() / 1000, (mEndDate.getTime() + 3600000) / 1000);
                URL url = new URL(urlStr);


                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                InputStream inputStream = httpsURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                while ((line = bufferedReader.readLine()) != null) {
                    mData += line;
                }

                if (!mData.isEmpty()) {
                    MarketData md = new MarketData(mData, mTimeZone);
                    DayModel highestVolumeDayModel = md.getHighestVolumeDayModel();

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.datepattern));
                    simpleDateFormat.setTimeZone(mTimeZone);
                    String dateString = simpleDateFormat.format(highestVolumeDayModel.getTimestamps().firstElement());

                    mTextviewLongestDownwardDays.setText(String.format("%s", md.getLongestBearishTrendInDays()));
                    mTextviewHighestVolumeValue.setText(String.format("%.2f %s", highestVolumeDayModel.getTotalVolumes().firstElement(), getString(R.string.currency)));
                    mTextviewHighestVolumeDate.setText(dateString);
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
    }
}
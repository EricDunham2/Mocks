package com.projects.mocks.fragments;


import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.projects.mocks.classes.ThreadParams;
import com.projects.mocks.classes.ThreadStock;
import com.projects.mocks.mocks.R;
import com.projects.mocks.mocks.databinding.FragmentDetailsBinding;
import java.util.Calendar;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    private LineChart chart;
    public Stock selectedStock;
    private TextView high;
    private TextView close;
    private TextView low;
    private TextView sym;
    private TextView company;
    private Thread updateThread;
    public String activeStockDetails;
    RadioGroup rgroup;
    Button invest;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        //leave this on top unless you're absolutely sure something needs to go above this
        super.onViewCreated(view, savedInstanceState);
        //used for back stacking and making sure the correct nav item is selected.
        chart = (LineChart)getView().findViewById(R.id.DetailsChart);
        setChartParams();
        chart.invalidate();
        chart.notifyDataSetChanged();
        high = (TextView) getView().findViewById(R.id.DetailsHigh);
        low = (TextView) getView().findViewById(R.id.DetailsLow);
        close = (TextView) getView().findViewById(R.id.DetailsValue);
        sym = (TextView) getView().findViewById(R.id.Symbol);
        company = (TextView) getView().findViewById(R.id.DetailsCompany);
        activeStockDetails = getArguments().get("selectedStock").toString();

        try {
            ThreadStock detailsThreadStock = new ThreadStock(activeStockDetails,"UPDATE_ONE",getContext());
            detailsThreadStock.df = this;
            Thread detailsThread = new Thread(detailsThreadStock);
            detailsThread.start();
        }catch (Exception e)
        {
            e.getMessage();
        }

        rgroup = (RadioGroup)getView().findViewById(R.id.rdogrp);
        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.rbtnDay:
                        setDateline("DAY");
                        break;
                    case R.id.rbtnWeek:
                        setDateline("WEEK");
                        break;
                    case R.id.rbtnMonth:
                        setDateline("MONTH");
                        break;
                    case R.id.rbtnYear:
                        setDateline("YEAR");
                        break;
                    case R.id.rbtnFiveYear:
                        setDateline("5YEAR");
                        break;
                }

                chart.invalidate();
                chart.notifyDataSetChanged();
            }
        });


        ThreadStock st = new ThreadStock("UPDATE_ONE",activeStockDetails);
        updateThread = new Thread(st);
        updateThread.start();

        setDateline("WEEK");

    }

    private void setChartParams()
    {
        chart.setBackgroundColor(Color.argb(255,21,21,21));
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getXAxis().setDrawLabels(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis = chart.getAxisRight();
        yAxis.setDrawGridLines(false);
        chart.getAxisLeft().setTextColor(Color.WHITE); // left y-axis
        chart.setContentDescription("");
        Description d = new Description();
        d.setText("");
        chart.setDescription(d);
    }

    private void setDateline(String timespan)
    {
        Calendar past = Calendar.getInstance();
        Calendar tdy = Calendar.getInstance();
        switch (timespan) {
            case "DAY"://Sunday, Monday, Happy Days,Tuesday, Wednesday, Happy Days,Thursday, Friday, Happy Days, The weekend comes, my cycle hums Ready to race to you
                past.add(Calendar.DAY_OF_MONTH, -1); //Days of stock data can be missing so number of points on graph can be off
                break;
            case "WEEK"://Sunday, Monday, Happy Days,Tuesday, Wednesday, Happy Days,Thursday, Friday, Happy Days, The weekend comes, my cycle hums Ready to race to you
                past.add(Calendar.DAY_OF_MONTH, -7); //Days of stock data can be missing so number of points on graph can be off
                break;
            case "MONTH":
                past.add(Calendar.MONTH, -1);
                break;
            case "YEAR":
                past.add(Calendar.YEAR, -1);
                break;
            case "5YEAR":
                past.add(Calendar.YEAR, -5);
                break;
        }
        ThreadStock st = new ThreadStock(activeStockDetails,"HISTORY", past, tdy, getView().getContext());

        Thread t = new Thread(st);
        t.start();
    }


}

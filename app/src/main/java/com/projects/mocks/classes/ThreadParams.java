package com.projects.mocks.classes;

import android.widget.ArrayAdapter;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;

import yahoofinance.Stock;

/**
 * Created by Eric on 11/19/2016.
 */
 //TODO Find a better way to pass by ref
public class ThreadParams
{
    public ArrayAdapter<Stock> adapter;
    public ArrayList<Stock> output;

    public ThreadParams( ArrayList<Stock> o, ArrayAdapter<Stock> a)
    {
        output = o;
        adapter = a;
    }
}
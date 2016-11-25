package com.projects.mocks.classes;

import android.content.Context;
import android.widget.ArrayAdapter;
import com.projects.mocks.fragments.DetailsFragment;
import java.util.ArrayList;
import java.util.Calendar;
import yahoofinance.Stock;

/**
 * Created by Eric on 11/19/2016.
 */
 //TODO Find a better way to pass by ref
public class ThreadParams
{
    public  String mth;
    public  Calendar from;
    public  Calendar to;
    public  String sym;
    public Context ctx;
    public  ArrayAdapter<Stock> adapter;
}
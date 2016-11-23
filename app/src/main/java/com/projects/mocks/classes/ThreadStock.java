package com.projects.mocks.classes;

/**
 * Created by Eric on 11/18/2016.
 */

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.projects.mocks.fragments.DetailsFragment;
import com.projects.mocks.fragments.MarketFragment;
import com.projects.mocks.mocks.MainActivity;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;
import com.projects.mocks.mocks.R;
import com.projects.mocks.mocks.databinding.FragmentDetailsBinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
//TODO: Create Custom constructor for each method, remove unused parts/clean up
public class ThreadStock implements Runnable {
    public  String mth;
    public  Calendar from;
    public  Calendar to;
    public  String sym;
    private Context ctx;
    public  ArrayAdapter<Stock> adapter;
    public  ArrayList<Stock> output;
    public DetailsFragment df;
    private Stock selectedStock;
    public int updateRangeTop;
    public int updateRangeLow;

    public ThreadStock(ThreadParams tp) //Multiple Stocks
    {
        ctx = tp.ctx;
        adapter = tp.adapter;
        output = tp.output;
        mth = tp.mth;
        from = tp.from;
        to = tp.to;
        sym = tp.sym;
        df = tp.df;
    }

    @Override
    public void run() {
        switch (mth) {
            case "UPDATE_MULTIPLE":
                updateMultiple();
                break;
            case "UPDATE_ONE":
                updateOne();
                break;
            case "ADD_SINGLE":
                addStockToPortfolio(sym);
                break;
            case "ADD_MULTIPLE":
                addMultipleStocksToListView();
                break;
            case "HISTORY":
                getStockHistory(from, to, sym);
                break;
        }
    }

    private Stock getStockDetails(String symbol)
    {
        Stock stock = new Stock(symbol);
        try {
            stock = YahooFinance.get(symbol);
        }
        catch (Exception e){
            e.getMessage();
        }
        return stock;
    }


    private void addMultipleStocksToListView()
    {
        MainActivity.addingMutex.lock();
        MarketFragment.midScrolling = true;
        try {
            for (Iterator<String> it = MainActivity.newStocks.iterator(); it.hasNext(); ) {
                if (MainActivity.stopThread) {
                    return;
                }
                String stockSym = it.next();
                try {
                    final Stock stock;
                    stockSym = stockSym.toUpperCase();
                    stock = YahooFinance.get(stockSym);
                    addStockToArrayList(stock);
                    ((MainActivity) ctx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                adapter.add(stock);
                                adapter.notifyDataSetChanged();

                        }
                    });
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }
        catch (Exception e)
        {
            e.getMessage();
        }
        finally {
            MarketFragment.midScrolling = false;
            MainActivity.addingMutex.unlock();
        }
    }

    private void addStockToPortfolio(String ticker)
    {
        try {
            final Stock stock;
            ticker = ticker.toUpperCase();
            stock = YahooFinance.get(ticker);
            synchronized (output) {
                addStockToArrayList(stock);
            }
            ((MainActivity)ctx).runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    synchronized (adapter) {
                        adapter.add(stock);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
        catch (Exception e){
            e.getMessage();
        }
    }

    private void addStockToArrayList(Stock s)
    {
        try {
                output.add(s);
        } catch (Exception e) {
            Log.d("Pushback Error:", e.getMessage());
        }
    }

    private void updateMultiple()
    {
        while(!((MainActivity)ctx).mFinished) {
            while (!((MainActivity)ctx).mPaused) {
                synchronized (output) {
                    for (int i = updateRangeLow; i <= updateRangeTop; ++i) {
                        try {
                            output.get(i).getQuote(true).getPrice();
                            ((MainActivity) ctx).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (adapter) {
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.getMessage();
                        }
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.getMessage();
                }
            }
            }
        }

    private void updateOne()
    {
        while(!df.mFinished) {
            while (!df.mPaused) {
                try {
                        selectedStock = getStockDetails(sym);
                        ((MainActivity) ctx).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView high = (TextView) df.getView().findViewById(R.id.DetailsHigh);
                                TextView value = (TextView) df.getView().findViewById(R.id.DetailsValue);
                                TextView low = (TextView) df.getView().findViewById(R.id.DetailsLow);
                                TextView percent = (TextView) df.getView().findViewById(R.id.DetailsPercent);
                                TextView symbol = (TextView) df.getView().findViewById(R.id.Symbol);
                                TextView company = (TextView) df.getView().findViewById(R.id.DetailsCompany);
                                high.setText(selectedStock.getQuote().getDayHigh().toString());
                                value.setText(selectedStock.getQuote().getPrice().toString());
                                low.setText(selectedStock.getQuote().getDayLow().toString());
                                percent.setText(selectedStock.getQuote().getChangeFromAvg50InPercent().toString());
                                symbol.setText(selectedStock.getSymbol().toString());
                                company.setText(selectedStock.getName().toString());
                            }
                        });
                    } catch (Exception e) {
                        e.getMessage();
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.getMessage();
                    }
            }
        }
    }

    private int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    private void getStockHistory(Calendar from, Calendar to, String sym) //Only used in details view
    {
        try {

            Stock stock = YahooFinance.get(sym);
            SeriesContainer[] dataSet;
            final ArrayList<Float> yAxisValues  = new ArrayList<Float>(){};
            final int days = daysBetween(from.getTime(),to.getTime());

            if ( days <= 7 ) {
                dataSet = getDailyData(sym, days);
                for(SeriesContainer s : dataSet)
                {
                    yAxisValues.add(s.close);
                }
            }
            else
            {
                final List<HistoricalQuote> hist = stock.getHistory(from, to, Interval.DAILY);
                Collections.reverse(hist);
                for(HistoricalQuote s : hist)
                {
                    yAxisValues.add(s.getClose().floatValue());
                }
            }

            ((MainActivity)ctx).runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    List<Entry> entries = new ArrayList<>();
                    int idx = 1;
                    for(float y : yAxisValues)
                    {
                        entries.add(new Entry(idx,y));
                        ++idx;
                    }
                    LineDataSet dataSet = new LineDataSet(entries,"");
                    dataSet.setDrawCircles(false);
                    dataSet.setLineWidth(2);
                    dataSet.setDrawValues(false);
                    dataSet.setColor(Color.rgb(0,125,80));
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    LineData lineData = new LineData(dataSet);
                    LineChart chart = (LineChart)((MainActivity) ctx).findViewById(R.id.DetailsChart);
                    chart.setData(lineData);
                    chart.invalidate(); // refresh
                }
            });
        }
        catch (Exception e){
            e.getMessage();
        }
    }

    private SeriesContainer[] getDailyData(String sym, int range) //Only used in details View
    {
        SeriesContainer[] rs;
        try
        {
            String response;
            URL url = new URL("http://chartapi.finance.yahoo.com/instrument/1.0/" + sym + "/chartdata;type=quote;range=" + range + "d/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            response = readResponse(conn);
            Gson gson = new Gson();
            rs = gson.fromJson(response,SeriesContainer[].class);
            return rs;
        }
        catch (Exception e){e.getMessage();}

        rs = new SeriesContainer[0];
        return rs;
    }

    private String readResponse(HttpURLConnection conn)
    {
        String resp = "";
        boolean addLine = false;
        try {
            String line;
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line = br.readLine()) != null) {
                if (line.equals(" \"series\" : ["))
                {
                    line = "[";
                    addLine = true;
                }
                if (line.equals("]")) {
                    sb.append("]");
                    addLine = false;
                }
                if (addLine)sb.append(line);
            }
            resp = sb.toString();
            br.close();
        }
        catch (Exception e){e.getMessage();}

        return resp;
    }
}



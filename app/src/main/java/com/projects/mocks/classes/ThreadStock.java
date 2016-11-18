package com.projects.mocks.classes;

/**
 * Created by Eric on 11/18/2016.
 */

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import com.projects.mocks.mocks.MainActivity;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class ThreadStock implements Runnable {
    public String mth;
    public  Calendar from;
    public  Calendar to;
    public  String sym;
    private final Lock outMtx;
    private Context ctx;

   public ThreadStock(Context ct)
    {
        outMtx = new ReentrantLock(true);
        ctx = ct;
    }


    @Override
    public void run() {
        switch (mth) {
            case "Update":
                updateStocks();
                break;
            case "Add":
                sym = ((MainActivity)ctx).inputStock.getText().toString();
                addStockToPortfolio(sym);
                break;
            case "History":
                getStockHistory(from, to, sym);
                break;
        }
    }

    private void addStockToPortfolio(String ticker)
    {
        final Stock stock;
        try {
            ticker = ticker.toUpperCase();
            stock = YahooFinance.get(ticker);
            addStock(stock);
            ((MainActivity)ctx).runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    ((MainActivity)ctx).adapter.add(stock);
                    ((MainActivity)ctx).adapter.notifyDataSetChanged();
                }
            });
        }
        catch (Exception e){e.getMessage();}

    }

    private void addStock(Stock s)
    {
        outMtx.lock();
        try {
            ((MainActivity)ctx).output.add(s);
        } catch (Exception e) {
            Log.d("Pushback Error:", e.getMessage());
        }
        finally {
            outMtx.unlock();
        }
    }

    private void updateStocks()
    {
        while(!((MainActivity)ctx).mFinished) {
            while (!((MainActivity)ctx).mPaused) {
                for (Stock s : ((MainActivity)ctx).output) {
                    try {
                        s.getQuote(true).getPrice();
                        ((MainActivity)ctx).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((MainActivity)ctx).adapter.notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {
                        e.getMessage();
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

    private int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    private void getStockHistory(Calendar from, Calendar to, String sym)
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
                    ((MainActivity)ctx).chart.setData(lineData);
                    ((MainActivity)ctx).chart.invalidate(); // refresh
                }
            });
        }
        catch (Exception e){e.getMessage();}
    }

    private SeriesContainer[] getDailyData(String sym, int range)
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

package com.projects.mocks.classes;

import android.database.Cursor;

import com.google.gson.Gson;
import com.projects.mocks.mocks.MainActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import static com.projects.mocks.mocks.MainActivity.databaseIndex;

/**
 * Created by Eric on 11/18/2016.
 */

public class ThreadLeaderboard implements Runnable {
    public String method = "ADD";
    public String username;
    public BigDecimal roi ;
    public int responseCode = 0;
    public User[] topPlayers = new User[11];
    private Gson gson = new Gson();

    @Override
    public void run() {
        if (method == "GET")
            topPlayers = get();
        else if(method == "UPDATE")
            responseCode = update();
        else if (method == "ADD")
            responseCode = add();
    }

    public String readResponse(HttpURLConnection conn)
    {
        String resp = "";
        try {
            String line;
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                resp = sb.toString();
            }
            br.close();
        }
        catch (Exception e)
        {
            e.getMessage();
        }
        return resp;
    }

    public int update()
    {
        double totalValue = MainActivity.user.Balance.doubleValue();

        String response = "";
        try
        {
            Map<String,Integer> portfolioStocks = new HashMap<>();
            MainActivity.db.open();
            Cursor cursor = MainActivity.db.getAllPortfolio();
            if (cursor.moveToFirst()) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String tempSymbol = cursor.getString(cursor.getColumnIndex("Symbol"));
                    Integer tempQty = cursor.getInt(cursor.getColumnIndex("Qty"));
                    portfolioStocks.put(tempSymbol,tempQty);
                    cursor.moveToNext();
                }
            }
            MainActivity.db.close();

            for(Map.Entry<String, Integer> entry : portfolioStocks.entrySet()) {
                Stock currStock = YahooFinance.get(entry.getKey());
                totalValue += entry.getValue() * currStock.getQuote().getPrice().doubleValue();
            }



            String params = "username=" + MainActivity.user.username +"&roi=" + totalValue +"&level=" + MainActivity.user.difficulty;
            byte[] postData = params.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            URL url = new URL("http://mocks.gear.host/updateUser.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("charset","utf-8");
            conn.setRequestProperty("Content-Length",Integer.toString(postDataLength));
            conn.setUseCaches(false);
            try(DataOutputStream wr = new DataOutputStream(conn.getOutputStream()))
            {
                wr.write(postData);
            }
            response = readResponse(conn);
        }
        catch (Exception e)
        {
            e.getMessage();
        }

        int responseCode = gson.fromJson(response,int.class);

        if (responseCode == 0) {
            return 0; //User exists
        }
        else if(responseCode == 1) {
            return 1; // Passed
        }
        else {
            return 2; // Failed insert
        }
    }

    public int add()
    {
        String response = "";
        try
        {
            String params = "username=" + username;
            byte[] postData = params.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            URL url = new URL("http://mocks.gear.host/newUser.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("charset","utf-8");
            conn.setRequestProperty("Content-Length",Integer.toString(postDataLength));
            conn.setUseCaches(false);
            try(DataOutputStream wr = new DataOutputStream(conn.getOutputStream()))
            {
                wr.write(postData);
            }
            response = readResponse(conn);
        }
        catch (Exception e)
        {
            e.getMessage();
        }
        int responseCode = gson.fromJson(response,int.class);
        if (responseCode == 0)
            return 0; //User exists
        else if(responseCode == 1)
            return 1; // Passed
        else
            return 2; // Failed insert
    }


    public User[] get()
    {
        String response = "";
        try
        {
            URL url = new URL("http://mocks.gear.host/getLeaderboard.php?username="+ username + "&level=" + MainActivity.user.difficulty);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            response = readResponse(conn);
        }
        catch (Exception e)
        {
            e.getMessage();
        }
        User[] lb = gson.fromJson(response,User[].class);
        return lb;
    }
}
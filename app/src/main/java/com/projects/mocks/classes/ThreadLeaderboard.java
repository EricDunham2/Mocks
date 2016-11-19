package com.projects.mocks.classes;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by Eric on 11/18/2016.
 */

public class ThreadLeaderboard implements Runnable {
    public String method = "ADD";
    public String username;
    public BigDecimal roi ;
    private int responseCode = 0;
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
        String response = "";
        try
        {
            String params = "username=" + username +"&roi=" + roi;
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
            URL url = new URL("http://mocks.gear.host/getLeaderboard.php?username="+ username);
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
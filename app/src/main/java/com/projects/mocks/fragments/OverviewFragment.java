package com.projects.mocks.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.NavigationView;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.projects.mocks.classes.ThreadParams;
import com.projects.mocks.classes.ThreadStock;
import com.projects.mocks.mocks.DBAdapter;
import com.projects.mocks.mocks.MainActivity;
import com.projects.mocks.mocks.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import static com.projects.mocks.mocks.MainActivity.databaseIndex;
import static com.projects.mocks.mocks.MainActivity.newStocks;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment
{

    TextView userBalance;
    TextView usernameO;
    TextView marketValue;
    Map<String,Integer> userStocks;
    List<Stock> userStockListView;
    //DON'T EDIT THIS. Anything you want done in a fragment should go in the "onViewCreated" function.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //leave this on top unless you're absolutely sure something needs to go above this
        super.onViewCreated(view, savedInstanceState);

        //used for back stacking and making sure the correct nav item is selected.
        if (MainActivity.navigationView != null)
            MainActivity.navigationView.getMenu().findItem(R.id.nav_overview).setChecked(true);
        if (!getContext().getSharedPreferences("settings", getActivity().CONTEXT_RESTRICTED).getBoolean("firstRun", true)) {
            userBalance = (TextView) getView().findViewById(R.id.overviewBalance);
            usernameO = (TextView) getView().findViewById(R.id.overviewUsername);
            marketValue = (TextView) getView().findViewById(R.id.overviewMarketValue);
            userStockListView = new ArrayList<>();
            userStocks = new HashMap<>();
            usernameO.setText(MainActivity.user.username);
            userBalance.setText(MainActivity.user.Balance.toString());

            MainActivity.db.open();
            Cursor cursor = MainActivity.db.getAllPortfolio();

            if (cursor.moveToFirst()) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    userStocks.put(cursor.getString(cursor.getColumnIndex("Symbol")), cursor.getInt(cursor.getColumnIndex("QTY")));
                    cursor.moveToNext();
                }
            }
            MainActivity.db.close();

            double totalValue = 0.0;

            for (Map.Entry<String, Integer> entry : userStocks.entrySet()) {
                try {
                    ThreadParams singleStockParams = new ThreadParams();
                    singleStockParams.sym = entry.getKey();
                    singleStockParams.mth = "DETAILS";
                    ThreadStock singleStockDetail = new ThreadStock(singleStockParams);
                    Thread singleStockThread = new Thread(singleStockDetail);
                    singleStockThread.start();
                    singleStockThread.join();
                    userStockListView.add(singleStockDetail.singleStockReturn);
                    double currStockValue = singleStockDetail.singleStockReturn.getQuote().getPrice().doubleValue();
                    totalValue += entry.getValue() * currStockValue;
                } catch (Exception e) {
                    e.getMessage();
                }
                marketValue.setText("~" + totalValue);

            }
        }
    }
}

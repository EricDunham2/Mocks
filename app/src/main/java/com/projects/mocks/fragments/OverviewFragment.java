package com.projects.mocks.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.NavigationView;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.projects.mocks.classes.ThreadParams;
import com.projects.mocks.classes.ThreadStock;
import com.projects.mocks.classes.User;
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

import static com.projects.mocks.mocks.MainActivity.adapter;
import static com.projects.mocks.mocks.MainActivity.databaseIndex;
import static com.projects.mocks.mocks.MainActivity.newStocks;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment {

    TextView userBalance;
    TextView usernameO;
    TextView marketValue;
    ArrayList<userPortfolioStocksCustom> userStocks;
    ListView portfolioListView;
    ThreadStock updateStock;
    //DON'T EDIT THIS. Anything you want done in a fragment should go in the "onViewCreated" function.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    public class userPortfolioStocksCustom
    {
        public String symbol;
        public int qty;
        public double price;
        public userPortfolioStocksCustom(String s, int q, double p)
        {
            symbol = s;
            qty = q;
            price = p;
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //leave this on top unless you're absolutely sure something needs to go above this
        super.onViewCreated(view, savedInstanceState);

        //used for back stacking and making sure the correct nav item is selected.
        if (MainActivity.navigationView != null)
            MainActivity.navigationView.getMenu().findItem(R.id.nav_overview).setChecked(true);
        if (!getContext().getSharedPreferences("settings", getActivity().CONTEXT_RESTRICTED).getBoolean("firstRun", true)) {
            FragmentManager fm = getFragmentManager();
            android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);
            userBalance = (TextView) getView().findViewById(R.id.overviewBalance);
            usernameO = (TextView) getView().findViewById(R.id.overviewUsername);
            marketValue = (TextView) getView().findViewById(R.id.overviewMarketValue);
            userStocks = new ArrayList<>();
            usernameO.setText(MainActivity.user.username);
            userBalance.setText(MainActivity.user.Balance.toString());
            portfolioListView = (ListView) getView().findViewById(R.id.overviewPortfolio);
            setStockListView();
            MainActivity.db.open();
            Cursor cursor = MainActivity.db.getAllPortfolio();

            if (cursor.moveToFirst()) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    ThreadParams params = new ThreadParams();
                    params.sym = cursor.getString(cursor.getColumnIndex("Symbol"));
                    params.mth = "DETAILS";
                    ThreadStock addThreadStock = new ThreadStock(params);
                    Thread addThread = new Thread(addThreadStock);
                    addThread.start();
                    try {
                        addThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                  Stock tmp = addThreadStock.singleStockReturn;

                    if(tmp != null || tmp.getQuote()!= null || tmp.getQuote().getPrice() != null) {
                        userPortfolioStocksCustom stock = new userPortfolioStocksCustom(cursor.getString(cursor.getColumnIndex("Symbol")), cursor.getInt(cursor.getColumnIndex("QTY")),tmp.getQuote().getPrice().doubleValue());
                        userStocks.add(stock);
                    }
                    cursor.moveToNext();
                }
            }
            MainActivity.db.close();


            double totalValue = 0.0;

            for (userPortfolioStocksCustom entry : userStocks) {
                try {
                    double currStockValue = entry.price;
                    totalValue += entry.qty * currStockValue;
                } catch (Exception e) {
                    e.getMessage();
                }
                marketValue.setText(String.format("~" + totalValue, "#.###"));
            }
            OverviewListAdapter overviewListAdapter = new OverviewListAdapter(getContext(), R.layout.user_layout, userStocks);
            portfolioListView.setAdapter(overviewListAdapter);

        }
    }

    private void setStockListView() {
        portfolioListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userPortfolioStocksCustom clk = (userPortfolioStocksCustom) portfolioListView.getItemAtPosition(position);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);
                Bundle bundle = new Bundle();
                bundle.putString("selectedStock",clk.symbol);
                DetailsFragment detailsFragment = new DetailsFragment();
                detailsFragment.setArguments(bundle);
                ft.replace(currentFragment.getId(), detailsFragment, "F_DETAILS");
                ft.addToBackStack(currentFragment.getTag());
                ft.commit();
            }
        });
    }

    public class OverviewListAdapter extends ArrayAdapter<userPortfolioStocksCustom> {

        private int layoutResource;

        public OverviewListAdapter(Context context, int layoutResource, ArrayList<userPortfolioStocksCustom> stocksCustoms) {
            super(context, layoutResource, stocksCustoms);
            this.layoutResource = layoutResource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                view = layoutInflater.inflate(layoutResource, null);
            }

            userPortfolioStocksCustom stock = getItem(position);

            if (stock != null) {
                TextView leftTextView = (TextView) view.findViewById(R.id.customLayoutLeft);
                TextView rightTextView = (TextView) view.findViewById(R.id.customLayoutCentre);
                TextView centreTextView = (TextView) view.findViewById(R.id.customLayoutRight);
                    leftTextView.setText(""+stock.qty);
                    rightTextView.setText("~"+stock.price);
                    centreTextView.setText(stock.symbol);

            }

            return view;
        }
    }
}

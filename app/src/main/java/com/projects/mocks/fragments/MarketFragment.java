package com.projects.mocks.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.projects.mocks.mocks.MainActivity;
import com.projects.mocks.classes.*;
import com.projects.mocks.mocks.R; // WHY!!!!!!!!!!!!!!!!!!!!!
import java.util.ArrayList;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;


/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment
{
    private ListView stocklv;
    public ArrayList<Stock> output;
    public ArrayAdapter<Stock> adapter;
    public Thread updateThread;
    EditText searchFor;
    //DON'T EDIT THIS. Anything you want done in a fragment should go in the "onViewCreated" function.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_market, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        //leave this on top unless you're absolutely sure something needs to go above this
        super.onViewCreated(view, savedInstanceState);

        //used for back stacking and making sure the correct nav item is selected.
        if(MainActivity.navigationView != null)
            MainActivity.navigationView.getMenu().findItem(R.id.nav_market).setChecked(true);

        output = new ArrayList<>();
        stocklv = (ListView) getView().findViewById(R.id.AllStocks);
        setStockListView();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        stocklv.setAdapter(adapter);
      ThreadParams addParams = new ThreadParams();
                    addParams.output = output;
                    addParams.adapter = adapter;
                    addParams.mth = "ADD";
                    addParams.sym = "GOOGL";
//        ThreadStock threadStock = new ThreadStock("ADD",getContext(),tp);
//        threadStock.sym = "AAPL"; // TODO Replace with for loop to go through all stocks
//        Thread thread = new Thread(threadStock);
//        thread.start();

        ThreadParams updateParams = new ThreadParams();
            updateParams.adapter = adapter;
            updateParams.output = output;
            updateParams.ctx = getContext();
            updateParams.mth = "UPDATE_MULTIPLE";
        ThreadStock st = new ThreadStock(updateParams);
        updateThread = new Thread(st);
        updateThread.start();
    }

    private void setStockListView()
    {
        stocklv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stock clk = (Stock)stocklv.getItemAtPosition(position);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);
                Bundle bundle = new Bundle();
                bundle.putString("selectedStock",clk.getSymbol());
                DetailsFragment detailsFragment = new DetailsFragment();
                detailsFragment.setArguments(bundle);
                ft.replace(currentFragment.getId(),detailsFragment,"F_DETAILS");
                ft.addToBackStack(currentFragment.getTag());
                ft.commit();
                //Open Details Fragment
                //Pass it Params of the symbol.
            }
        });

        stocklv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
                int firstVisibleRow = stocklv.getFirstVisiblePosition();
                int lastVisibleRow = stocklv.getLastVisiblePosition();

                for(int i=firstVisibleRow;i<=lastVisibleRow;i++)
                {
                    //ADD CODE FOR BETTER UPDATING
                }
            }
        });
    }
}

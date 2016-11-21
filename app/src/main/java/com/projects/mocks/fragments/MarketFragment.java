package com.projects.mocks.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
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

import static com.projects.mocks.mocks.MainActivity.adapter;
import static com.projects.mocks.mocks.MainActivity.databaseIndex;
import static com.projects.mocks.mocks.MainActivity.output;


/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment {
    private ListView stocklv;

    public Thread updateThread;
    private ArrayList<String> newStocks;
    ThreadStock updateStock;
    EditText searchFor;  // TODO Somehow search the database and fill the listview idea: Query db make array of results copy current listview contents to new array and replace with query values on "" replace with old values

    //DON'T EDIT THIS. Anything you want done in a fragment should go in the "onViewCreated" function.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_market, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //leave this on top unless you're absolutely sure something needs to go above this
        super.onViewCreated(view, savedInstanceState);

        //used for back stacking and making sure the correct nav item is selected.
        if (MainActivity.navigationView != null)
            MainActivity.navigationView.getMenu().findItem(R.id.nav_market).setChecked(true);

        newStocks = new ArrayList<>();
        stocklv = (ListView) getView().findViewById(R.id.AllStocks);
        setStockListView();

        stocklv.setAdapter(adapter);
        if (adapter.getCount() == 0) {
            MainActivity.db.open();
            Cursor cursor = MainActivity.db.getFiftySymbols(databaseIndex);

            if (cursor.moveToFirst()) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    newStocks.add(cursor.getString(cursor.getColumnIndex("Name")));
                    cursor.moveToNext();
                }
            }
            MainActivity.db.close();

            ThreadParams addParams = new ThreadParams();
            addParams.output = output;
            addParams.adapter = adapter;
            addParams.mth = "ADD_MULTIPLE";
            addParams.ctx = getContext();
            ThreadStock threadStock = new ThreadStock(addParams);
            threadStock.stocksToAdd = newStocks;
            Thread thread = new Thread(threadStock);
            thread.start();


            ThreadParams updateParams = new ThreadParams();
            updateParams.adapter = adapter;
            updateParams.output = output;
            updateParams.ctx = getContext();
            updateParams.mth = "UPDATE_MULTIPLE";
            updateStock = new ThreadStock(updateParams);
            updateStock.updateRangeLow = 0;
            updateStock.updateRangeTop = 11;
            updateThread = new Thread(updateStock);
            updateThread.start();
        }
    }

    private void setStockListView() {
        stocklv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stock clk = (Stock) stocklv.getItemAtPosition(position);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);
                Bundle bundle = new Bundle();
                bundle.putString("selectedStock", clk.getSymbol());
                DetailsFragment detailsFragment = new DetailsFragment();
                detailsFragment.setArguments(bundle);
                ft.replace(currentFragment.getId(), detailsFragment, "F_DETAILS");
                ft.addToBackStack(currentFragment.getTag());
                ft.commit();
            }
        });

        stocklv.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.scrollComplete();

            }

            public void scrollComplete() {
                int totalItemCount = adapter.getCount();
                if ((stocklv.getLastVisiblePosition() + 1) == totalItemCount && stocklv.getChildAt(stocklv.getChildCount() - 1).getBottom() <= stocklv.getHeight()) {
                    MainActivity.db.open();
                    databaseIndex += 50;
                    Cursor cursor = MainActivity.db.getFiftySymbols(databaseIndex);
                    newStocks.clear();
                    if (cursor.moveToFirst()) {
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            newStocks.add(cursor.getString(cursor.getColumnIndex("Name")));
                            cursor.moveToNext();
                        }
                    }
                    MainActivity.db.close();

                    ThreadParams addParams = new ThreadParams();
                    addParams.output = output;
                    addParams.adapter = adapter;
                    addParams.mth = "ADD_MULTIPLE";
                    addParams.ctx = getContext();
                    ThreadStock threadStock = new ThreadStock(addParams);
                    threadStock.stocksToAdd = newStocks;
                    Thread thread = new Thread(threadStock);
                    thread.start();
                }
            }


            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int firstVisibleRow = stocklv.getFirstVisiblePosition();
                int lastVisibleRow = stocklv.getLastVisiblePosition();
                for (int i = firstVisibleRow; i <= lastVisibleRow; i++) {
                    updateStock.updateRangeLow = firstVisibleRow;
                    updateStock.updateRangeTop = lastVisibleRow;
                }
            }
        });
    }
}

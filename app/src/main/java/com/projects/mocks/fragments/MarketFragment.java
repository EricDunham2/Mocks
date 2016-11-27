package com.projects.mocks.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import com.projects.mocks.mocks.MainActivity;
import com.projects.mocks.classes.*;
import com.projects.mocks.mocks.R;
import yahoofinance.Stock;
import static com.projects.mocks.mocks.MainActivity.adapter;
import static com.projects.mocks.mocks.MainActivity.allStocksArrayList;
import static com.projects.mocks.mocks.MainActivity.databaseIndex;
import static com.projects.mocks.mocks.MainActivity.newStocks;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment {
    private ListView allStocksListView;
    Thread updateThread;
    Thread addThread;
    ThreadStock updateStock;
    ThreadStock addStocks;
    EditText searchSymbolsFor;
    boolean reloadedFragment = false;
    boolean isSearchResults;
    public static boolean marketPaused;
    public static boolean midScrolling;

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
        MainActivity.fab.hide();
        if (MainActivity.navigationView != null)
            MainActivity.navigationView.getMenu().findItem(R.id.nav_market).setChecked(true);
            fillListView();
    }

    private void fillListView()
    {
        MainActivity.stopThread = false;
        MainActivity.fab.hide();
        searchSymbolsFor = (EditText)getView().findViewById(R.id.searchStocks);
        allStocksListView = (ListView) getView().findViewById(R.id.AllStocks);
        setStockListView();
        isSearchResults = false;
        midScrolling = false;
        allStocksListView.setAdapter(adapter);
        if (MainActivity.allStocksArrayList.size() == 0) {
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
        }
        else if(MainActivity.allStocksArrayList.size() != 0)
        {
            newStocks.clear();
            for(Stock s : allStocksArrayList)
                newStocks.add(s.getSymbol());
            allStocksArrayList.clear();
            reloadedFragment = true;
        }

            ThreadParams addParams = new ThreadParams();
            addParams.adapter = adapter;
            addParams.mth = "ADD_MULTIPLE";
            addParams.ctx = getContext();

            addStocks = new ThreadStock(addParams);
            addThread = new Thread(addStocks);
            addThread.start();

            ThreadParams updateParams = new ThreadParams();
            updateParams.adapter = adapter;
            updateParams.ctx = getContext();
            updateParams.mth = "UPDATE_MULTIPLE";
            updateStock = new ThreadStock(updateParams);
            updateStock.updateRangeLow = 0;
            updateStock.updateRangeTop = 11;
            updateThread = new Thread(updateStock);
            updateThread.start();
    }

    private void setStockListView() {
        allStocksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stock clk = (Stock) allStocksListView.getItemAtPosition(position);
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

        allStocksListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
             if(!isSearchResults)
                this.scrollComplete();
            }

            public void scrollComplete() {
                int totalItemCount = adapter.getCount();
                if ((allStocksListView.getLastVisiblePosition() + 1) == totalItemCount &&
                        allStocksListView.getChildAt(allStocksListView.getChildCount() - 1).getBottom() <= allStocksListView.getHeight() &&
                        !midScrolling) {
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
                    addThread = new Thread(addStocks);
                    addThread.start();
                }
            }


            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int firstVisibleRow = allStocksListView.getFirstVisiblePosition();
                int lastVisibleRow = allStocksListView.getLastVisiblePosition();
                for (int i = firstVisibleRow; i <= lastVisibleRow; i++) {
                    updateStock.updateRangeLow = firstVisibleRow;
                    updateStock.updateRangeTop = lastVisibleRow;
                }
            }
        });



        searchSymbolsFor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!reloadedFragment) {
                    MainActivity.stopThread = true;
                    MainActivity.fab.setImageResource(R.drawable.ic_menu_plus);
                    if (s.toString().equals("")) {
                        newStocks.clear();
                        for (String stockSymbol : MainActivity.listViewState)
                            newStocks.add(stockSymbol);
                        MainActivity.listViewState.clear();
                        isSearchResults = false;
                    } else {
                        MainActivity.db.open();
                        isSearchResults = true;
                        if (MainActivity.listViewState.isEmpty()) {
                            for(Stock tmps : allStocksArrayList)
                                MainActivity.listViewState.add(tmps.getSymbol());
                        }
                       Cursor cursor = MainActivity.db.searchForSymbol(s.toString());
                        newStocks.clear();
                        if (cursor.moveToFirst()) {
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                newStocks.add(cursor.getString(cursor.getColumnIndex("Name")));
                                cursor.moveToNext();
                            }
                        }
                    }
                    MainActivity.stopThread = false;
                    MainActivity.allStocksArrayList.clear();
                    adapter.notifyDataSetChanged();
                    addThread = new Thread(addStocks);
                    addThread.start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        marketPaused = false;
        reloadedFragment = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        marketPaused = true;
        reloadedFragment = true;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

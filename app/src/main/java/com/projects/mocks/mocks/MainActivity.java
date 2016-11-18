package com.projects.mocks.mocks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import com.projects.mocks.classes.*;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.projects.mocks.fragments.InventoryFragment;
import com.projects.mocks.fragments.LeaderboardFragment;
import com.projects.mocks.fragments.MarketFragment;
import com.projects.mocks.fragments.OverviewFragment;
import com.projects.mocks.fragments.SettingsFragment;
import com.projects.mocks.fragments.ShopFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

import yahoofinance.Stock;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    private OverviewFragment oFrag;
    private MarketFragment mFrag;
    private InventoryFragment iFrag;
    private ShopFragment sFrag;
    private LeaderboardFragment lFrag;
    private SettingsFragment settingsFrag;
    public ArrayList<Stock> output = new ArrayList<>();
    public ArrayAdapter<Stock> adapter;
    public ListView stocklv;
    public EditText inputStock;
    public LineChart chart;
    public boolean mPaused = false;
    public boolean mFinished = false;
    Thread updateThread;
    EditText searchFor;
    EditText newUser;
    //ArrayList<Stock> visibleListViewStocks = new ArrayList<>();
    String activeStockDetails = "";
    private FloatingActionButton fab;
    private ProgressDialog progress;
    public static Activity main;

    private android.app.FragmentManager fm;
    public static NavigationView navigationView;
    SharedPreferences settings;

    protected void onCreate(Bundle savedInstanceState)
    {

        settings = getSharedPreferences("settings", CONTEXT_RESTRICTED);

        if(settings.getBoolean("theme", false)){
            //Toast.makeText(this, "is dark theme", Toast.LENGTH_LONG).show();
            setTheme(R.style.AppThemeDark);
        }
        else{
            //Toast.makeText(this, "is light theme", Toast.LENGTH_LONG).show();
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = getFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        oFrag = new OverviewFragment();
        mFrag = new MarketFragment();
        iFrag = new InventoryFragment();
        sFrag = new ShopFragment();
        lFrag = new LeaderboardFragment();
        settingsFrag = new SettingsFragment();

        fm.beginTransaction().replace(R.id.mainFrame, oFrag, "F_OVERVIEW").commit();

        navigationView.getMenu().findItem(R.id.nav_overview).setChecked(true);
        //getWindow().setNavigationBarColor(getResources().getColor(R.color.colorAccent));

        main = this;
    }

    //This shouldn't have to be changed. Back button is being handled correctly.
    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            int backStackEntryCount = fm.getBackStackEntryCount();

            android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);

            if(backStackEntryCount == 0)
            {
                ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogTheme);
                new AlertDialog.Builder(ctw)
                        .setMessage("Are you sure you want to exit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                MainActivity.super.onBackPressed();
                            }
                        })
                        .setNegativeButton("No", null).show();
            }
            else
            {
                //drawCorrectFragmentFromId(currentFragment.getId());
                //fm.popBackStack();
                super.onBackPressed();
            }
        }
    }

    //Unless we add more buttons to the options menu, this shouldn't need to be changed at all.
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    //This shouldn't have to be changed. All of the fragment handling is happening correctly as far as i can tell.
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            if(currentFragment.getTag() != "F_SETTINGS")
            {
                fm.beginTransaction().replace(currentFragment.getId(), settingsFrag, "F_SETTINGS").addToBackStack(currentFragment.getTag()).commit();
            }
            fab.hide();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        drawCorrectFragmentFromId(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void drawCorrectFragmentFromId(int id)
    {

        android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);

        //make sure that the id is the same as the fragment it's trying to draw but also that the current fragment's tag isnt the same, that way we wont draw the same fragment on top of itself.
        if (id == R.id.nav_overview && currentFragment.getTag() != "F_OVERVIEW")
        {
            //make sure that the current fragment isnt a fragment that should not be added to the backstack.
            if(currentFragment.getTag() == "F_SETTINGS")
            {
                //apparently I need this.
                fm.popBackStack();
                //don't add settings to the backstack.
                fm.beginTransaction().replace(currentFragment.getId(), oFrag, "F_OVERVIEW").addToBackStack(currentFragment.getTag()).commit();
            }
            //any other fragment, add to the backstack.
            else
                fm.beginTransaction().replace(currentFragment.getId(), oFrag, "F_OVERVIEW").addToBackStack(currentFragment.getTag()).commit();
            //this is an activity that should show the fab
            fab.show();
        }
        else if (id == R.id.nav_market && currentFragment.getTag() != "F_MARKET")
        {
            if(currentFragment.getTag() == "F_SETTINGS")
            {
                fm.popBackStack();
                fm.beginTransaction().replace(currentFragment.getId(), mFrag, "F_MARKET").addToBackStack(currentFragment.getTag()).commit();
            }
            else
                fm.beginTransaction().replace(currentFragment.getId(), mFrag, "F_MARKET").addToBackStack(currentFragment.getTag()).commit();
            fab.show();
        }
        else if (id == R.id.nav_inventory && currentFragment.getTag() != "F_INVENTORY")
        {
            fab.hide();
            if(currentFragment.getTag() == "F_SETTINGS")
            {
                fm.popBackStack();
                fm.beginTransaction().replace(currentFragment.getId(), iFrag, "F_INVENTORY").addToBackStack(currentFragment.getTag()).commit();
            }
            else
                fm.beginTransaction().replace(currentFragment.getId(), iFrag, "F_INVENTORY").addToBackStack(currentFragment.getTag()).commit();
        }
        else if (id == R.id.nav_shop && currentFragment.getTag() != "F_SHOP")
        {
            fab.hide();
            if(currentFragment.getTag() == "F_SETTINGS")
            {
                fm.popBackStack();
                fm.beginTransaction().replace(currentFragment.getId(), sFrag, "F_SHOP").addToBackStack(currentFragment.getTag()).commit();
            }
            else
                fm.beginTransaction().replace(currentFragment.getId(), sFrag, "F_SHOP").addToBackStack(currentFragment.getTag()).commit();
        }
        else if (id == R.id.nav_leaderboard && currentFragment.getTag() != "F_LEADERBOARD")
        {
            fab.hide();
            if(currentFragment.getTag() == "F_SETTINGS")
            {
                fm.popBackStack();
                fm.beginTransaction().replace(currentFragment.getId(), lFrag, "F_LEADERBOARD").addToBackStack(currentFragment.getTag()).commit();
            }
            else
                fm.beginTransaction().replace(currentFragment.getId(), lFrag, "F_LEADERBOARD").addToBackStack(currentFragment.getTag()).commit();
        }

    }

    public void onClickBankruptcy(View view)
    {
        this.onBackPressed();

        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogTheme);
        new AlertDialog.Builder(ctw)
                .setMessage("Are you sure you want to declare bankruptcy?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //todo: something else.
                        //Toast.makeText(getApplicationContext(), "Ha, you're bankrupt!", Toast.LENGTH_LONG).show();

                        //todo: uncomment the blow block to see what SHOULD happen when you click the button
                        progress = ProgressDialog.show(MainActivity.this, "Declare Bankruptcy",
                                "Resetting Everything.", true);


                        new Thread(new Runnable() {
                            @Override
                            public void run()
                            {
                                // do the thing that takes a long time
                                SystemClock.sleep(3000);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        progress.dismiss();
                                    }
                                });
                            }
                        }).start();
                        //todo: when all that is done, redirect to select difficulty activity.

                    }
                })
                .setNegativeButton("No", null).show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onPause();
        mPaused = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //inputStock = (EditText) findViewById(R.id.editText3);
        stocklv = (ListView) findViewById(R.id.AllStocks);
        chart = (LineChart)findViewById(R.id.DetailsChart);
        searchFor = (EditText)findViewById(R.id.searchListView);
        //newUser = (EditText)findViewById(R.id.inputUsername);
        setChartParams();
        setStockListView();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        stocklv.setAdapter(adapter);
        ThreadStock st = new ThreadStock(this);
        st.mth = "Update";
        updateThread = new Thread(st);
        updateThread.start();
    }
    public void setStockListView()
    {
        stocklv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stock clk = (Stock)stocklv.getItemAtPosition(position);
                activeStockDetails = clk.getSymbol();
                setDateline("WEEK");
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

    public void setDateline(String timespan)
    {
        ThreadStock st = new ThreadStock(this);
        Calendar yes = Calendar.getInstance();
        Calendar tdy = Calendar.getInstance();
        switch (timespan) {
            case "WEEK"://Sunday, Monday, Happy Days,Tuesday, Wednesday, Happy Days,Thursday, Friday, Happy Days, The weekend comes, my cycle hums Ready to race to you
                yes.add(Calendar.DAY_OF_MONTH, -7); //Days of stock data can be missing so number of points on graph can be off
                break;
            case "MONTH":
                yes.add(Calendar.MONTH, -1);
                break;
            case "YEAR":
                yes.add(Calendar.YEAR, -1);
                break;
            case "5YEAR":
                yes.add(Calendar.YEAR, -5);
                break;
        }

        st.mth  = "History";
        st.from = yes;
        st.to = tdy;
        st.sym = activeStockDetails;
        Thread t = new Thread(st);
        t.start();
    }

    public void addU(View view)
    {
        ThreadLeaderboard tl = new ThreadLeaderboard();
        tl.username = newUser.getText().toString();
        tl.method = "ADD";
        Thread t = new Thread(tl);
        t.start();
    }

    public void updateU(View view)
    {
        ThreadLeaderboard tl = new ThreadLeaderboard();
        tl.username = "Cristian";
        tl.roi = new BigDecimal(10000000);
        tl.method = "UPDATE";
        Thread t = new Thread(tl);
        t.start();
    }

    public void leaderboard(View view)
    {
        ThreadLeaderboard tl = new ThreadLeaderboard();
        tl.username = "Cristian";
        tl.method = "GET";
        Thread t = new Thread(tl);
        t.start();
    }

    public void setChartParams()
    {
        chart.setBackgroundColor(Color.argb(255,21,21,21));
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getXAxis().setDrawLabels(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis = chart.getAxisRight();
        yAxis.setDrawGridLines(false);
        chart.getAxisLeft().setTextColor(Color.WHITE); // left y-axis
    }

    public void test(View view)
    {
        ThreadStock st = new ThreadStock(this);
        st.mth  = "Add";
        Thread t = new Thread(st);
        t.start();
    }


}

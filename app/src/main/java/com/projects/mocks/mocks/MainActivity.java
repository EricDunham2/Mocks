        package com.projects.mocks.mocks;

        import android.app.Activity;
        import android.app.Dialog;
        import android.app.ProgressDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.graphics.Color;
        import android.os.Bundle;
        import android.os.SystemClock;
        import android.support.design.widget.FloatingActionButton;
        import android.support.design.widget.Snackbar;
        import android.support.v4.app.FragmentManager;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.view.ContextThemeWrapper;
        import android.util.Log;
        import android.view.View;
        import android.support.design.widget.NavigationView;
        import android.support.v4.view.GravityCompat;
        import android.support.v4.widget.DrawerLayout;
        import android.support.v7.app.ActionBarDrawerToggle;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.CompoundButton;
        import android.widget.EditText;
        import android.widget.NumberPicker;
        import android.widget.Toast;
        import com.projects.mocks.classes.*;
        import com.projects.mocks.fragments.InventoryFragment;
        import com.projects.mocks.fragments.LeaderboardFragment;
        import com.projects.mocks.fragments.MarketFragment;
        import com.projects.mocks.fragments.OverviewFragment;
        import com.projects.mocks.fragments.SettingsFragment;
        import com.projects.mocks.fragments.ShopFragment;

        import java.io.BufferedReader;
        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.FileReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.math.BigDecimal;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.concurrent.locks.ReentrantLock;

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
    private FloatingActionButton fab;
    private ProgressDialog progress;
    static public ArrayList<Stock> allStocksArrayList;
    static public ArrayAdapter<Stock> adapter;
    static public ReentrantLock allStocksMutex;
    static public ReentrantLock addingMutex;
    public static boolean stopThread;
    static public int databaseIndex;
    public static Activity main;
    public boolean mPaused = false;
    public boolean mFinished = false;
    private android.app.FragmentManager fm;
    public static List<String> newStocks;
    public static NavigationView navigationView;
    public static User user;
    //TODO: Make sure that when you move to a new fragment you stop certain fragments
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    //this might need to be static?
    public static DBAdapter db;


    //*
    // Shared Preferences = "settings", CONTEXT_RESTRICTED
    // theme: boolean
    // username: string
    // startingBalance: int
    // currentBalance: string
    // difficulty: string
    //
    //
    // *//

    protected void onCreate(Bundle savedInstanceState)
    {

        settings = getSharedPreferences("settings", CONTEXT_RESTRICTED);
        editor = settings.edit();
        allStocksArrayList = new ArrayList<>();
        databaseIndex = 1;
        stopThread = false;
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
        addingMutex = new ReentrantLock();
        allStocksMutex = new ReentrantLock();
        newStocks = new ArrayList<>();

        if(settings.getBoolean("theme", false)){
            setTheme(R.style.AppThemeDark);
        }
        else{
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(settings.getBoolean("firstRun", true))
        {
            //do stuff if the application is running the first time, such as do the showy showy for the swipe activity, and get their name and poerty level and shit
            Intent i = new Intent(this, FirstTimeRunActivity.class);
            startActivity(i);
        }
        else
        {
            user = new User(settings.getString("username", ""));
            user.StartingAmount = new BigDecimal(settings.getInt("startingBalance", 0));
            user.Balance = new BigDecimal(settings.getString("currentBalance", "0"));
            user.difficulty = settings.getString("difficulty", "none");


            progress = ProgressDialog.show(this, "Setting things up!", "Please wait while we get everything set up for you. This may take a while.", true);
            db = new DBAdapter(MainActivity.this);
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    boolean dbCreated = false;
                    //do database maintenance if needed.


                    try{
                        String destPath = "/data/data/" + getPackageName() + "/databases";
                        File f = new File(destPath);
                        if(!f.exists() || f.listFiles().length < 1)
                        {
                            f.mkdir();
                            f.createNewFile();

                            CopyDB(getBaseContext().getAssets().open("mocksdb.db"), new FileOutputStream(destPath + "/mocksdb"));
                            dbCreated = true;
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                        Log.d("FILE NOT FOUND", e.getMessage());
                    }
                    catch (IOException e)
                    {
                        Log.d("IO Exception", e.getMessage());
                    }
                    //end of database maintenance

//                    if(dbCreated)
//                    {
//                        doAllInserts();
//                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            progress.dismiss();
                        }
                    });
                }
            }).start();

        }


        fm = getFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                android.app.FragmentManager fm = getFragmentManager();
                android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);
                if(currentFragment.getTag().equals("F_DETAILS"))
                {
                    showBuyDialog();
                }

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

    //no longer needed, but kept here just in case.
//    public void doAllInserts(){
//        String line = "";
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("tickers.csv")))) {
//            MainActivity.db.open();
//            while ((line = br.readLine()) != null) {
//                MainActivity.db.insertSymbol(line);
//            }
//            MainActivity.db.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    protected void onStop()
    {
        //no other user member should have to be set.
        if(user != null && user.username != "")
            editor.putString("currentBalance", user.Balance.toString());
        super.onStop();
    }

    public void showBuyDialog()
    {
        final Dialog d = new Dialog(MainActivity.this);
        d.setTitle("Number of stocks to purchase");
        d.setContentView(R.layout.buy_dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(100);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                ///Buy shit
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }


    public void CopyDB(InputStream inputStream, OutputStream outputStream) throws IOException
    {
        // COpy one byte at a time
        byte[] buffer = new byte[1024];
        int length;
        while((length = inputStream.read(buffer)) > 0)
        {
            outputStream.write(buffer,0,length);
        }
        inputStream.close();  // close streams
        outputStream.close();
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

}

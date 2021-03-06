        package com.projects.mocks.mocks;

        import android.app.Activity;
        import android.app.Dialog;
        import android.app.FragmentTransaction;
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.database.Cursor;
        import android.os.Bundle;
        import android.os.SystemClock;
        import android.support.design.widget.FloatingActionButton;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.view.ContextThemeWrapper;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.support.design.widget.NavigationView;
        import android.support.v4.view.GravityCompat;
        import android.support.v4.widget.DrawerLayout;
        import android.support.v7.app.ActionBarDrawerToggle;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.NumberPicker;
        import android.widget.RadioGroup;
        import android.widget.TextView;

        import com.projects.mocks.classes.*;
        import com.projects.mocks.fragments.InventoryFragment;
        import com.projects.mocks.fragments.LeaderboardFragment;
        import com.projects.mocks.fragments.MarketFragment;
        import com.projects.mocks.fragments.OverviewFragment;
        import com.projects.mocks.fragments.SettingsFragment;
        import com.projects.mocks.fragments.ShopFragment;

        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
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
    public static FloatingActionButton fab;
    private ProgressDialog progress;
    static public ArrayList<Stock> allStocksArrayList;
    static public MarketListAdapter adapter;
    static public ReentrantLock addingMutex;
    public static boolean stopThread;
    static public int databaseIndex;
    public static Activity main;
    public static boolean mPaused = false;
    public static boolean mFinished = false;
    private android.app.FragmentManager fm;
    public static Stock selectedStock;
    public static List<String> newStocks;
    public static NavigationView navigationView;
    public static User user;
    public static ArrayList<String> listViewState;


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
        adapter = new MarketListAdapter(getApplicationContext(),R.layout.user_layout,allStocksArrayList);
        addingMutex = new ReentrantLock();
        newStocks = new ArrayList<>();
        listViewState = new ArrayList<>();


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
            //finish();
            startActivity(i);
        }
        else
        {
            user = new User(settings.getString("username", ""));
            user.StartingAmount = new BigDecimal(settings.getInt("startingBalance", 0));
            user.Balance = new BigDecimal(settings.getString("currentBalance", "0"));
            user.difficulty = settings.getString("difficulty", "none");


//            progress = ProgressDialog.show(this, "Setting things up!", "Please wait while we get everything set up for you. This may take a while.", true);
            db = new DBAdapter(MainActivity.this);
//            new Thread(new Runnable() {
//                @Override
//                public void run()
//                {
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

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run()
//                        {
//                            progress.dismiss();
//                        }
//                    });
                //}
//            }).start();

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
                int index = fm.getBackStackEntryCount() - 1;
                String lastFrag = getFragmentManager().getBackStackEntryAt(index).getName();
                android.app.FragmentManager fm = getFragmentManager();
                android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);
                if(selectedStock != null) {
                    if (lastFrag.equals("F_MARKET") & selectedStock.getQuote()!= null) {
                        if (currentFragment.getTag().equals("F_DETAILS") & selectedStock.getQuote().getPrice()!= null) {
                            showBuyDialog();
                        }
                    }
                    if (lastFrag.equals("F_OVERVIEW") & selectedStock.getQuote()!= null) {
                        if (currentFragment.getTag().equals("F_DETAILS") & selectedStock.getQuote().getPrice()!= null) {
                            showSellDialog();
                        }
                    }
                }
                else if(currentFragment.getTag().equals("F_OVERVIEW"))
                {
                    fm.beginTransaction().replace(currentFragment.getId(), mFrag, "F_MARKET").addToBackStack(currentFragment.getTag()).commit();
                }

            }
        });
        fab.hide();


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

        TextView tvUsername = (TextView)navigationView.getHeaderView(0).findViewById(R.id.tcNavUsername);
        tvUsername.setText(settings.getString("username", ""));
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        mFinished = true;
        if(user != null && user.username != "")
            editor.putString("currentBalance", user.Balance.toString());
        editor.commit();

    }

    public void showBuyDialog()
    {
        if(selectedStock.getQuote() == null){return;}
        final Dialog d = new Dialog(MainActivity.this);
        d.setContentView(R.layout.custom_dialog);
        d.setTitle("Number of stocks to purchase");
        TextView dialogText =(TextView)d.findViewById(R.id.dialogTextView);
        dialogText.setText("Choose a number of stocks to Buy.");
        Button buy = (Button) d.findViewById(R.id.btnBuy);
        Button cancel = (Button) d.findViewById(R.id.btnCancel);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        int topBuyQty = (int)Math.floor((user.Balance.doubleValue()/selectedStock.getQuote().getPrice().doubleValue()));
        np.setMaxValue(topBuyQty);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        buy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                MainActivity.db.open();
                Cursor cursor = MainActivity.db.getPortfolioSymbol(selectedStock.getSymbol());
                if (cursor.moveToFirst())
                    MainActivity.db.updatePortfolioSymbol(selectedStock.getSymbol(),(cursor.getInt(cursor.getColumnIndex("QTY")) + np.getValue()));
                else
                    MainActivity.db.insertPortfolio(selectedStock.getSymbol(),np.getValue());
                user.Balance = user.Balance.subtract(selectedStock.getQuote().getPrice().multiply(new BigDecimal(np.getValue())));
                editor.putString("currentBalance", user.Balance.toString());
                editor.commit();
                MainActivity.db.close();
                d.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    public void showSellDialog()
    {
        if(selectedStock.getQuote() == null){return;}
        final Dialog d = new Dialog(MainActivity.this);
        d.setContentView(R.layout.custom_dialog);
        d.setTitle("Number of stocks to sell");
        TextView dialogText =(TextView)d.findViewById(R.id.dialogTextView);
        dialogText.setText("Choose a number of stocks to sell.");

        Button sell = (Button) d.findViewById(R.id.btnBuy);
                sell.setText("Sell");
        Button cancel = (Button) d.findViewById(R.id.btnCancel);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);

        MainActivity.db.open();
        Cursor cursor = MainActivity.db.getPortfolioSymbol(selectedStock.getSymbol());
        int oldQty = 0;
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
             oldQty = cursor.getInt(cursor.getColumnIndex("QTY"));
        }
        MainActivity.db.close();

        int topSellQty = oldQty;
        np.setMaxValue(topSellQty);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        sell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                MainActivity.db.open();
                boolean deleteRow = false;
                int updateReturn = 0;
                int newQTY = np.getMaxValue() - np.getValue();
                if (newQTY == 0)
                    deleteRow = db.deletePortfolioRow(selectedStock.getSymbol());
                else
                 updateReturn = MainActivity.db.updatePortfolioSymbol(selectedStock.getSymbol(),newQTY);

                if(updateReturn == 1 | deleteRow) {
                    user.Balance = user.Balance.add(selectedStock.getQuote().getPrice().multiply(new BigDecimal(np.getValue())));
                    editor.putString("currentBalance", user.Balance.toString());
                    editor.commit();
                }
                MainActivity.db.close();
                d.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    public void showResetDialog()
    {
        final Dialog d = new Dialog(MainActivity.this);
        d.setTitle("New Starting Balance");
        d.setContentView(R.layout.bankrupt);

        RadioGroup rgroup =  (RadioGroup)d.findViewById(R.id.rdogrpBank);
        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.bankruptPoor:
                        editor.putInt("startingBalance", 1000);
                        editor.putString("currentBalance", "1000");
                        editor.putString("difficulty", "hard");
                        user.Balance = new BigDecimal(1000);
                        user.difficulty = "hard";
                        break;
                    case R.id.bankruptMid:
                        editor.putInt("startingBalance", 10000);
                        editor.putString("currentBalance", "10000");
                        editor.putString("difficulty", "medium");
                        user.Balance = new BigDecimal(10000);
                        user.difficulty = "medium";
                        break;
                    case R.id.bankruptRich:
                        editor.putInt("startingBalance", 100000);
                        editor.putString("currentBalance", "100000");
                        editor.putString("difficulty", "easy");
                        user.difficulty = "easy";
                        user.Balance = new BigDecimal(100000);
                        break;
                }
            }
        });

        Button btnRes = (Button)d.findViewById(R.id.btnReset);
        btnRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.db.open();
                MainActivity.db.deleteAllPortfolio();
                MainActivity.db.close();
                android.app.Fragment currentFragment = fm.findFragmentById(R.id.mainFrame);
                if(currentFragment.getTag() == "F_OVERVIEW")
                {
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.detach(currentFragment);
                    ft.attach(currentFragment);
                    ft.commit();
                }
                editor.commit();
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
        else if(id == R.id.nav_share)
        {
            Intent i = new Intent(this, BeamActivity.class);
            startActivity(i);
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

                        showResetDialog();
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
        if(user != null && user.username != "")
            editor.putString("currentBalance", user.Balance.toString());
        editor.commit();
    }

    @Override
    protected void onResume() {
        if(settings.getBoolean("firstRun", true))
            finish();
        super.onPause();
        mPaused = false;
    }

    public class MarketListAdapter extends ArrayAdapter<Stock> {

        private int layoutResource;

        public MarketListAdapter(Context context, int layoutResource, ArrayList<Stock> userStocks) {
            super(context, layoutResource, userStocks);
            this.layoutResource = layoutResource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                view = layoutInflater.inflate(layoutResource, null);
            }

            Stock s = getItem(position);

            if (s != null) {
                TextView rightTextView = (TextView) view.findViewById(R.id.customLayoutCentre);
                TextView centreTextView = (TextView) view.findViewById(R.id.customLayoutRight);
                TextView leftThrowAway = (TextView) view.findViewById(R.id.customLayoutLeft);
                leftThrowAway.setText("");

                if (rightTextView != null && s != null) {
                    rightTextView.setText(s.getSymbol());
                }

                if (centreTextView != null && s != null) {
                    centreTextView.setText("No Data");
                    if(s.getQuote() != null)
                        if(s.getQuote().getPrice() != null)
                            centreTextView.setText(String.format(s.getQuote().getPrice().toString(), "#.00"));
                }
            }
            return view;
        }
    }
}

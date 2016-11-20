package com.projects.mocks.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.NavigationView;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.mocks.mocks.DBAdapter;
import com.projects.mocks.mocks.MainActivity;
import com.projects.mocks.mocks.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment
{
    //DON'T EDIT THIS. Anything you want done in a fragment should go in the "onViewCreated" function.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        //leave this on top unless you're absolutely sure something needs to go above this
        super.onViewCreated(view, savedInstanceState);

        //used for back stacking and making sure the correct nav item is selected.
        if(MainActivity.navigationView != null)
            MainActivity.navigationView.getMenu().findItem(R.id.nav_overview).setChecked(true);

        //remember to open the connection. i lost way too much sleep forgetting to do this.
        MainActivity.db.open();

        //simple insert into the portfolio table.
        MainActivity.db.insertPortfolio("TEST", 5);

        ArrayList<String> slist = new ArrayList<>();
        //get a cursor object from a select query.
        Cursor cursor = MainActivity.db.getAllPortfolio();
        //traverse the cursor.
        if(cursor.moveToFirst())
        {
            //technically this should "move to first" in the if, but sometimes it doesnt. it doesnt hurt to do it again though.
            cursor.moveToFirst();
            //make sure the cursor isnt past the last one (kind of like how c++ does it's pointers)
            while(!cursor.isAfterLast())
            {
                //add to the arraylist. The arraylist could be of anything obviously. if you needed to make a new object
                //    you would just do something like:
                //    "new MyObject(cursor.getString(cursor.getColumnIndex("Name")))"
                slist.add(cursor.getString(cursor.getColumnIndex("QTY")));
                //move to the next item
                cursor.moveToNext();
            }
        }
        //make sure to close the db, always do this. please dont forget.
        MainActivity.db.close();

        TextView tv = (TextView)getView().findViewById(R.id.testBox);
        String st = slist.get(0);
        tv.setText(st);
    }
}

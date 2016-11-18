package com.projects.mocks.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projects.mocks.mocks.MainActivity;
import com.projects.mocks.mocks.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShopFragment extends Fragment
{
    //DON'T EDIT THIS. Anything you want done in a fragment should go in the "onViewCreated" function.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        //leave this on top unless you're absolutely sure something needs to go above this
        super.onViewCreated(view, savedInstanceState);

        //used for back stacking and making sure the correct nav item is selected.
        if(MainActivity.navigationView != null)
            MainActivity.navigationView.getMenu().findItem(R.id.nav_shop).setChecked(true);
    }
}

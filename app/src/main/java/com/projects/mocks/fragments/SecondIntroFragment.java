package com.projects.mocks.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projects.mocks.mocks.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondIntroFragment extends android.support.v4.app.Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second_intro, container, false);
    }

}

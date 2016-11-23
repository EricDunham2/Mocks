package com.projects.mocks.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.projects.mocks.mocks.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BalanceIntroFragment extends android.support.v4.app.Fragment
{
    RadioGroup rgroup;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_balance_intro, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        settings = getContext().getSharedPreferences("settings", Context.CONTEXT_RESTRICTED);
        editor = settings.edit();
        editor.putInt("startingBalance", 1000);
        editor.putString("currentBalance", "1000");
        editor.putString("difficulty", "hard");
        editor.commit();

        rgroup =  (RadioGroup)getView().findViewById(R.id.groupBalance);
        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.rbtnBalPoor:
                        editor.putInt("startingBalance", 1000);
                        editor.putString("currentBalance", "1000");
                        editor.putString("difficulty", "hard");
                        break;
                    case R.id.rbtnBalMiddle:
                        editor.putInt("startingBalance", 10000);
                        editor.putString("currentBalance", "10000");
                        editor.putString("difficulty", "medium");
                        break;
                    case R.id.rbtnBalRich:
                        editor.putInt("startingBalance", 10000);
                        editor.putString("currentBalance", "100000");
                        editor.putString("difficulty", "easy");
                        break;
                }
                editor.commit();
            }
        });
    }
}

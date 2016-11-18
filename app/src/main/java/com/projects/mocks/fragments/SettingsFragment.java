package com.projects.mocks.fragments;


import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.projects.mocks.mocks.MainActivity;
import com.projects.mocks.mocks.R;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment
{
    private SwitchPreference switchTheme;

    boolean ret = false;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

//    //DON'T EDIT THIS. Anything you want done in a fragment should go in the "onViewCreated" function.
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState)
//    {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_settings_pref, container, false);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        //leave this on top unless you're absolutely sure something needs to go above this
        super.onViewCreated(view, savedInstanceState);

        //get preferences to set theme later.
        settings = getContext().getSharedPreferences("settings", Context.CONTEXT_RESTRICTED);
        editor = settings.edit();

        //select the hidden nav item because no nav item should be selected when settings is visible.
        if(MainActivity.navigationView != null)
            MainActivity.navigationView.setCheckedItem(R.id.menu_none);

        //set the value of the switch based on the current theme.
        switchTheme = (SwitchPreference)findPreference("prefSwitchTheme"); //(SwitchPreference)getView().findViewById(R.id.themeSwitch);
        switchTheme.setChecked(settings.getBoolean("theme", false));

        //if the switch is clicked, alert the user that the app needs to reload for the theme to take effect.
        switchTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){

            @Override
            public boolean onPreferenceChange(Preference preference, final Object newValue)
            {

                ContextThemeWrapper ctw = new ContextThemeWrapper(getContext(), R.style.AlertDialogTheme);
                new AlertDialog.Builder(ctw)
                        .setTitle("Warning!")
                        .setMessage("Switching themes requires for an application reload. Would you like to continue?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //if the user clicks yes:
                                //put the bool to the editor to tell it the current theme.
                                editor.putBoolean("theme", (Boolean) newValue);
                                editor.commit();

                                //empty the back stack. For some reason, reloading the app while there are items in the back stack causes weird overlay bugs. dont know why.
                                FragmentManager fm = getFragmentManager();
                                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                                //call the static main activity method to reload (its ugly but it works)
                                MainActivity.main.recreate();
                                ret = true;
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //if the user clicks no, switch the switch back to what it were before.
                                //switchTheme.setChecked(!switchTheme.isChecked());
                                ret = false;
                            }
                        }).show();


                return ret;
            }
        });

    }

}
/*
*             public boolean onPreferenceChange(Preference arg0, Object i)
            {

                new AlertDialog.Builder(getContext())
                        .setMessage("Switching themes requires for an application reload. Would you like to continue?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //if the user clicks yes:
                                //put the bool to the editor to tell it the current theme.
                                editor.putBoolean("theme", switchTheme.isChecked());
                                editor.commit();

                                //empty the back stack. For some reason, reloading the app while there are items in the back stack causes weird overlay bugs. dont know why.
                                FragmentManager fm = getFragmentManager();
                                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                                //call the static main activity method to reload (its ugly but it works)
                                MainActivity.main.recreate();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //if the user clicks no, switch the switch back to what it were before.
                                switchTheme.setChecked(!switchTheme.isChecked());
                            }
                        }).show();
            }
* */
package com.projects.mocks.fragments;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.projects.mocks.classes.ThreadLeaderboard;
import com.projects.mocks.mocks.MainActivity;
import com.projects.mocks.mocks.R;
import com.projects.mocks.classes.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderboardFragment extends Fragment {
    private ListView leaderboardListView;

    //DON'T EDIT THIS. Anything you want done in a fragment should go in the "onViewCreated" function.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //leave this on top unless you're absolutely sure something needs to go above this
        super.onViewCreated(view, savedInstanceState);

        //used for back stacking and making sure the correct nav item is selected.
        if (MainActivity.navigationView != null)
            MainActivity.navigationView.getMenu().findItem(R.id.nav_leaderboard).setChecked(true);

        //TODO: Use portfolio to calculate the new user ROI to update the leaderboard.

        updateU();
        leaderboardListView = (ListView) getView().findViewById(R.id.leaderboardListView);
        User[] topPlayers = leaderboard();
        ArrayList<User> topPlayersArrayList = new ArrayList<User>(Arrays.asList(topPlayers));
        LeaderboardListAdapter threeHorizontalTextViewsAdapter = new LeaderboardListAdapter(getContext(), R.layout.user_layout, topPlayersArrayList);
        leaderboardListView.setAdapter(threeHorizontalTextViewsAdapter);
    }

    public void updateU() {
        ThreadLeaderboard tl = new ThreadLeaderboard();
        tl.method = "UPDATE";
        Thread t = new Thread(tl);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public User[] leaderboard() {
        ThreadLeaderboard tl = new ThreadLeaderboard();
        try {
            tl.method = "GET";
            Thread t = new Thread(tl);
            t.start();
            t.join();
        }catch (Exception e)
        {
            e.getMessage();
        }
        return tl.topPlayers;
    }


    public class LeaderboardListAdapter extends ArrayAdapter<User> {

        private int layoutResource;

        public LeaderboardListAdapter(Context context, int layoutResource, ArrayList<User> players) {
            super(context, layoutResource, players);
            this.layoutResource = layoutResource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                view = layoutInflater.inflate(layoutResource, null);
            }

            User user = getItem(position);

            if (user != null) {
                TextView leftTextView = (TextView) view.findViewById(R.id.customLayoutLeft);
                TextView rightTextView = (TextView) view.findViewById(R.id.customLayoutCentre);
                TextView centreTextView = (TextView) view.findViewById(R.id.customLayoutRight);

                if (leftTextView != null) {
                    leftTextView.setText(user.getRank());
                }

                if (rightTextView != null) {
                    rightTextView.setText(user.username);
                }

                if (centreTextView != null) {
                    centreTextView.setText(user.ROI.toString());
                }
            }

            return view;
        }
    }
}




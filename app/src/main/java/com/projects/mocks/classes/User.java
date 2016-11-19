package com.projects.mocks.classes;

import java.math.BigDecimal;

/**
 * Created by Eric on 11/18/2016.
 */

public class User {
    private int rank;
    final public String username;
    public BigDecimal ROI;

    public User(String username){this.username = username;}

    public String getRank()
    {
        return Integer.toString(rank);
    }
}



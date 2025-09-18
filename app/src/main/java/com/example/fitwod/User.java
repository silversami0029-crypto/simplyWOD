package com.example.fitwod;

import java.util.Date;

public class User {
    private String userId;
    private boolean isPremium;
    private Date subscriptionExpiry;

    // Constructor
    public User() {
        this.userId = userId;
        this.isPremium = isPremium;
    }

    public boolean isPremium() {
        return isPremium && (subscriptionExpiry == null ||
                subscriptionExpiry.after(new Date()));
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    // Add other getters and setters as needed
}
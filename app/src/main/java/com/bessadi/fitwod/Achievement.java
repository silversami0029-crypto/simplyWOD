package com.bessadi.fitwod;

import java.util.Date;

public class Achievement {
    private int id;
    private String title;
    private String description;
    private boolean unlocked;
    private int iconResId;
    private Date unlockedDate;
    private int currentProgress;
    private int targetProgress;

    public Achievement(int id, String title, String description, int iconResId,
                       int currentProgress, int targetProgress) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.currentProgress = currentProgress;
        this.targetProgress = targetProgress;
        this.unlocked = currentProgress >= targetProgress;
    }


    // Getters
    public boolean isUnlocked(){ return unlocked;}
    public void setUnlocked(boolean unlocked){this.unlocked = unlocked;}
    public void setUnlockedDate(Date date){this.unlockedDate = date;}

    public String getTitle() { return title; }

    public int getId() {return id;}
    public String getDescription() { return description; }
    public int getIconResId() { return iconResId; }

    public int getCurrentProgress() { return currentProgress; }
    public int getTargetProgress() { return targetProgress; }
    public String getProgressText() {
        return currentProgress + "/" + targetProgress;
    }

}
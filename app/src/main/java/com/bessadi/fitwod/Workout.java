package com.bessadi.fitwod;

public class Workout {
    private int id;
    private String workType;
    private long createdAt;
    private int duration;
    private int workTime;
    private int restTime;
    private int rounds;
    private String audioPath;

    // Getters and Setters
    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }


    public int getWorkTime() { return workTime; }
    public void setWorkTime(int workTime) { this.workTime = workTime; }

    public int getRestTime() { return restTime; }
    public void setRestTime(int restTime) { this.restTime = restTime; }

    public int getRounds() { return rounds; }
    public void setRounds(int rounds) { this.rounds = rounds; }
}
package com.cs407.wakeguard;

public class AlarmCard {
    /*
    * Notice we're using a String to represent the time and not Java's Date object. This is because
    * using a String for the time in the alarm card example is a matter of simplicity
    * and convenience for display purposes. When you display a time in a user
    * interface, especially if it's for something like an alarm clock,
    * you often want it formatted in a human-readable way, such as "08:00 AM".
    * A String is straightforward for this because it directly represents
    * the formatted text you want to display. You don't need to perform
    * any additional steps to render it in your view.*/
    private String time;
    private String title;

    // If an alarm is active (true) it means it will go off when the time comes
    private boolean active;

    // Is the alarm selected for deletion/turning it off?
    private boolean isSelected = false;

    public AlarmCard(String time, String title, boolean active) {
        this.time = time;
        this.title = title;
        this.active = active;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

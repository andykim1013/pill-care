package org.techtown.medicheck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MedicineItem implements Serializable {
    private String name;
    private boolean morning;
    private boolean lunch;
    private boolean dinner;
    private boolean wakeUp;
    private boolean beforeBed;
    private String recommendText;
    private String ingredient;
    private boolean conflict;

    // 성분 포함 생성자
    public MedicineItem(String name, boolean morning, boolean lunch, boolean dinner, boolean wakeUp, boolean beforeBed, String recommendText, String ingredient) {

        this.name = name;
        this.morning = morning;
        this.lunch = lunch;
        this.dinner = dinner;
        this.wakeUp = wakeUp;
        this.beforeBed = beforeBed;

        this.recommendText = recommendText;
        this.ingredient = ingredient;
        this.conflict = false;
    }

    public String getName() {
        return name;
    }

    public boolean isMorning() {
        return morning;
    }
    public boolean isLunch() {
        return lunch;
    }
    public boolean isDinner() {
        return dinner;
    }
    public boolean isWakeUp() {
        return wakeUp;
    }
    public boolean isBeforeBed() {
        return beforeBed;
    }
    public void setMorning(boolean morning) {
        this.morning = morning;
    }

    public void setLunch(boolean lunch) {
        this.lunch = lunch;
    }

    public void setDinner(boolean dinner) {
        this.dinner = dinner;
    }
    public void setWakeUp(boolean wakeUp) { this.wakeUp = wakeUp;}
    public void setBeforeBed(boolean beforeBed) {this.beforeBed = beforeBed;}
    public String getIngredient() {
        return ingredient;
    }

    public boolean isConflict() {
        return conflict;
    }

    private List<String> repeatDays = new ArrayList<>();

    public List<String> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(List<String> repeatDays) {
        this.repeatDays = repeatDays;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    private boolean isCycleMode;
    private String cycleDays;

    public boolean isCycleMode() {
        return isCycleMode;
    }

    public void setCycleMode(boolean cycleMode) {
        isCycleMode = cycleMode;
    }

    public String getCycleDays() {
        return cycleDays;
    }

    public void setCycleDays(String cycleDays) {
        this.cycleDays = cycleDays;
    }
}
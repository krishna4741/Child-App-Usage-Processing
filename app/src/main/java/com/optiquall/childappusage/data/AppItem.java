package com.optiquall.childappusage.data;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Locale;
import java.util.Objects;


@IgnoreExtraProperties
public class AppItem {
    public String mName;
    public String mPackageName;
    public long mEventTime;
    public long mUsageTime;
    public int mEventType;
    public int mCount;
    public long mMobile;
    public boolean mCanOpen;
    private boolean mIsSystem;
    private boolean isSelected = false;

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "name:%s package_name:%s time:%d total:%d type:%d system:%b count:%d isSelected:%b",
                mName, mPackageName, mEventTime, mUsageTime, mEventType, mIsSystem, mCount, isSelected);
    }

    public AppItem copy() {
        AppItem newItem = new AppItem();
        newItem.mName = this.mName;
        newItem.mPackageName = this.mPackageName;
        newItem.mEventTime = this.mEventTime;
        newItem.mUsageTime = this.mUsageTime;
        newItem.mEventType = this.mEventType;
        newItem.mIsSystem = this.mIsSystem;
        newItem.mCount = this.mCount;
        return newItem;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPackageName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        else if (obj instanceof AppItem) { // implicit null check
            AppItem other = (AppItem) obj;
            return Objects.equals(this.mPackageName, other.mPackageName);
        } else return false;
    }

}

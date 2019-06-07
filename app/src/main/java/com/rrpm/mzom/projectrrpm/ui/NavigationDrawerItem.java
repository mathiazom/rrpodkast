package com.rrpm.mzom.projectrrpm.ui;

import androidx.annotation.NonNull;

public class NavigationDrawerItem {


    private final String title;

    private final int imgResId;

    private final DrawerItemListener itemListener;

    public interface DrawerItemListener{
        void onItemClicked();
    }


    public NavigationDrawerItem(@NonNull final String title, final int imgResId, @NonNull final DrawerItemListener itemListener){

        this.title = title;
        this.imgResId = imgResId;
        this.itemListener = itemListener;

    }

    @NonNull
    String getTitle() {
        return title;
    }

    int getImgResId() {
        return imgResId;
    }

    @NonNull
    DrawerItemListener getItemListener() {
        return itemListener;
    }
}

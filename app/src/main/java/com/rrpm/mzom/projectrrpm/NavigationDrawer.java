package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

class NavigationDrawer extends DrawerLayout {

    private ArrayList<String> itemTitles;
    private ArrayList<Integer> itemImgResIds;
    private ArrayList<DrawerItemListener> drawerItemListeners;

    private int drawerPos;

    public NavigationDrawer(@NonNull Context context) {
        super(context);
    }

    public NavigationDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationDrawer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    interface DrawerItemListener{
        void onItemClicked();
    }

    NavigationDrawer addItem(@NonNull final String itemTitle, final int itemImgResId, @NonNull final DrawerItemListener itemListener){

        if(itemTitles == null){
            itemTitles = new ArrayList<>();
        }
        itemTitles.add(itemTitle);

        if(itemImgResIds == null){
            itemImgResIds = new ArrayList<>();
        }
        itemImgResIds.add(itemImgResId);

        if(drawerItemListeners == null){
            drawerItemListeners = new ArrayList<>();
        }
        drawerItemListeners.add(itemListener);

        return this;

    }

    void initDrawer() {

        final ListView mDrawerList = findViewById(R.id.left_drawer);

        final String[] itemTitlesArray = itemTitles.toArray(new String[0]);

        final int[] itemImgResIdsArray = new int[itemImgResIds.size()];
        for(int i = 0;i<itemImgResIds.size();i++){
            itemImgResIdsArray[i] = itemImgResIds.get(i);
        }

        mDrawerList.setAdapter(new DrawerListAdapter(getContext(), itemTitlesArray, itemImgResIdsArray));

        mDrawerList.setOnItemClickListener((parent, view, position, id) -> {
            drawerPos = position;
            closeDrawers();
        });

        addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

                if (drawerPos >= 0 && drawerPos < drawerItemListeners.size()){

                    drawerItemListeners.get(drawerPos).onItemClicked();

                }

            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

    }




}

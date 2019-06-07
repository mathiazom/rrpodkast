package com.rrpm.mzom.projectrrpm.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.rrpm.mzom.projectrrpm.R;

import java.util.ArrayList;

public class NavigationDrawer extends DrawerLayout {


    private ArrayList<NavigationDrawerItem> drawerItems;

    private int drawerPos;

    private boolean isOpen;


    public NavigationDrawer(@NonNull Context context) {
        super(context);
    }

    public NavigationDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationDrawer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @NonNull
    public NavigationDrawer addItem(@NonNull NavigationDrawerItem item){

        if(drawerItems == null){
            drawerItems = new ArrayList<>();
        }

        drawerItems.add(item);

        return this;

    }

    public void initialize() {

        final ListView mDrawerList = findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(getDrawerListAdapter(getContext()));

        mDrawerList.setOnItemClickListener((parent, view, position, id) -> {

            /*drawerItems.get(position).getItemListener().onItemClicked();

            closeDrawers();*/


            drawerPos = position;

            closeDrawers();

        });

        addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

                drawerPos = -1;

                isOpen = true;

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

                if (drawerPos >= 0 && drawerPos < drawerItems.size()){

                    drawerItems.get(drawerPos).getItemListener().onItemClicked();

                }

                isOpen = false;

            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

    }

    public boolean close(){

        if(isOpen){
            closeDrawers();
            return true;
        }

        return false;

    }

    @NonNull
    private DrawerListAdapter getDrawerListAdapter(@NonNull Context context){

        final ArrayList<String> itemTitleList = new ArrayList<>();
        final ArrayList<Integer> itemImgResIdList = new ArrayList<>();

        for(NavigationDrawerItem item : drawerItems){
            itemTitleList.add(item.getTitle());
            itemImgResIdList.add(item.getImgResId());
        }

        return new DrawerListAdapter(context,itemTitleList.toArray(new String[0]), itemImgResIdList.toArray(new Integer[0]));

    }




}

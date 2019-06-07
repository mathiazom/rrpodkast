package com.rrpm.mzom.projectrrpm.notifications;

import android.app.NotificationChannelGroup;
import android.content.Context;
import android.os.Build;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationChannelGroupBuilder {


    private String id;

    private int nameResId;


    @NonNull
    public NotificationChannelGroup build(@NonNull Context context){

        return new NotificationChannelGroup(id,context.getString(nameResId));

    }


    @NonNull
    NotificationChannelGroupBuilder setId(@NonNull final String id) {
        this.id = id;
        return this;
    }

    @NonNull
    NotificationChannelGroupBuilder setName(final int nameResId) {
        this.nameResId = nameResId;
        return this;
    }

}

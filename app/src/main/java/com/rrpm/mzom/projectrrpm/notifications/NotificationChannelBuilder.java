package com.rrpm.mzom.projectrrpm.notifications;

import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationChannelBuilder{



    private String id;

    private int name;

    private int description;

    private int importance;

    private String groupId;


    NotificationChannelBuilder(){



    }


    @NonNull
    public NotificationChannel build(@NonNull Context context){

        final NotificationChannel channel = new NotificationChannel(id, context.getString(name), importance);

        channel.setDescription(context.getString(description));
        channel.setGroup(groupId);

        return channel;

    }


    @NonNull
    NotificationChannelBuilder setId(@NonNull final String id) {

        this.id = id;

        return this;
    }

    @NonNull
    NotificationChannelBuilder setName(final int nameResId) {

        this.name = nameResId;

        return this;
    }

    @NonNull
    NotificationChannelBuilder setDescription(final int descriptionResId) {

        this.description = descriptionResId;

        return this;
    }

    @NonNull
    NotificationChannelBuilder setImportance(final int importance) {

        this.importance = importance;

        return this;
    }

    @NonNull
    NotificationChannelBuilder setGroupId(@NonNull final String groupId) {

        this.groupId = groupId;

        return this;
    }
}

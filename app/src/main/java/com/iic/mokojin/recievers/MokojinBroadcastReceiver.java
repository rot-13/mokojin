package com.iic.mokojin.recievers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iic.mokojin.Application;
import com.parse.ParsePushBroadcastReceiver;
import com.squareup.otto.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

/**
 * Created by giladgo on 3/11/15.
 */
public class MokojinBroadcastReceiver extends ParsePushBroadcastReceiver {

    private static final String LOG_TAG = MokojinBroadcastReceiver.class.getName();
    public static final String SESSION_DATA_CHANGED = "sessionDataChanged";

    public static class SessionDataChangeBroadcastEvent { }
    public static class PeopleListChangeBroadcastEvent { }
    public static class CharacterListChangeChangeBroadcastEvent { }

    @Inject Bus mBroadcastReceiverBus;

    public MokojinBroadcastReceiver() {
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        ((Application) context.getApplicationContext()).inject(this);

        Log.d(LOG_TAG, "received broadcast!");
        String dataString = intent.getStringExtra("com.parse.Data");

        try {
            JSONObject jsonObject = new JSONObject(dataString);
            String eventType = jsonObject.getString("type");

            Object event = null;

            if (eventType.equals(SESSION_DATA_CHANGED)) {
                event = new SessionDataChangeBroadcastEvent();
            }
            // TODO handle other types of events

            if (event != null) {
                mBroadcastReceiverBus.post(event);
            }
        }
        catch (JSONException ignored) {
            // If "type" doesn't exist on the "data" JSONObject, this isn't a datachange event,
            // and should be displayed as a regular push by ParsePushBroadcastReceiver
            super.onPushReceive(context, intent);
        }



    }

}

package us.bitworld.lmf.andriod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;



public class EventServiceAutoStart extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		// TODO Auto-generated method stub
		Log.i(LMFforAndroid.tag, "Boot Event Received.");
		SharedPreferences settings = context.getSharedPreferences(LMFforAndroid.PREFS_NAME, 0);
        boolean enabled = settings.getBoolean("active", true);

        //start service if pref indicates (this happens on first run)
        if(enabled){
        	context.startService(new Intent(context, EventService.class));
        	Log.i(LMFforAndroid.tag, "Starting Service as indicated by pref");
        }
	}

}

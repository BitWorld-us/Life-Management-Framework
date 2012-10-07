package us.bitworld.lmf.andriod;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
//import android.widget.ToggleButton;

public class LMFforAndroid extends Activity {
	public static final String PREFS_NAME = "LMFPrefs";
	static final String tag = "bitworld";
    /** Called when the activity is first created. */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
               
        final SharedPreferences settings = getSharedPreferences(LMFforAndroid.PREFS_NAME, 0);
        String signalurl = settings.getString("CID_url", "notpresent");
        //boolean active = settings.getBoolean("LMF_active", false);

        if(signalurl == "notpresent") {
        	//need to launch pair activity
        	Intent goToNextActivity = new Intent(getApplicationContext(), Connect.class);
        	startActivity(goToNextActivity);
        	finish();
        }
        else {
            Intent getSleepActivity = new Intent(getApplicationContext(), ZeoSleep.class);
        	startActivity(getSleepActivity);
        	finish();
        }
 
/*
        final ToggleButton button = (ToggleButton) findViewById(R.id.active_button);
        //set initial value
        button.setChecked(active);

        //handle change
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
    			SharedPreferences.Editor editor = settings.edit();
    			editor.putBoolean("LMF_active", button.isChecked());
    			editor.commit();
    			//update service state
    			Toast.makeText(getApplicationContext(), "channel saved", Toast.LENGTH_SHORT).show();
            }
        });
*/

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.mainmenu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.reset:
            //reset config
        	resetConfig();
            return true;
        case R.id.prefs:
            userPrefs();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 
    private void userPrefs() {
        Intent goToSettingActivity = new Intent(getApplicationContext(), Settings.class);
        startActivity(goToSettingActivity);
    }
    
    private void resetConfig(){
    	//clear setting
    	SharedPreferences settings = getSharedPreferences(LMFforAndroid.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove("CID_url");
		editor.commit();
		//toast reset
		Toast.makeText(getApplicationContext(), "Connection URL Reset", Toast.LENGTH_SHORT).show();
		//forward to connect activity
		Intent goToNextActivity = new Intent(getApplicationContext(), Connect.class);
    	startActivity(goToNextActivity);
    	finish();
    }
    	  
}

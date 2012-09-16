package us.bitworld.lmf.andriod;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Connect extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.connect);

        //add choice to use QRcode or login to Kynetx account

        final Button button = (Button) findViewById(R.id.connect_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//start scan activity
            	IntentIntegrator.initiateScan(Connect.this, 
            								IntentIntegrator.DEFAULT_TITLE, 
            								IntentIntegrator.DEFAULT_MESSAGE, 
            								IntentIntegrator.DEFAULT_YES,
            								IntentIntegrator.DEFAULT_NO,
            								IntentIntegrator.QR_CODE_TYPES);
            }
        });

//        final Button login = (Button) findViewById(R.id.login_btn);
//        login.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//            	//get login information
//                setContentView(R.layout.login);
//            }
//        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	if (scanResult != null) {
    		// handle scan result
    		String scanned_url = scanResult.getContents();
    		
    		Log.i(LMFforAndroid.tag, "CID url: "+ scanned_url);
    		
    		//TODO: Check scanned_url to see if is URL
    		
    		SharedPreferences settings = getSharedPreferences(LMFforAndroid.PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("CID_url", scanned_url);
			editor.commit();
			
			//pop toast saying config was saved
			Toast.makeText(getApplicationContext(), "Connection URL Saved", Toast.LENGTH_SHORT).show();
			
			//forward back to main activity
			Intent goToNextActivity = new Intent(getApplicationContext(), LMFforAndroid.class);
        	startActivity(goToNextActivity);
        	finish();
            
    	}
    	//TODO: Display error message here saying to try again.
    }
    	  
}
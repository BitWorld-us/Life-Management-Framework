package us.bitworld.lmf.andriod;

import java.util.Calendar;
import java.util.Date;
import java.io.IOException;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.CalendarContract.Events;

import com.myzeo.android.api.data.ZeoDataContract.SleepRecord;

public class ZeoSleep extends Activity {
    private String mySleep;
    //private JSONObject jSleepRecord = new JSONObject();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleep);

        Button approveShare = (Button) findViewById(R.id.btnOK);
        approveShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView csvSleep = (TextView) findViewById(R.id.csvSleep);
                
                mySleep = buildCsv();
            	if (mySleep == null || mySleep.equals("")) {
                    // Warn user that there is no data and abort prematurely.
                	csvSleep.setText(mySleep);
                    Toast.makeText(ZeoSleep.this, "Sorry, there is no data to send.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                	csvSleep.setText(mySleep);                	
                }
/*
                Intent intent = new Intent(Intent.ACTION_SEND);
                final long now = System.currentTimeMillis();
                final String currentDateTime =
                    DateUtils.formatDateTime(ZeoSleep.this,
                                             now,
                                             DateUtils.FORMAT_SHOW_DATE |
                                             DateUtils.FORMAT_SHOW_TIME |
                                             DateUtils.FORMAT_SHOW_YEAR);
                
                intent.putExtra(Intent.EXTRA_SUBJECT, "My Zeo sleep data as of " + currentDateTime + ".");
                // Add the body text
                StringBuilder builder = new StringBuilder();
                builder.append(mySleep + "\n");
                builder.append("My average ZQ is " + getAvgZQ() + ".\n\n");
                //builder.append("My normal bedtime is " + getRunBed() + ".\n\n");

                intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
                intent.setType("text/plain");

                startActivity(Intent.createChooser(intent, "Share Sleep Data"));
*/
                //finish();

            }
        });    
    }
    
	private Integer getAvgZQ() {
    	String[] selectedColumns = new String[] {
            	SleepRecord.SLEEP_EPISODE_ID,
                SleepRecord.ZQ_SCORE,
            };
    	final Cursor zscores = getContentResolver().query(SleepRecord.CONTENT_URI,
                selectedColumns, null, null, null);
        if (zscores == null) {
            Log.w(LMFforAndroid.tag, "Cursor was null; something is wrong; perhaps Zeo not installed.");
            Toast.makeText(this, "Unable to access data, is Zeo installed?", Toast.LENGTH_LONG).show();
            return null;
        }
        int totZQ = 0;
        int countZQ = 0;
        int avgZQ = 0;
        int totThree = 0;
        int avgThree = 0;
        String bed;
        
        if (zscores.moveToFirst()) {
            do {
            	totZQ = totZQ + zscores.getInt(zscores.getColumnIndex(SleepRecord.ZQ_SCORE));
            	countZQ++;
            } while (zscores.moveToNext());
        } else {
            Log.w(LMFforAndroid.tag, "No sleep records found.");
            Toast.makeText(this, "No sleep records found in the provider.", Toast.LENGTH_SHORT).show();
        }
        //get last three
        zscores.moveToPosition(zscores.getCount() - 3);
        do {
        	totThree = totThree + zscores.getInt(zscores.getColumnIndex(SleepRecord.ZQ_SCORE));
        } while (zscores.moveToNext());
        
        bed = zscores.getString(zscores.getColumnIndex(SleepRecord.START_OF_NIGHT));
        
        zscores.close();
        avgZQ = totZQ/countZQ;
        avgThree = totThree/3;
        
        TextView avgTxt;
        TextView threeAvg;
        
        avgTxt = (TextView) findViewById(R.id.avgZQ);
        threeAvg = (TextView) findViewById(R.id.avgThree);
        avgTxt.setText(String.valueOf(avgZQ));
        threeAvg.setText(String.valueOf(avgThree));    
        
        String avg = "Average Z-score is " + avgZQ + " calculated over " + countZQ + " nights\nLast three days average " + avgThree;
        //Toast.makeText(this, avg, Toast.LENGTH_SHORT).show();

        //put calendar event on
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(Events.TITLE, "Bedtime");
        intent.putExtra(Events.EVENT_LOCATION, "Home sweet home");
        intent.putExtra(Events.DESCRIPTION, bed);

        // Setting dates
        Calendar dateCal = Calendar.getInstance();
      
        // make it now
        dateCal.setTime(new Date());
        // make it tomorrow
        dateCal.add(Calendar.DAY_OF_YEAR, 1);
        
        // Now set it to new bedtime
        //assume bedtime is 2100 
        int bedtimeHours = 21;
        int bedtimeMins = 0;
        if (avgThree < avgZQ) {bedtimeHours--;}
        
        TimeZone tz = TimeZone.getDefault();
        
        dateCal.set(Calendar.ZONE_OFFSET, tz.getRawOffset());
        dateCal.set(Calendar.HOUR_OF_DAY, bedtimeHours);
        dateCal.set(Calendar.MINUTE, bedtimeMins);
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);
     	intent.putExtra(Events.DTSTART, dateCal.getTimeInMillis());
        intent.putExtra(Events.DTEND, dateCal.getTimeInMillis());
        intent.putExtra(Events.HAS_ALARM, 1);
        
/*
        ContentResolver cr = getContentResolver();
        // reminder insert
        Uri REMINDERS_URI = Uri.parse(getCalendarUriBase(this) + "reminders");
        values = new ContentValues();
        values.put( "event_id", Long.parseLong(event.getLastPathSegment()));
        values.put( "method", 1 );
        values.put( "minutes", 10 );
        cr.insert( REMINDERS_URI, values );
 */
        // Make it a full day event
        //intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        // Make it a recurring Event
        //intent.putExtra(Events.RRULE, "FREQ=WEEKLY;COUNT=11;WKST=SU;BYDAY=TU,TH");

        // Making it private and shown as busy
        intent.putExtra(Events.VISIBLE, Events.ACCESS_PRIVATE);
        intent.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
        
        //put it on the calendar
        startActivity(intent);
        
        //end of calculating
        
        //get to the PEN
        processSleepData(avgZQ, countZQ, avgThree);
        return avgZQ;		
	}
    
    private String buildCsv() {
    	
    	String[] projection = new String[] {
            	SleepRecord.SLEEP_EPISODE_ID,
                SleepRecord.LOCALIZED_START_OF_NIGHT,
                SleepRecord.START_OF_NIGHT,
                SleepRecord.END_OF_NIGHT,
                SleepRecord.TIMEZONE,
                SleepRecord.ZQ_SCORE,
                SleepRecord.AWAKENINGS,
                SleepRecord.TIME_IN_DEEP,
                SleepRecord.TIME_IN_LIGHT,
                SleepRecord.TIME_IN_REM,
                SleepRecord.TIME_IN_WAKE,
                SleepRecord.TIME_TO_Z,
                SleepRecord.TOTAL_Z,
                SleepRecord.SOURCE,
                SleepRecord.END_REASON,
                SleepRecord.DISPLAY_HYPNOGRAM,
                SleepRecord.BASE_HYPNOGRAM
            };
    	
        //phone stores in db; web returns JSON object
/*
        final JSONObject jSleep;
        jSleep = new JSONObject();
        final JSONObject jBedTime;
        jBedTime = new JSONObject();
        final JSONObject jRiseTime;
        jRiseTime = new JSONObject();
        final JSONObject jStartDate;
        jStartDate = new JSONObject();
        final JSONObject jGraphStart;
        jGraphStart = new JSONObject();
        final JSONObject jRecord;
        jRecord = new JSONObject();
*/
    	
        String builder = new String();
//    	Calendar myDay = Calendar.getInstance();
//    	int sleepAmt;
//    	int totSleep;
//    	double sleepPerc;
     	//int times = 0;
    	
    	//alter so most recent 3 are only ones pulled?
        final Cursor cursor = getContentResolver().query(SleepRecord.CONTENT_URI, projection, null, null, null);
        if (cursor == null) {
            Log.w(LMFforAndroid.tag, "Cursor was null; something is wrong; perhaps Zeo not installed.");
            Toast.makeText(this, "Unable to access Zeo data, is Zeo installed?", Toast.LENGTH_LONG).show();
            return null;
        }
           	
        if (cursor.moveToLast()) {
        	builder = "Last night you scored " + cursor.getInt(cursor.getColumnIndex(SleepRecord.ZQ_SCORE)) + "\n";
        	//builder = builder + "and your bedTime was " + cursor.getLong(cursor.getColumnIndex(SleepRecord.START_OF_NIGHT));
/*
    		//myDay.setTimeZone(TimeZone.getTimeZone(cursor.getString(cursor.getColumnIndex(SleepRecord.TIMEZONE))));
            //build the JSON structures
            try {      	
            		//bedTime
            		//myDay.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(SleepRecord.START_OF_NIGHT)));
            		//jBedTime.put("day", myDay.get(Calendar.DATE));
            		//jBedTime.put("hour", myDay.get(Calendar.HOUR_OF_DAY));
            		//jBedTime.put("minute", myDay.get(Calendar.MINUTE));
            		//jBedTime.put("month", myDay.get(Calendar.MONTH));
            		//jBedTime.put("second", myDay.get(Calendar.SECOND));
            		//jBedTime.put("year", myDay.get(Calendar.YEAR));
            		//jBedTime.put("startOfNight", cursor.getLong(cursor.getColumnIndex(SleepRecord.START_OF_NIGHT)));
            		//jSleep.put("bedTime", jBedTime);
            	
            		jSleep.put("bedTime", cursor.getLong(cursor.getColumnIndex(SleepRecord.START_OF_NIGHT)));
            		
            		//startDate
            		//jStartDate.put("day", myDay.get(Calendar.DATE));
            		//jStartDate.put("month", myDay.get(Calendar.MONTH));
            		//jStartDate.put("year", myDay.get(Calendar.YEAR));
            		//jSleep.put("startDate", jStartDate);
             
            		//graphStart - only when pulling from web
            		//jGraphStart.put("day", myDay.get(Calendar.DATE));
            		//jGraphStart.put("hour", myDay.get(Calendar.HOUR_OF_DAY));
            		//jGraphStart.put("minute", myDay.get(Calendar.MINUTE));
            		//jGraphStart.put("month", myDay.get(Calendar.MONTH));
            		//jGraphStart.put("second", myDay.get(Calendar.SECOND));
            		//jGraphStart.put("year", myDay.get(Calendar.YEAR));
                	//jSleep.put("sleepGraphStartTime", jGraphStart);
   
            		//riseTime
            		//myDay.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(SleepRecord.END_OF_NIGHT)));
            		//jRiseTime.put("endOfNight", cursor.getLong(cursor.getColumnIndex(SleepRecord.END_OF_NIGHT)));
            		//jRiseTime.put("day", myDay.get(Calendar.DATE));
            		//jRiseTime.put("hour", myDay.get(Calendar.HOUR_OF_DAY));
            		//jRiseTime.put("minute", myDay.get(Calendar.MINUTE));
            		//jRiseTime.put("month", myDay.get(Calendar.MONTH));
            		//jRiseTime.put("second", myDay.get(Calendar.SECOND));
            		//jRiseTime.put("year", myDay.get(Calendar.YEAR));
            		//jSleep.put("riseTime", jRiseTime);
            	
            		jSleep.put("endOfNight", cursor.getLong(cursor.getColumnIndex(SleepRecord.END_OF_NIGHT)));
            		
            		jSleep.put("awakenings", cursor.getInt(cursor.getColumnIndex(SleepRecord.AWAKENINGS)));
            		
            		//jSleep.put("awakeningsZqPoints", 0);  //only from web
            		//jSleep.put("grouping", "DAILY");  //only from web
            		//jSleep.put("morningFeel", 0);  //only from web
            		//jSleep.put("timeInDeepZqPoints", 0);  //only from web
            		//jSleep.put("timeInRemZqPoints", 0);  //only from web
            		//jSleep.put("timeInWakeZqPoints", 0);  //only from web
            		//jSleep.put("totalZZqPoints", 0);  //only from web
            		
            		totSleep = cursor.getInt(cursor.getColumnIndex(SleepRecord.TOTAL_Z));
           		
            		sleepAmt = cursor.getInt(cursor.getColumnIndex(SleepRecord.TIME_IN_DEEP));
            		sleepPerc = Math.round((sleepAmt/totSleep)*100.0);
            		jSleep.put("timeInDeep", sleepAmt); 
            		jSleep.put("timeInDeepPercentage", sleepPerc);
            		
            		sleepAmt = cursor.getInt(cursor.getColumnIndex(SleepRecord.TIME_IN_LIGHT));
            		sleepPerc = Math.round((sleepAmt/totSleep)*100.0);
            		jSleep.put("timeInLight", sleepAmt);
            		jSleep.put("timeInLightPercentage", sleepPerc);
            		
            		sleepAmt = cursor.getInt(cursor.getColumnIndex(SleepRecord.TIME_IN_REM));
            		sleepPerc = Math.round((sleepAmt/totSleep)*100.0);
            		jSleep.put("timeInRem", sleepAmt);
            		jSleep.put("timeInRemPercentage", sleepPerc);
            		
            		sleepAmt = cursor.getInt(cursor.getColumnIndex(SleepRecord.TIME_IN_WAKE));
            		sleepPerc = Math.round((sleepAmt/totSleep)*100.0);
            		jSleep.put("timeInWake", sleepAmt);
            		jSleep.put("timeInWakePercentage", sleepPerc);
            		
            		jSleep.put("timeToZ", cursor.getInt(cursor.getColumnIndex(SleepRecord.TIME_TO_Z)));
            		jSleep.put("totalZ", totSleep);
            		jSleep.put("zq", cursor.getInt(cursor.getColumnIndex(SleepRecord.ZQ_SCORE)));
            		
            		//jSleep.put("alarmReason", "NO_ALARM");  //only from web
            		//jSleep.put("alarmRingIndex", 0);  //only from web
            		//jSleep.put("dayFeel", 0);  //only from web
                	//jSleep.put("sleepStealerScore", 0);  //only from web
                	//jSleep.put("wakeWindowEndIndex", 0);  //only from web
                	//jSleep.put("sleewakeWindowShowpStealerScore", 0);  //only from web
                	//jSleep.put("wakeWindowStartIndex", 0);  //only from web           		
            }
            catch (JSONException je) {
            	//ignore
                Toast.makeText(this, "Error with the JSON", Toast.LENGTH_LONG).show();
            }
*/            
/*           	
                // Output the display hypnogram - 5 min slices
                final byte[] displayHypnogram =
                    cursor.getBlob(cursor.getColumnIndex(SleepRecord.DISPLAY_HYPNOGRAM));
                for (byte stage : displayHypnogram) {
                	try {
                		//jSleep.accumulate(SleepRecord.DISPLAY_HYPNOGRAM, Byte.toString(stage)); 
                		jSleep.accumulate(SleepRecord.DISPLAY_HYPNOGRAM, stage); 
                	}
                	catch (JSONException je) {
                		//ignore
                        Toast.makeText(this, "Error with the JSON 2", Toast.LENGTH_LONG).show();
                	}
                 }
                
                // Output the base hypnogram - 30 sec slices
                final byte[] baseHypnogram =
                    cursor.getBlob(cursor.getColumnIndex(SleepRecord.BASE_HYPNOGRAM));
                for (byte stage : baseHypnogram) {
                	try {
                		//jSleep.accumulate(SleepRecord.BASE_HYPNOGRAM, Byte.toString(stage));
                		jSleep.accumulate(SleepRecord.BASE_HYPNOGRAM, stage);
                	}
                	catch (JSONException je) {
                		//ignore
                        Toast.makeText(this, "Error with the JSON 3", Toast.LENGTH_LONG).show();
                	}
                }
 */  
            
                //add to jSleep
            //try {
                	//jRecord.put("sleep record", jSleep);
                	//jSleepRecord.put("records", jRecord);  //not really necessary
//                	processZeoData(jSleep);
            //}
            //catch (JSONException je) {
            		//ignore
                    //Toast.makeText(this, "Error with the JSON 4", Toast.LENGTH_LONG).show();
            //}
            	
        } 
        else {
            Log.w(LMFforAndroid.tag, "No sleep records found.");
            Toast.makeText(this, "No sleep records found in the provider.", Toast.LENGTH_SHORT).show();
            builder = "No sleep records found";
        }

        cursor.close();
        return builder;
     }
    
    @Override
    public void onResume() {
        super.onResume();

        TextView csvSleep = (TextView) findViewById(R.id.csvSleep);
        
        mySleep = buildCsv();
    	if (mySleep == null || mySleep.equals("")) {
            // Warn user that there is no data and abort prematurely.
        	csvSleep.setText("No sleep records found!");
            Toast.makeText(ZeoSleep.this,
                           "Sorry, there is no data to send.",
                           Toast.LENGTH_SHORT).show();
            return;
        } else {
        	csvSleep.setText(mySleep);                	
        }
/*
        Intent intent = new Intent(Intent.ACTION_SEND);
        final long now = System.currentTimeMillis();
        final String currentDateTime =
            DateUtils.formatDateTime(ZeoSleep.this,
                                     now,
                                     DateUtils.FORMAT_SHOW_DATE |
                                     DateUtils.FORMAT_SHOW_TIME |
                                     DateUtils.FORMAT_SHOW_YEAR);
        // Add the data header
        intent.putExtra(Intent.EXTRA_SUBJECT, "My Zeo sleep data as of " +
                        currentDateTime + ".");
        // Add the body text
        StringBuilder builder = new StringBuilder();               
        builder.append("My average ZQ is " + getAvgZQ() + ".\n\n");
        //builder.append("My normal bedtime is " + getRunBed() + ".\n\n");

        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        intent.setType("text/plain");

        startActivity(Intent.createChooser(intent, "Share Sleep Data"));
        finish();
*/
    }
    
    public void processZeoData(JSONObject jSleepRecord) {
    	// Process Zeo data

    	//create event
    	Event sleepEvent = new Event("smartphone", "zeo_sleep_record");
    	sleepEvent.addAttribute("ZeoSleepRecord", jSleepRecord);
    	//send event
    	sendEvent(sleepEvent);
    }

    private void processSleepData(int totAvg, int count, int threeAvg) {
    	Event sleepDataEvent = new Event("smartphone", "zq_avg_score");
    	sleepDataEvent.addAttribute("total_avg", totAvg);
    	sleepDataEvent.addAttribute("number_days", count);
    	sleepDataEvent.addAttribute("three_avg", threeAvg);
    	
    	//this will return the values to the PEN
    	sendEvent(sleepDataEvent);
    }

    private void sendEvent(Event event){
    	// Execute HTTP Post Request
    	try {
    		final SharedPreferences settings = getSharedPreferences(LMFforAndroid.PREFS_NAME, 0);
    		String signalurl = settings.getString("CID_url", "notpresent");
            TextView csvSleep = (TextView) findViewById(R.id.csvSleep);
            csvSleep.setText(signalurl);
            
    		//if it isn't present need to do something
    		
    		// Create a new HttpClient and Post Header
    		HttpClient httpclient = new DefaultHttpClient();
    		HttpPost httppost = new HttpPost(signalurl);
    		
    		httppost.setEntity(event.asEntity());
    		HttpResponse response = httpclient.execute(httppost);
    		Log.i(LMFforAndroid.tag, "Raised Event: "+event.typeName()+"  response: " + response);
    	} catch (ClientProtocolException e) {
    		Log.e(LMFforAndroid.tag, "protocol error!");
    	} catch (IOException e) {
    		Log.e(LMFforAndroid.tag, "io error!");
    	}
    }
/*
    private void sendReceiveEvent(Event event){
    	// Execute HTTP Post Request

        try {
    		final SharedPreferences settings = getSharedPreferences(LMFforAndroid.PREFS_NAME, 0);
    		String signalurl = settings.getString("CID_url", "notpresent");
            HttpClient client = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(signalurl);
/*   		
    		httppost.setEntity(event.asEntity());
    	    HttpResponse response = client.execute(httppost);
    	    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    	      String line = "";
    	      while ((line = rd.readLine()) != null) {
    	        System.out.println(line);
    	        if (line.startsWith("Auth=")) {
    	          String key = line.substring(5);
    	          // Do something with the key
    	        }
re-end comment here 
    		
    		httppost.setEntity(event.asEntity());
    		HttpResponse response = client.execute(httppost);
    		Log.i(LMFforAndroid.tag, "Raised Event: "+event.typeName()+"  response: " + response);
    	} catch (ClientProtocolException e) {
    		Log.e(LMFforAndroid.tag, "protocol error!");
    	} catch (IOException e) {
    		Log.e(LMFforAndroid.tag, "io error!");
    	}
    }
*/

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

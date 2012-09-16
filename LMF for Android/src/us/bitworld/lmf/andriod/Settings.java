package us.bitworld.lmf.andriod;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class Settings extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Toast.makeText(getApplicationContext(), "before config", Toast.LENGTH_SHORT).show();
        
        setContentView(R.layout.config);
		Toast.makeText(getApplicationContext(), "after config", Toast.LENGTH_SHORT).show();

    }
}

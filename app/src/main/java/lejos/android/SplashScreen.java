package lejos.android;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
 
public class SplashScreen extends Activity {

    final Context context = this;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
 
        // Laitetaan äänet päälle. Käyttäjä pystyy itse myöhemmin poistaa ääni efektit
		SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
		SharedPreferences.Editor editor = myPrefs.edit();
		editor.putString("Sound", "On");
		editor.apply();
		
        // Soitetaan lyhyt ääniklippi
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.kuka_muu_muka);
        mediaPlayer.start();
                
        // 3 sekunnin "Splash screen"
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent i = new Intent(SplashScreen.this, LeJOSDroid.class);
                startActivity(i);
                finish();
            }
        }, 3000);
    }
    
    
    
    
}
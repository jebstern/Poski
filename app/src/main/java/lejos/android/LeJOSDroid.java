package lejos.android;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class LeJOSDroid extends Activity {

	public enum CONN_TYPE {
		LEJOS_PACKET, LEGO_LCP
	}

	public static final String MESSAGE_CONTENT = "String_message";
	public static final int MESSAGE = 1000;
	public static final int TOAST = 2000;

	private BTSend btSend;
	private TextView red_value;
	private TextView green_value;
	private TextView blue_value;
	private TextView yellow_value;
	private TextView total_value;
	private TextView bt_status;

	private CheckBox sounds_box;

	private int red = 0;
	private int green = 0;
	private int blue = 0;
	private int yellow = 0;
	private int total_balls = 0;

	private final static int REQUEST_ENABLE_BT = 1;
	private TextView _message;

	public static UIMessageHandler mUIMessageHandler;
	private final static String TAG = "LeJOSDroid";

	@SuppressLint("SetTextI18n")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mUIMessageHandler = new UIMessageHandler();
		setContentView(R.layout.main);
		_message = (TextView) findViewById(R.id.messageText);

		setupBTSend();


		bt_status = (TextView) findViewById(R.id.btstatus);
		red_value = (TextView) findViewById(R.id.red_value);
		green_value = (TextView) findViewById(R.id.green_value);
		blue_value = (TextView) findViewById(R.id.blue_value);
		yellow_value = (TextView) findViewById(R.id.yellow_value);
		total_value = (TextView) findViewById(R.id.total_value);

		sounds_box = (CheckBox) findViewById(R.id.enable_sound);
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter == null) { // Puhelimessa ei ole Bluetooth
			bt_status.setTextColor(Color.parseColor("#FF0000"));
			bt_status.setText("Puhelimesi ei tue Bluetoothia --> Sovellus ei toimi!");
		}

		if ( mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) { // BlueTooth ei ole päällää
			bt_status.setTextColor(Color.parseColor("#FF0000"));
			bt_status.setText("Bluetooth ei ole päällä --> Sovellus ei toimi");
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) { // BlueTooth ei ole pällää
			bt_status.setTextColor(Color.parseColor("#4B9903"));
			bt_status.setText("Bluetooth päällä.");
		}

		// Luodaan BlueTooth kuuntelija. Huomautetaan käyttäjää mikäli BT ei ole
		// pällä/BT sammutetaan
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (btSend != null) {
			Log.d(TAG, "onPause() closing btSend ");
			btSend.closeConnection();
			btSend = null;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		this.unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Toast.makeText(getApplicationContext(), "Group 7: Ville Kerminen, Ilari Raijas, Henrik Raitasola, Jesper Skand. Android app made for Ammatillisesti suuntaava projekti @ 2014",Toast.LENGTH_LONG).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static NXTConnector connect(final CONN_TYPE connection_type) {
		Log.d(TAG, " about to add LEJOS listener ");

		NXTConnector conn = new NXTConnector();
		conn.setDebug(true);
		conn.addLogListener(new NXTCommLogListener() {

			public void logEvent(String arg0) {
				Log.e(TAG + " NXJ log:", arg0);
			}

			public void logEvent(Throwable arg0) {
				Log.e(TAG + " NXJ log:", arg0.getMessage(), arg0);
			}
		});

		switch (connection_type) {
		case LEGO_LCP:
			conn.connectTo("btspp://NXT", NXTComm.LCP);
			break;
		case LEJOS_PACKET:
			conn.connectTo("btspp://Ryhma_7b");
			break;
		}

		return conn;

	}

	class UIMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MESSAGE:
				String vari = (String) msg.getData().get(MESSAGE_CONTENT);

				if ("Punainen".equalsIgnoreCase(vari)) {
					setColorValue("red");
				} else if ("Vihreä".equalsIgnoreCase(vari)) {
					setColorValue("green");
				} else if ("Sininen".equalsIgnoreCase(vari)) {
					setColorValue("blue");
				} else if ("Keltainen".equalsIgnoreCase(vari)) {
					setColorValue("yellow");
				}

				break;

			case TOAST:
				showToast((String) msg.getData().get(MESSAGE_CONTENT));
				break;
			}

			_message.setVisibility(View.VISIBLE);
			_message.requestLayout();

		}

	}

	public static void displayToastOnUIThread(String message) {
		Message message_holder = formMessage(message);
		message_holder.what = LeJOSDroid.TOAST;
		mUIMessageHandler.sendMessage(message_holder);
	}

	private static Message formMessage(String message) {
		Bundle b = new Bundle();
		b.putString(LeJOSDroid.MESSAGE_CONTENT, message);
		Message message_holder = new Message();
		message_holder.setData(b);
		return message_holder;
	}

	public static void sendMessageToUIThread(String message) {
		Message message_holder = formMessage(message);
		message_holder.what = LeJOSDroid.MESSAGE;
		mUIMessageHandler.sendMessage(message_holder);
	}

	private void setupBTSend() {
		Button button;
		button = (Button) findViewById(R.id.button2);
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				Toast.makeText(getApplicationContext(),
						"Muodostetaan yhteys robottiin...", Toast.LENGTH_LONG)
						.show();
				try {
					btSend = new BTSend(mUIMessageHandler);
					btSend.start();
				} catch (Exception e) {
					Log.e(TAG, "failed to run BTSend:" + e.getMessage(), e);
				}

			}
		});
	}

	private void showToast(String textToShow) {
        Toast.makeText(this, textToShow, Toast.LENGTH_SHORT).show();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        @Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				switch (state) {
				case BluetoothAdapter.STATE_OFF:
					bt_status.setTextColor(Color.parseColor("#FF0000"));
					bt_status
							.setText("Bluetooth pois päältä --> Sovellus ei toimi");
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					bt_status.setTextColor(Color.parseColor("#FF0000"));
					bt_status.setText("Bluetooth suljetaan...");
					break;
				case BluetoothAdapter.STATE_ON:
					bt_status.setTextColor(Color.parseColor("#4B9903"));
					bt_status.setText("Bluetooth päällä.");
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					bt_status.setTextColor(Color.parseColor("#4B9903"));
					bt_status.setText("Bluetooth käynnistetään...");
					break;
				}
			}
		}
	};

	@SuppressLint("SetTextI18n")
    public void setSound(View v) {

		if (sounds_box.isChecked()) {
			sounds_box.setText("äänet päällä");
			SharedPreferences myPrefs = this.getSharedPreferences("myPrefs",MODE_PRIVATE);
			SharedPreferences.Editor editor = myPrefs.edit();
			editor.putString("Sound", "On");
			editor.apply();
		} else {
			sounds_box.setText("äänet pois päältä");
			SharedPreferences myPrefs = this.getSharedPreferences("myPrefs",MODE_PRIVATE);
			SharedPreferences.Editor editor = myPrefs.edit();
			editor.putString("Sound", "off");
			editor.apply();
		}

	}




	@SuppressLint("SetTextI18n")
    public void setColorValue(String color) {
		if (color.equalsIgnoreCase("red")) {
			red++;
			red_value.setText("" + red);
		}
		if (color.equalsIgnoreCase("green")) {
			green++;
			green_value.setText("" + green);
		}
		if (color.equalsIgnoreCase("blue")) {
			blue++;
			blue_value.setText("" + blue);
		}
		if (color.equalsIgnoreCase("yellow")) {
			yellow++;
			yellow_value.setText("" + yellow);
		}
		total_balls++;
		total_value.setText("" + total_balls);

		SharedPreferences myPrefs = this.getSharedPreferences("myPrefs",MODE_PRIVATE);
		String sounds = myPrefs.getString("Sound", "");


		if (sounds.equalsIgnoreCase("on")) {
			if (total_balls % 6 == 0) {
				MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(),
						R.raw.sokka_irti);
				mediaPlayer.start();
			}

			if (total_balls % 9 == 0) {
				MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(),
						R.raw.kuka_muu_muka);
				mediaPlayer.start();
			}

			if (total_balls % 20 == 0) {
				MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(),
						R.raw.timantit_on_ikuisia);
				mediaPlayer.start();
			}
		}

	}

}

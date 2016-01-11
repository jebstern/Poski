package lejos.android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.android.LeJOSDroid.CONN_TYPE;
import lejos.pc.comm.NXTConnector;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BTSend extends Thread {
    static final String TAG = "BTSend";
    private NXTConnector conn;
    Handler mUIMessageHandler;
    DataOutputStream dos;
    DataInputStream dis;

    public BTSend(Handler mUIMessageHandler) {

        super();
        this.mUIMessageHandler = mUIMessageHandler;
    }

    public void closeConnection() {
        try {
            Log.d(TAG, "BTSend run loop finished and closing");

            dis.close();
            dos.close();
            conn.getNXTComm().close();
        } catch (Exception ignored) {
        } finally {
            dis = null;
            dos = null;
            conn = null;
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "BTSend run");
        Looper.prepare();

        conn = LeJOSDroid.connect(CONN_TYPE.LEJOS_PACKET);

        dos = conn.getDataOut();
        dis = conn.getDataIn();

        if (dis == null) {
            Log.d(TAG, "dis is null");
            LeJOSDroid.displayToastOnUIThread("Ei saatu yhteyttä robottiin, yritä uudelleen.");
        } else {
            LeJOSDroid.displayToastOnUIThread("Yhteys muodostettu robottiin.");
            String s;
            for (int i = 0; i < 100; i++) {
                try {
                    s = dis.readUTF();
                    LeJOSDroid.sendMessageToUIThread(s);
                    yield();
                } catch (IOException e) {
                    Log.e(TAG, "Error ... ", e);
                }
            }
            closeConnection();
            Looper.loop();


            Looper.myLooper().quit();


        }
    }

}

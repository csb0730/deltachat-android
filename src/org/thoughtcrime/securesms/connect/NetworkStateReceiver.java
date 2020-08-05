package org.thoughtcrime.securesms.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            ApplicationDcContext dcContext = DcHelper.getContext(context);

            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Log.i("DeltaChat", "++++++++++++++++++ Connected ++++++++++++++++++");
                new Thread(() -> {
                    // call dc_maybe_network(1) from a worker thread.
                    // cs: status 1 sets online state and leads to real working of dc_maybe_online()
                    // theoretically, dc_maybe_network() can be called from the main thread and returns at once,
                    // however, in reality, it does currently halt things for some seconds.
                    // this is a workaround that make things usable for now.
                    //Log.i("DeltaChat", "calling maybeNetwork() - Connected");
                    dcContext.maybeNetwork(1);
                    //Log.i("DeltaChat", "maybeNetwork() returned - Connected");
                }).start();
            }
            else {
                Log.i("DeltaChat", "++++++++++++++ Not connected ++++++++++++++++++");
                // call dc_maybe_network(0) with status 0 only sets online state and returns immediately
                dcContext.maybeNetwork(0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.spacebanana.funwithgeofence;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkStateManager {
    private Context context;
    private String networkName;
    private ConnectivityManager connectivityManager;

    public NetworkStateManager(Context context) {
        this.context = context;
    }

    public void subscribeOnNetworkChanges(String networkSSID) {
        if (networkSSID == null || networkSSID.isEmpty()) return;

        networkName = networkSSID;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(
                    builder.build(),
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            checkNetworkNameAndProceed(true);
                        }

                        @Override
                        public void onLost(Network network) {
                            checkNetworkNameAndProceed(false);
                        }
                    }
            );
        }
    }

    private void checkNetworkNameAndProceed(boolean isConnected) {
        if (connectivityManager == null) return;

        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager == null) return;

            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !connectionInfo.getSSID().isEmpty()) {
                String ssid = connectionInfo.getSSID();
                if (ssid.equals(networkName)) {

                }
            }
        }
    }
}

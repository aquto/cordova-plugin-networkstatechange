package com.aquto.cordova.networkstate;

import android.util.Log;
import org.apache.cordova.*;
import android.net.*;
import android.content.*;
import android.content.Intent;
import org.json.*;

public class NetworkStateMonitor extends CordovaPlugin {

    private final class PluginActions {
        public static final String GET_STATE = "getState";
        public static final String REGISTER_CALLBACK = "registerCallback";
        public static final String UNREGISTER_CALLBACK = "unregisterCallback";
    }

    private final class NetworkTypes {
        public static final String NONE_TYPE = "NONE";
        public static final String WIFI_TYPE = "WIFI";
        public static final String MOBILE_TYPE = "MOBILE";
        public static final String UNKNOWN_TYPE = "UNKNOWN";
    }

    private static final String TAG = NetworkStateMonitor.class.getSimpleName();
    private NetworkStateReceiver nsr;
    private ConnectivityManager connMan;
    private Context ctx;

    private class NetworkStateReceiver extends BroadcastReceiver {

        private CallbackContext callback;
        private int prevState;

        public NetworkStateReceiver(CallbackContext callbackContext) {
            callback = callbackContext;
            NetworkInfo info = connMan.getActiveNetworkInfo();
            prevState = toIntState(info);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = connMan.getActiveNetworkInfo();
            if(!hasStateChanged(info))
                return;
            prevState = toIntState(info);
            String typeString = typeNameToString(info);
            PluginResult pr = new PluginResult(PluginResult.Status.OK, typeString);
            pr.setKeepCallback(true);
            callback.sendPluginResult(pr);
            Log.d(TAG, "Network transitioned to " + typeString);
        }

        private int toIntState(NetworkInfo info) {
            return (info != null ? info.getType() : -1);
        }

        private boolean hasStateChanged(NetworkInfo info) {
            int currState = toIntState(info);
            return currState != prevState;
        }
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        ctx = webView.getContext();
        connMan = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void onDestroy() {
        doUnregisterListener();
    }

    private String typeNameToString(NetworkInfo info) {
        if(info == null)
            return NetworkTypes.NONE_TYPE;
        switch(info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return NetworkTypes.WIFI_TYPE;
            case ConnectivityManager.TYPE_MOBILE:
                return NetworkTypes.MOBILE_TYPE;
            default:
                return NetworkTypes.UNKNOWN_TYPE;
        }
    }

    private PluginResult handleGetState() {
        NetworkInfo info = connMan.getActiveNetworkInfo();
        String typeString = typeNameToString(info);
        Log.d(TAG, "Network is " + typeString);
        return new PluginResult(PluginResult.Status.OK, typeString);
    }

    private PluginResult handleRegisterCallbackAction(CallbackContext callbackContext) {
        doUnregisterListener();
        nsr = new NetworkStateReceiver(callbackContext);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        ctx.registerReceiver(nsr, filter);
        Log.d(TAG, "Registered callback");
        PluginResult pr = new PluginResult(PluginResult.Status.OK, true);
        pr.setKeepCallback(true);
        return pr;
    }

    private void doUnregisterListener() {
        if(nsr != null) {
            ctx.unregisterReceiver(nsr);
            nsr = null;
            Log.d(TAG, "Unregistered callback");
        }        
    }

    private PluginResult handleUnregisterCallbackAction() {
        doUnregisterListener();
        return new PluginResult(PluginResult.Status.OK, true);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if(action.equals(PluginActions.GET_STATE))
            callbackContext.sendPluginResult(handleGetState());
        else if(action.equals(PluginActions.REGISTER_CALLBACK))
            callbackContext.sendPluginResult(handleRegisterCallbackAction(callbackContext));
        else if(action.equals(PluginActions.UNREGISTER_CALLBACK))
            callbackContext.sendPluginResult(handleUnregisterCallbackAction());
        else
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION, ""));
        return true;
    }
}
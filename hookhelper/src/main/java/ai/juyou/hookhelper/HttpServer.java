package ai.juyou.hookhelper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import ai.juyou.hookhelper.nanohttpd.webserver.SimpleWebServer;

public class HttpServer {
    private Thread thread;
    private String dir;
    public HttpServer(Context context) {
        SimpleWebServer.init(context,true);
        String ipAddress = getIpAddress(context);
        Log.d("CameraHook","Server ip:" + ipAddress);
        dir = context.getCacheDir().getAbsolutePath();
        Log.d("CameraHook","Dir "+ dir);
    }

    public static void start(Context context){

    }

    private void start() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleWebServer.runServer(new String[]{"-h","0.0.0.0","-p","8080","-d",dir});
            }
        });
        thread.start();
    }

    private static String getIpAddress(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities != null) {
                if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    return intIP2StringIP(wifiInfo.getIpAddress());
                }
            }
        }
        return null;
    }

    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}

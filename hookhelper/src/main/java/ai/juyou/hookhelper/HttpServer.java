package ai.juyou.hookhelper;

import static ai.juyou.hookhelper.nanohttpd.core.protocols.http.response.Response.newFixedLengthResponse;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.LogRecord;

import ai.juyou.hookhelper.nanohttpd.core.protocols.http.IHTTPSession;
import ai.juyou.hookhelper.nanohttpd.core.protocols.http.NanoHTTPD;
import ai.juyou.hookhelper.nanohttpd.core.protocols.http.response.Response;
import ai.juyou.hookhelper.nanohttpd.webserver.SimpleWebServer;

public class HttpServer  extends NanoHTTPD {
    private Thread thread;
    private String dir;
    private Activity activity;
    private Handler handler;
    private String treeMsg="";
    public HttpServer(Activity activity) {
        super(8080);
        //SimpleWebServer.init(context,true);
        String ipAddress = getIpAddress(activity);
        Log.d("CameraHook","Server ip:" + ipAddress);
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(android.os.Message msg) {
                Log.d("CameraHook","handleMessage");
                if(msg.what == 100) {
                    //treeMsg = (String) msg.obj;
                    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                    ViewTree viewTree = HookHelper.getViewTree(decorView);
                    treeMsg = viewTree.toString();
                }
            }
        };
        //dir = context.getCacheDir().getAbsolutePath();
        //Log.d("CameraHook","Dir "+ dir);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //SimpleWebServer.runServer(new String[]{"-h","0.0.0.0","-p","8080","-d",dir});
                try {
                    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                } catch (IOException e) {

                }
            }
        });
        thread.start();

    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>Hello server</h1>\n";
        Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }
        handler.obtainMessage(100).sendToTarget();
        msg += "<p>Tree: " + treeMsg + "</p>";
        return newFixedLengthResponse(msg + "</body></html>\n");
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

package ai.juyou.remotecamera;

public abstract class CameraPullCallback {
    public void onConnected()
    {

    }
    public void onConnectFailed()
    {

    }
    public void onDisconnected()
    {

    }
    public void onReceived(byte[] data)
    {

    }
    public void onError(Exception e)
    {

    }
}

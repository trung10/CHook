package ai.juyou.remotecamera;

public abstract class CameraPullCallback {
    public void onConnected(VideoDecoder videoDecoder)
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

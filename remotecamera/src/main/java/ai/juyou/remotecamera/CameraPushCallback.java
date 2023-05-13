package ai.juyou.remotecamera;

public abstract class CameraPushCallback {

    public void onConnected(VideoEncoder videoEncoder)
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

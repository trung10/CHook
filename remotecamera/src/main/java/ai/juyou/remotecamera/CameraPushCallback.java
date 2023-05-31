package ai.juyou.remotecamera;
abstract class CameraPushCallback {

    public void onConnected()
    {

    }
    public void onConnectFailed(Throwable e)
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

package ai.juyou.remotecamera;

import java.net.InetAddress;

class ServerConfig {
    private InetAddress mServerAddress;
    private int mServerPort;

    public ServerConfig(String address, int port)
    {
        try {
            this.mServerAddress = InetAddress.getByName(address);
        } catch (Exception e) {
        }
        this.mServerPort = port;
    }

    public InetAddress getServerAddress() {
        return mServerAddress;
    }

    public int getServerPort() {
        return mServerPort;
    }
}

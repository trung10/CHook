package ai.juyou.remotecamera;

import android.media.Image;

public interface CameraPushSession {
    void encode(Image image);
}

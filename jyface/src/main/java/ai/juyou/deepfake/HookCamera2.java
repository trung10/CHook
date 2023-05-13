package ai.juyou.deepfake;

import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;

import java.nio.ByteBuffer;

import ai.juyou.remotecamera.CameraDecoder;
import ai.juyou.remotecamera.CameraEncoder;
import ai.juyou.remotecamera.PullCallback;
import ai.juyou.remotecamera.PushCallback;
import ai.juyou.remotecamera.RemoteCamera;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCamera2 {
    private static final String TAG = "CameraHook";
    private final ImageReader.OnImageAvailableListener mHookImageAvailableListener;
    private final RemoteCamera mRemoteCamera;
    private ImageReader mOriginImageReader;
    private ImageReader.OnImageAvailableListener mOriginImageAvailableListener;
    private CameraEncoder mCameraEncoder;
    private CameraDecoder mCameraDecoder;

    public HookCamera2() {
        mRemoteCamera = new RemoteCamera();
        mRemoteCamera.setPushCallback(new PushCallback() {
            @Override
            public void onConnected(CameraEncoder cameraPush) {
                mCameraEncoder = cameraPush;
            }

            @Override
            public void onDisconnect() {
                mCameraEncoder = null;
            }
        });

        mRemoteCamera.setPullCallback(new PullCallback() {
            @Override
            public void onConnected(CameraDecoder cameraDecoder) {
                mCameraDecoder = cameraDecoder;
            }

            @Override
            public void onDisconnect() {
                mCameraDecoder = null;
            }
        });

        mHookImageAvailableListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                //Log.d(TAG,"onImageAvailable :" + reader);
                if(mOriginImageAvailableListener!=null){
                    mOriginImageAvailableListener.onImageAvailable(reader);
                }
            }
        };
    }

    public void hook(XC_LoadPackage.LoadPackageParam lpParam) {
        try {
            XposedHelpers.findAndHookMethod(ImageReader.class,"setOnImageAvailableListener", ImageReader.OnImageAvailableListener.class, Handler.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ImageReader.OnImageAvailableListener listener = (ImageReader.OnImageAvailableListener)param.args[0];

                    if(listener != null){
                        mOriginImageReader = (ImageReader)param.thisObject;
                        mOriginImageAvailableListener = listener;
                        param.args[0] = mHookImageAvailableListener;
                        mRemoteCamera.Open(new Size(mOriginImageReader.getWidth(), mOriginImageReader.getHeight()));
                    }
                    else{
                        if(param.thisObject== mOriginImageReader){
                            mOriginImageReader = null;
                            mOriginImageAvailableListener = null;
                            mRemoteCamera.Close();
                        }
                    }
                }
            });
            XposedHelpers.findAndHookMethod(ImageReader.class,"acquireLatestImage", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    //param.setResult(null);

                    if(param.thisObject==mOriginImageReader)
                    {
                        Image image = (Image)param.getResult();
                        if(image!=null){
                            if(mCameraEncoder != null) {
                                mCameraEncoder.encode(image);
                            }
                            if(mCameraDecoder != null) {
                                mCameraDecoder.decode(image);
                            }
                            else{
                                clearImage(image);
                            }
                            resetPosition(image);
                        }
                    }
                }
            });

            XposedHelpers.findAndHookMethod(ImageReader.class,"acquireNextImage", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG,"acquireNextImage :" + param.getResult());
                }
            });

        }
        catch (Exception e)
        {
            Log.e(TAG,"",e);
        }
    }

    private void clearImage(Image image)
    {
        Image.Plane[] planes = image.getPlanes();
        int size =0;
        for (int i = 1; i < planes.length; i++) {
            ByteBuffer buffer = planes[i].getBuffer();
            buffer.position(0);
            byte[] zeroBytes = new byte[buffer.remaining()];
            buffer.put(zeroBytes);
            size= size + zeroBytes.length;
        }
    }

    private void resetPosition(Image image)
    {
        Image.Plane[] planes = image.getPlanes();
        for (int i = 0; i < planes.length; i++) {
            ByteBuffer buffer = planes[i].getBuffer();
            buffer.position(0);
        }
    }
}
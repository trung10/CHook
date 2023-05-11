package ai.juyou.deepfake;

import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

import ai.juyou.remotecamera.CameraEncoder;
import ai.juyou.remotecamera.PushCallback;
import ai.juyou.remotecamera.RemoteCamera;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCamera2 {

    private static final String TAG = "CameraHook";




    private ImageReader mImageReader;
    private ImageReader.OnImageAvailableListener mHookImageAvailableListener;
    private ImageReader.OnImageAvailableListener mOriginImageAvailableListener;

    private RemoteCamera mRemoteCamera;
    private CameraEncoder mCameraPush;

    public HookCamera2() {
        mRemoteCamera = new RemoteCamera();
        mRemoteCamera.setPushCallback(new PushCallback() {
            @Override
            public void onConnected(CameraEncoder cameraPush) {
                mCameraPush = cameraPush;
            }

            @Override
            public void onDisconnect() {
                mCameraPush = null;
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

    List<String> list = new ArrayList<>();

    public void hook(XC_LoadPackage.LoadPackageParam lpParam) {
//        XposedHelpers.findAndHookMethod(CameraManager.class,"openCamera", String.class, CameraDevice.StateCallback.class, Handler.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                String cameraId = (String)param.args[0];
//                Log.d(TAG, "openCamera: " + cameraId);
//                mOriginCameraDeviceStateCallback = (CameraDevice.StateCallback)param.args[1];
//                param.args[1] = mHookCameraDeviceStateCallback;
//            }
//        });

//        try {
//            XposedHelpers.findAndHookMethod("android.hardware.camera2.impl.CameraDeviceImpl", lpParam.classLoader, "createCaptureSession",SessionConfiguration.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    Object obj = param.args[0];
//                    Log.d(TAG, "createCaptureSession: " + obj);
//                }
//            });
//        }
//        catch (Exception e)
//        {
//            Log.e(TAG,"",e);
//        }

        try {
//            XposedHelpers.findAndHookMethod("android.hardware.camera2.impl.CameraDeviceImpl", lpParam.classLoader, "createCaptureSession",List.class,CameraCaptureSession.StateCallback.class, Handler.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    Object obj = param.args[0];
//                    Log.d(TAG, "createCaptureSession: " + obj);
//                    mOriginCameraCaptureSessionStateCallback = (CameraCaptureSession.StateCallback)param.args[1];
//                    param.args[1] = mHookCameraCaptureSessionStateCallback;
//                }
//            });
//
//            XposedHelpers.findAndHookMethod(CaptureRequest.Builder.class,"addTarget", Surface.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    Surface surface = (Surface)param.args[0];
//                    Log.d(TAG, "addTarget: " + surface);
//                }
//            });

            XposedHelpers.findAndHookMethod(ImageReader.class,"setOnImageAvailableListener", ImageReader.OnImageAvailableListener.class, Handler.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "setOnImageAvailableListener "+param.thisObject);
                    ImageReader.OnImageAvailableListener listener = (ImageReader.OnImageAvailableListener)param.args[0];

                    if(listener != null){
                        mImageReader = (ImageReader)param.thisObject;
                        mOriginImageAvailableListener = listener;
                        Log.d(TAG, "Hook is OK");
                        param.args[0] = mHookImageAvailableListener;
                        Log.d(TAG, "setOnImageAvailableListener: " + mImageReader.getWidth() + " " + mImageReader.getHeight() + " " + mImageReader.getImageFormat() + " " +param.thisObject);
                        mRemoteCamera.Open(new Size(mImageReader.getWidth(),mImageReader.getHeight()));
                    }
                    else{
                        if(param.thisObject==mImageReader){
                            mImageReader = null;
                            mOriginImageAvailableListener = null;
                            Log.d(TAG, "setOnImageAvailableListener: null "+param.thisObject);
                            mRemoteCamera.Close();
                        }
                    }
                }
            });

//            XposedHelpers.findAndHookMethod(ImageReader.class,"acquireLatestImage", new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                }
//            });

            XposedHelpers.findAndHookMethod(ImageReader.class,"acquireNextImage", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Image image = (Image)param.getResult();
                    if(mCameraPush != null) {
                        mCameraPush.encode(image);
                    }
                }
            });

        }
        catch (Exception e)
        {
            Log.e(TAG,"",e);
        }
    }
}

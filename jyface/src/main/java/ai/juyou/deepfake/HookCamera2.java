package ai.juyou.deepfake;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCamera2 {

    private static final String TAG = "CameraHook";

    private CameraDevice.StateCallback mHookCameraDeviceStateCallback;
    private CameraDevice.StateCallback mOriginCameraDeviceStateCallback;


    private CameraCaptureSession.StateCallback mHookCameraCaptureSessionStateCallback;
    private CameraCaptureSession.StateCallback mOriginCameraCaptureSessionStateCallback;


    private ImageReader.OnImageAvailableListener mHookImageAvailableListener;
    private ImageReader.OnImageAvailableListener mOriginImageAvailableListener;


    public HookCamera2() {
        mHookCameraDeviceStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.d(TAG, "onOpened :" + camera);
                mOriginCameraDeviceStateCallback.onOpened(camera);
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.d(TAG, "onDisconnected");
                mOriginCameraDeviceStateCallback.onDisconnected(camera);
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.d(TAG, "onError");
                mOriginCameraDeviceStateCallback.onError(camera, error);
            }
        };

        mHookCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Log.d(TAG,"onConfigured");
                mOriginCameraCaptureSessionStateCallback.onConfigured(session);
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Log.d(TAG,"onConfigureFailed");
                mOriginCameraCaptureSessionStateCallback.onConfigureFailed(session);
            }
        };

        mHookImageAvailableListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.d(TAG,"onImageAvailable :" + reader);

                mOriginImageAvailableListener.onImageAvailable(reader);
            }
        };
    }
    public void hook(XC_LoadPackage.LoadPackageParam lpParam) {
        XposedHelpers.findAndHookMethod(CameraManager.class,"openCamera", String.class, CameraDevice.StateCallback.class, Handler.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "openCamera");
                String cameraId = (String)param.args[0];
                Log.d(TAG, "openCamera: " + cameraId);
                mOriginCameraDeviceStateCallback = (CameraDevice.StateCallback)param.args[1];
                param.args[1] = mHookCameraDeviceStateCallback;
            }
        });

        try {
            XposedHelpers.findAndHookMethod("android.hardware.camera2.impl.CameraDeviceImpl", lpParam.classLoader, "createCaptureSession",SessionConfiguration.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "createCaptureSession");
                    Object obj = param.args[0];
                    Log.d(TAG, "createCaptureSession: " + obj);
                }
            });
        }
        catch (Exception e)
        {
            Log.e(TAG,"",e);
        }

        try {
            XposedHelpers.findAndHookMethod("android.hardware.camera2.impl.CameraDeviceImpl", lpParam.classLoader, "createCaptureSession",List.class,CameraCaptureSession.StateCallback.class, Handler.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "createCaptureSession");
                    Object obj = param.args[0];
                    Log.d(TAG, "createCaptureSession: " + obj);
                    mOriginCameraCaptureSessionStateCallback = (CameraCaptureSession.StateCallback)param.args[1];
                    param.args[1] = mHookCameraCaptureSessionStateCallback;
                }
            });

            XposedHelpers.findAndHookMethod(CaptureRequest.Builder.class,"addTarget", Surface.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "addTarget");
                    Surface surface = (Surface)param.args[0];
                    Log.d(TAG, "addTarget: " + surface);
                }
            });

            XposedHelpers.findAndHookMethod(ImageReader.class,"setOnImageAvailableListener", ImageReader.OnImageAvailableListener.class, Handler.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "setOnImageAvailableListener");
                    mOriginImageAvailableListener = (ImageReader.OnImageAvailableListener)param.args[0];
                    param.args[0] = mHookImageAvailableListener;
                    Log.d(TAG, "setOnImageAvailableListener: " + mOriginImageAvailableListener);
                }
            });

            //XposedBridge.hookAllMethods(android.media.ImageReader.class,)
        }
        catch (Exception e)
        {
            Log.e(TAG,"",e);
        }
    }
}

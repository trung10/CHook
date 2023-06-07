package ai.juyou.deepfake.hook;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;

import ai.juyou.remotecamera.RemoteCameraCallback;
import ai.juyou.remotecamera.RemoteCameraPullSession;
import ai.juyou.remotecamera.RemoteCameraPushSession;
import ai.juyou.remotecamera.RemoteCamera;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCamera2 {
    private static final String TAG = "CameraHook";
    private CameraDevice.StateCallback mOriginCameraDeviceStateCallback;
    private CameraDevice.StateCallback mHookCameraDeviceStateCallback;
    private CameraDevice mCameraDevice;

    private CameraCaptureSession.StateCallback mOriginCameraCaptureSessionStateCallback;
    private CameraCaptureSession.StateCallback mHookCameraCaptureSessionStateCallback;
    private CameraCaptureSession mCameraCaptureSession;

    private Surface mOriginPreviewSurface;
    private Surface mOriginImageReaderSurface;

    private Surface mHookPreviewSurface; //HOOK的预览Surface由解码器提供，解码器直接渲染到Hook的Surface上
    private Surface mHookImageReaderSurface; //HOOK的ImageReaderSurface由编码器提供，编码器直接从Hook的ImageReaderSurface进行编码

    private List<Surface> mOriginSurfaces;
    private List<Surface> mHookSurfaces;

    private final ImageReader.OnImageAvailableListener mHookImageAvailableListener;
    private final RemoteCamera mCamera;
    private ImageReader mOriginImageReader;
    private ImageReader.OnImageAvailableListener mOriginImageAvailableListener;
    private RemoteCameraPushSession mCameraPushSession;
    private RemoteCameraPullSession mCameraPullSession;

    public HookCamera2() {
        mCamera = new RemoteCamera("192.168.1.13");
        mCamera.setCallback(new RemoteCameraCallback() {
            @Override
            public void onPushConnected(RemoteCameraPushSession session) {
                if(session != mCameraPushSession){
                    mCameraPushSession = session;
                }
            }

            @Override
            public void onPullConnected(RemoteCameraPullSession session) {
                if(session != mCameraPullSession){
                    mCameraPullSession = session;
                }
            }

            @Override
            public void onDecoded(byte[] data)
            {

            }

            @Override
            public void onPushConnectFailed(Throwable e) {

            }

            @Override
            public void onPullConnectFailed(Throwable e) {

            }

            @Override
            public void onPushDisconnected() {
                mCameraPushSession = null;
            }

            @Override
            public void onPullDisconnected() {
                mCameraPullSession = null;
            }
        });

        mHookCameraDeviceStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.d(TAG,"onOpened :" + camera);
                mCameraDevice = camera;
                if(mOriginCameraDeviceStateCallback!=null){
                    mOriginCameraDeviceStateCallback.onOpened(camera);
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                if(mOriginCameraDeviceStateCallback!=null){
                    mOriginCameraDeviceStateCallback.onDisconnected(camera);
                }
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                if(mOriginCameraDeviceStateCallback!=null){
                    mOriginCameraDeviceStateCallback.onError(camera, error);
                }
            }
        };

        mHookCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Log.d(TAG,"onConfigured :" + session);
                mCameraCaptureSession = session;
                if(mOriginCameraCaptureSessionStateCallback!=null){
                    mOriginCameraCaptureSessionStateCallback.onConfigured(session);
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                if(mOriginCameraCaptureSessionStateCallback!=null){
                    mOriginCameraCaptureSessionStateCallback.onConfigureFailed(session);
                }
            }
        };

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

    long lastTime = 0;
    int fps= 0;

    public void hook(XC_LoadPackage.LoadPackageParam lpParam) {
        try {

            XposedHelpers.findAndHookMethod(CameraManager.class,"openCamera", String.class,CameraDevice.StateCallback.class,Handler.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String cameraId = (String)param.args[0];
                    mOriginCameraDeviceStateCallback = (CameraDevice.StateCallback)param.args[1];
                    param.args[1] = mHookCameraDeviceStateCallback;
                    Log.d(TAG,"CameraManager openCamera:"+cameraId);
                }
            });


            XposedHelpers.findAndHookMethod(CaptureRequest.Builder.class,"addTarget", Surface.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Surface surface = (Surface)param.args[0];
                    Field field = XposedHelpers.findField(Surface.class,"mName");
                    String mName = (String)field.get(surface);
//                    if(mName!=null){
//                        mOriginPreviewSurface = surface;
//                        param.args[0] = mHookPreviewSurface;
//                    }
//                    else{
//                        mOriginImageReaderSurface = surface;
//                        param.args[0] = mHookImageReaderSurface;
//                    }
                    //HookHelper.printStackTrace();
                    //Log.d(TAG,"addTarget :" + surface);
                }
            });


            XposedHelpers.findAndHookMethod("android.hardware.camera2.impl.CameraDeviceImpl",lpParam.classLoader,"createCaptureSession", List.class,CameraCaptureSession.StateCallback.class,Handler.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    mOriginSurfaces = (List<Surface>)param.args[0];
//                    param.args[0] = mHookSurfaces;
//
//                    mOriginCameraCaptureSessionStateCallback = (CameraCaptureSession.StateCallback)param.args[1];
//                    param.args[1] = mHookCameraCaptureSessionStateCallback;
                    Log.d(TAG,"CameraCaptureSession createCaptureSession"+param.args[0]);
                    Log.d(TAG,"CameraCaptureSession createCaptureSession");
                }
            });

            XposedHelpers.findAndHookMethod("android.hardware.camera2.impl.CameraDeviceImpl",lpParam.classLoader,"close", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    //Log.d(TAG,"CameraCaptureSession close");
                }
            });

            XposedHelpers.findAndHookMethod(ImageReader.class,"setOnImageAvailableListener", ImageReader.OnImageAvailableListener.class, Handler.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ImageReader.OnImageAvailableListener listener = (ImageReader.OnImageAvailableListener)param.args[0];
                    if(listener != null){
                        mOriginImageReader = (ImageReader)param.thisObject;
                        mOriginImageAvailableListener = listener;
                        param.args[0] = mHookImageAvailableListener;
                        mCamera.open(new Size(mOriginImageReader.getWidth(), mOriginImageReader.getHeight()));
                    }
                    else{
                        if(param.thisObject== mOriginImageReader){
                            mOriginImageReader = null;
                            mOriginImageAvailableListener = null;
                            mCamera.close();
                        }
                    }
                    Log.d(TAG,"setOnImageAvailableListener " + listener);
                }
            });
            XposedBridge.hookAllMethods(ImageReader.class, "acquireNextSurfaceImage", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    int state = (int)param.getResult();
                    if(state==0){
                        Image image = (Image)param.args[0];
                        if(image!=null){
                            long startTime = System.currentTimeMillis();
                            if(mCameraPushSession != null) {
                                mCameraPushSession.push(image);
                            }
                            //Log.d(TAG,"encode cost:" + (System.currentTimeMillis() - startTime));
                            startTime = System.currentTimeMillis();
                            if(mCameraPullSession != null) {
                                mCameraPullSession.pull(image);
                            }
                            else{
                                clearImage(image);
                            }
                            //Log.d(TAG,"render cost:" + (System.currentTimeMillis() - startTime));
                        }
                    }
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
        for (int i = 0; i < planes.length; i++) {
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
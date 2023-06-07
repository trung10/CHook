package ai.juyou.deepfake;


import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import ai.juyou.deepfake.databinding.ActivityFaceBinding;
import ai.juyou.remotecamera.RemoteCameraCallback;
import ai.juyou.remotecamera.RemoteCameraPullSession;
import ai.juyou.remotecamera.RemoteCameraPushSession;
import ai.juyou.remotecamera.RemoteCamera;

public class FaceActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, RemoteCameraCallback, ImageReader.OnImageAvailableListener {
    private static final String TAG = "FaceActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private ActivityFaceBinding binding;

    private String mCameraId;
    private CameraDevice  mCameraDevice;
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;

    private Size mPreviewSize;
    private AutoFitTextureView mTextureView;

    private RemoteCamera mRemoteCamera;
    private RemoteCameraPushSession mRemoteCameraPushSession;
    private RemoteCameraPullSession mRemoteCameraPullSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFaceBinding.inflate(getLayoutInflater());
        mTextureView = binding.previewView;
        getWindow().setStatusBarColor(getColor(R.color.black));
        setContentView(binding.getRoot());
    }

    private void initPreview() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mCameraId = getCameraId(CameraCharacteristics.LENS_FACING_FRONT, cameraManager);
        mPreviewSize = new Size(1280, 720);

        if(mRemoteCamera==null){
            initRemoteCamera(mPreviewSize);
        }

        try {
            startPreview(cameraManager);
        }catch (CameraAccessException e){
            Toast.makeText(this, "相机访问异常", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initRemoteCamera(Size previewSize)
    {
        String ipAddress = "192.168.1.5";
        mRemoteCamera = new RemoteCamera(ipAddress);
        mRemoteCamera.setCallback(this);
        Surface surface = new Surface(mTextureView.getSurfaceTexture());
        mRemoteCamera.open(previewSize,null, surface);
        updateStatusText("开始连接...");
    }


    private boolean allPermissionsGranted() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length == 1 && grantResults[0] == PERMISSION_GRANTED) {
                initPreview();
            } else {
                showPermissionDenyDialog();
                //Toast.makeText(this, "权限不足", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showPermissionDenyDialog() {
        PermissionDialog dialog = new PermissionDialog();
        dialog.show(getSupportFragmentManager(), "PermissionDeny");
    }

    private void startPreview(CameraManager cameraManager) throws CameraAccessException {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        cameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                if(mCameraDevice != null){
                    mCameraDevice.close();
                }
                mCameraDevice = camera;
                createPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        }, null);
    }

    private void createPreviewSession() {
        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 2);
        mImageReader.setOnImageAvailableListener(this, null);

        List<Surface> surfaces = new ArrayList<>();
        surfaces.add(mImageReader.getSurface());

//        if(mTextureView!=null){
//            mTextureView.setAspectRation(mPreviewSize.getHeight(), mPreviewSize.getWidth());
//            //根据TextureView 和 选定的 previewSize 创建用于显示预览数据的Surface
//            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
//            //设置SurfaceTexture缓冲区大小
//            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            Surface mPreviewSurface = new Surface(surfaceTexture);
//            surfaces.add(mPreviewSurface);
//        }

        try {
            //创建预览session
            mCameraDevice.createCaptureSession(surfaces,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            try {
                                //构建预览捕获请求
                                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                for(Surface surface:surfaces){
                                    builder.addTarget(surface);
                                }
                                CaptureRequest captureRequest = builder.build();
                                //设置重复请求，以获取连续预览数据
                                session.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                                                super.onCaptureProgressed(session, request, partialResult);
                                            }

                                            @Override
                                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                                super.onCaptureCompleted(session, request, result);
                                            }
                                        },
                                        null);
                                mCameraCaptureSession = session;
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {

                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(mTextureView.isAvailable()){
            if (allPermissionsGranted()) {
                initPreview();
            }
        }else{
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        closeCamera();
    }

    private void closeCamera(){
        if(mCameraCaptureSession!=null){
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if(mCameraDevice !=null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null){
            mImageReader.close();
            mImageReader = null;
        }
    }

    private static String getCameraId(int lens, CameraManager cameraManager){
        try {
            for (String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraFacing == lens){
                    return cameraId;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }

        return null;
    }


    private void startPush(Size size) {
//        if(mCameraPush != null) {
//            stopPush();
//        }
//        mCameraPush = new CameraPush(mCameraPushContext);
//        mCameraPush.setCallback(this);
//        mCameraPush.start(size);
    }

    private void stopPush() {
//        if (mCameraPush != null) {
//            mCameraPush.stop();
//            mCameraPush = null;
//        }
    }

    public void onDestroy() {
        super.onDestroy();
        stopPush();
    }


    private StringBuffer mStatusText = new StringBuffer();
    private void updateStatusText(String status)
    {
        mStatusText.append(status);
        mStatusText.append("\n");
        String str = mStatusText.toString();
        binding.status.setText(str);
    }


    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        if (allPermissionsGranted()) {
            initPreview();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

    @Override
    public void onPushConnected(RemoteCameraPushSession session) {
        updateStatusText("Push已连接");
        mRemoteCameraPushSession = session;
    }

    @Override
    public void onPullConnected(RemoteCameraPullSession session) {
        updateStatusText("Pull已连接");
        mRemoteCameraPullSession = session;
    }

    @Override
    public void onPushConnectFailed(Throwable e) {
        updateStatusText("Push连接失败");
    }

    @Override
    public void onPullConnectFailed(Throwable e) {
        updateStatusText("Pull连接失败");
    }

    @Override
    public void onPushDisconnected() {
        updateStatusText("Push连接已断开");
        mRemoteCameraPushSession = null;
    }

    @Override
    public void onPullDisconnected() {
        updateStatusText("Pull连接已断开");
        mRemoteCameraPullSession = null;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        //byte[] nv21 = ImageUtils.YUV_420_888toNV21(image);
        if(image!=null){
            if(mRemoteCameraPushSession!=null){
                mRemoteCameraPushSession.push(image);
            }

            image.close();
        }
    }
}

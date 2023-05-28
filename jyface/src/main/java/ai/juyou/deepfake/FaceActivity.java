package ai.juyou.deepfake;


import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import ai.juyou.deepfake.databinding.ActivityFaceBinding;
import ai.juyou.remotecamera.CameraCallback;
import ai.juyou.remotecamera.CameraImagePullSession;
import ai.juyou.remotecamera.CameraImagePushSession;
import ai.juyou.remotecamera.CameraPullSession;
import ai.juyou.remotecamera.CameraPushSession;
import ai.juyou.remotecamera.CameraImage;

public class FaceActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, CameraCallback {
    private static final String TAG = "FaceActivity";
    private ActivityFaceBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private CameraImage cameraImage;
    private CameraImagePushSession cameraImagePushSession;
    private CameraImagePullSession cameraImagePullSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(getColor(R.color.black));

        String ipAddress = "192.168.1.113";
        cameraImage = new CameraImage(ipAddress);
        cameraImage.setCallback(this);

        startPreview();
    }

    void startPreview()
    {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.d(TAG, "Error binding preview", e);
                //Toast.makeText(FaceActivity.this, R.string.error_1001, Toast.LENGTH_LONG).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder().build();
        imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(this), this);


        preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview,imageAnalyzer);
        startPush(imageAnalyzer.getResolutionInfo().getResolution());
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

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
//        if (mCameraPush != null) {
//            Image image=imageProxy.getImage();
//            if(image!=null){
//                if(mPushEncoder!=null){
//                    //Log.d(TAG, "analyze: "+image.getWidth()+" "+image.getHeight());
//                    mPushEncoder.encode(image);
//                }
//                imageProxy.close();
//            }
//        }
    }

    @Override
    public void onPushConnected(CameraPushSession session) {
        cameraImagePushSession = (CameraImagePushSession)session;
    }

    @Override
    public void onPullConnected(CameraPullSession session) {
        cameraImagePullSession = (CameraImagePullSession)session;
    }

    @Override
    public void onPushDisconnected() {
        cameraImagePushSession = null;
    }

    @Override
    public void onPullDisconnected() {
        cameraImagePullSession = null;
    }
}

package ai.juyou.deepfake;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import ai.juyou.deepfake.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(allPermissionsGranted()){
            initPreview();
        }
    }

    private void initPreview()
    {
        //Intent intent = new Intent(this, FaceActivity.class);
        //startActivity(intent);
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
}
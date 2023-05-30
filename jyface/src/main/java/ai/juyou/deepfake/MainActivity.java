package ai.juyou.deepfake;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import ai.juyou.deepfake.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setModuleState(binding);

        binding.menuDetectionTest.setOnClickListener(this);
        binding.menuCredit.setOnClickListener(this);
        binding.menuSettings.setOnClickListener(this);
        binding.menuAbout.setOnClickListener(this);

        setContentView(binding.getRoot());

        if(allPermissionsGranted()){
            initPreview();
        }
    }

    private void setModuleState(ActivityMainBinding binding) {

        if (isModuleActivated()) {
            binding.moduleStatusCard.setCardBackgroundColor(getColor(R.color.purple_500));
            binding.moduleStatusIcon.setImageDrawable(AppCompatResources.getDrawable(this,
                    R.drawable.baseline_check_circle_24
            ));
            binding.moduleStatusText.setText(getString(R.string.card_title_activated));
            binding.serviceStatusText.setText(getString(R.string.card_detail_activated));
            binding.serveTimes.setText(getString(R.string.card_serve_time));
        } else {

            binding.moduleStatusCard.setCardBackgroundColor(getColor(R.color.red_500));
            binding.moduleStatusIcon.setImageDrawable(AppCompatResources.getDrawable(this,
                    R.drawable.baseline_error_24
            ));
            binding.moduleStatusText.setText(getText(R.string.card_title_not_activated));
            binding.serviceStatusText.setText(getText(R.string.card_detail_not_activated));
        }
    }

    private boolean isModuleActivated() {
        return false;
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.menu_detection_test) {
            Log.d("MainActivity", "onClick: menu_detection_test");
        } else if (id == R.id.menu_credit) {
            Log.d("MainActivity", "onClick: menu_location_credit");
        } else if (id == R.id.menu_settings) {
            Log.d("MainActivity", "onClick: menu_settings");
        } else if (id == R.id.menu_about) {
            Log.d("MainActivity", "onClick: menu_about");
        }
    }
}
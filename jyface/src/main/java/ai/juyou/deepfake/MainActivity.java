package ai.juyou.deepfake;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import ai.juyou.deepfake.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setModuleState(binding);

        binding.menuPreview.setOnClickListener(this);
        binding.menuCredit.setOnClickListener(this);
        binding.menuSettings.setOnClickListener(this);
        binding.menuAbout.setOnClickListener(this);

        setContentView(binding.getRoot());
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


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.menu_preview) {
            Intent intent = new Intent(this, FaceActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_credit) {
            //Intent intent = new Intent(this, FaceActivity.class);
            //startActivity(intent);
        } else if (id == R.id.menu_settings) {
            Log.d("MainActivity", "onClick: menu_settings");
        } else if (id == R.id.menu_about) {
            Log.d("MainActivity", "onClick: menu_about");
        }
    }
}
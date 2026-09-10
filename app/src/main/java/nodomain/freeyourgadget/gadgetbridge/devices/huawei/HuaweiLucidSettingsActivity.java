package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 华为手环 8 × SaA 清醒梦与睡眠联动配置界面
 * 包含：震动强度、震动次数、脉冲时长调节，以及【一键测试当前震动】功能
 */
public class HuaweiLucidSettingsActivity extends Activity {

    private HuaweiLucidSettings settings;
    private TextView tvDurationValue;
    private SeekBar sbDuration;
    private RadioGroup rgIntensity;
    private RadioGroup rgRepeat;
    private Button btnTestVibration;
    private Button btnStartService;

    // 显式控件 ID 常量
    public static final int ID_INTENSITY_1 = 101;
    public static final int ID_INTENSITY_2 = 102;
    public static final int ID_INTENSITY_3 = 103;

    public static final int ID_REPEAT_1 = 201;
    public static final int ID_REPEAT_2 = 202;
    public static final int ID_REPEAT_3 = 203;
    public static final int ID_REPEAT_4 = 204;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new HuaweiLucidSettings(this);

        setupViews();
        loadCurrentSettings();
    }

    private void setupViews() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 60, 40, 40);

        TextView title = new TextView(this);
        title.setText("华为手环 8 × SaA 清醒梦联动设置");
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        // 1. 震动强度设置
        TextView tvIntensityTitle = new TextView(this);
        tvIntensityTitle.setText("\n震动强度 (微弱适合手腕，中等适合大臂):");
        root.addView(tvIntensityTitle);

        rgIntensity = new RadioGroup(this);
        rgIntensity.setOrientation(RadioGroup.HORIZONTAL);
        addRadioButton(rgIntensity, ID_INTENSITY_1, "1. 微弱(手腕)");
        addRadioButton(rgIntensity, ID_INTENSITY_2, "2. 中等(大臂)");
        addRadioButton(rgIntensity, ID_INTENSITY_3, "3. 强力");
        root.addView(rgIntensity);

        // 2. 震动次数设置
        TextView tvRepeatTitle = new TextView(this);
        tvRepeatTitle.setText("\n震动次数 (清醒梦提示脉冲数):");
        root.addView(tvRepeatTitle);

        rgRepeat = new RadioGroup(this);
        rgRepeat.setOrientation(RadioGroup.HORIZONTAL);
        addRadioButton(rgRepeat, ID_REPEAT_1, "1 次");
        addRadioButton(rgRepeat, ID_REPEAT_2, "2 次 (双击)");
        addRadioButton(rgRepeat, ID_REPEAT_3, "3 次");
        addRadioButton(rgRepeat, ID_REPEAT_4, "4 次");
        root.addView(rgRepeat);

        // 3. 单次时长设置
        tvDurationValue = new TextView(this);
        root.addView(tvDurationValue);

        sbDuration = new SeekBar(this);
        sbDuration.setMax(500); // 最大 500ms
        root.addView(sbDuration);

        // 4. 一键测试震动按钮
        btnTestVibration = new Button(this);
        btnTestVibration.setText("★ 点击测试手环震动 (Test Vibration)");
        btnTestVibration.setBackgroundColor(0xFF4CAF50);
        btnTestVibration.setTextColor(0xFFFFFFFF);
        root.addView(btnTestVibration);

        // 5. 启动联动后台服务按钮
        btnStartService = new Button(this);
        btnStartService.setText("启动睡眠联动后台服务");
        root.addView(btnStartService);

        setContentView(root);

        bindListeners();
    }

    private void addRadioButton(RadioGroup group, int id, String text) {
        RadioButton rb = new RadioButton(this);
        rb.setId(id);
        rb.setText(text);
        group.addView(rb);
    }

    private void loadCurrentSettings() {
        int intensity = settings.getVibrateIntensity();
        if (intensity == 1) rgIntensity.check(ID_INTENSITY_1);
        else if (intensity == 2) rgIntensity.check(ID_INTENSITY_2);
        else if (intensity == 3) rgIntensity.check(ID_INTENSITY_3);

        int repeat = settings.getVibrateRepeat();
        if (repeat == 1) rgRepeat.check(ID_REPEAT_1);
        else if (repeat == 2) rgRepeat.check(ID_REPEAT_2);
        else if (repeat == 3) rgRepeat.check(ID_REPEAT_3);
        else if (repeat == 4) rgRepeat.check(ID_REPEAT_4);

        int duration = settings.getVibrateDurationMs();
        sbDuration.setProgress(duration);
        tvDurationValue.setText("\n单次脉冲时长: " + duration + " 毫秒");
    }

    private void bindListeners() {
        rgIntensity.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == ID_INTENSITY_1) settings.setVibrateIntensity(1);
            else if (checkedId == ID_INTENSITY_2) settings.setVibrateIntensity(2);
            else if (checkedId == ID_INTENSITY_3) settings.setVibrateIntensity(3);
        });

        rgRepeat.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == ID_REPEAT_1) settings.setVibrateRepeat(1);
            else if (checkedId == ID_REPEAT_2) settings.setVibrateRepeat(2);
            else if (checkedId == ID_REPEAT_3) settings.setVibrateRepeat(3);
            else if (checkedId == ID_REPEAT_4) settings.setVibrateRepeat(4);
        });

        sbDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int duration = Math.max(50, progress);
                tvDurationValue.setText("\n单次脉冲时长: " + duration + " 毫秒");
                if (fromUser) {
                    settings.setVibrateDurationMs(duration);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 点击测试手环震动
        btnTestVibration.setOnClickListener(v -> {
            int intensity = settings.getVibrateIntensity();
            int repeat = settings.getVibrateRepeat();
            int duration = settings.getVibrateDurationMs();

            Toast.makeText(this,
                    String.format("正在发送测试指令: 强度=%d, 次数=%d, 时长=%dms", intensity, repeat, duration),
                    Toast.LENGTH_SHORT).show();

            Intent testIntent = new Intent(HuaweiSleepAsAndroidSupport.ACTION_HINT);
            sendBroadcast(testIntent);
        });

        // 启动后台服务
        btnStartService.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, HuaweiSaABridgeService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "后台联动服务已启动！现在可在 SaA 中点击开始追踪", Toast.LENGTH_LONG).show();
        });
    }
}

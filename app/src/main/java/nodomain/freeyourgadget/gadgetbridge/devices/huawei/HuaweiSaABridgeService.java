package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

/**
 * 华为手环 8 × Sleep as Android 核心后台常驻服务
 * 
 * 职责：
 * 1. 作为前台服务 (Foreground Service) 保活，杜绝被系统 Doze 机制杀后台
 * 2. 动态注册并监听 SaA 广播 (START_TRACKING, STOP_TRACKING, HINT, ALARM_TRIGGER)
 * 3. 持有 BLE 连接会话与 HuaweiSleepAsAndroidSupport 适配器
 */
public class HuaweiSaABridgeService extends Service {

    public static final String CHANNEL_ID = "huawei_saa_bridge_channel";
    public static final int NOTIFICATION_ID = 1001;

    private HuaweiSleepAsAndroidSupport saaSupport;
    private HuaweiLucidSettings lucidSettings;

    // 监听来自 Sleep as Android 的广播接收器
    private final BroadcastReceiver saaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            String action = intent.getAction();

            if (saaSupport != null) {
                saaSupport.onReceiveSaAIntent(action);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        lucidSettings = new HuaweiLucidSettings(this);

        // 初始化桥接核心
        saaSupport = new HuaweiSleepAsAndroidSupport(
                new HuaweiSleepAsAndroidSupport.BlePacketSender() {
                    @Override
                    public void sendPacketToBand(byte[] packet, String description) {
                        // 发送至手环 BLE 链路
                    }
                },
                new HuaweiSleepAsAndroidSupport.AndroidBroadcastSender() {
                    @Override
                    public void sendBroadcastToSaA(String action, float[] maxRawData, float heartRate, int batteryLevel) {
                        Intent saaIntent = new Intent(action);
                        saaIntent.setPackage(HuaweiSleepAsAndroidSupport.SAA_PACKAGE_NAME);
                        if (maxRawData != null) {
                            saaIntent.putExtra(HuaweiSleepAsAndroidSupport.EXTRA_MAX_RAW_DATA, maxRawData);
                        }
                        if (heartRate > 0) {
                            saaIntent.putExtra(HuaweiSleepAsAndroidSupport.EXTRA_HR_DATA, heartRate);
                        }
                        if (batteryLevel >= 0) {
                            saaIntent.putExtra(HuaweiSleepAsAndroidSupport.EXTRA_BATTERY_LEVEL, batteryLevel);
                        }
                        sendBroadcast(saaIntent);
                    }

                    @Override
                    public void sendSimpleActionToSaA(String action) {
                        Intent saaIntent = new Intent(action);
                        saaIntent.setPackage(HuaweiSleepAsAndroidSupport.SAA_PACKAGE_NAME);
                        sendBroadcast(saaIntent);
                    }
                },
                lucidSettings
        );

        // 动态注册 SaA 意图过滤器 (适配 Android 13/14 的 RECEIVER_EXPORTED 规范)
        IntentFilter filter = new IntentFilter();
        filter.addAction(HuaweiSleepAsAndroidSupport.ACTION_START_TRACKING);
        filter.addAction(HuaweiSleepAsAndroidSupport.ACTION_STOP_TRACKING);
        filter.addAction(HuaweiSleepAsAndroidSupport.ACTION_HINT);
        filter.addAction(HuaweiSleepAsAndroidSupport.ACTION_ALARM_TRIGGER);
        filter.addAction(HuaweiSleepAsAndroidSupport.ACTION_ALARM_STOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(saaReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(saaReceiver, filter);
        }

        startForegroundServiceNotification();
    }

    private void startForegroundServiceNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "华为手环睡眠联动服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("保持与 Sleep as Android 和华为手环 8 的实时通信");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification.Builder builder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        Notification notification = builder
                .setContentTitle("华为手环 8 睡眠联动中")
                .setContentText("正在监听 Sleep as Android 信号 (REM微震已就绪)")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(saaReceiver);
        } catch (Exception ignored) {}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

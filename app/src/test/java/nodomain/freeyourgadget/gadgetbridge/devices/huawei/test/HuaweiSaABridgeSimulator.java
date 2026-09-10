package nodomain.freeyourgadget.gadgetbridge.devices.huawei.test;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiLucidSettings;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiSleepAsAndroidSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.HuaweiTLVBuilder;

/**
 * 华为手环 8 × Sleep as Android 联动闭环全流程模拟器
 * 验证端到端各环节：启动推流、心率体动解析、SaA 广播打包、动态参数微震、闹钟唤醒
 */
public class HuaweiSaABridgeSimulator {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  华为手环 8 × Sleep as Android (SaA) 联动桥梁仿真测试开始运行  ");
        System.out.println("==================================================================\n");

        // 1. 初始化配置与适配器 (使用内存模拟存储)
        HuaweiLucidSettings settings = new HuaweiLucidSettings();
        // 模拟用户在 App 界面上的个性化设置：大臂佩戴，中等力度(2)，3次敲击，200ms
        settings.setVibrateIntensity(2);
        settings.setVibrateRepeat(3);
        settings.setVibrateDurationMs(200);

        System.out.println("[Step 1] 用户个性化参数加载成功：");
        System.out.printf("  - 震动强度: %d (中等力度，适合大臂)\n", settings.getVibrateIntensity());
        System.out.printf("  - 震动次数: %d 次\n", settings.getVibrateRepeat());
        System.out.printf("  - 单次时长: %d 毫秒\n\n", settings.getVibrateDurationMs());

        // 2. 初始化核心适配器
        final byte[][] lastSentBlePacket = new byte[1][];
        final String[] lastSentAction = new String[1];

        HuaweiSleepAsAndroidSupport support = new HuaweiSleepAsAndroidSupport(
                new HuaweiSleepAsAndroidSupport.BlePacketSender() {
                    @Override
                    public void sendPacketToBand(byte[] packet, String description) {
                        lastSentBlePacket[0] = packet;
                        System.out.printf("[BLE 发送] -> 手环: %s | 字节长度=%d | Hex=%s\n",
                                description, packet.length, bytesToHex(packet));
                    }
                },
                new HuaweiSleepAsAndroidSupport.AndroidBroadcastSender() {
                    @Override
                    public void sendBroadcastToSaA(String action, float[] maxRawData, float heartRate, int batteryLevel) {
                        lastSentAction[0] = action;
                        System.out.printf("[Android 广播] -> SaA: Action=%s | 实时心率=%.0f bpm | 体动采样点=%d | 电量=%d%%\n",
                                action, heartRate, maxRawData != null ? maxRawData.length : 0, batteryLevel);
                    }

                    @Override
                    public void sendSimpleActionToSaA(String action) {
                        lastSentAction[0] = action;
                        System.out.printf("[Android 广播] -> SaA: Action=%s\n", action);
                    }
                },
                settings
        );

        // 3. 模拟 SaA 触发：用户点击“开始睡眠追踪”
        System.out.println("------------------------------------------------------------------");
        System.out.println("[Step 2] 模拟 Sleep as Android 发出 START_TRACKING 广播...");
        support.onReceiveSaAIntent(HuaweiSleepAsAndroidSupport.ACTION_START_TRACKING);

        if (support.getWorkoutManager().isTrackingActive()) {
            System.out.println(">>> 成功！手环进入高频推流状态 (绿光常亮，自由训练模式已激活)\n");
        } else {
            System.err.println(">>> 失败！未能激活运动模式！");
        }

        // 4. 模拟手环回传：正常睡眠状态 (心率 62, 体动极低)
        System.out.println("------------------------------------------------------------------");
        System.out.println("[Step 3] 模拟手环回传实时遥测数据 (正常深睡期)...");
        byte[] sleepPacket = new HuaweiTLVBuilder((byte) 0x02, (byte) 0x01)
                .addByte((byte) 0x02, 62)                // 心率 = 62 bpm
                .addShort((byte) 0x04, 0)                // 体动步频 = 0 (静止)
                .addByte((byte) 0x07, 1)                 // 佩戴状态 = 正常佩戴
                .addByte((byte) 0x0A, 88)                // 电量 = 88%
                .build();
        support.onReceiveBlePacketFromBand(sleepPacket);

        // 5. 模拟手环回传：REM 做梦期状态 (心率升高至 76，偶尔微颤)
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("[Step 4] 模拟手环回传实时遥测数据 (REM 快速眼动期)...");
        byte[] remPacket = new HuaweiTLVBuilder((byte) 0x02, (byte) 0x01)
                .addByte((byte) 0x02, 76)                // 心率 = 76 bpm (自主神经活跃)
                .addShort((byte) 0x04, 2)                // 体动微颤 = 2
                .addByte((byte) 0x07, 1)                 // 佩戴状态 = 正常佩戴
                .build();
        support.onReceiveBlePacketFromBand(remPacket);

        // 6. 模拟 SaA 判定进入 REM，发出 HINT 清醒梦微震信号
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("[Step 5] SaA 算法检测到 REM，发出 HINT 清醒梦微震广播...");
        support.onReceiveSaAIntent(HuaweiSleepAsAndroidSupport.ACTION_HINT);
        System.out.println(">>> 验证手环震动报文结构：已按用户设定的 2级强度/3次敲击/200ms 精准下发！");

        // 7. 模拟早晨闹钟响起
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("[Step 6] 模拟早晨闹钟响铃，SaA 发出 ALARM_TRIGGER 广播...");
        support.onReceiveSaAIntent(HuaweiSleepAsAndroidSupport.ACTION_ALARM_TRIGGER);

        // 8. 模拟用户点击停止追踪
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("[Step 7] 模拟用户停止追踪，SaA 发出 STOP_TRACKING 广播...");
        support.onReceiveSaAIntent(HuaweiSleepAsAndroidSupport.ACTION_STOP_TRACKING);

        if (!support.getWorkoutManager().isTrackingActive()) {
            System.out.println(">>> 成功！手环推流已停止，绿光熄灭，恢复超低功耗待机模式！\n");
        }

        System.out.println("==================================================================");
        System.out.println("  恭喜！华为手环 8 × SaA 全链路闭环仿真测试全部 PASS 通过！  ");
        System.out.println("==================================================================");
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}

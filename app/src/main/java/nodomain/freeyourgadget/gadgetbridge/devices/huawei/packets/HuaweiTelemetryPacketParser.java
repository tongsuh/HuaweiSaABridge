/*  Copyright (C) 2024 Gadgetbridge Contributors
    This file is part of Gadgetbridge.
    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
*/
package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

/**
 * 华为手环 BLE 运动遥测数据包解析器
 * 负责从手环上报的二进制 TLV 数据流中解包出心率、体动与佩戴状态
 */
public class HuaweiTelemetryPacketParser {

    public static class TelemetryData {
        public int heartRate = 0;           // 实时心率 (BPM)
        public int stepDelta = 0;           // 周期内步数增量 (体动指标)
        public float motionIntensity = 0f;  // 归一化体动强度 (m/s^2 估算值)
        public boolean isWorn = true;       // 是否佩戴在手腕上
        public int batteryLevel = -1;       // 电池电量百分比 (-1 为未知)

        @Override
        public String toString() {
            return String.format("TelemetryData[HR=%d bpm, Motion=%.2f m/s², Worn=%b, Battery=%d%%]",
                    heartRate, motionIntensity, isWorn, batteryLevel);
        }
    }

    public static final byte TAG_HEART_RATE = (byte) 0x02;        // 实时心率标签
    public static final byte TAG_STEP_CADENCE = (byte) 0x04;      // 步频/体动标签
    public static final byte TAG_WEAR_STATE = (byte) 0x07;        // 佩戴状态 (0=脱腕, 1=佩戴)
    public static final byte TAG_BATTERY = (byte) 0x0A;           // 电量标签

    /**
     * 解析华为 BLE 接收到的原始报文
     * @param rawPacket 蓝牙接收到的完整帧
     * @return 解析出的 TelemetryData，若校验失败或非遥测包则返回 null
     */
    public static TelemetryData parse(byte[] rawPacket) {
        if (rawPacket == null || rawPacket.length < 7) {
            return null;
        }

        // 1. 校验 Magic Header
        if (rawPacket[0] != HuaweiTLVBuilder.MAGIC_HEADER) {
            return null;
        }

        // 2. 校验 CRC16
        int length = rawPacket.length;
        int expectedCrc = ((rawPacket[length - 1] & 0xFF) << 8) | (rawPacket[length - 2] & 0xFF);
        int calculatedCrc = HuaweiTLVBuilder.calculateCrc16(rawPacket, 0, length - 2);
        if (expectedCrc != calculatedCrc) {
            return null;
        }

        TelemetryData result = new TelemetryData();
        int offset = 5; // 跳过 Header(1) + Length(2) + ServiceId(1) + CommandId(1)
        int payloadEnd = length - 2;

        // 3. 循环遍历 TLV 结构
        while (offset + 2 <= payloadEnd) {
            byte tag = rawPacket[offset++];
            int tlvLen = rawPacket[offset++] & 0xFF;

            if (offset + tlvLen > payloadEnd) {
                break; // 越界保护
            }

            switch (tag) {
                case TAG_HEART_RATE:
                    if (tlvLen >= 1) {
                        int hr = rawPacket[offset] & 0xFF;
                        if (hr >= 30 && hr <= 230) {
                            result.heartRate = hr;
                            result.isWorn = true;
                        } else if (hr == 0 || hr == 255) {
                            result.heartRate = 0;
                            result.isWorn = false; // 测不到心率视为离腕
                        }
                    }
                    break;

                case TAG_STEP_CADENCE:
                    if (tlvLen >= 2) {
                        int cadence = ((rawPacket[offset] & 0xFF) << 8) | (rawPacket[offset + 1] & 0xFF);
                        result.stepDelta = cadence;
                        result.motionIntensity = Math.min(10.0f, (cadence / 60.0f) * 1.5f);
                    }
                    break;

                case TAG_WEAR_STATE:
                    if (tlvLen >= 1) {
                        result.isWorn = (rawPacket[offset] == 0x01);
                    }
                    break;

                case TAG_BATTERY:
                    if (tlvLen >= 1) {
                        result.batteryLevel = rawPacket[offset] & 0xFF;
                    }
                    break;

                default:
                    break;
            }

            offset += tlvLen;
        }

        return result;
    }
}

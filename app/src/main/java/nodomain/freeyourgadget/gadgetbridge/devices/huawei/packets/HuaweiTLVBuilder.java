package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 华为手环 BLE 协议 TLV (Type-Length-Value) 数据包组装与校验工具类
 * 
 * 华为手环协议标准帧结构:
 * [0x5A] [Packet Length (2 bytes)] [Service ID] [Command ID] [TLV Payload...] [CRC16 (2 bytes)]
 */
public class HuaweiTLVBuilder {

    public static final byte MAGIC_HEADER = 0x5A;

    private final byte serviceId;
    private final byte commandId;
    private final ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();

    public HuaweiTLVBuilder(byte serviceId, byte commandId) {
        this.serviceId = serviceId;
        this.commandId = commandId;
    }

    /**
     * 添加 1 字节整数 TLV
     */
    public HuaweiTLVBuilder addByte(byte tag, int value) {
        payloadStream.write(tag);
        payloadStream.write(0x01); // Length = 1
        payloadStream.write((byte) (value & 0xFF));
        return this;
    }

    /**
     * 添加 2 字节整数 TLV (Big Endian)
     */
    public HuaweiTLVBuilder addShort(byte tag, int value) {
        payloadStream.write(tag);
        payloadStream.write(0x02); // Length = 2
        payloadStream.write((byte) ((value >> 8) & 0xFF));
        payloadStream.write((byte) (value & 0xFF));
        return this;
    }

    /**
     * 添加 4 字节整数 TLV (Big Endian)
     */
    public HuaweiTLVBuilder addInt(byte tag, long value) {
        payloadStream.write(tag);
        payloadStream.write(0x04); // Length = 4
        payloadStream.write((byte) ((value >> 24) & 0xFF));
        payloadStream.write((byte) ((value >> 16) & 0xFF));
        payloadStream.write((byte) ((value >> 8) & 0xFF));
        payloadStream.write((byte) (value & 0xFF));
        return this;
    }

    /**
     * 添加原始字节数组 TLV
     */
    public HuaweiTLVBuilder addBytes(byte tag, byte[] bytes) {
        if (bytes == null) return this;
        payloadStream.write(tag);
        if (bytes.length < 128) {
            payloadStream.write((byte) bytes.length);
        } else {
            payloadStream.write((byte) (0x80 | ((bytes.length >> 8) & 0x7F)));
            payloadStream.write((byte) (bytes.length & 0xFF));
        }
        try {
            payloadStream.write(bytes);
        } catch (IOException ignored) {}
        return this;
    }

    /**
     * 完成封包，计算 CRC16 并生成最终 BLE 发送字节流
     */
    public byte[] build() {
        byte[] payload = payloadStream.toByteArray();

        ByteArrayOutputStream packetStream = new ByteArrayOutputStream();
        packetStream.write(MAGIC_HEADER);

        // 长度字段 (Srv + Cmd + Payload)
        int bodyLength = 1 + 1 + payload.length;
        packetStream.write((byte) ((bodyLength >> 8) & 0xFF));
        packetStream.write((byte) (bodyLength & 0xFF));

        packetStream.write(serviceId);
        packetStream.write(commandId);
        try {
            packetStream.write(payload);
        } catch (IOException ignored) {}

        byte[] rawWithoutCrc = packetStream.toByteArray();
        int crc = calculateCrc16(rawWithoutCrc, 0, rawWithoutCrc.length);

        packetStream.write((byte) (crc & 0xFF));
        packetStream.write((byte) ((crc >> 8) & 0xFF));

        return packetStream.toByteArray();
    }

    /**
     * 华为手环标准 CRC16-CCITT 校验计算 (Poly: 0x1021, Init: 0x0000)
     */
    public static int calculateCrc16(byte[] data, int offset, int length) {
        int crc = 0x0000;
        for (int i = offset; i < offset + length; i++) {
            crc ^= (data[i] & 0xFF) << 8;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
                } else {
                    crc = (crc << 1) & 0xFFFF;
                }
            }
        }
        return crc;
    }
}

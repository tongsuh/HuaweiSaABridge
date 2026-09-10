/*  Copyright (C) 2024 Gadgetbridge Contributors
    This file is part of Gadgetbridge.
    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
*/
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.HuaweiVibrateCommander;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;

public class SendVibrateRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SendVibrateRequest.class);

    private final int intensity;
    private final int repeat;
    private final int durationMs;

    public SendVibrateRequest(HuaweiSupportProvider support, int intensity, int repeat, int durationMs) {
        super(support);
        this.intensity = intensity;
        this.repeat = repeat;
        this.durationMs = durationMs;
        this.serviceId = HuaweiVibrateCommander.SERVICE_DEVICE_CONTROL;
        this.commandId = HuaweiVibrateCommander.COMMAND_SET_VIBRATION;
        this.addToResponse = false;
    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        LOG.debug("Creating Huawei Vibrate Request: intensity={}, repeat={}, duration={}ms", intensity, repeat, durationMs);
        try {
            return new HuaweiVibrateCommander.VibratePacket(paramsProvider, intensity, repeat, durationMs).serialize();
        } catch (nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }

    @Override
    protected void processResponse() {
        LOG.debug("Huawei Vibrate command acknowledged by device");
    }
}

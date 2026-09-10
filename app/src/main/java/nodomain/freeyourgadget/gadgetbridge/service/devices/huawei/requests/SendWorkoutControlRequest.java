/*  Copyright (C) 2024 Gadgetbridge Contributors

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Workout;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;

public class SendWorkoutControlRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SendWorkoutControlRequest.class);

    public static final byte WORKOUT_TYPE_OUTDOOR_RUN = 0x01;
    public static final byte WORKOUT_TYPE_INDOOR_RUN = 0x02;
    public static final byte WORKOUT_TYPE_FREE_TRAIN = 0x08;

    public static final byte ACTION_START = 0x01;
    public static final byte ACTION_PAUSE = 0x02;
    public static final byte ACTION_RESUME = 0x03;
    public static final byte ACTION_STOP = 0x04;

    private final byte workoutType;
    private final byte action;

    public SendWorkoutControlRequest(HuaweiSupportProvider support, byte workoutType, byte action) {
        super(support);
        this.serviceId = Workout.id;
        this.commandId = Workout.WorkoutControl.id;
        this.addToResponse = false;
        this.workoutType = workoutType;
        this.action = action;
    }

    @Override
    protected boolean requestSupported() {
        return true;
    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new Workout.WorkoutControl.Request(paramsProvider, workoutType, action).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }

    @Override
    protected void processResponse() {
        LOG.debug("Handled Workout Control Request: type={}, action={}", workoutType, action);
    }
}

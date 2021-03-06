/*******************************************************************************
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.nd4j.jita.allocator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.DeviceLocalNDArray;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@Slf4j
public class DeviceLocalNDArrayTests {

    @Test
    public void testDeviceLocalArray_1() throws Exception{
        val arr = Nd4j.create(DataType.DOUBLE, 10, 10);

        val dl = new DeviceLocalNDArray(arr);

        for (int e = 0; e < Nd4j.getAffinityManager().getNumberOfDevices(); e++) {
            val t = new Thread(new Runnable() {
                @Override
                public void run() {
                    dl.get().addi(1.0);
                    Nd4j.getExecutioner().commit();
                }
            });
            Nd4j.getAffinityManager().attachThreadToDevice(t, e);
            t.start();
            t.join();
        }
    }


    @Test
    public void testDeviceLocalArray_2() throws Exception{
        val shape = new long[]{10, 10};
        val arr = Nd4j.create(DataType.DOUBLE, shape);

        val dl = new DeviceLocalNDArray(arr);

        for (int e = 0; e < Nd4j.getAffinityManager().getNumberOfDevices(); e++) {
            val t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        val tmp = Nd4j.create(DataType.DOUBLE, shape);
                        tmp.addi(1.0);
                        Nd4j.getExecutioner().commit();
                    }
                }
            });
            Nd4j.getAffinityManager().attachThreadToDevice(t, e);
            t.start();
            t.join();

            System.gc();
        }
        System.gc();

        for (int e = 0; e < Nd4j.getAffinityManager().getNumberOfDevices(); e++) {
            val t = new Thread(new Runnable() {
                @Override
                public void run() {
                    dl.get().addi(1.0);
                    Nd4j.getExecutioner().commit();
                }
            });
            Nd4j.getAffinityManager().attachThreadToDevice(t, e);
            t.start();
            t.join();
        }
    }
}

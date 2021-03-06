/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.core.provider.consumer;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.core.CseContext;
import mockit.Injectable;

public class TestReferenceConfigUtils {
    @Test
    public void testNotReady() {
        String exceptionMessage = "System is not ready for remote calls. "
                + "When beans are making remote calls in initialization, it's better to "
                + "implement io.servicecomb.core.BootListener and do it after EventType.AFTER_REGISTRY.";

        ReferenceConfigUtils.setReady(false);
        try {
            ReferenceConfigUtils.getForInvoke("abc");
            Assert.fail("must throw exception");
        } catch (IllegalStateException e) {
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }

        try {
            ReferenceConfigUtils.getForInvoke("abc", "v1", "");
            Assert.fail("must throw exception");
        } catch (IllegalStateException e) {
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test
    public void testReady(@Injectable ConsumerProviderManager consumerProviderManager) {
        ReferenceConfigUtils.setReady(true);
        CseContext.getInstance().setConsumerProviderManager(consumerProviderManager);

        Assert.assertNotNull(ReferenceConfigUtils.getForInvoke("abc"));
        Assert.assertNotNull(ReferenceConfigUtils.getForInvoke("abc", "v1", ""));
    }
}

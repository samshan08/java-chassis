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

package io.servicecomb.transport.rest.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.transport.AbstractTransport;
import io.servicecomb.foundation.common.net.NetUtils;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.foundation.vertx.SimpleJsonObject;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.transport.rest.client.RestTransportClient;
import io.servicecomb.transport.rest.client.RestTransportClientManager;
import io.vertx.core.DeploymentOptions;

@Component
public class VertxRestTransport extends AbstractTransport {
    private static final Logger log = LoggerFactory.getLogger(VertxRestTransport.class);

    @Override
    public String getName() {
        return Const.RESTFUL;
    }

    @Override
    public int getOrder() {
        return -1000;
    }

    @Override
    public boolean canInit() {
        setListenAddressWithoutSchema(TransportConfig.getAddress());

        URIEndpointObject ep = (URIEndpointObject) getEndpoint().getAddress();
        if (ep == null) {
            return true;
        }

        if (!NetUtils.canTcpListen(ep.getSocketAddress().getAddress(), ep.getPort())) {
            log.info("can not listen {}, skip {}.", ep.getSocketAddress(), this.getClass().getName());
            return false;
        }

        return true;
    }

    @Override
    public boolean init() throws Exception {
        // 部署transport server
        DeploymentOptions options = new DeploymentOptions().setInstances(TransportConfig.getThreadCount());
        SimpleJsonObject json = new SimpleJsonObject();
        json.put(ENDPOINT_KEY, getEndpoint());
        options.setConfig(json);
        return VertxUtils.blockDeploy(transportVertx, RestServerVerticle.class, options) && deployClient();
    }

    private boolean deployClient() {
        return RestTransportClientManager.INSTANCE.getRestTransportClient(true) != null &&
            RestTransportClientManager.INSTANCE.getRestTransportClient(false) != null;
    }

    @Override
    public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        URIEndpointObject endpoint = (URIEndpointObject) invocation.getEndpoint().getAddress();
        RestTransportClient client =
            RestTransportClientManager.INSTANCE.getRestTransportClient(endpoint.isSslEnabled());
        log.debug("Sending request by rest to endpoint {}:{}", endpoint.getHostOrIp(), endpoint.getPort());
        client.send(invocation, asyncResp);
    }
}

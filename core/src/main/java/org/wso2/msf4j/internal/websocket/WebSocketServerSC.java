/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.msf4j.internal.websocket;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.msf4j.internal.DataHolder;
import org.wso2.msf4j.internal.MSF4JConstants;
import org.wso2.msf4j.websocket.WebSocketEndpoint;
import org.wso2.msf4j.websocket.WebSocketEndpointsRegistry;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * OSGi Service component for WebSocket server. This will identify the endpoints which are trying to identify
 * and register them as WebSocket Server Endpoints
 */
@Component(
        name = "org.wso2.msf4j.internal.websocket.WebSocketServerSC",
        immediate = true,
        property = {
                "componentName=wso2-websocket-server"
        }
)
public class WebSocketServerSC implements RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServerSC.class);
    private EndpointsRegistryImpl endpointsRegistry = new EndpointsRegistryImpl();

    @Activate
    protected void start(final BundleContext bundleContext) {
        if (log.isDebugEnabled()) {
            log.debug("Endpoint Activated.");
        }
    }

    /**
     * Add endpoint to the endpoint registry.
     *
     * @param endpoint endpoint which should be added to the registry.
     */
    @Reference(
        name = "websocketEndpoint",
        service = WebSocketEndpoint.class,
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        unbind = "removeEndpoint"
    )
    protected void addEndpoint(WebSocketEndpoint endpoint, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, EndpointsRegistryImpl> endpointRegistries = DataHolder.getInstance().getEndpointsRegistries();
        if (channelId != null) {
            EndpointsRegistryImpl endpointRegistry = endpointRegistries.get(channelId);
            if (endpointRegistry == null) {
                throw new RuntimeException("Couldn't found the endpoint registry for channel ID " + channelId);
            }
            endpointRegistry.addEndpoint(endpoint);
        }
    }

    /**
     * Remove endpoint from the endpoint registry.
     *
     * @param endpoint endpoint which should be removed from the registry.
     */
    protected void removeEndpoint(WebSocketEndpoint endpoint) {
        endpointsRegistry.removeEndpoint(endpoint);
    }

    @Reference(
            name = "ws-connector-provider",
            service = ServerConnector.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCarbonTransport"
    )
    protected void addCarbonTransport(ServerConnector serverConnector) {
        EndpointsRegistryImpl endpointsRegistry = new EndpointsRegistryImpl();
        Map<String, EndpointsRegistryImpl> endpointsRegistries = DataHolder.getInstance().getEndpointsRegistries();
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put(MSF4JConstants.CHANNEL_ID, serverConnector.getId());
        endpointsRegistries.put(serverConnector.getId(), endpointsRegistry);
        DataHolder.getInstance().getBundleContext()
                .registerService(WebSocketEndpointsRegistry.class, endpointsRegistry, properties);
    }

    protected void removeCarbonTransport(ServerConnector serverConnector) {
        DataHolder.getInstance().getMicroservicesRegistries().remove(serverConnector.getId());
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        DataHolder.getInstance().getBundleContext().registerService(WebSocketServerSC.class, this, null);
        log.info("All required capabilities are available of WebSocket service component is available.");
    }
}

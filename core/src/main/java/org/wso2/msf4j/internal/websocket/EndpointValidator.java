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

import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;
import org.wso2.msf4j.websocket.exception.WebSocketMethodParameterException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * This validates all the methods which are relevant to WebSocket Server endpoint using JSR-356 specification.
 */
public class EndpointValidator {

    public EndpointValidator() {
    }

    /**
     * Validate the whole WebSocket endpoint.
     *
     * @param webSocketEndpoint endpoint which should be validated.
     * @return true if validation is completed without any error.
     * @throws WebSocketEndpointAnnotationException if error on an annotation declaration occurred.
     * @throws WebSocketMethodParameterException if the method parameters are invalid for a given method according
     * to JSR-356 specification.
     */
    public boolean validate(Object webSocketEndpoint) throws WebSocketEndpointAnnotationException,
                                                             WebSocketMethodParameterException {
        return validateURI(webSocketEndpoint) && validateOnStringMethod(webSocketEndpoint) &&
                validateOnBinaryMethod(webSocketEndpoint) && validateOnPongMethod(webSocketEndpoint) &&
                validateOnOpenMethod(webSocketEndpoint) && validateOnCloseMethod(webSocketEndpoint) &&
                validateOnErrorMethod(webSocketEndpoint);
    }

    private boolean validateURI(Object webSocketEndpoint) throws WebSocketEndpointAnnotationException {
        if (webSocketEndpoint.getClass().isAnnotationPresent(ServerEndpoint.class)) {
            return true;
        }
        throw new WebSocketEndpointAnnotationException("Server Endpoint is not defined.");
    }

    private boolean validateOnStringMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnStringMessageMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnStringMessageMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        boolean foundPrimaryString = false;
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class) {
                if (parameter.getAnnotation(PathParam.class) == null) {
                    if (foundPrimaryString) {
                        throw new WebSocketMethodParameterException("Invalid method parameter found");
                    }
                    foundPrimaryString = true;
                }
            } else if (paraType != Session.class) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            }
        }
        return foundPrimaryString;
    }

    private boolean validateOnBinaryMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnBinaryMessageMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnBinaryMessageMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        boolean foundPrimaryBuffer = false;
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class && parameter.getAnnotation(PathParam.class) == null) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            } else if (paraType == ByteBuffer.class || paraType == byte[].class) {
                if (foundPrimaryBuffer) {
                    throw new WebSocketMethodParameterException("Invalid method parameter found");
                }
                foundPrimaryBuffer = true;
            } else if (paraType != Session.class) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            }
        }
        return foundPrimaryBuffer;
    }

    private boolean validateOnPongMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnPongMessageMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnPongMessageMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        boolean foundPrimaryPong = false;
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == PongMessage.class) {
                if (foundPrimaryPong) {
                    throw new WebSocketMethodParameterException("Invalid method parameter found");
                }
                foundPrimaryPong = true;
            } else if (paraType != Session.class) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            }
        }
        return foundPrimaryPong;
    }

    private boolean validateOnOpenMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnOpenMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnOpenMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class && parameter.getAnnotation(PathParam.class) == null) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            } else if (paraType != Session.class) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            }
        }
        return true;
    }

    private boolean validateOnCloseMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnCloseMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnCloseMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class && parameter.getAnnotation(PathParam.class) == null) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            } else if (paraType != CloseReason.class || paraType != Session.class) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            }
        }
        return true;
    }

    private boolean validateOnErrorMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnErrorMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnErrorMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        boolean foundPrimaryThrowable = false;
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class && parameter.getAnnotation(PathParam.class) == null) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            } else if (paraType == Throwable.class) {
                if (foundPrimaryThrowable) {
                    throw new WebSocketMethodParameterException("Invalid method parameter found");
                }
                foundPrimaryThrowable = true;
            } else if (paraType != Session.class) {
                throw new WebSocketMethodParameterException("Invalid method parameter found");
            }
        }
        return foundPrimaryThrowable;
    }
}

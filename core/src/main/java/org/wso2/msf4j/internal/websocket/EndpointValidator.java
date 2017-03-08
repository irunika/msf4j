/*
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 * This validates all the methods which are relevant to WebSocket Server endpoint.
 */
public class EndpointValidator {

    public EndpointValidator() {
    }

    public boolean validate(Object webSocketEndpoint) throws WebSocketEndpointAnnotationException,
                                                             WebSocketMethodParameterException {
        return validateURI(webSocketEndpoint) && validateOnStringMethod(webSocketEndpoint) &&
                validateOnBinaryMethod(webSocketEndpoint) && validateOnPongMethod(webSocketEndpoint) &&
                validateOnOpenMethod(webSocketEndpoint) && validateOnCloseMethod(webSocketEndpoint) &&
                validateOnErrorMethod(webSocketEndpoint);
    }

    private boolean validateURI(Object webSocketEndpoint) throws WebSocketEndpointAnnotationException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        if (dispatcher.getUri(webSocketEndpoint) == null) {
            throw new WebSocketEndpointAnnotationException("");
        }
        return true;
    }

    /*
    Validate the String method found by Endpoint Dispatcher.
     */
    private boolean validateOnStringMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method = dispatcher.getOnStringMessageMethod(webSocketEndpoint);

        //If method is not found that means that method is already validated.
        if (method == null) {
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

    /*
    Validate Binary method found by Endpoint Dispatcher
     */
    private boolean validateOnBinaryMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method = dispatcher.getOnBinaryMessageMethod(webSocketEndpoint);

        //If method is not found that means that method is already validated.
        if (method == null) {
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

    /*
    Validate Pong Message found by Endpoint Dispatcher
    */
    private boolean validateOnPongMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method = dispatcher.getOnPongMessageMethod(webSocketEndpoint);

        //If method is not found that means that method is already validated.
        if (method == null) {
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

    /*
    Validate On Open Method found by Dispatcher.
     */
    private boolean validateOnOpenMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method = dispatcher.getOnOpenMethod(webSocketEndpoint);

        //If method is not found that means that method is already validated.
        if (method == null) {
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

    /*
    Validate On Close Method found by Dispatcher.
     */
    private boolean validateOnCloseMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method = dispatcher.getOnCloseMethod(webSocketEndpoint);

        //If method is not found that means that method is already validated.
        if (method == null) {
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

    /*
    Validate on Error Method found by Dispatcher.
     */
    private boolean validateOnErrorMethod(Object webSocketEndpoint) throws WebSocketMethodParameterException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method = dispatcher.getOnErrorMethod(webSocketEndpoint);

        //If method is not found that means that method is already validated.
        if (method == null) {
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

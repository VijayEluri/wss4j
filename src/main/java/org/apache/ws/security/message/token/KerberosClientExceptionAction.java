/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ws.security.message.token;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;

import org.apache.ws.security.WSSecurityException;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 * This class represents a PrivilegedExceptionAction implementation to obtain a service ticket from a Kerberos
 * Key Distribution Center.
 */
public class KerberosClientExceptionAction implements PrivilegedExceptionAction<KerberosContext> {
    private static final String javaVersion = System.getProperty("java.version");
    private static final boolean isJava5Or6 = javaVersion.startsWith("1.5") || javaVersion.startsWith("1.6");
    private static final boolean isOracleJavaVendor = System.getProperty("java.vendor").startsWith("Oracle");
    private static final boolean isIBMJavaVendor = System.getProperty("java.vendor").startsWith("IBM");

    private static final String SUN_JGSS_INQUIRE_TYPE_CLASS = "com.sun.security.jgss.InquireType";
    private static final String SUN_JGSS_EXT_GSSCTX_CLASS = "com.sun.security.jgss.ExtendedGSSContext";

    private static final String IBM_JGSS_INQUIRE_TYPE_CLASS = "com.ibm.security.jgss.InquireType";
    private static final String IBM_JGSS_EXT_GSSCTX_CLASS = "com.ibm.security.jgss.ExtendedGSSContext";

    private Principal clientPrincipal;
    private String serviceName;
    private boolean isUsernameServiceNameForm;
    private boolean requestCredDeleg;
    private GSSCredential delegatedCredential;

    public KerberosClientExceptionAction(Principal clientPrincipal, String serviceName, 
                                         boolean isUsernameServiceNameForm, boolean requestCredDeleg) {
        this(clientPrincipal, serviceName, isUsernameServiceNameForm, requestCredDeleg, null);
    }
    
    public KerberosClientExceptionAction(Principal clientPrincipal, String serviceName, 
                                         boolean isUsernameServiceNameForm, boolean requestCredDeleg,
                                         GSSCredential delegatedCredential) {
        this.clientPrincipal = clientPrincipal;
        this.serviceName = serviceName;
        this.isUsernameServiceNameForm = isUsernameServiceNameForm;
        this.requestCredDeleg = requestCredDeleg;
        this.delegatedCredential = delegatedCredential;
    }
    
    public KerberosContext run() throws GSSException, WSSecurityException {
        GSSManager gssManager = GSSManager.getInstance();

        Oid kerberos5Oid = new Oid("1.2.840.113554.1.2.2");
        
        GSSCredential credentials = delegatedCredential;
        if (credentials == null) {
            GSSName gssClient = gssManager.createName(clientPrincipal.getName(), GSSName.NT_USER_NAME);
            credentials = 
                gssManager.createCredential(
                    gssClient, GSSCredential.DEFAULT_LIFETIME, kerberos5Oid, GSSCredential.INITIATE_ONLY
                );
        }

        GSSName gssService = gssManager.createName(serviceName, isUsernameServiceNameForm ? GSSName.NT_USER_NAME : GSSName.NT_HOSTBASED_SERVICE);
        GSSContext secContext =
            gssManager.createContext(
                gssService, kerberos5Oid, credentials, GSSContext.DEFAULT_LIFETIME
            );

        secContext.requestMutualAuth(false);
        secContext.requestCredDeleg(requestCredDeleg);

        byte[] token = new byte[0];
        byte[] returnedToken = secContext.initSecContext(token, 0, token.length);

        KerberosContext krbCtx = new KerberosContext();
        krbCtx.setGssContext(secContext);
        krbCtx.setKerberosToken(returnedToken);

        if (!isJava5Or6 && (isOracleJavaVendor || isIBMJavaVendor)) {
            try {
                @SuppressWarnings("rawtypes")
                Class inquireType = Class.forName(isOracleJavaVendor ? SUN_JGSS_INQUIRE_TYPE_CLASS : IBM_JGSS_INQUIRE_TYPE_CLASS);

                @SuppressWarnings("rawtypes")
                Class extendedGSSContext = Class.forName(isOracleJavaVendor ? SUN_JGSS_EXT_GSSCTX_CLASS : IBM_JGSS_EXT_GSSCTX_CLASS);

                @SuppressWarnings("unchecked")
                Method inquireSecContext = extendedGSSContext.getMethod("inquireSecContext", inquireType);

                @SuppressWarnings("unchecked")
                Key key = (Key) inquireSecContext.invoke(secContext, Enum.valueOf(inquireType, "KRB5_GET_SESSION_KEY"));

                krbCtx.setSecretKey(key);
            }
            catch (ClassNotFoundException e) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, "kerberosServiceTicketError", new Object[] {}, e
                );
            }
            catch (NoSuchMethodException e) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, "kerberosServiceTicketError", new Object[] {}, e
                );
            }
            catch (InvocationTargetException e) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, "kerberosServiceTicketError", new Object[] {}, e.getCause()
                );
            }
            catch (IllegalAccessException e) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, "kerberosServiceTicketError", new Object[] {}, e
                );
            }
        }

        return krbCtx;
    }
}

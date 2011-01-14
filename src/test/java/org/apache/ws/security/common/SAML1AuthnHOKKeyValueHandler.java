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

package org.apache.ws.security.common;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.saml.ext.SAMLCallback;
import org.apache.ws.security.saml.ext.bean.AuthenticationStatementBean;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean;
import org.apache.ws.security.saml.ext.bean.SubjectBean;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean.CERT_IDENTIFIER;
import org.apache.ws.security.saml.ext.builder.SAML1Constants;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Collections;

/**
 * A Callback Handler implementation for a SAML 1.1 authentication assertion using
 * Holder of Key, and where the KeyInfo object in the subject uses KeyValue.
 */
public class SAML1AuthnHOKKeyValueHandler implements CallbackHandler {
    
    private String subjectName = "uid=joe,ou=people,ou=saml-demo,o=example.com";
    private String subjectQualifier = "www.example.com";
    private X509Certificate[] certs;
    
    public SAML1AuthnHOKKeyValueHandler() throws WSSecurityException {
        Crypto crypto = CryptoFactory.getInstance("crypto.properties");
        certs = crypto.getCertificates("16c73ab6-b892-458f-abf5-2f875f74882e");
    }
    
    public void handle(Callback[] callbacks)
        throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof SAMLCallback) {
                SAMLCallback callback = (SAMLCallback) callbacks[i];
                SubjectBean subjectBean = 
                    new SubjectBean(
                        subjectName, subjectQualifier, SAML1Constants.CONF_HOLDER_KEY
                    );
                KeyInfoBean keyInfo = new KeyInfoBean();
                keyInfo.setCertificate(certs[0]);
                keyInfo.setCertIdentifer(CERT_IDENTIFIER.KEY_VALUE);
                subjectBean.setKeyInfo(keyInfo);
                AuthenticationStatementBean authBean = new AuthenticationStatementBean();
                authBean.setSubject(subjectBean);
                authBean.setAuthenticationMethod("Password");
                callback.setAuthenticationStatementData(Collections.singletonList(authBean));
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }
}

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
package org.apache.ws.security.policy.stax.assertionStates;

import org.apache.ws.security.policy.WSSPolicyException;
import org.apache.ws.security.policy.model.AbstractSecurityAssertion;
import org.apache.ws.security.policy.model.AbstractToken;
import org.apache.ws.security.policy.model.IssuedToken;
import org.apache.xml.security.stax.securityEvent.SecurityEventConstants;
import org.apache.xml.security.stax.securityEvent.TokenSecurityEvent;
import org.swssf.wss.securityEvent.IssuedTokenSecurityEvent;
import org.swssf.wss.securityEvent.WSSecurityEventConstants;

/**
 * WSP1.3, 5.4.2 IssuedToken Assertion
 *
 * @author $Author$
 * @version $Revision$ $Date$
 */

public class IssuedTokenAssertionState extends TokenAssertionState {

    public IssuedTokenAssertionState(AbstractSecurityAssertion assertion, boolean asserted) {
        super(assertion, asserted);
    }

    @Override
    public SecurityEventConstants.Event[] getSecurityEventType() {
        return new SecurityEventConstants.Event[]{
                WSSecurityEventConstants.SecurityContextToken,
                WSSecurityEventConstants.SamlToken,
                WSSecurityEventConstants.RelToken,
        };
    }

    @Override
    public boolean assertToken(TokenSecurityEvent tokenSecurityEvent, AbstractToken abstractToken) throws WSSPolicyException {
        if (!(tokenSecurityEvent instanceof IssuedTokenSecurityEvent)) {
            throw new WSSPolicyException("Expected a IssuedTokenSecurityEvent but got " + tokenSecurityEvent.getClass().getName());
        }

        IssuedToken issuedToken = (IssuedToken) abstractToken;
        IssuedTokenSecurityEvent issuedTokenSecurityEvent = (IssuedTokenSecurityEvent) tokenSecurityEvent;
        if (issuedToken.getIssuerName() != null) {
            if (!issuedToken.getIssuerName().equals(issuedTokenSecurityEvent.getIssuerName())) {
                setErrorMessage("IssuerName in Policy (" + issuedToken.getIssuerName() + ") didn't match with the one in the IssuedToken (" + issuedTokenSecurityEvent.getIssuerName() + ")");
                return false;
            }
        }
        //todo internal/external reference?
        //always return true to prevent false alarm in case additional tokens with the same usage
        //appears in the message but do not fulfill the policy and are also not needed to fulfil the policy.
        return true;
    }
}
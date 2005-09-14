/*
 * Copyright  2003-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ws.security.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Vector;

public class TimestampProcessor implements Processor {
    private static Log log = LogFactory.getLog(TimestampProcessor.class.getName());

    public void handleToken(Element elem, Crypto crypto, Crypto decCrypto, CallbackHandler cb, WSDocInfo wsDocInfo, Vector returnResults) throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Found Timestamp list element");
        }
        /*
         * Decode Timestamp, add the found time (created/expiry) to result
         */
        Timestamp timestamp = new Timestamp((Element) elem);
        handleTimestamp(timestamp);
        returnResults.add(0,
                new WSSecurityEngineResult(WSConstants.TS,
                        timestamp));
    }

    public void handleTimestamp(Timestamp timestamp) throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to verify the timestamp");

            DateFormat zulu = new XmlSchemaDateFormat();

            log.debug("Current time: " + zulu.format(Calendar.getInstance().getTime()));
            log.debug("Timestamp created: " + zulu.format(timestamp.getCreated().getTime()));
            log.debug("Timestamp expires: " + zulu.format(timestamp.getExpires().getTime()));
        }

        // Validate whether the security semantics have expired
        Calendar rightNow = Calendar.getInstance();
        if (timestamp.getExpires().before(rightNow)) {
            throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "invalidTimestamp", new Object[]{"The security semantics of message have expired"});
        }

        return;
    }
}

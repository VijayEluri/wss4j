package ch.gigerstyle.xmlsec.ext;

import ch.gigerstyle.xmlsec.securityEvent.SecurityEvent;
import ch.gigerstyle.xmlsec.securityEvent.SecurityEventListener;

import java.util.*;

/**
 * User: giger
 * Date: May 15, 2010
 * Time: 11:53:40 AM
 * Copyright 2010 Marc Giger gigerstyle@gmx.ch
 * <p/>
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2, or (at your option) any
 * later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
public class SecurityContextImpl implements SecurityContext {

    private Map<String, SecurityTokenProvider> secretTokenProviders = new HashMap<String, SecurityTokenProvider>();

    private SecurityEventListener securityEventListener;

    @SuppressWarnings("unchecked")
    private Map content = Collections.synchronizedMap(new HashMap());

    @SuppressWarnings("unchecked")
    public <T> void put(String key, T value) {
        content.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) content.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> void putAsList(Class key, T value) {
        List<T> entry = (List<T>) content.get(key);
        if (entry == null) {
            entry = new ArrayList();
            content.put(key, entry);
        }
        entry.add(value);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAsList(Class key) {
        return (List<T>) content.get(key);
    }

    public void registerSecurityTokenProvider(String id, SecurityTokenProvider securityTokenProvider) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        secretTokenProviders.put(id, securityTokenProvider);
    }

    public SecurityTokenProvider getSecurityTokenProvider(String id) {
        return secretTokenProviders.get(id);
    }

    public void setSecurityEventListener(SecurityEventListener securityEventListener) {
        this.securityEventListener = securityEventListener;
    }

    public void registerSecurityEvent(SecurityEvent securityEvent) throws XMLSecurityException {
        if (securityEventListener != null) {
            securityEventListener.registerSecurityEvent(securityEvent);
        }
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.metadata.web.filters;

import com.google.inject.Singleton;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.authentication.server.AuthenticationFilter;
import org.apache.hadoop.security.authentication.server.KerberosAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * This enforces authentication as part of the filter before processing the request.
 * todo: Subclass of {@link org.apache.hadoop.security.authentication.server.AuthenticationFilter}.
 */
@Singleton
public class MetadataAuthenticationFilter extends AuthenticationFilter {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataAuthenticationFilter.class);
    static final String PREFIX = "metadata.http.authentication.";
    static final String BIND_ADDRESS = "bind.address";

    @Override
    protected Properties getConfiguration(String configPrefix, FilterConfig filterConfig) throws ServletException {
        PropertiesConfiguration configuration;
        try {
            configuration = new PropertiesConfiguration("application.properties");
        } catch (ConfigurationException e) {
            throw new ServletException(e);
        }

        Properties config = new Properties();

        config.put(AuthenticationFilter.COOKIE_PATH, "/");

        // add any config passed in as init parameters
        Enumeration<String> enumeration = filterConfig.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            config.put(name, filterConfig.getInitParameter(name));
        }
        // transfer application.properties config items starting with defined prefix
        Iterator<String> itor = configuration.getKeys();
        while (itor.hasNext()) {
            String name = itor.next();
            if (name.startsWith(PREFIX)) {
                String value = configuration.getString(name);
                name = name.substring(PREFIX.length());
                config.put(name, value);
            }
        }

        //Resolve _HOST into bind address
        String bindAddress = config.getProperty(BIND_ADDRESS);
        if (bindAddress == null) {
            LOG.info("No host name configured.  Defaulting to local host name.");
            try {
                bindAddress = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                throw new ServletException("Unable to obtain host name", e);
            }
        }
        String principal = config.getProperty(KerberosAuthenticationHandler.PRINCIPAL);
        if (principal != null) {
            try {
                principal = SecurityUtil.getServerPrincipal(principal, bindAddress);
            } catch (IOException ex) {
                throw new RuntimeException("Could not resolve Kerberos principal name: " + ex.toString(), ex);
            }
            config.put(KerberosAuthenticationHandler.PRINCIPAL, principal);
        }

        return config;
    }

}

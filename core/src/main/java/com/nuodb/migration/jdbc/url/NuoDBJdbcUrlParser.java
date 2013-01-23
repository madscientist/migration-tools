/**
 * Copyright (c) 2012, NuoDB, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of NuoDB, Inc. nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUODB, INC. BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.nuodb.migration.jdbc.url;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.startsWith;

/**
 * @author Sergey Bushik
 */
public class NuoDBJdbcUrlParser implements JdbcUrlParser {

    @Override
    public boolean canParse(String url) {
        return startsWith(url, "jdbc:com.nuodb");
    }

    @Override
    public JdbcUrl parse(String url, Map<String, Object> overrides) {
        return new NuoDBJdbcUrl(url, overrides);
    }

    class NuoDBJdbcUrl implements JdbcUrl {

        private Map<String, Object> properties = new HashMap<String, Object>();

        public NuoDBJdbcUrl(String url, Map<String, Object> overrides) {
            int start = url.indexOf("://") + 3;
            if (start > 0) {
                int params = url.indexOf('?', start);
                if (params > 0) {
                    String pairs[] = url.substring(params + 1).split("&");
                    int length = pairs.length;
                    for (int i = 0; i < length; i++) {
                        String pair[] = pairs[i].split("=");
                        if (pair.length == 2) {
                            properties.put(pair[0], pair[1]);
                        }
                    }

                }
            }
            if (overrides != null) {
                properties.putAll(overrides);
            }
        }

        @Override
        public String getCatalog() {
            return null;
        }

        @Override
        public String getSchema() {
            return (String) properties.get("schema");
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }
    }
}

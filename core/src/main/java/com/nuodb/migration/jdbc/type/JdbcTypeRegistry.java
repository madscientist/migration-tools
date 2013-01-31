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
package com.nuodb.migration.jdbc.type;

import java.util.Collection;
import java.util.Map;

/**
 * @author Sergey Bushik
 */
public interface JdbcTypeRegistry {

    void addJdbcType(JdbcType jdbcType);

    JdbcType getJdbcType(int typeCode);

    JdbcType getJdbcType(int typeCode, String typeName);

    JdbcType getJdbcType(JdbcTypeDesc jdbcTypeDesc);

    void addJdbcTypes(Collection<JdbcType> jdbcTypes);

    void addJdbcTypes(JdbcTypeRegistry jdbcTypeRegistry);

    Collection<JdbcType> getJdbcTypeMap();

    void addJdbcTypeAdapter(JdbcTypeAdapter jdbcTypeAdapter);

    void addJdbcTypeAdapters(Collection<JdbcTypeAdapter> jdbcTypeAdapters);

    Collection<JdbcTypeAdapter> getJdbcTypeAdapterMap();

    JdbcTypeAdapter getJdbcTypeAdapter(Class typeClass);

    void addJdbcTypeDescAlias(int typeCode, int typeCodeAlias);

    void addJdbcTypeDescAlias(int typeCode, String typeName, int typeCodeAlias);

    void addJdbcTypeDescAlias(JdbcTypeDesc jdbcTypeDesc, JdbcTypeDesc jdbcTypeDescAlias);

    JdbcTypeDesc getJdbcTypeDescAlias(int typeCode);

    JdbcTypeDesc getJdbcTypeDescAlias(int typeCode, String typeName);

    JdbcTypeDesc getJdbcTypeDescAlias(JdbcTypeDesc jdbcTypeDesc);

    Map<JdbcTypeDesc, JdbcTypeDesc> getJdbcTypeDescAliases();
}
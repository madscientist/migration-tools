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
package com.nuodb.migration.jdbc.dialect;

import com.nuodb.migration.jdbc.metadata.ReferenceAction;
import com.nuodb.migration.jdbc.type.JdbcTypeRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;

/**
 * Vendor specific database dialect
 *
 * @author Sergey Bushik
 */
public interface Dialect {

    boolean supportsUnique();

    boolean supportsNotNullUnique();

    boolean supportsSessionTimeZone();

    boolean supportsColumnCheck();

    boolean supportsIndexInCreateTable();

    boolean supportsTableCheck();

    boolean supportsTransactionIsolationLevel(int transactionIsolationLevel) throws SQLException;

    boolean supportsIfExistsBeforeTable();

    boolean supportsIfExistsAfterTable();

    void setSessionTimeZone(Connection connection, TimeZone timeZone) throws SQLException;

    void setTransactionIsolationLevel(Connection connection, int[] transactionIsolationLevels) throws SQLException;

    void stream(Statement statement) throws SQLException;

    boolean dropConstraints();

    String quote(String name);

    String getNullColumnString();

    String getNoColumnsInsertString();

    String getColumnComment(String comment);

    String getCascadeConstraintsString();

    String getTableComment(String comment);

    String getIdentityColumnString();

    String getTableCheck(String check);

    String getColumnCheck(String check);

    String getDefaultValue(int typeCode, String defaultValue);

    String getDeleteAction(ReferenceAction deleteAction);

    String getUpdateAction(ReferenceAction updateAction);

    JdbcTypeRegistry getJdbcTypeRegistry();
}

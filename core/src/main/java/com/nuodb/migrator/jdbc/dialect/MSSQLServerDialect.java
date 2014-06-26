/**
 * Copyright (c) 2014, NuoDB, Inc.
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
package com.nuodb.migrator.jdbc.dialect;

import com.nuodb.migrator.jdbc.metadata.Column;
import com.nuodb.migrator.jdbc.metadata.DatabaseInfo;
import com.nuodb.migrator.jdbc.metadata.Table;
import com.nuodb.migrator.jdbc.query.QueryLimit;

import static com.nuodb.migrator.jdbc.dialect.RowCountType.APPROX;
import static com.nuodb.migrator.jdbc.dialect.RowCountType.EXACT;
import static java.lang.String.valueOf;
import static java.sql.Types.*;

/**
 * @author Sergey Bushik
 */
public class MSSQLServerDialect extends SimpleDialect {

    public static final int DATETIMEOFFSET_CODE = -155;
    public static final String DATETIMEOFFSET_NAME = "DATETIMEOFFSET";

    public MSSQLServerDialect(DatabaseInfo databaseInfo) {
        super(databaseInfo);
    }

    @Override
    protected void initJdbcTypes() {
        super.initJdbcTypes();
        addJdbcType(MSSQLServerTimeValue.INSTANCE);
        addJdbcTypeDescAlias(VARCHAR, "TIME", TIME);
        addJdbcTypeDescAlias(VARCHAR, "DATE", DATE);
        addJdbcTypeDescAlias(VARCHAR, "DATETIME2", TIMESTAMP);
        addJdbcTypeDescAlias(LONGVARBINARY, "IMAGE", BLOB);
        addJdbcTypeDescAlias(LONGVARCHAR, "TEXT", CLOB);
        addJdbcTypeDescAlias(LONGNVARCHAR, "XML", CLOB);
        addJdbcTypeDescAlias(DATETIMEOFFSET_CODE, DATETIMEOFFSET_NAME, TIMESTAMP);
    }

    @Override
    public String openQuote() {
        return valueOf('[');
    }

    @Override
    public String closeQuote() {
        return valueOf(']');
    }

    @Override
    public String getNoColumnsInsert() {
        return "DEFAULT VALUES";
    }

    @Override
    public boolean supportsSessionTimeZone() {
        return false;
    }

    @Override
    public boolean supportsRowCount(Table table, Column column, String filter, RowCountType rowCountType) {
        return (rowCountType == APPROX && column == null && filter == null) || rowCountType == EXACT;
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsLimitOffset() {
        return false;
    }

    @Override
    public boolean supportsLimitParameters() {
        return true;
    }

    @Override
    public boolean supportsCatalogs() {
        return true;
    }

    @Override
    public boolean supportsSchemas() {
        return true;
    }

    @Override
    public LimitHandler createLimitHandler(String query, QueryLimit queryLimit) {
        return new MSSQLServerLimitHandler(this, query, queryLimit);
    }

    @Override
    public RowCountHandler createRowCountHandler(Table table, Column column, String filter,
                                                      RowCountType rowCountType) {
        return new MSSQLServerTableRowCountHandler(this, table, column, filter, rowCountType);
    }
}

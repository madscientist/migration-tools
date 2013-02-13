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
package com.nuodb.migration.resultset.format;

import com.nuodb.migration.jdbc.model.ValueModelList;
import com.nuodb.migration.jdbc.type.JdbcTypeDesc;
import com.nuodb.migration.jdbc.type.access.JdbcTypeValueAccess;
import com.nuodb.migration.resultset.format.value.ValueFormat;
import com.nuodb.migration.resultset.format.value.ValueFormatModel;
import com.nuodb.migration.resultset.format.value.ValueVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Sergey Bushik
 */
@SuppressWarnings("unchecked")
public abstract class ResultSetInputBase extends ResultSetFormatBase implements ResultSetInput {

    private transient final Logger logger = LoggerFactory.getLogger(getClass());

    private Reader reader;
    private InputStream inputStream;
    private PreparedStatement preparedStatement;

    public Reader getReader() {
        return reader;
    }

    @Override
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    @Override
    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    @Override
    public void initInputModel() throws SQLException {
        int index = 0;
        for (ValueFormatModel valueFormatModel : getValueFormatModelList()) {
            JdbcTypeDesc typeDesc = getValueAccessProvider().getJdbcTypeDescAlias(
                    new JdbcTypeDesc(valueFormatModel.getTypeCode(), valueFormatModel.getTypeName())
            );
            ValueFormat valueFormat = getValueFormatRegistry().getValueFormat(typeDesc);
            JdbcTypeValueAccess<Object> valueAccess = getValueAccessProvider().getPreparedStatementAccess(
                    getPreparedStatement(), valueFormatModel, index + 1);
            valueFormatModel.setTypeCode(typeDesc.getTypeCode());
            valueFormatModel.setTypeName(typeDesc.getTypeName());

            valueFormatModel.setValueFormat(valueFormat);
            valueFormatModel.setValueAccess(valueAccess);
            visitValueFormatModel(valueFormatModel);
            index++;
        }
    }


    @Override
    public final void readBegin() {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Read input %s", getClass().getName()));
        }
        doReadBegin();
    }

    protected abstract void doReadBegin();

    protected void setValues(ValueVariant[] variants) {
        ValueModelList<ValueFormatModel> valueFormatModelList = getValueFormatModelList();
        for (int index = 0; index < variants.length; index++) {
            ValueFormatModel valueFormatModel = valueFormatModelList.get(index);
            valueFormatModel.getValueFormat().setValue(
                    variants[index], valueFormatModel.getValueAccess(),
                    valueFormatModel.getValueAccessOptions());
        }
    }

    @Override
    public final void readEnd() {
        doReadEnd();
    }

    protected abstract void doReadEnd();
}

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
package com.nuodb.migrator.backup.format.csv;

import com.nuodb.migrator.backup.format.OutputFormatBase;
import com.nuodb.migrator.backup.format.OutputFormatException;
import com.nuodb.migrator.backup.format.value.Value;
import com.nuodb.migrator.backup.format.value.ValueHandle;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import static com.nuodb.migrator.backup.format.utils.BinaryEncoder.BASE64;
import static java.lang.String.valueOf;
import static java.nio.charset.Charset.forName;

/**
 * @author Sergey Bushik
 */
@SuppressWarnings("unchecked")
public class CsvOutputFormat extends OutputFormatBase implements CsvAttributes {

    private String doubleQuote;
    private CSVPrinter csvPrinter;

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    protected void init(OutputStream outputStream) {
        init(new OutputStreamWriter(outputStream, forName((String) getAttribute(ATTRIBUTE_ENCODING, ENCODING))));
    }

    @Override
    protected void init(Writer writer) {
        CsvFormatBuilder builder = new CsvFormatBuilder(this);
        CSVFormat format = builder.build();
        doubleQuote = valueOf(builder.getQuote()) + valueOf(builder.getQuote());
        csvPrinter = new CSVPrinter(wrapWriter(writer), format);
    }

    @Override
    public void writeStart() {
        try {
            for (ValueHandle valueHandle : getValueHandleList()) {
                csvPrinter.print(valueHandle.getName());
            }
            csvPrinter.println();
        } catch (IOException exception) {
            throw new OutputFormatException(exception);
        }
    }

    @Override
    public void writeValues(Value[] values) {
        try {
            String[] record = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                String value = null;
                switch (getValueTypes().get(i)) {
                    case BINARY:
                        value = BASE64.encode(values[i].asBytes());
                        break;
                    case STRING:
                        value = values[i].asString();
                        break;
                }
                if (value != null && value.length() == 0) {
                    value = doubleQuote;
                }
                record[i] = value;
            }
            csvPrinter.printRecord(record);
        } catch (IOException exception) {
            throw new OutputFormatException(exception);
        }
    }

    @Override
    public void writeEnd() {
        try {
            if (csvPrinter != null) {
                csvPrinter.flush();
            }
        } catch (IOException exception) {
            throw new OutputFormatException(exception);
        }
    }

    @Override
    public void close() {
        try {
            if (csvPrinter != null) {
                csvPrinter.close();
            }
        } catch (IOException exception) {
            throw new OutputFormatException(exception);
        }
    }
}

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
package com.nuodb.migration.jdbc.metadata;

/**
 * @author Sergey Bushik
 */
public class ForeignKeyReference extends IndentedOutputBase {

    private Column primaryColumn;
    private Column foreignColumn;

    public ForeignKeyReference(Column sourceColumn, Column foreignColumn) {
        this.primaryColumn = sourceColumn;
        this.foreignColumn = foreignColumn;
    }

    public Column getPrimaryColumn() {
        return primaryColumn;
    }

    public Table getPrimaryTable() {
        return primaryColumn.getTable();
    }

    public Column getForeignColumn() {
        return foreignColumn;
    }

    public Table getForeignTable() {
        return foreignColumn.getTable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKeyReference that = (ForeignKeyReference) o;

        if (primaryColumn != null ? !primaryColumn.equals(that.primaryColumn) : that.primaryColumn != null) return false;
        if (foreignColumn != null ? !foreignColumn.equals(that.foreignColumn) : that.foreignColumn != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = primaryColumn != null ? primaryColumn.hashCode() : 0;
        result = 31 * result + (foreignColumn != null ? foreignColumn.hashCode() : 0);
        return result;
    }

    @Override
    public void output(int indent, StringBuilder buffer) {
        outputIndent(indent, buffer);
        buffer.append(qualify(primaryColumn));
        buffer.append("->");
        buffer.append(qualify(foreignColumn));
    }

    protected String qualify(Column column) {
        StringBuilder buffer = new StringBuilder();
        if (column.getTable() != null) {
            buffer.append(column.getTable().getQualifiedName(null));
            buffer.append('.');
        }
        buffer.append(column.getName());
        return buffer.toString();
    }
}
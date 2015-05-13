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
package com.nuodb.migrator.jdbc.type;

import static java.sql.Types.OTHER;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.nuodb.migrator.jdbc.dialect.Dialect;
import com.nuodb.migrator.jdbc.dialect.DialectResolver;
import com.nuodb.migrator.jdbc.dialect.SimpleDialectResolver;
import com.nuodb.migrator.jdbc.metadata.DatabaseInfo;

/**
 * @author Mahesha Godekere
 */
public class JdbcTypeAliasMapTest {

	private DialectResolver dialectResolver;
	private Dialect dialectMSSQL;
	private Dialect dialectNuoDB;

	@BeforeMethod
	public void init() {
		dialectResolver = new SimpleDialectResolver();
		dialectMSSQL = dialectResolver.resolve(new DatabaseInfo(
				"Microsoft SQL Server"));
		dialectNuoDB = dialectResolver.resolve(new DatabaseInfo(
                "NuoDB"));
	}

	@Test(dataProvider = "getTypeAliasNameMSSQL")
	public void testJdbcTypeAliasMSSQL(JdbcType jdbcType, String expected) {

		String actual = dialectMSSQL.getTypeName(new DatabaseInfo(
				"Microsoft SQL Server"), jdbcType);
		Assert.assertEquals(expected, actual);
	}

	@Test(dataProvider = "getTypeAliasNameOracle")
	public void testJdbcTypeAliasOracle(JdbcType jdbcType) {

	    String actual = dialectNuoDB.getTypeName(new DatabaseInfo("Oracle"), jdbcType);
        Assert.assertEquals(actual,"BLOB" );
	}

	@DataProvider(name = "getTypeAliasNameMSSQL")
	public Object[][] createGetTypeNameDataMSSQL() {
		return new Object[][] {
				{ getJdbcType(12, "UNIQUEIDENTIFIER", 36), "VARCHAR(36)" },
				{ getJdbcType(2005, "NTEXT", 0), "CLOB" } 
		};
	}

	   @DataProvider(name = "getTypeAliasNameOracle")
	    public Object[][] createGetTypeNameDataOracle() {
	       return new Object[][] {
	                { getJdbcType(OTHER,"ARRAY")},
	                { getJdbcType(OTHER, "STRUCT")},
	                { getJdbcType(OTHER, "REF")}
	        };
	    }

	private JdbcType getJdbcType(int typeCode, String typeName, int size) {
		JdbcType jdbcType = new JdbcType();
		jdbcType.setTypeCode(typeCode);
		jdbcType.setTypeName(typeName);
		jdbcType.setSize((long)size);
		jdbcType.setPrecision(size);
		return jdbcType;

	}

    private JdbcType getJdbcType(int typeCode, String typeName) {
        JdbcType jdbcType = new JdbcType();
        jdbcType.setTypeCode(typeCode);
        jdbcType.setTypeName(typeName);
        return jdbcType;
    }
}
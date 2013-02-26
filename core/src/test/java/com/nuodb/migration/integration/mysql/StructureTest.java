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
package com.nuodb.migration.integration.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.nuodb.migration.integration.MigrationTestBase;
import com.nuodb.migration.integration.types.MySQLTypes;

/**
 * Test to make sure all the Tables, Constraints, Views, Triggers etc have been
 * migrated.
 * 
 * @author Krishnamoorthy Dhandapani
 */
public class StructureTest extends MigrationTestBase {

	/*
	 * test if all the Tables are migrated with the right columns
	 */
	@Test(groups = { "integrationtest" })
	public void testTables() throws Exception {
		String sqlStr1 = "select TABLE_NAME from information_schema.TABLES where TABLE_SCHEMA = ?";
		String sqlStr2 = "select tablename from system.TABLES where TYPE = 'TABLE' and schema = ?";
		PreparedStatement stmt1 = null, stmt2 = null;
		ResultSet rs1 = null, rs2 = null;
		ArrayList<String> list1 = new ArrayList<String>();
		ArrayList<String> list2 = new ArrayList<String>();
		try {
			stmt1 = sourceConnection.prepareStatement(sqlStr1);
			stmt1.setString(1, sourceConnection.getCatalog());
			rs1 = stmt1.executeQuery();

			Assert.assertNotNull(rs1);

			while (rs1.next()) {
				list1.add(rs1.getString(1).toUpperCase());
			}

			Assert.assertFalse(list1.isEmpty());

			stmt2 = nuodbConnection.prepareStatement(sqlStr2);
			stmt2.setString(1, nuodbSchemaUsed);
			rs2 = stmt2.executeQuery();

			Assert.assertNotNull(rs2);

			while (rs2.next()) {
				list2.add(rs2.getString(1).toUpperCase());
			}

			Assert.assertFalse(list2.isEmpty());

			for (String tname : list1) {
				Assert.assertTrue(list2.contains(tname));
				verifyTableColumns(tname);
			}

		} finally {
			closeAll(rs1, stmt1, rs2, stmt2);
		}
	}

	/*
	 * TODO: Need to add check for complex data types with scale and precision
	 */
	private void verifyTableColumns(String tableName) throws Exception {
		String sqlStr1 = "select * from information_schema.COLUMNS where TABLE_SCHEMA = ? and TABLE_NAME = ? order by ORDINAL_POSITION";
		String sqlStr2 = "select * from  system.FIELDS F inner join system.DATATYPES D on "
				+ "F.DATATYPE = D.ID and F.SCHEMA = ? and F.TABLENAME = ? order by F.FIELDPOSITION";
		String[] colNames = new String[] { "COLUMN_NAME", "ORDINAL_POSITION",
				"COLUMN_DEFAULT", "IS_NULLABLE", "DATA_TYPE",
				"CHARACTER_MAXIMUM_LENGTH", "NUMERIC_PRECISION",
				"NUMERIC_SCALE", "CHARACTER_SET_NAME", "COLLATION_NAME",
				"COLUMN_TYPE" };
		PreparedStatement stmt1 = null, stmt2 = null;
		ResultSet rs1 = null, rs2 = null;
		HashMap<String, HashMap<String, String>> tabColMap = new HashMap<String, HashMap<String, String>>();
		try {
			stmt1 = sourceConnection.prepareStatement(sqlStr1);
			stmt1.setString(1, sourceConnection.getCatalog());
			stmt1.setString(2, tableName);
			rs1 = stmt1.executeQuery();

			Assert.assertNotNull(rs1);

			while (rs1.next()) {
				HashMap<String, String> tabColDetailsMap = new HashMap<String, String>();
				for (String colName : colNames) {
					tabColDetailsMap.put(colName, rs1.getString(colName));
				}

				Assert.assertFalse(tabColDetailsMap.isEmpty());

				tabColMap.put(tabColDetailsMap.get(colNames[0]),
						tabColDetailsMap);
			}

			Assert.assertFalse(tabColMap.isEmpty());

			stmt2 = nuodbConnection.prepareStatement(sqlStr2);
			stmt2.setString(1, nuodbSchemaUsed);
			stmt2.setString(2, tableName);
			rs2 = stmt2.executeQuery();

			Assert.assertNotNull(rs2);

			while (rs2.next()) {
				String colName = rs2.getString("FIELD");
				HashMap<String, String> tabColDetailsMap = tabColMap
						.get(colName);
				Assert.assertNotNull(tabColDetailsMap);
				Assert.assertEquals(colName, tabColDetailsMap.get(colNames[0]));
				Assert.assertEquals(rs2.getInt("JDBCTYPE"), MySQLTypes
						.getMappedJDBCType(tabColDetailsMap.get(colNames[4])));
				// System.out.print("mysqlcoltype="
				// + tabColDetailsMap.get(colNames[4]) + ",");
				// System.out.println("mysqlval="
				// + tabColDetailsMap.get(colNames[5]));
				Assert.assertEquals(rs2.getString("LENGTH"), MySQLTypes
						.getMappedLength(tabColDetailsMap.get(colNames[4]),
								tabColDetailsMap.get(colNames[5])));
				// TBD
				// String val = tabColDetailsMap.get(colNames[7]);
				// Assert.assertEquals(rs2.getInt("SCALE"), val == null ? 0
				// : Integer.parseInt(val));

				// Assert.assertEquals(rs2.getString("PRECISION"),
				// tabColDetailsMap.get(colNames[6]));

				String val = tabColDetailsMap.get(colNames[2]);
				Assert.assertEquals(rs2.getString("DEFAULTVALUE"), MySQLTypes
						.getMappedDefault(tabColDetailsMap.get(colNames[4]),
								tabColDetailsMap.get(colNames[2])));
			}
		} finally {
			closeAll(rs1, stmt1, rs2, stmt2);
		}
	}

	/*
	 * test if all the Primary and Unique Key Constraints are migrated
	 */
	@Test(groups = { "integrationtest" })
	public void testPrimaryAndUniqueKeyConstraints() throws Exception {
		String sqlStr1 = "select TC.TABLE_NAME, C.COLUMN_NAME, C.COLUMN_KEY from information_schema.TABLE_CONSTRAINTS TC "
				+ "inner join information_schema.COLUMNS C on TC.CONSTRAINT_SCHEMA=? "
				+ "and C.TABLE_SCHEMA = TC.CONSTRAINT_SCHEMA "
				+ "and TC.TABLE_NAME=C.TABLE_NAME AND C.COLUMN_KEY=SUBSTRING(TC.CONSTRAINT_TYPE,1,3) "
				+ "AND C.COLUMN_KEY IN ('PRI', 'UNI')";
		String sqlStr2 = "SELECT FIELD FROM SYSTEM.INDEXES INNER JOIN SYSTEM.INDEXFIELDS ON "
				+ "INDEXES.SCHEMA=INDEXFIELDS.SCHEMA AND "
				+ "INDEXES.TABLENAME=INDEXFIELDS.TABLENAME AND "
				+ "INDEXES.INDEXNAME=INDEXFIELDS.INDEXNAME WHERE SCHEMA=? AND TABLENAME=? AND INDEXTYPE=?";
		PreparedStatement stmt1 = null, stmt2 = null;
		ResultSet rs1 = null, rs2 = null;
		try {
			stmt1 = sourceConnection.prepareStatement(sqlStr1);
			stmt1.setString(1, sourceConnection.getCatalog());
			rs1 = stmt1.executeQuery();

			Assert.assertNotNull(rs1);

			while (rs1.next()) {
				String tName = rs1.getString("TABLE_NAME");
				String cName = rs1.getString("COLUMN_NAME");
				String cKey = rs1.getString("COLUMN_KEY");

				stmt2 = nuodbConnection.prepareStatement(sqlStr2);
				stmt2.setString(1, nuodbSchemaUsed);
				stmt2.setString(2, tName);
				stmt2.setInt(3, MySQLTypes.getKeyType(cKey));
				rs2 = stmt2.executeQuery();
				boolean found = false;
				while (rs2.next()) {
					found = true;
					Assert.assertEquals(rs2.getString(1), cName);
				}
				Assert.assertTrue(found);
				rs2.close();
				stmt2.close();
			}
		} finally {
			closeAll(rs1, stmt1, rs2, stmt2);
		}
	}

	/*
	 * test if all the Check Constraints are migrated
	 */
	@Test(groups = { "integrationtest", "disabled" })
	public void testCheckConstraints() throws Exception {
		// MYSQL Does not have any implementations for CHECK constraints
	}

	/*
	 * test if all the Foreign Key Constraints are migrated
	 */
	@Test(groups = { "integrationtest" })
	public void testForeignKeyConstraints() throws Exception {
		String sqlStr1 = "select TC.TABLE_NAME, CU.COLUMN_NAME, CU.REFERENCED_TABLE_NAME, CU.REFERENCED_COLUMN_NAME "
				+ "from information_schema.TABLE_CONSTRAINTS TC INNER JOIN information_schema.KEY_COLUMN_USAGE CU "
				+ "on TC.CONSTRAINT_SCHEMA=? and CU.TABLE_SCHEMA = TC.CONSTRAINT_SCHEMA and "
				+ "TC.TABLE_NAME = CU.TABLE_NAME AND TC.CONSTRAINT_TYPE = 'FOREIGN KEY';";
		String sqlStr2 = "SELECT PRIMARYTABLE.SCHEMA AS PKTABLE_SCHEM, PRIMARYTABLE.TABLENAME AS PKTABLE_NAME, "
				+ " PRIMARYFIELD.FIELD AS PKCOLUMN_NAME, FOREIGNTABLE.SCHEMA AS FKTABLE_SCHEM, "
				+ " FOREIGNTABLE.TABLENAME AS FKTABLE_NAME, FOREIGNFIELD.FIELD AS FKCOLUMN_NAME, "
				+ " FOREIGNKEYS.POSITION+1 AS KEY_SEQ, FOREIGNKEYS.UPDATERULE AS UPDATE_RULE, "
				+ " FOREIGNKEYS.DELETERULE AS DELETE_RULE, FOREIGNKEYS.DEFERRABILITY AS DEFERRABILITY "
				+ "FROM SYSTEM.FOREIGNKEYS "
				+ "INNER JOIN SYSTEM.TABLES PRIMARYTABLE ON PRIMARYTABLEID=PRIMARYTABLE.TABLEID "
				+ "INNER JOIN SYSTEM.FIELDS PRIMARYFIELD ON PRIMARYTABLE.SCHEMA=PRIMARYFIELD.SCHEMA "
				+ "AND PRIMARYTABLE.TABLENAME=PRIMARYFIELD.TABLENAME "
				+ "AND FOREIGNKEYS.PRIMARYFIELDID=PRIMARYFIELD.FIELDID "
				+ "INNER JOIN SYSTEM.TABLES FOREIGNTABLE ON FOREIGNTABLEID=FOREIGNTABLE.TABLEID "
				+ "INNER JOIN SYSTEM.FIELDS FOREIGNFIELD ON FOREIGNTABLE.SCHEMA=FOREIGNFIELD.SCHEMA "
				+ "AND FOREIGNTABLE.TABLENAME=FOREIGNFIELD.TABLENAME "
				+ "AND FOREIGNKEYS.FOREIGNFIELDID=FOREIGNFIELD.FIELDID "
				+ "WHERE SCHEMA=? AND TABLENAME=? ORDER BY PKTABLE_SCHEM, PKTABLE_NAME, KEY_SEQ ASC";
		PreparedStatement stmt1 = null, stmt2 = null;
		ResultSet rs1 = null, rs2 = null;
		try {
			stmt1 = sourceConnection.prepareStatement(sqlStr1);
			stmt1.setString(1, sourceConnection.getCatalog());
			rs1 = stmt1.executeQuery();

			Assert.assertNotNull(rs1);

			while (rs1.next()) {
				String tName = rs1.getString("TABLE_NAME");
				String cName = rs1.getString("COLUMN_NAME");
				String rtName = rs1.getString("REFERENCED_TABLE_NAME");
				String rcName = rs1.getString("REFERENCED_COLUMN_NAME");

				stmt2 = nuodbConnection.prepareStatement(sqlStr2);
				stmt2.setString(1, nuodbSchemaUsed);
				stmt2.setString(2, tName);
				rs2 = stmt2.executeQuery();
				boolean found = false;
				while (rs2.next()) {
					found = true;
					Assert.assertEquals(rs2.getString("FKTABLE_SCHEM"),
							rs2.getString("PKTABLE_SCHEM"));
					Assert.assertEquals(rs2.getString("FKTABLE_NAME"), tName);
					Assert.assertEquals(rs2.getString("FKCOLUMN_NAME"), cName);
					Assert.assertEquals(rs2.getString("PKTABLE_NAME"), rtName);
					Assert.assertEquals(rs2.getString("PKCOLUMN_NAME"), rcName);
				}
				Assert.assertTrue(found);
				rs2.close();
				stmt2.close();
			}
		} finally {
			closeAll(rs1, stmt1, rs2, stmt2);
		}
	}

	/*
	 * test if all the auto increment settings are migrated
	 */
	@Test(groups = { "integrationtest" })
	public void testAutoIncrement() throws Exception {
		String sqlStr1 = "select T.TABLE_NAME, T.AUTO_INCREMENT, C.COLUMN_NAME "
				+ "from information_schema.TABLES T INNER JOIN information_schema.COLUMNS C "
				+ "on T.TABLE_SCHEMA=? and C.TABLE_SCHEMA = T.TABLE_SCHEMA and "
				+ "T.TABLE_NAME = C.TABLE_NAME AND C.EXTRA = 'auto_increment' AND "
				+ "T.AUTO_INCREMENT IS NOT NULL";
		String sqlStr2 = "SELECT S.SEQUENCENAME FROM SYSTEM.SEQUENCES S "
				+ "INNER JOIN SYSTEM.FIELDS F ON S.SCHEMA=F.SCHEMA "
				+ "WHERE F.SCHEMA=? AND F.TABLENAME=? AND F.FIELD=?";
		PreparedStatement stmt1 = null, stmt2 = null;
		ResultSet rs1 = null, rs2 = null;
		try {
			stmt1 = sourceConnection.prepareStatement(sqlStr1);
			stmt1.setString(1, sourceConnection.getCatalog());
			rs1 = stmt1.executeQuery();

			Assert.assertNotNull(rs1);

			while (rs1.next()) {
				String tName = rs1.getString("TABLE_NAME");
				String cName = rs1.getString("COLUMN_NAME");
				long ai = rs1.getLong("AUTO_INCREMENT");
				stmt2 = nuodbConnection.prepareStatement(sqlStr2);
				stmt2.setString(1, nuodbSchemaUsed);
				stmt2.setString(2, tName);
				stmt2.setString(3, cName);
				rs2 = stmt2.executeQuery();
				boolean found = false;
				while (rs2.next()) {
					found = true;
					Assert.assertNotNull(rs2.getString("SEQUENCENAME"));
					Assert.assertEquals(rs2.getString("SEQUENCENAME")
							.substring(0, 4), "SEQ_");
					// TODO: Need to check start value - Don't know how yet
				}
				Assert.assertTrue(found);
				rs2.close();
				stmt2.close();

			}
		} finally {
			closeAll(rs1, stmt1, rs2, stmt2);
		}
	}
}

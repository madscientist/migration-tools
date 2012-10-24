package com.nuodb.tools.migration;


import com.nuodb.tools.migration.dump.output.OutputFormat;
import com.nuodb.tools.migration.dump.output.OutputFormatLookupImpl;
import com.nuodb.tools.migration.jdbc.query.SelectQuery;
import com.nuodb.tools.migration.jdbc.metamodel.Column;
import com.nuodb.tools.migration.jdbc.metamodel.Database;
import com.nuodb.tools.migration.jdbc.metamodel.Schema;
import com.nuodb.tools.migration.jdbc.metamodel.Table;
import com.nuodb.tools.migration.spec.DriverManagerConnectionSpec;

public class TestConstants {

    public final static String[] ARGUMENTS = new String[]{
            "dump",
            "--source.driver=com.mysql.jdbc.Driver",
            "--source.url=jdbc:mysql://localhost:3306/test",
            "--source.username=root",
            "--output.type=cvs",
            "--output.path=/tmp/"
    };

    public static final String TEST_CATALOG_NAME = "";
    public static final String TEST_SCHEMA_NAME = "HOCKEY";
    public static final String TEST_TABLE_NAME = "hockey";
    public static final String FIRST_COLUMN_NAME = "ID";
    public static final String SECOND_COLUMN_NAME = "Name";

    public static DriverManagerConnectionSpec createTestNuoDBConnectionSpec() {
        final DriverManagerConnectionSpec connectionSpec = new DriverManagerConnectionSpec();
        connectionSpec.setUrl("jdbc:com.nuodb://localhost/test");
        connectionSpec.setUsername("dba");
        connectionSpec.setPassword("goalie");
        connectionSpec.setDriver("com.nuodb.jdbc.Driver");
        return connectionSpec;
    }

    public static OutputFormat getDefaultOutputFormat() throws IllegalAccessException, InstantiationException {
        return new OutputFormatLookupImpl().getDefaultFormatClass().newInstance();
    }

    public static SelectQuery createTestSelectQuery() {

        final SelectQuery query = new SelectQuery();
        final Database database = new Database();
        final Schema schema = database.createSchema(TEST_CATALOG_NAME, TEST_SCHEMA_NAME);
        final Table table = schema.createTable(TEST_TABLE_NAME, Table.TABLE);
        //testTable.createColumn(FIRST_COLUMN_NAME);
        //testTable.createColumn(SECOND_COLUMN_NAME);
        query.addTable(table);
        query.addColumn(new Column(table, FIRST_COLUMN_NAME));

        return query;
    }
}

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
package com.nuodb.migration.schema;

import com.google.common.collect.Maps;
import com.nuodb.migration.jdbc.connection.ConnectionProvider;
import com.nuodb.migration.jdbc.connection.ConnectionProviderFactory;
import com.nuodb.migration.jdbc.dialect.NuoDBDialect;
import com.nuodb.migration.jdbc.metadata.generator.*;
import com.nuodb.migration.jdbc.type.JdbcTypeNameMap;
import com.nuodb.migration.job.JobExecutor;
import com.nuodb.migration.job.JobExecutors;
import com.nuodb.migration.job.JobFactory;
import com.nuodb.migration.job.TraceJobExecutionListener;
import com.nuodb.migration.spec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static com.nuodb.migration.jdbc.metadata.generator.DatabaseGenerator.GROUP_SCRIPTS_BY;
import static com.nuodb.migration.jdbc.metadata.generator.WriterScriptExporter.SYSTEM_OUT_SCRIPT_EXPORTER;
import static com.nuodb.migration.jdbc.type.JdbcTypeSpecifiers.newSizePrecisionScale;
import static com.nuodb.migration.utils.ValidationUtils.isNotNull;

/**
 * @author Sergey Bushik
 */
public class SchemaJobFactory extends ConnectionProviderFactory implements JobFactory<SchemaJob> {

    public static final boolean FAIL_ON_EMPTY_SCRIPTS = true;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private SchemaSpec schemaSpec;
    private boolean failOnEmptyScripts = FAIL_ON_EMPTY_SCRIPTS;

    @Override
    public SchemaJob createJob() {
        isNotNull(schemaSpec, "Generate schema spec is required");
        SchemaJob schemaJob = new SchemaJob();
        schemaJob.setConnectionProvider(createConnectionProvider(schemaSpec.getSourceConnectionSpec()));
        schemaJob.setScriptGeneratorContext(createScriptGeneratorContext());
        schemaJob.setFailOnEmptyScripts(isFailOnEmptyScripts());
        schemaJob.setScriptExporter(createScriptExporter());
        return schemaJob;
    }

    protected ConnectionProvider createConnectionProvider(ConnectionSpec connectionSpec) {
        return connectionSpec != null ? createConnectionProvider(connectionSpec, false) : null;
    }

    protected ScriptGeneratorContext createScriptGeneratorContext() {
        ScriptGeneratorContext context = new ScriptGeneratorContext();
        context.getAttributes().put(GROUP_SCRIPTS_BY, getSchemaSpec().getGroupScriptsBy());
        context.setMetaDataTypes(getSchemaSpec().getMetaDataTypes());
        context.setScriptTypes(getSchemaSpec().getScriptTypes());

        NuoDBDialect dialect = new NuoDBDialect();
        JdbcTypeNameMap jdbcTypeNameMap = dialect.getJdbcTypeNameMap();
        for (JdbcTypeSpec jdbcTypeSpec : getSchemaSpec().getJdbcTypeSpecs()) {
            jdbcTypeNameMap.addTypeName(
                    jdbcTypeSpec.getTypeCode(), jdbcTypeSpec.getTypeName(),
                    newSizePrecisionScale(
                            jdbcTypeSpec.getSize(), jdbcTypeSpec.getPrecision(), jdbcTypeSpec.getScale()));
        }
        dialect.setIdentifierQuoting(getSchemaSpec().getIdentifierQuoting());
        dialect.setIdentifierNormalizer(getSchemaSpec().getIdentifierNormalizer());
        context.setDialect(dialect);

        ConnectionSpec connectionSpec = getSchemaSpec().getTargetConnectionSpec();
        if (connectionSpec != null) {
            context.setCatalog(connectionSpec.getCatalog());
            context.setSchema(connectionSpec.getSchema());
        }
        return context;
    }

    protected ScriptExporter createScriptExporter() {
        Collection<ScriptExporter> exporters = newArrayList();
        ConnectionProvider connectionProvider = createConnectionProvider(schemaSpec.getTargetConnectionSpec());
        if (connectionProvider != null) {
            exporters.add(new ConnectionScriptExporter(connectionProvider.getConnectionServices()));
        }
        ResourceSpec outputSpec = schemaSpec.getOutputSpec();
        if (outputSpec != null) {
            exporters.add(new FileScriptExporter(outputSpec.getPath()));
        }
        // Fallback to the standard output if neither target connection nor target file were specified
        if (exporters.isEmpty()) {
            exporters.add(SYSTEM_OUT_SCRIPT_EXPORTER);
        }
        return new CompositeScriptExporter(exporters);
    }

    public SchemaSpec getSchemaSpec() {
        return schemaSpec;
    }

    public void setSchemaSpec(SchemaSpec schemaSpec) {
        this.schemaSpec = schemaSpec;
    }

    public boolean isFailOnEmptyScripts() {
        return failOnEmptyScripts;
    }

    public void setFailOnEmptyScripts(boolean failOnEmptyScripts) {
        this.failOnEmptyScripts = failOnEmptyScripts;
    }

    public static void main(String[] args) {
        SchemaJobFactory jobFactory = new SchemaJobFactory();
        jobFactory.setSchemaSpec(new SchemaSpec() {
            {
                DriverConnectionSpec sourceConnectionSpec = new DriverConnectionSpec();
                sourceConnectionSpec.setDriverClassName("com.mysql.jdbc.Driver");
                sourceConnectionSpec.setUrl("jdbc:mysql://localhost:3306/mysql");
                sourceConnectionSpec.setUsername("root");

                DriverConnectionSpec targetConnectionSpec = new DriverConnectionSpec();
                targetConnectionSpec.setDriverClassName("com.nuodb.jdbc.Driver");
                targetConnectionSpec.setUrl("jdbc:com.nuodb://localhost/test");
                targetConnectionSpec.setUsername("dba");
                targetConnectionSpec.setPassword("goalie");
                targetConnectionSpec.setSchema("hockey");

                ResourceSpec outputSpec = new ResourceSpec();
                outputSpec.setPath("/tmp/test/schema.sql");

                setOutputSpec(outputSpec);
                setSourceConnectionSpec(sourceConnectionSpec);
                //setTargetConnectionSpec(targetConnectionSpec);
            }
        });
        JobExecutor executor = JobExecutors.createJobExecutor(jobFactory.createJob());
        executor.addJobExecutionListener(new TraceJobExecutionListener());
        executor.execute(Maps.<String, Object>newHashMap());
    }
}
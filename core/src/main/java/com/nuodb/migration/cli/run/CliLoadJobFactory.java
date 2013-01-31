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
package com.nuodb.migration.cli.run;

import com.nuodb.migration.cli.CliResources;
import com.nuodb.migration.cli.parse.CommandLine;
import com.nuodb.migration.cli.parse.Option;
import com.nuodb.migration.cli.parse.option.OptionFormat;
import com.nuodb.migration.cli.parse.option.OptionToolkit;
import com.nuodb.migration.jdbc.query.InsertType;
import com.nuodb.migration.load.LoadJobFactory;
import com.nuodb.migration.spec.LoadSpec;

/**
 * @author Sergey Bushik
 */
public class CliLoadJobFactory extends CliRunSupport implements CliRunFactory, CliResources {

    private static final String COMMAND = "load";

    public CliLoadJobFactory(OptionToolkit optionToolkit) {
        super(optionToolkit);
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public CliRun createCliRun() {
        return new CliLoadJob();
    }

    class CliLoadJob extends CliRunJob {

        public CliLoadJob() {
            super(CliLoadJobFactory.this.getOptionFormat(), COMMAND, new LoadJobFactory());
        }

        @Override
        protected Option createOption() {
            return newGroup()
                    .withName(getResources().getMessage(LOAD_GROUP_NAME))
                    .withOption(createTargetGroup())
                    .withOption(createInputGroup())
                    .withOption(createReplaceOption())
                    .withOption(createTimeZoneOption())
                    .withRequired(true).build();
        }

        @Override
        protected void bind(CommandLine commandLine) {
            LoadSpec loadSpec = new LoadSpec();
            loadSpec.setTargetConnectionSpec(parseTargetGroup(commandLine, this));
            loadSpec.setInputSpec(parseInputGroup(commandLine, this));
            loadSpec.setTimeZone(parseTimeZoneOption(commandLine, this));
            loadSpec.setInsertType(parseReplaceOption(commandLine, this));
            ((LoadJobFactory) getJobFactory()).setLoadSpec(loadSpec);
        }
    }

    protected Option createReplaceOption() {
        return newOption().
                withName(REPLACE_OPTION).
                withAlias(REPLACE_SHORT_OPTION, OptionFormat.SHORT).
                withDescription(getMessage(REPLACE_OPTION_DESCRIPTION)).build();
    }

    protected InsertType parseReplaceOption(CommandLine commandLine, Option option) {
        return commandLine.hasOption(REPLACE_OPTION) ? InsertType.REPLACE : InsertType.INSERT;
    }
}
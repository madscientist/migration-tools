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
package com.nuodb.tools.migration.cli;

import com.nuodb.tools.migration.cli.parse.Group;
import com.nuodb.tools.migration.cli.parse.Option;
import com.nuodb.tools.migration.cli.parse.OptionException;
import com.nuodb.tools.migration.cli.parse.OptionSet;
import com.nuodb.tools.migration.cli.parse.help.HelpFormatter;
import com.nuodb.tools.migration.cli.parse.option.OptionToolkit;
import com.nuodb.tools.migration.cli.parse.parser.ParserImpl;
import com.nuodb.tools.migration.cli.run.CliRun;
import com.nuodb.tools.migration.cli.run.CliRunFactoryLookup;
import com.nuodb.tools.migration.i18n.Resources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Bushik
 */
public class CliHandler implements CliResources {

    private transient final Log log = LogFactory.getLog(getClass());

    public static final int HELP_OPTION_ID = 1;
    public static final int LIST_OPTION_ID = 2;
    public static final int CONFIG_OPTION_ID = 3;
    public static final int COMMAND_OPTION_ID = 4;

    public static final String HELP_OPTION = "help";
    public static final String LIST_OPTION = "list";
    public static final String CONFIG_OPTION = "config";
    public static final String COMMAND_OPTION = "command";

    public static final String MIGRATION_EXECUTABLE = "migration";

    public void handle(String[] arguments) throws OptionException {
        Option root = createOption(new OptionToolkit());
        try {
            OptionSet options = new ParserImpl().parse(arguments, root);
            handleOptionSet(options, root);
        } catch (OptionException exception) {
            handleOptionException(exception);
        }
    }

    protected Group createOption(OptionToolkit optionToolkit) {
        //OptionToolkit optionToolkit = new OptionToolkit();
        Resources resources = Resources.getResources();
        Option help = optionToolkit.newOption().
                withId(HELP_OPTION_ID).
                withName(HELP_OPTION).
                withDescription(resources.getMessage(HELP_OPTION_DESCRIPTION)).
                withArgument(
                        optionToolkit.newArgument().
                                withName(resources.getMessage(HELP_ARGUMENT_NAME)).build()
                ).build();
        Option list = optionToolkit.newOption().
                withId(LIST_OPTION_ID).
                withName(LIST_OPTION).
                withDescription(resources.getMessage(LIST_OPTION_DESCRIPTION)).build();
        Option config = optionToolkit.newOption().
                withId(CONFIG_OPTION_ID).
                withName(CONFIG_OPTION).
                withDescription(resources.getMessage(CONFIG_OPTION_DESCRIPTION)).
                withArgument(
                        optionToolkit.newArgument().
                                withName(resources.getMessage(CONFIG_ARGUMENT_NAME)).
                                withMinimum(1).
                                withMaximum(1).build()
                ).build();
        Option command = new CliCommand(
                COMMAND_OPTION_ID, COMMAND_OPTION, resources.getMessage(COMMAND_OPTION_DESCRIPTION), false,
                new CliRunFactoryLookup(), optionToolkit);
        return optionToolkit.newGroup().
                withName(resources.getMessage(MIGRATION_GROUP_NAME)).
                withOption(help).
                withOption(list).
                withOption(config).
                withOption(command).
                withRequired(true).build();
    }

    protected void handleOptionSet(OptionSet options, Option root) {
        if (options.hasOption(HELP_OPTION)) {
            if (log.isTraceEnabled()) {
                log.trace("Handling --help");
            }
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOption(root);
            formatter.setExecutable(MIGRATION_EXECUTABLE);
            formatter.format(System.out);
        } else if (options.hasOption(COMMAND_OPTION)) {
            CliRun runnable = options.getValue(COMMAND_OPTION);
            if (log.isTraceEnabled()) {
                log.trace(String.format("Handling %1$s", runnable.getCommand()));
            }
            runnable.run();
        } else if (options.hasOption(CONFIG_OPTION)) {
            if (log.isTraceEnabled()) {
                log.trace(String.format("Handling --config %1$s", options.getValue("config")));
            }
        }
    }

    protected void handleOptionException(OptionException exception) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setException(exception);
        Option option = exception.getOption();
        String executable;
        if (option instanceof CliRun) {
            String command = ((CliRun) option).getCommand();
            executable = MIGRATION_EXECUTABLE + " " + command;
        } else {
            executable = MIGRATION_EXECUTABLE;
        }
        formatter.setExecutable(executable);
        formatter.format(System.out);
    }

    public static void main(String[] args) throws IOException {
        CliHandler handler = new CliHandler();
        handler.handle(loadArguments("arguments.properties"));
    }

    private static String[] loadArguments(String resource) throws IOException {
        InputStream input = CliHandler.class.getResourceAsStream(resource);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        List<String> arguments = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            arguments.add(line.trim());
        }
        return arguments.toArray(new String[arguments.size()]);
    }
}
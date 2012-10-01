/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nuodb.tools.migration.cli.handler.option;

import com.nuodb.tools.migration.cli.handler.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.nuodb.tools.migration.cli.handler.HelpHint.*;

/**
 * An implementation of option of options.
 */
public class SimpleGroup extends BaseOption implements Group {

    public static final String OPTION_SEPARATOR = "|";

    class DescOrder implements Comparator<String> {
        public int compare(String o1, String o2) {
            return -(o1.compareTo(o2));
        }
    }

    private int minimum;
    private int maximum;
    private List<Option> options = new ArrayList<Option>();
    private List<Argument> arguments = new ArrayList<Argument>();
    private SortedMap<String, Option> triggers = new TreeMap<String, Option>(new DescOrder());
    private Set<String> prefixes = new HashSet<String>();

    public SimpleGroup(int id, String name, String description, boolean required,
                       int minimum, int maximum, List<Option> options) {
        super(id, name, description, required);
        this.minimum = minimum;
        this.maximum = maximum;
        addOptions(options);
    }

    @Override
    public int getMinimum() {
        return minimum;
    }

    @Override
    public int getMaximum() {
        return maximum;
    }

    @Override
    public void addOption(Option option) {
        if (option instanceof Argument) {
            arguments.add((Argument) option);
        } else {
            options.add(option);
            for (String trigger : option.getTriggers()) {
                triggers.put(trigger, option);
            }
            prefixes.addAll(option.getPrefixes());
        }
    }

    @Override
    public void addOptions(List<Option> options) {
        for (Option option : options) {
            addOption(option);
        }
    }

    protected boolean isOption(CommandLine commandLine, String argument) {
        if (argument == null) {
            return false;
        }
        // if arg does not require bursting
        if (triggers.containsKey(argument)) {
            return true;
        }
        Map<String, Option> tailMap = triggers.tailMap(argument);
        for (Option option : tailMap.values()) {
            if (option.canProcess(commandLine, argument)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Option findOption(String trigger) {
        for (Option option : this.options) {
            option = option.findOption(trigger);
            if (option != null) {
                return option;
            }
        }
        return null;
    }

    @Override
    public boolean canProcess(CommandLine commandLine, String argument) {
        return isOption(commandLine, argument) || arguments.size() > 0;
    }

    @Override
    public Set<String> getPrefixes() {
        return prefixes;
    }

    @Override
    public Set<String> getTriggers() {
        return triggers.keySet();
    }

    /**
     * Tests whether this option is required. For groups we evaluate the <code>required</code> flag common to all
     * options, but also take the minimum constraints into account.
     *
     * @return a flag whether this option is required
     */
    @Override
    public boolean isRequired() {
        return super.isRequired() && getMinimum() > 0;
    }

    @Override
    public void defaults(CommandLine commandLine) {
        for (Option option : this.options) {
            option.defaults(commandLine);
        }
        for (Argument argument : this.arguments) {
            argument.defaults(commandLine);
        }
    }

    @Override
    @SuppressWarnings("StringEquality")
    public void process(CommandLine commandLine, ListIterator<String> arguments) {
        String previous = null;
        // start process each command line token
        while (arguments.hasNext()) {
            // grab the next argument
            String argument = arguments.next();
            arguments.previous();
            // if we have just tried to process this instance
            if (argument == previous) {
                // rollback and abort
                break;
            }
            if (isOption(commandLine, argument)) {
                processOption(commandLine, arguments, argument);
            } else {
                processArgument(commandLine, arguments, argument);
            }
            // remember last processed instance
            previous = argument;
        }
    }

    protected void processOption(CommandLine commandLine, ListIterator<String> arguments, String argument) {
        for (Option option : this.triggers.tailMap(argument).values()) {
            if (option.canProcess(commandLine, argument)) {
                option.process(commandLine, arguments);
                break;
            }
        }
    }

    protected void processArgument(CommandLine commandLine, ListIterator<String> arguments, String argument) {
        for (Option option : this.arguments) {
            if (option.canProcess(commandLine, argument)) {
                option.process(commandLine, arguments);
                break;
            }
        }
    }

    @Override
    public void postProcess(CommandLine commandLine) {
        // number of options found
        int present = 0;
        // reference to first unexpected option
        Option unexpected = null;
        for (Option option : this.options) {
            // if the child option is present then postProcess it
            if (commandLine.hasOption(option)) {
                if (++present > this.maximum) {
                    unexpected = option;
                    break;
                }
            }
            option.postProcess(commandLine);
        }
        // too many options
        if (unexpected != null) {
            throw new OptionException(this, "Unexpected option " + unexpected.getName());
        }
        // too few options
        if (present < this.minimum) {
            throw new OptionException(this, "Missing option");
        }
        // post process each arguments argument
        for (Argument argument : arguments) {
            argument.postProcess(commandLine);
        }
    }

    @Override
    public void help(StringBuilder buffer, Set<HelpHint> hints, Comparator<Option> comparator) {
        help(buffer, hints, comparator, OPTION_SEPARATOR);
    }

    @Override
    public void help(StringBuilder buffer, Set<HelpHint> hints, Comparator<Option> comparator, String optionSeparator) {
        hints = new HashSet<HelpHint>(hints);
        boolean optional = !isRequired() && (hints.contains(OPTIONAL) || hints.contains(OPTIONAL_CHILD_GROUP));
        boolean expanded = (getName() == null) || hints.contains(GROUP_OPTIONS);
        boolean named = !expanded || ((getName() != null) && hints.contains(GROUP));
        boolean arguments = hints.contains(GROUP_ARGUMENTS);
        boolean outer = hints.contains(GROUP_OUTER);
        hints.remove(GROUP_OUTER);

        boolean both = named && expanded;
        if (optional) {
            buffer.append('[');
        }
        if (named) {
            buffer.append(getName());
        }
        if (both) {
            buffer.append(" (");
        }
        if (expanded) {
            Set<HelpHint> optionHints;
            if (!hints.contains(GROUP_OPTIONS)) {
                optionHints = Collections.emptySet();
            } else {
                optionHints = new HashSet<HelpHint>(hints);
                optionHints.remove(OPTIONAL);
            }
            // grab a list of the option's options.
            List<Option> list;
            if (comparator == null) {
                // default to using the initial order
                list = options;
            } else {
                // sort options if comparator is supplied
                list = new ArrayList<Option>(options);
                Collections.sort(list, comparator);
            }
            // for each option.
            for (Iterator i = list.iterator(); i.hasNext(); ) {
                Option option = (Option) i.next();

                // append help information
                option.help(buffer, optionHints, comparator);

                // add separators as needed
                if (i.hasNext()) {
                    buffer.append(optionSeparator);
                }
            }
        }
        if (both) {
            buffer.append(')');
        }
        if (optional && outer) {
            buffer.append(']');
        }
        if (arguments) {
            for (Argument argument : this.arguments) {
                buffer.append(' ');
                argument.help(buffer, hints, comparator);
            }
        }
        if (optional && !outer) {
            buffer.append(']');
        }
    }

    @Override
    public List<Help> help(int indent, Set<HelpHint> hints, Comparator<Option> comparator) {
        List<Help> help = new ArrayList<Help>();
        if (hints.contains(GROUP)) {
            help.add(new SimpleHelp(this, indent));
        }
        if (hints.contains(GROUP_OPTIONS)) {
            // grab a list of the option's options.
            List<Option> options;
            if (comparator == null) {
                // default to using the initial order
                options = this.options;
            } else {
                // sort options if comparator is supplied
                options = new ArrayList<Option>(this.options);
                Collections.sort(options, comparator);
            }
            // for each option
            for (Option option : options) {
                help.addAll(option.help(indent + 1, hints, comparator));
            }
        }
        if (hints.contains(GROUP_ARGUMENTS)) {
            for (Argument argument : this.arguments) {
                help.addAll(argument.help(indent + 1, hints, comparator));
            }
        }
        return help;
    }
}
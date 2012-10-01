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
package com.nuodb.tools.migration.cli.handler.toolkit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sergey Bushik
 */
public class OptionFormat {

    public static final String OPTION_PREFIX = "--";

    public static final String ARGUMENT_SEPARATOR = "=";

    public static final String ARGUMENT_VALUES_SEPARATOR = ",";

    private final Set<String> optionPrefixes;
    private final String argumentSeparator;
    private final String argumentValuesSeparator;

    public OptionFormat() {
        this(OPTION_PREFIX, ARGUMENT_SEPARATOR, ARGUMENT_VALUES_SEPARATOR);
    }

    public OptionFormat(String optionPrefix) {
        this(optionPrefix, ARGUMENT_SEPARATOR, ARGUMENT_VALUES_SEPARATOR);
    }

    public OptionFormat(String optionPrefix, String argumentSeparator) {
        this(optionPrefix, argumentSeparator, ARGUMENT_VALUES_SEPARATOR);
    }

    public OptionFormat(String optionPrefix, String argumentSeparator, String argumentValuesSeparator) {
        this(new HashSet<String>(Arrays.asList(optionPrefix)), argumentSeparator, argumentValuesSeparator);
    }

    public OptionFormat(Set<String> optionPrefixes, String argumentSeparator, String argumentValuesSeparator) {
        this.optionPrefixes = optionPrefixes;
        this.argumentSeparator = argumentSeparator;
        this.argumentValuesSeparator = argumentValuesSeparator;
    }

    public Set<String> getOptionPrefixes() {
        return optionPrefixes;
    }

    public String getArgumentSeparator() {
        return argumentSeparator;
    }

    public String getArgumentValuesSeparator() {
        return argumentValuesSeparator;
    }
}

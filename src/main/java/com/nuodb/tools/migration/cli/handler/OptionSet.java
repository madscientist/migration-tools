/**
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
package com.nuodb.tools.migration.cli.handler;

import java.util.List;
import java.util.Set;

public interface OptionSet {

    Option getOption(String trigger);

    List<Object> getValues(String trigger);

    List<Object> getValues(String trigger, List<Object> defaultValues);

    List<Object> getValues(Option option);

    List<Object> getValues(Option option, List<Object> defaultValues);

    Object getValue(String trigger);

    Object getValue(String trigger, Object defaultValue);

    Object getValue(Option option);

    Object getValue(Option option, Object defaultValue);

    Boolean getSwitch(String trigger);

    Boolean getSwitch(String trigger, Boolean defaultValue);

    Boolean getSwitch(Option option);

    Boolean getSwitch(Option option, Boolean defaultValue);

    String getProperty(String property);

    String getProperty(Option option, String property);

    String getProperty(Option option, String property, String defaultValue);

    Set<String> getProperties(Option option);

    boolean hasOption(Option trigger);

    boolean hasOption(String trigger);

    Set<String> getProperties();

    List<Option> getOptions();

    Set<String> getTriggers();

    int getOptionCount(String trigger);

    int getOptionCount(Option option);
}

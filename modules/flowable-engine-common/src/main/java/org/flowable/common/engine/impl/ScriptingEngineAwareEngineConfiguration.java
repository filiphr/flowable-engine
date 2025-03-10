/*
 * Copyright 2018 Flowable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.common.engine.impl;

import org.flowable.common.engine.impl.scripting.FlowableScriptEngine;
import org.flowable.common.engine.impl.scripting.ScriptingEngines;

/**
 * Interface that could be implemented by EngineConfiguration to access ScriptingEngines. with fluent setter
 * @author Dennis
 */
public interface ScriptingEngineAwareEngineConfiguration {

    FlowableScriptEngine getScriptEngine();

    AbstractEngineConfiguration setScriptEngine(FlowableScriptEngine scriptEngine);

    ScriptingEngines getScriptingEngines();

    AbstractEngineConfiguration setScriptingEngines(ScriptingEngines scriptingEngines);
}

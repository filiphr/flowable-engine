/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.flowable.cmmn.engine.impl.runtime;

import java.util.HashMap;
import java.util.Map;

import org.flowable.cmmn.api.runtime.PlanItemInstanceTransitionBuilder;
import org.flowable.cmmn.engine.impl.cmd.CompleteStagePlanItemInstanceCmd;
import org.flowable.cmmn.engine.impl.cmd.DisablePlanItemInstanceCmd;
import org.flowable.cmmn.engine.impl.cmd.EnablePlanItemInstanceCmd;
import org.flowable.cmmn.engine.impl.cmd.StartPlanItemInstanceCmd;
import org.flowable.cmmn.engine.impl.cmd.TerminatePlanItemInstanceCmd;
import org.flowable.cmmn.engine.impl.cmd.TriggerPlanItemInstanceCmd;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.api.variable.VariableTrace;
import org.flowable.common.engine.impl.interceptor.CommandExecutor;
import org.flowable.common.engine.impl.variabletrace.VariableTraceHelper;
import org.flowable.form.api.FormInfo;

/**
 * @author Joram Barrez
 */
public class PlanItemInstanceTransitionBuilderImpl implements PlanItemInstanceTransitionBuilder {

    protected CommandExecutor commandExecutor;
    protected VariableTraceHelper variableTraceHelper;

    protected String planItemInstanceId;
    protected VariableTrace variableTrace;
    protected Map<String, Object> variables;
    protected Map<String, Object> formVariables;
    protected String formOutcome;
    protected FormInfo formInfo;
    protected Map<String, Object> localVariables;
    protected Map<String, Object> transientVariables;
    protected Map<String, Object> childTaskVariables;
    protected Map<String, Object> childTaskFormVariables;
    protected String childTaskFormOutcome;
    protected FormInfo childTaskFormInfo;

    public PlanItemInstanceTransitionBuilderImpl(CommandExecutor commandExecutor, String planItemInstanceId) {
        this.commandExecutor = commandExecutor;
        this.planItemInstanceId = planItemInstanceId;
    }

    public PlanItemInstanceTransitionBuilderImpl(CommandExecutor commandExecutor, String planItemInstanceId, VariableTraceHelper variableTraceHelper) {
        this.commandExecutor = commandExecutor;
        this.planItemInstanceId = planItemInstanceId;
        this.variableTraceHelper = variableTraceHelper;
    }

    @Override
    public PlanItemInstanceTransitionBuilder variableTrace(VariableTrace variableTrace) {
        this.variableTrace = variableTrace;
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder variable(String variableName, Object variableValue) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(variableName, variableValue);
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder variables(Map<String, Object> variables) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        if (variables != null) {
            this.variables.putAll(variables);
        }
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder formVariables(Map<String, Object> variables, FormInfo formInfo, String outcome) {
        if (formInfo == null) {
            throw new FlowableIllegalArgumentException("formInfo is null");
        }

        if (this.formVariables == null) {
            this.formVariables = new HashMap<>();
        }

        if (variables != null) {
            this.formVariables.putAll(variables);
        }

        this.formOutcome = outcome;
        this.formInfo = formInfo;
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder localVariable(String variableName, Object variableValue) {
        if (localVariables == null) {
            localVariables = new HashMap<>();
        }
        localVariables.put(variableName, variableValue);
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder localVariables(Map<String, Object> localVariables) {
        if (this.localVariables == null) {
            this.localVariables = new HashMap<>();
        }
        if (localVariables != null) {
            this.localVariables.putAll(localVariables);
        }
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder transientVariable(String variableName, Object variableValue) {
        if (transientVariables == null) {
            transientVariables = new HashMap<>();
        }
        transientVariables.put(variableName, variableValue);
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder transientVariables(Map<String, Object> transientVariables) {
        if (this.transientVariables == null) {
            this.transientVariables = new HashMap<>();
        }
        if (transientVariables != null) {
            this.transientVariables.putAll(transientVariables);
        }
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder childTaskVariable(String variableName, Object variableValue) {
        if (childTaskVariables == null) {
            childTaskVariables = new HashMap<>();
        }
        childTaskVariables.put(variableName, variableValue);
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder childTaskVariables(Map<String, Object> childTaskVariables) {
        if (this.childTaskVariables == null) {
            this.childTaskVariables = new HashMap<>();
        }
        if (childTaskVariables != null) {
            this.childTaskVariables.putAll(childTaskVariables);
        }
        return this;
    }

    @Override
    public PlanItemInstanceTransitionBuilder childTaskFormVariables(Map<String, Object> variables, FormInfo formInfo, String outcome) {
        if (formInfo == null) {
            throw new FlowableIllegalArgumentException("formInfo is null");
        }

        if (this.childTaskFormVariables == null) {
            this.childTaskFormVariables = new HashMap<>();
        }

        if (variables != null) {
            this.childTaskFormVariables.putAll(variables);
        }

        this.childTaskFormOutcome = outcome;
        this.childTaskFormInfo = formInfo;
        return this;
    }

    @Override
    public void trigger() {
        validateChildTaskVariablesNotSet();
        withTrace(() -> commandExecutor.execute(new TriggerPlanItemInstanceCmd(planItemInstanceId, variables, formVariables, formOutcome,
                formInfo, localVariables, transientVariables)));
    }

    @Override
    public void enable() {
        validateChildTaskVariablesNotSet();
        withTrace(() -> commandExecutor.execute(new EnablePlanItemInstanceCmd(planItemInstanceId, variables, formVariables, formOutcome,
                formInfo, localVariables, transientVariables)));
    }

    @Override
    public void disable() {
        validateChildTaskVariablesNotSet();
        withTrace(() -> commandExecutor.execute(new DisablePlanItemInstanceCmd(planItemInstanceId, variables, formVariables, formOutcome,
                formInfo, localVariables, transientVariables)));
    }

    @Override
    public void start() {
        withTrace(() -> commandExecutor.execute(new StartPlanItemInstanceCmd(planItemInstanceId, variables, formVariables, formOutcome,
                formInfo, localVariables, transientVariables, childTaskVariables, childTaskFormVariables,
                childTaskFormOutcome, childTaskFormInfo)));
    }

    @Override
    public void terminate() {
        validateChildTaskVariablesNotSet();
        withTrace(() -> commandExecutor.execute(new TerminatePlanItemInstanceCmd(planItemInstanceId, variables, formVariables, formOutcome,
                formInfo, localVariables, transientVariables)));
    }

    @Override
    public void completeStage() {
        validateChildTaskVariablesNotSet();
        withTrace(() -> commandExecutor.execute(new CompleteStagePlanItemInstanceCmd(planItemInstanceId, variables, formVariables, formOutcome,
                formInfo, localVariables, transientVariables, false)));
    }

    @Override
    public void forceCompleteStage() {
        validateChildTaskVariablesNotSet();
        withTrace(() -> commandExecutor.execute(new CompleteStagePlanItemInstanceCmd(planItemInstanceId, variables, formVariables, formOutcome,
                formInfo, localVariables, transientVariables, true)));
    }

    protected void withTrace(Runnable runnable) {
        if (this.variableTrace != null) {
            VariableTraceHelper.runWithCallerTrace(this.variableTrace, runnable);
        } else if (variableTraceHelper != null) {
            variableTraceHelper.runWithAutoTrace(runnable);
        } else {
            runnable.run();
        }
    }

    protected void validateChildTaskVariablesNotSet() {
        if (childTaskVariables != null) {
            throw new FlowableIllegalArgumentException("Child task variables can only be set when starting a plan item instance");
        }

        if (childTaskFormInfo != null) {
            throw new FlowableIllegalArgumentException("Child form variables can only be set when starting a plan item instance");
        }
    }
}

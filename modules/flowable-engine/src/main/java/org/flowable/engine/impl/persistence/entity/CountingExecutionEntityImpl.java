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
package org.flowable.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.flowable.engine.impl.persistence.CountingExecutionEntity;

/**
 * @author Filip Hrisafov
 */
public class CountingExecutionEntityImpl extends AbstractEntityNoRevision implements CountingExecutionEntity {

    protected final ExecutionEntity executionEntity;

    protected int eventSubscriptionCount = 0;
    protected int eventSubscriptionDeltaCount = 0;

    protected int taskCount = 0;
    protected int taskDeltaCount = 0;

    protected int jobCount = 0;
    protected int jobDeltaCount = 0;

    protected int timerJobCount = 0;
    protected int timerJobDeltaCount = 0;

    protected int suspendedJobCount = 0;
    protected int suspendedJobDeltaCount = 0;

    protected int deadLetterJobCount = 0;
    protected int deadLetterJobDeltaCount = 0;

    protected int externalWorkerJobCount = 0;
    protected int externalWorkerJobDeltaCount = 0;

    protected int variableCount = 0;
    protected int variableDeltaCount = 0;

    protected int identityLinkCount = 0;
    protected int identityLinkDeltaCount = 0;

    protected Object originalPersistentState;

    public CountingExecutionEntityImpl(ExecutionEntity executionEntity) {
        this.executionEntity = executionEntity;
    }

    @Override
    public String getId() {
        return executionEntity.getId();
    }

    @Override
    public String getIdPrefix() {
        return "";
    }

    @Override
    public Object getPersistentState() {
        Map<String, Object> persistentState = new HashMap<>();
        persistentState.put("eventSubscriptionDeltaCount", eventSubscriptionDeltaCount);
        persistentState.put("taskDeltaCount", taskDeltaCount);
        persistentState.put("jobDeltaCount", jobDeltaCount);
        persistentState.put("timerJobDeltaCount", timerJobDeltaCount);
        persistentState.put("suspendedJobDeltaCount", suspendedJobDeltaCount);
        persistentState.put("deadLetterJobDeltaCount", deadLetterJobDeltaCount);
        persistentState.put("externalWorkerJobDeltaCount", externalWorkerJobDeltaCount);
        persistentState.put("variableDeltaCount", variableDeltaCount);
        persistentState.put("identityLinkDeltaCount", identityLinkDeltaCount);

        return persistentState;
    }

    @Override
    public boolean isProcessInstanceType() {
        return executionEntity.isProcessInstanceType();
    }

    @Override
    public Object getOriginalPersistentState() {
        return originalPersistentState;
    }

    @Override
    public void setOriginalPersistentState(Object persistentState) {
        this.originalPersistentState = persistentState;
    }

    @Override
    public boolean isCountEnabled() {
        return executionEntity.isCountEnabled();
    }

    @Override
    public int getEventSubscriptionCount() {
        return eventSubscriptionCount + eventSubscriptionDeltaCount;
    }

    public void setEventSubscriptionCount(int eventSubscriptionCount) {
        this.eventSubscriptionCount = eventSubscriptionCount;
    }

    public int getEventSubscriptionDeltaCount() {
        return eventSubscriptionDeltaCount;
    }

    @Override
    public void incrementEventSubscriptionCount() {
        eventSubscriptionDeltaCount++;
    }

    @Override
    public void decrementEventSubscriptionCount() {
        eventSubscriptionDeltaCount--;
    }

    @Override
    public int getTaskCount() {
        return taskCount + taskDeltaCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getTaskDeltaCount() {
        return taskDeltaCount;
    }

    @Override
    public void incrementTaskCount() {
        taskDeltaCount++;
    }

    @Override
    public void decrementTaskCount() {
        taskDeltaCount--;
    }

    @Override
    public int getJobCount() {
        return jobCount + jobDeltaCount;
    }

    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }

    public int getJobDeltaCount() {
        return jobDeltaCount;
    }

    @Override
    public void incrementJobCount() {
        jobDeltaCount++;
    }

    @Override
    public void decrementJobCount() {
        jobDeltaCount--;
    }

    @Override
    public int getTimerJobCount() {
        return timerJobCount + timerJobDeltaCount;
    }

    public void setTimerJobCount(int timerJobCount) {
        this.timerJobCount = timerJobCount;
    }

    public int getTimerJobDeltaCount() {
        return timerJobDeltaCount;
    }

    @Override
    public void incrementTimerJobCount() {
        timerJobDeltaCount++;
    }

    @Override
    public void decrementTimerJobCount() {
        timerJobDeltaCount++;
    }

    @Override
    public int getSuspendedJobCount() {
        return suspendedJobCount + suspendedJobDeltaCount;
    }

    public void setSuspendedJobCount(int suspendedJobCount) {
        this.suspendedJobCount = suspendedJobCount;
    }

    public int getSuspendedJobDeltaCount() {
        return suspendedJobDeltaCount;
    }

    @Override
    public void incrementSuspendedJobCount() {
        suspendedJobDeltaCount++;
    }

    @Override
    public void decrementSuspendedJobCount() {
        suspendedJobDeltaCount--;
    }

    @Override
    public int getDeadLetterJobCount() {
        return deadLetterJobCount + deadLetterJobDeltaCount;
    }

    public void setDeadLetterJobCount(int deadLetterJobCount) {
        this.deadLetterJobCount = deadLetterJobCount;
    }

    public int getDeadLetterJobDeltaCount() {
        return deadLetterJobDeltaCount;
    }

    @Override
    public void incrementDeadLetterJobCount() {
        deadLetterJobDeltaCount++;
    }

    @Override
    public void decrementDeadLetterJobCount() {
        deadLetterJobDeltaCount--;
    }

    @Override
    public int getExternalWorkerJobCount() {
        return externalWorkerJobCount + externalWorkerJobDeltaCount;
    }

    public void setExternalWorkerJobCount(int externalWorkerJobCount) {
        this.externalWorkerJobCount = externalWorkerJobCount;
    }

    public int getExternalWorkerJobDeltaCount() {
        return externalWorkerJobDeltaCount;
    }

    @Override
    public void incrementExternalWorkerJobCount() {
        externalWorkerJobDeltaCount++;
    }

    @Override
    public void decrementExternalWorkerJobCount() {
        externalWorkerJobDeltaCount--;
    }

    @Override
    public int getVariableCount() {
        return variableCount + variableDeltaCount;
    }

    public void setVariableCount(int variableCount) {
        this.variableCount = variableCount;
    }

    public int getVariableDeltaCount() {
        return variableDeltaCount;
    }

    @Override
    public void incrementVariableCount() {
        variableDeltaCount++;
    }

    @Override
    public void decrementVariableCount() {
        variableDeltaCount--;
    }

    @Override
    public int getIdentityLinkCount() {
        return identityLinkCount + identityLinkDeltaCount;
    }

    public void setIdentityLinkCount(int identityLinkCount) {
        this.identityLinkCount = identityLinkCount;
    }

    public int getIdentityLinkDeltaCount() {
        return identityLinkDeltaCount;
    }

    @Override
    public void incrementIdentityLinkCount() {
        identityLinkDeltaCount++;
    }

    @Override
    public void decrementIdentityLinkCount() {
        identityLinkDeltaCount--;
    }
}

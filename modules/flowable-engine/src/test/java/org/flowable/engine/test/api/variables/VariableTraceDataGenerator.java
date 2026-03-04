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
package org.flowable.engine.test.api.variables;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.flowable.common.engine.impl.db.DbSqlSession;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.task.api.Task;

/**
 * Standalone data generator that connects to a PostgreSQL database, deploys varied
 * BPMN process models, and starts process instances to populate the {@code ACT_HI_VAR_TRACE}
 * table with diverse variable trace data.
 * <p>
 * Usage:
 * <pre>
 * ./mvnw exec:java -pl modules/flowable-engine \
 *   -Ppostgresql \
 *   -Dexec.mainClass="org.flowable.engine.test.api.variables.VariableTraceDataGenerator" \
 *   -Dexec.classpathScope=test \
 *   -Djdbc.url="jdbc:postgresql://localhost:5432/flowable-trace" \
 *   -Djdbc.username="flowable" \
 *   -Djdbc.password="flowable"
 * </pre>
 *
 * @author Filip Hrisafov
 */
public class VariableTraceDataGenerator {

    private static final String[] CUSTOMER_NAMES = {
            "Alice Johnson", "Bob Smith", "Carol Davis", "David Wilson",
            "Emma Brown", "Frank Miller", "Grace Lee", "Henry Taylor",
            "Iris Martinez", "Jack Anderson"
    };

    private static final String[][] ITEM_LISTS = {
            { "laptop", "mouse", "keyboard" },
            { "monitor", "webcam", "headset", "dock" },
            { "phone", "case", "charger" },
            { "tablet", "stylus", "cover", "screen-protector", "keyboard-case" },
            { "printer", "ink", "paper" }
    };

    public static void main(String[] args) {
        String jdbcUrl = System.getProperty("jdbc.url", "jdbc:postgresql://localhost:5432/flowable-trace");
        String jdbcDriver = System.getProperty("jdbc.driver", "org.postgresql.Driver");
        String jdbcUsername = System.getProperty("jdbc.username", "flowable");
        String jdbcPassword = System.getProperty("jdbc.password", "flowable");

        System.out.println("=== Variable Trace Data Generator ===");
        System.out.println("Connecting to: " + jdbcUrl);

        ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) new StandaloneProcessEngineConfiguration()
                .setJdbcDriver(jdbcDriver)
                .setJdbcUrl(jdbcUrl)
                .setJdbcUsername(jdbcUsername)
                .setJdbcPassword(jdbcPassword)
                .setDatabaseSchemaUpdate("true")
                .setHistory("full")
                .setAsyncExecutorActivate(false);

        config.setAsyncHistoryEnabled(false);
        config.setVariableTracePersistenceEnabled(true);
        config.setEnableEntityLinks(true);

        ProcessEngine processEngine = config.buildProcessEngine();

        try {
            RepositoryService repositoryService = processEngine.getRepositoryService();
            RuntimeService runtimeService = processEngine.getRuntimeService();
            TaskService taskService = processEngine.getTaskService();

            // Deploy process models
            System.out.println("\nDeploying process models...");
            String basePath = "org/flowable/engine/test/api/variables/datagenerator/";
            repositoryService.createDeployment()
                    .addClasspathResource(basePath + "order-processing.bpmn20.xml")
                    .addClasspathResource(basePath + "batch-processing.bpmn20.xml")
                    .addClasspathResource(basePath + "customer-onboarding.bpmn20.xml")
                    .addClasspathResource(basePath + "variable-lifecycle.bpmn20.xml")
                    .addClasspathResource(basePath + "loan-application.bpmn20.xml")
                    .addClasspathResource(basePath + "credit-check.bpmn20.xml")
                    .name("Variable Trace Data Generator")
                    .deploy();
            System.out.println("  Deployed 6 process models.");

            Random random = new Random(42); // fixed seed for reproducible data
            int totalInstances = 0;

            // --- Order Processing (20 instances) ---
            System.out.println("\nGenerating Order Processing instances...");
            for (int i = 0; i < 20; i++) {
                int orderAmount = 100 + random.nextInt(4900); // 100-5000
                String customerName = CUSTOMER_NAMES[random.nextInt(CUSTOMER_NAMES.length)];
                String processInstanceId = runtimeService.createProcessInstanceBuilder()
                        .processDefinitionKey("orderProcessing")
                        .variable("orderAmount", orderAmount)
                        .variable("customerName", customerName)
                        .variable("orderId", "ORD-" + UUID.randomUUID().toString().substring(0, 8))
                        .start()
                        .getId();

                // High-value orders have a user task to complete
                if (orderAmount > 2000) {
                    Task task = taskService.createTaskQuery()
                            .processInstanceId(processInstanceId)
                            .singleResult();
                    if (task != null) {
                        taskService.createTaskCompletionBuilder()
                                .taskId(task.getId())
                                .variable("approvalStatus", random.nextBoolean() ? "approved" : "rejected")
                                .variable("approverComment", "Reviewed by manager")
                                .complete();
                    }
                }
                totalInstances++;
            }
            System.out.println("  Created 20 order processing instances.");

            // --- Batch Processing (5 instances) ---
            System.out.println("\nGenerating Batch Processing instances...");
            for (int i = 0; i < 5; i++) {
                List<String> items = Arrays.asList(ITEM_LISTS[i]);
                runtimeService.createProcessInstanceBuilder()
                        .processDefinitionKey("batchProcessing")
                        .variable("itemList", items)
                        .variable("batchId", "BATCH-" + (i + 1))
                        .start();
                totalInstances++;
            }
            System.out.println("  Created 5 batch processing instances.");

            // --- Customer Onboarding (10 instances) ---
            System.out.println("\nGenerating Customer Onboarding instances...");
            for (int i = 0; i < 10; i++) {
                String customerName = CUSTOMER_NAMES[i];
                String processInstanceId = runtimeService.createProcessInstanceBuilder()
                        .processDefinitionKey("customerOnboarding")
                        .variable("customerName", customerName)
                        .variable("customerId", "CUST-" + (1000 + i))
                        .variable("customerEmail", customerName.toLowerCase().replace(" ", ".") + "@example.com")
                        .start()
                        .getId();

                // Complete the KYC review task
                Task task = taskService.createTaskQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult();
                if (task != null) {
                    String reviewResult = (i % 3 == 0) ? "rejected" : "approved";
                    taskService.createTaskCompletionBuilder()
                            .taskId(task.getId())
                            .variable("reviewResult", reviewResult)
                            .variable("reviewNotes", "KYC review for " + customerName)
                            .complete();
                }
                totalInstances++;
            }
            System.out.println("  Created 10 customer onboarding instances.");

            // --- Variable Lifecycle (5 instances) ---
            System.out.println("\nGenerating Variable Lifecycle instances...");
            for (int i = 0; i < 5; i++) {
                runtimeService.createProcessInstanceBuilder()
                        .processDefinitionKey("variableLifecycle")
                        .variable("inputData", "data-" + i)
                        .variable("iteration", i)
                        .start();
                totalInstances++;
            }
            System.out.println("  Created 5 variable lifecycle instances.");

            // --- Loan Application with Call Activity (10 instances) ---
            System.out.println("\nGenerating Loan Application instances (with call activity)...");
            for (int i = 0; i < 10; i++) {
                String customerName = CUSTOMER_NAMES[i];
                int income = 30000 + random.nextInt(120000);
                int loanAmount = 5000 + random.nextInt(45000);
                String processInstanceId = runtimeService.createProcessInstanceBuilder()
                        .processDefinitionKey("loanApplication")
                        .variable("applicantName", customerName)
                        .variable("applicantIncome", income)
                        .variable("loanAmount", loanAmount)
                        .variable("applicationId", "LOAN-" + UUID.randomUUID().toString().substring(0, 8))
                        .start()
                        .getId();

                // Some loans need manual review — complete the user task
                Task task = taskService.createTaskQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult();
                if (task != null) {
                    taskService.createTaskCompletionBuilder()
                            .taskId(task.getId())
                            .variable("reviewerDecision", random.nextBoolean() ? "approved" : "rejected")
                            .variable("reviewerNotes", "Manual review for " + customerName)
                            .complete();
                }
                totalInstances++;
            }
            System.out.println("  Created 10 loan application instances.");

            // Print summary
            long traceEntryCount = config.getCommandExecutor().execute(commandContext -> {
                DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
                try (var rs = dbSqlSession.getSqlSession().getConnection()
                        .prepareStatement("SELECT count(*) FROM ACT_HI_VAR_TRACE").executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                } catch (java.sql.SQLException e) {
                    throw new RuntimeException("Failed to count trace entries", e);
                }
            });

            System.out.println("\n=== Summary ===");
            System.out.println("  Process instances created: " + totalInstances);
            System.out.println("  Variable trace entries:    " + traceEntryCount);
            System.out.println("===============");

        } finally {
            processEngine.close();
        }
    }

    // --- JavaDelegate inner classes ---

    /**
     * Reads orderAmount and customerName, creates orderValid and orderCategory.
     */
    public static class ValidateOrderDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            int orderAmount = (int) execution.getVariable("orderAmount");
            String customerName = (String) execution.getVariable("customerName");
            execution.setVariable("orderValid", true);
            execution.setVariable("orderCategory", orderAmount > 2000 ? "high" : "normal");
            execution.setVariable("validationMessage", "Order validated for " + customerName);
        }
    }

    /**
     * Reads orderAmount, applies discount, creates discountPercent and updates orderAmount.
     */
    public static class ApplyDiscountDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            int orderAmount = (int) execution.getVariable("orderAmount");
            int discountPercent = 10;
            int discountedAmount = orderAmount - (orderAmount * discountPercent / 100);
            execution.setVariable("discountPercent", discountPercent);
            execution.setVariable("originalAmount", orderAmount);
            execution.setVariable("orderAmount", discountedAmount);
        }
    }

    /**
     * Reads orderAmount and orderCategory, creates paymentRef.
     */
    public static class ProcessPaymentDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            int orderAmount = (int) execution.getVariable("orderAmount");
            String orderCategory = (String) execution.getVariable("orderCategory");
            execution.setVariable("paymentRef", "PAY-" + UUID.randomUUID().toString().substring(0, 8));
            execution.setVariable("paymentAmount", orderAmount);
            execution.setVariable("paymentStatus", "completed");
        }
    }

    /**
     * Multi-instance delegate: reads the loop element variable 'item', creates itemResult.
     */
    public static class ProcessItemDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String item = (String) execution.getVariable("item");
            execution.setVariable("lastProcessedItem", item);
            execution.setVariable("itemResult_" + item, "processed:" + item.toUpperCase());
        }
    }

    /**
     * Reads loop counter, creates batchSummary.
     */
    public static class AggregateResultsDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String batchId = (String) execution.getVariable("batchId");
            String lastItem = (String) execution.getVariable("lastProcessedItem");
            execution.setVariable("batchSummary", "Batch " + batchId + " complete, last item: " + lastItem);
            execution.setVariable("batchStatus", "completed");
        }
    }

    /**
     * KYC check: reads customerName and customerId, creates kycScore and kycStatus.
     */
    public static class KycCheckDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String customerName = (String) execution.getVariable("customerName");
            String customerId = (String) execution.getVariable("customerId");
            // Simulate KYC scoring
            int score = customerName.length() * 7 + customerId.hashCode() % 100;
            score = Math.abs(score % 100);
            execution.setVariable("kycScore", score);
            execution.setVariable("kycStatus", score > 30 ? "pass" : "review");
            execution.setVariable("kycDetails", "Checked identity for " + customerName + " (" + customerId + ")");
        }
    }

    /**
     * Creates account based on KYC results.
     */
    public static class CreateAccountDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String customerName = (String) execution.getVariable("customerName");
            String kycStatus = (String) execution.getVariable("kycStatus");
            String reviewResult = (String) execution.getVariable("reviewResult");
            execution.setVariable("accountId", "ACC-" + UUID.randomUUID().toString().substring(0, 8));
            execution.setVariable("accountStatus", "approved".equals(reviewResult) ? "active" : "suspended");
            execution.setVariable("accountHolder", customerName);
        }
    }

    /**
     * Creates multiple variables to exercise the CREATE operation.
     */
    public static class VariableLifecycleDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String inputData = (String) execution.getVariable("inputData");
            int iteration = (int) execution.getVariable("iteration");
            execution.setVariable("tempVar1", "temp-" + inputData);
            execution.setVariable("tempVar2", "ephemeral-" + iteration);
            execution.setVariable("counter", 1);
            execution.setVariable("status", "initialized");
        }
    }

    /**
     * Reads, updates, and deletes variables to exercise UPDATE and DELETE operations.
     */
    public static class UpdateAndDeleteDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            // READ + UPDATE
            String tempVar1 = (String) execution.getVariable("tempVar1");
            int counter = (int) execution.getVariable("counter");
            execution.setVariable("counter", counter + 1);
            execution.setVariable("status", "finalized");

            // DELETE
            execution.removeVariable("tempVar1");
            execution.removeVariable("tempVar2");

            // CREATE a summary
            execution.setVariable("summary", "Processed " + tempVar1 + " with counter " + (counter + 1));
        }
    }

    // --- Loan Application / Credit Check delegates ---

    /**
     * Prepares loan data: reads applicant info, computes debt-to-income ratio.
     */
    public static class PrepareLoanDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String applicantName = (String) execution.getVariable("applicantName");
            int income = (int) execution.getVariable("applicantIncome");
            int loanAmount = (int) execution.getVariable("loanAmount");
            double dtiRatio = (double) loanAmount / income;
            execution.setVariable("dtiRatio", Math.round(dtiRatio * 100.0) / 100.0);
            execution.setVariable("loanStatus", "pending");
        }
    }

    /**
     * Child process delegate: calculates credit score from applicant name and income.
     */
    public static class CalculateCreditScoreDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String applicantName = (String) execution.getVariable("applicantName");
            int income = (int) execution.getVariable("annualIncome");
            int requestedAmount = (int) execution.getVariable("requestedAmount");
            // Simulate credit score calculation
            int baseScore = 300 + (income / 500);
            int nameHash = Math.abs(applicantName.hashCode() % 200);
            int score = Math.min(850, baseScore + nameHash - (requestedAmount / 1000));
            score = Math.max(300, score);
            execution.setVariable("creditScore", score);
            execution.setVariable("scoreDetails", "Base: " + baseScore + ", adjustment: " + nameHash);
        }
    }

    /**
     * Child process delegate: assesses risk category based on credit score.
     */
    public static class AssessRiskDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            int creditScore = (int) execution.getVariable("creditScore");
            String risk;
            if (creditScore >= 720) {
                risk = "low";
            } else if (creditScore >= 620) {
                risk = "medium";
            } else {
                risk = "high";
            }
            execution.setVariable("creditRisk", risk);
            execution.setVariable("riskDetails", "Score " + creditScore + " -> " + risk + " risk");
        }
    }

    /**
     * Parent process delegate: makes loan decision based on credit score and risk from child process.
     */
    public static class LoanDecisionDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            int creditScore = (int) execution.getVariable("creditScore");
            String riskCategory = (String) execution.getVariable("riskCategory");
            int loanAmount = (int) execution.getVariable("loanAmount");

            String decision;
            if ("low".equals(riskCategory) && loanAmount < 30000) {
                decision = "approved";
            } else if ("high".equals(riskCategory)) {
                decision = "rejected";
            } else {
                decision = "review";
            }
            execution.setVariable("loanDecision", decision);
            execution.setVariable("decisionReason",
                    "Score: " + creditScore + ", Risk: " + riskCategory + ", Amount: " + loanAmount);
        }
    }
}

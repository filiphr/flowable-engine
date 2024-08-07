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
package org.flowable.common.engine.impl.db;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableWrongDbException;
import org.flowable.common.engine.impl.FlowableVersions;

/**
 * @author Joram Barrez
 */
public abstract class ServiceSqlScriptBasedDbSchemaManager extends AbstractSqlScriptBasedDbSchemaManager {
    
    protected String table;
    protected String schemaComponent;
    protected String schemaVersionProperty;
    
    public ServiceSqlScriptBasedDbSchemaManager(String table, String schemaComponent, String schemaVersionProperty) {
        this.table = table;
        this.schemaComponent = schemaComponent;
        this.schemaVersionProperty = schemaVersionProperty;
    }
    
    @Override
    public void schemaCreate() {
        if (isUpdateNeeded()) {
            String dbVersion = getSchemaVersion();
            if (!FlowableVersions.CURRENT_VERSION.equals(dbVersion)) {
                throw new FlowableWrongDbException(FlowableVersions.CURRENT_VERSION, dbVersion);
            }
        } else {
            internalDbSchemaCreate();
        }
    }

    protected void internalDbSchemaCreate() {
        executeMandatorySchemaResource("create", schemaComponent);
    }

    @Override
    public void schemaDrop() {
        try {
            executeMandatorySchemaResource("drop", schemaComponent);
        } catch (Exception e) {
            logger.info("Error dropping {} tables", schemaComponent, e);
        }
    }

    @Override
    public String schemaUpdate() {
        return schemaUpdate(null);
    }
    
    @Override
    public String schemaUpdate(String engineDbVersion) {
        String feedback = null;
        if (isUpdateNeeded()) {
            String dbVersion = getSchemaVersion();
            String compareWithVersion = null;
            if (dbVersion == null) {
                compareWithVersion = "6.1.2.0"; // last version before services were separated. Start upgrading from this point.
            } else {
                compareWithVersion = dbVersion;
            }
            
            int matchingVersionIndex = FlowableVersions.getFlowableVersionIndexForDbVersion(compareWithVersion);
            boolean isUpgradeNeeded = (matchingVersionIndex != (FlowableVersions.FLOWABLE_VERSIONS.size() - 1));
            if (isUpgradeNeeded) {
                dbSchemaUpgrade(schemaComponent, matchingVersionIndex, engineDbVersion);
            }
            
            feedback = "upgraded from " + compareWithVersion + " to " + FlowableVersions.CURRENT_VERSION;
        } else {
            schemaCreate();
        }
        return feedback;
    }
    
    @Override
    public void schemaCheckVersion() {
        String dbVersion = getSchemaVersion();
        if (!FlowableVersions.CURRENT_VERSION.equals(dbVersion)) {
            throw new FlowableWrongDbException(FlowableVersions.CURRENT_VERSION, dbVersion);
        }
    }

    protected boolean isUpdateNeeded() {
        return isTablePresent(table);
    }

    protected String getSchemaVersion() {
        if (schemaVersionProperty == null) {
            throw new FlowableException("Schema version property is not set");
        }
        String dbVersion = getProperty(schemaVersionProperty);
        if (dbVersion == null) {
            return getUpgradeStartVersion();
        }
        return dbVersion;
    }
    
    protected String getUpgradeStartVersion() {
        return "6.1.2.0"; // last version before most services were separated. Start upgrading from this point.
    }

}

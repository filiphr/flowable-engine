CREATE TABLE ACT_APP_DEPLOYMENT (ID_ varchar(255) NOT NULL, NAME_ varchar(255), CATEGORY_ varchar(255), KEY_ varchar(255), DEPLOY_TIME_ datetime, TENANT_ID_ varchar(255) CONSTRAINT DF_ACT_APP_DEPLOYMENT_TENANT_ID_ DEFAULT '', CONSTRAINT PK_ACT_APP_DEPLOYMENT PRIMARY KEY (ID_));

CREATE TABLE ACT_APP_DEPLOYMENT_RESOURCE (ID_ varchar(255) NOT NULL, NAME_ varchar(255), DEPLOYMENT_ID_ varchar(255), RESOURCE_BYTES_ varbinary(MAX), CONSTRAINT PK_APP_DEPLOYMENT_RESOURCE PRIMARY KEY (ID_));

ALTER TABLE ACT_APP_DEPLOYMENT_RESOURCE ADD CONSTRAINT ACT_FK_APP_RSRC_DPL FOREIGN KEY (DEPLOYMENT_ID_) REFERENCES ACT_APP_DEPLOYMENT (ID_);

CREATE NONCLUSTERED INDEX ACT_IDX_APP_RSRC_DPL ON ACT_APP_DEPLOYMENT_RESOURCE(DEPLOYMENT_ID_);

CREATE TABLE ACT_APP_APPDEF (ID_ varchar(255) NOT NULL, REV_ int NOT NULL, NAME_ varchar(255), KEY_ varchar(255) NOT NULL, VERSION_ int NOT NULL, CATEGORY_ varchar(255), DEPLOYMENT_ID_ varchar(255), RESOURCE_NAME_ varchar(4000), DESCRIPTION_ varchar(4000), TENANT_ID_ varchar(255) CONSTRAINT DF_ACT_APP_APPDEF_TENANT_ID_ DEFAULT '', CONSTRAINT PK_ACT_APP_APPDEF PRIMARY KEY (ID_));

ALTER TABLE ACT_APP_APPDEF ADD CONSTRAINT ACT_FK_APP_DEF_DPLY FOREIGN KEY (DEPLOYMENT_ID_) REFERENCES ACT_APP_DEPLOYMENT (ID_);

CREATE NONCLUSTERED INDEX ACT_IDX_APP_DEF_DPLY ON ACT_APP_APPDEF(DEPLOYMENT_ID_);

CREATE UNIQUE NONCLUSTERED INDEX ACT_IDX_APP_DEF_UNIQ ON ACT_APP_APPDEF(KEY_, VERSION_, TENANT_ID_);

insert into ACT_GE_PROPERTY
values ('app.schema.version', '7.2.0.2', 1);

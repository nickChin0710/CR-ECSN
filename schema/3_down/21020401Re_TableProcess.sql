connect to cr;

ALTER TABLE ECSCRDB.DBB_OTHEXP  ALTER COLUMN ACCT_NO SET DATA TYPE VARCHAR(11);
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.DBB_OTHEXP');

terminate;
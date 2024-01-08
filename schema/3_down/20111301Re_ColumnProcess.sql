connect to cr;

ALTER TABLE ECSCRDB.DBC_IDNO  DROP COLUMN MARKET_AGREE_BASE;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.DBC_IDNO');

ALTER TABLE ECSCRDB.ACT_REPAY_CREDITLIMIT  DROP COLUMN REMARK;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.ACT_REPAY_CREDITLIMIT');

ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT  DROP COLUMN VIP_KIND;
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT  DROP COLUMN IN_PERSON_COUNT;
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT  DROP COLUMN IN_FILE_TYPE;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CMS_PPCARD_VISIT');
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT  DROP COLUMN PYMT_COND;
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT  DROP COLUMN PYMT_FAIL_COUNT;
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT  DROP COLUMN PYMT_FAIL_PERSON_COUNT;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CMS_PPCARD_VISIT');
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT  DROP COLUMN PYMT_FAIL_TOT_COUNT;
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT  DROP COLUMN ERR_CODE;
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT  DROP COLUMN ERR_DESC;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CMS_PPCARD_VISIT');

ALTER TABLE ECSCRDB.CCA_OPPOSITION  DROP COLUMN FISC_REASON_CODE;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_OPPOSITION');

ALTER TABLE ECSCRDB.CCA_COUNTRY  DROP COLUMN COUNTRY_NO;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_COUNTRY');

ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  DROP COLUMN OPEN_CHK;
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  DROP COLUMN MCHT_CHK;
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  DROP COLUMN OVERSEA_CHK;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_DEBIT_PARM');
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  DROP COLUMN AVG_CONSUME_CHK;
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  DROP COLUMN MONTH_RISK_CHK;
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  DROP COLUMN DAY_RISK_CHK;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_DEBIT_PARM');

ALTER TABLE ECSCRDB.DBC_IDNO ALTER COLUMN INST_FLAG        SET DEFAULT '';
ALTER TABLE ECSCRDB.DBC_IDNO ALTER COLUMN FEE_CODE_I       SET DEFAULT '';
ALTER TABLE ECSCRDB.DBC_IDNO ALTER COLUMN UR_FLAG          SET DEFAULT '';
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.DBC_IDNO');
ALTER TABLE ECSCRDB.DBC_IDNO ALTER COLUMN SMS_PRIM_CH_FLAG SET DEFAULT '';
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.DBC_IDNO');
                                                                     
ALTER TABLE ECSCRDB.CCA_OUTGOING  DROP COLUMN V_CARD_NO;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_OUTGOING');

ALTER TABLE ECSCRDB.CRD_CARD  DROP COLUMN E_INVOICE_DEPOSIT_ACCOUNT;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CRD_CARD');

ALTER TABLE ECSCRDB.DBC_CARD  DROP COLUMN E_INVOICE_DEPOSIT_ACCOUNT;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.DBC_CARD');

ALTER TABLE ECSCRDB.TSC_VD_CARD  DROP COLUMN DAY_CNT;
ALTER TABLE ECSCRDB.TSC_VD_CARD  DROP COLUMN LAST_ADDVALUE_DATE;
ALTER TABLE ECSCRDB.TSC_VD_CARD  DROP COLUMN DAY_AMT;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.TSC_VD_CARD');

ALTER TABLE ECSCRDB.CMS_CHGCOLUMN_LOG   ALTER COLUMN CHG_USER SET DATA TYPE VARCHAR(10);
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CMS_CHGCOLUMN_LOG');

ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  DROP COLUMN BONUS_POINT;
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  DROP COLUMN UNIT_PRICE;
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  DROP COLUMN TOT_TERM;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_AUTH_TXLOG');
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  DROP COLUMN REDEEM_POINT;
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  DROP COLUMN REDEEM_AMT;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_AUTH_TXLOG');

ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG   ALTER COLUMN IBM_BIT39_CODE SET DATA TYPE VARCHAR(3);
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_AUTH_TXLOG');

ALTER TABLE ECSCRDB.CRD_ITEM_UNIT  DROP COLUMN ISSUER_CONFIGURATION_ID;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CRD_ITEM_UNIT');

ALTER TABLE ECSCRDB.CCA_CONSUME  DROP COLUMN AUTH_TXLOG_AMT_1;
ALTER TABLE ECSCRDB.CCA_CONSUME  DROP COLUMN AUTH_TXLOG_AMT_CASH_1;
ALTER TABLE ECSCRDB.CCA_CONSUME  DROP COLUMN AUTH_TXLOG_AMT_2;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_CONSUME');
ALTER TABLE ECSCRDB.CCA_CONSUME  DROP COLUMN AUTH_TXLOG_AMT_CASH_2;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_CONSUME');
   
ALTER TABLE ECSCRDB.CMS_RIGHT_PARM  DROP COLUMN USE_CNT_COND;
ALTER TABLE ECSCRDB.CMS_RIGHT_PARM  DROP COLUMN USE_MAX_CNT;
ALTER TABLE ECSCRDB.CMS_RIGHT_PARM  DROP COLUMN CURR_MIN_AMT;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CMS_RIGHT_PARM');
ALTER TABLE ECSCRDB.CMS_RIGHT_PARM  DROP COLUMN CURR_TOT_CNT;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CMS_RIGHT_PARM');

ALTER TABLE ECSCRDB.BIL_PROD_NCCC ALTER COLUMN FEE_CAT   SET DEFAULT '';
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.BIL_PROD_NCCC');


--1013

ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  DROP COLUMN INSTALLMENT_TYPE;
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  DROP COLUMN FIRST_PRICE;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_AUTH_TXLOG');
				   
ALTER TABLE ECSCRDB.CCA_VIP  DROP COLUMN ACNO_P_SEQNO;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_VIP');

ALTER TABLE ECSCRDB.CCA_VIP_LOG  DROP COLUMN ACNO_P_SEQNO;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_VIP_LOG');

ALTER TABLE ECSCRDB.PTR_GROUP_CODE  DROP COLUMN PURCHASE_CARD_FLAG;
ALTER TABLE ECSCRDB.PTR_GROUP_CODE  DROP COLUMN CCA_GROUP_MCHT_CHK;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.PTR_GROUP_CODE');

ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  DROP COLUMN ORI_AUTH_SEQNO;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_AUTH_TXLOG');

ALTER TABLE ECSCRDB.FSC_ICUD03  DROP COLUMN NOPASS_REASON;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.FSC_ICUD03');

ALTER TABLE ECSCRDB.FSC_ICUD15  DROP COLUMN NOPASS_REASON;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.FSC_ICUD15');

ALTER TABLE ECSCRDB.CMS_RIGHT_PARM  DROP COLUMN CURR_AMT_COND;
ALTER TABLE ECSCRDB.CMS_RIGHT_PARM  DROP COLUMN CURR_CNT_COND;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CMS_RIGHT_PARM');

--1105
ALTER TABLE ECSCRDB.CCA_RESP_CODE ALTER NCCC_P38 SET DATA TYPE VARCHAR(2);
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_RESP_CODE');

--1110
ALTER TABLE ECSCRDB.CCA_ADJ_PARM_T  DROP COLUMN SPEC_FLAG;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_ADJ_PARM_T');

ALTER TABLE ECSCRDB.CCA_CARD_ACCT_T  DROP COLUMN ADJ_RISK_TYPE;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_CARD_ACCT_T');


terminate;

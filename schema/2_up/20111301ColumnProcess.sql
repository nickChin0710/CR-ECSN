connect to cr;

--cr 0921 
ALTER TABLE  ECSCRDB.DBC_IDNO  ADD COLUMN MARKET_AGREE_BASE   VARCHAR(1) DEFAULT ''  NOT NULL;   
COMMENT ON COLUMN ECSCRDB.DBC_IDNO.MARKET_AGREE_BASE  IS  '拒絕行銷註記';

ALTER TABLE ECSCRDB.ACT_REPAY_CREDITLIMIT ADD REMARK     VARGRAPHIC(50)   DEFAULT ''    NOT NULL ;
COMMENT ON COLUMN ECSCRDB.ACT_REPAY_CREDITLIMIT.REMARK      IS '備註說明';

ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT ADD COLUMN VIP_KIND               VARCHAR(1)       DEFAULT '' NOT NULL; 
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT ADD COLUMN IN_PERSON_COUNT        INT              DEFAULT '0'; 
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT ADD COLUMN IN_FILE_TYPE           VARCHAR(1)       DEFAULT ''; 
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT ADD COLUMN PYMT_COND              VARCHAR(2)       DEFAULT ''; 
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT ADD COLUMN PYMT_FAIL_COUNT        INT              DEFAULT '0'; 
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT ADD COLUMN PYMT_FAIL_PERSON_COUNT INT              DEFAULT '0'; 
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT ADD COLUMN PYMT_FAIL_TOT_COUNT    INT              DEFAULT '0' ;
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT ADD COLUMN ERR_CODE               VARCHAR(2)       DEFAULT ''; 
ALTER TABLE ECSCRDB.CMS_PPCARD_VISIT ADD COLUMN ERR_DESC               VARGRAPHIC(100)  DEFAULT '';

COMMENT ON COLUMN ECSCRDB.CMS_PPCARD_VISIT.VIP_KIND               IS '貴賓卡:1.新貴通(PRIORITY PASS) 2.龍騰卡(LOUNGE)  ' ;
COMMENT ON COLUMN ECSCRDB.CMS_PPCARD_VISIT.IN_PERSON_COUNT        IS '攜伴總人數  ' ;
COMMENT ON COLUMN ECSCRDB.CMS_PPCARD_VISIT.IN_FILE_TYPE           IS 'PP卡:IN-FILE-TYPE' ;
COMMENT ON COLUMN ECSCRDB.CMS_PPCARD_VISIT.PYMT_COND              IS 'PP卡:收費狀況(程式檢核結果寫入)' ;
COMMENT ON COLUMN ECSCRDB.CMS_PPCARD_VISIT.PYMT_FAIL_COUNT        IS 'PP卡:收費失敗持卡人使用數總計 ' ;
COMMENT ON COLUMN ECSCRDB.CMS_PPCARD_VISIT.PYMT_FAIL_PERSON_COUNT IS 'PP卡:收費失敗攜伴總入數' ;
COMMENT ON COLUMN ECSCRDB.CMS_PPCARD_VISIT.PYMT_FAIL_TOT_COUNT    IS 'PP卡:收費失敗總筆數' ;
COMMENT ON COLUMN ECSCRDB.CMS_PPCARD_VISIT.ERR_CODE               IS '錯誤碼' ;
COMMENT ON COLUMN ECSCRDB.CMS_PPCARD_VISIT.ERR_DESC               IS '錯誤說明 ' ;

ALTER TABLE ECSCRDB.CCA_OPPOSITION ADD COLUMN FISC_REASON_CODE VARCHAR(2) NOT NULL DEFAULT;
COMMENT ON COLUMN ECSCRDB.CCA_OPPOSITION.FISC_REASON_CODE IS 'FISC原因碼' ;

ALTER TABLE ECSCRDB.CCA_COUNTRY ADD COLUMN COUNTRY_NO VARCHAR(3) NOT NULL DEFAULT;
COMMENT ON COLUMN ECSCRDB.CCA_COUNTRY.COUNTRY_NO IS '國家數字代碼' ;

ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  ADD COLUMN OPEN_CHK          VARCHAR(1)      DEFAULT '' NOT NULL ;
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  ADD COLUMN MCHT_CHK          VARCHAR(1)      DEFAULT '' NOT NULL ;
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  ADD COLUMN OVERSEA_CHK       VARCHAR(1)      DEFAULT '' NOT NULL ;
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  ADD COLUMN AVG_CONSUME_CHK   VARCHAR(1)      DEFAULT '' NOT NULL ;
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  ADD COLUMN MONTH_RISK_CHK    VARCHAR(1)      DEFAULT '' NOT NULL ;
ALTER TABLE ECSCRDB.CCA_DEBIT_PARM  ADD COLUMN DAY_RISK_CHK      VARCHAR(1)      DEFAULT '' NOT NULL ;
                                                                            
COMMENT ON COLUMN ECSCRDB.CCA_DEBIT_PARM.OPEN_CHK        IS '是否要做開卡檢核      ' ;    
COMMENT ON COLUMN ECSCRDB.CCA_DEBIT_PARM.MCHT_CHK        IS '是否要做風險特店檢核  ' ; 
COMMENT ON COLUMN ECSCRDB.CCA_DEBIT_PARM.OVERSEA_CHK     IS '是否要做國外消費檢核  ' ; 
COMMENT ON COLUMN ECSCRDB.CCA_DEBIT_PARM.AVG_CONSUME_CHK IS '是否要做平均消費檢核  ' ; 
COMMENT ON COLUMN ECSCRDB.CCA_DEBIT_PARM.MONTH_RISK_CHK  IS '是否要做月限次風險檢核' ;                                                                         
COMMENT ON COLUMN ECSCRDB.CCA_DEBIT_PARM.DAY_RISK_CHK    IS '是否要做日限次風險檢核' ; 
                                                                        
ALTER TABLE ECSCRDB.DBC_IDNO ALTER COLUMN INST_FLAG        SET DEFAULT 'N';
ALTER TABLE ECSCRDB.DBC_IDNO ALTER COLUMN FEE_CODE_I       SET DEFAULT 'N';
ALTER TABLE ECSCRDB.DBC_IDNO ALTER COLUMN UR_FLAG          SET DEFAULT 'N';
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.DBC_IDNO');
ALTER TABLE ECSCRDB.DBC_IDNO ALTER COLUMN SMS_PRIM_CH_FLAG SET DEFAULT 'Y';
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.DBC_IDNO');

COMMENT ON COLUMN ECSCRDB.DBC_IDNO.INST_FLAG        IS '分期同意書，欄位預設為N';
COMMENT ON COLUMN ECSCRDB.DBC_IDNO.FEE_CODE_I       IS '頂級卡年費優惠註記，欄位預設為N';
COMMENT ON COLUMN ECSCRDB.DBC_IDNO.UR_FLAG          IS '歐盟註記，欄位預設為N';
COMMENT ON COLUMN ECSCRDB.DBC_IDNO.SMS_PRIM_CH_FLAG IS '是否接受附卡人交易簡訊，欄位預設為Y';

ALTER TABLE ECSCRDB.CCA_OUTGOING ADD COLUMN V_CARD_NO VARCHAR(19) NOT NULL DEFAULT;
COMMENT ON COLUMN ECSCRDB.CCA_OUTGOING.V_CARD_NO        IS '虛擬卡號';

ALTER TABLE  ECSCRDB.CRD_CARD  ADD COLUMN E_INVOICE_DEPOSIT_ACCOUNT   VARCHAR(1)  DEFAULT ''    NOT NULL; 
ALTER TABLE  ECSCRDB.DBC_CARD  ADD COLUMN E_INVOICE_DEPOSIT_ACCOUNT   VARCHAR(1)  DEFAULT ''    NOT NULL; 

COMMENT ON COLUMN ECSCRDB.CRD_CARD.E_INVOICE_DEPOSIT_ACCOUNT  IS  '電子發票約定中獎入戶 A：表示持卡人同意 B：表示持卡人不同意';
COMMENT ON COLUMN ECSCRDB.DBC_CARD.E_INVOICE_DEPOSIT_ACCOUNT  IS  '電子發票約定中獎入戶 A：表示持卡人同意 B：表示持卡人不同意';

ALTER TABLE ECSCRDB.TSC_VD_CARD ADD DAY_CNT DECIMAL NOT NULL DEFAULT 0;
ALTER TABLE ECSCRDB.TSC_VD_CARD ADD LAST_ADDVALUE_DATE VARCHAR(8) NOT NULL DEFAULT '';
ALTER TABLE ECSCRDB.TSC_VD_CARD ADD DAY_AMT DECIMAL NOT NULL DEFAULT 0;

COMMENT ON COLUMN ECSCRDB.TSC_VD_CARD.DAY_CNT IS '每日自動加值累計次數';
COMMENT ON COLUMN ECSCRDB.TSC_VD_CARD.LAST_ADDVALUE_DATE IS '最近一次使用自動加值的日期';
COMMENT ON COLUMN ECSCRDB.TSC_VD_CARD.DAY_AMT IS '每日自動加值累計金額';

ALTER TABLE ECSCRDB.CMS_CHGCOLUMN_LOG   ALTER COLUMN CHG_USER     SET DATA TYPE VARCHAR(20);  
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CMS_CHGCOLUMN_LOG');

COMMENT ON COLUMN ECSCRDB.CMS_CHGCOLUMN_LOG.CHG_USER IS '主檔修改人員';  

ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  ADD COLUMN BONUS_POINT  DECIMAL(10,2) NOT NULL DEFAULT 0;  
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  ADD COLUMN UNIT_PRICE   DECIMAL(10,2) NOT NULL DEFAULT 0;  
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  ADD COLUMN TOT_TERM     VARCHAR(4)    NOT NULL DEFAULT '';  
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  ADD COLUMN REDEEM_POINT DECIMAL(10,2) NOT NULL DEFAULT 0;  
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG  ADD COLUMN REDEEM_AMT   DECIMAL(10,2) NOT NULL DEFAULT 0;  

COMMENT ON COLUMN ECSCRDB.CCA_AUTH_TXLOG.BONUS_POINT  IS '原紅利點數';
COMMENT ON COLUMN ECSCRDB.CCA_AUTH_TXLOG.UNIT_PRICE   IS '每期金額';
COMMENT ON COLUMN ECSCRDB.CCA_AUTH_TXLOG.TOT_TERM     IS '分期期數';
COMMENT ON COLUMN ECSCRDB.CCA_AUTH_TXLOG.REDEEM_POINT IS '抵扣點數';
COMMENT ON COLUMN ECSCRDB.CCA_AUTH_TXLOG.REDEEM_AMT   IS '抵扣金額';


ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG ALTER IBM_BIT39_CODE SET DATA TYPE VARCHAR(4);
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_AUTH_TXLOG');

ALTER TABLE  ECSCRDB.CRD_ITEM_UNIT  ADD  ISSUER_CONFIGURATION_ID   VARCHAR(8) DEFAULT '' NOT NULL;
COMMENT ON COLUMN ECSCRDB.CRD_ITEM_UNIT.ISSUER_CONFIGURATION_ID IS  'GOOGLE PAY 卡樣代碼';        

ALTER TABLE  ECSCRDB.CCA_CONSUME  ADD  AUTH_TXLOG_AMT_1        DECIMAL(12,2) NOT NULL DEFAULT 0 ;
ALTER TABLE  ECSCRDB.CCA_CONSUME  ADD  AUTH_TXLOG_AMT_CASH_1   DECIMAL(12,2) NOT NULL DEFAULT 0 ;
ALTER TABLE  ECSCRDB.CCA_CONSUME  ADD  AUTH_TXLOG_AMT_2        DECIMAL(12,2) NOT NULL DEFAULT 0 ;
ALTER TABLE  ECSCRDB.CCA_CONSUME  ADD  AUTH_TXLOG_AMT_CASH_2   DECIMAL(12,2) NOT NULL DEFAULT 0 ;

COMMENT ON COLUMN ECSCRDB.CCA_CONSUME.AUTH_TXLOG_AMT_1      IS  'day1授權紀錄通知總金額    ';   
COMMENT ON COLUMN ECSCRDB.CCA_CONSUME.AUTH_TXLOG_AMT_CASH_1 IS  'day1授權紀錄通知預現總金額';  
COMMENT ON COLUMN ECSCRDB.CCA_CONSUME.AUTH_TXLOG_AMT_2      IS  'day2授權紀錄通知總金額    ';  
COMMENT ON COLUMN ECSCRDB.CCA_CONSUME.AUTH_TXLOG_AMT_CASH_2 IS  'day2授權紀錄通知預現總金額';  

ALTER TABLE ECSCRDB.CMS_RIGHT_PARM ADD COLUMN USE_CNT_COND VARCHAR(1)    WITH DEFAULT ''; 
ALTER TABLE ECSCRDB.CMS_RIGHT_PARM ADD COLUMN USE_MAX_CNT  INT           WITH DEFAULT '0'; 
ALTER TABLE ECSCRDB.CMS_RIGHT_PARM ADD COLUMN CURR_MIN_AMT DECIMAL(9)    WITH DEFAULT '0';
ALTER TABLE ECSCRDB.CMS_RIGHT_PARM ADD COLUMN CURR_TOT_CNT INT           WITH DEFAULT '0'; 

COMMENT ON COLUMN ECSCRDB.CMS_RIGHT_PARM.USE_CNT_COND IS '使用次數期別' ;    
COMMENT ON COLUMN ECSCRDB.CMS_RIGHT_PARM.USE_MAX_CNT  IS '使用次數限制' ; 
COMMENT ON COLUMN ECSCRDB.CMS_RIGHT_PARM.CURR_MIN_AMT IS '單筆最低金額' ;
COMMENT ON COLUMN ECSCRDB.CMS_RIGHT_PARM.CURR_TOT_CNT IS '累積消費筆數' ;

COMMENT ON COLUMN ECSCRDB.CMS_RIGHT_PARM.CURR_PRE_MONTH IS '近N個月刷卡消費' ;
--

ALTER TABLE ECSCRDB.BIL_PROD_NCCC ALTER COLUMN FEE_CAT   SET DEFAULT 'I';
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.BIL_PROD_NCCC');

--1013

GRANT SELECT,INSERT,DELETE,UPDATE ON "ECSCRDB "."CCA_RISK_CONSUME_PARM_T"  TO USER crap1, USER dcdbmod ;
GRANT SELECT ON "ECSCRDB "."CCA_RISK_CONSUME_PARM_T"  TO USER emdap41, USER emdap42 ;
				   
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG ADD INSTALLMENT_TYPE VARCHAR(1)    NOT NULL DEFAULT '';
ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG ADD FIRST_PRICE      DECIMAL(10,2) NOT NULL WITH DEFAULT 0 ;
COMMENT ON COLUMN ECSCRDB.CCA_AUTH_TXLOG.INSTALLMENT_TYPE IS '分期種類 I:分期內含手續費  E:分期外加手續費';
COMMENT ON COLUMN ECSCRDB.CCA_AUTH_TXLOG.FIRST_PRICE      IS '首期金額';

ALTER TABLE  ECSCRDB.CCA_VIP     ADD COLUMN ACNO_P_SEQNO    VARGRAPHIC(10) NOT NULL WITH DEFAULT '';
ALTER TABLE  ECSCRDB.CCA_VIP_LOG ADD COLUMN ACNO_P_SEQNO    VARGRAPHIC(10) NOT NULL WITH DEFAULT '';

COMMENT ON COLUMN "ECSCRDB"."CCA_VIP"."ACNO_P_SEQNO" IS '帳戶流水號';
COMMENT ON COLUMN "ECSCRDB"."CCA_VIP_LOG"."ACNO_P_SEQNO" IS '帳戶流水號';

ALTER TABLE  ECSCRDB.PTR_GROUP_CODE  ADD COLUMN PURCHASE_CARD_FLAG  VARCHAR(1) NOT NULL DEFAULT '' ;  
ALTER TABLE  ECSCRDB.PTR_GROUP_CODE  ADD COLUMN CCA_GROUP_MCHT_CHK  VARCHAR(1) NOT NULL DEFAULT '' ;
COMMENT ON COLUMN ECSCRDB.PTR_GROUP_CODE.PURCHASE_CARD_FLAG IS  '採購卡註記';
COMMENT ON COLUMN ECSCRDB.PTR_GROUP_CODE.CCA_GROUP_MCHT_CHK IS  '指定採購、配銷卡特定消費設定';

ALTER TABLE ECSCRDB.CCA_AUTH_TXLOG ADD COLUMN ORI_AUTH_SEQNO VARCHAR(12)   DEFAULT '' NOT NULL;
COMMENT ON COLUMN ECSCRDB.CCA_AUTH_TXLOG.ORI_AUTH_SEQNO IS  '原始授權流水號';

ALTER TABLE ECSCRDB.FSC_ICUD03 ADD COLUMN NOPASS_REASON VARCHAR(2)   DEFAULT '' NOT NULL;
COMMENT ON COLUMN ECSCRDB.FSC_ICUD03.NOPASS_REASON IS  '失敗原因';

ALTER TABLE ECSCRDB.FSC_ICUD15 ADD COLUMN NOPASS_REASON VARCHAR(2)   DEFAULT '' NOT NULL;
COMMENT ON COLUMN ECSCRDB.FSC_ICUD15.NOPASS_REASON IS  '失敗原因';

ALTER TABLE ECSCRDB.CMS_RIGHT_PARM ADD COLUMN CURR_AMT_COND VARCHAR(1)         WITH DEFAULT ''; 
ALTER TABLE ECSCRDB.CMS_RIGHT_PARM ADD COLUMN CURR_CNT_COND VARCHAR(1)         WITH DEFAULT ''; 
COMMENT ON COLUMN "ECSCRDB"."CMS_RIGHT_PARM"."CURR_AMT_COND" IS '累積消費金額選項' ;            
COMMENT ON COLUMN "ECSCRDB"."CMS_RIGHT_PARM"."CURR_CNT_COND" IS '累積消費筆數選項' ;

--1105
ALTER TABLE ECSCRDB.CCA_RESP_CODE ALTER NCCC_P38 SET DATA TYPE VARCHAR(6);
CALL SYSPROC.ADMIN_CMD('REORG TABLE ECSCRDB.CCA_RESP_CODE');


--1110
ALTER TABLE ECSCRDB.CCA_ADJ_PARM_T ADD COLUMN SPEC_FLAG VARCHAR(1)      DEFAULT '' NOT NULL;
COMMENT ON COLUMN "ECSCRDB"."CCA_ADJ_PARM_T"."SPEC_FLAG" IS '專款專用旗標' ;

ALTER TABLE ECSCRDB.CCA_CARD_ACCT_T ADD COLUMN ADJ_RISK_TYPE  VARCHAR(50)      DEFAULT '' NOT NULL;
COMMENT ON COLUMN "ECSCRDB"."CCA_CARD_ACCT_T"."ADJ_RISK_TYPE" IS '調整的風險分類' ;`


terminate;

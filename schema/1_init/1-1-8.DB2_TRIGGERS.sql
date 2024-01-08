connect to CR;


-------------------------------
-- DDL Statements for Triggers
-------------------------------


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACCT_CURR_HIS_A AFTER
INSERT
    ON
    ACT_ACCT_CURR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.autopay_indicator,
    '') <> nvl(new.autopay_indicator,
    '')
    OR nvl(old.curr_code,
    '') <> nvl(new.curr_code,
    '')
    OR nvl(old.autopay_acct_bank,
    '') <> nvl(new.autopay_acct_bank,
    '')
    OR nvl(old.autopay_acct_no,
    '') <> nvl(new.autopay_acct_no,
    '')
    OR nvl(old.autopay_id,
    '') <> nvl(new.autopay_id,
    '')
    OR nvl(old.autopay_id_code,
    '') <> nvl(new.autopay_id_code,
    '')
    OR nvl(old.autopay_dc_flag,
    '') <> nvl(new.autopay_dc_flag,
    '')
    OR nvl(old.autopay_dc_indicator,
    '') <> nvl(new.autopay_dc_indicator,
    '')
    OR nvl(old.no_interest_flag,
    '') <> nvl(new.no_interest_flag,
    '')
    OR nvl(old.no_interest_s_month,
    '') <> nvl(new.no_interest_s_month,
    '')
    OR nvl(old.no_interest_e_month,
    '') <> nvl(new.no_interest_e_month,
    '') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    ACT_ACCT_CURR_HIS( MOD_HIS_AUDCODE,
    MOD_HIS_DATE,
    P_SEQNO,
    ACCT_TYPE,
    CURR_CODE,
    AUTOPAY_INDICATOR,
    AUTOPAY_ACCT_BANK,
    AUTOPAY_ACCT_NO,
    AUTOPAY_ID,
    AUTOPAY_ID_CODE,
    AUTOPAY_DC_FLAG,
    NO_INTEREST_FLAG,
    NO_INTEREST_S_MONTH,
    NO_INTEREST_E_MONTH,
    MP_1_AMT,
    MP_1_S_MONTH,
    MP_1_E_MONTH,
    AUTOPAY_BEG_AMT,
    DC_AUTOPAY_BEG_AMT,
    AUTOPAY_BAL,
    DC_AUTOPAY_BAL,
    MIN_PAY,
    DC_MIN_PAY,
    MIN_PAY_BAL,
    DC_MIN_PAY_BAL,
    TTL_AMT,
    DC_TTL_AMT,
    TTL_AMT_BAL,
    DC_TTL_AMT_BAL,
    BEG_BAL_LK,
    DC_BEG_BAL_LK,
    END_BAL_LK,
    DC_END_BAL_LK,
    BEG_BAL_OP,
    DC_BEG_BAL_OP,
    END_BAL_OP,
    DC_END_BAL_OP,
    TEMP_UNBILL_INTEREST,
    DC_TEMP_UNBILL_INTEREST,
    LAST_TTL_AMT,
    DC_LAST_TTL_AMT,
    OVERPAY_LOCK_STA_DATE,
    OVERPAY_LOCK_DUE_DATE,
    BILL_SORT_SEQ,
    ADJUST_DR_AMT,
    DC_ADJUST_DR_AMT,
    ADJUST_DR_CNT,
    ADJUST_CR_AMT,
    DC_ADJUST_CR_AMT,
    ACCT_JRNL_BAL,
    DC_ACCT_JRNL_BAL,
    ADJUST_CR_CNT,
    PAY_AMT,
    DC_PAY_AMT,
    PAY_CNT,
    CRT_USER,
    CRT_DATE,
    APR_FLAG,
    APR_DATE,
    APR_USER,
    MOD_USER,
    MOD_TIME,
    MOD_PGM,
    MOD_SEQNO,
    DELAYPAY_OK_FLAG,
    autopay_dc_indicator )
VALUES ( wk_audcode,
to_char(sysdate,
'yyyymmddhh24miss'),
new.P_SEQNO,
new.ACCT_TYPE,
new.CURR_CODE,
new.AUTOPAY_INDICATOR,
new.AUTOPAY_ACCT_BANK,
new.AUTOPAY_ACCT_NO,
new.AUTOPAY_ID,
new.AUTOPAY_ID_CODE,
new.AUTOPAY_DC_FLAG,
new.NO_INTEREST_FLAG,
new.NO_INTEREST_S_MONTH,
new.NO_INTEREST_E_MONTH,
new.MP_1_AMT,
new.MP_1_S_MONTH,
new.MP_1_E_MONTH,
new.AUTOPAY_BEG_AMT,
new.DC_AUTOPAY_BEG_AMT,
new.AUTOPAY_BAL,
new.DC_AUTOPAY_BAL,
new.MIN_PAY,
new.DC_MIN_PAY,
new.MIN_PAY_BAL,
new.DC_MIN_PAY_BAL,
new.TTL_AMT,
new.DC_TTL_AMT,
new.TTL_AMT_BAL,
new.DC_TTL_AMT_BAL,
new.BEG_BAL_LK,
new.DC_BEG_BAL_LK,
new.END_BAL_LK,
new.DC_END_BAL_LK,
new.BEG_BAL_OP,
new.DC_BEG_BAL_OP,
new.END_BAL_OP,
new.DC_END_BAL_OP,
new.TEMP_UNBILL_INTEREST,
new.DC_TEMP_UNBILL_INTEREST ,
new.LAST_TTL_AMT ,
new.DC_LAST_TTL_AMT ,
new.OVERPAY_LOCK_STA_DATE ,
new.OVERPAY_LOCK_DUE_DATE ,
new.BILL_SORT_SEQ ,
new.ADJUST_DR_AMT ,
new.DC_ADJUST_DR_AMT ,
new.ADJUST_DR_CNT ,
new.ADJUST_CR_AMT ,
new.DC_ADJUST_CR_AMT ,
new.ACCT_JRNL_BAL ,
new.DC_ACCT_JRNL_BAL ,
new.ADJUST_CR_CNT ,
new.PAY_AMT ,
new.DC_PAY_AMT,
new.PAY_CNT ,
new.CRT_USER ,
new.CRT_DATE ,
new.APR_FLAG ,
new.APR_DATE ,
new.APR_USER ,
new.MOD_USER ,
new.MOD_TIME ,
new.MOD_PGM ,
new.MOD_SEQNO ,
new.DELAYPAY_OK_FLAG ,
new.autopay_dc_indicator );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACCT_CURR_HIS_D AFTER
DELETE
    ON
    ACT_ACCT_CURR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.autopay_indicator,
    '') <> nvl(new.autopay_indicator,
    '')
    OR nvl(old.curr_code,
    '') <> nvl(new.curr_code,
    '')
    OR nvl(old.autopay_acct_bank,
    '') <> nvl(new.autopay_acct_bank,
    '')
    OR nvl(old.autopay_acct_no,
    '') <> nvl(new.autopay_acct_no,
    '')
    OR nvl(old.autopay_id,
    '') <> nvl(new.autopay_id,
    '')
    OR nvl(old.autopay_id_code,
    '') <> nvl(new.autopay_id_code,
    '')
    OR nvl(old.autopay_dc_flag,
    '') <> nvl(new.autopay_dc_flag,
    '')
    OR nvl(old.autopay_dc_indicator,
    '') <> nvl(new.autopay_dc_indicator,
    '')
    OR nvl(old.no_interest_flag,
    '') <> nvl(new.no_interest_flag,
    '')
    OR nvl(old.no_interest_s_month,
    '') <> nvl(new.no_interest_s_month,
    '')
    OR nvl(old.no_interest_e_month,
    '') <> nvl(new.no_interest_e_month,
    '') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    ACT_ACCT_CURR_HIS( MOD_HIS_AUDCODE,
    MOD_HIS_DATE,
    P_SEQNO,
    ACCT_TYPE,
    CURR_CODE,
    AUTOPAY_INDICATOR,
    AUTOPAY_ACCT_BANK,
    AUTOPAY_ACCT_NO,
    AUTOPAY_ID,
    AUTOPAY_ID_CODE,
    AUTOPAY_DC_FLAG,
    NO_INTEREST_FLAG,
    NO_INTEREST_S_MONTH,
    NO_INTEREST_E_MONTH,
    MP_1_AMT,
    MP_1_S_MONTH,
    MP_1_E_MONTH,
    AUTOPAY_BEG_AMT,
    DC_AUTOPAY_BEG_AMT,
    AUTOPAY_BAL,
    DC_AUTOPAY_BAL,
    MIN_PAY,
    DC_MIN_PAY,
    MIN_PAY_BAL,
    DC_MIN_PAY_BAL,
    TTL_AMT,
    DC_TTL_AMT,
    TTL_AMT_BAL,
    DC_TTL_AMT_BAL,
    BEG_BAL_LK,
    DC_BEG_BAL_LK,
    END_BAL_LK,
    DC_END_BAL_LK,
    BEG_BAL_OP,
    DC_BEG_BAL_OP,
    END_BAL_OP,
    DC_END_BAL_OP,
    TEMP_UNBILL_INTEREST,
    DC_TEMP_UNBILL_INTEREST,
    LAST_TTL_AMT,
    DC_LAST_TTL_AMT,
    OVERPAY_LOCK_STA_DATE,
    OVERPAY_LOCK_DUE_DATE,
    BILL_SORT_SEQ,
    ADJUST_DR_AMT,
    DC_ADJUST_DR_AMT,
    ADJUST_DR_CNT,
    ADJUST_CR_AMT,
    DC_ADJUST_CR_AMT,
    ACCT_JRNL_BAL,
    DC_ACCT_JRNL_BAL,
    ADJUST_CR_CNT,
    PAY_AMT,
    DC_PAY_AMT,
    PAY_CNT,
    CRT_USER,
    CRT_DATE,
    APR_FLAG,
    APR_DATE,
    APR_USER,
    MOD_USER,
    MOD_TIME,
    MOD_PGM,
    MOD_SEQNO,
    DELAYPAY_OK_FLAG ,
    autopay_dc_indicator )
VALUES ( wk_audcode,
to_char(sysdate,
'yyyymmddhh24miss'),
old.P_SEQNO,
old.ACCT_TYPE,
old.CURR_CODE,
old.AUTOPAY_INDICATOR,
old.AUTOPAY_ACCT_BANK,
old.AUTOPAY_ACCT_NO,
old.AUTOPAY_ID,
old.AUTOPAY_ID_CODE,
old.AUTOPAY_DC_FLAG,
old.NO_INTEREST_FLAG,
old.NO_INTEREST_S_MONTH,
old.NO_INTEREST_E_MONTH,
old.MP_1_AMT,
old.MP_1_S_MONTH,
old.MP_1_E_MONTH,
old.AUTOPAY_BEG_AMT,
old.DC_AUTOPAY_BEG_AMT,
old.AUTOPAY_BAL,
old.DC_AUTOPAY_BAL,
old.MIN_PAY,
old.DC_MIN_PAY,
old.MIN_PAY_BAL,
old.DC_MIN_PAY_BAL,
old.TTL_AMT,
old.DC_TTL_AMT,
old.TTL_AMT_BAL,
old.DC_TTL_AMT_BAL,
old.BEG_BAL_LK,
old.DC_BEG_BAL_LK,
old.END_BAL_LK,
old.DC_END_BAL_LK,
old.BEG_BAL_OP,
old.DC_BEG_BAL_OP,
old.END_BAL_OP,
old.DC_END_BAL_OP,
old.TEMP_UNBILL_INTEREST,
old.DC_TEMP_UNBILL_INTEREST,
old.LAST_TTL_AMT,
old.DC_LAST_TTL_AMT,
old.OVERPAY_LOCK_STA_DATE,
old.OVERPAY_LOCK_DUE_DATE,
old.BILL_SORT_SEQ,
old.ADJUST_DR_AMT,
old.DC_ADJUST_DR_AMT,
old.ADJUST_DR_CNT,
old.ADJUST_CR_AMT,
old.DC_ADJUST_CR_AMT,
old.ACCT_JRNL_BAL,
old.DC_ACCT_JRNL_BAL,
old.ADJUST_CR_CNT,
old.PAY_AMT,
old.DC_PAY_AMT,
old.PAY_CNT,
old.CRT_USER,
old.CRT_DATE,
old.APR_FLAG,
old.APR_DATE,
old.APR_USER,
old.MOD_USER,
old.MOD_TIME,
old.MOD_PGM,
old.MOD_SEQNO,
old.DELAYPAY_OK_FLAG,
old.autopay_dc_indicator );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACCT_CURR_HIS_U AFTER
UPDATE
    OF autopay_indicator,
    curr_code,
    autopay_acct_bank,
    autopay_acct_no,
    autopay_id,
    autopay_id_code,
    autopay_dc_flag,
    autopay_dc_indicator,
    no_interest_flag,
    no_interest_s_month,
    no_interest_e_month ON
    ACT_ACCT_CURR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.autopay_indicator,
    '') <> nvl(new.autopay_indicator,
    '')
    OR nvl(old.curr_code,
    '') <> nvl(new.curr_code,
    '')
    OR nvl(old.autopay_acct_bank,
    '') <> nvl(new.autopay_acct_bank,
    '')
    OR nvl(old.autopay_acct_no,
    '') <> nvl(new.autopay_acct_no,
    '')
    OR nvl(old.autopay_id,
    '') <> nvl(new.autopay_id,
    '')
    OR nvl(old.autopay_id_code,
    '') <> nvl(new.autopay_id_code,
    '')
    OR nvl(old.autopay_dc_flag,
    '') <> nvl(new.autopay_dc_flag,
    '')
    OR nvl(old.autopay_dc_indicator,
    '') <> nvl(new.autopay_dc_indicator,
    '')
    OR nvl(old.no_interest_flag,
    '') <> nvl(new.no_interest_flag,
    '')
    OR nvl(old.no_interest_s_month,
    '') <> nvl(new.no_interest_s_month,
    '')
    OR nvl(old.no_interest_e_month,
    '') <> nvl(new.no_interest_e_month,
    '') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    ACT_ACCT_CURR_HIS( MOD_HIS_AUDCODE,
    MOD_HIS_DATE,
    P_SEQNO,
    ACCT_TYPE,
    CURR_CODE,
    AUTOPAY_INDICATOR,
    AUTOPAY_ACCT_BANK,
    AUTOPAY_ACCT_NO,
    AUTOPAY_ID,
    AUTOPAY_ID_CODE,
    AUTOPAY_DC_FLAG,
    NO_INTEREST_FLAG,
    NO_INTEREST_S_MONTH,
    NO_INTEREST_E_MONTH,
    MP_1_AMT,
    MP_1_S_MONTH,
    MP_1_E_MONTH,
    AUTOPAY_BEG_AMT,
    DC_AUTOPAY_BEG_AMT,
    AUTOPAY_BAL,
    DC_AUTOPAY_BAL,
    MIN_PAY,
    DC_MIN_PAY,
    MIN_PAY_BAL,
    DC_MIN_PAY_BAL,
    TTL_AMT,
    DC_TTL_AMT,
    TTL_AMT_BAL,
    DC_TTL_AMT_BAL,
    BEG_BAL_LK,
    DC_BEG_BAL_LK,
    END_BAL_LK,
    DC_END_BAL_LK,
    BEG_BAL_OP,
    DC_BEG_BAL_OP,
    END_BAL_OP,
    DC_END_BAL_OP,
    TEMP_UNBILL_INTEREST,
    DC_TEMP_UNBILL_INTEREST,
    LAST_TTL_AMT,
    DC_LAST_TTL_AMT,
    OVERPAY_LOCK_STA_DATE,
    OVERPAY_LOCK_DUE_DATE,
    BILL_SORT_SEQ,
    ADJUST_DR_AMT,
    DC_ADJUST_DR_AMT,
    ADJUST_DR_CNT,
    ADJUST_CR_AMT,
    DC_ADJUST_CR_AMT,
    ACCT_JRNL_BAL,
    DC_ACCT_JRNL_BAL,
    ADJUST_CR_CNT,
    PAY_AMT,
    DC_PAY_AMT,
    PAY_CNT,
    CRT_USER,
    CRT_DATE,
    APR_FLAG,
    APR_DATE,
    APR_USER,
    MOD_USER,
    MOD_TIME,
    MOD_PGM,
    MOD_SEQNO,
    DELAYPAY_OK_FLAG,
    autopay_dc_indicator )
VALUES ( wk_audcode,
to_char(sysdate,
'yyyymmddhh24miss'),
new.P_SEQNO,
new.ACCT_TYPE,
new.CURR_CODE,
new.AUTOPAY_INDICATOR,
new.AUTOPAY_ACCT_BANK,
new.AUTOPAY_ACCT_NO,
new.AUTOPAY_ID,
new.AUTOPAY_ID_CODE,
new.AUTOPAY_DC_FLAG,
new.NO_INTEREST_FLAG,
new.NO_INTEREST_S_MONTH,
new.NO_INTEREST_E_MONTH,
new.MP_1_AMT,
new.MP_1_S_MONTH,
new.MP_1_E_MONTH,
new.AUTOPAY_BEG_AMT,
new.DC_AUTOPAY_BEG_AMT,
new.AUTOPAY_BAL,
new.DC_AUTOPAY_BAL,
new.MIN_PAY,
new.DC_MIN_PAY,
new.MIN_PAY_BAL,
new.DC_MIN_PAY_BAL,
new.TTL_AMT,
new.DC_TTL_AMT,
new.TTL_AMT_BAL,
new.DC_TTL_AMT_BAL,
new.BEG_BAL_LK,
new.DC_BEG_BAL_LK,
new.END_BAL_LK,
new.DC_END_BAL_LK,
new.BEG_BAL_OP,
new.DC_BEG_BAL_OP,
new.END_BAL_OP,
new.DC_END_BAL_OP,
new.TEMP_UNBILL_INTEREST,
new.DC_TEMP_UNBILL_INTEREST ,
new.LAST_TTL_AMT ,
new.DC_LAST_TTL_AMT ,
new.OVERPAY_LOCK_STA_DATE ,
new.OVERPAY_LOCK_DUE_DATE ,
new.BILL_SORT_SEQ ,
new.ADJUST_DR_AMT ,
new.DC_ADJUST_DR_AMT ,
new.ADJUST_DR_CNT ,
new.ADJUST_CR_AMT ,
new.DC_ADJUST_CR_AMT ,
new.ACCT_JRNL_BAL ,
new.DC_ACCT_JRNL_BAL ,
new.ADJUST_CR_CNT ,
new.PAY_AMT ,
new.DC_PAY_AMT,
new.PAY_CNT ,
new.CRT_USER ,
new.CRT_DATE ,
new.APR_FLAG ,
new.APR_DATE ,
new.APR_USER ,
new.MOD_USER ,
new.MOD_TIME ,
new.MOD_PGM ,
new.MOD_SEQNO ,
new.DELAYPAY_OK_FLAG ,
new.autopay_dc_indicator );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACNO_ACT2APS_U AFTER
UPDATE
    OF BLOCK_STATUS ON
    CCA_CARD_ACCT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.block_status,
    ' ') <> NVL(old.block_status,
    ' ') )
BEGIN
        DECLARE wk_date VARCHAR(8);--
--
 DECLARE wk_time VARCHAR(6);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--  select to_char(sysdate, 'YYYYMMDDHH24MISSSSS') into wk_modtime from dual;--
--  select to_char(sysdate, 'YYYYMMDD') into wk_date from dual;--

--  select to_char(sysdate, 'HH24MISSSSS') into wk_time from dual;--
 INSERT
    INTO
    act_toaps ( mod_time,
    mod_user,
    mod_pgm,
    mod_table,
    p_seqno,
    id_p_seqno,
    proc_mark,
    update_date,
    update_time )
VALUES ( sysdate,
new.mod_user,
new.mod_pgm,
'ACT_ACNO',
new.p_seqno,
'',
'0',
to_char(sysdate,
'YYYYMMDD'),
to_char(sysdate,
'HH24MISS') ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACNO_HIST_A AFTER
UPDATE
    OF risk_bank_no,
    class_code,
    vip_code,
    autopay_acct_no,
    autopay_id,
    autopay_id_code,
    autopay_indicator,
    autopay_rate,
    autopay_fix_amt,
    min_pay_rate,
    rc_use_indicator,
    bill_sending_zip,
    bill_sending_addr1,
    bill_sending_addr2,
    bill_sending_addr3,
    bill_sending_addr4,
    bill_sending_addr5,
    stat_send_paper,
    stat_send_internet,
    stat_send_fax,
    stat_send_s_month,
    stat_send_e_month,
    stat_send_s_month2,
    stat_send_e_month2,
    internet_upd_user,
    internet_upd_date,
    paper_upd_user,
    paper_upd_date ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.risk_bank_no,
    ' ') <> nvl(new.risk_bank_no,
    ' ')
    OR nvl(old.class_code,
    ' ') <> nvl(new.class_code,
    ' ')
    OR nvl(old.vip_code,
    ' ') <> nvl(new.vip_code,
    ' ')
    OR nvl(old.autopay_acct_no,
    ' ') <> nvl(new.autopay_acct_no,
    ' ')
    OR nvl(old.autopay_indicator,
    ' ') <> nvl(new.autopay_indicator,
    ' ')
    OR nvl(old.autopay_rate,
    0) <> nvl(new.autopay_rate,
    0)
    OR nvl(old.autopay_fix_amt,
    0) <> nvl(new.autopay_fix_amt,
    0)
    OR nvl(old.min_pay_rate,
    0) <> nvl(new.min_pay_rate,
    0)
    OR nvl(old.rc_use_indicator,
    ' ') <> nvl(new.rc_use_indicator,
    ' ')
    OR nvl(old.bill_sending_zip,
    ' ') <> nvl(new.bill_sending_zip,
    ' ')
    OR nvl(old.bill_sending_addr1,
    ' ') <> nvl(new.bill_sending_addr1,
    ' ')
    OR nvl(old.bill_sending_addr2,
    ' ') <> nvl(new.bill_sending_addr2,
    ' ')
    OR nvl(old.bill_sending_addr3,
    ' ') <> nvl(new.bill_sending_addr3,
    ' ')
    OR nvl(old.bill_sending_addr4,
    ' ') <> nvl(new.bill_sending_addr4,
    ' ')
    OR nvl(old.bill_sending_addr5,
    ' ') <> nvl(new.bill_sending_addr5,
    ' ')
    OR nvl(old.stat_send_paper,
    ' ') <> nvl(new.stat_send_paper,
    ' ')
    OR nvl(old.stat_send_internet,
    ' ') <> nvl(new.stat_send_internet,
    ' ')
    OR nvl(old.stat_send_s_month,
    ' ') <> nvl(new.stat_send_s_month,
    ' ')
    OR nvl(old.stat_send_e_month,
    ' ') <> nvl(new.stat_send_e_month,
    ' ')
    OR nvl(old.stat_send_s_month2,
    ' ') <> nvl(new.stat_send_s_month2,
    ' ')
    OR nvl(old.stat_send_e_month2,
    ' ') <> nvl(new.stat_send_e_month2,
    ' ')
    OR nvl(old.internet_upd_user,
    ' ') <> nvl(new.internet_upd_user,
    ' ')
    OR nvl(old.internet_upd_date,
    ' ') <> nvl(new.internet_upd_date,
    ' ')
    OR nvl(old.paper_upd_user,
    ' ') <> nvl(new.paper_upd_user,
    ' ')
    OR nvl(old.paper_upd_date,
    ' ') <> nvl(new.paper_upd_date,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 SET
wk_audcode = 'A';--
--
 SET
wk_audcode = 'U';--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    act_acno_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    --p_seqno,
 acno_p_seqno,
    acct_type,
    acct_key,
    --acct_p_seqno,
 p_seqno,
    corp_p_seqno,
    corp_no,
    --corp_no_code,
 acct_status,
    stmt_cycle,
    acct_holder_id,
    acct_holder_id_code,
    id_p_seqno,
    rc_use_indicator,
    rc_use_s_date,
    rc_use_e_date,
    credit_act_no,
    line_of_credit_amt,
    inst_auth_loc_amt,
    min_pay_rate,
    autopay_indicator,
    autopay_rate,
    autopay_fix_amt,
    autopay_acct_bank,
    autopay_acct_no,
    autopay_id,
    autopay_id_code,
    class_code,
    vip_code,
    bill_sending_zip,
    bill_sending_addr1,
    bill_sending_addr2,
    bill_sending_addr3,
    bill_sending_addr4,
    bill_sending_addr5,
    reg_bank_no,
    risk_bank_no,
    card_indicator,
    update_date,
    update_user,
    stat_send_paper,
    stat_send_internet,
    stat_send_fax,
    stat_send_s_month,
    stat_send_e_month,
    stat_send_s_month2,
    stat_send_e_month2,
    internet_upd_user,
    internet_upd_date,
    paper_upd_user,
    paper_upd_date )
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
new.mod_user,
wk_audcode,
--new.p_seqno,
 new.acno_p_seqno,
new.acct_type,
new.acct_key,
--new.gp_no,
 new.p_seqno,
new.corp_p_seqno,
(
SELECT
    c.corp_no
FROM
    crd_corp c
WHERE
    new.corp_p_seqno = c.corp_p_seqno),
--new.corp_no,
--new.corp_no_code,
 new.acct_status,
new.stmt_cycle,
(
SELECT
    c.id_no
FROM
    crd_idno c
WHERE
    new.id_p_seqno = c.id_p_seqno),
--new.acct_holder_id,
(
SELECT
    c.id_no_code
FROM
    crd_idno c
WHERE
    new.id_p_seqno = c.id_p_seqno),
--new.acct_holder_id_code,
 new.id_p_seqno,
new.rc_use_indicator,
new.rc_use_s_date,
new.rc_use_e_date,
new.credit_act_no,
new.line_of_credit_amt,
new.inst_auth_loc_amt,
new.min_pay_rate,
new.autopay_indicator,
new.autopay_rate,
new.autopay_fix_amt,
new.autopay_acct_bank,
new.autopay_acct_no,
new.autopay_id,
new.autopay_id_code,
new.class_code,
new.vip_code,
new.bill_sending_zip,
new.bill_sending_addr1,
new.bill_sending_addr2,
new.bill_sending_addr3,
new.bill_sending_addr4,
new.bill_sending_addr5,
new.reg_bank_no,
new.risk_bank_no,
new.card_indicator,
new.update_date,
new.update_user,
new.stat_send_paper,
new.stat_send_internet,
new.stat_send_fax,
new.stat_send_s_month,
new.stat_send_e_month,
new.stat_send_s_month2,
new.stat_send_e_month2,
new.internet_upd_user,
new.internet_upd_date,
new.paper_upd_user,
new.paper_upd_date );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACNO_HIST_D AFTER
DELETE
    ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.risk_bank_no,
    ' ') <> nvl(new.risk_bank_no,
    ' ')
    OR nvl(old.class_code,
    ' ') <> nvl(new.class_code,
    ' ')
    OR nvl(old.vip_code,
    ' ') <> nvl(new.vip_code,
    ' ')
    OR nvl(old.autopay_acct_no,
    ' ') <> nvl(new.autopay_acct_no,
    ' ')
    OR nvl(old.autopay_indicator,
    ' ') <> nvl(new.autopay_indicator,
    ' ')
    OR nvl(old.autopay_rate,
    0) <> nvl(new.autopay_rate,
    0)
    OR nvl(old.autopay_fix_amt,
    0) <> nvl(new.autopay_fix_amt,
    0)
    OR nvl(old.min_pay_rate,
    0) <> nvl(new.min_pay_rate,
    0)
    OR nvl(old.rc_use_indicator,
    ' ') <> nvl(new.rc_use_indicator,
    ' ')
    OR nvl(old.bill_sending_zip,
    ' ') <> nvl(new.bill_sending_zip,
    ' ')
    OR nvl(old.bill_sending_addr1,
    ' ') <> nvl(new.bill_sending_addr1,
    ' ')
    OR nvl(old.bill_sending_addr2,
    ' ') <> nvl(new.bill_sending_addr2,
    ' ')
    OR nvl(old.bill_sending_addr3,
    ' ') <> nvl(new.bill_sending_addr3,
    ' ')
    OR nvl(old.bill_sending_addr4,
    ' ') <> nvl(new.bill_sending_addr4,
    ' ')
    OR nvl(old.bill_sending_addr5,
    ' ') <> nvl(new.bill_sending_addr5,
    ' ')
    OR nvl(old.stat_send_paper,
    ' ') <> nvl(new.stat_send_paper,
    ' ')
    OR nvl(old.stat_send_internet,
    ' ') <> nvl(new.stat_send_internet,
    ' ')
    OR nvl(old.stat_send_s_month,
    ' ') <> nvl(new.stat_send_s_month,
    ' ')
    OR nvl(old.stat_send_e_month,
    ' ') <> nvl(new.stat_send_e_month,
    ' ')
    OR nvl(old.stat_send_s_month2,
    ' ') <> nvl(new.stat_send_s_month2,
    ' ')
    OR nvl(old.stat_send_e_month2,
    ' ') <> nvl(new.stat_send_e_month2,
    ' ')
    OR nvl(old.internet_upd_user,
    ' ') <> nvl(new.internet_upd_user,
    ' ')
    OR nvl(old.internet_upd_date,
    ' ') <> nvl(new.internet_upd_date,
    ' ')
    OR nvl(old.paper_upd_user,
    ' ') <> nvl(new.paper_upd_user,
    ' ')
    OR nvl(old.paper_upd_date,
    ' ') <> nvl(new.paper_upd_date,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    act_acno_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    --p_seqno,
 acno_p_seqno,
    acct_type,
    acct_key,
    --acct_p_seqno,
 p_seqno,
    corp_p_seqno,
    corp_no,
    --corp_no_code,
 acct_status,
    stmt_cycle,
    acct_holder_id,
    acct_holder_id_code,
    id_p_seqno,
    rc_use_indicator,
    rc_use_s_date,
    rc_use_e_date,
    credit_act_no,
    line_of_credit_amt,
    inst_auth_loc_amt,
    min_pay_rate,
    autopay_indicator,
    autopay_rate,
    autopay_fix_amt,
    autopay_acct_bank,
    autopay_acct_no,
    autopay_id,
    autopay_id_code,
    class_code,
    vip_code,
    bill_sending_zip,
    bill_sending_addr1,
    bill_sending_addr2,
    bill_sending_addr3,
    bill_sending_addr4,
    bill_sending_addr5,
    reg_bank_no,
    risk_bank_no,
    card_indicator,
    update_date,
    update_user,
    stat_send_paper,
    stat_send_internet,
    stat_send_fax,
    stat_send_s_month,
    stat_send_e_month,
    stat_send_s_month2,
    stat_send_e_month2,
    internet_upd_user,
    internet_upd_date,
    paper_upd_user,
    paper_upd_date )
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
old.mod_pgm,
old.mod_user,
wk_audcode,
--old.p_seqno,
 old.acno_p_seqno,
old.acct_type,
old.acct_key,
--old.gp_no,
 old.p_seqno,
old.corp_p_seqno,
(
SELECT
    c.corp_no
FROM
    crd_corp c
WHERE
    old.corp_p_seqno = c.corp_p_seqno),
--old.corp_no,
--old.corp_no_code,
 old.acct_status,
old.stmt_cycle,
(
SELECT
    c.id_no
FROM
    crd_idno c
WHERE
    old.id_p_seqno = c.id_p_seqno),
--old.acct_holder_id,
(
SELECT
    c.id_no_code
FROM
    crd_idno c
WHERE
    old.id_p_seqno = c.id_p_seqno),
--old.acct_holder_id_code,
 old.id_p_seqno,
old.rc_use_indicator,
old.rc_use_s_date,
old.rc_use_e_date,
old.credit_act_no,
old.line_of_credit_amt,
old.inst_auth_loc_amt,
old.min_pay_rate,
old.autopay_indicator,
old.autopay_rate,
old.autopay_fix_amt,
old.autopay_acct_bank,
old.autopay_acct_no,
old.autopay_id,
old.autopay_id_code,
old.class_code,
old.vip_code,
old.bill_sending_zip,
old.bill_sending_addr1,
old.bill_sending_addr2,
old.bill_sending_addr3,
old.bill_sending_addr4,
old.bill_sending_addr5,
old.reg_bank_no,
old.risk_bank_no,
old.card_indicator,
old.update_date,
old.update_user,
old.stat_send_paper,
old.stat_send_internet,
old.stat_send_fax,
old.stat_send_s_month,
old.stat_send_e_month,
old.stat_send_s_month2,
old.stat_send_e_month2,
old.internet_upd_user,
old.internet_upd_date,
old.paper_upd_user,
old.paper_upd_date );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACNO_HIST_U AFTER
UPDATE
    OF risk_bank_no,
    class_code,
    vip_code,
    autopay_acct_no,
    autopay_id,
    autopay_id_code,
    autopay_indicator,
    autopay_rate,
    autopay_fix_amt,
    min_pay_rate,
    rc_use_indicator,
    bill_sending_zip,
    bill_sending_addr1,
    bill_sending_addr2,
    bill_sending_addr3,
    bill_sending_addr4,
    bill_sending_addr5,
    stat_send_paper,
    stat_send_internet,
    stat_send_fax,
    stat_send_s_month,
    stat_send_e_month,
    stat_send_s_month2,
    stat_send_e_month2,
    internet_upd_user,
    internet_upd_date,
    paper_upd_user,
    paper_upd_date ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.risk_bank_no,
    ' ') <> nvl(new.risk_bank_no,
    ' ')
    OR nvl(old.class_code,
    ' ') <> nvl(new.class_code,
    ' ')
    OR nvl(old.vip_code,
    ' ') <> nvl(new.vip_code,
    ' ')
    OR nvl(old.autopay_acct_no,
    ' ') <> nvl(new.autopay_acct_no,
    ' ')
    OR nvl(old.autopay_indicator,
    ' ') <> nvl(new.autopay_indicator,
    ' ')
    OR nvl(old.autopay_rate,
    0) <> nvl(new.autopay_rate,
    0)
    OR nvl(old.autopay_fix_amt,
    0) <> nvl(new.autopay_fix_amt,
    0)
    OR nvl(old.min_pay_rate,
    0) <> nvl(new.min_pay_rate,
    0)
    OR nvl(old.rc_use_indicator,
    ' ') <> nvl(new.rc_use_indicator,
    ' ')
    OR nvl(old.bill_sending_zip,
    ' ') <> nvl(new.bill_sending_zip,
    ' ')
    OR nvl(old.bill_sending_addr1,
    ' ') <> nvl(new.bill_sending_addr1,
    ' ')
    OR nvl(old.bill_sending_addr2,
    ' ') <> nvl(new.bill_sending_addr2,
    ' ')
    OR nvl(old.bill_sending_addr3,
    ' ') <> nvl(new.bill_sending_addr3,
    ' ')
    OR nvl(old.bill_sending_addr4,
    ' ') <> nvl(new.bill_sending_addr4,
    ' ')
    OR nvl(old.bill_sending_addr5,
    ' ') <> nvl(new.bill_sending_addr5,
    ' ')
    OR nvl(old.stat_send_paper,
    ' ') <> nvl(new.stat_send_paper,
    ' ')
    OR nvl(old.stat_send_internet,
    ' ') <> nvl(new.stat_send_internet,
    ' ')
    OR nvl(old.stat_send_s_month,
    ' ') <> nvl(new.stat_send_s_month,
    ' ')
    OR nvl(old.stat_send_e_month,
    ' ') <> nvl(new.stat_send_e_month,
    ' ')
    OR nvl(old.stat_send_s_month2,
    ' ') <> nvl(new.stat_send_s_month2,
    ' ')
    OR nvl(old.stat_send_e_month2,
    ' ') <> nvl(new.stat_send_e_month2,
    ' ')
    OR nvl(old.internet_upd_user,
    ' ') <> nvl(new.internet_upd_user,
    ' ')
    OR nvl(old.internet_upd_date,
    ' ') <> nvl(new.internet_upd_date,
    ' ')
    OR nvl(old.paper_upd_user,
    ' ') <> nvl(new.paper_upd_user,
    ' ')
    OR nvl(old.paper_upd_date,
    ' ') <> nvl(new.paper_upd_date,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 SET
wk_audcode = 'A';--
--
 SET
wk_audcode = 'U';--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    act_acno_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    --p_seqno,
 acno_p_seqno,
    acct_type,
    acct_key,
    --acct_p_seqno,
 p_seqno,
    corp_p_seqno,
    corp_no,
    --corp_no_code,
 acct_status,
    stmt_cycle,
    acct_holder_id,
    acct_holder_id_code,
    id_p_seqno,
    rc_use_indicator,
    rc_use_s_date,
    rc_use_e_date,
    credit_act_no,
    line_of_credit_amt,
    inst_auth_loc_amt,
    min_pay_rate,
    autopay_indicator,
    autopay_rate,
    autopay_fix_amt,
    autopay_acct_bank,
    autopay_acct_no,
    autopay_id,
    autopay_id_code,
    class_code,
    vip_code,
    bill_sending_zip,
    bill_sending_addr1,
    bill_sending_addr2,
    bill_sending_addr3,
    bill_sending_addr4,
    bill_sending_addr5,
    reg_bank_no,
    risk_bank_no,
    card_indicator,
    update_date,
    update_user,
    stat_send_paper,
    stat_send_internet,
    stat_send_fax,
    stat_send_s_month,
    stat_send_e_month,
    stat_send_s_month2,
    stat_send_e_month2,
    internet_upd_user,
    internet_upd_date,
    paper_upd_user,
    paper_upd_date )
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
new.mod_user,
wk_audcode,
--new.p_seqno,
 new.acno_p_seqno,
new.acct_type,
new.acct_key,
--new.gp_no,
 new.p_seqno,
new.corp_p_seqno,
(
SELECT
    c.corp_no
FROM
    crd_corp c
WHERE
    new.corp_p_seqno = c.corp_p_seqno),
--new.corp_no,
--new.corp_no_code,
 new.acct_status,
new.stmt_cycle,
(
SELECT
    c.id_no
FROM
    crd_idno c
WHERE
    new.id_p_seqno = c.id_p_seqno),
--new.acct_holder_id,
(
SELECT
    c.id_no_code
FROM
    crd_idno c
WHERE
    new.id_p_seqno = c.id_p_seqno),
--new.acct_holder_id_code,
 new.id_p_seqno,
new.rc_use_indicator,
new.rc_use_s_date,
new.rc_use_e_date,
new.credit_act_no,
new.line_of_credit_amt,
new.inst_auth_loc_amt,
new.min_pay_rate,
new.autopay_indicator,
new.autopay_rate,
new.autopay_fix_amt,
new.autopay_acct_bank,
new.autopay_acct_no,
new.autopay_id,
new.autopay_id_code,
new.class_code,
new.vip_code,
new.bill_sending_zip,
new.bill_sending_addr1,
new.bill_sending_addr2,
new.bill_sending_addr3,
new.bill_sending_addr4,
new.bill_sending_addr5,
new.reg_bank_no,
new.risk_bank_no,
new.card_indicator,
new.update_date,
new.update_user,
new.stat_send_paper,
new.stat_send_internet,
new.stat_send_fax,
new.stat_send_s_month,
new.stat_send_e_month,
new.stat_send_s_month2,
new.stat_send_e_month2,
new.internet_upd_user,
new.internet_upd_date,
new.paper_upd_user,
new.paper_upd_date );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_ACNO_STAT_TYPE_LOG_A
AFTER 
   INSERT 
ON ACT_ACNO
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL
WHEN (
        nvl(new.mod_pgm,' ') <> 'cyc_b750' and (
	nvl(new.stat_send_paper,' ') <> nvl(old.stat_send_paper,' ') or
	nvl(new.stat_send_internet,' ') <> nvl(old.stat_send_internet,' ') or
	nvl(new.stat_send_s_month,' ') <> nvl(old.stat_send_s_month,' ') or
	nvl(new.stat_send_e_month,' ') <> nvl(old.stat_send_e_month,' ') or
	nvl(new.stat_send_s_month2,' ') <> nvl(old.stat_send_s_month2,' ') or
	nvl(new.stat_send_e_month2,' ') <> nvl(old.stat_send_e_month2,' '))
      )
BEGIN
 DECLARE ls_chi_name varchar(20);--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
/******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.00       2007/12/27  JHW              R96-037
   1.01       2008/04/21  JHW              R96-037
   1.02       2008/04/29  JHW              R96-037
   2.01       2008/11/03  carrie           B97-165
******************************************************************************/

   IF nvl(new.stat_send_paper,' ')<>nvl(old.stat_send_paper,' ') OR
      nvl(new.stat_send_s_month,' ')<>nvl(old.stat_send_s_month,' ') OR
      nvl(new.stat_send_e_month,' ')<>nvl(old.stat_send_e_month,' ') THEN
        INSERT INTO act_stat_type_log
               ( mod_date,
                 mod_type,
                 p_seqno,
                 id_p_seqno,
                 acct_type,
                 chi_name,
                 stat_send_type,
                 stat_send_month_s,
                 stat_send_month_e,
                 e_mail_addr,
                 e_mail_from_mark,
                 apr_user,
                 mod_user,
                 mod_time,
                 mod_pgm )
        VALUES ( to_char(sysdate,'yyyymmdd'),
                 '1',
                 new.p_seqno,
                 new.id_p_seqno,
                 new.acct_type,
                 '',
                 '1',
                 new.stat_send_s_month,
                 new.stat_send_e_month,
                 '',
                 '',
                 '',
                 new.mod_user,
                 sysdate,
                 new.mod_pgm )  ;--
   END IF;--
   IF nvl(new.stat_send_internet,' ')<>nvl(old.stat_send_internet,' ') OR
      nvl(new.stat_send_s_month2,' ')<>nvl(old.stat_send_s_month2,' ') OR
      nvl(new.stat_send_e_month2,' ')<>nvl(old.stat_send_e_month2,' ') THEN
        INSERT INTO act_stat_type_log
               ( mod_date,
                 mod_type,
                 p_seqno,
                 id_p_seqno,
                 acct_type,
                 chi_name,
                 stat_send_type,
                 stat_send_month_s,
                 stat_send_month_e,
                 e_mail_addr,
                 e_mail_from_mark,
                 apr_user,
                 mod_user,
                 mod_time,
                 mod_pgm )
        VALUES ( to_char(sysdate,'yyyymmdd'),
                 '1',
                 new.p_seqno,
                 new.id_p_seqno,
                 new.acct_type,
                 '',
                 '2',
                 new.stat_send_s_month2,
                 new.stat_send_e_month2,
                 '',
                 '',
                 '',
                 new.mod_user,
                 sysdate,
                 new.mod_pgm )  ;--
   END IF;--
 
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_ACNO_STAT_TYPE_LOG_U
AFTER 
   UPDATE OF 
   STAT_SEND_PAPER
  ,STAT_SEND_INTERNET
  ,STAT_SEND_S_MONTH
  ,STAT_SEND_E_MONTH
  ,STAT_SEND_S_MONTH2
  ,STAT_SEND_E_MONTH2
ON ACT_ACNO
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL
WHEN (
        nvl(new.mod_pgm,' ') <> 'cyc_b750' and (
	nvl(new.stat_send_paper,' ') <> nvl(old.stat_send_paper,' ') or
	nvl(new.stat_send_internet,' ') <> nvl(old.stat_send_internet,' ') or
	nvl(new.stat_send_s_month,' ') <> nvl(old.stat_send_s_month,' ') or
	nvl(new.stat_send_e_month,' ') <> nvl(old.stat_send_e_month,' ') or
	nvl(new.stat_send_s_month2,' ') <> nvl(old.stat_send_s_month2,' ') or
	nvl(new.stat_send_e_month2,' ') <> nvl(old.stat_send_e_month2,' '))
      )
BEGIN
 DECLARE ls_chi_name varchar(20);--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
/******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.00       2007/12/27  JHW              R96-037
   1.01       2008/04/21  JHW              R96-037
   1.02       2008/04/29  JHW              R96-037
   2.01       2008/11/03  carrie           B97-165
******************************************************************************/

   IF nvl(new.stat_send_paper,' ')<>nvl(old.stat_send_paper,' ') OR
      nvl(new.stat_send_s_month,' ')<>nvl(old.stat_send_s_month,' ') OR
      nvl(new.stat_send_e_month,' ')<>nvl(old.stat_send_e_month,' ') THEN
        INSERT INTO act_stat_type_log
               ( mod_date,
                 mod_type,
                 p_seqno,
                 id_p_seqno,
                 acct_type,
                 chi_name,
                 stat_send_type,
                 stat_send_month_s,
                 stat_send_month_e,
                 e_mail_addr,
                 e_mail_from_mark,
                 apr_user,
                 mod_user,
                 mod_time,
                 mod_pgm )
        VALUES ( to_char(sysdate,'yyyymmdd'),
                 '1',
                 new.p_seqno,
                 new.id_p_seqno,
                 new.acct_type,
                 '',
                 '1',
                 new.stat_send_s_month,
                 new.stat_send_e_month,
                 '',
                 '',
                 '',
                 new.mod_user,
                 sysdate,
                 new.mod_pgm )  ;--
   END IF;--
   IF nvl(new.stat_send_internet,' ')<>nvl(old.stat_send_internet,' ') OR
      nvl(new.stat_send_s_month2,' ')<>nvl(old.stat_send_s_month2,' ') OR
      nvl(new.stat_send_e_month2,' ')<>nvl(old.stat_send_e_month2,' ') THEN
        INSERT INTO act_stat_type_log
               ( mod_date,
                 mod_type,
                 p_seqno,
                 id_p_seqno,
                 acct_type,
                 chi_name,
                 stat_send_type,
                 stat_send_month_s,
                 stat_send_month_e,
                 e_mail_addr,
                 e_mail_from_mark,
                 apr_user,
                 mod_user,
                 mod_time,
                 mod_pgm )
        VALUES ( to_char(sysdate,'yyyymmdd'),
                 '1',
                 new.p_seqno,
                 new.id_p_seqno,
                 new.acct_type,
                 '',
                 '2',
                 new.stat_send_s_month2,
                 new.stat_send_e_month2,
                 '',
                 '',
                 '',
                 new.mod_user,
                 sysdate,
                 new.mod_pgm )  ;--
   END IF;--
 
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER tr_act_acno_acct_status_u
AFTER 
   UPDATE OF acct_status,sale_date
ON ACT_ACNO
REFERENCING OLD AS old NEW AS new
FOR EACH ROW MODE DB2SQL

 BEGIN
 DECLARE wk_count             INTEGER;--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

   IF (new.acct_status <>'3') and
      (new.acct_status <>'4') and
      ((old.acct_status = '3') or (old.acct_status = '4')) THEN
      INSERT INTO act_jcic_end_log(
                  p_seqno,
                  acct_status,
                  ori_acct_status,
                  mod_pgm,
                  mod_time
                 )
          VALUES (
                  new.p_seqno,
                  new.acct_status,
                  old.acct_status,
                  'tr_act_acno_acct_status' ,
                  sysdate);--
   END IF;--
   IF (new.sale_date ='') and (old.sale_date <> '')  THEN
      INSERT INTO act_jcic_end_log(
                  p_seqno,
                  sale_date,
                  ori_sale_date,
                  mod_pgm,
                  mod_time
                 )
          VALUES (
                  new.p_seqno,
                  new.sale_date,
                  old.sale_date,
                  'tr_act_acno_acct_status' ,
                  sysdate);--
   END IF;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACT_ACNO_COL1_U AFTER
UPDATE
    OF CREDIT_ACT_NO,
    AUTOPAY_ACCT_BANK,
    AUTOPAY_ACCT_NO,
    LINE_OF_CREDIT_AMT,
    STOP_STATUS,
    --BLOCK_DATE,
    --BLOCK_REASON,
    --BLOCK_REASON2,
 NO_PER_COLL_FLAG,
    NO_TEL_COLL_FLAG,
    RC_USE_INDICATOR,
    NO_ADJ_LOC_HIGH,
    NO_ADJ_LOC_LOW,
    NO_F_STOP_FLAG,
    NO_UNBLOCK_FLAG,
    BILL_SENDING_ZIP,
    BILL_SENDING_ADDR1,
    BILL_SENDING_ADDR2,
    BILL_SENDING_ADDR3,
    BILL_SENDING_ADDR4,
    BILL_SENDING_ADDR5,
    VIP_CODE,
    STAT_SEND_INTERNET,
    STAT_SEND_S_MONTH2,
    STAT_SEND_E_MONTH2,
    STAT_SEND_PAPER,
    STAT_SEND_S_MONTH,
    STAT_SEND_E_MONTH,
    STAT_UNPRINT_FLAG,
    STAT_UNPRINT_S_MONTH,
    STAT_UNPRINT_E_MONTH ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.CREDIT_ACT_NO ,
    ' ') <> nvl(new.CREDIT_ACT_NO ,
    ' ')
    OR nvl(old.AUTOPAY_ACCT_BANK ,
    ' ') <> nvl(new.AUTOPAY_ACCT_BANK ,
    ' ')
    OR nvl(old.AUTOPAY_ACCT_NO ,
    ' ') <> nvl(new.AUTOPAY_ACCT_NO ,
    ' ')
    OR nvl(old.LINE_OF_CREDIT_AMT ,
    0 ) <> nvl(new.LINE_OF_CREDIT_AMT ,
    0 )
    OR nvl(old.STOP_STATUS ,
    ' ') <> nvl(new.STOP_STATUS ,
    ' ')
    OR
    --  nvl(old.BLOCK_DATE           , ' ') <> nvl(new.BLOCK_DATE           , ' ') or
    --  nvl(old.BLOCK_REASON         , ' ') <> nvl(new.BLOCK_REASON         , ' ') or
    --  nvl(old.BLOCK_REASON2        , ' ') <> nvl(new.BLOCK_REASON2        , ' ') or
 nvl(old.NO_PER_COLL_FLAG ,
    ' ') <> nvl(new.NO_PER_COLL_FLAG ,
    ' ')
    OR nvl(old.NO_TEL_COLL_FLAG ,
    ' ') <> nvl(new.NO_TEL_COLL_FLAG ,
    ' ')
    OR nvl(old.RC_USE_INDICATOR ,
    ' ') <> nvl(new.RC_USE_INDICATOR ,
    ' ')
    OR nvl(old.NO_ADJ_LOC_HIGH ,
    ' ') <> nvl(new.NO_ADJ_LOC_HIGH ,
    ' ')
    OR nvl(old.NO_ADJ_LOC_LOW ,
    ' ') <> nvl(new.NO_ADJ_LOC_LOW ,
    ' ')
    OR nvl(old.NO_F_STOP_FLAG ,
    ' ') <> nvl(new.NO_F_STOP_FLAG ,
    ' ')
    OR nvl(old.NO_UNBLOCK_FLAG ,
    ' ') <> nvl(new.NO_UNBLOCK_FLAG ,
    ' ')
    OR nvl(old.BILL_SENDING_ZIP ,
    ' ') <> nvl(new.BILL_SENDING_ZIP ,
    ' ')
    OR nvl(old.BILL_SENDING_ADDR1 ,
    ' ') <> nvl(new.BILL_SENDING_ADDR1 ,
    ' ')
    OR nvl(old.BILL_SENDING_ADDR2 ,
    ' ') <> nvl(new.BILL_SENDING_ADDR2 ,
    ' ')
    OR nvl(old.BILL_SENDING_ADDR3 ,
    ' ') <> nvl(new.BILL_SENDING_ADDR3 ,
    ' ')
    OR nvl(old.BILL_SENDING_ADDR4 ,
    ' ') <> nvl(new.BILL_SENDING_ADDR4 ,
    ' ')
    OR nvl(old.BILL_SENDING_ADDR5 ,
    ' ') <> nvl(new.BILL_SENDING_ADDR5 ,
    ' ')
    OR nvl(old.STAT_SEND_INTERNET ,
    ' ') <> nvl(new.STAT_SEND_INTERNET ,
    ' ')
    OR nvl(old.STAT_SEND_S_MONTH2 ,
    ' ') <> nvl(new.STAT_SEND_S_MONTH2 ,
    ' ')
    OR nvl(old.STAT_SEND_E_MONTH2 ,
    ' ') <> nvl(new.STAT_SEND_E_MONTH2 ,
    ' ')
    OR nvl(old.STAT_SEND_PAPER ,
    ' ') <> nvl(new.STAT_SEND_PAPER ,
    ' ')
    OR nvl(old.STAT_SEND_S_MONTH ,
    ' ') <> nvl(new.STAT_SEND_S_MONTH ,
    ' ')
    OR nvl(old.STAT_SEND_E_MONTH ,
    ' ') <> nvl(new.STAT_SEND_E_MONTH ,
    ' ')
    OR nvl(old.STAT_UNPRINT_FLAG ,
    ' ') <> nvl(new.STAT_UNPRINT_FLAG ,
    ' ')
    OR nvl(old.STAT_UNPRINT_S_MONTH ,
    ' ') <> nvl(new.STAT_UNPRINT_S_MONTH ,
    ' ')
    OR nvl(old.STAT_UNPRINT_E_MONTH ,
    ' ') <> nvl(new.STAT_UNPRINT_E_MONTH ,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 INSERT
    INTO
    COL_CS_LOG( proc_mark,
    cs_type,
    p_seqno,
    id_p_seqno,
    card_no,
    rela_id,
    proc_type,
    mod_time )
VALUES ( '0',
'1',
old.p_seqno,
old.id_p_seqno,
'1',
'',
'U',
sysdate );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACT_CCAS_LOG1_U AFTER
UPDATE
    OF ACCT_JRNL_BAL,
    TTL_AMT_BAL ON
    ACT_ACCT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 INSERT
    INTO
    act_ccas_log( p_seqno,
    acct_type,
    act_flag,
    corp_p_seqno,
    mod_time )
VALUES ( new.p_seqno,
new.acct_type ,
'Y' ,
new.corp_p_seqno,
to_char(sysdate,
'YYYYMMDDHH24MISS'));--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACT_CCAS_LOG2_U AFTER
UPDATE
    OF BILL_SENDING_ADDR1 ,
    BILL_SENDING_ADDR2 ,
    BILL_SENDING_ADDR3 ,
    BILL_SENDING_ADDR4 ,
    BILL_SENDING_ADDR5 ,
    CLASS_CODE ,
    CURR_PD_RATING ,
    LAST_PAY_AMT ,
    LAST_PAY_DATE ,
    LINE_OF_CREDIT_AMT ,
    MONTH_PURCHASE_LMT ,
    NO_BLOCK_E_DATE ,
    NO_BLOCK_FLAG ,
    NO_BLOCK_S_DATE ,
    NO_F_STOP_E_DATE ,
    NO_F_STOP_FLAG ,
    NO_F_STOP_S_DATE ,
    NO_UNBLOCK_E_DATE ,
    NO_UNBLOCK_FLAG ,
    NO_UNBLOCK_S_DATE ,
    PAYMENT_RATE1 ,
    PAY_BY_STAGE_FLAG ,
    RC_USE_INDICATOR ,
    RISK_BANK_NO ,
    VIP_CODE ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 INSERT
    INTO
    act_ccas_log( p_seqno,
    acct_type,
    act_flag,
    corp_p_seqno,
    mod_time )
VALUES ( new.p_seqno,
new.acct_type ,
'N' ,
new.corp_p_seqno,
to_char(sysdate,
'YYYYMMDDHH24MISS'));--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACT_CCAS_LOG3_U AFTER
UPDATE
    OF POST_CYCLE_DD,
    REFUND_APR_DATE ON
    BIL_CONTRACT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 INSERT
    INTO
    act_ccas_log( p_seqno,
    acct_type,
    act_flag,
    corp_p_seqno,
    mod_time )
SELECT
    p_seqno,
    acct_type,
    'N',
    corp_p_seqno,
    to_char(sysdate,
    'YYYYMMDDHH24MISS')
FROM
    act_acno
WHERE
    acct_type = new.acct_type
    AND acct_key = (
    SELECT
        a.acct_key
    FROM
        act_acno a
    WHERE
        new.p_seqno = a.p_seqno);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACT_JCIC_LIAC_LOG_A AFTER
INSERT
    ON
    ACT_JCIC_LIAC REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE tmpVar INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 /******************************************************************************
   NAME
   PURPOSE

   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2009/4/21   JHW              1. Program initial
******************************************************************************/
SET
tmpVar = 0;--
--
 INSERT
    INTO
    act_jcic_liac_hst ( crt_date,
    crt_time,
    id_p_seqno,
    liac_status,
    liac_date,
    payment_rate,
    mod_time,
    mod_pgm,
    crt_user )
VALUES ( to_char(sysdate,
'yyyymmdd'),
to_char(sysdate,
'hh24miss'),
new.id_p_seqno,
new.liac_status,
new.liac_date,
new.payment_rate,
sysdate,
new.mod_pgm,
new.crt_user ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_ACT_JCIC_LIAC_LOG_U AFTER
UPDATE
    ON
    ACT_JCIC_LIAC REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE tmpVar INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 /******************************************************************************
   NAME
   PURPOSE

   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2009/4/21   JHW              1. Program initial
******************************************************************************/
SET
tmpVar = 0;--
--
 INSERT
    INTO
    act_jcic_liac_hst ( crt_date,
    crt_time,
    id_p_seqno,
    liac_status,
    liac_date,
    payment_rate,
    mod_time,
    mod_pgm,
    crt_user )
VALUES ( to_char(sysdate,
'yyyymmdd'),
to_char(sysdate,
'hh24miss'),
new.id_p_seqno,
new.liac_status,
new.liac_date,
new.payment_rate,
sysdate,
new.mod_pgm,
new.crt_user ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CARD_ACCT_BLOCK AFTER
UPDATE
    OF block_reason1,
    block_reason2 ,
    block_reason3,
    block_reason4,
    block_reason5 ,
    spec_status,
    spec_del_date,
    spec_remark ,
    spec_user ON
    cca_card_acct REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW
    WHEN ( ( TRIM(old.block_reason1)|| TRIM(old.block_reason2)|| TRIM(old.block_reason3) || TRIM(old.block_reason4)|| TRIM(old.block_reason5) ) <> ( TRIM(new.block_reason1)|| TRIM(new.block_reason2)|| TRIM(new.block_reason3) || TRIM(new.block_reason4)|| TRIM(new.block_reason5) )
    OR TRIM(nvl(old.spec_status, '')) <> TRIM(nvl(new.spec_status, ''))
    OR TRIM(nvl(old.spec_del_date, '')) <> TRIM(nvl(new.spec_del_date, '')) )
BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
        SQLWARNING
    BEGIN
            RETURN;--
END;--
--
 INSERT
    INTO
    cca_block_log ( log_date ,
    log_time ,
    data_type ,
    card_no ,
    acno_p_seqno ,
    acct_type ,
    block_spec_flag ,
    block_reason1 ,
    block_reason2 ,
    block_reason3 ,
    block_reason4 ,
    block_reason5 ,
    spec_status ,
    spec_del_date ,
    debit_flag ,
    card_acct_idx ,
    log_remark ,
    block_code_o ,
    spec_status_o ,
    spec_del_date_o ,
    spec_user
    --         , user_dept_no    
,
    mod_user ,
    mod_time ,
    mod_pgm )
VALUES ( to_char(sysdate,
'yyyymmdd') ,
to_char(sysdate,
'hh24miss') ,
'A' ,
'' ,
new.acno_p_seqno ,
new.acct_type ,
'B' ,
new.block_reason1 ,
new.block_reason2 ,
new.block_reason3 ,
new.block_reason4 ,
new.block_reason5 ,
new.spec_status ,
new.spec_del_date ,
new.debit_flag ,
new.card_acct_idx ,
new.spec_remark ,
old.spec_status ,
TRIM(old.block_reason1)|| TRIM(old.block_reason2) || TRIM(old.block_reason3)|| TRIM(old.block_reason4) || TRIM(old.block_reason5) ,
old.spec_del_date ,
new.spec_user
--         , new.spec_dept_no
,
new.mod_user ,
new.mod_time ,
new.mod_pgm );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CARD_BASE_SPEC AFTER
UPDATE
    OF spec_status,
    spec_del_date,
    spec_remark ,
    spec_user ON
    cca_card_base REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW
    WHEN ( TRIM(nvl(old.spec_status, '')) <> TRIM(nvl(new.spec_status, ''))
    OR TRIM(nvl(old.spec_del_date, '')) <> TRIM(nvl(new.spec_del_date, '')) )
BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
        SQLWARNING
    BEGIN
            RETURN;--
END;--
--

--    set wk_audcode = 'U';--
 INSERT
    INTO
    cca_block_log ( log_date ,
    log_time ,
    data_type ,
    card_no ,
    acno_p_seqno ,
    acct_type ,
    block_spec_flag ,
    spec_status ,
    spec_del_date ,
    debit_flag ,
    card_acct_idx ,
    log_remark ,
    spec_status_o ,
    spec_del_date_o ,
    spec_user ,
    user_dept_no ,
    mod_user ,
    mod_time ,
    mod_pgm )
VALUES ( to_char(sysdate,
'yyyymmdd') ,
to_char(sysdate,
'hh24miss') ,
'C' ,
new.card_no ,
new.acno_p_seqno ,
new.acct_type ,
'S' ,
new.spec_status ,
new.spec_del_date ,
new.debit_flag ,
new.card_acct_idx ,
new.spec_remark ,
old.spec_status ,
old.spec_del_date ,
new.spec_user ,
new.spec_dept_no ,
new.mod_user ,
new.mod_time ,
new.mod_pgm );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER "ECSCRDB"."TR_CARD_CMSMODLOG_U"
  AFTER UPDATE OF "CURRENT_CODE"
  ON "ECSCRDB"."CRD_CARD"
  REFERENCING 
    OLD AS OLD
    NEW AS NEW
  FOR EACH ROW
  WHEN ( NVL(old.current_code,
    '0')= '0'
    AND NVL(new.current_code,
    '0')<> NVL(old.current_code,
    '0') ) 
BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
        SQLWARNING
    BEGIN
            RETURN;--
END;--
--
--wk_date     CHAR(8);--
--wk_time     CHAR(6);--
--v_AUDcode   CHAR(1);--
--v_RUN       CHAR(1);--
--v_ROWS      NUMBER(5);--
 /******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.10       2009/03/13  JHW              R97-044?X??Dd?~3B2z
******************************************************************************/
--v_RUN = '0';--

--v_ROWS = 0;--
 INSERT
    INTO
    cms_modlog_card ( mod_time,
    mod_user,
    mod_pgm,
    aud_code,
    card_no,
    card_type )
VALUES ( sysdate,
new.mod_user,
new.mod_pgm,
'U',
new.card_no,
new.card_type ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CARD_HIST_A AFTER
INSERT
    ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.fee_code,
    ' ') <> nvl(new.fee_code,
    ' ')
    OR nvl(old.son_card_flag,
    ' ') <> nvl(new.son_card_flag,
    ' ')
    OR nvl(old.indiv_crd_lmt,
    0) <> nvl(new.indiv_crd_lmt,
    0)
    OR nvl(old.indiv_inst_lmt,
    0) <> nvl(new.indiv_inst_lmt,
    0) )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    crd_card_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    card_no,
    id_p_seqno,
    corp_p_seqno,
    corp_no,
    card_type,
    urgent_flag,
    group_code,
    source_code,
    sup_flag,
    son_card_flag,
    major_relation,
    major_id_p_seqno,
    major_card_no,
    member_id,
    current_code,
    force_flag,
    eng_name,
    reg_bank_no,
    unit_code,
    new_beg_date,
    new_end_date,
    issue_date,
    emergent_flag,
    oppost_reason,
    oppost_date,
    acct_type,
    p_seqno,
    acno_p_seqno,
    fee_code,
    curr_fee_code,
    stmt_cycle,
    indiv_crd_lmt,
    indiv_inst_lmt,
    crt_date,
    crt_user)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
new.mod_user,
wk_audcode,
new.card_no,
new.id_p_seqno,
new.corp_no,
new.corp_no_code,
new.card_type,
new.urgent_flag,
new.group_code,
new.source_code,
new.sup_flag,
new.son_card_flag,
new.major_relation,
new.major_id_p_seqno,
new.major_card_no,
new.member_id,
new.current_code,
new.force_flag,
new.eng_name,
new.reg_bank_no,
new.unit_code,
new.new_beg_date,
new.new_end_date,
new.issue_date,
new.emergent_flag,
new.oppost_reason,
new.oppost_date,
new.acct_type,
new.p_seqno,
new.acno_p_seqno,
new.fee_code,
new.curr_fee_code,
new.stmt_cycle,
new.indiv_crd_lmt,
new.indiv_inst_lmt,
new.crt_date,
new.crt_user);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CARD_HIST_D AFTER
DELETE
    ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.fee_code,
    ' ') <> nvl(new.fee_code,
    ' ')
    OR nvl(old.son_card_flag,
    ' ') <> nvl(new.son_card_flag,
    ' ')
    OR nvl(old.indiv_crd_lmt,
    0) <> nvl(new.indiv_crd_lmt,
    0)
    OR nvl(old.indiv_inst_lmt,
    0) <> nvl(new.indiv_inst_lmt,
    0) )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    crd_card_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    card_no,
    id_p_seqno,
    corp_p_seqno,
    corp_no,
    card_type,
    urgent_flag,
    group_code,
    source_code,
    sup_flag,
    son_card_flag,
    major_relation,
    major_id_p_seqno,
    major_card_no,
    member_id,
    current_code,
    force_flag,
    eng_name,
    reg_bank_no,
    unit_code,
    new_beg_date,
    new_end_date,
    issue_date,
    emergent_flag,
    oppost_reason,
    oppost_date,
    acct_type,
    p_seqno,
    acno_p_seqno,
    fee_code,
    curr_fee_code,
    stmt_cycle,
    indiv_crd_lmt,
    indiv_inst_lmt,
    crt_date,
    crt_user)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
old.mod_pgm,
old.mod_user,
wk_audcode,
old.card_no,
old.id_p_seqno,
old.corp_no,
old.corp_no_code,
old.card_type,
old.urgent_flag,
old.group_code,
old.source_code,
old.sup_flag,
old.son_card_flag,
old.major_relation,
old.major_id_p_seqno,
old.major_card_no,
old.member_id,
old.current_code,
old.force_flag,
old.eng_name,
old.reg_bank_no,
old.unit_code,
old.new_beg_date,
old.new_end_date,
old.issue_date,
old.emergent_flag,
old.oppost_reason,
old.oppost_date,
old.acct_type,
old.p_seqno,
old.acno_p_seqno,
old.fee_code,
old.curr_fee_code,
old.stmt_cycle,
old.indiv_crd_lmt,
old.indiv_inst_lmt,
old.crt_date,
old.crt_user);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CARD_HIST_U AFTER
UPDATE
    OF fee_code,
    son_card_flag,
    indiv_crd_lmt,
    indiv_inst_lmt ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.fee_code,
    ' ') <> nvl(new.fee_code,
    ' ')
    OR nvl(old.son_card_flag,
    ' ') <> nvl(new.son_card_flag,
    ' ')
    OR nvl(old.indiv_crd_lmt,
    0) <> nvl(new.indiv_crd_lmt,
    0)
    OR nvl(old.indiv_inst_lmt,
    0) <> nvl(new.indiv_inst_lmt,
    0) )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    crd_card_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    card_no,
    id_p_seqno,
    corp_p_seqno,
    corp_no,
    card_type,
    urgent_flag,
    group_code,
    source_code,
    sup_flag,
    son_card_flag,
    major_relation,
    major_id_p_seqno,
    major_card_no,
    member_id,
    current_code,
    force_flag,
    eng_name,
    reg_bank_no,
    unit_code,
    new_beg_date,
    new_end_date,
    issue_date,
    emergent_flag,
    oppost_reason,
    oppost_date,
    acct_type,
    p_seqno,
    acno_p_seqno,
    fee_code,
    curr_fee_code,
    stmt_cycle,
    indiv_crd_lmt,
    indiv_inst_lmt,
    crt_date,
    crt_user)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
new.mod_user,
wk_audcode,
new.card_no,
new.id_p_seqno,
new.corp_no,
new.corp_no_code,
new.card_type,
new.urgent_flag,
new.group_code,
new.source_code,
new.sup_flag,
new.son_card_flag,
new.major_relation,
new.major_id_p_seqno,
new.major_card_no,
new.member_id,
new.current_code,
new.force_flag,
new.eng_name,
new.reg_bank_no,
new.unit_code,
new.new_beg_date,
new.new_end_date,
new.issue_date,
new.emergent_flag,
new.oppost_reason,
new.oppost_date,
new.acct_type,
new.p_seqno,
new.acno_p_seqno,
new.fee_code,
new.curr_fee_code,
new.stmt_cycle,
new.indiv_crd_lmt,
new.indiv_inst_lmt,
new.crt_date,
new.crt_user);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CARD_KK1_U AFTER
UPDATE
    OF "ENG_NAME" ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.eng_name ,
    ' ') <> nvl(new.eng_name ,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    crd_card_kk1( mod_time ,
    mod_pgm ,
    mod_user ,
    mod_audcode ,
    id_p_seqno ,
    card_no ,
    eng_name ,
    post_flag )
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm ,
new.mod_user ,
wk_audcode ,
new.id_p_seqno ,
new.card_no ,
new.eng_name ,
'N' );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CMS_CRD_CARD_CHG AFTER UPDATE
    OF "ENG_NAME" ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.eng_name ,
    ' ') <> nvl(new.eng_name ,
    ' ') )
BEGIN
    DECLARE wk_audcode VARCHAR(1);--
--
--
 DECLARE wk_modtime VARCHAR(8);--
--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
RETURN;--
--
END;--
--
--
 SET
wk_audcode = 'U';--
--
--
/*******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -------------------------------------
   1.0        2020/5/21   Pino          Created this trigger.

*******************************************************************************/
 INSERT
    INTO
        cms_chgcolumn_log( 
		id_p_seqno ,
		card_no ,
		debit_flag ,
		chg_table ,
		chg_column ,
		chg_user ,
		chg_date ,
		chg_time ,
		chg_data_old ,
		chg_data ,
		mod_time ,
		mod_pgm 
		)
    VALUES ( 
	new.id_p_seqno ,
	new.card_no ,
	'N' ,
	'crd_card' ,
	'eng_name' ,
	new.mod_pgm ,
	to_char(new.mod_time,'yyyymmdd') ,
	to_char(new.mod_time,'hh24miss') ,
	old.eng_name ,
	new.eng_name ,
	to_char(sysdate,'yyyymmddhh24miss'),
	'TR_CMS_CRD_CARD_CHG'
	);--
--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CMS_DBC_CARD_CHG AFTER UPDATE
    OF "ENG_NAME" ON
    DBC_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.eng_name ,
    ' ') <> nvl(new.eng_name ,
    ' ') )
BEGIN
    DECLARE wk_audcode VARCHAR(1);--
--
--
 DECLARE wk_modtime VARCHAR(8);--
--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
RETURN;--
--
END;--
--
--
 SET
wk_audcode = 'U';--
--
--
/*******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -------------------------------------
   1.0        2020/5/21   Pino          Created this trigger.

*******************************************************************************/
 INSERT
    INTO
        cms_chgcolumn_log( 
		id_p_seqno ,
		card_no ,
		debit_flag ,
		chg_table ,
		chg_column ,
		chg_user ,
		chg_date ,
		chg_time ,
		chg_data_old ,
		chg_data ,
		mod_time ,
		mod_pgm 
		)
    VALUES ( 
	new.id_p_seqno ,
	new.card_no ,
	'Y' ,
	'dbc_card' ,
	'eng_name' ,
	new.mod_pgm ,
	to_char(new.mod_time,'yyyymmdd') ,
	to_char(new.mod_time,'hh24miss') ,
	old.eng_name ,
	new.eng_name ,
	to_char(sysdate,'yyyymmddhh24miss'),
	'TR_CMS_DBC_CARD_CHG'
	);--
--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_COL_LIAB_PARAM_LOG_A AFTER
INSERT
    ON
    COL_LIAB_PARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE ll_cnt INTEGER;--
--
 DECLARE ls_aud_flag VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 /******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2009/6/11   JHW              1. CREATE OR REPLACEd this trigger.
******************************************************************************/
SET
ls_aud_flag = '';--
--
 SET
ll_cnt = 0;--
--
 SET
ls_aud_flag = 'A';--
--
 INSERT
    INTO
    col_liab_param_log ( mod_action,
    mod_datetime,
    liab_type,
    liab_status,
    stat_unprint_flag,
    no_tel_coll_flag,
    no_delinquent_flag,
    no_collection_flag,
    no_f_stop_flag,
    revolve_rate_flag,
    no_penalty_flag,
    no_sms_flag,
    min_pay_flag,
    autopay_flag,
    pay_stage_flag,
    pay_stage_mark,
    block_flag,
    block_mark1,
    block_mark3,
    send_cs_flag,
    apr_user,
    apr_date,
    crt_user,
    crt_date,
    mod_user,
    mod_time,
    mod_pgm,
    mod_seqno,
    d_bal_flag,
    jcic_payrate_flag,
    oppost_flag,
    oppost_reason,
    noauto_balance_flag )
VALUES ( ls_aud_flag,
to_char(sysdate,
'yyyymmdd hh24miss'),
NEW.liab_type,
NEW.liab_status,
NEW.stat_unprint_flag,
NEW.no_tel_coll_flag,
NEW.no_delinquent_flag,
NEW.no_collection_flag,
NEW.no_f_stop_flag,
NEW.revolve_rate_flag,
NEW.no_penalty_flag,
NEW.no_sms_flag,
NEW.min_pay_flag,
NEW.autopay_flag,
NEW.pay_stage_flag,
NEW.pay_stage_mark,
NEW.block_flag,
NEW.block_mark1,
NEW.block_mark3,
NEW.send_cs_flag,
NEW.apr_user,
NEW.apr_date,
NEW.crt_user,
NEW.crt_date,
NEW.mod_user,
NEW.mod_time,
NEW.mod_pgm,
NEW.mod_seqno,
NEW.d_bal_flag,
NEW.jcic_payrate_flag,
NEW.oppost_flag,
NEW.oppost_reason,
NEW.noauto_balance_flag ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_COL_LIAB_PARAM_LOG_D AFTER
DELETE
    ON
    COL_LIAB_PARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE ll_cnt INTEGER;--
--
 DECLARE ls_aud_flag VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 /******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2009/6/11   JHW              1. CREATE OR REPLACEd this trigger.
******************************************************************************/
SET
ls_aud_flag = '';--
--
 SET
ll_cnt = 0;--
--
 SET
ls_aud_flag = 'D';--
--
 INSERT
    INTO
    col_liab_param_log ( mod_action,
    mod_datetime,
    liab_type,
    liab_status,
    stat_unprint_flag,
    no_tel_coll_flag,
    no_delinquent_flag,
    no_collection_flag,
    no_f_stop_flag,
    revolve_rate_flag,
    no_penalty_flag,
    no_sms_flag,
    min_pay_flag,
    autopay_flag,
    pay_stage_flag,
    pay_stage_mark,
    block_flag,
    block_mark1,
    block_mark3,
    send_cs_flag,
    apr_user,
    apr_date,
    crt_user,
    crt_date,
    mod_user,
    mod_time,
    mod_pgm,
    mod_seqno,
    d_bal_flag,
    jcic_payrate_flag,
    oppost_flag,
    oppost_reason,
    noauto_balance_flag )
VALUES ( 'D',
to_char(sysdate,
'yyyymmdd hh24miss'),
OLD.liab_type,
OLD.liab_status,
OLD.stat_unprint_flag,
OLD.no_tel_coll_flag,
OLD.no_delinquent_flag,
OLD.no_collection_flag,
OLD.no_f_stop_flag,
OLD.revolve_rate_flag,
OLD.no_penalty_flag,
OLD.no_sms_flag,
OLD.min_pay_flag,
OLD.autopay_flag,
OLD.pay_stage_flag,
OLD.pay_stage_mark,
OLD.block_flag,
OLD.block_mark1,
OLD.block_mark3,
OLD.send_cs_flag,
OLD.apr_user,
OLD.apr_date,
OLD.crt_user,
OLD.crt_date,
OLD.mod_user,
OLD.mod_time,
OLD.mod_pgm,
OLD.mod_seqno,
OLD.d_bal_flag,
OLD.jcic_payrate_flag,
OLD.oppost_flag,
OLD.oppost_reason,
OLD.noauto_balance_flag ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_COL_LIAB_PARAM_LOG_U AFTER
UPDATE
    ON
    COL_LIAB_PARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE ll_cnt INTEGER;--
--
 DECLARE ls_aud_flag VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 /******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2009/6/11   JHW              1. CREATE OR REPLACEd this trigger.
******************************************************************************/
SET
ls_aud_flag = '';--
--
 SET
ll_cnt = 0;--
--
 SET
ls_aud_flag = 'A';--
--
 SET
ls_aud_flag = 'U';--
--
 SET
ls_aud_flag = 'D';--
--
 INSERT
    INTO
    col_liab_param_log ( mod_action,
    mod_datetime,
    liab_type,
    liab_status,
    stat_unprint_flag,
    no_tel_coll_flag,
    no_delinquent_flag,
    no_collection_flag,
    no_f_stop_flag,
    revolve_rate_flag,
    no_penalty_flag,
    no_sms_flag,
    min_pay_flag,
    autopay_flag,
    pay_stage_flag,
    pay_stage_mark,
    block_flag,
    block_mark1,
    block_mark3,
    send_cs_flag,
    apr_user,
    apr_date,
    crt_user,
    crt_date,
    mod_user,
    mod_time,
    mod_pgm,
    mod_seqno,
    d_bal_flag,
    jcic_payrate_flag,
    oppost_flag,
    oppost_reason,
    noauto_balance_flag )
VALUES ( ls_aud_flag,
to_char(sysdate,
'yyyymmdd hh24miss'),
NEW.liab_type,
NEW.liab_status,
NEW.stat_unprint_flag,
NEW.no_tel_coll_flag,
NEW.no_delinquent_flag,
NEW.no_collection_flag,
NEW.no_f_stop_flag,
NEW.revolve_rate_flag,
NEW.no_penalty_flag,
NEW.no_sms_flag,
NEW.min_pay_flag,
NEW.autopay_flag,
NEW.pay_stage_flag,
NEW.pay_stage_mark,
NEW.block_flag,
NEW.block_mark1,
NEW.block_mark3,
NEW.send_cs_flag,
NEW.apr_user,
NEW.apr_date,
NEW.crt_user,
NEW.crt_date,
NEW.mod_user,
NEW.mod_time,
NEW.mod_pgm,
NEW.mod_seqno,
NEW.d_bal_flag,
NEW.jcic_payrate_flag,
NEW.oppost_flag,
NEW.oppost_reason,
NEW.noauto_balance_flag ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CORP_HIST_A AFTER
INSERT
    ON
    CRD_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    crd_corp_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    corp_p_seqno,
    corp_no,
    -- corp_no_code,
 chi_name,
    abbr_name,
    eng_name,
    reg_zip,
    reg_addr1,
    reg_addr2,
    reg_addr3,
    reg_addr4,
    reg_addr5,
    crt_date,
    crt_user,
    assure_value,
    e_mail_addr)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
new.mod_user,
wk_audcode,
new.corp_p_seqno,
new.corp_no,
-- new.corp_no_code,
 new.chi_name,
new.abbr_name,
new.eng_name,
new.reg_zip,
new.reg_addr1,
new.reg_addr2,
new.reg_addr3,
new.reg_addr4,
new.reg_addr5,
new.crt_date,
new.crt_user,
new.assure_value,
new.e_mail_addr);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CORP_HIST_D AFTER
DELETE
    ON
    CRD_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    crd_corp_hist ( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    corp_p_seqno,
    corp_no,
    --corp_no_code,
 chi_name,
    abbr_name,
    eng_name,
    reg_zip,
    reg_addr1,
    reg_addr2,
    reg_addr3,
    reg_addr4,
    reg_addr5,
    crt_date,
    crt_user,
    assure_value,
    e_mail_addr)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
old.mod_pgm,
old.mod_user,
wk_audcode,
old.corp_p_seqno,
old.corp_no,
--old.corp_no_code,
 old.chi_name,
old.abbr_name,
old.eng_name,
old.reg_zip,
old.reg_addr1,
old.reg_addr2,
old.reg_addr3,
old.reg_addr4,
old.reg_addr5,
old.crt_date,
old.crt_user,
old.assure_value,
old.e_mail_addr);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CORP_HIST_U AFTER
UPDATE
    ON
    CRD_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 SET
wk_audcode = 'A';--
--
 SET
wk_audcode = 'U';--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    crd_corp_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    corp_p_seqno,
    corp_no,
    -- corp_no_code,
 chi_name,
    abbr_name,
    eng_name,
    reg_zip,
    reg_addr1,
    reg_addr2,
    reg_addr3,
    reg_addr4,
    reg_addr5,
    crt_date,
    crt_user,
    assure_value,
    e_mail_addr)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
new.mod_user,
wk_audcode,
new.corp_p_seqno,
new.corp_no,
-- new.corp_no_code,
 new.chi_name,
new.abbr_name,
new.eng_name,
new.reg_zip,
new.reg_addr1,
new.reg_addr2,
new.reg_addr3,
new.reg_addr4,
new.reg_addr5,
new.crt_date,
new.crt_user,
new.assure_value,
new.e_mail_addr);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRDB_04_CRD_CHG AFTER UPDATE
    OF CURRENT_CODE ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(NEW.CURRENT_CODE,
    ' ') <> NVL(OLD.CURRENT_CODE,
    ' '))
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
    SQLWARNING
BEGIN
    RETURN;--
END;--

/*******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -------------------------------------
   1.0        2020/5/4   Wendy          Created this trigger.

*******************************************************************************/
IF NVL(NEW.CURRENT_CODE,
' ') <> NVL(OLD.CURRENT_CODE,
' ') THEN INSERT
    INTO
        CMS_CHGColumn_Log( ID_P_SEQNO,
        ACCT_TYPE,
        P_SEQNO,
        CARD_NO,
        DEBIT_FLAG,
        CHG_TABLE,
        CHG_COLUMN,
        CHG_USER,
        CHG_DATE,
        CHG_TIME,
        CHG_DATA_OLD,
        CHG_DATA,
        MOD_TIME,
        MOD_PGM )
    VALUES( NEW.id_p_seqno,
    NEW.acct_type,
    NEW.p_seqno,
    NEW.card_no,
    'N',
    'crd_card',
    'current_code',
    NEW.mod_pgm,
    TO_CHAR(NEW.mod_time,
    'YYYYMMDD'),
    TO_CHAR(NEW.mod_time,
    'hhmmss'),
    OLD.current_code,
    NEW.current_code,
    TO_CHAR(sysdate,
    'YYYYMMDDHH24MISS'),
    'TR_CRDB_04_CRD_CHG' );--
END IF;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRDB_04_DBC_CHG AFTER UPDATE
    OF CURRENT_CODE ON
    DBC_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(NEW.CURRENT_CODE,
    ' ') <> NVL(OLD.CURRENT_CODE,
    ' '))
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
    SQLWARNING
BEGIN
    RETURN;--
END;--

/*******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -------------------------------------
   1.0        2020/5/4   Wendy          Created this trigger.

*******************************************************************************/
IF NVL(NEW.CURRENT_CODE,
' ') <> NVL(OLD.CURRENT_CODE,
' ') THEN INSERT
    INTO
        CMS_CHGColumn_Log( ID_P_SEQNO,
        ACCT_TYPE,
        P_SEQNO,
        CARD_NO,
        DEBIT_FLAG,
        CHG_TABLE,
        CHG_COLUMN,
        CHG_USER,
        CHG_DATE,
        CHG_TIME,
        CHG_DATA_OLD,
        CHG_DATA,
        MOD_TIME,
        MOD_PGM )
    VALUES( NEW.id_p_seqno,
    NEW.acct_type,
    NEW.p_seqno,
    NEW.card_no,
    'Y',
    'dbc_card',
    'current_code',
    NEW.mod_pgm,
    TO_CHAR(NEW.mod_time,
    'YYYYMMDD'),
    TO_CHAR(NEW.mod_time,
    'hhmmss'),
    OLD.current_code,
    NEW.current_code,
    TO_CHAR(sysdate,
    'YYYYMMDDHH24MISS'),
    'TR_CRDB_04_DBC_CHG' );--
END IF;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRDB_04_PREFAB AFTER UPDATE
    OF "PREFAB_CANCEL_FLAG" ON
    DBC_EMBOSS REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.prefab_cancel_flag ,
    ' ') <> nvl(new.prefab_cancel_flag ,
    ' ')
    AND nvl(new.prefab_cancel_flag,
    'Y') )
BEGIN
    DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
RETURN;--
--
END;--
--
--
 INSERT
    INTO
        CMS_CHGColumn_Log( id_p_seqno,
        card_no,
        debit_flag,
        chg_table,
        chg_column,
        chg_user,
        chg_date,
        chg_time,
        chg_data_old,
        chg_data,
        mod_time,
        mod_pgm)
    VALUES ( '',
    new.card_no,
    'Y',
    'dbc_emboss',
    'prefab_cancel_flag',
    new.mod_pgm,
    to_char(new.mod_time,
    'yyyymmdd'),
    to_char(new.mod_time,
    'hh24miss'),
    '0',
    '1',
    to_char(sysdate,
    'yyyymmddhh24miss'),
    'TR_CRDB_04_PREFAB');--
--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRD_CARD_COL_A AFTER
INSERT
    ON
    CRD_CARD REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
    --===========================================================

        BEGIN DECLARE wk_audcode VARCHAR(1);--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    COL_CS_LOG( proc_mark,
    cs_type,
    p_seqno,
    id_p_seqno,
    card_no,
    rela_id,
    proc_type,
    mod_time )
VALUES ('0',
'3',
new.p_seqno,
decode(new.id_p_seqno,
new.major_id_p_seqno,
'0',
'1'),
decode(new.p_seqno,
new.acno_p_seqno ,
'0',
'1'),
'',
wk_audcode,
sysdate ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRD_CARD_COL_U AFTER
UPDATE
    ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.CURRENT_CODE,
    '') <> nvl(new.CURRENT_CODE,
    '') )
    --===========================================================

        BEGIN DECLARE wk_audcode VARCHAR(1);--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    COL_CS_LOG( proc_mark,
    cs_type,
    p_seqno,
    id_p_seqno,
    card_no,
    rela_id,
    proc_type,
    mod_time )
VALUES ('0',
'3',
new.p_seqno,
decode(new.id_p_seqno,
new.major_id_p_seqno,
'0',
'1'),
decode(new.p_seqno,
new.acno_p_seqno ,
'0',
'1'),
'',
wk_audcode,
sysdate ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER tr_crd_card_current_code_u
 AFTER UPDATE OF current_code
 ON CRD_CARD
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL

BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE wk_count            INTEGER;--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

   IF (new.current_code <>'0') and
      (new.current_code <>'3') and
      (old.current_code = '3') THEN
      INSERT INTO act_jcic_end_log(
                  p_seqno,
                  oppost_date,
                  current_code,
                  ori_current_code,
                  mod_pgm,
                  mod_time
                 )
          VALUES (
                  new.p_seqno,
                  new.oppost_date,
                  new.current_code,
                  old.current_code,
                  'tr_crd_card_current_code' ,
                  sysdate);--
   END IF;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRD_CORP_COL_U AFTER
UPDATE
    OF CHI_NAME,
    REG_ZIP,
    REG_ADDR1,
    REG_ADDR2,
    REG_ADDR3,
    REG_ADDR4,
    REG_ADDR5,
    CONTACT_NAME,
    CORP_TEL_ZONE1,
    CORP_TEL_NO1,
    CORP_TEL_EXT1,
    CORP_TEL_ZONE2,
    CORP_TEL_NO2,
    CORP_TEL_EXT2,
    CHARGE_NAME,
    E_MAIL_ADDR ON
    CRD_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.CHI_NAME ,
    ' ') <> nvl(new.CHI_NAME ,
    ' ')
    OR nvl(old.REG_ZIP ,
    ' ') <> nvl(new.REG_ZIP ,
    ' ')
    OR nvl(old.REG_ADDR1 ,
    ' ') <> nvl(new.REG_ADDR1 ,
    ' ')
    OR nvl(old.REG_ADDR2 ,
    ' ') <> nvl(new.REG_ADDR2 ,
    ' ')
    OR nvl(old.REG_ADDR3 ,
    ' ') <> nvl(new.REG_ADDR3 ,
    ' ')
    OR nvl(old.REG_ADDR4 ,
    ' ') <> nvl(new.REG_ADDR4 ,
    ' ')
    OR nvl(old.REG_ADDR5 ,
    ' ') <> nvl(new.REG_ADDR5 ,
    ' ')
    OR nvl(old.CONTACT_NAME ,
    ' ') <> nvl(new.CONTACT_NAME ,
    ' ')
    OR nvl(old.CORP_TEL_ZONE1 ,
    ' ') <> nvl(new.CORP_TEL_ZONE1 ,
    ' ')
    OR nvl(old.CORP_TEL_NO1 ,
    ' ') <> nvl(new.CORP_TEL_NO1 ,
    ' ')
    OR nvl(old.CORP_TEL_EXT1 ,
    ' ') <> nvl(new.CORP_TEL_EXT1 ,
    ' ')
    OR nvl(old.CORP_TEL_ZONE2 ,
    ' ') <> nvl(new.CORP_TEL_ZONE2 ,
    ' ')
    OR nvl(old.CORP_TEL_NO2 ,
    ' ') <> nvl(new.CORP_TEL_NO2 ,
    ' ')
    OR nvl(old.CORP_TEL_EXT2 ,
    ' ') <> nvl(new.CORP_TEL_EXT2 ,
    ' ')
    OR nvl(old.CHARGE_NAME ,
    ' ') <> nvl(new.CHARGE_NAME ,
    ' ')
    OR nvl(old.E_MAIL_ADDR ,
    ' ') <> nvl(new.E_MAIL_ADDR ,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    COL_CS_LOG( proc_mark,
    cs_type,
    p_seqno,
    id_p_seqno,
    card_no,
    rela_id,
    proc_type,
    mod_time )
VALUES ( '0',
'2',
old.corp_p_seqno,
old.corp_p_seqno,
'1',
'',
wk_audcode,
sysdate );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_IBM_MARKET_A
AFTER INSERT
ON CRD_IDNO
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL

BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
 
/*--
is_market_base_new char(1);--
is_market_base_old char(1);--
is_market_act_new char(1);--
is_market_act_old char(1);--
--*/
/******************************************************************************
   PURPOSE 持卡人共同行銷資料異動送IBM
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2006/2/24    JHW             1. Created this trigger.
   1.1        2010/06/23  林志鴻           除了畫面維護,異動前後相同仍要處理
   1.2        2011/10/27  林志鴻           七家子公司
   1.3        2020/07/23  Pino             欄位更名
******************************************************************************/

   DELETE FROM crd_ibm_market
    WHERE id_p_seqno = new.id_p_seqno;--

   INSERT INTO crd_ibm_market
            ( id_p_seqno,
              market_agree_base,
              market_agree_act,
              chg_date,
              chg_user,
              mod_user,
              mod_time,
              mod_pgm,
              bank_securit_flag,
              bank_prod_insur_flag,
              bank_bills_flag,
              bank_life_insur_flag,
              bank_invest_flag,
              bank_asset_flag,
              bank_venture_flag )
     VALUES ( new.id_p_seqno,
              nvl(new.market_agree_base,'N'),
              nvl(new.market_agree_act,'N'),
              to_char(sysdate,'yyyymmdd'),
              new.mod_user,
              new.mod_user,
              sysdate,
              new.mod_pgm,
              nvl(new.bank_securit_flag,'N'),
              nvl(new.bank_prod_insur_flag,'N'),
              nvl(new.bank_bills_flag,'N'),
              nvl(new.bank_life_insur_flag,'N'),
              nvl(new.bank_invest_flag,'N'),
              nvl(new.bank_asset_flag,'N'),
              nvl(new.bank_venture_flag,'N') )  ;--
-- add modify log --
   INSERT INTO crd_ibm_market_log
          ( mod_time,
            mod_user,
            id_p_seqno,
            market_agree_base,
            market_agree_act,
            bank_securit_flag,
            bank_prod_insur_flag,
            bank_bills_flag,
            bank_life_insur_flag,
            bank_invest_flag,
            bank_asset_flag,
            bank_venture_flag )
   VALUES ( sysdate,
            new.mod_user,
            new.id_p_seqno,
            nvl(new.market_agree_base,'N'),
            nvl(new.market_agree_act,'N'),
            nvl(new.bank_securit_flag,'N'),
            nvl(new.bank_prod_insur_flag,'N'),
            nvl(new.bank_bills_flag,'N'),
            nvl(new.bank_life_insur_flag,'N'),
            nvl(new.bank_invest_flag,'N'),
            nvl(new.bank_asset_flag,'N'),
            nvl(new.bank_venture_flag,'N') )  ;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_IBM_MARKET_D
AFTER INSERT OR UPDATE
OF MARKET_AGREE_BASE
  ,MARKET_AGREE_ACT
  ,BANK_SECURIT_FLAG
  ,BANK_PROD_INSUR_FLAG
  ,BANK_BILLS_FLAG
  ,BANK_LIFE_INSUR_FLAG
  ,BANK_INVEST_FLAG
  ,BANK_ASSET_FLAG
  ,BANK_VENTURE_FLAG
ON CRD_IDNO
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL

BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE
tmpVar INTEGER;--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
 
	
/*--
is_market_base_new char(1);--
is_market_base_old char(1);--
is_market_act_new char(1);--
is_market_act_old char(1);--
--*/
/******************************************************************************
   PURPOSE 持卡人共同行銷資料異動送IBM
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2006/2/24    JHW             1. Created this trigger.
   1.1        2010/06/23  林志鴻           除了畫面維護,異動前後相同仍要處理
   1.2        2011/10/27  林志鴻           七家子公司
   1.3        2020/07/23  Pino             欄位更名
******************************************************************************/

   set tmpVar = 0;--
   if UPDATING or DELETING then
/*--
    select nvl(old.market_agree_base,'N'), nvl(new.market_agree_base,'N')
         , nvl(old.market_agree_act,'N'), nvl(new.market_agree_act,'N')
   into is_market_base_old, is_market_base_new
      , is_market_act_old, is_market_act_new
   from dual ;--
--*/
      if (nvl(old.market_agree_base,'N') = nvl(new.market_agree_base,'N')) and
         (nvl(old.market_agree_act,'N') = nvl(new.market_agree_act,'N')) and
         (nvl(old.bank_securit_flag,'N') = nvl(new.bank_securit_flag,'N')) and
         (nvl(old.bank_prod_insur_flag,'N') = nvl(new.bank_prod_insur_flag,'N')) and
         (nvl(old.bank_bills_flag,'N') = nvl(new.bank_bills_flag,'N')) and
         (nvl(old.bank_life_insur_flag,'N') = nvl(new.bank_life_insur_flag,'N')) and
         (nvl(old.bank_invest_flag,'N') = nvl(new.bank_invest_flag,'N')) and
         (nvl(old.bank_asset_flag,'N') = nvl(new.bank_asset_flag,'N')) and
         (nvl(old.bank_venture_flag,'N') = nvl(new.bank_venture_flag,'N')) and
	     (substr(nvl(new.mod_pgm,'w_'),1,2) = 'w_') then
      return;--
      end if;--
   end if;--
   DELETE FROM crd_ibm_market
    WHERE id_p_seqno = new.id_p_seqno;--
     INSERT INTO crd_ibm_market
            ( id_p_seqno,
              market_agree_base,
              market_agree_act,
              chg_date,
              chg_user,
              mod_user,
              mod_time,
              mod_pgm,
              bank_securit_flag,
              bank_prod_insur_flag,
              bank_bills_flag,
              bank_life_insur_flag,
              bank_invest_flag,
              bank_asset_flag,
              bank_venture_flag )
     VALUES ( new.id_p_seqno,
              nvl(new.market_agree_base,'N'),
              nvl(new.market_agree_act,'N'),
              to_char(sysdate,'yyyymmdd'),
              new.mod_user,
              new.mod_user,
              sysdate,
              new.mod_pgm,
              nvl(new.bank_securit_flag,'N'),
              nvl(new.bank_prod_insur_flag,'N'),
              nvl(new.bank_bills_flag,'N'),
              nvl(new.bank_life_insur_flag,'N'),
              nvl(new.bank_invest_flag,'N'),
              nvl(new.bank_asset_flag,'N'),
              nvl(new.bank_venture_flag,'N') )  ;--
-- add modify log --
   INSERT INTO crd_ibm_market_log
          ( mod_time,
            mod_user,
            id_p_seqno,
            market_agree_base,
            market_agree_act,
            bank_securit_flag,
            bank_prod_insur_flag,
            bank_bills_flag,
            bank_life_insur_flag,
            bank_invest_flag,
            bank_asset_flag,
            bank_venture_flag )
   VALUES ( sysdate,
            new.mod_user,
            new.id_p_seqno,
            nvl(new.market_agree_base,'N'),
            nvl(new.market_agree_act,'N'),
            nvl(new.bank_securit_flag,'N'),
            nvl(new.bank_prod_insur_flag,'N'),
            nvl(new.bank_bills_flag,'N'),
            nvl(new.bank_life_insur_flag,'N'),
            nvl(new.bank_invest_flag,'N'),
            nvl(new.bank_asset_flag,'N'),
            nvl(new.bank_venture_flag,'N') )  ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_IBM_MARKET_U
AFTER UPDATE
OF MARKET_AGREE_BASE
  ,MARKET_AGREE_ACT
  ,BANK_SECURIT_FLAG
  ,BANK_PROD_INSUR_FLAG
  ,BANK_BILLS_FLAG
  ,BANK_LIFE_INSUR_FLAG
  ,BANK_INVEST_FLAG
  ,BANK_ASSET_FLAG
  ,BANK_VENTURE_FLAG
ON CRD_IDNO
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL

BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
 
/*--
is_market_base_new char(1);--
is_market_base_old char(1);--
is_market_act_new char(1);--
is_market_act_old char(1);--
--*/
/******************************************************************************
   PURPOSE 持卡人共同行銷資料異動送IBM
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2006/2/24    JHW             1. Created this trigger.
   1.1        2010/06/23  林志鴻           除了畫面維護,異動前後相同仍要處理
   1.2        2011/10/27  林志鴻           七家子公司
   1.3        2020/07/23  Pino             欄位更名
******************************************************************************/

   if (decode(old.market_agree_base   ,'','N',old.market_agree_base   ) = 
       decode(new.market_agree_base   ,'','N',new.market_agree_base   ) ) and
      (decode(old.market_agree_act    ,'','N',old.market_agree_act    ) = 
       decode(new.market_agree_act    ,'','N',new.market_agree_act    ) ) and
      (decode(old.bank_securit_flag   ,'','N',old.bank_securit_flag   ) = 
       decode(new.bank_securit_flag   ,'','N',new.bank_securit_flag   ) ) and
      (decode(old.bank_prod_insur_flag,'','N',old.bank_prod_insur_flag) = 
       decode(new.bank_prod_insur_flag,'','N',new.bank_prod_insur_flag) ) and
      (decode(old.bank_bills_flag     ,'','N',old.bank_bills_flag     ) = 
       decode(new.bank_bills_flag     ,'','N',new.bank_bills_flag     ) ) and
      (decode(old.bank_life_insur_flag,'','N',old.bank_life_insur_flag) = 
       decode(new.bank_life_insur_flag,'','N',new.bank_life_insur_flag) ) and
      (decode(old.bank_invest_flag    ,'','N',old.bank_invest_flag    ) = 
       decode(new.bank_invest_flag    ,'','N',new.bank_invest_flag    ) ) and
      (decode(old.bank_asset_flag     ,'','N',old.bank_asset_flag     ) = 
       decode(new.bank_asset_flag     ,'','N',new.bank_asset_flag     ) ) and
      (decode(old.bank_venture_flag   ,'','N',old.bank_venture_flag   ) = 
       decode(new.bank_venture_flag   ,'','N',new.bank_venture_flag   ) ) and
      (length(new.mod_pgm) in (0,8) ) then
      return;--
   end if;--

   DELETE FROM crd_ibm_market
    WHERE id_p_seqno = new.id_p_seqno;--

   INSERT INTO crd_ibm_market
            ( id_p_seqno,
              market_agree_base,
              market_agree_act,
              chg_date,
              chg_user,
              mod_user,
              mod_time,
              mod_pgm,
              bank_securit_flag,
              bank_prod_insur_flag,
              bank_bills_flag,
              bank_life_insur_flag,
              bank_invest_flag,
              bank_asset_flag,
              bank_venture_flag )
     VALUES ( new.id_p_seqno,
              nvl(new.market_agree_base,'N'),
              nvl(new.market_agree_act,'N'),
              to_char(sysdate,'yyyymmdd'),
              new.mod_user,
              new.mod_user,
              sysdate,
              new.mod_pgm,
              nvl(new.bank_securit_flag,'N'),
              nvl(new.bank_prod_insur_flag,'N'),
              nvl(new.bank_bills_flag,'N'),
              nvl(new.bank_life_insur_flag,'N'),
              nvl(new.bank_invest_flag,'N'),
              nvl(new.bank_asset_flag,'N'),
              nvl(new.bank_venture_flag,'N') )  ;--
-- add modify log --
   INSERT INTO crd_ibm_market_log
          ( mod_time,
            mod_user,
            id_p_seqno,
            market_agree_base,
            market_agree_act,
            bank_securit_flag,
            bank_prod_insur_flag,
            bank_bills_flag,
            bank_life_insur_flag,
            bank_invest_flag,
            bank_asset_flag,
            bank_venture_flag )
   VALUES ( sysdate,
            new.mod_user,
            new.id_p_seqno,
            nvl(new.market_agree_base,'N'),
            nvl(new.market_agree_act,'N'),
            nvl(new.bank_securit_flag,'N'),
            nvl(new.bank_prod_insur_flag,'N'),
            nvl(new.bank_bills_flag,'N'),
            nvl(new.bank_life_insur_flag,'N'),
            nvl(new.bank_invest_flag,'N'),
            nvl(new.bank_asset_flag,'N'),
            nvl(new.bank_venture_flag,'N') )  ;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRD_IDNO_COL_U AFTER
UPDATE
    OF COMPANY_NAME,
    JOB_POSITION,
    CHI_NAME,
    BIRTHDAY,
    SEX,
    CARD_SINCE,
    OFFICE_AREA_CODE1,
    OFFICE_TEL_NO1,
    OFFICE_TEL_EXT1,
    OFFICE_AREA_CODE2,
    OFFICE_TEL_NO2,
    OFFICE_TEL_EXT2,
    HOME_AREA_CODE1,
    HOME_TEL_NO1,
    HOME_TEL_EXT1,
    HOME_AREA_CODE2,
    HOME_TEL_NO2,
    HOME_TEL_EXT2,
    RESIDENT_ZIP,
    RESIDENT_ADDR1,
    RESIDENT_ADDR2,
    RESIDENT_ADDR3,
    RESIDENT_ADDR4,
    RESIDENT_ADDR5,
    CELLAR_PHONE,
    SALARY_CODE,
    SALARY_HOLDIN_FLAG,
    E_MAIL_ADDR ON
    CRD_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.COMPANY_NAME ,
    ' ') <> nvl(new.COMPANY_NAME ,
    ' ')
    OR nvl(old.JOB_POSITION ,
    ' ') <> nvl(new.JOB_POSITION ,
    ' ')
    OR nvl(old.CHI_NAME ,
    ' ') <> nvl(new.CHI_NAME ,
    ' ')
    OR nvl(old.BIRTHDAY ,
    ' ') <> nvl(new.BIRTHDAY ,
    ' ')
    OR nvl(old.SEX ,
    ' ') <> nvl(new.SEX ,
    ' ')
    OR nvl(old.CARD_SINCE ,
    ' ') <> nvl(new.CARD_SINCE ,
    ' ')
    OR nvl(old.OFFICE_AREA_CODE1 ,
    ' ') <> nvl(new.OFFICE_AREA_CODE1 ,
    ' ')
    OR nvl(old.OFFICE_TEL_NO1 ,
    ' ') <> nvl(new.OFFICE_TEL_NO1 ,
    ' ')
    OR nvl(old.OFFICE_TEL_EXT1 ,
    ' ') <> nvl(new.OFFICE_TEL_EXT1 ,
    ' ')
    OR nvl(old.OFFICE_AREA_CODE2 ,
    ' ') <> nvl(new.OFFICE_AREA_CODE2 ,
    ' ')
    OR nvl(old.OFFICE_TEL_NO2 ,
    ' ') <> nvl(new.OFFICE_TEL_NO2 ,
    ' ')
    OR nvl(old.OFFICE_TEL_EXT2 ,
    ' ') <> nvl(new.OFFICE_TEL_EXT2 ,
    ' ')
    OR nvl(old.HOME_AREA_CODE1 ,
    ' ') <> nvl(new.HOME_AREA_CODE1 ,
    ' ')
    OR nvl(old.HOME_TEL_NO1 ,
    ' ') <> nvl(new.HOME_TEL_NO1 ,
    ' ')
    OR nvl(old.HOME_TEL_EXT1 ,
    ' ') <> nvl(new.HOME_TEL_EXT1 ,
    ' ')
    OR nvl(old.HOME_AREA_CODE2 ,
    ' ') <> nvl(new.HOME_AREA_CODE2 ,
    ' ')
    OR nvl(old.HOME_TEL_NO2 ,
    ' ') <> nvl(new.HOME_TEL_NO2 ,
    ' ')
    OR nvl(old.HOME_TEL_EXT2 ,
    ' ') <> nvl(new.HOME_TEL_EXT2 ,
    ' ')
    OR nvl(old.RESIDENT_ZIP ,
    ' ') <> nvl(new.RESIDENT_ZIP ,
    ' ')
    OR nvl(old.RESIDENT_ADDR1 ,
    ' ') <> nvl(new.RESIDENT_ADDR1 ,
    ' ')
    OR nvl(old.RESIDENT_ADDR2 ,
    ' ') <> nvl(new.RESIDENT_ADDR2 ,
    ' ')
    OR nvl(old.RESIDENT_ADDR3 ,
    ' ') <> nvl(new.RESIDENT_ADDR3 ,
    ' ')
    OR nvl(old.RESIDENT_ADDR4 ,
    ' ') <> nvl(new.RESIDENT_ADDR4 ,
    ' ')
    OR nvl(old.RESIDENT_ADDR5 ,
    ' ') <> nvl(new.RESIDENT_ADDR5 ,
    ' ')
    OR nvl(old.CELLAR_PHONE ,
    ' ') <> nvl(new.CELLAR_PHONE ,
    ' ')
    OR nvl(old.E_MAIL_ADDR ,
    ' ') <> nvl(new.E_MAIL_ADDR ,
    ' ')
    OR nvl(old.SALARY_CODE ,
    ' ') <> nvl(new.SALARY_CODE ,
    ' ')
    OR nvl(old.SALARY_HOLDIN_FLAG ,
    ' ') <> nvl(new.SALARY_HOLDIN_FLAG ,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    COL_CS_LOG( proc_mark,
    cs_type,
    p_seqno,
    id_p_seqno,
    card_no,
    rela_id,
    proc_type,
    mod_time )
VALUES ('0',
'0',
old.id_p_seqno,
old.id_p_seqno,
'1',
'',
wk_audcode,
sysdate ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRD_LOST_FEELOG_U AFTER
UPDATE
    OF LOST_FEE_CODE ON
    CRD_CARD REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
    WHEN ( 1 = 1
    AND decode(old.lost_fee_code,
    '',
    'N',
    old.lost_fee_code) = 'N'
    AND decode(new.lost_fee_code,
    '',
    'N',
    new.lost_fee_code) <> decode(old.lost_fee_code,
    '',
    'N' ,
    old.lost_fee_code) )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 INSERT
    INTO
    crd_lost_feelog ( acno_p_seqno,
    id_p_seqno,
    acct_type,
    crt_date,
    sup_flag,
    lost_fee_code,
    card_no,
    card_type,
    issue_date,
    oppost_date,
    mod_user,
    mod_pgm,
    mod_time )
VALUES (new.acno_p_seqno,
new.id_p_seqno,
new.acct_type,
to_char(sysdate,
'yyyymmdd'),
new.sup_flag,
new.lost_fee_code,
new.card_no,
new.card_type,
new.issue_date,
new.oppost_date,
new.mod_user,
new.mod_pgm,
sysdate );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRD_RELA_COL_A AFTER
INSERT
    ON
    CRD_RELA REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    COL_CS_LOG( proc_mark,
    cs_type,
    p_seqno,
    id_p_seqno,
    card_no,
    rela_id,
    proc_type,
    mod_time )
VALUES ( '0',
'5',
decode(wk_audcode,
'A',
new.id_p_seqno,
old.id_p_seqno),
decode(wk_audcode,
'A',
new.id_p_seqno,
old.id_p_seqno),
'1',
'',
wk_audcode,
sysdate ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRD_RELA_COL_D AFTER
DELETE
    ON
    CRD_RELA REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    COL_CS_LOG( proc_mark,
    cs_type,
    p_seqno,
    id_p_seqno,
    card_no,
    rela_id,
    proc_type,
    mod_time )
VALUES ( '0',
'5',
decode(wk_audcode,
'A',
new.id_p_seqno,
old.id_p_seqno),
decode(wk_audcode,
'A',
new.id_p_seqno,
old.id_p_seqno),
'1',
'',
wk_audcode,
sysdate ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRD_RELA_COL_U AFTER
UPDATE
    OF RELA_ID,
    RELA_NAME,
    COMPANY_NAME,
    COMPANY_ZIP,
    COMPANY_ADDR1,
    COMPANY_ADDR2,
    COMPANY_ADDR3,
    COMPANY_ADDR4,
    COMPANY_ADDR5,
    OFFICE_AREA_CODE1,
    OFFICE_TEL_NO1,
    OFFICE_TEL_EXT1,
    OFFICE_AREA_CODE2,
    OFFICE_TEL_NO2,
    OFFICE_TEL_EXT2,
    HOME_AREA_CODE1,
    HOME_TEL_NO1,
    HOME_TEL_EXT1,
    HOME_AREA_CODE2,
    HOME_TEL_NO2,
    HOME_TEL_EXT2,
    RESIDENT_ZIP,
    RESIDENT_ADDR1,
    RESIDENT_ADDR2,
    RESIDENT_ADDR3,
    RESIDENT_ADDR4,
    RESIDENT_ADDR5,
    CELLAR_PHONE ON
    CRD_RELA REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( 1 = 1
    AND ( old.RELA_TYPE = '1'
    OR new.RELA_TYPE = '1' )
    AND ( nvl(old.RELA_ID ,
    ' ') <> nvl(new.RELA_ID ,
    ' ')
    OR nvl(old.RELA_NAME ,
    ' ') <> nvl(new.RELA_NAME ,
    ' ')
    OR nvl(old.COMPANY_NAME ,
    ' ') <> nvl(new.COMPANY_NAME ,
    ' ')
    OR nvl(old.COMPANY_ZIP ,
    ' ') <> nvl(new.COMPANY_ZIP ,
    ' ')
    OR nvl(old.COMPANY_ADDR1 ,
    ' ') <> nvl(new.COMPANY_ADDR1 ,
    ' ')
    OR nvl(old.COMPANY_ADDR2 ,
    ' ') <> nvl(new.COMPANY_ADDR2 ,
    ' ')
    OR nvl(old.COMPANY_ADDR3 ,
    ' ') <> nvl(new.COMPANY_ADDR3 ,
    ' ')
    OR nvl(old.COMPANY_ADDR4 ,
    ' ') <> nvl(new.COMPANY_ADDR4 ,
    ' ')
    OR nvl(old.COMPANY_ADDR5 ,
    ' ') <> nvl(new.COMPANY_ADDR5 ,
    ' ')
    OR nvl(old.OFFICE_AREA_CODE1 ,
    ' ') <> nvl(new.OFFICE_AREA_CODE1 ,
    ' ')
    OR nvl(old.OFFICE_TEL_NO1 ,
    ' ') <> nvl(new.OFFICE_TEL_NO1 ,
    ' ')
    OR nvl(old.OFFICE_TEL_EXT1 ,
    ' ') <> nvl(new.OFFICE_TEL_EXT1 ,
    ' ')
    OR nvl(old.OFFICE_AREA_CODE2 ,
    ' ') <> nvl(new.OFFICE_AREA_CODE2 ,
    ' ')
    OR nvl(old.OFFICE_TEL_NO2 ,
    ' ') <> nvl(new.OFFICE_TEL_NO2 ,
    ' ')
    OR nvl(old.OFFICE_TEL_EXT2 ,
    ' ') <> nvl(new.OFFICE_TEL_EXT2 ,
    ' ')
    OR nvl(old.HOME_AREA_CODE1 ,
    ' ') <> nvl(new.HOME_AREA_CODE1 ,
    ' ')
    OR nvl(old.HOME_TEL_NO1 ,
    ' ') <> nvl(new.HOME_TEL_NO1 ,
    ' ')
    OR nvl(old.HOME_TEL_EXT1 ,
    ' ') <> nvl(new.HOME_TEL_EXT1 ,
    ' ')
    OR nvl(old.HOME_AREA_CODE2 ,
    ' ') <> nvl(new.HOME_AREA_CODE2 ,
    ' ')
    OR nvl(old.HOME_TEL_NO2 ,
    ' ') <> nvl(new.HOME_TEL_NO2 ,
    ' ')
    OR nvl(old.HOME_TEL_EXT2 ,
    ' ') <> nvl(new.HOME_TEL_EXT2 ,
    ' ')
    OR nvl(old.RESIDENT_ZIP ,
    ' ') <> nvl(new.RESIDENT_ZIP ,
    ' ')
    OR nvl(old.RESIDENT_ADDR1 ,
    ' ') <> nvl(new.RESIDENT_ADDR1 ,
    ' ')
    OR nvl(old.RESIDENT_ADDR2 ,
    ' ') <> nvl(new.RESIDENT_ADDR2 ,
    ' ')
    OR nvl(old.RESIDENT_ADDR3 ,
    ' ') <> nvl(new.RESIDENT_ADDR3 ,
    ' ')
    OR nvl(old.RESIDENT_ADDR4 ,
    ' ') <> nvl(new.RESIDENT_ADDR4 ,
    ' ')
    OR nvl(old.RESIDENT_ADDR5 ,
    ' ') <> nvl(new.RESIDENT_ADDR5 ,
    ' ')
    OR nvl(old.CELLAR_PHONE ,
    ' ') <> nvl(new.CELLAR_PHONE ,
    ' ') ) )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    COL_CS_LOG( proc_mark,
    cs_type,
    p_seqno,
    id_p_seqno,
    card_no,
    rela_id,
    proc_type,
    mod_time )
VALUES ( '0',
'5',
decode(wk_audcode,
'A',
new.id_p_seqno,
old.id_p_seqno),
decode(wk_audcode,
'A',
new.id_p_seqno,
old.id_p_seqno),
'1',
'',
wk_audcode,
sysdate ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_RETURN_A
 BEFORE INSERT 
 ON CRD_RETURN
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW  MODE DB2SQL

BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

  INSERT INTO crd_return_log
         ( mod_date,
           mod_time,
           mod_user,
           card_no,
           return_date,
           group_code,
           id_p_seqno,
           ic_flag,
           reason_code,
           return_type,
           mail_type,
           mail_branch,
           zip_code,
           mail_addr1,
           mail_addr2,
           mail_addr3,
           mail_addr4,
           mail_addr5,
           beg_date,
           end_date,
           return_note,
           proc_status,
           package_flag,
           package_date, 
           return_seqno, 
           mail_date,
           mail_no, 
           barcode_num )
  VALUES ( to_char(sysdate,'yyyymmdd'),
           to_char(sysdate,'hh24miss'),
           new.mod_user,
           new.card_no,
           new.return_date,
           new.group_code,
           new.id_p_seqno,
           new.ic_flag,
           new.reason_code,
           new.return_type,
           new.mail_type,
           new.mail_branch,
           new.zip_code,
           new.mail_addr1,
           new.mail_addr2,
           new.mail_addr3,
           new.mail_addr4,
           new.mail_addr5,
           new.beg_date,
           new.end_date,
           new.return_note,
           new.proc_status,
           new.package_flag,
           new.package_date, 
           new.return_seqno,
           new.mail_date, 
           new.mail_no, 
           new.barcode_num);--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_RETURN_CHG_A
 AFTER INSERT
 ON CRD_RETURN
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL

	
BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
 
/******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2014/8/28   James Hung       Created this trigger.

******************************************************************************/

   IF (new.proc_status = '2') THEN
   update crd_card
      set expire_chg_flag ='3',
          expire_reason   = 'Y1',
          expire_chg_date = to_char(sysdate,'yyyymmdd')
    where card_no         = new.card_no;--
   END IF;--

   IF (new.proc_status = '5') THEN
   update crd_card
      set expire_chg_flag = '',
          expire_reason   = '',
          expire_chg_date = ''
    where card_no         = new.card_no;--
   END IF;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_RETURN_CHG_U
 AFTER UPDATE OF
 proc_status
 ON CRD_RETURN
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL

	
WHEN (
       NVL(new.proc_status,' ') <>  NVL(old.proc_status,' ')
     )

BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
 
/******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2014/8/28   James Hung       Created this trigger.

******************************************************************************/

   IF (new.proc_status = '2') THEN
   update crd_card
      set expire_chg_flag ='3',
          expire_reason   = 'Y1',
          expire_chg_date = to_char(sysdate,'yyyymmdd')
    where card_no         = new.card_no;--
   END IF;--

   IF (new.proc_status = '5') THEN
   update crd_card
      set expire_chg_flag = '',
          expire_reason   = '',
          expire_chg_date = ''
    where card_no         = new.card_no;--
   END IF;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_RETURN_PP_A
 BEFORE INSERT 
ON CRD_RETURN_PP
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL

BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
 
  INSERT INTO crd_return_pp_log
         ( mod_date,
           mod_time,
           mod_user,
           pp_card_no,
           return_date,
           group_code,
           id_p_seqno,
           reason_code,
           return_type,
           mail_type,
           mail_branch,
           zip_code,
           mail_addr1,
           mail_addr2,
           mail_addr3,
           mail_addr4,
           mail_addr5,
           beg_date,
           end_date,
           return_note,
           proc_status,
           package_flag,
           package_date, 
           return_seqno, 
--           mail_date,
           mail_no, 
           barcode_num )
  VALUES ( to_char(sysdate,'yyyymmdd'),
           to_char(sysdate,'hh24miss'),
           new.mod_user,
           new.pp_card_no,
           new.return_date,
           new.group_code,
           new.id_p_seqno,
           new.reason_code,
           new.return_type,
           new.mail_type,
           new.mail_branch,
           new.zip_code,
           new.mail_addr1,
           new.mail_addr2,
           new.mail_addr3,
           new.mail_addr4,
           new.mail_addr5,
           new.beg_date,
           new.end_date,
           new.return_note,
           new.proc_status,
           new.package_flag,
           new.package_date, 
           new.return_seqno,
--           new.mail_date, 
           new.mail_no, 
           new.barcode_num )  ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_RETURN_PP_U
 BEFORE UPDATE OF
 pp_card_no,
 return_date,
 reason_code,
 return_type,
 mail_type,
 mail_branch,
 zip_code,
 mail_addr1,
 mail_addr2,
 mail_addr3,
 mail_addr4,
 mail_addr5,
 beg_date,
 end_date,
 proc_status,
 return_note,
 return_seqno, 
 mail_date, 
 -- mail_no, 
 barcode_num
ON CRD_RETURN_PP
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL

WHEN (
        NVL(new.pp_card_no,' ')  <> NVL(old.pp_card_no,' ')  or
        NVL(new.return_date,' ') <> NVL(old.return_date,' ') or
        NVL(new.reason_code,' ') <> NVL(old.reason_code,' ') or
        NVL(new.return_type,' ') <> NVL(old.return_type,' ') or
        NVL(new.mail_type,' '  ) <> NVL(old.mail_type,' ')   or
        NVL(new.mail_branch,' ') <> NVL(old.mail_branch,' ') or
        NVL(new.zip_code,' ')    <> NVL(old.zip_code,' ')    or
        NVL(new.mail_addr1,' ')  <> NVL(old.mail_addr1,' ')  or
        NVL(new.mail_addr2,' ')  <> NVL(old.mail_addr2,' ')  or
        NVL(new.mail_addr3,' ')  <> NVL(old.mail_addr3,' ')  or
        NVL(new.mail_addr4,' ')  <> NVL(old.mail_addr4,' ')  or
        NVL(new.mail_addr5,' ')  <> NVL(old.mail_addr5,' ')  or
        NVL(new.beg_date,' ')    <> NVL(old.beg_date,' ')    or
        NVL(new.end_date,' ')    <> NVL(old.end_date,' ')    or
        NVL(new.proc_status,' ') <> NVL(old.proc_status,' ') or
        NVL(new.return_note,' ') <> NVL(old.return_note,' ') or
        NVL(new.return_seqno,' ') <> NVL(old.return_seqno,' ') or
--        NVL(new.mail_date,' ') <> NVL(old.mail_date,' ') or
        NVL(new.mail_no,' ') <> NVL(old.mail_no,' ') or
        NVL(new.barcode_num,' ') <> NVL(old.barcode_num,' ')
      )
BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
 
  INSERT INTO crd_return_pp_log
         ( mod_date,
           mod_time,
           mod_user,
           pp_card_no,
           return_date,
           group_code,
           id_p_seqno,
           reason_code,
           return_type,
           mail_type,
           mail_branch,
           zip_code,
           mail_addr1,
           mail_addr2,
           mail_addr3,
           mail_addr4,
           mail_addr5,
           beg_date,
           end_date,
           return_note,
           proc_status,
           package_flag,
           package_date,
           return_seqno, 
--           mail_date, 
           mail_no, 
           barcode_num)
  VALUES ( to_char(sysdate,'yyyymmdd'),
           to_char(sysdate,'hh24miss'),
           new.mod_user,
           new.pp_card_no,
           new.return_date,
           new.group_code,
           new.id_p_seqno,
           new.reason_code,
           new.return_type,
           new.mail_type,
           new.mail_branch,
           new.zip_code,
           new.mail_addr1,
           new.mail_addr2,
           new.mail_addr3,
           new.mail_addr4,
           new.mail_addr5,
           new.beg_date,
           new.end_date,
           new.return_note,
           new.proc_status,
           new.package_flag,
           new.package_date, 
           new.return_seqno, 
--           new.mail_date, 
           new.mail_no, 
           new.barcode_num )  ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_RETURN_U
 BEFORE UPDATE OF
 card_no,
 return_date,
 ic_flag,
 reason_code,
 return_type,
 mail_type,
 mail_branch,
 zip_code,
 mail_addr1,
 mail_addr2,
 mail_addr3,
 mail_addr4,
 mail_addr5,
 beg_date,
 end_date,
 proc_status,
 return_note,
 return_seqno, 
 mail_date, 
 mail_no, 
 barcode_num
 ON CRD_RETURN
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW  MODE DB2SQL

WHEN (
        NVL(new.card_no,' ') <> NVL(old.card_no,' ') or
        NVL(new.return_date,' ') <> NVL(old.return_date,' ') or
        NVL(new.ic_flag,' ') <> NVL(old.ic_flag,' ') or
        NVL(new.reason_code,' ') <> NVL(old.reason_code,' ') or
        NVL(new.return_type,' ') <> NVL(old.return_type,' ') or
        NVL(new.mail_type,' ') <> NVL(old.mail_type,' ') or
        NVL(new.mail_branch,' ') <> NVL(old.mail_branch,' ') or
        NVL(new.zip_code,' ') <> NVL(old.zip_code,' ') or
        NVL(new.mail_addr1,' ') <> NVL(old.mail_addr1,' ') or
        NVL(new.mail_addr2,' ') <> NVL(old.mail_addr2,' ') or
        NVL(new.mail_addr3,' ') <> NVL(old.mail_addr3,' ') or
        NVL(new.mail_addr4,' ') <> NVL(old.mail_addr4,' ') or
        NVL(new.mail_addr5,' ') <> NVL(old.mail_addr5,' ') or
        NVL(new.beg_date,' ') <> NVL(old.beg_date,' ') or
        NVL(new.end_date,' ') <> NVL(old.end_date,' ') or
        NVL(new.proc_status,' ') <> NVL(old.proc_status,' ') or
        NVL(new.return_note,' ') <> NVL(old.return_note,' ') or
        NVL(new.return_seqno,' ') <> NVL(old.return_seqno,' ') or
        NVL(new.mail_date,' ') <> NVL(old.mail_date,' ') or
        NVL(new.mail_no,' ') <> NVL(old.mail_no,' ') or
        NVL(new.barcode_num,' ') <> NVL(old.barcode_num,' ')
      )
BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

  INSERT INTO crd_return_log
         ( mod_date,
           mod_time,
           mod_user,
           card_no,
           return_date,
           group_code,
           id_p_seqno,
           ic_flag,
           reason_code,
           return_type,
           mail_type,
           mail_branch,
           zip_code,
           mail_addr1,
           mail_addr2,
           mail_addr3,
           mail_addr4,
           mail_addr5,
           beg_date,
           end_date,
           return_note,
           proc_status,
           package_flag,
           package_date,
           return_seqno, 
           mail_date, 
           mail_no, 
           barcode_num )
  VALUES ( to_char(sysdate,'yyyymmdd'),
           to_char(sysdate,'hh24miss'),
           new.mod_user,
           new.card_no,
           new.return_date,
           new.group_code,
           new.id_p_seqno,
           new.ic_flag,
           new.reason_code,
           new.return_type,
           new.mail_type,
           new.mail_branch,
           new.zip_code,
           new.mail_addr1,
           new.mail_addr2,
           new.mail_addr3,
           new.mail_addr4,
           new.mail_addr5,
           new.beg_date,
           new.end_date,
           new.return_note,
           new.proc_status,
           new.package_flag,
           new.package_date, 
           new.return_seqno, 
           new.mail_date, 
           new.mail_no, 
           new.barcode_num );--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CRD_WH_HST_U AFTER
UPDATE
    OF bill_sending_zip,
    bill_sending_addr1,
    bill_sending_addr2,
    bill_sending_addr3,
    bill_sending_addr4,
    bill_sending_addr5 ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( (NVL(OLD.bill_sending_zip,
    ' ') <> NVL(NEW.bill_sending_zip,
    ' ')
    OR NVL(OLD.bill_sending_addr1,
    ' ') <> NVL(NEW.bill_sending_addr1,
    ' ')
    OR NVL(OLD.bill_sending_addr2,
    ' ') <> NVL(NEW.bill_sending_addr2,
    ' ')
    OR NVL(OLD.bill_sending_addr3,
    ' ') <> NVL(NEW.bill_sending_addr3,
    ' ')
    OR NVL(OLD.bill_sending_addr4,
    ' ') <> NVL(NEW.bill_sending_addr4,
    ' ')
    OR NVL(OLD.bill_sending_addr5,
    ' ') <> NVL(NEW.bill_sending_addr5,
    ' '))
    AND new.acct_type NOT IN ('02',
    '03',
    '05',
    '06') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--

-- select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
 INSERT
    INTO
    CRD_WH_HST( ID_p_seqno,
    mod_time,
    mod_pgm )
VALUES ( NEW.id_p_seqno,
TO_CHAR(SYSDATE,
'yyyymmddhh24miss'),
NEW.mod_pgm );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CTI_act_acno_a AFTER
INSERT
    ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.BILL_SENDING_ZIP,
    ' ') <> NVL(old.BILL_SENDING_ZIP,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR1,
    ' ') <> NVL(old.BILL_SENDING_ADDR1,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR2,
    ' ') <> NVL(old.BILL_SENDING_ADDR2,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR3,
    ' ') <> NVL(old.BILL_SENDING_ADDR3,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR4,
    ' ') <> NVL(old.BILL_SENDING_ADDR4,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR5,
    ' ') <> NVL(old.BILL_SENDING_ADDR5,
    ' ')
    OR NVL(new.VIP_CODE ,
    ' ') <> NVL(old.VIP_CODE,
    ' ')
    OR NVL(new.BANK_REL_FLAG,
    ' ') <> NVL(old.BANK_REL_FLAG,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'I';--
--
 INSERT
    INTO
    cti_txn_log( id_p_seqno,
    proc_type,
    proc_flag,
    mod_time )
VALUES ( new.id_p_seqno,
-- id -> id_p_seqno
 wk_audcode,
'N',
sysdate );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CTI_act_acno_u AFTER
UPDATE
    OF BILL_SENDING_ZIP,
    BILL_SENDING_ADDR1,
    BILL_SENDING_ADDR2,
    BILL_SENDING_ADDR3,
    BILL_SENDING_ADDR4,
    BILL_SENDING_ADDR5,
    VIP_CODE,
    BANK_REL_FLAG ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.BILL_SENDING_ZIP,
    ' ') <> NVL(old.BILL_SENDING_ZIP,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR1,
    ' ') <> NVL(old.BILL_SENDING_ADDR1,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR2,
    ' ') <> NVL(old.BILL_SENDING_ADDR2,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR3,
    ' ') <> NVL(old.BILL_SENDING_ADDR3,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR4,
    ' ') <> NVL(old.BILL_SENDING_ADDR4,
    ' ')
    OR NVL(new.BILL_SENDING_ADDR5,
    ' ') <> NVL(old.BILL_SENDING_ADDR5,
    ' ')
    OR NVL(new.VIP_CODE ,
    ' ') <> NVL(old.VIP_CODE,
    ' ')
    OR NVL(new.BANK_REL_FLAG,
    ' ') <> NVL(old.BANK_REL_FLAG,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    cti_txn_log( id_p_seqno,
    proc_type,
    proc_flag,
    mod_time )
VALUES ( new.id_p_seqno,
-- id -> id_p_seqno
 wk_audcode,
'N',
sysdate );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CTI_crd_card_a AFTER
INSERT
    ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    cti_txn_log( id_p_seqno,
    proc_type,
    proc_flag,
    mod_time )
VALUES ( new.id_p_seqno,
wk_audcode,
'N',
sysdate );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CTI_crd_card_U AFTER
UPDATE
    OF CURRENT_CODE,
    OPPOST_DATE,
    NEW_END_DATE ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.CURRENT_CODE,
    ' ') <> NVL(old.CURRENT_CODE,
    ' ')
    OR NVL(new.OPPOST_DATE,
    ' ') <> NVL(old.OPPOST_DATE,
    ' ')
    OR NVL(new.NEW_END_DATE,
    ' ') <> NVL(old.NEW_END_DATE,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    cti_txn_log( id_p_seqno,
    proc_type,
    proc_flag,
    mod_time )
VALUES ( new.id_p_seqno,
wk_audcode,
'N',
sysdate );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CTI_crd_idno_A AFTER
INSERT
    ON
    CRD_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'I';--
--
 INSERT
    INTO
    cti_txn_log( id_p_seqno,
    proc_type,
    proc_flag,
    mod_time )
VALUES ( new.id_p_seqno,
wk_audcode,
'N',
sysdate );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CTI_crd_idno_U AFTER
UPDATE
    OF CHI_NAME,
    COMPANY_NAME,
    JOB_POSITION,
    HOME_AREA_CODE2,
    HOME_TEL_NO2,
    HOME_TEL_EXT2,
    HOME_AREA_CODE1,
    HOME_TEL_NO1,
    HOME_TEL_EXT1,
    OFFICE_AREA_CODE2,
    OFFICE_TEL_NO2,
    OFFICE_TEL_EXT2,
    OFFICE_AREA_CODE1,
    OFFICE_TEL_NO1,
    OFFICE_TEL_EXT1,
    CELLAR_PHONE,
    SPECIAL_CODE ON
    CRD_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.CELLAR_PHONE,
    ' ') <> NVL(old.CELLAR_PHONE,
    ' ')
    OR NVL(new.COMPANY_NAME,
    ' ') <> NVL(old.COMPANY_NAME,
    ' ')
    OR NVL(new.CHI_NAME,
    ' ') <> NVL(old.CHI_NAME,
    ' ')
    OR NVL(new.HOME_AREA_CODE1,
    ' ') <> NVL(old.HOME_AREA_CODE1,
    ' ')
    OR NVL(new.HOME_AREA_CODE2,
    ' ') <> NVL(old.HOME_AREA_CODE2,
    ' ')
    OR NVL(new.HOME_TEL_EXT1,
    ' ') <> NVL(old.HOME_TEL_EXT1,
    ' ')
    OR NVL(new.HOME_TEL_EXT2,
    ' ') <> NVL(old.HOME_TEL_EXT2,
    ' ')
    OR NVL(new.HOME_TEL_NO1,
    ' ') <> NVL(old.HOME_TEL_NO1,
    ' ')
    OR NVL(new.HOME_TEL_NO2,
    ' ') <> NVL(old.HOME_TEL_NO2,
    ' ')
    OR NVL(new.SPECIAL_CODE,
    ' ') <> NVL(old.SPECIAL_CODE,
    ' ')
    OR NVL(new.JOB_POSITION,
    ' ') <> NVL(old.JOB_POSITION,
    ' ')
    OR NVL(new.OFFICE_AREA_CODE1,
    ' ') <> NVL(old.OFFICE_AREA_CODE1,
    ' ')
    OR NVL(new.OFFICE_AREA_CODE2,
    ' ') <> NVL(old.OFFICE_AREA_CODE2,
    ' ')
    OR NVL(new.OFFICE_TEL_EXT1,
    ' ') <> NVL(old.OFFICE_TEL_EXT1,
    ' ')
    OR NVL(new.OFFICE_TEL_EXT2,
    ' ') <> NVL(old.OFFICE_TEL_EXT2,
    ' ')
    OR NVL(new.OFFICE_TEL_NO1,
    ' ') <> NVL(old.OFFICE_TEL_NO1,
    ' ')
    OR NVL(new.OFFICE_TEL_NO2,
    ' ') <> NVL(old.OFFICE_TEL_NO2,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    cti_txn_log( id_p_seqno,
    proc_type,
    proc_flag,
    mod_time )
VALUES ( new.id_p_seqno,
wk_audcode,
'N',
sysdate );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER "ECSCRDB"."TR_CURRENT_CODE_U"
  AFTER UPDATE OF "CURRENT_CODE"
  ON "ECSCRDB"."CRD_CARD"
  REFERENCING 
    OLD AS OLD
    NEW AS NEW
  FOR EACH ROW
  WHEN (
      nvl(old.current_code,0) <> nvl(new.current_code,0)
                                              ) 
BEGIN
 DECLARE  wk_modtime   VARCHAR(8);--
 DECLARE  wk_v_card_no VARCHAR(19);--hce_card.v_card_no%TYPE;--
 DECLARE  wk_act_code  VARCHAR(1);--
 DECLARE  wk_reason_code VARCHAR(10);--
-- wk_v_card_no VARCHAR(19);--
 DECLARE  xcnt INTEGER;--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

   if nvl(new.current_code,0) = '3' then
      update act_acno
         set deposit_flag = 'Y'
       where p_seqno      = new.p_seqno;--
   end if;--

-- current_code   1  2  3 4 5 
-- operation     01 02 03

  begin
   select act_code,reason_code
     into wk_act_code,wk_reason_code
     from cca_outgoing
    where card_no      = new.card_no
      and key_value = 'NCCC'
      and crt_date = to_char(sysdate,'yyyymmdd')
      order by crt_time DESC
      fetch first 1 rows only;--
   
  end;--

  set xcnt  = 1;--
  begin
   select v_card_no
     into wk_v_card_no
     from hce_card
    where card_no      = new.card_no
      and status_code in ('0')
      fetch first 1 rows only;--
   
  end;--

  if(xcnt  = 1) then
   if nvl(old.current_code,0) = '0' then
      insert into hce_life_cycle
                      (card_no,
                       v_card_no,
                       id_p_seqno,
                       current_code,
                       oppost_date,
                       operation,
                       proc_flag,
                       mod_time,
                       mod_pgm)
              values( new.card_no
                    ,  wk_v_card_no
                    , new.id_p_seqno
                    , new.current_code
                    , new.oppost_date
                    , decode(new.current_code,'2','01','03')
                    , 'N'
                    , sysdate
                    , 'tr_current_code');--
   end if;--
   if nvl(old.current_code,0) = '2' and nvl(new.current_code,0) = '0' then
      insert into hce_life_cycle
                      (card_no,
                       v_card_no,
                       id_p_seqno,
                       current_code,
                       oppost_date,
                       operation,
                       proc_flag,
                       mod_time,
                       mod_pgm)
              values( new.card_no
                    ,  wk_v_card_no
                    , new.id_p_seqno
                    , new.current_code
                    , new.oppost_date
                    , '02'
                    , 'N'
                    , sysdate
                    , 'tr_current_code');--
   end if;--
   
   if nvl(new.current_code ,' ') <> nvl(old.current_code ,' ') then
       update hce_card set status_code = '3'
       ,change_date = to_char(sysdate,'yyyymmdd')
       ,mod_time = sysdate
       ,mod_pgm = 'tr_current_code'
       where v_card_no = wk_v_card_no;--
       
       INSERT INTO
        cca_outgoing( 
		crt_date ,
		crt_time ,
		card_no ,
		key_value ,
		key_table ,
		bitmap ,
		act_code ,
		crt_user ,
		proc_flag ,
		send_times ,
		data_from ,
		data_type ,
		bin_type ,
		vip_amt ,
		mod_time ,
        mod_pgm ,
		electronic_card_no ,
		current_code ,
		new_end_date ,
		oppost_date ,
		oppost_reason,
        reason_code
		)
    VALUES ( 
	to_char(sysdate,'yyyymmdd'),
    to_char(new.mod_time,'hh24miss'),
	wk_v_card_no ,
	'TWMP' ,
	'LOST' ,
	'' ,
	wk_act_code ,
	new.mod_user ,
	'1' ,
	'1' ,
	'2' ,
	'OPPO' ,
	new.bin_type ,
	'0' ,
	to_char(sysdate,'yyyymmddhh24miss'),
    'tr_current_code' ,
	'' ,
	new.current_code ,
	new.new_end_date ,
	new.oppost_date ,
	new.oppost_reason ,
    wk_reason_code);--
   end if;--
  end if;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CYC_INT_DIFFER_LOG_A AFTER
INSERT
    ON
    CYC_INT_DIFFER REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE tmpVar INTEGER;--
--
 DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
tmpVar = 0;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    cyc_int_differ_log ( log_date,
    log_time,
    aud_code,
    jcic_grade,
    jcic_int_sign,
    jcic_int_rate,
    class_code,
    class_int_sign,
    class_int_rate,
    purchase_month,
    purchase_rate,
    purchase_int_sign_up,
    purchase_int_rate_up,
    purchase_int_sign_dn,
    purchase_int_rate_dn,
    other_int_sign,
    other_int_rate,
    run_batch_no,
    crt_user,
    crt_date,
    apr_user,
    apr_date,
    mod_user,
    mod_time,
    mod_pgm,
    jcic_grade2,
    class_pd_opt,
    pd_rating,
    pd_rating_sign,
    pd_rating_rate,
    other_int_sign2,
    other_int_rate2 )
VALUES ( to_char(sysdate,
'yyyymmdd'),
to_char(sysdate,
'hh24miss'),
wk_audcode,
new.jcic_grade,
new.jcic_int_sign,
new.jcic_int_rate,
new.class_code,
new.class_int_sign,
new.class_int_rate,
new.purchase_month,
new.purchase_rate,
new.purchase_int_sign_up,
new.purchase_int_rate_up,
new.purchase_int_sign_dn,
new.purchase_int_rate_dn,
new.other_int_sign,
new.other_int_rate,
'',
new.crt_user,
new.crt_date,
new.apr_user,
new.apr_date,
new.mod_user,
new.mod_time,
new.mod_pgm,
new.jcic_grade2,
new.class_pd_opt,
new.pd_rating,
new.pd_rating_sign,
new.pd_rating_rate,
new.other_int_sign2,
new.other_int_rate2 ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CYC_INT_DIFFER_LOG_D AFTER
DELETE
    ON
    CYC_INT_DIFFER REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE tmpVar INTEGER;--
--
 DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
tmpVar = 0;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    cyc_int_differ_log ( log_date,
    log_time,
    aud_code,
    jcic_grade,
    jcic_int_sign,
    jcic_int_rate,
    class_code,
    class_int_sign,
    class_int_rate,
    purchase_month,
    purchase_rate,
    purchase_int_sign_up,
    purchase_int_rate_up,
    purchase_int_sign_dn,
    purchase_int_rate_dn,
    other_int_sign,
    other_int_rate,
    run_batch_no,
    crt_user,
    crt_date,
    apr_user,
    apr_date,
    mod_user,
    mod_time,
    mod_pgm,
    jcic_grade2,
    class_pd_opt,
    pd_rating,
    pd_rating_sign,
    pd_rating_rate,
    other_int_sign2,
    other_int_rate2 )
VALUES ( to_char(sysdate,
'yyyymmdd'),
to_char(sysdate,
'hh24miss'),
wk_audcode,
old.jcic_grade,
old.jcic_int_sign,
old.jcic_int_rate,
old.class_code,
old.class_int_sign,
old.class_int_rate,
old.purchase_month,
old.purchase_rate,
old.purchase_int_sign_up,
old.purchase_int_rate_up,
old.purchase_int_sign_dn,
old.purchase_int_rate_dn,
old.other_int_sign,
old.other_int_rate,
'',
old.crt_user,
old.crt_date,
old.apr_user,
old.apr_date,
old.mod_user,
old.mod_time,
old.mod_pgm,
old.jcic_grade2,
old.class_pd_opt,
old.pd_rating,
old.pd_rating_sign,
old.pd_rating_rate,
old.other_int_sign2,
old.other_int_rate2 ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CYC_INT_DIFFER_LOG_U AFTER
UPDATE
    ON
    CYC_INT_DIFFER REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE tmpVar INTEGER;--
--
 DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
tmpVar = 0;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    cyc_int_differ_log ( log_date,
    log_time,
    aud_code,
    jcic_grade,
    jcic_int_sign,
    jcic_int_rate,
    class_code,
    class_int_sign,
    class_int_rate,
    purchase_month,
    purchase_rate,
    purchase_int_sign_up,
    purchase_int_rate_up,
    purchase_int_sign_dn,
    purchase_int_rate_dn,
    other_int_sign,
    other_int_rate,
    run_batch_no,
    crt_user,
    crt_date,
    apr_user,
    apr_date,
    mod_user,
    mod_time,
    mod_pgm,
    jcic_grade2,
    class_pd_opt,
    pd_rating,
    pd_rating_sign,
    pd_rating_rate,
    other_int_sign2,
    other_int_rate2 )
VALUES ( to_char(sysdate,
'yyyymmdd'),
to_char(sysdate,
'hh24miss'),
wk_audcode,
new.jcic_grade,
new.jcic_int_sign,
new.jcic_int_rate,
new.class_code,
new.class_int_sign,
new.class_int_rate,
new.purchase_month,
new.purchase_rate,
new.purchase_int_sign_up,
new.purchase_int_rate_up,
new.purchase_int_sign_dn,
new.purchase_int_rate_dn,
new.other_int_sign,
new.other_int_rate,
'',
new.crt_user,
new.crt_date,
new.apr_user,
new.apr_date,
new.mod_user,
new.mod_time,
new.mod_pgm,
new.jcic_grade2,
new.class_pd_opt,
new.pd_rating,
new.pd_rating_sign,
new.pd_rating_rate,
new.other_int_sign2,
new.other_int_rate2 ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_DBC_RETURN_A BEFORE
INSERT
    ON
    DBC_RETURN REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.card_no,
    ' ') <> NVL(old.card_no,
    ' ')
    OR NVL(new.acct_no,
    ' ') <> NVL(old.acct_no,
    ' ')
    OR NVL(new.return_date,
    ' ') <> NVL(old.return_date,
    ' ')
    OR NVL(new.ic_flag,
    ' ') <> NVL(old.ic_flag,
    ' ')
    OR NVL(new.reason_code,
    ' ') <> NVL(old.reason_code,
    ' ')
    OR NVL(new.return_type,
    ' ') <> NVL(old.return_type,
    ' ')
    OR NVL(new.mail_type,
    ' ') <> NVL(old.mail_type,
    ' ')
    OR NVL(new.mail_branch,
    ' ') <> NVL(old.mail_branch,
    ' ')
    OR NVL(new.mail_date ,
    ' ') <> NVL(old.mail_date ,
    ' ')
    OR NVL(new.zip_code,
    ' ') <> NVL(old.zip_code,
    ' ')
    OR NVL(new.mail_addr1,
    ' ') <> NVL(old.mail_addr1,
    ' ')
    OR NVL(new.mail_addr2,
    ' ') <> NVL(old.mail_addr2,
    ' ')
    OR NVL(new.mail_addr3,
    ' ') <> NVL(old.mail_addr3,
    ' ')
    OR NVL(new.mail_addr4,
    ' ') <> NVL(old.mail_addr4,
    ' ')
    OR NVL(new.mail_addr5,
    ' ') <> NVL(old.mail_addr5,
    ' ')
    OR NVL(new.beg_date,
    ' ') <> NVL(old.beg_date,
    ' ')
    OR NVL(new.end_date,
    ' ') <> NVL(old.end_date,
    ' ')
    OR NVL(new.proc_status,
    ' ') <> NVL(old.proc_status,
    ' ')
    OR NVL(new.return_note,
    ' ') <> NVL(old.return_note,
    ' ') )
BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
        SQLWARNING
    BEGIN
            RETURN;--
END;--
--
 INSERT
    INTO
    dbc_return_log ( mod_date,
    mod_time,
    mod_user,
    card_no,
    acct_no,
    return_date,
    group_code,
    id_p_seqno,
    ic_flag,
    reason_code,
    return_type,
    mail_type,
    mail_branch,
    mail_date,
    zip_code,
    mail_addr1,
    mail_addr2,
    mail_addr3,
    mail_addr4,
    mail_addr5,
    beg_date,
    end_date,
    return_note,
    proc_status,
    package_flag,
    package_date )
VALUES ( to_char(sysdate,
'yyyymmdd'),
to_char(sysdate,
'hh24miss'),
new.mod_user,
new.card_no,
new.acct_no,
new.return_date,
new.group_code,
new.id_p_seqno,
new.ic_flag,
new.reason_code,
new.return_type,
new.mail_type,
new.mail_branch,
new.mail_date,
new.zip_code,
new.mail_addr1,
new.mail_addr2,
new.mail_addr3,
new.mail_addr4,
new.mail_addr5,
new.beg_date,
new.end_date,
new.return_note,
new.proc_status,
new.package_flag,
new.package_date ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_DBC_RETURN_U BEFORE
UPDATE
    OF card_no,
    acct_no,
    return_date,
    ic_flag,
    reason_code,
    return_type,
    mail_type,
    mail_branch,
    mail_date,
    zip_code,
    mail_addr1,
    mail_addr2,
    mail_addr3,
    mail_addr4,
    mail_addr5,
    beg_date,
    end_date,
    proc_status,
    return_note ON
    DBC_RETURN REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.card_no,
    ' ') <> NVL(old.card_no,
    ' ')
    OR NVL(new.acct_no,
    ' ') <> NVL(old.acct_no,
    ' ')
    OR NVL(new.return_date,
    ' ') <> NVL(old.return_date,
    ' ')
    OR NVL(new.ic_flag,
    ' ') <> NVL(old.ic_flag,
    ' ')
    OR NVL(new.reason_code,
    ' ') <> NVL(old.reason_code,
    ' ')
    OR NVL(new.return_type,
    ' ') <> NVL(old.return_type,
    ' ')
    OR NVL(new.mail_type,
    ' ') <> NVL(old.mail_type,
    ' ')
    OR NVL(new.mail_branch,
    ' ') <> NVL(old.mail_branch,
    ' ')
    OR NVL(new.mail_date ,
    ' ') <> NVL(old.mail_date ,
    ' ')
    OR NVL(new.zip_code,
    ' ') <> NVL(old.zip_code,
    ' ')
    OR NVL(new.mail_addr1,
    ' ') <> NVL(old.mail_addr1,
    ' ')
    OR NVL(new.mail_addr2,
    ' ') <> NVL(old.mail_addr2,
    ' ')
    OR NVL(new.mail_addr3,
    ' ') <> NVL(old.mail_addr3,
    ' ')
    OR NVL(new.mail_addr4,
    ' ') <> NVL(old.mail_addr4,
    ' ')
    OR NVL(new.mail_addr5,
    ' ') <> NVL(old.mail_addr5,
    ' ')
    OR NVL(new.beg_date,
    ' ') <> NVL(old.beg_date,
    ' ')
    OR NVL(new.end_date,
    ' ') <> NVL(old.end_date,
    ' ')
    OR NVL(new.proc_status,
    ' ') <> NVL(old.proc_status,
    ' ')
    OR NVL(new.return_note,
    ' ') <> NVL(old.return_note,
    ' ') )
BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
        SQLWARNING
    BEGIN
            RETURN;--
END;--
--
 INSERT
    INTO
    dbc_return_log ( mod_date,
    mod_time,
    mod_user,
    card_no,
    acct_no,
    return_date,
    group_code,
    id_p_seqno,
    ic_flag,
    reason_code,
    return_type,
    mail_type,
    mail_branch,
    mail_date,
    zip_code,
    mail_addr1,
    mail_addr2,
    mail_addr3,
    mail_addr4,
    mail_addr5,
    beg_date,
    end_date,
    return_note,
    proc_status,
    package_flag,
    package_date )
VALUES ( to_char(sysdate,
'yyyymmdd'),
to_char(sysdate,
'hh24miss'),
new.mod_user,
new.card_no,
new.acct_no,
new.return_date,
new.group_code,
new.id_p_seqno,
new.ic_flag,
new.reason_code,
new.return_type,
new.mail_type,
new.mail_branch,
new.mail_date,
new.zip_code,
new.mail_addr1,
new.mail_addr2,
new.mail_addr3,
new.mail_addr4,
new.mail_addr5,
new.beg_date,
new.end_date,
new.return_note,
new.proc_status,
new.package_flag,
new.package_date ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_ICH_ACT_ACNO
AFTER UPDATE OF
      bill_sending_addr1,
      bill_sending_addr2,
      bill_sending_addr3,
      bill_sending_addr4,
      bill_sending_addr5
 ON ACT_ACNO
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL
WHEN (
        NVL(new.bill_sending_addr1,' ') <> NVL(old.bill_sending_addr1,' ') or
        NVL(new.bill_sending_addr2,' ') <> NVL(old.bill_sending_addr2,' ') or
        NVL(new.bill_sending_addr3,' ') <> NVL(old.bill_sending_addr3,' ') or
        NVL(new.bill_sending_addr4,' ') <> NVL(old.bill_sending_addr4,' ') or
        NVL(new.bill_sending_addr5,' ') <> NVL(old.bill_sending_addr5,' ')
      )

BEGIN

DECLARE h_ich_card_no      varchar(19);--
DECLARE h_card_no          varchar(19);--
DECLARE wk_sup_flag        varchar(1);--
DECLARE wk_send            varchar(80);--
DECLARE wk_resident_addr   varchar(80);--
DECLARE wk_cellar_phone    varchar(20);--
DECLARE wk_e_mail_addr     varchar(80);--
DECLARE wk_card_no         varchar(20);--
DECLARE wk_ich_card_no     varchar(20);--
DECLARE wk_old_ich_card_no varchar(20);--
DECLARE wk_phone_1         varchar(20);--
DECLARE wk_phone_2         varchar(20);--
DECLARE wk_ich_emboss_rsn  varchar(1);--
DECLARE wk_id              varchar(19);--
DECLARE wk_chi_name        varchar(30);--
DECLARE wk_birthday        varchar(20);--

DECLARE wk_cnt         integer;--
DECLARE step_flag      varchar(1);--

  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
--declare exit handler for sqlstate '02000'
  declare exit handler for SQLEXCEPTION, SQLWARNING
          signal   sqlstate '38S02' set message_text='Error : tr_ich_act_acno Error.';--

   set step_flag    = '0';--
   
   SELECT b.sup_flag
        , a.id_no
        , a.chi_name 
        , a.birthday
     INTO wk_sup_flag
        , wk_id
        , wk_chi_name 
        , wk_birthday
     FROM crd_card b , crd_idno a
    where a.id_p_seqno = new.id_p_seqno
      and b.id_p_seqno = a.id_p_seqno
      and b.issue_date in ( select max(issue_date)
                              from crd_card c
                             where c.id_p_seqno = new.id_p_seqno)
      fetch first 1 rows only;--
  
   SELECT a.ich_emboss_rsn
        , a.ich_card_no
        , a.old_ich_card_no
     INTO wk_ich_emboss_rsn
        , wk_ich_card_no
        , wk_old_ich_card_no
     FROM ich_card a
    where a.card_no    = wk_card_no
      and a.new_beg_date in ( select max(new_beg_date)
                                from ich_card c
                               where c.card_no   = wk_card_no)
      fetch first 1 rows only;--

  IF UPDATING THEN
   
       SELECT count(*)
         INTO wk_cnt
         FROM ich_b06b_idno
        where ich_card_no    = wk_ich_card_no
          and (proc_flag     = '' or proc_flag     = 'N');--

       if wk_cnt > 0 then
          set step_flag    = 'B';--
          return;--
       end if;--

      INSERT INTO ich_b06b_idno
         (ich_card_no,        
          id_no,             
          name,
          birthday,
          addr_1,
          addr_2,
          mob_phone_1,
          mob_phone_2,
          phone_1,
          phone_2,
          e_mail_addr_1,
          e_mail_addr_2, 
          op_code,
          op_kind,              
          sup_flag,            
          old_ich_card_no,
          card_level,
          sys_date,
          sys_time,
          proc_flag,
          ok_flag,
          mod_time,
          mod_pgm )
       VALUES
         (wk_ich_card_no,
          wk_id,
          wk_chi_name,
          wk_birthday,
          wk_send,
          wk_resident_addr,
          wk_cellar_phone,
          '0000000000',
          wk_phone_1,
          wk_phone_2,
          decode(wk_e_mail_addr,'','NA',wk_e_mail_addr),
          'NA',
          'U',
          decode(wk_ich_emboss_rsn,'1','A','3','D','4','D','5','B','C'),
          wk_sup_flag,
          wk_old_ich_card_no,
          '00',
          to_char(sysdate, 'YYYYMMDD'),
          to_char(sysdate, 'HH24MISS'),
          'N',
          'N',
          sysdate, 
          'tr_ich_act_acno');--
  end if;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_ICH_BLACK_MODLOG
 AFTER UPDATE
 ON ICH_BLACK_LIST
 REFERENCING OLD AS OLD NEW AS NEW
 FOR EACH ROW MODE DB2SQL
	  
BEGIN

DECLARE  ls_pgm_type    varchar(1);--
DECLARE  ls_mod_audcode varchar(1);--
DECLARE  ls_mod_user    varchar(20);--
DECLARE  ls_mod_pgm     varchar(20);--


  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
--declare exit handler for sqlstate '02000'   
  declare exit handler for SQLEXCEPTION, SQLWARNING
          signal   sqlstate '38S02' set message_text='Error : tr_ich_black_modlog Error.';--


   set ls_mod_user = new.mod_user;--
   set ls_mod_pgm  = new.mod_pgm;--

   IF INSERTING THEN
        set ls_mod_audcode = 'A';--
   END IF;--
   IF UPDATING THEN
        set ls_mod_audcode = 'U';--
   END IF;--
   IF DELETING THEN
        set ls_mod_audcode = 'D';--
   END IF;--

   IF INSERTING or UPDATING THEN
     INSERT INTO ICH_BLACK_MODLOG
               ( mod_date,
                 mod_time,
                 mod_user,
                 mod_pgm,
                 mod_audcode,
                 ich_card_no,
                 card_no,
                 black_date,
                 black_user_id,
                 black_remark,
                 black_flag,
                 send_date_s,
                 send_date_e,
                 from_type,
                 apr_date,
                 apr_user
      ) VALUES (
                 to_char(sysdate,'yyyymmdd'),
                 sysdate,
                 ls_mod_user,
                 ls_mod_pgm,
                 ls_mod_audcode,
                 new.ich_card_no,
                 new.card_no,
                 new.black_date,
                 new.black_user_id,
                 new.black_remark,
                 new.black_flag,
                 new.send_date_s,
                 new.send_date_e,
                 new.from_type,
                 new.apr_date,
                 new.apr_user
               )  ;--
   END IF;--
   IF DELETING THEN
     INSERT INTO ICH_BLACK_MODLOG
               ( mod_date,
                 mod_time,
                 mod_user,
                 mod_pgm,
                 mod_audcode,
                 ich_card_no,
                 card_no,
                 black_date,
                 black_user_id,
                 black_remark,
                 black_flag,
                 send_date_s,
                 send_date_e,
                 from_type,
                 apr_date,
                 apr_user
      ) VALUES (
                 to_char(sysdate,'yyyymmdd'),
                 sysdate,
                 ls_mod_user,
                 ls_mod_pgm,
                 ls_mod_audcode,
                 old.ich_card_no,
                 old.card_no,
                 old.black_date,
                 old.black_user_id,
                 old.black_remark,
                 old.black_flag,
                 old.send_date_s,
                 old.send_date_e,
                 old.from_type,
                 to_char(sysdate,'yyyymmdd'),
                 old.apr_user
               )  ;--
   END IF;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_ICH_CARD_A AFTER
INSERT
    ON
    ICH_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_date CHAR(8);--
--
 DECLARE wk_time CHAR(6);--
--
 DECLARE wk_sup_flag VARCHAR(1);--
--
 DECLARE wk_id_no VARCHAR(20);--
--
 DECLARE wk_name VARCHAR(75);--
--
 DECLARE wk_birthday VARCHAR(8);--
--
 DECLARE wk_addr_1 VARCHAR(300);--
--
 DECLARE wk_addr_2 VARCHAR(300);--
--
 DECLARE wk_mob_phone_1 VARCHAR(15);--
--
 DECLARE wk_mob_phone_2 VARCHAR(15);--
--
 DECLARE wk_phone_1 VARCHAR(20);--
--
 DECLARE wk_phone_2 VARCHAR(20);--
--
 DECLARE wk_e_mail_addr_1 VARCHAR(50);--
--
 DECLARE wk_e_mail_addr_2 VARCHAR(50);--
--
 DECLARE wk_other_cntry_code VARCHAR(5);--
--
 DECLARE wk_ich_card_no VARCHAR(20);--
--
 DECLARE wk_cnt INTEGER;--
--
 DECLARE step_flag VARCHAR(1);--
--
 DECLARE CONTINUE HANDLER FOR NOT FOUND
BEGIN
    RETURN;--
END;--
--
--declare exit handler for sqlstate '02000'
 DECLARE EXIT HANDLER FOR SQLEXCEPTION,
SQLWARNING SIGNAL SQLSTATE '38S02' SET
message_text = 'Error : tr_ich_card_a   Error.';--
--
 SELECT
    a.sup_flag,
    b.id_no,
    b.chi_name,
    b.birthday,
    b.other_cntry_code,
    c.bill_sending_addr1 || c.bill_sending_addr2 || c.bill_sending_addr3 || c.bill_sending_addr4 || c.bill_sending_addr5,
    b.resident_addr1 || b.resident_addr2 || b.resident_addr3 || b.resident_addr4 || b.resident_addr5,
    b.cellar_phone,
    '0000000000',
    '(' || b.OFFICE_AREA_CODE1 || ')' || b.OFFICE_TEL_NO1 || '#' || OFFICE_TEL_EXT1,
    '(' || b.HOME_AREA_CODE1 || ')' || b.HOME_TEL_NO1 || '#' || HOME_TEL_EXT1,
    decode(b.e_mail_addr,
    '',
    'NA',
    b.e_mail_addr),
    'NA'
INTO
    wk_sup_flag,
    wk_id_no,
    wk_name,
    wk_birthday,
    wk_other_cntry_code,
    wk_addr_1,
    wk_addr_2,
    wk_mob_phone_1,
    wk_mob_phone_2,
    wk_phone_1,
    wk_phone_2,
    wk_e_mail_addr_1,
    wk_e_mail_addr_2
FROM
    act_acno c,
    crd_idno b ,
    crd_card a
WHERE
    b.id_p_seqno = a.id_p_seqno
    AND c.p_seqno = a.p_seqno
    AND a.card_no = new.card_no;--
--
 IF INSERTING THEN SET
wk_ich_card_no = new.ich_card_no;--
--

    BEGIN
SELECT
    COUNT(*)
INTO
    wk_cnt
FROM
    ich_b06b_idno
WHERE
    ich_card_no = wk_ich_card_no
    AND (proc_flag = ''
    OR proc_flag = 'N');--
--
END;--
--
 IF wk_cnt > 0 THEN SET
step_flag = 'B';--
--
 RETURN;--
--
END IF;--
--
 INSERT
    INTO
    ich_b06b_idno (ich_card_no,
    id_no,
    name,
    birthday,
    addr_1,
    addr_2,
    mob_phone_1,
    mob_phone_2,
    phone_1,
    phone_2,
    e_mail_addr_1,
    e_mail_addr_2,
    op_code,
    op_kind,
    sup_flag,
    old_ich_card_no,
    card_level,
    sys_date,
    sys_time,
    proc_flag,
    ok_flag,
    mod_time,
    mod_pgm )
VALUES (new.ich_card_no,
wk_id_no,
wk_name,
wk_birthday,
wk_addr_1,
wk_addr_2,
wk_mob_phone_1,
wk_mob_phone_2,
wk_phone_1,
wk_phone_2,
wk_e_mail_addr_1,
wk_e_mail_addr_2,
'A',
decode(new.ich_emboss_rsn,
'1',
'A',
'3',
'D',
'4',
'D',
'5',
'B',
'C'),
wk_sup_flag,
new.old_ich_card_no,
'00',
to_char(sysdate,
'YYYYMMDD'),
to_char(sysdate,
'HH24MISS'),
'N',
'N',
sysdate,
'tr_ich_card_a');--
--

    BEGIN
SELECT
    COUNT(*)
INTO
    wk_cnt
FROM
    ich_b96b_idno
WHERE
    id_no = wk_id_no
    AND (proc_flag = ''
    OR proc_flag = 'N');--
--
END;--
--
 IF wk_cnt > 0 THEN SET
step_flag = 'D';--
--
 RETURN;--
--
END IF;--
--
 INSERT
    INTO
    ich_b96b_idno (id_no,
    chg_type,
    chg_date,
    issue_loc,
    nation_full,
    vacationn_full,
    sys_date,
    sys_time,
    proc_flag,
    ok_flag,
    mod_time,
    mod_pgm )
VALUES (wk_id_no,
'',
'',
'',
wk_other_cntry_code,
'',
to_char(sysdate,
'YYYYMMDD'),
to_char(sysdate,
'HH24MISS'),
'N',
'N',
sysdate,
'tr_ich_card_a');--
--
END IF;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_ICH_CARD_PARM
 AFTER INSERT
 ON ICH_CARD_PARM
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL

BEGIN

DECLARE wk_date              CHAR(8);--
DECLARE wk_time              CHAR(6);--

  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
--declare exit handler for sqlstate '02000'
  declare exit handler for SQLEXCEPTION, SQLWARNING
          signal   sqlstate '38S02' set message_text='Error : tr_ich_card_parm Error.';--

    if INSERTING then
       INSERT INTO ich_b01b_parm
         (bank_id     ,
          group_id    ,
          add_year    ,
          bin_type    ,
          SEQ_NO      ,
          lot_no_ich  ,
          card_code   ,
          bar_code    ,
          card_name   ,
          vendor_ich  ,
          seq_no_curr ,
          ich_kind    ,
          union_flag  ,
          UNIFORM_NO  ,
          SYS_DATE    ,
          SYS_TIME    ,
          PROC_FLAG   ,
          OK_FLAG     ,
          MOD_TIME    ,
          MOD_PGM )
       VALUES
         (new.bank_id     ,
          new.group_id    ,
          substr(new.add_year ,3,2) ,
          substr(new.card_code,7,1) ,
          substr(new.card_code,8,2) ,
          substr(new.card_code,7,3) ,
          new.card_code    ,
          '0000000000000000',
          new.card_name    ,
          '00'              ,
          new.seq_no_curr  ,
          '01'              ,
          '0'               ,
          '00000000'        ,
          to_char(sysdate, 'YYYYMMDD'), 
          to_char(sysdate, 'HH24MISS'),
          'N',
          'N',
          sysdate, 
          'tr_ich_card_parm');--
        end if;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_ICH_CRD_IDNO
 AFTER UPDATE of
              resident_addr1,
              resident_addr2,
              resident_addr3,
              resident_addr4,
              resident_addr5,
              chi_name
 ON CRD_IDNO
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL
   WHEN ( NVL(new.chi_name          ,' ') <> NVL(old.chi_name          ,' ') or
          NVL(new.birthday          ,' ') <> NVL(old.birthday          ,' ') or
          NVL(new.cellar_phone      ,' ') <> NVL(old.cellar_phone      ,' ') or
          NVL(new.office_area_code1 ,' ') <> NVL(old.office_area_code1 ,' ') or
          NVL(new.office_tel_no1    ,' ') <> NVL(old.office_tel_no1    ,' ') or
          NVL(new.office_tel_ext1   ,' ') <> NVL(old.office_tel_ext1   ,' ') or
          NVL(new.home_area_code1   ,' ') <> NVL(old.home_area_code1   ,' ') or
          NVL(new.home_tel_no1      ,' ') <> NVL(old.home_tel_no1      ,' ') or
          NVL(new.home_tel_ext1     ,' ') <> NVL(old.home_tel_ext1     ,' ') or
          NVL(new.e_mail_addr       ,' ') <> NVL(old.e_mail_addr       ,' ') or
          NVL(new.resident_addr1    ,' ') <> NVL(old.resident_addr1    ,' ') or
          NVL(new.resident_addr2    ,' ') <> NVL(old.resident_addr2    ,' ') or
          NVL(new.resident_addr3    ,' ') <> NVL(old.resident_addr3    ,' ') or
          NVL(new.resident_addr4    ,' ') <> NVL(old.resident_addr4    ,' ') or
          NVL(new.resident_addr5    ,' ') <> NVL(old.resident_addr5    ,' ')
         )

BEGIN

DECLARE wk_date               CHAR(8);--
DECLARE wk_time               CHAR(6);--
DECLARE wk_sup_flag        varchar(1);--
DECLARE wk_addr_1          varchar(80);--
DECLARE wk_card_no         varchar(20);--
DECLARE wk_ich_card_no     varchar(20);--
DECLARE wk_old_ich_card_no varchar(20);--
DECLARE wk_phone_1         varchar(20);--
DECLARE wk_phone_2         varchar(20);--
DECLARE wk_ich_emboss_rsn  varchar(1);--

DECLARE wk_cnt         integer;--
DECLARE step_flag      varchar(100);--


  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
  DECLARE exit handler for SQLEXCEPTION, SQLWARNING
          signal   sqlstate '38S02' set message_text= step_flag;--

   set step_flag    = '0';--

  BEGIN
  
   SELECT b.sup_flag
        , b.card_no
        , c.bill_sending_addr1||c.bill_sending_addr2||c.bill_sending_addr3||c.bill_sending_addr4||c.bill_sending_addr5
     INTO wk_sup_flag
        , wk_card_no
        , wk_addr_1
     FROM act_acno c, crd_card b
    where b.id_p_seqno = old.id_p_seqno
      and c.p_seqno    = b.p_seqno
      and b.issue_date in ( select max(issue_date)
                              from crd_card c
                             where c.id_p_seqno = old.id_p_seqno)
      fetch first 1 rows only;--
   set step_flag    = 'A';--
 
   SELECT b.sup_flag
        , b.card_no
        , c.bill_sending_addr1||c.bill_sending_addr2||c.bill_sending_addr3||c.bill_sending_addr4||c.bill_sending_addr5
     INTO wk_sup_flag
        , wk_card_no
        , wk_addr_1
    FROM ecs_act_acno c, ecs_crd_card b
   where b.id_p_seqno = old.id_p_seqno
     and c.p_seqno    = b.p_seqno
     and b.issue_date in ( select max(issue_date)
                             from ecs_crd_card c
                            where c.id_p_seqno = old.id_p_seqno)
    fetch first 1 rows only;--
  END;--
   set step_flag    = '2';--
   SELECT a.ich_emboss_rsn
        , nvl(a.ich_card_no,'')
        , a.old_ich_card_no
     INTO wk_ich_emboss_rsn
        , wk_ich_card_no
        , wk_old_ich_card_no
     FROM ich_card a
    where a.card_no    = wk_card_no
      and a.new_beg_date in ( select max(new_beg_date)
                                from ich_card c
                               where c.card_no   = wk_card_no)
      fetch first 1 rows only;--

    set wk_phone_1 = '('||new.OFFICE_AREA_CODE1||')'||new.OFFICE_TEL_NO1||'#'||new.OFFICE_TEL_EXT1;--
    set wk_phone_2 = '('||new.HOME_AREA_CODE1||')'||new.HOME_TEL_NO1||'#'||new.HOME_TEL_EXT1;--

    if UPDATING then
        SELECT count(*)
          INTO wk_cnt
          FROM ich_b06b_idno
         where ich_card_no    = wk_ich_card_no
           and (proc_flag     = '' or proc_flag     = 'N');--

       if wk_cnt > 0 then
          set step_flag    = 'B';--
          return;--
       end if;--

       set step_flag    = 'c';--
       set step_flag     = step_flag || ',' || length(wk_addr_1);--

       INSERT INTO ich_b06b_idno
         (ich_card_no,
          id_no,
          name,
          birthday,
          addr_1,
          addr_2,
          mob_phone_1,
          mob_phone_2,
          phone_1,
          phone_2,
          e_mail_addr_1,
          e_mail_addr_2,
          op_code,
          op_kind,
          sup_flag,
          old_ich_card_no,
          card_level,
          sys_date,
          sys_time,
          proc_flag,
          ok_flag,
          mod_time,
          mod_pgm )
       VALUES
         (wk_ich_card_no,
          new.id_no,
          new.chi_name,
          new.birthday,
          wk_addr_1,
          new.resident_addr1||new.resident_addr2||new.resident_addr3||new.resident_addr4||new.resident_addr5,
          new.cellar_phone,
          '0000000000',
          wk_phone_1,
          wk_phone_2,
          decode(new.e_mail_addr,'','NA',new.e_mail_addr),
          'NA',
          'U',
          decode(wk_ich_emboss_rsn,'1','A','3','D','4','D','5','B','C'),
          wk_sup_flag,
          wk_old_ich_card_no,
          '00',
          to_char(sysdate, 'YYYYMMDD'),
          to_char(sysdate, 'HH24MISS'),
          'N',
          'N',
          sysdate,
          'tr_ich_card_a');--
        end if;--

       set step_flag    = 'D';--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_ACT2APS_U AFTER
UPDATE
    OF BIRTHDAY ON
    CRD_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.birthday,
    ' ') <> NVL(old.birthday,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE wk_date VARCHAR(8);--
--
 DECLARE wk_time VARCHAR(6);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
--  select to_char(sysdate, 'YYYYMMDDHH24MISSSSS') into wk_modtime from dual;--
--  select to_char(sysdate, 'YYYYMMDD') into wk_date from dual;--

--  select to_char(sysdate, 'HH24MISSSSS') into wk_time from dual;--
 INSERT
    INTO
    act_toaps ( mod_time,
    mod_user,
    mod_pgm,
    mod_table,
    p_seqno,
    id_p_seqno,
    proc_mark,
    update_date,
    update_time )
VALUES ( sysdate,
new.mod_user,
new.mod_pgm,
'CRD_IDNO',
'',
new.id_p_seqno,
'0',
to_char(sysdate,
'YYYYMMDD'),
to_char(sysdate,
'HH24MISS') ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_HIST_A AFTER
INSERT
    ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    crd_idno_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    id_p_seqno,
    id_no,
    id_no_code,
    chi_name,
    sex,
    birthday,
    nation,
    education,
    marriage,
    company_name,
    job_position,
    asset_value,
    e_mail_addr,
    crt_date,
    crt_user,
    office_area_code1,
    office_tel_no1,
    office_tel_ext1,
    office_area_code2,
    office_tel_no2,
    office_tel_ext2,
    home_area_code1,
    home_tel_no1,
    home_tel_ext1,
    home_area_code2,
    home_tel_no2,
    home_tel_ext2,
    cellar_phone,
    special_code,
    voice_passwd_flag)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
new.mod_user,
wk_audcode,
new.id_p_seqno,
new.id_no,
new.id_no_code,
new.chi_name,
new.sex,
new.birthday,
new.nation,
new.education,
new.marriage,
new.company_name,
new.job_position,
new.asset_value,
new.e_mail_addr,
new.crt_date,
new.crt_user,
new.office_area_code1,
new.office_tel_no1,
new.office_tel_ext1,
new.office_area_code2,
new.office_tel_no2,
new.office_tel_ext2,
new.home_area_code1,
new.home_tel_no1,
new.home_tel_ext1,
new.home_area_code2,
new.home_tel_no2,
new.home_tel_ext2,
new.cellar_phone,
new.special_code,
new.voice_passwd_flag );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_HIST_D AFTER
DELETE
    ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    crd_idno_hist ( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    id_p_seqno,
    id_no,
    id_no_code,
    chi_name,
    sex,
    birthday,
    nation,
    education,
    marriage,
    company_name,
    job_position,
    asset_value,
    e_mail_addr,
    crt_date,
    crt_user,
    office_area_code1,
    office_tel_no1,
    office_tel_ext1,
    office_area_code2,
    office_tel_no2,
    office_tel_ext2,
    home_area_code1,
    home_tel_no1,
    home_tel_ext1,
    home_area_code2,
    home_tel_no2,
    home_tel_ext2,
    cellar_phone,
    special_code,
    voice_passwd_flag)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
old.mod_pgm,
old.mod_user,
wk_audcode,
old.id_p_seqno,
old.id_no,
old.id_no_code,
old.chi_name,
old.sex,
old.birthday,
old.nation,
old.education,
old.marriage,
old.company_name,
old.job_position,
old.asset_value,
old.e_mail_addr,
old.crt_date,
old.crt_user,
old.office_area_code1,
old.office_tel_no1,
old.office_tel_ext1,
old.office_area_code2,
old.office_tel_no2,
old.office_tel_ext2,
old.home_area_code1,
old.home_tel_no1,
old.home_tel_ext1,
old.home_area_code2,
old.home_tel_no2,
old.home_tel_ext2,
old.cellar_phone,
old.special_code,
old.voice_passwd_flag );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_HIST_U AFTER
UPDATE
    OF ASSET_VALUE,
    CELLAR_PHONE,
    E_MAIL_ADDR,
    HOME_AREA_CODE1,
    HOME_AREA_CODE2,
    HOME_TEL_EXT1,
    HOME_TEL_EXT2,
    HOME_TEL_NO1,
    HOME_TEL_NO2,
    OFFICE_AREA_CODE1,
    OFFICE_AREA_CODE2,
    OFFICE_TEL_EXT1,
    OFFICE_TEL_EXT2,
    OFFICE_TEL_NO1,
    OFFICE_TEL_NO2,
    SPECIAL_CODE,
    VOICE_PASSWD_FLAG ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.asset_value,
    0) <> nvl(new.asset_value,
    0)
    OR nvl(old.e_mail_addr,
    ' ') <> nvl(new.e_mail_addr,
    ' ')
    OR nvl(old.office_area_code1,
    ' ') <> nvl(new.office_area_code1,
    ' ')
    OR nvl(old.office_tel_no1,
    ' ') <> nvl(new.office_tel_no1,
    ' ')
    OR nvl(old.office_tel_ext1,
    ' ') <> nvl(new.office_tel_ext1,
    ' ')
    OR nvl(old.office_area_code2,
    ' ') <> nvl(new.office_area_code2,
    ' ')
    OR nvl(old.office_tel_no2,
    ' ') <> nvl(new.office_tel_no2,
    ' ')
    OR nvl(old.office_tel_ext2,
    ' ') <> nvl(new.office_tel_ext2,
    ' ')
    OR nvl(old.home_area_code1,
    ' ') <> nvl(new.home_area_code1,
    ' ')
    OR nvl(old.home_tel_no1,
    ' ') <> nvl(new.home_tel_no1,
    ' ')
    OR nvl(old.home_tel_ext1,
    ' ') <> nvl(new.home_tel_ext1,
    ' ')
    OR nvl(old.home_area_code2,
    ' ') <> nvl(new.home_area_code2,
    ' ')
    OR nvl(old.home_tel_no2,
    ' ') <> nvl(new.home_tel_no2,
    ' ')
    OR nvl(old.home_tel_ext2,
    ' ') <> nvl(new.home_tel_ext2,
    ' ')
    OR nvl(old.special_code,
    ' ') <> nvl(new.special_code,
    ' ')
    OR nvl(old.voice_passwd_flag,
    ' ') <> nvl(new.voice_passwd_flag,
    ' ')
    OR nvl(old.cellar_phone,
    ' ') <> nvl(new.cellar_phone,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_modtime VARCHAR(8);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    crd_idno_hist( mod_time,
    mod_pgm,
    mod_user,
    mod_audcode,
    id_p_seqno,
    id_no,
    id_no_code,
    chi_name,
    sex,
    birthday,
    nation,
    education,
    marriage,
    company_name,
    job_position,
    asset_value,
    e_mail_addr,
    crt_date,
    crt_user,
    office_area_code1,
    office_tel_no1,
    office_tel_ext1,
    office_area_code2,
    office_tel_no2,
    office_tel_ext2,
    home_area_code1,
    home_tel_no1,
    home_tel_ext1,
    home_area_code2,
    home_tel_no2,
    home_tel_ext2,
    cellar_phone,
    special_code,
    voice_passwd_flag)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
new.mod_user,
wk_audcode,
new.id_p_seqno,
new.id_no,
new.id_no_code,
new.chi_name,
new.sex,
new.birthday,
new.nation,
new.education,
new.marriage,
new.company_name,
new.job_position,
new.asset_value,
new.e_mail_addr,
new.crt_date,
new.crt_user,
new.office_area_code1,
new.office_tel_no1,
new.office_tel_ext1,
new.office_area_code2,
new.office_tel_no2,
new.office_tel_ext2,
new.home_area_code1,
new.home_tel_no1,
new.home_tel_ext1,
new.home_area_code2,
new.home_tel_no2,
new.home_tel_ext2,
new.cellar_phone,
new.special_code,
new.voice_passwd_flag );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_KK1_U AFTER
UPDATE
    OF CHI_NAME ,
    BIRTHDAY ,
    OTHER_CNTRY_CODE ,
    PASSPORT_NO ,
    PASSPORT_DATE ,
    STUDENT ,
    BUSINESS_CODE ,
    VACATION_CODE ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.chi_name ,
    ' ') <> nvl(new.chi_name ,
    ' ')
    OR nvl(old.birthday ,
    ' ') <> nvl(new.birthday ,
    ' ')
    OR nvl(old.other_cntry_code ,
    ' ') <> nvl(new.other_cntry_code ,
    ' ')
    OR nvl(old.passport_no ,
    ' ') <> nvl(new.passport_no ,
    ' ')
    OR nvl(old.passport_date ,
    ' ') <> nvl(new.passport_date ,
    ' ')
    OR nvl(old.student ,
    ' ') <> nvl(new.student ,
    ' ')
    OR nvl(old.business_code ,
    ' ') <> nvl(new.business_code ,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    crd_idno_kk1( mod_time ,
    mod_pgm ,
    mod_user ,
    mod_audcode ,
    id_p_seqno ,
    chi_name ,
    birthday ,
    nation ,
    passport_no ,
    passport_date ,
    student ,
    business_code ,
    other_cntry_code)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm ,
new.mod_user ,
wk_audcode ,
new.id_p_seqno ,
new.chi_name ,
new.birthday ,
new.nation ,
new.passport_no ,
new.passport_date ,
new.student ,
new.business_code ,
new.other_cntry_code);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_IDNO_MARKET_U
AFTER UPDATE OF
   tsc_market_flag
 ON CRD_IDNO
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL
WHEN (
      nvl(old.tsc_market_flag,'N')  <> nvl(new.tsc_market_flag,'N')
     )
BEGIN
DECLARE h_ips_card_no varchar(19);--
DECLARE h_card_no varchar(19);--
--DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
  DECLARE END_CL INT DEFAULT 0;--
  DECLARE cursv CURSOR FOR
  --CURSOR cursv IS
  select a.tsc_card_no,a.card_no
    from tsc_card a, crd_card b
   where a.card_no              = b.card_no
     and b.id_p_seqno           = new.id_p_seqno
     and b.ELECTRONIC_CODE      = '01';--
     
DECLARE CONTINUE HANDLER FOR NOT FOUND SET END_CL = 1;--
--FETCH cursv  INTO h_ips_card_no, h_card_no;--

  IF UPDATING THEN
   BEGIN
      OPEN cursv;--
      FETCH cursv INTO h_ips_card_no, h_card_no;--
      WHILE END_CL = 0 DO
            INSERT INTO tsc_cdpf_log
                    (crt_date,
                     crt_time,
                     tsc_card_no,
                     card_no,
                     tx_type,
                     tx_rsn,
                     id_p_seqno,
                     proc_flag,
                     mod_time,
                     mod_pgm)
               values
                    (to_char(sysdate,'yyyymmdd'),
                     to_char(sysdate,'hh24miss'),
                     h_ips_card_no,
                     h_card_no,
                     'C',
                     'M',
                     new.id_p_seqno,
                     'N',
                     sysdate,
                     'tri_idno_market');--
              FETCH cursv INTO h_ips_card_no, h_card_no;--
       END WHILE;--
      
      CLOSE cursv;--
    END;--
  END IF;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_STAT_TYPE_LOG_A AFTER
INSERT
    ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 /******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2007/12/27   JHW             R96-037
******************************************************************************/
INSERT
    INTO
    act_stat_type_log ( mod_date,
    mod_type,
    p_seqno,
    id_p_seqno,
    acct_type,
    chi_name,
    stat_send_type,
    stat_send_month_s,
    stat_send_month_e,
    e_mail_addr,
    e_mail_from_mark,
    apr_user,
    mod_user,
    mod_time,
    mod_pgm )
VALUES ( to_char(sysdate,
'yyyymmdd'),
'2',
'',
new.id_p_seqno,
'',
new.chi_name,
'',
'',
'',
new.e_mail_addr,
new.e_mail_from_mark,
new.apr_user,
new.mod_user,
sysdate,
new.mod_pgm ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_STAT_TYPE_LOG_U AFTER
UPDATE
    OF E_MAIL_ADDR ,
    E_MAIL_FROM_MARK ,
    E_MAIL_CHG_DATE ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.e_mail_addr,
    ' ') <> NVL(old.e_mail_addr,
    ' ')
    OR NVL(new.e_mail_from_mark,
    ' ') <> NVL(old.e_mail_from_mark,
    ' ')
    OR NVL(new.e_mail_chg_date,
    ' ') <> NVL(old.e_mail_chg_date,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 /******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2007/12/27   JHW             R96-037
******************************************************************************/
INSERT
    INTO
    act_stat_type_log ( mod_date,
    mod_type,
    p_seqno,
    id_p_seqno,
    acct_type,
    chi_name,
    stat_send_type,
    stat_send_month_s,
    stat_send_month_e,
    e_mail_addr,
    e_mail_from_mark,
    apr_user,
    mod_user,
    mod_time,
    mod_pgm )
VALUES ( to_char(sysdate,
'yyyymmdd'),
'2',
'',
new.id_p_seqno,
'',
new.chi_name,
'',
'',
'',
new.e_mail_addr,
new.e_mail_from_mark,
new.apr_user,
new.mod_user,
sysdate,
new.mod_pgm ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_TONCCC_A AFTER
INSERT
    ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    crd_idno_tonccc( mod_time,
    mod_pgm,
    mod_audcode,
    id_p_seqno,
    sex,
    birthday,
    education,
    annual_income,
    business_code,
    resident_zip)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
wk_audcode,
new.id_p_seqno,
new.sex,
new.birthday,
new.education,
new.annual_income,
new.business_code,
new.resident_zip);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_TONCCC_D AFTER
DELETE
    ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    crd_idno_tonccc ( mod_time,
    mod_pgm,
    mod_audcode,
    id_p_seqno,
    sex,
    birthday,
    education,
    annual_income,
    business_code,
    resident_zip)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
old.mod_pgm,
wk_audcode,
old.id_p_seqno,
old.sex,
old.birthday,
old.education,
old.annual_income,
old.business_code,
old.resident_zip);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_IDNO_TONCCC_U AFTER
UPDATE
    OF sex,
    birthday,
    annual_income,
    business_code,
    education,
    resident_zip ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    crd_idno_tonccc( mod_time,
    mod_pgm,
    mod_audcode,
    id_p_seqno,
    sex,
    birthday,
    education,
    annual_income,
    business_code,
    resident_zip)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
wk_audcode,
new.id_p_seqno,
new.sex,
new.birthday,
new.education,
new.annual_income,
new.business_code,
new.resident_zip);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_INSERT_TSC_CDNR_LOG_U
AFTER UPDATE
 OF current_code
 ON CRD_CARD
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW MODE DB2SQL

WHEN (
      nvl(old.current_code,'0') <> nvl(new.current_code,'0')
     )
	  
BEGIN
DECLARE wk_create_date   varchar(10);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

/******************************************************************************
   NAME  卡片停卡 insert tsc_cdnr_log
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2015/06/08  JAMES             1. 停卡報送悠遊卡公司
******************************************************************************/


   IF (nvl(new.current_code,'0') <> '0' AND nvl(old.current_code,'0') = '0') THEN

      SELECT max(crt_date)
        INTO wk_create_date
        FROM TSC_CARD
       WHERE card_no = new.card_no;--


      insert into tsc_cdnr_log
             (tsc_card_no,
              card_no,
              tsc_emboss_rsn,
              tsc_vendor_cd,
              vendor_emboss_date,
              isam_batch_seq,
              vendor_date_rtn,
              ok_flag,
              mod_time,
              mod_pgm)
      select tsc_card_no,
             card_no,
             '9',
             ' ',
             nvl(oppost_date,to_char(sysdate,'yyyymmdd')),
             '00000',
             to_char(sysdate,'yyyymmdd'),
             'Y',
             sysdate,
             'trigger'
        from tsc_card
       where card_no   = new.card_no
         and crt_date  = wk_create_date;--

 ELSEIF (nvl(new.current_code,'0') = '0' AND nvl(old.current_code,'0') <> '0') THEN
    DELETE TSC_CDNR_LOG
     WHERE card_no        = new.card_no
       AND TSC_EMBOSS_RSN = '9'
       AND PROC_FLAG      = 'N';--

 END IF;--
 
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_IPS_ACT_ACNO_U
AFTER UPDATE OF
   bill_sending_addr1,
   bill_sending_addr2,
   bill_sending_addr3,
   bill_sending_addr4,
   bill_sending_addr5
 ON ACT_ACNO
 REFERENCING OLD AS old NEW AS new
 FOR EACH ROW MODE DB2SQL
WHEN (
        NVL(new.bill_sending_addr1,' ') <> NVL(old.bill_sending_addr1,' ') or
        NVL(new.bill_sending_addr2,' ') <> NVL(old.bill_sending_addr2,' ') or
        NVL(new.bill_sending_addr3,' ') <> NVL(old.bill_sending_addr3,' ') or
        NVL(new.bill_sending_addr4,' ') <> NVL(old.bill_sending_addr4,' ') or
        NVL(new.bill_sending_addr5,' ') <> NVL(old.bill_sending_addr5,' ')
      )
BEGIN
DECLARE h_ips_card_no varchar(19);--
DECLARE h_card_no varchar(19);--
--DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--
  DECLARE END_CL INT DEFAULT 0;--
  DECLARE cursv CURSOR FOR
  --CURSOR cursv IS
  select ips_card_no,b.card_no
    --into h_ips_card_no,h_card_no
    from crd_card a,ips_card b
   where a.card_no = b.card_no
     and a.id_p_seqno = NEW.id_p_seqno
     and b.current_code = '0'
     and b.blacklt_date = ''
     and b.lock_date    = ''
     and b.return_date  = '';--
DECLARE CONTINUE HANDLER FOR NOT FOUND SET END_CL = 1;--
--FETCH cursv  INTO h_ips_card_no, h_card_no;--

  IF UPDATING THEN
   BEGIN
      OPEN cursv;--
      FETCH cursv
      INTO h_ips_card_no, h_card_no;--
      WHILE END_CL = 0 DO
          INSERT INTO IPS_B2I005_LOG(
          crt_date,
          crt_time,
          ips_card_no,
          card_no,
          id_p_seqno,
          --id,
          --id_code,
          trans_type,
          proc_flag,
          mod_time,
          mod_pgm)
          values (to_char(sysdate,'yyyymmdd'),
          to_char(sysdate,'hh24miss'),
          h_ips_card_no,
          h_card_no,
          new.id_p_seqno,
          --new.acct_holder_id,
          --NEW.acct_holder_id_code,
          'C',
          'N',
          sysdate,
          'tr_ips_act_acno');--
      --<< NEXT >>
              FETCH cursv
                INTO h_ips_card_no, h_card_no;--
       END WHILE;--
      
      CLOSE cursv;--
    END;--
  END IF;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_IPS_CRD_IDNO_U
AFTER UPDATE OF
      chi_name,
      Birthday,
      e_mail_addr,
      home_tel_no1,
      home_tel_no2,
      cellar_phone,
      other_cntry_code
ON CRD_IDNO
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL

WHEN (
 NVL(OLD.chi_name,' ')         <> NVL(NEW.chi_name,' ')     OR
 NVL(OLD.Birthday,' ')         <> NVL(NEW.Birthday,' ')     OR
 NVL(OLD.e_mail_addr,' ')      <> NVL(NEW.e_mail_addr,' ')  OR
 NVL(OLD.home_tel_no1,' ')     <> NVL(NEW.home_tel_no1,' ') OR
 NVL(OLD.home_tel_no2,' ')     <> NVL(NEW.home_tel_no2,' ') OR
 NVL(OLD.cellar_phone,' ')     <> NVL(NEW.cellar_phone,' ') OR
 NVL(OLD.other_cntry_code,' ') <> NVL(NEW.other_cntry_code,' ')
      )
	  
BEGIN
DECLARE wk_audcode          VARCHAR(1);--
DECLARE wk_user             VARCHAR(10);--
DECLARE wk_pgm              VARCHAR(20);--
DECLARE wk_ws               VARCHAR(20);--
DECLARE wk_count            INTEGER;--
DECLARE h_ips_card_no       varchar(19);--
DECLARE h_card_no           varchar(19);--
--DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

DECLARE END_CL INT DEFAULT 0;--
DECLARE cursv CURSOR FOR
  --CURSOR cursv IS
  select ips_card_no,b.card_no
  --  into h_ips_card_no,h_card_no
    from crd_card a,ips_card b
   where a.card_no = b.card_no
     and a.id_p_seqno = new.id_p_seqno
     and b.current_code = '0'
     and b.blacklt_date =  ''
     and b.lock_date    =  ''
     and b.return_date  =  '';--
DECLARE CONTINUE HANDLER FOR NOT FOUND SET END_CL = 1;--


  IF UPDATING THEN
   BEGIN
      OPEN cursv;--
   FETCH cursv
      INTO h_ips_card_no, h_card_no;--
   WHILE END_CL = 0 DO
 -- while (cursv%FOUND) LOOP
      INSERT INTO IPS_B2I005_LOG(
      crt_date,
      crt_time,
      ips_card_no,
      card_no,
      id_p_seqno,
      trans_type,
      proc_flag,
      mod_time,
      mod_pgm)
      values (to_char(sysdate,'yyyymmdd'),
      to_char(sysdate,'hh24miss'),
      h_ips_card_no,
      h_card_no,
      new.id_p_seqno,
      'C',
      'N',
      sysdate,
      'tr_ips_crd_idno');--
  -- << NEXT >>
          FETCH cursv
             INTO h_ips_card_no, h_card_no;--
	END WHILE;--
      --END LOOP;--
      CLOSE cursv;--
 END;--
  END IF;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_JCIC_KK2_U
  AFTER UPDATE OF LINE_OF_CREDIT_AMT
  ON ACT_ACNO 
  REFERENCING OLD AS OLD NEW AS NEW
  FOR EACH ROW MODE DB2SQL
    WHEN (
          nvl(old.line_of_credit_amt , 0) <> nvl(new.line_of_credit_amt , 0)
         )
BEGIN
  DECLARE  wk_audcode       VARCHAR(1);--
  DECLARE  wk_modtime       VARCHAR(8);--
  DECLARE  wk_card_no       VARCHAR(19);--
  DECLARE  wk_current_code  VARCHAR(1);--
  DECLARE  wk_oppost_reason VARCHAR(4);--
  DECLARE  wk_oppost_date   VARCHAR(8);--
  DECLARE  wk_payment_date  VARCHAR(8);--

DECLARE step_flag         varchar(2);--

  DECLARE END_CL INT DEFAULT 0;--
  DECLARE c_email CURSOR FOR
--CURSOR  c_email  IS
        select card_no
             , current_code
             , oppost_reason
             , oppost_date
          from crd_card
         where p_seqno  = new.p_seqno;--
DECLARE CONTINUE HANDLER FOR NOT FOUND SET END_CL = 1;--



  set step_flag    = '0';--

  BEGIN
    OPEN  c_email;--
    FETCH c_email
          into wk_card_no
             , wk_current_code
             , wk_oppost_reason
             , wk_oppost_date;--

   -- if(c_email%NOTFOUND) then
   --    step_flag    = '1';--
   -- end if;--

   -- while (c_email%FOUND) LOOP
    WHILE END_CL = 0 DO
        set wk_audcode = 'C';--

        set wk_payment_date = '';--
        IF wk_oppost_reason = 'U' then
           IF new.debt_close_date <> '' then
              set wk_payment_date = new.debt_close_date;--
           END IF;--
        END IF;--

        if UPDATING then
                INSERT INTO crd_jcic_kk2(
                       card_no,
                       crt_date,
                       trans_type,
                       current_code,
                       oppost_reason,
                       oppost_date,
                       payment_date,
                       mod_time,
                       mod_pgm       )
                VALUES (
                        wk_card_no,
                        to_char(sysdate,'yyyymmdd'),
                        wk_audcode,
                        wk_current_code,
                        wk_oppost_reason,
                        wk_oppost_date,
                        wk_payment_date,
                        sysdate,
                        'tri_crd_acno_kk2');--
        end if;--

      FETCH c_email
            INTO wk_card_no
             , wk_current_code
             , wk_oppost_reason
             , wk_oppost_date;--
    END WHILE;--
    

    CLOSE c_email;--

  END;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LIAD_REMOD_LIQU_A
AFTER 
   INSERT 
ON COL_LIAD_LIQUIDATE
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL
WHEN (
      NVL(old.liqu_status,' ')<>NVL(new.liqu_status,' ')
  AND NVL(new.apr_flag,'N')='Y'
     )
BEGIN
 DECLARE  ll_cnt INTEGER;--
 DECLARE  ls_aud_flag varchar(1);--
 DECLARE  ls_recv_date   varchar(8);--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

/******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2010/06/28  JHW              1. Created this trigger.
******************************************************************************/

   set ls_aud_flag    = '';--
   set ll_cnt         =0;--
   set ls_recv_date   ='';--

   begin
      select max(recv_date) into ls_recv_date
        from col_liad_liquidate
       where id_no =new.id_no
         and case_letter =new.case_letter;--
  
   end;--
   if nvl(new.recv_date,' ')<nvl(ls_recv_date,' ') then
      return;--
   end if;--

   INSERT INTO col_liad_remod
            ( id_no,
              liad_doc_no,
              liad_type,
              liad_status,
              mod_time,
              mod_pgm )
     VALUES ( NEW.id_no,
              '',
              '4',
              new.liqu_status,
              sysdate,
              new.mod_pgm ) ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LIAD_REMOD_LIQU_U
AFTER 
   UPDATE
ON COL_LIAD_LIQUIDATE
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL
WHEN (
      NVL(old.liqu_status,' ')<>NVL(new.liqu_status,' ')
  AND NVL(new.apr_flag,'N')='Y'
     )
BEGIN
 DECLARE  ll_cnt INTEGER;--
 DECLARE  ls_aud_flag varchar(1);--
 DECLARE  ls_recv_date   varchar(8);--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

/******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2010/06/28  JHW              1. Created this trigger.
******************************************************************************/

   set ls_aud_flag    = '';--
   set ll_cnt         =0;--
   set ls_recv_date   ='';--

   begin
      select max(recv_date) into ls_recv_date
        from col_liad_liquidate
       where id_no =new.id_no
         and case_letter =new.case_letter;--
  
   end;--
   if nvl(new.recv_date,' ')<nvl(ls_recv_date,' ') then
      return;--
   end if;--

   INSERT INTO col_liad_remod
            ( id_no,
              liad_doc_no,
              liad_type,
              liad_status,
              mod_time,
              mod_pgm )
     VALUES ( NEW.id_no,
              '',
              '4',
              new.liqu_status,
              sysdate,
              new.mod_pgm ) ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LIAD_REMOD_RENEW_A
AFTER 
   INSERT 
ON COL_LIAD_RENEW
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL
WHEN (
      NVL(old.RENEW_STATUS,' ')<>NVL(new.RENEW_STATUS,' ')
 AND  NVL(new.apr_flag,'N')='Y'
     )

BEGIN
 DECLARE  ll_cnt INTEGER;--
 DECLARE  ls_aud_flag varchar(1);--
 DECLARE  ls_recv_date   varchar(8);--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

/******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2010/06/28  JHW              1. Created this trigger.
******************************************************************************/

   set ls_aud_flag = '';--
   set ll_cnt      =0;--
   set ls_recv_date ='';--

   begin
      select max(recv_date) into ls_recv_date
        from col_liad_renew
       where id_no =new.id_no
         and case_letter =new.case_letter;--
   
   end;--
   if nvl(new.recv_date,' ')<nvl(ls_recv_date,' ') then
      return;--
   end if;--

   INSERT INTO col_liad_remod
            ( id_no,
              liad_type,
              liad_status,
              liad_doc_no,
              mod_time,
              mod_pgm )
     VALUES ( NEW.id_no,
              '3',
              new.renew_status,
              '',
              sysdate,
              new.mod_pgm ) ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LIAD_REMOD_RENEW_U
AFTER 
   UPDATE
ON COL_LIAD_RENEW
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL
WHEN (
      NVL(old.RENEW_STATUS,' ')<>NVL(new.RENEW_STATUS,' ')
 AND  NVL(new.apr_flag,'N')='Y'
     )

BEGIN
 DECLARE  ll_cnt INTEGER;--
 DECLARE  ls_aud_flag varchar(1);--
 DECLARE  ls_recv_date   varchar(8);--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

/******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2010/06/28  JHW              1. Created this trigger.
******************************************************************************/

   set ls_aud_flag = '';--
   set ll_cnt      =0;--
   set ls_recv_date ='';--

   begin
      select max(recv_date) into ls_recv_date
        from col_liad_renew
       where id_no =new.id_no
         and case_letter =new.case_letter;--
   
   end;--
   if nvl(new.recv_date,' ')<nvl(ls_recv_date,' ') then
      return;--
   end if;--

   INSERT INTO col_liad_remod
            ( id_no,
              liad_type,
              liad_status,
              liad_doc_no,
              mod_time,
              mod_pgm )
     VALUES ( NEW.id_no,
              '3',
              new.renew_status,
              '',
              sysdate,
              new.mod_pgm ) ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_ACAG_A AFTER
INSERT
    ON
    ACT_ACAG REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_ACT_ACAG( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_P_SEQNO ,
    PAY_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.P_SEQNO ,
NEW.PAY_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_ACAG_D AFTER
DELETE
    ON
    ACT_ACAG REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_ACT_ACAG( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_P_SEQNO ,
    PAY_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.P_SEQNO ,
OLD.PAY_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_ACAG_U AFTER
UPDATE
    OF PAY_AMT ON
    ACT_ACAG REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_ACT_ACAG( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_P_SEQNO ,
    PAY_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.P_SEQNO ,
NEW.PAY_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_ACCT_A AFTER
INSERT
    ON
    ACT_ACCT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_ACT_ACCT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_P_SEQNO
    --,ACCT_HOLDER_ID
,
    END_BAL_OP ,
    BEG_BAL_LK ,
    END_BAL_LK ,
    OVERPAY_LOCK_STA_DATE ,
    OVERPAY_LOCK_DUE_DATE ,
    PAY_BY_STAGE_AMT ,
    PAY_BY_STAGE_BAL ,
    PAY_BY_STAGE_DATE ,
    UPDATE_DATE ,
    UPDATE_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.P_SEQNO
--,NEW.ACCT_HOLDER_ID
,
NEW.END_BAL_OP ,
NEW.BEG_BAL_LK ,
NEW.END_BAL_LK ,
NEW.OVERPAY_LOCK_STA_DATE ,
NEW.OVERPAY_LOCK_DUE_DATE ,
NEW.PAY_BY_STAGE_AMT ,
NEW.PAY_BY_STAGE_BAL ,
NEW.PAY_BY_STAGE_DATE ,
NEW.UPDATE_DATE ,
NEW.UPDATE_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_ACCT_D AFTER
DELETE
    ON
    ACT_ACCT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_ACT_ACCT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_P_SEQNO
    -- ,ACCT_HOLDER_ID
,
    END_BAL_OP ,
    BEG_BAL_LK ,
    END_BAL_LK ,
    OVERPAY_LOCK_STA_DATE ,
    OVERPAY_LOCK_DUE_DATE ,
    PAY_BY_STAGE_AMT ,
    PAY_BY_STAGE_BAL ,
    PAY_BY_STAGE_DATE ,
    UPDATE_DATE ,
    UPDATE_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.P_SEQNO
-- ,OLD.ACCT_HOLDER_ID
,
OLD.END_BAL_OP ,
OLD.BEG_BAL_LK ,
OLD.END_BAL_LK ,
OLD.OVERPAY_LOCK_STA_DATE ,
OLD.OVERPAY_LOCK_DUE_DATE ,
OLD.PAY_BY_STAGE_AMT ,
OLD.PAY_BY_STAGE_BAL ,
OLD.PAY_BY_STAGE_DATE ,
OLD.UPDATE_DATE ,
OLD.UPDATE_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_ACCT_U AFTER
UPDATE
    OF END_BAL_OP ,
    BEG_BAL_LK ,
    END_BAL_LK ,
    OVERPAY_LOCK_STA_DATE ,
    OVERPAY_LOCK_DUE_DATE ,
    PAY_BY_STAGE_AMT ,
    PAY_BY_STAGE_BAL ,
    PAY_BY_STAGE_DATE ,
    UPDATE_DATE ,
    UPDATE_USER ON
    ACT_ACCT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_ACT_ACCT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_P_SEQNO
    --,ACCT_HOLDER_ID
,
    END_BAL_OP ,
    BEG_BAL_LK ,
    END_BAL_LK ,
    OVERPAY_LOCK_STA_DATE ,
    OVERPAY_LOCK_DUE_DATE ,
    PAY_BY_STAGE_AMT ,
    PAY_BY_STAGE_BAL ,
    PAY_BY_STAGE_DATE ,
    UPDATE_DATE ,
    UPDATE_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.P_SEQNO
--,NEW.ACCT_HOLDER_ID
,
NEW.END_BAL_OP ,
NEW.BEG_BAL_LK ,
NEW.END_BAL_LK ,
NEW.OVERPAY_LOCK_STA_DATE ,
NEW.OVERPAY_LOCK_DUE_DATE ,
NEW.PAY_BY_STAGE_AMT ,
NEW.PAY_BY_STAGE_BAL ,
NEW.PAY_BY_STAGE_DATE ,
NEW.UPDATE_DATE ,
NEW.UPDATE_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_ACNO_A AFTER
INSERT
    ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_ACT_ACNO( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACNO_P_SEQNO
    --,CORP_NO
,
    ACCT_STATUS ,
    NO_BLOCK_FLAG ,
    NO_BLOCK_S_DATE ,
    NO_BLOCK_E_DATE ,
    NO_UNBLOCK_FLAG ,
    NO_UNBLOCK_S_DATE ,
    NO_UNBLOCK_E_DATE ,
    RC_USE_B_ADJ ,
    RC_USE_INDICATOR ,
    RC_USE_S_DATE ,
    RC_USE_E_DATE ,
    RC_USE_CHANGE_DATE ,
    RC_USE_REASON_CODE ,
    NO_INTEREST_FLAG ,
    NO_INTEREST_S_MONTH ,
    NO_INTEREST_E_MONTH ,
    NO_PENALTY_FLAG ,
    NO_PENALTY_S_MONTH ,
    NO_PENALTY_E_MONTH ,
    SPECIAL_STAT_CODE ,
    SPECIAL_STAT_DIVISION ,
    SPECIAL_STAT_FEE ,
    SPECIAL_STAT_S_MONTH ,
    SPECIAL_STAT_E_MONTH ,
    STAT_UNPRINT_FLAG ,
    STAT_UNPRINT_S_MONTH ,
    STAT_UNPRINT_E_MONTH ,
    STAT_SEND_INTERNET ,
    STAT_SEND_FAX ,
    STAT_SEND_S_MONTH ,
    STAT_SEND_E_MONTH ,
    NO_CANCEL_DEBT_FLAG ,
    NO_CANCEL_DEBT_S_DATE ,
    NO_CANCEL_DEBT_E_DATE ,
    PAY_BY_STAGE_FLAG ,
    NO_TEL_COLL_FLAG ,
    NO_TEL_COLL_S_DATE ,
    NO_TEL_COLL_E_DATE ,
    NO_PER_COLL_FLAG ,
    NO_PER_COLL_S_DATE ,
    NO_PER_COLL_E_DATE ,
    NO_DELINQUENT_FLAG ,
    NO_DELINQUENT_S_DATE ,
    NO_DELINQUENT_E_DATE ,
    NO_COLLECTION_FLAG ,
    NO_COLLECTION_S_DATE ,
    NO_COLLECTION_E_DATE ,
    LAWSUIT_PROCESS_LOG ,
    CREDIT_ACT_NO ,
    LAST_ACCT_STATUS ,
    LAST_ACCT_SUB_STATUS ,
    NO_ADJ_LOC_HIGH ,
    NO_ADJ_LOC_HIGH_S_DATE ,
    NO_ADJ_LOC_HIGH_E_DATE ,
    NO_ADJ_LOC_LOW ,
    NO_ADJ_LOC_LOW_S_DATE ,
    NO_ADJ_LOC_LOW_E_DATE ,
    NO_F_STOP_FLAG ,
    NO_F_STOP_S_DATE ,
    NO_F_STOP_E_DATE ,
    H_ADJ_LOC_HIGH_DATE ,
    H_ADJ_LOC_LOW_DATE ,
    ADJ_LOC_HIGH_T ,
    ADJ_LOC_LOW_T ,
    LINE_OF_CREDIT_AMT ,
    INST_AUTH_LOC_AMT ,
    ADJ_BEFORE_LOC_AMT ,
    REVOLVE_INT_SIGN ,
    REVOLVE_INT_RATE ,
    REVOLVE_RATE_S_MONTH ,
    REVOLVE_RATE_E_MONTH ,
    PENALTY_SIGN ,
    PENALTY_RATE ,
    PENALTY_RATE_S_MONTH ,
    PENALTY_RATE_E_MONTH ,
    MIN_PAY_RATE ,
    MIN_PAY_RATE_S_MONTH ,
    MIN_PAY_RATE_E_MONTH ,
    AUTOPAY_INDICATOR ,
    AUTOPAY_RATE ,
    AUTOPAY_FIX_AMT ,
    AUTOPAY_ACCT_BANK ,
    AUTOPAY_ACCT_NO ,
    AUTOPAY_ID ,
    AUTOPAY_ID_CODE ,
    AUTOPAY_ACCT_S_DATE ,
    AUTOPAY_ACCT_E_DATE ,
    WORSE_MCODE ,
    BILL_SENDING_ZIP ,
    BILL_SENDING_ADDR1 ,
    BILL_SENDING_ADDR2 ,
    BILL_SENDING_ADDR3 ,
    BILL_SENDING_ADDR4 ,
    BILL_SENDING_ADDR5 ,
    CHG_ADDR_DATE ,
    ACCEPT_DM ,
    CORP_ASSURE_FLAG ,
    SPECIAL_COMMENT ,
    CORP_ACT_FLAG ,
    LOST_FEE_FLAG ,
    BANK_REL_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ACNO_P_SEQNO
--,NEW.CORP_NO
,
NEW.ACCT_STATUS ,
NEW.NO_BLOCK_FLAG ,
NEW.NO_BLOCK_S_DATE ,
NEW.NO_BLOCK_E_DATE ,
NEW.NO_UNBLOCK_FLAG ,
NEW.NO_UNBLOCK_S_DATE ,
NEW.NO_UNBLOCK_E_DATE ,
NEW.RC_USE_B_ADJ ,
NEW.RC_USE_INDICATOR ,
NEW.RC_USE_S_DATE ,
NEW.RC_USE_E_DATE ,
NEW.RC_USE_CHANGE_DATE ,
NEW.RC_USE_REASON_CODE ,
NEW.NO_INTEREST_FLAG ,
NEW.NO_INTEREST_S_MONTH ,
NEW.NO_INTEREST_E_MONTH ,
NEW.NO_PENALTY_FLAG ,
NEW.NO_PENALTY_S_MONTH ,
NEW.NO_PENALTY_E_MONTH ,
NEW.SPECIAL_STAT_CODE ,
NEW.SPECIAL_STAT_DIVISION ,
NEW.SPECIAL_STAT_FEE ,
NEW.SPECIAL_STAT_S_MONTH ,
NEW.SPECIAL_STAT_E_MONTH ,
NEW.STAT_UNPRINT_FLAG ,
NEW.STAT_UNPRINT_S_MONTH ,
NEW.STAT_UNPRINT_E_MONTH ,
NEW.STAT_SEND_INTERNET ,
NEW.STAT_SEND_FAX ,
NEW.STAT_SEND_S_MONTH ,
NEW.STAT_SEND_E_MONTH ,
NEW.NO_CANCEL_DEBT_FLAG ,
NEW.NO_CANCEL_DEBT_S_DATE ,
NEW.NO_CANCEL_DEBT_E_DATE ,
NEW.PAY_BY_STAGE_FLAG ,
NEW.NO_TEL_COLL_FLAG ,
NEW.NO_TEL_COLL_S_DATE ,
NEW.NO_TEL_COLL_E_DATE ,
NEW.NO_PER_COLL_FLAG ,
NEW.NO_PER_COLL_S_DATE ,
NEW.NO_PER_COLL_E_DATE ,
NEW.NO_DELINQUENT_FLAG ,
NEW.NO_DELINQUENT_S_DATE ,
NEW.NO_DELINQUENT_E_DATE ,
NEW.NO_COLLECTION_FLAG ,
NEW.NO_COLLECTION_S_DATE ,
NEW.NO_COLLECTION_E_DATE ,
NEW.LAWSUIT_PROCESS_LOG ,
NEW.CREDIT_ACT_NO ,
NEW.LAST_ACCT_STATUS ,
NEW.LAST_ACCT_SUB_STATUS ,
NEW.NO_ADJ_LOC_HIGH ,
NEW.NO_ADJ_LOC_HIGH_S_DATE ,
NEW.NO_ADJ_LOC_HIGH_E_DATE ,
NEW.NO_ADJ_LOC_LOW ,
NEW.NO_ADJ_LOC_LOW_S_DATE ,
NEW.NO_ADJ_LOC_LOW_E_DATE ,
NEW.NO_F_STOP_FLAG ,
NEW.NO_F_STOP_S_DATE ,
NEW.NO_F_STOP_E_DATE ,
NEW.H_ADJ_LOC_HIGH_DATE ,
NEW.H_ADJ_LOC_LOW_DATE ,
NEW.ADJ_LOC_HIGH_T ,
NEW.ADJ_LOC_LOW_T ,
NEW.LINE_OF_CREDIT_AMT ,
NEW.INST_AUTH_LOC_AMT ,
NEW.ADJ_BEFORE_LOC_AMT ,
NEW.REVOLVE_INT_SIGN ,
NEW.REVOLVE_INT_RATE ,
NEW.REVOLVE_RATE_S_MONTH ,
NEW.REVOLVE_RATE_E_MONTH ,
NEW.PENALTY_SIGN ,
NEW.PENALTY_RATE ,
NEW.PENALTY_RATE_S_MONTH ,
NEW.PENALTY_RATE_E_MONTH ,
NEW.MIN_PAY_RATE ,
NEW.MIN_PAY_RATE_S_MONTH ,
NEW.MIN_PAY_RATE_E_MONTH ,
NEW.AUTOPAY_INDICATOR ,
NEW.AUTOPAY_RATE ,
NEW.AUTOPAY_FIX_AMT ,
NEW.AUTOPAY_ACCT_BANK ,
NEW.AUTOPAY_ACCT_NO ,
NEW.AUTOPAY_ID ,
NEW.AUTOPAY_ID_CODE ,
NEW.AUTOPAY_ACCT_S_DATE ,
NEW.AUTOPAY_ACCT_E_DATE ,
NEW.WORSE_MCODE ,
NEW.BILL_SENDING_ZIP ,
NEW.BILL_SENDING_ADDR1 ,
NEW.BILL_SENDING_ADDR2 ,
NEW.BILL_SENDING_ADDR3 ,
NEW.BILL_SENDING_ADDR4 ,
NEW.BILL_SENDING_ADDR5 ,
NEW.CHG_ADDR_DATE ,
NEW.ACCEPT_DM ,
NEW.CORP_ASSURE_FLAG ,
NEW.SPECIAL_COMMENT ,
NEW.CORP_ACT_FLAG ,
NEW.LOST_FEE_FLAG ,
NEW.BANK_REL_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_ACNO_D AFTER
DELETE
    ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_ACT_ACNO( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACNO_P_SEQNO
    -- ,CORP_NO
,
    ACCT_STATUS ,
    NO_BLOCK_FLAG ,
    NO_BLOCK_S_DATE ,
    NO_BLOCK_E_DATE ,
    NO_UNBLOCK_FLAG ,
    NO_UNBLOCK_S_DATE ,
    NO_UNBLOCK_E_DATE ,
    RC_USE_B_ADJ ,
    RC_USE_INDICATOR ,
    RC_USE_S_DATE ,
    RC_USE_E_DATE ,
    RC_USE_CHANGE_DATE ,
    RC_USE_REASON_CODE ,
    NO_INTEREST_FLAG ,
    NO_INTEREST_S_MONTH ,
    NO_INTEREST_E_MONTH ,
    NO_PENALTY_FLAG ,
    NO_PENALTY_S_MONTH ,
    NO_PENALTY_E_MONTH ,
    SPECIAL_STAT_CODE ,
    SPECIAL_STAT_DIVISION ,
    SPECIAL_STAT_FEE ,
    SPECIAL_STAT_S_MONTH ,
    SPECIAL_STAT_E_MONTH ,
    STAT_UNPRINT_FLAG ,
    STAT_UNPRINT_S_MONTH ,
    STAT_UNPRINT_E_MONTH ,
    STAT_SEND_INTERNET ,
    STAT_SEND_FAX ,
    STAT_SEND_S_MONTH ,
    STAT_SEND_E_MONTH ,
    NO_CANCEL_DEBT_FLAG ,
    NO_CANCEL_DEBT_S_DATE ,
    NO_CANCEL_DEBT_E_DATE ,
    PAY_BY_STAGE_FLAG ,
    NO_TEL_COLL_FLAG ,
    NO_TEL_COLL_S_DATE ,
    NO_TEL_COLL_E_DATE ,
    NO_PER_COLL_FLAG ,
    NO_PER_COLL_S_DATE ,
    NO_PER_COLL_E_DATE ,
    NO_DELINQUENT_FLAG ,
    NO_DELINQUENT_S_DATE ,
    NO_DELINQUENT_E_DATE ,
    NO_COLLECTION_FLAG ,
    NO_COLLECTION_S_DATE ,
    NO_COLLECTION_E_DATE ,
    LAWSUIT_PROCESS_LOG ,
    CREDIT_ACT_NO ,
    LAST_ACCT_STATUS ,
    LAST_ACCT_SUB_STATUS ,
    NO_ADJ_LOC_HIGH ,
    NO_ADJ_LOC_HIGH_S_DATE ,
    NO_ADJ_LOC_HIGH_E_DATE ,
    NO_ADJ_LOC_LOW ,
    NO_ADJ_LOC_LOW_S_DATE ,
    NO_ADJ_LOC_LOW_E_DATE ,
    NO_F_STOP_FLAG ,
    NO_F_STOP_S_DATE ,
    NO_F_STOP_E_DATE ,
    H_ADJ_LOC_HIGH_DATE ,
    H_ADJ_LOC_LOW_DATE ,
    ADJ_LOC_HIGH_T ,
    ADJ_LOC_LOW_T ,
    LINE_OF_CREDIT_AMT ,
    INST_AUTH_LOC_AMT ,
    ADJ_BEFORE_LOC_AMT ,
    REVOLVE_INT_SIGN ,
    REVOLVE_INT_RATE ,
    REVOLVE_RATE_S_MONTH ,
    REVOLVE_RATE_E_MONTH ,
    PENALTY_SIGN ,
    PENALTY_RATE ,
    PENALTY_RATE_S_MONTH ,
    PENALTY_RATE_E_MONTH ,
    MIN_PAY_RATE ,
    MIN_PAY_RATE_S_MONTH ,
    MIN_PAY_RATE_E_MONTH ,
    AUTOPAY_INDICATOR ,
    AUTOPAY_RATE ,
    AUTOPAY_FIX_AMT ,
    AUTOPAY_ACCT_BANK ,
    AUTOPAY_ACCT_NO ,
    AUTOPAY_ID ,
    AUTOPAY_ID_CODE ,
    AUTOPAY_ACCT_S_DATE ,
    AUTOPAY_ACCT_E_DATE ,
    WORSE_MCODE ,
    BILL_SENDING_ZIP ,
    BILL_SENDING_ADDR1 ,
    BILL_SENDING_ADDR2 ,
    BILL_SENDING_ADDR3 ,
    BILL_SENDING_ADDR4 ,
    BILL_SENDING_ADDR5 ,
    CHG_ADDR_DATE ,
    ACCEPT_DM ,
    CORP_ASSURE_FLAG ,
    SPECIAL_COMMENT ,
    CORP_ACT_FLAG ,
    LOST_FEE_FLAG ,
    BANK_REL_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.ACNO_P_SEQNO
--,OLD.CORP_NO
,
OLD.ACCT_STATUS ,
OLD.NO_BLOCK_FLAG ,
OLD.NO_BLOCK_S_DATE ,
OLD.NO_BLOCK_E_DATE ,
OLD.NO_UNBLOCK_FLAG ,
OLD.NO_UNBLOCK_S_DATE ,
OLD.NO_UNBLOCK_E_DATE ,
OLD.RC_USE_B_ADJ ,
OLD.RC_USE_INDICATOR ,
OLD.RC_USE_S_DATE ,
OLD.RC_USE_E_DATE ,
OLD.RC_USE_CHANGE_DATE ,
OLD.RC_USE_REASON_CODE ,
OLD.NO_INTEREST_FLAG ,
OLD.NO_INTEREST_S_MONTH ,
OLD.NO_INTEREST_E_MONTH ,
OLD.NO_PENALTY_FLAG ,
OLD.NO_PENALTY_S_MONTH ,
OLD.NO_PENALTY_E_MONTH ,
OLD.SPECIAL_STAT_CODE ,
OLD.SPECIAL_STAT_DIVISION ,
OLD.SPECIAL_STAT_FEE ,
OLD.SPECIAL_STAT_S_MONTH ,
OLD.SPECIAL_STAT_E_MONTH ,
OLD.STAT_UNPRINT_FLAG ,
OLD.STAT_UNPRINT_S_MONTH ,
OLD.STAT_UNPRINT_E_MONTH ,
OLD.STAT_SEND_INTERNET ,
OLD.STAT_SEND_FAX ,
OLD.STAT_SEND_S_MONTH ,
OLD.STAT_SEND_E_MONTH ,
OLD.NO_CANCEL_DEBT_FLAG ,
OLD.NO_CANCEL_DEBT_S_DATE ,
OLD.NO_CANCEL_DEBT_E_DATE ,
OLD.PAY_BY_STAGE_FLAG ,
OLD.NO_TEL_COLL_FLAG ,
OLD.NO_TEL_COLL_S_DATE ,
OLD.NO_TEL_COLL_E_DATE ,
OLD.NO_PER_COLL_FLAG ,
OLD.NO_PER_COLL_S_DATE ,
OLD.NO_PER_COLL_E_DATE ,
OLD.NO_DELINQUENT_FLAG ,
OLD.NO_DELINQUENT_S_DATE ,
OLD.NO_DELINQUENT_E_DATE ,
OLD.NO_COLLECTION_FLAG ,
OLD.NO_COLLECTION_S_DATE ,
OLD.NO_COLLECTION_E_DATE ,
OLD.LAWSUIT_PROCESS_LOG ,
OLD.CREDIT_ACT_NO ,
OLD.LAST_ACCT_STATUS ,
OLD.LAST_ACCT_SUB_STATUS ,
OLD.NO_ADJ_LOC_HIGH ,
OLD.NO_ADJ_LOC_HIGH_S_DATE ,
OLD.NO_ADJ_LOC_HIGH_E_DATE ,
OLD.NO_ADJ_LOC_LOW ,
OLD.NO_ADJ_LOC_LOW_S_DATE ,
OLD.NO_ADJ_LOC_LOW_E_DATE ,
OLD.NO_F_STOP_FLAG ,
OLD.NO_F_STOP_S_DATE ,
OLD.NO_F_STOP_E_DATE ,
OLD.H_ADJ_LOC_HIGH_DATE ,
OLD.H_ADJ_LOC_LOW_DATE ,
OLD.ADJ_LOC_HIGH_T ,
OLD.ADJ_LOC_LOW_T ,
OLD.LINE_OF_CREDIT_AMT ,
OLD.INST_AUTH_LOC_AMT ,
OLD.ADJ_BEFORE_LOC_AMT ,
OLD.REVOLVE_INT_SIGN ,
OLD.REVOLVE_INT_RATE ,
OLD.REVOLVE_RATE_S_MONTH ,
OLD.REVOLVE_RATE_E_MONTH ,
OLD.PENALTY_SIGN ,
OLD.PENALTY_RATE ,
OLD.PENALTY_RATE_S_MONTH ,
OLD.PENALTY_RATE_E_MONTH ,
OLD.MIN_PAY_RATE ,
OLD.MIN_PAY_RATE_S_MONTH ,
OLD.MIN_PAY_RATE_E_MONTH ,
OLD.AUTOPAY_INDICATOR ,
OLD.AUTOPAY_RATE ,
OLD.AUTOPAY_FIX_AMT ,
OLD.AUTOPAY_ACCT_BANK ,
OLD.AUTOPAY_ACCT_NO ,
OLD.AUTOPAY_ID ,
OLD.AUTOPAY_ID_CODE ,
OLD.AUTOPAY_ACCT_S_DATE ,
OLD.AUTOPAY_ACCT_E_DATE ,
OLD.WORSE_MCODE ,
OLD.BILL_SENDING_ZIP ,
OLD.BILL_SENDING_ADDR1 ,
OLD.BILL_SENDING_ADDR2 ,
OLD.BILL_SENDING_ADDR3 ,
OLD.BILL_SENDING_ADDR4 ,
OLD.BILL_SENDING_ADDR5 ,
OLD.CHG_ADDR_DATE ,
OLD.ACCEPT_DM ,
OLD.CORP_ASSURE_FLAG ,
OLD.SPECIAL_COMMENT ,
OLD.CORP_ACT_FLAG ,
OLD.LOST_FEE_FLAG ,
OLD.BANK_REL_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_ACNO_U AFTER
UPDATE
    OF
    --CORP_NO
 ACCT_STATUS ,
    NO_BLOCK_FLAG ,
    NO_BLOCK_S_DATE ,
    NO_BLOCK_E_DATE ,
    NO_UNBLOCK_FLAG ,
    NO_UNBLOCK_S_DATE ,
    NO_UNBLOCK_E_DATE ,
    RC_USE_B_ADJ ,
    RC_USE_INDICATOR ,
    RC_USE_S_DATE ,
    RC_USE_E_DATE ,
    RC_USE_CHANGE_DATE ,
    RC_USE_REASON_CODE ,
    NO_INTEREST_FLAG ,
    NO_INTEREST_S_MONTH ,
    NO_INTEREST_E_MONTH ,
    NO_PENALTY_FLAG ,
    NO_PENALTY_S_MONTH ,
    NO_PENALTY_E_MONTH ,
    SPECIAL_STAT_CODE ,
    SPECIAL_STAT_DIVISION ,
    SPECIAL_STAT_FEE ,
    SPECIAL_STAT_S_MONTH ,
    SPECIAL_STAT_E_MONTH ,
    STAT_UNPRINT_FLAG ,
    STAT_UNPRINT_S_MONTH ,
    STAT_UNPRINT_E_MONTH ,
    STAT_SEND_INTERNET ,
    STAT_SEND_FAX ,
    STAT_SEND_S_MONTH ,
    STAT_SEND_E_MONTH ,
    NO_CANCEL_DEBT_FLAG ,
    NO_CANCEL_DEBT_S_DATE ,
    NO_CANCEL_DEBT_E_DATE ,
    PAY_BY_STAGE_FLAG ,
    NO_TEL_COLL_FLAG ,
    NO_TEL_COLL_S_DATE ,
    NO_TEL_COLL_E_DATE ,
    NO_PER_COLL_FLAG ,
    NO_PER_COLL_S_DATE ,
    NO_PER_COLL_E_DATE ,
    NO_DELINQUENT_FLAG ,
    NO_DELINQUENT_S_DATE ,
    NO_DELINQUENT_E_DATE ,
    NO_COLLECTION_FLAG ,
    NO_COLLECTION_S_DATE ,
    NO_COLLECTION_E_DATE ,
    LAWSUIT_PROCESS_LOG ,
    CREDIT_ACT_NO ,
    LAST_ACCT_STATUS ,
    LAST_ACCT_SUB_STATUS ,
    NO_ADJ_LOC_HIGH ,
    NO_ADJ_LOC_HIGH_S_DATE ,
    NO_ADJ_LOC_HIGH_E_DATE ,
    NO_ADJ_LOC_LOW ,
    NO_ADJ_LOC_LOW_S_DATE ,
    NO_ADJ_LOC_LOW_E_DATE ,
    NO_F_STOP_FLAG ,
    NO_F_STOP_S_DATE ,
    NO_F_STOP_E_DATE ,
    H_ADJ_LOC_HIGH_DATE ,
    H_ADJ_LOC_LOW_DATE ,
    ADJ_LOC_HIGH_T ,
    ADJ_LOC_LOW_T ,
    LINE_OF_CREDIT_AMT ,
    INST_AUTH_LOC_AMT ,
    ADJ_BEFORE_LOC_AMT ,
    REVOLVE_INT_SIGN ,
    REVOLVE_INT_RATE ,
    REVOLVE_RATE_S_MONTH ,
    REVOLVE_RATE_E_MONTH ,
    PENALTY_SIGN ,
    PENALTY_RATE ,
    PENALTY_RATE_S_MONTH ,
    PENALTY_RATE_E_MONTH ,
    MIN_PAY_RATE ,
    MIN_PAY_RATE_S_MONTH ,
    MIN_PAY_RATE_E_MONTH ,
    AUTOPAY_INDICATOR ,
    AUTOPAY_RATE ,
    AUTOPAY_FIX_AMT ,
    AUTOPAY_ACCT_BANK ,
    AUTOPAY_ACCT_NO ,
    AUTOPAY_ID ,
    AUTOPAY_ID_CODE ,
    AUTOPAY_ACCT_S_DATE ,
    AUTOPAY_ACCT_E_DATE ,
    WORSE_MCODE ,
    BILL_SENDING_ZIP ,
    BILL_SENDING_ADDR1 ,
    BILL_SENDING_ADDR2 ,
    BILL_SENDING_ADDR3 ,
    BILL_SENDING_ADDR4 ,
    BILL_SENDING_ADDR5 ,
    CHG_ADDR_DATE ,
    ACCEPT_DM ,
    CORP_ASSURE_FLAG ,
    SPECIAL_COMMENT ,
    CORP_ACT_FLAG ,
    LOST_FEE_FLAG ,
    BANK_REL_FLAG ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 SET
wk_audcode = 'U';--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_ACT_ACNO( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACNO_P_SEQNO
    --,CORP_NO
,
    ACCT_STATUS ,
    NO_BLOCK_FLAG ,
    NO_BLOCK_S_DATE ,
    NO_BLOCK_E_DATE ,
    NO_UNBLOCK_FLAG ,
    NO_UNBLOCK_S_DATE ,
    NO_UNBLOCK_E_DATE ,
    RC_USE_B_ADJ ,
    RC_USE_INDICATOR ,
    RC_USE_S_DATE ,
    RC_USE_E_DATE ,
    RC_USE_CHANGE_DATE ,
    RC_USE_REASON_CODE ,
    NO_INTEREST_FLAG ,
    NO_INTEREST_S_MONTH ,
    NO_INTEREST_E_MONTH ,
    NO_PENALTY_FLAG ,
    NO_PENALTY_S_MONTH ,
    NO_PENALTY_E_MONTH ,
    SPECIAL_STAT_CODE ,
    SPECIAL_STAT_DIVISION ,
    SPECIAL_STAT_FEE ,
    SPECIAL_STAT_S_MONTH ,
    SPECIAL_STAT_E_MONTH ,
    STAT_UNPRINT_FLAG ,
    STAT_UNPRINT_S_MONTH ,
    STAT_UNPRINT_E_MONTH ,
    STAT_SEND_INTERNET ,
    STAT_SEND_FAX ,
    STAT_SEND_S_MONTH ,
    STAT_SEND_E_MONTH ,
    NO_CANCEL_DEBT_FLAG ,
    NO_CANCEL_DEBT_S_DATE ,
    NO_CANCEL_DEBT_E_DATE ,
    PAY_BY_STAGE_FLAG ,
    NO_TEL_COLL_FLAG ,
    NO_TEL_COLL_S_DATE ,
    NO_TEL_COLL_E_DATE ,
    NO_PER_COLL_FLAG ,
    NO_PER_COLL_S_DATE ,
    NO_PER_COLL_E_DATE ,
    NO_DELINQUENT_FLAG ,
    NO_DELINQUENT_S_DATE ,
    NO_DELINQUENT_E_DATE ,
    NO_COLLECTION_FLAG ,
    NO_COLLECTION_S_DATE ,
    NO_COLLECTION_E_DATE ,
    LAWSUIT_PROCESS_LOG ,
    CREDIT_ACT_NO ,
    LAST_ACCT_STATUS ,
    LAST_ACCT_SUB_STATUS ,
    NO_ADJ_LOC_HIGH ,
    NO_ADJ_LOC_HIGH_S_DATE ,
    NO_ADJ_LOC_HIGH_E_DATE ,
    NO_ADJ_LOC_LOW ,
    NO_ADJ_LOC_LOW_S_DATE ,
    NO_ADJ_LOC_LOW_E_DATE ,
    NO_F_STOP_FLAG ,
    NO_F_STOP_S_DATE ,
    NO_F_STOP_E_DATE ,
    H_ADJ_LOC_HIGH_DATE ,
    H_ADJ_LOC_LOW_DATE ,
    ADJ_LOC_HIGH_T ,
    ADJ_LOC_LOW_T ,
    LINE_OF_CREDIT_AMT ,
    INST_AUTH_LOC_AMT ,
    ADJ_BEFORE_LOC_AMT ,
    REVOLVE_INT_SIGN ,
    REVOLVE_INT_RATE ,
    REVOLVE_RATE_S_MONTH ,
    REVOLVE_RATE_E_MONTH ,
    PENALTY_SIGN ,
    PENALTY_RATE ,
    PENALTY_RATE_S_MONTH ,
    PENALTY_RATE_E_MONTH ,
    MIN_PAY_RATE ,
    MIN_PAY_RATE_S_MONTH ,
    MIN_PAY_RATE_E_MONTH ,
    AUTOPAY_INDICATOR ,
    AUTOPAY_RATE ,
    AUTOPAY_FIX_AMT ,
    AUTOPAY_ACCT_BANK ,
    AUTOPAY_ACCT_NO ,
    AUTOPAY_ID ,
    AUTOPAY_ID_CODE ,
    AUTOPAY_ACCT_S_DATE ,
    AUTOPAY_ACCT_E_DATE ,
    WORSE_MCODE ,
    BILL_SENDING_ZIP ,
    BILL_SENDING_ADDR1 ,
    BILL_SENDING_ADDR2 ,
    BILL_SENDING_ADDR3 ,
    BILL_SENDING_ADDR4 ,
    BILL_SENDING_ADDR5 ,
    CHG_ADDR_DATE ,
    ACCEPT_DM ,
    CORP_ASSURE_FLAG ,
    SPECIAL_COMMENT ,
    CORP_ACT_FLAG ,
    LOST_FEE_FLAG ,
    BANK_REL_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ACNO_P_SEQNO
--,NEW.CORP_NO
,
NEW.ACCT_STATUS ,
NEW.NO_BLOCK_FLAG ,
NEW.NO_BLOCK_S_DATE ,
NEW.NO_BLOCK_E_DATE ,
NEW.NO_UNBLOCK_FLAG ,
NEW.NO_UNBLOCK_S_DATE ,
NEW.NO_UNBLOCK_E_DATE ,
NEW.RC_USE_B_ADJ ,
NEW.RC_USE_INDICATOR ,
NEW.RC_USE_S_DATE ,
NEW.RC_USE_E_DATE ,
NEW.RC_USE_CHANGE_DATE ,
NEW.RC_USE_REASON_CODE ,
NEW.NO_INTEREST_FLAG ,
NEW.NO_INTEREST_S_MONTH ,
NEW.NO_INTEREST_E_MONTH ,
NEW.NO_PENALTY_FLAG ,
NEW.NO_PENALTY_S_MONTH ,
NEW.NO_PENALTY_E_MONTH ,
NEW.SPECIAL_STAT_CODE ,
NEW.SPECIAL_STAT_DIVISION ,
NEW.SPECIAL_STAT_FEE ,
NEW.SPECIAL_STAT_S_MONTH ,
NEW.SPECIAL_STAT_E_MONTH ,
NEW.STAT_UNPRINT_FLAG ,
NEW.STAT_UNPRINT_S_MONTH ,
NEW.STAT_UNPRINT_E_MONTH ,
NEW.STAT_SEND_INTERNET ,
NEW.STAT_SEND_FAX ,
NEW.STAT_SEND_S_MONTH ,
NEW.STAT_SEND_E_MONTH ,
NEW.NO_CANCEL_DEBT_FLAG ,
NEW.NO_CANCEL_DEBT_S_DATE ,
NEW.NO_CANCEL_DEBT_E_DATE ,
NEW.PAY_BY_STAGE_FLAG ,
NEW.NO_TEL_COLL_FLAG ,
NEW.NO_TEL_COLL_S_DATE ,
NEW.NO_TEL_COLL_E_DATE ,
NEW.NO_PER_COLL_FLAG ,
NEW.NO_PER_COLL_S_DATE ,
NEW.NO_PER_COLL_E_DATE ,
NEW.NO_DELINQUENT_FLAG ,
NEW.NO_DELINQUENT_S_DATE ,
NEW.NO_DELINQUENT_E_DATE ,
NEW.NO_COLLECTION_FLAG ,
NEW.NO_COLLECTION_S_DATE ,
NEW.NO_COLLECTION_E_DATE ,
NEW.LAWSUIT_PROCESS_LOG ,
NEW.CREDIT_ACT_NO ,
NEW.LAST_ACCT_STATUS ,
NEW.LAST_ACCT_SUB_STATUS ,
NEW.NO_ADJ_LOC_HIGH ,
NEW.NO_ADJ_LOC_HIGH_S_DATE ,
NEW.NO_ADJ_LOC_HIGH_E_DATE ,
NEW.NO_ADJ_LOC_LOW ,
NEW.NO_ADJ_LOC_LOW_S_DATE ,
NEW.NO_ADJ_LOC_LOW_E_DATE ,
NEW.NO_F_STOP_FLAG ,
NEW.NO_F_STOP_S_DATE ,
NEW.NO_F_STOP_E_DATE ,
NEW.H_ADJ_LOC_HIGH_DATE ,
NEW.H_ADJ_LOC_LOW_DATE ,
NEW.ADJ_LOC_HIGH_T ,
NEW.ADJ_LOC_LOW_T ,
NEW.LINE_OF_CREDIT_AMT ,
NEW.INST_AUTH_LOC_AMT ,
NEW.ADJ_BEFORE_LOC_AMT ,
NEW.REVOLVE_INT_SIGN ,
NEW.REVOLVE_INT_RATE ,
NEW.REVOLVE_RATE_S_MONTH ,
NEW.REVOLVE_RATE_E_MONTH ,
NEW.PENALTY_SIGN ,
NEW.PENALTY_RATE ,
NEW.PENALTY_RATE_S_MONTH ,
NEW.PENALTY_RATE_E_MONTH ,
NEW.MIN_PAY_RATE ,
NEW.MIN_PAY_RATE_S_MONTH ,
NEW.MIN_PAY_RATE_E_MONTH ,
NEW.AUTOPAY_INDICATOR ,
NEW.AUTOPAY_RATE ,
NEW.AUTOPAY_FIX_AMT ,
NEW.AUTOPAY_ACCT_BANK ,
NEW.AUTOPAY_ACCT_NO ,
NEW.AUTOPAY_ID ,
NEW.AUTOPAY_ID_CODE ,
NEW.AUTOPAY_ACCT_S_DATE ,
NEW.AUTOPAY_ACCT_E_DATE ,
NEW.WORSE_MCODE ,
NEW.BILL_SENDING_ZIP ,
NEW.BILL_SENDING_ADDR1 ,
NEW.BILL_SENDING_ADDR2 ,
NEW.BILL_SENDING_ADDR3 ,
NEW.BILL_SENDING_ADDR4 ,
NEW.BILL_SENDING_ADDR5 ,
NEW.CHG_ADDR_DATE ,
NEW.ACCEPT_DM ,
NEW.CORP_ASSURE_FLAG ,
NEW.SPECIAL_COMMENT ,
NEW.CORP_ACT_FLAG ,
NEW.LOST_FEE_FLAG ,
NEW.BANK_REL_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_CLASS_EX_A AFTER
INSERT
    ON
    ACT_CLASS_EX REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_ACT_CLASS_EX( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_P_SEQNO ,
    ACCT_TYPE
    --,ACCT_KEY
,
    CLASS_CODE ,
    VALUE_S_DATE ,
    VALUE_E_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.P_SEQNO ,
NEW.ACCT_TYPE
-- ,NEW.ACCT_KEY
,
NEW.CLASS_CODE ,
NEW.VALUE_S_DATE ,
NEW.VALUE_E_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_CLASS_EX_D AFTER
DELETE
    ON
    ACT_CLASS_EX REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_ACT_CLASS_EX( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_P_SEQNO ,
    ACCT_TYPE
    -- ,ACCT_KEY
,
    CLASS_CODE ,
    VALUE_S_DATE ,
    VALUE_E_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.P_SEQNO ,
OLD.ACCT_TYPE
-- ,OLD.ACCT_KEY
,
OLD.CLASS_CODE ,
OLD.VALUE_S_DATE ,
OLD.VALUE_E_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_CLASS_EX_U AFTER
UPDATE
    OF ACCT_TYPE
    --,ACCT_KEY
,
    CLASS_CODE ,
    VALUE_S_DATE ,
    VALUE_E_DATE ON
    ACT_CLASS_EX REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_ACT_CLASS_EX( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_P_SEQNO ,
    ACCT_TYPE
    --,ACCT_KEY
,
    CLASS_CODE ,
    VALUE_S_DATE ,
    VALUE_E_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.P_SEQNO ,
NEW.ACCT_TYPE
-- ,NEW.ACCT_KEY
,
NEW.CLASS_CODE ,
NEW.VALUE_S_DATE ,
NEW.VALUE_E_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_DEBT_A AFTER
INSERT
    ON
    ACT_DEBT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_ACT_DEBT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    STMT_CYCLE ,
    ACCT_CODE
    -- ,ACCT_ITEM_CNAME
,
    INTEREST_RS_DATE ,
    CRT_DATE ,
    CRT_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REFERENCE_NO ,
NEW.STMT_CYCLE ,
NEW.ACCT_CODE
-- ,NEW.ACCT_ITEM_CNAME
,
NEW.INTEREST_RS_DATE ,
NEW.CRT_DATE ,
NEW.CRT_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_DEBT_D AFTER
DELETE
    ON
    ACT_DEBT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_ACT_DEBT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    STMT_CYCLE ,
    ACCT_CODE
    --,ACCT_ITEM_CNAME
,
    INTEREST_RS_DATE ,
    CRT_DATE ,
    CRT_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.REFERENCE_NO ,
OLD.STMT_CYCLE ,
OLD.ACCT_CODE
-- ,OLD.ACCT_ITEM_CNAME
,
OLD.INTEREST_RS_DATE ,
OLD.CRT_DATE ,
OLD.CRT_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_ACT_DEBT_U AFTER
UPDATE
    OF STMT_CYCLE ,
    ACCT_CODE
    --,ACCT_ITEM_CNAME
,
    INTEREST_RS_DATE ,
    CRT_DATE ,
    CRT_USER ON
    ACT_DEBT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_ACT_DEBT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    STMT_CYCLE ,
    ACCT_CODE
    -- ,ACCT_ITEM_CNAME
,
    INTEREST_RS_DATE ,
    CRT_DATE ,
    CRT_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REFERENCE_NO ,
NEW.STMT_CYCLE ,
NEW.ACCT_CODE
-- ,NEW.ACCT_ITEM_CNAME
,
NEW.INTEREST_RS_DATE ,
NEW.CRT_DATE ,
NEW.CRT_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_ATMDOUBT_A AFTER
INSERT
    ON
    BIL_ATMDOUBT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_BIL_ATMDOUBT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    K_FILM_NO ,
    K_PURCHASE_DATE ,
    K_DESTINATION_AMT_CHAR ,
    RESULT_CODE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_NO ,
NEW.FILM_NO ,
NEW.PURCHASE_DATE ,
NEW.DESTINATION_AMT_CHAR ,
NEW.RESULT_CODE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_ATMDOUBT_D AFTER
DELETE
    ON
    BIL_ATMDOUBT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_BIL_ATMDOUBT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    K_FILM_NO ,
    K_PURCHASE_DATE ,
    K_DESTINATION_AMT_CHAR ,
    RESULT_CODE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.CARD_NO ,
OLD.FILM_NO ,
OLD.PURCHASE_DATE ,
OLD.DESTINATION_AMT_CHAR ,
OLD.RESULT_CODE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_ATMDOUBT_U AFTER
UPDATE
    OF RESULT_CODE ON
    BIL_ATMDOUBT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_BIL_ATMDOUBT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    K_FILM_NO ,
    K_PURCHASE_DATE ,
    K_DESTINATION_AMT_CHAR ,
    RESULT_CODE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_NO ,
NEW.FILM_NO ,
NEW.PURCHASE_DATE ,
NEW.DESTINATION_AMT_CHAR ,
NEW.RESULT_CODE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_CHTMAIN_A AFTER
INSERT
    ON
    BIL_CHTMAIN REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_BIL_CHTMAIN( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    K_TELEPHONE_NO ,
    K_OFFICE_M_CODE ,
    K_OFFICE_CODE ,
    K_TRANSACTION_TYPE ,
    CONFIRM_DATE ,
    CONFIRM_FLAG ,
    ERROR_CODE1 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_NO ,
NEW.TELEPHONE_NO ,
NEW.OFFICE_M_CODE ,
NEW.OFFICE_CODE ,
NEW.TRANSACTION_TYPE ,
NEW.CONFIRM_DATE ,
NEW.CONFIRM_FLAG ,
NEW.ERROR_CODE1 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_CHTMAIN_D AFTER
DELETE
    ON
    BIL_CHTMAIN REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_BIL_CHTMAIN( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    K_TELEPHONE_NO ,
    K_OFFICE_M_CODE ,
    K_OFFICE_CODE ,
    K_TRANSACTION_TYPE ,
    CONFIRM_DATE ,
    CONFIRM_FLAG ,
    ERROR_CODE1 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.CARD_NO ,
OLD.TELEPHONE_NO ,
OLD.OFFICE_M_CODE ,
OLD.OFFICE_CODE ,
OLD.TRANSACTION_TYPE ,
OLD.CONFIRM_DATE ,
OLD.CONFIRM_FLAG ,
OLD.ERROR_CODE1 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_CHTMAIN_U AFTER
UPDATE
    OF CONFIRM_DATE ,
    CONFIRM_FLAG ,
    ERROR_CODE1 ON
    BIL_CHTMAIN REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_BIL_CHTMAIN( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    K_TELEPHONE_NO ,
    K_OFFICE_M_CODE ,
    K_OFFICE_CODE ,
    K_TRANSACTION_TYPE ,
    CONFIRM_DATE ,
    CONFIRM_FLAG ,
    ERROR_CODE1 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_NO ,
NEW.TELEPHONE_NO ,
NEW.OFFICE_M_CODE ,
NEW.OFFICE_CODE ,
NEW.TRANSACTION_TYPE ,
NEW.CONFIRM_DATE ,
NEW.CONFIRM_FLAG ,
NEW.ERROR_CODE1 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_CONTRACT_A AFTER
INSERT
    ON
    BIL_CONTRACT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_BIL_CONTRACT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CONTRACT_NO ,
    K_CONTRACT_SEQ_NO ,
    ALLOCATE_FLAG ,
    REFUND_FLAG ,
    REFUND_QTY ,
    REFUND_APR_FLAG ,
    REFUND_APR_DATE ,
    APR_DATE ,
    APR_FLAG ,
    RECEIVE_NAME ,
    VOUCHER_HEAD ,
    UNIFORM_NO ,
    RECEIVE_ADDRESS ,
    DELV_DATE ,
    DELV_CONFIRM_FLAG ,
    DELV_CONFIRM_DATE ,
    REGISTER_NO ,
    DELV_BATCH_NO ,
    FORCED_POST_FLAG ,
    INSTALL_BACK_TERM ,
    INSTALL_BACK_TERM_FLAG ,
    UNIT_PRICE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CONTRACT_NO ,
NEW.CONTRACT_SEQ_NO ,
NEW.ALLOCATE_FLAG ,
NEW.REFUND_FLAG ,
NEW.REFUND_QTY ,
NEW.REFUND_APR_FLAG ,
NEW.REFUND_APR_DATE ,
NEW.APR_DATE ,
NEW.APR_FLAG ,
NEW.RECEIVE_NAME ,
NEW.VOUCHER_HEAD ,
NEW.UNIFORM_NO ,
NEW.RECEIVE_ADDRESS ,
NEW.DELV_DATE ,
NEW.DELV_CONFIRM_FLAG ,
NEW.DELV_CONFIRM_DATE ,
NEW.REGISTER_NO ,
NEW.DELV_BATCH_NO ,
NEW.FORCED_POST_FLAG ,
NEW.INSTALL_BACK_TERM ,
NEW.INSTALL_BACK_TERM_FLAG ,
NEW.UNIT_PRICE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_CONTRACT_D AFTER
DELETE
    ON
    BIL_CONTRACT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_BIL_CONTRACT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CONTRACT_NO ,
    K_CONTRACT_SEQ_NO ,
    ALLOCATE_FLAG ,
    REFUND_FLAG ,
    REFUND_QTY ,
    REFUND_APR_FLAG ,
    REFUND_APR_DATE ,
    APR_DATE ,
    APR_FLAG ,
    RECEIVE_NAME ,
    VOUCHER_HEAD ,
    UNIFORM_NO ,
    RECEIVE_ADDRESS ,
    DELV_DATE ,
    DELV_CONFIRM_FLAG ,
    DELV_CONFIRM_DATE ,
    REGISTER_NO ,
    DELV_BATCH_NO ,
    FORCED_POST_FLAG ,
    INSTALL_BACK_TERM ,
    INSTALL_BACK_TERM_FLAG ,
    UNIT_PRICE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.CONTRACT_NO ,
OLD.CONTRACT_SEQ_NO ,
OLD.ALLOCATE_FLAG ,
OLD.REFUND_FLAG ,
OLD.REFUND_QTY ,
OLD.REFUND_APR_FLAG ,
OLD.REFUND_APR_DATE ,
OLD.APR_DATE ,
OLD.APR_FLAG ,
OLD.RECEIVE_NAME ,
OLD.VOUCHER_HEAD ,
OLD.UNIFORM_NO ,
OLD.RECEIVE_ADDRESS ,
OLD.DELV_DATE ,
OLD.DELV_CONFIRM_FLAG ,
OLD.DELV_CONFIRM_DATE ,
OLD.REGISTER_NO ,
OLD.DELV_BATCH_NO ,
OLD.FORCED_POST_FLAG ,
OLD.INSTALL_BACK_TERM ,
OLD.INSTALL_BACK_TERM_FLAG ,
OLD.UNIT_PRICE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_CONTRACT_U AFTER
UPDATE
    OF ALLOCATE_FLAG ,
    REFUND_FLAG ,
    REFUND_QTY ,
    REFUND_APR_FLAG ,
    REFUND_APR_DATE ,
    APR_DATE ,
    APR_FLAG ,
    RECEIVE_NAME ,
    VOUCHER_HEAD ,
    UNIFORM_NO ,
    RECEIVE_ADDRESS ,
    DELV_DATE ,
    DELV_CONFIRM_FLAG ,
    DELV_CONFIRM_DATE ,
    REGISTER_NO ,
    DELV_BATCH_NO ,
    FORCED_POST_FLAG ,
    INSTALL_BACK_TERM ,
    INSTALL_BACK_TERM_FLAG ,
    UNIT_PRICE ON
    BIL_CONTRACT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_BIL_CONTRACT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CONTRACT_NO ,
    K_CONTRACT_SEQ_NO ,
    ALLOCATE_FLAG ,
    REFUND_FLAG ,
    REFUND_QTY ,
    REFUND_APR_FLAG ,
    REFUND_APR_DATE ,
    APR_DATE ,
    APR_FLAG ,
    RECEIVE_NAME ,
    VOUCHER_HEAD ,
    UNIFORM_NO ,
    RECEIVE_ADDRESS ,
    DELV_DATE ,
    DELV_CONFIRM_FLAG ,
    DELV_CONFIRM_DATE ,
    REGISTER_NO ,
    DELV_BATCH_NO ,
    FORCED_POST_FLAG ,
    INSTALL_BACK_TERM ,
    INSTALL_BACK_TERM_FLAG ,
    UNIT_PRICE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CONTRACT_NO ,
NEW.CONTRACT_SEQ_NO ,
NEW.ALLOCATE_FLAG ,
NEW.REFUND_FLAG ,
NEW.REFUND_QTY ,
NEW.REFUND_APR_FLAG ,
NEW.REFUND_APR_DATE ,
NEW.APR_DATE ,
NEW.APR_FLAG ,
NEW.RECEIVE_NAME ,
NEW.VOUCHER_HEAD ,
NEW.UNIFORM_NO ,
NEW.RECEIVE_ADDRESS ,
NEW.DELV_DATE ,
NEW.DELV_CONFIRM_FLAG ,
NEW.DELV_CONFIRM_DATE ,
NEW.REGISTER_NO ,
NEW.DELV_BATCH_NO ,
NEW.FORCED_POST_FLAG ,
NEW.INSTALL_BACK_TERM ,
NEW.INSTALL_BACK_TERM_FLAG ,
NEW.UNIT_PRICE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_MERCHANT_A AFTER
INSERT
    ON
    BIL_MERCHANT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_BIL_MERCHANT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_MCHT_NO ,
    UNIFORM_NO ,
    SIGN_DATE ,
    FORCED_FLAG ,
    BROKEN_DATE ,
    MCHT_STATUS ,
    MCHT_ENG_NAME ,
    MCHT_CHI_NAME ,
    MCHT_ADDRESS ,
    MCHT_ZIP ,
    OWNER_NAME ,
    OWNER_ID ,
    MCHT_TEL1 ,
    MCHT_TEL1_1 ,
    MCHT_TEL1_2 ,
    MCHT_TEL2 ,
    MCHT_TEL2_1 ,
    MCHT_TEL2_2 ,
    MCHT_FAX1 ,
    MCHT_FAX1_1 ,
    MCHT_FAX2 ,
    MCHT_FAX2_1 ,
    E_MAIL ,
    CONTRACT_NAME ,
    ASSIGN_ACCT ,
    BANK_NAME ,
    OTH_BANK_ID ,
    OTH_BANK_ACCT ,
    CLR_BANK_ID ,
    MCHT_ACCT_NAME ,
    MCHT_CITY ,
    MCHT_COUNTRY ,
    MCHT_STATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.MCHT_NO ,
NEW.UNIFORM_NO ,
NEW.SIGN_DATE ,
NEW.FORCED_FLAG ,
NEW.BROKEN_DATE ,
NEW.MCHT_STATUS ,
NEW.MCHT_ENG_NAME ,
NEW.MCHT_CHI_NAME ,
NEW.MCHT_ADDRESS ,
NEW.MCHT_ZIP ,
NEW.OWNER_NAME ,
NEW.OWNER_ID ,
NEW.MCHT_TEL1 ,
NEW.MCHT_TEL1_1 ,
NEW.MCHT_TEL1_2 ,
NEW.MCHT_TEL2 ,
NEW.MCHT_TEL2_1 ,
NEW.MCHT_TEL2_2 ,
NEW.MCHT_FAX1 ,
NEW.MCHT_FAX1_1 ,
NEW.MCHT_FAX2 ,
NEW.MCHT_FAX2_1 ,
NEW.E_MAIL ,
NEW.CONTRACT_NAME ,
NEW.ASSIGN_ACCT ,
NEW.BANK_NAME ,
NEW.OTH_BANK_ID ,
NEW.OTH_BANK_ACCT ,
NEW.CLR_BANK_ID ,
NEW.MCHT_ACCT_NAME ,
NEW.MCHT_CITY ,
NEW.MCHT_COUNTRY ,
NEW.MCHT_STATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_MERCHANT_D AFTER
DELETE
    ON
    BIL_MERCHANT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_BIL_MERCHANT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_MCHT_NO ,
    UNIFORM_NO ,
    SIGN_DATE ,
    FORCED_FLAG ,
    BROKEN_DATE ,
    MCHT_STATUS ,
    MCHT_ENG_NAME ,
    MCHT_CHI_NAME ,
    MCHT_ADDRESS ,
    MCHT_ZIP ,
    OWNER_NAME ,
    OWNER_ID ,
    MCHT_TEL1 ,
    MCHT_TEL1_1 ,
    MCHT_TEL1_2 ,
    MCHT_TEL2 ,
    MCHT_TEL2_1 ,
    MCHT_TEL2_2 ,
    MCHT_FAX1 ,
    MCHT_FAX1_1 ,
    MCHT_FAX2 ,
    MCHT_FAX2_1 ,
    E_MAIL ,
    CONTRACT_NAME ,
    ASSIGN_ACCT ,
    BANK_NAME ,
    OTH_BANK_ID ,
    OTH_BANK_ACCT ,
    CLR_BANK_ID ,
    MCHT_ACCT_NAME ,
    MCHT_CITY ,
    MCHT_COUNTRY ,
    MCHT_STATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.MCHT_NO ,
OLD.UNIFORM_NO ,
OLD.SIGN_DATE ,
OLD.FORCED_FLAG ,
OLD.BROKEN_DATE ,
OLD.MCHT_STATUS ,
OLD.MCHT_ENG_NAME ,
OLD.MCHT_CHI_NAME ,
OLD.MCHT_ADDRESS ,
OLD.MCHT_ZIP ,
OLD.OWNER_NAME ,
OLD.OWNER_ID ,
OLD.MCHT_TEL1 ,
OLD.MCHT_TEL1_1 ,
OLD.MCHT_TEL1_2 ,
OLD.MCHT_TEL2 ,
OLD.MCHT_TEL2_1 ,
OLD.MCHT_TEL2_2 ,
OLD.MCHT_FAX1 ,
OLD.MCHT_FAX1_1 ,
OLD.MCHT_FAX2 ,
OLD.MCHT_FAX2_1 ,
OLD.E_MAIL ,
OLD.CONTRACT_NAME ,
OLD.ASSIGN_ACCT ,
OLD.BANK_NAME ,
OLD.OTH_BANK_ID ,
OLD.OTH_BANK_ACCT ,
OLD.CLR_BANK_ID ,
OLD.MCHT_ACCT_NAME ,
OLD.MCHT_CITY ,
OLD.MCHT_COUNTRY ,
OLD.MCHT_STATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_MERCHANT_U AFTER
UPDATE
    OF UNIFORM_NO ,
    SIGN_DATE ,
    FORCED_FLAG ,
    BROKEN_DATE ,
    MCHT_STATUS ,
    MCHT_ENG_NAME ,
    MCHT_CHI_NAME ,
    MCHT_ADDRESS ,
    MCHT_ZIP ,
    OWNER_NAME ,
    OWNER_ID ,
    MCHT_TEL1 ,
    MCHT_TEL1_1 ,
    MCHT_TEL1_2 ,
    MCHT_TEL2 ,
    MCHT_TEL2_1 ,
    MCHT_TEL2_2 ,
    MCHT_FAX1 ,
    MCHT_FAX1_1 ,
    MCHT_FAX2 ,
    MCHT_FAX2_1 ,
    E_MAIL ,
    CONTRACT_NAME ,
    ASSIGN_ACCT ,
    BANK_NAME ,
    OTH_BANK_ID ,
    OTH_BANK_ACCT ,
    CLR_BANK_ID ,
    MCHT_ACCT_NAME ,
    MCHT_CITY ,
    MCHT_COUNTRY ,
    MCHT_STATE ON
    BIL_MERCHANT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_BIL_MERCHANT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_MCHT_NO ,
    UNIFORM_NO ,
    SIGN_DATE ,
    FORCED_FLAG ,
    BROKEN_DATE ,
    MCHT_STATUS ,
    MCHT_ENG_NAME ,
    MCHT_CHI_NAME ,
    MCHT_ADDRESS ,
    MCHT_ZIP ,
    OWNER_NAME ,
    OWNER_ID ,
    MCHT_TEL1 ,
    MCHT_TEL1_1 ,
    MCHT_TEL1_2 ,
    MCHT_TEL2 ,
    MCHT_TEL2_1 ,
    MCHT_TEL2_2 ,
    MCHT_FAX1 ,
    MCHT_FAX1_1 ,
    MCHT_FAX2 ,
    MCHT_FAX2_1 ,
    E_MAIL ,
    CONTRACT_NAME ,
    ASSIGN_ACCT ,
    BANK_NAME ,
    OTH_BANK_ID ,
    OTH_BANK_ACCT ,
    CLR_BANK_ID ,
    MCHT_ACCT_NAME ,
    MCHT_CITY ,
    MCHT_COUNTRY ,
    MCHT_STATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.MCHT_NO ,
NEW.UNIFORM_NO ,
NEW.SIGN_DATE ,
NEW.FORCED_FLAG ,
NEW.BROKEN_DATE ,
NEW.MCHT_STATUS ,
NEW.MCHT_ENG_NAME ,
NEW.MCHT_CHI_NAME ,
NEW.MCHT_ADDRESS ,
NEW.MCHT_ZIP ,
NEW.OWNER_NAME ,
NEW.OWNER_ID ,
NEW.MCHT_TEL1 ,
NEW.MCHT_TEL1_1 ,
NEW.MCHT_TEL1_2 ,
NEW.MCHT_TEL2 ,
NEW.MCHT_TEL2_1 ,
NEW.MCHT_TEL2_2 ,
NEW.MCHT_FAX1 ,
NEW.MCHT_FAX1_1 ,
NEW.MCHT_FAX2 ,
NEW.MCHT_FAX2_1 ,
NEW.E_MAIL ,
NEW.CONTRACT_NAME ,
NEW.ASSIGN_ACCT ,
NEW.BANK_NAME ,
NEW.OTH_BANK_ID ,
NEW.OTH_BANK_ACCT ,
NEW.CLR_BANK_ID ,
NEW.MCHT_ACCT_NAME ,
NEW.MCHT_CITY ,
NEW.MCHT_COUNTRY ,
NEW.MCHT_STATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_OFFICE_A AFTER
INSERT
    ON
    BIL_OFFICE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_BIL_OFFICE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_OFFICE_M_CODE ,
    K_OFFICE_CODE ,
    OFFICE_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.OFFICE_M_CODE ,
NEW.OFFICE_CODE ,
NEW.OFFICE_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_OFFICE_D AFTER
DELETE
    ON
    BIL_OFFICE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_BIL_OFFICE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_OFFICE_M_CODE ,
    K_OFFICE_CODE ,
    OFFICE_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.OFFICE_M_CODE ,
OLD.OFFICE_CODE ,
OLD.OFFICE_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_OFFICE_M_A AFTER
INSERT
    ON
    BIL_OFFICE_M REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_BIL_OFFICE_M( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_OFFICE_M_CODE ,
    OFFICE_M_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.OFFICE_M_CODE ,
NEW.OFFICE_M_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_OFFICE_M_D AFTER
DELETE
    ON
    BIL_OFFICE_M REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_BIL_OFFICE_M( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_OFFICE_M_CODE ,
    OFFICE_M_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.OFFICE_M_CODE ,
OLD.OFFICE_M_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_OFFICE_M_U AFTER
UPDATE
    OF OFFICE_M_NAME ON
    BIL_OFFICE_M REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_BIL_OFFICE_M( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_OFFICE_M_CODE ,
    OFFICE_M_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.OFFICE_M_CODE ,
NEW.OFFICE_M_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_OFFICE_TIME_A AFTER
INSERT
    ON
    BIL_OFFICE_TIME REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_BIL_OFFICE_TIME( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_OFFICE_M_CODE ,
    OFFICE_DAYS ,
    GROUP_CODE1 ,
    GROUP_CODE2 ,
    GROUP_CODE3 ,
    GROUP_CODE4 ,
    GROUP_CODE5 ,
    APR_FLAG ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.OFFICE_M_CODE ,
NEW.OFFICE_DAYS ,
NEW.GROUP_CODE1 ,
NEW.GROUP_CODE2 ,
NEW.GROUP_CODE3 ,
NEW.GROUP_CODE4 ,
NEW.GROUP_CODE5 ,
NEW.APR_FLAG ,
NEW.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_OFFICE_TIME_D AFTER
DELETE
    ON
    BIL_OFFICE_TIME REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_BIL_OFFICE_TIME( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_OFFICE_M_CODE ,
    OFFICE_DAYS ,
    GROUP_CODE1 ,
    GROUP_CODE2 ,
    GROUP_CODE3 ,
    GROUP_CODE4 ,
    GROUP_CODE5 ,
    APR_FLAG ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.OFFICE_M_CODE ,
OLD.OFFICE_DAYS ,
OLD.GROUP_CODE1 ,
OLD.GROUP_CODE2 ,
OLD.GROUP_CODE3 ,
OLD.GROUP_CODE4 ,
OLD.GROUP_CODE5 ,
OLD.APR_FLAG ,
OLD.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_OFFICE_TIME_U AFTER
UPDATE
    OF OFFICE_DAYS ,
    GROUP_CODE1 ,
    GROUP_CODE2 ,
    GROUP_CODE3 ,
    GROUP_CODE4 ,
    GROUP_CODE5 ,
    APR_FLAG ,
    APR_DATE ON
    BIL_OFFICE_TIME REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_BIL_OFFICE_TIME( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_OFFICE_M_CODE ,
    OFFICE_DAYS ,
    GROUP_CODE1 ,
    GROUP_CODE2 ,
    GROUP_CODE3 ,
    GROUP_CODE4 ,
    GROUP_CODE5 ,
    APR_FLAG ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.OFFICE_M_CODE ,
NEW.OFFICE_DAYS ,
NEW.GROUP_CODE1 ,
NEW.GROUP_CODE2 ,
NEW.GROUP_CODE3 ,
NEW.GROUP_CODE4 ,
NEW.GROUP_CODE5 ,
NEW.APR_FLAG ,
NEW.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_OFFICE_U AFTER
UPDATE
    OF OFFICE_NAME ON
    BIL_OFFICE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_BIL_OFFICE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_OFFICE_M_CODE ,
    K_OFFICE_CODE ,
    OFFICE_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.OFFICE_M_CODE ,
NEW.OFFICE_CODE ,
NEW.OFFICE_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_PROD_A AFTER
INSERT
    ON
    BIL_PROD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_BIL_PROD( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PRODUCT_NO ,
    K_MCHT_NO ,
    PRODUCT_NAME ,
    UNIT_PRICE ,
    TOT_AMT ,
    TOT_TERM ,
    EXTRA_FEES ,
    FEES_FIX_AMT ,
    FEES_MIN_AMT ,
    FEES_MAX_AMT ,
    AUTO_DELV_FLAG ,
    AGAINST_NUM ,
    AUTO_PRINT_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PRODUCT_NO ,
NEW.MCHT_NO ,
NEW.PRODUCT_NAME ,
NEW.UNIT_PRICE ,
NEW.TOT_AMT ,
NEW.TOT_TERM ,
NEW.EXTRA_FEES ,
NEW.FEES_FIX_AMT ,
NEW.FEES_MIN_AMT ,
NEW.FEES_MAX_AMT ,
NEW.AUTO_DELV_FLAG ,
NEW.AGAINST_NUM ,
NEW.AUTO_PRINT_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_PROD_D AFTER
DELETE
    ON
    BIL_PROD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_BIL_PROD( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PRODUCT_NO ,
    K_MCHT_NO ,
    PRODUCT_NAME ,
    UNIT_PRICE ,
    TOT_AMT ,
    TOT_TERM ,
    EXTRA_FEES ,
    FEES_FIX_AMT ,
    FEES_MIN_AMT ,
    FEES_MAX_AMT ,
    AUTO_DELV_FLAG ,
    AGAINST_NUM ,
    AUTO_PRINT_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.PRODUCT_NO ,
OLD.MCHT_NO ,
OLD.PRODUCT_NAME ,
OLD.UNIT_PRICE ,
OLD.TOT_AMT ,
OLD.TOT_TERM ,
OLD.EXTRA_FEES ,
OLD.FEES_FIX_AMT ,
OLD.FEES_MIN_AMT ,
OLD.FEES_MAX_AMT ,
OLD.AUTO_DELV_FLAG ,
OLD.AGAINST_NUM ,
OLD.AUTO_PRINT_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_BIL_PROD_U AFTER
UPDATE
    OF PRODUCT_NAME ,
    UNIT_PRICE ,
    TOT_AMT ,
    TOT_TERM ,
    EXTRA_FEES ,
    FEES_FIX_AMT ,
    FEES_MIN_AMT ,
    FEES_MAX_AMT ,
    AUTO_DELV_FLAG ,
    AGAINST_NUM ,
    AUTO_PRINT_FLAG ON
    BIL_PROD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_BIL_PROD( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PRODUCT_NO ,
    K_MCHT_NO ,
    PRODUCT_NAME ,
    UNIT_PRICE ,
    TOT_AMT ,
    TOT_TERM ,
    EXTRA_FEES ,
    FEES_FIX_AMT ,
    FEES_MIN_AMT ,
    FEES_MAX_AMT ,
    AUTO_DELV_FLAG ,
    AGAINST_NUM ,
    AUTO_PRINT_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PRODUCT_NO ,
NEW.MCHT_NO ,
NEW.PRODUCT_NAME ,
NEW.UNIT_PRICE ,
NEW.TOT_AMT ,
NEW.TOT_TERM ,
NEW.EXTRA_FEES ,
NEW.FEES_FIX_AMT ,
NEW.FEES_MIN_AMT ,
NEW.FEES_MAX_AMT ,
NEW.AUTO_DELV_FLAG ,
NEW.AGAINST_NUM ,
NEW.AUTO_PRINT_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CMS_ROADGROUP_A AFTER
INSERT
    ON
    CMS_ROADGROUP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CMS_ROADGROUP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    GCARD_FLAG ,
    BCARD_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.GCARD_FLAG ,
NEW.BCARD_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CMS_ROADGROUP_D AFTER
DELETE
    ON
    CMS_ROADGROUP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CMS_ROADGROUP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    GCARD_FLAG ,
    BCARD_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.GROUP_CODE ,
OLD.GCARD_FLAG ,
OLD.BCARD_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CMS_ROADGROUP_U AFTER
UPDATE
    OF MOD_PGM,
    MOD_USER,
    BCARD_FLAG,
    GCARD_FLAG,
    GROUP_CODE ON
    CMS_ROADGROUP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_CMS_ROADGROUP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    GCARD_FLAG ,
    BCARD_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.GCARD_FLAG ,
NEW.BCARD_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CMS_ROADMASTER_A AFTER
INSERT
    ON
    CMS_ROADMASTER REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CMS_ROADMASTER( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    K_RM_TYPE ,
    K_RM_CARNO ,
    GROUP_CODE ,
    RM_CARMANNAME ,
    RM_CARMANID ,
    RM_HTELNO1 ,
    RM_HTELNO2 ,
    RM_HTELNO3 ,
    RM_OTELNO1 ,
    RM_OTELNO2 ,
    RM_OTELNO3 ,
    CELLAR_PHONE ,
    RM_VALIDDATE ,
    RM_PAYAMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_NO ,
NEW.RM_TYPE ,
NEW.RM_CARNO ,
NEW.GROUP_CODE ,
NEW.RM_CARMANNAME ,
NEW.RM_CARMANID ,
NEW.RM_HTELNO1 ,
NEW.RM_HTELNO2 ,
NEW.RM_HTELNO3 ,
NEW.RM_OTELNO1 ,
NEW.RM_OTELNO2 ,
NEW.RM_OTELNO3 ,
NEW.CELLAR_PHONE ,
NEW.RM_VALIDDATE ,
NEW.RM_PAYAMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CMS_ROADMASTER_D AFTER
DELETE
    ON
    CMS_ROADMASTER REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CMS_ROADMASTER( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    K_RM_TYPE ,
    K_RM_CARNO ,
    GROUP_CODE ,
    RM_CARMANNAME ,
    RM_CARMANID ,
    RM_HTELNO1 ,
    RM_HTELNO2 ,
    RM_HTELNO3 ,
    RM_OTELNO1 ,
    RM_OTELNO2 ,
    RM_OTELNO3 ,
    CELLAR_PHONE ,
    RM_VALIDDATE ,
    RM_PAYAMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.CARD_NO ,
OLD.RM_TYPE ,
OLD.RM_CARNO ,
OLD.GROUP_CODE ,
OLD.RM_CARMANNAME ,
OLD.RM_CARMANID ,
OLD.RM_HTELNO1 ,
OLD.RM_HTELNO2 ,
OLD.RM_HTELNO3 ,
OLD.RM_OTELNO1 ,
OLD.RM_OTELNO2 ,
OLD.RM_OTELNO3 ,
OLD.CELLAR_PHONE ,
OLD.RM_VALIDDATE ,
OLD.RM_PAYAMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CMS_ROADMASTER_U AFTER
UPDATE
    OF GROUP_CODE ,
    RM_CARMANNAME ,
    RM_CARMANID ,
    RM_HTELNO1 ,
    RM_HTELNO2 ,
    RM_HTELNO3 ,
    RM_OTELNO1 ,
    RM_OTELNO2 ,
    RM_OTELNO3 ,
    CELLAR_PHONE ,
    RM_VALIDDATE ,
    RM_PAYAMT ON
    CMS_ROADMASTER REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_CMS_ROADMASTER( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    K_RM_TYPE ,
    K_RM_CARNO ,
    GROUP_CODE ,
    RM_CARMANNAME ,
    RM_CARMANID ,
    RM_HTELNO1 ,
    RM_HTELNO2 ,
    RM_HTELNO3 ,
    RM_OTELNO1 ,
    RM_OTELNO2 ,
    RM_OTELNO3 ,
    CELLAR_PHONE ,
    RM_VALIDDATE ,
    RM_PAYAMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_NO ,
NEW.RM_TYPE ,
NEW.RM_CARNO ,
NEW.GROUP_CODE ,
NEW.RM_CARMANNAME ,
NEW.RM_CARMANID ,
NEW.RM_HTELNO1 ,
NEW.RM_HTELNO2 ,
NEW.RM_HTELNO3 ,
NEW.RM_OTELNO1 ,
NEW.RM_OTELNO2 ,
NEW.RM_OTELNO3 ,
NEW.CELLAR_PHONE ,
NEW.RM_VALIDDATE ,
NEW.RM_PAYAMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CMS_ROADPARM_A AFTER
INSERT
    ON
    CMS_ROADPARM REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CMS_ROADPARM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    FREE_AMT ,
    CYEAR_AMT ,
    RECV_AMT ,
    GCARD_FLAG ,
    BCARD_FLAG ,
    STOP_DAYS ,
    FSTOP_DAYS ,
    LOST_DAYS ,
    FALSE_DAYS )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.FREE_AMT ,
NEW.CYEAR_AMT ,
NEW.RECV_AMT ,
NEW.GCARD_FLAG ,
NEW.BCARD_FLAG ,
NEW.STOP_DAYS ,
NEW.FSTOP_DAYS ,
NEW.LOST_DAYS ,
NEW.FALSE_DAYS );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CMS_ROADPARM_D AFTER
DELETE
    ON
    CMS_ROADPARM REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CMS_ROADPARM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    FREE_AMT ,
    CYEAR_AMT ,
    RECV_AMT ,
    GCARD_FLAG ,
    BCARD_FLAG ,
    STOP_DAYS ,
    FSTOP_DAYS ,
    LOST_DAYS ,
    FALSE_DAYS )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.FREE_AMT ,
OLD.CYEAR_AMT ,
OLD.RECV_AMT ,
OLD.GCARD_FLAG ,
OLD.BCARD_FLAG ,
OLD.STOP_DAYS ,
OLD.FSTOP_DAYS ,
OLD.LOST_DAYS ,
OLD.FALSE_DAYS );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CMS_ROADPARM_U AFTER
UPDATE
    OF MOD_PGM,
    MOD_USER,
    BCARD_FLAG,
    CYEAR_AMT,
    FALSE_DAYS,
    FREE_AMT,
    FSTOP_DAYS,
    GCARD_FLAG,
    LOST_DAYS,
    RECV_AMT,
    STOP_DAYS ON
    CMS_ROADPARM REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_CMS_ROADPARM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    FREE_AMT ,
    CYEAR_AMT ,
    RECV_AMT ,
    GCARD_FLAG ,
    BCARD_FLAG ,
    STOP_DAYS ,
    FSTOP_DAYS ,
    LOST_DAYS ,
    FALSE_DAYS )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.FREE_AMT ,
NEW.CYEAR_AMT ,
NEW.RECV_AMT ,
NEW.GCARD_FLAG ,
NEW.BCARD_FLAG ,
NEW.STOP_DAYS ,
NEW.FSTOP_DAYS ,
NEW.LOST_DAYS ,
NEW.FALSE_DAYS );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_COL_BAD_DEBT_A AFTER
INSERT
    ON
    COL_BAD_DEBT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_COL_BAD_DEBT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_TRANS_TYPE ,
    K_P_SEQNO ,
    K_TRANS_DATE ,
    ALW_BAD_DATE ,
    PAPER_CONF_DATE ,
    PAPER_NAME ,
    PAPER_TYPE ,
    DESCRIPTION )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.TRANS_TYPE ,
NEW.P_SEQNO ,
NEW.TRANS_DATE ,
NEW.ALW_BAD_DATE ,
NEW.PAPER_CONF_DATE ,
NEW.PAPER_NAME ,
NEW.PAPER_TYPE ,
NEW.DESCRIPTION );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_COL_BAD_DEBT_D AFTER
DELETE
    ON
    COL_BAD_DEBT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_COL_BAD_DEBT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_TRANS_TYPE ,
    K_P_SEQNO ,
    K_TRANS_DATE ,
    ALW_BAD_DATE ,
    PAPER_CONF_DATE ,
    PAPER_NAME ,
    PAPER_TYPE ,
    DESCRIPTION )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.TRANS_TYPE ,
OLD.P_SEQNO ,
OLD.TRANS_DATE ,
OLD.ALW_BAD_DATE ,
OLD.PAPER_CONF_DATE ,
OLD.PAPER_NAME ,
OLD.PAPER_TYPE ,
OLD.DESCRIPTION );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_COL_BAD_DEBT_U AFTER
UPDATE
    OF ALW_BAD_DATE ,
    PAPER_CONF_DATE ,
    PAPER_NAME ,
    PAPER_TYPE ,
    DESCRIPTION ON
    COL_BAD_DEBT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_COL_BAD_DEBT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_TRANS_TYPE ,
    K_P_SEQNO ,
    K_TRANS_DATE ,
    ALW_BAD_DATE ,
    PAPER_CONF_DATE ,
    PAPER_NAME ,
    PAPER_TYPE ,
    DESCRIPTION )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.TRANS_TYPE ,
NEW.P_SEQNO ,
NEW.TRANS_DATE ,
NEW.ALW_BAD_DATE ,
NEW.PAPER_CONF_DATE ,
NEW.PAPER_NAME ,
NEW.PAPER_TYPE ,
NEW.DESCRIPTION );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_COL_DRAWRATE_A AFTER
INSERT
    ON
    COL_DRAWRATE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_COL_DRAWRATE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    MCODE_1 ,
    MCODE_1_RATE ,
    MCODE_X_Y_RATE ,
    MCODE_2 ,
    MCODE_2_RATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.MCODE_1 ,
NEW.MCODE_1_RATE ,
NEW.MCODE_X_Y_RATE ,
NEW.MCODE_2 ,
NEW.MCODE_2_RATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_COL_DRAWRATE_D AFTER
DELETE
    ON
    COL_DRAWRATE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_COL_DRAWRATE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    MCODE_1 ,
    MCODE_1_RATE ,
    MCODE_X_Y_RATE ,
    MCODE_2 ,
    MCODE_2_RATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.MCODE_1 ,
OLD.MCODE_1_RATE ,
OLD.MCODE_X_Y_RATE ,
OLD.MCODE_2 ,
OLD.MCODE_2_RATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_COL_DRAWRATE_U AFTER
UPDATE
    OF MCODE_1 ,
    MCODE_1_RATE ,
    MCODE_X_Y_RATE ,
    MCODE_2 ,
    MCODE_2_RATE ON
    COL_DRAWRATE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_COL_DRAWRATE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    MCODE_1 ,
    MCODE_1_RATE ,
    MCODE_X_Y_RATE ,
    MCODE_2 ,
    MCODE_2_RATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.MCODE_1 ,
NEW.MCODE_1_RATE ,
NEW.MCODE_X_Y_RATE ,
NEW.MCODE_2 ,
NEW.MCODE_2_RATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_COL_PARAM_A AFTER
INSERT
    ON
    COL_PARAM REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_COL_PARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    REQ_DEBT_LMT ,
    EXC_TTL_LMT_1 ,
    EXC_OWE_LMT_1 ,
    EXC_TTL_LMT_2 ,
    EXC_OWE_LMT_2 ,
    GEN_CS_DAY ,
    TRANS_COL_DAY )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REQ_DEBT_LMT ,
NEW.EXC_TTL_LMT_1 ,
NEW.EXC_OWE_LMT_1 ,
NEW.EXC_TTL_LMT_2 ,
NEW.EXC_OWE_LMT_2 ,
NEW.GEN_CS_DAY ,
NEW.TRANS_COL_DAY );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_COL_PARAM_D AFTER
DELETE
    ON
    COL_PARAM REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_COL_PARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    REQ_DEBT_LMT ,
    EXC_TTL_LMT_1 ,
    EXC_OWE_LMT_1 ,
    EXC_TTL_LMT_2 ,
    EXC_OWE_LMT_2 ,
    GEN_CS_DAY ,
    TRANS_COL_DAY )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.REQ_DEBT_LMT ,
OLD.EXC_TTL_LMT_1 ,
OLD.EXC_OWE_LMT_1 ,
OLD.EXC_TTL_LMT_2 ,
OLD.EXC_OWE_LMT_2 ,
OLD.GEN_CS_DAY ,
OLD.TRANS_COL_DAY );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_COL_PARAM_U AFTER
UPDATE
    OF REQ_DEBT_LMT ,
    EXC_TTL_LMT_1 ,
    EXC_OWE_LMT_1 ,
    EXC_TTL_LMT_2 ,
    EXC_OWE_LMT_2 ,
    GEN_CS_DAY ,
    TRANS_COL_DAY ON
    COL_PARAM REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_COL_PARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    REQ_DEBT_LMT ,
    EXC_TTL_LMT_1 ,
    EXC_OWE_LMT_1 ,
    EXC_TTL_LMT_2 ,
    EXC_OWE_LMT_2 ,
    GEN_CS_DAY ,
    TRANS_COL_DAY )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REQ_DEBT_LMT ,
NEW.EXC_TTL_LMT_1 ,
NEW.EXC_OWE_LMT_1 ,
NEW.EXC_TTL_LMT_2 ,
NEW.EXC_OWE_LMT_2 ,
NEW.GEN_CS_DAY ,
NEW.TRANS_COL_DAY );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_CARD_A AFTER
INSERT
    ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CRD_CARD( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    ID_P_SEQNO ,
    CORP_NO ,
    SOURCE_CODE ,
    SON_CARD_FLAG ,
    MAJOR_RELATION ,
    MAJOR_ID_P_SEQNO ,
    MEMBER_NOTE ,
    MEMBER_ID ,
    FORCE_FLAG ,
    ENG_NAME ,
    REG_BANK_NO ,
    UNIT_CODE ,
    PROMOTE_EMP_NO ,
    INTRODUCE_EMP_NO ,
    INTRODUCE_ID ,
    INTRODUCE_NAME ,
    INTR_REASON_CODE ,
    FEE_CODE ,
    INDIV_CRD_LMT ,
    INDIV_INST_LMT ,
    EXPIRE_REASON ,
    EXPIRE_CHG_DATE ,
    EMBOSS_DATA ,
    SET_CODE ,
    MAIL_TYPE ,
    MAIL_NO ,
    MAIL_BRANCH ,
    MAIL_PROC_DATE ,
    MAIL_REJECT_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_NO ,
NEW.ID_P_SEQNO ,
NEW.CORP_NO ,
NEW.SOURCE_CODE ,
NEW.SON_CARD_FLAG ,
NEW.MAJOR_RELATION ,
NEW.MAJOR_ID_P_SEQNO ,
NEW.MEMBER_NOTE ,
NEW.MEMBER_ID ,
NEW.FORCE_FLAG ,
NEW.ENG_NAME ,
NEW.REG_BANK_NO ,
NEW.UNIT_CODE ,
NEW.PROMOTE_EMP_NO ,
NEW.INTRODUCE_EMP_NO ,
NEW.INTRODUCE_ID ,
NEW.INTRODUCE_NAME ,
NEW.INTR_REASON_CODE ,
NEW.FEE_CODE ,
NEW.INDIV_CRD_LMT ,
NEW.INDIV_INST_LMT ,
NEW.EXPIRE_REASON ,
NEW.EXPIRE_CHG_DATE ,
NEW.EMBOSS_DATA ,
NEW.SET_CODE ,
NEW.MAIL_TYPE ,
NEW.MAIL_NO ,
NEW.MAIL_BRANCH ,
NEW.MAIL_PROC_DATE ,
NEW.MAIL_REJECT_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_CARD_D AFTER
DELETE
    ON
    CRD_CARD REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CRD_CARD( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_NO ,
    ID_P_SEQNO ,
    CORP_NO ,
    SOURCE_CODE ,
    SON_CARD_FLAG ,
    MAJOR_RELATION ,
    MAJOR_ID_P_SEQNO ,
    MEMBER_NOTE ,
    MEMBER_ID ,
    FORCE_FLAG ,
    ENG_NAME ,
    REG_BANK_NO ,
    UNIT_CODE ,
    PROMOTE_EMP_NO ,
    INTRODUCE_EMP_NO ,
    INTRODUCE_ID ,
    INTRODUCE_NAME ,
    INTR_REASON_CODE ,
    FEE_CODE ,
    INDIV_CRD_LMT ,
    INDIV_INST_LMT ,
    EXPIRE_REASON ,
    EXPIRE_CHG_DATE ,
    EMBOSS_DATA ,
    SET_CODE ,
    MAIL_TYPE ,
    MAIL_NO ,
    MAIL_BRANCH ,
    MAIL_PROC_DATE ,
    MAIL_REJECT_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.CARD_NO ,
OLD.ID_P_SEQNO ,
OLD.CORP_NO ,
OLD.SOURCE_CODE ,
OLD.SON_CARD_FLAG ,
OLD.MAJOR_RELATION ,
OLD.MAJOR_ID_P_SEQNO ,
OLD.MEMBER_NOTE ,
OLD.MEMBER_ID ,
OLD.FORCE_FLAG ,
OLD.ENG_NAME ,
OLD.REG_BANK_NO ,
OLD.UNIT_CODE ,
OLD.PROMOTE_EMP_NO ,
OLD.INTRODUCE_EMP_NO ,
OLD.INTRODUCE_ID ,
OLD.INTRODUCE_NAME ,
OLD.INTR_REASON_CODE ,
OLD.FEE_CODE ,
OLD.INDIV_CRD_LMT ,
OLD.INDIV_INST_LMT ,
OLD.EXPIRE_REASON ,
OLD.EXPIRE_CHG_DATE ,
OLD.EMBOSS_DATA ,
OLD.SET_CODE ,
OLD.MAIL_TYPE ,
OLD.MAIL_NO ,
OLD.MAIL_BRANCH ,
OLD.MAIL_PROC_DATE ,
OLD.MAIL_REJECT_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_CRD_CARD_U
AFTER UPDATE OF
 ID_P_SEQNO, bin_type
,CORP_NO
,SOURCE_CODE
,SON_CARD_FLAG
,MAJOR_RELATION
,MAJOR_ID_P_SEQNO
,MEMBER_NOTE
,MEMBER_ID
,FORCE_FLAG
,ENG_NAME
,REG_BANK_NO
,UNIT_CODE
,PROMOTE_EMP_NO
,INTRODUCE_EMP_NO
,INTRODUCE_ID
,INTRODUCE_NAME
,INTR_REASON_CODE
,FEE_CODE
,INDIV_CRD_LMT
,INDIV_INST_LMT
,EXPIRE_REASON
,EXPIRE_CHG_DATE
,EMBOSS_DATA
,SET_CODE
,MAIL_TYPE
,MAIL_NO
,MAIL_BRANCH
,MAIL_PROC_DATE
,MAIL_REJECT_DATE
ON CRD_CARD
REFERENCING OLD AS oo NEW AS nn
FOR EACH ROW MODE DB2SQL
  WHEN (
      oo.source_code <> nn.source_code
   or oo.member_note<>nn.member_note
   or oo.member_id<>nn.member_id
   or oo.force_flag<>nn.force_flag
   or oo.eng_name<>nn.eng_name
   or oo.reg_bank_no<>nn.reg_bank_no
   or oo.unit_code<>nn.unit_code
   or oo.promote_emp_no<>nn.promote_emp_no
   or oo.introduce_emp_no<>nn.introduce_emp_no
   or oo.introduce_id<>nn.introduce_id
   or oo.introduce_name<>nn.introduce_name
   or oo.intr_reason_code<>nn.intr_reason_code
   or oo.fee_code<>nn.fee_code
   or oo.indiv_crd_lmt<>nn.indiv_crd_lmt
   or oo.indiv_inst_lmt<>nn.indiv_inst_lmt
   or oo.expire_reason<>nn.expire_reason
   or oo.expire_chg_date<>nn.expire_chg_date
   or oo.emboss_data<>nn.emboss_data
   or oo.set_code<>nn.set_code
   or oo.mail_type<>nn.mail_type
   or oo.mail_no<>nn.mail_no
   or oo.mail_branch<>nn.mail_branch
   or oo.mail_proc_date<>nn.mail_proc_date
   or oo.mail_reject_date<>nn.mail_reject_date
   or oo.bin_type<>nn.bin_type
     ) 
BEGIN ATOMIC
DECLARE wk_audcode     VARCHAR(1);--
DECLARE wk_user        VARCHAR(10);--
DECLARE wk_pgm         VARCHAR(20);--
DECLARE wk_ws          VARCHAR(20);--
--DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

    set wk_audcode = 'U';--
    
    if oo.bin_type <> nn.bin_type then
      SIGNAL SQLSTATE '75001' ('CRD_CARD: bin_type Unchangeable'||oo.bin_type||'|'||nn.bin_type);--
    end if;--
  
    INSERT INTO LOG_CRD_CARD(
                MOD_TIME
                ,MOD_PGM
                ,MOD_USER
                ,MOD_AUDCODE
                ,K_CARD_NO
                ,ID_P_SEQNO
                ,CORP_NO
                ,SOURCE_CODE
                ,SON_CARD_FLAG
                ,MAJOR_RELATION
                ,MAJOR_ID_P_SEQNO
                ,MEMBER_NOTE
                ,MEMBER_ID
                ,FORCE_FLAG
                ,ENG_NAME
                ,REG_BANK_NO
                ,UNIT_CODE
                ,PROMOTE_EMP_NO
                ,INTRODUCE_EMP_NO
                ,INTRODUCE_ID
                ,INTRODUCE_NAME
                ,INTR_REASON_CODE
                ,FEE_CODE
                ,INDIV_CRD_LMT
                ,INDIV_INST_LMT
                ,EXPIRE_REASON
                ,EXPIRE_CHG_DATE
                ,EMBOSS_DATA
                ,SET_CODE
                ,MAIL_TYPE
                ,MAIL_NO
                ,MAIL_BRANCH
                ,MAIL_PROC_DATE
                ,MAIL_REJECT_DATE
           ) VALUES (
                to_char(sysdate,'yyyymmddhh24misssss')
                ,nn.MOD_PGM
                ,nn.MOD_USER
                ,wk_audcode
                ,nn.CARD_NO
                ,nn.ID_P_SEQNO
                ,nn.CORP_NO
                ,nn.SOURCE_CODE
                ,nn.SON_CARD_FLAG
                ,nn.MAJOR_RELATION
                ,nn.MAJOR_ID_P_SEQNO
                ,nn.MEMBER_NOTE
                ,nn.MEMBER_ID
                ,nn.FORCE_FLAG
                ,nn.ENG_NAME
                ,nn.REG_BANK_NO
                ,nn.UNIT_CODE
                ,nn.PROMOTE_EMP_NO
                ,nn.INTRODUCE_EMP_NO
                ,nn.INTRODUCE_ID
                ,nn.INTRODUCE_NAME
                ,nn.INTR_REASON_CODE
                ,nn.FEE_CODE
                ,nn.INDIV_CRD_LMT
                ,nn.INDIV_INST_LMT
                ,nn.EXPIRE_REASON
                ,nn.EXPIRE_CHG_DATE
                ,nn.EMBOSS_DATA
                ,nn.SET_CODE
                ,nn.MAIL_TYPE
                ,nn.MAIL_NO
                ,nn.MAIL_BRANCH
                ,nn.MAIL_PROC_DATE
                ,nn.MAIL_REJECT_DATE
           );--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_CORP_A AFTER
INSERT
    ON
    CRD_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CRD_CORP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CORP_P_SEQNO ,
    CORP_NO ,
    CHI_NAME ,
    ABBR_NAME ,
    ENG_NAME ,
    REG_ZIP ,
    REG_ADDR1 ,
    REG_ADDR2 ,
    REG_ADDR3 ,
    REG_ADDR4 ,
    REG_ADDR5 ,
    CORP_TEL_ZONE1 ,
    CORP_TEL_NO1 ,
    CORP_TEL_EXT1 ,
    CORP_TEL_ZONE2 ,
    CORP_TEL_NO2 ,
    CORP_TEL_EXT2 ,
    CHARGE_ID ,
    CHARGE_NAME ,
    CHARGE_TEL_ZONE ,
    CHARGE_TEL_NO ,
    CHARGE_TEL_EXT ,
    CAPITAL ,
    BUSINESS_CODE ,
    SETUP_DATE ,
    FORCE_FLAG ,
    CONTACT_NAME ,
    CONTACT_AREA_CODE ,
    CONTACT_TEL_NO ,
    CONTACT_TEL_EXT ,
    CONTACT_ZIP ,
    CONTACT_ADDR1 ,
    CONTACT_ADDR2 ,
    CONTACT_ADDR3 ,
    CONTACT_ADDR4 ,
    CONTACT_ADDR5 ,
    EMBOSS_DATA )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CORP_P_SEQNO ,
NEW.CORP_NO ,
NEW.CHI_NAME ,
NEW.ABBR_NAME ,
NEW.ENG_NAME ,
NEW.REG_ZIP ,
NEW.REG_ADDR1 ,
NEW.REG_ADDR2 ,
NEW.REG_ADDR3 ,
NEW.REG_ADDR4 ,
NEW.REG_ADDR5 ,
NEW.CORP_TEL_ZONE1 ,
NEW.CORP_TEL_NO1 ,
NEW.CORP_TEL_EXT1 ,
NEW.CORP_TEL_ZONE2 ,
NEW.CORP_TEL_NO2 ,
NEW.CORP_TEL_EXT2 ,
NEW.CHARGE_ID ,
NEW.CHARGE_NAME ,
NEW.CHARGE_TEL_ZONE ,
NEW.CHARGE_TEL_NO ,
NEW.CHARGE_TEL_EXT ,
NEW.CAPITAL ,
NEW.BUSINESS_CODE ,
NEW.SETUP_DATE ,
NEW.FORCE_FLAG ,
NEW.CONTACT_NAME ,
NEW.CONTACT_AREA_CODE ,
NEW.CONTACT_TEL_NO ,
NEW.CONTACT_TEL_EXT ,
NEW.CONTACT_ZIP ,
NEW.CONTACT_ADDR1 ,
NEW.CONTACT_ADDR2 ,
NEW.CONTACT_ADDR3 ,
NEW.CONTACT_ADDR4 ,
NEW.CONTACT_ADDR5 ,
NEW.EMBOSS_DATA );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_CRD_CORP_D AFTER DELETE
    ON
    CRD_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
    DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
        LOG_CRD_CORP( MOD_TIME ,
        MOD_PGM ,
        MOD_USER ,
        MOD_AUDCODE ,
        K_CORP_P_SEQNO ,
        CORP_NO ,
        CHI_NAME ,
        ABBR_NAME ,
        ENG_NAME ,
        REG_ZIP ,
        REG_ADDR1 ,
        REG_ADDR2 ,
        REG_ADDR3 ,
        REG_ADDR4 ,
        REG_ADDR5 ,
        CORP_TEL_ZONE1 ,
        CORP_TEL_NO1 ,
        CORP_TEL_EXT1 ,
        CORP_TEL_ZONE2 ,
        CORP_TEL_NO2 ,
        CORP_TEL_EXT2 ,
        CHARGE_ID ,
        CHARGE_NAME ,
        CHARGE_TEL_ZONE ,
        CHARGE_TEL_NO ,
        CHARGE_TEL_EXT ,
        CAPITAL ,
        BUSINESS_CODE ,
        SETUP_DATE ,
        FORCE_FLAG ,
        CONTACT_NAME ,
        CONTACT_AREA_CODE ,
        CONTACT_TEL_NO ,
        CONTACT_TEL_EXT ,
        CONTACT_ZIP ,
        CONTACT_ADDR1 ,
        CONTACT_ADDR2 ,
        CONTACT_ADDR3 ,
        CONTACT_ADDR4 ,
        CONTACT_ADDR5 ,
        EMBOSS_DATA )
    VALUES ( to_char(sysdate,
    'yyyymmddhh24misssss') ,
    OLD.MOD_PGM ,
    OLD.MOD_USER ,
    wk_audcode ,
    OLD.CORP_P_SEQNO ,
    OLD.CORP_NO ,
    OLD.CHI_NAME ,
    OLD.ABBR_NAME ,
    OLD.ENG_NAME ,
    OLD.REG_ZIP ,
    OLD.REG_ADDR1 ,
    OLD.REG_ADDR2 ,
    OLD.REG_ADDR3 ,
    OLD.REG_ADDR4 ,
    OLD.REG_ADDR5 ,
    OLD.CORP_TEL_ZONE1 ,
    OLD.CORP_TEL_NO1 ,
    OLD.CORP_TEL_EXT1 ,
    OLD.CORP_TEL_ZONE2 ,
    OLD.CORP_TEL_NO2 ,
    OLD.CORP_TEL_EXT2 ,
    OLD.CHARGE_ID ,
    OLD.CHARGE_NAME ,
    OLD.CHARGE_TEL_ZONE ,
    OLD.CHARGE_TEL_NO ,
    OLD.CHARGE_TEL_EXT ,
    OLD.CAPITAL ,
    OLD.BUSINESS_CODE ,
    OLD.SETUP_DATE ,
    OLD.FORCE_FLAG ,
    OLD.CONTACT_NAME ,
    OLD.CONTACT_AREA_CODE ,
    OLD.CONTACT_TEL_NO ,
    OLD.CONTACT_TEL_EXT ,
    OLD.CONTACT_ZIP ,
    OLD.CONTACT_ADDR1 ,
    OLD.CONTACT_ADDR2 ,
    OLD.CONTACT_ADDR3 ,
    OLD.CONTACT_ADDR4 ,
    OLD.CONTACT_ADDR5 ,
    OLD.EMBOSS_DATA );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_CORP_U AFTER
UPDATE
    OF CORP_NO ,
    CHI_NAME ,
    ABBR_NAME ,
    ENG_NAME ,
    REG_ZIP ,
    REG_ADDR1 ,
    REG_ADDR2 ,
    REG_ADDR3 ,
    REG_ADDR4 ,
    REG_ADDR5 ,
    CORP_TEL_ZONE1 ,
    CORP_TEL_NO1 ,
    CORP_TEL_EXT1 ,
    CORP_TEL_ZONE2 ,
    CORP_TEL_NO2 ,
    CORP_TEL_EXT2 ,
    CHARGE_ID ,
    CHARGE_NAME ,
    CHARGE_TEL_ZONE ,
    CHARGE_TEL_NO ,
    CHARGE_TEL_EXT ,
    CAPITAL ,
    BUSINESS_CODE ,
    SETUP_DATE ,
    FORCE_FLAG ,
    CONTACT_NAME ,
    CONTACT_AREA_CODE ,
    CONTACT_TEL_NO ,
    CONTACT_TEL_EXT ,
    CONTACT_ZIP ,
    CONTACT_ADDR1 ,
    CONTACT_ADDR2 ,
    CONTACT_ADDR3 ,
    CONTACT_ADDR4 ,
    CONTACT_ADDR5 ,
    EMBOSS_DATA ON
    CRD_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_CRD_CORP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CORP_P_SEQNO ,
    CORP_NO ,
    CHI_NAME ,
    ABBR_NAME ,
    ENG_NAME ,
    REG_ZIP ,
    REG_ADDR1 ,
    REG_ADDR2 ,
    REG_ADDR3 ,
    REG_ADDR4 ,
    REG_ADDR5 ,
    CORP_TEL_ZONE1 ,
    CORP_TEL_NO1 ,
    CORP_TEL_EXT1 ,
    CORP_TEL_ZONE2 ,
    CORP_TEL_NO2 ,
    CORP_TEL_EXT2 ,
    CHARGE_ID ,
    CHARGE_NAME ,
    CHARGE_TEL_ZONE ,
    CHARGE_TEL_NO ,
    CHARGE_TEL_EXT ,
    CAPITAL ,
    BUSINESS_CODE ,
    SETUP_DATE ,
    FORCE_FLAG ,
    CONTACT_NAME ,
    CONTACT_AREA_CODE ,
    CONTACT_TEL_NO ,
    CONTACT_TEL_EXT ,
    CONTACT_ZIP ,
    CONTACT_ADDR1 ,
    CONTACT_ADDR2 ,
    CONTACT_ADDR3 ,
    CONTACT_ADDR4 ,
    CONTACT_ADDR5 ,
    EMBOSS_DATA )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CORP_P_SEQNO ,
NEW.CORP_NO ,
NEW.CHI_NAME ,
NEW.ABBR_NAME ,
NEW.ENG_NAME ,
NEW.REG_ZIP ,
NEW.REG_ADDR1 ,
NEW.REG_ADDR2 ,
NEW.REG_ADDR3 ,
NEW.REG_ADDR4 ,
NEW.REG_ADDR5 ,
NEW.CORP_TEL_ZONE1 ,
NEW.CORP_TEL_NO1 ,
NEW.CORP_TEL_EXT1 ,
NEW.CORP_TEL_ZONE2 ,
NEW.CORP_TEL_NO2 ,
NEW.CORP_TEL_EXT2 ,
NEW.CHARGE_ID ,
NEW.CHARGE_NAME ,
NEW.CHARGE_TEL_ZONE ,
NEW.CHARGE_TEL_NO ,
NEW.CHARGE_TEL_EXT ,
NEW.CAPITAL ,
NEW.BUSINESS_CODE ,
NEW.SETUP_DATE ,
NEW.FORCE_FLAG ,
NEW.CONTACT_NAME ,
NEW.CONTACT_AREA_CODE ,
NEW.CONTACT_TEL_NO ,
NEW.CONTACT_TEL_EXT ,
NEW.CONTACT_ZIP ,
NEW.CONTACT_ADDR1 ,
NEW.CONTACT_ADDR2 ,
NEW.CONTACT_ADDR3 ,
NEW.CONTACT_ADDR4 ,
NEW.CONTACT_ADDR5 ,
NEW.EMBOSS_DATA );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_IDNO_A AFTER
INSERT
    ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CRD_IDNO( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ID_P_SEQNO ,
    ID_NO ,
    CHI_NAME ,
    SEX ,
    BIRTHDAY ,
    MARRIAGE ,
    EDUCATION ,
    STUDENT ,
    NATION ,
    SERVICE_YEAR ,
    ANNUAL_INCOME ,
    STAFF_FLAG ,
    STAFF_BR_NO ,
    CREDIT_FLAG ,
    COMM_FLAG ,
    SALARY_CODE ,
    RESIDENT_NO ,
    OTHER_CNTRY_CODE ,
    PASSPORT_NO ,
    OTHER_ID ,
    OFFICE_AREA_CODE1 ,
    OFFICE_TEL_NO1 ,
    OFFICE_TEL_EXT1 ,
    OFFICE_AREA_CODE2 ,
    OFFICE_TEL_NO2 ,
    OFFICE_TEL_EXT2 ,
    HOME_AREA_CODE1 ,
    HOME_TEL_NO1 ,
    HOME_TEL_EXT1 ,
    HOME_AREA_CODE2 ,
    HOME_TEL_NO2 ,
    HOME_TEL_EXT2 ,
    RESIDENT_ZIP ,
    RESIDENT_ADDR1 ,
    RESIDENT_ADDR2 ,
    RESIDENT_ADDR3 ,
    RESIDENT_ADDR4 ,
    RESIDENT_ADDR5 ,
    JOB_POSITION ,
    COMPANY_NAME ,
    BUSINESS_ID ,
    BUSINESS_CODE ,
    CELLAR_PHONE ,
    FAX_NO ,
    E_MAIL_ADDR ,
    SPECIAL_CODE ,
    VOICE_PASSWD_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
'trigger' ,
'trigger' ,
wk_audcode ,
NEW.ID_P_SEQNO ,
NEW.ID_NO ,
NEW.CHI_NAME ,
NEW.SEX ,
NEW.BIRTHDAY ,
NEW.MARRIAGE ,
NEW.EDUCATION ,
NEW.STUDENT ,
NEW.NATION ,
NEW.SERVICE_YEAR ,
NEW.ANNUAL_INCOME ,
NEW.STAFF_FLAG ,
NEW.STAFF_BR_NO ,
NEW.CREDIT_FLAG ,
NEW.COMM_FLAG ,
NEW.SALARY_CODE ,
NEW.RESIDENT_NO ,
NEW.OTHER_CNTRY_CODE ,
NEW.PASSPORT_NO ,
NEW.OTHER_ID ,
NEW.OFFICE_AREA_CODE1 ,
NEW.OFFICE_TEL_NO1 ,
NEW.OFFICE_TEL_EXT1 ,
NEW.OFFICE_AREA_CODE2 ,
NEW.OFFICE_TEL_NO2 ,
NEW.OFFICE_TEL_EXT2 ,
NEW.HOME_AREA_CODE1 ,
NEW.HOME_TEL_NO1 ,
NEW.HOME_TEL_EXT1 ,
NEW.HOME_AREA_CODE2 ,
NEW.HOME_TEL_NO2 ,
NEW.HOME_TEL_EXT2 ,
NEW.RESIDENT_ZIP ,
NEW.RESIDENT_ADDR1 ,
NEW.RESIDENT_ADDR2 ,
NEW.RESIDENT_ADDR3 ,
NEW.RESIDENT_ADDR4 ,
NEW.RESIDENT_ADDR5 ,
NEW.JOB_POSITION ,
NEW.COMPANY_NAME ,
NEW.BUSINESS_ID ,
NEW.BUSINESS_CODE ,
NEW.CELLAR_PHONE ,
NEW.FAX_NO ,
NEW.E_MAIL_ADDR ,
NEW.SPECIAL_CODE ,
NEW.VOICE_PASSWD_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_IDNO_D AFTER
DELETE
    ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CRD_IDNO( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ID_P_SEQNO ,
    ID_NO ,
    CHI_NAME ,
    SEX ,
    BIRTHDAY ,
    MARRIAGE ,
    EDUCATION ,
    STUDENT ,
    NATION ,
    SERVICE_YEAR ,
    ANNUAL_INCOME ,
    STAFF_FLAG ,
    STAFF_BR_NO ,
    CREDIT_FLAG ,
    COMM_FLAG ,
    SALARY_CODE ,
    RESIDENT_NO ,
    OTHER_CNTRY_CODE ,
    PASSPORT_NO ,
    OTHER_ID ,
    OFFICE_AREA_CODE1 ,
    OFFICE_TEL_NO1 ,
    OFFICE_TEL_EXT1 ,
    OFFICE_AREA_CODE2 ,
    OFFICE_TEL_NO2 ,
    OFFICE_TEL_EXT2 ,
    HOME_AREA_CODE1 ,
    HOME_TEL_NO1 ,
    HOME_TEL_EXT1 ,
    HOME_AREA_CODE2 ,
    HOME_TEL_NO2 ,
    HOME_TEL_EXT2 ,
    RESIDENT_ZIP ,
    RESIDENT_ADDR1 ,
    RESIDENT_ADDR2 ,
    RESIDENT_ADDR3 ,
    RESIDENT_ADDR4 ,
    RESIDENT_ADDR5 ,
    JOB_POSITION ,
    COMPANY_NAME ,
    BUSINESS_ID ,
    BUSINESS_CODE ,
    CELLAR_PHONE ,
    FAX_NO ,
    E_MAIL_ADDR ,
    SPECIAL_CODE ,
    VOICE_PASSWD_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
'trigger' ,
'trigger' ,
wk_audcode ,
NEW.ID_P_SEQNO ,
NEW.ID_NO ,
NEW.CHI_NAME ,
NEW.SEX ,
NEW.BIRTHDAY ,
NEW.MARRIAGE ,
NEW.EDUCATION ,
NEW.STUDENT ,
NEW.NATION ,
NEW.SERVICE_YEAR ,
NEW.ANNUAL_INCOME ,
NEW.STAFF_FLAG ,
NEW.STAFF_BR_NO ,
NEW.CREDIT_FLAG ,
NEW.COMM_FLAG ,
NEW.SALARY_CODE ,
NEW.RESIDENT_NO ,
NEW.OTHER_CNTRY_CODE ,
NEW.PASSPORT_NO ,
NEW.OTHER_ID ,
NEW.OFFICE_AREA_CODE1 ,
NEW.OFFICE_TEL_NO1 ,
NEW.OFFICE_TEL_EXT1 ,
NEW.OFFICE_AREA_CODE2 ,
NEW.OFFICE_TEL_NO2 ,
NEW.OFFICE_TEL_EXT2 ,
NEW.HOME_AREA_CODE1 ,
NEW.HOME_TEL_NO1 ,
NEW.HOME_TEL_EXT1 ,
NEW.HOME_AREA_CODE2 ,
NEW.HOME_TEL_NO2 ,
NEW.HOME_TEL_EXT2 ,
NEW.RESIDENT_ZIP ,
NEW.RESIDENT_ADDR1 ,
NEW.RESIDENT_ADDR2 ,
NEW.RESIDENT_ADDR3 ,
NEW.RESIDENT_ADDR4 ,
NEW.RESIDENT_ADDR5 ,
NEW.JOB_POSITION ,
NEW.COMPANY_NAME ,
NEW.BUSINESS_ID ,
NEW.BUSINESS_CODE ,
NEW.CELLAR_PHONE ,
NEW.FAX_NO ,
NEW.E_MAIL_ADDR ,
NEW.SPECIAL_CODE ,
NEW.VOICE_PASSWD_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_IDNO_U AFTER
UPDATE
    OF ANNUAL_INCOME,
    BIRTHDAY,
    BUSINESS_CODE,
    BUSINESS_ID,
    CELLAR_PHONE,
    CHI_NAME,
    COMM_FLAG,
    COMPANY_NAME,
    CREDIT_FLAG,
    EDUCATION,
    E_MAIL_ADDR,
    FAX_NO,
    HOME_AREA_CODE1,
    HOME_AREA_CODE2,
    HOME_TEL_EXT1,
    HOME_TEL_EXT2,
    HOME_TEL_NO1,
    HOME_TEL_NO2,
    ID_NO,
    JOB_POSITION,
    MARRIAGE,
    NATION,
    OFFICE_AREA_CODE1,
    OFFICE_AREA_CODE2,
    OFFICE_TEL_EXT1,
    OFFICE_TEL_EXT2,
    OFFICE_TEL_NO1,
    OFFICE_TEL_NO2,
    OTHER_CNTRY_CODE,
    OTHER_ID,
    PASSPORT_NO,
    RESIDENT_ADDR1,
    RESIDENT_ADDR2,
    RESIDENT_ADDR3,
    RESIDENT_ADDR4,
    RESIDENT_ADDR5,
    RESIDENT_NO,
    RESIDENT_ZIP,
    SALARY_CODE,
    SERVICE_YEAR,
    SEX,
    STAFF_BR_NO,
    STAFF_FLAG,
    STUDENT,
    SPECIAL_CODE ,
    VOICE_PASSWD_FLAG ON
    CRD_IDNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_CRD_IDNO( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ID_P_SEQNO ,
    ID_NO ,
    CHI_NAME ,
    SEX ,
    BIRTHDAY ,
    MARRIAGE ,
    EDUCATION ,
    STUDENT ,
    NATION ,
    SERVICE_YEAR ,
    ANNUAL_INCOME ,
    STAFF_FLAG ,
    STAFF_BR_NO ,
    CREDIT_FLAG ,
    COMM_FLAG ,
    SALARY_CODE ,
    RESIDENT_NO ,
    OTHER_CNTRY_CODE ,
    PASSPORT_NO ,
    OTHER_ID ,
    OFFICE_AREA_CODE1 ,
    OFFICE_TEL_NO1 ,
    OFFICE_TEL_EXT1 ,
    OFFICE_AREA_CODE2 ,
    OFFICE_TEL_NO2 ,
    OFFICE_TEL_EXT2 ,
    HOME_AREA_CODE1 ,
    HOME_TEL_NO1 ,
    HOME_TEL_EXT1 ,
    HOME_AREA_CODE2 ,
    HOME_TEL_NO2 ,
    HOME_TEL_EXT2 ,
    RESIDENT_ZIP ,
    RESIDENT_ADDR1 ,
    RESIDENT_ADDR2 ,
    RESIDENT_ADDR3 ,
    RESIDENT_ADDR4 ,
    RESIDENT_ADDR5 ,
    JOB_POSITION ,
    COMPANY_NAME ,
    BUSINESS_ID ,
    BUSINESS_CODE ,
    CELLAR_PHONE ,
    FAX_NO ,
    E_MAIL_ADDR ,
    SPECIAL_CODE ,
    VOICE_PASSWD_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
'trigger' ,
'trigger' ,
wk_audcode ,
OLD.ID_P_SEQNO ,
OLD.ID_NO ,
OLD.CHI_NAME ,
OLD.SEX ,
OLD.BIRTHDAY ,
OLD.MARRIAGE ,
OLD.EDUCATION ,
OLD.STUDENT ,
OLD.NATION ,
OLD.SERVICE_YEAR ,
OLD.ANNUAL_INCOME ,
OLD.STAFF_FLAG ,
OLD.STAFF_BR_NO ,
OLD.CREDIT_FLAG ,
OLD.COMM_FLAG ,
OLD.SALARY_CODE ,
OLD.RESIDENT_NO ,
OLD.OTHER_CNTRY_CODE ,
OLD.PASSPORT_NO ,
OLD.OTHER_ID ,
OLD.OFFICE_AREA_CODE1 ,
OLD.OFFICE_TEL_NO1 ,
OLD.OFFICE_TEL_EXT1 ,
OLD.OFFICE_AREA_CODE2 ,
OLD.OFFICE_TEL_NO2 ,
OLD.OFFICE_TEL_EXT2 ,
OLD.HOME_AREA_CODE1 ,
OLD.HOME_TEL_NO1 ,
OLD.HOME_TEL_EXT1 ,
OLD.HOME_AREA_CODE2 ,
OLD.HOME_TEL_NO2 ,
OLD.HOME_TEL_EXT2 ,
OLD.RESIDENT_ZIP ,
OLD.RESIDENT_ADDR1 ,
OLD.RESIDENT_ADDR2 ,
OLD.RESIDENT_ADDR3 ,
OLD.RESIDENT_ADDR4 ,
OLD.RESIDENT_ADDR5 ,
OLD.JOB_POSITION ,
OLD.COMPANY_NAME ,
OLD.BUSINESS_ID ,
OLD.BUSINESS_CODE ,
OLD.CELLAR_PHONE ,
OLD.FAX_NO ,
OLD.E_MAIL_ADDR ,
OLD.SPECIAL_CODE ,
OLD.VOICE_PASSWD_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_RELA_A AFTER
DELETE
    OR
INSERT
    OR
UPDATE
    OF SEX ,
    BIRTHDAY ,
    ID_P_SEQNO ,
    RELA_NAME ,
    COMPANY_NAME ,
    COMPANY_ZIP ,
    COMPANY_ADDR1 ,
    COMPANY_ADDR2 ,
    COMPANY_ADDR3 ,
    COMPANY_ADDR4 ,
    COMPANY_ADDR5 ,
    OFFICE_AREA_CODE1 ,
    OFFICE_TEL_NO1 ,
    OFFICE_TEL_EXT1 ,
    OFFICE_AREA_CODE2 ,
    OFFICE_TEL_NO2 ,
    OFFICE_TEL_EXT2 ,
    HOME_AREA_CODE1 ,
    HOME_TEL_NO1 ,
    HOME_TEL_EXT1 ,
    HOME_AREA_CODE2 ,
    HOME_TEL_NO2 ,
    HOME_TEL_EXT2 ,
    RESIDENT_ZIP ,
    RESIDENT_ADDR1 ,
    RESIDENT_ADDR2 ,
    RESIDENT_ADDR3 ,
    RESIDENT_ADDR4 ,
    RESIDENT_ADDR5 ,
    MAIL_ZIP ,
    MAIL_ADDR1 ,
    MAIL_ADDR2 ,
    MAIL_ADDR3 ,
    MAIL_ADDR4 ,
    MAIL_ADDR5 ,
    CELLAR_PHONE ,
    START_DATE ,
    END_DATE ON
    CRD_RELA REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CRD_RELA( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_RELA_TYPE ,
    K_P_SEQNO ,
    K_RELA_ID ,
    SEX ,
    BIRTHDAY ,
    ID_P_SEQNO ,
    RELA_NAME ,
    COMPANY_NAME ,
    COMPANY_ZIP ,
    COMPANY_ADDR1 ,
    COMPANY_ADDR2 ,
    COMPANY_ADDR3 ,
    COMPANY_ADDR4 ,
    COMPANY_ADDR5 ,
    OFFICE_AREA_CODE1 ,
    OFFICE_TEL_NO1 ,
    OFFICE_TEL_EXT1 ,
    OFFICE_AREA_CODE2 ,
    OFFICE_TEL_NO2 ,
    OFFICE_TEL_EXT2 ,
    HOME_AREA_CODE1 ,
    HOME_TEL_NO1 ,
    HOME_TEL_EXT1 ,
    HOME_AREA_CODE2 ,
    HOME_TEL_NO2 ,
    HOME_TEL_EXT2 ,
    RESIDENT_ZIP ,
    RESIDENT_ADDR1 ,
    RESIDENT_ADDR2 ,
    RESIDENT_ADDR3 ,
    RESIDENT_ADDR4 ,
    RESIDENT_ADDR5 ,
    MAIL_ZIP ,
    MAIL_ADDR1 ,
    MAIL_ADDR2 ,
    MAIL_ADDR3 ,
    MAIL_ADDR4 ,
    MAIL_ADDR5 ,
    CELLAR_PHONE ,
    START_DATE ,
    END_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.RELA_TYPE ,
NEW.ACNO_P_SEQNO ,
NEW.RELA_ID ,
NEW.SEX ,
NEW.BIRTHDAY ,
NEW.ID_P_SEQNO ,
NEW.RELA_NAME ,
NEW.COMPANY_NAME ,
NEW.COMPANY_ZIP ,
NEW.COMPANY_ADDR1 ,
NEW.COMPANY_ADDR2 ,
NEW.COMPANY_ADDR3 ,
NEW.COMPANY_ADDR4 ,
NEW.COMPANY_ADDR5 ,
NEW.OFFICE_AREA_CODE1 ,
NEW.OFFICE_TEL_NO1 ,
NEW.OFFICE_TEL_EXT1 ,
NEW.OFFICE_AREA_CODE2 ,
NEW.OFFICE_TEL_NO2 ,
NEW.OFFICE_TEL_EXT2 ,
NEW.HOME_AREA_CODE1 ,
NEW.HOME_TEL_NO1 ,
NEW.HOME_TEL_EXT1 ,
NEW.HOME_AREA_CODE2 ,
NEW.HOME_TEL_NO2 ,
NEW.HOME_TEL_EXT2 ,
NEW.RESIDENT_ZIP ,
NEW.RESIDENT_ADDR1 ,
NEW.RESIDENT_ADDR2 ,
NEW.RESIDENT_ADDR3 ,
NEW.RESIDENT_ADDR4 ,
NEW.RESIDENT_ADDR5 ,
NEW.MAIL_ZIP ,
NEW.MAIL_ADDR1 ,
NEW.MAIL_ADDR2 ,
NEW.MAIL_ADDR3 ,
NEW.MAIL_ADDR4 ,
NEW.MAIL_ADDR5 ,
NEW.CELLAR_PHONE ,
NEW.START_DATE ,
NEW.END_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_RELA_D AFTER
DELETE
    ON
    CRD_RELA REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CRD_RELA( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_RELA_TYPE ,
    K_P_SEQNO ,
    K_RELA_ID ,
    SEX ,
    BIRTHDAY ,
    ID_P_SEQNO ,
    RELA_NAME ,
    COMPANY_NAME ,
    COMPANY_ZIP ,
    COMPANY_ADDR1 ,
    COMPANY_ADDR2 ,
    COMPANY_ADDR3 ,
    COMPANY_ADDR4 ,
    COMPANY_ADDR5 ,
    OFFICE_AREA_CODE1 ,
    OFFICE_TEL_NO1 ,
    OFFICE_TEL_EXT1 ,
    OFFICE_AREA_CODE2 ,
    OFFICE_TEL_NO2 ,
    OFFICE_TEL_EXT2 ,
    HOME_AREA_CODE1 ,
    HOME_TEL_NO1 ,
    HOME_TEL_EXT1 ,
    HOME_AREA_CODE2 ,
    HOME_TEL_NO2 ,
    HOME_TEL_EXT2 ,
    RESIDENT_ZIP ,
    RESIDENT_ADDR1 ,
    RESIDENT_ADDR2 ,
    RESIDENT_ADDR3 ,
    RESIDENT_ADDR4 ,
    RESIDENT_ADDR5 ,
    MAIL_ZIP ,
    MAIL_ADDR1 ,
    MAIL_ADDR2 ,
    MAIL_ADDR3 ,
    MAIL_ADDR4 ,
    MAIL_ADDR5 ,
    CELLAR_PHONE ,
    START_DATE ,
    END_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.RELA_TYPE ,
OLD.ACNO_P_SEQNO ,
OLD.RELA_ID ,
OLD.SEX ,
OLD.BIRTHDAY ,
OLD.ID_P_SEQNO ,
OLD.RELA_NAME ,
OLD.COMPANY_NAME ,
OLD.COMPANY_ZIP ,
OLD.COMPANY_ADDR1 ,
OLD.COMPANY_ADDR2 ,
OLD.COMPANY_ADDR3 ,
OLD.COMPANY_ADDR4 ,
OLD.COMPANY_ADDR5 ,
OLD.OFFICE_AREA_CODE1 ,
OLD.OFFICE_TEL_NO1 ,
OLD.OFFICE_TEL_EXT1 ,
OLD.OFFICE_AREA_CODE2 ,
OLD.OFFICE_TEL_NO2 ,
OLD.OFFICE_TEL_EXT2 ,
OLD.HOME_AREA_CODE1 ,
OLD.HOME_TEL_NO1 ,
OLD.HOME_TEL_EXT1 ,
OLD.HOME_AREA_CODE2 ,
OLD.HOME_TEL_NO2 ,
OLD.HOME_TEL_EXT2 ,
OLD.RESIDENT_ZIP ,
OLD.RESIDENT_ADDR1 ,
OLD.RESIDENT_ADDR2 ,
OLD.RESIDENT_ADDR3 ,
OLD.RESIDENT_ADDR4 ,
OLD.RESIDENT_ADDR5 ,
OLD.MAIL_ZIP ,
OLD.MAIL_ADDR1 ,
OLD.MAIL_ADDR2 ,
OLD.MAIL_ADDR3 ,
OLD.MAIL_ADDR4 ,
OLD.MAIL_ADDR5 ,
OLD.CELLAR_PHONE ,
OLD.START_DATE ,
OLD.END_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CRD_RELA_U AFTER
UPDATE
    OF SEX ,
    BIRTHDAY ,
    ID_P_SEQNO ,
    RELA_NAME ,
    COMPANY_NAME ,
    COMPANY_ZIP ,
    COMPANY_ADDR1 ,
    COMPANY_ADDR2 ,
    COMPANY_ADDR3 ,
    COMPANY_ADDR4 ,
    COMPANY_ADDR5 ,
    OFFICE_AREA_CODE1 ,
    OFFICE_TEL_NO1 ,
    OFFICE_TEL_EXT1 ,
    OFFICE_AREA_CODE2 ,
    OFFICE_TEL_NO2 ,
    OFFICE_TEL_EXT2 ,
    HOME_AREA_CODE1 ,
    HOME_TEL_NO1 ,
    HOME_TEL_EXT1 ,
    HOME_AREA_CODE2 ,
    HOME_TEL_NO2 ,
    HOME_TEL_EXT2 ,
    RESIDENT_ZIP ,
    RESIDENT_ADDR1 ,
    RESIDENT_ADDR2 ,
    RESIDENT_ADDR3 ,
    RESIDENT_ADDR4 ,
    RESIDENT_ADDR5 ,
    MAIL_ZIP ,
    MAIL_ADDR1 ,
    MAIL_ADDR2 ,
    MAIL_ADDR3 ,
    MAIL_ADDR4 ,
    MAIL_ADDR5 ,
    CELLAR_PHONE ,
    START_DATE ,
    END_DATE ON
    CRD_RELA REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_CRD_RELA( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_RELA_TYPE ,
    K_P_SEQNO ,
    K_RELA_ID ,
    SEX ,
    BIRTHDAY ,
    ID_P_SEQNO ,
    RELA_NAME ,
    COMPANY_NAME ,
    COMPANY_ZIP ,
    COMPANY_ADDR1 ,
    COMPANY_ADDR2 ,
    COMPANY_ADDR3 ,
    COMPANY_ADDR4 ,
    COMPANY_ADDR5 ,
    OFFICE_AREA_CODE1 ,
    OFFICE_TEL_NO1 ,
    OFFICE_TEL_EXT1 ,
    OFFICE_AREA_CODE2 ,
    OFFICE_TEL_NO2 ,
    OFFICE_TEL_EXT2 ,
    HOME_AREA_CODE1 ,
    HOME_TEL_NO1 ,
    HOME_TEL_EXT1 ,
    HOME_AREA_CODE2 ,
    HOME_TEL_NO2 ,
    HOME_TEL_EXT2 ,
    RESIDENT_ZIP ,
    RESIDENT_ADDR1 ,
    RESIDENT_ADDR2 ,
    RESIDENT_ADDR3 ,
    RESIDENT_ADDR4 ,
    RESIDENT_ADDR5 ,
    MAIL_ZIP ,
    MAIL_ADDR1 ,
    MAIL_ADDR2 ,
    MAIL_ADDR3 ,
    MAIL_ADDR4 ,
    MAIL_ADDR5 ,
    CELLAR_PHONE ,
    START_DATE ,
    END_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.RELA_TYPE ,
NEW.ACNO_P_SEQNO ,
NEW.RELA_ID ,
NEW.SEX ,
NEW.BIRTHDAY ,
NEW.ID_P_SEQNO ,
NEW.RELA_NAME ,
NEW.COMPANY_NAME ,
NEW.COMPANY_ZIP ,
NEW.COMPANY_ADDR1 ,
NEW.COMPANY_ADDR2 ,
NEW.COMPANY_ADDR3 ,
NEW.COMPANY_ADDR4 ,
NEW.COMPANY_ADDR5 ,
NEW.OFFICE_AREA_CODE1 ,
NEW.OFFICE_TEL_NO1 ,
NEW.OFFICE_TEL_EXT1 ,
NEW.OFFICE_AREA_CODE2 ,
NEW.OFFICE_TEL_NO2 ,
NEW.OFFICE_TEL_EXT2 ,
NEW.HOME_AREA_CODE1 ,
NEW.HOME_TEL_NO1 ,
NEW.HOME_TEL_EXT1 ,
NEW.HOME_AREA_CODE2 ,
NEW.HOME_TEL_NO2 ,
NEW.HOME_TEL_EXT2 ,
NEW.RESIDENT_ZIP ,
NEW.RESIDENT_ADDR1 ,
NEW.RESIDENT_ADDR2 ,
NEW.RESIDENT_ADDR3 ,
NEW.RESIDENT_ADDR4 ,
NEW.RESIDENT_ADDR5 ,
NEW.MAIL_ZIP ,
NEW.MAIL_ADDR1 ,
NEW.MAIL_ADDR2 ,
NEW.MAIL_ADDR3 ,
NEW.MAIL_ADDR4 ,
NEW.MAIL_ADDR5 ,
NEW.CELLAR_PHONE ,
NEW.START_DATE ,
NEW.END_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_ANUL_CORP_A AFTER
INSERT
    ON
    CYC_ANUL_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CYC_ANUL_CORP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    K_CORP_P_SEQNO ,
    CORP_NO ,
    LOAN_AMT ,
    PURCH_AMT ,
    LOAN_FREE ,
    PURCH_FREE ,
    TOTAL_FREE_CNT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ACCT_TYPE ,
NEW.CORP_P_SEQNO ,
NEW.CORP_NO ,
NEW.LOAN_AMT ,
NEW.PURCH_AMT ,
NEW.LOAN_FREE ,
NEW.PURCH_FREE ,
NEW.TOTAL_FREE_CNT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_ANUL_CORP_D AFTER
DELETE
    ON
    CYC_ANUL_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CYC_ANUL_CORP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    K_CORP_P_SEQNO ,
    CORP_NO ,
    LOAN_AMT ,
    PURCH_AMT ,
    LOAN_FREE ,
    PURCH_FREE ,
    TOTAL_FREE_CNT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.ACCT_TYPE ,
OLD.CORP_P_SEQNO ,
OLD.CORP_NO ,
OLD.LOAN_AMT ,
OLD.PURCH_AMT ,
OLD.LOAN_FREE ,
OLD.PURCH_FREE ,
OLD.TOTAL_FREE_CNT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_ANUL_CORP_U AFTER
UPDATE
    OF CORP_NO ,
    LOAN_AMT ,
    PURCH_AMT ,
    LOAN_FREE ,
    PURCH_FREE ,
    TOTAL_FREE_CNT ON
    CYC_ANUL_CORP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_CYC_ANUL_CORP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    K_CORP_P_SEQNO ,
    CORP_NO ,
    LOAN_AMT ,
    PURCH_AMT ,
    LOAN_FREE ,
    PURCH_FREE ,
    TOTAL_FREE_CNT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ACCT_TYPE ,
NEW.CORP_P_SEQNO ,
NEW.CORP_NO ,
NEW.LOAN_AMT ,
NEW.PURCH_AMT ,
NEW.LOAN_FREE ,
NEW.PURCH_FREE ,
NEW.TOTAL_FREE_CNT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_ANUL_SPEC_A AFTER
INSERT
    ON
    CYC_ANUL_SPEC REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CYC_ANUL_SPEC( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    STAFF_FLAG ,
    ON_US_BANK ,
    OTHER_BANK ,
    SALARY_FLAG ,
    CREDIT_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ACCT_TYPE ,
NEW.STAFF_FLAG ,
NEW.ON_US_BANK ,
NEW.OTHER_BANK ,
NEW.SALARY_FLAG ,
NEW.CREDIT_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_ANUL_SPEC_D AFTER
DELETE
    ON
    CYC_ANUL_SPEC REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CYC_ANUL_SPEC( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    STAFF_FLAG ,
    ON_US_BANK ,
    OTHER_BANK ,
    SALARY_FLAG ,
    CREDIT_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.ACCT_TYPE ,
OLD.STAFF_FLAG ,
OLD.ON_US_BANK ,
OLD.OTHER_BANK ,
OLD.SALARY_FLAG ,
OLD.CREDIT_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_ANUL_SPEC_U AFTER
UPDATE
    OF STAFF_FLAG ,
    ON_US_BANK ,
    OTHER_BANK ,
    SALARY_FLAG ,
    CREDIT_FLAG ON
    CYC_ANUL_SPEC REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_CYC_ANUL_SPEC( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    STAFF_FLAG ,
    ON_US_BANK ,
    OTHER_BANK ,
    SALARY_FLAG ,
    CREDIT_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ACCT_TYPE ,
NEW.STAFF_FLAG ,
NEW.ON_US_BANK ,
NEW.OTHER_BANK ,
NEW.SALARY_FLAG ,
NEW.CREDIT_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_BPID_A AFTER
INSERT
    ON
    CYC_BPID REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CYC_BPID( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_YEARS ,
    K_ACCT_TYPE ,
    K_ITEM_CODE ,
    OTHER_ITEM ,
    MERCHANT_SEL ,
    MCHT_GROUP_SEL ,
    GROUP_CARD_SEL ,
    GROUP_MERCHANT_SEL
    --,GROUP_1
    --,GROUP_2
    --,GROUP_3
    --,GROUP_4
    --,GROUP_5
,
    LIMIT_1_BEG ,
    LIMIT_1_END ,
    EXCHANGE_1 ,
    LIMIT_2_BEG ,
    LIMIT_2_END ,
    EXCHANGE_2 ,
    LIMIT_3_BEG ,
    LIMIT_3_END ,
    EXCHANGE_3 ,
    LIMIT_4_BEG ,
    LIMIT_4_END ,
    EXCHANGE_4 ,
    LIMIT_5_BEG ,
    LIMIT_5_END ,
    EXCHANGE_5 ,
    LIMIT_6_BEG ,
    LIMIT_6_END ,
    EXCHANGE_6 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.YEARS ,
NEW.ACCT_TYPE ,
NEW.ITEM_CODE ,
NEW.OTHER_ITEM ,
NEW.MERCHANT_SEL ,
NEW.MCHT_GROUP_SEL ,
NEW.GROUP_CARD_SEL ,
NEW.GROUP_MERCHANT_SEL
--,NEW.GROUP_1
--,NEW.GROUP_2
--,NEW.GROUP_3
--,NEW.GROUP_4
--,NEW.GROUP_5
,
NEW.LIMIT_1_BEG ,
NEW.LIMIT_1_END ,
NEW.EXCHANGE_1 ,
NEW.LIMIT_2_BEG ,
NEW.LIMIT_2_END ,
NEW.EXCHANGE_2 ,
NEW.LIMIT_3_BEG ,
NEW.LIMIT_3_END ,
NEW.EXCHANGE_3 ,
NEW.LIMIT_4_BEG ,
NEW.LIMIT_4_END ,
NEW.EXCHANGE_4 ,
NEW.LIMIT_5_BEG ,
NEW.LIMIT_5_END ,
NEW.EXCHANGE_5 ,
NEW.LIMIT_6_BEG ,
NEW.LIMIT_6_END ,
NEW.EXCHANGE_6 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_BPID_D AFTER
DELETE
    ON
    CYC_BPID REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CYC_BPID( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_YEARS ,
    K_ACCT_TYPE ,
    K_ITEM_CODE ,
    OTHER_ITEM ,
    MERCHANT_SEL ,
    MCHT_GROUP_SEL ,
    GROUP_CARD_SEL ,
    GROUP_MERCHANT_SEL
    --,GROUP_1
    --,GROUP_2
    --,GROUP_3
    --,GROUP_4
    --,GROUP_5
,
    LIMIT_1_BEG ,
    LIMIT_1_END ,
    EXCHANGE_1 ,
    LIMIT_2_BEG ,
    LIMIT_2_END ,
    EXCHANGE_2 ,
    LIMIT_3_BEG ,
    LIMIT_3_END ,
    EXCHANGE_3 ,
    LIMIT_4_BEG ,
    LIMIT_4_END ,
    EXCHANGE_4 ,
    LIMIT_5_BEG ,
    LIMIT_5_END ,
    EXCHANGE_5 ,
    LIMIT_6_BEG ,
    LIMIT_6_END ,
    EXCHANGE_6 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.YEARS ,
OLD.ACCT_TYPE ,
OLD.ITEM_CODE ,
OLD.OTHER_ITEM ,
OLD.MERCHANT_SEL ,
OLD.MCHT_GROUP_SEL ,
OLD.GROUP_CARD_SEL ,
OLD.GROUP_MERCHANT_SEL
--,OLD.GROUP_1
--,OLD.GROUP_2
--,OLD.GROUP_3
--,OLD.GROUP_4
--,OLD.GROUP_5
,
OLD.LIMIT_1_BEG ,
OLD.LIMIT_1_END ,
OLD.EXCHANGE_1 ,
OLD.LIMIT_2_BEG ,
OLD.LIMIT_2_END ,
OLD.EXCHANGE_2 ,
OLD.LIMIT_3_BEG ,
OLD.LIMIT_3_END ,
OLD.EXCHANGE_3 ,
OLD.LIMIT_4_BEG ,
OLD.LIMIT_4_END ,
OLD.EXCHANGE_4 ,
OLD.LIMIT_5_BEG ,
OLD.LIMIT_5_END ,
OLD.EXCHANGE_5 ,
OLD.LIMIT_6_BEG ,
OLD.LIMIT_6_END ,
OLD.EXCHANGE_6 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_BPID_U AFTER
UPDATE
    OF OTHER_ITEM ,
    MERCHANT_SEL ,
    MCHT_GROUP_SEL ,
    GROUP_CARD_SEL ,
    GROUP_MERCHANT_SEL
    --,GROUP_1
    --,GROUP_2
    --,GROUP_3
    --,GROUP_4
    --,GROUP_5
,
    LIMIT_1_BEG ,
    LIMIT_1_END ,
    EXCHANGE_1 ,
    LIMIT_2_BEG ,
    LIMIT_2_END ,
    EXCHANGE_2 ,
    LIMIT_3_BEG ,
    LIMIT_3_END ,
    EXCHANGE_3 ,
    LIMIT_4_BEG ,
    LIMIT_4_END ,
    EXCHANGE_4 ,
    LIMIT_5_BEG ,
    LIMIT_5_END ,
    EXCHANGE_5 ,
    LIMIT_6_BEG ,
    LIMIT_6_END ,
    EXCHANGE_6 ON
    CYC_BPID REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_CYC_BPID( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_YEARS ,
    K_ACCT_TYPE ,
    K_ITEM_CODE ,
    OTHER_ITEM ,
    MERCHANT_SEL ,
    MCHT_GROUP_SEL ,
    GROUP_CARD_SEL ,
    GROUP_MERCHANT_SEL
    --,GROUP_1
    --,GROUP_2
    --,GROUP_3
    --,GROUP_4
    --,GROUP_5
,
    LIMIT_1_BEG ,
    LIMIT_1_END ,
    EXCHANGE_1 ,
    LIMIT_2_BEG ,
    LIMIT_2_END ,
    EXCHANGE_2 ,
    LIMIT_3_BEG ,
    LIMIT_3_END ,
    EXCHANGE_3 ,
    LIMIT_4_BEG ,
    LIMIT_4_END ,
    EXCHANGE_4 ,
    LIMIT_5_BEG ,
    LIMIT_5_END ,
    EXCHANGE_5 ,
    LIMIT_6_BEG ,
    LIMIT_6_END ,
    EXCHANGE_6 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.YEARS ,
NEW.ACCT_TYPE ,
NEW.ITEM_CODE ,
NEW.OTHER_ITEM ,
NEW.MERCHANT_SEL ,
NEW.MCHT_GROUP_SEL ,
NEW.GROUP_CARD_SEL ,
NEW.GROUP_MERCHANT_SEL
--,NEW.GROUP_1
--,NEW.GROUP_2
--,NEW.GROUP_3
--,NEW.GROUP_4
--,NEW.GROUP_5
,
NEW.LIMIT_1_BEG ,
NEW.LIMIT_1_END ,
NEW.EXCHANGE_1 ,
NEW.LIMIT_2_BEG ,
NEW.LIMIT_2_END ,
NEW.EXCHANGE_2 ,
NEW.LIMIT_3_BEG ,
NEW.LIMIT_3_END ,
NEW.EXCHANGE_3 ,
NEW.LIMIT_4_BEG ,
NEW.LIMIT_4_END ,
NEW.EXCHANGE_4 ,
NEW.LIMIT_5_BEG ,
NEW.LIMIT_5_END ,
NEW.EXCHANGE_5 ,
NEW.LIMIT_6_BEG ,
NEW.LIMIT_6_END ,
NEW.EXCHANGE_6 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_LOSTFEE_A AFTER
INSERT
    ON
    CYC_LOSTFEE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_CYC_LOSTFEE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    K_LOST_CODE ,
    DESCRIPTION
    --,GOLD_MAJOR
    --,GOLD_SUB
    --,NORMAL_MAJOR
    --,NORMAL_SUB
    --,UD_MAJOR
    --,UD_SUB
,
    ONUS_BANK ,
    ONUS_AUTO_PAY ,
    OTHER_AUTO_PAY ,
    SALARY_ACCT ,
    CREDIT_ACCT ,
    CREDIT_LIMIT ,
    CREDIT_AMT ,
    BONUS_SEL ,
    BONUS ,
    LOST_LIMIT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ACCT_TYPE ,
NEW.LOST_CODE ,
NEW.DESCRIPTION
--,NEW.GOLD_MAJOR
--,NEW.GOLD_SUB
--,NEW.NORMAL_MAJOR
--,NEW.NORMAL_SUB
--,NEW.UD_MAJOR
--,NEW.UD_SUB
,
NEW.ONUS_BANK ,
NEW.ONUS_AUTO_PAY ,
NEW.OTHER_AUTO_PAY ,
NEW.SALARY_ACCT ,
NEW.CREDIT_ACCT ,
NEW.CREDIT_LIMIT ,
NEW.CREDIT_AMT ,
NEW.BONUS_SEL ,
NEW.BONUS ,
NEW.LOST_LIMIT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_LOSTFEE_D AFTER
DELETE
    ON
    CYC_LOSTFEE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CYC_LOSTFEE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    K_LOST_CODE ,
    DESCRIPTION
    --,GOLD_MAJOR
    --,GOLD_SUB
    --,NORMAL_MAJOR
    --,NORMAL_SUB
    --,UD_MAJOR
    --,UD_SUB
,
    ONUS_BANK ,
    ONUS_AUTO_PAY ,
    OTHER_AUTO_PAY ,
    SALARY_ACCT ,
    CREDIT_ACCT ,
    CREDIT_LIMIT ,
    CREDIT_AMT ,
    BONUS_SEL ,
    BONUS ,
    LOST_LIMIT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.ACCT_TYPE ,
OLD.LOST_CODE ,
OLD.DESCRIPTION
--,OLD.GOLD_MAJOR
--,OLD.GOLD_SUB
--,OLD.NORMAL_MAJOR
--,OLD.NORMAL_SUB
--,OLD.UD_MAJOR
--,OLD.UD_SUB
,
OLD.ONUS_BANK ,
OLD.ONUS_AUTO_PAY ,
OLD.OTHER_AUTO_PAY ,
OLD.SALARY_ACCT ,
OLD.CREDIT_ACCT ,
OLD.CREDIT_LIMIT ,
OLD.CREDIT_AMT ,
OLD.BONUS_SEL ,
OLD.BONUS ,
OLD.LOST_LIMIT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_CYC_LOSTFEE_U AFTER
UPDATE
    OF DESCRIPTION
    --,GOLD_MAJOR
    --,GOLD_SUB
    --,NORMAL_MAJOR
    --,NORMAL_SUB
    --,UD_MAJOR
    --,UD_SUB
,
    ONUS_BANK ,
    ONUS_AUTO_PAY ,
    OTHER_AUTO_PAY ,
    SALARY_ACCT ,
    CREDIT_ACCT ,
    CREDIT_LIMIT ,
    CREDIT_AMT ,
    BONUS_SEL ,
    BONUS ,
    LOST_LIMIT ON
    CYC_LOSTFEE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 SET
wk_audcode = 'U';--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_CYC_LOSTFEE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    K_LOST_CODE ,
    DESCRIPTION
    --,GOLD_MAJOR
    --,GOLD_SUB
    --,NORMAL_MAJOR
    --,NORMAL_SUB
    --,UD_MAJOR
    --,UD_SUB
,
    ONUS_BANK ,
    ONUS_AUTO_PAY ,
    OTHER_AUTO_PAY ,
    SALARY_ACCT ,
    CREDIT_ACCT ,
    CREDIT_LIMIT ,
    CREDIT_AMT ,
    BONUS_SEL ,
    BONUS ,
    LOST_LIMIT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ACCT_TYPE ,
NEW.LOST_CODE ,
NEW.DESCRIPTION
--,NEW.GOLD_MAJOR
--,NEW.GOLD_SUB
--,NEW.NORMAL_MAJOR
--,NEW.NORMAL_SUB
--,NEW.UD_MAJOR
--,NEW.UD_SUB
,
NEW.ONUS_BANK ,
NEW.ONUS_AUTO_PAY ,
NEW.OTHER_AUTO_PAY ,
NEW.SALARY_ACCT ,
NEW.CREDIT_ACCT ,
NEW.CREDIT_LIMIT ,
NEW.CREDIT_AMT ,
NEW.BONUS_SEL ,
NEW.BONUS ,
NEW.LOST_LIMIT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_ANUL_GP_A AFTER
INSERT
    ON
    MKT_ANUL_GP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_MKT_ANUL_GP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    GOLD_CARD ,
    NORMAL_CARD ,
    MAJOR_CARD ,
    ADDITION_CARD ,
    REWARD_YEAR ,
    DESCRIPTION ,
    REWARD_TYPE_1 ,
    PARTIAL_GM_1 ,
    PARTIAL_GS_1 ,
    PARTIAL_NM_1 ,
    PARTIAL_NS_1 ,
    FIX_AMT_GM_1 ,
    FIX_AMT_GS_1 ,
    FIX_AMT_NM_1 ,
    FIX_AMT_NS_1 ,
    RATE_GM_1 ,
    RATE_GS_1 ,
    RATE_NM_1 ,
    RATE_NS_1 ,
    REWARD_TYPE_2 ,
    PARTIAL_GM_2 ,
    PARTIAL_GS_2 ,
    PARTIAL_NM_2 ,
    PARTIAL_NS_2 ,
    FIX_AMT_GM_2 ,
    FIX_AMT_GS_2 ,
    FIX_AMT_NM_2 ,
    FIX_AMT_NS_2 ,
    RATE_GM_2 ,
    RATE_GS_2 ,
    RATE_NM_2 ,
    RATE_NS_2 ,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.GOLD_CARD ,
NEW.NORMAL_CARD ,
NEW.MAJOR_CARD ,
NEW.ADDITION_CARD ,
NEW.REWARD_YEAR ,
NEW.DESCRIPTION ,
NEW.REWARD_TYPE_1 ,
NEW.PARTIAL_GM_1 ,
NEW.PARTIAL_GS_1 ,
NEW.PARTIAL_NM_1 ,
NEW.PARTIAL_NS_1 ,
NEW.FIX_AMT_GM_1 ,
NEW.FIX_AMT_GS_1 ,
NEW.FIX_AMT_NM_1 ,
NEW.FIX_AMT_NS_1 ,
NEW.RATE_GM_1 ,
NEW.RATE_GS_1 ,
NEW.RATE_NM_1 ,
NEW.RATE_NS_1 ,
NEW.REWARD_TYPE_2 ,
NEW.PARTIAL_GM_2 ,
NEW.PARTIAL_GS_2 ,
NEW.PARTIAL_NM_2 ,
NEW.PARTIAL_NS_2 ,
NEW.FIX_AMT_GM_2 ,
NEW.FIX_AMT_GS_2 ,
NEW.FIX_AMT_NM_2 ,
NEW.FIX_AMT_NS_2 ,
NEW.RATE_GM_2 ,
NEW.RATE_GS_2 ,
NEW.RATE_NM_2 ,
NEW.RATE_NS_2 ,
NEW.CRT_USER ,
NEW.CRT_DATE ,
NEW.APR_FLAG ,
NEW.APR_USER ,
NEW.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_ANUL_GP_D AFTER
DELETE
    ON
    MKT_ANUL_GP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_MKT_ANUL_GP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    GOLD_CARD ,
    NORMAL_CARD ,
    MAJOR_CARD ,
    ADDITION_CARD ,
    REWARD_YEAR ,
    DESCRIPTION ,
    REWARD_TYPE_1 ,
    PARTIAL_GM_1 ,
    PARTIAL_GS_1 ,
    PARTIAL_NM_1 ,
    PARTIAL_NS_1 ,
    FIX_AMT_GM_1 ,
    FIX_AMT_GS_1 ,
    FIX_AMT_NM_1 ,
    FIX_AMT_NS_1 ,
    RATE_GM_1 ,
    RATE_GS_1 ,
    RATE_NM_1 ,
    RATE_NS_1 ,
    REWARD_TYPE_2 ,
    PARTIAL_GM_2 ,
    PARTIAL_GS_2 ,
    PARTIAL_NM_2 ,
    PARTIAL_NS_2 ,
    FIX_AMT_GM_2 ,
    FIX_AMT_GS_2 ,
    FIX_AMT_NM_2 ,
    FIX_AMT_NS_2 ,
    RATE_GM_2 ,
    RATE_GS_2 ,
    RATE_NM_2 ,
    RATE_NS_2 ,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.GROUP_CODE ,
OLD.GOLD_CARD ,
OLD.NORMAL_CARD ,
OLD.MAJOR_CARD ,
OLD.ADDITION_CARD ,
OLD.REWARD_YEAR ,
OLD.DESCRIPTION ,
OLD.REWARD_TYPE_1 ,
OLD.PARTIAL_GM_1 ,
OLD.PARTIAL_GS_1 ,
OLD.PARTIAL_NM_1 ,
OLD.PARTIAL_NS_1 ,
OLD.FIX_AMT_GM_1 ,
OLD.FIX_AMT_GS_1 ,
OLD.FIX_AMT_NM_1 ,
OLD.FIX_AMT_NS_1 ,
OLD.RATE_GM_1 ,
OLD.RATE_GS_1 ,
OLD.RATE_NM_1 ,
OLD.RATE_NS_1 ,
OLD.REWARD_TYPE_2 ,
OLD.PARTIAL_GM_2 ,
OLD.PARTIAL_GS_2 ,
OLD.PARTIAL_NM_2 ,
OLD.PARTIAL_NS_2 ,
OLD.FIX_AMT_GM_2 ,
OLD.FIX_AMT_GS_2 ,
OLD.FIX_AMT_NM_2 ,
OLD.FIX_AMT_NS_2 ,
OLD.RATE_GM_2 ,
OLD.RATE_GS_2 ,
OLD.RATE_NM_2 ,
OLD.RATE_NS_2 ,
OLD.CRT_USER ,
OLD.CRT_DATE ,
OLD.APR_FLAG ,
OLD.APR_USER ,
OLD.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_ANUL_GP_U AFTER
UPDATE
    OF GOLD_CARD ,
    NORMAL_CARD ,
    MAJOR_CARD ,
    ADDITION_CARD ,
    REWARD_YEAR ,
    DESCRIPTION ,
    REWARD_TYPE_1 ,
    PARTIAL_GM_1 ,
    PARTIAL_GS_1 ,
    PARTIAL_NM_1 ,
    PARTIAL_NS_1 ,
    FIX_AMT_GM_1 ,
    FIX_AMT_GS_1 ,
    FIX_AMT_NM_1 ,
    FIX_AMT_NS_1 ,
    RATE_GM_1 ,
    RATE_GS_1 ,
    RATE_NM_1 ,
    RATE_NS_1 ,
    REWARD_TYPE_2 ,
    PARTIAL_GM_2 ,
    PARTIAL_GS_2 ,
    PARTIAL_NM_2 ,
    PARTIAL_NS_2 ,
    FIX_AMT_GM_2 ,
    FIX_AMT_GS_2 ,
    FIX_AMT_NM_2 ,
    FIX_AMT_NS_2 ,
    RATE_GM_2 ,
    RATE_GS_2 ,
    RATE_NM_2 ,
    RATE_NS_2 ,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE ON
    MKT_ANUL_GP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_MKT_ANUL_GP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    GOLD_CARD ,
    NORMAL_CARD ,
    MAJOR_CARD ,
    ADDITION_CARD ,
    REWARD_YEAR ,
    DESCRIPTION ,
    REWARD_TYPE_1 ,
    PARTIAL_GM_1 ,
    PARTIAL_GS_1 ,
    PARTIAL_NM_1 ,
    PARTIAL_NS_1 ,
    FIX_AMT_GM_1 ,
    FIX_AMT_GS_1 ,
    FIX_AMT_NM_1 ,
    FIX_AMT_NS_1 ,
    RATE_GM_1 ,
    RATE_GS_1 ,
    RATE_NM_1 ,
    RATE_NS_1 ,
    REWARD_TYPE_2 ,
    PARTIAL_GM_2 ,
    PARTIAL_GS_2 ,
    PARTIAL_NM_2 ,
    PARTIAL_NS_2 ,
    FIX_AMT_GM_2 ,
    FIX_AMT_GS_2 ,
    FIX_AMT_NM_2 ,
    FIX_AMT_NS_2 ,
    RATE_GM_2 ,
    RATE_GS_2 ,
    RATE_NM_2 ,
    RATE_NS_2 ,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.GOLD_CARD ,
NEW.NORMAL_CARD ,
NEW.MAJOR_CARD ,
NEW.ADDITION_CARD ,
NEW.REWARD_YEAR ,
NEW.DESCRIPTION ,
NEW.REWARD_TYPE_1 ,
NEW.PARTIAL_GM_1 ,
NEW.PARTIAL_GS_1 ,
NEW.PARTIAL_NM_1 ,
NEW.PARTIAL_NS_1 ,
NEW.FIX_AMT_GM_1 ,
NEW.FIX_AMT_GS_1 ,
NEW.FIX_AMT_NM_1 ,
NEW.FIX_AMT_NS_1 ,
NEW.RATE_GM_1 ,
NEW.RATE_GS_1 ,
NEW.RATE_NM_1 ,
NEW.RATE_NS_1 ,
NEW.REWARD_TYPE_2 ,
NEW.PARTIAL_GM_2 ,
NEW.PARTIAL_GS_2 ,
NEW.PARTIAL_NM_2 ,
NEW.PARTIAL_NS_2 ,
NEW.FIX_AMT_GM_2 ,
NEW.FIX_AMT_GS_2 ,
NEW.FIX_AMT_NM_2 ,
NEW.FIX_AMT_NS_2 ,
NEW.RATE_GM_2 ,
NEW.RATE_GS_2 ,
NEW.RATE_NM_2 ,
NEW.RATE_NS_2 ,
NEW.CRT_USER ,
NEW.CRT_DATE ,
NEW.APR_FLAG ,
NEW.APR_USER ,
NEW.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_MKT_BONUS_DTL
AFTER DELETE OR INSERT OR UPDATE 
ON "MKT_BONUS_DTL"
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW MODE DB2SQL
BEGIN
 DECLARE wk_audcode      VARCHAR(1);--
 DECLARE wk_user         VARCHAR(10);--
 DECLARE wk_pgm          VARCHAR(20);--
 DECLARE wk_ws           VARCHAR(20);--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

  IF INSERTING THEN
       set wk_audcode = 'A';--
  ELSEIF UPDATING THEN
       set wk_audcode = 'U';--
  ELSE
       set wk_audcode   = 'D';--
  END IF;--
  if INSERTING or UPDATING then
    INSERT INTO LOG_MKT_BONUS_DTL(
                MOD_TIME
                ,MOD_PGM
                ,MOD_USER
                ,MOD_AUDCODE
                ,TRAN_DATE     
                ,TRAN_TIME     
                ,ACTIVE_CODE   
                ,ACTIVE_NAME   
                ,BONUS_TYPE    
                ,P_SEQNO       
                ,ID_P_SEQNO    
                ,ACCT_TYPE     
                ,TRAN_CODE     
                ,TRAN_PGM      
                ,BEG_TRAN_BP   
                ,END_TRAN_BP   
                ,RES_E_DATE    
                ,RES_TRAN_BP   
                ,RES_UPD_DATE  
                ,TAX_TRAN_BP   
                ,TAX_FLAG      
                ,MOD_DESC      
                ,MOD_MEMO      
                ,MOD_REASON    
                ,EFFECT_E_DATE 
                ,EFFECT_FLAG   
                ,MOVE_CNT      
                ,MOVE_BP       
                ,REMOVE_DATE   
                ,TRAN_SEQNO    
                ,PROC_MONTH    
                ,ACCT_DATE  
           )
    VALUES (
                to_char(sysdate,'yyyymmddhh24misssss')
                ,NEW.MOD_PGM
                ,NEW.MOD_USER
                ,wk_audcode
                ,NEW.TRAN_DATE     
                ,NEW.TRAN_TIME     
                ,NEW.ACTIVE_CODE   
                ,NEW.ACTIVE_NAME   
                ,NEW.BONUS_TYPE    
                ,NEW.P_SEQNO       
                ,NEW.ID_P_SEQNO    
                ,NEW.ACCT_TYPE     
                ,NEW.TRAN_CODE     
                ,NEW.TRAN_PGM      
                ,NEW.BEG_TRAN_BP   
                ,NEW.END_TRAN_BP   
                ,NEW.RES_E_DATE    
                ,NEW.RES_TRAN_BP   
                ,NEW.RES_UPD_DATE  
                ,NEW.TAX_TRAN_BP   
                ,NEW.TAX_FLAG      
                ,NEW.MOD_DESC      
                ,NEW.MOD_MEMO      
                ,NEW.MOD_REASON    
                ,NEW.EFFECT_E_DATE 
                ,NEW.EFFECT_FLAG   
                ,NEW.MOVE_CNT      
                ,NEW.MOVE_BP       
                ,NEW.REMOVE_DATE   
                ,NEW.TRAN_SEQNO    
                ,NEW.PROC_MONTH    
                ,NEW.ACCT_DATE    
           );--
  else
    INSERT INTO LOG_MKT_BONUS_DTL(
                MOD_TIME
                ,MOD_PGM
                ,MOD_USER
                ,MOD_AUDCODE
                ,TRAN_DATE     
                ,TRAN_TIME     
                ,ACTIVE_CODE   
                ,ACTIVE_NAME   
                ,BONUS_TYPE    
                ,P_SEQNO       
                ,ID_P_SEQNO    
                ,ACCT_TYPE     
                ,TRAN_CODE     
                ,TRAN_PGM      
                ,BEG_TRAN_BP   
                ,END_TRAN_BP   
                ,RES_E_DATE    
                ,RES_TRAN_BP   
                ,RES_UPD_DATE  
                ,TAX_TRAN_BP   
                ,TAX_FLAG      
                ,MOD_DESC      
                ,MOD_MEMO      
                ,MOD_REASON    
                ,EFFECT_E_DATE 
                ,EFFECT_FLAG   
                ,MOVE_CNT      
                ,MOVE_BP       
                ,REMOVE_DATE   
                ,TRAN_SEQNO    
                ,PROC_MONTH    
                ,ACCT_DATE   
           )
    VALUES (
                to_char(sysdate,'yyyymmddhh24misssss')
                ,wk_pgm
                ,wk_user
                ,wk_audcode
                ,OLD.TRAN_DATE     
                ,OLD.TRAN_TIME     
                ,OLD.ACTIVE_CODE   
                ,OLD.ACTIVE_NAME   
                ,OLD.BONUS_TYPE    
                ,OLD.P_SEQNO       
                ,OLD.ID_P_SEQNO    
                ,OLD.ACCT_TYPE     
                ,OLD.TRAN_CODE     
                ,OLD.TRAN_PGM      
                ,OLD.BEG_TRAN_BP   
                ,OLD.END_TRAN_BP   
                ,OLD.RES_E_DATE    
                ,OLD.RES_TRAN_BP   
                ,OLD.RES_UPD_DATE  
                ,OLD.TAX_TRAN_BP   
                ,OLD.TAX_FLAG      
                ,OLD.MOD_DESC      
                ,OLD.MOD_MEMO      
                ,OLD.MOD_REASON    
                ,OLD.EFFECT_E_DATE 
                ,OLD.EFFECT_FLAG   
                ,OLD.MOVE_CNT      
                ,OLD.MOVE_BP       
                ,OLD.REMOVE_DATE   
                ,OLD.TRAN_SEQNO    
                ,OLD.PROC_MONTH    
                ,OLD.ACCT_DATE 
           );--
  end if;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_GIFT_A AFTER
INSERT
    ON
    MKT_GIFT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_MKT_GIFT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GIFT_NO ,
    GIFT_NAME ,
    GIFT_TYPE ,
    CASH_VALUE ,
    SUPPLY_COUNT ,
    USE_COUNT ,
    NET_COUNT ,
    VENDOR_NO ,
    NAME_LIST ,
    TOTAL_LIST ,
    BONUS_TYPE ,
    FUND_CODE ,
    DISABLE_FLAG ,
    EXCHG_TYPE ,
    GIFT_TYPENO ,
    UP_FLAG ,
    WEB_SUMCNT
    --,NOR_TTL_PT_S
    --,GOLD_TTL_PT_S
    --,NOR_PAR_PT_S
    --,NOR_CASH_S
    --,GOLD_PAR_PT_S
    --,GOLD_CASH_S
    --,GROUP_CODE_1
    --,NOR_TTL_PT_1
    --,GOLD_TTL_PT_1
    --,NOR_PAR_PT_1
    --,NOR_CASH_1
    --,GOLD_PAR_PT_1
    --,GOLD_CASH_1
    --,GROUP_CODE_2
    --,NOR_TTL_PT_2
    --,GOLD_TTL_PT_2
    --,NOR_PAR_PT_2
    --,NOR_CASH_2
    --,GOLD_PAR_PT_2
    --,GOLD_CASH_2
    --,PRINT_LIST
    --,GROUP_CODE_3
    --,NOR_TTL_PT_3
    --,GOLD_TTL_PT_3
    --,NOR_PAR_PT_3
    --,NOR_CASH_3
    --,GOLD_PAR_PT_3
    --,GOLD_CASH_3
    --,GROUP_CODE_4
    --,NOR_TTL_PT_4
    --,GOLD_TTL_PT_4
    --,NOR_PAR_PT_4
    --,NOR_CASH_4
    --,GOLD_PAR_PT_4
    --,GOLD_CASH_4
    --,GROUP_CODE_5
    --,NOR_TTL_PT_5
    --,GOLD_TTL_PT_5
    --,NOR_PAR_PT_5
    --,NOR_CASH_5
    --,GOLD_PAR_PT_5
    --,GOLD_CASH_5
,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GIFT_NO ,
NEW.GIFT_NAME ,
NEW.GIFT_TYPE ,
NEW.CASH_VALUE ,
NEW.SUPPLY_COUNT ,
NEW.USE_COUNT ,
NEW.NET_COUNT ,
NEW.VENDOR_NO ,
NEW.NAME_LIST ,
NEW.TOTAL_LIST ,
NEW.BONUS_TYPE ,
NEW.FUND_CODE ,
NEW.DISABLE_FLAG ,
NEW.EXCHG_TYPE ,
NEW.GIFT_TYPENO ,
NEW.UP_FLAG ,
NEW.WEB_SUMCNT
--,NEW.NOR_TTL_PT_S
--,NEW.GOLD_TTL_PT_S
--,NEW.NOR_PAR_PT_S
--,NEW.NOR_CASH_S
--,NEW.GOLD_PAR_PT_S
--,NEW.GOLD_CASH_S
--,NEW.GROUP_CODE_1
--,NEW.NOR_TTL_PT_1
--,NEW.GOLD_TTL_PT_1
--,NEW.NOR_PAR_PT_1
--,NEW.NOR_CASH_1
--,NEW.GOLD_PAR_PT_1
--,NEW.GOLD_CASH_1
--,NEW.GROUP_CODE_2
--,NEW.NOR_TTL_PT_2
--,NEW.GOLD_TTL_PT_2
--,NEW.NOR_PAR_PT_2
--,NEW.NOR_CASH_2
--,NEW.GOLD_PAR_PT_2
--,NEW.GOLD_CASH_2
--,NEW.PRINT_LIST
--,NEW.GROUP_CODE_3
--,NEW.NOR_TTL_PT_3
--,NEW.GOLD_TTL_PT_3
--,NEW.NOR_PAR_PT_3
--,NEW.NOR_CASH_3
--,NEW.GOLD_PAR_PT_3
--,NEW.GOLD_CASH_3
--,NEW.GROUP_CODE_4
--,NEW.NOR_TTL_PT_4
--,NEW.GOLD_TTL_PT_4
--,NEW.NOR_PAR_PT_4
--,NEW.NOR_CASH_4
--,NEW.GOLD_PAR_PT_4
--,NEW.GOLD_CASH_4
--,NEW.GROUP_CODE_5
--,NEW.NOR_TTL_PT_5
--,NEW.GOLD_TTL_PT_5
--,NEW.NOR_PAR_PT_5
--,NEW.NOR_CASH_5
--,NEW.GOLD_PAR_PT_5
--,NEW.GOLD_CASH_5
,
NEW.CRT_USER ,
NEW.CRT_DATE ,
NEW.APR_FLAG ,
NEW.APR_USER ,
NEW.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_GIFT_D AFTER
DELETE
    ON
    MKT_GIFT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_MKT_GIFT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GIFT_NO ,
    GIFT_NAME ,
    GIFT_TYPE ,
    CASH_VALUE ,
    SUPPLY_COUNT ,
    USE_COUNT ,
    NET_COUNT ,
    VENDOR_NO ,
    NAME_LIST ,
    TOTAL_LIST ,
    BONUS_TYPE ,
    FUND_CODE ,
    DISABLE_FLAG ,
    EXCHG_TYPE ,
    GIFT_TYPENO ,
    UP_FLAG ,
    WEB_SUMCNT
    --,NOR_TTL_PT_S
    --,GOLD_TTL_PT_S
    --,NOR_PAR_PT_S
    --,NOR_CASH_S
    --,GOLD_PAR_PT_S
    --,GOLD_CASH_S
    --,GROUP_CODE_1
    --,NOR_TTL_PT_1
    --,GOLD_TTL_PT_1
    --,NOR_PAR_PT_1
    --,NOR_CASH_1
    --,GOLD_PAR_PT_1
    --,GOLD_CASH_1
    --,GROUP_CODE_2
    --,NOR_TTL_PT_2
    --,GOLD_TTL_PT_2
    --,NOR_PAR_PT_2
    --,NOR_CASH_2
    --,GOLD_PAR_PT_2
    --,GOLD_CASH_2
    --,PRINT_LIST
    --,GROUP_CODE_3
    --,NOR_TTL_PT_3
    --,GOLD_TTL_PT_3
    --,NOR_PAR_PT_3
    --,NOR_CASH_3
    --,GOLD_PAR_PT_3
    --,GOLD_CASH_3
    --,GROUP_CODE_4
    --,NOR_TTL_PT_4
    --,GOLD_TTL_PT_4
    --,NOR_PAR_PT_4
    --,NOR_CASH_4
    --,GOLD_PAR_PT_4
    --,GOLD_CASH_4
    --,GROUP_CODE_5
    --,NOR_TTL_PT_5
    --,GOLD_TTL_PT_5
    --,NOR_PAR_PT_5
    --,NOR_CASH_5
    --,GOLD_PAR_PT_5
    --,GOLD_CASH_5
,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.GIFT_NO ,
OLD.GIFT_NAME ,
OLD.GIFT_TYPE ,
OLD.CASH_VALUE ,
OLD.SUPPLY_COUNT ,
OLD.USE_COUNT ,
OLD.NET_COUNT ,
OLD.VENDOR_NO ,
OLD.NAME_LIST ,
OLD.TOTAL_LIST ,
OLD.BONUS_TYPE ,
OLD.FUND_CODE ,
OLD.DISABLE_FLAG ,
OLD.EXCHG_TYPE ,
OLD.GIFT_TYPENO ,
OLD.UP_FLAG ,
OLD.WEB_SUMCNT
--,OLD.NOR_TTL_PT_S
--,OLD.GOLD_TTL_PT_S
--,OLD.NOR_PAR_PT_S
--,OLD.NOR_CASH_S
--,OLD.GOLD_PAR_PT_S
--,OLD.GOLD_CASH_S
--,OLD.GROUP_CODE_1
--,OLD.NOR_TTL_PT_1
--,OLD.GOLD_TTL_PT_1
--,OLD.NOR_PAR_PT_1
--,OLD.NOR_CASH_1
--,OLD.GOLD_PAR_PT_1
--,OLD.GOLD_CASH_1
--,OLD.GROUP_CODE_2
--,OLD.NOR_TTL_PT_2
--,OLD.GOLD_TTL_PT_2
--,OLD.NOR_PAR_PT_2
--,OLD.NOR_CASH_2
--,OLD.GOLD_PAR_PT_2
--,OLD.GOLD_CASH_2
--,OLD.PRINT_LIST
--,OLD.GROUP_CODE_3
--,OLD.NOR_TTL_PT_3
--,OLD.GOLD_TTL_PT_3
--,OLD.NOR_PAR_PT_3
--,OLD.NOR_CASH_3
--,OLD.GOLD_PAR_PT_3
--,OLD.GOLD_CASH_3
--,OLD.GROUP_CODE_4
--,OLD.NOR_TTL_PT_4
--,OLD.GOLD_TTL_PT_4
--,OLD.NOR_PAR_PT_4
--,OLD.NOR_CASH_4
--,OLD.GOLD_PAR_PT_4
--,OLD.GOLD_CASH_4
--,OLD.GROUP_CODE_5
--,OLD.NOR_TTL_PT_5
--,OLD.GOLD_TTL_PT_5
--,OLD.NOR_PAR_PT_5
--,OLD.NOR_CASH_5
--,OLD.GOLD_PAR_PT_5
--,OLD.GOLD_CASH_5
,
OLD.CRT_USER ,
OLD.CRT_DATE ,
OLD.APR_FLAG ,
OLD.APR_USER ,
OLD.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_GIFT_U AFTER
UPDATE
    OF GIFT_NAME ,
    GIFT_TYPE ,
    CASH_VALUE ,
    SUPPLY_COUNT ,
    USE_COUNT ,
    NET_COUNT ,
    VENDOR_NO ,
    NAME_LIST ,
    TOTAL_LIST ,
    BONUS_TYPE ,
    FUND_CODE ,
    DISABLE_FLAG ,
    EXCHG_TYPE ,
    GIFT_TYPENO ,
    UP_FLAG ,
    WEB_SUMCNT
    --,NOR_TTL_PT_S
    --,GOLD_TTL_PT_S
    --,NOR_PAR_PT_S
    --,NOR_CASH_S
    --,GOLD_PAR_PT_S
    --,GOLD_CASH_S
    --,GROUP_CODE_1
    --,NOR_TTL_PT_1
    --,GOLD_TTL_PT_1
    --,NOR_PAR_PT_1
    --,NOR_CASH_1
    --,GOLD_PAR_PT_1
    --,GOLD_CASH_1
    --,GROUP_CODE_2
    --,NOR_TTL_PT_2
    --,GOLD_TTL_PT_2
    --,NOR_PAR_PT_2
    --,NOR_CASH_2
    --,GOLD_PAR_PT_2
    --,GOLD_CASH_2
    --,PRINT_LIST
    --,GROUP_CODE_3
    --,NOR_TTL_PT_3
    --,GOLD_TTL_PT_3
    --,NOR_PAR_PT_3
    --,NOR_CASH_3
    --,GOLD_PAR_PT_3
    --,GOLD_CASH_3
    --,GROUP_CODE_4
    --,NOR_TTL_PT_4
    --,GOLD_TTL_PT_4
    --,NOR_PAR_PT_4
    --,NOR_CASH_4
    --,GOLD_PAR_PT_4
    --,GOLD_CASH_4
    --,GROUP_CODE_5
    --,NOR_TTL_PT_5
    --,GOLD_TTL_PT_5
    --,NOR_PAR_PT_5
    --,NOR_CASH_5
    --,GOLD_PAR_PT_5
    --,GOLD_CASH_5
,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE ON
    MKT_GIFT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_MKT_GIFT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GIFT_NO ,
    GIFT_NAME ,
    GIFT_TYPE ,
    CASH_VALUE ,
    SUPPLY_COUNT ,
    USE_COUNT ,
    NET_COUNT ,
    VENDOR_NO ,
    NAME_LIST ,
    TOTAL_LIST ,
    BONUS_TYPE ,
    FUND_CODE ,
    DISABLE_FLAG ,
    EXCHG_TYPE ,
    GIFT_TYPENO ,
    UP_FLAG ,
    WEB_SUMCNT
    --,NOR_TTL_PT_S
    --,GOLD_TTL_PT_S
    --,NOR_PAR_PT_S
    --,NOR_CASH_S
    --,GOLD_PAR_PT_S
    --,GOLD_CASH_S
    --,GROUP_CODE_1
    --,NOR_TTL_PT_1
    --,GOLD_TTL_PT_1
    --,NOR_PAR_PT_1
    --,NOR_CASH_1
    --,GOLD_PAR_PT_1
    --,GOLD_CASH_1
    --,GROUP_CODE_2
    --,NOR_TTL_PT_2
    --,GOLD_TTL_PT_2
    --,NOR_PAR_PT_2
    --,NOR_CASH_2
    --,GOLD_PAR_PT_2
    --,GOLD_CASH_2
    --,PRINT_LIST
    --,GROUP_CODE_3
    --,NOR_TTL_PT_3
    --,GOLD_TTL_PT_3
    --,NOR_PAR_PT_3
    --,NOR_CASH_3
    --,GOLD_PAR_PT_3
    --,GOLD_CASH_3
    --,GROUP_CODE_4
    --,NOR_TTL_PT_4
    --,GOLD_TTL_PT_4
    --,NOR_PAR_PT_4
    --,NOR_CASH_4
    --,GOLD_PAR_PT_4
    --,GOLD_CASH_4
    --,GROUP_CODE_5
    --,NOR_TTL_PT_5
    --,GOLD_TTL_PT_5
    --,NOR_PAR_PT_5
    --,NOR_CASH_5
    --,GOLD_PAR_PT_5
    --,GOLD_CASH_5
,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GIFT_NO ,
NEW.GIFT_NAME ,
NEW.GIFT_TYPE ,
NEW.CASH_VALUE ,
NEW.SUPPLY_COUNT ,
NEW.USE_COUNT ,
NEW.NET_COUNT ,
NEW.VENDOR_NO ,
NEW.NAME_LIST ,
NEW.TOTAL_LIST ,
NEW.BONUS_TYPE ,
NEW.FUND_CODE ,
NEW.DISABLE_FLAG ,
NEW.EXCHG_TYPE ,
NEW.GIFT_TYPENO ,
NEW.UP_FLAG ,
NEW.WEB_SUMCNT
--,NEW.NOR_TTL_PT_S
--,NEW.GOLD_TTL_PT_S
--,NEW.NOR_PAR_PT_S
--,NEW.NOR_CASH_S
--,NEW.GOLD_PAR_PT_S
--,NEW.GOLD_CASH_S
--,NEW.GROUP_CODE_1
--,NEW.NOR_TTL_PT_1
--,NEW.GOLD_TTL_PT_1
--,NEW.NOR_PAR_PT_1
--,NEW.NOR_CASH_1
--,NEW.GOLD_PAR_PT_1
--,NEW.GOLD_CASH_1
--,NEW.GROUP_CODE_2
--,NEW.NOR_TTL_PT_2
--,NEW.GOLD_TTL_PT_2
--,NEW.NOR_PAR_PT_2
--,NEW.NOR_CASH_2
--,NEW.GOLD_PAR_PT_2
--,NEW.GOLD_CASH_2
--,NEW.PRINT_LIST
--,NEW.GROUP_CODE_3
--,NEW.NOR_TTL_PT_3
--,NEW.GOLD_TTL_PT_3
--,NEW.NOR_PAR_PT_3
--,NEW.NOR_CASH_3
--,NEW.GOLD_PAR_PT_3
--,NEW.GOLD_CASH_3
--,NEW.GROUP_CODE_4
--,NEW.NOR_TTL_PT_4
--,NEW.GOLD_TTL_PT_4
--,NEW.NOR_PAR_PT_4
--,NEW.NOR_CASH_4
--,NEW.GOLD_PAR_PT_4
--,NEW.GOLD_CASH_4
--,NEW.GROUP_CODE_5
--,NEW.NOR_TTL_PT_5
--,NEW.GOLD_TTL_PT_5
--,NEW.NOR_PAR_PT_5
--,NEW.NOR_CASH_5
--,NEW.GOLD_PAR_PT_5
--,NEW.GOLD_CASH_5
,
NEW.CRT_USER ,
NEW.CRT_DATE ,
NEW.APR_FLAG ,
NEW.APR_USER ,
NEW.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_MONTH_PAR_A AFTER
INSERT
    ON
    MKT_MONTH_PAR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_MKT_MONTH_PAR( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BATCH_NO ,
    DESCRIPTION ,
    CONFIRM_PARM ,
    CONFIRM_DATE ,
    ACCT_TYPE_1 ,
    ACCT_TYPE_2 ,
    ACCT_TYPE_3 ,
    ACCT_TYPE_4 ,
    ACCT_TYPE_5 ,
    ALL_CARD ,
    PURCH_MONTH ,
    PURCH_AMT ,
    GROUP_CODE_1 ,
    GROUP_CODE_2 ,
    GROUP_CODE_3 ,
    GROUP_CODE_4 ,
    GROUP_CODE_5 ,
    GROUP_CHIN_1 ,
    GROUP_CHIN_2 ,
    GROUP_CHIN_3 ,
    GROUP_CHIN_4 ,
    GROUP_CHIN_5 ,
    CONDITION ,
    NEW_NOT_PURCHASE_MONTH ,
    NEW_NOT_PURCHASE_YM_CT_FLAG ,
    NEW_NOT_PURCHASE_YM_CT ,
    NEW_NOT_PURCHASE_YM_GC_FLAG ,
    NEW_NOT_PURCHASE_YM_GC ,
    OLD_NOT_BILL_YM_S ,
    OLD_NOT_BILL_YM_E ,
    OLD_NOT_BILL_YM_CT_FLAG ,
    OLD_NOT_BILL_YM_CT ,
    OLD_NOT_BILL_YM_GC_FLAG ,
    OLD_NOT_BILL_YM_GC ,
    NOT_BILL_YM_S ,
    NOT_BILL_YM_E ,
    NOT_BILL_YM_CT_FLAG ,
    NOT_BILL_YM_CT ,
    NOT_BILL_YM_GC_FLAG ,
    NOT_BILL_YM_GC ,
    EXCLUDE_FOREIGNER_FLAG ,
    EXCLUDE_STAFF_FLAG ,
    EXCLUDE_MBULLET_FLAG ,
    EXCLUDE_CALL_SELL_FLAG ,
    EXCLUDE_SMS_FLAG ,
    EXCLUDE_DM_FLAG ,
    EXCLUDE_E_NEWS_FLAG ,
    EXCLUDE_LIST_FLAG ,
    EXCLUDE_LIST ,
    ACCT_TYPE_CNT1 ,
    ACCT_TYPE_CNT2 ,
    ACCT_TYPE_CNT3 ,
    ACCT_TYPE_CNT4 ,
    ACCT_TYPE_CNT5 ,
    TOTAL_CNT
    --,GROUP_1_CNT
    --,GROUP_2_CNT
    --,GROUP_3_CNT
    --,GROUP_4_CNT
    --,GROUP_5_CNT
    --,GROUP_DUP_CNT
,
    NORMAL_CARD_CNT ,
    NO_PURCH_CNT ,
    CONFIRM_PRINT ,
    FILE_DATE ,
    EMPLOYEE_NO ,
    COMMIT_CODE ,
    PROCESS_CODE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BATCH_NO ,
NEW.DESCRIPTION ,
NEW.CONFIRM_PARM ,
NEW.CONFIRM_DATE ,
NEW.ACCT_TYPE_1 ,
NEW.ACCT_TYPE_2 ,
NEW.ACCT_TYPE_3 ,
NEW.ACCT_TYPE_4 ,
NEW.ACCT_TYPE_5 ,
NEW.ALL_CARD ,
NEW.PURCH_MONTH ,
NEW.PURCH_AMT ,
NEW.GROUP_CODE_1 ,
NEW.GROUP_CODE_2 ,
NEW.GROUP_CODE_3 ,
NEW.GROUP_CODE_4 ,
NEW.GROUP_CODE_5 ,
NEW.GROUP_CHIN_1 ,
NEW.GROUP_CHIN_2 ,
NEW.GROUP_CHIN_3 ,
NEW.GROUP_CHIN_4 ,
NEW.GROUP_CHIN_5 ,
NEW.CONDITION ,
NEW.NEW_NOT_PURCHASE_MONTH ,
NEW.NEW_NOT_PURCHASE_YM_CT_FLAG ,
NEW.NEW_NOT_PURCHASE_YM_CT ,
NEW.NEW_NOT_PURCHASE_YM_GC_FLAG ,
NEW.NEW_NOT_PURCHASE_YM_GC ,
NEW.OLD_NOT_BILL_YM_S ,
NEW.OLD_NOT_BILL_YM_E ,
NEW.OLD_NOT_BILL_YM_CT_FLAG ,
NEW.OLD_NOT_BILL_YM_CT ,
NEW.OLD_NOT_BILL_YM_GC_FLAG ,
NEW.OLD_NOT_BILL_YM_GC ,
NEW.NOT_BILL_YM_S ,
NEW.NOT_BILL_YM_E ,
NEW.NOT_BILL_YM_CT_FLAG ,
NEW.NOT_BILL_YM_CT ,
NEW.NOT_BILL_YM_GC_FLAG ,
NEW.NOT_BILL_YM_GC ,
NEW.EXCLUDE_FOREIGNER_FLAG ,
NEW.EXCLUDE_STAFF_FLAG ,
NEW.EXCLUDE_MBULLET_FLAG ,
NEW.EXCLUDE_CALL_SELL_FLAG ,
NEW.EXCLUDE_SMS_FLAG ,
NEW.EXCLUDE_DM_FLAG ,
NEW.EXCLUDE_E_NEWS_FLAG ,
NEW.EXCLUDE_LIST_FLAG ,
NEW.EXCLUDE_LIST ,
NEW.ACCT_TYPE_CNT1 ,
NEW.ACCT_TYPE_CNT2 ,
NEW.ACCT_TYPE_CNT3 ,
NEW.ACCT_TYPE_CNT4 ,
NEW.ACCT_TYPE_CNT5 ,
NEW.TOTAL_CNT
--,NEW.GROUP_1_CNT
--,NEW.GROUP_2_CNT
--,NEW.GROUP_3_CNT
--,NEW.GROUP_4_CNT
--,NEW.GROUP_5_CNT
--,NEW.GROUP_DUP_CNT
,
NEW.NORMAL_CARD_CNT ,
NEW.NO_PURCH_CNT ,
NEW.CONFIRM_PRINT ,
NEW.FILE_DATE ,
NEW.EMPLOYEE_NO ,
NEW.COMMIT_CODE ,
NEW.PROCESS_CODE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_MONTH_PAR_D AFTER
DELETE
    ON
    MKT_MONTH_PAR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_MKT_MONTH_PAR( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BATCH_NO ,
    DESCRIPTION ,
    CONFIRM_PARM ,
    CONFIRM_DATE ,
    ACCT_TYPE_1 ,
    ACCT_TYPE_2 ,
    ACCT_TYPE_3 ,
    ACCT_TYPE_4 ,
    ACCT_TYPE_5 ,
    ALL_CARD ,
    PURCH_MONTH ,
    PURCH_AMT ,
    GROUP_CODE_1 ,
    GROUP_CODE_2 ,
    GROUP_CODE_3 ,
    GROUP_CODE_4 ,
    GROUP_CODE_5 ,
    GROUP_CHIN_1 ,
    GROUP_CHIN_2 ,
    GROUP_CHIN_3 ,
    GROUP_CHIN_4 ,
    GROUP_CHIN_5 ,
    CONDITION ,
    NEW_NOT_PURCHASE_MONTH ,
    NEW_NOT_PURCHASE_YM_CT_FLAG ,
    NEW_NOT_PURCHASE_YM_CT ,
    NEW_NOT_PURCHASE_YM_GC_FLAG ,
    NEW_NOT_PURCHASE_YM_GC ,
    OLD_NOT_BILL_YM_S ,
    OLD_NOT_BILL_YM_E ,
    OLD_NOT_BILL_YM_CT_FLAG ,
    OLD_NOT_BILL_YM_CT ,
    OLD_NOT_BILL_YM_GC_FLAG ,
    OLD_NOT_BILL_YM_GC ,
    NOT_BILL_YM_S ,
    NOT_BILL_YM_E ,
    NOT_BILL_YM_CT_FLAG ,
    NOT_BILL_YM_CT ,
    NOT_BILL_YM_GC_FLAG ,
    NOT_BILL_YM_GC ,
    EXCLUDE_FOREIGNER_FLAG ,
    EXCLUDE_STAFF_FLAG ,
    EXCLUDE_MBULLET_FLAG ,
    EXCLUDE_CALL_SELL_FLAG ,
    EXCLUDE_SMS_FLAG ,
    EXCLUDE_DM_FLAG ,
    EXCLUDE_E_NEWS_FLAG ,
    EXCLUDE_LIST_FLAG ,
    EXCLUDE_LIST ,
    ACCT_TYPE_CNT1 ,
    ACCT_TYPE_CNT2 ,
    ACCT_TYPE_CNT3 ,
    ACCT_TYPE_CNT4 ,
    ACCT_TYPE_CNT5 ,
    TOTAL_CNT
    --,GROUP_1_CNT
    --,GROUP_2_CNT
    --,GROUP_3_CNT
    --,GROUP_4_CNT
    --,GROUP_5_CNT
    --,GROUP_DUP_CNT
,
    NORMAL_CARD_CNT ,
    NO_PURCH_CNT ,
    CONFIRM_PRINT ,
    FILE_DATE ,
    EMPLOYEE_NO ,
    COMMIT_CODE ,
    PROCESS_CODE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.BATCH_NO ,
OLD.DESCRIPTION ,
OLD.CONFIRM_PARM ,
OLD.CONFIRM_DATE ,
OLD.ACCT_TYPE_1 ,
OLD.ACCT_TYPE_2 ,
OLD.ACCT_TYPE_3 ,
OLD.ACCT_TYPE_4 ,
OLD.ACCT_TYPE_5 ,
OLD.ALL_CARD ,
OLD.PURCH_MONTH ,
OLD.PURCH_AMT ,
OLD.GROUP_CODE_1 ,
OLD.GROUP_CODE_2 ,
OLD.GROUP_CODE_3 ,
OLD.GROUP_CODE_4 ,
OLD.GROUP_CODE_5 ,
OLD.GROUP_CHIN_1 ,
OLD.GROUP_CHIN_2 ,
OLD.GROUP_CHIN_3 ,
OLD.GROUP_CHIN_4 ,
OLD.GROUP_CHIN_5 ,
OLD.CONDITION ,
OLD.NEW_NOT_PURCHASE_MONTH ,
OLD.NEW_NOT_PURCHASE_YM_CT_FLAG ,
OLD.NEW_NOT_PURCHASE_YM_CT ,
OLD.NEW_NOT_PURCHASE_YM_GC_FLAG ,
OLD.NEW_NOT_PURCHASE_YM_GC ,
OLD.OLD_NOT_BILL_YM_S ,
OLD.OLD_NOT_BILL_YM_E ,
OLD.OLD_NOT_BILL_YM_CT_FLAG ,
OLD.OLD_NOT_BILL_YM_CT ,
OLD.OLD_NOT_BILL_YM_GC_FLAG ,
OLD.OLD_NOT_BILL_YM_GC ,
OLD.NOT_BILL_YM_S ,
OLD.NOT_BILL_YM_E ,
OLD.NOT_BILL_YM_CT_FLAG ,
OLD.NOT_BILL_YM_CT ,
OLD.NOT_BILL_YM_GC_FLAG ,
OLD.NOT_BILL_YM_GC ,
OLD.EXCLUDE_FOREIGNER_FLAG ,
OLD.EXCLUDE_STAFF_FLAG ,
OLD.EXCLUDE_MBULLET_FLAG ,
OLD.EXCLUDE_CALL_SELL_FLAG ,
OLD.EXCLUDE_SMS_FLAG ,
OLD.EXCLUDE_DM_FLAG ,
OLD.EXCLUDE_E_NEWS_FLAG ,
OLD.EXCLUDE_LIST_FLAG ,
OLD.EXCLUDE_LIST ,
OLD.ACCT_TYPE_CNT1 ,
OLD.ACCT_TYPE_CNT2 ,
OLD.ACCT_TYPE_CNT3 ,
OLD.ACCT_TYPE_CNT4 ,
OLD.ACCT_TYPE_CNT5 ,
OLD.TOTAL_CNT
--,OLD.GROUP_1_CNT
--,OLD.GROUP_2_CNT
--,OLD.GROUP_3_CNT
--,OLD.GROUP_4_CNT
--,OLD.GROUP_5_CNT
--,OLD.GROUP_DUP_CNT
,
OLD.NORMAL_CARD_CNT ,
OLD.NO_PURCH_CNT ,
OLD.CONFIRM_PRINT ,
OLD.FILE_DATE ,
OLD.EMPLOYEE_NO ,
OLD.COMMIT_CODE ,
OLD.PROCESS_CODE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_MONTH_PAR_U AFTER
UPDATE
    OF DESCRIPTION ,
    CONFIRM_PARM ,
    CONFIRM_DATE ,
    ACCT_TYPE_1 ,
    ACCT_TYPE_2 ,
    ACCT_TYPE_3 ,
    ACCT_TYPE_4 ,
    ACCT_TYPE_5 ,
    ALL_CARD ,
    PURCH_MONTH ,
    PURCH_AMT ,
    GROUP_CODE_1 ,
    GROUP_CODE_2 ,
    GROUP_CODE_3 ,
    GROUP_CODE_4 ,
    GROUP_CODE_5 ,
    GROUP_CHIN_1 ,
    GROUP_CHIN_2 ,
    GROUP_CHIN_3 ,
    GROUP_CHIN_4 ,
    GROUP_CHIN_5 ,
    CONDITION ,
    NEW_NOT_PURCHASE_MONTH ,
    NEW_NOT_PURCHASE_YM_CT_FLAG ,
    NEW_NOT_PURCHASE_YM_CT ,
    NEW_NOT_PURCHASE_YM_GC_FLAG ,
    NEW_NOT_PURCHASE_YM_GC ,
    OLD_NOT_BILL_YM_S ,
    OLD_NOT_BILL_YM_E ,
    OLD_NOT_BILL_YM_CT_FLAG ,
    OLD_NOT_BILL_YM_CT ,
    OLD_NOT_BILL_YM_GC_FLAG ,
    OLD_NOT_BILL_YM_GC ,
    NOT_BILL_YM_S ,
    NOT_BILL_YM_E ,
    NOT_BILL_YM_CT_FLAG ,
    NOT_BILL_YM_CT ,
    NOT_BILL_YM_GC_FLAG ,
    NOT_BILL_YM_GC ,
    EXCLUDE_FOREIGNER_FLAG ,
    EXCLUDE_STAFF_FLAG ,
    EXCLUDE_MBULLET_FLAG ,
    EXCLUDE_CALL_SELL_FLAG ,
    EXCLUDE_SMS_FLAG ,
    EXCLUDE_DM_FLAG ,
    EXCLUDE_E_NEWS_FLAG ,
    EXCLUDE_LIST_FLAG ,
    EXCLUDE_LIST ,
    ACCT_TYPE_CNT1 ,
    ACCT_TYPE_CNT2 ,
    ACCT_TYPE_CNT3 ,
    ACCT_TYPE_CNT4 ,
    ACCT_TYPE_CNT5 ,
    TOTAL_CNT
    --,GROUP_1_CNT
    --,GROUP_2_CNT
    --,GROUP_3_CNT
    --,GROUP_4_CNT
    --,GROUP_5_CNT
    --,GROUP_DUP_CNT
,
    NORMAL_CARD_CNT ,
    NO_PURCH_CNT ,
    CONFIRM_PRINT ,
    FILE_DATE ,
    EMPLOYEE_NO ,
    COMMIT_CODE ,
    PROCESS_CODE ON
    MKT_MONTH_PAR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_MKT_MONTH_PAR( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BATCH_NO ,
    DESCRIPTION ,
    CONFIRM_PARM ,
    CONFIRM_DATE ,
    ACCT_TYPE_1 ,
    ACCT_TYPE_2 ,
    ACCT_TYPE_3 ,
    ACCT_TYPE_4 ,
    ACCT_TYPE_5 ,
    ALL_CARD ,
    PURCH_MONTH ,
    PURCH_AMT ,
    GROUP_CODE_1 ,
    GROUP_CODE_2 ,
    GROUP_CODE_3 ,
    GROUP_CODE_4 ,
    GROUP_CODE_5 ,
    GROUP_CHIN_1 ,
    GROUP_CHIN_2 ,
    GROUP_CHIN_3 ,
    GROUP_CHIN_4 ,
    GROUP_CHIN_5 ,
    CONDITION ,
    NEW_NOT_PURCHASE_MONTH ,
    NEW_NOT_PURCHASE_YM_CT_FLAG ,
    NEW_NOT_PURCHASE_YM_CT ,
    NEW_NOT_PURCHASE_YM_GC_FLAG ,
    NEW_NOT_PURCHASE_YM_GC ,
    OLD_NOT_BILL_YM_S ,
    OLD_NOT_BILL_YM_E ,
    OLD_NOT_BILL_YM_CT_FLAG ,
    OLD_NOT_BILL_YM_CT ,
    OLD_NOT_BILL_YM_GC_FLAG ,
    OLD_NOT_BILL_YM_GC ,
    NOT_BILL_YM_S ,
    NOT_BILL_YM_E ,
    NOT_BILL_YM_CT_FLAG ,
    NOT_BILL_YM_CT ,
    NOT_BILL_YM_GC_FLAG ,
    NOT_BILL_YM_GC ,
    EXCLUDE_FOREIGNER_FLAG ,
    EXCLUDE_STAFF_FLAG ,
    EXCLUDE_MBULLET_FLAG ,
    EXCLUDE_CALL_SELL_FLAG ,
    EXCLUDE_SMS_FLAG ,
    EXCLUDE_DM_FLAG ,
    EXCLUDE_E_NEWS_FLAG ,
    EXCLUDE_LIST_FLAG ,
    EXCLUDE_LIST ,
    ACCT_TYPE_CNT1 ,
    ACCT_TYPE_CNT2 ,
    ACCT_TYPE_CNT3 ,
    ACCT_TYPE_CNT4 ,
    ACCT_TYPE_CNT5 ,
    TOTAL_CNT
    --,GROUP_1_CNT
    --,GROUP_2_CNT
    --,GROUP_3_CNT
    --,GROUP_4_CNT
    --,GROUP_5_CNT
    --,GROUP_DUP_CNT
,
    NORMAL_CARD_CNT ,
    NO_PURCH_CNT ,
    CONFIRM_PRINT ,
    FILE_DATE ,
    EMPLOYEE_NO ,
    COMMIT_CODE ,
    PROCESS_CODE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BATCH_NO ,
NEW.DESCRIPTION ,
NEW.CONFIRM_PARM ,
NEW.CONFIRM_DATE ,
NEW.ACCT_TYPE_1 ,
NEW.ACCT_TYPE_2 ,
NEW.ACCT_TYPE_3 ,
NEW.ACCT_TYPE_4 ,
NEW.ACCT_TYPE_5 ,
NEW.ALL_CARD ,
NEW.PURCH_MONTH ,
NEW.PURCH_AMT ,
NEW.GROUP_CODE_1 ,
NEW.GROUP_CODE_2 ,
NEW.GROUP_CODE_3 ,
NEW.GROUP_CODE_4 ,
NEW.GROUP_CODE_5 ,
NEW.GROUP_CHIN_1 ,
NEW.GROUP_CHIN_2 ,
NEW.GROUP_CHIN_3 ,
NEW.GROUP_CHIN_4 ,
NEW.GROUP_CHIN_5 ,
NEW.CONDITION ,
NEW.NEW_NOT_PURCHASE_MONTH ,
NEW.NEW_NOT_PURCHASE_YM_CT_FLAG ,
NEW.NEW_NOT_PURCHASE_YM_CT ,
NEW.NEW_NOT_PURCHASE_YM_GC_FLAG ,
NEW.NEW_NOT_PURCHASE_YM_GC ,
NEW.OLD_NOT_BILL_YM_S ,
NEW.OLD_NOT_BILL_YM_E ,
NEW.OLD_NOT_BILL_YM_CT_FLAG ,
NEW.OLD_NOT_BILL_YM_CT ,
NEW.OLD_NOT_BILL_YM_GC_FLAG ,
NEW.OLD_NOT_BILL_YM_GC ,
NEW.NOT_BILL_YM_S ,
NEW.NOT_BILL_YM_E ,
NEW.NOT_BILL_YM_CT_FLAG ,
NEW.NOT_BILL_YM_CT ,
NEW.NOT_BILL_YM_GC_FLAG ,
NEW.NOT_BILL_YM_GC ,
NEW.EXCLUDE_FOREIGNER_FLAG ,
NEW.EXCLUDE_STAFF_FLAG ,
NEW.EXCLUDE_MBULLET_FLAG ,
NEW.EXCLUDE_CALL_SELL_FLAG ,
NEW.EXCLUDE_SMS_FLAG ,
NEW.EXCLUDE_DM_FLAG ,
NEW.EXCLUDE_E_NEWS_FLAG ,
NEW.EXCLUDE_LIST_FLAG ,
NEW.EXCLUDE_LIST ,
NEW.ACCT_TYPE_CNT1 ,
NEW.ACCT_TYPE_CNT2 ,
NEW.ACCT_TYPE_CNT3 ,
NEW.ACCT_TYPE_CNT4 ,
NEW.ACCT_TYPE_CNT5 ,
NEW.TOTAL_CNT
--,NEW.GROUP_1_CNT
--,NEW.GROUP_2_CNT
--,NEW.GROUP_3_CNT
--,NEW.GROUP_4_CNT
--,NEW.GROUP_5_CNT
--,NEW.GROUP_DUP_CNT
,
NEW.NORMAL_CARD_CNT ,
NEW.NO_PURCH_CNT ,
NEW.CONFIRM_PRINT ,
NEW.FILE_DATE ,
NEW.EMPLOYEE_NO ,
NEW.COMMIT_CODE ,
NEW.PROCESS_CODE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_PURC_GP_A AFTER
INSERT
    ON
    MKT_PURC_GP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_MKT_PURC_GP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    DESCRIPTION ,
    REWARD_TYPE ,
    BASE_AMT_1 ,
    PURCH_AMT_S_A1 ,
    PURCH_AMT_E_A1 ,
    RATE_A1 ,
    PURCH_AMT_S_A2 ,
    PURCH_AMT_E_A2 ,
    RATE_A2 ,
    PURCH_AMT_S_A3 ,
    PURCH_AMT_E_A3 ,
    RATE_A3 ,
    PURCH_AMT_S_A4 ,
    PURCH_AMT_E_A4 ,
    RATE_A4 ,
    PURCH_AMT_S_A5 ,
    PURCH_AMT_E_A5 ,
    RATE_A5 ,
    MCHT_NO_1 ,
    MCHT_NO_2 ,
    MCHT_NO_3 ,
    MCHT_NO_4 ,
    MCHT_NO_5 ,
    MCHT_NO_6 ,
    MCHT_NO_7 ,
    MCHT_NO_8 ,
    MCHT_NO_9 ,
    MCHT_NO_10 ,
    INT_AMT_S_1 ,
    INT_AMT_E_1 ,
    INT_RATE_1 ,
    INT_AMT_S_2 ,
    INT_AMT_E_2 ,
    INT_RATE_2 ,
    INT_AMT_S_3 ,
    INT_AMT_E_3 ,
    INT_RATE_3 ,
    INT_AMT_S_4 ,
    INT_AMT_E_4 ,
    INT_RATE_4 ,
    INT_AMT_S_5 ,
    INT_AMT_E_5 ,
    INT_RATE_5 ,
    OUT_AMT_S_1 ,
    OUT_AMT_E_1 ,
    OUT_RATE_1 ,
    OUT_AMT_S_2 ,
    OUT_AMT_E_2 ,
    OUT_RATE_2 ,
    OUT_AMT_S_3 ,
    OUT_AMT_E_3 ,
    OUT_RATE_3 ,
    OUT_AMT_S_4 ,
    OUT_AMT_E_4 ,
    OUT_RATE_4 ,
    OUT_AMT_S_5 ,
    OUT_AMT_E_5 ,
    OUT_RATE_5 ,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.DESCRIPTION ,
NEW.REWARD_TYPE ,
NEW.BASE_AMT_1 ,
NEW.PURCH_AMT_S_A1 ,
NEW.PURCH_AMT_E_A1 ,
NEW.RATE_A1 ,
NEW.PURCH_AMT_S_A2 ,
NEW.PURCH_AMT_E_A2 ,
NEW.RATE_A2 ,
NEW.PURCH_AMT_S_A3 ,
NEW.PURCH_AMT_E_A3 ,
NEW.RATE_A3 ,
NEW.PURCH_AMT_S_A4 ,
NEW.PURCH_AMT_E_A4 ,
NEW.RATE_A4 ,
NEW.PURCH_AMT_S_A5 ,
NEW.PURCH_AMT_E_A5 ,
NEW.RATE_A5 ,
NEW.MCHT_NO_1 ,
NEW.MCHT_NO_2 ,
NEW.MCHT_NO_3 ,
NEW.MCHT_NO_4 ,
NEW.MCHT_NO_5 ,
NEW.MCHT_NO_6 ,
NEW.MCHT_NO_7 ,
NEW.MCHT_NO_8 ,
NEW.MCHT_NO_9 ,
NEW.MCHT_NO_10 ,
NEW.INT_AMT_S_1 ,
NEW.INT_AMT_E_1 ,
NEW.INT_RATE_1 ,
NEW.INT_AMT_S_2 ,
NEW.INT_AMT_E_2 ,
NEW.INT_RATE_2 ,
NEW.INT_AMT_S_3 ,
NEW.INT_AMT_E_3 ,
NEW.INT_RATE_3 ,
NEW.INT_AMT_S_4 ,
NEW.INT_AMT_E_4 ,
NEW.INT_RATE_4 ,
NEW.INT_AMT_S_5 ,
NEW.INT_AMT_E_5 ,
NEW.INT_RATE_5 ,
NEW.OUT_AMT_S_1 ,
NEW.OUT_AMT_E_1 ,
NEW.OUT_RATE_1 ,
NEW.OUT_AMT_S_2 ,
NEW.OUT_AMT_E_2 ,
NEW.OUT_RATE_2 ,
NEW.OUT_AMT_S_3 ,
NEW.OUT_AMT_E_3 ,
NEW.OUT_RATE_3 ,
NEW.OUT_AMT_S_4 ,
NEW.OUT_AMT_E_4 ,
NEW.OUT_RATE_4 ,
NEW.OUT_AMT_S_5 ,
NEW.OUT_AMT_E_5 ,
NEW.OUT_RATE_5 ,
NEW.CRT_USER ,
NEW.CRT_DATE ,
NEW.APR_FLAG ,
NEW.APR_USER ,
NEW.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_PURC_GP_D AFTER
DELETE
    ON
    MKT_PURC_GP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_MKT_PURC_GP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    DESCRIPTION ,
    REWARD_TYPE ,
    BASE_AMT_1 ,
    PURCH_AMT_S_A1 ,
    PURCH_AMT_E_A1 ,
    RATE_A1 ,
    PURCH_AMT_S_A2 ,
    PURCH_AMT_E_A2 ,
    RATE_A2 ,
    PURCH_AMT_S_A3 ,
    PURCH_AMT_E_A3 ,
    RATE_A3 ,
    PURCH_AMT_S_A4 ,
    PURCH_AMT_E_A4 ,
    RATE_A4 ,
    PURCH_AMT_S_A5 ,
    PURCH_AMT_E_A5 ,
    RATE_A5 ,
    MCHT_NO_1 ,
    MCHT_NO_2 ,
    MCHT_NO_3 ,
    MCHT_NO_4 ,
    MCHT_NO_5 ,
    MCHT_NO_6 ,
    MCHT_NO_7 ,
    MCHT_NO_8 ,
    MCHT_NO_9 ,
    MCHT_NO_10 ,
    INT_AMT_S_1 ,
    INT_AMT_E_1 ,
    INT_RATE_1 ,
    INT_AMT_S_2 ,
    INT_AMT_E_2 ,
    INT_RATE_2 ,
    INT_AMT_S_3 ,
    INT_AMT_E_3 ,
    INT_RATE_3 ,
    INT_AMT_S_4 ,
    INT_AMT_E_4 ,
    INT_RATE_4 ,
    INT_AMT_S_5 ,
    INT_AMT_E_5 ,
    INT_RATE_5 ,
    OUT_AMT_S_1 ,
    OUT_AMT_E_1 ,
    OUT_RATE_1 ,
    OUT_AMT_S_2 ,
    OUT_AMT_E_2 ,
    OUT_RATE_2 ,
    OUT_AMT_S_3 ,
    OUT_AMT_E_3 ,
    OUT_RATE_3 ,
    OUT_AMT_S_4 ,
    OUT_AMT_E_4 ,
    OUT_RATE_4 ,
    OUT_AMT_S_5 ,
    OUT_AMT_E_5 ,
    OUT_RATE_5 ,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.GROUP_CODE ,
OLD.DESCRIPTION ,
OLD.REWARD_TYPE ,
OLD.BASE_AMT_1 ,
OLD.PURCH_AMT_S_A1 ,
OLD.PURCH_AMT_E_A1 ,
OLD.RATE_A1 ,
OLD.PURCH_AMT_S_A2 ,
OLD.PURCH_AMT_E_A2 ,
OLD.RATE_A2 ,
OLD.PURCH_AMT_S_A3 ,
OLD.PURCH_AMT_E_A3 ,
OLD.RATE_A3 ,
OLD.PURCH_AMT_S_A4 ,
OLD.PURCH_AMT_E_A4 ,
OLD.RATE_A4 ,
OLD.PURCH_AMT_S_A5 ,
OLD.PURCH_AMT_E_A5 ,
OLD.RATE_A5 ,
OLD.MCHT_NO_1 ,
OLD.MCHT_NO_2 ,
OLD.MCHT_NO_3 ,
OLD.MCHT_NO_4 ,
OLD.MCHT_NO_5 ,
OLD.MCHT_NO_6 ,
OLD.MCHT_NO_7 ,
OLD.MCHT_NO_8 ,
OLD.MCHT_NO_9 ,
OLD.MCHT_NO_10 ,
OLD.INT_AMT_S_1 ,
OLD.INT_AMT_E_1 ,
OLD.INT_RATE_1 ,
OLD.INT_AMT_S_2 ,
OLD.INT_AMT_E_2 ,
OLD.INT_RATE_2 ,
OLD.INT_AMT_S_3 ,
OLD.INT_AMT_E_3 ,
OLD.INT_RATE_3 ,
OLD.INT_AMT_S_4 ,
OLD.INT_AMT_E_4 ,
OLD.INT_RATE_4 ,
OLD.INT_AMT_S_5 ,
OLD.INT_AMT_E_5 ,
OLD.INT_RATE_5 ,
OLD.OUT_AMT_S_1 ,
OLD.OUT_AMT_E_1 ,
OLD.OUT_RATE_1 ,
OLD.OUT_AMT_S_2 ,
OLD.OUT_AMT_E_2 ,
OLD.OUT_RATE_2 ,
OLD.OUT_AMT_S_3 ,
OLD.OUT_AMT_E_3 ,
OLD.OUT_RATE_3 ,
OLD.OUT_AMT_S_4 ,
OLD.OUT_AMT_E_4 ,
OLD.OUT_RATE_4 ,
OLD.OUT_AMT_S_5 ,
OLD.OUT_AMT_E_5 ,
OLD.OUT_RATE_5 ,
OLD.CRT_USER ,
OLD.CRT_DATE ,
OLD.APR_FLAG ,
OLD.APR_USER ,
OLD.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_PURC_GP_U AFTER
UPDATE
    OF DESCRIPTION ,
    REWARD_TYPE ,
    BASE_AMT_1 ,
    PURCH_AMT_S_A1 ,
    PURCH_AMT_E_A1 ,
    RATE_A1 ,
    PURCH_AMT_S_A2 ,
    PURCH_AMT_E_A2 ,
    RATE_A2 ,
    PURCH_AMT_S_A3 ,
    PURCH_AMT_E_A3 ,
    RATE_A3 ,
    PURCH_AMT_S_A4 ,
    PURCH_AMT_E_A4 ,
    RATE_A4 ,
    PURCH_AMT_S_A5 ,
    PURCH_AMT_E_A5 ,
    RATE_A5 ,
    MCHT_NO_1 ,
    MCHT_NO_2 ,
    MCHT_NO_3 ,
    MCHT_NO_4 ,
    MCHT_NO_5 ,
    MCHT_NO_6 ,
    MCHT_NO_7 ,
    MCHT_NO_8 ,
    MCHT_NO_9 ,
    MCHT_NO_10 ,
    INT_AMT_S_1 ,
    INT_AMT_E_1 ,
    INT_RATE_1 ,
    INT_AMT_S_2 ,
    INT_AMT_E_2 ,
    INT_RATE_2 ,
    INT_AMT_S_3 ,
    INT_AMT_E_3 ,
    INT_RATE_3 ,
    INT_AMT_S_4 ,
    INT_AMT_E_4 ,
    INT_RATE_4 ,
    INT_AMT_S_5 ,
    INT_AMT_E_5 ,
    INT_RATE_5 ,
    OUT_AMT_S_1 ,
    OUT_AMT_E_1 ,
    OUT_RATE_1 ,
    OUT_AMT_S_2 ,
    OUT_AMT_E_2 ,
    OUT_RATE_2 ,
    OUT_AMT_S_3 ,
    OUT_AMT_E_3 ,
    OUT_RATE_3 ,
    OUT_AMT_S_4 ,
    OUT_AMT_E_4 ,
    OUT_RATE_4 ,
    OUT_AMT_S_5 ,
    OUT_AMT_E_5 ,
    OUT_RATE_5 ,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE ON
    MKT_PURC_GP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 SET
wk_audcode = 'U';--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_MKT_PURC_GP( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    DESCRIPTION ,
    REWARD_TYPE ,
    BASE_AMT_1 ,
    PURCH_AMT_S_A1 ,
    PURCH_AMT_E_A1 ,
    RATE_A1 ,
    PURCH_AMT_S_A2 ,
    PURCH_AMT_E_A2 ,
    RATE_A2 ,
    PURCH_AMT_S_A3 ,
    PURCH_AMT_E_A3 ,
    RATE_A3 ,
    PURCH_AMT_S_A4 ,
    PURCH_AMT_E_A4 ,
    RATE_A4 ,
    PURCH_AMT_S_A5 ,
    PURCH_AMT_E_A5 ,
    RATE_A5 ,
    MCHT_NO_1 ,
    MCHT_NO_2 ,
    MCHT_NO_3 ,
    MCHT_NO_4 ,
    MCHT_NO_5 ,
    MCHT_NO_6 ,
    MCHT_NO_7 ,
    MCHT_NO_8 ,
    MCHT_NO_9 ,
    MCHT_NO_10 ,
    INT_AMT_S_1 ,
    INT_AMT_E_1 ,
    INT_RATE_1 ,
    INT_AMT_S_2 ,
    INT_AMT_E_2 ,
    INT_RATE_2 ,
    INT_AMT_S_3 ,
    INT_AMT_E_3 ,
    INT_RATE_3 ,
    INT_AMT_S_4 ,
    INT_AMT_E_4 ,
    INT_RATE_4 ,
    INT_AMT_S_5 ,
    INT_AMT_E_5 ,
    INT_RATE_5 ,
    OUT_AMT_S_1 ,
    OUT_AMT_E_1 ,
    OUT_RATE_1 ,
    OUT_AMT_S_2 ,
    OUT_AMT_E_2 ,
    OUT_RATE_2 ,
    OUT_AMT_S_3 ,
    OUT_AMT_E_3 ,
    OUT_RATE_3 ,
    OUT_AMT_S_4 ,
    OUT_AMT_E_4 ,
    OUT_RATE_4 ,
    OUT_AMT_S_5 ,
    OUT_AMT_E_5 ,
    OUT_RATE_5 ,
    CRT_USER ,
    CRT_DATE ,
    APR_FLAG ,
    APR_USER ,
    APR_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.DESCRIPTION ,
NEW.REWARD_TYPE ,
NEW.BASE_AMT_1 ,
NEW.PURCH_AMT_S_A1 ,
NEW.PURCH_AMT_E_A1 ,
NEW.RATE_A1 ,
NEW.PURCH_AMT_S_A2 ,
NEW.PURCH_AMT_E_A2 ,
NEW.RATE_A2 ,
NEW.PURCH_AMT_S_A3 ,
NEW.PURCH_AMT_E_A3 ,
NEW.RATE_A3 ,
NEW.PURCH_AMT_S_A4 ,
NEW.PURCH_AMT_E_A4 ,
NEW.RATE_A4 ,
NEW.PURCH_AMT_S_A5 ,
NEW.PURCH_AMT_E_A5 ,
NEW.RATE_A5 ,
NEW.MCHT_NO_1 ,
NEW.MCHT_NO_2 ,
NEW.MCHT_NO_3 ,
NEW.MCHT_NO_4 ,
NEW.MCHT_NO_5 ,
NEW.MCHT_NO_6 ,
NEW.MCHT_NO_7 ,
NEW.MCHT_NO_8 ,
NEW.MCHT_NO_9 ,
NEW.MCHT_NO_10 ,
NEW.INT_AMT_S_1 ,
NEW.INT_AMT_E_1 ,
NEW.INT_RATE_1 ,
NEW.INT_AMT_S_2 ,
NEW.INT_AMT_E_2 ,
NEW.INT_RATE_2 ,
NEW.INT_AMT_S_3 ,
NEW.INT_AMT_E_3 ,
NEW.INT_RATE_3 ,
NEW.INT_AMT_S_4 ,
NEW.INT_AMT_E_4 ,
NEW.INT_RATE_4 ,
NEW.INT_AMT_S_5 ,
NEW.INT_AMT_E_5 ,
NEW.INT_RATE_5 ,
NEW.OUT_AMT_S_1 ,
NEW.OUT_AMT_E_1 ,
NEW.OUT_RATE_1 ,
NEW.OUT_AMT_S_2 ,
NEW.OUT_AMT_E_2 ,
NEW.OUT_RATE_2 ,
NEW.OUT_AMT_S_3 ,
NEW.OUT_AMT_E_3 ,
NEW.OUT_RATE_3 ,
NEW.OUT_AMT_S_4 ,
NEW.OUT_AMT_E_4 ,
NEW.OUT_RATE_4 ,
NEW.OUT_AMT_S_5 ,
NEW.OUT_AMT_E_5 ,
NEW.OUT_RATE_5 ,
NEW.CRT_USER ,
NEW.CRT_DATE ,
NEW.APR_FLAG ,
NEW.APR_USER ,
NEW.APR_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_REPT_PAR_A AFTER
INSERT
    ON
    MKT_REPT_PAR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_MKT_REPT_PAR( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BATCH_NO ,
    DESCRIPTION ,
    ACCT_TYPE_SEL ,
    GROUP_CODE_SEL ,
    SOURCE_CODE_SEL
    --,TYPE_SELECT
    --,ACCT_TYPE_1
    --,ACCT_TYPE_2
    --,ACCT_TYPE_3
    --,ACCT_TYPE_4
    --,ACCT_TYPE_5
    --,GROUP_CODE_S1
    --,GROUP_CODE_S2
    --,GROUP_CODE_S3
    --,GROUP_CODE_S4
    --,GROUP_CODE_S5
,
    DUP_GROUP
    --,SOURCE_CODE_S1
    --,SOURCE_CODE_S2
    --,SOURCE_CODE_S3
    --,SOURCE_CODE_S4
    --,SOURCE_CODE_S5
,
    DUP_SOURCE ,
    CARDDATE_SEL ,
    ISSUE_DATE_S ,
    ISSUE_DATE_E ,
    CHANGE_DATE_S ,
    CHANGE_DATE_E ,
    FORMAT_FORM ,
    STOP_DATE_S ,
    STOP_DATE_E ,
    VALIDATE_CARD ,
    INVALIDATE_CARD ,
    PROCESS_CODE ,
    APPLY_DATE_S ,
    APPLY_DATE_E ,
    SUBISSUE_DATE_S ,
    SUBISSUE_DATE_E ,
    REISSUE_DATE_S ,
    REISSUE_DATE_E ,
    TEXT_FORM ,
    ZIP_PASSWD )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BATCH_NO ,
NEW.DESCRIPTION ,
NEW.ACCT_TYPE_SEL ,
NEW.GROUP_CODE_SEL ,
NEW.SOURCE_CODE_SEL
--,NEW.TYPE_SELECT
--,NEW.ACCT_TYPE_1
--,NEW.ACCT_TYPE_2
--,NEW.ACCT_TYPE_3
--,NEW.ACCT_TYPE_4
--,NEW.ACCT_TYPE_5
--,NEW.GROUP_CODE_S1
--,NEW.GROUP_CODE_S2
--,NEW.GROUP_CODE_S3
--,NEW.GROUP_CODE_S4
--,NEW.GROUP_CODE_S5
,
NEW.DUP_GROUP
--,NEW.SOURCE_CODE_S1
--,NEW.SOURCE_CODE_S2
--,NEW.SOURCE_CODE_S3
--,NEW.SOURCE_CODE_S4
--,NEW.SOURCE_CODE_S5
,
NEW.DUP_SOURCE ,
NEW.CARDDATE_SEL ,
NEW.ISSUE_DATE_S ,
NEW.ISSUE_DATE_E ,
NEW.CHANGE_DATE_S ,
NEW.CHANGE_DATE_E ,
NEW.FORMAT_FORM ,
NEW.STOP_DATE_S ,
NEW.STOP_DATE_E ,
NEW.VALIDATE_CARD ,
NEW.INVALIDATE_CARD ,
NEW.PROCESS_CODE ,
NEW.APPLY_DATE_S ,
NEW.APPLY_DATE_E ,
NEW.SUBISSUE_DATE_S ,
NEW.SUBISSUE_DATE_E ,
NEW.REISSUE_DATE_S ,
NEW.REISSUE_DATE_E ,
NEW.TEXT_FORM ,
NEW.ZIP_PASSWD );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_REPT_PAR_D AFTER
DELETE
    ON
    MKT_REPT_PAR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_MKT_REPT_PAR( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BATCH_NO ,
    DESCRIPTION ,
    ACCT_TYPE_SEL ,
    GROUP_CODE_SEL ,
    SOURCE_CODE_SEL
    --,TYPE_SELECT
    --,ACCT_TYPE_1
    --,ACCT_TYPE_2
    --,ACCT_TYPE_3
    --,ACCT_TYPE_4
    --,ACCT_TYPE_5
    --,GROUP_CODE_S1
    --,GROUP_CODE_S2
    --,GROUP_CODE_S3
    --,GROUP_CODE_S4
    --,GROUP_CODE_S5
,
    DUP_GROUP
    --,SOURCE_CODE_S1
    --,SOURCE_CODE_S2
    --,SOURCE_CODE_S3
    --,SOURCE_CODE_S4
    --,SOURCE_CODE_S5
,
    DUP_SOURCE ,
    CARDDATE_SEL ,
    ISSUE_DATE_S ,
    ISSUE_DATE_E ,
    CHANGE_DATE_S ,
    CHANGE_DATE_E ,
    FORMAT_FORM ,
    STOP_DATE_S ,
    STOP_DATE_E ,
    VALIDATE_CARD ,
    INVALIDATE_CARD ,
    PROCESS_CODE ,
    APPLY_DATE_S ,
    APPLY_DATE_E ,
    SUBISSUE_DATE_S ,
    SUBISSUE_DATE_E ,
    REISSUE_DATE_S ,
    REISSUE_DATE_E ,
    TEXT_FORM ,
    ZIP_PASSWD )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.BATCH_NO ,
OLD.DESCRIPTION ,
OLD.ACCT_TYPE_SEL ,
OLD.GROUP_CODE_SEL ,
OLD.SOURCE_CODE_SEL
--,OLD.TYPE_SELECT
--,OLD.ACCT_TYPE_1
--,OLD.ACCT_TYPE_2
--,OLD.ACCT_TYPE_3
--,OLD.ACCT_TYPE_4
--,OLD.ACCT_TYPE_5
--,OLD.GROUP_CODE_S1
--,OLD.GROUP_CODE_S2
--,OLD.GROUP_CODE_S3
--,OLD.GROUP_CODE_S4
--,OLD.GROUP_CODE_S5
,
OLD.DUP_GROUP
--,OLD.SOURCE_CODE_S1
--,OLD.SOURCE_CODE_S2
--,OLD.SOURCE_CODE_S3
--,OLD.SOURCE_CODE_S4
--,OLD.SOURCE_CODE_S5
,
OLD.DUP_SOURCE ,
OLD.CARDDATE_SEL ,
OLD.ISSUE_DATE_S ,
OLD.ISSUE_DATE_E ,
OLD.CHANGE_DATE_S ,
OLD.CHANGE_DATE_E ,
OLD.FORMAT_FORM ,
OLD.STOP_DATE_S ,
OLD.STOP_DATE_E ,
OLD.VALIDATE_CARD ,
OLD.INVALIDATE_CARD ,
OLD.PROCESS_CODE ,
OLD.APPLY_DATE_S ,
OLD.APPLY_DATE_E ,
OLD.SUBISSUE_DATE_S ,
OLD.SUBISSUE_DATE_E ,
OLD.REISSUE_DATE_S ,
OLD.REISSUE_DATE_E ,
OLD.TEXT_FORM ,
OLD.ZIP_PASSWD );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_REPT_PAR_U AFTER
UPDATE
    OF DESCRIPTION ,
    ACCT_TYPE_SEL ,
    GROUP_CODE_SEL ,
    SOURCE_CODE_SEL
    --,TYPE_SELECT
    --,ACCT_TYPE_1
    --,ACCT_TYPE_2
    --,ACCT_TYPE_3
    --,ACCT_TYPE_4
    --,ACCT_TYPE_5
    --,GROUP_CODE_S1
    --,GROUP_CODE_S2
    --,GROUP_CODE_S3
    --,GROUP_CODE_S4
    --,GROUP_CODE_S5
,
    DUP_GROUP
    --,SOURCE_CODE_S1
    --,SOURCE_CODE_S2
    --,SOURCE_CODE_S3
    --,SOURCE_CODE_S4
    --,SOURCE_CODE_S5
,
    DUP_SOURCE ,
    CARDDATE_SEL ,
    ISSUE_DATE_S ,
    ISSUE_DATE_E ,
    CHANGE_DATE_S ,
    CHANGE_DATE_E ,
    FORMAT_FORM ,
    STOP_DATE_S ,
    STOP_DATE_E ,
    VALIDATE_CARD ,
    INVALIDATE_CARD ,
    PROCESS_CODE ,
    APPLY_DATE_S ,
    APPLY_DATE_E ,
    SUBISSUE_DATE_S ,
    SUBISSUE_DATE_E ,
    REISSUE_DATE_S ,
    REISSUE_DATE_E ,
    TEXT_FORM ,
    ZIP_PASSWD ON
    MKT_REPT_PAR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_MKT_REPT_PAR( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BATCH_NO ,
    DESCRIPTION ,
    ACCT_TYPE_SEL ,
    GROUP_CODE_SEL ,
    SOURCE_CODE_SEL
    --,TYPE_SELECT
    --,ACCT_TYPE_1
    --,ACCT_TYPE_2
    --,ACCT_TYPE_3
    --,ACCT_TYPE_4
    --,ACCT_TYPE_5
    --,GROUP_CODE_S1
    --,GROUP_CODE_S2
    --,GROUP_CODE_S3
    --,GROUP_CODE_S4
    --,GROUP_CODE_S5
,
    DUP_GROUP
    --,SOURCE_CODE_S1
    --,SOURCE_CODE_S2
    --,SOURCE_CODE_S3
    --,SOURCE_CODE_S4
    --,SOURCE_CODE_S5
,
    DUP_SOURCE ,
    CARDDATE_SEL ,
    ISSUE_DATE_S ,
    ISSUE_DATE_E ,
    CHANGE_DATE_S ,
    CHANGE_DATE_E ,
    FORMAT_FORM ,
    STOP_DATE_S ,
    STOP_DATE_E ,
    VALIDATE_CARD ,
    INVALIDATE_CARD ,
    PROCESS_CODE ,
    APPLY_DATE_S ,
    APPLY_DATE_E ,
    SUBISSUE_DATE_S ,
    SUBISSUE_DATE_E ,
    REISSUE_DATE_S ,
    REISSUE_DATE_E ,
    TEXT_FORM ,
    ZIP_PASSWD )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BATCH_NO ,
NEW.DESCRIPTION ,
NEW.ACCT_TYPE_SEL ,
NEW.GROUP_CODE_SEL ,
NEW.SOURCE_CODE_SEL
--,NEW.TYPE_SELECT
--,NEW.ACCT_TYPE_1
--,NEW.ACCT_TYPE_2
--,NEW.ACCT_TYPE_3
--,NEW.ACCT_TYPE_4
--,NEW.ACCT_TYPE_5
--,NEW.GROUP_CODE_S1
--,NEW.GROUP_CODE_S2
--,NEW.GROUP_CODE_S3
--,NEW.GROUP_CODE_S4
--,NEW.GROUP_CODE_S5
,
NEW.DUP_GROUP
--,NEW.SOURCE_CODE_S1
--,NEW.SOURCE_CODE_S2
--,NEW.SOURCE_CODE_S3
--,NEW.SOURCE_CODE_S4
--,NEW.SOURCE_CODE_S5
,
NEW.DUP_SOURCE ,
NEW.CARDDATE_SEL ,
NEW.ISSUE_DATE_S ,
NEW.ISSUE_DATE_E ,
NEW.CHANGE_DATE_S ,
NEW.CHANGE_DATE_E ,
NEW.FORMAT_FORM ,
NEW.STOP_DATE_S ,
NEW.STOP_DATE_E ,
NEW.VALIDATE_CARD ,
NEW.INVALIDATE_CARD ,
NEW.PROCESS_CODE ,
NEW.APPLY_DATE_S ,
NEW.APPLY_DATE_E ,
NEW.SUBISSUE_DATE_S ,
NEW.SUBISSUE_DATE_E ,
NEW.REISSUE_DATE_S ,
NEW.REISSUE_DATE_E ,
NEW.TEXT_FORM ,
NEW.ZIP_PASSWD );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_VENDOR_A AFTER
INSERT
    ON
    MKT_VENDOR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_MKT_VENDOR( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_VENDOR_NO ,
    VENDOR_NAME ,
    NAME ,
    ID_NO ,
    TEL_NO ,
    CONTACT_ID ,
    CONTACT_TEL ,
    AREA_CODE ,
    ADDRESS1 ,
    ADDRESS2 ,
    ADDRESS3 ,
    ADDRESS4 ,
    ADDRESS5 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.VENDOR_NO ,
NEW.VENDOR_NAME ,
NEW.NAME ,
NEW.ID_NO ,
NEW.TEL_NO ,
NEW.CONTACT_ID ,
NEW.CONTACT_TEL ,
NEW.AREA_CODE ,
NEW.ADDRESS1 ,
NEW.ADDRESS2 ,
NEW.ADDRESS3 ,
NEW.ADDRESS4 ,
NEW.ADDRESS5 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_VENDOR_D AFTER
DELETE
    ON
    MKT_VENDOR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_MKT_VENDOR( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_VENDOR_NO ,
    VENDOR_NAME ,
    NAME ,
    ID_NO ,
    TEL_NO ,
    CONTACT_ID ,
    CONTACT_TEL ,
    AREA_CODE ,
    ADDRESS1 ,
    ADDRESS2 ,
    ADDRESS3 ,
    ADDRESS4 ,
    ADDRESS5 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.VENDOR_NO ,
OLD.VENDOR_NAME ,
OLD.NAME ,
OLD.ID_NO ,
OLD.TEL_NO ,
OLD.CONTACT_ID ,
OLD.CONTACT_TEL ,
OLD.AREA_CODE ,
OLD.ADDRESS1 ,
OLD.ADDRESS2 ,
OLD.ADDRESS3 ,
OLD.ADDRESS4 ,
OLD.ADDRESS5 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_MKT_VENDOR_U AFTER
UPDATE
    OF VENDOR_NAME ,
    NAME ,
    ID_NO ,
    TEL_NO ,
    CONTACT_ID ,
    CONTACT_TEL ,
    AREA_CODE ,
    ADDRESS1 ,
    ADDRESS2 ,
    ADDRESS3 ,
    ADDRESS4 ,
    ADDRESS5 ON
    MKT_VENDOR REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_MKT_VENDOR( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_VENDOR_NO ,
    VENDOR_NAME ,
    NAME ,
    ID_NO ,
    TEL_NO ,
    CONTACT_ID ,
    CONTACT_TEL ,
    AREA_CODE ,
    ADDRESS1 ,
    ADDRESS2 ,
    ADDRESS3 ,
    ADDRESS4 ,
    ADDRESS5 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.VENDOR_NO ,
NEW.VENDOR_NAME ,
NEW.NAME ,
NEW.ID_NO ,
NEW.TEL_NO ,
NEW.CONTACT_ID ,
NEW.CONTACT_TEL ,
NEW.AREA_CODE ,
NEW.ADDRESS1 ,
NEW.ADDRESS2 ,
NEW.ADDRESS3 ,
NEW.ADDRESS4 ,
NEW.ADDRESS5 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_ACCT_TYPE_A AFTER
INSERT
    ON
    PTR_ACCT_TYPE REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL
INSERT INTO LOG_PTR_ACCT_TYPE( 
    MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ACCT_TYPE ,
    CHIN_NAME ,
    CURR_CODE ,
    CARD_INDICATOR ,
    F_CURRENCY_FLAG ,
    BONUS_FLAG ,
    RC_USE_FLAG ,
    CAR_SERVICE_FLAG ,
    INSSURANCE_FLAG ,
    U_CYCLE_FLAG ,
    STMT_CYCLE ,
    --CARD_TYPE,
    --GROUP_CODE,
    ATM_CODE ,
    INST_CRDTAMT ,
    INST_CRDTRATE ,
    NO_COLLECTION_FLAG
    )
VALUES ( 
    to_char(sysdate,'yyyymmddhh24misssss') ,
    NEW.MOD_PGM ,
    NEW.MOD_USER ,
    'A' ,
    NEW.ACCT_TYPE ,
    NEW.CHIN_NAME ,
    NEW.CURR_CODE ,
    NEW.CARD_INDICATOR ,
    NEW.F_CURRENCY_FLAG ,
    NEW.BONUS_FLAG ,
    NEW.RC_USE_FLAG ,
    NEW.CAR_SERVICE_FLAG ,
    NEW.INSSURANCE_FLAG ,
    NEW.U_CYCLE_FLAG ,
    NEW.STMT_CYCLE ,
    --NEW.CARD_TYPE ,
    --NEW.GROUP_CODE ,
    NEW.ATM_CODE ,
    NEW.INST_CRDTAMT ,
    NEW.INST_CRDTRATE ,
    NEW.NO_COLLECTION_FLAG
    );


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_ACCT_TYPE_D
AFTER
    DELETE
    ON PTR_ACCT_TYPE
    REFERENCING OLD AS OLD 
    FOR EACH ROW MODE DB2SQL
  
    INSERT INTO LOG_PTR_ACCT_TYPE(
                MOD_TIME
                ,MOD_PGM
                ,MOD_USER
                ,MOD_AUDCODE
                ,K_ACCT_TYPE
                ,CHIN_NAME
                ,CURR_CODE
                ,CARD_INDICATOR
                ,F_CURRENCY_FLAG
                ,BONUS_FLAG
                ,RC_USE_FLAG
                ,CAR_SERVICE_FLAG
                ,INSSURANCE_FLAG
                ,U_CYCLE_FLAG
                ,STMT_CYCLE
                --,CARD_TYPE
                --,GROUP_CODE
                ,ATM_CODE
                ,INST_CRDTAMT
                ,INST_CRDTRATE
                ,NO_COLLECTION_FLAG
           )
    VALUES (
                to_char(sysdate,'yyyymmddhh24misssss')
                ,OLD.MOD_PGM
                ,OLD.MOD_USER
                ,'D'
                ,OLD.ACCT_TYPE
                ,OLD.CHIN_NAME
                ,OLD.CURR_CODE
                ,OLD.CARD_INDICATOR
                ,OLD.F_CURRENCY_FLAG
                ,OLD.BONUS_FLAG
                ,OLD.RC_USE_FLAG
                ,OLD.CAR_SERVICE_FLAG
                ,OLD.INSSURANCE_FLAG
                ,OLD.U_CYCLE_FLAG
                ,OLD.STMT_CYCLE
                --,OLD.CARD_TYPE
                --,OLD.GROUP_CODE
                ,OLD.ATM_CODE
                ,OLD.INST_CRDTAMT
                ,OLD.INST_CRDTRATE
                ,OLD.NO_COLLECTION_FLAG
           );


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_ACCT_TYPE_U
AFTER
    UPDATE OF ACCT_TYPE, ATM_CODE, BONUS_FLAG,
    CARD_INDICATOR, /*CARD_TYPE,*/ CAR_SERVICE_FLAG, CHIN_NAME,
    CURR_CODE, F_CURRENCY_FLAG, /*GROUP_CODE,*/ INSSURANCE_FLAG,
    INST_CRDTAMT, INST_CRDTRATE, MOD_PGM, MOD_USER,
    RC_USE_FLAG, STMT_CYCLE, U_CYCLE_FLAG, NO_COLLECTION_FLAG
    ON PTR_ACCT_TYPE
    REFERENCING NEW AS NEW OLD AS OLD 
    FOR EACH ROW MODE DB2SQL

BEGIN   

DECLARE wk_audcode     VARCHAR(1);--
DECLARE wk_user        VARCHAR(10);--
DECLARE wk_pgm         VARCHAR(20);--
DECLARE wk_ws          VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

    set wk_audcode = 'U';--
  
    INSERT INTO LOG_PTR_ACCT_TYPE(
                MOD_TIME
                ,MOD_PGM
                ,MOD_USER
                ,MOD_AUDCODE
                ,K_ACCT_TYPE
                ,CHIN_NAME
                ,CURR_CODE
                ,CARD_INDICATOR
                ,F_CURRENCY_FLAG
                ,BONUS_FLAG
                ,RC_USE_FLAG
                ,CAR_SERVICE_FLAG
                ,INSSURANCE_FLAG
                ,U_CYCLE_FLAG
                ,STMT_CYCLE
               -- ,CARD_TYPE
               -- ,GROUP_CODE
                ,ATM_CODE
                ,INST_CRDTAMT
                ,INST_CRDTRATE
                ,NO_COLLECTION_FLAG
           )
    VALUES (
                to_char(sysdate,'yyyymmddhh24misssss')
                ,NEW.MOD_PGM
                ,NEW.MOD_USER
                ,wk_audcode
                ,NEW.ACCT_TYPE
                ,NEW.CHIN_NAME
                ,NEW.CURR_CODE
                ,NEW.CARD_INDICATOR
                ,NEW.F_CURRENCY_FLAG
                ,NEW.BONUS_FLAG
                ,NEW.RC_USE_FLAG
                ,NEW.CAR_SERVICE_FLAG
                ,NEW.INSSURANCE_FLAG
                ,NEW.U_CYCLE_FLAG
                ,NEW.STMT_CYCLE
                --,NEW.CARD_TYPE
                --,NEW.GROUP_CODE
                ,NEW.ATM_CODE
                ,NEW.INST_CRDTAMT
                ,NEW.INST_CRDTRATE
                ,NEW.NO_COLLECTION_FLAG
           );--
  
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ACTCODE_A AFTER
INSERT
    ON
    PTR_ACTCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_ACTCODE( mod_time,
    mod_audcode,
    mod_user,
    mod_pgm,
    k_acct_code,
    chi_short_name,
    chi_long_name,
    eng_short_name,
    eng_long_name,
    item_order_normal,
    item_order_refund,
    item_class_normal,
    item_class_refund,
    interest_method,
    inter_rate_code,
    part_rev,
    revolve,
    urge_2st,
    urge_3st,
    occupy,
    receivables,
    item_order_back_date,
    item_class_back_date )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss'),
wk_audcode,
NEW.mod_user,
NEW.mod_pgm,
NEW.acct_code,
NEW.chi_short_name,
NEW.chi_long_name,
NEW.eng_short_name,
NEW.eng_long_name,
NEW.item_order_normal,
NEW.item_order_refund,
NEW.item_class_normal,
NEW.item_class_refund,
NEW.interest_method,
NEW.inter_rate_code,
NEW.part_rev,
NEW.revolve,
NEW.urge_2st,
NEW.urge_3st,
NEW.occupy,
NEW.receivables,
NEW.item_order_back_date,
NEW.item_class_back_date );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ACTCODE_D AFTER
DELETE
    ON
    PTR_ACTCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_ACTCODE( mod_time,
    mod_audcode,
    mod_user,
    mod_pgm,
    k_acct_code,
    chi_short_name,
    chi_long_name,
    eng_short_name,
    eng_long_name,
    item_order_normal,
    item_order_refund,
    item_class_normal,
    item_class_refund,
    interest_method,
    inter_rate_code,
    part_rev,
    revolve,
    urge_2st,
    urge_3st,
    occupy,
    receivables,
    item_order_back_date,
    item_class_back_date )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss'),
wk_audcode,
OLD.mod_user,
OLD.mod_pgm,
OLD.acct_code,
OLD.chi_short_name,
OLD.chi_long_name,
OLD.eng_short_name,
OLD.eng_long_name,
OLD.item_order_normal,
OLD.item_order_refund,
OLD.item_class_normal,
OLD.item_class_refund,
OLD.interest_method,
OLD.inter_rate_code,
OLD.part_rev,
OLD.revolve,
OLD.urge_2st,
OLD.urge_3st,
OLD.occupy,
OLD.receivables,
OLD.item_order_back_date,
OLD.item_class_back_date );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ACTCODE_U AFTER
UPDATE
    OF acct_code,
    chi_short_name,
    chi_long_name,
    eng_short_name,
    eng_long_name,
    item_order_normal,
    item_order_back_date,
    item_order_refund,
    item_class_normal,
    item_class_back_date,
    item_class_refund,
    interest_method,
    inter_rate_code,
    part_rev,
    revolve,
    mod_time,
    mod_pgm,
    mod_seqno,
    inter_rate_code2 ON
    PTR_ACTCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_ACTCODE( mod_time,
    mod_audcode,
    mod_user,
    mod_pgm,
    k_acct_code,
    chi_short_name,
    chi_long_name,
    eng_short_name,
    eng_long_name,
    item_order_normal,
    item_order_refund,
    item_class_normal,
    item_class_refund,
    interest_method,
    inter_rate_code,
    part_rev,
    revolve,
    urge_2st,
    urge_3st,
    occupy,
    receivables,
    item_order_back_date,
    item_class_back_date )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss'),
wk_audcode,
NEW.mod_user,
NEW.mod_pgm,
NEW.acct_code,
NEW.chi_short_name,
NEW.chi_long_name,
NEW.eng_short_name,
NEW.eng_long_name,
NEW.item_order_normal,
NEW.item_order_refund,
NEW.item_class_normal,
NEW.item_class_refund,
NEW.interest_method,
NEW.inter_rate_code,
NEW.part_rev,
NEW.revolve,
NEW.urge_2st,
NEW.urge_3st,
NEW.occupy,
NEW.receivables,
NEW.item_order_back_date,
NEW.item_class_back_date );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_ACTGENERAL AFTER
    INSERT OR
    DELETE OR
    UPDATE OF "ATM_FEE", "AUTOPAY_B_DUE_DAYS", "AUTOPAY_DEDUCT_DAYS",
    "AUTOPAY_ERR_CNT", "FIX_PENALTY", "INSTPAY_B_DUE_DAYS",
    "INSTPAY_DEDUCT_DAYS", "LOW_PENALTY", "LOW_REV_INTEREST",
    "M12_D_B_DAYS", "MAX_PENALTY", "MIN_PAYMENT", "MIN_PENALTY",
    "MIN_PERCENT_PAYMENT", "MIX_MP_BALANCE", "MI_D_MCODE", "MOD_PGM",
    "MOD_USER", "NONAUTOPAY_FEE", "PAYMENT_LMT", "PERCENT_PENALTY",
    "RC_MAX_RATE", "REVOLVING_INTEREST1", "REVOLVING_INTEREST2",
    "REVOLVING_INTEREST3", "REVOLVING_INTEREST4",
    "REVOLVING_INTEREST5", "REVOLVING_INTEREST6"
    ON PTR_ACTGENERAL
	REFERENCING NEW AS NEW OLD AS OLD 
    FOR EACH ROW MODE DB2SQL

BEGIN
DECLARE wk_audcode      VARCHAR(1);--
DECLARE wk_user         VARCHAR(10);--
DECLARE wk_pgm          VARCHAR(20);--
DECLARE wk_ws           VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

  IF INSERTING THEN
       set wk_audcode = 'A';--
  ELSEIF UPDATING THEN
       set wk_audcode = 'U';--
  ELSE
       set wk_audcode   = 'D';--
  END IF;--
  if INSERTING or UPDATING then
    INSERT INTO LOG_PTR_ACTGENERAL(
                MOD_TIME
                ,MOD_PGM
                ,MOD_USER
                ,MOD_AUDCODE
                ,FIX_PENALTY
                ,PERCENT_PENALTY
                ,MAX_PENALTY
                ,MIN_PENALTY
                ,REVOLVING_INTEREST1
                ,REVOLVING_INTEREST2
                ,REVOLVING_INTEREST3
                ,REVOLVING_INTEREST4
                ,REVOLVING_INTEREST5
                ,REVOLVING_INTEREST6
                ,MIN_PERCENT_PAYMENT
                ,MIN_PAYMENT
                ,LOW_PENALTY
                ,LOW_REV_INTEREST
                ,MIX_MP_BALANCE
                ,AUTOPAY_B_DUE_DAYS
                ,AUTOPAY_DEDUCT_DAYS
                ,INSTPAY_B_DUE_DAYS
                ,INSTPAY_DEDUCT_DAYS
                ,AUTOPAY_ERR_CNT
                ,MI_D_MCODE
                ,M12_D_B_DAYS
                ,RC_MAX_RATE
                ,ATM_FEE
                ,NONAUTOPAY_FEE
                ,PAYMENT_LMT
           )
    VALUES (
                to_char(sysdate,'yyyymmddhh24misssss')
                ,NEW.MOD_PGM
                ,NEW.MOD_USER
                ,wk_audcode
                ,NEW.FIX_PENALTY
                ,NEW.PERCENT_PENALTY
                ,NEW.MAX_PENALTY
                ,NEW.MIN_PENALTY
                ,NEW.REVOLVING_INTEREST1
                ,NEW.REVOLVING_INTEREST2
                ,NEW.REVOLVING_INTEREST3
                ,NEW.REVOLVING_INTEREST4
                ,NEW.REVOLVING_INTEREST5
                ,NEW.REVOLVING_INTEREST6
                ,NEW.MIN_PERCENT_PAYMENT
                ,NEW.MIN_PAYMENT
                ,NEW.LOW_PENALTY
                ,NEW.LOW_REV_INTEREST
                ,NEW.MIX_MP_BALANCE
                ,NEW.AUTOPAY_B_DUE_DAYS
                ,NEW.AUTOPAY_DEDUCT_DAYS
                ,NEW.INSTPAY_B_DUE_DAYS
                ,NEW.INSTPAY_DEDUCT_DAYS
                ,NEW.AUTOPAY_ERR_CNT
                ,NEW.MI_D_MCODE
                ,NEW.M12_D_B_DAYS
                ,NEW.RC_MAX_RATE
                ,NEW.ATM_FEE
                ,NEW.NONAUTOPAY_FEE
                ,NEW.PAYMENT_LMT
           );--
  else
    INSERT INTO LOG_PTR_ACTGENERAL(
                MOD_TIME
                ,MOD_PGM
                ,MOD_USER
                ,MOD_AUDCODE
                ,FIX_PENALTY
                ,PERCENT_PENALTY
                ,MAX_PENALTY
                ,MIN_PENALTY
                ,REVOLVING_INTEREST1
                ,REVOLVING_INTEREST2
                ,REVOLVING_INTEREST3
                ,REVOLVING_INTEREST4
                ,REVOLVING_INTEREST5
                ,REVOLVING_INTEREST6
                ,MIN_PERCENT_PAYMENT
                ,MIN_PAYMENT
                ,LOW_PENALTY
                ,LOW_REV_INTEREST
                ,MIX_MP_BALANCE
                ,AUTOPAY_B_DUE_DAYS
                ,AUTOPAY_DEDUCT_DAYS
                ,INSTPAY_B_DUE_DAYS
                ,INSTPAY_DEDUCT_DAYS
                ,AUTOPAY_ERR_CNT
                ,MI_D_MCODE
                ,M12_D_B_DAYS
                ,RC_MAX_RATE
                ,ATM_FEE
                ,NONAUTOPAY_FEE
                ,PAYMENT_LMT
           )
    VALUES (
                to_char(sysdate,'yyyymmddhh24misssss')
                ,OLD.MOD_PGM
                ,OLD.MOD_USER
                ,wk_audcode
                ,OLD.FIX_PENALTY
                ,OLD.PERCENT_PENALTY
                ,OLD.MAX_PENALTY
                ,OLD.MIN_PENALTY
                ,OLD.REVOLVING_INTEREST1
                ,OLD.REVOLVING_INTEREST2
                ,OLD.REVOLVING_INTEREST3
                ,OLD.REVOLVING_INTEREST4
                ,OLD.REVOLVING_INTEREST5
                ,OLD.REVOLVING_INTEREST6
                ,OLD.MIN_PERCENT_PAYMENT
                ,OLD.MIN_PAYMENT
                ,OLD.LOW_PENALTY
                ,OLD.LOW_REV_INTEREST
                ,OLD.MIX_MP_BALANCE
                ,OLD.AUTOPAY_B_DUE_DAYS
                ,OLD.AUTOPAY_DEDUCT_DAYS
                ,OLD.INSTPAY_B_DUE_DAYS
                ,OLD.INSTPAY_DEDUCT_DAYS
                ,OLD.AUTOPAY_ERR_CNT
                ,OLD.MI_D_MCODE
                ,OLD.M12_D_B_DAYS
                ,OLD.RC_MAX_RATE
                ,OLD.ATM_FEE
                ,OLD.NONAUTOPAY_FEE
                ,OLD.PAYMENT_LMT
           );--
  end if;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ADJPARAM_A AFTER
INSERT
    ON
    PTR_ADJPARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_ADJPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_PARAM_NO ,
    K_VALID_DATE ,
    GROUP_CODE ,
    APPROVE_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    N0_MONTH ,
    N0_ADJ_MONTH ,
    N1_CYCLE ,
    MCODE_CURR ,
    N2_CYCLE ,
    MCODE_MIN ,
    N3_CYCLE ,
    PRT_UP_FLAG ,
    PRT_DOWN_FLAG ,
    N4_CYCLE ,
    A_PUR_LO ,
    A_SCALE ,
    A_RATE ,
    A_RATE_FIX ,
    A_ADJ_MIN ,
    A_ADJ_MAX ,
    B_PUR_HI ,
    B_SCALE ,
    B_RATE ,
    B_RATE_FIX ,
    B_ADJ_MIN ,
    B_ADJ_MAX ,
    N5_CYCLE ,
    C_RC_LO ,
    C_RC_HI ,
    C_SCALE ,
    C_RATE ,
    C_RATE_FIX ,
    C_ADJ_MIN ,
    C_ADJ_MAX ,
    D_RATE ,
    D_ADJ_MIN ,
    D_ADJ_MAX ,
    UPGRADE_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PARAM_TYPE ,
NEW.ACCT_TYPE ,
NEW.PARAM_NO ,
NEW.VALID_DATE ,
NEW.GROUP_CODE ,
NEW.APPROVE_FLAG ,
NEW.EXEC_MODE ,
NEW.EXEC_DAY ,
NEW.EXEC_CYCLE_NDAY ,
NEW.N0_MONTH ,
NEW.N0_ADJ_MONTH ,
NEW.N1_CYCLE ,
NEW.MCODE_CURR ,
NEW.N2_CYCLE ,
NEW.MCODE_MIN ,
NEW.N3_CYCLE ,
NEW.PRT_UP_FLAG ,
NEW.PRT_DOWN_FLAG ,
NEW.N4_CYCLE ,
NEW.A_PUR_LO ,
NEW.A_SCALE ,
NEW.A_RATE ,
NEW.A_RATE_FIX ,
NEW.A_ADJ_MIN ,
NEW.A_ADJ_MAX ,
NEW.B_PUR_HI ,
NEW.B_SCALE ,
NEW.B_RATE ,
NEW.B_RATE_FIX ,
NEW.B_ADJ_MIN ,
NEW.B_ADJ_MAX ,
NEW.N5_CYCLE ,
NEW.C_RC_LO ,
NEW.C_RC_HI ,
NEW.C_SCALE ,
NEW.C_RATE ,
NEW.C_RATE_FIX ,
NEW.C_ADJ_MIN ,
NEW.C_ADJ_MAX ,
NEW.D_RATE ,
NEW.D_ADJ_MIN ,
NEW.D_ADJ_MAX ,
NEW.UPGRADE_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ADJPARAM_D AFTER
DELETE
    ON
    PTR_ADJPARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_ADJPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_PARAM_NO ,
    K_VALID_DATE ,
    GROUP_CODE ,
    APPROVE_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    N0_MONTH ,
    N0_ADJ_MONTH ,
    N1_CYCLE ,
    MCODE_CURR ,
    N2_CYCLE ,
    MCODE_MIN ,
    N3_CYCLE ,
    PRT_UP_FLAG ,
    PRT_DOWN_FLAG ,
    N4_CYCLE ,
    A_PUR_LO ,
    A_SCALE ,
    A_RATE ,
    A_RATE_FIX ,
    A_ADJ_MIN ,
    A_ADJ_MAX ,
    B_PUR_HI ,
    B_SCALE ,
    B_RATE ,
    B_RATE_FIX ,
    B_ADJ_MIN ,
    B_ADJ_MAX ,
    N5_CYCLE ,
    C_RC_LO ,
    C_RC_HI ,
    C_SCALE ,
    C_RATE ,
    C_RATE_FIX ,
    C_ADJ_MIN ,
    C_ADJ_MAX ,
    D_RATE ,
    D_ADJ_MIN ,
    D_ADJ_MAX ,
    UPGRADE_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.PARAM_TYPE ,
OLD.ACCT_TYPE ,
OLD.PARAM_NO ,
OLD.VALID_DATE ,
OLD.GROUP_CODE ,
OLD.APPROVE_FLAG ,
OLD.EXEC_MODE ,
OLD.EXEC_DAY ,
OLD.EXEC_CYCLE_NDAY ,
OLD.N0_MONTH ,
OLD.N0_ADJ_MONTH ,
OLD.N1_CYCLE ,
OLD.MCODE_CURR ,
OLD.N2_CYCLE ,
OLD.MCODE_MIN ,
OLD.N3_CYCLE ,
OLD.PRT_UP_FLAG ,
OLD.PRT_DOWN_FLAG ,
OLD.N4_CYCLE ,
OLD.A_PUR_LO ,
OLD.A_SCALE ,
OLD.A_RATE ,
OLD.A_RATE_FIX ,
OLD.A_ADJ_MIN ,
OLD.A_ADJ_MAX ,
OLD.B_PUR_HI ,
OLD.B_SCALE ,
OLD.B_RATE ,
OLD.B_RATE_FIX ,
OLD.B_ADJ_MIN ,
OLD.B_ADJ_MAX ,
OLD.N5_CYCLE ,
OLD.C_RC_LO ,
OLD.C_RC_HI ,
OLD.C_SCALE ,
OLD.C_RATE ,
OLD.C_RATE_FIX ,
OLD.C_ADJ_MIN ,
OLD.C_ADJ_MAX ,
OLD.D_RATE ,
OLD.D_ADJ_MIN ,
OLD.D_ADJ_MAX ,
OLD.UPGRADE_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ADJPARAM_U AFTER
UPDATE
    OF GROUP_CODE ,
    APPROVE_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    N0_MONTH ,
    N0_ADJ_MONTH ,
    N1_CYCLE ,
    MCODE_CURR ,
    N2_CYCLE ,
    MCODE_MIN ,
    N3_CYCLE ,
    PRT_UP_FLAG ,
    PRT_DOWN_FLAG ,
    N4_CYCLE ,
    A_PUR_LO ,
    A_SCALE ,
    A_RATE ,
    A_RATE_FIX ,
    A_ADJ_MIN ,
    A_ADJ_MAX ,
    B_PUR_HI ,
    B_SCALE ,
    B_RATE ,
    B_RATE_FIX ,
    B_ADJ_MIN ,
    B_ADJ_MAX ,
    N5_CYCLE ,
    C_RC_LO ,
    C_RC_HI ,
    C_SCALE ,
    C_RATE ,
    C_RATE_FIX ,
    C_ADJ_MIN ,
    C_ADJ_MAX ,
    D_RATE ,
    D_ADJ_MIN ,
    D_ADJ_MAX ,
    UPGRADE_AMT ON
    PTR_ADJPARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_ADJPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_PARAM_NO ,
    K_VALID_DATE ,
    GROUP_CODE ,
    APPROVE_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    N0_MONTH ,
    N0_ADJ_MONTH ,
    N1_CYCLE ,
    MCODE_CURR ,
    N2_CYCLE ,
    MCODE_MIN ,
    N3_CYCLE ,
    PRT_UP_FLAG ,
    PRT_DOWN_FLAG ,
    N4_CYCLE ,
    A_PUR_LO ,
    A_SCALE ,
    A_RATE ,
    A_RATE_FIX ,
    A_ADJ_MIN ,
    A_ADJ_MAX ,
    B_PUR_HI ,
    B_SCALE ,
    B_RATE ,
    B_RATE_FIX ,
    B_ADJ_MIN ,
    B_ADJ_MAX ,
    N5_CYCLE ,
    C_RC_LO ,
    C_RC_HI ,
    C_SCALE ,
    C_RATE ,
    C_RATE_FIX ,
    C_ADJ_MIN ,
    C_ADJ_MAX ,
    D_RATE ,
    D_ADJ_MIN ,
    D_ADJ_MAX ,
    UPGRADE_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PARAM_TYPE ,
NEW.ACCT_TYPE ,
NEW.PARAM_NO ,
NEW.VALID_DATE ,
NEW.GROUP_CODE ,
NEW.APPROVE_FLAG ,
NEW.EXEC_MODE ,
NEW.EXEC_DAY ,
NEW.EXEC_CYCLE_NDAY ,
NEW.N0_MONTH ,
NEW.N0_ADJ_MONTH ,
NEW.N1_CYCLE ,
NEW.MCODE_CURR ,
NEW.N2_CYCLE ,
NEW.MCODE_MIN ,
NEW.N3_CYCLE ,
NEW.PRT_UP_FLAG ,
NEW.PRT_DOWN_FLAG ,
NEW.N4_CYCLE ,
NEW.A_PUR_LO ,
NEW.A_SCALE ,
NEW.A_RATE ,
NEW.A_RATE_FIX ,
NEW.A_ADJ_MIN ,
NEW.A_ADJ_MAX ,
NEW.B_PUR_HI ,
NEW.B_SCALE ,
NEW.B_RATE ,
NEW.B_RATE_FIX ,
NEW.B_ADJ_MIN ,
NEW.B_ADJ_MAX ,
NEW.N5_CYCLE ,
NEW.C_RC_LO ,
NEW.C_RC_HI ,
NEW.C_SCALE ,
NEW.C_RATE ,
NEW.C_RATE_FIX ,
NEW.C_ADJ_MIN ,
NEW.C_ADJ_MAX ,
NEW.D_RATE ,
NEW.D_ADJ_MIN ,
NEW.D_ADJ_MAX ,
NEW.UPGRADE_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BANKCODE_A AFTER
INSERT
    ON
    PTR_BANKCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_BANKCODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    BC_BANKCODE ,
    BC_ABNAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BC_BANKCODE ,
NEW.BC_ABNAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BANKCODE_D AFTER
DELETE
    ON
    PTR_BANKCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_BANKCODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    BC_BANKCODE ,
    BC_ABNAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.BC_BANKCODE ,
OLD.BC_ABNAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BANKCODE_U AFTER
UPDATE
    OF BC_ABNAME,
    BC_BANKCODE,
    MOD_PGM,
    MOD_USER ON
    PTR_BANKCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_BANKCODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    BC_BANKCODE ,
    BC_ABNAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BC_BANKCODE ,
NEW.BC_ABNAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BILLMSG_A AFTER
INSERT
    ON
    PTR_BILLMSG REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_BILLMSG( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_MSG_MONTH ,
    K_MSG_TYPE ,
    PARAM1 ,
    PARAM2 ,
    PARAM3 ,
    PARAM4 ,
    PARAM5 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.MSG_MONTH ,
NEW.MSG_TYPE ,
NEW.PARAM1 ,
NEW.PARAM2 ,
NEW.PARAM3 ,
NEW.PARAM4 ,
NEW.PARAM5 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BILLMSG_D AFTER
DELETE
    ON
    PTR_BILLMSG REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_BILLMSG( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_MSG_MONTH ,
    K_MSG_TYPE ,
    PARAM1 ,
    PARAM2 ,
    PARAM3 ,
    PARAM4 ,
    PARAM5 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.MSG_MONTH ,
OLD.MSG_TYPE ,
OLD.PARAM1 ,
OLD.PARAM2 ,
OLD.PARAM3 ,
OLD.PARAM4 ,
OLD.PARAM5 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BILLMSG_U AFTER
UPDATE
    OF PARAM1 ,
    PARAM2 ,
    PARAM3 ,
    PARAM4 ,
    PARAM5 ON
    PTR_BILLMSG REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_BILLMSG( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_MSG_MONTH ,
    K_MSG_TYPE ,
    PARAM1 ,
    PARAM2 ,
    PARAM3 ,
    PARAM4 ,
    PARAM5 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.MSG_MONTH ,
NEW.MSG_TYPE ,
NEW.PARAM1 ,
NEW.PARAM2 ,
NEW.PARAM3 ,
NEW.PARAM4 ,
NEW.PARAM5 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_BILLTYPE_A AFTER
INSERT
    ON
    PTR_BILLTYPE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_BILLTYPE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BILL_TYPE ,
    K_TXN_CODE ,
    ACCT_CODE ,
    ACCT_ITEM ,
    FEES_STATE ,
    FEES_FIX_AMT ,
    FEES_PERCENT ,
    FEES_MIN ,
    FEES_MAX ,
    FEES_BILL_TYPE ,
    FEES_TXN_CODE ,
    INTER_DESC ,
    EXTER_DESC ,
    INTEREST_MODE ,
    ADV_WKDAY ,
    BALANCE_STATE ,
    CASH_ADV_STATE ,
    ENTRY_ACCT ,
    CHK_ERR_BILL ,
    DOUBLE_CHK ,
    FORMAT_CHK ,
    MERCH_FEE ,
    BLOCK_RSN_X100 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BILL_TYPE ,
NEW.TXN_CODE ,
NEW.ACCT_CODE ,
NEW.ACCT_ITEM ,
NEW.FEES_STATE ,
NEW.FEES_FIX_AMT ,
NEW.FEES_PERCENT ,
NEW.FEES_MIN ,
NEW.FEES_MAX ,
NEW.FEES_BILL_TYPE ,
NEW.FEES_TXN_CODE ,
NEW.INTER_DESC ,
NEW.EXTER_DESC ,
NEW.INTEREST_MODE ,
NEW.ADV_WKDAY ,
NEW.BALANCE_STATE ,
NEW.CASH_ADV_STATE ,
NEW.ENTRY_ACCT ,
NEW.CHK_ERR_BILL ,
NEW.DOUBLE_CHK ,
NEW.FORMAT_CHK ,
NEW.MERCH_FEE ,
NEW.BLOCK_RSN_X100 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_BILLTYPE_D AFTER
DELETE
    ON
    PTR_BILLTYPE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_BILLTYPE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BILL_TYPE ,
    K_TXN_CODE ,
    ACCT_CODE ,
    ACCT_ITEM ,
    FEES_STATE ,
    FEES_FIX_AMT ,
    FEES_PERCENT ,
    FEES_MIN ,
    FEES_MAX ,
    FEES_BILL_TYPE ,
    FEES_TXN_CODE ,
    INTER_DESC ,
    EXTER_DESC ,
    INTEREST_MODE ,
    ADV_WKDAY ,
    BALANCE_STATE ,
    CASH_ADV_STATE ,
    ENTRY_ACCT ,
    CHK_ERR_BILL ,
    DOUBLE_CHK ,
    FORMAT_CHK ,
    MERCH_FEE ,
    BLOCK_RSN_X100 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.BILL_TYPE ,
OLD.TXN_CODE ,
OLD.ACCT_CODE ,
OLD.ACCT_ITEM ,
OLD.FEES_STATE ,
OLD.FEES_FIX_AMT ,
OLD.FEES_PERCENT ,
OLD.FEES_MIN ,
OLD.FEES_MAX ,
OLD.FEES_BILL_TYPE ,
OLD.FEES_TXN_CODE ,
OLD.INTER_DESC ,
OLD.EXTER_DESC ,
OLD.INTEREST_MODE ,
OLD.ADV_WKDAY ,
OLD.BALANCE_STATE ,
OLD.CASH_ADV_STATE ,
OLD.ENTRY_ACCT ,
OLD.CHK_ERR_BILL ,
OLD.DOUBLE_CHK ,
OLD.FORMAT_CHK ,
OLD.MERCH_FEE ,
OLD.BLOCK_RSN_X100 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_BILLTYPE_U AFTER
UPDATE
    OF ACCT_CODE,
    ACCT_ITEM,
    ADV_WKDAY,
    BALANCE_STATE,
    BILL_TYPE,
    BLOCK_RSN_X100,
    CASH_ADV_STATE,
    CHK_ERR_BILL,
    DOUBLE_CHK,
    ENTRY_ACCT,
    EXTER_DESC,
    FEES_BILL_TYPE,
    FEES_FIX_AMT,
    FEES_MAX,
    FEES_MIN,
    FEES_PERCENT,
    FEES_STATE,
    FEES_TXN_CODE,
    FORMAT_CHK,
    INTEREST_MODE,
    INTER_DESC,
    MERCH_FEE,
    MOD_PGM,
    MOD_USER,
    TXN_CODE ON
    PTR_BILLTYPE REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_BILLTYPE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BILL_TYPE ,
    K_TXN_CODE ,
    ACCT_CODE ,
    ACCT_ITEM ,
    FEES_STATE ,
    FEES_FIX_AMT ,
    FEES_PERCENT ,
    FEES_MIN ,
    FEES_MAX ,
    FEES_BILL_TYPE ,
    FEES_TXN_CODE ,
    INTER_DESC ,
    EXTER_DESC ,
    INTEREST_MODE ,
    ADV_WKDAY ,
    BALANCE_STATE ,
    CASH_ADV_STATE ,
    ENTRY_ACCT ,
    CHK_ERR_BILL ,
    DOUBLE_CHK ,
    FORMAT_CHK ,
    MERCH_FEE ,
    BLOCK_RSN_X100 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BILL_TYPE ,
NEW.TXN_CODE ,
NEW.ACCT_CODE ,
NEW.ACCT_ITEM ,
NEW.FEES_STATE ,
NEW.FEES_FIX_AMT ,
NEW.FEES_PERCENT ,
NEW.FEES_MIN ,
NEW.FEES_MAX ,
NEW.FEES_BILL_TYPE ,
NEW.FEES_TXN_CODE ,
NEW.INTER_DESC ,
NEW.EXTER_DESC ,
NEW.INTEREST_MODE ,
NEW.ADV_WKDAY ,
NEW.BALANCE_STATE ,
NEW.CASH_ADV_STATE ,
NEW.ENTRY_ACCT ,
NEW.CHK_ERR_BILL ,
NEW.DOUBLE_CHK ,
NEW.FORMAT_CHK ,
NEW.MERCH_FEE ,
NEW.BLOCK_RSN_X100 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BILLUNIT_A AFTER
INSERT
    ON
    PTR_BILLUNIT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_BILLUNIT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BILL_UNIT ,
    SHORT_TITLE ,
    CONF_FLAG ,
    DESCRIBE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BILL_UNIT ,
NEW.SHORT_TITLE ,
NEW.CONF_FLAG ,
NEW.DESCRIBE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BILLUNIT_D AFTER
DELETE
    ON
    PTR_BILLUNIT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_BILLUNIT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BILL_UNIT ,
    SHORT_TITLE ,
    CONF_FLAG ,
    DESCRIBE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.BILL_UNIT ,
OLD.SHORT_TITLE ,
OLD.CONF_FLAG ,
OLD.DESCRIBE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BILLUNIT_U AFTER
UPDATE
    OF BILL_UNIT,
    CONF_FLAG,
    DESCRIBE,
    SHORT_TITLE ON
    PTR_BILLUNIT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_BILLUNIT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BILL_UNIT ,
    SHORT_TITLE ,
    CONF_FLAG ,
    DESCRIBE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BILL_UNIT ,
NEW.SHORT_TITLE ,
NEW.CONF_FLAG ,
NEW.DESCRIBE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_BINTABLE_A AFTER
INSERT
    ON
    PTR_BINTABLE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
DECLARE wk_audcode VARCHAR(1);--
--
--
DECLARE wk_user VARCHAR(10);--
--
--
DECLARE wk_pgm VARCHAR(20);--
--
--
DECLARE wk_ws VARCHAR(20);--
--
--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
RETURN;--
--
END;--
--
--
 SET
wk_audcode = 'A';--
--

--
 INSERT
    INTO
    LOG_PTR_BINTABLE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BIN_NO
    --  ,CNTRY_CODE
    --  ,CURR_CODE
,
    BIN_TYPE ,
    CARD_DESC ,
    ICA_NO )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BIN_NO
--  ,NEW.CNTRY_CODE
--  ,NEW.CURR_CODE
,
NEW.BIN_TYPE ,
NEW.CARD_DESC ,
NEW.ICA_NO );--
--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_BINTABLE_D AFTER
DELETE
    ON
    PTR_BINTABLE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
DECLARE wk_audcode VARCHAR(1);--
--
--
DECLARE wk_user VARCHAR(10);--
--
--
DECLARE wk_pgm VARCHAR(20);--
--
--
DECLARE wk_ws VARCHAR(20);--
--
--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
RETURN;--
--
END;--
--
--
 SET
wk_audcode = 'D';--
--

--
 INSERT
    INTO
    LOG_PTR_BINTABLE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BIN_NO
    -- ,CNTRY_CODE
    -- ,CURR_CODE
,
    BIN_TYPE ,
    CARD_DESC ,
    ICA_NO )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.BIN_NO
-- ,OLD.CNTRY_CODE
-- ,OLD.CURR_CODE
,
OLD.BIN_TYPE ,
OLD.CARD_DESC ,
OLD.ICA_NO );--
--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_BINTABLE_U AFTER
UPDATE
    OF "BIN_NO",
    "BIN_TYPE",
    "CARD_DESC",
    /*"CNTRY_CODE",*/
    /*"CURR_CODE",*/
    "ICA_NO",
    "MOD_PGM",
    "MOD_USER" ON
    PTR_BINTABLE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
DECLARE wk_audcode VARCHAR(1);--
--
--
DECLARE wk_user VARCHAR(10);--
--
--
DECLARE wk_pgm VARCHAR(20);--
--
--
DECLARE wk_ws VARCHAR(20);--
--
--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
RETURN;--
--
END;--
--
--
 SET
wk_audcode = 'U';--
--

--
 INSERT
    INTO
    LOG_PTR_BINTABLE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BIN_NO
    --  ,CNTRY_CODE
    --  ,CURR_CODE
,
    BIN_TYPE ,
    CARD_DESC ,
    ICA_NO )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BIN_NO
--  ,NEW.CNTRY_CODE
--  ,NEW.CURR_CODE
,
NEW.BIN_TYPE ,
NEW.CARD_DESC ,
NEW.ICA_NO );--
--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BLOCKPARAM_A AFTER
INSERT
    ON
    PTR_BLOCKPARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_BLOCKPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_VALID_DATE ,
    APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE1 ,
    DEBT_AMT1 ,
    MCODE_VALUE2 ,
    DEBT_AMT2 ,
    MCODE_VALUE3 ,
    DEBT_AMT3 ,
    MCODE_VALUE4 ,
    DEBT_AMT4 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PARAM_TYPE ,
NEW.ACCT_TYPE ,
NEW.VALID_DATE ,
NEW.APR_FLAG ,
NEW.EXEC_MODE ,
NEW.EXEC_DAY ,
NEW.EXEC_CYCLE_NDAY ,
NEW.EXEC_DATE ,
NEW.N0_MONTH ,
NEW.N1_CYCLE ,
NEW.MCODE_VALUE1 ,
NEW.DEBT_AMT1 ,
NEW.MCODE_VALUE2 ,
NEW.DEBT_AMT2 ,
NEW.MCODE_VALUE3 ,
NEW.DEBT_AMT3 ,
NEW.MCODE_VALUE4 ,
NEW.DEBT_AMT4 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BLOCKPARAM_D AFTER
DELETE
    ON
    PTR_BLOCKPARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_BLOCKPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_VALID_DATE ,
    APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE1 ,
    DEBT_AMT1 ,
    MCODE_VALUE2 ,
    DEBT_AMT2 ,
    MCODE_VALUE3 ,
    DEBT_AMT3 ,
    MCODE_VALUE4 ,
    DEBT_AMT4 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.PARAM_TYPE ,
OLD.ACCT_TYPE ,
OLD.VALID_DATE ,
OLD.APR_FLAG ,
OLD.EXEC_MODE ,
OLD.EXEC_DAY ,
OLD.EXEC_CYCLE_NDAY ,
OLD.EXEC_DATE ,
OLD.N0_MONTH ,
OLD.N1_CYCLE ,
OLD.MCODE_VALUE1 ,
OLD.DEBT_AMT1 ,
OLD.MCODE_VALUE2 ,
OLD.DEBT_AMT2 ,
OLD.MCODE_VALUE3 ,
OLD.DEBT_AMT3 ,
OLD.MCODE_VALUE4 ,
OLD.DEBT_AMT4 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BLOCKPARAM_U AFTER
UPDATE
    OF APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE1 ,
    DEBT_AMT1 ,
    MCODE_VALUE2 ,
    DEBT_AMT2 ,
    MCODE_VALUE3 ,
    DEBT_AMT3 ,
    MCODE_VALUE4 ,
    DEBT_AMT4 ON
    PTR_BLOCKPARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_BLOCKPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_VALID_DATE ,
    APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE1 ,
    DEBT_AMT1 ,
    MCODE_VALUE2 ,
    DEBT_AMT2 ,
    MCODE_VALUE3 ,
    DEBT_AMT3 ,
    MCODE_VALUE4 ,
    DEBT_AMT4 )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PARAM_TYPE ,
NEW.ACCT_TYPE ,
NEW.VALID_DATE ,
NEW.APR_FLAG ,
NEW.EXEC_MODE ,
NEW.EXEC_DAY ,
NEW.EXEC_CYCLE_NDAY ,
NEW.EXEC_DATE ,
NEW.N0_MONTH ,
NEW.N1_CYCLE ,
NEW.MCODE_VALUE1 ,
NEW.DEBT_AMT1 ,
NEW.MCODE_VALUE2 ,
NEW.DEBT_AMT2 ,
NEW.MCODE_VALUE3 ,
NEW.DEBT_AMT3 ,
NEW.MCODE_VALUE4 ,
NEW.DEBT_AMT4 );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BUSINDAY_A AFTER
INSERT
    ON
    PTR_BUSINDAY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_BUSINDAY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    BUSINESS_DATE ,
    ONLINE_DATE ,
    VOUCH_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BUSINESS_DATE ,
NEW.ONLINE_DATE ,
NEW.VOUCH_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BUSINDAY_D AFTER
DELETE
    ON
    PTR_BUSINDAY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_BUSINDAY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    BUSINESS_DATE ,
    ONLINE_DATE ,
    VOUCH_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.BUSINESS_DATE ,
OLD.ONLINE_DATE ,
OLD.VOUCH_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_BUSINDAY_U AFTER
UPDATE
    OF BUSINESS_DATE ,
    ONLINE_DATE ,
    VOUCH_DATE ON
    PTR_BUSINDAY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_BUSINDAY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    BUSINESS_DATE ,
    ONLINE_DATE ,
    VOUCH_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BUSINESS_DATE ,
NEW.ONLINE_DATE ,
NEW.VOUCH_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CARD_TYPE_A AFTER
INSERT
    ON
    PTR_CARD_TYPE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_CARD_TYPE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_TYPE ,
    NAME
    -- ,BIN_NO
    -- ,PM_NCCC_NO1
    -- ,PM_NCCC_NO2
    -- ,SUP_NCCC_NO1
    -- ,SUP_NCCC_NO2
,
    CARD_NOTE
    -- ,SERVICE_CODE
)
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_TYPE ,
NEW.NAME
--,NEW.BIN_NO
--,NEW.PM_NCCC_NO1
--,NEW.PM_NCCC_NO2
--,NEW.SUP_NCCC_NO1
--,NEW.SUP_NCCC_NO2
,
NEW.CARD_NOTE
--,NEW.SERVICE_CODE
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CARD_TYPE_D AFTER
DELETE
    ON
    PTR_CARD_TYPE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_CARD_TYPE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_TYPE ,
    NAME
    --,BIN_NO
    --,PM_NCCC_NO1
    --,PM_NCCC_NO2
    --,SUP_NCCC_NO1
    --,SUP_NCCC_NO2
,
    CARD_NOTE
    --,SERVICE_CODE
)
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.CARD_TYPE ,
OLD.NAME
--,OLD.BIN_NO
--,OLD.PM_NCCC_NO1
--,OLD.PM_NCCC_NO2
--,OLD.SUP_NCCC_NO1
--,OLD.SUP_NCCC_NO2
,
OLD.CARD_NOTE
--,OLD.SERVICE_CODE
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CARD_TYPE_U AFTER
UPDATE
    OF /*BIN_NO,*/
    CARD_NOTE,
    CARD_TYPE,
    MOD_PGM,
    MOD_USER,
    NAME /*,PM_NCCC_NO1, PM_NCCC_NO2, SERVICE_CODE,
    SUP_NCCC_NO1, SUP_NCCC_NO2*/
    ON
    PTR_CARD_TYPE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_CARD_TYPE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_TYPE ,
    NAME
    -- ,BIN_NO
    -- ,PM_NCCC_NO1
    -- ,PM_NCCC_NO2
    -- ,SUP_NCCC_NO1
    -- ,SUP_NCCC_NO2
,
    CARD_NOTE
    -- ,SERVICE_CODE
)
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_TYPE ,
NEW.NAME
--,NEW.BIN_NO
--,NEW.PM_NCCC_NO1
--,NEW.PM_NCCC_NO2
--,NEW.SUP_NCCC_NO1
--,NEW.SUP_NCCC_NO2
,
NEW.CARD_NOTE
--,NEW.SERVICE_CODE
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CLASS_A AFTER
INSERT
    ON
    PTR_CLASS REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_CLASS( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    BEG_CREDIT_LMT ,
    END_CREDIT_LMT ,
    CLASS_CODE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BEG_CREDIT_LMT ,
NEW.END_CREDIT_LMT ,
NEW.CLASS_CODE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CLASS_D AFTER
DELETE
    ON
    PTR_CLASS REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_CLASS( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    BEG_CREDIT_LMT ,
    END_CREDIT_LMT ,
    CLASS_CODE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.BEG_CREDIT_LMT ,
OLD.END_CREDIT_LMT ,
OLD.CLASS_CODE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CLASS_U AFTER
UPDATE
    OF BEG_CREDIT_LMT,
    CLASS_CODE,
    END_CREDIT_LMT,
    MOD_PGM,
    MOD_USER ON
    PTR_CLASS REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_CLASS( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    BEG_CREDIT_LMT ,
    END_CREDIT_LMT ,
    CLASS_CODE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BEG_CREDIT_LMT ,
NEW.END_CREDIT_LMT ,
NEW.CLASS_CODE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CORP_FEE_A AFTER
INSERT
    ON
    PTR_CORP_FEE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_CORP_FEE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CORP_P_SEQNO ,
    K_CARD_TYPE ,
    CORP_NO
    --,CORP_NO_CODE
,
    FIRST_FEE_AMT ,
    OTHER_FEE_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CORP_P_SEQNO ,
NEW.CARD_TYPE ,
NEW.CORP_NO
--,NEW.CORP_NO_CODE
,
NEW.FIRST_FEE_AMT ,
NEW.OTHER_FEE_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CORP_FEE_D AFTER
DELETE
    ON
    PTR_CORP_FEE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_CORP_FEE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CORP_P_SEQNO ,
    K_CARD_TYPE ,
    CORP_NO
    --,CORP_NO_CODE
,
    FIRST_FEE_AMT ,
    OTHER_FEE_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.CORP_P_SEQNO ,
OLD.CARD_TYPE ,
OLD.CORP_NO
--,OLD.CORP_NO_CODE
,
OLD.FIRST_FEE_AMT ,
OLD.OTHER_FEE_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CORP_FEE_U AFTER
UPDATE
    OF CORP_NO /*,CORP_NO_CODE*/
    ,
    FIRST_FEE_AMT ,
    OTHER_FEE_AMT ON
    PTR_CORP_FEE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_CORP_FEE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CORP_P_SEQNO ,
    K_CARD_TYPE ,
    CORP_NO
    --,CORP_NO_CODE
,
    FIRST_FEE_AMT ,
    OTHER_FEE_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CORP_P_SEQNO ,
NEW.CARD_TYPE ,
NEW.CORP_NO
--,NEW.CORP_NO_CODE
,
NEW.FIRST_FEE_AMT ,
NEW.OTHER_FEE_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CURRCODE_A AFTER
INSERT
    ON
    PTR_CURRCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_CURRCODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CURR_CODE ,
    CURR_ENG_NAME ,
    CURR_CHI_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CURR_CODE ,
NEW.CURR_ENG_NAME ,
NEW.CURR_CHI_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CURRCODE_D AFTER
DELETE
    ON
    PTR_CURRCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_CURRCODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CURR_CODE ,
    CURR_ENG_NAME ,
    CURR_CHI_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.CURR_CODE ,
OLD.CURR_ENG_NAME ,
OLD.CURR_CHI_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_CURRCODE_U AFTER
UPDATE
    OF CURR_ENG_NAME ,
    CURR_CHI_NAME ON
    PTR_CURRCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_CURRCODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CURR_CODE ,
    CURR_ENG_NAME ,
    CURR_CHI_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CURR_CODE ,
NEW.CURR_ENG_NAME ,
NEW.CURR_CHI_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_LOG_PTR_DEPT_CODE
AFTER DELETE OR INSERT OR UPDATE OF
"DEPT_NAME"
,"DESCRIBE"
,"GL_CODE"
,"GL_CODE2"
ON PTR_DEPT_CODE
	REFERENCING NEW AS NEW OLD AS OLD 
    FOR EACH ROW MODE DB2SQL

BEGIN
DECLARE wk_audcode      VARCHAR(1);--
DECLARE wk_user         VARCHAR(10);--
DECLARE wk_pgm          VARCHAR(20);--
DECLARE wk_ws           VARCHAR(20);--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

  IF INSERTING THEN
       set wk_audcode = 'A';--
  ELSEIF UPDATING THEN
       set wk_audcode = 'U';--
  ELSE
       set wk_audcode   = 'D';--
  END IF;--
  if INSERTING or UPDATING then
    INSERT INTO LOG_PTR_DEPT_CODE(
                MOD_TIME
                ,MOD_PGM
                ,MOD_USER
                ,MOD_AUDCODE
                ,K_DEPT_CODE
                ,DEPT_NAME
                ,DESCRIBE
                ,GL_CODE
                ,GL_CODE2
           )
    VALUES (
                to_char(sysdate,'yyyymmddhh24misssss')
                ,NEW.MOD_PGM
                ,NEW.MOD_USER
                ,wk_audcode
                ,NEW.DEPT_CODE
                ,NEW.DEPT_NAME
                ,NEW.DESCRIBE
                ,NEW.GL_CODE
                ,NEW.GL_CODE2
           );--
  else
    INSERT INTO LOG_PTR_DEPT_CODE(
                MOD_TIME
                ,MOD_PGM
                ,MOD_USER
                ,MOD_AUDCODE
                ,K_DEPT_CODE
                ,DEPT_NAME
                ,DESCRIBE
                ,GL_CODE
                ,GL_CODE2
           )
    VALUES (
                to_char(sysdate,'yyyymmddhh24misssss')
                ,wk_pgm
                ,wk_user
                ,wk_audcode
                ,OLD.DEPT_CODE
                ,OLD.DEPT_NAME
                ,OLD.DESCRIBE
                ,OLD.GL_CODE
                ,OLD.GL_CODE2
           );--
  end if;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_GROUP_CARD_A AFTER
INSERT
    ON
    PTR_GROUP_CARD REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_GROUP_CARD( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    K_CARD_TYPE ,
    SPEC_FLAG ,
    NAME
    -- ,MC_CARD_TYPE
,
    ORG_CARDNO_FLAG ,
    FIRST_FEE ,
    OTHER_FEE ,
    SUP_RATE ,
    SUP_END_MONTH ,
    SUP_END_RATE ,
    REMARK /*  ,UNIT_CODE
                ,UNIT_CODE2
                ,UNIT_CODE3
                ,UNIT_CODE4
                ,UNIT_CODE5
                ,UNIT_CODE6
                ,UNIT_CODE7
                ,UNIT_CODE8
                ,UNIT_CODE9
                ,UNIT_CODE10
                ,UNIT_CODE11
                ,UNIT_CODE12
                ,UNIT_CODE13
                ,UNIT_CODE14
                ,CASH_LIMIT_RATE
                ,UPGRADE_FLAG*/
    )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.CARD_TYPE ,
NEW.SPEC_FLAG ,
NEW.NAME
-- ,NEW.MC_CARD_TYPE
,
NEW.ORG_CARDNO_FLAG ,
NEW.FIRST_FEE ,
NEW.OTHER_FEE ,
NEW.SUP_RATE ,
NEW.SUP_END_MONTH ,
NEW.SUP_END_RATE ,
NEW.REMARK /*,NEW.UNIT_CODE
                ,NEW.UNIT_CODE2
                ,NEW.UNIT_CODE3
                ,NEW.UNIT_CODE4
                ,NEW.UNIT_CODE5
                ,NEW.UNIT_CODE6
                ,NEW.UNIT_CODE7
                ,NEW.UNIT_CODE8
                ,NEW.UNIT_CODE9
                ,NEW.UNIT_CODE10
                ,NEW.UNIT_CODE11
                ,NEW.UNIT_CODE12
                ,NEW.UNIT_CODE13
                ,NEW.UNIT_CODE14
                ,NEW.CASH_LIMIT_RATE
                ,NEW.UPGRADE_FLAG*/
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_GROUP_CARD_D AFTER
DELETE
    ON
    PTR_GROUP_CARD REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_GROUP_CARD( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    K_CARD_TYPE ,
    SPEC_FLAG ,
    NAME
    -- ,MC_CARD_TYPE
,
    ORG_CARDNO_FLAG ,
    FIRST_FEE ,
    OTHER_FEE ,
    SUP_RATE ,
    SUP_END_MONTH ,
    SUP_END_RATE ,
    REMARK /* ,UNIT_CODE
                ,UNIT_CODE2
                ,UNIT_CODE3
                ,UNIT_CODE4
                ,UNIT_CODE5
                ,UNIT_CODE6
                ,UNIT_CODE7
                ,UNIT_CODE8
                ,UNIT_CODE9
                ,UNIT_CODE10
                ,UNIT_CODE11
                ,UNIT_CODE12
                ,UNIT_CODE13
                ,UNIT_CODE14
                ,CASH_LIMIT_RATE
                ,UPGRADE_FLAG*/
    )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.GROUP_CODE ,
OLD.CARD_TYPE ,
OLD.SPEC_FLAG ,
OLD.NAME
-- ,OLD.MC_CARD_TYPE
,
OLD.ORG_CARDNO_FLAG ,
OLD.FIRST_FEE ,
OLD.OTHER_FEE ,
OLD.SUP_RATE ,
OLD.SUP_END_MONTH ,
OLD.SUP_END_RATE ,
OLD.REMARK /* ,OLD.UNIT_CODE
                ,OLD.UNIT_CODE2
                ,OLD.UNIT_CODE3
                ,OLD.UNIT_CODE4
                ,OLD.UNIT_CODE5
                ,OLD.UNIT_CODE6
                ,OLD.UNIT_CODE7
                ,OLD.UNIT_CODE8
                ,OLD.UNIT_CODE9
                ,OLD.UNIT_CODE10
                ,OLD.UNIT_CODE11
                ,OLD.UNIT_CODE12
                ,OLD.UNIT_CODE13
                ,OLD.UNIT_CODE14
                ,OLD.CASH_LIMIT_RATE
                ,OLD.UPGRADE_FLAG*/
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_GROUP_CARD_U AFTER
UPDATE
    OF CARD_TYPE,
    CASH_LIMIT_RATE,
    FIRST_FEE,
    GROUP_CODE,
    /*MC_CARD_TYPE, */
    MOD_PGM,
    MOD_USER,
    NAME,
    ORG_CARDNO_FLAG,
    OTHER_FEE,
    REMARK,
    SPEC_FLAG,
    SUP_END_MONTH,
    SUP_END_RATE,
    SUP_RATE /*, UNIT_CODE,
    UNIT_CODE10, UNIT_CODE11, UNIT_CODE12, UNIT_CODE13,
    UNIT_CODE14, UNIT_CODE2, UNIT_CODE3, UNIT_CODE4,
    UNIT_CODE5, UNIT_CODE6, UNIT_CODE7, UNIT_CODE8,
    UNIT_CODE9, UPGRADE_FLAG*/
    ON
    PTR_GROUP_CARD REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_GROUP_CARD( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    K_CARD_TYPE ,
    SPEC_FLAG ,
    NAME
    -- ,MC_CARD_TYPE
,
    ORG_CARDNO_FLAG ,
    FIRST_FEE ,
    OTHER_FEE ,
    SUP_RATE ,
    SUP_END_MONTH ,
    SUP_END_RATE ,
    REMARK /*  ,UNIT_CODE
                ,UNIT_CODE2
                ,UNIT_CODE3
                ,UNIT_CODE4
                ,UNIT_CODE5
                ,UNIT_CODE6
                ,UNIT_CODE7
                ,UNIT_CODE8
                ,UNIT_CODE9
                ,UNIT_CODE10
                ,UNIT_CODE11
                ,UNIT_CODE12
                ,UNIT_CODE13
                ,UNIT_CODE14
                ,CASH_LIMIT_RATE
                ,UPGRADE_FLAG*/
    )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.CARD_TYPE ,
NEW.SPEC_FLAG ,
NEW.NAME
-- ,NEW.MC_CARD_TYPE
,
NEW.ORG_CARDNO_FLAG ,
NEW.FIRST_FEE ,
NEW.OTHER_FEE ,
NEW.SUP_RATE ,
NEW.SUP_END_MONTH ,
NEW.SUP_END_RATE ,
NEW.REMARK /*,NEW.UNIT_CODE
                ,NEW.UNIT_CODE2
                ,NEW.UNIT_CODE3
                ,NEW.UNIT_CODE4
                ,NEW.UNIT_CODE5
                ,NEW.UNIT_CODE6
                ,NEW.UNIT_CODE7
                ,NEW.UNIT_CODE8
                ,NEW.UNIT_CODE9
                ,NEW.UNIT_CODE10
                ,NEW.UNIT_CODE11
                ,NEW.UNIT_CODE12
                ,NEW.UNIT_CODE13
                ,NEW.UNIT_CODE14
                ,NEW.CASH_LIMIT_RATE
                ,NEW.UPGRADE_FLAG*/
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_GROUP_CODE_A AFTER
INSERT
    ON
    PTR_GROUP_CODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_GROUP_CODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    GROUP_ABBR_CODE ,
    GROUP_NAME ,
    EMBOSS_DATA
    --,MEMBER_FLAG
,
    CO_MEMBER_FLAG ,
    GROUP_ORDER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.GROUP_ABBR_CODE ,
NEW.GROUP_NAME ,
NEW.EMBOSS_DATA
-- ,NEW.MEMBER_FLAG
,
NEW.CO_MEMBER_FLAG ,
NEW.GROUP_ORDER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_GROUP_CODE_D AFTER
DELETE
    ON
    PTR_GROUP_CODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_GROUP_CODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    GROUP_ABBR_CODE ,
    GROUP_NAME ,
    EMBOSS_DATA
    --,MEMBER_FLAG
,
    CO_MEMBER_FLAG ,
    GROUP_ORDER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.GROUP_CODE ,
OLD.GROUP_ABBR_CODE ,
OLD.GROUP_NAME ,
OLD.EMBOSS_DATA
--,OLD.MEMBER_FLAG
,
OLD.CO_MEMBER_FLAG ,
OLD.GROUP_ORDER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_GROUP_CODE_U AFTER
UPDATE
    OF CO_MEMBER_FLAG,
    EMBOSS_DATA,
    GROUP_ABBR_CODE,
    GROUP_CODE,
    GROUP_NAME,
    GROUP_ORDER,
    /* MEMBER_FLAG,*/
    MOD_PGM,
    MOD_USER ON
    PTR_GROUP_CODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_GROUP_CODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_GROUP_CODE ,
    GROUP_ABBR_CODE ,
    GROUP_NAME ,
    EMBOSS_DATA
    --,MEMBER_FLAG
,
    CO_MEMBER_FLAG ,
    GROUP_ORDER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.GROUP_CODE ,
NEW.GROUP_ABBR_CODE ,
NEW.GROUP_NAME ,
NEW.EMBOSS_DATA
-- ,NEW.MEMBER_FLAG
,
NEW.CO_MEMBER_FLAG ,
NEW.GROUP_ORDER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_HOLIDAY_A AFTER
INSERT
    ON
    PTR_HOLIDAY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_HOLIDAY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_HOLIDAY ,
    HOLIDAY )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.HOLIDAY ,
NEW.HOLIDAY );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_HOLIDAY_D AFTER
DELETE
    ON
    PTR_HOLIDAY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_HOLIDAY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_HOLIDAY ,
    HOLIDAY )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.HOLIDAY ,
OLD.HOLIDAY );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_HOLIDAY_U AFTER
DELETE
    OR
INSERT
    OR
UPDATE
    OF HOLIDAY ON
    PTR_HOLIDAY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_HOLIDAY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_HOLIDAY ,
    HOLIDAY )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.HOLIDAY ,
NEW.HOLIDAY );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER "ECSCRDB"."TR_LOG_PTR_HSM_KEYS_A"
  AFTER DELETE
  ON "ECSCRDB"."PTR_HSM_KEYS"
  REFERENCING 
    OLD AS OLD
    NEW AS NEW
  FOR EACH ROW
BEGIN
 DECLARE wk_audcode   VARCHAR (1);                                        --
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_HSM_KEYS( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    HSM_KEYS_ORG ,
    VISA_PVKA ,
    VISA_PVKA_CHK ,
    VISA_PVKB ,
    VISA_PVKB_CHK ,
    MASTER_PVKA ,
    MASTER_PVKA_CHK ,
    MASTER_PVKB ,
    MASTER_PVKB_CHK ,
    JCB_PVKA ,
    JCB_PVKA_CHK ,
    JCB_PVKB ,
    JCB_PVKB_CHK ,
    VISA_CVKA ,
    VISA_CVKA_CHK ,
    VISA_CVKB ,
    VISA_CVKB_CHK ,
    MASTER_CVKA ,
    MASTER_CVKA_CHK ,
    MASTER_CVKB ,
    MASTER_CVKB_CHK ,
    JCB_CVKA ,
    JCB_CVKA_CHK ,
    JCB_CVKB ,
    JCB_CVKB_CHK ,
    VISA_MDK ,
    VISA_MDK_CHK ,
    MASTER_MDK ,
    MASTER_MDK_CHK ,
    JCB_MDK ,
    JCB_MDK_CHK ,
    NET_ZMK ,
    NET_ZMK_CHK ,
    NET_ZPK ,
    NET_ZPK_CHK ,
    ATM_ZMK ,
    ATM_ZMK_CHK ,
    ATM_ZPK ,
    ATM_ZPK_CHK ,
    ECS_CSCK1 ,
    MOB_KEK ,
    MOB_KEK_CHK ,
    MOB_DEK ,
    MOB_DEK_CHK ,
    ACS_KEK ,
    ACS_KEK_CHK ,
    ACS_DEK ,
    ACS_DEK_CHK ,
    EBK_ZEK ,
    EBK_ZEK_CHK ,
    EBK_DEK ,
    EBK_DEK_CHK ,
    JCB_PVKI,
    VISA_PVKI,
    MASTER_PVKI,
    REMARK
    )
VALUES ( 
sysdate ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
    NEW.HSM_KEYS_ORG ,
    NEW.VISA_PVKA ,
    NEW.VISA_PVKA_CHK ,
    NEW.VISA_PVKB ,
    NEW.VISA_PVKB_CHK ,
    NEW.MASTER_PVKA ,
    NEW.MASTER_PVKA_CHK ,
    NEW.MASTER_PVKB ,
    NEW.MASTER_PVKB_CHK ,
    NEW.JCB_PVKA ,
    NEW.JCB_PVKA_CHK ,
    NEW.JCB_PVKB ,
    NEW.JCB_PVKB_CHK ,
    NEW.VISA_CVKA ,
    NEW.VISA_CVKA_CHK ,
    NEW.VISA_CVKB ,
    NEW.VISA_CVKB_CHK ,
    NEW.MASTER_CVKA ,
    NEW.MASTER_CVKA_CHK ,
    NEW.MASTER_CVKB ,
    NEW.MASTER_CVKB_CHK ,
    NEW.JCB_CVKA ,
    NEW.JCB_CVKA_CHK ,
    NEW.JCB_CVKB ,
    NEW.JCB_CVKB_CHK ,
    NEW.VISA_MDK ,
    NEW.VISA_MDK_CHK ,
    NEW.MASTER_MDK ,
    NEW.MASTER_MDK_CHK ,
    NEW.JCB_MDK ,
    NEW.JCB_MDK_CHK ,
    NEW.NET_ZMK ,
    NEW.NET_ZMK_CHK ,
    NEW.NET_ZPK ,
    NEW.NET_ZPK_CHK ,
    NEW.ATM_ZMK ,
    NEW.ATM_ZMK_CHK ,
    NEW.ATM_ZPK ,
    NEW.ATM_ZPK_CHK ,
    NEW.ECS_CSCK1 ,
    NEW.MOB_KEK ,
    NEW.MOB_KEK_CHK ,
    NEW.MOB_DEK ,
    NEW.MOB_DEK_CHK ,
    NEW.ACS_KEK ,
    NEW.ACS_KEK_CHK ,
    NEW.ACS_DEK ,
    NEW.ACS_DEK_CHK ,
    NEW.EBK_ZEK ,
    NEW.EBK_ZEK_CHK ,
    NEW.EBK_DEK ,
    NEW.EBK_DEK_CHK ,
    NEW.JCB_PVKI,
    NEW.VISA_PVKI,
    NEW.MASTER_PVKI,
    NEW.REMARK  );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER "ECSCRDB"."TR_LOG_PTR_HSM_KEYS_D"
  AFTER DELETE
  ON "ECSCRDB"."PTR_HSM_KEYS"
  REFERENCING 
    OLD AS OLD
    NEW AS NEW
  FOR EACH ROW
BEGIN
 DECLARE wk_audcode   VARCHAR (1);                                        --
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_HSM_KEYS( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    HSM_KEYS_ORG ,
    VISA_PVKA ,
    VISA_PVKA_CHK ,
    VISA_PVKB ,
    VISA_PVKB_CHK ,
    MASTER_PVKA ,
    MASTER_PVKA_CHK ,
    MASTER_PVKB ,
    MASTER_PVKB_CHK ,
    JCB_PVKA ,
    JCB_PVKA_CHK ,
    JCB_PVKB ,
    JCB_PVKB_CHK ,
    VISA_CVKA ,
    VISA_CVKA_CHK ,
    VISA_CVKB ,
    VISA_CVKB_CHK ,
    MASTER_CVKA ,
    MASTER_CVKA_CHK ,
    MASTER_CVKB ,
    MASTER_CVKB_CHK ,
    JCB_CVKA ,
    JCB_CVKA_CHK ,
    JCB_CVKB ,
    JCB_CVKB_CHK ,
    VISA_MDK ,
    VISA_MDK_CHK ,
    MASTER_MDK ,
    MASTER_MDK_CHK ,
    JCB_MDK ,
    JCB_MDK_CHK ,
    NET_ZMK ,
    NET_ZMK_CHK ,
    NET_ZPK ,
    NET_ZPK_CHK ,
    ATM_ZMK ,
    ATM_ZMK_CHK ,
    ATM_ZPK ,
    ATM_ZPK_CHK ,
    ECS_CSCK1 ,
    MOB_KEK ,
    MOB_KEK_CHK ,
    MOB_DEK ,
    MOB_DEK_CHK ,
    ACS_KEK ,
    ACS_KEK_CHK ,
    ACS_DEK ,
    ACS_DEK_CHK ,
    EBK_ZEK ,
    EBK_ZEK_CHK ,
    EBK_DEK ,
    EBK_DEK_CHK ,
    JCB_PVKI,
    VISA_PVKI,
    MASTER_PVKI,
    REMARK
    )
VALUES ( 
sysdate ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
    NEW.HSM_KEYS_ORG ,
    NEW.VISA_PVKA ,
    NEW.VISA_PVKA_CHK ,
    NEW.VISA_PVKB ,
    NEW.VISA_PVKB_CHK ,
    NEW.MASTER_PVKA ,
    NEW.MASTER_PVKA_CHK ,
    NEW.MASTER_PVKB ,
    NEW.MASTER_PVKB_CHK ,
    NEW.JCB_PVKA ,
    NEW.JCB_PVKA_CHK ,
    NEW.JCB_PVKB ,
    NEW.JCB_PVKB_CHK ,
    NEW.VISA_CVKA ,
    NEW.VISA_CVKA_CHK ,
    NEW.VISA_CVKB ,
    NEW.VISA_CVKB_CHK ,
    NEW.MASTER_CVKA ,
    NEW.MASTER_CVKA_CHK ,
    NEW.MASTER_CVKB ,
    NEW.MASTER_CVKB_CHK ,
    NEW.JCB_CVKA ,
    NEW.JCB_CVKA_CHK ,
    NEW.JCB_CVKB ,
    NEW.JCB_CVKB_CHK ,
    NEW.VISA_MDK ,
    NEW.VISA_MDK_CHK ,
    NEW.MASTER_MDK ,
    NEW.MASTER_MDK_CHK ,
    NEW.JCB_MDK ,
    NEW.JCB_MDK_CHK ,
    NEW.NET_ZMK ,
    NEW.NET_ZMK_CHK ,
    NEW.NET_ZPK ,
    NEW.NET_ZPK_CHK ,
    NEW.ATM_ZMK ,
    NEW.ATM_ZMK_CHK ,
    NEW.ATM_ZPK ,
    NEW.ATM_ZPK_CHK ,
    NEW.ECS_CSCK1 ,
    NEW.MOB_KEK ,
    NEW.MOB_KEK_CHK ,
    NEW.MOB_DEK ,
    NEW.MOB_DEK_CHK ,
    NEW.ACS_KEK ,
    NEW.ACS_KEK_CHK ,
    NEW.ACS_DEK ,
    NEW.ACS_DEK_CHK ,
    NEW.EBK_ZEK ,
    NEW.EBK_ZEK_CHK ,
    NEW.EBK_DEK ,
    NEW.EBK_DEK_CHK ,
    NEW.JCB_PVKI,
    NEW.VISA_PVKI,
    NEW.MASTER_PVKI,
    NEW.REMARK  );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER "ECSCRDB"."TR_LOG_PTR_HSM_KEYS_U"
  AFTER UPDATE OF "ACS_DEK", "ACS_DEK_CHK", "ACS_KEK", "ACS_KEK_CHK", "ACS_VERSION_ID", 
    "ATM_ZMK", "ATM_ZMK_CHK", "ATM_ZPK", "ATM_ZPK_CHK", "EBK_DEK", 
    "EBK_DEK_CHK", "EBK_HMACK", "EBK_ZEK", "EBK_ZEK_CHK", "ECS_CSCK1", 
    "HSM_KEYS_ORG", "JCB_CVKA", "JCB_CVKA_CHK", "JCB_CVKB", "JCB_CVKB_CHK", 
    "JCB_MDK", "JCB_MDK_CHK", "JCB_PVKA", "JCB_PVKA_CHK", "JCB_PVKB", 
    "JCB_PVKB_CHK", "MASTER_CVKA", "MASTER_CVKA_CHK", "MASTER_CVKB", "MASTER_CVKB_CHK", 
    "MASTER_MDK", "MASTER_MDK_CHK", "MASTER_PVKA", "MASTER_PVKA_CHK", "MASTER_PVKB", 
    "MASTER_PVKB_CHK", "MOB_DEK", "MOB_DEK_CHK", "MOB_KEK", "MOB_KEK_CHK", 
    "MOB_VERSION_ID", "MOD_PGM", "MOD_SEQNO", "MOD_TIME", "MOD_USER", 
    "NET_ZMK", "NET_ZMK_CHK", "NET_ZPK", "NET_ZPK_CHK", "REMARK", 
    "VISA_CVKA", "VISA_CVKA_CHK", "VISA_CVKB", "VISA_CVKB_CHK", "VISA_MDK", 
    "VISA_MDK_CHK", "VISA_PVKA", "VISA_PVKA_CHK", "VISA_PVKB", "VISA_PVKB_CHK",
    "JCB_PVKI","VISA_PVKI","MASTER_PVKI"
  ON "ECSCRDB"."PTR_HSM_KEYS"
  REFERENCING 
    OLD AS OLD
    NEW AS NEW
  FOR EACH ROW
BEGIN
 DECLARE wk_audcode VARCHAR(1);--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_HSM_KEYS( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    HSM_KEYS_ORG ,
    VISA_PVKA ,
    VISA_PVKA_CHK ,
    VISA_PVKB ,
    VISA_PVKB_CHK ,
    MASTER_PVKA ,
    MASTER_PVKA_CHK ,
    MASTER_PVKB ,
    MASTER_PVKB_CHK ,
    JCB_PVKA ,
    JCB_PVKA_CHK ,
    JCB_PVKB ,
    JCB_PVKB_CHK ,
    VISA_CVKA ,
    VISA_CVKA_CHK ,
    VISA_CVKB ,
    VISA_CVKB_CHK ,
    MASTER_CVKA ,
    MASTER_CVKA_CHK ,
    MASTER_CVKB ,
    MASTER_CVKB_CHK ,
    JCB_CVKA ,
    JCB_CVKA_CHK ,
    JCB_CVKB ,
    JCB_CVKB_CHK ,
    VISA_MDK ,
    VISA_MDK_CHK ,
    MASTER_MDK ,
    MASTER_MDK_CHK ,
    JCB_MDK ,
    JCB_MDK_CHK ,
    NET_ZMK ,
    NET_ZMK_CHK ,        
    NET_ZPK ,
    NET_ZPK_CHK ,
    ATM_ZMK ,
    ATM_ZMK_CHK ,        
    ATM_ZPK ,
    ATM_ZPK_CHK ,
    ECS_CSCK1 ,
    MOB_KEK ,
    MOB_KEK_CHK ,
    MOB_DEK ,
    MOB_DEK_CHK ,
    ACS_KEK ,
    ACS_KEK_CHK ,
    ACS_DEK ,
    ACS_DEK_CHK ,
    EBK_ZEK ,
    EBK_ZEK_CHK ,
    EBK_DEK ,
    EBK_DEK_CHK ,
    JCB_PVKI,
    VISA_PVKI,
    MASTER_PVKI,
    REMARK 
    )
VALUES (
sysdate ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
    NEW.HSM_KEYS_ORG ,
    NEW.VISA_PVKA ,
    NEW.VISA_PVKA_CHK ,
    NEW.VISA_PVKB ,
    NEW.VISA_PVKB_CHK ,
    NEW.MASTER_PVKA ,
    NEW.MASTER_PVKA_CHK ,
    NEW.MASTER_PVKB ,
    NEW.MASTER_PVKB_CHK ,
    NEW.JCB_PVKA ,
    NEW.JCB_PVKA_CHK ,
    NEW.JCB_PVKB ,
    NEW.JCB_PVKB_CHK ,
    NEW.VISA_CVKA ,
    NEW.VISA_CVKA_CHK ,
    NEW.VISA_CVKB ,
    NEW.VISA_CVKB_CHK ,
    NEW.MASTER_CVKA ,
    NEW.MASTER_CVKA_CHK ,
    NEW.MASTER_CVKB ,
    NEW.MASTER_CVKB_CHK ,
    NEW.JCB_CVKA ,
    NEW.JCB_CVKA_CHK ,
    NEW.JCB_CVKB ,
    NEW.JCB_CVKB_CHK ,
    NEW.VISA_MDK ,
    NEW.VISA_MDK_CHK ,
    NEW.MASTER_MDK ,
    NEW.MASTER_MDK_CHK ,
    NEW.JCB_MDK ,
    NEW.JCB_MDK_CHK ,
    NEW.NET_ZMK ,
    NEW.NET_ZMK_CHK ,        
    NEW.NET_ZPK ,
    NEW.NET_ZPK_CHK ,
    NEW.ATM_ZMK ,
    NEW.ATM_ZMK_CHK ,        
    NEW.ATM_ZPK ,
    NEW.ATM_ZPK_CHK ,
    NEW.ECS_CSCK1 ,
    NEW.MOB_KEK ,
    NEW.MOB_KEK_CHK ,
    NEW.MOB_DEK ,
    NEW.MOB_DEK_CHK ,
    NEW.ACS_KEK ,
    NEW.ACS_KEK_CHK ,
    NEW.ACS_DEK ,
    NEW.ACS_DEK_CHK ,
    NEW.EBK_ZEK ,
    NEW.EBK_ZEK_CHK ,
    NEW.EBK_DEK ,
    NEW.EBK_DEK_CHK ,
    NEW.JCB_PVKI,
    NEW.VISA_PVKI,
    NEW.MASTER_PVKI,
    NEW.REMARK  );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ICKEY_A AFTER
INSERT
    ON
    PTR_ICKEY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_ICKEY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    KEY_TYPE ,
    KEY_ID ,
    KEY_SIZE ,
    IC_INDICATOR ,
    EXPIRE_DATE ,
    APR_DATE ,
    APR_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.KEY_TYPE ,
NEW.KEY_ID ,
NEW.KEY_SIZE ,
NEW.IC_INDICATOR ,
NEW.EXPIRE_DATE ,
NEW.APR_DATE ,
NEW.APR_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ICKEY_D AFTER
DELETE
    ON
    PTR_ICKEY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_ICKEY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    KEY_TYPE ,
    KEY_ID ,
    KEY_SIZE ,
    IC_INDICATOR ,
    EXPIRE_DATE ,
    APR_DATE ,
    APR_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.KEY_TYPE ,
OLD.KEY_ID ,
OLD.KEY_SIZE ,
OLD.IC_INDICATOR ,
OLD.EXPIRE_DATE ,
OLD.APR_DATE ,
OLD.APR_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ICKEY_U AFTER
UPDATE
    ON
    PTR_ICKEY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_ICKEY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    KEY_TYPE ,
    KEY_ID ,
    KEY_SIZE ,
    IC_INDICATOR ,
    EXPIRE_DATE ,
    APR_DATE ,
    APR_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.KEY_TYPE ,
NEW.KEY_ID ,
NEW.KEY_SIZE ,
NEW.IC_INDICATOR ,
NEW.EXPIRE_DATE ,
NEW.APR_DATE ,
NEW.APR_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PAYMENT_A AFTER
INSERT
    ON
    PTR_PAYMENT REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_PAYMENT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PAYMENT_TYPE ,
    CHI_NAME ,
    BILL_DESC ,
    PAY_NOTE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PAYMENT_TYPE ,
NEW.CHI_NAME ,
NEW.BILL_DESC ,
NEW.PAY_NOTE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PAYMENT_D AFTER
DELETE
    ON
    PTR_PAYMENT REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_PAYMENT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PAYMENT_TYPE ,
    CHI_NAME ,
    BILL_DESC ,
    PAY_NOTE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.PAYMENT_TYPE ,
OLD.CHI_NAME ,
OLD.BILL_DESC ,
OLD.PAY_NOTE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PAYMENT_U AFTER
UPDATE
    OF BILL_DESC,
    CHI_NAME,
    MOD_PGM,
    MOD_USER,
    PAY_NOTE,
    PAYMENT_TYPE ON
    PTR_PAYMENT REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_PAYMENT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PAYMENT_TYPE ,
    CHI_NAME ,
    BILL_DESC ,
    PAY_NOTE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PAYMENT_TYPE ,
NEW.CHI_NAME ,
NEW.BILL_DESC ,
NEW.PAY_NOTE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PREPAIDFEE_A AFTER
INSERT
    ON
    PTR_PREPAIDFEE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_PREPAIDFEE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_TYPE ,
    K_FEES_TXN_CODE ,
    K_FEES_BILL_TYPE ,
    DOM_FIX_AMT ,
    DOM_PERCENT ,
    DOM_MIN_AMT ,
    DOM_MAX_AMT ,
    INT_FIX_AMT ,
    INT_PERCENT ,
    INT_MIN_AMT ,
    INT_MAX_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_TYPE ,
NEW.FEES_TXN_CODE ,
NEW.FEES_BILL_TYPE ,
NEW.DOM_FIX_AMT ,
NEW.DOM_PERCENT ,
NEW.DOM_MIN_AMT ,
NEW.DOM_MAX_AMT ,
NEW.INT_FIX_AMT ,
NEW.INT_PERCENT ,
NEW.INT_MIN_AMT ,
NEW.INT_MAX_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PREPAIDFEE_D AFTER
DELETE
    ON
    PTR_PREPAIDFEE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_PREPAIDFEE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_TYPE ,
    K_FEES_TXN_CODE ,
    K_FEES_BILL_TYPE ,
    DOM_FIX_AMT ,
    DOM_PERCENT ,
    DOM_MIN_AMT ,
    DOM_MAX_AMT ,
    INT_FIX_AMT ,
    INT_PERCENT ,
    INT_MIN_AMT ,
    INT_MAX_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.CARD_TYPE ,
OLD.FEES_TXN_CODE ,
OLD.FEES_BILL_TYPE ,
OLD.DOM_FIX_AMT ,
OLD.DOM_PERCENT ,
OLD.DOM_MIN_AMT ,
OLD.DOM_MAX_AMT ,
OLD.INT_FIX_AMT ,
OLD.INT_PERCENT ,
OLD.INT_MIN_AMT ,
OLD.INT_MAX_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PREPAIDFEE_M_A AFTER
INSERT
    ON
    PTR_PREPAIDFEE_M REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_PREPAIDFEE_M( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_MERCHANT_NO ,
    K_FEES_TXN_CODE ,
    FEES_BILL_TYPE ,
    DOM_FIX_AMT ,
    DOM_PERCENT ,
    DOM_MIN_AMT ,
    DOM_MAX_AMT ,
    INT_FIX_AMT ,
    INT_PERCENT ,
    INT_MIN_AMT ,
    INT_MAX_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.MERCHANT_NO ,
NEW.FEES_TXN_CODE ,
NEW.FEES_BILL_TYPE ,
NEW.DOM_FIX_AMT ,
NEW.DOM_PERCENT ,
NEW.DOM_MIN_AMT ,
NEW.DOM_MAX_AMT ,
NEW.INT_FIX_AMT ,
NEW.INT_PERCENT ,
NEW.INT_MIN_AMT ,
NEW.INT_MAX_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PREPAIDFEE_M_D AFTER
DELETE
    ON
    PTR_PREPAIDFEE_M REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_PREPAIDFEE_M( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_MERCHANT_NO ,
    K_FEES_TXN_CODE ,
    FEES_BILL_TYPE ,
    DOM_FIX_AMT ,
    DOM_PERCENT ,
    DOM_MIN_AMT ,
    DOM_MAX_AMT ,
    INT_FIX_AMT ,
    INT_PERCENT ,
    INT_MIN_AMT ,
    INT_MAX_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.MERCHANT_NO ,
OLD.FEES_TXN_CODE ,
OLD.FEES_BILL_TYPE ,
OLD.DOM_FIX_AMT ,
OLD.DOM_PERCENT ,
OLD.DOM_MIN_AMT ,
OLD.DOM_MAX_AMT ,
OLD.INT_FIX_AMT ,
OLD.INT_PERCENT ,
OLD.INT_MIN_AMT ,
OLD.INT_MAX_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PREPAIDFEE_M_U AFTER
UPDATE
    OF FEES_BILL_TYPE ,
    DOM_FIX_AMT ,
    DOM_PERCENT ,
    DOM_MIN_AMT ,
    DOM_MAX_AMT ,
    INT_FIX_AMT ,
    INT_PERCENT ,
    INT_MIN_AMT ,
    INT_MAX_AMT ON
    PTR_PREPAIDFEE_M REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_PREPAIDFEE_M( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_MERCHANT_NO ,
    K_FEES_TXN_CODE ,
    FEES_BILL_TYPE ,
    DOM_FIX_AMT ,
    DOM_PERCENT ,
    DOM_MIN_AMT ,
    DOM_MAX_AMT ,
    INT_FIX_AMT ,
    INT_PERCENT ,
    INT_MIN_AMT ,
    INT_MAX_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.MERCHANT_NO ,
NEW.FEES_TXN_CODE ,
NEW.FEES_BILL_TYPE ,
NEW.DOM_FIX_AMT ,
NEW.DOM_PERCENT ,
NEW.DOM_MIN_AMT ,
NEW.DOM_MAX_AMT ,
NEW.INT_FIX_AMT ,
NEW.INT_PERCENT ,
NEW.INT_MIN_AMT ,
NEW.INT_MAX_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PREPAIDFEE_U AFTER
UPDATE
    OF CARD_TYPE,
    DOM_FIX_AMT,
    DOM_MAX_AMT,
    DOM_MIN_AMT,
    DOM_PERCENT,
    FEES_BILL_TYPE,
    FEES_TXN_CODE,
    INT_FIX_AMT,
    INT_MAX_AMT,
    INT_MIN_AMT,
    INT_PERCENT,
    MOD_PGM,
    MOD_USER ON
    PTR_PREPAIDFEE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_PREPAIDFEE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_CARD_TYPE ,
    K_FEES_TXN_CODE ,
    K_FEES_BILL_TYPE ,
    DOM_FIX_AMT ,
    DOM_PERCENT ,
    DOM_MIN_AMT ,
    DOM_MAX_AMT ,
    INT_FIX_AMT ,
    INT_PERCENT ,
    INT_MIN_AMT ,
    INT_MAX_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.CARD_TYPE ,
NEW.FEES_TXN_CODE ,
NEW.FEES_BILL_TYPE ,
NEW.DOM_FIX_AMT ,
NEW.DOM_PERCENT ,
NEW.DOM_MIN_AMT ,
NEW.DOM_MAX_AMT ,
NEW.INT_FIX_AMT ,
NEW.INT_PERCENT ,
NEW.INT_MIN_AMT ,
NEW.INT_MAX_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PROD_TYPE_A AFTER
INSERT
    ON
    PTR_PROD_TYPE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    log_ptr_prod_type ( mod_time,
    mod_audcode,
    mod_user,
    mod_pgm,
    acct_type,
    seqno,
    reg_bank_no,
    group_code,
    card_type,
    CRT_DATE,
    --file_time,
 CRT_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss'),
wk_audcode,
NEW.MOD_USER,
NEW.MOD_PGM,
NEW.ACCT_TYPE,
NEW.SEQNO,
NEW.REG_BANK_NO,
NEW.GROUP_CODE,
NEW.CARD_TYPE,
NEW.CRT_DATE,
-- NEW.FILE_TIME,
 NEW.CRT_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PROD_TYPE_D AFTER
DELETE
    ON
    PTR_PROD_TYPE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    log_ptr_prod_type ( mod_time,
    mod_audcode,
    mod_user,
    mod_pgm,
    acct_type,
    seqno,
    reg_bank_no,
    group_code,
    card_type,
    CRT_DATE,
    --file_time,
 CRT_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss'),
wk_audcode,
OLD.MOD_PGM,
OLD.MOD_USER ,
OLD.ACCT_TYPE,
OLD.SEQNO,
OLD.REG_BANK_NO,
OLD.GROUP_CODE,
OLD.CARD_TYPE,
OLD.CRT_DATE,
--OLD.FILE_TIME,
 OLD.CRT_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PROD_TYPE_U AFTER
UPDATE
    OF ACCT_TYPE,
    CARD_TYPE,
    CRT_DATE,
    /*FILE_TIME,*/
    GROUP_CODE,
    MOD_PGM,
    MOD_USER,
    REG_BANK_NO,
    SEQNO,
    CRT_USER ON
    PTR_PROD_TYPE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    log_ptr_prod_type ( mod_time,
    mod_audcode,
    mod_user,
    mod_pgm,
    acct_type,
    seqno,
    reg_bank_no,
    group_code,
    card_type,
    CRT_DATE,
    --file_time,
 CRT_USER )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss'),
wk_audcode,
NEW.MOD_USER,
NEW.MOD_PGM,
NEW.ACCT_TYPE,
NEW.SEQNO,
NEW.REG_BANK_NO,
NEW.GROUP_CODE,
NEW.CARD_TYPE,
NEW.CRT_DATE,
-- NEW.FILE_TIME,
 NEW.CRT_USER );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PUR_LMT_A AFTER
INSERT
    ON
    PTR_PUR_LMT REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    log_ptr_pur_lmt ( mod_time,
    mod_audcode,
    mod_user,
    mod_pgm,
    month_n,
    percent_a,
    hfix_a,
    hperc_a,
    lfix_a,
    lperc_a,
    percent_b,
    hfix_b,
    hperc_b,
    lfix_b,
    lprec_b,
    percent_c,
    lfix_c )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss'),
wk_audcode,
NEW.MOD_USER,
NEW.MOD_PGM,
NEW.MONTH_N,
NEW.PERCENT_A,
NEW.HFIX_A,
NEW.HPERC_A,
NEW.LFIX_A,
NEW.LPERC_A,
NEW.PERCENT_B,
NEW.HFIX_B,
NEW.HPERC_B,
NEW.LFIX_B,
NEW.LPREC_B,
NEW.PERCENT_C,
NEW.LFIX_C );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PUR_LMT_D AFTER
DELETE
    ON
    PTR_PUR_LMT REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    log_ptr_pur_lmt ( mod_time,
    mod_audcode,
    mod_user,
    mod_pgm,
    month_n,
    percent_a,
    hfix_a,
    hperc_a,
    lfix_a,
    lperc_a,
    percent_b,
    hfix_b,
    hperc_b,
    lfix_b,
    lprec_b,
    percent_c,
    lfix_c )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss'),
OLD.MOD_PGM,
OLD.MOD_USER,
wk_pgm,
OLD.MONTH_N,
OLD.PERCENT_A,
OLD.HFIX_A,
OLD.HPERC_A,
OLD.LFIX_A,
OLD.LPERC_A,
OLD.PERCENT_B,
OLD.HFIX_B,
OLD.HPERC_B,
OLD.LFIX_B,
OLD.LPREC_B,
OLD.PERCENT_C,
OLD.LFIX_C );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_PUR_LMT_U AFTER
UPDATE
    OF HFIX_A,
    HFIX_B,
    HPERC_A,
    HPERC_B,
    LFIX_A,
    LFIX_B,
    LFIX_C,
    LPERC_A,
    LPREC_B,
    MOD_PGM,
    MOD_USER,
    MONTH_N,
    PERCENT_A,
    PERCENT_B,
    PERCENT_C ON
    PTR_PUR_LMT REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    log_ptr_pur_lmt ( mod_time,
    mod_audcode,
    mod_user,
    mod_pgm,
    month_n,
    percent_a,
    hfix_a,
    hperc_a,
    lfix_a,
    lperc_a,
    percent_b,
    hfix_b,
    hperc_b,
    lfix_b,
    lprec_b,
    percent_c,
    lfix_c )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss'),
wk_audcode,
NEW.MOD_USER,
NEW.MOD_PGM,
NEW.MONTH_N,
NEW.PERCENT_A,
NEW.HFIX_A,
NEW.HPERC_A,
NEW.LFIX_A,
NEW.LPERC_A,
NEW.PERCENT_B,
NEW.HFIX_B,
NEW.HPERC_B,
NEW.LFIX_B,
NEW.LPREC_B,
NEW.PERCENT_C,
NEW.LFIX_C );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_RSKINTERVAL_A AFTER
INSERT
    ON
    PTR_RSKINTERVAL REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_RSKINTERVAL( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BIN_TYPE ,
    K_TRANS_TYPE ,
    K_ACQ_TYPE ,
    RETURN_DAY ,
    FST_CB_DAY ,
    REPRESENT_DAY ,
    SEC_CB_DAY ,
    PRE_ARBIT_DAY ,
    WARN_DAY )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BIN_TYPE ,
NEW.TRANS_TYPE ,
NEW.ACQ_TYPE ,
NEW.RETURN_DAY ,
NEW.FST_CB_DAY ,
NEW.REPRESENT_DAY ,
NEW.SEC_CB_DAY ,
NEW.PRE_ARBIT_DAY ,
NEW.WARN_DAY );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_RSKINTERVAL_D AFTER
DELETE
    ON
    PTR_RSKINTERVAL REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_RSKINTERVAL( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BIN_TYPE ,
    K_TRANS_TYPE ,
    K_ACQ_TYPE ,
    RETURN_DAY ,
    FST_CB_DAY ,
    REPRESENT_DAY ,
    SEC_CB_DAY ,
    PRE_ARBIT_DAY ,
    WARN_DAY )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.BIN_TYPE ,
OLD.TRANS_TYPE ,
OLD.ACQ_TYPE ,
OLD.RETURN_DAY ,
OLD.FST_CB_DAY ,
OLD.REPRESENT_DAY ,
OLD.SEC_CB_DAY ,
OLD.PRE_ARBIT_DAY ,
OLD.WARN_DAY );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_RSKINTERVAL_U AFTER
UPDATE
    OF RETURN_DAY ,
    FST_CB_DAY ,
    REPRESENT_DAY ,
    SEC_CB_DAY ,
    PRE_ARBIT_DAY ,
    WARN_DAY ON
    PTR_RSKINTERVAL REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_RSKINTERVAL( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_BIN_TYPE ,
    K_TRANS_TYPE ,
    K_ACQ_TYPE ,
    RETURN_DAY ,
    FST_CB_DAY ,
    REPRESENT_DAY ,
    SEC_CB_DAY ,
    PRE_ARBIT_DAY ,
    WARN_DAY )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.BIN_TYPE ,
NEW.TRANS_TYPE ,
NEW.ACQ_TYPE ,
NEW.RETURN_DAY ,
NEW.FST_CB_DAY ,
NEW.REPRESENT_DAY ,
NEW.SEC_CB_DAY ,
NEW.PRE_ARBIT_DAY ,
NEW.WARN_DAY );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_SRC_CODE_A AFTER
INSERT
    ON
    PTR_SRC_CODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_SRC_CODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_SOURCE_CODE ,
    SOURCE_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.SOURCE_CODE ,
NEW.SOURCE_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_SRC_CODE_D AFTER
DELETE
    ON
    PTR_SRC_CODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_SRC_CODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_SOURCE_CODE ,
    SOURCE_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.SOURCE_CODE ,
OLD.SOURCE_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_SRC_CODE_U AFTER
UPDATE
    OF SOURCE_NAME ON
    PTR_SRC_CODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_SRC_CODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_SOURCE_CODE ,
    SOURCE_NAME )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.SOURCE_CODE ,
NEW.SOURCE_NAME );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_STOPPARAM_A AFTER
DELETE
    OR
INSERT
    OR
UPDATE
    OF APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE ,
    DEBT_AMT ON
    PTR_STOPPARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 SET
wk_audcode = 'U';--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_STOPPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_VALID_DATE ,
    APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE ,
    DEBT_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PARAM_TYPE ,
NEW.ACCT_TYPE ,
NEW.VALID_DATE ,
NEW.APR_FLAG ,
NEW.EXEC_MODE ,
NEW.EXEC_DAY ,
NEW.EXEC_CYCLE_NDAY ,
NEW.EXEC_DATE ,
NEW.N0_MONTH ,
NEW.N1_CYCLE ,
NEW.MCODE_VALUE ,
NEW.DEBT_AMT );--
--
 INSERT
    INTO
    LOG_PTR_STOPPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_VALID_DATE ,
    APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE ,
    DEBT_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.PARAM_TYPE ,
OLD.ACCT_TYPE ,
OLD.VALID_DATE ,
OLD.APR_FLAG ,
OLD.EXEC_MODE ,
OLD.EXEC_DAY ,
OLD.EXEC_CYCLE_NDAY ,
OLD.EXEC_DATE ,
OLD.N0_MONTH ,
OLD.N1_CYCLE ,
OLD.MCODE_VALUE ,
OLD.DEBT_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_STOPPARAM_D AFTER
DELETE
    ON
    PTR_STOPPARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_STOPPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_VALID_DATE ,
    APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE ,
    DEBT_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.PARAM_TYPE ,
OLD.ACCT_TYPE ,
OLD.VALID_DATE ,
OLD.APR_FLAG ,
OLD.EXEC_MODE ,
OLD.EXEC_DAY ,
OLD.EXEC_CYCLE_NDAY ,
OLD.EXEC_DATE ,
OLD.N0_MONTH ,
OLD.N1_CYCLE ,
OLD.MCODE_VALUE ,
OLD.DEBT_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_STOPPARAM_U AFTER
UPDATE
    OF APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE ,
    DEBT_AMT ON
    PTR_STOPPARAM REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_STOPPARAM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_PARAM_TYPE ,
    K_ACCT_TYPE ,
    K_VALID_DATE ,
    APR_FLAG ,
    EXEC_MODE ,
    EXEC_DAY ,
    EXEC_CYCLE_NDAY ,
    EXEC_DATE ,
    N0_MONTH ,
    N1_CYCLE ,
    MCODE_VALUE ,
    DEBT_AMT )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.PARAM_TYPE ,
NEW.ACCT_TYPE ,
NEW.VALID_DATE ,
NEW.APR_FLAG ,
NEW.EXEC_MODE ,
NEW.EXEC_DAY ,
NEW.EXEC_CYCLE_NDAY ,
NEW.EXEC_DATE ,
NEW.N0_MONTH ,
NEW.N1_CYCLE ,
NEW.MCODE_VALUE ,
NEW.DEBT_AMT );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_WORKDAY_A AFTER
INSERT
    ON
    PTR_WORKDAY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_WORKDAY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_STMT_CYCLE ,
    ISSUE_S_DAY ,
    ISSUE_E_DAY ,
    CLOSE_STAND ,
    INTEREST_STAND ,
    LASTPAY_STAND ,
    DELAYPAY_STAND ,
    THIS_ACCT_MONTH ,
    LAST_ACCT_MONTH ,
    NEXT_ACCT_MONTH ,
    LL_ACCT_MONTH ,
    THIS_CLOSE_DATE ,
    LAST_CLOSE_DATE ,
    NEXT_CLOSE_DATE ,
    LL_CLOSE_DATE ,
    THIS_BILLING_DATE ,
    LAST_BILLING_DATE ,
    NEXT_BILLING_DATE ,
    LL_BILLING_DATE ,
    THIS_INTEREST_DATE ,
    LAST_INTEREST_DATE ,
    NEXT_INTEREST_DATE ,
    LL_INTEREST_DATE ,
    THIS_LASTPAY_DATE ,
    LAST_LASTPAY_DATE ,
    NEXT_LASTPAY_DATE ,
    LL_LASTPAY_DATE ,
    THIS_DELAYPAY_DATE ,
    LAST_DELAYPAY_DATE ,
    NEXT_DELAYPAY_DATE ,
    LL_DELAYPAY_DATE ,
    T_1ST_DEL_NOTICE_DATE ,
    L_1ST_DEL_NOTICE_DATE ,
    N_1ST_DEL_NOTICE_DATE ,
    LL_1ST_DEL_NOTICE_DATE ,
    T_2ST_DEL_NOTICE_DATE ,
    L_2ST_DEL_NOTICE_DATE ,
    N_2ST_DEL_NOTICE_DATE ,
    LL_2ST_DEL_NOTICE_DATE ,
    T_3TH_DEL_NOTICE_DATE ,
    L_3TH_DEL_NOTICE_DATE ,
    N_3TH_DEL_NOTICE_DATE ,
    LL_3TH_DEL_NOTICE_DATE ,
    CYCLE_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.STMT_CYCLE ,
NEW.ISSUE_S_DAY ,
NEW.ISSUE_E_DAY ,
NEW.CLOSE_STAND ,
NEW.INTEREST_STAND ,
NEW.LASTPAY_STAND ,
NEW.DELAYPAY_STAND ,
NEW.THIS_ACCT_MONTH ,
NEW.LAST_ACCT_MONTH ,
NEW.NEXT_ACCT_MONTH ,
NEW.LL_ACCT_MONTH ,
NEW.THIS_CLOSE_DATE ,
NEW.LAST_CLOSE_DATE ,
NEW.NEXT_CLOSE_DATE ,
NEW.LL_CLOSE_DATE ,
NEW.THIS_BILLING_DATE ,
NEW.LAST_BILLING_DATE ,
NEW.NEXT_BILLING_DATE ,
NEW.LL_BILLING_DATE ,
NEW.THIS_INTEREST_DATE ,
NEW.LAST_INTEREST_DATE ,
NEW.NEXT_INTEREST_DATE ,
NEW.LL_INTEREST_DATE ,
NEW.THIS_LASTPAY_DATE ,
NEW.LAST_LASTPAY_DATE ,
NEW.NEXT_LASTPAY_DATE ,
NEW.LL_LASTPAY_DATE ,
NEW.THIS_DELAYPAY_DATE ,
NEW.LAST_DELAYPAY_DATE ,
NEW.NEXT_DELAYPAY_DATE ,
NEW.LL_DELAYPAY_DATE ,
NEW.T_1ST_DEL_NOTICE_DATE ,
NEW.L_1ST_DEL_NOTICE_DATE ,
NEW.N_1ST_DEL_NOTICE_DATE ,
NEW.LL_1ST_DEL_NOTICE_DATE ,
NEW.T_2ST_DEL_NOTICE_DATE ,
NEW.L_2ST_DEL_NOTICE_DATE ,
NEW.N_2ST_DEL_NOTICE_DATE ,
NEW.LL_2ST_DEL_NOTICE_DATE ,
NEW.T_3TH_DEL_NOTICE_DATE ,
NEW.L_3TH_DEL_NOTICE_DATE ,
NEW.N_3TH_DEL_NOTICE_DATE ,
NEW.LL_3TH_DEL_NOTICE_DATE ,
NEW.CYCLE_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_WORKDAY_D AFTER
DELETE
    ON
    PTR_WORKDAY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_WORKDAY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_STMT_CYCLE ,
    ISSUE_S_DAY ,
    ISSUE_E_DAY ,
    CLOSE_STAND ,
    INTEREST_STAND ,
    LASTPAY_STAND ,
    DELAYPAY_STAND ,
    THIS_ACCT_MONTH ,
    LAST_ACCT_MONTH ,
    NEXT_ACCT_MONTH ,
    LL_ACCT_MONTH ,
    THIS_CLOSE_DATE ,
    LAST_CLOSE_DATE ,
    NEXT_CLOSE_DATE ,
    LL_CLOSE_DATE ,
    THIS_BILLING_DATE ,
    LAST_BILLING_DATE ,
    NEXT_BILLING_DATE ,
    LL_BILLING_DATE ,
    THIS_INTEREST_DATE ,
    LAST_INTEREST_DATE ,
    NEXT_INTEREST_DATE ,
    LL_INTEREST_DATE ,
    THIS_LASTPAY_DATE ,
    LAST_LASTPAY_DATE ,
    NEXT_LASTPAY_DATE ,
    LL_LASTPAY_DATE ,
    THIS_DELAYPAY_DATE ,
    LAST_DELAYPAY_DATE ,
    NEXT_DELAYPAY_DATE ,
    LL_DELAYPAY_DATE ,
    T_1ST_DEL_NOTICE_DATE ,
    L_1ST_DEL_NOTICE_DATE ,
    N_1ST_DEL_NOTICE_DATE ,
    LL_1ST_DEL_NOTICE_DATE ,
    T_2ST_DEL_NOTICE_DATE ,
    L_2ST_DEL_NOTICE_DATE ,
    N_2ST_DEL_NOTICE_DATE ,
    LL_2ST_DEL_NOTICE_DATE ,
    T_3TH_DEL_NOTICE_DATE ,
    L_3TH_DEL_NOTICE_DATE ,
    N_3TH_DEL_NOTICE_DATE ,
    LL_3TH_DEL_NOTICE_DATE ,
    CYCLE_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
OLD.MOD_PGM ,
OLD.MOD_USER ,
wk_audcode ,
OLD.STMT_CYCLE ,
OLD.ISSUE_S_DAY ,
OLD.ISSUE_E_DAY ,
OLD.CLOSE_STAND ,
OLD.INTEREST_STAND ,
OLD.LASTPAY_STAND ,
OLD.DELAYPAY_STAND ,
OLD.THIS_ACCT_MONTH ,
OLD.LAST_ACCT_MONTH ,
OLD.NEXT_ACCT_MONTH ,
OLD.LL_ACCT_MONTH ,
OLD.THIS_CLOSE_DATE ,
OLD.LAST_CLOSE_DATE ,
OLD.NEXT_CLOSE_DATE ,
OLD.LL_CLOSE_DATE ,
OLD.THIS_BILLING_DATE ,
OLD.LAST_BILLING_DATE ,
OLD.NEXT_BILLING_DATE ,
OLD.LL_BILLING_DATE ,
OLD.THIS_INTEREST_DATE ,
OLD.LAST_INTEREST_DATE ,
OLD.NEXT_INTEREST_DATE ,
OLD.LL_INTEREST_DATE ,
OLD.THIS_LASTPAY_DATE ,
OLD.LAST_LASTPAY_DATE ,
OLD.NEXT_LASTPAY_DATE ,
OLD.LL_LASTPAY_DATE ,
OLD.THIS_DELAYPAY_DATE ,
OLD.LAST_DELAYPAY_DATE ,
OLD.NEXT_DELAYPAY_DATE ,
OLD.LL_DELAYPAY_DATE ,
OLD.T_1ST_DEL_NOTICE_DATE ,
OLD.L_1ST_DEL_NOTICE_DATE ,
OLD.N_1ST_DEL_NOTICE_DATE ,
OLD.LL_1ST_DEL_NOTICE_DATE ,
OLD.T_2ST_DEL_NOTICE_DATE ,
OLD.L_2ST_DEL_NOTICE_DATE ,
OLD.N_2ST_DEL_NOTICE_DATE ,
OLD.LL_2ST_DEL_NOTICE_DATE ,
OLD.T_3TH_DEL_NOTICE_DATE ,
OLD.L_3TH_DEL_NOTICE_DATE ,
OLD.N_3TH_DEL_NOTICE_DATE ,
OLD.LL_3TH_DEL_NOTICE_DATE ,
OLD.CYCLE_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_WORKDAY_U AFTER
UPDATE
    OF CLOSE_STAND,
    CYCLE_FLAG,
    DELAYPAY_STAND,
    INTEREST_STAND,
    ISSUE_E_DAY,
    ISSUE_S_DAY,
    LASTPAY_STAND,
    LAST_ACCT_MONTH,
    LAST_BILLING_DATE,
    LAST_CLOSE_DATE,
    LAST_DELAYPAY_DATE,
    LAST_INTEREST_DATE,
    LAST_LASTPAY_DATE,
    LL_1ST_DEL_NOTICE_DATE,
    LL_2ST_DEL_NOTICE_DATE,
    LL_3TH_DEL_NOTICE_DATE,
    LL_ACCT_MONTH,
    LL_BILLING_DATE,
    LL_CLOSE_DATE,
    LL_DELAYPAY_DATE,
    LL_INTEREST_DATE,
    LL_LASTPAY_DATE,
    L_1ST_DEL_NOTICE_DATE,
    L_2ST_DEL_NOTICE_DATE,
    L_3TH_DEL_NOTICE_DATE,
    MOD_PGM,
    MOD_USER,
    NEXT_ACCT_MONTH,
    NEXT_BILLING_DATE,
    NEXT_CLOSE_DATE,
    NEXT_DELAYPAY_DATE,
    NEXT_INTEREST_DATE,
    NEXT_LASTPAY_DATE,
    N_1ST_DEL_NOTICE_DATE,
    N_2ST_DEL_NOTICE_DATE,
    N_3TH_DEL_NOTICE_DATE,
    STMT_CYCLE,
    THIS_ACCT_MONTH,
    THIS_BILLING_DATE,
    THIS_CLOSE_DATE,
    THIS_DELAYPAY_DATE,
    THIS_INTEREST_DATE,
    THIS_LASTPAY_DATE,
    T_1ST_DEL_NOTICE_DATE,
    T_2ST_DEL_NOTICE_DATE,
    T_3TH_DEL_NOTICE_DATE ON
    PTR_WORKDAY REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_WORKDAY( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_STMT_CYCLE ,
    ISSUE_S_DAY ,
    ISSUE_E_DAY ,
    CLOSE_STAND ,
    INTEREST_STAND ,
    LASTPAY_STAND ,
    DELAYPAY_STAND ,
    THIS_ACCT_MONTH ,
    LAST_ACCT_MONTH ,
    NEXT_ACCT_MONTH ,
    LL_ACCT_MONTH ,
    THIS_CLOSE_DATE ,
    LAST_CLOSE_DATE ,
    NEXT_CLOSE_DATE ,
    LL_CLOSE_DATE ,
    THIS_BILLING_DATE ,
    LAST_BILLING_DATE ,
    NEXT_BILLING_DATE ,
    LL_BILLING_DATE ,
    THIS_INTEREST_DATE ,
    LAST_INTEREST_DATE ,
    NEXT_INTEREST_DATE ,
    LL_INTEREST_DATE ,
    THIS_LASTPAY_DATE ,
    LAST_LASTPAY_DATE ,
    NEXT_LASTPAY_DATE ,
    LL_LASTPAY_DATE ,
    THIS_DELAYPAY_DATE ,
    LAST_DELAYPAY_DATE ,
    NEXT_DELAYPAY_DATE ,
    LL_DELAYPAY_DATE ,
    T_1ST_DEL_NOTICE_DATE ,
    L_1ST_DEL_NOTICE_DATE ,
    N_1ST_DEL_NOTICE_DATE ,
    LL_1ST_DEL_NOTICE_DATE ,
    T_2ST_DEL_NOTICE_DATE ,
    L_2ST_DEL_NOTICE_DATE ,
    N_2ST_DEL_NOTICE_DATE ,
    LL_2ST_DEL_NOTICE_DATE ,
    T_3TH_DEL_NOTICE_DATE ,
    L_3TH_DEL_NOTICE_DATE ,
    N_3TH_DEL_NOTICE_DATE ,
    LL_3TH_DEL_NOTICE_DATE ,
    CYCLE_FLAG )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.STMT_CYCLE ,
NEW.ISSUE_S_DAY ,
NEW.ISSUE_E_DAY ,
NEW.CLOSE_STAND ,
NEW.INTEREST_STAND ,
NEW.LASTPAY_STAND ,
NEW.DELAYPAY_STAND ,
NEW.THIS_ACCT_MONTH ,
NEW.LAST_ACCT_MONTH ,
NEW.NEXT_ACCT_MONTH ,
NEW.LL_ACCT_MONTH ,
NEW.THIS_CLOSE_DATE ,
NEW.LAST_CLOSE_DATE ,
NEW.NEXT_CLOSE_DATE ,
NEW.LL_CLOSE_DATE ,
NEW.THIS_BILLING_DATE ,
NEW.LAST_BILLING_DATE ,
NEW.NEXT_BILLING_DATE ,
NEW.LL_BILLING_DATE ,
NEW.THIS_INTEREST_DATE ,
NEW.LAST_INTEREST_DATE ,
NEW.NEXT_INTEREST_DATE ,
NEW.LL_INTEREST_DATE ,
NEW.THIS_LASTPAY_DATE ,
NEW.LAST_LASTPAY_DATE ,
NEW.NEXT_LASTPAY_DATE ,
NEW.LL_LASTPAY_DATE ,
NEW.THIS_DELAYPAY_DATE ,
NEW.LAST_DELAYPAY_DATE ,
NEW.NEXT_DELAYPAY_DATE ,
NEW.LL_DELAYPAY_DATE ,
NEW.T_1ST_DEL_NOTICE_DATE ,
NEW.L_1ST_DEL_NOTICE_DATE ,
NEW.N_1ST_DEL_NOTICE_DATE ,
NEW.LL_1ST_DEL_NOTICE_DATE ,
NEW.T_2ST_DEL_NOTICE_DATE ,
NEW.L_2ST_DEL_NOTICE_DATE ,
NEW.N_2ST_DEL_NOTICE_DATE ,
NEW.LL_2ST_DEL_NOTICE_DATE ,
NEW.T_3TH_DEL_NOTICE_DATE ,
NEW.L_3TH_DEL_NOTICE_DATE ,
NEW.N_3TH_DEL_NOTICE_DATE ,
NEW.LL_3TH_DEL_NOTICE_DATE ,
NEW.CYCLE_FLAG );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ZIPCODE_A AFTER
INSERT
    ON
    PTR_ZIPCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_PTR_ZIPCODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ZIP_CODE ,
    ZIP_CITY ,
    ZIP_TOWN )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ZIP_CODE ,
NEW.ZIP_CITY ,
NEW.ZIP_TOWN );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ZIPCODE_D AFTER
DELETE
    ON
    PTR_ZIPCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_PTR_ZIPCODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ZIP_CODE ,
    ZIP_CITY ,
    ZIP_TOWN )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.ZIP_CODE ,
OLD.ZIP_CITY ,
OLD.ZIP_TOWN );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_PTR_ZIPCODE_U AFTER
UPDATE
    OF ZIP_CITY ,
    ZIP_TOWN ON
    PTR_ZIPCODE REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_PTR_ZIPCODE( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_ZIP_CODE ,
    ZIP_CITY ,
    ZIP_TOWN )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.ZIP_CODE ,
NEW.ZIP_CITY ,
NEW.ZIP_TOWN );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_RSK_CHGBACK_A AFTER
INSERT
    ON
    RSK_CHGBACK REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_RSK_CHGBACK( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    K_REFERENCE_SEQ ,
    BIN_TYPE ,
    FINAL_CLOSE ,
    CLO_RESULT ,
    CHG_TIMES ,
    FST_REVERSE_MARK ,
    FST_REBUILD_MARK ,
    FST_REASON_CODE ,
    FST_MSG ,
    FST_DOC_MARK ,
    FST_AMOUNT ,
    FST_PART_MARK ,
    FST_DISB_AMT ,
    REP_MSG ,
    REP_DOC_MARK ,
    REP_AMT ,
    SEC_REVERSE_MARK ,
    SEC_REBUILD_MARK ,
    SEC_REASON_CODE ,
    SEC_MSG ,
    SEC_DOC_MARK ,
    SEC_AMOUNT ,
    SEC_PART_MARK ,
    SEC_DISB_AMT
    --,PREARBIT_DATE
    --,ARBIT_DATE
)
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REFERENCE_NO ,
NEW.REFERENCE_SEQ ,
NEW.BIN_TYPE ,
NEW.FINAL_CLOSE ,
NEW.CLO_RESULT ,
NEW.CHG_TIMES ,
NEW.FST_REVERSE_MARK ,
NEW.FST_REBUILD_MARK ,
NEW.FST_REASON_CODE ,
NEW.FST_MSG ,
NEW.FST_DOC_MARK ,
NEW.FST_AMOUNT ,
NEW.FST_PART_MARK ,
NEW.FST_DISB_AMT ,
NEW.REP_MSG ,
NEW.REP_DOC_MARK ,
NEW.REP_AMT ,
NEW.SEC_REVERSE_MARK ,
NEW.SEC_REBUILD_MARK ,
NEW.SEC_REASON_CODE ,
NEW.SEC_MSG ,
NEW.SEC_DOC_MARK ,
NEW.SEC_AMOUNT ,
NEW.SEC_PART_MARK ,
NEW.SEC_DISB_AMT
--,NEW.PREARBIT_DATE
--,NEW.ARBIT_DATE
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_RSK_CHGBACK_D AFTER
DELETE
    ON
    RSK_CHGBACK REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_RSK_CHGBACK( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    K_REFERENCE_SEQ ,
    BIN_TYPE ,
    FINAL_CLOSE ,
    CLO_RESULT ,
    CHG_TIMES ,
    FST_REVERSE_MARK ,
    FST_REBUILD_MARK ,
    FST_REASON_CODE ,
    FST_MSG ,
    FST_DOC_MARK ,
    FST_AMOUNT ,
    FST_PART_MARK ,
    FST_DISB_AMT ,
    REP_MSG ,
    REP_DOC_MARK ,
    REP_AMT ,
    SEC_REVERSE_MARK ,
    SEC_REBUILD_MARK ,
    SEC_REASON_CODE ,
    SEC_MSG ,
    SEC_DOC_MARK ,
    SEC_AMOUNT ,
    SEC_PART_MARK ,
    SEC_DISB_AMT
    --,PREARBIT_DATE
    --,ARBIT_DATE
)
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.REFERENCE_NO ,
OLD.REFERENCE_SEQ ,
OLD.BIN_TYPE ,
OLD.FINAL_CLOSE ,
OLD.CLO_RESULT ,
OLD.CHG_TIMES ,
OLD.FST_REVERSE_MARK ,
OLD.FST_REBUILD_MARK ,
OLD.FST_REASON_CODE ,
OLD.FST_MSG ,
OLD.FST_DOC_MARK ,
OLD.FST_AMOUNT ,
OLD.FST_PART_MARK ,
OLD.FST_DISB_AMT ,
OLD.REP_MSG ,
OLD.REP_DOC_MARK ,
OLD.REP_AMT ,
OLD.SEC_REVERSE_MARK ,
OLD.SEC_REBUILD_MARK ,
OLD.SEC_REASON_CODE ,
OLD.SEC_MSG ,
OLD.SEC_DOC_MARK ,
OLD.SEC_AMOUNT ,
OLD.SEC_PART_MARK ,
OLD.SEC_DISB_AMT
--,OLD.PREARBIT_DATE
--,OLD.ARBIT_DATE
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_RSK_CHGBACK_U AFTER
UPDATE
    OF BIN_TYPE ,
    FINAL_CLOSE ,
    CLO_RESULT ,
    CHG_TIMES ,
    FST_REVERSE_MARK ,
    FST_REBUILD_MARK ,
    FST_REASON_CODE ,
    FST_MSG ,
    FST_DOC_MARK ,
    FST_AMOUNT ,
    FST_PART_MARK ,
    FST_DISB_AMT ,
    REP_MSG ,
    REP_DOC_MARK ,
    REP_AMT ,
    SEC_REVERSE_MARK ,
    SEC_REBUILD_MARK ,
    SEC_REASON_CODE ,
    SEC_MSG ,
    SEC_DOC_MARK ,
    SEC_AMOUNT ,
    SEC_PART_MARK ,
    SEC_DISB_AMT
    --,PREARBIT_DATE
    --,ARBIT_DATE
 ON
    RSK_CHGBACK REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_RSK_CHGBACK( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    K_REFERENCE_SEQ ,
    BIN_TYPE ,
    FINAL_CLOSE ,
    CLO_RESULT ,
    CHG_TIMES ,
    FST_REVERSE_MARK ,
    FST_REBUILD_MARK ,
    FST_REASON_CODE ,
    FST_MSG ,
    FST_DOC_MARK ,
    FST_AMOUNT ,
    FST_PART_MARK ,
    FST_DISB_AMT ,
    REP_MSG ,
    REP_DOC_MARK ,
    REP_AMT ,
    SEC_REVERSE_MARK ,
    SEC_REBUILD_MARK ,
    SEC_REASON_CODE ,
    SEC_MSG ,
    SEC_DOC_MARK ,
    SEC_AMOUNT ,
    SEC_PART_MARK ,
    SEC_DISB_AMT
    --,PREARBIT_DATE
    --,ARBIT_DATE
)
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REFERENCE_NO ,
NEW.REFERENCE_SEQ ,
NEW.BIN_TYPE ,
NEW.FINAL_CLOSE ,
NEW.CLO_RESULT ,
NEW.CHG_TIMES ,
NEW.FST_REVERSE_MARK ,
NEW.FST_REBUILD_MARK ,
NEW.FST_REASON_CODE ,
NEW.FST_MSG ,
NEW.FST_DOC_MARK ,
NEW.FST_AMOUNT ,
NEW.FST_PART_MARK ,
NEW.FST_DISB_AMT ,
NEW.REP_MSG ,
NEW.REP_DOC_MARK ,
NEW.REP_AMT ,
NEW.SEC_REVERSE_MARK ,
NEW.SEC_REBUILD_MARK ,
NEW.SEC_REASON_CODE ,
NEW.SEC_MSG ,
NEW.SEC_DOC_MARK ,
NEW.SEC_AMOUNT ,
NEW.SEC_PART_MARK ,
NEW.SEC_DISB_AMT
--,NEW.PREARBIT_DATE
--,NEW.ARBIT_DATE
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_RSK_PROBLEM_A AFTER
INSERT
    ON
    RSK_PROBLEM REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_RSK_PROBLEM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    K_REFERENCE_SEQ ,
    PRB_REASON_CODE ,
    PRB_AMOUNT ,
    PRB_FRAUD_RPT ,
    PRB_COMMENT ,
    CLO_RESULT
    --,REASON_CODE
)
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REFERENCE_NO ,
NEW.REFERENCE_SEQ ,
NEW.PRB_REASON_CODE ,
NEW.PRB_AMOUNT ,
NEW.PRB_FRAUD_RPT ,
NEW.PRB_COMMENT ,
NEW.CLO_RESULT
--,NEW.REASON_CODE
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_RSK_PROBLEM_D AFTER
DELETE
    ON
    RSK_PROBLEM REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_RSK_PROBLEM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    K_REFERENCE_SEQ ,
    PRB_REASON_CODE ,
    PRB_AMOUNT ,
    PRB_FRAUD_RPT ,
    PRB_COMMENT ,
    CLO_RESULT
    --,REASON_CODE
)
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.REFERENCE_NO ,
OLD.REFERENCE_SEQ ,
OLD.PRB_REASON_CODE ,
OLD.PRB_AMOUNT ,
OLD.PRB_FRAUD_RPT ,
OLD.PRB_COMMENT ,
OLD.CLO_RESULT
--,OLD.REASON_CODE
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_RSK_PROBLEM_U AFTER
UPDATE
    OF PRB_REASON_CODE ,
    PRB_AMOUNT ,
    PRB_FRAUD_RPT ,
    PRB_COMMENT ,
    CLO_RESULT
    --,REASON_CODE
 ON
    RSK_PROBLEM REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_RSK_PROBLEM( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    K_REFERENCE_SEQ ,
    PRB_REASON_CODE ,
    PRB_AMOUNT ,
    PRB_FRAUD_RPT ,
    PRB_COMMENT ,
    CLO_RESULT
    --,REASON_CODE
)
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REFERENCE_NO ,
NEW.REFERENCE_SEQ ,
NEW.PRB_REASON_CODE ,
NEW.PRB_AMOUNT ,
NEW.PRB_FRAUD_RPT ,
NEW.PRB_COMMENT ,
NEW.CLO_RESULT
--,NEW.REASON_CODE
);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_RSK_RECEIPT_A AFTER
INSERT
    ON
    RSK_RECEIPT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    LOG_RSK_RECEIPT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    K_REFERENCE_SEQ ,
    REPT_SEQNO ,
    REJECT_MARK ,
    REPT_TYPE ,
    REASON_CODE ,
    RECV_DATE ,
    PROC_RESULT ,
    CLOSE_ADD_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REFERENCE_NO ,
NEW.REFERENCE_SEQ ,
NEW.REPT_SEQNO ,
NEW.REJECT_MARK ,
NEW.REPT_TYPE ,
NEW.REASON_CODE ,
NEW.RECV_DATE ,
NEW.PROC_RESULT ,
NEW.CLOSE_ADD_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_RSK_RECEIPT_D AFTER
DELETE
    ON
    RSK_RECEIPT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    LOG_RSK_RECEIPT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    K_REFERENCE_SEQ ,
    REPT_SEQNO ,
    REJECT_MARK ,
    REPT_TYPE ,
    REASON_CODE ,
    RECV_DATE ,
    PROC_RESULT ,
    CLOSE_ADD_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
wk_pgm ,
wk_user ,
wk_audcode ,
OLD.REFERENCE_NO ,
OLD.REFERENCE_SEQ ,
OLD.REPT_SEQNO ,
OLD.REJECT_MARK ,
OLD.REPT_TYPE ,
OLD.REASON_CODE ,
OLD.RECV_DATE ,
OLD.PROC_RESULT ,
OLD.CLOSE_ADD_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_LOG_RSK_RECEIPT_U AFTER
UPDATE
    OF REPT_SEQNO ,
    REJECT_MARK ,
    REPT_TYPE ,
    REASON_CODE ,
    RECV_DATE ,
    PROC_RESULT ,
    CLOSE_ADD_DATE ON
    RSK_RECEIPT REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    LOG_RSK_RECEIPT( MOD_TIME ,
    MOD_PGM ,
    MOD_USER ,
    MOD_AUDCODE ,
    K_REFERENCE_NO ,
    K_REFERENCE_SEQ ,
    REPT_SEQNO ,
    REJECT_MARK ,
    REPT_TYPE ,
    REASON_CODE ,
    RECV_DATE ,
    PROC_RESULT ,
    CLOSE_ADD_DATE )
VALUES ( to_char(sysdate,
'yyyymmddhh24misssss') ,
NEW.MOD_PGM ,
NEW.MOD_USER ,
wk_audcode ,
NEW.REFERENCE_NO ,
NEW.REFERENCE_SEQ ,
NEW.REPT_SEQNO ,
NEW.REJECT_MARK ,
NEW.REPT_TYPE ,
NEW.REASON_CODE ,
NEW.RECV_DATE ,
NEW.PROC_RESULT ,
NEW.CLOSE_ADD_DATE );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_RELA_HIST_A
AFTER 
   INSERT
ON CRD_RELA
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW MODE DB2SQL
WHEN (
        nvl(old.rela_id,' ') <> nvl(new.rela_id,' ') or
      	nvl(old.rela_name,' ') <> nvl(new.rela_name,' ') or
	nvl(old.card_no,' ')   <> nvl(new.card_no,' ') or
	nvl(old.office_area_code1,' ') <> nvl(new.office_area_code1,' ') or
	nvl(old.office_tel_no1,' ')    <> nvl(new.office_tel_no1,' ') or
	nvl(old.office_tel_ext1,' ')   <> nvl(new.office_tel_ext1,' ') or
	nvl(old.office_area_code2,' ') <> nvl(new.office_area_code2,' ') or
	nvl(old.office_tel_no2,' ')    <> nvl(new.office_tel_no2,' ') or
	nvl(old.office_tel_ext2,' ')   <> nvl(new.office_tel_ext2,' ') or
	nvl(old.home_area_code1,' ')   <> nvl(new.home_area_code1,' ') or
	nvl(old.home_tel_no1,' ')      <> nvl(new.home_tel_no1,' ') or
	nvl(old.home_tel_ext1,' ')     <> nvl(new.home_tel_ext1,' ') or
	nvl(old.home_area_code2,' ')   <> nvl(new.home_area_code2,' ') or
	nvl(old.home_tel_no2,' ')      <> nvl(new.home_tel_no2,' ') or
	nvl(old.home_tel_ext2,' ')     <> nvl(new.home_tel_ext2,' ') or
	nvl(old.cellar_phone,' ')      <> nvl(new.cellar_phone,' ') or
	nvl(old.start_date,' ')        <> nvl(new.start_date,' ') or
	nvl(old.end_date,' ')          <> nvl(new.end_date,' ')
      )

BEGIN

  DECLARE  wk_audcode          VARCHAR(1);--
  DECLARE  wk_user             VARCHAR(10);--
  DECLARE  wk_pgm              VARCHAR(20);--
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--


--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
	IF new.rela_type != '1' THEN
	    RETURN;--
	END IF;--

		set wk_audcode = 'A';--
		set wk_user = new.mod_user;--
		set wk_pgm = new.mod_pgm;--

	  INSERT INTO crd_rela_hist(
			mod_time            ,
               		mod_pgm             ,
               		mod_user            ,
               		mod_audcode         ,
               		rela_type           ,
                        acno_p_seqno        ,
               		rela_id             ,
               		card_no             ,
               		sex                 ,
               		birthday            ,
               		id_p_seqno          ,
               		--id                  ,
               		--id_code             ,
               		acct_type           ,
               		--acct_key            ,
               		rela_name           ,
               		company_name        ,
               		company_zip         ,
               		company_addr1       ,
               		company_addr2       ,
               		company_addr3       ,
               		company_addr4       ,
               		company_addr5       ,
               		home_area_code1     ,
               		home_tel_no1        ,
               		home_tel_ext1       ,
               		home_area_code2     ,
               		home_tel_no2        ,
               		home_tel_ext2       ,
            			office_area_code1   ,
            			office_tel_no1      ,
            			office_tel_ext1     ,
            			office_area_code2   ,
            			office_tel_no2      ,
            			office_tel_ext2     ,
                     cellar_phone        ,
                     --bb_call             ,
               		mail_zip            ,
               		mail_addr1          ,
               		mail_addr2          ,
               		mail_addr3          ,
               		mail_addr4          ,
               		mail_addr5          ,
               		start_date          ,
               		end_date             
	 ) VALUES (
			            to_char(sysdate,'yyyymmddhh24miss'),
               		new.mod_pgm             ,
               		new.mod_user            ,
               		wk_audcode         ,
               		new.rela_type           ,
               		new.acno_p_seqno             ,
               		new.rela_id             ,
               		new.card_no             ,
               		new.sex                 ,
               		new.birthday            ,
               		new.id_p_seqno          ,
               		--new.id                  ,
               		--new.id_code             ,
               		new.acct_type           ,
               		--new.acct_key            ,
               		new.rela_name           ,
               		new.company_name        ,
               		new.company_zip         ,
               		new.company_addr1       ,
               		new.company_addr2       ,
               		new.company_addr3       ,
               		new.company_addr4       ,
               		new.company_addr5       ,
               		new.home_area_code1     ,
               		new.home_tel_no1        ,
               		new.home_tel_ext1       ,
               		new.home_area_code2     ,
               		new.home_tel_no2        ,
               		new.home_tel_ext2       ,
            			new.office_area_code1   ,
            			new.office_tel_no1      ,
            			new.office_tel_ext1     ,
            			new.office_area_code2   ,
            			new.office_tel_no2      ,
            			new.office_tel_ext2     ,
                     new.cellar_phone        ,
                     --new.bb_call             ,
               		new.mail_zip            ,
               		new.mail_addr1          ,
               		new.mail_addr2          ,
               		new.mail_addr3          ,
               		new.mail_addr4          ,
               		new.mail_addr5          ,
               		new.start_date          ,
               		new.end_date            );--
        

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_RELA_HIST_D
AFTER 
   DELETE 
ON CRD_RELA
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW MODE DB2SQL
WHEN (
        nvl(old.rela_id,' ') <> nvl(new.rela_id,' ') or
      	nvl(old.rela_name,' ') <> nvl(new.rela_name,' ') or
	nvl(old.card_no,' ')   <> nvl(new.card_no,' ') or
	nvl(old.office_area_code1,' ') <> nvl(new.office_area_code1,' ') or
	nvl(old.office_tel_no1,' ')    <> nvl(new.office_tel_no1,' ') or
	nvl(old.office_tel_ext1,' ')   <> nvl(new.office_tel_ext1,' ') or
	nvl(old.office_area_code2,' ') <> nvl(new.office_area_code2,' ') or
	nvl(old.office_tel_no2,' ')    <> nvl(new.office_tel_no2,' ') or
	nvl(old.office_tel_ext2,' ')   <> nvl(new.office_tel_ext2,' ') or
	nvl(old.home_area_code1,' ')   <> nvl(new.home_area_code1,' ') or
	nvl(old.home_tel_no1,' ')      <> nvl(new.home_tel_no1,' ') or
	nvl(old.home_tel_ext1,' ')     <> nvl(new.home_tel_ext1,' ') or
	nvl(old.home_area_code2,' ')   <> nvl(new.home_area_code2,' ') or
	nvl(old.home_tel_no2,' ')      <> nvl(new.home_tel_no2,' ') or
	nvl(old.home_tel_ext2,' ')     <> nvl(new.home_tel_ext2,' ') or
	nvl(old.cellar_phone,' ')      <> nvl(new.cellar_phone,' ') or
	nvl(old.start_date,' ')        <> nvl(new.start_date,' ') or
	nvl(old.end_date,' ')          <> nvl(new.end_date,' ')
      )

BEGIN

  DECLARE  wk_audcode          VARCHAR(1);--
  DECLARE  wk_user             VARCHAR(10);--
  DECLARE  wk_pgm              VARCHAR(20);--
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--


--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
	IF new.rela_type != '1' THEN
	    RETURN;--
	END IF;--

		set wk_audcode = 'D';--
		set wk_user = new.mod_user;--
		set wk_pgm = new.mod_pgm;--

	  INSERT INTO crd_rela_hist(
	                mod_time            ,
               		mod_pgm             ,
               		mod_user            ,
               		mod_audcode         ,
               		rela_type           ,
               		acno_p_seqno             ,
               		rela_id             ,
               		card_no             ,
               		sex                 ,
               		birthday            ,
               		id_p_seqno          ,
               		--id                  ,
               		--id_code             ,
               		acct_type           ,
               		--acct_key            ,
               		rela_name           ,
               		company_name        ,
               		company_zip         ,
               		company_addr1       ,
               		company_addr2       ,
               		company_addr3       ,
               		company_addr4       ,
               		company_addr5       ,
               		home_area_code1     ,
               		home_tel_no1        ,
               		home_tel_ext1       ,
               		home_area_code2     ,
               		home_tel_no2        ,
               		home_tel_ext2       ,
            			office_area_code1   ,
            			office_tel_no1      ,
            			office_tel_ext1     ,
            			office_area_code2   ,
            			office_tel_no2      ,
            			office_tel_ext2     ,
                     cellar_phone        ,
                     --bb_call             ,
               		mail_zip            ,
               		mail_addr1          ,
               		mail_addr2          ,
               		mail_addr3          ,
               		mail_addr4          ,
               		mail_addr5          ,
               		start_date          ,
               		end_date            
	 ) VALUES (
			            to_char(sysdate,'yyyymmddhh24miss'),
               		nvl(wk_pgm,'x')          ,
               		nvl(wk_user,'x')         ,
               		wk_audcode               ,
               		old.rela_type           ,
               		old.acno_p_seqno        ,
               		old.rela_id             ,
               		old.card_no             ,
               		old.sex                 ,
               		old.birthday            ,
               		old.id_p_seqno          ,
               		--old.id                  ,
               		--old.id_code             ,
               		old.acct_type           ,
               		--old.acct_key            ,
               		old.rela_name           ,
               		old.company_name        ,
               		old.company_zip         ,
               		old.company_addr1       ,
               		old.company_addr2       ,
               		old.company_addr3       ,
               		old.company_addr4       ,
               		old.company_addr5       ,
               		old.home_area_code1     ,
               		old.home_tel_no1        ,
               		old.home_tel_ext1       ,
               		old.home_area_code2     ,
               		old.home_tel_no2        ,
               		old.home_tel_ext2       ,
            			old.office_area_code1   ,
            			old.office_tel_no1      ,
            			old.office_tel_ext1     ,
            			old.office_area_code2   ,
            			old.office_tel_no2      ,
            			old.office_tel_ext2     ,
                     old.cellar_phone        ,
                     --old.bb_call             ,
               		old.mail_zip            ,
               		old.mail_addr1          ,
               		old.mail_addr2          ,
               		old.mail_addr3          ,
               		old.mail_addr4          ,
               		old.mail_addr5          ,
               		old.start_date          ,
               		old.end_date            );--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_RELA_HIST_U
AFTER 
   UPDATE
 OF rela_id,rela_name,card_no, 
    office_area_code1,office_tel_no1,office_tel_ext1,
    office_area_code2,office_tel_no2,office_tel_ext2,
    home_area_code1,home_tel_no1,home_tel_ext1,
    home_area_code2,home_tel_no2,home_tel_ext2,cellar_phone,
    start_date,end_date
ON CRD_RELA
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW MODE DB2SQL
WHEN (
        nvl(old.rela_id,' ') <> nvl(new.rela_id,' ') or
      	nvl(old.rela_name,' ') <> nvl(new.rela_name,' ') or
	nvl(old.card_no,' ')   <> nvl(new.card_no,' ') or
	nvl(old.office_area_code1,' ') <> nvl(new.office_area_code1,' ') or
	nvl(old.office_tel_no1,' ')    <> nvl(new.office_tel_no1,' ') or
	nvl(old.office_tel_ext1,' ')   <> nvl(new.office_tel_ext1,' ') or
	nvl(old.office_area_code2,' ') <> nvl(new.office_area_code2,' ') or
	nvl(old.office_tel_no2,' ')    <> nvl(new.office_tel_no2,' ') or
	nvl(old.office_tel_ext2,' ')   <> nvl(new.office_tel_ext2,' ') or
	nvl(old.home_area_code1,' ')   <> nvl(new.home_area_code1,' ') or
	nvl(old.home_tel_no1,' ')      <> nvl(new.home_tel_no1,' ') or
	nvl(old.home_tel_ext1,' ')     <> nvl(new.home_tel_ext1,' ') or
	nvl(old.home_area_code2,' ')   <> nvl(new.home_area_code2,' ') or
	nvl(old.home_tel_no2,' ')      <> nvl(new.home_tel_no2,' ') or
	nvl(old.home_tel_ext2,' ')     <> nvl(new.home_tel_ext2,' ') or
	nvl(old.cellar_phone,' ')      <> nvl(new.cellar_phone,' ') or
	nvl(old.start_date,' ')        <> nvl(new.start_date,' ') or
	nvl(old.end_date,' ')          <> nvl(new.end_date,' ')
      )

BEGIN

  DECLARE  wk_audcode          VARCHAR(1);--
  DECLARE  wk_user             VARCHAR(10);--
  DECLARE  wk_pgm              VARCHAR(20);--
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--


--	select to_char(sysdate, 'YYYYMMDD-HH24MISSSSSSS') into wk_modtime from dual;--
	IF new.rela_type != '1' THEN
	    RETURN;--
	END IF;--

		set wk_audcode = 'U';--
		set wk_user = new.mod_user;--
		set wk_pgm = new.mod_pgm;--

	  INSERT INTO crd_rela_hist(
			            mod_time            ,
               		mod_pgm             ,
               		mod_user            ,
               		mod_audcode         ,
               		rela_type           ,
               		acno_p_seqno             ,
               		rela_id             ,
               		card_no             ,
               		sex                 ,
               		birthday            ,
               		id_p_seqno          ,
               		--id                  ,
               		--id_code             ,
               		acct_type           ,
               		--acct_key            ,
               		rela_name           ,
               		company_name        ,
               		company_zip         ,
               		company_addr1       ,
               		company_addr2       ,
               		company_addr3       ,
               		company_addr4       ,
               		company_addr5       ,
               		home_area_code1     ,
               		home_tel_no1        ,
               		home_tel_ext1       ,
               		home_area_code2     ,
               		home_tel_no2        ,
               		home_tel_ext2       ,
            			office_area_code1   ,
            			office_tel_no1      ,
            			office_tel_ext1     ,
            			office_area_code2   ,
            			office_tel_no2      ,
            			office_tel_ext2     ,
                     cellar_phone        ,
                     --bb_call             ,
               		mail_zip            ,
               		mail_addr1          ,
               		mail_addr2          ,
               		mail_addr3          ,
               		mail_addr4          ,
               		mail_addr5          ,
               		start_date          ,
               		end_date             
	 ) VALUES (
			            to_char(sysdate,'yyyymmddhh24miss'),
               		new.mod_pgm             ,
               		new.mod_user            ,
               		wk_audcode         ,
               		new.rela_type           ,
               		new.acno_p_seqno             ,
               		new.rela_id             ,
               		new.card_no             ,
               		new.sex                 ,
               		new.birthday            ,
               		new.id_p_seqno          ,
               		--new.id                  ,
               		--new.id_code             ,
               		new.acct_type           ,
               		--new.acct_key            ,
               		new.rela_name           ,
               		new.company_name        ,
               		new.company_zip         ,
               		new.company_addr1       ,
               		new.company_addr2       ,
               		new.company_addr3       ,
               		new.company_addr4       ,
               		new.company_addr5       ,
               		new.home_area_code1     ,
               		new.home_tel_no1        ,
               		new.home_tel_ext1       ,
               		new.home_area_code2     ,
               		new.home_tel_no2        ,
               		new.home_tel_ext2       ,
            			new.office_area_code1   ,
            			new.office_tel_no1      ,
            			new.office_tel_ext1     ,
            			new.office_area_code2   ,
            			new.office_tel_no2      ,
            			new.office_tel_ext2     ,
                     new.cellar_phone        ,
                     --new.bb_call             ,
               		new.mail_zip            ,
               		new.mail_addr1          ,
               		new.mail_addr2          ,
               		new.mail_addr3          ,
               		new.mail_addr4          ,
               		new.mail_addr5          ,
               		new.start_date          ,
               		new.end_date            );--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_RSK_COMM_PARM_BKEC_A
AFTER 
  INSERT 
ON RSK_COMM_PARM
REFERENCING OLD AS old NEW AS new
FOR EACH ROW MODE DB2SQL
WHEN (
        NVL(new.card_lost_cond,' ') <> NVL(old.card_lost_cond,' ') or
        NVL(new.card_oppo_cond,' ') <> NVL(old.card_oppo_cond,' ') or
        NVL(new.mcode_cond,' ') <> NVL(old.mcode_cond,' ') or
        NVL(new.bkec_block_cond,' ') <> NVL(old.bkec_block_cond,' ') or
        NVL(new.bkec_block_reason,' ') <> NVL(old.bkec_block_reason,' ') or
        NVL(new.auto_block_cond,' ') <> NVL(old.auto_block_cond,' ') or
        NVL(new.auto_block_reason,' ') <> NVL(old.auto_block_reason,' ') or
        nvl(new.payment_rate,' ') <> nvl(old.payment_rate,' ') or
        nvl(new.mcode_amt,' ') <> nvl(old.mcode_amt,' ')
      )

BEGIN

  DECLARE  ls_aud_code          VARCHAR(1);--
  DECLARE  ls_mod_user          VARCHAR(20);--
  DECLARE  ls_mod_pgm           VARCHAR(20);--
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

   if DELETING and nvl(old.parm_type,' ')<>'W_RSKM2230' then
      return;--
   end if;   --
   if (INSERTING or UPDATING) and nvl(new.parm_type,' ')<>'W_RSKM2230' then
      return;--
   end if;--
   --<<mod_xxx---------------------------------------------------------------
   set ls_mod_user = new.mod_user;--
   set ls_mod_pgm = new.mod_pgm;--

   -->>mod_xxx---------------------------------------------------------------   
	set ls_aud_code = 'A';--
	set ls_aud_code = 'U';--
	set ls_aud_code = 'D';--
   
     INSERT INTO rsk_parm_log  
            (   LOG_DATE  
             ,  LOG_TIME  
             ,  TABLE_NAME
             ,  AUD_CODE  
             ,  MOD_USER  
             ,  APR_USER  
             ,  MOD_PGM   
             ,  PARM_DATA 
      ) VALUES ( to_char(sysdate,'yyyymmdd')
             , to_char(sysdate,'hh24miss')
             , 'W_RSKM2230'   
             , ls_aud_code 
             , new.mod_user
             , new.apr_user
             , new.mod_pgm
             , new.card_lost_cond||';'||new.card_oppo_cond
             ||';'||new.mcode_cond||';'||new.payment_rate||';'||to_char(new.mcode_amt)
             ||';'||new.bkec_block_cond||';'||new.bkec_block_reason
             ||';'||new.auto_block_cond||';'||new.auto_block_reason
      ) ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_RSK_COMM_PARM_BKEC_D
AFTER 
  DELETE 
ON RSK_COMM_PARM
REFERENCING OLD AS old NEW AS new
FOR EACH ROW MODE DB2SQL
WHEN (
        NVL(new.card_lost_cond,' ') <> NVL(old.card_lost_cond,' ') or
        NVL(new.card_oppo_cond,' ') <> NVL(old.card_oppo_cond,' ') or
        NVL(new.mcode_cond,' ') <> NVL(old.mcode_cond,' ') or
        NVL(new.bkec_block_cond,' ') <> NVL(old.bkec_block_cond,' ') or
        NVL(new.bkec_block_reason,' ') <> NVL(old.bkec_block_reason,' ') or
        NVL(new.auto_block_cond,' ') <> NVL(old.auto_block_cond,' ') or
        NVL(new.auto_block_reason,' ') <> NVL(old.auto_block_reason,' ') or
        nvl(new.payment_rate,' ') <> nvl(old.payment_rate,' ') or
        nvl(new.mcode_amt,' ') <> nvl(old.mcode_amt,' ')
      )

BEGIN

  DECLARE  ls_aud_code          VARCHAR(1);--
  DECLARE  ls_mod_user          VARCHAR(20);--
  DECLARE  ls_mod_pgm           VARCHAR(20);--
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

   if DELETING and nvl(old.parm_type,' ')<>'W_RSKM2230' then
      return;--
   end if;   --
   if (INSERTING or UPDATING) and nvl(new.parm_type,' ')<>'W_RSKM2230' then
      return;--
   end if;--
   --<<mod_xxx---------------------------------------------------------------
   set ls_mod_user = new.mod_user;--
   set ls_mod_pgm = new.mod_pgm;--

   -->>mod_xxx---------------------------------------------------------------   
	set ls_aud_code = 'D';--
   
     INSERT INTO rsk_parm_log  
            (   LOG_DATE  
             ,  LOG_TIME  
             ,  TABLE_NAME
             ,  AUD_CODE  
             ,  MOD_USER  
             ,  APR_USER  
             ,  MOD_PGM   
             ,  PARM_DATA 
      ) VALUES ( to_char(sysdate,'yyyymmdd')
             , to_char(sysdate,'hh24miss')
             , 'W_RSKM2230'   
             , ls_aud_code 
             , ls_mod_user
             , ls_mod_user
             , ls_mod_pgm
             , old.card_lost_cond||';'||old.card_oppo_cond
             ||';'||old.mcode_cond||';'||old.payment_rate||';'||to_char(old.mcode_amt)
             ||';'||old.bkec_block_cond||';'||old.bkec_block_reason
             ||';'||old.auto_block_cond||';'||old.auto_block_reason
      ) ;--
  
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_RSK_COMM_PARM_BKEC_U
AFTER 
   UPDATE OF
     CARD_LOST_COND, CARD_OPPO_COND, MCODE_COND
	, BKEC_BLOCK_COND, BKEC_BLOCK_REASON
	, AUTO_BLOCK_COND, AUTO_BLOCK_REASON
	, PAYMENT_RATE, MCODE_AMT 
ON RSK_COMM_PARM
REFERENCING OLD AS old NEW AS new
FOR EACH ROW MODE DB2SQL
WHEN (
        NVL(new.card_lost_cond,' ') <> NVL(old.card_lost_cond,' ') or
        NVL(new.card_oppo_cond,' ') <> NVL(old.card_oppo_cond,' ') or
        NVL(new.mcode_cond,' ') <> NVL(old.mcode_cond,' ') or
        NVL(new.bkec_block_cond,' ') <> NVL(old.bkec_block_cond,' ') or
        NVL(new.bkec_block_reason,' ') <> NVL(old.bkec_block_reason,' ') or
        NVL(new.auto_block_cond,' ') <> NVL(old.auto_block_cond,' ') or
        NVL(new.auto_block_reason,' ') <> NVL(old.auto_block_reason,' ') or
        nvl(new.payment_rate,' ') <> nvl(old.payment_rate,' ') or
        nvl(new.mcode_amt,' ') <> nvl(old.mcode_amt,' ')
      )

BEGIN

  DECLARE  ls_aud_code          VARCHAR(1);--
  DECLARE  ls_mod_user          VARCHAR(20);--
  DECLARE  ls_mod_pgm           VARCHAR(20);--
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

   if DELETING and nvl(old.parm_type,' ')<>'W_RSKM2230' then
      return;--
   end if;   --
   if (INSERTING or UPDATING) and nvl(new.parm_type,' ')<>'W_RSKM2230' then
      return;--
   end if;--
   --<<mod_xxx---------------------------------------------------------------
   set ls_mod_user = new.mod_user;--
   set ls_mod_pgm = new.mod_pgm;--

   -->>mod_xxx---------------------------------------------------------------   
	set ls_aud_code = 'U';--
   
     INSERT INTO rsk_parm_log  
            (   LOG_DATE  
             ,  LOG_TIME  
             ,  TABLE_NAME
             ,  AUD_CODE  
             ,  MOD_USER  
             ,  APR_USER  
             ,  MOD_PGM   
             ,  PARM_DATA 
      ) VALUES ( to_char(sysdate,'yyyymmdd')
             , to_char(sysdate,'hh24miss')
             , 'W_RSKM2230'   
             , ls_aud_code 
             , new.mod_user
             , new.apr_user
             , new.mod_pgm
             , new.card_lost_cond||';'||new.card_oppo_cond
             ||';'||new.mcode_cond||';'||new.payment_rate||';'||to_char(new.mcode_amt)
             ||';'||new.bkec_block_cond||';'||new.bkec_block_reason
             ||';'||new.auto_block_cond||';'||new.auto_block_reason
      ) ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER tr_rsk_trial_parm2_u
AFTER UPDATE
ON RSK_TRIAL_PARM2
REFERENCING NEW AS new OLD AS old
FOR EACH ROW MODE DB2SQL
WHEN (
   nvl(new.apr_flag,'N') = 'Y'
      )
--==============================================================================
--V.2018-0731:    JH    initial
--V.2018-0809:    JH    modify
--==============================================================================
BEGIN
DECLARE ll_cnt integer;--
declare ls_aud_flag varchar(1);--
declare ls_mod_user varchar(10);--
declare ldt_mod_time timestamp;--
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

   set ls_aud_flag = '';--
   set ll_cnt      =0;--

   set ls_aud_flag = 'U';--
   set ls_mod_user = old.mod_user;--
   set ldt_mod_time = old.mod_time;--

     INSERT INTO rsk_trial_parm2_log ( 
              log_date,
              log_time,
              aud_flag,
              risk_group,
              risk_group_desc,
              riskgp_remark,
              dbr_cond,
              dbr_s,
              dbr_e,
              no_assure_cond,
              no_assure_amt_s,
              no_assure_amt_e,
              k34_estimate_rcbal_cond,
              k34_estimate_rcbal_s,
              k34_estimate_rcbal_e,
              k34_use_rc_rate_cond,
              k34_use_rc_rate_s,
              k34_use_rc_rate_e,
              k34_overdue_cond,
              k34_overdue_flag,
              k34_overdue_banks_cond,
              k34_overdue_banks_s,
              k34_overdue_banks_e,
              k34_overdue_6mm_cond,
              k34_overdue_6mm_s,
              k34_overdue_6mm_e,
              k34_overdue_12mm_cond,
              k34_overdue_12mm_s,
              k34_overdue_12mm_e,
              k34_use_cash_cond,
              k34_use_cash_flag,
              k34_use_cash_6mm_cond,
              k34_use_cash_6mm_s,
              k34_use_cash_6mm_e,
              k34_use_cash_12mm_cond,
              k34_use_cash_12mm_s,
              k34_use_cash_12mm_e,
              k34_debt_code_cond,
              k34_debt_code,
              b63_no_overdue_amt_cond,
              b63_no_overdue_amt_s,
              b63_no_overdue_amt_e,
              b63_overdue_cond,
              b63_overdue_flag,
              b63_overdue_nopay_cond,
              b63_overdue_nopay_s,
              b63_overdue_nopay_e,
              b63_cash_due_amt_cond,
              b63_cash_due_amt_s,
              b63_cash_due_amt_e,
              credit_limit_cond,
              credit_limit_s_date,
              credit_limit_e_date,
              credit_limit_code,
              rc_avguse_cond,
              rc_avguse_mm,
              rc_avguse_rate,
              cash_use_cond,
              cash_use_mm,
              cash_use_times,
              limit_avguse_cond,
              limit_avguse_mm,
              limit_avguse_rate,
              payment_rate_cond,
              payment_rate_mm,
              payment_rate_times,
              no_debt_cond,
              no_debt_mm,
              payment_int_cond,
              acct_jrnl_bal_cond,
              acct_jrnl_bal_s,
              acct_jrnl_bal_e,
              trial_score_cond  ,  
              trial_score_s     ,
              trial_score_e     ,
              no_assure_add_cond,
              no_assure_add_amt ,
              no_assure_add_amt2,
              jcic028_cond   ,   
              jcic028_s      ,   
              jcic028_e      ,   
              jcic029_cond   ,   
              jcic029_s      ,   
              jcic029_e      ,   
              jcic036_cond   ,   
              jcic036        ,   
              jcic030_cond   ,   
              jcic030        ,   
              jcic031_cond   ,   
              jcic031        ,   
              jcic023_03_cond   ,   
              jcic023_03        ,   
              jcic025_01_cond   ,   
              jcic025_01        ,   
              jcic030_01_cond   ,   
              jcic030_01        ,   
              jcic030_02_cond   ,   
              jcic030_02        ,   
              jcic031_01_cond   ,   
              jcic031_01        ,   
              jcic031_02_cond   ,   
              jcic031_02        ,   
              jcic034_cond   ,   
              jcic034        ,   
              jcic032_cond   ,   
              jcic032        ,   
              jcic004_01_cond,   
              jcic004_01     ,   
              jcic009_cond   ,   
              jcic009        ,   
              jcic010_02_cond,   
              jcic010_02     ,   
              jcic013_cond   ,   
              jcic013        ,   
              jcic023_01_cond,   
              jcic023_01     ,   
              jcic023_02_cond,   
              jcic023_02,
              jcic023_03_e,
              jcic025_01_e,
              jcic004_01_e,
              jcic009_e   ,
              jcic010_02_e,
              jcic023_01_e,
              jcic023_02_e, 
              crt_user,
              crt_date,
              apr_flag,
              apr_date,
              apr_user,
              mod_user,
              mod_time,
              mod_pgm,
              mod_seqno 
   ) VALUES ( to_char(sysdate,'yyyymmdd'),
              to_char(sysdate,'hh24miss'),
              ls_aud_flag,
              new.risk_group,
              new.risk_group_desc,
              new.rskgp_remark,
              new.dbr_cond,
              new.dbr_s,
              new.dbr_e,
              new.no_assure_cond,
              new.no_assure_amt_s,
              new.no_assure_amt_e,
              new.k34_estimate_rcbal_cond,
              new.k34_estimate_rcbal_s,
              new.k34_estimate_rcbal_e,
              new.k34_use_rc_rate_cond,
              new.k34_use_rc_rate_s,
              new.k34_use_rc_rate_e,
              new.k34_overdue_cond,
              new.k34_overdue_flag,
              new.k34_overdue_banks_cond,
              new.k34_overdue_banks_s,
              new.k34_overdue_banks_e,
              new.k34_overdue_6mm_cond,
              new.k34_overdue_6mm_s,
              new.k34_overdue_6mm_e,
              new.k34_overdue_12mm_cond,
              new.k34_overdue_12mm_s,
              new.k34_overdue_12mm_e,
              new.k34_use_cash_cond,
              new.k34_use_cash_flag,
              new.k34_use_cash_6mm_cond,
              new.k34_use_cash_6mm_s,
              new.k34_use_cash_6mm_e,
              new.k34_use_cash_12mm_cond,
              new.k34_use_cash_12mm_s,
              new.k34_use_cash_12mm_e,
              new.k34_debt_code_cond,
              new.k34_debt_code,
              new.b63_no_overdue_amt_cond,
              new.b63_no_overdue_amt_s,
              new.b63_no_overdue_amt_e,
              new.b63_overdue_cond,
              new.b63_overdue_flag,
              new.b63_overdue_nopay_cond,
              new.b63_overdue_nopay_s,
              new.b63_overdue_nopay_e,
              new.b63_cash_due_amt_cond,
              new.b63_cash_due_amt_s,
              new.b63_cash_due_amt_e,
              new.credit_limit_cond,
              new.credit_limit_s_date,
              new.credit_limit_e_date,
              new.credit_limit_code,
              new.rc_avguse_cond,
              new.rc_avguse_mm,
              new.rc_avguse_rate,
              new.cash_use_cond,
              new.cash_use_mm,
              new.cash_use_times,
              new.limit_avguse_cond,
              new.limit_avguse_mm,
              new.limit_avguse_rate,
              new.payment_rate_cond,
              new.payment_rate_mm,
              new.payment_rate_times,
              new.no_debt_cond,
              new.no_debt_mm,
              new.payment_int_cond,
              new.acct_jrnl_bal_cond,
              new.acct_jrnl_bal_s,
              new.acct_jrnl_bal_e,
              new.trial_score_cond  ,  
              new.trial_score_s     ,
              new.trial_score_e     ,
              new.no_assure_add_cond,
              new.no_assure_add_amt ,
              new.no_assure_add_amt2,
              new.jcic028_cond   ,   
              new.jcic028_s      ,   
              new.jcic028_e      ,   
              new.jcic029_cond   ,   
              new.jcic029_s      ,   
              new.jcic029_e      ,   
              new.jcic036_cond   ,   
              new.jcic036        ,   
              new.jcic030_cond   ,   
              new.jcic030        ,   
              new.jcic031_cond   ,   
              new.jcic031        ,   
              new.jcic023_03_cond   ,   
              new.jcic023_03        ,   
              new.jcic025_01_cond   ,   
              new.jcic025_01        ,   
              new.jcic030_01_cond   ,   
              new.jcic030_01        ,   
              new.jcic030_02_cond   ,   
              new.jcic030_02        ,   
              new.jcic031_01_cond   ,   
              new.jcic031_01        ,   
              new.jcic031_02_cond   ,   
              new.jcic031_02        ,   
              new.jcic034_cond   ,   
              new.jcic034        ,   
              new.jcic032_cond   ,   
              new.jcic032        ,   
              new.jcic004_01_cond,   
              new.jcic004_01     ,   
              new.jcic009_cond   ,   
              new.jcic009        ,   
              new.jcic010_02_cond,   
              new.jcic010_02     ,   
              new.jcic013_cond   ,   
              new.jcic013        ,   
              new.jcic023_01_cond,   
              new.jcic023_01     ,   
              new.jcic023_02_cond,   
              new.jcic023_02,
              new.jcic023_03_e,
              new.jcic025_01_e,
              new.jcic004_01_e,
              new.jcic009_e   ,
              new.jcic010_02_e,
              new.jcic023_01_e,
              new.jcic023_02_e,
              new.crt_user,
              new.crt_date,
              new.apr_flag,
              new.apr_date,
              new.apr_user,
              ls_mod_user,
              ldt_mod_time,
              new.mod_pgm,
              new.mod_seqno 
         ) ;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_SMS_MSG_ID_A AFTER
INSERT
    ON
    SMS_MSG_ID REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime DATE;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    log_sms_msg_id ( mod_aud_code,
    mod_time,
    mod_user,
    mod_pgm,
    msg_pgm,
    msg_dept,
    msg_serve,
    msg_id,
    msg_desc,
    msg_send_flag,
    msg_userid,
    ACCT_TYPE_SEL,
    --msg_acct_type,
 msg_sel_amt01,
    msg_amt01,
    msg_run_day )
VALUES ( wk_audcode,
wk_modtime,
new.mod_user,
new.mod_pgm,
new.msg_pgm,
new.msg_dept,
new.msg_serve,
new.msg_id,
new.msg_desc,
new.msg_send_flag,
new.msg_userid,
new.ACCT_TYPE_SEL,
--new.msg_acct_type,
 new.msg_sel_amt01,
new.msg_amt01,
new.msg_run_day ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_SMS_MSG_ID_D AFTER
DELETE
    ON
    SMS_MSG_ID REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime DATE;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    log_sms_msg_id ( mod_aud_code,
    mod_time,
    mod_user,
    mod_pgm,
    msg_pgm,
    msg_dept,
    msg_serve,
    msg_id,
    msg_desc,
    msg_send_flag,
    msg_userid,
    ACCT_TYPE_SEL,
    --msg_acct_type,
 msg_sel_amt01,
    msg_amt01,
    msg_run_day )
VALUES ( wk_audcode,
wk_modtime,
old.mod_user,
old.mod_pgm,
old.msg_pgm,
old.msg_dept,
old.msg_serve,
old.msg_id,
old.msg_desc,
old.msg_send_flag,
old.msg_userid,
old.acct_type_sel,
--old.msg_acct_type,
 old.msg_sel_amt01,
old.msg_amt01,
old.msg_run_day ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_SMS_MSG_ID_U AFTER
UPDATE
    OF mod_user,
    mod_pgm,
    msg_pgm,
    msg_dept,
    msg_userid,
    msg_serve,
    msg_id,
    msg_desc,
    msg_send_flag,
    MSG_SEL_AMT01,
    MSG_AMT01,
    MSG_RUN_DAY,
    ACCT_TYPE_SEL ON
    SMS_MSG_ID REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_modtime DATE;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    log_sms_msg_id ( mod_aud_code,
    mod_time,
    mod_user,
    mod_pgm,
    msg_pgm,
    msg_dept,
    msg_serve,
    msg_id,
    msg_desc,
    msg_send_flag,
    msg_userid,
    ACCT_TYPE_SEL,
    --msg_acct_type,
 msg_sel_amt01,
    msg_amt01,
    msg_run_day )
VALUES ( wk_audcode,
wk_modtime,
new.mod_user,
new.mod_pgm,
new.msg_pgm,
new.msg_dept,
new.msg_serve,
new.msg_id,
new.msg_desc,
new.msg_send_flag,
new.msg_userid,
new.ACCT_TYPE_SEL,
--new.msg_acct_type,
 new.msg_sel_amt01,
new.msg_amt01,
new.msg_run_day ) ;--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_TSC_BLEC_EXPT_LOG_D AFTER
DELETE
    ON
    TSC_BKEC_EXPT REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE ls_pgm_type VARCHAR(1);--
--
 DECLARE ls_mod_audcode VARCHAR(1);--
--
 DECLARE ls_mod_user VARCHAR(20);--
--
 DECLARE ls_mod_pgm VARCHAR(20);--
--
 DECLARE ls_mod_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 /******************************************************************************
   NAME  !?1C￠Dd?ucw?A|W3a 2!±￠XE￠XO?y     
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2009/1/20   JHW              1. CREATE OR REPLACEd this trigger.
   2.0.1      2012/4/24   JHW              R100-034
******************************************************************************/
SET
ls_mod_user = new.mod_user;--
--
 SET
ls_mod_pgm = new.mod_pgm;--
--
 SET
ls_mod_audcode = 'D';--
--
 INSERT
    INTO
    tsc_bkec_expt_log ( mod_date,
    mod_time,
    mod_user,
    mod_pgm,
    mod_audcode,
    tsc_card_no,
    card_no,
    black_date,
    black_user_id,
    black_remark,
    black_flag,
    sEND_date_s,
    sEND_date_e,
    from_type,
    apr_date,
    apr_user )
VALUES ( to_char(sysdate,
'yyyymmdd'),
to_char(sysdate,
'hh24miss'),
ls_mod_user,
ls_mod_pgm,
ls_mod_audcode,
old.tsc_card_no,
old.card_no,
old.black_date,
old.black_user_id,
old.black_remark,
old.black_flag,
old.sEND_date_s,
old.sEND_date_e,
old.from_type,
to_char(sysdate,
'yyyymmdd'),
old.apr_user );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_TSC_BLEC_EXPT_LOG_U AFTER
UPDATE
    ON
    TSC_BKEC_EXPT REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE ls_pgm_type VARCHAR(1);--
--
 DECLARE ls_mod_audcode VARCHAR(1);--
--
 DECLARE ls_mod_user VARCHAR(20);--
--
 DECLARE ls_mod_pgm VARCHAR(20);--
--
 DECLARE ls_mod_ws VARCHAR(20);--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 /******************************************************************************
   NAME  !?1C￠Dd?ucw?A|W3a 2!±￠XE￠XO?y     
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2009/1/20   JHW              1. CREATE OR REPLACEd this trigger.
   2.0.1      2012/4/24   JHW              R100-034
******************************************************************************/
SET
ls_mod_user = new.mod_user;--
--
 SET
ls_mod_pgm = new.mod_pgm;--
--
 SET
ls_mod_audcode = 'U';--
--
 INSERT
    INTO
    tsc_bkec_expt_log ( mod_date,
    mod_time,
    mod_user,
    mod_pgm,
    mod_audcode,
    tsc_card_no,
    card_no,
    black_date,
    black_user_id,
    black_remark,
    black_flag,
    sEND_date_s,
    sEND_date_e,
    from_type,
    apr_date,
    apr_user )
VALUES ( to_char(sysdate,
'yyyymmdd'),
to_char(sysdate,
'hh24miss'),
ls_mod_user,
ls_mod_pgm,
ls_mod_audcode,
new.tsc_card_no,
new.card_no,
new.black_date,
new.black_user_id,
new.black_remark,
new.black_flag,
new.sEND_date_s,
new.sEND_date_e,
new.from_type,
new.apr_date,
new.apr_user );--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_UPD_CRD_CARD_PP_U 
  AFTER UPDATE OF CURRENT_CODE
  ON CRD_CARD 
  REFERENCING 
    OLD AS OLD NEW AS NEW
  FOR EACH ROW
  WHEN (
      ( old.current_code = '0' and new.current_code in('1','3')
      ) 
   OR ( new.current_code ='0' and old.current_code in('1','3')
      )
     ) 
BEGIN

DECLARE wk_count         INTEGER;--
DECLARE wk_count2        INTEGER;--
DECLARE wk_count3        INTEGER;--
  --PRAGMA AUTONOMOUS_TRANSACTION;--
/******************************************************************************
   NAME  卡片停卡 update CRD_CARD_PP
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0   2015/6/2     JAMES           1. program initial.
   1.0.1 2016/09/20   WILSON          1. 停用原因AI不停PP卡
******************************************************************************/

   /* 此trigger 查詢到自己的table, 因此用PRAGMA AUTONOMOUS_TRANSACTION
      當歸戶後只剩一筆有效卡時,update crd_card_pp, trigger完即無有效卡  */
   SELECT COUNT(*)
     INTO wk_count
     FROM mkt_ppcard_apply
    WHERE card_type = new.card_type;--

   IF wk_count > 0 THEN
      IF old.current_code= '0' AND new.current_code in ('1','3')  THEN
     --如果條件 current_code = '1' 且 oppost_reason = 'AI' 的卡不存在，才執行連動停用pp卡*/                                         
   
      SELECT COUNT(*) INTO wk_count3
        FROM crd_card
       WHERE current_code  = '1'
         AND card_type    in (SELECT card_type from mkt_ppcard_apply)
         AND id_p_seqno    = old.id_p_seqno
         AND oppost_reason = 'AI'       
         AND reissue_status in ('','1','2','4');--

      SELECT COUNT(*) INTO wk_count2
        FROM crd_card
       WHERE current_code = '0'
         AND card_type   in (SELECT card_type from mkt_ppcard_apply)
         AND id_p_seqno   = new.id_p_seqno
         AND new.oppost_reason <> 'AI';--

      IF wk_count2 = 1 AND wk_count3 = 0 THEN
         UPDATE CRD_CARD_PP
            SET CURRENT_CODE  = '1',
                OPPOST_REASON = 'P3',
                OPPOST_DATE   = new.oppost_date,
                mod_time      = sysdate,
                mod_pgm       = 'trigger'
          WHERE id_p_seqno  = new.id_p_seqno
            AND CURRENT_CODE = '0';--
      END IF;--
   ELSEIF new.current_code ='0' AND old.current_code in ('1','3') THEN
    SELECT COUNT(*) INTO wk_count2
      FROM crd_card
     WHERE current_code = '0'
       AND card_type   in (SELECT card_type from mkt_ppcard_apply)
       AND id_p_seqno   = new.id_p_seqno
       AND old.oppost_reason <> 'AI';--

    IF wk_count2 = 0 THEN
       UPDATE CRD_CARD_PP
          SET CURRENT_CODE  = '0',
              OPPOST_REASON = '',
              OPPOST_DATE   = '',
              mod_time      = sysdate,
              mod_pgm       = 'trigger'
        WHERE id_p_seqno    = new.id_p_seqno
          AND CURRENT_CODE  = '1'
          AND oppost_date   = old.oppost_date;--
     END IF; --wk_count2
    END IF; --else
   END IF;  --wk_count

--  commit;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_UPD_MARKET_AGREE_U
AFTER 
   UPDATE OF current_code
ON CRD_CARD
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW MODE DB2SQL
WHEN (
       NVL(old.current_code,'0')='0'
   and NVL(new.current_code,'0')<>NVL(old.current_code,'0')
      )
/******************************************************************************
   NAME  卡片停卡 update CRD_IDNO
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2010/06/22  林志鴻           1. program initial.
******************************************************************************/
BEGIN

   IF (new.sup_flag = '0') THEN
   update crd_idno
      set market_agree_base ='N'
         ,market_agree_act  ='N'
         ,mod_user     ='trigger'
         ,mod_pgm      ='tr_upd_market_agree'
         ,mod_time     =sysdate
         ,mod_seqno    =nvl(mod_seqno,0)+1
   where id_p_seqno = old.id_p_seqno;--
   END IF;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_UPD_TSC_CARD_U
AFTER UPDATE
 OF current_code
 ON CRD_CARD
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW MODE DB2SQL
WHEN (
      nvl(old.current_code,0) <> nvl(new.current_code,0)
     )
	  
BEGIN

DECLARE   wk_emboss_rsn    varchar(2);--
DECLARE   wk_ich_card_no   varchar(20);--
DECLARE   wk_emboss_source varchar(1);--
DECLARE   wk_emboss_reason varchar(1);--

  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
--declare exit handler for sqlstate '02000'   
  declare exit handler for SQLEXCEPTION, SQLWARNING
          signal   sqlstate '38S02' set message_text='Error : tr_upd_tsc_card_u Error.';--

/******************************************************************************
   NAME  卡片停卡 update TSC_CARD
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2009/3/13   JHW              1. program initial.
   1.0.1      2009/3/17   JHW              1. rule change
   1.1.1      2014/5/12   Alan             1. current_code  = old.current_code
   1.2        2016/5/12   來哥             新增一卡通
******************************************************************************/

   update tsc_card
      set current_code = new.current_code
         ,oppost_date  = new.oppost_date
         ,mod_user     = 'trigger'
         ,mod_pgm      = 'tr_upd_tsc_card'
         ,mod_time     = sysdate
         ,mod_seqno    = nvl(mod_seqno,0)+1
   where card_no       = old.card_no
     and current_code  = old.current_code;--

   update ips_card
      set current_code     = new.current_code
         ,ips_oppost_date  = new.oppost_date
         ,mod_user         = 'trigger'
         ,mod_pgm          = 'tr_upd_tsc_card'
         ,mod_time         = sysdate
         ,mod_seqno        = nvl(mod_seqno,0)+1
   where card_no           = old.card_no
     and current_code      = old.current_code;--

   set wk_ich_card_no = '';--
   SELECT a.ich_card_no
     INTO wk_ich_card_no
     FROM ich_card a
    where a.card_no    = old.card_no
      and a.new_beg_date in ( select max(new_beg_date)
                                from ich_card c
                               where c.card_no   = old.card_no)
    fetch first 1 rows only;--

   update ich_card
      set current_code     = new.current_code
         ,ich_oppost_date  = new.oppost_date
         ,mod_user         = 'trigger'
         ,mod_pgm          = 'tr_upd_tsc_card'
         ,mod_time         = sysdate
         ,mod_seqno        = nvl(mod_seqno,0)+1
   where card_no           = old.card_no
     and current_code      = old.current_code;--

   set wk_emboss_source = '';--
   set wk_emboss_reason = '';--
   SELECT a.emboss_source , a.emboss_reason
     INTO wk_emboss_source, wk_emboss_reason
     FROM crd_emboss a
    where a.card_no    = new.card_no
      and a.valid_to  in ( select max(valid_to)
                             from crd_emboss c
                            where c.card_no   = new.card_no)
    fetch first 1 rows only;--

   /* 01:?��?璈��??����-?∠??��?  02:?��?璈��??����-?臬??∪??? 04:?∠??唳? */
   set wk_emboss_rsn  = '';--
   if wk_emboss_source in ('5')         and nvl(wk_emboss_reason,' ') in ('2')  then
      set wk_emboss_rsn  = '01';--
   end if;--
   if nvl(new.oppost_date  ,' ') <> ' ' and new.oppost_date <> ''       then
      set wk_emboss_rsn  = '02';--
   end if;--
   if nvl(new.oppost_reason,' ') in ('R3','B3')  then
      set wk_emboss_rsn  = '04';--
   end if;--

   INSERT INTO ich_b09b_bal
         (ich_card_no,
          card_no,
          bal_rsn,
          loss_date,
          loss_time,
          sys_date,
          sys_time,
          proc_flag,
          ok_flag,
          mod_time,
          mod_pgm )
       VALUES
         (wk_ich_card_no,
          old.card_no,
          wk_emboss_rsn,
          to_char(sysdate, 'YYYYMMDD'),
          to_char(sysdate, 'HH24MISS'),
          to_char(sysdate, 'YYYYMMDD'),
          to_char(sysdate, 'HH24MISS'),
          'N',
          'N',
          sysdate,
          'tr_upd_tsc_card');--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_VD_IDNO_TONCCC_A AFTER
INSERT
    ON
    DBC_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'A';--
--
 INSERT
    INTO
    dbc_idno_tonccc( mod_time,
    mod_pgm,
    mod_audcode,
    id_p_seqno,
    sex,
    birthday,
    education,
    annual_income,
    business_code,
    resident_zip)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
wk_audcode,
new.id_p_seqno,
new.sex,
new.birthday,
new.education,
new.annual_income,
new.business_code,
new.resident_zip);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_VD_IDNO_TONCCC_D AFTER
DELETE
    ON
    DBC_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'D';--
--
 INSERT
    INTO
    dbc_idno_tonccc( mod_time,
    mod_pgm,
    mod_audcode,
    id_p_seqno,
    sex,
    birthday,
    education,
    annual_income,
    business_code,
    resident_zip)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
old.mod_pgm,
wk_audcode,
old.id_p_seqno,
old.sex,
old.birthday,
old.education,
old.annual_income,
old.business_code,
old.resident_zip);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_VD_IDNO_TONCCC_U AFTER
UPDATE
    OF sex,
    birthday,
    annual_income,
    business_code,
    education,
    resident_zip ON
    DBC_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.sex,
    ' ' ) <> nvl(new.sex,
    ' ')
    OR nvl(old.birthday,
    ' ') <> nvl(new.birthday,
    ' ')
    OR nvl(old.annual_income,
    0) <> nvl(new.annual_income,
    0)
    OR nvl(old.business_code,
    ' ') <> nvl(new.business_code,
    ' ')
    OR nvl(old.education,
    ' ') <> nvl(new.education,
    ' ')
    OR nvl(old.resident_zip,
    ' ') <> nvl(new.resident_zip,
    ' ') )
BEGIN
        DECLARE wk_audcode VARCHAR(1);--
--
 DECLARE wk_user VARCHAR(10);--
--
 DECLARE wk_pgm VARCHAR(20);--
--
 DECLARE wk_ws VARCHAR(20);--
--
 DECLARE wk_count INTEGER;--
--
 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION,
SQLWARNING
BEGIN
    RETURN;--
END;--
--
 SET
wk_audcode = 'U';--
--
 INSERT
    INTO
    dbc_idno_tonccc ( mod_time,
    mod_pgm,
    mod_audcode,
    id_p_seqno,
    sex,
    birthday,
    education,
    annual_income,
    business_code,
    resident_zip)
VALUES ( to_char(sysdate,
'yyyymmddhh24miss'),
new.mod_pgm,
wk_audcode,
new.id_p_seqno,
new.sex,
new.birthday,
new.education,
new.annual_income,
new.business_code,
new.resident_zip);--
--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_VIP_CODE_LOG_A
AFTER 
   INSERT 
ON ACT_ACNO
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL
WHEN (
      NVL(old.VIP_CODE,' ')<>NVL(new.VIP_CODE,' ')
 OR   NVL(old.VIP_REMARK,' ')<>NVL(new.VIP_REMARK,' ')
     )

BEGIN
   DECLARE tmpVar INT;--
	DECLARE ls_pgm_type    varchar(1);--
	DECLARE ls_mod_user    varchar(20);--
	DECLARE ls_mod_pgm     varchar(20);--
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

   set ls_pgm_type ='';--
   set ls_mod_user ='';--
   set ls_mod_pgm  ='';--

   if ls_mod_user is NULL then
      set ls_mod_user = new.mod_user;--
   end if;--
   if ls_mod_pgm is NULL then
      set ls_mod_pgm = new.mod_pgm;--
   end if;--

   INSERT INTO act_vip_code_log
            ( log_date,
              log_time,
              log_type,
              p_seqno,
              acct_type,
              id_p_seqno,
              vip_code,
              vip_remark,
              mod_user,
              apr_user )
  VALUES    ( to_char(sysdate,'yyyymmdd'),
              to_char(sysdate,'hh24miss'),
              ls_pgm_type,
              new.p_seqno,
              new.acct_type,
              new.id_p_seqno,
              new.vip_code,
              new.vip_remark,
              decode(ls_pgm_type,'1',new.update_user,ls_mod_user),
              decode(ls_pgm_type,'1',new.mod_user,ls_mod_user)
            )  ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_VIP_CODE_LOG_U
AFTER 
   UPDATE OF VIP_CODE, VIP_REMARK
ON ACT_ACNO
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW MODE DB2SQL
WHEN (
      NVL(old.VIP_CODE,' ')<>NVL(new.VIP_CODE,' ')
 OR   NVL(old.VIP_REMARK,' ')<>NVL(new.VIP_REMARK,' ')
     )

BEGIN
   DECLARE tmpVar INT;--
	DECLARE ls_pgm_type    varchar(1);--
	DECLARE ls_mod_user    varchar(20);--
	DECLARE ls_mod_pgm     varchar(20);--
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--

   set ls_pgm_type ='';--
   set ls_mod_user ='';--
   set ls_mod_pgm  ='';--

   if ls_mod_user is NULL then
      set ls_mod_user = new.mod_user;--
   end if;--
   if ls_mod_pgm is NULL then
      set ls_mod_pgm = new.mod_pgm;--
   end if;--

   INSERT INTO act_vip_code_log
            ( log_date,
              log_time,
              log_type,
              p_seqno,
              acct_type,
              id_p_seqno,
              vip_code,
              vip_remark,
              mod_user,
              apr_user )
  VALUES    ( to_char(sysdate,'yyyymmdd'),
              to_char(sysdate,'hh24miss'),
              ls_pgm_type,
              new.p_seqno,
              new.acct_type,
              new.id_p_seqno,
              new.vip_code,
              new.vip_remark,
              decode(ls_pgm_type,'1',new.update_user,ls_mod_user),
              decode(ls_pgm_type,'1',new.mod_user,ls_mod_user)
            )  ;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_WH_HIST_U AFTER
UPDATE
    ON
    CRD_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(OLD.chi_name,
    ' ') <> NVL(NEW.chi_name,
    ' ')
    OR NVL(OLD.sex,
    ' ') <> NVL(NEW.sex,
    ' ')
    OR NVL(OLD.Birthday,
    ' ') <> NVL(NEW.Birthday,
    ' ')
    OR NVL(OLD.e_mail_addr,
    ' ') <> NVL(NEW.e_mail_addr,
    ' ')
    OR NVL(OLD.home_tel_no1,
    ' ') <> NVL(NEW.home_tel_no1,
    ' ')
    OR NVL(OLD.home_tel_no2,
    ' ') <> NVL(NEW.home_tel_no2,
    ' ')
    OR NVL(OLD.office_tel_no1,
    ' ') <> NVL(NEW.office_tel_no1,
    ' ')
    OR NVL(OLD.cellar_phone,
    ' ') <> NVL(NEW.cellar_phone,
    ' ')
    OR NVL(OLD.resident_addr1,
    ' ') <> NVL(NEW.resident_addr1,
    ' ')
    OR NVL(OLD.business_code,
    ' ') <> NVL(NEW.business_code,
    ' ')
    OR NVL(OLD.education,
    ' ') <> NVL(NEW.education,
    ' ')
    OR NVL(OLD.annual_income,
    NULL) <> NVL(NEW.annual_income,
    NULL)
    OR NVL(OLD.marriage,
    ' ') <> NVL(NEW.marriage,
    ' ') )
BEGIN
        INSERT
    INTO
    CRD_WH_HST(
    --id,
 id_p_seqno,
    mod_time,
    mod_pgm )
VALUES (
--NEW.id,
 NEW.id_p_seqno,
TO_CHAR(SYSDATE,
'yyyymmddhh24miss'),
NEW.mod_pgm );--
--
END;


COMMIT WORK;

CONNECT RESET;

TERMINATE;


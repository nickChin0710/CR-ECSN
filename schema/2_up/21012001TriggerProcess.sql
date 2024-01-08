connect to cr;

DROP TRIGGER ECSCRDB.TR_ACNO_HIST_A; 
DROP TRIGGER ECSCRDB.TR_ACNO_HIST_D;
DROP TRIGGER ECSCRDB.TR_ACNO_STAT_TYPE_LOG_A;
DROP TRIGGER ECSCRDB.TR_ACNO_STAT_TYPE_LOG_U;
DROP TRIGGER ECSCRDB.tr_act_acno_acct_status_u;
DROP TRIGGER ECSCRDB.TR_ACT_ACNO_COL1_U; 
DROP TRIGGER ECSCRDB.TR_ACT_CCAS_LOG2_U; 
DROP TRIGGER ECSCRDB.TR_ACNO_HIST_U; 
DROP TRIGGER ECSCRDB.TR_CRD_WH_HST_U; 
DROP TRIGGER ECSCRDB.TR_CTI_act_acno_a; 
DROP TRIGGER ECSCRDB.TR_CTI_act_acno_u; 
DROP TRIGGER ECSCRDB.TR_ICH_ACT_ACNO;
DROP TRIGGER ECSCRDB.TR_IPS_ACT_ACNO_U;
DROP TRIGGER ECSCRDB.TR_JCIC_KK2_U;
DROP TRIGGER ECSCRDB.TR_LOG_ACT_ACNO_A; 
DROP TRIGGER ECSCRDB.TR_LOG_ACT_ACNO_D; 
DROP TRIGGER ECSCRDB.TR_LOG_ACT_ACNO_U; 
DROP TRIGGER ECSCRDB.TR_ACNO_ACT2APS_U; 
DROP TRIGGER ECSCRDB.TR_CARD_ACCT_BLOCK; 
DROP TRIGGER ECSCRDB.TR_CARD_BASE_SPEC; 
DROP TRIGGER ECSCRDB.TR_CARD_CMSMODLOG_U;
DROP TRIGGER ECSCRDB.TR_CARD_HIST_A; 
DROP TRIGGER ECSCRDB.TR_CARD_HIST_U; 
DROP TRIGGER ECSCRDB.TR_CARD_KK1_U; 
DROP TRIGGER ECSCRDB.TR_CARD_HIST_D; 
DROP TRIGGER ECSCRDB.TR_LOG_CRD_CARD_A; 
DROP TRIGGER ECSCRDB.TR_LOG_CRD_CARD_D; 
DROP TRIGGER ECSCRDB.TR_LOG_CRD_CARD_U;
DROP TRIGGER ECSCRDB.TR_UPD_MARKET_AGREE_U;
DROP TRIGGER ECSCRDB.TR_UPD_TSC_CARD_U;
DROP TRIGGER ECSCRDB.TR_UPD_CRD_CARD_PP_U; 
DROP TRIGGER ECSCRDB.TR_CRD_CARD_COL_A; 
DROP TRIGGER ECSCRDB.TR_CRD_CARD_COL_U; 
DROP TRIGGER ECSCRDB.tr_crd_card_current_code_u;
DROP TRIGGER ECSCRDB.TR_CRD_LOST_FEELOG_U; 
DROP TRIGGER ECSCRDB.TR_CTI_crd_card_a; 
DROP TRIGGER ECSCRDB.TR_CTI_crd_card_U; 
DROP TRIGGER ECSCRDB.TR_INSERT_TSC_CDNR_LOG_U;
DROP TRIGGER ECSCRDB.TR_CORP_HIST_A; 
DROP TRIGGER ECSCRDB.TR_CORP_HIST_D; 
DROP TRIGGER ECSCRDB.TR_CORP_HIST_U; 
DROP TRIGGER ECSCRDB.TR_LOG_CRD_CORP_U; 
DROP TRIGGER ECSCRDB.TR_LOG_CRD_CORP_A; 
DROP TRIGGER ECSCRDB.TR_LOG_CRD_CORP_D;
DROP TRIGGER ECSCRDB.TR_CRD_CORP_COL_U; 
DROP TRIGGER ECSCRDB.TR_LOG_CRD_IDNO_A; 
DROP TRIGGER ECSCRDB.TR_LOG_CRD_IDNO_D; 
DROP TRIGGER ECSCRDB.TR_LOG_CRD_IDNO_U; 
DROP TRIGGER ECSCRDB.TR_WH_HIST_U;
DROP TRIGGER ECSCRDB.TR_CRD_IBM_MARKET_A;
DROP TRIGGER ECSCRDB.TR_CRD_IBM_MARKET_D;
DROP TRIGGER ECSCRDB.TR_CRD_IBM_MARKET_U;
DROP TRIGGER ECSCRDB.TR_CRD_IDNO_COL_U; 
DROP TRIGGER ECSCRDB.TR_CTI_crd_idno_A; 
DROP TRIGGER ECSCRDB.TR_CTI_crd_idno_U; 
DROP TRIGGER ECSCRDB.TR_ICH_CRD_IDNO;
DROP TRIGGER ECSCRDB.TR_IDNO_ACT2APS_U; 
DROP TRIGGER ECSCRDB.TR_IDNO_HIST_A; 
DROP TRIGGER ECSCRDB.TR_IDNO_HIST_D; 
DROP TRIGGER ECSCRDB.TR_IDNO_HIST_U; 
DROP TRIGGER ECSCRDB.TR_IDNO_KK1_U; 
DROP TRIGGER ECSCRDB.TR_IDNO_MARKET_U;
DROP TRIGGER ECSCRDB.TR_IDNO_STAT_TYPE_LOG_A; 
DROP TRIGGER ECSCRDB.TR_IDNO_STAT_TYPE_LOG_U; 
DROP TRIGGER ECSCRDB.TR_IDNO_TONCCC_A; 
DROP TRIGGER ECSCRDB.TR_IDNO_TONCCC_D; 
DROP TRIGGER ECSCRDB.TR_IDNO_TONCCC_U; 
DROP TRIGGER ECSCRDB.TR_IPS_CRD_IDNO_U;
DROP TRIGGER ECSCRDB.TR_VD_IDNO_TONCCC_A; 
DROP TRIGGER ECSCRDB.TR_VD_IDNO_TONCCC_D; 
DROP TRIGGER ECSCRDB.TR_VD_IDNO_TONCCC_U; 
DROP TRIGGER ECSCRDB.TR_CURRENT_CODE_U;

-------------------------------
-- DDL Statements for Triggers
-------------------------------
SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
   NAME  ?‚à†??Ó∞™Ôôì update CRD_IDNO
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2010/06/22  ?Ó§ô?Êöæ?          1. program initial.
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
   NAME  •d§˘∞±•d update TSC_CARD
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2009/3/13   JHW              1. program initial.
   1.0.1      2009/3/17   JHW              1. rule change
   1.1.1      2014/5/12   Alan             1. current_code  = old.current_code
   1.2        2016/5/12   ®”≠Ù             ∑sºW§@•d≥q
******************************************************************************/

IF (old.electronic_code = '01') THEN
   update tsc_card
      set current_code = new.current_code
         ,oppost_date  = new.oppost_date
         ,mod_user     = 'trigger'
         ,mod_pgm      = 'tr_upd_tsc_card'
         ,mod_time     = sysdate
         ,mod_seqno    = nvl(mod_seqno,0)+1
   where card_no       = old.card_no
     and current_code  = old.current_code;--

END IF;--

IF (old.electronic_code = '02') THEN

   update ips_card
      set current_code     = new.current_code
         ,ips_oppost_date  = new.oppost_date
         ,mod_user         = 'trigger'
         ,mod_pgm          = 'tr_upd_tsc_card'
         ,mod_time         = sysdate
         ,mod_seqno        = nvl(mod_seqno,0)+1
   where card_no           = old.card_no
     and current_code      = old.current_code;--
	 
END IF;--

IF (old.electronic_code = '03') THEN

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

   /* 01: */
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

END IF;		  --

END
;

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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

   SELECT COUNT(*)
     INTO wk_count
     FROM mkt_ppcard_apply
    WHERE card_type = new.card_type;--

   IF wk_count > 0 THEN
      IF old.current_code= '0' AND new.current_code in ('1','3')  THEN                                       
   
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
   NAME  ?‚à†??Ó∞™Ôôì insert tsc_cdnr_log
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        2015/06/08  JAMES             1. ÂÅúÂç°Â†±ÈÄÅÊÇ†ÈÅäÂç°ÂÖ¨Âè∏
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
 


   set tmpVar = 0;--
   if UPDATING or DELETING then

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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
SET CURRENT SCHEMA = "ECSCRDB";
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
CREATE TRIGGER "ECSCRDB"."TR_CURRENT_CODE_U"
  AFTER UPDATE OF "CURRENT_CODE"
  ON "ECSCRDB"."CRD_CARD"
  REFERENCING 
    OLD AS OLD
    NEW AS NEW
  FOR EACH ROW
  WHEN (nvl (old.current_code, 0) <> nvl (new.current_code, 0)) 
BEGIN
   DECLARE wk_modtime       VARCHAR (8);--
   DECLARE wk_v_card_no     VARCHAR (19);        --hce_card.v_card_no%TYPE;--
   DECLARE wk_v_card_no2    VARCHAR (19);        --oempay_card.v_card_no%TYPE;--
   DECLARE wk_reason_code   VARCHAR (10);--

   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING  BEGIN
                                                             RETURN;--
                                                          END;--

   -- current_code   1∞±•d  2±æ•¢  3±j∞± 4®‰•L 5 ∞∞•d
   -- operation     01º»∞± 02´Ï¥_ 03≤◊§Ó
   
   IF nvl (new.current_code, 0) = '3'
   THEN
      UPDATE act_acno
         SET deposit_flag = 'Y'
       WHERE p_seqno = new.p_seqno;--
   END IF;--

   SELECT neg_opp_reason 
     INTO wk_reason_code
     FROM cca_opp_type_reason
    WHERE opp_status = new.oppost_reason
    FETCH FIRST 1 ROWS ONLY;--

   SELECT v_card_no
     INTO wk_v_card_no
     FROM hce_card
    WHERE card_no = new.card_no AND status_code = '0'
   FETCH FIRST 1 ROWS ONLY;--


   SELECT v_card_no
     INTO wk_v_card_no2
     FROM oempay_card
    WHERE card_no = new.card_no AND status_code = '0'
   FETCH FIRST 1 ROWS ONLY;--

   IF nvl (old.current_code, 0) = '0' AND nvl (wk_v_card_no, '') != ''
   THEN
      INSERT INTO hce_life_cycle (card_no,
                                  v_card_no,
                                  id_p_seqno,
                                  current_code,
                                  oppost_date,
                                  operation,
                                  proc_flag,
                                  mod_time,
                                  mod_pgm)
      VALUES (new.card_no,
              wk_v_card_no,
              new.id_p_seqno,
              new.current_code,
              new.oppost_date,
              decode (new.current_code,
                      '2',
                      '01',
                      '03'),
              'N',
              sysdate,
              'tr_current_code');--
   END IF;--

   IF     nvl (old.current_code, 0) = '2'
      AND nvl (new.current_code, 0) = '0'
      AND nvl (wk_v_card_no, '') != ''
   THEN
      INSERT INTO hce_life_cycle (card_no,
                                  v_card_no,
                                  id_p_seqno,
                                  current_code,
                                  oppost_date,
                                  operation,
                                  proc_flag,
                                  mod_time,
                                  mod_pgm)
      VALUES (new.card_no,
              wk_v_card_no,
              new.id_p_seqno,
              new.current_code,
              new.oppost_date,
              '02',
              'N',
              sysdate,
              'tr_current_code');--
   END IF;--

   IF nvl (new.current_code, '') <> '0' AND nvl (wk_v_card_no, '') != ''
   THEN
      --HCE UPDATE
      UPDATE hce_card
         SET status_code = '3',
             change_date = to_char (sysdate, 'yyyymmdd'),
             mod_time = sysdate,
             mod_pgm = 'tr_current_code'
       WHERE v_card_no = wk_v_card_no;--

      INSERT INTO cca_outgoing (crt_date,
                                crt_time,
                                card_no,
                                v_card_no,
                                key_value,
                                key_table,
                                bitmap,
                                act_code,
                                crt_user,
                                proc_flag,
                                send_times,
                                data_from,
                                data_type,
                                bin_type,
                                vip_amt,
                                mod_time,
                                mod_pgm,
                                electronic_card_no,
                                current_code,
                                new_end_date,
                                oppost_date,
                                oppost_reason,
                                reason_code)
      VALUES (to_char (sysdate, 'yyyymmdd'),
              to_char (new.mod_time, 'hh24miss'),
              new.card_no,
              wk_v_card_no,
              'TWMP',
              'OPPOSITION',
              '',
              '1',
              new.mod_user,
              '1',
              '1',
              '2',
              'OPPO',
              new.bin_type,
              '0',
              to_char (sysdate, 'yyyymmddhh24miss'),
              'tr_current_code',
              '',
              new.current_code,
              new.new_end_date,
              new.oppost_date,
              new.oppost_reason,
              nvl (wk_reason_code, ''));--
   END IF;--

   --OEMPAY UPDATE
   IF nvl (new.current_code, '') <> '0' AND nvl (wk_v_card_no2, '') != ''
   THEN
      UPDATE oempay_card
         SET status_code = '3',
             change_date = to_char (sysdate, 'yyyymmdd'),
             mod_time = sysdate,
             mod_pgm = 'tr_current_code'
       WHERE v_card_no = wk_v_card_no2;--

      INSERT INTO cca_outgoing (crt_date,
                                crt_time,
                                card_no,
                                v_card_no,
                                key_value,
                                key_table,
                                bitmap,
                                act_code,
                                crt_user,
                                proc_flag,
                                send_times,
                                data_from,
                                data_type,
                                bin_type,
                                vip_amt,
                                mod_time,
                                mod_pgm,
                                electronic_card_no,
                                current_code,
                                new_end_date,
                                oppost_date,
                                oppost_reason,
                                reason_code)
      VALUES (to_char (sysdate, 'yyyymmdd'),
              to_char (new.mod_time, 'hh24miss'),
              new.card_no,
              wk_v_card_no2,
              'OEMPAY',
              'OPPOSITION',
              '',
              '1',
              new.mod_user,
              '1',
              '1',
              '2',
              'OPPO',
              new.bin_type,
              '0',
              to_char (sysdate, 'yyyymmddhh24miss'),
              'tr_current_code',
              '',
              new.current_code,
              new.new_end_date,
              new.oppost_date,
              new.oppost_reason,
              nvl (wk_reason_code, ''));--
   END IF;--
END;



--trigger new

COMMIT WORK;

CONNECT RESET;

TERMINATE;
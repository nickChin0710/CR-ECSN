

CONNECT TO CR;


----------------------------

-- DDL Statements for Views

----------------------------
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW V_IDNO AS select 'N' as debit_flag, id_p_seqno, id_no, id_no_code,
chi_name, birthday, sex, home_area_code1, home_tel_no1, home_area_code2,
home_tel_no2, office_area_code1, office_tel_no1, office_tel_ext1, office_area_code2,
office_tel_no2, office_tel_ext2, cellar_phone, e_mail_addr from crd_idno
union select 'Y' as debit_flag, id_p_seqno, id_no, id_no_code, chi_name,
birthday, sex, home_area_code1, home_tel_no1, home_area_code2, home_tel_no2,
office_area_code1, office_tel_no1, office_tel_ext1, office_area_code2,
office_tel_no2, office_tel_ext2, cellar_phone, e_mail_addr from dbc_idno;


COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."BIRTHDAY" IS '生日';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."CELLAR_PHONE" IS '手機號碼 ';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."CHI_NAME" IS '中文姓名 ';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."DEBIT_FLAG" IS 'DEBIT卡註記';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."E_MAIL_ADDR" IS '電子郵件信箱 ';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."HOME_AREA_CODE1" IS '住家電話區碼1 ';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."HOME_AREA_CODE2" IS '住家電話區碼2  ';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."HOME_TEL_NO1" IS '住家電話號碼1 ';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."HOME_TEL_NO2" IS '住家電話號碼2 ';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."ID_NO" IS '身分證字號';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."ID_NO_CODE" IS '身分證辨識碼 ';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."ID_P_SEQNO" IS '身分證序號';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."OFFICE_AREA_CODE1" IS '公司電話區碼1';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."OFFICE_AREA_CODE2" IS '公司電話區碼2';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."OFFICE_TEL_EXT1" IS '公司電話分機1';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."OFFICE_TEL_EXT2" IS '公司電話分機2';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."OFFICE_TEL_NO1" IS '公司電話號碼1';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."OFFICE_TEL_NO2" IS '公司電話號碼2';

COMMENT ON COLUMN "ECSCRDB "."V_IDNO"."SEX" IS '性別 ';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW VDBM_BONUS_ACTIVE_NAME  AS SELECT  active_code,           active_name
,  'DBM_BPMH' as table_name FROM    dbm_bpmh UNION SELECT  active_code,
active_name  ,  'DBM_BPBIR' as table_name FROM    dbm_bpbir UNION SELECT
active_code,           active_name  ,  'DBM_BPIS' as table_name FROM dbm_bpis
UNION SELECT  'A0YYYY901' as active_code, '' as active_name , 
'DUMMY' as table_name FROM    SYSIBM.SYSDUMMY1 UNION SELECT 'A0YYYY902'
as active_code, '' as active_name  ,  'DUMMY' as table_name FROM
SYSIBM.SYSDUMMY1;


COMMENT ON COLUMN "ECSCRDB "."VDBM_BONUS_ACTIVE_NAME"."ACTIVE_CODE" IS '活動方案';

COMMENT ON COLUMN "ECSCRDB "."VDBM_BONUS_ACTIVE_NAME"."ACTIVE_NAME" IS '方案說明';

COMMENT ON COLUMN "ECSCRDB "."VDBM_BONUS_ACTIVE_NAME"."TABLE_NAME" IS '表格名稱';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW VMKT_ACCT_TYPE  AS SELECT  acct_type, chin_name, 'N' as vd_flag
FROM    ptr_acct_type UNION SELECT  acct_type, chin_name, 'Y' as vd_flag
FROM    dbp_acct_type;


COMMENT ON COLUMN "ECSCRDB "."VMKT_ACCT_TYPE"."ACCT_TYPE" IS '帳戶帳號類別碼';

COMMENT ON COLUMN "ECSCRDB "."VMKT_ACCT_TYPE"."CHIN_NAME" IS '類別中文說明  ';

COMMENT ON COLUMN "ECSCRDB "."VMKT_ACCT_TYPE"."VD_FLAG" IS 'VD旗標';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW VMKT_BONUS_ACTIVE_NAME  AS SELECT  active_code,           active_name
,  'MKT_BPMH2' as table_name FROM    mkt_bpmh2 UNION SELECT  active_code,
active_name  ,  'MKT_BPMH3' as table_name FROM    mkt_bpmh3 UNION SELECT
active_code,           active_name  ,  'MKT_BPNW' as table_name FROM mkt_bpnw;


COMMENT ON COLUMN "ECSCRDB "."VMKT_BONUS_ACTIVE_NAME"."ACTIVE_CODE" IS '紅利活動代碼';

COMMENT ON COLUMN "ECSCRDB "."VMKT_BONUS_ACTIVE_NAME"."ACTIVE_NAME" IS '紅利活動說明';

COMMENT ON COLUMN "ECSCRDB "."VMKT_BONUS_ACTIVE_NAME"."TABLE_NAME" IS '表格名稱';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW VMKT_DCFUND_NAME  AS SELECT  fund_code,           fund_name
, 'cyc_dc_fund_dtl' as table_name FROM    cyc_dc_fund_parm;


COMMENT ON COLUMN "ECSCRDB "."VMKT_DCFUND_NAME"."FUND_CODE" IS '基金代碼';

COMMENT ON COLUMN "ECSCRDB "."VMKT_DCFUND_NAME"."FUND_NAME" IS '中文姓名';

COMMENT ON COLUMN "ECSCRDB "."VMKT_DCFUND_NAME"."TABLE_NAME" IS '表格名稱';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW VMKT_FUND_NAME  AS SELECT  fund_code, fund_name, 'ptr_fundp'
as table_name, '1' as fund_type, '' as type_name FROM    ptr_fundp
UNION SELECT  fund_code, fund_name, 'ptr_combo_fundp' as table_name, '2'
as fund_type, 'COMBO' as type_name FROM    ptr_combo_fundp UNION SELECT
fund_code, fund_name, 'cyc_dc_fund_parm' as table_name, '3' as fund_type,
'' as type_name FROM    cyc_dc_fund_parm UNION SELECT  fund_code,
fund_name, 'mkt_loan_parm' as table_name, '4' as fund_type, '' as
type_name FROM    mkt_loan_parm UNION SELECT  fund_code, fund_name, 'mkt_nfc_parm'
as table_name, '5' as fund_type, '' as type_name FROM    mkt_nfc_parm;


COMMENT ON COLUMN "ECSCRDB "."VMKT_FUND_NAME"."FUND_CODE" IS '基金代碼';

COMMENT ON COLUMN "ECSCRDB "."VMKT_FUND_NAME"."FUND_NAME" IS '中文姓名';

COMMENT ON COLUMN "ECSCRDB "."VMKT_FUND_NAME"."FUND_TYPE" IS '回饋類型';

COMMENT ON COLUMN "ECSCRDB "."VMKT_FUND_NAME"."TABLE_NAME" IS '表格名稱';

COMMENT ON COLUMN "ECSCRDB "."VMKT_FUND_NAME"."TYPE_NAME" IS '類型名稱';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW Vrsk_chgback AS select  reference_no , reference_seq , bin_type
, ctrl_seqno , ctrl_seqno2 , debit_flag , chg_stage , sub_stage , send_flag
, send_date , final_close , clo_result , close_add_date , close_add_user
, close_apr_date , close_apr_user , chg_times , decode(chg_times,2,sec_status
,fst_status       ) as fst_status , decode(chg_times,2,sec_reverse_mark
,fst_reverse_mark ) as fst_reverse_mark , decode(chg_times,2,sec_reverse_date
,fst_reverse_date ) as fst_reverse_date , decode(chg_times,2,sec_rebuild_mark
,fst_rebuild_mark ) as fst_rebuild_mark , decode(chg_times,2,sec_rebuild_date
,fst_rebuild_date ) as fst_rebuild_date , decode(chg_times,2,sec_send_date
,fst_send_date    ) as fst_send_date , decode(chg_times,2,sec_send_cnt
,fst_send_cnt     ) as fst_send_cnt , decode(chg_times,2,sec_usage_code
,fst_usage_code   ) as fst_usage_code , decode(chg_times,2,sec_reason_code
,fst_reason_code  ) as fst_reason_code , decode(chg_times,2,sec_msg ,fst_msg
) as fst_msg , decode(chg_times,2,sec_doc_mark ,fst_doc_mark ) as fst_doc_mark
, decode(chg_times,2,sec_amount ,fst_amount ) as fst_amount , decode(chg_times,2,sec_twd_amt
,fst_twd_amt      ) as fst_twd_amt , decode(chg_times,2,sec_part_mark ,fst_part_mark
   ) as fst_part_mark , decode(chg_times,2,sec_expire_date ,fst_expire_date
 ) as fst_expire_date , decode(chg_times,2,sec_add_date ,fst_add_date 
   ) as fst_add_date , decode(chg_times,2,sec_add_user ,fst_add_user  
  ) as fst_add_user , decode(chg_times,2,sec_apr_date ,fst_apr_date   
 ) as fst_apr_date , decode(chg_times,2,sec_apr_user ,fst_apr_user ) as
fst_apr_user , decode(chg_times,2,sec_disb_yn ,fst_disb_yn ) as fst_disb_yn
, decode(chg_times,2,sec_disb_amt ,fst_disb_amt ) as fst_disb_amt , decode(chg_times,2,sec_disb_add_date,fst_disb_add_date)
as fst_disb_add_date , decode(chg_times,2,sec_disb_add_user,fst_disb_add_user)
as fst_disb_add_user , decode(chg_times,2,sec_disb_apr_date,fst_disb_apr_date)
as fst_disb_apr_date , decode(chg_times,2,sec_disb_apr_user,fst_disb_apr_user)
as fst_disb_apr_user , decode(chg_times,2,sec_dc_amt       ,fst_dc_amt
) as fst_dc_amt , decode(chg_times,2,sec_disb_dc_amt  ,fst_disb_dc_amt
) as fst_disb_dc_amt , rep_status , rep_msg , rep_doc_mark , repsent_date
, rep_amt , rep_amt_twd , rep_expire_date , rep_add_date , rep_add_user
, rep_apr_date , rep_apr_user , rep_glmemo3 , rep_dc_amt , card_no , id_p_seqno
, major_id_p_seqno , corp_p_seqno , acct_type , txn_code , film_no , acq_member_id
, source_amt , source_curr , settl_amt , settl_flag , dest_amt , dest_curr
, mcht_eng_name , mcht_city , mcht_country , mcht_category , mcht_zip ,
mcht_state , mcht_no , mcht_chi_name , auth_code , acct_month , bill_type
, purchase_date , post_date , payment_type , curr_tx_amount , install_tot_term1
, deduct_bp , cash_pay_amt , no_send_cnt , dc_dest_amt , curr_code , v_card_no
, rep_ac_no , fst_ac_no , fst_glmemo3 , gl_proc_flag , decode(chg_times,2,sec_gl_date,fst_gl_date)
as fst_gl_date , rep_gl_date , contract_no , reference_no_ori , sign_flag
, tpan_type , mod_user , mod_time , mod_pgm , mod_seqno from rsk_chgback;


COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."ACCT_MONTH" IS '帳務年月';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."ACCT_TYPE" IS '帳戶帳號類別碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."ACQ_MEMBER_ID" IS '清算會員ID';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."AUTH_CODE" IS '授權碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."BILL_TYPE" IS '帳單類別';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."BIN_TYPE" IS '卡片種類';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CARD_NO" IS '消費卡號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CASH_PAY_AMT" IS 'CASH_PAY_AMT';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CHG_STAGE" IS '扣款主階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CHG_TIMES" IS '扣款次數';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CLOSE_ADD_DATE" IS '總結案登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CLOSE_ADD_USER" IS '總結案登錄者ID';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CLOSE_APR_DATE" IS '總結案覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CLOSE_APR_USER" IS '總結案覆核者ID';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CLO_RESULT" IS '處理結果碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CONTRACT_NO" IS '合約編號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CORP_P_SEQNO" IS '公司流水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CTRL_SEQNO" IS '控制流水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CTRL_SEQNO2" IS '控制流水號2';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CURR_CODE" IS '結算幣別';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."CURR_TX_AMOUNT" IS '異動金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."DC_DEST_AMT" IS '外幣清算幣別消費金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."DEBIT_FLAG" IS 'VD卡片旗標';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."DEDUCT_BP" IS 'DEDUCT_BP';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."DEST_AMT" IS '目的地金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."DEST_CURR" IS '目的地幣別';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FILM_NO" IS '微縮影編號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FINAL_CLOSE" IS '總結案註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_AC_NO" IS '扣款貸方科目';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_ADD_DATE" IS '扣款登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_ADD_USER" IS '扣款登錄者ID';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_AMOUNT" IS '扣款金額(美金)';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_APR_DATE" IS '扣款覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_APR_USER" IS '扣款覆核者ID';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_DC_AMT" IS '扣款結算金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_DISB_ADD_DATE" IS '扣款撥款登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_DISB_ADD_USER" IS '扣款撥款登錄者ID';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_DISB_AMT" IS '扣款撥款實際金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_DISB_APR_DATE" IS '扣款撥款覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_DISB_APR_USER" IS '扣款撥款覆核者ID';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_DISB_DC_AMT" IS '扣款撥款結算金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_DISB_YN" IS '扣款是否撥款';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_DOC_MARK" IS '扣款文件註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_EXPIRE_DATE" IS '扣款逾期日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_GLMEMO3" IS '扣款銷帳鍵值';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_GL_DATE" IS '扣款啟帳日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_MSG" IS '扣款訊息欄';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_PART_MARK" IS '扣款部份扣款註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_REASON_CODE" IS '扣款理由碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_REBUILD_DATE" IS '扣款重送日';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_REBUILD_MARK" IS '扣款重送註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_REVERSE_DATE" IS '扣款沖銷日';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_REVERSE_MARK" IS '扣款沖銷註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_SEND_CNT" IS '扣款傳送次數';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_SEND_DATE" IS '扣款待傳送日';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_STATUS" IS '扣款狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_TWD_AMT" IS '扣款台幣金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."FST_USAGE_CODE" IS '扣款使用碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."GL_PROC_FLAG" IS '啟帳處理旗標';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."ID_P_SEQNO" IS '卡人身分證流水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."INSTALL_TOT_TERM1" IS '分期付款總期數1';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MAJOR_ID_P_SEQNO" IS '正卡人身分證流水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MCHT_CATEGORY" IS '特店業別';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MCHT_CHI_NAME" IS '特店中文名稱';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MCHT_CITY" IS '特店城市';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MCHT_COUNTRY" IS '特店國別';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MCHT_ENG_NAME" IS '特店英文名稱';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MCHT_NO" IS '特店代號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MCHT_STATE" IS '特店州別';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MCHT_ZIP" IS '特店郵區';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MOD_PGM" IS '異動程式';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MOD_SEQNO" IS '異動註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MOD_TIME" IS '異動時間';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."MOD_USER" IS '異動使用者';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."NO_SEND_CNT" IS '不送NCCC筆數';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."PAYMENT_TYPE" IS 'PAYMENT_TYPE';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."POST_DATE" IS '入帳日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."PURCHASE_DATE" IS '消費日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REFERENCE_NO" IS '參考號碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REFERENCE_NO_ORI" IS '原始帳單水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REFERENCE_SEQ" IS '參考序號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REPSENT_DATE" IS '再提示發生日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_AC_NO" IS '再提示借方科目';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_ADD_DATE" IS '再提示登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_ADD_USER" IS '再提示登錄者ID';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_AMT" IS '再提示金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_AMT_TWD" IS '再提示台幣金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_APR_DATE" IS '再提示覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_APR_USER" IS '再提示覆核者ID';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_DC_AMT" IS '再提示結算金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_DOC_MARK" IS '再提示文件註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_EXPIRE_DATE" IS '再提示逾期日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_GLMEMO3" IS 'REP_GLMEMO3';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_GL_DATE" IS '再提啟帳日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_MSG" IS '再提示訊息欄';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."REP_STATUS" IS '再提示狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."SEND_DATE" IS '傳送日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."SEND_FLAG" IS '傳送旗標';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."SETTL_AMT" IS '清算金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."SETTL_FLAG" IS '清算註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."SIGN_FLAG" IS '正負項註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."SOURCE_AMT" IS '原始金額';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."SOURCE_CURR" IS '原始幣別';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."SUB_STAGE" IS '扣款子階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."TPAN_TYPE" IS '行動支付類別';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."TXN_CODE" IS '交易碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CHGBACK"."V_CARD_NO" IS '行動支付卡號';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW Vrsk_ctrlseqno_bil AS select BB.reference_no , BB.rsk_ctrl_seqno
as CTRL_SEQNO , BB.card_no , BB.bin_type , BB.rsk_print_flag as print_flag
, 'N' as DEBIT_FLAG , BB.reference_no_original as REFERENCE_NO_ORI , nvl(A.reference_seq,0)
as	PRBL_SEQ , nvl(A.prb_status,'') as	PRBL_STATUS , nvl(A.add_date,'')
as PRBL_ADD_DATE , nvl(A.prb_reason_code,'') as PRBL_REASON_CODE , nvl(A.prb_mark,'')
as PRBL_MARK , nvl(A.prb_src_code,'') as PRBL_src_code , nvl(decode(nvl(A.close_apr_date,''),'',A.add_apr_date,A.close_apr_date),'')
as other3_mark , nvl(B.reference_seq,0) as CHGB_SEQ , nvl(B.chg_stage,'')
as CHGB_stage1 , nvl(B.sub_stage,'') as CHGB_stage2 , nvl(B.final_close,'')
as CHGB_close , nvl(B.fst_add_date,'') as CHGB_ADD_DATE , nvl(decode(B.close_apr_date,'',decode(B.chg_stage,'2',B.rep_apr_date,B.fst_apr_date),B.close_apr_date),'')
as other2_mark , nvl(C.reference_seq,0) as REPT_SEQNO , nvl(C.rept_status,'')
as REPT_STATUS , nvl(C.add_date,'') as REPT_ADD_DATE , nvl(D.compl_times||decode(D.compl_times,2,D.com_status,D.pre_status),'')
as COMPL_MARK , nvl(decode(D.compl_times,2,D.com_add_date,D.pre_add_date),'')
as COMPL_ADD_DATE , nvl(E.arbit_times||decode(E.arbit_times,2,E.arb_status,E.pre_status),'')
as ARBIT_MARK , nvl(decode(E.arbit_times,2,E.arb_add_date,E.pre_add_date),'')
as ARBIT_ADD_DATE , (decode(A.add_date,null,'0','1')||decode(B.fst_add_date,null,'0','1')||decode(C.add_date,null,'0','1')||decode(D.compl_times,null,'0','1')||decode(E.arbit_times,null,'0','1'))
as rsk_data_type from bil_bill BB left join rsk_problem A on A.reference_no=BB.reference_no
left join rsk_chgback B on B.reference_no=BB.reference_no left join rsk_receipt
C on C.reference_no=BB.reference_no left join rsk_precompl D on D.reference_no=BB.reference_no
left join rsk_prearbit E on E.reference_no=BB.reference_no where BB.rsk_ctrl_seqno<>'';


COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."ARBIT_ADD_DATE" IS '仲裁登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."ARBIT_MARK" IS '仲裁狀態';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."BIN_TYPE" IS '卡片種類';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."CARD_NO" IS '消費卡號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."CHGB_ADD_DATE" IS '扣款登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."CHGB_CLOSE" IS '扣款總結案註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."CHGB_SEQ" IS '扣款參考序號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."CHGB_STAGE1" IS '扣款主階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."CHGB_STAGE2" IS '扣款子階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."COMPL_ADD_DATE" IS '依從權登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."COMPL_MARK" IS '依從權狀態';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."CTRL_SEQNO" IS '控制流水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."DEBIT_FLAG" IS 'VD卡片旗標';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."OTHER2_MARK" IS '扣款覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."OTHER3_MARK" IS '問題交易覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."PRBL_ADD_DATE" IS '問題交易新增登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."PRBL_MARK" IS '問題交易本記錄歸類';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."PRBL_REASON_CODE" IS '問題交易原因碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."PRBL_SEQ" IS '問題交易參考序碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."PRBL_SRC_CODE" IS '問題交易本記錄來源碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."PRBL_STATUS" IS '問題交易狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."PRINT_FLAG" IS '爭議款列印註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."REFERENCE_NO" IS '參考號碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."REFERENCE_NO_ORI" IS '原始帳單水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."REPT_ADD_DATE" IS '調單新增登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."REPT_SEQNO" IS '調單參考號碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."REPT_STATUS" IS '調單狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL"."RSK_DATA_TYPE" IS '交易資料狀態';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW Vrsk_ctrlseqno_bil3 AS select BB.reference_no , BB.rsk_ctrl_seqno
as CTRL_SEQNO , BB.card_no , BB.bin_type , BB.rsk_print_flag as print_flag
, 'N' as DEBIT_FLAG , BB.reference_no_original as REFERENCE_NO_ORI , nvl(A.reference_seq,0)
as	PRBL_SEQ , nvl(A.prb_status,'') as	PRBL_STATUS , nvl(A.add_date,'')
as PRBL_ADD_DATE , nvl(A.prb_reason_code,'') as PRBL_REASON_CODE , nvl(A.prb_mark,'')
as PRBL_MARK , nvl(A.prb_src_code,'') as PRBL_src_code , nvl(decode(nvl(A.close_apr_date,''),'',A.add_apr_date,A.close_apr_date),'')
as other3_mark , nvl(B.reference_seq,0) as CHGB_SEQ , nvl(B.chg_stage,'')
as CHGB_stage1 , nvl(B.sub_stage,'') as CHGB_stage2 , nvl(B.final_close,'')
as CHGB_close , nvl(B.fst_add_date,'') as CHGB_ADD_DATE , nvl(decode(B.close_apr_date,'',decode(B.chg_stage,'2',B.rep_apr_date,B.fst_apr_date),B.close_apr_date),'')
as other2_mark , nvl(C.reference_seq,0) as REPT_SEQNO , nvl(C.rept_status,'')
as REPT_STATUS , nvl(C.add_date,'') as REPT_ADD_DATE , (decode(A.add_date,null,'0','1')||decode(B.fst_add_date,null,'0','1')||decode(C.add_date,null,'0','1'))
as rsk_data_type from bil_bill BB left join rsk_problem A on A.reference_no=BB.reference_no
left join rsk_chgback B on B.reference_no=BB.reference_no left join rsk_receipt
C on C.reference_no=BB.reference_no where BB.rsk_ctrl_seqno<>'';


COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."BIN_TYPE" IS '卡片種類';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."CARD_NO" IS '消費卡號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."CHGB_ADD_DATE" IS '扣款登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."CHGB_CLOSE" IS '扣款總結案註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."CHGB_SEQ" IS '扣款參考序號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."CHGB_STAGE1" IS '扣款主階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."CHGB_STAGE2" IS '扣款子階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."CTRL_SEQNO" IS '控制流水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."DEBIT_FLAG" IS 'VD卡片旗標';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."OTHER2_MARK" IS '扣款覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."OTHER3_MARK" IS '問題交易覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."PRBL_ADD_DATE" IS '問題交易新增登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."PRBL_MARK" IS '問題交易本記錄歸類';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."PRBL_REASON_CODE" IS '問題交易原因碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."PRBL_SEQ" IS '問題交易參考序碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."PRBL_SRC_CODE" IS '問題交易本記錄來源碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."PRBL_STATUS" IS '問題交易狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."PRINT_FLAG" IS '爭議款列印註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."REFERENCE_NO" IS '參考號碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."REFERENCE_NO_ORI" IS '原始帳單水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."REPT_ADD_DATE" IS '調單新增登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."REPT_SEQNO" IS '調單參考號碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."REPT_STATUS" IS '調單狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_BIL3"."RSK_DATA_TYPE" IS '交易資料狀態';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW Vrsk_ctrlseqno_dbb AS select BB.reference_no , BB.rsk_ctrl_seqno
as CTRL_SEQNO , BB.card_no , BB.bin_type , BB.rsk_print_flag as print_flag
, 'Y' as DEBIT_FLAG , BB.reference_no_original as REFERENCE_NO_ORI , A.reference_seq
as	PRBL_SEQ , A.prb_status as	PRBL_STATUS , A.add_date as PRBL_ADD_DATE
, A.prb_reason_code as PRBL_REASON_CODE , A.prb_mark as PRBL_MARK , A.prb_src_code
as PRBL_src_code , decode(A.close_apr_date,'',A.add_apr_date,A.close_apr_date)
as other3_mark , B.reference_seq as CHGB_SEQ , B.chg_stage as CHGB_stage1
, B.sub_stage as CHGB_stage2 , B.final_close as CHGB_close , B.fst_add_date
as CHGB_ADD_DATE , decode(B.close_apr_date,'',decode(B.chg_stage,'2',B.rep_apr_date,B.fst_apr_date),B.close_apr_date)
as other2_mark , C.reference_seq as REPT_SEQNO , C.rept_status as REPT_STATUS
, C.add_date as REPT_ADD_DATE , D.compl_times||decode(D.compl_times,2,D.com_status,D.pre_status)
as compl_MARK , decode(D.compl_times,2,D.com_add_date,D.pre_add_date) as
compl_ADD_DATE , E.arbit_times||decode(E.arbit_times,2,E.arb_status,E.pre_status)
as arbit_MARK , decode(E.arbit_times,2,E.arb_add_date,E.pre_add_date) as
arbit_ADD_DATE , (decode(A.add_date,null,'0','1')||decode(B.fst_add_date,null,'0','1')||decode(C.add_date,null,'0','1')||decode(D.compl_times,null,'0','1')||decode(E.arbit_times,null,'0','1'))
as rsk_data_type from dbb_bill BB left join rsk_problem A on A.reference_no=BB.reference_no
left join rsk_chgback B on B.reference_no=BB.reference_no left join rsk_receipt
C on C.reference_no=BB.reference_no left join rsk_precompl D on D.reference_no=BB.reference_no
left join rsk_prearbit E on E.reference_no=BB.reference_no where BB.rsk_ctrl_seqno<>'';


COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."ARBIT_ADD_DATE" IS '仲裁登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."ARBIT_MARK" IS '仲裁狀態';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."BIN_TYPE" IS '卡片種類';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."CARD_NO" IS '消費卡號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."CHGB_ADD_DATE" IS '扣款登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."CHGB_CLOSE" IS '扣款總結案註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."CHGB_SEQ" IS '扣款參考序號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."CHGB_STAGE1" IS '扣款主階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."CHGB_STAGE2" IS '扣款子階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."COMPL_ADD_DATE" IS '依從權登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."COMPL_MARK" IS '依從權狀態';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."CTRL_SEQNO" IS '控制流水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."DEBIT_FLAG" IS 'VD卡片旗標';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."OTHER2_MARK" IS '扣款覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."OTHER3_MARK" IS '問題交易覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."PRBL_ADD_DATE" IS '問題交易新增登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."PRBL_MARK" IS '問題交易本記錄歸類';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."PRBL_REASON_CODE" IS '問題交易原因碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."PRBL_SEQ" IS '問題交易參考序碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."PRBL_SRC_CODE" IS '問題交易本記錄來源碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."PRBL_STATUS" IS '問題交易狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."PRINT_FLAG" IS '爭議款列印註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."REFERENCE_NO" IS '參考號碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."REFERENCE_NO_ORI" IS '原始帳單水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."REPT_ADD_DATE" IS '調單新增登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."REPT_SEQNO" IS '調單參考號碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."REPT_STATUS" IS '調單狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB"."RSK_DATA_TYPE" IS '交易資料狀態';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW Vrsk_ctrlseqno_dbb3 AS select BB.reference_no , BB.rsk_ctrl_seqno
as CTRL_SEQNO , BB.card_no , BB.bin_type , BB.rsk_print_flag as print_flag
, 'Y' as DEBIT_FLAG , BB.reference_no_original as REFERENCE_NO_ORI , A.reference_seq
as	PRBL_SEQ , A.prb_status as	PRBL_STATUS , A.add_date as PRBL_ADD_DATE
, A.prb_reason_code as PRBL_REASON_CODE , A.prb_mark as PRBL_MARK , A.prb_src_code
as PRBL_src_code , decode(A.close_apr_date,'',A.add_apr_date,A.close_apr_date)
as other3_mark , B.reference_seq as CHGB_SEQ , B.chg_stage as CHGB_stage1
, B.sub_stage as CHGB_stage2 , B.final_close as CHGB_close , B.fst_add_date
as CHGB_ADD_DATE , decode(B.close_apr_date,'',decode(B.chg_stage,'2',B.rep_apr_date,B.fst_apr_date),B.close_apr_date)
as other2_mark , C.reference_seq as REPT_SEQNO , C.rept_status as REPT_STATUS
, C.add_date as REPT_ADD_DATE , (decode(A.add_date,null,'0','1')||decode(B.fst_add_date,null,'0','1')||decode(C.add_date,null,'0','1'))
as rsk_data_type from dbb_bill BB left join rsk_problem A on A.reference_no=BB.reference_no
left join rsk_chgback B on B.reference_no=BB.reference_no left join rsk_receipt
C on C.reference_no=BB.reference_no where BB.rsk_ctrl_seqno<>'';


COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."BIN_TYPE" IS '卡片種類';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."CARD_NO" IS '消費卡號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."CHGB_ADD_DATE" IS '扣款登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."CHGB_CLOSE" IS '扣款總結案註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."CHGB_SEQ" IS '扣款參考序號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."CHGB_STAGE1" IS '扣款主階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."CHGB_STAGE2" IS '扣款子階段';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."CTRL_SEQNO" IS '控制流水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."DEBIT_FLAG" IS 'VD卡片旗標';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."OTHER2_MARK" IS '扣款覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."OTHER3_MARK" IS '問題交易覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."PRBL_ADD_DATE" IS '問題交易新增登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."PRBL_MARK" IS '問題交易本記錄歸類';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."PRBL_REASON_CODE" IS '問題交易原因碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."PRBL_SEQ" IS '問題交易參考序碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."PRBL_SRC_CODE" IS '問題交易本記錄來源碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."PRBL_STATUS" IS '問題交易狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."PRINT_FLAG" IS '爭議款列印註記';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."REFERENCE_NO" IS '參考號碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."REFERENCE_NO_ORI" IS '原始帳單水號';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."REPT_ADD_DATE" IS '調單新增登錄日期';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."REPT_SEQNO" IS '調單參考號碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."REPT_STATUS" IS '調單狀態碼';

COMMENT ON COLUMN "ECSCRDB "."VRSK_CTRLSEQNO_DBB3"."RSK_DATA_TYPE" IS '交易資料狀態';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW V_FUNDP AS SELECT 'ptr_fundp' tab_name, FUND_CODE, FUND_NAME
FROM ptr_fundp UNION SELECT 'mkt_loan_parm' tab_name, FUND_CODE, FUND_NAME
FROM mkt_loan_parm UNION SELECT 'mkt_pp_fundp' tab_name, program_code,
chi_name FROM mkt_pp_fundp UNION SELECT 'mkt_intr_fund' tab_name, program_code,
chi_name FROM mkt_intr_fund;


COMMENT ON COLUMN "ECSCRDB "."V_FUNDP"."FUND_CODE" IS '基金代碼';

COMMENT ON COLUMN "ECSCRDB "."V_FUNDP"."FUND_NAME" IS '中文姓名';

COMMENT ON COLUMN "ECSCRDB "."V_FUNDP"."TAB_NAME" IS '表格名稱';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW Vcard_acno AS select A.card_no, A.current_code, A.id_p_seqno,
A.major_id_p_seqno, A.acno_p_seqno , A.corp_p_seqno, A.sup_flag , B.p_seqno,
B.acct_type, B.acct_key, B.acno_flag from crd_card A join act_acno B on
A.acno_p_seqno=B.acno_p_seqno;


COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."ACCT_KEY" IS '帳戶查詢碼 ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."ACCT_TYPE" IS '帳戶類別 ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."ACNO_FLAG" IS '商務卡總個繳詳細註     ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."ACNO_P_SEQNO" IS '帳戶流水號碼      ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."CARD_NO" IS '卡號         ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."CORP_P_SEQNO" IS '法人流水號碼 ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."CURRENT_CODE" IS '狀態碼       0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."ID_P_SEQNO" IS '卡人流水號碼 ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."MAJOR_ID_P_SEQNO" IS '正卡身分證流水號      ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."P_SEQNO" IS '總繳流水號     ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_ACNO"."SUP_FLAG" IS '正附卡別     0:正卡 1:附卡';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW Vcard_idno AS select A.card_no,A.major_card_no, A.current_code,
A.id_p_seqno, A.major_id_p_seqno, A.acno_p_seqno , A.corp_p_seqno, A.sup_flag
, A.card_type, decode(A.group_code,'','0000',A.group_code) as group_code
, B.id_no, B.id_no_code, B.chi_name from crd_card A join crd_idno B on
A.id_p_seqno=B.id_p_seqno;


COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."ACNO_P_SEQNO" IS '帳戶序號       ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."CARD_NO" IS '卡號 ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."CARD_TYPE" IS '卡種                 ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."CHI_NAME" IS '中文姓名          ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."CORP_P_SEQNO" IS '公司序號               ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."CURRENT_CODE" IS '狀態碼         ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."GROUP_CODE" IS '團代               ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."ID_NO" IS '身分證號       ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."ID_NO_CODE" IS '身分證辨識碼          ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."ID_P_SEQNO" IS '身分證序號       ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."MAJOR_CARD_NO" IS '主卡卡號      ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."MAJOR_ID_P_SEQNO" IS '主卡身分證序號           ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO"."SUP_FLAG" IS '主附卡別      ';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW Vcard_idno_m AS select A.card_no,A.major_card_no, A.current_code,
A.id_p_seqno, A.major_id_p_seqno, A.acno_p_seqno , A.corp_p_seqno, A.sup_flag
, A.card_type, decode(A.group_code,'','0000',A.group_code) as group_code
, B.id_no, B.id_no_code, B.chi_name from crd_card A join crd_idno B on
A.major_id_p_seqno=B.id_p_seqno;


COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."ACNO_P_SEQNO" IS '帳戶序號';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."CARD_NO" IS '卡號';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."CARD_TYPE" IS '卡片種類';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."CHI_NAME" IS '中文姓名';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."CORP_P_SEQNO" IS '公司戶序號';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."CURRENT_CODE" IS '卡片狀態碼 ';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."GROUP_CODE" IS '團代';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."ID_NO" IS '身分證字號';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."ID_NO_CODE" IS '身分證辨識碼';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."ID_P_SEQNO" IS '身分證序號';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."MAJOR_CARD_NO" IS '主卡卡號';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."MAJOR_ID_P_SEQNO" IS '主卡身分證序號';

COMMENT ON COLUMN "ECSCRDB "."VCARD_IDNO_M"."SUP_FLAG" IS '正附卡別';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW Vall_ACCT_TYPE AS select acct_type, chin_name, card_indicator,
'N' as debit_flag from ptr_acct_type union select acct_type, chin_name,
card_indicator, 'Y' as debit_flag from dbp_acct_type;


COMMENT ON COLUMN "ECSCRDB "."VALL_ACCT_TYPE"."ACCT_TYPE" IS '帳戶類別';

COMMENT ON COLUMN "ECSCRDB "."VALL_ACCT_TYPE"."CARD_INDICATOR" IS '商務卡類旗標';

COMMENT ON COLUMN "ECSCRDB "."VALL_ACCT_TYPE"."CHIN_NAME" IS '中文姓名';

COMMENT ON COLUMN "ECSCRDB "."VALL_ACCT_TYPE"."DEBIT_FLAG" IS 'Debit卡旗標';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW VCCA_EXCEPTION AS SELECT sys_id 		as bin_type    	,	/*  */
sys_key 		as exc_code    	,	/*           */ sys_data1 as exc_desc ,
 /*         */ sys_data2   as nccc_pickup	   ,  /* NCCC: 0,1 */ crt_date
							,  /*         */ crt_user ,  /*         */ apr_date 			
			,  /*         */ apr_user ,  /*         */ mod_user ,  /*  
              */ mod_time ,  /*                 */ mod_pgm ,  /* */ mod_seqno
     						/*                 */ FROM CCA_sys_parm1 WHERE sys_id in ('NCCC','VISA','MAST','JCB');


COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."APR_DATE" IS '覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."APR_USER" IS '覆核人員';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."BIN_TYPE" IS '卡別';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."CRT_DATE" IS '鍵檔日期';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."CRT_USER" IS '鍵檔人員';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."EXC_CODE" IS '原因碼';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."EXC_DESC" IS '原因碼說明';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."MOD_PGM" IS '異動程式';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."MOD_SEQNO" IS '異動註記';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."MOD_TIME" IS '異動時間';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."MOD_USER" IS '異動人員';

COMMENT ON COLUMN "ECSCRDB "."VCCA_EXCEPTION"."NCCC_PICKUP" IS 'NCCC留置碼';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";
CREATE VIEW VCCA_REGION AS SELECT sys_id 		as bin_type    	,	/*  */ sys_key
as regn_code    	,	/* region   */ sys_data1   as regn_desc ,  /*  */
crt_date 							,  /*      */ crt_user ,  /*      */ apr_date 
						,  /*      */ apr_user  							, /*      */ mod_user ,  /*
             */ mod_time ,  /*              */ mod_pgm ,  /*          
   */ mod_seqno /*              */ FROM cca_sys_parm2 WHERE sys_id in ('VISA','MAST','JCB');


COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."APR_DATE" IS '覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."APR_USER" IS '覆核人員';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."BIN_TYPE" IS '卡別';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."CRT_DATE" IS '鍵檔日期';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."CRT_USER" IS '鍵檔人員';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."MOD_PGM" IS '異動程式';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."MOD_SEQNO" IS '異動註記';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."MOD_TIME" IS '異動時間';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."MOD_USER" IS '異動人員';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."REGN_CODE" IS 'Region代碼';

COMMENT ON COLUMN "ECSCRDB "."VCCA_REGION"."REGN_DESC" IS 'Region代碼說明';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRTINST1";
CREATE VIEW VCCA_RISK_TYPE
AS
 SELECT  
   sys_key 		as risk_type    	,	/* 風險類別 */
   sys_data1   as risk_desc		,  /* 風險說明 */
   sys_data3   as high_risk_flag , /* 是否為高風險交易 Y/N */
   crt_date 							,  /*          */
   crt_user  							,  /*          */
   apr_date 							,  /*          */
   apr_user  							,  /*          */
   mod_user  							,  /*          */      
   mod_time    						,  /*          */      
   mod_pgm     						,  /*          */      
   mod_seqno      						/*          */      
 FROM cca_sys_parm1
 WHERE sys_id ='RISK'
;


COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."APR_DATE" IS '覆核日期';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."APR_USER" IS '覆核人員';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."CRT_DATE" IS '鍵檔日期';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."CRT_USER" IS '鍵檔人員';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."HIGH_RISK_FLAG" IS '高風險註記';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."MOD_PGM" IS '異動程式';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."MOD_SEQNO" IS '異動註記';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."MOD_TIME" IS '異動時間';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."MOD_USER" IS '異動人員';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."RISK_DESC" IS '風險類別說明';

COMMENT ON COLUMN "ECSCRDB "."VCCA_RISK_TYPE"."RISK_TYPE" IS '風險類別';







SET CURRENT SCHEMA = "ECSCRDB";                                                          
GRANT SELECT,INSERT,DELETE,UPDATE ON VALL_ACCT_TYPE         TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VCARD_ACNO             TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VCARD_IDNO             TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VCARD_IDNO_M           TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VCCA_EXCEPTION         TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VCCA_REGION            TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VCCA_RISK_TYPE         TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VDBM_BONUS_ACTIVE_NAME TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VMKT_ACCT_TYPE         TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VMKT_BONUS_ACTIVE_NAME TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VMKT_DCFUND_NAME       TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VMKT_FUND_NAME         TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VRSK_CHGBACK           TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VRSK_CTRLSEQNO_BIL     TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VRSK_CTRLSEQNO_BIL3    TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VRSK_CTRLSEQNO_DBB     TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON VRSK_CTRLSEQNO_DBB3    TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON V_FUNDP                TO USER crap1, USER dcdbmod ;
GRANT SELECT,INSERT,DELETE,UPDATE ON V_IDNO                 TO USER crap1, USER dcdbmod ;






SET CURRENT SCHEMA = "ECSCRDB";                                                          
GRANT SELECT ON VALL_ACCT_TYPE         TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VCARD_ACNO             TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VCARD_IDNO             TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VCARD_IDNO_M           TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VCCA_EXCEPTION         TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VCCA_REGION            TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VCCA_RISK_TYPE         TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VDBM_BONUS_ACTIVE_NAME TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VMKT_ACCT_TYPE         TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VMKT_BONUS_ACTIVE_NAME TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VMKT_DCFUND_NAME       TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VMKT_FUND_NAME         TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VRSK_CHGBACK           TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VRSK_CTRLSEQNO_BIL     TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VRSK_CTRLSEQNO_BIL3    TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VRSK_CTRLSEQNO_DBB     TO USER emdap41, USER emdap42 ;
GRANT SELECT ON VRSK_CTRLSEQNO_DBB3    TO USER emdap41, USER emdap42 ;
GRANT SELECT ON V_FUNDP                TO USER emdap41, USER emdap42 ;
GRANT SELECT ON V_IDNO                 TO USER emdap41, USER emdap42 ;




COMMIT WORK;

CONNECT RESET;

TERMINATE;


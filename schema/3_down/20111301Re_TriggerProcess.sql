connect to cr;

--drop new 

DROP TRIGGER ECSCRDB.TR_CMSCHG_IDNO_U;
DROP TRIGGER ECSCRDB.TR_CMSCHG_ACNO_U;
DROP TRIGGER ECSCRDB.TR_CMSCHG_IDNO_VD_U;
DROP TRIGGER ECSCRDB.TR_CMSCHG_ACNO_VD_U;
DROP TRIGGER ECSCRDB.TR_UPD_TSC_VD_CARD_U;
DROP TRIGGER ECSCRDB.TR_CRD_DP_DRAGON;
DROP TRIGGER ECSCRDB.TR_CRD_SUP_STOP;

-- forupdate 
DROP TRIGGER ECSCRDB.TR_CMS_CRD_CARD_CHG; 

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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


DROP TRIGGER ECSCRDB.TR_CMS_DBC_CARD_CHG;  

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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


DROP TRIGGER ECSCRDB.TR_CRDB_04_CRD_CHG;  

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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
--
DROP TRIGGER ECSCRDB.TR_CRDB_04_DBC_CHG;  

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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


DROP TRIGGER ECSCRDB.TR_VIP_CODE_LOG_A;
SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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

DROP TRIGGER ECSCRDB.TR_VIP_CODE_LOG_U;

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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



DROP TRIGGER ECSCRDB.TR_CURRENT_CODE_U;

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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


DROP TRIGGER ECSCRDB.TR_CRDB_04_PREFAB;

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
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


--------------------------- fin

COMMIT WORK;

CONNECT RESET;

TERMINATE;

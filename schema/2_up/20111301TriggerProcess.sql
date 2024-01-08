connect to cr;

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CMSCHG_IDNO_U AFTER
UPDATE
    OF chi_name,
    resident_zip,
    resident_addr1,
    resident_addr2,
    resident_addr3,
    resident_addr4,
    resident_addr5,
    company_zip,
    company_addr1,
    company_addr2,
    company_addr3,
    company_addr4,
    company_addr5,
    mail_zip,
    mail_addr1,
    mail_addr2,
    mail_addr3,
    mail_addr4,
    mail_addr5,
    office_area_code1,
    office_tel_no1,
    office_tel_ext1,
    office_area_code2,
    office_tel_no2,
    office_tel_ext2,
    home_area_code1,
    home_tel_no1,
    home_area_code2,
    home_tel_no2,
    cellar_phone,
    e_mail_addr ON
    CRD_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.chi_name,
    ' ') <> NVL(old.chi_name,
    ' ')
    OR NVL(new.resident_zip,
    ' ') <> NVL(old.resident_zip,
    ' ')
    OR NVL(new.resident_addr1,
    ' ') <> NVL(old.resident_addr1,
    ' ')
    OR NVL(new.resident_addr2,
    ' ') <> NVL(old.resident_addr2,
    ' ')
    OR NVL(new.resident_addr3,
    ' ') <> NVL(old.resident_addr3,
    ' ')
    OR NVL(new.resident_addr4,
    ' ') <> NVL(old.resident_addr4,
    ' ')
    OR NVL(new.resident_addr5,
    ' ') <> NVL(old.resident_addr5,
    ' ')
    OR NVL(new.company_zip,
    ' ') <> NVL(old.company_zip,
    ' ')
    OR NVL(new.company_addr1,
    ' ') <> NVL(old.company_addr1,
    ' ')
    OR NVL(new.company_addr2,
    ' ') <> NVL(old.company_addr2,
    ' ')
    OR NVL(new.company_addr3,
    ' ') <> NVL(old.company_addr3,
    ' ')
    OR NVL(new.company_addr4,
    ' ') <> NVL(old.company_addr4,
    ' ')
    OR NVL(new.company_addr5,
    ' ') <> NVL(old.company_addr5,
    ' ')
    OR NVL(new.mail_zip,
    ' ') <> NVL(old.mail_zip,
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
    OR NVL(new.office_area_code1,
    ' ') <> NVL(old.office_area_code1,
    ' ')
    OR NVL(new.office_tel_no1,
    ' ') <> NVL(old.office_tel_no1,
    ' ')
    OR NVL(new.office_tel_ext1,
    ' ') <> NVL(old.office_tel_ext1,
    ' ')
    OR NVL(new.office_area_code2,
    ' ') <> NVL(old.office_area_code2,
    ' ')
    OR NVL(new.office_tel_no2,
    ' ') <> NVL(old.office_tel_no2,
    ' ')
    OR NVL(new.office_tel_ext2,
    ' ') <> NVL(old.office_tel_ext2,
    ' ')
    OR NVL(new.home_area_code1,
    ' ') <> NVL(old.home_area_code1,
    ' ')
    OR NVL(new.home_tel_no1,
    ' ') <> NVL(old.home_tel_no1,
    ' ')
    OR NVL(new.home_area_code2,
    ' ') <> NVL(old.home_area_code2,
    ' ')
    OR NVL(new.home_tel_no2,
    ' ') <> NVL(old.home_tel_no2,
    ' ')
    OR NVL(new.cellar_phone,
    ' ') <> NVL(old.cellar_phone,
    ' ')
    OR NVL(new.e_mail_addr,
    ' ') <> NVL(old.e_mail_addr,
    ' '))
BEGIN DECLARE tmpVar INT;--
  DECLARE step_flag     varchar(1);--
  DECLARE ls_mod_pgm    VARCHAR(20);--

	  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
	  declare exit handler for SQLEXCEPTION, SQLWARNING
			  signal   sqlstate 'TCIU0'  set message_text = step_flag;--

	  set step_flag    = '0';--
	
  set ls_mod_pgm= 'TR_CMSCHG_IDNO_U';--

/*******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -------------------------------------
   1.0        2020/4/22   Wendy          Created this trigger.
   1.1        2020/6/9    Sunny          Fix hhmmss -> hh24miss
   1.2        2020/8/19   JustinWu       mod_time and mod_date's based date -> sysdate
   1.3        2020/9/9    Sunny          add ls_mod_pgm,upper case
   1.4        2020/9/14   JustinWu       address -> nvl(address,'')
*******************************************************************************/

-- chi_name  
 IF NVL(new.chi_name,
' ') <> NVL(old.chi_name,
' ') THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'chi_name',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.chi_name,''),
NVL(NEW.chi_name,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--resident 
 IF (NVL(new.resident_zip,
' ') <> NVL(old.resident_zip,
' ')
OR NVL(new.resident_addr1,
' ') <> NVL(old.resident_addr1,
' ')
OR NVL(new.resident_addr2,
' ') <> NVL(old.resident_addr2,
' ')
OR NVL(new.resident_addr3,
' ') <> NVL(old.resident_addr3,
' ')
OR NVL(new.resident_addr4,
' ') <> NVL(old.resident_addr4,
' ')
OR NVL(new.resident_addr5,
' ') <> NVL(old.resident_addr5,
' ')) THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'resident_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.resident_zip,'') || NVL(OLD.resident_addr1,'') || NVL(OLD.resident_addr2,'') || NVL(OLD.resident_addr3,'') || NVL(OLD.resident_addr4,'') || NVL(OLD.resident_addr5,''),
NVL(NEW.resident_zip,'') || NVL(NEW.resident_addr1,'') || NVL(NEW.resident_addr2,'') || NVL(NEW.resident_addr3,'') || NVL(NEW.resident_addr4,'') || NVL(NEW.resident_addr5,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--company  
 IF (NVL(new.company_zip,
' ') <> NVL(old.company_zip,
' ')
OR NVL(new.company_addr1,
' ') <> NVL(old.company_addr1,
' ')
OR NVL(new.company_addr2,
' ') <> NVL(old.company_addr2,
' ')
OR NVL(new.company_addr3,
' ') <> NVL(old.company_addr3,
' ')
OR NVL(new.company_addr4,
' ') <> NVL(old.company_addr4,
' ')
OR NVL(new.company_addr5,
' ') <> NVL(old.company_addr5,
' ')) THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'company_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.company_zip,'') || NVL(OLD.company_addr1,'') || NVL(OLD.company_addr2,'') || NVL(OLD.company_addr3,'') || NVL(OLD.company_addr4,'') || NVL(OLD.company_addr5,''),
NVL(NEW.company_zip,'') || NVL(NEW.company_addr1,'') || NVL(NEW.company_addr2,'') || NVL(NEW.company_addr3,'') || NVL(NEW.company_addr4,'') || NVL(NEW.company_addr5,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--

--mail 
 IF (NVL(new.mail_zip,
' ') <> NVL(old.mail_zip,
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
' ')) THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'mail_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.mail_zip,'') || NVL(OLD.mail_addr1,'') || NVL(OLD.mail_addr2,'') || NVL(OLD.mail_addr3,'') || NVL(OLD.mail_addr4,'') || NVL(OLD.mail_addr5,''),
NVL(NEW.mail_zip,'') || NVL(NEW.mail_addr1,'') || NVL(NEW.mail_addr2,'') || NVL(NEW.mail_addr3,'') || NVL(NEW.mail_addr4,'') || NVL(NEW.mail_addr5,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--office_tel_no_1
 IF (NVL(new.office_area_code1,
' ') <> NVL(old.office_area_code1,
' ')
OR NVL(new.office_tel_no1,
' ') <> NVL(old.office_tel_no1,
' ')
OR NVL(new.office_tel_ext1,
' ') <> NVL(old.office_tel_ext1,
' ')) THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'office_tel_no1',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.office_area_code1,'') || NVL(OLD.office_tel_no1,'') || NVL(OLD.office_tel_ext1,''),
NVL(NEW.office_area_code1,'') || NVL(NEW.office_tel_no1,'') || NVL(NEW.office_tel_ext1,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--office_tel_no_2
 IF (NVL(new.office_area_code2,
' ') <> NVL(old.office_area_code2,
' ')
OR NVL(new.office_tel_no2,
' ') <> NVL(old.office_tel_no2,
' ')
OR NVL(new.office_tel_ext2,
' ') <> NVL(old.office_tel_ext2,
' ')) THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'office_tel_no2',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.office_area_code2,'') || NVL(OLD.office_tel_no2,'') || NVL(OLD.office_tel_ext2,''),
NVL(NEW.office_area_code2,'') || NVL(NEW.office_tel_no2,'') || NVL(NEW.office_tel_ext2,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--home_tel_no_1
 IF (NVL(new.home_area_code1,
' ') <> NVL(old.home_area_code1,
' ')
OR NVL(new.home_tel_no1,
' ') <> NVL(old.home_tel_no1,
' ')) THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'home_tel_no1',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.home_area_code1,'') || NVL(OLD.home_tel_no1,''),
NVL(NEW.home_area_code1,'') || NVL(NEW.home_tel_no1,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--home_tel_no_2
 IF (NVL(new.home_area_code2,
' ') <> NVL(old.home_area_code2,
' ')
OR NVL(new.home_tel_no2,
' ') <> NVL(old.home_tel_no2,
' ')) THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'home_tel_no2',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.home_area_code2,'') || NVL(OLD.home_tel_no2,''),
NVL(NEW.home_area_code2,'') || NVL(NEW.home_tel_no2,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--cellar_phone
 IF NVL(new.cellar_phone,
' ') <> NVL(old.cellar_phone,
' ') THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'cellar_phone',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.cellar_phone,''),
NVL(NEW.cellar_phone,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--e_mail
 IF NVL(new.e_mail_addr,
' ') <> NVL(old.e_mail_addr,
' ') THEN
INSERT
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
'',
'',
'',
'N',
'crd_idno',
'e_mail_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
OLD.e_mail_addr,
NEW.e_mail_addr,
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CMSCHG_ACNO_U AFTER
UPDATE
    OF e_mail_ebill,
    bill_apply_flag,
    bill_sending_zip,
    bill_sending_addr1,
    bill_sending_addr2,
    bill_sending_addr3,
    bill_sending_addr4,
    bill_sending_addr5 ON
    ACT_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.e_mail_ebill,
    ' ') <> NVL(old.e_mail_ebill,
    ' ')
    OR NVL(new.bill_apply_flag,
    ' ') <> NVL(old.bill_apply_flag,
    ' ')
    OR NVL(new.bill_sending_zip,
    ' ') <> NVL(old.bill_sending_zip,
    ' ')
    OR NVL(new.bill_sending_addr1,
    ' ') <> NVL(old.bill_sending_addr1,
    ' ')
    OR NVL(new.bill_sending_addr2,
    ' ') <> NVL(old.bill_sending_addr2,
    ' ')
    OR NVL(new.bill_sending_addr3,
    ' ') <> NVL(old.bill_sending_addr3,
    ' ')
    OR NVL(new.bill_sending_addr4,
    ' ') <> NVL(old.bill_sending_addr4,
    ' ')
    OR NVL(new.bill_sending_addr5,
    ' ') <> NVL(old.bill_sending_addr5,
    ' ') )
BEGIN
  DECLARE tmpVar        INTEGER;--
  DECLARE step_flag     varchar(1);--
  DECLARE ls_mod_pgm    VARCHAR(20);--

	  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
	  declare exit handler for SQLEXCEPTION, SQLWARNING
			  signal   sqlstate 'TCAU0'  set message_text = step_flag;--

	  set step_flag    = '0';--
	  set ls_mod_pgm= 'TR_CMSCHG_ACNO_U';--

/*******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -------------------------------------
   1.0        2020/4/22   Wendy          Created this trigger.
   1.1        2020/6/9    Sunny          Fix hhmmss -> hh24miss
   1.2        2020/8/19   JustinWu       mod_time and mod_date's based date -> sysdate 
   1.3        2020/9/9    Sunny          add ls_mod_pgm,upper case
   1.4        2020/9/14   JustinWu       address -> nvl(address,'')
*******************************************************************************/
IF NVL(new.e_mail_ebill,
' ') <> NVL(old.e_mail_ebill,
' ') THEN
INSERT
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
VALUES(NEW.id_p_seqno,
NEW.acct_type,
NEW.p_seqno,
'',
'N',
'act_acno',
'e_mail_ebill',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.e_mail_ebill,''),
NVL(NEW.e_mail_ebill,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--

IF NVL(new.bill_apply_flag,
' ') <> NVL(old.bill_apply_flag,
' ') THEN
INSERT
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
'',
'N',
'act_acno',
'bill_apply_flag',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.bill_apply_flag,''),
NVL(NEW.bill_apply_flag,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--

IF (NVL(new.bill_sending_zip,
' ') <> NVL(old.bill_sending_zip,
' ')
OR NVL(new.bill_sending_addr1,
' ') <> NVL(old.bill_sending_addr1,
' ')
OR NVL(new.bill_sending_addr2,
' ') <> NVL(old.bill_sending_addr2,
' ')
OR NVL(new.bill_sending_addr3,
' ') <> NVL(old.bill_sending_addr3,
' ')
OR NVL(new.bill_sending_addr4,
' ') <> NVL(old.bill_sending_addr4,
' ')
OR NVL(new.bill_sending_addr5,
' ') <> NVL(old.bill_sending_addr5,
' ')) THEN
INSERT
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
'',
'N',
'act_acno',
'bill_sending_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.bill_sending_zip,'') || NVL(OLD.bill_sending_addr1,'') || NVL(OLD.bill_sending_addr2,'') || NVL(OLD.bill_sending_addr3,'') || NVL(OLD.bill_sending_addr4,'') || NVL(OLD.bill_sending_addr5,''),
NVL(NEW.bill_sending_zip,'') || NVL(NEW.bill_sending_addr1,'') || NVL(NEW.bill_sending_addr2,'') || NVL(NEW.bill_sending_addr3,'') || NVL(NEW.bill_sending_addr4,'') || NVL(NEW.bill_sending_addr5,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CMSCHG_IDNO_VD_U AFTER
UPDATE
    OF chi_name,
    resident_zip,
    resident_addr1,
    resident_addr2,
    resident_addr3,
    resident_addr4,
    resident_addr5,
    company_zip,
    company_addr1,
    company_addr2,
    company_addr3,
    company_addr4,
    company_addr5,
    mail_zip,
    mail_addr1,
    mail_addr2,
    mail_addr3,
    mail_addr4,
    mail_addr5,
    office_area_code1,
    office_tel_no1,
    office_tel_ext1,
    office_area_code2,
    office_tel_no2,
    office_tel_ext2,
    home_area_code1,
    home_tel_no1,
    home_area_code2,
    home_tel_no2,
    cellar_phone,
    e_mail_addr,
    eng_name ON
    DBC_IDNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.chi_name,
    ' ') <> NVL(old.chi_name,
    ' ')
    OR NVL(new.resident_zip,
    ' ') <> NVL(old.resident_zip,
    ' ')
    OR NVL(new.resident_addr1,
    ' ') <> NVL(old.resident_addr1,
    ' ')
    OR NVL(new.resident_addr2,
    ' ') <> NVL(old.resident_addr2,
    ' ')
    OR NVL(new.resident_addr3,
    ' ') <> NVL(old.resident_addr3,
    ' ')
    OR NVL(new.resident_addr4,
    ' ') <> NVL(old.resident_addr4,
    ' ')
    OR NVL(new.resident_addr5,
    ' ') <> NVL(old.resident_addr5,
    ' ')
    OR NVL(new.company_zip,
    ' ') <> NVL(old.company_zip,
    ' ')
    OR NVL(new.company_addr1,
    ' ') <> NVL(old.company_addr1,
    ' ')
    OR NVL(new.company_addr2,
    ' ') <> NVL(old.company_addr2,
    ' ')
    OR NVL(new.company_addr3,
    ' ') <> NVL(old.company_addr3,
    ' ')
    OR NVL(new.company_addr4,
    ' ') <> NVL(old.company_addr4,
    ' ')
    OR NVL(new.company_addr5,
    ' ') <> NVL(old.company_addr5,
    ' ')
    OR NVL(new.mail_zip,
    ' ') <> NVL(old.mail_zip,
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
    OR NVL(new.office_area_code1,
    ' ') <> NVL(old.office_area_code1,
    ' ')
    OR NVL(new.office_tel_no1,
    ' ') <> NVL(old.office_tel_no1,
    ' ')
    OR NVL(new.office_tel_ext1,
    ' ') <> NVL(old.office_tel_ext1,
    ' ')
    OR NVL(new.office_area_code2,
    ' ') <> NVL(old.office_area_code2,
    ' ')
    OR NVL(new.office_tel_no2,
    ' ') <> NVL(old.office_tel_no2,
    ' ')
    OR NVL(new.office_tel_ext2,
    ' ') <> NVL(old.office_tel_ext2,
    ' ')
    OR NVL(new.home_area_code1,
    ' ') <> NVL(old.home_area_code1,
    ' ')
    OR NVL(new.home_tel_no1,
    ' ') <> NVL(old.home_tel_no1,
    ' ')
    OR NVL(new.home_area_code2,
    ' ') <> NVL(old.home_area_code2,
    ' ')
    OR NVL(new.home_tel_no2,
    ' ') <> NVL(old.home_tel_no2,
    ' ')
    OR NVL(new.cellar_phone,
    ' ') <> NVL(old.cellar_phone,
    ' ')
    OR NVL(new.e_mail_addr,
    ' ') <> NVL(old.e_mail_addr,
    ' ')
    OR NVL(new.eng_name,
    ' ') <> NVL(old.eng_name,
    ' '))
BEGIN
  DECLARE tmpVar        INTEGER;--
  DECLARE step_flag     VARCHAR(1);--
  DECLARE ls_mod_pgm    VARCHAR(20);--

	  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
	  declare exit handler for SQLEXCEPTION, SQLWARNING		  
			  signal   sqlstate 'TCIVU'  set message_text = step_flag;--

	  set step_flag    = '0';--
	  set ls_mod_pgm   = 'TR_CMSCHG_IDNO_VD_U';--

/*******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -------------------------------------
   1.0        2020/4/22   Wendy          Created this trigger.
   1.1        2020/6/9    Sunny          Fix hhmmss -> hh24miss
   1.2        2020/8/19   JustinWu       mod_time and mod_date's based date -> sysdate
   1.3        2020/9/9    Sunny          add ls_mod_pgm,upper case
   1.4        2020/9/14   JustinWu       address -> nvl(address,'')
*******************************************************************************/
-- chi_name 
 IF NVL(new.chi_name,
' ') <> NVL(old.chi_name,
' ') THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'chi_name',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
OLD.chi_name,
NEW.chi_name,
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--resident 
 IF (NVL(new.resident_zip,
' ') <> NVL(old.resident_zip,
' ')
OR NVL(new.resident_addr1,
' ') <> NVL(old.resident_addr1,
' ')
OR NVL(new.resident_addr2,
' ') <> NVL(old.resident_addr2,
' ')
OR NVL(new.resident_addr3,
' ') <> NVL(old.resident_addr3,
' ')
OR NVL(new.resident_addr4,
' ') <> NVL(old.resident_addr4,
' ')
OR NVL(new.resident_addr5,
' ') <> NVL(old.resident_addr5,
' ')) THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'resident_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.resident_zip,'') || NVL(OLD.resident_addr1,'') || NVL(OLD.resident_addr2,'') || NVL(OLD.resident_addr3,'') || NVL(OLD.resident_addr4,'') || NVL(OLD.resident_addr5,''),
NVL(NEW.resident_zip,'') || NVL(NEW.resident_addr1,'') || NVL(NEW.resident_addr2,'') || NVL(NEW.resident_addr3,'') || NVL(NEW.resident_addr4,'') || NVL(NEW.resident_addr5,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--company  
 IF (NVL(new.company_zip,
' ') <> NVL(old.company_zip,
' ')
OR NVL(new.company_addr1,
' ') <> NVL(old.company_addr1,
' ')
OR NVL(new.company_addr2,
' ') <> NVL(old.company_addr2,
' ')
OR NVL(new.company_addr3,
' ') <> NVL(old.company_addr3,
' ')
OR NVL(new.company_addr4,
' ') <> NVL(old.company_addr4,
' ')
OR NVL(new.company_addr5,
' ') <> NVL(old.company_addr5,
' ')) THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'company_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.company_zip,'') || NVL(OLD.company_addr1,'') || NVL(OLD.company_addr2,'') || NVL(OLD.company_addr3,'') || NVL(OLD.company_addr4,'') || NVL(OLD.company_addr5,''),
NVL(NEW.company_zip,'') || NVL(NEW.company_addr1,'') || NVL(NEW.company_addr2,'') || NVL(NEW.company_addr3,'') || NVL(NEW.company_addr4,'') || NVL(NEW.company_addr5,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--mail 
 IF (NVL(new.mail_zip,
' ') <> NVL(old.mail_zip,
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
' ')) THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'mail_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.mail_zip,'') || NVL(OLD.mail_addr1,'') || NVL(OLD.mail_addr2,'') || NVL(OLD.mail_addr3,'') || NVL(OLD.mail_addr4,'') || NVL(OLD.mail_addr5,''),
NVL(NEW.mail_zip,'') || NVL(NEW.mail_addr1,'') || NVL(NEW.mail_addr2,'') || NVL(NEW.mail_addr3,'') || NVL(NEW.mail_addr4,'') || NVL(NEW.mail_addr5,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--office_tel_no_1
 IF (NVL(new.office_area_code1,
' ') <> NVL(old.office_area_code1,
' ')
OR NVL(new.office_tel_no1,
' ') <> NVL(old.office_tel_no1,
' ')
OR NVL(new.office_tel_ext1,
' ') <> NVL(old.office_tel_ext1,
' ')) THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'office_tel_no1',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.office_area_code1 || OLD.office_tel_no1 || OLD.office_tel_ext1,''),
NVL(NEW.office_area_code1 || NEW.office_tel_no1 || NEW.office_tel_ext1,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--office_tel_no_2
 IF (NVL(new.office_area_code2,
' ') <> NVL(old.office_area_code2,
' ')
OR NVL(new.office_tel_no2,
' ') <> NVL(old.office_tel_no2,
' ')
OR NVL(new.office_tel_ext2,
' ') <> NVL(old.office_tel_ext2,
' ')) THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'office_tel_no2',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.office_area_code2 || OLD.office_tel_no2 || OLD.office_tel_ext2,''),
NVL(NEW.office_area_code2 || NEW.office_tel_no2 || NEW.office_tel_ext2,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--home_tel_no_1
 IF (NVL(new.home_area_code1,
' ') <> NVL(old.home_area_code1,
' ')
OR NVL(new.home_tel_no1,
' ') <> NVL(old.home_tel_no1,
' ')) THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'home_tel_no1',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.home_area_code1 || OLD.home_tel_no1,''),
NVL(NEW.home_area_code1 || NEW.home_tel_no1,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--home_tel_no_2
 IF (NVL(new.home_area_code2,
' ') <> NVL(old.home_area_code2,
' ')
OR NVL(new.home_tel_no2,
' ') <> NVL(old.home_tel_no2,
' ')) THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'home_tel_no2',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.home_area_code2 || OLD.home_tel_no2,''),
NVL(NEW.home_area_code2 || NEW.home_tel_no2,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--cellar_phone
 IF NVL(new.cellar_phone,
' ') <> NVL(old.cellar_phone,
' ') THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'cellar_phone',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.cellar_phone,''),
NVL(NEW.cellar_phone,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--e_mail
 IF NVL(new.e_mail_addr,
' ') <> NVL(old.e_mail_addr,
' ') THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'e_mail_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.e_mail_addr,''),
NVL(NEW.e_mail_addr,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
--eng_name
 IF NVL(new.eng_name,
' ') <> NVL(old.eng_name,
' ') THEN
INSERT
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
'',
'',
'',
'Y',
'dbc_idno',
'eng_name',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.eng_name,''),
NVL(NEW.eng_name,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
END;

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE 
TRIGGER TR_CMSCHG_ACNO_VD_U AFTER
UPDATE
    OF e_mail_ebill,
    bill_apply_flag,
    bill_sending_zip,
    bill_sending_addr1,
    bill_sending_addr2,
    bill_sending_addr3,
    bill_sending_addr4,
    bill_sending_addr5 ON
    DBA_ACNO REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(new.e_mail_ebill,
    ' ') <> NVL(old.e_mail_ebill,
    ' ')
    OR NVL(new.bill_apply_flag,
    ' ') <> NVL(old.bill_apply_flag,
    ' ')
    OR NVL(new.bill_sending_zip,
    ' ') <> NVL(old.bill_sending_zip,
    ' ')
    OR NVL(new.bill_sending_addr1,
    ' ') <> NVL(old.bill_sending_addr1,
    ' ')
    OR NVL(new.bill_sending_addr2,
    ' ') <> NVL(old.bill_sending_addr2,
    ' ')
    OR NVL(new.bill_sending_addr3,
    ' ') <> NVL(old.bill_sending_addr3,
    ' ')
    OR NVL(new.bill_sending_addr4,
    ' ') <> NVL(old.bill_sending_addr4,
    ' ')
    OR NVL(new.bill_sending_addr5,
    ' ') <> NVL(old.bill_sending_addr5,
    ' ') )
BEGIN
  DECLARE tmpVar        INTEGER;--
  DECLARE step_flag     varchar(1);--
  DECLARE ls_mod_pgm    VARCHAR(20);--

	  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
	  declare exit handler for SQLEXCEPTION, SQLWARNING
			  signal   sqlstate 'TCAVU'  set message_text = step_flag;--

	  set step_flag    = '0';--
	  set ls_mod_pgm= 'TR_CMSCHG_ACNO_VD_U';--

/*******************************************************************************
   REVISIONS
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -------------------------------------
   1.0        2020/4/22   Wendy          Created this trigger.
   1.1        2020/6/9    Sunny          Fix hhmmss -> hh24miss
   1.2        2020/8/19   JustinWu       mod_time and mod_date's based date -> sysdate
   1.3        2020/9/9    Sunny          add ls_mod_pgm,upper case
   1.4        2020/9/14   JustinWu       address -> nvl(address,'')
*******************************************************************************/
IF NVL(new.e_mail_ebill,
' ') <> NVL(old.e_mail_ebill,
' ') THEN
INSERT
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
'',
'N',
'dba_acno',
'e_mail_ebill',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.e_mail_ebill,''),
NVL(NEW.e_mail_ebill,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--

IF NVL(new.bill_apply_flag,
' ') <> NVL(old.bill_apply_flag,
' ') THEN
INSERT
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
'',
'N',
'dba_acno',
'bill_apply_flag',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.bill_apply_flag,''),
NVL(NEW.bill_apply_flag,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--

IF (NVL(new.bill_sending_zip,
' ') <> NVL(old.bill_sending_zip,
' ')
OR NVL(new.bill_sending_addr1,
' ') <> NVL(old.bill_sending_addr1,
' ')
OR NVL(new.bill_sending_addr2,
' ') <> NVL(old.bill_sending_addr2,
' ')
OR NVL(new.bill_sending_addr3,
' ') <> NVL(old.bill_sending_addr3,
' ')
OR NVL(new.bill_sending_addr4,
' ') <> NVL(old.bill_sending_addr4,
' ')
OR NVL(new.bill_sending_addr5,
' ') <> NVL(old.bill_sending_addr5,
' ')) THEN
INSERT
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
'',
'N',
'dba_acno',
'bill_sending_addr',
NVL(NEW.mod_user,''),
TO_CHAR(sysdate,
'YYYYMMDD'),
TO_CHAR(sysdate,
'hh24miss'),
NVL(OLD.bill_sending_zip,'') || NVL(OLD.bill_sending_addr1,'') || NVL(OLD.bill_sending_addr2,'') || NVL(OLD.bill_sending_addr3,'') || NVL(OLD.bill_sending_addr4,'') || NVL(OLD.bill_sending_addr5,''),
NVL(NEW.bill_sending_zip,'') || NVL(NEW.bill_sending_addr1,'') || NVL(NEW.bill_sending_addr2,'') || NVL(NEW.bill_sending_addr3,'') || NVL(NEW.bill_sending_addr4,'') || NVL(NEW.bill_sending_addr5,''),
sysdate,
NVL(ls_mod_pgm,''));--
END IF;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_UPD_TSC_VD_CARD_U
AFTER UPDATE
 OF current_code
 ON DBC_CARD
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW MODE DB2SQL
WHEN (
      nvl(old.current_code,0) <> nvl(new.current_code,0)
     )
	  
BEGIN

  DECLARE CONTINUE HANDLER FOR NOT FOUND                BEGIN return; END;--
--declare exit handler for sqlstate '02000'   
  declare exit handler for SQLEXCEPTION, SQLWARNING
          signal   sqlstate '38S02' set message_text='Error : tr_upd_tsc_vd_card_u Error.';--

/******************************************************************************
   NAME   update TSC_VD_CARD
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
      1.0        2020/9/10   Wilson           program initial 
******************************************************************************/
	 
	 update tsc_vd_card
      set current_code = new.current_code
         ,oppost_date  = new.oppost_date
         ,mod_user     = 'trigger'
         ,mod_pgm      = 'tr_upd_tsc_vd_card'
         ,mod_time     = sysdate
         ,mod_seqno    = nvl(mod_seqno,0)+1
   where vd_card_no       = old.card_no
     and current_code  = old.current_code;--

END
;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_CRD_DP_DRAGON AFTER UPDATE
    OF "CURRENT_CODE" ON
    CRD_CARD_PP REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW MODE DB2SQL
    WHEN ( nvl(old.current_code ,
    0) <> nvl(new.current_code ,
    0) )
BEGIN
    DECLARE wk_audcode VARCHAR(1);--
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
 IF (old.vip_kind = '2') THEN IF (old.current_code = '0') THEN IF (new.current_code <> '0') THEN INSERT
    INTO
        crd_dp_dragon( mod_audcode ,
        dp_card_no ,
        id_p_seqno ,
        old_dp_card_no ,
        post_flag ,
        post_date ,
        crt_date ,
        crt_user ,
        mod_time ,
        mod_pgm ,
        mod_user )
    VALUES ( wk_audcode ,
    new.pp_card_no,
    new.id_p_seqno,
    ' ',
    'N',
    ' ',
    to_char(sysdate,
    'yyyymmdd') ,
    'trigger' ,
    to_char(sysdate,
    'yyyymmddhh24miss') ,
    'TR_CRD_DP_DRAGON' ,
    'trigger' );--
--
--
END IF;--
END IF;--
END IF;--
--
 IF (old.vip_kind = '2') THEN IF (old.current_code <> '0') THEN IF (new.current_code = '0') THEN DELETE
FROM
    crd_dp_dragon
WHERE
    id_p_seqno IN (
        SELECT crd_dp_dragon.id_p_seqno
    FROM
        crd_card,
        crd_dp_dragon
    WHERE
        crd_card.id_p_seqno = crd_dp_dragon.id_p_seqno)
    AND crd_dp_dragon.post_flag = 'N'
    AND crd_dp_dragon.mod_audcode = 'D'	;--
END IF;--
END IF;--
END IF;--
--
END;

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE OR REPLACE TRIGGER "ECSCRDB"."TR_CRD_SUP_STOP"
  AFTER UPDATE OF "CURRENT_CODE"
  ON "ECSCRDB"."CRD_CARD"
  REFERENCING 
    OLD AS OLD
    NEW AS NEW
  FOR EACH ROW
  WHEN (
      nvl(old.current_code,0) <> nvl(new.current_code,0)
      and (nvl(new.current_code,0) = '1'
      or nvl(new.current_code,0) = '3')
                                              ) 
BEGIN

DECLARE wk_spec_flag VARCHAR(1);--
DECLARE wk_trans_type VARCHAR(1) DEFAULT '6';--
DECLARE wk_db_cnt INTEGER DEFAULT 0;--

DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return; END;--


 if(nvl(new.current_code,0) = '3') then set wk_trans_type = '5'; end if ;--


  FOR V AS CURSOR_CARDNO CURSOR FOR SELECT card_no FROM crd_card where major_card_no = new.card_no and current_code ='0' and sup_flag='1'
  DO

    BEGIN
        update crd_card 
            set current_code = '1',
                oppost_date = to_char(sysdate,'yyyymmdd'),
                oppost_reason = 'Q1',
                lost_fee_code = new.lost_fee_code,
                mod_user = new.mod_user,
                mod_pgm = 'TR_CRD_SUP_STOP'
            where card_no = V.card_no;--
            
        begin
            select spec_flag
            into wk_spec_flag
            from cca_card_base
            where card_no = V.card_no;--
        end;   --
        
        if(wk_spec_flag = 'Y') then
            update cca_card_base 
                set spec_status ='', 
                    spec_flag ='N',
                    spec_mst_vip_amt = 0,
                    spec_del_date ='',
                    spec_remark ='',
                    mod_user = new.mod_user,
                    mod_pgm = 'TR_CRD_SUP_STOP'
                where card_no = V.card_no;--
                
            delete cca_special_visa where card_no = V.card_no;--
            
            insert into cca_spec_his (log_date,log_time,card_no,bin_type,from_type,aud_code,pgm_id,log_user)
                   values(to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),V.card_no,old.bin_type,'1','D','TR_CRD_SUP_STOP',new.mod_user);--
        end if;--
        
        begin
            select count(*) as db_cnt
            into wk_db_cnt
            from cca_opposition
            where card_no = V.card_no;--
        end; --

        if (wk_db_cnt>0) then
            delete cca_opposition where card_no = V.card_no;--
        end if;--
        
        insert into cca_opposition(
                    card_no,
                    card_acct_idx,
                    debit_flag,
                    card_type,
                    bin_type,
                    group_code,
                    from_type,
                    oppo_type,
                    oppo_status,
                    oppo_user,
                    oppo_date,
                    oppo_time,
                    neg_del_date,
                    renew_flag,
                    cycle_credit,
                    opp_remark,
                    mail_branch,
                    lost_fee_flag,
                    excep_flag,
                    except_proc_flag,
                    neg_resp_code,
                    visa_resp_code,
                    mst_reason_code,
                    vis_reason_code,
                    curr_tot_tx_amt,
                    curr_tot_cash_amt,
                    bank_acct_no,
                    crt_date,
                    crt_time,
                    crt_user,
                    mod_time,
                    mod_user,
                    mod_pgm,
                    mod_seqno
            ) values(V.card_no ,
                    nvl((select card_acct_idx from cca_card_base where card_no = new.card_no fetch first 1 rows only),0),
                    'N',
                    new.card_type,
                    new.bin_type,
                    new.group_code,
                    '1',
                    '1',
                    'Q1',
                    new.mod_user,
                    to_char(sysdate,'yyyymmdd'),
                    to_char(sysdate,'hh24miss'),
                    nvl((select del_date from cca_outgoing where card_no = new.card_no and oppost_date = to_char(sysdate,'yyyymmdd') and key_value != 'NCCC' fetch first 1 rows only),''),
                    'N',
                    '',
                    '',
                    '',
                    new.lost_fee_code,
                    '',
                    '0',
                    '',
                    '',
                    nvl((select neg_opp_reason from cca_opp_type_reason where opp_status = 'Q1'),''),
                    decode(
                    new.bin_type,
                    'V',
                    nvl((select vis_excep_code from cca_opp_type_reason where opp_status = 'Q1'),''),
                    'M',
                    nvl((select mst_auth_code from cca_opp_type_reason where opp_status = 'Q1'),''),
                    'J',
                    nvl((select jcb_excp_code from cca_opp_type_reason where opp_status = 'Q1'),''),
                    ''
                    ),
                    0,
                    0,
                    '',
                    to_char(sysdate,'yyyymmdd'),
                    to_char(sysdate,'hh24miss'),
                    new.mod_user,
                    sysdate,
                    new.mod_user,
                    'TR_CRD_SUP_STOP',
                    1 
                    );--
            
        insert into onbat_2ecs(
                    trans_type,
                    to_which,
                    dog,
                    proc_mode,
                    card_no,
                    acct_type,
                    acno_p_seqno,
                    id_p_seqno,
                    opp_type,
                    opp_reason,
                    opp_date,
                    is_renew,
                    curr_tot_lost_amt,
                    mail_branch,
                    lost_fee_flag,
                    debit_flag
        )values(
                   wk_trans_type,
                   '1',
                   sysdate,
                   'O',
                   V.card_no,
                   old.acct_type,
                   old.acno_p_seqno,
                   old.id_p_seqno,
                   '1',
                   'Q1',
                   to_char(sysdate,'yyyymmdd'),
                   'N',
                   100,
                   new.mail_branch,
                   new.lost_fee_code,
                   'N'                   
        );--
        
        INSERT INTO cca_outgoing( 
                crt_date ,
                crt_time ,
                card_no ,
                key_value ,
                key_table ,
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
                   ) select 
                to_char(sysdate,'yyyymmdd'),
                to_char(sysdate,'hh24miss'),
                V.card_no,
                key_value,
                key_table,
                act_code,
                crt_user,
                proc_flag,
                send_times,
                data_from,
                data_type,
                bin_type,
                vip_amt,
                sysdate,
                'TR_CRD_SUP_STOP',
                electronic_card_no,
                '1',
                new_end_date,
                oppost_date,
                'Q1',
                reason_code
        from cca_outgoing 
        where card_no = new.card_no
        and oppost_date = to_char(sysdate,'yyyymmdd')
        and key_value != 'TWMP'
        and key_value != 'OEMPAY';--

    END;--
END FOR;   --
END;

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
	decode(new.mod_user,'',new.mod_pgm,new.mod_user) ,
	to_char(sysdate,'yyyymmdd') ,
	to_char(sysdate,'hh24miss') ,
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
	decode(new.mod_user,'',new.mod_pgm,new.mod_user) ,
	to_char(sysdate,'yyyymmdd') ,
	to_char(sysdate,'hh24miss') ,
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
    DECODE(NEW.mod_user,'',NEW.mod_pgm,NEW.mod_user),
    TO_CHAR(sysdate,
    'YYYYMMDD'),
    TO_CHAR(sysdate,
    'HH24MISS'),
    OLD.current_code,
    NEW.current_code,
    TO_CHAR(sysdate,
    'YYYYMMDDHH24MISS'),
    'TR_CRDB_04_CRD_CHG' );--
END IF;--
END;

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
    DECODE(NEW.mod_user,'',NEW.mod_pgm,NEW.mod_user),
    TO_CHAR(sysdate,
    'YYYYMMDD'),
    TO_CHAR(sysdate,
    'HH24MISS'),
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
CREATE TRIGGER TR_VIP_CODE_LOG_A AFTER
INSERT
    ON
    ACT_ACNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(old.VIP_CODE , ' ')<> NVL(new.VIP_CODE , ' ')
    OR NVL(old.VIP_REMARK , ' ')<> NVL(new.VIP_REMARK, ' ') ) BEGIN DECLARE tmpVar INT;--

DECLARE ls_pgm_type varchar(1);--

DECLARE ls_mod_user varchar(20);--

DECLARE ls_mod_pgm varchar(20);--

DECLARE step_flag varchar(1);--

DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN RETURN;--
END;--

DECLARE EXIT HANDLER FOR SQLEXCEPTION,
SQLWARNING SIGNAL SQLSTATE 'TVCLA' SET
message_text = step_flag;--

SET
step_flag = '0';--

SET
ls_pgm_type = '1';--

SET
ls_mod_user = '';--

SET
ls_mod_pgm = '';--

IF ls_mod_user IS NULL or ls_mod_user='' THEN SET
ls_mod_user = new.mod_user;--

IF ls_mod_user = 'system' THEN SET
ls_pgm_type = '2';--
END IF;--
END IF;--

IF ls_mod_pgm IS NULL or ls_mod_user='' THEN SET
ls_mod_pgm = new.mod_pgm;--
END IF;--

INSERT
    INTO
    act_vip_code_log ( log_date, log_time, log_type, p_seqno, acct_type, id_p_seqno, vip_code, vip_remark, mod_user, apr_user )
VALUES ( to_char(sysdate, 'yyyymmdd'), to_char(sysdate, 'hh24miss'), ls_pgm_type, new.p_seqno, new.acct_type, new.id_p_seqno, new.vip_code, new.vip_remark, decode(ls_pgm_type, '1', new.update_user, ls_mod_user), decode(ls_pgm_type, '1', new.mod_user, ls_mod_user) ) ;--
END;

DROP TRIGGER ECSCRDB.TR_VIP_CODE_LOG_U;

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";
CREATE TRIGGER TR_VIP_CODE_LOG_U AFTER
UPDATE
    OF VIP_CODE,
    VIP_REMARK ON
    ACT_ACNO REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW MODE DB2SQL
    WHEN ( NVL(old.VIP_CODE, ' ')<> NVL(new.VIP_CODE, ' ')
    OR NVL(old.VIP_REMARK, ' ')<> NVL(new.VIP_REMARK, ' ') ) BEGIN DECLARE tmpVar INT;--

DECLARE ls_pgm_type varchar(1);--

DECLARE ls_mod_user varchar(20);--

DECLARE ls_mod_pgm varchar(20);--

DECLARE step_flag varchar(1);--

DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN RETURN;--
END;--

DECLARE EXIT HANDLER FOR SQLEXCEPTION,
SQLWARNING SIGNAL SQLSTATE 'TVCLU' SET
message_text = step_flag;--

SET
step_flag = '0';--

SET
ls_pgm_type = '1';--

SET
ls_mod_user = '';--

SET
ls_mod_pgm = '';--

IF ls_mod_user IS NULL or ls_mod_user='' THEN SET
ls_mod_user = new.mod_user;--

	IF ls_mod_user = 'system' THEN SET
	ls_pgm_type = '2';--
	END IF;--
END IF;--

IF ls_mod_pgm IS NULL or ls_mod_user='' THEN SET
ls_mod_pgm = new.mod_pgm;--
END IF;--

INSERT
    INTO
    act_vip_code_log ( log_date, log_time, log_type, p_seqno, acct_type, id_p_seqno, vip_code, vip_remark, mod_user, apr_user )
VALUES ( to_char(sysdate, 'yyyymmdd'), to_char(sysdate, 'hh24miss'), ls_pgm_type, new.p_seqno, new.acct_type, new.id_p_seqno, new.vip_code, new.vip_remark, decode(ls_pgm_type, '1', new.update_user, ls_mod_user), decode(ls_pgm_type, '1', new.mod_user, ls_mod_user) ) ;--
END;

DROP TRIGGER ECSCRDB.TR_CURRENT_CODE_U;

SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB";
SET CURRENT PATH = "SYSIBM","SYSFUN","SYSPROC","SYSIBMADM","CRPINST1";

CREATE OR REPLACE TRIGGER "ECSCRDB"."TR_CURRENT_CODE_U"
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

   -- current_code    1停卡  2掛失  3強停 4其他 5 偽卡
   -- operation       01暫停 02恢復 03終止

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
              'LOST',
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
              'LOST',
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
    decode(new.mod_user,'',new.mod_pgm,new.mod_user),
    to_char(sysdate,
    'yyyymmdd'),
    to_char(sysdate,
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

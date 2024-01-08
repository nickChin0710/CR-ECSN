CONNECT TO CR;


---------------------------------
-- DDL Statements for User Defined Functions
---------------------------------


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_2num ( 
		as_str 		VARCHAR(20)
	)
  RETURNS decimal(15,2)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN

   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return 0; END;--
   
	if as_str = null or Rtrim(Ltrim(as_str))='' then
		return 0;--
	end if;--
   
	return decfloat(Rtrim(ltrim(as_str)));	--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_2ymd( a_dtime TIMESTAMP )
RETURNS varchar(8)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
	RETURN varchar_format(a_dtime,'yyyymmdd');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_auth_seqno()
RETURNS varchar(12)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- 2018-1218   JHW   initial
------------------------------------------------------------------------
BEGIN
--  DECLARE li_seqno int;--
--  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
--  set li_seqno = seq_auth_seqno.nextval;--
  
  RETURN varchar_format(sysdate,'yymmdd')||lpad(seq_auth_seqno.nextval,6,'0');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_case_seqno()
RETURNS varchar(6)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- 2017-0815   JHW   initial
------------------------------------------------------------------------
BEGIN
  --DECLARE ls_seqno varchar(10);--
--  DECLARE li_seqno int;--
--  set li_seqno = cms_case.nextval;--
--	RETURN substring(to_char(li_seqno,'000000'),2);--
   return lpad(cms_case.nextval,6,'0');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_ccar0270 ( 
  a_card_type VARCHAR(2)
, a_debt_flag varchar(1)
, a_crt_user varchar(10)
, a_vis_reason_code varchar(10)
, a_vis_resp_code varchar(10)
, a_mst_reason_code varchar(10)
, a_neg_resp_code varchar(10)
)
  RETURNS int
------------------------------------------------------------------------
-- ccar0270         
-- 2019-1014   JH    initial
------------------------------------------------------------------------
BEGIN 
   if a_card_type ='AX' then
      if a_vis_resp_code not in ('','300','303') then
        return 0;--
      end if;--
      return 1;      --
   end if;--
   
   if a_debt_flag ='Y' and lcase(a_crt_user)='ecs006' then
     return 1;--
   end if;--
   if a_debt_flag='Y' and trim(a_crt_user)<>'' and trim(a_crt_user)<'A' then
     return 1;--
   end if;--
   
   if trim(a_vis_reason_code)<>'' and trim(a_vis_resp_code) not in ('','00') then
     return 0;--
   end if;--

   if trim(a_mst_reason_code)<>'' and trim(a_neg_resp_code) not in ('','00') then
     return 0;--
   end if;--
   
   return 1;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_cmsr4240_fun ( mcht_no VARCHAR(15)
         , tot_amt decimal(11), tot_term decimal(5)
 )
  RETURNS decimal(11)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar):     
-- V00.0    JH    2017-10xx: initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_id_name vargraphic(20);--
   DECLARE ls_corp_name vargraphic(40);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return 0; END;--
      
   if nvl(mcht_no,'')='' then
      return 0;--
   end if;--
   if mcht_no not in ('0025800031','7850655292') then
      return 0;--
   end if;--

--    -
--                 :
-- 3,000 ~29,999   40 
--30,000 ~59,999   70 
--60,000     120 
   if mcht_no='0025800031' then
      if tot_amt>=3000 and tot_amt<=29999 then
         return 40;--
      end if;   --
      if tot_amt>=30000 and tot_amt<=59999 then
         return 70;--
      end if;   --
      if tot_amt>=60000 then
         return 120;--
      end if;--
      return 0;--
   end if;--

--                  :
--   3 6   50 
--   12   100 
   if mcht_no='7850655292' then
      if tot_term in (3,6) then
         return 50;--
      end if;--
      if tot_term = 12 then
         return 100;--
      end if;--
      return 0;--
   end if;--
   
   return 0;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_corp_p_seqno()
RETURNS varchar(10)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- 2017-0726   JHW   initial
------------------------------------------------------------------------
BEGIN
  --DECLARE ls_seqno varchar(10);--
--	RETURN substring(lpad(to_char(ecs_corp.nextval),10,'0'),2);--
	RETURN lpad(to_char(ecs_corp.nextval),10,'0');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_ctfc_seqno ( 
		a_str 		VARCHAR(15)
	)
  RETURNS varchar(15)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 2017-1207: JH  initial
------------------------------------------------------------------------
BEGIN

   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
	if a_str = null or Rtrim(Ltrim(a_str))='' then
		return '';--
	end if;--
	
	if substring(a_str,1,1) between 'A' and 'Z' then
	   return nvl(substring(a_str,2),'');--
	end if;--
   
	return nvl(a_str,'');	--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_curr_sort ( AS_CURR VARCHAR(3) )
  RETURNS int
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
--     
-- V00.00   JH    2017-10xx initial
------------------------------------------------------------------------
BEGIN 
	if as_curr = null or trim(as_curr)='' then
		return 1;--
	end if;--
   
   return decode(as_curr,'901',1,'TWD',1,'840',2,'USD',2,'392',3,'978',4,9);--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_date2fmt( a_date1 VARCHAR(8))
  RETURNS VARCHAR(10)
LANGUAGE SQL
DETERMINISTIC
NO EXTERNAL ACTION
READS SQL DATA
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_date1 varchar(10);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, not found BEGIN return ''; END;--

   if (a_date1 is null) then
      return '';--
   end if;--
   if length(a_date1)<=4 then
      return a_date1;--
   end if;--
   if length(a_date1) >6 then
      return substring(a_date1,1,4)||'/'||substring(a_date1,5,2)||'/'||substring(a_date1,7,2);--
   end if;--
   if length(a_date1) >4 then
      return substring(a_date1,1,4)||'/'||substring(a_date1,5,2);--
   end if;--

   return a_date1;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_date_add ( a_date VARCHAR(8)
      , a_yy int, a_mm int, a_dd int)
  RETURNS varchar(8)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar): get    -yy  -mm  -dd 
-- 2017-1010:  JH    initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_rc_date varchar(8);--
   declare ls_date varchar(8);--
   declare ldt_date  timestamp;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;   --
   
   if a_date is null or trim(a_date)='' then
      set ls_date =to_char(sysdate,'yyyymmdd');--
--   else if trim(a_date)='' then
--      return '';--
   else   
      set ls_date = substring(a_date||'0101',1,8);       --
   end if;--

   set ldt_date =timestamp_format(ls_date,'yyyymmdd') + a_yy years + a_mm months + a_dd days;--
   
   -- select timestamp_format(ls_date,'yyyymmdd') + a_yy years + a_mm months + a_dd days
     -- into ldt_date
     -- from SYSIBM.SYSDUMMY1
   if length(a_date)=4 then
      return to_char(ldt_date,'yyyy');--
   end if;   --
   if length(a_date)=6 then
      return to_char(ldt_date,'yyyymm');--
   end if;   --
   return to_char(ldt_date,'yyyymmdd');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_date_diff ( a_date1 VARCHAR(8), a_date2 varchar(8)
)
  RETURNS int
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar): get       
-- V00.00   JH    2017-1025
-- V00.01   JH    2018-1023
------------------------------------------------------------------------
BEGIN
   --DECLARE ls_rc_date varchar(8);--
   declare ls_date varchar(8);--
   declare ldt_date  timestamp;--
   declare ldt_date2  timestamp;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return null; END;--
   
   set ls_date = substring(a_date1||'0101',1,8);--
   set ldt_date =timestamp_format(ls_date,'yyyymmdd');--
   set ls_date = substring(a_date2||'0101',1,8);--
   set ldt_date2 =timestamp_format(ls_date,'yyyymmdd');--
   
   return days(ldt_date) - days(ldt_date2);--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_DC2TW_AMT ( 
		am_tw		decimal(13,4),
		am_dc	decimal(13,4),
      am_src_amt  decimal(13,4)
	)
  RETURNS decimal(13,0)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 2019-1204   JH    dec(13,4)
--V00.00 JH-107-1201: initial
------------------------------------------------------------------------
BEGIN 
   if nvl(am_dc,0)=0 or am_tw =am_dc then
      return am_src_amt;--
   end if;   --
	return round((am_src_amt * am_tw) / am_dc,0);--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_DC_AMT ( 
		as_curr 		VARCHAR(3),
		am_amt		decimal(13,4),
		am_dc_amt	decimal(13,4)
	)
  RETURNS decimal(13,4)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 2019-1204   JH    decimal(13,4)
------------------------------------------------------------------------
BEGIN 
	if as_curr = null or trim(as_curr)='' then
		return nvl(am_amt,0);--
	end if;--
	if trim(as_curr) in ('901','TWD') then
		return nvl(am_amt,0);--
	end if;--
	return nvl(am_dc_amt,0);	--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_DC_AMT2 ( 
		am_amt		decimal(13,4),
		am_dc_amt	decimal(13,4)
	)
  RETURNS decimal(13,4)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
--V00.01 JH-109-1204: decimal(13,4)
--V00.00 JH-107-1201: initial
------------------------------------------------------------------------
BEGIN 
   if nvl(am_dc_amt,0)<>0 then
      return am_dc_amt;--
   end if;   --
	return nvl(am_amt,0);--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_DC_CURR ( AS_CURR VARCHAR(3) )
  RETURNS VARCHAR(3)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
F1: BEGIN ATOMIC
	if as_curr = null or trim(as_curr)='' then
		return '901';--
	end if;--
	if trim(as_curr) in ('901','TWD') then
		return '901';--
	end if;--
	return trim(as_curr);	--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_ecs_stop()
RETURNS varchar(10)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- 2017-1222   JHW   initial
------------------------------------------------------------------------
BEGIN
--  DECLARE ls_seqno varchar(10);--
--  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;  --
  --DECLARE li_seqno int;--
  
  --set li_seqno = ecs_stop.nextval;--
  
	--set ls_seqno = to_char(sysdate,'yy')||substr(to_char(ecs_stop.nextval,'0000000000'),3,10);--
	--return ls_seqno;--
   return to_char(sysdate,'yy')||substr(lpad(ecs_stop.nextval,10,'0'),3);--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_hi_acctno ( a_s1 varchar(20) )
  RETURNS varchar(20)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_s1 varchar(20);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_s1 ='';--
   
   if a_s1 is null or a_s1='' then
      return '';--
   end if;--

   --      5~8 -
   set ls_s1 =substring(a_s1,1,4)||'XXXX'||substring(a_s1,9);--
   
   return ls_s1;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_hi_addr ( a_s1 vargraphic(100) )
  RETURNS vargraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_s1 vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_s1 ='';--
   
   if a_s1 is null then
      return '';--
   end if;--

   --  :1.     ->  7~10  -
   set ls_s1 =substring(a_s1,1,6)||'XXXX'||substring(a_s1,11);--
   
   return ls_s1;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_hi_cardno ( a_s1 varchar(19) )
  RETURNS varchar(19)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_s1 varchar(19);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_s1 ='';--
   
   if a_s1 is null then
      return '';--
   end if;--

   --       7~12 -
   set ls_s1 =substring(a_s1,1,6)||'XXXXXX'||substring(a_s1,13);--
   
   return ls_s1;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_hi_cname ( a_s1 vargraphic(50) )
  RETURNS vargraphic(50)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_s1 vargraphic(50);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_s1 ='';--
   
   if a_s1 is null then
      return '';--
   end if;--

   --    2 -
   set ls_s1 =substring(a_s1,1,1)||'X'||substring(a_s1,3);--
   
   return ls_s1;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_hi_email ( a_s1 varchar(50) )
  RETURNS varchar(50)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN 
   DECLARE ls_s1 	varchar(50);--
   declare li_pos int;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_s1 ='';--
   
   if a_s1 is null or a_s1='' then
      return '';--
   end if;--

   set li_pos =1;--
   --mail: @ 3 -
   pos_loop:   
   WHILE (li_pos <= length(a_s1)) DO
   	if substring(a_s1,li_pos,1)='@' then
   		leave pos_loop;--
   	end if;--
    	SET li_pos = li_pos + 1;--
  	END WHILE pos_loop;--
   
   if li_pos<=0 then
      return a_s1;--
   end if;--
   
   if li_pos<=3 then
      return 'XXX'||substring(a_s1,li_pos);--
   end if; --
   set ls_s1 =substring(a_s1,1,li_pos - 4)||'XXX'||substring(a_s1,li_pos);   --
   
   return ls_s1;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_hi_idno ( a_idno VARCHAR(20) )
  RETURNS VARCHAR(20)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_s1 varchar(20);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_s1 ='';--
   
   if a_idno is null or a_idno='' then
      return '';--
   end if;--
   if length(a_idno)<>10 then
      return a_idno;--
   end if;--

   --       4~7 
   set ls_s1 =substring(a_idno,1,3)||'XXXX'||substring(a_idno,8);--
   
   return ls_s1;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_hi_passport ( a_s1 varchar(20) )
  RETURNS varchar(20)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_s1 varchar(20);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_s1 ='';--
   
   if a_s1 is null or a_s1='' then
      return '';--
   end if;--

   --      4~7 
   set ls_s1 =substring(a_s1,1,3)||'XXXX'||substring(a_s1,8);--
   
   return ls_s1;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_hi_telno ( a_s1 varchar(20) )
  RETURNS varchar(20)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_s1 varchar(20);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_s1 ='';--
   
   if a_s1 is null or a_s1='' then
      return '';--
   end if;--

   --  :  6~9 (     )-
   set ls_s1 =substring(a_s1,1,5)||'XXXX'||substring(a_s1,10);--
   
   return ls_s1;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_month_add ( a_date VARCHAR(8)
      , a_mm int)
  RETURNS varchar(6)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar): get    -yy  -mm  -dd 
-- 2018-0313:  JH    initial
-- 2018-0705:  JH    return YYYYMM
------------------------------------------------------------------------
BEGIN
   DECLARE ls_rc_date varchar(8);--
   declare ls_date varchar(8);--
   declare ldt_date  timestamp;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;   --
   
   if a_date is null or trim(a_date)='' then
      set ls_date =to_char(sysdate,'yyyymmdd');--
--   else if trim(a_date)='' then
--      return '';--
   else   
      set ls_date = substr(a_date||'0101',1,8);       --
   end if;--

   set ldt_date =timestamp_format(ls_date,'yyyymmdd') + a_mm months;--
   
   -- select timestamp_format(ls_date,'yyyymmdd') + a_yy years + a_mm months + a_dd days
     -- into ldt_date
     -- from SYSIBM.SYSDUMMY1
   if length(a_date)=4 then
      return to_char(ldt_date,'yyyy');--
   end if;   --
   if length(a_date)=6 then
      return to_char(ldt_date,'yyyymm');--
   end if;   --
   return to_char(ldt_date,'yyyymm');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_month_between ( a_date1 VARCHAR(8)
      , a_date2 varchar(8) )
  RETURNS decimal(10,2)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar):      
-- V00.00   JH    2017-1025
------------------------------------------------------------------------
BEGIN
   DECLARE ls_date1 varchar(8);--
   declare ls_date2 varchar(8);--
   --declare ldt_date  timestamp;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return null; END;      --
   
   if a_date1 is null or trim(a_date1)='' then
      return null;--
   end if;--
   if a_date2 is null or trim(a_date2)='' then
      return null;--
   end if;--
   
   set ls_date1 = substr(trim(a_date1)||'0101',1,8);--
   set ls_date2 = substr(trim(a_date2)||'0101',1,8);--

   return months_between(to_date(ls_date1,'yyyymmdd'),to_date(ls_date2,'yyyymmdd'));--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_months_between ( a_date1 VARCHAR(8)
      , a_date2 varchar(8) )
  RETURNS decimal(10,2)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar):      
-- V00.00   JH    2017-1025
------------------------------------------------------------------------
BEGIN
   DECLARE ls_date1 varchar(8);--
   declare ls_date2 varchar(8);--
   --declare ldt_date  timestamp;--
   
   if a_date1 is null or trim(a_date1)='' then
      return null;--
   end if;--
   if a_date2 is null or trim(a_date2)='' then
      return null;--
   end if;--
   
   set ls_date1 = substr(trim(a_date1)||'0101',1,8);--
   set ls_date2 = substr(trim(a_date2)||'0101',1,8);--

   return months_between(to_date(ls_date1,'yyyymmdd'),to_date(ls_date2,'yyyymmdd'));--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_NVL (
    A_VAL1	VARCHAR(4000) , A_VAL2	VARCHAR(4000) )
  RETURNS VARCHAR(4000)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
BEGIN
   return decode(trim(nvl(a_val1,'')),'',a_val2,trim(a_val1));--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_payrate ( 
		as_str 		VARCHAR(2)
	)
  RETURNS int
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN

   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return 0; END;--
   
	if as_str = null or Rtrim(Ltrim(as_str))='' then
		return 0;--
	end if;--
   
   if as_str='00' then
      return 1;--
   elseif as_str='0E' then
      return 2;--
   elseif as_str='0A' then
      return 11;   --
   elseif as_str='0B' then
      return 12;   --
   elseif as_str='0C' then
      return 13;   --
   elseif as_str='0D' then
      return 14;      end if;--
   
	return 100+to_number(Rtrim(Ltrim(as_str)));	--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_rsk_ctrlseqno()
RETURNS varchar(10)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- 2017-0602   JHW   initial
------------------------------------------------------------------------
BEGIN
  --DECLARE ls_seqno varchar(10);--
--  DECLARE li_seqno int;--
--  set li_seqno = rsk_ctrlseqno.nextval;--
  
	RETURN varchar_format(sysdate,'yymm')||lpad(rsk_ctrlseqno.nextval,6,'0');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_secq1020 ( a_table VARCHAR(200), a_where varchar(200))
  RETURNS varchar(10)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 2019-1213   JH    initial
-- 2019-1226   JH    
------------------------------------------------------------------------
BEGIN
   DECLARE ll_cnt integer;--
   declare ls_stmt varchar(1000);--
  DECLARE cusr_1 CURSOR FOR v_stmt;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return 'E1'; END;--
--  DECLARE EXIT HANDLER FOR NOT FOUND
--  BEGIN
--    SIGNAL SQLSTATE '70001' 
--     SET MESSAGE_TEXT = 'Exit handler for not found fired';--
--  END;  --
      
   if nvl(a_table,'')='' or nvl(a_where,'')='' then
   	return 'E2';--
   end if;--
   
   if upper(a_table)='DUAL' then
      set ls_stmt ='select '||a_where||' from dual';--
   else    
      set ls_stmt ='select 1 from '||a_table||' '||a_where||' fetch first 1 rows only';--
   end if;--
   
  prepare v_stmt from ls_stmt;--
  OPEN cusr_1;--
  FETCH cusr_1 INTO ll_cnt;--
  close cusr_1;--
  if (ll_cnt = null) then
    return 'E3';--
  end if;--
  if ll_cnt>0 then
    return 'Y';--
  end if;  --
  return 'N';--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_spec_status ( a_spec VARCHAR(2)
      , a_del_date varchar(8) )
  RETURNS varchar(2)
------------------------------------------------------------------------
-- SQL UDF (Scalar):  S  
-- V00.00   JH    2019-0820
------------------------------------------------------------------------
BEGIN
--   declare ls_date2 varchar(8);--
   --declare ldt_date  timestamp;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return null; END;      --
   
   if trim(nvl(a_del_date,''))='' or a_del_date>=to_char(sysdate,'yyyymmdd') then
      return a_spec;--
   end if;--

   return '';--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_tw2date ( a_date VARCHAR(8) )
  RETURNS varchar(8)
------------------------------------------------------------------------
-- SQL UDF (Scalar):    2    
-- V00.00   JH    2017-1025
------------------------------------------------------------------------
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
BEGIN
   DECLARE ls_rc_date varchar(8);--
   declare ls_date varchar(8);--
   --declare ldt_date  timestamp;--
   
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return a_date; END;--
   
   if a_date is null or trim(a_date)='' then
      return '';--
   end if;--
   
   set ls_date =Rtrim(Ltrim(a_date));--
   
   if length(ls_date)=4 or length(ls_date)=5 then
      return to_char(to_number(ls_date||'01')+19110000);--
   end if;--
   if length(ls_date)=6 then
      return to_char(to_number(ls_date)+19110000);--
   end if;--
   if length(ls_date)=7 then
      if substr(ls_date,1,1) in ('1','0') then
         return to_char(to_number(ls_date)+19110000);--
      else
         return to_char(to_number(substr(ls_date,1,6))+19110000);--
      end if;--
   end if;--
   if length(ls_date)=8 then
      return ls_date;--
   end if;--
   return '';--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TW2DC ( 
		am_tw		decimal(13,2),
		am_dc	decimal(13,2),
      am_src_amt  decimal(13,2)
	)
  RETURNS decimal(13,2)
------------------------------------------------------------------------
-- SQL UDF (Scalar)
--V00.00 JH-107-1201: initial
------------------------------------------------------------------------
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
BEGIN 
   if nvl(am_dc,0)=0 or am_tw =am_dc then
      return am_src_amt;--
   end if;   --
	return round((am_src_amt * am_dc) / am_tw,2);--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_tx_sign ( 
		as_str 		VARCHAR(02)
	)
  RETURNS integer
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
BEGIN

   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return 0; END;--
   
	if as_str = null or Rtrim(Ltrim(as_str))='' then
		return 0;--
	end if;--
   if as_str in ('06','25','27','28','29') then
      return -1;--
   end if;--
   
	return 1;	--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_acct_pseqno ( a_key VARCHAR(16) )
  RETURNS VARCHAR(10)
LANGUAGE SQL
DETERMINISTIC
NO EXTERNAL ACTION
READS SQL DATA
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_pseqno varchar(10);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, not found BEGIN return ''; END;--

   if (a_key is null) then
      return '';--
   end if;--
   if length(a_key)<>10 and length(a_key)<>16 then
      return '';--
   end if;--
   
   select p_seqno into ls_pseqno
   from crd_card
   where acno_p_seqno = a_key
   or card_no =a_key
   fetch first 1 rows only
   ;--
   
   return ls_pseqno;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_acno_endbal ( 
      a_p_seqno VARCHAR(10)
  )    
  RETURNS decimal(12)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
--       
-- 2017-0921: jh->initial 
------------------------------------------------------------------------
BEGIN
   DECLARE lm_debt      decimal(12);--
   DECLARE lm_cash_use  decimal(12);--
   DECLARE lm_unbill_amt  decimal(12);--
   DECLARE lm_unbill_fee  decimal(12);--
   --DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return 0; END;--
      
   if (a_p_seqno is null or length(a_p_seqno)<10) then
      return 0;--
   end if;--
   
   set lm_debt =0;--
   set lm_cash_use =0;--
   set lm_unbill_amt =0;--
   set lm_unbill_fee =0;--

   --billed_amt+unbill_amt-
   SELECT sum(nvl(end_bal,0)) INTO lm_debt
   FROM act_debt
   WHERE p_seqno =a_p_seqno;--

   --          -
   SELECT sum(nvl(cash_use_balance,0)) INTO lm_cash_use
   FROM act_combo_m_jrnl
   WHERE p_seqno   =a_p_seqno;--

   --   billing-
   SELECT sum(
            nvl(a.unit_price,0)*(nvl(a.install_tot_term,0)-
            nvl(a.install_curr_term,0))+nvl(a.remd_amt,0)+
            decode(a.install_curr_term,0,nvl(a.first_remd_amt,0)+nvl(a.extra_fees,0),0)
            ) as db_unbill_amt,
          sum(
            nvl(a.clt_unit_price,0)*(nvl(a.clt_install_tot_term,0)-
            nvl(a.install_curr_term,0))+nvl(a.clt_remd_amt,0)
            ) as db_unbill_fee
   INTO   lm_unbill_amt, lm_unbill_fee
   FROM   bil_contract a 
   WHERE  a.p_seqno  = a_p_seqno
     AND  a.install_tot_term != a.install_curr_term
     AND  nvl(a.auth_code,'N') not in ('N','REJECT','P','reject');--

   return nvl(lm_debt,0)+nvl(lm_cash_use,0)+nvl(lm_unbill_amt,0)+nvl(lm_unbill_fee,0);--
END
;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_acno_key ( a_pseqno VARCHAR(20) )
  RETURNS VARCHAR(11)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_acct_key varchar(11);--
   declare sqlstate char(5) default '00000';--
   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' BEGIN return ''; END;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, not found BEGIN return ''; END;--

   if a_pseqno is null or length(a_pseqno)<>10 then
      return '';--
   end if;--
   -- if length(a_pseqno)<>10 then
      -- return '';--
   -- end if;--
   
   select acct_key into ls_acct_key
   from act_acno
   where acno_p_seqno = a_pseqno;--
   
   return ls_acct_key;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_acno_key2 ( a_kk1 VARCHAR(19),
   a_type varchar(02) )
  RETURNS VARCHAR(11)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC      
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 2019-0618:  JH    card_no
-- 2018-1019:    JH    parm2: debit_flag --> debit_flag/acct_type
------------------------------------------------------------------------
BEGIN
   DECLARE ls_acct_key varchar(11);--
   DECLARE li_debit int;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, not found BEGIN return ''; END;--
   
   if (a_kk1 is null or a_kk1='') then
      return '';--
   end if;--
   
   if length(a_kk1)<>10 and length(a_kk1)<>16 then
      return '';--
   end if;--
   set li_debit =0;--
   if (upper(a_type) ='Y' or a_type='90') then
      set li_debit =1;--
   end if;--
   
   if length(a_kk1)=16 then
      select acct_key into ls_acct_key from (
      select B.acct_key from crd_card A join act_acno B on B.acno_p_seqno=A.acno_p_seqno
      where A.card_no =a_kk1
      union
      select B.acct_key from dbc_card A join dba_acno B on B.p_seqno=A.p_seqno
      where A.card_no =a_kk1 );--
     return ls_acct_key; --
   end if;--
   
   if li_debit =1 then
      select acct_key into ls_acct_key
      from dba_acno
      where p_seqno = a_kk1;--
      return ls_acct_key;--
   end if;--

   select acct_key into ls_acct_key
   from act_acno
   where acno_p_seqno = a_kk1;--
   return ls_acct_key;--
END
 ;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_acno_key_idx ( a_ccas_indx decimal(10) )
  RETURNS VARCHAR(20)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- return X(2)+X(11): acct_type+acct_key
------------------------------------------------------------------------
BEGIN
   DECLARE ls_acct_key varchar(20);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, not found BEGIN return ''; END;--

   if (a_ccas_indx=0) then
      return '';--
   end if;--
   
   select acct_type||'-'||uf_acno_key2(acno_p_seqno,debit_flag) into ls_acct_key
   from cca_card_acct
   where card_acct_idx =a_ccas_indx;--
   
   return ls_acct_key;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_acno_name ( a_pseqno VARCHAR(10) )
  RETURNS vargraphic(50)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V00.0    JH    2017-08xx: initial
-- V00.1    JH    2018-0601: modify
-- V00.2    Alex  2018-1204: modify
------------------------------------------------------------------------
BEGIN
   DECLARE ls_id_name vargraphic(50);--
   DECLARE ls_corp_name vargraphic(50);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if a_pseqno is null or a_pseqno='' then
      return '';--
   end if;--
   if length(a_pseqno)<>10 then
      return '';--
   end if;--
   
   set ls_id_name ='';--
   set ls_corp_name ='';--

   select decode(A.acno_flag,'2',C.chi_name,B.chi_name)
   into ls_id_name
   from act_acno A left join crd_idno B on B.id_p_seqno=A.id_p_seqno
         left join crd_corp C on C.corp_p_seqno=A.corp_p_seqno
   where A.acno_p_seqno =a_pseqno;         --
   
   return ls_id_name;   --
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_acno_name2 ( a_pseqno VARCHAR(10), a_type
varchar(02) )
  RETURNS vargraphic(50)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V.2018-1203:   JH    initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_id_name vargraphic(50);--
   DECLARE ls_corp_name vargraphic(50);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if a_pseqno is null or a_pseqno='' then
      return '';--
   end if;--
   
   if length(a_pseqno)<>10 then
      return '';--
   end if;--
   
   set ls_id_name ='';--
   set ls_corp_name ='';--

   if upper(a_type)='Y' or a_type='90' then
      select B.chi_name, C.chi_name
      into ls_id_name, ls_corp_name
      from  dba_acno A left join dbc_idno B on B.id_p_seqno=A.id_p_seqno
            left join crd_corp C on C.corp_p_seqno =A.corp_p_seqno
      where A.p_seqno =a_pseqno;--
      if nvl(ls_id_name,'')='' and nvl(ls_corp_name,'')<>'' then
         return ls_corp_name;--
      end if;--
      return ls_id_name;--
   end if;--
   
   select decode(A.acno_flag,'2',C.chi_name,B.chi_name)
   into ls_id_name
   from act_acno A left join crd_idno B on B.id_p_seqno=A.id_p_seqno
         left join crd_corp C on C.corp_p_seqno=A.corp_p_seqno
   where A.acno_p_seqno =a_pseqno;--
   
   return ls_id_name;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_acno_name_idx ( a_ccas_indx decimal(10) )
  RETURNS vargraphic(50)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- return X(2)+X(11): chi_name
------------------------------------------------------------------------
BEGIN
   declare ls_acno_flag varchar(1);--
   DECLARE ls_id_name vargraphic(50);--
   DECLARE ls_corp_name vargraphic(50);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--

   if (a_ccas_indx=0) then
      return '';--
   end if;--
   
   set ls_id_name ='';--
   set ls_corp_name ='';--
   
   --
   select B.chi_name, C.chi_name, A.acno_flag 
   into ls_id_name, ls_corp_name, ls_acno_flag
   from cca_card_acct A left join crd_idno B on B.id_p_seqno=A.id_p_seqno
      left join crd_corp C on C.corp_p_seqno=A.corp_p_seqno
   where A.card_acct_idx =a_ccas_indx
     and A.debit_flag<>'Y';--
   if nvl(ls_acno_flag,'')='2' and nvl(ls_corp_name,'')<>'' then
      return ls_corp_name;--
   end if;--
   if nvl(ls_acno_flag,'') in ('1','3','Y') then
      return ls_id_name;--
   end if;--
   
   --debit-card-
   select B.chi_name, C.chi_name
   into ls_id_name, ls_corp_name
   from cca_card_acct A left join dbc_idno B on B.id_p_seqno=A.id_p_seqno
      left join crd_corp C on C.corp_p_seqno=A.corp_p_seqno
   where A.card_acct_idx =a_ccas_indx
     and A.debit_flag ='Y';--
   if nvl(ls_corp_name,'')>'' then
      return ls_corp_name;--
   end if;--
   return nvl(ls_id_name,'');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_acno_pseqno ( 
      a_kk1 varchar(19), a_kk2 VARCHAR(19)
  )    
  RETURNS VARCHAR(20)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 106-0609: jh->initial
-- 108-0614: JH  modify 
------------------------------------------------------------------------
BEGIN
   DECLARE ls_p_seqno varchar(10);--
   DECLARE ls_acct_type varchar(20);--
   DECLARE ls_acct_key varchar(20);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, not found BEGIN return ''; END;--
   
   set ls_acct_type =a_kk1;--
   set ls_acct_key =a_kk2;--
   if length(a_kk1) >2 and length(a_kk2) <=2 then
      set ls_acct_type =a_kk2;--
      set ls_acct_key =a_kk1;--
   end if;--
   
   if length(ls_acct_key)>=15 then
     select kk into ls_p_seqno from (
     select acno_p_seqno as kk from crd_card where card_no =ls_acct_key
     union select p_seqno as kk from dbc_card where card_no =ls_acct_key );--
     return nvl(ls_p_seqno,'');--
   end if;--
  
   if ls_acct_type <>'90' and upper(ls_acct_type)<>'Y' then
      select acno_p_seqno into ls_p_seqno
      from act_acno
      where acct_key = rpad(ls_acct_key,11,'0')
        and acct_type = decode(ls_acct_type,'','01',ls_acct_type);--
      return nvl(ls_p_seqno,'');--
   end if;--
--   
    select p_seqno into ls_p_seqno
    from dba_acno
    where acct_key = rpad(ls_acct_key,11,'0')
      and acct_type = decode(ls_acct_type,'','90',ls_acct_type);--
   return nvl(ls_p_seqno,'');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_apr_cmsp2010 ()
  RETURNS integer
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 2019-1226  JH    initial
-- 2020-0103   JH    modify
------------------------------------------------------------------------
BEGIN
   DECLARE ll_cnt integer;--
   declare ls_stmt varchar(1000);--
   DECLARE cusr_1 CURSOR FOR v_stmt;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return 'E1'; END;--
   
   select 1 into ll_cnt from crd_idno_online
   where apr_YN='Y' and  apr_flag<>'Y' and data_image='2' fetch first 1 rows only;--
   if ll_cnt>0 then
      return 1;--
   end if;--
   select 1 into ll_cnt from act_acno_online
   where apr_yn ='Y' and apr_flag <>'Y' and data_image ='2' fetch first 1 row only;--
   if ll_cnt>0 then
      return 1;--
   end if;--
   select 1 into ll_cnt from crd_card_online
   where apr_yn ='Y' and apr_flag <>'Y' and data_image ='2' fetch first 1 row only;--
   if ll_cnt>0 then
      return 1;--
   end if;--

   --debit-card-------------   
   select 1 into ll_cnt from dbs_modlog_main 
   where mod_table ='DBC_IDNO' and apr_flag<>'Y' fetch first 1 rows only;--
   if ll_cnt>0 then
      return 1;--
   end if;--
   select 1 into ll_cnt from dbs_modlog_main
   where mod_table ='DBA_ACNO' and apr_flag<>'Y' fetch first 1 rows only;--
   if ll_cnt>0 then
      return 1;--
   end if;--
   select 1 into ll_cnt from dbs_modlog_main
   where mod_table ='DBC_CARD' and apr_flag <>'Y' fetch first 1 rows only;--
   if ll_cnt>0 then
      return 1;--
   end if;--

   return 0;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_bin_type ( a_kk1 VARCHAR(19) )
  RETURNS varchar(1)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- 2018-0731: JH  modify
-- 2018-0627: JH  modify
-- 2017-1207: JH  initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_bin_type varchar(1) default '';--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
   
   if a_kk1 is null or a_kk1='' or length(a_kk1)<6 then
      return '';--
   end if;   --

   select bin_type into ls_bin_type
     from  ptr_bintable
   where 1=1 
     and rpad(a_kk1,16,'0') between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9')
   fetch first 1 row only
   ;--

   RETURN nvl(ls_bin_type,'');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_card_indicator ( a_key VARCHAR(02) )
  RETURNS varchar(1)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
--       : 1,0
-- V00.00   JH    2017-10xx: initial
------------------------------------------------------------------------
BEGIN 
  declare ls_rc varchar(1); --
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
  
--  select card_indicator into ls_rc
--            from ptr_acct_type
--            where acct_type =a_key;--
--  return ls_rc;       --
  select card_indicator into ls_rc
    from ptr_acct_type where acct_type =a_key;--
  return ls_rc;  --
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_card_name ( a_card_no VARCHAR(16) )
  RETURNS vargraphic(40)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
BEGIN
   DECLARE ls_id_name vargraphic(20);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if a_card_no is null or a_card_no='' then
      return '';--
   end if;--
   if length(a_card_no)<15 then
      return '';--
   end if;--
   
   set ls_id_name ='';--

   select B.chi_name
   into ls_id_name
   from crd_card A, crd_idno B
   where A.id_p_seqno =B.id_p_seqno
     and A.card_no =a_card_no;--
   if Length(ls_id_name)>0 then
      return ls_id_name;--
   end if;--
   
   select B.chi_name
   into ls_id_name
   from dbc_card A, dbc_idno B
   where A.id_p_seqno =B.id_p_seqno
     and A.card_no =a_card_no;--
   if Length(ls_id_name)>0 then
      return ls_id_name;--
   end if;--

   return '';--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_cca_mcht_name ( A_mcht_no VARCHAR(15) )
  RETURNS vargraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V.00.0   2017-0628   JHW  :initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
   if (A_mcht_no is null or trim(a_mcht_no)='') then
      return '';--
   end if;--
   
   set ls_desc ='';--
   
   select mcht_name into ls_desc
   from cca_mcht_bill
   where mcht_no = trim(A_mcht_no);--

	RETURN ls_desc;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_CHI_NAME ( A_KEY VARCHAR(16) )
  RETURNS VARCHAR(40)
  LANGUAGE SQL
  READS SQL DATA
  NO EXTERNAL ACTION
  DETERMINISTIC
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V.00.0   2017-1228   Phopho  :initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_cname varchar(40);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
   if a_key is null or a_key='' then
      return '';--
   end if;--

  --ID--
   if length(a_key)=10 then
      select chi_name into ls_cname
        from crd_idno
       where id_no = a_key fetch first 1 row only;--
      return ls_cname;--
   end if;--
   
   --   q W  --
   if length(a_key)=8 then
      select chi_name into ls_cname
        from crd_corp
       where corp_no = a_key fetch first 1 row only;--
      return ls_cname;--
   end if;--
   
   -- d  --
   select b.chi_name into ls_cname
     from crd_idno b, crd_card c
    where b.id_p_seqno = c.id_p_seqno
      and c.card_no = a_key
      fetch first 1 row only;--
   return ls_cname;--

   RETURN '';--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_corp_name ( a_key VARCHAR(10) )
  RETURNS VARGRAPHIC(40)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- get     
------------------------------------------------------------------------
BEGIN
   DECLARE ls_name vargraphic(40);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if a_key is null then
      return '';--
   end if;--
   if length(a_key)<>10 and length(a_key)<>8 then
      return '';--
   end if;--
   
   --corp_p_seqno--
   if length(a_key)=10 then
      select nvl(chi_name,'') into ls_name
      from crd_corp
      where corp_p_seqno = a_key;--
      if ls_name <> '' then
         return ls_name;--
      end if;--
      --dbc_corp--
--      select nvl(chi_name,'') into ls_name
--      from dbc_corp
--      where corp_p_seqno = a_key;--
--      if ls_name <> '' then
--         return ls_name;--
--      end if;--
   end if;--
  --corp_no---
   if length(a_key)=8 then
      select nvl(chi_name,'') into ls_name
      from crd_corp
      where corp_no = a_key
        fetch first 1 rows only;--
      if ls_name <> '' then
         return ls_name;--
      end if;--
      --dbc_corp--
--     select nvl(chi_name,'') into ls_name
--     from dbc_corp
--     where corp_no = a_key
--       and fetch first 1 rows only;--
--     if ls_name <> '' then
--        return ls_name;--
--     end if;--
   end if;--
	
   RETURN '';--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_corp_no ( a_corp_pseqno VARCHAR(10) )
  RETURNS VARCHAR(8)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_corp_no varchar(8);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if a_corp_pseqno is null or trim(a_corp_pseqno)='' then
      return '';--
   end if;--
   
   select corp_no into ls_corp_no
   from crd_corp
   where corp_p_seqno = a_corp_pseqno;--
   
	RETURN ls_corp_no;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_ctfc_idname ( a_key VARCHAR(19) )
  RETURNS vargraphic(60)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 2017-1207: JH  initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_name vargraphic(60);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
   if a_key is null or a_key='' then
      return '';--
   end if;   --

   select A.chi_name||decode(B.sup_flag,'1','(  )','') into ls_name
   from crd_idno A, crd_card B
   where A.id_p_seqno =B.id_p_seqno
     and B.card_no = a_key;--
   if ls_name is null or length(ls_name)=0 then
      --dbc_card--
      select A.chi_name||decode(B.sup_flag,'1','(  )','') into ls_name
      from dbc_idno A, dbc_card B
      where A.id_p_seqno =B.id_p_seqno
        and B.card_no = a_key;--
   end if;--
	
   RETURN nvl(ls_name,'');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_curr_name ( A_code VARCHAR(3) )
  RETURNS vargraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V.00.0   2018-0613   JHW  :initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
   if (A_code is null or trim(A_code)='') then
      return '';--
   end if;--
   
   set ls_desc ='';--
   
   select curr_chi_name into ls_desc
   from ptr_currcode
   where curr_code = trim(A_code);--

	RETURN ls_desc;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_debit_flag ( a_card_no VARCHAR(16) )
  RETURNS VARCHAR(01)
LANGUAGE SQL
DETERMINISTIC
NO EXTERNAL ACTION
READS SQL DATA
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_flag varchar(01);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, not found BEGIN return 'N'; END;--

   if (a_card_no is null) then
      return 'N';--
   end if;--
   if length(a_card_no)<>16 then
      return 'N';--
   end if;--
   
   select debit_flag into ls_flag
   from ptr_bintable
   where a_card_no between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9') 
   fetch first 1 rows only
   ;--
   
   return decode(ls_flag,'Y','Y','N');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_idno_ename ( A_pseqno VARCHAR(10) )
  RETURNS VARCHAR(25)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc varchar(25);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_desc ='';--
   
     	select eng_name into ls_desc
     	from 	crd_card
      where id_p_seqno = A_pseqno
      order by current_code
      fetch first 1 row only;--
   
	RETURN nvl(ls_desc,'');--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_IDNO_id ( a_key VARCHAR(16) )
  RETURNS VARCHAR(10)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
--    ID:
-- V00.0    JH    2017-xxxx: initial
-- V00.1    JH    2017-0921: key: card_no,id_p_seqno
-- V00.2    JH    2017-0921: key: card_no,id_p_seqno, crd_card/dbc_card
-- V00.3    JH    2018-0611: only    
------------------------------------------------------------------------
BEGIN
   DECLARE ls_idno varchar(10);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
  --ID_P_SEQNO---
   if length(a_key)=10 then
      select B.id_no into ls_idno
      from crd_idno B
      where B.id_p_seqno = a_key;--
      if ls_idno <> '' then
         return ls_idno;--
      end if;--
   end if;--
   
   --  --
   if length(a_key)>10 and length(a_key)<=16 then
      select B.id_no into ls_idno
      from crd_card A join crd_idno B on A.id_p_seqno=B.id_p_seqno
      where A.card_no = a_key;--
      if ls_idno <> '' then
         return ls_idno;--
      end if;--
   end if;--
	
   RETURN '';--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_IDNO_id2 ( a_key VARCHAR(16), a_type varchar(02)
)
  RETURNS VARCHAR(10)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
--    ID:
-- V00.0    JH    2017-12xx: initial
-- V.2018-1005:   JH    modify: card_no
-- V.2018-1019:   JH    debit/acct_type
-- V.2018-1024:   JH    crd_card union dbc_card
------------------------------------------------------------------------
BEGIN 
   DECLARE ls_idno varchar(10);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--

  --ID_P_SEQNO---
   if length(a_key)=10 then
      if upper(trim(a_type))='Y' or a_type='90' then
        --dbc_card--
        select nvl(B.id_no,'') into ls_idno
        from dbc_idno B
        where B.id_p_seqno = a_key;--
      else
        select nvl(B.id_no,'') into ls_idno
        from crd_idno B
        where B.id_p_seqno = a_key;--
      end if;--
      return nvl(ls_idno,'');--
   end if;   --
   
   --  --
   if length(a_key)>10 then
      select id_no into ls_idno from (
         select id_no from crd_idno
         where id_p_seqno in (select id_p_seqno from crd_card where card_no =a_key) 
         union
         select nvl(id_no,'') from dbc_idno
         where id_p_seqno in (select id_p_seqno from dbc_card where card_no = a_key)
      ) fetch first 1 rows only ;--
   end if;--
	
   RETURN nvl(ls_idno,'');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_idno_name ( a_key VARCHAR(16) )
  RETURNS vargraphic(50)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_name vargraphic(50);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
  --ID_P_SEQNO---
   if length(a_key)=10 then
      select nvl(B.chi_name,'') into ls_name
      from crd_idno B
      where B.id_p_seqno = a_key;--
      if ls_name <> '' then
         return ls_name;--
      end if;--
      --dbc_card--
      select nvl(B.chi_name,'') into ls_name
      from dbc_idno B
      where B.id_p_seqno = a_key;--
      if ls_name <> '' then
         return ls_name;--
      end if;--
   end if;--
   
   --  --
   if length(a_key)>10 and length(a_key)<=16 then
      select nvl(B.chi_name,'') into ls_name
      from crd_card A join crd_idno B on A.id_p_seqno=B.id_p_seqno
      where A.card_no = a_key;--
      if ls_name <> '' then
         return ls_name;--
      end if;--
      --dbc_card--
      select nvl(B.chi_name,'') into ls_name
      from dbc_card A join dbc_idno B on A.id_p_seqno=B.id_p_seqno
      where A.card_no = a_key;--
      if ls_name <> '' then
         return ls_name;--
      end if;--
   end if;--
	
   RETURN '';--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_idno_name2 ( a_key VARCHAR(16), a_type varchar(02)
)
  RETURNS vargraphic(50)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V.2018-1019:   JH    debit/acct_type
------------------------------------------------------------------------
BEGIN
   DECLARE ls_name vargraphic(50);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--

   if length(a_key) <10 then
      return '';--
   end if;--
   
  --ID_P_SEQNO---
   if length(a_key)=10 then
      if upper(a_type)='Y' or a_type='90' then
        --dbc_card--
        select nvl(B.chi_name,'') into ls_name
        from dbc_idno B
        where B.id_p_seqno = a_key;--
      else
        select nvl(B.chi_name,'') into ls_name
        from crd_idno B
        where B.id_p_seqno = a_key;--
      end if;--
      return nvl(ls_name,'');--
   end if;   --
   
   --card_no--
   select chi_name into ls_name from (
    select chi_name
    from crd_idno B join crd_card A on B.id_p_seqno=A.id_p_seqno
    where A.card_no =a_key
    union 
    select chi_name
    from dbc_idno D join dbc_card C on D.id_p_seqno=C.id_p_seqno
    where C.card_no =a_key
   );--
	
   RETURN nvl(ls_name,'');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_IDNO_pseqno ( a_idno VARCHAR(10) )
  RETURNS VARCHAR(10)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
  DECLARE ls_p_seqno varchar(10);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, not found BEGIN return ''; END;--
     
  set ls_p_seqno ='';--
  
  select id_p_seqno into ls_p_seqno
   from crd_idno
   where id_no = a_idno
   fetch first 1 rows only
   ;--

   return nvl(ls_p_seqno,'');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_mcht_bill ( A_mcht_no VARCHAR(15), A_bank_id
varchar(11) )
  RETURNS vargraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V.2018-1029   JHW  :initial
-- V.2018-1127   JH   :modify
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if (A_mcht_no is null or trim(a_mcht_no)='') then
      return '';--
   end if;--
   
   set ls_desc ='';--
   
   select replace(mcht_name,' @','') into ls_desc
   from  cca_mcht_bill
   where mcht_no = trim(A_mcht_no)
     and acq_bank_id =decode(trim(A_bank_id),'',acq_bank_id,trim(A_bank_id))
   fetch first 1 row only  
   ;--
   if (ls_desc <>'') then 
      RETURN ls_desc;--
   end if;--

   select replace(mcht_name,'','') into ls_desc
   from cca_mcht_bill
   where mcht_no =trim(A_mcht_no)
     and acq_bank_id =decode(trim(A_bank_id),'',acq_bank_id,trim(A_bank_id))
   fetch first 1 row only  
   ;--
   
   return ls_desc;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_mcht_name_bil ( A_mcht_no VARCHAR(15) )
  RETURNS vargraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V.00.0   2017-1002   JHW  :initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if (A_mcht_no is null or trim(a_mcht_no)='') then
      return '';--
   end if;--
   
   set ls_desc ='';--
   
   select mcht_chi_name into ls_desc
   from  bil_merchant
   where mcht_no = trim(A_mcht_no);--

	RETURN ls_desc;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_opp_status ( A_code VARCHAR(2) )
  RETURNS vargraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- select opp_remark from cca_opp_type_reason where opp_status = 'A2'
-- V.00.0   2018-0613   JHW  :initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
   if (A_code is null or trim(A_code)='') then
      return '';--
   end if;--
   
   set ls_desc ='';--
   
   select opp_remark into ls_desc
   from cca_opp_type_reason
   where opp_status = trim(A_code);--

	RETURN ls_desc;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_refno_ori ( a_contr_no VARCHAR(10), a_ref_no
varchar(10) )
  RETURNS VARCHAR(10)
------------------------------------------------------------------------
--   l b      X:
-- V00.0    JH    2019-0319: initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_refno_ori varchar(10);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return a_ref_no; END;--
   
   if nvl(a_ref_no,'')<>'' or nvl(a_contr_no,'')='' then
      return nvl(a_ref_no,'');--
   end if;--
   
   --ID_P_SEQNO---
   select reference_no into ls_refno_ori
   from bil_contract
   where contract_no = a_contr_no;--
	
   RETURN nvl(ls_refno_ori,'');--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE Function uf_rsk_stage_days
   (  a_bin_type        varchar(1),
      a_txn_code        varchar(2),    
--      a_acq_type       varchar2, 'NC'
      a_day_type       varchar(1),
      a_base_date      varchar(8)
   )
   RETURNS VARCHAR(8)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC     
--*******************************************************************
--       
--a_trans_type: txn_code='05', =1 else =2
--a_day_type: R.  , 1,2,3,4,5
--V01.00    JHW   106-0611: initial
--*******************************************************************
   --ls_tx_type        varchar(1);--
BEGIN

   DECLARE ldt_rc_date        date;--
   DECLARE ldt_base_date      date;--
   DECLARE ls_base_date       varchar(8);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
   if a_bin_type='' or a_txn_code='' or a_day_type='' then
      return '';--
   end if;--
   
   if a_day_type not in ('R','1','2','3','4','5') then
      return '';--
   end if;   --
 
   set ldt_base_date =decode(trim(a_base_date),'',current date,to_date(trim(a_base_date),'yyyymmdd'));--
   
   set ldt_rc_date = (select   case a_day_type 
            when 'R' then ldt_base_date + return_day day 
            when '1' then ldt_base_date + fst_cb_day day 
            when '2' then ldt_base_date + represent_day day 
            when '3' then ldt_base_date + sec_cb_day day 
            when '4' then ldt_base_date + pre_arbit_day day 
            when '5' then ldt_base_date + pre_comp_day day 
            else null end  
     from ptr_rskinterval
    where bin_type = a_bin_type 
      and trans_type = decode(a_txn_code,'05','1','2')
      and acq_type = 'NC'
      );--
      
   if ldt_rc_date is null then
      return '';--
   end if;--
   
   return to_char(ldt_rc_date,'yyyymmdd');--
   
end

;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_same_dept ( a_mod_user VARCHAR(10), a_apr_user
varchar(10) )
  RETURNS integer
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE li_cnt integer;--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return -1; END;--
      
   if (nvl(a_mod_user,'')='' or nvl(a_apr_user,'')='') then
   	return 0;--
   end if;	--
   
   select count(*) into li_cnt 
   from sec_user A , sec_user B 
   where 1=1 
   and decode(A.bank_unitno,'Z09','109',A.bank_unitno)=decode(B.bank_unitno,'Z09','109',B.bank_unitno)  
     and A.usr_id=a_mod_user and B.usr_id=a_apr_user;--
   
	return nvl(li_cnt,0);--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_acct_code ( A_kk1 VARCHAR(2) )
  RETURNS VARgraphic(20)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_name vargraphic(40);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--

   set ls_name ='';--
   if a_kk1>'' then
     select chi_short_name into ls_name
       from ptr_actcode
      where acct_code = A_kk1;--
   end if;--
   
   return ls_name;   --

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_acct_type ( A_code VARCHAR(2) )
  RETURNS vargraphic(60)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(60);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_desc ='';--
   
   select chin_name into ls_desc
   from ptr_acct_type
   where acct_type =a_code;--

	RETURN ls_desc;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_card_type ( A_kk1 VARCHAR(2) )
  RETURNS VARgraphic(20)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_name vargraphic(40);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--

   set ls_name ='';--
   
     select name into ls_name
       from ptr_card_type
      where card_type = A_kk1;--

   return ls_name;   --

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_ccas_parm3 ( A_id VARCHAR(10), A_key varchar(10)
)
  RETURNS VARgraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   
   set ls_desc ='';--
   
   select sys_data1 into ls_desc
   from cca_sys_parm3
   where sys_id = A_id
     and sys_key = A_key;--

	RETURN ls_desc;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_cms_case_id ( A_type VARCHAR(1), A_id varchar(5)
)
  RETURNS vargraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   
   if (a_type='' or a_id='') then
      return '';--
   end if;--
   
   set ls_desc ='';--
   
   select case_desc into ls_desc
   from  cms_casetype
   where case_type = A_type
     and case_id = A_id;--

	RETURN nvl(ls_desc,'');--
--EXCEPTION
--  when others then
--   return '';--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_group_code ( A_key VARCHAR(4) )
  RETURNS VARgraphic(40)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
--     :
-- 2017-1213: initial
------------------------------------------------------------------------
BEGIN 
   DECLARE ls_name vargraphic(40);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--

   
   select group_name into ls_name
   from ptr_group_code
   where group_code = decode(nvl(a_key,''),'','0000',a_key);--
   
   return ls_name;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_idtab ( A_type VARCHAR(20), A_id varchar(20)
)
  RETURNS VARgraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 2018-1002.jh      where like
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_desc ='';--
   
   select wf_desc into ls_desc
   from ptr_sys_idtab
   where wf_type like A_type
     and wf_id = A_id;--

	RETURN ls_desc;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_idtab_jcic ( A_type VARCHAR(20), A_id varchar(20)
)
  RETURNS VARGRAPHIC(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- V.2018-0703
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   set ls_desc ='';--
   
   if trim(A_id)='' then
      return ls_desc;--
   end if;   --
   
   select id_desc into ls_desc
   from col_jcic_idtab
   where id_key = A_type
     and id_value = A_id;--

	RETURN ls_desc;--

END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_tt_mcc_code ( a_mcc_code VARCHAR(4) )
  RETURNS vargraphic(60)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(60);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if (nvl(a_mcc_code,'')='') then
   	return '';--
   end if;	--
   
   select mcc_remark into ls_desc
            from cca_mcc_risk
            where MCC_CODE = a_MCC_CODE;--
   
	return ls_desc;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_mcht_base ( 
   a_mcht_no varchar(15),
   A_acq_bank VARCHAR(11) )
  RETURNS VARgraphic(50)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V00.0    JH    2017-08xx
------------------------------------------------------------------------
BEGIN
   DECLARE ls_name vargraphic(50);--
   
   set ls_name ='';--

   select mcht_name into ls_name
   from 	cca_mcht_bill
   where mcht_no = A_mcht_no
     and acq_bank_id = A_acq_bank
   order by mcht_no, acq_bank_id
   fetch first 1 row only;--
   
	RETURN nvl(ls_name,'');   --

END
;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_resp_code ( A_resp_code VARCHAR(10) )
  RETURNS VARGRAPHIC(60)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN

   DECLARE ls_desc vargraphic(60);--
   
   select resp_remark into ls_desc
   from cca_resp_code
   where resp_code = a_resp_code;--
   if ls_desc is null then
      return '';--
   end if;--
   
   return ls_desc;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_RISK_TYPE ( A_RISK_TYPE VARCHAR(2) )
  RETURNS VARgraphic(100)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC    
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- 2018-1203:  JH    initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_desc vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
   if trim(a_risk_type)='' then
      return '';--
   end if;--
   
   set ls_desc ='';--
   
   select sys_data1 into ls_desc
   FROM cca_sys_parm1
   WHERE sys_id ='RISK'
     and sys_key  = a_risk_type;--

	RETURN ls_desc;--
   
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_TT_spec_code ( A_code VARCHAR(2) )
  RETURNS VARGRAPHIC(100)
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
BEGIN

   DECLARE ls_desc vargraphic(100);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
   select spec_desc into ls_desc
   from cca_spec_code
   where spec_code = a_code;--
   if ls_desc is null then
      return '';--
   end if;--
   
   return ls_desc;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_user_name ( A_user_id VARCHAR(10) )
  RETURNS vargraphic(20)
------------------------------------------------------------------------
--      
-- V00.0    JH    2017-08xx
------------------------------------------------------------------------
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
BEGIN
   DECLARE ls_name vargraphic(20);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if (a_user_id is null or a_user_id='') then
      return '';--
   end if;--
   
   set ls_name ='';--
   
   select usr_cname into ls_name
   from  sec_user
   where usr_id = A_user_id;--

	RETURN decode(nvl(ls_name,''),'',a_user_id,ls_name);--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_VD_acno_key ( a_pseqno VARCHAR(10) )
  RETURNS VARCHAR(11)
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC
BEGIN 
   DECLARE ls_acct_key varchar(11);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if (a_pseqno is null) then
      return '';--
   end if;--
   if length(a_pseqno)<>10 then
      return '';--
   end if;--
   
   select acct_key into ls_acct_key
   from dba_acno
   where p_seqno = a_pseqno;--
   
	return ls_acct_key;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION UF_VD_idno_id ( a_id_pseqno VARCHAR(10) )
  RETURNS VARCHAR(10)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V00.0    JH    2017-08xx: initial
------------------------------------------------------------------------
BEGIN
   DECLARE ls_idno varchar(10);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   select id_no into ls_idno
   from dbc_idno
   where id_p_seqno = a_id_pseqno;--
   
	return ls_idno;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_VD_idno_name ( a_key VARCHAR(16) )
  RETURNS vargraphic(50)
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_name vargraphic(50);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
   
  --ID_P_SEQNO---
   if length(a_key)=10 then
      --dbc_IDNO--
      select nvl(B.chi_name,'') into ls_name
      from dbc_idno B
      where B.id_p_seqno = a_key;--
      if ls_name <> '' then
         return ls_name;--
      end if;--
   end if;--
   
   -- d  --
   if length(a_key)>10 and length(a_key)<=16 then
      --dbc_card--
      select nvl(B.chi_name,'') into ls_name
      from dbc_card A join dbc_idno B on A.id_p_seqno=B.id_p_seqno
      where A.card_no = a_key;--
      if ls_name <> '' then
         return ls_name;--
      end if;--
   end if;--
	
   RETURN '';--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_VD_acno_name ( a_pseqno VARCHAR(10) )
  RETURNS vargraphic(50)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
------------------------------------------------------------------------
BEGIN
   DECLARE ls_id_name vargraphic(50);--
   DECLARE ls_corp_name vargraphic(50);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
      
   if a_pseqno is null or a_pseqno='' then
      return '';--
   end if;--
   if length(a_pseqno)<>10 then
      return '';--
   end if;--
   
   set ls_id_name ='';--
   set ls_corp_name ='';--
   
   select uf_vd_idno_name(id_p_seqno)
     into ls_id_name
   from dba_acno
   where p_seqno =a_pseqno;--
   
   return ls_id_name;--
END;


SET SYSIBM.NLS_STRING_UNITS = 'SYSTEM';
SET CURRENT SCHEMA = "ECSCRDB ";
SET CURRENT PATH = "ECSCRDB";

CREATE FUNCTION uf_VD_IDNO_pseqno ( a_idno VARCHAR(10) )
  RETURNS VARCHAR(10)
LANGUAGE SQL
READS SQL DATA
NO EXTERNAL ACTION
DETERMINISTIC  
------------------------------------------------------------------------
-- SQL UDF (Scalar)
-- V00.0    JH    2017-08xx: initial
------------------------------------------------------------------------
BEGIN
  DECLARE ls_p_seqno varchar(10);--
   DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING BEGIN return ''; END;--
     
  set ls_p_seqno ='';--
  
  select id_p_seqno into ls_p_seqno
   from dbc_idno
   where id_no = a_idno
   fetch first 1 rows only
   ;--

   return ls_p_seqno;--
END;


SET CURRENT SCHEMA = "ECSCRDB ";

GRANT EXECUTE ON FUNCTION UF_2num               TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_CHI_NAME           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_DC2TW_AMT          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_DC_AMT             TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_DC_AMT2            TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_DC_CURR            TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_IDNO_id            TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_IDNO_id2           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_IDNO_pseqno        TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_NVL                TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_RISK_TYPE       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_acct_code       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_acct_type       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_card_type       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_ccas_parm3      TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_cms_case_id     TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_group_code      TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_idtab           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_idtab_jcic      TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_mcht_base       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_resp_code       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TT_spec_code       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_TW2DC              TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_VD_acno_key        TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_VD_idno_id         TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_acct_pseqno        TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_acno_endbal        TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_acno_key           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_acno_key2          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_acno_key_idx       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_acno_name_idx      TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_acno_pseqno        TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_cca_mcht_name      TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_ccar0270           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_corp_no            TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_curr_name          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_curr_sort          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_date2fmt           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_debit_flag         TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_hi_acctno          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_hi_addr            TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_hi_cardno          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_hi_cname           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_hi_idno            TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_idno_ename         TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_mcht_bill          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_mcht_name_bil      TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_opp_status         TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_payrate            TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_refno_ori          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION UF_tx_sign            TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_2ymd               TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_VD_IDNO_pseqno     TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_VD_acno_name       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_VD_idno_name       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_acno_name          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_acno_name2         TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_apr_cmsp2010       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_auth_seqno         TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_bin_type           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_card_indicator     TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_card_name          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_case_seqno         TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_cmsr4240_fun       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_corp_name          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_corp_p_seqno       TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_ctfc_idname        TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_ctfc_seqno         TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_date_add           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_date_diff          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_ecs_stop           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_hi_email           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_hi_passport        TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_hi_telno           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_idno_name          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_idno_name2         TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_month_add          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_month_between      TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_months_between     TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_rsk_ctrlseqno      TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_same_dept          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_secq1020           TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_spec_status        TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_tt_mcc_code        TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_tw2date            TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_user_name          TO USER crap1, USER dcdbmod ;
GRANT EXECUTE ON FUNCTION uf_rsk_stage_days     TO USER crap1, USER dcdbmod ;

COMMIT WORK;

CONNECT RESET;

TERMINATE;

package rskm03;
/**
 * 2020-0916   JH    sms_msg_dtl.apr_flag=Y
 * 2020-0114:  Alex  sms audtype = 'A'
 * 2019-1219:  Alex  add dataCheck
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import busi.func.SmsMsgDetl;
import busi.FuncEdit;
import taroko.base.Parm2Sql;
import taroko.com.TarokoCommon;


public class Rskm3110Func extends FuncEdit {
taroko.base.CommDate zzdate = new taroko.base.CommDate();
String kk1 = "", kk3 = "";
int kk2 = 0;
String isWarnDate = "", isWarnTime = "", isWarnUser = "";
String isRelsDate = "", isRelsTime = "", isRelsUser = "";
String isSpecRemark = "", isProcRemark = "";
String isTransDate = "", isSecndDate = "";
String orgControlTabName = "sms_msg_dtl";
String controlTabName = "sms_msg_dtl_t";

public Rskm3110Func(TarokoCommon wr) {
   wp = wr;
   this.conn = wp.getConn();
   modUser =wp.loginUser;
   modPgm =wp.modPgm();
}

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}


@Override
public void dataCheck() {
   if (this.ibAdd) {
      kk1 = wp.itemStr("kk_card_no");
      kk2 = selectCtfgSeqno();
   }
   else {
      kk1 = wp.itemStr("card_no");
      kk2 = (int) wp.itemNum("ctfg_seqno");
   }
   if (empty(kk1)) {
      errmsg("卡號：不可空白");
      return;
   }
   if (kk2 == 0) {
      errmsg("管制序號：不可空白");
      return;
   }

//--  因舊系統table無此欄位導致新系統table中的id_p_seqno大多為空值故暫時先拿掉此檢核 待id_p_seqno補齊後再打開此檢核		
//		if(wp.itemEmpty("id_p_seqno")){
//			errmsg("此卡號非本行卡");
//			return ;
//		}

   if (ibAdd || ibUpdate) {
      isSpecRemark = wp.itemStr("spec_remark_1") + wp.itemStr("spec_remark_2") + wp.itemStr("spec_remark_3")
            + wp.itemStr("spec_remark_4") + wp.itemStr("spec_remark_5");
      isProcRemark = wp.itemStr("proc_remark_1") + wp.itemStr("proc_remark_2") + wp.itemStr("proc_remark_3")
            + wp.itemStr("proc_remark_4") + wp.itemStr("proc_remark_5");
      if (checkCard(kk1) == false) {
         errmsg("非本行卡號");
         return;
      }
   }

   if (isAdd()) {
      if (checkMast(kk1) == false) {
         errmsg("此卡號有管制記錄未解控, 不可新增");
         return;
      }
   }

   if (isAdd() || isUpdate()) {
      if (wp.itemEmpty("find_type")) {
         errmsg("發現來源不可空白");
         return;
      }

      if (eqIgno(wp.itemNvl("secnd_flag", "0"), "1")) {
         isTransDate = wp.itemStr("trans_date");

         if (wp.itemEmpty("secnd_date")) isSecndDate = zzdate.sysDate();
         else isSecndDate = wp.itemStr("secnd_date");
         //--2019/10/3 取消
//				if(wp.itemEmpty("trans_date"))	isTransDate = zzdate.sysDate();
//				else	isTransDate = wp.itemStr("trans_date");

         //--2019/05/22取消此限制
//				if(wp.itemEmpty("trans_unit")){
//					errmsg("移交單位 不可空白");
//					return ;
//				}
      }

   }

   if (wp.itemEmpty("warn_date")) {
      isWarnDate = zzdate.sysDate();
   }
   else {
      isWarnDate = wp.itemStr("warn_date");
   }

   if (wp.itemEmpty("warn_time")) {
      isWarnTime = zzdate.sysTime();
   }
   else {
      isWarnTime = wp.itemStr("warn_time");
   }

   if (wp.itemEmpty("warn_user")) {
      isWarnUser = selectUser();
   }
   else {
      isWarnUser = wp.itemStr("warn_user");
   }

   if (wp.itemEmpty("rels_code")) {
      isRelsDate = "";
      isRelsTime = "";
      isRelsUser = "";
   }
   else {
      if (wp.itemEmpty("rels_date")) {
         isRelsDate = zzdate.sysDate();
      }
      else {
         isRelsDate = wp.itemStr("rels_date");
      }

      if (wp.itemEmpty("rels_time")) {
         isRelsTime = zzdate.sysTime();
      }
      else {
         isRelsTime = wp.itemStr("rels_time");
      }

      if (wp.itemEmpty("rels_user")) {
         isRelsUser = selectUser();
      }
      else {
         isRelsUser = wp.itemStr("rels_user");
      }
   }

   if (isAdd()) return;

   if (ibUpdate || ibDelete) {
      if (checkNew() == false) {
         errmsg("不是 最近之管制記錄[此管制記錄已結案], 不可 修改/刪除");
         return;
      }
   }

   sqlWhere = " where 1=1"
         + " and card_no='" + kk1 + "'"
         + " and ctfg_seqno=" + kk2
         + " and nvl(mod_seqno,0) =" + wp.modSeqno();
   if (this.isOtherModify("RSK_CTFG_MAST", sqlWhere)) {
      return;
   }
}

boolean checkNew()  {

   String sql1 = " select count(*) as db_cnt "
         + " from rsk_ctfg_mast "
         + " where card_no = ? "
         + " and warn_date||warn_time > ? "
         + " and ctfg_seqno <> ? ";

   sqlSelect(sql1, new Object[]{kk1, wp.itemStr("warn_date") + wp.itemStr("warn_time"), kk2});

   return !(colNum("db_cnt") > 0);
}

String selectUser()  {

   String sql1 = " select usr_cname from sec_user where usr_id = ? ";
   sqlSelect(sql1, new Object[]{wp.loginUser});
   if (sqlRowNum > 0) return colStr("usr_cname");

   return "";
}

boolean checkCard(String ls_card_no)  {

   String sql0 = " select count(*) as db_cnt0 from cca_card_base where card_no = ? ";
   sqlSelect(sql0, new Object[]{ls_card_no});
   if (colNum("db_cnt0") <= 0) return false;

   String sql1 = " select count(*) as db_cnt1 from crd_card where card_no = ? ";
   sqlSelect(sql1, new Object[]{ls_card_no});

   if (colNum("db_cnt1") > 0) return true;

   String sql2 = " select count(*) as db_cnt2 from dbc_card where card_no = ? ";
   sqlSelect(sql2, new Object[]{ls_card_no});

   return colNum("db_cnt2") > 0;
}

boolean checkMast(String ls_card_no)  {
   String sql1 = " select count(*) as db_cnt3 from rsk_ctfg_mast where card_no = ? and rels_code = '' ";
   sqlSelect(sql1, new Object[]{ls_card_no});

   return !(colNum("db_cnt3") > 0);
}

int selectCtfgSeqno()  {
   String sql1 = " select ecs_modseq.nextval as kk_ctfg_seqno from dual ";
   sqlSelect(sql1);
   return colInt("kk_ctfg_seqno");
}

@Override
public int dbInsert()  {
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = " insert into rsk_ctfg_mast ( "
         + " card_no ,"
         + " ctfg_seqno ,"
         + " warn_date ,"
         + " warn_time ,"
         + " find_type ,"
         + " warn_user ,"
         + " pay_date ,"
         + " rels_code ,"
         + " rels_date ,"
         + " rels_time ,"
         + " rels_user ,"
         + " id_p_seqno ,"
         + " sms_flag ,"
         + " mod_user ,"
         + " mod_time ,"
         + " mod_pgm ,"
         + " mod_seqno "
         + " ) values ( "
         + " :kk1 ,"
         + " :kk2 ,"
         + " :warn_date ,"
         + " :warn_time ,"
         + " :find_type ,"
         + " :warn_user ,"
         + " :pay_date ,"
         + " :rels_code ,"
         + " :rels_date ,"
         + " :rels_time ,"
         + " :rels_user ,"
         + " :id_p_seqno ,"
         + " :sms_flag ,"
         + " :mod_user ,"
         + " sysdate ,"
         + " :mod_pgm ,"
         + " 1 "
         + " ) "
   ;

   setString("kk1", kk1);
   setString("kk2", "" + kk2);
   setString("warn_date", isWarnDate);
   setString("warn_time", isWarnTime);
   item2ParmStr("find_type");
   setString("warn_user", isWarnUser);
   item2ParmStr("pay_date");
   item2ParmStr("rels_code");
   setString("rels_date", isRelsDate);
   setString("rels_time", isRelsTime);
   setString("rels_user", isRelsUser);
   item2ParmStr("id_p_seqno");
   item2ParmNvl("sms_flag", "N");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3110");
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
   }

   deleteSecond();
   if (rc != 1) return rc;
   if (eqIgno(wp.itemStr("secnd_flag"), "1")) {
      insertSecond();
      if (rc != 1) return rc;
   }
   if (checkCard() == false) {
      updateCard();
   }
   else {
      insertCard();
   }

   if (rc != 1) return rc;

   return rc;
}

boolean checkSecond()  {

   String sql1 = " select count(*) as second_cnt from rsk_ctfg_secnd where card_no = ? and ctfg_seqno = ? ";
   sqlSelect(sql1, new Object[]{kk1, kk2});
   return sqlRowNum > 0 && colNum("second_cnt") > 0;
}

public int deleteSecond()  {
   msgOK();

   strSql = " delete rsk_ctfg_secnd where card_no =:kk1 and ctfg_seqno =:kk2 ";
   setString("kk1", kk1);
   setString("kk2", "" + kk2);
   sqlExec(strSql);

   if (sqlRowNum < 0) {
      errmsg("delete rsk_ctfg_second error ~! ");
   }
   else rc = 1;

   return rc;
}

public int insertSecond()  {
   msgOK();

   strSql = " insert into rsk_ctfg_secnd ( "
         + " card_no ,"
         + " ctfg_seqno ,"
         + " secnd_flag ,"
         + " secnd_date ,"
         + " trans_unit ,"
         + " trans_date ,"
         + " case_type ,"
         + " otb_amt ,"
         + " credit_limit ,"
         + " proc_remark ,"
         + " attm_cnt ,"
         + " attm_amt ,"
         + " close_flag ,"
         + " fraud_ok_cnt ,"
         + " mod_user ,"
         + " mod_time ,"
         + " mod_pgm ,"
         + " mod_seqno "
         + " ) values ( "
         + " :kk1 ,"
         + " :kk2 ,"
         + " '1' ,"
         + " :secnd_date ,"
         + " :trans_unit ,"
         + " :trans_date ,"
         + " :case_type ,"
         + " :otb_amt ,"
         + " :credit_limit ,"
         + " :proc_remark ,"
         + " :attm_cnt ,"
         + " :attm_amt ,"
         + " :close_flag ,"
         + " :fraud_ok_cnt ,"
         + " :mod_user ,"
         + " sysdate ,"
         + " :mod_pgm ,"
         + " 1 "
         + " ) "
   ;
   setString("kk1", kk1);
   setString("kk2", "" + kk2);

   item2ParmStr("trans_unit");
   setString("trans_date", isTransDate);
   setString("secnd_date", isSecndDate);
   item2ParmStr("case_type");
   item2ParmNum("otb_amt");
   item2ParmNum("credit_limit");
   setString("proc_remark", isProcRemark);
   item2ParmNum("attm_cnt");
   item2ParmNum("attm_amt");
   item2ParmStr("close_flag");
   item2ParmNum("fraud_ok_cnt");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3110");

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("insert rsk_ctfg_secnd error !");
   }
   return rc;
}

boolean checkCard()  {

   String sql1 = " select "
         + " count(*) as db_cnt "
         + " from rsk_ctfg_card "
         + " where card_no = ? ";

   sqlSelect(sql1, new Object[]{kk1});

   return !(colNum("db_cnt") > 0);
}

public int insertCard()  {
   msgOK();
   strSql = " insert into rsk_ctfg_card ( "
         + " card_no ,"
         + " secnd_flag ,"
         + " trans_unit ,"
         + " trans_date ,"
         + " case_type ,"
         + " otb_amt ,"
         + " credit_limit ,"
         + " sec_proc_remark ,"
         + " sec_close_flag ,"
         + " vip_flag ,"
         + " ibm_flag ,"
         + " spec_remark ,"
         + " attm_cnt ,"
         + " attm_amt ,"
         + " mod_user ,"
         + " mod_time ,"
         + " mod_pgm ,"
         + " mod_seqno "
         + " ) values ( "
         + " :kk1 ,"
         + " :secnd_flag ,"
         + " :trans_unit ,"
         + " :trans_date ,"
         + " :case_type ,"
         + " :otb_amt ,"
         + " :credit_limit ,"
         + " :sec_proc_remark ,"
         + " :sec_close_flag ,"
         + " '' ,"
         + " '' ,"
         + " :spec_remark ,"
         + " :attm_cnt ,"
         + " :attm_amt ,"
         + " :mod_user ,"
         + " sysdate ,"
         + " :mod_pgm ,"
         + " 1 "
         + " ) "
   ;

   setString("kk1", kk1);
   item2ParmStr("secnd_flag");
   item2ParmStr("trans_unit");
   item2ParmStr("trans_date");
   item2ParmStr("case_type");
   item2ParmNum("otb_amt");
   item2ParmNum("credit_limit");
   item2ParmStr("sec_proc_remark", isProcRemark);
   item2ParmStr("sec_close_flag");
   setString("spec_remark", isSpecRemark);
   item2ParmNum("attm_cnt");
   item2ParmNum("attm_amt");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3110");
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("insert rsk_ctfg_card error !");
   }
   return rc;
}

public int updateCard()  {
   msgOK();

   strSql = " update rsk_ctfg_card set "
         + " secnd_flag =:secnd_flag ,"
         + " trans_unit =:trans_unit ,"
         + " trans_date =:trans_date ,"
         + " case_type =:case_type ,"
         + " otb_amt =:otb_amt ,"
         + " credit_limit =:credit_limit ,"
         + " sec_proc_remark =:sec_proc_remark ,"
         + " sec_close_flag =:sec_close_flag ,"
         + " spec_remark =:spec_remark ,"
         + " attm_cnt =:attm_cnt ,"
         + " attm_amt =:attm_amt ,"
         + " mod_user =:mod_user ,"
         + " mod_time =sysdate ,"
         + " mod_pgm =:mod_pgm ,"
         + " mod_seqno = nvl(mod_seqno,0)+1 "
         + " where card_no =:kk1 "
   ;

   item2ParmStr("secnd_flag");
   item2ParmStr("trans_unit");
   item2ParmStr("trans_date");
   item2ParmStr("case_type");
   item2ParmNum("otb_amt");
   item2ParmNum("credit_limit");
   item2ParmStr("sec_proc_remark", isProcRemark);
   item2ParmStr("sec_close_flag");
   setString("spec_remark", isSpecRemark);
   item2ParmNum("attm_cnt");
   item2ParmNum("attm_amt");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3110");
   setString("kk1", kk1);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfg_card error !");
   }

   return rc;
}

@Override
public int dbUpdate()  {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = " update rsk_ctfg_mast set "
         + " warn_date =:warn_date ,"
         + " warn_time =:warn_time ,"
         + " find_type =:find_type ,"
         + " warn_user =:warn_user ,"
         + " pay_date =:pay_date ,"
         + " rels_code =:rels_code ,"
         + " rels_date =:rels_date ,"
         + " rels_time =:rels_time ,"
         + " rels_user =:rels_user ,"
         + " sms_flag =:sms_flag ,"
         + " mod_user =:mod_user ,"
         + " mod_time =sysdate ,"
         + " mod_pgm =:mod_pgm ,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where card_no =:kk1 "
         + " and ctfg_seqno =:kk2 "
   ;

   setString("warn_date", isWarnDate);
   setString("warn_time", isWarnTime);
   item2ParmStr("find_type");
   setString("warn_user", isWarnUser);
   item2ParmStr("pay_date");
   item2ParmStr("rels_code");
   setString("rels_date", isRelsDate);
   setString("rels_time", isRelsTime);
   setString("rels_user", isRelsUser);
   item2ParmNvl("sms_flag", "N");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3110");
   setString("kk1", kk1);
   setString("kk2", "" + kk2);
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfg_mast error !");
      return rc;
   }


   deleteSecond();
   if (rc != 1) return rc;
   if (eqIgno(wp.itemStr("secnd_flag"), "1")) {
      insertSecond();
      if (rc != 1) return rc;
   }


   if (checkCard() == false) {
      updateCard();
   }
   else {
      insertCard();
   }

   if (rc != 1) return rc;

   return rc;
}

int updateSecond()  {
   msgOK();

   strSql = " update rsk_ctfg_secnd set "
         + " secnd_flag =:secnd_flag , "
         + " trans_unit =:trans_unit , "
         + " trans_date =:trans_date , "
         + " case_type =:case_type , "
         + " otb_amt =:otb_amt , "
         + " credit_limit =:credit_limit , "
         + " attm_cnt =:attm_cnt , "
         + " attm_amt =:attm_amt , "
         + " fraud_ok_cnt =:fraud_ok_cnt , "
         + " close_flag =:close_flag , "
         + " proc_remark =:proc_remark , "
         + " mod_user =:mod_user , "
         + " mod_time = sysdate , "
         + " mod_pgm =:mod_pgm , "
         + " mod_seqno = nvl(mod_seqno,0)+1 "
         + " where card_no =:kk1 "
         + " and ctfg_seqno =:kk2 "
   ;

   item2ParmStr("secnd_flag");
   item2ParmStr("trans_unit");
   setString("trans_date", isTransDate);
   item2ParmStr("case_type");
   item2ParmNum("otb_amt");
   item2ParmNum("credit_limit");
   setString("proc_remark", isProcRemark);
   item2ParmNum("attm_cnt");
   item2ParmNum("attm_amt");
   item2ParmStr("close_flag");
   item2ParmNum("fraud_ok_cnt");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskm3110");
   setString("kk1", kk1);
   setString("kk2", "" + kk2);

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
   }

   return rc;
}

@Override
public int dbDelete()  {
   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }
   strSql = "delete rsk_ctfg_mast where card_no =:kk1 and ctfg_seqno =:kk2 ";
   setString("kk1", kk1);
   setString("kk2", "" + kk2);
   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("delete rsk_ctfg_mast error !");
      return rc;
   }

   deleteSecond();

   return rc;
}

//---SMS
public int insertSms()  {
//   rc = dataSelect();
//   if (rc != 1) return rc;

   actionInit("A");
   dataCheck_SMS();
   if (rc != 1) return rc;
   busi.func.SmsMsgDetl oosms=new SmsMsgDetl();
   oosms.setConn(wp);
   oosms.hsms.initData();
   
   String tmpMsgDesc = "";
   tmpMsgDesc = wp.itemStr("msg_userid") +"," +wp.itemStr("msg_id")+","+wp.itemStr("cellar_phone")+","+ wp.itemStr("msg_desc");
   
   oosms.hsms.idNo =kk1;
   oosms.hsms.msgSeqno =kk3;
   oosms.hsms.cellarPhone =wp.itemStr("cellar_phone");
   oosms.hsms.msgDept =wp.itemStr("msg_dept");
   oosms.hsms.chiName =wp.itemStr("chi_name");
   oosms.hsms.idPseqno =wp.itemStr("id_p_seqno");
   oosms.hsms.exId =wp.itemStr("ex_id");
   oosms.hsms.msgUserid =wp.itemStr("msg_userid");
   oosms.hsms.msgId =wp.itemStr("msg_id");
   oosms.hsms.msgDesc =tmpMsgDesc;
   oosms.hsms.chiNameFlag =wp.itemStr("chi_name_flag");
   //tt.aaa("create_txt_date", col_ss("create_txt_date"));
   oosms.hsms.addMode ="O";
   oosms.hsms.phoneFlag ="Y";
   oosms.hsms.sendFlag ="Y";
   oosms.hsms.crtUser =modUser;
//   tt.aaa_ymd("crt_date");
//   tt.aaa_time("crt_time");
   oosms.hsms.aprFlag ="Y";
   oosms.hsms.aprUser =modUser;
//   tt.aaa_ymd("apr_date");
//   tt.aaa_modxxx(mod_user,mod_pgm);

   rc =oosms.insertMsgDtl(tmpMsgDesc);
   if (rc != 1) {
      errmsg(oosms.getMsg());
      return rc;
   }

   strSql = " update rsk_ctfg_mast set "
         + " sms_flag ='Y' "
         + " where card_no =? "
         + " and ctfg_seqno =? "
   ;

   setParm(1,wp.itemStr("card_no"));
   setParm(2,wp.num("ctfg_seqno"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
   }

   return rc;
}


@Override
public int dataSelect()  {
   String proc_tab_name = "";
   proc_tab_name = wp.itemStr("controlTabName");
   strSql = " select "
         + " cellar_phone, "
         + " msg_dept, "
         + " chi_name, "
         + " ex_id, "
         + " msg_desc, "
         + " chi_name_flag, "
         + " create_txt_date, "
         + " add_mode, "
         + " cellphone_check_flag, "
         + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
         + " from " + proc_tab_name
         + " where 1=1"+commSqlStr.whereRowid;

   Object[] param = new Object[]
         {
               wp.itemStr("rowid")
         };

   sqlSelect(strSql, param);
   if (sqlRowNum <= 0) errmsg(sqlErrtext);

   return 1;
}

public void dataCheck_SMS()  {
   if (this.ibAdd) {
      kk1 = wp.itemStr("id_no");
      if (empty(kk1)) {
         errmsg("持卡者ID: 不可空白");
         return;
      }
      kk3 = wp.itemStr("msg_seqno");
   }
   else {
      kk1 = wp.itemStr("id_no");
      kk3 = wp.itemStr("msg_seqno");
   }
   if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
         if (kk1.length() > 0) {
            strSql = "select count(*) as qua "
                  + "from " + orgControlTabName
                  + " where id_no = ? "
                  + "and   msg_seqno = ? "
            ;
            Object[] param = new Object[]{kk1, kk3};
            sqlSelect(strSql, param);
            int qua = Integer.parseInt(colStr("qua"));
            if (qua > 0) {
               errmsg("[持卡者ID:][簡訊流水號:] 不可重複(" + orgControlTabName + ") ,請重新輸入!");
               return;
            }
         }

   if (this.ibAdd)
      if (kk1.length() > 0) {
         strSql = "select count(*) as qua "
               + "from " + controlTabName
               + " where id_no = ? "
               + "and   msg_seqno = ? "
         ;
         Object[] param = new Object[]{kk1, kk3};
         sqlSelect(strSql, param);
         int qua = Integer.parseInt(colStr("qua"));
         if (qua > 0) {
            errmsg("[持卡者ID:][簡訊流水號:] 不可重複(" + controlTabName + ") ,請重新輸入!");
            return;
         }
      }


   if (this.ibAdd) {
      if ((wp.itemStr("exg_apr_date").length() != 0) &&
            (wp.itemStr("aud_type").equals("D")) &&
            (wp.itemStr("controlTabName").equals(orgControlTabName))) {
         errmsg("傳送紀錄資料, 只可修改不可刪除 !");
         return;
      }
   }

   if (wp.itemStr("msg_seqno").length() == 0) {
      busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
      comr.setConn(wp);
      wp.itemSet("msg_seqno", comr.getSeqno("MKT_MODSEQ"));
      kk3 = wp.itemStr("msg_seqno");
   }

   if (wp.itemStr("controlTabName").equals(controlTabName)) {
      wp.itemSet("resend_flag", "N");
      colSet("resend_flag", "N");
   }
   if (this.ibAdd) {
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

      if ((!comm.isNumber(wp.itemStr("cellar_phone"))) ||
            (wp.itemStr("cellar_phone").length() < 10)) {
         errmsg("行動電話: 不符規則 ");
         return;
      }
   }

   if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("cellar_phone")) {
         errmsg("行動電話: 不可空白");
         return;
      }


   if (this.isAdd()) return;

}

}

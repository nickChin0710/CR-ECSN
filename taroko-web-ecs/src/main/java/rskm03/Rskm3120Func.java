package rskm03;
/**
 * 2021-1115:  JH    9044: update error
 * 2020-0114:  Alex  sms audtype = 'A'
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import busi.func.SmsMsgDetl;
import busi.FuncEdit;
import taroko.base.Parm2Sql;
import taroko.com.TarokoCommon;

public class Rskm3120Func extends FuncEdit {
String kk1 = "", kk2 = "";
String is_proc_date = "", is_proc_time = "", is_proc_remark = "";
String org_control_tab_name = "sms_msg_dtl";
String control_tab_name = "sms_msg_dtl_t";
taroko.base.CommDate zzdate = new taroko.base.CommDate();

public Rskm3120Func(TarokoCommon wr) {
   wp = wr;
   this.conn = wp.getConn();
   modUser =wp.loginUser;
   modPgm =wp.modPgm();
//   printVersion("V.2021-1115  No.9044");
}

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public void dataCheck()  {
   kk1 = wp.itemStr("rowid");
   if (empty(wp.itemStr("card_no"))) {
      errmsg("卡號：不可空白");
      return;
   }
   if (empty(wp.itemStr("proc_type"))) {
      errmsg("處理方式：不可空白");
      return;
   }

   is_proc_date = wp.itemStr("proc_date");
   is_proc_time = wp.itemStr("proc_time");
   if (empty(is_proc_date)) is_proc_date = zzdate.sysDate();
   if (empty(is_proc_time)) is_proc_time = zzdate.sysTime();
   is_proc_remark = wp.itemStr("proc_remark_1") + wp.itemStr("proc_remark_2") + wp.itemStr("proc_remark_3") +
         wp.itemStr("proc_remark_4") + wp.itemStr("proc_remark_5");

   if (wp.itemEq("proc_type", "郵寄") && wp.itemNum("ctfg_seqno") == 0) {
      errmsg("管制序號為0時無法郵寄");
      return;
   }

   if (this.isAdd()) {
      return;
   }
   sqlWhere = " where 1=1"
         + " and rowid=x'" + kk1 + "'"
         + " and nvl(mod_seqno,0) =" + wp.modSeqno();

   if (this.isOtherModify("RSK_CTFG_proc", sqlWhere)) {
      return;
   }
}

@Override
public int dbInsert()  {
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   taroko.base.Parm2Sql tt=new Parm2Sql();
   tt.insert("rsk_ctfg_proc");
   tt.parmSet("card_no", wp.itemStr("card_no"));
   tt.parmSet("proc_date", is_proc_date);
   tt.parmSet("proc_time", is_proc_time);
   tt.parmSet("ctfg_seqno", wp.itemNum("ctfg_seqno"));
   tt.parmSet("proc_type", wp.itemStr("proc_type"));
   tt.parmSet("tel_type", wp.itemStr("tel_type"));
   tt.parmSet("tel_no", wp.itemStr("tel_no"));
   tt.parmSet("contr_result", wp.itemStr("contr_result"));
   tt.parmSet("proc_user", wp.itemStr("proc_user"));
   tt.parmSet("cntl_way", wp.itemStr("cntl_way"));
   tt.parmSet("proc_remark", is_proc_remark);
   tt.parmSet("mail_date", wp.itemStr("mail_date"));
   tt.parmSet("otb_amt", wp.itemNum("otb_amt"));
   tt.parmSet("rejt_amt", wp.itemNum("rejt_amt"));
   tt.parmSet("rejt_reason", wp.itemStr("rejt_reason"));
   tt.parmSet("auto_remark", wp.itemStr("auto_remark"));
   tt.parmSet("close_flag", wp.itemNvl("close_flag", "0"));
   tt.parmSet("proc_status", wp.itemStr("proc_status"));
   tt.parmSet("ok_cnt", wp.itemNum("ok_cnt"));
   tt.parmSet("ok_amt", wp.itemNum("ok_amt"));
   tt.parmSet("send_sms_flag", wp.itemNvl("send_sms_flag", "0"));
   tt.parmSet("view_date", wp.itemStr("view_date"));
   tt.parmSet("data_from", wp.itemStr("data_from"));
   tt.parmSet("callout_cnt", wp.itemStr("callout_cnt"));
   tt.parmSet("noneed_reason", wp.itemStr("noneed_reason"));
   tt.parmSet("tx_mode", wp.itemStr("tx_mode"));
   tt.parmSet("wash_amt_flag", wp.itemNvl("wash_amt_flag", "N"));
   tt.parmSet("mesg_terr_flag", wp.itemNvl("mesg_terr_flag", "N"));
   tt.parmSet("id_p_seqno", wp.itemStr("id_p_seqno"));
   tt.modxxxSet(modUser, modPgm);

   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      sqlErr("insert rsk_ctfg_proc");
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

   taroko.base.Parm2Sql tt=new Parm2Sql();
   tt.update("RSK_CTFG_proc");
   tt.parmSet("proc_date", is_proc_date);
   tt.parmSet("proc_time", is_proc_time);
   tt.parmSet("proc_type", wp.itemStr("proc_type"));
   tt.parmSet("tel_type", wp.itemStr("tel_type"));
   tt.parmSet("tel_no", wp.itemStr("tel_no"));
   tt.parmSet("contr_result", wp.itemStr("contr_result"));
   tt.parmSet("proc_user", wp.itemStr("proc_user"));
   tt.parmSet("cntl_way", wp.itemStr("cntl_way"));
   tt.parmSet("proc_remark", is_proc_remark);
   tt.parmSet("mail_date", wp.itemStr("mail_date"));
   tt.parmSet("otb_amt", wp.num("otb_amt"));
   tt.parmSet("rejt_amt", wp.num("rejt_amt"));
   tt.parmSet("rejt_reason", wp.itemStr("rejt_reason"));
   tt.parmSet("auto_remark", wp.itemStr("auto_remark"));
   tt.parmSet("close_flag", wp.itemNvl("close_flag", "0"));
   tt.parmSet("proc_status", wp.itemStr("proc_status"));
   tt.parmSet("ok_cnt", wp.num("ok_cnt"));
   tt.parmSet("ok_amt", wp.num("ok_amt"));
   tt.parmSet("send_sms_flag", wp.itemNvl("send_sms_flag", "0"));
   tt.parmSet("view_date", wp.itemStr("view_date"));
   tt.parmSet("data_from", wp.itemStr("data_from"));
   tt.parmSet("callout_cnt", wp.itemStr("callout_cnt"));
   tt.parmSet("noneed_reason", wp.itemStr("noneed_reason"));
   tt.parmSet("tx_mode", wp.itemStr("tx_mode"));
   tt.parmSet("wash_amt_flag", wp.itemNvl("wash_amt_flag", "N"));
   tt.parmSet("mesg_terr_flag", wp.itemNvl("mesg_terr_flag", "N"));
   tt.modxxxSet(modUser,modPgm);
   //tt.aaa_where(" where rowid =x'?'", kk1);
   tt.whereRowid(kk1);
   tt.whereParm(" and nvl(mod_seqno,0) =?", wp.modSeqno());

   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("update RSK_CTFG_proc error: " + this.sqlErrtext);
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
   strSql = "delete RSK_CTFG_proc "
         + sqlWhere;

   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
   }

   return rc;
}

//--
public int insertSms()  {
   //rc = dataSelect();
   //if (rc != 1) return rc;
   actionInit("A");
   dataCheck_SMS();
   if (rc != 1) return rc;
   busi.func.SmsMsgDetl ooSms=new SmsMsgDetl();
   ooSms.setConn(wp);
   ooSms.hsms.initData();
   
   String tmpMsgDesc = "";
   tmpMsgDesc = wp.itemStr("msg_userid") +"," +wp.itemStr("msg_id")+","+wp.itemStr("cellar_phone")+","+wp.itemStr("msg_desc");
   
   ooSms.hsms.idNo =kk1;
//ooSms.hsms.aud_type =
   ooSms.hsms.msgSeqno =kk2;
   ooSms.hsms.cellarPhone =wp.itemStr("cellar_phone");
   ooSms.hsms.msgDept =wp.itemStr("msg_dept");
   ooSms.hsms.chiName =wp.itemStr("chi_name");
   ooSms.hsms.idPseqno =wp.itemStr("id_p_seqno");
   ooSms.hsms.exId =wp.itemStr("ex_id");
   ooSms.hsms.msgUserid =wp.itemStr("msg_userid");
   ooSms.hsms.msgId =wp.itemStr("msg_id");
   ooSms.hsms.msgDesc = tmpMsgDesc;
   ooSms.hsms.chiNameFlag =wp.itemStr("chi_name_flag");
   ooSms.hsms.addMode ="O";
   ooSms.hsms.phoneFlag ="Y";
   ooSms.hsms.crtUser =modUser;
   ooSms.hsms.aprFlag ="Y";
   ooSms.hsms.aprUser =modUser;
   rc =ooSms.insertMsgDtl(tmpMsgDesc);
   if (rc!=1) {
      errmsg(ooSms.getMsg());
      return rc;
   }

   strSql = " update rsk_ctfg_proc set "
         + " send_sms_flag ='1' "
         + " where 1=1 "+commSqlStr.whereRowid
   ;
   setParm(1,wp.itemStr("rowid"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
   }

   return rc;

}

@Override
public int dataSelect()  {
//   String proc_tab_name = "";
//   proc_tab_name = wp.itemStr("control_tab_name");
//   strSql = " select "
//         + " cellar_phone, "
//         + " msg_dept, "
//         + " chi_name, "
//         + " ex_id, "
//         + " msg_desc, "
//         + " chi_name_flag, "
//         + " create_txt_date, "
//         + " add_mode, "
//         + " cellphone_check_flag, "
//         + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
//         + " from " + proc_tab_name
//         + " where rowid = ? ";
//
//   Object[] param = new Object[]
//         {
//               wp.item_RowId("rowid")
//         };
//
//   sqlSelect(strSql, param);
//   if (sqlRowNum <= 0) errmsg(sqlErrtext);

   return 1;
}

void dataCheck_SMS()  {
   if (!ibAdd) return;

   kk1 = wp.itemStr("id_no");
   if (empty(kk1)) {
      errmsg("持卡者ID: 不可空白");
      return;
   }
   kk2 = wp.itemStr("msg_seqno");
//   }
//   else {
//      kk1 = wp.itemStr("id_no");
//      kk2 = wp.itemStr("msg_seqno");
//   }
//   if (wp.respHtml.indexOf("_nadd") > 0)
//      if (this.ibAdd)
//         if (kk1.length() > 0) {
//            strSql = "select count(*) as qua "
//                  + "from " + org_control_tab_name
//                  + " where id_no = ? "
//                  + "and   msg_seqno = ? "
//            ;
//            Object[] param = new Object[]{kk1, kk2};
//            sqlSelect(strSql, param);
//            int qua = Integer.parseInt(col_ss("qua"));
//            if (qua > 0) {
//               errmsg("[持卡者ID:][簡訊流水號:] 不可重複(" + org_control_tab_name + ") ,請重新輸入!");
//               return;
//            }
//         }

//   if (this.ibAdd)
//      if (kk1.length() > 0) {
//         strSql = "select count(*) as qua "
//               + "from " + control_tab_name
//               + " where id_no = ? "
//               + "and   msg_seqno = ? "
//         ;
//         Object[] param = new Object[]{kk1, kk2};
//         sqlSelect(strSql, param);
//         int qua = Integer.parseInt(col_ss("qua"));
//         if (qua > 0) {
//            errmsg("[持卡者ID:][簡訊流水號:] 不可重複(" + control_tab_name + ") ,請重新輸入!");
//            return;
//         }
//      }


//   if (this.ibAdd) {
//      if ((wp.itemStr("exg_apr_date").length() != 0) &&
//            (wp.itemStr("aud_type").equals("D")) &&
//            (wp.itemStr("control_tab_name").equals(org_control_tab_name))) {
//         errmsg("傳送紀錄資料, 只可修改不可刪除 !");
//         return;
//      }
//   }

   if (wp.itemEmpty("msg_seqno")) {
      busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
      comr.setConn(wp);
      wp.itemSet("msg_seqno", comr.getSeqno("MKT_MODSEQ"));
      kk2 = wp.itemStr("msg_seqno");
   }

//   if (wp.itemStr("control_tab_name").equals(control_tab_name)) {
//      wp.item_set("resend_flag", "N");
//      col_set("resend_flag", "N");
//   }
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

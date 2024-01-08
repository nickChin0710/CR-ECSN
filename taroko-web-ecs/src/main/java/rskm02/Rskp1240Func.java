package rskm02;
/**
 * 商務卡覆審 JCIC查詢及統計作業
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 * V.2018-0820
 */

import busi.FuncAction;
import taroko.base.Parm2Sql;

public class Rskp1240Func extends FuncAction {
String kk_batch_no = "", is_appr_no = "";
int il_ok = 0, il_err = 0;
String is_corp_jcic_no = "", is_corp_jcic_reason = "";
String is_idno_jcic_no = "", is_idno_jcic_reason = "";

busi.DataSet ds_list = null;

@Override
public void dataCheck() {
   // -- select JCIC

   String sql1 = " select "
         + " purpose_code "
         + " from col_jcic_query_mast "
         + " where jcic_no = ? ";
   is_corp_jcic_no = colNvl("A.corp_jcic_no", "609");
   setParm(1, is_corp_jcic_no);
   sqlSelect(sql1);
   if (sqlRowNum <= 0) {
      errmsg("JCIC合約代號 不存在, [%s]", is_corp_jcic_no);
      return;
   }
   is_corp_jcic_reason = colStr("purpose_code");

   is_idno_jcic_no = colNvl("A.idno_jcic_no", "610");
   setParm(1, is_idno_jcic_no);
   sqlSelect(sql1);
   if (sqlRowNum <= 0) {
      errmsg("JCIC合約代號 不存在, [%s]", is_idno_jcic_no);
      return;
   }
   is_idno_jcic_reason = colStr("purpose_code");

   //-check JCIC是否己回覆-
   strSql = "select count(*) as jcic_cnt"
         + " from col_jcic_file_log"
         + " where trial_batch_no =?"
         + " and resp_date <>''";
   setParm(1, kk_batch_no);
   sqlSelect(strSql);
   if (sqlRowNum > 0 && colInt("jcic_cnt") > 0) {
      errmsg("JCIC已回覆, 不可再匯入名單, [%s]", kk_batch_no);
   }
}

void selectRsk_trcorp_mast(String a_batch_no) {
   if (empty(a_batch_no)) {
      errmsg("覆審批號: 不可空白");
      return;
   }
   daoTid = "A.";
   strSql = "select * from rsk_trcorp_mast"
         + " where batch_no =?"
   ;
   setParm(1, a_batch_no);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("select rsk_trcorp_mast error, kk=" + a_batch_no);
      return;
   }

}

@Override
public int dbInsert() {
   // -import COL_JCIC_QUERY_req-
   kk_batch_no = varsStr("batch_no");
   selectRsk_trcorp_mast(kk_batch_no);
   if (rc != 1)
      return rc;
   dataCheck();
   if (rc != 1)
      return rc;

   // --覆審批號
   is_appr_no = "";
   String sql2 = "select to_char(max(to_number(apr_no))+1) as ls_appr_no "
         + " from	col_jcic_query_req "
         + " where apr_no like ? ";
   sqlSelect(sql2, new Object[]{
         this.getSysDate() + "%"
   });
   is_appr_no = colStr("ls_appr_no");
   if (empty(is_appr_no)) {
      is_appr_no = this.getSysDate() + "01";
   }
   if (empty(is_appr_no)) {
      errmsg("無法取得覆核批號");
      return rc;
   }

   return rc;
   //-改為: callBatch()-
   //-import-corp_no-
//	import_corp_no();
//	
//	vars_set("ok_cnt",""+il_ok);
//	vars_set("err_cnt",""+il_err);
//	return rc;
}

//void select_trcorp_parm() {
//	strSql ="select risk_group, corp_jcic_send, idno_jcic_send"
//			+" from rsk_trcorp_parm"
//			+" where apr_flag ='Y'"
//			+" and risk_group in (select distinct risk_group1"
//			+" from rsk_trcorp_list where batch_no =?)"
//			;
//	setParm(1,kk_batch_no);
//	ds_parm.colList =sqlQuery(strSql);
//	if (ds_parm.list_rows()==0) {
//		errmsg("查無商務卡族群參數; kk[%s]",kk_batch_no);
//	}
//}

void importCorp_no() {
   ds_list = new busi.DataSet();

   strSql = "select A.corp_no, A.corp_p_seqno, A.risk_group1"
         + " , B.corp_jcic_send, B.idno_jcic_send"
         + ", C.charge_id"
         + " from rsk_trcorp_list A left join rsk_trcorp_parm B on A.risk_group1 =B.risk_group and B.apr_flag='Y'"
         + " join crd_corp C on C.corp_p_seqno =A.corp_p_seqno"
         + " where A.batch_no =?";
   setParm(1, kk_batch_no);
   ds_list.colList = sqlQuery(strSql);
   if (sqlRowNum <= 0) {
      errmsg("select rsk_trcorp_list no-data, kk[%s]", kk_batch_no);
      return;
   }

   while (ds_list.listNext()) {
      //-統編-
      String ls_corp_no = ds_list.colStr("corp_no");
      String ls_corp_p_seqno = ds_list.colStr("corp_p_seqno");
      if (ds_list.colEq("corp_jcic_send", "N") == false) {
         insertJcic_query_Req(2, ls_corp_no, ls_corp_p_seqno);
      }

      //-公司負責人-
      if (ds_list.colEq("idno_jcic_send", "N"))
         continue;
      importCharge_id(ds_list.colStr("charge_id"));
   }

   ds_list.dataClear();
   return;
}

void importCharge_id(String a_idno) {
   if (empty(a_idno))
      return;

   strSql = "select id_p_seqno, id_no"
         + " from crd_idno"
         + " where id_no in ?"
         + " order by id_no_code"
         + commSqlStr.rownum(1)
   ;
   setParm(1, a_idno);
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return;

   String ls_idno = colStr("id_no");
   String ls_id_pseqno = colStr("id_p_seqno");

   //-check流通卡-
   strSql = "select count(*) as card_cnt"
         + " from crd_card"
         + " where id_p_seqno =? and current_code ='0'";
   setParm(1, ls_id_pseqno);
   sqlSelect(strSql);
   if (sqlRowNum <= 0 || colInt("card_cnt") == 0)
      return;

   insertJcic_query_Req(1, ls_idno, ls_id_pseqno);
}

@Override
public int dbUpdate() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

void insertJcic_query_Req(int ai_type, String a_id_corp, String a_p_seqno) {
   String sql1 = "slect count(*) as xx_cnt from col_jcic_query_req"
         + " where id_no =? and batch_no =?";
   setParm(1, a_id_corp);
   setParm(2, kk_batch_no);
   sqlSelect(sql1);
   if (sqlRowNum > 0 && colInt("xx_cnt") > 0) {
      il_err++;
      return;
   }

   taroko.base.Parm2Sql spp=new Parm2Sql();
   spp.insert("col_jcic_query_req");
   spp.parmSet("id_p_seqno", a_p_seqno);
   spp.parmSet("id_no", a_id_corp);
   if (ai_type == 1) {
      spp.parmSet("jcic_no", is_idno_jcic_no);
      spp.parmSet("reason_code", is_idno_jcic_reason);
   }
   else {
      spp.parmSet("jcic_no", is_corp_jcic_no);
      spp.parmSet("reason_code", is_corp_jcic_reason);
   }
   spp.parmSet("add_type", ",'2'");
   spp.parmSet("dept_no", wp.loginDeptNo);
   spp.parmSet("batch_no", kk_batch_no);
   spp.parmSet("id_type", "" + ai_type);
   spp.parmSet("apr_user", modUser);
   spp.parmSet("apr_date", sysDate);
   spp.parmSet("apr_no", is_appr_no);
   spp.modxxxSet(modUser,modPgm);

   sqlExec(spp.getSql(),spp.getParms());
   if (sqlRowNum <= 0) {
      errmsg("insert col_jcic_query_req error, kk[%s]", a_id_corp);
      il_err++;
      return;
   }

   il_ok++;
}

public int updateLogProc4() {
   msgOK();
   strSql = " update col_jcic_file_log set "
         + " trial_batch_no = nvl(trial_batch_no , :batch_no) ,"
         + " mod_user = :mod_user , "
         + " mod_time = sysdate , "
         + " mod_pgm = :mod_pgm , "
         + " mod_seqno = nvl(mod_seqno,0)+1 "
         + " where 1=1 and "
         + " (substrb(send_date,1,4), resp_file) in "
         + " (select distinct substrb(send_date,1,4), resp_file "
         + " from  col_jcic_query_req "
         + " where 1=1 "
         + " and batch_no = :batch_no "
         + " and resp_file <>'') ";
   var2ParmStr("batch_no");
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", "rskp1240");
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update col_jcic_file_log (C4) error !");
   }
   return rc;
}

}

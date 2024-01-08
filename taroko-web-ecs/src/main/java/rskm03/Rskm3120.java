package rskm03;
/**
 * 2020-0115:  Alex  fix bug
 * 2020-0114:  Alex  fix warn_user_1
 * 2019-1219:  Alex  ctfg_seqno =0 not read mast
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Rskm3120 extends BaseEdit {
Rskm3120Func func;
String kk1 = "", kk2 = "";
String isUserName = "";
String isWhere = "";
taroko.base.CommDate zzdate = new taroko.base.CommDate();

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   msgOK();

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "X":
         strAction = "new";
         clearFunc();
         break;
      case "X1": /* 轉換顯示畫面 */
         strAction = "new";
         readCtfgMast();
         break;
      case "Q": /* 查詢功能 */
         strAction = "Q";
         queryFunc();
         break;
      case "R":      //-資料讀取-
         strAction = "R";
         dataRead();
         break;
      case "R2": //-資料讀取-
         strAction = "R2";
         break;
      case "A":      /* 新增簡訊 */
         insertFunc();
         break;
      case "U":      /* 更新功能 */
         updateFunc();
         break;
      case "D":      /* 刪除功能 */
         deleteFunc();
         break;
      case "M":      /* 瀏覽功能 :skip-page*/
         queryRead();
         break;
      case "S":      /* 動態查詢 */
         querySelect();
         break;
      case "S2":      /* 動態查詢 */
         readSmsFlag();
         break;
      case "L":   /* 清畫面 */
         strAction = "";
         clearFunc();
         break;
      case "Z":
         selectData();
         break;
      default:
         alertErr("未指定 actionCode 功能 [%s]", wp.buttonCode);
   }

   dddwSelect();
   initButton();
}

@Override
public void initPage() {

}


@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("proc_type");
         dddwList("d_dddw_proc_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_PROC_TYPE'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("tel_type");
         dddwList("d_dddw_tel_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_TEL_TYPE'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("contr_result");
         dddwList("d_dddw_contr_result", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_CONTR_RESULT'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("cntl_way");
         dddwList("d_dddw_cntl_way", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_CNTL_WAY'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("data_from");
         dddwList("d_dddw_data_from", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_DATA_FROM'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("rejt_reason");
         dddwList("d_dddw_rejt_reason", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_REJT_REASON'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("proc_status");
         dddwList("d_dddw_proc_status", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_PROC_STATUS'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("noneed_reason");
         dddwList("d_dddw_noneed_reason", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_NONEED_REASON'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if ((wp.respHtml.equals("rskm3120_nadd"))) {
         wp.optionKey = "";
         wp.initOption = "";
         if (wp.colStr("msg_dept").length() > 0) {
            wp.optionKey = wp.colStr("msg_dept");
         }
         this.dddwList("dddw_dept_code"
               , "ptr_dept_code"
               , "trim(dept_code)"
               , "trim(dept_name)"
               , " where 1 = 1 ");
         wp.optionKey = "";
         wp.initOption = "";
         if (wp.colStr("ex_id").length() > 0) {
            wp.optionKey = wp.colStr("ex_id");
         }
         this.dddwList("dddw_msg_ex"
               , "sms_msg_ex"
               , "trim(ex_id)"
               , "trim(ex_desc)"
               , " where stop_flag!='Y'");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {

   if (wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_idno")) {
      alertErr("卡號 身分證ID不可同時空白");
      return;
   }
   
   isWhere = " where 1=1"+ sqlCol(wp.itemStr("ex_card_no"), "card_no");
   if (!wp.itemEmpty("ex_idno")) {		   
	   isWhere += " and id_p_seqno in "
	            + " (select id_p_seqno from crd_idno where 1=1 " +sqlCol(wp.itemStr("ex_idno"),"id_no")
	            + " union "
	            + " select id_p_seqno from dbc_idno where 1=1 " +sqlCol(wp.itemStr("ex_idno"),"id_no")
	            +")"
	            ;
   }	   
   
   wp.whereStr = isWhere;
   wp.queryWhere = wp.whereStr;   
   queryRead2();   
   if (rc == 1) {
	   isWhere = " where 1=1"+ sqlCol(wp.itemStr("ex_card_no"), "card_no");
	   isWhere += sqlCol(wp.itemStr("ex_proc_date1"), "proc_date", ">=") + sqlCol(wp.itemStr("ex_proc_user"), "proc_user", "like%");	   
	   if (!wp.itemEmpty("ex_idno")) {		   
		   isWhere += " and id_p_seqno in "
		            + " (select id_p_seqno from crd_idno where 1=1 " +sqlCol(wp.itemStr("ex_idno"),"id_no")
		            + " union "
		            + " select id_p_seqno from dbc_idno where 1=1 " +sqlCol(wp.itemStr("ex_idno"),"id_no")
		            +")"
		            ;
	   }	   
	   wp.whereStr = isWhere;
	   wp.queryWhere = wp.whereStr;
	   queryRead();
   }   
}

@Override
public void queryRead() throws Exception {

   this.daoTid = "A_";
   wp.selectSQL = "card_no, "
         + "proc_date,"
         + "proc_time, "
         + "ctfg_seqno,"
         + "proc_type,"
         + "tel_type,"
         + "tel_no,"
         + "contr_result,"
         + "proc_user,"
         + "cntl_way,"
         + "proc_remark,"
         + "hex(rowid) as rowid,"
         + "mail_date,"
         + "uf_idno_id2(card_no,'') as id_no "
   ;
   wp.daoTable = "rsk_ctfg_proc";
   wp.whereOrder = " order by proc_date Desc , proc_time Desc ";
   wp.whereStr = isWhere;
   pageQuery();
   if (sqlNotFind()) {
      selectOK();
      return;
   }
   wp.setListCount(1);

   queryAfter();

}

void queryAfter() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "A_contr_result", "本人消費")) {
         wp.colSet(ii, "wk_color", "style='background-color:deepskyblue'");
      }
      else if (wp.colEq(ii, "A_contr_result", "非本人消費")) {
         wp.colSet(ii, "wk_color", "style='background-color:red'");
      }
   }
}

void queryRead2() throws Exception {
   this.daoTid = "B_";
   wp.selectSQL = "card_no, "
         + "ctfg_seqno,"
         + "warn_date, "
         + "warn_time,"
         + "find_type,"
         + "warn_user,"
         + "pay_date,"
         + "rels_code,"
         + "rels_date,"
         + "rels_time,"
         + "rels_user,"
         + "uf_idno_id2(card_no,'') as id_no"
   ;
   wp.daoTable = "rsk_ctfg_mast";
   wp.whereOrder = " order by warn_date Desc , warn_time Desc ";
   pageQuery();
   wp.setListCount(2);
   if (sqlNotFind()) {
      selectOK();
      return;
   }
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   dataRead();

}

@Override
public void dataRead() throws Exception {
   if (empty(kk1)) {
      kk1 = itemKk("rowid");
   }
   wp.selectSQL = "hex(A.rowid) as rowid, A.mod_seqno, "
         + "A.card_no, "
         + "uf_idno_id2(A.card_no,'') as id_no , "
         + "uf_card_name(A.card_no) as chi_name , "
         + "A.proc_date, "
         + "A.proc_time, "
         + "A.ctfg_seqno,"
         + "A.proc_type,"
         + "A.tel_type,"
         + "A.tel_no,"
         + "A.contr_result,"
         + "A.proc_user,"
         + "A.cntl_way,"
         + "A.proc_remark,"
         + " A.mail_date, "
         + " A.otb_amt, "
         + "A.rejt_amt,"
         + "A.rejt_reason,"
         + "A.auto_remark,"
         + "A.close_flag,"
         + "A.proc_status,"
         + "A.ok_cnt,"
         + "A.ok_amt,"
         + "A.send_sms_flag,"
         + "A.view_date,"
         + "A.data_from,"
         + "A.callout_cnt,"
         + "A.noneed_reason,"
         + "A.mod_user,"
         + "to_char(mod_time,'yyyymmdd') as mod_date,"
         + "A.mod_pgm,"
         + "A.tx_mode,"
//				  + "B.spec_remark, "
//				  + "C.warn_date, "
//				  + "C.warn_time, "
//				  + "C.find_type, "
//				  + "C.warn_user, "
//				  + "C.rels_code, "
//				  + "C.rels_date, "
//				  + "C.rels_time, "
//				  + "C.rels_user, "
         + "A.id_p_seqno as db_idno, "
         + "A.wash_amt_flag, "
         + "A.mesg_terr_flag, "
         + "substr(A.proc_remark,1,60) as proc_remark_1 , "
         + "substr(A.proc_remark,61,60) as proc_remark_2 , "
         + "substr(A.proc_remark,121,60) as proc_remark_3 , "
         + "substr(A.proc_remark,181,60) as proc_remark_4 , "
         + "substr(A.proc_remark,241,60) as proc_remark_5  "
   ;
   //wp.daoTable = "rsk_ctfg_proc A left join  rsk_ctfg_card B  on A.card_no = B.card_no  left join RSK_CTFG_MAST C on A.CTFG_SEQNO = C.CTFG_SEQNO   and C.card_no = A.CARD_NO";
   wp.daoTable = "rsk_ctfg_proc A";
   wp.whereStr = " where 1=1 "
         + commSqlStr.whereRowid
   ;
   setString(1, kk1);
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1 + ", " + kk2);
      return;
   }
   selectRskCtfgMast();
}

void selectRskCtfgMast() throws Exception {

   if (wp.colEq("ctfg_seqno", "0")) return;

   wp.selectSQL = ""
         + "A.warn_date, "
         + "A.warn_time, "
         + "A.find_type, "
         + "A.warn_user, "
         + "A.warn_user as warn_user_1 , "
         + "A.rels_code, "
         + "A.rels_date, "
         + "A.rels_time, "
         + "A.rels_user, "
         + "B.spec_remark, "
         + "substr(B.spec_remark,1,60) as spec_remark_1 , "
         + "substr(B.spec_remark,61,60) as spec_remark_2 , "
         + "substr(B.spec_remark,121,60) as spec_remark_3 , "
         + "substr(B.spec_remark,181,60) as spec_remark_4 , "
         + "substr(B.spec_remark,241,60) as spec_remark_5  "
   ;
   wp.daoTable = "rsk_ctfg_mast A left join rsk_ctfg_card B on A.card_no = B.card_no ";
   wp.whereStr = " where 1=1 "
         + sqlCol(wp.colStr("card_no"), "A.card_no")
         + sqlCol(wp.colStr("ctfg_seqno"), "A.ctfg_seqno")
         + " order by A.card_no, A.warn_date desc"
         + " fetch first 1 rows only"
   ;
   pageSelect();
   if (sqlRowNum <= 0) selectOK();
}

@Override
public void saveFunc() throws Exception {
   if (eqIgno(wp.respHtml, "rskm3120_detl")) {
      func = new Rskm3120Func(wp);
      rc = func.dbSave(strAction);
      this.sqlCommit(rc);
      if (rc != 1) {
         alertErr(func.getMsg());
         return;
      }
      if (eqIgno(strAction,"U")) {
         dataRead();
      }
   }
   else {
      func = new Rskm3120Func(wp);
      rc = func.insertSms();
      this.sqlCommit(rc);
      if (rc != 1) {
         alertErr(func.getMsg());
      }
   }

}


@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
   }
}


void readCtfgMast() throws Exception {
   String lsCardNo = wp.itemStr("data_k1");
   String lsSeqNo = wp.itemStr("data_k2");
   wp.selectSQL = ""
         + "A.warn_date, "
         + "A.warn_time, "
         + "A.find_type, "
         + "A.warn_user, "
         + "A.warn_user as warn_user_1 , "
         + "A.rels_code, "
         + "A.rels_date, "
         + "A.rels_time, "
         + "A.rels_user, "
         + "A.id_p_seqno, "
         + "B.spec_remark, "
         + "substr(B.spec_remark,1,60) as spec_remark_1 , "
         + "substr(B.spec_remark,61,60) as spec_remark_2 , "
         + "substr(B.spec_remark,121,60) as spec_remark_3 , "
         + "substr(B.spec_remark,181,60) as spec_remark_4 , "
         + "substr(B.spec_remark,241,60) as spec_remark_5  "
   ;
   wp.daoTable = "rsk_ctfg_mast A left join rsk_ctfg_card B on A.card_no = B.card_no ";
   wp.whereStr = " where 1=1 "
         + sqlCol(lsCardNo, "A.card_no")
         + sqlCol(lsSeqNo, "A.ctfg_seqno")
         + " order by A.card_no, A.warn_date desc"
         + " fetch first 1 rows only"
   ;

   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + lsCardNo + ", " + lsSeqNo);
      return;
   }
   wp.colSet("card_no", lsCardNo);
   wp.colSet("ctfg_seqno", lsSeqNo);
   getInitData(lsCardNo);
}

void getInitData(String lsCardNo) throws Exception  {
   String sql1 = " select uf_idno_id2(?,'') as id_no , uf_card_name(?) as chi_name from dual ";
   sqlSelect(sql1, new Object[]{lsCardNo, lsCardNo});
   if (sqlRowNum > 0) {
      wp.colSet("id_no", sqlStr("id_no"));
      wp.colSet("chi_name", sqlStr("chi_name"));
   }
   
   String sql2 = " select id_p_seqno from cca_card_base where card_no = ? ";
   sqlSelect(sql2,new Object[] {lsCardNo});
   if (sqlRowNum > 0) {
	  wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));	  
   }
   
   wp.colSet("proc_date", zzdate.sysDate());
   wp.colSet("proc_time", zzdate.sysTime());
   selectUserName();
   wp.colSet("proc_user", isUserName);
}

void readCtfgProc() throws Exception  {
   wp.selectSQL = "hex(A.rowid) as rowid, A.mod_seqno, "
         + "A.card_no,   "
         + "A.ctfg_seqno"
   ;
   wp.daoTable = "rsk_ctfg_proc A ";
   wp.whereStr = "where 1=1"
         + " and rowid = :rowid"
   ;
   setRowid("rowid", kk1);
   pageSelect();
}

public void wfAjaxCardNo(TarokoCommon wr) throws Exception {
   super.wp = wr;

   // String ls_winid =
   selectCard(wp.itemStr("ax_card_no"));
   if (rc != 1) {
      return;
   }

   wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
   wp.addJSON("id_no", sqlStr("id_no"));
   wp.addJSON("chi_name", sqlStr("chi_name"));
   selectUserName();
   wp.addJSON("proc_user", isUserName);
   wp.addJSON("warn_user", sqlStr("warn_user"));
   wp.addJSON("proc_date", zzdate.sysDate());
   wp.addJSON("proc_time", zzdate.sysTime());
   wp.addJSON("ctfg_seqno", sqlStr("ctfg_seqno"));
   wp.addJSON("find_type", sqlStr("find_type"));
   wp.addJSON("warn_user_1", sqlStr("warn_user"));
   wp.addJSON("rels_code", sqlStr("rels_code"));
   wp.addJSON("rels_user", sqlStr("rels_user"));
   wp.addJSON("spec_remark", sqlStr("spec_remark"));
   wp.addJSON("spec_remark_1", sqlStr("spec_remark_1"));
   wp.addJSON("spec_remark_2", sqlStr("spec_remark_2"));
   wp.addJSON("spec_remark_3", sqlStr("spec_remark_3"));
   wp.addJSON("spec_remark_4", sqlStr("spec_remark_4"));
   wp.addJSON("spec_remark_5", sqlStr("spec_remark_5"));
   if (!empty(sqlStr("warn_date"))) {
      wp.addJSON("warn_date", commString.strToYmd(sqlStr("warn_date")));
   }
   if (!empty(sqlStr("rels_date"))) {
      wp.addJSON("rels_date", commString.strToYmd(sqlStr("rels_date")));
   }
   if (!empty(sqlStr("warn_time"))) {
      String ls_warn_time = "";
      if (!empty(commString.mid(sqlStr("warn_time"), 0, 2))) ls_warn_time += commString.mid(sqlStr("warn_time"), 0, 2);
      if (!empty(commString.mid(sqlStr("warn_time"), 2, 2))) ls_warn_time += ":" + commString.mid(sqlStr("warn_time"), 2, 2);
      if (!empty(commString.mid(sqlStr("warn_time"), 4, 2))) ls_warn_time += ":" + commString.mid(sqlStr("warn_time"), 4, 2);
      wp.addJSON("warn_time", ls_warn_time);
   }
   if (!empty(sqlStr("rels_time"))) {
      String ls_rels_time = "";
      if (!empty(commString.mid(sqlStr("rels_time"), 0, 2))) ls_rels_time += commString.mid(sqlStr("rels_time"), 0, 2);
      if (!empty(commString.mid(sqlStr("rels_time"), 2, 2))) ls_rels_time += ":" + commString.mid(sqlStr("rels_time"), 2, 2);
      if (!empty(commString.mid(sqlStr("rels_time"), 4, 2))) ls_rels_time += ":" + commString.mid(sqlStr("rels_time"), 4, 2);
      wp.addJSON("rels_time", ls_rels_time);
   }

}

void selectCard(String s1) throws Exception  {

   String sql1 = " select "
         + " debit_flag "
         + " from cca_card_base "
         + " where card_no = ? ";

   sqlSelect(sql1, new Object[]{s1});

   if (sqlRowNum <= 0) {
      alertErr("查無 卡號: card_no=" + s1);
      return;
   }

   if (eqIgno(sqlStr("debit_flag"), "Y")) {
      String sql2 = " select id_p_seqno , uf_idno_id2(id_p_seqno,'Y') as id_no , uf_card_name(card_no) as chi_name "
            + " from dbc_card  "
            + " where card_no = ?  ";

      sqlSelect(sql2, new Object[]{s1});

   }
   else {
      String lsSql = "select id_p_seqno, uf_idno_id(id_p_seqno) as id_no , uf_card_name(card_no) as chi_name"
            + " from crd_card "
            + " where card_no = ? ";
      sqlSelect(lsSql, new Object[]{s1});
   }

   if (sqlRowNum <= 0) {
      alertErr("查無 卡號: card_no=" + s1);
      return;
   }

   String sql3 = " select "
         + " A.ctfg_seqno , "
         + " A.find_type , "
         + " A.warn_date , "
         + " A.warn_time , "
         + " A.warn_user , "
         + " A.rels_code , "
         + " A.rels_date , "
         + " A.rels_time , "
         + " A.rels_user , "
         + " B.spec_remark , "
         + " substr(B.spec_remark,1,60) as spec_remark_1 , "
         + " substr(B.spec_remark,61,60) as spec_remark_2 , "
         + " substr(B.spec_remark,121,60) as spec_remark_3 , "
         + " substr(B.spec_remark,181,60) as spec_remark_4 , "
         + " substr(B.spec_remark,241,60) as spec_remark_5  "
         + " from rsk_ctfg_mast A left join rsk_ctfg_card B on A.card_no = B.card_no where A.card_no = ? and A.rels_code = '' ";
   sqlSelect(sql3, new Object[]{s1});
   rc = 1;

   return;
}

void selectUserName() throws Exception  {
   String sql1 = " select usr_cname from sec_user where usr_id = ? ";
   sqlSelect(sql1, new Object[]{wp.loginUser});

   if (sqlRowNum <= 0) return;

   isUserName = sqlStr("usr_cname");
}

void selectData() throws Exception  {

   String sql1 = " select debit_flag from cca_card_base where card_no = ? ";
   sqlSelect(sql1, new Object[]{wp.itemStr("data_k1")});

   if (sqlRowNum <= 0) {
      errmsg("查無此卡人");
      return;
   }

   if (eqIgno(sqlStr("debit_flag"), "Y")) {
      wp.sqlCmd = " select "
            + " a.cellar_phone as cellar_phone ,"
            + " a.chi_name as chi_name ,"
            + " a.id_p_seqno as id_p_seqno "
            + " from  dbc_idno a "
            + " where a.id_no ='" + wp.itemStr("id_no") + "' "
      ;
   }
   else {
      wp.sqlCmd = " select "
            + " a.cellar_phone as cellar_phone ,"
            + " a.chi_name as chi_name ,"
            + " a.id_p_seqno as id_p_seqno "
            + " from  crd_idno a "
            + " where a.id_no ='" + wp.itemStr("id_no") + "' "
      ;
   }


   this.sqlSelect();
   if (sqlRowNum <= 0)
      alertErr("持卡者ID:[" + wp.itemStr("id_no") + "]查無資料");

   wp.colSet("cellar_phone", sqlStr("cellar_phone"));
   wp.colSet("chi_name", sqlStr("chi_name"));
   wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));

   return;
}

void readSmsFlag() throws Exception  {
   wp.selectSQL = " send_sms_flag ";
   wp.daoTable = " rsk_ctfg_proc ";
   wp.whereStr = " where 1=1 "
         + commSqlStr.whereRowid  //zzsql.where_rowid(wp.itemStr("rowid"));
   ;

   setString(1, wp.itemStr("rowid"));
   pageSelect();
}

public void wfAjaxFunc1(TarokoCommon wr) throws Exception {
   super.wp = wr;

   if (wp.itemStr("ax_win_ex_id").length() == 0) return;

   selectAjaxFunc10(
         wp.itemStr("ax_win_ex_id"));

   if (rc != 1) {
      wp.addJSON("msg_userid", "");
      wp.addJSON("msg_id", "");
      wp.addJSON("msg_desc", "");
      wp.addJSON("chi_name_flag", "");
      return;
   }

   wp.addJSON("msg_userid", sqlStr("msg_userid"));
   wp.addJSON("msg_id", sqlStr("msg_id"));
   wp.addJSON("msg_desc", sqlStr("msg_desc"));
   wp.addJSON("chi_name_flag", sqlStr("chi_name_flag"));
}

// ************************************************************************
void selectAjaxFunc10(String s1) throws Exception  {
   wp.sqlCmd = " select "
         + " a.msg_userid as msg_userid ,"
         + " a.msg_id as msg_id ,"
         + " a.ex_desc as msg_desc ,"
         + " a.chi_name_flag as chi_name_flag "
         + " from  sms_msg_ex a "
         + " where a.ex_id ='" + s1 + "' "
   ;

   this.sqlSelect();
   if (sqlRowNum <= 0)
      alertErr("簡訊範例[" + s1 + "]查無資料");

   return;
}

}

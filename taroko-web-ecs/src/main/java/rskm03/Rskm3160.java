package rskm03;
/**
 * 2019-1213:  Alex  bug fix
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Rskm3160 extends BaseEdit {
Rskm3160Func func;
String kk1 = "";
String is_user_name = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "L":
         strAction = "";
         clearFunc();
         break;
      case "X":
         strAction = "new";
//         clearFunc();
         break;
      case "Q":
         queryFunc(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "R":
         dataRead(); break;
      case "A":
         insertFunc(); break;
      case "U":
         updateRetrieve = true;
         updateFunc(); break;
      case "D":
         deleteFunc(); break;
      case "AJAX":
    	 wf_ajax_cardno(wr); break;
      default:
         alertErr("未指定 actionCode 執行功能, action[%s]",wp.buttonCode);
   }

   dddwSelect();
   initButton();
   initPage();
}

@Override
public void clearFunc() throws Exception {
   wp.resetInputData();
   wp.resetOutputData();
   wp.initFlag ="Y";
   queryModeClear();

   initPage();

   if (wp.respHtml.indexOf("m3160_detl")>0) {
      selectUserName();
      wp.colSet("case_user", is_user_name);
      wp.colSet("case_date", wp.sysDate);
   }
}

@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.optionKey = wp.colStr("case_type");
         dddwList("dddw_case_type", "rsk_ctfi_casetype"
               , "case_desc", "case_desc", "where 1=1 and apr_flag ='Y'");
         wp.optionKey = wp.colStr("survey_user");
         dddwList("dddw_survey_user", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFI_CASE_USER'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.optionKey = wp.colStr("case_source");
         dddwList("dddw_case_source", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFI_CASE_SOURCE' order by wf_id ");
      }
   }
   catch (Exception ex) {
   }
}


@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_case_date1"), wp.itemStr("ex_case_date2")) == false) {
      alertErr("立案日期起迄：輸入錯誤");
      return;
   }
   if (empty(wp.itemStr("ex_idno"))
         && empty(wp.itemStr("ex_card_no"))
         && empty(wp.itemStr("ex_case_date1"))
         && empty(wp.itemStr("ex_case_date2"))
         && empty(wp.itemStr("ex_case_user"))
         && empty(wp.itemStr("ex_survey_user"))) {
      alertErr("條件不可全部空白");
      return;
   }

   wp.whereStr = " where 1=1"
         + sqlCol(wp.itemStr("ex_case_date1"), "case_date", ">=")
         + sqlCol(wp.itemStr("ex_case_date2"), "case_date", "<=")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no")
         + sqlCol(wp.itemStr("ex_case_user"), "case_user", "like%")
         + sqlCol(wp.itemStr("ex_survey_user"), "survey_user", "like%")
   ;

   if (wp.itemEmpty("ex_idno") == false) {
      wp.whereStr += " and card_no in"
            + " (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 " + sqlCol(wp.itemStr("ex_idno"), "B.id_no")
            + " union "
            + " select A.card_no from dbc_card A join dbc_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 " + sqlCol(wp.itemStr("ex_idno"), "B.id_no")
            + " ) "
      ;
   }

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "card_no, "
         + "id_p_seqno, "
         + "case_date, "
         + "case_seqno, "
         + "group_code, "
         + "new_end_date,"
         + "case_type,"
         + "case_diffi,"
         + "case_source,"
         + "fraud_ok_cnt,"
         + "fraud_ok_amt,"
         + "survey_user,"
         + "case_user,"
         + "uf_idno_id2(card_no,'') as id_no"
   ;
   wp.daoTable = "rsk_ctfi_case";
   wp.whereOrder = " order by case_seqno Asc ";
   pageQuery();

   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   wp.setListCount(0);
   wp.setPageValue();

}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   if (empty(kk1)) {
      kk1 = wp.itemStr("card_no");
   }

   if (empty(kk1)) {
      alertErr("卡號:不可空白");
      return;
   }

   wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
         + "card_no,   "
         + "id_p_seqno, "
         + "case_date, "
         + "case_seqno,"
         + "group_code,"
         + "new_end_date,"
         + "case_type,"
         + "case_diffi,"
         + "case_source,"
         + "fraud_ok_cnt,"
         + "fraud_ok_amt,"
         + "turn_legal_flag,"
         + "turn_legal_date,"
         + "turn_coll_flag,"
         + "turn_coll_date,"
         + "survey_remark,"
         + "survey_user,"
         + "case_remark,"
         + "addr1,"
         + "addr2,"
         + "bill_addr,"
         + "rpad(' ',20) db_chi_name,"
         + "rpad(' ',8) db_bir_date,"
         + "case_user,"
         + "tel_no1,"
         + "tel_no2,"
         + "tel_no3,"
         + "ctfg_seqno,"
         + "vd_flag,"
         + "mod_user,"
         + "to_char(mod_time,'yyyymmdd') as mod_date ,"
         + "mod_pgm"
   //	  + "mod_ws,"
   //	  + "mod_log"
   ;
   wp.daoTable = "rsk_ctfi_case";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "card_no")
   ;

   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }
   read_After(kk1);
}

void read_After(String a_cardNo) throws Exception  {
   if (empty(a_cardNo)) return;

   String sql1 ="select A.id_no as db_idno"+
         ", A.chi_name as db_chi_name"+
         ", A.birthday as db_bir_date"+
         " from crd_idno A join crd_card B on A.id_p_seqno=B.id_p_seqno"+
         " where card_no =?"+
         " union select A.id_no as db_idno"+
         ", A.chi_name as db_chi_name"+
         ", A.birthday as db_bir_date"+
         " from dbc_idno A join dbc_card B on A.id_p_seqno=B.id_p_seqno"+
         " where card_no =?"
         ;
   sqlSelect(sql1, new Object[]{a_cardNo,a_cardNo});
   if (sqlRowNum >0) {
      sql2wp("db_idno");
      sql2wp("db_chi_name");
      sql2wp("db_bir_date");
   }
}

@Override
public void saveFunc() throws Exception {
   func = new Rskm3160Func(wp);

   switch (strAction) {
      case "A":
         rc =func.dbInsert(); break;
      case "U":
         rc =func.dbUpdate(); break;
      case "D":
         rc =func.dbDelete(); break;
   }
   this.sqlCommit(rc);

   if (rc != 1) {
      alertErr(func.getMsg());
   }
}


@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
   }
}

@Override
public void initPage() {
   if (eqIgno(wp.respHtml, "rskm3160_detl") && eqIgno(strAction, "new")) {
      selectUserName();
      wp.colSet("case_date", wp.sysDate);
      wp.colSet("case_user", is_user_name);
   }
}

void select_idnoData(String s1) throws Exception  {
   if (empty(s1)) {
      rc=0;
      return;
   }
   String sql1 = "select b.id_no as db_idno,"
         + " b.chi_name as db_chi_name, b.birthday as db_bir_date,"
         + " 'C' as vd_flag, "
         + " b.home_area_code1||'-'||b.home_tel_no1 as tel_no1,"
         + " b.office_area_code1||'-'||b.office_tel_no1 as tel_no2,"
         + " b.cellar_phone as tel_no3,"
         + " a.bill_sending_addr1||a.bill_sending_addr2||a.bill_sending_addr3||a.bill_sending_addr4||a.bill_sending_addr5 as bill_addr, "
         + " c.new_end_date, c.group_code, b.id_p_seqno "
         + " from crd_card C join crd_idno B on C.id_p_seqno=B.id_p_seqno"
         + " join act_acno A on A.acno_p_seqno=C.acno_p_seqno"
         + " where C.card_no = ? "
         +" union "
         +"select b.id_no as db_idno,"
         + " b.chi_name as db_chi_name, b.birthday as db_bir_date,"
         + " 'D' as vd_flag, "
         + " b.home_area_code1||'-'||b.home_tel_no1 as tel_no1,"
         + " b.office_area_code1||'-'||b.office_tel_no1 as tel_no2,"
         + " b.cellar_phone as tel_no3,"
         + " a.bill_sending_addr1||a.bill_sending_addr2||a.bill_sending_addr3||a.bill_sending_addr4||a.bill_sending_addr5 as bill_addr, "
         + " c.new_end_date, c.group_code, b.id_p_seqno "
         + " from dbc_card C join dbc_idno B on C.id_p_seqno=B.id_p_seqno"
         + " join dba_acno A on A.p_seqno=C.p_seqno"
         + " where C.card_no = ? "
         ;
   sqlSelect(sql1, new Object[]{s1,s1});
   if (sqlRowNum <= 0) {
      alertErr("查無卡號: Card_No=" + s1);
   }

   return;
}

public void wf_ajax_cardno(TarokoCommon wr) throws Exception {
   super.wp = wr;
   msgOK();

   select_idnoData(wp.itemStr("ax_winid"));
   if (rc != 1) {
      wp.addJSON("db_idno", "");
      wp.addJSON("db_chi_name", "");
      wp.addJSON("db_bir_date", "");
      wp.addJSON("vd_flag", "");
      wp.addJSON("tel_no1", "");
      wp.addJSON("tel_no2", "");
      wp.addJSON("tel_no3", "");
      wp.addJSON("bill_addr", "");
      wp.addJSON("new_end_date", "");
      wp.addJSON("group_code", "");
      wp.addJSON("id_p_seqno", "");
      return;
   }
   wp.addJSON("db_idno", sqlStr("db_idno"));
   wp.addJSON("db_chi_name", sqlStr("db_chi_name"));
   wp.addJSON("db_bir_date", sqlStr("db_bir_date"));
   wp.addJSON("vd_flag", sqlStr("vd_flag"));
   wp.addJSON("tel_no1", sqlStr("tel_no1"));
   wp.addJSON("tel_no2", sqlStr("tel_no2"));
   wp.addJSON("tel_no3", sqlStr("tel_no3"));
   wp.addJSON("bill_addr", sqlStr("bill_addr"));
   wp.addJSON("new_end_date", commString.mid(sqlStr("new_end_date"), 0, 6));
   wp.addJSON("group_code", sqlStr("group_code"));
   wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
}

void selectUserName()   {
   String sql1 = " select usr_cname from sec_user where usr_id = ? ";
   sqlSelect(sql1, new Object[]{wp.loginUser});

   if (sqlRowNum <= 0) return;

   is_user_name = sqlStr("usr_cname");
}

}

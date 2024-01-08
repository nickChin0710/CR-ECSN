package rskm03;
/**
 * 2020-0610   JH    modify: idno_name
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Rskm3250 extends BaseAction {
String kk1 = "";

@Override
public void userAction() throws Exception {
   strAction =wp.buttonCode;
   switch (wp.buttonCode) {
      case "L":
         strAction = "";
         clearFunc(); break;
      case "X":
         strAction = "new";
         clearFunc(); break;
      case "Q":
         queryFunc(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "R":
         dataRead(); break;
      case "A":
      case "U":
      case "D":
         saveFunc(); break;
      case "C":
         procFunc(); break;
//      case "PDF":
//         pdfPrint(); break;
//      case "XLS":  //-Excel-
//         strAction = "XLS";
//         xlsPrint(); break;
      case "AJAX":
    	 if(wp.itemEq("idCode", "1")) {
    		 wf_ajax_cardno(wp);
    	 }	else if(wp.itemEq("idCode","2")) {
    		 wf_ajax_idno(wp);
    	 }
    	 break;
      default:
         alertErr("未指定 actionCode 執行功能, action[%s]",wp.buttonCode);
   }
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskm3250_detl")) {
         wp.optionKey = wp.colStr(0, "proc_user");
         dddwList("dddw_rskid_desc2", "ptr_sys_idtab", "wf_desc", "wf_desc", "where wf_type='CB-USER'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (eqIgno(wp.respHtml, "rskm3250")) {
         wp.optionKey = wp.colStr(0, "ex_proc_user");
         dddwList("dddw_rskid_desc2", "ptr_sys_idtab", "wf_desc", "wf_desc", "where wf_type='CB-USER'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (eqIgno(wp.respHtml, "rskm3250_detl")) {
         wp.optionKey = wp.colStr(0, "ddlb_source_type");
         dddwList("d_dddw_idtab3", "ptr_sys_idtab", "wf_desc", "wf_desc", "where wf_type='CTFC_MAST_SOURCE'");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   if (!empty(wp.itemStr("ex_idno"))) {
      if (wp.itemStr("ex_idno").length() <= 8) {
         alertErr("ID 輸入錯誤 !");
         return;
      }
   }

   if (!empty(wp.itemStr("ex_card_no"))) {
      if (wp.itemStr("ex_card_no").length() < 6) {
         alertErr("卡號 至少 6 碼");
         return;
      }
   }


   String ls_where = " where 1=1 "
         + sqlCol(wp.itemStr("ex_idno"), "idno", "like%")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
         + sqlCol(wp.itemStr("ex_assign_date"), "assign_date", ">=")
         + sqlCol(wp.itemStr("ex_proc_user"), "proc_user", "like%");

   wp.whereStr = ls_where;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " case_no, idno ,"
         + " assign_date ,"
         + " card_no ,"
         + " proc_user ,"
         + " source_type ,"
         + " uf_ctfc_idname(card_no) as db_idno_name "
   ;
   wp.daoTable = "rsk_ctfc_mast";
   wp.whereOrder = " order by case_no ";
   pageQuery();

   wp.setListCount(1);
//		queryAfter();
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();

}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   dataRead();

}

@Override
public void dataRead() throws Exception {
   if (empty(kk1))
      kk1 = itemkk("case_no");
   if (empty(kk1)) {
      alertErr("案件流水號: 不可空白");
      return;
   }

   wp.selectSQL = ""
         + " case_no ,"
         + " idno ,"
         + " select_flag ,"
         + " assign_date ,"
         + " card_no ,"
         + " proc_user ,"
         + " source_type ,"
         + " mod_seqno ,"
         + " hex(rowid) as rowid ,"
         + " uf_ctfc_idname(card_no) as idno_name ,"
         +" uf_idno_name2(idno,'') as idno_name2,"
         + " to_char(mod_time,'yyyymmdd') as mod_date ,"
         + " mod_user "
   ;
   wp.daoTable = "rsk_ctfc_mast";
   wp.whereStr = "where case_no =?";

   setString(1,kk1);
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
   }
   if (wp.colEmpty("idno_name")) {
      wp.colSet("idno_name",wp.colStr("idno_name2"));
   }
}


@Override
public void saveFunc() throws Exception {
   Rskm3250Func func = new Rskm3250Func();
   func.setConn(wp);
   rc = func.dbSave(strAction);
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
      return;
   }
   if (this.isAdd()) {
      alertMsg("Case No:" + func.is_case_no);
   }
   this.saveAfter(false);
}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initButton() {
   this.btnModeAud();

}

@Override
public void initPage() {
   if (wp.respHtml.indexOf("_detl")>0) {
      wp.colSet("assign_date",wp.sysDate);
   }
}

public void wf_ajax_cardno(TarokoCommon wr) throws Exception {
   super.wp = wr;

   // String ls_winid =
   select_Vcard_idno(wp.itemStr("ax_card_no"));
   if (rc != 1) {
      wp.addJSON("id_no", "");
      wp.addJSON("db_idno_name", "");
      wp.addJSON("max_assign_date", "");
      return;
   }
   wp.addJSON("id_no", sqlStr("id_no"));
   if (empty(sqlStr("db_ctft_idname"))) {
      wp.addJSON("db_idno_name", sqlStr("db_idno_name"));
   }
   else {
      wp.addJSON("db_idno_name", sqlStr("db_ctft_idname"));
   }
   if (empty(sqlStr("max_assign_date"))) {
   	wp.addJSON("max_assign_date", "");
   } else {
   	wp.addJSON("max_assign_date", sqlStr("max_assign_date"));
   }
}

void select_Vcard_idno(String s1) throws Exception  {
   String ls_sql = " select id_no, chi_name as db_idno_name "
         +", uf_ctfc_idname(card_no) as db_ctft_idname"
         + " from Vcard_idno"
         + " where card_no =?";
   this.sqlSelect(ls_sql, s1);
   if (sqlRowNum <= 0) {
      alertErr("非本行卡號");
      return ;
   }
   
   String sql2 = "select max(assign_date) as max_assign_date from rsk_ctfc_mast where card_no = ? ";
   sqlSelect(sql2,new Object[]{s1});
   if(sqlRowNum ==0) {
   	rc =1; 
   }
   
   return;
}

public void wf_ajax_idno(TarokoCommon wr) throws Exception {
   super.wp = wr;

   // String ls_winid =
   select_crd_idno(wp.itemStr("ax_id_no").toUpperCase());
   if (rc != 1) {
      wp.addJSON("db_idno_name", "");
      return;
   }
   wp.addJSON("db_idno_name", sqlStr("db_idno_name"));
}
void select_crd_idno(String s1) throws Exception  {
   String ls_sql = "select chi_name as db_idno_name "
         + " from Vcard_idno"
         + " where id_no =? "
         +commSqlStr.rownum(1)
         ;

   this.sqlSelect(ls_sql, s1);
   if (sqlRowNum <= 0) {
      alertErr("查無 持卡人姓名");
   }
   return;
}

}

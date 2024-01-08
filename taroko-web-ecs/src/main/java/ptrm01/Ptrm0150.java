/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-10-20  v1.00.00  Zuwei Su   sync from mega, update coding standard                     *
******************************************************************************/
package ptrm01;

import ofcapp.BaseEdit;
import taroko.base.CommSqlStr;
import taroko.com.TarokoCommon;

public class Ptrm0150 extends BaseEdit {
Ptrm0150Func func;
CommSqlStr commSqlStr = new CommSqlStr();
String acctCode = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
   //ddd("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) {
      //-資料讀取-
      strAction = "R";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page*/
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }

   dddwSelect();
   initButton();
}

@Override
public void queryFunc() throws Exception {
   wp.whereStr = " where 1=1"
           + sqlCol(wp.itemStr("ex_acct_code"), "acct_code", ">=")
   ;
   wp.whereOrder = " order by acct_code ";
   if (!wp.itemEq("ex_int_method", "0"))
      wp.whereStr += commSqlStr.col(wp.itemStr("ex_int_method"), "interest_method");
   if (!wp.itemEq("ex_part_rev", "0"))
      wp.whereStr += commSqlStr.col(wp.itemStr("ex_part_rev"), "part_rev");
	if (!wp.itemEq("ex_revolve", "0"))
		wp.whereStr += commSqlStr.col(wp.itemStr("ex_revolve"), "revolve");

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "acct_code, "
           + "chi_long_name,"
           + "eng_long_name, "
           + " interest_method, part_rev, revolve, "
           + "mod_user,"
           + "to_char(mod_time,'yyyymmdd') as mod_date, "
           + "acct_code_flag "
   ;
   wp.daoTable = "ptr_actcode";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }
   pageQuery();

   wp.setListCount(1);
   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   wp.totalRows = wp.dataCnt;
   wp.listCount[1] = wp.dataCnt;
   wp.setPageValue();

}

@Override
public void querySelect() throws Exception {
   acctCode = wp.itemStr("data_k1");
   dataRead();


}

@Override
public void dataRead() throws Exception {
   if (empty(acctCode)) {
      acctCode = itemKk("acct_code");
   }
   wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
           + "acct_code,   "
           + "chi_short_name, "
           + "chi_long_name, "
           + "eng_short_name, "
           + "eng_long_name,"
           + "item_order_normal,"
           + "item_order_back_date,"
           + "item_order_refund,"
           + "item_class_normal,"
           + "item_class_back_date,"
           + "item_class_refund,"
           + "interest_method,"
           + "inter_rate_code,"
           + "part_rev,"
           + "revolve,"
           + "acct_method,"
           + "urge_1st,"
           + "urge_2st,"
           + "urge_3st,"
           + "occupy,"
           + "receivables,"
           + "inter_rate_code2,"
           + "mod_user,"
           + "to_char(mod_time,'yyyymmdd') as mod_date,"
           + "mod_pgm,"
           + "mod_seqno ,"
           + "acct_code_flag , "
           + "crt_user , "
           + "crt_date "
   ;
   wp.daoTable = "ptr_actcode";
   wp.whereStr = " where 1=1"
           + sqlCol(acctCode, "acct_code");

   this.sqlLog();
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + acctCode);
      return;
   }
}

@Override
public void saveFunc() throws Exception {
   if (this.checkApproveZz() == false) {
      return;
   }
   func = new ptrm01.Ptrm0150Func();
   func.setConn(wp);
   rc = func.dbSave(strAction);
   log(func.getMsg());
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   this.sqlCommit(rc);
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
   }
}

}
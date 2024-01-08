/*
 * 2020-0107  V1.00.01  Alex  dataRead fix
 *
 */
package rskm03;

import ofcapp.BaseAction;

public class Rskm3108 extends BaseAction {
String kk1 = "";

@Override
public void userAction() throws Exception {
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
      // -資料讀取-
      strAction = "R";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
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
   else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   }

}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_case_type"), "case_type", "like%");
   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = " * "
   ;
   wp.daoTable = " rsk_ctfi_casetype ";
   wp.whereOrder = " order by case_type ";
   pageQuery();
   if (sqlRowNum <= 0) {
      alertErr("查無資料");
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
   if (empty(kk1)) kk1 = itemkk("case_type");

   if (empty(kk1)) {
      alertErr("案件類型不可空白");
      return;
   }

   wp.selectSQL = " A.* , "
         + " hex(A.rowid) as rowid "
   ;
   wp.daoTable = "rsk_ctfi_casetype A";
   wp.whereStr = " where 1=1 "
         + sqlCol(kk1, "A.case_type")
   ;
   pageSelect();
   if (sqlRowNum <= 0) {
      alertErr("查無資料 , case_type = " + kk1);
      return;
   }

}

@Override
public void saveFunc() throws Exception {
   Rskm3108Func func = new Rskm3108Func();
   func.setConn(wp);
   rc = func.dbSave(strAction);
   sqlCommit(rc);
   if (rc <= 0) {
      errmsg(func.getMsg());
      return;
   }
   else this.saveAfter(false);

}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initButton() {
   if (eqIgno(wp.respHtml, "rskm3108_detl")) {
      btnModeAud();
   }

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

}

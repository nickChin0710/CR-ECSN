/*
 * 2020-0108  V1.00.01  Alex  add cond
 *
 *
 */
package rskm03;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Rskm3105 extends BaseEdit {
Rskm3105Func func;
String kk1 = "", kk2 = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   this.strAction = wp.buttonCode;
   //ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
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
         + sqlCol(wp.itemStr("ex_area_code"), "area_code", "like%")
         + sqlCol(wp.itemStr("ex_cntry_code"), "cntry_code", "like%")
         + sqlCol(wp.itemStr("ex_area_remark"), "area_remark", "%like%")
   ;


   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "area_code, "
         + "cntry_code, "
         + "area_remark, "
         + "to_char(mod_time,'yyyymmdd') as mod_date, "
         + "mod_user "
   ;
   wp.daoTable = "rsk_ctfi_area";
   wp.whereOrder = " order by area_code ";
   pageQuery();

   wp.setListCount(1);
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
      kk1 = itemKk("area_code");
   }
   wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
         + "area_code , "
         + "cntry_code , "
         + "area_remark , "
         + "mod_user , "
         + "to_char(mod_time,'yyyymmdd') as mod_date , "
         + "crt_date , "
         + "crt_user "

   ;
   wp.daoTable = "rsk_ctfi_area";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "area_code");
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }
}

@Override
public void saveFunc() throws Exception {
   func = new Rskm3105Func(wp);
   rc = func.dbSave(strAction);
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

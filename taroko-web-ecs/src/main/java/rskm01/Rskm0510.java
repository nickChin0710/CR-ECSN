package rskm01;
/*風管作業間隔天數參數維護 V.2018-0118
 * 2018-0118:	JH		modify
 *
 * */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Rskm0510 extends BaseEdit {

Rskm0510Func func;
String kk1 = "", kk2 = "", kk3 = "NC";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
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
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
   }
}

@Override
public void dataRead() throws Exception {
   if (isEmpty(kk1)) {
      kk1 = itemKk("bin_type");
   }
   if (isEmpty(kk2)) {
      kk2 = itemKk("trans_type");
   }

   if (isEmpty(kk1) || isEmpty(kk2)) {
      alertErr("[卡別, 交易類別] 不可空白");
      return;
   }

   wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
         + " bin_type , "
         + " trans_type , "
         + " return_day , "
         + " fst_cb_day , "
         + " represent_day , "
         + " sec_cb_day , "
         + " pre_arbit_day , "
         + " pre_comp_day,"
         + " warn_day ,"
         + " mod_user , "
         + " to_char(mod_time,'yyyymmdd') as mod_date , "
         + " pre_comp_day2 "
   ;
   wp.daoTable = "ptr_rskinterval";
   wp.whereStr = "where 1=1"
         + sqlCol(kk1, "bin_type")
         + sqlCol(kk2, "trans_type")
         + sqlCol(kk3, "acq_type");
   pageSelect();
   if (sqlRowNum <= 0) {
      alertErr("查無資料, key=" + kk1 + "; " + kk2 + "; " + kk3);
      return;
   }
   detlWkdata();
}

void detlWkdata() {
	/*${trans_type_1}>1.一般消費</option>
      <option value="2" ${trans_type_2}>2.預借現金</option>*/
   String ss = commString.decode(wp.colStr("trans_type"), "1,2", "一般消費,預借現金");
   wp.colSet("tt_trans_type", ss);
}

@Override
public void saveFunc() throws Exception  {
   func = new Rskm0510Func();
   func.setConn(wp.getConn());

   if (this.checkApproveZz() == false) {
      return;
   }

   String[] col = new String[]{
         "bin_type",
         "trans_type",
         "return_day",
         "fst_cb_day",
         "represent_day",
         "sec_cb_day",
         "pre_arbit_day",
         "pre_comp_day",
         "pre_comp_day2"
   };
   for (String col1 : col) {
      func.varsSet(col1, wp.itemStr(col1));
   }
   if (eqIgno(strAction, "A")) {
      func.varsSet("bin_type", wp.itemStr("kk_bin_type"));
      func.varsSet("trans_type", wp.itemStr("kk_trans_type"));
   }
   //-default-
   func.varsSet("acq_type", "NC");
   func.setModxxx(loginUser(), "rskm0510", wp.itemStr("mod_seqno"));

   rc = func.dbSave(strAction);
   if (rc != 1) {
      alertErr("Insert/Update ptr_rskinterval error:" + func.getMsg());
   }
   sqlCommit(rc);
}

@Override
public void queryFunc() throws Exception {
   String lsBinType = wp.itemStr("ex_card_type");
   String lsTransType = wp.itemStr("ex_trans_type");

   wp.whereStr = "WHERE 1=1"
         + sqlCol(lsBinType, "bin_type")
         + sqlCol(lsTransType, "trans_type")
         + " and acq_type ='NC'"
         + " order by bin_type, trans_type";

   //-page control-
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " bin_type , "
         + " trans_type , "
         + " acq_type , "
         + " return_day , "
         + " fst_cb_day , "
         + " represent_day , "
         + " sec_cb_day , "
         + " pre_arbit_day , "
         + " pre_comp_day, "
         + " warn_day , "
         + " pre_comp_day2 "
   ;
   wp.daoTable = "ptr_rskinterval";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }

   pageQuery();
   listWkdata();

   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.totalRows = wp.dataCnt;
   wp.setPageValue();
}

void listWkdata() {
   String ss = "";

   //trans_type:  .  1.1.一般消費 2.2.預借現金
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      ss = wp.colStr(ii, "trans_type");
      wp.colSet(ii, "tt_trans_type", commString.decode(ss, "1,2", "一般消費,預借現金"));
   }
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");

   dataRead();
}

}

package rskm02;
/** rskm1230;商務卡分群參數指定維護
 * 2020-1008   JH    modify
 * 2020-0203	JH		group_cnt[_assgn]
 * V00.0		JH		2017-0803: initial
 * V00.1    Alex  2018-08-03
 */

import ofcapp.BaseAction;
import taroko.base.CommDate;

public class Rskm1230 extends BaseAction {
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
   try {
      if (eqIgno(wp.respHtml, "rskm1230_detl")) {
         wp.optionKey = wp.colStr(0, "corp_jcic_no");
         dddwList("dddw_corp_jcic_no", "col_jcic_query_mast", "jcic_no", "jcic_no||'_'||contract_desc", "where 1=1");

         wp.optionKey = wp.colStr(0, "idno_jcic_no");
         dddwList("dddw_idno_jcic_no", "col_jcic_query_mast", "jcic_no", "jcic_no||'_'||contract_desc", "where 1=1");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   String ls_where =
         " where 1=1 "
               + sqlCol(wp.itemStr("ex_group_date1"), "group_date1", ">=")
               + sqlCol(wp.itemStr("ex_batch_no"), "batch_no", "like%")
               + sqlCol(wp.itemStr("ex_group_date2"), "group_date2", ">=");

   wp.whereStr = ls_where;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " batch_no ,"
         + " batch_desc ,"
         + " data_yymm ,"
         + " group_date1 ,"
         + " crt_user1 ,"
         + " group_proc_date1 ,"
         + " group_date2 ,"
         + " group_proc_date2";
   wp.daoTable = "rsk_trcorp_mast ";
   wp.whereOrder = " order by batch_no Desc ";
   pageQuery();

   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setListCount(1);
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
      kk1 = itemkk("batch_no");

   if (empty(kk1)) {
      alertErr("覆審批號:不可空白");
      return;
   }

   wp.selectSQL = ""
         + " batch_no ,"
         + " batch_desc ,"
         + " data_yymm ,"
         + " trial_status ,"
         + " group_date1 ,"
         + " crt_user1 ,"
         + " apr_user1 ,"
         + " group_proc_date1 ,"
         + " jcic_query_date ,"
         + " jcic_proc_date ,"
         + " group_date2 ,"
         + " crt_user2 ,"
         + " apr_user2 ,"
         + " group_proc_date2 ,"
         + " mod_user ,"
         + " to_char(mod_time,'yyyymmdd') as mod_date ,"
         + " mod_pgm ,"
         + " mod_seqno ,"
         + " hex(rowid) as rowid ,"
         + " '0' as db_group1 ,"
         + " '0' as db_group2 , "
         + " corp_jcic_no , "
         + " idno_jcic_no ";
   wp.daoTable = "rsk_trcorp_mast";
   wp.whereStr = " where 1=1" + sqlCol(kk1, "batch_no");
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }

   selectDetl1();
   selectDetl2();
}

void selectDetl1() throws Exception {
   this.selectNoLimit();
   // wp.sel.varRows = 99999;
   daoTid = "A-";
   wp.daoTable = "rsk_trcorp_parm A ";
   if (eqIgno(strAction, "new") || empty(kk1)) {
      wp.selectSQL =
            "A.risk_group "
                  + ", A.risk_group_desc "
                  + ", (select 'Y' from rsk_trcorp_mast_group B where B.batch_no='' "
                  + " and B.risk_group=A.risk_group and B.group_type='1') as db_opt1"
      ;
   }
   else {
      wp.selectSQL =
            "A.risk_group "
                  + ", A.risk_group_desc "
                  + ", (select 'Y' from rsk_trcorp_mast_group B where B.group_type='1'"
                  + sqlCol(kk1, "B.batch_no")+" and B.risk_group=A.risk_group ) as db_opt1"
      ;
   }
   wp.whereStr = "WHERE A.apr_flag='Y'";
   wp.whereOrder = " order by A.risk_group Asc";
   pageQuery();
   wp.setListSernum(0,"A-ser_num",sqlRowNum);
   wp.notFound = "";
   wp.colSet("db_group1_cnt", sqlRowNum);
   for(int ll=0; ll<wp.listCount[0]; ll++) {
      if (wp.colEq(ll,"A-db_opt1","Y")) {
         wp.colSet(ll,"A-opt_on","checked");
      }
   }

}

void selectDetl2() throws Exception {
   this.selectNoLimit();
   // wp.sel.varRows = 99999;
   daoTid = "B-";
   wp.daoTable = "rsk_trcorp_parm A ";
   if (eqIgno(strAction, "new") || empty(kk1)) {
      wp.selectSQL =
            "A.risk_group "
                  + ", A.risk_group_desc "
                  + ", (select 'Y' from rsk_trcorp_mast_group B where B.group_type='2' "
                  + " and B.risk_group=A.risk_group and B.batch_no='') as db_opt2"
      ;
   }
   else {
      wp.selectSQL =
            "A.risk_group "
                  + ", A.risk_group_desc "
                  + ", (select 'Y' from rsk_trcorp_mast_group B where B.group_type='2'"
                  + sqlCol(kk1, "B.batch_no")
                  + " and B.risk_group=A.risk_group ) as db_opt2"
      ;
   }

   wp.whereStr = "WHERE A.apr_flag='Y'";
   wp.whereOrder = " order by A.risk_group Asc";
   pageQuery();
   wp.setListSernum(1,"B-ser_num",sqlRowNum);
   wp.notFound = "";
   wp.colSet("db_group2_cnt", sqlRowNum);

   for(int ll=0; ll<wp.listCount[1]; ll++) {
      if (wp.colEq(ll,"B-db_opt2","Y")) {
         wp.colSet(ll,"B-opt_on","checked");
      }
   }
}

public void ajaxConfirm(taroko.com.TarokoCommon wr) throws Exception {
   wp = wr;

   wp.log("ajax-confirm: batch-No=" + wp.itemStr("ax_batch_no"));
   if (empty(wp.itemStr("group_proc_date1")) == false) {
      wp.addJSON("conf_flag", "Y");
   }
}

//int opt_check(int row_cnt, String col) {
//
//   for (int ll = 0; ll < row_cnt; ll++) {
//      if (empty(wp.itemStr(col + ll))) {
//         continue;
//      }
//      return 1;
//   }
//   return 0;
//}

void keepOptValue() {
   //--
   String[] aaOpt1 =wp.itemBuff("A-opt");
   for (int ii = 0; ii < aaOpt1.length; ii++) {
      int rr=optToIndex(aaOpt1[ii]);
      wp.colSet(rr, "A-opt_on", "checked");
   }
   //--
   String[] aaOpt2 = wp.itemBuff("B-opt");
   for (int ii = 0; ii < aaOpt2.length; ii++) {
      int rr=optToIndex(aaOpt2[ii]);
      wp.colSet(rr, "B-opt_on", "checked");
   }
}

@Override
public void saveFunc() throws Exception {
   Rskm1230Func func = new Rskm1230Func();
   func.setConn(wp);

   String[] aaGroup1 = wp.itemBuff("A-risk_group");
   String[] aaGroup2 = wp.itemBuff("B-risk_group");
   String[] aaOpt1 =wp.itemBuff("A-opt");
   String[] aaOpt2 =wp.itemBuff("B-opt");
   wp.listCount[0] = aaGroup1.length;
   wp.listCount[1] = aaGroup2.length;
   keepOptValue();

   //-group-1-
   if (this.isAdd()) {
      if (wp.iempty("kk_batch_no")) {
         alertErr("覆審批號: 不可空白");
         return;
      }
      if (wp.itemEq("db_group1", "Y") == false) {
         alertErr("未選取維護分群一");
         return;
      }
   }
   if (this.isAdd() || this.isUpdate()) {
      if (optToIndex(aaOpt1[0]) <0) {
         alertErr("未指定分群1");
         return;
      }
      //-group-2-
      if (wp.itemEq("db_group2", "Y")) {
         if (optToIndex(aaOpt2[0]) <0) {
            alertErr("未指定分群2");
            return;
         }
      }
   }

   if (checkApproveZz() == false) {
      return;
   }

   rc = func.dbSave(strAction);
   if (rc==1) insertDetl1();
   if (rc==1) insertDetl2();
   this.sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
      return;
   }

   this.saveAfter(true);

}

void insertDetl1() throws Exception  {
   Rskm1230Func func = new Rskm1230Func();
   func.setConn(wp);
   if (func.deleteGroup1() < 0) {
      alertErr(func.getMsg());
      return;
   }

   if (wp.itemEq("db_group1", "Y") == false) {
      return;
   }
   int llErr = 0;

   String[] aaGroup = wp.itemBuff("A-risk_group");
   String[] aaOpt = wp.itemBuff("A-opt");

   for (int ll = 0; ll < aaOpt.length; ll++) {
      int rr=optToIndex(aaOpt[ll]);
      if (rr <0) continue;
      //-option-ON-
      func.varsSet("risk_group1", aaGroup[rr]);
      if (func.insertGroup1() == 1) {
         continue;
      }
      //-error-
      llErr++;
      break;
   }
   if (llErr>0) {
      alertErr("第一次分群條件: 新增錯誤");
   }
}

void insertDetl2() throws Exception  {
   Rskm1230Func func = new Rskm1230Func();
   func.setConn(wp);
   if (func.deleteGroup2() < 0) {
      alertErr(func.getMsg());
      return;
   }

   if (wp.itemEq("db_group2", "Y") == false) {
      return;
   }
   int llErr = 0;

   String[] aaGroup = wp.itemBuff("B-risk_group");
   String[] aaOpt = wp.itemBuff("B-opt");

   for (int ll = 0; ll < aaOpt.length; ll++) {
      int rr =optToIndex(aaOpt[ll]);
      if (rr<0) continue;
      //-option-ON-
      func.varsSet("risk_group2", aaGroup[rr]);

      if (func.insertGroup2() != 1) {
         llErr++;
         break;
      }
   }
   if (llErr>0) {
      alertErr("第二次分群條件: 新增錯誤");
   }
}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
   }
}

@Override
public void initPage() {
   if (wp.respHtml.indexOf("1230_detl")>0) {
      taroko.base.CommDate zzdate=new CommDate();
      String ss=busiDate();
      ss =commString.left(zzdate.dateAdd(ss,0,-1,0),6);
      wp.colSet("data_yymm",ss);

      wp.colSet("corp_jcic_no", "609");
      wp.colSet("idno_jcic_no", "610");
      try {
         selectDetl1();
         selectDetl2();
      }
      catch (Exception ex) {
         wp.log("initPage.err "+ex.getMessage());
      }
   }

}


}

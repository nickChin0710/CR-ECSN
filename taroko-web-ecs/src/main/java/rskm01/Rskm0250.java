package rskm01;
/**
 * 2020-0515   JH    UAT.modify
 * 2019-1211   JH    UAT
 * 預備仲裁/仲裁維護
 */

import ofcapp.BaseAction;

public class Rskm0250 extends BaseAction {
String kk1 = "";

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面--
         strAction = "new";
         if (empty(wp.itemStr("ex_bin_type"))) {
            alertErr("卡別 : 不可空白");
            return;
         }

         if (!empty(wp.itemStr("ex_ctrl_seqno"))) {
            selectData1();
            BilBill bil = new BilBill();
            bil.setConn(wp);
            bil.dataSelect(wp.colStr("reference_no"), wp.colStr("debit_flag"));
         }

         if (eqIgno(wp.itemStr("ex_bin_type"), "V")) {
            wp.respHtml = "rskm0250_detl_visa";
         }
         else {
            wp.respHtml = "rskm0250_detl_mj";
         }
         break;
      case "Q" ://查詢功能--
         queryFunc(); break;
      case "R": //資料讀取--
         dataRead(); break;
      case "M": //瀏覽功能 :skip-page-
         queryRead();
         break;
      case "S": //動態查詢--
         querySelect();
         break;
      case "L": //清畫面--
         strAction = "";
         clearFunc();
         break;
      case "C": // -資料處理-
         procFunc();
         break;
      case "A": //新增功能--
      case "D": //刪除功能--
         saveFunc(); break;
      case "U": //-更新功能-
         doUpdate1(); break;
      case "U2": //--
         strAction = "U";
         doUpdate2(); break;
      case "D2": //--
         strAction = "U";
         doDelete2(); break;
   }
}

void selectData1() throws Exception  {
   wp.colSet("bin_type", wp.itemStr("ex_bin_type"));
   wp.colSet("ctrl_seqno", wp.itemStr("ex_ctrl_seqno"));
   String sql1 = " select reference_no , debit_flag from rsk_ctrlseqno_log "
         + " where ctrl_seqno = ? ";

   sqlSelect(sql1, new Object[]{wp.itemStr("ex_ctrl_seqno")});

   if (sqlRowNum <= 0) {
      errmsg("控制流水號輸入錯誤");
      return;
   }
   wp.colSet("debit_flag", sqlStr("debit_flag"));
   wp.colSet("reference_no", sqlStr("reference_no"));
   if (eqIgno(sqlStr("debit_flag"), "Y")) {
      String sql2 = " select "
            + " bin_type ,"
            + " card_no ,"
            + " mcht_country ,"
            + " acct_month ,"
            + " purchase_date ,"
            + " film_no ,"
            + " auth_code ,"
            + " source_amt ,"
            + " source_curr "
            + " from dbb_bill "
            + " where reference_no = ? ";

      sqlSelect(sql2, new Object[]{sqlStr("reference_no")});

      if (sqlRowNum <= 0) {
         errmsg("查無資料");
         return;
      }

      wp.colSet("card", sqlStr("card_no"));
      wp.colSet("mcht_country", sqlStr("mcht_country"));
      wp.colSet("acct_month", sqlStr("acct_month"));
      wp.colSet("purchase_date", sqlStr("purchase_date"));
      wp.colSet("film_no", sqlStr("film_no"));
      wp.colSet("auth_code", sqlStr("auth_code"));
      wp.colSet("source_amt", sqlStr("source_amt"));
      wp.colSet("source_curr", sqlStr("source_curr"));
      wp.colSet("dest_amt", sqlStr("dest_amt"));
      wp.colSet("settl_amt", sqlStr("settl_amt"));

   }
   else if (eqIgno(sqlStr("debit_flag"), "N")) {
      String sql3 = " select "
            + " bin_type ,"
            + " card_no ,"
            + " mcht_country ,"
            + " acct_month ,"
            + " purchase_date ,"
            + " film_no ,"
            + " auth_code ,"
            + " source_amt ,"
            + " source_curr ,"
            + " dc_dest_amt ,"
            + " curr_code ,"
            + " dest_amt ,"
            + " settl_amt "
            + " from bil_bill"
            + " where reference_no = ? ";

      sqlSelect(sql3, new Object[]{
            sqlStr("reference_no")
      });

      if (sqlRowNum <= 0) {
         errmsg("查無資料");
         return;
      }

      wp.colSet("card_no", sqlStr("card_no"));
      wp.colSet("mcht_country", sqlStr("mcht_country"));
      wp.colSet("acct_month", sqlStr("acct_month"));
      wp.colSet("purchase_date", sqlStr("purchase_date"));
      wp.colSet("film_no", sqlStr("film_no"));
      wp.colSet("auth_code", sqlStr("auth_code"));
      wp.colSet("source_amt", sqlStr("source_amt"));
      wp.colSet("source_curr", sqlStr("source_curr"));
      wp.colSet("dc_dest_amt", sqlStr("dc_dest_amt"));
      wp.colSet("curr_code", sqlStr("curr_code"));
      wp.colSet("dest_amt", sqlStr("dest_amt"));
      wp.colSet("settl_amt", sqlStr("settl_amt"));

   }

}

@Override
public void dddwSelect() {
   if (wp.respHtml.indexOf("_detl_visa")>0) {
      wp.optionKey =wp.colStr("pre_result");
      wp.colSet("ddlb_pre_result",ecsfunc.DeCodeRsk.arbitPreResult(wp.optionKey,true));
      wp.optionKey =wp.colStr("arb_result");
      wp.colSet("ddlb_arb_result",ecsfunc.DeCodeRsk.arbitCloResult(wp.optionKey,true));
   }
   if (wp.respHtml.indexOf("_detl_mj")>0) {
      wp.optionKey =wp.colStr("pre_result");
      wp.colSet("ddlb_pre_result",ecsfunc.DeCodeRsk.arbitPreResult(wp.optionKey,true));
      wp.optionKey =wp.colStr("arb_result");
      wp.colSet("ddlb_arb_result",ecsfunc.DeCodeRsk.arbitCloResult(wp.optionKey,true));
   }

}

@Override
public void queryFunc() throws Exception {
   String ls_where = " where 1=1 "
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no")
         + sqlCol(wp.itemStr("ex_bin_type"), "bin_type");

   if (!empty(wp.itemStr("ex_user_id"))) {
      ls_where += " and (pre_add_user ='"
            + wp.itemStr("ex_user_id")
            + "' or arb_add_user='"
            + wp.itemStr("ex_user_id")
            + "') ";
   }

   wp.whereStr = ls_where;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + "  ctrl_seqno, bin_type"
         + ", card_no"
         + ", purchase_date "
         + ", mcht_country"
         + ", film_no"
         + ", uf_dc_curr(curr_code) as curr_code "
         + ", uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt "
         + ", arbit_times, PRE_ALLOC_CODE"
         + ", pre_event_date "
         + ", pre_apply_date "
         + ", pre_result"
         + ", pre_add_user"
         + ", arb_apply_date "
         + ", arb_result"
         + ", arb_add_user"
         + ", debit_flag"
         +", decode(pre_alloc_code,'1','10/11','2','12/13',pre_alloc_code) as tt_alloc_code"
   ;
   wp.daoTable = "rsk_prearbit";
   wp.whereOrder = " order by ctrl_seqno, reference_no ";

   pageQuery();
   wp.setListCount(1);
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
   if (empty(kk1)) {
      kk1 = itemkk("ctrl_seqno");
   }

   wp.selectSQL = "hex(rowid) as rowid ,"
         + " A.* "
         + ", uf_dc_curr(A.curr_code) as curr_code "
         + ", uf_dc_amt2(A.dest_amt,A.dc_dest_amt) as dc_dest_amt "
         + ", A.pre_apply_date as pre_apply_date2  "
   ;
   wp.daoTable = "rsk_prearbit A ";
   wp.whereStr = "where reference_seq=0" + sqlCol(kk1, "ctrl_seqno");
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
   }

   BilBill bil = new BilBill();
   bil.setConn(wp);
   bil.dataSelect(wp.colStr("reference_no"), wp.colStr("debit_flag"));

}

@Override
public void saveFunc() throws Exception {
   rskm01.Rskm0250Func func = new rskm01.Rskm0250Func();
   func.setConn(wp);
   rc = func.dbSave(strAction);
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else saveAfter(false);
}

void doUpdate1() throws Exception {
   rskm01.Rskm0250Func func = new rskm01.Rskm0250Func();
   func.setConn(wp);
   rc = func.updateU1();
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else {
      alertMsg("預備仲裁存檔成功");
      saveAfter(true);
   }

}

void doUpdate2() throws Exception {
   rskm01.Rskm0250Func func = new rskm01.Rskm0250Func();
   func.setConn(wp);
   rc = func.updateU2();
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else {
      alertMsg("仲裁存檔成功");
      saveAfter(true);
   }

}

void doDelete2() throws Exception {
   rskm01.Rskm0250Func func = new rskm01.Rskm0250Func();
   func.setConn(wp);
   rc = func.deleteD2();
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else {
      alertMsg("仲裁刪除成功");
      saveAfter(true);
   }
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
   // TODO Auto-generated method stub

}

}

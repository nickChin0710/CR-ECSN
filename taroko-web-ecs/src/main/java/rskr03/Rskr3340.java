package rskr03;
/**
 * 2022-0314   JH    U-1835
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Rskr3340 extends BaseAction implements InfacePdf, InfaceExcel {

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
   else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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
   else if (eqIgno(wp.buttonCode, "XLS1")) {   //-初次陳報-
      strAction = "XLS1";
      xlsPrint();
   }
   else if (eqIgno(wp.buttonCode, "XLS2")) {   //-後續陳報-
      strAction = "XLS2";
      xlsPrint_2();
   }
   else if (eqIgno(wp.buttonCode, "PDF")) {   //-PDF-
      strAction = "PDF";
      pdfPrint();
   }

}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (chkStrend(wp.itemStr("ex_case_date1"), wp.itemStr("ex_case_date2")) == false) {
      alertErr("立案日期 :起迄輸入錯誤");
      return;
   }

   if (chkStrend(wp.itemStr("ex_debit_date1"), wp.itemStr("ex_debit_date2")) == false) {
      alertErr("帳卡登錄日 :起迄輸入錯誤");
      return;
   }

   if (eqIgno(strAction, "XLS1")) {
      if (wp.itemEmpty("ex_case_date1") || wp.itemEmpty("ex_case_date2")) {
         alertErr("列印初次陳報時立案日期不可空白 !");
         return;
      }
   }

   if (eqIgno(strAction, "XLS2")) {
      if (wp.itemEmpty("ex_debit_date1") || wp.itemEmpty("ex_debit_date2")) {
         alertErr("列印後續陳報時帳卡登錄日期不可空白 !");
         return;
      }
   }

   String lsWhere = ""
         + sqlCol(wp.itemStr("ex_case_date1"), "A.case_date", ">=")
         + sqlCol(wp.itemStr("ex_case_date2"), "A.case_date", "<=")
         + sqlCol(wp.itemStr("ex_debit_date1"), "A.debit_card_date", ">=")
         + sqlCol(wp.itemStr("ex_debit_date2"), "A.debit_card_date", "<=");
   if (empty(lsWhere)) {
      alertErr("[立案日期, 帳卡登錄日]: 不可全部空白");
      return;
   }
   lsWhere = "where 1=1" + lsWhere;

   if (wp.itemEq("ex_case_type99", "Y")) {
      lsWhere += " and A.case_source <> '法院起訴或判決' ";
   }
   
   setSqlParmNoClear(true);
   sumData(lsWhere);
   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = ""
         + " uf_idno_id(a.id_p_seqno) as id_no , "
         + " uf_tt_idtab('CTFI_CASE_TYPE2',case_type) as wk_case_type2 ,"
         + " a.case_date , "
         + " a.case_seqno , "
         + " '109-20'||substr(case_seqno,1,4)||'0'||substr(case_seqno,5,3) as ex_case_seqno1 , "
         + " a.card_no , "
         + " a.case_type , "
         + " a.fraud_ok_amt , "
         + " a.miscell_unpay_amt , "
         + " a.recov_cash_amt , "
         + " a.ch_deuct_amt , "
         + " a.for_mcht_amt , "
         + " a.cb_ok_amt , "
         + " a.recov_unpay_amt , "
         + " a.fraud_unpay_amt , "
         + " a.oth_unpay_amt , "
         + " a.debit_card_date , "
         + " b.fra_fir_date , "
         + " a.fraud_area ,"
         + " a.card_no , "
         + " decode(a.fraud_area_code,'',(select area_code from rsk_ctfi_area where area_remark = a.fraud_area),a.fraud_area_code) as fraud_area_code "
   +", A.turn_coll_date"
   ;
   wp.daoTable = " rsk_ctfi_case A left join rsk_ctfi_warn B on A.card_no=B.card_no ";
   wp.whereOrder = " order by case_seqno Asc ";
   pageQuery();

   if (sqlRowNum <= 0) {
      alertErr("查無資料");
      return;
   }
   queryAfter();
   wp.setListCount(0);
   wp.setPageValue();
}

public void sumData(String lsWhere) throws Exception  {
   wp.logSql =false;
   String sql1 = " select "
         + " count(*) as sum_tot_cnt , "
         + " sum(A.fraud_ok_amt) as sum_fra_ok_amt , "
         + " sum(A.miscell_unpay_amt) as sum_recov_amt , "
         + " sum(A.ch_deuct_amt) as sum_ch_amt , "
         + " sum(A.for_mcht_amt) as sum_for_mcht , "
         + " sum(A.cb_ok_amt) as sum_cb_ok , "
         + " sum(A.recov_cash_amt) as sum_recov_cash ,"
         + " sum(A.recov_unpay_amt) as sum_recov_unpay , "
         + " sum(A.fraud_unpay_amt) as sum_unpay_fraud , "
         + " sum(A.oth_unpay_amt) as sum_unpay_oth "
         + " from rsk_ctfi_case A left join rsk_ctfi_warn B on A.card_no = B.card_no "
         + lsWhere;
   sqlSelect(sql1);

   wp.colSet("tl_cnt", sqlStr("sum_tot_cnt"));
   wp.colSet("tl_FOA", sqlStr("sum_fra_ok_amt"));
   wp.colSet("tl_MUA", sqlStr("sum_recov_amt"));
   wp.colSet("tl_CDA", sqlStr("sum_ch_amt"));
   wp.colSet("tl_FMA", sqlStr("sum_for_mcht"));
   wp.colSet("tl_COA", sqlStr("sum_cb_ok"));
   wp.colSet("tl_RCA", sqlStr("sum_recov_cash"));
   wp.colSet("tl_RUA", sqlStr("sum_recov_unpay"));
   wp.colSet("tl_FUA", sqlStr("sum_unpay_fraud"));
   wp.colSet("tl_OUA", sqlStr("sum_unpay_oth"));

}

void queryAfter() throws Exception {
   wp.logSql = false;
   //--初次呈報
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      String sql1 = " select "
            + " event_risk , " // 事件代號
            + " event_desc , " // 是件說明
            + " event_reason , " // 事件發生原因
            + " proc_code1 , " // 處理方案
            + " event_type , " // 業務類別
            + " loss_code , " // 損失內容
            + " loss_ac_no , "
            + " proc_code2 , " // 續報-處理方案
            + " loss_recov_desc " // 損失收回方式
            + " from rsk_ctfi_casetype "
            + " where case_desc = ? ";
      sqlSelect(sql1, new Object[]{wp.colStr(ii, "case_type")});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "event_risk", sqlStr("event_risk"));
         wp.colSet(ii, "event_desc", sqlStr("event_desc"));
         wp.colSet(ii, "event_reason", sqlStr("event_reason"));
         wp.colSet(ii, "proc_code1", sqlStr("proc_code1"));
         wp.colSet(ii, "event_type", sqlStr("event_type"));
         wp.colSet(ii, "loss_code", sqlStr("loss_code"));
         wp.colSet(ii, "loss_ac_no", sqlStr("loss_ac_no"));
         wp.colSet(ii, "proc_code2", sqlStr("proc_code2"));
         wp.colSet(ii, "loss_recov_desc", sqlStr("loss_recov_desc"));
      }

      //--掛帳科目 fraud_ok_amt
      wp.colSet(ii, "wk_loss_ac_no", select_lost_ac_no(wp.colStr(ii, "case_type"), wp.colNum(ii, "fraud_ok_amt")));
   }

   //--後續呈報
   for (int rr = 0; rr < wp.selectCnt; rr++) {

      String sql2 = " select "
            + " actual_amt "   //	實際發生金額
            + " from rsk_ctfi_case "
            + " where card_no = ? ";
      sqlSelect(sql2, new Object[]{wp.colStr(rr, "card_no")});
      if (sqlRowNum > 0) {
         double li_re_amt = 0;
         //--損失收回金額
         li_re_amt = (int) (sqlInt("actual_amt") - wp.colNum(rr, "miscell_unpay_amt"));
         if (li_re_amt == 0) {
            wp.colSet(rr, "ex_N", "");
            wp.colSet(rr, "li_re_amt", "");
            wp.colSet(rr, "ex_M", "");
         }
         else {
            wp.colSet(rr, "ex_N", "TWD");
            wp.colSet(rr, "li_re_amt", "" + li_re_amt);
            wp.colSet(rr, "ex_M", wp.colStr(rr, "loss_recov_desc"));
         }

         if (li_re_amt < 0) {
            wp.colSet(rr, "ex_P", "");
         }
         else if (li_re_amt > 0) {
            wp.colSet(rr, "ex_P", wp.colStr(rr, "debit_card_date"));
         }

         //--後續處理方案或改善措施
         wp.colSet(rr, "ex_D", wp.colStr(rr, "proc_code2"));
         //--損失收回方式


         //--損失金額 :實際損失金額 - 初次陳報金額 + 損失收回金額
         double li_lost_amt = 0;
         li_lost_amt = (wp.colNum(rr, "miscell_unpay_amt") - wp.colNum(rr, "fraud_ok_amt") + wp.colNum(rr, "li_re_amt"));
         wp.colSet(rr, "lost_amt", li_lost_amt);
         wp.colSet(rr, "wk_loss_ac_no2", select_lost_ac_no(wp.colStr(rr, "case_type"), wp.colNum(rr, "lost_amt")));
      }

   }

}

String select_lost_ac_no(String ls_case_type, Double ld_amount) throws Exception  {

   String sql1 = " select loss_flag1 , loss_flag2 , loss_amt1 , loss_amt2 , loss_ac_no1 , loss_ac_no2 "
         + " from rsk_ctfi_casetype where case_desc = ? ";
   log("sql1:" + sql1);
   log("case_type:" + ls_case_type);
   sqlSelect(sql1, new Object[]{ls_case_type});
   if (sqlRowNum <= 0) return "未設定案件類別";

   if (empty(sqlStr("loss_flag1")) == false) {
      if (eqIgno(sqlStr("loss_flag1"), "1")) {
         if (ld_amount > sqlNum("loss_amt1")) return sqlStr("loss_ac_no1");
      }
      else if (eqIgno(sqlStr("loss_flag1"), "2")) {
         if (ld_amount >= sqlNum("loss_amt1")) return sqlStr("loss_ac_no1");
      }
      else if (eqIgno(sqlStr("loss_flag1"), "3")) {
         if (ld_amount == sqlNum("loss_amt1")) return sqlStr("loss_ac_no1");
      }
      else if (eqIgno(sqlStr("loss_flag1"), "4")) {
         if (ld_amount != sqlNum("loss_amt1")) return sqlStr("loss_ac_no1");
      }
      else if (eqIgno(sqlStr("loss_flag1"), "5")) {
         if (ld_amount < sqlNum("loss_amt1")) return sqlStr("loss_ac_no1");
      }
      else if (eqIgno(sqlStr("loss_flag1"), "6")) {
         if (ld_amount <= sqlNum("loss_amt1")) return sqlStr("loss_ac_no1");
      }
   }

   if (empty(sqlStr("loss_flag2")) == false) {
      if (eqIgno(sqlStr("loss_flag2"), "1")) {
         if (ld_amount > sqlNum("loss_amt2")) return sqlStr("loss_ac_no2");
      }
      else if (eqIgno(sqlStr("loss_flag2"), "2")) {
         if (ld_amount >= sqlNum("loss_amt2")) return sqlStr("loss_ac_no2");
      }
      else if (eqIgno(sqlStr("loss_flag2"), "3")) {
         if (ld_amount == sqlNum("loss_amt2")) return sqlStr("loss_ac_no2");
      }
      else if (eqIgno(sqlStr("loss_flag2"), "4")) {
         if (ld_amount != sqlNum("loss_amt2")) return sqlStr("loss_ac_no2");
      }
      else if (eqIgno(sqlStr("loss_flag2"), "5")) {
         if (ld_amount < sqlNum("loss_amt2")) return sqlStr("loss_ac_no2");
      }
      else if (eqIgno(sqlStr("loss_flag2"), "6")) {
         if (ld_amount <= sqlNum("loss_amt2")) return sqlStr("loss_ac_no2");
      }
   }


   return "不符合掛帳科目條件";
}

@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initButton() {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "Rskr3340";
   wp.pageRows = 9999;
   wp.colSet("user_id", wp.loginUser);

   queryFunc();
   if (wp.listCount[0] == 0 || rc <= 0) {
      alertErr("此條件查無資料");
      wp.respHtml = "TarokoErrorPDF";
      return;
   }
   TarokoPDF pdf = new TarokoPDF();
   pdf.excelTemplate = "rskr3340_1.xlsx";
   wp.fileMode = "Y";
   pdf.pageCount = 28;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

@Override
public void xlsPrint() throws Exception {
   try {
      log("xlsFunction: started--------");

      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "rskr3340_2.xlsx";

      wp.pageRows = 9999;
      queryFunc();
      if (wp.listCount[0] == 0 || rc <= 0) {
         wp.respHtml = "TarokoErrorPDF";
         return;
      }
      wp.colSet("user_id", wp.loginUser);
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
   }
   catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
   }

}

public void xlsPrint_2() throws Exception {
   try {
      log("xlsFunction: started--------");

      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "rskr3340.xlsx";

      wp.pageRows = 9999;
      queryFunc();
      if (wp.listCount[0] == 0 || rc <= 0) {
         wp.respHtml = "TarokoErrorPDF";
         return;
      }
      wp.colSet("user_id", wp.loginUser);
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
   }
   catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
   }

}

@Override
public void logOnlineApprove() throws Exception {
   // TODO Auto-generated method stub

}

}

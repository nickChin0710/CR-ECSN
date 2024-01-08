package rskr03;
/**
 * 2020-0108:  Alex  add dddw
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskr3330 extends BaseQuery {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   //log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
//				strAction="new";
//				clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) { //-資料讀取-
      strAction = "R";
      //         dataRead();
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
public void dddwSelect() {
   try {
      wp.optionKey = wp.colStr("ex_case_source");
      dddwList("dddw_case_source", "ptr_sys_idtab"
            , "wf_desc", "wf_desc", "where wf_type='CTFI_CASE_SOURCE' order by wf_id ");
   }
   catch (Exception ex) {
   }
}

void listWkdata() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "case_close_flag", "Y")) {
         wp.colSet(ii, "db_last_sts", "9");
         wp.colSet(ii, "tt_last_sts", "案件關閉");
      }
      else if (wp.colEq(ii, "survey_close", "Y")) {
         wp.colSet(ii, "db_last_sts", "5");
         wp.colSet(ii, "tt_last_sts", "調查結束");
      }
      else if (wp.colEq(ii, "survey_ing", "Y")) {
         wp.colSet(ii, "db_last_sts", "1");
         wp.colSet(ii, "tt_last_sts", "調查開始");
      }
      else {
         wp.colSet(ii, "db_last_sts", "");
      }
      int mm = (int) wp.colNum(ii, "miscell_unpay_amt");
      if (wp.colEq(ii, "db_last_sts", "9") == false || wp.colEq(ii, "db_last_sts", "") == true) {
         wp.colSet(ii, "wk_case_result", "");
      }
      else {
         if (mm <= 0) {
            wp.colSet(ii, "wk_case_result", "無損結案");
         }
         else {
            wp.colSet(ii, "wk_case_result", "有損結案:" + commString.numFormat(mm, "#,##0") + "元");
         }
      }
   }
}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_case_date1")) || empty(wp.itemStr("ex_case_date2"))) {
      alertErr("立案日期不可空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_case_date1"), wp.itemStr("ex_case_date2")) == false) {
      alertErr("立案日期起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_case_date1"), "A.case_date", ">=")
         + sqlCol(wp.itemStr("ex_case_date2"), "A.case_date", "<=")
         + sqlCol(wp.itemStr("ex_case_source"), "A.case_source");
   
   setSqlParmNoClear(true);
   sumRead(lsWhere);
   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();
   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " A.id_p_seqno as idno ,"
         + " A.case_date ,"
         + " A.case_seqno, "
         + " A.card_no,"
         + " uf_idno_name(A.card_no) as db_chi_name,"
         + " A.case_type,"
         + " A.case_source,"
         + " A.fraud_ok_cnt,"
         + " A.fraud_ok_amt,"
         + " A.survey_user,"
         + " A.case_close_flag,"
         + " A.case_close_flag,"
         + " A.survey_close,"
         + " A.survey_ing,"
         + " B.cntl_date,"
         + " B.cntl_user,"
         + " B.source_type,"
         + " B.imput_cnt,"
         + " B.d6_amt,"
         + " B.otb_amt,"
         + " A.miscell_unpay_amt"
   ;
   wp.daoTable = "rsk_ctfi_case a left join rsk_ctfi_warn b on A.card_no=B.card_no";

   if (wp.itemEq("ex_report_type", "A")) {
      wp.whereOrder = " order by A.case_type , A.case_date Asc ";
   }
   else if (wp.itemEq("ex_report_type", "B")) {
      wp.whereOrder = " order by A.case_source , A.case_date Asc ";
   }
   else if (wp.itemEq("ex_report_type", "C")) {
      wp.whereOrder = " order by A.survey_user , A.case_date Asc ";
   }
   else if (wp.itemEq("ex_report_type", "D")) {
      wp.whereOrder = " order by A.case_seqno , A.case_date Asc ";
   }


   pageQuery();

   if (sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setListCount(1);
   wp.setPageValue();
   listWkdata();
}

void sumRead(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " count(A.card_no) as obj_700311304,"
         + " sum(A.fraud_ok_cnt) as obj_700311306,"
         + " sum(A.fraud_ok_amt) as obj_700311308,"
         + " sum(B.d6_amt) as obj_700311310,"
         + " sum(B.otb_amt) as obj_700311312"
   ;
   wp.daoTable = "rsk_ctfi_case a left join rsk_ctfi_warn b on A.card_no=B.card_no ";
   wp.whereStr = lsWhere;

   pageSelect();
}

@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

}

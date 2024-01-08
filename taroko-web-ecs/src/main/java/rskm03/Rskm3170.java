package rskm03;
/**
 * 2022-0601   JH    mt0009409 bugfix
 * 2020-1106:  JH    modify
 * 2020-0106:  Alex  actionFunction-initButon fix
 * 2019-1225:  Alex  bug fix
 * 2019-1219:  Alex  ptr_branch -> gen_brn
 * 2019-1213:  Alex  bug fix
 * 2019-1206:  Alex  add initButton
 * 2019-1129:  Alex  pdf1 fixed
 * 2019-0815   JH    rskm3190_read
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseEdit;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Rskm3170 extends BaseEdit implements InfacePdf {
String kk1 = "", kk2 = "", kk3 = "";
String is_refer_no = "";
String is_row_id = "";
int ii_print_cnt=0;
taroko.base.CommDate zzdate = new taroko.base.CommDate();

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      kk1 = wp.itemStr("card_no");
      wp.itemSet("card_no", kk1);
   }
   else if (eqIgno(wp.buttonCode, "X2")) {
      /* 查詢功能 */
      strAction = "new";
      is_refer_no = wp.itemStr("data_k1");
      bil_bill();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) {
      //-資料讀取-
      strAction = "R";
      if (wp.respHtml.indexOf("_detl") > 0) {
         dataRead();
      }
      else if (wp.respHtml.equals("rskm3180")) {
         rskm3180_read();
      }
      else if (wp.respHtml.equals("rskm3190")) {
         rskm3190_read();
      }
      else if (wp.respHtml.equals("rskm3210")) {
         rskm3210_read();
      }
      else if (wp.respHtml.equals("rskm3220")) {
         rskm3220_read();
      }
   }
   else if (eqIgno(wp.buttonCode, "R2")) {
      //-資料讀取-
      strAction = "R2";
      is_row_id = wp.itemStr("data_k1");
      select_rsk_ctfi_txn();
   }
   else if (eqIgno(wp.buttonCode, "R3")) {
      //-資料讀取-
      strAction = "R";
      rskm3180_read();
   }
   else if (eqIgno(wp.buttonCode, "R4")) {
      //-處理程序.R-
      strAction = "R";
      rskm3190_read();
   }
   else if (eqIgno(wp.buttonCode, "R5")) {
      //-資料讀取-
      strAction = "R";
      rskm3210_read();
   }
   else if (eqIgno(wp.buttonCode, "R6")) {
      //-資料讀取-
      strAction = "R";
      rskm3220_read();
   }
   else if (eqIgno(wp.buttonCode, "R8")) {
      //-資料讀取-
      strAction = "R";
      detl4_add();
   }
   else if (eqIgno(wp.buttonCode, "R9")) {
      //-資料讀取-
      strAction = "R";
      kk1 = wp.itemStr("ex_card_no");
      detlRead3();
   }
   else if (eqIgno(wp.buttonCode, "R9U")) {
      //-資料讀取-
      strAction = "RU";
      kk1 = wp.itemStr("ex_card_no");
      detlRead3UD();
   }
   else if (eqIgno(wp.buttonCode, "R9D")) {
      //-資料讀取-
      strAction = "RD";
      kk1 = wp.itemStr("ex_card_no");
      detlRead3UD();
   }
   else if (eqIgno(wp.buttonCode, "R10")) {
      //-資料讀取-
      strAction = "R";
      kk1 = wp.itemStr("ex_card_no");
      detlRead4();
   }
   else if (eqIgno(wp.buttonCode, "R10U")) {
      //-資料讀取-
      strAction = "RU";
      kk1 = wp.itemStr("ex_card_no");
      detlRead4UD();
   }
   else if (eqIgno(wp.buttonCode, "R10D")) {
      //-資料讀取-
      strAction = "RD";
      kk1 = wp.itemStr("ex_card_no");
      detlRead4UD();
   }
   else if (eqIgno(wp.buttonCode, "R20")) {
      wp.colSet("ex_card_no", wp.itemStr("data_k1"));
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      strAction = "U";
      updateFunc();
   }
   else if (eqIgno(wp.buttonCode, "U3")) {
      /* 更新功能 */
      strAction = "U";
      if (wp.respHtml.indexOf("rskm3180") >= 0) {
         rskm3180_update();
      }

   }
   else if (eqIgno(wp.buttonCode, "U4")) {
      /* 更新功能 */
      strAction = "U";
      if (wp.respHtml.indexOf("rskm3210") >= 0) {
         rskm3210_update();
      }
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
   else if (eqIgno(wp.buttonCode, "PDF1")) {
      //--工作底稿
      strAction = "PDF";
      pdfPrint();
   }
   else if (eqIgno(wp.buttonCode, "PDF2")) {
      //--敘實報告
      strAction = "PDF";
      pdfPrint2();
   }
   else if (eqIgno(wp.buttonCode, "PDF3")) {
      //--聲明書
      strAction = "PDF";
      pdfPrint3();
   }
   else if (eqIgno(wp.buttonCode, "PDF4")) {
      //--結果報告
      strAction = "PDF";
      pdfPrint4();
   }
   else if (eqIgno(wp.buttonCode, "PDF5")) {
      //--冒用明細-
      strAction = "PDF";
      pdfPrint5();
   }
   else if (eqIgno(wp.buttonCode, "C9")) {
      //--Bill 新增至 交易紀錄
      strAction = "C9";
      proFuncBill();
   }
   else if (eqIgno(wp.buttonCode, "C10")) {
      //--txlog 新增至 交易紀錄
      strAction = "C10";
      proFuncTxlog();
   }
   else if (eqIgno(wp.buttonCode, "A3")) {
      //--敘實記錄 新增明細
      rskm3180_insert();
   }
   else if (eqIgno(wp.buttonCode, "A5")) {
      //--調查結果 新增明細
      rskm3210_insert();
   }
   else if (eqIgno(wp.buttonCode, "AJAX")) {
	   if(wp.respHtml.equals("rskm3170_detl"))
		   processAjaxOption(wr);
	   else if(wp.respHtml.equals("rskm3210"))
		   wf_ajax_key(wr);
   }

   dddwSelect();
   initButton();
//		if (rc==1) {
//			initButton();
//		}

}


@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml,"rskm3170")) {
         wp.optionKey =wp.itemStr("ex_case_type");
         dddwList("dddw_ex_case_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFI_CASE_TYPE'");
      }

      if (wp.respHtml.indexOf("3210") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("conf_case_type");
         dddwList("dddw_case_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFI_CASE_TYPE'");
         wp.initOption = "--";
         wp.optionKey = wp.colStr("fraud_area");
         dddwList("dddw_fraud_area", "rsk_ctfi_area"
               , "area_remark", "area_remark", "where 1=1 order by area_code Asc");
      }
   }
   catch (Exception ex) {
   }


   try {
      if (wp.respHtml.indexOf("3220") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("skim_type");
         dddwList("d_dddw_skim_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFI_WARN_SKIM'");
      }
      wp.optionKey = wp.colStr("cntl_user");
      dddwList("d_dddw_cntl_user", "ptr_sys_idtab"
            , "wf_desc", "wf_desc", "where wf_type ='CTFI_CASE_USER'");
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("3220") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("source_type");
         dddwList("d_dddw_source_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFI_WARN_SOURCE'");
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

   if (wp.itemEmpty("ex_case_date1") &&
         wp.itemEmpty("ex_case_date2") &&
         wp.itemEmpty("ex_card_no") &&
         wp.itemEmpty("ex_case_seqno") &&
         wp.itemEmpty("ex_case_user") &&
         wp.itemEmpty("ex_idno")) {
      alertErr("查詢條件不可全部空白");
      return;
   }

   wp.whereStr = " where 1=1"
         + sqlCol(wp.itemStr("ex_case_date1"), "case_date", ">=")
         + sqlCol(wp.itemStr("ex_case_date2"), "case_date", "<=")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
         + sqlCol(wp.itemStr("ex_case_seqno"), "case_seqno", "like%")
         + sqlCol(wp.itemStr("ex_case_user"), "case_user", "like%")
   +sqlCol(wp.itemStr("ex_surv_user"),"survey_user", "like%")
   +sqlCol(wp.itemStr("ex_case_type"),"case_type")
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
   wp.selectSQL = "A.card_no, "
         + "uf_idno_id2(A.card_no,'') as idno, "
         + "replace(uf_idno_name2(A.card_no,''),'　','') as db_chi_name, "
         + "A.case_date, "
         + "A.case_seqno, "
         + "A.new_end_date,"
         + "A.group_code,"
         + "A.case_type,"
         + "A.case_source,"
         + "A.fraud_ok_cnt,"
         + "A.fraud_ok_amt,"
         + "A.case_close_flag,"
         + "A.survey_close,"
         + "A.survey_ing,"
         + "A.miscell_unpay_amt,"
         + "A.survey_user,"
         + "hex(A.rowid) as rowid"
   ;
   wp.daoTable = "rsk_ctfi_case A";
   wp.whereOrder = " order by A.case_seqno, A.fraud_ok_amt Asc ";

   pageQuery();
   list_wkdata();

   wp.setListCount(1);
   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   wp.totalRows = wp.dataCnt;
   wp.listCount[1] = wp.dataCnt;
   wp.setPageValue();
}

void list_wkdata() {
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
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   if (empty(kk1)) {
      kk1 = itemKk("card_no");
   }

   if (empty(kk1)) {
      alertErr("卡號:不可空白");
      return;
   }
   wp.selectSQL = "hex(A.rowid) as rowid, A.mod_seqno,"
         + "card_no,   "
//				  + "B.id_no as idno, "
//				  + "B.chi_name as db_chi_name, "
//				  + "B.birthday as db_bir_date,"
         + "tel_no1,"
         + "tel_no2,"
         + "tel_no3,"
         + "bill_addr,"
         + "card_no,"
         + "new_end_date,"
         + "group_code,"
         + "vd_flag,"
         + "case_date,"
         + "case_seqno,"
         + "turn_legal_flag,"
         + "turn_coll_flag,"
         + "case_type,"
         + "case_source,"
         + "fraud_ok_cnt,"
         + "fraud_ok_amt,"
         + "survey_user,"
         + "survey_date,"
         + "case_remark,"
         + "risk_bank_no"
   ;
   wp.daoTable = "rsk_ctfi_case A ";//left join crd_idno B on A.id_p_seqno =B.id_p_seqno";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "A.card_no");

   pageSelect();

   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }
   detlRead();
   detlRead2();
//		detlRead3();
//		detlRead4();
}

void detlRead() throws Exception  {
   wp.selectSQL = "A.id_no as db_idno, "
         + "A.id_p_seqno,   "
         + "replace(A.chi_name,'　','') as db_chi_name, "
         + "A.birthday as db_bir_date, "
         + "(select risk_bank_no from act_acno where acno_p_seqno =B.acno_p_seqno) as risk_bank_no , "
         + "'C' as vd_flag"
   ;
   wp.daoTable = "crd_card B join crd_idno A on A.id_p_seqno = B.id_p_seqno";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "B.card_no");
   pageSelect();
   if (sqlNotFind()) {
      wp.selectSQL = "A.id_no as db_idno, "
            + "A.id_p_seqno,   "
            + "replace(A.chi_name,'　','') as db_chi_name, "
            + "A.birthday as db_bir_date, "
            + "(select risk_bank_no from dba_acno where p_seqno =B.p_seqno) as risk_bank_no , "
            + "'D' as vd_flag"
      ;
      wp.daoTable = "dbc_card B join dbc_idno A on A.id_p_seqno = B.id_p_seqno";
      wp.whereStr = " where 1=1"
            + sqlCol(kk1, "B.card_no");
      pageSelect();
      if (sqlNotFind()) {
         wp.notFound = "N";
      }
   }

   if (!wp.colEmpty("risk_bank_no")) {
      //--風險行
      String sql1 = "select full_chi_name "
            + " from gen_brn"
            + " where branch =? ";
      sqlSelect(sql1, new Object[]{
            wp.colStr("risk_bank_no")
      });
      if (sqlRowNum > 0) {
         wp.colSet("tt_risk_bank_no", "." + sqlStr("full_chi_name"));
      }
   }

}

void detlRead2() throws Exception {
   wp.pageRows = 999;
   this.daoTid = "A-";
   wp.selectSQL = "hex(rowid) as rowid,"
         + "card_no, "
         + "txn_date, "
         + "txn_time, "
         + "mcht_no, "
         + "term_id,"
         + "mcht_category,"
         + "pos_em,"
         + "mcht_name,"
         + "mcht_addr,"
         + "mcht_tel_no,"
         + "txn_amt,"
         + "resp_code,"
         + "mcht_city,"
         + "mcht_country,"
         + "arq_bank_no,"
         + "cris_score,"
         + "cris_action,"
         + "cris_actor,"
         + "recept_flag,"
         + "recept_date,"
         + "problm_flag,"
         + "problm_date,"
         + "recept_return_flag,"
         + "recept_return_date,"
         + "txn_remark,"
         + "three_d_flag,"
         + "ip_addr,"
         + "source_type,"
         + "vd_flag,"
         + "reference_no,"
         + "mcht_zip,"
         + "source_amt,"
         + "source_curr,"
         + "IC_FALLBACK,"
         + "mcht_ename,"
         + "eci_data,"
         + "ucaf_data,"
         + "crt_user,"
         + "crt_date,"
         + "mod_user,"
         + "mod_time,"
         + "mod_pgm,"
         + "mod_seqno"
   ;
   wp.daoTable = "rsk_ctfi_txn";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "card_no")
   ;
   wp.whereOrder = " order by txn_date Asc , txn_time Asc ";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }
   pageQuery();
   wp.setListCount(1);
   if (this.sqlNotFind()) {
      wp.notFound = "N";
   }
//		if(this.sqlNotFind()) {
//			wp.respCode ="00";
//		}
}

/**
 * @throws Exception
 */

int selectCountBill() throws Exception  {
   
   if(empty(kk1)) {
	   errmsg("卡號不可空白");
	   return 0;
   }
   
   String sql1 = " select count(*) as db_cnt from bil_bill A left join bil_nccc300_dtl B on a.reference_no =B.reference_no "
         + " where 1=1 "
		 + sqlCol(kk1,"A.card_no")
         + sqlCol(wp.itemStr("ex_tx_date1"), "A.purchase_date", ">=")
         + sqlCol(wp.itemStr("ex_tx_date2"), "A.purchase_date", "<=");

   sqlSelect(sql1);

   return sqlInt("db_cnt");
}

void detlRead3() throws Exception {
   int li_limit = 0;
   selectNoLimit();
   li_limit = wp.pageRows;

   wp.selectSQL = "A.reference_no, "
         + "A.p_seqno, "
         + "A.card_no, "
         + "A.purchase_date, "
         + "A.acq_member_id,"
         + "A.dest_amt,"
         + "A.mcht_no,"
         + commSqlStr.mchtName("A.mcht_chi_name", "A.mcht_eng_name") + " as mcht_chi_name ,"
         + "A.mcht_city,"
         + "A.mcht_country,"
         + "A.mcht_category,"
         + "A.mcht_zip,"
         + "A.pos_entry_mode,"
         + "A.auth_code,"
         + "A.source_amt,"
         + "A.source_curr,"
         + "A.dest_curr,"
         + "B.auth_response_code,"
         + "B.ucaf,"
         + "B.ec_ind"
   ;
   wp.daoTable = "bil_bill A left join bil_nccc300_dtl B on a.reference_no =B.reference_no";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "A.card_no")
         + sqlCol(wp.itemStr("ex_tx_date1"), "A.purchase_date", ">=")
         + sqlCol(wp.itemStr("ex_tx_date2"), "A.purchase_date", "<=")
   ;
   wp.whereOrder = " order by A.purchase_date Desc limit 0 , " + li_limit;

   pageQuery();

   if (this.sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
   }
   wp.colSet("page_no", "1");

   wp.colSet("total_cnt", selectCountBill());

   wp.setListCount(0);
}

void detlRead3UD() throws Exception {
   selectNoLimit();
   int li_limit = 0, li_page = 0, li_limit_s = 0, li_total_cnt = 0;
   li_limit = wp.pageRows;
   li_page = (int) wp.itemNum("page_no");
   li_total_cnt = (int) wp.itemNum("total_cnt");
   wp.listCount[0] = wp.itemRows("card_no");
   if(li_page == 0) {
	   alertErr("請先執行查詢");
	   return ;
   }
   if (eqIgno(strAction, "RU")) {
      if (li_page == 1) {
         alertErr("已是第一頁 !");
         return;
      }
      else {
         li_page--;
         li_limit_s = (li_page - 1) * li_limit;

      }
   }
   else {
      li_page++;
      li_limit_s = (li_page - 1) * li_limit;
      if (li_limit_s >= li_total_cnt) {
         alertErr("已是最後一頁 !");
         return;
      }
   }


   wp.selectSQL = "A.reference_no, "
         + "A.p_seqno, "
         + "A.card_no, "
         + "A.purchase_date, "
         + "A.acq_member_id,"
         + "A.dest_amt,"
         + "A.mcht_no,"
         + commSqlStr.mchtName("A.mcht_chi_name", "A.mcht_eng_name") + " as mcht_chi_name ,"
         + "A.mcht_city,"
         + "A.mcht_country,"
         + "A.mcht_category,"
         + "A.mcht_zip,"
         + "A.pos_entry_mode,"
         + "A.auth_code,"
         + "A.source_amt,"
         + "A.source_curr,"
         + "A.dest_curr,"
         + "B.auth_response_code,"
         + "B.ucaf,"
         + "B.ec_ind"
   ;
   wp.daoTable = "bil_bill A left join bil_nccc300_dtl B on a.reference_no =B.reference_no";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "A.card_no")
         + sqlCol(wp.itemStr("ex_tx_date1"), "A.purchase_date", ">=")
         + sqlCol(wp.itemStr("ex_tx_date2"), "A.purchase_date", "<=")
   ;
   wp.whereOrder = " order by card_no limit " + li_limit_s + " , " + li_limit;

   pageQuery();

   if (this.sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
   }
   wp.colSet("page_no", li_page);
   wp.listCount[0] = wp.selectCnt;
   int aa = 1;
   for (int ii = 0; ii < li_limit; ii++) {
      if (li_limit_s + aa < 10) {
         wp.colSet(ii, "ser_num", "0" + (li_limit_s + aa));
      }
      else {
         wp.colSet(ii, "ser_num", "" + (li_limit_s + aa));
      }
      aa++;
   }

}

int selectCountTxlog() throws Exception  {
   String sql1 = " select count(*) as db_cnt from cca_auth_txlog A "
         + " where 1=1 "
         + sqlCol(kk1,"A.card_no")
         + sqlCol(wp.itemStr("ex_tx_date1"), "A.tx_date", ">=")
         + sqlCol(wp.itemStr("ex_tx_date2"), "A.tx_date", "<=");
   
   
   
   sqlSelect(sql1);

   return sqlInt("db_cnt");

}

void detlRead4() throws Exception {
   int li_limit = 0;
   selectNoLimit();
   li_limit = wp.pageRows;

   wp.selectSQL = ""
         + " A.auth_seqno ,"
         + " A.card_no ,"
         + " A.tx_date ,"
         + " A.tx_time ,"
         + " A.eff_date_end ,"
         + " A.mcht_no ,"
         + " A.mcht_name ,"
         + " A.mcc_code ,"
         + " A.pos_mode ,"
         + " substr(A.pos_mode,1,2) as pos_mode_1_2 ,"
         + " substr(A.pos_mode,3,1) as pos_mode_3 ,"
         + " A.nt_amt ,"
         + " A.consume_country ,"
         + " A.tx_currency ,"
         + " A.iso_resp_code ,"
         + " A.auth_status_code ,"
         + " A.iso_adj_code ,"
         + " A.auth_no ,"
         + " A.auth_user ,"
         + " A.vip_code ,"
         + " A.stand_in ,"
         + " A.class_code ,"
         + " A.auth_unit ,"
         + " A.logic_del ,"
         + " A.auth_remark ,"
         + " A.trans_type ,"
         + " uf_idno_id2(A.card_no,'') as id_no ,"
         + " uf_idno_name(A.id_p_seqno) as db_idno_name ,"
         + " A.curr_otb_amt ,"
         + " A.curr_tot_lmt_amt ,"
         + " A.curr_tot_std_amt ,"
         + " A.curr_tot_tx_amt ,"
         + " A.curr_tot_cash_amt ,"
         + " A.curr_tot_unpaid ,"
         + " A.fallback ,"
         + " A.roc ,"
         + " A.ibm_bit39_code ,"
         + " A.ibm_bit33_code ,"
         + " A.ec_ind ,"
         + " A.ucaf ,"
         + " A.mtch_flag ,"
         + " A.ec_flag ,"
         + " uf_tt_ccas_parm3('LOGICDEL',A.logic_del) as tt_logic_del ,"
         + " A.v_card_no ,"
         + " A.online_redeem ,"
//				 + " (select mcht_name from cca_mcht_bill where mcht_no =A.mcht_no fetch first 1 rows only) as mcht_chi_name ,"
         + " uf_tt_ccas_parm3('AUTHUNIT',A.auth_unit) as tt_auth_unit ,"
         + " decode(A.online_redeem,'','','A','分期 (A)','I','分期 (I)','E','分期 (E)','Z','分期 (Z)','0','紅利 (0)','1','紅利 (1)','2','紅利 (2)',"
         + " '3','紅利 (3)','4','紅利 (4)','5','紅利 (5)','6','紅利 (6)','7','紅利 (7)','') as tt_online_redeem ,"
         + " decode(curr_tot_std_amt,0,0,((curr_tot_unpaid+decode(cacu_amount,'Y',nt_amt,0)) / curr_tot_std_amt)) * 100 as cond_curr_rate , "
         + " A.id_p_seqno , "
         + " iso_resp_code||'-'||auth_status_code||'-'||iso_adj_code as wk_resp , "
         + " ibm_bit39_code||'-'||ibm_bit33_code as wk_IBM  ";
   wp.daoTable = " cca_auth_txlog A ";
   wp.whereStr = " where 1=1 "
         + sqlCol(kk1, "A.card_no")
         + sqlCol(wp.itemStr("ex_tx_date1"), "A.tx_date", ">=")
         + sqlCol(wp.itemStr("ex_tx_date2"), "A.tx_date", "<=")
   ;
   wp.whereOrder = " order by A.tx_date desc, A.tx_time Desc limit 0 , " + li_limit;
   pageQuery();
   if (this.sqlNotFind()) {
//      selectOK();
	  alertErr("此條件查無資料");
      return;
   }
   wp.colSet("page_no", "1");

   wp.colSet("total_cnt", selectCountTxlog());

   wp.setListCount(0);
   detlRead4_after();

}

void detlRead4UD() throws Exception {
   selectNoLimit();
   int li_limit = 0, li_page = 0, li_limit_s = 0, li_total_cnt = 0;
   li_limit = wp.pageRows;
   li_page = (int) wp.itemNum("page_no");
   li_total_cnt = (int) wp.itemNum("total_cnt");
   wp.listCount[0] = wp.itemRows("card_no");
   if(li_page == 0) {
	   alertErr("請先執行查詢");
	   return ;
   }
   if (eqIgno(strAction, "RU")) {
      if (li_page == 1) {
         alertErr("已是第一頁 !");
         return;
      }
      else {
         li_page--;
         li_limit_s = (li_page - 1) * li_limit;

      }
   }
   else {
      li_page++;
      li_limit_s = (li_page - 1) * li_limit;
      if (li_limit_s >= li_total_cnt) {
         alertErr("已是最後一頁 !");
         return;
      }
   }

   wp.selectSQL = ""
         + " A.auth_seqno ,"
         + " A.card_no ,"
         + " A.tx_date ,"
         + " A.tx_time ,"
         + " A.eff_date_end ,"
         + " A.mcht_no ,"
         + " A.mcht_name ,"
         + " A.mcc_code ,"
         + " A.pos_mode ,"
         + " substr(A.pos_mode,1,2) as pos_mode_1_2 ,"
         + " substr(A.pos_mode,3,1) as pos_mode_3 ,"
         + " A.nt_amt ,"
         + " A.consume_country ,"
         + " A.tx_currency ,"
         + " A.iso_resp_code ,"
         + " A.auth_status_code ,"
         + " A.iso_adj_code ,"
         + " A.auth_no ,"
         + " A.auth_user ,"
         + " A.vip_code ,"
         + " A.stand_in ,"
         + " A.class_code ,"
         + " A.auth_unit ,"
         + " A.logic_del ,"
         + " A.auth_remark ,"
         + " A.trans_type ,"
         + " uf_idno_id2(A.card_no,'') as id_no ,"
         + " uf_idno_name(A.id_p_seqno) as db_idno_name ,"
         + " A.curr_otb_amt ,"
         + " A.curr_tot_lmt_amt ,"
         + " A.curr_tot_std_amt ,"
         + " A.curr_tot_tx_amt ,"
         + " A.curr_tot_cash_amt ,"
         + " A.curr_tot_unpaid ,"
         + " A.fallback ,"
         + " A.roc ,"
         + " A.ibm_bit39_code ,"
         + " A.ibm_bit33_code ,"
         + " A.ec_ind ,"
         + " A.ucaf ,"
         + " A.mtch_flag ,"
         + " A.ec_flag ,"
         + " uf_tt_ccas_parm3('LOGICDEL',A.logic_del) as tt_logic_del ,"
         + " A.v_card_no ,"
         + " A.online_redeem ,"
//				 + " (select mcht_name from cca_mcht_bill where mcht_no =A.mcht_no fetch first 1 rows only) as mcht_chi_name ,"
         + " uf_tt_ccas_parm3('AUTHUNIT',A.auth_unit) as tt_auth_unit ,"
         + " decode(A.online_redeem,'','','A','分期 (A)','I','分期 (I)','E','分期 (E)','Z','分期 (Z)','0','紅利 (0)','1','紅利 (1)','2','紅利 (2)',"
         + " '3','紅利 (3)','4','紅利 (4)','5','紅利 (5)','6','紅利 (6)','7','紅利 (7)','') as tt_online_redeem ,"
         + " decode(curr_tot_std_amt,0,0,((curr_tot_unpaid+decode(cacu_amount,'Y',nt_amt,0)) / curr_tot_std_amt)) * 100 as cond_curr_rate , "
         + " A.id_p_seqno , "
         + " iso_resp_code||'-'||auth_status_code||'-'||iso_adj_code as wk_resp , "
         + " ibm_bit39_code||'-'||ibm_bit33_code as wk_IBM  ";
   wp.daoTable = " cca_auth_txlog A ";
   wp.whereStr = " where 1=1 "
         + sqlCol(kk1, "A.card_no")
         + sqlCol(wp.itemStr("ex_tx_date1"), "A.tx_date", ">=")
         + sqlCol(wp.itemStr("ex_tx_date2"), "A.tx_date", "<=")
   ;
   wp.whereOrder = " order by A.tx_date desc, A.tx_time Desc limit " + li_limit_s + " , " + li_limit;
   pageQuery();
   if (this.sqlNotFind()) {
      selectOK();
      return;
   }
   wp.colSet("page_no", li_page);
   wp.listCount[0] = wp.selectCnt;
   int aa = 1;
   for (int ii = 0; ii < li_limit; ii++) {
      if (li_limit_s + aa < 10) {
         wp.colSet(ii, "ser_num", "0" + (li_limit_s + aa));
      }
      else {
         wp.colSet(ii, "ser_num", "" + (li_limit_s + aa));
      }
      aa++;
   }
   detlRead4_after();

}

void detlRead4_after()  throws Exception {
   //--特店中文名稱
   wp.logSql = false;
   String sql1 = " select mcht_name from cca_mcht_bill where mcht_no = ?  " + commSqlStr.rownum(1);
   String sql2 = " select entry_type from cca_entry_mode where entry_mode = ? " + commSqlStr.rownum(1);

   for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[]{wp.colStr(ii, "C-mcht_no")});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "C-mcht_chi_name", sqlStr("mcht_name"));
      }
      sqlSelect(sql2, new Object[]{wp.colStr(ii, "C-pos_mode_1_2")});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "C-db_entry_mode_type", sqlStr("entry_type"));
      }
      if (ii < 9) {
         wp.colSet(ii, "C-ser_num", "0" + (ii + 1));
      }
      else {
         wp.colSet(ii, "C-ser_num", "" + (ii + 1));
      }
      //wp.colSet(ii,"wk_resp", wp.colStr(ii,"iso_resp_code")+"-"+wp.colStr(ii,"auth_status_code")+"-"+wp.colStr(ii,"iso_adj_code"));

   }
}

void detl4_add() throws Exception  {
   //--kk1:auth_seqno kk2:card_no kk3 = nt_amt
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   kk3 = wp.itemStr("data_k3");

   wp.selectSQL = " card_no , "
         + " tx_date as txn_date , "
         + " tx_time as txn_time , "
         + " mcht_no , "
         + " term_id , "
         + " mcc_code as mcht_category , "
         + " pos_mode as pos_em , "
         + " nt_amt as txn_amt , "
         + " iso_resp_code as resp_code , "
         + " stand_in as arq_bank_no , "
         + " mcht_city , "
         + " mcht_country , "
         + " ec_ind as eci_data , "
         + " ucaf as ucaf_data , "
         + " mcht_name as mcht_ename , "
         + " '' as mcht_name , "
         + " ori_amt as source_amt , "
         + " tx_currency as source_curr , "
         + " '' as mcht_addr , "
         + " '' as mcht_tel_no , "
         + " '' as id_addr , "
         + " auth_remark as txn_remark , "
         + " fallback as ic_fallback "
   ;

   wp.daoTable = " cca_auth_txlog ";
   wp.whereStr = " where 1=1 "
         + sqlCol(kk1, "auth_seqno")
         + sqlCol(kk2, "card_no")
         + sqlCol(kk3, "nt_amt")
   ;

   pageSelect();
   if (sqlRowNum <= 0) {
      errmsg("查無資料");
      return;
   }

   detl4_add_after();

}

void detl4_add_after() throws Exception  {
   String sql1 = " select mcht_name , mcht_addr from cca_mcht_bill where mcht_no = ?  ";
   sqlSelect(sql1, new Object[]{wp.colStr("mcht_no")});
   if (sqlRowNum > 0) {
      wp.colSet("mcht_name", sqlStr("mcht_name"));
      wp.colSet("mcht_addr", sqlStr("mcht_addr"));
   }
}

@Override
public void saveFunc() throws Exception {
   int li_page = wp.respHtml.lastIndexOf("_detl");
   wp.log("_detl=" + li_page);
   if (li_page >= 0) {
      wp.listCount[0] = wp.itemRows("A-rowid");
      wp.selectCnt = wp.itemRows("B-card_no");
      wp.setListSernum(2, "B-ser_num", wp.itemRows("B-card_no"));
      Rskm3170Func func = new Rskm3170Func(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
         alertErr(func.getMsg());
      }
      this.sqlCommit(rc);
      return;
   }

   li_page = wp.respHtml.lastIndexOf("_txn");
   wp.log("_txn=" + li_page);
   if (li_page > 0) {
      Rskm3170Txn func = new Rskm3170Txn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
         alertErr(func.getMsg());
      }
      this.sqlCommit(rc);
      return;
   }

   li_page = wp.respHtml.lastIndexOf("3190");
   wp.log("3190=" + li_page);
   if (li_page > 0) {
      Rskm3190Func func = new Rskm3190Func(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
         alertErr(func.getMsg());
      }
      this.sqlCommit(rc);
      return;
   }

   li_page = wp.respHtml.lastIndexOf("3220");
   if (li_page > 0) {
      Rskm3220Func func = new Rskm3220Func();
      func.setConn(wp);

      if (wp.itemEmpty("rowid_warn")) {
         rc = func.dbSave("A");
      }
      else {
         rc = func.dbSave("U");
      }
      this.sqlCommit(rc);
      if (rc < 0) {
         alertErr(func.getMsg());
      }
      return;
   }

}
//
//	boolean checkRskm3220(){
//		String sql1 = " select count(*) as cr_cnt from rsk_ctfi_warn where card_no = ? and mod_seqno = ? ";
//		sqlSelect(sql1,new Object[]{wp.itemStr("card_no"),wp.itemNum("mod_seqno")});
//		if(sql_num("cr_cnt")>0)	return true ;
//		return false ;
//	}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
   }
   if (wp.respHtml.indexOf("_txn") > 0) {
      btnModeAud(wp.colStr("db_old"));
   }

   if (eqIgno(wp.respHtml, "rskm3170_bill") || eqIgno(wp.respHtml, "rskm3170_txlog")) {
      btnModeAud("XX");
   }
   
   if (eqIgno(wp.respHtml, "rskm3180") || eqIgno(wp.respHtml, "rskm3190") ||
   	 eqIgno(wp.respHtml, "rskm3210") || eqIgno(wp.respHtml, "rskm3220")) {
      btnModeAud("XX");
   }

}

void get_idno_data(String s1) throws Exception  {
   wp.sqlCmd = "select b.id_no as db_idno,"
         + " b.chi_name as db_chi_name,"
         + " b.birthday as db_bir_date,"
         + " 'C' as vd_flag, "
         + " decode(b.home_area_code1,null,null,b.home_area_code1||'-')||b.home_tel_no1 as tel_no1,"
         + " decode(b.office_area_code1,null,null,b.office_area_code1||'-')||b.office_tel_no1 as tel_no2,"
         + " b.cellar_phone as tel_no3,"
         + " a.bill_sending_addr1||a.bill_sending_addr2||a.bill_sending_addr3||a.bill_sending_addr4||a.bill_sending_addr5 as bill_addr, "
         + " c.new_end_date,"
         + " c.group_code"
         + " from crd_card c, crd_idno b, act_acno a "
         + " where b.id_p_seqno = c.id_p_seqno and a.acno_p_seqno = c.acno_p_seqno and c.card_no ='" + s1 + "'"
   ;
   this.sqlSelect();
   if (sqlRowNum <= 0) {
      wp.sqlCmd = "select b.id_no as db_idno,"
            + " b.chi_name as db_chi_name,"
            + " b.birthday as db_bir_date,"
            + " 'D' as vd_flag, "
            + " decode(b.home_area_code1,null,null,b.home_area_code1||'-')||b.home_tel_no1 as tel_no1,"
            + " decode(b.office_area_code1,null,null,b.office_area_code1||'-')||b.office_tel_no1 as tel_no2,"
            + " b.cellar_phone as tel_no3,"
            + " a.bill_sending_addr1||a.bill_sending_addr2||a.bill_sending_addr3||a.bill_sending_addr4||a.bill_sending_addr5 as bill_addr, "
            + " c.new_end_date,"
            + " c.group_code"
            + " from dbc_card c, dbc_idno b, dba_acno a "
            + " where b.id_p_seqno = c.id_p_seqno and a.p_seqno = c.p_seqno and c.card_no ='" + s1 + "'"
      ;
      this.sqlSelect();
   }
   if (sqlRowNum <= 0) {
      alertErr("查無卡號: Card_No=" + s1);
   }
   return;
}

public void wf_ajax_winid(TarokoCommon wr) throws Exception {
   super.wp = wr;

   //String ls_winid =
   get_idno_data(wp.itemStr("ax_winid"));
   if (rc != 1) {
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
   wp.addJSON("new_end_date", sqlStr("new_end_date"));
   wp.addJSON("group_code", sqlStr("group_code"));
}

void detl2_Read() throws Exception {
   if (empty(kk1)) {
      kk1 = wp.itemStr("data_k1");
   }
   wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
         + "card_no,   "
         + "txn_date, "
         + "txn_time, "
         + "mcht_no,"
         + "term_id,"
         + "mcht_category,"
         + "pos_em,"
         + "mcht_name,"
         + "mcht_addr,"
         + "mcht_tel_no,"
         + "txn_amt,"
         + "resp_code,"
         + "mcht_city,"
         + "mcht_country,"
         + "arq_bank_no,"
         + "cris_score,"
         + "crstrAction,"
         + "cris_actor,"
         + "recept_flag,"
         + "recept_date,"
         + "problm_flag,"
         + "problm_date,"
         + "recept_return_flag,"
         + "recept_return_date,"
         + "txn_remark,"
         + "three_d_flag,"
         + "ip_addr,"
         + "source_type,"
         + "vd_flag,"
         + "reference_no,"
         + "mcht_zip,"
         + "source_amt,"
         + "source_curr,"
         + "IC_FALLBACK,"
         + "mcht_ename,"
         + "eci_data,"
         + "ucaf_data,"
         + "crt_user,"
         + "crt_date,"
         + "mod_user,"
         + "mod_time,"
         + "mod_pgm"

   ;
   wp.daoTable = "rsk_ctfi_txn";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "hex(rowid)");

   pageSelect();
   detlRead();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }
}

void bil_bill() throws Exception {
   //String rr=wp.itemStr("B-reference_no");
   wp.selectSQL = "A.card_no, "
         + "A.purchase_date as txn_date, "
         + "A.dest_amt as txn_amt, "
         + "A.mcht_no as mcht_no, "
         + "A.mcht_chi_name as mcht_name,"
         + "A.mcht_eng_name as mcht_ename,"
         + "A.auth_code as resp_code,"
         + "A.mcht_city ,"
         + "A.mcht_country,"
         + "A.mcht_category,"
         + "A.source_amt,"
         + "A.source_curr,"
         + "B.EC_IND as eci_data,"
         + "B.UCAF as ucaf_data"

   //     + "vd_flag"

   ;
   wp.daoTable = "bil_bill A , bil_nccc300_dtl B ";
   wp.whereStr = " where 1=1"
         + " and A.reference_no = B.reference_no"
         + " and A.reference_no =:refer_no"
//				+sqlCol(is_refer_no,"reference_no")
   ;
   this.setString("refer_no", is_refer_no);

   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, rr=" + is_refer_no);
      return;
   }

}

void select_rsk_ctfi_txn() throws Exception {
   if (empty(is_row_id)) {
      is_row_id = wp.itemStr("kk1");
   }
   wp.selectSQL =
         "card_no,"
               + "mcht_no,   "
               + "txn_date, "
               + "txn_time, "
               + "term_id,"
               + "mcht_category,"
               + "pos_em,"
               + "txn_amt,"
               + "resp_code,"
               + "arq_bank_no,"
               + "mcht_city,"
               + "mcht_country,"
               + "eci_data,"
               + "ucaf_data,"
               + "mcht_name,"
               + "mcht_ename,"
               + "source_amt,"
               + "source_curr,"
               + "three_d_flag,"
               + "mcht_addr,"
               + "mcht_tel_no,"
               + "ip_addr,"
               + "txn_remark,"
               + "hex(rowid) as rowid, mod_seqno, "
               + " '1' as db_old , "
               + " ic_fallback ";

   wp.daoTable = "rsk_ctfi_txn";
   wp.whereStr = " where 1=1" + " and rowid =x'" + is_row_id + "'";

   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, rr=" + is_row_id);
      return;
   }
}

void rskm3180_read() throws Exception {
   kk1 = wp.itemStr("card_no");

   if (empty(kk1)) {
      alertErr("卡號不可空白 !");
      return;
   }

   wp.selectSQL = "hex(A.rowid) as rowid, A.mod_seqno,"
         + "A.card_no,"
         + "A.call_date,"
         + "A.call_time,"
         + "A.tel_no,"
         + "A.tel_no2,"
         + "A.call_man,"
         + "A.call_desc,"
         + "A.call_desc02,"
         + "A.call_desc03,"
         + "A.call_desc04,"
         + "A.call_telno,"
         + "A.call_man||A.call_telno||A.tel_no2||A.call_desc||A.call_desc02||A.call_desc03 as old_data , "
         + "A.mod_user,"
         + "decode((select usr_cname from sec_user where usr_id =A.mod_user),'',A.mod_user,(select usr_cname from sec_user where usr_id =A.mod_user)) as tt_mod_user ,"
         + "A.mod_time,"
         + "A.mod_pgm";

   wp.daoTable = "rsk_ctfi_call_log A ";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "A.card_no");
   wp.whereOrder = " order by A.call_date Desc , A.call_time Desc ";
   pageQuery();
   if (sqlNotFind()) {
      selectOK();
      wp.colSet("ex_sysdate", wp.sysDate);
      wp.colSet("ex_systime", zzdate.sysTime());
      wp.colSet("ex_call_date", wp.sysDate);
      wp.colSet("ex_call_time", zzdate.sysTime());
      wp.colSet("ex_loginUser", wp.loginUser);
      return;
   }
   wp.setListCount(1);
   //wp.log("call-log="+wp.selectCnt+", sqlnrow="+this.sqlRowNum);
   wp.colSet("ex_call_date", wp.sysDate);
   wp.colSet("ex_sysdate", wp.sysDate);
   wp.colSet("ex_systime", zzdate.sysTime());
   wp.colSet("ex_call_time", zzdate.sysTime());
   wp.colSet("ex_loginUser", wp.loginUser);
}

void rskm3190_read() throws Exception {
   Rskm3190Func func = new Rskm3190Func(wp);
   rc = func.dataSelect();
   if (rc == -1) {
      alertErr(func.getMsg());
   }

   return;
}

void rskm3210_read() throws Exception {
   kk1 = wp.itemStr("card_no");

   if (empty(kk1)) {
      alertErr("卡號不可空白 !");
      return;
   }

   Rskm3210Func func = new Rskm3210Func();
   func.setConn(wp);
   rc = func.dataSelect();
   if (rc == -1) {
      alertErr(func.getMsg());
      return;
   }
//	wp.selectSQL = "hex(rowid) as rowid, mod_seqno,"
//			+ "card_no,"
//			+ "id_p_seqno,"
//			+ "case_seqno,"
//			+ "case_type,"
//			+ "fraud_ok_amt,"
//			+ "vd_flag,"
//			+ "fraud_area,"
//			+ "disput_sign,"
//			+ "conf_case_type,"
//			+ "modus_oper,"
//			+ "friend_fraud_flag,"
//			+ "ch_moral_flag,"
//			+ "no_fraud_flag,"
//			+ "survey_result,"
//			+ " survey_result2 ,"
//			+ " survey_result3 ,"
//			+ " survey_result4 ,"
//			+ " survey_result5 ,"
//			+ " survey_result6 ,"
//			+ " survey_result7 ,"
//			+ " survey_result8 ,"
//			+ " survey_result9 ,"
//			+ " survey_result10 ,"
//			+ "survey_result_remark,"
//			+ "case_file_flag,"
//			+ "fraud_file_flag,"
//			+ "adj_limit_code,"
//			+ "adj_limit,"
//			+ "un_adj_reason,"
//		//	+ "uf_idno_name(card_no) as db_idno_name,"
//			+ "mod_user,"
//			+ "mod_time,"
//			+ "mod_pgm,"
//			+ "fraud_cntry_code,"
//			+ "fraud_area_code ,"
//			+ "reissue_card_flag ";
//
//	wp.daoTable = "rsk_ctfi_case";
//	wp.whereStr = " where 1=1"
//			+ sqlCol(kk1, "card_no");
//	pageQuery();
//	if(sqlNotFind()){
//		selectOK();
//	}

   //--option
   String ls_option = "";
   wp.optionKey = wp.colStr("modus_oper");
   if (wp.colEq("conf_case_type", "Fraud Application")) {
      ls_option = ddlbOption(new String[]{"", "真證件", "假證件"});
   }
   else if (wp.colEq("conf_case_type", "Lost/Stolen")) {
      ls_option = ddlbOption(new String[]{"", "汽機車竊盜", "住家竊盜", "遺失", "被竊", "被搶", "商店竊盜", "親友盜用"});
   }
   else if (wp.colEq("conf_case_type", "MO/TO-3D") || wp.colEq("conf_case_type", "MO/TO-非3D")) {
      ls_option = ddlbOption(new String[]{"", "卡號抄錄", "簽單外流", "駭客入侵", "CreditMaster", "資訊人員涉案"});
   }
   else if (wp.colEq("conf_case_type", "Multi-Imprint")) {
      ls_option = ddlbOption(new String[]{"", "EDC", "CAT"});
   }
   else if (wp.colEq("conf_case_type", "NRI")) {
      ls_option = ddlbOption(new String[]{"", "樓管人員涉案", "郵件未送達", "郵件遭竊", "持假證件領取", "他人冒領"});
   }

   wp.colSet("OPTION_LIST_2", ls_option);

   read_rskm3210_detl();

}

void read_rskm3210_detl() throws Exception {
   kk1 = wp.itemStr("card_no");
   wp.selectSQL = "hex(rowid) as B_rowid,"
         + "card_no,"
         + "frman_name,"
         + "frman_idno,"
         + "frman_birdate,"
         + "fr_remark,"
         + "no_fraud_man"
   ;

   wp.daoTable = "rsk_ctfi_frman";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "card_no");
   wp.whereOrder = " order by card_no, frman_idno";

   pageQuery();
   if (sqlNotFind()) {
      selectOK();
      wp.colSet("IND_NUM", "0");
      return;
   }
   wp.setListCount(1);
   //wp.log("call-log="+wp.selectCnt+", sqlnrow="+this.sqlRowNum);
   wp.colSet("IND_NUM", "" + wp.selectCnt);

}

void rskm3220_read() throws Exception {
   kk1 = wp.itemStr("card_no");

   if (empty(kk1)) {
      alertErr("卡號不可空白 !");
      return;
   }

   Rskm3220Func func = new Rskm3220Func();
   func.setConn(wp);

   rc = func.dataSelect();
   if (rc != 1) {
      selectD6amt();
      selectOK();
      return;
   }
}

void selectD6amt() throws Exception  {
   //--D6 帳戶額度
   String sql1 = "select line_of_credit_amt from act_acno"
         + " where acno_p_seqno in (select acno_p_seqno from crd_card where card_no = ? )";
   sqlSelect(sql1, new Object[]{kk1});
   if (sqlRowNum > 0) {
      wp.colSet("d6_amt", sqlStr("line_of_credit_amt"));
   }
   else {
      String sql2 = "select line_of_credit_amt from dba_acno where p_seqno in (select p_seqno from dbc_card where card_no = ? )";
      sqlSelect(sql2, new Object[]{kk1});
      if (sqlRowNum > 0) {
         wp.colSet("d6_amt", sqlStr("line_of_credit_amt"));
      }
   }
   //--D1 若是子卡則帶出子卡額度，若不是子卡直接 0
   String sql2 = " select indiv_crd_lmt from crd_card where card_no = ? and son_card_flag ='Y' ";
   sqlSelect(sql2, new Object[]{kk1});

   if (sqlRowNum > 0) {
      wp.colSet("d1_amt", sqlStr("indiv_crd_lmt"));
   }
   else {
      wp.colSet("d1_amt", "0");
   }
}


void rskm3180_update() throws Exception {
   int ll_ok = 0, ll_err = 0;


   Rskm3180Func func2 = new Rskm3180Func();
   func2.setConn(wp);
   String[] aa_rowid = wp.itemBuff("rowid");
   String[] aa_opt = wp.itemBuff("opt");
   String[] aa_calldate = wp.itemBuff("call_date");
   String[] aa_calltime = wp.itemBuff("call_time");
   String[] aa_callman = wp.itemBuff("call_man");
   String[] aa_calltel = wp.itemBuff("call_telno");
   String[] aa_calldesc = wp.itemBuff("call_desc");
   String[] aa_calldesc2 = wp.itemBuff("call_desc02");
   String[] aa_calldesc3 = wp.itemBuff("call_desc03");
   String[] aa_tel_no2 = wp.itemBuff("tel_no2");
   String[] aa_old_data = wp.itemBuff("old_data");
   wp.listCount[0] = aa_rowid.length;
   wp.colSet("IND_NUM", "" + aa_rowid.length);

   func2.varModxxx(wp.loginUser, "rskm3180", "1");
   func2.varsSet("card_no", wp.itemStr("card_no"));
   for (int ll = 0; ll < aa_rowid.length; ll++) {
      wp.colSet(ll, "ok_flag", "");

      //-option-ON: delete-
      if (checkBoxOptOn(ll, aa_opt)) {
         func2.varsSet("rowid", aa_rowid[ll]);
         if (func2.dbDelete() != 1) {
            wp.colSet(ll, "ok_flag", "x");
            ll_err++;
         }
         else ll_ok++;
         continue;
      }

//		if (empty(aa_rowid[ll])==false) {
//			continue;
//		}

      func2.varsSet("call_date", aa_calldate[ll]);
      func2.varsSet("call_time", aa_calltime[ll]);
      func2.varsSet("call_man", aa_callman[ll]);
      func2.varsSet("call_telno", aa_calltel[ll]);
      func2.varsSet("call_desc", aa_calldesc[ll]);
      func2.varsSet("call_desc02", aa_calldesc2[ll]);
      func2.varsSet("call_desc03", aa_calldesc3[ll]);
      func2.varsSet("tel_no2", aa_tel_no2[ll]);
      //--+ "call_man||call_telno||tel_no2||call_desc||call_desc02||call_desc03 as old_data , "
      if (empty(aa_rowid[ll]) == false) {
         if (eqIgno(aa_old_data[ll], aa_callman[ll] + aa_calltel[ll] + aa_tel_no2[ll] + aa_calldesc[ll] + aa_calldesc2[ll] + aa_calldesc3[ll]))
            continue;

         func2.varsSet("rowid", aa_rowid[ll]);
         if (func2.dbUpdate() == 1) {
            ll_ok++;
         }
         else {
            ll_err++;
            wp.colSet(ll, "ok_flag", "x");
         }
      }
      else {
         if (func2.dbInsert() == 1) {
            ll_ok++;
         }
         else {
            ll_err++;
            wp.colSet(ll, "ok_flag", "x");
         }
      }

   }

   if (ll_ok > 0) {
      sqlCommit(1);
   }

   alertMsg("資料存檔處理完成; OK=" + ll_ok + ", ERR=" + ll_err);
   rskm3180_read();
}

void rskm3210_update() throws Exception {
   int ll_ok = 0, ll_err = 0;
   int ii = 0;
   Rskm3210Func func3 = new Rskm3210Func();
   func3.setConn(wp);
   func3.dbUpdate();

	/*
      <input type="hidden" name="ser_num" value="${ser_num}">
      <input type="hidden" name="rowid" value="${rowid}" >
      <input type="hidden" name="no_fraud_man" value="${no_fraud_man}">
      <input type="hidden" name="frman_name" value="${frman_name}">
      <input type="hidden" name="frman_idno" value="${frman_idno}">
      <input type="hidden" name="frman_birdate" value="${frman_birdate}">
      <input type="hidden" name="fr_remark" value="${fr_remark}">
      <input type="hidden" name="old_data" value="${no_fraud_man},${frman_name},${frman_idno},${frman_birdate},${fr_remark}">

 * */
   String[] aa_rowid = wp.itemBuff("B_rowid");
   String[] aa_opt = wp.itemBuff("opt");
   String[] aa_cn = wp.itemBuff("card_no");
//	String[] aa_nfm =wp.itemBuff("no_fraud_man");
   String[] aa_fn = wp.itemBuff("frman_name");
   String[] aa_fi = wp.itemBuff("frman_idno");
   String[] aa_fb = wp.itemBuff("frman_birdate");
   String[] aa_fr = wp.itemBuff("fr_remark");
   String[] aa_olddata = wp.itemBuff("old_data");

   wp.listCount[0] = wp.itemRows("B_rowid");
   wp.colSet("IND_NUM", "" + aa_rowid.length);

   func3.varModxxx(wp.loginUser, "rskm3210", "1");
   func3.varsSet("card_no", wp.itemStr("card_no"));
   for (int ll = 0; ll < wp.itemRows("B_rowid"); ll++) {
      wp.colSet(ll, "ok_flag", "");

      //-option-ON: delete-
      if (checkBoxOptOn(ll, aa_opt)) {
         func3.varsSet("rowid", aa_rowid[ll]);
         if (func3.dbDelete_dtl() != 1) {
            wp.colSet(ll, "ok_flag", "x");
            ll_err++;
         }
         else ll_ok++;
         continue;

      }
      //-no-update-
      if (empty(aa_rowid[ll]) == false) {
         continue;
      }
      func3.varsSet("card_no", wp.itemStr("card_no"));
      if (isEmpty(aa_fn[ll]) && isEmpty(aa_fi[ll])) {
         func3.varsSet("no_fraud_man", "Y");
      }
      else {
         func3.varsSet("no_fraud_man", "N");
      }
      func3.varsSet("frman_name", aa_fn[ll]);
      func3.varsSet("frman_idno", aa_fi[ll]);
      func3.varsSet("frman_birdate", aa_fb[ll]);
      func3.varsSet("fr_remark", aa_fr[ll]);

      if (func3.dbInsert_dtl() == 1) {
         ll_ok++;
      }
      else {
         ll_err++;
         wp.colSet(ll, "ok_flag", "x");
      }
   }
   if (ll_ok > 0) {
      sqlCommit(1);
   }

   alertMsg("存檔完成");
   rskm3210_read();
}


@Override
public void pdfPrint() throws Exception {
   wp.reportId = "rskm3170";

   wp.pageRows = 9999;
   int li_rc =pdfRead1();
   if (li_rc <= 0) {
      alertErr("無資料可列印 , 卡號:" + wp.itemStr("card_no"));
      wp.respHtml = "TarokoErrorPDF";
      return;
   }

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.pageVert = true;
   pdf.excelTemplate = "rskm3170_1.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

public void pdfPrint2() throws Exception {
   wp.reportId = "rskm3170";

   wp.pageRows = 9999;
   int li_rc=pdfRead2();

   if (li_rc <= 0) {
      alertErr("無資料可列印 , 卡號:" + wp.itemStr("card_no"));
      wp.respHtml = "TarokoErrorPDF";
      return;
   }

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.pageVert = true;
   pdf.excelTemplate = "rskm3170_2.xlsx";
   pdf.pageCount = 40;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

public void pdfPrint3() throws Exception {
   wp.reportId = "rskm3170";

   wp.pageRows = 9999;
   pdfRead3();

   if (wp.listCount[0] == 0) {
      alertErr("無資料可列印 , 卡號:" + wp.itemStr("card_no"));
      wp.respHtml = "TarokoErrorPDF";
      return;
   }

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.pageVert = true;
   pdf.excelTemplate = "rskm3170_3.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

public void pdfPrint4() throws Exception {
   wp.reportId = "rskm3170";

   wp.pageRows = 9999;
   pdfRead4();

   if (ii_print_cnt <= 0) {
      alertErr("無資料可列印 , 卡號:" + wp.itemStr("card_no"));
      wp.respHtml = errPagePDF;
      return;
   }

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.pageVert = true;
   pdf.excelTemplate = "rskm3170_4.xlsx";
   pdf.pageCount = 1;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

public void pdfPrint5() throws Exception {
   wp.reportId = "rskm3170";

   wp.pageRows = 9999;
   pdfRead5();

   if (wp.listCount[0] == 0) {
      alertErr("無資料可列印 , 卡號:" + wp.itemStr("card_no"));
      wp.respHtml = "TarokoErrorPDF";
      return;
   }

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskm3170_5.xlsx";
   pdf.pageCount = 15;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

int pdfRead1() throws Exception {
   wp.listCount[0] =0;

   String sql1 = " select "
         + " uf_idno_id2(a.card_no,'') as id_no , "
         + " a.id_p_seqno ,"
         + " a.case_date ,"
         + " a.case_seqno ,"
         + " a.card_no ,"
         + " a.group_code ,"
         + " substr(a.new_end_date,1,6) as new_end_date ,"
         + " a.case_type ,"
         + " a.case_source ,"
         + " a.fraud_ok_cnt ,"
         + " a.fraud_ok_amt , "
         + " a.risk_bank_no , "
         + " b.fra_fir_date ,"
         + " b.cntl_date ,"
         + " b.source_type ,"
         + " b.susp_poct ,"
         + " b.poc_terminal ,"
         + " b.skim_type ,"
         + " b.skim_period ,"
         + " b.cntl_remark "
         + " from rsk_ctfi_warn b left join rsk_ctfi_case a on b.card_no = a.card_no "
         + " where a.card_no = ? ";

   sqlSelect(sql1, new Object[]{wp.itemStr("card_no")});
   if (sqlRowNum <=0)
      return -1;

   if (sqlRowNum > 0) {
      String sql2 = " select "
            + " A.chi_name , "
            + " A.birthday , "
            + " substr(B.new_end_date,1,6) as new_end_date , "
            + " B.group_code , "
            + " (select risk_bank_no from act_acno where acno_p_seqno =B.acno_p_seqno) as risk_bank_no "
            + " from crd_idno A join crd_card B on A.id_p_seqno = B.id_p_seqno "
            + " where A.id_p_seqno = ? "
            + " and B.card_no = ? ";

      sqlSelect(sql2, new Object[]{sqlStr("id_p_seqno"), wp.itemStr("card_no")});
      if (sqlRowNum <= 0) {
         String sql3 = " select "
               + " A.chi_name , "
               + " A.birthday , "
               + " substr(B.new_end_date,1,6) as new_end_date , "
               + " B.group_code , "
               + " (select risk_bank_no from dba_acno where p_seqno =B.p_seqno) as risk_bank_no "
               + " from dbc_idno A join dbc_card B on A.id_p_seqno = B.id_p_seqno "
               + " where A.id_p_seqno = ? "
               + " and B.card_no = ? ";

         sqlSelect(sql3, new Object[]{sqlStr("id_p_seqno"), wp.itemStr("card_no")});
      }

      String sql4 = "select full_chi_name "
            + " from gen_brn"
            + " where branch =? ";
      sqlSelect(sql4, new Object[]{sqlStr("risk_bank_no")});


      wp.colSet("ex_case_seqno", sqlStr("case_seqno"));
      wp.colSet("ex_case_date", sqlStr("case_date"));
      wp.colSet("ex_id_no", sqlStr("id_no"));
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
      wp.colSet("ex_birthday", sqlStr("birthday"));
      wp.colSet("ex_card_no", sqlStr("card_no"));
      wp.colSet("ex_group_code", sqlStr("group_code"));
      wp.colSet("ex_new_end_date", sqlStr("new_end_date"));
      wp.colSet("ex_case_type", sqlStr("case_type"));
      wp.colSet("ex_case_source", sqlStr("case_source"));
      wp.colSet("ex_fraud_ok_cnt", sqlStr("fraud_ok_cnt"));
      wp.colSet("ex_fraud_ok_amt", sqlStr("fraud_ok_amt"));
      wp.colSet("ex_fra_fir_date", sqlStr("fra_fir_date"));
      wp.colSet("ex_cntl_date", sqlStr("cntl_date"));
      wp.colSet("ex_source_type", sqlStr("source_type"));
      wp.colSet("ex_susp_poct", sqlStr("susp_poct"));
      wp.colSet("ex_poc_terminal", sqlStr("poc_terminal"));
      wp.colSet("ex_skim_period", sqlStr("skim_period"));
      wp.colSet("ex_skim_type", sqlStr("skim_type"));
      wp.colSet("ex_cntl_remark", sqlStr("cntl_remark"));
      wp.colSet("ex_risk_bank_no", sqlStr("risk_bank_no") + "." + sqlStr("full_chi_name"));
   }
   else {
      wp.colSet("ex_case_seqno", wp.itemStr("case_seqno"));
      wp.colSet("ex_case_date", wp.itemStr("case_date"));
      wp.colSet("ex_id_no", wp.itemStr("db_idno"));
      wp.colSet("ex_chi_name", wp.itemStr("db_chi_name"));
      wp.colSet("ex_birthday", wp.itemStr("db_bir_date"));
      wp.colSet("ex_card_no", wp.itemStr("card_no"));
      wp.colSet("ex_group_code", wp.itemStr("group_code"));
      wp.colSet("ex_new_end_date", wp.itemStr("new_end_date"));
      wp.colSet("ex_risk_bank_no", wp.itemStr("risk_bank_no") + wp.itemStr("tt_risk_bank_no"));
      wp.colSet("ex_case_type", wp.itemStr("case_type"));
      wp.colSet("ex_case_source", wp.itemStr("case_source"));
      wp.colSet("ex_fraud_ok_cnt", wp.itemStr("fraud_ok_cnt"));
      wp.colSet("ex_fraud_ok_amt", wp.itemStr("fraud_ok_amt"));
   }

   wp.listCount[0] = 1;
   return 1;
}

int pdfRead2() throws Exception {
   String sql1 = " select "
         + " A.call_date ,"
         + " A.call_time ,"
         + " A.tel_no ,"
         + " A.call_man ,"
         + " A.call_desc ,"
         + " A.call_desc02 ,"
         + " A.call_desc03 ,"
         + " A.tel_no2 , "
         + " A.call_telno , "
         + " uf_idno_id2(B.card_no,'') as id_no , "
         + " replace(uf_idno_name2(B.card_no,''),'　','') as chi_name , "
         + " B.case_seqno ,"
         + " B.card_no , "
         + " B.id_p_seqno ,  "
         + " decode((select usr_cname from sec_user where usr_id =A.mod_user),'',A.mod_user,(select usr_cname from sec_user where usr_id =A.mod_user)) as tt_mod_user "
         + " from rsk_ctfi_call_log A join rsk_ctfi_case B on A.card_no = B.card_no "
         + " where B.card_no = ? "
         + " order by A.call_date Desc , A.call_time Desc ";
   sqlSelect(sql1, new Object[]{wp.itemStr("card_no")});

   if (sqlRowNum <= 0) {
      errmsg("無資料可列印 , 卡號:" + wp.itemStr("card_no"));
      wp.respHtml = "TarokoErrorPDF";
      return -1;
   }

   int il_select_cnt = sqlRowNum;

//	if(!empty(sqlStr("id_p_seqno"))){
//		String sql2 = " select chi_name from crd_idno where id_p_seqno = ? ";
//		sqlSelect(sql2,new Object[]{sqlStr("id_p_seqno")});
//	}

   for (int ii = 0; ii < il_select_cnt; ii++) {
      if (ii == 0) {
         wp.colSet("ex_case_seqno", sqlStr(ii, "case_seqno"));
         wp.colSet("ex_id_no", sqlStr(ii, "id_no"));
         wp.colSet("ex_card_no", sqlStr(ii, "card_no"));
         wp.colSet("ex_chi_name", sqlStr(ii, "chi_name"));
         wp.colSet("report_user", wp.loginUser);
      }

      byte[] ss = sqlStr(ii, "call_desc").getBytes();
      wp.colSet(ii, "call_date", sqlStr(ii, "call_date"));
      wp.colSet(ii, "call_time", sqlStr(ii, "call_time"));
      wp.colSet(ii, "tel_no", sqlStr(ii, "tel_no"));
      wp.colSet(ii, "tel_no2", sqlStr(ii, "tel_no2"));
      wp.colSet(ii, "call_telno", sqlStr(ii, "call_telno"));
      wp.colSet(ii, "call_man", sqlStr(ii, "call_man"));
      wp.colSet(ii, "call_desc1", sqlStr(ii, "call_desc"));
      wp.colSet(ii, "call_desc2", sqlStr(ii, "call_desc02"));
      wp.colSet(ii, "call_desc3", sqlStr(ii, "call_desc03"));
      wp.colSet(ii, "ex_mod_user", sqlStr(ii, "tt_mod_user"));


   }

   wp.listCount[0] = il_select_cnt;
   return 1;
}

String selectCurrCode(String ls_curr_code) throws Exception  {

   String sql1 = "select curr_eng_name from ptr_currcode where curr_code = ?";
   sqlSelect(sql1, new Object[]{ls_curr_code});

   if (sqlRowNum <= 0) return ls_curr_code;

   return sqlStr("curr_eng_name");
}

void pdfRead3() throws Exception {

   String[] aa_opt = wp.itemBuff("opt");
   String[] ls_tx_date = wp.itemBuff("A-txn_date");
   String[] ls_tx_time = wp.itemBuff("A-txn_time");
   String[] ls_mcht_name = wp.itemBuff("A-mcht_name");
   String[] ls_mcht_ename = wp.itemBuff("A-mcht_ename");
   String[] ls_txn_amt = wp.itemBuff("A-txn_amt");
   String[] ls_resp_code = wp.itemBuff("A-resp_code");
   String[] ls_source_curr = wp.itemBuff("A-source_curr");
   String[] ls_source_amt = wp.itemBuff("A-source_amt");
   int il_select_cnt = 0, zz = -1;
   wp.colSet("ex_card_no", wp.itemStr("card_no"));
   for (int ii = 0; ii < wp.itemRows("A-card_no"); ii++) {
      if (!checkBoxOptOn(ii, aa_opt)) continue;
      il_select_cnt++;
      zz++;
      wp.colSet(zz, "ex_txn_date", ls_tx_date[ii]);
      wp.colSet(zz, "ex_txn_time", ls_tx_time[ii]);
      if (!empty(ls_mcht_name[ii].replace("　", " ").trim())) {
         wp.colSet(zz, "ex_mcht_name", ls_mcht_name[ii]);
      }
      else {
         wp.colSet(zz, "ex_mcht_name", ls_mcht_ename[ii]);
      }
      wp.colSet(zz, "ex_txn_amt", ls_txn_amt[ii]);
      wp.colSet(zz, "ex_source_curr", selectCurrCode(ls_source_curr[ii]));
      wp.colSet(zz, "ex_source_amt", ls_source_amt[ii]);
      if (eqIgno(ls_resp_code[ii], "00") || eqIgno(ls_resp_code[ii], "85")) {
         wp.colSet(zz, "ex_resp_code", "成功");
      }
      else {
         wp.colSet(zz, "ex_resp_code", "失敗");
      }
      wp.colSet(zz, "ex_tl_cnt", "1");
   }

   if (il_select_cnt == 0) {
      String sql1 = " select "
            + " card_no , "
            + " txn_date , "
            + " txn_time , "
            + " mcht_name , "
            + " mcht_ename , "
            + " txn_amt , "
            + " source_amt , "
            + " source_curr , "
            + " resp_code "
            + " from rsk_ctfi_txn "
            + " where 1=1 "
            + " and card_no = ? "
            + " order by txn_date Asc , txn_time Asc ";

      sqlSelect(sql1, new Object[]{wp.itemStr("card_no")});

      if (sqlRowNum <= 0) {

         return;
      }

      il_select_cnt = sqlRowNum;

      for (int ii = 0; ii < il_select_cnt; ii++) {
         wp.colSet(ii, "ex_txn_date", sqlStr(ii, "txn_date"));
         wp.colSet(ii, "ex_txn_time", sqlStr(ii, "txn_time"));
         if (!empty(sqlStr(ii, "mcht_name").replace("　", " ").trim())) {
            wp.colSet(ii, "ex_mcht_name", sqlStr(ii, "mcht_name"));
         }
         else {
            wp.colSet(ii, "ex_mcht_name", sqlStr(ii, "mcht_ename"));
         }

         wp.colSet(ii, "ex_txn_amt", sqlStr(ii, "txn_amt"));
         wp.colSet(ii, "ex_source_curr", selectCurrCode(sqlStr(ii, "source_curr")));
         if (empty(sqlStr(ii, "source_amt"))) {
            wp.colSet(ii, "ex_source_amt", "");
         }
         else {
            wp.colSet(ii, "ex_source_amt", sqlStr(ii, "source_amt"));
         }

         if (eqIgno(sqlStr(ii, "resp_code"), "00") || eqIgno(sqlStr(ii, "resp_code"), "85")) {
            wp.colSet(ii, "ex_resp_code", "成功");
         }
         else {
            wp.colSet(ii, "ex_resp_code", "失敗");
         }
         wp.colSet(ii, "ex_tl_cnt", "1");
      }
   }
   wp.colSet(il_select_cnt, "ex_mcht_name", "--以下無資料--");
   String ex_47 = "";
   ex_47 = "如有疑問，請電                         轉";
   if (wp.itemEmpty("ex_tel_no")) {
      ex_47 += "　　　　分機";
   }
   else {
      ex_47 += "　" + wp.itemStr("ex_tel_no") + "　分機";
   }
   if (wp.itemEmpty("ex_tel_user")) {
      ex_47 += "　　　　　　專員洽詢，謝謝。";
   }
   else {
      ex_47 += "　　" + wp.itemStr("ex_tel_user") + "　　專員洽詢，謝謝。";
   }
   wp.colSet("ex_47", ex_47);
   wp.colSet("ex20", "＊信用卡背面是否已簽名 □ 是　□ 否");
   wp.colSet("ex21", "＊信用卡是否已遺失　　 □ 是　□ 否");

   if (il_select_cnt % 30 > 10) {
      il_select_cnt += ((30 - il_select_cnt % 30) + 1);
   }
   else if (il_select_cnt % 30 == 0) {
      il_select_cnt += 1;
   }

   wp.listCount[0] = il_select_cnt;
}

void pdfRead4() throws Exception {
   ii_print_cnt =0;

   String sql0 = " select "
         + " frman_name , "
         + " frman_idno "
         + " from rsk_ctfi_frman "
         + " where card_no = ? and no_fraud_man = 'N' ";

   sqlSelect(sql0, new Object[]{wp.itemStr("card_no")});

   if (sqlRowNum > 0) {
      wp.colSet("ex_29O", sqlStr(0, "frman_name"));
      wp.colSet("ex_30O", sqlStr(1, "frman_name"));
      wp.colSet("ex_31O", sqlStr(2, "frman_name"));
      wp.colSet("ex_32O", sqlStr(3, "frman_name"));
      wp.colSet("ex_29Q", sqlStr(0, "frman_idno"));
      wp.colSet("ex_30Q", sqlStr(1, "frman_idno"));
      wp.colSet("ex_31Q", sqlStr(2, "frman_idno"));
      wp.colSet("ex_32Q", sqlStr(3, "frman_idno"));
   }
   else {
      wp.colSet("ex_29Q", "本案經查証屬實係遭冒用");
      wp.colSet("ex_30Q", "惟無特定冒用對象，故免依");
      wp.colSet("ex_31Q", "辦法第24條規定進行民事法");
      wp.colSet("ex_32Q", "律訴追程序");
   }

   String sql1 = " select "
         + " uf_idno_id2(A.card_no,'') as id_no ,"
         + " replace(uf_idno_name2(A.card_no,''),'　','') as chi_name ,"
         + " A.case_date ,"
         + " A.case_seqno ,"
         + " A.card_no ,"
         + " A.group_code ,"
         + " A.case_type ,"
         + " A.case_source ,"
         + " A.fraud_ok_cnt ,"
         + " A.fraud_ok_amt ,"
         + " A.friend_fraud_flag ,"
         + " A.ch_moral_flag ,"
         + " A.no_fraud_flag ,"
         + " A.actual_amt ,"
         + " A.for_ch_amt ,"
         + " A.ch_deuct_amt ,"
         + " A.for_fraud_amt ,"
         + " A.for_mcht_amt ,"
         + " A.cb_ok_amt ,"
         + " A.non_disput_amt ,"
         + " A.unbill_amt ,"
         + " A.turn_miscell_amt ,"
         + " A.fraud_area ,"
         + " A.disput_sign ,"
         + " A.conf_case_type ,"
         + " A.modus_oper ,"
         + " A.poc ,"
         + " A.adj_limit_code ,"
         + " A.adj_limit ,"
         + " A.un_adj_reason ,"
         + " A.survey_result ,"
         + " A.survey_result2 ,"
         + " A.survey_result3 ,"
         + " A.survey_result4 ,"
         + " A.survey_result5 ,"
         + " A.survey_result6 ,"
         + " A.survey_result7 ,"
         + " A.survey_result8 ,"
         + " A.survey_result9 ,"
         + " A.survey_result10 ,"
         + " A.for_ch_amt+A.ch_deuct_amt+A.for_mcht_amt+A.cb_ok_amt+A.non_disput_amt+A.unbill_amt+A.for_fraud_amt as wk_bank_no_load ,"
         + " A.reissue_card_flag ,"
         + " B.fra_fir_date ,"
         + " B.cntl_date ,"
         + " B.cntl_user ,"
         + " B.source_type ,"
         + " B.cris_ns_alerts ,"
         + " B.n_rec_alerts ,"
         + " B.first_score ,"
         + " B.imput_cnt ,"
         + " B.h_score , "
         + " B.susp_poct "
         + " from rsk_ctfi_case A left join rsk_ctfi_warn B on B.card_no = A.card_no "
         + " where A.card_no = ? ";

   sqlSelect(sql1, new Object[]{wp.itemStr("card_no")});

   if (sqlRowNum <= 0) {
      return;
   }

   wp.colSet("ex_id_no", sqlStr("id_no"));
   wp.colSet("ex_chi_name", sqlStr("chi_name"));
   wp.colSet("ex_case_date", sqlStr("case_date"));
   wp.colSet("ex_case_seqno", sqlStr("case_seqno"));
   wp.colSet("ex_card_no", sqlStr("card_no"));
   wp.colSet("ex_group_code", sqlStr("group_code"));
   wp.colSet("ex_case_type", sqlStr("case_type"));
   wp.colSet("ex_case_source", sqlStr("case_source"));
   wp.colSet("ex_fraud_ok_cnt", sqlStr("fraud_ok_cnt"));
   wp.colSet("ex_fraud_ok_amt", sqlStr("fraud_ok_amt"));
   if (eqIgno(sqlStr("friend_fraud_flag"), "1")) {
      wp.colSet("ex_29G", "親友冒用");
   }
   else if (eqIgno(sqlStr("friend_fraud_flag"), "2")) {
      wp.colSet("ex_29G", "C/H 道德風險");
   }
   else if (eqIgno(sqlStr("friend_fraud_flag"), "3")) {
      wp.colSet("ex_29G", "非偽冒案件");
   }

   if (eqIgno(sqlStr("ch_moral_flag"), "Y")) {
      wp.colSet("ex_ch_moral_flag", "■");
   }
   else {
      wp.colSet("ex_ch_moral_flag", "□");
   }

   if (eqIgno(sqlStr("no_fraud_flag"), "Y")) {
      wp.colSet("ex_no_fraud_flag", "■");
   }
   else {
      wp.colSet("ex_no_fraud_flag", "□");
   }

   if (eqIgno(sqlStr("reissue_card_flag"), "Y")) {
      wp.colSet("ex28L", "■ 補發新卡");
   }
   else {
      wp.colSet("ex28L", "□ 補發新卡");
   }

   wp.colSet("ex_actual_amt", sqlStr("actual_amt"));
   wp.colSet("ex_for_ch_amt", sqlStr("for_ch_amt"));
   wp.colSet("ex_ch_deuct_amt", sqlStr("ch_deuct_amt"));
   wp.colSet("ex_for_fraud_amt", sqlStr("for_fraud_amt"));
   wp.colSet("ex_for_mcht_amt", sqlStr("for_mcht_amt"));
   wp.colSet("ex_cb_ok_amt", sqlStr("cb_ok_amt"));
   wp.colSet("ex_54", sqlStr("cb_ok_amt"));
   wp.colSet("ex_non_disput_amt", sqlStr("non_disput_amt"));
   wp.colSet("ex_unbill_amt", sqlStr("unbill_amt"));
   wp.colSet("ex_wk_bank_no_load", sqlStr("wk_bank_no_load"));
   wp.colSet("ex_turn_miscell_amt", sqlStr("turn_miscell_amt"));
   wp.colSet("ex_fraud_area", sqlStr("fraud_area"));
   wp.colSet("ex_disput_sign", sqlStr("disput_sign"));
   wp.colSet("ex_conf_case_type", sqlStr("conf_case_type"));
   wp.colSet("ex_modus_oper", sqlStr("modus_oper"));
   wp.colSet("ex_poc", sqlStr("susp_poct"));
   wp.colSet("ex_adj_limit_code", sqlStr("adj_limit_code"));
   wp.colSet("ex_adj_limit", sqlStr("adj_limit"));
   wp.colSet("ex_un_adj_reason", sqlStr("un_adj_reason"));
   wp.colSet("ex_survey_result1", sqlStr("survey_result2"));
   wp.colSet("ex_survey_result2", sqlStr("survey_result3"));
   wp.colSet("ex_survey_result3", sqlStr("survey_result4"));
   wp.colSet("ex_survey_result4", sqlStr("survey_result5"));
   wp.colSet("ex_survey_result5", sqlStr("survey_result6"));
   wp.colSet("ex_survey_result6", sqlStr("survey_result7"));
   wp.colSet("ex_survey_result7", sqlStr("survey_result8"));
   wp.colSet("ex_survey_result8", sqlStr("survey_result9"));
   wp.colSet("ex_survey_result9", sqlStr("survey_result10"));
   wp.colSet("ex_survey_result", sqlStr("survey_result"));
   wp.colSet("ex_fra_fir_date", sqlStr("fra_fir_date"));
   wp.colSet("ex_cntl_date", sqlStr("cntl_date"));
   wp.colSet("ex_cntl_user", sqlStr("cntl_user"));
   wp.colSet("ex_source_type", sqlStr("source_type"));
   wp.colSet("ex_cris_ns_alerts", sqlStr("cris_ns_alerts"));
   wp.colSet("ex_n_rec_alerts", sqlStr("n_rec_alerts"));
   wp.colSet("ex_first_score", sqlStr("first_score"));
   wp.colSet("ex_imput_cnt", sqlStr("imput_cnt"));
   wp.colSet("ex_h_score", sqlStr("h_score"));
   wp.colSet("ex_risk_bank_no", wp.itemStr("risk_bank_no") + wp.itemStr("tt_risk_bank_no"));

   wp.listCount[0] = 1;
   ii_print_cnt =1;
}

void pdfRead5() throws Exception {

   String[] aa_opt = wp.itemBuff("opt");
   String[] ls_tx_date = wp.itemBuff("A-txn_date");
   String[] ls_tx_time = wp.itemBuff("A-txn_time");
   String[] ls_mcht_name = wp.itemBuff("A-mcht_name");
   String[] ls_mcht_ename = wp.itemBuff("A-mcht_ename");
   String[] ls_term_id = wp.itemBuff("A-term_id");
   String[] ls_txn_amt = wp.itemBuff("A-txn_amt");
   String[] ls_resp_code = wp.itemBuff("A-resp_code");
   String[] ls_mcht_addr = wp.itemBuff("A-mcht_addr");
   String[] ls_mcht_tel = wp.itemBuff("A-mcht_tel_no");
   int il_select_cnt = 0, zz = -1;
   wp.colSet("ex_card_no", wp.itemStr("card_no"));
   for (int ii = 0; ii < wp.itemRows("A-card_no"); ii++) {
      if (!checkBoxOptOn(ii, aa_opt)) continue;
      il_select_cnt++;
      zz++;
      wp.colSet(zz, "ex_txn_date", ls_tx_date[ii]);
      wp.colSet(zz, "ex_txn_time", ls_tx_time[ii]);
      if (!empty(ls_mcht_name[ii].replace("　", " ").trim())) {
         wp.colSet(zz, "ex_mcht_name", ls_mcht_name[ii]);
      }
      else {
         wp.colSet(zz, "ex_mcht_name", ls_mcht_ename[ii]);
      }
      wp.colSet(zz, "ex_term_id", ls_term_id[ii]);
      wp.colSet(zz, "ex_txn_amt", ls_txn_amt[ii]);
      wp.colSet(zz, "ex_mcht_addr", ls_mcht_addr[ii]);
      wp.colSet(zz, "ex_mcht_tel_no", ls_mcht_tel[ii]);
      if (eqIgno(ls_resp_code[ii], "00") || eqIgno(ls_resp_code[ii], "85")) {
         wp.colSet(zz, "ex_resp_code", "成功");
      }
      else {
         wp.colSet(zz, "ex_resp_code", "失敗");
      }
   }

   if (il_select_cnt == 0) {
      String sql1 = " select "
            + " card_no ,"
            + " txn_date ,"
            + " txn_time ,"
            + " mcht_no ,"
            + " mcht_name ,"
            + " mcht_ename ,"
            + " term_id ,"
            + " mcht_addr ,"
            + " mcht_tel_no ,"
            + " txn_amt , "
            + " resp_code "
            + " from rsk_ctfi_txn "
            + " where 1=1 "
//						+ " and resp_code ='00' "
            + " and card_no = ? "
            + " order by txn_date Asc , txn_time Asc ";

      sqlSelect(sql1, new Object[]{wp.itemStr("card_no")});

      if (sqlRowNum <= 0) {
         errmsg("無資料可列印 , 卡號:" + wp.itemStr("card_no"));
         wp.respHtml = "TarokoErrorPDF";
         return;
      }

      il_select_cnt = sqlRowNum;

      for (int ii = 0; ii < il_select_cnt; ii++) {
         wp.colSet(ii, "ex_txn_date", sqlStr(ii, "txn_date"));
         wp.colSet(ii, "ex_txn_time", sqlStr(ii, "txn_time"));
         if (!empty(sqlStr(ii, "mcht_name").replace("　", " ").trim())) {
            wp.colSet(ii, "ex_mcht_name", sqlStr(ii, "mcht_name"));
         }
         else {
            wp.colSet(ii, "ex_mcht_name", sqlStr(ii, "mcht_ename"));
         }

         wp.colSet(ii, "ex_term_id", sqlStr(ii, "term_id"));
         wp.colSet(ii, "ex_txn_amt", sqlStr(ii, "txn_amt"));
         wp.colSet(ii, "ex_mcht_addr", sqlStr(ii, "mcht_addr"));
         wp.colSet(ii, "ex_mcht_tel_no", sqlStr(ii, "mcht_tel_no"));
         if (eqIgno(sqlStr(ii, "resp_code"), "00") || eqIgno(sqlStr(ii, "resp_code"), "85")) {
            wp.colSet(ii, "ex_resp_code", "成功");
         }
         else {
            wp.colSet(ii, "ex_resp_code", "失敗");
         }
//   		wp.colSet(ii,"ex_resp_code", "成功");
      }

   }

   String ex_14 = "";
   ex_14 = "　案件處理專員：";
   if (wp.itemEmpty("ex_tel_user")) {
      ex_14 += "　　　　　　　專員　　電話                                           ";
   }
   else {
      ex_14 += "　　" + wp.itemStr("ex_tel_user") + "　　專員　　電話                                           ";
   }
   if (wp.itemEmpty("ex_tel_no")) {
      ex_14 += "　　　　　分機　地址：                                                                                                    ";
   }
   else {
      ex_14 += "　" + wp.itemStr("ex_tel_no") + "　　分機　地址：                                                                                              ";
   }

   wp.colSet("ex_14", ex_14);
   wp.colSet("ex_case_seqno", wp.itemStr("case_seqno"));

   wp.listCount[0] = il_select_cnt;
}

public void processAjaxOption(TarokoCommon wr) throws Exception {
   super.wp = wr;
   daoTid = "pop_";
   wp.varRows = 200;
   wp.selectSQL = " card_no , mcht_no , mcht_name , mcht_tel_no , mcht_addr ";
   wp.daoTable = "rsk_ctfi_txn";
   wp.whereStr = "where 1=1 " + sqlCol(wp.itemStr("kk_mcht_no2"), "mcht_no", "like%");
   wp.whereOrder = " order by mcht_no fetch first 200 rows only";
//   setString("batch_no",wp.itemStr("ex_action_batch_no")+"%");
   pageQuery();
   if (sqlRowNum <= 0) {
      return;
   }


   for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.addJSON("pop_card_no", wp.colStr(ii, "pop_card_no"));
      wp.addJSON("pop_mcht_no", wp.colStr(ii, "pop_mcht_no"));
      wp.addJSON("pop_mcht_name", wp.colStr(ii, "pop_mcht_name"));
      wp.addJSON("pop_mcht_tel_no", wp.colStr(ii, "pop_mcht_tel_no"));
      wp.addJSON("pop_mcht_addr", wp.colStr(ii, "pop_mcht_addr"));
   }
   return;
}

public void proFuncBill() throws Exception {
   int ll_ok = 0, ll_err = 0, ll_cnt = 0;
   String[] ls_card_no = wp.itemBuff("card_no");
   String[] ls_reference_no = wp.itemBuff("reference_no");
   String[] aa_opt = wp.itemBuff("opt");

   wp.listCount[0] = wp.itemRows("reference_no");

   Rskm3170Func func = new Rskm3170Func(wp);

   int rr = 0;
   for (int ii = 0; ii < aa_opt.length; ii++) {
      rr = optToIndex(aa_opt[ii]);
      if (rr < 0) continue;
//		if(checkBoxOptOn(ii, aa_opt)==false)	continue;
      ll_cnt++;
      func.varsSet("card_no", ls_card_no[rr]);
      func.varsSet("reference_no", ls_reference_no[rr]);

      if (func.procFuncBill() == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ll_ok++;
         sqlCommit(1);
         continue;
      }
      else {
         wp.colSet(rr, "ok_flag", "X");
         ll_err++;
         dbRollback();
         continue;
      }

   }

   if (ll_cnt == 0) {
      alertErr("請選擇新增資料 !");
      return;
   }
   alertMsg("新增完成  , 成功:" + ll_ok + " 失敗:" + ll_err);


}

public void proFuncTxlog() throws Exception {
   int ll_ok = 0, ll_err = 0, ll_cnt = 0;
   String[] ls_auth_seqno = wp.itemBuff("auth_seqno");
   String[] ls_card_no = wp.itemBuff("card_no");
   String[] ls_nt_amt = wp.itemBuff("nt_amt");
   String[] aa_opt = wp.itemBuff("opt");

   wp.listCount[0] = wp.itemRows("auth_seqno");

   Rskm3170Func func = new Rskm3170Func(wp);

   int rr = 0;
   for (int ii = 0; ii < aa_opt.length; ii++) {
      rr = optToIndex(aa_opt[ii]);
      if (rr < 0) continue;
      ll_cnt++;
      func.varsSet("card_no", ls_card_no[rr]);
      func.varsSet("auth_seqno", ls_auth_seqno[rr]);
      func.varsSet("nt_amt", ls_nt_amt[rr]);

      if (func.procFuncTxlog() == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ll_ok++;
         sqlCommit(1);
         continue;
      }
      else {
         wp.colSet(rr, "ok_flag", "X");
         ll_err++;
         dbRollback();
         continue;
      }

   }

   if (ll_cnt == 0) {
      alertErr("請選擇新增資料 !");
      return;
   }
   alertMsg("新增完成  , 成功:" + ll_ok + " 失敗:" + ll_err);


}

public void wf_ajax_key(TarokoCommon wr) throws Exception {
   super.wp = wr;

   // String ls_winid =
   selectData(wp.itemStr("ax_area"));
   if (rc != 1) {
      wp.addJSON("fraud_area_code", "");
      wp.addJSON("fraud_cntry_code", "");
      return;
   }
   wp.addJSON("fraud_area_code", sqlStr("fraud_area_code"));
   wp.addJSON("fraud_cntry_code", sqlStr("fraud_cntry_code"));
}

void selectData(String s1) throws Exception  {
   String sql1 = " select "
         + " area_code as fraud_area_code , "
         + " cntry_code as fraud_cntry_code "
         + " from rsk_ctfi_area "
         + " where area_remark = ? ";

   sqlSelect(sql1, new Object[]{s1});

   if (sqlRowNum <= 0) {
      alertErr("地區說明不存在:" + s1);
      return;
   }

}

void rskm3180_insert() throws Exception  {

   wp.listCount[0] = wp.itemRows("call_date");

   if (wp.itemEmpty("ex_call_date") || wp.itemEmpty("ex_call_time")) {
      alertErr("記錄日期、記錄時間 不可空白");
      return;
   }

   Rskm3180Func func = new Rskm3180Func();
   func.setConn(wp);

   rc = func.dbInsert();
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else {
      wp.respMesg = "敘實記錄新增完成";
   }

}

void rskm3210_insert() throws Exception  {
   wp.listCount[0] = wp.itemRows("B_rowid");

   Rskm3210Func func = new Rskm3210Func();
   func.setConn(wp);

   rc = func.insert_dtl();
   if (rc != 1) {
      errmsg(func.getMsg());
   }
   else {
      wp.respMesg = "調查結果新增完成";
   }

}

}



	

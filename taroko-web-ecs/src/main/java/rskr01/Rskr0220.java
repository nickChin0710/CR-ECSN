package rskr01;
/**
 * 2022-0317   JH    mt9316: VD curr_code
 * 2020-1019   JH    modify
 * 2020-0428:  Alex  cond add card_no
 * 2020-0423   JH    modify
 * 2019-1211:  Alex  queryRead fix
 */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDFLine;

public class Rskr0220 extends BaseQuery implements InfacePdf {
   public String modVersion() { return "v22.0317"; }

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
//      case "X":
//         is_action = "new";
//         clearFunc(); break;
      case "Q":
         queryFunc(); break;
      case "R":
         dataRead(); break;
//      case "A":
//         insertFunc(); break;
//      case "U":
//         updateRetrieve = true;
//         updateFunc(); break;
//      case "D":
//         deleteFunc(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "L":
         strAction = "";
         clearFunc(); break;
//      case "C":
//         procFunc(); break;
      case "PDF":
         pdfPrint(); break;
//      case "XLS":  //-Excel-
//         is_action = "XLS";
//         xlsPrint(); break;
      default:
         alertErr("未指定 actionCode 執行Method, action[%s]",wp.buttonCode);
   }

   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskr0220")) {
         wp.optionKey = wp.colStr("ex_curr_code");
         dddwList("dddw_dc_curr_code_tw", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("結案登錄日期起迄：輸入錯誤");
      return;
   }

   String lsWhere =
         " where chg_stage in ('1','3') and sub_stage='30' and fst_disb_amt>0  "
               + sqlCol(wp.itemStr("ex_date1"), "fst_disb_add_date", ">=")
               + sqlCol(wp.itemStr("ex_date2"), "fst_disb_add_date", "<=")
               + sqlCol(wp.itemStr("ex_id1"), "fst_disb_add_user")
               + sqlCol(wp.itemStr("ex_curr_code"), "decode(curr_code,'','901',curr_code)")
               + sqlCol(wp.itemStr("ex_card_no"),"card_no","like%");
   if (wp.itemEq("ex_apr_flag", "Y")) {
      lsWhere += " and fst_disb_apr_date <>''";
   }
   else {
      lsWhere += " and fst_disb_apr_date = ''";
   }

   if (wp.itemEq("ex_dbcard_flag", "1")) {
      lsWhere += " and debit_flag <> 'Y' and bill_type <> 'TSCC' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "2")) {
      lsWhere += " and debit_flag = 'Y' and bill_type <> 'TSCC' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "3")) {
      lsWhere += " and bill_type = 'TSCC' ";
   }
   
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
         + " card_no , "
         + " purchase_date , "
         + " dest_amt , "
         + " ctrl_seqno , "
         + " chg_times,"
         + " fst_reason_code,"
         + " acct_type,"
         + " uf_acno_key2(card_no,acct_type) as acct_key,"
         + " fst_disb_add_date,"
         + " fst_disb_add_user,"
         + " bill_type,"
         + " film_no,"
         + " uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt , "
         + " uf_dc_amt2(fst_twd_amt,fst_dc_amt) as fst_twd_amt , "
         + " uf_dc_amt2(fst_disb_amt,fst_disb_dc_amt) as fst_disb_dc_amt , "
         + " uf_dc_curr(curr_code) as curr_code,"
         + " rpad(' ',8) as db_ac_no,"
         + " rpad(' ',20) as db_gl_memo3,"
         + " decode(bill_type,'TSCC',1,0) as db_tscc_cnt, "
         + " decode(bill_type,'TSCC',0,1) as db_card_cnt,"
         + " decode(bill_type,'TSCC',fst_dc_amt,0) as db_tscc_fstamt, "
         + " decode(bill_type,'TSCC',0,fst_dc_amt) as db_card_fstamt,"
         + " decode(bill_type,'TSCC',fst_disb_amt,0) as db_tscc_disbamt, "
         + " decode(bill_type,'TSCC',0,fst_disb_amt) as db_card_disbamt,"
         //+ "uf_hi_cardno(substr(card_no,1,16)) as wk_card_no"
         + "card_no as wk_card_no"
   ;
//			+ "acct_type||'-'||"
//			+ "uf_hi_idno(substr(uf_acno_key(p_seqno),1,10)) as wk_acct_key";
   wp.daoTable = "rsk_chgback";
   if (eqIgno(strAction,"PDF")) {
      wp.whereOrder = " order by acct_type, card_no, ctrl_seqno  ";
   }
   else {
      wp.whereOrder = " order by acct_type, card_no, ctrl_seqno  ";
   }
   pageQuery();


   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   listWkdata(sqlRowNum);
//   wk_num();
   wp.setPageValue();
}

void listWkdata(int ll_nrow) {
   for (int ii = 0; ii < ll_nrow; ii++) {
      // wp.col_set(ii,"wk_acct_key",
      // wp.colStr(ii,"acct_type")+"-"+wp.colStr(ii,"acct_key"));
      wp.colSet(ii, "wk_ctrl_seqno", wp.colStr(ii, "ctrl_seqno") + "-"
            + wp.colStr(ii, "chg_times"));
      //wp.col_set(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "-" + commString.hi_idno(wp.colStr(ii, "acct_key")));
      wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "-" + wp.colStr(ii, "acct_key"));

      double liNum = wp.colNum(ii, "fst_disb_dc_amt") - wp.colNum(ii, "fst_twd_amt");
      wp.colSet(ii, "wk_diff_amt", "" + liNum);
      double liCardDiffamt = wp.colNum(ii, "db_card_disbamt")
            - wp.colNum(ii, "db_card_fstamt");
      wp.colSet(ii, "db_card_diffamt", "" + liCardDiffamt);
      double lsTsccDisbamt = wp.colNum(ii, "db_tscc_disbamt")
            - wp.colNum(ii, "db_tscc_fstamt");
      wp.colSet(ii, "db_tscc_disbamt", "" + lsTsccDisbamt);

   }
}

//void set0() {
//   wp.col_set("wk_A", "0");
//   wp.col_set("wk_A1", "0");
//   wp.col_set("wk_A2", "0");
//   wp.col_set("wk_A3", "0");
//}
//
//void wk_num() throws Exception {
//   double liNum = 0, liCardDiffamt = 0, lsTsccDisbamt = 0;
//   for (int ii = 0; ii < wp.selectCnt; ii++) {
//   }
//}


void sumRead(String lsWhere) throws Exception {
   String sql1 = "select "
         + " count(*) as wk_A,"
         + " sum(uf_dc_amt2(fst_twd_amt,fst_dc_amt)) as wk_A1,"
         + " sum(uf_dc_amt2(fst_disb_amt,fst_disb_dc_amt)) as wk_A2,"
         + " sum(uf_dc_amt2(fst_disb_amt,fst_disb_dc_amt)) - sum(uf_dc_amt2(fst_twd_amt,fst_dc_amt)) as wk_A3"
         + " from rsk_chgback"
         + lsWhere;
   sqlSelect(sql1);
   if (sqlRowNum >0) {
      sql2wp("wk_A");
      sql2wp("wk_A1");
      sql2wp("wk_A2");
      sql2wp("wk_A3");
   }
   else {
      wp.colSet("wk_A",0);
      wp.colSet("wk_A1",0);
      wp.colSet("wk_A2",0);
      wp.colSet("wk_A3",0);
   }
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
public void pdfPrint() throws Exception {
	
	if(checkApproveZz()==false) {
		wp.respHtml = "TarokoErrorPDF";
      return;
	}
	
   wp.reportId = "rskr0220";
   String ss;
   ss = "交易卡別: ";
   if(wp.itemEq("ex_dbcard_flag", "0")) {
	   ss += "全部";
   } else if(wp.itemEq("ex_dbcard_flag", "1")) {
	   ss += "信用卡";
   } else if(wp.itemEq("ex_dbcard_flag", "2")) {
	   ss += "VD卡";
   } else if(wp.itemEq("ex_dbcard_flag", "3")) {
	   ss += "悠遊卡加值";
   }
   
   ss += "    撥款登錄日期: " + commString.strToYmd(wp.itemStr("ex_date1"))   	
      + " -- " + commString.strToYmd(wp.itemStr("ex_date2"))
      + "    撥款登錄者: " + wp.itemStr("ex_id1");
   wp.colSet("cond1", ss);
   wp.colSet("user_id", wp.loginUser);
   wp.pageRows = 9999;
   queryFunc();
   
   for (int ll=0; ll<wp.listCount[0]; ll++) {
      wp.colSet(ll,"wk_card_no",wp.colStr(ll,"card_no"));
      wp.colSet(ll, "wk_acct_key",wp.colStr(ll,"acct_type")+"-"+wp.colStr(ll,"acct_key"));
   }
   
   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0220.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}

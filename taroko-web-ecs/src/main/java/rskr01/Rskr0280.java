package rskr01;
/**
 * 2019-0621:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDFLine;

public class Rskr0280 extends BaseQuery implements InfacePdf {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "Q":
         queryFunc(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "L":
         strAction = "";
         clearFunc(); break;
      case "PDF":
         strAction = "PDF";
         pdfPrint(); break;
   }

   dddwSelect();
   initButton();

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("印表日期起迄：輸入錯誤");
      return;
   }

   String lsWhere =
         " where ((chg_stage in ('2') and sub_stage='30') or (chg_stage in ('3') and sub_stage='10')) and final_close = '' "
               + sqlCol(wp.itemStr("ex_date1"), "rep_apr_date", ">=")
               + sqlCol(wp.itemStr("ex_date2"), "rep_apr_date", "<=")
               + sqlCol(wp.itemStr("ex_user_id"),"fst_add_user");

   if (wp.itemEq("ex_dbcard_flag", "Y")) {
      lsWhere += " and debit_flag='Y' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "N")) {
      lsWhere += " and debit_flag<>'Y' ";
   }
   
   setSqlParmNoClear(true);
   sumFstAmt(lsWhere);

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
         + "uf_idno_name2(card_no,debit_flag) as db_idno_name, "
         + " acct_type , "
         + "uf_acno_key2(card_no,acct_type) as acct_key,"
         + " bin_type , "
         + " ctrl_seqno,"
         + " purchase_date,"
         + " dest_amt,"
         + " fst_apr_date,"
         + " chg_times,"
         + " '' as process_desc,"
         + " reference_no,"
         + " reference_seq,"
         + " chg_stage,"
         + " rep_apr_date,"
         + " fst_add_date,"
         + " fst_add_user,"
         + " rep_expire_date,"
         + " repsent_date,"
         + " dest_amt as db_sum_dest_amt,"
         + " 1 as db_sum"
//			+ "uf_hi_idno(uf_acno_key2(p_seqno,debit_flag)) as wk_acct_key,"
//			+ "uf_hi_cardno(card_no) as wk_card_no,"
//			+ "uf_hi_cname(uf_idno_name2(card_no,debit_flag)) as hh_chi_name"
   ;
   wp.daoTable = "Vrsk_chgback";
   wp.whereOrder = " order by  acct_type, card_no ";

   pageQuery();

   if (sqlRowNum <= 0) {
      set0();
      alertErr("此條件查無資料");
      return;
   }
   wp.setListCount(1);
   wp.setPageValue();
   queryAfter();
}

void queryAfter()  throws Exception {
   int il_select_cnt = 0;
   il_select_cnt = wp.selectCnt;

   String sql1 = " select ls_date, ls_status, uf_tt_idtab('RSK_STATUS_DESC',ls_status) as ls_desc from ( "
         + " select rep_apr_date as ls_date , 'C'||chg_stage||'30' as ls_status from rsk_chgback "
         + " where reference_no = ? and ctrl_seqno = ? "
         + " union all "
         + " select decode(rept_status,'10',add_date,'30',add_date,'60',close_add_date,'80',close_apr_date) as ls_date , "
         + " decode(rept_status,'10','RR10','30','RR30','60','RR60','80','RR80') as ls_status from rsk_receipt "
         + " where reference_no = ? and ctrl_seqno = ? "
         + " union all "
         + " select decode(prb_status,'10',add_date,'30',add_apr_date,'40',add_apr_date,'50',add_apr_date,'60',close_add_date,'80',close_apr_date,'83',close_add_date_2,'85',close_apr_date_2) as ls_date , "
         + " decode(prb_status,'10','R10','30',decode(prb_src_code,'R','R30',prb_mark||'30'),'40','R40','50','R50','60',decode(prb_src_code,'R','R60',prb_mark||'60'),'80',decode(prb_src_code,'R','R80',prb_mark||'80'),'83',decode(prb_src_code,'R','R82',prb_mark||'83'),'85',decode(prb_src_code,'R','R85',prb_mark||'85')) as ls_status "
         + " from rsk_problem where reference_no = ? and ctrl_seqno = ? "
         + " ) order by 1 Desc ";

   ecsfunc.HiData oohh = new ecsfunc.HiData();
   for (int ii = 0; ii < il_select_cnt; ii++) {      
      sqlSelect(sql1, new Object[]{
            wp.colStr(ii, "reference_no"), wp.colStr(ii, "ctrl_seqno"),
            wp.colStr(ii, "reference_no"), wp.colStr(ii, "ctrl_seqno"),
            wp.colStr(ii, "reference_no"), wp.colStr(ii, "ctrl_seqno")
      });
      if (sqlRowNum > 0) {
         wp.colSet(ii, "process_desc", sqlStr("ls_date") + "-" + sqlStr("ls_desc"));
      }

      if (eqIgno("pdf", strAction)) {
      	wp.colSet(ii, "card_no",commString.hideCardNo(wp.colStr(ii,"card_no")));
      	wp.colSet(ii, "acct_key",commString.hideIdno(wp.colStr(ii,"acct_key")));
      	wp.colSet(ii, "hh_chi_name",commString.hideIdnoName(wp.colStr(ii,"db_idno_name")));
      }
   }

}

void set0() {
   wp.colSet("sum_count", "0");
   wp.colSet("sum_dest_amt", "0");
}

void sumFstAmt(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " count(*) as sum_count,"
         + " sum(dest_amt) as sum_dest_amt";
   wp.daoTable = "Vrsk_chgback";
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

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "rskr0280";
   wp.pageRows = 9999;
   String ss, tt;
   if (wp.itemEq("ex_dbcard_flag", "Y")) {
      ss = "Debit Card 再提示未結案月報表";
   }
   else {
      ss = "再提示未結案月報表";
   }
   tt = "印表期間: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
         + commString.strToYmd(wp.itemStr("ex_date2"));
   wp.colSet("title", ss);
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   queryFunc();
   if(sqlNotFind()) {
	   wp.respHtml = this.errPagePDF;
	   return ;
   }
   TarokoPDFLine pdf = new TarokoPDFLine();
//   pdf.pageCount = 30;
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0280.xlsx";
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;
}

}

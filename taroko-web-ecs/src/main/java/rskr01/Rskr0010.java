package rskr01;
/**
 * 2019-1211   Alex  fix pdf mail
 * 2019-1127   JH    UAT-performance
 * 2019-0908   JH    --xxxctrl_log
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr0010 extends BaseAction implements InfacePdf {
busi.CommCurr zzCurr = new busi.CommCurr();

double liDdPrbAmount = 0.0, liDcPrbAmount = 0.0, liPrbAmount = 0.0;
String lsPrbFraudRpt = "", ss = "";
int llCnt = 0;
taroko.base.CommDate zzdate = new taroko.base.CommDate();

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "U":
         checkUpdate(); break;
      case "PDF":
         printData(); break;
      case "PDF2":
         strAction = "PDF";
         printData2(); break;
      default:
         defaultAction();
   }
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

void queryCount() throws Exception {
   boolean lbDebit = wp.itemEq("ex_debit", "Y");
   String sql1 = "select count(*) as xx_cnt from bil_bill where 1=1";
   if (lbDebit) {
      sql1 = "select count(*) as xx_cnt from dbb_bill where 1=1";
   }
   sql1 += sqlCol(wp.itemStr("ex_card_no"), "card_no") +
         sqlCol(wp.itemStr("ex_bin_type"), "bin_type") +
         sqlCol(wp.itemStr("ex_ctrl_seqno"), "rsk_ctrl_seqno", "like%");

   if (eqIgno(wp.itemStr("ex_print"), "2")) {
      sql1 += " and rsk_print_flag ='Y' ";
   }
   else if (eqIgno(wp.itemStr("ex_print"), "1")) {
      sql1 += " and rsk_print_flag <>'Y' ";
   }

   sql1 += " and reference_no in ( ";
   String lsType = wp.itemStr("ex_type");
   if (eqAny(lsType, "0")) {
      if (lbDebit) {
         sql1 += "select reference_no from rsk_problem where debit_flag='Y' "
        	  + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
        	  + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "add_date") 
        	  + " union select reference_no from rsk_chgback where debit_flag='Y' "
        	  + sqlCol(wp.itemStr("ex_date1"),"fst_add_date",">=")
        	  + sqlCol(wp.itemStr("ex_date2"),"fst_add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "fst_add_date") 
        	  + " union select reference_no from rsk_receipt where debit_flag='Y' " 
        	  + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
        	  + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "add_date")
        	  ;
      }
      else {
         sql1 += "select reference_no from rsk_problem where debit_flag<>'Y' "
        	  + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
           	  + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "add_date")
        	  + " union select reference_no from rsk_chgback where debit_flag<>'Y' "
        	  + sqlCol(wp.itemStr("ex_date1"),"fst_add_date",">=")
        	  + sqlCol(wp.itemStr("ex_date2"),"fst_add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "fst_add_date")
        	  + " union select reference_no from rsk_receipt where debit_flag<>'Y' "
        	  + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
        	  + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "add_date")
        	  ;
      }
      sql1 += " )";
   }
   else if (eqAny(lsType, "1")) {
      if (lbDebit) {
         sql1 += " select reference_no from rsk_problem where debit_flag='Y' "
        	  + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
              + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "add_date")
        	  ;
      }
      else {
         sql1 += " select reference_no from rsk_problem where debit_flag<>'Y'"
        	  + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
              + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")	 
//        	  + sqlBetween("ex_date1", "ex_date2", "add_date")
        	  ;
      }
      sql1 += " )";
   }
   else if (eqAny(lsType, "3")) {
      if (lbDebit) {
         sql1 += " select reference_no from rsk_chgback where debit_flag='Y' "
        	  + sqlCol(wp.itemStr("ex_date1"),"fst_add_date",">=")
           	  + sqlCol(wp.itemStr("ex_date2"),"fst_add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "fst_add_date")
        	  ;
      }
      else {
         sql1 += " select reference_no from rsk_chgback where debit_flag<>'Y' "
        	  + sqlCol(wp.itemStr("ex_date1"),"fst_add_date",">=")
           	  + sqlCol(wp.itemStr("ex_date2"),"fst_add_date","<=")	 
//        	  + sqlBetween("ex_date1", "ex_date2", "fst_add_date")
        	  ;
      }
      sql1 += " )";
   }
   else if (eqAny(lsType, "2")) {
      if (lbDebit) {
         sql1 += " select reference_no from rsk_receipt where debit_flag='Y' " 
        	  + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
              + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "add_date")
        	  ;
      }
      else {
         sql1 += " select reference_no from rsk_receipt where debit_flag<>'Y' "
        	  + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
              + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")
//        	  + sqlBetween("ex_date1", "ex_date2", "add_date")
        	  ;
      }
      sql1 += " )";
   }

   sqlSelect(sql1);
   wp.totalRows = (int) sqlNum("xx_cnt");
}

@Override
public void queryFunc() throws Exception {
   if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("新增登錄日期: 起迄錯誤");
      return;
   }

   if (itemallEmpty("ex_date1,ex_date2,ex_ctrl_seqno,ex_card_no")) {
      alertErr("新增登錄日期,控制流水號,卡號 不可全部空白");
      return;
   }
   
   queryCount();
   if (wp.totalRows <= 0) {
     alertErr(appMsg.errCondNodata);
     return;
   }
   
   String lsType = "";
   String lsWhere = ""
         + sqlCol(wp.itemStr("ex_bin_type"), "A.bin_type")
         + sqlCol(wp.itemStr("ex_card_no"), "A.card_no")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "A.rsk_ctrl_seqno", "like%");

   if (eqIgno(wp.itemStr("ex_print"), "2")) {
      lsWhere += " and A.rsk_print_flag ='Y' ";
   }
   else if (eqIgno(wp.itemStr("ex_print"), "1")) {
      lsWhere += " and A.rsk_print_flag <>'Y' ";
   }

   String lsDebit = " debit_flag<>'Y' ";
   if (wp.itemEq("ex_debit", "Y"))
      lsDebit = " debit_flag='Y' ";

   String lsDate1 = wp.itemStr("ex_date1");
   String lsDate2 = wp.itemStr("ex_date2");
   if (!empty("ex_date1") || !empty("ex_date2")) {
      if (eqIgno(wp.itemStr("ex_type"), "0")) {
         lsWhere += " and A.reference_no in (" +
               " select reference_no from rsk_problem where " + lsDebit + sqlStrend(lsDate1, lsDate2, "add_date") +
               " union select reference_no from rsk_chgback where " + lsDebit + sqlStrend(lsDate1, lsDate2, "fst_add_date") +
               " union select reference_no from rsk_receipt where " + lsDebit + sqlStrend(lsDate1, lsDate2, "add_date") +
               " )";
      }
      else if (eqIgno(wp.itemStr("ex_type"), "1")) {
         lsWhere += " and A.reference_no in (" +
               " select reference_no from rsk_problem where " + lsDebit + sqlStrend(lsDate1, lsDate2, "add_date") +
               " )";
      }
      else if (eqIgno(wp.itemStr("ex_type"), "2")) {
         lsWhere += " and A.reference_no in (" +
               " select reference_no from rsk_receipt where " + lsDebit + sqlStrend(lsDate1, lsDate2, "add_date") +
               " )";
      }
      else if (eqIgno(wp.itemStr("ex_type"), "3")) {
         lsWhere += " and A.reference_no in (" +
               " select reference_no from rsk_chgback where " + lsDebit + sqlStrend(lsDate1, lsDate2, "fst_add_date") +
               " )";
      }
   }
   
   if (wp.itemEq("ex_debit", "Y") == false) {
   	lsWhere += sqlCol(wp.itemStr("ex_v_card_no"),"A.v_card_no","like%");
   }
   
//   this.setSqlParmNoClear(true);
//   queryCount();
   
   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   //-select.count(*)-
   
//   if (wp.totalRows <= 0) {
//      alertErr(appMsg.errCondNodata);
//      return;
//   }

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.sqlCmd = "select "
         + " A.rsk_print_flag as print_flag ,"
         + " A.rsk_ctrl_seqno as ctrl_seqno ,"
         + " A.bin_type ,"
         + " A.card_no ,"
         + " A.acct_month ,"
         + " A.post_date ,"
         + " A.payment_type ,"
         + " A.acct_type ,"
         + " A.auth_code ,"
         + " A.purchase_date ,"
         + " A.film_no ,"
         + " A.txn_code ,"
         + " A.source_amt ,"
         + " A.source_curr ,"
         + " A.dest_amt ,"
         + " A.settl_amt ,"
         + " A.process_date ,"
         + " A.acq_member_id ,"
         + " A.pos_entry_mode ,"
         + " A.mcht_chi_name ,"
         + " A.mcht_eng_name ,"
         + " A.mcht_no ,"
         + " A.mcht_city ,"
         + " A.mcht_category ,"
         + " A.mcht_country ,"
         + " A.mcht_state ,"
         + " A.reference_no ,"
         + " A.txn_code ,"
         + " A.contract_no ,"
         + " A.install_curr_term , "
         + " A.contract_amt"
         + ",'' as prbl_add_date"
         + ",'' as prbl_reason_code"
         + ",'' as prbl_mark"
         + ",'' as chgb_add_date"
         + ",'' as rept_add_date"
   ;

   if (wp.itemEq("ex_debit", "N")) {
      wp.sqlCmd += ", A.curr_code, 'N' as debit_flag , A.v_card_no" +
            " from bil_bill A" + // join Vrsk_ctrlseqno_bil B on A.reference_no = B.reference_no"+
            " where 1=1" + wp.queryWhere;
      wp.pageCountSql = "select count(*) "
            + " from bil_bill A join Vrsk_ctrlseqno_bil B on A.reference_no = B.reference_no "
            + " where 1=1 " + wp.queryWhere;
   }
   else {
      wp.sqlCmd += ",'901' as curr_code, 'Y' as debit_flag" +
            " from dbb_bill A" +  // join Vrsk_ctrlseqno_dbb B on A.reference_no = B.reference_no"+
            " where 1=1" + wp.queryWhere;
      wp.pageCountSql = "select count(*) "
            + " from dbb_bill A join Vrsk_ctrlseqno_dbb B on A.reference_no = B.reference_no "
            + " where 1=1 " + wp.queryWhere;
   }
   wp.sqlCmd += " order by A.rsk_ctrl_seqno";

   pageQuery();
   wp.setListCount(1);
   wp.setPageValue();
   queryAfter();
}

void queryAfter() throws Exception {
   if (wp.listCount[0] == 0) return;

   String sql1 = "select prbl_add_date, prbl_reason_code, prbl_mark" +
         ", chgb_add_date, rept_add_date" +
         " from Vrsk_ctrlseqno_bil" +
         " where reference_no =?";
   if (wp.colEq(0, "debit_flag", "Y")) {
      sql1 = "select prbl_add_date, prbl_reason_code, prbl_mark" +
            ", chgb_add_date, rept_add_date" +
            " from Vrsk_ctrlseqno_dbb" +
            " where reference_no =?";
   }

   int llNrow = wp.listCount[0];
   for (int ll = 0; ll < llNrow; ll++) {
      String ls_refno = wp.colStr(ll, "reference_no");
      sqlSelect(sql1, new Object[]{ls_refno});
      if (sqlRowNum <= 0) continue;
      sql2wp(ll, "prbl_add_date");
      sql2wp(ll, "prbl_reason_code");
      sql2wp(ll, "prbl_mark");
      sql2wp(ll, "chgb_add_date");
      sql2wp(ll, "rept_add_date");
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
   wp.reportId = "rskr0010";
   wp.pageRows = 9999;
   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0010.xlsx";
   pdf.pageCount = 1;
   pdf.sheetNo = 0;
   pdf.pageVert = true;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

public void printData2() throws Exception {
   String ls_sysdate = "", ls_print_date = "";
   llCnt = 0;
   String ls_card_no = wp.itemStr("ex_card_no");
   String[] ls_ctrl_seqno = wp.itemBuff("ctrl_seqno");
   String[] ls_purchase_date = wp.itemBuff("purchase_date");
   String[] ls_mcht_chi_name = wp.itemBuff("mcht_chi_name");
   String[] ls_mcht_eng_name = wp.itemBuff("mcht_eng_name");
   String[] ls_curr_code = wp.itemBuff("curr_code");
   String[] ls_contract_no = wp.itemBuff("contract_no");
   String[] ls_install_curr_term = wp.itemBuff("install_curr_term");
   String[] ls_contract_amt = wp.itemBuff("contract_amt");
   String[] ls_chgb_add_date = wp.itemBuff("chgb_add_date");
   String[] ls_prbl_add_date = wp.itemBuff("prbl_add_date");
   String[] aa_opt = wp.itemBuff("opt");
   String ls_user = wp.itemStr("ex_tel_user");
   String ls_tel = wp.itemStr("ex_tel_no");
   wp.listCount[0] = wp.itemRows("ctrl_seqno");

   if (wp.itemEmpty("ex_card_no")) {
      alertErr("未輸入卡號,不可列印列問交信函");
      wp.respHtml = "TarokoErrorPDF";
      return;
   }

   //--set zip , addr , chi_name
   if (selectAct(wp.itemStr("ex_card_no")) == false) {
      alertErr("卡號輸入錯誤 或 查無此卡號 ");
      wp.respHtml = "TarokoErrorPDF";
      return;
   }
   //--set card_no   
   wp.colSet("wk_card_no_4", commString.mid(ls_card_no, 0, 4) + "-       -       -" + commString.mid(ls_card_no, ls_card_no.length() - 4));
   //--set print date
   ls_sysdate = getSysDate();
   ls_sysdate = zzdate.toTwDate(ls_sysdate);
   ls_print_date = "中　　　華　　　民　　　國　　　" + commString.mid(ls_sysdate, 0, 3) + "　　　年";
   if (commString.strToNum(commString.mid(ls_sysdate, 3, 2)) < 10) {
      ls_print_date += "　　" + commString.mid(ls_sysdate, 4, 1) + "　　　月";
   }
   else {
      ls_print_date += "　　" + commString.mid(ls_sysdate, 3, 2) + "　　　月";
   }
   if (commString.strToNum(commString.mid(ls_sysdate, 5, 2)) < 10) {
      ls_print_date += "　　　" + commString.mid(ls_sysdate, 6, 1) + "　　　日";
   }
   else {
      ls_print_date += "　　　" + commString.mid(ls_sysdate, 5, 2) + "　　　日";
   }
   wp.colSet("wk_print_date", ls_print_date);
   //--聯絡人員  分機
   wp.colSet("ex_tel_user", ls_user);
   wp.colSet("ex_tel_no", ls_tel);
   wp.colSet("ex_wk_tel", ls_tel + " " + ls_user);
   //--扣款
   String sql1 = " select fst_dc_amt from rsk_chgback where ctrl_seqno = ? ";
   //--問交
   String sql2 = " select dc_prb_amount from rsk_problem where ctrl_seqno = ? ";
   //--幣別
   String sql3 = " select curr_eng_name from ptr_currcode where curr_code = ? ";

   for (int ii = 0; ii < wp.itemRows("ctrl_seqno"); ii++) {
      if (this.checkBoxOptOn(ii, aa_opt) == false) continue;

      if (!empty(ls_chgb_add_date[ii])) {
         sqlSelect(sql1, new Object[]{ls_ctrl_seqno[ii]});
         if (sqlRowNum <= 0) continue;
         wp.colSet(llCnt, "wk_amount", sqlNum("fst_dc_amt"));
      }
      else if (!empty(ls_prbl_add_date[ii])) {
         if (!empty(ls_contract_no[ii]) && eqIgno(ls_install_curr_term[ii], "1")) {
            wp.colSet(llCnt, "wk_amount", ls_contract_amt[ii]);
         }
         else if (empty(ls_contract_no[ii])) {
            sqlSelect(sql2, new Object[]{ls_ctrl_seqno[ii]});
            if (sqlRowNum <= 0) continue;
            wp.colSet(llCnt, "wk_amount", sqlNum("dc_prb_amount"));
         }
      }
      else continue;

      //--mcht_name
      if (!empty(ls_mcht_chi_name[ii]) && eqIgno(ls_mcht_chi_name[ii], null)) {
         wp.colSet(llCnt, "wk_mcht_name", ls_mcht_chi_name[ii]);
      }
      else if (!empty(ls_mcht_eng_name[ii])) {
         wp.colSet(llCnt, "wk_mcht_name", ls_mcht_eng_name[ii]);
      }
      wp.colSet(llCnt, "wk_tx_date", ls_purchase_date[ii]);

      //--curr_code
      sqlSelect(sql3, new Object[]{ls_curr_code[ii]});
      if (sqlRowNum > 0) {
         wp.colSet(llCnt, "wk_curr_code", sqlStr("curr_eng_name"));
      }

      llCnt++;
   }

   if (llCnt == 0) {
      alertErr("請勾選列印資料");
      wp.respHtml = "TarokoErrorPDF";
      return;
   }

   pdfPrint2();

}

boolean selectAct(String ls_card_no) throws Exception  {

   String sql0 = " select "
         + " debit_flag "
         + " from cca_card_base "
         + " where card_no = ? ";

   sqlSelect(sql0, new Object[]{ls_card_no});
   if (sqlRowNum <= 0) return false;

   if (eqIgno(sqlStr("debit_flag"), "Y")) {
      String sql2 = " select "
            + " replace(uf_idno_name2(A.card_no,'Y'),'　','') as chi_name , "
            + " B.bill_sending_zip , "
            + " B.bill_sending_addr1||B.bill_sending_addr2||B.bill_sending_addr3||B.bill_sending_addr4||B.bill_sending_addr5 as bill_send_addr "
            + " from dbc_card A , dba_acno B "
            + " where A.p_seqno = B.p_seqno "
            + " and A.card_no = ? ";
      sqlSelect(sql2, new Object[]{ls_card_no});
      if (sqlRowNum <= 0) return false;

   }
   else {
      String sql1 = " select "
            + " replace(uf_idno_name(A.card_no),'　','') as chi_name , "
            + " B.bill_sending_zip , "
            + " B.bill_sending_addr1||B.bill_sending_addr2||B.bill_sending_addr3||B.bill_sending_addr4||B.bill_sending_addr5 as bill_send_addr "
            + " from crd_card A , act_acno B "
            + " where A.acno_p_seqno = B.acno_p_seqno "
            + " and A.card_no = ? ";
      sqlSelect(sql1, new Object[]{ls_card_no});
      if (sqlRowNum <= 0) return false;
   }


   wp.colSet("wk_chi_name", sqlStr("chi_name"));
   wp.colSet("wk_zip_code", sqlStr("bill_sending_zip"));
   wp.colSet("wk_send_addr", sqlStr("bill_send_addr"));
   return true;
}

public void pdfPrint2() throws Exception {
   wp.reportId = "rskr0010";
   wp.pageRows = 9999;
   TarokoPDF pdf = new TarokoPDF();
   wp.listCount[0] = llCnt;
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0010_mail.xlsx";
   pdf.pageCount = 35;
   pdf.sheetNo = 0;
   pdf.pageVert = true;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}


public void printData() throws Exception {
   String[] ls_ctrl_seqno = wp.itemBuff("ctrl_seqno");
   String[] ls_bin_type = wp.itemBuff("bin_type");
   String[] lsDebit_flag = wp.itemBuff("debit_flag");
   String[] ls_card_no = wp.itemBuff("card_no");
   String[] ls_acct_month = wp.itemBuff("acct_month");
   String[] ls_post_date = wp.itemBuff("post_date");
   String[] ls_prbl_add_date = wp.itemBuff("prbl_add_date");
   String[] ls_prbl_reason_code = wp.itemBuff("prbl_reason_code");
   String[] ls_chgb_add_date = wp.itemBuff("chgb_add_date");
   String[] ls_rept_add_date = wp.itemBuff("rept_add_date");
   String[] ls_payment_type = wp.itemBuff("payment_type");
   String[] ls_acct_type = wp.itemBuff("acct_type");
   String[] ls_auth_code = wp.itemBuff("auth_code");
   String[] ls_purchase_date = wp.itemBuff("purchase_date");
   String[] ls_film_no = wp.itemBuff("film_no");
   String[] ls_txn_code = wp.itemBuff("txn_code");
   String[] ls_source_amt = wp.itemBuff("source_amt");
   String[] ls_source_curr = wp.itemBuff("source_curr");
   String[] ls_dest_amt = wp.itemBuff("dest_amt");
   String[] ls_settl_amt = wp.itemBuff("settl_amt");
   String[] ls_process_date = wp.itemBuff("process_date");
   String[] ls_acq_member_id = wp.itemBuff("acq_member_id");
   String[] ls_pos_entry_mode = wp.itemBuff("pos_entry_mode");
   String[] ls_mcht_chi_name = wp.itemBuff("mcht_chi_name");
   String[] ls_mcht_eng_name = wp.itemBuff("mcht_eng_name");
   String[] ls_mcht_no = wp.itemBuff("mcht_no");
   String[] ls_mcht_city = wp.itemBuff("mcht_city");
   String[] ls_mcht_category = wp.itemBuff("mcht_category");
   String[] ls_mcht_country = wp.itemBuff("mcht_country");
   String[] ls_mcht_state = wp.itemBuff("mcht_state");
   String[] ls_reference_no = wp.itemBuff("reference_no");
   String[] ls_prbl_mark = wp.itemBuff("prbl_mark");
   String[] ls_opt = wp.itemBuff("opt");
   String ls_user = wp.itemStr("ex_tel_user");
   String ls_tel = wp.itemStr("ex_tel_no");
   wp.listCount[0] = ls_ctrl_seqno.length;
   int ii = -1;
   for (int rr = 0; rr < ls_ctrl_seqno.length; rr++) {
      if (!checkBoxOptOn(rr, ls_opt)) {
         continue;
      }
      ii++;

      if (!empty(ls_prbl_add_date[rr].trim())) {
         if (eqIgno(ls_prbl_mark[rr], "E")) {
            if (eqIgno(lsDebit_flag[rr], "Y")) {
               wp.colSet(ii, "title", "Debit Card 不合格帳單/扣款/調單流程表");
            }
            else {
               wp.colSet(ii, "title", "不合格帳單/扣款/調單流程表");
            }
         }
         else if (eqIgno(ls_prbl_mark[rr], "S")) {
            wp.colSet(ii, "title", "特殊交易/扣款/調單流程表");
         }
         else {
            if (eqIgno(lsDebit_flag[rr], "Y")) {
               wp.colSet(ii, "title", "Debit Card 問題交易/扣款/調單流程表");
            }
            else {
               wp.colSet(ii, "title", "問題交易/扣款/調單流程表");
            }
         }
      }
      else {
         if (eqIgno(lsDebit_flag[rr], "Y")) {
            wp.colSet(ii, "title", "Debit Card 問題交易/扣款/調單流程表");
         }
         else {
            wp.colSet(ii, "title", "問題交易/扣款/調單流程表");
         }
      }

      wp.colSet(ii, "is_ctrl_seqno", ls_ctrl_seqno[rr]);
      wp.colSet(ii, "is_acct_type", ls_acct_type[rr]);
      wp.colSet(ii, "is_acct_month", ls_acct_month[rr]);
      wp.colSet(ii, "is_card_no", ls_card_no[rr]);
      wp.colSet(ii, "is_reference_no", ls_reference_no[rr]);

      if (eqIgno(lsDebit_flag[rr], "Y")) {
         if (selectDbcCard(ls_card_no[rr]) < 0) {
            wp.colSet(ii, "is_major_id", "");
            wp.colSet(ii, "is_corp_no", "");
            wp.colSet(ii, "is_chi_name", "");
            wp.colSet(ii, "is_debit_acct_no", "");
         }
         else {
            wp.colSet(ii, "is_major_id", sqlStr("id_no"));
            wp.colSet(ii, "is_corp_no", sqlStr("corp_no"));
            wp.colSet(ii, "is_chi_name", sqlStr("chi_name"));
            wp.colSet(ii, "is_debit_acct_no", sqlStr("debit_acct_no"));
         }
      }
      else {
         if (selectCrdCard(ls_card_no[rr]) < 0) {
            wp.colSet(ii, "is_major_id", "");
            wp.colSet(ii, "is_corp_no", "");
            wp.colSet(ii, "is_chi_name", "");
         }
         else {
            wp.colSet(ii, "is_major_id", sqlStr("major_id"));
            wp.colSet(ii, "is_corp_no", sqlStr("corp_no"));
            wp.colSet(ii, "is_chi_name", sqlStr("chi_name"));
         }
      }

      wp.colSet(ii, "is_bin_type", ls_bin_type[rr]);

      liDdPrbAmount = 0;
      lsPrbFraudRpt = "";
      liPrbAmount = 0;
      liDcPrbAmount = 0;
      selectRskProblem(ls_ctrl_seqno[rr], ls_bin_type[rr]);
//      wp.col_set(ii, "is_prb_fraud_rpt", "N");
      wp.colSet(ii, "is_dd_prb_amount", "" + liDdPrbAmount);
      if (eqIgno(lsPrbFraudRpt, "1")) wp.colSet(ii, "is_prb_fraud_rpt", "Y");
      wp.colSet(ii, "is_post_date", ls_post_date[rr]);
      wp.colSet(ii, "is_auth_code", ls_auth_code[rr]);

      if (eqIgno(lsDebit_flag[rr], "Y")) {
         if (selectDbb(ls_reference_no[rr]) > 0) {
            wp.colSet(ii, "is_purchase_date", sqlStr("dbb.purchase_date"));
            wp.colSet(ii, "is_txn_code", sqlStr("dbb.txn_code"));
            wp.colSet(ii, "is_film_no", sqlStr("dbb.film_no"));
            wp.colSet(ii, "is_source_amt", sqlStr("dbb.source_amt"));
            wp.colSet(ii, "is_source_curr", sqlStr("dbb.source_curr"));
            wp.colSet(ii, "is_dest_amt", sqlStr("dbb.dest_amt"));
            wp.colSet(ii, "is_settl_amt", sqlStr("dbb.settl_amt"));
            wp.colSet(ii, "is_process_date", sqlStr("dbb.process_date"));
            wp.colSet(ii, "is_acq_member_id", sqlStr("dbb.acq_member_id"));
            wp.colSet(ii, "is_pos_entry_mode", sqlStr("dbb.pos_entry_mode"));
            wp.colSet(ii, "is_mcht_chi_name", sqlStr("dbb.mcht_chi_name"));
            wp.colSet(ii, "is_mcht_eng_name", sqlStr("dbb.mcht_eng_name"));
            wp.colSet(ii, "is_mcht_name", sqlStr("dbb.mcht_chi_name") + " " + sqlStr("dbb.mcht_eng_name"));
            wp.colSet(ii, "is_mcht_city", sqlStr("dbb.mcht_city"));
            wp.colSet(ii, "is_mcht_country", sqlStr("dbb.mcht_country"));
            wp.colSet(ii, "is_mcht_category", sqlStr("dbb.mcht_category"));
            wp.colSet(ii, "is_mcht_state", sqlStr("dbb.mcht_state"));
            wp.colSet(ii, "is_mcht_no", sqlStr("dbb.mcht_no"));
            wp.colSet(ii, "is_interest_date", sqlStr("dbb.interest_date"));
            ss = sqlStr("dbb.payment_type");
         }
      }
      else if (eqIgno(lsDebit_flag[rr], "N")) {
         if (selectBil(ls_reference_no[rr]) > 0) {
            wp.colSet(ii, "is_purchase_date", sqlStr("bil.purchase_date"));
            wp.colSet(ii, "is_txn_code", sqlStr("bil.txn_code"));
            wp.colSet(ii, "is_film_no", sqlStr("bil.film_no"));
            wp.colSet(ii, "is_source_amt", sqlStr("bil.source_amt"));
            wp.colSet(ii, "is_source_curr", sqlStr("bil.source_curr"));
            wp.colSet(ii, "is_dest_amt", sqlStr("bil.dest_amt"));
            wp.colSet(ii, "is_settl_amt", sqlStr("bil.settl_amt"));
            wp.colSet(ii, "is_process_date", sqlStr("bil.process_date"));
            wp.colSet(ii, "is_acq_member_id", sqlStr("bil.acq_member_id"));
            wp.colSet(ii, "is_pos_entry_mode", sqlStr("bil.pos_entry_mode"));
            wp.colSet(ii, "is_mcht_chi_name", sqlStr("bil.mcht_chi_name"));
            wp.colSet(ii, "is_mcht_eng_name", sqlStr("bil.mcht_eng_name"));
            wp.colSet(ii, "is_mcht_name", sqlStr("bil.mcht_chi_name") + " " + sqlStr("bil.mcht_eng_name"));
            wp.colSet(ii, "is_mcht_city", sqlStr("bil.mcht_city"));
            wp.colSet(ii, "is_mcht_country", sqlStr("bil.mcht_country"));
            wp.colSet(ii, "is_mcht_category", sqlStr("bil.mcht_category"));
            wp.colSet(ii, "is_mcht_state", sqlStr("bil.mcht_state"));
            wp.colSet(ii, "is_mcht_no", sqlStr("bil.mcht_no"));
            wp.colSet(ii, "is_interest_date", sqlStr("bil.interest_date"));
            ss = sqlStr("bil.payment_type");
         }
      }
      if (selectPrbl(ls_ctrl_seqno[rr], ls_bin_type[rr]) > 0) {
         wp.colSet(ii, "is_prbl_add_date", sqlStr("Prbl.add_date"));
         wp.colSet(ii, "is_prb_reason_code", sqlStr("Prbl.prb_reason_code"));
      }
      wp.colSet(ii, "is_payment_type", ss);
      if (eqIgno(lsDebit_flag[rr], "Y")) {
         if (pos("|I|E", ss) > 0) {
            wp.colSet(ii, "is_install_tot_term1", sqlStr("dbb.install_tot_term1"));
            wp.colSet(ii, "is_install_first_amt", sqlStr("dbb.install_first_amt"));
            wp.colSet(ii, "is_install_per_amt", sqlStr("dbb.install_per_amt"));
            wp.colSet(ii, "is_install_fee", sqlStr("dbb.install_fee"));
            wp.colSet(ii, "is_contract_no", sqlStr("dbb.contract_no"));
         }
         else if (pos("|1|2", ss) > 0) {
            wp.colSet(ii, "is_deduct_bp", sqlStr("bil.deduct_bp"));
            wp.colSet(ii, "is_cash_pay_amt", sqlStr("bil.cash_pay_amt"));
         }
      }
      else if ((eqIgno(lsDebit_flag[rr], "N"))) {
         if (pos("|I|E", ss) > 0) {
            wp.colSet(ii, "is_install_tot_term1", sqlStr("bil.install_tot_term1"));
            wp.colSet(ii, "is_install_first_amt", sqlStr("bil.install_first_amt"));
            wp.colSet(ii, "is_install_per_amt", sqlStr("bil.install_per_amt"));
            wp.colSet(ii, "is_install_fee", sqlStr("bil.install_fee"));
            wp.colSet(ii, "is_contract_no", sqlStr("bil.contract_no"));
         }
         else if (pos("|1|2", ss) > 0) {
            wp.colSet(ii, "is_deduct_bp", sqlStr("bil.deduct_bp"));
            wp.colSet(ii, "is_cash_pay_amt", sqlStr("bil.cash_pay_amt"));
         }
      }

      if (eqIgno(lsDebit_flag[rr], "Y")) {
         wp.colSet(ii, "is_bill_type", sqlStr("dbb.bill_type"));
         wp.colSet(ii, "is_db_bill_stmt_cycle", sqlStr("dbb.stmt_cycle"));
         wp.colSet(ii, "is_curr_code", sqlStr("dbb.curr_code"));
         wp.colSet(ii, "is_dc_dest_amt", sqlStr("dbb.dc_dest_amt"));
      }
      else if (eqIgno(lsDebit_flag[rr], "N")) {
         wp.colSet(ii, "is_bill_type", sqlStr("bil.bill_type"));
         wp.colSet(ii, "is_db_bill_stmt_cycle", sqlStr("bil.stmt_cycle"));
         wp.colSet(ii, "is_curr_code", sqlStr("bil.curr_code"));
         wp.colSet(ii, "is_dc_dest_amt", sqlStr("bil.dc_dest_amt"));
      }

      wp.colSet(ii, "is,dc_prb_amount", "" + liDcPrbAmount);
      wp.colSet(ii, "is,prb_amount", "" + liPrbAmount);

      //-rsk_receipt------------------------
      if (selectRept(ls_ctrl_seqno[rr], ls_bin_type[rr]) > 0) {
         wp.colSet(ii, "is_last_return_date", sqlStr("Rept.last_return_date"));
         wp.colSet(ii, "is_reason_code", sqlStr("Rept.reason_code"));
         wp.colSet(ii, "is_rept_add_date", sqlStr("Rept.add_date"));
      }

      //-rsk_chgback-------------------------
      if (selectChgb(ls_ctrl_seqno[rr], ls_bin_type[rr]) > 0) {
         chgbDecode(ls_ctrl_seqno[rr], ls_bin_type[rr]);
         wp.colSet(ii, "is_db_chgback_fst_add_date", sqlStr("fst_add_date"));
         wp.colSet(ii, "is_db_chgback_fst_reason_code", sqlStr("fst_reason_code"));
         wp.colSet(ii, "is_db_chgback_fst_doc_mark", sqlStr("fst_doc_mark"));
         wp.colSet(ii, "is_fst_twd_amt", "" + zzCurr.dcAmt(sqlStr("curr_code"), sqlNum("fst_twd_amt"), sqlNum("fst_dc_amt")));
         wp.colSet(ii, "is_db_chgback_fst_msg", sqlStr("fst_msg"));
         if (!empty(sqlStr("sec_add_date"))) {
            if (!empty(sqlStr("sec_apr_date"))) {
               wp.colSet(ii, "is_fst_add_date", sqlStr("sec_apr_date"));
            }
            else {
               wp.colSet(ii, "is_fst_add_date", sqlStr("sec_add_date"));
            }

         }
         else {
            if (!empty(sqlStr("fst_apr_date"))) {
               wp.colSet(ii, "is_fst_add_date", sqlStr("fst_apr_date"));
            }
            else {
               wp.colSet(ii, "is_fst_add_date", sqlStr("fst_add_date"));
            }
         }
      }

   }

   if (ii < 0) {
      alertErr("請選擇列印項目");
      return;
   }
   wp.listCount[0] = (ii + 1);
   pdfPrint();
}

int selectBil(String ex_reference_no) throws Exception  {
   daoTid = "bil.";
   String sql1 = " select "
         + " * "
         + " from bil_bill "
         + " where reference_no = ? ";
   sqlSelect(sql1, new Object[]{ex_reference_no});
   if (sqlRowNum <= 0) return -1;

   return 1;
}

int selectDbb(String ex_reference_no) throws Exception  {
   daoTid = "Dbb.";
   String sql1 = " select "
         + " * "
         + " from dbb_bill "
         + " where reference_no = ? ";
   sqlSelect(sql1, new Object[]{ex_reference_no});
   if (sqlRowNum <= 0) return -1;

   return 1;
}

int selectPrbl(String ex_ctrl_seqno, String ex_bin_type) throws Exception  {
   daoTid = "Prbl.";
   String sql1 = " select "
         + " * "
         + " from rsk_problem"
         + " where ctrl_seqno = ? "
         + " and bin_type = ? ";
   sqlSelect(sql1, new Object[]{ex_ctrl_seqno, ex_bin_type});
   if (sqlRowNum <= 0) return -1;

   return 1;
}

int selectRept(String ex_ctrl_seqno, String ex_bin_type) throws Exception  {
   daoTid = "Rept.";
   String sql1 = " select "
         + " * "
         + " from rsk_receipt "
         + " where ctrl_seqno = ? "
         + " and bin_type = ? ";
   sqlSelect(sql1, new Object[]{ex_ctrl_seqno, ex_bin_type});
   if (sqlRowNum <= 0) return -1;

   return 1;
}

int selectChgb(String ex_ctrl_seqno, String ex_bin_type) throws Exception  {
   daoTid = "Chgb.";
   String sql1 = " select "
         + " * "
         + " from rsk_chgback "
         + " where ctrl_seqno = ? "
         + " and bin_type = ? ";
   sqlSelect(sql1, new Object[]{ex_ctrl_seqno, ex_bin_type});
   if (sqlRowNum <= 0) return -1;

   return 1;
}

int selectDbcCard(String ex_card_no) throws Exception  {
   String sql1 = " select "
         + " D.id_no , "
         + " D.chi_name , "
         + " C.corp_no , "
         + " C.acct_no as debit_acct_no "
         + " from dbc_card C join dbc_idno D on C.id_p_seqno =D.id_p_seqno "
         + " where C.card_no = ? ";
   sqlSelect(sql1, new Object[]{ex_card_no});
   if (sqlRowNum <= 0) return -1;
   return 1;
}

int selectCrdCard(String ex_card_no) throws Exception  {
   String sql1 = " select "
         + commSqlStr.ufunc("uf_idno_id(C.major_id_p_seqno) as major_id , ")
         + commSqlStr.ufunc("uf_corp_no(C.corp_p_seqno) as corp_no , ")
         + commSqlStr.ufunc("uf_idno_name(C.id_p_seqno) as chi_name ")
         + " from crd_card C "
         + " where C.card_no = ? ";
   sqlSelect(sql1, new Object[]{ex_card_no});
   if (sqlRowNum <= 0) return -1;
   return 1;
}

void selectRskProblem(String ex_ctrl_seqno, String ex_bin_type) throws Exception  {
   String sql1 = " select "
         + " prb_fraud_rpt , "
         + " prb_amount , "
         + " dc_prb_amount , "
         + commSqlStr.ufunc("uf_dc_amt2(prb_amount,dc_prb_amount) as dd_prb_amount ")
         + " from rsk_problem "
         + " where ctrl_seqno = ? "
         + " and bin_type = ? ";
   sqlSelect(sql1, new Object[]{ex_ctrl_seqno, ex_bin_type});
   if (sqlRowNum <= 0) return;
   lsPrbFraudRpt = sqlStr("prb_fraud_rpt");
   liDdPrbAmount = sqlNum("dd_prb_amount");
   liPrbAmount = sqlNum("prb_amount");
   liDcPrbAmount = sqlNum("dc_prb_amount");
}

void chgbDecode(String ex_ctrl_seqno, String ex_bin_type) throws Exception  {
   String sql1 = " select "
         + " decode(chg_times,2,sec_add_date,fst_add_date) as fst_add_date ,"
         + " decode(chg_times,2,sec_apr_date,fst_apr_date) as fst_apr_date ,"
         + " decode(chg_times,2,sec_reason_code,fst_reason_code) as fst_reason_code ,"
         + " decode(chg_times,2,sec_doc_mark,fst_doc_mark) as fst_doc_mark ,"
         + " decode(chg_times,2,sec_twd_amt,fst_twd_amt) as fst_twd_amt ,"
         + " decode(chg_times,2,sec_dc_amt,fst_dc_amt) as fst_dc_amt ,"
         + " decode(chg_times,2,sec_msg,fst_msg) as fst_msg ,"
         + " decode(chg_times,2,fst_add_date,sec_add_date) as sec_add_date ,"
         + " decode(chg_times,2,fst_apr_date,sec_apr_date) as sec_apr_date ,"
         + " curr_code "
         + " from rsk_chgback "
         + " where ctrl_seqno = ? "
         + " and bin_type = ? ";
   sqlSelect(sql1, new Object[]{ex_ctrl_seqno, ex_bin_type});
}

void checkUpdate() throws Exception {
   int ll_ok = 0, ll_err = 0;

   String[] aa_opt = wp.itemBuff("opt");
   wp.listCount[0] = wp.itemRows("reference_no");
   int rr = optToIndex(aa_opt[0]);
   if (rr < 0) {
      alertErr("請點選列印完成資料");
      return;
   }

   for (int ii = 0; ii < aa_opt.length; ii++) {
      rr = optToIndex(aa_opt[ii]);
      if (rr < 0) continue;

      optOkflag(rr);
      String ls_ref_no = wp.itemStr(rr, "reference_no");
      String lsDebit = wp.itemStr(rr, "debit_flag");
      if (updateData(rr) > 0) {
         ll_ok++;
         wp.colSet(rr, "ok_flag", "V");
         this.sqlCommit(1);
      }
      else {
         ll_err++;
         wp.colSet(rr, "ok_flag", "X");
         this.sqlCommit(-1);
      }
   }

   alertMsg("列印確認完成 : OK:" + ll_ok + " ERR:" + ll_err);
}

int updateData(int rr) throws Exception {
   msgOK();
   String ls_ref_no = wp.itemStr(rr, "reference_no");
   boolean lbDebit = wp.itemEq(rr, "debit_flag", "Y");
   String sql1 = " update bil_bill set "
         + " rsk_print_flag = 'Y' "
         + " where reference_no =? and rsk_ctrl_seqno<>''";
   if (lbDebit) {
      sql1 = " update dbb_bill set "
            + " rsk_print_flag = 'Y' "
            + " where reference_no =? and rsk_ctrl_seqno<>''";
   }
   setString(1, ls_ref_no);
   sqlExec(sql1);
   if (sqlRowNum <= 0) return -1;
   return 1;
}

}

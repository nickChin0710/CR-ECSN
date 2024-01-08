package rskr01;
/**
 * 2020-0617   JH    modify
 * 2020-0609   JH    modify
 */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDFLine;

public class Rskr0260 extends BaseQuery implements InfacePdf {
taroko.base.CommDate zzdate=new CommDate();

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "Q":
         queryFunc(); break;
      case "R":
         dataRead(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "L":
         strAction = "";
         clearFunc(); break;
      case "PDF":
         pdfPrint(); break;
   }

   dddwSelect();
   initButton();

}

@Override
public void queryFunc() throws Exception {
   String lsWhere ="where A.prb_status ='30' and A.sign_flag <>'-'"+
         " and not exists (select 1 from rsk_chgback where reference_no=A.reference_no)"+
         " and not exists (select 1 from rsk_precompl where reference_no=A.reference_no)"+
         " and not exists (select 1 from rsk_prearbit where reference_no=A.reference_no)";
   lsWhere +=sqlStrend(wp.itemStr("ex_date1"),wp.itemStr("ex_date2"),"A.purchase_date")
   + sqlCol(wp.itemStr("ex_user").toLowerCase(),"lower(A.add_user)");

   if (wp.itemEq("ex_dbcard_flag", "1")) {
      lsWhere += " and A.debit_flag <>'Y' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "2")) {
      lsWhere += " and A.debit_flag = 'Y' ";
   }
   else if (wp.itemEq("ex_dbcard_flag", "3")) {
      lsWhere += " and A.bill_type = 'TSCC' ";
   }

//   sum_NU_read(lsWhere);
//   sum_U_read(lsWhere);
//   sum_read(lsWhere);

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {

   wp.pageControl();

   wp.selectSQL = ""
         + " A.card_no , A.reference_no, A.reference_seq, "
         + " uf_idno_name2(A.card_no,A.debit_flag) as db_idno_name, "
         + " A.acct_type , "
         + " uf_acno_key2(A.card_no,A.acct_type) as acct_key, "
         + " A.bill_type, A.ctrl_seqno,"
         + " A.purchase_date,"
         + " A.dc_dest_amt, A.add_user, A.dc_prb_amount"
         +", A.prb_reason_code, A.txn_code"
   +", B.add_date as rept_add_date"
   ;
   wp.daoTable = "rsk_problem A left join rsk_receipt B on A.reference_no=B.reference_no";
   wp.whereOrder = " order by A.purchase_date, A.ctrl_seqno  ";

   pageQuery();
   wp.setListCount(1);
   wp.setPageValue();
   if (sqlRowNum <= 0) {
//      err_alert("此條件查無資料");
      return;
   }

   queryAfter();
}

void queryAfter() throws Exception  {

   String sql1 ="select B.source_type as ctfc_source_type"
         +" from rsk_ctfc_txn A join rsk_ctfc_mast B on A.case_no=B.case_no"
         +" where A.reference_no =?"
         +commSqlStr.rownum(1);
   String sql2 ="select add_date as rept_add_date from rsk_receipt"
         +" where reference_no =?"+commSqlStr.rownum(1);

   int ll_nrow =wp.listCount[0];
   for(int ii=0; ii<ll_nrow; ii++) {
      String ls_refno =wp.colStr(ii,"reference_no");
      sqlSelect(sql1, ls_refno);
      if (sqlRowNum >0) {
         sql2wp(ii,"ctfc_source_type");
      }
      
      if (eqIgno(strAction,"PDF")) {
      	wp.colSet(ii, "card_no",commString.hideCardNo(wp.colStr(ii,"card_no")));
      	wp.colSet(ii, "acct_key",commString.hideIdno(wp.colStr(ii,"acct_key")));
      	wp.colSet(ii, "hh_idno_name",commString.hideIdnoName(wp.colStr(ii,"db_idno_name")));
      }            
   }   
}

void set0() {
   wp.colSet("wk_Nu", "0");
   wp.colSet("wk_N1", "0");
   wp.colSet("wk_U", "0");
   wp.colSet("wk_U1", "0");
   wp.colSet("wk_A", "0");
   wp.colSet("wk_A1", "0");
}

void sum_NU_read(String lsWhere) throws Exception {
   String NU_where = lsWhere + " and bill_type <> 'TSCC' ";
   wp.selectSQL = ""
         + " count(*) as wk_Nu,"
         + " sum(dest_amt) as wk_N1";
   wp.daoTable = "rsk_chgback";
   wp.whereStr = NU_where;
   pageSelect();
}

void sum_U_read(String lsWhere) throws Exception {
   String U_where = lsWhere + " and bill_type ='TSCC' ";
   wp.selectSQL = ""
         + " count(*) as wk_U,"
         + " sum(dest_amt) as wk_U1";
   wp.daoTable = "rsk_chgback";
   wp.whereStr = U_where;
   pageSelect();
}

void sum_read(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " count(*) as wk_A,"
         + " sum(dest_amt) as wk_A1";
   wp.daoTable = "rsk_chgback";
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
   wp.reportId = "rskr0260";
   wp.pageRows = 9999;
   String ss = "印表期間: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
         + commString.strToYmd(wp.itemStr("ex_date2"));
   wp.colSet("cond1", ss);
   wp.colSet("user_id", wp.loginUser);
   queryFunc();
   if(sqlNotFind()) {
	   wp.respHtml = this.errPagePDF;
	   return ;
   }
   TarokoPDFLine pdf = new TarokoPDFLine();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0260.xlsx";
//	pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

@Override
public void initButton() {
   String lsDate2 =zzdate.dateAdd(wp.sysDate,0,0,-60);
   wp.colSet("ex_date2",lsDate2);
}

}

package rskr01;
/**
 * 2020-0526   JH    bugfix
 * 2020-0515   JH    pdf.bug
 * 2020-0505   JH    modify
 * 2020-0327	JH		modify
 * 2019-1211   JH    ++prbl_amt
 * 2019-0621:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDFLine;

public class Rskr3030 extends BaseAction implements InfacePdf {
String isWhere1 = "", isWhere2 = "";
String isIdPseqno1 = "", isIdPSeqno2 = "";

@Override
public void userAction() throws Exception {
   strAction = wp.buttonCode;
   defaultAction();
   //--
   switch (wp.buttonCode) {
      case "PDF": //-PDF-
         strAction = "PDF";
         pdfPrint();
         break;
   }

}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (itemallEmpty("ex_add_date1,ex_add_date2,ex_card_no,ex_idno,ex_ctrl_seqno")) {
      alertErr("查詢條件: 不可全部空白");
      return;
   }

   if (this.chkStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2")) == false) {
      alertErr("新增登錄日期起迄：輸入錯誤");
      return;
   }

//   if (getWhereStr() == false) return;

//   wp.whereStr = isWhere1;
//   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

boolean getWhereStr()  throws Exception {
   isWhere1 = " where 1=1" + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%");
   isWhere2 = " where 1=1" + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%");
   String ls_card_no = wp.itemStr("ex_card_no");
   String ls_idno = wp.itemStr("ex_idno");
   if (!empty(ls_card_no)) {
      isWhere1 += sqlCol(ls_card_no, "card_no");
      isWhere2 += sqlCol(ls_card_no, "card_no");
   }
   else if (!empty(ls_idno)) {
      selectIdPseqno(ls_idno);
      if (!empty(isIdPseqno1) || !empty(isIdPSeqno2)) {
         isWhere1 += " and card_no in (select card_no from cca_card_base where id_p_seqno in (?,?))";
         isWhere2 += " and card_no in (select card_no from cca_card_base where id_p_seqno in (?,?))";
         setString(commString.nvl(isIdPseqno1, "xxx"));
         setString(commString.nvl(isIdPSeqno2, "xxx"));
      }
      else {
         alertErr("身分證ID:輸入錯誤");
         return false;
      }
   }

   //--
   String lsTable = wp.itemStr("ex_table");
   String lsDate1 = wp.itemStr("ex_add_date1");
   String lsDate2 = wp.itemStr("ex_add_date2");
   String lsAprFlag = wp.itemStr("ex_apr_flag");
   String lsClose = wp.itemStr("ex_close_flag");
   if (eqIgno(lsTable, "com")) {
      if (!empty(lsDate1) || !empty(lsDate2)) {
    	  isWhere1 += sqlCol(lsDate1,"pre_add_date",">=")
    			   + sqlCol(lsDate2,"pre_add_date","<=")
    			   ;
    	  isWhere2 += sqlCol(lsDate1,"pre_add_date",">=")
   			   	   + sqlCol(lsDate2,"pre_add_date","<=")
   			   	   ;    	  
//         isWhere1 += sqlStrend(lsDate1, lsDate2, "pre_add_date");
//         isWhere2 += sqlStrend(lsDate1, lsDate2, "pre_add_date");
      }
      if (eqIgno(lsAprFlag, "N")) {
         isWhere1 += " and pre_apr_date=''";
         isWhere2 += " and com_apr_date=''";
      }
      else if (eqIgno(lsAprFlag, "Y")) {
         isWhere1 += " and pre_apr_date<>''";
         isWhere2 += " and com_apr_date<>''";
      }
      if (eqIgno(lsClose, "1")) {
         isWhere1 += " and pre_close_date<>''";
         isWhere2 += " and com_close_date<>''";
      }
      else if (eqIgno(lsClose, "2")) {
         isWhere1 += " and pre_close_date=''";
         isWhere2 += " and com_close_date=''";
      }
   }
   else if (eqIgno(lsTable, "arb")) {
      if (!empty(lsDate1) || !empty(lsDate2)) {    	  
    	  isWhere1 += sqlCol(lsDate1,"pre_add_date",">=")
   			   	   + sqlCol(lsDate2,"pre_add_date","<=")
   			   	   ;
    	  isWhere2 += sqlCol(lsDate1,"pre_add_date",">=")
  			   	   + sqlCol(lsDate2,"pre_add_date","<=")
  			   	   ;    	      	  
//         isWhere1 += sqlStrend(lsDate1, lsDate2, "pre_add_date");
//         isWhere2 += sqlStrend(lsDate1, lsDate2, "pre_add_date");
      }
      if (eqIgno(lsAprFlag, "N")) {
         isWhere1 += " and pre_apr_date=''";
         isWhere2 += " and arb_apr_date=''";
      }
      else if (eqIgno(lsAprFlag, "Y")) {
         isWhere1 += " and pre_apr_date<>''";
         isWhere2 += " and arb_apr_date<>''";
      }
      if (eqIgno(lsClose, "1")) {
         isWhere1 += " and pre_close_date<>''";
         isWhere2 += " and arb_close_date<>''";
      }
      else if (eqIgno(lsClose, "2")) {
         isWhere1 += " and pre_close_date=''";
         isWhere2 += " and arb_close_date=''";
      }
   }

   return true;
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   if (getWhereStr() == false) return;
   String lsTable = wp.itemStr("ex_table");
   if (wp.itemEq("ex_table", "com")) {
      sqlSelectCom();
   }
   else if (wp.itemEq("ex_table", "arb")) {
      sqlSelectArb();
   }
   this.pageQuery();

   if (sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setListCount(1);
   wp.setPageValue();
   pageQueryAfter();
}

void sqlSelectCom() {
   wp.sqlCmd = " select "
         + " 'COM' as db_table, 1 as xx_times, bin_type,"
         +" 'pre-com' as db_table_times,"
         + " card_no , debit_flag ,"
         + " uf_idno_id2(card_no,debit_flag) as idno ,"
         + " ctrl_seqno, purchase_date, dest_amt, vcr_case_no,"
         + " pre_expire_date as expire_date,"
         + " pre_clo_result as clo_result , "
         + " pre_close_date as close_date ,"
         + " pre_add_date as add_date, pre_add_user as add_user, pre_amt as prbl_amt "
         + " from rsk_precompl " + isWhere1 + " and pre_add_date<>''"
         + " union select "
         + " 'COM' as db_table, 2 as xx_times, bin_type,"
         +" 'com' as db_table_times,"
         + " card_no , debit_flag ,"
         + " uf_idno_id2(card_no,debit_flag) as idno ,"
         + " ctrl_seqno, purchase_date, dest_amt, vcr_case_no,"
         + " COM_EXPIRE_DATE as expire_date,"
         + " com_clo_result as clo_result , "
         + " com_close_date as close_date ,"
         + " com_add_date as add_date, com_add_user as add_user, com_amt as prbl_amt "
         + " from rsk_precompl " + isWhere2 + " and com_add_date<>''" +
         //"  order by ctrl_seqno, xx_times"
         " order by expire_date, ctrl_seqno";
   ;
   wp.pageCountSql = "select sum(xx_cnt) from (select count(*) as xx_cnt from rsk_precompl " +
         isWhere1 + " and pre_add_date<>''" +
         " union select count(*) as xx_cnt from rsk_precompl" + isWhere2 + " and com_add_date<>'' )";

}

void sqlSelectArb() {
   wp.sqlCmd = " select "
         + " 'ARB' as db_table, 1 as xx_times, bin_type,"
         +" 'pre-arb' as db_table_times,"
         + " card_no, debit_flag ,"
         + " uf_idno_id2(card_no,debit_flag) as idno ,"
         + " ctrl_seqno, purchase_date, dest_amt ,"
         + " pre_expire_date as expire_date,"
         + " vcr_case_no ,"
         + " pre_result as clo_result ,"
         + " pre_close_date as close_date ,"
         + " pre_add_date as add_date,"
         + " pre_add_user as add_user, 0 as prbl_amt"
         + " from rsk_prearbit " + isWhere1 + " and pre_add_date<>''"
         + " union select "
         + " 'ARB' as db_table, 2 as xx_times, bin_type,"
         +" 'arb' as db_table_times,"
         + " card_no, debit_flag ,"
         + " uf_idno_id2(card_no,debit_flag) as idno ,"
         + " ctrl_seqno, purchase_date, dest_amt ,"
         + " pre_expire_date as expire_date,"
         + " vcr_case_no ,"
         + " arb_result as clo_result ,"
         + " arb_close_date as close_date ,"
         + " arb_add_date as add_date,"
         + " arb_add_user as add_user, 0 as prbl_amt"
         + " from rsk_prearbit " + isWhere2 + " and arb_add_date<>''"
//         + " order by ctrl_seqno, xx_times "
   +" order by expire_date, ctrl_seqno"
   ;

   wp.pageCountSql = "select sum(xx_cnt) from (select count(*) as xx_cnt from rsk_prearbit " +
         isWhere1 + " and pre_add_date<>''" +
         " union select count(*) as xx_cnt from rsk_prearbit" + isWhere2 + " and arb_add_date<>'' )";
}

void pageQueryAfter() {
   int ll_nrow = wp.listCount[0];

   //tt_clo_result
   for (int ll = 0; ll < ll_nrow; ll++) {
      String lsTable = wp.colStr(ll, "db_table");
      String lsResult = wp.colStr(ll, "clo_result");
      
      boolean lbPdf = eqIgno(wp.buttonCode, "PDF");
      if(lbPdf) {
			wp.colSet(ll,"card_no", commString.hideCardNo(wp.colStr(ll,"card_no")));
			wp.colSet(ll,"idno", commString.hideIdno(wp.colStr(ll,"idno")));			
		}
      
      int liTime = wp.colInt("xx_times");
      if (empty(lsResult)) continue;

      if (commString.strIn(lsTable, ",COM")) {
         if (liTime == 1) {
            wp.colSet(ll, "tt_clo_result", ecsfunc.DeCodeRsk.complPreResult(lsResult));
         }
         else if (liTime == 2) {
            wp.colSet(ll, "tt_clo_result", ecsfunc.DeCodeRsk.complCloResult(lsResult));
         }
         continue;
      }
      if (commString.strIn(lsTable, ",ARB")) {
         if (liTime == 1) {
            wp.colSet(ll, "tt_clo_result", ecsfunc.DeCodeRsk.arbitPreResult(lsResult));
         }
         else if (liTime == 2) {
            wp.colSet(ll, "tt_clo_result", ecsfunc.DeCodeRsk.arbitCloResult(lsResult));
         }
         continue;
      }
   }
}

void selectIdPseqno(String ls_id_no) throws Exception  {

   String sql1 = " select id_p_seqno from crd_idno where id_no = ? ";
   String sql2 = " select id_p_seqno from dbc_idno where id_no = ? ";

   sqlSelect(sql1, new Object[]{ls_id_no});
   if (sqlRowNum > 0) isIdPseqno1 = sqlStr("id_p_seqno");

   sqlSelect(sql2, new Object[]{ls_id_no});
   if (sqlRowNum > 0) isIdPSeqno2 = sqlStr("id_p_seqno");
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
   wp.reportId = "rskr3030";
   wp.pageRows = 9999;
   queryFunc();

//   ecsfunc.hiData oohh = new ecsfunc.hiData();
//   oohh.hh_cardno("card_no");
//   oohh.hh_idno("idno");
//   oohh.hidata_wp(wp);

   TarokoPDFLine pdf = new TarokoPDFLine();
//      pdf.pageCount =35;

   String tt = "";
   tt = "新增登錄日期: " + commString.strToYmd(wp.itemStr("ex_add_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_add_date2"))
   ;
   if (eqIgno(wp.itemStr("ex_close_flag"), "0"))
      tt += "  全部  ";
   else if (eqIgno(wp.itemStr("ex_close_flag"), "1"))
      tt += "  結案  ";
   else if (eqIgno(wp.itemStr("ex_close_flag"), "2"))
      tt += "  未結案  ";
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3030.xlsx";
   pdf.sheetNo = 0;

   pdf.procesPDFreport(wp);
   pdf = null;
   return;


}

}

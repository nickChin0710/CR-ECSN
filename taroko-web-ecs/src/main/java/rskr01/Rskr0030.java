package rskr01;
/**
 * 2022-0321   JH    ++ex_clo_result
 * 2020-0902   JH    orderBY
 * 2020-0428   JH    modify
 * */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDFLine;

public class Rskr0030 extends BaseQuery implements InfacePdf {
public String modVersion() { return "v22.0321"; }
String lsWhere = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "Q": //查詢功能 --
         queryFunc(); break;
      case "M": //瀏覽功能 :skip-page --
         queryRead(); break;
      case "S": //動態查詢--
         querySelect(); break;
      case "L": //清畫面 --
         strAction = "";
         clearFunc(); break;
      case "XLS":  // -Excel-
         strAction = "XLS";
         // xlsPrint();
         break;
      case "PDF": // -PDF-
         strAction = "PDF";
         pdfPrint(); break;
   }

   dddwSelect();
   initButton();
}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("結案登錄日期起迄：輸入錯誤");
      return;
   }
   
   if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2") && wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_ctrl_seqno")) {	  
	   set0();
	   alertErr2("結案登錄日期,卡號,控制流水號 不可全部空白");
	   return;
   }
   
   getWhereStr();
   
   this.setSqlParmNoClear(true);
   sumRead(lsWhere);

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

void getWhereStr() throws Exception {	  	
	lsWhere = " where 1=1 "
			+ sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
		    + sqlCol(wp.itemStr("ex_card_no"), "card_no")
	  		+ sqlCol(wp.itemStr("ex_clo_result"), "clo_result")
			;
	  
	if(wp.itemEmpty("ex_date1") == false) {
		lsWhere += " and ((close_add_date <> '' and close_add_date >= :ex_date1) or (close_add_date_2 <> '' and close_add_date_2 >= :ex_date1)) ";
		setString("ex_date1",wp.itemStr("ex_date1"));
	}
	  
	if(wp.itemEmpty("ex_date2") == false) {
		lsWhere += " and ((close_add_date <> '' and close_add_date <= :ex_date2) or (close_add_date_2 <> '' and close_add_date_2 <= :ex_date2)) ";
		setString("ex_date2",wp.itemStr("ex_date2"));
	}
	  
	if(wp.itemEmpty("ex_id1") == false) {
		lsWhere += " and (close_add_user like :ex_id1 or close_add_user_2 like :ex_id1) ";
		setString("ex_id1",wp.itemStr("ex_id1")+"%");
	}
	  
	if (wp.itemEq("ex_apr_flag", "Y")) {
		if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2") && wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_ctrl_seqno")) {			  
			set0();
		    alertErr2("結案登錄日期,卡號,控制流水號 不可全部空白");
		    return;
		}
		lsWhere += " and prb_status in ('80','85') ";
	}	else	{
		lsWhere += " and prb_status in ('60','83') ";
	}
	  
	if (wp.itemEq("ex_dbcard_flag", "1")) {		 
		lsWhere += " and debit_flag <>'Y' and bill_type <>'TSCC' ";
	} else if (wp.itemEq("ex_dbcard_flag", "2")) {
		lsWhere += " and debit_flag ='Y' and bill_type <>'TSCC' ";
	} else if (wp.itemEq("ex_dbcard_flag", "3")) {
	    lsWhere += " and bill_type = 'TSCC' ";
	}
	
	if (wp.itemEq("ex_type", "Q")) {
	    lsWhere += " and prb_mark = 'Q' ";
	} else if (wp.itemEq("ex_type", "S")) {
	    lsWhere += " and prb_mark = 'S' ";
	} else if (wp.itemEq("ex_type", "E")) {
	    lsWhere += " and prb_mark = 'E' ";
	}	  	  	  
}

@Override
public void queryRead() throws Exception {

   wp.pageControl();

   wp.selectSQL = ""
         + " card_no , "
         + " uf_hi_cardno(card_no) as hh_card_no , "
         + " replace(uf_idno_name(card_no),'　','') as db_idno_name , "
         + " uf_hi_cname(replace(uf_idno_name(card_no),'　','')) as hh_name , "
         + " ctrl_seqno , "
         + " purchase_date , "
         + " prb_amount, dc_prb_amount,"
         + " mcht_chi_name,"
         + " prb_reason_code,"
         + " uf_dc_curr(curr_code) as curr_code , "
         + " uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt , "
         + " uf_dc_amt2(mcht_repay,dc_mcht_repay) as dc_mcht_repay , "
         + " uf_dc_amt2(mcht_repay_2,dc_mcht_repay_2) as dc_mcht_repay_2 , "
         + " close_add_date,"
         + " close_add_user,"
         + " clo_result,"
         + " clo_result_2,"
         + " bin_type,"
         + " bill_type"
   ;
   wp.daoTable = "rsk_problem";
   wp.whereOrder = " order by close_add_user ASC, bin_type, ctrl_seqno ";
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

void queryAfter() throws Exception {
   double liNum = 0;
   boolean lbPdf = eqIgno(wp.buttonCode, "PDF");
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      liNum = wp.colNum(ii, "dc_prb_amount") - wp.colNum(ii, "dc_mcht_repay") - wp.colNum(ii, "dc_mcht_repay_2");
      wp.colSet(ii, "wk_diff_amt", "" + liNum);
      if(lbPdf) {
      	wp.colSet(ii, "card_no",wp.colStr(ii,"hh_card_no"));
      	wp.colSet(ii, "hh_name",commString.mid(wp.colStr(ii,"hh_name"), 0,10));
      }
   }
}

void set0() {
   wp.colSet("wk_cnt", "" + 0);
   wp.colSet("wk_dest_amt", "" + 0);
   wp.colSet("wk_prb_amt", "" + 0);
   wp.colSet("wk_repay_amt", "" + 0);
   wp.colSet("wk_diff_amt", "" + 0);
}


void sumRead(String lsWhere) throws Exception {
   wp.selectSQL = ""
         + " count(*) as wk_cnt,"
         + " sum(uf_dc_amt2(dest_amt,dc_dest_amt)) as wk_dest_amt,"
         +" sum(uf_dc_amt2(prb_amount,dc_prb_amount)) as wk_prb_amt,"
         + " sum(uf_dc_amt2(mcht_repay,dc_mcht_repay)) as wk_repay_amt"
   ;
   wp.daoTable = "rsk_problem";
   wp.whereStr = lsWhere;
   pageSelect();
   if (wp.colNum(0, "wk_cnt") == 0) {
      wp.colSet("wk_cnt", "" + 0);
      wp.colSet("wk_dest_amt", "" + 0);
      wp.colSet("wk_prb_amt", "" + 0);
      wp.colSet("wk_repay_amt", "" + 0);
      wp.colSet("wk_diff_amt", "" + 0);
   }
   else {
      wp.colSet("wk_diff_amt", wp.colNum("wk_dest_amt") - wp.colNum("wk_repay_amt"));
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
   wp.reportId = "rskr0030";
   String ss;
   ss = "交易卡別 : " + wp.itemStr("ex_dbcard_flag")
         + " 結果登錄日期 :" + commString.strToYmd(wp.itemStr("ex_date1"))
         + " -- " + commString.strToYmd(wp.itemStr("ex_date2"))
         + " "
         + "結果登錄者:: "
         + wp.itemStr("ex_id1");
   wp.colSet("cond1", ss);
   wp.pageRows = 9999;
   queryFunc();

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0030.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}

package cmsr02;
/*
 *108-06-17:   JH    p_xxx >>acno_p_xxx
 *109-04-28  shiyuqi       updated for project coding standard     *
 *109-08-03  JustinWu   change the sql querying the data 
 *109-08-05  JustinWu   check date values are not null and two dates are in certain interval
 * 110-01-05  V1.00.03  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         * 
 * 110-01-06  V1.00.03  shiyuqi       修改无意义命名         
 * 111-11-02  V1.00.03  machao        頁面bug調整                                                                            *    
 * 112-03-19  V1.00.04  Zuwei Su      列表查詢bug  
 * 112-10-22  V1.00.05  sunny         將問交交易從原本正向表列改以排除的方式處理                                  *    
 * */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Cmsr4080 extends BaseAction implements InfacePdf {
  private static final String progTitle = "案件類別及消費統計報表";

taroko.base.CommDate commDate = new taroko.base.CommDate();

  String lsDate1 = "", lsDate2 = "", lsValidDate = "";
  String lsRcvDate1 = "", lsRcvDate2 = "", lsCaseType1 = "", lsCaseType2 = "";
  double ldcDest = 0, ldcMaxamt = 0, ldcTolamt = 0, ldcTolcnt = 0;
  String lsCardNo = "", lsPurDate1 = "";
  String lsWhere = "";
  int rr = 0;
  int ldcYearAmt = 0;
 // int cc = 0, aa = 1;
  //int bb = 0;
  //int zz = 0;

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    } 
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsr4080")) {
        wp.optionKey = wp.colStr(0, "ex_case_type1");
        dddwList("dddw_casetype01", "cms_casetype", "case_id", "case_id", "where 1=1");
      }
    } catch (Exception ex) {
    }
    try {
      if (eqIgno(wp.respHtml, "cmsr4080")) {
        wp.optionKey = wp.colStr(0, "ex_case_type2");
        dddwList("dddw_casetype02", "cms_casetype", "case_id", "case_id", "where 1=1");
      }
    } catch (Exception ex) {
    }
    
  }

  @Override
  public void queryFunc() throws Exception {
	  if ( ! checkRequireCol()) {
		  return;
	  }
	  
	  if (! checkDateInterval()) {
		  return;
	  }
	  
    if (this.chkStrend(wp.itemStr("ex_rcv_date1"), wp.itemStr("ex_rcv_date2")) == false) {
      alertErr2("接聽日期起迄：輸入錯誤");
      return;
    }
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
	  wp.pageControl();
	  
	  wp.selectSQL = ""
			  + "        c.case_user , "
			  + "        c.case_date , "
			  + "        c.case_idno , "
			  + "        c.card_no , "
			  + "        c.case_type ,"
			  +"         f.activate_flag as db_open_card, "
			  + "        f.current_code as db_current_code,"
			  +"         e.chi_name as db_cname,"
			  +"         e.home_area_code1||'-'||e.home_tel_no1||'-'||e.home_tel_ext1 as db_htelno,"
			  +"         e.cellar_phone as db_cellno,"
			  +"         d.ldc_maxamt, "
			  + "        d.ldc_tolamt as db_sum_dest_amt, "
			  + "        nvl(d.ldc_purchaseamt,0,d.ldc_purchaseamt) as db_sum_set_purchase_amt, "
			  + "        d.ldc_tolcnt, "
			  + "        d.min_purdate, "
			  + "        d.max_purdate ";
	  
	  wp.daoTable = ""
			  + "        cms_casemaster c"
			  +"         LEFT JOIN crd_idno e ON e.id_no=c.case_idno"
			  +"         LEFT JOIN crd_card f ON f.card_no=c.card_no"
			  +"         LEFT JOIN ("
			  +"		         select card_no AS card_no_b, "
			  + "                          min(purchase_date) AS min_purdate, "
			  + "                          max(purchase_date) AS  max_purdate,  "
			  + "                          max(db_dest_amt) as ldc_maxamt, "
			  + "                          sum(db_dest_amt) as ldc_tolamt ,"
			  +"		                      sum(pur_dest_amt) as ldc_purchaseamt,"
			  +"		                      sum(db_dest_cnt) as ldc_tolcnt "
			  +"		         from ("
			  +"			         select CARD_NO, "
			  +"                               REFERENCE_NO, "
			  +"                               TXN_CODE, "
			  +"                               PURCHASE_DATE, "
			  +"			                      uf_tx_sign(txn_code)*dest_amt as db_dest_amt ,"
			  +"			                      uf_tx_sign(txn_code)*(CASE when DEST_AMT>= ? then DEST_AMT else '0' ";
      setDouble( wp.itemNum("ex_dest_amt"));
      
	  wp.daoTable += "                                 END) AS pur_dest_amt,	"
			  +"	    	                      uf_tx_sign(txn_code) as db_dest_cnt "
			  +"			         from bil_bill "
			  +"			         where 1=1"
			  //+"			              and nvl(rsk_type,' ') in (' ','4') "
			  +"			              and nvl(rsk_type,' ') not in ('1','2','3') " //不是問交
			  +"                       and acct_code in ('BL','CA','IT') 	"
			  +                         sqlCol(wp.itemStr("ex_pur_date1"), "purchase_date", ">=")
			  +                         sqlCol(wp.itemStr("ex_pur_date2"), "purchase_date", "<=")
			  +                         sqlCol(wp.itemStr("ex_card_no"), "card_no")
			  +"			                )"
			  +"		             group by card_no"
			  +"         ) d ON c.card_no=d.card_no_b";

	    lsWhere =
	        " where 1=1 "
	            + " and c.card_no <> '' " 
	            + sqlCol(wp.itemStr("ex_rcv_date1"), "c.case_date", ">=")
	            + sqlCol(wp.itemStr("ex_rcv_date2"), "c.case_date", "<=")
	            + sqlCol(wp.itemStr("ex_case_type1"), "c.case_type", ">=")
	            + sqlCol(wp.itemStr("ex_case_type2"), "c.case_type", "<=")
	            + sqlCol(wp.itemStr("ex_idno"), "c.case_idno")
	            + sqlCol(wp.itemStr("ex_card_no"), "c.card_no")
	            ;

	    String lsCaseUser = "";
	    if (!wp.itemEmpty("ex_user_id01")) {
	      if (empty(lsCaseUser))
	        lsCaseUser += " and c.case_user in ('" + wp.itemStr("ex_user_id01") + "'";
	      else
	        lsCaseUser += " ,'" + wp.itemStr("ex_user_id01") + "'";
	    }
	    if (!wp.itemEmpty("ex_user_id02")) {
	      if (empty(lsCaseUser))
	        lsCaseUser += " and c.case_user in ('" + wp.itemStr("ex_user_id02") + "'";
	      else
	        lsCaseUser += " ,'" + wp.itemStr("ex_user_id02") + "'";
	    }
	    if (!wp.itemEmpty("ex_user_id03")) {
	      if (empty(lsCaseUser))
	        lsCaseUser += " and c.case_user in ('" + wp.itemStr("ex_user_id03") + "'";
	      else
	        lsCaseUser += " ,'" + wp.itemStr("ex_user_id03") + "'";
	    }
	    if (!wp.itemEmpty("ex_user_id04")) {
	      if (empty(lsCaseUser))
	        lsCaseUser += " and c.case_user in ('" + wp.itemStr("ex_user_id04") + "'";
	      else
	        lsCaseUser += " ,'" + wp.itemStr("ex_user_id04") + "'";
	    }
	    if (!wp.itemEmpty("ex_user_id05")) {
	      if (empty(lsCaseUser))
	        lsCaseUser += " and c.case_user in ('" + wp.itemStr("ex_user_id05") + "'";
	      else
	        lsCaseUser += " ,'" + wp.itemStr("ex_user_id05") + "'";
	    }
	    

	    if (!empty(lsCaseUser)) {
	      lsCaseUser += " )";
	      lsWhere += lsCaseUser;
	    }

	    wp.whereStr = lsWhere;
	    wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();

	  wp.whereOrder = " order by c.case_date, c.case_idno ,c.card_no ";

	pageQuery();
	
    rr = sqlRowNum;

    if (wp.selectCnt == 0) {
      alertErr2("查無資料");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();
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
    wp.colSet("ex_dest_amt", "0");

  }


  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "cmsr4080";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "接聽日期: " + commString.strToYmd(wp.itemStr("ex_rcv_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_rcv_date2"));
    wp.colSet("cond1", cond1);
    wp.colSet("progTitle", progTitle);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();

    wp.fileMode = "Y";
    pdf.excelTemplate = "cmsr4080.xlsx";
    pdf.pageCount = 32;
    pdf.sheetNo = 0;

    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

private boolean checkRequireCol() {
	if (wp.itemEmpty("ex_rcv_date1")) {
		alertErr2("接聽日期起日：輸入錯誤");
		 return false;
	}
    if (wp.itemEmpty("ex_rcv_date2")) {
    	alertErr2("接聽日期迄日：輸入錯誤");
		 return false;
	}
	if (wp.itemEmpty("ex_pur_date1")) {
		alertErr2("消費日期起日：輸入錯誤");
		 return false;
	}
    if (wp.itemEmpty("ex_pur_date2")) {
    	alertErr2("消費日期迄日：輸入錯誤");
		 return false;
	}    
    
	return true;
}

private boolean checkDateInterval() throws ParseException {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	if (computeDayDiff(wp.itemStr("ex_rcv_date1"), wp.itemStr("ex_rcv_date2"), sdf) > 31) {
		alertErr2("接聽日期起迄區間限查詢一個月內的資料");
		 return false;
	}
    if (computeDayDiff(wp.itemStr("ex_pur_date1"), wp.itemStr("ex_pur_date2"), sdf) > 365) {
    	alertErr2("消費日期起迄區間限查詢一年內的資料");
		 return false;
	} 
	  
	return true;
}

private long computeDayDiff(String dateStr1, String dateStr2, SimpleDateFormat sdf) throws ParseException {
	Date date1 = sdf.parse(dateStr1);
	Date date2 = sdf.parse(dateStr2);
	
	return (long) ( (date2.getTime() - date1.getTime())/(60*60*24*1000) );
}


}

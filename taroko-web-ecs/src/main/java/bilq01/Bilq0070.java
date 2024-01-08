/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-28  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 107-05-10  V1.00.02  Andy		  update : SQL ,UI,report                    *	
* 109-04-23  V1.00.03  shiyuqi       updated for project coding standard     * 
* 111-12-02  V1.00.04  Ryan       modify sqlCmd     * 
******************************************************************************/
package bilq01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Bilq0070 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "bilq0070";

  String condWhere = "";
  String reportSubtitle = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // wp.setExcelMode();
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      // wp.setExcelMode();
      pdfPrint();
    }

    dddwSelect();
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  private boolean getWhereStr() throws Exception {
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    // cond_where = wp.whereStr + "";
    // wp.whereStr =cond_where;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }


  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
   
    if (getWhereStr() == false)
      return;

    wp.sqlCmd = getSqlCmd();

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr);
    // wp.daoTable);

    // 重新計算頁筆數/跳頁
    wp.pageCountSql = " select count(*) from (";
    wp.pageCountSql += wp.sqlCmd;
    wp.pageCountSql += ")";

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    //
    wp.colSet("ft_cnt", "0");
    wp.colSet("sum_all_amt", "0");
    wp.colSet("sum_05_amt", "0");
    wp.colSet("sum_06_amt", "0");

    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();

    String sqlSelect = getSqlCmd();
    String lsSql = "";
    lsSql =
        "select sum(dest_amt) sum_all_amt, " + "sum(decode(txn_code,'05',dest_amt,'26',dest_amt,0)) sum_05_amt, "
            + "sum(decode(txn_code,'06',dest_amt,'25',dest_amt,0)) sum_06_amt "
            + "from (";
    lsSql += sqlSelect;
//    if (!empty(exID)) {
//      setString("ex_id", exID);
//    }
    lsSql += ")";
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      wp.colSet("sum_all_amt", numToStr(sqlNum("sum_all_amt"), "#,###"));
      wp.colSet("sum_05_amt", numToStr(sqlNum("sum_05_amt"), "#,###"));
      wp.colSet("sum_06_amt", numToStr(sqlNum("sum_06_amt"), "#,###"));
    }
    // list_wkdata();
  }
  
  String getSqlCmd(){
		String sqlCmd = "";
		String exDateS = wp.itemStr("exDateS");
		String exDateE = wp.itemStr("exDateE");
		String exDateS1 = wp.itemStr("exDateS1");
		String exDateE1 = wp.itemStr("exDateE1");
		String exTransType = wp.itemStr("ex_trans_type");
		String exID = wp.itemStr("ex_id");
		String exCardNo = wp.itemStr("ex_card_no");
		String exVCardNo = wp.itemStr("ex_v_card_no");
		String exPaymentType = wp.itemStr("ex_payment_type");
		String exWalletType = wp.itemStr("ex_wallet_type");

		sqlCmd = "select a.reference_no, " + "a.purchase_date, " + "a.post_date, " + "a.card_type, "
				+ "a.group_code, " + "a.card_no, " + "a.v_card_no, " + "a.dest_amt, "
				+ "decode(a.mcht_chi_name,'',a.mcht_eng_name,a.mcht_chi_name) as mcht_name, "
				+ " CASE WHEN nvl(c.WALLET_IDENTIFIER,'') = '103' THEN 'apple pay' " 
			    + "      WHEN nvl(c.WALLET_IDENTIFIER,'') = '216' THEN 'google pay' "
			    + "      WHEN nvl(c.WALLET_IDENTIFIER,'') = '217' THEN 'samsung pay' "
			    + "      WHEN nvl(c.WALLET_IDENTIFIER,'') = '327' THEN 'merchant token' "
			    + "      WHEN nvl(c.WALLET_IDENTIFIER,'') <> ''   THEN 'others pay' "
			    + "      ELSE 'taiwan pay' "
			    + " END AS wallet_type, "
				+ "a.mcht_city, " + "a.mcht_country, " 
				+ "decode(a.payment_type,'Q',a.payment_type,'t',a.payment_type,substr(a.ecs_platform_kind,1,1)) as payment_type, " 
				+ "uf_idno_id (a.id_p_seqno) id_no, " + "a.txn_code " 
				+ "from bil_bill a "
				+ "left join oempay_card c on a.v_card_no = c.v_card_no "
				+ "where 1=1 " ;
		if (!empty(exDateS1) || !empty(exDateE1)) {
			sqlCmd += sqlStrend(exDateS1, exDateE1, "a.post_date");
		}
		if (!empty(exDateS) || !empty(exDateE)) {
			sqlCmd += sqlStrend(exDateS, exDateE, "a.purchase_date");
		}
		if (!exTransType.equals("0")) {
			switch (exTransType) {
			case "P":
				sqlCmd += "and txn_code in ('05','26') ";
				break;
			case "R":
				sqlCmd += "and txn_code in ('25','06') ";
				break;
			}
		}
		
		if (!exPaymentType.equals("0")) {
			switch (exPaymentType) {
			case "Q":
				sqlCmd += "and a.payment_type = 'Q' ";
				break;
			case "t":
				sqlCmd += "and ( a.ecs_platform_kind = 't1' or a.payment_type = 't' ) ";
				break;
			}
		} else {
			sqlCmd += "and ( (a.v_card_no <> '') or (a.payment_type = 'Q' or (a.ecs_platform_kind = 't1' or a.payment_type = 't' ) ) ) ";
		}
		
		if (!exWalletType.equals("0")) {
			switch (exWalletType) {
			case "H":
				sqlCmd += "and nvl(c.WALLET_IDENTIFIER,'') = '' ";
				break;
			case "O":
				sqlCmd += "and nvl(c.WALLET_IDENTIFIER,'') <> '' ";
				break;
			}
		}
		
		if (!empty(exID)) {
			sqlCmd += "and a.id_p_seqno = (select id_p_seqno from crd_idno where 1=1 ";
			sqlCmd += sqlCol(exID, "id_no");
			sqlCmd += ")";
		}

		sqlCmd += sqlCol(exCardNo, "a.card_no");  //如果條件是空,回傳空值
		sqlCmd += sqlCol(exVCardNo, "a.v_card_no");

/*
		sqlCmd += "union " + "SELECT DISTINCT v0.reference_no, " + "a.purchase_date, " + "a.post_date, "
				+ "v0.card_type, " + "v0.group_code, " + "v0.card_no, " + "v_card_no, " + "a.dest_amt, " + "v0.id_no, "
				+ "a.txn_code " + "FROM ( " + "SELECT a.reference_no, " + "a.card_no, " + "b.group_code, "
				+ "b.card_type, " + "uf_idno_id (a.id_p_seqno) id_no " + "FROM bil_contract a, crd_card b "
				+ "WHERE 1=1 and a.card_no = b.card_no " + "AND a.installment_kind = '' " + ") v0, bil_bill a "
				+ "WHERE 2=2 and v0.reference_no = a.reference_no AND a.v_card_no != '' ";
		if (!empty(exDateS) || !empty(exDateE)) {
			sqlCmd += sqlStrend(exDateS, exDateE, "a.purchase_date");
		}
		if (!empty(exDateS1) || !empty(exDateE1)) {
			sqlCmd += sqlStrend(exDateS1, exDateE1, "a.post_date");
		}
		if (!exTransType.equals("0")) {
			switch (exTransType) {
			case "P":
				sqlCmd += "and txn_code = '05' ";
				break;
			case "R":
				sqlCmd += "and txn_code = '06' ";
				break;
			case "I":
				sqlCmd += "and txn_code = 'IN' ";
				break;
			}
		}
		if (!empty(exID)) {
			sqlCmd += "and a.id_p_seqno = (select id_p_seqno from crd_idno where 1=1 ";
//	          setString("ex_id", exID);
			sqlCmd += sqlCol(exID, "id_no");
			sqlCmd += ")";
		}

		sqlCmd += sqlCol(exCardNo, "a.card_no");
		sqlCmd += sqlCol(exVCardNo, "a.v_card_no");

		sqlCmd += "union " + "SELECT DISTINCT v2.reference_no, " + "a.purchase_date, "
				+ "a.purchase_date AS post_date, " + "v2.card_type, " + "v2.group_code, " + "v2.card_no, "
				+ "v_card_no, " + "a.dest_amt, " + "v2.id_no, " + "a.txn_code " + "FROM " + "(SELECT a.reference_no, "
				+ "a.card_no, " + "b.group_code, " + "b.card_type, " + "uf_idno_id (a.id_p_seqno) id_no "
				+ "FROM bil_contract a, crd_card b " + "WHERE 1=1 " + "and a.card_no = b.card_no "
				+ "AND installment_kind IN ('A', 'D', 'N') " + "AND reference_no != '' " + ") v2, bil_curpost a "
				+ "WHERE 3 = 3 and v2.reference_no = a.reference_no AND v_card_no != ''";
		if (!empty(exDateS) || !empty(exDateE)) {
			sqlCmd += sqlStrend(exDateS, exDateE, "a.purchase_date");
		}
		if (!empty(exDateS1) || !empty(exDateE1)) {
			sqlCmd += sqlStrend(exDateS1, exDateE1, "a.this_close_date");
		}
		if (!exTransType.equals("0")) {
			switch (exTransType) {
			case "P":
				sqlCmd += "and txn_code = '05' ";
				break;
			case "R":
				sqlCmd += "and txn_code = '06' ";
				break;
			case "I":
				sqlCmd += "and txn_code = 'IN' ";
				break;
			}
		}
		if (!empty(exID)) {
			sqlCmd += "and a.id_p_seqno = (select id_p_seqno from crd_idno where 1=1 ";
//	          setString("ex_id", exID);
			sqlCmd += sqlCol(exID, "id_no");
			sqlCmd += ")";
		}
		sqlCmd += sqlCol(exCardNo, "a.card_no");
		sqlCmd += sqlCol(exVCardNo, "a.v_card_no");
*/
		
		
		return sqlCmd;
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    int selCt = wp.selectCnt;
    String wkTxnCode = "";
    for (int ii = 0; ii < selCt; ii++) {
      // 計算欄位
      rowCt += 1;
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("user_id", wp.loginUser);

  }
  
  public String sqlStrend(String strName, String colName, String col) {
	  StringBuffer sqlStr = new StringBuffer();
	  if(!empty(strName)) {
		  sqlStr.append(" and ");
		  sqlStr.append(col);
		  sqlStr.append(" >= '");
		  sqlStr.append(strName);
		  sqlStr.append("' ");
	  }
	  if(!empty(colName)) {
		  sqlStr.append(" and ");
		  sqlStr.append(col);
		  sqlStr.append(" <= '");
		  sqlStr.append(colName);
		  sqlStr.append("' ");
	  }
	  return sqlStr.toString();
  }

  void subTitle() {}

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      queryFunc();
      wp.setListCount(1);
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);
      /*
       * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where); wp.listCount[1] =sql_nrow;
       * ddd("Summ: rowcnt:" + wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
       */
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");

    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);

    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_office_m_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_office");
      // dddw_list("dddw_office_m_code", "bil_office_m", "office_m_code", "office_m_name", "where
      // 1=1 group by office_m_code,office_m_name order by office_m_code");

    } catch (Exception ex) {
    }
  }

}


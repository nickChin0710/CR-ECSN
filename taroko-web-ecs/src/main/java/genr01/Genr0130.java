/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
 * 112-03-15  V1.00.00  Yang Han   program init                              *
 * 112-03-21  V1.00.01  Yang Han   error fix 
 * 112-04-10  V1.00.02  machao     增借方合計、貸方合計欄位;                               *
 * 112-05-03  V1.00.03  Ryan       增加科子細目類別查詢條件       
 * 112-05-05  V1.00.04  machao     合計金額計算調整       
 * 112-05-08  V1.00.05  machao     合計金額計算調整，查詢頁面tx_date條件微調                                                         *	
******************************************************************************/
package genr01;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDF2;

public class Genr0130 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "genr0130";
  int sun =0;

  String condWhere = "";

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
    } else if (eqIgno(wp.buttonCode, "R1")) { // 
        strAction = "R1";
        wp.listCount[0] = wp.itemBuffLen("SER_NUM");
    }


    dddwSelect();
//    initButton();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  private boolean getWhereStr() throws Exception {
	String txAcNo = wp.itemStr("ex_ac_no");
    String txDateF = wp.itemStr("exDateS");
    String txDateU = wp.itemStr("exDateE");

    String lsWhere = "where 1=1";
    if (empty(txAcNo) == false) {
        lsWhere += " and ac_no = :ac_no_f";
        setString("ac_no_f", txAcNo);
      }
    
    if (empty(txDateF) == false) {
      lsWhere += " and tx_date >= :tx_date_f";
      setString("tx_date_f", txDateF);
    }

    if (empty(txDateU) == false) {
      lsWhere += " and tx_date <= :tx_date_u";
      setString("tx_date_u", txDateU);
    }
    
    
    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  public void queryExcel() throws Exception {
    wp.setQueryMode();
    queryReadExcel();
  }
 
  private void setParameter() throws Exception {
  }
  

  @Override
  public void queryRead() throws Exception {
	wp.pageControl();
	String txAcNo = wp.itemStr("ex_ac_no");
	String txDateF = wp.itemStr("exDateS");
	String txDateU = wp.itemStr("exDateE");	  
    String dateStrSql = " ";
    String acNoStrSql = " ";

    if(empty(txAcNo)) {
    	  alertErr2("科子細目代碼不能為空值");
          return;
    }
    
    List<Object> paramList = new ArrayList();
    Boolean paramFlag = true;
    if (!empty(txAcNo)) {
    	dateStrSql =  dateStrSql +  " and ac_no = ?";
    	acNoStrSql =  " and ac_no <> ?";;
    	paramList.add(txAcNo);
    	paramList.add(txAcNo);
    }
    if (!empty(txDateF) || !empty(txDateU)) {
        dateStrSql =dateStrSql +  " and tx_date >= ? and tx_date <= ?" ;
        paramList.add(txDateF);
        paramList.add(txDateU);
    }
    wp.selectSQL = "" + "v.tx_date , " + " count(1) as voucher_num, "
    		+ "sum(CASE WHEN v.dbcr = 'C' Then amt ELSE 0 end) AS amtc, "
	        + "sum(CASE WHEN v.dbcr = 'D' Then amt ELSE 0 end) AS amtd, "
	        + "v.memo1  ";
		wp.daoTable = "gen_vouch_h v ";
	wp.whereStr =  "WHERE 1=1  " + acNoStrSql
			+ " AND tx_date || refno in "
			+ "(SELECT DISTINCT tx_date || refno FROM GEN_VOUCH_H "
			+ "WHERE 1=1  "
			+ dateStrSql
			+ ") group by v.memo1,v.tx_date ";
    wp.whereOrder = "ORDER BY tx_date, memo1 DESC ";
    wp.pageCountSql = "select count(*) from (SELECT v.tx_date ,  count(1) as voucher_num, "
    		+ "sum(CASE WHEN v.dbcr = 'C' Then amt ELSE 0 end) AS amtc, "
    		+ "sum(CASE WHEN v.dbcr = 'D' Then amt ELSE 0 end) AS amtd, v.memo1   "
    		+ "FROM gen_vouch_h v  "
    		+  "WHERE 1=1  " + acNoStrSql
    		+ "AND tx_date || refno in (SELECT DISTINCT tx_date || refno "
    		+ "FROM GEN_VOUCH_H WHERE 1=1   "
    		+ dateStrSql
    		+ ")group by v.memo1,v.tx_date) a" ;

    pageQuery(paramList.toArray());   
    wp.setListCount(1);
    sun = wp.selectCnt;
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    BigDecimal  balance_num = new BigDecimal (0);
    if(wp.dataCnt>20) {
    	String sql1 = "Select sum(c.amtc) as Sum_AmtC, sum(c.amtd) as Sum_AmtD from ( SELECT v.tx_date ,  count(1) as voucher_num, sum(CASE WHEN v.dbcr = 'C' Then amt ELSE 0 end) AS amtc, "
	    		+ "sum(CASE WHEN v.dbcr = 'D' Then amt ELSE 0 end) AS amtd, v.memo1   FROM gen_vouch_h v "
	    		+ "WHERE 1=1 and ac_no <> ? " 
				+ " AND tx_date || refno in "
				+ "(SELECT DISTINCT tx_date || refno FROM GEN_VOUCH_H "
				+ "WHERE 1=1 and ac_no = ? "
				+ ") group by v.memo1,v.tx_date ORDER BY tx_date, memo1 DESC ) c";
	    sqlSelect(sql1, new Object[]{wp.itemStr("ex_ac_no"),wp.itemStr("ex_ac_no")});
	    if (sqlRowNum <= 0) {
	        alertErr2("此條件查無資料");
	        return;
	      }
	    String dbcrAmtD ;
	    String dbcrAmtC ;
	    dbcrAmtD = sqlStr("Sum_AmtD");
	    dbcrAmtC = sqlStr("Sum_AmtC");
	    wp.colSet("dbcr_Amt_D", dbcrAmtD);
	    wp.colSet("dbcr_Amt_C", dbcrAmtC);
    }else {
    	double dbcrAmtD = 0;
        double dbcrAmtC = 0;
        for(int ii = 0; ii<sun;ii++) {
        	String amtD = wp.getValue("amtd",ii);
        	String amtC = wp.getValue("amtc",ii);
        	balance_num = balance_num.add(new BigDecimal (amtD)).subtract(new BigDecimal (amtC));
    	    wp.setValue("balance_num", balance_num.toString(), ii);
    	    dbcrAmtD += wp.getNumber("amtd",ii);
    	    dbcrAmtC += wp.getNumber("amtc",ii);
        }
        wp.colSet("dbcr_Amt_D", dbcrAmtD);
	    wp.colSet("dbcr_Amt_C", dbcrAmtC);
    }
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }
  

public void queryReadExcel() throws Exception {
    	wp.pageControl();
    	String txAcNo = wp.itemStr("ex_ac_no");
    	String txDateF = wp.itemStr("exDateS");
    	String txDateU = wp.itemStr("exDateE");	  
        String dateStrSql = " ";
        String acNoStrSql = " ";

        List<Object> paramList = new ArrayList();
        Boolean paramFlag = true;
        if (!empty(txAcNo)) {
        	dateStrSql =  dateStrSql +  " and ac_no = ?";
        	acNoStrSql =  " and ac_no <> ?";;
        	paramList.add(txAcNo);
        	paramList.add(txAcNo);
        }
        if (!empty(txDateF) && !empty(txDateU)) {
            dateStrSql =dateStrSql +  " and tx_date >= ? and tx_date <= ?" ;
            paramList.add(txDateF);
            paramList.add(txDateU);
        } 
        wp.selectSQL = "" + "v.tx_date , " + " count(1) as voucher_num, "
        		+ "sum(CASE WHEN v.dbcr = 'C' Then amt ELSE 0 end) AS amtc, "
    	        + "sum(CASE WHEN v.dbcr = 'D' Then amt ELSE 0 end) AS amtd, "
    	        + "v.memo1  ";
    		wp.daoTable = "gen_vouch_h v ";
    	wp.whereStr =  "WHERE 1=1  " + acNoStrSql
    			+ " AND tx_date || refno in "
    			+ "(SELECT DISTINCT tx_date || refno FROM GEN_VOUCH_H "
    			+ "WHERE 1=1  "
    			+ dateStrSql
    			+ ") group by v.memo1,v.tx_date ";
        wp.whereOrder = "ORDER BY tx_date, memo1 DESC";
	    if (strAction.equals("XLS")) {
		      selectNoLimit();
		}
	    pageQuery(paramList.toArray());    
	    wp.setListCount(1);
	    sun = wp.selectCnt;
	    if (sqlRowNum <= 0) {
	      alertErr2("此條件查無資料");
	      return;
	    }
	    BigDecimal  balance_num = new BigDecimal (0);
	    double dbcrAmtD = 0;
	    double dbcrAmtC = 0;
	    int voucherCnt = 0;
	    for(int ii = 0; ii<sun;ii++) {
	    	balance_num = balance_num.add(new BigDecimal (wp.getValue("amtd",ii))).subtract(new BigDecimal (wp.getValue("amtc",ii)));
		    wp.setValue("balance_num", balance_num.toString(), ii);
		    dbcrAmtD += wp.getNumber("amtd",ii);
		    dbcrAmtC += wp.getNumber("amtc",ii);
		    voucherCnt += wp.getNumber("voucher_num",ii);
	    }
	    wp.listCount[1] = wp.dataCnt;
//	    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
	    wp.colSet("dbcr_Amt_D", dbcrAmtD);
	    wp.colSet("dbcr_Amt_C", dbcrAmtC);
	    wp.colSet("voucher_cnt", voucherCnt);
	    wp.setPageValue();
	  }
  
  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = "genr0130.xlsx";
      xlsx.sheetName[0] = "明細";
      wp.pageRows = 99999;
      queryExcel();
      wp.setListCount(1);
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");

    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
		StringBuffer bf = new StringBuffer();
		bf.append("where 1=1 ");

		if (!wp.itemEmpty("ex_acno")) {
			bf.append(" and ac_no like '").append(wp.itemStr("ex_acno")).append("%'");
		}
		bf.append(" group by ac_no,ac_full_name order by ac_no ");

        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_ac_no");
        this.dddwList("dddw_ac_no", "gen_acct_m", "ac_no", "AC_FULL_NAME",bf.toString());
    } catch (Exception ex) {
    }
  }

}

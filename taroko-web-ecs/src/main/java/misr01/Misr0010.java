/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-08-11  V1.00.01  ryan                                                  * 
* 111-09-05  V1.00.02  ryan        增加排序                                                                                                     * 
* 112-11-02  V1.00.03  Wilson      增加AI501處理                                                                                       *
******************************************************************************/
package misr01;

import org.json.JSONObject;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import taroko.base.CommDate;

public class Misr0010 extends BaseAction implements InfacePdf {
  private String progName = "Misr0010";
	
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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      //pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {  
    try {
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
	  
	  if (empty(wp.itemStr("ex_data_month"))) {
	      alertErr2("請輸入報表年月");
	      return;
	    }

    String lsWhere = " where 1=1 " 
        + sqlCol(wp.itemStr("ex_data_month"), "A.DATA_MONTH")
        ;
    if(wp.itemEq("rpt_name","AI505")) {
    	lsWhere += " and data_from = 'CRD50A' ";
    } else if (wp.itemEq("rpt_name","AI501")) {
    	lsWhere += " and data_from = 'CRM65' ";
    }
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " A.DATA_MONTH, DECODE(A.DATA_FROM,'CRD50A','AI505-信用卡交易金額分析表','AI501-信用卡發卡量分析表') AS report_name, A.DATA_DATE, A.DATA_CONTENT ";
    wp.daoTable = "MIS_REPORT_DATA A ";
    wp.whereOrder = "";
    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
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
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {

  }
  
  void xlsPrint() {
	    try {
	      log("xlsFunction: started--------");
	      wp.reportId = progName;

	      // ===================================
	      TarokoExcel xlsx = new TarokoExcel();
	      wp.fileMode = "N";
	      
	      if(wp.itemEq("rpt_name","AI505")) {
	    	  xlsx.excelTemplate = "misr0010_AI505.xlsx";
	    	  processAI505RptData();
	    	  xlsx.sheetName[0] = "AI505";
	      } else {
	    	  xlsx.excelTemplate = "misr0010_AI501.xlsx";
	    	  processAI501RptData();
	    	  xlsx.sheetName[0] = "AI501";
	      }
	      

	      wp.setListCount(1);

	      xlsx.processExcelSheet(wp);
	      xlsx.outputExcel();
	      xlsx = null;
	      log("xlsFunction: ended-------------");

	    } catch (Exception ex) {
	      wp.expMethod = "xlsPrint";
	      wp.expHandle(ex);
	    }
	  }
  
  private void processAI505RptData() {
		String sql = ""; 
		JSONObject obj = null;
		JSONObject objcrd50v = null;
		JSONObject objcrm68 = null;
		
		//取CRD50A當月資料 
		sql += "select data_content ";
		sql += "from mis_report_data ";
		sql += "where data_month = ? and data_from='CRD50A' ";
		setString(1, wp.itemStr("ex_data_month"));
		sqlSelect(sql);
		if (sqlRowNum > 0) {
			obj = initCRD50A("Y",new JSONObject(sqlStr("data_content")));
		} else {
			obj = initCRD50A("N",new JSONObject());
		}
		
		//取CRD50V當月資料
		sql = ""; 
		sql += "select data_content ";
		sql += "from mis_report_data ";
		sql += "where data_month = ? and data_from='CRD50V' ";
		setString(1, wp.itemStr("ex_data_month"));
		sqlSelect(sql);
		if (sqlRowNum > 0) {
			objcrd50v = initCRD50V("Y",new JSONObject(sqlStr("data_content")));
		} else {
			objcrd50v = initCRD50V("N",new JSONObject());
		}
		
		//取CRM68當月資料
		sql = ""; 
		sql += "select data_content ";
		sql += "from mis_report_data ";
		sql += "where data_month = ? and data_from='CRM68' ";
		setString(1, wp.itemStr("ex_data_month"));
		sqlSelect(sql);
		if (sqlRowNum > 0) {
			objcrm68 = initCRM68("Y",new JSONObject(sqlStr("data_content")));
		} else {
			objcrm68 = initCRM68("N",new JSONObject());
		}
		
		//取CRD50A及CRD50V年度累積資料
		sql = ""; 
		sql += "select sum(SUM_FIELD1) as YEAR_RTL_AMT, sum(SUM_FIELD2) as YEAR_CSH_AMT ";
		sql += "from mis_report_data ";
		sql += "where data_month like ? and data_month <= ? and data_from in ('CRD50A','CRD50V') ";
		setString(1, (wp.itemStr("ex_data_month").substring(0,4)+"%"));
		setString(2, wp.itemStr("ex_data_month"));
		sqlSelect(sql);
		
		wp.colSet("report_month", Integer.parseInt(wp.itemStr("ex_data_month"))-191100);
		
		wp.colSet("DUMMY_AMT", 0.0);
		wp.colSet("DUMMY_CNT", 0);
	    wp.colSet("NAT_V_RTL_AMT", obj.getDouble("NAT_V_ONUS_RTL_AMT")+obj.getDouble("NAT_V_OFUS_RTL_AMT")+objcrd50v.getDouble("NAT_VD_RTL_AMT"));
	    wp.colSet("NAT_M_RTL_AMT", obj.getDouble("NAT_M_ONUS_RTL_AMT")+obj.getDouble("NAT_M_OFUS_RTL_AMT"));
	    wp.colSet("NAT_J_RTL_AMT", obj.getDouble("NAT_J_ONUS_RTL_AMT")+obj.getDouble("NAT_J_OFUS_RTL_AMT"));
	    wp.colSet("NAT_INTCARD_RTL_AMT",wp.colNum("NAT_V_RTL_AMT")+wp.colNum("NAT_M_RTL_AMT")+wp.colNum("NAT_J_RTL_AMT"));
	    wp.colSet("NAT_RTL_AMT",wp.colNum("NAT_V_RTL_AMT")+wp.colNum("NAT_M_RTL_AMT")+wp.colNum("NAT_J_RTL_AMT"));
	    
	    wp.colSet("INT_V_RTL_AMT", obj.getDouble("INT_V_RTL_AMT")+objcrd50v.getDouble("INT_VD_RTL_AMT"));
	    wp.colSet("INT_M_RTL_AMT", obj.getDouble("INT_M_RTL_AMT"));
	    wp.colSet("INT_J_RTL_AMT", obj.getDouble("INT_J_RTL_AMT"));
	    wp.colSet("INT_RTL_AMT",wp.colNum("INT_V_RTL_AMT")+wp.colNum("INT_M_RTL_AMT")+wp.colNum("INT_J_RTL_AMT"));
	    
	    wp.colSet("MONTH_RTL_AMT", wp.colNum("NAT_RTL_AMT")+wp.colNum("INT_RTL_AMT"));
	    
	    wp.colSet("NAT_V_RTL_CNT", obj.getDouble("NAT_V_ONUS_RTL_CNT")+obj.getDouble("NAT_V_OFUS_RTL_CNT")+objcrd50v.getDouble("NAT_VD_RTL_CNT"));
	    wp.colSet("NAT_M_RTL_CNT", obj.getDouble("NAT_M_ONUS_RTL_CNT")+obj.getDouble("NAT_M_OFUS_RTL_CNT"));
	    wp.colSet("NAT_J_RTL_CNT", obj.getDouble("NAT_J_ONUS_RTL_CNT")+obj.getDouble("NAT_J_OFUS_RTL_CNT"));
	    wp.colSet("NAT_INTCARD_RTL_CNT",wp.colNum("NAT_V_RTL_CNT")+wp.colNum("NAT_M_RTL_CNT")+wp.colNum("NAT_J_RTL_CNT"));
	    wp.colSet("NAT_RTL_CNT",wp.colNum("NAT_V_RTL_CNT")+wp.colNum("NAT_M_RTL_CNT")+wp.colNum("NAT_J_RTL_CNT"));
	    
	    wp.colSet("INT_V_RTL_CNT", obj.getDouble("INT_V_RTL_CNT")+objcrd50v.getDouble("INT_VD_RTL_CNT"));
	    wp.colSet("INT_M_RTL_CNT", obj.getDouble("INT_M_RTL_CNT"));
	    wp.colSet("INT_J_RTL_CNT", obj.getDouble("INT_J_RTL_CNT"));
	    wp.colSet("INT_RTL_CNT",wp.colNum("INT_V_RTL_CNT")+wp.colNum("INT_M_RTL_CNT")+wp.colNum("INT_J_RTL_CNT"));
	    
	    wp.colSet("MONTH_RTL_CNT", wp.colNum("NAT_RTL_CNT")+wp.colNum("INT_RTL_CNT"));
	    
	    wp.colSet("NAT_V_CSH_AMT", obj.getDouble("NAT_V_ONUS_CSH_AMT")+obj.getDouble("NAT_V_OFUS_CSH_AMT"));
	    wp.colSet("NAT_M_CSH_AMT", obj.getDouble("NAT_M_ONUS_CSH_AMT")+obj.getDouble("NAT_M_OFUS_CSH_AMT"));
	    wp.colSet("NAT_J_CSH_AMT", obj.getDouble("NAT_J_ONUS_CSH_AMT")+obj.getDouble("NAT_J_OFUS_CSH_AMT"));
	    wp.colSet("NAT_INTCARD_CSH_AMT",wp.colNum("NAT_V_CSH_AMT")+wp.colNum("NAT_M_CSH_AMT")+wp.colNum("NAT_J_CSH_AMT"));
	    wp.colSet("NAT_CSH_AMT",wp.colNum("NAT_V_CSH_AMT")+wp.colNum("NAT_M_CSH_AMT")+wp.colNum("NAT_J_CSH_AMT"));
	    
	    wp.colSet("INT_V_CSH_AMT", obj.getDouble("INT_V_CSH_AMT"));
	    wp.colSet("INT_M_CSH_AMT", obj.getDouble("INT_M_CSH_AMT"));
	    wp.colSet("INT_J_CSH_AMT", obj.getDouble("INT_J_CSH_AMT"));
	    wp.colSet("INT_CSH_AMT",wp.colNum("INT_V_CSH_AMT")+wp.colNum("INT_M_CSH_AMT")+wp.colNum("INT_J_CSH_AMT"));
	    
	    wp.colSet("MONTH_CSH_AMT", wp.colNum("NAT_CSH_AMT")+wp.colNum("INT_CSH_AMT"));
	    
	    wp.colSet("NAT_V_CSH_CNT", obj.getDouble("NAT_V_ONUS_CSH_CNT")+obj.getDouble("NAT_V_OFUS_CSH_CNT"));
	    wp.colSet("NAT_M_CSH_CNT", obj.getDouble("NAT_M_ONUS_CSH_CNT")+obj.getDouble("NAT_M_OFUS_CSH_CNT"));
	    wp.colSet("NAT_J_CSH_CNT", obj.getDouble("NAT_J_ONUS_CSH_CNT")+obj.getDouble("NAT_J_OFUS_CSH_CNT"));
	    wp.colSet("NAT_INTCARD_CSH_CNT",wp.colNum("NAT_V_CSH_CNT")+wp.colNum("NAT_M_CSH_CNT")+wp.colNum("NAT_J_CSH_CNT"));
	    wp.colSet("NAT_CSH_CNT",wp.colNum("NAT_V_CSH_CNT")+wp.colNum("NAT_M_CSH_CNT")+wp.colNum("NAT_J_CSH_CNT"));
	    
	    wp.colSet("INT_V_CSH_CNT", obj.getDouble("INT_V_CSH_CNT"));
	    wp.colSet("INT_M_CSH_CNT", obj.getDouble("INT_M_CSH_CNT"));
	    wp.colSet("INT_J_CSH_CNT", obj.getDouble("INT_J_CSH_CNT"));
	    wp.colSet("INT_CSH_CNT",wp.colNum("INT_V_CSH_CNT")+wp.colNum("INT_M_CSH_CNT")+wp.colNum("INT_J_CSH_CNT"));
	    
	    wp.colSet("MONTH_CSH_CNT", wp.colNum("NAT_CSH_CNT")+wp.colNum("INT_CSH_CNT"));
	    
	    wp.colSet("YEAR_RTL_AMT", sqlStr("YEAR_RTL_AMT"));  //年度消費金額
	    wp.colSet("YEAR_CSH_AMT", sqlStr("YEAR_CSH_AMT"));  //年度預借金額
	    
	    wp.colSet("REVOLVE_BAL", objcrm68.getDouble("REVOLVE_BAL")); //CRM68動用循環金額

	    /*處理前月資料*/
	    
		String preDataMonth = "";
		JSONObject objPre = null;
		JSONObject objcrd50vPre = null;
		JSONObject objcrm68Pre = null;
		CommDate comd = new CommDate();

		if (wp.itemStr("ex_data_month").length() == 6 && "01".equals(wp.itemStr("ex_data_month").substring(4, 6))) {
			objPre = initCRD50A("N", new JSONObject());
			objcrd50vPre = initCRD50V("N", new JSONObject());
			objcrm68Pre = initCRM68("N", new JSONObject());
			preDataMonth = wp.itemStr("ex_data_month").substring(0, 4) + "00";
			wp.colSet("pre_report_month", "新年度");
		} else {
			preDataMonth = comd.monthAdd(wp.itemStr("ex_data_month"), -1);
			wp.colSet("pre_report_month", Integer.parseInt(preDataMonth) - 191100);

			// 取CRD50A前月資料
			sql = ""; 
			sql += "select data_content ";
			sql += "from mis_report_data ";
			sql += "where data_month = ? and data_from='CRD50A' ";
			setString(1, preDataMonth);
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objPre = initCRD50A("Y", new JSONObject(sqlStr("data_content")));
			} else {
				objPre = initCRD50A("N", new JSONObject());
			}

			// 取CRD50V前月資料
			sql = "";
			sql += "select data_content ";
			sql += "from mis_report_data ";
			sql += "where data_month = ? and data_from='CRD50V' ";
			setString(1, preDataMonth);
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrd50vPre = initCRD50V("Y", new JSONObject(sqlStr("data_content")));
			} else {
				objcrd50vPre = initCRD50V("N", new JSONObject());
			}

			// 取CRM68前月資料
			sql = "";
			sql += "select data_content ";
			sql += "from mis_report_data ";
			sql += "where data_month = ? and data_from='CRM68' ";
			setString(1, preDataMonth);
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrm68Pre = initCRM68("Y", new JSONObject(sqlStr("data_content")));
			} else {
				objcrm68Pre = initCRM68("N", new JSONObject());
			}
		}

		// 取CRD50A及CRD50V年度累積資料
		sql = "";
		sql += "select sum(SUM_FIELD1) as YEAR_RTL_AMT, sum(SUM_FIELD2) as YEAR_CSH_AMT ";
		sql += "from mis_report_data ";
		sql += "where data_month like ? and data_month <= ? and data_from in ('CRD50A','CRD50V') ";
		setString(1, (preDataMonth.substring(0, 4) + "%"));
		setString(2, preDataMonth);
		sqlSelect(sql);

		wp.colSet("DUMMY_AMT", 0.0);
		wp.colSet("DUMMY_CNT", 0);
		wp.colSet("PRE_NAT_V_RTL_AMT", objPre.getDouble("NAT_V_ONUS_RTL_AMT") + objPre.getDouble("NAT_V_OFUS_RTL_AMT")
				+ objcrd50vPre.getDouble("NAT_VD_RTL_AMT"));
		wp.colSet("PRE_NAT_M_RTL_AMT", objPre.getDouble("NAT_M_ONUS_RTL_AMT") + objPre.getDouble("NAT_M_OFUS_RTL_AMT"));
		wp.colSet("PRE_NAT_J_RTL_AMT", objPre.getDouble("NAT_J_ONUS_RTL_AMT") + objPre.getDouble("NAT_J_OFUS_RTL_AMT"));
		wp.colSet("PRE_NAT_INTCARD_RTL_AMT",
				wp.colNum("PRE_NAT_V_RTL_AMT") + wp.colNum("PRE_NAT_M_RTL_AMT") + wp.colNum("PRE_NAT_J_RTL_AMT"));
		wp.colSet("PRE_NAT_RTL_AMT", wp.colNum("PRE_NAT_V_RTL_AMT") + wp.colNum("PRE_NAT_M_RTL_AMT") + wp.colNum("PRE_NAT_J_RTL_AMT"));

		wp.colSet("PRE_INT_V_RTL_AMT", objPre.getDouble("INT_V_RTL_AMT") + objcrd50vPre.getDouble("INT_VD_RTL_AMT"));
		wp.colSet("PRE_INT_M_RTL_AMT", objPre.getDouble("INT_M_RTL_AMT"));
		wp.colSet("PRE_INT_J_RTL_AMT", objPre.getDouble("INT_J_RTL_AMT"));
		wp.colSet("PRE_INT_RTL_AMT", wp.colNum("PRE_INT_V_RTL_AMT") + wp.colNum("PRE_INT_M_RTL_AMT") + wp.colNum("PRE_INT_J_RTL_AMT"));

		wp.colSet("PRE_MONTH_RTL_AMT", wp.colNum("PRE_NAT_RTL_AMT") + wp.colNum("PRE_INT_RTL_AMT"));

		wp.colSet("PRE_NAT_V_RTL_CNT", objPre.getDouble("NAT_V_ONUS_RTL_CNT") + objPre.getDouble("NAT_V_OFUS_RTL_CNT")
				+ objcrd50vPre.getDouble("NAT_VD_RTL_CNT"));
		wp.colSet("PRE_NAT_M_RTL_CNT", objPre.getDouble("NAT_M_ONUS_RTL_CNT") + objPre.getDouble("NAT_M_OFUS_RTL_CNT"));
		wp.colSet("PRE_NAT_J_RTL_CNT", objPre.getDouble("NAT_J_ONUS_RTL_CNT") + objPre.getDouble("NAT_J_OFUS_RTL_CNT"));
		wp.colSet("PRE_NAT_INTCARD_RTL_CNT",
				wp.colNum("PRE_NAT_V_RTL_CNT") + wp.colNum("PRE_NAT_M_RTL_CNT") + wp.colNum("PRE_NAT_J_RTL_CNT"));
		wp.colSet("PRE_NAT_RTL_CNT", wp.colNum("PRE_NAT_V_RTL_CNT") + wp.colNum("PRE_NAT_M_RTL_CNT") + wp.colNum("PRE_NAT_J_RTL_CNT"));

		wp.colSet("PRE_INT_V_RTL_CNT", objPre.getDouble("INT_V_RTL_CNT") + objcrd50vPre.getDouble("INT_VD_RTL_CNT"));
		wp.colSet("PRE_INT_M_RTL_CNT", objPre.getDouble("INT_M_RTL_CNT"));
		wp.colSet("PRE_INT_J_RTL_CNT", objPre.getDouble("INT_J_RTL_CNT"));
		wp.colSet("PRE_INT_RTL_CNT", wp.colNum("PRE_INT_V_RTL_CNT") + wp.colNum("PRE_INT_M_RTL_CNT") + wp.colNum("PRE_INT_J_RTL_CNT"));

		wp.colSet("PRE_MONTH_RTL_CNT", wp.colNum("PRE_NAT_RTL_CNT") + wp.colNum("PRE_INT_RTL_CNT"));

		wp.colSet("PRE_NAT_V_CSH_AMT", objPre.getDouble("NAT_V_ONUS_CSH_AMT") + objPre.getDouble("NAT_V_OFUS_CSH_AMT"));
		wp.colSet("PRE_NAT_M_CSH_AMT", objPre.getDouble("NAT_M_ONUS_CSH_AMT") + objPre.getDouble("NAT_M_OFUS_CSH_AMT"));
		wp.colSet("PRE_NAT_J_CSH_AMT", objPre.getDouble("NAT_J_ONUS_CSH_AMT") + objPre.getDouble("NAT_J_OFUS_CSH_AMT"));
		wp.colSet("PRE_NAT_INTCARD_CSH_AMT",
				wp.colNum("PRE_NAT_V_CSH_AMT") + wp.colNum("PRE_NAT_M_CSH_AMT") + wp.colNum("PRE_NAT_J_CSH_AMT"));
		wp.colSet("PRE_NAT_CSH_AMT", wp.colNum("PRE_NAT_V_CSH_AMT") + wp.colNum("PRE_NAT_M_CSH_AMT") + wp.colNum("PRE_NAT_J_CSH_AMT"));

		wp.colSet("PRE_INT_V_CSH_AMT", objPre.getDouble("INT_V_CSH_AMT"));
		wp.colSet("PRE_INT_M_CSH_AMT", objPre.getDouble("INT_M_CSH_AMT"));
		wp.colSet("PRE_INT_J_CSH_AMT", objPre.getDouble("INT_J_CSH_AMT"));
		wp.colSet("PRE_INT_CSH_AMT", wp.colNum("PRE_INT_V_CSH_AMT") + wp.colNum("PRE_INT_M_CSH_AMT") + wp.colNum("PRE_INT_J_CSH_AMT"));

		wp.colSet("PRE_MONTH_CSH_AMT", wp.colNum("PRE_NAT_CSH_AMT") + wp.colNum("PRE_INT_CSH_AMT"));

		wp.colSet("PRE_NAT_V_CSH_CNT", objPre.getDouble("NAT_V_ONUS_CSH_CNT") + objPre.getDouble("NAT_V_OFUS_CSH_CNT"));
		wp.colSet("PRE_NAT_M_CSH_CNT", objPre.getDouble("NAT_M_ONUS_CSH_CNT") + objPre.getDouble("NAT_M_OFUS_CSH_CNT"));
		wp.colSet("PRE_NAT_J_CSH_CNT", objPre.getDouble("NAT_J_ONUS_CSH_CNT") + objPre.getDouble("NAT_J_OFUS_CSH_CNT"));
		wp.colSet("PRE_NAT_INTCARD_CSH_CNT",
				wp.colNum("PRE_NAT_V_CSH_CNT") + wp.colNum("PRE_NAT_M_CSH_CNT") + wp.colNum("PRE_NAT_J_CSH_CNT"));
		wp.colSet("PRE_NAT_CSH_CNT", wp.colNum("PRE_NAT_V_CSH_CNT") + wp.colNum("PRE_NAT_M_CSH_CNT") + wp.colNum("PRE_NAT_J_CSH_CNT"));

		wp.colSet("PRE_INT_V_CSH_CNT", objPre.getDouble("INT_V_CSH_CNT"));
		wp.colSet("PRE_INT_M_CSH_CNT", objPre.getDouble("INT_M_CSH_CNT"));
		wp.colSet("PRE_INT_J_CSH_CNT", objPre.getDouble("INT_J_CSH_CNT"));
		wp.colSet("PRE_INT_CSH_CNT", wp.colNum("PRE_INT_V_CSH_CNT") + wp.colNum("PRE_INT_M_CSH_CNT") + wp.colNum("PRE_INT_J_CSH_CNT"));

		wp.colSet("PRE_MONTH_CSH_CNT", wp.colNum("PRE_NAT_CSH_CNT") + wp.colNum("PRE_INT_CSH_CNT"));

		wp.colSet("PRE_YEAR_RTL_AMT", sqlStr("YEAR_RTL_AMT")); // 年度消費金額
		wp.colSet("PRE_YEAR_CSH_AMT", sqlStr("YEAR_CSH_AMT")); // 年度預借金額

		wp.colSet("PRE_REVOLVE_BAL", objcrm68Pre.getDouble("REVOLVE_BAL")); // CRM68動用循環金額

	}

	private JSONObject initCRD50A(String haveData, JSONObject objcrd50a) {

		JSONObject rptData = null;

		if ("Y".equals(haveData)) {
			rptData = objcrd50a;
		} else {
			rptData = new JSONObject();
			rptData.put("NAT_V_ONUS_RTL_AMT", 0);
			rptData.put("NAT_M_ONUS_RTL_AMT", 0);
			rptData.put("NAT_J_ONUS_RTL_AMT", 0);
			rptData.put("NAT_V_ONUS_CSH_AMT", 0);
			rptData.put("NAT_M_ONUS_CSH_AMT", 0);
			rptData.put("NAT_J_ONUS_CSH_AMT", 0);
			rptData.put("NAT_V_ONUS_RTL_CNT", 0);
			rptData.put("NAT_M_ONUS_RTL_CNT", 0);
			rptData.put("NAT_J_ONUS_RTL_CNT", 0);
			rptData.put("NAT_V_ONUS_CSH_CNT", 0);
			rptData.put("NAT_M_ONUS_CSH_CNT", 0);
			rptData.put("NAT_J_ONUS_CSH_CNT", 0);
			rptData.put("NAT_V_OFUS_RTL_AMT", 0);
			rptData.put("NAT_M_OFUS_RTL_AMT", 0);
			rptData.put("NAT_J_OFUS_RTL_AMT", 0);
			rptData.put("NAT_V_OFUS_CSH_AMT", 0);
			rptData.put("NAT_M_OFUS_CSH_AMT", 0);
			rptData.put("NAT_J_OFUS_CSH_AMT", 0);
			rptData.put("NAT_V_OFUS_RTL_CNT", 0);
			rptData.put("NAT_M_OFUS_RTL_CNT", 0);
			rptData.put("NAT_J_OFUS_RTL_CNT", 0);
			rptData.put("NAT_V_OFUS_CSH_CNT", 0);
			rptData.put("NAT_M_OFUS_CSH_CNT", 0);
			rptData.put("NAT_J_OFUS_CSH_CNT", 0);
			rptData.put("INT_V_RTL_AMT", 0);
			rptData.put("INT_M_RTL_AMT", 0);
			rptData.put("INT_J_RTL_AMT", 0);
			rptData.put("INT_V_CSH_AMT", 0);
			rptData.put("INT_M_CSH_AMT", 0);
			rptData.put("INT_J_CSH_AMT", 0);
			rptData.put("INT_V_RTL_CNT", 0);
			rptData.put("INT_M_RTL_CNT", 0);
			rptData.put("INT_J_RTL_CNT", 0);
			rptData.put("INT_V_CSH_CNT", 0);
			rptData.put("INT_M_CSH_CNT", 0);
			rptData.put("INT_J_CSH_CNT", 0);
		}

		return rptData;

	}

	private JSONObject initCRD50V(String haveData, JSONObject objcrd50v) {

		JSONObject rptData = null;

		if ("Y".equals(haveData)) {
			rptData = objcrd50v;
		} else {
			rptData = new JSONObject();
			rptData.put("NAT_VD_RTL_AMT", 0);
			rptData.put("NAT_VD_RTL_CNT", 0);
			rptData.put("INT_VD_RTL_AMT", 0);
			rptData.put("INT_VD_RTL_CNT", 0);

		}

		return rptData;

	}
	
	private JSONObject initCRM68(String haveData, JSONObject objcrm68) {

		JSONObject rptData = null;

		if ("Y".equals(haveData)) {
			rptData = objcrm68;
		} else {
			rptData = new JSONObject();
			rptData.put("REVOLVE_BAL", 0);
		}

		return rptData;

	}
	
	  private void processAI501RptData() {
		    CommDate comd = new CommDate();
			
		    String sql = ""; 
			String LastDataMonth = "";
			
			JSONObject objcrd52 = null;
			JSONObject objcrm63 = null;
			JSONObject objcrm64 = null;
			JSONObject objcrm65 = null;
			JSONObject objcrm67 = null;
			JSONObject objcrm68 = null;
			JSONObject objcrm69 = null;
			JSONObject objcrm69c = null;
			JSONObject objcrm69d = null;
			
			LastDataMonth = comd.monthAdd(wp.itemStr("ex_data_month"), -1);
			
			//取CRD52當月資料 
			sql += "select sum_field1 as CARD_ISSUE_TOTAL, ";
			sql += "       data_content ";
			sql += "  from mis_report_data ";
			sql += " where data_month = ? and data_from='CRD52' ";
			setString(1, LastDataMonth);
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrd52 = initCRD52("Y",new JSONObject(sqlStr("data_content")));
			} else {
				objcrd52 = initCRD52("N",new JSONObject());
			}
					
			//取CRM63當月資料
			sql = "";
			sql += "select sum_field1 as GEN_CARD_CURRENT_TOTAL, ";
			sql += "       data_content ";
			sql += "  from mis_report_data ";
			sql += " where data_month = ? and data_from='CRM63' ";
			setString(1, LastDataMonth);
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrm63 = initCRM63("Y",new JSONObject(sqlStr("data_content")));
			} else {
				objcrm63 = initCRM63("N",new JSONObject());
			}
					
			//取CRM64當月資料
			sql = "";
			sql += "select sum_field1 as STU_CARD_CURRENT_TOTAL, ";
			sql += "       data_content ";
			sql += "  from mis_report_data ";
			sql += " where data_month = ? and data_from='CRM64' ";
			setString(1, LastDataMonth);
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrm64 = initCRM64("Y",new JSONObject(sqlStr("data_content")));
			} else {
				objcrm64 = initCRM64("N",new JSONObject());
			}
			
			//取CRM65當月資料
			sql = "";
			sql += "select sum_field1 as V_CARD_ISSUE_TOTAL, ";
			sql += "       sum_field2 as M_CARD_ISSUE_TOTAL, ";
			sql += "       sum_field3 as J_CARD_ISSUE_TOTAL, ";
			sql += "       sum_field4 as STU_CARD_ACCT_TOTAL, ";
			sql += "       data_content ";
			sql += "  from mis_report_data ";
			sql += " where data_month = ? and data_from='CRM65' ";
			setString(1, wp.itemStr("ex_data_month"));
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrm65 = initCRM65("Y",new JSONObject(sqlStr("data_content")));
			} else {
				objcrm65 = initCRM65("N",new JSONObject());
			}
					
			//取CRM67當月資料
			sql = "";
			sql += "select sum_field1 as CURRENT_CODE5_TOTAL, ";
			sql += "       data_content ";
			sql += "  from mis_report_data ";
			sql += " where data_month = ? and data_from='CRM67' ";
			setString(1, LastDataMonth);
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrm67 = initCRM67("Y",new JSONObject(sqlStr("data_content")));
			} else {
				objcrm67 = initCRM67("N",new JSONObject());
			}
					
			//取CRM68當月資料
			sql = "";
			sql += "select data_content ";
			sql += "from mis_report_data ";
			sql += "where data_month = ? and data_from='CRM68' ";
			setString(1, wp.itemStr("ex_data_month"));
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrm68 = initCRM68A("Y",new JSONObject(sqlStr("data_content")));
			} else {
				objcrm68 = initCRM68A("N",new JSONObject());
			}
			
			//取CRM69當月資料
			sql = "";
			sql += "select data_content ";
			sql += "from mis_report_data ";
			sql += "where data_month = ? and data_from='CRM69' ";
			setString(1, wp.itemStr("ex_data_month"));
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrm69 = initCRM69("Y",new JSONObject(sqlStr("data_content")));
			} else {
				objcrm69 = initCRM69("N",new JSONObject());
			}
			
			//取CRM69C當月資料
			sql = "";
			sql += "select data_content ";
			sql += "from mis_report_data ";
			sql += "where data_month = ? and data_from='CRM69C' ";
			setString(1, wp.itemStr("ex_data_month"));
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrm69c = initCRM69C("Y",new JSONObject(sqlStr("data_content")));
			} else {
				objcrm69c = initCRM69C("N",new JSONObject());
			}
			
			//取CRM69D當月資料
			sql = "";
			sql += "select data_content ";
			sql += "from mis_report_data ";
			sql += "where data_month = ? and data_from='CRM69D' ";
			setString(1, wp.itemStr("ex_data_month"));
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				objcrm69d = initCRM69D("Y",new JSONObject(sqlStr("data_content")));
			} else {
				objcrm69d = initCRM69D("N",new JSONObject());
			}
			
			int tmpDefaultCnt = 0;
			
			wp.colSet("report_month", Integer.parseInt(wp.itemStr("ex_data_month"))-191100);
			wp.colSet("DEFAULT_CNT", tmpDefaultCnt);
			
			wp.colSet("CARD_ISSUE_TOTAL", sqlStr("CARD_ISSUE_TOTAL"));  //發卡總數(累計數)
			wp.colSet("GEN_CARD_CURRENT_TOTAL", sqlStr("GEN_CARD_CURRENT_TOTAL"));  //一般持卡人流通卡數(總計)
			wp.colSet("STU_CARD_CURRENT_TOTAL", sqlStr("STU_CARD_CURRENT_TOTAL"));  //學生持卡人流通卡數(總計)
			wp.colSet("V_CARD_ISSUE_TOTAL", sqlStr("V_CARD_ISSUE_TOTAL"));  //VISA卡發卡總數(總計)
			wp.colSet("M_CARD_ISSUE_TOTAL", sqlStr("M_CARD_ISSUE_TOTAL"));  //MASTER卡發卡總數(總計)
			wp.colSet("J_CARD_ISSUE_TOTAL", sqlStr("J_CARD_ISSUE_TOTAL"));  //JCB卡發卡總數(總計)
			wp.colSet("STU_CARD_ACCT_TOTAL", sqlStr("STU_CARD_ACCT_TOTAL"));  //學生持卡總歸戶數(總計)
			wp.colSet("CURRENT_CODE5_TOTAL", sqlStr("CURRENT_CODE5_TOTAL"));  //本年度累計至申報月份之偽冒停卡張數(總計)
			
			//CRD52
			wp.colSet("THIS_MONTH_CARD_ISSUE_CNT", objcrd52.getInt("THIS_MONTH_CARD_ISSUE_CNT"));
			wp.colSet("THIS_MONTH_CARD_OPPOST_CNT", objcrd52.getInt("THIS_MONTH_CARD_OPPOST_CNT"));
			wp.colSet("THIS_MONTH_CARD_EFFC_CNT", objcrd52.getInt("THIS_MONTH_CARD_EFFC_CNT"));
			
			//CRM63
			wp.colSet("GEN_MAIN_CARD_20UNDER_CURRENT_CNT", objcrm63.getInt("GEN_MAIN_CARD_20UNDER_CURRENT_CNT"));
			wp.colSet("GEN_MAIN_CARD_20UP_CURRENT_CNT", objcrm63.getInt("GEN_MAIN_CARD_20UP_CURRENT_CNT"));
			wp.colSet("GEN_MAIN_CARD_25UP_CURRENT_CNT", objcrm63.getInt("GEN_MAIN_CARD_25UP_CURRENT_CNT"));
			wp.colSet("GEN_MAIN_CARD_30UP_CURRENT_CNT", objcrm63.getInt("GEN_MAIN_CARD_30UP_CURRENT_CNT"));
			wp.colSet("GEN_MAIN_CARD_40UP_CURRENT_CNT", objcrm63.getInt("GEN_MAIN_CARD_40UP_CURRENT_CNT"));
			wp.colSet("GEN_MAIN_CARD_50UP_CURRENT_CNT", objcrm63.getInt("GEN_MAIN_CARD_50UP_CURRENT_CNT"));
			wp.colSet("GEN_MAIN_CARD_60UP_CURRENT_CNT", objcrm63.getInt("GEN_MAIN_CARD_60UP_CURRENT_CNT"));
			wp.colSet("GEN_MAIN_CARD_CURRENT_SUM", objcrm63.getInt("GEN_MAIN_CARD_CURRENT_SUM"));		
			wp.colSet("GEN_ADDI_CARD_20UNDER_CURRENT_CNT", objcrm63.getInt("GEN_ADDI_CARD_20UNDER_CURRENT_CNT"));
			wp.colSet("GEN_ADDI_CARD_20UP_CURRENT_CNT", objcrm63.getInt("GEN_ADDI_CARD_20UP_CURRENT_CNT"));
			wp.colSet("GEN_ADDI_CARD_25UP_CURRENT_CNT", objcrm63.getInt("GEN_ADDI_CARD_25UP_CURRENT_CNT"));
			wp.colSet("GEN_ADDI_CARD_30UP_CURRENT_CNT", objcrm63.getInt("GEN_ADDI_CARD_30UP_CURRENT_CNT"));
			wp.colSet("GEN_ADDI_CARD_40UP_CURRENT_CNT", objcrm63.getInt("GEN_ADDI_CARD_40UP_CURRENT_CNT"));
			wp.colSet("GEN_ADDI_CARD_50UP_CURRENT_CNT", objcrm63.getInt("GEN_ADDI_CARD_50UP_CURRENT_CNT"));
			wp.colSet("GEN_ADDI_CARD_60UP_CURRENT_CNT", objcrm63.getInt("GEN_ADDI_CARD_60UP_CURRENT_CNT"));
			wp.colSet("GEN_ADDI_CARD_CURRENT_SUM", objcrm63.getInt("GEN_ADDI_CARD_CURRENT_SUM"));
						
			//CRM64
			wp.colSet("STU_MAIN_CARD_20UNDER_CURRENT_CNT", objcrm64.getInt("STU_MAIN_CARD_20UNDER_CURRENT_CNT"));		
			wp.colSet("STU_MAIN_CARD_20UP_CURRENT_CNT", objcrm64.getInt("STU_MAIN_CARD_20UP_CURRENT_CNT"));
			wp.colSet("STU_MAIN_CARD_25UP_CURRENT_CNT", objcrm64.getInt("STU_MAIN_CARD_25UP_CURRENT_CNT"));
			wp.colSet("STU_MAIN_CARD_30UP_CURRENT_CNT", objcrm64.getInt("STU_MAIN_CARD_30UP_CURRENT_CNT"));
			wp.colSet("STU_MAIN_CARD_40UP_CURRENT_CNT", objcrm64.getInt("STU_MAIN_CARD_40UP_CURRENT_CNT"));
			wp.colSet("STU_MAIN_CARD_50UP_CURRENT_CNT", objcrm64.getInt("STU_MAIN_CARD_50UP_CURRENT_CNT"));
			wp.colSet("STU_MAIN_CARD_60UP_CURRENT_CNT", objcrm64.getInt("STU_MAIN_CARD_60UP_CURRENT_CNT"));
			wp.colSet("STU_MAIN_CARD_CURRENT_SUM", objcrm64.getInt("STU_MAIN_CARD_CURRENT_SUM"));		
			wp.colSet("STU_ADDI_CARD_20UNDER_CURRENT_CNT", objcrm64.getInt("STU_ADDI_CARD_20UNDER_CURRENT_CNT"));
			wp.colSet("STU_ADDI_CARD_20UP_CURRENT_CNT", objcrm64.getInt("STU_ADDI_CARD_20UP_CURRENT_CNT"));
			wp.colSet("STU_ADDI_CARD_25UP_CURRENT_CNT", objcrm64.getInt("STU_ADDI_CARD_25UP_CURRENT_CNT"));
			wp.colSet("STU_ADDI_CARD_30UP_CURRENT_CNT", objcrm64.getInt("STU_ADDI_CARD_30UP_CURRENT_CNT"));
			wp.colSet("STU_ADDI_CARD_40UP_CURRENT_CNT", objcrm64.getInt("STU_ADDI_CARD_40UP_CURRENT_CNT"));
			wp.colSet("STU_ADDI_CARD_50UP_CURRENT_CNT", objcrm64.getInt("STU_ADDI_CARD_50UP_CURRENT_CNT"));
			wp.colSet("STU_ADDI_CARD_60UP_CURRENT_CNT", objcrm64.getInt("STU_ADDI_CARD_60UP_CURRENT_CNT"));
			wp.colSet("STU_ADDI_CARD_CURRENT_SUM", objcrm64.getInt("STU_ADDI_CARD_CURRENT_SUM"));
			
			//CRM65
			wp.colSet("STU_20UNDER_MAIN_ACCT_CNT", objcrm65.getInt("STU_20UNDER_MAIN_ACCT_CNT"));
			wp.colSet("STU_20UP_MAIN_ACCT_CNT", objcrm65.getInt("STU_20UP_MAIN_ACCT_CNT"));
			wp.colSet("STU_MAIN_CARD_ACCT_SUM", objcrm65.getInt("STU_MAIN_CARD_ACCT_SUM"));
			wp.colSet("STU_20UNDER_ADDI_ACCT_CNT", objcrm65.getInt("STU_20UNDER_ADDI_ACCT_CNT"));
			wp.colSet("STU_20UP_ADDI_ACCT_CNT", objcrm65.getInt("STU_20UP_ADDI_ACCT_CNT"));
			wp.colSet("STU_ADDI_CARD_ACCT_SUM", objcrm65.getInt("STU_ADDI_CARD_ACCT_SUM"));
						
			//CRM67
			wp.colSet("THIS_MONTH_CURRENT_CODE5_CNT", objcrm67.getInt("THIS_MONTH_CURRENT_CODE5_CNT"));		
			
	  }
	  
		private JSONObject initCRD52(String haveData, JSONObject objcrd52) {

			JSONObject rptData = null;

			if ("Y".equals(haveData)) {
				rptData = objcrd52;
			} else {
				rptData = new JSONObject();
				rptData.put("THIS_MONTH_CARD_ISSUE_CNT", 0);
				rptData.put("THIS_MONTH_CARD_OPPOST_CNT", 0);
				rptData.put("THIS_MONTH_CARD_EFFC_CNT", 0);
			}

			return rptData;
		}
		
		private JSONObject initCRM63(String haveData, JSONObject objcrm63) {

			JSONObject rptData = null;

			if ("Y".equals(haveData)) {
				rptData = objcrm63;
			} else {
				rptData = new JSONObject();
				rptData.put("GEN_MAIN_CARD_20UNDER_CURRENT_CNT", 0);
				rptData.put("GEN_MAIN_CARD_20UP_CURRENT_CNT", 0);
				rptData.put("GEN_MAIN_CARD_25UP_CURRENT_CNT", 0);
				rptData.put("GEN_MAIN_CARD_30UP_CURRENT_CNT", 0);
				rptData.put("GEN_MAIN_CARD_40UP_CURRENT_CNT", 0);
				rptData.put("GEN_MAIN_CARD_50UP_CURRENT_CNT", 0);
				rptData.put("GEN_MAIN_CARD_60UP_CURRENT_CNT", 0);
				rptData.put("GEN_MAIN_CARD_CURRENT_SUM", 0);
				rptData.put("GEN_ADDI_CARD_20UNDER_CURRENT_CNT", 0);
				rptData.put("GEN_ADDI_CARD_20UP_CURRENT_CNT", 0);
				rptData.put("GEN_ADDI_CARD_25UP_CURRENT_CNT", 0);
				rptData.put("GEN_ADDI_CARD_30UP_CURRENT_CNT", 0);
				rptData.put("GEN_ADDI_CARD_40UP_CURRENT_CNT", 0);
				rptData.put("GEN_ADDI_CARD_50UP_CURRENT_CNT", 0);
				rptData.put("GEN_ADDI_CARD_60UP_CURRENT_CNT", 0);
				rptData.put("GEN_ADDI_CARD_CURRENT_SUM", 0);
			}

			return rptData;
		}

		private JSONObject initCRM64(String haveData, JSONObject objcrm64) {

			JSONObject rptData = null;

			if ("Y".equals(haveData)) {
				rptData = objcrm64;
			} else {
				rptData = new JSONObject();
				rptData.put("STU_MAIN_CARD_20UNDER_CURRENT_CNT", 0);
				rptData.put("STU_MAIN_CARD_20UP_CURRENT_CNT", 0);
				rptData.put("STU_MAIN_CARD_25UP_CURRENT_CNT", 0);
				rptData.put("STU_MAIN_CARD_30UP_CURRENT_CNT", 0);
				rptData.put("STU_MAIN_CARD_40UP_CURRENT_CNT", 0);
				rptData.put("STU_MAIN_CARD_50UP_CURRENT_CNT", 0);
				rptData.put("STU_MAIN_CARD_60UP_CURRENT_CNT", 0);
				rptData.put("STU_MAIN_CARD_CURRENT_SUM", 0);
				rptData.put("STU_ADDI_CARD_20UNDER_CURRENT_CNT", 0);
				rptData.put("STU_ADDI_CARD_20UP_CURRENT_CNT", 0);
				rptData.put("STU_ADDI_CARD_25UP_CURRENT_CNT", 0);
				rptData.put("STU_ADDI_CARD_30UP_CURRENT_CNT", 0);
				rptData.put("STU_ADDI_CARD_40UP_CURRENT_CNT", 0);
				rptData.put("STU_ADDI_CARD_50UP_CURRENT_CNT", 0);
				rptData.put("STU_ADDI_CARD_60UP_CURRENT_CNT", 0);
				rptData.put("STU_ADDI_CARD_CURRENT_SUM", 0);
			}

			return rptData;
		}

		private JSONObject initCRM65(String haveData, JSONObject objcrm65) {

			JSONObject rptData = null;

			if ("Y".equals(haveData)) {
				rptData = objcrm65;
			} else {
				rptData = new JSONObject();
				rptData.put("STU_20UNDER_MAIN_ACCT_CNT", 0);
				rptData.put("STU_20UP_MAIN_ACCT_CNT", 0);
				rptData.put("STU_MAIN_CARD_ACCT_SUM", 0);
				rptData.put("STU_20UNDER_ADDI_ACCT_CNT", 0);
				rptData.put("STU_20UP_ADDI_ACCT_CNT", 0);
				rptData.put("STU_ADDI_CARD_ACCT_SUM", 0);
			}

			return rptData;
		}

		private JSONObject initCRM67(String haveData, JSONObject objcrm67) {

			JSONObject rptData = null;

			if ("Y".equals(haveData)) {
				rptData = objcrm67;
			} else {
				rptData = new JSONObject();
				rptData.put("THIS_MONTH_CURRENT_CODE5_CNT", 0);
			}

			return rptData;
		}
		
		private JSONObject initCRM68A(String haveData, JSONObject objcrm68) {

			JSONObject rptData = null;

			if ("Y".equals(haveData)) {
				rptData = objcrm68;
			} else {
				rptData = new JSONObject();
			}

			return rptData;
		}

		private JSONObject initCRM69(String haveData, JSONObject objcrm69) {

			JSONObject rptData = null;

			if ("Y".equals(haveData)) {
				rptData = objcrm69;
			} else {
				rptData = new JSONObject();
			}

			return rptData;
		}

		private JSONObject initCRM69C(String haveData, JSONObject objcrm69c) {

			JSONObject rptData = null;

			if ("Y".equals(haveData)) {
				rptData = objcrm69c;
			} else {
				rptData = new JSONObject();
			}

			return rptData;
		}

		private JSONObject initCRM69D(String haveData, JSONObject objcrm69d) {

			JSONObject rptData = null;

			if ("Y".equals(haveData)) {
				rptData = objcrm69d;
			} else {
				rptData = new JSONObject();
			}

			return rptData;
		}

}

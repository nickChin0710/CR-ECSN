/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE            Version     AUTHOR        DESCRIPTION                      *
* ---------       --------      ----------         --------------------------*
* 112-04-24  V1.00.00   Ryan        program initial                     *
* 112-06-05  V1.00.01   Ryan        增加內裝物品                                                                                              *
* 112-06-20  V1.00.02   Ryan        內裝物品修正                                                               *
******************************************************************************/
package crdr01;

import java.util.HashMap;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;


public class Crdr0550 extends BaseReport {
  private final static String PROG_NAME = "crdr0550";
  HashMap<String,HashMap> employeeMap = new HashMap<String,HashMap>();
  HashMap<String,HashMap> notEmployeeMap = new HashMap<String,HashMap>();
  String reportSubtitle = "";
  int type1Cnt = 0;
  int type2Cnt = 0;
  int type3Cnt = 0;
  
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        // is_action="new";
        // clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "PDF":
        // -PDF-
        strAction = "PDF";
        // wp.setExcelMode();
        pdfPrint();
        break;
      default:
        break;
    }

    // dddw_select();
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  @Override
  public void initPage() {

  }

  private String getWhereStr() throws Exception {
     String exReturnDate1 = wp.itemStr("ex_return_date_1");
     String exReturnDate2 = wp.itemStr("ex_return_date_2");
     String exType = wp.itemStr("ex_type");
     
     StringBuffer buf = new StringBuffer();
     switch(exType) {
     case "1":
    	 return buf.append("(")
    			 .append(getType1Sql(exReturnDate1,exReturnDate2))
    			 .append(")").toString();
     case "2":
    	 return buf.append("(")
    			 .append(getType2Sql(exReturnDate1,exReturnDate2))
    			 .append(")").toString();
     case "3":
    	 return buf.append("(")
    			 .append(getType3Sql(exReturnDate1,exReturnDate2))
    			 .append(")").toString();
     }
     
     return buf.append("(").append(getType1Sql(exReturnDate1,exReturnDate2))
    		 .append(" UNION ")
    		 .append(getType2Sql(exReturnDate1,exReturnDate2))
    		 .append(" UNION ")
    		 .append(getType3Sql(exReturnDate1,exReturnDate2))
    		 .append(")").toString();
  }

  private String getWhereStrPdf(int index) throws Exception {
	     String exToNcccDate1 = wp.itemStr("ex_to_nccc_date_1");
	     String exToNcccDate2 = wp.itemStr("ex_to_nccc_date_2");
	     String mailBranch = wp.colStr(index,"MAIL_BRANCH");
	     String barcodeNum = wp.colStr(index,"BARCODE_NUM");
	     String exType = wp.itemStr("ex_type");
	     
	     StringBuffer buf = new StringBuffer();
	     switch(exType) {
	     case "1":
	    	 return buf.append("(")
	    			 .append(getType1SqlPdf(exToNcccDate1,exToNcccDate2,mailBranch,barcodeNum))
	    			 .append(")").toString();
	     case "2":
	    	 return buf.append("(")
	    			 .append(getType2SqlPdf(exToNcccDate1,exToNcccDate2,mailBranch,barcodeNum))
	    			 .append(")").toString();
	     case "3":
	    	 return buf.append("(")
	    			 .append(getType3SqlPdf(exToNcccDate1,exToNcccDate2,mailBranch,barcodeNum))
	    			 .append(")").toString();
	     }
	     
	     return buf.append("(").append(getType1SqlPdf(exToNcccDate1,exToNcccDate2,mailBranch,barcodeNum))
	    		 .append(" UNION ")
	    		 .append(getType2SqlPdf(exToNcccDate1,exToNcccDate2,mailBranch,barcodeNum))
	    		 .append(" UNION ")
	    		 .append(getType3SqlPdf(exToNcccDate1,exToNcccDate2,mailBranch,barcodeNum))
	    		 .append(")").toString();
	  }

  
  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    if("PDF".equals(strAction))
    	queryReadPdf();
    else
    	queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    String sqlCmd = getWhereStr();
    
    wp.selectSQL = " * ";
    wp.daoTable = sqlCmd;
    wp.whereOrder = " ORDER BY MAIL_BRANCH,BARCODE_NUM,TYPE,RETURN_SEQNO ";

    pageQuery();
    // list_wkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
   
  }
  
  public void queryReadPdf() throws Exception {
	    wp.pageControl();

	    String sqlCmd = getWhereStr();
	    
	    wp.selectSQL = " MAIL_BRANCH,FULL_CHI_NAME,BARCODE_NUM,CHI_ADDR,count(*) cnt  ";
	    wp.daoTable = sqlCmd;
	    wp.whereOrder = " group by MAIL_BRANCH,FULL_CHI_NAME,BARCODE_NUM,CHI_ADDR ORDER BY MAIL_BRANCH,BARCODE_NUM  ";

	    pageQuery();
	    // list_wkdata();
	    wp.setListCount(1);
	    if (sqlRowNum <= 0) {
	      alertErr2("此條件查無資料");
	      return;
	    }

	    wp.listCount[1] = wp.dataCnt;
	    wp.setPageValue();
	   
  }

  String getType1Sql(String exReturnDate1,String exReturnDate2) {
	  StringBuffer sbf = new StringBuffer();
	  sbf.append("SELECT '1' AS TYPE, ")
	  .append("'信用卡' AS TT_TYPE, ")
	  .append("A.MAIL_BRANCH, ")
	  .append("C.FULL_CHI_NAME, ")
	  .append("A.RETURN_SEQNO, ")
	  .append("A.BARCODE_NUM, ")
	  .append("A.RETURN_DATE, ")
	  .append("A.CARD_NO, ")
	  .append("B.ID_NO, ")
	  .append("B.CHI_NAME, ")
	  .append("(C.ZIP_CODE ||' '|| C.CHI_ADDR_1 || C.CHI_ADDR_2 || C.CHI_ADDR_3 || C.CHI_ADDR_4 || C.CHI_ADDR_5) AS CHI_ADDR ")
	  .append("FROM CRD_RETURN A,CRD_IDNO B,GEN_BRN C ")
	  .append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
	  .append("AND A.MAIL_BRANCH = C.BRANCH ")
	  .append("AND A.PROC_STATUS IN ('3','6') ")
	  .append("AND A.MAIL_TYPE = '4' ");
	  if(!empty(exReturnDate1)) {
		  sbf.append("AND A.RETURN_DATE >= '")
		  .append(exReturnDate1)
		  .append("'");
	  }
	  if(!empty(exReturnDate2)) {
		  sbf.append("AND A.RETURN_DATE <= '")
		  .append(exReturnDate2)
		  .append("'");
	  }
	  return sbf.toString();
  }
  
  String getType2Sql(String exReturnDate1,String exReturnDate2) {
	  StringBuffer sbf = new StringBuffer();
	  sbf.append("SELECT '2' AS TYPE, ")
	  .append("'VD卡' AS TT_TYPE, ")
	  .append("A.MAIL_BRANCH, ")
	  .append("C.FULL_CHI_NAME, ")
	  .append("A.RETURN_SEQNO, ")
	  .append("A.BARCODE_NUM, ")
	  .append("A.RETURN_DATE, ")
	  .append("A.CARD_NO, ")
	  .append("B.ID_NO, ")
	  .append("B.CHI_NAME, ")
	  .append("(C.ZIP_CODE ||' '|| C.CHI_ADDR_1 || C.CHI_ADDR_2 || C.CHI_ADDR_3 || C.CHI_ADDR_4 || C.CHI_ADDR_5) AS CHI_ADDR ")
	  .append("FROM DBC_RETURN A,DBC_IDNO B,GEN_BRN C ")
	  .append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
	  .append("AND A.MAIL_BRANCH = C.BRANCH ")
	  .append("AND A.PROC_STATUS IN ('3','6') ")
	  .append("AND A.MAIL_TYPE = '4' ");
	  if(!empty(exReturnDate1)) {
		  sbf.append("AND A.RETURN_DATE >= '")
		  .append(exReturnDate1)
		  .append("'");
	  }
	  if(!empty(exReturnDate2)) {
		  sbf.append("AND A.RETURN_DATE <= '")
		  .append(exReturnDate2)
		  .append("'");
	  }
	  return sbf.toString();
  }
  
  String getType3Sql(String exReturnDate1,String exReturnDate2) {
	  StringBuffer sbf = new StringBuffer();
	  sbf.append("SELECT '3' AS TYPE, ")
	  .append("'貴賓卡' AS TT_TYPE, ")
	  .append("A.MAIL_BRANCH, ")
	  .append("C.FULL_CHI_NAME, ")
	  .append("A.RETURN_SEQNO, ")
	  .append("A.BARCODE_NUM, ")
	  .append("A.RETURN_DATE, ")
	  .append("A.PP_CARD_NO AS CARD_NO, ")
	  .append("B.ID_NO, ")
	  .append("B.CHI_NAME, ")
	  .append("(C.ZIP_CODE ||' '|| C.CHI_ADDR_1 || C.CHI_ADDR_2 || C.CHI_ADDR_3 || C.CHI_ADDR_4 || C.CHI_ADDR_5) AS CHI_ADDR ")
	  .append("FROM CRD_RETURN_PP A,CRD_IDNO B,GEN_BRN C ")
	  .append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
	  .append("AND A.MAIL_BRANCH = C.BRANCH ")
	  .append("AND A.PROC_STATUS IN ('3','6') ")
	  .append("AND A.MAIL_TYPE ='4' ");
	  if(!empty(exReturnDate1)) {
		  sbf.append("AND A.RETURN_DATE >= '")
		  .append(exReturnDate1)
		  .append("'");
	  }
	  if(!empty(exReturnDate2)) {
		  sbf.append("AND A.RETURN_DATE <= '")
		  .append(exReturnDate2)
		  .append("'");
	  }
	  return sbf.toString();
  }
  
  String getType1SqlPdf(String exReturnDate1,String exReturnDate2,String mailBranch,String barcodeNum) {
	  StringBuffer sbf = new StringBuffer();
	  sbf.append("SELECT ")
		.append("D.DEP_CHI_NAME, ")
		.append("A.RETURN_DATE, ")
		.append("D.CHI_NAME, ")
		.append("A.CARD_NO, ")
		.append("A.GROUP_CODE, ")
		.append("CASE WHEN E.CORP_NO = '' THEN ")
		.append("DECODE(E.COMBO_INDICATOR,'Y','C','GN') ELSE 'B' END AS TYPE ")
		.append("FROM CRD_RETURN A ,CRD_IDNO B,GEN_BRN C LEFT JOIN ")
		.append("(SELECT B.ID,C.DEP_CHI_NAME,B.CHI_NAME ")
		.append("FROM CRD_EMPLOYEE B, PTR_ACCOUNTING_NO C ")
		.append("WHERE B.ACCOUNTING_NO = C.ACCOUNTING_NO) D ")
		.append("ON B.ID_NO = D.ID ")
		.append("LEFT JOIN CRD_CARD E ON A.CARD_NO = E.CARD_NO ")
		.append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append("AND A.MAIL_BRANCH = C.BRANCH ")
		.append("AND A.PROC_STATUS IN ('3','6') ")
		.append("AND A.MAIL_TYPE = '4' ")
		.append("AND A.MAIL_BRANCH = '")
		.append(mailBranch)
		.append("' ")
		.append("AND A.BARCODE_NUM = '")
		.append(barcodeNum)
		.append("' ");
	  if(!empty(exReturnDate1)) {
		  sbf.append("AND A.RETURN_DATE >= '")
		  .append(exReturnDate1)
		  .append("'");
	  }
	  if(!empty(exReturnDate2)) {
		  sbf.append("AND A.RETURN_DATE <= '")
		  .append(exReturnDate2)
		  .append("'");
	  }
	  return sbf.toString();
  }
  
  String getType2SqlPdf(String exReturnDate1,String exReturnDate2,String mailBranch,String barcodeNum) {
	  StringBuffer sbf = new StringBuffer();
	  sbf.append("SELECT ")
		.append("D.DEP_CHI_NAME, ")
		.append("A.RETURN_DATE, ")
		.append("D.CHI_NAME, ")
		.append("A.CARD_NO, ")
		.append("A.GROUP_CODE, ")
		.append("DECODE(E.PREFAB_FLAG,'Y','VP','V') AS TYPE ")
		.append("FROM DBC_RETURN A ,DBC_IDNO B,GEN_BRN C LEFT JOIN  ")
		.append("(SELECT B.ID, ")
		.append("C.DEP_CHI_NAME, ")
		.append("B.CHI_NAME ")
		.append("FROM CRD_EMPLOYEE B, PTR_ACCOUNTING_NO C ")
		.append("WHERE B.ACCOUNTING_NO = C.ACCOUNTING_NO) D ")
		.append("ON B.ID_NO = D.ID ")
		.append("LEFT JOIN DBC_CARD E ON A.CARD_NO = E.CARD_NO ")
		.append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append("AND A.MAIL_BRANCH = C.BRANCH ")
		.append("AND A.PROC_STATUS IN ('3','6') ")
		.append("AND A.MAIL_TYPE = '4' ")
		.append("AND A.MAIL_BRANCH = '")
		.append(mailBranch)
		.append("' ")
		.append("AND A.BARCODE_NUM = '")
		.append(barcodeNum)
		.append("' ");
	  if(!empty(exReturnDate1)) {
		  sbf.append("AND A.RETURN_DATE >= '")
		  .append(exReturnDate1)
		  .append("'");
	  }
	  if(!empty(exReturnDate2)) {
		  sbf.append("AND A.RETURN_DATE <= '")
		  .append(exReturnDate2)
		  .append("'");
	  }
	  return sbf.toString();
  }
  
  String getType3SqlPdf(String exReturnDate1,String exReturnDate2,String mailBranch,String barcodeNum) {
	  StringBuffer sbf = new StringBuffer();
	  sbf.append("SELECT ")
		.append("D.DEP_CHI_NAME, ")
		.append("A.RETURN_DATE, ")
		.append("D.CHI_NAME, ")
		.append("A.PP_CARD_NO AS CARD_NO, ")
		.append("A.GROUP_CODE, ")
		.append("DECODE(E.VIP_KIND,'1','PP','PPW') AS TYPE ")
		.append("FROM CRD_RETURN_PP A ,CRD_IDNO B,GEN_BRN C LEFT JOIN ")
		.append("(SELECT B.ID, ")
		.append("C.DEP_CHI_NAME, ")
		.append("B.CHI_NAME ")
		.append("FROM CRD_EMPLOYEE B, PTR_ACCOUNTING_NO C ")
		.append("WHERE B.ACCOUNTING_NO = C.ACCOUNTING_NO) D ")
		.append("ON B.ID_NO = D.ID ")
		.append("LEFT JOIN CRD_CARD_PP E ON A.PP_CARD_NO = E.PP_CARD_NO ")
		.append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append("AND A.MAIL_BRANCH = C.BRANCH ")
		.append("AND A.PROC_STATUS IN ('3','6') ")
		.append("AND A.MAIL_TYPE ='4' ")
		.append("AND A.MAIL_BRANCH = '")
		.append(mailBranch)
		.append("' ")
		.append("AND A.BARCODE_NUM = '")
		.append(barcodeNum)
		.append("' ");
	  if(!empty(exReturnDate1)) {
		  sbf.append("AND A.RETURN_DATE >= '")
		  .append(exReturnDate1)
		  .append("'");
	  }
	  if(!empty(exReturnDate2)) {
		  sbf.append("AND A.RETURN_DATE <= '")
		  .append(exReturnDate2)
		  .append("'");
	  }
	  return sbf.toString();
  }
  
  
  void selectCrdEmployee(int index) throws Exception {
	String sqlCmd = "select * from " + getWhereStrPdf(index);
	sqlSelect(sqlCmd);
	//處理員工
	HashMap<String, String> mapN = new HashMap<String, String>();
	HashMap<String, String> mapN1 = new HashMap<String, String>();
	HashMap<String, Integer> mapN2 = new HashMap<String, Integer>();
	HashMap<String, String> mapN3 = new HashMap<String, String>();
	StringBuffer buf = null;
	
	for (int i = 0; i < sqlRowNum; i++) {
		String returnDate = sqlStr(i,"RETURN_DATE");
		String type = "GN".equals(sqlStr(i,"TYPE"))?sqlStr(i,"GROUP_CODE"):sqlStr(i,"TYPE");
		buf = new StringBuffer();
		if(empty(sqlStr(i,"DEP_CHI_NAME"))) {
			String dateType = String.format("%s-%s,", returnDate,type);
			if(mapN1.get(dateType)==null) {
				mapN1.put(dateType,buf.append(dateType).append(1).toString());
				mapN2.put(dateType, 1);
			}else {
				mapN1.put(dateType,buf.append(dateType).append(mapN2.get(dateType).intValue() + 1).append(" ").toString());
				mapN2.put(dateType, mapN2.get(dateType).intValue() + 1);
			}
		}
	}
	for(String key :mapN1.keySet()) {
		buf = new StringBuffer();
		String[] type = null;
		String dateType = mapN1.get(key).toString();
		String[] splits = dateType.split("-");
		if(splits.length>1) {
			type = splits[1].split(",");
			if(type.length>1) {
				if(mapN.get(type[0]) == null) {
					mapN.put(type[0], dateType);
				}else {
					buf.append(mapN.get(type[0])).append(dateType).append(".");
					mapN.put(type[0],buf.toString());
				}
			}
		}
	}
	notEmployeeMap.clear();
	for(String key :mapN.keySet()) {
		mapN3 = new HashMap<String,String>();
		String dateType = mapN.get(key).toString();
		mapN3.put("DATA",dateType);
		notEmployeeMap.put(key,mapN3);
	}
	
	HashMap<String, String> mapY = new HashMap<String, String>();
	HashMap<String, String> mapY1 = new HashMap<String, String>();
	HashMap<String, String> mapY3 = new HashMap<String, String>();
	for (int i = 0; i < sqlRowNum; i++) {
		String embossDate = sqlStr(i, "EMBOSS_DATE");
		String type = "GN".equals(sqlStr(i,"TYPE"))?sqlStr(i,"GROUP_CODE"):sqlStr(i,"TYPE");
		String depChiName = sqlStr(i, "DEP_CHI_NAME");
		String chiName = sqlStr(i, "CHI_NAME");
		buf = new StringBuffer();
		if(!empty(sqlStr(i,"DEP_CHI_NAME"))) {
			String dateType = String.format("%s,%s-", depChiName, embossDate);
			if (mapY1.get(dateType) == null) {
				mapY1.put(dateType, String.format("%s△%s%s", dateType, type, chiName));
			} else {
				buf.append(mapY1.get(dateType)).append(String.format("△%s%s", type, chiName));
				mapY1.put(dateType, buf.toString());
			}
		}
	}

	mapY = new HashMap<String, String>();
	for (String key : mapY1.keySet()) {
		String dateType = mapY1.get(key).toString();
		int splits = dateType.split("△").length - 1;
		if (splits >= 1) {
			mapY.put(key, String.format("%s,%s. ", dateType, splits));
		}
	}
	employeeMap.clear();
	for (String key : mapY.keySet()) {
		mapY3 = new HashMap<String, String>();
		String dateType = mapY.get(key).toString();
		mapY3.put("DATA", dateType);
		employeeMap.put(key, mapY3);
	}

  }

  void listWkdata() throws Exception {
	  int r = 0;
	  for(int i=0 ; i<wp.selectCnt ;i+=2) {
		  String pdfData = "";
		  selectCrdEmployee(i);	  
		  wp.colSet(r,"MAIL_BRANCH1", wp.colStr(i,"MAIL_BRANCH")); 
		  wp.colSet(r,"FULL_CHI_NAME1", String.format("%s 副理 查收", wp.colStr(i,"FULL_CHI_NAME")));	 
		  wp.colSet(r,"BARCODE_NUM1", wp.colStr(i,"BARCODE_NUM"));		
		  wp.colSet(r,"CHI_ADDR1", wp.colStr(i,"CHI_ADDR"));
			for (String key : notEmployeeMap.keySet()) {
				 pdfData += notEmployeeMap.get(key).get("DATA").toString();
			}
			for (String key : employeeMap.keySet()) {
				pdfData +=  employeeMap.get(key).get("DATA").toString();
			}
			wp.colSet(r, "col1" , pdfData);
		  if(i+1>=wp.selectCnt) {
			  r++;
			  break;
		  } 
		  pdfData = "";
		  selectCrdEmployee(i+1);
		  wp.colSet(r,"MAIL_BRANCH2", wp.colStr(i+1,"MAIL_BRANCH"));
		  wp.colSet(r,"FULL_CHI_NAME2", String.format("%s 副理 查收", wp.colStr(i+1,"FULL_CHI_NAME")));
		  wp.colSet(r,"BARCODE_NUM2", wp.colStr(i+1,"BARCODE_NUM"));
		  wp.colSet(r,"CHI_ADDR2", wp.colStr(i+1,"CHI_ADDR"));
			for (String key : notEmployeeMap.keySet()) {
				pdfData += notEmployeeMap.get(key).get("DATA").toString();
			}
			for (String key : employeeMap.keySet()) {
				pdfData += employeeMap.get(key).get("DATA").toString();
			}
			wp.colSet(r, "col2" , pdfData);
		  r++;
	  }
	  wp.listCount[0] = r;
  }
  
  
  void pdfPrint() throws Exception {
    wp.reportId = PROG_NAME;
    // -cond-
    // ===========================
    wp.pageRows = 99999;
    queryFunc();
    listWkdata();
    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = PROG_NAME + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 40;
    pdf.pageVert= true; //直印
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

}


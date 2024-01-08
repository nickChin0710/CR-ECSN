/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE            Version     AUTHOR        DESCRIPTION                      *
* ---------       --------      ----------         --------------------------*
* 112-04-24  V1.00.00   Ryan        program initial                     *

******************************************************************************/
package crdr01;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;


public class Crdr0540 extends BaseReport {
  private final static String PROG_NAME = "crdr0540";
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

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    String sqlCmd = getWhereStr();
    
    wp.selectSQL = " * ";
    wp.daoTable = sqlCmd;
    wp.whereOrder = " ORDER BY TYPE,RETURN_SEQNO ";

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
	  .append("A.RETURN_SEQNO, ")
	  .append("A.BARCODE_NUM, ")
	  .append("A.RETURN_DATE, ")
	  .append("A.CARD_NO, ")
	  .append("B.ID_NO, ")
	  .append("B.CHI_NAME, ")
	  .append("A.ZIP_CODE, ")
	  .append("(A.MAIL_ADDR1 || A.MAIL_ADDR2 || A.MAIL_ADDR3 || A.MAIL_ADDR4 || A.MAIL_ADDR5) AS MAIL_ADDR ")
	  .append("FROM CRD_RETURN A,CRD_IDNO B ")
	  .append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
	  .append("AND PROC_STATUS IN ('3','6') ")
	  .append("AND MAIL_TYPE IN ('1','2') ");
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
	  .append("A.RETURN_SEQNO, ")
	  .append("A.BARCODE_NUM, ")
	  .append("A.RETURN_DATE, ")
	  .append("A.CARD_NO, ")
	  .append("B.ID_NO, ")
	  .append("B.CHI_NAME, ")
	  .append("A.ZIP_CODE, ")
	  .append("(A.MAIL_ADDR1 || A.MAIL_ADDR2 || A.MAIL_ADDR3 || A.MAIL_ADDR4 || A.MAIL_ADDR5) AS MAIL_ADDR ")
	  .append("FROM DBC_RETURN A,DBC_IDNO B ")
	  .append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
	  .append("AND PROC_STATUS IN ('3','6') ")
	  .append("AND MAIL_TYPE IN ('1','2') ");
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
	  .append("A.RETURN_SEQNO, ")
	  .append("A.BARCODE_NUM, ")
	  .append("A.RETURN_DATE, ")
	  .append("A.PP_CARD_NO AS CARD_NO, ")
	  .append("B.ID_NO, ")
	  .append("B.CHI_NAME, ")
	  .append("A.ZIP_CODE, ")
	  .append("(A.MAIL_ADDR1 || A.MAIL_ADDR2 || A.MAIL_ADDR3 || A.MAIL_ADDR4 || A.MAIL_ADDR5) AS MAIL_ADDR ")
	  .append("FROM CRD_RETURN_PP A,CRD_IDNO B ")
	  .append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
	  .append("AND PROC_STATUS IN ('3','6') ")
	  .append("AND MAIL_TYPE IN ('1','2') ");
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

  void listWkdata() throws Exception {
	  int r = 0;
	  for(int i=0 ; i<wp.selectCnt ;i+=2) {
		  wp.colSet(r,"ZIP_CODE1", wp.colStr(i,"ZIP_CODE")); 
		  wp.colSet(r,"MAIL_ADDR1", wp.colStr(i,"MAIL_ADDR"));
		  wp.colSet(r,"CHI_NAME1", String.format("%s 先生/小姐 收", wp.colStr(i,"CHI_NAME")));	 
		  wp.colSet(r,"BARCODE_NUM1", wp.colStr(i,"BARCODE_NUM"));		
		  wp.colSet(r,"RETURN_SEQNO1", wp.colStr(i,"RETURN_SEQNO"));	

		  if(i+1>=wp.selectCnt) {
			  r++;
			  break;
		  } 
		  wp.colSet(r,"ZIP_CODE2", wp.colStr(i+1,"ZIP_CODE"));
		  wp.colSet(r,"MAIL_ADDR2", wp.colStr(i+1,"MAIL_ADDR"));
		  wp.colSet(r,"CHI_NAME2", String.format("%s 先生/小姐 收", wp.colStr(i+1,"CHI_NAME")));
		  wp.colSet(r,"BARCODE_NUM2", wp.colStr(i+1,"BARCODE_NUM"));
		  wp.colSet(r,"RETURN_SEQNO2", wp.colStr(i+1,"RETURN_SEQNO"));	
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
    pdf.pageCount = 48;
    pdf.pageVert= true; //直印
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

}


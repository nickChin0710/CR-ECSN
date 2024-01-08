/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-12-19  V1.00.00  Ma Chao   Genr0120  會計分錄(日結)表列印  
* 111-12-27  V1.00.01  Ma Chao   明細，傳票程式二合一                                                                                                                                  *
* 112-01-09  V1.00.02  Machao    order by 順序調整                                                                                                                                *     
* 112-02-16  V1.00.03  Machao    程式需求調整 ，幣別預設值設置                                                                                                                   *
* 112-07-06  V1.00.04  Machao    調整傳票PDF、直式一頁3筆                                                                                                                         *
* 112-07-25  V1.00.05  Ryan      memo1 ==> memo1||', ' ||memo2||', ' ||memo3               *                                                  *
* 112-08-11  V1.00.06  Grace     '中山記帳' --> '中心記帳'; 不限人工帳/自動起帳皆以'中心記帳'字樣呈現                         *
* 112-08-21  V1.00.07  Grace     memo1/2/3 中間不做區隔    
* 112-08-30  V1.00.08  Machao    調整明細呈現樣式及各報表格式                                                                                                             *        
* 112-11-21  V1.00.09  Zuwei Su  增列印條件 + (Grace) 經辦欄位暫為空值                                                                                     *    
* 112-12-28  V1.00.10  Zuwei Su  產出PDF摘要按20字換行                                                                                    *    
*                                                                                           *
********************************************************************************************/
package genr01;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDF2;

public class Genr0120 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "genr0120";
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
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      // wp.setExcelMode();
      pdfPrint();
    }else if (eqIgno(wp.buttonCode, "PDF2")) { // -PDF-
        strAction = "PDF2";
        pdfPrint();
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
    String txDateF = wp.itemStr("exDateS");
    String txDateU = wp.itemStr("exDateE");
    String exUserId = wp.itemStr("ex_user_id");
    String exRefnoS = wp.itemStr("ex_refno_start");
    String exRefnoE = wp.itemStr("ex_refno_end");
    String exModPgm = wp.itemStr("ex_mod_pgm");

    String lsWhere = "where 1=1";

    if (empty(txDateF) == false) {
      lsWhere += " and tx_date >= :tx_date_f";
      setString("tx_date_f", txDateF);
    }

    if (empty(txDateU) == false) {
      lsWhere += " and tx_date <= :tx_date_u";
      setString("tx_date_u", txDateU);
    }
    if(!wp.itemStr("ex_curr_chi_name").equals("0")) {
    	lsWhere += " and curr_chi_name = :curr_chi_name";
    	if(wp.itemStr("ex_curr_chi_name").equals("1"))
    	setString("curr_chi_name", "新台幣");
    	if(wp.itemStr("ex_curr_chi_name").equals("2"))
        	setString("curr_chi_name", "美金");
    	if(wp.itemStr("ex_curr_chi_name").equals("3"))
        	setString("curr_chi_name", "日幣");
    }

    if(exRefnoS.length() > 0) {
        lsWhere += " and v.refno >= :refnoS";
        setString("refnoS", exRefnoS);
    }
    if(exRefnoE.length() > 0) {
        lsWhere += " and v.refno <= :refnoE";
        setString("refnoE", exRefnoE);
    }
    if(exUserId.length() > 0) {
        lsWhere += " and v.mod_user = :mod_user";
        setString("mod_user", exUserId);
    }
    if(exModPgm.length() > 0) {
        lsWhere += " and v.mod_pgm like :mod_pgm||'%'";
        setString("mod_pgm", exModPgm);
    }
    
    if(wp.itemEq("ex_post_flag","Y")) {
    	lsWhere += " and post_flag = 'Y' ";
    }
    
    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  public void queryExcel() throws Exception {
    if (getWhereStr() == false)
      return;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryReadExcel();
  }
  
  public void queryPdf() throws Exception {
	    if (getWhereStr() == false)
	      return;
	    wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();
	    queryReadPdf();
	  }
  private void setParameter() throws Exception {


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "" + "v.tx_date , " + "v.refno , " + " brno, "
    		+ "p.curr_chi_name , "
	        + "decode(v.dbcr,'D','借','C','貸','') as dbcr, "
	        + "v.dbcr as dbcc, " 
    		+ "v.ac_no, "+ "j.ac_brief_name, " + "v.sign_flag , " + "v.amt , " 
	        + "v.memo1 || v.memo2 || v.memo3 as memo123, " + " v.ias24_id, " + " v.acct_no, " + " v.acct_name, "+ "v.mod_pgm  "
//	        + "v.memo1 || ', ' || v.memo2 || ', ' || v.memo3 as memo123, " + " v.ias24_id, " + " v.acct_no, " + " v.acct_name, "+ "v.mod_pgm  "
         ;

	if(wp.itemStr("post_flag").equals("Y")) {
		wp.daoTable = "gen_vouch_h v left join ptr_currcode p on v.curr=p.curr_code_gl "
	    		+ "left join gen_acct_m J on v.ac_no=j.ac_no ";
	}else {
		wp.daoTable = "gen_vouch v left join ptr_currcode p on v.curr=p.curr_code_gl "
	    		+ "left join gen_acct_m J on v.ac_no=j.ac_no ";
	}
    
    wp.whereOrder = " order by v.tx_date,v.refno"
    		+ ",dbcc desc ,seqno";

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    wp.setListCount(1);
    sun = wp.selectCnt;
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    for(int ii = 0; ii<sun;ii++) {
	    if(wp.getValue("ac_no",ii).equals("10000000")) {
	    	wp.setValue("ac_brief_name", " 庫存現金 ", ii);
	    }
//	    if(wp.itemStr("curr_chi_name").equals("全部"))
//	    wp.setValue("curr_chi_name", "全部");
	    
	   if( wp.getValue("dbcr",ii).equals("貸")) {
		   String indent ="__";
		   wp.colSet( ii,"indent", indent);

		   String amtC = wp.getValue("amt",ii);
		   wp.setValue("amt_C", amtC, ii);
	   }
	   if( wp.getValue("dbcr",ii).equals("借")) {
		   String amtD = wp.getValue("amt",ii);
		   wp.setValue("amt_D", amtD, ii);
	   }
	   
    }

    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
  }
  


public void queryReadPdf() throws Exception {
	    wp.pageControl();

	    if (getWhereStr() == false)
	      return;

	    wp.selectSQL = "" + "v.tx_date , " + "v.refno , " + " brno, "
	    		+ "p.curr_chi_name , "
		        + "decode(v.dbcr,'D','借','C','貸','') as dbcr, "
	    		+ "decode(v.dbcr,'D','轉帳支出','C','轉帳收入','') as cond_2, "
		        + "v.dbcr as dbcc, " 
	    		+ "v.ac_no, "+ "j.ac_brief_name, " + "v.sign_flag , " + "v.amt , " 
	    		+ "decode(v.dbcr,'D','(借)','C','(貸)','') || v.ac_no || j.ac_brief_name  as acno_chi, "
	    		//+ "v.memo1 || ', ' || v.memo2 || ', ' || v.memo3 as memo123, "
	    		+ "v.memo1 || v.memo2 || v.memo3 as memo123, "
	    		+ " v.ias24_id, " + " v.acct_no, " + " v.acct_name, "+ "v.mod_user,  " + "v.mod_pgm  "
	         ;

		if(wp.itemStr("post_flag").equals("Y")) {
			wp.daoTable = "gen_vouch_h v left join ptr_currcode p on v.curr=p.curr_code_gl "
		    		+ "left join gen_acct_m J on v.ac_no=j.ac_no ";
		}else {
			wp.daoTable = "gen_vouch v left join ptr_currcode p on v.curr=p.curr_code_gl "
		    		+ "left join gen_acct_m J on v.ac_no=j.ac_no ";
		}
	    
	    wp.whereOrder = " order by v.tx_date,v.refno"
	    		+ ",dbcc desc ,seqno";

	    pageQuery();
	    wp.setListCount(1);
	    sun = wp.selectCnt;
	    if (sqlRowNum <= 0) {
	      alertErr2("此條件查無資料");
	      return;
	    }
 	    for(int ii = 0; ii<sun;ii++) {
 	        String memo123 = wp.getValue("memo123",ii);
 	        if (memo123.length() > 20) {
 	           memo123 = memo123.substring(0, 20) + "\n" + memo123.substring(20);
 	           wp.setValue("memo123", memo123, ii);
 	        }
 	        
 		   if(!empty(wp.getValue("acct_no",ii)) && !empty(wp.getValue("acct_name",ii))) {
 			   wp.colSet("acctNM", wp.getValue("acct_no",ii)+"-"+wp.getValue("acct_name",ii));
 		   }
 		   String dbcr = "";
 		   String refno =wp.getValue("refno", ii);
 		  String txDate = wp.getValue("tx_Date",ii);
 		   if(wp.getValue("dbcr", ii).equals("借")) {
 			   dbcr = "D" ;
 		   }else {
 			  dbcr = "C" ;
 		   }
 		  String[] acNo = SelectGenVouch(refno,dbcr,txDate);
 		  if(acNo.length == 1) {
 			 wp.setValue("ac_no_DC1", acNo[0],ii); 
 		  }else if(acNo.length == 2) {
  			 wp.setValue("ac_no_DC1", acNo[0],ii); 
  			 wp.setValue("ac_no_DC2", acNo[1],ii); 
  		  }else if(acNo.length == 3){
  			wp.setValue("ac_no_DC1", acNo[0],ii); 
 			wp.setValue("ac_no_DC2", acNo[1],ii);
 			wp.setValue("ac_no_DC3", acNo[2],ii); 
  		  }else if(acNo.length >= 4){
  			wp.setValue("ac_no_DC1", acNo[0],ii); 
 			wp.setValue("ac_no_DC2", acNo[1],ii);
 			wp.setValue("ac_no_DC3", acNo[2],ii); 
 			wp.setValue("ac_no_DC4", acNo[3],ii); 
 			wp.setValue("ac_no_DC5", acNo[4],ii); 
 			wp.setValue("ac_no_DC6", acNo[5],ii);
 			wp.setValue("ac_no_DC7", acNo[6],ii); 
 			wp.setValue("ac_no_DC8", acNo[7],ii); 
  		  }
 		   
 		   String txY = txDate.substring(0, 4);
 		   int txDateYY = Integer.parseInt(txY)-1911;
 		   String txDateY = String.valueOf(txDateYY);
 		   //wp.setValue("txDateY", txDateY,ii);
 		   String txDateM = txDate.substring(4, 6);
 		   //wp.setValue("txDateM", txDateM,ii);
 		   String txDateD = txDate.substring(6, 8);
 		   //wp.setValue("txDateD", txDateD,ii);	
 		   wp.setValue("txDate_chi", "中華民國"+txDateY+"年"+txDateM+"月"+txDateD+"日",ii);
 		  
 		   //經辦
 		   String crtName = SelectSecUser(txDate,refno);
 		   if (empty(crtName) == true) {
 			   crtName=wp.getValue("mod_user", ii);
 			 }
 	 	//	 wp.setValue("crt_name",crtName,ii);
 		   if(wp.getValue("mod_pgm",ii).equals("genp0110") || 
 				   wp.getValue("mod_pgm",ii).equals("genp0120")){
 			   //String crtName = SelectSecUser(txDate,refno);
 			   //wp.setValue("crt_name",crtName,ii);
 			   wp.setValue("crt_name", "",ii);	//暫放空值
 		   }else {
 			   wp.setValue("crt_name", "",ii);
 		   }
 		   //(20230811, grace: 不論人工起帳/自動起帳, 皆以 '中心記帳' 字樣呈現)
 		   /*
 		   if(wp.getValue("mod_pgm",ii).equals("genp0110") || 
 				   wp.getValue("mod_pgm",ii).equals("genp0120")){
 			   //String userNameK = SelectSecUser(wp.getValue("mod_user",ii));
 			  //wp.setValue("userNameK", userNameK,ii);	//會計
 			  wp.setValue("userNameJ1",crtName,ii);		
 		   }else {
 			  //wp.setValue("userNameJ1", "中山",ii);		//記帳
 			  wp.setValue("userNameJ1", "中心",ii);		//記帳 			  
 			  wp.setValue("userNameJ2", "記帳",ii);
 		   }
 		   */
// 			  wp.setValue("userNameJ1", "中心",ii);		//記帳 			  
// 			  wp.setValue("userNameJ2", "記帳",ii);
 		   //核章
 		  //if(wp.getValue("mod_pgm",ii).equals("genp0120")){
 		//	 String modUser =SelectSecUser(wp.getValue("mod_user", ii));
 		//	wp.setValue("mod_user", modUser,ii);
 		//  }else {
 			 wp.setValue("mod_user", "",ii);
 		  //}
 		  
 		  if(!empty(wp.getValue("ias24_id", ii))) {
// 			 wp.setValue("checkY", "V",ii);
 			 String IAS24 = wp.getValue("ias24_id", ii);
 			wp.setValue("ias24_id", "V是 " + " □否" + " IAS24關係人統編/身分證號：" + IAS24,ii);
 		  }else {
// 			 wp.setValue("checkN", "V",ii);
 			 wp.setValue("ias24_id", "□是 " + " V否" + " IAS24關係人統編/身分證號：" ,ii);
 		  }
 	    }
	    wp.listCount[1] = wp.dataCnt;
	    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
	    wp.setPageValue();
	  }
 
  private String SelectSecUser(String modUser) {
	  String lsSql = " select usr_cname from sec_user where usr_id = ? ";
      sqlSelect(lsSql, new Object[]{modUser});
      
	return sqlStr("usr_cname");
}
  
  private String SelectSecUser(String txDate ,String refno) {
	  String lsSql = " SELECT sec_user.usr_cname FROM SEC_USER, gen_vouch where sec_user.usr_id=gen_vouch.mod_user AND gen_vouch.mod_pgm IN ('genp0110','genp0120') "
	  		+ " and tx_date = ? and refno = ? ";
      sqlSelect(lsSql, new Object[]{txDate,refno});
      
	return sqlStr("usr_cname");
}

private String[] SelectGenVouch(String refno, String dbcr,String txdate) {
	  String lsSql = " select ac_no from gen_vouch where "
	  		+ "refno = ?  and dbcr <> ? "
	  		+ "and tx_date = ? ";
	  sqlSelect(lsSql, new Object[]{refno,dbcr,txdate});
      
      String[] acNo = new String[sqlRowNum];
      for(int i =0;i<sqlRowNum;i++) {
    	  acNo[i] = sqlStr(i,"ac_no");
      }
	return acNo;
}

public void queryReadExcel() throws Exception {
	    wp.pageControl();

	    if (getWhereStr() == false)
	      return;

	    wp.selectSQL = "" + "v.tx_date , " + "v.refno , " + " brno, "
	    		+ "p.curr_chi_name , "
		        + "decode(v.dbcr,'D','借','C','貸','') as dbcr, "
		        + "v.dbcr as dbcc, " 
		        + "decode(v.dbcr,'D',v.amt) amt_D, "
	    		+ "decode(v.dbcr,'C',v.amt) amt_C, "
	    		+ "v.ac_no, "+ "j.ac_brief_name, "  
	    		//+ "v.memo1 || ', ' || v.memo2 || ', ' || v.memo3 as memo123, "
	    		+ "v.memo1 || v.memo2 || v.memo3 as memo123, "
	    		+ " v.ias24_id, " + " v.acct_no, " + " v.acct_name, "+ "v.mod_pgm  "
	         ;
	         
		if(wp.itemStr("post_flag").equals("Y")) {
			wp.daoTable = "gen_vouch_h v left join ptr_currcode p on v.curr=p.curr_code_gl "
		    		+ "left join gen_acct_m J on v.ac_no=j.ac_no ";
		}else {
			wp.daoTable = "gen_vouch v left join ptr_currcode p on v.curr=p.curr_code_gl "
		    		+ "left join gen_acct_m J on v.ac_no=j.ac_no ";
		}
	    
	    wp.whereOrder = " order by v.tx_date,v.refno"
	    		+ ",dbcc desc ,seqno";

	    if (strAction.equals("XLS")) {
	      selectNoLimit();
	    }
	    pageQuery();
	    wp.setListCount(1);
	    sun = wp.selectCnt;
	    if (sqlRowNum <= 0) {
	      alertErr2("此條件查無資料");
	      return;
	    }
	    for(int ii = 0; ii<sun;ii++) {
	    if(!wp.getValue("amt_C",ii).equals("0") && wp.getValue("amt_D",ii).equals("0")) {
	    	wp.setValue("amt_D", " ", ii);
	    }else {
	    	wp.setValue("amt_C"," ",ii);
	   }
	    if(wp.getValue("ac_no",ii).equals("10000000")) {
	    	wp.setValue("ac_brief_name", " 庫存現金 ", ii);
	    }
	    if( wp.getValue("dbcr",ii).equals("貸")) {
			   wp.colSet( ii,"ac_brief_name", "__"+wp.getValue("ac_brief_name",ii));
	    }
	    
	    }
	    wp.listCount[1] = wp.dataCnt;
	    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
	    wp.setPageValue();
	  }
  
  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = "genr0120_excel.xlsx";
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

  void pdfPrint() throws Exception {
    if(eqIgno(strAction,"PDF")){
    	 wp.reportId = "Genr0120_明細";
    	    wp.pageRows = 9999;
    	    queryExcel();
    	    wp.setListCount(1);
    	    TarokoPDF pdf = new TarokoPDF();
    	    wp.fileMode = "N";
    	    pdf.excelTemplate = "genr0120_excel.xlsx";
    	    pdf.sheetNo = 0;
    	    pdf.procesPDFreport(wp);
    	    pdf = null;
        return;
      }
    else if(eqIgno(strAction,"PDF2")){
	 	wp.reportId = "Genr0120_傳票";
 	    wp.pageRows = 9999;
 	    queryPdf();
// 	    wp.setListCount(1);
 	    TarokoPDF pdf = new TarokoPDF();
// 	    pdf.fixHeader[1] = "cond_1";
 	    wp.fileMode = "Y";
 	    pdf.pageVert = true;
 	    pdf.excelTemplate = "genr0120_pdf.xlsx";
 	    pdf.sheetNo = 0;
 	    pdf.pageCount = 30;
 	    pdf.procesPDFreport(wp);
 	    pdf = null;
        return;
   }
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_user_id");
        dropdownList("dddw_user_id", "gen_vouch", "distinct mod_user", "mod_user", null);
    } catch (Exception ex) {
    }
  }

}

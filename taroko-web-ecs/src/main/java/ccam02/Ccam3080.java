/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-18  V1.00.00  JustinWu  program initial  
* 108-12-25  V1.00.01  JustinWu  remove risk_factor                           *
* 109-04-06  V1.00.02  JustinWu  fix the bug of displaying total count
* 109-04-08  V1.00.03  JustinWu  fix the bugs of where conditions 
* 109-04-20  V1.00.04  yanghan  修改了變量名稱和方法名稱*
* 111-04-19  V1.00.05  Alex      add pdf                                     *
******************************************************************************/

package ccam02;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ccam3080 extends BaseAction implements InfacePdf {
  String acctType = "", idOrCard = "", cardIndicator = "";

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        /* 更新功能 */
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
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
      case "C":
        // -資料處理-
        procFunc();
        break;
      case "PDF":
		// --報表列印
		pdfPrint();
		break;
      default:
        break;
    }

  }

  @Override
  public void queryFunc() throws Exception {
	  
	if(chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
		alertErr("有效日期: 起迄錯誤");
		return ;
	}
	  
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    String select1, select2, daoTable1, daoTable2, where1, where2;
    String acctTypePage, cardIndicatorPage;
    wp.pageControl();
    acctTypePage = wp.itemStr("ex_acct_type");
    cardIndicatorPage = wp.itemStr("ex_card_indicator");
            
    select1 = "'已覆核' as apr_temp , 'Y' as temp_flag , hex(v.rowid) as rowid , v.acct_type , v.idno_p_seqno , v.corp_p_seqno , v.acno_p_seqno , v.with_sup_card , "
    		+ "v.start_date , v.end_date , v.mod_user , to_char(v.mod_time,'yyyymmdd') as mod_date , to_char(v.mod_time,'hh24miss') as mod_time2 , "
    		+ "p.chin_name , p.card_indicator , c.current_code , decode(v.corp_p_seqno,'',i.id_no,c.card_no) as idorcard "
    		;
    
    daoTable1 = "cca_vip v left join ptr_acct_type p on v.acct_type = p.acct_type "
    		+ " left join crd_idno i on v.idno_p_seqno = i.id_p_seqno "
    		+ " left join crd_card c on v.corp_p_seqno = c.corp_p_seqno and v.idno_p_seqno = c.id_p_seqno and c.corp_p_seqno <> '' "
    		;
    where1 = " 1=1 ";
    
    select2 = "'未覆核 ' as apr_temp , 'N' as temp_flag , hex(v.rowid) as rowid , v.acct_type , v.idno_p_seqno , v.corp_p_seqno , v.acno_p_seqno , v.with_sup_card , "
    		+ "v.start_date , v.end_date , v.mod_user , to_char(v.mod_time,'yyyymmdd') as mod_date , to_char(v.mod_time,'hh24miss') as mod_time2 , "
    		+ "p.chin_name , p.card_indicator , c.current_code , decode(v.corp_p_seqno,'',i.id_no,c.card_no) as idorcard "
    		;
    
    daoTable2 = "cca_vip_t v left join ptr_acct_type p on v.acct_type = p.acct_type "
    		+ " left join crd_idno i on v.idno_p_seqno = i.id_p_seqno "
    		+ " left join crd_card c on v.corp_p_seqno = c.corp_p_seqno and v.idno_p_seqno = c.id_p_seqno and c.corp_p_seqno <> ''  "
    		;
    where2 = " 1=1 ";
    
    if(empty(acctTypePage)) {
    	if(wp.itemEq("ex_apr_flag", "0") || wp.itemEq("ex_apr_flag","Y")) {
    		where1 += sqlCol(wp.itemStr("ex_date1"),"v.start_date",">=")
        			+ sqlCol(wp.itemStr("ex_date2"),"v.end_date","<=")
        			+ sqlCol(wp.itemStr("ex_mod_user"),"v.mod_user","like%")
        			;
    	}
    	
    	if(wp.itemEq("ex_apr_flag", "0") || wp.itemEq("ex_apr_flag","N")) {
    		where2 += sqlCol(wp.itemStr("ex_date1"),"v.start_date",">=")
        			+ sqlCol(wp.itemStr("ex_date2"),"v.end_date","<=")
        			+ sqlCol(wp.itemStr("ex_mod_user"),"v.mod_user","like%")
        			;    	
    	}    	
    }	else if("01".equals(acctTypePage)) {
    	if(wp.itemEq("ex_apr_flag", "0") || wp.itemEq("ex_apr_flag","Y")) {
    		where1 += sqlCol(wp.itemStr("ex_date1"),"v.start_date",">=")
        			+ sqlCol(wp.itemStr("ex_date2"),"v.end_date","<=")
        			+ sqlCol(wp.itemStr("ex_mod_user"),"v.mod_user","like%")
        			+ sqlCol(wp.itemStr("ex_id_no"),"i.id_no")
        			+ sqlCol(wp.itemStr("ex_acct_type"),"v.acct_type")
        			;
    	}
    	if(wp.itemEq("ex_apr_flag", "0") || wp.itemEq("ex_apr_flag","N")) {
        	where2 += sqlCol(wp.itemStr("ex_date1"),"v.start_date",">=")
        			+ sqlCol(wp.itemStr("ex_date2"),"v.end_date","<=")
        			+ sqlCol(wp.itemStr("ex_mod_user"),"v.mod_user","like%")
        			+ sqlCol(wp.itemStr("ex_id_no"),"i.id_no")
        			+ sqlCol(wp.itemStr("ex_acct_type"),"v.acct_type")
        			;
    	}    	    	
    }	else	{
    	if(wp.itemEq("ex_apr_flag", "0") || wp.itemEq("ex_apr_flag","Y")) {
    		where1 += sqlCol(wp.itemStr("ex_date1"),"v.start_date",">=")
        			+ sqlCol(wp.itemStr("ex_date2"),"v.end_date","<=")
        			+ sqlCol(wp.itemStr("ex_mod_user"),"v.mod_user","like%")
        			+ sqlCol(wp.itemStr("ex_card_no"),"c.card_no")
        			+ sqlCol(wp.itemStr("ex_acct_type"),"v.acct_type")
        			;
    	}
    	if(wp.itemEq("ex_apr_flag", "0") || wp.itemEq("ex_apr_flag","N")) {
    		where2 += sqlCol(wp.itemStr("ex_date1"),"v.start_date",">=")
        			+ sqlCol(wp.itemStr("ex_date2"),"v.end_date","<=")
        			+ sqlCol(wp.itemStr("ex_mod_user"),"v.mod_user","like%")
        			+ sqlCol(wp.itemStr("ex_card_no"),"c.card_no")
        			+ sqlCol(wp.itemStr("ex_acct_type"),"v.acct_type")
        			;
    	}    	    	
    }
    
    if(wp.itemEq("ex_apr_flag", "0")) {
    	wp.selectSQL = "*";
        wp.daoTable = 
        		" (select " + select1 + " from " + daoTable1 + " where " + where1 + " union all "
        		+ " select "+ select2 + " from " + daoTable2 + " where " + where2 + " ) ";
    }	else if(wp.itemEq("ex_apr_flag", "Y")) {
    	wp.selectSQL = select1;
    	wp.daoTable = daoTable1 ; 
    	wp.whereStr = " where " + where1;
    	wp.queryWhere = wp.whereStr;
    }	else if(wp.itemEq("ex_apr_flag", "N")) {
    	wp.selectSQL = select2;
    	wp.daoTable = daoTable2 ; 
    	wp.whereStr = " where " + where2;
    	wp.queryWhere = wp.whereStr;
    }
            
    wp.whereOrder = " order by acct_type Asc ";
    
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }

    queryAfter();
    wp.setPageValue();
    
    if("PDF".equals(wp.buttonCode)) {
    	queryAfterForPDF();
    }
    
  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
	String idOrCard = wp.itemStr("data_k2"); // idorcard
	String cardIndicator = wp.itemStr("data_k3"); // card_indicator
    String rowid = wp.itemStr("data_k4");
    String tempApr = wp.itemStr("data_k5");
    String idOrCardColName = "";

    switch (cardIndicator) {
      case "1":
        idOrCardColName = "kk_id_no";
        break;

      case "2":
        idOrCardColName = "kk_card_no";
        break;
    }
       
    if("Y".equals(tempApr)) {
    	wp.selectSQL = "'Y' as temp_flag ," + " hex(rowid) as rowid , " + " acct_type as kk_acct_type, "
    	        + " corp_p_seqno , " + " idno_p_seqno, " + " with_sup_card, " + " start_date, "
    	        + " end_date, " + " crt_date, " + " crt_user, " + " apr_date, " + " apr_user, "
    	        + " mod_user, " + " to_char(mod_time,'yyyymmdd') as mod_date ";
    	wp.daoTable = "cca_vip";
    }	else	{
    	wp.selectSQL = "'N' as temp_flag ," + " hex(rowid) as rowid , " + " acct_type as kk_acct_type, "
    	        + " corp_p_seqno , " + " idno_p_seqno, " + " with_sup_card, " + " start_date, "
    	        + " end_date, " + " crt_date, " + " crt_user, " 
    	        + " mod_user, " + " to_char(mod_time,'yyyymmdd') as mod_date , mod_audcode ";
    	wp.daoTable = "cca_vip_t";
    }
    
    wp.whereStr = "where 1=1" + sqlCol(rowid, "hex(rowid)");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料");
    }

    wp.colSet(idOrCardColName, idOrCard);
    wp.colSet("card_indicator", cardIndicator);

    if("N".equals(tempApr)) {
    	if(wp.colEq("mod_audcode", "A")) {
    		wp.colSet("temp_desc", "待覆核處理指示: 新增");
    	}	else if(wp.colEq("mod_audcode", "U")) {
    		wp.colSet("temp_desc", "待覆核處理指示: 修改");
    	}	else if(wp.colEq("mod_audcode", "D")) {
    		wp.colSet("temp_desc", "待覆核處理指示: 刪除");
    	}
    	alertMsg("此筆資料待覆核....");
    }
    
    queryAfter();

  }

  @Override
  public void saveFunc() throws Exception {
    
    Ccam3080Func func = new Ccam3080Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      alertErr(func.getMsg());
    } else {
    	if(wp.itemEq("temp_flag", "Y")) {
    		saveAfter(false);
    		alertMsg("異動已寫入待覆核資料");
    		return ;
    	}	else	{
    		if("D".equals(strAction)) {
    			clearFunc();
    			alertMsg("待覆核資料已刪除");
    			return ;
    		} else if("A".equals(strAction)) {    			
    			saveAfter(false);
    			alertMsg("新增資料已寫入待覆核資料");
    			return ;
    		} else if("U".equals(strAction)) {
    			saveAfter(false);
    			alertMsg("待覆核資料已異動");
    			return ;
    		}
    	}
    }
      


  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    wp.initOption = "--";
    if (wp.itemEmpty("ex_acct_type")) {
      wp.optionKey = wp.colStr("kk_acct_type");
    } else {
      wp.optionKey = wp.itemStr("ex_acct_type");
    }

    try {
      this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name",
          "where acct_type in ('01','03','06') order by acct_type");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void queryAfter() {
	  
	  String sql1 = "select id_no from crd_idno where id_p_seqno = ? ";
	  String sql2 = "select card_no , current_code from crd_card where acno_p_seqno = ? ";	  
	  
    for (int ii = 0; ii < wp.selectCnt; ii++) {
    	
//    	if(wp.colEmpty(ii,"corp_p_seqno") == false) {
//    		sqlSelect(sql2,new Object[] {wp.colStr(ii,"acno_p_seqno")});
//    		if(sqlRowNum > 0 ) {
//    			wp.colSet(ii,"idorcard", sqlStr("card_no"));
//    			wp.colSet(ii,"current_code", sqlStr("current_code"));
//    		}
//    	}	else	{
//    		sqlSelect(sql1,new Object[] {wp.colStr(ii,"idno_p_seqno")});
//    		wp.colSet(ii,"idorcard", sqlStr("id_no"));
//    	}    	    	    	
    	
      // 若with_sup_card不為"Y"(即為"N"或"")，則with_sup_card設為""
      if (!wp.colStr(ii, "with_sup_card").equalsIgnoreCase("Y")) {
        wp.colSet(ii, "with_sup_card", "");
      }

      switch (wp.colStr(ii, "current_code")) {
        case "0":
          wp.colSet(ii, "current_code_desc", "(正常)");
          break;
        case "1":
          wp.colSet(ii, "current_code_desc", "(申停)");
          break;
        case "2":
          wp.colSet(ii, "current_code_desc", "(掛失)");
          break;
        case "3":
          wp.colSet(ii, "current_code_desc", "(強停)");
          break;
        case "4":
          wp.colSet(ii, "current_code_desc", "(其他停用)");
          break;
        case "5":
          wp.colSet(ii, "current_code_desc", "(偽卡)");
          break;
      }                 
    }
  }

  private void queryAfterForPDF() throws Exception {	  	 
	  for (int ii = 0; ii < wp.selectCnt; ii++) {
		  wp.colSet(ii,"tt_acct_type", wp.colStr(ii,"acct_type")+"-"+wp.colStr(ii,"chin_name"));
		  wp.colSet(ii, "tt_idorcard",wp.colStr(ii,"idorcard")+wp.colStr(ii,"current_code_desc"));
		  wp.colSet(ii, "tt_date", commString.strToYmd(wp.colStr(ii,"start_date"))+" ~ "+commString.strToYmd(wp.colStr(ii,"end_date")));
	  }
  }
  
@Override
public void pdfPrint() throws Exception {
	wp.reportId = "ccam3080";		
	wp.pageRows = 9999;
	queryFunc();
	TarokoPDF pdf = new TarokoPDF();
	wp.fileMode = "Y";
	pdf.excelTemplate = "ccam3080.xlsx";
	pdf.pageCount = 30;
	pdf.sheetNo = 0;
	pdf.procesPDFreport(wp);
	pdf = null;	
}

}

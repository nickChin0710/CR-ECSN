/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE     VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/02/22   V1.00.01    machao      Initial                              *
***************************************************************************/
package mktr01;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoExcel;
// ************************************************************************
public class Mktr2120 extends BaseAction implements InfaceExcel {
  private final String PROGNAME = "聯名機構每月推卡統計表112/02/22 V1.00.01";

  // ************************************************************************
  @Override
  public void userAction() throws Exception {
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "XLS")) {/* Excek- */
      strAction = "XLS";
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if(!empty(wp.itemStr("ex_issue_date"))) {
    	wp.whereStr ="WHERE 1=1 "
    	        + sqlCol(wp.itemStr("ex_issue_date"), "a.static_month")
    	        + sqlCol(wp.itemStr("ex_mkt_member"), "d.staff_branch_no");
    }else {
    	wp.whereStr =  "WHERE 1=1 "  + sqlCol(wp.itemStr("ex_issue_date"), "a.static_month");
    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

//    String issueDate = wp.itemStr("ex_issue_date");
    wp.selectSQL = " " +"a.static_month ,"
	    + " b.staff_branch_no ,"
	    + " nvl(d.member_name,'')member_name ,"
	    + " c.id_no,b.id_p_seqno ,"
	    + " b.issue_date ,"
	    + " decode(substr(b.issue_date,1,6),a.static_month,1,0) as issue_cnt ," 
	    + " decode(oppost_date,'',1,0) as oppost_cnt ,"
	    + " decode(current_code,0,1,0) as current_cnt ," 
	    + " decode(activate_flag,'1',0,'2','1') as activate_cnt ,"  
	    + " decode(purchase_amt,0,0,1) as effective_cnt  ,"
	    + " FEEDBACK_AMT ,"
	    + " purchase_amt as DEST_AMT";

    wp.daoTable = "mkt_member_cardlist a "
    		+ "left join crd_card b on a.card_no=b.card_no " 
    		+ " left join crd_idno c on b.id_p_seqno=c.id_p_seqno " 
    		+ "  left join mkt_member d on b.staff_branch_no=d.staff_branch_no ";

    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    int sun = wp.selectCnt;
    for(int ii = 0; ii<sun;ii++) {
    	
    	String issueY = wp.getValue("static_month",ii);
    	String issue1 = issueY.substring(0, 4) +"01";
    	String issue2 = issueY.substring(0, 4) +"12";
    	String staffBranchNo = wp.getValue("staff_branch_no",ii);
    	String DestAmtY = SelectMktMemberCardlist(staffBranchNo,issue1,issue2);
    	wp.setValue("Year_DEST_AMT", DestAmtY, ii);
    	
    	String staff = wp.getValue("staff_branch_no",ii);
    	String name = wp.getValue("member_name",ii);
    	wp.setValue("staff_name", staff+"_"+name, ii);
    }
    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {}

  // ************************************************************************
  public void saveFunc() throws Exception {
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
	 wp.initOption = "--";
       wp.optionKey = wp.itemStr("ex_mkt_member");
       try {
		this.dddwList("dddw_mkt_member", "MKT_MEMBER","trim(STAFF_BRANCH_NO)", "trim(MEMBER_NAME)",
				"where 1=1 order by STAFF_BRANCH_NO");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }

  // ************************************************************************
  private String SelectMktMemberCardlist(String staffBranchNo,String issue1,String issue2) {
	  String lsSql = " select staff_branch_no, sum(purchase_amt) as Year_DEST_AMT from  mkt_member_cardlist  " + 
	  		" where staff_branch_no = ? and static_month >= ? and static_month <= ? group by staff_branch_no";
      sqlSelect(lsSql, new Object[]{staffBranchNo,issue1,issue2});
      
	return sqlStr("Year_DEST_AMT");
}
  // ************************************************************************
  @Override
public void xlsPrint() {
	    try {
	      log("xlsFunction: started--------");
	      wp.reportId = "mktr2120";
	      
	      // ===================================
	      TarokoExcel xlsx = new TarokoExcel();
	      wp.fileMode = "N";
	      xlsx.excelTemplate = "mktr2120.xlsx";
	      wp.pageRows = 99999;
	      queryFunc();
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
  // ************************************************************************
  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub
  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub
  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

} // End of class

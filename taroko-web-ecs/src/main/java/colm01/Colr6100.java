/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/10/19  V1.00.00     Ryan     program initial                           *
*  112/10/24  V1.00.00     Ryan     營業日改系統日                           *
******************************************************************************/

package colm01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Colr6100 extends BaseReport {
  CommString comms = new CommString();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      // dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      // is_action = "R";
      // dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    } 

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_cert_status");
      this.dddwList("dddw_cert_status", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'COLM6100_CERT_STATUS' order by wf_id ");
    } catch (Exception ex) {
    }
  }

  
	private boolean getWhereStr() throws Exception {
		String lsWhere = " where 1=1 ";
	    String exIdCorpNo = wp.itemStr("ex_id_corp_no");
	    String exCertDate1 = wp.itemStr("ex_cert_date1");
	    String exCertDate2 = wp.itemStr("ex_cert_date2");
	    String exCardFlag = wp.itemStr("ex_card_flag");
	    String exCertType = wp.itemStr("ex_cert_type");
	    String exCertStatus = wp.itemStr("ex_cert_status");
	    String exCertEndDate1 = wp.itemStr("ex_cert_end_date1");
	    String exCertEndDate2 = wp.itemStr("ex_cert_end_date2");
	    String exBranch = wp.itemStr("ex_branch");
	    
		if (this.chkStrend(exCertDate1, exCertDate2) == false) {
			alertErr("[時效起算日-起迄]  輸入錯誤");
			return false;
		}
		if (this.chkStrend(exCertEndDate1, exCertEndDate2) == false) {
			alertErr("[時效到期日-起迄]  輸入錯誤");
			return false;
		}
		
	    if (!empty(exIdCorpNo)) {
	        lsWhere += " and  id_corp_no = :exIdCorpNo ";
	        setString("exIdCorpNo", exIdCorpNo);
	    }
	    if (!empty(exCertDate1)) {
	    	lsWhere += " and  cert_date >= :exCertDate1 ";
		    setString("exCertDate1", exCertDate1);
		}
	    if (!empty(exCertDate2)) {
	    	lsWhere += " and  cert_date <= :exCertDate2 ";
		    setString("exCertDate2", exCertDate2);
		}
	    if (toInt(exCardFlag) > 0) {
	    	lsWhere += " and  card_flag = :exCardFlag ";
		    setString("exCardFlag", exCardFlag);
		}
	    if (toInt(exCertType) > 0) {
	    	lsWhere += " and  cert_type = :exCertType ";
		    setString("exCertType", exCertType);
		}
	    if (!empty(exBranch)) {
	    	lsWhere += " and  brunch = :exBranch ";
		    setString("exBranch", exBranch);
		}
	    if(wp.itemEq("ex_excel_type", "1")) {
	    	lsWhere += " and  cert_status = '3' ";
	    } else {
	    	if(!empty(exCertStatus)) {
		    	lsWhere += " and  cert_status = :exCertStatus ";
				setString("exCertStatus", exCertStatus);
			}
	    }
	    if(wp.itemEq("ex_excel_type", "2")) {
	    	lsWhere += " and  left(cert_end_date,6) = left(ecscrdb.uf_month_add(to_char(sysdate,'yyyymmdd'),1),6) ";
	    } else {
	    	if (!empty(exCertEndDate1)) {
		    	lsWhere += " and  cert_end_date >= :exCertEndDate1 ";
			    setString("exCertEndDate1", exCertEndDate1);
			}
		    if (!empty(exCertEndDate2)) {
		    	lsWhere += " and  cert_end_date <= :exCertEndDate2 ";
			    setString("exCertEndDate2", exCertEndDate2);
		    }
	    }
	    wp.whereStr = lsWhere;
	    return true;
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
    
    if(getWhereStr() == false)
    	return;

    wp.selectSQL = " id_corp_p_seqno,id_corp_no,card_flag,to_char(crt_time,'yyyymmdd') as crt_date"
    		+ " ,cert_type,chi_name,court_id,court_name,court_year,court_desc,cert_kind"
    		+ " ,cert_status,cert_date,cert_end_date,mod_user,to_char(mod_time,'yyyymmdd') as mod_date"
    		+ " ,apr_user,apr_date ";

    wp.daoTable = "COL_BAD_CERTINFO";

    wp.whereOrder = "  ";

    if (comms.left(strAction, 3).equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
    
  }

  void listWkdata() throws Exception {
		commPtrSysIdtab("cert_kind", "COLM6100_CERT_KIND");
		commPtrSysIdtab("cert_status", "COLM6100_CERT_STATUS");
		commPtrSysIdtab("court_id", "COLM6100_CERT_COURT");
		commCertType("cert_type");
		commCardFlag("card_flag");
  }

	private void commPtrSysIdtab(String commStr ,String wfType) {
		for(int ii =0;ii<wp.selectCnt;ii++) {
			String sqlCmd = "select wf_desc from ptr_sys_idtab where wf_type = :wf_type and wf_id = :wf_id ";
			setString("wf_type",wfType);
			setString("wf_id",wp.colStr(ii,commStr));
			sqlSelect(sqlCmd);
			if(sqlRowNum > 0) {
				wp.colSet(ii,"comm_" + commStr, wp.colStr(ii,commStr) + "." + sqlStr("wf_desc"));
			}
		}
	}
	
	private void commCertType(String commStr) {
		for(int ii =0;ii<wp.selectCnt;ii++) {
			String commCertType = comms.decode(wp.colStr(ii,commStr), ",1,2",",執行名義,債權憑證");
			wp.colSet(ii,"comm_" + commStr, wp.colStr(ii,commStr) + "." + commCertType);
		}
	}
	
	private void commCardFlag(String commStr) {
		for(int ii =0;ii<wp.selectCnt;ii++) {
			String commCertType = comms.decode(wp.colStr(ii,commStr), ",1,2",",個人,公司");
			wp.colSet(ii,"comm_" + commStr, wp.colStr(ii,commStr) + "." + commCertType);
		}
	}

  @Override
  public void querySelect() throws Exception {

  }

  void xlsPrint() throws Exception {
    try {
    	
      String mProgName = "colr6100";

      if(wp.itemEq("ex_excel_type", "1")) {
    	  wp.colSet("table_name", "憑證已到期報表");
      }
      if(wp.itemEq("ex_excel_type", "2")) {
    	  wp.colSet("table_name", "憑證下月到期報表");
      }
      log("xlsFunction: started--------");
      wp.reportId = mProgName;

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";

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

  void pdfPrint() throws Exception {
//    wp.reportId = mProgName;
//    // -cond-
////    String cond1 = "資料轉入日期: " + commString.strToYmd(wp.itemStr("exFileDateS")) + " -- "
////        + commString.strToYmd(wp.itemStr("exFileDateE"));
////    wp.colSet("cond_1", cond1);
//    wp.colSet("loginUser", wp.loginUser);
//    wp.pageRows = 9999;
//    queryFunc();
//    // wp.setListCount(1);
//
//    TarokoPDF pdf = new TarokoPDF();
//    wp.fileMode = "N";
//    pdf.excelTemplate = mProgName + ".xlsx";
//    pdf.sheetNo = 0;
//    pdf.procesPDFreport(wp);
//    pdf = null;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}

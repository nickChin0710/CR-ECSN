/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/15  V1.00.00    phopho     program initial                          *
*  108/12/03  V1.00.01    phopho     remove deduct_proc_date & id_no          *
*  109-04-21  V1.00.02    yanghan    修改了變量名稱和方法名稱*
*  109-12-23  V1.00.03    Justin     chg commString and add a comment of the function
*  109-01-04  V1.00.04    shiyuqi    修改无意义命名                           *
*  111-04-01  V1.00.05    Justin     增加Excel功能及回覆理由碼mapping代碼     *
*  111-04-06  V1.00.06    Justin     修改回覆理由碼畫面呈現                   *
*  111-12-12  V1.00.07    JeffKung   XLS列印除了列印一天的扣款失敗,其餘皆限制筆數
******************************************************************************/

package dbar01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbar0010 extends BaseReport {
  CommString commString = new CommString();
  String m_progName = "dbar0010";
  String messageBox = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -EXCEL-
      strAction = "XLS";
      xlsxPrint();
    }

    // dddw_select();
    initButton();
  }

  @Override
  public void initPage() {
    if (empty(strAction)) {
      wp.colSet("exCrDateE", wp.sysDate);
    }
  }

  private boolean getWhereStr() throws Exception {
    // String ls_date1 = wp.item_ss("exDeductProcDateS");
    // String ls_date2 = wp.item_ss("exDeductProcDateE");
    String exCrDateS = wp.itemStr("exCrDateS");
    String exCrDateE = wp.itemStr("exCrDateE");

    // if (empty(wp.item_ss("exDeductProcDateS")) && empty(wp.item_ss("exCrDateS"))
    // && empty(wp.item_ss("exIdNo")) && empty(wp.item_ss("exAccno"))) {
    // err_alert("至少輸入一欄位條件!");
    // return false;
    // }
    // if (!empty(wp.item_ss("exIdNo")) && !empty(wp.item_ss("exAccno"))) {
    // err_alert("[身分證字號]、[金融卡帳號]，請擇一輸入!");
    // return false;
    // }
    // if (this.chk_strend(ls_date1, ls_date2) == false) {
    // err_alert("[扣款處理日期-起迄] 輸入錯誤");
    // return false;
    // }

    if (empty(wp.itemStr("exCrDateS")) && empty(wp.itemStr("exAccno"))) {
      alertErr2("至少輸入一欄位條件!");
      return false;
    }
    if (this.chkStrend(exCrDateS, exCrDateE) == false) {
      alertErr2("[扣款日期-起迄]  輸入錯誤");
      return false;
    }

    // String accIdNo="";
    String accIdPSeqno = "";
    if (empty(wp.itemStr("exAccno")) == false) {
      // accIdNo = wf_getIdno(wp.item_ss("exAccno"));
      accIdPSeqno = wfGetIdno(wp.itemStr("exAccno"));
      // if (accIdNo.equals("")) {
      if (accIdPSeqno.equals("")) {
        alertErr2("[金融卡帳號]  輸入錯誤，查無卡人帳號");
        return false;
      } else {
        // if (f_auth_query_vd(accIdNo,m_progName)==false) {
        if (authQueryVd(accIdPSeqno, m_progName) == false) {
          // err_alert("執行查詢權限檢查錯誤!!");
          alertErr2(messageBox);
          return false;
        }
      }
    }
    String idnoPSeqnoStrList = "";
    if (empty(wp.itemStr("exIdNo")) == false) {
      idnoPSeqnoStrList = wfGetIdPSeqno(wp.itemStr("exIdNo"));
      if (idnoPSeqnoStrList.equals("")) {
        alertErr2("查無資料");
        return false;
      }
    }

    wp.whereStr = "where 1=1 and (A.id_p_seqno = B.id_p_seqno) ";

    if (empty(wp.itemStr("exDeductProcDateS")) == false) {
      wp.whereStr += " and A.deduct_proc_date >= :deduct_proc_dates ";
      setString("deduct_proc_dates", wp.itemStr("exDeductProcDateS"));
    }
    if (empty(wp.itemStr("exDeductProcDateE")) == false) {
      wp.whereStr += " and A.deduct_proc_date <= :deduct_proc_datee ";
      setString("deduct_proc_datee", wp.itemStr("exDeductProcDateE"));
    }
    if (empty(wp.itemStr("exCrDateS")) == false) {
      wp.whereStr += " and A.deduct_date >= :deduct_dates ";
      setString("deduct_dates", wp.itemStr("exCrDateS"));
    }
    if (empty(wp.itemStr("exCrDateE")) == false) {
      wp.whereStr += " and A.deduct_date <= :deduct_datee ";
      setString("deduct_datee", wp.itemStr("exCrDateE"));
    }
    if (empty(wp.itemStr("exAccno")) == false) {
      wp.whereStr += " and A.acct_no = :acct_no ";
      setString("acct_no", wp.itemStr("exAccno"));
    }
    if (empty(wp.itemStr("exDeductType")) == false) {
      wp.whereStr += " and A.deduct_proc_type = :deduct_proc_type ";
      setString("deduct_proc_type", wp.itemStr("exDeductType"));
    }
    if (empty(wp.itemStr("exIdNo")) == false) {
      // wp.whereStr += " and A.id_p_seqno in ("+ idnoPSeqno +") ";
      StringBuffer sb = new StringBuffer();
      sb.append("and A.id_p_seqno in (");
      sb.append(idnoPSeqnoStrList);
      sb.append(")");
      wp.whereStr += sb.toString();

    }

    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  String wfGetIdno(String acctNo) throws Exception {
    String rtn = "";
    // dbc.acct_no, dbc.id_p_seqno, dbc.corp_p_seqno, crd.id_p_seqno, crd.id_no
    // String ls_sql = "select d.id_no from dbc_card a "
    String lsSql = "select d.id_p_seqno from dbc_card a "
        + "LEFT JOIN dbc_idno d ON a.id_p_seqno = d.id_p_seqno "
        + "where a.acct_no = :acct_no fetch first 1 row only ";
    setString("acct_no", acctNo);

    sqlSelect(lsSql);
    if (sqlRowNum != 0) {
      // rtn=sql_ss("id_no");
      rtn = sqlStr("id_p_seqno");
    }
    return rtn;
  }

  /**
   * get a string that composes of a series of id_p_seqno, and each id_p_seqno is separated by a comma 
   * the return string like " '1234', '2345', '1245' "
   * @param idNo
   * @return
   * @throws Exception
   */
  String wfGetIdPSeqno(String idNo) throws Exception {
    String rtn = "";
    // dbc.acct_no, dbc.id_p_seqno, dbc.corp_p_seqno, crd.id_p_seqno, crd.id_no
    String lsSql = "select d.id_p_seqno from dbc_card a "
        + "LEFT JOIN dbc_idno d ON a.id_p_seqno = d.id_p_seqno " + "where d.id_no = :id_no ";
    setString("id_no", idNo);

    sqlSelect(lsSql);
    if (sqlRowNum != 0) {
      for (int i = 0; i < sqlRowNum; i++) {
        rtn += ",'" + sqlStr(i, "id_p_seqno") + "'";
      }
      rtn = rtn.substring(1);
    }
    return rtn;
  }


  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "A.deduct_date, " + "A.deduct_seq, " + "A.reference_no, " + "A.p_seqno, "
        + "A.acct_type, " + "A.id_p_seqno, " + "A.id_no, " + "A.id_no_code, " + "A.card_no, "
        + "A.acct_no, "+ "A.item_post_date, " 
//        + "A.deduct_proc_code, " 
//        + "decode(deduct_proc_type,'01','扣款成功','10','扣款失敗','02','部份扣款','') || '(' || nvl(C.WF_DESC, '') || ')' as tt_deduct_proc_code, "
        + "A.deduct_proc_code || '(' || nvl(C.WF_DESC, '') || ')' as tt_deduct_proc_code, "
        + "A.deduct_proc_date, "
        + "A.deduct_proc_type, " + "A.org_deduct_amt, " + "A.deduct_amt, " + "B.chi_name, "
        + "A.deduct_proc_time, " + "A.beg_bal, " + "A.acct_code " ;

    wp.daoTable = "dba_deduct_txn A, dbc_idno B LEFT JOIN PTR_SYS_IDTAB C  ON A.deduct_proc_code = C.WF_ID AND C.WF_TYPE = 'DBAR0010' ";

    wp.whereOrder = "order by A.deduct_date, A.deduct_seq ";

    //扣款失敗(10),且扣款日期的起迄日相同才能全部列印,否則只能列印999筆
    if (strAction.equals("XLS") && "10".equals(wp.itemStr("exDeductType")) && wp.itemStr("exCrDateS").equals(wp.itemStr("exCrDateE")) ) {
      selectNoLimit();
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    list_wkdata();
    wp.setPageValue();
  }

  void list_wkdata() throws Exception {
    String procCode = "";
    int subBegBal = 0, subDeductAmt = 0, subOrgDeductAmt = 0;
//    String[] cde = new String[] {"00", "01", "02", "03", "04", "05", "06", "07", "98", "99"};
//    String[] txt = new String[] {"扣帳成功", "存款不足", "非委託代繳代發戶", "終止委託代繳代發戶", "存戶查核資料錯誤", "無此帳戶",
//        "帳戶結清銷戶", "存款遭強制執行無法代繳", "其他", "扣款中"};

    int recordCnt = wp.selectCnt;
    for (int ii = 0; ii < recordCnt; ii++) {
//      procCode = wp.colStr(ii, "deduct_proc_code");
//      wp.colSet(ii, "tt_deduct_proc_code", commString.decode(procCode, cde, txt));

      subBegBal += wp.colNum(ii, "beg_bal");
      subDeductAmt += wp.colNum(ii, "deduct_amt");
      subOrgDeductAmt += wp.colNum(ii, "org_deduct_amt");
    }
    wp.colSet("sum_tolrow", intToStr(recordCnt));
    wp.colSet("sum_beg_bal", intToStr(subBegBal));
    wp.colSet("sum_deduct_amt", intToStr(subDeductAmt));
    wp.colSet("sum_org_deduct_amt", intToStr(subOrgDeductAmt));
  }

  @Override
  public void querySelect() throws Exception {

  }

  void xlsxPrint() throws Exception {
    if (getWhereStr() == false) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.reportId = m_progName;
    wp.pageRows = 999;

    String cond1 = "扣款日期: " + commString.strToYmd(wp.itemStr("exCrDateS")) + " -- "
        + commString.strToYmd(wp.itemStr("exCrDateE"));
    wp.colSet("cond_1", cond1);
    // ss = "帳號: " + wp.item_ss("exAccno")
    // + " 扣款結果: " + commString.decode(wp.item_ss("exDeductType"), ",01,10,02", ",扣款成功,扣款失敗,部份扣款");
    cond1 = "扣款結果: " + commString.decode(wp.itemStr("exDeductType"), ",01,10,02", ",扣款成功,扣款失敗,部份扣款");
    wp.colSet("cond_2", cond1);
    wp.colSet("reportName", m_progName.toUpperCase());
    wp.colSet("loginUser", wp.loginUser);
    queryFunc();

    
    TarokoExcel xlsx = new TarokoExcel();
    wp.fileMode = "N";
	xlsx.excelTemplate = m_progName + ".xlsx";
	xlsx.sheetName[0] ="明細";
	wp.setListCount(1);
	xlsx.processExcelSheet(wp);
	xlsx.outputExcel();
	xlsx = null;
	log("xlsFunction: ended-------------");
  }
  
  void pdfPrint() throws Exception {
	    if (getWhereStr() == false) {
	      wp.respHtml = "TarokoErrorPDF";
	      return;
	    }
	    wp.reportId = m_progName;
	    wp.pageRows = 999;

	    String cond1 = "扣款日期: " + commString.strToYmd(wp.itemStr("exCrDateS")) + " -- "
	        + commString.strToYmd(wp.itemStr("exCrDateE"));
	    wp.colSet("cond_1", cond1);
	    // ss = "帳號: " + wp.item_ss("exAccno")
	    // + " 扣款結果: " + commString.decode(wp.item_ss("exDeductType"), ",01,10,02", ",扣款成功,扣款失敗,部份扣款");
	    cond1 = "扣款結果: " + commString.decode(wp.itemStr("exDeductType"), ",01,10,02", ",扣款成功,扣款失敗,部份扣款");
	    wp.colSet("cond_2", cond1);
	    wp.colSet("reportName", m_progName.toUpperCase());
	    wp.colSet("loginUser", wp.loginUser);
	    queryFunc();

	    TarokoPDF pdf = new TarokoPDF();
	    wp.fileMode = "N";
	    pdf.excelTemplate = m_progName + ".xlsx";
	    pdf.sheetNo = 0;
	    pdf.pageCount = 30;
	    // pdf.pageVert= true; //直印
	    pdf.procesPDFreport(wp);
	    pdf = null;
	  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  boolean authQueryVd(String asKey, String asWinId) throws Exception {
    String winId = "", idno = "", userId = "", idpSeqno = "";
    long count;
    double lmAmtParm, lmAmt;

    winId = asWinId.trim();
    if (empty(winId)) {
      messageBox = "資料查詢權限: 程式代碼 不可空白";
      return false;
    }

    count = 0;
    userId = wp.loginUser;

    // -win_id-
    String ls_sql = "select count(*) cnt from col_qry_data_auth "
        + "where user_id = :ls_user_id and win_id =:ls_win_id and decode(run_flag,'','N',run_flag)='Y' ";
    setString("ls_user_id", userId);
    setString("ls_win_id", winId);

    sqlSelect(ls_sql);
    if (wp.errSql.equals("Y")) {
      messageBox = "資料查詢權限: select COL_QRY_DATA_AUTH error; win_id=" + winId;
      return false;
    }
    count = (long) sqlNum("cnt");
    // if (ll_cnt==0) return true;

    // -欠款條件-
    lmAmtParm = 0;
    ls_sql = "select vd_end_bal from ptr_comm_data " + "where parm_code='COLM0910' and seq_no =1 ";

    sqlSelect(ls_sql);
    if (wp.errSql.equals("Y")) {
      messageBox = "資料查詢權限: select PTR_COMM_DATA error";
      return false;
    }
    lmAmtParm = (long) sqlNum("vd_end_bal");
    if (lmAmtParm <= 0)
      return true;

    // -ls_idno-
    if (empty(asKey.trim())) {
      messageBox = "資料查詢權限: 卡號 or 身分證ID 不可空白";
      return false;
    }
    // ls_key = as_key.trim(); //傳入參數為id_no, 非card_no
    // if (ls_key.length()>=14){
    // ls_sql = "select id_no from dbc_card "
    // + "where card_no like :card_no fetch first 1 row only ";
    // setString("card_no", ls_key);
    //
    // sqlSelect(ls_sql);
    // if (sql_nrow != 0) {
    // ls_idno=sql_ss("id_no");
    // }
    // if (empty(ls_idno)) {
    // messageBox = "資料查詢權限: 查核資料不是 VD 卡號 or 身分證ID";
    // return false;
    // }
    // }
    // ls_idno = as_key.trim();
    idpSeqno = asKey.trim();

    lmAmt = 0;
    // ls_sql = "select nvl(sum(end_bal),0) end_bal from dba_debt "
    // + "where p_seqno = UF_IDNO_PSEQNO(:id_no) ";
    // setString("id_no", ls_idno);
    ls_sql =
        "select nvl(sum(end_bal),0) end_bal from dba_debt " + "where id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", idpSeqno);

    sqlSelect(ls_sql);
    if (wp.errSql.equals("Y")) {
      messageBox = "資料查詢權限: select DBC_DEBT error; KEY=" + asKey;
      return false;
    }
    lmAmt = (long) sqlNum("end_bal");
    if (lmAmt >= lmAmtParm)
      return true;

    messageBox = "資料查詢權限: 卡友欠款未達 [參數金額] 不可查詢; KEY=" + asKey;
    return false;
  }

}

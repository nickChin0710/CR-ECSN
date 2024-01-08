package cmsm02;
/** cmsm6020	案件移送入口畫面
 * 2022-1102   Sunny  queryRead fixed  
 * 2020-0727   JustinWu change CMS_PROC_DEPT into PTR_DEPT_CODE ,and remove insertLog
 * 2020-0723   JustinWu add 退回數, case_conf_flag in querySelect2
 * 2020-0720   JustinWu remove useless code
 * 2020-0720   Sunny remove querySelect3,CmsChgAddr,deleteCmsChgAddr
 * 2019-1219:  Alex  keep opt , add id_code
 * 2019-1206:  Alex  fix case_trace
 * 2019-1126:  Alex  queryRead fixed 
 * 2019-1122:  Alex  帶出附卡人、corp line_of_credit_amt
 * 2019-1121   JH    AJAX redefine
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * 2018-0226	Alex	vall card 改為　crd_card union dbc_card
 * V00.0		JH		2017-0816: initial
 * 109-04-27  shiyuqi       updated for project coding standard     *  
 * 109-07-17   shiyuqi        rename tableName &FiledName   
 * 109-07-17    yanghan       修改了字段名称            *
  * 109-12-30  V1.00.01  shiyuqi       修改无意义命名                   
  * 111-11-21  V1.00.01  Machao     頁面bug調整，存儲時間較長問題調整                                                                  *
  * 111-11-21  V1.00.02  sunny      調整身分證ID/卡號檢核長度                                                                  *
  * 111-11-25  V1.00.03  sunny      重新取號，解決第二次儲存重覆鍵值無法寫入的問題                      *
 */


import ofcapp.BaseAction;

public class Cmsm6020 extends BaseAction {
String idCard = "", caseIdno = "", flag = "";
boolean ibDebit = false;

@Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      idCard = wp.itemStr("ex_id_card");
      if (empty(idCard)) {
        alertErr2("請輸入身分證ID/卡號");
        wp.respHtml = "cmsm6020";
        return;
      }
      if ( doesIdnoOrCardnoExist(idCard)) {
    	  alertErr2("本行卡友請使用查詢功能進入維護頁面");
    	  wp.respHtml = "cmsm6020";
    	  return;
      }
      
      clearFunc();
      wp.colSet("case_idno", idCard);
      selectPtrDeptCode();//
      tabClick();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "Q2")) {
      /* 查詢功能 */
      strAction = "Q2";
      queryReadTrace();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 案件存檔 */
      strAction = "A1";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 補寄資料 */
      strAction = "A2";
      saveFunc();
    // @sunny 20200717 取消CmsChgAddr
	// } else if (eqIgno(wp.buttonCode, "U2")) {
    //   updateCmsChgAddr();
    // } else if (eqIgno(wp.buttonCode, "D")) {
    // deleteCmsChgAddr();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 動態查詢 */
      querySelect2();
   // @sunny 20200720 取消S3
   // } else if (eqIgno(wp.buttonCode, "S3")) {
   //   /* 動態查詢 */
   //   querySelect3();
    } else if (eqIgno(wp.buttonCode, "C")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      ajaxFunc();
      return;
    }

    if (wp.respHtml.indexOf("_detl") > 0) {
      wp.colSet("case_date", this.getSysDate());
      wp.colSet("case_user", wp.loginUser);
      zzVipColor(wp.colStr("card_no"));
    } else if (eqIgno(wp.respHtml, "cmsm6020")) {
      zzVipColor(wp.itemStr("ex_id_card"));
    }

  }

  private boolean doesIdnoOrCardnoExist(String idno) throws Exception {
	if ( isIdnoOrCardnoLenghtInvalid(idno))
		return false;
	
	queryRead();
	
	if(wp.notFound.equals("Y"))
		return false;
	else
		return true;
}

@Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.itemStr("case_type");
        this.dddwList("dddw_casetype", "cms_casetype", "case_id", "case_desc",
            " where case_type='1' and apr_flag='Y'");

        wp.optionKey = wp.itemStr("db_case_id_A");
        this.dddwList("dddw_casetype_A", "cms_casetype", "case_type||case_id", "case_desc",
            " where case_type='A' and apr_flag='Y'");
        wp.optionKey = wp.itemStr("db_case_id_B");
        this.dddwList("dddw_casetype_B", "cms_casetype", "case_type||case_id", "case_desc",
            " where case_type='B' and apr_flag='Y'");
        wp.optionKey = wp.itemStr("db_case_id_C");
        this.dddwList("dddw_casetype_C", "cms_casetype", "case_type||case_id", "case_desc",
            " where case_type='C' and apr_flag='Y'");
        wp.optionKey = wp.itemStr("db_case_id_D");
        this.dddwList("dddw_casetype_D", "cms_casetype", "case_type||case_id", "case_desc",
            " where case_type='D' and apr_flag='Y'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (isIdnoOrCardnoLenghtInvalid(wp.itemStr("ex_id_card"))) {
    	 alertErr2("身分證ID/卡號: 不可空白; 且長度須為 10,16");
      return;
    }

    queryRead();
  }

private boolean isIdnoOrCardnoLenghtInvalid(String idno) {
	int len = idno.length();
	return len != 10 && len != 16;
}

  void queryReadTrace() throws Exception {
    String lsUserId = "", lsDate = "";
    lsUserId = wp.loginUser;
    lsDate = getSysDate();

    wp.selectSQL = "" 
        + " case_idno , " 
        + " case_seqno , " 
    	+ " case_trace_date , "
    	+ " case_date as case_date1 , "
        + " case_desc||case_desc2||case_desc3 as case_desc ";
    wp.daoTable = " cms_casemaster ";
    wp.whereStr = " where 1=1 " + sqlCol(lsUserId, "mod_user")
    // + sql_col(ls_date, "case_trace_date", ">=")
    // + sql_col(ls_date, "last_remind_date", "<")
        + " and case_trace_flag = 'Y' ";
    wp.whereOrder = " order by case_seqno ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("無追蹤案件");
      return;
    }

    wp.setListCount(1);

    // --過追蹤日期反紅
    int ilSelectRows = 0;
    ilSelectRows = wp.selectCnt;
    for (int ii = 0; ii < ilSelectRows; ii++) {
      if (wp.colNum(ii, "case_trace_date") < commString.strToNum(lsDate)) {
        wp.colSet(ii, "over_trace", " style='color: rgb(255,0,0)' ");
      }
    }
 
  }

  String getIdpseqno(String idNo) {
    String sql1 = " select id_p_seqno from crd_idno where id_no = ? ";
    if (wp.itemEmpty("ex_id_code") == false) {
      sql1 += " and id_no_code = ?";
      sqlSelect(sql1, new Object[] {idNo, wp.itemStr("ex_id_code")});
    } else {
      sqlSelect(sql1, new Object[] {idNo});
    }
    if (sqlRowNum <= 0)
      return null;

    return sqlStr("id_p_seqno");
  }

  @Override
  public void queryRead() throws Exception {
    String lsWhere1 = "";
    String lsWhere2 = "";
    String lsIdPSeqno = "";
    idCard = wp.itemStr("ex_id_card");

    if (idCard.length() == 10) {
      lsIdPSeqno = getIdpseqno(idCard);
      if (lsIdPSeqno == null || lsIdPSeqno.trim().length() == 0) {
    	  wp.notFound = "Y";
    	  alertMsg("查無卡片資料");
    	  return;
      }
      lsWhere1 += " and (:id_p_seqno1 in (C.major_id_p_seqno) or :id_p_seqno2 in (C.id_p_seqno))";
      setString("id_p_seqno1", lsIdPSeqno);
      setString("id_p_seqno2", lsIdPSeqno);
      lsWhere2 +="and B.id_no = :id_no ";
      lsWhere2 +="and B.id_no_code = :id_no_code ";
      setString("id_no", idCard );
      setString("id_no_code", wp.itemStr("ex_id_code"));
// lsWhere2 += sqlCol(idCard, "B.id_no");
//      lsWhere2 += sqlCol(wp.itemStr("idCard"), "B.proc_result");
    } else if (idCard.length() == 8) {
      lsWhere1 = sqlCol(idCard, "A.acct_key", "like%");
      lsWhere2 = sqlCol(idCard, "A.acct_key", "like%");
    } else {
      lsWhere1 = sqlCol(idCard, "C.card_no");
      lsWhere2 = sqlCol(idCard, "C.card_no");
    }

    wp.sqlCmd = "select decode(C.current_code,'0','0','1') as tt_current_code , " 
        + " A.acct_type ,"
        + " A.acct_key ," 
    	+ " 'N' as debit_flag ,"
        + " decode(C.current_code,'0',C.issue_date,C.oppost_date) ," 
    	+ " C.card_no ,"
        + " C.sup_flag ," 
    	+ " C.oppost_date ," 
        + " C.reissue_date, "
        + " C.p_seqno , "
        + " C.acno_p_seqno ," 
        + " C.corp_p_seqno ," 
        + " C.id_p_seqno ,"
        + " A.acct_status ," 
        + " A.corp_act_flag ," 
        + " A.combo_acct_no as acct_no ,"
        + " B.chi_name ," 
        + " B.birthday, " 
        + " C.card_type, " 
        + " C.group_code, "
        + " (select name from ptr_card_type where card_type = C.card_type) as card_type_name ,"
        + " (select group_name from ptr_group_code where group_code = C.group_code) as group_code_name ,"
        // + " A.bill_sending_zip as bill_zip, "
        // + " A.bill_sending_addr1 as addr_1, "
        // + " A.bill_sending_addr2 as addr_2, "
        // + " A.bill_sending_addr3 as addr_3, "
        // + " A.bill_sending_addr4 as addr_4, "
        // + " A.bill_sending_addr5 as addr_5, "
        + " '' as db_road_card , " 
        + " C.current_code "
        + " from act_acno A, crd_idno B, crd_card C " 
        + " where B.id_p_seqno =C.id_p_seqno "
        + " and A.acno_p_seqno =C.acno_p_seqno " + lsWhere1 
        + " union all "
        + " select decode(C.current_code,'0','0','1') as tt_current_code , " + " A.acct_type ,"
        + " A.acct_key ," + " 'Y' as debit_flag ,"
        + " decode(C.current_code,'0',C.issue_date,C.oppost_date) ," + " C.card_no ,"
        + " C.sup_flag ," + " C.oppost_date ," + " C.reissue_date, "
        + " C.p_seqno ,C.p_seqno as acno_p_seqno," + " C.corp_p_seqno ," + " C.id_p_seqno ,"
        + " A.acct_status ," + " A.corp_act_flag ," + " A.acct_no as acct_no ," + " B.chi_name ,"
        + " B.birthday, " + " C.card_type, " + " C.group_code, "
        + " (select name from ptr_card_type where card_type = C.card_type) as card_type_name ,"
        + " (select group_name from ptr_group_code where group_code = C.group_code) as group_code_name ,"
        // + " A.bill_sending_zip as bill_zip, "
        // + " A.bill_sending_addr1 as addr_1, "
        // + " A.bill_sending_addr2 as addr_2, "
        // + " A.bill_sending_addr3 as addr_3, "
        // + " A.bill_sending_addr4 as addr_4, "
        // + " A.bill_sending_addr5 as addr_5, "
        + " '' as db_road_card , " + " C.current_code "
        + " from dba_acno A, dbc_idno B, dbc_card C " 
        + " where B.id_p_seqno =C.id_p_seqno "
        + " and A.p_seqno =C.p_seqno " + lsWhere2 
        + " order by 1,2,5 Asc ,6 , 7";
    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      wp.notFound = "Y";
      alertMsg("查無卡片資料");
      // wp.col_set("to_page_2", " || 1==1 ");
      return;
    }

    queryAfter();
  }

  void queryAfter() {
    String sql1 = "select rm_carno from cms_roadmaster" + " where card_no =?"
        + " and rm_status<>'0'" + " order by crt_date desc" + commSqlStr.rownum(1);

    for (int ll = 0; ll < wp.selectCnt; ll++) {
      setString2(1, wp.colStr(ll, "card_no"));
      sqlSelect(sql1);
      if (sqlRowNum > 0) {
        wp.colSet(ll, "db_road_car", sqlStr("rm_carno"));
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    idCard = wp.itemStr("data_k1");
    caseIdno = wp.itemStr2("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(idCard)) {
      idCard = wp.itemStr("card_no");
    }
    if (empty(caseIdno)) {
      caseIdno = wp.itemStr2("case_idno");
    }

    if (empty(idCard) && empty(caseIdno)) {
      alertErr2("卡片/身分證字號: 不可同時空白");
      return;
    }


    // --
    if (empty(idCard) == false) {
      wp.sqlCmd = " select " 
          + " 'N' as debit_flag , " 
    	  + " card_no , " 
          + " id_p_seqno , "
          + " p_seqno , "
          + " acno_p_seqno, " 
    	  + " corp_p_seqno , " 
          + " major_id_p_seqno "
          + " from crd_card " 
          + " where 1=1 "
          + " and card_no =?" 
          + " union " 
          + " select "
          + " 'Y' as debit_flag , " 
          + " card_no , " 
          + " id_p_seqno , "
          + " p_seqno, p_seqno as acno_p_seqno, " 
          + " corp_p_seqno , " 
          + " major_id_p_seqno "
          + " from dbc_card " 
          + " where card_no =?";

      setString2(1, idCard);
      setString(idCard);
      pageSelect();
      if (sqlRowNum <= 0) {
        alertErr2("查無持卡人資料: card_no=" + idCard);
        return;
      }
      
      ibDebit = wp.colEq("debit_flag", "Y");
      dataReadAfter();
      selectCmsCaseMaster(wp.colStr("case_idno"));
    } else {
      wp.colSet("case_idno", caseIdno);
      selectCmsCaseMaster(caseIdno);
    }
    
    selectPtrDeptCode();

    selectCaseSeqno();

    logQueryIdno(wp.colStr("debit_flag"), wp.colStr("case_idno"));

    tabClick();
  }

  private void selectCaseSeqno() throws Exception {
		cmsm02.Cmsm6020Func func = new cmsm02.Cmsm6020Func();
		func.setConn(wp);
		
		//重新取號，解決第二次儲存重覆鍵值無法寫入的問題。
		func.getCaseSeqno();	
		
		wp.colSet("case_seqno", wp.itemStr("case_seqno"));
	
  }

void dataReadAfter() {
    String sql1 = "";
    // -正卡資料-
    if (ibDebit) {
      sql1 = "select id_no," + " chi_name," + " home_area_code1," + " home_tel_no1 ,"
          + " home_tel_ext1 , " + " cellar_phone , " + " e_mail_addr " + " from dbc_idno"
          + " where 1=1" + " and id_p_seqno =?";
    } else {
      sql1 = "select id_no," + " chi_name," + " home_area_code1," + " home_tel_no1 ,"
          + " home_tel_ext1 , " + " cellar_phone , " + " e_mail_addr  " + " from crd_idno"
          + " where id_p_seqno =?";
    }
    setString2(1, wp.colStr("major_id_p_seqno"));
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      alertErr2("查無正卡人資料");
      if (wp.itemStr("ex_id_card").length() == 10) {
        wp.colSet("case_idno", wp.itemStr("ex_id_card"));
      }
      return;
    }

    wp.colSet("maj_idno", this.sqlStr("id_no"));
    wp.colSet("maj_chi_name", sqlStr("chi_name"));
    wp.colSet("maj_telno", sqlStr("cellar_phone"));
    // wp.col_set("maj_telno",sql_ss("home_area_code1")+"-"+sql_ss("home_tel_no1")+"-"+sql_ss("home_tel_ext1"));
    wp.colSet("case_idno", sqlStr("id_no"));
    wp.colSet("recv_cname", sqlStr("chi_name"));
    wp.colSet("e_mail_addr", sqlStr("e_mail_addr"));

    // -帳務資料:XXX_ACNO-
    if (ibDebit) {
      sql1 = "select bill_sending_zip as bill_zip, " + "bill_sending_addr1 as bill_addr1 "
          + ", bill_sending_addr2 as bill_addr2" + ", bill_sending_addr3 as bill_addr3"
          + ", bill_sending_addr4 as bill_addr4" + ", bill_sending_addr5 as bill_addr5"
          + ", bill_sending_zip as hh_bill_zip" + ", bill_sending_addr1 as hh_bill_addr1 "
          + ", bill_sending_addr2 as hh_bill_addr2" + ", bill_sending_addr3 as hh_bill_addr3"
          + ", bill_sending_addr4 as hh_bill_addr4" + ", bill_sending_addr5 as hh_bill_addr5"
          + ", '' as payment_no" + ", autopay_acct_no" + ", line_of_credit_amt" + ", stmt_cycle "
          + ", uf_nvl(stat_send_paper,'N') as stat_send_paper "
          + ", uf_nvl(stat_send_internet,'N') as stat_send_internet " + " from dba_acno"
          + " where p_seqno =?" // +sql_col(wp.col_ss("p_seqno"),"p_seqno")
      ;
      setString2(1, wp.colStr("p_seqno"));
    } else {
      sql1 = "select bill_sending_zip as bill_zip, " + "bill_sending_addr1 as bill_addr1 "
          + ", bill_sending_addr2 as bill_addr2" + ", bill_sending_addr3 as bill_addr3"
          + ", bill_sending_addr4 as bill_addr4" + ", bill_sending_addr5 as bill_addr5"
          + ", bill_sending_zip as hh_bill_zip" + ", bill_sending_addr1 as hh_bill_addr1 "
          + ", bill_sending_addr2 as hh_bill_addr2" + ", bill_sending_addr3 as hh_bill_addr3"
          + ", bill_sending_addr4 as hh_bill_addr4" + ", bill_sending_addr5 as hh_bill_addr5"
          + ", payment_no" + ", autopay_acct_no" + ", line_of_credit_amt" + ", stmt_cycle "
          + ", uf_nvl(stat_send_paper,'N') as stat_send_paper "
          + ", uf_nvl(stat_send_internet,'N') as stat_send_internet " + ", auto_installment "
          + ", autopay_acct_bank " + " from act_acno" + " where acno_p_seqno =? "
      // + " and acno_flag<>'Y'" //+sql_col(wp.col_ss("p_seqno"),"p_seqno")
      ;
      setString2(1, wp.colStr("acno_p_seqno"));
    }

    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      wp.colSet("bill_addr", sqlStr("bill_zip") + " " + sqlStr("bill_addr1") + sqlStr("bill_addr2")
          + sqlStr("bill_addr2") + sqlStr("bill_addr4") + sqlStr("bill_addr5"));
      wp.colSet("bill_zip", sqlStr("bill_zip"));
      wp.colSet("bill_addr1", sqlStr("bill_addr1"));
      wp.colSet("bill_addr2", sqlStr("bill_addr2"));
      wp.colSet("bill_addr3", sqlStr("bill_addr3"));
      wp.colSet("bill_addr4", sqlStr("bill_addr4"));
      wp.colSet("bill_addr5", sqlStr("bill_addr5"));
      wp.colSet("hh_bill_zip", sqlStr("hh_bill_zip"));
      wp.colSet("hh_bill_addr1", sqlStr("hh_bill_addr1"));
      wp.colSet("hh_bill_addr2", sqlStr("hh_bill_addr2"));
      wp.colSet("hh_bill_addr3", sqlStr("hh_bill_addr3"));
      wp.colSet("hh_bill_addr4", sqlStr("hh_bill_addr4"));
      wp.colSet("hh_bill_addr5", sqlStr("hh_bill_addr5"));
      wp.colSet("ex_acno_payno", sqlStr("payment_no"));
      wp.colSet("ex_auto_acctno", sqlStr("autopay_acct_no"));
      wp.colSet("ex_credit_limit", sqlStr("line_of_credit_amt"));
      wp.colSet("ex_stmt_cycle", sqlStr("stmt_cycle"));
      wp.colSet("stat_send_paper", sqlStr("stat_send_paper"));
      wp.colSet("stat_send_internet", sqlStr("stat_send_internet"));
      wp.colSet("auto_installment", sqlStr("auto_installment"));
      wp.colSet("autopay_acct_bank", sqlStr("autopay_acct_bank"));
    }

    // -act_acct-
    if (ibDebit == false) {
      sql1 = "select ttl_amt, min_pay" + " from act_acct" + " where p_seqno =?" // +sql_col(wp.col_ss("p_seqno"),"p_seqno")
      ;
      setString2(1, wp.colStr("p_seqno"));
      sqlSelect(sql1);
      if (sqlRowNum > 0) {
        wp.colSet("ex_ttl_amt", sqlStr("ttl_amt"));
        wp.colSet("ex_min_pay", sqlStr("min_pay"));
      }
    }

    // -BONUS-
    if (!ibDebit) {
      sql1 = "select sum(end_tran_bp+res_tran_bp) as ttl_bp" + " from mkt_bonus_dtl" + " where 1=1"
          + sqlCol(wp.colStr("p_seqno"), "p_seqno") + " and bonus_type ='BONU'";
      sqlSelect(sql1);
      if (sqlRowNum > 0) {
        wp.colSet("ex_bonus", sqlInt("ttl_bp"));
      } else
        wp.colSet("ex_bonus", 0);
    }

    // -有效卡數-
    if (ibDebit) {
      sql1 = "select count(*) as db_valid_cards" + " from dbc_card " + " where current_code='0'"
          + " and p_seqno =?";
    } else {
      sql1 = "select count(*) as db_valid_cards" + " from crd_card " + " where current_code='0'"
          + " and p_seqno =?";
    }
    sqlSelect(sql1, new Object[] {wp.colStr("p_seqno")});
    int liCards = sqlInt("db_valid_cards");
    wp.colSet("ex_valid_cards", "" + liCards);

    if (ibDebit == false) {
      // --判斷此卡有無自動分期
      String sql2 = " select auto_installment from crd_card where card_no = ? ";
      sqlSelect(sql2, new Object[] {wp.colStr("card_no")});

      if (sqlRowNum > 0) {
        wp.colSet("auto_installment", sqlStr("auto_installment"));
      }

      // --自動扣繳所屬銀行
      if (wp.colEmpty("autopay_acct_bank") == false) {
        String sql3 = " select bank_name from act_ach_bank where bank_no like ? ";
        sqlSelect(sql3, new Object[] {wp.colStr("autopay_acct_bank") + "%"});
        if (sqlRowNum > 0) {
          wp.colSet("autopay_bank_name", sqlStr("bank_name"));
        }
      }

    }

  }

  void selectCmsCaseMaster(String aIdno) {
    if (empty(aIdno))
      return;

    this.daoTid = "B.";
    wp.sqlCmd = "select" 
        + " ccm.case_date ," 
    	+ " ccm.case_seqno ," 
        + " ccm.card_no ," 
    	+ " ccm.case_type ,"
        + " ccm.case_desc ," 
    	+ " ccm.case_result ," 
        + " ccm.finish_date ," 
    	+ " ccm.case_user ,"
        + " (select su.usr_cname from sec_user as su where su.usr_id = ccm.case_user) as tt_case_user ,"
        + " ccm.send_code ," 
        + " ccm.case_idno ," 
        + " to_char(ccm.mod_time,'hh24miss') as mod_time ,"
        + " ccm.ugcall_flag ," 
        + " ccm.reply_flag , "
        + "( select sum(decode(ccd.case_conf_flag,'R','1','0')) || '/' || count(*) "
        + "  from cms_casedetail as ccd "
        + "  where ccd.case_seqno = ccm.case_seqno "
        + "  group by  ccd.case_seqno ) as returnCnt "
        + " from cms_casemaster as ccm" 
        + " where 1=1"
        + sqlCol(aIdno, "ccm.case_idno") 
        + " order by case_date desc, case_seqno desc" 
        + sqlRownum(10);
    pageQuery();
    wp.setListSernum(1, "B.ser_num");
    selectOK();
  }

  void selectPtrDeptCode() {
    wp.selectSQL = "dept_code as dept_code" + ", dept_name";
    wp.daoTable = "PTR_DEPT_CODE";
    wp.whereStr = " where 1=1";
    wp.whereOrder = " order by 1";

    pageQuery();
    wp.setListSernum(0, "");
    // wp.listCount[0] = wp.selectCnt;

  }

  int dataCheck() {

    String[] aaDept = wp.itemBuff("dept_code");
    String[] aaOpt = wp.itemBuff("opt_dept");
    if (aaDept == null)
      return 0;

    wp.listCount[0] = wp.itemRows("dept_code");
    wp.listCount[1] = wp.itemRows("B.case_date");

    for (int ii = 0; ii < wp.itemRows("dept_code"); ii++) {
      if (checkBoxOptOn(ii, aaOpt) && wp.itemEq("send_code", "Y") == false) {
        alertErr2("請勾選案件移送");
        return -1;
      }
    }

    if (wp.itemEq("send_code", "Y") == false)
      return 1;

    for (int ll = 0; ll < aaDept.length; ll++) {
      if (checkBoxOptOn(ll, aaOpt)) {
        return 1;
      }
    }

    // -no-select-
    alertErr2("請點選 案件移送部門");

    return rc;
  }

  @Override
  public void saveFunc() throws Exception {
    cmsm02.Cmsm6020Func func = new cmsm02.Cmsm6020Func();
    func.setConn(wp);

    if (eqAny(strAction, "A1")) {
      optNumKeep(wp.itemRows("dept_code"), "opt_dept", "on_opt");
      wp.listCount[0] = wp.itemRows("dept_code");
      wp.listCount[1] = wp.itemRows("B.case_date");
      if (dataCheck() == -1) {
        tabClick();
        return;
      }
      rc = func.dbInsert();
      if (rc == 1) {
        // wp.listCount[0] = 0;
        // wp.listCount[1] = 0;
      }
    }
    this.sqlCommit(rc);
    if (rc != 1) {
      tabClick();
      alertErr2(func.getMsg());
    }
    if (eqAny(strAction, "A1") && rc == 1) {
      dataRead();
      initTab2();
      initTab3();
    } else
      this.saveAfter(false);
  }

  void initTab2() throws Exception  {
    wp.itemSet("case_type", "");
    wp.itemSet("case_desc", "");
    wp.colSet("case_desc", "");
    wp.itemSet("case_desc2", "");
    wp.colSet("case_desc2", "");
    wp.itemSet("ugcall_flag", "");
    wp.colSet("ugcall_flag", "");
    wp.itemSet("reply_flag", "");
    wp.colSet("reply_flag", "");
    wp.itemSet("eta_date", "");
    wp.colSet("eta_date", "");
    wp.itemSet("reply_phone", "");
    wp.colSet("reply_phone", "");
    wp.itemSet("send_code", "");
    wp.colSet("send_code", "");
    wp.itemSet("case_trace_flag", "");
    wp.colSet("case_trace_flag", "");
    wp.itemSet("case_trace_date", "");
    wp.colSet("case_trace_date", "");
  }

  void initTab3() throws Exception  {
    wp.itemSet("db_case_id_A", "");
    wp.itemSet("db_case_id_B", "");
    wp.itemSet("db_case_id_C", "");
    wp.itemSet("db_case_id_D", "");
    wp.itemSet("mail_desc", "");
    wp.itemSet("mail_desc2", "");
    wp.colSet("mail_desc", "");
    wp.colSet("mail_desc2", "");
  }

  @Override
  public void procFunc() throws Exception {
    int ilOk = 0, ilErr = 0, ilCnt = 0;
    cmsm02.Cmsm6020Func func = new cmsm02.Cmsm6020Func();
    func.setConn(wp);

    String[] aaOpt = wp.itemBuff("opt");
    String[] lsCaseSeqno = wp.itemBuff("case_seqno");

    wp.listCount[0] = wp.itemRows("case_seqno");

    for (int ii = 0; ii < wp.itemRows("case_seqno"); ii++) {
      if (checkBoxOptOn(ii, aaOpt) == false)
        continue;
      ilCnt++;
      func.varsSet("case_seqno", lsCaseSeqno[ii]);
      if (func.dataProc() == 1) {
        ilOk++;
        wp.colSet(ii, "ok_flag", "V");
        sqlCommit(1);
        continue;
      } else {
        ilErr++;
        wp.colSet(ii, "ok_flag", "X");
        dbRollback();
        continue;
      }
    }

    if (ilCnt == 0) {
      alertErr2("請勾選完成追蹤項目");
      return;
    }

    alertMsg("成功:" + ilOk + " 筆 , 失敗:" + ilErr + " 筆");


  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

  @Override
  public void initPage() {
    wp.colSet("ex_id_code", "0");
    countTrace();
  }

  public void querySelect2() {
    idCard = wp.itemStr("data_k1");
    caseIdno = wp.itemStr("data_k2");

    wp.selectSQL = "case_date," + " case_seqno , " + " case_user," + " ugcall_flag," + " case_idno,"
        + " card_no," + " case_result," + " finish_date," + " send_code," + " case_type,"
        + " case_trace_date," + " reply_flag," + " case_desc," + " case_desc2 , "
        + " decode(case_result,'0','未處理','5','處理中','9','處理完成') as tt_case_result , "
        + " reply_flag , " + " eta_date , " + " reply_phone ";
    wp.daoTable = "cms_casemaster";
    wp.whereStr = " where 1=1" + sqlCol(idCard, "case_date") + sqlCol(caseIdno, "case_seqno");
    pageSelect();
    if (sqlRowNum <= 0) {
      alertErr("查無案件處理資料, key=" + idCard);
      return;
    }
    // --
    if (wp.colEq("case_type", "99999")) {
      daoTid = "log_";
      wp.selectSQL = " * ";
      wp.daoTable = " cms_casepost ";
      wp.whereStr = " where 1=1 " 
          + sqlCol(wp.colStr("case_date"), "case_date")
          + sqlCol(wp.colStr("case_seqno"), "case_seqno");

      pageSelect();
      if (sqlRowNum <= 0) {
        selectOK();
      }
    }

    // --
    wp.selectSQL = "proc_deptno," + " proc_result , " + " finish_date as D_finish_date,"
        + " crt_user," + " recall_date," + " proc_id," + " proc_desc," + " proc_desc2, "
        + " 'rgba(255,255,230,0.5)' as tr_bgcolor , "
        + " decode(proc_id,'1','問題案件','2','處理部門') as tt_proc_id, "
        + " case_conf_flag ";
    wp.daoTable = "cms_casedetail";
    wp.whereStr = " where 1=1" + sqlCol(idCard, "case_date") + sqlCol(caseIdno, "case_seqno");

    pageQuery();
    setListCount(1, "", "tr_bgcolor");
    if (this.sqlNotFind()) {
      wp.notFound = "N";
      return;
    }

    querySelect2After();
  }

  void querySelect2After() {

    String sql1 = " select " + " dept_name " 
                          + " from PTR_DEPT_CODE " 
    		              + " where dept_code = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "proc_deptno")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_proc_deptname", sqlStr("dept_name"));
      }
    }

  }


  void selectCrdIdno() {
    wp.sqlCmd = " select " + " id_no ," + " id_p_seqno ," + " chi_name ," + " cellar_phone ,"
        + " office_area_code1 as otel_area1 ," + " office_tel_no1 as otel_no1 ,"
        + " office_tel_ext1 as otel_ext1 ," + " office_area_code2 as otel_area2 ,"
        + " office_tel_no2 as otel_no2 ," + " office_tel_ext2 as otel_ext2 ,"
        + " home_area_code1 as htel_area1 ," + " home_tel_no1 as htel_no1 ,"
        + " home_tel_ext1 as htel_ext1 ," + " home_area_code2 as htel_area2 ,"
        + " home_tel_no2 as htel_no2 ," + " home_tel_ext2 as htel_ext2 ,"
        + " e_mail_addr as email_addr ," + " company_name as comp_name ,"
        + " job_position as job_posi ," + " e_news ," + " accept_mbullet ," + " accept_dm ,"
        + " accept_sms ," + " accept_call_sell ," + " tsc_market_flag ," + " market_agree_base ,"+ " market_agree_act " 
        + " from crd_idno " + " where 1=1 "
        + sqlCol(idCard, "id_no");
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料");
    }
  }

  void selectDbcIdno() {
    wp.sqlCmd = " select " + " id_no ," + " chi_name ," + " cellar_phone ," + " id_p_seqno ,"
        + " office_area_code1 as otel_area1 ," + " office_tel_no1 as otel_no1 ,"
        + " office_tel_ext1 as otel_ext1 ," + " office_area_code2 as otel_area2 ,"
        + " office_tel_no2 as otel_no2 ," + " office_tel_ext2 as otel_ext2 ,"
        + " home_area_code1 as htel_area1 ," + " home_tel_no1 as htel_no1 ,"
        + " home_tel_ext1 as htel_ext1 ," + " home_area_code2 as htel_area2 ,"
        + " home_tel_no2 as htel_no2 ," + " home_tel_ext2 as htel_ext2 ,"
        + " e_mail_addr as email_addr ," + " company_name as comp_name ,"
        + " job_position as job_posi ," + " e_news ," + " accept_mbullet ," + " accept_dm ,"
        + " accept_sms ," + " accept_call_sell " + " from dbc_idno " + " where 1=1 "
        + sqlCol(idCard, "id_no");
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料");
    }
  }

  void selectActAcno() {
    wp.sqlCmd = " select " + " bill_sending_zip as zip_code ,"
        + " bill_sending_addr1 as mail_addr1 ," + " bill_sending_addr2 as mail_addr2 ,"
        + " bill_sending_addr3 as mail_addr3 ," + " bill_sending_addr4 as mail_addr4 ,"
        + " bill_sending_addr5 as mail_addr5 ," + " tel_off_flag ," + " auto_installment "
        + " from act_acno " + " where acno_flag<>'Y' and p_seqno in (select p_seqno from crd_card "
        + " where 1=1 " + sqlCol(caseIdno, "card_no") + " )";
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料");
      return;
    }

    // --判斷此卡有無自動分期
    String sql1 = " select auto_installment from crd_card where card_no = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});

    if (sqlRowNum > 0) {
      wp.colSet("auto_installment", sqlStr("auto_installment"));
    }

  }

  void selectDbaAcno() {
    wp.sqlCmd =
        " select " + " bill_sending_zip as zip_code ," + " bill_sending_addr1 as mail_addr1 ,"
            + " bill_sending_addr2 as mail_addr2 ," + " bill_sending_addr3 as mail_addr3 ,"
            + " bill_sending_addr4 as mail_addr4 ," + " bill_sending_addr5 as mail_addr5  "
            + " from dba_acno " + " where p_seqno in (select p_seqno from dbc_card " + " where 1=1 "
            + sqlCol(caseIdno, "card_no") + " )";
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料");
    }
  }

  void selectTab1() throws Exception {
    wp.pageRows = 999;
    daoTid = "A1_";
    wp.sqlCmd = " select " + " chk_code , " + " chk_desc " + " from cms_addrcheck " + " where 1=1 "
        + " and chk_type = 'A'";
    if (eqIgno(flag, "N")) {
      wp.sqlCmd += " and chk_flag = 'Y' ";
    } else if (eqIgno(flag, "Y")) {
      wp.sqlCmd += " and chk_debit = 'Y' ";
    }
    pageQuery();
    if (this.sqlNotFind()) {
      this.selectOK();
    }
    wp.setListCount(1);
  }

  void selectTab2() throws Exception {
    wp.pageRows = 999;
    daoTid = "A2_";
    wp.sqlCmd = " select " + " chk_code , " + " chk_desc " + " from cms_addrcheck " + " where 1=1 "
        + " and chk_type = 'B'";
    if (eqIgno(flag, "N")) {
      wp.sqlCmd += " and chk_flag = 'Y' ";
    } else if (eqIgno(flag, "Y")) {
      wp.sqlCmd += " and chk_debit = 'Y' ";
    }
    pageQuery();
    if (this.sqlNotFind()) {
      this.selectOK();
    }
    wp.setListCount(2);
  }

  void ajaxFunc() throws Exception {
    String lsCol = wp.itemStr("ax_col");
    if (eqIgno(lsCol, "case_type")) {
      wfAjaxCaseType();
    } else if (eqIgno(lsCol, "zip_code")) {
      wfAjaxZip();
    }
  }


  public void wfAjaxCaseType() throws Exception {
    String sql1 = "select param1, param2, param3, param4, param5 "
        + ", param6, param7, param8, param9, param10 "
        + ", param11, param12, param13, param14, param15 " + " from cms_casetype_msg "
        + " where case_type ='1' and apr_flag='Y'" + sqlCol(wp.itemStr("ax_case_type"), "case_id");
    sqlSelect(sql1);
    // wp.resetJSON();
    if (sqlRowNum <= 0) {
      wp.addJSON("ax_param_flag", "N");
    } else {
      wp.addJSON("ax_param_flag", "Y");
      wp.addJSON("ax_param_1", sqlStr("param1"));
      wp.addJSON("ax_param_2", sqlStr("param2"));
      wp.addJSON("ax_param_3", sqlStr("param3"));
      wp.addJSON("ax_param_4", sqlStr("param4"));
      wp.addJSON("ax_param_5", sqlStr("param5"));
      wp.addJSON("ax_param_6", sqlStr("param6"));
      wp.addJSON("ax_param_7", sqlStr("param7"));
      wp.addJSON("ax_param_8", sqlStr("param8"));
      wp.addJSON("ax_param_9", sqlStr("param9"));
      wp.addJSON("ax_param_10", sqlStr("param10"));
      wp.addJSON("ax_param_11", sqlStr("param11"));
      wp.addJSON("ax_param_12", sqlStr("param12"));
      wp.addJSON("ax_param_13", sqlStr("param13"));
      wp.addJSON("ax_param_14", sqlStr("param14"));
      wp.addJSON("ax_param_15", sqlStr("param15"));
    }
    // -get 移送部門-
    sql1 = "select" + " case_id, send_code, dept_no, dept_no2, dept_no3 , conf_mark "
        + " from cms_casetype" + " where case_type='1' and apr_flag='Y'"
        + " and   case_id =? and send_code='Y'";
    setString2(1, wp.itemStr2("ax_case_type"));
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      wp.addJSON("ax_send_cnt", "0");
      return;
    } else {
      int liSendCnt = 0;
      String deptNo = sqlStr("dept_no");
      if (notEmpty(deptNo)) {
        liSendCnt++;
        wp.addJSON("ax_dept_no", deptNo);
      }
      deptNo = sqlStr("dept_no2");
      if (notEmpty(deptNo)) {
        liSendCnt++;
        wp.addJSON("ax_dept_no", deptNo);
      }
      deptNo = sqlStr("dept_no3");
      if (notEmpty(deptNo)) {
        liSendCnt++;
        wp.addJSON("ax_dept_no", deptNo);
      }
      deptNo = sqlStr("conf_mark");
      if (notEmpty(deptNo)) {
        wp.addJSON("ax_conf_mark", deptNo);
      }
      wp.addJSON("ax_dept_cnt", "" + liSendCnt);
    }
  }

  void countTrace() {
    String lsDate = "", lsUserId = "";
    lsDate = this.getSysDate();
    lsUserId = wp.loginUser;

    String sql1 =
        " select count(*) as db_cnt "
    + " from cms_casemaster "
    + " where mod_user = ? "
    + " and case_trace_flag = 'Y' ";
    
    sqlSelect(sql1, new Object[] {lsUserId});

    if (sqlRowNum > 0) {
      wp.colSet("trace_cnt", sqlStr("db_cnt"));
    }
  }

  void wfAjaxZip() throws Exception {
    selectData(wp.itemStr("ax_zip"));
    if (rc != 1) {
      wp.addJSON("bill_addr1", "");
      wp.addJSON("bill_addr2", "");
      return;
    }
    wp.addJSON("bill_addr1", sqlStr("bill_addr1"));
    wp.addJSON("bill_addr2", sqlStr("bill_addr2"));
  }

  void selectData(String zipCode) {
    String sql1 = " select " + " zip_city as bill_addr1 , " + " zip_town as bill_addr2 "
        + " from ptr_zipcode " + " where zip_code = ?";

    sqlSelect(sql1, new Object[] {zipCode});

    if (sqlRowNum <= 0) {
      alertErr2("郵遞區號輸入錯誤:" + zipCode);
      return;
    }
  }

  void tabClick() {
    String lsClick = "";
    lsClick = wp.itemStr2("tab_click");
    if (eqIgno(lsClick, "1")) {
      wp.colSet("a_click_1", "id='tab_active'");
    } else if (eqIgno(lsClick, "2")) {
      wp.colSet("a_click_2", "id='tab_active'");
    } else if (eqIgno(lsClick, "3")) {
      wp.colSet("a_click_3", "id='tab_active'");
    } else {
      wp.colSet("a_click_3", "id='tab_active'");
    }
  }

}

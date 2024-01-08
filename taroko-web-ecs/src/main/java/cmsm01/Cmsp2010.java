package cmsm01;
/** 
 *  2019-1219:  Alex              ptr_branch -> gen_brn
 *  2019-1211:  Alex              bug fix
 *  2019-1203:  Alex              risk_level ->chinese
 *  2019-0613:  JH                  p_xxx >>acno_pxxx
 *  2019-0418:  JH                  read all data, 不可同一人覆核
 *  109-04-27    shiyuqi         updated for project coding standard     *  
 *  109-08-20:   JustinWu     fix a bug when click next page
 *  109-08-28    JustinWu     wk_ -> wk_autopay_id_and_desc
 *  109-09-01    JustinWu     selectActAcctCurr set notFound=N
 *  109-09-03    JustinWu     add bill_apply_flag_desc
 *  109-12-09    JustinWu      add old_annual_income
 *  109-12-16     JustinWu      add cleanModCol()
 *  109-12-31  V1.00.03   shiyuqi       修改无意义命名 
 *  110-10-05  V1.00.04   JustinWu  add zip2                       
 *  110-10-18  V1.00.05   Ryan      修正debit卡異動經辦查詢異常                                                                      * 
 * */

import busi.func.CmsFunc;
import ecsfunc.DeCodeAct;
import ofcapp.BaseAction;

public class Cmsp2010 extends BaseAction {
  String isKkIdPSeqno = "", isKkPSqno = "", isKkCardNNo = "";
  String lsIdPSeqno = "";String iskkIdno = "";
  boolean debitFlag = false;

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
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R";
      dataReadACNO();
    } else if (eqIgno(wp.buttonCode, "R3")) {
      // -資料讀取-
      strAction = "R";
      dataReadCARD();
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
    }
    // else if (eq_igno(wp.buttonCode, "S1")) {
    // /* 動態查詢 */
    // querySelect_ACNO();
    // }
    // else if (eq_igno(wp.buttonCode, "S2")) {
    // /* 動態查詢 */
    // querySelect_CARD();
    // }
    else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C1")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "C2")) {
      // -資料處理-
      procFuncAcno();
    } else if (eqIgno(wp.buttonCode, "C3")) {
      // -資料處理-
      procFuncCard();
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {

    String lsWhere = " where 1=1 ";
    debitFlag = wp.itemEq("ex_debit_flag", "Y");

    if (!debitFlag) {
      if (wp.itemEmpty("ex_idno") == false) {
        String sql1 = "select id_p_seqno from crd_idno where id_no = ? ";
        sqlSelect(sql1, new Object[] {wp.itemStr2("ex_idno")});
        if (sqlRowNum <= 0) {
          alertErr2("身分證ID:輸入錯誤");
          return;
        }
        lsIdPSeqno = sqlStr("id_p_seqno");
      }

      lsWhere += " and apr_YN='Y' and apr_flag<>'Y' and data_image='2'"
          + sqlCol(wp.itemStr("ex_mod_user"), "crt_user", "like%")
          + sqlCol(wp.itemStr("ex_mod_date1"), "crt_date", ">=")
          + sqlCol(wp.itemStr("ex_mod_date2"), "crt_date", "<=") 
          + sqlCol(lsIdPSeqno, "id_p_seqno");
      // if ( !wp.col_eq("ex_mod_all","Y")) {
      // ls_where +=sql_col(wp.loginUser,"crt_user","<>");
      // }
    } else if (eqIgno(wp.itemStr("ex_debit_flag"), "Y")) {
      if (wp.itemEmpty("ex_idno") == false) {
        String sql1 = "select id_p_seqno from dbc_idno where id_no = ? ";
        sqlSelect(sql1, new Object[] {wp.itemStr2("ex_idno")});
        if (sqlRowNum <= 0) {
          alertErr2("身分證ID:輸入錯誤");
          return;
        }
        lsIdPSeqno = sqlStr("id_p_seqno");
      }
      lsWhere += " and A.id_p_seqno = B.id_p_seqno and A.mod_table ='DBC_IDNO' and A.apr_flag<>'Y'"
          + sqlCol(wp.itemStr("ex_mod_user"), "A.mod_user", "like%")
          + sqlCol(wp.itemStr("ex_mod_date1"), "A.mod_date", ">=")
          + sqlCol(wp.itemStr("ex_mod_date2"), "A.mod_date", "<=")
          + sqlCol(lsIdPSeqno, "A.id_p_seqno");
      // if ( !wp.col_eq("ex_mod_all","Y")) {
      // ls_where +=sql_col(wp.loginUser,"A.mod_user","<>");
      // }
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.pageRows = 999;
    daoTid = "A.";
    if (eqIgno(wp.itemStr("ex_debit_flag"), "N")) {

      wp.selectSQL = " crt_date as mod_date ," + " crt_time as mod_time ," + " 'N' as debit_flag ,"
          + " uf_idno_id(id_p_seqno) as db_idno ," + " hex(rowid) as rowid ," + " chi_name ,"
          + " crt_user as mod_user , " + " id_p_seqno ";
      wp.daoTable = "crd_idno_online";
      wp.whereOrder = " order by 1 Asc , 2 Asc ";

      pageQuery();
      wp.colSet("tl_cnt1", wp.selectCnt);
      wp.setListCount(1);

      if (sqlRowNum <= 0) {
        wp.notFound = "N";
      }
    } else if (eqIgno(wp.itemStr("ex_debit_flag"), "Y")) {
      wp.selectSQL = " A.mod_date as mod_date ," + " A.mod_time2 as mod_time ,"
          + " 'Y' as debit_flag ," + " B.id_no as db_idno ," + " B.id_no_code ," + " B.chi_name ,"
          + " A.mod_user ," + " hex(A.rowid) as rowid ," + " A.id_p_seqno ";
      wp.daoTable = " dbs_modlog_main A, dbc_idno B ";
      wp.whereOrder = " order by 1 Asc , 2 Asc ";
      pageQuery();
      wp.colSet("tl_cnt1", wp.selectCnt);
      wp.setListCount(1);

      if (sqlRowNum <= 0) {
        wp.notFound = "N";
      }
    }

    if (eqIgno(wp.itemStr("ex_debit_flag"), "N")) {
      selectTab2N();
      selectTab3N();
    } else {
      selectTab2Y();
      selectTab3Y();
    }

  }

  void selectTab2N() throws Exception {
    daoTid = "B.";
    wp.sqlCmd = " select " + " A.mod_date ," + " A.mod_time2 as mod_time ,"
        + " 'N' as debit_flag , " + " A.acct_type ," + " A.acct_key ," + " A.mod_user ,"
        + " A.acno_p_seqno , " + " hex(A.rowid) as rowid "
        + " from act_acno_online A left join act_acno B on A.acno_p_seqno = B.acno_p_seqno "
        + " where 1=1 " + " and A.apr_yn = 'Y' " + " and A.apr_flag <>'Y' "
        + " and A.data_image = '2' " + sqlCol(wp.itemStr("ex_mod_user"), "A.mod_user", "like%")
        + sqlCol(wp.itemStr("ex_mod_date1"), "A.mod_date", ">=")
        + sqlCol(wp.itemStr("ex_mod_date2"), "A.mod_date", "<=")
        + sqlCol(lsIdPSeqno, "B.id_p_seqno") + " order by 1 Asc , 2 Asc ";

    pageQuery();
    wp.colSet("tl_cnt2", wp.selectCnt);
    wp.setListCount(2);
    wp.notFound = "N";
  }

  void selectTab2Y() throws Exception {
    daoTid = "B.";
    wp.sqlCmd = " select " + " A.mod_date ," + " A.mod_time2 as mod_time ,"
        + " 'Y' as debit_flag , " + " B.acct_type ," + " B.acct_key ," + " A.mod_user ,"
        + " hex(A.rowid) as rowid ," + " A.id_p_seqno ," + " A.p_seqno as acno_p_seqno, A.p_seqno "
        + " from dbs_modlog_main A, dba_acno B " + " where 1=1 " + " and A.p_seqno = B.p_seqno "
        + " and A.mod_table ='DBA_ACNO' " + " and A.apr_flag<>'Y' "
        + sqlCol(wp.itemStr("ex_mod_user"), "A.mod_user", "like%")
        + sqlCol(wp.itemStr("ex_mod_date1"), "A.mod_date", ">=")
        + sqlCol(wp.itemStr("ex_mod_date2"), "A.mod_date", "<=") + sqlCol(lsIdPSeqno, "B.id_p_seqno")
        + " order by 1 Asc , 2 Asc ";

    pageQuery();
    wp.colSet("tl_cnt2", wp.selectCnt);
    wp.setListCount(2);
    wp.notFound = "N";
  }

  void selectTab3N() throws Exception {
    daoTid = "C.";
    wp.sqlCmd = " select " + " A.mod_date ," + " A.mod_time2 as mod_time ,"
        + " 'N' as debit_flag , " + " A.card_no ," + " A.promote_emp_no ," + " A.mod_user ,"
        + " A.mod_deptno ," + " B.id_no as db_idno ," + " B.chi_name as db_cname "
        + " from crd_card_online A, crd_idno B, crd_card C " + " where 1=1 "
        + " and A.card_no = C.card_no " + " and B.id_p_seqno = C.id_p_seqno "
        + " and A.apr_yn = 'Y' " + " and A.apr_flag <> 'Y' " + " and A.data_image = '2' "
        + sqlCol(wp.itemStr("ex_mod_user"), "A.mod_user", "like%")
        + sqlCol(wp.itemStr("ex_mod_date1"), "A.mod_date", ">=")
        + sqlCol(wp.itemStr("ex_mod_date2"), "A.mod_date", "<=")
        + sqlCol(wp.itemStr2("ex_idno"), "B.id_no") + " order by 1 Asc , 2 Asc ";
    pageQuery();
    wp.colSet("tl_cnt3", wp.selectCnt);
    wp.setListCount(3);
    wp.notFound = "N";
  }

  void selectTab3Y() throws Exception {
    daoTid = "C.";
    wp.sqlCmd = " select " + " A.mod_date ," + " A.mod_time2 as mod_time ,"
        + " 'Y' as debit_flag , " + " A.card_no ," + " A.promote_emp_no ," + " A.mod_user ,"
        + " A.mod_deptno ," + " B.id_no as db_idno ," + " B.chi_name as db_cname "
        + " from dbs_modlog_main A, dbc_idno B, dbc_card C " + " where 1=1 "
        + " and A.card_no = C.card_no " + " and B.id_p_seqno = C.id_p_seqno "
        + " and A.mod_table ='DBC_CARD' " + " and A.apr_flag <> 'Y' "
        + sqlCol(wp.itemStr("ex_mod_user"), "A.mod_user", "like%")
        + sqlCol(wp.itemStr("ex_mod_date1"), "A.mod_date", ">=")
        + sqlCol(wp.itemStr("ex_mod_date2"), "A.mod_date", "<=")
        + sqlCol(wp.itemStr2("ex_idno"), "B.id_no") + " order by 1 Asc , 2 Asc ";
    pageQuery();
    wp.colSet("tl_cnt3", wp.selectCnt);
    wp.setListCount(3);
    wp.notFound = "N";
  }

  @Override
  public void querySelect() throws Exception {}

  @Override
  public void dataRead() throws Exception {
    if (empty(isKkIdPSeqno)) {
      isKkIdPSeqno = wp.itemStr("kks_id_p_seqno");
    }
    if (isKkIdPSeqno.length() < 10) {
      alertMsg("這筆為最後一筆！");
      return;
    }
    if (empty(iskkIdno)) {
    	iskkIdno = wp.itemStr("kks_id_no");
    }
    
    if (wp.respHtml.indexOf("_idno") > 0) {
      String idPSeqno = isKkIdPSeqno.substring(0, 10);
      String idno1 = iskkIdno.substring(0, 10);
      dataReadIdno(idPSeqno);

      cmsm01.Cmsm2010Idno idno = new cmsm01.Cmsm2010Idno();
      idno.setConn(wp);
      String [] arr=new String[] {};
      arr=idno.selectCmsChgColumnLog("crd_idno","cellar_phone",idno1);
      wp.colSet("c_phone_chg_date", arr[0]);
      wp.colSet("c_phone_chg_time", arr[1]);
        
      if (idno.idnoOnline() <= 0) {
        alertErr2("無異動資料");
        wp.colSet("kks_id_p_seqno", isKkIdPSeqno.substring(10));
        wp.colSet("kks_id_no", iskkIdno.substring(10));
        idno.cleanModCol();
        return;
      }
      idno.cmsp2010SetModifyData();
      wp.colSet("kks_id_p_seqno", isKkIdPSeqno.substring(10));
      wp.colSet("kks_id_no", iskkIdno.substring(10));
    } else if (wp.respHtml.indexOf("_dbidno") > 0) {
      debitFlag = true;
      String idPSeqno = isKkIdPSeqno.substring(0, 10);
      String idno1 = iskkIdno.substring(0, 10);
      dataReadDBidno(idPSeqno);
      cmsm01.Cmsm2010Dbidno dbidno = new cmsm01.Cmsm2010Dbidno();
      dbidno.setConn(wp);
      
      if (dbidno.dbIdnoOnline() <= 0) {
        alertErr2("無異動資料");
        dbidno.cleanModCol();
        return;
      }     
      dbidno.cmsp2010SetModifyData();
      wp.colSet("kks_id_p_seqno", isKkIdPSeqno.substring(10));
      wp.colSet("kks_id_no", iskkIdno.substring(10));
      cmsm01.Cmsm2010Idno idno = new cmsm01.Cmsm2010Idno();
      idno.setConn(wp);
      String [] arr=new String[] {};
      arr=idno.selectCmsChgColumnLog("dbc_idno","cellar_phone",idno1);
      wp.colSet("c_phone_chg_date", arr[0]);
      wp.colSet("c_phone_chg_time", arr[1]);
    }

    dataReadAfterIDNO();

  }

  void dataReadAfterIDNO() {
	  CmsFunc cmsFunc = new CmsFunc(wp);
    
	  if ( ! wp.colEmpty("education")) {
		  wp.colSet("tt_education", "." + cmsFunc.getEducationDesc(wp.colStr("education")));
	  }
	  if ( ! wp.colEmpty("business_code")) {
		  wp.colSet("tt_business_code", "." + cmsFunc.getBusinessDesc(wp.colStr("business_code")));
	  }
	  if ( ! wp.colEmpty("marriage")) {
		  wp.colSet("tt_marriage", "." + ecsfunc.DeCodeCms.marriageCode(wp.colStr("marriage")));
	  }
	  if ( ! wp.colEmpty("major_relation")) {
		  wp.colSet("tt_major_relation", "." + ecsfunc.DeCodeCms.majorRelationCode(wp.colStr("major_relation")));
	  }
	  
    if (wp.colEq("nation", "1")) {
      wp.colSet("tt_nation", ".本國");
    } else {
      wp.colSet("tt_nation", ".外國");
    }
    
    if (wp.colEq("spec_busi_code", "01")) {
      wp.colSet("tt_spec_busi_code", "軍火業");
    } else if (wp.colEq("spec_busi_code", "02")) {
      wp.colSet("tt_spec_busi_code", "虛擬貨幣業務");
    } else if (wp.colEq("spec_busi_code", "03")) {
      wp.colSet("tt_spec_busi_code", "空殼公司");
    } else if (wp.colEq("spec_busi_code", "99")) {
      wp.colSet("tt_spec_busi_code", "非以上特殊行業別");
    }

    if (wp.colEq("risk_level", "H")) {
      wp.colSet("tt_risk_level", "高");
    } else if (wp.colEq("risk_level", "M")) {
      wp.colSet("tt_risk_level", "中");
    } else if (wp.colEq("risk_level", "L")) {
      wp.colSet("tt_risk_level", "低");
    }

    if (wp.colEq("market_agree_base", "0")) {
      wp.colSet("tt_market_agree_base", "不同意");
    } else if (wp.colEq("market_agree_base", "1")) {
      wp.colSet("tt_market_agree_base", "同意共銷");
    } else if (wp.colEq("market_agree_base", "2")) {
      wp.colSet("tt_market_agree_base", "同意共享");
    }

    String sql2 = " select full_chi_name from gen_brn where branch = ? ";
    sqlSelect(sql2, new Object[] {wp.colStr("staff_br_no")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_staff_br_no", sqlStr("full_chi_name"));
    } else {
      wp.colSet("tt_staff_br_no", "");
    }

  }

  @Override
  public void saveFunc() throws Exception {
    // --
    if (checkAprUser(0, "mod_user")) {
      alertErr2(" [覆核主管/維護經辦] 不可同一人");
      return;
    }

    if (wp.respHtml.indexOf("_idno") > 0) {

      cmsm01.Cmsm2010Idno func = new cmsm01.Cmsm2010Idno();
      func.setConn(wp);
      rc = func.dataProc();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }

    if (wp.respHtml.indexOf("_dbidno") > 0) {
      cmsm01.Cmsm2010Dbidno func = new cmsm01.Cmsm2010Dbidno();
      func.setConn(wp);
      rc = func.dataProc();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }

    if (wp.respHtml.indexOf("_acno") > 0) {
      cmsm01.Cmsm2010Acno func = new cmsm01.Cmsm2010Acno();
      func.setConn(wp);
      rc = func.dataProc();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }

    if (wp.respHtml.indexOf("_dbacno") > 0) {
      cmsm01.Cmsm2010Dbacno func = new cmsm01.Cmsm2010Dbacno();
      func.setConn(wp);
      rc = func.dataProc();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }

    if (wp.respHtml.indexOf("_card") > 0) {
      cmsm01.Cmsm2010Card func = new cmsm01.Cmsm2010Card();
      func.setConn(wp);
      rc = func.dataProc();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }

    if (rc == 1 && (wp.respHtml.indexOf("_dbidno") > 0 || wp.respHtml.indexOf("_idno") > 0)) {
      dataRead();
    }

    if (rc == 1 && (wp.respHtml.indexOf("_dbacno") > 0 || wp.respHtml.indexOf("_acno") > 0)) {
      dataReadACNO();
    }

    if (rc == 1 && (wp.respHtml.indexOf("_card") > 0)) {
      dataReadCARD();
    }

  }

  @Override
  public void procFunc() throws Exception {
    String[] idNoOpt = wp.itemBuff("opt1");
    String[] iDNOIdPSeqno = wp.itemBuff("A.id_p_seqno");
    String[] iDNOIdno = wp.itemBuff("A.db_idno");
    debitFlag = wp.itemEq("data_k1", "Y"); // wp.item_ss("ex_debit_flag");

    wp.listCount[0] = iDNOIdPSeqno.length;
    log("C:" + iDNOIdPSeqno.length);
    isKkIdPSeqno = "";
    iskkIdno = "";
    for (int rr = 0; rr < iDNOIdPSeqno.length; rr++) {
      // -option-ON-
      if (!checkBoxOptOn(rr, idNoOpt)) {
        continue;
      }
      isKkIdPSeqno += iDNOIdPSeqno[rr];
      
    }
    for (int rr = 0; rr < iDNOIdno.length; rr++) {
        // -option-ON-
        if (!checkBoxOptOn(rr, idNoOpt)) {
          continue;
        }
        iskkIdno += iDNOIdno[rr];
        
    }
    if (empty(isKkIdPSeqno)) {
      alertErr2("請點選覆核資料");
      return;
    }
    dataRead();
    wp.respMesg = "";
  }

  void procFuncAcno() throws Exception {
    String[] acNoOpt = wp.itemBuff("opt2");
    String[] aCNOPSeqno = wp.itemBuff("B.acno_p_seqno");
    debitFlag = wp.itemEq("data_k1", "Y"); // wp.item_ss("ex_debit_flag");

    wp.listCount[1] = aCNOPSeqno.length;

    isKkPSqno = "";
    for (int rr = 0; rr < aCNOPSeqno.length; rr++) {
      // -option-ON-
      if (!checkBoxOptOn(rr, acNoOpt)) {
        continue;
      }
      isKkPSqno += aCNOPSeqno[rr];
    }
    if (empty(isKkPSqno)) {
      alertErr2("請點選覆核資料");
      return;
    }
    dataReadACNO();
  }

  void procFuncCard() throws Exception {
    String[] cardnoOpt = wp.itemBuff("opt3");
    String[] cardNo = wp.itemBuff("C.card_no");
    debitFlag = wp.itemEq("data_k1", "Y"); // wp.item_ss("ex_debit_flag");

    wp.listCount[1] = cardNo.length;

    isKkCardNNo = "";
    for (int rr = 0; rr < cardNo.length; rr++) {
      // -option-ON-
      if (!checkBoxOptOn(rr, cardnoOpt)) {
        continue;
      }

      isKkCardNNo += cardNo[rr] + ",";
    }
    if (empty(isKkCardNNo)) {
      alertErr2("請點選覆核資料");
      return;
    }
    dataReadCARD();
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // 默認選中信用卡
    wp.colSet("DEFAULT_CHK", "checked");

  }
  // ******************************************************************************


  // ******************************************************************************
  void dataReadIdno(String lsIdPSeqno1) {
    wp.sqlCmd = "select *, annual_income as old_annual_income from crd_idno where 1=1" + sqlCol(lsIdPSeqno1, "id_p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + lsIdPSeqno1);
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] residentZipArr = commString.splitZipCode(wp.colStr("resident_zip"));
    wp.colSet("resident_zip", residentZipArr[0]);
    wp.colSet("resident_zip2", residentZipArr[1]);
    String[] mailZipArr = commString.splitZipCode(wp.colStr("mail_zip"));
    wp.colSet("mail_zip", mailZipArr[0]);
    wp.colSet("mail_zip2", mailZipArr[1]);
    String[] companyZipArr = commString.splitZipCode(wp.colStr("company_zip"));
    wp.colSet("company_zip", companyZipArr[0]);
    wp.colSet("company_zip2", companyZipArr[1]);
    
    selectIdnoExt(lsIdPSeqno1);
  }

  void selectIdnoExt(String lsIdPSeqno1) {
    wp.sqlCmd = "select * from crd_idno_ext where 1=1" + sqlCol(lsIdPSeqno1, "id_p_seqno");

    pageSelect();
    wp.notFound = "N";
  }

  void dataReadDBidno(String lsIdPSeqno1) {
    wp.sqlCmd = "select * from dbc_idno where 1=1" + sqlCol(lsIdPSeqno1, "id_p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + lsIdPSeqno1);
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] residentZipArr = commString.splitZipCode(wp.colStr("resident_zip"));
    wp.colSet("resident_zip", residentZipArr[0]);
    wp.colSet("resident_zip2", residentZipArr[1]);
    String[] mailZipArr = commString.splitZipCode(wp.colStr("mail_zip"));
    wp.colSet("mail_zip", mailZipArr[0]);
    wp.colSet("mail_zip2", mailZipArr[1]);
    String[] companyZipArr = commString.splitZipCode(wp.colStr("company_zip"));
    wp.colSet("company_zip", companyZipArr[0]);
    wp.colSet("company_zip2", companyZipArr[1]);

    selectDBidnoExt(lsIdPSeqno1);
    dataReadAfterIDNO();
  }

  void selectDBidnoExt(String lsIdPSeqno1) {
    wp.sqlCmd = "select * from dbc_idno_ext where 1=1" + sqlCol(lsIdPSeqno1, "id_p_seqno");

    pageSelect();
    wp.notFound = "N";
  }

  // ******************************************************************************
  void dataReadACNO() throws Exception {
    if (empty(isKkPSqno)) {
      isKkPSqno = wp.itemStr("kks_p_seqno");
    }
    if (isKkPSqno.length() < 10) {
      alertMsg("這筆為最後一筆！");
      return;
    }

    debitFlag = false;
    if (wp.respHtml.indexOf("_acno") > 0) {
      String psqno = isKkPSqno.substring(0, 10);
      dataReadAcno(psqno);

      cmsm01.Cmsm2010Acno acno = new cmsm01.Cmsm2010Acno();
      acno.setConn(wp);
      if (acno.acnoOnline() <= 0) {
        alertErr2("無異動資料");
        acno.cleanModCol();
        wp.colSet("kks_p_seqno", isKkPSqno.substring(10));
        return;
      }
      acno.cmsp2010SetModifyData();
      selcetDetl(wp.colStr("id_p_seqno"));
      selectActAcctCurr(psqno);
      wp.colSet("kks_p_seqno", isKkPSqno.substring(10));
    } else if (wp.respHtml.indexOf("_dbacno") > 0) {
      debitFlag = true;
      String psqno = isKkPSqno.substring(0, 10);
      dataReadDBacno(psqno);
      cmsm01.Cmsm2010Dbacno dbacno = new cmsm01.Cmsm2010Dbacno();
      dbacno.setConn(wp);
      if (dbacno.dbAcnoOnline() <= 0) {
        alertErr2("無異動資料");
        dbacno.cleanModCol();
        return;
      }
      selectDbDetl(wp.colStr("id_p_seqno"));

      wp.colSet("kks_p_seqno", isKkPSqno.substring(10));
    }
  }

  // ***************************************************************************
  void dataReadAcno(String isPSeqno) throws Exception {
    wp.sqlCmd = "select * from act_acno where 1=1" + sqlCol(isPSeqno, "acno_p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + isPSeqno);
      return;
    }
    
    String[] billSendingZipArr = commString.splitZipCode(wp.colStr("bill_sending_zip"));
    wp.colSet("bill_sending_zip", billSendingZipArr[0]);
    wp.colSet("bill_sending_zip2", billSendingZipArr[1]);
    
    ttdataAcno();
  }

  void selectActAcctCurr(String isPSeqno) throws Exception {

    wp.sqlCmd = "select" + " p_seqno ," + " acct_type ," + " curr_code ," + " autopay_indicator ,"
        + " autopay_acct_bank ," + " autopay_acct_no ," + " autopay_id ," + " autopay_id_code ,"
        + " autopay_dc_flag ," + " no_interest_flag ," + " no_interest_s_month ,"
        + " no_interest_e_month " 
        + " from act_acct_curr" + " where 1=1 "
        + sqlCol(isPSeqno, "p_seqno");

    this.pageQuery();
    
    wp.notFound = "N"; //2020-09-01 JustinWu: 
    
    listWkdataAcno();
    wp.setListCount(1);
  }

  void ttdataAcno() {
    wp.colSet("wk_acctkey", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));    
    
    wp.colSet("bill_apply_flag_desc", ecsfunc.DeCodeCms.billApplyFlagCode(wp.colStr("bill_apply_flag")));
    
    if ( ! wp.colEmpty("acct_status")) {
		wp.colSet("tt_acct_status", "." + DeCodeAct.acctStatus(wp.colStr("acct_status")));
    }

    if (wp.colEq("rc_use_indicator", "1")) {
      wp.colSet("tt_rc_use_indicator", ".正常允用");
    } else if (wp.colEq("rc_use_indicator", "2")) {
      wp.colSet("tt_rc_use_indicator", ".例外允用");
    } else if (wp.colEq("rc_use_indicator", "3")) {
      wp.colSet("tt_rc_use_indicator", ".不準允用 RC");
    }

    if (wp.colEq("rc_use_b_adj", "1")) {
      wp.colSet("tt_rc_use_b_adj", ".正常允用");
    } else if (wp.colEq("rc_use_b_adj", "2")) {
      wp.colSet("tt_rc_use_b_adj", ".例外允用");
    } else if (wp.colEq("rc_use_b_adj", "3")) {
      wp.colSet("tt_rc_use_b_adj", ".不準允用 RC");
    }

    if (wp.colEq("special_stat_code", "1")) {
      wp.colSet("tt_special_stat_code", ".航空");
    } else if (wp.colEq("special_stat_code", "2")) {
      wp.colSet("tt_special_stat_code", ".掛號");
    } else if (wp.colEq("special_stat_code", "3")) {
      wp.colSet("tt_special_stat_code", ".人工處理");
    } else if (wp.colEq("special_stat_code", "4")) {
      wp.colSet("tt_special_stat_code", ".行員");
    } else if (wp.colEq("special_stat_code", "5")) {
      wp.colSet("tt_special_stat_code", ".其他");
    }

    if (wp.colEq("corp_act_flag", "Y")) {
      wp.colSet("tt_corp_act_flag", ".總繳");
    } else if (wp.colEq("corp_act_flag", "N")) {
      wp.colSet("tt_corp_act_flag", ".個繳");
    }

    if (wp.colEq("autopay_indicator", "1")) {
      wp.colSet("tt_autopay_indicator", "扣TTL");
    } else if (wp.colEq("autopay_indicator", "2")) {
      wp.colSet("tt_autopay_indicator", "扣MP");
    }

  }

  void listWkdataAcno() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_autopay_id_and_desc", wp.colEmpty(ii, "autopay_id") ? "" : wp.colStr(ii, "autopay_id") + "-" + wp.colStr(ii, "autopay_id_code"));
      wp.colSet(ii, "wk_no_int_month", wp.colStr(ii, "no_interest_s_month") + "-" + wp.colStr(ii, "no_interest_e_month"));
    }
  }

  void selcetDetl(String isIdPSeqno) {
    String sql1 = "select id_no ," + "id_no_code," + "birthday ," + "sex , " + "chi_name , "
        + "resident_no , " + "other_cntry_code , " + "passport_no , " + "id_p_seqno  "
        + " from crd_idno " + " where id_p_seqno =?";
    sqlSelect(sql1, new Object[] {isIdPSeqno});

    wp.colSet("wk_idno", sqlStr("id_no") + "-" + sqlStr("id_no_code"));
    wp.colSet("birthday", sqlStr("birthday"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("resident_no", sqlStr("resident_no"));
    wp.colSet("other_cntry_code", sqlStr("other_cntry_code"));
    wp.colSet("passport_no", sqlStr("passport_no"));
    wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
  }

  // **************************************************************************
  void dataReadCARD() throws Exception {
    if (empty(isKkCardNNo)) {
      isKkCardNNo = wp.itemStr("kks_card_no");
    }
    if (isKkCardNNo.length() < 16) {
      alertMsg("這筆為最後一筆！");
      return;
    }

    String[] string = new String[2];
    string[0] = isKkCardNNo;
    string = commString.token(string, ",");
    String isCard = string[1];
    dataReadCard(isCard);

    cmsm01.Cmsm2010Card card = new cmsm01.Cmsm2010Card();
    card.setConn(wp);
    if (card.cardOnline() <= 0) {
      alertErr2("無異動資料");
      card.cleanModCol();
      wp.colSet("kks_card_no", string[0]);
      return;
    }
    selcetDetl(wp.colStr("id_p_seqno"));
    setCurrFee();
    wp.colSet("kks_card_no", string[0]);
  }

  void dataReadCard(String isCard) {    
    wp.sqlCmd = "select A.* , " +" B.chi_name as introduce_chi_name, "+ " uf_acno_key(A.acno_p_seqno) as acct_key ,"
        + " A.fee_code as db_fee_code " + " from crd_card A left join CRD_EMPLOYEE B on A.introduce_id= B.id  " + " where 1=1 "
        + sqlCol(isCard, "card_no");

    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料, key=" + isCard);
    }
    wkdataCard();
  }

  void wkdataCard() {
	  if ( ! wp.colEmpty("major_relation")) {
		  wp.colSet("tt_major_relation", "." + ecsfunc.DeCodeCms.majorRelationCode(wp.colStr("major_relation")));
	  }
	  	  
    wp.colSet("wk_acct", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));
    if (wp.colEq("mail_type", "1")) {
      wp.colSet("tt_mail_type", ".普掛");
    } else if (wp.colEq("mail_type", "2")) {
      wp.colSet("tt_mail_type", ".限掛");
    } else if (wp.colEq("mail_type", "3")) {
      wp.colSet("tt_mail_type", ".自取");
    } else if (wp.colEq("mail_type", "4")) {
      wp.colSet("tt_mail_type", ".分行");
    } else if (wp.colEq("mail_type", "N")) {
      wp.colSet("tt_mail_type", ".退件");
    } else if (wp.colEq("mail_type", "Q")) {
      wp.colSet("tt_mail_type", ".其他");
    }

    if (wp.colEq("expire_chg_flag", "1")) {
      wp.colSet("tt_expire_chg_flag", ".系統不續卡");
    } else if (wp.colEq("expire_chg_flag", "2")) {
      wp.colSet("tt_expire_chg_flag", ".提前不續卡 - 客戶來電");
    } else if (wp.colEq("expire_chg_flag", "3")) {
      wp.colSet("tt_expire_chg_flag", ".提前不續卡 - 本行調整");
    }
  }

  void setCurrFee() {
    String dbFeeCode = "";
    String feeCode = "";
    String dbCurrFeecode = "";
    int currFeeCode = 0;
    int liVal = 0;
    dbFeeCode = wp.colStr("db_fee_code");
    feeCode = wp.colStr("fee_code");

    if (commString.isNumber(dbFeeCode) == false || commString.isNumber(feeCode) == false) {
      log("A:" + feeCode);
      wp.colSet("curr_fee_code", feeCode);
    } else {
      if (!eqIgno(dbFeeCode, feeCode)) {

        liVal = (int) (commString.strToNum(feeCode) - commString.strToNum(dbFeeCode));
        dbCurrFeecode = wp.itemStr("curr_fee_code");
        currFeeCode = (int) (commString.strToNum(dbCurrFeecode) + liVal);
        log("B:" + currFeeCode);
        if (currFeeCode <= 0) {
          wp.colSet("curr_fee_code", "0");
        } else {
          wp.colSet("curr_fee_code", "" + currFeeCode);
        }
      }
    }

  }

  // ************
  void dataReadDBacno(String isPSeqno) {
    wp.sqlCmd = "select * from dba_acno " + "where 1=1" + sqlCol(isPSeqno, "p_seqno");

    pageSelect();
    ttdataAcno();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + isPSeqno);
    }
    
    String[] billSendingZipArr = commString.splitZipCode(wp.colStr("bill_sending_zip"));
    wp.colSet("bill_sending_zip", billSendingZipArr[0]);
    wp.colSet("bill_sending_zip2", billSendingZipArr[1]);

  }

  void selectDbDetl(String isIdPSeqno) {
    String sql1 = "select id_p_seqno ," + "id_no," + "id_no_code ," + "chi_name , " + "sex , "
        + "birthday ," + "resident_no , " + "other_cntry_code , " + "passport_no , " + "eng_name  "
        + " from dbc_idno " + " where id_p_seqno =?";
    sqlSelect(sql1, new Object[] {isIdPSeqno});

    wp.colSet("wk_idno", sqlStr("id_no") + "-" + sqlStr("id_no_code"));
    wp.colSet("birthday", sqlStr("birthday"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("resident_no", sqlStr("resident_no"));
    wp.colSet("other_cntry_code", sqlStr("other_cntry_code"));
    wp.colSet("passport_no", sqlStr("passport_no"));
    wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
  }

}

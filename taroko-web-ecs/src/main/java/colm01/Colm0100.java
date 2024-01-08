/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-12-14  V1.00.01  ryan       program initial                            *
* 108/12/31  V1.00.02  phopho     add busi.func.ColFunc.f_auth_query()       *
* 109-05-06  V1.00.03  Aoyulan       updated for project coding standard     *
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
* 112-11-16  V1.00.04   Ryan      588行 增加系統日前一天也可以執行                                                                                      *  
* 112-12-05  V1.00.05   Sunny     588行 原為系統日改為判斷營業日當天及營業日前一天*
*****************************************************************************/
package colm01;

import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.base.CommDate;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;


public class Colm0100 extends BaseProc {
  String mProgName = "colm0100";
  int rr = -1;
  String msg = "", msgok = "", whereStr = "", tableName = "", whereOrder = "";
  //String kk1 = "", kk2 = "";
  int ilOk = 0;
  int ilErr = 0;
  CommString commString = new CommString();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "A")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // 執行批次
      strAction = "C";
      runBatch();
    }


    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("btnPDF", "disabled");
    wp.colSet("btnAdd", "disabled");
    wp.colSet("ex_alw_bad_date", getSysDate());
    wp.colSet("ex_paper_conf_date", getSysDate());
    wp.colSet("ex_paper_name", "存證信函");
    wp.colSet("ex_id_total_amt_s", "0");
    wp.colSet("mcode_s", "00");
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = "存證信函";
      dddwList("PtrCertificateList", "ptr_sys_idtab", "wf_desc", "wf_desc",
          "where wf_type = 'COL_CERTIFICATE' order by wf_id ");
    } catch (Exception e) {
    }
  }

  // for query use only
  private int getWhereStr() throws Exception {

    tableName = "col_bad_trans ";
    String sqlSelect = "";
    if (empty(wp.itemStr("ex_key_id")) && empty(wp.itemStr("ex_stmt_cycle"))
        && empty(wp.itemStr("ex_id_total_amt")) && empty(wp.itemStr("ex_pay_by_stage_flag"))
        && wp.itemNum("ex_id_total_amt_s") == 0 && wp.itemNum("ex_id_total_amt_e") == 0
        && wp.itemNum("mcode_s") == 0 && wp.itemNum("mcode_e") == 0
        && wp.itemStr("ex_apr_flag").equals("0")) {
      alertErr("至少輸入一個查詢條件");
      return -1;
    }
    String data1 = wp.itemStr("ex_id_total_amt_s");
    String data2 = wp.itemStr("ex_id_total_amt_e");

    if (toNum(data1) > toNum(data2)) {
      alertErr2("[ID歸戶欠款總額區間-起迄]  均為必填");
      return -1;
    }

    data1 = wp.itemStr("mcode_s");
    data2 = wp.itemStr("mcode_e");

    if (this.chkStrend(data1, data2) == false) {
      alertErr2("[Mcode(current)區間-起迄]  均為必填");
      return -1;
    }

    if (wp.itemStr("ex_apr_flag").equals("0")) {
      tableName = "col_bad_trans_temp ";
    }

    String wfValue2 = chkPtrSysParm();
    if (wfValue2.equals("N")) {
      alertErr("今日尚未執行批次，請先執行批次");
      return -1;
    }
    if (wfValue2.equals("DOING")) {
      alertErr("批次執行中，請稍後");
      return -1;
    }


    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_key_id")) == false) {
      StringBuffer str = new StringBuffer();
      String param = "";
      int idNum = 0;
      String exKeyId = wp.itemStr("ex_key_id").replaceAll("\n|\r", "");
      String keyId[] = exKeyId.trim().split(",");
      if (keyId.length > 20) {
        alertErr("身分證字號 最多輸入20組");
        return -1;
      }
      if (keyId.length > 0) {
        for (int i = 0; i < keyId.length; i++) {
          // phopho add
          // 2. 進行能否進行此功能之權限檢查，依據是否有輸入【ex_id_no】，
          // 若無輸入【ex_id_no】
          // if f_auth_query(classname(), “”)=false then return -1
          // 或有輸入【ex_id_no】
          // if f_auth_query(classname(), “ex_id_no”)=false then return -1
          busi.func.ColFunc func = new busi.func.ColFunc();
          func.setConn(wp);
          if (func.fAuthQuery(keyId[i]) != 1) {
            alertErr2(func.getMsg());
            return -1;
          }
          // phopho add end
          sqlSelect = "select id_p_seqno from crd_idno where id_no = :id_no ";
          setString("id_no", keyId[i]);
          sqlSelect(sqlSelect);
          if (empty(sqlStr(0, "id_p_seqno"))) {
            idNum++;
            continue;
          }
          param += "'" + sqlStr(0, "id_p_seqno") + "',";
        }
        if (idNum >= keyId.length) {
          alertErr("查無資料");
          return -1;
        }
        param = strMid(param, 0, param.length() - 1);
        str.append(" and id_p_seqno in ( ");
        str.append(param);
        str.append(" ) ");
        wp.whereStr += str.toString();
      }
    }
    if (empty(wp.itemStr("ex_stmt_cycle")) == false) {
      wp.whereStr += " and stmt_cycle = :ex_stmt_cycle ";
      setString("ex_stmt_cycle", wp.itemStr("ex_stmt_cycle"));
    }

    if (empty(wp.itemStr("ex_id_total_amt_s")) == false) {
      wp.whereStr += " and id_total_amt >= :ex_id_total_amt_s ";
      setString("ex_id_total_amt_s", wp.itemStr("ex_id_total_amt_s"));
    }

    if (empty(wp.itemStr("ex_id_total_amt_e")) == false) {
      wp.whereStr += " and id_total_amt <= :ex_id_total_amt_e ";
      setString("ex_id_total_amt_e", wp.itemStr("ex_id_total_amt_e"));
    }

    if (empty(wp.itemStr("mcode_s")) == false) {
      String mcodes = wp.itemStr("mcode_s");
      if (mcodes.equals("00"))
        mcodes = "0";
      wp.whereStr += " and mcode >= :mcode_s ";
      setString("mcode_s", mcodes);
    }

    if (empty(wp.itemStr("mcode_e")) == false) {
      String mcodee = wp.itemStr("mcode_e");
      if (mcodee.equals("00"))
        mcodee = "0";
      wp.whereStr += " and mcode <= :mcode_e ";
      setString("mcode_e", mcodee);
    }

    if (wp.itemStr("ex_pay_by_stage_flag").equals("0")) {
      wp.whereStr += " and pay_by_stage_flag = ''";

    } else if (wp.itemStr("ex_pay_by_stage_flag").equals("1")) {
      wp.whereStr += " and pay_by_stage_flag != ''";
    }

    if (wp.itemStr("ex_orderby").equals("a")) {
      whereOrder += " order by id_total_amt, id_no ";

    } else if (wp.itemStr("ex_orderby").equals("b")) {
      whereOrder += " order by pay_by_stage_flag, id_no ";
    }

    return 1;
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

    wp.selectSQL =
        " p_seqno " + " ,id_p_seqno " + " ,total_amt " + " ,id_total_amt " + " ,acct_type "
            + " ,uf_idno_id(id_p_seqno) as id_no " + " ,uf_idno_name(id_p_seqno) as chi_name "
            + " ,stmt_cycle " + " ,risk_bank_no " + " ,pay_by_stage_flag " + " ,end_bal_cb "
            + " ,end_bal_ci " + " ,end_bal_cc " + " ,line_of_credit_amt " + " ,org_delinquent_date "
            + " ,mcode " + " ,crd_rela_flag " + " ,card_sup_flag " + " ,last_pay_date "
            + " ,trans_date " + " ,nego_type " + " ,nego_status " + " ,apply_nego_mcode "
            + " ,nego_type||'_'||nego_status as nego_type_nego_status " + " ,card_since ";
    if (getWhereStr() != 1) {
      return;
    }
    wp.daoTable = tableName;
    wp.whereOrder = whereOrder;
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // 3. 執行【mainQuery】、SQL查詢，取得資料，顯示於結果列表。
    // 取得之結果，逐筆進行權限檢查。
    // if f_auth_query(classname(), “id_no”)=false then return -1
    if (listAuthQuery() != 1)
      return;

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata(wp.selectCnt);
    if (wp.itemStr("ex_apr_flag").equals("N")) {
      wp.colSet("btnPDF", "");
    }
    wp.colSet("btnAdd", "");
  }

  int listAuthQuery() throws Exception {
    String idNo = "";
    busi.func.ColFunc func = new busi.func.ColFunc();
    func.setConn(wp);

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      idNo = wp.colStr(ii, "id_no");
      if (func.fAuthQuery(idNo) != 1) {
        wp.listCount[0] = 0;
        alertErr2(func.getMsg());
        return -1;
      }
    }
    return 1;
  }

  void listWkdata(int selectCnt) {
    String pSeqno = "", aprFlag = "";
    wp.logSql = false;
    for (int i = 0; i < selectCnt; i++) {
      pSeqno = wp.colStr(i, "p_seqno");
      String sqlSelect = " select apr_flag from col_wait_trans where p_seqno = :p_seqno ";
      setString("p_seqno", pSeqno);
      sqlSelect(sqlSelect);
      aprFlag = sqlStr("apr_flag");
      if (sqlRowNum <= 0) {
        wp.colSet(i, "trans_status", "");
        wp.colSet(i, "table_name", "col_bad_trans_temp");
        wp.colSet(i, "checked_D", "disabled");
        wp.colSet(i, "checked_A", "");
      } else {
        wp.colSet(i, "checked_A", "disabled");
        if (aprFlag.equals("Y")) {
          wp.colSet(i, "trans_status", "已覆核");
          wp.colSet(i, "checked_D", "disabled");
          wp.colSet(i, "table_name", "col_bad_trans");
        } else {
          wp.colSet(i, "trans_status", "待覆核");
          wp.colSet(i, "table_name", "col_bad_trans");
          wp.colSet(i, "checked_D", "");
        }
      }
    }
  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    String kkPSeqno = itemKk("data_k1");
    String tableName = itemKk("data_k2");
    wp.selectSQL = " p_seqno " + " ,id_p_seqno " + " ,total_amt " + " ,id_total_amt "
        + " ,acct_type " + " ,decode(ecscrdb.uf_idno_id(id_p_seqno),'',ecscrdb.uf_corp_no(id_p_seqno),ecscrdb.uf_idno_id(id_p_seqno)) as id_no "
        + " ,uf_idno_name(id_p_seqno) as chi_name " + " ,stmt_cycle " + " ,risk_bank_no "
        + " ,pay_by_stage_flag " + " ,end_bal_cb " + " ,end_bal_ci " + " ,end_bal_cc "
        + " ,line_of_credit_amt " + " ,org_delinquent_date " + " ,mcode " + " ,crd_rela_flag "
        + " ,card_sup_flag " + " ,last_pay_date " + " ,trans_date " + " ,nego_type "
        + " ,nego_status " + " ,apply_nego_mcode " + " ,card_since ";
    if (tableName.equals("col_bad_trans")) {
      wp.selectSQL += " ,paper_name " + " ,alw_bad_date " + " ,paper_conf_date ";
    }
    wp.daoTable = tableName;
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and p_seqno = :kk_p_seqno ";
    setString("kk_p_seqno", kkPSeqno);
    pageSelect();

  }


  @Override
  public void dataProcess() throws Exception {
    busi.SqlPrepare sp = new SqlPrepare();
    int err = 0, ok = 0;
    String sqlDelete = "";
    String[] optA = wp.itemBuff("opt_A");
    String[] optD = wp.itemBuff("opt_D");
    String[] aaAcctType = wp.itemBuff("acct_type");
    String[] aaPSeqno = wp.itemBuff("p_seqno");
    String[] aaChiName = wp.itemBuff("chi_name");
    String[] aaIdPSeqno = wp.itemBuff("id_p_seqno");
    String[] aaStmtCycle = wp.itemBuff("stmt_cycle");
    String[] aaEndBalCb = wp.itemBuff("end_bal_cb");
    String[] aaEndBalCi = wp.itemBuff("end_bal_ci");
    String[] aaEndBalCc = wp.itemBuff("end_bal_cc");
    String[] aaTotalAmt = wp.itemBuff("total_amt");
    String[] aaIdTotalAmt = wp.itemBuff("id_total_amt");
    String[] aaRiskBankNo = wp.itemBuff("risk_bank_no");
    String[] aaPayByStageFlag = wp.itemBuff("pay_by_stage_flag");
    String[] aaLineOfCreditAmt = wp.itemBuff("line_of_credit_amt");
    String[] aaMcode = wp.itemBuff("mcode");
    String[] aaOrgDelinquentDate = wp.itemBuff("org_delinquent_date");
    String[] aaCrdRelaFlag = wp.itemBuff("crd_rela_flag");
    String[] aaCardSupFlag = wp.itemBuff("card_sup_flag");
    String[] aaLastPayDate = wp.itemBuff("last_pay_date");
    String[] aaTransDate = wp.itemBuff("trans_date");
    String[] aaNegoType = wp.itemBuff("nego_type");
    String[] aaNegoStatus = wp.itemBuff("nego_status");
    String[] aaApplyNegoMcode = wp.itemBuff("apply_nego_mcode");
    String[] aaCardSince = wp.itemBuff("card_since");
    wp.listCount[0] = aaPSeqno.length;

    for (int ii = 0; ii < aaPSeqno.length; ii++) {
      // -insert-
      if (checkBoxOptOn(ii, optA)) {
        sp.sql2Insert("col_wait_trans");
        sp.ppstr("p_seqno", aaPSeqno[ii]);
        sp.ppstr("acct_type", aaAcctType[ii]);
        sp.ppstr("chi_name", aaChiName[ii]);
        sp.ppstr("src_acct_stat", "3");
        sp.ppstr("trans_type", "4");
        sp.ppstr("alw_bad_date", wp.itemStr("ex_alw_bad_date"));
        sp.ppstr("paper_conf_date", wp.itemStr("ex_paper_conf_date"));
        sp.ppstr("paper_name", wp.itemStr("ex_paper_name"));
        sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
        sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
        sp.ppstr("crt_user", wp.loginUser);
        sp.ppstr("mod_user", wp.loginUser);
        sp.ppstr("mod_pgm", wp.modPgm());
        sp.addsql(", mod_time ", ", sysdate ");
        sp.ppstr("mod_seqno", "1");
        sp.ppstr("acno_flag", wp.itemStr("acno_flag")); //20221209 add
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum != 1) {
          err++;
          sqlCommit(0);
          wp.colSet(ii, "msg", "insert col_wait_trans err");
          wp.colSet(ii, "ok_flag", "!");
          continue;
        }
        busi.SqlPrepare sp2 = new SqlPrepare();
        sp2.sql2Insert("col_bad_trans");
        sp2.ppstr("p_seqno", aaPSeqno[ii]);
        sp2.ppstr("acct_type", aaAcctType[ii]);
        sp2.ppstr("chi_name", aaChiName[ii]);
        sp2.ppstr("src_acct_stat", "3");
        sp2.ppstr("trans_type", "4");
        sp2.ppstr("alw_bad_date", wp.itemStr("ex_alw_bad_date"));
        sp2.ppstr("paper_conf_date", wp.itemStr("ex_paper_conf_date"));
        sp2.ppstr("paper_name", wp.itemStr("ex_paper_name"));
        sp2.ppstr("id_p_seqno", aaIdPSeqno[ii]);
        sp2.ppstr("stmt_cycle", aaStmtCycle[ii]);
        sp2.ppnum("end_bal_cb", toNum(aaEndBalCb[ii]));
        sp2.ppnum("end_bal_ci", toNum(aaEndBalCi[ii]));
        sp2.ppnum("end_bal_cc", toNum(aaEndBalCc[ii]));
        sp2.ppnum("total_amt", toNum(aaTotalAmt[ii]));
        sp2.ppnum("id_total_amt", toNum(aaIdTotalAmt[ii]));
        sp2.ppnum("line_of_credit_amt", toNum(aaLineOfCreditAmt[ii]));
        sp2.ppstr("risk_bank_no", aaRiskBankNo[ii]);
        sp2.ppstr("pay_by_stage_flag", aaPayByStageFlag[ii]);
        sp2.ppstr("mcode", aaMcode[ii]);
        sp2.ppstr("org_delinquent_date", aaOrgDelinquentDate[ii]);
        sp2.ppstr("crd_rela_flag", aaCrdRelaFlag[ii]);
        sp2.ppstr("card_sup_flag", aaCardSupFlag[ii]);
        sp2.ppstr("last_pay_date", aaLastPayDate[ii]);
        sp2.ppstr("trans_date", aaTransDate[ii]);
        sp2.ppstr("nego_type", aaNegoType[ii]);
        sp2.ppstr("nego_status", aaNegoStatus[ii]);
        sp2.ppstr("apply_nego_mcode", aaApplyNegoMcode[ii]);
        sp2.ppstr("card_since", aaCardSince[ii]);
        sp2.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
        sp2.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
        sp2.ppstr("crt_user", wp.loginUser);
        sp2.ppstr("mod_user", wp.loginUser);
        sp2.ppstr("mod_pgm", wp.modPgm());
        sp2.addsql(", mod_time ", ", sysdate ");
        sp2.ppstr("mod_seqno", "1");
        sqlExec(sp2.sqlStmt(), sp2.sqlParm());
        if (sqlRowNum != 1) {
          err++;
          sqlCommit(0);
          wp.colSet(ii, "msg", "insert col_bad_trans err");
          wp.colSet(ii, "ok_flag", "!");
          continue;
        } else {
          sqlCommit(1);
        }
        ok++;
        wp.colSet(ii, "ok_flag", "V");
      }

      // delete
      if (checkBoxOptOn(ii, optD)) {
        sqlDelete = " delete from col_wait_trans where p_seqno = :p_seqno";
        setString("p_seqno", aaPSeqno[ii]);
        sqlExec(sqlDelete);
        if (sqlRowNum <= 0) {
          err++;
          sqlCommit(0);
          wp.colSet(ii, "msg", "delete col_wait_trans err");
          wp.colSet(ii, "ok_flag", "!");
          continue;
        } else {
          sqlCommit(1);
        }
        String sqlSelect = "select proc_flag from COL_BAD_TRANS where p_seqno = :p_seqno ";
        setString("p_seqno", aaPSeqno[ii]);
        sqlSelect(sqlSelect);
        String procFlag = sqlStr("proc_flag");
        if (!procFlag.equals("Y")) {
          sqlDelete = " delete from col_bad_trans where p_seqno = :p_seqno and proc_flag!='Y' ";
          setString("p_seqno", aaPSeqno[ii]);
          sqlExec(sqlDelete);

          if (sqlRowNum <= 0) {
            err++;
            sqlCommit(0);
            wp.colSet(ii, "msg", "delete col_bad_trans err");
            wp.colSet(ii, "ok_flag", "!");
            continue;
          } else {
            sqlCommit(1);
          }
        }
        ok++;
        wp.colSet(ii, "ok_flag", "V");
      }
    }
    alertMsg("處理完成, 成功=" + ok + "筆, 失敗=" + err + "筆");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    /*
     * String ss = "生效年月: " + commString.ss_2ymd(wp.item_ss("ex_yymm1")) + " -- " +
     * commString.ss_2ymd(wp.item_ss("ex_yymm2")); wp.col_set("cond_1", ss);
     */
    /*
     * String ss2 = "回報日期: " + commString.ss_2ymd(wp.item_ss("ex_send_date1")) + " -- " +
     * commString.ss_2ymd(wp.item_ss("ex_send_date1")); wp.col_set("cond_2", ss2);
     */
    wp.colSet("IdUser", wp.loginUser);
    wp.colSet("sysdate", this.strMid(getSysDate(), 0, 6));
    wp.pageRows = 9999;
    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.pageCount = 28;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  void runBatch() throws Exception {
    String msg = "";
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    String wfValue2 = chkPtrSysParm();
    if (wfValue2.equals("DOING")) {
      alertErr("批次執行中，請稍後");
      return;
    }
    if (wfValue2.equals("FINISHED")) {
      alertErr("批次執行完畢，可查詢資料");
      return;
    }
    // --callbatch
    rc = batch.callBatch("ColB027" + " " + wp.loginUser);
    if (rc != 1) {
      errmsg("ColB027 處理: callbatch 失敗");
      return;
    }
    msg += "ColB027  處理:" + batch.batchSeqno();

    errmsg("開始執行batch程式，請稍候再讀取資料....." + msg);
  }

  String chkPtrSysParm() {
	CommDate commd = new CommDate();
		
	//讀取營業日
    String lsSql = "select business_date from ptr_businday ";
    sqlSelect(lsSql);
    String lsBusinessDate = sqlStr("business_date");
    
    //營業日前一天
    String lsBusinessDateBefore = commd.dateAdd(lsBusinessDate, 0, 0, -1);
    
    String sqlSelect =
        "select wf_value,wf_value2 from ptr_sys_parm where wf_parm = 'COL_PARM' and wf_key = 'COLB027_PARM' ";
    sqlSelect(sqlSelect);
    String wfValue = sqlStr("wf_value");
    String wfValue2 = sqlStr("wf_value2");
    if (sqlRowNum > 0) {
     // if (wfValue.equals(getSysDate()) || wfValue.equals(commd.dateAdd(getSysDate(), 0, 0, -1))) {
    	if (wfValue.equals(lsBusinessDate) || wfValue.equals(lsBusinessDateBefore)) {
        if (wfValue2.equals("DOING"))
          return wfValue2;          
        if (wfValue2.equals("FINISHED"))
          return wfValue2;
      } else {
        return "N";
      }
    }
    return "Y";
  }
}

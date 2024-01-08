/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-22  V1.00.00  ryan       program initial                            *
* 108-06-13  V2.00.00  ryan       p_seqno change to acno_p_seqno             *
* 109-05-06  V1.00.01  Aoyulan       updated for project coding standard     *
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package colm01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm1220 extends BaseProc {
  Colm1220Func func;
  int rr = -1;
  String msg = "";
  String lsAcctType = "", lsAcctKey = "", acctType = "", acctKey = "";
  String lsPSeqno = "", lsIdCorpPSeqno = "", lsIdCorpType = "", lsIdCorpNo = "", sql4 = "";
  int ilOk = 0;
  int ilErr = 0;
  int llCnt = 0;
  int llApr = 0;
  String lsColl = "", lsTable = "", lsRowid = "", lsSendDate = "";
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      checkData();
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      // -資料處理-
      checkdata2();
    } else if (eqIgno(wp.buttonCode, "S3")) {
      // -資料處理-
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

  }

  @Override
  public void dddwSelect() {
    // wp.initOption ="--";
    wp.optionKey = wp.itemStr("ex_acct_type");
    try {
      dddwList("PtrAcctTypeList", "ptr_acct_type", "acct_type", "acct_type||'['||chin_name||']'",
          "where 1=1 order by acct_type");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private int getWhereStr() throws Exception {
    lsAcctType = wp.itemStr("ex_acct_type");
    lsAcctKey = wp.itemStr("ex_acct_key");
    String lsDate1 = wp.itemStr("ex_acct_date1");
    String lsDate2 = wp.itemStr("ex_acct_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      return -1;
    }

    if (empty(lsAcctKey)) {
      alertErr2("帳戶帳號  不可空白");
      return -1;
    }
    if (lsAcctKey.length() == 10) {
      lsAcctKey += "0";
    }
    if (lsAcctKey.length() != 11 && lsAcctKey.length() != 8) {
      alertErr2("帳戶帳號  輸入錯誤");
      return -1;
    }
    // etAcnoData
    String sql1 = " select uf_acno_name (acno_p_seqno) as cname " + " ,acct_status "
        + " ,status_change_date " + " ,acno_p_seqno "
        + " ,decode (id_p_seqno,'',corp_p_seqno,id_p_seqno) as id_corp_p_seqno "
        + " ,decode (id_p_seqno,'','2','1')as id_corp_type" + " ,acct_type " + " ,acct_key "
        + " from act_acno " + " where decode (acct_type, '', '01', acct_type) = :ex_acct_type "
        + " and acct_key = :ex_acct_key ";
    setString("ex_acct_type", lsAcctType);
    setString("ex_acct_key", lsAcctKey);
    sqlSelect(sql1);

    if (sqlRowNum <= 0) {
      alertErr2("帳戶帳號  不存在");
      return -1;
    }
    acctType = sqlStr("acct_type");
    acctKey = sqlStr("acct_key");
    String acctStatus = sqlStr("acct_status");
    if (!empty(acctStatus)) {
      wp.colSet("ex_acct_status", commString.decode(acctStatus, ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
    } else {
      wp.colSet("ex_acct_status", "");
    }

    wp.colSet("ex_chg_date", sqlStr("status_change_date"));
    wp.colSet("ex_cname", sqlStr("cname"));
    lsPSeqno = sqlStr("acno_p_seqno");
    wp.colSet("ls_p_seqno", lsPSeqno);
    lsIdCorpType = sqlStr("id_corp_type");
    wp.colSet("ls_id_corp_type", lsIdCorpType);
    lsIdCorpPSeqno = sqlStr("id_corp_p_seqno");
    wp.colSet("ls_id_corp_p_seqno", lsIdCorpPSeqno);
    // getAcnoDays
    String sql2 = " select min (decode (trans_type,'3',trans_date,'')) as acct_date3, "
        + " max (decode (trans_type,'4',trans_date,'')) as acct_date4 " + " from col_bad_detail "
        + " where trans_type in ('3','4') and p_seqno = :ls_p_seqno ";
    setString("ls_p_seqno", lsPSeqno);
    sqlSelect(sql2);
    wp.colSet("ex_acct_date3", sqlStr("acct_date3"));
    wp.colSet("ex_acct_date4", sqlStr("acct_date4"));

    if (lsIdCorpType.equals("1")) {
      String sql3 =
          " select id_no as id_corp_no from crd_idno where id_p_seqno = :ls_id_corp_p_seqno ";
      setString("ls_id_corp_p_seqno", lsIdCorpPSeqno);
      sqlSelect(sql3);

    }
    if (lsIdCorpType.equals("2")) {
      String sql3 =
          " select corp_no as id_corp_no from crd_corp where corp_p_seqno = :ls_id_corp_p_seqno ";
      setString("ls_id_corp_p_seqno", lsIdCorpPSeqno);
      sqlSelect(sql3);
    }
    lsIdCorpNo = sqlStr("id_corp_no");
    wp.colSet("ls_id_corp_no", lsIdCorpNo);

    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and (tran_class='P' or tran_type='DR11') and ";
    sql4 += " select hex(rowid) as rowid, " + " p_seqno, " + " jrnl_seqno, " + " enq_seqno, "
        + " acct_date, " + " trans_amt, " + " lgd_coll_flag, " + " send_date, "
        + " 'col_lgd' as db_table, " + " apr_date, " + " mod_seqno " + " from col_lgd_jrnl "
        + " where 1=1 ";
    if (empty(lsAcctType) == false && empty(lsAcctKey) == false) {
      wp.whereStr += "p_seqno = :ls_p_seqno";
      setString("ls_p_seqno", lsPSeqno);
      sql4 += "  ";
    }

    if (empty(wp.itemStr("ex_acct_date1")) == false) {
      wp.whereStr += " and acct_date >= :ex_acct_date1 ";
      setString("ex_acct_date1", wp.itemStr("ex_acct_date1"));
      sql4 += " and acct_date  >= :ex_acct_date1 ";
    }
    if (empty(wp.itemStr("ex_acct_date2")) == false) {
      wp.whereStr += " and acct_date <= :ex_acct_date2 ";
      setString("ex_acct_date2", wp.itemStr("ex_acct_date2"));
      sql4 += " and acct_date  <= :ex_acct_date2 ";
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
    if (getWhereStr() != 1)
      return;
    wp.selectSQL = " '0' as db_mod, " + " '' as rowd, " + " '0' as mod_seqno, " + " crt_date, "
        + " crt_time, " + " jrnl_seqno, " + " enq_seqno, " + " p_seqno, " + " acct_type, "
        + " acct_date, " + " tran_class, " + " tran_type, " + " transaction_amt, "
        + " interest_date, " + " curr_code, " + " dc_transaction_amt, "
        // + " id, " //no column
        // + " corp_no, "
        // + " decode (id, '', corp_no, id) db_id_corp_no, "
        + " rpad (' ', 8) db_send_date, " + " 'act_jrnl' db_table, " + " ' ' db_coll_flag, "
        + " ' ' db_apr_flag ";

    wp.daoTable = "act_jrnl";

    wp.whereOrder = "  ";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {

      alertErr(appMsg.errCondNodata);
      return;
    }
    listWkdata();

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    sql4 += " and p_seqno = :ls_p_seqno ";
    setString("ls_p_seqno", lsPSeqno);

    setString("ex_acct_date1", wp.itemStr("ex_acct_date1"));
    setString("ex_acct_date2", wp.itemStr("ex_acct_date2"));
    sqlSelect(sql4);
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_acct_key", acctType + "-" + acctKey);
      wp.colSet(ii, "db_coll_flag", "1");
      for (int yy = 0; yy < sqlRowNum; yy++) {
        if (wp.colStr(ii, "jrnl_seqno").equals(sqlStr(yy, "jrnl_seqno"))
            && wp.colStr(ii, "enq_seqno").equals(sqlStr(yy, "enq_seqno"))
            && wp.colStr(ii, "transaction_amt").equals(sqlStr(yy, "trans_amt"))) {
          wp.colSet(ii, "mod_seqno", sqlStr(yy, "mod_seqno"));
          wp.colSet(ii, "db_coll_flag", sqlStr(yy, "lgd_coll_flag"));
          wp.colSet(ii, "db_send_date", sqlStr(yy, "send_date"));
          wp.colSet(ii, "rowid", sqlStr(yy, "rowid"));
          wp.colSet(ii, "db_table", sqlStr(yy, "db_table"));
          if (!empty(sqlStr(yy, "apr_date"))) {
            wp.colSet(ii, "db_apr_flag", "Y");

          } else {
            wp.colSet(ii, "db_apr_flag", "N");

          }
        }
      }
      String tranType = wp.colStr(ii, "tran_type");
      wp.colSet(ii, "tt_tran_type", wfPtrPaymentBillDesc(tranType));
      // wp.col_set(ii,"tt_tran_type", commString.decode(ss, ",AUT1,AUT2,COU1,TIKT,IBC1,IBC2,IBC3,"
      // + "IBA1,IBA2,IBA3,TEBK,INBK,IBOT,REFU,MIST,COMA,COMB,BON1,BON2,WAIP,BACK,"
      // + "DUMY,OTHR,COBO,AUT3,AUT4,AUT5,COU2,COU3,COU4,COU5,ACH1,IBC4,IBA4,EPAY,AUT6,"
      // + "COU6,AUT7,AUT8,IBC5,AUT9,AUT0", ",ICBC自動扣繳,它行自動扣繳,它行臨櫃繳款,郵局劃撥,自行臨櫃繳款-現金,"
      // + "自行臨櫃繳款-轉帳,自行臨櫃繳款-票據,金融卡ATM轉帳-自行現金,金融卡ATM轉帳-自行轉帳,金融卡ATM轉帳-它行轉帳,"
      // + "自行/它行電話銀行繳款,自行網路銀行繳款,IBM 繳款-其他,退貨或Reversal轉Payment,誤入帳補正,ATM繳款手續費轉Payment,"
      // + "Fancy卡退 手續費轉Payment,年費回饋,拉卡獎金轉Payment,D檔 退利息、違約金,Back Date 退利息、違約金,虛擬繳款,"
      // + "其他繳款,Fancy 卡基金,本行帳戶自動抵銷款,債務協商入帳-系統比例,債務協商入帳-欠款比例,統一超商臨櫃繳款,全家便利商店繳款,"
      // + "福客多便利商店繳款,萊爾富便利商店繳款,ACH他行自動扣款,自行臨櫃-其他票據,本行金融卡-它行轉帳-全國繳稅費平台,本行-E化繳費平台,"
      // + "債務協商入帳-本行最大債權,來來超商臨櫃繳款,前協-本行最大債權行,前協-他行最大債權行,個人信用貸款,更生-他行最大債權行,更生-本行最大債權行"));
    }

  }

  String wfPtrPaymentBillDesc(String idcode) throws Exception {
    String rtn = "";
    String lsSql = "select payment_type||' ['||bill_desc||']' id_desc from ptr_payment "
        + "where payment_type= :id_code ";
    setString("id_code", idcode);

    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      rtn = idcode;
    } else {
      rtn = sqlStr("id_desc");
    }
    return rtn;
  }

  int getLgd901Cnt() throws Exception {

    String sql5 =
        "select count (*) as ll_cnt from col_lgd_901 where id_corp_p_seqno in (select decode(id_p_seqno,'',corp_p_seqno,id_p_seqno) from act_acno  where acno_p_seqno = :ls_p_seqno) ";
    setString("ls_p_seqno", wp.itemStr("ls_p_seqno"));

    sqlSelect(sql5);
    if (this.sqlNotFind()) {
      llCnt = 0;
    } else {
      llCnt = (int) sqlNum("ll_cnt");
    }


    return llCnt;
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {

    func = new Colm1220Func(wp);

    String[] aaRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaDbCollFlag = wp.itemBuff("db_coll_flag");
    String[] aaDbTable = wp.itemBuff("db_table");
    String[] aaDbSendDate = wp.itemBuff("db_send_date");
    String[] aaPSeqno = wp.itemBuff("p_seqno");
    String[] aaCrtDate = wp.itemBuff("crt_date");
    String[] aaCrtTime = wp.itemBuff("crt_time");
    String[] aaJrnlSeqno = wp.itemBuff("jrnl_seqno");
    String[] aaEnqSeqno = wp.itemBuff("enq_seqno");
    String[] aaAcctType = wp.itemBuff("acct_type");
    String[] aaAcctDate = wp.itemBuff("acct_date");
    String[] aaTranClass = wp.itemBuff("tran_class");
    String[] aaTranType = wp.itemBuff("tran_type");
    String[] aaInterestDate = wp.itemBuff("interest_date");
    String[] aaTransactionAmt = wp.itemBuff("transaction_amt");
    String[] aaCurrCode = wp.itemBuff("curr_code");
    String[] aaDcTransactionAmt = wp.itemBuff("dc_transaction_amt");
    String[] aaLgdCollFlag = wp.itemBuff("lgd_coll_flag");

    wp.listCount[0] = aaRowid.length;


    // -update-
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;

      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "check", "checked");
      func.varsSet("aa_rowid", aaRowid[rr]);
      func.varsSet("aa_mod_seqno", aaModSeqno[rr]);
      func.varsSet("aa_db_coll_flag", aaDbCollFlag[rr]);
      func.varsSet("aa_db_table", aaDbTable[rr]);
      func.varsSet("aa_db_send_date", aaDbSendDate[rr]);
      func.varsSet("aa_p_seqno", aaPSeqno[rr]);
      func.varsSet("aa_crt_date", aaCrtDate[rr]);
      func.varsSet("aa_crt_time", aaCrtTime[rr]);
      func.varsSet("aa_jrnl_seqno", aaJrnlSeqno[rr]);
      func.varsSet("aa_enq_seqno", aaEnqSeqno[rr]);
      func.varsSet("aa_acct_type", aaAcctType[rr]);
      func.varsSet("aa_acct_date", aaAcctDate[rr]);
      func.varsSet("aa_tran_class", aaTranClass[rr]);
      func.varsSet("aa_tran_type", aaTranType[rr]);
      func.varsSet("aa_interest_date", aaInterestDate[rr]);
      func.varsSet("aa_transaction_amt", aaTransactionAmt[rr]);
      func.varsSet("aa_curr_code", aaCurrCode[rr]);
      func.varsSet("aa_dc_transaction_amt", aaDcTransactionAmt[rr]);
      func.varsSet("aa_lgd_coll_flag", aaLgdCollFlag[rr]);
      func.varsSet("aa_id_corp_type", wp.itemStr("ls_id_corp_type"));
      func.varsSet("aa_id_corp_p_seqno", wp.itemStr("ls_id_corp_p_seqno"));

      if (!empty(aaDbSendDate[rr])) {
        continue;
      }
      if (aaDbTable[rr].equals("act_jrnl")) {

        if (empty(aaDbCollFlag[rr])) {
          continue;
        }
      }
      if (empty(aaDbCollFlag[rr])) {
        rc = func.deleteFunc();
        if (rc != 1) {
          ilErr++;
          msg = "delete col_lgd_jrnl err";
          break;
        }
        continue;
      }

      if (aaDbTable[rr].equals("act_jrnl")) {
        rc = func.insertFunc();
        if (rc != 1) {
          ilErr++;
          msg = "insert col_lgd_jrnl err";
          break;
        }
      } else {
        rc = func.updateFunc();
        if (rc != 1) {
          msg = "update col_lgd_jrnl err";
          ilErr++;
          break;
        }
      }

    }
    if (ilErr > 0) {
      sqlCommit(0);
      wp.colSet(rr, "ok_flag", "!");
      alertErr("資料存檔處理失敗," + msg);
      return;
    }
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "V");
    }
    sqlCommit(1);
    queryFunc();
    alertMsg("資料存檔處理成功");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
    this.btnModeAud("XX");
  }

  void checkData() throws Exception {
    String[] aaRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    String[] aaTtDbCollFlag = wp.itemBuff("tt_db_coll_flag");
    String[] aaDbCollFlag = wp.itemBuff("db_coll_flag");
    wp.listCount[0] = aaRowid.length;
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "check", "checked");
      // wp.col_set(rr,"db_coll_flag", aa_db_coll_flag[rr]);
      if (empty(aaTtDbCollFlag[rr])) {
        aaTtDbCollFlag[rr] = "1";
      }
      if (aaTtDbCollFlag[rr].equals(aaDbCollFlag[rr])) {
        wp.colSet(rr, "ok_flag", "!");
        alertMsg("資料未異動，不可執行新增或修改之作業！");
        return;
      }
    }

    if (getLgd901Cnt() == 0) {
      wp.colSet("ll_cnt", llCnt);
    } else {
      checkdata2();
    }

  }

  void checkdata2() throws Exception {
    String[] aaRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    String[] aaDbSendDate = wp.itemBuff("db_send_date");
    String[] aaDbAprFlag = wp.itemBuff("db_apr_flag");
    String[] aaDbCollFlag = wp.itemBuff("db_coll_flag");
    wp.listCount[0] = aaRowid.length;
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "check", "checked");
      // wp.col_set(rr,"db_coll_flag", aa_db_coll_flag[rr]);
      if (aaDbAprFlag[rr].equals("Y")) {
        llApr++;
      }
      if (!empty(aaDbSendDate[rr])) {
        alertMsg("資料已傳送  不可再修改");
        wp.colSet(rr, "ok_flag", "!");
        return;
      }
    }
    if (llApr > 0) {
      wp.colSet("ll_apr", 0);
    } else {
      dataProcess();
    }
  }
}

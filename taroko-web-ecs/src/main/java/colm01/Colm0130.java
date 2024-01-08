/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Aoyulan       updated for project coding standard   *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                          * 
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                          *  
* 112-10-23  V1.00.04   sunny         調整處理結果的訊息文字                                                *                              
******************************************************************************/
package colm01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm0130 extends BaseProc {
  CommString commString = new CommString();
  Colm0130Func func;

  String pSeqno = "", kk2 = "";
  int ilOk = 0;
  int ilErr = 0;

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
      strAction = "Q";
      wp.colSet("queryReadCnt", "0");
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "";
      wp.optionKey = wp.colStr("exAccttype");
      this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type",
          "acct_type||' ['||chin_name||']'", "where 1=1 order by acct_type");
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "where 1=1 ";
    if (empty(wp.itemStr("exAccttype")) == false) {
      wp.whereStr += " and acct_type = :acct_type ";
      setString("acct_type", wp.itemStr("exAccttype"));
    }
    if (empty(wp.itemStr("exAcctkey")) == false) {
      wp.whereStr += " and acct_key like :acct_key ";
      setString("acct_key", wp.itemStr("exAcctkey") + "%");
    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "acct_type, " + "acct_key, " + "acct_status, " + "crd_idno.chi_name, "
        + "crd_idno.birthday, "
        // + "act_acno.p_seqno, "
        + "act_acno.acno_p_seqno as p_seqno, " + "act_acno.id_p_seqno ";

    wp.daoTable = "act_acno " + "left join crd_idno on crd_idno.id_p_seqno=act_acno.id_p_seqno ";

    // wp.whereOrder =" order by p_seqno ";
    wp.whereOrder = " order by acno_p_seqno ";

    pageQuery();
    wp.setListCount(2);
    wp.notFound = "N";
    if (sqlNotFind()) {
      wp.notFound = "Y";
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.colSet("queryReadCnt", intToStr(wp.dataCnt));
  }

  void listWkdata() {
    String acctStatus = "";
    String[] cde = new String[] {"1", "2", "3", "4", "5"};
    String[] txt = new String[] {"1.正常", "2.逾放", "3.催收", "4.呆帳", "5.結清"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      acctStatus = wp.colStr(ii, "acct_status");
      wp.colSet(ii, "tt_acct_status", commString.decode(acctStatus, cde, txt));
    }
  }

  void listWkdata2() {
    // 問題單0001421: Colm0130 戶況若非催呆戶，則不允許執行明細轉催呆
    String acctStatus = "";
    acctStatus = wp.itemStr("exAcctStatus");

    String audCode = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String ssDisabled = "";
      audCode = wp.colStr(ii, "aud_code");
      wp.colSet(ii, "tt_aud_code", commString.decode(audCode, ",0,1,2", ",無,待覆核,已覆核"));

      audCode = wp.colStr(ii, "acct_code");
//      if (",CF,AF,PF,DP,LF,AI,OF".indexOf(audCode) > 0)
//        ssDisabled = "disabled";
// TCB:只有爭議款不能處理，AI帳外息無，但先留著防呆。
      if (",DP,AI".indexOf(audCode) > 0)
        ssDisabled = "disabled";

      if (",1,2,5".indexOf(acctStatus) > 0)
        ssDisabled = "disabled";
      wp.colSet(ii, "tt_opt_disb", ssDisabled);
    }
  }

  void querySummary() {
    String lsTransDate, lsSrcAmt;
    String lsSql = "select trans_date, src_amt from col_bad_debt " + "where p_seqno = :p_seqno "
        + "  and trans_type = '4' ";
    setString("p_seqno", pSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      lsTransDate = sqlStr("trans_date");
      lsSrcAmt = sqlStr("src_amt");
      wp.colSet("exBadDate", lsTransDate);
      wp.colSet("exBadAmt", lsSrcAmt);
    }
  }

  @Override
  public void querySelect() throws Exception {
    pSeqno = wp.itemStr("data_k1");
    kk2 = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.setQueryMode();
    wp.pageControl();

    wp.selectSQL = "'0' as opt, "
        // + "deCode(col_debt_t.reference_no,CAST(NULL AS
        // VARCHAR(1)),'無',deCode(col_debt_t.aud_code,'1','待覆核','2','已覆核')) as tt_optcode, "
        + "act_debt.p_seqno, " + "act_debt.acct_type, " + "act_debt.post_date, "
        + "act_debt.acct_month, " + "act_debt.bill_type, " + "act_debt.txn_code, "
        + "act_debt.beg_bal, " + "act_debt.end_bal, " + "act_debt.d_avail_bal, "
        + "act_debt.acct_code, " + "ptr_actcode.chi_short_name, " + "act_debt.interest_date, "
        + "act_debt.reference_no, " + "act_debt.mod_user, " + "act_debt.mod_time, "
        + "act_debt.mod_pgm, " + "act_debt.mod_seqno, "
        + "nvl(col_debt_t.aud_code,'0') as aud_code, "
        + "nvl(col_debt_t.reference_no,'') as ColDebtTRefno, "
        + "nvl(col_debt_t.aud_code,'') as ColDebtTAudCode, "
        + "nvl(col_debt_t.apr_user,'') as ColDebtTAprUser, "
        + "nvl(col_debt_t.apr_date,'') as ColDebtTAprDate ";

    wp.daoTable = "act_acno, act_debt "
        + "left join ptr_actcode on act_debt.acct_code = ptr_actcode.acct_code "
        + "left join col_debt_t on act_debt.reference_no = col_debt_t.reference_no ";

    // wp.whereStr = "where act_acno.p_seqno = act_debt.p_seqno "
    // + "and act_acno.p_seqno = :p_seqno ";
    wp.whereStr =
        "where act_acno.acno_p_seqno = act_debt.p_seqno " + "and act_acno.acno_p_seqno = :p_seqno ";
    setString("p_seqno", pSeqno);
    if ("3".equals(kk2)) {
      wp.whereStr += "and act_debt.acct_code not in ('CB','CI','CC') ";
    } else if ("4".equals(kk2)) {
      wp.whereStr += "and act_debt.acct_code not in ('DB','AI') ";
    }

    wp.whereOrder = " order by act_debt.reference_no ";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata2();
    querySummary();
  }

  @Override
  public void dataProcess() throws Exception {
    func = new Colm0130Func(wp);

    String[] opt = wp.itemBuff("opt");
    String[] lsReferenceNo = wp.itemBuff("reference_no");
    String[] lsAcctCode = wp.itemBuff("acct_code");
    String[] lsAudCode = wp.itemBuff("aud_code");
    String[] lsEndBal = wp.itemBuff("end_bal");
    String[] lsColDebtTRefno = wp.itemBuff("ColDebtTRefno");
    String[] lsPSeqno = wp.itemBuff("p_seqno");
    String[] lsAcctType = wp.itemBuff("acct_type");
    String[] lsPostDate = wp.itemBuff("post_date");
    String[] lsAcctMonth = wp.itemBuff("acct_month");
    String[] lsBillType = wp.itemBuff("bill_type");
    String[] lsTxnCode = wp.itemBuff("txn_code");
    String[] lsBegBal = wp.itemBuff("beg_bal");
    String[] lsDAvailBal = wp.itemBuff("d_avail_bal");
    String[] lsInterestDate = wp.itemBuff("interest_date");
    // String[] ls_mod_seqno = wp.item_buff("mod_seqno");
    int rowcntaa = 0;
    if (!(lsReferenceNo == null) && !empty(lsReferenceNo[0]))
      rowcntaa = lsReferenceNo.length;
    wp.listCount[0] = rowcntaa;

    // -check duplication-
    int llErr = 0;
    for (int ii = 0; ii < opt.length; ii++) {
      wp.colSet(ii, "ok_flag", "");

      if ("1".equals(opt[ii])) {
//        if (",CF,AF,PF,DP,LF,AI,OF".indexOf(lsAcctCode[ii]) > 0) {
    	  if (",DP,AI".indexOf(lsAcctCode[ii]) > 0) {
          wp.colSet(ii, "ok_flag", "!");
          llErr++;
          continue;
        }
      }
    }
    if (llErr > 0) {
      // alert_err("CF現金手續費/AF年費/PF雜項手續費/DP爭議款/LF掛失費/AI帳外息/OF海外簽帳手續費 禁止轉催及D檔");
      //alertErr("CF現金手續費/AF年費/PF雜項手續費/DP爭議款/LF掛失費/AI帳外息 禁止轉催及D檔");
    	alertErr("DP爭議款/AI帳外息 禁止轉催及D檔");
      return;
    }

    // -insert-
    for (int ii = 0; ii < opt.length; ii++) {
      if ("0".equals(opt[ii]))
        continue;
      // System.out.println("-DDD->" + ii + ": ls_reference_no=" + ls_reference_no[ii]
      // + ", ls_acct_code=" + ls_acct_code[ii]
      // + ", ls_aud_code=" + ls_aud_code[ii]
      // + ", ls_end_bal=" + ls_end_bal[ii]
      // + ", ls_ColDebtTRefno=" + ls_ColDebtTRefno[ii]
      // + ", opt=" + opt[ii]);

      // 1.轉
      if ("1".equals(opt[ii])) {
        if (!"0".equals(lsAudCode[ii]))
          continue;

        if ("0".equals(numToStr(toNum(lsEndBal[ii]), "###0")))
          continue;

        if (!empty(lsColDebtTRefno[ii]))
          continue;
      }
      // 2.刪
      if ("2".equals(opt[ii])) {
        if (empty(lsColDebtTRefno[ii]))
          continue;

        if (!"1".equals(lsAudCode[ii]))
          continue;
      }

      // func.vars_set("reference_no", ls_reference_no[ii]);
      wp.itemSet("optcode", opt[ii]);
      wp.itemSet("reference_no", lsReferenceNo[ii]);
      wp.itemSet("p_seqno", lsPSeqno[ii]);
      wp.itemSet("acct_type", lsAcctType[ii]);
      wp.itemSet("post_date", lsPostDate[ii]);
      wp.itemSet("acct_month", lsAcctMonth[ii]);
      wp.itemSet("bill_type", lsBillType[ii]);
      wp.itemSet("txn_code", lsTxnCode[ii]);
      wp.itemSet("beg_bal", lsBegBal[ii]);
      wp.itemSet("end_bal", lsEndBal[ii]);
      wp.itemSet("d_avail_bal", lsDAvailBal[ii]);
      wp.itemSet("acct_code", lsAcctCode[ii]);
      wp.itemSet("interest_date", lsInterestDate[ii]);
      wp.itemSet("acct_status", wp.itemStr("exAcctStatus"));
      wp.itemSet("chi_name", wp.itemStr("exCname"));
      wp.itemSet("mod_user", loginUser());
      wp.itemSet("mod_pgm", "colm0130");
      // wp.item_set("mod_seqno", ls_mod_seqno[rr]);
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
        wp.colSet(ii, "ok_flag", "V");
        ilOk++;
        continue;
      }
      ilErr++;
      wp.colSet(ii, "ok_flag", "X");

    }
    // -re-Query-
    // queryRead();
    // queryFunc();
//    alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
      alertMsg("存檔處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

}

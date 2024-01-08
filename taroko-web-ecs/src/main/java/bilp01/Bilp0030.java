/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-01  V1.00.00  Andy       program initial                            *
* 107-06-26  V1.00.01  Andy       Update,UI,bug                              *
* 109-01-06  V1.00.02  Ru Chen    Modify AJAX                                *
*109-04-23   V1.00.03  shiyuqi       updated for project coding standard     *
* 110-10-15  V1.00.04  Yang Bo    joint sql replace to parameters way        *
* 111-05-31  V1.00.03  Ryan       增加異動人員與登入人員相同時不能覆核                                                    * 
******************************************************************************/

package bilp01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilp0030 extends BaseEdit {
  String mExMchtNo = "";
  String mExProductNo = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
      /* 執行 */
      strAction = "S2";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "itemchange")) {
      /* itemchange */
      itemchanged1();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      // 20200106 modify AJAX
      itemchanged();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    String exMchtNo = wp.itemStr("ex_merchant");
    String exProductNo = wp.itemStr("ex_product_no");
    String exModUser = wp.itemStr("ex_mod_user");

    wp.whereStr = " where 1=1  ";

    if (empty(exMchtNo) == false) {
//      wp.whereStr += " and  mcht_no like '" + exMchtNo + "%' ";
      wp.whereStr += sqlCol(exMchtNo, "mcht_no", "like%");
    }

    if (empty(exProductNo) == false) {
//      wp.whereStr += " and  product_no like '" + exProductNo + "%' ";
      wp.whereStr += sqlCol(exProductNo, "product_no", "like%");
    }

    if (empty(exModUser) == false) {
//      wp.whereStr += " and  mod_user like '" + exModUser + "%' ";
      wp.whereStr += sqlCol(exModUser, "mod_user", "like%");
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + "product_no, " + "product_name, " + "mcht_no, " + "unit_price, "
        + "tot_amt, " + "tot_term, " + "remd_amt, " + "extra_fees, " + "fees_fix_amt, "
        + "fees_min_amt, " + "fees_max_amt, " + "interest_rate, " + "interest_min_rate, "
        + "interest_max_rate, " + "auto_delv_flag, " + "auto_print_flag, " + "against_num, "
        + "confirm_flag, " + "clt_fees_fix_amt, " + "clt_interest_rate, " + "clt_fees_min_amt, "
        + "clt_fees_max_amt, " + "installment_flag, " + "refund_flag, " + "dtl_flag, "
        + "redeem_point, " + "redeem_amt, " + "risk_code, " + "trans_rate, " + "year_fees_rate, "
        + "'' wk_err, " + "mod_audcode, " + "mod_user, " + "mod_time, " + "mod_pgm, "
        + "mod_seqno ";
    wp.daoTable = "bil_prod_t";
    wp.whereOrder = " order by mcht_no,product_no ";
    getWhereStr();

    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
    // + wp.whereStr + wp.whereOrder);
    String lsSql = "select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr + wp.whereOrder;
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    
    wp.daoTable = "bil_prod_t";
    getWhereStr();
    
    lsSql = "select count(*) ct from " + wp.daoTable + wp.whereStr;
    sqlSelect(lsSql);
    wp.colSet("tot_cont", sqlNum("ct"));
    // list_wkdata();
    apprDisabled("mod_user");
  }

  void listWkdata() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_mcht_no = wp.item_ss("mcht_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExMchtNo = itemKk("data_k1");
    mExProductNo = itemKk("data_k2");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", mcht_no " + ", product_no"
        + ", product_name" + ", trans_rate" + ", risk_code" + ", tot_amt" + ", unit_price"
        + ", tot_term" + ", redeem_point" + ", redeem_amt" + ", remd_amt" + ", extra_fees"
        + ", against_num" + ", clt_fees_fix_amt" + ", interest_rate" + ", installment_flag"
        + ", clt_fees_min_amt" + ", clt_fees_max_amt" + ", year_fees_rate" + ", fees_fix_amt"
        + ", clt_interest_rate" + ", fees_min_amt" + ", fees_max_amt" + ", auto_delv_flag"
        + ", auto_print_flag" + ", refund_flag" + ", confirm_flag";
    wp.daoTable = "bil_prod_t";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  mcht_no = :mcht_no and product_no = :product_no";
    setString("mcht_no", mExMchtNo);
    setString("product_no", mExProductNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, product_no=" + mExMchtNo);
      return;
    }
    //
    queryDtl(mExMchtNo, mExProductNo);

  }

  @Override
  public void saveFunc() throws Exception {

    // String[] aa_rowid = wp.item_buff("rowid");
    // String[] aa_mod_seqno = wp.item_buff("mod_seqno");
    String[] opt = wp.itemBuff("opt");

    String[] aaProductNo = wp.itemBuff("product_no");
    String[] aaProductName = wp.itemBuff("product_name");
    String[] aaMchtNo = wp.itemBuff("mcht_no");
    String[] aaUnitPrice = wp.itemBuff("unit_price");
    String[] aaTotAmt = wp.itemBuff("tot_amt");
    String[] aaTotTerm = wp.itemBuff("tot_term");
    String[] aaRemdAmt = wp.itemBuff("remd_amt");
    String[] aaExtraFees = wp.itemBuff("extra_fees");
    String[] aaFeesFixAmt = wp.itemBuff("fees_fix_amt");
    String[] aaFeesMinAmt = wp.itemBuff("fees_min_amt");
    String[] aaFeesMaxAmt = wp.itemBuff("fees_max_amt");
    String[] aaInterestRate = wp.itemBuff("interest_rate");
    String[] aaInterestMinRate = wp.itemBuff("interest_min_rate");
    String[] aaInterestMaxRate = wp.itemBuff("interest_max_rate");
    String[] aaAutoDelvFlag = wp.itemBuff("auto_delv_flag");
    String[] aaAutoPrintFlag = wp.itemBuff("auto_print_flag");
    String[] aaAgainstNum = wp.itemBuff("against_num");
    // String[] aa_confirm_flag = wp.item_buff("confirm_flag");
    String[] aaCltfeesFixAmt = wp.itemBuff("clt_fees_fix_amt");
    String[] aaCltInterestRate = wp.itemBuff("clt_interest_rate");
    String[] aaCltFeesMinAmt = wp.itemBuff("clt_fees_min_amt");
    String[] aaCltFeesMaxAmt = wp.itemBuff("clt_fees_max_amt");
    String[] aaInstallmentFlag = wp.itemBuff("installment_flag");
    String[] aaRefundFlag = wp.itemBuff("refund_flag");
    String[] aaDtlFlag = wp.itemBuff("dtl_flag");
    String[] aaRedeemPoint = wp.itemBuff("redeem_point");
    String[] aaRedeemAmt = wp.itemBuff("redeem_amt");
    String[] aaRiskCode = wp.itemBuff("risk_code");
    String[] aaTransRate = wp.itemBuff("trans_rate");
    String[] aaYearFeesRate = wp.itemBuff("year_fees_rate");
    String[] aaModUser = wp.itemBuff("mod_user");

    wp.listCount[0] = aaMchtNo.length;

    // check
    int rr = -1;
    int llOk = 0, llErr = 0;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }

    }

    // save

    if (llErr == 0) {
      // -update-
      rr = -1;
      for (int ii = 0; ii < opt.length; ii++) {
        rr = (int) this.toNum(opt[ii]) - 1;
        if (rr < 0) {
          continue;
        }
        String lsSql = "";
        // System.out.println("GGS:" + aa_mcht_no[rr]);
        mExMchtNo = aaMchtNo[rr];
        mExProductNo = aaProductNo[rr];
        String wpRiskCode = aaRiskCode[rr];
        String wpTotTerm = aaTotTerm[rr];
        String wpProdName = aaProductName[rr];
        String wpModUser = aaModUser[rr];
        String wpAprUser = wp.loginUser;

        // check rsk_hirisk_prod
        if (empty(wpRiskCode) == false) {
          lsSql = "select decode(risk_code,'";
          lsSql += wpRiskCode;
          lsSql += "',1,0) risk1 , decode(risk_code,'";
          lsSql += wpRiskCode;
          lsSql += "',0,1) risk2 " + "from rsk_hirisk_prod " + "where 1=1 ";
          lsSql += sqlCol(mExMchtNo, "mcht_no");
          lsSql += sqlCol(wpTotTerm, "tot_term");

          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            if (toNum(sqlStr("risk1")) > 0) {
              // System.out.println("高風險產品檔資料已存在");
            }
            if (toNum(sqlStr("risk2")) > 0) {
              // alert_err("風險分類已在高風險產品~中登錄, 請用[w_rskm2130]修改");
              wp.colSet(rr, "ok_flag", "X");
              wp.colSet(rr, "wk_err", "風險分類已在高風險產品~中登錄, 請用[w_rskm2130]修改");
              llErr++;
              continue;
            }
          } else {
            lsSql = "insert into rsk_hirisk_prod "
                + "(mcht_no, tot_term, prod_name, risk_code, active_flag,"
                + "crt_user, crt_date, apr_flag, apr_date, apr_user, "
                + "mod_user, mod_time, mod_pgm, mod_seqno) " + "values ("
                + ":mcht_no, :tot_term, :prod_name, :risk_code, :active_flag,"
                + ":crt_user,  to_char(sysdate,'yyyymmdd'), :apr_flag, to_char(sysdate,'yyyymmdd'), :apr_user,"
                + ":mod_user, sysdate, 'Bilp0030', 1)";
            setString("mcht_no", mExMchtNo);
            setString("tot_term", wpTotTerm);
            setString("prod_name", wpProdName);
            setString("risk_code", wpRiskCode);
            setString("active_flag", "Y");
            setString("crt_user", wpModUser);
            setString("apr_flag", "Y");
            setString("apr_user", wpAprUser);
            setString("mod_user", wpModUser);
            // System.out.println("ls_sql D "+ls_sql);
            sqlExec(lsSql);
            if (sqlRowNum < 0) {
              // System.out.println("新增高風險產品檔失敗, mchtno = " +
              // m_ex_mcht_no);
              llErr++;
              wp.colSet(rr, "ok_flag", "X");
              wp.colSet(rr, "wk_err", "高風險產品檔(rsk_hirisk_prod)新增失敗");
              sqlCommit(0);
              continue;
            } else {
              // System.out.println("新增高風險產品檔成功");
            }
          }
        }
        // --Delete bil_prod_dtl刪除明細檔--
        lsSql = "delete from bil_prod_dtl ";
        lsSql += "where 1=1 ";
        lsSql += sqlCol(mExMchtNo, "mcht_no");
        lsSql += sqlCol(mExProductNo, "product_no");
        sqlExec(lsSql);
        if (sqlRowNum < 0) {
          // System.out.println("刪除bil_prod_dtl明細檔失敗, mchtno = " +
          // m_ex_mcht_no);
          llErr++;
          wp.colSet(rr, "ok_flag", "X");
          wp.colSet(rr, "wk_err", "刪除產品明細暫存檔(bil_prod_dtl)失敗");
          sqlCommit(0);
          continue;
        } else {
          // System.out.println("刪除明細檔成功");
        }
        // --insert bil_prod_dtl
        // ls_sql = "insert into bil_prod_dtl (mcht_no, product_no,
        // dtl_kind, dtl_value) "
        // + "select mcht_no, product_no, dtl_kind, dtl_value from
        // bil_prod_dtl_t "
        lsSql = "insert into bil_prod_dtl " + "(select * from bil_prod_dtl_t " + "where 1=1 ";
        lsSql += sqlCol(mExMchtNo, "mcht_no");
        lsSql += sqlCol(mExProductNo, "product_no");
        lsSql += ")";
        sqlExec(lsSql);
        if (sqlRowNum < 0) {
          // System.out.println("新增bil_prod_dtl明細檔失敗, mchtno = " +
          // m_ex_mcht_no);
          llErr++;
          wp.colSet(rr, "ok_flag", "X");
          wp.colSet(rr, "wk_err", "新增產品明細檔(bil_prod_dtl)失敗");
          sqlCommit(0);
          return;
        } else {
          // System.out.println("新增bil_prod_dtl明細檔成功");
        }

        // --Delete bil_prod_dtl_t
        lsSql = "delete from bil_prod_dtl_t " + "where 1=1 ";
        lsSql += sqlCol(mExMchtNo, "mcht_no");
        lsSql += sqlCol(mExProductNo, "product_no");
        sqlExec(lsSql);
        if (sqlRowNum < 0) {
          // System.out.println("刪除bil_prod_dtl_t明細暫存檔失敗, mchtno = " +
          // m_ex_mcht_no);
          llErr++;
          wp.colSet(rr, "ok_flag", "X");
          wp.colSet(rr, "wk_err", "刪除產品明細暫存檔(bil_prod_dtl_t)失敗");
          sqlCommit(0);
          continue;
        } else {
          // System.out.println("刪除bil_prod_dtl_t明細暫存檔成功");
        }

        // delete bil_merchant_t
        // ls_sql = " delete bil_merchant_t where 1=1 ";
        // ls_sql += sql_col(m_ex_mcht_no, "mcht_no");
        // ls_sql += sql_col(m_ex_product_no, "product_no");
        // sqlExec(ls_sql);
        // if (sql_nrow < 0) {
        // // System.out.println("刪除bil_merchant_t特店暫存檔失敗, mchtno = " +
        // // m_ex_mcht_no);
        // ll_err++;
        // wp.col_set(rr, "ok_flag", "X");
        // wp.col_set(rr, "wk_err", "刪除特店暫存檔失敗(bil_merchant_t)失敗");
        // sql_commit(0);
        // continue;
        // }

        // insert or update bil_prod
        String mRowid = "", mModSeqno = "";
        lsSql = "select hex(rowid) as rowid, mod_seqno from bil_prod " + "where 1=1 ";
        lsSql += sqlCol(mExMchtNo, "mcht_no");
        lsSql += sqlCol(mExProductNo, "product_no");
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          mRowid = sqlStr("rowid");
          mModSeqno = sqlStr("mod_seqno");
          // update
          String usSq = "update bil_prod set "
              // + "product_no =:product_no , "
              // + "mcht_no =:mcht_no, "
              + "product_name =:product_name, " // 1
              + "unit_price =:unit_price, " + "tot_amt =:tot_amt, " + "tot_term =:tot_term, "
              + "remd_amt =:remd_amt, " + "extra_fees =:extra_fees, "
              + "fees_fix_amt =:fees_fix_amt, " + "fees_min_amt =:fees_min_amt, "
              + "fees_max_amt =:fees_max_amt, " + "interest_rate =:interest_rate, "
              + "interest_min_rate =:interest_min_rate, " // 11
              + "interest_max_rate =:interest_max_rate, " + "auto_delv_flag =:auto_delv_flag, "
              + "auto_print_flag =:auto_print_flag, " + "against_num =:against_num, "
              + "confirm_flag =:confirm_flag, " + "clt_fees_fix_amt =:clt_fees_fix_amt, "
              + "clt_interest_rate =:clt_interest_rate, " + "clt_fees_min_amt =:clt_fees_min_amt, "
              + "clt_fees_max_amt =:clt_fees_max_amt, " + "installment_flag =:installment_flag, " // 21
              + "refund_flag =:refund_flag, " + "dtl_flag =:dtl_flag, "
              + "redeem_point =:redeem_point, " + "redeem_amt =:redeem_amt, "
              + "risk_code =:risk_code, " + "trans_rate =:trans_rate, "
              + "year_fees_rate =:year_fees_rate, " + "mod_user =:mod_user, "
              + "mod_time = sysdate, " // 31
              + "mod_pgm = 'Bilp0030', " + "mod_seqno = nvl(mod_seqno,0)+1 "
              + "where  hex(rowid) = :m_rowid  and mod_seqno = :m_mod_seqno ";
          // setString("product_no", aa_product_no[rr]);
          // setString("mcht_no", aa_mcht_no[rr]);
          setString("product_name", aaProductName[rr]);
          setString("unit_price", aaUnitPrice[rr]);
          setString("tot_amt", aaTotAmt[rr]);
          setString("tot_term", aaTotTerm[rr]);
          setString("remd_amt", aaRemdAmt[rr]);
          setString("extra_fees", aaExtraFees[rr]);
          setString("fees_fix_amt", aaFeesFixAmt[rr]);
          setString("fees_min_amt", aaFeesMinAmt[rr]);
          setString("fees_max_amt", aaFeesMaxAmt[rr]);
          setString("interest_rate", aaInterestRate[rr]);

          setString("interest_min_rate", aaInterestMinRate[rr]);
          setString("interest_max_rate", aaInterestMaxRate[rr]);
          setString("auto_delv_flag", aaAutoDelvFlag[rr]);
          setString("auto_print_flag", aaAutoPrintFlag[rr]);
          setString("against_num", aaAgainstNum[rr]);
          setString("confirm_flag", "Y");
          setString("clt_fees_fix_amt", aaCltfeesFixAmt[rr]);
          setString("clt_interest_rate", aaCltInterestRate[rr]);
          setString("clt_fees_min_amt", aaCltFeesMinAmt[rr]);
          setString("clt_fees_max_amt", aaCltFeesMaxAmt[rr]);

          setString("installment_flag", aaInstallmentFlag[rr]);
          setString("refund_flag", aaRefundFlag[rr]);
          setString("dtl_flag", aaDtlFlag[rr]);
          setString("redeem_point", aaRedeemPoint[rr]);
          setString("redeem_amt", aaRedeemAmt[rr]);
          setString("risk_code", aaRiskCode[rr]);
          setString("trans_rate", aaTransRate[rr]);
          setString("year_fees_rate", aaYearFeesRate[rr]);
          setString("mod_user", wp.loginUser);

          setString("m_rowid", mRowid);
          setString("m_mod_seqno", mModSeqno);
          // System.out.println("us_sq:" + us_sq);
          sqlExec(usSq);
          if (sqlRowNum <= 0) {
            // System.out.println("更新bil_prod失敗");
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "wk_err", "更新產品檔(bil_prod)失敗");
            llErr++;
            sqlCommit(0);
            continue;
          }
        } else {
          // insert
          String liSql = "insert into bil_prod "
              + "(product_no, product_name, mcht_no, unit_price, tot_amt, tot_term, remd_amt, extra_fees, "
              + "fees_fix_amt, fees_min_amt, fees_max_amt, interest_rate, interest_min_rate, interest_max_rate, "
              + "auto_delv_flag, auto_print_flag, against_num, confirm_flag, clt_fees_fix_amt, clt_interest_rate, "
              + "clt_fees_min_amt, clt_fees_max_amt, installment_flag, refund_flag, dtl_flag, redeem_point, "
              + "redeem_amt, risk_code, trans_rate, year_fees_rate, mod_user, mod_time, mod_pgm, mod_seqno) "
              + "values "
              + "(:product_no, :product_name, :mcht_no, :unit_price, :tot_amt, :tot_term, :remd_amt, :extra_fees,"
              + " :fees_fix_amt, :fees_min_amt, :fees_max_amt, :interest_rate, :interest_min_rate, :interest_max_rate, "
              + " :auto_delv_flag, :auto_print_flag, :against_num, :confirm_flag, :clt_fees_fix_amt, :clt_interest_rate, "
              + " :clt_fees_min_amt, :clt_fees_max_amt, :installment_flag, :refund_flag, :dtl_flag, :redeem_point, "
              + " :redeem_amt, :risk_code, :trans_rate, :year_fees_rate, :mod_user, sysdate, 'Bilp0030', 1)";

          setString("product_no", aaProductNo[rr]);
          setString("product_name", aaProductName[rr]);
          setString("mcht_no", aaMchtNo[rr]);
          setString("unit_price", aaUnitPrice[rr]);
          setString("tot_amt", aaTotAmt[rr]);
          setString("tot_term", aaTotTerm[rr]);
          setString("remd_amt", aaRemdAmt[rr]);
          setString("extra_fees", aaExtraFees[rr]);

          setString("fees_fix_amt", aaFeesFixAmt[rr]);
          setString("fees_min_amt", aaFeesMinAmt[rr]);
          setString("fees_max_amt", aaFeesMaxAmt[rr]);
          setString("interest_rate", aaInterestRate[rr]);
          setString("interest_min_rate", aaInterestMinRate[rr]);
          setString("interest_max_rate", aaInterestMaxRate[rr]);

          setString("auto_delv_flag", aaAutoDelvFlag[rr]);
          setString("auto_print_flag", aaAutoPrintFlag[rr]);
          setString("against_num", aaAgainstNum[rr]);
          setString("confirm_flag", "Y");
          setString("clt_fees_fix_amt", aaCltfeesFixAmt[rr]);
          setString("clt_interest_rate", aaCltInterestRate[rr]);

          setString("clt_fees_min_amt", aaCltFeesMinAmt[rr]);
          setString("clt_fees_max_amt", aaCltFeesMaxAmt[rr]);
          setString("installment_flag", aaInstallmentFlag[rr]);
          setString("refund_flag", aaRefundFlag[rr]);
          setString("dtl_flag", aaDtlFlag[rr]);
          setString("redeem_point", aaRedeemPoint[rr]);

          setString("redeem_amt", aaRedeemAmt[rr]);
          setString("risk_code", aaRiskCode[rr]);
          setString("trans_rate", aaTransRate[rr]);
          setString("year_fees_rate", aaYearFeesRate[rr]);
          setString("mod_user", wp.loginUser);
          // System.out.println("li_sql:" + li_sql);
          sqlExec(liSql);

          if (sqlRowNum <= 0) {
            // System.out.println("新增bil_prod失敗");
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "wk_err", "新增產品檔(bil_prod)失敗");
            llErr++;
            sqlCommit(0);
            continue;
          } else {
            // System.out.println("新增bil_prod成功");
          }
        }
        // --Delete Temp--
        lsSql = "delete from bil_prod_t where 1=1 ";
        lsSql += sqlCol(mExMchtNo, "mcht_no");
        lsSql += sqlCol(mExProductNo, "product_no");
        sqlExec(lsSql);
        if (sqlRowNum <= 0) {
          // System.out.println("刪除bil_prod_t失敗");
          wp.colSet(rr, "ok_flag", "X");
          wp.colSet(rr, "wk_err", "刪除產品暫存檔(bil_prod_t)失敗");
          llErr++;
          sqlCommit(0);
          continue;
        } else {
          wp.colSet(rr, "ok_flag", "V");
          sqlCommit(1);
          llOk++;
        }
      }
    }
    alertMsg("放行處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";");
    // alert_msg("放行處理成功!!");

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_mcht_no");
      this.dddwList("dddw_mcht_no", "bil_merchant", "mcht_no", "mcht_chi_name",
          "where 1=1 and mcht_status = '1' group by mcht_no,mcht_chi_name order by mcht_no");

      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("ex_product_no");
      // this.dddw_list("dddw_product_no", "bil_prod", "product_no",
      // "product_name", " where 1=1 order by product_no");
    } catch (Exception ex) {
    }
  }

  void queryDtl(String mchtNo, String productNo) throws Exception {
    daoTid = "a-";
    wp.selectSQL = " dtl_kind, dtl_value ";
    wp.daoTable = " bil_prod_dtl_t ";
    wp.whereStr = "where mcht_no=:mcht_no and product_no=:product_no ";
    setString("mcht_no", mchtNo);
    setString("product_no", productNo);
    wp.whereOrder = " order by dtl_value ";

    pageQuery();

    wp.notFound = "N";
    wp.setListCount(1);

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String wpKind = wp.colStr(ii, "a-dtl_kind");
      String wpDtlCValue = wp.colStr(ii, "a-dtl_value");
      String lsSql = "";
      switch (wpKind) {
        case "ACCT-TYPE":
          lsSql = " select acct_type, chin_name from ptr_acct_type where 1=1";
          lsSql += sqlCol(wpDtlCValue, "acct_type");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            wp.colSet(ii, "a-dtl_value", wpDtlCValue + " " + sqlStr("chin_name"));
          }
          break;
        case "CARD-TYPE":
          lsSql = " select card_type, name from ptr_card_type where 1=1";
          lsSql += sqlCol(wpDtlCValue, "card_type");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            wp.colSet(ii, "a-dtl_value", wpDtlCValue + " " + sqlStr("name"));
          }
          break;
        case "GROUP-CODE":
          lsSql = " select group_code, group_name from ptr_group_code where 1=1";
          lsSql += sqlCol(wpDtlCValue, "group_code");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            wp.colSet(ii, "a-dtl_value", wpDtlCValue + " " + sqlStr("group_name"));
          }
          break;
      }
    }
  }

  // 20200106 modify AJAX
  public int itemchanged() throws Exception {
    // super.wp = wr;
    String ajaxName = "";
    String lsMchtNo = "";
    String option = "";
    String lsSql = "";
    ajaxName = wp.itemStr("ajaxName");

    switch (ajaxName) {
      case "pop_mchtno":
        wp.varRows = 1000;
        setSelectLimit(0);
        // wp.selectSQL = "mcht_no,mcht_chi_name";
        // wp.daoTable = "bil_merchant";
        // wp.whereStr = "where mcht_status = '1' and mcht_no like :mcht_no ";
        // wp.orderField = "mcht_no1";
        // setString("mcht_no", wp.item_ss("ex_merchant") + "%");
        // pageQuery();
        // for (int i = 0; i < wp.selectCnt; i++) {
        // wp.addJSON("OPTION_TEXT", wp.col_ss(i, "mcht_no") + "_" + wp.col_ss(i, "mcht_chi_name"));
        // wp.addJSON("OPTION_VALUE", wp.col_ss(i, "mcht_no"));
        // }
        String lsSql2 = "select mcht_no,mcht_chi_name "
            + " ,mcht_no||'_'||mcht_chi_name as inter_desc " + " from bil_merchant "
            + " where mcht_status = '1' and mcht_no like :mcht_no " + " order by mcht_no ";

        setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");

        sqlSelect(lsSql2);

        for (int i = 0; i < sqlRowNum; i++) {
          wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
          wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
        }
        break;
      case "mcht_no_change":
        lsMchtNo = wp.itemStr("mcht_no");
        lsSql = "select product_no, product_no||'_'||product_name as product_name from bil_prod "
            + "where 1=1 ";
        lsSql += sqlCol(lsMchtNo, "mcht_no");
        lsSql += " order by product_no ";
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          break;
        }
        option += "<option value=\"\">--</option>";
        for (int ii = 0; ii < sqlRowNum; ii++) {
          option += "<option value=\"" + sqlStr(ii, "product_no") + "\">"
              + sqlStr(ii, "product_name") + "</option>";
        }
        wp.addJSON("dddw_product_no", option);
        break;
    }
    return 1;
  }

  void itemchanged1() throws Exception {
    String lsSql = "", lsMchtNo = "", option = "";

    lsMchtNo = wp.itemStr("ex_merchant");
    if (empty(lsMchtNo)) {
      return;
    }
    lsSql = "select product_no, product_no||'_'||product_name as product_name from bil_prod "
        + "where 1=1 ";
    lsSql += sqlCol(lsMchtNo, "mcht_no");
    lsSql += " order by product_no ";
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      return;
    }
    // option += "<option value=\"\">--</option>";
    for (int ii = 0; ii < sqlRowNum; ii++) {
      option += "<option value=\"" + sqlStr(ii, "product_no") + "\">" + sqlStr(ii, "product_name")
          + "</option>";
    }
    wp.colSet("dddw_product_no", option);

    return;
  }
  
//	void apprDisabled(String col) throws Exception {
//		for (int ll = 0; ll < wp.listCount[0]; ll++) {
//			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
//				wp.colSet(ll, "opt_disabled", "");
//			} else
//				wp.colSet(ll, "opt_disabled", "disabled");
//		}
//	}

}

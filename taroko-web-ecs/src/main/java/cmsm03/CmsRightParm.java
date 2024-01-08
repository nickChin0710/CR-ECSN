/*卡片權益參數公用程式 V.2018-1016.jh
* 109-04-27  shiyuqi       updated for project coding standard     *  
 * 
 * */
package cmsm03;

import busi.FuncBase;

public class CmsRightParm extends FuncBase {

  boolean ibBankEmp = false;
  private String idNo = "", cardNo = "", idPSeqno = "";
  public String isYymm1 = "", isYymm2 = "";

  busi.DataSet dsParm = new busi.DataSet();
  busi.DataSet dsDetl = new busi.DataSet();
  private String acctType;
  private String groupCode;
  private String cardType;

  public int isBankEmp(String aKey) {
    ibBankEmp = false;
    if (aKey.length() == 10) {
      strSql = "select id_p_seqno from crd_idno" + " where id_no =?";
      setString2(1, aKey);
      sqlSelect(strSql);
      if (sqlRowNum <= 0) {
        errmsg("身分證ID: 錯誤, 非本行卡友");
        return rc;
      }
      idPSeqno = colStr("id_p_seqno");
      idNo = commString.nvl(aKey);
    }

    if (aKey.length() >= 15) {
      cardNo = commString.nvl(aKey);
      strSql = "select id_p_seqno, id_no from crd_idno"
          + " where id_p_seqno in (select id_p_seqno from crd_card where card_no =?)";
      setString2(1, cardNo);
      sqlSelect(strSql);
      if (sqlRowNum <= 0) {
        errmsg("卡號: 錯誤, 非本卡友");
        return rc;
      }
      idPSeqno = colStr("id_p_seqno");
      idNo = colStr("id_no");
    }


    ibBankEmp = selectCrdEmployee(idNo);

    return 1;
  }

  boolean selectCrdEmployee(String aIdno) {
    strSql = "select count(*) as xx_cnt" + " from crd_employee" + " where status_id in ('1','7')"
        + " and id =?";
    setString2(1, aIdno);
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      return (colNum("xx_cnt") > 0);
    }

    return false;
  }

  public int useCountItem08() {
    if (ibBankEmp)
      return useCountItem08ID();

    return useCountItem08Card();
  }

  public int useCountItem08Card() {
    // --機場接送次數: 卡號
    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }

    strSql = "select count(*) as xx_cnt" + " from bil_contract" + " where purchase between ? and ?"
        + " and id_p_seqno =?"
        + " and (mcht_no,product_no) in (select distinct trim(substr(key_data,1,15)) as mcht_no,"
        + " trim(substr(key_data,16,8)) as prod_no"
        + " from mkt_contri_parm where item_no='08' and cost_month between ? and ?)";
    setString2(1, isYymm1 + "01");
    setString(isYymm2 + "31");
    setString(cardNo);
    setString(isYymm1);
    setString(isYymm2);
    sqlSelect(strSql);
    if (sqlRowNum > 0)
      return colInt("xx_cnt");
    return 0;
  }

  public int useCountItem08ID() {
    // --機場接送次數
    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }
    // -行員-
    strSql = "select count(*) as xx_cnt" + " from bil_contract" + " where purchase between ? and ?"
        + " and id_p_seqno =?"
        + " and (mcht_no,product_no) in (select distinct trim(substr(key_data,1,15)) as mcht_no,"
        + " trim(substr(key_data,16,8)) as prod_no"
        + " from mkt_contri_parm where item_no='08' and cost_month between ? and ?)";
    setString2(1, isYymm1 + "01");
    setString(isYymm2 + "31");
    setString(idPSeqno);
    setString(isYymm1);
    setString(isYymm2);
    sqlSelect(strSql);
    if (sqlRowNum > 0)
      return colInt("xx_cnt");
    return 0;

  }

  public int useCountItem09() {
    if (ibBankEmp)
      return useCountItem09ID();

    return useCountItem09Card();
  }

  public int useCountItem09ID() {
    // 機場停車次數--
    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }

    strSql = " select count(*) as xx_cnt from bil_contract " + " where " + " id_p_seqno = ? "
        + " and purchase_date between ? and ? "
        + " and (mcht_no,product_no) in (select distinct trim(substr(key_data,1,15)) as mcht_no,"
        + " trim(substr(key_data,16,8)) as prod_no "
        + " from mkt_contri_parm where item_no='09' and cost_month between ? and ?) ";


    setString2(1, idPSeqno);
    setString(isYymm1 + "01");
    setString(isYymm2 + "31");
    setString(isYymm1);
    setString(isYymm2);
    sqlSelect(strSql);
    if (sqlRowNum > 0)
      return colInt("xx_cnt");
    return 0;

  }

  public int useCountItem09Card() {
    // 機場停車次數--
    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }

    strSql = " select count(*) as xx_cnt from bil_contract " + " where " + " card_no = ? "
        + " and purchase_date between ? and ? "
        + " and (mcht_no,product_no) in (select distinct trim(substr(key_data,1,15)) as mcht_no,"
        + " trim(substr(key_data,16,8)) as prod_no "
        + " from mkt_contri_parm where item_no='09' and cost_month between ? and ?) ";


    setString2(1, cardNo);
    setString(isYymm1 + "01");
    setString(isYymm2 + "31");
    setString(isYymm1);
    setString(isYymm2);
    sqlSelect(strSql);
    if (sqlRowNum > 0)
      return colInt("xx_cnt");
    return 0;

  }

  public int useCountItem10() {
    if (ibBankEmp)
      return useCountItem10ID();

    return useCountItem10Card();
  }

  public int useCountItem10ID() {
    // 摩爾貴賓室--
    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }

    strSql = " select count(*) as xx_cnt from bil_contract " + " where " + " id_p_seqno = ? "
        + " and purchase_date between ? and ? "
        + " and (mcht_no,product_no) in (select distinct trim(substr(key_data,1,15)) as mcht_no,"
        + " trim(substr(key_data,16,8)) as prod_no "
        + " from mkt_contri_parm where item_no='10' and cost_month between ? and ?) ";

    setString2(1, idPSeqno);
    setString(isYymm1 + "01");
    setString(isYymm2 + "31");
    setString(isYymm1);
    setString(isYymm2);
    sqlSelect(strSql);
    if (sqlRowNum > 0)
      return colInt("xx_cnt");
    return 0;
  }

  public int useCountItem10Card() {
    // 摩爾貴賓室--
    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }

    strSql = " select count(*) as xx_cnt from bil_contract " + " where card_no =? "
        + " and purchase_date between ? and ? "
        + " and (mcht_no,product_no) in (select distinct trim(substr(key_data,1,15)) as mcht_no,"
        + " trim(substr(key_data,16,8)) as prod_no "
        + " from mkt_contri_parm where item_no='10' and cost_month between ? and ?) ";

    setString2(1, cardNo);
    setString(isYymm1 + "01");
    setString(isYymm2 + "31");
    setString(isYymm1);
    setString(isYymm2);
    sqlSelect(strSql);
    if (sqlRowNum > 0)
      return colInt("xx_cnt");
    return 0;
  }

  public int useCountItem11() {
    if (ibBankEmp)
      return useCountItem11ID();

    return useCountItem11Card();
  }

  public int useCountItem11ID() {
    // 市區停車--

    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }

    strSql = " select sum(free_hr) as xx_cnt from mkt_dodo_resp " + " where id_p_seqno = ? "
        + " and tran_date between ? and ?  ";

    setString2(1, idPSeqno);
    setString(isYymm1 + "01");
    setString(isYymm2 + "31");
    sqlSelect(strSql);
    return 0;
  }

  public int useCountItem11Card() {
    // 市區停車--

    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }

    strSql = " select sum(free_hr) as xx_cnt from mkt_dodo_resp " + " where card_no = ? "
        + " and tran_date between ? and ?  ";
    setString2(1, cardNo);
    setString(isYymm1 + "01");
    setString(isYymm2 + "31");
    sqlSelect(strSql);
    return 0;
  }

  public int useCountItem13() {
    if (ibBankEmp)
      return useCountItem13ID();

    return useCountItem13Card();
  }

  public int useCountItem13ID() {
    // 新貴通貴賓室--
    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }

    strSql = " select count(*) as xx_cnt from bil_contract " + " where " + " id_p_seqno = ? "
        + " and visit_date between ? and ? "
        + " and (mcht_no,product_no) in (select distinct trim(substr(key_data,1,15)) as mcht_no,"
        + " trim(substr(key_data,16,8)) as prod_no "
        + " from mkt_contri_parm where item_no='13' and cost_month between ? and ?) "
        + " and uf_nvl(free_use_cnt,0) > 0 ";

    setString2(1, idPSeqno);
    setString(isYymm1 + "01");
    setString(isYymm2 + "31");
    setString(isYymm1);
    setString(isYymm2);

    if (sqlRowNum > 0)
      return colInt("xx_cnt");
    return 0;

  }

  public int useCountItem13Card() {
    // 新貴通貴賓室--
    if (empty(isYymm1) || empty(isYymm2)) {
      errmsg("使用期間: 不可空白");
      return rc;
    }

    strSql = " select count(*) as xx_cnt from bil_contract " + " where card_no =? "
        + " and visit_date between ? and ? "
        + " and (mcht_no,product_no) in (select distinct trim(substr(key_data,1,15)) as mcht_no,"
        + " trim(substr(key_data,16,8)) as prod_no "
        + " from mkt_contri_parm where item_no='13' and cost_month between ? and ?) "
        + " and uf_nvl(free_use_cnt,0) > 0 ";

    setString2(1, cardNo);
    setString(isYymm1 + "01");
    setString(isYymm2 + "31");
    setString(isYymm1);
    setString(isYymm2);
    sqlSelect(strSql);
    if (sqlRowNum > 0)
      return colInt("xx_cnt");
    return 0;

  }

  public int canUseItemCurr(String aItemNo) {
    if (empty(aItemNo)) {
      errmsg("權益項目: 不可空白");
      return -1;
    }
    if (ibBankEmp)
      return canUseItemCurrID(aItemNo);

    return canUseItemCurrCard(aItemNo);
  }

  public int canUseItemCurrID(String aItemNo) {
    // -今年可用次數-
    selectCmsRightParm(aItemNo);
    if (dsParm.listRows() <= 0) {
      errmsg("無符合專案");
      return 0;
    }

    strSql = "select card_no, acct_type, group_code, card_type"
        + " from crd_card where id_p_seqno =? and current_code='0'";
    setString2(1, idPSeqno);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("無流通卡");
      return 0;
    }
    int llNrow = sqlRowNum;
    String lsProjCode = "";
    for (int ii = 0; ii < llNrow; ii++) {
      acctType = colStr(ii, "acct_type");
      groupCode = colStr(ii, "group_code");
      cardType = colStr(ii, "card_type");
      lsProjCode = checkRightParmdetl();
      if (!empty(lsProjCode)) {
        break;
      }
    }
    if (empty(lsProjCode)) {
      errmsg("卡人所有卡片, 無符合專案");
      return 0;
    }

    // -今年消費金額-
    strSql = "select sum(consume_bl_amt+consume_ca_amt+consume_it_amt"
        + "+consume_ao_amt+consume_id_amt+consume_ot_amt) as consume_amt"
        + ", SUM(sub_bl_amt+sub_ca_amt+sub_it_amt" + "+sub_ao_amt+sub_id_amt+sub_ot_amt) as sub_amt"
        + " from mkt_post_consume" + " where 1=1"
        + " and acct_month like to_char(sysdate,'yyyy')||'%'" + " and id_p_seqno =?";
    setString2(1, idPSeqno);
    sqlSelect(strSql);
    double lmAmt = 0;
    if (sqlRowNum > 0) {
      lmAmt = colNum("consume_amt") - colNum("sub_amt");
    }
    if (lmAmt <= 0) {
      errmsg("本年度消費為 %s", lmAmt);
      return 0;
    }

    int llCnt = 0;
    for (int ii = 0; ii < dsParm.listRows(); ii++) {
      dsParm.listToCol(ii);
      if (dsParm.colEq("proj_code", lsProjCode) == false)
        continue;

      double lmAmt2 = dsParm.colNum("curr_amt1");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt1");
      lmAmt2 = dsParm.colNum("curr_amt2");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt2");
      lmAmt2 = dsParm.colNum("curr_amt3");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt3");
      lmAmt2 = dsParm.colNum("curr_amt4");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt4");
      lmAmt2 = dsParm.colNum("curr_amt5");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt5");
      lmAmt2 = dsParm.colNum("curr_amt6");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt6");
    }

    return llCnt;
  }

  public int canUseItemCurrCard(String aItemNo) {
    // -今年可用次數-
    selectCmsRightParm(aItemNo);
    if (dsParm.listRows() <= 0) {
      errmsg("無符合專案");
      return 0;
    }

    strSql = "select card_no, acct_type, group_code, card_type"
        + " from crd_card where card_no =? and current_code='0'";
    setString2(1, cardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("無流通卡");
      return 0;
    }
    int llNrow = sqlRowNum;
    String lsProjCode = "";
    for (int ii = 0; ii < llNrow; ii++) {
      acctType = colStr(ii, "acct_type");
      groupCode = colStr(ii, "group_code");
      cardType = colStr(ii, "card_type");
      lsProjCode = checkRightParmdetl();
      if (!empty(lsProjCode)) {
        break;
      }
    }
    if (empty(lsProjCode)) {
      errmsg("卡片[%s], 無符合專案", cardNo);
      return 0;
    }

    // -今年消費金額-
    strSql = "select sum(consume_bl_amt+consume_ca_amt+consume_it_amt"
        + "+consume_ao_amt+consume_id_amt+consume_ot_amt) as consume_amt"
        + ", SUM(sub_bl_amt+sub_ca_amt+sub_it_amt" + "+sub_ao_amt+sub_id_amt+sub_ot_amt) as sub_amt"
        + " from mkt_post_consume" + " where 1=1"
        + " and acct_month like to_char(sysdate,'yyyy')||'%'" + " and card_no =?";
    setString2(1, cardNo);
    sqlSelect(strSql);
    double lmAmt = 0;
    if (sqlRowNum > 0) {
      lmAmt = colNum("consume_amt") - colNum("sub_amt");
    }
    if (lmAmt <= 0) {
      errmsg("本年度消費為 %s", lmAmt);
      return 0;
    }

    int llCnt = 0;
    for (int ii = 0; ii < dsParm.listRows(); ii++) {
      dsParm.listToCol(ii);
      if (dsParm.colEq("proj_code", lsProjCode) == false)
        continue;

      double lmAmt2 = dsParm.colNum("curr_amt1");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt1");
      lmAmt2 = dsParm.colNum("curr_amt2");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt2");
      lmAmt2 = dsParm.colNum("curr_amt3");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt3");
      lmAmt2 = dsParm.colNum("curr_amt4");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt4");
      lmAmt2 = dsParm.colNum("curr_amt5");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt5");
      lmAmt2 = dsParm.colNum("curr_amt6");
      if (lmAmt2 > 0 && lmAmt > lmAmt2)
        llCnt = dsParm.colInt("curr_cnt6");
    }

    return llCnt;
  }

  void selectCmsRightParm(String aItemNo) {
    dsParm.dataClear();
    dsDetl.dataClear();

    strSql = "select proj_code, acct_type_flag, group_card_flag" + ", curr_date_s, curr_date_e"
        + ", last_amt1, last_cnt1, last_amt2, last_cnt2, last_amt3, last_cnt3"
        + " from cms_right_parm" + " where apr_flag ='Y' and active_status='Y'"
        + " and item_no =? and card_hldr_flag=?" + " order by proj_code";
    setString2(1, aItemNo);
    if (ibBankEmp)
      setString("1");
    else
      setString("2");
    dsParm.colList = sqlQuery(strSql);
    if (dsParm.listRows() == 0)
      return;

    // -
    strSql = "select B.acct_type_flag, B.group_card_flag, A.*"
        + " from cms_right_parm_detl A join cms_right_parm B on A.proj_code=B.proj_code"
        + " where A.table_id='QUAL' and A.apr_flag='Y'"
        + " and  B.apr_flag ='Y' and B.active_status='Y'"
        + " and B.item_no =? and B.card_hldr_flag=?"
        + " order by A.proj_code, A.data_type, A.data_code";
    setString2(1, aItemNo);
    if (ibBankEmp)
      setString("1");
    else
      setString("2");
    dsDetl.colList = sqlQuery(strSql);
  }

  String checkRightParmdetl() {
    int llNrow = dsDetl.listRows();
    for (int ii = 0; ii < llNrow; ii++) {
      dsDetl.listToCol(ii);

      if (dsDetl.colEq("acct_type_flag", "Y") && dsDetl.colEq("data_type", "01")) {
        if (dsDetl.colEq("data_code", acctType) == false)
          continue;
      }
      if (dsDetl.colEq("group_card_flag", "Y") && dsDetl.colEq("data_type", "02")) {
        if (!dsDetl.colEmpty("data_code") && dsDetl.colEq("data_code", groupCode) == false)
          continue;
        if (!dsDetl.colEmpty("data_code2") && dsDetl.colEq("data_code2", cardType) == false)
          continue;
      }

      return dsDetl.colStr("proj_code");
    }
    return "";
  }

}

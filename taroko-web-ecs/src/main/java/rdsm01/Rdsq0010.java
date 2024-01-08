/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-1227  V1.00.01  Alex  fix order by
* 109-04-22  V1.00.02  Tanwei       updated for project coding standard      *
* 109-07-21  V1.00.05  jiangyigndong  change table field                     *
******************************************************************************/
package rdsm01;

import ofcapp.BaseAction;

public class Rdsq0010 extends BaseAction {

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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {

    if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2") && wp.itemEmpty("ex_card_no")
        && wp.itemEmpty("ex_idno") && wp.itemEmpty("ex_car_no")) {
      alertErr2("登錄卡號,登錄車號,持卡人ID,異動日期 不可全部空白");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("異動日期起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "rd_moddate", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "rd_moddate", "<=") + " and apr_date <>'' ";

    if (!empty(wp.itemStr("ex_card_no"))) {
      lsWhere += " and (card_no = '" + wp.itemStr("ex_card_no") + "' or new_card_no ='"
          + wp.itemStr("ex_card_no") + "') ";
    }

    if (!empty(wp.itemStr("ex_idno"))) {
      lsWhere +=
          " and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 "
              + sqlCol(wp.itemStr2("ex_idno"), "B.id_no") + ")";
    }

    if (!empty(wp.itemStr("ex_car_no"))) {
      lsWhere += "and (rd_carno = '" + wp.itemStr("ex_car_no") + "' or rd_newcarno ='"
          + wp.itemStr("ex_car_no") + "') ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " rd_moddate , " + " rd_seqno , " + " rd_modtype , "
//        + " decode(rd_modtype,'B','批次處理','OADD','線上登錄','OUPD','線上修改','V','語音/WEB') as tt_rd_modtype , "
        + " decode(rd_modtype,'B','批次處理','OADD','線上登錄','OUPD','線上修改','V','語音/WEB','O','online') as tt_rd_modtype , "
        + " card_no , " + " new_card_no , " + " rd_type , "
        + " decode(rd_type,'E','自費','F','免費') as tt_rd_type , " + " rd_carno , " + " rd_newcarno , "
        + " rd_validdate , " + " rd_status , "
//        + " decode(rd_status,'0','停用','1','新增車號','2','變更車號','3','白金卡取消車號') as tt_rd_status , "
        + " decode(rd_status,'0','停用','1','新增車號','2','變更車號','3','白金卡取消車號','4','未啟用') as tt_rd_status , "
        + " rd_payamt , " + " rd_stopdate , " + " rd_stoprsn , "
//        + " decode(rd_stoprsn,'1','到期不續購','2','未達免費續用標準','3','卡片已為無效卡','4','卡友來電要求停用','5','金卡提升為白金卡','6','未達年度續用標準','7','無車號且非自動登錄卡') as tt_rd_stoprsn , "
        + " decode(rd_stoprsn,'1','到期不續購','2','未達免費續用標準','3','卡片已為無效卡','4','卡友來電要求停用','5','金卡提升為白金卡','6','未達年度續用標準','7','無車號且非自動登錄卡','8','製卡進件停用') as tt_rd_stoprsn , "
        + " crt_user , " + " apr_date , " + " rd_senddate , " + " rd_sendsts , " + " proj_no , "
        + " purch_amt , " + " purch_cnt , " + " purch_amt_lyy , " + " cardholder_type ";
    wp.daoTable = "cms_roaddetail";
    wp.whereOrder = " order by rd_moddate Asc ";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(1);
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    String lsCardNo = "", lsCurrentCode = "", lsSupFlag = "", lsMsg = "";
    int liRc = 0;

    wp.listCount[0] = wp.itemRows("rd_moddate");

    lsCardNo = wp.itemStr2("ex_card_no");
    if (empty(lsCardNo)) {
      alertErr2("請輸入登錄卡號");
      return;
    }

    String sql1 = "select current_code , sup_flag from crd_card where card_no = ? ";
    sqlSelect(sql1, new Object[] {lsCardNo});

    if (sqlRowNum <= 0) {
      alertErr2("登錄卡號 不存在");
      return;
    }

    lsCurrentCode = sqlStr("current_code");
    lsSupFlag = sqlStr("sup_flag");

    if (eqIgno(lsCurrentCode, "0") == false) {
      alertErr2("卡號 已停用");
      return;
    }

    rdsm01.RdsFunc func = new rdsm01.RdsFunc();
    func.setConn(wp);

    liRc = func.hasFree(lsCardNo);
    if (liRc == -1) {
      alertErr2(func.getMsg());
      return;
    } else if (liRc == 0) {
      alertErr2("卡號 不符合免費道路救援參數");
      return;
    }

    // --消費金額
    liRc = func.checkRoadparm2();
    if (liRc == -1) {
      errmsg(func.getMsg());
      return;
    }
    lsMsg += "\\n1. 適用免費專案代號:" + func.lsProjNo;
    if (eqIgno(func.lsChType, "A")) {
      lsMsg += "\\n2. 卡友適用條件: A.首年";
    } else if (eqIgno(func.lsChType, "B")) {
      lsMsg += "\\n2. 卡友適用條件: B.非首年";
    } else if (eqIgno(func.lsChType, "C")) {
      lsMsg += "\\n2. 卡友適用條件: C.今年非首年";
    }

    lsMsg += "\\n3. 累計消費金額:" + func.idcPurchAmt;
    lsMsg += "\\n4. 累計消費筆數:" + func.ilPurchCnt;
    lsMsg += "\\n5. 去年累計消費金額:" + func.idcLastAmt;

    if (liRc == 0) {
      okAlert("持卡人消費資格不符參數" + lsMsg);
      wp.respMesg = "　";
    } else if (liRc == 1) {
      okAlert("卡號適用免費道路救援" + lsMsg);
      wp.respMesg = "　";
    }

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}

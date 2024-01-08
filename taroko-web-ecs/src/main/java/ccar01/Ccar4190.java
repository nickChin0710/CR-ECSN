package ccar01;
/** 特店風險註記管制明細表
 * 2019-0919   JH    modify
 *  V.2018-0730.jh
 * 109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard 
 * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *   
 * */

import ofcapp.BaseQuery;
import ofcapp.InfaceExcel;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;

public class Ccar4190 extends BaseQuery implements InfaceExcel {

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
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
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
    }



    dddwSelect();
    initButton();
    // list_wkdata();
  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.colStr("ex_acq_bank_id");
      dddwList("dddw_mbase_acqid", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where wf_type='ACQ_BANK_ID' and wf_id not in ('999999')");
    } catch (Exception ex) {
    }
  }


  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_mcht_no"), "mcht_no", "like%")
        + sqlCol(wp.itemStr("ex_mcc_code"), "mcc_code")
        + sqlCol(wp.itemStr("ex_acq_bank_id"), "acq_bank_id");
    if (wp.itemEq("ex_auth_flag", "Y")) {
      wp.whereStr += " and risk_end_date < to_char(sysdate,'yyyymmdd') ";
    }
    if (wp.itemEq("ex_auth_flag", "N")) {
      wp.whereStr +=
          " and risk_start_date<= to_char(sysdate,'yyyymmdd') and risk_end_date>= to_char(sysdate,'yyyymmdd') ";
    }
    if (wp.itemEmpty("ex_card_no") == false) {
      wp.whereStr += " and mcht_no in (select mcht_no from cca_mcht_risk_detl where 1=1 "+sqlCol(wp.itemStr("ex_card_no"),"data_code")+")";
    }

    if (!wp.itemEmpty("ex_risk_date")) {
      wp.whereStr += " and :risk_date between B.risk_start_date and risk_end_date ";
      setString("risk_date", wp.itemStr("ex_risk_date"));
    }

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "acq_bank_id," + "mcht_no, " + "mcht_name," + "risk_start_date , "
        + "risk_end_date , " + "mcht_risk_code , " + "auth_amt_s , " + "auth_amt_e , "
        + "auth_amt_rate,  " + "edc_pos_no1,  " + "edc_pos_no2,  " + "edc_pos_no3,  "
        + "risk_chg_date,  " + "risk_chg_user, " + "risk_remark as mcht_remark ";
    wp.daoTable = "cca_mcht_risk";
    wp.whereOrder = " order by acq_bank_id , mcht_no ";
    pageQuery();


    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
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
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "ccar4190";
      String cond1 = "";
      wp.colSet("cond_1", cond1);
      wp.colSet("user_id", wp.loginUser);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "ccar4190.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      listWkdata();
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }

  }

  void listWkdata() throws Exception {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_risk_date", commString.strToYmd(wp.colStr(ii, "risk_start_date")) + " -- "
          + commString.strToYmd(wp.colStr(ii, "risk_end_date")));
      wp.colSet(ii, "wk_auth_amt",
          wp.colStr(ii, "auth_amt_s") + " -- " + wp.colStr(ii, "auth_amt_e"));
      wp.colSet(ii, "wk_edc_pos", wp.colStr(ii, "edc_pos_no1") + "," + wp.colStr(ii, "edc_pos_no2")
          + "," + wp.colStr(ii, "edc_pos_no3"));
    }
  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }

}

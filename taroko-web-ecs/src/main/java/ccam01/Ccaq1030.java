package ccam01;
/*卡戶交易記錄查詢[w_auth_txlog] V.2018-0423-JH
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * * 109-01-04  V1.00.01   shiyuqi       修改无意义命名
* 110-01-05  V1.00.02  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *  *  
 */
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ccaq1030 extends ofcapp.BaseAction implements InfaceExcel {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  String cardNo, txDate, txTime;
  Ccaq1030Func func;
  ofcapp.AppMsg appmsg = new ofcapp.AppMsg();

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
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 新增功能 */
      // if (item_empty("rowid")) {
      // is_action = "A";
      // insertFunc();
      // } else {
      // /* 更新功能 */
      // is_action = "U";
      // updateFunc();
      // }
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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -PDF-
      strAction = "XLS";
      xlsPrint();
    }

  }

  @Override
  public void initPage() {
    wp.colSet("ex_tx_date1", commDate.sysDate());
    wp.colSet("ex_tx_date2", commDate.sysDate());
  }

  @Override
  public void queryFunc() throws Exception {
    // if (item_empty("ex_idno") && item_empty("ex_card_no")) {
    // err_alert("[身分證ID, 卡號] 不可同時空白");
    // return;
    // }
    if (wp.itemEmpty("ex_tx_time1") == false || wp.itemEmpty("ex_tx_time2") == false) {
      if (wp.itemEmpty("ex_tx_date1")) {
        this.alertErr("[交易日期-起] 不可空白");
        return;
      }
    }

    if (!wp.itemEmpty("ex_amt1") && !wp.itemEmpty("ex_amt2")) {
      if (wp.itemNum("EX_AMT1") > wp.itemNum("EX_AMT2")) {
        alertErr2("消費金額起迄錯誤");
        return;
      }
    }

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    func = new Ccaq1030Func();
    func.setConn(wp);

    rc = func.querySelect();
    // wp =(taroko.com.TarokoCommon) func.wp;
    // ddd("cnt="+wp.selectCnt);

    wp.setListSernum(1, "");
    if (wp.selectCnt == 0) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    txDate = wp.itemStr("data_k2");
    txTime = wp.itemStr("data_k3");

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNo)) {
      cardNo = wp.itemStr("card_no");
    }
    if (empty(txDate)) {
      txDate = wp.itemStr("tx_date");
    }
    if (empty(txTime)) {
      txTime = wp.itemStr("tx_time");
    }
    wp.sqlCmd = "select A.*, " + " uf_idno_id(A.id_p_seqno) as db_idno,    "
        + " substr(A.pos_mode,1,2) as pos_mode_12," + " substr(A.pos_mode,3,1) as pos_mode_3,"
        + " '' as db_service_code, " + " uf_idno_name(A.id_p_seqno) as db_idno_name "
        + ", uf_tt_ccas_parm3('LOGICDEL',A.logic_del) as tt_logic_del"
        + ", (select sys_data1 from cca_sys_parm3 where sys_id = 'AUTHUNIT' and sys_key =A.auth_unit) as tt_auth_unit "
        + ", decode(A.online_redeem,'A','分期 (A)','I','分期 (I)','E','分期 (E)','Z','分期 (Z)','0','紅利 (0)','1','紅利 (1)','2','紅利 (2)'"
        + "'3','紅利 (3)','4','紅利 (4)','5','紅利 (5)','6','紅利 (6)','7','紅利 (7)','') as tt_online_redeem "
        + " from cca_auth_txlog A" + " where A.card_no =?" + " and A.tx_date =? and A.tx_time =?";
    Object[] param = new Object[] {cardNo, txDate, txTime};
    this.pageSelect(param);
    if (sqlRowNum <= 0) {
      alertErr2("授權交易: not find; " + this.sqlErrtext);
    }
    return;
  }


  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.colStr("ex_entry_type");
      dddwList("dddw_entry_type", "select distinct entry_type as db_code , "
          + "entry_type as db_desc from cca_entry_mode where 1=1 ");

      wp.optionKey = wp.colStr("ex_entry_mode");
      dddwList("dddw_entry_mode", "select distinct entry_mode as db_code , "
          + "entry_mode as db_desc from cca_entry_mode where 1=1 ");

    } catch (Exception ex) {
    }

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "ccaq1030";
      String cond1 = "";
      cond1 = "日期(起):" + commString.strToYmd(wp.itemStr("ex_tx_date1")) + " 日期(迄):"
          + commString.strToYmd(wp.itemStr("ex_tx_date2"));
      wp.colSet("cond1", cond1);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "ccaq1030.xlsx";
      wp.pageRows = 9999;
      queryRead();
      // wp.setListCount(1);
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }

  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }


}

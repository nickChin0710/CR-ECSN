package ccam01;
/**ccam2017 線上持卡人信用額度調整
 * 2020-0107:  Ru    modify AJAX
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 **/

import busi.func.OutgoingOppo;
/**非本行發行卡片停用 V.190311-JH
   190311:     JH    outgoing_Query()
   2018-1112:  JH    outgoing_query()
 * 
 * */
import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Ccam2017 extends BaseAction {
  String cardNo = "";
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  OutgoingOppo ooOutgo = new OutgoingOppo();

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    }
    // else if (eq_igno(wp.buttonCode, "Q")) {
    // /* 查詢功能 */
    // is_action = "Q";
    // queryFunc();
    // }
    else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      if (wp.itemEmpty("rowid"))
        strAction = "A";
      else
        strAction = "U";
      saveFunc();
    }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    }
    // else if (eq_igno(wp.buttonCode, "M")) {
    // /* 瀏覽功能 :skip-page */
    // queryRead();
    // }
    // else if (eq_igno(wp.buttonCode, "S")) {
    // /* 動態查詢 */
    // querySelect();
    // }
    else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R";
      cardNo = wp.colStr("card_no");
      if (isEmpty(cardNo)) {
        alertErr("卡號: 不可空白");
        return;
      }
     // outgoingQuery();
    }
    // else if (eq_igno(wp.buttonCode, "C")) {
    // // -資料處理-
    // procFunc();
    // }
    // 20200107 modify AJAX
    else if (eqIgno(wp.buttonCode, "AJAX")) {
      if ("1".equals(wp.getValue("ID_CODE"))) {
        wfAjaxOpptype();
      }
    }

  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.colStr("oppo_reason");
      this.dddwList("dddw_oppo_reason", "cca_opp_type_reason", "opp_status", "opp_remark",
          " where onus_opp_type='5'");

      if (wp.colEq("bin_type", "V")) {
        wp.optionKey = wp.colStr("vis_area_1");
        dddwList("dddw_visa_area1", "cca_sys_parm2", "sys_key", "sys_data1",
            " where sys_id='VISA' and sys_key in('0','A','B','C','D','E','F')");
        wp.optionKey = wp.colStr("vis_area_2");
        dddwShare("dddw_visa_area2");
        wp.optionKey = wp.colStr("vis_area_3");
        dddwShare("dddw_visa_area3");
        wp.optionKey = wp.colStr("vis_area_4");
        dddwShare("dddw_visa_area4");
        wp.optionKey = wp.colStr("vis_area_5");
        dddwShare("dddw_visa_area5");
        wp.optionKey = wp.colStr("vis_area_6");
        dddwShare("dddw_visa_area6");
        wp.optionKey = wp.colStr("vis_area_7");
        dddwShare("dddw_visa_area7");
        wp.optionKey = wp.colStr("vis_area_8");
        dddwShare("dddw_visa_area8");
        wp.optionKey = wp.colStr("vis_area_9");
        dddwShare("dddw_visa_area9");
      } else if (wp.colEq("bin_type", "M")) {
        wp.optionKey = wp.colStr("mast_area_1");
        dddwList("dddw_mast_area1", "cca_sys_parm2", "sys_key", "sys_data1",
            " where sys_id='MAST'");
        wp.optionKey = wp.colStr("mast_area_2");
        dddwShare("dddw_mast_area2");
        wp.optionKey = wp.colStr("mast_area_3");
        dddwShare("dddw_mast_area3");
        wp.optionKey = wp.colStr("mast_area_4");
        dddwShare("dddw_mast_area4");
        wp.optionKey = wp.colStr("mast_area_5");
        dddwShare("dddw_mast_area5");
        wp.optionKey = wp.colStr("mast_area_6");
        dddwShare("dddw_mast_area6");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    cardNo = itemkk("card_no");
    if (empty(cardNo)) {
      alertErr2("卡號: 不可空白");
      return;
    }

    if (cardNo.length() < 15) {
      alertErr2("卡號: 至少須要15位 !");
      return;
    }

    String sql1 = " select count(*) as cnt1 from cca_card_base where card_no =? ";
    setString2(1, cardNo);
    sqlSelect(sql1);
    if (sqlNum("cnt1") > 0) {
      alertErr2("卡號已存在, 請由其他程式停掛");
      return;
    }

    daoTid = "AA.";
    String sql2 = " select bin_type, debit_flag from ptr_bintable where 1=1" + commSqlStr.whereBinno
        + commSqlStr.rownum(1);
    sqlSelect(sql2, new Object[] {cardNo});
    if (sqlRowNum <= 0) {
      alertErr2("非本行BIN NUMBER 之卡號無法作業");
      return;
    }

    wp.sqlCmd = "select hex(A.rowid) as rowid, A.*" + ", visa_resp_code as vmj_resp_code"
        + ", mst_reason_code as neg_reason_code" + ", vis_reason_code as vmj_reason_code"
        + " from cca_opposition A" + " where card_no =? and logic_del<>'Y'";

    setString2(1, cardNo);
    pageSelect();
    if (sqlRowNum <= 0) {
      wp.colSet("card_no", cardNo);
      wp.colSet("bin_type", sqlStr("AA.bin_type"));
      wp.colSet("debit_flag", sqlNvl("AA.debit_flag", "N"));
      wp.colSet("oppo_date", this.getSysDate());
      wp.colSet("neg_del_date", commDate.dateAdd(this.getSysDate(), 0, 2, 0));
      // --
      wp.colClear(0, "oppo_reason");
      wp.colClear(0, "excep_flag");
      wp.colClear(0, "opp_remark");
      detlWkdata();
      this.selectOK();
      return;
    }

    detlWkdata();
    outgoingClear();
    // outgoing_Query();
  }

  void outgoingClear() {
    ooOutgo.setConn(wp);
    ooOutgo.wpCallStatus("");
  }

  void outgoingQuery() {
    ooOutgo.setConn(wp);
    ooOutgo.wpCallStatus("");
    if (empty(cardNo))
      return;
    // -NEG-
    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = wp.colStr("bin_type");
    ooOutgo.oppoNegId("5");
    // -VMJ-
    String lsBinType = wp.colStr("bin_type");
    String lsOppoDate = wp.colStr("oppo_date");
    // String ls_neg_reason =wp.col_ss("mst_reason_code");
    String lsVisReason = wp.colStr("vis_reason_code");
    String lsArea = "";

    ooOutgo.p4Reason = lsVisReason;
    if (eqIgno(lsBinType, "M")) {
      ooOutgo.oppoMasterReq2("5");
    } else if (eqIgno(lsBinType, "J")) {
      lsArea = wp.colStr("vis_area_1") + wp.colStr("vis_area_2") + wp.colStr("vis_area_3")
          + wp.colStr("vis_area_4") + wp.colStr("vis_area_5");
      ooOutgo.p5DelDate = lsOppoDate;
      ooOutgo.p7Region = lsArea;
      ooOutgo.oppoJcbReq("5");
    } else {
      lsArea = wp.colStr("vis_area_1") + wp.colStr("vis_area_2") + wp.colStr("vis_area_3")
          + wp.colStr("vis_area_4") + wp.colStr("vis_area_5") + wp.colStr("vis_area_6")
          + wp.colStr("vis_area_7") + wp.colStr("vis_area_8") + wp.colStr("vis_area_9");
      ooOutgo.p5DelDate = lsOppoDate;
      if (empty(lsArea)) {
        ooOutgo.p4Reason = "41";
        ooOutgo.p7Region = "0" + commString.space(8);
      } else {
        ooOutgo.p7Region = lsArea;
      }
      ooOutgo.oppoVisaReq("5");
    }
  }

  void detlWkdata() {
    wp.colSet("oppo_reason", wp.colStr("oppo_status"));
    if (wp.colEq("bin_type", "V")) {
      if (wp.colEmpty("vis_purg_date_1"))
        wp.colSet("vis_purg_date_1", commDate.dateAdd(this.getSysDate(), 0, 2, 0));
      else
        wp.colSet("vis_purg_date_1", wp.colStr("vis_purg_date_1"));
    } else if (wp.colEq("bin_type", "M")) {
      for (int ii = 1; ii <= 6; ii++) {
        wp.colSet("mast_area_" + ii, wp.colStr("vis_area_" + ii));
        wp.colSet("mast_date" + ii, wp.colStr("vis_purg_date_" + ii));
      }
    } else if (wp.colEq("bin_type", "J")) {
      if (wp.colEmpty("vis_purg_date_1"))
        wp.colSet("jcb_date1", commDate.dateAdd(this.getSysDate(), 0, 2, 0));
      else
        wp.colSet("jcb_date1", wp.colStr("vis_purg_date_1"));
      for (int ii = 1; ii <= 5; ii++) {
        wp.colSet("jcb_area_" + ii, wp.colStr("vis_area_" + ii));
      }
    }
  }


  @Override
  public void saveFunc() throws Exception {
    Ccam2017Func func = new Ccam2017Func();
    func.setConn(wp);
    String lsCardNo = wp.itemStr("card_no");
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
      return;
    }
    if (this.isAdd()) {
      alertMsg("[非本行卡停用] 成功");
    } else if (this.isDelete()) {
      this.saveAfter(false);
      wp.colSet("card_no", lsCardNo);
      alertMsg("[非本行卡撤掛] 成功");
    }


  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    this.btnAddOn(!wp.colEmpty("card_no"));
    this.btnDeleteOn(!wp.colEmpty("rowid"));
  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  // 20200107 modify AJAX
  public void wfAjaxOpptype() throws Exception {
    // super.wp = wr;

    if (itemIsempty("ax_oppo")) {
      return;
    }

    // String ls_winid =
    selectOpptypeReason(wp.itemStr("ax_oppo"));
    if (rc != 1) {
      return;
    }
    wp.log("bin_type[%s],V[%s],M[%s],J[%s]", wp.itemStr2("ax_bin_type"), sqlStr("visa_code"),
        sqlStr("mast_code"), sqlStr("jcb_code"));

    if (itemEq("ax_bin_type", "V") && empty(sqlStr("visa_code")) == false) {
      wp.addJSON("excep_flag", "V");
    } else if (itemEq("ax_bin_type", "M") && empty(sqlStr("mast_code")) == false) {
      wp.addJSON("excep_flag", "M");
    } else if (itemEq("ax_bin_type", "J") && empty(sqlStr("jcb_code")) == false) {
      wp.addJSON("excep_flag", "J");
    }
  }

  void selectOpptypeReason(String lsOppo) {

    String sql1 = "select vis_excep_code as visa_code,"
        + " mst_auth_code as mast_code, jcb_excp_code as jcb_code" + " from cca_opp_type_reason"
        + " where 1=1 " + " and opp_status =?";
    sqlSelect(sql1, new Object[] {lsOppo});

  }

}

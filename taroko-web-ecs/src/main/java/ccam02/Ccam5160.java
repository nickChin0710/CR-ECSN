package ccam02;

/*
 * 停掛原因碼維護　exc_reason_code
 * V00.0		Alex		2017-0821: initial
 * V1.00.01    yanghan  2020-04-20   修改了變量名稱和方法名稱*
 * */
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5160 extends BaseEdit {
  String oppStatus = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    wp = wr;
    rc = 1;
    wp.logActive();

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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ccam5160")) {
        wp.optionKey = wp.itemStr("ex_opp_type");
        ddlbList("ddw_ex_opp_type", wp.colStr("ex_opp_type"), "ecsfunc.DeCodeCcas.onusOpptype");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "ccam5160_detl")) {
        wp.optionKey = wp.colStr("onus_opp_type");
        ddlbList("ddw_onus_opp_type", "ecsfunc.DeCodeCcas.onusOpptype");

        wp.optionKey = wp.colStr("ncc_opp_type");
        ddlbList("ddw_ncc_opp_type", "ecsfunc.DeCodeCcas.ncccOpptype");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "ccam5160_detl")) {
        wp.optionKey = wp.colStr("neg_opp_reason");
        dddwList("dw_sysparm1_nccc_list", "cca_sys_parm1", "sys_key", "sys_data1",
            "where sys_id= 'NCCC'");

        wp.optionKey = wp.colStr(0, "vis_excep_code");
        dddwList("dw_sysparm1_visa_list", "cca_sys_parm1", "sys_key", "sys_data1",
            "where sys_id= 'VISA'");

        wp.optionKey = wp.colStr("mst_auth_code");
        dddwList("dw_sysparm1_mast_list", "cca_sys_parm1", "sys_key", "sys_data1",
            "where sys_id= 'MAST'");

        wp.optionKey = wp.colStr("jcb_excp_code");
        dddwList("dw_sysparm1_jcb_list", "cca_sys_parm1", "sys_key", "sys_data1",
            "where sys_id= 'JCB'");

      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_opp_status"), "opp_status", "like%")
        + sqlCol(wp.itemStr("ex_opp_type"), "onus_opp_type");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "opp_status,   " + "onus_opp_type, " + "ncc_opp_type," + "neg_opp_reason,"
        + "vis_excep_code," + "mst_auth_code," + "jcb_excp_code," + "opp_remark,"
        + "uf_tt_ccas_parm3('OPPTYPE',onus_opp_type) as tt_onus_opp_type,"
        + "uf_tt_ccas_parm3('OPPTYPE',ncc_opp_type) as tt_ncc_opp_type," + sqlModDate + ", "
        + " mod_user , " + " jcic_opp_reason , " + " fisc_opp_code , " + " ctrl_code ";
    wp.daoTable = "CCA_OPP_TYPE_REASON";
    wp.whereOrder = " order by opp_status";

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    oppStatus = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(oppStatus)) {
      oppStatus = itemKk("opp_status");
    }
    if (empty(oppStatus)) {
      alertErr("停掛原因：不可空白");
      return;
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "opp_status,   " + "onus_opp_type, "
        + "ncc_opp_type, " + "neg_opp_reason, " + "vis_excep_code, " + "mst_auth_code, "
        + "jcb_excp_code, " + "opp_remark, " + "crt_date," + "mod_user, "
        + "uf_tt_ccas_parm3('OPPTYPE',onus_opp_type) as tt_onus_opp_type, " + sqlModDate + ", "
        + " crt_user , "+ " jcic_opp_reason , " + " fisc_opp_code , " + " ctrl_code ";
    wp.daoTable = "CCA_OPP_TYPE_REASON";
    wp.whereStr = "where 1=1" + sqlCol(oppStatus, "opp_status");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + oppStatus);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    Ccam5160Func func = new Ccam5160Func();
    func.setConn(wp);
    if (checkApproveZz() == false) {
      return;
    }
    rc = func.dbSave(strAction);
    this.sqlCommit(rc);

    if (rc != 1) {
      alertErr2(func.getMsg());
    }

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}

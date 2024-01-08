/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112-01-03 V1.00.01  machao         Initial                              *
***************************************************************************/
package mktp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0520 extends BaseProc {
  private String PROGNAME = "推廣人員推廣人員資料覆核2023/01/03 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0520Func func = null;
  String rowid;// kk2;
  String groupCode, cardType;
  String fstAprFlag = "";
  String orgTabName = "crd_employee_a_t";
  String controlTabName = "";

  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "C")) {// 資料處理 -/
      strAction = "A";
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
	// -page control-
      wp.queryWhere = wp.whereStr;
      wp.setQueryMode();

      queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
	 
    wp.pageControl();

    wp.selectSQL = " "
            + "hex(rowid) as rowid, "
            + "corp_no, "
            + "a.aud_type, "
            + "a.subsidiary_no, "
            + "a.employ_no, "
            + "a.chi_name, "
            + "a.id, "
            + "a.acct_no, "
            + "a.unit_no, "
            + "a.unit_name, "
            + "a.subunit_no, "
            + "a.subunit_name, "
            + "a.position_id, "
            + "a.position_name, "
            + "a.status_id, "
            + "a.status_name, "
            + "a.description, "
            + "a.file_name, "
            + "a.apr_user, "
            + "a.apr_flag, "
            + "a.apr_date, "
            + "a.crt_user, "
            + "a.crt_date, "
            + "a.mod_user, "
            + "a.mod_time";

    wp.daoTable = orgTabName + " a ";
    wp.whereStr = " where a.apr_flag <> 'Y' and a.error_code = '00' or a.error_code = '' " ;
    if (!wp.itemEmpty("ex_corp_no")) {
        wp.whereStr += " and a.corp_no = ? ";
        setString(wp.itemStr("ex_corp_no"));
    }
    if (!wp.itemEmpty("ex_office_code")) {
        wp.whereStr += " and a.subsidiary_no = ? ";
        setString(wp.itemStr("ex_office_code"));
    }

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commfuncAudType("aud_type");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
  }

  // ************************************************************************
  void listWkdata() throws Exception {
  }
  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp01.Mktp0520Func func = new mktp01.Mktp0520Func(wp);

    String[] lsCorpNo = wp.itemBuff("corp_no");
    String[] lsEmployNo = wp.itemBuff("employ_no");
    String[] lsChiName = wp.itemBuff("chi_name");
    String[] lsSubunitNo = wp.itemBuff("subunit_no");
    String[] lsStatusId = wp.itemBuff("status_id");
    String[] lsAudType = wp.itemBuff("aud_type");
    String[] lsSubsidiaryNo = wp.itemBuff("subsidiary_no");
    String[] lsModUser = wp.itemBuff("mod_user");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsAudType.length;

    int rr = -1;
    wp.selectCnt = lsAudType.length;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0)
        continue;
      wp.log("" + ii + "-ON." + lsRowid[rr]);

      wp.colSet(rr, "ok_flag", "-");
      if (lsModUser[rr].equals(wp.loginUser)) {
        ilErr++;
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }

      func.varsSet("corp_no", lsCorpNo[rr]);
      func.varsSet("employ_no", lsEmployNo[rr]);
      func.varsSet("chi_name", lsChiName[rr]);
      func.varsSet("subunit_no", lsSubunitNo[rr]);
      func.varsSet("status_id", lsStatusId[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("subsidiary_no", lsSubsidiaryNo[rr]);
      func.varsSet("mod_user", lsModUser[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A")) {
        rc = func.dbInsertA4();
        if (rc == 1)
          rc = func.dbDeleteD4T();
      } else if (lsAudType[rr].equals("U")) {
        rc = func.dbUpdateU4();
        if (rc == 1)
          rc = func.dbDeleteD4T();
      } else if (lsAudType[rr].equals("D")) {
        rc = func.dbDeleteD4();
        if (rc == 1)
          rc = func.dbDeleteD4T();
      }

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commfuncAudType("aud_type");

        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
      try {
          wp.initOption = "--";
          wp.optionKey = "";
          if (wp.colStr("ex_corp_no").length() > 0) {
              wp.optionKey = wp.colStr("ex_corp_no");
          }
          this.dddwList("dddw_mkt_office_m", "mkt_office_m", "corp_no", "office_m_name",
                  "where 1=1 order by corp_no");

          wp.initOption = "--";
          wp.optionKey = "";
          if (wp.colStr("ex_office_code").length() > 0) {
              wp.optionKey = wp.colStr("ex_office_code");
          }
          this.dddwList("dddw_mkt_office_d", "mkt_office_d", "office_code", "office_name",
                  "where 1=1 order by office_code");
      } catch (Exception ex) {
      }
  }
  // ************************************************************************
  void commfuncAudType(String cde1) {
    if (cde1 == null || cde1.trim().length() == 0)
      return;
    String[] cde = {"Y", "A", "U", "D"};
    String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "comm_func_" + cde1, "");
      for (int inti = 0; inti < cde.length; inti++)
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
          break;
        }
    }
  }

  // ************************************************************************

}  // End of class

package cmsm01;
/**FANCY卡帳務明細查詢
 * 19-0613:    JH    p_xxx >>acno_p_xxx
 * 109-04-27   shiyuqi       updated for project coding standard     *  
 * */
import ofcapp.BaseAction;

public class Cmsq0020 extends BaseAction {
  cmsm01.Cmsq0020Func func = new cmsm01.Cmsq0020Func();

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
    }
    // else if (eq_igno(wp.buttonCode, "A")) {
    // /* 新增功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "D")) {
    // /* 刪除功能 */
    // saveFunc();
    // }
    else if (eqIgno(wp.buttonCode, "M")) {
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
    try {
      wp.optionKey = wp.colStr("ex_acct_type");
      dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1 ");
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (itemallEmpty("ex_acct_key,ex_card_no".split(","))) {
      alertErr2("帳戶帳號 及 卡號  不可全部空白 !");
      return;
    }

    // --ex_acno_name
    String sql1 = " select " + " uf_acno_name(acno_p_seqno) as ex_acno_name , " + " combo_acct_no "
        + " from act_acno " + " where 1=1 ";

    if (!empty(wp.itemStr("ex_acct_key"))) {
      sql1 += " and acct_key =? and acct_type =?";
      sqlSelect(sql1, new Object[] {commSqlStr.acctKey(wp.itemStr("ex_acct_key")),
          wp.itemNvl("ex_acct_type", "01")});
    } else if (!empty(wp.itemStr("ex_card_no"))) {
      sql1 += " and acno_p_seqno in (select acno_p_seqno from crd_card where card_no = ? ) ";
      sqlSelect(sql1, new Object[] {wp.itemStr("ex_card_no")});
    }

    if (sqlRowNum <= 0) {
      alertErr2("持卡人資料不存在");
      return;
    }

    if (empty(sqlStr("combo_acct_no"))) {
      alertErr2("無法取得卡戶之歡喜卡帳號");
      return;
    }

    wp.colSet("ex_combo_acct_no", sqlStr("combo_acct_no"));
    wp.colSet("ex_acno_name", sqlStr("ex_acno_name"));

    // --check employee
    func.setConn(wp);
    if (wfChkEmployee() != 1) {
      rc = func.wfAddLog("3", "在職員工");
      sqlCommit(rc);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
      return;
    }

    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    // --queryRead
    rc = func.dataProc();
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
  }

  public int wfChkEmployee() throws Exception {
    String lsId = "";

    if (empty(wp.itemStr("ex_acct_key"))) {
      String sql1 = " select uf_idno_id(A.id_p_seqno) as id_no " + " from crd_card A "
          + " where A.card_no = ? ";
      sqlSelect(sql1, new Object[] {wp.itemStr2("ex_card_no")});
      if (sqlRowNum <= 0) {
        alertErr2("卡號不存在");
        return -1;
      }
      lsId = sqlStr("id_no");
    } else {
      lsId = commString.left(wp.itemStr2("ex_acct_key"), 10);
    }

    if (empty(lsId.trim())) {
      alertErr2("查無卡人 身分證字號");
      return -1;
    }

    // --check 在職員工
    busi.func.CrdFunc ecs = new busi.func.CrdFunc();
    ecs.setConn(wp);
    if (ecs.employeeStatus(lsId)) {
      alertErr2("在職員工  不可查詢 !");
      return -1;
    }

    return 1;
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
    // TODO Auto-generated method stub

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
  public void initPage() {
    // TODO Auto-generated method stub

  }

}

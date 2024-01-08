package cmsm03;
/** 
 * 2020-0131: JustinWu add new column vip_kind
 * 2019-1205:  Alex  add initButton
 * 2019-0614:  JH    p_xxx >>acno_pxxx
  109-04-20   shiyuqi       updated for project coding standard     *
 * */
import ofcapp.BaseAction;

public class Cmsm3130 extends BaseAction {
  String cardNo = "", vipKind = "";

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        /* 更新功能 */
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        procFunc();
        break;
      default:
        break;
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_idno") && wp.itemEmpty("ex_pp_card_no") && wp.itemEmpty("ex_vip_kind")) {
      alertErr2("請輸入 身分證ID or 貴賓卡號 or 貴賓卡");
      return;
    }

    if (!empty(wp.itemStr("ex_idno"))) {
      if (wp.itemStr("ex_idno").length() <= 5) {
        alertErr2("身分證ID:至少5碼");
        return;
      }
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_pp_card_no"), "pp_card_no", "like%");

    if (notEmpty(wp.itemStr("ex_idno"))) {
      lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where id_no like '"
          + wp.itemStr("ex_idno") + "%')";
    }
    if (notEmpty(wp.itemStr("ex_vip_kind"))) {
      lsWhere += sqlCol(wp.itemStr("ex_vip_kind"), "vip_kind");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " vip_kind, " + " pp_card_no ," + " current_code ," + " bin_type ,"
        + " oppost_date ," + " oppost_reason ," + " uf_idno_id(id_p_seqno) as db_idno ,"
        + " uf_idno_name(id_p_seqno) as db_chi_name ," + " valid_to ," + " stop_apply_no ,"
        + " id_p_seqno , "
        + " (select wf_desc from ptr_sys_idtab where wf_type = 'PPCARD_OPPOST_REASON' and wf_id = oppost_reason ) as tt_oppost_reason ";
    wp.daoTable = "crd_card_pp ";
    wp.whereOrder = " order by 1";
    logSql();
    pageQuery();
    // queryAfter();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();
    queryAfter();

  }

  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    vipKind = wp.itemStr("data_k2");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNo)) {
      cardNo = itemkk("pp_card_no");
    }

    if (empty(cardNo)) {
      alertErr2("貴賓卡號:不可空白");
      return;
    }

    if (empty(vipKind)) {
      vipKind = itemkk("vip_kind");
    }

    wp.selectSQL = " " + " vip_kind ," + " pp_card_no ," + " uf_idno_id(id_p_seqno) as db_idno ,"
        + " uf_idno_name(id_p_seqno) as db_chi_name ," + " current_code ,"
        + " valid_to as end_date ," + " oppost_date ," + " oppost_reason ," + " stop_apply_no ,"
        + " id_p_seqno ," + " 'N' as cancel_flag ," + " card_no , " + " 'N' as reissue_flag ," + " mod_user , "
        + " to_char(mod_time,'yyyymmdd') as mod_date ";
    wp.daoTable = " crd_card_pp ";
    wp.whereStr = " where 1=1 " + sqlCol(cardNo, "pp_card_no") + sqlCol(vipKind, "vip_kind");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNo);
      return;
    }
    if (notEmpty(wp.colStr("stop_apply_no"))) {
      selectStop();
    }
    // --掛失處理旗標
    if (wp.colEq("proc_flag", "Y")) {
      wp.colSet("tt_proc_flag", "已處理");
    } else {
      wp.colSet("tt_proc_flag", "未處理");
    }
    queryAfter();
  }

  void queryAfter() {

    for (int i = 0; i < wp.selectCnt; i++) {

      // 貴賓卡
      switch (wp.colStr(i, "vip_kind")) {
        case "1":
          wp.colSet(i, "vip_kind_desc", "1_新貴通卡");
          break;
        case "2":
          wp.colSet(i, "vip_kind_desc", "2_龍騰卡");
          break;
      }

    }
  }

  void selectStop() {
    String sql1 = "select " + " oppost_remark ," + " proc_flag ," + " proc_date ," + " crt_date ,"
        + " crt_user ," + " crt_time ," + " card_no ," + " reissue_flag ," + " hex(rowid) as rowid,"
        + " cancel_flag ," + " new_pp_card_no " + " from crd_ppcard_stop "
        + " where stop_apply_no =? " + "and vip_kind =? "
        + " order by crt_date Desc, crt_time Desc " + " fetch first 1  rows only ";
    sqlSelect(sql1, new Object[] {wp.colStr("stop_apply_no"), wp.colStr("vip_kind")});
    if (sqlRowNum <= 0) {
      return;
    }
    wp.colSet("oppost_remark", sqlStr("oppost_remark"));
    wp.colSet("proc_flag", sqlStr("proc_flag"));
    wp.colSet("proc_date", sqlStr("proc_date"));
    wp.colSet("crt_date", sqlStr("crt_date"));
    wp.colSet("crt_user", sqlStr("crt_user"));
    wp.colSet("crt_time", sqlStr("crt_time"));
    wp.colSet("card_no", sqlStr("card_no"));
    wp.colSet("reissue_flag", sqlStr("reissue_flag"));
    wp.colSet("rowid", sqlStr("rowid"));
    wp.colSet("cancel_flag", sqlStr("cancel_flag"));
    wp.colSet("new_pp_card_no", sqlStr("new_pp_card_no"));
  }

  @Override
  public void saveFunc() throws Exception {
    cmsm03.Cmsm3130Func func = new cmsm03.Cmsm3130Func();
    func.setConn(wp);
    if (empty(wp.itemStr("stop_apply_no"))) {
      rc = func.dbInsert();
    } else {
      rc = func.dbSave(strAction);
    }
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else {
      this.saveAfter(false);
//      dataRead();
    }
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    this.btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}

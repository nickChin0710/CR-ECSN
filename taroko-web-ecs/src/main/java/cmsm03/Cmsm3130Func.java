package cmsm03;
/** 
 * 2020-0131:  JustinWu add new column vip_kind, and check if the card is being made
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * 109-04-20  shiyuqi       updated for project coding standard     *
 * */
import busi.FuncAction;

public class Cmsm3130Func extends FuncAction {
  String isApplyNo = "";

  @Override
  public void dataCheck() {
    if (notEmpty(wp.itemStr("stop_apply_no"))) {
      if (checkData() == false) {
        errmsg("停掛記錄 已不存在, 請重新讀取資料");
        return;
      }
    }

    if (!ibAdd) {
      if (wp.itemEq("proc_flag", "Y")) {
        errmsg("資料已處理, 不可再修改");
        return;
      }
      log("Date:" + this.getSysDate());
      log("crt:" + wp.itemStr("crt_date"));
      if (!wp.itemEq("crt_date", this.getSysDate())) {
        errmsg("停掛產生日期 不是今天, 不可存檔");
        return;
      }
    }

    if (this.ibAdd && wp.itemEq("cancel_flag", "Y")) {
      errmsg("無人工停掛記錄 不可撤掛");
      return;
    }

    if (wp.itemEq("cancel_flag", "Y")) {
      if (wfCheckCancel() == false) {
        rc = -1;
        return;
      }
    } else {
      if (ibAdd) {
        if (checkStop() == false) {
          errmsg("此卡號已停掛, 不可重複停掛");
          rc = -1;
          return;
        }
      }


      if (wfCheckStop() == false) {
        rc = -1;
        return;
      }
    }

  }

  // 檢查是否製卡中
  boolean isCardBeingMade() {
    String sql1 = "" + " select count(*) as db_cnt " + " from crd_card_pp "
        + " where  pp_card_no =? " + " and vip_kind =?" + " and ( reissue_status in ('1','2') "
        + " or change_status in ('1','2') )";
    sqlSelect(sql1, new Object[] {wp.itemStr("pp_card_no"), wp.itemStr("vip_kind")});

    if (colNum("db_cnt") == 1) {
      return true;
    } else {
      return false;
    }
  }

  boolean checkData() {
    String sql1 = "select count(*) as db_cnt " + " from crd_ppcard_stop "
        + " where stop_apply_no =? " + "and vip_kind =? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("stop_apply_no"), wp.itemStr("vip_kind")});

    if (colNum("db_cnt") <= 0)
      return false;
    return true;
  }

  boolean wfCheckCancel() {
    if (checkData() == false) {
      errmsg("讀不到人工停掛記錄 不可撤掛");
      return false;
    }

    if (!wp.itemEq("crt_date", this.getSysDate())) {
      errmsg("停掛產生日期 不是今天, 不可存檔");
      return false;
    }

    if (isCardBeingMade()) {
      errmsg("製卡中，無法撤掛");
      return false;
    }

    if (!empty(wp.itemStr("new_pp_card_no"))) {
      errmsg("停掛補發 已製卡, 不可撤掛");
      return false;
    }

    return true;
  }

  boolean wfCheckStop() {
    if (empty(wp.itemStr("oppost_reason"))) {
      errmsg("停掛原因 不可空白");
      return false;
    }

    return true;
  }

  boolean checkStop() {
    String sql1 = "select count(*) as db_cnt " + " from crd_card_pp " + " where pp_card_no =? "
        + "and vip_kind =? " + " and nvl(current_code,'0') ='0' ";
    sqlSelect(sql1, new Object[] {wp.itemStr("pp_card_no"), wp.itemStr("vip_kind")});

    if (colNum("db_cnt") == 0)
      return false;
    return true;
  }


  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    // -get stop_apply_no-
    strSql =
        "select lpad(varchar(ecs_modseq.nextval),10,'0') as stop_apply_no from" + commSqlStr.sqlDual;
    this.sqlSelect(strSql);
    isApplyNo = colStr("stop_apply_no");
    wp.itemSet("stop_apply_no", isApplyNo);

    strSql = "insert into crd_ppcard_stop (" + " stop_apply_no ," + " pp_card_no ," + " vip_kind ,"
        + " id_p_seqno ," + " new_pp_card_no ," + " current_code ," + " end_date ,"
        + " oppost_date ," + " oppost_reason ," + " oppost_remark ," + " lost_fee ," + " card_no ,"
        + " reissue_flag ," + " cancel_flag ," + " proc_flag ," + " crt_date ," + " crt_time ,"
        + " crt_user ," + " apr_flag ," + " apr_date ," + " apr_user ," + " mod_user ,"
        + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values (" + " :stop_apply_no ,"
        + " :pp_card_no ," + " :vip_kind ," + " :id_p_seqno ," + " :new_pp_card_no ,"
        + " :current_code ," + " :end_date ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :oppost_reason ," + " :oppost_remark ," + " '0' ," + " :card_no ," + " :reissue_flag ," + " 'N' ,"
        + " 'N' ," + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ,"
        + " :crt_user ," + " 'Y' ," + " to_char(sysdate,'yyyymmdd') ," + " :apr_user ,"
        + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " '1' " + " )";
    item2ParmStr("stop_apply_no");
    item2ParmStr("pp_card_no");
    item2ParmStr("vip_kind");
    item2ParmStr("id_p_seqno");
    item2ParmStr("new_pp_card_no");
    item2ParmStr("current_code");
    item2ParmStr("end_date");
    item2ParmStr("oppost_reason");
    item2ParmStr("oppost_remark");
    item2ParmStr("card_no");
    item2ParmNvl("reissue_flag", "N");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm3130");
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert crd_ppcard_stop error, " + getMsg());
      rc = -1;
      return rc;
    }
    updatePpcard();
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    if (wp.itemEq("cancel_flag", "Y")) {
      strSql = " update crd_ppcard_stop set " + " cancel_flag ='Y' ,"
          + " cancel_user =:cancel_user ," + " cancel_date =to_char(sysdate,'yyyymmdd') ,"
          + " cancel_time =to_char(sysdate,'hh24miss') ," + " cancel_remark =:cancel_remark ,"
          // + " reissue_flag =:reissue_flag ,"
          + " apr_flag='Y' ," + " apr_date=to_char(sysdate,'yyyymmdd') ," + " apr_user=:apr_user "
          + " where stop_apply_no =:stop_apply_no " + " and vip_kind =:vip_kind ";

      setString("cancel_user", wp.loginUser);
      item2ParmStr("cancel_remark");
      setString("apr_user", wp.loginUser);
      item2ParmStr("stop_apply_no");
      item2ParmStr("vip_kind");


    } else {
      strSql = " update crd_ppcard_stop set " + " oppost_reason=:oppost_reason ,"
          + " oppost_remark=:oppost_remark ," + " reissue_flag =:reissue_flag ," + " apr_flag='Y' ,"
          + " apr_date=to_char(sysdate,'yyyymmdd') ," + " apr_user=:apr_user "
          + " where stop_apply_no =:stop_apply_no " + " and vip_kind =:vip_kind ";

      item2ParmStr("oppost_reason");
      item2ParmStr("oppost_remark");
      item2ParmStr("reissue_flag");
      setString("apr_user", wp.loginUser);
      item2ParmStr("stop_apply_no");
      item2ParmStr("vip_kind");
    }


    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Update crd_ppcard_stop error, " + getMsg());
      rc = -1;
      return rc;
    }
    updatePpcard();
    return rc;
  }

  public int updatePpcard() {
    if (wp.itemEq("cancel_flag", "Y")) {
      strSql = " update crd_card_pp set " + " current_code ='0' ," + " oppost_date ='' ,"
          + " oppost_reason ='' ," + " stop_apply_no ='' ," + " mod_pgm =:mod_pgm ,"
          + " mod_user =:mod_user ," + " mod_time = sysdate ," + " mod_seqno = nvl(mod_seqno,0)+1 "
          + " where pp_card_no =:pp_card_no " + " and vip_kind =:vip_kind ";

      setString("mod_user", wp.loginUser);
      setString("mod_pgm", "cmsm3130");
      item2ParmStr("pp_card_no");
      item2ParmStr("vip_kind");
    } else {
      strSql = " update crd_card_pp set " + " current_code ='1' ,"
          + " oppost_date =to_char(sysdate,'yyyymmdd') ," + " oppost_reason =:oppost_reason ,"
          + " stop_apply_no =:stop_apply_no ," + " mod_pgm =:mod_pgm ," + " mod_user =:mod_user ,"
          + " mod_time = sysdate ," + " mod_seqno = nvl(mod_seqno,0)+1 "
          + " where pp_card_no =:pp_card_no " + " and vip_kind =:vip_kind ";
      item2ParmStr("stop_apply_no");
      item2ParmStr("oppost_reason");
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", "cmsm3130");
      item2ParmStr("pp_card_no");
      item2ParmStr("vip_kind");
    }

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Update crd_card_pp error, " + getMsg());
    }

    return rc;
  }


  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}

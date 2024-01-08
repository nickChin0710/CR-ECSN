/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-22  V1.00.00  ryan       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package crdm01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Crdm2080 extends BaseProc {
  int rr = -1, liextn = 0;
  String msg = "", lsBegVal = "", lsEndVal = "", dbChangeReason = "", idPSeqno = "";
  //String kk1 = "";
  int ilOk = 0;
  int ilErr = 0, ilErr2 = 0;
  String lsRiskBankNo = "";
  int selectCnt = 0;
  SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_id_code", "0");
  }

  @Override
  public void dddwSelect() {
    try {
      // wp.optionKey = wp.item_ss("db_optcode1");
      this.dddwList("dddw_db_optcode1", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type='NOTCHG_KIND_O' order by wf_id");
      // wp.optionKey = wp.item_ss("db_optcode2");
      this.dddwList("dddw_db_optcode2", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type='NOTCHG_KIND_M' order by wf_id");
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "where 1=1 and a.current_code = '0' ";

    if (empty(wp.itemStr("ex_cardno")) && empty(wp.itemStr("ex_id"))) {
      alertMsg("請輸入查詢之鍵值");
      return;
    }
    if (empty(wp.itemStr("ex_cardno")) == false) {
      wp.whereStr += " and a.pp_card_no = :ex_cardno ";
      setString("ex_cardno", wp.itemStr("ex_cardno"));
    }
    if (empty(wp.itemStr("ex_id")) == false) {
      wp.whereStr += " and b.id_no = :ex_id ";
      setString("ex_id", wp.itemStr("ex_id"));
    }

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(a.rowid) as rowid " + " ,a.pp_card_no " + " ,b.id_no " + " ,a.card_type "
        + " ,a.change_status " + " ,a.expire_chg_flag " + " ,a.expire_chg_date "
        + " ,a.source_code " + " ,a.current_code " + " ,a.change_reason " + " ,a.change_date "
        + " ,b.chi_name " + " ,a.expire_reason " + " ,a.eng_name " + " ,a.new_beg_date "
        + " ,a.new_end_date " + " ,a.valid_to " + " ,a.reissue_date " + " ,a.reissue_reason "
        + " ,a.reissue_status " + " ,a.group_code " + " ,a.mod_seqno " + " ,a.id_p_seqno ";

    wp.daoTable = "crd_card_pp a left join crd_idno b on a.id_p_seqno = b.id_p_seqno ";

    wp.whereOrder = "";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    String wkData = "", lsCardno = "", lsApprDate = "";
    for (int i = 0; i < wp.selectCnt; i++) {
      lsCardno = wp.colStr(i, "pp_card_no");
      String sqlSelect = "select process_kind " + ",expire_reason " + ",expire_chg_flag "
          + ",expire_chg_date " + ",apr_date " + "from crd_card_pp_tmp  "
          + "where pp_card_no  = :ls_cardno " + "and kind_type = '080' ";
      setString("ls_cardno", lsCardno);
      sqlSelect(sqlSelect);
      String lsProcessKind = sqlStr("process_kind");
      String lsExpireReason = sqlStr("expire_reason");
      String lsExpireChgFlag = sqlStr("expire_chg_flag");
      String lsExpireChgDate = sqlStr("expire_chg_date");
      lsApprDate = sqlStr("apr_date");
      if (sqlRowNum > 0) {
        wp.colSet(i, "db_old_process", lsProcessKind);
        wp.colSet(i, "expire_reason", lsExpireReason);
        wp.colSet(i, "expire_chg_date", lsExpireChgDate);
        wp.colSet(i, "expire_chg_flag", lsExpireChgFlag);
        wp.colSet(i, "db_appr_date", lsApprDate);
        if (lsExpireChgFlag.equals("1")) {
          String sqlSelect1 =
              "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_O' and wf_id = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(sqlSelect1);
          String lsExpireDesc = sqlStr("wf_desc");
          wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
        }
        if (lsExpireChgFlag.equals("4")) {
          String sqlSelect1 =
              "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_M' and wf_id = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(sqlSelect1);
          String lsExpireDesc = sqlStr("wf_desc");
          wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
        }
        if (lsExpireChgFlag.equals("5")) {
          String sqlSelect1 =
              "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_S_P' and wf_id = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(sqlSelect1);
          String lsExpireDesc = sqlStr("wf_desc");
          wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
        }
        wp.colSet(i, "db_appr", "N");
        wp.colSet(i, "db_expire_chg", lsExpireChgFlag);
      } else {
        String sql3 = " select process_kind " + " ,expire_chg_flag " + " ,apr_date "
            + " ,expire_reason " + " ,expire_chg_date "
            + " from crd_card_pp_tmp_h where pp_card_no = :ls_cardno and kind_type = '080' "
            + " and apr_date in ( "
            + " select max(apr_date) from crd_card_pp_tmp_h where pp_card_no = :ls_cardno2 and kind_type = '080') ";
        setString("ls_cardno", lsCardno);
        setString("ls_cardno2", lsCardno);
        sqlSelect(sql3);
        lsProcessKind = sqlStr("process_kind");
        lsExpireReason = sqlStr("expire_reason");
        lsExpireChgDate = sqlStr("expire_chg_date");
        lsExpireChgFlag = sqlStr("expire_chg_flag");
        lsApprDate = sqlStr("apr_date");
        if (sqlRowNum > 0) {
          wp.colSet(i, "db_appr_date", lsApprDate);
          wp.colSet(i, "db_appr", "Y");
          String sqlSelect1 =
              "select expire_reason,expire_chg_flag,expire_chg_date from crd_card_pp  where pp_card_no = :ls_cardno ";
          setString("ls_cardno", lsCardno);
          sqlSelect(sqlSelect1);
          lsExpireReason = sqlStr("expire_reason");
          lsExpireChgFlag = sqlStr("expire_chg_flag");
          lsExpireChgDate = sqlStr("expire_chg_date");
          if (sqlRowNum > 0) {
            // wp.col_set(i, "db_appr", "");
            wp.colSet(i, "expire_reason", lsExpireReason);
            wp.colSet(i, "expire_chg_date", lsExpireChgDate);
            wp.colSet(i, "expire_chg_flag", lsExpireChgFlag);
            if (lsExpireChgFlag.equals("1")) {
              String sqlSelect2 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_O' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sqlSelect2);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            if (lsExpireChgFlag.equals("4")) {
              String sqlSelect2 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_M' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sqlSelect2);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            if (lsExpireChgFlag.equals("5")) {
              String sqlSelect2 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_S_P' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sqlSelect2);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            wp.colSet(i, "db_expire_chg", lsExpireChgFlag);
          }
        } else {
          String sqlSelect1 =
              "select expire_reason,expire_chg_flag,expire_chg_date from crd_card_pp  where pp_card_no = :ls_cardno ";
          setString("ls_cardno", lsCardno);
          sqlSelect(sqlSelect1);
          lsExpireReason = sqlStr("expire_reason");
          lsExpireChgFlag = sqlStr("expire_chg_flag");
          lsExpireChgDate = sqlStr("expire_chg_date");
          if (sqlRowNum > 0) {
            wp.colSet(i, "db_appr", "");
            wp.colSet(i, "expire_reason", lsExpireReason);
            wp.colSet(i, "expire_chg_date", lsExpireChgDate);
            wp.colSet(i, "expire_chg_flag", lsExpireChgFlag);
            if (lsExpireChgFlag.equals("1")) {
              String sqlSelect2 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_O' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sqlSelect2);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            if (lsExpireChgFlag.equals("4")) {
              String sqlSelect2 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_M' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sqlSelect2);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            if (lsExpireChgFlag.equals("5")) {
              String sqlSelect2 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_S_P' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sqlSelect2);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            wp.colSet(i, "db_expire_chg", lsExpireChgFlag);
          }
        }
      }
      wkData = wp.colStr(i, "change_status");
      wp.colSet(i, "tt_change_status", commString.decode(wkData, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));
      wkData = wp.colStr(i, "change_reason");
      wp.colSet(i, "tt_change_reason", commString.decode(wkData, ",1,2,3", ",系統續卡,提前續卡,人工續卡"));
      wkData = lsExpireChgFlag;
      wp.colSet(i, "tt_db_expire_chg",
          commString.decode(wkData, ",1,4,5,0,2,3", ",預約不續卡,人工不續卡,系統不續卡,取消不續卡(放行前),取消不續卡(放行後),系統不續卡改續卡"));
      wkData = lsProcessKind;
      wp.colSet(i, "tt_db_old_process",
          commString.decode(wkData, ",0,1,4,2,3", ",取消不續卡(放行前),預約不續卡,人工不續卡,取消不續卡(放行後),系統不續卡改續卡"));
      wp.colSet(i, "opt", "5");
    }
  }



  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {
    String[] aaRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    String[] dbOptcode1 = wp.itemBuff("db_optcode1");
    String[] dbOptcode2 = wp.itemBuff("db_optcode2");
    String[] lsAppr = wp.itemBuff("db_appr");

    wp.listCount[0] = aaRowid.length;

    // -update-

    for (rr = 0; rr < opt.length; rr++) {
      if (opt[rr].equals("5")) {
        continue;
      }
      if (opt[rr].equals("1") && empty(dbOptcode1[rr])) {
        alertErr("請輸入預約原因 !!");
        ilErr++;
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
        return;
      }
      if (opt[rr].equals("4") && empty(dbOptcode2[rr])) {
        alertErr("請輸入人工原因 !!");
        ilErr++;
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
        return;
      }
      if (opt[rr].equals("0") && !lsAppr[rr].equals("N")) {
        alertErr("請執行【取消不續卡(放行後)】!!");
        ilErr++;
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
        return;
      }
      if (opt[rr].equals("2") && !lsAppr[rr].equals("Y") && lsAppr[rr].length() != 0) {
        alertErr("請執行【取消不續卡(放行前)】!!");
        ilErr++;
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
        return;
      }
      if (wfChkExpireChgFlag() != 1) {
        ilErr++;
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
        return;
      } else {
        switch (opt[rr]) {
          case "0":
            if (wfDeleteCrdCardPpTmp() != 1) {
              ilErr++;
              wp.colSet(rr, "ok_flag", "!");
              sqlCommit(0);
              return;
            }
            break;
          case "1":
            deleteCrdCardPpTmp();
            if (wfUpdCrdCardPp() != 1) {
              ilErr++;
              wp.colSet(rr, "ok_flag", "!");
              sqlCommit(0);
              return;
            }
            break;
          case "2":
            deleteCrdCardPpTmp();
            if (wfCancelExpire() != 1) {
              ilErr++;
              wp.colSet(rr, "ok_flag", "!");
              sqlCommit(0);
              return;
            }
            break;
          case "3":
            deleteCrdCardPpTmp();
            if (wfMoveEmbossTmp() != 1) {
              ilErr++;
              wp.colSet(rr, "ok_flag", "!");
              return;
            }
            break;
          case "4":
            deleteCrdCardPpTmp();
            if (wfUpdCrdCardPp() != 1) {
              wp.colSet(rr, "ok_flag", "!");
              sqlCommit(0);
              return;
            }
            break;
        }
      }

    }
    sqlCommit(1);
    alertMsg("執行處理成功");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  int wfInsertCrdCardPpTmp() throws Exception {
    String lsExpireReason = "", lsExpireChgFlag = "";
    String[] opt = wp.itemBuff("opt");
    String[] lsCardNo = wp.itemBuff("pp_card_no");
    String[] lsProcessKind = wp.itemBuff("opt");
    String[] idPSeqno = wp.itemBuff("id_p_seqno");
    String lsExpireChgDate = wp.sysDate;
    String[] dbOptcode1 = wp.itemBuff("db_optcode1");
    String[] dbOptcode2 = wp.itemBuff("db_optcode2");
    String[] dbExpireChg = wp.itemBuff("db_expire_chg");
    if (opt[rr].equals("0") || opt[rr].equals("2")) {
      lsExpireReason = "";
      lsExpireChgFlag = "";
    } else {
      if (opt[rr].equals("1")) {
        lsExpireReason = dbOptcode1[rr];
      }
      if (opt[rr].equals("4")) {
        lsExpireReason = dbOptcode2[rr];
      }
      lsExpireChgFlag = dbExpireChg[rr];
    }
    String[] lsExpireChgDateOld = wp.itemBuff("expire_chg_date");
    String[] lsExpireReasonOld = wp.itemBuff("expire_reason");
    String[] lsExpireChgFlagOld = wp.itemBuff("expire_chg_flag");
    String[] lsOldEndDate = wp.itemBuff("valid_to");
    String[] lsCurEndDate = wp.itemBuff("valid_to");
    String lsCreateUser = wp.loginUser;
    String lsCreateDate = wp.sysDate;
    String sqlSelect =
        "select count(*) as li_cnt  from crd_card_pp_tmp where pp_card_no = :ls_card_no and kind_type = '080' ";
    setString("ls_card_no", lsCardNo[rr]);
    sqlSelect(sqlSelect);
    int liCnt = this.toInt(sqlStr("li_cnt"));
    if (liCnt > 0) {
      alertErr("已經處理提前續卡過,請先查詢資料(crd_card_pp_tmp) !!");
      return -1;
    }
    String sqlInsert = "insert into crd_card_pp_tmp(" + " pp_card_no" + " ,kind_type "
        + " ,id_p_seqno " + " ,process_kind " + " ,expire_reason " + " ,expire_chg_flag "
        + " ,expire_chg_date " + " ,expire_reason_old " + " ,expire_chg_flag_old "
        + " ,expire_chg_date_old " + " ,cur_end_date " + " ,old_end_date " + " ,crt_user "
        + " ,crt_date " + " ,mod_user " + " ,mod_time " + " ,mod_pgm " + " ,mod_seqno" + " )values("
        + " :ls_card_no" + " ,'080'" + " ,:id_p_seqno " + " ,:ls_process_kind "
        + " ,:ls_expire_reason " + " ,:ls_expire_chg_flag " + " ,:ls_expire_chg_date "
        + " ,:ls_expire_reason_old " + " ,:ls_expire_chg_flag_old " + " ,:ls_expire_chg_date_old "
        + " ,:ls_cur_end_date " + " ,:ls_old_end_date " + " ,:ls_create_user "
        + " ,:ls_create_date " + " ,:ls_create_user " + " ,sysdate " + " ,'crdm2080' " + " ,1) ";
    setString("ls_card_no", lsCardNo[rr]);
    setString("id_p_seqno", idPSeqno[rr]);
    setString("ls_process_kind", lsProcessKind[rr]);
    setString("ls_expire_reason", lsExpireReason);
    setString("ls_expire_chg_flag", lsExpireChgFlag);
    setString("ls_expire_chg_date", lsExpireChgDate);
    setString("ls_expire_reason_old", lsExpireReasonOld[rr]);
    setString("ls_expire_chg_flag_old", lsExpireChgFlagOld[rr]);
    setString("ls_expire_chg_date_old", lsExpireChgDateOld[rr]);
    setString("ls_cur_end_date", lsCurEndDate[rr]);
    setString("ls_old_end_date", lsOldEndDate[rr]);
    setString("ls_create_user", lsCreateUser);
    setString("ls_create_date", lsCreateDate);
    setString("ls_create_user", lsCreateUser);
    sqlExec(sqlInsert);
    if (sqlRowNum <= 0) {
      alertErr("寫入卡片暫存檔錯誤~");
      return -1;
    }
    queryFunc();
    return 1;
  }

  int wfUpdCrdCardPp() throws Exception {
    // -- 抓取螢幕上所選取之值
    String[] lsChangeStatus = wp.itemBuff("change_status");
    if (lsChangeStatus[rr].equals("2")) {
      alertErr("此卡片已送製卡,不可作任何異動");
      return -1;
    }
    // -- 由覆核處理資料 **********************************************************************
    if (wfInsertCrdCardPpTmp() != 1) {
      return -1;
    }
    return 1;
  }

  int wfMoveEmbossTmp() throws Exception {
    String liRecno = "";
    String lsChk = "";
    String[] lsChangeStatus = wp.itemBuff("change_status");
    if (lsChangeStatus[rr].equals("1")) {
      alertErr("續卡製卡中，不可改為不續卡 1");
      return -1;
    }
    if (lsChangeStatus[rr].equals("2")) {
      alertErr("此卡片送製卡中,不可再做提前續卡");
      return -1;
    }
    String[] lsCardno = wp.itemBuff("pp_card_no");
    String sqlSelect =
        "select count(*) li_cnt from  crd_emboss_pp where pp_card_no = :ls_cardno and in_main_date ='' ";
    setString("ls_cardno", lsCardno[rr]);
    sqlSelect(sqlSelect);
    int liCnt = this.toInt(sqlStr("li_cnt"));
    if (liCnt > 0) {
      alertErr("續卡製卡中，不可改為不續卡 2");
      return -1;
    }
    // -- 改為抓自己之效期最展期 2002/01/24
    String[] lsValidDate = wp.itemBuff("new_end_date");
    String lsCreateDate = wp.sysDate;
    Calendar cal = Calendar.getInstance();
    if (empty(lsValidDate[rr]) == false) {
      Date date = format.parse(lsValidDate[rr]);
      cal.setTime(date);
      cal.add(Calendar.MARCH, -6);
      lsChk = format.format(cal.getTime());
    }
    if (this.toInt(lsChk) > this.toInt(lsCreateDate)) {
      alertErr("效期需在系統日六個月內");
      return -1;
    }
    // --Get Batchno--
    String lsBatchno1 = lsCreateDate.substring(2, 8);
    String sqlSelect2 =
        "select max(batchno) as ls_batchno from crd_emboss_pp where substr(batchno,1,6) = :ls_batchno1";
    setString("ls_batchno1", lsBatchno1);
    sqlSelect(sqlSelect2);
    String lsSatchno = sqlStr("ls_batchno");
    if (empty(lsSatchno)) {
      lsSatchno = lsBatchno1 + "01";
    } else {
      String sqlSelect3 =
          "select max(recno)+1 as li_recno from crd_emboss_pp where batchno = :ls_batchno";
      setString("ls_batchno", lsSatchno);
      sqlSelect(sqlSelect3);
      liRecno = sqlStr("li_recno");
    }
    if (empty(liRecno) || liRecno.equals("0")) {
      liRecno = "1";
    }
    // -- 由覆核處理資料 **********************************************************************
    if (wfInsertCrdCardPpTmp() != 1) {
      return -1;
    }

    return 1;
  }

  int wfCancelExpire() throws Exception {
    String[] lsChangeStatus = wp.itemBuff("change_status");
    String[] lsExpireChgFlag = wp.itemBuff("expire_chg_flag");
    if (lsChangeStatus[rr].equals("2")) {
      alertErr("此卡片已送製卡,不可作任何異動");
      return -1;
    }
    if (empty(lsExpireChgFlag[rr])) {
      alertErr("此筆資料本身並無不續卡註記");
      return -1;
    }
    // -- 由覆核處理資料 **********************************************************************
    if (wfInsertCrdCardPpTmp() != 1) {
      return -1;
    }

    return 1;
  }

  int wfChkExpireChgFlag() {
    String[] opt = wp.itemBuff("opt");
    String[] expireChgFlag = wp.itemBuff("expire_chg_flag");
    String[] dbExpireChg = wp.itemBuff("db_expire_chg");
    String[] changeStatus = wp.itemBuff("change_status");
    String[] reissueStatus = wp.itemBuff("reissue_status");

    if (opt[rr].equals("1") || opt[rr].equals("4")) {
      if (!empty(expireChgFlag[rr])) {
        alertErr("此卡片已在不續卡狀態下");
        return -1;
      }
      /*
       * if(empty(db_expire_chg[rr])){ alert_err("請輸入預約不續卡註記"); return -1; }
       */
      if (changeStatus[rr].equals("1") || changeStatus[rr].equals("2")) {
        alertErr("此卡片在續卡狀態下,不可做預約不續卡");
        return -1;
      }
      if (reissueStatus[rr].equals("1") || reissueStatus[rr].equals("2")) {
        alertErr("此卡片在重製卡狀態下,不可做預約不續卡");
        return -1;
      }
    }
    if (opt[rr].equals("2")) {
      if (empty(expireChgFlag[rr])) {
        alertErr("此卡片未在不續卡狀態下,不可做取消預約不續卡");
        return -1;
      }
    }
    return 1;
  }

  int wfDeleteCrdCardPpTmp() {
    String[] lsCardNo = wp.itemBuff("pp_card_no");
    String sqlDelete =
        "delete crd_card_pp_tmp where pp_card_no = :ls_card_no and kind_type = '080' ";
    setString("ls_card_no", lsCardNo[rr]);
    sqlExec(sqlDelete);
    if (sqlRowNum <= 0) {
      alertErr("續卡製卡中，不可再做續卡");
      return -1;
    }

    return 1;
  }

  int deleteCrdCardPpTmp() {
    String[] lsCardNo = wp.itemBuff("pp_card_no");
    String sqlDelete =
        "delete crd_card_pp_tmp where pp_card_no = :ls_card_no and kind_type = '080' ";
    setString("ls_card_no", lsCardNo[rr]);
    sqlExec(sqlDelete);

    return 1;
  }

}

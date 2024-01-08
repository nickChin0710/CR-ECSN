/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-29  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 108-12-17  V1.00.02  ryan		  update : ptr_group_card==>crd_item_unit    *
* 109-04-28  V1.00.03  YangFang   updated for project coding standard        *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package crdm01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Crdm0120 extends BaseProc {
  Crdm0120Func func;
  int rr = -1, liExtn = 0;
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
    wp.colSet("ex_corp_no_code", "0");
    wp.colSet("ex_id_code", "0");
  }

  @Override
  public void dddwSelect() {
    /*
     * try { wp.initOption = "--"; wp.optionKey = wp.item_ss("ex_user_id");
     * dddw_list("SecUserIDNameList","sec_user" , "usr_id", "usr_id||'['||usr_cname||']'",
     * "where 1=1 and usr_type = '4' order by usr_id"); } catch(Exception ex){}
     */
  }

  int getWhereStr() {
    if (empty(wp.itemStr("ex_id")) == false) {
      String lsSql =
          " select id_p_seqno  from crd_idno  where id_no = :id_no and id_no_code = :id_no_code";
      setString("id_no", wp.itemStr("ex_id"));
      setString("id_no_code", wp.itemStr("ex_id_code"));
      sqlSelect(lsSql);
      idPSeqno = sqlStr("id_p_seqno");
    }
    wp.whereStr = "where 1=1 ";
    if (empty(wp.itemStr("ex_cardno")) == true && empty(wp.itemStr("ex_id")) == true
        && empty(wp.itemStr("ex_corp_no")) == true) {
      alertMsg("請輸入查詢之鍵值");
      return -1;
    }
    if (empty(wp.itemStr("ex_cardno")) == false || empty(wp.itemStr("ex_id")) == false) {
      wp.whereStr +=
          "and a.major_id_p_seqno in (select c.major_id_p_seqno from crd_card as c where 1=1 ";
      if (empty(wp.itemStr("ex_cardno")) == false) {
        wp.whereStr += " and a.card_no = :ex_cardno ";
        setString("ex_cardno", wp.itemStr("ex_cardno"));
        wp.whereStr += " and a.card_no   = c.card_no " + " and a.card_type = c.card_type "
            + " and a.group_code = c.group_code ";
      }
      if (empty(wp.itemStr("ex_id")) == false) {
        wp.whereStr += " and a.id_p_seqno = :ex_id_p_seqno ";
        setString("ex_id_p_seqno", idPSeqno);
      }
      wp.whereStr += " ) "
          + " and ( (a.current_code = '0' and a.change_status in('','3','4')) or a.change_status='1' ) ";
    }
    if (empty(wp.itemStr("ex_corp_no")) == false) {
      wp.whereStr += " and a.corp_no = :ex_corp_no ";
      setString("ex_corp_no", wp.itemStr("ex_corp_no"));
      wp.whereStr += " and a.corp_no_code = :ex_corp_no_code ";
      setString("ex_corp_no_code", wp.itemStr("ex_corp_no_code"));
      wp.whereStr +=
          " and ( (a.current_code = '0' and a.change_status in('','3','4')) or a.change_status='1' ) ";
    }
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // wp.whereStr = "where 1=1 ";
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(a.rowid) as rowid, " + "a.card_no, " + "b.id_no, " + "b.id_no_code, "
        + "b.chi_name, " + "b.eng_name, " + "a.change_reason, " + "a.change_status, "
        + "a.expire_chg_flag, " + "a.change_date, " + "a.old_end_date, " + "a.new_end_date, "
        + "a.sup_flag, " + "a.major_card_no, " + "a.corp_no, " + "a.corp_no_code, "
        + "a.group_code, " + "a.card_type, " + "a.ic_flag, " + "a.id_p_seqno, " + "a.acno_p_seqno, "
        + "a.unit_code, " + "a.reg_bank_no, " + "a.old_card_no, " + "a.major_id_p_seqno, "
        + "a.emboss_data, " + "a.source_code, " + "a.acct_type, " + "a.new_beg_date, "
        + "a.force_flag, " + "a.new_beg_date, " + "a.new_beg_date, " + "a.mod_seqno,"
        + "a.reissue_status ";

    wp.daoTable = "crd_card as a left join crd_idno as b on a.id_p_seqno = b.id_p_seqno";
    wp.whereOrder = "order by card_type,major_card_no,sup_flag";
    if (getWhereStr() != 1)
      return;
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata(wp.selectCnt);
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  void listWkdata(int selectCnt) throws Exception {
    String wkData = "", lsCardno = "";
    for (int i = 0; i < selectCnt; i++) {
      lsCardno = wp.colStr(i, "card_no");
      wp.colSet(i, "wk_id", wp.colStr(i, "id_no") + wp.colStr(i, "id_no_code"));
      wp.colSet(i, "wk_corp_no", wp.colStr(i, "corp_no") + wp.colStr(i, "corp_no_code"));

      wkData = wp.colStr(i, "change_status");
      wp.colSet(i, "tt_change_status", commString.decode(wkData, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));
      wkData = wp.colStr(i, "expire_chg_flag");
      wp.colSet(i, "tt_expire_chg_flag", commString.decode(wkData, ",1,2,3", ",系統不續卡,預約不續卡,人工不續卡"));

      wkData = wp.colStr(i, "sup_flag");
      wp.colSet(i, "tt_sup_flag", commString.decode(wkData, ",0,1", ",正卡,附卡"));

      if (wfGetAcnoData(i) != 1) {
        continue;
      }
      liExtn = wfGetExtnYear(wp.colStr(i, "unit_code"), wp.colStr(i, "card_type"));
      if (liExtn <= 0) {
        liExtn = 2;
      }

      lsBegVal = strMid(getSysDate(), 0, 6) + "01";
      lsEndVal = wp.colStr(i, "new_end_date");

      Calendar cal2 = Calendar.getInstance();
      Date date2 = format.parse(lsEndVal);
      cal2.setTime(date2);
      cal2.add(Calendar.YEAR, liExtn);
      lsEndVal = format.format(cal2.getTime());
      wp.colSet(i, "cur_beg_date", lsBegVal);
      wp.colSet(i, "cur_end_date", lsEndVal);
      if (wp.colNum(i, "change_status") >= 3) {
        dbChangeReason = "";
        wp.colSet(i, "db_change_reason", dbChangeReason);
      } else {
        dbChangeReason = wp.colStr(i, "change_reason");
        wp.colSet(i, "db_change_reason", dbChangeReason);
      }
      String sqlSelect = "select process_kind" + ",change_reason" + ",change_status"
          + ",change_date" + ",expire_chg_flag" + ",expire_reason" + ",apr_date "
          + "from crd_card_tmp " + "where card_no  = :ls_cardno " + "and kind_type = '120' ";
      setString("ls_cardno", lsCardno);
      sqlSelect(sqlSelect);
      String lsProcessKind = sqlStr("process_kind");
      String lsChangeReason = sqlStr("change_reason");
      String lsChangeStatus = sqlStr("change_status");
      String lsChangeDate = sqlStr("change_date");
      String lsExpireChgFlag = sqlStr("expire_chg_flag");
      String lsExpireReason = sqlStr("expire_reason");
      String lsApprDate = sqlStr("apr_date");
      wp.colSet(i, "tt_db_old_process", "");
      if (sqlRowNum > 0) {
        wp.colSet(i, "db_old_process", lsProcessKind);
        wp.colSet(i, "tt_db_old_process",
            commString.decode(lsProcessKind, ",0,1,2,3", ",取消線上續卡(放行前),線上續卡,取消線上續卡(放行後),系統續卡改不續卡"));
        wp.colSet(i, "change_date", lsChangeDate);
        wp.colSet(i, "db_change_reason", lsChangeReason);
        wp.colSet(i, "change_status", lsChangeStatus);
        wp.colSet(i, "tt_change_status",
            commString.decode(lsChangeStatus, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));
        wp.colSet(i, "db_appr_date", lsExpireChgFlag);
        wp.colSet(i, "db_appr", "N");
        wp.colSet(i, "expire_chg_flag", lsExpireChgFlag);
        wp.colSet(i, "tt_expire_chg_flag",
            commString.decode(lsExpireChgFlag, ",1,2,3", ",系統不續卡,預約不續卡,人工不續卡"));
        if (lsExpireChgFlag.equals("1")) {
          sqlSelect = "select wf_desc " + "from ptr_sys_idtab " + "where wf_type = 'NOTCHG_KIND_O' "
              + "and wf_id   = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(sqlSelect);
          String lsExpireDesc = sqlStr("wf_desc");
          wp.colSet(i, "db_expire_reason", lsExpireReason + "_" + lsExpireDesc);
        }
        if (lsExpireChgFlag.equals("4")) {
          sqlSelect = "select wf_desc " + "from ptr_sys_idtab " + "where wf_type = 'NOTCHG_KIND_M' "
              + "and wf_id   = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(sqlSelect);
          String lsExpireDesc = sqlStr("wf_desc");
          wp.colSet(i, "db_expire_reason", lsExpireReason + "_" + lsExpireDesc);
        }
      } else {
        sqlSelect = "select process_kind" + ",change_reason" + ",change_status" + ",change_date"
            + ",expire_chg_flag" + ",expire_reason" + ",apr_date " + "from crd_card_tmp_h "
            + "where card_no  = :ls_cardno " + "and kind_type = '120' " + "and apr_date in ( "
            + " select max(apr_date) " + "from crd_card_tmp_h " + "where card_no = :ls_cardno2  "
            + "and kind_type = '120' )";
        setString("ls_cardno", lsCardno);
        setString("ls_cardno2", lsCardno);
        sqlSelect(sqlSelect);

        if (sqlRowNum > 0 && sqlRowNum < 2) {
          lsApprDate = sqlStr("apr_date");
          wp.colSet(i, "db_appr_date", lsApprDate);
          wp.colSet(i, "db_appr", "Y");
          sqlSelect =
              "select change_reason" + ",expire_chg_flag" + ",expire_reason" + ",change_status"
                  + ",change_date " + "from crd_card " + "where card_no = :ls_cardno ";
          setString("ls_cardno", lsCardno);
          sqlSelect(sqlSelect);
          lsChangeReason = sqlStr("change_reason");
          lsExpireChgFlag = sqlStr("expire_chg_flag");
          lsExpireReason = sqlStr("expire_reason");
          lsChangeStatus = sqlStr("change_status");
          lsChangeDate = sqlStr("change_date");
          wp.colSet(i, "change_date", lsChangeDate);
          wp.colSet(i, "db_change_reason", lsChangeReason);
          wp.colSet(i, "change_status", lsChangeStatus);
          wp.colSet(i, "tt_change_status",
              commString.decode(lsChangeStatus, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));
          wp.colSet(i, "expire_chg_flag", lsExpireChgFlag);
          wp.colSet(i, "tt_expire_chg_flag",
              commString.decode(lsExpireChgFlag, ",1,2,3", ",系統不續卡,預約不續卡,人工不續卡"));
          if (lsExpireChgFlag.equals("1")) {
            sqlSelect = "select wf_desc " + "from ptr_sys_idtab "
                + "where wf_type = 'NOTCHG_KIND_O' " + "and wf_id   = :ls_expire_reason ";
            setString("ls_expire_reason", lsExpireReason);
            sqlSelect(sqlSelect);
            String lsExpireDesc = sqlStr("wf_desc");
            wp.colSet(i, "db_expire_reason", lsExpireReason + "_" + lsExpireDesc);
          }
          if (lsExpireChgFlag.equals("4")) {
            sqlSelect = "select wf_desc " + "from ptr_sys_idtab "
                + "where wf_type = 'NOTCHG_KIND_M' " + "and wf_id   = :ls_expire_reason ";
            setString("ls_expire_reason", lsExpireReason);
            sqlSelect(sqlSelect);
            String lsExpireDesc = sqlStr("wf_desc");
            wp.colSet(i, "db_expire_reason", lsExpireReason + "_" + lsExpireDesc);
          }
        } else {
          sqlSelect =
              "select change_reason" + ",expire_chg_flag" + ",expire_reason" + ",change_status"
                  + ",change_date " + "from crd_card " + "where card_no = :ls_cardno ";
          setString("ls_cardno", lsCardno);
          sqlSelect(sqlSelect);

          lsChangeReason = sqlStr("change_reason");
          lsExpireChgFlag = sqlStr("expire_chg_flag");
          lsExpireReason = sqlStr("expire_reason");
          lsChangeStatus = sqlStr("change_status");
          lsChangeDate = sqlStr("change_date");
          wp.colSet(i, "change_date", lsChangeDate);
          wp.colSet(i, "db_change_reason", lsChangeReason);
          wp.colSet(i, "change_status", lsChangeStatus);
          wp.colSet(i, "tt_change_status",
              commString.decode(lsChangeStatus, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));
          wp.colSet(i, "db_appr", "");
          wp.colSet(i, "db_appr_date", "");
          wp.colSet(i, "expire_chg_flag", lsExpireChgFlag);
          wp.colSet(i, "tt_expire_chg_flag",
              commString.decode(lsExpireChgFlag, ",1,2,3", ",系統不續卡,預約不續卡,人工不續卡"));
          if (lsExpireChgFlag.equals("1")) {
            sqlSelect = "select wf_desc " + "from ptr_sys_idtab "
                + "where wf_type = 'NOTCHG_KIND_O' " + "and wf_id   = :ls_expire_reason ";
            setString("ls_expire_reason", lsExpireReason);
            sqlSelect(sqlSelect);
            String lsExpireDesc = sqlStr("wf_desc");
            wp.colSet(i, "db_expire_reason", lsExpireReason + "_" + lsExpireDesc);
          }
          if (lsExpireChgFlag.equals("4")) {
            sqlSelect = "select wf_desc " + "from ptr_sys_idtab "
                + "where wf_type = 'NOTCHG_KIND_M' " + "and wf_id   = :ls_expire_reason ";
            setString("ls_expire_reason", lsExpireReason);
            sqlSelect(sqlSelect);
            String lsExpireDesc = sqlStr("wf_desc");
            wp.colSet(i, "db_expire_reason", lsExpireReason + "_" + lsExpireDesc);
          }
        }
      }
    }
  }

  int wfMoveEmbossTmp() throws Exception {
    String[] changeStatus = wp.itemBuff("change_status");
    String[] cardNo = wp.itemBuff("card_no");
    String[] supFlag = wp.itemBuff("sup_flag");
    String[] majorCardNo = wp.itemBuff("major_card_no");
    String[] newEndDate = wp.itemBuff("new_end_date");

    String lsCardno = "", lsMajorCurrentCode = "", lsChk = "", lsCreateDate = "";
    String lsValidDate = "";

    if (changeStatus[rr].equals("1")) {
      msg = "續卡製卡中，不可再做續卡";
      return -1;
    }
    if (changeStatus[rr].equals("2")) {
      msg = "已送製卡中,不可再做續卡";
      return -1;
    }
    String lsSql2 = " select *  from  crd_emboss_tmp where  old_card_no = :ls_cardno ";
    lsCardno = cardNo[rr];
    setString("ls_cardno", lsCardno);
    sqlSelect(lsSql2);
    if (sqlRowNum > 0) {
      msg = "續卡製卡中，不可再做續卡";
      return -1;
    }
    // -- 附卡抓取正卡效期,正卡效期 > sysdate+6
    if (supFlag[rr].equals("1")) {
      String lsSql3 =
          " select new_beg_date,new_end_date,current_code  from  crd_card where  card_no = :ls_major_cardno ";
      setString("ls_major_cardno", majorCardNo[rr]);
      sqlSelect(lsSql3);
      lsMajorCurrentCode = sqlStr("current_code");
      if (sqlRowNum <= 0) {
        msg = "找取不到正卡資料";
        return -1;
      }
      if (!lsMajorCurrentCode.equals("0")) {
        msg = "正卡不為正常卡,不可做線上續卡";
        return -1;
      }
    }
    // -- 改為抓自己之效期最展期 2002/01/24
    lsValidDate = newEndDate[rr];
    Calendar cal = Calendar.getInstance();
    lsCreateDate = getSysDate();
    Date date = format.parse(lsValidDate);
    cal.setTime(date);
    cal.add(Calendar.MARCH, -6);
    lsChk = format.format(cal.getTime());
    if (this.toInt(lsChk) > this.toInt(lsCreateDate)) {
      msg = "卡片屆期前六個月方可續發";
      return -1;
    }
    // -- 由覆核處理資料
    if (wfInsertCrdCardTmp() != 1) {
      return -1;
    }

    return 1;
  }

  int wfGetAcnoData(int alRow) throws Exception {

    String lsSql10 = " select risk_bank_no from act_acno where acno_p_seqno = :acno_p_seqno ";
    setString("acno_p_seqno", wp.colStr(alRow, "acno_p_seqno"));
    sqlSelect(lsSql10);
    lsRiskBankNo = sqlStr("risk_bank_no");
    if (sqlRowNum <= 0) {
      return -1;
    }
    return 1;
  }

  int wfCancelChg() {
    String lsChangeStatus[] = wp.itemBuff("change_status");

    if (empty(lsChangeStatus[rr]) == true) {
      msg = "該卡為不續卡狀態下，不需取消續卡";
      return -1;
    }
    if (lsChangeStatus[rr].equals("2")) {
      msg = "此卡片已送製卡,不可取消續卡";
      return -1;
    }
    // -- 由覆核處理資料
    if (wfInsertCrdCardTmp() != 1) {
      return -1;
    }
    return 1;
  }

  int wfGetExtnYear(String unitCode, String cardType) throws Exception {
    int liYear = 0;
    String extnYear = "";
    if (empty(unitCode) == true) {
      unitCode = "0000";
    }
    String lsSql9 =
        " select extn_year from crd_item_unit where unit_code = :unit_code and card_type = :card_type";
    setString("unit_code", unitCode);
    setString("card_type", cardType);
    sqlSelect(lsSql9);
    extnYear = sqlStr("extn_year");
    if (empty(sqlStr("extn_year")) == true) {
      extnYear = "0";
    }
    liYear = this.toInt(extnYear);
    if (sqlRowNum <= 0) {
      msg = "抓取不到展期年~";
      return -1;
    }

    return liYear;
  }

  int wfChkChangeStatus(String dbOptcode, String dbChangeReason) {
    String[] expireChgFlag = wp.itemBuff("expire_chg_flag");
    String[] reissueStatus = wp.itemBuff("reissue_status");
    String[] changeStatus = wp.itemBuff("change_status");

    if (dbOptcode.equals("1")) {
      if (empty(expireChgFlag[rr]) == false) {
        msg = "已在不續卡狀態下,不可提前續卡";
        return -1;
      }
      if (reissueStatus[rr].equals("1") || reissueStatus[rr].equals("2")) {
        msg = "已在重製卡狀態下,不可提前續卡";
        return -1;
      }
      if (empty(dbChangeReason)) {
        msg = "需輸入續卡註記欄位值";
        return -1;
      }
    }
    if (dbOptcode.equals("2")) {
      if (changeStatus[rr].equals("2")) {
        msg = "已送製卡,不可取消提前續卡";
        return -1;
      }
    }
    if (dbOptcode.equals("3")) {

      if (empty(changeStatus[rr])) {
        msg = "此卡片不在續卡狀態下,不可做系統續卡改系統不續卡";
        return -1;
      }
      if (changeStatus[rr].equals("2")) {
        msg = "此卡片已在續卡待製卡中";
        return -1;
      }

      if (!changeStatus[rr].equals("1")) {
        msg = "只能系統續卡改系統不續卡";
        return -1;
      }
    }
    return 1;
  }

  int wfProcessChg() {
    String lsExpireChgFlag[] = wp.itemBuff("expire_chg_flag");
    String lsChangeStatus[] = wp.itemBuff("change_status");

    if (empty(lsExpireChgFlag[rr]) == false) {
      msg = "不可重複做預約不續卡";
      return -1;
    }
    if (lsChangeStatus[rr].equals("2")) {
      msg = "已在續卡製卡中,不可改為不續卡";
      return -1;
    }
    // -- 由覆核處理資料
    if (wfInsertCrdCardTmp() != 1) {
      return -1;
    }

    return 1;
  }

  int wfInsertCrdCardTmp() {
    String lsChangeReason = "", lsChangeStatus = "";
    String[] opt = wp.itemBuff("opt");
    String[] lsCardNo = wp.itemBuff("card_no");
    String[] lsProcessKind = wp.itemBuff("opt");
    String[] idPSeqno = wp.itemBuff("id_p_seqno");
    String[] lsCorpNo = wp.itemBuff("corp_no");
    String lsChangeDate = wp.sysDate;
    String[] dbChangeReason = wp.itemBuff("db_change_reason");
    String[] changeStatus = wp.itemBuff("change_status");

    if (opt[rr].equals("0") || opt[rr].equals("2")) {
      lsChangeReason = "";
      lsChangeStatus = "";
    } else {
      lsChangeReason = dbChangeReason[rr];
      lsChangeStatus = changeStatus[rr];
    }
    String[] lsChangeDateOld = wp.itemBuff("change_date");
    String[] lsChangeReasonOld = wp.itemBuff("change_reason");
    String[] lsChangeStatusOld = wp.itemBuff("change_status");
    String[] lsOldEndDate = wp.itemBuff("new_end_date");
    String[] lsCuEndDate = wp.itemBuff("cur_end_date");
    String[] lsCurBegDate = wp.itemBuff("cur_beg_date");
    String lsCreateUser = wp.loginUser;
    String lsCreateDate = wp.sysDate;
    String sqlSelect =
        "select count(*) as li_cnt  from crd_card_tmp where card_no = :ls_card_no and kind_type = '120' ";
    setString("ls_card_no", lsCardNo[rr]);
    sqlSelect(sqlSelect);
    int liCnt = this.toInt(sqlStr("li_cnt"));
    if (liCnt > 0) {
      wp.alertMesg =
          "<script language='javascript'> alert('已經處理提前續卡過,請先查詢資料(crd_card_tmp) !!')</script>";
      return -1;
    }
    String sqlInsert = "insert into crd_card_tmp(card_no" + " ,kind_type " + " ,id_p_seqno "
        + " ,corp_no " + " ,process_kind " + " ,change_reason " + " ,change_status "
        + " ,change_date " + " ,change_reason_old " + " ,change_status_old " + " ,change_date_old "
        + " ,cur_end_date " + " ,old_end_date " + " ,cur_beg_date " + " ,crt_user " + " ,crt_date "
        + " ,mod_user " + " ,mod_time " + " ,mod_pgm " + " ,mod_seqno" + " )values("
        + " :ls_card_no" + " ,'120'" + " ,:id_p_seqno " + " ,:ls_corp_no " + " ,:ls_process_kind "
        + " ,:ls_change_reason " + " ,:ls_change_status " + " ,:ls_change_date "
        + " ,:ls_change_reason_old " + " ,:ls_change_status_old " + " ,:ls_change_date_old "
        + " ,:ls_cur_end_date " + " ,:ls_old_end_date " + " ,:ls_cur_beg_date "
        + " ,:ls_create_user " + " ,:ls_create_date " + " ,:ls_create_user " + " ,sysdate "
        + " ,'crdm0120' " + " ,1) ";
    setString("ls_card_no", lsCardNo[rr]);
    setString("id_p_seqno", idPSeqno[rr]);
    setString("ls_corp_no", lsCorpNo[rr]);
    setString("ls_process_kind", lsProcessKind[rr]);
    setString("ls_change_reason", lsChangeReason);
    setString("ls_change_status", lsChangeStatus);
    setString("ls_change_date", lsChangeDate);
    setString("ls_change_reason_old", lsChangeReasonOld[rr]);
    setString("ls_change_status_old", lsChangeStatusOld[rr]);
    setString("ls_change_date_old", lsChangeDateOld[rr]);
    setString("ls_cur_end_date", lsCuEndDate[rr]);
    setString("ls_old_end_date", lsOldEndDate[rr]);
    setString("ls_cur_beg_date", lsCurBegDate[rr]);
    setString("ls_create_user", lsCreateUser);
    setString("ls_create_date", lsCreateDate);
    setString("ls_create_user", lsCreateUser);
    sqlExec(sqlInsert);
    if (sqlRowNum <= 0) {
      alertErr("寫入卡片暫存檔錯誤~");
      return -1;
    }

    return 1;
  }

  int wfDeleteCrdCardTmp() {
    String[] lsCardNo = wp.itemBuff("card_no");
    String sqlDelete = "delete crd_card_tmp where card_no = :ls_card_no and kind_type = '120' ";
    setString("ls_card_no", lsCardNo[rr]);
    sqlExec(sqlDelete);
    if (sqlRowNum < 0) {
      msg = "delete crd_card_tmp err";
      return -1;
    }
    return 1;
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
    func = new Crdm0120Func(wp);
    String[] aaDbChangeReason = wp.itemBuff("db_change_reason");
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaDbAppr = wp.itemBuff("db_appr");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = aaRowid.length;

    // -update-

    for (rr = 0; rr < opt.length; rr++) {
      ilErr2 = 0;
      func.varsSet("aa_rowid", aaRowid[rr]);
      func.varsSet("aa_db_change_reason", aaDbChangeReason[rr]);

      if (empty(opt[rr])) {
        continue;
      }
      if (opt[rr].equals("1") && empty(aaDbChangeReason[rr])) {
        alertErr("請輸入續卡註記 !!");
        wp.colSet(rr, "ok_flag", "!");
        return;
      }
      if (opt[rr].equals("0") && !aaDbAppr[rr].equals("N")) {
        alertErr("請執行【取消線上續卡(放行後)】!!");
        wp.colSet(rr, "ok_flag", "!");
        return;
      }
      if (opt[rr].equals("2") && !aaDbAppr[rr].equals("Y") && aaDbAppr[rr].length() != 0) {
        alertErr("請執行【取消線上續卡(放行前)】!!");
        wp.colSet(rr, "ok_flag", "!");
        return;
      }
      if (wfChkChangeStatus(opt[rr], aaDbChangeReason[rr]) != 1) {
        ilErr++;
        break;
      }
      if (wfDeleteCrdCardTmp() != 1) {
        ilErr++;
        break;
      }
      switch (opt[rr]) {
        case "0":
          break;
        case "1":
          if (wfMoveEmbossTmp() != 1) {
            ilErr++;
          }
          break;
        case "2":
          if (wfCancelChg() != 1) {
            ilErr++;
          }
          break;
        case "3":
          if (wfProcessChg() != 1) {
            ilErr++;
          }
          break;
      }
      if (ilErr > 0) {
        break;
      }
    }
    if (ilErr > 0) {
      wp.colSet(rr, "ok_flag", "!");
      alertErr("執行處理失敗," + msg);
      sqlCommit(0);
      return;
    }
    sqlCommit(1);
    queryFunc();
    errmsg("執行處理成功");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}

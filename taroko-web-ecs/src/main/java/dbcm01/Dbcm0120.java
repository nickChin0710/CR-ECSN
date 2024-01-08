/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-21  V1.00.00  Andy       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 107-07-30  V1.00.02  Andy       Update : Complete program                  *
* 108-12-03  V1.00.03  Amber	  Update init_button Authority 	     		 *
* 108-12-17  V1.00.04  ryan		  update : ptr_group_card==>crd_item_unit    *
* 109-04-27 V1.00.05  yanghan  修改了變量名稱和方法名稱*
* 109-12-25  V1.00.06  Justin       sql parameterize
* 109-01-04  V1.00.04   shiyuqi       修改无意义命名                                                                                      *  
* ****************************************************************************/

package dbcm01;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Dbcm0120 extends BaseEdit {
  String mExCardType = "";
  String mExGroupCode = "";
  String mExTscBinNo = "";
  String dbExpireReason = "";
  String gsOptcode = "";
  String[] aaRowid = null;
  String[] aaDbOptcode = null;
  String[] aaDbChangeReason = null;
  String[] aaDbAppr = null;
  String[] aaCardNo = null;
  String[] aaExpireChgFlag = null;
  String[] aaReissueStatus = null;
  String[] aaChangeStatus = null;
  String[] aaChangeReason = null;
  String[] aaSupFlag = null;
  String[] aaMajorCardno = null;
  String[] aaNewEndDate = null;
  String[] aaIdPSeqno = null;
  String[] aaPSeqno = null;
  String[] aaIdNo = null;
  String[] aaIdNoCode = null;
  String[] aaCorpNo = null;
  String[] aaChangeDate = null;
  String[] aaCurEndDate = null;
  String[] aaCurBegDate = null;
  String[] aaExpireReason = null;
  String[] aaCardType = null;
  String[] aaUnitCode = null;
  String[] aaRegBankNo = null;
  String[] aaRiskBankNo = null;
  String[] aaIcFlag = null;
  String[] aaMajorIdPSeqno = null;
  String[] aaEmbossData = null;
  String[] aaGroupCode = null;
  String[] aaSourceCode = null;
  String[] aaAcctType = null;
  String[] aaChiName = null;
  String[] aaEngName = null;
  String[] aaForceFlag = null;
  String[] aaNewBegDate = null;
  String[] aaAcctKey = null;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      saveFunc();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    wp.whereStr = "";
    int cnt = 0;
    String exCardNo = wp.itemStr("ex_card_no");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCorpNo = wp.itemStr("ex_corp_no");
    wp.whereStr = " where 1=1 ";
    // 固定條件
    wp.whereStr += "and ( (current_code = '0' "
        + "and decode(change_status,'','3',change_status) in ('3','4') ) "
        + "or change_status = '1' ) ";
    // 自鍵條件
    if (empty(exCardNo) == false) {
      cnt += 1;
    }
    if (empty(exIdNo) == false) {
      cnt += 1;
    }
    if (empty(exCorpNo) == false) {
      cnt += 1;
    }
    if (cnt == 0) {
      alertMsg("請至少輸入一項查詢鍵值 !!");
      return false;
    }

    if (empty(exCardNo) == false) {
      wp.whereStr += sqlCol(exCardNo, "a.card_no");
    }
    if (empty(exIdNo) == false) {
      wp.whereStr += sqlCol(exIdNo, "b.id_no");
    }
    if (empty(exCorpNo) == false) {
      wp.whereStr += sqlCol(exCorpNo, "a.corp_no");
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false) {
      return;
    }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex (a.rowid) AS rowid, a.mod_seqno, " + "b.id_no, " + "b.id_no_code, "
        + "b.chi_name, " + "(b.id_no||'_'||b.id_no_code) as db_id_no, " + "a.card_no, "
        + "a.corp_no, " + "a.card_type, " + "a.group_code, " + "a.new_end_date, "
        + "a.change_status, " + "a.expire_chg_flag, " + "a.sup_flag, " + "a.major_card_no, "
        + "' ' db_optcode, " + "a.expire_chg_date, " + "a.id_p_seqno, " + "a.corp_p_seqno, "
        + "a.acct_type, " + "a.p_seqno, " + "a.urgent_flag, " + "a.source_code, "
        + "a.son_card_flag, " + "a.major_relation, " + "a.major_id_p_seqno, " + "a.member_id, "
        + "a.current_code, " + "a.force_flag, " + "a.eng_name, " + "a.reg_bank_no, "
        + "a.unit_code, " + "a.old_beg_date, " + "a.old_end_date, " + "a.new_beg_date, "
        + "a.issue_date, " + "a.emergent_flag, " + "a.reissue_date, " + "a.reissue_reason, "
        + "a.reissue_status, " + "a.change_reason, " + "a.change_date, " + "a.upgrade_status, "
        + "a.upgrade_date, " + "a.apply_no, " + "a.promote_dept, " + "a.promote_emp_no, "
        + "a.introduce_emp_no, " + "a.introduce_id, " + "a.introduce_name, " + "a.prod_no, "
        + "a.reward_amt, " + "a.intr_reason_code, " + "a.pvv, " + "a.cvv, " + "a.cvv2, "
        + "a.pvki, " + "a.batchno, " + "a.recno, " + "'        ' cur_end_date, "
        + "'        ' cur_beg_date, " + "'   ' risk_bank_no, "
        + "lpad (' ', 8, ' ') major_valid_fm, " + "lpad (' ', 8, ' ') major_valid_to, "
        + "a.emboss_data, " + "a.clerk_id, " + "a.pin_block, " + "' ' db_change_reason, "
        + "a.ic_flag, " + "lpad (' ', 6, ' ') db_appr, " + "lpad (' ', 8, ' ') db_appr_date, "
        + "lpad (' ', 20, ' ') db_old_process, " + "lpad (' ', 20, ' ') db_expire_reason ";
    wp.daoTable = "dbc_card a LEFT JOIN DBC_IDNO b ON a.ID_P_SEQNO = b.ID_P_SEQNO ";
    wp.whereOrder = " order by a.card_no ";
    if (empty(wp.whereStr)) {
    	getWhereStr();
	}
    
    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr +
    // wp.whereOrder);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    String lsSql = "";
    String wkUnitCode = "", wkCardType = "", wkChangeStatus = "", wpCardNo = "",
        wpExpireReason = "";
    String wpExpireChgFlag = "";
    String lsBegVal = "", lsEndVal = "";

    wp.logSql = false;
    int liExtn = 0;
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      // 展期年
      wkUnitCode = wp.colStr(ii, "unit_code");
      wkCardType = wp.colStr(ii, "card_type");
      liExtn = wfGetExtnYear(wkUnitCode, wkCardType);
      if (liExtn <= 0) {
        liExtn = 2;
      }
      // -- 自動展效期,新有效期起日為'舊效期迄日之當月一號'
      // --- 起日為'舊有效期迄日'+展期年
      lsBegVal = strMid(getSysDate(), 0, 6) + "01";
      lsEndVal = wp.colStr(ii, "new_end_date");
      lsEndVal =
          numToStr(toNum(strMid(lsEndVal, 0, 4)) + liExtn, "###") + strMid(lsEndVal, 4, 4);
      wp.colSet(ii, "cur_beg_date", lsBegVal);
      wp.colSet(ii, "cur_end_date", lsEndVal);
      // change_status ==> db_change_reason
      wkChangeStatus = wp.colStr(ii, "change_status");
      if (empty(wkChangeStatus) == false) {
        if (Integer.parseInt(wkChangeStatus) > 3) {
          wp.colSet(ii, "db_change_reason", "");
        } else {
          wp.colSet(ii, "db_change_reason", wp.colStr(ii, "change_reason"));
        }
      }
      //
      if (wfGetAcnoData(ii) != 1) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "err_msg", "無法抓取到此卡號帳戶資料!!");
        continue;
      }
      //
      wpCardNo = wp.colStr(ii, "card_no");
      lsSql = "select process_kind, " + "change_reason, " + "change_status, " + "change_date, "
          + "expire_chg_flag, " + "expire_reason, " + "apr_date " + "from dbc_card_tmp "
          + "where 1=1 and kind_type = '120' ";
      lsSql += sqlCol(wpCardNo, "card_no");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        String[] cde = new String[] {"0", "1", "2", "3"};
        String[] txt = new String[] {"取消線上續卡(放行前)", "線上續卡", "取消線上續卡(放行後)", "系統續卡改不續卡"};
        wp.colSet(ii, "db_old_process", commString.decode(sqlStr("process_kind"), cde, txt));
        wp.colSet(ii, "db_change_reason", sqlStr("change_reason"));
        wp.colSet(ii, "change_status", sqlStr("change_status"));
        wp.colSet(ii, "change_date", sqlStr("change_date"));
        wp.colSet(ii, "expire_chg_flag", sqlStr("expire_chg_flag"));
        wp.colSet(ii, "db_appr_date", sqlStr("apr_date"));
        wp.colSet(ii, "db_appr", "N");
        wpExpireChgFlag = sqlStr("expire_chg_flag");
        wpExpireReason = sqlStr("expire_reason");
        wfDbExpireReason(wpExpireChgFlag, wpExpireReason);
        wp.colSet(ii, "db_expire_reason", dbExpireReason);
      } else {
        lsSql = "select process_kind, " + "change_reason, " + "change_status, " + "change_date, "
            + "apr_date " + "from dbc_card_tmp_h " + "where 1=1 and kind_type = '120' ";
        lsSql += sqlCol(wpCardNo, "card_no");
        lsSql +=
            "and apr_date in (select max(apr_date) from dbc_card_tmp_h where 1=1 and kind_type = '120' ";
        lsSql += sqlCol(wpCardNo, "card_no") + " ) ";
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          wp.colSet(ii, "db_appr_date", sqlStr("apr_date"));
          wp.colSet(ii, "db_appr", "Y");
          lsSql = "select change_reason, " + "expire_chg_flag, " + "expire_reason, "
              + "change_status, " + "change_date " + "from dbc_card " + "where 1=1 ";
          lsSql += sqlCol(wpCardNo, "card_no");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            wp.colSet(ii, "change_date", sqlStr("change_date"));
            wp.colSet(ii, "db_change_reason", sqlStr("change_reason"));
            wp.colSet(ii, "change_status", sqlStr("change_status"));
            wp.colSet(ii, "expire_chg_flag", sqlStr("expire_chg_flag"));
            wp.colSet(ii, "db_old_process", sqlStr("process_kind"));
            wpExpireChgFlag = sqlStr("expire_chg_flag");
            wpExpireReason = sqlStr("expire_reason");
            wfDbExpireReason(wpExpireChgFlag, wpExpireReason);
            wp.colSet(ii, "db_expire_reason", dbExpireReason);
          }
        } else {
          lsSql = "select change_reason, " + "expire_chg_flag, " + "expire_reason, "
              + "change_status, " + "change_date " + "from dbc_card " + "where 1=1 ";
          lsSql += sqlCol(wpCardNo, "card_no");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            wp.colSet(ii, "change_date", sqlStr("change_date"));
            wp.colSet(ii, "db_change_reason", sqlStr("change_reason"));
            wp.colSet(ii, "change_status", sqlStr("change_status"));
            wp.colSet(ii, "db_appr", "");
            wp.colSet(ii, "expire_chg_flag", sqlStr("expire_chg_flag"));
            wp.colSet(ii, "db_old_process", sqlStr("process_kind"));
            wpExpireChgFlag = sqlStr("expire_chg_flag");
            wpExpireReason = sqlStr("expire_reason");
            wfDbExpireReason(wpExpireChgFlag, wpExpireReason);
            wp.colSet(ii, "db_expire_reason", dbExpireReason);
          }
        }
      }
    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  // 展期年數
  public int wfGetExtnYear(String unitCode, String cardType) {
    String lsSql = "";
    lsSql = "select extn_year from crd_item_unit where 1=1 ";
    lsSql += sqlCol(unitCode, "unit_code");
    lsSql += sqlCol(cardType, "card_type");
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      return -1;
    }
    return Integer.parseInt(sqlStr("extn_year"));
  }

  // 卡號帳戶資料
  public int wfGetAcnoData(int alRow) {
    String lsPSeqno = "";
    String lsSql = "";

    // ls_p_seqno = aa_p_seqno[al_row];
    lsPSeqno = wp.colStr(alRow, "p_seqno");
    lsSql = "select acct_key,risk_bank_no from dba_acno where 1=1 and p_seqno =:p_seqno ";
    setString("p_seqno", lsPSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      return -1;
    } else {
      wp.colSet(alRow, "acct_key", sqlStr("acct_key"));
      wp.colSet(alRow, "risk_bank_no", sqlStr("risk_bank_no"));
    }
    return 1;
  }

  // set db_expire_reason
  public void wfDbExpireReason(String expireChgFlag, String expireReason) {
    String lsSql = "";
    if (expireChgFlag.equals("1")) {
      lsSql = "select wf_desc from ptr_sys_idtab where 1=1 and wf_type = 'NOTCHG_VD_S' ";
      lsSql += sqlCol(expireReason, "wf_id");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        dbExpireReason = expireReason + "-" + sqlStr("wf_desc");
      } else {
        dbExpireReason = expireReason;
      }
    }
    if (expireChgFlag.equals("2")) {
      lsSql = "select wf_desc from ptr_sys_idtab where 1=1 and wf_type = 'NOTCHG_VD_O' ";
      lsSql += sqlCol(expireReason, "wf_id");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        dbExpireReason = expireReason + "-" + sqlStr("wf_desc");
      } else {
        dbExpireReason = expireReason;
      }
    }
    if (expireChgFlag.equals("3")) {
      lsSql = "select wf_desc from ptr_sys_idtab where 1=1 and wf_type = 'NOTCHG_VD_M' ";
      lsSql += sqlCol(expireReason, "wf_id");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        dbExpireReason = expireReason + "-" + sqlStr("wf_desc");
      } else {
        dbExpireReason = expireReason;
      }
    }
    return;
  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_pp_card_no = wp.item_ss("pp_card_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    aaRowid = wp.itemBuff("rowid");
    aaDbOptcode = wp.itemBuff("db_optcode");
    aaDbChangeReason = wp.itemBuff("db_change_reason");
    aaDbAppr = wp.itemBuff("db_appr");
    aaCardNo = wp.itemBuff("card_no");
    aaExpireChgFlag = wp.itemBuff("expire_chg_flag");
    aaReissueStatus = wp.itemBuff("reissue_status");
    aaChangeStatus = wp.itemBuff("change_status");
    aaChangeReason = wp.itemBuff("change_reason");
    aaSupFlag = wp.itemBuff("sup_flag");
    aaMajorCardno = wp.itemBuff("major_cardno");
    aaNewEndDate = wp.itemBuff("new_end_date");
    aaIdPSeqno = wp.itemBuff("id_p_seqno");
    aaPSeqno = wp.itemBuff("p_seqno");
    aaIdNo = wp.itemBuff("id_no");
    aaIdNoCode = wp.itemBuff("id_no_code");
    aaChangeDate = wp.itemBuff("change_date");
    aaCurEndDate = wp.itemBuff("cur_end_date");
    aaCurBegDate = wp.itemBuff("cur_beg_date");
    aaExpireReason = wp.itemBuff("expire_reason");
    aaCardType = wp.itemBuff("card_type");
    aaUnitCode = wp.itemBuff("unit_code");
    aaRiskBankNo = wp.itemBuff("risk_bank_no");
    aaIcFlag = wp.itemBuff("ic_flag");
    aaMajorIdPSeqno = wp.itemBuff("major_id_p_seqno");
    aaEmbossData = wp.itemBuff("emboss_data");
    aaGroupCode = wp.itemBuff("group_code");
    aaSourceCode = wp.itemBuff("source_code");
    aaEngName = wp.itemBuff("eng_name");
    aaForceFlag = wp.itemBuff("force_flag");
    aaNewBegDate = wp.itemBuff("new_beg_date");
    aaAcctKey = wp.itemBuff("acct_key");
    aaCorpNo = wp.itemBuff("corp_no");
    aaRegBankNo = wp.itemBuff("reg_bank_no");
    aaAcctType = wp.itemBuff("acct_type");
    //
    wp.listCount[0] = aaRowid.length;

    // int rr = -1;
    int liOk = 0, err = 0;

    for (int ii = 0; ii < aaRowid.length; ii++) {
      // check
      String wkDbOptcode = aaDbOptcode[ii];
      gsOptcode = aaDbOptcode[ii];
      if (empty(wkDbOptcode)) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "err_msg", "未處理");
        continue;
      }
      if (wkDbOptcode.equals("1")) {
        if (empty(aaDbChangeReason[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "請輸入續卡註記!!");
          err++;
          continue;
        }
      }
      String wkDbAppr = aaDbAppr[ii];
      if (wkDbOptcode.equals("0") && wkDbAppr.equals("N") == false) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "err_msg", "請執行【取消線上續卡(放行後)】!!");
        err++;
        continue;
      }
      if (wkDbOptcode.equals("2") && wkDbAppr.equals("Y") == false
          && wkDbAppr.length() != 0) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "err_msg", "請執行【取消線上續卡(放行前)】!!");
        err++;
        continue;
      }
      String wkCardNo = aaCardNo[ii];
      // ***原wf_chk_change_status()
      if (wkDbOptcode.equals("1")) {
        if (empty(aaExpireChgFlag[ii]) == false) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "已在不續卡狀態下,不可提前續卡!!");
          err++;
          continue;
        }
        if (aaReissueStatus[ii].equals("1") || aaReissueStatus[ii].equals("2")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "已在重製卡狀態下,不可提前續卡!!");
          err++;
          continue;
        }
        if (empty(aaDbChangeReason[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "需輸入續卡註記欄位值!!");
          err++;
          continue;
        }
      }

      if (wkDbOptcode.equals("2")) {
        if (aaChangeStatus[ii].equals("2")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "已送製卡,不可取消提前續卡!!");
          err++;
          continue;
        }
      }
      if (wkDbOptcode.equals("3")) {
        if (empty(aaChangeStatus[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "此卡片不在續卡狀態下,不可做系統續卡改系統不續卡!!");
          err++;
          continue;
        }
        if (aaChangeStatus[ii].equals("2")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "此卡片已在續卡待製卡中!!");
          err++;
          continue;
        }
        if (aaChangeReason[ii].equals("1") == false) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "只能系統續卡改系統不續卡!!");
          err++;
          continue;
        }
      }
      // ***end 原wf_chk_change_status()

      switch (wkDbOptcode) {
        case "0":
          if (wfDeleteDbcCardTmp(wkCardNo) != 1) {
            wp.colSet(ii, "ok_flag", "X");
            wp.colSet(ii, "err_msg", "delete dbc_card_temp error!!");
            err++;
            continue;
          } else {
            liOk++;
          }
          break;
        case "1":
          wfDeleteDbcCardTmp(wkCardNo);
          if (wfMoveEmbossTmp(ii) != 1) {
            err++;
            continue;
          } else {
            liOk++;
          }
          break;
        case "2":
          wfDeleteDbcCardTmp(wkCardNo);
          if (wfCancelChg(ii) != 1) {
            err++;
          } else {
            liOk++;
          }

          break;
        case "3":
          wfDeleteDbcCardTmp(wkCardNo);
          if (wfProcessChg(ii) != 1) {
            err++;
          } else {
            liOk++;
          }
          break;
      }
    }
    if (liOk > 0) {
      sqlCommit(1);
    } else {
      sqlCommit(0);
    }
    alertMsg("資料修改: 成功=" + liOk + " 失敗=" + err);
  }

  public int wfDeleteDbcCardTmp(String cardNo) {
    String dsSql = "delete dbc_card_tmp where 1=1 and kind_type = '120' and card_no =:card_no ";
    setString("card_no", cardNo);
    sqlExec(dsSql);
    if (sqlRowNum <= 0) {
      return -1;
    }
    return 1;
  }

  public int wfMoveEmbossTmp(int ii) throws Exception {
    String lsSql = "", usSql = "", lsMajorIdPSeqno = "", lsMajorIdNo = "",
        lsMajorIdNoCode = "";
    String lsCardno = "", lsBatchno1 = "", lsBatchno = "";
    // String ls_chg_addr_date="";
    String lsIdnoValue1 = "", lsIdnoValue2 = "", lsCreateDate = "";
    String lsIdPSeqno = "", lsPSeqno = "", lsSupFlag = "";
    String lsValidDate = "", lsChk, lsDateFm = "", lsDateTo = "";
    String lsChangeStatus = "", lsChangeReason = "";
    String lsMajorCardno = "", lsMajorValidFm = "", lsMajorValidTo = "",
        lsMajorCurrentCode = "";
    double liActCreditAmt = 0, liSystemd = 0;
    int liRecno = 0;

    lsChangeStatus = aaChangeStatus[ii];
    lsChangeReason = aaDbChangeReason[ii];
    if (lsChangeStatus.equals("1")) {
      wp.colSet(ii, "ok_flag", "X");
      wp.colSet(ii, "err_msg", "續卡製卡中，不可再做續卡!!");
      return -1;
    }

    if (lsChangeStatus.equals("2")) {
      wp.colSet(ii, "ok_flag", "X");
      wp.colSet(ii, "err_msg", "已送製卡中,不可再做續卡!!");
      return -1;
    }

    lsCardno = aaCardNo[ii];
    lsSql = "select count(*) ct from  dbc_emboss_tmp where 1=1";
    lsSql += sqlCol(lsCardno, "old_card_no");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      if (Integer.parseInt(sqlStr("ct")) > 0) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "err_msg", "續卡製卡中，不可再做續卡!!");
        return -1;
      }
    }

    // -- 附卡抓取正卡效期,正卡效期 > sysdate+6
    lsSupFlag = aaSupFlag[ii];
    lsMajorCardno = aaMajorCardno[ii];
    if (lsSupFlag.equals("1")) {
      lsSql = "select new_beg_date, " + "new_end_date, " + "current_code " + "from dbc_card "
          + "where 1=1 ";
      lsSql += sqlCol(lsMajorCardno, "card_no");
      sqlSelect(lsSql);
      if (sqlRowNum <= 0) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "err_msg", "找取不到正卡資料!!");
        return -1;
      } else {
        lsMajorValidFm = sqlStr("new_beg_date");
        lsMajorValidTo = sqlStr("new_end_date");
        lsMajorCurrentCode = sqlStr("current_code");
      }
      if (lsMajorCurrentCode.equals("0") == false) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "err_msg", "正卡不為正常卡,不可做線上續卡!!");
        return -1;
      }
    }

    lsCreateDate = getSysDate();
    lsValidDate = aaNewEndDate[ii];
    lsChk = preMonth(lsValidDate);
    if (lsChk.compareTo(lsCreateDate) == 1) {
      wp.colSet(ii, "ok_flag", "X");
      wp.colSet(ii, "err_msg", "效期需在系統日六個月內!!");
      return -1;
    }

    // -- 由覆核處理資料
    if (wfInsertDbcCardTmp(ii, 1) != 1) {
      return -1;
    } else {
      return 1;
    }
    // 以下都白寫了Andy 20181024
    // --Get Batchno--
    // ls_batchno1 = ss_mid(ls_create_date, 2, 6);
    // ls_sql = "select max(batchno) max_batchno from dbc_emboss_tmp where substr(batchno,1,6)
    // =:ls_batchno1 ";
    // setString("ls_batchno1", ls_batchno1);
    // sqlSelect(ls_sql);
    // if (sql_nrow > 0) {
    // ls_batchno = sql_ss("max_batchno");
    // if (empty(ls_batchno)) {
    // ls_batchno = ls_batchno1 + "01";
    // } else {
    // ls_sql = "select (max(recno)+1) max_recno from dbc_emboss_tmp where batchno =:ls_batchno ";
    // setString("ls_batchno", ls_batchno);
    // sqlSelect(ls_sql);
    // if (sql_nrow > 0) {
    // if (sql_ss("max_recno").equals("0")) {
    // li_recno = 1;
    // } else {
    // li_recno = Integer.parseInt(sql_ss("max_recno"));
    // }
    // } else {
    // li_recno = 1;
    // }
    // }
    //
    // } else {
    // ls_batchno = ls_batchno1 + "01";
    // }
    //
    // // select col from dbc_idno
    // ls_id_p_seqno = aa_id_p_seqno[ii];
    // ls_sql = "select chi_name, birthday from dbc_idno where id_p_seqno =:ls_id_p_seqno ";
    // setString("ls_id_p_seqno", ls_id_p_seqno);
    // sqlSelect(ls_sql);
    // if (sql_nrow <= 0) {
    // wp.col_set(ii, "ok_flag", "X");
    // wp.col_set(ii, "err_msg", "抓取卡人檔失敗!!");
    // return -1;
    // } else {
    // ls_idno_value1 = sql_ss("chi_name");
    // ls_idno_value2 = sql_ss("birthday");
    // }
    //
    // // select col from dba_acno
    // ls_p_seqno = aa_p_seqno[ii];
    // ls_sql = "select line_of_credit_amt,chg_addr_date from dba_acno where p_seqno =:ls_p_seqno ";
    // setString("ls_p_seqno", ls_p_seqno);
    // sqlSelect(ls_sql);
    // if (sql_nrow <= 0) {
    // wp.col_set(ii, "ok_flag", "X");
    // wp.col_set(ii, "err_msg", "抓取dba_acno檔失敗!!");
    // return -1;
    // } else {
    // li_act_credit_amt = to_Num(sql_ss("line_of_credit_amt"));
    // // ls_chg_addr_date = sql_ss("chg_addr_date"); //没用到
    // }
    //
    // ls_sup_flag = aa_sup_flag[ii];
    // ls_major_id_p_seqno = aa_major_id_p_seqno[ii];
    // if (ls_sup_flag.equals("1")) {
    // ls_sql = "select id_no, id_no_code from dbc_idno where id_p_seqno =:id_p_seqno ";
    // setString("id_p_seqno", ls_major_id_p_seqno);
    // sqlSelect(ls_sql);
    // if (sql_nrow > 0) {
    // ls_major_id_no = sql_ss("id_no");
    // ls_major_id_no_code = sql_ss("id_no_code");
    // }
    // }
    //
    // // update dbc_card
    // us_sql = "update dbc_card set "
    // + "expire_chg_flag='', "
    // + "expire_chg_date='', "
    // + "change_reason=:ls_change_reason, "
    // + "change_date = to_char(sysdate,'yyyymmdd'), "
    // + "change_status='1' "
    // + "where card_no =:ls_cardno ";
    // setString("ls_change_reason", ls_change_reason);
    // setString("ls_cardno", ls_cardno);
    // sqlExec(us_sql);
    // if (sql_nrow < 0) {
    // wp.col_set(ii, "ok_flag", "X");
    // wp.col_set(ii, "err_msg", "寫入卡片檔錯誤~");
    // return -1;
    // }
    //
    // // insert dbc_emboss_tmp
    // busi.SqlPrepare sp = new SqlPrepare();
    // sp.sql2Insert("dbc_emboss_tmp");
    // sp.ppss("batchno", ls_batchno);
    // sp.ppnum("recno", li_recno);
    // sp.ppss("emboss_source", "4"); // 提前續卡
    // sp.ppss("to_nccc_code", "Y");
    // sp.ppss("card_type", aa_card_type[ii]);
    // sp.ppss("unit_code", aa_unit_code[ii]);
    // // sp.ppss("member_note", aa_member_note[ii]); //dbc_card 無此欄位
    // sp.ppss("reg_bank_no", aa_reg_bank_no[ii]);
    // sp.ppss("risk_bank_no", aa_risk_bank_no[ii]);
    // sp.ppss("status_code", "1"); // --換卡
    // sp.ppss("card_no", aa_card_no[ii]);
    // sp.ppss("old_card_no", aa_card_no[ii]);
    // sp.ppss("change_reason", aa_db_change_reason[ii]);
    // sp.ppss("nccc_type", "2"); // -- 換卡格式
    // sp.ppss("reason_code", "");
    // sp.ppss("apply_id", aa_id_no[ii]);
    // sp.ppss("apply_id_code", aa_id_no_code[ii]);
    // sp.ppss("ic_flag", aa_ic_flag[ii]);
    // if (ls_sup_flag.equals("1")) {
    // sp.ppss("pm_id", ls_major_id_no);
    // sp.ppss("pm_id_code", ls_major_id_no_code);
    // sp.ppss("major_card_no", aa_ic_flag[ii]);
    // sp.ppss("major_valid_fm", ls_major_valid_fm);
    // sp.ppss("major_valid_to", ls_major_valid_to);
    // } else {
    // sp.ppss("pm_id", ls_major_id_no);
    // sp.ppss("pm_id_code", ls_major_id_no_code);
    // }
    // sp.ppss("emboss_4th_data", aa_emboss_data[ii]);
    // sp.ppss("group_code", aa_group_code[ii]);
    // sp.ppss("source_code", aa_source_code[ii]);
    // sp.ppss("corp_no", aa_corp_no[ii]);
    // // sp.ppss("corp_no_code", aa_corp_no_code[ii]); //dbc_card 無此欄位
    // sp.ppss("acct_type", aa_acct_type[ii]);
    // sp.ppss("acct_key", aa_acct_key[ii]);
    // sp.ppss("chi_name", ls_idno_value1);
    // sp.ppss("eng_name", aa_eng_name[ii]);
    // sp.ppss("birthday", ls_idno_value2);
    // sp.ppnum("credit_lmt", li_act_credit_amt);
    // sp.ppss("force_flag", aa_force_flag[ii]);
    // sp.ppss("old_beg_date", aa_new_beg_date[ii]);
    // sp.ppss("old_end_date", aa_new_end_date[ii]);
    // ls_date_fm = aa_cur_beg_date[ii];
    // ls_date_to = aa_cur_end_date[ii];
    // li_system_dd = to_Num(ss_mid(ls_create_date, 6, 2));
    // if (li_system_dd >= 25) {
    // ls_sql = " select (to_char(add_months(to_date(:ls_date_fm,'yyyymmdd'),1),'yyyymm')||'01') as
    // new_ls_date_fm from dual ";
    // setString("ls_date_fm", ls_date_fm);
    // sqlSelect(ls_sql);
    // if (sql_nrow <= 0) {
    // wp.col_set(ii, "ok_flag", "X");
    // wp.col_set(ii, "err_msg", "日期資料轉換錯誤 !!");
    // return -1;
    // } else {
    // ls_date_fm = sql_ss("new_ls_date_fm");
    // }
    // }
    // sp.ppss("valid_fm", ls_date_fm);
    // sp.ppss("valid_to", ls_date_to);
    // sp.ppss("sup_flag", aa_sup_flag[ii]);
    // sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    // sp.ppss("crt_user", wp.loginUser);
    // sp.ppss("mod_user", wp.loginUser);
    // sp.ppss("mod_pgm", wp.mod_pgm());
    // sp.ppnum("mod_seqno", 1);
    // sp.addsql(", mod_time ", ", sysdate ");
    //
    // sqlExec(sp.sql_stmt(), sp.sql_parm());
    // if (sql_nrow <= 0) {
    // wp.col_set(ii, "ok_flag", "X");
    // wp.col_set(ii, "err_msg", "此資料無法搬到續卡檔內");
    // return -1;
    // }
    //
    // return 1;
  }

  public int wfCancelChg(int asRow) {
    String lsChangeStatus = "";

    lsChangeStatus = aaChangeStatus[asRow];
    if (empty(lsChangeStatus)) {
      wp.colSet(asRow, "ok_flag", "X");
      wp.colSet(asRow, "err_msg", "該卡為不續卡狀態下，不需取消續卡");
      return -1;
    }
    if (lsChangeStatus.equals("2")) {
      wp.colSet(asRow, "ok_flag", "X");
      wp.colSet(asRow, "err_msg", "此卡片已送製卡,不可取消續卡");
      return -1;
    }

    // -- 由覆核處理資料
    if (wfInsertDbcCardTmp(asRow, 2) != 1) {
      return -1;
    }
    return 1;
  }

  public int wfProcessChg(int asRow) {
    String lsCardNo = "", lsExpireChgFlag = "";
    String lsChangeStatus = "";
    String usSql = "", dsSql = "";
    lsCardNo = aaCardNo[asRow];
    lsExpireChgFlag = aaExpireChgFlag[asRow];
    lsChangeStatus = aaChangeStatus[asRow];

    if (empty(lsExpireChgFlag) == false) {
      wp.colSet(asRow, "ok_flag", "X");
      wp.colSet(asRow, "err_msg", "不可重複做預約不續卡!!");
      sqlCommit(0);
      return -1;
    }
    if (lsChangeStatus.equals("2")) {
      wp.colSet(asRow, "ok_flag", "X");
      wp.colSet(asRow, "err_msg", "已在續卡製卡中,不可改為不續卡");
      sqlCommit(0);
      return -1;
    }
    // -- 由覆核處理資料
    if (wfInsertDbcCardTmp(asRow, 1) != 1) {
      return -1;
    } else {
      return 1;
    }
    // 刪除資料
    // ds_sql = "delete from dbc_emboss_tmp where old_card_no = :ls_card_no ";
    // setString("ls_card_no", ls_card_no);
    // sqlExec(ds_sql);
    // if (sql_nrow <= 0) {
    // wp.col_set(as_row, "ok_flag", "X");
    // wp.col_set(as_row, "err_msg", "已製卡完成不在暫存檔內");
    // sql_commit(0);
    // return -1;
    // }
    // // -- update卡片主檔改為系統不續卡
    // us_sql = "update dbc_card "
    // + "set expire_chg_flag = '1', "
    // + "expire_reason = '', "
    // + "expire_chg_date = to_char(sysdate,'yyyymmdd'), "
    // + "change_status = '', "
    // + "change_reason = '', "
    // + "change_date = '' "
    // + "where card_no =:ls_card_no ";
    // setString("ls_card_no", ls_card_no);
    // sqlExec(us_sql);
    // if (sql_nrow <= 0) {
    // wp.col_set(as_row, "ok_flag", "X");
    // wp.col_set(as_row, "err_msg", "寫入不續卡註記失敗");
    // sql_commit(0);
    // return -1;
    // }
    // return 1;
  }

  public int wfInsertDbcCardTmp(int asRow, int asKind) {
    String lsCardNo = "", lsOldEndDate = "", lsCurEndDate = "", lsProcessKind = "",
        lsCurBegDate = "";
    String lsChangeDate = "", lsChangeReason = "", lsChangeStatus = "";
    String lsChangeDateOld = "", lsChangeReasonOld = "", lsChangeStatusOld = "",
        lsDbOptcode = "";
    String lsCreateUser = "", lsCreateDate = "", lsCorpNo = "";
    String lsSql = "";

    lsDbOptcode = aaDbOptcode[asRow];
    lsCardNo = aaCardNo[asRow];
    lsProcessKind = aaDbOptcode[asRow];
    lsCorpNo = aaCorpNo[asRow];
    lsChangeDate = getSysDate();

    if (lsDbOptcode.equals("0") || lsDbOptcode.equals("2")) {
      lsChangeReason = "";
      lsChangeStatus = "";
    } else {
      lsChangeReason = aaDbChangeReason[asRow];
      lsChangeStatus = aaChangeStatus[asRow];
    }
    lsChangeDateOld = aaChangeDate[asRow];
    lsChangeReasonOld = aaChangeReason[asRow];
    lsChangeStatusOld = aaChangeStatus[asRow];
    lsOldEndDate = aaNewEndDate[asRow];
    lsCurEndDate = aaCurEndDate[asRow];
    lsCurBegDate = aaCurBegDate[asRow];
    lsCreateUser = wp.loginUser;
    lsCreateDate = getSysDate();

    lsSql = "select count(*) ct " + "from dbc_card_tmp " + "where card_no =:ls_card_no "
        + "and kind_type = '120' ";
    setString("ls_card_no", lsCardNo);
    sqlSelect(lsSql);
    if (sqlNum("ct") > 0) {
      wp.colSet(asRow, "ok_flag", "X");
      wp.colSet(asRow, "err_msg", "已經處理提前續卡過,請先查詢資料(dbc_card_tmp) !");
      sqlCommit(0);
      return -1;
    }
    // insert dbc_card_tmp
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("dbc_card_tmp");
    sp.ppstr("card_no", lsCardNo);
    sp.ppstr("id_p_seqno", aaIdPSeqno[asRow]);
    sp.ppstr("corp_no", lsCorpNo);
    sp.ppstr("kind_type", "120");
    sp.ppstr("process_kind", lsProcessKind);
    sp.ppstr("change_reason", lsChangeReason);
    sp.ppstr("change_status", lsChangeStatus);
    sp.ppstr("change_date", lsChangeDate);
    sp.ppstr("change_reason_old", lsChangeReasonOld);
    sp.ppstr("change_status_old", lsChangeStatusOld);
    sp.ppstr("change_date_old", lsChangeDateOld);
    sp.ppstr("old_end_date", lsOldEndDate);
    sp.ppstr("cur_beg_date", lsCurBegDate);
    sp.ppstr("cur_end_date", lsCurEndDate);
    sp.ppstr("crt_user", lsCreateUser);
    sp.ppstr("crt_date", lsCreateDate);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", "Dbcm0120");
    sp.ppnum("mod_seqno", 1);
    sp.addsql(", mod_time ", ", sysdate ");

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      wp.colSet(asRow, "ok_flag", "X");
      wp.colSet(asRow, "err_msg", "寫入卡片暫存檔錯誤~");
      sqlCommit(0);
      return -1;
    }
    return 1;
  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void dddwSelect() {
    try {
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("group_code");
      // this.dddw_list("dddw_group_code", "ptr_group_code", "group_code",
      // "group_name", " where 1=1 order by group_code");
      // wp.optionKey = wp.item_ss("card_type");
      // this.dddw_list("dddw_card_type", "ptr_card_type", "card_type",
      // "name", " where 1=1 order by card_type");
      // this.dddw_list("dddw_group_code_h", "ptr_group_code",
      // "group_code", "group_name", " where 1=1 order by group_code");
      // this.dddw_list("dddw_card_type_h", "ptr_card_type", "card_type",
      // "name", " where 1=1 order by card_type");
    } catch (Exception ex) {
    }
  }

  // 系統日往前推6個月
  public String preMonth(String date) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Date startdate = (Date) sdf.parse(date);
    Calendar start = Calendar.getInstance();
    start.setTime(startdate);
    start.add(Calendar.MONTH, -6);
    String db_pre_month_date = sdf.format(start.getTime());
    return db_pre_month_date;
  }
}

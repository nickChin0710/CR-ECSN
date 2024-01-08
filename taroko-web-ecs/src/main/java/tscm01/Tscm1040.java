/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-07-07  V1.00.00  Ryan       program initial  
* 111-04-14  V1.00.01  machao     TSC畫面整合                          *
******************************************************************************/

package tscm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Tscm1040 extends BaseEdit {
  String mExCardType = "";
  String mExGroupCode = "";
  String mExTscBinNo = "";

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
    } else if (eqIgno(wp.buttonCode, "item_change")) {
      /* 執行 */
      strAction = "item_change";
      itemChange();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    wp.whereStr = "";
    String exCardNo = wp.itemStr("ex_card_no");
    String exTscCardNo = wp.itemStr("ex_tsc_card_no");
    String exDate1 = wp.itemStr("ex_date1");
    wp.whereStr = " where 1=1 ";
    // 固定條件

    // 自鍵條件
    if (empty(exCardNo) && empty(exTscCardNo) && empty(exDate1)) {
      alertErr("請至少輸入一項查詢條件!!");
      return false;
    }

    if (empty(exCardNo) == false) {
      wp.whereStr += sqlCol(exCardNo, "card_no");
    }
    if (empty(exTscCardNo) == false) {
      wp.whereStr += sqlCol(exTscCardNo, "tsc_card_no");
    }
    if (empty(exDate1) == false) {
      wp.whereStr += sqlCol(exDate1, "create_date");
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex (rowid) AS rowid, " + "tsc_card_no, " + "tsc_card_no as tt_tsc_card_no, "
        + "card_no, " + "card_no as tt_card_no, " + "create_date, "
        + "balance_date_plan, " + "balance_date, "
        + "balance_date_rtn, " + "appr_user, " + "appr_date, " + "mod_user, " + "mod_time, "
        + "mod_pgm, " + "mod_seqno, " + "emboss_kind, " + "emboss_kind as tt_emboss_kind ";
    wp.daoTable = "tsc_dcbq_log";
    wp.whereOrder = " order by create_date,tsc_card_no,card_no ";
    if (getWhereStr() == false)
      return;

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
    wp.colSet("h_mod_user", wp.loginUser);
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    // update main table [crd_emap_tmp] use
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] opt = wp.itemBuff("opt");

    String[] aaTscCardNo = wp.itemBuff("tsc_card_no");
    String[] aaCardNo = wp.itemBuff("card_no");
    String[] aaEmbossKind = wp.itemBuff("emboss_kind");
    String aaCreateDate = getSysDate();
    String[] aaBalanceDate = wp.itemBuff("balance_date");
    String[] aaTtTscCardNo = wp.itemBuff("tt_tsc_card_no");
    String[] aaTtCardNo = wp.itemBuff("tt_card_no");
    String[] aaTtEmbossKind = wp.itemBuff("tt_emboss_kind");

    String mRowid = "";
    //
    wp.listCount[0] = aaTscCardNo.length;
    String lsSql = "", lsNewEndDate = "";
    int llOk = 0, llErr = 0;

    for (int ii = 0; ii < aaTscCardNo.length; ii++) {
      if (checkBoxOptOn(ii, opt)) {
        if (empty(aaRowid[ii])) {
          continue;
        } else {
          if (empty(aaBalanceDate[ii]) == false) {
            wp.colSet(ii, "ok_flag", "X");
            wp.colSet(ii, "err_msg", "已傳送不可刪除");
            llErr++;
            continue;
          }
        }
      }
      if (!checkBoxOptOn(ii, opt)) {
        // 未異動資料不處理
        if (!empty(aaRowid[ii])) {
          if (aaTscCardNo[ii].equals(aaTtTscCardNo[ii]) && aaCardNo[ii].equals(aaTtCardNo[ii])
              && aaEmbossKind[ii].equals(aaTtEmbossKind[ii]))
            continue;
        }

        // tsc_card_no
        if (empty(aaTscCardNo[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "悠遊卡號必需輸入 !!");
          llErr++;
          continue;
        }

        // tsc_card_no
        lsSql = "select new_end_date ," + "current_code ," + "tsc_sign_flag "
            + "from tsc_vd_card where 1=1 ";
        lsSql += sqlCol(aaTscCardNo[ii], "tsc_card_no");
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "抓取TSCC卡檔失敗");
          llErr++;
          continue;
        } else {
          lsNewEndDate = sqlStr("new_end_date");
          if (lsNewEndDate.compareTo(getSysDate()) < 0) {
            wp.colSet(ii, "ok_flag", "X");
            wp.colSet(ii, "err_msg", "此卡號已到期" + lsNewEndDate);
            llErr++;
            continue;
          }
          if (sqlStr("current_code").equals("2") && sqlStr("tsc_sign_flag").equals("Y")) {
            wp.colSet(ii, "ok_flag", "X");
            wp.colSet(ii, "err_msg", "已掛失且為記名卡!!");
            llErr++;
            continue;
          }

        }
        //
        if (empty(aaBalanceDate[ii]) == false) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "已傳送不可修改");
          llErr++;
          continue;
        }
      }
    }
    
    if (llErr > 0) {
      alertMsg("資料有誤請重新檢核 !!");
      return;
    }

    // save
    if (llErr == 0) {
      for (int ii = 0; ii < aaTscCardNo.length; ii++) {
        if (checkBoxOptOn(ii, opt)) {
          if (deleteTscBtrqLog(aaRowid[ii], aaModSeqno[ii]) != 1) {
            wp.colSet(ii, "ok_flag", "X");
            wp.colSet(ii, "err_msg", "資料刪除失敗");
            llErr++;
          } else {
            llOk++;
            wp.colSet(ii, "ok_flag", "V");
          }
        }
        mRowid = nvl(aaRowid[ii]);
        // 未勾選資料處理
        if (checkBoxOptOn(ii, opt) == false) {
          // 未異動資料不處理
          if (!empty(aaRowid[ii])) {
            if (aaTscCardNo[ii].equals(aaTtTscCardNo[ii]) && aaCardNo[ii].equals(aaTtCardNo[ii])
                && aaEmbossKind[ii].equals(aaTtEmbossKind[ii]))
              continue;
          }

          if (empty(aaRowid[ii])) {
            String isSql =
                "insert into tsc_dcbq_log " + "(tsc_card_no, card_no, emboss_kind, create_date, "
                    + "mod_user, mod_time, mod_pgm, mod_seqno) " + "values ("
                    + ":tsc_card_no, :card_no, :emboss_kind, :create_date, "
                    + ":mod_user, sysdate, 'tscm1040', 1)";
            setString("tsc_card_no", aaTscCardNo[ii]);
            setString("card_no", aaCardNo[ii]);
            setString("emboss_kind", aaEmbossKind[ii]);
            setString("create_date", aaCreateDate);
            setString("mod_user", wp.loginUser);
            sqlExec(isSql);
            if (sqlRowNum <= 0) {
              llErr++;
              wp.colSet(ii, "ok_flag", "X");
              wp.colSet(ii, "err_msg", "資料新增失敗");
            } else {
              llOk++;
              wp.colSet(ii, "ok_flag", "V");
            }
          } else {
            String usSql = "update tsc_dcbq_log set " + "tsc_card_no =:tsc_card_no , "
                + "card_no =:card_no, " + "emboss_kind =:emboss_kind, "
                + "mod_user =:mod_user, " + "mod_time = sysdate, "
                + "mod_pgm = 'tscm1040', " + "mod_seqno =nvl(mod_seqno,0)+1 where 1=1 and hex(rowid) = :rowid ";
            setString("tsc_card_no", aaTscCardNo[ii]);
            setString("card_no", aaCardNo[ii]);
            setString("emboss_kind", aaEmbossKind[ii]);
            setString("mod_user", wp.loginUser);
            setString("rowid", mRowid);
            sqlExec(usSql);
            if (sqlRowNum <= 0) {
              llErr++;
              wp.colSet(ii, "ok_flag", "X");
              wp.colSet(ii, "err_msg", "資料更新失敗");
            } else {
              llOk++;
              wp.colSet(ii, "ok_flag", "V");
            }
          }
        }
      }
    }
    sqlCommit(1);
    alertMsg("資料處理完成, 成功 = " + llOk + "筆 ,失敗 = " + llErr + "筆");
  }

  @Override
  public void initButton() {
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void dddwSelect() {
    try {

    } catch (Exception ex) {
    }
  }

  public int deleteTscBtrqLog(String rowid, String modSeqno) throws Exception {
    String dsSql = "delete tsc_dcbq_log where 1=1 ";
    dsSql += sqlCol(rowid, "hex(rowid)");
    sqlExec(dsSql);
    if (sqlRowNum <= 0) {
      return -1;
    } else {
      return 1;
    }
  }

  void itemChange() {
    String[] aaTscCardNo = wp.itemBuff("tsc_card_no");
    wp.listCount[0] = aaTscCardNo.length;
    String tscCardNo = itemKk("data_k2");
    String lsSql = "", lsNewEndDate = "", lsSysDate = "";
    int ii = Integer.parseInt(itemKk("data_k1"));
    
    if(empty(tscCardNo)) {
    	wp.colSet(ii, "card_no", "");
    	wp.colSet(ii, "err_msg", "悠遊卡號必需輸入 !!");
    	return;
    }
    
    lsSql = "select vd_card_no, " + "new_end_date, " + "current_code, " + "tsc_sign_flag "
        + "from tsc_vd_card where 1=1 ";
    lsSql += sqlCol(tscCardNo, "tsc_card_no");
    sqlSelect(lsSql);

    if (sqlRowNum > 0) {
      lsNewEndDate = sqlStr("new_end_date");
      lsSysDate = getSysDate();
      wp.colSet(ii, "card_no", sqlStr("vd_card_no"));
      wp.colSet(ii, "create_date", getSysDate());
      if (sqlStr("current_code").equals("2") && sqlStr("tsc_sign_flag").equals("Y")) {
        wp.colSet(ii, "card_no", "已掛失且為記名卡!!");
      }
      if (lsNewEndDate.compareTo(lsSysDate) < 0) {
        wp.colSet(ii, "card_no", "此卡號已到期 " + sqlStr("new_end_date"));
      }
    } else {
    	wp.colSet(ii, "card_no", "Tsc卡號無效!!");
    }
  }
}

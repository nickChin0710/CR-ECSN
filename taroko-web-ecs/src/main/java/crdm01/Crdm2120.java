/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-01  V1.00.00  yash       program initial                            *
* 108-12-17  V1.00.01  ryan		  update : ptr_group_card==>crd_item_unit    *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 110-01-05  V1.00.03  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *
******************************************************************************/

package crdm01;

import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Crdm2120 extends BaseProc {

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

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
      // insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      // updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 存檔 */
      strAction = "S2";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_id_code", "0");
  }


  // for query use only
  private boolean getWhereStr() throws Exception {

    wp.whereStr =
        " where ((c.current_code='0' and decode(c.change_status,'','3',c.change_status) in ('3','4')) or c.change_status='1' ) ";

    if (empty(wp.itemStr("ex_cardno")) == false) {
      wp.whereStr += " and  c.pp_card_no = :ex_cardno ";
      setString("ex_cardno", wp.itemStr("ex_cardno"));
    }


    if (empty(wp.itemStr("ex_id")) == false) {
      wp.whereStr += " and  b.id_no = :ex_id ";
      setString("ex_id", wp.itemStr("ex_id"));


      wp.whereStr += " and  b.id_no_code = :ex_id_code ";
      setString("ex_id_code", wp.itemStr("ex_id_code"));

    }



    return true;
  }

  @Override
  public void queryFunc() throws Exception {

    if (empty(wp.itemStr("ex_cardno")) && empty(wp.itemStr("ex_id"))) {
      alertMsg("卡號或身分字號，不可空白!!");
      return;
    }

    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " c.pp_card_no  " + ",c.id_p_seqno " + ",b.id_no " + ",b.id_no_code "
        + ",b.chi_name " + ",c.card_type " + ",c.group_code " + ",c.unit_code "
        + ",c.change_status " + ",c.expire_chg_flag " + ",'' as db_optcode " + ",c.expire_chg_date "
        + ",'' as cur_end_date " + ",'' as cur_beg_date " + ",'' as db_change_reason "
        + ",lpad(' ',6,' ') as db_appr" + ",lpad(' ',8,' ') as db_appr_date"
        + ",lpad(' ',20,' ') as db_old_process" + ",lpad(' ',20,' ') as db_expire_reason"
        + ",c.change_date " + ",c.change_reason" + ",c.reissue_reason " + ",c.reissue_status "
        + ",c.valid_to ";

    wp.daoTable = "crd_card_pp c left join crd_idno as b on c.id_p_seqno = b.id_p_seqno";
    wp.whereOrder = " order by c.card_type,c.pp_card_no";
    getWhereStr();

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

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }


  @Override
  public void dataProcess() throws Exception {

    String[] aaDbOptcode = wp.itemBuff("db_optcode");// 處理
    String[] aaDbChangeReason = wp.itemBuff("db_change_reason");// 續卡註記
    String[] aaPpCardNo = wp.itemBuff("pp_card_no");
    String[] aaExpireChgFlag = wp.itemBuff("expire_chg_flag");
    String[] aaReissueStatus = wp.itemBuff("reissue_status");
    String[] aaChangeStatus = wp.itemBuff("change_status");
    String[] aaIdPSeqno = wp.itemBuff("id_p_seqno");
    String[] aaChangeReason = wp.itemBuff("change_reason");
    String[] aaValidTo = wp.itemBuff("valid_to");
    String[] aaChangeDate = wp.itemBuff("change_date");
    String[] aaAprr = wp.itemBuff("db_appr");
    String[] aaCurBegDate = wp.itemBuff("cur_beg_date");
    String[] aaCurEndDate = wp.itemBuff("cur_end_date");


    wp.listCount[0] = aaPpCardNo.length;

    // save
    int llOk = 0, llErr = 0;

    for (int ii = 0; ii < aaPpCardNo.length; ii++) {


      if (empty(aaDbOptcode[ii])) {
        continue;
      }


      if (aaDbOptcode[ii].equals("1") && empty(aaDbChangeReason[ii])) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "ls_errmsg", "請輸入續卡註記  !!");
        llErr++;
        continue;
      }

      if (aaDbOptcode[ii].equals("0") && !aaAprr[ii].equals("N")) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "ls_errmsg", "請執行[取消線上續卡(放行後)] !!");
        llErr++;
        continue;
      }

      if (aaDbOptcode[ii].equals("2") && !aaAprr[ii].equals("Y") && aaAprr[ii].length() != 0) {
        wp.colSet(ii, "ok_flag", "X");
        wp.colSet(ii, "ls_errmsg", "請執行[取消線上續卡(放行前)] !!");
        llErr++;
        continue;
      }


      // wf_chk_chang_status
      if (aaDbOptcode[ii].equals("1")) {
        if (!empty(aaExpireChgFlag[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "已在不續卡狀態下,不可提前續卡  !!");
          llErr++;
          continue;
        }

        if (aaReissueStatus[ii].equals("1") || aaReissueStatus[ii].equals("2")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "已在重製卡狀態下,不可提前續卡  !!");
          llErr++;
          continue;
        }

        if (empty(aaDbChangeReason[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "需輸入續卡註記欄位值 !!");
          llErr++;
          continue;
        }

      }

      if (aaDbOptcode[ii].equals("2")) {
        if (aaChangeStatus[ii].equals("2")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "已送製卡,不可取消提前續卡");
          llErr++;
          continue;
        }
      }

      if (aaDbOptcode[ii].equals("3")) {
        if (empty(aaChangeStatus[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "此卡片不在續卡狀態下,不可做系統續卡改系統不續卡");
          llErr++;
          continue;
        }

        if (aaChangeStatus[ii].equals("2")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "此卡片已在續卡待製卡中");
          llErr++;
          continue;
        }

        if (!aaChangeStatus[ii].equals("1")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "只能系統續卡改系統不續卡");
          llErr++;
          continue;
        }

      }



      if (aaDbOptcode[ii].equals("0")) {
        // wf_delete_crd_card_pp_tmp
        String delSq =
            " delete crd_card_pp_tmp   " + " where  pp_card_no=:ls_card_no  and  kind_type='120' ";
        setString("ls_card_no", aaPpCardNo[ii]);
        sqlExec(delSq);
        if (sqlRowNum <= 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "wf_delete_crd_card_pp_tmp err");
          llErr++;
          sqlCommit(0);
          continue;
        } else {
          wp.colSet(ii, "ok_flag", "V");
          llOk++;
          sqlCommit(1);
          continue;
        }

      }


      if (aaDbOptcode[ii].equals("1")) {

        String delSq =
            " delete crd_card_pp_tmp   " + " where  pp_card_no=:ls_card_no  and  kind_type='120' ";
        setString("ls_card_no", aaPpCardNo[ii]);
        sqlExec(delSq);

        // wf_move_emboss_tmp
        if (aaChangeStatus[ii].equals("1")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "續卡待製卡中,不可再登續卡!");
          llErr++;
          sqlCommit(0);
          continue;
        }

        if (aaChangeStatus[ii].equals("2")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "已送製卡中,不可再做續卡 1 ");
          llErr++;
          sqlCommit(0);
          continue;
        }

        String lsSql =
            "select count(*) as li_cnt from crd_emboss_pp_tmp  where pp_card_no =:pp_card_no ";
        setString("pp_card_no", aaPpCardNo[ii]);
        sqlSelect(lsSql);
        int liCnt = (int) sqlNum("li_cnt");
        if (liCnt > 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "已送製卡中,不可再做續卡 2");
          llErr++;
          sqlCommit(0);
          continue;
        }


        taroko.base.CommDate commDate = new taroko.base.CommDate();
        int lsChk = Integer.parseInt(commDate.dateAdd(aaValidTo[ii], 0, -6, 0));
        int lsCreateDate = Integer.parseInt(getSysDate());
        if (lsChk > lsCreateDate) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "效期需在系統日六個月內");
          llErr++;
          sqlCommit(0);
          continue;
        }

        // wf_insert_crd_card_pp_tmp
        String lsSql2 =
            "select count(*) as li_cnt2 from crd_card_pp_tmp  where pp_card_no =:pp_card_no ";
        setString("pp_card_no", aaPpCardNo[ii]);
        sqlSelect(lsSql2);
        int liCnt2 = (int) sqlNum("li_cnt2");
        if (liCnt2 > 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "已經處理提前續卡過,請先查詢資料(crd_card_pp_tmp) !!");
          llErr++;
          sqlCommit(0);
          continue;
        }

        String lsChangeReason = "";
        String lsChangeStatus = "";


        if (aaDbOptcode.equals("0") || aaDbOptcode.equals("2")) {
          lsChangeReason = "";
          lsChangeStatus = "";
        } else {
          lsChangeReason = aaDbChangeReason[ii];
          lsChangeStatus = aaChangeStatus[ii];
        }


        busi.SqlPrepare spi = new SqlPrepare();
        spi.sql2Insert("crd_card_pp_tmp");
        spi.ppstr("pp_card_no", aaPpCardNo[ii]);
        spi.ppstr("kind_type", "120");
        spi.ppstr("id_p_seqno", aaIdPSeqno[ii]);
        spi.ppstr("process_kind", aaDbOptcode[ii]);
        spi.ppstr("change_reason", lsChangeReason);
        spi.ppstr("change_status", lsChangeStatus);
        spi.ppstr("change_date", getSysDate());
        spi.ppstr("change_reason_old", aaChangeReason[ii]);
        spi.ppstr("change_status_old", aaChangeStatus[ii]);
        spi.ppstr("change_date_old", aaChangeDate[ii]);
        spi.ppstr("cur_end_date", aaCurEndDate[ii]);
        spi.ppstr("old_end_date", aaValidTo[ii]);
        spi.ppstr("cur_beg_date", aaCurBegDate[ii]);
        spi.ppstr("crt_user", wp.loginUser);
        spi.ppstr("crt_date", getSysDate());
        spi.ppstr("mod_user", wp.loginUser);
        spi.addsql(", mod_time ", ", sysdate ");
        spi.ppnum("mod_pgm", 1);
        sqlExec(spi.sqlStmt(), spi.sqlParm());
        if (sqlRowNum <= 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "寫入卡片暫存檔錯誤~");
          llErr++;
          sqlCommit(0);
          continue;
        } else {
          wp.colSet(ii, "ok_flag", "V");
          llOk++;
          sqlCommit(1);
          continue;
        }
      }

      if (aaDbOptcode[ii].equals("2")) {

        String delSq =
            " delete crd_card_pp_tmp   " + " where  pp_card_no=:ls_card_no  and  kind_type='120' ";
        setString("ls_card_no", aaPpCardNo[ii]);
        sqlExec(delSq);

        // wf_cancel_chg
        if (empty(aaChangeStatus[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "該卡為不續卡狀態下,不需取消續卡~");
          llErr++;
          sqlCommit(0);
          continue;
        }

        if (aaChangeStatus[ii].equals("2")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "此卡片已送製卡,不可取消續卡~");
          llErr++;
          sqlCommit(0);
          continue;
        }

        // wf_insert_crd_card_pp_tmp
        String lsSql2 =
            "select count(*) as li_cnt2 from crd_card_pp_tmp  where pp_card_no =:pp_card_no ";
        setString("pp_card_no", aaPpCardNo[ii]);
        sqlSelect(lsSql2);
        int liCnt2 = (int) sqlNum("li_cnt2");
        if (liCnt2 > 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "已經處理提前續卡過,請先查詢資料(crd_card_pp_tmp) !!");
          llErr++;
          sqlCommit(0);
          continue;
        }

        String lsChangeReason = "";
        String lsChangeStatus = "";


        if (aaDbOptcode.equals("0") || aaDbOptcode.equals("2")) {
          lsChangeReason = "";
          lsChangeStatus = "";
        } else {
          lsChangeReason = aaDbChangeReason[ii];
          lsChangeStatus = aaChangeStatus[ii];
        }


        busi.SqlPrepare spi = new SqlPrepare();
        spi.sql2Insert("crd_card_pp_tmp");
        spi.ppstr("pp_card_no", aaPpCardNo[ii]);
        spi.ppstr("kind_type", "120");
        spi.ppstr("id_p_seqno", aaIdPSeqno[ii]);
        spi.ppstr("process_kind", aaDbOptcode[ii]);
        spi.ppstr("change_reason", lsChangeReason);
        spi.ppstr("change_status", lsChangeStatus);
        spi.ppstr("change_date", getSysDate());
        spi.ppstr("change_reason_old", aaChangeReason[ii]);
        spi.ppstr("change_status_old", aaChangeStatus[ii]);
        spi.ppstr("change_date_old", aaChangeDate[ii]);
        spi.ppstr("cur_end_date", aaCurEndDate[ii]);
        spi.ppstr("old_end_date", aaValidTo[ii]);
        spi.ppstr("cur_beg_date", aaCurBegDate[ii]);
        spi.ppstr("crt_user", wp.loginUser);
        spi.ppstr("crt_date", getSysDate());
        spi.ppstr("mod_user", wp.loginUser);
        spi.addsql(", mod_time ", ", sysdate ");
        spi.ppnum("mod_pgm", 1);
        sqlExec(spi.sqlStmt(), spi.sqlParm());
        if (sqlRowNum <= 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "寫入卡片暫存檔錯誤~");
          llErr++;
          sqlCommit(0);
          continue;
        } else {
          wp.colSet(ii, "ok_flag", "V");
          llOk++;
          sqlCommit(1);
          continue;
        }

      }

      if (aaDbOptcode[ii].equals("3")) {

        String delSq =
            " delete crd_card_pp_tmp   " + " where  pp_card_no=:ls_card_no  and  kind_type='120' ";
        setString("ls_card_no", aaPpCardNo[ii]);
        sqlExec(delSq);

        // wf_process_chg
        if (!empty(aaExpireChgFlag[ii])) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "不可重複做預約不續卡");
          llErr++;
          sqlCommit(0);
          continue;
        }
        if (aaChangeStatus[ii].equals("2")) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "已在續卡製作中,不可改為不續卡~");
          llErr++;
          sqlCommit(0);
          continue;
        }

        // wf_insert_crd_card_pp_tmp
        String lsSql2 =
            "select count(*) as li_cnt2 from crd_card_pp_tmp  where pp_card_no =:pp_card_no ";
        setString("pp_card_no", aaPpCardNo[ii]);
        sqlSelect(lsSql2);
        int liCnt2 = (int) sqlNum("li_cnt2");
        if (liCnt2 > 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "已經處理提前續卡過,請先查詢資料(crd_card_pp_tmp) !!");
          llErr++;
          sqlCommit(0);
          continue;
        }

        String lsChangeReason = "";
        String lsChangeStatus = "";


        if (aaDbOptcode.equals("0") || aaDbOptcode.equals("2")) {
          lsChangeReason = "";
          lsChangeStatus = "";
        } else {
          lsChangeReason = aaDbChangeReason[ii];
          lsChangeStatus = aaChangeStatus[ii];
        }


        busi.SqlPrepare spi = new SqlPrepare();
        spi.sql2Insert("crd_card_pp_tmp");
        spi.ppstr("pp_card_no", aaPpCardNo[ii]);
        spi.ppstr("kind_type", "120");
        spi.ppstr("id_p_seqno", aaIdPSeqno[ii]);
        spi.ppstr("process_kind", aaDbOptcode[ii]);
        spi.ppstr("change_reason", lsChangeReason);
        spi.ppstr("change_status", lsChangeStatus);
        spi.ppstr("change_date", getSysDate());
        spi.ppstr("change_reason_old", aaChangeReason[ii]);
        spi.ppstr("change_status_old", aaChangeStatus[ii]);
        spi.ppstr("change_date_old", aaChangeDate[ii]);
        spi.ppstr("cur_end_date", aaCurEndDate[ii]);
        spi.ppstr("old_end_date", aaValidTo[ii]);
        spi.ppstr("cur_beg_date", aaCurBegDate[ii]);
        spi.ppstr("crt_user", wp.loginUser);
        spi.ppstr("crt_date", getSysDate());
        spi.ppstr("mod_user", wp.loginUser);
        spi.addsql(", mod_time ", ", sysdate ");
        spi.ppnum("mod_pgm", 1);
        sqlExec(spi.sqlStmt(), spi.sqlParm());
        if (sqlRowNum <= 0) {
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "ls_errmsg", "寫入卡片暫存檔錯誤~");
          llErr++;
          sqlCommit(0);
          continue;
        } else {
          wp.colSet(ii, "ok_flag", "V");
          llOk++;
          sqlCommit(1);
          continue;
        }

      }

    }

    alertMsg("處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {

      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("ex_merchant");
      // this.dddw_list("dddw_merchant", "bil_merchant", "mcht_no", "mcht_chi_name", "where 1=1 and
      // mcht_status = '1' group by mcht_no,mcht_chi_name order by mcht_no");

    } catch (Exception ex) {
    }
  }


  void listWkdata() throws Exception {
    wp.logSql = false;

    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {

      String liExtn = "", lsEndVal = "", lsBegVal = "", isChangeReason = "", isExpireChgFlag = "";

      // wf_get_extn_year
      String lsSql =
          "select extn_year from crd_item_unit  where unit_code =:as_unit_code  and card_type =:as_card_type ";
      setString("as_unit_code",
          empty(wp.colStr(ii, "unit_code")) ? "0000" : wp.colStr(ii, "unit_code"));
      setString("as_card_type", wp.colStr(ii, "card_type"));
      sqlSelect(lsSql);
      if (sqlRowNum <= 0) {
        liExtn = "2";
      } else {
        liExtn = sqlStr("extn_year");
      }


      lsEndVal = wp.colStr(ii, "valid_to");
      lsBegVal = getSysDate().substring(0, 6) + "01";

      taroko.base.CommDate commDate = new taroko.base.CommDate();
      lsEndVal = commDate.dateAdd(lsEndVal, Integer.parseInt(liExtn), 0, 0);
      wp.colSet(ii, "cur_beg_date", lsBegVal);
      wp.colSet(ii, "cur_end_date", lsEndVal);

      if (wp.colNum(ii, "change_status") >= 3) {
        wp.colSet(ii, "db_change_reason", "");
      } else {
        wp.colSet(ii, "db_change_reason", wp.colStr(ii, "change_reason"));
      }

      isChangeReason = wp.colStr(ii, "change_reason");
      isExpireChgFlag = wp.colStr(ii, "expire_chg_flag");


      String lsProcessKind = "", lsChangeReason = "", lsChangeStatus = "";
      String lsChangeDate = "", lsExpireChgFlag = "", lsExpireReason = "", lsAprDate = "";


      String lsSql2 = "select process_kind," + "        change_reason,  "
          + "        change_status,  " + "        change_date,  " + "        expire_chg_flag,  "
          + "        expire_reason,  " + "        apr_date  " + " from crd_card_pp_tmp "
          + " where pp_card_no =:pp_card_no  and  kind_type  = '120' ";
      setString("pp_card_no", wp.colStr(ii, "pp_card_no"));
      sqlSelect(lsSql2);

      lsProcessKind = sqlStr("process_kind");
      lsChangeReason = sqlStr("change_reason");
      lsChangeStatus = sqlStr("change_status");
      lsChangeDate = sqlStr("change_date");
      lsExpireChgFlag = sqlStr("expire_chg_flag");
      lsExpireReason = sqlStr("expire_reason");
      lsAprDate = sqlStr("apr_date");

      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_old_process", lsProcessKind);
        wp.colSet(ii, "change_date", lsChangeDate);
        wp.colSet(ii, "db_change_reason", lsChangeReason);
        wp.colSet(ii, "change_status", lsChangeStatus);
        wp.colSet(ii, "db_appr_date", lsAprDate);
        wp.colSet(ii, "db_appr", "N");
        wp.colSet(ii, "expire_chg_flag", lsExpireChgFlag);

        if (lsExpireChgFlag.equals("1")) {
          String lsSql3 = "select wf_desc " + " from ptr_sys_idtab "
              + " where wf_type='NOTCHG_KIND_O' and wf_id=:wf_id ";
          setString("wf_id", lsExpireReason);
          sqlSelect(lsSql3);
          wp.colSet(ii, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
        }

        if (lsExpireChgFlag.equals("4")) {
          String lsSql4 = "select wf_desc " + " from ptr_sys_idtab "
              + " where wf_type='NOTCHG_KIND_M' and wf_id=:wf_id ";
          setString("wf_id", lsExpireReason);
          sqlSelect(lsSql4);
          wp.colSet(ii, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
        }


        if (lsExpireChgFlag.equals("5")) {
          String lsSql4 = "select wf_desc " + " from ptr_sys_idtab "
              + " where wf_type='NOTCHG_KIND_S_P' and wf_id=:wf_id ";
          setString("wf_id", lsExpireReason);
          sqlSelect(lsSql4);
          wp.colSet(ii, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
        }


      } else {

        String lsSql5 = "select " + "        change_reason,  " + "        expire_chg_flag,  "
            + "        expire_reason,  " + "        change_status,  " + "        change_date  "
            + " from crd_card_pp " + " where pp_card_no =:pp_card_no  ";
        setString("pp_card_no", wp.colStr(ii, "pp_card_no"));
        sqlSelect(lsSql5);

        lsExpireChgFlag = sqlStr("expire_chg_flag");
        lsChangeReason = sqlStr("change_reason");
        lsExpireReason = sqlStr("expire_reason");
        lsChangeStatus = sqlStr("change_status");
        lsChangeDate = sqlStr("change_date");


        wp.colSet(ii, "change_date", lsChangeDate);
        wp.colSet(ii, "db_change_reason", lsChangeReason);
        wp.colSet(ii, "change_status", lsChangeStatus);
        wp.colSet(ii, "db_appr", "");
        wp.colSet(ii, "expire_chg_flag", lsExpireChgFlag);
        wp.colSet(ii, "change_date", sqlStr("change_date"));

        if (lsExpireChgFlag.equals("1")) {
          String lsSql3 = "select wf_desc " + " from ptr_sys_idtab "
              + " where wf_type='NOTCHG_KIND_O' and wf_id=:wf_id ";
          setString("wf_id", lsExpireReason);
          sqlSelect(lsSql3);
          wp.colSet(ii, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
        }

        if (lsExpireChgFlag.equals("4")) {
          String lsSql4 = "select wf_desc " + " from ptr_sys_idtab "
              + " where wf_type='NOTCHG_KIND_M' and wf_id=:wf_id ";
          setString("wf_id", lsExpireReason);
          sqlSelect(lsSql4);
          wp.colSet(ii, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
        }


        if (lsExpireChgFlag.equals("5")) {
          String lsSql4 = "select wf_desc " + " from ptr_sys_idtab "
              + " where wf_type='NOTCHG_KIND_S_P' and wf_id=:wf_id ";
          setString("wf_id", lsExpireReason);
          sqlSelect(lsSql4);
          wp.colSet(ii, "db_expire_reason", lsExpireReason + "-" + sqlStr("wf_desc"));
        }


      }

    }



  }


}

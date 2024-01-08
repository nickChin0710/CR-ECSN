/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-07  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>commString                     *
* 108-12-17  V1.00.02  ryan		  update : ptr_group_card==>crd_item_unit    *
* 109-04-28  V1.00.03  YangFang   updated for project coding standard        *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package crdm01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Crdm0080 extends BaseProc {
  Crdm0080Func func;
  CommString commString = new CommString();
  int rr = -1, liExtn = 0;
  String msg = "";
  //String kk1 = "";
  int ilOk = 0;
  int ilErr = 0, ilErr2 = 0;
  int selectCnt = 0;
  String isExpireChgFlag = "", isChangeReason = "";

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

    for (int i = 0; i < wp.selectCnt; i++) {
      try {
        wp.initOption = "--";
        // wp.optionKey = wp.item_ss(i,"db_optcode1");
        this.dddwList(i, "dddw_db_optcode1", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1 and wf_type='NOTCHG_KIND_O' order by wf_id");
        // wp.optionKey = wp.item_ss(i,"db_optcode2");
        this.dddwList(i, "dddw_db_optcode2", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1 and wf_type='NOTCHG_KIND_M' order by wf_id");
      } catch (Exception ex) {
      }
    }
  }

  @Override
  public void queryFunc() throws Exception {
    String idPSeqno = "";
    if (empty(wp.itemStr("ex_id")) == false) {
      String sqlSelect =
          "select id_p_seqno from  crd_idno where 1 = 1 AND id_no = :ex_id and id_no_code = :ex_id_code ";
      setString("ex_id", wp.itemStr("ex_id"));
      setString("ex_id_code", wp.itemStr("ex_id_code"));
      sqlSelect(sqlSelect);
      idPSeqno = sqlStr("id_p_seqno");
    }
    wp.whereStr = "where 1=1 ";

    if (empty(wp.itemStr("ex_cardno")) && empty(wp.itemStr("ex_id"))
        && empty(wp.itemStr("ex_corp_no"))) {
      alertMsg("請輸入查詢之鍵值");
      return;
    }
    if (empty(wp.itemStr("ex_cardno")) == false || empty(wp.itemStr("ex_id")) == false) {
      wp.whereStr +=
          "and a.major_id_p_seqno in (select major_id_p_seqno from crd_card as c where 1=1 ";
      if (empty(wp.itemStr("ex_cardno")) == false) {
        wp.whereStr += " and c.card_no = :ex_cardno ";
        setString("ex_cardno", wp.itemStr("ex_cardno"));
        wp.whereStr += " and a.card_type = c.card_type and a.group_code = c.group_code ";
      }
      if (empty(wp.itemStr("ex_id")) == false) {
        wp.whereStr += " and c.id_p_seqno = :id_p_seqno ";
        setString("id_p_seqno", idPSeqno);
      }
      wp.whereStr += "  and c.current_code = '0' ) and a.current_code = '0' ";
    }
    if (empty(wp.itemStr("ex_corp_no")) == false) {
      wp.whereStr += " and a.corp_no = :ex_corp_no ";
      setString("ex_corp_no", wp.itemStr("ex_corp_no"));
      wp.whereStr += " and a.corp_no_code = :ex_corp_no_code ";
      setString("ex_corp_no_code", wp.itemStr("ex_corp_no_code"));
      wp.whereStr += " and a.current_code = '0' ";
    }

    // wp.whereStr = "where 1=1 ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(a.rowid) as rowid, " + "a.card_no, " + "b.id_no, " + "b.id_no_code, "
        + "b.id_no||'-'||b.id_no_code as wk_id, " + "b.chi_name, " + "a.corp_no, "
        + "a.corp_no_code, " + "a.corp_no||'-'||a.corp_no_code as wk_corp_no ," + "a.card_type, "
        + "a.group_code, " + "a.new_end_date, " + "a.change_status, " + "a.expire_chg_flag, "
        + "a.sup_flag, " + "a.major_card_no, " + "a.expire_chg_date, " + "a.id_p_seqno, "
        + "a.acno_p_seqno, " + "a.corp_p_seqno, " + "a.acct_type, "
        + "UF_ACNO_KEY(a.acno_p_seqno) as acct_key, " + "a.urgent_flag, " + "a.source_code, "
        + "a.son_card_flag, " + "a.major_relation, "
        + "UF_IDNO_ID(a.major_id_p_seqno) as  major_id, " + "a.major_id_p_seqno, "
        + "a.member_note, " + "a.member_id, " + "a.current_code, " + "a.force_flag, "
        + "b.eng_name, " + "a.reg_bank_no, " + "a.unit_code, " + "a.old_beg_date, "
        + "a.old_end_date, " + "a.new_beg_date, " + "a.issue_date, " + "a.emergent_flag, "
        + "a.reissue_date, " + "a.reissue_reason, " + "a.reissue_status, " + "a.change_reason, "
        + "a.change_date, " + "a.upgrade_status, " + "a.upgrade_date, " + "a.apply_no, "
        + "a.promote_dept, " + "a.promote_emp_no, " + "a.introduce_emp_no, " + "a.introduce_id, "
        + "a.introduce_name, " + "a.prod_no, " + "a.reward_amt, " + "a.intr_reason_code, "
        + "a.pvv, " + "a.cvv, " + "a.cvv2, " + "a.pvki, " + "a.batchno, " + "a.recno, "
        + "a.expire_reason, " + "a.emboss_data, " + "a.combo_indicator, " + "a.ic_flag ";

    wp.daoTable = "crd_card as a left join crd_idno as b on a.id_p_seqno = b.id_p_seqno";
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
    selectCnt = wp.selectCnt;
    String wkData = "", lsCardno = "", lsProcessKind = "", lsExpireReason = "", lsExpireChgDate = "",
        lsExpireChgFlag = "", lsApprDate = "";
    for (int i = 0; i < wp.selectCnt; i++) {

      wkData = wp.colStr(i, "change_reason");
      wp.colSet(i, "tt_change_reason", commString.decode(wkData, ",1,2,3", ",系統續卡,提前續卡,人工續卡"));

      wkData = wp.colStr(i, "change_status");
      wp.colSet(i, "tt_change_status", commString.decode(wkData, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));

      wkData = wp.colStr(i, "sup_flag");
      wp.colSet(i, "tt_sup_flag", commString.decode(wkData, ",0,1", ",正卡,附卡"));

      lsCardno = wp.colStr(i, "card_no");

      String sql1 = " select process_kind " + " ,expire_chg_flag " + " ,apr_date "
          + " ,expire_reason " + " ,expire_chg_date "
          + " from crd_card_tmp where card_no = :ls_cardno and kind_type = '080' ";
      setString("ls_cardno", lsCardno);
      sqlSelect(sql1);
      lsProcessKind = sqlStr("process_kind");
      lsExpireReason = sqlStr("expire_reason");
      lsExpireChgDate = sqlStr("expire_chg_date");
      lsExpireChgFlag = sqlStr("expire_chg_flag");
      lsApprDate = sqlStr("apr_date");
      if (sqlRowNum > 0) {
        wp.colSet(i, "db_old_process", lsProcessKind);
        wp.colSet(i, "tt_db_old_process", commString.decode(lsProcessKind, ",0,1,4,2,3",
            ",取消不續卡(放行前),預約不續卡,人工不續卡,取消不續卡(放行後),系統不續卡改續卡"));
        wp.colSet(i, "expire_reason", lsExpireReason);
        wp.colSet(i, "expire_chg_date", lsExpireChgDate);
        wp.colSet(i, "expire_chg_flag", lsExpireChgFlag);
        if (lsExpireChgFlag.equals("1")) {
          String sql2 =
              "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_O' and wf_id = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(sql2);
          String lsExpireDesc = sqlStr("wf_desc");
          wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
        }
        if (lsExpireChgFlag.equals("4")) {
          String sql2 =
              "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_M' and wf_id = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(sql2);
          String lsExpireDesc = sqlStr("wf_desc");
          wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
        }
        wp.colSet(i, "db_appr", "N");
        wp.colSet(i, "db_appr_date", lsApprDate);
        wp.colSet(i, "db_expire_chg", lsExpireChgFlag);
        isExpireChgFlag = wp.colStr(i, "expire_chg_flag");
        isChangeReason = wp.colStr(i, "expire_reason");
      } else {
        String sql3 = " select process_kind " + " ,expire_chg_flag " + " ,apr_date "
            + " ,expire_reason " + " ,expire_chg_date "
            + " from crd_card_tmp_h where card_no = :ls_cardno and kind_type = '080' "
            + " and apr_date in ( "
            + " select max(apr_date) from crd_card_tmp_h where card_no = :ls_cardno2 and kind_type = '080') ";
        setString("ls_cardno", lsCardno);
        setString("ls_cardno2", lsCardno);
        sqlSelect(sql3);
        lsProcessKind = sqlStr("process_kind");
        lsExpireReason = sqlStr("expire_reason");
        lsExpireChgDate = sqlStr("expire_chg_date");
        lsExpireChgFlag = sqlStr("expire_chg_flag");

        lsApprDate = sqlStr("apr_date");
        if (sqlRowNum > 0 && sqlRowNum < 2) {
          wp.colSet(i, "db_appr_date", lsApprDate);
          wp.colSet(i, "db_appr", "Y");
          String sql4 = " select expire_reason, "
              + " decode(expire_chg_flag,'1','5','2','1','3','4') as expire_chg_flag, "
              + " expire_chg_date " + " from crd_card " + " where card_no = :ls_cardno ";
          setString("ls_cardno", lsCardno);
          sqlSelect(sql4);
          lsExpireReason = sqlStr("expire_reason");
          lsExpireChgDate = sqlStr("expire_chg_date");
          lsExpireChgFlag = sqlStr("expire_chg_flag");
          if (sqlRowNum > 0) {
            wp.colSet(i, "expire_reason", lsExpireReason);
            wp.colSet(i, "expire_chg_date", lsExpireChgDate);
            wp.colSet(i, "expire_chg_flag", lsExpireChgFlag);
            if (lsExpireChgFlag.equals("1")) {
              String sql5 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_O' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sql5);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            if (lsExpireChgFlag.equals("4")) {
              String sql5 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_M' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sql5);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            wp.colSet(i, "db_expire_chg", lsExpireChgFlag);
            isExpireChgFlag = wp.colStr(i, "expire_chg_flag");
            isChangeReason = wp.colStr(i, "expire_reason");
          }
        } else {
          String sql6 = " select expire_reason, "
              + " decode(expire_chg_flag,'1','5','2','1','3','4') as expire_chg_flag, "
              + " expire_chg_date " + " from crd_card " + " where card_no = :ls_cardno ";
          setString("ls_cardno", lsCardno);
          sqlSelect(sql6);
          lsExpireReason = sqlStr("expire_reason");
          lsExpireChgDate = sqlStr("expire_chg_date");
          lsExpireChgFlag = sqlStr("expire_chg_flag");

          if (sqlRowNum > 0) {
            wp.colSet(i, "db_appr", "");
            wp.colSet(i, "db_appr_date", "");
            wp.colSet(i, "expire_reason", lsExpireReason);
            wp.colSet(i, "expire_chg_date", lsExpireChgDate);
            wp.colSet(i, "expire_chg_flag", lsExpireChgFlag);
            if (lsExpireChgFlag.equals("1")) {
              String sql7 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_O' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sql7);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            if (lsExpireChgFlag.equals("4")) {
              String sql7 =
                  "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_KIND_M' and wf_id = :ls_expire_reason ";
              setString("ls_expire_reason", lsExpireReason);
              sqlSelect(sql7);
              String lsExpireDesc = sqlStr("wf_desc");
              wp.colSet(i, "db_expire_reason", lsExpireReason + "-" + lsExpireDesc);
            }
            wp.colSet(i, "db_expire_chg", lsExpireChgFlag);
            isExpireChgFlag = wp.colStr(i, "expire_chg_flag");
            isChangeReason = wp.colStr(i, "expire_reason");
          }
        }
      }
      wkData = lsExpireChgFlag;
      wp.colSet(i, "tt_db_expire_chg",
          commString.decode(wkData, ",1,4,5,0,2,3", ",預約不續卡,人工不續卡,系統不續卡,取消不續卡(放行前),取消不續卡(放行後),系統不續卡改續卡"));

    }
  }

  int wfMoveEmbossTmp() throws Exception {
    String[] lsCardno;
    String[] lsIdPSeqno, lsAcnoPSeqno;
    String[] lsValidDate;
    String[] lsChangeStatus;
    String[] lsMajorCardno;
    String lsMajorCurrentCode = "", lsCreateDate = "", lsChk = "", lsBatchno1 = "", lsBatchno = "";
    double liRecno = 0;
    lsChangeStatus = wp.itemBuff("change_status");
    String[] aaSupFlag = wp.itemBuff("sup_flag");

    if (lsChangeStatus[rr].equals("1")) {
      alertErr("續卡製卡中，不可再做續卡");
      return -1;
    }
    if (lsChangeStatus[rr].equals("2")) {
      alertErr("此卡片送製卡中,不可再做提前續卡");
      return -1;
    }
    lsCardno = wp.itemBuff("card_no");
    String lsSql2 =
        " select count(*) as li_cnt  from  crd_emboss_tmp where  old_card_no = :ls_cardno ";
    setString("ls_cardno", lsCardno[rr]);
    sqlSelect(lsSql2);

    if (sqlNum("li_cnt") > 0) {
      alertErr("續卡製卡中，不可改為不續卡");
      return -1;
    }
    if (aaSupFlag[rr].equals("1")) {
      lsMajorCardno = wp.itemBuff("major_card_no");
      String lsSql3 =
          " select new_beg_date,new_end_date,current_code  from  crd_card where  card_no = :ls_major_cardno ";
      setString("ls_major_cardno", lsMajorCardno[rr]);
      sqlSelect(lsSql3);
      lsMajorCurrentCode = sqlStr("current_code");
      if (sqlRowNum <= 0) {
        alertErr("找取不到正卡資料");
        return -1;
      }
      if (!lsMajorCurrentCode.equals("0")) {
        alertErr("正卡不為正常卡,不可做線上續卡");
        return -1;
      }
    }
    // -- 改為抓自己之效期最展期 2002/01/24
    lsValidDate = wp.itemBuff("new_end_date");
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    lsCreateDate = getSysDate();
    Date date = format.parse(lsValidDate[rr]);
    cal.setTime(date);
    cal.add(Calendar.MARCH, -6);
    lsChk = format.format(cal.getTime());
    if (this.toInt(lsChk) > this.toInt(lsCreateDate)) {
      alertErr("效期需在系統日六個月內");
      return -1;
    }

    lsBatchno1 = strMid(lsCreateDate, 2, 6);

    String lsSql4 =
        " select max(batchno) as batchno from  crd_emboss_tmp where substr(batchno,1,6) = :ls_batchno1 ";
    setString("ls_batchno1", lsBatchno1);
    sqlSelect(lsSql4);
    lsBatchno = sqlStr("batchno");
    if (empty(lsBatchno)) {
      lsBatchno = lsBatchno1 + "01";
    } else {
      String lsSql5 =
          " select max(recno)+1 as recno from  crd_emboss_tmp where batchno = :ls_batchno ";
      setString("ls_batchno", lsBatchno);
      sqlSelect(lsSql5);
      liRecno = this.toNum(sqlStr("recno"));
      if (liRecno == 0) {
        liRecno = 1;
      }
    }
    lsIdPSeqno = wp.itemBuff("id_p_seqno");
    lsAcnoPSeqno = wp.itemBuff("acno_p_seqno");
    String lsSql5 = " select chi_name, birthday from  crd_idno where id_p_seqno = :ls_id_p_seqno ";
    setString("ls_id_p_seqno", lsIdPSeqno[rr]);
    sqlSelect(lsSql5);
    if (sqlRowNum <= 0) {
      alertErr("抓取卡人檔失敗");
      return -1;
    }
    String lsSql6 =
        " select line_of_credit_amt, chg_addr_date from  act_acno where acno_p_seqno = :ls_acno_p_seqno ";
    setString("ls_acno_p_seqno", lsAcnoPSeqno[rr]);
    sqlSelect(lsSql6);
    if (sqlRowNum <= 0) {
      return -1;
    }
    // -- 抓取展期年
    String[] aaUnitCode = wp.itemBuff("unit_code");
    String[] aaCardType = wp.itemBuff("card_type");
    liExtn = wfGetExtnYear(aaUnitCode[rr], aaCardType[rr]);
    // -- 由覆核處理資料
    if (wfInsertCrdCardTmp() != 1) {
      return -1;
    }
    return 1;
  }

  int wfGetAcnoData(int alRow) throws Exception {
    String[] lsAcnoPSeqno;
    lsAcnoPSeqno = wp.itemBuff("acno_p_seqno");
    String lsSql10 = " select risk_bank_no from act_acno where acno_p_seqno = :ls_acno_p_seqno ";
    setString("ls_acno_p_seqno", lsAcnoPSeqno[rr]);
    sqlSelect(lsSql10);
    if (sqlRowNum <= 0) {
      alertMsg("第" + (alRow + 1) + "筆資料: " + "無法抓取到此卡號帳戶資料");
      return -1;
    }
    return 1;
  }

  int wfCancelExpire() {
    String[] lsExpireChgFlag, lsChangeStatus;
    lsExpireChgFlag = wp.itemBuff("expire_chg_flag");
    lsChangeStatus = wp.itemBuff("change_status");

    if (lsChangeStatus[rr].equals("2")) {
      alertErr("此卡片已送製卡,不可作任何異動");
      return -1;
    }
    if (empty(lsExpireChgFlag[rr])) {
      // if (func.dbDelete() == -1) {
      alertErr("此筆資料本身並無不續卡註記");
      return -1;
      // }
    }
    // -- 由覆核處理資料 **********************************************************************
    if (wfInsertCrdCardTmp() != 1) {
      return -1;
    }
    return 1;
  }

  int wfGetExtnYear(String asUnitCode, String asCardType) throws Exception {
    int liYear = 0;
    if (empty(asUnitCode) == true) {
      asUnitCode = "0000";
    }
    String lsSql9 =
        " select extn_year from crd_item_unit where unit_code = :unit_code and card_type = :card_type";
    setString("unit_code", asUnitCode);
    setString("card_type", asCardType);
    sqlSelect(lsSql9);
    liYear = (int) this.toNum(sqlStr("extn_year"));
    if (sqlRowNum <= 0) {
      msg += "第" + (rr + 1) + "筆資料: " + "抓取不到展期年~" + "，";
      return -1;
    }

    return liYear;
  }

  int wfChkExpireChgFlag() {
    // -- is_expire_chg_flag 卡檔為抓出時值
    String[] aaOpt = wp.itemBuff("opt");
    String[] aaExpireChgFlag = wp.itemBuff("expire_chg_flag");
    String[] aaChangeStatus = wp.itemBuff("change_status");
    String[] aaReissueStatus = wp.itemBuff("reissue_status");

    if (aaOpt[rr].equals("1") || aaOpt[rr].equals("4")) {
      if (empty(aaExpireChgFlag[rr]) == false) {
        alertErr("此卡片已在不續卡狀態下");
        return -1;
      }
      /*
       * if (empty(aa_db_expire_chg[rr])) { msg += "第" + (rr + 1) + "筆資料: " + "請輸入預約不續卡註記" + "，";
       * return -1; }
       */
      if (aaChangeStatus[rr].equals("1") || aaChangeStatus[rr].equals("2")) {
        alertErr("此卡片在續卡狀態下,不可做預約不續卡");
        return -1;
      }
      if (aaReissueStatus[rr].equals("1") || aaReissueStatus[rr].equals("2")) {
        alertErr("此卡片在重製卡狀態下,不可做預約不續卡");
        return -1;
      }
    }
    if (aaOpt[rr].equals("2")) {
      if (empty(aaExpireChgFlag[rr])) {
        alertErr("此卡片未在不續卡狀態下,不可做取消預約不續卡");
        return -1;
      }
    }
    return 1;
  }

  int wfDelNotchg() {
    // --- combo卡在取消預約不續卡時需檢核是否已存在crd_notchg
    // --- 若存在則需刪除
    String[] lsCardNo = wp.itemBuff("card_no");
    func.varsSet("ls_card_no", lsCardNo[rr]);
    String sql1 = " select hex(rowid) as ls_rowid from crd_notchg where card_no = :ls_card_no ";
    setString("ls_card_no", lsCardNo[rr]);
    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      if (func.dbDelete() != 1) {
        alertErr("無法刪除combo卡不續卡資料");
        return -1;
      }
    }
    return 1;
  }

  int wfInsertCrdCardTmp() {
    String lsExpireReason = "", lsExpireChgFlag = "";
    String[] lsCardNo = wp.itemBuff("card_no");
    String[] lsProcessKind = wp.itemBuff("opt");
    String[] lsId = wp.itemBuff("id_no");
    String[] lsIdCode = wp.itemBuff("id_no_code");
    String[] lsCorpNo = wp.itemBuff("corp_no");
    String[] lsDbOptcode = wp.itemBuff("opt");
    String[] aaDbOptcode1 = wp.itemBuff("db_optcode1");
    String[] aaDbOptcode2 = wp.itemBuff("db_optcode2");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaDbRxpireChg = wp.itemBuff("db_expire_chg");

    String lsExpireChgDate = wp.sysDate;

    if (lsDbOptcode[rr].equals("0") || lsDbOptcode[rr].equals("2")) {
      lsExpireReason = "";
      lsExpireChgFlag = "";
    } else {
      if (lsDbOptcode[rr].equals("1")) {
        lsExpireReason = aaDbOptcode1[rr];
      }
      if (lsDbOptcode[rr].equals("4")) {
        lsExpireReason = aaDbOptcode2[rr];
      }
      lsExpireChgFlag = aaDbRxpireChg[rr];
    }

    String[] lsExpireChgDateOld = wp.itemBuff("expire_chg_date");
    String[] lsExpireReasonOld = wp.itemBuff("expire_reason");
    String[] lsExpireChgFlagOld = wp.itemBuff("expire_chg_flag");
    String[] lsOldEndDate = wp.itemBuff("new_end_date");
    String[] lsCurEndDate = wp.itemBuff("new_end_date");

    String sql1 =
        " select count(*) as li_cnt from crd_card_tmp where card_no  = :ls_card_no and kind_type = '080' ";
    setString("ls_card_no", lsCardNo[rr]);
    sqlSelect(sql1);
    if (this.toNum(sqlStr("li_cnt")) > 0) {
      alertErr("已經處理提前續卡過,請先查詢資料(crd_card_tmp) !!");
      return -1;
    }
    String sql2 =
        " select id_p_seqno from crd_idno where id_no = :ls_id and id_no_code = :ls_id_code ";
    setString("ls_id", lsId[rr]);
    setString("ls_id_code", lsIdCode[rr]);
    sqlSelect(sql2);

    func.varsSet("ls_id_p_seqno", sqlStr("id_p_seqno"));
    func.varsSet("ls_card_no", lsCardNo[rr]);
    func.varsSet("ls_corp_no", lsCorpNo[rr]);
    func.varsSet("ls_process_kind", lsProcessKind[rr]);
    func.varsSet("ls_expire_reason", lsExpireReason);
    func.varsSet("ls_expire_chg_flag", lsExpireChgFlag);
    func.varsSet("ls_expire_chg_date", lsExpireChgDate);
    func.varsSet("ls_expire_reason_old", lsExpireReasonOld[rr]);
    func.varsSet("ls_expire_chg_flag_old", lsExpireChgFlagOld[rr]);
    func.varsSet("ls_expire_chg_date_old", lsExpireChgDateOld[rr]);
    func.varsSet("ls_cur_end_date", lsCurEndDate[rr]);
    func.varsSet("ls_old_end_date", lsOldEndDate[rr]);
    func.varsSet("aa_mod_seqno", aaModSeqno[rr]);
    if (func.insertFunc() < 0) {
      alertErr("寫入卡片暫存檔錯誤~");
      return -1;
    }
    return 1;
  }

  int wfDeleteCrdCardTmp() {
    String[] lsCardNo = wp.itemBuff("card_no");
    func.varsSet("ls_card_no", lsCardNo[rr]);
    if (func.dbDelete2() == -1) {
      alertErr("處理失敗");
      return -1;
    }
    return 1;
  }

  int wfUpdCrdCard() {
    // String ls_expire_reason="";
    // -- 抓取螢幕上所選取之值
    String[] lsDbOptcode = wp.itemBuff("opt");
    if (lsDbOptcode[rr].equals("1")) {
      // ls_expire_reason =db_optcode1[rr];
    }
    if (lsDbOptcode[rr].equals("4")) {
      // ls_expire_reason =db_optcode2[rr];
    }
    if (lsDbOptcode[rr].equals("2")) {
      alertErr("此卡片已送製卡,不可作任何異動");
      return -1;
    }
    // -- 由覆核處理資料 **********************************************************************
    if (wfInsertCrdCardTmp() != 1) {
      return -1;
    }

    return 1;
  }

  int dataProcessCheck() {
    String[] dbOptcode1 = wp.itemBuff("db_optcode1");
    String[] dbOptcode2 = wp.itemBuff("db_optcode2");
    String[] lsAppr = wp.itemBuff("db_appr");
    String[] opt = wp.itemBuff("opt");
    String[] aaRowid = wp.itemBuff("rowid");
    wp.listCount[0] = aaRowid.length;
    for (rr = 0; rr < opt.length; rr++) {
      if (empty(opt[rr])) {
        continue;
      }
      if (opt[rr].equals("1") && empty(dbOptcode1[rr])) {
        alertMsg("請輸入預約原因 !!");
        wp.colSet(rr, "ok_flag", "!");
        return -1;
      }
      if (opt[rr].equals("4") && empty(dbOptcode2[rr])) {
        alertMsg("請輸入人工原因 !!");
        wp.colSet(rr, "ok_flag", "!");
        return -1;
      }
      if (opt[rr].equals("0") && !lsAppr[rr].equals("N")) {
        alertMsg("請執行【取消不續卡(放行後)】!!");
        wp.colSet(rr, "ok_flag", "!");
        return -1;
      }
      if (opt[rr].equals("2") && !lsAppr[rr].equals("Y") && lsAppr[rr].length() != 0) {
        alertMsg("請執行【取消不續卡(放行前)】!!");
        wp.colSet(rr, "ok_flag", "!");
        return -1;
      }
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
    func = new Crdm0080Func(wp);
    if (dataProcessCheck() != 1) {
      return;
    }
    String[] aaRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    String[] comboIndicator = wp.itemBuff("combo_indicator");
    String[] lsCardNo = wp.itemBuff("card_no");

    wp.listCount[0] = aaRowid.length;

    // -update-
    for (rr = 0; rr < opt.length; rr++) {
      ilErr2 = 0;
      func.varsSet("aa_rowid", aaRowid[rr]);

      // wp.col_set(rr, "opt", opt[rr]);
      if (empty(opt[rr])) {
        continue;
      }
      if (wfChkExpireChgFlag() != 1) {
        ilErr++;
        wp.colSet(rr, "ok_flag", "!");
        return;
      }
      switch (opt[rr]) {
        case "0":
          if (wfDeleteCrdCardTmp() != 1) {
            ilErr++;
            wp.colSet(rr, "ok_flag", "!");
            return;
          }
          break;
        case "1":
          func.varsSet("ls_card_no", lsCardNo[rr]);
          func.dbDelete2();
          if (wfUpdCrdCard() != 1) {
            ilErr++;
            wp.colSet(rr, "ok_flag", "!");
            return;
          }
          break;
        case "2":
          func.varsSet("ls_card_no", lsCardNo[rr]);
          func.dbDelete2();
          if (wfCancelExpire() != 1) {
            ilErr++;
            wp.colSet(rr, "ok_flag", "!");
            return;
          }
          if (!comboIndicator[rr].equals("N")) {
            if (wfDelNotchg() != 1) {
              ilErr++;
              wp.colSet(rr, "ok_flag", "!");
              return;
            }
          }
          break;
        case "3":
          func.varsSet("ls_card_no", lsCardNo[rr]);
          func.dbDelete2();
          if (wfMoveEmbossTmp() != 1) {
            ilErr++;
            wp.colSet(rr, "ok_flag", "!");
            return;
          } else {
            if (!comboIndicator[rr].equals("N")) {
              if (wfDelNotchg() != 1) {
                ilErr++;
                wp.colSet(rr, "ok_flag", "!");
                return;
              }
            }
          }
          break;
        case "4":
          func.varsSet("ls_card_no", lsCardNo[rr]);
          func.dbDelete2();
          if (wfUpdCrdCard() != 1) {
            ilErr++;
            wp.colSet(rr, "ok_flag", "!");
            return;
          }
      }
      if (ilErr > 0) {
        sqlCommit(0);
        return;
      }
    }
    sqlCommit(1);
    alertMsg("處理成功");
    queryFunc();
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}

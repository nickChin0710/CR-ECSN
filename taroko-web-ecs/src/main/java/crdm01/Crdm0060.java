/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-27  V1.00.00  yash            program initial                       *
* 109-02-04  V1.00.01  JustinWu   add new validations and insertion function *
* 109-03-04  V1.00.02  JustinWu   keep opt_on and add new inserted columns   *
* 109-03-16  V1.00.03  Wilson     card_flag = '1'                            *
* 109-04-09  V1.00.04  Wilson     post_flag = 'Y'                            *
* 109-04-28  V1.00.05  YangFang   updated for project coding standard        *  
* 112-02-06  V1.00.06  Ryan       add updated crd_emap_tmp.bin_no(= card_no的前6碼)    *  
******************************************************************************/

package crdm01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import busi.ecs.CommFunction;

public class Crdm0060 extends BaseProc {
  String mExBatchno = "";
  String mExIdNo = "";
  CommString commString = new CommString();
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
        // insertFunc();
        break;
      case "U":
        /* 更新功能 */
        // updateFunc();
        break;
      case "D":
        /* 刪除功能 */
        // deleteFunc();
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
      case "S2":
        /* 執行 */
        strAction = "S2";
        dataProcess();
        break;
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    String exBatchno = wp.itemStr("ex_batchno");
    String exIdNo = wp.itemStr("ex_id_no");
    wp.whereStr = " where 1=1 ";
    // 固定條件
    wp.whereStr += " and  card_no =''  ";
    // wp.whereStr += "and ( nvl(check_code,'0') != '0' or card_no ='') ";

    // 自鍵條件
    if (notEmpty(exBatchno)) {
      wp.whereStr += sqlCol(exBatchno, "batchno");
    }
    if (notEmpty(exIdNo)) {
      wp.whereStr += sqlCol(exIdNo, "apply_id");
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex (rowid) AS rowid, " + "'        ' cpm_birthday, "
        + "(aps_batchno || '-' || aps_recno || '-' || seqno) db_aps_batchno, "
        + "(batchno || '_' || recno) db_batchno, " + "accept_dm, " + "acct_key, " + "acct_type, "
        + "act_no, " + "act_no_f, " + "act_no_f_ind, " + "annual_fee, " + "apply_id, "
        + "apply_id_code, " + "apply_id_ecode, " + "apply_no, " + "apr_date, " + "apr_user, "
        + "aps_batchno, " + "aps_recno, " + "batchno, " + "birthday, " + "business_code, "
        + "card_no, " + "card_type, " + "cardcat, " + "cardno_code, " + "cellar_phone, "
        + "check_code, " + "chi_name, " + "class_code, " + "comm_flag, " + "company_name, "
        + "corp_act_flag, " + "corp_assure_flag, " + "corp_no, " + "corp_no_code, "
        + "corp_no_ecode, " + "credit_flag, " + "credit_lmt, " + "crt_date, " + "cvv, " + "cvv2, "
        + "e_mail_addr, " + "education, " + "emboss_4th_data, " + "emboss_date, " + "eng_name, "
        + "fee_code, " + "fee_reason_code, " + "final_fee_code, " + "force_flag, " + "group_code, "
        + "home_area_code1, " + "home_area_code2, " + "home_tel_ext1, " + "home_tel_ext2, "
        + "home_tel_no1, " + "home_tel_no2, " + "introduce_id, " + "introduce_name, "
        + "introduce_no, " + "job_position, " + "lpad (' ', 20, ' ') db_msg, " + "mail_addr1, "
        + "mail_addr2, " + "mail_addr3, " + "mail_addr4, " + "mail_addr5, " + "mail_type, "
        + "mail_zip, " + "major_card_no, " + "major_chg_flag, " + "major_valid_fm, "
        + "major_valid_to, " + "marriage, " + "member_id, " + "mod_pgm, " + "mod_seqno, "
        + "mod_time, " + "mod_user, " + "nation, " + "nccc_batchno, " + "nccc_recno, "
        + "nccc_type, " + "office_area_code1, " + "office_area_code2, " + "office_tel_ext1, "
        + "office_tel_ext2, " + "office_tel_no1, " + "office_tel_no2, " + "old_card_no, "
        + "online_mark, " + "oth_chk_code, " + "other_cntry_code, " + "passport_no, "
        + "pm_birthday, " + "pm_cash, " + "pm_id, " + "pm_id_code, " + "pm_id_ecode, "
        + "police_no1, " + "police_no2, " + "police_no3, " + "pvki, " + "pvv, " + "recno, "
        + "reg_bank_no, " + "reject_code, " + "reject_date, " + "rel_with_pm, " + "resident_addr1, "
        + "resident_addr2, " + "resident_addr3, " + "resident_addr4, " + "resident_addr5, "
        + "resident_no, " + "resident_zip, " + "risk_bank_no, " + "salary, " + "salary_code, "
        + "seqno, " + "service_code, " + "service_year, " + "sex, " + "source, " + "source_code, "
        + "staff_flag, " + "standard_fee, " + "stmt_cycle, " + "student, " + "sup_birthday, "
        + "sup_cash, " + "unit_code, " + "valid_fm, " + "valid_to, " + "value, " + "vip";
    wp.daoTable = "crd_emap_tmp";
    wp.whereOrder = " order by batchno";
    getWhereStr();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
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
  public void dataProcess() throws Exception {

    String[] aaCardNo = wp.itemBuff("card_no");
    String[] aaCardType = wp.itemBuff("card_type");
    String[] aaGroupCode = wp.itemBuff("group_code");
    String[] aaUnitCode = wp.itemBuff("unit_code");
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] opt = wp.itemBuff("opt");
    optNumKeep(wp.itemRows("ser_num"));

    wp.listCount[0] = aaRowid.length;


    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    // save
    int rr = -1;
    int llOk = 0, llErr = 0;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "");

      if (empty(aaCardNo[rr]) || aaCardNo[rr].length() != 16) {

        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "ls_errmsg", "卡號不正確 !!");
        llErr++;
        continue;
      }


      String lsCardNo = aaCardNo[rr];
      String lsBinNo = lsCardNo.substring(0, 6);
      String lsSeqno = lsCardNo.substring(6, 16);

      // 2020-02-04 Justin Begin
      String lsSql1 = "select beg_seqno, end_seqno " + " from crd_cardno_range "
          + " where group_code =:group_code " + " and card_type =:card_type "
          + " and bin_no =:bin_no " + " and card_flag = '1' " + " and post_flag = 'Y' ";

      setString("group_code", aaGroupCode[rr]);
      setString("card_type", aaCardType[rr]);
      setString("bin_no", lsBinNo);
      sqlSelect(lsSql1);

      if (sqlRowNum == 0 || !chkStrend(sqlStr("beg_seqno"), lsSeqno.substring(0, 9))
          || !chkStrend(lsSeqno.substring(0, 9), sqlStr("end_seqno"))) {
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "ls_errmsg", "該卡號不在區間內");
        llErr++;
        continue;
      }

      String lsSql2 = "select card_no from crd_prohibit " + " where card_no = :card_no ";
      setString("card_no", lsCardNo);
      sqlSelect(lsSql2);

      if (sqlRowNum >= 1) {
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "ls_errmsg", "該卡號為禁號");
        llErr++;
        continue;
      }

      CommFunction comm = new CommFunction();
      String checkNum = comm.cardChkCode(lsCardNo.substring(0, 15));
      if (!comm.isNumber(checkNum) || !lsCardNo.substring(15, 16).equals(checkNum)) {
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "ls_errmsg", "該卡號檢查碼有誤");
        llErr++;
        continue;
      }


      // 2020-02-04 End

      String lsSql3 = "select reserve, use_date " + " from crd_seqno_log "
          + " where bin_no=:bin_no " + " and seqno=:seqno  ";
      setString("bin_no", lsBinNo);
      setString("seqno", lsSeqno);
      sqlSelect(lsSql3);
      String reserve = sqlStr("reserve");
      String useDate = sqlStr("use_date");

      // // 2020-02-04
      // if (sql_nrow <= 0) {
      // wp.col_set(rr, "ok_flag", "X");
      // wp.col_set(rr,"ls_errmsg", "select crd_seqno_log err ! ");
      // ll_err++;
      // continue;
      // }

      if (reserve.equals("Y") && !empty(useDate)) {
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "ls_errmsg", "此號碼已使用 !!");
        llErr++;
        continue;
      }
      // update crd_emap_tmp
      String usSq = " update crd_emap_tmp set " + "  card_no =:card_no ," + " bin_no = :bin_no ,"
          + "  apr_user =:apr_user ," + "  apr_date =:apr_date ," + "  mod_user =:mod_user ,"
          + "  mod_time =sysdate , " + "  mod_pgm =:mod_pgm , " + "  mod_seqno =nvl(mod_seqno,0)+1 "
          + " where hex(rowid) =:rowid  and mod_seqno=:mod_seqno ";
      setString("card_no", aaCardNo[rr]);
      setString("bin_no", commString.left(aaCardNo[rr], 6));
      setString("apr_user", wp.itemStr("approval_user"));
      setString("apr_date", getSysDate());
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.itemStr("mod_pgm"));
      setString("rowid", aaRowid[rr]);
      setString("mod_seqno", aaModSeqno[rr]);
      sqlExec(usSq);
      if (sqlRowNum <= 0) {
        sqlCommit(0);
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "ls_errmsg", "up crd_emap_tmp err ");
        llErr++;
        continue;
      }


      // 確認使用者指定的卡號是否已存在crd_seqno_log
      String lsSql4 = "select bin_no, seqno from crd_seqno_log " + " where bin_no = :bin_no "
          + " and seqno = :seqno ";
      setString("bin_no", lsBinNo);
      setString("seqno", lsSeqno);

      sqlSelect(lsSql4);

      String usSql2 = "";
      if (sqlRowNum >= 1) {
        // update crd_seqno_log
        usSql2 = " update crd_seqno_log set " + "  reserve ='Y' ," + "  use_date=:use_date ,"
            + "  use_id=:use_id ," + " card_item=:card_item ," + " unit_code=:unit_code ,"
            + "  mod_user =:mod_user ," + "  mod_time =sysdate , " + "  mod_pgm =:mod_pgm , "
            + "  mod_seqno =nvl(mod_seqno,0)+1 " + " where bin_no = :bin_no "
            + " and seqno = :seqno  ";
        setString("use_date", getSysDate());
        setString("card_item", aaUnitCode[rr] + aaCardType[rr]);
        setString("unit_code", aaUnitCode[rr]);
        setString("use_id", wp.loginUser);
        setString("mod_user", wp.loginUser);
        setString("mod_pgm", wp.itemStr("mod_pgm"));
        setString("bin_no", lsBinNo);
        setString("seqno", lsSeqno);

      } else {
        // insert crd_seqno_log 2020-02-07 Justin
        usSql2 = " insert into crd_seqno_log ( " + " card_type_sort, " + " bin_no, " + " seqno, "
            + " card_type, " + " group_code, " // 5
            + " card_flag, " + " reserve, " + " trans_no, " + " use_date, " + " use_id, " // 10
            + " card_item, " + " unit_code," + " crt_date," + " mod_user," + " mod_time," // 15
            + " mod_pgm," + " seqno_old " + " ) values ( " + " :card_type_sort, " + " :bin_no, "
            + " :seqno, " + " :card_type, " + " :group_code, " + " :card_flag, " + " :reserve, "
            + " :trans_no, " + " :use_date, " + " :use_id, " + " :card_item, " + " :unit_code, "
            + " :crt_date," + " :mod_user," + " sysdate," + " :mod_pgm," + " :seqno_old " + " ) ";

        setString("card_type_sort", "0");
        setString("bin_no", lsBinNo);
        setString("seqno", lsSeqno);
        setString("card_type", aaCardType[rr]);
        setString("group_code", aaGroupCode[rr]); // 5
        setString("card_flag", "1");
        setString("reserve", "Y");
        setString("trans_no", "");
        setString("use_date", getSysDate());
        setString("use_id", wp.loginUser); // 10
        setString("card_item", aaUnitCode[rr] + aaCardType[rr]);
        setString("unit_code", aaUnitCode[rr]);
        setString("crt_date", getSysDate());
        setString("mod_user", wp.loginUser);
        // 15
        setString("mod_pgm", wp.itemStr("mod_pgm"));
        setString("seqno_old", lsSeqno.substring(0, 9)); // 17

      }

      sqlExec(usSql2);
      if (sqlRowNum <= 0) {
        sqlCommit(0);
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "ls_errmsg", "up crd_seqno_log err ");
        llErr++;
        continue;
      } else {
        sqlCommit(1);
        wp.colSet(rr, "ok_flag", "V");
        llOk++;
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

      // wp.optionKey = wp.col_ss("zip_code");
      // this.dddw_list("dddw_zipcode", "ptr_zipcode", "zip_code", "",
      // "where 1=1 order by zip_code");
    } catch (Exception ex) {
    }
  }



}

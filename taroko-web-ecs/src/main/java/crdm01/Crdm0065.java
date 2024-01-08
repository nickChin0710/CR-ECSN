/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-09  V1.00.00  Andy       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-07-30  V1.00.02  Andy		  update : Debug                             *
* 108-12-03  V1.00.03  Amber	  Update init_button Authority 	     		 *
* 108-12-17  V1.00.04  Andy 	  Update UI                  	     		 * 
* 108-12-18  V1.00.05  Andy 	  Update UI                  	     		 * 
* 108-12-20  V1.00.06  Andy 	  Update UI                  	     		 * 
* 2020-03-11 V1.00.07  Yuqi 	  針對新製卡檢核有誤的資料更正或退件                                                          * 
* 109-01-04  V1.00.03  shiyuqi       修改无意义命名                                                                                      *  
* 112-03-09  V1.00.04  Wilson    updateCrdEmapTmp2 set reject_code = check_code*
******************************************************************************/

package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0065 extends BaseEdit {
  String mExBatchno = "";
  String mExIdNo = "";
  String mExProcFlag = "";
  String[] aaRowid = null;
  String[] aaModSeqno = null;
  String[] opt = null;

  String[] aaBatchno = null;
  String[] aaRecno = null;
  String[] aaApplyId = null;
  String[] aaBirthday = null;
  String[] aaEngName = null;
  String[] aaStmtCycle = null;
  String[] aaSource = null;
  String[] aaActNof = null;
  String[] aaActNofind = null;
  // insert crd_apscdinv use
  String[] aaApsBatchno = null;
  String[] aaApsRecno = null;
  String[] aaApsSeqno = null;
  String[] aaSeqno = null;
  String[] aaApplyIdCode = null;
  String[] aaPmId = null;
  String[] aaPmIdCode = null;
  String[] aaCardType = null;
  String[] aaGroupCode = null;
  String[] aaChiName = null;
  // crd_reject
  // String[] aa_emboss_source = wp.item_buff("emboss_source");
  String[] aaCardNo = null;
  String[] aaMajorValidFm = null;
  String[] aaMajorValidTo = null;
  String[] aaSourceCode = null;
  String[] aaCorpNo = null;
  String[] aaCorpActFlag = null;
  String[] aaCorpAssureFlag = null;
  String[] aaRegBankNo = null;
  String[] aaRiskBankNo = null;
  String[] aaMarriage = null;
  String[] aaServiceYear = null;
  String[] aaEducation = null;
  String[] aaNation = null;
  String[] aaSalary = null;
  String[] aaMailZip = null;
  String[] aaMailAddr1 = null;
  String[] aaMailAddr2 = null;
  String[] aaMailAddr3 = null;
  String[] aaMailAddr4 = null;
  String[] aaMailAddr5 = null;
  String[] aaResidentZip = null;
  String[] aaResidentAddr1 = null;
  String[] aaResidentAddr2 = null;
  String[] aaResidentAddr3 = null;
  String[] aaResidentAddr4 = null;
  String[] aaResidentAddr5 = null;
  String[] aaCompanyName = null;
  String[] aaJobPosition = null;
  String[] aaHomeAreaCode1 = null;
  String[] aaHomeTelNo1 = null;
  String[] aaHomeTelExt1 = null;
  String[] aaHomeAreaCode2 = null;
  String[] aaHomeTelNo2 = null;
  String[] aaHomeTelExt2 = null;
  String[] aaOfficeAreaCode1 = null;
  String[] aaOfficeTelNo1 = null;
  String[] aaOfficeTelExt1 = null;
  String[] aaOfficeAreaCode2 = null;
  String[] aaOfficeTelNo2 = null;
  String[] aaOfficeTelExt2 = null;
  String[] aaEmailAddr = null;
  String[] aaCellarPhone = null;
  String[] aaActNo = null;
  String[] aaVip = null;
  String[] aaFeeCode = null;
  String[] aaForceFlag = null;
  String[] aaBusinessCode = null;
  String[] aaIntroduceNo = null;
  String[] aaValidFm = null;
  String[] aaValidTo = null;
  String[] aaSex = null;
  String[] aaValue = null;
  String[] aaAcceptDm = null;
  String[] aaApplyNo = null;
  String[] aaCardcat = null;
  String[] aaMailType = null;
  String[] aaIntroduceId = null;
  String[] aaIntroduceName = null;
  String[] aaSalaryCode = null;
  String[] aaStudent = null;
  String[] aaCreditLmt = null;
  String[] aaApplyIdEcode = null;
  String[] aaCorpNoEcode = null;
  String[] aaPmIdEcode = null;
  String[] aaPoliceNo1 = null;
  String[] aaPoliceNo2 = null;
  String[] aaPoliceNo3 = null;
  String[] aaPmCash = null;
  String[] aaSupCash = null;
  String[] aaOnlineMark = null;
  String[] aaRejectCode = null;
  String[] aaEmboss4thData = null;
  String[] aaMemberId = null;
  String[] aaPmBirthday = null;
  String[] aaSupBirthday = null;
  String[] aaStandardFee = null;
  String[] aaFinalFeeCode = null;
  String[] aaFeeReasonCode = null;
  String[] aaAnnualFee = null;
  String[] aaServiceCode = null;
  String[] aaCrtDate = null;
  String[] aaCheckCode = null;
  // 新增欄位
  String[] aaUrFlag = null;
  String[] aaENews = null;
  String[] aaSpouseIdNo = null;
  String[] aaSpouseBirthday = null;
  String[] aaResidentNoExpireDate = null;
  String[] aaPassportDate = null;
  String[] aaRoadsideAssistApply = null;
  String[] aaBillApplyFlag = null;
  String[] aaRevolveIntRate = null;
  String[] aaSpecialCardRate = null;
  String[] aaCompanyZip = null;
  String[] aaAutopayAcctBank = null;
  String[] aaInstFlag = null;
  String[] aaFeeCodei = null;
  String[] aaGraduationElementarty = null;
  String[] aaSpouseName = null;
  String[] aaCompanyAddr1 = null;
  String[] aaCompanyAddr2 = null;
  String[] aaCompanyAddr3 = null;
  String[] aaCompanyAddr4 = null;
  String[] aaCompanyAddr5 = null;
  String[] aaCurrChangeAccout = null;
  String[] aaCreditLevelNew = null;
  String[] aaCrtUser = null;
  String[] aaRevolveIntRateYear = null;
  String[] aaPromoteEmpNo = null;
  String[] aaPromoteDept = null;
  String[] aaCardRefNum = null;
  String[] aaIntroduceEmpNo = null;

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
    String exBatchno = wp.itemStr("ex_batchno");
    String exIdNo = wp.itemStr("ex_id_no");
    wp.whereStr = " where 1=1 ";
    // 固定條件
    wp.whereStr += "and ( decode(check_code,'','000',check_code) != '000') and reject_code = '' ";

    // 自鍵條件

    // 問題0000644 user提出去掉查詢限制
    // if(empty(ex_batchno) && empty(ex_id_no)){
    // alert_err("請至少輸入一項查詢條件");
    // return false;
    // }

    if (empty(exBatchno) == false) {
      wp.whereStr += sqlCol(exBatchno, "batchno", "like%");
    }
    if (empty(exIdNo) == false) {
      wp.whereStr += sqlCol(exIdNo, "apply_id");
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    if (getWhereStr() == false)
//      return;
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex (rowid) AS rowid, " + "'        ' cpm_birthday, " + "aps_batchno, "
        + "(aps_batchno || '-' || aps_recno ) db_aps_batchno, "
        + "(batchno || '-' || recno) db_batchno, " + "accept_dm, " + "acct_key, " + "acct_type, "
        + "act_no, " + "act_no_f, " + "act_no_f_ind, " + "annual_fee, " + "apply_id, "
        + "apply_id_code, " + "(apply_id || '-' || apply_id_code) db_apply_id," + "apply_id_ecode, "
        + "apply_no, " + "apr_date, " + "apr_user, " + "aps_batchno, " + "aps_recno, " + "batchno, "
        + "birthday, " + "business_code, " + "card_no, " + "card_type, " + "cardcat, "
        + "cardno_code, " + "cellar_phone, " + "check_code, "
        // + "check_code||':'||(select wf_desc from ptr_sys_idtab where wf_type ='CHECK_CODE' and
        // wf_id = crd_emap_tmp.check_code) as db_check_code, "
        + "chi_name, " + "class_code, " + "comm_flag, " + "company_name, " + "corp_act_flag, "
        + "corp_assure_flag, " + "corp_no, " + "corp_no_code, " + "corp_no_ecode, "
        + "credit_flag, " + "credit_lmt, " + "crt_date, " + "cvv, " + "cvv2, " + "e_mail_addr, "
        + "education, " + "emboss_4th_data, " + "emboss_date, " + "eng_name, " + "fee_code, "
        + "fee_reason_code, " + "final_fee_code, " + "decode(force_flag,'','N','Y') as force_flag, "
        + "group_code, " + "home_area_code1, " + "home_area_code2, " + "home_tel_ext1, "
        + "home_tel_ext2, " + "home_tel_no1, " + "home_tel_no2, " + "introduce_id, "
        + "introduce_name, " + "introduce_no, " + "job_position, " + "lpad (' ', 20, ' ') db_msg, "
        + "mail_addr1, " + "mail_addr2, " + "mail_addr3, " + "mail_addr4, " + "mail_addr5, "
        + "mail_type, " + "mail_zip, " + "major_card_no, " + "major_chg_flag, " + "major_valid_fm, "
        + "major_valid_to, " + "marriage, " + "member_id, " + "mod_pgm, " + "mod_seqno, "
        + "mod_time, " + "mod_user, " + "nation, " + "nccc_batchno, " + "nccc_recno, "
        + "nccc_type, " + "office_area_code1, " + "office_area_code2, " + "office_tel_ext1, "
        + "office_tel_ext2, " + "office_tel_no1, " + "office_tel_no2, " + "old_card_no, "
        + "online_mark, " + "oth_chk_code, " + "other_cntry_code, " + "passport_no, "
        + "pm_birthday, " + "pm_cash, " + "pm_id, " + "pm_id_code, "
        + "(pm_id || '-' || pm_id_code) db_pm_id," + "pm_id_ecode, " + "police_no1, "
        + "police_no2, " + "police_no3, " + "pvki, " + "pvv, " + "recno, " + "reg_bank_no, "
        + "reject_code, " + "reject_date, " + "rel_with_pm, " + "resident_addr1, "
        + "resident_addr2, " + "resident_addr3, " + "resident_addr4, " + "resident_addr5, "
        + "resident_no, " + "resident_zip, " + "risk_bank_no, " + "salary, " + "salary_code, "
        + "seqno, " + "service_code, " + "service_year, " + "sex, " + "source, " + "source_code, "
        + "staff_flag, " + "standard_fee, " + "stmt_cycle, " + "student, " + "sup_birthday, "
        + "sup_cash, " + "unit_code, " + "valid_fm, " + "valid_to, " + "value, " + "vip, "
        + "ur_flag, " // 新增
        + "e_news, " + "spouse_id_no, " + "spouse_birthday, " + "resident_no_expire_date, "
        + "passport_date, " + "roadside_assist_apply, " + "bill_apply_flag, " + "revolve_int_rate, "
        + "special_card_rate, " + "company_zip, " + "autopay_acct_bank, " + "inst_flag, "
        + "fee_code_i, " + "graduation_elementarty, " + "spouse_name, " + "company_addr1, "
        + "company_addr2, " + "company_addr3, " + "company_addr4, " + "company_addr5, "
        + "curr_change_accout, " + "credit_level_new, " + "crt_user, " + "revolve_int_rate_year, "
        + "promote_dept, " + "promote_emp_no, " + "card_ref_num, " + "introduce_emp_no, "// 结束
        + "'' err_msg ";
    wp.daoTable = "crd_emap_tmp";
    wp.whereOrder = " order by batchno,recno";
    getWhereStr();
    // System.out.println("select " + wp.selectSQL + " from
    // "+wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkData();
  }

  void listWkData() throws Exception {
    int rowCt = 0;
    String isSql = "";
    String wpCheckCode = "", wpCardnoCode = "";
    String dbMsg = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");
      // source
      String source = wp.colStr(ii, "source");
      String[] cde0 = new String[] {"1", "2", "3", "4", "5"};
      String[] txt0 = new String[] {"1.[新製卡]", "2.[普昇金卡]", "3.[整批續卡]", "4.[提前續卡]", "5.[重製]"};
      wp.colSet(ii, "db_source", commString.decode(source, cde0, txt0));

      // db_online_mark
      String onlineMark = wp.colStr(ii, "online_mark");
      String[] cde = new String[] {"0", "1", "2"};
      String[] txt = new String[] {"一般卡", "線上製卡", "緊急製卡"};
      wp.colSet(ii, "db_online_mark", commString.decode(onlineMark, cde, txt));

      // db_msg
      wpCheckCode = wp.colStr(ii, "check_code");
      wpCardnoCode = wp.colStr(ii, "cardno_code");
      dbMsg = "";
      if (empty(wpCheckCode)) {
        dbMsg = "尚未做製卡檢核";
      }
      if (wpCheckCode.equals("000") == false) {
        isSql = "select msg from crd_message ";
        isSql += "where 1=1 and msg_type = 'NEW_CARD' ";
        isSql += " and msg_value = :msg_value ";
        setString("msg_value",wpCheckCode);
        sqlSelect(isSql);
        if (sqlRowNum > 0) {
          dbMsg = sqlStr("msg");
        }
      }
      if (wpCardnoCode.equals("1")) {
        dbMsg = "無法編列卡號";
      }
      wp.colSet(ii, "db_msg", dbMsg);

    }
    wp.colSet("row_ct", intToStr(rowCt));
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
    mExProcFlag = wp.itemStr("ex_proc_flag");

    // update main table [crd_emap_tmp] use
    aaRowid = wp.itemBuff("rowid");
    aaModSeqno = wp.itemBuff("mod_seqno");
    opt = wp.itemBuff("opt");

    aaBatchno = wp.itemBuff("batchno");
    aaRecno = wp.itemBuff("recno");
    aaApplyId = wp.itemBuff("apply_id");
    aaBirthday = wp.itemBuff("birthday");
    aaEngName = wp.itemBuff("eng_name");
    aaStmtCycle = wp.itemBuff("stmt_cycle");
    aaSource = wp.itemBuff("source");
    aaSourceCode = wp.itemBuff("source_code");
    aaActNof = wp.itemBuff("act_no_f");
    aaActNofind = wp.itemBuff("act_no_f_ind");
    // insert crd_apscdinv use
    aaApsBatchno = wp.itemBuff("aps_batchno");
    aaApsRecno = wp.itemBuff("aps_recno");
    aaApsSeqno = wp.itemBuff("seqno");
    aaSeqno = wp.itemBuff("seqno");
    aaApplyIdCode = wp.itemBuff("apply_id_code");
    aaPmId = wp.itemBuff("pm_id");
    aaPmIdCode = wp.itemBuff("pm_id_code");
    aaCardType = wp.itemBuff("card_type");
    aaGroupCode = wp.itemBuff("group_code");
    aaChiName = wp.itemBuff("chi_name");
    // crd_reject
    // String[] aa_emboss_source = wp.item_buff("emboss_source");
    aaCardNo = wp.itemBuff("card_no");
    aaMajorValidFm = wp.itemBuff("major_valid_fm");
    aaMajorValidTo = wp.itemBuff("major_valid_to");
    aaCorpNo = wp.itemBuff("corp_no");
    aaCorpActFlag = wp.itemBuff("corp_act_flag");
    aaCorpAssureFlag = wp.itemBuff("corp_assure_flag");
    aaRegBankNo = wp.itemBuff("reg_bank_no");
    aaRiskBankNo = wp.itemBuff("risk_bank_no");
    aaMarriage = wp.itemBuff("marriage");
    aaServiceYear = wp.itemBuff("service_year");
    aaEducation = wp.itemBuff("education");
    aaNation = wp.itemBuff("nation");
    aaSalary = wp.itemBuff("salary");
    aaMailZip = wp.itemBuff("mail_zip");
    aaMailAddr1 = wp.itemBuff("mail_addr1");
    aaMailAddr2 = wp.itemBuff("mail_addr2");
    aaMailAddr3 = wp.itemBuff("mail_addr3");
    aaMailAddr4 = wp.itemBuff("mail_addr4");
    aaMailAddr5 = wp.itemBuff("mail_addr5");
    aaResidentZip = wp.itemBuff("resident_zip");
    aaResidentAddr1 = wp.itemBuff("resident_addr1");
    aaResidentAddr2 = wp.itemBuff("resident_addr2");
    aaResidentAddr3 = wp.itemBuff("resident_addr3");
    aaResidentAddr4 = wp.itemBuff("resident_addr4");
    aaResidentAddr5 = wp.itemBuff("resident_addr5");
    aaCompanyName = wp.itemBuff("company_name");
    aaJobPosition = wp.itemBuff("job_position");
    aaHomeAreaCode1 = wp.itemBuff("home_area_code1");
    aaHomeTelNo1 = wp.itemBuff("home_tel_no1");
    aaHomeTelExt1 = wp.itemBuff("home_tel_ext1");
    aaHomeAreaCode2 = wp.itemBuff("home_area_code2");
    aaHomeTelNo2 = wp.itemBuff("home_tel_no2");
    aaHomeTelExt2 = wp.itemBuff("home_tel_ext2");
    aaOfficeAreaCode1 = wp.itemBuff("office_area_code1");
    aaOfficeTelNo1 = wp.itemBuff("office_tel_no1");
    aaOfficeTelExt1 = wp.itemBuff("office_tel_ext1");
    aaOfficeAreaCode2 = wp.itemBuff("office_area_code2");
    aaOfficeTelNo2 = wp.itemBuff("office_tel_no2");
    aaOfficeTelExt2 = wp.itemBuff("office_tel_ext2");
    aaEmailAddr = wp.itemBuff("e_mail_addr");
    aaCellarPhone = wp.itemBuff("cellar_phone");
    aaActNo = wp.itemBuff("act_no");
    aaVip = wp.itemBuff("vip");
    aaFeeCode = wp.itemBuff("fee_code");
    aaForceFlag = wp.itemBuff("force_flag");
    aaBusinessCode = wp.itemBuff("business_code");
    aaIntroduceNo = wp.itemBuff("introduce_no");
    aaValidFm = wp.itemBuff("valid_fm");
    aaValidTo = wp.itemBuff("valid_to");
    aaSex = wp.itemBuff("sex");
    aaValue = wp.itemBuff("value");
    aaAcceptDm = wp.itemBuff("accept_dm");
    aaApplyNo = wp.itemBuff("apply_no");
    aaCardcat = wp.itemBuff("cardcat");
    aaMailType = wp.itemBuff("mail_type");
    aaIntroduceId = wp.itemBuff("introduce_id");
    aaIntroduceName = wp.itemBuff("introduce_name");
    aaSalaryCode = wp.itemBuff("salary_code");
    aaStudent = wp.itemBuff("student");
    aaCreditLmt = wp.itemBuff("credit_lmt");
    aaApplyIdEcode = wp.itemBuff("apply_id_ecode");
    aaCorpNoEcode = wp.itemBuff("corp_no_ecode");
    aaPmIdEcode = wp.itemBuff("pm_id_ecode");
    aaPoliceNo1 = wp.itemBuff("police_no1");
    aaPoliceNo2 = wp.itemBuff("police_no2");
    aaPoliceNo3 = wp.itemBuff("police_no3");
    aaPmCash = wp.itemBuff("pm_cash");
    aaSupCash = wp.itemBuff("sup_cash");
    aaOnlineMark = wp.itemBuff("online_mark");
    aaRejectCode = wp.itemBuff("reject_code");
    aaEmboss4thData = wp.itemBuff("emboss_4th_data");
    aaMemberId = wp.itemBuff("member_id");
    aaPmBirthday = wp.itemBuff("pm_birthday");
    aaSupBirthday = wp.itemBuff("sup_birthday");
    aaStandardFee = wp.itemBuff("standard_fee");
    aaFinalFeeCode = wp.itemBuff("final_fee_code");
    aaFeeReasonCode = wp.itemBuff("fee_reason_code");
    aaAnnualFee = wp.itemBuff("annual_fee");
    aaServiceCode = wp.itemBuff("service_code");
    aaCrtDate = wp.itemBuff("crt_date");
    aaCheckCode = wp.itemBuff("check_code");
    // 新增的欄位
    aaUrFlag = wp.itemBuff("ur_flag");
    aaENews = wp.itemBuff("e_news");
    aaSpouseIdNo = wp.itemBuff("spouse_id_no");
    aaSpouseBirthday = wp.itemBuff("spouse_birthday");
    aaResidentNoExpireDate = wp.itemBuff("resident_no_expire_date");
    aaPassportDate = wp.itemBuff("passport_date");
    aaRoadsideAssistApply = wp.itemBuff("roadside_assist_apply");
    aaBillApplyFlag = wp.itemBuff("bill_apply_flag");
    aaRevolveIntRate = wp.itemBuff("revolve_int_rate");
    aaSpecialCardRate = wp.itemBuff("special_card_rate");
    aaCompanyZip = wp.itemBuff("company_zip");
    aaAutopayAcctBank = wp.itemBuff("autopay_acct_bank");
    aaInstFlag = wp.itemBuff("inst_flag");
    aaFeeCodei = wp.itemBuff("fee_code_i");
    aaGraduationElementarty = wp.itemBuff("graduation_elementarty");
    aaSpouseName = wp.itemBuff("spouse_name");
    aaCompanyAddr1 = wp.itemBuff("company_addr1");
    aaCompanyAddr2 = wp.itemBuff("company_addr2");
    aaCompanyAddr3 = wp.itemBuff("company_addr3");
    aaCompanyAddr4 = wp.itemBuff("company_addr4");
    aaCompanyAddr5 = wp.itemBuff("company_addr5");
    aaCurrChangeAccout = wp.itemBuff("curr_change_accout");
    aaCreditLevelNew = wp.itemBuff("credit_level_new");
    aaCrtUser = wp.itemBuff("crt_user");
    aaRevolveIntRateYear = wp.itemBuff("revolve_int_rate_year");
    aaPromoteDept = wp.itemBuff("promote_dept");
    aaPromoteEmpNo = wp.itemBuff("promote_emp_no");
    aaCardRefNum = wp.itemBuff("card_ref_num");
    aaIntroduceEmpNo = wp.itemBuff("introduce_emp_no");

    wp.listCount[0] = aaBatchno.length;
    String isSql = "", wpStmtCycle = "", mRowid = "", mModSeqno = "";
    int rr = -1;
    int llOk = 0, llErr = 0;
    // check
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
    }

    if (llErr == 0) {
      rr = -1;
      for (int ii = 0; ii < opt.length; ii++) {
        wpStmtCycle = aaStmtCycle[ii];
        rr = (int) this.toNum(opt[ii]) - 1;
        if (rr < 0) {
          continue;
        }
        // chrck stmt_cycle
        if (mExProcFlag.equals("0")) {
          if (wfChkStmtCycle(wpStmtCycle) != 1) {
            wp.colSet(rr, "err_msg", "關帳期不存在");
            wp.colSet(rr, "ok_flag", "X");
            llErr++;
            continue;
          }
          if (updateCrdEmapTmp(rr) != 1) {
            llErr++;
            continue;
          }
          if (aaSource[rr].equals("1")) {
            if (crdApscdinvProc(rr) != 1) {
              llErr++;
              continue;
            }
          }
          llOk++;
          sqlCommit(1);
        } else {
          // if (update_crd_emap_tmp(rr) != 1) {
          // ll_err++;
          // continue;
          // }

          if (aaSource[rr].equals("1")) {
            if (crdApscdinvProc(rr) != 1) {
              llErr++;
              continue;
            }
          }
          if (aaSource[rr].equals("1") || aaSource[rr].equals("2")) {
            if (crdRejectProc(rr) != 1) {
              llErr++;
              continue;
            }
          }
          if (updateCrdEmapTmp2(rr) != 1) {
            llErr++;
            continue;
          }
          llOk++;
          sqlCommit(1);
        }
      }
    }
    alertMsg("成功=" + llOk + " 失敗=" + llErr);
  }

  public int wfChkStmtCycle(String stmtCycle) throws Exception {
    String isSql = "";
    isSql = "select count(*) ct from ptr_workday where 1=1 ";
    isSql += sqlCol(stmtCycle, "stmt_cycle");
    sqlSelect(isSql);
    if (sqlRowNum > 0) {
      if (sqlStr("ct").equals("0")) {
        return -1;
      }
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
      // wp.optionKey = wp.col_ss("mail_branch");
      // this.dddw_list("dddw_branch", "ptr_branch", "branch",
      // "branch_name", "where 1=1 order by branch");
      // wp.optionKey = wp.col_ss("zip_code");
      // this.dddw_list("dddw_zipcode", "ptr_zipcode", "zip_code", "",
      // "where 1=1 order by zip_code");
    } catch (Exception ex) {
    }
  }

  public int updateCrdEmapTmp(int ii) {
    String usSql = "", isCheckCode = "";
    if (mExProcFlag.equals("0")) {
      isCheckCode = "";
    } else {
      isCheckCode = aaCheckCode[ii];
    }

    usSql = "update crd_emap_tmp set " + "source_code =:source_code, " + "eng_name =:eng_name, "
        + "birthday =:birthday, " + "stmt_cycle =:stmt_cycle, " + "act_no_f =:act_no_f, "
        + "act_no_f_ind =:act_no_f_ind, " + "check_code =:ls_check_code, " + "mod_user =:mod_user, "
        + "mod_time = sysdate, " + "mod_pgm = 'Crdm0065', " + "mod_seqno = nvl(mod_seqno,0)+1 where hex(rowid) = :rowid";
    setString("source_code", aaSourceCode[ii]);
    setString("eng_name", aaEngName[ii]);
    setString("birthday", aaBirthday[ii]);
    setString("stmt_cycle", aaStmtCycle[ii]);
    setString("act_no_f", aaActNof[ii]);
    setString("act_no_f_ind", aaActNofind[ii]);
    setString("ls_check_code", isCheckCode);
    setString("mod_user", wp.loginUser);
    setString("rowid", aaRowid[ii]);
//    usSql += sqlCol(aaModSeqno[ii], "mod_seqno");
    // System.out.println("ls_sql update crd_emap_tmp :" + ls_sql);
    sqlExec(usSql);
    if (sqlRowNum <= 0) {
      wp.colSet(ii, "err_msg", "Update crd_emap_tmp err");
      wp.colSet(ii, "ok_flag", "X");
      return -1;
    }
    return 1;
  }

  public int crdApscdinvProc(int ii) {
    // insert or update crd_apscdinv
    String lsSql = "", usSql = "", isSql = "", mRowid = "", mModSeqno = "";
    lsSql = "select hex(rowid) as rowid, mod_seqno from crd_apscdinv where 1=1 ";
    lsSql += sqlCol(aaApsBatchno[ii], "aps_batchno");
    lsSql += sqlCol(aaApsRecno[ii], "aps_recno");
    lsSql += sqlCol(aaApsSeqno[ii], "aps_seqno");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      mRowid = sqlStr("rowid");
      mModSeqno = sqlStr("mod_seqno");
      usSql = "update crd_apscdinv set " + "apply_id =:apply_id, "
          + "apply_id_code =:apply_id_code, " + "pm_id =:pm_id, " + "pm_id_code =:pm_id_code, "
          + "card_type =:card_type, " + "group_code =:group_code, " + "chi_name =:chi_name, "
          + "reject_code = '997', " + "mod_user =:mod_user, " + "mod_time = sysdate, "
          + "mod_pgm = 'crdm0065', " + "mod_seqno = nvl(mod_seqno,0)+1 where hex(rowid) = :rowid and mod_seqno = :mod_seqno";
      setString("apply_id", aaApplyId[ii]);
      setString("apply_id_code", aaApplyIdCode[ii]);
      setString("pm_id", aaPmId[ii]);
      setString("pm_id_code", aaPmIdCode[ii]);
      setString("card_type", aaCardType[ii]);
      setString("group_code", aaGroupCode[ii]);
      setString("chi_name", aaChiName[ii]);
      setString("mod_user", wp.loginUser);
      setString("rowid", mRowid);
      setString("mod_seqno", mModSeqno);
      sqlExec(usSql);
      if (mExProcFlag.equals("0")) {
        if (sqlRowNum <= 0) {
          wp.colSet(ii, "err_msg", "Update crd_apscdinv err");
          wp.colSet(ii, "ok_flag", "X");
          return -1;
        } else {
          wp.colSet(ii, "ok_flag", "V");
          sqlCommit(1);
        }
      } else {
        if (sqlRowNum <= 0) {
          wp.colSet(ii, "err_msg", "Update crd_apscdinv err");
          wp.colSet(ii, "ok_flag", "X");
          return -1;
        }
      }
    } else {
      isSql = "insert into crd_apscdinv ( "
          + "aps_batchno, aps_recno, aps_seqno, apply_id, apply_id_code, "
          + "pm_id, pm_id_code, card_type, group_code, chi_name, reject_code, "
          + "crt_date, mod_user, mod_time, mod_pgm, mod_seqno" + ") values ("
          + ":aps_batchno, :aps_recno, :aps_seqno, :apply_id, :apply_id_code, "
          + ":pm_id, :pm_id_code, :card_type, :group_code, :chi_name, '997',"
          + "to_char(sysdate,'yyyymmdd'), :mod_user, sysdate, 'crdm0065', 1)";
      setString("aps_batchno", aaApsBatchno[ii]);
      setString("aps_recno", aaApsRecno[ii]);
      setString("aps_seqno", aaApsSeqno[ii]);
      setString("apply_id", aaApplyId[ii]);
      setString("apply_id_code", aaApplyIdCode[ii]);
      setString("pm_id", aaPmId[ii]);
      setString("pm_id_code", aaPmIdCode[ii]);
      setString("card_type", aaCardType[ii]);
      setString("group_code", aaGroupCode[ii]);
      setString("chi_name", aaChiName[ii]);
      setString("mod_user", wp.loginUser);
      sqlExec(isSql);
      if (sqlRowNum <= 0) {
        wp.colSet(ii, "err_msg", "Insert crd_apscdinv err");
        wp.colSet(ii, "ok_flag", "X");
        return -1;
      }
    }
    return 1;
  }

  public int crdRejectProc(int ii) {
    String lsSql = "", usSql = "", isSql = "", mRowid = "", mModSeqno = "";
    lsSql = "select hex(rowid) as rowid, mod_seqno from crd_reject where 1=1 ";
    lsSql += sqlCol(aaBatchno[ii], "batchno");
    lsSql += sqlCol(aaRecno[ii], "recno");
    lsSql += sqlCol(aaApplyId[ii], "apply_id");
    lsSql += sqlCol(aaBirthday[ii], "birthday");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      mRowid = sqlStr("rowid");
      mModSeqno = sqlStr("mod_seqno");
      // update crd_reject
      usSql = "update crd_reject set " + "aps_batchno =:aps_batchno, " // 1
          + "emboss_source =:emboss_source, " + "aps_recno =:aps_recno, " + "seqno =:aps_recno, "
          + "card_type =:card_type, " + "card_no =:card_no, " + "major_valid_fm =:major_valid_fm, "
          + "major_valid_to =:major_valid_to, " + "apply_id_code =:apply_id_code, "
          + "pm_id =:pm_id, " + "pm_id_code =:pm_id_code, " // 11
          + "group_code =:group_code, " + "source_code =:source_code, " + "corp_no =:corp_no, "
          + "corp_act_flag =:corp_act_flag, " + "corp_assure_flag =:corp_assure_flag, "
          + "reg_bank_no =:reg_bank_no, " + "risk_bank_no =:risk_bank_no, "
          + "chi_name =:chi_name, " + "eng_name =:eng_name, " + "marriage =:marriage, " // 21
          + "service_year =:service_year, " + "education =:education, " + "nation =:nation, "
          + "salary =:salary, " + "mail_zip =:mail_zip, " + "mail_addr1 =:mail_addr1, "
          + "mail_addr2 =:mail_addr2, " + "mail_addr3 =:mail_addr3, " + "mail_addr4 =:mail_addr4, "
          + "mail_addr5 =:mail_addr5, " // 31
          + "resident_zip =:resident_zip, " + "resident_addr1 =:resident_addr1, "
          + "resident_addr2 =:resident_addr2, " + "resident_addr3 =:resident_addr3, "
          + "resident_addr4 =:resident_addr4, " + "resident_addr5 =:resident_addr5, "
          + "company_name =:company_name, " + "job_position =:job_position, "
          + "home_area_code1 =:home_area_code1, " + "home_tel_no1 =:home_tel_no1, " // 41
          + "home_tel_ext1 =:home_tel_ext1, " + "home_area_code2 =:home_area_code2, "
          + "home_tel_no2 =:home_tel_no2, " + "home_tel_ext2 =:home_tel_ext2, "
          + "office_area_code1 =:office_area_code1, " + "office_tel_no1 =:office_tel_no1, "
          + "office_tel_ext1 =:office_tel_ext1, " + "office_area_code2 =:office_area_code2, "
          + "office_tel_no2 =:office_tel_no2, " + "office_tel_ext2 =:office_tel_ext2, " // 51
          + "e_mail_addr =:e_mail_addr, " + "cellar_phone =:cellar_phone, " + "act_no =:act_no, "
          + "vip =:vip, " + "fee_code =:fee_code, " + "force_flag =:force_flag, "
          + "business_code =:business_code, " + "introduce_no =:introduce_no, "
          + "valid_fm =:valid_fm, " + "valid_to =:valid_to, " // 61
          + "sex =:sex, " + "value =:value, " + "accept_dm =:accept_dm, " + "apply_no =:apply_no, "
          + "cardcat =:cardcat, " + "mail_type =:mail_type, " + "introduce_id =:introduce_id, "
          + "introduce_name =:introduce_name, " + "salary_code =:salary_code, "
          + "student =:student, " // 71
          + "credit_lmt =:credit_lmt, " + "apply_id_ecode =:apply_id_ecode, "
          + "corp_no_ecode =:corp_no_ecode, " + "pm_id_ecode =:pm_id_ecode, "
          + "police_no1 =:police_no1, " + "police_no2 =:police_no2, " + "police_no3 =:police_no3, "
          + "pm_cash =:pm_cash, " + "sup_cash =:sup_cash, " + "online_mark =:online_mark, " // 81
          + "reject_code =:reject_code, " + "emboss_4th_data =:emboss_4th_data, "
          + "member_id =:member_id, " + "stmt_cycle =:stmt_cycle, " + "pm_birthday =:pm_birthday, "
          + "sup_birthday =:sup_birthday, " + "standard_fee =:standard_fee, "
          + "final_fee_code =:final_fee_code, " + "fee_reason_code =:fee_reason_code, "
          + "annual_fee =:annual_fee, " // 91
          + "service_code =:service_code, " + "crt_date =:crt_date, " + "ur_flag =:ur_flag, " // 新增
          + "e_news =:e_news, " + "spouse_id_no =:spouse_id_no, "
          + "spouse_birthday =:spouse_birthday, "
          + "resident_no_expire_date =:resident_no_expire_date, "
          + "passport_date =:passport_date, " + "roadside_assist_apply =:roadside_assist_apply, "
          + "bill_apply_flag =:bill_apply_flag, " + "revolve_int_rate =:revolve_int_rate, "
          + "special_card_rate =:special_card_rate, " + "company_zip =:company_zip, "
          + "autopay_acct_bank =:autopay_acct_bank, " + "inst_flag =:inst_flag, "
          + "fee_code_i =:fee_code_i, " + "graduation_elementarty =:graduation_elementarty, "
          + "spouse_name =:spouse_name, " + "company_addr1 =:company_addr1, "
          + "company_addr2 =:company_addr2, " + "company_addr3 =:company_addr3, "
          + "company_addr4 =:company_addr4, " + "company_addr5 =:company_addr5, "
          + "curr_change_accout =:curr_change_accout, " + "credit_level_new =:credit_level_new, "
          + "crt_user =:crt_user, " + "revolve_int_rate_year =:revolve_int_rate_year, "
          + "promote_emp_no =:promote_emp_no, " + "promote_dept =:promote_dept, "
          + "card_ref_num =:card_ref_num, " + "introduce_emp_no =:introduce_emp_no, "
          + "mod_user =:mod_user, " + "mod_time = sysdate, " + "mod_pgm ='crdm0065', "
          + "mod_seqno =nvl(mod_seqno,0)+1 where 1=1 and hex(rowid) = :rowid and mod_seqno = :mod_seqno ";
      setString("aps_batchno", aaApsBatchno[ii]);
      setString("emboss_source", aaSource[ii]);
      setString("aps_recno", aaApsRecno[ii]);
      setString("seqno", aaSeqno[ii]);
      setString("card_type", aaCardType[ii]);

      setString("card_no", aaCardNo[ii]);
      setString("major_valid_fm", aaMajorValidFm[ii]);
      setString("major_valid_to", aaMajorValidTo[ii]);
      setString("apply_id_code", aaApplyIdCode[ii]);
      setString("pm_id", aaPmId[ii]);
      // 11
      setString("pm_id_code", aaPmIdCode[ii]);
      setString("group_code", aaGroupCode[ii]);
      setString("source_code", aaSourceCode[ii]);
      setString("corp_no", aaCorpNo[ii]);
      setString("corp_act_flag", aaCorpActFlag[ii]);

      setString("corp_assure_flag", aaCorpAssureFlag[ii]);
      setString("reg_bank_no", aaRegBankNo[ii]);
      setString("risk_bank_no", aaRiskBankNo[ii]);
      setString("chi_name", aaChiName[ii]);
      setString("eng_name", aaEngName[ii]);
      // 21
      setString("marriage", aaMarriage[ii]);
      setString("service_year", aaServiceYear[ii]);
      setString("education", aaEducation[ii]);
      setString("nation", aaNation[ii]);
      setString("salary", aaSalary[ii]);

      setString("mail_zip", aaMailZip[ii]);
      setString("mail_addr1", aaMailAddr1[ii]);
      setString("mail_addr2", aaMailAddr2[ii]);
      setString("mail_addr3", aaMailAddr3[ii]);
      setString("mail_addr4", aaMailAddr4[ii]);
      // 31
      setString("mail_addr5", aaMailAddr5[ii]);
      setString("resident_zip", aaResidentZip[ii]);
      setString("resident_addr1", aaResidentAddr1[ii]);
      setString("resident_addr2", aaResidentAddr2[ii]);
      setString("resident_addr3", aaResidentAddr3[ii]);

      setString("resident_addr4", aaResidentAddr4[ii]);
      setString("resident_addr5", aaResidentAddr5[ii]);
      setString("company_name", aaCompanyName[ii]);
      setString("job_position", aaJobPosition[ii]);
      setString("home_area_code1", aaHomeAreaCode1[ii]);
      // 41
      setString("home_tel_no1", aaHomeTelNo1[ii]);
      setString("home_tel_ext1", aaHomeTelExt1[ii]);
      setString("home_area_code2", aaHomeAreaCode2[ii]);
      setString("home_tel_no2", aaHomeTelNo2[ii]);
      setString("home_tel_ext2", aaHomeTelExt2[ii]);

      setString("office_area_code1", aaOfficeAreaCode1[ii]);
      setString("office_tel_no1", aaOfficeTelNo1[ii]);
      setString("office_tel_ext1", aaOfficeTelExt1[ii]);
      setString("office_area_code2", aaOfficeAreaCode2[ii]);
      setString("office_tel_no2", aaOfficeTelNo2[ii]);
      // 51
      setString("office_tel_ext2", aaOfficeTelExt2[ii]);
      setString("e_mail_addr", aaEmailAddr[ii]);
      setString("cellar_phone", aaCellarPhone[ii]);
      setString("act_no", aaActNo[ii]);
      setString("vip", aaVip[ii]);

      setString("fee_code", aaFeeCode[ii]);
      setString("force_flag", aaForceFlag[ii]);
      setString("business_code", aaBusinessCode[ii]);
      setString("introduce_no", aaIntroduceNo[ii]);
      setString("valid_fm", aaValidFm[ii]);
      // 61
      setString("valid_to", aaValidTo[ii]);
      setString("sex", aaSex[ii]);
      setString("value", aaValue[ii]);
      setString("accept_dm", aaAcceptDm[ii]);
      setString("apply_no", aaApplyNo[ii]);

      setString("cardcat", aaCardcat[ii]);
      setString("mail_type", aaMailType[ii]);
      setString("introduce_id", aaIntroduceId[ii]);
      setString("introduce_name", aaIntroduceName[ii]);
      setString("salary_code", aaSalaryCode[ii]);
      // 71
      setString("student", aaStudent[ii]);
      setString("credit_lmt", aaCreditLmt[ii]);
      setString("apply_id_ecode", aaApplyIdEcode[ii]);
      setString("corp_no_ecode", aaCorpNoEcode[ii]);
      setString("pm_id_ecode", aaPmIdEcode[ii]);

      setString("police_no1", aaPoliceNo1[ii]);
      setString("police_no2", aaPoliceNo2[ii]);
      setString("police_no3", aaPoliceNo3[ii]);
      setString("pm_cash", aaPmCash[ii]);
      setString("sup_cash", aaSupCash[ii]);
      // 81
      setString("online_mark", aaOnlineMark[ii]);
      setString("reject_code", aaRejectCode[ii]);
      setString("emboss_4th_data", aaEmboss4thData[ii]);
      setString("member_id", aaMemberId[ii]);
      setString("stmt_cycle", aaStmtCycle[ii]);

      setString("pm_birthday", aaPmBirthday[ii]);
      setString("sup_birthday", aaSupBirthday[ii]);
      setString("standard_fee", aaStandardFee[ii]);
      setString("final_fee_code", aaFinalFeeCode[ii]);
      setString("fee_reason_code", aaFeeReasonCode[ii]);
      // 91
      setString("annual_fee", aaAnnualFee[ii]);
      setString("service_code", aaServiceCode[ii]);
      setString("crt_date", aaCrtDate[ii]);

      // 新增欄位
      setString("ur_flag", aaUrFlag[ii]);
      setString("e_news", aaENews[ii]);
      setString("spouse_id_no", aaSpouseIdNo[ii]);
      setString("spouse_birthday", aaSpouseBirthday[ii]);
      setString("resident_no_expire_date", aaResidentNoExpireDate[ii]);
      setString("passport_date", aaPassportDate[ii]);
      setString("roadside_assist_apply", aaRoadsideAssistApply[ii]);
      setString("bill_apply_flag", aaBillApplyFlag[ii]);
      setString("revolve_int_rate", aaRevolveIntRate[ii]);
      setString("special_card_rate", aaSpecialCardRate[ii]);
      setString("company_zip", aaCompanyZip[ii]);
      setString("autopay_acct_bank", aaAutopayAcctBank[ii]);
      setString("inst_flag", aaInstFlag[ii]);
      setString("fee_code_i", aaFeeCodei[ii]);
      setString("graduation_elementarty", aaGraduationElementarty[ii]);
      setString("spouse_name", aaSpouseName[ii]);
      setString("company_addr1", aaCompanyAddr1[ii]);
      setString("company_addr2", aaCompanyAddr2[ii]);
      setString("company_addr3", aaCompanyAddr3[ii]);
      setString("company_addr4", aaCompanyAddr4[ii]);
      setString("company_addr5", aaCompanyAddr5[ii]);
      setString("curr_change_accout", aaCurrChangeAccout[ii]);
      setString("credit_level_new", aaCreditLevelNew[ii]);
      setString("crt_user", aaCrtUser[ii]);
      setString("revolve_int_rate_year", aaRevolveIntRateYear[ii]);
      setString("promote_dept", aaPromoteDept[ii]);
      setString("promote_emp_no", aaPromoteEmpNo[ii]);
      setString("card_ref_num", aaCardRefNum[ii]);
      setString("introduce_emp_no", aaIntroduceEmpNo[ii]);

      setString("mod_user", wp.loginUser);
      setString("rowid", mRowid);
      setString("mod_seqno", mModSeqno);
      sqlExec(usSql);

      if (sqlRowNum <= 0) {
        wp.colSet(ii, "err_msg", "Update crd_reject err");
        wp.colSet(ii, "ok_flag", "X");
        return -1;
      }
    } else {
      // insert crd_reject
      isSql = "insert into crd_reject ("
          + "batchno,           recno,          apply_id,        birthday, "
          + "aps_batchno,       emboss_source,  aps_recno,       seqno,             card_type, "
          + "card_no,           major_valid_fm, major_valid_to,  apply_id_code,     pm_id, "
          + "pm_id_code, 		  group_code,     source_code,     corp_no,           corp_act_flag,  "
          + "corp_assure_flag,  reg_bank_no,    risk_bank_no,    chi_name,          eng_name, "
          + "marriage,          service_year,   education,       nation,            salary, "
          + "mail_zip,          mail_addr1,     mail_addr2,      mail_addr3,        mail_addr4, "
          + "mail_addr5,        resident_zip,   resident_addr1,  resident_addr2,    resident_addr3, "
          + "resident_addr4,    resident_addr5, company_name,    job_position,      home_area_code1, "
          + "home_tel_no1,      home_tel_ext1,  home_area_code2, home_tel_no2,      home_tel_ext2, "
          + "office_area_code1, office_tel_no1, office_tel_ext1, office_area_code2, office_tel_no2, "
          + "office_tel_ext2,   e_mail_addr,    cellar_phone,    act_no,            vip, "
          + "fee_code,          force_flag,     business_code,   introduce_no,      valid_fm, "
          + "valid_to,          sex,            value,           accept_dm,         apply_no, "
          + "cardcat,           mail_type,      introduce_id,    introduce_name,    salary_code, "
          + "student,           credit_lmt,     apply_id_ecode,  corp_no_ecode,     pm_id_ecode, "
          + "police_no1,        police_no2,     police_no3,      pm_cash,           sup_cash, "
          + "online_mark,       reject_code,    emboss_4th_data, member_id,         stmt_cycle, "
          + "pm_birthday,       sup_birthday,   standard_fee,    final_fee_code,    fee_reason_code, "
          + "annual_fee,        service_code,   crt_date,        ur_flag,           e_news, "
          + "spouse_id_no,      spouse_birthday,  resident_no_expire_date,  passport_date,  roadside_assist_apply, "
          + "bill_apply_flag,   revolve_int_rate, special_card_rate,        company_zip,    autopay_acct_bank, "
          + "inst_flag,         fee_code_i,       graduation_elementarty,   spouse_name,    company_addr1, "
          + "company_addr2,     company_addr3,    company_addr4,            company_addr5,  curr_change_accout, "
          + "credit_level_new,  crt_user,         revolve_int_rate_year,    promote_dept,   promote_emp_no, "
          + "card_ref_num,      introduce_emp_no, mod_user,                 mod_time,  "
          + "mod_pgm,           mod_seqno " + ")values ("
          + ":batchno,          :recno,           :apply_id,          :birthday,      "
          + ":aps_batchno,      :emboss_source,   :aps_recno,         :seqno,              :card_type, "
          + ":card_no,          :major_valid_fm,  :major_valid_to,    :apply_id_code,      :pm_id, "
          + ":pm_id_code, 	  :group_code,      :source_code,       :corp_no,            :corp_act_flag,  "
          + ":corp_assure_flag, :reg_bank_no,     :risk_bank_no,      :chi_name,           :eng_name, "
          + ":marriage,         :service_year,    :education,         :nation,             :salary, "
          + ":mail_zip,         :mail_addr1,      :mail_addr2,        :mail_addr3,         :mail_addr4, "
          + ":mail_addr5,       :resident_zip,    :resident_addr1,    :resident_addr2,     :resident_addr3, "
          + ":resident_addr4,   :resident_addr5,  :company_name,      :job_position,       :home_area_code1, "
          + ":home_tel_no1,     :home_tel_ext1,   :home_area_code2,   :home_tel_no2,       :home_tel_ext2, "
          + ":office_area_code1,:office_tel_no1,  :office_tel_ext1,   :office_area_code2,  :office_tel_no2, "
          + ":office_tel_ext2,  :e_mail_addr,     :cellar_phone,      :act_no,             :vip, "
          + ":fee_code,         :force_flag,      :business_code,     :introduce_no,       :valid_fm, "
          + ":valid_to,         :sex,             :value,             :accept_dm,          :apply_no, "
          + ":cardcat,          :mail_type,       :introduce_id,      :introduce_name,     :salary_code, "
          + ":student,          :credit_lmt,      :apply_id_ecode,    :corp_no_ecode,      :pm_id_ecode, "
          + ":police_no1,       :police_no2,      :police_no3,        :pm_cash,            :sup_cash, "
          + ":online_mark,      :reject_code,     :emboss_4th_data,   :member_id,          :stmt_cycle, "
          + ":pm_birthday,      :sup_birthday,    :standard_fee,      :final_fee_code,     :fee_reason_code, "
          + ":annual_fee,       :service_code,     to_char(sysdate,'yyyymmdd'), :ur_flag,       :e_news, "
          + ":spouse_id_no,     :spouse_birthday,  :resident_no_expire_date,    :passport_date,  :roadside_assist_apply, "
          + ":bill_apply_flag,  :revolve_int_rate, :special_card_rate,          :company_zip,    :autopay_acct_bank, "
          + ":inst_flag,        :fee_code_i,       :graduation_elementarty,     :spouse_name,    :company_addr1, "
          + ":company_addr2,    :company_addr3,    :company_addr4,              :company_addr5,  :curr_change_accout, "
          + ":credit_level_new, :crt_user,         :revolve_int_rate_year,      :promote_dept,   :promote_emp_no, "
          + ":card_ref_num,     :introduce_emp_no, :mod_user,                   sysdate,  "
          + "'crdm0065',        1 " + ")";
      setString("batchno", aaBatchno[ii]);
      setString("recno", aaRecno[ii]);
      setString("apply_id", aaApplyId[ii]);
      setString("birthday", aaBirthday[ii]);

      setString("aps_batchno", aaApsBatchno[ii]);
      setString("emboss_source", aaSource[ii]);
      setString("aps_recno", aaApsRecno[ii]);
      setString("seqno", aaSeqno[ii]);
      setString("card_type", aaCardType[ii]);

      setString("card_no", aaCardNo[ii]);
      setString("major_valid_fm", aaMajorValidFm[ii]);
      setString("major_valid_to", aaMajorValidTo[ii]);
      setString("apply_id_code", aaApplyIdCode[ii]);
      setString("pm_id", aaPmId[ii]);

      setString("pm_id_code", aaPmIdCode[ii]);
      setString("group_code", aaGroupCode[ii]);
      setString("source_code", aaSourceCode[ii]);
      setString("corp_no", aaCorpNo[ii]);
      setString("corp_act_flag", aaCorpActFlag[ii]);

      setString("corp_assure_flag", aaCorpAssureFlag[ii]);
      setString("reg_bank_no", aaRegBankNo[ii]);
      setString("risk_bank_no", aaRiskBankNo[ii]);
      setString("chi_name", aaChiName[ii]);
      setString("eng_name", aaEngName[ii]);

      setString("marriage", aaMarriage[ii]);
      setString("service_year", aaServiceYear[ii]);
      setString("education", aaEducation[ii]);
      setString("nation", aaNation[ii]);
      setString("salary", aaSalary[ii]);

      setString("mail_zip", aaMailZip[ii]);
      setString("mail_addr1", aaMailAddr1[ii]);
      setString("mail_addr2", aaMailAddr2[ii]);
      setString("mail_addr3", aaMailAddr3[ii]);
      setString("mail_addr4", aaMailAddr4[ii]);

      setString("mail_addr5", aaMailAddr5[ii]);
      setString("resident_zip", aaResidentZip[ii]);
      setString("resident_addr1", aaResidentAddr1[ii]);
      setString("resident_addr2", aaResidentAddr2[ii]);
      setString("resident_addr3", aaResidentAddr3[ii]);

      setString("resident_addr4", aaResidentAddr4[ii]);
      setString("resident_addr5", aaResidentAddr5[ii]);
      setString("company_name", aaCompanyName[ii]);
      setString("job_position", aaJobPosition[ii]);
      setString("home_area_code1", aaHomeAreaCode1[ii]);

      setString("home_tel_no1", aaHomeTelNo1[ii]);
      setString("home_tel_ext1", aaHomeTelExt1[ii]);
      setString("home_area_code2", aaHomeAreaCode2[ii]);
      setString("home_tel_no2", aaHomeTelNo2[ii]);
      setString("home_tel_ext2", aaHomeTelExt2[ii]);

      setString("office_area_code1", aaOfficeAreaCode1[ii]);
      setString("office_tel_no1", aaOfficeTelNo1[ii]);
      setString("office_tel_ext1", aaOfficeTelExt1[ii]);
      setString("office_area_code2", aaOfficeAreaCode2[ii]);
      setString("office_tel_no2", aaOfficeTelNo2[ii]);

      setString("office_tel_ext2", aaOfficeTelExt2[ii]);
      setString("e_mail_addr", aaEmailAddr[ii]);
      setString("cellar_phone", aaCellarPhone[ii]);
      setString("act_no", aaActNo[ii]);
      setString("vip", aaVip[ii]);

      setString("fee_code", aaFeeCode[ii]);
      setString("force_flag", aaForceFlag[ii]);
      setString("business_code", aaBusinessCode[ii]);
      setString("introduce_no", aaIntroduceNo[ii]);
      setString("valid_fm", aaValidFm[ii]);

      setString("valid_to", aaValidTo[ii]);
      setString("sex", aaSex[ii]);
      setString("value", aaValue[ii]);
      setString("accept_dm", aaAcceptDm[ii]);
      setString("apply_no", aaApplyNo[ii]);

      setString("cardcat", aaCardcat[ii]);
      setString("mail_type", aaMailType[ii]);
      setString("introduce_id", aaIntroduceId[ii]);
      setString("introduce_name", aaIntroduceName[ii]);
      setString("salary_code", aaSalaryCode[ii]);

      setString("student", aaStudent[ii]);
      setString("credit_lmt", aaCreditLmt[ii]);
      setString("apply_id_ecode", aaApplyIdEcode[ii]);
      setString("corp_no_ecode", aaCorpNoEcode[ii]);
      setString("pm_id_ecode", aaPmIdEcode[ii]);

      setString("police_no1", aaPoliceNo1[ii]);
      setString("police_no2", aaPoliceNo2[ii]);
      setString("police_no3", aaPoliceNo3[ii]);
      setString("pm_cash", aaPmCash[ii]);
      setString("sup_cash", aaSupCash[ii]);
      //
      setString("online_mark", aaOnlineMark[ii]);
      setString("reject_code", aaRejectCode[ii]);
      setString("emboss_4th_data", aaEmboss4thData[ii]);
      setString("member_id", aaMemberId[ii]);
      setString("stmt_cycle", aaStmtCycle[ii]);

      setString("pm_birthday", aaPmBirthday[ii]);
      setString("sup_birthday", aaSupBirthday[ii]);
      setString("standard_fee", aaStandardFee[ii]);
      setString("final_fee_code", aaFinalFeeCode[ii]);
      setString("fee_reason_code", aaFeeReasonCode[ii]);

      setString("annual_fee", aaAnnualFee[ii]);
      setString("service_code", aaServiceCode[ii]);
      // 新增欄位
      setString("ur_flag", aaUrFlag[ii]);
      setString("e_news", aaENews[ii]);
      setString("spouse_id_no", aaSpouseIdNo[ii]);
      setString("spouse_birthday", aaSpouseBirthday[ii]);
      setString("resident_no_expire_date", aaResidentNoExpireDate[ii]);
      setString("passport_date", aaPassportDate[ii]);
      setString("roadside_assist_apply", aaRoadsideAssistApply[ii]);
      setString("bill_apply_flag", aaBillApplyFlag[ii]);
      setString("revolve_int_rate", aaRevolveIntRate[ii]);
      setString("special_card_rate", aaSpecialCardRate[ii]);
      setString("company_zip", aaCompanyZip[ii]);
      setString("autopay_acct_bank", aaAutopayAcctBank[ii]);
      setString("inst_flag", aaInstFlag[ii]);
      setString("fee_code_i", aaFeeCodei[ii]);
      setString("graduation_elementarty", aaGraduationElementarty[ii]);
      setString("spouse_name", aaSpouseName[ii]);
      setString("company_addr1", aaCompanyAddr1[ii]);
      setString("company_addr2", aaCompanyAddr2[ii]);
      setString("company_addr3", aaCompanyAddr3[ii]);
      setString("company_addr4", aaCompanyAddr4[ii]);
      setString("company_addr5", aaCompanyAddr5[ii]);
      setString("curr_change_accout", aaCurrChangeAccout[ii]);
      setString("credit_level_new", aaCreditLevelNew[ii]);
      setString("crt_user", aaCrtUser[ii]);
      setString("revolve_int_rate_year", aaRevolveIntRateYear[ii]);
      setString("promote_dept", aaPromoteDept[ii]);
      setString("promote_emp_no", aaPromoteEmpNo[ii]);
      setString("card_ref_num", aaCardRefNum[ii]);
      setString("introduce_emp_no", aaIntroduceEmpNo[ii]);

      setString("mod_user", wp.loginUser);
      sqlExec(isSql);
      if (sqlRowNum <= 0) {
        wp.colSet(ii, "err_msg", "Insert crd_reject err");
        wp.colSet(ii, "ok_flag", "X");
        return -1;
      }
    }
    return 1;
  }

  public int updateCrdEmapTmp2(int ii) {
//    String dsSql = "";
//    dsSql = "delete crd_emap_tmp where hex(rowid) =:rowid and mod_seqno =:mod_seqno ";
//    setString("rowid", aaRowid[ii]);
//    setString("mod_seqno", aaModSeqno[ii]);
//    sqlExec(dsSql);
	  String usSql = "update crd_emap_tmp set reject_code = :check_code ,reject_date = to_char(sysdate,'yyyymmdd'),mod_user = :mod_user ,mod_time = sysdate ,mod_pgm = 'crdm0065' "
	  		+ " where 1=1 and hex(rowid) = :rowid and mod_seqno = :mod_seqno ";
	        setString("check_code", aaCheckCode[ii]);
		    setString("mod_user", wp.loginUser);
		    setString("rowid", aaRowid[ii]);
		    setString("mod_seqno", aaModSeqno[ii]);
		    sqlExec(usSql);
    if (sqlRowNum <= 0) {
      wp.colSet(ii, "err_msg", "update crd_emap_tmp err2");
      wp.colSet(ii, "ok_flag", "X");
      return -1;
    }
    return 1;
  }
}

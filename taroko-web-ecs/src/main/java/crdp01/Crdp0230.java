/**************c**************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-10  V1.00.00  yash       program initial                            *
* 109-02-26  V1.00.01  Wilson     修正where條件                                                                                           *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 112-03-09  V1.00.02  Wilson     updateCrdEmboss set reject_code = check_code*
* 112-07-13  V1.00.03  Wilson     mark update crd_proc_tmp                   *
******************************************************************************/

package crdp01;


import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import java.util.regex.Pattern;

public class Crdp0230 extends BaseProc {
  String apsBatchno = "", apsRecno = "", apsSeqno = "", applyId = "", applyIdCode = "", pmId = "",
      pmIdCode = "";
  String cardType = "", groupCode = "", chiName = "", rejectCode = "", embossSource = "",
      embossReason = "", supFlag = "", birthday = "", corpNo = "", corpNoCode = "", oldCardNo = "",
      cardNo = "";
  String lsMsg = "";
  String isBatchnoBoss = "", isBatchnoEmap = "";
  String[] aaBatchno;
  String[] aaRecno;
  String[] aaEmbossSource;
  String[] aaCardType;
  String[] aaChiName;
  String[] aaApsBatchno;
  String[] aaApsRecno;
  String[] aaSeqno;
  String[] aaApplyId;
  String[] aaApplyIdCode;
  String[] aaPmId;
  String[] aaPmIdCode;
  String[] aaGroupCode;
  String[] aaRejectCode;
  String[] aaEmbossReason;
  String[] aaSupFlag;
  String[] aaBirthday;
  String[] aaCorpNo;
  String[] aaCorpNoCode;
  String[] aaOldCardNo;
  String[] aaCardNo;
  String[] aaResendNote;
  String[] aaSourceBatchno;
  String[] aaSourceRecno;
  String[] aaToNcccCode;
  String[] aaAcctType;
  String[] aaClassCode;
  String[] aaUnitCode;
  String[] aaMajorValidFm;
  String[] aaMajorValidTo;
  String[] aaMajorChgFlag;
  String[] aaChangeReason;
  String[] aaStatusCode;
  String[] aaReasonCode;
  String[] aaSourceCode;
  String[] aaCorpActFlag;
  String[] aaOrpAssureFlag;
  String[] aaRegBankNo;
  String[] aaRiskBankNo;
  String[] aaEngName;
  String[] aaMarriage;
  String[] aaRelWithPm;
  String[] aaServiceYear;
  String[] aaEducation;
  String[] aaNation;
  String[] aaSalary;
  String[] aaMailZip;
  String[] aaMailDddr1;
  String[] aaMailArddr2;
  String[] aaMailAddr3;
  String[] aaMailAddr4;
  String[] aaMailAddr5;
  String[] aaResidentAip;
  String[] aaResidentAddr1;
  String[] aaResidentAddr2;
  String[] aaResidentAddr3;
  String[] aaResidentAddr4;
  String[] aaResidentAddr5;
  String[] aaCompanyName;
  String[] aaJobPosition;
  String[] aaHomeAreaCode1;
  String[] aaHomeTelNo1;
  String[] aaHomeTelExt1;
  String[] aaHomeAreaCode2;
  String[] aaHomeTelNo2;
  String[] aaHomeTelExt2;
  String[] aaOfficeAreaCode1;
  String[] aaOfficeTelNo1;
  String[] aaOfficeTelExt1;
  String[] aaOfficeAreaCode2;
  String[] aaOfficeTelNo2;
  String[] aaOfficeTelExt2;
  String[] aaEMailAddr;
  String[] aaCellarPhone;
  String[] aaActNo;
  String[] aaVip;
  String[] aaFeeCode;
  String[] aaForceFlag;
  String[] aaBusinessCode;
  String[] aaIntroduceNo;
  String[] aaValidFm;
  String[] aaValidTo;
  String[] aaSex;
  String[] aaValue;
  String[] aaAcceptDm;
  String[] aaApplyNo;
  String[] aaCardcat;
  String[] aaMailType;
  String[] aaIntroduceId;
  String[] aaIntroduceName;
  String[] aaSalaryCode;
  String[] aaStudent;
  String[] aaCreditLmt;
  String[] aaApplyIdEcode;
  String[] aaCorpNoEcode;
  String[] aaPmIdEcode;
  String[] aaPoliceNo1;
  String[] aaPoliceNo2;
  String[] aaPoliceNo3;
  String[] aaPmCash;
  String[] aaSupCash;
  String[] aaOnlineMark;
  String[] aaErrorCode;
  String[] aaEmboss4thData;
  String[] aaMemberId;
  String[] aaStmtCycle;
  String[] aaCreditFlag;
  String[] aaCommFlag;
  String[] aaResidentNo;
  String[] aaOtherCntryCode;
  String[] aaPassportNo;
  String[] aaStaffFlag;
  String[] aaPmBirthday;
  String[] aaSupBirthday;
  String[] aaStandardFee;
  String[] aaFinalFeeCode;
  String[] aaFeeReasonCode;
  String[] aaAnnualFee;
  String[] aaChgAddrFlag;
  String[] aaPvv;
  String[] aaCvv;
  String[] aaCvv2;
  String[] aaPvki;
  String[] aaServiceCode;
  String[] aaCntlAreaCode;
  String[] aaStockNo;
  String[] aaOldBegDate;
  String[] aaOldEndDate;
  String[] aaEmbossResult;
  String[] aaDiffCode;
  String[] aaCreditError;
  String[] aaAuthCreditLmt;
  String[] aaAprDate;
  String[] aaAprUser;
  String[] aaCompleteCode;
  String[] aaMailCode;
  String[] aaCrtDate;
  String[] aaSonCardFlag;
  String[] aaOrgIndivCrdLmt;
  String[] aaIndivCrdLmt;
  String[] aaNcccOthDate;
  String[] aaOrgEmbossData;
  String[] aaIcFlag;
  String[] aaBranch;
  String[] aaMailAttach1;
  String[] aaMailAttach2;
  String[] aaComboIndicator;
  String[] opt;
  String[] aaNcccType;
  String[] aaAcctKey;
  String[] aaMajorCardNo;
  String[] aaDcIndicator;
  String[] aaCurrCode;
  String[] aaActNoF;
  String[] aaActNoL;
  String[] aaActNoFInd;
  String[] aaAgreelInd;
  String[] aaActNolInd;
  String[] aaSendPwdFlag;
  String[] aaJcicScore;
  String[] aaModUser;
  String[] aaModTime;
  String[] aaModPgm;
  String[] aaModSeqno;
  String[] aElectronicCode;
  String[] aaMarketAgreeBase;
  String[] aaMarketAgreeAct;
  String[] aaVacationCode;
  String[] aaFancyLimitFlag;
  String[] aaStatSendInternet;
  String[] aaCheckCode;
  int rr = 0;

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

  // for query use only
  private boolean getWhereStr() throws Exception {

    wp.whereStr = " where 1=1 " + " and decode(c.in_main_error,'','0',c.in_main_error) != '0'  "
        + " and c.in_main_date = ''   "
        + " and  decode(c.group_code,'','0000',c.group_code)  = p.group_code ";

    if (empty(wp.itemStr("ex_batchno")) == false) {
      wp.whereStr += " and  c.batchno = :ex_batchno ";
      setString("ex_batchno", wp.itemStr("ex_batchno"));
    }

    if (empty(wp.itemStr("ex_emboss_source")) == false) {
      wp.whereStr += " and  c.emboss_source = :ex_emboss_source ";
      setString("ex_emboss_source", wp.itemStr("ex_emboss_source"));
    }

    if (empty(wp.itemStr("ex_apply_id")) == false) {
      wp.whereStr += " and  c.apply_id = :ex_apply_id ";
      setString("ex_apply_id", wp.itemStr("ex_apply_id"));
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

    wp.selectSQL = " c.batchno  " + ",c.batchno||'-'||recno as batchnorno " + ",c.recno "
        + ",decode(c.emboss_source,'1','1:新製卡'" + "                       ,'2','2:普昇金卡'"
        + "                       ,'3','3:整批續卡'" + "                       ,'4','4:提前續卡'"
        + "                       ,'5','5:重製'" + "                       ,'7','7:緊急補發卡'"
        + "                       ,c.emboss_source) as embossdesc " + ",c.emboss_source "
        + ",decode(c.emboss_reason,'1','1:掛失'" + "                       ,'2','2:毀損'"
        + "                       ,'3','3:偽卡'" + "                       ,'5','5:星座卡毀損重製'"
        + "                       ,c.emboss_reason) as emboss_reasondesc "

        + ",c.emboss_reason " + ",c.in_main_msg " + ",c.sup_flag " + ",c.card_no "
        + ",c.corp_no_code" + ",c.old_card_no" + ",c.apply_id||'-'||c.apply_id_code as applyid "
        + ",c.apply_id" + ",c.apply_id_code " + ",c.corp_no " + ",c.card_type " + ",c.chi_name "
        + ",c.birthday " + ",c.ic_flag " + ",p.combo_indicator " + ",c.aps_batchno "
        + ",c.aps_recno" + ",c.seqno" + ",c.apply_id" + ",c.apply_id_code" + ",c.pm_id"
        + ",c.pm_id_code" + ",c.group_code" + ",c.reject_code" + ",c.mod_user" + ",c.mod_time"
        + ",c.mod_pgm" + ",c.mod_pgm as mod_pgm2 " + ",c.mod_seqno" + ",c.resend_note"
        + ",c.source_batchno" + ",c.source_recno" + ",c.to_nccc_code" + ",c.acct_type"
        + ",c.class_code" + ",c.unit_code" + ",c.major_valid_fm" + ",c.major_valid_to"
        + ",c.major_chg_flag" + ",c.change_reason" + ",c.status_code" + ",c.reason_code"
        + ",c.source_code" + ",c.corp_act_flag" + ",c.corp_assure_flag" + ",c.reg_bank_no"
        + ",c.risk_bank_no" + ",c.eng_name" + ",c.marriage" + ",c.rel_with_pm" + ",c.service_year"
        + ",c.education" + ",c.nation" + ",c.salary" + ",c.mail_zip" + ",c.mail_addr1"
        + ",c.mail_addr2" + ",c.mail_addr3" + ",c.mail_addr4" + ",c.mail_addr5" + ",c.resident_zip"
        + ",c.resident_addr1" + ",c.resident_addr2" + ",c.resident_addr3" + ",c.resident_addr4"
        + ",c.resident_addr5" + ",c.company_name" + ",c.job_position" + ",c.home_area_code1"
        + ",c.home_tel_no1" + ",c.home_tel_ext1" + ",c.home_area_code2" + ",c.home_tel_no2"
        + ",c.home_tel_ext2" + ",c.office_area_code1" + ",c.office_tel_no1" + ",c.office_tel_ext1"
        + ",c.office_area_code2" + ",c.office_tel_no2" + ",c.office_tel_ext2" + ",c.e_mail_addr"
        + ",c.cellar_phone" + ",c.act_no" + ",c.vip" + ",c.fee_code" + ",c.force_flag"
        + ",c.business_code" + ",c.introduce_no" + ",c.valid_fm" + ",c.valid_to" + ",c.sex"
        + ",c.value" + ",c.accept_dm" + ",c.apply_no" + ",c.cardcat" + ",c.mail_type"
        + ",c.introduce_id" + ",c.introduce_name" + ",c.salary_code" + ",c.student"
        + ",c.credit_lmt" + ",c.apply_id_ecode" + ",c.corp_no_ecode" + ",c.pm_id_ecode"
        + ",c.police_no1" + ",c.police_no2" + ",c.police_no3" + ",c.pm_cash" + ",c.sup_cash"
        + ",c.online_mark" + ",c.error_code" + ",c.emboss_4th_data" + ",c.member_id"
        + ",c.stmt_cycle" + ",c.credit_flag" + ",c.comm_flag" + ",c.resident_no"
        + ",c.other_cntry_code" + ",c.passport_no" + ",c.staff_flag" + ",c.pm_birthday"
        + ",c.sup_birthday" + ",c.standard_fee" + ",c.final_fee_code" + ",c.fee_reason_code"
        + ",c.annual_fee" + ",c.chg_addr_flag" + ",c.pvv" + ",c.cvv" + ",c.cvv2" + ",c.pvki"
        + ",c.service_code" + ",c.cntl_area_code" + ",c.stock_no" + ",c.old_beg_date"
        + ",c.old_end_date" + ",c.emboss_result" + ",c.diff_code" + ",c.credit_error"
        + ",c.auth_credit_lmt" + ",c.apr_date" + ",c.apr_user" + ",c.complete_code" + ",c.mail_code"
        + ",c.crt_date" + ",c.son_card_flag" + ",c.org_indiv_crd_lmt" + ",c.indiv_crd_lmt"
        + ",c.nccc_oth_date" + ",c.org_emboss_data" + ",c.ic_flag" + ",c.branch" + ",c.mail_attach1"
        + ",c.mail_attach2" + ",hex(c.rowid) as rowid"
        // crd_emboss_tmp insert用 20190513 up
        + ",c.nccc_type" + ",c.acct_key" + ",c.major_card_no" + ",c.dc_indicator" + ",c.curr_code"
        + ",c.act_no_f" + ",c.act_no_l" + ",c.act_no_f_ind" + ",c.agree_l_ind" + ",c.act_no_l_ind"
        + ",c.send_pwd_flag" + ",c.jcic_score" + ",c.electronic_code" + ",c.market_agree_base"
        + ",c.market_agree_act" + ",c.vacation_code" + ",c.fancy_limit_flag"
        + ",c.stat_send_internet" + ",c.check_code";

    wp.daoTable = "crd_emboss c,ptr_group_code p";
    wp.whereOrder = " order by c.batchno";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    for (int i = 0; i < wp.selectCnt; i++) {
      wp.colSet(i, "opt", "0");
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
    opt = wp.itemBuff("opt");
    aaBatchno = wp.itemBuff("batchno");
    aaRecno = wp.itemBuff("recno");
    aaEmbossSource = wp.itemBuff("emboss_source");
    aaCardType = wp.itemBuff("card_type");
    aaChiName = wp.itemBuff("chi_name");
    aaApsBatchno = wp.itemBuff("aps_batchno");
    aaApsRecno = wp.itemBuff("aps_recno");
    aaSeqno = wp.itemBuff("seqno");
    aaApplyId = wp.itemBuff("apply_id");
    aaApplyIdCode = wp.itemBuff("apply_id_code");
    aaPmId = wp.itemBuff("pm_id");
    aaPmIdCode = wp.itemBuff("pm_id_code");
    aaGroupCode = wp.itemBuff("group_code");
    aaRejectCode = wp.itemBuff("reject_code");
    aaEmbossReason = wp.itemBuff("emboss_reason");
    aaSupFlag = wp.itemBuff("sup_flag");
    aaBirthday = wp.itemBuff("birthday");
    aaCorpNo = wp.itemBuff("corp_no");
    aaCorpNoCode = wp.itemBuff("corp_no_code");
    aaOldCardNo = wp.itemBuff("old_card_no");
    aaCardNo = wp.itemBuff("card_no");
    aaResendNote = wp.itemBuff("resend_note");
    aaSourceBatchno = wp.itemBuff("source_batchno");
    aaSourceRecno = wp.itemBuff("source_recno");
    aaToNcccCode = wp.itemBuff("to_nccc_code");
    aaAcctType = wp.itemBuff("acct_type");
    aaClassCode = wp.itemBuff("class_code");
    aaUnitCode = wp.itemBuff("unit_code");
    aaMajorValidFm = wp.itemBuff("major_valid_fm");
    aaMajorValidTo = wp.itemBuff("major_valid_to");
    aaMajorChgFlag = wp.itemBuff("major_chg_flag");
    aaChangeReason = wp.itemBuff("change_reason");
    aaStatusCode = wp.itemBuff("status_code");
    aaReasonCode = wp.itemBuff("reason_code");
    aaSourceCode = wp.itemBuff("source_code");
    aaCorpActFlag = wp.itemBuff("corp_act_flag");
    aaOrpAssureFlag = wp.itemBuff("corp_assure_flag");
    aaRegBankNo = wp.itemBuff("reg_bank_no");
    aaRiskBankNo = wp.itemBuff("risk_bank_no");
    aaEngName = wp.itemBuff("eng_name");
    aaMarriage = wp.itemBuff("marriage");
    aaRelWithPm = wp.itemBuff("rel_with_pm");
    aaServiceYear = wp.itemBuff("service_year");
    aaEducation = wp.itemBuff("education");
    aaNation = wp.itemBuff("nation");
    aaSalary = wp.itemBuff("salary");
    aaMailZip = wp.itemBuff("mail_zip");
    aaMailDddr1 = wp.itemBuff("mail_addr1");
    aaMailArddr2 = wp.itemBuff("mail_addr2");
    aaMailAddr3 = wp.itemBuff("mail_addr3");
    aaMailAddr4 = wp.itemBuff("mail_addr4");
    aaMailAddr5 = wp.itemBuff("mail_addr5");
    aaResidentAip = wp.itemBuff("resident_zip");
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
    aaEMailAddr = wp.itemBuff("e_mail_addr");
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
    aaErrorCode = wp.itemBuff("error_code");
    aaEmboss4thData = wp.itemBuff("emboss_4th_data");
    aaMemberId = wp.itemBuff("member_id");
    aaStmtCycle = wp.itemBuff("stmt_cycle");
    aaCreditFlag = wp.itemBuff("credit_flag");
    aaCommFlag = wp.itemBuff("comm_flag");
    aaResidentNo = wp.itemBuff("resident_no");
    aaOtherCntryCode = wp.itemBuff("other_cntry_code");
    aaPassportNo = wp.itemBuff("passport_no");
    aaStaffFlag = wp.itemBuff("staff_flag");
    aaPmBirthday = wp.itemBuff("pm_birthday");
    aaSupBirthday = wp.itemBuff("sup_birthday");
    aaStandardFee = wp.itemBuff("standard_fee");
    aaFinalFeeCode = wp.itemBuff("final_fee_code");
    aaFeeReasonCode = wp.itemBuff("fee_reason_code");
    aaAnnualFee = wp.itemBuff("annual_fee");
    aaChgAddrFlag = wp.itemBuff("chg_addr_flag");
    aaPvv = wp.itemBuff("pvv");
    aaCvv = wp.itemBuff("cvv");
    aaCvv2 = wp.itemBuff("cvv2");
    aaPvki = wp.itemBuff("pvki");
    aaServiceCode = wp.itemBuff("service_code");
    aaCntlAreaCode = wp.itemBuff("cntl_area_code");
    aaStockNo = wp.itemBuff("stock_no");
    aaOldBegDate = wp.itemBuff("old_beg_date");
    aaOldEndDate = wp.itemBuff("old_end_date");
    aaEmbossResult = wp.itemBuff("emboss_result");
    aaDiffCode = wp.itemBuff("diff_code");
    aaCreditError = wp.itemBuff("credit_error");
    aaAuthCreditLmt = wp.itemBuff("auth_credit_lmt");
    aaAprDate = wp.itemBuff("apr_date");
    aaAprUser = wp.itemBuff("apr_user");
    aaCompleteCode = wp.itemBuff("complete_code");
    aaMailCode = wp.itemBuff("mail_code");
    aaCrtDate = wp.itemBuff("crt_date");
    aaSonCardFlag = wp.itemBuff("son_card_flag");
    aaOrgIndivCrdLmt = wp.itemBuff("org_indiv_crd_lmt");
    aaIndivCrdLmt = wp.itemBuff("indiv_crd_lmt");
    aaNcccOthDate = wp.itemBuff("nccc_oth_date");
    aaOrgEmbossData = wp.itemBuff("org_emboss_data");
    aaIcFlag = wp.itemBuff("ic_flag");
    aaBranch = wp.itemBuff("branch");
    aaMailAttach1 = wp.itemBuff("mail_attach1");
    aaMailAttach2 = wp.itemBuff("mail_attach2");
    aaComboIndicator = wp.itemBuff("combo_indicator");
    aaNcccType = wp.itemBuff("nccc_type");
    aaAcctKey = wp.itemBuff("acct_key");
    aaMajorCardNo = wp.itemBuff("major_card_no");
    aaDcIndicator = wp.itemBuff("dc_indicator");
    aaCurrCode = wp.itemBuff("curr_code");
    aaActNoF = wp.itemBuff("act_no_f");
    aaActNoL = wp.itemBuff("act_no_l");
    aaActNoFInd = wp.itemBuff("act_no_f_ind");
    aaAgreelInd = wp.itemBuff("agree_l_ind");
    aaActNolInd = wp.itemBuff("act_no_l_ind");
    aaSendPwdFlag = wp.itemBuff("send_pwd_flag");
    aaJcicScore = wp.itemBuff("jcic_score");
    aaModUser = wp.itemBuff("mod_user");
    aaModTime = wp.itemBuff("mod_time");
    aaModPgm = wp.itemBuff("mod_pgm2");
    aaModSeqno = wp.itemBuff("mod_seqno");
    aElectronicCode = wp.itemBuff("electronic_code");
    aaMarketAgreeBase = wp.itemBuff("market_agree_base");
    aaMarketAgreeAct = wp.itemBuff("market_agree_act");
    aaVacationCode = wp.itemBuff("vacation_code");
    aaFancyLimitFlag = wp.itemBuff("fancy_limit_flag");
    aaStatSendInternet = wp.itemBuff("stat_send_internet");
    aaCheckCode = wp.itemBuff("check_code");
    wp.listCount[0] = aaBatchno.length;
    // save
    apsBatchno = aaApsBatchno[rr];
    apsRecno = aaApsRecno[rr];
    apsSeqno = aaSeqno[rr];
    applyId = aaApplyId[rr];
    applyIdCode = aaApplyIdCode[rr];
    pmId = aaPmId[rr];
    pmIdCode = aaPmIdCode[rr];
    cardType = aaCardType[rr];
    groupCode = aaGroupCode[rr];
    chiName = aaChiName[rr];
    rejectCode = aaRejectCode[rr];
    embossSource = aaEmbossSource[rr];
    embossReason = aaEmbossReason[rr];
    supFlag = aaSupFlag[rr];
    birthday = aaBirthday[rr];
    corpNo = aaCorpNo[rr];
    corpNoCode = aaCorpNoCode[rr];
    oldCardNo = aaOldCardNo[rr];
    cardNo = aaCardNo[rr];
    int llOk = 0, llErr = 0;
    for (rr = 0; rr < aaBatchno.length; rr++) {
      if (opt[rr].equals("0")) {
        continue;
      }
      switch (opt[rr]) {
        case "1":
          if (aaEmbossSource[rr].equals("1")) {
            if (wfAddApscdinv() < 0) {
              llErr++;
              wp.colSet(rr, "ok_flag", "X");
              wp.colSet(rr, "ls_errmsg", "wf_add_apscdinv err !");
              sqlCommit(0);
              continue;
            }

          }

          if (aaEmbossSource[rr].equals("5") || aaEmbossSource[rr].equals("7")) {

            if (wfAddApscard(rr) < 0) {
              llErr++;
              wp.colSet(rr, "ok_flag", "X");
              sqlCommit(0);
              continue;
            }

          }

          if (!aaComboIndicator[rr].equals("N")) {
            if (wfUpdateCombo() < 0) {
              llErr++;
              wp.colSet(rr, "ok_flag", "X");
              wp.colSet(rr, "ls_errmsg", "寫入combo卡失敗!");
              sqlCommit(0);
              continue;
            }
          }

          // wf_add_reject
          String batchno = "";
          int liRecno = 0;

          String lsSql =
              "select   max(batchno) as bano  from crd_reject where batchno like :batchno and apr_date ='' ";
          setString("batchno", getSysDate().substring(2) + "%");
          sqlSelect(lsSql);

          batchno = sqlStr("bano");

          if (empty(batchno)) {
            batchno = getSysDate().substring(2) + "01";
          } else {

            String lsSql2 =
                "select   max(recno)+1 as bano1  from crd_reject where batchno = :batchno  ";
            setString("batchno", batchno);
            sqlSelect(lsSql2);
            liRecno = sqlInt("bano1");
          }

          if (liRecno == 0) {
            liRecno = 1 ;
          }

          busi.SqlPrepare sprej = new SqlPrepare();
          sprej.sql2Insert("crd_reject");
          sprej.ppstr("batchno", batchno);
          sprej.ppint("recno", liRecno);
          sprej.ppstr("resend_note", aaResendNote[rr]);
          sprej.ppstr("acct_type", aaAcctType[rr]);
          // acct_key
          sprej.ppstr("emboss_source", aaEmbossSource[rr]);
          sprej.ppstr("source_batchno", aaSourceBatchno[rr]);
          sprej.ppstr("source_recno", aaSourceRecno[rr]);
          sprej.ppstr("aps_batchno", aaApsBatchno[rr]);
          sprej.ppnum("aps_recno", toNum(aaApsRecno[rr]));
          sprej.ppint("seqno", toInt(aaSeqno[rr]));
          sprej.ppstr("to_nccc_code", aaToNcccCode[rr]);
          sprej.ppstr("card_type", aaCardType[rr]);
          sprej.ppstr("class_code", aaClassCode[rr]);
          sprej.ppstr("unit_code", aaUnitCode[rr]);
          sprej.ppstr("card_no", aaCardNo[rr]);
          sprej.ppstr("major_valid_fm", aaMajorValidFm[rr]);
          sprej.ppstr("major_valid_to", aaMajorValidTo[rr]);
          sprej.ppstr("major_chg_flag", aaMajorChgFlag[rr]);
          sprej.ppstr("old_card_no", aaOldCardNo[rr]);
          sprej.ppstr("change_reason", aaChangeReason[rr]);
          sprej.ppstr("status_code", aaStatusCode[rr]);
          sprej.ppstr("reason_code", aaReasonCode[rr]);
          // member_note
          sprej.ppstr("apply_id", aaApplyId[rr]);
          sprej.ppstr("apply_id_code", aaApplyIdCode[rr]);
          sprej.ppstr("pm_id", aaPmId[rr]);
          sprej.ppstr("pm_id_code", aaPmIdCode[rr]);
          sprej.ppstr("group_code", aaGroupCode[rr]);
          sprej.ppstr("source_code", aaSourceCode[rr]);
          sprej.ppstr("corp_no", aaCorpNo[rr]);
          // corp_no_code
          sprej.ppstr("corp_act_flag", aaCorpActFlag[rr]);
          sprej.ppstr("corp_assure_flag", aaOrpAssureFlag[rr]);
          sprej.ppstr("reg_bank_no", aaRegBankNo[rr]);
          sprej.ppstr("risk_bank_no", aaRiskBankNo[rr]);
          sprej.ppstr("chi_name", aaChiName[rr]);
          sprej.ppstr("eng_name", aaEngName[rr]);
          sprej.ppstr("birthday", aaBirthday[rr]);
          sprej.ppstr("marriage", aaMarriage[rr]);
          sprej.ppstr("rel_with_pm", aaRelWithPm[rr]);
          sprej.ppint("service_year", toInt(aaServiceYear[rr]));
          sprej.ppstr("education", aaEducation[rr]);
          sprej.ppstr("nation", aaNation[rr]);
          sprej.ppnum("salary", toNum(aaSalary[rr]));
          sprej.ppstr("mail_zip", aaMailZip[rr]);
          sprej.ppstr("mail_addr1", aaMailDddr1[rr]);
          sprej.ppstr("mail_addr2", aaMailArddr2[rr]);
          sprej.ppstr("mail_addr3", aaMailAddr3[rr]);
          sprej.ppstr("mail_addr4", aaMailAddr4[rr]);
          sprej.ppstr("mail_addr5", aaMailAddr5[rr]);
          sprej.ppstr("resident_zip", aaResidentAip[rr]);
          sprej.ppstr("resident_addr1", aaResidentAddr1[rr]);
          sprej.ppstr("resident_addr2", aaResidentAddr2[rr]);
          sprej.ppstr("resident_addr3", aaResidentAddr3[rr]);
          sprej.ppstr("resident_addr4", aaResidentAddr4[rr]);
          sprej.ppstr("resident_addr5", aaResidentAddr5[rr]);
          sprej.ppstr("company_name", aaCompanyName[rr]);
          sprej.ppstr("job_position", aaJobPosition[rr]);
          sprej.ppstr("home_area_code1", aaHomeAreaCode1[rr]);
          sprej.ppstr("home_tel_no1", aaHomeTelNo1[rr]);
          sprej.ppstr("home_tel_ext1", aaHomeTelExt1[rr]);
          sprej.ppstr("home_area_code2", aaHomeAreaCode2[rr]);
          sprej.ppstr("home_tel_no2", aaHomeTelNo2[rr]);
          sprej.ppstr("home_tel_ext2", aaHomeTelExt2[rr]);
          sprej.ppstr("office_area_code1", aaOfficeAreaCode1[rr]);
          sprej.ppstr("office_tel_no1", aaOfficeTelNo1[rr]);
          sprej.ppstr("office_tel_ext1", aaOfficeTelExt1[rr]);
          sprej.ppstr("office_area_code2", aaOfficeAreaCode2[rr]);
          sprej.ppstr("office_tel_no2", aaOfficeTelNo2[rr]);
          sprej.ppstr("office_tel_ext2", aaOfficeTelExt2[rr]);
          // bb_call
          sprej.ppstr("e_mail_addr", aaEMailAddr[rr]);
          sprej.ppstr("cellar_phone", aaCellarPhone[rr]);
          sprej.ppstr("act_no", aaActNo[rr]);
          sprej.ppstr("vip", aaVip[rr]);
          sprej.ppstr("fee_code", aaFeeCode[rr]);
          sprej.ppstr("force_flag", aaForceFlag[rr]);
          sprej.ppstr("business_code", aaBusinessCode[rr]);
          sprej.ppstr("introduce_no", aaIntroduceNo[rr]);
          sprej.ppstr("valid_fm", aaValidFm[rr]);
          sprej.ppstr("valid_to", aaValidTo[rr]);
          sprej.ppstr("sex", aaSex[rr]);
          sprej.ppnum("value", toNum(aaValue[rr]));
          sprej.ppstr("accept_dm", aaAcceptDm[rr]);
          sprej.ppstr("apply_no", aaApplyNo[rr]);
          sprej.ppstr("cardcat", aaCardcat[rr]);
          sprej.ppstr("mail_type", aaMailType[rr]);
          sprej.ppstr("introduce_id", aaIntroduceId[rr]);
          sprej.ppstr("introduce_name", aaIntroduceName[rr]);
          sprej.ppstr("salary_code", aaSalaryCode[rr]);
          sprej.ppstr("student", aaStudent[rr]);
          sprej.ppint("credit_lmt", toInt(aaCreditLmt[rr]));
          sprej.ppstr("apply_id_ecode", aaApplyIdEcode[rr]);
          sprej.ppstr("corp_no_ecode", aaCorpNoEcode[rr]);
          sprej.ppstr("pm_id_ecode", aaPmIdEcode[rr]);
          sprej.ppstr("police_no1", aaPoliceNo1[rr]);
          sprej.ppstr("police_no2", aaPoliceNo2[rr]);
          sprej.ppstr("police_no3", aaPoliceNo3[rr]);
          sprej.ppstr("pm_cash", aaPmCash[rr]);
          sprej.ppstr("sup_cash", aaSupCash[rr]);
          sprej.ppstr("online_mark", aaOnlineMark[rr]);
          sprej.ppstr("error_code", aaErrorCode[rr]);
          sprej.ppstr("reject_code", aaRejectCode[rr]);
          sprej.ppstr("emboss_4th_data", aaEmboss4thData[rr]);
          sprej.ppstr("member_id", aaMemberId[rr]);
          sprej.ppstr("pm_birthday", aaPmBirthday[rr]);
          sprej.ppstr("sup_birthday", aaSupBirthday[rr]);
          sprej.ppint("standard_fee", toInt(aaStandardFee[rr]));
          sprej.ppstr("final_fee_code", aaFinalFeeCode[rr]);
          sprej.ppstr("fee_reason_code", aaFeeReasonCode[rr]);
          sprej.ppint("annual_fee", toInt(aaAnnualFee[rr]));
          sprej.ppstr("stmt_cycle", aaStmtCycle[rr]);
          sprej.ppstr("chg_addr_flag", aaChgAddrFlag[rr]);
          sprej.ppstr("pvv", aaPvv[rr]);
          sprej.ppstr("cvv", aaCvv[rr]);
          sprej.ppstr("cvv2", aaCvv2[rr]);
          sprej.ppstr("pvki", aaPvki[rr]);
          sprej.ppstr("service_code", aaServiceCode[rr]);
          sprej.ppstr("cntl_area_code", aaCntlAreaCode[rr]);
          sprej.ppstr("stock_no", aaStockNo[rr]);
          sprej.ppstr("old_beg_date", aaOldBegDate[rr]);
          sprej.ppstr("old_end_date", aaOldEndDate[rr]);
          sprej.ppstr("emboss_result", aaEmbossResult[rr]);
          sprej.ppstr("diff_code", aaDiffCode[rr]);
          sprej.ppstr("credit_error", aaCreditError[rr]);
          sprej.ppint("auth_credit_lmt", toInt(aaAuthCreditLmt[rr]));
          sprej.ppstr("apr_date", aaAprDate[rr]);
          sprej.ppstr("apr_user", aaAprUser[rr]);
          sprej.ppstr("complete_code", aaCompleteCode[rr]);
          sprej.ppstr("mail_code", aaMailCode[rr]);
          sprej.ppstr("ic_flag", aaIcFlag[rr]);
          sprej.ppstr("branch", aaBranch[rr]);
          sprej.ppstr("mail_attach1", aaMailAttach1[rr]);
          sprej.ppstr("mail_attach2", aaMailAttach2[rr]);
          sprej.ppstr("crt_date", getSysDate());
          sprej.ppstr("fail_proc_code", "1");
          // sprej.ppss("in_maim_date",get_sysDate());
          sprej.addsql(", mod_time ", ", sysdate ");
          sprej.ppstr("mod_user", wp.loginUser);
          sprej.ppstr("mod_pgm", wp.modPgm());
          sprej.ppnum("mod_seqno", 1);
          sqlExec(sprej.sqlStmt(), sprej.sqlParm());

          if (sqlRowNum <= 0) {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "ls_errmsg", "wf_add_reject err !");
            sqlCommit(0);
            continue;
          }

          if (wfUpdateCard(rr) < 0) {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            sqlCommit(0);
            continue;
          }
          if (updateCrdEmboss(rr,aaRowid[rr]) < 0) {
              llErr++;
	          wp.colSet(rr, "ok_flag", "X");
	          wp.colSet(rr, "ls_errmsg", "update crd_emboss err !");
	          sqlCommit(0);
	          continue;
          }
          break;
        case "2":
          if (this.toNum(aaEmbossSource[rr]) > 2) {
            if (wfAddEmbossTmp() != 1) {
              sqlCommit(0);
              llErr++;
              wp.colSet(rr, "ok_flag", "X");
              continue;
            }
          } else {
            if (wfAddEmapTmp() != 1) {
              sqlCommit(0);
              llErr++;
              wp.colSet(rr, "ok_flag", "X");
              continue;
            }
          }
          if (!aaComboIndicator[rr].equals("N")) {
            if (wfUpdateCombo() != 1) {
              sqlCommit(0);
              llErr++;
              wp.colSet(rr, "ok_flag", "X");
              continue;
            }
          }
      }
      // -- 寫入CRD_EMBOSS
      String usDsq = " update crd_emboss set " + "  fail_proc_code=:fail_proc_code "
          + " ,in_main_date=:in_main_date "
          + " where  hex(rowid) = :rowid  and mod_seqno = :mod_seqno ";
      setString("fail_proc_code", "1");
      setString("in_main_date", getSysDate());
      setString("rowid", aaRowid[rr]);
      setString("mod_seqno", aaModSeqno[rr]);
      sqlExec(usDsq);
      if (sqlRowNum <= 0) {
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "ls_errmsg", "update crd_emboss err !");
        sqlCommit(0);
        continue;
      }
      llOk++;
      wp.colSet(rr, "ok_flag", "V");
      sqlCommit(1);
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

  public int wfAddApscdinv() throws Exception {

    busi.SqlPrepare spi = new SqlPrepare();
    spi.sql2Insert("crd_apscdinv");
    spi.ppstr("APS_BATCHNO", apsBatchno);
    spi.ppnum("APS_RECNO", toNum(apsRecno));
    spi.ppstr("APS_SEQNO", apsSeqno);
    spi.ppstr("APPLY_ID", applyId);
    spi.ppstr("APPLY_ID_CODE", applyIdCode);
    spi.ppstr("PM_ID", pmId);
    spi.ppstr("PM_ID_CODE", pmIdCode);
    spi.ppstr("CARD_TYPE", cardType);
    spi.ppstr("GROUP_CODE", groupCode);
    spi.ppstr("CHI_NAME", chiName);
    spi.ppstr("REJECT_CODE", rejectCode);
    spi.ppstr("CRT_DATE", getSysDate());
    spi.ppstr("MOD_USER", wp.loginUser);
    spi.addsql(", mod_time ", ", sysdate ");
    spi.ppstr("mod_pgm", wp.modPgm());
    spi.ppnum("mod_seqno", 1);
    sqlExec(spi.sqlStmt(), spi.sqlParm());

    if (sqlRowNum <= 0) {
      return -1;
    }
    return 1;

  }


  public int wfAddApscard(int rr) throws Exception {
    String hBirthday = "";
    String hChiName = "";
    String hMailType = "";
    String hMailBranch = "";
    String hMailNo = "";
    String hMailProcDate = "";
    String hCurrentCode = "";
    wp.dateTime();

    if (embossSource.equals("5")) {
      if (embossReason.equals("2")) {
        return 1;
      }
    }

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("crd_apscard");
    sp.ppstr("crt_datetime", wp.sysDate + wp.sysTime);
    sp.ppstr("card_no", cardNo);


    if (supFlag.equals("1")) {
      String lsSql2 =
          "select  birthday,chi_name from crd_idno where id_no =:id_no and id_no_code =:id_no_code ";
      setString("id_no", pmId);
      setString("id_no_code", pmIdCode);
      sqlSelect(lsSql2);
      if (sqlRowNum <= 0) {
        wp.colSet(rr, "ls_errmsg", "抓取不到正卡卡人資料!");
        return -1;
      }
      hBirthday = sqlStr("birthday");
      hChiName = sqlStr("chi_name");


      sp.ppstr("sup_id", applyId);
      sp.ppstr("sup_id_code", applyIdCode);
      sp.ppstr("sup_birthday", birthday);
      sp.ppstr("pm_id", pmId);
      sp.ppstr("pm_id_code", pmIdCode);
      sp.ppstr("pm_birthday", hBirthday);
      sp.ppstr("pm_name", hChiName);
      sp.ppstr("sup_lost_status", "0");

    } else {


      sp.ppstr("pm_id", pmId);
      sp.ppstr("pm_id_code", pmIdCode);
      sp.ppstr("pm_birthday", birthday);
      sp.ppstr("pm_name", chiName);

    }


    sp.ppstr("corp_no", corpNo);
    sp.ppstr("corp_no_code", corpNoCode);
    sp.ppstr("group_code", groupCode);
    sp.ppstr("card_type", cardType);


    String lsSql3 =
        "select   mail_type,mail_branch,mail_no,mail_proc_date,current_code from crd_card where  card_no=:card_no ";
    setString("card_no", oldCardNo);
    sqlSelect(lsSql3);
    if (sqlRowNum <= 0) {
      wp.colSet(rr, "ls_errmsg", "抓取不到卡片資料!");
      return -1;
    }

    hMailType = sqlStr("mail_type");
    hMailBranch = sqlStr("mail_branch");
    hMailNo = sqlStr("mail_no");
    hMailProcDate = sqlStr("mail_proc_date");
    hCurrentCode = sqlStr("current_code");

    sp.ppstr("mail_type", hMailType);
    sp.ppstr("mail_branch", hMailBranch);
    sp.ppstr("mail_no", hMailNo);
    sp.ppstr("mail_date", hMailProcDate);
    sp.ppstr("stop_date", getSysDate());

    switch (hCurrentCode) {

      case "1":
        sp.ppstr("stop_reason", "3");
        break;

      case "2":
        sp.ppstr("stop_reason", "2");
        break;

      case "3":

        sp.ppstr("stop_reason", "1");
        break;

      case "4":

        sp.ppstr("stop_reason", "3");
        break;

      case "5":

        sp.ppstr("stop_reason", "5");
        break;

    }

    if (embossSource.equals("5")) {
      if (embossReason.equals("1")) {
        sp.ppstr("status_code", "11");
      } else if (embossReason.equals("3")) {
        sp.ppstr("status_code", "10");
      }
    }

    if (embossSource.equals("7")) {
      sp.ppstr("status_code", "11");

    }


    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sqlExec(sp.sqlStmt(), sp.sqlParm());


    if (sqlRowNum <= 0) {
      return -1;
    }
    return 1;

  }

  public int wfUpdateCombo() throws Exception {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_combo");
    sp.ppstr("emboss_code", "1");
    sp.ppstr("emboss_date", getSysDate());
    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where card_no=?", cardNo);
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      alertErr("update crd_combo err");
      return -1;
    }
    return 1;

  }


  public int wfUpdateCard(int rr) throws Exception {
    // --新製卡失敗不需寫入主檔
    if (embossSource.equals("1")) {
      return 1;
    }

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card");


    if (embossSource.equals("2")) {
      sp.ppstr("upgrade_status", "4");
      sp.ppstr("upgrade_date", getSysDate());
    }


    if (embossSource.equals("3") || embossSource.equals("4")) {
      sp.ppstr("change_status", "4");
      sp.ppstr("change_date", getSysDate());
    }

    if (embossSource.equals("5")) {
      sp.ppstr("reissue_status", "4");
      sp.ppstr("reissue_date", getSysDate());
    }

    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where card_no=?", oldCardNo);
    sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
      wp.colSet(rr, "ls_errmsg", "寫入卡檔錯誤~");
      return -1;
    }
    return 1;

  }
  
  int updateCrdEmboss(int rr ,String aaRowid) {
		String upSql = "update crd_emboss set reject_code = :check_code ,reject_date = to_char(sysdate,'yyyymmdd'),mod_user = :mod_user ,mod_time = sysdate ,mod_pgm = 'crdp0230' ";
		upSql += " where  hex(rowid) = :rowid  and mod_seqno = :mod_seqno ";
		setString("check_code", aaCheckCode[rr]);
		setString("mod_user", wp.loginUser);
		setString("rowid", aaRowid);
		setString("mod_seqno", aaModSeqno[rr]);
		sqlExec(upSql);
		if (sqlRowNum <= 0) {
			return -1;
		}
	  return 1;
  }

  int wfAddEmbossTmp() {
    String liRecno = "", lsComboIndicator = "", lsGroupCode = "";

    if (empty(isBatchnoBoss)) {
      if (wfGetBatchno(2) != 1) {
        return -1;
      }
    }
    if (empty(isBatchnoBoss)) {
      alertErr("無法取得 重製卡批號");
      return -1;
    }

    String sql1 =
        "select max(recno)+1 as li_recno from crd_emboss_tmp where batchno = :is_batchno_boss";
    setString("is_batchno_boss", isBatchnoBoss);
    sqlSelect(sql1);
    liRecno = sqlStr("li_recno");
    if (empty(liRecno) || liRecno.equals("0")) {
      liRecno = "1";
    }
    // --Insert crd_emboss_tmp
    busi.SqlPrepare sp1 = new SqlPrepare();
    sp1.sql2Insert("crd_emboss_tmp");
    sp1.ppstr("batchno", isBatchnoBoss);
    sp1.ppnum("recno", toNum(liRecno));
    sp1.ppstr("emboss_source", aaEmbossSource[rr]);
    sp1.ppstr("emboss_reason", aaEmbossReason[rr]);
    sp1.ppstr("risk_bank_no", aaRiskBankNo[rr]);
    sp1.ppstr("sup_flag", aaSupFlag[rr]);
    sp1.ppstr("reg_bank_no", aaRegBankNo[rr]);
    sp1.ppstr("resend_note", "Y");
    sp1.ppstr("nccc_type", aaNcccType[rr]);
    sp1.ppstr("to_nccc_code", aaToNcccCode[rr]);
    sp1.ppstr("acct_type", aaAcctType[rr]);
    sp1.ppstr("acct_key", aaAcctKey[rr]);
    sp1.ppstr("card_type", aaCardType[rr]);
    sp1.ppstr("unit_code", aaUnitCode[rr]);
    sp1.ppstr("major_card_no", aaMajorCardNo[rr]);
    sp1.ppstr("major_valid_fm", aaMajorValidFm[rr]);
    sp1.ppstr("major_valid_to", aaMajorValidTo[rr]);
    sp1.ppstr("old_card_no", aaOldCardNo[rr]);
    sp1.ppstr("card_no", aaCardNo[rr]);
    sp1.ppstr("status_code", aaStatusCode[rr]);
    sp1.ppstr("reason_code", aaReasonCode[rr]);
    // sp1.ppss("member_note", aa_member_note[rr]);
    sp1.ppstr("apply_id", aaApplyId[rr]);
    sp1.ppstr("apply_id_code", aaApplyIdCode[rr]);
    sp1.ppstr("pm_id", aaPmId[rr]);
    sp1.ppstr("pm_id_code", aaPmIdCode[rr]);
    sp1.ppstr("group_code", aaGroupCode[rr]);
    lsGroupCode = aaGroupCode[rr];
    if (empty(lsGroupCode)) {
      lsGroupCode = "0000";
    }
    String sql2 =
        "select combo_indicator as ls_combo_indicator from ptr_group_code where group_code = :ls_group_code";
    setString("ls_group_code", lsGroupCode);
    sqlSelect(sql2);
    lsComboIndicator = sqlStr("ls_combo_indicator");
    if (sqlRowNum <= 0) {
      alertErr("抓取ptr_group_code檔失敗',sqlca.sqlcode");
      return -1;
    }
    sp1.ppstr("combo_indicator", lsComboIndicator);
    sp1.ppstr("corp_no", aaCorpNo[rr]);
    sp1.ppstr("corp_no_code", aaCorpNoCode[rr]);
    sp1.ppstr("chi_name", aaChiName[rr]);
    sp1.ppstr("eng_name", aaEngName[rr]);
    sp1.ppstr("birthday", aaBirthday[rr]);
    sp1.ppstr("fee_code", aaFeeCode[rr]);
    sp1.ppstr("force_flag", aaForceFlag[rr]);
    sp1.ppstr("business_code", aaBusinessCode[rr]);
    sp1.ppstr("valid_fm", aaValidFm[rr]);
    sp1.ppstr("valid_to", aaValidTo[rr]);
    sp1.ppnum("credit_lmt", toNum(aaCreditLmt[rr]));
    sp1.ppstr("emboss_4th_data", aaEmboss4thData[rr]);
    sp1.ppstr("member_id", aaMemberId[rr]);
    sp1.ppnum("standard_fee", toNum(aaStandardFee[rr]));
    sp1.ppstr("fee_reason_code", aaFeeReasonCode[rr]);
    sp1.ppnum("annual_fee", toNum(aaAnnualFee[rr]));
    sp1.ppstr("chg_addr_flag", aaChgAddrFlag[rr]);
    sp1.ppstr("old_beg_date", aaOldBegDate[rr]);
    sp1.ppstr("old_end_date", aaOldEndDate[rr]);
    sp1.ppstr("mail_type", aaMailType[rr]);
    sp1.ppstr("ic_flag", aaIcFlag[rr]);
    sp1.ppstr("branch", aaBranch[rr]);
    sp1.ppstr("mail_attach1", aaMailAttach1[rr]);
    sp1.ppstr("mail_attach2", aaMailAttach2[rr]);
    sp1.ppstr("dc_indicator", aaDcIndicator[rr]);
    sp1.ppstr("curr_code", aaCurrCode[rr]);
    sp1.ppstr("act_no_f", aaActNoF[rr]);
    sp1.ppstr("act_no_l", aaActNoL[rr]);
    sp1.ppstr("act_no_f_ind", aaActNoFInd[rr]);
    sp1.ppstr("agree_l_ind", aaAgreelInd[rr]);
    sp1.ppstr("act_no_l_ind", aaActNolInd[rr]);
    sp1.ppstr("send_pwd_flag", aaSendPwdFlag[rr]);
    sp1.ppnum("jcic_score", toNum(aaJcicScore[rr]));
    sp1.ppstr("crt_date", getSysDate());
    sp1.ppstr("mod_user", aaModUser[rr]);
    sp1.ppstr("mod_time", aaModTime[rr]);
    sp1.ppstr("mod_pgm", aaModPgm[rr]);
    sp1.ppnum("mod_seqno", toNum(aaModSeqno[rr]));
    sp1.ppstr("electronic_code", aElectronicCode[rr]);
    sp1.ppstr("cardcat", aaCardcat[rr]);
    sp1.ppstr("son_card_flag", aaSonCardFlag[rr]);
    sp1.ppstr("stat_send_internet", aaStatSendInternet[rr]);

    String sqlSelect = "select bin_no from crd_seqno_log where bin_no||seqno = :crd_no ";
    setString("crd_no", aaCardNo[rr]);
    sqlSelect(sqlSelect);
    String binNo = sqlStr("bin_no");
    sp1.ppstr("bin_no", binNo);
    sqlExec(sp1.sqlStmt(), sp1.sqlParm());
    if (sqlRowNum <= 0) {
      alertErr("update crd_emboss_tmp err");
      return -1;
    }
    // -- 寫入NEW BATCHN,RECNO 到crd_proc_tmp-------------------------------------------
//    busi.SqlPrepare sp2 = new SqlPrepare();
//    sp2.sql2Update("crd_proc_tmp");
//    sp2.ppstr("proc_date", getSysDate());
//    sp2.ppstr("new_tbl", "2");
//    sp2.ppstr("resend_note", "Y");
//    sp2.ppstr("new_batchno", isBatchnoBoss);
//    sp2.ppnum("new_recno", toNum(liRecno));
//    sp2.ppstr("apr_date", getSysDate());
//    sp2.ppstr("apr_user", wp.loginUser);
//    sp2.ppstr("mod_user", wp.loginUser);
//    sp2.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time =sysdate");
//    sp2.ppstr("mod_pgm", wp.modPgm());
//    sp2.sql2Where(" where batchno=?", aaBatchno[rr]);
//    sp2.sql2Where(" and recno=?", toInt(aaRecno[rr]));
//    sqlExec(sp2.sqlStmt(), sp2.sqlParm());
//    if (sqlRowNum <= 0) {
//      alertErr("update crd_proc_tmp err");
//      return -1;
//    }
    return 1;
  }

  int wfAddEmapTmp() {
    String liRecno = "";

    if (empty(isBatchnoEmap)) {
      if (wfGetBatchno(1) != 1) {
        return -1;
      }
    }
    if (empty(isBatchnoEmap)) {
      alertErr("無法取得重製卡批號 (crd_emap_tmp)");
      return -1;
    }
    // --seq-no---------------
    String sql1 = "select max(recno)+1 as li_recno from crd_emap_tmp where  batchno = :batchno ";
    setString("batchno", isBatchnoEmap);
    sqlSelect(sql1);
    liRecno = sqlStr("li_recno");
    if (empty(liRecno) && liRecno.equals("0")) {
      liRecno = "1";
    }
    busi.SqlPrepare sp1 = new SqlPrepare();
    sp1.sql2Insert("crd_emap_tmp");
    sp1.ppstr("batchno", isBatchnoEmap);
    sp1.ppnum("recno", toNum(liRecno));
    sp1.ppstr("source", aaEmbossSource[rr]);
    // sp1.ppss("resend_note", "Y");
    sp1.ppstr("acct_type", aaAcctType[rr]);
    sp1.ppstr("acct_key", aaAcctKey[rr]);
    sp1.ppstr("nccc_type", aaNcccType[rr]);
    sp1.ppstr("aps_batchno", aaApsBatchno[rr]);
    sp1.ppnum("aps_recno", toNum(aaApsRecno[rr]));
    sp1.ppnum("seqno", toNum(wp.colStr(rr, "seqno")));
    sp1.ppstr("check_code", "0");
    sp1.ppstr("oth_chk_code", "0");
    sp1.ppstr("card_type", aaCardType[rr]);
    sp1.ppstr("class_code", aaClassCode[rr]);
    sp1.ppstr("unit_code", aaUnitCode[rr]);
    sp1.ppstr("major_valid_fm", aaMajorValidFm[rr]);
    sp1.ppstr("major_valid_to", aaMajorValidTo[rr]);
    sp1.ppstr("major_chg_flag", aaMajorChgFlag[rr]);
    // sp1.ppss("member_note", aa_member_note[rr]);
    sp1.ppstr("apply_id", aaApplyId[rr]);
    sp1.ppstr("apply_id_code", aaApplyIdCode[rr]);
    sp1.ppstr("pm_id", aaPmId[rr]);
    sp1.ppstr("pm_id_code", aaPmIdCode[rr]);
    sp1.ppstr("group_code", aaGroupCode[rr]);
    sp1.ppstr("source_code", aaSourceCode[rr]);
    sp1.ppstr("old_card_no", aaOldCardNo[rr]);
    sp1.ppstr("card_no", aaCardNo[rr]);
    sp1.ppstr("corp_no", aaCorpNo[rr]);
    sp1.ppstr("corp_no_code", aaCorpNoCode[rr]);
    sp1.ppstr("corp_act_flag", aaCorpActFlag[rr]);
    sp1.ppstr("corp_assure_flag", aaOrpAssureFlag[rr]);
    sp1.ppstr("reg_bank_no", aaRegBankNo[rr]);
    sp1.ppstr("risk_bank_no", aaRiskBankNo[rr]);
    sp1.ppstr("chi_name", aaChiName[rr]);
    sp1.ppstr("eng_name", aaEngName[rr]);
    sp1.ppstr("birthday", wp.colStr(rr, "birthday"));
    sp1.ppstr("marriage", aaMarriage[rr]);
    sp1.ppstr("rel_with_pm", aaRelWithPm[rr]);
    sp1.ppnum("service_year", toNum(aaServiceYear[rr]));
    sp1.ppstr("education", aaEducation[rr]);
    sp1.ppstr("nation", aaNation[rr]);
    sp1.ppnum("salary", toNum(aaSalary[rr]));
    sp1.ppstr("mail_zip", aaMailZip[rr]);
    sp1.ppstr("mail_addr1", aaMailDddr1[rr]);
    sp1.ppstr("mail_addr2", aaMailArddr2[rr]);
    sp1.ppstr("mail_addr3", aaMailAddr3[rr]);
    sp1.ppstr("mail_addr4", aaMailAddr4[rr]);
    sp1.ppstr("mail_addr5", aaMailAddr5[rr]);
    sp1.ppstr("resident_zip", aaResidentAip[rr]);
    sp1.ppstr("resident_addr1", aaResidentAddr1[rr]);
    sp1.ppstr("resident_addr2", aaResidentAddr2[rr]);
    sp1.ppstr("resident_addr3", aaResidentAddr3[rr]);
    sp1.ppstr("resident_addr4", aaResidentAddr4[rr]);
    sp1.ppstr("resident_addr5", aaResidentAddr5[rr]);
    sp1.ppstr("company_name", aaCompanyName[rr]);
    sp1.ppstr("job_position", aaJobPosition[rr]);
    sp1.ppstr("home_area_code1", aaHomeAreaCode1[rr]);
    sp1.ppstr("home_tel_no1", aaHomeTelNo1[rr]);
    sp1.ppstr("home_tel_ext1", aaHomeTelExt1[rr]);
    sp1.ppstr("home_area_code2", aaHomeAreaCode2[rr]);
    sp1.ppstr("home_tel_no2", aaHomeTelNo2[rr]);
    sp1.ppstr("home_tel_ext2", aaHomeTelExt2[rr]);
    sp1.ppstr("office_area_code1", aaOfficeAreaCode1[rr]);
    sp1.ppstr("office_tel_no1", aaOfficeTelNo1[rr]);
    sp1.ppstr("office_tel_ext1", aaOfficeTelExt1[rr]);
    sp1.ppstr("office_area_code2", aaOfficeAreaCode2[rr]);
    sp1.ppstr("office_tel_no2", aaOfficeTelNo2[rr]);
    sp1.ppstr("office_tel_ext2", aaOfficeTelExt2[rr]);
    sp1.ppstr("e_mail_addr", aaEMailAddr[rr]);
    sp1.ppstr("cellar_phone", aaCellarPhone[rr]);
    sp1.ppstr("act_no", aaActNo[rr]);
    sp1.ppstr("vip", aaVip[rr]);
    sp1.ppstr("fee_code", aaFeeCode[rr]);
    sp1.ppstr("force_flag", aaForceFlag[rr]);
    sp1.ppstr("business_code", aaBusinessCode[rr]);
    sp1.ppstr("introduce_no", aaIntroduceNo[rr]);
    sp1.ppstr("valid_fm", aaValidFm[rr]);
    sp1.ppstr("valid_to", aaValidTo[rr]);
    sp1.ppstr("sex", aaSex[rr]);
    sp1.ppnum("value", toNum(aaValue[rr]));
    sp1.ppstr("accept_dm", aaAcceptDm[rr]);
    sp1.ppstr("apply_no", aaApplyNo[rr]);
    sp1.ppstr("mail_type", aaMailType[rr]);
    sp1.ppstr("introduce_id", aaIntroduceId[rr]);
    sp1.ppstr("introduce_name", aaIntroduceName[rr]);
    sp1.ppstr("salary_code", aaSalaryCode[rr]);
    sp1.ppstr("student", aaStudent[rr]);
    sp1.ppnum("credit_lmt", toNum(aaCreditLmt[rr]));
    sp1.ppstr("police_no1", aaPoliceNo1[rr]);
    sp1.ppstr("police_no2", aaPoliceNo2[rr]);
    sp1.ppstr("police_no3", aaPoliceNo3[rr]);
    sp1.ppstr("pm_cash", aaPmCash[rr]);
    sp1.ppstr("sup_cash", aaSupCash[rr]);
    sp1.ppstr("online_mark", aaOnlineMark[rr]);
    sp1.ppstr("emboss_4th_data", aaEmboss4thData[rr]);
    sp1.ppstr("member_id", aaMemberId[rr]);
    sp1.ppstr("pm_birthday", aaPmBirthday[rr]);
    sp1.ppstr("sup_birthday", aaSupBirthday[rr]);
    sp1.ppnum("standard_fee", toNum(aaStandardFee[rr]));
    sp1.ppstr("final_fee_code", aaFinalFeeCode[rr]);
    sp1.ppstr("fee_reason_code", aaFeeReasonCode[rr]);
    sp1.ppnum("annual_fee", toNum(aaAnnualFee[rr]));
    sp1.ppstr("stmt_cycle", aaStmtCycle[rr]);
    sp1.ppstr("pvv", wp.colStr(rr, "pvv"));
    sp1.ppstr("cvv", wp.colStr(rr, "cvv"));
    sp1.ppstr("cvv2", wp.colStr(rr, "cvv2"));
    sp1.ppstr("pvki", wp.colStr(rr, "pvki"));
    sp1.ppstr("service_code", aaServiceCode[rr]);
    sp1.ppstr("credit_flag", aaCreditFlag[rr]);
    sp1.ppstr("comm_flag", aaCommFlag[rr]);
    sp1.ppstr("ic_flag", aaIcFlag[rr]);
    sp1.ppstr("branch", aaBranch[rr]);
    sp1.ppstr("mail_attach1", aaMailAttach1[rr]);
    sp1.ppstr("mail_attach2", aaMailAttach2[rr]);
    sp1.ppstr("contactor1_name", wp.colStr(rr, "contactor1_name"));
    sp1.ppstr("contactor1_relation", wp.colStr(rr, "contactor1_relation"));
    sp1.ppstr("contactor1_area_code", wp.colStr(rr, "contactor1_area_code"));
    sp1.ppstr("contactor1_tel", wp.colStr(rr, "contactor1_tel"));
    sp1.ppstr("contactor1_ext", wp.colStr(rr, "contactor1_ext"));
    sp1.ppstr("contactor2_name", wp.colStr(rr, "contactor2_name"));
    sp1.ppstr("contactor2_relation", wp.colStr(rr, "contactor2_relation"));
    sp1.ppstr("contactor2_area_code", wp.colStr(rr, "contactor2_area_code"));
    sp1.ppstr("contactor2_tel", wp.colStr(rr, "contactor2_tel"));
    sp1.ppstr("contactor2_ext", wp.colStr(rr, "contactor2_ext"));
    sp1.ppstr("dc_indicator", aaDcIndicator[rr]);
    sp1.ppstr("curr_code", aaCurrCode[rr]);
    sp1.ppstr("act_no_f", aaActNoF[rr]);
    sp1.ppstr("act_no_l", aaActNoL[rr]);
    sp1.ppstr("act_no_f_ind", aaActNoFInd[rr]);
    sp1.ppstr("agree_l_ind", aaAgreelInd[rr]);
    sp1.ppstr("act_no_l_ind", aaActNolInd[rr]);
    sp1.ppstr("send_pwd_flag", aaSendPwdFlag[rr]);
    sp1.ppnum("jcic_score", toNum(aaJcicScore[rr]));
    sp1.ppstr("crt_date", getSysDate());
    sp1.ppstr("mod_user", aaModUser[rr]);
    sp1.ppstr("mod_time", aaModTime[rr]);
    sp1.ppstr("mod_pgm", aaModPgm[rr]);
    sp1.ppnum("mod_seqno", toNum(aaModSeqno[rr]));
    sp1.ppstr("cardcat", aaCardcat[rr]);
    sp1.ppstr("electronic_code", aElectronicCode[rr]);
    sp1.ppstr("staff_flag", aaStaffFlag[rr]);
    sp1.ppstr("son_card_flag", aaSonCardFlag[rr]);
    sp1.ppstr("market_agree_base", aaMarketAgreeBase[rr]);
    sp1.ppstr("market_agree_act", aaMarketAgreeAct[rr]);
    sp1.ppstr("vacation_code", aaVacationCode[rr]);
    sp1.ppstr("fancy_limit_flag", aaFancyLimitFlag[rr]);
    sp1.ppstr("stat_send_internet", aaStatSendInternet[rr]);
    String sqlSelect = "select bin_no from crd_seqno_log where bin_no||seqno = :crd_no ";
    setString("crd_no", aaCardNo[rr]);
    sqlSelect(sqlSelect);
    String binNo = sqlStr("bin_no");
    sp1.ppstr("bin_no", binNo);
    sqlExec(sp1.sqlStmt(), sp1.sqlParm());
    if (sqlRowNum <= 0) {
      alertErr("update crd_emap_tmp err");
      return -1;
    }
    // -- 寫入NEW BATCHN,RECNO 到crd_proc_tmp
//    busi.SqlPrepare sp2 = new SqlPrepare();
//    sp2.sql2Update("crd_proc_tmp");
//    sp2.ppstr("proc_date", getSysDate());
//    sp2.ppstr("new_tbl", "1");
//    sp2.ppstr("new_batchno", isBatchnoBoss);
//    sp2.ppnum("new_recno", toNum(liRecno));
//    sp2.ppstr("apr_date", getSysDate());
//    sp2.ppstr("apr_user", wp.loginUser);
//    sp2.ppstr("mod_user", wp.loginUser);
//    sp2.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time =sysdate");
//    sp2.ppstr("mod_pgm", wp.modPgm());
//    sp2.sql2Where(" where batchno=?", aaBatchno[rr]);
//    sp2.sql2Where(" and recno=?", toInt(aaRecno[rr]));
//    sqlExec(sp2.sqlStmt(), sp2.sqlParm());
//    if (sqlRowNum <= 0) {
//      alertErr("update crd_proc_tmp err");
//      return -1;
//    }
    return 1;
  }

  int wfGetBatchno(int aiType) {// --get 批號&序號
    Pattern pattern = Pattern.compile("[0-9]*");
    String lsBno1 = "", lsBno2 = "", lsSysdate = "";
    int liMaxno = 0;
    lsSysdate = strMid(getSysDate(), 2, 6);
    lsBno1 = lsSysdate + "%";
    switch (aiType) {
      case 1:
        // --crd_emap_tmp batch-no---------------------------------
        String sql1 =
            "select batchno from crd_emap_tmp where apr_date = '' and batchno like :batchno ";
        setString("batchno", lsBno1);
        sqlSelect(sql1);
        lsBno2 = sqlStr("batchno");
        if (empty(lsBno2) || !pattern.matcher(lsBno2).matches()) {
          String sql2 = "select batchno from crd_emap_tmp where  batchno like :batchno ";
          setString("batchno", lsBno1);
          sqlSelect(sql2);
          lsBno2 = sqlStr("batchno");
        }
        if (empty(lsBno2) || !pattern.matcher(lsBno2).matches()) {
          lsBno2 = lsSysdate + "01";
        } else {
          int i = lsBno2.length();
          liMaxno = (int) this.toNum(strMid(lsBno2, i - 2, 2));
          if (liMaxno >= 99) {
            alertErr("當天送製卡批號已達 99, 不可再產生(crd_emap_tmp)");
            return -1;
          }
          if (lsBno2.length() < 8) {
            lsBno2 = String.format("%08d", lsBno2 + 1);
          }
        }
        isBatchnoEmap = lsBno2;

        break;
      case 2:
        // --get crd_emboss_tmp---------------------------------------
        String sql3 =
            "select max(batchno) as batchno from crd_emboss_tmp where apr_date = '' and batchno like :ls_bno1";
        setString("ls_bno1", lsBno1);
        sqlSelect(sql3);
        lsBno2 = sqlStr("batchno");
        if (empty(lsBno2) || !pattern.matcher(lsBno2).matches()) {
          String sql4 = "select batchno from crd_emboss_tmp where  batchno like :ls_bno1 ";
          setString("ls_bno1", lsBno1);
          sqlSelect(sql4);
          lsBno2 = sqlStr("batchno");
        }
        if (empty(lsBno2) || pattern.matcher(lsBno2).matches()) {
          lsBno2 = lsSysdate + "01";
        } else {
          int i = lsBno2.length();
          liMaxno = (int) this.toNum(strMid(lsBno2, i - 2, 2));
          if (liMaxno >= 99) {
            alertErr("當天送製卡批號已達 99, 不可再產生(crd_emboss_tmp)");
            return -1;
          }
          if (lsBno2.length() < 8) {
            lsBno2 = String.format("%08d", lsBno2 + 1);
          }
        }
        isBatchnoBoss = lsBno2;
    }
    return 1;
  }


}

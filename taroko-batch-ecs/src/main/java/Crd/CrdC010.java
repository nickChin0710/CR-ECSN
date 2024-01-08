/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/08/18  v1.00.01    Wilson    insert cca_card_open、crd_card必要欄位寫入             *
*  109/09/28  v1.00.01    Zuwei    Fix code scan issue             *
*  109-10-19  V1.00.02    shiyuqi       updated for project coding standard     *
*  110/02/19  V1.00.03    Wilson    檢核程式是否在執行中                                                                              *
******************************************************************************/

package Crd;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

import bank.authbatch.main.AuthBatch080;
import bank.authbatch.vo.Data080Vo;

/*緊急替代卡寫入主檔作業*/
public class CrdC010 extends AccessDAO {
  private String progname = "緊急替代卡寫入主檔作業  110/02/19  V1.00.03";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  int debug = 1;

  String stderr = "";
  long hModSeqno = 0;
  String hModUser = "";
  String hModTime = "";
  String hModPgm = "";
  String hCallBatchSeqno = "";
  String hMCurpModPgm = "";
  String hMCurpModTime = "";
  String hMCurpModUser = "";
  long hMCurpModSeqno = 0;

  String hTempUser = "";
  String hOnbaCardNo = "";
  String hOnbaOldCardNo = "";
  String hOnbaCardValidFrom = "";
  String hOnbaCardValidTo = "";
  String hOnbaProcessDate = "";
  String hOnbaRowid = "";
  String hCardOldCardNo = "";
  // String h_card_id = "";
  // String h_card_id_code = "";
  String hCardIdPSeqno = "";
  String hCardCorpNo = "";
  String hCardCorpNoCode = "";
  String hCardCorpPSeqno = "";
  String hCardCardType = "";
  String hCardUrgentFlag = "";
  String hCardGroupCode = "";
  String hCardSourceCode = "";
  String hCardBinType = "";
  String hCardSupFlag = "";
  String hCardMajorRelation = "";
  String hCardMajorIdPSeqno = "";
  String hCardMajorCardNo = "";
  String hCardMemberNote = "";
  String hCardMemberId = "";
  String hCardForceFlag = "";
  String hCardEngName = "";
  String hCardRegBankNo = "";
  String hCardUnitCode = "";
  String hCardCurrentCode = "";
  String hCardOldBegDate = "";
  String hCardOldEndDate = "";
  String hCardAcctType = "";
  String hCardGpNo = "";
  String hCardPSeqno = "";
  String hCardComboIndicator = "";
  String hCardComboAcctNo = "";
  String hCardSonCardFlag = "";
  String hCardStmtCycle = "";
  String hCardBinNo = "";
  String hCardCardNote = "";
  String hCardAutoInstallment = "";
  String hCardCardNo = "";
  String hCardNewBegDate = "";
  String hCardNewEndDate = "";
  String hCardIssueDate = "";
  String hCardReissueDate = "";
  String hCardReissueReason = "";
  String hCardChangeDate = "";
  String hCardUpgradeDate = "";
  String hCardApplyNo = "";
  String hCardPromoteEmpNo = "";
  String hCardIntroduceId = "";
  String hCardIntroduceName = "";
  String hCardProdNo = "";
  String hCardPvv = "";
  String hCardCvv = "";
  String hCardCvv2 = "";
  String hCardPvki = "";
  String hCardBatchno = "";
  int hCardRecno = 0;
  String hCardOppostReason = "";
  String hCardOppostDate = "";
  String hCardFeeCode = "";
  String hCardExpireChgFlag = "";
  String hCardExpireChgDate = "";
  String hCardCorpActFlag = "";
  String hCardActivateFlag = "";
  String hCardActivateDate = "";
  String hCardActivateType = "";
  String hCardSetCode = "";
  String hCardMailType = "";
  String hCardMailNo = "";
  String hCardStockNo = "";
  String hCardCrtDate = "";
  String hCardCrtUser = "";
  String hCardAprDate = "";
  String hCardAprUser = "";
  String hCardModUser = "";
  String hCardModTime = "";
  String hCardModPgm = "";
  String hCardModWs = "";
  double hCardModSeqno = 0;
  String hCardModLog = "";
  String hCardNewCardNo = "";
  String hChiName = "";
  String hBirthday = "";
  String hMajorChiName = "";
  String hMajorBirthday = "";
  String hRowid = "";
  String hCardNo = "";
  String hApscStatusCode = "";
  String hApscCardNo = "";
  String hApscValidDate = "";
  String hApscStopDate = "";
  String hApscReissueDate = "";
  String hApscStopReason = "";
  String hApscMailType = "";
  String hApscMailNo = "";
  String hApscMailBranch = "";
  String hApscMailDate = "";
  String hApscPmId = "";
  String hApscPmIdCode = "";
  String hApscPmBirthday = "";
  String hApscSupId = "";
  String hApscSupIdCode = "";
  String hApscSupBirthday = "";
  String hApscCorpNo = "";
  String hApscCorpNoCode = "";
  String hApscCardType = "";
  String hApscPmName = "";
  String hApscSupName = "";
  String hApscSupLostStatus = "";
  String hApscGroupCode = "";
  int hStatus = 0;
  int totCnt = 0;

  String hCcabId = "";
  String hCcabIdCode = "";
  String hCcabAcctKey = "";
  String hCcabCardIndicator = "";
  String hCcabCorpActFlag = "";
  String hCcabCorpNo = "";
  String hCcabVoicePawd = "";
  String hCcabAcctType = "";
  String hCcabAcnoFlag = "";
  String hCcabCardNo = "";
  String hCcabCorpPSeqno = "";
  int hCcabSonCreditLmt = 0;
  String hCcabCvv2 = "";
  String hCcabEngName = "";
  String hCcabPSeqno = "";
  String hCcabGroupCode = "";
  String hCcabIdPSeqno = "";
  String hCcabMajorIdPSeqno = "";
  String hCcabIssueDate = "";
  String hCcabAcnoPSeqno = "";

  public int mainProcess(String[] args) {

    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname + "," + args.length);
      // =====================================
      if (args.length > 1) {
        comc.errExit("Usage : CrdC010", "");
      }

      // 固定要做的
      
      if(comm.isAppActive(javaProgram)) {
//          comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
    	  showLogMessage("I", "", "Someone is running this program now!!!, Please wait a moment to run again!! ");
    	  return (0);
      }

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      hTempUser = "";
      if (hCallBatchSeqno.length() == 20) {

        comcr.hCallBatchSeqno = hCallBatchSeqno;
        comcr.hCallRProgramCode = javaProgram;

        comcr.callbatch(0, 0, 1);
        sqlCmd = "select user_id ";
        sqlCmd += " from ptr_callbatch  ";
        sqlCmd += "where batch_seqno = ? ";
        setString(1, hCallBatchSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
          hTempUser = getValue("user_id");
        }
      }
      if (hTempUser.length() == 0) {
        hModUser = comc.commGetUserID();
        hTempUser = hModUser;
      }

      fetchDetail();

      showLogMessage("I", "", String.format("程式執行結束,筆數=[%d]", totCnt));

      // ==============================================
      // 固定要做的
      if (hCallBatchSeqno.length() == 20)
        comcr.callbatch(1, 0, 1); /* 1: 結束 */
      showLogMessage("I", "", "執行結束");
      finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
    }
  }

  /***********************************************************************/
  void fetchDetail() throws Exception {

    sqlCmd = "select ";
    sqlCmd += "card_no,";
    sqlCmd += "old_card_no,";
    sqlCmd += "card_valid_from, ";
    sqlCmd += "card_valid_to, ";
    sqlCmd += "proc_date, rowid  as rowid ";
    sqlCmd += " from onbat_2ecs ";
    sqlCmd += "where to_which    = '1' ";
    sqlCmd += "  and trans_type  = '8' ";
    sqlCmd += "  and proc_status = '0' ";
    sqlCmd += "order by dog,card_no ";
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      hOnbaCardNo = getValue("card_no", i);
      hOnbaOldCardNo = getValue("old_card_no", i);
      hOnbaCardValidFrom = getValue("card_valid_from", i);
      hOnbaCardValidTo = getValue("card_valid_to", i);
      hOnbaProcessDate = getValue("process_date", i);
      hOnbaRowid = getValue("rowid", i);
      if (debug == 1)
        showLogMessage("I", "", "Read card=" + hOnbaCardNo + ", Old=" + hOnbaOldCardNo);

      hStatus = 0;
      if (getCardData() != 0) {
        hStatus = 2;
        updateOnbat2ecs();
        continue;
      }
      if (getIdnoData() != 0) {
        hStatus = 2;
        updateOnbat2ecs();
        continue;
      }
      totCnt++;
      insertCrdCard();
      int rtn = insertCcaBase();
      if (rtn != 0)
        continue;
      updateOldCard();
      // process_apscard();
      hStatus = 1;
      updateOnbat2ecs();
      insertCcaCardOpen();
      commitDataBase();
    }
  }

  /***********************************************************************/
  int getCardData() throws Exception {
    try {
      sqlCmd = "select card_no,";
      sqlCmd += "id_p_seqno,";
      sqlCmd += "corp_no,";
      sqlCmd += "corp_no_code,";
      sqlCmd += "corp_p_seqno,";
      sqlCmd += "card_type,";
      sqlCmd += "'Y' as h_card_urgent_flag,";
      sqlCmd += "group_code,";
      sqlCmd += "source_code,";
      sqlCmd += "bin_type,";
      sqlCmd += "sup_flag,";
      sqlCmd += "major_relation,";
      sqlCmd += "major_id_p_seqno,";
      sqlCmd += "major_card_no,";
      sqlCmd += "member_note,";
      sqlCmd += "member_id,";
      sqlCmd += "force_flag,";
      sqlCmd += "eng_name,";
      sqlCmd += "reg_bank_no,";
      sqlCmd += "unit_code,";
      sqlCmd += "current_code,";
      sqlCmd += "new_beg_date,";
      sqlCmd += "new_end_date,";
      sqlCmd += "acct_type,";
      sqlCmd += "p_seqno,";
      sqlCmd += "acno_p_seqno,";
      sqlCmd += "combo_indicator,";
      sqlCmd += "combo_acct_no,";
      sqlCmd += "son_card_flag,";
      sqlCmd += "stmt_cycle, ";
      sqlCmd += "bin_no, ";
      sqlCmd += "card_note, ";
      /* add for insert_cca_base */
      sqlCmd += "(select id_no from crd_idno where id_p_seqno = a.id_p_seqno) as id_no , ";
      sqlCmd +=
          "(select id_no_code from crd_idno where id_p_seqno = a.id_p_seqno) as id_no_code , ";
      sqlCmd += "(select acct_key from act_acno where acno_p_seqno = a.acno_p_seqno) as acct_key, ";
      sqlCmd +=
          "(select card_indicator from ptr_acct_type where acct_type = a.acct_type) as card_indicator, ";
      sqlCmd +=
          "(select corp_act_flag from act_acno where acno_p_seqno = a.acno_p_seqno) as corp_act_flag, ";
      sqlCmd += "corp_no, ";
      sqlCmd +=
          "(select voice_passwd from crd_idno where id_p_seqno = a.id_p_seqno) as voice_passwd , ";
      sqlCmd += "acct_type, ";
      sqlCmd += "acno_flag, ";
      sqlCmd += "card_no, ";
      sqlCmd += "corp_p_seqno, ";
      sqlCmd += "indiv_crd_lmt, ";
      sqlCmd += "son_card_flag, ";
      sqlCmd += "cvv2, ";
      sqlCmd += "eng_name, ";
      sqlCmd += "p_seqno, ";
      sqlCmd += "group_code, ";
      sqlCmd += "id_p_seqno, ";
      sqlCmd += "major_id_p_seqno, ";
      sqlCmd += "issue_date, ";
      sqlCmd += "acno_p_seqno  ";
      sqlCmd += " from crd_card a ";
      sqlCmd += "where card_no = ? ";
      setString(1, hOnbaOldCardNo);
      extendField = "card.";
      int recordCnt = selectTable();
      if (recordCnt > 0) {
        hCardOldCardNo = getValue("card.card_no");
        hCardIdPSeqno = getValue("card.id_p_seqno");
        hCardCorpNo = getValue("card.corp_no");
        hCardCorpNoCode = getValue("card.corp_no_code");
        hCardCorpPSeqno = getValue("card.corp_p_seqno");
        hCardCardType = getValue("card.card_type");
        hCardUrgentFlag = getValue("card.h_card_urgent_flag");
        hCardGroupCode = getValue("card.group_code");
        hCardSourceCode = getValue("card.source_code");
        hCardBinType = getValue("card.bin_type");
        hCardSupFlag = getValue("card.sup_flag");
        hCardMajorRelation = getValue("card.major_relation");
        hCardMajorIdPSeqno = getValue("card.major_id_p_seqno");
        hCardMajorCardNo = getValue("card.major_card_no");
        hCardMemberNote = getValue("card.member_note");
        hCardMemberId = getValue("card.member_id");
        hCardForceFlag = getValue("card.force_flag");
        hCardEngName = getValue("card.eng_name");
        hCardRegBankNo = getValue("card.reg_bank_no");
        hCardUnitCode = getValue("card.unit_code");
        hCardCurrentCode = getValue("card.current_code");
        hCardOldBegDate = getValue("card.new_beg_date");
        hCardOldEndDate = getValue("card.new_end_date");
        hCardAcctType = getValue("card.acct_type");
        hCardGpNo = getValue("card.p_seqno");
        hCardPSeqno = getValue("card.acno_p_seqno");
        hCardComboIndicator = getValue("card.combo_indicator");
        hCardComboAcctNo = getValue("card.combo_acct_no");
        hCardSonCardFlag = getValue("card.son_card_flag");
        hCardStmtCycle = getValue("card.stmt_cycle");
        hCardBinNo = getValue("card.bin_no");
        hCardCardNote = getValue("card.card_note");

        if (debug == 1)
          showLogMessage("I", "", "888 Read =" + hOnbaOldCardNo + ",type=" + hCardBinType);
        /* add for insert_cca_base */
        hCcabId = getValue("card.id_no");
        hCcabIdCode = getValue("card.id_no_code");
        hCcabAcctKey = getValue("card.acct_key");
        hCcabCardIndicator = getValue("card.card_indicator");
        hCcabCorpActFlag = getValue("card.corp_act_flag");
        hCcabCorpNo = getValue("card.corp_no");
        hCcabVoicePawd = getValue("card.voice_passwd");
        hCcabAcctType = getValue("card.acct_type");
        hCcabAcnoFlag = getValue("card.acno_flag");
        hCcabCardNo = hOnbaCardNo;
        hCcabCorpPSeqno = getValue("card.corp_p_seqno");
        hCcabSonCreditLmt = getValueInt("card.indiv_crd_lmt");
        if (getValue("son_card_flag").length() == 0)
          hCcabSonCreditLmt = 0;
        hCcabCvv2 = getValue("card.cvv2");
        hCcabEngName = getValue("card.eng_name");
        hCcabPSeqno = getValue("card.p_seqno");
        hCcabGroupCode = getValue("card.group_code");
        hCcabIdPSeqno = getValue("card.id_p_seqno");
        hCcabMajorIdPSeqno = getValue("card.major_id_p_seqno");
        hCcabIssueDate = getValue("card.issue_date");
        hCcabAcnoPSeqno = getValue("card.acno_p_seqno");
      } else {
        return (2);
      }
    } catch (Exception ex) {
      return 2;
    }

    if (hCardCurrentCode.equals("0")) {
      stderr = String.format("old_card[%s] CURRENT_CODE ERROR [%s] cannot be '0'", hOnbaOldCardNo,
          hCardCurrentCode);
      showLogMessage("I", "", stderr);
      return (1);
    }
    /******** 正卡時,major_card_no = card_no ************/
    if (hCardSupFlag.equals("0")) {
      hCardMajorCardNo = hOnbaCardNo;
    }
    return (0);
  }

  /***********************************************************************/
  void updateOnbat2ecs() throws Exception {
    daoTable = "onbat_2ecs";
    updateSQL = " proc_status = ?,";
    updateSQL += " dop         = sysdate";
    whereStr = "where rowid  = ? ";
    setInt(1, hStatus);
    setRowId(2, hOnbaRowid);
    updateTable();

    return;
  }

  /***********************************************************************/
  int getIdnoData() throws Exception {
    hChiName = "";
    hBirthday = "";

    try {
      sqlCmd = "select chi_name,";
      sqlCmd += "birthday ";
      sqlCmd += " from crd_idno  ";
      sqlCmd += "where id_p_seqno  = ?  ";
      setString(1, hCardIdPSeqno);
      int recordCnt = selectTable();
      if (recordCnt > 0) {
        hChiName = getValue("chi_name");
        hBirthday = getValue("birthday");
      } else {
        return (1);
      }
    } catch (Exception ex) {
      return 1;
    }

    if (hCardSupFlag.equals("1")) {
      if (getPmData() != 0) {
        return (1);
      }
    }

    return (0);
  }

  /***********************************************************************/
  int getPmData() throws Exception {
    hMajorChiName = "";
    hMajorBirthday = "";
    try {
      sqlCmd = "select chi_name,";
      sqlCmd += "birthday ";
      sqlCmd += " from crd_idno  ";
      sqlCmd += "where id_p_seqno  = ?  ";
      setString(1, hCardMajorIdPSeqno);
      int recordCnt = selectTable();
      if (recordCnt > 0) {
        hMajorChiName = getValue("chi_name");
        hMajorBirthday = getValue("birthday");
      } else {
        return (1);
      }
    } catch (Exception ex) {
      return 1;
    }

    return (0);
  }

  /***********************************************************************/
  void insertCrdCard() throws Exception {

    hCardAutoInstallment = "N";
    sqlCmd = "select auto_installment ";
    sqlCmd += " from ptr_group_code  ";
    sqlCmd += "where group_code = ? ";
    setString(1, hCardGroupCode);
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_group_code not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hCardAutoInstallment = getValue("auto_installment");
    }

    hCardCardNo = hOnbaCardNo;
    hCardNewBegDate = hOnbaCardValidFrom;
    hCardNewEndDate = hOnbaCardValidTo;
    hCardActivateFlag = "2";
    hCardActivateDate = sysDate;
    hCardActivateType = "O";
    hCardExpireChgFlag = "1";
    hCardExpireChgDate = sysDate;
    hCardIssueDate = sysDate;
    hCardCurrentCode = "0";
    hCardUrgentFlag = "Y";
    hCardCrtDate = sysDate;
    hCardModPgm = javaProgram;
    hCardModUser = comc.commGetUserID();

    setValue("card_no", hCardCardNo);
    setValue("id_p_seqno", hCardIdPSeqno);
    setValue("corp_p_seqno", hCardCorpPSeqno);
    setValue("corp_no", hCardCorpNo);
    setValue("corp_no_code", hCardCorpNoCode);
    setValue("auto_installment", hCardAutoInstallment);
    setValue("card_type", hCardCardType);
    setValue("urgent_flag", hCardUrgentFlag);
    setValue("group_code", hCardGroupCode);
    setValue("source_code", hCardSourceCode);
    setValue("sup_flag", hCardSupFlag);
    setValue("major_relation", hCardMajorRelation);
    setValue("major_id_p_seqno", hCardMajorIdPSeqno);
    setValue("major_card_no", hCardMajorCardNo);
    setValue("member_note", hCardMemberNote);
    setValue("member_id", hCardMemberId);
    setValue("current_code", hCardCurrentCode);
    setValue("eng_name", hCardEngName);
    setValue("reg_bank_no", hCardRegBankNo);
    setValue("unit_code", hCardUnitCode);
    setValue("new_beg_date", hCardNewBegDate);
    setValue("new_end_date", hCardNewEndDate);
    setValue("issue_date", hCardIssueDate);
    setValue("reissue_date", hCardReissueDate);
    setValue("reissue_reason", hCardReissueReason);
    setValue("change_date", hCardChangeDate);
    setValue("upgrade_date", hCardUpgradeDate);
    setValue("apply_no", hCardApplyNo);
    setValue("promote_emp_no", hCardPromoteEmpNo);
    setValue("introduce_id", hCardIntroduceId);
    setValue("introduce_name", hCardIntroduceName);
    setValue("prod_no", hCardProdNo);
    setValue("pvv", hCardPvv);
    setValue("cvv", hCardCvv);
    setValue("cvv2", hCardCvv2);
    setValue("pvki", hCardPvki);
    setValue("batchno", hCardBatchno);
    setValueInt("recno", hCardRecno);
    setValue("oppost_reason", hCardOppostReason);
    setValue("oppost_date", hCardOppostDate);
    setValue("old_card_no", hCardOldCardNo);
    setValue("acct_type", hCardAcctType);
    setValue("p_seqno", hCardGpNo);
    setValue("acno_p_seqno", hCardPSeqno);
    setValue("fee_code", hCardFeeCode);
    setValue("stmt_cycle", hCardStmtCycle);
    setValue("expire_chg_flag", hCardExpireChgFlag);
    setValue("expire_chg_date", hCardExpireChgDate);
    setValue("corp_act_flag", hCardCorpActFlag);
    setValue("activate_flag", hCardActivateFlag);
    setValue("activate_date", hCardActivateDate);
    setValue("activate_type", hCardActivateType);
    setValue("son_card_flag", hCardSonCardFlag);
    setValue("set_code", hCardSetCode);
    setValue("mail_type", hCardMailType);
    setValue("mail_no", hCardMailNo);
    setValue("stock_no", hCardStockNo);
    setValue("combo_indicator", hCardComboIndicator);
    setValue("combo_acct_no", hCardComboAcctNo);
    setValue("bin_type", hCardBinType);
    setValue("bin_no", hCardBinNo);
    setValue("card_note", hCardCardNote);
    setValue("crt_date", hCardCrtDate);
    setValue("crt_user", hCardCrtUser);
    setValue("apr_date", hCardAprDate);
    setValue("apr_user", hCardAprUser);
    setValue("mod_user", hCardModUser);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", hCardModPgm);
    setValueDouble("mod_seqno", hCardModSeqno);
    daoTable = "crd_card";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_crd_card duplicate!", "", hCallBatchSeqno);
    }

    return;
  }

  // ************************************************************************
  public int insertCcaBase() throws Exception {
    String hPaymentRule = "";
    String hApplyId = "";
    String pOpenPawd = null;
    String pVoicePawd = null;
    String corpAcctKey = "";
    String hCcsAcctKey = "";

    if (debug == 1)
      showLogMessage("I", "", "  888 insert_cca_base ");

    hApplyId = hCcabId + hCcabIdCode;
    hCcsAcctKey = hCcabAcctKey;
    String hBusCard = "N";
    hPaymentRule = "1";
    if (hCcabCardIndicator.equals("2")) {
      hBusCard = "Y";
      if (hCcabCorpActFlag.equals("Y")) {
        hPaymentRule = "2";
        corpAcctKey = hCcabCorpNo + "000";
        hCcsAcctKey = corpAcctKey;
      } else {
        hPaymentRule = "1";
      }
    }
    pOpenPawd = getValue("open_passwd");
    pVoicePawd = hCcabVoicePawd;

    /* 201504013 三合一卡改不等於N */
    if (getValue("combo_indicator").compareTo("N") != 0
        && getValue("sup_flag").compareTo("0") == 0) {
      pOpenPawd = "";
    }

    Data080Vo lData080Vo = new Data080Vo();
    lData080Vo.setAccountType(hCcabAcctType);
    lData080Vo.setAcctNo(getValue("act_no"));
    lData080Vo.setAcnoFlag(hCcabAcnoFlag);
    lData080Vo.setBankActNo("");
    lData080Vo.setBinType(hCardBinType);
    lData080Vo.setBusinessCard(hBusCard);
    lData080Vo.setCardAcctId(hCcsAcctKey); // => 應該是 11 bytes
    lData080Vo.setCardHolderId(hApplyId); // CardHolderId+Seq => 應該是 11 bytes
    lData080Vo.setCardNo(hCcabCardNo);
    lData080Vo.setCardType("Y");
    if (getValue("sup_flag").equals("1"))
      lData080Vo.setCardType("N");
    lData080Vo.setBusinessCard("");
    lData080Vo.setComboIndicator(getValue("combo_indicator"));
    lData080Vo.setCorpPSeqN0(hCcabCorpPSeqno);
    lData080Vo.setCreditLimit(hCcabSonCreditLmt);
    lData080Vo.setCvc2(hCcabCvv2);
    lData080Vo.setDcCurrCode("");
    lData080Vo.setDebitFlag("N");
    lData080Vo.setEngName(hCcabEngName);
    lData080Vo.setPSeqNo(hCcabPSeqno);
    lData080Vo.setGroupCode(hCcabGroupCode);
    lData080Vo.setIdPSqno(hCcabIdPSeqno);
    lData080Vo.setMajorIdPSeqNo(hCcabMajorIdPSeqno);
    lData080Vo.setMemberSince(hCcabIssueDate);
    lData080Vo.setOldCardNo(getValue("old_card_no"));
    lData080Vo.setPaymentRule(hPaymentRule);
    lData080Vo.setPinBlock("");// mbos_pin_block
    lData080Vo.setPinOfActive(pOpenPawd);
    lData080Vo.setPinOfVoice(pVoicePawd);
    lData080Vo.setAcnoPSeqNo(hCcabAcnoPSeqno);
    lData080Vo.setPvki("");// mbos_pvki
    lData080Vo.setRule(hCcabCardIndicator);
    lData080Vo.setSource("4"); // 固定塞4
    lData080Vo.setValidFrom(hCardNewBegDate.substring(0, 6));
    lData080Vo.setValidTo(hCardNewEndDate.substring(0, 6));
    AuthBatch080 l080 = new AuthBatch080();
    l080.initProg(getConnection());
    int nLResult = l080.startProcess(lData080Vo);
    /*
     * return 0 => 正常處理完成 return > 0 => 程式正常處理完成，但有資料面的問題 return -1 => 有 error (exception)
     */
    if (nLResult == 0)
      System.out.println(lData080Vo.getCardNo() + ":" + "正常處理完成...");
    else if (nLResult > 0) {
      System.out.println(lData080Vo.getCardNo() + ":" + "程式正常處理完成，但有資料面的問題...");
      rollbackDataBase();
      return (1);
    }
    if (nLResult < 0) {
      System.out.println(lData080Vo.getCardNo() + ":" + "發生 error...");
      rollbackDataBase();
      return (1);
    }

    // System.out.println(L_Data080Vo.getCardNo() + "=>" + nL_Result);

    lData080Vo = null;

    return (0);
  }

  /***********************************************************************/
  void updateOldCard() throws Exception {
    /* 之前insert or update 不成功,不可做insert or update */
    hCardNewCardNo = hOnbaCardNo;
    daoTable = "crd_card";
    updateSQL = "new_card_no   = ?,";
    updateSQL += "reissue_date   = to_char(sysdate,'yyyymmdd'),";
    updateSQL += "reissue_status   = '3',";
    updateSQL += "mod_pgm   = ?,";
    updateSQL += "mod_time   = sysdate";
    whereStr = "where card_no = ? ";
    setString(1, hCardNewCardNo);
    setString(2, hCardModPgm);
    setString(3, hOnbaOldCardNo);

    updateTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("update_crd_card not found!", "", hCallBatchSeqno);
    }

    return;
  }

  /**********************************************************************/
  // void process_apscard() throws Exception {
  // String h_rowid = "";
  // String h_card_no = "";
  //
  // h_rowid = "";
  // h_card_no = "";
  // h_card_no = h_onba_card_no;

  // h_apsc_status_code = "2";
  // try {
  // sqlCmd = "select rowid as rowid ";
  // sqlCmd += " from crd_apscard ";
  // sqlCmd += "where card_no = ? ";
  // sqlCmd += " and to_aps_date = '' ";
  // setString(1, h_card_no);
  // int recordCnt = selectTable();
  // if (recordCnt > 0) {
  // h_rowid = getValue("rowid");

  // daoTable = "crd_apscard";
  // updateSQL = "status_code = ?,";
  // updateSQL += " reissue_date = to_char(sysdate,'yyyymmdd'),";
  // updateSQL += " mod_time = sysdate,";
  // updateSQL += " mod_user = ?,";
  // updateSQL += " mod_pgm = ?";
  // whereStr = "where rowid = ? ";
  // setString(1, h_apsc_status_code);
  // setString(2, h_mod_user);
  // setString(3, javaProgram);
  // setRowId(4, h_rowid);
  // updateTable();
  // if (notFound.equals("Y")) {
  // comcr.errRtn("update_crd_apscard not found!", "", h_call_batch_seqno);
  // }
  // } else {
  // insert_apscard();
  // }
  // } catch (Exception ex) {
  // insert_apscard();
  // }

  // return;
  // }

  /***********************************************************************/
  // void insert_apscard() throws Exception {
  // h_apsc_card_no = h_onba_card_no;
  // h_apsc_valid_date = h_card_new_end_date;
  // if (h_card_sup_flag.equals("0")) {
  // String[] info = comcr.getIDInfo(h_card_id_p_seqno);
  // h_apsc_pm_id = info[0];
  // h_apsc_pm_id_code = info[1];
  //
  // h_apsc_pm_birthday = h_birthday;
  // h_apsc_pm_name = h_chi_name;
  // }
  // if (h_card_sup_flag.equals("1")) {
  // String[] info = comcr.getIDInfo(h_card_major_id_p_seqno);
  // h_apsc_pm_id = info[0];
  // h_apsc_pm_id_code = info[1];
  //
  // h_apsc_sup_birthday = h_birthday;
  // h_apsc_sup_name = h_chi_name;

  // info = comcr.getIDInfo(h_card_id_p_seqno);
  // h_apsc_sup_id = info[0];
  // h_apsc_sup_id_code = info[1];
  //
  // h_apsc_pm_birthday = h_major_birthday;
  // h_apsc_pm_name = h_major_chi_name;
  // }

  // h_apsc_corp_no = h_card_corp_no;
  // h_apsc_corp_no_code = h_card_corp_no_code;
  // h_apsc_card_type = h_card_card_type;
  // h_apsc_group_code = h_card_group_code;
  // setValue("crt_datetime" , sysDate + sysTime);
  // setValue("card_no" , h_apsc_card_no);
  // setValue("valid_date" , h_apsc_valid_date);
  // setValue("stop_date" , h_apsc_stop_date);
  // setValue("reissue_date" , h_apsc_reissue_date);
  // setValue("stop_reason" , h_apsc_stop_reason);
  // setValue("mail_type" , h_apsc_mail_type);
  // setValue("mail_no" , h_apsc_mail_no);
  // setValue("mail_branch" , h_apsc_mail_branch);
  // setValue("mail_date" , h_apsc_mail_date);
  // setValue("pm_id" , h_apsc_pm_id);
  // setValue("pm_id_code" , h_apsc_pm_id_code);
  // setValue("pm_birthday" , h_apsc_pm_birthday);
  // setValue("sup_id" , h_apsc_sup_id);
  // setValue("sup_id_code" , h_apsc_sup_id_code);
  // setValue("sup_birthday" , h_apsc_sup_birthday);
  // setValue("corp_no" , h_apsc_corp_no);
  // setValue("corp_no_code" , h_apsc_corp_no_code);
  // setValue("card_type" , h_apsc_card_type);
  // setValue("pm_name" , h_apsc_pm_name);
  // setValue("sup_name" , h_apsc_sup_name);
  // setValue("sup_lost_status", h_apsc_sup_lost_status);
  // setValue("status_code" , h_apsc_status_code);
  // setValue("group_code" , h_apsc_group_code);
  // setValue("mod_user" , h_mod_user);
  // setValue("mod_time" , sysDate + sysTime);
  // setValue("mod_pgm" , javaProgram);
  // daoTable = "crd_apscard";
  // insertTable();
  // if (dupRecord.equals("Y")) {
  // comcr.errRtn("insert_crd_apscard duplicate!", "", h_call_batch_seqno);
  // }

  // return;
  // }

  /***********************************************************************/
  void insertCcaCardOpen() throws Exception {

    setValue("card_no", hCardCardNo);
    setValue("new_beg_date", hCardNewBegDate);
    setValue("new_end_date", hCardNewEndDate);
    setValue("new_old_flag", "N");
    setValue("open_type", "O");
    setValue("open_date", hCardActivateDate);
    setValue("open_time", sysTime);
    setValue("open_user", hCardModUser);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", hCardModPgm);
    daoTable = "cca_card_open";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_cca_card_open duplicate!", "", hCallBatchSeqno);
    }

    return;
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    CrdC010 proc = new CrdC010();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
}

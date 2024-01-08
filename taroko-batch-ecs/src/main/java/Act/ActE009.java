/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/07/18  V1.00.01    Simon     TCB 繳款起帳 initial                      * 
 *  112/08/04  V1.00.02    Simon     1.TCB 繳款起帳新增貸方：入帳失敗(1借2貸)  * 
 *                                   2.ACH自扣繳款作業因需人工介入輸入變化值，故不列入自動起帳* 
 *                                   3.新增郵局自扣借方手續費(2借2貸)          * 
 *  112/08/07  V1.00.03    Simon     process act_pay_batch.batch_tot_cnt = 0   * 
 *  112/09/18  V1.00.04    Simon     繳款入帳、繳款銷帳、調整銷帳會計分錄套號第3碼固定為"2"* 
 *  112/10/31  V1.00.05    Simon     同業代收款入帳摘要細分                    * 
 *  112/12/08  V1.00.06    Simon     還額檔與自行扣款都不起帳                  * 
 *  113/01/05  V1.00.07    Simon     他行代償、債務協商、前置協商、全國繳費網都不起帳*                  * 
 ******************************************************************************/

package Act;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/*繳款起帳處理程式*/
public class ActE009 extends AccessDAO {

  private final String PROGNAME = "繳款起帳處理程式  113-01-05  V1.00.07";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;
  CommString comStr = new CommString();

  String rptNameMemo3 = "";
  String buf = "";
  String szTmp = "";
  String hCallBatchSeqno = "";

  String hPcceCurrChiName = "";
  String hPcceCurrCode = "";
  String hPcceCurrCodeGl = "";
  String hAdclBatchNo = "";
  String hAdclPSeqno = "";
  String hAdclAcctType = "";
  String hAdclAcctKey = "";
  String hAdclPayCardNo = "";
  double hAdclPayAmt = 0;
  String hAdclPayDate = "";
  String hAdclPaymentType = "";
  String hAdclDebitItem = "";
  String hAdclDebtKey = "";
  String hAdclVouchMemo3 = "";
  String hAdclJobCode = "";
  String hAdclVouchJobCode = "";
  String hAdclBranch = "";
  String hAperBatchNo = "";
  String hAperSerialNo = "";
  String hAperPSeqno = "";
  String hAperAcctType = "";
  String hAperAcctKey = "";
  String hOrigAcctKey = "";
  String hAperPayIdNumber = "";
  double hAperPayAmt = 0;
  String hAperErrorReason = "";
  String hAperErrorRemark = "";
  String hAperRowid = "";
  String hAdclRowid = "";
  double hOldErrAmt = 0;
  String hOldBatchNo = "";
  double hOldMinusAmt = 0;
  String hVouchCdKind = "";
  String hGsvhAcNo = "";
  int hGsvhDbcrSeq = 0;
  String hGsvhDbcr = "";
  String blk         = "　";

  String hAccmMemo3Kind = "";
  String hAccmMemo3Flag = "";
  String hAccmDrFlag = "";
  String hAccmCrFlag = "";
  String hAperBranch = "";
  String hRealAcNo = "";
  String hVoucMemo1 = "";
  String hVoucMemo2 = "";
  String hVoucMemo3 = "";
  //String h_gsvh_std_vouch_cd = "";
  String hVoucAcNo = "";
  double hVoucAmt = 0;
  String hTempAcBriefName = "";
  String hMemo3FlagCom = "";
  String hBusinssChiDate = "";
  String hAperVouchMemo3 = "";
  String hBusiBusinessDate = "";
  String hTempVouchDate = "";
  String hVouchChiDate = "";
  String hSystemDateFull = "";
  String hPccdGlCode = "";
  String hPrintName = "";
  String hRptName = "";

  String[] aPcceCurrChiName = new String[100];
  String[] aPcceCurrCode = new String[100];
  String[] aPcceCurrCodeGl = new String[100];

  double hAmblAutopayAmt = 0;
  double hAmblOffsetAmt = 0;
  double hAmblOnuspayAmt = 0;
  String hAdclModUser = "";
  double hOldOkAmt;
  int    hOldOkCnt;
  long   hOldOkFee;
  double hOldOkRem;
  String hOldVouchJobCode = "";
  String temstrCom = "";
  String memo3Mark = "N";
  int seqRcnt = 0;
  int seqEcnt = 0;
  
//int firstFlag1 = 0;
  int firstFlag2 = 0;
//int firstFlag3 = 0;
  int ptrCurrcodeCnt = 0;
  String hOldDebitItem = "";
  String hOldPaymentType = "";
  String hOldDebtKey = "";

  public int mainProcess(String[] args) {

    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + PROGNAME);
      // =====================================
      if (args.length > 1) {
          comc.errExit("Usage : ActE009, this program need only one parameter", "");
      }

      // 固定要做的

      if (!connectDataBase()) {
          comc.errExit("connect DataBase error", "");
      }

      hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

      /* get system date & time */
      /*
       * after db connect successful, system date & time will be got
       * automatically
       */
      selectPtrBusinday();

      selectPtrCurrcode();

      for (int inti = 0; inti < ptrCurrcodeCnt; inti++) {
        hPcceCurrCode = aPcceCurrCode[inti];
        hPcceCurrCodeGl = aPcceCurrCodeGl[inti];

        hAmblAutopayAmt = 0;
        hAmblOffsetAmt = 0;
        hAmblOnuspayAmt = 0;
        firstFlag2 = 0;

        mainProcessRtn2();

        /* for BATCH_TOT_CNT = 0 */
        mainProcessRtn4();

        updateActMasterBal();
      }

      // ==============================================
      // 固定要做的
      comcr.hCallErrorDesc = "程式執行結束";
      comcr.callbatchEnd();
      finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
    }
  }

  /***********************************************************************/
  void selectPtrBusinday() throws Exception {
    hBusiBusinessDate = "";
    hBusinssChiDate = "";
    hVouchChiDate = "";
    hSystemDateFull = "";

    sqlCmd = "select business_date, ";
    sqlCmd += " vouch_date  ";
    sqlCmd += " from ptr_businday  ";
    sqlCmd += "fetch first 1 rows only ";
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_businday1 not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
        hBusiBusinessDate = getValue("business_date");
        hTempVouchDate = getValue("vouch_date");
    }
    sqlCmd = "select substr(to_char(to_number(business_date) - 19110000,'0000000'),2,7) h_businss_chi_date ";
    sqlCmd += " from ptr_businday ";
    recordCnt = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_businday2 not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
        hBusinssChiDate = getValue("h_businss_chi_date");
    }

    sqlCmd = "select substr(to_char(to_number(vouch_date) - 19110000,'00000000'),4,6) h_vouch_chi_date ";
    sqlCmd += " from ptr_businday ";
    recordCnt = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_businday3 not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
        hVouchChiDate = getValue("h_vouch_chi_date");
    }

    sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') h_system_date_full ";
    sqlCmd += " from dual ";
    recordCnt = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_system date not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
        hSystemDateFull = getValue("h_system_date_full");
    }

/***
    hPccdGlCode = "";

    sqlCmd = "select gl_code ";
    sqlCmd += " from PTR_DEPT_CODE  "; // ptr_classcode
    sqlCmd += "where dept_code = 'A401' "; // class_code
    recordCnt = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_dept_code not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
        hPccdGlCode = getValue("gl_code");
    }
***/

    hPccdGlCode = "2";

  }

  /***********************************************************************/
  void mainProcessRtn2() throws Exception {
    int vouchCnt = 0;
    sqlCmd = "select ";
    sqlCmd += " batch_no,";
    sqlCmd += " p_seqno,";
    sqlCmd += " acct_type,";
    sqlCmd += " UF_ACNO_KEY(p_seqno) acct_key,";
    sqlCmd += " pay_card_no,";
    sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',pay_amt,dc_pay_amt) h_adcl_pay_amt,";
    sqlCmd += " pay_date,";
    sqlCmd += " payment_type,";
    sqlCmd += " debit_item,";
    sqlCmd += " debt_key,";
    sqlCmd += " vouch_memo3,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_job_code,";
    sqlCmd += " branch ";
    sqlCmd += "  from act_debt_cancel ";
    sqlCmd += " where (substr(batch_no,9,4) in ('1003',"
          //他行代償、還額檔轉繳款、自行扣款、全國繳費網、債務協商、前置協商都不起帳
            + " '9002') or (substr(batch_no,9,4) = '1002' and "
            + " substr(batch_no,13,3) = '700')) ";
            //他行自動扣繳(含郵局自扣)只取郵局自扣，排除ACH自扣(這部分人工起帳)
    sqlCmd += "   and decode(process_flag,'','N',process_flag) <> 'Y' ";
    sqlCmd += "   and vouch_memo3 = '' ";
    sqlCmd += "   and decode(curr_code,'','901',curr_code) = ? ";
    sqlCmd += " order by batch_no ";
    setString(1, hPcceCurrCode);
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
      hAdclBatchNo = getValue("batch_no");
      hAdclPSeqno = getValue("p_seqno");
      hAdclAcctType = getValue("acct_type");
      hAdclAcctKey = getValue("acct_key");
      hAdclPayCardNo = getValue("pay_card_no");
      hAdclPayAmt = getValueDouble("h_adcl_pay_amt");
      hAdclPayDate = getValue("pay_date");
      hAdclPaymentType = getValue("payment_type");
      hAdclDebitItem = getValue("debit_item");
      hAdclDebtKey = getValue("debt_key");
      hAdclVouchMemo3 = getValue("vouch_memo3");
      hAdclJobCode = getValue("job_code");
      hAdclVouchJobCode = getValue("vouch_job_code");
      hAdclBranch = getValue("branch");
  
      /***************************************************/
      /* processing exclusion */
      /***************************************************/
      vouchCnt = 1;
      firstFlag2++;
  
      if (firstFlag2 == 1) {
          hOldBatchNo = hAdclBatchNo;
          hOldOkCnt = 0;
          hOldOkAmt = 0.00;
          hOldErrAmt = 0.00;
          hOldMinusAmt = 0.00;
      }
  
      if (!hAdclBatchNo.equals(hOldBatchNo)) {
          if (hOldBatchNo.substring(8, 12).equals("1003") 
                  || hOldBatchNo.substring(8, 12).equals("1005")
                  || hOldBatchNo.substring(8, 12).equals("1006")) {
              selectActPayErr1003();
        //} else if (hOldBatchNo.substring(8, 12).equals("5555")) {
        //    selectActPayErr5555();
          } else {
              selectActPayError();
          }

          insertVoucherRtn();
          hOldOkCnt = 0;
          hOldOkFee = 0;
          hOldOkRem = 0.00;
          hOldOkAmt = 0.00;
          hOldErrAmt = 0.00;
          hOldMinusAmt = 0.00;
      }
  
      hOldOkCnt++;
      hOldOkAmt = hOldOkAmt + hAdclPayAmt;
  
      hOldBatchNo = hAdclBatchNo;
      hOldPaymentType = hAdclPaymentType;
      hOldDebitItem = hAdclDebitItem;
      hOldDebtKey = hAdclDebtKey;
      hOldVouchJobCode = hAdclVouchJobCode;
    }
    closeCursor(cursorIndex);
    if (vouchCnt == 1) {
        /* process the last batch */
        if ((hOldBatchNo.substring(8, 12).equals("1003")) 
                || (hOldBatchNo.substring(8, 12).equals("1005"))
                || (hOldBatchNo.substring(8, 12).equals("1006"))) {
            selectActPayErr1003();
      //} else if (hOldBatchNo.substring(8, 12).equals("5555")) {
      //    selectActPayErr5555();
        } else {
            selectActPayError();
        }
  
        insertVoucherRtn();
    }
  }

  /***********************************************************************/
  void mainProcessRtn4() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " batch_no,";
    sqlCmd += " rowid rowid ";
    sqlCmd += "  from act_pay_batch ";
    sqlCmd += " where decode(post_gen_flag,'','N',post_gen_flag) <> 'Y' ";
    sqlCmd += "   and decode(curr_code,'','901',curr_code) = ? ";
    sqlCmd += "   and batch_tot_cnt = 0 ";
    setString(1, hPcceCurrCode);
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
      hAdclBatchNo = getValue("batch_no");
      hAdclRowid = getValue("rowid");

      hOldOkAmt = 0.00;
      hOldErrAmt = 0.00;
      hOldMinusAmt = 0.00;
      hAdclPayAmt = 0;
      hOldOkCnt = 0;
      hOldOkFee = 0;
      hOldOkRem = 0.00;

      hOldBatchNo = hAdclBatchNo;

      selectActPayErr1003();

      insertVoucherRtn();

      daoTable = "act_pay_batch";
      whereStr = "where rowid = ? ";
      setRowId(1, hAdclRowid);
      deleteTable();
      if (notFound.equals("Y")) {
          comcr.errRtn("delete_act_pay_batch not found!", "", hCallBatchSeqno);
      }

    }
    closeCursor(cursorIndex);
  }

  /***********************************************************************/
  void selectActPayErr5555() throws Exception {
      sqlCmd = "select sum(decode(decode(curr_code, '','901',curr_code),'901',pay_amt,dc_pay_amt)) h_old_err_amt ";
      sqlCmd += "  from act_pay_error  ";
      sqlCmd += " where batch_no = ?  ";
      sqlCmd += "   and decode(curr_code,'','901',curr_code) = ?  ";
      sqlCmd += "   and pay_amt != 0  ";
      sqlCmd += "   and vouch_memo3 = '' ";
      setString(1, hOldBatchNo);
      setString(2, hPcceCurrCode);
      int recordCnt = selectTable();
      if (recordCnt > 0) {
          hOldErrAmt = getValueDouble("h_old_err_amt");
      }

  }

  /*************************/
  /* subroutine start here */
  /*************************/
  /***********************************************************************/
  void selectActPayError() throws Exception {
      sqlCmd = "select sum(decode(decode(curr_code, '','901',curr_code),'901',pay_amt,dc_pay_amt)) h_old_err_amt ";
      sqlCmd += "  from act_pay_error  ";
      sqlCmd += " where batch_no = ?  ";
      sqlCmd += "   and decode(curr_code,'','901',curr_code) = ?  ";
      sqlCmd += "   and  vouch_memo3 = '' ";
      setString(1, hOldBatchNo);
      setString(2, hPcceCurrCode);
      int recordCnt = selectTable();
      if (recordCnt > 0) {
          hOldErrAmt = getValueDouble("h_old_err_amt");
      }
  }


  /***********************************************************************/
  void selectActPayErr1003() throws Exception {
      sqlCmd = "select sum(decode(decode(curr_code, '','901',curr_code),'901',pay_amt,dc_pay_amt)) h_old_err_amt ";
      sqlCmd += " from act_pay_error  ";
      sqlCmd += "where batch_no = ?  ";
      sqlCmd += "and decode(curr_code,'','901',curr_code) = ?  ";
      sqlCmd += "and pay_amt > 0  ";
      sqlCmd += "and vouch_memo3 = '' ";
      setString(1, hOldBatchNo);
      setString(2, hPcceCurrCode);
      int recordCnt = selectTable();
      if (recordCnt > 0) {
          hOldErrAmt = getValueDouble("h_old_err_amt");
      }

    //sqlCmd = "select sum(pay_amt) h_old_minus_amt ";
      sqlCmd = "select sum(decode(decode(curr_code, '','901',curr_code),'901',pay_amt,dc_pay_amt)) h_old_minus_amt ";
      sqlCmd += " from act_pay_error  ";
      sqlCmd += "where batch_no = ?  ";
      sqlCmd += "and decode(curr_code,'','901',curr_code) = ?  ";
      sqlCmd += "and pay_amt < 0  ";
      sqlCmd += "and vouch_memo3 = '' ";
      setString(1, hOldBatchNo);
      setString(2, hPcceCurrCode);
      recordCnt = selectTable();
      if (recordCnt > 0) {
          hOldMinusAmt = getValueDouble("h_old_minus_amt");
      }

  }

  /***********************************************************************/
  void insertVoucherRtn() throws Exception {

    hOldOkAmt  = comcr.commCurrAmt(hPcceCurrCode, hOldOkAmt, 0);
    hOldErrAmt = comcr.commCurrAmt(hPcceCurrCode, hOldErrAmt, 0);

    double callVoucherAmt=0.0;
    String tMemo1 = "";
    String tMemo2 = "";
    String tMemo3 = "";
    String tSysRem = "";

    comcr.hGsvhCurr = hPcceCurrCodeGl;
    comcr.hGsvhModPgm = javaProgram;

    if (hOldBatchNo.substring(8, 12).equals("9001")    /* TCB自行自動扣繳 */
     || hOldBatchNo.substring(8, 12).equals("9002")    /* 花農卡 自動繳款回饋 */
     || hOldBatchNo.substring(8, 12).equals("5555")    /* 還額檔繳款 */
     || hOldBatchNo.substring(8, 12).equals("5556")) { /* 全國繳費網繳款 */
        hVouchCdKind = "E001";
        /** OP --- > Variable **/
        comcr.startVouch(hPccdGlCode, hVouchCdKind);
    } else if (hOldBatchNo.substring(8, 12).equals("1001")    /* 他行代償 */
            || hOldBatchNo.substring(8, 12).equals("1003")    /* 同業代收款 */
            || hOldBatchNo.substring(8, 12).equals("1005")    /* 債務協商 */
            || hOldBatchNo.substring(8, 12).equals("1006")) { /* 前置協商 */
        hVouchCdKind = "E002";
        /** OP --- > Variable **/
        comcr.startVouch(hPccdGlCode, hVouchCdKind);
    } else if (hOldBatchNo.substring(8, 12).equals("1002"))  { /* 他行自動扣繳(含郵局) */
        //他行自動扣繳(含郵局自扣)只取郵局自扣，排除ACH自扣(這部分人工起帳)
        hVouchCdKind = "E003";
        hOldOkFee = hOldOkCnt * 10;
        hOldOkRem = hOldOkAmt - hOldOkFee; 
        hOldOkRem = convAmtDp0r(hOldOkRem);
        comcr.startVouch(hPccdGlCode, hVouchCdKind);
    } else {
        return;
    }

    /*
     * cancel get_bank_chi_name();
     */

    /** OP --- > 1 **/
    /* start_vouch("1",h_vouch_cd_kind.arr); */

    comcr.hGsvhCurr = hPcceCurrCodeGl;
    sqlCmd  = " select ";
    sqlCmd += " gen_sys_vouch.ac_no,";
    sqlCmd += " gen_sys_vouch.dbcr_seq,";
    sqlCmd += " gen_sys_vouch.dbcr,";
    sqlCmd += " gen_acct_m.memo3_kind,";
    sqlCmd += " decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) h_accm_memo3_flag,";
    sqlCmd += " decode(gen_acct_m.dr_flag,'','N',gen_acct_m.dr_flag) h_accm_dr_flag,";
    sqlCmd += " decode(gen_acct_m.cr_flag,'','N',gen_acct_m.cr_flag) h_accm_cr_flag ";
    sqlCmd += "  from gen_sys_vouch,gen_acct_m ";
    sqlCmd += " where std_vouch_cd = ? ";
    sqlCmd += "   and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
    sqlCmd += " order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
    setString(1, hVouchCdKind);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      hGsvhAcNo = getValue("ac_no", i);
      hGsvhDbcrSeq = getValueInt("dbcr_seq", i);
      hGsvhDbcr = getValue("dbcr", i);
      hAccmMemo3Kind = getValue("memo3_kind", i);
      hAccmMemo3Flag = getValue("h_accm_memo3_flag", i);
      hAccmDrFlag = getValue("h_accm_dr_flag", i);
      hAccmCrFlag = getValue("h_accm_cr_flag", i);

      /* Memo 1, Memo 2 */
      comcr.hGsvhMemo1 = "";
      comcr.hGsvhMemo2 = "";
      comcr.hGsvhMemo3 = "";
      comcr.hVoucSysRem = "";
      tMemo1 = "";
      tMemo2 = "";
      tMemo3 = "";
      tSysRem = "";

      if (hGsvhDbcrSeq == 1) {
        callVoucherAmt = hOldOkAmt + hOldErrAmt;

        if (hOldBatchNo.substring(8, 12).equals("1002")) { /* 他行自動扣繳(含郵局自扣) */
          if (hOldBatchNo.substring(12, 15).equals("700")) {
              callVoucherAmt = hOldOkFee;
              tMemo1 = String.format("郵局帳號扣款手續費");
        //} else {
        //    tMemo1 = String.format("ACH帳號扣款");
          }

        } else if (hOldBatchNo.substring(8, 12).equals("1003")    /* 同業代收款 */
                || hOldBatchNo.substring(8, 12).equals("1001")    /* 他行代償 */
                || hOldBatchNo.substring(8, 12).equals("1005")    /* 債務協商 */
                || hOldBatchNo.substring(8, 12).equals("1006")) { /* 前置協商 */
            if (hOldBatchNo.substring(8, 12).equals("1001")) {
                tMemo1 = String.format("他行代償");
            }
            if (hOldBatchNo.substring(8, 12).equals("1003")) {
              //tMemo1 = String.format("同業代收款");
              if (hOldPaymentType.equals("COU2")) {
                tMemo1 = String.format("7-11超商代收卡款 ");
              } else if (hOldPaymentType.equals("COU3")) {
                tMemo1 = String.format("全家超商代收卡款 ");
              } else if (hOldPaymentType.equals("COU5")) {
                tMemo1 = String.format("萊爾富超商代收卡款 ");
              } else if (hOldPaymentType.equals("COU7")) {
                tMemo1 = String.format("農業金庫代收卡款 ");
              } else if (hOldPaymentType.equals("COU8")) {
                tMemo1 = String.format("信聯社代收卡款 ");
              } else {
                tMemo1 = String.format("同業代收款 ");
              }
            }
            if (hOldBatchNo.substring(8, 12).equals("1005")) {
                tMemo1 = String.format("債務協商入帳");
            }
            if (hOldBatchNo.substring(8, 12).equals("1006")) {
                tMemo1 = String.format("前置協商入帳");
            }
        } else if (hOldBatchNo.substring(8, 12).equals("9001")    /* TCB自行自動扣繳 */
                || hOldBatchNo.substring(8, 12).equals("9002")) { /* 花農卡自動繳款 */
            hAmblAutopayAmt += callVoucherAmt;
            if (hOldBatchNo.substring(8, 12).equals("9001")) {
                tMemo1 = String.format("TCB自行自動扣繳");
            }
            if (hOldBatchNo.substring(8, 12).equals("9002")) {
                tMemo1 = String.format("花農卡自動繳款");
            }
        } else if (hOldBatchNo.substring(8, 12).equals("5555")    /* 還額檔繳款 */
                || hOldBatchNo.substring(8, 12).equals("5556")) { /* 全國繳費網繳款 */
            hAmblOnuspayAmt += callVoucherAmt;
            if (hOldBatchNo.substring(8, 12).equals("5555")) {
                tMemo1 = String.format("還額檔繳款");
            }
            if (hOldBatchNo.substring(8, 12).equals("5556")) {
                tMemo1 = String.format("全國繳費網繳款");
            }
        }
        comcr.hGsvhMemo1 = tMemo1;
        /* Memo 2 */
      //tMemo2 = String.format("NO%16.16s", hOldBatchNo);
      //comcr.hGsvhMemo2 = tMemo2;
        tSysRem = String.format("NO%16.16s", hOldBatchNo);
        comcr.hVoucSysRem = tSysRem;
      } else if (hGsvhDbcrSeq == 2) {
        callVoucherAmt = hOldOkAmt;

        if (hOldBatchNo.substring(8, 12).equals("1002")) { /* 它行自動扣繳(含郵局自扣) */
            /* Memo 1 */
            if (hOldBatchNo.substring(12, 15).equals("700")) {
                callVoucherAmt = hOldOkRem + hOldErrAmt;
                tMemo1 = String.format("郵局帳號扣除扣款手續費");
          //} else {
          //    tMemo1 = String.format("ACH帳號扣款");
            }
        } else if (hOldBatchNo.substring(8, 12).equals("1003") /* 同業代收款 */
                || hOldBatchNo.substring(8, 12).equals("1001") /* 他行代償 */
                || hOldBatchNo.substring(8, 12).equals("1006") /* 前置協商 */
                || hOldBatchNo.substring(8, 12).equals("1005")) { /* 債務協商 */
          //callVoucherAmt = hOldMinusAmt * -1;

            if (hOldBatchNo.substring(8, 12).equals("1001")) {
                tMemo1 = String.format("他行代償入帳成功 ");
            }
            if (hOldBatchNo.substring(8, 12).equals("1003")) {
              //tMemo1 = String.format("同業代收款入帳成功 ");
              if (hOldPaymentType.equals("COU2")) {
                tMemo1 = String.format("7-11超商代收卡款入帳成功 ");
              } else if (hOldPaymentType.equals("COU3")) {
                tMemo1 = String.format("全家超商代收卡款入帳成功 ");
              } else if (hOldPaymentType.equals("COU5")) {
                tMemo1 = String.format("萊爾富超商代收卡款入帳成功 ");
              } else if (hOldPaymentType.equals("COU7")) {
                tMemo1 = String.format("農業金庫代收卡款入帳成功 ");
              } else if (hOldPaymentType.equals("COU8")) {
                tMemo1 = String.format("信聯社代收卡款入帳成功 ");
              } else {
                tMemo1 = String.format("同業代收款入帳成功 ");
              }
            }
            if (hOldBatchNo.substring(8, 12).equals("1005")) {
                tMemo1 = String.format("債務協商入帳成功 ");
            }
            if (hOldBatchNo.substring(8, 12).equals("1006")) {
                tMemo1 = String.format("前置協商入帳入帳成功 ");
            }
        } else if (hOldBatchNo.substring(8, 12).equals("9001")    /* TCB自行自動扣繳 */
                || hOldBatchNo.substring(8, 12).equals("9002")) { /* 花農卡自動繳款 */
            hAmblAutopayAmt += callVoucherAmt;
            if (hOldBatchNo.substring(8, 12).equals("9001")) {
                tMemo1 = String.format("TCB自行自動扣繳入帳成功");
            }
            if (hOldBatchNo.substring(8, 12).equals("9002")) {
                tMemo1 = String.format("花農卡自動繳款入帳成功");
            }
        } else if (hOldBatchNo.substring(8, 12).equals("5555")    /* 還額檔繳款 */
                || hOldBatchNo.substring(8, 12).equals("5556")) { /* 全國繳費網繳款 */
            hAmblOnuspayAmt += callVoucherAmt;
            if (hOldBatchNo.substring(8, 12).equals("5555")) {
                tMemo1 = String.format("還額檔繳款入帳成功");
            }
            if (hOldBatchNo.substring(8, 12).equals("5556")) {
                tMemo1 = String.format("全國繳費網繳款入帳成功");
            }
        }
        comcr.hGsvhMemo1 = tMemo1;
        /* Memo 2 */
      //tMemo2 = String.format("NO%16.16s", hOldBatchNo);
      //comcr.hGsvhMemo2 = tMemo2;
        tSysRem = String.format("NO%16.16s", hOldBatchNo);
        comcr.hVoucSysRem = tSysRem;

        /* Memo 3 */
      //tMemo3 = String.format("%6.6s%6.6s%02d", hVouchChiDate, comcr.hVoucRefno, hGsvhDbcrSeq);
      //comcr.hGsvhMemo3 = tMemo3;
      } else if (hGsvhDbcrSeq == 3) {
        callVoucherAmt = hOldErrAmt;
        if (hOldBatchNo.substring(8, 12).equals("1002")) { /* 它行自動扣繳(含郵局自扣) */
            /* Memo 1 */
            if (hOldBatchNo.substring(12, 15).equals("700")) {
                callVoucherAmt = hOldOkAmt;
                tMemo1 = String.format("郵局帳號扣款入帳成功 ");
            }
            comcr.hGsvhMemo1 = tMemo1;

        } else if (hOldBatchNo.substring(8, 12).equals("1003") /* 同業代收款 */
                || hOldBatchNo.substring(8, 12).equals("1001") /* 他行代償  */
                || hOldBatchNo.substring(8, 12).equals("1006") /* 前置協商  */
                || hOldBatchNo.substring(8, 12).equals("1005")) { /* 債務協商  */
          //callVoucherAmt = hOldMinusAmt * -1;

            if (hOldBatchNo.substring(8, 12).equals("1001")) {
                tMemo1 = String.format("他行代償入帳失敗");
            }
            if (hOldBatchNo.substring(8, 12).equals("1003")) {
              //tMemo1 = String.format("同業代收款入帳失敗");
              if (hOldPaymentType.equals("COU2")) {
                tMemo1 = String.format("7-11超商代收卡款入帳失敗 ");
              } else if (hOldPaymentType.equals("COU3")) {
                tMemo1 = String.format("全家超商代收卡款入帳失敗 ");
              } else if (hOldPaymentType.equals("COU5")) {
                tMemo1 = String.format("萊爾富超商代收卡款入帳失敗 ");
              } else if (hOldPaymentType.equals("COU7")) {
                tMemo1 = String.format("農業金庫代收卡款入帳失敗 ");
              } else if (hOldPaymentType.equals("COU8")) {
                tMemo1 = String.format("信聯社代收卡款入帳失敗 ");
              } else {
                tMemo1 = String.format("同業代收款入帳失敗 ");
              }
            }
            if (hOldBatchNo.substring(8, 12).equals("1005")) {
                tMemo1 = String.format("債務協商入帳失敗");
            }
            if (hOldBatchNo.substring(8, 12).equals("1006")) {
                tMemo1 = String.format("前置協商入帳失敗");
            }
        } else if (hOldBatchNo.substring(8, 12).equals("9001")    /* TCB自行自動扣繳 */
                || hOldBatchNo.substring(8, 12).equals("9002")) { /* 花農卡自動繳款 */
            hAmblAutopayAmt += callVoucherAmt;
            if (hOldBatchNo.substring(8, 12).equals("9001")) {
                tMemo1 = String.format("TCB自行自動扣繳入帳失敗");
            }
            if (hOldBatchNo.substring(8, 12).equals("9002")) {
                tMemo1 = String.format("花農卡自動繳款入帳失敗");
            }
        } else if (hOldBatchNo.substring(8, 12).equals("5555")    /* 還額檔繳款 */
                || hOldBatchNo.substring(8, 12).equals("5556")) { /* 全國繳費網繳款 */
            hAmblOnuspayAmt += callVoucherAmt;
            if (hOldBatchNo.substring(8, 12).equals("5555")) {
                tMemo1 = String.format("還額檔繳款入帳失敗");
            }
            if (hOldBatchNo.substring(8, 12).equals("5556")) {
                tMemo1 = String.format("全國繳費網繳款入帳失敗");
            }
        }
        comcr.hGsvhMemo1 = tMemo1;
        /* Memo 2 */
      //tMemo2 = String.format("NO%16.16s", hOldBatchNo);
      //comcr.hGsvhMemo2 = tMemo2;
        tSysRem = String.format("NO%16.16s", hOldBatchNo);
        comcr.hVoucSysRem = tSysRem;

      } else if (hGsvhDbcrSeq == 4) {
      //callVoucherAmt = hOldErrAmt;
        if (hOldBatchNo.substring(8, 12).equals("1002")) { /* 它行自動扣繳(含郵局自扣) */
            /* Memo 1 */
            if (hOldBatchNo.substring(12, 15).equals("700")) {
                callVoucherAmt = hOldErrAmt;
                tMemo1 = String.format("郵局帳號扣款入帳失敗");
            }
        comcr.hGsvhMemo1 = tMemo1;
        /* Memo 2 */
        tMemo2 = String.format("NO%16.16s", hOldBatchNo);
        comcr.hGsvhMemo2 = tMemo2;
        } 
      } else {
      //multipleVoucherRtn(hGsvhAcNo);
        continue;
      }

      if (callVoucherAmt != 0) {
        if (comcr.detailVouch(hGsvhAcNo, hGsvhDbcrSeq, callVoucherAmt, hPcceCurrCode) != 0) {
            comcr.errRtn(String.format("call detail_vouch error"), "", hCallBatchSeqno);
        }
      }
    }
  }

  /***********************************************************************/
/***
  void selectActAcno() throws Exception {
      hAperAcctType = "";
      hAperAcctKey = "";

      sqlCmd = "select acct_type,";
      sqlCmd += " acct_key ";
      sqlCmd += "  from act_acno  ";
      sqlCmd += " where acno_p_seqno = ? ";
      setString(1, hAperPSeqno);
      int recordCnt = selectTable();
      if (notFound.equals("Y")) {
          comcr.errRtn("select_act_acno not found!", "", hCallBatchSeqno);
      }
      if (recordCnt > 0) {
          hAperAcctType = getValue("acct_type");
          hAperAcctKey = getValue("acct_key");
      }

  }
***/
  /***********************************************************************/
  void updateActMasterBal() throws Exception {

      hAmblAutopayAmt = convAmtDp2r(hAmblAutopayAmt);
      hAmblOffsetAmt  = convAmtDp2r(hAmblOffsetAmt);
      hAmblOnuspayAmt = convAmtDp2r(hAmblOnuspayAmt);

      daoTable = "act_master_bal";
      updateSQL = " autopay_amt = ?,";
      updateSQL += " offset_amt   = ? ,";
      updateSQL += " onuspay_amt = ?,";
      updateSQL += " mod_time    = sysdate,";
      updateSQL += " mod_pgm     = 'ActE009'";
      whereStr = "where check_date = ?  ";
      whereStr += "and decode(curr_code,'','901',curr_code)  = ? ";
      setDouble(1, hAmblAutopayAmt);
      setDouble(2, hAmblOffsetAmt);
      setDouble(3, hAmblOnuspayAmt);
      setString(4, hBusiBusinessDate);
      setString(5, hPcceCurrCode);
      updateTable();
      if (notFound.equals("Y")) {
          setValue("check_date", hBusiBusinessDate);
          setValueDouble("autopay_amt", hAmblAutopayAmt);
          setValueDouble("offset_amt", hAmblOffsetAmt);
          setValueDouble("onuspay_amt", hAmblOnuspayAmt);
          setValue("curr_code", hPcceCurrCode);
          setValue("mod_user", hAdclModUser);
          setValue("mod_time", sysDate + sysTime);
          setValue("mod_pgm", "ActE009");
          daoTable = "act_master_bal";
          insertTable();
          if (dupRecord.equals("Y")) {
              comcr.errRtn("insert_act_master_bal duplicate!", "", hCallBatchSeqno);
          }

      }
  }

  /***********************************************************************/
  void selectPtrCurrcode() throws Exception {
    sqlCmd = "select curr_chi_name,";
    sqlCmd += " curr_code,";
    sqlCmd += " curr_code_gl ";
    sqlCmd += "  from ptr_currcode  ";
    sqlCmd += " where bill_sort_seq != '' ORDER BY bill_sort_seq ";
    int recordCnt = selectTable();
    if (recordCnt > 100) {
       comcr.errRtn("select complied curr_codes exceeds 100 !", "", hCallBatchSeqno);
    }

    for (int i = 0; i < recordCnt; i++) {
        aPcceCurrChiName[i] = getValue("curr_chi_name", i);
        aPcceCurrCode[i] = getValue("curr_code", i);
        aPcceCurrCodeGl[i] = getValue("curr_code_gl", i);
    }

    ptrCurrcodeCnt = recordCnt;

  }

  /*** conv_amt_dp0r(x) 有以下兩點作用：
  1.校正微小誤差：double 變數運算可能後會發生 .99999999...的問題，例如 19.125, 
    實際會變成 19.1249999999999999...，所以執行 conv_amt_dp0r(x)變成 19.00
  2.四捨五入到整數位
  ***/
  public double  convAmtDp0r(double cvtAmt) throws Exception
  {
    long   cvtLong   = (long) Math.round(cvtAmt + 0.000001);
    double cvtDouble =  ((double) cvtLong);
    return cvtDouble;
  }

  /*** conv_amt_dp2r(x) 有以下兩點作用：
  1.校正微小誤差：double 變數運算可能後會發生 .99999999...的問題，例如 19.125, 
    實際會變成 19.1249999999999999...，所以執行 conv_amt_dp2r(x)變成 19.13
  2.四捨五入到小數以下第二位
  ***/
  public double  convAmtDp2r(double cvtAmt) throws Exception
  {
    long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
    double cvtDouble =  ((double) cvtLong) / 100;
    return cvtDouble;
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {

      ActE009 proc = new ActE009();
      int retCode = proc.mainProcess(args);
      proc.programEnd(retCode);
  }
}

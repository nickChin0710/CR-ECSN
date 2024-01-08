/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version   AUTHOR                       DESCRIPTION             *
 *  ---------  --------- --------- ------------------------------------------  *
 *  112/07/30  V1.00.01  Simon     TCB initial ACT_F*會計起帳                  * 
 *  112/08/09  V1.00.02  Simon     1.雜項費用、交易手續費合併為雜項手續費      * 
 *                                 2.處理D檔催收款項                           * 
 *  112/09/18  V1.00.03  Simon     繳款入帳、繳款銷帳、調整銷帳會計分錄套號第3碼固定為"2"* 
 ******************************************************************************/

package Act;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡人會計起帳(ACT_F*)處理*/
public class ActV101 extends AccessDAO {

  public final boolean DEBUG_MODE = false;

  private String PROGNAME = "卡人會計起帳(ACT_F*)處理  112/09/18 V1.00.03";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  final int DEBUG = 1;

  int ptrCurrcodeCnt = 0;
  String[] aPcceCurrChiName = new String[100];
  String[] aPcceCurrCode = new String[100];
  String[] aPcceCurrCodeGl = new String[100];

  String hCallBatchSeqno = "";
  String hBusiBusinessDate = "";
  String hTempVouchDate = "";
  String hTempVouchChiDate = "";
  String hBusiVouchDate = "";
  String hAvdaJobCode = "";
  String hAvdaProcStage = "";
  String hAvdaAcctCode = "";
  double hAvdaVouchAmt = 0;
  double hAvdaDVouchAmt = 0;
  String hVouchCdKind = "";
  String hGsvhAcNo = "";
  String hGsvhDbcr = "";

  String hAccmMemo3Kind = "";
  String hAccmMemo3Flag = "";
  String hAccmDrFlag = "";
  String hAccmCrFlag = "";

  int totalCnt = 0;
  String tmpstr = "";
  String hPcceCurrCode = "";
  String hPcceCurrCodeGl = "";
  String hPccdGlcode = "";
  int hGsvhDbcrSeq = 0;
  double callVoucherAmt = 0;
  String chiDate = "";
  String tMemo3 = "";
  String tMemo2 = "";
  String tMemo1 = "";
  String hPccdClassCode = "";

  private int maxVouchAmtLength = 50;
  double[] vouchAmt = new double[maxVouchAmtLength];

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
          comc.errExit("Usage : ActV101, this program need only one parameter  ", "");
      }

      // 固定要做的

      if (!connectDataBase()) {
          comc.errExit("connect DataBase error", "");
      }

      hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

      selectPtrBusinday();
      showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));
      /******************************/

      selectPtrCurrcode();

      for (int inti = 0; inti < ptrCurrcodeCnt; inti++) {
        hPcceCurrCode = aPcceCurrCode[inti];
        hPcceCurrCodeGl = aPcceCurrCodeGl[inti];

        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("幣別：[%s]會計分錄開始.......",hPcceCurrCode));
        showLogMessage("I", "", String.format("一般D檔循環息會計分錄開始......."));
        comcr.vouchPageCnt = 0;
        comcr.rptSeq         = 0;
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData01();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));

        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("一般D檔違約金會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData02();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));

        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("一般D檔年費會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData03();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));

        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("一般D檔雜項手續費會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData04();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));

        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("一般D檔掛失費會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData05();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));
/***
        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("一般D檔交易手續費會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData06();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));
***/
        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("一般D檔預借現金手續費會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData07();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));

        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("溢付款費提領轉帳會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData08();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));

        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("一般D檔催收款會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData09();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));

        showLogMessage("I", "", String.format("*******************************"));
      }
      updateActVouchData();

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
    hTempVouchDate = "";
    hTempVouchChiDate = "";
    sqlCmd = "select business_date,";
    sqlCmd += " vouch_date,";
    sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'0000000'),2,7) h_temp_vouch_chi_date,";
    sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'00000000'),4,6) h_busi_vouch_date ";
    sqlCmd += " from ptr_businday ";
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
        hBusiBusinessDate = getValue("business_date");
        hTempVouchDate = getValue("vouch_date");
        hTempVouchChiDate = getValue("h_temp_vouch_chi_date");
        hBusiVouchDate = getValue("h_busi_vouch_date");
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

  /***********************************************************************/
  void selectActVouchData01() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " proc_stage,";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; 
    sqlCmd += " d_vouch_amt "; 
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActF001' ";
    sqlCmd += "  and acct_code = 'RI' ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaProcStage = getValue("proc_stage", i);
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaJobCode = getValue("job_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;
    }

    insertVoucherRtn(1, hPcceCurrCodeGl);

  }

  /***********************************************************************/
  void selectActVouchData02() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " proc_stage,";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; 
    sqlCmd += " d_vouch_amt "; 
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActF001' ";
    sqlCmd += "  and acct_code = 'PN' ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaProcStage = getValue("proc_stage", i);
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaJobCode = getValue("job_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;
    }

    insertVoucherRtn(2, hPcceCurrCodeGl);

  }

  /***********************************************************************/
  void selectActVouchData03() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " proc_stage,";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; 
    sqlCmd += " d_vouch_amt "; 
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActF001' ";
    sqlCmd += "  and acct_code = 'AF' ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaProcStage = getValue("proc_stage", i);
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaJobCode = getValue("job_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;
    }

    insertVoucherRtn(3, hPcceCurrCodeGl);

  }

  /***********************************************************************/
  void selectActVouchData04() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " proc_stage,";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; 
    sqlCmd += " d_vouch_amt "; 
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActF001' ";
    sqlCmd += "  and acct_code = 'PF' ";
  //sqlCmd += "  and proc_stage != '4' ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaProcStage = getValue("proc_stage", i);
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaJobCode = getValue("job_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;
    }

    insertVoucherRtn(4, hPcceCurrCodeGl);

  }

  /***********************************************************************/
  void selectActVouchData05() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " proc_stage,";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; 
    sqlCmd += " d_vouch_amt "; 
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActF001' ";
    sqlCmd += "  and acct_code = 'LF' ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaProcStage = getValue("proc_stage", i);
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaJobCode = getValue("job_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;
    }

    insertVoucherRtn(5, hPcceCurrCodeGl);

  }

  /***********************************************************************/
/***
  void selectActVouchData06() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " proc_stage,";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; 
    sqlCmd += " d_vouch_amt "; 
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActF001' ";
    sqlCmd += "  and acct_code = 'PF' ";
    sqlCmd += "  and proc_stage = '4' ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaProcStage = getValue("proc_stage", i);
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaJobCode = getValue("job_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;
    }

    insertVoucherRtn(6, hPcceCurrCodeGl);

  }
***/
  /***********************************************************************/
  void selectActVouchData07() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " proc_stage,";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; 
    sqlCmd += " d_vouch_amt "; 
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActF001' ";
    sqlCmd += "  and acct_code = 'CF' ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaProcStage = getValue("proc_stage", i);
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaJobCode = getValue("job_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;
    }

    insertVoucherRtn(7, hPcceCurrCodeGl);

  }

  /***********************************************************************/
  void selectActVouchData08() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " proc_stage,";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; 
    sqlCmd += " d_vouch_amt "; 
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActF004' ";
    sqlCmd += "  and payment_type = 'OP02' ";
    sqlCmd += "  and proc_stage = '1' ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaProcStage = getValue("proc_stage", i);
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaJobCode = getValue("job_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;
    }

    insertVoucherRtn(8, hPcceCurrCodeGl);

  }

  /***********************************************************************/
  void selectActVouchData09() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " proc_stage,";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; 
    sqlCmd += " d_vouch_amt "; 
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActF001' ";
    sqlCmd += "  and acct_code in ('CB','CI','CC') ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaProcStage = getValue("proc_stage", i);
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaJobCode = getValue("job_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;
    }

    insertVoucherRtn(9, hPcceCurrCodeGl);

  }

  /***********************************************************************/
  void insertVoucherRtn(int index, String idxCurr) throws Exception {

    for (int int1 = 1; int1 < maxVouchAmtLength; int1++)
      vouchAmt[int1] = comcr.commCurrAmt(hPcceCurrCode, vouchAmt[int1], 0);

    comcr.hGsvhCurr = idxCurr;

    chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

    hAvdaJobCode = "02";

    switch (index) {
    case 1:/* 一般D檔循環息會計分錄 */
      hVouchCdKind = "F001";
      comcr.startVouch(comc.getSubString(hAvdaJobCode, 1,2), hVouchCdKind);
      break;
    case 2:/* 一般D檔違約金會計分錄 */
      hVouchCdKind = "F002";
    //selectPtrDeptCode("A401");
    //comcr.startVouch(hPccdGlcode, hVouchCdKind);
      comcr.startVouch(comc.getSubString(hAvdaJobCode, 1,2), hVouchCdKind);
      break;
    case 3:/* 一般D檔年費會計分錄 */
      hVouchCdKind = "F003";
      comcr.startVouch(comc.getSubString(hAvdaJobCode, 1,2), hVouchCdKind);
      break;
    case 4:/* 一般D檔雜項手續費會計分錄 */
      hVouchCdKind = "F004";
      comcr.startVouch(comc.getSubString(hAvdaJobCode, 1,2), hVouchCdKind);
      break;
    case 5:/* 一般D檔掛失費會計分錄 */
      hVouchCdKind = "F005";
      comcr.startVouch(comc.getSubString(hAvdaJobCode, 1,2), hVouchCdKind);
      break;
  //case 6:/* 一般D交易手續費會計分錄 */
  //  hVouchCdKind = "F006";
  //  comcr.startVouch(comc.getSubString(hAvdaJobCode, 1,2), hVouchCdKind);
  //  break;
    case 7:/* 一般D檔預借現金手續費會計分錄 */
      hVouchCdKind = "F007";
      comcr.startVouch(comc.getSubString(hAvdaJobCode, 1,2), hVouchCdKind);
      break;
    case 8:/* 溢付款提領轉帳會計分錄 */
      switch (hAvdaProcStage) {
        case "1":/* 系統隔日轉入本行帳號 */
          hVouchCdKind = "F008";
          comcr.startVouch(comc.getSubString(hAvdaJobCode, 1,2), hVouchCdKind);
          break;
        }
      break;
    case 9:/* 一般D檔催收款會計分錄 */
      hVouchCdKind = "F009";
      comcr.startVouch(comc.getSubString(hAvdaJobCode, 1,2), hVouchCdKind);
      break;
    default: 
      break;
    }

    tmpstr = String.format("ActV101_%d.%s_%s", index, hVouchCdKind, idxCurr);
    comcr.hGsvhModPgm = tmpstr;

    sqlCmd = "select ";
    sqlCmd += " gen_sys_vouch.ac_no,";
    sqlCmd += " gen_sys_vouch.dbcr_seq,";
    sqlCmd += " gen_sys_vouch.dbcr,";
    sqlCmd += " gen_acct_m.memo3_kind,";
    sqlCmd += " decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) h_accm_memo3_flag,";
    sqlCmd += " decode(gen_acct_m.dr_flag,'','N',gen_acct_m.dr_flag) h_accm_dr_flag,";
    sqlCmd += " decode(gen_acct_m.cr_flag,'','N',gen_acct_m.cr_flag) h_accm_cr_flag ";
    sqlCmd += " from gen_sys_vouch,gen_acct_m ";
    sqlCmd += "where std_vouch_cd = ? ";
    sqlCmd += "  and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
    sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
    setString(1, hVouchCdKind);
    int recordCnt1 = selectTable();
    for (int i = 0; i < recordCnt1; i++) {
      hGsvhAcNo = getValue("ac_no", i);
      hGsvhDbcrSeq = getValueInt("dbcr_seq", i);
      hGsvhDbcr = getValue("dbcr", i);
      hAccmMemo3Kind = getValue("memo3_kind", i);
      hAccmMemo3Flag = getValue("h_accm_memo3_flag", i);
      hAccmDrFlag = getValue("h_accm_dr_flag", i);
      hAccmCrFlag = getValue("h_accm_cr_flag", i);

      /* Memo 1, Memo 2, Memo3 */
      comcr.hGsvhMemo1 = "";
      comcr.hGsvhMemo2 = "";
      comcr.hGsvhMemo3 = "";
      tMemo1 = "";
      tMemo2 = "";
      tMemo3 = "";

      switch (index) {
      case 1:/*一般D檔循環息*/
        callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
        if (hGsvhDbcrSeq == 1) {
          tMemo1 = String.format("D檔循環息");
        } else if (hGsvhDbcrSeq == 2) {
          tMemo1 = String.format("D檔循環息入帳");
        }
        comcr.hGsvhMemo1 = tMemo1;
        break;
      case 2:/*一般D檔違約金*/
        callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
        if (hGsvhDbcrSeq == 1) {
          tMemo1 = String.format("D檔違約金");
        } else if (hGsvhDbcrSeq == 2) {
          tMemo1 = String.format("D檔違約金入帳");
        }
        comcr.hGsvhMemo1 = tMemo1;
        break;
      case 3:/*一般D檔年費*/
        callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
        if (hGsvhDbcrSeq == 1) {
          tMemo1 = String.format("D檔年費");
        } else if (hGsvhDbcrSeq == 2) {
          tMemo1 = String.format("D檔年費入帳");
        }
        comcr.hGsvhMemo1 = tMemo1;
        break;
      case 4:/*一般D檔雜項手續費*/
        callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
        if (hGsvhDbcrSeq == 1) {
          tMemo1 = String.format("D檔雜項手續費");
        } else if (hGsvhDbcrSeq == 2) {
          tMemo1 = String.format("D檔雜項手續費入帳");
        }
        comcr.hGsvhMemo1 = tMemo1;
        break;
      case 5:/*一般D檔掛失費 */
        callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
        if (hGsvhDbcrSeq == 1) {
          tMemo1 = String.format("D檔掛失費");
        } else if (hGsvhDbcrSeq == 2) {
          tMemo1 = String.format("D檔掛失費入帳");
        }
        comcr.hGsvhMemo1 = tMemo1;
        break;
    //case 6:/*一般D檔交易手續費*/
    //  callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
    //  tMemo1 = String.format("D檔交易手續費");
    //  comcr.hGsvhMemo1 = tMemo1;
    //  break;
      case 7:/*一般D檔預借現金手續費 */
        callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
        if (hGsvhDbcrSeq == 1) {
          tMemo1 = String.format("D檔預借現金手續費");
        } else if (hGsvhDbcrSeq == 2) {
          tMemo1 = String.format("D檔預借現金手續費入帳");
        }
        comcr.hGsvhMemo1 = tMemo1;
        break;
      case 8:/*溢付款提領轉帳 */
        callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
        if (hGsvhDbcrSeq == 1) {
          tMemo1 = String.format("溢付款提領轉帳");
        } else if (hGsvhDbcrSeq == 2) {
          tMemo1 = String.format("溢付款提領轉帳入帳");
        }
        comcr.hGsvhMemo1 = tMemo1;
        break;
      case 9:/*一般D檔催收款項 */
        callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
        if (hGsvhDbcrSeq == 1) {
          tMemo1 = String.format("D檔催收款項");
        } else if (hGsvhDbcrSeq == 2) {
          tMemo1 = String.format("D檔催收款項入帳");
        }
        comcr.hGsvhMemo1 = tMemo1;
        break;
      }

      if (callVoucherAmt != 0) {
        if (comcr.detailVouch(hGsvhAcNo, hGsvhDbcrSeq, callVoucherAmt, hPcceCurrCode) != 0) {
            comcr.errRtn(String.format("call detail_vouch error"), "", hCallBatchSeqno);
        }
      }
    }

  }

  /***********************************************************************/
  void selectPtrDeptCode(String deptCode) throws Exception {
    hPccdClassCode = deptCode;
    hPccdGlcode = "";
    sqlCmd = "select gl_code ";
    sqlCmd += " from ptr_dept_code  ";
    sqlCmd += "where dept_code = ? ";
    setString(1, hPccdClassCode);
    int recordCnt1 = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_dept_code not found!", "", hCallBatchSeqno);
    }
    if (recordCnt1 > 0) {
        hPccdGlcode = getValue("gl_code");
    }

  }

  /***********************************************************************/
  void updateActVouchData() throws Exception {
    daoTable = "act_vouch_data";
    updateSQL = " proc_flag = 'Y',";
  //updateSQL += " proc_date = to_char(sysdate, 'yyyymmdd')";
    updateSQL += " proc_date = ? ";
    whereStr = "where proc_flag = 'N'  ";
    whereStr += "  and src_pgm like 'ActF%' ";
    setString(1, hBusiBusinessDate);
    updateTable();
  }

  /*****************************************************************************/
  double resetFeeAmt(double begAmt) throws Exception {
    double tempDouble, lastDouble;
    // last_double = comcr.commCurrAmt(h_avda_curr_code, (beg_amt / 1.05),
    // 0);
    lastDouble = begAmt / 1.05;
    tempDouble = begAmt - lastDouble;
    return (tempDouble);
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {

    ActV101 proc = new ActV101();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

}

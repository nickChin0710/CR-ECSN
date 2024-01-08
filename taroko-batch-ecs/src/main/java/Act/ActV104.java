/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ---------- -----------------------------------------  *
 *  112/08/09  V1.00.01  Simon      1.TCB initial 一般繳款銷帳會計起帳         * 
 *                                  2.催收款繳款銷帳會計起帳                   * 
 *                                  3.呆帳繳款銷帳會計起帳                     * 
 *  112/08/18  V1.00.02  Simon      1.一般繳款銷帳會計起帳需包含法訴費         * 
 *                                  2.取消呆帳繳款銷帳會計起帳                 * 
 *  112/09/18  V1.00.03  Simon      1.繳款入帳、繳款銷帳、調整銷帳會計分錄套號第3碼固定為"2"* 
 *                                  2.非催、呆戶之"還款銷帳"更改為"繳款銷帳"   * 
 ******************************************************************************/

package Act;

import java.util.Arrays;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡人繳款銷帳會計起帳處理*/
public class ActV104 extends AccessDAO {

  private String progname = "卡人繳款銷帳會計起帳處理  112/09/18  V1.00.03  ";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  String hCallBatchSeqno = "";

  int ptrCurrcodeCnt = 0;
  String[] aPcceCurrChiName = new String[100];
  String[] aPcceCurrCode = new String[100];
  String[] aPcceCurrCodeGl = new String[100];

  int totalCnt = 0;
  String tmpstr = "";
  String hPcceCurrCode = "";
  String hPcceCurrCodeGl = "";
  String hPccdGlcode = "";
  int hGsvhDbcrSeq = 0;
  double callVoucherAmt = 0;

  String hBusiBusinessDate = "";
  String hBusiVouchDate = "";
  String hTempVouchChiDate = "";
  String hAvdaCurrCode = "";
  double hAvdaVouchAmt = 0;
  double hMAvdaVouchAmt = 0;
  double hAvdaDVouchAmt = 0;
  String hVouchCdKind = "";
  String hGsvhAcNo = "";
  String hGsvhDbcr = "";
  String hAccmMemo3Kind = "";
  String hAccmMemo3Flag = "";
  String hAccmDrFlag = "";
  String hAccmCrFlag = "";
  String hPccdClassCode = "";
  String tMemo3 = "";
  String tMemo2 = "";
  String tMemo1 = "";

  String hAvdaAcctCode = "";

  private int maxVouchAmtLength = 50;
  double[] vouchAmt = new double[maxVouchAmtLength];

  public int mainProcess(String[] args) {

    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);
      // =====================================
      if (args.length > 1) {
          comc.errExit("Usage : ActV104, this program need only one parameter ", "");
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

      selectPtrCurrcode();

      for (int inti = 0; inti < ptrCurrcodeCnt; inti++) {
        hPcceCurrCode = aPcceCurrCode[inti];
        hPcceCurrCodeGl = aPcceCurrCodeGl[inti];

        showLogMessage("I", "", String.format("*******************************"));
        showLogMessage("I", "", String.format("幣別：[%s]會計分錄開始.......",hPcceCurrCode));
        showLogMessage("I", "", String.format("一般繳款銷帳會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData01();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));

        showLogMessage("I", "", String.format("催收款繳款銷帳會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData02();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));
/***
        showLogMessage("I", "", String.format("呆帳繳款銷帳會計分錄開始......."));
        totalCnt = 0;
        for (int int1 = 0; int1 < 50; int1++) {
          vouchAmt[int1] = 0;
        }
        selectActVouchData03();
        showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));
***/
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
      hBusiVouchDate = "";
      hTempVouchChiDate = "";
      sqlCmd = "select business_date,";
      sqlCmd += " vouch_date,";
      sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'0000000'),2,7) h_temp_vouch_chi_date ";
      sqlCmd += " from ptr_businday ";
      int recordCnt = selectTable();
      if (notFound.equals("Y")) {
          comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
      }
      if (recordCnt > 0) {
          hBusiBusinessDate = getValue("business_date");
          hBusiVouchDate = getValue("vouch_date");
          hTempVouchChiDate = getValue("h_temp_vouch_chi_date");
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
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; /* v+w */
    sqlCmd += " d_vouch_amt "; /* v */
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActE004' ";
    sqlCmd += "  and acct_code not in ('CB','CI','CC','DB') ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      if (Arrays.asList("BL","CA","IT","OT","ID","AO").contains(hAvdaAcctCode)) {
        vouchAmt[2] += hAvdaVouchAmt;
      } if (Arrays.asList("RI").contains(hAvdaAcctCode) ) {
        vouchAmt[3] += hAvdaVouchAmt;
      } if (Arrays.asList("PN","PF","CF","AF","LF","SF").contains(hAvdaAcctCode) ) {
        vouchAmt[4] += hAvdaVouchAmt;
      } 

    }

    insertVoucherRtn(1, hPcceCurrCodeGl);

  }

  /***********************************************************************/
  void selectActVouchData02() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; /* v+w */
    sqlCmd += " d_vouch_amt "; /* v */
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActE004' ";
    sqlCmd += "  and acct_code in ('CB','CI','CC') ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaAcctCode = getValue("acct_code", i);
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
    sqlCmd += " acct_code,";
    sqlCmd += " job_code,";
    sqlCmd += " vouch_amt,"; /* v+w */
    sqlCmd += " d_vouch_amt "; /* v */
    sqlCmd += " from act_vouch_data ";
    sqlCmd += "where vouch_data_type  = '1'     ";
    sqlCmd += "  and proc_flag        = 'N' ";
    sqlCmd += "  and src_pgm          = 'ActE004' ";
    sqlCmd += "  and acct_code in ('DB') ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hPcceCurrCode);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      totalCnt++;
      hAvdaAcctCode = getValue("acct_code", i);
      hAvdaVouchAmt = getValueDouble("vouch_amt", i);
      hAvdaDVouchAmt = getValueDouble("d_vouch_amt", i);
      vouchAmt[1] += hAvdaVouchAmt;
      vouchAmt[2] += hAvdaVouchAmt;

    }

    insertVoucherRtn(3, hPcceCurrCodeGl);

  }

  /***********************************************************************/
  public double convAmt(double cvtAmt) throws Exception
  {
      long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
      double cvtDouble =  ((double) cvtLong) / 100;
      return cvtDouble;
  }

  /***********************************************************************/
  void insertVoucherRtn(int index, String idxCurr) throws Exception {

    for (int int1 = 1; int1 < maxVouchAmtLength; int1++)
      vouchAmt[int1] = comcr.commCurrAmt(hPcceCurrCode, vouchAmt[int1], 0);

    double callVoucherAmt = 0;
    String chiDate = "";

    comcr.hGsvhCurr = idxCurr;
    comcr.hGsvhModPgm = javaProgram;

    chiDate = String.format("%07d", comcr.str2long(hBusiVouchDate) - 19110000);
  //selectPtrDeptCode("A401");

    hPccdGlcode = "2";

    switch (index) {
      case 1: /* 一般銷帳 */
        hVouchCdKind = "G001";
        comcr.startVouch(hPccdGlcode, hVouchCdKind);
        break;
      case 2: /* 催收款銷帳 */
        hVouchCdKind = "G002";
        comcr.startVouch(hPccdGlcode, hVouchCdKind);
        break;
      case 3: /* 呆帳銷帳 */
        hVouchCdKind = "G003";
        comcr.startVouch(hPccdGlcode, hVouchCdKind);
        break;
      default: 
        break;
    }

    sqlCmd = "select ";
    sqlCmd += " gen_sys_vouch.ac_no,";
    sqlCmd += " gen_sys_vouch.dbcr_seq,";
    sqlCmd += " gen_sys_vouch.dbcr,";
    sqlCmd += " gen_acct_m.memo3_kind,";
    sqlCmd += " decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) h_accm_memo3_flag,";
    sqlCmd += " decode(gen_acct_m.dr_flag,'','N',gen_acct_m.dr_flag) h_accm_dr_flag,";
    sqlCmd += " decode(gen_acct_m.cr_flag,'','N',gen_acct_m.cr_flag) h_accm_cr_flag ";
    sqlCmd += " from gen_sys_vouch, gen_acct_m ";
    sqlCmd += "where std_vouch_cd = ? ";
    sqlCmd += "  and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
    sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
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

      /* Memo 1, Memo 2, Memo3 */
      comcr.hGsvhMemo1 = "";
      comcr.hGsvhMemo2 = "";
      comcr.hGsvhMemo3 = "";
      tMemo1 = "";
      tMemo2 = "";
      tMemo3 = "";

      switch (index) {
        case 1:
          callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
          if (hGsvhDbcrSeq == 1) {
            tMemo1 = String.format("繳款銷帳");
          } else if (hGsvhDbcrSeq == 2) {
            tMemo1 = String.format("本金繳款銷帳入帳");
          } else if (hGsvhDbcrSeq == 3) {
            tMemo1 = String.format("循環信用息繳款銷帳入帳");
          } else if (hGsvhDbcrSeq == 4) {
            tMemo1 = String.format("各項費用繳款銷帳入帳");
          }
          comcr.hGsvhMemo1 = tMemo1;
          break;
        case 2:
          callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
          if (hGsvhDbcrSeq == 1) {
            tMemo1 = String.format("催收款還款銷帳");
          } else if (hGsvhDbcrSeq == 2) {
            tMemo1 = String.format("催收款還款銷帳入帳");
          }
          comcr.hGsvhMemo1 = tMemo1;
          break;
        case 3:
          callVoucherAmt = vouchAmt[hGsvhDbcrSeq];
          if (hGsvhDbcrSeq == 1) {
            tMemo1 = String.format("呆帳還款銷帳");
          } else if (hGsvhDbcrSeq == 2) {
            tMemo1 = String.format("呆帳還款銷帳入帳");
          }
          comcr.hGsvhMemo1 = tMemo1;
          break;
        default: 
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
      int recordCnt = selectTable();
      if (notFound.equals("Y")) {
          comcr.errRtn("select_ptr_dept_code not found!", "", hCallBatchSeqno);
      }
      if (recordCnt > 0) {
          hPccdGlcode = getValue("gl_code");
      }

  }

  /***********************************************************************/
  void updateActVouchData() throws Exception {
      daoTable = " act_vouch_data";
      updateSQL = " proc_flag = 'Y',";
      updateSQL += " proc_date = to_char(sysdate, 'yyyymmdd')";
      whereStr = "where proc_flag  = 'N'  ";
      whereStr += " and src_pgm    = 'ActE004' ";
      updateTable();

  }

  /*****************************************************************************/
  double resetFeeAmt(double begAmt) throws Exception {
      double tempDouble, lastDouble;
      lastDouble = comcr.commCurrAmt(hAvdaCurrCode, (begAmt / 1.05), 0);
      tempDouble = begAmt - lastDouble;
      return (tempDouble);
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
      ActV104 proc = new ActV104();
      int retCode = proc.mainProcess(args);
      proc.programEnd(retCode);
  }

}

/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  108/06/19  V1.09.01    黃繼民     RECS-s1070827-074 ACH新規格:cfee, noteb  *
 *  109/02/07  V1.09.01    Brian     update to V1.09.01                        *
 *  110/05/21  V1.09.04    Simon     fix2 vulnerability Absolute Path Traversal*
 *  111/10/12  V1.09.05  jiangyigndong  updated for project coding standard    *
 *  111/12/02  V1.00.06    Simon     1.not self-acting to ftp , MFT will transfer it to post office*
 *                                   2.apply to TCB data constant              *
 *                                   3.apply to TCB filename rules             *
 *  112/03/30  V1.00.07    Simon     add procFTP                               *
 *  112/04/18  V1.00.08    mhung3    exclude act_acno.fl_flag='Y'              *
 *  112/05/22  V1.00.09    Simon     1.removed selectActAchDtl()               *
 *                                   2.控制0筆不產生檔案                       *
 *  112/06/28  V1.00.10    mHung3    "and b.fl_flag <> 'Y' " 更改為 "and b.fl_flag = '' in selectActAcno() " *
 *  112/07/11  V1.00.11    Simon     1.selectActAcno() 新增 and c.autopay_acct_no  != '' *
 *                                   2.ref_ip_code="NCR2TCB" changed into "CRDATACREA" *
 *  112/10/17  V1.00.12    Simon     output檔更改為有換行                      *
 *  112/10/18  V1.00.13    Simon     因轉檔未提供 autopay_id(空值), 以卡戶個人id取代送扣*
 *  112/10/25  V1.00.14    Simon     固定搬tcb統編 "99629524" 至自扣媒體檔     *
 *  112/11/16  V1.00.15    Simon     tcb 要求催、呆戶不送自扣                  *
 *  112/12/02  V1.00.16    Simon     accept consecutive-flag                   *
/******************************************************************************/

package Act;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.*;

/*產生他行自動扣繳檔程式*/
public class ActH008 extends AccessDAO {
    private final boolean DEBUG = false;
    private final String PROGNAME = "產生他行自動扣繳檔程式  112/12/02  V1.00.16";
  	private final static String MEDIA_FOLDER = "/media/act/";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActH008";
    String rptName1 = "";
    int recordCnt = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq1 = 0;
    int errCnt = 0;
    String ErrMsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String ecsServer = "";
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    // String h_mod_ws = "";
    // String h_mod_log = "";
    String hCallBatchSeqno = "";
    String iPostDate = "";

    String hTempUser = "";
    String nextVouch = "";
    String hSystemTime = "";
    String hBusiBusinessDate = "";
    String hConsecutive = "";
    String hTempDeductDate = "";
    String hSysdate = "";
    String hSystime = "";
    String hAcctPSeqno = "";
    String hAcctAcctType = "";
    String hAcctAcctKey = "";
    String hAcnoStmtCycle = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoCardIndicator = "";
    String hAcnoCorpActFlag = "";
    String hAcnoAutopayAcctNo = "";
    String hAcnoAutopayId = "";
    String hAcnoAutopayAcctSDate = "";
    String hAcnoAutopayAcctEDate = "";
    String hAcnoAcctStatus = "";
    String hAcnoNewCycleMonth = "";
    double hAcctAcctJrnlBal = 0;
    double hAcctAdiEndBal = 0;
    double hAcctAutopayBal = 0;
    double hAcctTtlAmtBal = 0;
    double hAcurDcTtlAmt = 0;
    double hAcurDcAdjustDrAmt = 0;
    double hAcurDcAdjustCrAmt = 0;
    double hAcurDcPayAmt = 0;
    double hJrnlDcTransactionAmt = 0;
    double hNextAdjustDrAmt = 0;
    double hNextAdjustCrAmt = 0;
    String hAcnoAutopayAcctBank = "";
    String hAchdBankNo = "";
    double hAoayTransactionAmt = 0;
    String hAoayChiName = "";
    String hAcnoModUser = "";
    String hAcnoModPgm = "";
    // String h_acno_mod_ws = "";
    String hIdnoChiName = "";
    String hCorpChiName = "";
    String hAcctType = "";
    String hStmtCycle = "";
    String hPayDayFull = "";
    String hWdayThisAcctMonth = "";
    int hAchDays = 0;
    int testFlag = 0;
    double totAmt = 0;
    int tempInt = 0;
    String hHdayHoliday = "";
    String hNHdayHoliday = "";
    String[] aCheckAcctType = new String[250];
    String[] aCheckStmtCycle = new String[250];
    String[] aWdayThisAcctMonth = new String[250];
    String[] aCheckThisAcctMonth = new String[250];

    String fileNameFull = "";
    int readCnt = 0;
    int totCnt = 0;
    int totCnt3 = 0;
    int chkPnt = 0;
    String hChiDate = "";
    String hAoayEnterAcctDate = "";
    String vChiDate = "";
    String tempDate = "";
    String hCallRProgramCode = "";
    String temstr2 = "";
    String tmpstr1 = "";
    String tmpstrDisp = "";
    String hCallErrorDesc = "";

    BufferedWriter out = null;

    public int mainProcess(String[] args) {

      try {

        // ====================================
        // 固定要做的
        dateTime();
        setConsoleMode("Y");
        javaProgram = this.getClass().getName();
        showLogMessage("I", "", javaProgram + " " + PROGNAME);
        // =====================================
        //if (args.length > 3) {
        //    comc.errExit("Usage : ActH008 [proc_date] [test_flag] [batch_seq]", "");
        //}
        // 固定要做的

        if (!connectDataBase()) {
            comc.errExit("connect DataBase error", "");
        }

        /***
        hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
        hTempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);
        if (hTempUser.length() == 0) {
            hModUser = comc.commGetUserID();
            hTempUser = hModUser;
        }
        ***/

        comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
        hModUser = comc.commGetUserID();
        hAcnoModUser = hModUser;
        hAcnoModPgm = javaProgram;

        /***
        if (args.length > 0 && args[0].length() == 8 ) {
            if (args[0].chars().allMatch( Character::isDigit ) ) {
                //hBusiBusinessDate = sanitizeArg2(args[0]);
                hBusiBusinessDate = args[0];
            }
        }

        if (args.length >= 2)
            testFlag = comcr.str2int(args[1]);
        ***/

      if ( args.length == 1 ) {
      	if (args[0].length()==8) {
          hBusiBusinessDate = args[0]; 
      	} else if (args[0].equalsIgnoreCase("c")) {
          hConsecutive  = "C";
      	} else if (args[0].equals("1")) {
          testFlag  = 1;
        }
      } else if ( args.length == 2 ) {
      	if (args[0].length()==8) {
          hBusiBusinessDate = args[0]; 
      	} 
      	if (args[1].equalsIgnoreCase("c")) {
          hConsecutive  = "C";
      	} else if (args[1].equals("1")) {
          testFlag  = 1;
        } else {
          showLogMessage("I","","PARM 1 : [BUSINESS_DATE]/['c']/['1']");
          showLogMessage("I","","PARM 2 : ['c']/['1']");
          return(0);
        }
      }
      
        selectPtrBusinday();

        nextVouch = comcr.increaseDays(hBusiBusinessDate, 1);
        showLogMessage("I", "", String.format("本日=[%s],次營業日=[%s]", 
        hBusiBusinessDate, nextVouch));
        if (getHolidayDateRtn() == 1) {
            exceptExit = 0;
            comcr.errRtn(String.format("今日為假日, 不需執行 !!"), "", hCallBatchSeqno);
        }

        checkOpen();

        deleteActOtherApay();

        selectCycBankdate();
        if (testFlag == 1) {
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        }

        selectActAcno();
        if (out != null)
            out.close();

      //Temp 預設不傳檔
      //if (testFlag == 0) {
      //    testFlag = 2;                
      //}

/***
        if (testFlag == 0) {
            daoTable = "act_ftp_ctl";
            whereStr = "where file_no  = 'ACHP01'  ";
            whereStr += " and crt_date = ? ";
            setString(1, nextVouch);
            deleteTable();

            setValue("file_no", "ACHP01");
            setValue("crt_date", nextVouch);
            setValue("crt_time", hSystemTime);
            setValueInt("crt_cnt", totCnt);
            setValue("mod_pgm", "ActH008");
            setValue("mod_time", sysDate + sysTime);
            daoTable = "act_ftp_ctl";
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_act_ftp_ctl duplicate!", "", hCallBatchSeqno);
            }

            insertActAutoComfRtn();

            temstr2 = String.format("%s/bk/ECSACHP01_%s", comc.getIBMftp(), nextVouch);
            tmpstr1 = String.format("%s/ECSACHP01", comc.getIBMftp());
            tmpstrDisp = String.format("cp %s  %s ", tmpstr1, temstr2);
            showLogMessage("I", "", tmpstrDisp);
            if (comc.fileCopy(tmpstr1, temstr2) == false)
                showLogMessage("I", "", String.format("無法搬移[%s]", tmpstr1));
            CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
            commFTP.mqSend("ECSACHP01", 250);
        }
***/
        if (totCnt > 0) {
          insertActAutoComfRtn();
          if (testFlag == 0) {
		        String filePath = fileNameFull;
		        procFTP(Paths.get(filePath).getFileName().toString(), filePath);
          } 
        } else {
          showLogMessage("I", "", String.format("沒有ACH自動扣繳資料"));
        }


        comcr.hCallErrorDesc = String.format("程式執行結束,筆數=[%d],[%d]", readCnt, totCnt);
        showLogMessage("I", "", String.format("%s", hCallErrorDesc));

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

        sqlCmd = "select decode( cast(? as varchar(8)) ,'',business_date, ? ) h_busi_business_date,";
        sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)) ,'',business_date, ? ), 'yyyymmdd')+1 days,'yyyymmdd') h_temp_deduct_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_sysdate,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_systime ";
        sqlCmd += "  from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hTempDeductDate = getValue("h_temp_deduct_date");
            hSysdate = getValue("h_sysdate");
            hSystime = getValue("h_systime");
        }

    }

    /***********************************************************************/
    int getHolidayDateRtn() throws Exception {

        sqlCmd = "select count(*) temp_int ";
        sqlCmd += " from ptr_holiday  ";
        sqlCmd += "where holiday = ? ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }
        if (tempInt == 0) {
            return 0;
        }

        return 1;
    }

    /***********************************************************************/
    void deleteActOtherApay() throws Exception {
        daoTable = "act_other_apay";
        whereStr = "where enter_acct_date = ?  ";
        whereStr += " and mod_pgm  = ? ";
      //setString(1, hBusiBusinessDate);
        setString(1, nextVouch);
        setString(2, hAcnoModPgm);
        deleteTable();

    }

    /***********************************************************************/
    void selectCycBankdate() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " acct_type,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " this_lastpay_date,";
        sqlCmd += " this_acct_month,";
        sqlCmd += " ach_days ";
        sqlCmd += "from ptr_workday a, ptr_actgeneral_n b ";
        /*
         * where bank_no in ('004')
         */
        sqlCmd += "order by acct_type, stmt_cycle ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcctType = getValue("acct_type");
            hStmtCycle = getValue("stmt_cycle");
            hPayDayFull = getValue("this_lastpay_date");
            hWdayThisAcctMonth = getValue("this_acct_month");
            hAchDays = getValueInt("ach_days");

            tempDate = hPayDayFull;
            chkHoliday();
            hPayDayFull = tempDate;

            tempDate = comcr.increaseDays(hPayDayFull, 1);
            hPayDayFull = tempDate;
            tempDate = comcr.increaseDays(hPayDayFull, -1 * hAchDays);
            showLogMessage("I", "", String.format("    temp_date [%s]", tempDate));

            if (tempDate.equals(hBusiBusinessDate)) {
                aCheckAcctType[chkPnt] = hAcctType;
                aCheckStmtCycle[chkPnt] = hStmtCycle;
                aCheckThisAcctMonth[chkPnt] = hWdayThisAcctMonth;
                showLogMessage("I", "", String.format("    h_acct_type [%s]", hAcctType));
                showLogMessage("I", "", String.format("    h_stmt_cycle [%s]", hStmtCycle));
                showLogMessage("I", "", String.format("    h_wday_this_acct_month [%s]", hWdayThisAcctMonth));
                showLogMessage("I", "", String.format("    a_check_acct_typechk_pnt [%s]", aCheckAcctType[chkPnt]));
                showLogMessage("I", "", String.format("    a_check_stmt_cycle [%s]", aCheckStmtCycle[chkPnt]));
                showLogMessage("I", "", String.format("    a_check_this_acct_month [%s]", aCheckThisAcctMonth[chkPnt]));
                chkPnt++;
            }

        }
        showLogMessage("I", "", String.format("     Total chk_pnt[%d]", chkPnt));

        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void chkHoliday() throws Exception {
        String hNHdayHoliday = "";

        hNHdayHoliday = tempDate;

        int status = 0;
        while (status == 0) {

            sqlCmd = "select holiday ";
            sqlCmd += " from ptr_holiday  ";
            sqlCmd += "where holiday = ? ";
            setString(1, hNHdayHoliday);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hHdayHoliday = getValue("holiday");

                sqlCmd = "select to_char(to_date( ? ,'yyyymmdd')+ 1 days,'yyyymmdd') as h_n_hday_holiday ";
                sqlCmd += " from dual ";
                setString(1, hNHdayHoliday);
                int recordCnt1 = selectTable();
                if (recordCnt1 > 0) {
                    hNHdayHoliday = getValue("h_n_hday_holiday");
                }
                continue;
            } else {
                status = 1;
                tempDate = hNHdayHoliday;
            }
        }
    }

  /***********************************************************************/
  void selectActAcno() throws Exception {
   /***寫 header 搬到 totCnt == 1 才執行
    hChiDate = String.format("%08d", comcr.str2long(nextVouch) - 19110000);
    buf = String.format("BOFACHP01%-8.8s%-6.6s0060567%-7.7sV10%-210.210s", hChiDate, hSystime, "9990250", " ");
    out.write(buf);
    if (DEBUG) out.write("\n");
   ***/

    sqlCmd = "select ";
    sqlCmd += " a.p_seqno,";
    sqlCmd += " a.acct_type,";
    sqlCmd += " b.acct_key,";
    sqlCmd += " b.stmt_cycle,";
    sqlCmd += " b.id_p_seqno,";
    sqlCmd += " b.corp_p_seqno,";
    sqlCmd += " b.card_indicator,";
    sqlCmd += " b.corp_act_flag,";
    sqlCmd += " b.autopay_acct_no,";
  //sqlCmd += " b.autopay_id,";
    sqlCmd += " decode(b.autopay_id,'',UF_IDNO_ID(b.id_p_seqno),b.autopay_id) h_autopay_id,";
    sqlCmd += " b.autopay_acct_s_date,";
    sqlCmd += " b.autopay_acct_e_date,";
    sqlCmd += " b.acct_status,";
    sqlCmd += " b.new_cycle_month,";
    sqlCmd += " c.acct_jrnl_bal h_acct_acct_jrnl_bal,";
    sqlCmd += " a.adi_end_bal h_acct_adi_end_bal,";
    sqlCmd += " c.autopay_bal h_acct_autopay_bal,";
    sqlCmd += " c.ttl_amt_bal h_acct_ttl_amt_bal,";
    sqlCmd += " c.dc_ttl_amt h_acur_dc_ttl_amt, ";
    sqlCmd += " c.dc_adjust_dr_amt h_acur_dc_adjust_dr_amt, ";
    sqlCmd += " c.dc_adjust_cr_amt h_acur_dc_adjust_cr_amt, ";
    sqlCmd += " c.dc_pay_amt h_acur_dc_pay_amt, ";
    sqlCmd += " c.autopay_acct_bank ";
    sqlCmd += "from act_acno b, act_acct a, act_acct_curr c ";
    sqlCmd += "where a.p_seqno   = b.acno_p_seqno ";
    sqlCmd += " and b.acno_p_seqno   = c.p_seqno ";
    sqlCmd += " and c.curr_code = '901' ";
    sqlCmd += " and substr(c.autopay_acct_bank,1,3) not in ('006','700') ";
    sqlCmd += " and c.autopay_acct_no  != '' ";
    sqlCmd += " and c.autopay_bal > 0 ";
  //sqlCmd += " and c.ttl_amt_bal > 0 ";
    sqlCmd += " and b.fl_flag = '' ";
    sqlCmd += " and b.acct_status not in ('3','4') ";
    sqlCmd += " and ? between autopay_acct_s_date ";
    sqlCmd += " and decode(autopay_acct_e_date,'','29991231',autopay_acct_e_date) ";
    sqlCmd += " and 1 = 1 ";
    setString(1, hBusiBusinessDate);
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
      hJrnlDcTransactionAmt = 0;
      hNextAdjustDrAmt = 0;
      hNextAdjustCrAmt = 0;

      hAcctPSeqno = getValue("p_seqno");
      hAcctAcctType = getValue("acct_type");
      hAcctAcctKey = getValue("acct_key");
      hAcnoStmtCycle = getValue("stmt_cycle");
      hAcnoIdPSeqno = getValue("id_p_seqno");
      hAcnoCorpPSeqno = getValue("corp_p_seqno");
      hAcnoCardIndicator = getValue("card_indicator");
      hAcnoCorpActFlag = getValue("corp_act_flag");
      hAcnoAutopayAcctNo = getValue("autopay_acct_no");
    //hAcnoAutopayId = getValue("autopay_id");
      hAcnoAutopayId = getValue("h_autopay_id");
      hAcnoAutopayAcctSDate = getValue("autopay_acct_s_date");
      hAcnoAutopayAcctEDate = getValue("autopay_acct_e_date");
      hAcnoAcctStatus = getValue("acct_status");
      hAcnoNewCycleMonth = getValue("new_cycle_month");
      hAcctAcctJrnlBal = getValueDouble("h_acct_acct_jrnl_bal");
      hAcctAdiEndBal = getValueDouble("h_acct_adi_end_bal");
      hAcctAutopayBal = getValueDouble("h_acct_autopay_bal");
      hAcctTtlAmtBal = getValueDouble("h_acct_ttl_amt_bal");
      hAcurDcTtlAmt = getValueDouble("h_acur_dc_ttl_amt");
      hAcurDcAdjustDrAmt = getValueDouble("h_acur_dc_adjust_dr_amt");
      hAcurDcAdjustCrAmt = getValueDouble("h_acur_dc_adjust_cr_amt");
      hAcurDcPayAmt = getValueDouble("h_acur_dc_pay_amt");
      hAcnoAutopayAcctBank = getValue("autopay_acct_bank");

      readCnt++;
      if (readCnt % 100000 == 0 || readCnt == 1)
          showLogMessage("I", "", String.format("Process Read record=[%d]", 
          readCnt));

      hAchdBankNo = hAcnoAutopayAcctBank;

     /***
      selectActAchDtl();
      if (hAchdBankNo.length() == 0) {
          showLogMessage("I", "", String.format("Error: act_ach_dtl not find [%s],[%s]", hAcctPSeqno,
                  hAcnoAutopayAcctBank));
          continue;
      }
     ***/

      if (checkPtrActgeneralN() != 0) {
          totCnt3++;
          continue;
      }
      /* showLogMessage("I", "", String.format("G. tot_cnt_3  [%d]", tot_cnt_3));  */

      selectActJrnl();

      hAcctAutopayBal = hAcctAutopayBal - hJrnlDcTransactionAmt;
      hAcctTtlAmtBal = hAcurDcTtlAmt - hAcurDcPayAmt
              - hAcurDcAdjustDrAmt + hNextAdjustDrAmt
              + hAcurDcAdjustCrAmt - hNextAdjustCrAmt;

      hAoayTransactionAmt = hAcctAutopayBal;

      if (hAcctAutopayBal > hAcctTtlAmtBal)
          hAoayTransactionAmt = hAcctTtlAmtBal;

      if ((hAcnoAcctStatus.equals("3")) || (hAcnoAcctStatus.equals("4"))) {
          hAoayTransactionAmt = hAcctAcctJrnlBal;
      }

      if (hAoayTransactionAmt <= 0)
          continue;

      selectChiNameRtn();

      insertActOtherApay();

      totCnt++;
      if (totCnt % 10000 == 0 || totCnt == 1)
          showLogMessage("I", "", String.format("Process record=[%d]", totCnt));

      /*
       * if(tot_cnt == 1) { sprintf(h_chi_date.arr , "%08d" ,
       * atol(next_vouch.arr)-19110000); h_chi_date.len =
       * strlen(h_chi_date.arr);
       * fprintf(fptr1,"BOFACHP01%-8.8s%-6.6s0170077%-7.7s%-123.123s"
       * ,h_chi_date.arr,h_systime.arr,"9990250"," "); }
       */

      if (totCnt == 1) {
        hChiDate = String.format("%08d", 
        comcr.str2long(nextVouch) - 19110000);
        buf = String.format("BOFACHP01%-8.8s%-6.6s0060567%-7.7sV10%-210.210s", 
        hChiDate, hSystime, "9990250", " ");
        out.write(buf);
      //out.write("\n");
      //if (DEBUG) out.write("\n");
        if (!hConsecutive.equals("C")) out.write("\n");
      }

      totAmt = totAmt + hAoayTransactionAmt;

      buf = String.format(
        "NSD851%08d%-7.7s%-16.16s%-7.7s%016d%010.0f%-2.2sB%-10.10s%-10.10s%-6.6s%-8.8s%-8.8s%-1.1s%-20.20s%-40.40s%-10.10s%05d%-20.20s%-39.39s",
        totCnt, "0060567", "0000560081888887", hAchdBankNo, comcr.str2long(hAcnoAutopayAcctNo),
        hAoayTransactionAmt, " " /* 9-10 */
      //, "78506552", hAcnoAutopayId /* 12-13 */
        , "99629524", hAcnoAutopayId /* 12-13 */
        , " ", " " /* 14-15 */
        , " ", " " /* 16-17 */
        , hAcctPSeqno, nextVouch /* 18-19 */
        , " ", 0 /* 20-21 */
        , " ", " "); /* 22-23 */

      out.write(buf);
    //out.write("\n");
    //if (DEBUG) out.write("\n");
      if (!hConsecutive.equals("C")) out.write("\n");

    }
    closeCursor(cursorIndex);
    /*
     * if(tot_cnt > 0) { }
     */
    if (totCnt > 0) {
      buf = String.format("EOFACHP01%-8.8s00605679990250%08d%016.0f%-8.8s%-187.187s", 
      hChiDate, totCnt, totAmt, " "," ");
      out.write(buf);
    //out.write("\n");
    //if (DEBUG) out.write("\n");
      if (!hConsecutive.equals("C")) out.write("\n");
    }
  }

    /***********************************************************************/
    void selectActJrnl() throws Exception {
        sqlCmd  = "select sum(decode(tran_class,'P',dc_transaction_amt,0)) h_jrnl_dc_transaction_amt, ";
        sqlCmd += "       sum(decode(tran_class,'A',decode(dr_cr,'D',dc_transaction_amt,0),0)) h_next_adjust_dr_amt, ";
        sqlCmd += "       sum(decode(tran_class,'A',decode(dr_cr,'C',dc_transaction_amt,0),0)) h_next_adjust_cr_amt  ";
        sqlCmd += "  from act_jrnl  ";
        sqlCmd += " where p_seqno    =  ?  ";
        sqlCmd += "   and ((tran_class = 'P' and tran_type not in ('AUT1','AUT2','ACH1','REFU')) ";
        sqlCmd += "     or (tran_class = 'A' and tran_type not in ('OP01','OP02','OP03','OP04','AI01') and ";
        sqlCmd += "         item_date  > (select this_close_date from ptr_workday where stmt_cycle = ? ) and item_date <= ? ))";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = '901'  ";
        sqlCmd += "   and acct_date  > (select this_close_date from ptr_workday where stmt_cycle = ? )  ";
        sqlCmd += "   and acct_date <= ? ";
        setString(1, hAcctPSeqno);
        setString(2, hAcnoStmtCycle);
        setString(3, hBusiBusinessDate);
        setString(4, hAcnoStmtCycle);
        setString(5, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_jrnl not found!", "", hCallBatchSeqno);
        }
        hJrnlDcTransactionAmt = getValueDouble("h_jrnl_dc_transaction_amt");
        hNextAdjustDrAmt = getValueDouble("h_next_adjust_dr_amt");
        hNextAdjustCrAmt = getValueDouble("h_next_adjust_cr_amt");
    }

    /***********************************************************************/
    void selectActAchDtl() throws Exception {
        sqlCmd = "select bank_no ";
        sqlCmd += " from act_ach_dtl  ";
        sqlCmd += "where p_seqno   = ?  ";
        sqlCmd += " and autopay_acct_no = ? ";
        sqlCmd += " and process_date in (select max(process_date) ";
        sqlCmd += " from act_ach_dtl  ";
        sqlCmd += "where p_seqno         = ? ";
        sqlCmd += " and autopay_acct_no = ?) ";
        sqlCmd += "order by decode(ad_mark,'','B',ad_mark) ";
        setString(1, hAcctPSeqno);
        setString(2, hAcnoAutopayAcctNo);
        setString(3, hAcctPSeqno);
        setString(4, hAcnoAutopayAcctNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAchdBankNo = getValue("bank_no");
        }
    }

    /***********************************************************************/
    int checkPtrActgeneralN() throws Exception {
        int i = 0;
/*
showLogMessage("I", "", String.format("     Total chk_pnt[%d]", chk_pnt));
showLogMessage("I", "", String.format("a_check_acct_type-i [%s]", a_check_acct_type[i]));
showLogMessage("I", "", String.format("h_acct_acct_type [%s]", h_acct_acct_type));
showLogMessage("I", "", String.format("a_check_stmt_cycle-i [%s]", a_check_stmt_cycle[i]));
showLogMessage("I", "", String.format("h_acno_stmt_cycle [%s]", h_acno_stmt_cycle));

showLogMessage("I", "", String.format("h_acno_new_cycle_month [%s]", h_acno_new_cycle_month));
showLogMessage("I", "", String.format("a_wday_this_acct_month-i [%s]", a_wday_this_acct_month[i]));
showLogMessage("I", "", String.format("a_check_this_acct_month-i [%s]", a_check_this_acct_month[i]));
*/
        for (i = 0; i < chkPnt; i++) {


            if (aCheckAcctType[i].equals(hAcctAcctType) && aCheckStmtCycle[i].equals(hAcnoStmtCycle)) {

                if (hAcnoNewCycleMonth.compareTo(aCheckThisAcctMonth[i]) > 0)
                    return 1;
                else
                    return 0;
            }
        }

        return 1;
    }

    /***********************************************************************/
    void selectChiNameRtn() throws Exception {
        hIdnoChiName = "";

        if ((hAcnoCardIndicator.equals("2")) && (hAcnoCorpActFlag.equals("Y"))) {
            selectCrdCorp();
            return;
        }

        //sqlCmd = "select chi_name ";
        sqlCmd = "select substr(chi_name,1,40) chi_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("chi_name");
        }

        hAoayChiName = hIdnoChiName;
    }

    /***********************************************************************/
    void selectCrdCorp() throws Exception {
        hCorpChiName = "";

        //sqlCmd = "select chi_name ";
        sqlCmd = "select substr(chi_name,1,40) chi_name ";
        sqlCmd += " from crd_corp  ";
        sqlCmd += "where corp_p_seqno = ? ";
        setString(1, hAcnoCorpPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCorpChiName = getValue("chi_name");
        }

        hAoayChiName = hCorpChiName;
    }

    /***********************************************************************/
    void insertActOtherApay() throws Exception {
        String strb = "";

        strb = String.format("%08d", comcr.str2long(vChiDate)+19110000);
        hAoayEnterAcctDate = strb;

        daoTable = "act_other_apay";
        extendField = daoTable + ".";
        setValue(extendField + "enter_acct_date", nextVouch);
        setValue(extendField + "acct_bank", hAchdBankNo);
        setValue(extendField + "autopay_acct_no", hAcnoAutopayAcctNo);
        setValue(extendField + "stmt_cycle", hAcnoStmtCycle);
        setValue(extendField + "batch_no", hBusiBusinessDate);
        setValue(extendField + "crt_date", hBusiBusinessDate);
        setValueDouble(extendField + "transaction_amt", hAoayTransactionAmt);
        setValue(extendField + "autopay_id", hAcnoAutopayId);
        setValue(extendField + "p_seqno", hAcctPSeqno);
        setValue(extendField + "acct_type", hAcctAcctType);
        setValue(extendField + "id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField + "chi_name", hAoayChiName);
        setValue(extendField + "status_code", "XX");
        setValue(extendField + "from_mark", "01");
        setValue(extendField + "mod_user", hAcnoModUser);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", hAcnoModPgm);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_other_apay duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void checkOpen() throws Exception {

      //fileNameFull = String.format("%s/ECSACHP01", comc.getIBMftp());
        fileNameFull = String.format("%s/media/act/ACHP01_%8.8s.TXT", comc.getECSHOME(),
        hBusiBusinessDate);
        fileNameFull = Normalizer.normalize(fileNameFull, java.text.Normalizer.Form.NFKD);

        showLogMessage("I", "", String.format("output file name:[%s]", fileNameFull));
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileNameFull), "MS950"));
        } catch (FileNotFoundException exception) {
            comcr.errRtn(fileNameFull, "檔案開啓失敗！", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActAutoComfRtn() throws Exception {

        daoTable = "act_auto_comf";
        extendField = daoTable + ".";
        setValue(extendField + "enter_acct_date", nextVouch);
        setValue(extendField + "file_type", "2");
        setValueInt(extendField + "cr_cnt", 0);
        setValueDouble(extendField + "cr_amt", 0.0);
        setValueInt(extendField + "dr_cnt", totCnt);
        setValueDouble(extendField + "dr_amt", totAmt);
        setValue(extendField + "mod_user", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_auto_comf duplicate!", "", hCallBatchSeqno);
        }

    }

  //************************************************************************	
	void procFTP(String fileName, String filePath) throws Exception {
	  CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
		  
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	  commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	  commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(), 
	  MEDIA_FOLDER);
	  commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	  commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	  commFTP.hEflgModPgm = javaProgram;
	      
	//Temporary don't execute ftp
	//int errCode = 0;
	//showLogMessage("I", "", "put " + fileName + " 開始傳送....");
	//ecs_ref_ip_code.ref_ip_code="CRDATACREA"，其ftp_type="0"，在COMMFTP處理只是fileCopy
	  int errCode = commFTP.ftplogName("CRDATACREA", "put " + fileName);
	      
	  if (errCode != 0) {
	    showLogMessage("I", "", "ERROR:無法複製 " + fileName + " 資料"+" errcode:"+errCode);
	    insertEcsNotifyLog(fileName, commFTP, comr);          
	  } else {
	 	  moveTxtToBackup(filePath, fileName);
	  }
	}

  //************************************************************************		  
	public int insertEcsNotifyLog(String fileName, CommFTP commFTP, 
	CommRoutine comr) throws Exception {
	  setValue("crt_date", sysDate);
	  setValue("crt_time", sysTime);
	  setValue("unit_code", comr.getObjectOwner("3", javaProgram));
	  setValue("obj_type", "3");
	  setValue("notify_head", "無法複製 " + fileName + " 資料");
	  setValue("notify_name", "媒體檔名:" + fileName);
	  setValue("notify_desc1", "程式 " + javaProgram + " 無法複製 " + fileName + " 資料");
	  setValue("notify_desc2", "");
	  setValue("trans_seqno", commFTP.hEflgTransSeqno);
	  setValue("mod_time", sysDate + sysTime);
	  setValue("mod_pgm", javaProgram);
	  daoTable = "ecs_notify_log";

	  insertTable();

	  return (0);
	}
	
  //************************************************************************		
	private void moveTxtToBackup(String filePath, String fileName) 
	throws IOException, Exception {
		// media/dba/backup
		Path backupFileFolderPath = Paths.get(comc.getECSHOME(), MEDIA_FOLDER,
		 "backup");
		// create the parent directory if parent the directory is not exist
		Files.createDirectories(backupFileFolderPath);
		// get output file path
		String backupFilePath = Paths.get(backupFileFolderPath.toString(), 
		fileName + "." + sysDate + sysTime).toString();
		
		moveFile(filePath, backupFilePath);
	}
	
  //************************************************************************		
	private void moveFile(String srcFilePath, String targetFilePath) 
	throws Exception {
		
		if (comc.fileMove(srcFilePath, targetFilePath) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + srcFilePath + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + srcFilePath + "] 已移至 [" + targetFilePath + "]");
	}

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActH008 proc = new ActH008();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

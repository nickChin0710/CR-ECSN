/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/22  V1.00.01    SUP       error correction                          *
 *  111-10-13  V1.00.02    Machao    sync from mega & updated for project coding standard*
 *  111/12/02  V1.00.03    Simon     1.not self-acting to ftp , MFT will transfer it to post office*
 *                                   2.apply to TCB data constant              *
 *                                   3.apply to TCB filename rules             *
 *  112/02/24  V1.00.04    Simon     1.change output folder /rpt into /act     *
 *                                   2.change tmp_file_name into tmpFileName   *
 *                                   3.16 digital autopay_acct_no transformed into 14 digital*
 *  112/03/08  V1.00.04    yingdong  Erroneous String Compare Issue            *
 *  112/03/29  V1.00.05    Simon     add procFTP                               *
 *  112/05/22  V1.00.06    Simon     revised procFTP() statement codes         *
 *  112/07/12  V1.00.07    Simon     1.排除花農卡: "and b.fl_flag = '' in selectActAcno() " *
 *                                   2.add hAcnoAutopayAcctBank                *
 *  112/11/16  V1.00.08    Simon     tcb 要求催、呆戶不送自扣                  *
 ******************************************************************************/

package Act;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import com.*;

/*產生郵局自動扣繳檔*/
public class ActB006 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private final String PROGNAME = "產生郵局自動扣繳檔  112/11/16  V1.00.08";
  	private final static String MEDIA_FOLDER = "/media/act/";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";
    String buf = "";

    String hBusiBusinessDate = "";
    String hTempDeductDate = "";
    String hSystemDate = "";
    String hSystemTime = "";
    String hWorkDd = "";

    String temp1Date = "";
    String hAcctPSeqno = "";
    String hAcctAcctType = "";
    String hAcnoStmtCycle = "";
    String hAcnoAcctStatus = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoComboAcctNo = "";
    String hAcnoAutopayAcctBank = "";
    String hAcnoAutopayAcctNo = "";
    String hAchAutopayAcctNo = "";
    String hAcnoAutopayId = "";
    String hAcnoAutopayIndicator = "";
    String hAcnoNewCycleMonth = "";
    String hAcctPayByStageDate = "";
    double hAcctPayByStageBal = 0;
    double hAcctAdiEndBal = 0;
    String hAcctMinPay = "";
    String hCkapChiName = "";
    double hJrnlTransactionAmt = 0;
    String hTempDeductDateNew = "";
    double hCkapTransactionAmt = 0;
    String tmpFileName = "";

    int intFlag = 0;
    int testFlag = 0;
    int totalCnt = 0;
    double totalTransactionAmt = 0;
    double hJrnlDcTransactionAmt = 0;
    double hNextAdjustDrAmt = 0;
    double hNextAdjustCrAmt = 0;
  //String chiDate = "";
    String vChiDate = "";
    String tempAmt = "";
    String endflag = "\r\n";
    String cmdStr = "";
    String tmpstr = "";
    String tmpstr14 = "";
    String fileName = "";
    String fileNameFull = "";
    String tempAcctNo = "";
    String wsPostFlag = "";
    String tempDiskNo = "";
    String diskNo = "";
    List<String> hmTempNoneFlag = new ArrayList<String>();
    String hTempCycle = "";
    String hCkapAutopayAcctNo = "";
    double hAcctAutopayBal = 0;
    double hAcctTtlAmtBal = 0;
    double hAcctAcctJrnlBal = 0;
    double hAcurDcTtlAmt = 0;
    double hAcurDcAdjustDrAmt = 0;
    double hAcurDcAdjustCrAmt = 0;
    double hAcurDcPayAmt = 0;
    String hWdayThisCloseDate = "";

    int out = -1;

    private int ptrWorkdayCnt = 0;
    List<String> hmAgnnAcctType = new ArrayList<String>();
    List<String> hmWdayStmtCycle = new ArrayList<String>();
    
    List<String>  hmTempFromDate            = new ArrayList<String>();
    List<String>  hmTempEndDate             = new ArrayList<String>();
    List<String>  hmWdayThisAcctMonth      = new ArrayList<String>();
    List<String>  hmWdayThisCloseDate      = new ArrayList<String>();
    List<String>  hmWdayThisDelaypayDate   = new ArrayList<String>();
    List<Integer> hmAgnnInstpayBDueDays   = new ArrayList<Integer>();
    List<Integer> hmAgnnInstpayDeductDays  = new ArrayList<Integer>();

    private int tempInt = 0;
    private String hTempFromDate = "";
    private String hTempEndDate = "";
    private String hWdaythisacctMonth = "";
//    private String h_wday_this_close_date = "";
//    private String h_wday_this_delaypay_date = "";
//    private int h_agnn_instpay_b_due_days = 0;
//    private int h_agnn_instpay_deduct_days = 0;

    public int mainProcess(String[] args) {

      try {

        // ====================================
        // 固定要做的
        dateTime();
        setConsoleMode("Y");
        javaProgram = this.getClass().getName();
        showLogMessage("I", "", javaProgram + " " + PROGNAME);
        // =====================================
        if (args.length > 3) {
            comc.errExit("Usage : ActB006 [business_date [disp_type]]", "");
        }

        // 固定要做的

        if (!connectDataBase()) {
            comc.errExit("connect DataBase error", "");
        }

        hBusiBusinessDate = "";
        if (args.length > 0 && args[0].length() == 8)
            hBusiBusinessDate = args[0];

        hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
        comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

        comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

        selectPtrBusinday();

        selectPtrActgeneralN();

        temp1Date = comcr.increaseDays(hBusiBusinessDate, 1);
        if (!temp1Date.equals(hTempDeductDate)) {
            exceptExit = 0;
            comcr.errRtn(String.format("本日[%s]非營業日前一日[%s],本程式不處理", hBusiBusinessDate, temp1Date), "", hCallBatchSeqno);
        }

        if (args.length >= 2)
            testFlag = comcr.str2int(args[1]);
        selectPtrActgeneral();
        if (testFlag == 1) {
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        }

        checkOpen();

        selectActAcno();

      //Temp 預設不傳檔
      //if (testFlag == 0) {
      //    testFlag = 2;                
      //}

        if (totalCnt > 0) {
          tempAmt = String.format("%014.2f", totalTransactionAmt);                
          tempAmt = tempAmt.replace(".", "");

          buf = String.format("2 J03    %7.7s%3.3s%07d%13.13s%16.16s%020d%45.45s%s", vChiDate, " ", totalCnt,
                  tempAmt, " ", 0, " ", endflag);
          writeTextFile(out, buf);
          insertPtrMediaCntl();
          if (testFlag == 0) {
		        String filePath = String.format("%s/media/act/%s_%8.8s.TXT", 
		        comc.getECSHOME(),tmpFileName, hBusiBusinessDate);
		        procFTP(Paths.get(filePath).getFileName().toString(), filePath);
            showLogMessage("I", "", String.format("  郵局自扣檔案:"));
            showLogMessage("I", "", String.format("  尾筆 1筆"));
          } 
        } else {
          showLogMessage("I", "", String.format("沒有自動扣繳資料"));
        }

        closeOutputText(out);

        showLogMessage("I", "", String.format(" =============================================== "));
        showLogMessage("I", "", String.format("      本日扣款總筆數 [%d]", totalCnt));
        tmpstr = comcr.commFormat("3$,3$,3$,3$", totalTransactionAmt);
        showLogMessage("I", "", String.format("      本日扣款總金額 [%s]", tmpstr));
        showLogMessage("I", "", String.format(" =============================================== "));

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
        hTempDeductDate = "";
        hWorkDd = "";

        sqlCmd = "select decode( cast(? as varchar(8)) ,'',business_date, ? ) h_busi_business_date,";
        sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)) ,'',business_date, ? ), 'yyyymmdd')+1 days,'yyyymmdd') h_temp_deduct_date,";
        sqlCmd += " to_char(sysdate, 'yyyymmdd'  ) h_system_date,";
        sqlCmd += " to_char(sysdate, 'hh24:mi:ss') h_system_time,";
        sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)) ,'',business_date, ? ),'yyyymmdd'),'D') h_work_dd ";
        sqlCmd += "  from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hTempDeductDate = getValue("h_temp_deduct_date");
            hSystemDate = getValue("h_system_date");
            hSystemTime = getValue("h_system_time");
            hWorkDd = getValue("h_work_dd");
        }

    }

    /***********************************************************************/
    void selectPtrActgeneralN() throws Exception {
        intFlag = 0;
        /* 一天 或 一天以上 */
        sqlCmd = "select post_o_days ";
        sqlCmd += "  from ptr_actgeneral_n  ";
        sqlCmd += " fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
        }
        intFlag = getValueInt("post_o_days");

    }

    /***********************************************************************/
    void selectPtrActgeneral() throws Exception {
        int rtn = 0;
        hmTempNoneFlag.clear();
        hmAgnnAcctType.clear();
        hmWdayStmtCycle.clear();
        hmTempFromDate.clear();
        hmTempEndDate.clear();
        hmWdayThisAcctMonth.clear();
        hmWdayThisCloseDate.clear();
        hmWdayThisDelaypayDate.clear();
        hmAgnnInstpayBDueDays.clear();
        hmAgnnInstpayDeductDays.clear();

        sqlCmd = "select a.stmt_cycle,";
        sqlCmd += " a.this_acct_month,";
        sqlCmd += " a.this_close_date,";
        sqlCmd += " a.this_lastpay_date,";
        sqlCmd += " a.this_delaypay_date,";
        sqlCmd += " b.acct_type,";
        sqlCmd += " b.autopay_b_due_days,";
        sqlCmd += " b.autopay_deduct_days,";
        sqlCmd += " b.instpay_b_due_days,";
        sqlCmd += " b.instpay_deduct_days,";
        sqlCmd += " b.post_o_days ";
        sqlCmd += "  from ptr_workday a, ptr_actgeneral_n b ";
        sqlCmd += " order by b.acct_type, a.stmt_cycle ";
        ptrWorkdayCnt  = selectTable();
        
        for (int i = 0; i < ptrWorkdayCnt; i++) {
            String hWdayStmtCycle = getValue("stmt_cycle", i);
            hmWdayStmtCycle.add(hWdayStmtCycle);
            hmWdayThisAcctMonth.add(getValue("this_acct_month", i));
            hmWdayThisCloseDate.add(getValue("this_close_date", i));
            String hWdayThisLastpayDate = getValue("this_lastpay_date", i);
            hmWdayThisDelaypayDate.add(getValue("this_delaypay_date", i));
            String hAgnnAcctType = getValue("acct_type", i);
            hmAgnnAcctType.add(hAgnnAcctType);
//            int h_m_agnn_autopay_b_due_days = getValueInt("autopay_b_due_days");
//            int h_agnn_autopay_deduct_days = getValueInt("autopay_deduct_days");
            hmAgnnInstpayBDueDays.add(getValueInt("instpay_b_due_days", i));
            hmAgnnInstpayDeductDays.add(getValueInt("instpay_deduct_days", i));
            int hAgnnPostODays = getValueInt("post_o_days", i);
            hmTempNoneFlag.add("");

            sqlCmd = "select to_char(to_date( ? ,'yyyymmdd') + ? days,'yyyymmdd') temp_1_date";
            sqlCmd += "  from dual ";
            setString(1, hBusiBusinessDate);
            setInt(2, hAgnnPostODays);
            int recordCnt2 = selectTable();
            if (recordCnt2 > 0) {
                temp1Date = getValue("temp_1_date");
            }

            hTempFromDate = temp1Date;
            hmTempFromDate.add(temp1Date);

            String temp2Date = comcr.increaseDays(hBusiBusinessDate, hAgnnPostODays);
            hTempEndDate = temp2Date;
            hmTempEndDate.add(temp2Date);

            if (intFlag == 1) {
                rtn = getHolidayDateRtn();
                if (rtn == 1) {
                    temp1Date = comcr.increaseDays(hBusiBusinessDate, -1);
                    sqlCmd = "select to_char(to_date( ?  ,'yyyymmdd')+1 days,'yyyymmdd') temp_1_date";
                    sqlCmd += "  from dual ";
                    setString(1, temp1Date);
                    int recordCnt3 = selectTable();
                    if (recordCnt3 > 0) {
                        temp1Date = getValue("temp_1_date");
                    }
                }
                hTempFromDate = temp1Date;
                hmTempFromDate.set(i, temp1Date);
            }

            if (testFlag == 1) {
                showLogMessage("I", "", String.format("From_date[%s]-Test[%s] cycle[%s] type[%s][%s]", hTempFromDate,
                        hTempEndDate, hWdayStmtCycle, hAgnnAcctType, hWdayThisLastpayDate));
                continue;
            }

            if ((hWdayThisLastpayDate.compareTo(hTempFromDate) < 0)
                    || (hWdayThisLastpayDate.compareTo(hTempEndDate) > 0)) {
                hmTempNoneFlag.set(i, "Y");
            } else {
                showLogMessage("I", "", String.format("From_date[%s]-To[%s] cycle[%s] type[%s]", hTempFromDate,
                        hTempEndDate, hWdayStmtCycle, hAgnnAcctType));
            }
        }
    }

    /***********************************************************************/
    int getHolidayDateRtn() throws Exception {
        int tempInt = 0;
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += "  from ptr_holiday  ";
        sqlCmd += " where holiday = ? ";
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
    void selectActAcno() throws Exception {
        int int1 = 0;
//        String h_wday_this_delaypay_date = "";
//        int h_agnn_instpay_b_due_days = 0;
//        int h_agnn_instpay_deduct_days = 0;
        
        sqlCmd = "select a.p_seqno,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " b.stmt_cycle,";
        sqlCmd += " b.acct_status,";
        sqlCmd += " b.id_p_seqno,";
        sqlCmd += " b.corp_p_seqno,";
        sqlCmd += " b.combo_acct_no,";
        sqlCmd += " b.autopay_acct_bank,";
        sqlCmd += " b.autopay_acct_no,";
        sqlCmd += " b.autopay_id,";
        sqlCmd += " b.autopay_indicator,";
        sqlCmd += " b.new_cycle_month,";
        sqlCmd += " a.pay_by_stage_date,";
        sqlCmd += " a.pay_by_stage_bal,";
        sqlCmd += " c.acct_jrnl_bal,";
        sqlCmd += " c.autopay_bal,";
        sqlCmd += " a.adi_end_bal,";
        sqlCmd += " c.min_pay,";
        sqlCmd += " c.ttl_amt_bal, ";
        sqlCmd += " c.dc_ttl_amt, ";
        sqlCmd += " c.dc_adjust_dr_amt, ";
        sqlCmd += " c.dc_adjust_cr_amt, ";
        sqlCmd += " c.dc_pay_amt ";
        sqlCmd += "  from act_acct a,act_acno b,act_acct_curr c ";
        sqlCmd += " where a.p_seqno      = b.acno_p_seqno ";
        sqlCmd += "   and b.acno_p_seqno = c.p_seqno ";
        sqlCmd += "   and decode(c.curr_code,'','901',c.curr_code) = '901' ";
        sqlCmd += "   and c.autopay_bal  > 0 ";
      //sqlCmd += "   and c.ttl_amt_bal  > 0 ";
        sqlCmd += "   and c.autopay_acct_bank like '700%' ";
        sqlCmd += "   and c.autopay_acct_no  != '' ";
        sqlCmd += "   and b.fl_flag = '' ";
        sqlCmd += "   and b.acct_status not in ('3','4') ";
        sqlCmd += "   and ? >= decode(autopay_acct_s_date,'','00000000',autopay_acct_s_date) ";
        sqlCmd += "   and ?  < decode(autopay_acct_e_date,'','30001231',autopay_acct_e_date) ";
        /*
         * order by b.stmt_cycle,decode(b.acct_status,1,0,1),a.p_seqno;
         */
        setString(1, hTempDeductDate);
        setString(2, hTempDeductDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hJrnlDcTransactionAmt   = 0;
            hNextAdjustDrAmt        = 0;
            hNextAdjustCrAmt        = 0;
            
            hAcctPSeqno = getValue("p_seqno");
            hAcctAcctType = getValue("acct_type");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoComboAcctNo = getValue("combo_acct_no");
            hAcnoAutopayAcctBank = getValue("autopay_acct_bank");
            hAcnoAutopayAcctNo = getValue("autopay_acct_no");
            hAcnoAutopayId = getValue("autopay_id");
            hAcnoAutopayIndicator = getValue("autopay_indicator");
            hAcnoNewCycleMonth = getValue("new_cycle_month");
            hAcctPayByStageDate = getValue("pay_by_stage_date");
            hAcctPayByStageBal = getValueDouble("pay_by_stage_bal");
            hAcctAcctJrnlBal = getValueDouble("acct_jrnl_bal");
            hAcctAutopayBal = getValueDouble("autopay_bal");
            hAcctAdiEndBal = getValueDouble("adi_end_bal");
            hAcctMinPay = getValue("min_pay");
            hAcctTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAcurDcTtlAmt  = getValueDouble("dc_ttl_amt");
            hAcurDcAdjustDrAmt  = getValueDouble("dc_adjust_dr_amt");
            hAcurDcAdjustCrAmt  = getValueDouble("dc_adjust_cr_amt");
            hAcurDcPayAmt  = getValueDouble("dc_pay_amt");

            hTempCycle = hAcnoStmtCycle;

            for (int1 = 0; int1 < ptrWorkdayCnt; int1++)
            {
   /*  mark lai
             if((h_acno_acct_status.arr[0]=='1')&&
                (h_m_temp_none_flag[int1].arr[0]=='Y')) continue;
   */
             if(hmTempNoneFlag.get(int1).equals("Y")) continue;

             if((hAcctAcctType.equals(hmAgnnAcctType.get(int1) ) == false)||
                (hAcnoStmtCycle.equals(hmWdayStmtCycle.get(int1)) == false)) continue;
             break;
             }
            if (int1>=ptrWorkdayCnt) continue;

            hTempFromDate            = hmTempFromDate.get(int1);
            hTempEndDate             = hmTempEndDate.get(int1);
            hTempDeductDateNew      = hmTempEndDate.get(int1);
            hWdaythisacctMonth      = hmWdayThisAcctMonth.get(int1);
            hWdayThisCloseDate      = hmWdayThisCloseDate.get(int1);
//            h_wday_this_delaypay_date   = h_m_wday_this_delaypay_date.get(int1);
//            h_agnn_instpay_b_due_days      = h_m_agnn_instpay_b_due_days.get(int1);
//            h_agnn_instpay_deduct_days     = h_m_agnn_instpay_deduct_days.get(int1);

            if (hAcnoNewCycleMonth.compareTo(hWdaythisacctMonth) > 0)
                continue;

            if (hAcnoIdPSeqno.length() != 0)
                selectCrdIdno();
            else
                selectCrdCorp();

            selectActJrnl();

            hAcctAutopayBal = hAcctAutopayBal - hJrnlDcTransactionAmt;
            hAcctTtlAmtBal = hAcurDcTtlAmt - hAcurDcPayAmt
                               - hAcurDcAdjustDrAmt + hNextAdjustDrAmt 
                               + hAcurDcAdjustCrAmt - hNextAdjustCrAmt;

            if (hAcctAutopayBal > hAcctTtlAmtBal)
                hCkapTransactionAmt = hAcctTtlAmtBal;
            else
                hCkapTransactionAmt = hAcctAutopayBal;

            if ((hAcnoAcctStatus.equals("3")) || (hAcnoAcctStatus.equals("4"))) {
                hCkapTransactionAmt = hAcctAcctJrnlBal;
            }

            if (hCkapTransactionAmt <= 0)
                continue;

            totalTransactionAmt = totalTransactionAmt + hCkapTransactionAmt;

          //tmpstr = String.format("%14.14s", hAcnoAutopayAcctNo);
            tmpstr14 = String.format("%014d", comcr.str2long(hAcnoAutopayAcctNo));

            totalCnt++;
            if ((totalCnt % 5000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }

            insertActOtherApay();

            /* lai add */
            tempDiskNo = String.format("%05d", comcr.str2long(hWdaythisacctMonth) - 191100);
            diskNo = String.format("%5.5s%2.2s%2.2s", tempDiskNo, hAcnoStmtCycle, hAcnoStmtCycle);

            wsPostFlag = "P";
/**
            tempAcctNo = String.format("%14.14s", hAcnoAutopayAcctNo);
            for (tempInt = 0; tempInt < 14; tempInt++) {
                if (tempAcctNo.toCharArray()[tempInt] == ' ') {
                    wsPostFlag = "G";
                    // temp_acct_no[temp_int]='0';  Arthur, 91/01/20 
                } else {
                    break;
                }
            }

            if ("G".equals(wsPostFlag))
                tempAcctNo = String.format("000000%8.8s", hAcnoAutopayAcctNo);
***/

            tempAmt = String.format("%012.2f", hCkapTransactionAmt);

            tempAmt = tempAmt.replace(".", "");

            vChiDate = String.format("%07d", comcr.str2long(hTempDeductDateNew)-19110000);

            buf = String.format("1%1sJ03    %7.7sS  %-14.14s%-10.10s%11.11s%10.10s%-10.10s1 1   %5.5s%35.35s%s",
                    wsPostFlag, vChiDate, tmpstr14, " ", tempAmt, " ", hAcctPSeqno, tempDiskNo, " ", endflag);
            writeTextFile(out, buf);
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hCkapChiName = "";
      //sqlCmd = "select chi_name ";
        sqlCmd = "select substr(chi_name,1,40) chi_name ";
        sqlCmd += "  from crd_idno  ";
        sqlCmd += " where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        hCkapChiName = getValue("chi_name");

    }

    /***********************************************************************/
    void selectCrdCorp() throws Exception {
        hCkapChiName = "";
      //sqlCmd = "select chi_name ";
        sqlCmd = "select substr(chi_name,1,40) chi_name ";
        sqlCmd += "  from crd_corp  ";
        sqlCmd += " where corp_p_seqno = ? ";
        setString(1, hAcnoCorpPSeqno);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        }
        hCkapChiName = getValue("chi_name");

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
        sqlCmd += "         item_date  > ? and item_date <= ? ))";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = '901'  ";
        sqlCmd += "   and acct_date  > ?  ";
        sqlCmd += "   and acct_date <= ? ";
        setString(1, hAcctPSeqno);
        setString(2, hWdayThisCloseDate);
        setString(3, hBusiBusinessDate);
        setString(4, hWdayThisCloseDate);
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
    void insertActOtherApay() throws Exception {

        daoTable = "act_other_apay";
        setValue("enter_acct_date", hTempDeductDateNew);
      //setValue("acct_bank", "700");
        setValue("acct_bank", hAcnoAutopayAcctBank);
        setValue("autopay_acct_no", hAcnoAutopayAcctNo);
        setValue("stmt_cycle", hAcnoStmtCycle);
        setValue("crt_date", hBusiBusinessDate);
        setValueDouble("transaction_amt", hCkapTransactionAmt);
        setValue("autopay_id", hAcnoAutopayId);
        setValue("p_seqno", hAcctPSeqno);
        setValue("acct_type", hAcctAcctType);
        setValue("id_p_seqno", hAcnoIdPSeqno);
        setValue("chi_name", hCkapChiName);
        setValue("status_code", "99");
        setValue("from_mark", "01");
        setValue("mod_user", "batch");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_other_apay duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void checkOpen() throws Exception {
        tmpFileName = String.format("POST006");
      //fileName = String.format("%s/media/rpt/%s_%8.8s.TXT", comc.getECSHOME(), tmpFileName, hBusiBusinessDate);
        fileNameFull = String.format("%s/media/act/%s_%8.8s.TXT", comc.getECSHOME(), tmpFileName, hBusiBusinessDate);
        fileNameFull = Normalizer.normalize(fileNameFull, java.text.Normalizer.Form.NFKD);
      //fileNameFull = String.format("%s/media/rpt/%s_%8.8s", comc.getECSHOME(), tmpFileName, hBusiBusinessDate);
      //fileNameFull = Normalizer.normalize(fileNameFull, java.text.Normalizer.Form.NFKD);
        out = openOutputText(fileNameFull, "MS950");
        if (out == -1) {
            comcr.errRtn(fileNameFull, "檔案開啓失敗！", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertPtrMediaCntl() throws Exception {
        daoTable = "ptr_media_cntl";
        setValue("external_name", tmpFileName);
        setValue("media_name", "BANK700O");
        setValue("business_date", hBusiBusinessDate);
        setValueInt("seq_no", 1);
        setValue("acct_bank", "700");
        setValue("acct_month", hWdaythisacctMonth);
        setValue("s_stmt_cycle", comc.getSubString(hTempFromDate, 6, 6 + 2));
        setValue("e_stmt_cycle", comc.getSubString(hTempEndDate, 6, 6 + 2));
        setValue("value_date", hTempDeductDateNew);
        setValue("out_file_flag", "Y");
        setValue("out_media_flag", "Y");
        setValue("proc_date", hSystemDate);
        setValue("proc_time", sysTime);
        setValue("trans_date", hSystemDate);
        setValue("trans_time", sysTime);
        setValue("mod_pgm", javaProgram);
        setValue("program_name", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ptr_media_cntl duplicate!", "", hCallBatchSeqno);
        }
    }

  //************************************************************************	
	void procFTP(String fileName, String filePath) throws Exception {
	  CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
		  
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	  commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	  commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(), 
	  MEDIA_FOLDER);
	  commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	  commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	  commFTP.hEflgModPgm = javaProgram;
	      
	//Temporary don't execute ftp
	//int errCode = 0;
	//showLogMessage("I", "", "put " + fileName + " 開始傳送....");
	//ecs_ref_ip_code.ref_ip_code="NCR2TCB"，其ftp_type="0"，在COMMFTP處理只是fileCopy
	  int errCode = commFTP.ftplogName("NCR2TCB", "put " + fileName);
	      
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

        ActB006 proc = new ActB006();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

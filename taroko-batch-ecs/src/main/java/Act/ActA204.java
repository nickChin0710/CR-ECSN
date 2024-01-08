/**********************************************************************************
 *                                                                                *
 *                              MODIFICATION LOG                                  *
 *                                                                                *
 *     DATE     Version    AUTHOR                       DESCRIPTION               *
 *  ---------  --------- ----------- ----------------------------------------     *
 *  111/12/26  V1.00.00    Ryan     program initial                               *
 *  112/05/08  V1.00.01    Ryan     調整首尾筆欄位長度                                                                                             *
 *  112/05/18  V1.00.02    Ryan     明細增加p_seqno欄位                                                                                      *
 *  112/06/16  V1.00.03    Ryan     ACT_AUTO_COMF增加欄位 AUTOPAY_COUNTS,  THIS_LASTPAY_DATE *
 *  112/07/05  V1.00.04    Ryan     ACT_AUTO_COMF增加欄位 AUTOPAY_COUNTS_20,25,  THIS_LASTPAY_DATE_20,25 *
 *  112/08/14  V1.00.05    Ryan     筆數大於0才insert act_auto_comf ,act_chkautopay by cycle 有筆數才累加  autopay_counts_xx *
 *  112/09/27  V1.00.06    Simon    1.非一扣至三扣期間或非工作日前一天不要送自扣空檔*
 *                                  2.一扣至三扣期間無符合扣繳資料亦要寫首、尾筆  *
 **********************************************************************************/

package Act;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

/*產生IBM自行自動扣繳檔*/
public class ActA204 extends AccessDAO {

    public final boolean DEBUG = false;

    private final String PROGNAME = "產生自行台幣自扣檔  112/09/27   V1.00.06 ";
	private final static String LINE_SEPERATOR = "\r\n";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommString comStr = new CommString();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;
    CommFTP commFTP = null;
    String prgmId = "ActA204";
    String fileName = "ECSAUTPAY.DAT";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "";
    int rptSeq1 = 0;
    String buf = "";
    String szTmp = "";
    StringBuffer  bufStr  = null;
    String hBusiBusinessDate = "";
    String hTempDeductDate = "";
    List<String> hmWdayStmtCycle = new ArrayList<String>();
    List<String> hmWdayThisAcctMonth = new ArrayList<String>();
    List<String> hmWdayThisCloseDate = new ArrayList<String>();
    List<String> hmWdayThisLastpayDate = new ArrayList<String>();
    List<String> hmWdayThisDelaypayDate = new ArrayList<String>();
    List<String> hmAgnnAcctType = new ArrayList<String>();
    List<Integer> hmAgnnAutopayBDueDays = new ArrayList<Integer>();
    List<Integer> hmAgnnAutopayDeductDays = new ArrayList<Integer>();
    List<Integer> hmAgnnInstpayBDueDays = new ArrayList<Integer>();
    List<Integer> hmAgnnInstpayDeductDays = new ArrayList<Integer>();
    List<String> hmTempFromDate = new ArrayList<String>();
    List<String> hmTempEndDate = new ArrayList<String>();
    List<String> hmTempNoneFlag = new ArrayList<String>();
    String temp1Date = "";
    String hTemp2Date = "";
    String hAcctFrom = "";
    String hAcctPSeqno = "";
    String hAcctAcctType = "";
    String hAcnoStmtCycle = "";
    String hAcnoAcctStatus = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoComboAcctNo = "";
    String hAcnoAutopayAcctNo = "";
    String hAcnoAutopayId = "";
    String hAcnoAutopayIndicator = "";
    String hAcnoNewCycleMonth = "";
    String hAcctPayByStageDate = "";
    double hAcctMinPay = 0;
    double hAcctTtlAmtBal = 0;
    double hAcurDcTtlAmt = 0;
    double hAcurDcAdjustDrAmt = 0;
    double hAcurDcAdjustCrAmt = 0;
    double hAcurDcPayAmt = 0;
    String hAcnoRowid = "";
    String hCkapAutopayAcctNo = "";
    double hCkapTransactionAmt = 0;
    String hCkapChiName = "";
    int hAucoDrCnt = 0;
    double hAucoDrAmt = 0;
  //double hJrnlTransactionAmt = 0;
    String hTempFromDate = "";
    String hTempEndDate = "";
    String hCardNo = "";
    String hConformFlag = "";
    
    double hDebtEndBal = 0;
    double hAcctAcctJrnlBal = 0;
    double hAcctAutopayBal = 0;
    double hAcctAdiEndBal = 0;
    double hAcctPayByStageBal = 0;

    String temp2Date = "";
    String tmpstr = "";
    String temstr = "";
    String chiDate = "";
    int totalCnt = 0;
    int testFlag = 0;
    double totalTransactionAmt = 0;
    double hJrnlDcTransactionAmt = 0;
    double hNextAdjustDrAmt = 0;
    double hNextAdjustCrAmt = 0;
    //double longAmt = 0;
    long long_amt = 0;
    private long ptrWorkdayCnt = 0;

    private String hWdayThisAcctMonth = "";
    private String hWdayThisCloseDate = "";
    private String hWdayThisDelaypayDate = "";
    private int hAgnnInstpayBDueDays = 0;
    private int hAgnnInstpayDeductDays = 0;
    private String hThisLastpayDate01 = "";
    private String hThisLastpayDate20 = "";
    private String hThisLastpayDate25 = "";
    private int hAutopayCounts01 = 0;
    private int hAutopayCounts20 = 0;
    private int hAutopayCounts25 = 0;
    int out       = -1;
    private int cycleCnt01 = 0;
    private int cycleCnt20 = 0;
    private int cycleCnt25 = 0;
    
    public int mainProcess(String[] args) {

      try {

        // ====================================
        // 固定要做的
        dateTime();
        setConsoleMode("Y");
        javaProgram = this.getClass().getName();
        showLogMessage("I", "", javaProgram + " " + PROGNAME);
        // =====================================
        
        // 固定要做的

        if (!connectDataBase()) {
            comc.errExit("connect DataBase error", "");
        }

        comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
        commFTP = new CommFTP(getDBconnect(), getDBalias());
    	  comr = new CommRoutine(getDBconnect(), getDBalias());
        
        if (args.length > 3) {
            comcr.errRtn("Usage : ActA204 [business_date [disp_type] [mqsend_flag]]", "", "");
        }

        hBusiBusinessDate = "";
        if ((args.length > 0) && (args[0].length() == 8)) {
            if (args[0].chars().allMatch( Character::isDigit ) ) {
            	hBusiBusinessDate = args[0];
            } else {
                // hBusiBusinessDate will use the value get from selectPtrBusinday()
            }
        }

        if (args.length >= 2)
            testFlag = comcr.str2int(args[1]);

        selectPtrBusinday();
        showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));

        showLogMessage("I", "", String.format("step 11 "));
        temp1Date = comcr.increaseDays(hBusiBusinessDate, 1);
        if (!temp1Date.equals(hTempDeductDate)) {
          showLogMessage("I", "", String.format("次日[%s]非營業日", hTempDeductDate));
        	hConformFlag = "N";
        }else {
          showLogMessage("I", "", String.format("次日[%s]為營業日", hTempDeductDate));
          selectPtrActgeneral();
        }
      //selectPtrActgeneral();

        showLogMessage("I", "", String.format("step 12 "));
        if ( !hConformFlag.equals("Y") ) {
          showLogMessage("I", "", String.format("本日無需產生送扣資料"));
        }

        if ( (testFlag == 1) || (!hConformFlag.equals("Y")) ) {
        	//comc.writeReport(temstr, lpar1, "MS950", false);
          //procFTP();
          //renameFile();
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        }

        checkOpen();

        showLogMessage("I", "", String.format("step 14 "));
// 一扣至三扣期間無符合扣繳資料亦要寫首、尾筆
      	bufStr = new StringBuffer();
        chiDate = String.format("%07d", comcr.str2long(hTempDeductDate) - 19110000);
        bufStr.append("H0000002");
        bufStr.append(String.format("%7.7s" , chiDate));
        bufStr.append(String.format("%-13.13s" , " "));
        bufStr.append(String.format("%-10.10s" , " "));
        bufStr.append(" ");
        bufStr.append(String.format("%4.4s" , " "));
        bufStr.append(String.format("%06d" , 0));
        bufStr.append(String.format("%013d" , 0));
        bufStr.append(String.format("%013d" , 0));
        bufStr.append(String.format("%125.125s" , " "));
        bufStr.append(LINE_SEPERATOR);
        buf = bufStr.toString();
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        selectActAcno();
        if(totalCnt > 0)
         	insertActAutoComf();

        showLogMessage("I", "", String.format("step 15 "));
            /*** 尾筆
             * 1 表頭 X(01) 1-1 固定H
             * 2 序號 X(06) 2-7 首筆必為000000
             * 3 轉帳類別 X(01) 8-8 固定放 2 (扣款交易)
             * 4 日期 X(07) 9-15 記帳日，需>=TODAY，為民國年(yyymmdd) 
             * 5 專戶帳號 X(13) 16-28 專戶入扣帳帳號，位數不足時，左靠右補空白 
             * 6 專戶統編 X(10) 29-38 專戶身份證號，位數不足時，左靠右補空白
             * 7 交易類別 X(01) 39-39 
             * 8 業務類別 X(04) 40-43 
             * 9 總筆數 9(5) 44-48 明細之總筆數數，位數不足時，右靠左補0，必為數字
             * 10 總金額 9(13) 49-61 明細之交易金額加總，整數位數不足時，右靠左補0，必為數字
             * 11 總手續費 9(13) 62-74 明細之手續費加總，整數位數不足時，右靠左補0，必為數字 
             * 12 FILLER X(126) 75-200 空白 
             */
     //if (totalCnt >= 0) {//即使為0筆亦要送首、尾筆
//				totalTransactionAmt = totalTransactionAmt * 100;
		  		bufStr = new StringBuffer();
		  		bufStr.append("E");
		  		bufStr.append(String.format("%06d", 0));
		  		bufStr.append("2");
		  		bufStr.append(String.format("%7.7s", chiDate));
		  		bufStr.append(String.format("%13.13s", " "));
		  		bufStr.append(String.format("%10.10s", " "));
		  		bufStr.append(" ");
		  		bufStr.append(String.format("%4.4s", " "));
		  		bufStr.append(String.format("%06d", totalCnt));
		  		bufStr.append(String.format("%014.2f", totalTransactionAmt).replace(".", ""));
		  		bufStr.append(String.format("%013d", 0));
		  		bufStr.append(String.format("%125.125s", " "));
		  		buf = bufStr.toString();
		  		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
      //} else {
      //  showLogMessage("I", "", String.format("沒有自動扣繳資料"));
      //}

        comc.writeReport(temstr, lpar1, "MS950", false);
        procFTP();
        renameFile();
         
        showLogMessage("I", "", String.format(" =============================================== "));
        showLogMessage("I", "", String.format("  自行自扣檔案:"));
        showLogMessage("I", "", String.format("      首筆 1筆, 尾筆 1筆"));
        showLogMessage("I", "", String.format("      本日扣款總筆數 [%d]", totalCnt));
        tmpstr = comcr.commFormat("3$,3$,3$,3$", totalTransactionAmt / 100);
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
    void selectActAcno() throws Exception {
        int int1 = 0;
        showLogMessage("I", "", String.format("step 21 "));
        sqlCmd = "select '1' acct_from,";
        sqlCmd += " a.p_seqno,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " b.stmt_cycle,";
        sqlCmd += " b.acct_status,";
        sqlCmd += " b.id_p_seqno,";
        // sqlCmd += " b.acct_holder_id,";
        // sqlCmd += " b.acct_holder_id_code,";
        sqlCmd += " b.corp_p_seqno,";
        sqlCmd += " b.combo_acct_no,";
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
        sqlCmd += " c.ttl_amt_bal,";
        sqlCmd += " c.dc_ttl_amt,";
        sqlCmd += " c.dc_adjust_dr_amt,";
        sqlCmd += " c.dc_adjust_cr_amt,";
        sqlCmd += " c.dc_pay_amt,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "  from act_acct a, act_acno b, act_acct_curr c ";
        sqlCmd += " where a.p_seqno   = b.acno_p_seqno ";
        sqlCmd += "   and b.acno_p_seqno   = c.p_seqno ";
        sqlCmd += "   and c.curr_code = '901' ";
        sqlCmd += "   and c.autopay_bal > 0 ";
      //sqlCmd += "   and c.ttl_amt_bal > 0 ";
        sqlCmd += "   and c.autopay_acct_bank = '0060567' ";
        sqlCmd += "   and c.autopay_acct_no  != '' ";
        sqlCmd += "   and ? >= decode(b.autopay_acct_s_date,'','00000000',b.autopay_acct_s_date) ";
        sqlCmd += "   and ? <  decode(b.autopay_acct_e_date,'','30001231',b.autopay_acct_e_date) ";
//        sqlCmd += "   and not exists (select 1 " + "  from act_manu_debit " + " where p_seqno   = a.p_seqno "
//                + "   and curr_code = '901' " + "   and apr_flag  = 'Y' " + "   and proc_flag = 'N') ";
        sqlCmd += " and b.fl_flag = '' and b.acct_status not in ('3','4') ";
//        sqlCmd += " UNION ";
//        sqlCmd += "select '2' acct_from, ";
//        sqlCmd += " a.p_seqno, ";
//        sqlCmd += " a.acct_type, ";
//        sqlCmd += " b.stmt_cycle, ";
//        sqlCmd += " b.acct_status, ";
//        sqlCmd += " b.id_p_seqno, ";
//        sqlCmd += " b.corp_p_seqno, ";
//        sqlCmd += " b.combo_acct_no, ";
//        sqlCmd += " b.autopay_acct_no, ";
//        sqlCmd += " b.autopay_id, ";
//        sqlCmd += " b.autopay_indicator, ";
//        sqlCmd += " b.new_cycle_month, ";
//        sqlCmd += " a.pay_by_stage_date, ";
//        sqlCmd += " a.pay_by_stage_bal, ";
//        sqlCmd += " c.acct_jrnl_bal, ";
//        sqlCmd += " d.debit_amt,    "; /* c.autopay_bal, */
//        sqlCmd += " a.adi_end_bal, ";
//        sqlCmd += " c.min_pay, ";
//        sqlCmd += " c.ttl_amt_bal, ";
//        sqlCmd += " c.dc_ttl_amt,";
//        sqlCmd += " c.dc_adjust_dr_amt,";
//        sqlCmd += " c.dc_adjust_cr_amt,";
//        sqlCmd += " c.dc_pay_amt,";
//        sqlCmd += " d.rowid rowid ";
//        sqlCmd += "  from act_acct a, act_acno b, act_acct_curr c , act_manu_debit d ";
//        sqlCmd += " where b.acno_p_seqno      = d.p_seqno ";
//        sqlCmd += "   and a.p_seqno           = d.p_seqno ";
//        sqlCmd += "   and c.p_seqno           = d.p_seqno ";
//        sqlCmd += "   and c.curr_code         = '901' ";
//        sqlCmd += "   and d.curr_code         = '901' ";
//        sqlCmd += "   and c.autopay_acct_bank = '017' ";
//        sqlCmd += "   and c.autopay_acct_no  != '' ";
//        sqlCmd += "   and d.apr_flag          = 'Y' ";
//        sqlCmd += "   and d.proc_flag         = 'N' ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hTempFromDate            = "";
            hTempEndDate             = "";
            hWdayThisAcctMonth      = "";
            hWdayThisCloseDate      = "";
            hWdayThisDelaypayDate   = "";
            hAgnnInstpayBDueDays   = 0;
            hAgnnInstpayDeductDays  = 0;
            hJrnlDcTransactionAmt   = 0;
            hNextAdjustDrAmt        = 0;
            hNextAdjustCrAmt        = 0;
            
            hAcctFrom = getValue("acct_from");
            hAcctPSeqno = getValue("p_seqno");
            hAcctAcctType = getValue("acct_type");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoComboAcctNo = getValue("combo_acct_no");
            hAcnoAutopayAcctNo = getValue("autopay_acct_no");
            hAcnoAutopayId = getValue("autopay_id");
            hAcnoAutopayIndicator = getValue("autopay_indicator");
            hAcnoNewCycleMonth = getValue("new_cycle_month");
            hAcctPayByStageDate = getValue("pay_by_stage_date");
            hAcctPayByStageBal = getValueDouble("pay_by_stage_bal");
            hAcctAcctJrnlBal = getValueDouble("acct_jrnl_bal");
            hAcctAutopayBal = getValueDouble("autopay_bal");
            hAcctAdiEndBal = getValueDouble("adi_end_bal");
            hAcctMinPay = getValueDouble("min_pay");
            hAcctTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAcurDcTtlAmt = getValueDouble("dc_ttl_amt");
            hAcurDcAdjustDrAmt = getValueDouble("dc_adjust_dr_amt");
            hAcurDcAdjustCrAmt = getValueDouble("dc_adjust_cr_amt");
            hAcurDcPayAmt = getValueDouble("dc_pay_amt");
          //h_jrnl_transaction_amt = 0;
            hAcnoRowid = getValue("rowid");

            if(comStr.empty(hAcnoAutopayId)) {
          	  //showLogMessage("I", "", String.format("act_acno.autopay_id is empty ,p_seqno = [%s] ", hAcctPSeqno));
          	  continue;
            }
            if (hAcctFrom.equals("1")) {
                for (int1 = 0; int1 < ptrWorkdayCnt; int1++) {
                    if("01".equals(hmWdayStmtCycle.get(int1)))
                        hThisLastpayDate01 = hmWdayThisLastpayDate.get(int1);
                    if("20".equals(hmWdayStmtCycle.get(int1)))
                    	hThisLastpayDate20 = hmWdayThisLastpayDate.get(int1);
                    if("25".equals(hmWdayStmtCycle.get(int1)))
                    	hThisLastpayDate25 = hmWdayThisLastpayDate.get(int1);
                    if (hmTempNoneFlag.get(int1).equals("Y"))
                        continue;
                    if ((!hAcctAcctType.equals(hmAgnnAcctType.get(int1))) 
                            || (!hAcnoStmtCycle.equals(hmWdayStmtCycle.get(int1))))
                        continue;
                    
                    hTempFromDate            = hmTempFromDate.get(int1);
                    hTempEndDate             = hmTempEndDate.get(int1);
                    hWdayThisAcctMonth      = hmWdayThisAcctMonth.get(int1);
                    hWdayThisCloseDate      = hmWdayThisCloseDate.get(int1);
                    hWdayThisDelaypayDate   = hmWdayThisDelaypayDate.get(int1);
                    hAgnnInstpayBDueDays   = hmAgnnInstpayBDueDays.get(int1);
                    hAgnnInstpayDeductDays  = hmAgnnInstpayDeductDays.get(int1);
              
                    break;
                }
                

                if (hAcnoIdPSeqno.length() != 0) {
                    if(selectCrdIdno() == 1) continue;
                } else {
                    if(selectCrdCorp() == 1) continue;
                }

                if (int1 >= ptrWorkdayCnt)
                    continue;
                if (hAcnoNewCycleMonth.compareTo(int1 >= ptrWorkdayCnt ? "" : hmWdayThisAcctMonth.get(int1)) > 0)
                    continue;
                if ((!hAcnoAcctStatus.equals("1")) && (hAcctPayByStageDate.length() != 0)) {
                    if (hAcctPayByStageBal <= 0)
                        continue;
                    temp1Date = comcr.increaseDays(hAcctPayByStageDate, -1);
                    temp2Date = comcr.increaseDays(temp1Date, hAgnnInstpayBDueDays - 1);
                    hTempFromDate = temp2Date;
                    temp1Date = comcr.increaseDays(temp2Date, hAgnnInstpayDeductDays + 1);
                    sqlCmd = "select to_char(to_date( ? ,'yyyymmdd')-1 days,'yyyymmdd') as h_temp_2_date ";
                    sqlCmd += " from dual ";
                    setString(1, temp1Date);
                    int recordCnt = selectTable();
                    if (recordCnt > 0) {
                    	hTemp2Date = getValue("h_temp_2_date");
                    }

                    if (DEBUG) {
                        showLogMessage("I", "", String.format("hBusiBusinessDate[%s], hTempFromDate[%s], hTemp2Date[%s]"
                                , hBusiBusinessDate, hTempFromDate, hTemp2Date));
                    }
                    if ((hBusiBusinessDate.compareTo(hTempFromDate) < 0)
                            || (hBusiBusinessDate.compareTo(hTemp2Date) > 0))
                        continue;

                    hAcctAutopayBal = hAcctPayByStageBal;
                }
                /***
                if ((h_acno_autopay_indicator.equals("2")) 
                        && (!hAcnoAcctStatus.equals("3"))
                        && (!hAcnoAcctStatus.equals("4"))) {
                    select_act_jrnl();
                    if (h_jrnl_transaction_amt >= h_acct_autopay_bal)
                        continue;
                }
                ***/
                selectActJrnl();
            } else if (hAcctFrom.equals("2")) {
                for (int1 = 0; int1 < ptrWorkdayCnt; int1++) {
                    if ((hAcctAcctType.equals(hmAgnnAcctType.get(int1))) &&
                        (hAcnoStmtCycle.equals(hmWdayStmtCycle.get(int1)))) {
                    	hTempFromDate            = hmTempFromDate.get(int1);
                        hTempEndDate             = hmTempEndDate.get(int1);
                        hWdayThisAcctMonth      = hmWdayThisAcctMonth.get(int1);
                        hWdayThisCloseDate      = hmWdayThisCloseDate.get(int1);
                        hWdayThisDelaypayDate   = hmWdayThisDelaypayDate.get(int1);
                        hAgnnInstpayBDueDays   = hmAgnnInstpayBDueDays.get(int1);
                        hAgnnInstpayDeductDays  = hmAgnnInstpayDeductDays.get(int1);
                        break;
                    }
                }
                
                if (hAcnoIdPSeqno.length() != 0)
                    selectCrdIdno();
                else
                    selectCrdCorp();
            }

            hAcctAutopayBal = hAcctAutopayBal - hJrnlDcTransactionAmt;
            hAcctTtlAmtBal = hAcurDcTtlAmt - hAcurDcPayAmt
                               - hAcurDcAdjustDrAmt + hNextAdjustDrAmt 
                               + hAcurDcAdjustCrAmt - hNextAdjustCrAmt;

            if (hAcctAutopayBal > hAcctTtlAmtBal)
                hCkapTransactionAmt = hAcctTtlAmtBal;
            else
                hCkapTransactionAmt = hAcctAutopayBal;

            /***
            if (h_acno_autopay_indicator.equals("2"))
                h_ckap_transaction_amt = h_acct_autopay_bal - h_jrnl_transaction_amt;

            if (h_ckap_transaction_amt > h_acct_ttl_amt_bal)
                h_ckap_transaction_amt = h_acct_ttl_amt_bal;

            long_amt = h_acct_adi_end_bal;
            h_ckap_transaction_amt = h_ckap_transaction_amt + long_amt;
            ***/

            if ((hAcnoAcctStatus.equals("3")) 
                    || (hAcnoAcctStatus.equals("4"))) {
                if (hAcctPayByStageDate.length() == 0) {
                	hCkapTransactionAmt = hAcctAcctJrnlBal;
                }
            }

            /* lai add R104019 */
          //if (hAcctFrom.equals("2"))
          //    hCkapTransactionAmt = hAcctAutopayBal;

            if (hCkapTransactionAmt <= 0)
                continue;

            tmpstr = comStr.right(hAcnoAutopayAcctNo,13);
            hCkapAutopayAcctNo = tmpstr;

            if(insertActChkautopay() == 1) continue;
            
            totalTransactionAmt = totalTransactionAmt + hCkapTransactionAmt;
            
            totalCnt++;
            if ((totalCnt % 5000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }

//            if (hAcctFrom.equals("2")) {
//                daoTable  = "act_manu_debit";
//                updateSQL = " proc_flag   = 'Y',";
//                updateSQL += " mod_time  = sysdate,";
//                updateSQL += " mod_pgm   = ? ";
//                whereStr = " where rowid =  ? ";
//                setString(1, javaProgram);
//                setRowId(2, hAcnoRowid);
//                updateTable();
//                if (notFound.equals("Y")) {
////                    comcr.errRtn("update_act_manu_debit not found!", "", hCallBatchSeqno);
//                    showLogMessage("I", "", "update_act_manu_debit not found!");
//                }
//            }

            /* 
                                    * 首筆
             * 1 表頭 X(01) 1-1 固定H
             * 2 序號 X(06) 2-7 首筆必為000000
             * 3 轉帳類別 X(01) 8-8 固定放 2 (扣款交易)
             * 4 日期 X(07) 9-15 記帳日，需>=TODAY，為民國年(yyymmdd) 
             * 5 專戶帳號 X(13) 16-28 專戶入扣帳帳號，位數不足時，左靠右補空白 
             * 6 專戶統編 X(10) 29-38 專戶身份證號，位數不足時，左靠右補空白
             * 7 交易類別 X(01) 39-39 
             * 8 業務類別 X(04) 40-43 
             * 9 總筆數 9(5) 44-48 都放0
             * 10 總金額 9(13) 49-61 都放0
             * 11 總手續費 9(13) 62-74 都放0 
             * 12 FILLER X(126) 75-200 空白 
             */
/*** 一扣至三扣期間無符合扣繳資料亦要寫首、尾筆
            if (totalCnt == 1) {
            	bufStr = new StringBuffer();
                chiDate = String.format("%07d", comcr.str2long(hTempDeductDate) - 19110000);
                bufStr.append("H0000002");
                bufStr.append(String.format("%7.7s" , chiDate));
                bufStr.append(String.format("%-13.13s" , " "));
                bufStr.append(String.format("%-10.10s" , " "));
                bufStr.append(" ");
                bufStr.append(String.format("%4.4s" , " "));
                bufStr.append(String.format("%06d" , 0));
                bufStr.append(String.format("%013d" , 0));
                bufStr.append(String.format("%013d" , 0));
                bufStr.append(String.format("%125.125s" , " "));
                bufStr.append(LINE_SEPERATOR);
                buf = bufStr.toString();
                lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
            }
***/
            if ((hTempDeductDate.compareTo(hWdayThisDelaypayDate) >= 0)
                    || (hBusiBusinessDate.equals(hTempEndDate)))
                tmpstr = String.format("Y");
            else
                tmpstr = String.format("N");

            if ((hAcnoAutopayAcctNo.equals(hAcnoComboAcctNo)) && (selectActDebt() == 0))
                tmpstr = String.format("N");

            selectCrdCard();
            /* 
                                    * 明細
             * 1 表頭 X(01) 1-1 固定D
             * 2 序號 X(06) 2-7 首筆必為000000
             * 3 轉帳類別 X(01) 8-8 固定放 2 (扣款交易)
             * 4 日期 X(07) 9-15 記帳日，需>=TODAY，為民國年(yyymmdd) 
             * 5 入扣帳號一 X(13) 16-28 扣帳帳號
             * 6 客戶統編 X(10) 29-38 客戶身份證號
             * 7 交易類別 X(01) 39-39 固定8 = 依業務類別顯示
             * 8 業務類別 X(04) 40-43 固定ICCN
             * 9 總金額 9(13) 44-56 明細之交易金額加總，整數位數不足時，右靠左補0，必為數字
             * 10 總手續費 9(13) 57-69 明細之手續費加總，整數位數不足時，右靠左補0，必為數字 
             * 11 受理單位 X(4) 70-73 先3144 卡部的分行別
             * 12 BC/BS 903掛帳科目 X(3) 74-76 固定218
             * 13 BC/BS 903掛帳塊數 X(1) 77-77 固定3
             * 14 入扣帳號二 X(13) 88-90 帳號一無法扣款，可使用帳號二轉帳：轉入帳號
             * 15 帳號二之客戶統編 X(10) 91-100 客戶身份證號
             * 16 幣別 X(3) 101-103 空白 
             * 17 p_seqno X(10) 104-113  帳務流水號
             * 18 FILLER X(50) 114-163 空白
             * 19 實際交易金額 9(11) 164-176不足補0
             * 20 回覆碼 X(4) 177-180 
             * 21 經銷商代號（存摺顯示）X(20) 181-200 卡號 (直接帶中文"合庫信用卡")
             */
            bufStr = new StringBuffer();
            bufStr.append("D");
            bufStr.append(String.format("%06d", totalCnt));
            bufStr.append("2");
            bufStr.append(String.format("%-7.7s", chiDate));
            bufStr.append(String.format("%-13.13s", hCkapAutopayAcctNo).replaceAll(" ", "0"));
            bufStr.append(String.format("%-10.10s", comStr.right(hAcnoAutopayId, 10)));
            bufStr.append("8");
            bufStr.append("ICCN");
            bufStr.append(String.format("%013.0f", hCkapTransactionAmt * 100));
            bufStr.append(String.format("%013d", 0));
            bufStr.append("3144");
            bufStr.append("218");
            bufStr.append("3");
            bufStr.append(String.format("%13.13s", " "));
            bufStr.append(String.format("%10.10s", " "));
            bufStr.append(String.format("%3.3s", " "));
            bufStr.append(String.format("%10.10s", hAcctPSeqno));
            bufStr.append(String.format("%50.50s", " "));
            bufStr.append(String.format("%013d", 0));
            bufStr.append(String.format("%4.4s", " "));
            bufStr.append(comc.fixLeft("合庫信用卡",20));
            bufStr.append(LINE_SEPERATOR);
            buf = bufStr.toString();
            if (DEBUG) {
                showLogMessage("I", "", buf);
            }
            lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectActJrnl() throws Exception {
        /***
        h_jrnl_transaction_amt = 0;
        sqlCmd = "select sum(transaction_amt) h_jrnl_transaction_amt ";
        sqlCmd += "  from act_jrnl  ";
        sqlCmd += " where p_seqno    =  ?  ";
        sqlCmd += "   and tran_class = 'P' ";
        sqlCmd += "   and tran_type != 'AUT1' ";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = '901'  ";
        sqlCmd += "   and acct_date  > ?  ";
        sqlCmd += "   and acct_date <= ? ";
        setString(1, h_acct_p_seqno);
        setString(2, h_wday_this_close_date);
        setString(3, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.err_rtn("select_act_jrnl not found!", "", hCallBatchSeqno);
        }
        h_jrnl_transaction_amt = getValueDouble("h_jrnl_transaction_amt");
        ***/

        sqlCmd  = "select sum(decode(tran_class,'P',dc_transaction_amt,0)) h_jrnl_dc_transaction_amt, ";
        sqlCmd += "       sum(decode(tran_class,'A',decode(dr_cr,'D',dc_transaction_amt,0),0)) h_next_adjust_dr_amt, ";
        sqlCmd += "       sum(decode(tran_class,'A',decode(dr_cr,'C',dc_transaction_amt,0),0)) h_next_adjust_cr_amt  ";
        sqlCmd += "  from act_jrnl  ";
        sqlCmd += " where p_seqno    =  ?  ";
        sqlCmd += "   and ((tran_class = 'P' and tran_type not in ('AUT1','AUT2','ACH1','REFU')) ";
        sqlCmd += "     or (tran_class = 'A' and tran_type not in ('OP01','OP02','OP03','OP04','AI01') and ";
        sqlCmd += "         item_date  > ? and item_date <= ? )) ";
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
//            comcr.errRtn("select_act_jrnl not found!", "", hCallBatchSeqno);
            showLogMessage("I", "", "select_act_jrnl not found!");
        }
        hJrnlDcTransactionAmt = getValueDouble("h_jrnl_dc_transaction_amt");
        hNextAdjustDrAmt = getValueDouble("h_next_adjust_dr_amt");
        hNextAdjustCrAmt = getValueDouble("h_next_adjust_cr_amt");
    }

    
    /**
     * @throws Exception *********************************************************************/
    int getMaxAutopayCounts(String cycle) throws Exception {
    	sqlCmd = "select ";
    	if("01".equals(cycle))
    		sqlCmd += " max(autopay_counts) as max_autopay_counts ";
    	if("20".equals(cycle))
    		sqlCmd += " max(autopay_counts_20) as max_autopay_counts ";
    	if("25".equals(cycle))
    		sqlCmd += " max(autopay_counts_25) as max_autopay_counts ";
    	sqlCmd	+= " from act_auto_comf where file_type= '1'";
    	if("01".equals(cycle)) {
    		sqlCmd += " and this_lastpay_date = ? ";
    		setString(1,hThisLastpayDate01);
    	}
    	if("20".equals(cycle)) {
    		sqlCmd += " and this_lastpay_date_20 = ? ";
    		setString(1,hThisLastpayDate20);
    	}
    	if("25".equals(cycle)) {
    		sqlCmd += " and this_lastpay_date_25 = ? ";
    		setString(1,hThisLastpayDate25);
    	}
    	int n = selectTable();
    	if(n>0) {
    		return getValueInt("max_autopay_counts") + 1;
    	}
    	return 0;
    }
    
    private void selectActChkautopay() throws Exception {
    	extendField = "autopay.";
    	sqlCmd = "select count(*) as cycle_cnt , b.stmt_cycle from act_chkautopay a , act_acno b where a.p_seqno = b.p_seqno ";
    	sqlCmd += " and a.enter_acct_date = ? and a.from_mark = '01' and a.curr_code = '901' ";
    	sqlCmd += " group by b.stmt_cycle ";
        setString(1, hTempDeductDate);
        int n = selectTable();
        for(int i = 0 ; i < n; i++) {
        	switch(getValue("autopay.stmt_cycle",i)) {
        	case "01":
        		cycleCnt01 = getValueInt("autopay.cycle_cnt",i);
        		break;
        	case "20":
        		cycleCnt20 = getValueInt("autopay.cycle_cnt",i);
        		break;
        	case "25":
        		cycleCnt25 = getValueInt("autopay.cycle_cnt",i);
        		break;
        	}
        }
    }
    
    /***********************************************************************/
    int selectCrdIdno() throws Exception {
        hCkapChiName = "";
      //sqlCmd = "select chi_name ";
        sqlCmd = "select substr(chi_name,1,40) chi_name";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        selectTable();
        if (notFound.equals("Y")) {
//            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        	showLogMessage("I", "", String.format("select_crd_idno not found! ,id_p_seqno = [%s]", hAcnoIdPSeqno));
        	return 1;
        }
        hCkapChiName = getValue("chi_name");
        return 0;
    }

    /***********************************************************************/
    int selectCrdCorp() throws Exception {
        hCkapChiName = "";
      //sqlCmd = "select chi_name ";
        sqlCmd = "select substr(chi_name,1,40) chi_name";
        sqlCmd += " from crd_corp  ";
        sqlCmd += "where corp_p_seqno = ? ";
        setString(1, hAcnoCorpPSeqno);
        selectTable();
        if (notFound.equals("Y")) {
//            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        	showLogMessage("I", "", String.format("select_crd_corp not found! ,corp_p_seqno = [%s]", hAcnoCorpPSeqno));
        	return 1;
        }
        hCkapChiName = getValue("chi_name");
        return 0;

    }
    
    void selectCrdCard() throws Exception {
		hCardNo = "";
		sqlCmd = "select card_no ";
		sqlCmd += " from crd_card  ";
		sqlCmd += "where p_seqno = ? and curr_code = '901' ";
		setString(1, hAcctPSeqno);
		selectTable();
		if (!notFound.equals("Y")) {
			hCardNo = getValue("card_no");
		}
    }

    /***********************************************************************/
    int insertActChkautopay() throws Exception {
        daoTable = "act_chkautopay";
        setValue("crt_date", hBusiBusinessDate);
        setValue("autopay_acct_no", hAcnoAutopayAcctNo);
        setValue("send_unit", "009");
        setValue("commerce_type", "100");
        setValue("data_type", "11");
        setValue("desc_code", "NU13");
        setValue("enter_acct_date", hTempDeductDate);
        setValueDouble("ori_transaction_amt", hCkapTransactionAmt);
        setValueDouble("transaction_amt", hCkapTransactionAmt);
        setValue("autopay_id", hAcnoAutopayId);
        setValue("p_seqno", hAcctPSeqno);
        setValue("acct_type", hAcctAcctType);
        setValue("id_p_seqno", hAcnoIdPSeqno);
        setValue("chi_name", hCkapChiName);
        setValue("status_code", "99");
        setValue("from_mark", "01");
        setValue("curr_code", "901");
        setValueDouble("dc_min_pay",hAcctMinPay);
        setValue("mod_user", "icbcecs");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
//         	showLogMessage("I", "", String.format("insert_act_chkautopay duplicate! ,P_SEQNO=[%s] ,ENTER_ACCT_DATE=[%s] ,FROM_MARK=[%s] ,CURR_CODE=[%s]", 
//        			hAcctPSeqno,hTempDeductDate,"01","901"));
        	int i = 1;
            daoTable  = "act_chkautopay";
            updateSQL = " crt_date  = ?";
            updateSQL += " ,autopay_acct_no  = ?";
            updateSQL += " ,send_unit   = '009'";
            updateSQL += " ,commerce_type   = '100'";
            updateSQL += " ,data_type   = '11'";
            updateSQL += " ,desc_code   = 'NU13'";
            updateSQL += " ,ori_transaction_amt   = ?";
            updateSQL += " ,transaction_amt   = ?";
            updateSQL += " ,autopay_id   = ?";
            updateSQL += " ,acct_type   = ?";
            updateSQL += " ,id_p_seqno   = ?";
            updateSQL += " ,chi_name   = ?";
            updateSQL += " ,status_code   = '99'";
            updateSQL += " ,dc_min_pay   = ?";
            updateSQL += " ,mod_user  = 'system'";
            updateSQL += " ,mod_time  = sysdate";
            updateSQL += " ,mod_pgm   = ? ";
            whereStr = " where p_seqno =  ?  and enter_acct_date = ? and from_mark = '01' and curr_code = '901' ";
            setString(i++, hBusiBusinessDate);
            setString(i++, hAcnoAutopayAcctNo);
            setDouble(i++, hCkapTransactionAmt);
            setDouble(i++, hCkapTransactionAmt);
            setString(i++, hAcnoAutopayId);
            setString(i++, hAcctAcctType);
            setString(i++, hAcnoIdPSeqno);
            setString(i++, hCkapChiName);
            setDouble(i++, hAcctMinPay);
            setString(i++, prgmId);
            setString(i++, hAcctPSeqno);
            setString(i++, hTempDeductDate);
            updateTable();
            if (notFound.equals("Y")) {
            	showLogMessage("I", "", String.format("insert_act_chkautopay duplicate! ,P_SEQNO=[%s] ,ENTER_ACCT_DATE=[%s] ,FROM_MARK=[%s] ,CURR_CODE=[%s]", 
            			hAcctPSeqno,hTempDeductDate,"01","901"));
            	return 1;
            }
        }
        return 0;
    }

    /***********************************************************************/
    double selectActDebt() throws Exception {
    	hDebtEndBal = 0;
        sqlCmd = " select sum(end_bal) h_debt_end_bal ";
        sqlCmd += "  from act_debt  ";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = '901'  ";
        sqlCmd += "   and acct_month <= ?  ";
        sqlCmd += "   and ACCT_CODE in ('BL','CA','ID','IT','AO','OT','CB','DB') "; // acct_code
        setString(1, hAcctPSeqno);
        setString(2, hWdayThisAcctMonth);
        if (selectTable() > 0) {
            hDebtEndBal = getValueDouble("h_debt_end_bal");
        }

        return (hDebtEndBal);
    }

    /***********************************************************************/
    int insertActAutoComf() throws Exception {
    	selectActChkautopay();
    	if(cycleCnt01 > 0)
    		hAutopayCounts01 = getMaxAutopayCounts("01");
    	if(cycleCnt20 > 0)
    		hAutopayCounts20 = getMaxAutopayCounts("20");
    	if(cycleCnt25 > 0)
    		hAutopayCounts25 = getMaxAutopayCounts("25");
        hAucoDrCnt = totalCnt;
        hAucoDrAmt = totalTransactionAmt;
        daoTable = "act_auto_comf";
        setValue("enter_acct_date", hTempDeductDate);
        setValue("file_type", "1");
        setValueInt("cr_cnt", 0);
        setValueInt("cr_amt", 0);
        setValueInt("dr_cnt", hAucoDrCnt);
        setValueDouble("dr_amt", hAucoDrAmt);
        setValueInt("autopay_counts" , hAutopayCounts01);
        setValueInt("autopay_counts_20" , hAutopayCounts20);
        setValueInt("autopay_counts_25" , hAutopayCounts25);
        setValue("this_lastpay_date" , hThisLastpayDate01);
        setValue("this_lastpay_date_20" , hThisLastpayDate20);
        setValue("this_lastpay_date_25" , hThisLastpayDate25);
        setValue("mod_user", "icbcecs");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            daoTable  = "act_auto_comf";
            updateSQL = " cr_cnt  = 0,";
            updateSQL += " cr_amt  = 0,";
            updateSQL += " dr_cnt   = ?,";
            updateSQL += " dr_amt   = ?,";
            updateSQL += " autopay_counts   = ?,";
            updateSQL += " autopay_counts_20   = ?,";
            updateSQL += " autopay_counts_25   = ?,";
            updateSQL += " this_lastpay_date   = ?,";
            updateSQL += " this_lastpay_date_20   = ?,";
            updateSQL += " this_lastpay_date_25   = ?,";
            updateSQL += " mod_user  = 'system',";
            updateSQL += " mod_time  = sysdate,";
            updateSQL += " mod_pgm   = ? ";
            whereStr = " where enter_acct_date =  ?  and file_type = '1'";
            setInt(1, hAucoDrCnt);
            setDouble(2, hAucoDrAmt);
            setInt(3, hAutopayCounts01);
            setInt(4, hAutopayCounts20);
            setInt(5, hAutopayCounts25);
            setString(6, hThisLastpayDate01);
            setString(7, hThisLastpayDate20);
            setString(8, hThisLastpayDate25);
            setString(9, javaProgram);
            setString(10, hTempDeductDate);
            updateTable();
            if (notFound.equals("Y")) {
            	showLogMessage("I", "", String.format("insertActAutoComf duplicate! ,enter_acct_date=[%s]", hTempDeductDate));
            	return 1;
            }
        }
        return 0;
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
    	hTempDeductDate = "";

        sqlCmd = " select decode(cast(? as varchar(8)),'',business_date, ? ) h_busi_business_date,";
        sqlCmd += " to_char(to_date(decode(cast(? as varchar(8)), '', business_date, ? ), 'yyyymmdd')+1 days,'yyyymmdd') h_temp_deduct_date ";
        sqlCmd += "  from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("selectPtrBusinday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_busi_business_date");
        hTempDeductDate = getValue("h_temp_deduct_date");
    }

    /***********************************************************************/
    void selectPtrActgeneral() throws Exception {

        hmTempFromDate.clear();
        hmTempEndDate.clear();
        hmTempNoneFlag.clear();
        hmWdayStmtCycle.clear();
        hmWdayThisAcctMonth.clear();
        hmWdayThisCloseDate.clear();
        hmWdayThisLastpayDate.clear();
        hmWdayThisDelaypayDate.clear();
        hmAgnnAcctType.clear();
        hmAgnnAutopayBDueDays.clear();
        hmAgnnAutopayDeductDays.clear();
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
        sqlCmd += " b.instpay_deduct_days ";
        sqlCmd += "  from ptr_workday a, ptr_actgeneral_n b ";
        sqlCmd += " order by b.acct_type, a.stmt_cycle ";
        ptrWorkdayCnt = selectTable();
        for (int i = 0; i < ptrWorkdayCnt; i++) {
            hmWdayStmtCycle.add(getValue("stmt_cycle", i));
            hmWdayThisAcctMonth.add(getValue("this_acct_month", i));
            hmWdayThisCloseDate.add(getValue("this_close_date", i));
            hmWdayThisLastpayDate.add(getValue("this_lastpay_date", i));
            hmWdayThisDelaypayDate.add(getValue("this_delaypay_date", i));
            hmAgnnAcctType.add(getValue("acct_type", i));
            hmAgnnAutopayBDueDays.add(getValueInt("autopay_b_due_days", i));
            hmAgnnAutopayDeductDays.add(getValueInt("autopay_deduct_days", i));
            hmAgnnInstpayBDueDays.add(getValueInt("instpay_b_due_days", i));
            hmAgnnInstpayDeductDays.add(getValueInt("instpay_deduct_days", i));

            temp1Date = comcr.increaseDays(hmWdayThisLastpayDate.get(i), -1);
            temp2Date = comcr.increaseDays(temp1Date, hmAgnnAutopayBDueDays.get(i) - 1);
            hTempFromDate = temp2Date;
            hmTempFromDate.add(temp2Date);

            temp1Date = comcr.increaseDays(temp2Date, hmAgnnAutopayDeductDays.get(i));
            sqlCmd = "select to_char(to_date( cast(? as varchar(8)) ,'yyyymmdd')-1 days,'yyyymmdd') as h_temp_2_date ";
            sqlCmd += "  from dual ";
            setString(1, temp1Date);

            if (selectTable() > 0) {
                hTemp2Date = getValue("h_temp_2_date");
            }
            hTempEndDate = hTemp2Date;
            hmTempEndDate.add(hTemp2Date);
            hmTempNoneFlag.add("");

            if (testFlag == 1) {
                showLogMessage("I", "", String.format("From_date[%s] - To_date[%s] stmt_cycle[%s] acct_type[%s]",
                        hmTempFromDate.get(i), hmTempEndDate.get(i), 
                        hmWdayStmtCycle.get(i), hmAgnnAcctType.get(i)));
                continue;
            }
            if ((hBusiBusinessDate.compareTo(hmTempFromDate.get(i)) < 0)
                    || (hBusiBusinessDate.compareTo(hmTempEndDate.get(i)) > 0)) {
                hmTempNoneFlag.set(i, "Y");
            } else {
                hConformFlag = "Y";
                showLogMessage("I", "", String.format("From_date[%s] - To_date[%s] stmt_cycle[%s] acct_type[%s]",
                        hmTempFromDate.get(i), hmTempEndDate.get(i), 
                        hmWdayStmtCycle.get(i), hmAgnnAcctType.get(i)));
            }
        }
    }
    
    void checkOpen() throws Exception {
        temstr = String.format("%s/media/act/%s", comc.getECSHOME(),fileName);
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
//        out = openOutputText(temstr, "MS950");
//        if (out == -1) {
//            comcr.errRtn(temstr, "檔案開啓失敗！", hCallBatchSeqno);
//        }
    }

    void procFTP() throws Exception {
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
    	commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    	commFTP.hEriaLocalDir = String.format("%s/media/act", comc.getECSHOME());
    	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    	commFTP.hEflgModPgm = javaProgram;

    	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
    	showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
    	int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fileName);

    	if (errCode != 0) {
    		showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料" + " errcode:" + errCode);
    		insertEcsNotifyLog();
    	}
    }
    
    public int insertEcsNotifyLog() throws Exception {
    	setValue("crt_date", sysDate);
    	setValue("crt_time", sysTime);
    	setValue("unit_code", comr.getObjectOwner("3", javaProgram));
    	setValue("obj_type", "3");
    	setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
    	setValue("notify_name", "媒體檔名:" + fileName);
    	setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
    	setValue("notify_desc2", "");
    	setValue("trans_seqno", commFTP.hEflgTransSeqno);
    	setValue("mod_time", sysDate + sysTime);
    	setValue("mod_pgm", javaProgram);
    	daoTable = "ecs_notify_log";

    	insertTable();

    	return (0);
    }

    void renameFile() throws Exception {
    	String tmpstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
    	String tmpstr2 = String.format("%s/media/act/backup/%s.%s", comc.getECSHOME(), fileName,sysDate+sysTime);

    	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
    		showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
    		return;
    	}
    	showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActA204 proc = new ActA204();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

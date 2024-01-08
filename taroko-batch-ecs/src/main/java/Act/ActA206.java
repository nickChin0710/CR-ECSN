/**********************************************************************************
 *                                                                                *
 *                              MODIFICATION LOG                                  *
 *                                                                                *
 *     DATE     Version    AUTHOR                       DESCRIPTION               *
 *  ---------  --------- ----------- ----------------------------------------     *
 *  112/01/09  V1.00.00    Ryan     program initial                               *
 *  112/06/07  V1.00.01    Ryan     檔名異動為CRD14                               *
 *  112/06/29  V1.00.02    Ryan     modify                                     *
 *  112/09/04  V1.00.03    Ryan     修改  寫入act_chkautopay.enter_acct_date ,改為 hWdayThisCloseDate   *
 *  112/09/27  V1.00.04    Simon    1.移除多餘skip條件                            *
 *                                  2.調整自扣回覆相關日期                        *
 *                                  3.符合花農自扣送扣篩選條件調整                *
 *  113/01/05  V1.00.03    Ryan     NCR2TCB改CRDATACREA                              *
 **********************************************************************************/

package Act;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

/*產生IBM自行自動扣繳檔*/
public class ActA206 extends AccessDAO {

    public final boolean DEBUG = false;

    private final String PROGNAME = "產生花農卡自動扣繳檔  112/09/27   V1.00.04 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate comDate = new CommDate();
    CommString comStr = new CommString();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;
    CommFTP commFTP = null;
    String prgmId = "ActA206";
    String fileName  = "CRD140380yyymmdd   .txt";
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
    StringBuffer bufStrHeader = null;
    String hBusiBusinessDate = "";
  //String hTempDeductDate = "";
    String hPreBusiDate = "";
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
    String hAcnoLastPayDate = "";
    String hAcctPayByStageDate = "";
    String hAcctMinPay = "";
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
    
    double hDebtEndBal = 0;
    double hAcctAcctJrnlBal = 0;
    double hAcctAutopayBal = 0;
    double hAcctAdiEndBal = 0;
    double hAcctPayByStageBal = 0;
    
    String hFlFlag = "";

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
    private String hWdayThisLastpayDate = "";
    private int hAgnnInstpayBDueDays = 0;
    private int hAgnnInstpayDeductDays = 0;
    int out       = -1;
    private String hThisLastpayDate = "";
    
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
            comcr.errRtn("Usage : ActA206 [business_date [disp_type] [mqsend_flag]]", "", "");
        }

        hBusiBusinessDate = "";
        if ((args.length > 0) && (args[0].length() == 8)) {
            if (args[0].chars().allMatch( Character::isDigit ) ) {
            	hBusiBusinessDate = args[0];
            } else {
                // hBusiBusinessDate will use the value get from selectPtrBusinday()
            }
        }

        selectPtrBusinday();

        if (!"02".equals(comStr.right(hBusiBusinessDate, 2))) {
        	testFlag = 1;
            showLogMessage("I", "", String.format("本日[%s]非每月2日", hBusiBusinessDate));
            finalProcess();
            return 0;
            
        }
        
        showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));
        fileName = fileName.replace("yyymmdd", comDate.toTwDate(hPreBusiDate));

        if (args.length >= 2)
            testFlag = comcr.str2int(args[1]);

        showLogMessage("I", "", String.format("step 11 "));
      //selectPtrActgeneral();
        showLogMessage("I", "", String.format("step 12 "));
        checkOpen();
//            if (testFlag == 1) {
//              	closeOutputText(out);
//              	comc.writeReport(temstr, lpar1, "MS950", true);
//                procFTP();
//                renameFile();
//                comcr.hCallErrorDesc = "程式執行結束";
//                comcr.callbatchEnd();
//                finalProcess();
//                return 0;
//            }

        lpar1.add(null);
        showLogMessage("I", "", String.format("step 14 "));
        selectActAcno();
        showLogMessage("I", "", String.format("step 15 "));
            
            	/* 
             	* 首筆
             	* RP133CRD140380 X(18)
             	* 1 筆數 9(07) 1-7 筆數合計右靠左補0
             	* 2 應繳金額總計 9X(12) 8-19  
             	* 3 最低應繳金額總計 9(12) 20-31 
             	* 4 FILLER X(84) 32-115 空白 
             	*/
        if (totalCnt > 0) {
    			totalTransactionAmt = totalTransactionAmt * 100;
  				if (insertActAutoComf() == 0) {
	 	  			bufStrHeader = new StringBuffer();
		  			bufStrHeader.append(String.format("%07d", totalCnt));
		  			bufStrHeader.append(String.format("%012.0f", totalTransactionAmt));
		  			bufStrHeader.append(String.format("%012.0f", totalTransactionAmt));
		  			bufStrHeader.append(String.format("%84.84s", " "));
		  			buf = bufStrHeader.toString();
		  			lpar1.set(0,comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		  		}
          comc.writeReport(temstr, lpar1, "MS950", true);
          procFTP();
          renameFile();
        } else {
         	lpar1.clear();
          showLogMessage("I", "", String.format("沒有自動扣繳資料"));
        }


        showLogMessage("I", "", String.format(" =============================================== "));
        showLogMessage("I", "", String.format("  自行自扣檔案:"));
//      showLogMessage("I", "", String.format("      首筆 1筆, 尾筆 1筆"));
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
        sqlCmd += " b.fl_flag, ";
        sqlCmd += " b.stmt_cycle,";
        sqlCmd += " p.this_close_date,";
        sqlCmd += " p.this_lastpay_date,";
        sqlCmd += " b.acct_status,";
        sqlCmd += " b.id_p_seqno,";
        sqlCmd += " b.last_pay_date,";
        sqlCmd += " b.corp_p_seqno,";
        sqlCmd += " b.combo_acct_no,";
        sqlCmd += " b.autopay_acct_no,";
        sqlCmd += " decode(b.autopay_id, ''  , UF_IDNO_ID(b.id_p_seqno) ,  b.autopay_id) autopay_id,";
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
        sqlCmd += "  from act_acct a, act_acno b, act_acct_curr c, ptr_workday p ";
        sqlCmd += " where a.p_seqno   = b.acno_p_seqno ";
        sqlCmd += "   and b.acno_p_seqno   = c.p_seqno ";
        sqlCmd += "   and c.curr_code = '901' ";
      //sqlCmd += "   and c.autopay_bal > 0 ";
        sqlCmd += "   and b.stmt_cycle = p.stmt_cycle ";
        sqlCmd += "   and c.ttl_amt_bal > 0 ";
      //sqlCmd += "   and substr(c.autopay_acct_bank,1,3) = '621'  ";
      //sqlCmd += "   and c.autopay_acct_no  != '' ";
      //sqlCmd += "   and ? >= decode(b.autopay_acct_s_date,'','00000000',b.autopay_acct_s_date) ";
      //sqlCmd += "   and ? <  decode(b.autopay_acct_e_date,'','30001231',b.autopay_acct_e_date) ";
//        sqlCmd += "   and not exists (select 1 " + "  from act_manu_debit " + " where p_seqno   = a.p_seqno "
//                + "   and curr_code = '901' " + "   and apr_flag  = 'Y' " + "   and proc_flag = 'N') ";
//        sqlCmd += " and b.fl_flag = 'Y'";
        sqlCmd += " and b.fl_flag <> '' and b.acct_status not in ('3','4') ";

      //setString(1, hBusiBusinessDate);
      //setString(2, hBusiBusinessDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hTempFromDate            = "";
            hTempEndDate             = "";
            hWdayThisAcctMonth      = "";
            hWdayThisCloseDate      = "";
            hWdayThisDelaypayDate   = "";
            hWdayThisLastpayDate = "";
            hAgnnInstpayBDueDays   = 0;
            hAgnnInstpayDeductDays  = 0;
            hJrnlDcTransactionAmt   = 0;
            hNextAdjustDrAmt        = 0;
            hNextAdjustCrAmt        = 0;
            
            hAcctFrom = getValue("acct_from");
            hAcctPSeqno = getValue("p_seqno");
            hAcctAcctType = getValue("acct_type");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hWdayThisCloseDate   = getValue("this_close_date");
            hWdayThisLastpayDate = getValue("this_lastpay_date");
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
            hAcctMinPay = getValue("min_pay");
            hAcctTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAcurDcTtlAmt = getValueDouble("dc_ttl_amt");
            hAcurDcAdjustDrAmt = getValueDouble("dc_adjust_dr_amt");
            hAcurDcAdjustCrAmt = getValueDouble("dc_adjust_cr_amt");
            hAcurDcPayAmt = getValueDouble("dc_pay_amt");
            hAcnoLastPayDate = getValue("last_pay_date");
            hFlFlag = getValue("fl_flag");
          //h_jrnl_transaction_amt = 0;
            hAcnoRowid = getValue("rowid");

/***
            if(comStr.empty(hAcnoAutopayId)) {
            	  showLogMessage("I", "", String.format("act_acno.autopay_id is empty ,p_seqno = [%s] ", hAcctPSeqno));
            	  continue;
              }

          //if (hAcctFrom.equals("1")) {
            for (int1 = 0; int1 < ptrWorkdayCnt; int1++) {
                if("01".equals(hmWdayStmtCycle.get(int1)))
                    hThisLastpayDate = hmWdayThisLastpayDate.get(int1);
                if ((hAcnoAcctStatus.equals("1")) && (hmTempNoneFlag.get(int1).equals("Y")))
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
                hWdayThisLastpayDate = hmWdayThisLastpayDate.get(int1);
                break;
            }
        
            if (hAcnoIdPSeqno.length() != 0) {
                if(selectCrdIdno() == 1) continue;
            } else {
                if(selectCrdCorp() == 1) continue;
            }

            if ((hAcnoAcctStatus.equals("1")) && (int1 >= ptrWorkdayCnt))
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
***/
            /***
            if ((h_acno_autopay_indicator.equals("2")) 
                    && (!hAcnoAcctStatus.equals("3"))
                    && (!hAcnoAcctStatus.equals("4"))) {
                select_act_jrnl();
                if (h_jrnl_transaction_amt >= h_acct_autopay_bal)
                    continue;
            }
            ***/
            /***
            selectActJrnl();

            hAcctAutopayBal = hAcctAutopayBal - hJrnlDcTransactionAmt;
            hAcctTtlAmtBal = hAcurDcTtlAmt - hAcurDcPayAmt
                               - hAcurDcAdjustDrAmt + hNextAdjustDrAmt 
                               + hAcurDcAdjustCrAmt - hNextAdjustCrAmt;

            if (hAcctAutopayBal > hAcctTtlAmtBal)
                hCkapTransactionAmt = hAcctTtlAmtBal;
            else
                hCkapTransactionAmt = hAcctAutopayBal;
            ***/
            hCkapTransactionAmt = hAcctTtlAmtBal;

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

/***
            if ((hTempDeductDate.compareTo(hWdayThisDelaypayDate) >= 0)
                    || (hBusiBusinessDate.equals(hTempEndDate)))
                tmpstr = String.format("Y");
            else
                tmpstr = String.format("N");

            if ((hAcnoAutopayAcctNo.equals(hAcnoComboAcctNo)) && (selectActDebt() == 0))
                tmpstr = String.format("N");
***/
            selectCrdCard();
            /* 
                                    * 明細
             * RP133CRD140380 X(18) 
             * 1  帳單結帳日 X(07) 1-7 明國年(yyymmdd)
             * 2  信用卡號 X(16) 8-23 
             * 3  身份證號 X(10) 24-33 
             * 4  身份證號錯誤記號 X(01) 34-34 
             * 5  應繳金額正負號 X(01) 35-35 
             * 6  應繳金額 9(12) 36-47
             * 7  最低應繳金額正負號 X(01) 48-48 
             * 8  最低應繳金額 9(12) 49-60
             * 9  繳款截止日 X(07) 61-67 明國年(yyymmdd)
             * 10 農會代號 X(01) 68-68 ACT_ACNO.FL_FLAG
             * 17 FILLER X(47) 69-115 空白 
             */

            String tChiCloseDate="", tChiLastpayDate="";
            bufStr = new StringBuffer();
//            bufStr.append(String.format("%-18.18s", "RP133CRD140380"));
            tChiCloseDate = String.format("%07d", comcr.str2long(hWdayThisCloseDate) - 19110000);
            tChiLastpayDate = String.format("%07d", comcr.str2long(hWdayThisLastpayDate) - 19110000);
            bufStr.append(String.format("%7.7s", tChiCloseDate));
            bufStr.append(String.format("%16.16s", hCardNo));
            bufStr.append(String.format("%10.10s", comStr.right(hAcnoAutopayId, 10)));
            bufStr.append(" ");
            bufStr.append(hCkapTransactionAmt>=0?"+":"-");
            bufStr.append(String.format("%012.0f", hCkapTransactionAmt * 100));
            bufStr.append("+");
            bufStr.append(String.format("%012d", 100000));
            bufStr.append(String.format("%7.7s", tChiLastpayDate));
            bufStr.append(hFlFlag);
            bufStr.append(String.format("%47.47s", " "));
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
        setValue("enter_acct_date", hWdayThisCloseDate);//hTempDeductDate 改 hWdayThisCloseDate
        setValueDouble("ori_transaction_amt", hCkapTransactionAmt);
        setValueDouble("transaction_amt", hCkapTransactionAmt);
        setValue("autopay_id", hAcnoAutopayId);
        setValue("p_seqno", hAcctPSeqno);
        setValue("acct_type", hAcctAcctType);
        setValue("id_p_seqno", hAcnoIdPSeqno);
        setValue("chi_name", hCkapChiName);
        setValue("status_code", "99");
        setValue("from_mark", "02");
        setValue("curr_code", "901");
        setValue("mod_user", "icbcecs");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
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
            updateSQL += " ,mod_user  = 'system'";
            updateSQL += " ,mod_time  = sysdate";
            updateSQL += " ,mod_pgm   = ? ";
            whereStr = " where p_seqno =  ?  and enter_acct_date = ? and from_mark = '02' and curr_code = '901' ";
            setString(i++, hBusiBusinessDate);
            setString(i++, hAcnoAutopayAcctNo);
            setDouble(i++, hCkapTransactionAmt);
            setDouble(i++, hCkapTransactionAmt);
            setString(i++, hAcnoAutopayId);
            setString(i++, hAcctAcctType);
            setString(i++, hAcnoIdPSeqno);
            setString(i++, hCkapChiName);
            setString(i++, prgmId);
            setString(i++, hAcctPSeqno);
            setString(i++, hWdayThisCloseDate);
            updateTable();
            if (notFound.equals("Y")) {
            	showLogMessage("I", "", String.format("insert_act_chkautopay duplicate! ,P_SEQNO=[%s] ,ENTER_ACCT_DATE=[%s] ,FROM_MARK=[%s] ,CURR_CODE=[%s]", 
            			hAcctPSeqno,hWdayThisCloseDate,"01","901"));
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
        hAucoDrCnt = totalCnt;
        hAucoDrAmt = totalTransactionAmt;
        daoTable = "act_auto_comf";
        setValue("enter_acct_date", hWdayThisCloseDate);
        setValue("file_type", "4");
        setValueInt("cr_cnt", 0);
        setValueInt("cr_amt", 0);
        setValueInt("dr_cnt", hAucoDrCnt);
        setValueDouble("dr_amt", hAucoDrAmt);
        setValue("autopay_counts","1");
        setValue("this_lastpay_date",hWdayThisLastpayDate);
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
            updateSQL += " autopay_counts   = '1',";
            updateSQL += " this_lastpay_date   = ?,";
            updateSQL += " mod_user  = 'system',";
            updateSQL += " mod_time  = sysdate,";
            updateSQL += " mod_pgm   = ? ";
            whereStr = " where enter_acct_date =  ?  and file_type = '4'";
            setInt(1, hAucoDrCnt);
            setDouble(2, hAucoDrAmt);
            setString(3, hWdayThisLastpayDate);
            setString(4, javaProgram);
            setString(5, hWdayThisCloseDate);
            updateTable();
            if (notFound.equals("Y")) {
            	showLogMessage("I", "", String.format("insertActAutoComf duplicate! ,enter_acct_date=[%s]", hWdayThisCloseDate));
            	return 1;
            }
        }
        return 0;
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
    	hPreBusiDate = "";

      sqlCmd = " select decode(cast(? as varchar(8)),'',business_date, ? ) h_busi_business_date,";
      sqlCmd += " to_char(to_date(decode(cast(? as varchar(8)), '', business_date, ? ), 'yyyymmdd')-1 days,'yyyymmdd') h_pre_business_date ";
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
      hPreBusiDate = getValue("h_pre_business_date");
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
    	commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    	commFTP.hEriaLocalDir = String.format("%s/media/act", comc.getECSHOME());
    	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    	commFTP.hEflgModPgm = javaProgram;

    	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
    	showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
    	int errCode = commFTP.ftplogName("CRDATACREA", "mput " + fileName);

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
        ActA206 proc = new ActA206();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

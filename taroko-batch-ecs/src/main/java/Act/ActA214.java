/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/10/25  V1.00.00    Ryan     program initial                           *
 *  111/12/22  V1.00.01    Ryan     LAYOUT 增加16碼卡號,  LAYOUT長度由120 加到 200                    *
 *  112/06/16  V1.00.02    Ryan     ACT_AUTO_COMF增加欄位 AUTOPAY_COUNTS,  THIS_LASTPAY_DATE *
 *  112/06/30  V1.00.03    Ryan       修正台幣換匯帳號未產生 *
 *  112/07/05  V1.00.04    Ryan     判斷,  autpay_counts >=4  台幣換匯帳號 才有值                *
 *  112/08/16  V1.00.05    Ryan     每個月底不執行                                                                                               *
 *  112/09/27  V1.00.06    Simon    1.非一扣至月底前一天期間或非工作日前一天不要送自扣空檔*
 *                                  2.一扣至月底前一天期間無符合扣繳資料亦要寫首、尾筆*
 ******************************************************************************/

package Act;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

/*產生IBM雙幣自動扣繳檔*/
public class ActA214 extends AccessDAO {

    public final boolean DEBUG = false;

    private final String PROGNAME = "產生自行雙幣自扣檔 112/09/27 V1.00.06";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommString comStr = new CommString();
    CommCrdRoutine comcr    = null;
    CommRoutine comr = null;
    CommFTP commFTP = null;
    CommCol commCol = null;
    String prgmId                = "ActA214";
    String fileName = "ECSAUTPAYD.DAT";
    String hModUser            = "";
    long   hModSeqno           = 0;
    String hCallBatchSeqno    = "";
    String hModPgm             = "";
    String hCallRProgramCode = "";
    String buf                   = "";

    String szTmp = "";

    String hBusiBusinessDate        = "";
    String hTempDeductDate          = "";
    String hWdayThisAcctMonth      = "";
    String hWdayThisCloseDate      = "";
    String hWdayThisLastpayDate    = "";
    String hWdayThisDelaypayDate   = "";
    int    hAgnnInstpayBDueDays   = 0;
    int    hAgnnInstpayDeductDays  = 0;
    String temp1Date                 = "";
    String hTemp2Date               = "";
    String hAcctFrom                 = "";
    String hAcctPSeqno              = "";
    String hAcctAcctType            = "";
    String hAcctAcctKey             = "";
    String hAcnoStmtCycle           = "";
    String hAcctStat                 = "";
    String hAcnoAcctStatus          = "";
    String hAcnoIdPSeqno           = "";
    String hAcnoCorpNo              = "";
    String hAcnoCorpPSeqno         = "";
    String hAcnoComboAcctNo        = "";
    String hAcnoAutopayAcctNo      = "";
    String hAcnoAutopayId           = "";
    String hAcurAutopayIndicator    = "";
    String hAcctPayByStageDate    = "";
    double hAcctPayByStageBal     = 0;
    double hAcurDcAcctJrnlBal     = 0;
    double hAcurDcAutopayBal       = 0;
    double hAcctAdiEndBal          = 0;
	double hAcurDcMinPayBal       = 0;
    double hAcurDcTtlAmtBal       = 0;
	double hAcurDcMinPay           = 0;
	double hAcurDcTtlAmt           = 0;
	double hAcurDcAdjustDrAmt     = 0;
	double hAcurDcAdjustCrAmt     = 0;
    double hAcurDcPayAmt           = 0;
    String hAcurAutopayAcctNo      = "";
    String hAcurAutopayId           = "";
    String hAcurAutopayDcFlag      = "";
    String hAcurAutopayDcIndicator = "";
    String hAcnoAutopayAcctSDate  = "";
    String hAcnoAutopayAcctEDate  = "";
    String hAcurCurrCode            = "";
    String hPcceCurrCodeGl         = "";
    String hAcnoRowid                = "";
    String hCkapAutopayAcctNo      = "";
    double hCkapTransactionAmt      = 0;
    String hCkapChiName             = "";
    int    hAucoDrCnt               = 0;
    double hAucoDrAmt               = 0;
    String hIdnoBirthday             = "";
    double hJrnlDcTransactionAmt   = 0;
	double hNextAdjustDrAmt        = 0;   
    double hNextAdjustCrAmt        = 0;
    double hDebtEndBal              = 0;
    String hTempFromDate            = "";
    String hTempEndDate             = "";
    String hCurrEngName             = "";
    String hCardNo             = "";
    String hConformFlag = "";

    String hCurrChangeAccout  = "";
    private String hThisLastpayDate = "";
    private int hAutopayCounts = 0;

    String temstr                = "";
    String tmpstr                = "";
    String tempX10              = "";
    String szTmp1                = "";
    String temp2Date           = "";
    int    testFlag             = 0;
    int    totalCnt             = 0;
    int    intCmd               = 0;
    int    ptrWorkdayCnt       = 0;
    double totalTransactionAmt = 0;
    long   tempLong             = 0;
    double tempDouble           = 0;
    int    tempInt              = 0;
    long   longAmt              = 0;
    int    hSetTwAcctcnt        = 0;

    List<String>  aTempFromDate           = new ArrayList<String>();
    List<String>  aTempEndDate            = new ArrayList<String>();
    List<String>  aTempNoneFlag           = new ArrayList<String>();
    List<String>  aWdayStmtCycle          = new ArrayList<String>();
    List<String>  aWdayThisAcctMonth     = new ArrayList<String>();
    List<String>  aWdayThisCloseDate     = new ArrayList<String>();
	  List<String>  aWdayNextCloseDate     = new ArrayList<String>();
    List<String>  aWdayThisLastpayDate   = new ArrayList<String>();
    List<String>  aWdayThisDelaypayDate  = new ArrayList<String>();
    List<String>  aAgnnAcctType           = new ArrayList<String>();
    List<Integer> aAgnnAutopayBDueDays  = new ArrayList<Integer>();
    List<Integer> aAgnnAutopayDeductDays = new ArrayList<Integer>();
    List<Integer> aAgnnInstpayBDueDays  = new ArrayList<Integer>();
    List<Integer> aAgnnInstpayDeductDays = new ArrayList<Integer>();

    Buf1           sendData = new Buf1();
    int out       = -1;

    public int mainProcess(String[] args) {

      try {

        // ====================================
        // 固定要做的
        dateTime();
        setConsoleMode("Y");
        javaProgram = this.getClass().getName();
        showLogMessage("I", "", javaProgram + " " + PROGNAME);
        // =====================================
        if (args.length > 2) {
            showLogMessage("I", "", "Usage : ActA214 [businessDate [dispType] ");
            return 0;
        }

        // 固定要做的

        if (!connectDataBase()) {
            comc.errExit("connect DataBase error", "");
        }

//      hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
        comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    	  comr = new CommRoutine(getDBconnect(), getDBalias());
    	  commFTP = new CommFTP(getDBconnect(), getDBalias());
    	  commCol = new CommCol(getDBconnect(), getDBalias());
//      comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

        hBusiBusinessDate = "";
        if ((args.length > 0) && (args[0].length() == 8)) {
            String sGArgs0 = "";
            sGArgs0 = args[0];
            sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
            hBusiBusinessDate = sGArgs0;
        }

        if (args.length >= 2)
            testFlag = comcr.str2int(args[1]);

        selectPtrBusinday();
      	showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));
        
        String lastDate = commCol.lastdateOfmonth(hBusiBusinessDate);
        temp1Date = comcr.increaseDays(hBusiBusinessDate, 1);
        if(lastDate.equals(hBusiBusinessDate)) {
          showLogMessage("I", "", String.format("本月月底不執行[%s]", hBusiBusinessDate));
        	hConformFlag = "N";
        }else if (!temp1Date.equals(hTempDeductDate)) {
          showLogMessage("I", "", String.format("次日[%s]非營業日", hTempDeductDate));
        	hConformFlag = "N";
        }else{
          showLogMessage("I", "", String.format("次日[%s]為營業日", hTempDeductDate));
          selectPtrActgeneral();
        }
      //selectPtrActgeneral();

        if ( !hConformFlag.equals("Y") ) {
          showLogMessage("I", "", String.format("本日無需產生送扣資料"));
        }

        if ( (testFlag == 1) || (!hConformFlag.equals("Y")) ) {
          //closeOutputText(out);
          //procFTP();
          //renameFile();
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        }
        
        checkOpen();

        selectActAcno();

        if (totalCnt > 0) {
            insertActAutoComf();
        } 

        closeOutputText(out);
        procFTP();
        renameFile();

        showLogMessage("I", "", String.format(" =============================================== "));
        showLogMessage("I", "", String.format("  外幣自行自扣檔案:"));
        showLogMessage("I", "", String.format("  外幣送扣無首、尾筆"));
        showLogMessage("I", "", String.format("      本日扣款總筆數 [%d]", totalCnt));
        tmpstr = comcr.commFormat("1$,3$,3$,3$.2$", totalTransactionAmt);
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
    void insertActAutoComf() throws Exception {
    	hAutopayCounts = getMaxAutopayCounts();
        hAucoDrCnt = totalCnt;
        hAucoDrAmt = totalTransactionAmt;
        daoTable = "act_auto_comf";
        setValue("enter_acct_date", hTempDeductDate);
        setValue("file_type", "3");
        setValueInt("cr_cnt", 0);
        setValueDouble("cr_amt", 0);
        setValueInt("dr_cnt", hAucoDrCnt);
        setValueDouble("dr_amt", hAucoDrAmt);
        setValueInt("autopay_counts" , hAutopayCounts);
        setValue("this_lastpay_date" , hThisLastpayDate);
        setValue("mod_user", "system");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        insertTable();
        if (dupRecord.equals("Y")) {
            daoTable  = "act_auto_comf";
            updateSQL = " cr_cnt  = 0,";
            updateSQL += " cr_amt  = 0,";
            updateSQL += " dr_cnt   = ?,";
            updateSQL += " dr_amt   = ?,";
            updateSQL += " mod_user  = 'system',";
            updateSQL += " mod_time  = sysdate,";
            updateSQL += " mod_pgm   = ? ";
            whereStr = " where enter_acct_date =  ?  and file_type = '3'";
            setInt(1, hAucoDrCnt);
            setDouble(2, hAucoDrAmt);
            setString(3, prgmId);
            setString(4, hTempDeductDate);
            updateTable();
            if (notFound.equals("Y")) {
//            	comcr.errRtn("insert_act_auto_comf duplicate!", "", hCallBatchSeqno);
            	showLogMessage("I", "", String.format("insertActAutoComf duplicate! ,enter_acct_date=[%s]", hTempDeductDate));
            }
        }
    }

  /***********************************************************************/
  void selectActAcno() throws Exception {

    int int1 = 0;
    sqlCmd = "select '1' h_acct_from,";
    sqlCmd += " a.p_seqno,";
    sqlCmd += " a.acct_type,";
    sqlCmd += " b.acct_key,"; // act_acno.acct_key
    sqlCmd += " b.stmt_cycle,";
    sqlCmd += " decode(b.acct_status,1,0,1) h_acct_stat,";
    sqlCmd += " b.acct_status,";
    sqlCmd += " b.id_p_seqno,";
    sqlCmd += " b.corp_p_seqno,";
    sqlCmd += " b.combo_acct_no,";
    sqlCmd += " b.autopay_acct_no,"; /* 台幣 */
    sqlCmd += " decode(b.autopay_id, ''  , UF_IDNO_ID(b.id_p_seqno) ,  b.autopay_id) h_acno_autopay_id,";
    sqlCmd += " c.autopay_indicator,";
    sqlCmd += " a.pay_by_stage_date,";
    sqlCmd += " a.pay_by_stage_bal,";
    sqlCmd += " c.dc_acct_jrnl_bal,"; /* 原a.acct_jrnl_bal (RECS-s1050412-026 update) */
    sqlCmd += " c.dc_autopay_bal,";
    sqlCmd += " a.adi_end_bal,";
    sqlCmd += " c.dc_min_pay_bal,";
    sqlCmd += " c.dc_ttl_amt_bal,";
    sqlCmd += " c.dc_min_pay,";
    sqlCmd += " c.dc_ttl_amt,";
    sqlCmd += " c.dc_adjust_dr_amt,";
    sqlCmd += " c.dc_adjust_cr_amt,";
    sqlCmd += " c.dc_pay_amt,";
    sqlCmd += " c.autopay_acct_no as h_acur_autopay_acct_no,  "; /* 外幣 */
    //sqlCmd += " c.autopay_id,"; /* 外幣 */
    sqlCmd += " decode(c.autopay_id,'',UF_IDNO_ID(b.id_p_seqno),c.autopay_id) h_dc_autopay_id, ";
    sqlCmd += " c.autopay_dc_flag,";
    sqlCmd += " c.autopay_dc_indicator,"; /* RECS-s1050412-026 add */
    sqlCmd += " decode(b.autopay_acct_s_date,'','19110101',b.autopay_acct_s_date) h_acno_autopay_acct_s_date,";
    sqlCmd += " decode(b.autopay_acct_e_date,'','29991231',b.autopay_acct_e_date) h_acno_autopay_acct_e_date,";
    sqlCmd += " c.curr_code,";
    sqlCmd += " d.curr_code_gl,";
    sqlCmd += " d.curr_eng_name,";
    sqlCmd += " a.rowid rowid, ";
    sqlCmd += " c.curr_change_accout ";
    sqlCmd += "  from act_acct a,act_acno b,act_acct_curr c, ptr_currcode d ";
    sqlCmd += " where a.p_seqno           = b.acno_p_seqno ";
    sqlCmd += "   and b.acno_p_seqno      = c.p_seqno ";
    sqlCmd += "   and decode(c.curr_code,'','901',c.curr_code)  = d.curr_code ";
    sqlCmd += "   and decode(c.curr_code,'','901',c.curr_code) <> '901' ";
    sqlCmd += "   and c.dc_autopay_bal    > 0 ";
    sqlCmd += "   and c.autopay_acct_bank = '0060567' ";
    sqlCmd += "   and c.autopay_acct_no  != '' ";
    sqlCmd += "   and b.stmt_cycle = '01' and b.acct_type = '01' and b.acct_status not in ('3','4') ";
    sqlCmd += " order by 5, 6, 2 ";
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
      hJrnlDcTransactionAmt   = 0;
      hNextAdjustDrAmt        = 0;
      hNextAdjustCrAmt        = 0;

      hAcctFrom = getValue("h_acct_from");
      hAcctPSeqno = getValue("p_seqno");
      hAcctAcctType = getValue("acct_type");
      hAcctAcctKey = getValue("acct_key");
      hAcnoStmtCycle = getValue("stmt_cycle");
      hAcctStat = getValue("h_acct_stat");
      hAcnoAcctStatus = getValue("acct_status");
      hAcnoIdPSeqno = getValue("id_p_seqno");
      hAcnoCorpNo = getValue("corp_no");
      hAcnoCorpPSeqno = getValue("corp_p_seqno");
      hAcnoComboAcctNo = getValue("combo_acct_no");
      hAcnoAutopayAcctNo = getValue("autopay_acct_no");
      hAcnoAutopayId = getValue("h_acno_autopay_id");
      hAcurAutopayIndicator = getValue("autopay_indicator");
      hAcctPayByStageDate = getValue("pay_by_stage_date");
      hAcctPayByStageBal = getValueDouble("pay_by_stage_bal");
      hAcurDcAcctJrnlBal = getValueDouble("dc_acct_jrnl_bal");
      hAcurDcAutopayBal = getValueDouble("dc_autopay_bal");
      hAcctAdiEndBal = getValueDouble("adi_end_bal");
      hAcurDcMinPayBal = getValueDouble("dc_min_pay_bal");
      hAcurDcTtlAmtBal = getValueDouble("dc_ttl_amt_bal");
      hAcurDcMinPay = getValueDouble("dc_min_pay");
      hAcurDcTtlAmt = getValueDouble("dc_ttl_amt");
      hAcurDcAdjustDrAmt = getValueDouble("dc_adjust_dr_amt");
      hAcurDcAdjustCrAmt = getValueDouble("dc_adjust_cr_amt");
      hAcurDcPayAmt = getValueDouble("dc_pay_amt");
      hAcurAutopayAcctNo = getValue("h_acur_autopay_acct_no");
      hAcurAutopayId = getValue("h_dc_autopay_id");
      hAcurAutopayDcFlag = getValue("autopay_dc_flag");
      hAcurAutopayDcIndicator = getValue("autopay_dc_indicator");
      hAcnoAutopayAcctSDate = getValue("h_acno_autopay_acct_s_date");
      hAcnoAutopayAcctEDate = getValue("h_acno_autopay_acct_e_date");
      hAcurCurrCode = getValue("curr_code");
      hPcceCurrCodeGl = getValue("curr_code_gl");
      hCurrEngName = getValue("curr_eng_name");
      hAcnoRowid = getValue("rowid");
      hCurrChangeAccout = getValue("curr_change_accout");

			hTempFromDate = "";
			hTempEndDate = "";
			hWdayThisAcctMonth = "";
			hWdayThisCloseDate = "";
			hWdayThisLastpayDate  = "";
			hWdayThisDelaypayDate = "";
			hAgnnInstpayBDueDays = 0;
			hAgnnInstpayDeductDays = 0;
			for (int1 = 0; int1 < ptrWorkdayCnt; int1++) {
		
				if (aTempNoneFlag.get(int1).equals("Y"))
					continue;
				if ((!hAcctAcctType.equals(aAgnnAcctType.get(int1)))
						|| (!hAcnoStmtCycle.equals(aWdayStmtCycle.get(int1))))
					continue;
				hTempFromDate = aTempFromDate.get(int1);
				hTempEndDate = aTempEndDate.get(int1);
				hWdayThisAcctMonth = aWdayThisAcctMonth.get(int1);
				hWdayThisCloseDate = aWdayThisCloseDate.get(int1);
				hWdayThisLastpayDate  = aWdayThisLastpayDate.get(int1);
				hWdayThisDelaypayDate = aWdayThisDelaypayDate.get(int1);
				hAgnnInstpayBDueDays = aAgnnInstpayBDueDays.get(int1);
				hAgnnInstpayDeductDays = aAgnnInstpayDeductDays.get(int1);
				hThisLastpayDate = aWdayThisLastpayDate.get(int1);
			  break;
			}

			hIdnoBirthday = "";

			if (hAcnoIdPSeqno.length() != 0) {
				if(selectCrdIdno()==1)
					continue;
			}else {
				if(selectCrdCorp()==1)
					continue;
			}
			if (int1 >= ptrWorkdayCnt)
				continue;

			/***
			 * h_acct_pay_by_stage_bal 為台幣 if ((!hAcnoAcctStatus.equals("1")) &&
			 * (hAcctPayByStageDate.length() != 0)) { if (hAcctPayByStageBal <= 0) continue;
			 * temp1Date = comcr.increaseDays(hAcctPayByStageDate, -1); temp2Date =
			 * comcr.increaseDays(temp1Date, hAgnnInstpayBDueDays - 1); hTempFromDate =
			 * temp2Date; temp1Date = comcr.increaseDays(temp2Date, hAgnnInstpayDeductDays +
			 * 1); sqlCmd = "select to_char(to_date( ? ,'yyyymmdd')-1 days,'yyyymmdd') ";
			 * sqlCmd += " from dual "; setString(1, temp1Date); int recordCnt2 =
			 * selectTable(); if (recordCnt2 > 0) { hTemp2Date = getValue("h_temp_2_date");
			 * }
			 * 
			 * hAcurDcAutopayBal = hAcctPayByStageBal; }
			 ***/

			if ((hBusiBusinessDate.compareTo(hAcnoAutopayAcctSDate) < 0)
					|| (hBusiBusinessDate.compareTo(hAcnoAutopayAcctEDate) > 0)) {
				hAcnoAutopayAcctNo = "";
			}

			/***
			 * if ((h_acur_autopay_indicator.equals("2")) &&
			 * (!h_acno_acct_status.equals("3")) && (!h_acno_acct_status.equals("4"))) {
			 * select_act_jrnl(); if (h_jrnl_dc_transaction_amt >= h_acur_dc_autopay_bal)
			 * continue; }
			 ***/
			if (selectActJrnl() == 1)
				continue;

      hAcurDcAutopayBal = hAcurDcAutopayBal - hJrnlDcTransactionAmt;
      hAcurDcTtlAmtBal  = hAcurDcTtlAmt - hAcurDcPayAmt
      				  - hAcurDcAdjustDrAmt + hNextAdjustDrAmt 
                        + hAcurDcAdjustCrAmt - hNextAdjustCrAmt;

      if (hAcurDcAutopayBal > hAcurDcTtlAmtBal)
          hCkapTransactionAmt = hAcurDcTtlAmtBal;
      else
          hCkapTransactionAmt = hAcurDcAutopayBal;

			/*  h_acct_acct_jrnl_bal 為台幣 h_acur_dc_acct_jrnl_bal 為外幣*/
			if((hAcnoAcctStatus.equals("3"))||
               (hAcnoAcctStatus.equals("4")))
        if (hAcctPayByStageDate.length()==0) 
            hCkapTransactionAmt = hAcurDcAcctJrnlBal;
					
            /* lai add R104019 */
//            if (hAcctFrom.equals("2"))
//                hCkapTransactionAmt = hAcurDcAcctJrnlBal;

      hCkapTransactionAmt = convAmt(hCkapTransactionAmt);

      if (hCkapTransactionAmt <= 0)
          continue;

      tmpstr = comStr.right(hAcnoAutopayAcctNo,13);
      hCkapAutopayAcctNo = tmpstr;
		
      if(insertActChkautopay() == 1) {
      	continue;
      }
      
      totalTransactionAmt = totalTransactionAmt + hCkapTransactionAmt;

      if ((hTempDeductDate.compareTo(hWdayThisDelaypayDate) >= 0)
              || (hBusiBusinessDate.equals(hTempEndDate)))
          tmpstr = String.format("Y");
      else
          tmpstr = String.format("N");
      if ((hAcnoAutopayAcctNo.equals(hAcnoComboAcctNo)) && (selectActDebt() == 0))
          tmpstr = String.format("N");

      selectCrdCard();
      
      sendData.clearBuf1();

      sendData.procKind = "2";
      tempX10 = String.format("%07d", comcr.str2int(hTempDeductDate) - 19110000);
      sendData.procDate = tempX10;

      sendData.id = hAcurAutopayId;
      sendData.currCode = hCurrEngName;
      
      sendData.acctNof = hAcurAutopayAcctNo;

      //temp_double = h_ckap_transaction_amt;
    //temp_int = (int) ((float) (h_ckap_transaction_amt * 100));
    //temp_int = temp_int / 100;
    //temp_double = (temp_double - temp_int);
    //temp_double = (float) (temp_double * 100.0);
    //temp_x10 = String.format("%08d%02.0f", temp_int, temp_double);
      tempX10 = String.format("%013.0f", hCkapTransactionAmt * 100);
      sendData.amtf = tempX10;

		  //temp_long = (long) ((float) (h_acur_dc_min_pay_bal * 100));
      hAcurDcMinPayBal = hAcurDcMinPay - hAcurDcPayAmt;
      if (hAcurDcMinPayBal > hAcurDcMinPay)
          hAcurDcMinPayBal = hAcurDcMinPay;
				
		  //temp_long  = (long) ((float) (h_acur_dc_min_pay_bal * 100));
      hAcurDcMinPayBal = convAmt(hAcurDcMinPayBal);
      tempLong = (long) ((float) (hAcurDcMinPayBal * 100));
			
 			if (hAcurDcTtlAmtBal > hAcurDcTtlAmt)
          hAcurDcTtlAmtBal = hAcurDcTtlAmt;

		  if(hAcurAutopayDcIndicator.equals("1"))
             //temp_long = (long) ((float) (h_acur_dc_ttl_amt_bal * 100));
      {  
         hAcurDcTtlAmtBal = convAmt(hAcurDcTtlAmtBal);
         tempLong  = (long) (hAcurDcTtlAmtBal * 100);
      }
      
      if(tempLong < 0)
         tempLong = 0;
			   
//            tempX10 = String.format("%010d", tempLong);
//            sendData.minAmtf = tempX10;

//            if (hAcurAutopayDcFlag.equals("Y") && tempLong > 0) {
//                sendData.acctNol = hAcnoAutopayAcctNo;
//            }
            

      if(isCloseDate4(hWdayThisLastpayDate,hTempDeductDate)) {
         sendData.acctNol = hCurrChangeAccout;
         hSetTwAcctcnt++;
      }
	
  if (hSetTwAcctcnt <= 100) {
  showLogMessage("I", "", String.format("LastpayDate[%s]", hWdayThisLastpayDate));
	showLogMessage("I", "", String.format("DeductDate[%s]", hTempDeductDate));
	showLogMessage("I", "", String.format("sendData.acctNol [%s]", sendData.acctNol));
  }
           
      sendData.pSeqno = hAcctPSeqno;
            
      sendData.crdCard = hCardNo;
      buf = sendData.allText();
      writeTextFile(out, buf);
      writeTextFile(out, "\r\n");
            
			totalCnt++;
      if ((totalCnt % 5000) == 0) {
           showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
      }
    }
    closeCursor(cursorIndex);

  }
    
    /****
     * 
     * @param closeDay
     * @param businessDate
     * @return
     * @throws Exception
     */
    boolean isCloseDate4(String closeDay,String businessDate) throws Exception{
//    	int day = 0;
//    	int i = 0;
//    	String lastDay ="";
//    	
//    	while(i < 4){
//    		StringBuffer str = new StringBuffer();
//    		str.append(" select to_char(to_date(?,'YYYYMMDD') + ");
//    		str.append(day);
//    		str.append(" day,'YYYYMMDD') as last_day from dual ");
//    		sqlCmd = str.toString();
//    		setString(1,closeDay);
//    		selectTable();
//    		lastDay = getValue("last_day");
//    		sqlCmd = "select count(*) as holiday_cnt from ptr_holiday  where holiday = ? ";
//    		setString(1, lastDay);
//    		selectTable();
//    		int cnt = getValueInt("holiday_cnt");
//    		if( cnt > 0){//為假日
//    			day++;
//    			continue;
//    		}
//    		i++;
//    	}
//    	if(businessDate.compareTo(lastDay)>0)
//    		return true;
//    	else
//    		return false;
    	
    	int count = getAutopayCounts(closeDay,businessDate);
    	if(count == -1) {
    		count = getMaxAutopayCounts();
    	}
    	if(count >= 4)
    		return true;
    	return false;
    }

	/*** conv_amt(x) 有以下兩點作用：
     1.四捨五入到小數以下第二位
     2.double 變數運算後會發生 .99999999...的問題，例如 19.125, 
       實際會變成 19.1249999999999999...，所以執行 conv_amt(x)變成 19.13
    ***/
    public double  convAmt(double cvtAmt) throws Exception
    {
      long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.00001);
      double cvtDouble =  ((double) cvtLong) / 100;
      return cvtDouble;
    }
	
    /***********************************************************************/
    int selectActJrnl() throws Exception {	      

		sqlCmd  = "select sum(decode(tran_class,'P',dc_transaction_amt,0)) h_jrnl_dc_transaction_amt, ";
        sqlCmd += "       sum(decode(tran_class,'A',decode(dr_cr,'D',dc_transaction_amt,0),0)) h_next_adjust_dr_amt, ";
        sqlCmd += "       sum(decode(tran_class,'A',decode(dr_cr,'C',dc_transaction_amt,0),0)) h_next_adjust_cr_amt  ";
        sqlCmd += "  from act_jrnl  ";
        sqlCmd += " where p_seqno    =  ?  ";
        sqlCmd += "   and ((tran_class = 'P' and tran_type not in ('AUT1','AUT2','ACH1','REFU')) ";
        sqlCmd += "     or (tran_class = 'A' and tran_type not in ('OP01','OP02','OP03','OP04','AI01') and ";
        sqlCmd += "         item_date  > ? and item_date <= ? )) ";
      //sqlCmd += "   and decode(curr_code,'','901',curr_code) = '901'  ";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = ?  ";
        sqlCmd += "   and acct_date  > ?  ";
        sqlCmd += "   and acct_date <= ? ";
        setString(1, hAcctPSeqno);
        setString(2, hWdayThisCloseDate);
        setString(3, hBusiBusinessDate);
        setString(4, hAcurCurrCode);
        setString(5, hWdayThisCloseDate);
        setString(6, hBusiBusinessDate);
 		selectTable();
        if (notFound.equals("Y")) {
//            comcr.errRtn("select_act_jrnl not found!", "", hCallBatchSeqno);
        	showLogMessage("I", "", String.format("select_act_jrnl not found! ,p_seqno=[%s] ,item_date>[%s] ,item_date<=[%s] ,curr_code=[%s] ,acct_date>[%s] ,acct_date<=[%s]", 
        			hAcctPSeqno ,hWdayThisCloseDate ,hBusiBusinessDate ,hAcurCurrCode ,hWdayThisCloseDate ,hBusiBusinessDate));
        	return 1;
        }
        hJrnlDcTransactionAmt = getValueDouble("h_jrnl_dc_transaction_amt");
        hNextAdjustDrAmt = getValueDouble("h_next_adjust_dr_amt");
        hNextAdjustCrAmt = getValueDouble("h_next_adjust_cr_amt");
        return 0;
     }

    /***********************************************************************/
    int selectCrdIdno() throws Exception {
        hCkapChiName = "";
        //sqlCmd = "select chi_name,";
        sqlCmd = "select substr(chi_name,1,40) chi_name,";
        sqlCmd += " birthday ";
        sqlCmd += "  from crd_idno  ";
        sqlCmd += " where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        int recordCnt = selectTable();
        
        if (notFound.equals("Y")) {
//            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        	showLogMessage("I", "", String.format("select_crd_idno not found! ,id_p_seqno=[%s]", hAcnoIdPSeqno));
        	return 1;
        }
        
        if (recordCnt > 0) {
            hCkapChiName = getValue("chi_name");
            hIdnoBirthday = getValue("birthday");
        }
        return 0;
    }

    /***********************************************************************/
    int selectCrdCorp() throws Exception {
        hCkapChiName = "";
        //sqlCmd = "select chi_name ";
        sqlCmd = "select substr(chi_name,1,40) chi_name";
        sqlCmd += "  from crd_corp  ";
        sqlCmd += " where corp_p_seqno = ? ";
        setString(1, hAcnoCorpPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
//            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        	showLogMessage("I", "", String.format("select_crd_corp not found! ,corp_p_seqno=[%s]", hAcnoCorpPSeqno));
        	return 1;
        }
        if (recordCnt > 0) {
            hCkapChiName = getValue("chi_name");
        }
        return 0;
    }
    
    /***********************************************************************/
    //卡號讀取發卡日最近的雙幣卡
    int selectCrdCard() throws Exception {
    	hCardNo = "";
        sqlCmd = "select card_no ";
        sqlCmd += " from crd_card  ";
        sqlCmd += " where p_seqno = ? and curr_code = ? ";
        sqlCmd += " order by issue_date desc fetch first 1 rows only ";
        setString(1, hAcctPSeqno);
        setString(2, hAcurCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	showLogMessage("I", "", String.format("selectCrdCard not found! ,p_seqno=[%s]", hAcctPSeqno));
        	return 1;
        }
        if (recordCnt > 0) {
        	hCardNo = getValue("card_no");
        }
        return 0;
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
        setValue("autopay_id", hAcurAutopayId);
        setValue("p_seqno", hAcctPSeqno);
        setValue("acct_type", hAcctAcctType);
        setValue("id_p_seqno", hAcnoIdPSeqno);
        setValue("chi_name", hCkapChiName);
        setValue("status_code", "99");
        setValue("from_mark", "01");
        setValue("CURR_CODE", hAcurCurrCode);
        setValue("DC_AUTOPAY_ACCT_NO", hAcurAutopayAcctNo);
        setValue("DC_AUTOPAY_ID", hAcurAutopayId);
        setValueDouble("DC_MIN_PAY",
                hAcurAutopayDcIndicator.equals("1") ? hAcurDcTtlAmtBal : hAcurDcMinPayBal);
        setValueDouble("DC_TRANSACTION_AMT", hCkapTransactionAmt);
        setValue("mod_user", "icbcecs");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
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
            updateSQL += " ,from_mark   = '01'";
            updateSQL += " ,dc_autopay_acct_no   = ?";
            updateSQL += " ,dc_autopay_id   = ?";
            updateSQL += " ,dc_min_pay   = ?";
            updateSQL += " ,dc_transaction_amt   = ?";
            updateSQL += " ,mod_user  = 'system'";
            updateSQL += " ,mod_time  = sysdate";
            updateSQL += " ,mod_pgm   = ? ";
            whereStr = " where p_seqno =  ?  and enter_acct_date = ? and from_mark = '01' and curr_code = ? ";
            setString(i++, hBusiBusinessDate);
            setString(i++, hAcnoAutopayAcctNo);
            setDouble(i++, hCkapTransactionAmt);
            setDouble(i++, hCkapTransactionAmt);
            setString(i++, hAcnoAutopayId);
            setString(i++, hAcctAcctType);
            setString(i++, hAcnoIdPSeqno);
            setString(i++, hCkapChiName);
            setString(i++, hAcurAutopayAcctNo);
            setString(i++, hAcnoAutopayId);
            setDouble(i++, hAcurAutopayDcIndicator.equals("1") ? hAcurDcTtlAmtBal : hAcurDcMinPayBal);
            setDouble(i++, hCkapTransactionAmt);
            setString(i++, prgmId);
            setString(i++, hAcctPSeqno);
            setString(i++, hTempDeductDate);
            setString(i++, hAcurCurrCode);
            updateTable();
            if (notFound.equals("Y")) {
            	showLogMessage("I", "", String.format("insert_act_chkautopay duplicate! ,P_SEQNO=[%s] ,ENTER_ACCT_DATE=[%s] ,FROM_MARK=[%s] ,CURR_CODE=[%s]", 
            			hAcctPSeqno,hTempDeductDate,"01",hAcurCurrCode));
            	return 1;
            }
        }
        
        return 0;
    }

    /**
     * @throws Exception *********************************************************************/
    int getMaxAutopayCounts() throws Exception {
    	sqlCmd = "select max(autopay_counts) as max_autopay_counts from act_auto_comf where file_type= '3' and this_lastpay_date = ? ";
    	setString(1,hThisLastpayDate);
    	selectTable();
    	return getValueInt("max_autopay_counts") + 1;
    }
    
    /**
     * @throws Exception *********************************************************************/
    int getAutopayCounts(String thisLastpayDate ,String businessDate) throws Exception {
    	sqlCmd = "select autopay_counts from act_auto_comf where file_type= '3' and this_lastpay_date = ? and enter_acct_date = ? ";
    	setString(1,thisLastpayDate);
    	setString(2,businessDate);
    	int n = selectTable();
    	if(n>0) {
    		return getValueInt("autopay_counts");
    	}
    	return -1;
    }
    
    /***********************************************************************/
    double selectActDebt() throws Exception {
        hDebtEndBal = 0;
        sqlCmd = "select sum(end_bal) h_debt_end_bal ";
        sqlCmd += "  from act_debt";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = ?  ";
        sqlCmd += "   and acct_month <= ?  ";
        sqlCmd += "   and acct_code in ('BL','CA','ID','IT','AO','OT','CB','DB') ";// acct_code
        setString(1, hAcctPSeqno);
        setString(2, hAcurCurrCode);
        setString(3, hWdayThisAcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDebtEndBal = getValueDouble("h_debt_end_bal");
        }

        return (hDebtEndBal);
    }

    /***********************************************************************/
    int selectPtrBusinday() throws Exception {
        hTempDeductDate = "";

        sqlCmd = "select decode(cast(? as varchar(8)) ,'',business_date, ? ) h_busi_business_date,";
        sqlCmd += " to_char(to_date(decode(cast(? as varchar(8)) ,'',business_date, ? ), 'yyyymmdd')+1 days,'yyyymmdd') h_temp_deduct_date ";
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
        }
        return 0;
    }
    
    /*************************************************************************/
    int selectPtrHoliday() throws Exception {
        int hTemp1Cnt = 0;

        sqlCmd += "SELECT  count(*) h_temp_1_cnt";
        sqlCmd += "  FROM  ptr_holiday";
        sqlCmd += " where  holiday = ? ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTemp1Cnt = getValueInt("h_temp_1_cnt");
        }
        return (hTemp1Cnt);
    }

    /***********************************************************************/
    void selectPtrActgeneral() throws Exception {
        aTempFromDate.clear();
        aTempEndDate.clear();
        aTempNoneFlag.clear();
        aWdayStmtCycle.clear();
        aWdayThisAcctMonth.clear();
        aWdayThisCloseDate.clear();
		aWdayNextCloseDate.clear();
        aWdayThisLastpayDate.clear();
        aWdayThisDelaypayDate.clear();
        aAgnnAcctType.clear();
        aAgnnAutopayBDueDays.clear();
        aAgnnAutopayDeductDays.clear();
        aAgnnInstpayBDueDays.clear();
        aAgnnInstpayDeductDays.clear();
        sqlCmd = "select a.stmt_cycle,";
        sqlCmd += " a.this_acct_month,";
        sqlCmd += " a.this_close_date,";
        sqlCmd += " a.next_close_date,";
        sqlCmd += " a.this_lastpay_date,";
        sqlCmd += " a.this_delaypay_date,";
        sqlCmd += " b.acct_type,";
        sqlCmd += " b.autopay_b_due_days,";
        sqlCmd += " b.autopay_deduct_days,";
        sqlCmd += " b.instpay_b_due_days,";
        sqlCmd += " b.instpay_deduct_days ";
        sqlCmd += "  from ptr_workday a, ptr_actgeneral_n b  ";
        sqlCmd += " where a.stmt_cycle = '01' and b.acct_type = '01' ";
        sqlCmd += " order by b.acct_type,a.stmt_cycle ";
        ptrWorkdayCnt = selectTable();
        for (int i = 0; i < ptrWorkdayCnt; i++) {
            aWdayStmtCycle.add(getValue("stmt_cycle", i));
            aWdayThisAcctMonth.add(getValue("this_acct_month", i));
            aWdayThisCloseDate.add(getValue("this_close_date", i));
			aWdayNextCloseDate.add(getValue("next_close_date", i));
            aWdayThisLastpayDate.add(getValue("this_lastpay_date", i));
            aWdayThisDelaypayDate.add(getValue("this_delaypay_date", i));
            aAgnnAcctType.add(getValue("acct_type", i));
            aAgnnAutopayBDueDays.add(getValueInt("autopay_b_due_days", i));
            aAgnnAutopayDeductDays.add(getValueInt("autopay_deduct_days", i));
            aAgnnInstpayBDueDays.add(getValueInt("instpay_b_due_days", i));
            aAgnnInstpayDeductDays.add(getValueInt("instpay_deduct_days", i));

            temp1Date = comcr.increaseDays(aWdayThisLastpayDate.get(i), -1);
            temp2Date = comcr.increaseDays(temp1Date, aAgnnAutopayBDueDays.get(i) - 1);
            hTempFromDate = temp2Date;
            aTempFromDate.add(i, temp2Date);
			
            /*
			temp_1_date = comcr.increase_days(temp_2_date, a_agnn_autopay_deduct_days.get(i) + 30);
             * lai increase_days(temp_2_date,h_m_agnn_autopay_deduct_days[int1],
             * temp_1_date);
             */
          //temp_1_date = a_wday_next_close_date.get(i);
            temp1Date = String.format("%8.8s", aWdayNextCloseDate.get(i));
            sqlCmd = "select to_char(to_date( ? ,'yyyymmdd')-1 days,'yyyymmdd') h_temp_2_date";
            sqlCmd += "  from dual ";
            setString(1, temp1Date);
            int recordCnt2 = selectTable();
            if (recordCnt2 > 0) {
                hTemp2Date = getValue("h_temp_2_date");
            }
            hTempEndDate = hTemp2Date;
            aTempEndDate.add(i, hTemp2Date);

            if (testFlag == 1) {
                showLogMessage("I", "", String.format("From_date[%s] - To_date[%s] stmt_cycle[%s] acct_type[%s]",
                        aTempFromDate.get(i), aTempEndDate.get(i), aWdayStmtCycle.get(i), aAgnnAcctType.get(i)));
                continue;
            }
            if ((hBusiBusinessDate.compareTo(aTempFromDate.get(i)) < 0)
                    || (hBusiBusinessDate.compareTo(aTempEndDate.get(i)) > 0)) {
                aTempNoneFlag.add(i, "Y");
            } else {
              	hConformFlag = "Y";
                aTempNoneFlag.add(i, "");
                showLogMessage("I", "", String.format("From_date[%s] - To_date[%s] stmt_cycle[%s] acct_type[%s]",
                        aTempFromDate.get(i), aTempEndDate.get(i), aWdayStmtCycle.get(i), aAgnnAcctType.get(i)));
            }
        }
    }

    /***********************************************************************/
    void checkOpen() throws Exception {
        temstr = String.format("%s/media/act/%s", comc.getECSHOME(),fileName);
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        out = openOutputText(temstr, "MS950");
        if (out == -1) {
            comcr.errRtn(temstr, "檔案開啓失敗！", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActA214 proc = new ActA214();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    String removeDot(String myStr) {
        return myStr.replaceAll("\\.", "");
    }

    /***********************************************************************/
    /*
     * 
     * Detail 
     * 1 資料辨別碼 X(01) 01-01 固定為 "2" 
     * 2 扣款日 X(07) 02-08 YYYMMDD 
     * 3 身分證編號 X(10) 09-18 靠左補空白 
     * 4 幣別 X(03) 19-21 靠左補空白 
     * 5 外幣扣帳帳號 X(13) 22-34 靠左補空白 
     * 6 本次應扣款金額 9(13) 35-47 右靠左補0
     * 7 台幣扣帳帳號 X(13) 48-60 有值才會啟動換匯(送扣款第3次(天)(含)後才會放值)
     * 8 台幣扣帳金額 9(11)V9(2) 61-73 13碼(不含小數點) 由資訊處回應
     * 9 扣帳匯率 9(6)V9(4) 74-83 由資訊處回應10碼(不含小數點) 
     * 10 合計扣外幣金額 9(11)V9(2) 84-96 13碼(不含小數點) 由資訊處回應
     * 11 帳務流水號 X(10) 97-106 P_SEQNO 
     * 12 保留欄 X(07) 107-116 靠左補空白 
     * 13 回應訊息 X(04) 117-120
     */
    class Buf1 {
        String procKind; //資料辨別碼
        String procDate; //扣款日
        String id; //身分證編號
        String currCode; //幣別
        String acctNof; //外幣扣帳帳號
        String amtf; //本次應扣款金額
        String acctNol; //台幣扣帳帳號
        String amtl; //台幣扣帳金額
        String currRate; //扣帳匯率
        String deductAmt1;//合計扣外幣金額
        String pSeqno; //帳務流水號
        String filler1; //保留欄
        String respCode; //回應訊息
        String crdCard;//雙幣卡卡號
        String filler2;//保留欄

        void clearBuf1() throws UnsupportedEncodingException {
            procKind    = "";
            procDate    = "";
            id           = "";
            currCode    = "";
            acctNof    = "";
            amtf        = "";
            acctNol    = "";
            amtl        = "";
            currRate    = "";
            deductAmt1 = "";
            pSeqno      = "";
            filler1     = "";
            respCode    = "";
            crdCard     = "";
            filler2     = "";
        }

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += zroeRight(procKind, 1);
            rtn += zroeRight(procDate, 7);
            rtn += zroeRight(id, 10);
            rtn += zroeRight(currCode, 3);
            rtn += zroeRight(acctNof, 13);
            rtn += zroeRight(amtf, 13);
            rtn += fixRight(acctNol, 13);
            rtn += zroeRight(amtl, 13);
            rtn += zroeRight(currRate, 10);
            rtn += zroeRight(deductAmt1, 13);
            rtn += zroeRight(pSeqno, 10);
            rtn += fixRight(filler1, 10);
            rtn += fixRight(respCode, 4);
            rtn += zroeRight(crdCard, 16);
            rtn += fixRight(filler2, 64);
            return rtn;
        }

    }

    String zroeRight(String str, int len) throws UnsupportedEncodingException {
        String zroe = "";
        for (int i = 0; i < 100; i++)
        	zroe += "0";
        if (str == null)
            str = "";
        str = zroe + str;
        byte[] bytes = str.getBytes("MS950");
        int offset = bytes.length - len;
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, offset, vResult, 0, len);
        return new String(vResult, "MS950");
    }
    
    String fixRight(String str, int len) throws UnsupportedEncodingException {
        String spc = "";
        for (int i = 0; i < 100; i++)
        	spc += " ";
        if (str == null)
            str = "";
        str = spc + str;
        byte[] bytes = str.getBytes("MS950");
        int offset = bytes.length - len;
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, offset, vResult, 0, len);
        return new String(vResult, "MS950");
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

}

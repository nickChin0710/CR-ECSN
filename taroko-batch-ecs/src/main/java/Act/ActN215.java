/******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112/04/03  V1.00.01    Simon     program initial                          *
 *                                   TCB 商務卡個繳戶合併為一筆以公司戶報送   *
 *  112/04/05  V1.00.02    Simon     1.關帳後二日改為關帳後一日執行           *
 *                                   2.國外預借現金額度、循環信用年利率 取自 act_jcic_log*
 *  112/04/06  V1.00.03    Simon     商務卡合計修改為 by corp_p_seqno、acct_type*
 *  112/04/07  V1.00.04    Simon     selectActAcctHst2() group by corp_p_seqno,*
 *                                   acct_type get only 1 record              *
 *  112/04/10  V1.00.05    Simon     1.fixed acno_rowid getting error         *
 *                                   2.remove hTempBilledEndBalLf,hTempBilledEndBalAf,*
 *                                   hTempBilledEndBalRi,hTempBilledEndBalPn  *
 *                                   3.fixed selectActAcctHst1()              *
 *  112/04/19  V1.00.06    Simon     1.更新商務卡 act_acno.acno_flag='2'的int_rate_mcode*
 *                                   2.selectBilContract() code error fixed   *
 *  112/04/25  V1.00.07    Simon     comc.errExit() 取代 comcr.errRtn() 顯示"關帳日後一日執行"*
 *  112/05/05  V1.00.08    Simon     act_acno.payment_rate1 update error fixed*
 *  112/05/06  V1.00.09    Simon     use crd_card.bin_type instead of ptr_bintable.bin_type *
 *  112/06/13  V1.00.10    Simon     if crd_corp.obu_id、organ_id not null, replace corp_no*
 *  112/08/29  V1.00.11    Simon     1.修正更新商務卡總戶 payment_rate1       *
 *                                   2.若上期應付帳款大於0,則上期應付帳款應大於或等於循環信用餘額*
 *  112/08/31  V1.00.12    Simon     修正[3-3]若繳款金額狀況為2未全額繳清,則上期應付帳款應大於0*
 *  112/09/04  V1.00.13    Simon     修正[3-3]若繳款金額狀況為2未全額繳清、[3-4]若繳款金額狀況為3未繳足最低、*
 *                                   [3-5]若繳款金額狀況為4全額未繳,則上期應付帳款應大於0*
 *  112/09/05  V1.00.14    Simon     新增更新商務卡總戶 debt_close_date       *
 *  112/09/15  V1.00.15    Simon     1.修正[2-1]若上期應付帳款小於或等於0,則循環信用餘額應等於0*
 *                                   2.修正[11-2]若本期相關欄位合計金額大於0，且上期未溢繳，則本期應付帳款金額應大於0*
 *                                   3.修正[11-3]若本期應付帳款等於0，則本期最低應繳金額應等於0*
 *                                   4.只針對acno_flag="2"不一樣的各項status or date 做更新，更新時新增display*
 *                                   5.tcb 報 kk4 一般結清條件由 acct_jrnl_bal<=0 更改為 acct_jrnl_bal==0*
 *                                   6.新增讀取 cca_card_acct 臨調額度        *
 *                                   7.skip 結案U無結清日期                   *
 *  112/09/16  V1.00.16    Simon     催、呆帳戶報送判斷條件餘額 >=1000更改為 >0*
 *  112/10/02  V1.00.17    Simon     催收帳戶餘額 < 1000 判斷條件更改為 == 0 才能清除債權狀態註記"A" in Chen-S 段落*
 *  112/10/22  V1.00.18    Simon     公司戶總欠小於等於0時，其底下個人帳戶各項*
 *                                   欠款餘額加總若大於0則需歸0               *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;


/*kk4產檔前篩選卡戶資料處理程式*/
public class ActN215 extends AccessDAO {

    private final String PROGNAME = 
    "kk4產檔前TCB 商務卡個繳戶合併為一筆以公司戶報送篩選卡戶資料處理程式 " 
    + "112/10/22 V1.00.18";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
	  CommString commString = new com.CommString();

    String prgmId = "ActN215";
    String hModUser = "";
    long   hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hWdayLastAcctMonth = "";
    String hWdayStmtCycle = "";
    String hChkCorpPSeqno = "";
    String hChkAcctType = "";
    String hChkPSeqno = "";
    String hChkAcnoUpdFlag = "";
    String hAcno1Rowid = "";

    String hCorpPayRate1 = "";
       int hCorpIntRateMcode = 0;
    String hCorpAcctStatus = "";
    String hCorpStatusChgDate = "";
    String hCorpDebtCloseDate = "";
    String hCorpSaleDate = "";
    String hCorpIntSign = "";
    double hCorpIntRate = 0;
    double hCorpCaculRate = 0;
    String hCorpRevolveRateSMonth = "";
    String hCorpRevolveRateEMonth = "";

    String hCorpFeeOnly = "";
    String hCompRevolveFlag = "";
       int hCompRevolveCnt = 0;
    double hMinIntRate = 0;

    String hCompPayRate1 = "";
       int hCompIntRateMcode = 0;
    String hCompAcctStatus = "";
    String hCompStatusChgDate = "";
    String hCompDebtCloseDate = "";
    String hCompSaleDate = "";
    String hCompIntSign = "";
    double hCompIntRate = 0;
    double hCompCaculRate = 0;
    String hCompRevolveRateSMonth = "";
    String hCompRevolveRateEMonth = "";

    String hAcht1PayRate1 = "";
    String hAcht1AcctStatus = "";
       int hAcht1IntRateMcode = 0;
    String hAcht1StatusChgDate = "";
    String hAcht1DebtCloseDate = "";
    String hAcht1SaleDate = "";
    double hAcht1StmtTtlAmt = 0;
    double hAcht1FeeBal = 0;
    String hAcht1RevolveIntSign = "";
    double hAcht1RevolveIntRate = 0;
    double hAcht1CaculIntRate = 0;

    String hAcht1RevolveRateSMonth = "";
    String hAcht1RevolveRateEMonth = "";

    String hAcnoPSeqno = "";
    String hAcnoAcctHolderId = "";
    String hAcnoAcctHolderIdCode = "";
    String hAcnoSaleDate = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpNo = "";
    String hAcnoCorpObuId = "";
    String hAcnoCorpOrganId = "";

    String hAcnoPaymentRate1Orig = "";
    String hAcnoPaymentRate1     = "";
       int hAcnoIntRateMcodeOrig = 0;
       int hAcnoIntRateMcode     = 0;
    String hAcnoAcctStatus = "";
    String hAcnoStatusChangeDate = "";
    String hAcnoNewCycleMonth = "";

    double hAgenRevolvingInterest1 = 0;
      
    double hAcnoRevolveIntRate = 0;
    String hAcnoRevolveRateSMonth = "";
    String hAcnoRevolveRateEMonth = "";
    double hYearRealIntRate = 0;
    double hOverseaCashadvLimit = 0;

    String hAchtStmtCycleDate = "";
    String hAchtLastMinPayDate = "";
    String hAchtLastPaymentDate = "";
    String hAchtStmtLastPayday = "";
    String hAcnoDebtCloseDate = "";
    String hTemp0EFlag = "";
    String hBusiBusinessDate = "";
    String hWdayThisAcctMonth = "";
    String hWdayLlAcctMonth = "";
    String hWdayLastCloseDate = "";
    String hWdayThisCloseDate = "";
    String hWdayLastDelaypayDate = "";
    String hAjlgBinType = "";
    double hAjlgBilledEndBalRi = 0;
    String hAjlgPaymentTimeRate = "";
    String hAjlgJcicAcctStatus = "";
    String hAjlgJcicAcctStatusFlag = "";
    String hAjlgBillTypeFlag = "";
    String hAlcpCorpNo = "";
    String hAlcpJcicRemark = "";
    int hAjlgValidCnt = 0;
    String hAjlgStopFlag = "";
    String hPbtbBinType = "";
    String hCardCardNo = "";
    String hCardOppostDate = "";
    String hCardCurrentCode = "";
    String[] hMPbtbBinType = new String[1000];
    String[] hMCardCardNo = new String[1000];
    String[] hMCardOppostDate = new String[1000];
    String[] hMCardCurrentCode = new String[1000];
    String hAjlgCorpNo = "";
    String[] hMAjlgBillTypeFlag = new String[250];
    String[] hMAjlgCorpNo = new String[250];
    String[] hMAjlgAcctType = new String[250];
    String hAjlgAcctType = "";
    String hAjlgAcctStatus = "";
    String hTempAcctType = "";
    double hAchtStmtThisTtlAmt = 0;
    double hAchtStmtMp = 0;
    double hAchtBilledEndBalBl = 0;
    double hAchtBilledEndBalIt = 0;
    double hAchtBilledEndBalId = 0;
    double hAchtBilledEndBalOt = 0;
    double hAchtBilledEndBalCa = 0;
    double hAchtBilledEndBalAo = 0;
    double hAchtBilledEndBalAf = 0;
    double hAchtBilledEndBalLf = 0;
    double hAchtBilledEndBalPf = 0;
    double hAchtBilledEndBalRi = 0;
    double hAchtBilledEndBalPn = 0;
    double hTempTtlAmtBal = 0;
    double hAchtTtlAmtBal = 0;
    double hAchtBillInterest = 0;
    double hAchtBilledEndBalTot = 0;
    double hAchtStmtAdjustAmt = 0;
    double hAchtStmtLastTtl = 0;
    double hAchtStmtPaymentAmt = 0;
    double hAchtAcctJrnlBal = 0;
    double hAjlgCashadvLimit = 0;
    double hAcnoLineOfCreditAmt = 0;
    double hAcnoComboCashLimit = 0;
    double hAjlgUnpostCardFee = 0;
    double hAjlgUnpostInstFee = 0;
    double hAjlgUnpostInstStageFee = 0;
    String hAjlgPaymentAmtRate = "";
    double hAjlgTotAmtMonth = 0;
    String hAjlgAdjEffSdate = "";
    String hAjlgAdjEffEdate = "";
    double hAjlgTempCreditAmt = 0;
    String hChgiOldId = "";
    String hChgiOldIdCode = "";
    String hCbdtAlwBadDate =   "";

    int totcnt = 0;
    int hTempCnt = 0;
    int actJcicLogCnt = 0;
    String hAaetJcicBadDebtDate = "";
    String hTempCardNo = "";
    String hTempCreateId = "";
    int tmpCount = 0;
    String hCardModSeqno = "";
    String tempStopFlag = "";
    String tmpstr = "";
    int inta1 = 0;

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

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            if (args.length > 2) {
                comcr.errRtn("Usage : ActN215 [business_date]", "", hCallBatchSeqno);
            }

            selectPtrBusinday();

            if (args.length == 1)
                hBusiBusinessDate = args[0];
            if (selectPtrWorkday() != 0) {
                exceptExit = 0;
              //comcr.errRtn(String.format("本程式為關帳日後一日執行, 本日[%s] ! ", hBusiBusinessDate), "", hCallBatchSeqno);
                comc.errExit(String.format("本程式為關帳日後一日執行, 本日[%s] ! ", 
                hBusiBusinessDate), hCallBatchSeqno);
            }

            deleteTmpPaymentRate(); //豪哥認為應該加這一段
            selectPtrActgeneraln();
            selectActAcno1();
          //insertTmpPaymentRate();
            selectActAcno2();
            showLogMessage("I", "", String.format("累計筆數 : [%d]", totcnt));

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

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

  /***********************************************************************/
  int selectPtrWorkday() throws Exception {
      hWdayStmtCycle = "";
      hWdayThisAcctMonth = "";
      hWdayLastAcctMonth = "";
      hWdayLlAcctMonth = "";
      hWdayThisCloseDate = "";
      hWdayLastCloseDate = "";
      hWdayLastDelaypayDate = "";

      sqlCmd = "select stmt_cycle,";
      sqlCmd += " this_acct_month,";
      sqlCmd += " last_acct_month,";
      sqlCmd += " ll_acct_month,";
      sqlCmd += " last_close_date,";
      sqlCmd += " this_close_date,";
      sqlCmd += " last_delaypay_date ";
      sqlCmd += " from ptr_workday  ";
      sqlCmd += "where this_close_date = to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') ";
      setString(1, hBusiBusinessDate);
      int recordCnt = selectTable();
      if (recordCnt > 0) {
          hWdayStmtCycle = getValue("stmt_cycle");
          hWdayThisAcctMonth = getValue("this_acct_month");
          hWdayLastAcctMonth = getValue("last_acct_month");
          hWdayLlAcctMonth = getValue("ll_acct_month");
          hWdayLastCloseDate = getValue("last_close_date");
          hWdayThisCloseDate = getValue("this_close_date");
          hWdayLastDelaypayDate = getValue("last_delaypay_date");
      } else
          return (1);
      return (0);
  }
  /***********************************************************************/
  private void deleteTmpPaymentRate() throws Exception {
      daoTable = "tmp_payment_rate";
      deleteTable();
  }

  /***********************************************************************/
  int selectPtrActgeneraln() throws Exception {
      sqlCmd = "select max(revolving_interest1) h_agen_revolving_interest1 ";
      sqlCmd += " from ptr_actgeneral_n ";
      int recordCnt = selectTable();
      if (notFound.equals("Y")) {
          comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
      }
      if (recordCnt > 0) {
          hAgenRevolvingInterest1 = getValueDouble("h_agen_revolving_interest1");
      }

      return (0);
  }

  /***********************************************************************/
	void selectActAcno1() throws Exception {

    extendField = "acno1.";
    sqlCmd  = " select ";
 		sqlCmd += " rowid as acno_rowid, ";
    sqlCmd += " corp_p_seqno, ";
    sqlCmd += " acct_type, ";
    sqlCmd += " acct_status, ";
    sqlCmd += " payment_rate1, ";
    sqlCmd += " int_rate_mcode, ";
    sqlCmd += " status_change_date, ";
    sqlCmd += " debt_close_date, ";
    sqlCmd += " sale_date, ";
    sqlCmd += " revolve_int_sign, revolve_int_rate,";
    sqlCmd += " decode(revolve_int_sign,'+',revolve_int_rate,"
            + " revolve_int_rate*-1) calcu_revolve_int_rate,";
    sqlCmd += " revolve_rate_s_month,";
    sqlCmd += " decode(revolve_rate_e_month,'','999912',revolve_rate_e_month) "
            + " as revolve_rate_e_month,";
    sqlCmd += " p_seqno ";
    sqlCmd += " from act_acno ";
    sqlCmd += " where corp_p_seqno <> '' ";
    sqlCmd += "   and acno_flag  = '2' ";
    sqlCmd += "   and stmt_cycle = ? ";
    sqlCmd += " order by corp_p_seqno, acct_type ";
    setString(1, hWdayStmtCycle);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      hChkCorpPSeqno = getValue("acno1.corp_p_seqno", i);
      hChkAcctType = getValue("acno1.acct_type", i);
      hChkPSeqno = getValue("acno1.p_seqno", i);
      hCorpPayRate1 = getValue("acno1.payment_rate1", i);
      hCorpIntRateMcode = getValueInt("acno1.int_rate_mcode", i);
      hCorpAcctStatus = getValue("acno1.acct_status", i);
      hCorpStatusChgDate = getValue("acno1.status_change_date", i);
      hCorpDebtCloseDate = getValue("acno1.debt_close_date", i);
      hCorpSaleDate = getValue("acno1.sale_date", i);
      hCorpRevolveRateSMonth = getValue("acno1.revolve_rate_s_month", i);
      hCorpRevolveRateEMonth = getValue("acno1.revolve_rate_e_month", i);
      hCorpIntSign = getValue("acno1.revolve_int_sign", i);
      hCorpIntRate = getValueDouble("acno1.revolve_int_rate", i);
      hCorpCaculRate = getValueDouble("acno1.calcu_revolve_int_rate", i);
      hAcno1Rowid = getValue("acno1.acno_rowid", i);

      hCorpFeeOnly  = "";

      hCompPayRate1 = "";
      hCompIntRateMcode = 0;
      hCompAcctStatus = "";
      hCompStatusChgDate = "";
      hCompDebtCloseDate = "";
      hCompSaleDate = "";
      hCompRevolveFlag = "";
      hCompRevolveCnt = 0;
      hCompRevolveRateSMonth = "";
      hCompRevolveRateEMonth = "";
      hCompIntSign = "";
      hCompIntRate = 0;
      hCompCaculRate = hMinIntRate = hAgenRevolvingInterest1 * -1; 

      hChkAcnoUpdFlag = "";

      int recCnt = selectActAcctHst1();
      if (recCnt <= 0) {
        continue;
      }

      if (!hCompRevolveFlag.equals("Y")) {
        hCompCaculRate = hCorpCaculRate; 
        hCompRevolveRateSMonth = hCorpRevolveRateSMonth;    	
        hCompRevolveRateEMonth = hCorpRevolveRateEMonth;
        hCompIntSign = hCorpIntSign;
        hCompIntRate = hCorpIntRate;
      } 

      if (!hCompPayRate1.equals(hCorpPayRate1) || 
          (hCompIntRateMcode != hCorpIntRateMcode) || 
          !hCompAcctStatus.equals(hCorpAcctStatus) || 
          !hCompStatusChgDate.equals(hCorpStatusChgDate) || 
          !hCompDebtCloseDate.equals(hCorpDebtCloseDate) || 
          !hCompSaleDate.equals(hCorpSaleDate) || 
          (hCompCaculRate != hCorpCaculRate) ) 
      {
        hChkAcnoUpdFlag = "Y";
      }

      if (!hCompPayRate1.equals(hCorpPayRate1)) {
        showLogMessage("I", "", "==1 act_acno.payment_rate1 changed => "+
        "p_seqno, old, new : "+hChkPSeqno+", "+hCorpPayRate1+", "+
			  hCompPayRate1+" ===");
      }

      if (hCompIntRateMcode != hCorpIntRateMcode) {
        showLogMessage("I", "", "==1 act_acno.int_rate_mcode changed => "+
        "p_seqno, old, new : "+hChkPSeqno+", "+hCorpIntRateMcode+", "+
			  hCompIntRateMcode+" ===");
      }

      if (!hCompAcctStatus.equals(hCorpAcctStatus)) {
        showLogMessage("I", "", "=== act_acno.acct_status changed => "+
        "p_seqno, old, new : "+hChkPSeqno+", "+hCorpAcctStatus+", "+
			  hCompAcctStatus+" ===");
      }

      if (!hCompStatusChgDate.equals(hCorpStatusChgDate)) {
        showLogMessage("I", "", "=== act_acno.status_change_date changed => "+
        "p_seqno, old, new : "+hChkPSeqno+", "+hCorpStatusChgDate+", "+
			  hCompStatusChgDate+" ===");
      }

      if (!hCompDebtCloseDate.equals(hCorpDebtCloseDate)) {
        showLogMessage("I", "", "=== act_acno.debt_close_date changed => "+
        "p_seqno, old, new : "+hChkPSeqno+", "+hCorpDebtCloseDate+", "+
			  hCompDebtCloseDate+" ===");
      }

      if (!hCompSaleDate.equals(hCorpSaleDate)) {
        showLogMessage("I", "", "=== act_acno.sale_date changed => "+
        "p_seqno, old, new : "+hChkPSeqno+", "+hCorpSaleDate+", "+
			  hCompSaleDate+" ===");
      }

      if (hCompCaculRate != hCorpCaculRate) {
        showLogMessage("I", "", "=== act_acno.revolve_int_rate changed => "+
        "p_seqno, old, new : "+hChkPSeqno+", "+hCorpCaculRate+", "+
			  hCompCaculRate+" ===");
      }

      if (hChkAcnoUpdFlag.equals("Y")) {
        updateActAcno1();
      }

 	    if (hCorpFeeOnly.equals("Y")) {
		    insertTmpPaymentRate();
		  } 

    }
	}

  /***********************************************************************/
	int selectActAcctHst1() throws Exception {

    extendField = "acht1.";
		sqlCmd  = " select ";
 		sqlCmd += " a.corp_p_seqno, a.p_seqno,  ";
 		sqlCmd += " a.acct_status, ";
 		sqlCmd += " a.payment_rate1, a.int_rate_mcode, ";
 	//sqlCmd += " decode(a.acct_status,'4',a.status_change_date,'') status_change_date, ";
 		sqlCmd += " status_change_date, ";
 		sqlCmd += " a.debt_close_date,  ";
 		sqlCmd += " a.sale_date,  ";
    sqlCmd += " a.revolve_int_sign, a.revolve_int_rate,";
    sqlCmd += " decode(a.revolve_int_sign,'+',a.revolve_int_rate,"
            + " a.revolve_int_rate*-1) h_acno_revolve_int_rate,";
    sqlCmd += " a.revolve_rate_s_month,";
    sqlCmd += " decode(a.revolve_rate_e_month,'','999912',a.revolve_rate_e_month) "
            + " as revolve_rate_e_month,";
 		sqlCmd += " b.stmt_this_ttl_amt b_stmt_this_ttl_amt, "; 
 		sqlCmd += " (b.unbill_end_bal_lf + b.unbill_end_bal_af + b.billed_end_bal_lf + "
 		        + " b.billed_end_bal_af) b_fee_bal "; 
    sqlCmd += "  from act_acno a ";
    sqlCmd += " left outer join act_acct_hst b on b.p_seqno = a.p_seqno ";
		sqlCmd += "             and b.acct_month = ? ";
		sqlCmd += " where 1=1 ";
		sqlCmd += "   and a.acno_flag in ('3') ";
	//sqlCmd += "   and a.p_seqno = b.p_seqno ";
		sqlCmd += "   and a.corp_p_seqno = ?  ";
		sqlCmd += "   and a.acct_type = ?  ";
	//sqlCmd += "   and b.acct_month = ?  ";

    setString(1, hWdayLlAcctMonth);
    setString(2, hChkCorpPSeqno);
    setString(3, hChkAcctType);
    int recordCnt = selectTable();
    for (int ii = 0; ii < recordCnt; ii++) {

      hAcht1AcctStatus  = getValue("acht1.acct_status",ii);
      hAcht1PayRate1  = getValue("acht1.payment_rate1",ii);
      hAcht1IntRateMcode  = getValueInt("acht1.int_rate_mcode",ii);
      hAcht1StatusChgDate  = getValue("acht1.status_change_date",ii);
      hAcht1DebtCloseDate  = getValue("acht1.debt_close_date",ii);
      hAcht1SaleDate  = getValue("acht1.sale_date",ii);
      hAcht1StmtTtlAmt = getValueDouble("acht1.b_stmt_this_ttl_amt",ii);
      hAcht1FeeBal    = getValueDouble("acht1.b_fee_bal",ii);
      hAcht1FeeBal = convAmtDp0r(hAcht1FeeBal);
      hAcht1RevolveIntSign = getValue("acht1.revolve_int_sign",ii);
      hAcht1RevolveIntRate = getValueDouble("acht1.revolve_int_rate",ii);
      hAcht1CaculIntRate = getValueDouble("acht1.h_acno_revolve_int_rate",ii);
      hAcht1RevolveRateSMonth = getValue("acht1.revolve_rate_s_month",ii);
      hAcht1RevolveRateEMonth = getValue("acht1.revolve_rate_e_month",ii);

	  	if (isNumeric(hCompPayRate1))  
	  	{
	  	  if ((isNumeric(hAcht1PayRate1)) && 
            (hAcht1PayRate1.compareTo(hCompPayRate1) > 0))
    		{
	  		  hCompPayRate1 = hAcht1PayRate1;
	  	  }  
	  	} else if (isNumeric(hAcht1PayRate1))  
	  	{
	  		hCompPayRate1 = hAcht1PayRate1;
	  	} else if (commString.pos(",0A,0B,0C,0D", hAcht1PayRate1)>0)  
	  	{
        if (hAcht1PayRate1.compareTo(hCompPayRate1) > 0)
    		{
	  		  hCompPayRate1 = hAcht1PayRate1;
	  	  } else if (hCompPayRate1.equals("0E"))
    		{
	  		  hCompPayRate1 = hAcht1PayRate1;
	  	  } 
	  	} else if (hAcht1PayRate1.equals("0E"))  
	  	{
        if (commString.pos(",0A,0B,0C,0D", hCompPayRate1)>0)
    		{
	  		  //hCompPayRate1 不動
	  	  } else 
    		{
	  		  hCompPayRate1 = "0E";
	  	  } 
	  	} else 
	  	{
        if (!hAcht1PayRate1.equals(""))
    		{
	  		  hCompPayRate1 = hAcht1PayRate1;
	  	  } 
	  	} 

      if (hAcht1AcctStatus.compareTo(hCompAcctStatus) > 0) {
	  		hCompAcctStatus = hAcht1AcctStatus;
      }

      if (hAcht1StatusChgDate.compareTo(hCompStatusChgDate) > 0) {
	  		hCompStatusChgDate = hAcht1StatusChgDate;
      }

      if (hAcht1DebtCloseDate.compareTo(hCompDebtCloseDate) > 0) {
	  		hCompDebtCloseDate = hAcht1DebtCloseDate;
      }

      if (hAcht1IntRateMcode > hCompIntRateMcode) {
	  		hCompIntRateMcode = hAcht1IntRateMcode;
      }

      if (hAcht1SaleDate.compareTo(hCompSaleDate) > 0) {
	  		hCompSaleDate = hAcht1SaleDate;
      }

      if ((hWdayThisAcctMonth.compareTo(hAcht1RevolveRateSMonth)>=0)&&
          (hWdayThisAcctMonth.compareTo(hAcht1RevolveRateEMonth)<=0)) {
      //if (hAcht1CaculIntRate < hMinIntRate) {
      //  hAcht1CaculIntRate = hMinIntRate; 
      //} 
        hCompRevolveCnt++;
        hCompRevolveFlag = "Y"; 
      //取利率最大者
        if (hCompRevolveCnt == 1 ) {
          hCompCaculRate = hAcht1CaculIntRate; 
          hCompRevolveRateSMonth = hAcht1RevolveRateSMonth;    	
          hCompRevolveRateEMonth = hAcht1RevolveRateEMonth;
          hCompIntSign = hAcht1RevolveIntSign;
          hCompIntRate = hAcht1RevolveIntRate;
        } else if ((hAcht1CaculIntRate > hCompCaculRate) ) {
          hCompCaculRate = hAcht1CaculIntRate; 
          hCompRevolveRateSMonth = hAcht1RevolveRateSMonth;    	
          hCompRevolveRateEMonth = hAcht1RevolveRateEMonth;
          hCompIntSign = hAcht1RevolveIntSign;
          hCompIntRate = hAcht1RevolveIntRate;
        } 
      }

//以下判斷 feeOnly
	    if (hAcht1StmtTtlAmt <= 0) {
		    continue;
		  } 

	    if ((hAcht1StmtTtlAmt == hAcht1FeeBal) && 
	        (!comc.getSubString(hAcht1PayRate1, 1,2).equals("A")) &&
	        (!hCorpFeeOnly.equals("N"))) {
		    hCorpFeeOnly = "Y";
		  } else {
		  	hCorpFeeOnly = "N";
		  }

		}
		return recordCnt;
	}
	
  /***********************************************************************/
  public static boolean isNumeric(String str) { 
    if (str == null) {
        return false;
    }
    try {  
      Double.parseDouble(str);  
      return true;
    } catch(NumberFormatException e){  
      return false;  
    }  
  }

  /***********************************************************************/
  public double  convAmtDp0r(double cvtAmt) throws Exception
  {
    long   cvtLong   = (long) Math.round(cvtAmt + 0.000001);
    double cvtDouble =  ((double) cvtLong);
    return cvtDouble;
  }

  /***********************************************************************/
  public double  convAmtDp2r(double cvtAmt) throws Exception
  {
    long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
    double cvtDouble =  ((double) cvtLong) / 100;
    return cvtDouble;
  }

  //************************************************************************
  int updateActAcno1() throws Exception
  {
    dateTime();

    updateSQL = "payment_rate1      = ?,"
              + "acct_status        = ?, "
              + "int_rate_mcode     = ?, "
              + "status_change_date = ?, "
              + "debt_close_date    = ?, "
              + "sale_date          = ?, "
              + "revolve_int_sign   = ?, "
              + "revolve_int_rate   = ?, "
              + "revolve_rate_s_month = ?, "
              + "revolve_rate_e_month = ?, "
              + "mod_pgm            = ?, "
              + "mod_time           = sysdate";  
    daoTable  = "act_acno";
    whereStr  = "WHERE  rowid = ? ";

    setString(1 , hCompPayRate1);
    setString(2 , hCompAcctStatus);
    setInt(3 , hCompIntRateMcode);
    setString(4 , hCompStatusChgDate);
    setString(5 , hCompDebtCloseDate);
    setString(6 , hCompSaleDate);
    setString(7 , hCompIntSign);
    setDouble(8 , hCompIntRate);
    setString(9 , hCompRevolveRateSMonth);
    setString(10 , hCompRevolveRateEMonth);
    setString(11 , javaProgram);
    setRowId(12 , hAcno1Rowid);

    updateTable();

    return(0);
 }

  //************************************************************************
  int updateActAcno2() throws Exception
  {
    dateTime();

    updateSQL = "payment_rate1      = ?,"
              + "int_rate_mcode     = ?, "
              + "mod_pgm            = ?, "
              + "mod_time           = sysdate";  
    daoTable  = "act_acno";
    whereStr  = "WHERE  p_seqno = ? ";

    setString(1 , hAcnoPaymentRate1);
    setInt(2 , hAcnoIntRateMcode);
    setString(3 , javaProgram);
    setString(4 , hAcnoPSeqno);

    updateTable();

    return(0);
 }

  /***********************************************************************/
	void insertTmpPaymentRate() throws Exception {

		daoTable = "tmp_payment_rate";
		setValue("p_seqno", hChkPSeqno);

		try {
    	insertTable();
    	//if (dupRecord.equals("Y")) {
    	//	return;
    	//}非定義unique key
    } catch (Exception ex) {
			return;
		}

	}

  /***********************************************************************/
	void selectActAcno2() throws Exception {

    int badFlag = 0;

    extendField = "acno2.";
    sqlCmd  = " select ";
    sqlCmd += " c.corp_no, a.acct_type, ";
    sqlCmd += " c.obu_id, c.organ_id, ";
    sqlCmd += " a.corp_p_seqno, a.p_seqno, ";
 		sqlCmd += " a.acct_status, ";
 		sqlCmd += " a.payment_rate1, a.int_rate_mcode, ";
    sqlCmd += " decode(a.acct_status,'4',a.status_change_date,'') status_change_date,";
 		sqlCmd += " a.sale_date, ";
    sqlCmd += " decode(a.revolve_int_sign,'+',a.revolve_int_rate,"
            + " a.revolve_int_rate*-1) h_acno_revolve_int_rate,";
    sqlCmd += " a.revolve_rate_s_month,";
    sqlCmd += " decode(a.revolve_rate_e_month,'','999912',a.revolve_rate_e_month) "
            + " as revolve_rate_e_month,";
    sqlCmd += " decode(t.p_seqno, null,'N','Y') h_temp_0e_flag, ";
    sqlCmd += " a.line_of_credit_amt, a.line_of_credit_amt_cash ";
    sqlCmd += " from crd_corp c, act_acno a ";
    sqlCmd += " left outer join tmp_payment_rate t on t.p_seqno = a.p_seqno ";
    sqlCmd += " where a.corp_p_seqno <> '' ";
    sqlCmd += "   and a.corp_p_seqno  = c.corp_p_seqno ";
    sqlCmd += "   and a.acno_flag  = '2' ";
    sqlCmd += "   and a.stmt_cycle = ? ";
    sqlCmd += " order by a.corp_p_seqno, a.acct_type ";
    setString(1, hWdayStmtCycle);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      hAcnoCorpNo = getValue("acno2.corp_no",i);
      hAcnoCorpObuId = getValue("acno2.obu_id",i);
      hAcnoCorpOrganId = getValue("acno2.organ_id",i);
      if (!hAcnoCorpObuId.equals("")) {
        hAcnoCorpNo = hAcnoCorpObuId;
      } else if (!hAcnoCorpObuId.equals("")) {
        hAcnoCorpNo = hAcnoCorpOrganId;
      }

      hAcnoAcctType = getValue("acno2.acct_type",i);
      hAcnoCorpPSeqno = getValue("acno2.corp_p_seqno",i);
      hAcnoPSeqno = getValue("acno2.p_seqno",i);

      hAcnoPaymentRate1 = getValue("acno2.payment_rate1",i);
      hAcnoIntRateMcode = getValueInt("acno2.int_rate_mcode",i);
      hAcnoAcctStatus = getValue("acno2.acct_status",i);
      hAcnoSaleDate = getValue("acno2.sale_date",i);
      hAcnoStatusChangeDate = getValue("acno2.status_change_date",i);

      hAcnoLineOfCreditAmt = getValueDouble("acno2.line_of_credit_amt",i);
      hAjlgCashadvLimit = getValueDouble("acno2.line_of_credit_amt_cash",i);
      hTemp0EFlag = getValue("acno2.h_temp_0e_flag",i);
      hAcnoRevolveIntRate = getValueDouble("acno2.h_acno_revolve_int_rate",i);
      hAcnoRevolveRateSMonth = getValue("acno2.revolve_rate_s_month",i);
      hAcnoRevolveRateEMonth = getValue("acno2.revolve_rate_e_month",i);

      hAcnoPaymentRate1Orig = getValue("acno2.payment_rate1",i);
      hAcnoIntRateMcodeOrig = getValueInt("acno2.int_rate_mcode",i);

      selectActAcctHst2();
      
      hAlcpCorpNo = "";
      hAlcpJcicRemark = "";
      hAjlgBinType = "         ";
      hAjlgJcicAcctStatusFlag = "";
      hAjlgJcicAcctStatus = "";

      selectCrdCard();
      if (hAjlgBinType.substring(0, 9).equals("         "))
        continue;

      if ((hAcnoAcctStatus.equals("3")) || (hAcnoAcctStatus.equals("4"))) {
          hAaetJcicBadDebtDate = "";
          badFlag = selectActJcicLog2();
        //if (hTempTtlAmtBal >= 1000) {
          if (hTempTtlAmtBal >  0)    {
              if (hAcnoAcctStatus.equals("3"))
                  hAjlgJcicAcctStatus = "A";
              if (hAcnoAcctStatus.equals("4"))
                  hAjlgJcicAcctStatus = "B";
              if (badFlag == 0) {
                  if (!hAcnoAcctStatus.equals(hAjlgAcctStatus))
                      hAaetJcicBadDebtDate = hBusiBusinessDate;
              } else {
                  hAaetJcicBadDebtDate = hBusiBusinessDate;
                  if (insertActAcnoExt() != 0)
                      updateActAcnoExt();
              }
          } else {
              if (badFlag == 0) {
                  if (hAcnoAcctStatus.equals("3"))
                      hAjlgJcicAcctStatus = "A";
                  if (hAcnoAcctStatus.equals("4"))
                      hAjlgJcicAcctStatus = "B";
                  if (!hAcnoAcctStatus.equals(hAjlgAcctStatus))
                      hAaetJcicBadDebtDate = hBusiBusinessDate;
              }
          }
      }

      /*fix 20210812 temp_stop_flag應該是not euqals  */
      if ((hAcnoSaleDate.length() != 0)
           || ((hAjlgJcicAcctStatus.length() != 0) && (hAchtTtlAmtBal == 0))
         //|| ((hAjlgValidCnt == 0) && (hAchtAcctJrnlBal <= 0) && 
           || ((hAjlgValidCnt == 0) && (hAchtAcctJrnlBal == 0) && 
              (!tempStopFlag.equals("N")))) {
          if (selectActJcicEnd() == 0)
              continue;

          if (hAcnoSaleDate.length() != 0) {
              hAjlgJcicAcctStatusFlag = "T";
          } else {
              hAjlgJcicAcctStatusFlag = "Y";
              if (hAjlgJcicAcctStatus.equals("A"))
                  hAjlgJcicAcctStatusFlag = "Y";
              if (hAjlgStopFlag.equals("3"))
                  hAjlgJcicAcctStatusFlag = "U";
              if (hAjlgJcicAcctStatus.equals("B"))
                  hAjlgJcicAcctStatusFlag = "U";
          }
      }

      double tMinIntRate = 0;
      if ((hWdayThisAcctMonth.compareTo(hAcnoRevolveRateSMonth)>=0)&&
          (hWdayThisAcctMonth.compareTo(hAcnoRevolveRateEMonth)<= 0))
      {  tMinIntRate = hAcnoRevolveIntRate; }
      hYearRealIntRate = (hAgenRevolvingInterest1 + tMinIntRate) * 365 / 100;
      hYearRealIntRate = convAmtDp2r(hYearRealIntRate);

      computeOverseaCashadvLimit();

      if (hAjlgJcicAcctStatusFlag.equals("U") && hAchtLastPaymentDate.length() == 0) {
         showLogMessage("I", "", String.format("*** skip 結案U無結清日期 sPseqno:[%s], jcicAcctSt:[%s], jcicAcctStFlag:[%s] ",
           hAcnoPSeqno, hAjlgJcicAcctStatus, hAjlgJcicAcctStatusFlag));
         continue;
      }

      /* *chun-yang modify* */
      if (((hAjlgJcicAcctStatus.length() == 0) && 
           (hAcnoStatusChangeDate.length() != 0))
        //|| ((hAjlgJcicAcctStatus.equals("A")) && (hTempTtlAmtBal < 1000)
          || ((hAjlgJcicAcctStatus.equals("A")) && (hTempTtlAmtBal == 0)
              && (hAjlgJcicAcctStatusFlag.equals("Y") == false))) {
          showLogMessage("I", "", String.format("Chen-S pSeqno[%s] jcicStatus[%s] stChgDate[%s] change to null ",
                hAcnoPSeqno, hAjlgJcicAcctStatus, hAcnoStatusChangeDate));
          hAcnoStatusChangeDate = "";
          hAjlgJcicAcctStatus = "";
      }

      if (hAchtStmtPaymentAmt > 0) {
          hAchtStmtAdjustAmt = hAchtStmtAdjustAmt + hAchtStmtPaymentAmt;
          hAchtStmtPaymentAmt = 0;
      }
      if (hAchtTtlAmtBal < 0)
          hAchtTtlAmtBal = 0;
      if (hAcnoComboCashLimit < 0)
          hAcnoComboCashLimit = 0;

      hAjlgUnpostInstFee = 0;
      hAjlgUnpostCardFee = 0;
      hAjlgUnpostInstStageFee = 0; /* 增設：對帳單分期之分期總額 */

      /* 餘額只欠年費,掛失費 評等 '0E' */
      if (hTemp0EFlag.equals("Y"))
          hAcnoPaymentRate1 = "0E";

      if ((hAchtStmtLastTtl + hAchtStmtAdjustAmt) == 0)
          hAcnoPaymentRate1 = "0E";

      if ((hAcnoPaymentRate1.equals("0B"))
              && ((hAchtLastMinPayDate.compareTo(hWdayLastCloseDate) >= 0)
              && (hAchtLastMinPayDate.compareTo(hWdayLastDelaypayDate) <= 0))
              && ((hAchtLastPaymentDate.compareTo(hWdayThisCloseDate) <= 0)
              && (hAchtLastPaymentDate.compareTo(hWdayLastDelaypayDate) >= 0)))
          hAcnoPaymentRate1 = "0A";

      if ((hAchtStmtThisTtlAmt <= 0) && (hAchtStmtAdjustAmt < 0)
              && (hAchtStmtAdjustAmt + hAchtStmtLastTtl <= 0))
          hAcnoPaymentRate1 = "0E";

/***修正
27 循環信用餘額    :[00000262237] ERROR !!! 
    MSG: [2-1]若上期應付帳款小於或等於0,則循環信用餘額應等於0
         [3-1]若繳款金額狀況為X不須繳款,則循環信用餘額應小於或等於30000
14 本期應付帳款金額:[-0000000405] WARNING !!! 
    MSG: [11-2]若本期相關欄位合計金額大於0，且上期未溢繳，則本期應付帳款金額應
         大於0
***/
    //if (hAcnoPaymentRate1.equals("0E"))
    //  hAchtStmtLastTtl = 0;


      if (hAchtTtlAmtBal > hAchtStmtLastTtl) {
        if (hAchtStmtLastTtl > 0) {
          hAchtTtlAmtBal = hAchtStmtLastTtl;
        } else {
          hAchtTtlAmtBal = 0;
        }
      }

      hAchtBilledEndBalTot = 
      hAchtBilledEndBalBl  + hAchtBilledEndBalIt + hAchtBilledEndBalId +
      hAchtBilledEndBalOt  + hAchtBilledEndBalCa + hAchtBilledEndBalAo +
      hAchtBilledEndBalAf  + hAchtBilledEndBalLf + hAchtBilledEndBalPf + 
      hAchtBilledEndBalPn  + hAchtBillInterest;
      if (hAchtStmtThisTtlAmt <= 0) {
        if (hAchtBilledEndBalTot > 0) {
          showLogMessage("I", "", "=== hAchtStmtThisTtlAmt <= 0, but " +
          "hAchtBilledEndBalTot > 0 => p_seqno, hAchtStmtThisTtlAmt, " + 
          "hAchtBilledEndBalTot : "+hAcnoPSeqno+", "+hAchtStmtThisTtlAmt+", "+
		      hAchtBilledEndBalTot+" ===");
          hAchtBilledEndBalBl = 0;
          hAchtBilledEndBalIt = 0;
          hAchtBilledEndBalId = 0;
          hAchtBilledEndBalOt = 0;
          hAchtBilledEndBalCa = 0;
          hAchtBilledEndBalAo = 0;
          hAchtBilledEndBalAf = 0;
          hAchtBilledEndBalLf = 0;
          hAchtBilledEndBalPf = 0;
          hAchtBilledEndBalPn = 0;
          hAchtBillInterest = 0;
        } 
      }

/***修正
15 本期最低應繳金額:[00000000029] WARNING !!! 
    MSG: [11-3]若本期應付帳款等於0，則本期最低應繳金額應等於0
***/
      if ((hAchtStmtThisTtlAmt <= 0) && (hAchtStmtMp > 0))
          hAchtStmtMp = 0;

/***      
修正
[3-3]若繳款金額狀況為2未全額繳清,則上期應付帳款應大於0
***/
      if (hTempTtlAmtBal <= 0 && hAchtStmtLastTtl > 0 &&
		      (",0A,0B".indexOf(hAcnoPaymentRate1) < 0)) {
         hAcnoPaymentRate1 = "0A";
         hAcnoIntRateMcode = 0;
         showLogMessage("I", "", "==2 act_acno.payment_rate1 changed => "+
         "p_seqno, old, new : "+hAcnoPSeqno+", "+hAcnoPaymentRate1Orig+", "+
		     "0A"+" ===");
         if (hAcnoIntRateMcodeOrig != 0) {
           showLogMessage("I", "", "==2 act_acno.int_rate_mcode changed => "+
           "p_seqno, old, new : "+hAcnoPSeqno+", "+hAcnoIntRateMcodeOrig+", "+
		       "0"+" ===");
         }
         updateActAcno2();
      }
/***      
修正
[3-3]若繳款金額狀況為2未全額繳清、[3-4]若繳款金額狀況為3未繳足最低、
[3-5]若繳款金額狀況為4全額未繳,則上期應付帳款應大於0
***/
      if (hAchtStmtLastTtl <= 0 && !hAcnoPaymentRate1.equals("0A") &&
          !hAcnoPaymentRate1.equals("0E")) {
         hAcnoPaymentRate1 = "0E";
         hAcnoIntRateMcode = 0;
         showLogMessage("I", "", "==3 act_acno.payment_rate1 changed => "+
         "p_seqno, old, new : "+hAcnoPSeqno+", "+hAcnoPaymentRate1Orig+", "+
		     "0E"+" ===");
         if (hAcnoIntRateMcodeOrig != 0) {
          showLogMessage("I", "", "==3 act_acno.int_rate_mcode changed => "+
          "p_seqno, old, new : "+hAcnoPSeqno+", "+hAcnoIntRateMcodeOrig+", "+
		      "0"+" ===");
         }
         updateActAcno2();
      }

      if (hTempTtlAmtBal < 0)
          hTempTtlAmtBal = 0;
      if (hTempTtlAmtBal > hAchtTtlAmtBal)
          hTempTtlAmtBal = hAchtTtlAmtBal;

      /* 評等 '0A', 餘額改 0 */
      if (hAcnoPaymentRate1.equals("0A"))
          hTempTtlAmtBal = 0;

      if (hAcnoPaymentRate1.equals("0A")) {
          hAjlgPaymentAmtRate = "1";
          hAjlgPaymentTimeRate = "N";
      } else if (hAcnoPaymentRate1.equals("0B")) {
          hAjlgPaymentAmtRate = "1";
          hAjlgPaymentTimeRate = "0";
      } else if (hAcnoPaymentRate1.equals("0C")) {
          hAjlgPaymentAmtRate = "2";
          hAjlgPaymentTimeRate = "N";
      } else if (hAcnoPaymentRate1.equals("0D")) {
          hAjlgPaymentAmtRate = "2";
          hAjlgPaymentTimeRate = "0";
      } else if (hAcnoPaymentRate1.equals("0E")) {
          hAjlgPaymentAmtRate = "X";
          hAjlgPaymentTimeRate = "X";
      } else if ((hAcnoPaymentRate1.compareTo("01") >= 0) && (hAcnoPaymentRate1.compareTo("06") <= 0)) {
          if (hAchtStmtPaymentAmt == 0) {
              hAjlgPaymentAmtRate = "4";
          } else {
              hAjlgPaymentAmtRate = "3";
          }
          tmpstr = String.format("%1.1s", hAcnoPaymentRate1.substring(1));
          hAjlgPaymentTimeRate = tmpstr;
      } else {
          if (hAchtStmtPaymentAmt == 0) {
              hAjlgPaymentAmtRate = "4";
          } else {
              hAjlgPaymentAmtRate = "3";
          }
          hAjlgPaymentTimeRate = "7";
      }

      if ( (hAjlgValidCnt != 0) && (hAchtStmtThisTtlAmt == 0) &&
           (hAjlgJcicAcctStatusFlag.length() == 0) && (hAcnoLineOfCreditAmt == 0))
         continue;

      selectCcaCardAcct();

      selectBilContract();

      selectActJcicLog1();
      for (inta1 = 0; inta1 < actJcicLogCnt; inta1++) {
          if (hAcnoAcctType.equals(hMAjlgAcctType[inta1])) {
              hAjlgBillTypeFlag = hMAjlgBillTypeFlag[inta1];
              break;
          }
      }

      if (inta1 >= actJcicLogCnt) {
          if (actJcicLogCnt > 0)
              tmpstr = String.format("%02d",
                      comcr.str2int(hMAjlgBillTypeFlag[0]) + 1);
          else
              tmpstr = String.format("03");
          hAjlgBillTypeFlag = tmpstr;
          /*** 新的帳單別註記 加報KK2以維持kk4帳單別註記一致 ***/
          insertAllCardByType();
      }

      if (hAcnoSaleDate.length() != 0)
        selectActNplCorp();

      insertActJcicLog();
      if ((hAjlgJcicAcctStatusFlag.equals("Y")) || 
          (hAjlgJcicAcctStatusFlag.equals("U")) || 
          (hAjlgJcicAcctStatusFlag.equals("T"))) {
         if (insertActJcicEnd() != 0)
             updateActJcicEnd();
      }
      totcnt++;


    }//end for()
	}

  /***********************************************************************/
  void selectActAcctHst2() throws Exception {

    extendField = "acht2.";
		sqlCmd  = " select ";
 		sqlCmd += " a.corp_p_seqno, a.acct_type, "; 
 		sqlCmd += " max(h.last_min_pay_date) max_last_min_pay_date, "; 
 		sqlCmd += " max(h.last_payment_date) max_last_payment_date, "; 
 		sqlCmd += " max(h.stmt_last_payday) max_stmt_last_payday, "; 
 		sqlCmd += " max(h.stmt_cycle_date) max_stmt_cycle_date, "; 
 		sqlCmd += " sum(h.stmt_this_ttl_amt) h_acht_stmt_this_ttl_amt, "; 
 		sqlCmd += " sum(h.stmt_mp) h_acht_stmt_mp, "; 
 		sqlCmd += " sum(h.unbill_end_bal_bl) h_acht_billed_end_bal_bl, "; 
 		sqlCmd += " sum(h.unbill_end_bal_it) h_acht_billed_end_bal_it, "; 
 		sqlCmd += " sum(h.unbill_end_bal_id) h_acht_billed_end_bal_id, "; 
 		sqlCmd += " sum(h.unbill_end_bal_ot) h_acht_billed_end_bal_ot, "; 
 		sqlCmd += " sum(h.unbill_end_bal_ca) h_acht_billed_end_bal_ca, "; 
 		sqlCmd += " sum(h.unbill_end_bal_ao) h_acht_billed_end_bal_ao, "; 
 		sqlCmd += " sum(h.unbill_end_bal_af) h_acht_billed_end_bal_af, "; 
 		sqlCmd += " sum(h.unbill_end_bal_lf) h_acht_billed_end_bal_lf, "; 
 		sqlCmd += " sum(h.unbill_end_bal_pf) h_acht_billed_end_bal_pf, "; 
 		sqlCmd += " sum(h.unbill_end_bal_ri) h_acht_billed_end_bal_ri, "; 
 		sqlCmd += " sum(h.billed_end_bal_ca + h.billed_end_bal_id + h.billed_end_bal_it + "
 		        + " h.billed_end_bal_bl + h.billed_end_bal_db + h.billed_end_bal_cb + " 
 		        + " h.billed_end_bal_ot + h.billed_end_bal_ao)  " 
 		        + " h_temp_ttl_amt_bal, "; 
 		sqlCmd += " sum(h.ttl_amt_bal) h_acht_ttl_amt_bal, "; 
 		sqlCmd += " sum(h.unbill_end_bal_ri + h.unbill_end_bal_ci + h.unbill_end_bal_ai) "
 		        + " h_acht_bill_interest, "; 
 		sqlCmd += " sum(h.stmt_adjust_amt) h_acht_stmt_adjust_amt, "; 
 		sqlCmd += " sum(h.stmt_last_ttl) h_acht_stmt_last_ttl, "; 
 		sqlCmd += " sum(h.stmt_payment_amt) h_acht_stmt_payment_amt, "; 
 		sqlCmd += " sum(h.acct_jrnl_bal) h_acht_acct_jrnl_bal "; 
	  sqlCmd += " from act_acno a, act_acct_hst h ";  
		sqlCmd += " where 1=1 ";
		sqlCmd += "   and a.acno_flag in ('3') ";
    sqlCmd += "   and a.p_seqno = h.p_seqno ";
	//sqlCmd += "   and a.corp_p_seqno = h.corp_p_seqno "; 上行才對 ?
		sqlCmd += "   and a.corp_p_seqno = ?  ";
		sqlCmd += "   and a.acct_type = ?  ";
		sqlCmd += "   and h.acct_month = ?  ";
		sqlCmd += " group by a.corp_p_seqno, a.acct_type ";

    setString(1, hAcnoCorpPSeqno);
    setString(2, hAcnoAcctType);
    setString(3, hWdayLastAcctMonth);
    int recordCnt = selectTable();
  //for (int ii = 0; ii < recordCnt; ii++) {
    if (recordCnt > 0) {
      hAchtLastMinPayDate = getValue("acht2.max_last_min_pay_date");
      hAchtLastPaymentDate = getValue("acht2.max_last_payment_date");
      hAchtStmtLastPayday = getValue("acht2.max_stmt_last_payday");
      hAchtStmtCycleDate = getValue("acht2.max_stmt_cycle_date");

      hAchtStmtThisTtlAmt = getValueDouble("acht2.h_acht_stmt_this_ttl_amt");
      hAchtStmtMp = getValueDouble("acht2.h_acht_stmt_mp");
      hAchtBilledEndBalBl = getValueDouble("acht2.h_acht_billed_end_bal_bl");
      hAchtBilledEndBalIt = getValueDouble("acht2.h_acht_billed_end_bal_it");
      hAchtBilledEndBalId = getValueDouble("acht2.h_acht_billed_end_bal_id");
      hAchtBilledEndBalOt = getValueDouble("acht2.h_acht_billed_end_bal_ot");
      hAchtBilledEndBalCa = getValueDouble("acht2.h_acht_billed_end_bal_ca");
      hAchtBilledEndBalAo = getValueDouble("acht2.h_acht_billed_end_bal_ao");
      hAchtBilledEndBalAf = getValueDouble("acht2.h_acht_billed_end_bal_af");
      hAchtBilledEndBalLf = getValueDouble("acht2.h_acht_billed_end_bal_lf");
      hAchtBilledEndBalPf = getValueDouble("acht2.h_acht_billed_end_bal_pf");
      hAchtBilledEndBalRi = getValueDouble("acht2.h_acht_billed_end_bal_ri");
      hAchtBilledEndBalPn = getValueDouble("acht2.h_acht_billed_end_bal_pn");
      hTempTtlAmtBal = getValueDouble("acht2.h_temp_ttl_amt_bal");
      hAchtTtlAmtBal = getValueDouble("acht2.h_acht_ttl_amt_bal");
      hAchtBillInterest = getValueDouble("acht2.h_acht_bill_interest");
      hAchtStmtAdjustAmt = getValueDouble("acht2.h_acht_stmt_adjust_amt");
      hAchtStmtLastTtl = getValueDouble("acht2.h_acht_stmt_last_ttl");
      hAchtStmtPaymentAmt = getValueDouble("acht2.h_acht_stmt_payment_amt");
      hAchtAcctJrnlBal = getValueDouble("acht2.h_acht_acct_jrnl_bal");
    } else {
      hAchtLastMinPayDate = "";
      hAchtLastPaymentDate = "";
      hAchtStmtLastPayday = "";
      hAchtStmtCycleDate = "";

      hAchtStmtThisTtlAmt = 0;
      hAchtStmtMp = 0;
      hAchtBilledEndBalBl = 0;
      hAchtBilledEndBalIt = 0;
      hAchtBilledEndBalId = 0;
      hAchtBilledEndBalOt = 0;
      hAchtBilledEndBalCa = 0;
      hAchtBilledEndBalAo = 0;
      hAchtBilledEndBalAf = 0;
      hAchtBilledEndBalLf = 0;
      hAchtBilledEndBalPf = 0;
      hAchtBilledEndBalRi = 0;
      hAchtBilledEndBalPn = 0;
      hTempTtlAmtBal = 0;
      hAchtTtlAmtBal = 0;
      hAchtBillInterest = 0;
      hAchtStmtAdjustAmt = 0;
      hAchtStmtLastTtl = 0;
      hAchtStmtPaymentAmt = 0;
      hAchtAcctJrnlBal = 0;
    }

  }

  /***********************************************************************/
  void selectCrdCard() throws Exception {
    String[] binTypeMap = { "A", "D", "E", "G", "J", "M", "N", "V", "O" };
    hAjlgValidCnt = 0;

    sqlCmd = "select bin_type,";
    sqlCmd += " card_no,";
    sqlCmd += " oppost_date,";
    sqlCmd += " current_code ";
    sqlCmd += " from crd_card  ";
    sqlCmd += "where corp_p_seqno = ?  ";
    sqlCmd += "  and acct_type    = ?  ";
    sqlCmd += "  and card_no      = major_card_no "
            + "ORDER by decode(oppost_date,'','30001231',oppost_date) desc,issue_date desc ";
    setString(1, hAcnoCorpPSeqno);
    setString(2, hAcnoAcctType);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
        hMPbtbBinType[i] = getValue("bin_type", i);
        hMCardCardNo[i] = getValue("card_no", i);
        hMCardOppostDate[i] = getValue("oppost_date", i);
        hMCardCurrentCode[i] = getValue("current_code", i);
    }
    int ptrBintableCnt = recordCnt;
    if (ptrBintableCnt == 0) {
        tempStopFlag = "N";
        return;
    }

    tempStopFlag = " ";
    hAjlgStopFlag = "0";
    hCardCurrentCode = hMCardCurrentCode[0];
    hCardCardNo = hMCardCardNo[0];
    hCardOppostDate = hMCardOppostDate[0];

    if (hMCardCurrentCode[0].equals("3")) {
        tempStopFlag = "3";
        hAjlgStopFlag = "3";
    }
    for (inta1 = 0; inta1 < ptrBintableCnt; inta1++) {
        if (hMCardCurrentCode[inta1].equals("0"))
            hAjlgValidCnt++;
        for (int inta2 = 0; inta2 < 9; inta2++) {
            if (binTypeMap[inta2].equals(hMPbtbBinType[inta1])) {
                hAjlgBinType = hAjlgBinType.substring(0, inta2) + binTypeMap[inta2]
                        + hAjlgBinType.substring(inta2 + 1);
                break;
            }
        }
    }
  }

  /***********************************************************************/
  int selectActJcicLog2() throws Exception { /* acct_jrnl_bal < 1000 need to check */
    hAjlgJcicAcctStatus = "";
    hAjlgAcctStatus = "";

    sqlCmd = "select jcic_acct_status,";
    sqlCmd += " acct_status ";
    sqlCmd += " from act_jcic_log a  ";
    sqlCmd += "where p_seqno = ?  ";
    sqlCmd += "and log_type = 'A'  ";
    sqlCmd += "and sub_log_type = ''  ";
    sqlCmd += "and acct_month = ? ";
    setString(1, hAcnoPSeqno);
    setString(2, hWdayLastAcctMonth);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        hAjlgJcicAcctStatus = getValue("jcic_acct_status");
        hAjlgAcctStatus = getValue("acct_status");
    } else
        return (1); /* '1' : can't to A or B */
    if (hAjlgJcicAcctStatus.length() == 0)
        return (1);
    return (0);
  }

  /***********************************************************************/
  int insertActAcnoExt() throws Exception {
    daoTable = "act_acno_ext";
    extendField = daoTable + ".";
    setValue(extendField + "p_seqno", hAcnoPSeqno);
    setValue(extendField + "jcic_bad_debt_date", hAaetJcicBadDebtDate);
    setValue(extendField + "mod_time", sysDate + sysTime);
    setValue(extendField + "mod_pgm", javaProgram);
    insertTable();
    if (dupRecord.equals("Y"))
        return (1);
    return (0);
  }

  /***********************************************************************/
  void updateActAcnoExt() throws Exception {
    daoTable = "act_acno_ext";
    updateSQL = " jcic_bad_debt_date = ?,";
    updateSQL += " mod_time           = sysdate,";
    updateSQL += " mod_pgm            = 'ActN215'";
    whereStr = "where p_seqno = ? ";
    setString(1, hBusiBusinessDate);
    setString(2, hAcnoPSeqno);
    updateTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("update_act_acno_ext not found!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  int selectActJcicEnd() throws Exception {
    sqlCmd = "select 1 cnt";
    sqlCmd += " from act_jcic_end  ";
    sqlCmd += "where p_seqno = ?  ";
    sqlCmd += "  and send_flag in ('U','Y','T') ";
    setString(1, hAcnoPSeqno);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        hTempCnt = getValueInt("cnt");
    } else
        return (1);
    return (0);
  }

  /***********************************************************************/
  void computeOverseaCashadvLimit() throws Exception {
    double lbOverseaCashPct = 0;
    sqlCmd  = "select max(oversea_cash_pct) as lb_oversea_cash_pct ";
    sqlCmd += " from cca_auth_parm  ";
    sqlCmd += "where area_type = 'T'  ";
    sqlCmd += "  and (card_note = '*' or card_note in ";
    sqlCmd += "       (select distinct card_note from crd_card   ";
    sqlCmd += "        where current_code = '0' and corp_p_seqno = ? ";
    sqlCmd += "          and acct_type = ? )) ";
    setString(1, hAcnoCorpPSeqno);
    setString(2, hAcnoAcctType);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        lbOverseaCashPct = getValueDouble("lb_oversea_cash_pct");
    }

    hOverseaCashadvLimit = 0;
    sqlCmd  = "select (case when floor( ? * ? / 100) > cashadv_loc_maxamt ";
    sqlCmd += "        then cashadv_loc_maxamt else floor( ? * ? / 100) end) ";
    sqlCmd += "        as h_oversea_cashadv_limit ";
    sqlCmd += " from ptr_acct_type  ";
    sqlCmd += "where acct_type = ?  ";
    setDouble(1, hAcnoLineOfCreditAmt);
    setDouble(2, lbOverseaCashPct);
    setDouble(3, hAcnoLineOfCreditAmt);
    setDouble(4, lbOverseaCashPct);
    setString(5, hAcnoAcctType);
    recordCnt = selectTable();
    if (recordCnt > 0) {
        hOverseaCashadvLimit = getValueDouble("h_oversea_cashadv_limit");
    }

  }

    /***********************************************************************/
    void selectCcaCardAcct() throws Exception {
      hAjlgTotAmtMonth = 0;
      hAjlgAdjEffSdate = "";
      hAjlgAdjEffEdate = "";
      hAjlgTempCreditAmt = 0;
      String tempAdjEffSdate = "", tempAdjEffEdate = "";

      extendField = "caca.";
      sqlCmd  = " select ";
      sqlCmd += " tot_amt_month,";
      sqlCmd += " adj_eff_start_date,";
      sqlCmd += " adj_eff_end_date ";
      sqlCmd += " from cca_card_acct ";
      sqlCmd += "where acno_p_seqno = ? ";
      sqlCmd += " and debit_flag = 'N' ";
      setString(1, hAcnoPSeqno);
      int recordCnt = selectTable();
      if (recordCnt > 0) {
          hAjlgTotAmtMonth = getValueDouble("caca.tot_amt_month");
          hAjlgAdjEffSdate = getValue("caca.adj_eff_start_date");
          hAjlgAdjEffEdate = getValue("caca.adj_eff_end_date");
      }

      tempAdjEffSdate = hAjlgAdjEffSdate;
      tempAdjEffEdate = hAjlgAdjEffEdate;
      if (tempAdjEffSdate.length() > 0) {
        if (tempAdjEffEdate.length() == 0) {
          tempAdjEffEdate = "99991231";
        }
/***
		    String tempLastSdate = comm.nextDate(hWdayLastCloseDate,1);
        if ((hWdayThisCloseDate.compareTo(tempAdjEffSdate)>=0 &&
             hWdayThisCloseDate.compareTo(tempAdjEffEdate)<=0)  || 
            (tempLastSdate.compareTo(tempAdjEffSdate)>=0  &&
             tempLastSdate.compareTo(tempAdjEffEdate)<=0)  ) 
***/
        if  (hWdayThisCloseDate.compareTo(tempAdjEffSdate)>=0 &&
             hWdayThisCloseDate.compareTo(tempAdjEffEdate)<=0  ) 
        {
          hAjlgTempCreditAmt = hAjlgTotAmtMonth;
        } else {
          hAjlgTempCreditAmt = hAcnoLineOfCreditAmt;
        }
      } else {
          hAjlgTempCreditAmt = hAcnoLineOfCreditAmt;
      }

    }

  /***********************************************************************/
  void selectBilContract() throws Exception {
    hAjlgUnpostInstFee = 0;
    hAjlgUnpostCardFee = 0;
    hAjlgBilledEndBalRi = 0;
    hAjlgUnpostInstStageFee = 0; /* 增設：對帳單分期之分期總額 */

    sqlCmd  = "select c.corp_p_seqno,";
    sqlCmd += " sum(decode(b.loan_flag, 'C',0,'Y',0, a.unit_price*(a.install_tot_term- a.install_curr_term)+a.remd_amt+decode(install_curr_term,0,first_remd_amt,0))) h_ajlg_unpost_inst_fee,";
    sqlCmd += " sum(decode(b.loan_flag, 'C',a.unit_price*(a.install_tot_term- a.install_curr_term)+a.remd_amt+decode(install_curr_term,0,first_remd_amt,0), 0)) h_ajlg_unpost_card_fee,";
    sqlCmd += " sum(decode(b.loan_flag, 'C',decode(a.install_curr_term,0,0,a.install_tot_term, a.unit_price+a.remd_amt,a.unit_price), 0)) h_ajlg_billed_end_bal_ri,";
    sqlCmd += " sum(decode(b.loan_flag, 'Y',a.unit_price*(a.install_tot_term- a.install_curr_term)+a.remd_amt+decode(install_curr_term,0,first_remd_amt,0), 0)) h_ajlg_unpost_inst_stage_fee ";
    sqlCmd += " from act_acno c , bil_contract a left join bil_merchant b ";
    sqlCmd += " on a.mcht_no = b.mcht_no ";
    sqlCmd += "where c.corp_p_seqno = ? ";
    sqlCmd += " and c.acct_type = ? ";
    sqlCmd += " and c.acno_flag = '3' and c.acno_p_seqno = a.p_seqno ";
    sqlCmd += " and a.install_tot_term != a.install_curr_term ";
    sqlCmd += " and a.contract_kind = '1' ";
  //sqlCmd += " and nvl(b.trans_flag,'N') != 'Y' ";
  //sqlCmd += " and a.auth_code NOT IN ('', 'N', 'REJECT', 'P', 'reject', 'LOAN') ";
  //sqlCmd += " and ( (a.post_cycle_dd > 0 OR a.installment_kind = 'F') ";
  //sqlCmd += " or ( a.post_cycle_dd = 0 ";
  //sqlCmd += " and a.DELV_CONFIRM_FLAG = 'Y' ";
  //sqlCmd += " and a.auth_code = 'DEBT')) ";
    sqlCmd += " group by c.corp_p_seqno ";
    setString(1, hAcnoCorpPSeqno);
    setString(2, hAcnoAcctType);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        hAjlgUnpostInstFee = getValueDouble("h_ajlg_unpost_inst_fee");
        hAjlgUnpostCardFee = getValueDouble("h_ajlg_unpost_card_fee");
        hAjlgBilledEndBalRi = getValueDouble("h_ajlg_billed_end_bal_ri");
        hAjlgUnpostInstStageFee = getValueDouble("h_ajlg_unpost_inst_stage_fee");
    }

  }

  /***********************************************************************/
  void selectActJcicLog1() throws Exception {

    sqlCmd = "select a.bill_type_flag,";
    sqlCmd += " a.acct_type ";
    sqlCmd += " from act_jcic_log a, act_acno b  ";
    sqlCmd += "where b.corp_p_seqno = ? and b.acno_p_seqno = a.p_seqno ";
    sqlCmd += "  and a.log_type = 'A' ";
    sqlCmd += " group by a.bill_type_flag,a.acct_type ";
    sqlCmd += " order by a.bill_type_flag desc,a.acct_type ";

    setString(1, hAcnoCorpPSeqno);
    int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMAjlgBillTypeFlag[i] = getValue("bill_type_flag", i);
            hMAjlgAcctType[i] = getValue("acct_type", i);
        }

    actJcicLogCnt = recordCnt;
  }

  /***********************************************************************/
  void insertAllCardByType() throws Exception {

    sqlCmd = "select ";
    sqlCmd += " card_no,";
    sqlCmd += " crt_user ";
    sqlCmd += "from crd_card ";
    sqlCmd += "where corp_p_seqno = ? ";
    sqlCmd += "  and acct_type = ? ";
    sqlCmd += "  and current_code = '0' ";
    setString(1, hAcnoCorpPSeqno);
    setString(2, hAcnoAcctType);
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
        hTempCardNo = getValue("card_no", i);
        hTempCreateId = getValue("crt_user", i);

        if (checkCrdJcic() == 0) {
            insertCrdJcic();
        }
    }

  }

  /***********************************************************************/
  int checkCrdJcic() throws Exception {
    tmpCount = 0;

    sqlCmd = "select count(*) tmp_count ";
    sqlCmd += " from crd_jcic  ";
    sqlCmd += "where card_no  = ?  ";
    sqlCmd += "  and current_code = '0'  ";
    sqlCmd += "  and to_jcic_date = '' ";
    setString(1, hTempCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) { //recordCnt always == 1，即使 count(*)==0，其 recordCnt == 1
        tmpCount = getValueInt("tmp_count");
    }

    return tmpCount;
  }

  /***********************************************************************/
  void insertCrdJcic() throws Exception {
    hModSeqno = comcr.getModSeq();

    daoTable = "crd_jcic";
    extendField = daoTable + ".";
    setValue(extendField + "card_no", hTempCardNo);
    setValue(extendField + "crt_date", hBusiBusinessDate);
    setValue(extendField + "CRT_USER ", hTempCreateId);
    setValue(extendField + "trans_type", "C");
    setValue(extendField + "current_code", "0");
    setValue(extendField + "mod_user", "icbcecs");
    setValue(extendField + "mod_time", sysDate + sysTime);
    setValue(extendField + "mod_pgm", javaProgram);
    setValue(extendField + "mod_seqno", hCardModSeqno);
    insertTable();
    //if (dupRecord.equals("Y")) {
    //    comcr.errRtn("insert_crd_jcic duplicate!", "", hCallBatchSeqno);
    //}非定義unique key
  }

  /***********************************************************************/
  void selectActNplCorp() throws Exception {
    sqlCmd = "select a.corp_no,";
    sqlCmd += " a.jcic_remark ";
    sqlCmd += " from act_npl_corp a,act_npl_log b  ";
    sqlCmd += "where a.file_name = b.file_name  ";
    sqlCmd += "  and a.corp_no   = b.corp_no  ";
    sqlCmd += "  and a.sale_date = ?  ";
    sqlCmd += "  and b.p_seqno   = ?  ";
    sqlCmd += "  and b.proc_mark = '00' ";
    setString(1, hAcnoSaleDate);
    setString(2, hAcnoPSeqno);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        hAlcpCorpNo = getValue("corp_no");
        hAlcpJcicRemark = getValue("jcic_remark");
    }

  }

  /***********************************************************************/
  int insertActJcicLog() throws Exception {
    daoTable = "act_jcic_log";
    extendField = daoTable + ".";
    setValue(extendField + "log_type", "A");
    setValue(extendField + "sub_log_type", "");
    setValue(extendField + "acct_month", hWdayThisAcctMonth);
    setValue(extendField + "corp_id_p_seqno", hAcnoCorpPSeqno);
    setValue(extendField + "acct_type", hAcnoAcctType);
    setValue(extendField + "p_seqno", hAcnoPSeqno);
    setValue(extendField + "id_p_seqno", "");
    setValue(extendField + "stmt_cycle", hWdayStmtCycle);
    setValue(extendField + "proc_date", hBusiBusinessDate);
    setValue(extendField + "corp_no", hAcnoCorpNo);
    setValue(extendField + "stmt_cycle_date", hAchtStmtCycleDate);
    setValueDouble(extendField + "line_of_credit_amt", hAcnoLineOfCreditAmt);
    setValue(extendField + "stmt_last_payday", hAchtStmtLastPayday);
    setValue(extendField + "bin_type", hAjlgBinType);
    setValueDouble(extendField + "cash_lmt_balance", hAcnoComboCashLimit);
    setValueDouble(extendField + "cashadv_limit", hAjlgCashadvLimit);
    setValueDouble(extendField + "stmt_this_ttl_amt", hAchtStmtThisTtlAmt);
    setValueDouble(extendField + "stmt_mp", hAchtStmtMp);
    setValueDouble(extendField + "billed_end_bal_bl", hAchtBilledEndBalBl);
    setValueDouble(extendField + "billed_end_bal_it", hAchtBilledEndBalIt);
    setValueDouble(extendField + "billed_end_bal_id", hAchtBilledEndBalId);
    setValueDouble(extendField + "billed_end_bal_ot", hAchtBilledEndBalOt);
    setValueDouble(extendField + "billed_end_bal_ca", hAchtBilledEndBalCa);
    setValueDouble(extendField + "billed_end_bal_ao", hAchtBilledEndBalAo);
    setValueDouble(extendField + "billed_end_bal_af", hAchtBilledEndBalAf);
    setValueDouble(extendField + "billed_end_bal_lf", hAchtBilledEndBalLf);
    setValueDouble(extendField + "billed_end_bal_pf", hAchtBilledEndBalPf);
    setValueDouble(extendField + "billed_end_bal_ri", hAjlgBilledEndBalRi);
    setValueDouble(extendField + "billed_end_bal_pn", hAchtBilledEndBalPn);
    setValueDouble(extendField + "ttl_amt_bal", hTempTtlAmtBal);
    setValueDouble(extendField + "bill_interest", hAchtBillInterest);
    setValueDouble(extendField + "stmt_adjust_amt", hAchtStmtAdjustAmt);
    setValueDouble(extendField + "unpost_inst_fee", hAjlgUnpostInstFee);
    setValueDouble(extendField + "unpost_card_fee", hAjlgUnpostCardFee);
    setValueDouble(extendField + "stmt_last_ttl", hAchtStmtLastTtl);
    setValue(extendField + "payment_amt_rate", hAjlgPaymentAmtRate);
    setValue(extendField + "payment_time_rate", hAjlgPaymentTimeRate);
    setValueDouble(extendField + "stmt_payment_amt", hAchtStmtPaymentAmt);
    setValue(extendField + "jcic_acct_status", hAjlgJcicAcctStatus);
    setValue(extendField + "jcic_acct_status_flag", hAjlgJcicAcctStatusFlag);
    setValue(extendField + "bill_type_flag", hAjlgBillTypeFlag);
    setValue(extendField + "status_change_date", hAcnoStatusChangeDate);
    setValue(extendField + "debt_close_date",
            hAjlgJcicAcctStatusFlag.equals("U") ? hAchtLastPaymentDate : "");
    setValue(extendField + "last_min_pay_date", hAchtLastMinPayDate);
    setValue(extendField + "last_payment_date", hAchtLastPaymentDate);
    setValue(extendField + "sale_date", hAcnoSaleDate);
    setValue(extendField + "npl_corp_no", hAlcpCorpNo);
    setValue(extendField + "jcic_remark", hAlcpJcicRemark);
    setValueDouble(extendField + "ecs_ttl_amt_bal", hAchtTtlAmtBal);
    setValueDouble(extendField + "acct_jrnl_bal", hAchtAcctJrnlBal);
    setValueDouble(extendField + "valid_cnt", hAjlgValidCnt);
    setValue(extendField + "acct_status", hAcnoAcctStatus);
    setValue(extendField + "stop_flag", hAjlgStopFlag);
    setValue(extendField + "report_reason", "01");
    setValue(extendField + "mod_pgm", javaProgram);
    setValue(extendField + "mod_time", sysDate + sysTime);
    setValueDouble(extendField + "unpost_inst_stage_fee", hAjlgUnpostInstStageFee);
    setValueDouble(extendField + "oversea_cashadv_limit", hOverseaCashadvLimit);
    setValueDouble(extendField + "year_revolve_int_rate", hYearRealIntRate);
    setValueDouble(extendField + "temp_of_credit_amt", hAjlgTempCreditAmt);
    setValueDouble(extendField + "cca_temp_credit_amt", hAjlgTotAmtMonth);
    setValue(extendField + "cca_adj_eff_start_date", hAjlgAdjEffSdate);
    setValue(extendField + "cca_adj_eff_end_date", hAjlgAdjEffEdate);

    insertTable();
    //if (dupRecord.equals("Y")) {
    //    comcr.errRtn("insert_act_jcic_log duplicate!", "", hCallBatchSeqno);
    //}非定義unique key
    return (0);
  }

  /***********************************************************************/
  int insertActJcicEnd() throws Exception {
    daoTable = "act_jcic_end";
    extendField = daoTable + ".";
    setValue(extendField + "p_seqno", hAcnoPSeqno);
    setValue(extendField + "acct_month", hWdayThisAcctMonth);
    setValue(extendField + "acct_type", hAcnoAcctType);
    setValue(extendField + "oppost_date", hCardOppostDate);
    setValue(extendField + "card_no", hCardCardNo);
    setValue(extendField + "curr_code", hCardCurrentCode);
    setValue(extendField + "acct_status", hAcnoAcctStatus);
    setValue(extendField + "send_flag", hAjlgJcicAcctStatusFlag);
    setValue(extendField + "proc_date", hBusiBusinessDate);
    setValue(extendField + "sale_date", hAcnoSaleDate);
    setValue(extendField + "mod_time", sysDate + sysTime);
    setValue(extendField + "mod_pgm", javaProgram);
    insertTable();
    if (dupRecord.equals("Y")) {
        return (1);
    }
    return (0);
  }

  /***********************************************************************/
  void updateActJcicEnd() throws Exception {
    daoTable = "act_jcic_end";
    updateSQL = " send_flag = ?,";
    updateSQL += " proc_date = ?,";
    updateSQL += " mod_time  = sysdate,";
    updateSQL += " mod_pgm   = 'ActN215'";
    whereStr = "where p_seqno = ? ";
    setString(1, hAjlgJcicAcctStatusFlag);
    setString(2, hBusiBusinessDate);
    setString(3, hAcnoPSeqno);
    updateTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("update_act_jcic_end not found!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    ActN215 proc = new ActN215();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
}

/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/05/03  V2.21.01    陳君暘    BECS-1060109-001  修正年費掛失費繳評      *
 *  106/06/30  V2.22.02    陳君暘    BECS-1060307-019  fix adjust_amt & bad_date*
 *  106/11/13  V2.22.03    陳君暘    BECS-1061110-090  payment_rate1 <> 'A'    *
 *  107/03/12  V2.22.04    陳君暘    BECS-1070309-016  add billed_end_bal_ca   *
 *  107/04/11  V2.22.05    陳君暘    BECS-1070411-023  bill_type_flag[0]       *
 *  108/05/29  V2.22.05    Brian     update to V2.22.05                        *
 *  109/07/29  V2.23.01    陳君暘    RECS-s1090715-073 unpost + auto installment *
 *  109/08/28  V2.23.01    Brian     update to V2.23.01                        *
 *  110/08/12  V2.23.04    JeffKung  fix temp_stop_flag的判斷                  *
 *  110/08/12  V1.23.05    JeffKung  ref_mantis 8297 : 分期餘額改為與ccas_limit一致 *
 *  111/10/12  V2.23.10  jiangyigndong  updated for project coding standard    *
 *  112/03/10  V2.23.11    Simon     TCB 商務卡個繳戶合併為一筆以公司戶報送，另*
 *                                   新增程式處理商務卡，本程式改只處理一般卡  *
 *  112/04/03  V2.23.12    Simon     只處理一般卡調整                          *
 *  112/04/05  V2.23.13    Simon     1.關帳後二日改為關帳後一日執行            *
 *                                   2.國外預借現金額度、循環信用年利率 取自 act_jcic_log*
 *  112/04/10  V2.23.14    Simon     remove hTempBilledEndBalLf,hTempBilledEndBalAf,*
 *                                   hTempBilledEndBalRi,hTempBilledEndBalPn   *
 *  112/04/19  V2.23.15    Simon     selectBilContract() code error fixed      *
 *  112/04/25  V2.23.16    Simon     comc.errExit() 取代 comcr.errRtn() 顯示"關帳日後一日執行"*
 *  112/05/01  V2.23.17    Simon     add fetch's daoTable                      *
 *  112/05/06  V2.23.18    Simon     use crd_card.bin_type instead of ptr_bintable.bin_type *
 *  112/08/29  V2.23.19    Simon     取消無需繳款設定上期應繳為0               *
 *  112/09/15  V2.23.20    Simon     1.新增讀取 cca_card_acct 臨調額度         *
 *                                   2.刪除 mega acct_type='03' 的判斷         *
 *                                   3.修正[2-1]若上期應付帳款小於或等於0,則循環信用餘額應等於0*
 *                                   4.修正[11-2]若本期相關欄位合計金額大於0，且上期未溢繳，則本期應付帳款金額應大於0*
 *                                   5.修正[11-3]若本期應付帳款等於0，則本期最低應繳金額應等於0*
 *                                   6.tcb 報 kk4 一般結清條件由 acct_jrnl_bal<=0 更改為 acct_jrnl_bal==0*
 *                                   7.skip 結案U無結清日期                    *
 *  112/09/16  V2.23.21    Simon     催、呆帳戶報送判斷條件餘額 >=1000更改為 >0*
 *  112/10/02  V2.23.22    Simon     催收帳戶餘額 < 1000 判斷條件更改為 == 0 才能清除債權狀態註記"A" in Chen-S 段落*
 *  112/10/23  V2.23.23    Simon     1.雙幣別一正一負約當台幣總欠小於等於0，則科目餘額欠款需歸0*
 *                                   2.過寬延日繳清欠款，衍生之利息小於溢付款使得總欠小於等於0，則科目餘額欠款需歸0*
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*kk4產檔前篩選卡戶資料處理程式*/
public class ActN015 extends AccessDAO {

    private final String PROGNAME = "kk4產檔前篩選卡戶資料處理程式 112/10/23 V2.23.23";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActN015";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hWdayLastAcctMonth = "";
    String hWdayStmtCycle = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctHolderId = "";
    String hAcnoAcctHolderIdCode = "";
    String hAcnoSaleDate = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpNo = "";
    String hAcnoPaymentRate1 = "";
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
    String[] hMPbtbBinType = new String[250];
    String[] hMCardCardNo = new String[250];
    String[] hMCardOppostDate = new String[250];
    String[] hMCardCurrentCode = new String[250];
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
    String hAcnoPaymentRate1Orig = "";
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
                comcr.errRtn("Usage : ActN015 [business_date]", "", hCallBatchSeqno);
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
            insertTmpPaymentRate();
            selectActAcno();
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
    void insertTmpPaymentRate() throws Exception {

        sqlCmd = "insert into tmp_payment_rate  ";
        sqlCmd += " (p_seqno)";
        sqlCmd += "select a.p_seqno ";
        sqlCmd += "  from act_acct_hst b,act_acno a ";
      //sqlCmd += " where  a.acno_flag <> 'Y'";
        sqlCmd += " where  a.acno_flag = '1'";
        sqlCmd += "   and  b.stmt_cycle != ''";
        sqlCmd += "   and  decode(b.acct_month, '','x',b.acct_month) = ?";
        sqlCmd += "   and  b.p_seqno = a.acno_p_seqno";
        sqlCmd += "   and  a.stmt_cycle =  ? ";
        sqlCmd += "   and  substr(a.payment_rate1,2,1) <> 'A' ";
        sqlCmd += "   and  (b.unbill_end_bal_lf+b.unbill_end_bal_af+b.billed_end_bal_lf+b.billed_end_bal_af) = b.stmt_this_ttl_amt";
        sqlCmd += "   and  b.stmt_this_ttl_amt > 0";
        setString(1, hWdayLlAcctMonth);
        setString(2, hWdayStmtCycle);
        insertTable();
        //if (dupRecord.equals("Y")) {
        //    comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        //}非定義unique key

    }


  /***********************************************************************/
  void selectActAcno() throws Exception {

    int badFlag = 0;
    double minIntRate=0;

    daoTable = "act_acct_hst b, ptr_actgeneral_n p, act_acno a";
    sqlCmd = "select ";
    sqlCmd += " a.acno_p_seqno,";
    sqlCmd += " cd.id_no ,";
    sqlCmd += " cd.id_no_code,";
    sqlCmd += " a.sale_date,";
    sqlCmd += " a.acct_type,";
    sqlCmd += " a.acct_key,";
    sqlCmd += " a.corp_p_seqno,";
    sqlCmd += " a.id_p_seqno,";
  //sqlCmd += " d.corp_no,";
    sqlCmd += " a.payment_rate1,";
    sqlCmd += " a.line_of_credit_amt,";
    sqlCmd += " a.combo_cash_limit,";
    sqlCmd += " a.acct_status,";
    sqlCmd += " decode(a.acct_status,'4',status_change_date,'') h_acno_status_change_date,";
    sqlCmd += " a.new_cycle_month,";
    sqlCmd += " decode(a.acct_status,'3',p.revolving_interest2,'4',"
            + " p.revolving_interest2,p.revolving_interest1) h_agnn_revolving_interest1, ";
    sqlCmd += " decode(a.revolve_int_sign,'+',a.revolve_int_rate,"
            + " a.revolve_int_rate*-1) h_acno_revolve_int_rate,";
    sqlCmd += " a.revolve_rate_s_month,";
    sqlCmd += " decode(a.revolve_rate_e_month,'','999912',a.revolve_rate_e_month) "
            + " as revolve_rate_e_month,";
    sqlCmd += " decode(b.stmt_cycle_date,'',b.acct_month||b.stmt_cycle,b.stmt_cycle_date) h_acht_stmt_cycle_date,";
    sqlCmd += " b.last_min_pay_date,";
    sqlCmd += " b.last_payment_date,";
    sqlCmd += " b.stmt_last_payday,";
    sqlCmd += " a.debt_close_date,";
    sqlCmd += " b.stmt_this_ttl_amt h_acht_stmt_this_ttl_amt,";
    sqlCmd += " b.stmt_mp h_acht_stmt_mp,";
    sqlCmd += " b.unbill_end_bal_bl h_acht_billed_end_bal_bl,";
    sqlCmd += " b.unbill_end_bal_it h_acht_billed_end_bal_it,";
    sqlCmd += " b.unbill_end_bal_id h_acht_billed_end_bal_id,";
    sqlCmd += " b.unbill_end_bal_ot h_acht_billed_end_bal_ot,";
    sqlCmd += " b.unbill_end_bal_ca h_acht_billed_end_bal_ca,";
    sqlCmd += " b.unbill_end_bal_ao h_acht_billed_end_bal_ao,";
    sqlCmd += " b.unbill_end_bal_af h_acht_billed_end_bal_af,";
    sqlCmd += " b.unbill_end_bal_lf h_acht_billed_end_bal_lf,";
    sqlCmd += " b.unbill_end_bal_pf h_acht_billed_end_bal_pf,";
    sqlCmd += " b.unbill_end_bal_ri h_acht_billed_end_bal_ri,";
    sqlCmd += " b.unbill_end_bal_pn+b.unbill_end_bal_sf+b.unbill_end_bal_cf+b.unbill_end_bal_cc h_acht_billed_end_bal_pn,";
  //sqlCmd += " b.billed_end_bal_ca+b.billed_end_bal_id+b.billed_end_bal_it+b.billed_end_bal_bl+b.billed_end_bal_db+b.billed_end_bal_cb+b.billed_end_bal_ot+b.billed_end_bal_ao-decode(sign(b.stmt_adjust_amt),-1,0,1,b.stmt_adjust_amt,b.stmt_adjust_amt) h_temp_ttl_amt_bal,";
    sqlCmd += " b.billed_end_bal_ca+b.billed_end_bal_id+b.billed_end_bal_it+"
            + " b.billed_end_bal_bl+b.billed_end_bal_db+b.billed_end_bal_cb+"
            + " b.billed_end_bal_ot+b.billed_end_bal_ao h_temp_ttl_amt_bal,";
    sqlCmd += " b.ttl_amt_bal h_acht_ttl_amt_bal,";
    sqlCmd += " b.unbill_end_bal_ri+b.unbill_end_bal_ci+b.unbill_end_bal_ai h_acht_bill_interest,";
    sqlCmd += " b.stmt_adjust_amt h_acht_stmt_adjust_amt,";
    sqlCmd += " b.stmt_last_ttl h_acht_stmt_last_ttl,";
    sqlCmd += " b.stmt_payment_amt h_acht_stmt_payment_amt,";
    sqlCmd += " b.acct_jrnl_bal h_acht_acct_jrnl_bal,";
    sqlCmd += " decode(b.acct_type,'05',0,a.line_of_credit_amt_cash) h_ajlg_cashadv_limit,";
    sqlCmd += " decode(c.p_seqno, null,'N','Y') h_temp_0e_flag ";
    sqlCmd += " from act_acct_hst b, ptr_actgeneral_n p, act_acno a "
            + "  left join crd_idno cd on cd.id_p_seqno = a.id_p_seqno "
            + "  left outer join tmp_payment_rate c on c.p_seqno = a.acno_p_seqno  ";
  //sqlCmd += "  left join crd_corp d on d.corp_p_seqno = a.corp_p_seqno "; //find corp_no in crd_corp
  //sqlCmd += "where a.acno_flag <> 'Y' ";
    sqlCmd += "where a.acno_flag = '1' ";
    sqlCmd += "  and b.stmt_cycle != '' ";
    sqlCmd += "  and decode(b.acct_month,'','x',b.acct_month) = ? ";
    sqlCmd += "  and b.p_seqno = a.acno_p_seqno ";
    sqlCmd += "  and a.stmt_cycle = ? ";
    sqlCmd += "  and a.acct_type = p.acct_type  ";
    sqlCmd += " order by a.acct_key ";

    setString(1, hWdayLastAcctMonth);
    setString(2, hWdayStmtCycle);
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
      hAcnoPSeqno = getValue("acno_p_seqno");
      hAcnoAcctHolderId = getValue("id_no");
      hAcnoAcctHolderIdCode = getValue("id_no_code");
      hAcnoSaleDate = getValue("sale_date");
      hAcnoAcctType = getValue("acct_type");
      hAcnoAcctKey = getValue("acct_key");
      hAcnoCorpPSeqno = getValue("corp_p_seqno");
      hAcnoIdPSeqno = getValue("id_p_seqno");
      hAcnoCorpNo = getValue("corp_no");
      hAcnoPaymentRate1 = getValue("payment_rate1");
      hAcnoPaymentRate1Orig = getValue("payment_rate1");
      hAcnoLineOfCreditAmt = getValueDouble("line_of_credit_amt");
      hAcnoComboCashLimit = getValueDouble("combo_cash_limit");
      hAcnoAcctStatus = getValue("acct_status");
      hAcnoStatusChangeDate = getValue("h_acno_status_change_date");
      hAcnoNewCycleMonth = getValue("new_cycle_month");

      hAgenRevolvingInterest1 = getValueDouble("h_agnn_revolving_interest1");
      hAcnoRevolveIntRate = getValueDouble("h_acno_revolve_int_rate");
      hAcnoRevolveRateSMonth = getValue("revolve_rate_s_month");
      hAcnoRevolveRateEMonth = getValue("revolve_rate_e_month");

      hAchtStmtCycleDate = getValue("h_acht_stmt_cycle_date");
      hAchtLastMinPayDate = getValue("last_min_pay_date");
      hAchtLastPaymentDate = getValue("last_payment_date");
      hAchtStmtLastPayday = getValue("stmt_last_payday");
      hAcnoDebtCloseDate = getValue("debt_close_date");
      hAchtStmtThisTtlAmt = getValueDouble("h_acht_stmt_this_ttl_amt");
      hAchtStmtMp = getValueDouble("h_acht_stmt_mp");
      hAchtBilledEndBalBl = getValueDouble("h_acht_billed_end_bal_bl");
      hAchtBilledEndBalIt = getValueDouble("h_acht_billed_end_bal_it");
      hAchtBilledEndBalId = getValueDouble("h_acht_billed_end_bal_id");
      hAchtBilledEndBalOt = getValueDouble("h_acht_billed_end_bal_ot");
      hAchtBilledEndBalCa = getValueDouble("h_acht_billed_end_bal_ca");
      hAchtBilledEndBalAo = getValueDouble("h_acht_billed_end_bal_ao");
      hAchtBilledEndBalAf = getValueDouble("h_acht_billed_end_bal_af");
      hAchtBilledEndBalLf = getValueDouble("h_acht_billed_end_bal_lf");
      hAchtBilledEndBalPf = getValueDouble("h_acht_billed_end_bal_pf");
      hAchtBilledEndBalRi = getValueDouble("h_acht_billed_end_bal_ri");
      hAchtBilledEndBalPn = getValueDouble("h_acht_billed_end_bal_pn");
      hTempTtlAmtBal = getValueDouble("h_temp_ttl_amt_bal");
      hAchtTtlAmtBal = getValueDouble("h_acht_ttl_amt_bal");
      hAchtBillInterest = getValueDouble("h_acht_bill_interest");
      hAchtStmtAdjustAmt = getValueDouble("h_acht_stmt_adjust_amt");
      hAchtStmtLastTtl = getValueDouble("h_acht_stmt_last_ttl");
      hAchtStmtPaymentAmt = getValueDouble("h_acht_stmt_payment_amt");
      hAchtAcctJrnlBal = getValueDouble("h_acht_acct_jrnl_bal");
      hAjlgCashadvLimit = getValueDouble("h_ajlg_cashadv_limit");
      hTemp0EFlag = getValue("h_temp_0e_flag");
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

      /* *chun-yang modify* */
      if(hAjlgJcicAcctStatus.equals("B"))
          selectCrdChgId();

      /*fix 20210812 temp_stop_flag應該是not euqals  */
      if ((hAcnoSaleDate.length() != 0)
              || ((hAjlgJcicAcctStatus.length() != 0) && (hAchtTtlAmtBal == 0))
            //|| ((hAjlgValidCnt == 0) && (hAchtAcctJrnlBal <= 0) && (!tempStopFlag.equals("N")))) {
              || ((hAjlgValidCnt == 0) && (hAchtAcctJrnlBal == 0) && (!tempStopFlag.equals("N")))) {
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

      minIntRate = 0;
      if ((hWdayThisAcctMonth.compareTo(hAcnoRevolveRateSMonth)>=0)&&
          (hWdayThisAcctMonth.compareTo(hAcnoRevolveRateEMonth)<= 0))
      {  minIntRate = hAcnoRevolveIntRate; }
      hYearRealIntRate = (hAgenRevolvingInterest1 + minIntRate) * 365 / 100;
      hYearRealIntRate = convAmtDp2r(hYearRealIntRate);

      computeOverseaCashadvLimit();

      if (hAjlgJcicAcctStatusFlag.equals("U") && hAchtLastPaymentDate.length() == 0) {
         showLogMessage("I", "", String.format("*** skip 結案U無結清日期 sPseqno:[%s], jcicAcctSt:[%s], jcicAcctStFlag:[%s] ",
           hAcnoPSeqno, hAjlgJcicAcctStatus, hAjlgJcicAcctStatusFlag));
         continue;
      }

      /* *chun-yang modify* */
      if (((hAjlgJcicAcctStatus.length() == 0) && (hAcnoStatusChangeDate.length() != 0))
            //|| ((hAjlgJcicAcctStatus.equals("A")) && (hTempTtlAmtBal < 1000)
              || ((hAjlgJcicAcctStatus.equals("A")) && (hTempTtlAmtBal == 0 )
              && (hAjlgJcicAcctStatusFlag.equals("Y") == false))) {
          showLogMessage("I", "", String.format("Chen-S N[%s] Status[%s] Original[%s] change to null ",
                  hAcnoAcctHolderId, hAjlgJcicAcctStatus, hAcnoStatusChangeDate));
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
    //    hAchtStmtLastTtl = 0;

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

      if (hAcnoAcctHolderId.length() != 0)
          if (selectColLiabNego() == 0) {
              if ((!hAcnoPaymentRate1.equals("0A")) && (!hAcnoPaymentRate1.equals("0B"))
                      && (!hAcnoPaymentRate1.equals("0E"))) {
                  hAcnoPaymentRate1 = "0C";
              }
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

      if ((hAjlgValidCnt != 0) && (hAchtStmtThisTtlAmt == 0)
              && (hAjlgJcicAcctStatusFlag.length() == 0) && (hAcnoLineOfCreditAmt == 0))
          continue;

      selectCcaCardAcct();

      selectBilContract();

      selectActJcicLog();
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
              tmpstr = String.format("01");
          hAjlgBillTypeFlag = tmpstr;
          /*** 新的帳單別註記 加報KK2以維持kk4帳單別註記一致 ***/
          insertAllCardByType();
      }

      if (hAcnoSaleDate.length() != 0)
          selectActNplCorp();

      insertActJcicLog();
      if ((hAjlgJcicAcctStatusFlag.equals("Y")) || (hAjlgJcicAcctStatusFlag.equals("U"))
              || (hAjlgJcicAcctStatusFlag.equals("T"))) {
          if (insertActJcicEnd() != 0)
              updateActJcicEnd();
      }
      totcnt++;
      if (totcnt % 10000 == 0)
          showLogMessage("I", "", String.format("處理筆數 : [%d]", totcnt));
    }
    closeCursor(cursorIndex);

  }

  /***********************************************************************/
  public double  convAmtDp2r(double cvtAmt) throws Exception
  {
    long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
    double cvtDouble =  ((double) cvtLong) / 100;
    return cvtDouble;
  }

  /***********************************************************************/
  void computeOverseaCashadvLimit() throws Exception {
    double lbOverseaCashPct = 0;
    sqlCmd  = "select max(oversea_cash_pct) as lb_oversea_cash_pct ";
    sqlCmd += " from cca_auth_parm  ";
    sqlCmd += "where area_type = 'T'  ";
    sqlCmd += "  and (card_note = '*' or card_note in ";
    sqlCmd += "       (select distinct card_note from crd_card   ";
    sqlCmd += "        where current_code = '0' and p_seqno = ? )) ";
    setString(1, hAcnoPSeqno);
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
    void selectCrdCard() throws Exception {
        String[] binTypeMap = { "A", "D", "E", "G", "J", "M", "N", "V", "O" };
        hAjlgValidCnt = 0;

        sqlCmd = "select bin_type,";
        sqlCmd += " card_no,";
        sqlCmd += " oppost_date,";
        sqlCmd += " current_code ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where p_seqno  = ?  ";
        sqlCmd += "  and card_no  = major_card_no "
                + "ORDER by decode(oppost_date,'','30001231',oppost_date) desc,issue_date desc ";
        setString(1, hAcnoPSeqno);
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
    int selectActJcicLog2()
            throws Exception { /* acct_jrnl_bal < 1000 need to check */
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
        updateSQL += " mod_pgm            = 'ActN015'";
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
    int selectColLiabNego() throws Exception {
        sqlCmd = "select 1 cnt";
        sqlCmd += " from col_liab_nego  ";
        sqlCmd += "where id_no          = ?  ";
        sqlCmd += "  and liab_status = '3' ";
        setString(1, hAcnoAcctHolderId);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempCnt = getValueInt("cnt");
        } else
            return (1);
        return (0);
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

        sqlCmd = "select sum(decode(b.loan_flag, 'C',0,'Y',0, a.unit_price*(a.install_tot_term- a.install_curr_term)+a.remd_amt+decode(install_curr_term,0,first_remd_amt,0))) h_ajlg_unpost_inst_fee,";
        sqlCmd += " sum(decode(b.loan_flag, 'C',a.unit_price*(a.install_tot_term- a.install_curr_term)+a.remd_amt+decode(install_curr_term,0,first_remd_amt,0), 0)) h_ajlg_unpost_card_fee,";
        sqlCmd += " sum(decode(b.loan_flag, 'C',decode(a.install_curr_term,0,0,a.install_tot_term, a.unit_price+a.remd_amt,a.unit_price), 0)) h_ajlg_billed_end_bal_ri,";
        sqlCmd += " sum(decode(b.loan_flag, 'Y',a.unit_price*(a.install_tot_term- a.install_curr_term)+a.remd_amt+decode(install_curr_term,0,first_remd_amt,0), 0)) h_ajlg_unpost_inst_stage_fee ";
        sqlCmd += " from act_acno c , bil_contract a left join bil_merchant b ";
        sqlCmd += " on a.mcht_no = b.mcht_no ";
        sqlCmd += "where a.acct_type = ? ";
        sqlCmd += " and c.acct_key = ? and c.acno_p_seqno = a.p_seqno ";
        sqlCmd += " and a.install_tot_term != a.install_curr_term ";
        sqlCmd += " and a.contract_kind = '1' ";
        sqlCmd += " and nvl(b.trans_flag,'N') != 'Y' ";
        sqlCmd += " and a.auth_code NOT IN ('', 'N', 'REJECT', 'P', 'reject', 'LOAN') ";
        sqlCmd += " and ( (a.post_cycle_dd > 0 OR a.installment_kind = 'F') ";
        sqlCmd += " or ( a.post_cycle_dd = 0 ";
        sqlCmd += " and a.DELV_CONFIRM_FLAG = 'Y' ";
        sqlCmd += " and a.auth_code = 'DEBT')) ";
        setString(1, hAcnoAcctType);
        setString(2, hAcnoAcctKey);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAjlgUnpostInstFee = getValueDouble("h_ajlg_unpost_inst_fee");
            hAjlgUnpostCardFee = getValueDouble("h_ajlg_unpost_card_fee");
            hAjlgBilledEndBalRi = getValueDouble("h_ajlg_billed_end_bal_ri");
            hAjlgUnpostInstStageFee = getValueDouble("h_ajlg_unpost_inst_stage_fee");
        }

    }

    /***********************************************************************/
    void selectActJcicLog() throws Exception {

        sqlCmd = "select a.bill_type_flag,";
        sqlCmd += " a.acct_type ";
        sqlCmd += " from act_jcic_log a ,act_acno b ";
        sqlCmd += "where a.id_p_seqno = ? and b.acno_p_seqno = a.p_seqno ";
        sqlCmd += "  and a.log_type = 'A' ";
        sqlCmd += " group by a.bill_type_flag,a.acct_type ";
        sqlCmd += " order by a.bill_type_flag desc,a.acct_type ";
        setString(1, hAcnoIdPSeqno);
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
        //sqlCmd += "where acno_p_seqno = ? ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "  and current_code = '0' ";
        setString(1, hAcnoPSeqno);
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
        setValue(extendField + "corp_id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField + "acct_type", hAcnoAcctType);
        setValue(extendField + "p_seqno", hAcnoPSeqno);
        setValue(extendField + "id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField + "stmt_cycle", hWdayStmtCycle);
        setValue(extendField + "proc_date", hBusiBusinessDate);
        setValue(extendField + "corp_no", "");
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
        updateSQL += " mod_pgm   = 'ActN015'";
        whereStr = "where p_seqno = ? ";
        setString(1, hAjlgJcicAcctStatusFlag);
        setString(2, hBusiBusinessDate);
        setString(3, hAcnoPSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_jcic_end not found!", "", hCallBatchSeqno);
        }

    }

    /******************************************************************************/
    void selectCrdChgId() throws Exception/* *chun-yang modify* */
    {
        hChgiOldId = "";
        hChgiOldIdCode = "";

        sqlCmd = " SELECT old_id_no, ";
        sqlCmd += "        old_id_no_code ";
        sqlCmd += "  FROM  crd_chg_id ";
        sqlCmd += " WHERE  id_no       = ? ";
        sqlCmd += "   AND  id_no_code  = ? ";
        sqlCmd += "   fetch first 1 rows only ";
        setString(1, hAcnoAcctHolderId);
        setString(2, hAcnoAcctHolderIdCode);
        selectTable();
        hChgiOldId = getValue("old_id");
        hChgiOldIdCode = getValue("old_id_code");

        if (hChgiOldId.length() != 0)
            selectColBadDebt();
    }

    /***************************************************************************/
    void selectColBadDebt() throws Exception/* *chun-yang modify* */
    {
        hCbdtAlwBadDate = "";

        sqlCmd = " SELECT  alw_bad_date ";
        sqlCmd += "  FROM  col_bad_debt ";
        sqlCmd += " WHERE  trans_type = '4' ";
        sqlCmd += "   AND  id_no         = ? ";
        sqlCmd += "   AND  id_code    = ? ";
        sqlCmd += "   fetch first 1 rows only ";
        setString(1, hChgiOldId);
        setString(2, hChgiOldIdCode);

        selectTable();
        hCbdtAlwBadDate = getValue("alw_bad_date");
        if (hCbdtAlwBadDate.length() != 0) {
            showLogMessage("I", "", String.format("Chen-D N[%s] O[%s]- [%s] change to [%s]", hAcnoAcctHolderId,
                    hChgiOldId, hAcnoStatusChangeDate, hCbdtAlwBadDate));
            hAcnoStatusChangeDate = hCbdtAlwBadDate;
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN015 proc = new ActN015();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/01/03  V1.00.00    phopho     program initial                          *
*  109/03/17  V1.00.01    phopho     fix bug: if (m_code > 99) m_code = 99;   *
*  109/05/14  V1.00.02    phopho     add select_col_nego_status_curr()        *
*  109/06/01  V1.00.03    phopho     Mantis 0003470: mcode 不補 0.             *
*  109/07/24  V1.00.04    phopho     insert_col_bad_debt: add nego_type, nego_status *
*  109/12/11  V1.00.05    shiyuqi       updated for project coding standard   *
*  112/07/20  V1.00.06    sunny      取消selectColNegoStatusCurr()執行                    *
*  112/08/05  V1.00.07    sunny      調整自動轉催會計帳分錄                                                 *
*  112/09/12  V1.00.08    sunny      增加平帳作業處理寫入act_jrnl                 *
*  112/10/03  V1.00.09    sunny      僅限一般卡自動轉催(排除商務卡)                                   *
*  112/11/19  V1.00.10    sunny      調整自動轉催會計帳分錄(從逐戶明細傳票改整批總額帳傳票)    *
*  112/11/19  V1.00.10    sunny      增加判斷溢繳款不轉催                                                      *
******************************************************************************/

package Col;

import java.text.Normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

public class ColB002 extends AccessDAO {
	public final boolean debug = false; //debug用
	public final boolean debug1 = false; //debug用
    private String progname = "每月轉催收資料處理程式 112/11/19  V1.00.10";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;

    String rptNameNovou = "";
    List<Map<String, Object>> lparNovou = new ArrayList<Map<String, Object>>();
    int rptSeqNovou = 0;
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";
    
    String tmpstr= "", tmpstr1 = "";

    String hBusiBusinessDate = "";
    String hCprmTransColFlag = "";
    int hCprmCycleNDays = 0;
    int hCprmTransColDay = 0;
    int hCprmTransColDay2 = 0;
    int hCprmTransColDay3 = 0;
    int hCprmExcTtlLmt1 = 0;
    int hCprmExcOweLmt1 = 0;
    int hCprmExcTtlLmt2 = 0;
    int hCprmExcOweLmt2 = 0;
    int hCprmCodeTtl3 = 0;
    int hCprmCodeOwe3 = 0;
    int hTempCount = 0;
    String hPaccCurrCode = "";
    String hPcceCurrChiName = "";
    String hWdayStmtCycle = "";
    String hWdayThisCloseDate = "";
    String hWdayNextAcctMonth = "";
    String hWdayThisAcctMonth = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctHolderId = "";
    String hAcnoAcctHolderIdCode = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpNo = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoPaymentRate1 = "";
    String hAcnoStopStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoAcctStatus = "";
    String hAcnoAcnoFlag = "";
    String hAcnoRecourseMark = "";
    String hAcnoCreditActNo = "";
    double hAcnoLineOfCreditAmt = 0;
    String hAcnoOrgDelinquentDate = "";
    String hAcnoAcctSubStatus = "";
    String hAcnoLegalDelayCode = "";
    String hAcnoNoCollectionFlag = "";
    String hAcnoNoCollectionSDate = "";
    String hAcnoNoCollectionEDate = "";
    String hAcnoNewCycleMonth = "";
    String hAcnoLastInterestDate = "";
    String hAcnoRowid = "";
    String hPaccAcctType = "";
    String hPaccChinName = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoLastPayDate = "";
    double hAcurTempUnbillInterest = 0;
    int hAcurAcctJrnlBal = 0;
    String hAcurRowid = "";
    int hAcctAcctJrnlBal = 0;
    double hAcctTempUnbillInterest = 0;
    String hAcctRowid = "";
    int hDebtEndBal = 0;
    String hTempLegalDelayCode = "";
    String hAcnoModUser = "";
    String hAcnoModPgm = "";
    long hAcnoModSeqno = 0;
    String hTempAcctCode = "";
    String hDebtAcctItemEname = "";
    String hDebtPostDate = "";
    String hDebtReferenceSeq = "";
    int hDebtDAvailableBal = 0;
    String hDebtRowid = "";
    double hTempRate = 0;
    double hTempRateAmt = 0;
    String hDebtItemOrderNormal = "";
    String hDebtItemOrderBackDate = "";
    String hDebtItemOrderRefund = "";
    String hDebtItemClassNormal = "";
    String hDebtItemClassBackDate = "";
    String hDebtItemClassRefund = "";
    String hDebtAcctItemCname = "";
    double hCbdtSrcAmt = 0;
    String hAcnoCorpNoCode = "";
    String type = "";
    String hVouchCdKind = "";
    String hTAcNo = "";
    int hTSeqno = 0;
    String hTDbcr = "";
    String hTMemo3Kind = "";
    String hTMemo3Flag = "";
    String hTDrFlag = "";
    String hTCrFlag = "";
    String tMemo3 = "";
    String hIdnoId = "";
    String hIdnoChiName = "";
    String hCb02Mcode = "";
    String hCurrNegoType = "";    //phopho add
    String hCurrNegoStatus = "";  //phopho add
    int inta = 0;
    double hAgenRevolvingInterest1 = 0;
    double hAgenRevolvingInterest2 = 0;
    double hAgenRevolvingInterest3 = 0;
    double hAgenRevolvingInterest4 = 0;
    double hAgenRevolvingInterest5 = 0;
    double hAgenRevolvingInterest6 = 0;
    String hDebtCardNo = "";
//    String h_system_vouch_date = "";
    String hBusinssChiDate = "";
//    String h_print_name = "";
//    String h_rpt_name = "";
    String hCbdtRowid = "";
    int hInt = 0;
    int procCount = 0;
    double[] nTotalAmt = new double[12];
    double[] t6TotalAmt = new double[12];
    double[] tTotalAmt = new double[12];
    double[][] t1TotalAmt = new double[12][12];
    double[][] t2TotalAmt = new double[12][12];
    
    int intb = 0, currTypeInt = 0;
    int mCode = 0, getActDebtFlag = 0;
    String temstr = "";
    double amtNovouchAf = 0, amtNovouchCf = 0;
    double amtNovouchPf = 0, amtNovouchAi = 0;
    String[] transDay = new String[30];
//    String h_gsvh_mod_pgm = "";
//    String h_gsvh_memo1 = "";
    String hAcnoStatusChangeDate = "";
    String hAcnoLegalDelayCodeDate = "";

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
                comc.errExit("Usage : ColB002 [date]", "               1.day : yyyymmdd");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            selectColParam();

            if (args.length == 1)
            	if (args[0].length() == 8)
                hBusiBusinessDate = args[0];
            
            if (hCprmTransColFlag.equals("1")) {
                if (selectPtrWorkday() != 0) {
                	exceptExit = 0;
                    comcr.errRtn(String.format("轉催收為每CYCLE後%02d日執行", hCprmCycleNDays), "", hCallBatchSeqno);
                }
            } else {
                if ((hCprmTransColDay != comcr.str2int(comc.getSubString(hBusiBusinessDate,6)))
                        && (hCprmTransColDay2 != comcr.str2int(comc.getSubString(hBusiBusinessDate,6)))
                        && (hCprmTransColDay3 != comcr.str2int(comc.getSubString(hBusiBusinessDate,6)))) {
                	exceptExit = 0;
                    comcr.errRtn(String.format("轉催收為每月[%02d] [%02d] [%02d] 日執行 (0 表不設定日)", hCprmTransColDay,
                            hCprmTransColDay2, hCprmTransColDay3), "", hCallBatchSeqno);
                }

            }

            hAcnoModUser = comc.commGetUserID();
            hAcnoModPgm = javaProgram;

            //會計計算--初始化
            for (inta = 0; inta < 10; inta++)
            {
            	tTotalAmt[inta] = 0;
                for (intb = 0; intb < 10; intb++)
                    t1TotalAmt[inta][intb] = 0;
            }	

            deleteColB002R1();
            selectPtrActgeneral();
            selectPtrAcctType();

            if (temstr.length()>0) { //java.lang.NullPointerException
            comc.writeReport(temstr, lparNovou);
            }
            if (args.length == 0) comcr.lpRtn("COL_D_VOUCH", hBusiBusinessDate);

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
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
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

    /***********************************************************************/
    void selectColParam() throws Exception {

        hCprmTransColFlag = "";
        hCprmCycleNDays = 0;
        hCprmTransColDay = 0;
        hCprmTransColDay2 = 0;
        hCprmTransColDay3 = 0;
        hCprmExcTtlLmt1 = 0;
        hCprmExcOweLmt1 = 0;
        hCprmExcTtlLmt2 = 0;
        hCprmExcOweLmt2 = 0;
        hCprmCodeTtl3 = 0;
        hCprmCodeOwe3 = 0;

        sqlCmd = "select trans_col_flag,";
        sqlCmd += "cycle_n_days,";
        sqlCmd += "trans_col_day,";
        sqlCmd += "trans_col_day2,";
        sqlCmd += "trans_col_day3,";
        sqlCmd += "exc_ttl_lmt_1,";
        sqlCmd += "exc_owe_lmt_1,";
        sqlCmd += "exc_ttl_lmt_2,";
        sqlCmd += "exc_owe_lmt_2,";
        sqlCmd += "m_code_ttl_3,";
        sqlCmd += "m_code_owe_3 ";
        sqlCmd += " from col_param ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_param not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCprmTransColFlag = getValue("trans_col_flag");
            hCprmCycleNDays = getValueInt("cycle_n_days");
            hCprmTransColDay = getValueInt("trans_col_day");
            hCprmTransColDay2 = getValueInt("trans_col_day2");
            hCprmTransColDay3 = getValueInt("trans_col_day3");
            hCprmExcTtlLmt1 = getValueInt("exc_ttl_lmt_1");
            hCprmExcOweLmt1 = getValueInt("exc_owe_lmt_1");
            hCprmExcTtlLmt2 = getValueInt("exc_ttl_lmt_2");
            hCprmExcOweLmt2 = getValueInt("exc_owe_lmt_2");
            hCprmCodeTtl3 = getValueInt("m_code_ttl_3");
            hCprmCodeOwe3 = getValueInt("m_code_owe_3");
        }
    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        sqlCmd = "select 1 cnt";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where ? = to_char(to_date(this_close_date,'yyyymmdd')+ ? days,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);
        setInt(2, hCprmCycleNDays);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempCount = getValueInt("cnt");
        } else
            return 1;
        return 0;
    }

    /***********************************************************************/
    void deleteColB002R1() throws Exception {
        daoTable = "col_b002r1";
        deleteTable();
    }

    /***********************************************************************/
    // 注意:ptr_actgeneral，改成用 ptr_actgeneral_n，多帶入 acct_type
    void selectPtrActgeneral() throws Exception {
        hAgenRevolvingInterest1 = 0;
        hAgenRevolvingInterest2 = 0;
        hAgenRevolvingInterest3 = 0;
        hAgenRevolvingInterest4 = 0;
        hAgenRevolvingInterest5 = 0;
        hAgenRevolvingInterest6 = 0;

        sqlCmd = "select revolving_interest1,";
        sqlCmd += "revolving_interest2,";
        sqlCmd += "revolving_interest3,";
        sqlCmd += "revolving_interest4,";
        sqlCmd += "revolving_interest5,";
        sqlCmd += "revolving_interest6 ";
        sqlCmd += " from ptr_actgeneral_n  ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actgeneral not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAgenRevolvingInterest1 = getValueDouble("revolving_interest1");
            hAgenRevolvingInterest2 = getValueDouble("revolving_interest2");
            hAgenRevolvingInterest3 = getValueDouble("revolving_interest3");
            hAgenRevolvingInterest4 = getValueDouble("revolving_interest4");
            hAgenRevolvingInterest5 = getValueDouble("revolving_interest5");
            hAgenRevolvingInterest6 = getValueDouble("revolving_interest6");
        }
    }

    /***********************************************************************/
    void selectPtrAcctType() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "b.curr_code,";
        sqlCmd += "min(a.curr_chi_name) h_pcce_curr_chi_name ";
        sqlCmd += "from ptr_currcode a,ptr_acct_type b ";
        sqlCmd += "where a.curr_code = b.curr_code ";
        sqlCmd += "group by b.curr_code ";

        int i = 0;
        openCursor();
        while (fetchTable()) {
            hPaccCurrCode = getValue("curr_code");
            hPcceCurrChiName = getValue("h_pcce_curr_chi_name");

            amtNovouchAf = amtNovouchCf = amtNovouchPf = amtNovouchAi = 0;

            currTypeInt = i;

            selectActAcno();                                            
                    
          //自動轉催會計啟帳(總額帳/總帳)
            vouchRtn();   //I001 轉催

          //自動轉催會計啟帳(總額帳/總帳)
            vouchRtnT2(); //I004 違約金先轉利息
            
          

            /* 1120803 sunny TCB取消
            if ((amtNovouchAf > 0) || (amtNovouchCf > 0) || (amtNovouchPf > 0) || (amtNovouchAi > 0))
                novouchReportRtn();
            */
            i++;
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        int noColFlag = 0, lInt = 0;

        sqlCmd = "select ";
        sqlCmd += "b.stmt_cycle,";
        sqlCmd += "b.this_close_date,";
        sqlCmd += "b.next_acct_month,";
        sqlCmd += "b.this_acct_month,";
//        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "d.id_no,";       //a.acct_holder_id
        sqlCmd += "d.id_no_code,";  //a.acct_holder_id_code
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "e.corp_no,";     //a.corp_no
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "a.payment_rate1,";
        sqlCmd += "a.stop_status,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.acno_flag,";
        sqlCmd += "decode(a.recourse_mark,'','N',a.recourse_mark) h_acno_recourse_mark,";
        sqlCmd += "a.credit_act_no,";
        sqlCmd += "a.line_of_credit_amt,";
        sqlCmd += "a.org_delinquent_date,";
//        sqlCmd += "decode(a.acct_sub_status,'','x',a.acct_sub_status) h_acno_acct_sub_status,";  //no column and never use
        sqlCmd += "'' h_acno_acct_sub_status,";
        sqlCmd += "decode(a.legal_delay_code,'','x',a.legal_delay_code) h_acno_legal_delay_code,";
        sqlCmd += "decode(a.no_collection_flag,'','N',a.no_collection_flag) h_acno_no_collection_flag,";
        sqlCmd += "decode(a.no_collection_s_date,'','30000101',a.no_collection_s_date) h_acno_no_collection_s_date,";
        sqlCmd += "decode(a.no_collection_e_date,'','30000101',a.no_collection_e_date) h_acno_no_collection_e_date,";
        sqlCmd += "a.new_cycle_month,";
        sqlCmd += "a.last_interest_date,";
        sqlCmd += "a.rowid as rowid,";
        sqlCmd += "c.acct_type as pacc_acct_type,";
        sqlCmd += "c.chin_name,";
        sqlCmd += "a.pay_by_stage_flag,";
        sqlCmd += "a.last_pay_date,";
        sqlCmd += "a.int_rate_mcode ";
        sqlCmd += "from act_acno a, ptr_workday b, ptr_acct_type c ";
        sqlCmd += "left join crd_idno d on a.id_p_seqno = d.id_p_seqno ";
        sqlCmd += "left join crd_corp e on a.corp_p_seqno = e.corp_p_seqno ";
        sqlCmd += "where a.stmt_cycle = b.stmt_cycle ";
        sqlCmd += "and decode(a.acct_status,'','x',a.acct_status) <='3' ";
        sqlCmd += "and c.curr_code = ? ";
        sqlCmd += "and decode(a.acct_type,'','x',a.acct_type) = c.acct_type ";
        sqlCmd += "and decode(decode(a.payment_rate1,'','00',a.payment_rate1),'0A','00','0B','00','0C','00', ";
        sqlCmd += "'0D','00','0E','00',a.payment_rate1) >= '01' ";
//        sqlCmd += "and a.p_seqno = a.gp_no ";
        sqlCmd += "and a.acno_flag = '1' "; //僅限一般卡
        if (debug) {
        	//sqlCmd += "and a.p_seqno='0007574491' ";
//        	sqlCmd += "and a.p_seqno in ('0007040789','0007046920','0007065336') ";
        	sqlCmd += "and a.p_seqno='0007701777' ";
        }
        
        setString(1, hPaccCurrCode);

        extendField = "act_acno.";
        
        int recordCnt = selectTable();
        for(int i = 0; i < recordCnt; i++) {
            hWdayStmtCycle = getValue("act_acno.stmt_cycle",i);
            hWdayThisCloseDate = getValue("act_acno.this_close_date",i);
            hWdayNextAcctMonth = getValue("act_acno.next_acct_month",i);
            hWdayThisAcctMonth = getValue("act_acno.this_acct_month",i);
//            h_acno_p_seqno = getValue("p_seqno",i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno",i);
            hAcnoAcctType = getValue("act_acno.acct_type",i);
            hAcnoAcctKey = getValue("act_acno.acct_key",i);
            hAcnoAcctHolderId = getValue("act_acno.id_no",i);
            hAcnoAcctHolderIdCode = getValue("act_acno.id_no_code",i);
            hAcnoIdPSeqno = getValue("act_acno.id_p_seqno",i);
            hAcnoCorpNo = getValue("act_acno.corp_no",i);
            hAcnoCorpPSeqno = getValue("act_acno.corp_p_seqno",i);
            hAcnoPaymentRate1 = getValue("act_acno.payment_rate1",i);
            hAcnoStopStatus = getValue("act_acno.stop_status",i);
            hAcnoStmtCycle = getValue("act_acno.stmt_cycle",i);
            hAcnoAcctStatus = getValue("act_acno.acct_status",i);
            hAcnoAcnoFlag   = getValue("act_acno.acno_flag",i); // 1120803 sunny add
            hAcnoRecourseMark = getValue("act_acno.h_acno_recourse_mark",i);
            hAcnoCreditActNo = getValue("act_acno.credit_act_no",i);
            hAcnoLineOfCreditAmt = getValueDouble("act_acno.line_of_credit_amt",i);
            hAcnoOrgDelinquentDate = getValue("act_acno.org_delinquent_date",i);
            hAcnoAcctSubStatus = getValue("act_acno.h_acno_acct_sub_status",i);
            hAcnoLegalDelayCode = getValue("act_acno.h_acno_legal_delay_code",i);
            hAcnoNoCollectionFlag = getValue("act_acno.h_acno_no_collection_flag",i);
            hAcnoNoCollectionSDate = getValue("act_acno.h_acno_no_collection_s_date",i);
            hAcnoNoCollectionEDate = getValue("act_acno.h_acno_no_collection_e_date",i);
            hAcnoNewCycleMonth = getValue("act_acno.new_cycle_month",i);
            hAcnoLastInterestDate = getValue("act_acno.last_interest_date",i);
            hAcnoRowid = getValue("act_acno.rowid",i);
            hPaccAcctType = getValue("act_acno.pacc_acct_type",i);
            hPaccChinName = getValue("act_acno.chin_name",i);
            hAcnoPayByStageFlag = getValue("act_acno.pay_by_stage_flag",i);
            hAcnoLastPayDate = getValue("act_acno.last_pay_date",i);

            hAcnoStatusChangeDate = "";
            hAcnoLegalDelayCodeDate = "";

            getActDebtFlag = 0;
            noColFlag = 0;

            //會計參數-初始化
            vounhInit();

//            m_code = comr.getMcode(h_acno_acct_type, h_acno_p_seqno);
            mCode = getValueInt("act_acno.int_rate_mcode",i);

            selectActAcct();
            selectActAcctCurr();

            if (hAcctAcctJrnlBal == 0) {
                if ((hAcnoAcctStatus.equals("3"))
                        && (!hAcnoLegalDelayCode.equals("9")))
                    updateActAcno9();
                continue;
            }

            if ((hAcnoAcctStatus.equals("1")) || (hAcnoAcctStatus.equals("2"))) {
                selectActDebt1();
                if ((mCode >= hCprmCodeTtl3) && (hAcctAcctJrnlBal <= hCprmExcTtlLmt2)) {
                    noColFlag = 1;
                    if (mCode >= hCprmCodeTtl3)
                        insertColB002R1(1); /* TTL&結欠本金 */
                    if (hAcnoAcctStatus.equals("1"))
                        continue;
                } else if ((mCode >= hCprmCodeOwe3) && (hDebtEndBal <= hCprmExcOweLmt2)) {
                    noColFlag = 1;
                    if (mCode >= hCprmCodeOwe3)
                        insertColB002R1(1);/* TTL&結欠本金 */
                    if (hAcnoAcctStatus.equals("1"))
                        continue;
                }
            }

            if ((hAcnoAcctStatus.equals("1")) || (hAcnoAcctStatus.equals("3"))
                    || ((hAcnoAcctStatus.equals("2")) && (noColFlag == 0))) {
                if (((mCode >= hCprmCodeTtl3) && (hAcctAcctJrnlBal > hCprmExcTtlLmt2))
                        || ((mCode >= hCprmCodeOwe3) && (hDebtEndBal <= hCprmExcOweLmt2))) {
                    if ((comc.getSubString(hAcnoNoCollectionFlag,0,1).equals("Y"))
                            && (hBusiBusinessDate.compareTo(hAcnoNoCollectionSDate) >= 0)
                            && (hBusiBusinessDate.compareTo(hAcnoNoCollectionEDate) <= 0)) {
                    	insertColB002R1(3); /* 暫不轉催 */
                        continue;
                    }
                    if ((mCode >= 7) && (mCode < 12))
                        lInt = 2;
                    if ((mCode >= 12) && (mCode < 24))
                        lInt = 3;
                    if (mCode >= 24)
                        lInt = 4;
                    updateActAcno(lInt);
                }
            }
        }
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctTempUnbillInterest = 0;
        hAcctAcctJrnlBal = 0;

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += "temp_unbill_interest,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from act_acct  ";
        sqlCmd += "where p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acct.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcctAcctJrnlBal = getValueInt("act_acct.acct_jrnl_bal");
            hAcctTempUnbillInterest = getValueDouble("act_acct.temp_unbill_interest");
            hAcctRowid = getValue("act_acct.rowid");
        }
    }

    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        hAcurTempUnbillInterest = 0;

        sqlCmd = "select temp_unbill_interest,";
        sqlCmd += "acct_jrnl_bal,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from act_acct_curr ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and  curr_code = '901' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acct_curr.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcurTempUnbillInterest = getValueDouble("act_acct_curr.temp_unbill_interest");
            hAcurAcctJrnlBal = getValueInt("act_acct_curr.acct_jrnl_bal");
            hAcurRowid = getValue("act_acct_curr.rowid");
        }
    }

    /***********************************************************************/
    void updateActAcno9() throws Exception {
        hAcnoModSeqno = comcr.getModSeq();

        daoTable = "act_acno";
        updateSQL = "legal_delay_code  = '9',";
        updateSQL += " legal_delay_code_date = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno  = ?";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hAcnoModUser);
        setString(3, hAcnoModPgm);
        setLong(4, hAcnoModSeqno);
        setRowId(5, hAcnoRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcno(int hTempFlag) throws Exception {
        String tmpstr = "";

        tmpstr = String.format("%d", hTempFlag);
        hTempLegalDelayCode = tmpstr;
        if (!hAcnoAcctStatus.equals("3")) {
            if (selectCrdCard2() != 0) {
                insertColB002R1(2); /* 尚有活卡 */
                return;  /* 轉催收需所有卡要先停卡 */ 
            }
            
            if (selectActAcctCurrOp() != 0) {
                insertColB002R1(1); /* TTL及結欠本金，尚有溢付款(僅檢核外幣) */
                return;  /* 外幣有溢付款不轉催 */ 
            }
            
            hCbdtSrcAmt = 0;
            selectActDebt();
            updateActAcctCurr();
            updateActAcct();
            selectColBadDebt();
            insertColBadDebt();
                        
            //自動轉催會計啟帳(逐戶明細帳)
            //colStartVouchDtl();
            
        } else {
            if (hAcnoLegalDelayCode.equals(hTempLegalDelayCode))
                return;
        }

        hAcnoModSeqno = comcr.getModSeq();

        daoTable = "act_acno";
        updateSQL = "acct_status   = '3',";
//        updateSQL += " acct_sub_status  = '1',";  //No column
        updateSQL += " status_change_date = decode(cast(? as varchar(1)),'3',status_change_date,cast(? as varchar(8))),";
        updateSQL += " legal_delay_code  = ?,";
        updateSQL += " legal_delay_code_date = decode(cast(? as varchar(1)),cast(? as varchar(1)),legal_delay_code_date,cast(? as varchar(8))),";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno  = ?";
        whereStr = "where rowid = ? ";
        setString(1, hAcnoAcctStatus);
        setString(2, hBusiBusinessDate);
        setString(3, hTempLegalDelayCode);
        setString(4, hAcnoLegalDelayCode);
        setString(5, hTempLegalDelayCode);
        setString(6, hBusiBusinessDate);
        setString(7, hAcnoModUser);
        setString(8, hAcnoModPgm);
        setLong(9, hAcnoModSeqno);
        setRowId(10, hAcnoRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }

        procCount++;
    }

    /***********************************************************************/
    int selectCrdCard2() throws Exception {
        int hInt = 0;

        sqlCmd = "select 1 cnt";
        sqlCmd += " from crd_card ";
//        sqlCmd += "where gp_no = ? ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and current_code = '0' ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hAcnoPSeqno);
        
        extendField = "crd_card_2.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hInt = getValueInt("crd_card_2.cnt");
        }

        return hInt;
    }

    /***********************************************************************/
    int selectActAcctCurrOp() throws Exception {
        int hInt = 0;

        sqlCmd = "select 1 cnt";
        sqlCmd += " from act_acct_curr ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and acct_jrnl_bal <> 0 ";
        sqlCmd += "and curr_code != '901' ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acct_curr.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hInt = getValueInt("act_acct_curr.cnt");
        }

        return hInt;
    }
    
    /***********************************************************************/
    void insertColB002R1(int inta) throws Exception {
        String tmp = "";

        if (mCode > 99) mCode = 99;  //phopho mod 2020.3.17
//        tmp = String.format("%02d", m_code);  //phopho 2020.6.1 Maintis 0003470
        tmp = String.format("%d", mCode);
        hCb02Mcode = tmp;
        if (hAcnoAcctHolderId.length() != 0) {
            selectCrdIdno();
        } else {
            selectCrdCorp();
        }
        if (getActDebtFlag == 0)
            selectActDebt1();
        //2020.5.14 phopho 增加【協商類別】【協商狀態】。
        //selectColNegoStatusCurr();  //20230720 sunny mark

        daoTable = "col_b002r1";
        extendField = daoTable + ".";
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"id_no", hIdnoId);
        setValue(extendField+"chi_name", hIdnoChiName);
        setValue(extendField+"mcode", hCb02Mcode);
        setValue(extendField+"payment_rate1", hAcnoPaymentRate1);
        setValueInt(extendField+"acct_jrnl_bal", hAcctAcctJrnlBal);
        setValueInt(extendField+"end_bal", hDebtEndBal);
        setValue(extendField+"acct_status", hAcnoAcctStatus);
        setValue(extendField+"credit_act_no", hAcnoCreditActNo);
        //2020.5.14 phopho add
//        setValue(extendField+"nego_type", hCurrNegoType);
//        setValue(extendField+"nego_status", hCurrNegoStatus);

        String type = "";
        switch (inta) {
        case 1:
            type = "1"; /*TTL&結欠本金*/
            break;
        case 2:
            type = "2"; /*暫不轉催*/
            break;
        case 3:
            type = "3"; /*尚有活卡*/
            break;
        case 5:
            type = "5"; /* 20231122 新增 尚有溢付款(僅檢核外幣)*/
            break;
        default:
            type = "4"; /*其他*/
            break;
        }
        setValue(extendField+"err_type", type);
        setValue(extendField+"crt_date", hBusiBusinessDate);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"pay_by_stage_flag", hAcnoPayByStageFlag);
        setValue(extendField+"last_pay_date", hAcnoLastPayDate);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_b002r1 duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoId = "";
        hIdnoChiName = "";

        sqlCmd = "select id_no,";
        sqlCmd += "chi_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        
        extendField = "crd_idno.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoId = getValue("crd_idno.id_no");
            hIdnoChiName = getValue("crd_idno.chi_name");
        }
    }

    /***********************************************************************/
    void selectCrdCorp() throws Exception {
        hIdnoId = "";
        hIdnoChiName = "";

        sqlCmd = "select corp_no,";
        sqlCmd += "chi_name ";
        sqlCmd += " from crd_corp  ";
        sqlCmd += "where corp_p_seqno = ? ";
        setString(1, hAcnoCorpPSeqno);
        
        extendField = "crd_corp.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoId = getValue("crd_corp.corp_no");
            hIdnoChiName = getValue("crd_corp.chi_name");
        }
    }

    /***********************************************************************/
    void selectActDebt1() throws Exception {
        hDebtEndBal = 0;

        sqlCmd = "select sum(end_bal) h_debt_end_bal ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and decode(curr_code,'','901',curr_code) = '901'  ";
        sqlCmd += "and  acct_code in ('BL','CA','IT','ID','AO','OT','CB') ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt_1.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct_sum not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDebtEndBal = getValueInt("act_debt_1.h_debt_end_bal");
        }

        getActDebtFlag = 1;
    }
    
    /***********************************************************************/
    void selectColNegoStatusCurr() throws Exception {
    	hCurrNegoType = "";
    	hCurrNegoStatus = "";

        sqlCmd = "select nego_type, nego_status from col_nego_status_curr ";
        sqlCmd += "where id_no = ? ";
        setString(1, hIdnoId);
        
        extendField = "col_nego_status_curr.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hCurrNegoType = getValue("col_nego_status_curr.nego_type");
        	hCurrNegoStatus = getValue("col_nego_status_curr.nego_status");
        }
    }

    /***********************************************************************/
    void selectActDebt() throws Exception {
        long lAmt;

        for (int int3 = 0; int3 < 10; int3++)
            nTotalAmt[int3] = 0;

        sqlCmd = "select ";
        sqlCmd += "decode(acct_code,'BL','CB','CA','CB','IT','CB','ID','CB','AO','CB','OT','CB','RI','CI','PN','CI','LF','CC','SF','CC','AF','CC','CF','CC','PF','CC',acct_code) h_temp_acct_code,";
        sqlCmd += "acct_code,";
        sqlCmd += "post_date,";
        sqlCmd += "reference_no,";
        sqlCmd += "end_bal,";
        sqlCmd += "d_avail_bal,";        
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from act_debt ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and decode(curr_code,'','901',curr_code) = '901' ";
        sqlCmd += "and acct_code != 'CB' ";
        sqlCmd += "and acct_code != 'CI' ";
        sqlCmd += "and acct_code != 'CC' ";
        sqlCmd += "and acct_code != 'DP' ";
        sqlCmd += "and acct_code != 'AI' ";
//        sqlCmd += "and acct_code != 'AF' ";
//        sqlCmd += "and acct_code != 'CF' ";
//        sqlCmd += "and acct_code != 'PF' ";
        sqlCmd += "and end_bal >0 ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt.";

        int recordCnt = selectTable();
        for(int i = 0; i< recordCnt ; i++ ){
        	hTempAcctCode = getValue("act_debt.h_temp_acct_code",i);
            hDebtAcctItemEname = getValue("act_debt.acct_code",i);
            hDebtPostDate = getValue("act_debt.post_date",i);
            hDebtReferenceSeq = getValue("act_debt.reference_no",i);
            hDebtEndBal = getValueInt("act_debt.end_bal",i);
            hDebtDAvailableBal = getValueInt("act_debt.d_avail_bal",i);
            hDebtRowid = getValue("act_debt.rowid",i);

          //本金
            if ((hDebtAcctItemEname.equals("BL")) || (hDebtAcctItemEname.equals("CA"))
                    || (hDebtAcctItemEname.equals("IT")) || (hDebtAcctItemEname.equals("ID"))
                    || (hDebtAcctItemEname.equals("OT")) || (hDebtAcctItemEname.equals("AO"))) {
//                nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
//                tTotalAmt[currTypeInt][1] = tTotalAmt[currTypeInt][1] + hDebtEndBal;
//                nTotalAmt[7] = nTotalAmt[7] + hDebtEndBal;
//                tTotalAmt[currTypeInt][7] = tTotalAmt[currTypeInt][7] + hDebtEndBal;
            	nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
                t1TotalAmt[currTypeInt][1] = t1TotalAmt[currTypeInt][1] + hDebtEndBal;
                nTotalAmt[2] = nTotalAmt[2] + hDebtEndBal;
                t1TotalAmt[currTypeInt][2] = t1TotalAmt[currTypeInt][2] + hDebtEndBal;
            }
          //利息
            if (hDebtAcctItemEname.equals("RI")) {
//                nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal;
//                tTotalAmt[currTypeInt][3] = tTotalAmt[currTypeInt][3] + hDebtEndBal;
//                nTotalAmt[6] = nTotalAmt[6] + hDebtEndBal;
//                tTotalAmt[currTypeInt][6] = tTotalAmt[currTypeInt][6] + hDebtEndBal;
            	nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
                t1TotalAmt[currTypeInt][1] = t1TotalAmt[currTypeInt][1] + hDebtEndBal;
            	nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal;
                t1TotalAmt[currTypeInt][3] = t1TotalAmt[currTypeInt][3] + hDebtEndBal;                
            }
            //違約金
            if (hDebtAcctItemEname.equals("PN")) {
//                nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal;
//                tTotalAmt[currTypeInt][3] = tTotalAmt[currTypeInt][3] + hDebtEndBal;
//                nTotalAmt[5] = nTotalAmt[5] + hDebtEndBal;
//                tTotalAmt[currTypeInt][5] = tTotalAmt[currTypeInt][5] + hDebtEndBal;
                nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
                t1TotalAmt[currTypeInt][1] = t1TotalAmt[currTypeInt][1] + hDebtEndBal;
            	nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal;
                t1TotalAmt[currTypeInt][3] = t1TotalAmt[currTypeInt][3] + hDebtEndBal;
                t2TotalAmt[currTypeInt][1] = t2TotalAmt[currTypeInt][1] + hDebtEndBal;  	//違約金金額(加總)
                t2TotalAmt[currTypeInt][2] = t2TotalAmt[currTypeInt][2] + hDebtEndBal;   	//違約金金額(加總)
                t6TotalAmt[1] = t6TotalAmt[1] + hDebtEndBal;  	//違約金金額(逐戶)
                t6TotalAmt[2] = t6TotalAmt[2] + hDebtEndBal;   	//違約金金額(逐戶)
            }
          //帳外息 TCB取消
//            if (hDebtAcctItemEname.equals("AI")) {
//                nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal;
//                tTotalAmt[currTypeInt][3] = tTotalAmt[currTypeInt][3] + hDebtEndBal;
//                nTotalAmt[9] = nTotalAmt[9] + hDebtEndBal;
//                tTotalAmt[currTypeInt][9] = tTotalAmt[currTypeInt][9] + hDebtEndBal;
//            }
            
            //掛失費
            if (hDebtAcctItemEname.equals("LF")) {
//                nTotalAmt[2] = nTotalAmt[2] + hDebtEndBal;
//                tTotalAmt[currTypeInt][2] = tTotalAmt[currTypeInt][2] + hDebtEndBal;
//                nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;
//                tTotalAmt[currTypeInt][4] = tTotalAmt[currTypeInt][4] + hDebtEndBal;
            	 nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
                 t1TotalAmt[currTypeInt][1] = t1TotalAmt[currTypeInt][1] + hDebtEndBal;
             	 nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;
                 t1TotalAmt[currTypeInt][4] = t1TotalAmt[currTypeInt][4] + hDebtEndBal;   
            }
            
            //20230724 AF,CF,PF於TCB均轉入CC催收款科目
            //-------------------------------------------
            //年費
            if (hDebtAcctItemEname.equals("AF")) {
                 nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
                 t1TotalAmt[currTypeInt][1] = t1TotalAmt[currTypeInt][1] + hDebtEndBal;  //催收款項(總和)
             	 nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;
                 t1TotalAmt[currTypeInt][4] = t1TotalAmt[currTypeInt][4] + hDebtEndBal;  //費用130270024(營運科)
            }
            
           //預借現金手續費
            if (hDebtAcctItemEname.equals("CF")) {
                 nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
                 t1TotalAmt[currTypeInt][1] = t1TotalAmt[currTypeInt][1] + hDebtEndBal;  //催收款項(總和)
            	 nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;
                 t1TotalAmt[currTypeInt][4] = t1TotalAmt[currTypeInt][4] + hDebtEndBal;  //費用130270024(營運科)
            }
            
            //雜項手續費
            if (hDebtAcctItemEname.equals("PF")) {
                nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
                t1TotalAmt[currTypeInt][1] = t1TotalAmt[currTypeInt][1] + hDebtEndBal;  //催收款項(總和)
           	    nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;
                t1TotalAmt[currTypeInt][4] = t1TotalAmt[currTypeInt][4] + hDebtEndBal;  //費用130270024(營運科)
            }
            //-------------------------------------------
            
            //法訴費 TCB 轉催前轉成催收費用類
            if (hDebtAcctItemEname.equals("SF")) {
//                nTotalAmt[2] = nTotalAmt[2] + hDebtEndBal;
//                tTotalAmt[currTypeInt][2] = tTotalAmt[currTypeInt][2] + hDebtEndBal;
//                nTotalAmt[8] = nTotalAmt[8] + hDebtEndBal;
//                tTotalAmt[currTypeInt][8] = tTotalAmt[currTypeInt][8] + hDebtEndBal;
                nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
                t1TotalAmt[currTypeInt][1] = t1TotalAmt[currTypeInt][1] + hDebtEndBal;  //催收款項(總和)
           	    nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;
                t1TotalAmt[currTypeInt][4] = t1TotalAmt[currTypeInt][4] + hDebtEndBal;  //費用130270024(營運科)
            }

// 20230724 Tcb取消此段處理，因AF,CF,PF於TCB均轉入CC催收款科目     
//            if ((hDebtAcctItemEname.equals("AF")) || (hDebtAcctItemEname.equals("CF"))
//                    || (hDebtAcctItemEname.equals("PF"))) {
//            } else {
//                updateActDebt();
//                insertColBadDetail();
//            }
            
          updateActDebt();
         //20230912 轉催部分增加平帳處理寫入jrnl            
          if(debug1)showLogMessage("I", "", " 770[1]-轉催平帳作業(selectActDebt) acct_code["+hDebtAcctItemEname+"]");
          insertActJrnl(1); //insert act_jrnl 舊科目(減項)
          insertActJrnl(2); //insert act_jrnl 新科目(加項)
          insertColBadDetail();          
        }

        /* 20230721 tcb先取消，計算cycle結帳日至轉催日之間的利息轉催收款利息*/
        /*

        if ((nTotalAmt[7]==0)&&(hAcurTempUnbillInterest ==0)) return;
        hTempAcctCode = "CB";
        selectPtrActcode();

        if ((hWdayThisAcctMonth.compareTo(hAcnoNewCycleMonth) == 0) && (hAcnoNewCycleMonth.length() > 0)
                && (hAcnoLastInterestDate.length() != 0)) {
            hWdayThisCloseDate = hAcnoLastInterestDate;
        }

        hTempRateAmt = 0;
      //DB2 與 Oracle 對日期(時間)直接加減結果的表示方式不同, 語法須修正  phopho 2019.7.19
//        sqlCmd = "select (? * ? * ( to_char(to_date(?,'yyyymmdd'),'yyyymmdd') - to_char(to_date(?,'yyyymmdd'),'yyyymmdd') ))* 0.0001 amt ";
//        sqlCmd += " from dual ";
        //弱掃不過 Potential ReDoS phopho 2019.8.15
        sqlCmd = "select ( ((?) * (?)) * (days(to_date(?,'yyyymmdd')) - days(to_date(?,'yyyymmdd')))) *0.0001 rate_amt ";
        sqlCmd += "from dual ";
        setDouble(1, hTempRate);
        setDouble(2, nTotalAmt[7]);
        setString(3, hBusiBusinessDate);
        setString(4, hWdayThisCloseDate);
        
        extendField = "temp_rate.";
        
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", String.format("公式:[(%f * %f * (%s - %s))* 0.0001]", hTempRate, nTotalAmt[7],
                    hBusiBusinessDate, hWdayThisCloseDate));
            comcr.errRtn("select_compute rate_amt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempRateAmt = getValueDouble("temp_rate.rate_amt");
        }

        lAmt = (long) (hTempRateAmt + hAcurTempUnbillInterest);
        hAcurTempUnbillInterest = 0;
        hAcctTempUnbillInterest = 0;
        hTempRateAmt = lAmt;

        nTotalAmt[3] = nTotalAmt[3] + lAmt;
        tTotalAmt[currTypeInt][3] = tTotalAmt[currTypeInt][3] + lAmt;
        nTotalAmt[9] = nTotalAmt[9] + lAmt;
        tTotalAmt[currTypeInt][9] = tTotalAmt[currTypeInt][9] + lAmt;

        hTempAcctCode = "CI";
        selectPtrActcode();
        insertActDebt();
        hDebtAcctItemEname = "CI";
        hDebtEndBal = (int) hTempRateAmt;
        hDebtDAvailableBal = (int) hTempRateAmt;
        insertColBadDetail();
        hAcctAcctJrnlBal = (int) (hAcctAcctJrnlBal + hTempRateAmt);
        hAcurAcctJrnlBal = (int) (hAcurAcctJrnlBal + hTempRateAmt);
        insertActJrnl(2);
        insertCycPyaj(2);
        */
    }

    /***********************************************************************/
    void updateActDebt() throws Exception {
        hAcnoModSeqno = comcr.getModSeq();

        selectPtrActcode();

        daoTable = "act_debt";
        updateSQL = "acct_code = ?,";
        updateSQL += " item_order_normal = ?,";
        updateSQL += " item_order_back_date = ?,";
        updateSQL += " item_order_refund = ?,";
        updateSQL += " item_class_normal = ?,";
        updateSQL += " item_class_back_date = ?,";
        updateSQL += " item_class_refund = ?,";
//        updateSQL += " acct_item_cname  = ?,";      
        updateSQL += " end_bal   = ?,";
        updateSQL += " d_avail_bal = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno  = ?,";
        updateSQL += " org_acct_code = ? "; //20231119 add 轉催前原始科目
        whereStr = "where rowid = ? ";
        setString(1, hTempAcctCode); //轉催科目
        setString(2, hDebtItemOrderNormal);
        setString(3, hDebtItemOrderBackDate);
        setString(4, hDebtItemOrderRefund);
        setString(5, hDebtItemClassNormal);
        setString(6, hDebtItemClassBackDate);
        setString(7, hDebtItemClassRefund);
//        setString(8, h_debt_acct_item_cname); 
        setInt(8, hDebtEndBal);
        setInt(9, hDebtDAvailableBal);
        setString(10, hAcnoModUser);
        setString(11, hAcnoModPgm);
        setLong(12, hAcnoModSeqno);
        setString(13, hDebtAcctItemEname); //20231119 add 轉催前原始科目
        setRowId(14, hDebtRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_debt not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectPtrActcode() throws Exception {
        hDebtItemOrderNormal = "";
        hDebtItemOrderBackDate = "";
        hDebtItemOrderRefund = "";
        hDebtItemClassNormal = "";
        hDebtItemClassBackDate = "";
        hDebtItemClassRefund = "";
        hDebtAcctItemCname = "";
        hTempRate = 0;

        sqlCmd = "select item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "chi_long_name,";
        sqlCmd += "decode(inter_rate_code,'1',?, '2',?, '3',?, '4',?, '5',?, '6',?,0) h_temp_rate ";
        sqlCmd += " from ptr_actcode  ";
        sqlCmd += "where acct_code = ? ";
        setDouble(1, hAgenRevolvingInterest1);
        setDouble(2, hAgenRevolvingInterest2);
        setDouble(3, hAgenRevolvingInterest3);
        setDouble(4, hAgenRevolvingInterest4);
        setDouble(5, hAgenRevolvingInterest5);
        setDouble(6, hAgenRevolvingInterest6);
        setString(7, hTempAcctCode);
        
        extendField = "ptr_actcode.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actcode not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDebtItemOrderNormal = getValue("ptr_actcode.item_order_normal");
            hDebtItemOrderBackDate = getValue("ptr_actcode.item_order_back_date");
            hDebtItemOrderRefund = getValue("ptr_actcode.item_order_refund");
            hDebtItemClassNormal = getValue("ptr_actcode.item_class_normal");
            hDebtItemClassBackDate = getValue("ptr_actcode.item_class_back_date");
            hDebtItemClassRefund = getValue("ptr_actcode.item_class_refund");
            hDebtAcctItemCname = getValue("ptr_actcode.chi_long_name");
            hTempRate = getValueDouble("ptr_actcode.h_temp_rate");
        }
    }

    /***********************************************************************/
    void insertActDebt() throws Exception {
        hDebtReferenceSeq = "";

        sqlCmd = "select substr(?,3,2)||substr(to_char(bil_postseq.nextval,'0000000000'),4,8) debt_reference_seq";
        sqlCmd += " from dual ";
        setString(1, hBusiBusinessDate);
        
        extendField = "debt_seq.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDebtReferenceSeq = getValue("debt_seq.debt_reference_seq");
        }

        selectCrdCard1();

        daoTable = "act_debt";
        extendField = daoTable + ".";
        setValue(extendField+"reference_no", hDebtReferenceSeq);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"acno_p_seqno", hAcnoPSeqno);  //phopho add
        setValue(extendField+"curr_code", "901");
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);  //no column
        setValue(extendField+"post_date", hBusiBusinessDate);
        setValue(extendField+"item_order_normal", hDebtItemOrderNormal);
        setValue(extendField+"item_order_back_date", hDebtItemOrderBackDate);
        setValue(extendField+"item_order_refund", hDebtItemOrderRefund);
        setValue(extendField+"item_class_normal", hDebtItemClassNormal);
        setValue(extendField+"item_class_back_date", hDebtItemClassBackDate);
        setValue(extendField+"item_class_refund", hDebtItemClassRefund);
        setValue(extendField+"acct_month", hWdayNextAcctMonth);
        setValue(extendField+"stmt_cycle", hWdayStmtCycle);
        setValue(extendField+"bill_type", "OSSG");
        setValue(extendField+"txn_code", "AI");
        setValueDouble(extendField+"beg_bal", hTempRateAmt);
        setValueDouble(extendField+"dc_beg_bal", hTempRateAmt);
        setValueDouble(extendField+"end_bal", hTempRateAmt);
        setValueDouble(extendField+"dc_end_bal", hTempRateAmt);
        setValueDouble(extendField+"d_avail_bal", hTempRateAmt);
        setValueDouble(extendField+"dc_d_avail_bal", hTempRateAmt);
        setValue(extendField+"card_no", hDebtCardNo);
        setValue(extendField+"acct_code", hTempAcctCode);
//        setValue("acct_item_cname", h_debt_acct_item_cname);  //no column
        setValue(extendField+"interest_date", hBusiBusinessDate);
        setValue(extendField+"purchase_date", hBusiBusinessDate);
//        setValue("acquire_date", h_busi_business_date);  //no column
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_debt duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectCrdCard1() throws Exception {
        hDebtCardNo = "";

        sqlCmd = "select card_no ";
        sqlCmd += " from crd_card ";
//        sqlCmd += "where gp_no = ? ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hAcnoPSeqno);
        
        extendField = "crd_card_1.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_card not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDebtCardNo = getValue("crd_card_1.card_no");
        }
    }

    /***********************************************************************/
    void insertColBadDetail() throws Exception {
    	daoTable = "col_bad_detail";
    	extendField = daoTable + ".";
        hCbdtSrcAmt = hCbdtSrcAmt + hDebtEndBal;
        setValue(extendField+"trans_type", "3");
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"trans_date", hBusiBusinessDate);
//        setValue("acct_item_ename", h_debt_acct_item_ename);
        setValue(extendField+"reference_no", hDebtReferenceSeq);
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);
        setValueInt(extendField+"end_bal", hDebtEndBal);
        setValueInt(extendField+"d_avail_bal", hDebtDAvailableBal);
//        setValue("new_item_ename", h_temp_acct_item_ename);
        setValue(extendField+"new_acct_code", hTempAcctCode);  //轉催科目
        setValue(extendField+"acct_code", hDebtAcctItemEname); //轉催前原始科目
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_bad_detail duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActJrnl(int type) throws Exception {
    	daoTable = "act_jrnl";
    	extendField = daoTable + ".";
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"curr_code", "901");
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
//        setValue("id_no", h_acno_acct_holder_id);
//        setValue("id_no_code", h_acno_acct_holder_id_code);
        setValue(extendField+"corp_p_seqno", hAcnoCorpPSeqno);
//        setValue("corp_no", h_acno_corp_no);
//        setValue("corp_no_code", h_acno_corp_no_code);
        setValue(extendField+"acct_date", hBusiBusinessDate);
        setValue(extendField+"tran_class", type == 1 ? "A" : "B");
        setValue(extendField+"tran_type", type == 1 ? "CD01" : "CD02");
//        setValue("item_ename", h_temp_acct_item_ename);
        setValue(extendField+"acct_code", type == 1 ? hDebtAcctItemEname:hTempAcctCode); //1使用原acct_code、2使用新的acct_code
        setValue(extendField+"dr_cr", type == 1 ? "D" : "C");
        setValue(extendField + "item_date", hDebtPostDate);      //20231119 add 原始交易入帳日期
        setValue(extendField+"reference_no", hDebtReferenceSeq); //20231119 add 交易參考號
        setValueInt(extendField+"transaction_amt", hDebtEndBal);
        setValueInt(extendField+"dc_transaction_amt", hDebtEndBal);
        setValueInt(extendField+"jrnl_bal", hAcctAcctJrnlBal);
        setValueInt(extendField+"dc_jrnl_bal", hAcctAcctJrnlBal);
        setValueInt(extendField+"item_bal", hDebtEndBal);
        setValueInt(extendField+"dc_item_bal", hDebtEndBal);
        setValueInt(extendField+"item_d_bal", hDebtDAvailableBal);
        setValueInt(extendField+"dc_item_d_bal", hDebtDAvailableBal);
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_jrnl duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertCycPyaj(int classCode) throws Exception {
    	daoTable = "cyc_pyaj";
    	extendField = daoTable + ".";
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"curr_code", "901");
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);
        setValue(extendField+"class_code", classCode == 1 ? "A" : "B");
        setValue(extendField+"payment_date", hBusiBusinessDate);
        setValueDouble(extendField+"payment_amt", hDebtEndBal * -1);
        setValueDouble(extendField+"dc_payment_amt", hDebtEndBal * -1);
        setValue(extendField+"payment_type", "CD01");
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"settle_flag", "U");
        setValue(extendField+"reference_no", hDebtReferenceSeq);
        setValue(extendField+"fee_flag", "Y");
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cyc_pyaj duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        daoTable = "act_acct_curr";
        updateSQL = "acct_jrnl_bal = ?,";
        updateSQL += " dc_acct_jrnl_bal = ?,";
        updateSQL += " temp_unbill_interest = ?,";
        updateSQL += " dc_temp_unbill_interest = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setInt(1, hAcurAcctJrnlBal);
        setInt(2, hAcurAcctJrnlBal);
        setDouble(3, hAcurTempUnbillInterest);
        setDouble(4, hAcurTempUnbillInterest);
        setString(5, hAcnoModPgm);
        setRowId(6, hAcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcct() throws Exception {
        daoTable = "act_acct";
        updateSQL = "acct_jrnl_bal = ?,";
        updateSQL += " temp_unbill_interest = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_user = ?,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setInt(1, hAcctAcctJrnlBal);
        setDouble(2, hAcctTempUnbillInterest);
        setString(3, hAcnoModUser);
        setString(4, hAcnoModPgm);
        setRowId(5, hAcctRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectColBadDebt() throws Exception {
        hCbdtRowid = "";

        sqlCmd = "select rowid as rowid ";
        sqlCmd += " from col_bad_debt  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and  trans_type = '3' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "col_bad_debt.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCbdtRowid = getValue("col_bad_debt.rowid");
        } else
            return;

        insertColBadDebthst();
        deleteColBadDebt();
    }

    /***********************************************************************/
    void insertColBadDebthst() throws Exception {
        sqlCmd = "insert into col_bad_debthst";
        sqlCmd += " select * from col_bad_debt where rowid = ? ";
        setRowId(1, hCbdtRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + "duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void deleteColBadDebt() throws Exception {
        daoTable = "col_bad_debt";
        whereStr = "where rowid = ? ";
        setRowId(1, hCbdtRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_col_bad_debt not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertColBadDebt() throws Exception {
    	//2020.7.24 phopho 增加【協商類別】【協商狀態】。
    	//20230803 sunny mark
    	//if (hAcnoAcctHolderId.length() != 0) {    	
    	if (hAcnoAcnoFlag.equals("1")) {
            selectCrdIdno();
        } else {
            selectCrdCorp();
        }
        
    	//selectColNegoStatusCurr(); //20230720 sunny mark
    	
    	daoTable = "col_bad_debt";
    	extendField = daoTable + ".";
        setValue(extendField+"trans_type", "3");
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"trans_date", hBusiBusinessDate);
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);
        setValue(extendField+"id_no", hAcnoAcctHolderId);
        setValue(extendField+"id_code", hAcnoAcctHolderIdCode);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"corp_no", hAcnoCorpNo);
        setValue(extendField+"corp_p_seqno", hAcnoCorpPSeqno);
        setValueDouble(extendField+"src_amt", hCbdtSrcAmt);
        setValue(extendField+"credit_act_no", hAcnoCreditActNo);
        setValueDouble(extendField+"line_of_credit_amt", hAcnoLineOfCreditAmt);
        setValue(extendField+"tran_source", "1");
        setValue(extendField+"settle_date", hAcnoOrgDelinquentDate);
//        setValue(extendField+"nego_type", hCurrNegoType);  //phopho add【協商類別】
//        setValue(extendField+"nego_status", hCurrNegoStatus);  //phopho add【協商狀態】
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_bad_debt duplicate!", "", hCallBatchSeqno);
        }
        
        if(debug)showLogMessage("I", "", " 000-t6 insertColBadDebt,id["+hAcnoAcctType+"-"+hAcnoAcctHolderId+"],p_seqno["+hAcnoPSeqno+"]");
    }
       
    /***********************************************************************/
    void colStartVouchDtl() throws Exception {
    	 /*I001轉催會計帳
         * *******************************************************
            
            I001    D    1    155410016    非放款轉列之催收款項-信用卡墊款轉入    每月不定期批次作業產生轉催收起帳
	            I001    C    2    130270032    應收信用卡款項-信用卡墊款                   每月不定期批次作業產生轉催收起帳
	            I001    C    3    130270041    應收信用卡款項-循環信用息                   每月不定期批次作業產生轉催收起帳
	            I001    C    4    130270024    應收信用卡款項-信用卡手續費                每月不定期批次作業產生轉催收起帳
	            I001    C    5    130270024    應收信用卡款項-信用卡手續費                每月不定期批次作業產生轉催收起帳
        */
        
        /*I004轉催會計帳
         * *******************************************************
            
           I004		D	130270041	 1	應收信用卡款項-循環信用息	轉催-違約金轉循環利息
				I004	C	130270024	2	應收信用卡款項-信用卡手續費	轉催-違約金轉循環利息
        */
    	tmpstr1="";
    	
        //判斷是個人戶放ID或公司放統編
        tmpstr1 = String.format("%s-%s每月自動轉催", hAcnoAcctType,hAcnoAcctHolderId);
        
        if (!hAcnoAcnoFlag.equals("1")) { 
            tmpstr1 = String.format("%s-%s每月自動轉催", hAcnoAcctType,hAcnoCorpNo);
        }
        
        //違約金轉催時多一套會計帳
        if (t6TotalAmt[1] > 0) {
            hVouchCdKind = "I004";            
            for (inta = 1; inta < 10; inta++)
            {
//            	t1TotalAmt[currTypeInt][inta] = t2TotalAmt[currTypeInt][inta];
//            	showLogMessage("I", "", " 888-1 t6 get ID["+hAcnoAcctType+"-"+hAcnoAcctHolderId+"]tTotalAmt["+currTypeInt+"]["+inta+"]=["+ t1TotalAmt[currTypeInt][inta]+"]");
            	tTotalAmt[inta] = 0;
            	tTotalAmt[inta] = t6TotalAmt[inta];
            	if(debug) {
            	showLogMessage("I", "", " 888-2 t6TotalAmt get ID["+hAcnoAcctType+"-"+hAcnoAcctHolderId+"],t6TotalAmt["+inta+"]=["+ t6TotalAmt[inta]+"]");
                showLogMessage("I", "", " 888-2 tTotalAmt  get ID["+hAcnoAcctType+"-"+hAcnoAcctHolderId+"],t6TotalAmt["+inta+"]=["+ nTotalAmt[inta]+"]");
            	}
            }
            
            tmpstr = "違約金轉循環息";
            comcr.hGsvhMemo1 = tmpstr1;
        	comcr.hGsvhMemo2= tmpstr;
            comcr.hGsvhModPgm = javaProgram;  
            //vouchRtnDtl();
        }
        
        hVouchCdKind = "I001";
        tmpstr="";
        
        //會計計算--初始化
        for (inta = 0; inta < 10; inta++)
        {
        	tTotalAmt[inta] = nTotalAmt[inta];
        	if(debug) showLogMessage("I", "", " 888 get tTotalAmt["+inta+"]=["+ nTotalAmt[inta]+"]");        
        }
        //vouchRtnDtl();
 }            

    /***********************************************************************/
    void vouchRtnT2() throws Exception {

    	hVouchCdKind = "I004";
    	comcr.hGsvhModPgm = javaProgram;

        comcr.startVouch("4", hVouchCdKind); //啟帳

        sqlCmd = "select ";
        sqlCmd += "gen_sys_vouch.ac_no,";
        sqlCmd += "gen_sys_vouch.dbcr_seq,";
        sqlCmd += "gen_sys_vouch.dbcr,";
        sqlCmd += "gen_acct_m.memo3_kind,";
        sqlCmd += "decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) memo3_flag,";
        sqlCmd += "decode(gen_acct_m.dr_flag,'','N',gen_acct_m.dr_flag) dr_flag,";
        sqlCmd += "decode(gen_acct_m.cr_flag,'','N',gen_acct_m.cr_flag) cr_flag ";
        sqlCmd += "from gen_sys_vouch,gen_acct_m ";
        sqlCmd += "where std_vouch_cd = ? ";
        sqlCmd += "and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
        sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
        setString(1, hVouchCdKind);
        
        extendField = "gen_sys_vouch.";

        int recordCnt = selectTable();
        
        if (recordCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, std_vouch_cd = "+ hVouchCdKind );
			return;
		}
        
        for (int i = 0; i < recordCnt; i++) {
            hTAcNo = getValue("gen_sys_vouch.ac_no", i);
            hTSeqno = getValueInt("gen_sys_vouch.dbcr_seq", i);
            hTDbcr = getValue("gen_sys_vouch.dbcr", i);
            hTMemo3Kind = getValue("gen_sys_vouch.memo3_kind", i);
            hTMemo3Flag = getValue("gen_sys_vouch.memo3_flag", i);
            hTDrFlag = getValue("gen_sys_vouch.dr_flag", i);
            hTCrFlag = getValue("gen_sys_vouch.cr_flag", i);

            tMemo3 = "";
           comcr.hGsvhMemo1 = "系統自動每月轉催收";
           comcr.hGsvhMemo2="違約金轉循環息";
                      
            if (t2TotalAmt[currTypeInt][hTSeqno] != 0) {
               if (comcr.detailVouch(hTAcNo, hTSeqno, t2TotalAmt[currTypeInt][hTSeqno]) != 0)
                comcr.errRtn("", "", hCallBatchSeqno);
               
                if(debug) {
                showLogMessage("I", "", " T2-999-1 get ID["+hAcnoAcctType+"-"+hAcnoAcctHolderId+"]tTotalAmt["+inta+"]=["+ nTotalAmt[inta]+"]");
                showLogMessage("I", "", " T2-999-3 get ID["+hAcnoAcctType+"-"+hAcnoAcctHolderId+"] t2TotalAmt["+currTypeInt+"]"+"["+hTSeqno+"]=["+ t1TotalAmt[currTypeInt][hTSeqno]+"]");
                }
            }
        }
    }

    /***********************************************************************/
    void vouchRtn() throws Exception {

    	hVouchCdKind = "I001"; //舊C-01

//        h_gsvh_mod_pgm = "col_b002";
    	comcr.hGsvhModPgm = javaProgram;

        comcr.startVouch("4", hVouchCdKind); //啟帳

        sqlCmd = "select ";
        sqlCmd += "gen_sys_vouch.ac_no,";
        sqlCmd += "gen_sys_vouch.dbcr_seq,";
        sqlCmd += "gen_sys_vouch.dbcr,";
        sqlCmd += "gen_acct_m.memo3_kind,";
        sqlCmd += "decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) memo3_flag,";
        sqlCmd += "decode(gen_acct_m.dr_flag,'','N',gen_acct_m.dr_flag) dr_flag,";
        sqlCmd += "decode(gen_acct_m.cr_flag,'','N',gen_acct_m.cr_flag) cr_flag ";
        sqlCmd += "from gen_sys_vouch,gen_acct_m ";
        sqlCmd += "where std_vouch_cd = ? ";
        sqlCmd += "and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
        sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
        setString(1, hVouchCdKind);
        
        extendField = "gen_sys_vouch.";

        int recordCnt = selectTable();
        
        if (recordCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, std_vouch_cd= "+ hVouchCdKind );
			return;
		}
        
        for (int i = 0; i < recordCnt; i++) {
            hTAcNo = getValue("gen_sys_vouch.ac_no", i);
            hTSeqno = getValueInt("gen_sys_vouch.dbcr_seq", i);
            hTDbcr = getValue("gen_sys_vouch.dbcr", i);
            hTMemo3Kind = getValue("gen_sys_vouch.memo3_kind", i);
            hTMemo3Flag = getValue("gen_sys_vouch.memo3_flag", i);
            hTDrFlag = getValue("gen_sys_vouch.dr_flag", i);
            hTCrFlag = getValue("gen_sys_vouch.cr_flag", i);
            
//           if(debug)
//               showLogMessage("I", "", " 999-4 test");
           
            if (t1TotalAmt[currTypeInt][hTSeqno] != 0) {
//            if (tTotalAmt[hTSeqno] != 0) {	
//                comcr.hGsvhMemo1= tmpstr1;
//            	comcr.hGsvhMemo2= comcr.hGsvhMemo2;
//            	comcr.hGsvhCurr = "00";
      
//               if (comcr.detailVouch(hTAcNo, hTSeqno, tTotalAmt[hTSeqno]) != 0)
                                       	
                tMemo3 = "";
                comcr.hGsvhMemo1 = "系統自動每月轉催收";
                
            	if (comcr.detailVouch(hTAcNo, hTSeqno, t1TotalAmt[currTypeInt][hTSeqno]) != 0)
                comcr.errRtn("", "", hCallBatchSeqno);
               
                if(debug) {
                showLogMessage("I", "", " 999-1 get ID["+hAcnoAcctType+"-"+hAcnoAcctHolderId+"]tTotalAmt["+inta+"]=["+ nTotalAmt[inta]+"]");
                showLogMessage("I", "", " 999-2 get ID["+hAcnoAcctType+"-"+hAcnoAcctHolderId+"] t1TotalAmt["+currTypeInt+"]"+"["+hTSeqno+"]=["+ t1TotalAmt[currTypeInt][hTSeqno]+"]");
                }
            }
        }
    }

    /***********************************************************************/
    /*
    void vouchRtnDtl() throws Exception {

    	//hVouchCdKind = "I001"; //舊C-01

//        h_gsvh_mod_pgm = "col_b002";
    	comcr.hGsvhModPgm = javaProgram;

        comcr.startVouch("4", hVouchCdKind); //啟帳

        sqlCmd = "select ";
        sqlCmd += "gen_sys_vouch.ac_no,";
        sqlCmd += "gen_sys_vouch.dbcr_seq,";
        sqlCmd += "gen_sys_vouch.dbcr,";
        sqlCmd += "gen_acct_m.memo3_kind,";
        sqlCmd += "decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) memo3_flag,";
        sqlCmd += "decode(gen_acct_m.dr_flag,'','N',gen_acct_m.dr_flag) dr_flag,";
        sqlCmd += "decode(gen_acct_m.cr_flag,'','N',gen_acct_m.cr_flag) cr_flag ";
        sqlCmd += "from gen_sys_vouch,gen_acct_m ";
        sqlCmd += "where std_vouch_cd = ? ";
        sqlCmd += "and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
        sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
        setString(1, hVouchCdKind);
        
        extendField = "gen_sys_vouch.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTAcNo = getValue("gen_sys_vouch.ac_no", i);
            hTSeqno = getValueInt("gen_sys_vouch.dbcr_seq", i);
            hTDbcr = getValue("gen_sys_vouch.dbcr", i);
            hTMemo3Kind = getValue("gen_sys_vouch.memo3_kind", i);
            hTMemo3Flag = getValue("gen_sys_vouch.memo3_flag", i);
            hTDrFlag = getValue("gen_sys_vouch.dr_flag", i);
            hTCrFlag = getValue("gen_sys_vouch.cr_flag", i);

            tMemo3 = "";
          // comcr.hGsvhMemo1 = "系統自動每月轉催收";

//            if (t1TotalAmt[currTypeInt][hTSeqno] != 0) {
            if (tTotalAmt[hTSeqno] != 0) {	
                comcr.hGsvhMemo1= tmpstr1;
            	comcr.hGsvhMemo2= comcr.hGsvhMemo2;
            	comcr.hGsvhCurr = "00";
      
                if (comcr.detailVouch(hTAcNo, hTSeqno, tTotalAmt[hTSeqno]) != 0)
//               if (comcr.detailVouch(hTAcNo, hTSeqno, t1TotalAmt[currTypeInt][hTSeqno]) != 0)
                comcr.errRtn("", "", hCallBatchSeqno);
                if(debug)
                showLogMessage("I", "", " 999-1 get ID["+hAcnoAcctType+"-"+hAcnoAcctHolderId+"]tTotalAmt["+inta+"]=["+ nTotalAmt[inta]+"]");
                showLogMessage("I", "", " 999-2 get AcnoFlag["+ hAcnoAcnoFlag +"],tTotalAmt["+tTotalAmt[hTSeqno]+"],tmpstr1["+tmpstr1+"],Memo1["+comcr.hGsvhMemo1+"],Memo2["+ comcr.hGsvhMemo2+"]");
            }
        }
    }
    */
    /***********************************************************************/
    void novouchReportRtn() throws Exception {

        temstr = String.format("%s/reports/COL_B002_NOVOU_%4.4s", comc.getECSHOME(), comc.getSubString(hBusiBusinessDate,4));
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);

        buf = "";
        buf = comcr.insertStr(buf, " " + comcr.bankName + " ", 26);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱  :COL_B002_NOVOU", 3);
        buf = comcr.insertStrCenter(buf, "催收(每月轉催收)現金制未起帳金額報表", 80);
        buf = comcr.insertStr(buf, "頁次:", 68);
        szTmp = String.format("%04d", 1);
        buf = comcr.insertStr(buf, szTmp, 73);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        sqlCmd = "select substr(to_char(to_number(?)- 19110000,'0000000'),2,7) chi_date ";
        sqlCmd += " from ptr_businday ";
        setString(1, comcr.hSystemVouchDate);
        
        extendField = "novouch.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusinssChiDate = getValue("novouch.chi_date");
        }

        buf = "";
        buf = comcr.insertStr(buf, "單    位:", 1);
        buf = comcr.insertStr(buf, "009", 11);
        buf = comcr.insertStr(buf, "交易日期:", 58);
        String cDate = String.format("%3.3s年%2.2s月%2.2s日", hBusinssChiDate, comc.getSubString(hBusinssChiDate,3),
        		comc.getSubString(hBusinssChiDate,5));
        buf = comcr.insertStr(buf, cDate, 68);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "調 整 性 質  :", 10);
        buf = comcr.insertStr(buf, " D 檔", 25);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "幣        別 :", 10);
        buf = comcr.insertStr(buf, hPcceCurrChiName, 25);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "\n";
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "年 費 金 額   :", 10);
        szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchAf);
        buf = comcr.insertStr(buf, szTmp, 30);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "預 現 手 續 費:", 10);
        szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchCf);
        buf = comcr.insertStr(buf, szTmp, 30);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "其 它 手 續 費:", 10);
        szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchPf);
        buf = comcr.insertStr(buf, szTmp, 30);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "帳 外 息 金 額:", 10);
        szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchAi);
        buf = comcr.insertStr(buf, szTmp, 30);
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

        buf = "";
        for (int i = 0; i < 78; i++)
            buf += "-";
        lparNovou.add(comcr.putReport(rptNameNovou, rptNameNovou, sysDate, ++rptSeqNovou, "0", buf));

    }
    void vounhInit() {   	
        //會計參數-初始化
        for (inta = 0; inta < 10; inta++) {
            t6TotalAmt[inta] = 0;
            nTotalAmt[inta] = 0;
        }
        
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB002 proc = new ColB002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

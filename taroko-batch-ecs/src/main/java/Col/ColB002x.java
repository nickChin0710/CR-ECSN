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
*  112/01/05  V1.00.07    sunny      調整處理邏輯，0元不處理                             *
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

public class ColB002x extends AccessDAO {
    private String progname = "補轉催收紀錄處理程式 112/01/05  V1.00.07";
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
    
    /********crd_idno**********/
    String crdIdPSeqno = "";
    boolean chkParmIdNo = false;

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
    String hDebtOrgAcctCode = "";
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
    String hCbdtModPgm = "";
    int hInt = 0;
    int procCount = 0;
    double[] nTotalAmt = new double[12];
    double[][] tTotalAmt = new double[12][12];
    double[][] t6TotalAmt = new double[12][12];
    
    int intb = 0, currTypeInt = 0;
    int mCode = 0, getActDebtFlag = 0;
    String temstr = "";
    double amtNovouchAf = 0, amtNovouchCf = 0;
    double amtNovouchPf = 0, amtNovouchAi = 0;
    String[] transDay = new String[30];
//    String h_gsvh_mod_pgm = "";
//    String h_gsvh_memo1 = "";
    String hPSeqno="";
    String hAcnoStatusChangeDate = "";
    String hAcnoLegalDelayCodeDate = "";

//****************************************************
    public final boolean debug = false; //debug用
//****************************************************
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
                comc.errExit("Usage : ColB002x [id_no]", "");
            }


            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            
            // 固定要做的

            hAcnoModUser = "SYSCNV";
            hAcnoModPgm = javaProgram;
            
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            if (args.length == 1)
	            if(args[0].length()==10)
	        		getIdPSeqno(args[0]);
            
            selectPtrBusinday();
            selectActAcno();        	
            
//            if (args.length == 1)
//            	if (args[0].length() == 8)
//                hBusiBusinessDate = args[0];

//            hAcnoModUser = comc.commGetUserID();


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
//    void selectColParam() throws Exception {
//
//        hCprmTransColFlag = "";
//        hCprmCycleNDays = 0;
//        hCprmTransColDay = 0;
//        hCprmTransColDay2 = 0;
//        hCprmTransColDay3 = 0;
//        hCprmExcTtlLmt1 = 0;
//        hCprmExcOweLmt1 = 0;
//        hCprmExcTtlLmt2 = 0;
//        hCprmExcOweLmt2 = 0;
//        hCprmCodeTtl3 = 0;
//        hCprmCodeOwe3 = 0;
//
//        sqlCmd = "select trans_col_flag,";
//        sqlCmd += "cycle_n_days,";
//        sqlCmd += "trans_col_day,";
//        sqlCmd += "trans_col_day2,";
//        sqlCmd += "trans_col_day3,";
//        sqlCmd += "exc_ttl_lmt_1,";
//        sqlCmd += "exc_owe_lmt_1,";
//        sqlCmd += "exc_ttl_lmt_2,";
//        sqlCmd += "exc_owe_lmt_2,";
//        sqlCmd += "m_code_ttl_3,";
//        sqlCmd += "m_code_owe_3 ";
//        sqlCmd += " from col_param ";
//        int recordCnt = selectTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("select_col_param not found!", "", hCallBatchSeqno);
//        }
//        if (recordCnt > 0) {
//            hCprmTransColFlag = getValue("trans_col_flag");
//            hCprmCycleNDays = getValueInt("cycle_n_days");
//            hCprmTransColDay = getValueInt("trans_col_day");
//            hCprmTransColDay2 = getValueInt("trans_col_day2");
//            hCprmTransColDay3 = getValueInt("trans_col_day3");
//            hCprmExcTtlLmt1 = getValueInt("exc_ttl_lmt_1");
//            hCprmExcOweLmt1 = getValueInt("exc_owe_lmt_1");
//            hCprmExcTtlLmt2 = getValueInt("exc_ttl_lmt_2");
//            hCprmExcOweLmt2 = getValueInt("exc_owe_lmt_2");
//            hCprmCodeTtl3 = getValueInt("m_code_ttl_3");
//            hCprmCodeOwe3 = getValueInt("m_code_owe_3");
//        }
//    }

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

   //***********************************************************************/
    void getIdPSeqno(String idNo) throws Exception {
    	sqlCmd = "select id_p_seqno from crd_idno where id_no = ? ";
    	setString(1,idNo);
      	extendField = "crd_idno.";
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		crdIdPSeqno = getValue("crd_idno.id_p_seqno");
    		chkParmIdNo = true;
    		showLogMessage("I", "", String.format("getIdPSeqno 處理id_no[%s]...", idNo));
    		showLogMessage("I", "", String.format("getIdPSeqno 取得id_p_seqno[%s]...", crdIdPSeqno));
    	}else {
    		comc.errExit("select crd_idno notfound ,參數2 身分證號不存在", "");
    	}
    
    }
//    /***********************************************************************/
//    // 注意:ptr_actgeneral，改成用 ptr_actgeneral_n，多帶入 acct_type
//    void selectPtrActgeneral() throws Exception {
//        hAgenRevolvingInterest1 = 0;
//        hAgenRevolvingInterest2 = 0;
//        hAgenRevolvingInterest3 = 0;
//        hAgenRevolvingInterest4 = 0;
//        hAgenRevolvingInterest5 = 0;
//        hAgenRevolvingInterest6 = 0;
//
//        sqlCmd = "select revolving_interest1,";
//        sqlCmd += "revolving_interest2,";
//        sqlCmd += "revolving_interest3,";
//        sqlCmd += "revolving_interest4,";
//        sqlCmd += "revolving_interest5,";
//        sqlCmd += "revolving_interest6 ";
//        sqlCmd += " from ptr_actgeneral_n  ";
//        sqlCmd += "fetch first 1 row only ";
//        int recordCnt = selectTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("select_ptr_actgeneral not found!", "", hCallBatchSeqno);
//        }
//        if (recordCnt > 0) {
//            hAgenRevolvingInterest1 = getValueDouble("revolving_interest1");
//            hAgenRevolvingInterest2 = getValueDouble("revolving_interest2");
//            hAgenRevolvingInterest3 = getValueDouble("revolving_interest3");
//            hAgenRevolvingInterest4 = getValueDouble("revolving_interest4");
//            hAgenRevolvingInterest5 = getValueDouble("revolving_interest5");
//            hAgenRevolvingInterest6 = getValueDouble("revolving_interest6");
//        }
//    }

//    /***********************************************************************/
//    void selectPtrAcctType() throws Exception {
//        sqlCmd = "select ";
//        sqlCmd += "b.curr_code,";
//        sqlCmd += "min(a.curr_chi_name) h_pcce_curr_chi_name ";
//        sqlCmd += "from ptr_currcode a,ptr_acct_type b ";
//        sqlCmd += "where a.curr_code = b.curr_code ";
//        sqlCmd += "group by b.curr_code ";
//
//        int i = 0;
//        openCursor();
//        while (fetchTable()) {
//            hPaccCurrCode = getValue("curr_code");
//            hPcceCurrChiName = getValue("h_pcce_curr_chi_name");
//
//            amtNovouchAf = amtNovouchCf = amtNovouchPf = amtNovouchAi = 0;
//
//            currTypeInt = i;
//
//            selectActAcno();            
//            i++;
//        }
//        closeCursor();
//    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        int noColFlag = 0, lInt = 0;

        sqlCmd = "select ";
        sqlCmd += "b.stmt_cycle,";
        sqlCmd += "b.this_close_date,";
        sqlCmd += "b.next_acct_month,";
        sqlCmd += "b.this_acct_month,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "d.id_no,";
        sqlCmd += "d.id_no_code,"; 
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "e.corp_no,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "a.payment_rate1,";
        sqlCmd += "a.stop_status,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.status_change_date,"; //狀態日期
        sqlCmd += "a.acno_flag,";
        sqlCmd += "decode(a.recourse_mark,'','N',a.recourse_mark) h_acno_recourse_mark,";
        sqlCmd += "a.credit_act_no,";
        sqlCmd += "a.line_of_credit_amt,";
        sqlCmd += "a.org_delinquent_date,";
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
        sqlCmd += "and decode(a.acct_type,'','x',a.acct_type) = c.acct_type ";
        sqlCmd += "and a.acct_status='3' "; //讀取已轉催收資料
        if(chkParmIdNo) {
    		sqlCmd += " and a.id_p_seqno = ? ";
    		setString(1,crdIdPSeqno);
    		showLogMessage("I", "", String.format("selectActAcno 處理id_p_seqno[%s]...", crdIdPSeqno));
    	}          
        	
//        if(debug)
//        {
//        sqlCmd += "and a.p_seqno='0025297556' ";
//        }
//        
        extendField = "act_acno.";
        
        int recordCnt = selectTable();
        for(int i = 0; i < recordCnt; i++) {
            hWdayStmtCycle = getValue("act_acno.stmt_cycle",i);
            hWdayThisCloseDate = getValue("act_acno.this_close_date",i);
            hWdayNextAcctMonth = getValue("act_acno.next_acct_month",i);
            hWdayThisAcctMonth = getValue("act_acno.this_acct_month",i);
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
            hAcnoStatusChangeDate = getValue("act_acno.status_change_date",i);
            
            if(debug)
            showLogMessage("I", "", " selectActAcno, p_seqno["+hAcnoPSeqno+"],acct_key["+hAcnoAcctKey+"]");
            
            hCbdtSrcAmt = 0;
            selectActDebtPerson();           
            if(selectColBadDebt()!=0) continue;
            selectActDebt();
            if (hDebtEndBal>0) insertColBadDebt();
            
//            showLogMessage("I", "", String.format("處理id_p_seqno[%s]...", hAcnoIdPSeqno));
            
//            if (hAcnoAcctType.equals("03"))
//            	selectActDebtCorp();
            
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
    void selectActDebtPerson() throws Exception {
        hDebtEndBal = 0;

        sqlCmd = "select sum(end_bal) h_debt_end_bal ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and decode(curr_code,'','901',curr_code) = '901'  ";
        sqlCmd += "and  acct_code in ('CB','CC','CI') and end_bal>0 ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_debt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDebtEndBal = getValueInt("act_debt_1.h_debt_end_bal");
        }

        if(debug)
        showLogMessage("I", "", " selectActDebtPerson, p_seqno["+hAcnoPSeqno+"],acct_key["+hAcnoAcctKey+"]");
//        getActDebtFlag = 1;
    }
    
    /***********************************************************************/
    void selectActDebtCorp() throws Exception {
        hDebtEndBal = 0;

        sqlCmd = "select sum(end_bal) h_debt_end_bal ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where p_seqno in (select p_seqno from crd_corp where corp_p_seqno = ?) ";
        sqlCmd += "and decode(curr_code,'','901',curr_code) = '901'  ";
        sqlCmd += "and  acct_code in ('CB','CC','CI') ";
        setString(1, hAcnoCorpPSeqno);
        
        extendField = "act_debt.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_debt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDebtEndBal = getValueInt("act_debt_1.h_debt_end_bal");
        }

//        getActDebtFlag = 1;
    }
       
    /***********************************************************************/
    void selectActDebt() throws Exception {
        long lAmt;

        for (int int3 = 0; int3 < 10; int3++)
            nTotalAmt[int3] = 0;

        sqlCmd = "select ";
//        sqlCmd += "decode(acct_code,'BL','CB','CA','CB','IT','CB','ID','CB','AO','CB','OT','CB','RI','CI','PN','CI','LF','CC','SF','CC','AF','CC','CF','CC','PF','CC',acct_code) h_temp_acct_code,";
        sqlCmd += "acct_code,";
        sqlCmd += "org_acct_code,";
        sqlCmd += "reference_no,";
        sqlCmd += "end_bal,";
        sqlCmd += "d_avail_bal,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from act_debt ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and decode(curr_code,'','901',curr_code) = '901' ";
        sqlCmd += "and acct_code in ('CB','CC','CI') ";
//        sqlCmd += "and acct_code != 'CB' ";
//        sqlCmd += "and acct_code != 'CI' ";
//        sqlCmd += "and acct_code != 'CC' ";
//        sqlCmd += "and acct_code != 'DP' ";
//        sqlCmd += "and acct_code != 'AI' ";
        sqlCmd += "and end_bal >0 ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt.";

        int recordCnt = selectTable();
        for(int i = 0; i< recordCnt ; i++ ){
//        	hTempAcctCode = getValue("act_debt.h_temp_acct_code",i);
            hDebtAcctItemEname = getValue("act_debt.acct_code",i);
            hDebtOrgAcctCode = getValue("act_debt.org_acct_code",i);
            hDebtReferenceSeq = getValue("act_debt.reference_no",i);
            hDebtEndBal = getValueInt("act_debt.end_bal",i);
            hDebtDAvailableBal = getValueInt("act_debt.d_avail_bal",i);
            hDebtRowid = getValue("act_debt.rowid",i);
            
          if(debug)
          showLogMessage("I", "", " selectActDebt, p_seqno["+hAcnoPSeqno+"],acct_key["+hAcnoAcctKey+"]");

          if (hDebtEndBal>0) insertColBadDetail();
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
        setValue(extendField+"trans_date", hAcnoStatusChangeDate); //改放act_acno.status_change_date
        setValue(extendField+"reference_no", hDebtReferenceSeq);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValueInt(extendField+"end_bal", hDebtEndBal);
        setValueInt(extendField+"d_avail_bal", hDebtDAvailableBal);
        setValue(extendField+"acct_code",hDebtOrgAcctCode );
        setValue(extendField+"new_acct_code", hDebtAcctItemEname);
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_bad_detail duplicate! p_seqno["+hAcnoPSeqno+"],acct_key["+hAcnoAcctKey+"]", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    int selectColBadDebt() throws Exception {
        hCbdtRowid = "";

        sqlCmd = "select rowid as rowid,mod_pgm ";
        sqlCmd += " from col_bad_debt  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and  trans_type = '3' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "col_bad_debt.";
        
        if(debug)
        showLogMessage("I", "", " selectColBadDebt, p_seqno["+hAcnoPSeqno+"],acct_key["+hAcnoAcctKey+"]");
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCbdtRowid = getValue("col_bad_debt.rowid");
            hCbdtModPgm = getValue("col_bad_debt.mod_pgm");
 
            if(!hCbdtModPgm.equals("ColB002"))
                showLogMessage("I", "", " selectColBadDebt且非ColB002程式處理，已存在跳過不處理, p_seqno["+hAcnoPSeqno+"],acct_key["+hAcnoAcctKey+"]");
            
            if(!hCbdtModPgm.equals("ColB002x"))
             showLogMessage("I", "", " selectColBadDebt且非ColB002x程式處理，已存在跳過不處理, p_seqno["+hAcnoPSeqno+"],acct_key["+hAcnoAcctKey+"]");
            
            return 1;
        } else
            return 0;      
        
//        insertColBadDebthst();
//        deleteColBadDebt();
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
    	if (hAcnoAcnoFlag.equals("1")) {
            selectCrdIdno();
        } else {
            selectCrdCorp();
        }
    	
    	daoTable = "col_bad_debt";
    	extendField = daoTable + ".";
        setValue(extendField+"trans_type", "3");
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"trans_date", hAcnoStatusChangeDate); //改放act_acno.status_change_date
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"acct_type", hAcnoAcctType);
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
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hAcnoModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_bad_debt duplicate! p_seqno["+hAcnoPSeqno+"],acct_key["+hAcnoAcctKey+"]", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB002x proc = new ColB002x();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

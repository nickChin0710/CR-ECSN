/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/01/04  V1.00.00    phopho     program initial                          *
*  109/05/14  V1.00.01    phopho     add select_col_nego_status_curr()        *
*  109/06/01  V1.00.02    phopho     Mantis 0003470: mcode 不補 0.             *
*  109/12/12  V1.00.03    shiyuqi       updated for project coding standard   *
*  112/08/18  V1.00.04    Ryan       selectColNegoStatusCurr 改為   selectCpbdueCurrType  *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class ColB023 extends AccessDAO {
    private String progname = "應轉逾放但未轉逾放處理程式    112/08/18  V1.00.04  ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommString     comStr     = new CommString();
    CommCrdRoutine comcr    = null;
    CommRoutine    comr     = null;

    String hCallBatchSeqno = "";

    int hCprmExcTtlLmt1 = 0;
    int hCprmExcOweLmt1 = 0;
    int hCprmExcTtlLmt2 = 0;
    int hCprmExcOweLmt2 = 0;
    int hCprmCodeTtlS1 = 0;
    int hCprmCodeTtlE1 = 0;
    int hCprmCodeOweS1 = 0;
    int hCprmCodeOweE1 = 0;
    int hCprmCodeTtl1 = 0;
    int hCprmCodeOwe1 = 0;
    int hCprmCodeTtl2 = 0;
    int hCprmCodeOwe2 = 0;
    int hCprmCodeTtl3 = 0;
    int hCprmCodeOwe3 = 0;
    String hBusiBusinessDate = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoStmtCycle = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoPaymentRate1 = "";
    String hAcnoCreditActNo = "";
    String hAcnoNoDelinquentFlag = "";
    String hAcnoNoDelinquentSDate = "";
    String hAcnoNoDelinquentEDate = "";
    String hAcnoPayByStageFlag = "";
    int hAcctAcctJrnlBal = 0;
    String hAcnoNoLastPayDate = "";
    int hDebtEndBal = 0;
    String hIdnoId = "";
    String hIdnoChiName = "";
    String hCb01Mcode = "";
    String hCurrNegoType = "";    //phopho add
    String hCurrNegoStatus = "";  //phopho add
    String hCpbdueCurrType = "";
    String inta                        = "";
    int procCount = 0;
    int mCode;
    int recLine = 0, idxLine = 0, skipLine = 0, errorLine = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 0) {
                comc.errExit("Usage : ColB023", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            deleteColB001R1();

            selectColParam();
            selectPtrBusinday();
            selectCpbdueCurrType();
            selectActAcno();

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
    void deleteColB001R1() throws Exception {
        daoTable = "col_b001r1";
        deleteTable();
    }

    /***********************************************************************/
    void selectColParam() throws Exception {
        hCprmExcTtlLmt1 = 0;
        hCprmExcOweLmt1 = 0;
        hCprmExcTtlLmt2 = 0;
        hCprmExcOweLmt2 = 0;
        hCprmCodeTtlS1 = 0;
        hCprmCodeTtlE1 = 0;
        hCprmCodeOweS1 = 0;
        hCprmCodeOweE1 = 0;
        hCprmCodeTtl1 = 0;
        hCprmCodeOwe1 = 0;
        hCprmCodeTtl2 = 0;
        hCprmCodeOwe2 = 0;
        hCprmCodeTtl3 = 0;
        hCprmCodeOwe3 = 0;

        sqlCmd = "select exc_ttl_lmt_1,";
        sqlCmd += "exc_owe_lmt_1,";
        sqlCmd += "exc_ttl_lmt_2,";
        sqlCmd += "exc_owe_lmt_2,";
        sqlCmd += "m_code_ttl_s1,";
        sqlCmd += "m_code_ttl_e1,";
        sqlCmd += "m_code_owe_s1,";
        sqlCmd += "m_code_owe_e1,";
        sqlCmd += "m_code_ttl_1,";
        sqlCmd += "m_code_owe_1,";
        sqlCmd += "m_code_ttl_2,";
        sqlCmd += "m_code_owe_2,";
        sqlCmd += "m_code_ttl_3,";
        sqlCmd += "m_code_owe_3 ";
        sqlCmd += " from col_param ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_param not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCprmExcTtlLmt1 = getValueInt("exc_ttl_lmt_1");
            hCprmExcOweLmt1 = getValueInt("exc_owe_lmt_1");
            hCprmExcTtlLmt2 = getValueInt("exc_ttl_lmt_2");
            hCprmExcOweLmt2 = getValueInt("exc_owe_lmt_2");
            hCprmCodeTtlS1 = getValueInt("m_code_ttl_s1");
            hCprmCodeTtlE1 = getValueInt("m_code_ttl_e1");
            hCprmCodeOweS1 = getValueInt("m_code_owe_s1");
            hCprmCodeOweE1 = getValueInt("m_code_owe_e1");
            hCprmCodeTtl1 = getValueInt("m_code_ttl_1");
            hCprmCodeOwe1 = getValueInt("m_code_owe_1");
            hCprmCodeTtl2 = getValueInt("m_code_ttl_2");
            hCprmCodeOwe2 = getValueInt("m_code_owe_2");
            hCprmCodeTtl3 = getValueInt("m_code_ttl_3");
            hCprmCodeOwe3 = getValueInt("m_code_owe_3");
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
    void selectActAcno() throws Exception {

        sqlCmd = "select ";
//        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "a.payment_rate1,";
        sqlCmd += "a.credit_act_no,";
        sqlCmd += "decode(a.no_delinquent_flag,'','N',a.no_delinquent_flag) h_acno_no_delinquent_flag,";
        sqlCmd += "decode(a.no_delinquent_s_date,'','30000101',a.no_delinquent_s_date) h_acno_no_delinquent_s_date,";
        sqlCmd += "decode(a.no_delinquent_e_date,'','30000101',a.no_delinquent_e_date) h_acno_no_delinquent_e_date,";
        sqlCmd += "a.pay_by_stage_flag,";
        sqlCmd += "b.acct_jrnl_bal,";
        sqlCmd += "a.last_pay_date,";
        sqlCmd += "a.int_rate_mcode ";
        sqlCmd += "from act_acct b,act_acno a ";
//        sqlCmd += "where a.p_seqno = a.gp_no ";
//        sqlCmd += "and a.p_seqno = b.p_seqno ";
        sqlCmd += "where a.acno_flag <> 'Y' ";
        sqlCmd += "and a.acno_p_seqno = b.p_seqno ";
        sqlCmd += "and decode(a.payment_rate1,'0A','00','0B','00','0C','00','0D','00','0E','00', ";
        sqlCmd += "'0F','00',a.payment_rate1) > '03' ";
        sqlCmd += "and b.acct_jrnl_bal > 0 ";
        sqlCmd += "and a.acct_status ='1' ";

        openCursor();
        while (fetchTable()) {
//            h_acno_p_seqno = getValue("p_seqno");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoPaymentRate1 = getValue("payment_rate1");
            hAcnoCreditActNo = getValue("credit_act_no");
            hAcnoNoDelinquentFlag = getValue("h_acno_no_delinquent_flag");
            hAcnoNoDelinquentSDate = getValue("h_acno_no_delinquent_s_date");
            hAcnoNoDelinquentEDate = getValue("h_acno_no_delinquent_e_date");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcctAcctJrnlBal = getValueInt("acct_jrnl_bal");
            hAcnoNoLastPayDate = getValue("last_pay_date");
            recLine++;
            idxLine++;

            if (idxLine >= 100000) {
                idxLine = 0;
                showLogMessage("I", "", String.format("Process record [%d]", recLine));
                dateTime();
            }

//            m_code = comr.getMcode(h_acno_acct_type, h_acno_p_seqno);
            mCode = getValueInt("int_rate_mcode");
            if ((mCode < hCprmCodeTtlS1) && (mCode < hCprmCodeOweS1))
                continue;

            selectActDebt1();

            if ((hAcnoPayByStageFlag.equals("00")) || (hAcnoPayByStageFlag.equals("NW"))) {
                insertColB001R1(2); /* 分期還款 */
                continue;
            }

            if ((hAcnoNoDelinquentFlag.substring(0, 1).equals("Y"))
                    && (hAcnoNoDelinquentSDate.compareTo(hBusiBusinessDate) <= 0)
                    && (hAcnoNoDelinquentEDate.compareTo(hBusiBusinessDate) >= 0)) {
                insertColB001R1(3); /* 暫不轉逾 */
                continue;
            }

            if ((mCode <= hCprmCodeTtlE1) && (mCode >= hCprmCodeTtlS1)) {
                if (hAcctAcctJrnlBal <= hCprmExcTtlLmt1) {
                    insertColB001R1(1); /* TTL&結欠本金 */
                    continue;
                }
            }

            if ((mCode <= hCprmCodeOweE1) && (mCode >= hCprmCodeOweS1)) {
                if (hDebtEndBal <= hCprmExcOweLmt1) {
                    insertColB001R1(1); /* TTL&結欠本金 */
                    continue;
                }
            }
            if (mCode >= hCprmCodeTtl1) {
                if (hAcctAcctJrnlBal <= hCprmExcTtlLmt2) {
                    insertColB001R1(1); /* TTL&結欠本金 */
                    continue;
                }
            }
            if (mCode >= hCprmCodeOwe1) {
                if (hDebtEndBal <= hCprmExcOweLmt2) {
                    insertColB001R1(1); /* TTL&結欠本金 */
                    continue;
                }
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActDebt1() throws Exception {
        hDebtEndBal = 0;

        sqlCmd = "select sum(end_bal) h_debt_end_bal ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where p_seqno = ?  ";
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
    }

    /***********************************************************************/
    void insertColB001R1(int inta) throws Exception {
        String tmp = "";

        if (mCode > 99) mCode = 99;
//        tmp = String.format("%02d", m_code);  //phopho 2020.6.1 Maintis 0003470
        tmp = String.format("%d", mCode);
        hCb01Mcode = tmp;
        if (hAcnoIdPSeqno.length() != 0) {
            selectCrdIdno();
        } else {
            selectCrdCorp();
        }
        //2020.5.14 phopho 增加【協商類別】【協商狀態】。
//        selectColNegoStatusCurr();
        getCpbdueCurrType();

        daoTable = "col_b001r1";
        extendField = daoTable + ".";
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"id_no", hIdnoId);
        setValue(extendField+"chi_name", hIdnoChiName);
        setValue(extendField+"mcode", hCb01Mcode);
        setValue(extendField+"payment_rate1", hAcnoPaymentRate1);
        setValueInt(extendField+"acct_jrnl_bal", hAcctAcctJrnlBal);
        setValueInt(extendField+"end_bal", hDebtEndBal);
        setValue(extendField+"acct_status", "1");
        setValue(extendField+"credit_act_no", hAcnoCreditActNo);
        //2020.5.14 phopho add
        setValue(extendField+"nego_type", comStr.left(hCpbdueCurrType, 1));
        setValue(extendField+"nego_status", comStr.right(hCpbdueCurrType, 1));

        String type = "";
        switch (inta) {
        case 1:
            type = "1";
            break;
        case 2:
            type = "2";
            break;
        case 3:
            type = "3";
            break;
        default:
            type = "4";
            break;
        }
        setValue(extendField+"err_type", type);
        setValue(extendField+"crt_date", hBusiBusinessDate);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"pay_by_stage_flag", hAcnoPayByStageFlag);
        setValue(extendField+"last_pay_date", hAcnoNoLastPayDate);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_b001r1 duplicate!", "", hCallBatchSeqno);
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
    
    
	/***
	 * 查詢各項協商主檔的狀態，取最後一筆異動日期
	 * @param infS009Data
	 * @throws Exception 
	 */
	void selectCpbdueCurrType() throws Exception {
		daoTable = "CPBDUE_CURR_TYPE";
		extendField = "currtype.";
		sqlCmd = "SELECT * FROM ( ";
		sqlCmd += " select CPBDUE_UPD_DTE,ID_CORP_NO,CPBDUE_ID_P_SEQNO ";
		sqlCmd += " ,CASE WHEN cpbdue_type<>'' AND CPBDUE_CURR_TYPE<>'' AND CPBDUE_CURR_TYPE<>'0' ";
		sqlCmd += " THEN decode(cpbdue_type,'1','1','2','5','3','7','')||decode(CPBDUE_CURR_TYPE,'0','',CPBDUE_CURR_TYPE) ";
		sqlCmd += " ELSE '' END AS CPBDUE_CURR_TYPE ";
		sqlCmd += " from col_cpbdue ";
		sqlCmd += " UNION ";
		sqlCmd += " SELECT a.APPLY_DATE,b.id_no,a.ID_P_SEQNO,'2'||liac_status AS liac_status ";
		sqlCmd += " FROM col_liac_nego a,crd_idno b ";
		sqlCmd += " WHERE a.ID_P_SEQNO=b.ID_P_SEQNO ";
		sqlCmd += " UNION ";
		sqlCmd += " SELECT STATUS_DATE,ID_NO,ID_P_SEQNO,LIAD_TYPE||decode(LIAD_STATUS,'A','1','B','2','C','3','D','4','E','5','F','6','G','7','H','8',LIAD_STATUS) AS LIAD_STATUS FROM ( ";
		sqlCmd += " SELECT ROW_NUMBER() OVER(PARTITION BY A.ID_P_SEQNO ORDER BY A.ID_P_SEQNO,A.STATUS_DATE DESC) AS ROWID, ";
		sqlCmd += " B.ID_NO,A.ID_P_SEQNO,A.LIAD_TYPE,A.LIAD_STATUS,A.STATUS_DATE ";
		sqlCmd += " FROM COL_LIAD_RENEWLIQUI A,CRD_IDNO B ";
		sqlCmd += " WHERE A.ID_P_SEQNO =B.ID_P_SEQNO ";
		sqlCmd += " ) WHERE ROWID='1' ";
		sqlCmd += " ) ";
		sqlCmd += " ORDER BY DECODE(LEFT(CPBDUE_CURR_TYPE,1),'7',1,2) ,CPBDUE_UPD_DTE DESC ";
    	int n = loadTable();
  	    setLoadData("currtype.ID_CORP_NO");
		showLogMessage("I", "", "selectCpbdueCurrType已取得[" + n +"]筆");
	}
	
	/**
	 * @throws Exception *********************************************************************/
	void getCpbdueCurrType() throws Exception {
    	setValue("currtype.ID_CORP_NO",hIdnoId);
		getLoadData("currtype.ID_CORP_NO");
		
		hCpbdueCurrType = getValue("currtype.CPBDUE_CURR_TYPE");
	}
	

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB023 proc = new ColB023();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

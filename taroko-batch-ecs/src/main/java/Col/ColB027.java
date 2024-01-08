/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/03/29  V1.00.00    phopho     program initial                          *
*  108/12/19  V1.00.01    phopho     change table: prt_branch -> gen_brn      *
*  109/03/19  V1.00.02    phopho     問題單:0002944 col_批次修改需求說明 V3.9   *
*  109/12/14  V1.00.03    shiyuqi       updated for project coding standard   *
*  112/06/17  V1.00.04    sunny      修正最後繳款日期欄位名稱                                             *
*  112/08/25  V1.00.05    sunny      排除商務卡，商務卡使用colm0095功能                          *
*  112/10/04  V1.00.06    sunny      增加debug處理，先mark不需要邏輯                             *
*  112/12/05  V1.00.07    sunny      與colm0100功能同步修改，執行日期依營業日判斷(原為系統日)*
******************************************************************************/

package Col;

import com.*;

public class ColB027 extends AccessDAO {
	public final boolean debug = false;
    private String progname = "催收資料彙整倒檔作業   112/12/05  V1.00.07 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    int hParmCnt = 0;
    String hUserId = "";
    String hParmDate = "";
    String hParmStatus = "";
    
    String hBusiBusinessDate = "";
    
    String hAcnoCardIndicator = "";
    String hAcnoPSeqno = "";
    String hAcnoIdPSeqno = "";
    String hIdnoChiName = "";
    String hAcnoAcctType = "";
    String hAcnoStmtCycle = "";
    String hAcnoIntRateMcode = "";
    double hDebtEndBalCb = 0;
    double hDebtEndBalCi = 0;
    double hDebtEndBalCc = 0;
    double hDebtTotalAmt = 0;
    double hDebtIdTotalAmt = 0;
    double hAcctAcctJrnlBal = 0;
    double hAcctAcctJrnlBalSum = 0;
    String hAcctLastPaymentDate = "";
    String hIdnoCardSince = "";
    double hAcnoLineOfCreditAmt = 0;
    String hCrdRelaFlag = "";
    String hCardSupFlag = "";
    String hAcnoLastPayDate = "";
    String hAcnoOrgDelinquentDate = "";
    String hCbdtTransDate = "";
    String hAcnoRiskBankNo = "";
    String hCurrNegoType = "";
    String hCurrNegoStatus = "";
    String hCurrApplyNegoMcode = "";
    String hAcnoPayByStageFlag = "";
    int    totalCnt = 0;
    
    public int mainProcess(String[] args) {
        try {
        	dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            
            // 檢查參數
//            if (args.length != 1 && args.length != 2) {
//                comc.errExit("Usage : ColB027 user_id [batch_seqno]", "");
//            }
                                 
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            //到底要不要這段?
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(0, 0, 0);

            if(args.length > 0)
            hUserId = args[0];
            
            if (hUserId.equals("")) hUserId = javaProgram;
            //hUserId = args.length > 0 ? args[args.length - 1] : "";
            
//            1.	檢查ptr_sys_parm 資料，查詢條件:wf_parm = ‘COL_PARM’AND wf_key = ‘COLB027_PARM’。
//              a.	若有資料，檢查【wf_value】欄位，若此欄位值為【系統日】，表示今日已經執行此批次，
//              	停止此批次，並LOG，內容：【col_b027 催收資料彙整倒檔作業，每日僅能執行一次。】
//              b.	若有資料，且【wf_value】非為系統日，表示可以繼續執行批次。
//              c.	若無資料，表示可以繼續執行批次，insert一筆，ptr_sys_parm，設定:wf_parm = ‘COL_PARM’AND wf_key = ‘COLB027_PARM’。
//              d.	可以執行之情況，【wf_value】設定為【系統日】，【wf_value2】設定為【DOING】。
//              e.	承上述，若為線上啟動，CRT_USER、MOD_USER為online 功能傳入之USER參數，其餘CRT_XXX相關欄位，MOD_XXX相關欄位，依據系統規定。
//            2.	刪除TABLE中所有資料。TABLE：COL_BAD_TRANS_TEMP 催收加速轉呆彙整資料暫存檔。
//            3.	Insert TABLE。TABLE：COL_BAD_TRANS_TEMP 催收加速轉呆彙整資料暫存檔。
//            4.	參考SQL，【rpt_c001 催收加速轉呆帳文件準備 程式】之【select_crd_idno()】。內容:
//            5.	完成批次時，ptr_sys_parm之【wf_value2】設定為【FINISHED】。
//            6.	如果執行失敗，rollback，update ptr_sys_parm之【wf_value】、【wf_value2】設定為【空字串】。

            //1.
/*sunny 先mark
            hParmCnt = selectPtrSysParm();
            if (hParmCnt == 0) {  //c.
            	insertPtrSysParm();
            } else {  //a.
            	if (hParmDate.equals(sysDate)) {
//            		exceptExit = 0;
//            		comcr.errRtn("ColB027 催收資料彙整倒檔作業，每日僅能執行一次。", "", hCallBatchSeqno);
            	    showLogMessage("E", "", "ColB027 催收資料彙整倒檔作業，每日僅能執行一次");
            	    return (0);
            	}           	
            }
*/         
            //d.
//            hParmStatus = "DOING";
//            updatePtrSysParm();
            
            //1.
            selectPtrBusinday();
            
            //2.
            deleteColBadTransTemp();
            
            //3.
            selectCrdIdno();
            
            //5.
            hParmStatus = "FINISHED";
            updatePtrSysParm();
            

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,累計筆數 : [" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
            	comcr.callbatch(1, 0, 0);
            
//            showLogMessage("I", "", "程式執行結束");
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
        sqlCmd += "from ptr_businday where 1=1 ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            showLogMessage("I", "", "營業日期 = " + hBusiBusinessDate );
         }
    }
    /***********************************************************************/
    int selectPtrSysParm() throws Exception {
    	sqlCmd = "select wf_value, wf_value2  from ptr_sys_parm ";
        sqlCmd += "where wf_parm = 'COL_PARM' and wf_key = 'COLB027_PARM' ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hParmDate = getValue("wf_value");
            hParmStatus = getValue("wf_value2");
        }
    	
        return recordCnt;
    }
    
    /***********************************************************************/
    void insertPtrSysParm() throws Exception {
        dateTime();
        daoTable = "ptr_sys_parm";
        extendField = daoTable + ".";
        setValue(extendField+"wf_parm", "COL_PARM");
        setValue(extendField+"wf_key", "COLB027_PARM");
        setValue(extendField+"wf_desc", "催收加速轉呆彙整倒檔批次作業控制參數");
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_user", hUserId);
        setValue(extendField+"mod_user", hUserId);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
			clearPtrSysParm();
            comcr.errRtn("insert_ptr_sys_parm duplicate!", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    void updatePtrSysParm() throws Exception {

        daoTable = "ptr_sys_parm";
        updateSQL = " wf_value   = ?,";
        updateSQL += " wf_value2 = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_seqno = nvl(mod_seqno,0)+1 ";
        whereStr = "where wf_parm = 'COL_PARM' and wf_key = 'COLB027_PARM' ";
        //setString(1, sysDate);
        setString(1, hBusiBusinessDate); //20231205 改為營業日
        setString(2, hParmStatus);
        setString(3, hUserId);
        setString(4, javaProgram);

        updateTable();
        if (notFound.equals("Y")) {
        	clearPtrSysParm();
            comcr.errRtn("update_ptr_sys_parm not found!", "", hCallBatchSeqno);
        }
        commitDataBase();
    }
    
    /***********************************************************************/
    void clearPtrSysParm() throws Exception {
    	//6.
    	rollbackDataBase();
    	
        daoTable = "ptr_sys_parm";
        updateSQL = " wf_value   = '',";
        updateSQL += " wf_value2 = '',";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = '',";
        updateSQL += " mod_pgm   = '' ";
        whereStr = "where wf_parm = 'COL_PARM' and wf_key = 'COLB027_PARM' ";

        updateTable();
        commitDataBase();
    }
    
    /***********************************************************************/
    void deleteColBadTransTemp() throws Exception {
        daoTable = "col_bad_trans_temp";
        deleteTable();
    }
    
    /***********************************************************************/
    void selectCrdIdno() throws Exception {
    	sqlCmd = " select ";
    	sqlCmd += " a.card_indicator,";
//    	sqlCmd += " a.p_seqno, ";
    	sqlCmd += " a.acno_p_seqno, ";
		sqlCmd += " a.id_p_seqno, ";
		sqlCmd += " b.chi_name, ";
		sqlCmd += " a.acct_type, ";
		sqlCmd += " a.stmt_cycle, ";
		sqlCmd += " a.int_rate_mcode, ";
		sqlCmd += " b.card_since, ";
		sqlCmd += " a.line_of_credit_amt, ";
		sqlCmd += " a.last_pay_date, ";
		sqlCmd += " a.org_delinquent_date, ";
//		sqlCmd += " decode(a.risk_bank_no,'109','','009','',a.risk_bank_no) risk_bank_no, ";
		sqlCmd += " a.risk_bank_no,";
		sqlCmd += " a.pay_by_stage_flag ";
//		sqlCmd += "   from act_acno a, crd_idno b, gen_brn c ";  //2019.12.19 change table name
		sqlCmd += "   from act_acno a, crd_idno b";
//		sqlCmd += " left join gen_brn c on a.risk_bank_no = c.branch ";
		sqlCmd += "  where a.id_p_seqno = b.id_p_seqno ";
//		sqlCmd += "    and a.risk_bank_no = c.branch ";
		sqlCmd += "    and a.acct_status = '3' ";
		sqlCmd += "    and a.card_indicator = '1' ";
	
		/* 20230825 sunny 先排除商務卡 
		sqlCmd += " union ";
		sqlCmd += " select ";
		sqlCmd += " a.card_indicator,";
//		sqlCmd += " a.p_seqno, ";
		sqlCmd += " a.acno_p_seqno, ";
		sqlCmd += " a.corp_p_seqno, ";
		sqlCmd += " b.chi_name, ";
		sqlCmd += " a.acct_type, ";
		sqlCmd += " a.stmt_cycle, ";
		sqlCmd += " a.int_rate_mcode, ";
		sqlCmd += " b.card_since, ";
		sqlCmd += " a.line_of_credit_amt, ";
		sqlCmd += " a.last_pay_date, ";
		sqlCmd += " a.org_delinquent_date, ";
//		sqlCmd += " decode(a.risk_bank_no,'109','','009','',a.risk_bank_no) risk_bank_no, ";
		sqlCmd += " a.risk_bank_no,";
		sqlCmd += " a.pay_by_stage_flag ";
//		sqlCmd += "   from act_acno a, crd_corp b, gen_brn c ";  //2019.12.19 change table name
		sqlCmd += "   from act_acno a, crd_corp b";
		sqlCmd += "  where a.corp_p_seqno = b.corp_p_seqno ";
//		sqlCmd += " left join gen_brn c on a.risk_bank_no = c.branch ";
//		sqlCmd += "    and a.risk_bank_no = c.branch ";
		sqlCmd += "    and a.acct_status = '3' ";
		sqlCmd += "    and a.card_indicator = '2' ";
		*/
		
		openCursor();
        while (fetchTable()) {
        	hAcnoCardIndicator = getValue("card_indicator");
//        	h_acno_p_seqno = getValue("p_seqno");
        	hAcnoPSeqno = getValue("acno_p_seqno");
        	hAcnoIdPSeqno = getValue("id_p_seqno");
        	hIdnoChiName = getValue("chi_name");
        	hAcnoAcctType = getValue("acct_type");
        	hAcnoStmtCycle = getValue("stmt_cycle");
        	hAcnoIntRateMcode = getValue("int_rate_mcode");
        	hIdnoCardSince = getValue("card_since");
        	hAcnoLineOfCreditAmt = getValueDouble("line_of_credit_amt");
        	hAcnoLastPayDate = getValue("last_pay_date");
        	hAcnoOrgDelinquentDate = getValue("org_delinquent_date");
        	hAcnoRiskBankNo = getValue("risk_bank_no");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            
            totalCnt++;
            if (totalCnt % 300 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
            }
            
//          phopho mod 2020.3.19 問題單:0002944 col_批次修改需求說明 V3.9
//          1.	調整【b027 催收資料彙整倒檔作業】功能邏輯。重新檢視TABLE，TOTAL_AMT、ID_TOTAL_AMT、
//              ACCT_JRNL_BAL、ACCT_JRNL_BAL_SUM、LAST_PAYMENT_DATE等欄位的名稱、邏輯，重新檢視與調整。
//          2.	修改getColBadData 名稱為 getActBadData。
//            getColBadData();
            getActBadData();
            if (hDebtTotalAmt == 0)  continue;
            
            getActAcctBal();  //phopho mod
            if(debug)
            showLogMessage("W", "", "01-p_seqno["+hAcnoPSeqno+"],AcctLastPaymentDate["+hAcctLastPaymentDate+"]");
            hasCrdRela();
            hasCardSupFlag();
            getTransDate();
            //getNegoStatus(); //TCB無此處理

            insertColBadTransTemp();
    	
        }
        closeCursor();
    }
    
    /***********************************************************************/
    void getActBadData() throws Exception {
    	hDebtEndBalCb = 0;
        hDebtEndBalCi = 0;
        hDebtEndBalCc = 0;
        hDebtTotalAmt = 0;
        hDebtIdTotalAmt = 0;
        
        sqlCmd = "select sum(end_bal) as end_bal_cb from act_debt WHERE p_seqno = ? and acct_code='CB'";
        setString(1, hAcnoPSeqno);
        extendField = "act_debt_1.";
        if (selectTable() > 0) {
        	hDebtEndBalCb = getValueDouble("act_debt_1.end_bal_cb");
        }
        
        sqlCmd = "select sum(end_bal) as end_bal_ci from act_debt WHERE p_seqno = ? and acct_code='CI'";
        setString(1, hAcnoPSeqno);
        extendField = "act_debt_2.";
        if (selectTable() > 0) {
        	hDebtEndBalCi = getValueDouble("act_debt_2.end_bal_ci");
        }
        
        sqlCmd = "select sum(end_bal) as end_bal_cc from act_debt WHERE p_seqno = ? and acct_code='CC'";
        setString(1, hAcnoPSeqno);
        extendField = "act_debt_3.";
        if (selectTable() > 0) {
        	hDebtEndBalCc = getValueDouble("act_debt_3.end_bal_cc");
        }
        
        hDebtTotalAmt = hDebtEndBalCb + hDebtEndBalCi + hDebtEndBalCc;
        
//      phopho mod 2020.3.19
        if (hAcnoCardIndicator.equals("1")) {
            sqlCmd = "select sum(end_bal) tot_end_bal ";
            sqlCmd += "  from act_debt  ";
            sqlCmd += " where acct_code in ('CB','CI','CC')  ";
            sqlCmd += "   and p_seqno   in (select acno_p_seqno from act_acno where id_p_seqno= ?) ";
            setString(1, hAcnoIdPSeqno);
            if (selectTable() > 0) {
            	hDebtIdTotalAmt = getValueDouble("tot_end_bal");
            }
        } else {
            sqlCmd = "select sum(end_bal) tot_end_bal ";
            sqlCmd += "  from act_debt  ";
            sqlCmd += " where acct_code in ('CB','CI','CC')  ";
            sqlCmd += "   and p_seqno   in (select acno_p_seqno from act_acno where corp_p_seqno= ?) ";
            setString(1, hAcnoIdPSeqno);
            if (selectTable() > 0) {
            	hDebtIdTotalAmt = getValueDouble("tot_end_bal");
            }
        }
    }
    
    /***********************************************************************/
    void getActAcctBal() throws Exception {
    	hAcctAcctJrnlBal = 0;
        hAcctAcctJrnlBalSum = 0;
        
//        sqlCmd = "select acct_jrnl_bal from act_acct WHERE p_seqno = ? ";  //phopho mod 2020.3.19
        sqlCmd = "select acct_jrnl_bal, last_payment_date from act_acct WHERE p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        extendField = "act_acct_1.";
        if (selectTable() > 0) {
        	hAcctAcctJrnlBal = getValueDouble("act_acct_1.acct_jrnl_bal");
        	hAcctLastPaymentDate = getValue("act_acct_1.last_payment_date");        	 
        }
        
        sqlCmd = "select sum(acct_jrnl_bal) as acct_jrnl_bal_sum from act_acct WHERE id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        extendField = "act_acct_2.";
        if (selectTable() > 0) {
        	hAcctAcctJrnlBalSum = getValueDouble("act_acct_2.acct_jrnl_bal_sum");
        }
    }
    
    /***********************************************************************/
    void hasCrdRela() throws Exception {
    	hCrdRelaFlag = "";
        
        sqlCmd = "SELECT 1 as h_cnt from crd_rela where rela_type = '1' and id_p_seqno = ? ";
        sqlCmd += "and (rela_name <> ''  OR rela_id <> '') fetch first 1 row only ";
        setString(1, hAcnoIdPSeqno);
        if (selectTable() > 0) {
        	hCrdRelaFlag = "Y";
        } else {
        	hCrdRelaFlag = "N";
        }
        
//        if (notFound.equals("Y")) {
//            comcr.err_rtn("select_ptr_businday not found!", "", h_call_batch_seqno);
//        }
    }
    
    /***********************************************************************/
    void hasCardSupFlag() throws Exception {
    	hCardSupFlag = "";
        
        sqlCmd = "SELECT 1 as h_cnt from crd_card where major_id_p_seqno = ? ";
        sqlCmd += "and id_p_seqno != major_id_p_seqno fetch first 1 row only ";
        setString(1, hAcnoIdPSeqno);
        if (selectTable() > 0) {
        	hCardSupFlag = "Y";
        } else {
        	hCardSupFlag = "N";
        }
    }
    
    /***********************************************************************/
    void getTransDate() throws Exception {
    	hCbdtTransDate = "";
        
        sqlCmd = "SELECT MAX(trans_date) as trans_date from col_bad_debt ";
        sqlCmd += "where p_seqno = ? and trans_type = '3' ";
        
        extendField = "col_bad_debt.";
        
        setString(1, hAcnoPSeqno);
        if (selectTable() > 0) {
        	hCbdtTransDate = getValue("col_bad_debt.trans_date");
        }
    }

    /***********************************************************************/
    void getNegoStatus() throws Exception {
    	hCurrNegoType = "";
        hCurrNegoStatus = "";
        hCurrApplyNegoMcode = "";
        
        sqlCmd = "select nego_type, nego_status, apply_nego_mcode ";
        sqlCmd += "from col_nego_status_curr WHERE id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        
        extendField = "col_nego_status_curr.";
        
        if (selectTable() > 0) {
        	hCurrNegoType = getValue("col_nego_status_curr.nego_type");
        	hCurrNegoStatus = getValue("col_nego_status_curr.nego_status");
        	hCurrApplyNegoMcode = getValue("col_nego_status_curr.apply_nego_mcode");
        }
    }
    
    /***********************************************************************/
    void insertColBadTransTemp() throws Exception {
        dateTime();
        daoTable = "col_bad_trans_temp";
        extendField = daoTable + ".";
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"chi_name", hIdnoChiName);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"mcode", hAcnoIntRateMcode);
        setValueDouble(extendField+"end_bal_cb", hDebtEndBalCb);
        setValueDouble(extendField+"end_bal_ci", hDebtEndBalCi);
        setValueDouble(extendField+"end_bal_cc", hDebtEndBalCc);
        setValueDouble(extendField+"total_amt", hDebtTotalAmt);
        setValueDouble(extendField+"id_total_amt", hDebtIdTotalAmt);
        setValueDouble(extendField+"acct_jrnl_bal", hAcctAcctJrnlBal);
        setValueDouble(extendField+"acct_jrnl_bal_sum", hAcctAcctJrnlBalSum);
//        phopho mod 2020.3.19 問題單:0002944 col_批次修改需求說明 V3.9
//        setValueDouble(extendField+"total_amt", h_acct_acct_jrnl_bal);
//        setValueDouble(extendField+"id_total_amt", h_acct_acct_jrnl_bal_sum);
        setValue(extendField+"card_since", hIdnoCardSince);
        setValueDouble(extendField+"line_of_credit_amt", hAcnoLineOfCreditAmt);
        setValue(extendField+"crd_rela_flag", hCrdRelaFlag);
        setValue(extendField+"card_sup_flag", hCardSupFlag);
//        setValue(extendField+"last_pay_date", h_acno_last_pay_date);  //phopho mod 2020.3.19 問題單:0002944 col_批次修改需求說明 V3.9
        setValue(extendField+"last_pay_date", hAcctLastPaymentDate);
//        showLogMessage("W", "", "02-p_seqno["+hAcnoPSeqno+"],AcctLastPaymentDate["+hAcctLastPaymentDate+"]");
        setValue(extendField+"org_delinquent_date", hAcnoOrgDelinquentDate);
        setValue(extendField+"trans_date", hCbdtTransDate);
        setValue(extendField+"risk_bank_no", hAcnoRiskBankNo);
        setValue(extendField+"nego_type", hCurrNegoType);
        setValue(extendField+"nego_status", hCurrNegoStatus);
        setValue(extendField+"apply_nego_mcode", hCurrApplyNegoMcode);
        setValue(extendField+"pay_by_stage_flag", hAcnoPayByStageFlag);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"crt_user", hUserId);
        setValue(extendField+"mod_user", hUserId);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
        	clearPtrSysParm();
            comcr.errRtn("insert_col_bad_trans_temp duplicate!", "", hCallBatchSeqno);
        }
    }
    
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB027 proc = new ColB027();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/21  V1.00.00    phopho     program initial                          *
*  109/12/14  V1.00.01    shiyuqi       updated for project coding standard   *
*  111/12/30  V1.00.02    Ryan       增加col_cs_info處理邏輯                                          *
*  112/03/01  V1.00.03    Ryan       insertColCsBase() 移至處理col_cs_info之前做   *
*  112/09/26  V1.00.05    Sunny      移除只要是協商狀態都要處理的條件(M0無帳齡的協商才納入) *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC009 extends AccessDAO {
    private String progname = "傳送CS(M1)C-cycle挑選符合資料處理程式 112/09/25 V1.00.05 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommCol comCol = null;
    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hWdayStmtCycle = "";
    String hWdayThisAcctMonth = "";
    int hCprmGenCsDay = 0;
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctStatus = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoCorpActFlag = "";
    String hAcnoPaymentRate1 = "";
    String hAcnoPaymentRate2 = "";
    String hAcnoCardIndicator = "";
    String hAcnoPayByStageFlag = "";
    String hIdnoSalaryHoldinFlag = "";
    String hCloseFlag = "";
    String hDebtCurrCode = "";
    String hDebtCardNo = "";
    String hRegBankNo = "";
    String hIssueBankNo = "";
    int hTempMcode = 0;
    int hTempMcode0 = 0;
    int hTempMcode1 = 0;
    int hAcsuBilledEndBal = 0;
    String hCcbsIdPSeqno = "";
    String hCcbsAcctType = "";
    String hCcbsRowid = "";
    int hCnt = 0;

    int totalCnt = 0;
    int data1Cnt = 0;
    int data2Cnt = 0;
    int data3Cnt = 0;

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
                comc.errExit("Usage : ColC009 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comCol = new CommCol(getDBconnect(), getDBalias());
            
            hBusiBusinessDate = "";
            if (args.length == 1)
                hBusiBusinessDate = args[0];
            selectPtrBusinday();
            selectColParam();

            if (selectPtrWorkday() != 0) {
            	exceptExit = 0;
                comcr.errRtn(String.format("本日[%s]+[%d]不需執行", hBusiBusinessDate, hCprmGenCsDay), "", hCallBatchSeqno);
            }

            showLogMessage("I", "", String.format("本日[%s]執行[%s] cycle", hBusiBusinessDate, hWdayStmtCycle));
            showLogMessage("I", "", String.format("========================================="));
            showLogMessage("I", "", String.format("刪除當期  col_cs_base"));
            deleteColCsBase();
            showLogMessage("I", "", String.format("Total delete record[%d]", totalCnt));
            showLogMessage("I", "", String.format("-----------------------------------------"));
            showLogMessage("I", "", String.format("處理當期符合送 CS 之資料"));
            totalCnt = 0;
            selectActAcno();
            showLogMessage("I", "", String.format("累計處理筆數 [%d] 轉入筆數[%d]", totalCnt, data1Cnt + data2Cnt + data3Cnt));
            showLogMessage("I", "", String.format("    分期扣薪註記 [%d]", data1Cnt));
            showLogMessage("I", "", String.format("    催呆戶       [%d]", data2Cnt));
            showLogMessage("I", "", String.format("    逾放戶       [%d]", data3Cnt));
            showLogMessage("I", "", String.format("    不轉CS       [%d]", totalCnt - data1Cnt - data2Cnt - data3Cnt));
            showLogMessage("I", "", String.format("-----------------------------------------"));
            showLogMessage("I", "", String.format("刪除當期重複變更之ID"));
            totalCnt = 0;
            data1Cnt = 0;
            selectColCsBase();
            showLogMessage("I", "", String.format("Total Process[%d] delete[%d] record", totalCnt, data1Cnt));
            showLogMessage("I", "", String.format("========================================="));

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
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date ";
        sqlCmd += " from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
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
        sqlCmd = "select stmt_cycle,";
        sqlCmd += "this_acct_month ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where to_date(this_close_date,'yyyymmdd') + ? days = to_date(?,'yyyymmdd') -1 days ";
        setInt(1, hCprmGenCsDay);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayThisAcctMonth = getValue("this_acct_month");
        }

        return recordCnt > 0 ? 0 : 1;
    }

    /***********************************************************************/
    void selectColParam() throws Exception {
        hCprmGenCsDay = 0;
        sqlCmd = "select gen_cs_day ";
        sqlCmd += " from col_param ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_param not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCprmGenCsDay = getValueInt("gen_cs_day");
        }

    }

    /***********************************************************************/
    void deleteColCsBase() throws Exception {
        daoTable = "col_cs_base a";
        whereStr = "where stmt_cycle = ? ";
        setString(1, hWdayStmtCycle);
        totalCnt = deleteTable();
        if (notFound.equals("Y")) {
            //comcr.errRtn("delete_col_cs_base a not found!", "", hCallBatchSeqno);
            showLogMessage("I", "", String.format("delete_col_cs_base a not found! cycle[%s]",hWdayStmtCycle));
        }
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {

        sqlCmd = "select ";
//        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "a.corp_act_flag,";
        sqlCmd += "a.payment_rate1,";
        sqlCmd += "a.payment_rate2,";
        sqlCmd += "a.card_indicator,";
        sqlCmd += "a.pay_by_stage_flag,";
        sqlCmd += "'N' h_idno_salary_holdin_flag ";
        sqlCmd += "from act_acno a ";
        sqlCmd += "where a.sale_date = '' ";
        sqlCmd += "and a.id_p_seqno = '' ";
        sqlCmd += "and a.acno_flag <> 'Y' ";
        sqlCmd += "and a.stmt_cycle = ? ";
        sqlCmd += "and (a.acct_status in ('3','4') ";
//        sqlCmd += "or a.pay_by_stage_flag <> '' "; //TCB協商戶若繳足最低則不出現
        sqlCmd += "or (a.acct_status in ('1','2') ";
        sqlCmd += "and decode(a.payment_rate1,'','0A',a.payment_rate1) not in ('0A','0B','0C','0D','0E'))) ";
        sqlCmd += " UNION ";
//        sqlCmd += "select a.p_seqno, ";
        sqlCmd += "select a.acno_p_seqno, ";
        sqlCmd += "a.acct_type, ";
        sqlCmd += "a.acct_status, ";
        sqlCmd += "a.id_p_seqno, ";
        sqlCmd += "a.corp_p_seqno, ";
        sqlCmd += "a.corp_act_flag, ";
        sqlCmd += "a.payment_rate1, ";
        sqlCmd += "a.payment_rate2, ";
        sqlCmd += "a.card_indicator, ";
        sqlCmd += "a.pay_by_stage_flag, ";
        sqlCmd += "decode(b.salary_holdin_flag,'','N',b.salary_holdin_flag) ";
        sqlCmd += "from act_acno a, crd_idno b ";
        sqlCmd += "where a.sale_date = '' ";
        sqlCmd += "and a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += "and a.acno_flag <> 'Y' ";
        sqlCmd += "and a.stmt_cycle = ? ";
        sqlCmd += "and (a.acct_status in ('3','4') ";
// 	    sqlCmd += "or a.pay_by_stage_flag <> '' "; //TCB協商戶若繳足最低則不出現
 	  //  sqlCmd += "or (decode(b.salary_holdin_flag,'','N',b.salary_holdin_flag)='Y') "; //TCB取消
        sqlCmd += "or (a.acct_status in ('1','2') ";
        sqlCmd += "and decode(a.payment_rate1,'','0A',a.payment_rate1) not in ('0A','0B','0C','0D','0E'))) ";
        setString(1, hWdayStmtCycle);
        setString(2, hWdayStmtCycle);

        openCursor();
        while (fetchTable()) {
//            h_acno_p_seqno = getValue("p_seqno");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoCorpActFlag = getValue("corp_act_flag");
            hAcnoPaymentRate1 = getValue("payment_rate1");
            hAcnoPaymentRate2 = getValue("payment_rate2");
            hAcnoCardIndicator = getValue("card_indicator");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hIdnoSalaryHoldinFlag = getValue("h_idno_salary_holdin_flag");
            hTempMcode = comcr.str2int(hAcnoPaymentRate1);

            totalCnt++;
            if ((totalCnt % 5000)==0)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            if ((hAcnoPayByStageFlag.length() == 0) && (hIdnoSalaryHoldinFlag.equals("N"))) {
                if (selectActAcctSum() == 0)
                    continue;
                hTempMcode = comcr.str2int(hAcnoPaymentRate1);
                hTempMcode0 = hTempMcode;

                if (hAcnoAcctStatus.toCharArray()[0] < '3') {
                    if (hTempMcode0 <= 0)
                        continue;
                    data3Cnt++;
                } else {
                    if ((hAcnoPaymentRate1.equals("0C")) || (hAcnoPaymentRate1.equals("0D")))
                        hTempMcode0 = 1;
                    hTempMcode1 = comcr.str2int(hAcnoPaymentRate2);
                    if ((hTempMcode0 <= 0) && (hTempMcode == hTempMcode1))
                        continue;
                    data2Cnt++;
                }
            } else {
                data1Cnt++;
            }
            
            insertColCsBase();
            
            String[] debtArray = comCol.selectMaxCardDebtAmt(hAcnoPSeqno);
            hDebtCurrCode = debtArray[0];
            hDebtCardNo = debtArray[1];
            
            hRegBankNo = comCol.selectCardRiskBankNo(hDebtCardNo);
            hIssueBankNo = comCol.selectCardIssueRegBankNo(hDebtCardNo);
            if(selectColCsInfo() == 0) {
            	if(!hCloseFlag.equals("Y")) //理論上是非M0的案件 即維持現狀，跳過不處理，繼續處理下一筆
            		continue;
            	updateColCsInfo();
            }else {
            	insertColCsInfo();
            }
            updateAcnoRegBankNo();

        }
        closeCursor();
    }

    /***********************************************************************/
    int selectActAcctSum() throws Exception {
        hAcsuBilledEndBal = 0;
        sqlCmd = "select sum(billed_end_bal+unbill_end_bal) h_acsu_billed_end_bal ";
        sqlCmd += " from act_acct_sum  ";
        sqlCmd += "where acct_code != 'AF'  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_h_acno_p_seqno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcsuBilledEndBal = getValueInt("h_acsu_billed_end_bal");
        }

        return hAcsuBilledEndBal == 0 ? 0 : 1;
    }
    
    int selectColCsInfo() throws Exception {
    	hCloseFlag = "";
    	sqlCmd = "select close_flag from col_cs_info where p_seqno = ? and curr_code = ? ";
    	setString(1,hAcnoPSeqno);
    	setString(2,hDebtCurrCode);
    	int recordCnt = selectTable();
    	if(recordCnt > 0) {
    		hCloseFlag =  getValue("close_flag");
    		return 0; 
    	}
    	return 1;
    }

    /***********************************************************************/
    void insertColCsBase() throws Exception {
    	String lsPayByStageFlag = "";
    	
    	lsPayByStageFlag =  hAcnoPayByStageFlag.equals("")? "N" : "Y";
        setValue("p_seqno", hAcnoPSeqno);
        setValue("acct_type", hAcnoAcctType);
        setValue("id_p_seqno", hAcnoIdPSeqno);
        setValue("corp_p_seqno", hAcnoCorpPSeqno);
        setValue("stmt_cycle", hWdayStmtCycle);
        setValue("corp_act_flag", hAcnoCorpActFlag);
        setValue("card_indicator", hAcnoCardIndicator);
        setValueInt("mcode", hTempMcode);
        setValue("stage_flag", lsPayByStageFlag);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "col_cs_base";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_cs_base duplicate!", "", hCallBatchSeqno);
        }

    }
    
    void insertColCsInfo() throws Exception {
		setValue("p_seqno", hAcnoPSeqno);
		setValue("corp_p_seqno", !hAcnoAcctType.equals("01")?hAcnoCorpPSeqno:"");
		setValue("id_p_seqno", hAcnoIdPSeqno);
		setValue("curr_code", hDebtCurrCode);
		setValue("card_no", hDebtCardNo);
		setValue("reg_bank_no", hRegBankNo);
		setValue("issue_bank_no", hIssueBankNo);
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "col_cs_info";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insertColCsInfo duplicate!", "", hCallBatchSeqno);
		}
    }
    
    void updateColCsInfo() throws Exception {
        daoTable = "col_cs_info";
        updateSQL = "card_no = ?, ";
        updateSQL += " reg_bank_no = ?, ";
        updateSQL += " issue_bank_no = ?, ";
        updateSQL += " close_flag = ?, ";
        updateSQL += " close_date = '', ";
        updateSQL += " mod_time = sysdate, ";
        updateSQL += " mod_pgm = ? ";
        whereStr = "where p_seqno = ? and curr_code = ? ";
        setString(1, hDebtCardNo);
        setString(2, hRegBankNo);
        setString(3, hIssueBankNo);
        setString(4, "N");
        setString(5, javaProgram);
        setString(6, hAcnoPSeqno);
        setString(7, hDebtCurrCode);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("updateColCsInfo not found!", "", hCallBatchSeqno);
        }
    }
    
    void updateAcnoRegBankNo() throws Exception {
    	  daoTable = "act_acno";
          updateSQL = " reg_bank_no = ? ";
          whereStr = "where p_seqno = ? ";
          setString(1, hRegBankNo);
          setString(2, hAcnoPSeqno);
          updateTable();
          if (notFound.equals("Y")) {
              comcr.errRtn("updateAcnoRegBankNo not found!", "", hCallBatchSeqno);
          }
    }

    /***********************************************************************/
    void selectColCsBase() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "b.id_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from col_cs_base a,crd_chg_id b ";
        sqlCmd += "where a.id_p_seqno = b.old_id_p_seqno ";
        sqlCmd += "and a.stmt_cycle = ? ";
        setString(1, hWdayStmtCycle);

        openCursor();
        while (fetchTable()) {
            hCcbsIdPSeqno = getValue("id_p_seqno");
            hCcbsAcctType = getValue("acct_type");
            hCcbsRowid = getValue("rowid");

            totalCnt++;
            if ((totalCnt % 5000)==0)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            if (selectColCsBase1() != 0)
                continue;
            deleteColCsBase1();
        }
        closeCursor();
    }

    /***********************************************************************/
    int selectColCsBase1() throws Exception {
        int hCnt = 0;
        sqlCmd = "select 1 h_cnt ";
        sqlCmd += " from col_cs_base  ";
        sqlCmd += "where acct_type = ?  ";
        sqlCmd += "and id_p_seqno = ? ";
        setString(1, hCcbsAcctType);
        setString(2, hCcbsIdPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }

        return recordCnt == 0 ? 1 : 0;
    }

    /***********************************************************************/
    void deleteColCsBase1() throws Exception {
        data1Cnt++;
        daoTable = "col_cs_base";
        whereStr = "where rowid = ? ";
        setRowId(1, hCcbsRowid);
        deleteTable();

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC009 proc = new ColC009();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

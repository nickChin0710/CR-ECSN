/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/04/06  V1.00.00    phopho     program initial                          *
*  109/12/12  V1.00.01    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.*;

public class ColB028 extends AccessDAO {
    private String progname = "更生清算繳款狀況資料彙整作業 109/12/12  V1.00.01 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    int hParmCnt = 0;
    String hUserId = "";
    String hParmDate = "";
    String hParmStatus = "";

    String hLiadLiadType = "";
    String hLiadIdPSeqno = "";
    String hLiadIdNo = "";
    String hLiadCaseLetter = "";
    String hLiadRecvDate = "";
    String hLiadChiName = "";
    double hLiadOrgDebtAmt = 0;
    double hLiadAllocateAmt = 0;
    String hLiadLiadStatus = "";
    String hLiadLawUserId = "";  //todo
    double hLiadArTotAmt = 0;
    double hLiadActTotAmt = 0;
    double hLiadTotUnpayAmt = 0;
    String hLiadLastPayDate = "";
    int hLiadNopayCnt = 0;
    double hLiadArPerAmt = 0;
    
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
            if (args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ColB028 user_id [batch_seqno]", "");
            }
            
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(0, 0, 0);
            
            hUserId = args[0];
            
//            1.	檢查ptr_sys_parm 資料，查詢條件:wf_parm = ‘COL_PARM’AND wf_key = ‘COLB028_PARM’。
//              a.	若有資料，檢查【wf_value】欄位，若此欄位值為【系統日】，表示今日已經執行此批次，
//                  停止此批次，並LOG，內容：【ColB028 更生清算繳款狀況資料彙整作業，每日僅能執行一次。】
//              b.	若有資料，且【wf_value】非為系統日，表示可以繼續執行批次。
//              c.	若無資料，表示可以繼續執行批次，insert一筆，ptr_sys_parm，設定:wf_parm = ‘COL_PARM’AND wf_key = ‘COLB028_PARM’。
//              d.	可以執行之情況，【wf_value】設定為【系統日】，【wf_value2】設定為【DOING】。
//              e.	承上述，若為線上啟動，MOD_USER為online 功能傳入之USER參數，其餘MOD_XXX相關欄位，依據系統規定。
//            2.	刪除TABLE中所有資料。TABLE：COL_LIAD_PAY_STATIC 更生清算繳款狀況統計檔。
//            3.	Insert TABLE。TABLE：COL_LIAD_PAY_STATIC 更生清算繳款狀況統計檔。
//            4.	參考報表邏輯：
//              a.	先撈取主檔資料。
//              b.	參考colr1220程式，整理資料、insert TABLE。
//            5.	完成批次時，ptr_sys_parm之【wf_value2】設定為【FINISHED】。
//            6.	如果執行失敗，rollback，update ptr_sys_parm之【wf_value】、【wf_value2】設定為【空字串】。
            
            //1.
            hParmCnt = selectPtrSysParm();
            if (hParmCnt == 0) {  //c.
            	insertPtrSysParm();
            } else {  //a.
            	if (hParmDate.equals(sysDate)) {
            		exceptExit = 0;
            		comcr.errRtn("ColB028 更生清算繳款狀況資料彙整作業，每日僅能執行一次。", "", hCallBatchSeqno);
            	}
            }
            
            //d.
            hParmStatus = "DOING";
            updatePtrSysParm();
            
            //2.
            deleteColLiadPayStatic();
            
            //3.
            selectColLiad();
            
            //5.
            hParmStatus = "FINISHED";
            updatePtrSysParm();
            

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,累計筆數 : [" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
            	comcr.callbatch(1, 0, 0);
            
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    
    /***********************************************************************/
    int selectPtrSysParm() throws Exception {
    	sqlCmd = "select wf_value, wf_value2  from ptr_sys_parm ";
        sqlCmd += "where wf_parm = 'COL_PARM' and wf_key = 'COLB028_PARM' ";
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
        setValue(extendField+"wf_key", "COLB028_PARM");
        setValue(extendField+"wf_desc", "更生清算繳款狀況資料彙整倒檔批次作業控制參數");
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
        whereStr = "where wf_parm = 'COL_PARM' and wf_key = 'COLB028_PARM' ";
        setString(1, sysDate);
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
        whereStr = "where wf_parm = 'COL_PARM' and wf_key = 'COLB028_PARM' ";

        updateTable();
        commitDataBase();
    }
    
    /***********************************************************************/
    void deleteColLiadPayStatic() throws Exception {
        daoTable = "col_liad_pay_static";
        deleteTable();
    }
    
    /***********************************************************************/
    void selectColLiad() throws Exception {
        sqlCmd = " SELECT liad_type, id_p_seqno, id_no, case_letter, recv_date ";
    	sqlCmd += " FROM (SELECT '1' liad_type, ";
    	sqlCmd += "              id_p_seqno, ";
    	sqlCmd += "              id_no, ";
		sqlCmd += "              case_letter, ";
		sqlCmd += "              max (recv_date) as recv_date ";
		sqlCmd += "       FROM col_liad_renew ";
		sqlCmd += "       WHERE 1 = 1 ";
		sqlCmd += "       GROUP BY id_p_seqno, id_no, case_letter ";
		sqlCmd += "       UNION ";
		sqlCmd += "       SELECT '2' liad_type, ";
		sqlCmd += "              id_p_seqno, ";
    	sqlCmd += "              id_no, ";
		sqlCmd += "              case_letter, ";
		sqlCmd += "              max (recv_date) as recv_date ";
		sqlCmd += "       FROM col_liad_liquidate ";
		sqlCmd += "       WHERE 1 = 1 ";
		sqlCmd += "       GROUP BY id_p_seqno, id_no, case_letter) ";
    	
		openCursor();
        while (fetchTable()) {
            hLiadLiadType = getValue("liad_type");
            hLiadIdPSeqno = getValue("id_p_seqno");
            hLiadIdNo = getValue("id_no");
            hLiadCaseLetter = getValue("case_letter");
            hLiadRecvDate = getValue("recv_date");
        	
            totalCnt++;
            if (totalCnt % 1000 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
            }
            
            //這兩個需歸零
        	hLiadLastPayDate = "";
        	hLiadArPerAmt = 0;
            
            if (hLiadLiadType.equals("1")) {  //1. 更生; 2. 清算
            	getArTotAmt1();
            	getActTotAmt1();
            	getLiadStatus1();
            	if (!hLiadLiadStatus.equals("4")) {
            		getLastPayDate1();
                	getArPerAmt1();
            	}
            } else {
            	getArTotAmt2();
            	getActTotAmt2();
            	getLiadStatus2();
            }
            calTotUnpayAmt();
            
            insertColLiadPayStatic();
    	
        }
        closeCursor();
    }
    
    /***********************************************************************/
    void getArTotAmt1() throws Exception {
    	hLiadArTotAmt = 0;
    			
    	sqlCmd = "select sum(ar_per_amt) as ar_per_amt from col_liad_install ";
    	sqlCmd += "WHERE holder_id_p_seqno = ? and case_letter = ? and (inst_date_e||payment_day) <= ? ";
        setString(1, hLiadIdPSeqno);
        setString(2, hLiadCaseLetter);
        setString(3, sysDate);
        
        extendField = "col_liad_install_1.";
        if (selectTable() > 0) {
        	hLiadArTotAmt = getValueDouble("col_liad_install_1.ar_per_amt");
        }
    }

    /***********************************************************************/
    void getActTotAmt1() throws Exception {
    	hLiadActTotAmt = 0;
    	
    	sqlCmd = "select sum(act_per_amt) as act_per_amt from col_liad_paydetl ";
    	sqlCmd += "WHERE holder_id_p_seqno = ? and case_letter = ? and pay_date <= ? and liad_type ='1' ";
        setString(1, hLiadIdPSeqno);
        setString(2, hLiadCaseLetter);
        setString(3, sysDate);
        
        extendField = "col_liad_paydetl_1.";
        if (selectTable() > 0) {
        	hLiadActTotAmt = getValueDouble("col_liad_paydetl_1.act_per_amt");
        }
    }
    
    /***********************************************************************/
    void getLiadStatus1() throws Exception {
		hLiadChiName = "";
		hLiadOrgDebtAmt = 0;
		hLiadAllocateAmt = 0;
		hLiadLiadStatus = "";
		hLiadLawUserId = "";  //todo
        
        sqlCmd = "select chi_name, org_debt_amt, payoff_amt, renew_status, law_user_id ";
        sqlCmd += "from col_liad_renew ";
        sqlCmd += "where id_p_seqno = ? and case_letter = ? and recv_date = ? fetch first 1 row only ";
        setString(1, hLiadIdPSeqno);
        setString(2, hLiadCaseLetter);
        setString(3, hLiadRecvDate);
        
        extendField = "col_liad_renew.";
        if (selectTable() > 0) {
        	hLiadChiName = getValue("col_liad_renew.chi_name");
        	hLiadOrgDebtAmt = getValueDouble("col_liad_renew.org_debt_amt");
        	hLiadAllocateAmt = getValueDouble("col_liad_renew.payoff_amt");
        	hLiadLiadStatus = getValue("col_liad_renew.renew_status");
        	hLiadLawUserId = getValue("col_liad_renew.law_user_id");
        }
    }
    
    /***********************************************************************/
    void getLastPayDate1() throws Exception {
  		hLiadLastPayDate = "";
        
        sqlCmd = "select max(pay_date) as pay_date from col_liad_install ";
        sqlCmd += "where holder_id_p_seqno = ? and case_letter = ? ";
        setString(1, hLiadIdPSeqno);
        setString(2, hLiadCaseLetter);
        
        extendField = "col_liad_install_1a.";
        if (selectTable() > 0) {
        	hLiadLastPayDate = getValue("col_liad_install_1a.pay_date");
        }
    }
    
    /***********************************************************************/
    void getArPerAmt1() throws Exception {
  		hLiadArPerAmt = 0;
		
    	sqlCmd = "select ar_per_amt from col_liad_install ";
    	sqlCmd += "WHERE inst_date_s <= substr(?,1,6) and inst_date_e >= substr(?,1,6) ";
    	sqlCmd += "and holder_id_p_seqno = ? and case_letter = ? fetch first 1 row only ";
    	setString(1, sysDate);
    	setString(2, sysDate);
    	setString(3, hLiadIdPSeqno);
        setString(4, hLiadCaseLetter);
        
        extendField = "col_liad_install_1b.";
        if (selectTable() > 0) {
        	hLiadArPerAmt = getValueDouble("col_liad_install_1b.ar_per_amt");
        }
    }
    
    /***********************************************************************/
    void getArTotAmt2() throws Exception {
    	hLiadArTotAmt = 0;
    	hLiadAllocateAmt = 0;
		
    	sqlCmd = "select allocate_amt from col_liad_paymain ";
    	sqlCmd += "WHERE holder_id_p_seqno = ? and case_letter = ? and liad_type ='2' ";
        setString(1, hLiadIdPSeqno);
        setString(2, hLiadCaseLetter);
        
        extendField = "col_liad_paymain.";
        if (selectTable() > 0) {
        	hLiadArTotAmt = getValueDouble("col_liad_paymain.allocate_amt");
        	hLiadAllocateAmt = getValueDouble("col_liad_paymain.allocate_amt");
        }
    }

    /***********************************************************************/
    void getActTotAmt2() throws Exception {
    	hLiadActTotAmt = 0;
    	
    	sqlCmd = "select sum(act_per_amt) as act_per_amt from col_liad_paydetl ";
    	sqlCmd += "WHERE holder_id_p_seqno = ? and case_letter = ? and pay_date <= ? and liad_type ='2' ";
        setString(1, hLiadIdPSeqno);
        setString(2, hLiadCaseLetter);
        setString(3, sysDate);
        
        extendField = "col_liad_paydetl_2.";
        if (selectTable() > 0) {
        	hLiadActTotAmt = getValueDouble("col_liad_paydetl_2.act_per_amt");
        }
    }
    
    /***********************************************************************/
    void getLiadStatus2() throws Exception {
    	hLiadChiName = "";
		hLiadOrgDebtAmt = 0;
		hLiadLiadStatus = "";
		hLiadLawUserId = "";  //todo
        
        sqlCmd = "select chi_name, org_debt_amt, liqu_status, law_user_id ";
        sqlCmd += "from col_liad_liquidate ";
        sqlCmd += "where id_p_seqno = ? and case_letter = ? and recv_date = ? fetch first 1 row only ";
        setString(1, hLiadIdPSeqno);
        setString(2, hLiadCaseLetter);
        setString(3, hLiadRecvDate);
        
        extendField = "col_liad_liquidate.";
        if (selectTable() > 0) {
        	hLiadChiName = getValue("col_liad_liquidate.chi_name");
        	hLiadOrgDebtAmt = getValueDouble("col_liad_liquidate.org_debt_amt");
        	hLiadLiadStatus = getValue("col_liad_liquidate.liqu_status");
        	hLiadLawUserId = getValue("col_liad_liquidate.law_user_id");
        }
    }
    
    /***********************************************************************/
    void calTotUnpayAmt() throws Exception {
//      TOT_UNPAY_AMT  DECIMAL(14,2)  未繳足金額  **ATTR:等於【AR_TOT_AMT】-【ACT_TOT_AMT】
//      NOPAY_CNT      INT            逾期期數       **ATTR:逾期期數 = (累計應還款金額 減 已還款金額) 除 當月應繳金額(無條件捨去)
//                                                            -->(【AR_TOT_AMT】-【ACT_TOT_AMT】)/【AR_PER_AMT】
    	hLiadTotUnpayAmt = 0;
        hLiadNopayCnt = 0;
    	
        hLiadTotUnpayAmt = hLiadArTotAmt - hLiadActTotAmt;
        
        //todo 1.更生status<>"4"時, h_liad_ar_per_amt=0
        //     2.清算時, h_liad_last_pay_date, h_liad_ar_per_amt 均無值
////        逾期期數 = (累計應還款金額 減 已還款金額) 除 當月應繳金額(無條件捨去)
////               -->(【AR_TOT_AMT】-【ACT_TOT_AMT】)/【AR_PER_AMT】
        if (hLiadArPerAmt != 0) {
        	hLiadNopayCnt = (int)((hLiadArTotAmt - hLiadActTotAmt) / hLiadArPerAmt);
        }
    }
    
    /***********************************************************************/
    void insertColLiadPayStatic() throws Exception {
        dateTime();
        daoTable = "col_liad_pay_static";
        extendField = daoTable + ".";
        setValue(extendField+"id_p_seqno", hLiadIdPSeqno);
        setValue(extendField+"id_no", hLiadIdNo);
        setValue(extendField+"chi_name", hLiadChiName);
        setValue(extendField+"case_letter", hLiadCaseLetter);
        setValue(extendField+"liad_type", hLiadLiadType);
        setValue(extendField+"liad_status", hLiadLiadStatus);
        setValue(extendField+"recv_date", hLiadRecvDate);
        setValueDouble(extendField+"org_debt_amt", hLiadOrgDebtAmt);
        setValueDouble(extendField+"allocate_amt", hLiadAllocateAmt);
        setValueDouble(extendField+"ar_tot_amt", hLiadArTotAmt);
        setValueDouble(extendField+"act_tot_amt", hLiadActTotAmt);
        setValueDouble(extendField+"tot_unpay_amt", hLiadTotUnpayAmt);
        setValue(extendField+"last_pay_date", hLiadLastPayDate);
        setValueInt(extendField+"nopay_cnt", hLiadNopayCnt);
        setValueDouble(extendField+"ar_per_amt", hLiadArPerAmt);
        setValue(extendField+"mod_user", hUserId);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
        	clearPtrSysParm();
            comcr.errRtn("insert_col_liad_pay_static duplicate!", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB028 proc = new ColB028();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

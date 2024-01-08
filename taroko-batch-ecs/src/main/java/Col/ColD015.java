/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/13  V1.00.00    phopho     program initial                          *
*  108/12/02  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/15  V1.00.02    shiyuqi    updated for project coding standard      *
*  112/06/30  V1.00.03    sunny      調整簡訊處理參數                                                          *
*  112/07/15  V1.00.04    sunny      調整簡訊處理執行日期，顯示LOG                  *
*  112/07/19  V1.00.05    Ryan       insert SMS_MSG_DTL 增加 BOOKING_DATE,BOOKING_TIME *
*  112/07/20  V1.00.06    sunny      增加排除政府採購卡
*  112/08/07  V1.00.07    sunny      加強顯示排除條件的處理訊息                                            *
*  112/10/02  V1.00.08    sunny     縮小查詢範圍，改善效能                                                    *
******************************************************************************/

package Col;

import com.*;

public class ColD015 extends AccessDAO {
    private String progname = "篩選 MCode=M0-2,產生第二次催繳簡訊處理(繳款寬延日 + x天日曆日)程式 112/10/02  V1.00.08 ";
    
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;

    int debug = 0;
    int debugD = 0;
    String hCallErrorDesc = "";
    String hBusiBusinessDate = "";
    String newBusiBusinessDate = "";

    double hAcctMinPayBal = 0;
    double hAcctAutopayBal = 0;
    double hAcctTtlAmtBal = 0;
    double hAchtStmtThisTtlAmt = 0;
    double hApdlPayAmt = 0;
    double hAibmTxnAmt = 0;
    String hIdno = "";
    String hIdnoChiName = "";
    String hIdnoBirthday = "";
    String hIdnoCellarPhone = "";
    String hWdayStmtCycle = "";
    String hWdayLastAcctMonth = "";
    String hSmidMsgDept = "";
    String hSmidMsgId = "";
    String hSmidMsgSendFlag = "";
    String hSmidAcctTypeSel = "";
    String hSmidMsgAcctType = "";
    String hSmidMsgUserid = "";
    String hSmidMsgSelAmt01 = "";
    double hSmidMsgAmt01 = 0;
    int hSmidMsgRunDay = 0;
    String hSmidMsgDesc = "";
    String hAcnoIdPSeqno = "";
    String hAcnoAutopayAcctNo = "";
    String hAcnoAutopayAcctSDate = "";
    String hAcnoAutopayAcctEDate = "";
    String hAcnoPaymentRate1 = "";
    double hAcnoModSeqno = 0;
    String hAcnoNoSmsFlag = "";
    String hAcnoNoSmsSDate = "";
    String hAcnoNoSmsEDate = "";
    String hAcnoRowid = "";
    String hAcnoPSeqno = "";
    String hAcnoGpNo = "";
    String hAcnoAcctType = "";
    String hAcnoAcctHolderId = "";

    long totalCnt = 0;
    long prsCount = 0;
    String hSysDate = "";
    String hModUser = "";
    String hTempThisLastpayDate = "";
    String hTempThisDelaypayDate = "";    
    String hTempCellphoneCheckFlag = "";
    String szTmp = "";
    double hTempEndBalAf = 0;
    int paySmsFlag = 0;
    int noSmsFlagCnt = 0;
    int acctType06Cnt=0;
    private double hStmtMp = 0;
    

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColD015 proc = new ColD015();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            // 檢查參數
            if (args.length > 1) {
                comc.errExit("Usage : ColD015 [business_date]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            hModUser = comc.commGetUserID();

            hBusiBusinessDate = "";
            if (args.length > 0) {
                if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                }
            }

            /*--read 簡訊參數:notFound exit------------------------------*/
            selectSmsMsgId();
            
            showLogMessage("I", "", "取得營業日["+hBusiBusinessDate+"]，MSG_ID[" + hSmidMsgId + "]，參數-工作天數["+ hSmidMsgRunDay + "]");
            
            selectPtrBusinday();

            processData(); /* Main */
            commitDataBase();

            showLogMessage("I", "", "產生簡訊總筆數=[" + prsCount + "]");
            showLogMessage("I", "", "ColD015 程式執行結束 ");

            // ==============================================
            // 固定要做的
            // showLogMessage("I","","程式執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
      // ************************************************************************

    private void selectPtrBusinday() throws Exception {
    	sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
    	
        selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_businday error!(讀不到營業日)", "", comcr.hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
    }

    // ************************************************************************
    private void processData() throws Exception {
        selectSQL = "stmt_cycle, substr(to_char(to_number(this_lastpay_date)-19110000,'0000000'),2,3)||'/'||"
                + "substr(this_lastpay_date,5,2)||'/'||substr(this_lastpay_date,7,2) this_lastpay_date, this_delaypay_date,"
        		+ "to_char(to_date(this_delaypay_date,'yyyymmdd') + ? days,'yyyymmdd') this_exec_date," /*執行日*/
                + "last_acct_month ";
        daoTable = "ptr_workday";
        //whereStr = "where to_char(to_date(this_delaypay_date,'yyyymmdd') + ? days,'yyyymmdd')= ? "
        whereStr = "  where 1=1 order by stmt_cycle "; /* 執行日 = 繳款寬限日 + x 日 = 系統日(發簡訊日), M0與M0-2差異在發送日不同 */
        setInt(1, hSmidMsgRunDay);
//        setString(2, hBusiBusinessDate);

        int recordCnt = selectTable();
        
        if (recordCnt<=0) showLogMessage("I", "", "查無符合的執行cycle，今日不執行!");
        
        for (int i = 0; i < recordCnt; i++) {
            hWdayStmtCycle = getValue("stmt_cycle", i);
            hTempThisLastpayDate = getValue("this_lastpay_date", i);
            hTempThisDelaypayDate = getValue("this_delaypay_date", i);
            hWdayLastAcctMonth = getValue("last_acct_month", i);
            newBusiBusinessDate = getValue("this_exec_date", i);
            
            //計算執行日
            //newBusiBusinessDate = comcr.increaseDays(hTempThisLastpayDate, hSmidMsgRunDay);
            showLogMessage("I", "", "取得cycle["+hWdayStmtCycle+"],前次帳單月份["+hWdayLastAcctMonth+"],繳款寬延日 ["+hTempThisDelaypayDate+"] + [" + hSmidMsgRunDay +"] 天日曆日 , 即執行日[" + newBusiBusinessDate +"]");

            if(!newBusiBusinessDate.equals(hBusiBusinessDate))
            {
            	//showLogMessage("I", "", String.format("今日不執行"),"");
            	continue;
            }
            
//            if (debug == 1)
//                showLogMessage("I", "", "Cycle=[" + hWdayStmtCycle + "]");

            if (debug == 1)
                showLogMessage("I", "", "this_delaypay_date=[" + hTempThisDelaypayDate + "] stmt_cycle=["
                        + hWdayStmtCycle + "] last_acct_month=[" + hWdayLastAcctMonth + "]");

//            showLogMessage("I", "", "business_date[" +hBusiBusinessDate+"], stmt_cycle=["
//                           + hWdayStmtCycle + "], 繳款寬延日[ "+ hTempThisDelaypayDate +"] + RunDay["+hSmidMsgRunDay+"]");
            
            showLogMessage("I", "", "business_date[" +hBusiBusinessDate+"], stmt_cycle=["+ hWdayStmtCycle + "]");
            showLogMessage("I", "", "繳款寬延日[ "+ hTempThisDelaypayDate +"] + RunDay["+hSmidMsgRunDay+"]");            
            
            selectActAcno();
        }
    }

    // ************************************************************************
    private void selectActAcno() throws Exception {
//        selectSQL = "a.p_seqno, a.gp_no, a.id_p_seqno, a.acct_type, a.payment_rate1, "
        selectSQL = "a.acno_p_seqno, a.p_seqno, a.id_p_seqno, a.acct_type, a.payment_rate1, "
                + "b.id_no as acct_holder_id, a.no_sms_flag, a.no_sms_s_date, a.no_sms_e_date, "
                + "a.autopay_acct_no, a.autopay_acct_s_date, a.autopay_acct_e_date, a.rowid as rowid ";
        daoTable = "act_acno a, crd_idno b,act_acct d";
        whereStr = "where b.id_p_seqno=a.id_p_seqno "
                + "and a.p_seqno=d.p_seqno "
        		+ "and a.stmt_cycle = ? and   a.payment_rate1 >= '0A' " // M0-2與M0相同, rate1='0A'~'0E'
                + "and a.payment_rate1 <= '0E' "
                + "and a.acct_status < '3' " //不含催呆戶
                + "and d.min_pay_bal > 0 "; //縮小查詢範圍，改善效能
        setString(1, hWdayStmtCycle);

        openCursor();
        while (fetchTable()) {
//            h_acno_p_seqno = getValue("p_seqno");
//            h_acno_gp_no = getValue("gp_no");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoGpNo = getValue("p_seqno");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hAcnoPaymentRate1 = getValue("payment_rate1");
            hAcnoAcctHolderId = getValue("acct_holder_id");
            hAcnoNoSmsFlag = getValue("no_sms_flag");
            hAcnoNoSmsSDate = getValue("no_sms_s_date");
            hAcnoNoSmsEDate = getValue("no_sms_e_date");
            hAcnoAutopayAcctNo = getValue("autopay_acct_no");
            hAcnoAutopayAcctSDate = getValue("autopay_acct_s_date");
            hAcnoAutopayAcctEDate = getValue("autopay_acct_e_date");
            hAcnoRowid = getValue("rowid");

            totalCnt++;
            if (totalCnt % 5000 == 0)
                showLogMessage("I", "", "    讀取筆數 =[" + totalCnt + "]");

            /*--- check acct_type ---------------------------------------*/
            if (!checkAcctType())
                continue;
           
            /*排除政府採購卡---------------------------------------------*/
    		if (hAcnoAcctType.equals("06"))
    		{
    			if (debug == 1)
    			showLogMessage("I", "", "    排除政府採購卡 id_p_seqno =[" + hAcnoIdPSeqno + "]");
    			acctType06Cnt++;
     			continue;
    		}

            /*--min-pay is 0---------------------------------------------*/
            selectActAcct();
            selectActPayDetail();
            selectActPayIbm();

            /*--排除帳單僅有年費者-------------------------------------------*/
            selectActAcctHst();

            //20230805 TCB取消
            /*
            if (debug == 1)
                showLogMessage("I", "", "stmt_this_ttl_amt=[" + hAchtStmtThisTtlAmt + "] ttl_amt_bal=["
                        + hAcctTtlAmtBal + "] AF=[" + hTempEndBalAf + "]");
            if (hAchtStmtThisTtlAmt == hTempEndBalAf)
                continue;
            */

            hAcctMinPayBal = hAcctMinPayBal - hApdlPayAmt - hAibmTxnAmt;
            if (hAcctMinPayBal < 0)
                hAcctMinPayBal = 0;
            /* 對帳單總應繳款餘額=對帳單總應繳款餘額-繳款金額-透過ibm繳款金額 */
            hAcctTtlAmtBal = hAcctTtlAmtBal - hApdlPayAmt - hAibmTxnAmt;
            if (hAcctTtlAmtBal < 0)
                hAcctTtlAmtBal = 0;
            if (hAcctMinPayBal == 0) {
                if (debug == 1)
                    showLogMessage("I", "", "-- min pay is 0 not send");
                continue;
            }

            /*--check 欠款金額, 當online sms0010的[檢查消費金額]有選取---------*/
            if (hSmidMsgSelAmt01.compareTo("Y") == 0) {
                if (hAcctTtlAmtBal < hSmidMsgAmt01)
                    continue;
            }
            /*--check autopay-------------------------------------------*/
            if ((hAcnoAutopayAcctNo.length() > 0) && (hAcctAutopayBal > 0)) {
                if ((sysDate.compareTo(hAcnoAutopayAcctSDate) >= 0) && (hAcnoAutopayAcctEDate.length() == 0))
                    continue;
                if ((sysDate.compareTo(hAcnoAutopayAcctSDate) >= 0)
                        && (sysDate.compareTo(hAcnoAutopayAcctEDate) <= 0))
                    continue;
            }
            /** not send message **/
            if (hAcnoNoSmsFlag.compareTo("Y") == 0) {
                if ((sysDate.compareTo(hAcnoNoSmsSDate) >= 0) && (hAcnoNoSmsEDate.length() == 0))
                {
                	//if (debug == 1)
                	showLogMessage("I", "", "    排除暫不發催收簡訊名單 id_p_seqno =[" + hAcnoIdPSeqno + "]");
                	noSmsFlagCnt++;
                	continue;
                }
                if ((sysDate.compareTo(hAcnoNoSmsSDate) >= 0) && (sysDate.compareTo(hAcnoNoSmsSDate) <= 0))
                {
                	//if (debug == 1) 
               	 	showLogMessage("I", "", "    排除暫不發催收簡訊名單 id_p_seqno =[" + hAcnoIdPSeqno + "]");
                	noSmsFlagCnt++;
                	continue;
                }
            }
            selectCrdIdno();
            if (hIdnoCellarPhone.length() == 10) {
                hTempCellphoneCheckFlag = "Y";
            } else {
                hTempCellphoneCheckFlag = "N";
            }

//            szTmp = hSmidMsgUserid + "," + hSmidMsgId + "," + hIdnoCellarPhone + "," + hIdnoChiName + ","
//                    + hAcctMinPayBal + "," + hTempThisLastpayDate;
            
	        szTmp = hSmidMsgUserid + "," + hSmidMsgId + "," + hIdnoCellarPhone + "," + hIdnoChiName ;
            		
            if (debug == 1)
                showLogMessage("I", "", "SEND MSG=[" + szTmp + "]");

            /* 尚有欠款 >= user設定的[檢查消費金額], insert sms_msg_dtl table */
             insertSmsMsgDtl(); 

            prsCount++;
            if (prsCount % 2000 == 0) {
            	showLogMessage("I", "", "    讀取筆數 =[" + totalCnt + "]");
                showLogMessage("I", "", "    產生簡訊筆數 =[" + prsCount + "]");
                commitDataBase();
            }
        }
        closeCursor();
 
        //顯示排除條件的筆數
 			showLogMessage("I", "", String.format("排除政府採購卡[%d]筆資料", acctType06Cnt));
            showLogMessage("I", "", String.format("排除暫不發催收簡訊[%d]筆資料", noSmsFlagCnt));
    }

    // ************************************************************************
    private boolean checkAcctType() throws Exception {
        if (hSmidAcctTypeSel.equals("0"))
            return true;

        if (hSmidAcctTypeSel.equals("1")) {
            sqlCmd = "select data_code from sms_dtl_data where table_name='SMS_MSG_ID' and data_key = ? and data_type='1'";
            setString(1, "ColD015");
            
            extendField = "sms_dtl_data.";
            
            int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                String dataCode = getValue("sms_dtl_data.data_code", i);
                if (dataCode.equals(hAcnoAcctType)) {
                    return true;
                }
            }
        } else {
            sqlCmd = "select data_code "
                    + "  from sms_dtl_data "
                    + " where table_name = 'SMS_MSG_ID' "
                    + "   and data_key   = ? "
                    + "   and data_type  = '1'";
			setString(1, "ColD015");
			int recordCnt = selectTable();
			for (int i = 0; i < recordCnt; i++) {
				String data_code = getValue("data_code", i);
				if (data_code.equals(hAcnoAcctType)) {
					return false;
				}
			}
			return true;
		}
        return false;
    }
    // ************************************************************************
    private void insertSmsMsgDtl() throws Exception {
        hAcnoModSeqno = comcr.str2double(comr.getSeqno("ECS_MODSEQ"));

        dateTime();
        daoTable = "sms_msg_dtl";
        extendField = daoTable + ".";
        String seq_str  = String.format("%010d" , comcr.getModSeq());
        if(debug==1) showLogMessage("I", "", "SEQ="+comc.getECSHOME()+","+seq_str);

        //setValueDouble(extendField+"msg_seqno", hAcnoModSeqno);
        setValue(extendField+"msg_seqno", seq_str);
        setValue(extendField+"msg_pgm", javaProgram);
        setValue(extendField+"msg_dept", hSmidMsgDept);
        setValue(extendField+"msg_id", hSmidMsgId);
        setValue(extendField+"cellar_phone", hIdnoCellarPhone);
        setValue(extendField+"cellphone_check_flag", hTempCellphoneCheckFlag);
        setValue(extendField+"chi_name", hIdnoChiName);
        setValue(extendField+"msg_desc", szTmp);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"id_no", hAcnoAcctHolderId);
        setValueDouble(extendField+"min_pay", hAcctMinPayBal);
        //setValue("deadline_date", h_acno_no_sms_e_date);
        setValue(extendField+"add_mode", "B"); // to 三竹簡訊
        setValue(extendField+"crt_date", sysDate);
        //setValue("add_time", sysTime);
        //setValue("add_datetime", sysDate + sysTime);
        setValue(extendField+"crt_user", hModUser);
        setValue(extendField+"apr_date", sysDate);
        //setValue("conf_time", sysTime);
        //setValue("conf_datetime", sysDate + sysTime);
        setValue(extendField+"apr_user", hModUser);
        setValue(extendField+"apr_flag", "Y");
        //setValue("msg_status", "30");
        setValue(extendField+"SEND_FLAG", "N");
        setValue(extendField+"proc_flag", "N");
        //setValue("msg_userid", h_smid_msg_userid);
        setValue(extendField+"mod_user", hModUser);
        setValue(extendField+"mod_time", sysDate+sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        setValue(extendField+"BOOKING_DATE", newBusiBusinessDate);
        setValue(extendField+"BOOKING_TIME", prsCount>CommCol.SMS_CONTROL_CNT ? CommCol.SEND_SMS_TIME_PM : CommCol.SEND_SMS_TIME_AM);
        insertTable();
        if (dupRecord.equals("Y")) {
            rollbackDataBase();
            showLogMessage("I", "", "insert_sms_msg_dtl error msg_id[" + hSmidMsgId + "]");
            exitProgram(0);
        }
    }

    // ************************************************************************
    private void selectCrdIdno() throws Exception {
        hIdnoChiName = "";
        hIdnoBirthday = "";
        hIdnoCellarPhone = "";
        sqlCmd = "select ";
        sqlCmd += "id_no, ";
        sqlCmd += "chi_name, ";
        sqlCmd += "birthday, ";
        sqlCmd += "cellar_phone ";
        sqlCmd += "from crd_idno ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        
        extendField = "crd_idno.";

        if (selectTable() > 0) {
        	hIdno = getValue("crd_idno.id_no");
            hIdnoChiName = getValue("crd_idno.chi_name");
            hIdnoBirthday = getValue("crd_idno.birthday");
            hIdnoCellarPhone = getValue("crd_idno.cellar_phone");
        }
    }
    // ************************************************************************
    /*************************************************************************/

    /* read 簡訊參數 */
    /*************************************************************************/
    private void selectSmsMsgId() throws Exception {
        hSmidMsgId = "";
        hSmidMsgDept = "";
        hSmidMsgSendFlag = "";
        hSmidAcctTypeSel = "";
        hSmidMsgAcctType = "";
        hSmidMsgUserid = "";
        hSmidMsgSelAmt01 = "";
        hSmidMsgAmt01 = 0;
        hSmidMsgRunDay = 0;
        sqlCmd = "select ";
        sqlCmd += "msg_id, ";
        sqlCmd += "msg_dept, ";
        sqlCmd += "msg_send_flag, ";
        sqlCmd += "decode(acct_type_sel,'','Y', acct_type_sel) acct_type_sel, ";
        sqlCmd += "msg_userid, ";
        sqlCmd += "decode(msg_sel_amt01,'','N',msg_sel_amt01) msg_sel_amt01, ";
        sqlCmd += "msg_amt01, ";
        sqlCmd += "msg_run_day, ";
        sqlCmd += "msg_desc ";
        sqlCmd += "from sms_msg_id ";
        sqlCmd += "where msg_pgm = ? ";
        sqlCmd += "and decode(msg_send_flag,'','N',msg_send_flag) ='Y' ";
//        setString(1, javaProgram);
        setString(1, "ColD015");

        if (selectTable() > 0) {
            hSmidMsgId = getValue("msg_id");
            hSmidMsgDept = getValue("msg_dept");
            hSmidMsgSendFlag = getValue("msg_send_flag");
            hSmidAcctTypeSel = getValue("acct_type_sel");
            hSmidMsgAcctType = getValue("msg_acct_type");
            hSmidMsgUserid = getValue("msg_userid");
            hSmidMsgSelAmt01 = getValue("msg_sel_amt01");
            hSmidMsgAmt01 = getValueLong("msg_amt01");
            hSmidMsgRunDay = getValueInt("msg_run_day");
            hSmidMsgDesc   = getValue("msg_desc");  //sunny add
            
           // showLogMessage("I", "", "MSG_ID=[" + hSmidMsgId + "],rund_day=[" +hSmidMsgRunDay +"]");
            
        }

        /*--not found--------*/
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_sms_msg_id error! MSG_PGM=[" + javaProgram + "] send_flag=Y", "--簡訊暫停發送 (M0-2)", comcr.hCallBatchSeqno);
        }

        if (debug == 1)
            showLogMessage("I", "", "sel_amt01=[" + hSmidMsgSelAmt01 + "] msg_amt01=[" + hSmidMsgAmt01
                    + "] rund_day=[" + hSmidMsgRunDay + "]");
    }

    // ************************************************************************
    private void selectActAcct() throws Exception {
        hAcctMinPayBal = 0;
        hAcctTtlAmtBal = 0;
        hAcctAutopayBal = 0;
        sqlCmd = "select ";
        sqlCmd += "min_pay_bal, ";
        sqlCmd += "ttl_amt_bal, ";
        sqlCmd += "autopay_bal ";
        sqlCmd += "from act_acct ";
        sqlCmd += "where p_seqno = ? ";
        setString(1, hAcnoGpNo);

        extendField = "act_acct.";
        
        if (selectTable() > 0) {
            hAcctMinPayBal = getValueDouble("act_acct.min_pay_bal");
            hAcctTtlAmtBal = getValueDouble("act_acct.ttl_amt_bal");
            hAcctAutopayBal = getValueDouble("act_acct.autopay_bal");
        }
    }

    // ************************************************************************
    private void selectActPayDetail() throws Exception {
        hApdlPayAmt = 0;
        sqlCmd = "select ";
        sqlCmd += "sum(pay_amt) pay_amt ";
        sqlCmd += "from act_pay_detail d,act_pay_batch b ";
        sqlCmd += "where b.batch_no = d.batch_no ";
        sqlCmd += "and   p_seqno = ? ";
        setString(1, hAcnoGpNo);

        extendField = "act_pay_detail.";
        
        if (selectTable() > 0) {
            hApdlPayAmt = getValueDouble("act_pay_detail.pay_amt");
        }
    }

    // ************************************************************************
    private void selectActPayIbm() throws Exception {
        hAibmTxnAmt = 0;
        sqlCmd = "select ";
        sqlCmd += "sum(txn_amt) txn_amt ";
        sqlCmd += "from act_pay_ibm ";
        sqlCmd += "where decode(proc_mark,'',' ',proc_mark) !='Y' ";
        sqlCmd += "and   p_seqno = ? ";
        setString(1, hAcnoGpNo);
        
        extendField = "act_pay_ibm.";

        if (selectTable() > 0) {
            hAibmTxnAmt = getValueDouble("act_pay_ibm.txn_amt");
        }
    }

    // ************************************************************************
    private void selectActAcctHst() throws Exception {
        hTempEndBalAf = 0;
        hAchtStmtThisTtlAmt = 0;
        sqlCmd = "select ";
        sqlCmd += "(unbill_end_bal_af+billed_end_bal_af) end_bal_af, ";
        sqlCmd += "stmt_this_ttl_amt,stmt_mp ";
        sqlCmd += "from act_acct_hst ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and   decode(acct_month,'','x',acct_month) = ? ";
        setString(1, hAcnoGpNo);
        setString(2, hWdayLastAcctMonth);
        
        extendField = "act_acct_hst.";

        if (selectTable() > 0) {
            hTempEndBalAf = getValueDouble("act_acct_hst.end_bal_af");
            hAchtStmtThisTtlAmt = getValueDouble("act_acct_hst.stmt_this_ttl_amt");
            hStmtMp = getValueDouble("act_acct_hst.stmt_mp");
        }
    }
    // ************************************************************************
}
/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/02/26  V1.01.01  David FU    Initial                                   
*  109/11/20  V1.00.02  yanghan       修改了變量名稱和方法名稱  
*  112/05/26  V1.00.03  Wilson       調整where條件                                                                                    *  
******************************************************************************/

package Ich;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡到期產生餘額轉置-BTRQ程式V1.03.02*/
public class IchC061 extends AccessDAO {
    private String progname = "停卡、毀補、卡到期產生餘額返還-B09B程式  112/05/26 V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hSystemDate = "";
    String hSystemTime = "";
    String hParmDate = "";
    String hTardNewIchCardNo = "";
    String hTardIchCardNo = "";
    String hTardCardNo = "";
    String hTardNewEndDate = "";
    String hTardCurrentCode = "";
    String hTardRowid = "";
    int tempInt = 0;
    String hFeepExpireFeeFlag = "";
    double hFeepChargeFee = 0;
    String dateFmt = "";
    String dateFmm = "";
    String embossKind = "";
    String hBtrqBalanceDatePlan = "";
    String hBtrqEmbossKind = "";
    String sqlSt = "";
    int hFeepMonthMoney = 0;
    int hFeepMonthTimes = 0;
    int hFeepDaysToTsc = 0;
    int hCnt = 0;
    double tempAmt = 0;
    int totCnt = 0;
    int recCnt = 0;
    String chkPay = "";
    int hFeepUseTimes = 0;
    int hFeepUseMoney = 0;
    String sData = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : IchC061 [yyyymmdd] [batch_seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (comc.getSubString(hCallBatchSeqno, 0, 8).equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
                hCallBatchSeqno = "no-call";
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempUser = "";
            if (hCallBatchSeqno.length() == 20) {

                comcr.hCallBatchSeqno = hCallBatchSeqno;
                comcr.hCallRProgramCode = javaProgram;

                comcr.callbatch(0, 0, 1);
                sqlCmd = "select user_id ";
                sqlCmd += " from ptr_callbatch  ";
                sqlCmd += "where batch_seqno = ? ";
                setString(1, hCallBatchSeqno);
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hTempUser = getValue("user_id");
                }
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            selectPtrBusinday();

            if (args.length > 0 && args[0].length() == 8) {
                hParmDate = args[0];
            }

            showLogMessage("I", "", String.format(" Process date =[%s]\n", hParmDate));

            selectIchCard();

            showLogMessage("I", "", String.format("程式執行結束,筆數=[%d][%d]\n", totCnt, recCnt));
            // ==============================================
            // 固定要做的
            if (hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1);
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
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_system_time, ";
        sqlCmd += "to_char(sysdate-1,'yyyymmdd') as h_process_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hSystemDate = getValue("h_system_date");
            hSystemTime = getValue("h_system_time");
            hParmDate = getValue("h_process_date");
        }

    }

    /***********************************************************************/
    void selectIchCard() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "new_ich_card_no,";
        sqlCmd += "ich_card_no,";
        sqlCmd += "card_no,";
        sqlCmd += "new_end_date,";
        sqlCmd += "current_code,";
        sqlCmd += "rowid as rowid1 ";
        sqlCmd += "from ich_card ";
        sqlCmd += "where current_code not in ('0','2') ";
        sqlCmd += "and return_date = '' "; 
        sqlCmd += "and balance_date = '' ";
        sqlCmd += "and ich_oppost_date = ? ";
        setString(1, hParmDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTardNewIchCardNo = getValue("new_ich_card_no", i);
            hTardIchCardNo     = getValue("ich_card_no", i);
            hTardCardNo         = getValue("card_no", i);
            hTardNewEndDate    = getValue("new_end_date", i);
            hTardCurrentCode    = getValue("current_code", i);
            hTardRowid           = getValue("rowid1", i);

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

            selectIchB04bSpecial();
            if (tempInt > 0) {               
            } 
            else
            {
            	insertIchB04bSpecial();
            }

            selectIchB09bBal();
            if (tempInt > 0) {
			} 
            else
			{
				insertIchB09bBal();
			}
			
            recCnt++;			
        }

    }
    /***********************************************************************/
    void selectIchB04bSpecial() throws Exception {
        tempInt = 0;
        sqlCmd  = "select count(*) temp_int ";
        sqlCmd += "  from ich_b04b_special  ";
        sqlCmd += " where ich_card_no  = ?  ";
        sqlCmd += "   and proc_type   = '5' ";
        setString(1, hTardIchCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

    }
    /***********************************************************************/
    void selectIchB09bBal() throws Exception {
        tempInt = 0;
        sqlCmd  = "select count(*) temp_int ";
        sqlCmd += "  from ich_b09b_bal  ";
        sqlCmd += " where ich_card_no  = ? ";
        setString(1, hTardIchCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

    }
      /***********************************************************************/
    void insertIchB04bSpecial() throws Exception {

       
        setValue("ich_card_no", hTardIchCardNo);
        setValue("proc_type"  , "5");
        setValue("sys_date"   , sysDate);
        setValue("sys_time"   , sysTime);
        setValue("proc_flag"  , "N");
        setValue("mod_time"   , sysDate + sysTime);
        setValue("mod_pgm"    , javaProgram);
        daoTable = "ich_b04b_special";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ich_b04b_special duplicate!", hTardIchCardNo, hCallBatchSeqno);
        }
    }          
    /***********************************************************************/
    void insertIchB09bBal() throws Exception {
    	String tmpBalRsn = "";
    	
    	if(hTardCurrentCode.equals("6")) {
    		tmpBalRsn = "01";
    	}
    	else if(hTardCurrentCode.equals("7")) {
    		tmpBalRsn = "04";
    	}
    	else {
    		tmpBalRsn = "02";
    	}

        setValue("ich_card_no", hTardIchCardNo);
        setValue("card_no"    , hTardCardNo);
        setValue("bal_rsn"    , tmpBalRsn);
        setValue("sys_date"   , sysDate);
        setValue("sys_time"   , sysTime);
        setValue("proc_flag"  , "N");
        setValue("mod_time"   , sysDate + sysTime);
        setValue("mod_pgm"    , javaProgram);
        daoTable = "ich_b09b_bal";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ich_b09b_bal duplicate!", hTardIchCardNo, hCallBatchSeqno);
        }
    }

    /***********************************************************************/
public static void main(String[] args) throws Exception {
        IchC061 proc = new IchC061();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
}
/***********************************************************************/
}

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  2022-01-11  V1.00.0     Ryan     initial                                   *
*  2022-01-20  V1.00.1     Ryan     add  deleteMktLineMessage                 *
*  2022-03-03  V1.00.2     Ryan     add  deleteFscIcud03 deleteCcaAuthBitdata *
*  2022-03-07  V1.00.3     Ryan     add  deleteCcaImsLog                      *
*  2022-03-08  V1.00.4     Ryan     up   deleteAuthTxLog cacu_flag != 'Y'     *
******************************************************************************/
package Cca;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class CcaAuiClearDB extends AccessDAO {
    private String PROGNAME = "auiclearDB 依參數清除DB資料 111/03/08  V1.01.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int DEBUG = 1;

    String targetAuTxDate = "";
    String targetDbLogDate = "";
    String businessDate = "";

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : CcaAuiClearDB ", "");
            }

            // 固定要做的
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            getSysParamValue();
            deleteAuthTxLog();
            deleteStaDailyMcc();
            deleteCcaStaRiskType();
            deleteCcaStaTxUnormal();
            deleteCcaAcctBalance();
         // deleteOnBatData();

            deleteCcaDebitBil();
            deleteCcaIbmReversal();
            deleteCcaMsgLog();
            deleteSmsMsgDtl();
            deleteRskFactormaster();
            deleteMktLineMessage();
            deleteFscIcud03();
            deleteCcaAuthBitdata();
            deleteCcaImsLog();

            showLogMessage("I", "", String.format("AuiClearDb 處理完成"));

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
    private void getSysParamValue() throws Exception {

        sqlCmd = " SELECT TO_CHAR(SYSDATE- cast(decode(SYS_DATA1,'','20',SYS_DATA1) as int) days ,'YYYYMMDD') as sG_TargetAuTxDate ";
        sqlCmd += " FROM CCA_SYS_PARM1 ";
        sqlCmd += " WHERE SYS_ID='REPORT' ";
        sqlCmd += "   AND SYS_KEY='PHYSICAL' ";
        if (selectTable() > 0) {
        	targetAuTxDate = getValue("sG_TargetAuTxDate");
        }

        sqlCmd = " SELECT TO_CHAR(SYSDATE- cast(decode(SYS_DATA1,'','20',SYS_DATA1) as int) days ,'YYYYMMDD')  as sG_TargetDbLogDate ";
        sqlCmd += " FROM CCA_SYS_PARM1 ";
        sqlCmd += " WHERE SYS_ID='REPORT' ";
        sqlCmd += "   AND SYS_KEY='DB_LOG' ";
        if (selectTable() > 0) {
            targetDbLogDate = getValue("sG_TargetDbLogDate");
        }

    }
    
    /***********************************************************************/
    public void selectPtrBusinday() throws Exception{
    	
    	sqlCmd = "select business_date from ptr_businday";
    	 if (selectTable() > 0) {
    		 businessDate = getValue("business_date");
         }
    }

    /***********************************************************************/
    private void deleteCcaMsgLog() throws Exception {

        daoTable = "CCA_MSG_LOG";
        whereStr = "WHERE TX_DATE< ? ";
        setString(1, targetDbLogDate);
        deleteTable();

    }

    /***********************************************************************/
    private void deleteCcaIbmReversal() throws Exception {

        daoTable = "CCA_IBM_REVERSAL";
        whereStr = "WHERE TX_DATE< ? ";
        setString(1, targetDbLogDate);
        deleteTable();
    }

    /***********************************************************************/
    private void deleteCcaDebitBil() throws Exception {

        daoTable = "CCA_DEBIT_BIL";
        whereStr = "WHERE TX_DATE< ? ";
        setString(1, targetDbLogDate);
        deleteTable();
    }

    /***********************************************************************/
    private void deleteAuthTxLog() throws Exception {

        daoTable = "CCA_AUTH_TXLOG";
        whereStr = "WHERE TX_DATE< ? and cacu_flag != 'Y'";
        setString(1, targetAuTxDate);
        deleteTable();
    }

    /***********************************************************************/
    private void deleteStaDailyMcc() throws Exception {

        daoTable = "CCA_STA_DAILY_MCC";
        whereStr = "WHERE STA_DATE< ? ";
        setString(1, targetDbLogDate);
        deleteTable();
    }

    /***********************************************************************/
    private void deleteCcaStaRiskType() throws Exception {

        daoTable = "CCA_STA_RISK_TYPE";
        whereStr = "WHERE STA_DATE< ? ";
        setString(1, targetDbLogDate);
        deleteTable();
    }

    /***********************************************************************/
    private void deleteCcaStaTxUnormal() throws Exception {

        daoTable = "CCA_STA_TX_UNORMAL";
        whereStr = "WHERE STA_DATE< ? ";
        setString(1, targetDbLogDate);
        deleteTable();

    }

    /***********************************************************************/
    private void deleteCcaAcctBalance() throws Exception {

        daoTable = "CCA_ACCT_BALANCE";
        whereStr = "WHERE SYS_DATE< ? ";
        setString(1, targetDbLogDate);
        deleteTable();

    }

    /***********************************************************************/
    private void deleteOnBatData() throws Exception {

        daoTable = "ONBAT_2ECS";
        whereStr = "WHERE TO_CHAR(DOG,'YYYYMMDD') < ? AND PROC_STATUS > ?";
        setString(1, sysDate);
        setInt(2, 0);
        deleteTable();
    }
    
    /***********************************************************************/
    private void deleteSmsMsgDtl() throws Exception {

        daoTable = "SMS_MSG_DTL";
        whereStr = "WHERE CRT_DATE < ?";
        setString(1, targetDbLogDate);
        deleteTable();
    }
    
    /***********************************************************************/
    private void deleteRskFactormaster() throws Exception {

        daoTable = "RSK_FACTORMASTER";
        whereStr = "WHERE TX_DATE < ? ";
        setString(1, targetAuTxDate);
        deleteTable();
    }
    
    /***********************************************************************/
    private void deleteMktLineMessage() throws Exception {

        daoTable = "MKT_LINE_MESSAGE";
        whereStr = "WHERE CRT_DATE < ? ";
        setString(1, targetDbLogDate);
        deleteTable();
    }
    
    /***********************************************************************/
    private void deleteFscIcud03() throws Exception {

        daoTable = "FSC_ICUD03";
        whereStr = "WHERE file_no < to_char(to_date(?,'YYYYMMDD') - 1 MONTH,'YYYYMMDD') ";
        setString(1, businessDate);
        deleteTable();
    }
    
    /***********************************************************************/
    private void deleteCcaAuthBitdata() throws Exception {

        daoTable = "CCA_AUTH_BITDATA";
        whereStr = "WHERE TX_DATE < ? ";
        setString(1, targetAuTxDate);
        deleteTable();
    }
    
    /***********************************************************************/
    private void deleteCcaImsLog() throws Exception {

        daoTable = "cca_ims_log";
        whereStr = "WHERE crt_date < ? ";
        setString(1, targetAuTxDate);
        deleteTable();
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CcaAuiClearDB proc = new CcaAuiClearDB();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}

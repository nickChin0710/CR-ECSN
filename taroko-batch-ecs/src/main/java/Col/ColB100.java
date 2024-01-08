/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/29  V1.00.00    phopho     program initial                          *
*  108/11/29  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/13  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

public class ColB100 extends AccessDAO {
    private String progname = "JCIC S01 雙卡逾期3個月資料處理程式  109/12/13  V1.00.02  ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommRoutine    comr     = null;

    String hCallBatchSeqno = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;
    String hCurpModLog = "";

    String hTempSysdate = "";
    String hTempNextDate = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoPaymentNo = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAcctHolderId = "";
    String hAcnoIdPSeqno = "";
    long hAcctAcctJrnlBal = 0;
    int hAcnoIntRateMcode = 0;
    String hCjs1McodeStatus = "";
    String hCjs1ReportStatus = "";
    String hClbnLiabStatus = "";
    String hClbnRecolReason = "";
    String hClnoLiacStatus = "";
    String hClnoRecolReason = "";
    String hCjstReportStatus = "";

    int totalCnt = 0;

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
                comc.errExit("Usage : ColB100 [system_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            hTempSysdate = "";
            if (args.length == 1)
                hTempSysdate = args[0];
            selectPtrBusinday();

            if (!hTempNextDate.substring(6, 8).equals("01")) {
            	exceptExit = 0;
                comcr.errRtn(String.format("本程式為每月月底執行, 本日[%s], 次日[%s]\n", hTempSysdate, hTempNextDate), "",
                        hCallBatchSeqno);
            }
            deleteColJcicS01();
            deleteColJcicS0Sum();
            commitDataBase();
            selectActAcno();
            insertColJcicS0Sum();

            showLogMessage("I", "", String.format("Total record [%d]筆", totalCnt));
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
        sqlCmd = "select to_char(to_date(decode(cast(? as varchar(8)),'',to_char(sysdate,'yyyymmdd'),cast(? as varchar(8))),'yyyymmdd'),'yyyymmdd') h_temp_sysdate,";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(8)),'',to_char(sysdate,'yyyymmdd'),cast(? as varchar(8))),'yyyymmdd')+1 days,'yyyymmdd') h_temp_next_date ";
        sqlCmd += " from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hTempSysdate);
        setString(2, hTempSysdate);
        setString(3, hTempSysdate);
        setString(4, hTempSysdate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempSysdate = getValue("h_temp_sysdate");
            hTempNextDate = getValue("h_temp_next_date");
        }
    }

    /***********************************************************************/
    void deleteColJcicS01() throws Exception {
        daoTable = "col_jcic_s01";
        whereStr = "where report_month = substr(?,1,6) ";
        setString(1, hTempSysdate);
        deleteTable();
    }

    /***********************************************************************/
    void deleteColJcicS0Sum() throws Exception {
        daoTable = "col_jcic_s0_sum";
        whereStr = "where report_month = substr(?,1,6) ";
        whereStr += "and report_type = '1' ";
        setString(1, hTempSysdate);
        deleteTable();
    }

    /***********************************************************************/
    void insertColJcicS0Sum() throws Exception {
        sqlCmd = "insert into col_jcic_s0_sum ";
        sqlCmd += "(report_month,";
        sqlCmd += "report_type,";
        sqlCmd += "report_status,";
        sqlCmd += "status_cnt,";
        sqlCmd += "acct_jrnl_bal,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "report_month,";
        // sqlCmd += "max('1'),";
        sqlCmd += "'1',";
        sqlCmd += "report_status,";
        sqlCmd += "count(*),";
        sqlCmd += "sum(acct_jrnl_bal),";
        // sqlCmd += "max(sysdate),";
        // sqlCmd += "max('col_b100') ";
        sqlCmd += "sysdate,";
        sqlCmd += "? ";
        sqlCmd += "from col_jcic_s01 where report_month = substr(?,1,6) ";
        sqlCmd += "group by report_month, report_status ";
        setString(1, javaProgram);
        setString(2, hTempSysdate);
        insertTable();
        
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_col_jcic_s0_sum duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        int mCode = 0;
        sqlCmd = "select ";
//        sqlCmd += "b.p_seqno,";
        sqlCmd += "b.acno_p_seqno,";
        sqlCmd += "b.acct_type,";
        sqlCmd += "b.acct_key,";
        sqlCmd += "b.acct_status,";
        sqlCmd += "b.payment_no,";
        sqlCmd += "b.pay_by_stage_flag,";
        sqlCmd += "c.id_no acct_holder_id,";
        sqlCmd += "b.id_p_seqno,";
        sqlCmd += "a.acct_jrnl_bal,";
        sqlCmd += "b.int_rate_mcode ";
        sqlCmd += "from act_acct a,act_acno b, crd_idno c ";
//        sqlCmd += "where a.p_seqno = b.p_seqno ";
        sqlCmd += "where a.p_seqno = b.acno_p_seqno ";
        sqlCmd += "and b.id_p_seqno = c.id_p_seqno ";
        sqlCmd += "and (b.acct_status = '4' ";
        sqlCmd += "or decode(b.payment_rate1,'','00','0A','00', ";
        sqlCmd += "'0B','00','0C','00','0D','00', ";
        sqlCmd += "'0E','00',b.payment_rate1) > '02') ";
        sqlCmd += "and a.acct_jrnl_bal >0 ";
        //sqlCmd += "and a.acct_type not in ('02','03') ";
        sqlCmd += "and a.acct_type='01'";
        
        openCursor();
        while (fetchTable()) {
//            h_acno_p_seqno = getValue("p_seqno");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoPaymentNo = getValue("payment_no");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcnoAcctHolderId = getValue("acct_holder_id");
            hAcnoIdPSeqno = getValue("id_p_seqno");  //phopho add
            hAcctAcctJrnlBal = getValueLong("acct_jrnl_bal");
            hAcnoIntRateMcode = getValueInt("int_rate_mcode");

            hCjs1McodeStatus = "";
            hCjs1ReportStatus = "I";

            selectColLiacNego();

            if (!hAcnoAcctStatus.equals("4")) {
//                m_code = comr.getMcode(h_acno_acct_type, h_acno_p_seqno);
                mCode = hAcnoIntRateMcode;
                if (mCode < 3)
                    continue;
                hCjs1McodeStatus = "S";
                if (mCode > 5)
                    hCjs1McodeStatus = "T";
            } else if (hClnoLiacStatus.length() != 0) {
                selectActDebt();
                if (hAcctAcctJrnlBal == 0)
                    continue;
            }

            getReportStatus();
            totalCnt++;
            insertColJcicS01();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectColLiacNego() throws Exception {
        /* liac_status='4' && recol_reason='00' 毀諾復催 */
        hClnoLiacStatus = "";
        hClnoRecolReason = "";
        sqlCmd = "select liac_status,";
        sqlCmd += "recol_reason ";
        sqlCmd += "from col_liac_nego ";
//        sqlCmd += "where id_no = ? ";
        sqlCmd += "where id_p_seqno = ? ";
//        setString(1, h_acno_acct_holder_id);
        setString(1, hAcnoIdPSeqno);
        
        extendField = "col_liac_nego.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClnoLiacStatus = getValue("col_liac_nego.liac_status");
            hClnoRecolReason = getValue("col_liac_nego.recol_reason");
        }
    }

    /***********************************************************************/
    void selectActDebt() throws Exception {
        sqlCmd = "select sum(end_bal) h_acct_acct_jrnl_bal ";
        sqlCmd += " from act_debt ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and acct_code = 'DB' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_debt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcctAcctJrnlBal = getValueLong("act_debt.h_acct_acct_jrnl_bal");
        }
    }

    /***********************************************************************/
    void getReportStatus() throws Exception {
        if (hAcnoPayByStageFlag.equals("NE")) {
            hCjs1ReportStatus = "1";
            return;
        }
        if ((hAcnoPayByStageFlag.equals("NW"))
                || ((hAcnoPayByStageFlag.compareTo("00") >= 0) && (hAcnoPayByStageFlag.compareTo("99") <= 0)))
            hCjs1ReportStatus = "4";

        if (hAcnoPayByStageFlag.equals("NF"))
            hCjs1ReportStatus = "3";

        if ((hClnoLiacStatus.equals("1")) || (hClnoLiacStatus.equals("2"))) {
            hCjs1ReportStatus = "2";
            return;
        }
        if (hCjs1ReportStatus.equals("3"))
            return;

        if (hCjs1ReportStatus.equals("4")) {
            if ((hClnoLiacStatus.equals("4")) && (hClnoRecolReason.equals("00"))) {
                hCjs1ReportStatus = "5";
                return;
            }
            selectColLiabNego();
            if ((hClbnLiabStatus.equals("2")) && (hClbnRecolReason.equals("00"))) {
                hCjs1ReportStatus = "5";
            }
            return;
        }

        if (selectColJcicS0List() == 0)
            hCjs1ReportStatus = hCjstReportStatus;
    }

    /***********************************************************************/
    void selectColLiabNego() throws Exception {
        /* liab_status='2' && recol_reason='00' 毀諾復催 */
        hClbnLiabStatus = "";
        hClbnRecolReason = "";
        sqlCmd = "select liab_status,";
        sqlCmd += "recol_reason ";
        sqlCmd += "from col_liab_nego ";
//        sqlCmd += "where id_no = ? ";
        sqlCmd += "where id_p_seqno = ? ";
//        setString(1, h_acno_acct_holder_id);
        setString(1, hAcnoIdPSeqno);
        
        extendField = "col_liab_nego.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClbnLiabStatus = getValue("col_liab_nego.liab_status");
            hClbnRecolReason = getValue("col_liab_nego.recol_reason");
        }
    }

    /***********************************************************************/
    int selectColJcicS0List() throws Exception {
        int colJcicS0ListCnt = 0;
        hCjstReportStatus = "";
        sqlCmd = "select report_status ";
        sqlCmd += "from col_jcic_s0_list ";
//        sqlCmd += "where id_no = ? ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and report_type = '1' order by report_status ";
//        setString(1, h_acno_acct_holder_id);
        setString(1, hAcnoIdPSeqno);
        
        extendField = "col_jcic_s0_list.";
        
        colJcicS0ListCnt = selectTable();
        if (colJcicS0ListCnt == 0)
            return 1;
        if (colJcicS0ListCnt > 0) {
            hCjstReportStatus = getValue("col_jcic_s0_list.report_status");
        }
        return 0;
    }

    /***********************************************************************/
    void insertColJcicS01() throws Exception {
    	daoTable = "col_jcic_s01";
    	extendField = daoTable + ".";
//        setValue("report_month", sysDate);
        setValue(extendField+"report_month", comc.getSubString(hTempSysdate,0,6));
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"id_no", hAcnoAcctHolderId);
        setValue(extendField+"acct_status", hAcnoAcctStatus);
        setValue(extendField+"payment_no", hAcnoPaymentNo);
        setValue(extendField+"mcode_status", hCjs1McodeStatus);
        setValue(extendField+"report_status", hCjs1ReportStatus);
        setValueLong(extendField+"acct_jrnl_bal", hAcctAcctJrnlBal);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_col_jcic_s01 duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB100 proc = new ColB100();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

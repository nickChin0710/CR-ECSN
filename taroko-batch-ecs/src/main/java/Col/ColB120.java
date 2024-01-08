/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/29  V1.00.00    phopho     program initial                          *
*  108/12/02  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/13  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB120 extends AccessDAO {
    private String progname = "JCIC S02 前協與債協毀諾戶資料處理程式 109/12/13  V1.00.02 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;
    String hCurpModLog = "";
    String hCallRProgramCode = "";

    String hTempSysdate = "";
    String hTempNextDate = "";
    String hClnoId = "";
    String hClnoIdPSeqno = "";
    String hAcnoPaymentNo = "";
    String hCjs2NegoType = "";
    String hCjs2ReportStatus = "";
    long hAcctAcctJrnlBal = 0;
    int hCnt = 0;
    String hAcnoAcctStatus = "";
    String hAcnoIdPSeqno = "";
    int hTempStatus1 = 0;
    int hTempStatus2 = 0;
    int hTempStatus3 = 0;
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
                comc.errExit("Usage : ColB120 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempSysdate = "";
            if (args.length == 1)
                hTempSysdate = args[0];
            selectPtrBusinday();
            if (!hTempNextDate.substring(6, 8).equals("01")) {
            	exceptExit = 0;
                comcr.errRtn(String.format("本程式為每月月底執行, 本日[%s], 次日[%s]\n", hTempSysdate, hTempNextDate), "",
                        hCallBatchSeqno);
            }

            deleteColJcicS02();
            deleteColJcicS0Sum();
            commitDataBase();
            selectColLiabNego();
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
        sqlCmd += " from ptr_businday  ";
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
    void deleteColJcicS02() throws Exception {
        daoTable = "col_jcic_s02";
        whereStr = "where report_month = substr(?,1,6) ";
        setString(1, hTempSysdate);
        deleteTable();

    }

    /***********************************************************************/
    void deleteColJcicS0Sum() throws Exception {
        daoTable = "col_jcic_s0_sum";
        whereStr = "where report_month = substr(?,1,6)  ";
        whereStr += "and report_type = '2' ";
        setString(1, hTempSysdate);
        deleteTable();

    }

    /***********************************************************************/
    void selectColLiabNego() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_no, ";
        sqlCmd += "id_p_seqno ";
        sqlCmd += "from col_liab_nego ";
        sqlCmd += "where liab_status = '2' ";
        sqlCmd += "and recol_reason = '00' ";
        sqlCmd += "UNION ";
        sqlCmd += "select id_no, ";
        sqlCmd += "id_p_seqno ";
        sqlCmd += "from col_liac_nego ";
        sqlCmd += "where liac_status = '4' ";
        sqlCmd += "and recol_reason = '00' ";

        openCursor();
        while (fetchTable()) {
            hClnoId = getValue("id_no");
            hClnoIdPSeqno = getValue("id_p_seqno");

            hCjs2ReportStatus = "8";

            totalCnt++;
            selectActAcno();
            if (hAcnoPaymentNo.length() == 0)
                continue;
            hCjs2NegoType = "1";
            if (hAcctAcctJrnlBal == 0) {
                hCjs2ReportStatus = "1";
                insertColJcicS02();
                continue;
            }
            if (selectColLiacNego() == 0) {
                hCjs2NegoType = "2";
                if (hAcnoAcctStatus.equals("4"))
                    selectActDebt();
                if (hAcctAcctJrnlBal == 0) {
                    hCjs2ReportStatus = "1";
                    insertColJcicS02();
                    continue;
                }
            }
            if (hTempStatus1 == 0) {
                if (hTempStatus3 == 0) {
                    if (hTempStatus2 == 0) {
                        hCjs2ReportStatus = "2";
                    } else {
                        hCjs2ReportStatus = "3";
                    }
                    insertColJcicS02();
                    continue;
                }
            }

            if (selectColJcicS0List() == 0)
                hCjs2ReportStatus = hCjstReportStatus;
            insertColJcicS02();
        }
        closeCursor();
    }

    /***********************************************************************/
    void insertColJcicS0Sum() throws Exception {
        sqlCmd = "insert into col_jcic_s0_sum (";
        sqlCmd += "report_month,";
        sqlCmd += "report_type,";
        sqlCmd += "report_status,";
        sqlCmd += "status_cnt,";
        sqlCmd += "acct_jrnl_bal,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "report_month,";
        // sqlCmd += "max('2'),";
        sqlCmd += "'2',";
        sqlCmd += "report_status,";
        sqlCmd += "count(*),";
        sqlCmd += "sum(acct_jrnl_bal),";
        // sqlCmd += "max(sysdate),";
        // sqlCmd += "max('col_b120') ";
        sqlCmd += "sysdate,";
        sqlCmd += "? ";
        sqlCmd += "from col_jcic_s02 where report_month = substr(?,1,6) ";
        sqlCmd += "group by report_month, report_status ";
        setString(1, javaProgram);
        setString(2, hTempSysdate);
        insertTable();
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hAcnoAcctStatus = "";
        hAcnoPaymentNo = "";
        hTempStatus1 = 0;
        hTempStatus2 = 0;
        hTempStatus3 = 0;
        hAcctAcctJrnlBal = 0;

//        sqlCmd = "select max(b.acct_status) acct_status,";
//        sqlCmd += "min(b.payment_no) payment_no,";
//        sqlCmd += "max(b.id_p_seqno) id_p_seqno,";
//        sqlCmd += "min(case when ((b.pay_by_stage_flag='NW') or ((b.pay_by_stage_flag>='00') and (b.pay_by_stage_flag<='99'))) then 0 else 1 end) h_temp_status_1,";
//        sqlCmd += "min(case when ((b.pay_by_stage_flag='NW') or ((b.pay_by_stage_flag>='00') and (b.pay_by_stage_flag<='99'))) then decode(sign(months_between(to_date(decode(b.revolve_rate_e_month,'','300001',b.revolve_rate_e_month),'yyyymm'), to_date(decode(b.revolve_rate_s_month,'','300001',b.revolve_rate_s_month),'yyyymm'))-60),-1,0,1) else 1 end) h_temp_status_2,";
//        sqlCmd += "min(decode(b.revolve_rate_s_month,'',1,decode(b.revolve_rate_e_month,'',0, decode(sign(?-(b.revolve_rate_e_month||b.stmt_cycle)),1,1,0)))) h_temp_status_3,";
//        sqlCmd += "sum(acct_jrnl_bal) acct_jrnl_bal ";
//        sqlCmd += " from act_acct a,act_acno b, crd_idno c ";
////        sqlCmd += "where a.p_seqno  = b.p_seqno ";
//        sqlCmd += "where a.p_seqno  = b.acno_p_seqno ";
//        sqlCmd += "and b.acno_flag <> 'Y'  ";
//        sqlCmd += "and c.id_no = ? ";
//        sqlCmd += "and b.id_p_seqno = c.id_p_seqno ";
//        setString(1, h_temp_sysdate);
//        setString(2, h_clno_id);
        
        sqlCmd = "select max(b.acct_status) acct_status,";
        sqlCmd += "min(b.payment_no) payment_no,";
        sqlCmd += "min(case when ((b.pay_by_stage_flag='NW') or ((b.pay_by_stage_flag>='00') and (b.pay_by_stage_flag<='99'))) then 0 else 1 end) h_temp_status_1,";
        sqlCmd += "min(case when ((b.pay_by_stage_flag='NW') or ((b.pay_by_stage_flag>='00') and (b.pay_by_stage_flag<='99'))) then decode(sign(months_between(to_date(decode(b.revolve_rate_e_month,'','300001',b.revolve_rate_e_month),'yyyymm'), to_date(decode(b.revolve_rate_s_month,'','300001',b.revolve_rate_s_month),'yyyymm'))-60),-1,0,1) else 1 end) h_temp_status_2,";
        sqlCmd += "min(decode(b.revolve_rate_s_month,'',1,decode(b.revolve_rate_e_month,'',0, decode(sign(?-(b.revolve_rate_e_month||b.stmt_cycle)),1,1,0)))) h_temp_status_3,";
        sqlCmd += "sum(acct_jrnl_bal) acct_jrnl_bal ";
        sqlCmd += " from act_acct a, act_acno b ";
        sqlCmd += "where a.p_seqno  = b.acno_p_seqno ";
        sqlCmd += "and b.acno_flag <> 'Y'  ";
        sqlCmd += "and b.id_p_seqno = ? ";
        setString(1, hTempSysdate);
        setString(2, hClnoIdPSeqno);
        
        extendField = "act_acno.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcnoAcctStatus = getValue("act_acno.acct_status");
            hAcnoPaymentNo = getValue("act_acno.payment_no");
            hTempStatus1 = getValueInt("act_acno.h_temp_status_1");
            hTempStatus2 = getValueInt("act_acno.h_temp_status_2");
            hTempStatus3 = getValueInt("act_acno.h_temp_status_3");
            hAcctAcctJrnlBal = getValueLong("act_acno.acct_jrnl_bal");
//            h_acno_id_p_seqno = getValue("id_p_seqno");
        }

        if (hAcctAcctJrnlBal < 0)
            hAcctAcctJrnlBal = 0;
    }

    /***********************************************************************/
    int selectColLiacNego() throws Exception {
        int hCnt = 0;
        sqlCmd = "select 1 h_cnt ";
        sqlCmd += "from col_liac_nego ";
//        sqlCmd += "where id_no = ? ";
//        setString(1, h_clno_id);
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hClnoIdPSeqno);
        
        extendField = "col_liac_nego.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCnt = getValueInt("col_liac_nego.h_cnt");
        } else
            return 1;
        return 0;
    }

    /***********************************************************************/
    void selectActDebt() throws Exception {
        hAcctAcctJrnlBal = 0;
        sqlCmd = "select sum(a.end_bal) end_bal ";
        sqlCmd += " from act_debt a, act_acno b ";
//        sqlCmd += "where a.p_seqno = b.p_seqno ";
        sqlCmd += "where a.p_seqno = b.acno_p_seqno ";
        sqlCmd += "and a.acct_code = 'DB'  ";
        sqlCmd += "and b.id_p_seqno = ? ";
//        setString(1, h_clno_id);
        setString(1, hClnoIdPSeqno);
        
        extendField = "act_debt.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_debt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcctAcctJrnlBal = getValueLong("act_debt.end_bal");
        }
    }

    /***********************************************************************/
    int selectColJcicS0List() throws Exception {
        int colJcicS0ListCnt = 0;
        hCjstReportStatus = "";
        sqlCmd = "select report_status ";
        sqlCmd += "from col_jcic_s0_list ";
//        sqlCmd += "where id_no = ?  ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and report_type = '2' order by report_status ";
//        setString(1, h_clno_id);
        setString(1, hClnoIdPSeqno);
        
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
    void insertColJcicS02() throws Exception {
    	daoTable = "col_jcic_s02";
    	extendField = daoTable + ".";
//        setValue("report_month", sysDate.substring(0, 6));
        setValue(extendField+"report_month", comc.getSubString(hTempSysdate,0,6));
        setValue(extendField+"id_p_seqno", hClnoIdPSeqno);
        setValue(extendField+"id_no", hClnoId);
        setValue(extendField+"nego_type", hCjs2NegoType);
        setValue(extendField+"payment_no", hAcnoPaymentNo);
        setValue(extendField+"report_status", hCjs2ReportStatus);
        setValueLong(extendField+"acct_jrnl_bal", hAcctAcctJrnlBal);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_col_jcic_s02 duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB120 proc = new ColB120();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/03  V1.00.00    phopho     program initial                          *
*  109/12/10  V1.00.01    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*前置協商受理申請狀態統計處理程式*/
public class ColA426 extends AccessDAO {
    private String progname = "前置協商受理申請狀態統計處理程式  109/12/10  V1.00.01  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hCcddLiacSeqno = "";
    String hCcddNotifyDate = "";
    String hCcddAcctStatus = "";
    int hClsaMcode = 0;
    int hClsaLiacIdCnt = 0;
    double hClsaTotAmt = 0;
    String hCcddId = "";
    int totalCnt = 0;
    String hCcddMcode = "";
    double hCcddTotAmt = 0;
    int hTempCnt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 3) {
                comc.errExit("Usage : ColA426 [business_date] [start_month]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            if ((args.length == 2) && (args[1].length() == 8))
                hBusiBusinessDate = args[1];
            selectPtrBusinday();

            deleteColLiacStatApply();

            totalCnt = 0;
            selectColLiacDebtDtl0();


            showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));
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
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
    }

    /***********************************************************************/
    void deleteColLiacStatApply() throws Exception {
        daoTable = "col_liac_stat_apply";
        whereStr = "where static_day = ? ";
        setString(1, hBusiBusinessDate);
        deleteTable();
    }

    /***********************************************************************/
    void selectColLiacDebtDtl0() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_no,";
        sqlCmd += "liac_seqno,";
        sqlCmd += "min(notify_date) h_ccdd_notify_date ";
        sqlCmd += "from col_liac_debt_dtl a ";
        sqlCmd += "where apply_date = (select max(apply_date) ";
        sqlCmd += "from col_liac_nego_hst ";
        sqlCmd += "where id_no = a.id_no) ";
        sqlCmd += "group by id_no,liac_seqno ";

        openCursor();
        while (fetchTable()) {
            hCcddId = getValue("id_no");
            hCcddLiacSeqno = getValue("liac_seqno");
            hCcddNotifyDate = getValue("h_ccdd_notify_date");

            selectColLiacDebtDtl();

            totalCnt++;
            if ((totalCnt % 1000) == 0)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectColLiacDebtDtl() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "max(decode(acct_status,'4','4','1')) h_ccdd_acct_status,";
        sqlCmd += "max(decode(acct_status,'4',0,decode(mcode,0,0,1,1,2))) h_ccdd_mcode,";
        sqlCmd += "sum(decode(sign(tot_amt),-1,0,tot_amt)) h_ccdd_tot_amt,";
        sqlCmd += "count(*) h_temp_cnt ";
        sqlCmd += "from col_liac_debt_dtl ";
        sqlCmd += "where liac_seqno = ? ";
        sqlCmd += " order by h_ccdd_acct_status desc, h_ccdd_mcode desc ";
        setString(1, hCcddLiacSeqno);

        extendField = "col_liac_debt_dtl.";
        
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCcddAcctStatus = getValue("col_liac_debt_dtl.h_ccdd_acct_status",i);
            hCcddMcode = getValue("col_liac_debt_dtl.h_ccdd_mcode",i);
            hCcddTotAmt = getValueDouble("col_liac_debt_dtl.h_ccdd_tot_amt",i);
            hTempCnt = getValueInt("col_liac_debt_dtl.h_temp_cnt",i);
            hClsaMcode = getValueInt("col_liac_debt_dtl.h_ccdd_mcode",i);
            hClsaTotAmt = getValueDouble("col_liac_debt_dtl.h_ccdd_tot_amt",i);

            if (hTempCnt == 0)
                continue;
            hClsaLiacIdCnt = 0;
            if (i == 0)
                hClsaLiacIdCnt = 1;

            if (updateColLiacStatApply() != 0)
                insertColLiacStatApply();

        }
    }

    /***********************************************************************/
    void insertColLiacStatApply() throws Exception {
    	daoTable = "col_liac_stat_apply";
    	extendField = daoTable + ".";
        setValue(extendField+"static_month", comc.getSubString(hCcddNotifyDate,0,6));
        setValue(extendField+"static_day", hBusiBusinessDate);
        setValue(extendField+"acct_status", hCcddAcctStatus);
        setValueInt(extendField+"mcode", hClsaMcode);
        setValueInt(extendField+"liac_id_cnt", hClsaLiacIdCnt);
        setValueDouble(extendField+"tot_amt", hClsaTotAmt);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liac_stat_apply duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    int updateColLiacStatApply() throws Exception {
        daoTable = "col_liac_stat_apply";
        updateSQL = "liac_id_cnt = liac_id_cnt + ?,";
        updateSQL += " tot_amt = tot_amt + ?";
        whereStr = "where static_month = substr(?,1,6) ";
        whereStr += "and static_day = ? ";
        whereStr += "and acct_status = ? ";
        whereStr += "and mcode = ? ";
        setInt(1, hClsaLiacIdCnt);
        setDouble(2, hClsaTotAmt);
        setString(3, hCcddNotifyDate);
        setString(4, hBusiBusinessDate);
        setString(5, hCcddAcctStatus);
        setInt(6, hClsaMcode);
        updateTable();
        if (notFound.equals("Y")) {
            return 1;
        }

        return 0;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA426 proc = new ColA426();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-13  V1.00.01    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*人工指定轉取消代行授權明細檔處理程式*/
public class TscB024 extends AccessDAO {
    private final String progname = "人工指定轉取消代行授權明細檔處理程式   109/11/13 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTregCardNo = "";
    String hTardTscCardNo = "";
    String hTregSecuCode = "";
    String hTregModUser = "";
    String hTregRowid = "";
    String hTrahRetrRefNo1 = "";
    String hTrahRetrRefNo2 = "";
    String hTrahRptRespCode = "";
    String hTrahRiskClass = "";
    String hTrahSendReason = "";
    String hInt = "";

    int totalCnt = 0;
    int rtCode = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : TscB024 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            selectPtrBusinday();

            totalCnt = 0;
            selectTscRefuseLog();
            showLogMessage("I", "", String.format("累計 [%d] 筆\n", totalCnt));

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
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                    : hBusiBusinessDate;
        }

    }

    /***********************************************************************/
    void selectTscRefuseLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.secu_code,";
        sqlCmd += "a.crt_user,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from tsc_refuse_log a ";
        sqlCmd += "where a.send_date = '' ";
        sqlCmd += " order by a.mod_time ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTregCardNo     = getValue("card_no", i);            
            hTregSecuCode   = getValue("secu_code", i);
            hTregModUser    = getValue("crt_user", i);
            hTregRowid      = getValue("rowid", i);

            totalCnt++;
            
            selectTscCard();
            hTrahSendReason = "40";
            rtCode = selectTscRmActauth();
            if ((!hTrahRptRespCode.equals("00")) && (hTrahRptRespCode.length() > 0))
                updateTscRmActautha();

            switch (hTregSecuCode.toCharArray()[0]) {
            case '1':
                deleteTscRmActauth();
                insertTscRmActauth(1);
                break;
            case '2':
                deleteTscRmActauth();
                insertTscRmActauth(2);
                break;
            case '3':
                updateTscRmActauth(1);
                break;
            case '4':
                updateTscRmActauth(2);
                break;
            }
            updateTscRefuseLog();
        }

    }

    /***********************************************************************/
    void selectTscCard() throws Exception {

        sqlCmd = "select tsc_card_no ";
        sqlCmd += " from tsc_card ";
        sqlCmd += "where card_no = ? ";
        sqlCmd += "order by new_end_date desc,current_code fetch first 1 row only ";
        setString(1, hTregCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hTardTscCardNo = getValue("tsc_card_no");
        } 
    }
    
    /***********************************************************************/
    int selectTscRmActauth() throws Exception {
        hTrahRetrRefNo1 = "";
        hTrahRetrRefNo2 = "";
        hTrahRptRespCode = "";
        hTrahRiskClass = "";

        sqlCmd = "select retr_ref_no_1,";
        sqlCmd += "retr_ref_no_2,";
        sqlCmd += "decode(rpt_resp_code,'0000','00',rpt_resp_code) h_trah_rpt_resp_code,";
        sqlCmd += "risk_class ";
        sqlCmd += " from tsc_rm_actauth  ";
        sqlCmd += "where tsc_card_no = ?  ";
        sqlCmd += "  and send_reason = ? ";
        setString(1, hTardTscCardNo);
        setString(2, hTrahSendReason);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTrahRetrRefNo1 = getValue("retr_ref_no_1");
            hTrahRetrRefNo2 = getValue("retr_ref_no_2");
            hTrahRptRespCode = getValue("h_trah_rpt_resp_code");
            hTrahRiskClass    = getValue("risk_class");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void updateTscRmActautha() throws Exception {
        daoTable = "tsc_rm_actauth";
        updateSQL = "rpt_resp_code = '',";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm  = ?";
        whereStr = "where tsc_card_no = ? ";
        setString(1, javaProgram);
        setString(2, hTardTscCardNo);
        updateTable();
    }

    /***********************************************************************/
    void deleteTscRmActauth() throws Exception {
        daoTable  = "tsc_rm_actauth";
        whereStr  = "where tsc_card_no = ?  ";
        whereStr += "  and send_reason = ? ";
        setString(1, hTardTscCardNo);
        setString(2, hTrahSendReason);
        deleteTable();
    }

    /***********************************************************************/
    int insertTscRmActauth(int hInt) throws Exception {
        sqlCmd = "insert into tsc_rm_actauth ";
        sqlCmd += "(send_reason,";
        sqlCmd += "risk_class,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "remove_date,";
        sqlCmd += "card_no,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "new_end_date,";
        sqlCmd += "mod_user,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "decode(cast(? as int),1,'57','04'),";
        sqlCmd += "b.tsc_card_no,";
        sqlCmd += "to_char(sysdate,'yyyymmdd'),";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "b.new_end_date,";
        sqlCmd += "?,";
        sqlCmd += "sysdate,";
        sqlCmd += "? ";
        sqlCmd += "from crd_card a,tsc_card b where a.card_no  = b.card_no and b.tsc_card_no = ? ";
        setString(1, hTrahSendReason);
        setInt(2, hInt);
        setString(3, hTregModUser);
        setString(4, javaProgram);
        setString(5, hTardTscCardNo);
        insertTable();
        if (dupRecord.equals("Y")) {
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateTscRmActauth(int hInt) throws Exception {
        daoTable   = "tsc_rm_actauth";
        updateSQL  = " restore_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " mod_user     = ?,";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ? ";
        whereStr   = "where tsc_card_no = ?  ";
        whereStr  += "  and risk_class  = decode(cast(? as int),1,'57','04')  ";
        whereStr  += "  and send_reason = ? ";
        setString(1, hTregModUser);
        setString(2, javaProgram);
        setString(3, hTardTscCardNo);
        setInt(4, hInt);
        setString(5, hTrahSendReason);
        updateTable();

    }

    /***********************************************************************/
    void updateTscRefuseLog() throws Exception {
        daoTable   = "tsc_refuse_log";
        updateSQL  = "send_date   = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " mod_time   = sysdate,";
        updateSQL += " mod_pgm    = ?";
        whereStr   = "where rowid = ? ";
        setString(1, javaProgram);
        setRowId(2, hTregRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_refuse_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscB024 proc = new TscB024();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

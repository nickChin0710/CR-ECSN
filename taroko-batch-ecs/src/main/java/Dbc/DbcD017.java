/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/10/15  V1.00.01    Wilson    改每日執行、執行日為系統日                                                                    *
 *  109-10-19  V1.00.02    shiyuqi       updated for project coding standard     *
 *  112/08/04  V1.00.03    Wilson    停用原因碼調整                                                                                         *
 *  112/10/16  V1.00.04    Wilson    不為該月一日不執行                                                                                  *
 ******************************************************************************/

package Dbc;


import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*到期變更主檔狀態碼*/
public class DbcD017 extends AccessDAO {
    private String progname = "到期變更主檔狀態碼  112/10/16 V1.00.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "DbcD017";
    String hCallBatchSeqno = "";

    String hFirstDay = "";
    String hProcDate = "";
    String hSysdate = "";
    String hDccdCardNo = "";
    String hDccdSupFlag = "";
    String hDccdGroupCode = "";
    String hDccdSourceCode = "";
    String hDccdCardType = "";
    String hDccdAcctType = "";
    String hDccdPSeqno = "";
    String hDccdIdPSeqno = "";
    String hDccdNewBegDate = "";
    String hDccdNewEndDate = "";
    String hDccdCorpNo = "";
    String hDccdMajorCardNo = "";
    String hDccdMajorIdPSeqno = "";
    String hDccdMailType = "";
    String hDccdMailBranch = "";
    String hDccdMailNo = "";
    String hDccdMailProcDate = "";
    String hDccdExpireChgFlag = "";
    String hDccdRowid = "";
    String hReason = "";
    int totCnt = 0;
    private String hModUser = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : DbcD017 [yyyymm]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hFirstDay = "";
            sqlCmd = "select to_char(sysdate,'yyyymm')||'01' h_yesterday,";
            sqlCmd += "to_char(sysdate,'yyyymmdd') h_sysdate ";
            sqlCmd += " from dual ";
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hFirstDay = getValue("h_yesterday");
                hProcDate = getValue("h_sysdate");
                hSysdate = hProcDate;
            }
            if (args.length == 0) {
                if (!hFirstDay.equals(hProcDate)) {
                	showLogMessage("I", "", String.format("今天[%s]不為本月一號,不需跑程式，程式執行結束", hProcDate));                  
                	finalProcess();                  
                	return 0;              	
                }
            }
            
            if (args.length == 1) {
                if (args[0].length() != 6) {
                    comcr.errRtn("Usage : DbcD017 [yyyymm]", "", hCallBatchSeqno);
                }
                hProcDate = String.format("%6.6s01", args[0]);
            }

            showLogMessage("I", "", String.format("執行日期=[%s] ", hProcDate));

            hModUser = comc.commGetUserID();

            sqlCmd = "select ";
            sqlCmd += "card_no,";
            sqlCmd += "sup_flag,";
            sqlCmd += "group_code,";
            sqlCmd += "source_code,";
            sqlCmd += "card_type,";
            sqlCmd += "acct_type,";
            sqlCmd += "p_seqno,";
            sqlCmd += "id_p_seqno,";
            sqlCmd += "new_beg_date,";
            sqlCmd += "new_end_date,";
            sqlCmd += "corp_no,";
            sqlCmd += "major_card_no,";
            sqlCmd += "major_id_p_seqno,";
            sqlCmd += "mail_type,";
            sqlCmd += "mail_branch,";
            sqlCmd += "mail_no,";
            sqlCmd += "mail_proc_date,";
            sqlCmd += "expire_chg_flag,";
            sqlCmd += "rowid as rowid ";
            sqlCmd += "from dbc_card ";
            sqlCmd += "where current_code = '0' ";
            sqlCmd += "and new_end_date < ? ";
            setString(1, hProcDate);
            recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                hDccdCardNo = getValue("card_no", i);
                hDccdSupFlag = getValue("sup_flag", i);
                hDccdGroupCode = getValue("group_code", i);
                hDccdSourceCode = getValue("source_code", i);
                hDccdCardType = getValue("card_type", i);
                hDccdAcctType = getValue("acct_type", i);
                hDccdPSeqno = getValue("p_seqno", i);
                hDccdIdPSeqno = getValue("id_p_seqno", i);
                hDccdNewBegDate = getValue("new_beg_date", i);
                hDccdNewEndDate = getValue("new_end_date", i);
                hDccdCorpNo = getValue("corp_no", i);
                hDccdMajorCardNo = getValue("major_card_no", i);
                hDccdMajorIdPSeqno = getValue("major_id_p_seqno", i);
                hDccdMailType = getValue("mail_type", i);
                hDccdMailBranch = getValue("mail_branch", i);
                hDccdMailNo = getValue("mail_no", i);
                hDccdMailProcDate = getValue("mail_proc_date", i);
                hDccdExpireChgFlag = getValue("expire_chg_flag", i);
                hDccdRowid = getValue("rowid", i);

                totCnt++;
                if (totCnt % 1000 == 0 || totCnt == 1)
                    showLogMessage("I", "", String.format("crd Process 1 record=[%d]\n", totCnt));

                hReason = "B3";
                if (hDccdExpireChgFlag.compareTo("0") > 0) {
                    switch (Integer.parseInt(hDccdExpireChgFlag)) {
                    case 1:
                        hReason = "B3";
                        break;
                    case 2:
                        hReason = "B1";
                        break;
                    case 3:
                        hReason = "B2";
                        break;
                    }
                }
                updateDbcCard();
                insertOnbat();

            }
            showLogMessage("I", "", String.format("程式執行結束,筆數=[%d]", totCnt));
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
    void updateDbcCard() throws Exception {

        daoTable = "dbc_card";
        updateSQL = "current_code = '1',";
        updateSQL += " oppost_reason = ?,";
        updateSQL += " oppost_date = ?,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid   = ? ";
        setString(1, hReason);
        setString(2, hSysdate);
        setString(3, hModUser);
        setString(4, prgmId);
        setRowId(5, hDccdRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dbc_card not found!", "", hCallBatchSeqno);
        }

        return;
    }

    /***********************************************************************/
    void insertOnbat() throws Exception {

        setValue("trans_type"    , "6");
        setValueInt("to_which"   , 2);
        setValue("dog"           , sysDate + sysTime);
     // setValue("dop"           , null);
        setValue("proc_mode"     , "B");
        setValueInt("proc_status", 0);
        setValue("card_no"       , hDccdCardNo);
        setValue("opp_type"      , "1");
        setValue("opp_reason"    , hReason);
        setValue("opp_date"      , sysDate);
        daoTable = "onbat_2ccas";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_onbat_2ccas duplicate!", "", hCallBatchSeqno);
        }

        return;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcD017 proc = new DbcD017();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

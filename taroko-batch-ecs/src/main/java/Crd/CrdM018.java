/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------  *
*  109/01/07  V1.00.00    Rou       initial (Reference CrdM017 program)     *
*  109/12/23  V1.00.01   shiyuqi       updated for project coding standard   *
****************************************************************************/

package Crd;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class CrdM018 extends AccessDAO {
    public static final boolean debugMode = false;

    private String progname = "每日批次連動停用貴賓卡程式   109/12/23  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    String hTempUser = "";

    String prgmId = "CrdM018";
    String hModUser = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";

    String hYesterday = "";
    String hBusinessDate = "";
    String hSysdate = "";
    String hCrdpPpCardNo = "";
    String hCrdpRowid = "";
    String hCrdpIdPSeqno = "";

    int totCnt = 0;
    int temp = 0;
    int tempInt = 0;

    // ********************************************************

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
                comc.errExit("Usage : CrdM018 [yyyymmdd]", "");
            }

            // 固定要做的
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }
            if (args.length > 1) {
                hYesterday = args[0];
            }

            sqlCmd = "select business_date, ";
            sqlCmd += " to_char(sysdate,'yyyymmdd') h_sys_date ";
            sqlCmd += " from ptr_businday ";
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hBusinessDate = getValue("business_date");
                hSysdate = getValue("h_sys_date");
            }

            hModPgm = javaProgram;
            hModUser = comc.commGetUserID();

            showLogMessage("I", "", String.format("程式開始執行"));

            sqlCmd = "select ";
            sqlCmd += "pp_card_no, id_p_seqno,";
            sqlCmd += "rowid  rowid ";
            sqlCmd += " from crd_card_pp ";
            sqlCmd += "where current_code = '0' ";
            recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                hCrdpPpCardNo = getValue("pp_card_no", i);                
                hCrdpRowid = getValue("rowid", i);
                hCrdpIdPSeqno = getValue("id_p_seqno", i);
                
                if(debug ==1)
                	showLogMessage("I", "", "pp_card_no = [" + hCrdpPpCardNo + "] , id_p_seqno = [" + hCrdpIdPSeqno + "]");
                
                temp = chkCrdCard();
                totCnt++;
                if (temp > 0)
                	continue;
             
                updateCrdCardPp();

            }

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    
    /***********************************************************************/   
    int chkCrdCard() throws Exception {
    	tempInt = 0;
        sqlCmd = " select count(*) temp_int ";
        sqlCmd += " from crd_card ";
        sqlCmd += " where id_p_seqno   = ? ";
        sqlCmd += "  and card_type   in (select card_type from mkt_ppcard_apply) ";
        sqlCmd += "  and ((current_code = '0') or (reissue_status in ('1', '2'))) ";
        setString(1, hCrdpIdPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) 
            tempInt = getValueInt("temp_int");
        
        return (tempInt);
    }

    /***********************************************************************/
    void updateCrdCardPp() throws Exception {
        daoTable   = "crd_card_pp ";
        updateSQL  = "current_code  = '1', "; /* 一般申停 */
        updateSQL += "oppost_reason = 'B2', ";
        updateSQL += "oppost_date   = ?, ";
        updateSQL += "mod_user      = ?, ";
        updateSQL += "mod_time      = sysdate, ";
        updateSQL += "mod_pgm       = ? ";
        whereStr   = "where rowid   = ? ";
        setString(1, hSysdate);
        setString(2, hModUser);
        setString(3, prgmId);
        setRowId(4, hCrdpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_card_pp  not found!", "", comcr.hCallBatchSeqno);
        }

        return;
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdM018 proc = new CrdM018();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.01.00    Edson     program initial                           *
*  109/11/19  V1.01.02  yanghan       修改了變量名稱和方法名稱                                                                              *                                                                             *
******************************************************************************/

package Gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

@SuppressWarnings("unchecked")
/* 日結還原作業 */
public class GenA005 extends AccessDAO {
    // 一定要有
    private String progname = "日結還原作業  109/11/19  V1.01.02";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    int totCnt = 0;
    String tempStr = "";

    String prgmId = "GenA005";
    int actCnt = 0;
    String hCallBatchSeqno = "";
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq = 0;
    int errCnt = 0;
    String errMsg = "";

    int tempInt = 0;
    String hTempUser = "";
    String hBusinessDate = "";
    String hVouchDate = "";
    String hSystemDate = "";
    String hVoucTxDate = "";
    String hVoucRefno = "";	
    String hVoucCurr = "";
    String hVoucUserId = "";
    String hVoucManager = "";
    String hVoucJrnStatus = "";
    String hVoucBrno = "";
    String hVoucModPgm = "";
    String hVoucModLog = "";
    String hDate = "";
    String hTime = "";
    int hVoucVoucherCnt = 0;
    double hAmtDr = 0;
    double hAmtCr = 0;
    long hModSeqno = 0;
    String hErrorCode = "";
    String hErrorDesc = "";
    String hPrintName = "";
    String hRptName = "";
    private String hModPgm;
    private String hVoucModTime;
    private String hVoucModUser;
    private long hVoucModSeqno;
    private String hModUser;
    private String hModTime;

    public static void main(String[] args) throws Exception {
        GenA005 proc = new GenA005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // *************************************************************************************

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
                comc.errExit("Usage : GenA005 callbatch_seqno", Integer.toString(args.length));
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                comcr.hCallBatchSeqno = "no-call";
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

            commonRtn();

            if (args.length > 0) {
                if (args[0].length() == 8) {
                    hBusinessDate = args[0];
                }
            }
showLogMessage("I","","處理日期=["+hBusinessDate+"]"+args.length+","+args[0].length());

            hModPgm = prgmId;
            hVoucModPgm = hModPgm;
            hVoucModTime = hModTime;
            hVoucModUser = hModUser;
            hVoucModSeqno = hModSeqno;

            daoTable  = "gen_vouch ";
            updateSQL = "post_flag     = 'N' ";
            whereStr  = "where decode(post_flag, '', 'N', post_flag) = 'Y' ";
            whereStr += "  and tx_date = ? ";
            setString(1, hBusinessDate);
            updateTable();
            if (notFound.equals("Y")) {
                String err1 = "update_" + daoTable + " error";
                String err2 = hBusinessDate;
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }

            daoTable   = "ptr_businday ";
            updateSQL  = "vouch_chk_flag   = 'N', ";
            updateSQL += "vouch_close_flag = 'N' ";
            updateTable();
            if (notFound.equals("Y")) {
                String err1 = "update1 " + daoTable + " error";
                String err2 = hBusinessDate;
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束";
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
    void commonRtn() throws Exception {
        selectSQL = "business_date,vouch_date ";
        daoTable = "ptr_businday";

        if (selectTable() > 0) {
            hBusinessDate = getValue("business_date");
            hVouchDate = getValue("vouch_date");
        }

        // =============================
        selectSQL = "to_char(sysdate,'yyyymmdd') date1";
        daoTable = "dual";

        if (selectTable() > 0) {
            hSystemDate = getValue("date1");
        }

        hModSeqno = comcr.getModSeq();
        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }
    // ********************************************************************************
}

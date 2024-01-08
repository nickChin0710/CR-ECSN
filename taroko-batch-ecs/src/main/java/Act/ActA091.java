/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/20  V1.00.01    SUP       error correction                          *
  *  109/11/13  V1.00.02    shiyuqi       updated for project coding standard     *
 ******************************************************************************/

package Act;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*申停未入帳自動D除年費處理程式*/
public class ActA091 extends AccessDAO {

    public static final boolean debugMode = false;

    private String progname = "申停未入帳自動D除年費處理程式  109/01/17  V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActA091";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hCardPSeqno = "";
    String hCardCardNo = "";
    String hCardNewEndDate = "";
    String hCardAcctType = "";
    String hCardOppostDate = "";
    String hCardRowid = "";
    String hCfeeRowid = "";
    String hDebtEndBal = "";
    int hTempExistCnt = 0;
    double hTempMinusAmt = 0;
    double hTempNativeAmt = 0;
    String hTempDOppostDateS = ""; /* 抓取年費產生起日 */
    String hCardDropAfFlag = "";
    String hTempDate = "";
    String hTempDOppostDate = ""; /* 程式上線日以往不追朔 */
    String dOPPOSTDate = "";

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
                comc.errExit("Usage : ActA091 [callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            showLogMessage("I", "", String.format("本程式即自 %s 日以後之停卡客戶才會處理", dOPPOSTDate));
            hTempDOppostDate = dOPPOSTDate;
            selectPtrBusinday();
            selectCrdCard();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
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

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {

        sqlCmd = "select a.acno_p_seqno,";
        sqlCmd += " a.card_no,";
        sqlCmd += " a.new_end_date,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " a.oppost_date,";
        sqlCmd += " a.rowid h_card_rowid,";
        sqlCmd += " b.rowid h_cfee_rowid ";
        sqlCmd += "  from crd_card a,cyc_afee b ";
        sqlCmd += " where a.acno_p_seqno = a.p_seqno "; // acno_flag <> 'Y'
        /** recs-s951120-018增加'AT','AU','AV','AW','R1' **/
        sqlCmd += "   and a.oppost_reason in ('A2','AJ','AT','AU','AV','AW','B4','R1') ";
        sqlCmd += "   and a.drop_af_date = '' ";
        sqlCmd += "   and a.oppost_date >= to_char(add_months(sysdate,-1),'yyyymmdd') ";
        sqlCmd += "   and a.acno_p_seqno = b.P_SEQNO "; // acct_p_seqno
        sqlCmd += "   and a.card_no      = b.card_no ";
        sqlCmd += "   and decode(b.maintain_code,'','U',b.maintain_code) != 'Y' ";
        sqlCmd += "   and b.rcv_annual_fee != 0 ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hCardPSeqno = getValue("acno_p_seqno");
            hCardCardNo = getValue("card_no");
            hCardNewEndDate = getValue("new_end_date");
            hCardAcctType = getValue("acct_type");
            hCardOppostDate = getValue("oppost_date");
            hCardRowid = getValue("h_card_rowid");
            hCfeeRowid = getValue("h_cfee_rowid");

            /** 取得年費產生起日 **/
            checkFeeDate();

            /** 18:bil_contract 尚有分期 */
            /** 17:有消費記錄 ***/
            /** 11:act_debt.有期末餘額 ***/
            /** 10:無消費記錄且無act_debt記錄或期末餘額為0 **/
            /****
             * 依據ECS-930211-008(UAT-06)修改*************** if
             * ((inta=select_bil_contract())!=0) { update_crd_card(inta);
             * continue; }
             * 
             * if ((inta=select_bil_bill())!=0) { update_crd_card(inta);
             * continue; }
             * 
             * if ((inta=select_act_debt())!=0) { update_crd_card(inta);
             * continue; }
             ********/
            updateCycAfee();
            updateCrdCard(0);
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void checkFeeDate() throws Exception {
        String year = "";
        int tYear = 0;

        year = hCardOppostDate;
        tYear = comcr.str2int(year);

        sqlCmd = "select substr(to_char(add_months(to_date( ? , 'yyyymmdd'),1),'YYYYMM'),5,2) h_temp_date";
        sqlCmd += " from dual ";
        setString(1, hCardNewEndDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempDate = getValue("h_temp_date");
        }

        if (hCardOppostDate.substring(4).compareTo(hTempDate) < 0) {
            tYear = tYear - 1;
        }

        hTempDOppostDateS = String.format("%d%s", tYear, hTempDate);

    }

    /***********************************************************************/
    void updateCycAfee() throws Exception {
        daoTable = "cyc_afee";
        updateSQL = "reason_code     = 'DD',";
        updateSQL += " rcv_annual_fee = 0,";
        updateSQL += " mod_time       = sysdate,";
        updateSQL += " mod_pgm        = 'ActA091'";
        whereStr = "where rowid     = ?  ";
        whereStr += "and decode(maintain_code,'','x',maintain_code) != 'Y' ";
        setRowId(1, hCfeeRowid);
        updateTable();

    }

    /***********************************************************************/
    void updateCrdCard(int inta) throws Exception {
        hCardDropAfFlag = String.format("1%1d", inta);

        daoTable = "crd_card";
        updateSQL = "drop_af_flag  = ?,";
        updateSQL += " drop_af_date = ?,";
        updateSQL += " mod_pgm      = 'ActA091',";
        updateSQL += " mod_time     = sysdate";
        whereStr = "where rowid   = ? ";
        setString(1, hCardDropAfFlag);
        setString(2, hBusiBusinessDate);
        setRowId(3, hCardRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActA091 proc = new ActA091();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

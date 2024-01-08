/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/12  V1.00.01    Brian     error correction                          *
 *  109/11/18  V1.00.02    shiyuqi       updated for project coding standard   * 
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*授扣帳號已到期處理作業*/
public class ActH014 extends AccessDAO {

    private String progname = "授扣帳號已到期處理作業   109/11/18  V1.00.02 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String rptName1 = "";
    int recordCnt = 0;
    long hModSeqno = 0;
    String ecsServer = "";
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String iPostDate = "";

    String hTempUser = "";
    String hBusinessDate = "";
    String hNextBusinessDate = "";
    String hSystemDate = "";
    String hSystemTime = "";
    String hCknoPSeqno = "";
    String hCknoAcctType = "";
    String hCknoAcctKey = "";
    String hCknoApplNo = "";
    String hCknoIdPSeqno = "";
    String hCknoId = "";
    String hCknoIdCode = "";
    String hCknoAutopayAcctBank = "";
    String hCknoAutopayAcctNo = "";
    String hCknoCardNo = "";
    String hCknoAutopayIndicator = "";
    double hCknoAutopayFixAmt = 0;
    String hCknoAutopayRate = "";
    String hCknoIssueDate = "";
    String hCknoAutopayAcctSDate = "";
    String hCknoAutopayAcctEDate = "";
    String hCknoValidFlag = "";
    String hCknoRejectCode = "";
    String hCknoFromMark = "";
    String hCknoVerifyFlag = "";
    String hCknoVerifyDate = "";
    String hCknoVerifyReturnCode = "";
    String hCknoExecCheckFlag = "";
    String hCknoExecCheckDate = "";
    String hCknoIbmCheckFlag = "";
    String hCknoIbmCheckDate = "";
    String hCknoIbmReturnCode = "";
    String hCknoUpdateMainFlag = "";
    String hCknoUpdateMainDate = "";
    String hCknoStmtCycle = "";
    String hCknoAutopayIdPSeqno = "";
    String hCknoAutopayId = "";
    String hCknoAutopayIdCode = "";
    String hCknoProcMark = "";
    String hCknoCreateDate = "";
    String hCknoCreateUser = "";
    String hCknoCreateTime = "";
    String hCknoAchCheckFlag = "";
    String hCknoAchSendDate = "";
    String hCknoAchRtnDate = "";
    String hCknoOldAcctBank = "";
    String hCknoOldAcctNo = "";
    String hCknoOldAcctId = "";
    String hCknoAdMark = "";
    String hAcnoCorpNo = "";
    String hAcnoVipCode = "";
    String hAcnoStmtCycle = "";
    String hCknoRowid = "";
    String hAchdChiName = "";
    String hAchdCellarPhone = "";
    String nextVouch = "";
    String hAchbBankNo = "";
    String hSysdate = "";
    String hAchdRtnStatus = "";
    String hAchdRtnDescription = "";
    String hAchdSmsFlag = "";
    String hIdnoIdPSeqno = "";
    int totCnt = 0;
    String hCallRProgramCode = "";
    String hCallErrorDesc = "";

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
                comc.errExit("Usage : ActH014 [yyyymmdd] [batch_seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);
            if (hTempUser.length() == 0) {
                hModUser = comc.commGetUserID();
                hTempUser = hModUser;
            }

            commonRtn();

            if (args.length > 0 && args[0].length() == 8)
                hBusinessDate = args[0];

            nextVouch = comcr.increaseDays(hBusinessDate, 1);

            showLogMessage("I", "", String.format("產生資料日,[%s]", hBusinessDate));

            /*
             * check_open();
             */
            selectActChkno();
            comcr.hCallErrorDesc = String.format("程式執行結束,筆數=[%d]", totCnt);
            showLogMessage("I", "", String.format("%s", hCallErrorDesc));

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
    void commonRtn() throws Exception {
        sqlCmd = "select business_date,";
        sqlCmd += " to_char(to_date(business_date ,'yyyymmdd')+ 1 days ,'yyyymmdd') h_next_business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
            hNextBusinessDate = getValue("h_next_business_date");
        }

        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += " to_char(sysdate,'hh24:mi:ss') h_system_time ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hSystemTime = getValue("h_system_time");
        }

        /* get user seq */
        hModSeqno = comcr.getModSeq();
        hModUser = comc.commGetUserID();
        // h_mod_ws = ECS_SERVER;
        // h_mod_log = "1";
    }

    /***********************************************************************/
    void selectActChkno() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.acno_p_seqno,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " a.acct_key,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " a.autopay_acct_bank,";
        sqlCmd += " a.autopay_acct_no,";
        sqlCmd += " a.autopay_indicator,";
        sqlCmd += " a.autopay_fix_amt,";
        sqlCmd += " a.autopay_rate,";
        sqlCmd += " a.autopay_acct_s_date,";
        sqlCmd += " a.autopay_acct_e_date,";
        sqlCmd += " cast(? as varchar(8))   h_ckno_exec_check_date,"; /* exec_check_date */
        sqlCmd += " decode(substr(a.autopay_acct_bank,1,3),'017','N','Y') h_ckno_ibm_check_flag,";
        sqlCmd += " decode(substr(a.autopay_acct_bank,1,3),'017','', cast(? as varchar(8)) ) h_ckno_ibm_check_date,";
        sqlCmd += " a.stmt_cycle,";
        sqlCmd += " a.autopay_id,";
        sqlCmd += " a.autopay_id_code,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_ckno_create_date,"; /* create_date */
        sqlCmd += " to_char(sysdate,'hh24:mi:ss') h_ckno_create_time,";
        sqlCmd += " a.autopay_acct_bank,";
        sqlCmd += " a.autopay_acct_no,";
        sqlCmd += " a.autopay_id,";
        sqlCmd += " c.corp_no,";
        sqlCmd += " a.vip_code,";
        sqlCmd += " a.stmt_cycle,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += " from act_acno a  ";
        sqlCmd += "  left join crd_corp c on c.corp_p_seqno = a.corp_p_seqno "; //find corp_no in crd_corp
        sqlCmd += "where autopay_acct_e_date = ? ";
        sqlCmd += " and 1 = 1  ";
        setString(1, hBusinessDate);
        setString(2, hBusinessDate);
        setString(3, hBusinessDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hCknoPSeqno = getValue("acno_p_seqno");
            hCknoAcctType = getValue("acct_type");
            hCknoAcctKey = getValue("acct_key");
            hCknoApplNo = "";
            hCknoIdPSeqno = getValue("id_p_seqno");
            hCknoAutopayAcctBank = getValue("autopay_acct_bank");
            hCknoAutopayAcctNo = getValue("autopay_acct_no");
            hCknoCardNo = "";
            hCknoAutopayIndicator = getValue("autopay_indicator");
            hCknoAutopayFixAmt = getValueDouble("autopay_fix_amt");
            hCknoAutopayRate = getValue("autopay_rate");
            hCknoIssueDate = "";
            hCknoAutopayAcctSDate = getValue("autopay_acct_s_date");
            hCknoAutopayAcctEDate = getValue("autopay_acct_e_date");
            hCknoValidFlag = "1";
            hCknoRejectCode = "";
            hCknoFromMark = "03";
            hCknoVerifyFlag = "N";
            hCknoVerifyDate = "";
            hCknoVerifyReturnCode = "99";
            hCknoExecCheckFlag = "Y";
            hCknoExecCheckDate = getValue("h_ckno_exec_check_date");
            hCknoIbmCheckFlag = getValue("h_ckno_ibm_check_flag");
            hCknoIbmCheckDate = getValue("h_ckno_ibm_check_date");
            hCknoIbmReturnCode = "";
            hCknoUpdateMainFlag = "N";
            hCknoUpdateMainDate = "";
            hCknoStmtCycle = getValue("stmt_cycle");
            hCknoAutopayIdPSeqno = "";
            hCknoAutopayId = getValue("autopay_id");
            hCknoAutopayIdCode = getValue("autopay_id_code");
            hCknoProcMark = "N";
            hCknoCreateDate = getValue("h_ckno_create_date");
            hCknoCreateUser = javaProgram;
            hCknoCreateTime = getValue("h_ckno_create_time");
            hCknoAchCheckFlag = "N";
            hCknoAchSendDate = "";
            hCknoAchRtnDate = "";
            hCknoOldAcctBank = getValue("autopay_acct_bank");
            hCknoOldAcctNo = getValue("autopay_acct_no");
            hCknoOldAcctId = getValue("autopay_id");
            hCknoAdMark = "D";
            hAcnoCorpNo = getValue("corp_no");
            hAcnoVipCode = getValue("vip_code");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hCknoRowid = getValue("rowid");

            if (hCknoAutopayId.length() == 0) {
                hCknoAutopayId = hCknoId;
                hCknoAutopayIdCode = hCknoIdCode;
            }

            if (hCknoAutopayAcctBank.length() != 7) {
                sqlCmd = "select bank_no ";
                sqlCmd += " from act_ach_bank  ";
                sqlCmd += "where bank_no like ? || '%'  ";
                sqlCmd += "fetch first 1 rows only ";
                setString(1, hCknoAutopayAcctBank);
                int recordCnt = selectTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("select_act_ach_bank not found!", "", hCallBatchSeqno);
                }
                if (recordCnt > 0) {
                    hCknoAutopayAcctBank = getValue("bank_no");
                }
            }
            totCnt++;
            selectCrdIdno(hCknoAutopayAcctBank);

            if (totCnt % 10000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process record=[%d]", totCnt));

            /*
             * 由 act_h003 insert into ach_dtl insert_act_ach_dtl();
             */
            hCknoAdMark = "D";
            hAchdSmsFlag = "";

            insertActChkno();

        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectCrdIdno(String tBankNo) throws Exception {

        hAchbBankNo = tBankNo;

        hAchdChiName = "";
        hAchdCellarPhone = "";
        if (hAcnoCorpNo.length() < 1) {
            sqlCmd = "select chi_name,";
            sqlCmd += "cellar_phone ";
            sqlCmd += " from crd_idno  ";
            sqlCmd += "where id_p_seqno = ?  ";
            sqlCmd += "fetch first 1 rows only ";
            setString(1, hCknoIdPSeqno);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hAchdChiName = getValue("chi_name");
                hAchdCellarPhone = getValue("cellar_phone");
            }
        } else {
            if (hCknoId.length() == 0) {
                hCknoId = hAcnoCorpNo;
                hCknoIdCode = "0";
            }
            sqlCmd = "select chi_name ";
            sqlCmd += " from crd_corp  ";
            sqlCmd += "where corp_no = ?  ";
            sqlCmd += "fetch first 1 rows only ";
            setString(1, hAcnoCorpNo);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hAchdChiName = getValue("chi_name");
            }
        }
    }

    /***********************************************************************/
    void insertActChkno() throws Exception {

        setValue("p_seqno", hCknoPSeqno);
        setValue("acct_type", hCknoAcctType);
        setValue("appl_no", hCknoApplNo);
        setValue("id_p_seqno", hCknoIdPSeqno);
        setValue("autopay_acct_bank", hCknoAutopayAcctBank);
        setValue("autopay_acct_no", hCknoAutopayAcctNo);
        setValue("card_no", hCknoCardNo);
        setValue("autopay_indicator", hCknoAutopayIndicator);
        setValueDouble("autopay_fix_amt", hCknoAutopayFixAmt);
        setValue("autopay_rate", hCknoAutopayRate);
        setValue("issue_date", hCknoIssueDate);
        setValue("autopay_acct_s_date", hCknoAutopayAcctSDate);
        setValue("autopay_acct_e_date", hCknoAutopayAcctEDate);
        setValue("valid_flag", hCknoValidFlag);
        setValue("reject_code", hCknoRejectCode);
        setValue("from_mark", hCknoFromMark);
        setValue("verify_flag", hCknoVerifyFlag);
        setValue("verify_date", hCknoVerifyDate);
        setValue("verify_return_code", hCknoVerifyReturnCode);
        setValue("exec_check_flag", hCknoExecCheckFlag);
        setValue("exec_check_date", hCknoExecCheckDate);
        setValue("ibm_check_flag", hCknoIbmCheckFlag);
        setValue("ibm_check_date", hCknoIbmCheckDate);
        setValue("ibm_return_code", hCknoIbmReturnCode);
        setValue("update_main_flag", hCknoUpdateMainFlag);
        setValue("update_main_date", hCknoUpdateMainDate);
        setValue("stmt_cycle", hCknoStmtCycle);
        setValue("autopay_id_p_seqno", hIdnoIdPSeqno);
        setValue("autopay_id", hCknoAutopayId);
        setValue("autopay_id_code", hCknoAutopayIdCode);
        setValue("proc_mark", hCknoProcMark);
        setValue("crt_date", hCknoCreateDate);
        setValue("mod_user", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        setValue("crt_user", hCknoCreateUser);
        setValue("crt_time", hCknoCreateTime);
        setValue("ach_check_flag", hCknoAchCheckFlag);
        setValue("ach_send_date", hCknoAchSendDate);
        setValue("ach_rtn_date", hCknoAchRtnDate);
        setValue("old_acct_bank", "");
        setValue("old_acct_no", "");
        setValue("old_acct_id", "");
        setValue("ad_mark", hCknoAdMark);
        daoTable = "act_chkno";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_chkno duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActH014 proc = new ActH014();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

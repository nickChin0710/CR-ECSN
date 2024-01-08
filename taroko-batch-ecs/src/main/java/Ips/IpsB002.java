/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-14  V1.00.01    tanwei      updated for project coding standard     *
******************************************************************************/

package Ips;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*關閉自動加值資料處理*/
public class IpsB002 extends AccessDAO {
    private String progname = "關閉自動加值資料處理  109/12/14 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hAuofCrtDate = "";
    String hAuofCrtTime = "";
    String hIardIpsCardNo = "";
    String hIardCardNo = "";
    String hAcnoPSeqno = "";
    String hAcnoPaymentRate1 = "";
    String hIsBlockReason = "";
    String hIardCurrentCode = "";
    String hCardCurrentCode = "";
    String hCardOppostDate = "";
    String hCardOppostReason = "";
    String hCardReissueDate = "";
    String hCardReissueReason = "";
    String hIardOppostDate = "";
    String hIardRowid = "";
    String hAuofFromMark = "";
    String hAuofModUser = "";
    String hIpscMcodeCond = "";
    String hIpscPaymentRate = "";
    double hIpscMcodeAmt = 0;
    String hIpscBlockCond = "";
    String hIpscBlockCodes = "";
    String hIpscImpListCond = "";
    String hIpscStop1Cond = "";
    int hIpscStop1Days = 0;
    String hIpscStop2Cond = "";
    int hIpscStop2Days = 0;
    double tempDouble = 0;
    String dateCurr = "";
    String addDays = "";
    String hTempDate = "";
    String newStopDate = "";
    int totCnt = 0;
    int insertCnt = 0;

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
                comc.errExit("Usage : IpsB002 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if (args.length == 1)
                if (args[0].length() == 8)
                    hBusiBusinessDate = args[0];

            selectPtrBusinday();
            int rtn = selectIpsCommParm();
            if (rtn == 0)
                selectIpsCard();

            showLogMessage("I", "", String.format("Process records = [%d][%d]\n", totCnt, insertCnt));

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
        hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? sysDate : hBusiBusinessDate;
        sqlCmd = "select ";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_auof_crt_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_auof_crt_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAuofCrtDate = getValue("h_auof_crt_date");
            hAuofCrtTime = getValue("h_auof_crt_time");
        }

    }

    /***********************************************************************/
    int selectIpsCommParm() throws Exception {
        hIpscMcodeCond = "";
        hIpscPaymentRate = "";
        hIpscMcodeAmt = 0;
        hIpscBlockCond = "";
        hIpscBlockCodes = "";
        hIpscImpListCond = "";
        hIpscStop1Cond = "";
        hIpscStop1Days = 0;
        hIpscStop2Cond = "";
        hIpscStop2Days = 0;
        sqlCmd = "select mcode_cond,";
        sqlCmd += "payment_rate,";
        sqlCmd += "mcode_amt,";
        sqlCmd += "block_cond,";
        sqlCmd += "block_codes,";
        sqlCmd += "imp_list_cond,";
        sqlCmd += "stop1_cond,";
        sqlCmd += "stop1_days,";
        sqlCmd += "stop2_cond,";
        sqlCmd += "stop2_days ";
        sqlCmd += " from ips_comm_parm  ";
        sqlCmd += "where parm_type ='AUTOLOAD_OFF' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIpscMcodeCond    = getValue("mcode_cond");
            hIpscPaymentRate  = getValue("payment_rate");
            hIpscMcodeAmt     = getValueDouble("mcode_amt");
            hIpscBlockCond    = getValue("block_cond");
            hIpscBlockCodes   = getValue("block_codes");
            hIpscBlockCodes   = hIpscBlockCodes.replace(",", ""); //ryan:欄位新增逗號隔開block_codes modify by brian 20200513
            hIpscImpListCond = getValue("imp_list_cond");
            hIpscStop1Cond    = getValue("stop1_cond");
            hIpscStop1Days    = getValueInt("stop1_days");
            hIpscStop2Cond    = getValue("stop2_cond");
            hIpscStop2Days    = getValueInt("stop2_days");
        } else {
            return 1403;
        }
        return 0;
    }

    /***********************************************************************/
    void selectIpsCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.ips_card_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "c.acno_p_seqno,";
        sqlCmd += "c.p_seqno,";
        sqlCmd += "decode(c.payment_rate1,'0A','00','0B','00','0C','00','0D','00','0E','00',c.payment_rate1) h_acno_payment_rate1,";
        sqlCmd += "a.current_code as h_iard_current_code,";
        sqlCmd += "b.current_code as h_card_current_code,";
        sqlCmd += "b.oppost_date,";
        sqlCmd += "b.oppost_reason,";
        sqlCmd += "b.reissue_date,";
        sqlCmd += "b.reissue_reason,";
        sqlCmd += "a.ips_oppost_date,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from act_acno c, crd_card b , ips_card a ";
        sqlCmd += "where decode(a.autoload_flag,'','N',a.autoload_flag) = 'Y' ";
        sqlCmd += "and a.autoload_clo_date = '' ";
        sqlCmd += "and a.blacklt_date      = '' ";
        sqlCmd += "and a.lock_date         = '' ";
        sqlCmd += "and a.return_date       = '' ";
        sqlCmd += "and a.new_end_date      > ? ";
        sqlCmd += "and b.card_no           = a.card_no ";
        sqlCmd += "and c.acno_p_seqno      = b.acno_p_seqno ";
        sqlCmd += "and not exists (select '*' from ips_autooff_log d where a.ips_card_no=d.ips_card_no and proc_flag='N') ";
/* lai test
sqlCmd += "and a.ips_card_no in ('01771164440','01771164440')";
*/
        sqlCmd += " ";
        setString(1, hBusiBusinessDate);
        openCursor();
        while (fetchTable()) {
            hIardIpsCardNo = getValue("ips_card_no");
            hIardCardNo = getValue("card_no");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoPaymentRate1 = getValue("h_acno_payment_rate1");
            hIardCurrentCode = getValue("h_iard_current_code");
            hCardCurrentCode = getValue("h_card_current_code");
            hCardOppostDate = getValue("oppost_date");
            hCardOppostReason = getValue("oppost_reason");
            hCardReissueDate = getValue("reissue_date");
            hCardReissueReason = getValue("reissue_reason");
            hIardOppostDate = getValue("ips_oppost_date");
            hIardRowid = getValue("rowid");

            hIsBlockReason = "";
            sqlCmd = "select rpad(decode(block_reason1||block_reason2||block_reason3||block_reason4||block_reason5,'',' ',block_reason1||block_reason2||block_reason3||block_reason4||block_reason5),10,' ') h_is_block_reason"
                    + " from cca_card_acct ";
            sqlCmd += " where acno_p_seqno = ? " + " and decode(debit_flag,'','N',debit_flag) = 'N' ";
            sqlCmd += "fetch first 1 rows only";
            setString(1, hAcnoPSeqno);
            noTrim = "Y";
            if (selectTable() > 0) {
                hIsBlockReason = getValue("h_is_block_reason");
            }
            noTrim = "N";

            hAuofFromMark = "2";
            hAuofModUser = "";

            totCnt++;

            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("crd Process record=[%d]\n", totCnt));
if(debug == 1)
   showLogMessage("I","","888 Read =["+hIardIpsCardNo+"]"+hIardCardNo+","+totCnt+","
                                      +hIsBlockReason+","+hIpscBlockCond);

            if (hIpscStop1Cond.equals("Y") && hCardOppostDate.length() > 0) {
                if ((hCardCurrentCode.equals("2")) || (hCardCurrentCode.equals("5"))) {
                } else {
                    newStopDate = addDays(hBusiBusinessDate, hIpscStop1Days * -1);
                    if (hCardOppostDate.length() > 0 && 
                        comcr.str2long(hCardOppostDate) == comcr.str2long(newStopDate)) {
                        hAuofModUser = "1";
                        insertIpsAutooffLog();
                        updateIpsCard();
                        continue;
                    }
                }
            }

            if (hIpscStop2Cond.equals("Y") && hCardReissueDate.length() > 0) {
                if (hCardReissueReason.equals("2") && !hIardCurrentCode.equals("0")) {
                    showLogMessage("I", "", String.format("\n Atom=[%d][%s][%s][%s]", totCnt
                                  , hIardIpsCardNo, hCardReissueReason, hIardCurrentCode));
                    newStopDate = addDays(hBusiBusinessDate, hIpscStop2Days * -1);
                    if (hCardReissueDate.length() > 0 && 
                        comcr.str2long(hIardOppostDate) == comcr.str2long(newStopDate)) {
                        hAuofModUser = "2";
                        insertIpsAutooffLog();
                        updateIpsCard();
                        continue;
                    }
                }
            }

            if (hIpscBlockCond.equals("Y")) {
                int rtn = selectActAcno();
                if (rtn != 0) {/* 比到凍結 */
                    hAuofModUser = "3";
                    insertIpsAutooffLog();
                    updateIpsCard();
                    continue;
                }
            }

            if (hIpscMcodeCond.equals("Y")) {
                selectActAcctSum();
                if (comcr.str2int(hAcnoPaymentRate1) >= comcr.str2int(hIpscPaymentRate)) {
                    if (tempDouble >= hIpscMcodeAmt) {
                        hAuofModUser = "4";
                        insertIpsAutooffLog();
                        updateIpsCard();
                        continue;
                    }
                }
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    int selectActAcctSum() throws Exception {
        tempDouble = 0;

        sqlCmd = "select nvl(sum(unbill_end_bal+billed_end_bal),0) temp_double ";
        sqlCmd += " from act_acct_sum  ";
        sqlCmd += "where acct_code in ('BL','CA','ID','IT','AO','OT','DB','CB')  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct_sum not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            tempDouble = getValueDouble("temp_double");
        }

        return 0;
    }

    /***********************************************************************/
    int selectActAcno() throws Exception {
        if (comc.getSubString(hIsBlockReason, 0, 10).equals("          "))
            return 0;

        return chkBlockReason();
    }

    /***********************************************************************/
    int chkBlockReason() throws Exception {
        int i, j = 0;

if(debug == 1)
   showLogMessage("I","","  888 BLOCK=["+hIpscBlockCodes+"]"+hIsBlockReason);

        for (i = 0; i <= 4; i++) {
            if (comc.getSubString(hIsBlockReason, 2 * i, 2 * i + 2).trim().length() == 0)
                continue;

            for (j = 0; j <= 24; j++) {
                if (comc.getSubString(hIsBlockReason, 2 * i, 2 * i + 2)
                        .equals(comc.getSubString(hIpscBlockCodes, 2 * j, 2 * j + 2))) {
                    return 1;
                }
            }
        }

        return 0;
    }

    /***********************************************************************/
    void updateIpsCard() throws Exception {
        daoTable   = "ips_card";
        updateSQL  = " autoload_from     = ? ,";
        updateSQL += " autoload_flag     = 'N' ,";
        updateSQL += " autoload_clo_date = ? ,";
        updateSQL += " mod_pgm           = ? ,";
        updateSQL += " mod_time          = sysdate";
        whereStr   = "where rowid        = ? ";
        setString(1, hAuofFromMark);
        setString(2, hBusiBusinessDate);
        setString(3, javaProgram);
        setRowId(4, hIardRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int insertIpsAutooffLog() throws Exception {

        insertCnt++;

        setValue("crt_date"   , hAuofCrtDate);
        setValue("crt_time"   , hAuofCrtTime);
        setValue("ips_card_no", hIardIpsCardNo);
        setValue("card_no"    , hIardCardNo);
        setValue("CRT_USER"   , "batch");
        setValue("from_mark"  , hAuofFromMark);
        setValue("mod_user"   , hAuofModUser);
        setValue("proc_flag"  , "N");
        setValue("mod_pgm"    , javaProgram);
        setValue("mod_time"   , sysDate + sysTime);
        daoTable = "ips_autooff_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    String addDays(String dateCurr, int addDays) throws Exception {
        String hTempDate = "";

        hTempDate = "";
        sqlCmd = "select to_char((to_date(? , 'yyyymmdd') + ? days),'yyyymmdd') h_temp_date ";
        sqlCmd += " from dual ";
        setString(1, dateCurr);
        setInt(2, addDays);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("add_days() not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempDate = getValue("h_temp_date");
        }
        return hTempDate;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsB002 proc = new IpsB002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

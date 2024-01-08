/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/20  V1.00.01    SUP       error correction                          *
 *  109/11/13  V1.00.02    shiyuqi   updated for project coding standard       *
 *  112/05/29  V1.00.03    Simon     1.dOPPOSTDate changed into "20230101"     *
 *                                   2.crd_card.new_end_date invalid date check*
 ******************************************************************************/

package Act;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommDate;


/*申停已入帳自動D除年費處理程式*/
public class ActA090 extends AccessDAO {

    private String progname = "申停已入帳自動D除年費處理程式  112/05/29  V1.00.03 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
  	CommDate commDate = new CommDate();

    String prgmId = "ActA090";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hTempDOppostDate = ""; /* 程式上線日以往不追朔 */
    String hTempDate = "";
    String hTempDOppostDates = ""; /* 抓取年費產生起日 */
    String hCardPSeqno = "";
    String hCardCardNo = "";
    String hCardNewEndDate = "";
    String hCardAcctType = "";
    String hCardOppostDate = "";
    String hCardRowid = "";
    int hTempExistCnt = 0;
    String hDebtReferenceSeq = "";
    double hDebtBegBal = 0;
    double hDebtEndBal = 0;
    double hDebtDAvailableBal = 0;
    String hDebtInterestDate = "";
    String hDebtCurrCode = "";
    String hDebtAcctMonth = "";
    double hTempMinusAmt = 0;
    double hTempNativeAmt = 0;
    String hCardDropAfFlag = "";
  //String dOPPOSTDate = "20060630";
    String dOPPOSTDate = "20230101";

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
                comc.errExit("Usage : ActA090 [callbatch_seqno]", "");
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

        sqlCmd = "select acno_p_seqno,";
        sqlCmd += " card_no,";
        sqlCmd += " new_end_date,";
        sqlCmd += " acct_type,";
        sqlCmd += " oppost_date,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from crd_card ";
        sqlCmd += " where p_seqno = acno_p_seqno "; // acno_flag != 'Y'
        /** recs-s951120-018增加'AT','AU','AV','AW','R1' **/
        sqlCmd += "   and oppost_reason in ('A2','AJ','AT','AU','AV','AW','B4','R1') ";
        sqlCmd += "   and drop_af_date = '' ";
        sqlCmd += "   and oppost_date >= ? ";
        setString(1, hTempDOppostDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hCardPSeqno = getValue("acno_p_seqno");
            hCardCardNo = getValue("card_no");
            hCardNewEndDate = getValue("new_end_date");
            hCardAcctType = getValue("acct_type");
            hCardOppostDate = getValue("oppost_date");
            hCardRowid = getValue("rowid");

            /** 取得年費產生起日 **/
            checkFeeDate();
            int inta = selectActDebt();
            if (inta != 0) {
                updateCrdCard(inta);
                continue;
            }

            if (selectActAcaj() != 0) { /* 表示有找到act_acaj有資料 */
                continue;
            }
            /**
             * 依據ECS-930211-008(UAT-06)修改**** if
             * ((inta=select_bil_contract())!=0) { if(DEBUG)
             * fprintf(stderr,"CONTRACT acct_type:[%s] acct_key:[%s] "
             * "inta:[%d]\n",h_card_acct_type.arr,h_card_acct_key.arr,inta);
             * update_crd_card(inta); continue; } if
             * ((inta=select_bil_bill())!=0) { if(DEBUG) fprintf(stderr,"BILL
             * p_seqno:[%s] card_no:[%s] "
             * "inta:[%d]\n",h_card_p_seqno.arr,h_card_card_no.arr,inta);
             * update_crd_card(inta); continue; }
             **/
            insertActAcaj();
            updateCrdCard(0);
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void checkFeeDate() throws Exception {
        String year = "";
        int tYear = 0;

        year = hCardOppostDate;
//      t_year = comcr.str2int(year);
// modified by Simon on 2018/12/07      
        tYear = comcr.str2int(year.substring(0, 4));

				if(!commDate.isDate(hCardNewEndDate)) {
					hCardNewEndDate="20000101";
				}

        sqlCmd = "select substr(to_char(add_months(to_date( ? , 'yyyymmdd'),1),'YYYYMM'),5,2) h_temp_date";
        sqlCmd += " from dual ";
        setString(1, hCardNewEndDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempDate = getValue("h_temp_date");
        }

        if (hCardOppostDate.substring(4, 6).compareTo(hTempDate) < 0) {
            tYear = tYear - 1;
        }

        hTempDOppostDates = String.format("%d%s", tYear, hTempDate);

    }

    /***********************************************************************/
    int selectActDebt() throws Exception {
        hDebtReferenceSeq = "";
        hDebtAcctMonth = "";
        hDebtBegBal = 0;
        hDebtEndBal = 0;
        hDebtDAvailableBal = 0;
        hDebtCurrCode = "";
        hDebtInterestDate = "";

        sqlCmd = "select reference_no,"; // reference_seq
        sqlCmd += " acct_month,";
        sqlCmd += " beg_bal,";
        sqlCmd += " end_bal,";
        sqlCmd += " d_avail_bal,"; // d_available_bal
        sqlCmd += " curr_code,";
        sqlCmd += " interest_date ";
        sqlCmd += " from act_debt ";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and card_no   = ?  ";
        sqlCmd += "   and acct_code = 'AF' "; // acct_code
        setString(1, hCardPSeqno);
        setString(2, hCardCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDebtReferenceSeq = getValue("reference_no");
            hDebtAcctMonth = getValue("acct_month");
            hDebtBegBal = getValueDouble("beg_bal");
            hDebtEndBal = getValueDouble("end_bal");
            hDebtDAvailableBal = getValueDouble("d_avail_bal");
            hDebtCurrCode = getValue("curr_code");
            hDebtInterestDate = getValue("interest_date");
        } else
            return (1);
        // if (sqlca.sqlcode==-2112) return(2); //SELECT..INTO returns too many
        // rows
        if (hDebtAcctMonth.compareTo(hTempDOppostDates) < 0)
            return (3);

        if (hDebtEndBal == 0)
            return (4);
        /*****
         * 依據BECS950704-154修改 if (h_debt_d_available_bal ==0) return(4);
         *****************************************************/
        /******
         * 依據 ECS930211-008(UAT-04)********************** if
         * (h_debt_beg_bal!=h_debt_end_bal!=0) return(5);
         *******************************************/
        return (0);
    }

    /***********************************************************************/
    int selectActAcaj() throws Exception {
        int hTempExistCnt = 0;

        sqlCmd = "select count(*) h_temp_exist_cnt ";
        sqlCmd += " from act_acaj  ";
        sqlCmd += " where reference_no = ? ";
        setString(1, hDebtReferenceSeq);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acaj not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempExistCnt = getValueInt("h_temp_exist_cnt");
        }

        if (hTempExistCnt != 0)
            return (1);

        return (0);
    }

    /***********************************************************************/
    void insertActAcaj() throws Exception {
        daoTable = "act_acaj";
        setValue("crt_date", hBusiBusinessDate);
        setValue("crt_time", sysTime);
        setValue("p_seqno", hCardPSeqno);
        setValue("acct_type", hCardAcctType);
        setValue("adjust_type", "DE10");
        setValue("reference_no", hDebtReferenceSeq);
        setValue("post_date", hBusiBusinessDate);
        setValueDouble("orginal_amt", hDebtBegBal);
        setValueDouble("dr_amt", hDebtEndBal);
        setValueDouble("cr_amt", 0);
        setValueDouble("bef_amt", hDebtEndBal);
        setValueDouble("aft_amt", hDebtEndBal - hDebtEndBal);
        setValueDouble("bef_d_amt", hDebtDAvailableBal);
        setValueDouble("aft_d_amt", hDebtDAvailableBal - hDebtEndBal);
        setValue("acct_code", "AF"); // acct_code
        setValue("function_code", "U");
        setValue("card_no", hCardCardNo);
        setValue("value_type", "1");
        setValue("interest_date", hDebtInterestDate);
        setValue("apr_flag", "Y"); // confirm_flag
        setValue("update_date", hBusiBusinessDate);
        setValue("mod_time", sysDate + sysTime);
        setValue("adj_reason_code", "1");
        setValue("debit_item", "14817000");
        setValue("job_code", "DP");
        setValue("vouch_job_code", "99");
        setValue("update_user", "ActA090");
        setValue("mod_user", "ActA090");
        setValue("mod_pgm", "ActA090");
        setValue("curr_code", hDebtCurrCode);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acaj duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateCrdCard(int inta) throws Exception {

        /**
         * 00: D檔成功insert act_acaj 01: act_debt年費不存在 02: act_debt同帳戶同卡號二筆以上年費
         * 03: act_debt.acct_month<new_end_date 04: 可D數餘額=0 & 期末餘額=0 05:
         * 期初餘額不等於期末餘額 07: bil_bill有消費 08: bil_contract 尚有分期不D檔
         **************************/

        hCardDropAfFlag = String.format("0%1d", inta);

        daoTable = "crd_card";
        updateSQL = "drop_af_flag  = ?,";
        updateSQL += " drop_af_date = ?,";
        updateSQL += " mod_pgm      = 'ActA090',";
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

        ActA090 proc = new ActA090();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/01/01  V1.00.00    Edson     program initial                           *
 *  107/01/22  V1.00.01    Brian     error correction                          *
 *  109/02/04  V1.00.02    Brian     HESYUAN:新增欄位mkt_mcard_static.INTRODUCE_EMP_NO *
 *                                   刪除mkt_emp_static                               *
 *                                   mkt_mcard_static group by 新增INTRODUCE_EMP_NO, 尚未有CR單 *
 *  109/02/25  V1.00.03    Brian     mkt_year_target不再使用, 移除                                              *
 *  109/07/02  V1.00.04    Brian     HESYUAN mail 新增卡數的定義：首次發卡（非補/換/續卡）   *
 *  109-12-04  V1.00.05  tanwei      updated for project coding standard       *
 *  110-12-30  V1.00.06    YangBo    gen_brn update to v_crd_employee_unit     *
 *                                   add method selectMktCardConsumeMonth      *
 *                                   add method selectMktCardConsumeYear       *
 *                                   add method selectMktCardConsumeRecentYear *
 ******************************************************************************/

package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*全體員工招攬卡 月統計處理程式*/
public class MktA730 extends AccessDAO {

    private String progname = "全體員工招攬卡 月統計處理程式 109/12/04 V1.00.05";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hModUser = "";
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";

    String hBusiBusinessDate = "";
    String hTempAcctMonth = "";
    String hTempYear = "";
    String hMyttAcctType = "";
    String hCardCurrentCode = "";
    String hCardActivateFlag = "";
    String hCardIssueDate = "";
    String hCardRegBankNo = "";
    String hCardIntroduceEmpNo = "";
    String hCardOldCardNo = "";
//    int h_mesc_sum_cnt = 0;
//    int h_mesc_y_sum_cnt = 0;
//    int h_mesc_h_sum_cnt = 0;
//    String h_mesc_chi_name = "";
//    String h_mesc_acct_type = "";
//    String h_mesc_branch = "";
//    String h_mesc_introduce_emp_no = "";
//    String h_mesc_rowid = "";
    int hMmscMActCardCnt = 0;
    int hMmscMNoactCardCnt = 0;
    int hMmscMStopCardCnt = 0;
    int hMmscMSumCnt = 0;
    int hMmscYActCardCnt = 0;
    int hMmscYNoactCardCnt = 0;
    int hMmscYStopCardCnt = 0;
    int hMmscYSumCnt = 0;
    int hMmscHActCardCnt = 0;
    int hMmscHNoactCardCnt = 0;
    int hMmscHStopCardCnt = 0;
    int hMmscHSumCnt = 0;
    String hMmscIntroduceEmpNo = "";
    String hMmscAcctType = "";
    String hMmscBranch = "";
    String hMmscRowid = "";
    String hPbnhBranchName = "";
    int hMyttTargetCardCnt = 0;
    long hMyttSpreadMonths = 0;
    String hMyttBranch = "";
    String hMyttModUser = "";
    String hMyttModPgm = "";
    int hMmscMSort = 0;
    int hMmscYSort = 0;
    int hMescTotalSort = 0;
    int hMescDepSort = 0;
    double hMmscMRate = 0;
    double hMmscYRate = 0;
    String hTempOldCardNo = "";

    int inta = 0;
    int depSortInt = 0;
    String tmpstr = "";
    long indexCount = 0;
    int totalCount = 0;

    int    hMescNewCardCnt     = 0;
    int    hMescOldCardCnt     = 0;
    int    hMescConsumeCardCnt = 0;
    long   hMescCommonFees      = 0;
    long   hMescCaFees          = 0;
    long   hMescCfFees          = 0;
    String hCardIdPSeqno       = "";
    String hCardAcctType        = "";
    String hCardOppostDate      = "";
    String hCardBranch          = "";
    String hCardCardNo          = "";
    int    hMmscNewCardCnt     = 0;
    int    hMmscOldCardCnt     = 0;

    long hMmscCommonFees = 0; // 上個月一般消費
    long hMmscCaFees = 0; // 上個月預借現金消費
    int hMmscConsumeCardCnt = 0; // 上個月有消費卡數
    long hMmscCfFees = 0;
    long hMmscYearFees = 0; // 年度一般消費
    int hMmscYConsumeCardCnt = 0; // 年度有消費卡數
    long hMmscHYearFees = 0; // 近一年一般消費
    int hMmscHConsumeCardCnt = 0; // 近一年有消費卡數

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
                comc.errExit("Usage : MktA730 [acct_month]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            hModUser = comc.commGetUserID();
            hMyttModUser = hModUser;
            hMyttModPgm = javaProgram;

            selectPtrBusinday();

            if (args.length == 0) {
                if (!"06".equals(hBusiBusinessDate.substring(6))) {
                    exceptExit = 0;
                    comcr.errRtn("本程式為每月6日執行", "",hCallBatchSeqno);
                }
            } else {
                tmpstr = String.format("%6.6s", args[0]);
                hTempAcctMonth = tmpstr;
                tmpstr = String.format("%4.4s", args[0]);
                hTempYear = tmpstr;
            }
            showLogMessage("I", "", String.format("Will process month [%s]", hTempAcctMonth));

            deleteMktMcardTemp();
            // V1.00.03 由select_mkt_year_target搬至main處理
            // V1.00.06 由迴圈中移到此
            deleteMktMcardStatic();
//            select_mkt_year_target(); no use V1.00.03

            //V1.00.03 acct_type 全撈
            sqlCmd = "SELECT acct_type FROM crd_card GROUP BY ACCT_TYPE";
            int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                hMyttAcctType = getValue("acct_type", i);
                showLogMessage("I", "", String.format(" acct_type[%s]", hMyttAcctType));
                selectCrdCard(); // V1.00.03 由select_mkt_year_target搬至main處理
            }

//            select_mkt_mcard_temp_1(); V1.00.02 delete
//            select_mkt_emp_static(); V1.00.02 delete
            selectMktMcardTemp2();
            selectMktMcardStatic1();
            selectMktMcardStatic2();

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
        hBusiBusinessDate = "";
        hTempAcctMonth = "";
        hTempYear = "";

        sqlCmd = "select business_date,";
        sqlCmd += " to_char(to_date(business_date,'yyyymmdd')-6 days,'yyyymm') h_temp_acct_month,";
        sqlCmd += " to_char(to_date(business_date,'yyyymmdd')-6 days,'yyyy') h_temp_year ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if ("Y".equals(notFound)) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
        hTempAcctMonth = getValue("h_temp_acct_month");
        hTempYear = getValue("h_temp_year");

    }

    /***********************************************************************/
    void deleteMktMcardTemp() throws Exception {
        daoTable = "mkt_mcard_temp";
        deleteTable();
    }

    /***********************************************************************/
//    void select_mkt_year_target() throws Exception {
//
//        sqlCmd = "select ";
//        sqlCmd += " acct_type ";
//        sqlCmd += "from mkt_year_target ";
//        sqlCmd += "group by acct_type ";
//        int recordCnt = selectTable();
//        for (int i = 0; i < recordCnt; i++) {
//            h_mytt_acct_type = getValue("acct_type", i);
//            showLogMessage("I", "", String.format(" acct_type[%s]", h_mytt_acct_type));
//
//            delete_mkt_mcard_static();
////            delete_mkt_emp_static(); V1.00.02 delete
//            select_crd_card();
//
////            select_mkt_year_target_2(); not use V1.00.03
//        }
//
//    }

    /***********************************************************************/
    void deleteMktMcardStatic() throws Exception {
        daoTable = "mkt_mcard_static";
//        whereStr = "where acct_type = ?  ";  V1.0.6 no use
        whereStr = " where acct_month = ? ";
//        setString(1, hMyttAcctType);  V1.0.6 no use
        setString(1, hTempAcctMonth);
        deleteTable();

    }

    /***********************************************************************/
//    void delete_mkt_emp_static() throws Exception { V1.00.02 delete
//        daoTable = "mkt_emp_static";
//        whereStr = "where acct_type = ?  ";
//        whereStr += "and acct_month = ? ";
//        setString(1, h_mytt_acct_type);
//        setString(2, h_temp_acct_month);
//        deleteTable();
//
//    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.current_code,";
        sqlCmd += " a.activate_flag,";
        sqlCmd += " a.issue_date,";
        sqlCmd += " a.reg_bank_no,";
        sqlCmd += " decode(a.introduce_emp_no,'','XXXX',a.introduce_emp_no) h_card_introduce_emp_no,";
        sqlCmd += " a.old_card_no, ";
        sqlCmd += " a.id_p_seqno , a.acct_type, a.oppost_date , a.branch , a.card_no ";
        sqlCmd += "from crd_card a,v_crd_employee_unit b ";
        sqlCmd += "where a.acct_type = ? ";
        sqlCmd += "  and a.reg_bank_no != '' ";
        sqlCmd += "  and a.reg_bank_no = b.unit_no ";
//        sqlCmd += "  and substr(a.issue_date,1,6) = ? ";
        sqlCmd += "  and substr(a.issue_date,1,6) between to_char(add_months(to_date(?||'01','yyyymmdd'), -11), 'yyyymm') and ? ";  // 20211229 modify 取前十二個月
        sqlCmd += "  and old_card_no = '' "; //20200701 hesyuan mail modify 新增卡數的定義：首次發卡（非補/換/續卡）
        setString(1, hMyttAcctType);
        setString(2, hTempAcctMonth);
        setString(3, hTempAcctMonth);
        int cursorIndex = openCursor();
        while(fetchTable(cursorIndex)) {
            hCardCurrentCode = getValue("current_code");
            hCardActivateFlag = getValue("activate_flag");
            hCardIssueDate = getValue("issue_date");
            hCardRegBankNo = getValue("reg_bank_no");
            hCardIntroduceEmpNo = getValue("h_card_introduce_emp_no");
            hCardOldCardNo = getValue("old_card_no");

            hCardIdPSeqno  = getValue("id_p_seqno");
            hCardAcctType   = getValue("acct_type");
            hCardOppostDate = getValue("oppost_date");
            hCardBranch      = getValue("branch");
            hCardCardNo     = getValue("card_no");

            hMmscYNoactCardCnt = 0;
            hMmscYActCardCnt = 0;
            hMmscYStopCardCnt = 0;
            hMmscHNoactCardCnt = 0;
            hMmscHActCardCnt = 0;
            hMmscHStopCardCnt = 0;
            hMmscMNoactCardCnt = 0;
            hMmscMActCardCnt = 0;
            hMmscMStopCardCnt = 0;

            hMmscNewCardCnt = 0; /* 月招攬全新卡數 */
            hMmscOldCardCnt = 0; /* 月招攬舊卡數 */
            hMmscConsumeCardCnt = 0; /* 月有消費卡數 */
            hMmscCommonFees = 0;
            hMmscCaFees = 0;
            hMmscCfFees = 0;

            // 年度欄位
            if (hCardIssueDate.substring(0, 4).equals(hTempYear)) {
                if (!"0".equals(hCardCurrentCode)) {
                    hMmscYStopCardCnt++;
                } else {
                    if ("1".equals(hCardActivateFlag)) {
                        hMmscYNoactCardCnt++;
                    } else {
                        hMmscYActCardCnt++;
                    }
                }
                selectMktCardConsumeYear();

                // 上個月欄位
                if (hCardIssueDate.substring(0, 6).equals(hTempAcctMonth)) {
                    /* 判斷是否為全新卡 */
                    if (isNewCard(hCardIdPSeqno, hCardIssueDate)) {
                        hMmscNewCardCnt++;
                    } else {
                        hMmscOldCardCnt++;
                    }

                    if (!"0".equals(hCardCurrentCode)) {
                        hMmscMStopCardCnt++;
                        if (hCardOppostDate.length() == 0) {
                            // selectBilBill();
                            selectMktCardConsumeMonth();
                        } else {
                            if (comcr.str2int(hTempAcctMonth) <= comcr.str2int(comc.getSubString(hCardOppostDate, 0, 6))) { /* 執行日在停卡日前才計算消費 */
                                // selectBilBill();
                                selectMktCardConsumeMonth();
                            }
                        }
                    } else {
                        if ("1".equals(hCardActivateFlag)) {
                            hMmscMNoactCardCnt++;
                        } else {
                            hMmscMActCardCnt++;
                            // selectBilBill();
                            selectMktCardConsumeMonth();
                        }
                    }
                }
            }

            // 近一年欄位
            if (!"0".equals(hCardCurrentCode)) {
                hMmscHStopCardCnt++;
            } else {
                if ("1".equals(hCardActivateFlag)) {
                    hMmscHNoactCardCnt++;
                } else {
                    hMmscHActCardCnt++;
                }
            }
            selectMktCardConsumeRecentYear();


            insertMktMcardTemp();
            indexCount++;
            totalCount++;
            if (indexCount == 50000) {
                commitDataBase();
                showLogMessage("I", "", String.format("   Processed [%d] Records", totalCount));
                indexCount = 0;
            }
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    boolean isNewCard(String idPSeqno, String issueDate) throws Exception {
        // 核卡日前6個月內未持有本行任一信用卡正卡之客戶
        int tempCount = 0;

        sqlCmd = "select count(*) temp_count ";
        sqlCmd += "  from crd_card  ";
        sqlCmd += " where id_p_seqno  = ?  ";
        sqlCmd += "   and issue_date < ?  "; /*** 排除此張卡片 ***/
        sqlCmd += "   and sup_flag  = '0'  "; /*** 限正卡 ***/
        sqlCmd += "   and (    current_code = '0' " + "        or (current_code <> '0'  ";
        sqlCmd += "       and to_date(?,'yyyymmdd')-to_date(oppost_date,'yyyymmdd')<= 180)) ";
        setString(1, idPSeqno);
        setString(2, issueDate);
        setString(3, issueDate);
        selectTable();
        if (!"Y".equals(notFound)) {
            tempCount = getValueInt("temp_count");
        }
        return tempCount == 0;
    }

    /***********************************************************************/
//    void select_mkt_year_target_2() throws Exception { not use V1.00.03
//
//        sqlCmd = "select ";
//        sqlCmd += " acct_type,";
//        sqlCmd += " branch,";
//        sqlCmd += " target_card_cnt ";
//        sqlCmd += "from mkt_year_target a ";
//        sqlCmd += "where acct_year = ? ";
//        sqlCmd += "  and branch not in (select branch from mkt_mcard_temp ";
//        sqlCmd += "                      where acct_type = a.acct_type) ";
//        setString(1, h_temp_year);
//        int recordCnt = selectTable();
//        for (int i = 0; i < recordCnt; i++) {
//            h_mytt_acct_type = getValue("acct_type", i);
//            h_mmsc_branch = getValue("branch", i);
//            h_mytt_target_card_cnt = getValueInt("target_card_cnt", i);
//            h_card_reg_bank_no = h_mmsc_branch;
//
//            select_gen_brn();
//
//            h_card_introduce_emp_no = "YYYY";
//            h_mmsc_m_act_card_cnt = 0;
//            h_mmsc_m_noact_card_cnt = 0;
//            h_mmsc_m_stop_card_cnt = 0;
//            h_mmsc_y_act_card_cnt = 0;
//            h_mmsc_y_noact_card_cnt = 0;
//            h_mmsc_y_stop_card_cnt = 0;
//            h_mmsc_h_act_card_cnt = 0;
//            h_mmsc_h_noact_card_cnt = 0;
//            h_mmsc_h_stop_card_cnt = 0;
//
//
//            h_mmsc_new_card_cnt     = 0; /* 月招攬全新卡數 */
//            h_mmsc_old_card_cnt     = 0; /* 月招攬舊卡數 */
//            h_mmsc_consume_card_cnt = 0; /* 月有消費卡數 */
//            h_mmsc_common_fees      = 0;
//            h_mmsc_ca_fees          = 0;
//            h_mmsc_cf_fees          = 0;
//
//            insert_mkt_mcard_temp();
//        }
//
//    }

    /***********************************************************************/
    void insertMktMcardTemp() throws Exception {
        daoTable = "mkt_mcard_temp";
        updateSQL = " m_act_card_cnt     = m_act_card_cnt   + ?,";
        updateSQL += " m_noact_card_cnt   = m_noact_card_cnt + ?,";
        updateSQL += " m_stop_card_cnt    = m_stop_card_cnt  + ?,";
        updateSQL += " y_act_card_cnt     = y_act_card_cnt   + ?,";
        updateSQL += " y_noact_card_cnt   = y_noact_card_cnt + ?,";
        updateSQL += " y_stop_card_cnt    = y_stop_card_cnt  + ?,";
        updateSQL += " h_act_card_cnt     = h_act_card_cnt   + ?,";
        updateSQL += " h_noact_card_cnt   = h_noact_card_cnt + ?,";
        updateSQL += " h_stop_card_cnt    = h_stop_card_cnt  + ?,";

        updateSQL += " new_card_cnt       = new_card_cnt  + ?,";
        updateSQL += " old_card_cnt       = old_card_cnt  + ?,";
        updateSQL += " consume_card_cnt   = consume_card_cnt  + ?,";
        updateSQL += " common_fees        = common_fees  + ?,";
        updateSQL += " ca_fees            = ca_fees  + ?,";
        updateSQL += " cf_fees            = cf_fees  + ?,";
        updateSQL += " year_fees          = year_fees  + ?,";
        updateSQL += " y_consume_card_cnt = y_consume_card_cnt  + ?,";
        updateSQL += " h_year_fees        = h_year_fees  + ?,";
        updateSQL += " h_consume_card_cnt = h_consume_card_cnt  + ? ";

        whereStr = "where acct_type       = ?  ";
        whereStr += "and branch           = ?  ";
        whereStr += "and introduce_emp_no = ? ";

        setInt(1, hMmscMActCardCnt);
        setInt(2, hMmscMNoactCardCnt);
        setInt(3, hMmscMStopCardCnt);
        setInt(4, hMmscYActCardCnt);
        setInt(5, hMmscYNoactCardCnt);
        setInt(6, hMmscYStopCardCnt);
        setInt(7, hMmscHActCardCnt);
        setInt(8, hMmscHNoactCardCnt);
        setInt(9, hMmscHStopCardCnt);

        setInt(10, hMmscNewCardCnt);
        setInt(11, hMmscOldCardCnt);
        setInt(12, hMmscConsumeCardCnt);
        setLong(13, hMmscCommonFees);
        setLong(14, hMmscCaFees);
        setLong(15, hMmscCfFees);
        setLong(16, hMmscYearFees);
        setInt(17, hMmscYConsumeCardCnt);
        setLong(18, hMmscHYearFees);
        setInt(19, hMmscHConsumeCardCnt);

        setString(20, hMyttAcctType);
        setString(21, hCardRegBankNo);
        setString(22, hCardIntroduceEmpNo);
        updateTable();
        if (!"Y".equals(notFound)) {
            return;
        }


        setValue("acct_type", hMyttAcctType);
        setValue("branch", hCardRegBankNo);
        setValue("introduce_emp_no", hCardIntroduceEmpNo);
        setValueInt("m_act_card_cnt", hMmscMActCardCnt);
        setValueInt("m_noact_card_cnt", hMmscMNoactCardCnt);
        setValueInt("m_stop_card_cnt", hMmscMStopCardCnt);
        setValueInt("y_act_card_cnt", hMmscYActCardCnt);
        setValueInt("y_noact_card_cnt", hMmscYNoactCardCnt);
        setValueInt("y_stop_card_cnt", hMmscYStopCardCnt);
        setValueInt("h_act_card_cnt", hMmscHActCardCnt);
        setValueInt("h_noact_card_cnt", hMmscHNoactCardCnt);
        setValueInt("h_stop_card_cnt", hMmscHStopCardCnt);

        setValueInt("new_card_cnt", hMmscNewCardCnt);
        setValueInt("old_card_cnt", hMmscOldCardCnt);
        setValueInt("consume_card_cnt", hMmscConsumeCardCnt);
        setValueLong("common_fees", hMmscCommonFees);
        setValueLong("ca_fees", hMmscCaFees);
        setValueLong("cf_fees", hMmscCfFees);
        setValueLong("year_fees", hMmscYearFees);
        setValueInt("y_consume_card_cnt", hMmscYConsumeCardCnt);
        setValueLong("h_year_fees", hMmscHYearFees);
        setValueInt("h_consume_card_cnt", hMmscHConsumeCardCnt);

        setValue("mod_user", hMyttModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hMyttModPgm);
        daoTable = "mkt_mcard_temp";
        insertTable();

    }

    /***********************************************************************/
//    void select_mkt_mcard_temp_1() throws Exception { V1.00.02 delete
//        int sort_tag = 0;
//        int h_mesc_h_sum_cnt_tmp = 0;
//        int h_mesc_total_sort_tmp = 0;
//        sqlCmd = "select ";
//        sqlCmd += " sum(m_noact_card_cnt+m_act_card_cnt) h_mesc_sum_cnt,";
//        sqlCmd += " sum(y_noact_card_cnt+y_act_card_cnt) h_mesc_y_sum_cnt,";
//        sqlCmd += " sum(h_noact_card_cnt+h_act_card_cnt) h_mesc_h_sum_cnt,";
//        sqlCmd += " max(chi_name) h_mesc_chi_name,";
//        sqlCmd += " acct_type,";
//        sqlCmd += " b.unit_no,";
//        sqlCmd += " introduce_emp_no, ";
//
//        sqlCmd += " sum(new_card_cnt)     h_mesc_new_card_cnt , ";
//        sqlCmd += " sum(old_card_cnt)     h_mesc_old_card_cnt , ";
//        sqlCmd += " sum(consume_card_cnt) h_mesc_consume_card_cnt , ";
//        sqlCmd += " sum(common_fees)      h_mesc_common_fees , ";
//        sqlCmd += " sum(ca_fees)          h_mesc_ca_fees , ";
//        sqlCmd += " sum(cf_fees)          h_mesc_cf_fees  ";
//
//        sqlCmd += "  from mkt_mcard_temp a,crd_employee b ";
//        sqlCmd += " where a.introduce_emp_no = b.employ_no ";
//        sqlCmd += " group by acct_type,b.unit_no,introduce_emp_no ";
//        sqlCmd += " order by sum(h_act_card_cnt + h_noact_card_cnt) desc ";
//        int cursorIndex = openCursor();
//        int i = 0;
//        while (fetchTable(cursorIndex)) {
//            h_mesc_sum_cnt = getValueInt("h_mesc_sum_cnt");
//            h_mesc_y_sum_cnt = getValueInt("h_mesc_y_sum_cnt");
//            h_mesc_h_sum_cnt = getValueInt("h_mesc_h_sum_cnt");
//            h_mesc_chi_name = getValue("h_mesc_chi_name");
//            h_mesc_acct_type = getValue("acct_type");
//            h_mesc_branch = getValue("unit_no");
//            h_mesc_introduce_emp_no = getValue("introduce_emp_no");
//
//            h_mesc_new_card_cnt     = getValueInt("h_mesc_new_card_cnt");
//            h_mesc_old_card_cnt     = getValueInt("h_mesc_old_card_cnt");
//            h_mesc_consume_card_cnt = getValueInt("h_mesc_consume_card_cnt");
//            h_mesc_common_fees      = getValueLong("h_mesc_common_fees");
//            h_mesc_ca_fees          = getValueLong("h_mesc_ca_fees");
//            h_mesc_cf_fees          = getValueLong("h_mesc_cf_fees");
//
//            h_mesc_total_sort = i + 1;
//
//            if ((i > 0) && (h_mesc_h_sum_cnt == h_mesc_h_sum_cnt_tmp))
//                h_mesc_total_sort = h_mesc_total_sort_tmp;
//
//            if ((h_mesc_total_sort > 100) || (sort_tag == 1))
//                h_mesc_total_sort = 0;
//
//            insert_mkt_emp_static();
//
//            h_mesc_h_sum_cnt_tmp = h_mesc_h_sum_cnt;
//            h_mesc_total_sort_tmp = h_mesc_total_sort;
//            i++;
//        }
//        closeCursor(cursorIndex);
//        sort_tag = 1;
//
//    }

    /***********************************************************************/
//    void insert_mkt_emp_static() throws Exception { V1.00.02 delete
//        setValue("acct_type", h_mytt_acct_type);
//        setValue("acct_month", h_temp_acct_month);
//        setValue("branch", h_mesc_branch);
//        setValue("introduce_emp_no", h_mesc_introduce_emp_no);
//        setValueInt("m_sum_cnt", h_mesc_sum_cnt);
//        setValueInt("y_sum_cnt", h_mesc_y_sum_cnt);
//        setValueInt("h_sum_cnt", h_mesc_h_sum_cnt);
//        setValueInt("total_sort", h_mesc_total_sort);
//
//        setValueInt("new_card_cnt", h_mesc_new_card_cnt);
//        setValueInt("old_card_cnt", h_mesc_old_card_cnt);
//        setValueInt("consume_card_cnt", h_mesc_consume_card_cnt);
//        setValueLong("common_fees", h_mesc_common_fees);
//        setValueLong("ca_fees", h_mesc_ca_fees);
//        setValueLong("cf_fees", h_mesc_cf_fees);
//
//        setValue("mod_user", h_mytt_mod_user);
//        setValue("mod_time", sysDate + sysTime);
//        setValue("mod_pgm", h_mytt_mod_pgm);
//        daoTable = "mkt_emp_static";
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.err_rtn("insert_mkt_emp_static duplicate!", "", h_call_batch_seqno);
//        }
//
//    }

//    /***********************************************************************/
//    void select_mkt_emp_static() throws Exception { V1.00.02 delete
//        sqlCmd = "select ";
//        sqlCmd += " branch ";
//        sqlCmd += "  from mkt_emp_static ";
//        sqlCmd += " where acct_type = ? ";
//        sqlCmd += "   and acct_month = ? ";
//        sqlCmd += " group by branch ";
//        setString(1, h_mytt_acct_type);
//        setString(2, h_temp_acct_month);
//        int cursorIndex = openCursor();
//        while (fetchTable(cursorIndex)) {
//            h_mesc_branch = getValue("branch");
//
//            dep_sort_int = 0;
//            select_mkt_emp_static_1();
//        }
//        closeCursor(cursorIndex);
//
//    }

//    /***********************************************************************/
//    void select_mkt_emp_static_1() throws Exception { V1.00.02 delete
//
//        int h_mesc_h_sum_cnt_tmp = 0;
//        int h_mesc_dep_sort_tmp = 0;
//
//        sqlCmd = "select ";
//        sqlCmd += " rowid rowid,";
//        sqlCmd += " h_sum_cnt ";
//        sqlCmd += " from mkt_emp_static ";
//        sqlCmd += "where acct_type = ? ";
//        sqlCmd += "  and acct_month = ? ";
//        sqlCmd += "  and branch = ? ";
//        sqlCmd += "order by h_sum_cnt desc ";
//        setString(1, h_mytt_acct_type);
//        setString(2, h_temp_acct_month);
//        setString(3, h_mesc_branch);
//        int recordCnt = selectTable();
//        for (int i = 0; i < recordCnt; i++) {
//            h_mesc_rowid = getValue("rowid", i);
//            h_mesc_h_sum_cnt = getValueInt("h_sum_cnt", i);
//
//            h_mesc_dep_sort = i + 1;
//
//            if ((i > 0) && (h_mesc_h_sum_cnt == h_mesc_h_sum_cnt_tmp))
//                h_mesc_dep_sort = h_mesc_dep_sort_tmp;
//
//            update_mkt_emp_static();
//
//            h_mesc_h_sum_cnt_tmp = h_mesc_h_sum_cnt;
//            h_mesc_dep_sort_tmp = h_mesc_dep_sort;
//        }
//
//    }

//    /***********************************************************************/
//    void update_mkt_emp_static() throws Exception { V1.00.02 delete
//        daoTable = "mkt_emp_static";
//        updateSQL = "dep_sort = ?";
//        whereStr = "where rowid =? ";
//        setInt(1, h_mesc_dep_sort);
//        setRowId(2, h_mesc_rowid);
//        updateTable();
//        if (notFound.equals("Y")) {
//            comcr.err_rtn("update_mkt_emp_static not found!", "", h_call_batch_seqno);
//        }
//
//    }

    /***********************************************************************/
    void selectMktMcardTemp2() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " sum(m_act_card_cnt)                                  h_mmsc_act_card_cnt,";
        sqlCmd += " sum(m_noact_card_cnt)                                h_mmsc_noact_card_cnt,";
        sqlCmd += " sum(m_stop_card_cnt)                                 h_mmsc_stop_card_cnt,";
        sqlCmd += " sum(m_noact_card_cnt+m_act_card_cnt+m_stop_card_cnt) h_mmsc_sum_cnt,";
        sqlCmd += " sum(y_act_card_cnt)                                  h_mmsc_y_act_card_cnt,";
        sqlCmd += " sum(y_noact_card_cnt)                                h_mmsc_y_noact_card_cnt,";
        sqlCmd += " sum(y_stop_card_cnt)                                 h_mmsc_y_stop_card_cnt,";
        sqlCmd += " sum(y_noact_card_cnt+y_act_card_cnt+y_stop_card_cnt) h_mmsc_y_sum_cnt,";
        sqlCmd += " sum(h_act_card_cnt)                                  h_mmsc_h_act_card_cnt,";
        sqlCmd += " sum(h_noact_card_cnt)                                h_mmsc_h_noact_card_cnt,";
        sqlCmd += " sum(h_stop_card_cnt)                                 h_mmsc_h_stop_card_cnt,";
        sqlCmd += " sum(h_noact_card_cnt+h_act_card_cnt+h_stop_card_cnt) h_mmsc_h_sum_cnt,";
        sqlCmd += " acct_type,";
        sqlCmd += " branch, ";
        sqlCmd += " introduce_emp_no, ";
        sqlCmd += " sum(ca_fees)                                         h_mmsc_ca_fees,";
        sqlCmd += " sum(cf_fees)                                         h_mmsc_cf_fees, ";
        sqlCmd += " sum(new_card_cnt)                                    h_mmsc_new_card_cnt,";
        sqlCmd += " sum(old_card_cnt)                                    h_mmsc_old_card_cnt,";
        sqlCmd += " sum(common_fees)                                     h_mmsc_common_fees,";
        sqlCmd += " sum(consume_card_cnt)                                h_mmsc_consume_card_cnt,";
        sqlCmd += " sum(year_fees)                                       h_mmsc_year_fees,";
        sqlCmd += " sum(y_consume_card_cnt)                              h_mmsc_y_consume_card_cnt,";
        sqlCmd += " sum(h_year_fees)                                     h_mmsc_h_year_fees,";
        sqlCmd += " sum(h_consume_card_cnt)                              h_mmsc_h_consume_card_cnt ";
        sqlCmd += "from mkt_mcard_temp ";
        sqlCmd += "group by acct_type,branch,introduce_emp_no ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hMmscMActCardCnt = getValueInt("h_mmsc_act_card_cnt");
            hMmscMNoactCardCnt = getValueInt("h_mmsc_noact_card_cnt");
            hMmscMStopCardCnt = getValueInt("h_mmsc_stop_card_cnt");
            hMmscMSumCnt = getValueInt("h_mmsc_sum_cnt");
            hMmscYActCardCnt = getValueInt("h_mmsc_y_act_card_cnt");
            hMmscYNoactCardCnt = getValueInt("h_mmsc_y_noact_card_cnt");
            hMmscYStopCardCnt = getValueInt("h_mmsc_y_stop_card_cnt");
            hMmscYSumCnt = getValueInt("h_mmsc_y_sum_cnt");
            hMmscHActCardCnt = getValueInt("h_mmsc_h_act_card_cnt");
            hMmscHNoactCardCnt = getValueInt("h_mmsc_h_noact_card_cnt");
            hMmscHStopCardCnt = getValueInt("h_mmsc_h_stop_card_cnt");
            hMmscHSumCnt = getValueInt("h_mmsc_h_sum_cnt");
            hMmscAcctType = getValue("acct_type");
            hMmscBranch = getValue("branch");
            hMmscIntroduceEmpNo = getValue("introduce_emp_no");
            hMmscNewCardCnt = getValueInt("h_mmsc_new_card_cnt");
            hMmscOldCardCnt = getValueInt("h_mmsc_old_card_cnt");
            hMmscCommonFees = getValueLong("h_mmsc_common_fees");
            hMmscConsumeCardCnt = getValueInt("h_mmsc_consume_card_cnt");
            hMmscYearFees = getValueLong("h_mmsc_year_fees");
            hMmscYConsumeCardCnt = getValueInt("h_mmsc_y_consume_card_cnt");
            hMmscHYearFees = getValueLong("h_mmsc_h_year_fees");
            hMmscHConsumeCardCnt = getValueInt("h_mmsc_h_consume_card_cnt");
            hMmscCaFees     = getValueLong("h_mmsc_ca_fees");
            hMmscCfFees     = getValueLong("h_mmsc_cf_fees");

            selectGenBrn();
           /* select_mkt_year_target_1(); not use V1.00.03
            if ((h_mytt_spread_months != 0) && (h_mytt_target_card_cnt != 0)) {
                h_mmsc_m_rate = h_mmsc_m_sum_cnt * 100.0 / (h_mytt_target_card_cnt / h_mytt_spread_months);
                h_mmsc_y_rate = (h_mmsc_y_sum_cnt * 100.0 / h_mytt_target_card_cnt) * (12 / h_mytt_spread_months);
            } else {
                h_mmsc_m_rate = 0;
                h_mmsc_y_rate = 0;
            }
            if (h_mmsc_m_rate >= 1000)
                h_mmsc_m_rate = 999.99;
            if (h_mmsc_y_rate >= 1000)
                h_mmsc_y_rate = 999.99;*/

            insertMktMcardStatic();
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectGenBrn() throws Exception {
        hPbnhBranchName = "";

        sqlCmd = "select brief_chi_name ";
        sqlCmd += " from gen_brn  ";
        sqlCmd += "where branch = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hMmscBranch);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hPbnhBranchName = getValue("brief_chi_name");
        }

    }

    /***********************************************************************/
//    void select_mkt_year_target_1() throws Exception { not use V1.00.03
//        h_mytt_target_card_cnt = 0;
//        h_mytt_spread_months = 0;
//
//        sqlCmd = "select target_card_cnt,";
//        sqlCmd += " spread_months ";
//        sqlCmd += " from mkt_year_target  ";
//        sqlCmd += "where branch = ?  ";
//        sqlCmd += "  and acct_year = ?  ";
//        sqlCmd += "  and acct_type = ? ";
//        setString(1, h_mmsc_branch);
//        setString(2, h_temp_year);
//        setString(3, h_mytt_acct_type);
//        int recordCnt = selectTable();
//        if (recordCnt > 0) {
//            h_mytt_target_card_cnt = getValueInt("target_card_cnt");
//            h_mytt_spread_months = getValueLong("spread_months");
//        }
//
//    }

    /***********************************************************************/
// no use V1.0.6 remove
//    void selectBilBill() throws Exception {
//
//        sqlCmd  = " SELECT sum (decode (acct_code, ";
//        sqlCmd += "                     'BL', ";
//        sqlCmd += "                     decode (sign_flag, '-', 0 - dest_amt, dest_amt), ";
//        sqlCmd += "                     'IT', ";
//        sqlCmd += "                     decode (sign_flag, '-', 0 - dest_amt, dest_amt), ";
//        sqlCmd += "                     'OT', ";
//        sqlCmd += "                     decode (sign_flag, '-', 0 - dest_amt, dest_amt), ";
//        sqlCmd += "                     'ID', ";
//        sqlCmd += "                     decode (sign_flag, '-', 0 - dest_amt, dest_amt), ";
//        sqlCmd += "                     'AO', ";
//        sqlCmd += "                     decode (sign_flag, '-', 0 - dest_amt, dest_amt), ";
//        sqlCmd += "                     0)) h_mmsc_common_fees, "; // 一般消費金額
//        sqlCmd += "        sum (decode (acct_code, ";
//        sqlCmd += "                     'CA', ";
//        sqlCmd += "                     decode (sign_flag, '-', 0 - dest_amt, dest_amt), ";
//        sqlCmd += "                     0)) h_mmsc_ca_fees, "; // 有預借現金消費金額
//        sqlCmd += "        sum (decode (acct_code, ";
//        sqlCmd += "                     'CF', ";
//        sqlCmd += "                     decode (sign_flag, '-', 0 - dest_amt, dest_amt), ";
//        sqlCmd += "                     0)) h_mmsc_cf_fees "; // 有手收消費金額
//        sqlCmd += "   FROM bil_bill ";
//        sqlCmd += "  WHERE acct_month = ? ";
//        sqlCmd += "    AND reg_bank_no = ? ";
//        sqlCmd += "    AND card_no = ? ";
//        sqlCmd += "    AND acct_type = ? ";
//        setString(1, hTempAcctMonth);
//        setString(2, hCardBranch);
//        setString(3, hCardCardNo);
//        setString(4, hCardAcctType);
//        int recordCnt = selectTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("select_bil_bill not found!", "", hCallBatchSeqno);
//        }
//        if (recordCnt > 0) {
//            hMmscCommonFees = getValueLong("h_mmsc_common_fees");
//            hMmscCaFees     = getValueLong("h_mmsc_ca_fees");
//            hMmscCfFees     = getValueLong("h_mmsc_cf_fees");
//        }
//
//        if (hMmscCommonFees + hMmscCaFees + hMmscCfFees > 0)
//            hMmscConsumeCardCnt++; /** 月有消費卡數 **/
//
//    }

    void selectMktCardConsumeMonth() throws Exception {
        // V1.00.06 add
        // 上個月消費金額
        sqlCmd = " SELECT sum(consume_bl_amt+consume_it_amt+consume_ot_amt+consume_id_amt+consume_ao_amt) as h_mmsc_common_fees, "; // 一般消費金額
        sqlCmd += "       sum(consume_ca_amt) as h_mmsc_ca_fees  "; // 有預借現金消費金額
        sqlCmd += "   FROM mkt_card_consume ";
        sqlCmd += "  WHERE acct_month = ? ";
        sqlCmd += "    AND card_no = ? ";
        sqlCmd += "    AND acct_type = ? ";
        setString(1, hTempAcctMonth);
        setString(2, hCardCardNo);
        setString(3, hCardAcctType);
        int recordCnt = selectTable();
        if ("Y".equals(notFound)) {
            comcr.errRtn(String.format("select_mkt_card_consume not found!, acct_month[%s], card_no[%s]", hTempAcctMonth, hCardCardNo), "",
                    hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hMmscCommonFees = getValueLong("h_mmsc_common_fees");
            hMmscCaFees = getValueLong("h_mmsc_ca_fees");
        }

        if (hMmscCommonFees + hMmscCaFees > 0) {
            hMmscConsumeCardCnt++;
        } /** 月有消費卡數 **/
    }

    /***********************************************************************/
    void selectMktCardConsumeYear() throws Exception {
        // V1.00.06 add
        // 年度消費金額
        hMmscCfFees = 0;
        sqlCmd = " SELECT sum(consume_bl_amt+consume_it_amt+consume_ot_amt+consume_id_amt+consume_ao_amt+consume_ca_amt) as h_mmsc_year_fees "; // 一般消費金額
        sqlCmd += "   FROM mkt_card_consume ";
        sqlCmd += "  WHERE substr(acct_month,1,4) = ? ";
        sqlCmd += "    AND card_no = ? ";
        sqlCmd += "    AND acct_type = ? ";
        setString(1, hTempYear);
        setString(2, hCardCardNo);
        setString(3, hCardAcctType);
        int recordCnt = selectTable();
        if ("Y".equals(notFound)) {
            comcr.errRtn(String.format("select_mkt_card_consume not found!, acct_month[%s], card_no[%s]", hTempAcctMonth, hCardCardNo), "",
                    hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hMmscYearFees = getValueLong("h_mmsc_year_fees");
        }

        if (hMmscCommonFees > 0) {
            hMmscYConsumeCardCnt++;
        } /** 年度有消費卡數 **/
    }

    /***********************************************************************/
    void selectMktCardConsumeRecentYear() throws Exception {
        // V1.00.06 add
        // 近一年消費金額
        hMmscCfFees = 0;
        sqlCmd = " SELECT sum(consume_bl_amt+consume_it_amt+consume_ot_amt+consume_id_amt+consume_ao_amt+consume_ca_amt) as h_mmsc_h_year_fees "; // 近一年一般消費金額
        sqlCmd += "   FROM mkt_card_consume ";
        sqlCmd += "  WHERE acct_month between to_char(add_months(to_date(?||'01','yyyymmdd'), -11), 'yyyymm') and ? ";
        sqlCmd += "    AND card_no = ? ";
        sqlCmd += "    AND acct_type = ? ";
        setString(1, hTempAcctMonth);
        setString(2, hTempAcctMonth);
        setString(3, hCardCardNo);
        setString(4, hCardAcctType);
        int recordCnt = selectTable();
        if ("Y".equals(notFound)) {
            comcr.errRtn(String.format("select_mkt_card_consume not found!, acct_month[%s], card_no[%s]", hTempAcctMonth, hCardCardNo), "",
                    hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hMmscHYearFees = getValueLong("h_mmsc_h_year_fees");
        }

        if (hMmscCommonFees + hMmscCaFees > 0) {
            hMmscHConsumeCardCnt++;
        } /** 近一年有消費卡數 **/
    }

    /***********************************************************************/
    void insertMktMcardStatic() throws Exception {

        setValue("acct_type", hMmscAcctType);
        setValue("acct_month", hTempAcctMonth);
        setValue("branch", hMmscBranch);
        setValue("branch_name", hPbnhBranchName);
        setValue("introduce_emp_no", hMmscIntroduceEmpNo);
        setValueInt("target_card_cnt", hMyttTargetCardCnt);
        setValueInt("m_act_card_cnt", hMmscMActCardCnt);
        setValueInt("m_noact_card_cnt", hMmscMNoactCardCnt);
        setValueInt("m_stop_card_cnt", hMmscMStopCardCnt);
        setValueInt("m_sum_cnt", hMmscMSumCnt);
        setValueDouble("m_rate", hMmscMRate);
        setValueInt("y_act_card_cnt", hMmscYActCardCnt);
        setValueInt("y_noact_card_cnt", hMmscYNoactCardCnt);
        setValueInt("y_stop_card_cnt", hMmscYStopCardCnt);
        setValueInt("y_sum_cnt", hMmscYSumCnt);
        setValueDouble("y_rate", hMmscYRate);
        setValueInt("h_act_card_cnt", hMmscHActCardCnt);
        setValueInt("h_noact_card_cnt", hMmscHNoactCardCnt);
        setValueInt("h_stop_card_cnt", hMmscHStopCardCnt);
        setValueInt("h_sum_cnt", hMmscHSumCnt);
        setValueLong("common_fees", hMmscCommonFees);
        setValueLong("ca_fees", hMmscCaFees);
        setValueLong("cf_fees", hMmscCfFees);
        setValueInt("new_card_cnt", hMmscNewCardCnt);
        setValueInt("old_card_cnt", hMmscOldCardCnt);
        setValueInt("consume_card_cnt", hMmscConsumeCardCnt);
        setValueLong("year_fees", hMmscYearFees);
        setValueInt("y_consume_card_cnt", hMmscYConsumeCardCnt);
        setValueLong("h_year_fees", hMmscHYearFees);
        setValueInt("h_consume_card_cnt", hMmscHConsumeCardCnt);
        setValue("mod_user", hMyttModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hMyttModPgm);
        daoTable = "mkt_mcard_static";
        insertTable();
        if ("Y".equals(dupRecord)) {
            comcr.errRtn("insert_mkt_mcard_static duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectMktMcardStatic1() throws Exception {
        int i = 0;

        sqlCmd = "select ";
        sqlCmd += " rowid rowid ";
        sqlCmd += "from mkt_mcard_static ";
        sqlCmd += "where acct_type = ? ";
        sqlCmd += "  and acct_month = ? ";
        sqlCmd += "order by consume_card_cnt desc," ;
        sqlCmd += " (m_act_card_cnt + m_noact_card_cnt) desc, ";
        sqlCmd += " (common_fees + ca_fees) desc ";
        setString(1, hMyttAcctType);
        setString(2, hTempAcctMonth);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hMmscRowid = getValue("rowid");
            hMmscMSort = i + 1;

            updateMktMcardStatic1();
            i++;
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void updateMktMcardStatic1() throws Exception {
        daoTable = "mkt_mcard_static";
        updateSQL = "m_sort = ?";
        whereStr = "where rowid = ? ";
        setInt(1, hMmscMSort);
        setRowId(2, hMmscRowid);
        updateTable();
        if ("Y".equals(notFound)) {
            comcr.errRtn("update_mkt_mcard_static not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectMktMcardStatic2() throws Exception {
        int i = 0;
        sqlCmd = "select ";
        sqlCmd += " rowid rowid ";
        sqlCmd += "from mkt_mcard_static ";
        sqlCmd += "where acct_type = ? ";
        sqlCmd += "  and acct_month = ? ";
        sqlCmd += "order by y_consume_card_cnt desc,";
        sqlCmd += " (y_act_card_cnt + y_noact_card_cnt) desc, ";
        sqlCmd += " year_fees desc ";
        setString(1, hMyttAcctType);
        setString(2, hTempAcctMonth);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hMmscRowid = getValue("rowid");
            hMmscYSort = i + 1;

            updateMktMcardStatic2();
            i++;
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void updateMktMcardStatic2() throws Exception {
        daoTable = "mkt_mcard_static";
        updateSQL = "y_sort = ?";
        whereStr = "where rowid = ? ";
        setInt(1, hMmscYSort);
        setRowId(2, hMmscRowid);
        updateTable();
        if ("Y".equals(notFound)) {
            comcr.errRtn("update_mkt_mcard_static not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktA730 proc = new MktA730();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

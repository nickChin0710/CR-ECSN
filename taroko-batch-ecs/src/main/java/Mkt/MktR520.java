/******************************************************************************
 *                                                                            *
 * MODIFICATION LOG                                                           *
 *                                                                            *
 * DATE      Version   AUTHOR      DESCRIPTION                                * 
 * --------- --------- ----------- ----------------------------------------   *
 * 112/02/03 V1.00.00  Zuwei Su    program initial                            *
 * 112/02/06 V1.00.01  Zuwei Su    insert mkt_member_log error                *
 ******************************************************************************/

package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;

/* 聯名機構推卡獎勵統計明細處理程式 */
public class MktR520 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private final String PROGNAME = "聯名機構推卡獎勵統計明細處理程式  112/02/06 V1.00.01";
    private CommCrd comc = new CommCrd();
    private CommCrdRoutine comcr = null;
    private String hCallBatchseqno = "";
    private String hBusiBusinessDate = "";
    private String hWdayStmtCycle = "";
    private String hWdayThisAcctMonth = "";
    private String hConsumeBl = "";
    private String hConsumeCa = "";
    private String hConsumeIt = "";
    private String hConsumeId = "";
    private String hConsumeOt = "";
    private String hConsumeAo = "";
    private String hProjDateS = "";
    private String hProjDateE = "";
    private String hBillReferenceNo = "";
    private double hBillDestinationAmt = 0;
    private String hCardCardNo = "";
    private String hCardIssueDate = "";
    private int totalCnt = 0;
    private int inta = 0;
    private int tempTime = 1;
    private String[] tempWday = {
            "03", "06", "09", "12", "15", "18", "21", "24", "27"
    };
    private String tmpstr = "";
    private String projCode;
    private String acctTypeFlag;
    private String cardTypeFlag;
    private String groupCodeFlag;
    private String mccFlag;
    private String mchtFlag;

    private String hBillEcsPlatformKind;

    private String hCardAcctType;

    private String hCardGroupCode;

    private String hCardIdPSeqno;

    private String hCardPromoteDept;

    private String hCardPromoteEmpNo;

    private String hCardStaffBranchNo;

    private String hCardMemberId;

    private String hCardRegBankNo;

    private String hCardClerkId;

    private String hCardIntroduceEmpNo;

    private String hCardIntroduceId;

    private String hCardProdNo;

    private String hCardElectronicCode;

    private int hBillPlatformKindCnt;

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : MktR520 [[businessdate][acct_month]]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            // comcr.h_call_batch_seqno = h_call_batch_seqno;
            // comcr.h_call_r_program_code = javaProgram;

            // comcr.callbatch(0, 0, 0);

            tempTime = 1;
            hBusiBusinessDate = "";
            if ((args.length == 1) && (args[0].length() == 8)) {
                hBusiBusinessDate = args[0];
                tmpstr = String.format("%6.6s", hBusiBusinessDate);
                hWdayThisAcctMonth = tmpstr;
                tmpstr = String.format("%2.2s", hBusiBusinessDate.substring(6));
                hWdayStmtCycle = tmpstr;
            } else if ((args.length == 1) && (args[0].length() == 6)) {
                tempTime = 9;
            } else {
                selectPtrBusinday();

                if (selectPtrWorkday() != 0) {
                    exceptExit = 0;
                    comcr.errRtn(
                            String.format("Today[%s] is not cycle_date. It will not process ! ",
                                    hBusiBusinessDate),
                            "", hCallBatchseqno);
                }
            }

            if (notFound.equals("Y")) {
                showLogMessage("I", "", "Not found mkt_jointly_parm record.");
                return 1;
            }

            if (tempTime == 1) {
                showLogMessage("I", "", String.format("Processing acct_month[%s] stmt_cycle[%s]",
                        hWdayThisAcctMonth, hWdayStmtCycle));
                selectMktJointlyParm();
            } else {
                for (inta = 0; inta < 9; inta++) {
                    tmpstr = String.format("%6.6s%2.2s", args[0], tempWday[inta]);
                    hBusiBusinessDate = tmpstr;
                    tmpstr = String.format("%6.6s", hBusiBusinessDate);
                    hWdayThisAcctMonth = tmpstr;
                    tmpstr = String.format("%2.2s", hBusiBusinessDate.substring(6));
                    hWdayStmtCycle = tmpstr;
                    showLogMessage("I", "", String.format("[%s]", hBusiBusinessDate));
                    showLogMessage("I", "",
                            String.format("Processing acct_month[%s] stmt_cycle[%s]",
                                    hWdayThisAcctMonth, hWdayStmtCycle));
                    selectMktJointlyParm();
                }
            }

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
    private void selectMktJointlyParm() throws Exception {
        sqlCmd = "select ";
        sqlCmd += " proj_code,";
        sqlCmd += " acct_type_flag,";
        sqlCmd += " card_type_flag,";
        sqlCmd += " group_code_flag,";
        sqlCmd += " mcc_flag,";
        sqlCmd += " mcht_flag, ";
        sqlCmd += " consume_bl, ";
        sqlCmd += " consume_ca, ";
        sqlCmd += " consume_it, ";
        sqlCmd += " consume_ao, ";
        sqlCmd += " consume_id, ";
        sqlCmd += " consume_ot, ";
        sqlCmd += " proj_date_s, ";
        sqlCmd += " proj_date_e ";
        sqlCmd += "from mkt_jointly_parm ";
        sqlCmd += "where apr_flag = 'Y' ";
        sqlCmd += "  and decode(proj_date_s,'','20100101',proj_date_s) <= ? ";
        sqlCmd += "  and proj_date_e >= ? ";
        sqlCmd += "  and ( decode(acct_type_flag,'','0',acct_type_flag) != '0'";
        sqlCmd += "  or decode(card_type_flag,'','0',card_type_flag) != '0'";
        sqlCmd += "  or decode(group_code_flag,'','0',group_code_flag) != '0'";
        sqlCmd += "  or decode(mcc_flag,'','0',mcc_flag) != '0'";
        sqlCmd += "  or decode(mcht_flag,'','0',mcht_flag) != '0' ) ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            projCode = getValue("proj_code");
            acctTypeFlag = getValue("acct_type_flag");
            cardTypeFlag = getValue("card_type_flag");
            groupCodeFlag = getValue("group_code_flag");
            mccFlag = getValue("mcc_flag");
            mchtFlag = getValue("mcht_flag");
            hConsumeBl = getValue("consume_bl");
            hConsumeCa = getValue("consume_ca");
            hConsumeIt = getValue("consume_it");
            hConsumeAo = getValue("consume_ao");
            hConsumeId = getValue("consume_id");
            hConsumeOt = getValue("consume_ot");

            hProjDateS = getValue("proj_date_s");
            hProjDateE = getValue("proj_date_e");

            showLogMessage("I", "",
                    String.format("Fund_code=[%s] Processing....", projCode));
            totalCnt = 0;
            selectCrdCard();
            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    private void selectPtrBusinday() throws Exception {
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,?) h_busi_business_date ";
        sqlCmd += "  from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchseqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
        }

    }

    /***********************************************************************/
    private int selectPtrWorkday() throws Exception {

        sqlCmd = "select stmt_cycle,";
        sqlCmd += " this_acct_month ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where this_close_date = to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayThisAcctMonth = getValue("this_acct_month");
            return (0);
        } else
            return (1);
    }

    /***********************************************************************/
    private void selectCrdCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd +=
                " decode(c.txn_code,'06',-1,'25',-1,'27',-1,'28',-1,'29',-1,1)*c.dest_amt h_bill_destination_amt,";
        sqlCmd += " a.card_no,";
        sqlCmd += " a.issue_date,";
        sqlCmd += " a.acct_type, ";
        sqlCmd += " a.group_code, ";
        sqlCmd += " a.id_p_seqno, ";
        sqlCmd += " a.promote_dept, ";
        sqlCmd += " a.promote_emp_no, ";
        sqlCmd += " a.staff_branch_no, ";
        sqlCmd += " a.member_id, ";
        sqlCmd += " a.reg_bank_no, ";
        sqlCmd += " a.clerk_id, ";
        sqlCmd += " a.introduce_emp_no, ";
        sqlCmd += " a.introduce_id, ";
        sqlCmd += " a.prod_no, ";
        sqlCmd += " a.electronic_code, ";
        sqlCmd += " c.reference_no, ";
        sqlCmd += " c.dest_amt, ";
        sqlCmd += " c.ecs_platform_kind, ";
        sqlCmd += " d.platform_kind_cnt ";
        sqlCmd += " from crd_card a " 
                + " inner join bil_bill c on a.card_no = c.card_no ";
        sqlCmd += " left join (select count(1) as platform_kind_cnt, platform_kind "
                + "     from BIL_PLATFORM  group by PLATFORM_KIND) as d "
                + "     on d.platform_kind = c.ecs_platform_kind ";
        sqlCmd += " where ( a.staff_branch_no != '' or a.member_id != '' ) ";
        String cond;
        int paramCnt = 0;
        if ("1".equals(acctTypeFlag) || "2".equals(acctTypeFlag)) {
            cond = "1".equals(acctTypeFlag) ? "in" : "not in";
            sqlCmd += " and a.acct_type " + cond;
            sqlCmd += "     (select decode(data_code, '', decode(a.acct_type, '', 'x', a.acct_type), data_code) ";
            sqlCmd += "      from mkt_jointly_parm_detl ";
            sqlCmd += "      where proj_code = ? ";
            sqlCmd += "            and data_type = '01' )";
            setString(++paramCnt, projCode);
            
        }
        if ("1".equals(cardTypeFlag) || "2".equals(cardTypeFlag)) {
            cond = "1".equals(cardTypeFlag) ? "in" : "not in";
            sqlCmd += " and a.card_type " + cond;
            sqlCmd += "     (select decode(data_code, '', decode(a.card_type, '', 'x', a.card_type), data_code) ";
            sqlCmd += "      from mkt_jointly_parm_detl ";
            sqlCmd += "      where proj_code = ? ";
            sqlCmd += "            and data_type = '02' )";
            setString(++paramCnt, projCode);
        }
        if ("1".equals(groupCodeFlag) || "2".equals(groupCodeFlag)) {
            cond = "1".equals(groupCodeFlag) ? "in" : "not in";
            sqlCmd += " and a.group_code " + cond;
            sqlCmd += "     (select decode(data_code, '', decode(a.group_code, '', 'x', a.group_code), data_code) ";
            sqlCmd += "      from mkt_jointly_parm_detl ";
            sqlCmd += "      where proj_code = ? ";
            sqlCmd += "            and data_type = '03' )";
            setString(++paramCnt, projCode);
        }
        if ("1".equals(mccFlag) || "2".equals(mccFlag)) {
            cond = "1".equals(mccFlag) ? "in" : "not in";
            sqlCmd += " and b.mcht_category " + cond;
            sqlCmd += "     (select decode(data_code, '', decode(b.mcht_category, '', 'x', b.mcht_category), data_code) ";
            sqlCmd += "      from mkt_jointly_parm_detl ";
            sqlCmd += "      where proj_code = ? ";
            sqlCmd += "            and data_type = '04' )";
            setString(++paramCnt, projCode);
        }
        if ("1".equals(mchtFlag) || "2".equals(mchtFlag)) {
            cond = "1".equals(mchtFlag) ? "in" : "not in";
            sqlCmd += " and b.mcht_no " + cond;
            sqlCmd += "     (select decode(data_code, '', decode(b.mcht_no, '', 'x', b.mcht_no), data_code) ";
            sqlCmd += "      from mkt_jointly_parm_detl ";
            sqlCmd += "      where proj_code = ? ";
            sqlCmd += "            and data_type = '05' )";
            setString(++paramCnt, projCode);
        }
        sqlCmd += "   and c.acct_month||a.stmt_cycle between ? and ? ";
        sqlCmd += "   and decode(c.acct_code, '', 'x', c.acct_code) in (decode(?,'Y','BL','xx'), ";
        sqlCmd += "                                                decode(?,'Y','CA','xx'), ";
        sqlCmd += "                                                decode(?,'Y','IT','xx'), ";
        sqlCmd += "                                                decode(?,'Y','ID','xx'), ";
        sqlCmd += "                                                decode(?,'Y','OT','xx'), ";
        sqlCmd += "                                                decode(?,'Y','AO','xx')) ";
        sqlCmd += "   and decode(c.stmt_cycle,'','x',c.stmt_cycle) = ? ";
        sqlCmd += "   and decode( c.rsk_type, '', 'x',  c.rsk_type) not in ('1','2','3') ";
        sqlCmd += "   and c.acct_month = ? ";
        setString(++paramCnt, hProjDateS);
        setString(++paramCnt, hProjDateE);
        setString(++paramCnt, hConsumeBl);
        setString(++paramCnt, hConsumeCa);
        setString(++paramCnt, hConsumeIt);
        setString(++paramCnt, hConsumeId);
        setString(++paramCnt, hConsumeOt);
        setString(++paramCnt, hConsumeAo);
        setString(++paramCnt, hWdayStmtCycle);
        setString(++paramCnt, hWdayThisAcctMonth);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardAcctType = getValue("acct_type", i);
            hCardGroupCode = getValue("group_code", i);
            hCardCardNo = getValue("card_no", i);
            hCardIssueDate = getValue("issue_date", i);
            hCardIdPSeqno = getValue("id_p_seqno", i);
            hCardPromoteDept = getValue("promote_dept", i);
            hCardPromoteEmpNo = getValue("promote_emp_no", i);
            hCardStaffBranchNo = getValue("staff_branch_no", i);
            hCardMemberId= getValue("member_id", i);
            hCardRegBankNo= getValue("reg_bank_no", i);
            hCardClerkId= getValue("clerk_id", i);
            hCardIntroduceEmpNo= getValue("introduce_emp_no", i);
            hCardIntroduceId= getValue("introduce_id", i);
            hCardProdNo= getValue("prod_no", i);
            hCardElectronicCode= getValue("electronic_code", i);

            hBillReferenceNo= getValue("reference_no", i);
            hBillDestinationAmt= getValueDouble("dest_amt", i);
            hBillEcsPlatformKind = getValue("ecs_platform_kind", i);
            hBillPlatformKindCnt= getValueInt("platform_kind_cnt", i);

            totalCnt++;
            if ((totalCnt % 10000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
                commitDataBase();
            }
            insertMktMemberLog();
        }

    }

    /***********************************************************************/
    private void insertMktMemberLog() throws Exception {
        setValue("proj_code", projCode);
        setValue("card_no", hCardCardNo);
        // 取得【BIL_BILL】ECS_PLATFORM_KIND的值,檢查如果不是空白(or null), 再檢查ECS_PLATFORM_KIND是否存在【bil_PLATFORM】PLATFORM_KIND
        if (hBillEcsPlatformKind != null && hBillPlatformKindCnt > 0) {
            setValue("platform_kind_amt", ""+hBillDestinationAmt);
            setValue("platform_kind_cnt", "1");
            
        } else {
            setValue("platform_kind_amt", "0");
            setValue("platform_kind_cnt", "0");
        }
        setValue("acct_type", hCardAcctType);
        setValue("group_code", hCardGroupCode);
        setValue("card_no", hCardCardNo);
        setValue("issue_date", hCardIssueDate);
        setValue("id_p_seqno", hCardIdPSeqno);
        setValue("promote_dept", hCardPromoteDept);
        setValue("promote_emp_no", hCardPromoteEmpNo);
        setValue("staff_branch_no", hCardStaffBranchNo);
        setValue("member_id", hCardMemberId);
        setValue("reg_bank_no", hCardRegBankNo);
        setValue("clerk_id", hCardClerkId);
        setValue("introduce_emp_no", hCardIntroduceEmpNo);
        setValue("introduce_id", hCardIntroduceId);
        setValue("prod_no", hCardProdNo);
        setValue("electronic_code", hCardElectronicCode);
        
        setValue("ref_no", hBillReferenceNo);
        setValueDouble("dest_amt", hBillDestinationAmt);
//        setValue("platform_kind_amt", hBillPlatformKindAmt);
//        setValue("platform_kind_cnt", hBillPlatformKindCnt);
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "mkt_member_log";
//        debugInsert = "Y";
        int cnt = insertTable();
        if (cnt == 0) {
            showLogMessage("E", "", "Insert table mkt_member_log failed.");
            exitProgram(-1);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktR520 proc = new MktR520();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

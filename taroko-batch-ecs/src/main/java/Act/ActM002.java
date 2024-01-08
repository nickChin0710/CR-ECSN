/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/18  V1.00.01    Brian     error correction                          *
 *  109/11/19  V1.00.02    shiyuqi   updated for project coding standard       * 
 *  112/06/09  V1.00.03    Simon     1.show process_cnt                        *
 *                                   2.tunning                                 *
 *  112-09-27  V1.00.04    Simon     1.修改 tcb 無有效卡不允用RC 條件          *
 *                                   2.取消 ptrm0140 設定無有效卡且帳齡值<= n 時變更RC狀態碼*
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import java.util.*;

/*帳戶允用RC碼處理程式*/
public class ActM002 extends AccessDAO {


    private String progname = "帳戶允用RC碼處理程式  112/09/27  V1.00.04  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;

    String prgmId = "ActM002";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    int recordCnt = 0;
    String ecsServer = "";

    String hBusiBusinessDate = "";
    String hWdayStmtCycle = "";
    String hWdayLastCloseDate = "";
    String hWdayNextCloseDate = "";
    String hWdayLastAcctMonth = "";
    String hAcnoAcctPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoAcctStatus = "";
    String hAcnoRcUseBAdj = "";
    String hAcnoRcUseIndicator = "";
    String hAcnoRcUseBsDate = "";
    String hAcnoRcUseBeDate = "";
    String hAcnoRcUseSDate = "";
    String hAcnoRcUseEDate = "";
    String hAcnoRcUseChangeDate = "";
    String hAcnoRcUseReasonCode = "";
    String hAcnoCardIndicator = "";
    String hAcnoAcctHolderId = "";
    String hAcnoRowid = "";
    String hM021PrintDate = "";
    String hIdnoId = "";
    String hIdnoIdCode = "";
    String hIdnoChiName = "";
    int hValidCardCnt  = 0;
    int hValidCardCnt2 = 0;
    int validCnt = 0;
    String hM022PrintDate = "";
    String hTempBRcUse = "";
    String intInd = "";
    String hAcnoModPgm = "";
    String hAgnnRcUseIndicator = "";
    int validECnt = 0;
    int fhCnt = 0;
    String hAcnoModUser = "";
    int selectAcnoCnt = 0;
    int searchCardCnt = 0;
    int selectIdnoCnt = 0;
    int searchEmployeeCnt = 0;
    int searchFhCnt = 0;
    int updateAcnoCnt1 = 0;
    int updateAcnoCnt2 = 0;
    int updateAcnoCnt3 = 0;
    int updateAcnoCnt4 = 0;
    int updateAcnoCnt5 = 0;
    int updateAcnoCnt6 = 0;
    int insertM002R1Cnt = 0;
    int insertM002R2Cnt = 0;
    int indexCnt = 0;
    int hAgnnDelmths = 0;
    int hActAcnoCode = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);
            
            if (args.length > 1) {
                comcr.errRtn("Usage : ActM002", "", hCallBatchSeqno);
            }

            comr = new CommRoutine(getDBconnect(), getDBalias());

            hModUser = comc.commGetUserID();
            hAcnoModUser = hModUser;
            hAcnoModPgm = javaProgram;

            selectPtrBusinday();

            selectPtrWorkday();

            /* print database modification cnt */
            showLogMessage("I", "", String.format(" ----------------------------------------------- "));
            showLogMessage("I", "", String.format(" select_act_acno count[%d]", selectAcnoCnt));
            showLogMessage("I", "", String.format(" search_crd_card count[%d]", searchCardCnt));
            showLogMessage("I", "", String.format(" search_crd_idno count[%d]", selectIdnoCnt));
            showLogMessage("I", "", String.format(" search_crd_employee count[%d]", searchEmployeeCnt));
            showLogMessage("I", "", String.format(" search_crd_correlate count[%d]", searchFhCnt));
            showLogMessage("I", "", String.format(" update_act_acno from 1-->3   count[%d]", updateAcnoCnt1));
            showLogMessage("I", "", String.format(" update_act_acno from 2,3-->1 count[%d]", updateAcnoCnt2));
            showLogMessage("I", "", String.format(" update_act_acno from 2-->3   count[%d]", updateAcnoCnt3));
            showLogMessage("I", "", String.format(" update_act_acno ptr_actgeneral count[%d]", updateAcnoCnt4));
            showLogMessage("I", "", String.format(" update_act_acno crd_employee count[%d]", updateAcnoCnt5));
            showLogMessage("I", "", String.format(" update_act_acno crd_correlate count[%d]", updateAcnoCnt6));
            showLogMessage("I", "", String.format(" insert_act_m002r1 count[%d]", insertM002R1Cnt));
            showLogMessage("I", "", String.format(" insert_act_m002r2 count[%d]", insertM002R2Cnt));
            showLogMessage("I", "", String.format(" ----------------------------------------------- "));

            showLogMessage("I", "", String.format("程式執行結束"));
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
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

    /***********************************************************************/
    void selectPtrWorkday() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " last_close_date,";
        sqlCmd += " next_close_date,";
        sqlCmd += " last_acct_month ";
        sqlCmd += " from ptr_workday ";
        sqlCmd += "where next_close_date = ? ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hWdayStmtCycle = getValue("stmt_cycle", i);
            hWdayLastCloseDate = getValue("last_close_date", i);
            hWdayNextCloseDate = getValue("next_close_date", i);
            hWdayLastAcctMonth = getValue("last_acct_month", i);

            deleteActM002r2();

            selectActAcno();
        }

    }

    /***********************************************************************/
    void deleteActM002r2() throws Exception {
        daoTable = "act_m002r2";
        whereStr = "where stmt_cycle = ? ";
        setString(1, hWdayStmtCycle);
        deleteTable();
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        long validEmployeeCnt;
        long validFhCnt;

        sqlCmd = "select ";
        sqlCmd += " acno_p_seqno,";
        sqlCmd += " acct_type,";
        sqlCmd += " acct_key,";
        sqlCmd += " id_p_seqno,";
        sqlCmd += " corp_p_seqno,";
        sqlCmd += " acct_status,";
        sqlCmd += " rc_use_b_adj,";
        sqlCmd += " rc_use_indicator,";
        sqlCmd += " rc_use_s_date,";
        sqlCmd += " rc_use_e_date,";
        sqlCmd += " decode(rc_use_s_date,'','00000101',rc_use_s_date) h_acno_rc_use_s_date,";
        sqlCmd += " decode(rc_use_e_date,'','99991231',rc_use_e_date) h_acno_rc_use_e_date,";
        sqlCmd += " rc_use_change_date,";
        sqlCmd += " rc_use_reason_code,";
        sqlCmd += " card_indicator,";
         sqlCmd += " UF_IDNO_ID(id_p_seqno) acct_holder_id,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "from act_acno ";
        sqlCmd += "where stmt_cycle = ? ";
        sqlCmd += " and acno_flag <> 'Y' ";
        setString(1, hWdayStmtCycle);
        openCursor();
        while(fetchTable()) {
            hAcnoAcctPSeqno = getValue("acno_p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoRcUseBAdj = getValue("rc_use_b_adj");
            hAcnoRcUseIndicator = getValue("rc_use_indicator");
            hAcnoRcUseBsDate = getValue("rc_use_s_date");
            hAcnoRcUseBeDate = getValue("rc_use_e_date");
            hAcnoRcUseSDate = getValue("h_acno_rc_use_s_date");
            hAcnoRcUseEDate = getValue("h_acno_rc_use_e_date");
            hAcnoRcUseChangeDate = getValue("rc_use_change_date");
            hAcnoRcUseReasonCode = getValue("rc_use_reason_code");
            hAcnoCardIndicator = getValue("card_indicator");
            hAcnoAcctHolderId = getValue("acct_holder_id");
            hAcnoRowid = getValue("rowid");

            ++indexCnt;
            ++selectAcnoCnt;
            if (selectAcnoCnt % 5000 == 0)
                showLogMessage("I", "", String.format("  selectAcnoCnt:[%d]", selectAcnoCnt));

            hTempBRcUse = hAcnoRcUseBAdj;
            hAcnoRcUseBAdj = hAcnoRcUseIndicator;

/***員工review先停
            validEmployeeCnt = validEmployeeCheck();

            if (validEmployeeCnt > 0) {
                updateAcnoCnt5++;
                if ((hAcnoRcUseIndicator.equals("3"))
                        && ((hBusiBusinessDate.compareTo(hAcnoRcUseSDate) >= 0)
                                && (hBusiBusinessDate.compareTo(hAcnoRcUseEDate) <= 0))) {
                    continue;
                } else {
                    hAcnoRcUseSDate = hBusiBusinessDate;
                    hAcnoRcUseEDate = "";
                    updateActAcno(3);
                    continue;
                }
            }
***/
            /***金控利害關係人review***/
            validFhCnt = validFhCheck();

            if (validFhCnt > 0) {
                updateAcnoCnt6++;
                if (((hAcnoRcUseIndicator.equals("2"))
                        || (hAcnoRcUseIndicator.equals("3")))
                        && ((hBusiBusinessDate.compareTo(hAcnoRcUseSDate) >= 0)
                                && (hBusiBusinessDate.compareTo(hAcnoRcUseEDate) <= 0))) {
                    continue;
                } else {
                    hAcnoRcUseSDate = hBusiBusinessDate;
                    hAcnoRcUseEDate = "";
                    updateActAcno(3);
                    continue;
                }
            }

          //hValidCardCnt = validCardCheck();
            validCardCheck2();

//取消 ptrm0140 設定無有效卡且帳齡值<= n 時變更RC狀態碼
/***
          //hActAcnoCode = comr.getMcode(hAcnoAcctType, hAcnoAcctPSeqno);
          //修改為只有無活卡的帳戶才算帳齡，以提高效能
            if (hValidCardCnt == 0)  {
              hActAcnoCode = comr.getMcode(hAcnoAcctType, hAcnoAcctPSeqno);
            } else {
            	hActAcnoCode = 0;
            }

            selectPtrActgeneral();

            if ((hValidCardCnt == 0) && (hActAcnoCode <= hAgnnDelmths)) {
                updateAcnoCnt4++;
                if ((hAgnnRcUseIndicator.equals(hAcnoRcUseIndicator))
                        && ((hBusiBusinessDate.compareTo(hAcnoRcUseSDate) >= 0)
                                && (hBusiBusinessDate.compareTo(hAcnoRcUseEDate) <= 0))) {
                    continue;
                } else {
                    hAcnoRcUseSDate = hBusiBusinessDate;
                    hAcnoRcUseEDate = hBusiBusinessDate;
                    updateActAcno(comcr.str2int(hAgnnRcUseIndicator));
                    continue;
                }
            }
***/
            if ((hValidCardCnt2 == 0) || (hAcnoAcctStatus.equals("3"))
                    || (hAcnoAcctStatus.equals("4"))) {
                if ((hAcnoRcUseIndicator.equals("1"))
                        || ((!hAcnoRcUseIndicator.equals("1"))
                                && (hTempBRcUse.equals("1"))
                                && ((hBusiBusinessDate.compareTo(hAcnoRcUseSDate) < 0)
                                        || (hBusiBusinessDate.compareTo(hAcnoRcUseEDate) > 0)))) {
                    hAcnoRcUseSDate = hBusiBusinessDate;
                    hAcnoRcUseEDate = "";
                    updateAcnoCnt1++;
                    updateActAcno(3);
                    continue;
                }
            }

            if ((hValidCardCnt2 > 0) && ((hAcnoAcctStatus.equals("1"))
                    || (hAcnoAcctStatus.equals("2")))) {
                if ((hAcnoRcUseIndicator.equals("2"))
                        || (hAcnoRcUseIndicator.equals("3"))) {
                    hAcnoRcUseSDate = "";
                    hAcnoRcUseEDate = "";
                    updateAcnoCnt2++;
                    updateActAcno(1);
                    if (hAcnoCardIndicator.equals("2")) {
                        selectChiNameCorp();
                    } else {
                        selectChiNameRtn();
                    }
                    insertActM002r1();
                    continue;
                }
            }

            if ((hAcnoRcUseEDate.compareTo(hBusiBusinessDate) < 0)
                    && ((hAcnoRcUseIndicator.equals("2"))
                            || (hAcnoRcUseIndicator.equals("3")))) {
                hAcnoRcUseSDate = hBusiBusinessDate;
                hAcnoRcUseEDate = "";
                updateAcnoCnt3++;
                updateActAcno(3);
                continue;
            }

            if ((hValidCardCnt2 == 0) && (hAcnoRcUseIndicator.equals("2"))) {
                if (hAcnoCardIndicator.equals("2")) {
                    selectChiNameCorp();
                } else {
                    selectChiNameRtn();
                }
                insertActM002r2();
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    int validEmployeeCheck() throws Exception {
        sqlCmd = "select count(*) valid_e_cnt ";
        sqlCmd += " from crd_employee  ";
        sqlCmd += "where id = ?  ";
        sqlCmd += " and status_id in ('0','1','7')  ";
        sqlCmd += " and unit_no != '138' ";
        sqlCmd += "fetch first 1 rows only  ";
        setString(1, hAcnoAcctHolderId);
        int recordCnt1 = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_employee not found!", "", hCallBatchSeqno);
        }
        if (recordCnt1 > 0) {
            validECnt = getValueInt("valid_e_cnt");
        }

        searchEmployeeCnt = searchEmployeeCnt + validECnt;

        return (validECnt);
    }

    /***********************************************************************/
    int validFhCheck() throws Exception {
        sqlCmd = "select count(*) fh_cnt ";
        sqlCmd += " from crd_correlate  ";
        sqlCmd += "where correlate_id = ?  ";
        sqlCmd += " and fh_flag = 'Y' ";
        sqlCmd += "fetch first 1 rows only  ";
        setString(1, hAcnoAcctHolderId);
        int recordCnt1 = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_correlate not found!", "", hCallBatchSeqno);
        }
        if (recordCnt1 > 0) {
            fhCnt = getValueInt("fh_cnt");
        }

        searchFhCnt = searchFhCnt + fhCnt;

        return (fhCnt);
    }

    /***********************************************************************/
    int validCardCheck() throws Exception {
        sqlCmd = "select count(*) valid_cnt ";
        sqlCmd += " from crd_card  ";
      //sqlCmd += "where gp_no = ?  "; // acct_p_seqno
        sqlCmd += "where p_seqno = ?  "; // acct_p_seqno
        sqlCmd += " and current_code = '0' ";
        sqlCmd += "fetch first 1 rows only  ";
        setString(1, hAcnoAcctPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_card not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            validCnt = getValueInt("valid_cnt");
        }

        searchCardCnt++;

        return (validCnt);
    }

    /***********************************************************************/
    void validCardCheck2() throws Exception {
        hValidCardCnt  = 0;
        hValidCardCnt2 = 0;
        String tmpCurrentCode="", tmpReissueStatus="";

        sqlCmd  = " select ";
        sqlCmd += " reissue_status,";
        sqlCmd += " current_code ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where p_seqno  = ?  ";
        setString(1, hAcnoAcctPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
          tmpCurrentCode   = getValue("current_code", i);
          tmpReissueStatus = getValue("reissue_status", i);
          if (tmpCurrentCode.equals("0")) {
              hValidCardCnt++;
              hValidCardCnt2++;
          } if (Arrays.asList("1","2").contains(tmpReissueStatus)) {
              hValidCardCnt2++;
          }
        }

    }

    /***********************************************************************/
    void selectPtrActgeneral() throws Exception {
        hAgnnRcUseIndicator = "";
        sqlCmd = "select rc_use_indicator,";
        sqlCmd += " delmths ";
        sqlCmd += " from ptr_actgeneral_n  ";
        sqlCmd += "where acct_type = ? ";
        setString(1, hAcnoAcctType);
        int recordCnt1 = selectTable();
        if (recordCnt1 > 0) {
            hAgnnRcUseIndicator = getValue("rc_use_indicator");
            hAgnnDelmths = getValueInt("delmths");
        }
    }

    /***********************************************************************/
    void insertActM002r1() throws Exception {
        hM021PrintDate = sysDate;

        daoTable = "act_m002r1";
        extendField = daoTable + ".";
        setValue(extendField + "print_date", hM021PrintDate);
        setValue(extendField + "p_seqno", hAcnoAcctPSeqno);
        setValue(extendField + "acct_type", hAcnoAcctType);
      //setValue(extendField + "id_p_seqno", h_acno_id_p_seqno);
        if (hAcnoCardIndicator.equals("2")) {
            setValue(extendField + "id_p_seqno", hAcnoCorpPSeqno);
        } else {
            setValue(extendField + "id_p_seqno", hAcnoIdPSeqno);
        } 
        setValue(extendField + "chi_name", hIdnoChiName);
        setValueInt(extendField + "tot_valid_card", validCnt);
        setValue(extendField + "acct_status", hAcnoAcctStatus);
        setValue(extendField + "rc_use_b_adj", hAcnoRcUseBAdj);
        setValue(extendField + "rc_use_indicator", "1");
        setValue(extendField + "n_rc_use_s_date", hBusiBusinessDate);
        setValue(extendField + "rc_use_s_date", hAcnoRcUseSDate.equals("00000101") ? null : hAcnoRcUseSDate);
        setValue(extendField + "rc_use_e_date", hAcnoRcUseEDate.equals("99991231") ? null : hAcnoRcUseEDate);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_m002r1 duplicate!", "", hCallBatchSeqno);
        }

        insertM002R1Cnt++;
    }

    /***********************************************************************/
    void updateActAcno(int intInd) throws Exception {
        if (intInd != 2) {
            hAcnoRcUseEDate = "";
        }
        /*
         * else { str2var(h_acno_rc_use_e_date,h_acno_rc_use_s_date.arr); }
         */

        daoTable = "act_acno";
        updateSQL = " rc_use_b_adj       = ?,";
        updateSQL += " rc_use_indicator   = ?,";
        updateSQL += " rc_use_bs_date     = ?,";
        updateSQL += " rc_use_be_date     = ?,";
        updateSQL += " rc_use_s_date      = decode(?, 1, '', cast(? as varchar(8))),";
        updateSQL += " rc_use_e_date      = decode(?, 1, '', cast(? as varchar(8))),";
        updateSQL += " rc_use_change_date = ?,";
        updateSQL += " mod_time           = sysdate,";
        updateSQL += " mod_pgm            = ?";
        whereStr = " where rowid        = ? ";
        setString(1, hAcnoRcUseBAdj);
        setString(2, Integer.toString(intInd));
        setString(3, hAcnoRcUseBsDate);
        setString(4, hAcnoRcUseBeDate);
        setInt(5, intInd);
        setString(6, hAcnoRcUseSDate);
        setInt(7, intInd);
        setString(8, hAcnoRcUseEDate);
        setString(9, hBusiBusinessDate);
        setString(10, hAcnoModPgm);
        setRowId(11, hAcnoRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectChiNameCorp() throws Exception {
        hIdnoId = "";
        hIdnoIdCode = "";
        hIdnoChiName = "";

        sqlCmd = "select corp_no,";
        //sqlCmd += " corp_no_code,";
        sqlCmd += " chi_name ";
        sqlCmd += " from crd_corp  ";
        sqlCmd += "where corp_p_seqno = ? ";
        setString(1, hAcnoCorpPSeqno);
        int recordCnt1 = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        }
        if (recordCnt1 > 0) {
            hIdnoId = getValue("corp_no");
            //h_idno_id_code = getValue("corp_no_code");
            hIdnoChiName = getValue("chi_name");
        }

        selectIdnoCnt++;
    }

    /***********************************************************************/
    void selectChiNameRtn() throws Exception {
        hIdnoId = "";
        hIdnoIdCode = "";
        hIdnoChiName = "";

        sqlCmd = "select id_no,";
        sqlCmd += " id_no_code,";
        sqlCmd += " chi_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        int recordCnt1 = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt1 > 0) {
            hIdnoId = getValue("id_no");
            hIdnoIdCode = getValue("id_no_code");
            hIdnoChiName = getValue("chi_name");
        }

        selectIdnoCnt++;
    }

    /***********************************************************************/
    void insertActM002r2() throws Exception {
        hM022PrintDate = sysDate;

        daoTable = "act_m002r2";
        extendField = daoTable + ".";
        setValue(extendField + "print_date", hM022PrintDate);
        setValue(extendField + "p_seqno", hAcnoAcctPSeqno);
        setValue(extendField + "acct_type", hAcnoAcctType);
      //setValue(extendField + "acct_key", h_acno_acct_key);
      //setValue(extendField + "id_p_seqno", h_acno_id_p_seqno);
        if (hAcnoCardIndicator.equals("2")) {
            setValue(extendField + "id_p_seqno", hAcnoCorpPSeqno);
        } else {
            setValue(extendField + "id_p_seqno", hAcnoIdPSeqno);
        } 
      //setValue(extendField + "id_no", h_idno_id);
      //setValue(extendField + "id_no_code", h_idno_id_code);
        setValue(extendField + "chi_name", hIdnoChiName);
        setValueInt(extendField + "tot_valid_card", validCnt);
        setValue(extendField + "acct_status", hAcnoAcctStatus);
        setValue(extendField + "rc_use_b_adj", hTempBRcUse);
        setValue(extendField + "rc_use_indicator", "2");
        setValue(extendField + "n_rc_use_s_date", hBusiBusinessDate);
        setValue(extendField + "rc_use_s_date", hAcnoRcUseSDate.equals("00000101") ? null : hAcnoRcUseSDate);
        setValue(extendField + "rc_use_e_date", hAcnoRcUseEDate.equals("99991231") ? null : hAcnoRcUseEDate);
        setValue(extendField + "stmt_cycle", hWdayStmtCycle);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_m002r2 duplicate!", "", hCallBatchSeqno);
        }

        insertM002R2Cnt++;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActM002 proc = new ActM002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

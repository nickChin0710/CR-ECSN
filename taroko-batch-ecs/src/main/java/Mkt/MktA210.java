/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/01/01  V1.00.00    Edson     program initial                           *
 *  107/01/22  V1.00.01    Brian     error correction                          *
 *  109-12-03  V1.00.02  tanwei      updated for project coding standard       *
 ******************************************************************************/

package Mkt;

import java.util.ArrayList;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*年費回饋聯名機構資料處理*/
public class MktA210 extends AccessDAO {

    private String progname = "年費回饋聯名機構資料處理 109/12/03 V1.00.02";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno    = "";
    String hCallRProgramCode = "";
    String buf                   = "";

    String        wsSDate             = "";
    String        wsEDate             = "";
    int           afeeRows             = 0;
    String        hCfeeCardType      = "";
    String        hCfeeAcctType      = "";
    String        hCfeeAcctKey       = "";
    String        hCfeeSupFlag       = "";
    String        hCfeeExpireDate    = "";
    String        hCfeeIssueDate     = "";
    String        hCfeeGroupCode     = "";
    String        hCfeeDataType      = "";
    double        hCfeeRcvAnnualFee = 0;
    String        hCfeeRegBankNo    = "";
    String        hCfeePromoteEmpNo = "";
    String        hCfeeIntroduceId   = "";
    String        hCfeeMaintainCode  = "";
    String        hCfeeModTime       = "";
    String        hBusiBusinessDate  = "";
    String        wsMonth              = "";
    List<String>  hAngrGroupCode     = new ArrayList<String>();
    List<String>  hAngrGoldCard      = new ArrayList<String>();
    List<String>  hAngrWgoldCard     = new ArrayList<String>();
    List<String>  hAngrNormalCard    = new ArrayList<String>();
    List<String>  hAngrMajorCard     = new ArrayList<String>();
    List<String>  hAngrAdditionCard  = new ArrayList<String>();
    List<Integer> hAngrRewardYear    = new ArrayList<Integer>();
    List<String>  hAngrDescription    = new ArrayList<String>();
    List<String>  hAngrRewardType1  = new ArrayList<String>();
    List<Double>  hAngrPartialGm1   = new ArrayList<Double>();
    List<Double>  hAngrPartialGs1   = new ArrayList<Double>();
    List<Double>  hAngrPartialWm1   = new ArrayList<Double>();
    List<Double>  hAngrPartialWs1   = new ArrayList<Double>();
    List<Double>  hAngrPartialNm1   = new ArrayList<Double>();
    List<Double>  hAngrPartialNs1   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtGm1   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtGs1   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtWm1   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtWs1   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtNm1   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtNs1   = new ArrayList<Double>();
    List<Double>  hAngrRateGm1      = new ArrayList<Double>();
    List<Double>  hAngrRateGs1      = new ArrayList<Double>();
    List<Double>  hAngrRateWm1      = new ArrayList<Double>();
    List<Double>  hAngrRateWs1      = new ArrayList<Double>();
    List<Double>  hAngrRateNm1      = new ArrayList<Double>();
    List<Double>  hAngrRateNs1      = new ArrayList<Double>();
    List<String>  hAngrRewardType2  = new ArrayList<String>();
    List<Double>  hAngrPartialGm2   = new ArrayList<Double>();
    List<Double>  hAngrPartialGs2   = new ArrayList<Double>();
    List<Double>  hAngrPartialWm2   = new ArrayList<Double>();
    List<Double>  hAngrPartialWs2   = new ArrayList<Double>();
    List<Double>  hAngrPartialNm2   = new ArrayList<Double>();
    List<Double>  hAngrPartialNs2   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtGm2   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtGs2   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtWm2   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtWs2   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtNm2   = new ArrayList<Double>();
    List<Double>  hAngrFixAmtNs2   = new ArrayList<Double>();
    List<Double>  hAngrRateGm2      = new ArrayList<Double>();
    List<Double>  hAngrRateGs2      = new ArrayList<Double>();
    List<Double>  hAngrRateWm2      = new ArrayList<Double>();
    List<Double>  hAngrRateWs2      = new ArrayList<Double>();
    List<Double>  hAngrRateNm2      = new ArrayList<Double>();
    List<Double>  hAngrRateNs2      = new ArrayList<Double>();
    String        hRegpGroupCode     = "";
    String        hRegpRewYyyymm     = "";
    String        hRegpRewardType    = "";
    double        hRegpRewAmount     = 0;
    int           hRegpMajorCnt      = 0;
    int           hRegpSubCnt        = 0;
    double        hRegpReceiveAmt    = 0;
    int           hRegpMajorCntG1   = 0;
    int           hRegpSubCntG1     = 0;
    int           hRegpMajorCntW1   = 0;
    int           hRegpSubCntW1     = 0;
    int           hRegpMajorCntN1   = 0;
    int           hRegpSubCntN1     = 0;
    int           hRegpMajorCntG2   = 0;
    int           hRegpSubCntG2     = 0;
    int           hRegpMajorCntW2   = 0;
    int           hRegpSubCntW2     = 0;
    int           hRegpMajorCntN2   = 0;
    int           hRegpSubCntN2     = 0;
    String        hPctpCardType      = "";
    String        hPctpCardNote      = "";
    List<String>  aPctpCardType      = new ArrayList<String>();
    List<String>  aPctpCardNote      = new ArrayList<String>();

    String wsSkip         = "";
    String wsCardType    = "";
    String match           = "";
    String wsYy           = "";
    String wsMm           = "";
    String wsGroup        = "";
    double wsRewardAmt   = 0;
    double wsTmpAmt      = 0;
    int    wsTmpMons     = 0;
    int    wsCurrMons    = 0;
    int    wsIssuMons    = 0;
    int    wsAngrMons    = 0;
    int    i               = 0;
    int    k               = 0;
    int    angrThisTime  = 0;
    int    tmpYy          = 0;
    int    tmpMm          = 0;
    int    rowsToCard    = 0;
    int    afeeFetchCnt  = 0;
    int    afeeDataCnt   = 0;
    int    regpInsertCnt = 0;

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

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            /** initial_process **/
            match = "N";
            selectPtrBusinday();

            deleteMktReGroup();

            loadPtrCardType();
            loadMktAnulGp();

            fetchCycAfee();

            insertMktReGroup();

            trailProcess();

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
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

        wsSDate = hBusiBusinessDate.substring(0, 6);

        wsYy = wsSDate.substring(0, 4);
        tmpYy = comcr.str2int(wsYy);
        wsMm = wsSDate.substring(4);
        tmpMm = comcr.str2int(wsMm);
        tmpMm = tmpMm - 1;
        if (tmpMm == 0) {
            tmpYy = tmpYy - 1;
            tmpMm = 12;
        }

        wsYy = String.format("%04d", tmpYy);
        wsSDate = wsYy;
        wsMm = String.format("%02d", tmpMm);
        wsSDate = wsSDate + wsMm;
        wsSDate = wsSDate + "01";

        wsEDate = wsSDate.substring(0, 6);
        wsEDate = wsEDate + "31";

        wsCurrMons = tmpYy * 12 + tmpMm;
        wsMonth = wsSDate.substring(0, 6);
        showLogMessage("I", "", String.format("START DATE %s END DATE %s ", wsSDate, wsEDate));
    }

    /***********************************************************************/
    void deleteMktReGroup() throws Exception {
        daoTable = "mkt_re_group";
        whereStr = "where rew_yyyymm = ?  ";
        whereStr += "and reward_type = '1' ";
        setString(1, wsMonth);
        deleteTable();

    }

    /***********************************************************************/
    void loadPtrCardType() throws Exception {
        sqlCmd = "select card_type,";
        sqlCmd += " card_note ";
        sqlCmd += " from ptr_card_type  ";
        sqlCmd += " ORDER BY card_type desc ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aPctpCardType.add(getValue("card_type", i));
            aPctpCardNote.add(getValue("card_note", i));
        }
        rowsToCard = recordCnt;
    }

    /***********************************************************************/
    void loadMktAnulGp() throws Exception {
        sqlCmd = "select ";
        sqlCmd += " group_code,";
        sqlCmd += " gold_card,";
        sqlCmd += " wgold_card,";
        sqlCmd += " normal_card,";
        sqlCmd += " major_card,";
        sqlCmd += " addition_card,";
        sqlCmd += " reward_year,";
        sqlCmd += " description,";
        sqlCmd += " reward_type_1,";
        sqlCmd += " partial_gm_1,";
        sqlCmd += " partial_gs_1,";
        sqlCmd += " partial_wm_1,";
        sqlCmd += " partial_ws_1,";
        sqlCmd += " partial_nm_1,";
        sqlCmd += " partial_ns_1,";
        sqlCmd += " fix_amt_gm_1,";
        sqlCmd += " fix_amt_gs_1,";
        sqlCmd += " fix_amt_wm_1,";
        sqlCmd += " fix_amt_ws_1,";
        sqlCmd += " fix_amt_nm_1,";
        sqlCmd += " fix_amt_ns_1,";
        sqlCmd += " rate_gm_1,";
        sqlCmd += " rate_gs_1,";
        sqlCmd += " rate_wm_1,";
        sqlCmd += " rate_ws_1,";
        sqlCmd += " rate_nm_1,";
        sqlCmd += " rate_ns_1,";
        sqlCmd += " reward_type_2,";
        sqlCmd += " partial_gm_2,";
        sqlCmd += " partial_gs_2,";
        sqlCmd += " partial_wm_2,";
        sqlCmd += " partial_ws_2,";
        sqlCmd += " partial_nm_2,";
        sqlCmd += " partial_ns_2,";
        sqlCmd += " fix_amt_gm_2,";
        sqlCmd += " fix_amt_gs_2,";
        sqlCmd += " fix_amt_wm_2,";
        sqlCmd += " fix_amt_ws_2,";
        sqlCmd += " fix_amt_nm_2,";
        sqlCmd += " fix_amt_ns_2,";
        sqlCmd += " rate_gm_2,";
        sqlCmd += " rate_gs_2,";
        sqlCmd += " rate_wm_2,";
        sqlCmd += " rate_ws_2,";
        sqlCmd += " rate_nm_2,";
        sqlCmd += " rate_ns_2 ";
        sqlCmd += "  from mkt_anul_gp ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAngrGroupCode.add(getValue("group_code", i));
            hAngrGoldCard.add(getValue("gold_card", i));
            hAngrWgoldCard.add(getValue("wgold_card", i));
            hAngrNormalCard.add(getValue("normal_card", i));
            hAngrMajorCard.add(getValue("major_card", i));
            hAngrAdditionCard.add(getValue("addition_card", i));
            hAngrRewardYear.add(getValueInt("reward_year", i));
            hAngrDescription.add(getValue("description", i));
            hAngrRewardType1.add(getValue("reward_type_1", i));
            hAngrPartialGm1.add(getValueDouble("partial_gm_1", i));
            hAngrPartialGs1.add(getValueDouble("partial_gs_1", i));
            hAngrPartialWm1.add(getValueDouble("partial_wm_1", i));
            hAngrPartialWs1.add(getValueDouble("partial_ws_1", i));
            hAngrPartialNm1.add(getValueDouble("partial_nm_1", i));
            hAngrPartialNs1.add(getValueDouble("partial_ns_1", i));
            hAngrFixAmtGm1.add(getValueDouble("fix_amt_gm_1", i));
            hAngrFixAmtGs1.add(getValueDouble("fix_amt_gs_1", i));
            hAngrFixAmtWm1.add(getValueDouble("fix_amt_wm_1", i));
            hAngrFixAmtWs1.add(getValueDouble("fix_amt_ws_1", i));
            hAngrFixAmtNm1.add(getValueDouble("fix_amt_nm_1", i));
            hAngrFixAmtNs1.add(getValueDouble("fix_amt_ns_1", i));
            hAngrRateGm1.add(getValueDouble("rate_gm_1", i));
            hAngrRateGs1.add(getValueDouble("rate_gs_1", i));
            hAngrRateWm1.add(getValueDouble("rate_wm_1", i));
            hAngrRateWs1.add(getValueDouble("rate_ws_1", i));
            hAngrRateNm1.add(getValueDouble("rate_nm_1", i));
            hAngrRateNs1.add(getValueDouble("rate_ns_1", i));
            hAngrRewardType2.add(getValue("reward_type_2", i));
            hAngrPartialGm2.add(getValueDouble("partial_gm_2", i));
            hAngrPartialGs2.add(getValueDouble("partial_gs_2", i));
            hAngrPartialWm2.add(getValueDouble("partial_wm_2", i));
            hAngrPartialWs2.add(getValueDouble("partial_ws_2", i));
            hAngrPartialNm2.add(getValueDouble("partial_nm_2", i));
            hAngrPartialNs2.add(getValueDouble("partial_ns_2", i));
            hAngrFixAmtGm2.add(getValueDouble("fix_amt_gm_2", i));
            hAngrFixAmtGs2.add(getValueDouble("fix_amt_gs_2", i));
            hAngrFixAmtWm2.add(getValueDouble("fix_amt_wm_2", i));
            hAngrFixAmtWs2.add(getValueDouble("fix_amt_ws_2", i));
            hAngrFixAmtNm2.add(getValueDouble("fix_amt_nm_2", i));
            hAngrFixAmtNs2.add(getValueDouble("fix_amt_ns_2", i));
            hAngrRateGm2.add(getValueDouble("rate_gm_2", i));
            hAngrRateGs2.add(getValueDouble("rate_gs_2", i));
            hAngrRateWm2.add(getValueDouble("rate_wm_2", i));
            hAngrRateWs2.add(getValueDouble("rate_ws_2", i));
            hAngrRateNm2.add(getValueDouble("rate_nm_2", i));
            hAngrRateNs2.add(getValueDouble("rate_ns_2", i));
        }

        angrThisTime = recordCnt;
    }

    /***********************************************************************/
    void fetchCycAfee() throws Exception {
        /** reset_afee_value **/
        hCfeeCardType = "";
        hCfeeAcctType = "";
        hCfeeAcctKey = "";
        hCfeeSupFlag = "";
        hCfeeExpireDate = "";
        hCfeeIssueDate = "";
        hCfeeGroupCode = "";
        hCfeeDataType = "";
        hCfeeRcvAnnualFee = 0;
        hCfeeRegBankNo = "";
        hCfeePromoteEmpNo = "";
        hCfeeIntroduceId = "";
        hCfeeMaintainCode = "";
        hCfeeModTime = "";

        sqlCmd = "select ";
        sqlCmd += " card_type,";
        sqlCmd += " acct_type,";
        sqlCmd += " UF_ACNO_KEY(p_seqno) acct_key,";
        sqlCmd += " sup_flag,";
        sqlCmd += " expire_date,";
        sqlCmd += " issue_date,";
        sqlCmd += " decode(group_code,'','XXXX',group_code) h_cfee_group_code,";
        sqlCmd += " data_type,";
        sqlCmd += " rcv_annual_fee,";
        sqlCmd += " reg_bank_no,";
        sqlCmd += " promote_emp_no,";
        sqlCmd += "introduce_id,";
        sqlCmd += " decode(maintain_code,'','X',maintain_code) h_cfee_maintain_code,";
        sqlCmd += " to_char(mod_time,'yyyymmdd') h_cfee_mod_time ";
        sqlCmd += "from cyc_afee ";
        sqlCmd += "where to_char(mod_time,'yyyymmdd') >= ? and ";
        sqlCmd += " to_char(mod_time,'yyyymmdd') <= ? ";
        sqlCmd += " order by group_code ";
        setString(1, wsSDate);
        setString(2, wsEDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hCfeeCardType = getValue("card_type");
            hCfeeAcctType = getValue("acct_type");
            hCfeeAcctKey = getValue("acct_key");
            hCfeeSupFlag = getValue("sup_flag");
            hCfeeExpireDate = getValue("expire_date");
            hCfeeIssueDate = getValue("issue_date");
            hCfeeGroupCode = getValue("h_cfee_group_code");
            hCfeeDataType = getValue("data_type");
            hCfeeRcvAnnualFee = getValueDouble("rcv_annual_fee");
            hCfeeRegBankNo = getValue("reg_bank_no");
            hCfeePromoteEmpNo = getValue("promote_emp_no");
            hCfeeIntroduceId = getValue("introduce_id");
            hCfeeMaintainCode = getValue("h_cfee_maintain_code");
            hCfeeModTime = getValue("h_cfee_mod_time");

            afeeFetchCnt++;
            afeeDataCnt++;

            if (hCfeeGroupCode.equals("XXXX")) {
                continue;
            }

            if (!hCfeeMaintainCode.equals("Y")) {
                continue;
            }

            if (hCfeeRcvAnnualFee <= 0) {
                continue;
            }

            if (!wsGroup.equals(hCfeeGroupCode)) {
                insertMktReGroup();
            }

            wsGroup = hCfeeGroupCode;

            searchMktAnulGp();
            if (wsSkip.equals("Y")) {
                continue;
            }

            wsRewardAmt = 0;

            if (hCfeeDataType.equals("1")) {
                firstYearAmt();
            } else {
                secondYearAmt();
            }

            if (wsRewardAmt == 0) {
                continue;
            }

            hRegpRewAmount += wsRewardAmt;
            hRegpReceiveAmt += hCfeeRcvAnnualFee;

            if (hCfeeSupFlag.equals("0")) {
                hRegpMajorCnt++;
            } else {
                hRegpSubCnt++;
            }

        }
        closeCursor(cursorIndex);

        return;
    }

    /***********************************************************************/
    void insertMktReGroup() throws Exception {
        if (match.equals("N")) {
            return;
        }

        hRegpGroupCode = wsGroup;
        hRegpRewYyyymm = wsMonth;
        hRegpRewardType = "1";

        setValue("group_code", hRegpGroupCode);
        setValue("rew_yyyymm", hRegpRewYyyymm);
        setValue("reward_type", hRegpRewardType);
        setValueDouble("rew_amount", hRegpRewAmount);
        setValueInt("major_cnt", hRegpMajorCnt);
        setValueInt("sub_cnt", hRegpSubCnt);
        setValueDouble("receive_amt", hRegpReceiveAmt);
        setValueInt("major_cnt_g1", hRegpMajorCntG1);
        setValueInt("sub_cnt_g1", hRegpSubCntG1);
        setValueInt("major_cnt_w1", hRegpMajorCntW1);
        setValueInt("sub_cnt_w1", hRegpSubCntW1);
        setValueInt("major_cnt_n1", hRegpMajorCntN1);
        setValueInt("sub_cnt_n1", hRegpSubCntN1);
        setValueInt("major_cnt_g2", hRegpMajorCntG2);
        setValueInt("sub_cnt_g2", hRegpSubCntG2);
        setValueInt("major_cnt_w2", hRegpMajorCntW2);
        setValueInt("sub_cnt_w2", hRegpSubCntW2);
        setValueInt("major_cnt_n2", hRegpMajorCntN2);
        setValueInt("sub_cnt_n2", hRegpSubCntN2);
        daoTable = "mkt_re_group";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_mkt_re_group duplicate!", "", hCallBatchSeqno);
        }

        hRegpRewAmount = 0;
        hRegpReceiveAmt = 0;
        hRegpMajorCnt = 0;
        hRegpSubCnt = 0;
        hRegpMajorCntG1 = 0;
        hRegpSubCntG1 = 0;
        hRegpMajorCntW1 = 0;
        hRegpSubCntW1 = 0;
        hRegpMajorCntN1 = 0;
        hRegpSubCntN1 = 0;
        hRegpMajorCntG2 = 0;
        hRegpSubCntG2 = 0;
        hRegpMajorCntW2 = 0;
        hRegpSubCntW2 = 0;
        hRegpMajorCntN2 = 0;
        hRegpSubCntN2 = 0;

        regpInsertCnt++;
        return;
    }

    /***********************************************************************/
    void searchMktAnulGp() throws Exception {
        wsSkip = "Y";
        match = "N";

        wsGroup = hCfeeGroupCode;

        int j = hAngrGroupCode.indexOf(wsGroup);
        if (j >= 0) {
            k = j;
            wsSkip = "N";
            match = "Y";
        }

        if (wsSkip == "Y") {
            return;
        }

        wsCardType = comc.getSubString(hCfeeCardType, 1, 1 + 1);
        if (getCardData() != 0) {
            comcr.errRtn(String.format("此卡種[%s]不存在", hCfeeCardType), "", hCallBatchSeqno);
        }

        if (wsCardType.equals("B")) {
            wsCardType = "G";
        }

        if (hPctpCardNote.equals("G") && hAngrGoldCard.get(j) != "Y") {
            wsSkip = "Y";
        } else if (hPctpCardNote.equals("P") && hAngrWgoldCard.get(j) != "Y") {
            wsSkip = "Y";
        } else if (hPctpCardNote.equals("C") && hAngrNormalCard.get(j) != "Y") {
            wsSkip = "Y";
        }

        if (hCfeeSupFlag.equals("0") && !hAngrMajorCard.get(j).equals("Y")) {
            wsSkip = "Y";
        } else if (hCfeeSupFlag.equals("1") && !hAngrAdditionCard.get(j).equals("Y")) {
            wsSkip = "Y";
        }

        return;
    }

    /***********************************************************************/
    int getCardData() throws Exception {
        int i = 0;
        hPctpCardNote = "";
        i = aPctpCardType.indexOf(hCfeeCardType);
        if (i < 0) {
            return 1;
        }
        hPctpCardNote = aPctpCardNote.get(i);
        return (0);
    }

    /***********************************************************************/
    void firstYearAmt() throws Exception {
        if (hAngrRewardType1.get(k).equals("1")) {
            wsRewardAmt = hCfeeRcvAnnualFee;
        } else if (hAngrRewardType1.get(k).equals("2")) {
            partialAmt1();
        } else if (hAngrRewardType1.get(k).equals("3")) {
            fixAmt1();
        } else if (hAngrRewardType1.get(k).equals("4")) {
            rateAmt1();
        }

        if (hPctpCardNote.equals("G") && hCfeeSupFlag == "0") {
            hRegpMajorCntG1++;
        } else if (hPctpCardNote.equals("G") && hCfeeSupFlag != "0") {
            hRegpSubCntG1++;
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "0") {
            hRegpMajorCntW1++;
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag != "0") {
            hRegpSubCntW1++;
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "0") {
            hRegpMajorCntN1++;
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag != "0") {
            hRegpSubCntN1++;
        }

        return;
    }

    /***********************************************************************/
    void partialAmt1() throws Exception {
        wsTmpAmt = hCfeeRcvAnnualFee;

        if (hPctpCardNote.equals("G") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt - hAngrPartialGm1.get(k);
        } else if (hPctpCardNote.equals("G") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt - hAngrPartialGs1.get(k);
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt - hAngrPartialWm1.get(k);
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt - hAngrPartialWs1.get(k);
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt - hAngrPartialNm1.get(k);
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt - hAngrPartialNs1.get(k);
        }

        if (wsRewardAmt < 0) {
            wsRewardAmt = 0;
        }

        return;
    }

    /***********************************************************************/
    void fixAmt1() throws Exception {
        if (hPctpCardNote.equals("G") && hCfeeSupFlag == "0") {
            wsRewardAmt = hAngrFixAmtGm1.get(k);
        } else if (hPctpCardNote.equals("G") && hCfeeSupFlag == "1") {
            wsRewardAmt = hAngrFixAmtGs1.get(k);
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "0") {
            wsRewardAmt = hAngrFixAmtWm1.get(k);
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "1") {
            wsRewardAmt = hAngrFixAmtWs1.get(k);
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "0") {
            wsRewardAmt = hAngrFixAmtNm1.get(k);
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "1") {
            wsRewardAmt = hAngrFixAmtNs1.get(k);
        }

        return;
    }

    /***********************************************************************/
    void rateAmt1() throws Exception {
        wsTmpAmt = hCfeeRcvAnnualFee;

        if (hPctpCardNote.equals("G") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt * hAngrRateGm1.get(k) / 100;
        } else if (hPctpCardNote.equals("G") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt * hAngrRateGs1.get(k) / 100;
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt * hAngrRateWm1.get(k) / 100;
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt * hAngrRateWs1.get(k) / 100;
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "0") {
             wsRewardAmt= wsTmpAmt * hAngrRateNm1.get(k) / 100;
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt * hAngrRateNs1.get(k) / 100;
        }

        return;
    }

    /***********************************************************************/
    void secondYearAmt() throws Exception {
        checkYears();

        if (wsTmpMons > wsAngrMons) {
            return;
        }

        if (hAngrRewardType2.get(k).equals("1")) {
            wsRewardAmt = hCfeeRcvAnnualFee;
        } else if (hAngrRewardType2.get(k).equals("2")) {
            partialAmt2();
        } else if (hAngrRewardType2.get(k).equals("3")) {
            fixAmt2();
        } else if (hAngrRewardType2.get(k).equals("4")) {
            rateAmt2();
        }

        if (hPctpCardNote.equals("G") && hCfeeSupFlag == "0") {
            hRegpMajorCntG2++;
        } else if (hPctpCardNote.equals("G") && hCfeeSupFlag != "0") {
            hRegpSubCntG2++;
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "0") {
            hRegpMajorCntW2++;
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag != "0") {
            hRegpSubCntW2++;
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "0") {
            hRegpMajorCntN2++;
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag != "0") {
            hRegpSubCntN2++;
        }

        return;
    }

    /***********************************************************************/
    void checkYears() throws Exception {
        wsYy = hCfeeIssueDate.substring(0, 4);
        tmpYy = comcr.str2int(wsYy);
        wsMm = hCfeeIssueDate.substring(4, 6);
        tmpMm = comcr.str2int(wsMm);
        wsIssuMons = tmpYy * 12 + tmpMm;
        wsTmpMons = wsCurrMons - wsIssuMons + 2;
        wsAngrMons = hAngrRewardYear.get(k) * 12;

        return;
    }

    /***********************************************************************/
    void partialAmt2() throws Exception {
        wsTmpAmt = hCfeeRcvAnnualFee;

        if (hPctpCardNote.equals("G") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt - hAngrPartialGm2.get(k);
        } else if (hPctpCardNote.equals("G") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt - hAngrPartialGs2.get(k);
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt - hAngrPartialWm2.get(k);
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt - hAngrPartialWs2.get(k);
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt - hAngrPartialNm2.get(k);
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt - hAngrPartialNs2.get(k);
        }

        if (wsRewardAmt < 0) {
            wsRewardAmt = 0;
        }

        return;
    }

    /***********************************************************************/
    void fixAmt2() throws Exception {
        if (hPctpCardNote.equals("G") && hCfeeSupFlag == "0") {
            wsRewardAmt = hAngrFixAmtGm2.get(k);
        } else if (hPctpCardNote.equals("G") && hCfeeSupFlag == "1") {
            wsRewardAmt = hAngrFixAmtGs2.get(k);
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "0") {
            wsRewardAmt = hAngrFixAmtWm2.get(k);
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "1") {
            wsRewardAmt = hAngrFixAmtWs2.get(k);
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "0") {
            wsRewardAmt = hAngrFixAmtNm2.get(k);
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "1") {
            wsRewardAmt = hAngrFixAmtNs2.get(k);
        }

        return;
    }

    /***********************************************************************/
    void rateAmt2() throws Exception {
        wsTmpAmt = hCfeeRcvAnnualFee;
        if (hPctpCardNote.equals("G") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt * hAngrRateGm2.get(k) / 100;
        } else if (hPctpCardNote.equals("G") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt * hAngrRateGs2.get(k) / 100;
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt * hAngrRateWm2.get(k) / 100;
        } else if (hPctpCardNote.equals("P") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt * hAngrRateWs2.get(k) / 100;
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "0") {
            wsRewardAmt = wsTmpAmt * hAngrRateNm2.get(k) / 100;
        } else if (hPctpCardNote.equals("C") && hCfeeSupFlag == "1") {
            wsRewardAmt = wsTmpAmt * hAngrRateNs2.get(k) / 100;
        }

        return;
    }

    /***********************************************************************/
    void trailProcess() throws Exception {
        showLogMessage("I", "", String.format("CYC_AFEE      FETCH   CNT %6d", afeeFetchCnt));
        showLogMessage("I", "", String.format("CYC_AFEE      DATA    CNT %6d", afeeDataCnt));
        showLogMessage("I", "", String.format("MKT_RE_GROUP  INSERT  CNT %6d", regpInsertCnt));
        showLogMessage("I", "", String.format("================================"));
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktA210 proc = new MktA210();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

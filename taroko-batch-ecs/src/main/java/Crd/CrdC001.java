/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------  *
*  107/10/01  V1.00.01    Edson     program initial                         *
*  109/01/03  V1.00.01    Rou       Update condition_check()                *
*  109/08/03  V1.00.02    Wilson    修正 不續卡不update current_code            *
*  109/12/17  V1.00.03    shiyuqi       updated for project coding standard   *
*  112/02/18  V1.00.04    Wilson    調整不續卡條件                                                                                    *
*  112/03/25  V1.00.05    Wilson    insert crd_emboss_tmp add confirm_date  *
*  112/04/21  V1.00.06    Wilson    增加prepare8669Rtn                       *
*  112/04/27  V1.00.07    Wilson    增加判斷凍結不續卡                                                                             *
*  112/04/28  V1.00.08    Wilson    mark基本資料不全不續卡                                                                  *
*  112/05/09  V1.00.09    Ryan      增加判斷sysDate要是當月的第一個營業日才往下執行，否則就直接結束 *
*  112/08/26  V1.00.10    Wilson    修正弱掃問題                                                                                        *
*  112/11/11  V1.00.11    Wilson    調整為判斷當月第二個營業日才執行                                                   *
*  112/11/25  V1.00.12    Wilson    調整為判斷參數為第幾個營業日                                                          *
*  112/12/03  V1.00.13    Wilson    crd_item_unit不判斷卡種                                                        *
****************************************************************************/

package Crd;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

/*產生整批續卡/不續卡資料*/
public class CrdC001 extends AccessDAO {
    private String progname = "產生整批續卡/不續卡資料  112/12/03  V1.00.13";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;

    int debug = 1;

    String prgmId = "CrdC001";
    String rptIdR1 = "CRD_C001_T1";
    String rptidR2 = "CRD_C001_T2";
    String rptIdR3 = "CRD_C001_T3";
    String rptIdR4 = "CRD_C001_T4";
    String rptIdR5 = "CRD_C001_T5";
    String rptIdR6 = "CRD_C001_T6";
    String rptName1 = "CRD_C001_T1";
    String rptName2 = "CRD_C001_T2";
    String rptName3 = "CRD_C001_T3";
    String rptName4 = "CRD_C001_T4";
    String rptName5 = "CRD_C001_T5";
    String rptName6 = "CRD_C001_T6";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar3 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar4 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar5 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar6 = new ArrayList<Map<String, Object>>();
    int rptSeq1 = 0;
    int rptSeq2 = 0;
    int rptSeq3 = 0;
    int rptSeq4 = 0;
    int rptSeq5 = 0;
    int rptSeq6 = 0;
    int errCnt = 0;
    String errMsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;
    String hCardBinNo = "";
    String hCardCardNo = "";
    String hCardCorpNo = "";
    String hCardCorpNoCode = "";
    String hCardGroupCode = "";
    String hCardSourceCode = "";
    String hCardSupFlag = "";
    String hCardMajorRelation = "";
    String hCardMajorIdPSeqno = "";
    String hCardMajorCardNo = "";
    String hCardEngName = "";
    String hCardRegBankNo = "";
    String hCardForceFlag = "";
    String hCardUnitCode = "";
    String hCardElectronicCode = "";
    String hCardEmbossData = "";
    String hCardChangeStatus = "";
    String hCardChangeDate = "";
    String hCardReissueStatus = "";
    String hCardReissueDate = "";
    String hCardPSeqno = "";
    String hCardGpNo = "";
    String hCardAcctType = "";
    String hCardCurrentCode = "";
    String hCardCardType = "";
    String hCardNewBegDate = "";
    String hCardNewEndDate = "";
    String hCardIdPSeqno = "";
    String hCardCorpPSeqno = "";
    String hCardIcFlag = "";
    String hCardStmtCycle = "";
    String hCardComboIndicator = "";
    String hCardCurrCode = "";
    double hCardJcicScore = 0;
    String hParamDateLike = "";
    String hParamGroupCode = "";
    String hTempUser = "";

    String hChiDate = "";
    String hTmpDate = "";
    String hLimitConsumeDate = "";
    int hParamCnt = 0;
    String hIdnoMarriage = "";
    String hIdnoNation = "";
    String hIdnoChiName = "";
    String hIdnoSex = "";
    String hIdnoBirthday = "";
    String hIdnoStudent = "";
    int hActLineOfCreditAmt = 0;
    String hChgAddrDate = "";
    String hActNo = "";
    String hRiskBankNo = "";
    String hAcnoAcctKey = "";
    String hAcnoBlockReason2 = "";
    String hPaymentRate = "";
    String hAcnoChgAddrDate = "";
    String hAcnoBillSendingZip = "";
    String hBillAddress = "";
    String hAcnoBillSendingAddr1 = "";
    String hAcnoBillSendingAddr2 = "";
    String hAcnoBillSendingAddr3 = "";
    String hAcnoBillSendingAddr4 = "";
    String hAcnoBillSendingAddr5 = "";
    String hAcnoStatSendInternet = "";
    double hTttAcctJrnlBal = 0;
    int hAddMonths = 0;
    String hValidTo = "";
    // String h_card_id = "";
    String hMCardCardNo = "";
    String hMCardCorpNo = "";
    String hMCardCorpNoCode = "";
    String hMCardGroupCode = "";
    String hMCardSourceCode = "";
    String hMCardSupFlag = "";
    String hMCardMajorRelation = "";
    String hMCardMajorIdPSeqno = "";
    String hMCardMajorCardNo = "";
    String hMCardEngName = "";
    String hMCardRegBankNo = "";
    String hMCardForceFlag = "";
    String hMCardUnitCode = "";
    String hMCardElectronicCode = "";
    String hMCardEmbossData = "";
    String hMCardAcctType = "";
//  String h_m_card_acct_key = "";
    String hMCardPSeqno = "";
    String hMCardGpNo = "";
    String hMCardNewBegDate = "";
    String hMCardNewEndDate = "";
    String hMCardCurrentCode = "";
    String hMCardFeeCode = "";
    String hMCardCardType = "";
    String hMCardBinNo = "";
    String hMCardIdPSeqno = "";
    String hMCardCorpPSeqno = "";
    String hMCardCorpActFlag = "";
    String hMCardChangeReason = "";
    String hMCardChangeStatus = "";
    String hMCardChangeDate = "";
    // String h_card_block_reason = ""; "";
    // String h_card_block_reason2 = "";
    String hMCardReissueStatus = "";
    String hMCardReissueDate = "";
    String hMCardIcFlag = "";
    String hMCardStmtCycle = "";
    String hMCardComboIndicator = "";
    String hMCardCurrCode = "";
    double hMCardJcicScore = 0;
    String hMCardLastConsumeDate = "";
    String hMCardRowid = "";
    String hMCardCardNote = "";
    String hForceFlag = "";
    String hContiBatchno = "";
    String hAcnoBlockReason = "";
    int hContiRecno = 0;
    String hSource = "";
    String hValidFm = "";
    String hChgAddrFlag = "";
    String hVip = "";
    double hSumPurchaseAcct = 0;
    long hPtrExtnYear = 0;
    String hBegDate = "";
    String hSystemDate = "";
    int hSystemDd = 0;
    int hTttExtnYear = 0;
    String hEmapGroupCode = "";
    String hEmapCardType = "";
    String hEmapUnitCode = "";
    String hEmapBinNo = "";
    String hChiName = "";
    int tempInt = 0;
    String tempBlock = "";
    String hCptdBelongFlag = "";
    int hCptdBelongNum = 0;
    String hCptdChkMonthFlag = "";
    int hCptdChkMonth = 0;
    String hCptdChkAcnoFlag = "";
    int hValue = 0;
    String hTempDate = "";
    String hCardIndicator = "";
    double hSumPurchase = 0;
    int hMonthValue = 0;
    String hExpireReason = "";
    String hExpireChgFlag = "";
    String hExpireChgDate = "";
    String hExpireAddr = "";
    String hChangeDate = "";
    String hChangeReason = "";
    String hChangeStatus = "";
    String pCardNo = "";
    String hNomoveReason = "";
    String swMove = "";
    double hOtherFee = 0;
    String hItemName = "";
    int hTmpNoE = 0;
    long hTmpRecnoE = 0;
    int hTmpNo = 0;
    long hTmpRecno = 0;
    String hTttBegDate = "";
    String hTttEndDate = "";
    long hEmapCreditLmt = 0;
    String hEmapStmtCycle = "";
    String hEmapStatSendInternet = "";
    String hEmapMailZip = "";
    String hEmapMailAddr1 = "";
    String hEmapMailAddr2 = "";
    String hEmapMailAddr3 = "";
    String hEmapMailAddr4 = "";
    String hEmapMailAddr5 = "";
    int hTttAddMonths = 0;
    String hContiBatchnoE = "";
    int emapCnt = 0;
    long apsSeq = 0;
    String hEmapAcctType = "";
    String hEmapSourceCode = "";
    String tempX11 = "";
    String hEmapNcccType = "";
    String currSupFlag = "";
    double hTttLineOfCreditAmt = 0;
    String hEriaRefIp = "";
    String hEriaRefName = "";
    String hEriaUserId = "";
    String hEriaTransType = "";
    String hEriaRemoteDir = "";
    String hEriaLocalDir = "";
    String hEriaPortNo = "";
    String hEriaEcsIp = "";
    String ipCode = "";
    int int1 = 0;

    String[] hCptrPtrType = new String[250];
    String[] hCptrPtrId = new String[250];
    String[] hCptrPtrValue = new String[250];
    String[] hCptrDelNote = new String[250];
    String[] hCptrNotchgRsn = new String[250];
    String[] hNptrPtrType = new String[250];
    String[] hNptrPtrId = new String[250];
    String[] hNptrPtrValue = new String[250];
    String[] hNptrDelNote = new String[250];
    String[] hNptrNotchgRsn = new String[250];

    double hActAcctJrnlBal = 0;
    double hTotPurchase = 0;
    int hCardCount = 0;
    int hNotchgRecno = 0;
    int totCnt = 0;
    int allCnt = 0;
    String hCardcatCode = "";
//    String h_group_code = "";
    String hParamDate = "";
    String swProcess = "";
    String swOld = "";
    String swEmap01 = "";
    int nomoveFlag = 0;
    int chgCardFlag = 0;
    int hContiRecnoPm = 0;
    int cntCPtr = 0;
    int cntNPtr = 0;
    int printCnt = 0;
    int applySeq = 0;
    int hContiRecnoE = 0;
    int hSupCount = 0;
    int intRem = 0;
    String hIdnoBusinessCode = "";
    String hIdnoCompanyName = "";
    String hIdnoJobPosition = "";
    int hIdnoAnnualIncome = 0;
    String hCcaSpecFlag = "";
    String hCcaBlockReason = "";
    String toDay = "";
    String tmpWfValue = "";

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
//            if (args.length != 1 && args.length != 2) {
//                comc.err_exit("Usage : CrdC001 end_date [batch_seq]", "");
//            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (comc.getSubString(hCallBatchSeqno, 0, 8).equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
                hCallBatchSeqno = "no-call";
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            hTempUser = "";
            if (hCallBatchSeqno.length() == 20) {

                comcr.hCallBatchSeqno = hCallBatchSeqno;
                comcr.hCallRProgramCode = javaProgram;

                comcr.callbatch(0, 0, 1);
                sqlCmd = "select user_id ";
                sqlCmd += " from ptr_callbatch  ";
                sqlCmd += "where batch_seqno = ? ";
                setString(1, hCallBatchSeqno);
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hTempUser = getValue("user_id");
                }
            }
            if (hTempUser.length() == 0) {
                hModUser = comc.commGetUserID();
                hTempUser = hModUser;
            }

            hModPgm = javaProgram;
            toDay = sysDate;
            
            selectPtrSysParm();
            
            if (args.length != 0)
            	hParamDate = args[0].substring(0, 6);
            else {
            	if(isLastBusinday(toDay)) {
            		showLogMessage("I", "", String.format("今日[%s]為當月的第[%s]個營業日，開始續卡篩選作業", toDay, tmpWfValue));
            		hParamDate = selectAddMonth(toDay,1);
            	}
            	else {
            		  showLogMessage("I", "", String.format("今日[%s]非當月的第[%s]個營業日，程式執行結束", toDay, tmpWfValue));
                      finalProcess();
                      return 0;
            	}
            }
        		        	
            hParamDateLike = "";
            hParamDateLike = String.format("%s%s", hParamDate, "%");

            showLogMessage("I", "", String.format("Process DATE=[%s],[%s]\n", hParamDate, hParamDateLike));

            hTmpDate = "";
            hChiDate = "";
            hLimitConsumeDate = "";
            sqlCmd = "select trim(to_char(to_number(to_char(sysdate,'yyyymmdd')-19110000) ,'0000000')) h_chi_date, ";
            sqlCmd += " to_char(sysdate,'yymmdd') h_tmp_date, ";
            sqlCmd += "to_char(sysdate,'yyyymmdd') as h_system_date, ";
            sqlCmd += "to_char(sysdate -2 YEARS,'yyyymmdd') as h_limit_consume_date ";
            sqlCmd += " from dual ";
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hChiDate = getValue("h_chi_date");
                hTmpDate = getValue("h_tmp_date");
                hLimitConsumeDate = getValue("h_limit_consume_date");
            }
            
            prepare8669Rtn();

            swProcess = "M";
            mainProcess();

             // ==============================================
            // 固定要做的
            showLogMessage("I", "", String.format("程式執行結束"));
            
            if (hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); /* 1: 結束 */
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
    void prepare8669Rtn() throws Exception {

        hBegDate = "";
        sqlCmd = "select to_char(sysdate,'yyyymm')||'01' h_beg_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_number(to_char(sysdate,'dd')) h_system_dd ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBegDate = getValue("h_beg_date");
            hSystemDate = getValue("h_system_date");
            hSystemDd = getValueInt("h_system_dd");
        }

        if (hSystemDd >= 25) {
            sqlCmd = "select to_char(add_months(sysdate,1),'yyyymm')||'01' h_beg_date ";
            sqlCmd += " from dual ";
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hBegDate = getValue("h_beg_date");
            }
        }

        if (hSystemDd >= 25) {
            sqlCmd = "select to_char(add_months(sysdate,1),'yyyymm')||'01' h_beg_date ";
            sqlCmd += " from dual ";
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hBegDate = getValue("h_beg_date");
            }
        }

    }

    /**********************************************************************/
    void mainProcess() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_no_code,";
        sqlCmd += "group_code,";
        sqlCmd += "source_code,";
        sqlCmd += "sup_flag,";
        sqlCmd += "major_relation,";
        sqlCmd += "major_id_p_seqno,";
        sqlCmd += "major_card_no,";
        sqlCmd += "eng_name,";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "force_flag,";
        sqlCmd += "unit_code,";
        sqlCmd += "emboss_data,";
        sqlCmd += "acct_type,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "p_seqno,";
        sqlCmd += "new_beg_date,";
        sqlCmd += "new_end_date,";
        sqlCmd += "current_code,";
        sqlCmd += "fee_code,";
        sqlCmd += "electronic_code,";
        sqlCmd += "card_type,";
        sqlCmd += "bin_no   ,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "corp_p_seqno,";
        sqlCmd += "corp_act_flag,";
        sqlCmd += "change_reason,";
        sqlCmd += "change_status,";
        sqlCmd += "change_date,";
        // sqlCmd += "block_reason,";
        // sqlCmd += "block_reason2,";
        sqlCmd += "reissue_status,";
        sqlCmd += "reissue_date,";
        sqlCmd += "ic_flag,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "combo_indicator,";
        sqlCmd += "curr_code,";
        sqlCmd += "jcic_score,";
        sqlCmd += "last_consume_date,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from crd_card ";
        sqlCmd += "where current_code     = '0'  ";
        sqlCmd += "  and expire_chg_flag  =''  ";
        sqlCmd += "  and decode(change_status,'','x',change_status) not in ('1','2')  ";
        sqlCmd += "  and sup_flag         = '0' ";
        sqlCmd += "  and new_end_date  like ? ";
        sqlCmd += "order by group_code,card_type,id_p_seqno,sup_flag ";
        setString(1, hParamDateLike);
        int recordCnt = selectTable();
if(debug == 1)
   showLogMessage("I", "", "*** 888 Main cnt=["+ hParamDateLike +"]"+recordCnt);
        for (int1 = 0; int1 < recordCnt; int1++) {
            hMCardCardNo = getValue("card_no", int1);
            hMCardCorpNo = getValue("corp_no", int1);
            hMCardCorpNoCode = getValue("corp_no_code", int1);
            hMCardGroupCode = getValue("group_code", int1);
            hMCardSourceCode = getValue("source_code", int1);
            hMCardSupFlag = getValue("sup_flag", int1);
            hMCardMajorRelation = getValue("major_relation", int1);
            hMCardMajorIdPSeqno = getValue("major_id_p_seqno", int1);
            hMCardMajorCardNo = getValue("major_card_no", int1);
            hMCardEngName = getValue("eng_name", int1);
            hMCardRegBankNo = getValue("reg_bank_no", int1);
            hMCardForceFlag = getValue("force_flag", int1);
            hMCardUnitCode = getValue("unit_code", int1);
            hMCardElectronicCode = getValue("electronic_code", int1);
            hMCardEmbossData = getValue("emboss_data", int1);
            hMCardAcctType = getValue("acct_type", int1);
            hMCardPSeqno = getValue("acno_p_seqno", int1);
            hMCardGpNo = getValue("p_seqno", int1);
            hMCardNewBegDate = getValue("new_beg_date", int1);
            hMCardNewEndDate = getValue("new_end_date", int1);
            hMCardCurrentCode = getValue("current_code", int1);
            hMCardFeeCode = getValue("fee_code", int1);
            hMCardCardType = getValue("card_type", int1);
            hMCardElectronicCode = getValue("electronic_code", int1);
            hMCardBinNo = getValue("bin_no", int1);
            hMCardIdPSeqno = getValue("id_p_seqno", int1);
            hMCardCorpPSeqno = getValue("corp_p_seqno", int1);
            hMCardCorpActFlag = getValue("corp_act_flag", int1);
            hMCardChangeReason = getValue("change_reason", int1);
            hMCardChangeStatus = getValue("change_status", int1);
            hMCardChangeDate = getValue("change_date", int1);
            hMCardReissueStatus = getValue("reissue_status", int1);
            hMCardReissueDate = getValue("reissue_date", int1);
            hMCardIcFlag = getValue("ic_flag", int1);
            hMCardStmtCycle = getValue("stmt_cycle", int1);
            hMCardComboIndicator = getValue("combo_indicator", int1);
            hMCardCurrCode = getValue("curr_code", int1);
            hMCardJcicScore = getValueDouble("jcic_score", int1);
            hMCardLastConsumeDate = getValue("last_consume_date", int1);
            hMCardRowid = getValue("rowid", int1);

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("c001 Process record=[%d]\n", totCnt));
if(debug == 1) {
   showLogMessage("I", "", " 888 get=["+ hMCardCardNo +"]"+ totCnt +","+ hMCardIdPSeqno);
   showLogMessage("I", "", "     sup=["+ hMCardSupFlag +"]"+ hMCardGroupCode);
  }

            /****** 續卡中資料不要重覆送 *****/
            if ((hMCardChangeStatus.equals("1")) || (hMCardChangeStatus.equals("2"))) {
                continue;
            }

            if ((hMCardReissueStatus.equals("1")) || (hMCardReissueStatus.equals("2"))) {
                continue;
            }
                
            getPtrExtn();
               
            if (hMCardSupFlag.equals("0")) {                   
            	getActAcno();                   
            	getActAcct();               
            }
            
            currSupFlag = "N";
            process();
            hCardCount++;
        }

        stderr = String.format("篩選總筆數=[%d] 續卡筆數=[%d] 不續卡筆數=[%d]", hCardCount, hSupCount ,hNotchgRecno);
        showLogMessage("I", "", stderr);
    }

    /***********************************************************************/
    void process() throws Exception {

        nomoveFlag = 0;
        swMove = "N";
        swOld = "Y";
        hNomoveReason = "";
            
        chgCardFlag = 0;
            
        /* 判斷是否續卡 */            
        chgCardFlag = conditionCheck();
if(debug == 1) showLogMessage("I", "", "  condition_check1="+ chgCardFlag);        

        if (chgCardFlag == 0) { /* 可續卡 */
            if (hContiRecno == 0) {
                getBatchno(); /* 產生續卡的批號 */
            }
            hSupCount++;
            hContiRecno++;
            allCnt++;
            hContiRecnoPm++;
            getValidDate();
            /* 檢查是否需上地址變更註記 */
            getIdnoData(hMCardIdPSeqno);
            moveData();
            /* 製卡暫存檔 */
            insertCrdEmbossTmp();
            updateCrdCard(1, chgCardFlag, 0);
            procChgSupCard(1, chgCardFlag);
        } else {
            hNotchgRecno++;
            getIdnoData(hMCardIdPSeqno);
            updateCrdCard(2, chgCardFlag, 0);
            procChgSupCard(2, chgCardFlag);
        }
if(debug == 1)
  showLogMessage("I", "", " 888 Final="+ chgCardFlag +","+ hContiRecno +","+ hNotchgRecno);

        return;
    }

    /***********************************************************************/
    int getPtrExtn() throws Exception {
        /* 取得展期參數 */
        hPtrExtnYear = 0;
        try {            
            sqlCmd  = "select extn_year ";
            sqlCmd += " from crd_item_unit ";
            sqlCmd += "where unit_code = ?  ";
            setString(1, hMCardUnitCode);
            int recordCnt = selectTable();
            if (notFound.equals("Y")) {
                hPtrExtnYear = 2;
            }
            if (recordCnt > 0) {
                hPtrExtnYear = getValueLong("extn_year");
            }
        } catch (Exception ex) {
            hPtrExtnYear = 2;
        }

        return 0;
    }
    /***********************************************************************/
    void getActAcno1() throws Exception {
        
        sqlCmd = "select acct_key  ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acct_type      = ? ";
        sqlCmd += "  and acno_p_seqno   = ? ";
        setString(1, hMCardAcctType);
        setString(2, hMCardPSeqno);
        int recordCnt = selectTable();
        if(notFound.equals("Y")) {
           comcr.errRtn("select_act_acno1 not found=", hMCardPSeqno,comcr.hCallBatchSeqno);
        }
        hAcnoAcctKey = getValue("acct_key");
    }
    /***********************************************************************/
    void getActAcno() throws Exception {
        hActLineOfCreditAmt = 0;
        hChgAddrDate = "";
        hVip = "";
        hActNo = "";
        hRiskBankNo = "";
        hAcnoBlockReason2 = "";
        hPaymentRate = "";
        hBillAddress = "";
        hAcnoBillSendingZip = "";
        hAcnoBillSendingAddr1 = "";
        hAcnoBillSendingAddr2 = "";
        hAcnoBillSendingAddr3 = "";
        hAcnoBillSendingAddr4 = "";
        hAcnoBillSendingAddr5 = "";
        hAcnoChgAddrDate = "";
        hAcnoChgAddrDate = "";
        hAcnoStatSendInternet = "";
        sqlCmd = "select a.line_of_credit_amt,";
        sqlCmd += "a.chg_addr_date,";
        sqlCmd += "a.vip_code,";
        sqlCmd += "a.autopay_acct_no,";
        sqlCmd += "a.risk_bank_no,";
        sqlCmd += "nvl((select block_reason2||block_reason3||block_reason4||block_reason5 as block_reason2 "
                + "from cca_card_acct b where a.acno_p_seqno = b.acno_p_seqno and a.acct_type = b.acct_type "
                + "and b.debit_flag = 'N' fetch first 1 rows only),'') as h_acno_block_reason2, ";
        sqlCmd += "decode(a.payment_rate1,'','00',a.payment_rate1)||"
                + "decode(a.payment_rate2,'','00',a.payment_rate2)|| "
                + "decode(a.payment_rate3,'','00',a.payment_rate3)||"
                + "decode(a.payment_rate4,'','00',a.payment_rate4)|| "
                + "decode(a.payment_rate5,'','00',a.payment_rate5)||"
                + "decode(a.payment_rate6,'','00',a.payment_rate6)|| "
                + "decode(a.payment_rate7,'','00',a.payment_rate7)||"
                + "decode(a.payment_rate8,'','00',a.payment_rate8)|| "
                + "decode(a.payment_rate9,'','00',a.payment_rate9)||"
                + "decode(a.payment_rate10,'','00',a.payment_rate10)|| "
                + "decode(a.payment_rate11,'','00',a.payment_rate11)||"
                + "decode(a.payment_rate12,'','00',a.payment_rate12)|| "
                + "decode(a.payment_rate13,'','00',a.payment_rate13)||"
                + "decode(a.payment_rate14,'','00',a.payment_rate14)|| "
                + "decode(a.payment_rate15,'','00',a.payment_rate15)||"
                + "decode(a.payment_rate16,'','00',a.payment_rate16)|| "
                + "decode(a.payment_rate17,'','00',a.payment_rate17)||"
                + "decode(a.payment_rate18,'','00',a.payment_rate18)|| "
                + "decode(a.payment_rate19,'','00',a.payment_rate19)||"
                + "decode(a.payment_rate20,'','00',a.payment_rate20)|| "
                + "decode(a.payment_rate21,'','00',a.payment_rate21)||"
                + "decode(a.payment_rate22,'','00',a.payment_rate22)|| "
                + "decode(a.payment_rate23,'','00',a.payment_rate23)||"
                + "decode(a.payment_rate24,'','00',a.payment_rate24)|| "
                + "decode(a.payment_rate25,'','00',a.payment_rate25) h_payment_rate,";
        sqlCmd += "a.chg_addr_date,";
        sqlCmd += "a.bill_sending_zip,";
        sqlCmd += "(a.bill_sending_addr1||" + "a.bill_sending_addr2||" + "a.bill_sending_addr3||"
                + "a.bill_sending_addr4||" + "a.bill_sending_addr5) as bill_address,";
        sqlCmd += "a.bill_sending_addr1,";
        sqlCmd += "a.bill_sending_addr2,";
        sqlCmd += "a.bill_sending_addr3,";
        sqlCmd += "a.bill_sending_addr4,";
        sqlCmd += "a.bill_sending_addr5,";
        sqlCmd += "a.stat_send_internet ";
        sqlCmd += " from act_acno a ";
        sqlCmd += "where a.acno_p_seqno = ? ";
        setString(1, hMCardPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno not found=" + hMCardCardNo, hMCardPSeqno, hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hActLineOfCreditAmt = getValueInt("line_of_credit_amt");
            hChgAddrDate = getValue("chg_addr_date");
            hVip = getValue("vip_code");
            hActNo = getValue("autopay_acct_no");
            hRiskBankNo = getValue("risk_bank_no");
            hAcnoBlockReason2 = getValue("h_acno_block_reason2");
            hPaymentRate = getValue("h_payment_rate");
            hAcnoChgAddrDate = getValue("chg_addr_date");
            hAcnoBillSendingZip = getValue("bill_sending_zip");
            hBillAddress = getValue("bill_address");
            hAcnoBillSendingAddr1 = getValue("bill_sending_addr1");
            hAcnoBillSendingAddr2 = getValue("bill_sending_addr2");
            hAcnoBillSendingAddr3 = getValue("bill_sending_addr3");
            hAcnoBillSendingAddr4 = getValue("bill_sending_addr4");
            hAcnoBillSendingAddr5 = getValue("bill_sending_addr5");
            hAcnoStatSendInternet = getValue("stat_send_internet");
        }

    }

    /***********************************************************************/
    void getActAcct() throws Exception {
        hActAcctJrnlBal = 0;
        checkBLAmt();

        hTttAcctJrnlBal = 0;
        sqlCmd = "select acct_jrnl_bal ";
        sqlCmd += " from act_acct  ";
        sqlCmd += "where p_seqno = ? ";
        setString(1, hMCardGpNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct not found!", hMCardGpNo, hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTttAcctJrnlBal = getValueDouble("acct_jrnl_bal");
        }

    }

    /***********************************************************************/
    int checkBLAmt() throws Exception {
        String[] item = new String[10];
        double hOtherFee = 0.00;
        int i = 0;

        item[0] = "BL";
        item[1] = "IT";
        item[2] = "CA";
        item[3] = "ID";
        item[4] = "AO";
        item[5] = "OT";
        for (i = 0; i <= 5; i++) {
            hItemName = "";
            hItemName = item[i];
            hOtherFee = 0.00;
            sqlCmd = "select (billed_end_bal + unbill_end_bal) h_other_fee ";
            sqlCmd += " from act_acct_sum  ";
            sqlCmd += "where p_seqno   = ?  ";
            sqlCmd += "  and acct_code = ? ";
            setString(1, hMCardGpNo);
            setString(2, hItemName);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hOtherFee = getValueDouble("h_other_fee");
            }

            if (hOtherFee > 0.00) {
                hActAcctJrnlBal += hOtherFee;
            }
        }

        return 0;
    }

    /***********************************************************************/
    int conditionCheck() throws Exception {
	
    	String hChiName = "";
    	int rtn = 0;
	    int rtn1 = 0;
	    int temp = 0;
	    hExpireReason = "";
	    hExpireAddr = "N";

        //卡片超過2年無交易不續卡
	    if(hMCardLastConsumeDate.length() > 0) {
	        if(Integer.parseInt(hMCardLastConsumeDate) < Integer.parseInt(hLimitConsumeDate)) {
		        return 1;
	        } 
	    }
	    else {
	    	return 1;
	    }
  
        //基本資料不全不續卡(工作狀態、公司名稱、行業別、職稱、年收入)缺一不可,但工作狀態為退休其他可空白
	    //20230428卡部改可續卡
//        selectCrdIdno();
//  
//        if(!hIdnoBusinessCode.equals("1700")) {
//	        if(hIdnoBusinessCode.equals("") || hIdnoCompanyName.equals("") || hIdnoJobPosition.equals("") || hIdnoAnnualIncome == 0) {
//		        return 1;
//	        }
//        }
  
        //特指不續卡
        selectCcaCardBase();
  
        if(hCcaSpecFlag.equals("Y")) {
	        return 1;
        }
        
        //凍結不續卡
        selectCcaCardAcct();
        
        if(!hCcaBlockReason.equals("")) {
        	return 1;
        }

        return 0;
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
	    hIdnoBusinessCode = ""; 
	    hIdnoCompanyName = "";  
	    hIdnoJobPosition = "";  
	    hIdnoAnnualIncome = 0;
	
        sqlCmd = "select business_code, ";
        sqlCmd += " company_name,  ";
        sqlCmd += " job_position,  ";
        sqlCmd += " annual_income  ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hMCardIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "[" + hMCardCardNo + "],[" + hMCardIdPSeqno + "]", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoBusinessCode = getValue("business_code");
            hIdnoCompanyName  = getValue("company_name");
            hIdnoJobPosition  = getValue("job_position");
            hIdnoAnnualIncome = getValueInt("annual_income");
        }
    }

    /***********************************************************************/
    void selectCcaCardBase() throws Exception {
	    hCcaSpecFlag = ""; 
	
        sqlCmd = "select spec_flag ";
        sqlCmd += " from cca_card_base  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hMCardCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_cca_card_base not found!", "[" + hMCardCardNo + "]", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
        	hCcaSpecFlag = getValue("spec_flag");
        }
    }

    /***********************************************************************/
    void selectCcaCardAcct() throws Exception {
    	hCcaBlockReason = ""; 
	
        sqlCmd = "select block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as block_reason ";
        sqlCmd += " from cca_card_acct  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString(1, hMCardPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_cca_card_acct not found!", "[" + hMCardCardNo + "]", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
        	hCcaBlockReason = getValue("block_reason");
        }
    }

    /***********************************************************************/
    void getIdnoData(String idPSeqno) throws Exception {
        hIdnoMarriage = "";
        hIdnoNation = "";
        hIdnoChiName = "";
        hIdnoStudent = "";

        sqlCmd = "select marriage,";
        sqlCmd += "nation,";
        sqlCmd += "chi_name,";
        sqlCmd += "sex,";
        sqlCmd += "birthday,";
        sqlCmd += "decode(student,'','N',student) h_idno_student ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, idPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", idPSeqno, hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoMarriage = getValue("marriage");
            hIdnoNation = getValue("nation");
            hIdnoChiName = getValue("chi_name");
            hIdnoSex = getValue("sex");
            hIdnoBirthday = getValue("birthday");
            hIdnoStudent = getValue("h_idno_student");
        }
    }

    /***********************************************************************/
   void getBatchno() throws Exception {
        String hTmpBatchno = "";
        int hTmpRecno = 0;
        int hTmpNo = 0;

        sqlCmd = "select max(to_number(nvl(substr(batchno,7,2),0)))+1 h_tmp_no,";
        sqlCmd += "max(recno)+1 h_tmp_recno ";
        sqlCmd += " from crd_emboss_tmp  ";
        sqlCmd += "where substr(batchno,1,6) = ? ";
        setString(1, hTmpDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTmpNo = getValueInt("h_tmp_no");
            hTmpRecno = getValueInt("h_tmp_recno");
        }

        if (hTmpNo == 0)
            hTmpNo = 1;

        hContiBatchno = "";
        hTmpBatchno = String.format("%s%02d", hTmpDate, hTmpNo);
        hContiBatchno = hTmpBatchno;
        hContiRecno = hTmpRecno;
        if (debug == 1)
            showLogMessage("I", "", " Batchno=" + hTmpBatchno + "," + hContiRecno);
    }

    /***
     * @throws Exception
     *********************************************************************/
    void getValidDate() throws Exception {

        hAddMonths = (int) (hPtrExtnYear * 12);
        hValidFm = hBegDate;

        sqlCmd = "select to_char(add_months(to_date(? ,'yyyymmdd'),?),'yyyymmdd') as h_valid_to";
        sqlCmd += " from dual ";
        setString(1, hMCardNewEndDate);
        setInt(2, hAddMonths);
if(debug == 1)
   showLogMessage("I", "", "  get_valid_da="+ hMCardNewEndDate +","+ hAddMonths);

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hValidTo = getValue("h_valid_to");
        }
    }

    /***********************************************************************/
    /* 製卡暫存檔 */
    void insertCrdEmbossTmp() throws Exception {
        hModSeqno = comcr.getModSeq();

        if (debug == 1)
            showLogMessage("I", "", " inst emboss_t=" + hContiBatchno + "," + hContiRecno);

        hSource = "3";

        hCardComboIndicator = "";
        sqlCmd = "select decode(combo_indicator,'','N',combo_indicator) h_card_combo_indicator ";
        sqlCmd += " from ptr_group_code  ";
        sqlCmd += "where group_code = ? ";
        setString(1, hCardGroupCode.length() == 0 ? "0000" : hCardGroupCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_group_code not found!", hCardGroupCode, hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCardComboIndicator = getValue("h_card_combo_indicator");
        }

        getActAcno1();

        setValue("chi_name", hIdnoChiName);
        setValue("birthday", hIdnoBirthday);
        setValueInt("credit_lmt", hActLineOfCreditAmt);
        setValue("force_flag", hForceFlag);

        String[] info = comcr.getIDInfo(hCardIdPSeqno);
if(debug == 1) showLogMessage("I", "", "  card="+ hCardIdPSeqno + ","+info[0]);
        setValue("apply_id"       , info[0]);
        setValue("apply_id_code"  , info[1]);

        setValue("sup_flag"       , hCardSupFlag);
        setValue("group_code"     , hCardGroupCode);
        setValue("source_code"    , hCardSourceCode);
        setValue("eng_name"       , hCardEngName);
        setValue("unit_code"      , hCardUnitCode);
        setValue("electronic_code", hCardElectronicCode);
        setValue("emboss_4th_data", hCardEmbossData);
        setValue("card_type"      , hCardCardType);
        setValue("bin_no"         , hCardBinNo);
        setValue("batchno"        , hContiBatchno);
        setValueDouble("recno"    , hContiRecno);
        setValue("corp_no"        , hCardCorpNo);
        setValue("corp_no_code"   , hCardCorpNoCode);

        info = comcr.getIDInfo(hCardMajorIdPSeqno);
        setValue("pm_id"        , info[0]);
        setValue("pm_id_code"   , info[1]);

        setValue("acct_type"    , hCardAcctType);
        setValue("acct_key"     , hAcnoAcctKey);
        setValue("p_seqno"      , hCardPSeqno);
        setValue("reg_bank_no"  , hCardRegBankNo);
        setValue("risk_bank_no" , hRiskBankNo);
        setValue("emboss_source", hSource);
        setValue("to_nccc_code" , "Y");
        setValue("card_no"      , hCardCardNo);
        setValue("old_card_no"  , hCardCardNo);
        setValue("old_beg_date" , hCardNewBegDate);
        setValue("old_end_date" , hCardNewEndDate);
        setValue("change_reason", "1");
        setValue("status_code"  , "1");
        setValue("reason_code"  , "");
        setValue("valid_fm"     , hValidFm);
        setValue("valid_to"     , hValidTo);
        setValue("major_card_no", hCardMajorCardNo);
        setValue("major_valid_fm", hValidFm);
        setValue("major_valid_to", hValidTo);
        setValue("chg_addr_flag" , hChgAddrFlag);
        setValue("fee_code"      , hMCardFeeCode);
        setValue("nccc_type"     , "2");
        setValue("act_no"        , hActNo);
        setValue("vip"           , hVip);
        setValueDouble("purchase_amt", hSumPurchaseAcct);
        setValueDouble("balance_amt" , hTttAcctJrnlBal);
        setValue("ic_flag"           , hCardIcFlag);
        setValue("combo_indicator"   , hCardComboIndicator);
        setValue("curr_code"         , hCardCurrCode);
        setValueDouble("jcic_score"  , hCardJcicScore);
        setValue("confirm_date"      , sysDate);
        setValue("confirm_user"      , hModUser);
        setValue("crt_date"          , sysDate);
        setValue("mod_time"          , sysDate + sysTime);
        setValue("mod_user"          , hModUser);
        setValue("mod_pgm"           , hModPgm);

        daoTable = "crd_emboss_tmp";

        insertTable();
    }

    /***********************************************************************/
    void updateCrdCard(int flag, int value, int sup) throws Exception {
        String pCardNo = "";

        if (debug == 1)
            showLogMessage("I", "", " UPDATE CARD=" + flag + "," + value + "," + sup);
        hValue = value;
        hChangeReason = "";
        hChangeStatus = "";
        hExpireChgFlag = "";
        hExpireChgDate = "";
        pCardNo = "";
        if (sup == 0) {
            pCardNo = hMCardCardNo;
        } else {
            pCardNo = hCardCardNo;
        }
        /* 續卡 */
        if (flag == 1) {
            hChangeReason = "1"; /* 系統自動續卡 */
            hChangeStatus = "1"; /* 續卡中 */
            hChangeDate = sysDate;
            hExpireReason = "";
            hExpireChgFlag = "";
            hExpireChgDate = "";
        }
        /* 不續卡 */
        if (flag == 2) {
            hChangeDate = "";
            hChangeReason = "";
            hChangeStatus = "";
            hExpireChgFlag = "1"; /* 系統自動不續卡 */
            hExpireChgDate = sysDate;
        }
        if (swMove.equals("Y")) {
            hExpireChgFlag = "1";
            hExpireReason = "z1";
            hExpireChgDate = sysDate;
        }
        daoTable   = "crd_card";
        updateSQL  = " expire_reason   = ?,";
        updateSQL += " expire_chg_flag = ?,";
        updateSQL += " expire_chg_date = ?,";
        updateSQL += " expire_addr     = ?,";
        updateSQL += " change_date     = ?,";
        updateSQL += " change_reason   = ?,";
        updateSQL += " change_status   = ?,";
        updateSQL += " mod_time        = sysdate,";
        updateSQL += " mod_user        = ?,";
        updateSQL += " mod_pgm         = ?";
        whereStr   = "where card_no    = ? ";
        setString(1, hExpireReason);
        setString(2, hExpireChgFlag);
        setString(3, hExpireChgDate);
        setString(4, hExpireAddr);
        setString(5, hChangeDate);
        setString(6, hChangeReason);
        setString(7, hChangeStatus);
        setString(8, hModUser);
        setString(9, prgmId);
        setString(10, pCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_card not found!", pCardNo, hCallBatchSeqno);
        }

        return;
    }

    /***********************************************************************/
    /***
     * 帶出所有之附卡,並同時做續卡/不續卡
     * 
     * @param type
     * @param rtnValue
     * @return
     * @throws Exception
     */
    int procChgSupCard(int type, int rtnValue) throws Exception {

        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_no_code,";
        sqlCmd += "group_code,";
        sqlCmd += "source_code,";
        sqlCmd += "sup_flag,";
        sqlCmd += "major_relation,";
        sqlCmd += "major_id_p_seqno,";
        sqlCmd += "major_card_no,";
        sqlCmd += "eng_name,";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "force_flag,";
        sqlCmd += "unit_code,";
        sqlCmd += "emboss_data,";
        sqlCmd += "change_status,";
        sqlCmd += "change_date,";
        sqlCmd += "reissue_status,";
        sqlCmd += "reissue_date,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "current_code,";
        sqlCmd += "electronic_code,";
        sqlCmd += "card_type,";
        sqlCmd += "bin_no,";
        sqlCmd += "new_beg_date,";
        sqlCmd += "new_end_date,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "corp_p_seqno,";
        sqlCmd += "ic_flag,";
        sqlCmd += "combo_indicator,";
        sqlCmd += "curr_code,";
        sqlCmd += "jcic_score ";
        sqlCmd += "from crd_card ";
        sqlCmd += "where major_card_no   = ? ";
        sqlCmd += "  and current_code    = '0'  ";
        sqlCmd += "  and sup_flag        = '1'  ";
        sqlCmd += "  and expire_chg_flag ='' ";
        sqlCmd += "order by id_p_seqno,card_no ";
        setString(1, hMCardMajorCardNo);
        int recordCnt = selectTable();

        if (recordCnt == 0)
            return 0;

        for (int i = 0; i < recordCnt; i++) {
            hCardCardNo = getValue("card_no", i);
            hCardCorpNo = getValue("corp_no", i);
            hCardCorpNoCode = getValue("corp_no_code", i);
            hCardCardType = getValue("card_type", i);
            hCardGroupCode = getValue("group_code", i);
            hCardSourceCode = getValue("source_code", i);
            hCardSupFlag = getValue("sup_flag", i);
            hCardMajorRelation = getValue("major_relation", i);
            hCardMajorIdPSeqno = getValue("major_id_p_seqno", i);
            hCardMajorCardNo = getValue("major_card_no", i);
            hCardEngName = getValue("eng_name", i);
            hCardRegBankNo = getValue("reg_bank_no", i);
            hCardForceFlag = getValue("force_flag", i);
            hCardUnitCode = getValue("unit_code", i);
            hCardEmbossData = getValue("emboss_data", i);
            hCardChangeStatus = getValue("change_status", i);
            hCardChangeDate = getValue("change_date", i);
            hCardReissueStatus = getValue("reissue_status", i);
            hCardReissueDate = getValue("reissue_date", i);
            hCardPSeqno = getValue("acno_p_seqno", i);
            hCardGpNo = getValue("p_seqno", i);
            hCardAcctType = getValue("acct_type", i);
            hCardCurrentCode = getValue("current_code", i);
            hCardElectronicCode = getValue("electronic_code", i);
            hCardBinNo = getValue("bin_no", i);
            hCardNewBegDate = getValue("new_beg_date", i);
            hCardNewEndDate = getValue("new_end_date", i);
            hCardIdPSeqno = getValue("id_p_seqno", i);
            hCardCorpPSeqno = getValue("corp_p_seqno", i);
            hCardIcFlag = getValue("ic_flag", i);
            hCardComboIndicator = getValue("combo_indicator", i);
            hCardCurrCode = getValue("curr_code", i);
            hCardJcicScore = getValueDouble("jcic_score", i);

            swMove = "N";
            /*********************************************************************
             * 當附卡效期大於正卡效期,不做續卡 2001/08/08
             *********************************************************************/                
            if (hCardNewEndDate.compareTo(hMCardNewEndDate) > 0) {                
            	continue;                
            }                

            if ((hCardChangeStatus.equals("1")) || (hCardChangeStatus.equals("2"))) {                
            	continue;                
            }
                
            if ((hCardReissueStatus.equals("1")) || (hCardReissueStatus.equals("2"))) {
            	continue;                
            }                    

            currSupFlag = "Y";
            getIdnoData(hCardIdPSeqno);

            switch (type) {
            case 1:
                hContiRecno++;
                allCnt++;
                insertCrdEmbossTmp();
                updateCrdCard(1, rtnValue, 1);
                hSupCount++;
                break;
            case 2:
                updateCrdCard(2, rtnValue, 1);
                hNotchgRecno++;
                break;
            }
        }
        
        return 0;
    }
    
    /***********************************************************************/
    void selectPtrSysParm() throws Exception {
      
        sqlCmd  = "select wf_value ";
        sqlCmd += "  from ptr_sys_parm   ";
        sqlCmd += " where wf_parm = 'SYSPARM'  ";
        sqlCmd += "   and wf_key = 'RENEW_CARD' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
      	  tmpWfValue = getValue("wf_value");
        }
        return;
    }
    /***********************************************************************/
    
	/**
	 *  檢核是否為當月第N個營業日。
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean isLastBusinday(String toDay) throws Exception {
		String secondDate = "";
		int day = 1;
		int businDay = 0;
		while(true) {
			secondDate = String.format("%6.6s%02d", toDay,day);
			sqlCmd = "select count(*) as holiday_cnt from ptr_holiday where holiday = ? ";
			setString(1, secondDate);
			selectTable();
			int cnt = getValueInt("holiday_cnt");
			if(cnt <= 0) {
				businDay++;
				if(businDay == Integer.parseInt(tmpWfValue)) {
					break;
				}				
			}
			day++;
		}
		
		if(toDay.equals(secondDate)) {
			return true;
		}
		
		return false;
	}

    /***************************************************************************/
	   private String selectAddMonth(String inDate, int idx) throws Exception {
	        selectSQL = "to_char(add_months(to_date( ? ,'yyyymmdd'), ? ),'yyyymm')  as out_date";
	        daoTable = "sysibm.dual";
	        whereStr = "FETCH FIRST 1 ROW ONLY";

	        setString(1, inDate);
	        setInt(2, idx);
	        selectTable();

	        return getValue("out_date");
	    }

	/***************************************************************************/

    void initCard() {
        hCardElectronicCode = "";
        hCardCardType = "";
        hCardCardNo = "";
        hCardCorpNo = "";
        hCardCorpNoCode = "";
        hCardGroupCode = "";
        hCardSourceCode = "";
        hCardSupFlag = "";
        hCardMajorRelation = "";
        hCardMajorCardNo = "";
        hCardEngName = "";
        hCardRegBankNo = "";
        hCardUnitCode = "";
        hCardEmbossData = "";
        hCardChangeStatus = "";
        hCardChangeDate = "";
        hCardReissueStatus = "";
        hCardReissueDate = "";
        hCardPSeqno = "";
        hCardGpNo = "";
        hCardNewBegDate = "";
        hCardNewEndDate = "";
        hCardCurrentCode = "";
        hCardIdPSeqno = "";
        hCardCorpPSeqno = "";
        hCardIcFlag = "";
        hCardStmtCycle = "";
        hCardComboIndicator = "";
        hCardCurrCode = "";
        hCardJcicScore = 0;
    }

    /***************************************************************************/
    void moveData() {
        initCard();

        hCardAcctType = hMCardAcctType;
        hCardGroupCode = hMCardGroupCode;
        hCardSourceCode = hMCardSourceCode;
        hCardSupFlag = hMCardSupFlag;
        hCardEngName = hMCardEngName;
        hCardUnitCode = hMCardUnitCode;
        hCardEmbossData = hMCardEmbossData;
        hCardElectronicCode = hMCardElectronicCode;
        hCardCardType = hMCardCardType;
        hCardCorpNo = hMCardCorpNo;
        hCardCorpNoCode = hMCardCorpNoCode;
        hCardRegBankNo = hMCardRegBankNo;
        hCardCardNo = hMCardCardNo;
        hCardBinNo = hMCardBinNo;
        hCardNewBegDate = hMCardNewBegDate;
        hCardNewEndDate = hMCardNewEndDate;
        hCardIcFlag = hMCardIcFlag;
        hCardStmtCycle = hMCardStmtCycle;
        hCardComboIndicator = hMCardComboIndicator;
        hCardCurrCode = hMCardCurrCode;
        hCardJcicScore = hMCardJcicScore;

        hCardIdPSeqno = hMCardIdPSeqno;
        hCardMajorIdPSeqno = hMCardMajorIdPSeqno;

if(debug == 1) showLogMessage("I", "", "  MOVE="+ hCardIdPSeqno + ","+ hMCardIdPSeqno);

        return;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdC001 proc = new CrdC001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}

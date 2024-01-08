/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/03  V1.00.00    phopho     program initial                          *
*  108/12/02  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/14  V1.00.02    shiyuqi       updated for project coding standard   *
*  112/03/01  V1.00.03    sunny      cancel openALMONFiles()取消產生檔案      *
*  112/09/27  V1.00.04    sunny      移除不需要的程式邏輯                     *
******************************************************************************/

package Col;

import java.text.Normalizer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC001 extends AccessDAO {
    private String progname = "傳送CS(M1)C-cycle產生媒體檔案處理程式   109/12/14  V1.00.02 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar3 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar4 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar5 = new ArrayList<Map<String, Object>>();

    int    rptSeq1               = 0;
    int    rptSeq2               = 0;
    int    rptSeq3               = 0;
    int    rptSeq4               = 0;
    int    rptSeq5               = 0;
    String hCallBatchSeqno = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;
    String hcurpmodlog = "";
    String hCallRProgramCode = "";
    String buf                   = "";

    int hCprmGenCsDay = 0;
    String hBusiBusinessDate = "";
    String hTempBusinessDate = "";
    String hWdayStmtCycle = "";
    String hWdayThisAcctMonth = "";
    String hWdayThisCloseDate = "";
    String hCcbsCorpPSeqno = "";
    String hCcbsAcctType = "";
    String hCcotCorpOnFlag = "";
    int hTempMcode = 0;
    String hCcbsIdPSeqno = "";
    String hCcbsPSeqno = "";
    int hCcbsMcode = 0;
    String hTempCorpFlag = "";
    String hTempIdnoFlag = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoCorpNo = "";
    String hAcnoAcctStatus = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCreditActNo = "";
    String hAcnoBillSendingZip = "";
    String hTempAddr1 = "";
    String hAcnoAutopayAcctBank = "";
    String hAcnoAutopayAcctNo = "";
    String hAcnoStopStatus = "";
    String hAcnoBlockDate = "";
    String hAcnoOrgDelinquentDate = "";
    String hAcnoBlockReason = "";
    String hAcnoBlockReason2 = "";
    String hAcnoCreateDate = "";
    String hAcnoNoTelCollFlag = "";
    String hAcnoNoPerCollFlag = "";
    String hAcnoNoUnblockFlag = "";
    String hAcnoNoFStopFlag = "";
    String hAcnoNoAdjLocLow = "";
    String hAcnoRcUseIndicator = "";
    String hCcotEffectCardMark = "";
    double hAcnoLineOfCreditAmt = 0;
    String hAcnoCardIndicator = "";
    String hAcnoVipCode = "";
    String hAcnoPaymentNo = "";
    String hAcnoCorpActFlag = "";
    String hAcnoStatSendInternet = "";
    String hAcnoStatSendSMonth2 = "";
    String hAcnoStatSendEMonth2 = "";
    String hAcnoStatSendPaper = "";
    String hAcnoStatSendSMonth = "";
    String hAcnoStatSendEMonth = "";
    String hAcnoStatUnprintFlag = "";
    String hAcnoStatUnprintSMonth = "";
    String hAcnoStatUnprintEMonth = "";
    String hAcnoAcctHolderId = "";
    String hCardIdPSeqno = "";
    String hTempIdPSeqno = "";
    String hRelaCardNo = "";
    String hRelaId = "";
    String hRelaRelaId = "";
    String hRelaIdPSeqno = "";
    String hRelaRelaName = "";
    String hRelaCompanyName = "";
    String hRelaCompanyZip = "";
    String hTempAddr4 = "";
    String hRelaOfficeAreaCode1 = "";
    String hRelaOfficeTelNo1 = "";
    String hRelaOfficeTelExt1 = "";
    String hRelaOfficeAreaCode2 = "";
    String hRelaOfficeTelNo2 = "";
    String hRelaOfficeTelExt2 = "";
    String hRelaHomeAreaCode1 = "";
    String hRelaHomeTelNo1 = "";
    String hRelaHomeTelExt1 = "";
    String hRelaHomeAreaCode2 = "";
    String hRelaHomeTelNo2 = "";
    String hRelaHomeTelExt2 = "";
    String hRelaResidentZip = "";
    String hTempAddr5 = "";
    String hRelaCellarPhone = "";
    String hRelaBbCall = "";
    String hRelaRelaSeqno = "";
    String hRelaRowid = "";
    String hCardSourceCode = "";
    String hCardGroupCode = "";
    String hCardOppostDate = "";
    String hCardCurrentCode = "";
    String hIdnoChiName = "";
    String hIdnoId = "";
    String hIdnoBirthday = "";
    String hIdnoCompanyName = "";
    String hIdnoJobPosition = "";
    String hIdnoSex = "";
    String hIdnoHomeAreaCode1 = "";
    String hIdnoHomeTelNo1 = "";
    String hIdnoHomeTelExt1 = "";
    String hIdnoHomeAreaCode2 = "";
    String hIdnoHomeTelNo2 = "";
    String hIdnoHomeTelExt2 = "";
    String hIdnoOfficeAreaCode1 = "";
    String hIdnoOfficeTelNo1 = "";
    String hIdnoOfficeTelExt1 = "";
    String hIdnoOfficeAreaCode2 = "";
    String hIdnoOfficeTelNo2 = "";
    String hIdnoOfficeTelExt2 = "";
    String hIdnoResidentZip = "";
    String hTempAddr3 = "";
    String hIdnoCardSince = "";
    String hIdnoCellarPhone = "";
    String hIdnoBbCall = "";
    String hIdnoContactor1Relation = "";
    String hIdnoContactor1Name = "";
    String hIdnoContactor1AreaCode = "";
    String hIdnoContactor1Tel = "";
    String hIdnoContactor1Ext = "";
    String hIdnoContactor2Relation = "";
    String hIdnoContactor2Name = "";
    String hIdnoContactor2AreaCode = "";
    String hIdnoContactor2Tel = "";
    String hIdnoContactor2Ext = "";
    String hIdnoSalaryCode = "";
    String hIdnoSalaryHoldinFlag = "";
    String hIdnoEMailAddr = "";
    String hCcotBkFlag = "";
    String hCcotFhFlag = "";
    String hCorpCorpNo = "";
    String hCorpChiName = "";
    String hCorpRegZip = "";
    String hTempAddr2 = "";
    String hCorpContactName = "";
    String hCorpCorpTelZone1 = "";
    String hCorpCorpTelNo1 = "";
    String hCorpCorpTelExt1 = "";
    String hCorpCorpTelZone2 = "";
    String hCorpCorpTelNo2 = "";
    String hCorpCorpTelExt2 = "";
    String hCorpChargeName = "";
    String hCorpEMailAddr = "";
    String hCcotPSeqno = "";
    String hCcotCorpPSeqno = "";
    String hCcotCorpNo = "";
    String hCcotIdPSeqno = "";
    String hCcotId = "";
    String hCcotCardNo = "";
    String hCcotFormType = "";
    int hCcotMcode = 0;
    String hCcotLiabStatus = "";
    String hCcotLiabDate = "";
    String hCcotLiacStatus = "";
    String hCcotLiacDate = "";
    String hCcotLiacEndReason = "";
    String hCcotRenewStatus = "";
    String hCcotRenewDate = "";
    String hCcotLiquStatus = "";
    String hCcotLiquDate = "";
    String hCcotTrialDate = "";
    String hCcotCsmDate = "";
    String hCcotDcCurrFlag = "";
    String hCcotLastDDate = "";
    String hCcotCsmCasetype = "";
    double hCcotStmtOverDueAmt = 0;
    double hCcotTtlAmtBal = 0;
    int hCnt = 0;
    String hWdayLastAcctMonth = "";
    double hAgenMixMpBalance = 0;
    String hAcagAcctMonth = "";
    double hAcagPayAmt = 0;
    String hAclgLogDate = "";
    String hTempBirthday = "";
    int hTemp1Cnt = 0;
    int hTemp2Cnt = 0;

    int maxMcode = 0;
    double hCcotPayAmt = 0;
    int retInt = 0;
    int totalCnt = 0;
    int[] reasonCnt = new int[20];

    String temstr1 = "";
    String temstr2 = "";
    String temstr3 = "";
    String temstr4 = "";
    String temstr5 = "";

    PrtBuf01 prtData01 = new PrtBuf01();
    PrtBuf02 prtData02 = new PrtBuf02();
    PrtBuf03 prtData03 = new PrtBuf03();
    PrtBuf04 prtData04 = new PrtBuf04();
    PrtBuf05 prtData05 = new PrtBuf05();

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
                comc.errExit("Usage : ColC001 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            if (args.length == 1)
                hBusiBusinessDate = args[0];
            selectPtrBusinday();
            selectPtrActgeneral();
            selectColParam();
            if (selectPtrWorkday() != 0) {
            	exceptExit = 0;
                comcr.errRtn(String.format("本日[%s]+[%d]不需執行", hBusiBusinessDate, hCprmGenCsDay), "", hCallBatchSeqno);
            }

            showLogMessage("I", "", String.format("本日[%s]執行[%s] cycle", hBusiBusinessDate, hWdayStmtCycle));

            //openALMONFiles();
            showLogMessage("I", "", String.format("========================================="));
            showLogMessage("I", "", String.format("處理商務卡公司戶開始..."));
            totalCnt = 0;
            selectColCsBase1();
            showLogMessage("I", "", String.format("累計處理筆數 [%d]", totalCnt));
            showLogMessage("I", "", String.format("累計筆數  公司戶      [%d]", reasonCnt[1]));
            showLogMessage("I", "", String.format("              (總繳)  [%d]", reasonCnt[10]));
            showLogMessage("I", "", String.format("              (個繳)  [%d]", reasonCnt[1] - reasonCnt[10]));
            showLogMessage("I", "", String.format("          公司總繳戶  [%d]", reasonCnt[7]));
            showLogMessage("I", "", String.format("-----------------------------------------"));
            showLogMessage("I", "", String.format("處理商務卡公司戶開始..."));
            totalCnt = 0;
            selectColCsBase2();
            showLogMessage("I", "", String.format("累計處理筆數 [%d]", totalCnt));
            showLogMessage("I", "", String.format("          公司個繳戶  [%d]", reasonCnt[2]));
            showLogMessage("I", "", String.format("          保證人      [%d]", reasonCnt[6]));
            showLogMessage("I", "", String.format("-----------------------------------------"));
            showLogMessage("I", "", String.format("處理一般卡開始..."));
            totalCnt = 0;
            selectColCsBase3();
            showLogMessage("I", "", String.format("累計處理筆數 [%d]", totalCnt));
            showLogMessage("I", "", String.format("累計筆數  一般卡(正卡)[%d]", reasonCnt[3]));
            showLogMessage("I", "", String.format("          一般卡(附卡)[%d]", reasonCnt[4]));
            showLogMessage("I", "", String.format("          保證人      [%d]", reasonCnt[5] - reasonCnt[6]));
            showLogMessage("I", "", String.format("========================================="));
            showLogMessage("I", "", String.format("累計筆數  公司戶      [%d]", reasonCnt[1]));
            showLogMessage("I", "", String.format("          公司總繳戶  [%d]", reasonCnt[7]));
            showLogMessage("I", "", String.format("          公司個繳戶  [%d]", reasonCnt[2]));
            showLogMessage("I", "", String.format("          一般卡(正卡)[%d]", reasonCnt[3]));
            showLogMessage("I", "", String.format("          一般卡(附卡)[%d]", reasonCnt[4]));
            showLogMessage("I", "", String.format("          保證人      [%d]", reasonCnt[5]));
            showLogMessage("I", "", String.format("========================================="));
            //closeALMONFiles();

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
        hTempBusinessDate = "";
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date,";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))),'yyyymmdd') -1 days,'yyyymmdd') temp_business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempBusinessDate = getValue("temp_business_date");
        }
    }

    /***********************************************************************/
    void selectPtrActgeneral() throws Exception {
        hAgenMixMpBalance = 0;
        sqlCmd = "select mix_mp_balance ";
        sqlCmd += " from ptr_actgeneral_n ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAgenMixMpBalance = getValueDouble("mix_mp_balance");
        }
    }

    /***********************************************************************/
    void selectColParam() throws Exception {
        hCprmGenCsDay = 0;
        sqlCmd = "select gen_cs_day ";
        sqlCmd += " from col_param ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_param not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCprmGenCsDay = getValueInt("gen_cs_day");
        }
    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        sqlCmd = "select stmt_cycle,";
        sqlCmd += "this_acct_month,";
        sqlCmd += "this_close_date ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where to_date(this_close_date,'yyyymmdd') + ? days = to_date(?,'yyyymmdd') -1 days ";
        setInt(1, hCprmGenCsDay);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayThisCloseDate = getValue("this_close_date");
        } else
            return 1;
        return 0;
    }

    /***********************************************************************/
    void openALMONFiles() throws Exception {
        temstr1 = String.format("%s/media/col/CS/%sALMON01.TXT", comc.getECSHOME(), hWdayStmtCycle);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        temstr2 = String.format("%s/media/col/CS/%sALMON02.TXT", comc.getECSHOME(), hWdayStmtCycle);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        temstr3 = String.format("%s/media/col/CS/%sALMON03.TXT", comc.getECSHOME(), hWdayStmtCycle);
        temstr3 = Normalizer.normalize(temstr3, java.text.Normalizer.Form.NFKD);
        temstr4 = String.format("%s/media/col/CS/%sALMON04.TXT", comc.getECSHOME(), hWdayStmtCycle);
        temstr4 = Normalizer.normalize(temstr4, java.text.Normalizer.Form.NFKD);
        temstr5 = String.format("%s/media/col/CS/%sALMON05.TXT", comc.getECSHOME(), hWdayStmtCycle);
        temstr5 = Normalizer.normalize(temstr5, java.text.Normalizer.Form.NFKD);

        buf = String.format("%15.15s%8.8s%06d", " ", " ", 0);

        lpar1.add(comcr.putReport("", "", sysDate, rptSeq1++, "0", buf));
        lpar2.add(comcr.putReport("", "", sysDate, rptSeq2++, "0", buf));
        lpar3.add(comcr.putReport("", "", sysDate, rptSeq3++, "0", buf));
        lpar4.add(comcr.putReport("", "", sysDate, rptSeq4++, "0", buf));
        lpar5.add(comcr.putReport("", "", sysDate, rptSeq5++, "0", buf));
    }

    
    /***********************************************************************/
    void selectColCsBase1() throws Exception {
        for (int i = 0; i < reasonCnt.length; i++)
            reasonCnt[i] = 0;

        sqlCmd = "select ";
        sqlCmd += "corp_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "max(decode(id_p_seqno,'','Y','N')) h_ccot_corp_on_flag,";
        sqlCmd += "max(decode(id_p_seqno,'',mcode,0)) h_temp_mcode ";
        sqlCmd += "from col_cs_base a,ptr_acct_type b ";
        sqlCmd += "where a.stmt_cycle = ? ";
        sqlCmd += "and a.acct_type = b.acct_type ";
        sqlCmd += "and b.card_indicator ='2' ";
        sqlCmd += "group by corp_p_seqno,a.acct_type ";
        setString(1, hWdayStmtCycle);

        openCursor();
        while (fetchTable()) {
            initData();
            hCcbsCorpPSeqno = getValue("corp_p_seqno");
            hCcbsAcctType = getValue("acct_type");
            hCcotCorpOnFlag = getValue("h_ccot_corp_on_flag");
            hTempMcode = getValueInt("h_temp_mcode");

            totalCnt++;
            if (totalCnt % 1000 == 0)
                showLogMessage("I", "", String.format("    目前處理筆數 [%d]", totalCnt));

            if (selectCrdCorp() != 0)
                continue;

            if (selectActAcno1() != 0)
                continue;
            selectActAcnoA();

            selectCrdCard1();
            selectActAcag();
            selectActAcctHst();

            genALMON01(); /* 商務卡總繳戶 */

            if (hCcotCorpOnFlag.equals("Y")) {
                reasonCnt[10]++;
                selectActAcno2();
            }
        }
        closeCursor();
    }

    /**************************************************************************/
    void initData() {
        hCcotLiabStatus = "";
        hCcotLiabDate = "";
        hCcotLiacStatus = "";
        hCcotLiacDate = "";
        hCcotLiacEndReason = "";
        hCcotRenewStatus = "";
        hCcotRenewDate = "";
        hCcotLiquStatus = "";
        hCcotLiquDate = "";
        hCcotTrialDate = "";
        hCcotBkFlag = "";
        hCcotFhFlag = "";
        hCcotCsmDate = "";
        hCcotCsmCasetype = "";
        hCcotDcCurrFlag = "N";
        hCcotLastDDate = "";
        hCcotCorpOnFlag = "";
        hCcotStmtOverDueAmt = 0;
        hCcotTtlAmtBal = 0;
    }

    /***********************************************************************/
    int selectCrdCorp() throws Exception {
        hCorpCorpNo = "";
        hCorpChiName = "";
        hCorpRegZip = "";
        hTempAddr2 = "";
        hCorpContactName = "";
        hCorpCorpTelZone1 = "";
        hCorpCorpTelNo1 = "";
        hCorpCorpTelExt1 = "";
        hCorpCorpTelZone2 = "";
        hCorpCorpTelNo2 = "";
        hCorpCorpTelExt2 = "";
        hCorpChargeName = "";
        hCorpEMailAddr = "";

        sqlCmd = "select corp_no,";
        sqlCmd += "chi_name,";
        sqlCmd += "reg_zip,";
        sqlCmd += "trim(substrb(reg_addr1||reg_addr2||reg_addr3||reg_addr4||reg_addr5,1,70)) h_temp_addr2,";
        sqlCmd += "contact_name,";
        sqlCmd += "corp_tel_zone1,";
        sqlCmd += "corp_tel_no1,";
        sqlCmd += "corp_tel_ext1,";
        sqlCmd += "corp_tel_zone2,";
        sqlCmd += "corp_tel_no2,";
        sqlCmd += "corp_tel_ext2,";
        sqlCmd += "charge_name,";
        sqlCmd += "e_mail_addr ";
        sqlCmd += " from crd_corp ";
        sqlCmd += "where corp_p_seqno = ? ";
        setString(1, hCcbsCorpPSeqno);
        
        extendField = "crd_corp.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCorpCorpNo = getValue("crd_corp.corp_no");
            hCorpChiName = getValue("crd_corp.chi_name");
            hCorpRegZip = getValue("crd_corp.reg_zip");
            hTempAddr2 = getValue("crd_corp.h_temp_addr2");
            hCorpContactName = getValue("crd_corp.contact_name");
            hCorpCorpTelZone1 = getValue("crd_corp.corp_tel_zone1");
            hCorpCorpTelNo1 = getValue("crd_corp.corp_tel_no1");
            hCorpCorpTelExt1 = getValue("crd_corp.corp_tel_ext1");
            hCorpCorpTelZone2 = getValue("crd_corp.corp_tel_zone2");
            hCorpCorpTelNo2 = getValue("crd_corp.corp_tel_no2");
            hCorpCorpTelExt2 = getValue("crd_corp.corp_tel_ext2");
            hCorpChargeName = getValue("crd_corp.charge_name");
            hCorpEMailAddr = getValue("crd_corp.e_mail_addr");
        } else {
            showLogMessage("I", "", String.format("ERROR:corp_p_seqno[%s] not exist crd_corp", hCcbsCorpPSeqno));
            return 1;
        }
        return 0;
    }

    /***********************************************************************/
    int selectActAcno1() throws Exception {
        hAcnoPSeqno = "";
        hAcnoAcctPSeqno = "";
        hAcnoAcctType = "";
        hAcnoAcctKey = "";
        hAcnoCorpPSeqno = "";
        hAcnoCorpNo = "";
        hAcnoAcctStatus = "";
        hAcnoIdPSeqno = "";
        hAcnoCreditActNo = "";
        hAcnoBillSendingZip = "";
        hTempAddr1 = "";
        hAcnoAutopayAcctBank = "";
        hAcnoAutopayAcctNo = "";
        hAcnoStopStatus = "";
        hAcnoBlockDate = "";
        hAcnoOrgDelinquentDate = "";
        hAcnoBlockReason = "";
        hAcnoBlockReason2 = "";
        hAcnoCreateDate = "";
        hAcnoNoTelCollFlag = "";
        hAcnoNoPerCollFlag = "";
        hAcnoNoUnblockFlag = "";
        hAcnoNoFStopFlag = "";
        hAcnoNoAdjLocLow = "";
        hAcnoRcUseIndicator = "";
        hAcnoLineOfCreditAmt = 0;
        hAcnoCardIndicator = "";
        hAcnoCorpActFlag = "";
        hAcnoVipCode = "";
        hAcnoPaymentNo = "";
        hAcnoStatSendInternet = "";
        hAcnoStatSendSMonth2 = "";
        hAcnoStatSendEMonth2 = "";
        hAcnoStatSendPaper = "";
        hAcnoStatSendSMonth = "";
        hAcnoStatSendEMonth = "";
        hAcnoStatUnprintFlag = "";
        hAcnoStatUnprintSMonth = "";
        hAcnoStatUnprintEMonth = "";

//        sqlCmd = "select a.p_seqno,";
//        sqlCmd += "a.gp_no acct_p_seqno,";
        sqlCmd = "select a.acno_p_seqno,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "d.corp_no,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.credit_act_no,";
        sqlCmd += "a.bill_sending_zip,";
        sqlCmd += "trim(substrb(bill_sending_addr1||bill_sending_addr2||bill_sending_addr3 ||bill_sending_addr4||bill_sending_addr5,1,120)) h_temp_addr1,";
        sqlCmd += "a.autopay_acct_bank,";
        sqlCmd += "a.autopay_acct_no,";
        sqlCmd += "a.stop_status,";
        sqlCmd += "e.block_date,";
        sqlCmd += "a.org_delinquent_date,";
        sqlCmd += "e.block_reason1 block_reason,";
        sqlCmd += "e.block_reason2||e.block_reason3||e.block_reason4||e.block_reason5 block_reason2,";
        sqlCmd += "a.crt_date,";
        sqlCmd += "a.no_tel_coll_flag,";
        sqlCmd += "a.no_per_coll_flag,";
        sqlCmd += "a.no_unblock_flag,";
        sqlCmd += "a.no_f_stop_flag,";
        sqlCmd += "decode(no_adj_loc_low,'Y','Y', decode(no_adj_loc_high,'Y','Y','')) h_acno_no_adj_loc_low,";
        sqlCmd += "a.rc_use_indicator,";
        sqlCmd += "a.line_of_credit_amt,";
        sqlCmd += "a.card_indicator,";
        sqlCmd += "decode(a.vip_code,'6S','6S','WW','5S','5S','5S','WV','4S', 'V0','4S','V1','4S','V2','4S','V3','4S','V4','4S', 'V5','4S','V6','4S','V7','4S','V8','4S','4S','4S',' ') h_acno_vip_code,";
        sqlCmd += "a.payment_no,";
        sqlCmd += "a.corp_act_flag,";
        sqlCmd += "a.stat_send_internet,";
        sqlCmd += "a.stat_send_s_month2,";
        sqlCmd += "a.stat_send_e_month2,";
        sqlCmd += "a.stat_send_paper,";
        sqlCmd += "a.stat_send_s_month,";
        sqlCmd += "a.stat_send_e_month,";
        sqlCmd += "a.stat_unprint_flag,";
        sqlCmd += "a.stat_unprint_s_month,";
        sqlCmd += "a.stat_unprint_e_month ";
        sqlCmd += " from act_acno a ";
        sqlCmd += "  left join crd_corp d on d.corp_p_seqno = a.corp_p_seqno "; //find corp_no in crd_corp
//        sqlCmd += "  left join cca_card_acct e on a.p_seqno = e.p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";
        sqlCmd += "  left join cca_card_acct e on a.acno_p_seqno = e.acno_p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";
        sqlCmd += "where a.corp_p_seqno = ? ";
        //sqlCmd += "and  decode(a.p_seqno,'','x',a.p_seqno) = a.gp_no ";
        sqlCmd += "and  a.acno_flag <> 'Y' ";  //acno_flag (商務卡總個繳詳細註 ) : 1.一般卡, 2.商務卡總繳(公司), 3.商務卡個繳, Y.商務卡總繳(個人)
        sqlCmd += "and  a.acct_type = ? ";
        setString(1, hCcbsCorpPSeqno);
        setString(2, hCcbsAcctType);
        
        extendField = "act_acno_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
//            h_acno_p_seqno = getValue("p_seqno");
//            h_acno_acct_p_seqno = getValue("acct_p_seqno");
            hAcnoPSeqno = getValue("act_acno_1.acno_p_seqno");
            hAcnoAcctPSeqno = getValue("act_acno_1.p_seqno");
            hAcnoAcctType = getValue("act_acno_1.acct_type");
            hAcnoAcctKey = getValue("act_acno_1.acct_key");
            hAcnoCorpPSeqno = getValue("act_acno_1.corp_p_seqno");
            hAcnoCorpNo = getValue("act_acno_1.corp_no");
            hAcnoAcctStatus = getValue("act_acno_1.acct_status");
            hAcnoIdPSeqno = getValue("act_acno_1.id_p_seqno");
            hAcnoCreditActNo = getValue("act_acno_1.credit_act_no");
            hAcnoBillSendingZip = getValue("act_acno_1.bill_sending_zip");
            hTempAddr1 = getValue("act_acno_1.h_temp_addr1");
            hAcnoAutopayAcctBank = getValue("act_acno_1.autopay_acct_bank");
            hAcnoAutopayAcctNo = getValue("act_acno_1.autopay_acct_no");
            hAcnoStopStatus = getValue("act_acno_1.stop_status");
            hAcnoBlockDate = getValue("act_acno_1.block_date");
            hAcnoOrgDelinquentDate = getValue("act_acno_1.org_delinquent_date");
            hAcnoBlockReason = getValue("act_acno_1.block_reason");
            hAcnoBlockReason2 = getValue("act_acno_1.block_reason2");
            hAcnoCreateDate = getValue("act_acno_1.crt_date");
            hAcnoNoTelCollFlag = getValue("act_acno_1.no_tel_coll_flag");
            hAcnoNoPerCollFlag = getValue("act_acno_1.no_per_coll_flag");
            hAcnoNoUnblockFlag = getValue("act_acno_1.no_unblock_flag");
            hAcnoNoFStopFlag = getValue("act_acno_1.no_f_stop_flag");
            hAcnoNoAdjLocLow = getValue("act_acno_1.h_acno_no_adj_loc_low");
            hAcnoRcUseIndicator = getValue("act_acno_1.rc_use_indicator");
            hAcnoLineOfCreditAmt = getValueDouble("act_acno_1.line_of_credit_amt");
            hAcnoCardIndicator = getValue("act_acno_1.card_indicator");
            hAcnoVipCode = getValue("act_acno_1.h_acno_vip_code");
            hAcnoPaymentNo = getValue("act_acno_1.payment_no");
            hAcnoCorpActFlag = getValue("act_acno_1.corp_act_flag");
            hAcnoStatSendInternet = getValue("act_acno_1.stat_send_internet");
            hAcnoStatSendSMonth2 = getValue("act_acno_1.stat_send_s_month2");
            hAcnoStatSendEMonth2 = getValue("act_acno_1.stat_send_e_month2");
            hAcnoStatSendPaper = getValue("act_acno_1.stat_send_paper");
            hAcnoStatSendSMonth = getValue("act_acno_1.stat_send_s_month");
            hAcnoStatSendEMonth = getValue("act_acno_1.stat_send_e_month");
            hAcnoStatUnprintFlag = getValue("act_acno_1.stat_unprint_flag");
            hAcnoStatUnprintSMonth = getValue("act_acno_1.stat_unprint_s_month");
            hAcnoStatUnprintEMonth = getValue("act_acno_1.stat_unprint_e_month");
        }

        return 0;
    }

    /***********************************************************************/
    void selectActAcnoA() throws Exception {
        hTempCorpFlag = "";
        hTempIdnoFlag = "";

//        sqlCmd = "select max(decode(p_seqno,gp_no,'N','Y')) h_temp_corp_flag,";
//        sqlCmd += "max(decode(p_seqno,gp_no,'Y','N')) h_temp_idno_flag ";
        sqlCmd = "select max(decode(acno_p_seqno,p_seqno,'N','Y')) h_temp_corp_flag,";
        sqlCmd += "max(decode(acno_p_seqno,p_seqno,'Y','N')) h_temp_idno_flag ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where corp_p_seqno  = ? ";
        sqlCmd += "and  acct_type  = ?  ";
        sqlCmd += "and  id_p_seqno <> '' ";
        setString(1, hCcbsCorpPSeqno);
        setString(2, hCcbsAcctType);
        
        extendField = "act_acno_a.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno_a not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCorpFlag = getValue("act_acno_a.h_temp_corp_flag");
            hTempIdnoFlag = getValue("act_acno_a.h_temp_idno_flag");
        }
    }

    /***********************************************************************/
    void selectActAcno2() throws Exception {

        sqlCmd = "select ";
//        sqlCmd += "a.p_seqno,";
//        sqlCmd += "a.gp_no acct_p_seqno,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "d.corp_no,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.credit_act_no,";
        sqlCmd += "a.bill_sending_zip,";
        sqlCmd += "trim(substrb(bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5,1,70)) h_temp_addr1,";
        sqlCmd += "a.autopay_acct_bank,";
        sqlCmd += "a.autopay_acct_no,";
        sqlCmd += "a.stop_status,";
        sqlCmd += "e.block_date,";
        sqlCmd += "a.org_delinquent_date,";
        sqlCmd += "e.block_reason1 block_reason,";
        sqlCmd += "e.block_reason2||e.block_reason3||e.block_reason4||e.block_reason5 block_reason2,";
        sqlCmd += "a.crt_date,";
        sqlCmd += "a.no_tel_coll_flag,";
        sqlCmd += "a.no_per_coll_flag,";
        sqlCmd += "a.no_unblock_flag,";
        sqlCmd += "a.no_f_stop_flag,";
        sqlCmd += "decode(no_adj_loc_low,'Y','Y',decode(no_adj_loc_high,'Y','Y','')) h_acno_no_adj_loc_low,";
        sqlCmd += "a.rc_use_indicator,";
        sqlCmd += "a.line_of_credit_amt,";
        sqlCmd += "a.card_indicator,";
        sqlCmd += "decode(vip_code,'6S','6S','WW','5S','5S','5S','V0','4S','V1','4S','V2','4S','V3','4S','V4','4S','V5','4S','V6','4S','V7','4S','V8','4S','4S','4S','  ') h_acno_vip_code,";
        sqlCmd += "a.payment_no,";
        sqlCmd += "a.corp_act_flag,";
        sqlCmd += "a.stat_send_internet,";
        sqlCmd += "a.stat_send_s_month2,";
        sqlCmd += "a.stat_send_e_month2,";
        sqlCmd += "a.stat_send_paper,";
        sqlCmd += "a.stat_send_s_month,";
        sqlCmd += "a.stat_send_e_month,";
        sqlCmd += "a.stat_unprint_flag,";
        sqlCmd += "a.stat_unprint_s_month,";
        sqlCmd += "a.stat_unprint_e_month ";
        sqlCmd += " from act_acno a ";
        sqlCmd += "  left join crd_corp d on d.corp_p_seqno = a.corp_p_seqno "; //find corp_no in crd_corp
//        sqlCmd += "  left join cca_card_acct e on a.p_seqno = e.p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";
//        sqlCmd += "where a.gp_no = ? ";
        sqlCmd += "  left join cca_card_acct e on a.acno_p_seqno = e.acno_p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";
        sqlCmd += "where a.p_seqno = ? ";
        sqlCmd += "and a.acno_flag = 'Y' ";  //Y.商務卡總繳(個人)
        sqlCmd += "and a.id_p_seqno <> '' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acno_2.";
        
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            initData();
//            h_acno_p_seqno = getValue("p_seqno", i);
//            h_acno_acct_p_seqno = getValue("acct_p_seqno", i);
            hAcnoPSeqno = getValue("act_acno_2.acno_p_seqno", i);
            hAcnoAcctPSeqno = getValue("act_acno_2.p_seqno", i);
            hAcnoAcctType = getValue("act_acno_2.acct_type", i);
            hAcnoAcctKey = getValue("act_acno_2.acct_key", i);
            hAcnoCorpPSeqno = getValue("act_acno_2.corp_p_seqno", i);
            hAcnoCorpNo = getValue("act_acno_2.corp_no", i);
            hAcnoAcctStatus = getValue("act_acno_2.acct_status", i);
            hAcnoIdPSeqno = getValue("act_acno_2.id_p_seqno", i);
            hTempIdPSeqno = hAcnoIdPSeqno;
            hAcnoCreditActNo = getValue("act_acno_2.credit_act_no", i);

            hAcnoBillSendingZip = getValue("act_acno_2.bill_sending_zip", i);
            hTempAddr1 = getValue("act_acno_2.h_temp_addr1", i);
            hAcnoBillSendingZip = comc.commBig5Asc(hAcnoBillSendingZip);
            hTempAddr1 = comc.commBig5Asc(hTempAddr1);

            hAcnoAutopayAcctBank = getValue("act_acno_2.autopay_acct_bank", i);
            hAcnoAutopayAcctNo = getValue("act_acno_2.autopay_acct_no", i);
            hAcnoStopStatus = getValue("act_acno_2.stop_status", i);
            hAcnoBlockDate = getValue("act_acno_2.block_date", i);
            hAcnoOrgDelinquentDate = getValue("act_acno_2.org_delinquent_date", i);
            hAcnoBlockReason = getValue("act_acno_2.block_reason", i);
            hAcnoBlockReason2 = getValue("act_acno_2.block_reason2", i);
            hAcnoCreateDate = getValue("act_acno_2.crt_date", i);
            hAcnoNoTelCollFlag = getValue("act_acno_2.no_tel_coll_flag", i);
            hAcnoNoPerCollFlag = getValue("act_acno_2.no_per_coll_flag", i);
            hAcnoNoUnblockFlag = getValue("act_acno_2.no_unblock_flag", i);
            hAcnoNoFStopFlag = getValue("act_acno_2.no_f_stop_flag", i);
            hAcnoNoAdjLocLow = getValue("act_acno_2.h_acno_no_adj_loc_low", i);
            hAcnoRcUseIndicator = getValue("act_acno_2.rc_use_indicator", i);
            hAcnoLineOfCreditAmt = getValueDouble("act_acno_2.line_of_credit_amt", i);
            hAcnoCardIndicator = getValue("act_acno_2.card_indicator", i);
            hAcnoVipCode = getValue("act_acno_2.h_acno_vip_code", i);
            hAcnoPaymentNo = getValue("act_acno_2.payment_no", i);
            hAcnoCorpActFlag = getValue("act_acno_2.corp_act_flag", i);
            hAcnoStatSendInternet = getValue("act_acno_2.stat_send_internet", i);
            hAcnoStatSendSMonth2 = getValue("act_acno_2.stat_send_s_month2", i);
            hAcnoStatSendEMonth2 = getValue("act_acno_2.stat_send_e_month2", i);
            hAcnoStatSendPaper = getValue("act_acno_2.stat_send_paper", i);
            hAcnoStatSendSMonth = getValue("act_acno_2.stat_send_s_month", i);
            hAcnoStatSendEMonth = getValue("act_acno_2.stat_send_e_month", i);
            hAcnoStatUnprintFlag = getValue("act_acno_2.stat_unprint_flag", i);
            hAcnoStatUnprintSMonth = getValue("act_acno_2.stat_unprint_s_month", i);
            hAcnoStatUnprintEMonth = getValue("act_acno_2.stat_unprint_e_month", i);

            hTempIdPSeqno = hAcnoIdPSeqno;
            selectCrdIdno();

//            selectColLiabNego();
            if (selectColLiacNego() != 0)
                selectColLiacNegoHst();
//            selectColLiadRenew();
//            selectColLiadLiquidate();
            selectRskTrialIdno();
//            selectCmsCasemaster();

            reasonCnt[7]++;
            genALMON02(); /* 商務卡總繳戶 */
        }
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoChiName = "";
        hIdnoId = "";
        hIdnoBirthday = "";
        hIdnoCompanyName = "";
        hIdnoJobPosition = "";
        hIdnoSex = "";
        hIdnoHomeAreaCode1 = "";
        hIdnoHomeTelNo1 = "";
        hIdnoHomeTelExt1 = "";
        hIdnoHomeAreaCode2 = "";
        hIdnoHomeTelNo2 = "";
        hIdnoHomeTelExt2 = "";
        hIdnoOfficeAreaCode1 = "";
        hIdnoOfficeTelNo1 = "";
        hIdnoOfficeTelExt1 = "";
        hIdnoOfficeAreaCode2 = "";
        hIdnoOfficeTelNo2 = "";
        hIdnoOfficeTelExt2 = "";
        hIdnoResidentZip = "";
        hTempAddr3 = "";
        hIdnoCardSince = "";
        hIdnoCellarPhone = "";
        hIdnoBbCall = "";
        hIdnoContactor1Relation = "";
        hIdnoContactor1Name = "";
        hIdnoContactor1AreaCode = "";
        hIdnoContactor1Tel = "";
        hIdnoContactor1Ext = "";
        hIdnoContactor2Relation = "";
        hIdnoContactor2Name = "";
        hIdnoContactor2AreaCode = "";
        hIdnoContactor2Tel = "";
        hIdnoContactor2Ext = "";
        hIdnoSalaryCode = "";
        hIdnoSalaryHoldinFlag = "";
        hIdnoEMailAddr = "";

        sqlCmd = "select trim(substrb(chi_name,1,20)) h_idno_chi_name,"; /* sunny,trim(substrb(chi_name,1,10)) */
        sqlCmd += "id_no||id_no_code id_no,";
        sqlCmd += "birthday,";
        sqlCmd += "company_name,";
        sqlCmd += "job_position,";
        sqlCmd += "sex,";
        sqlCmd += "home_area_code1,";
        sqlCmd += "home_tel_no1,";
        sqlCmd += "home_tel_ext1,";
        sqlCmd += "home_area_code2,";
        sqlCmd += "home_tel_no2,";
        sqlCmd += "home_tel_ext2,";
        sqlCmd += "office_area_code1,";
        sqlCmd += "office_tel_no1,";
        sqlCmd += "office_tel_ext1,";
        sqlCmd += "office_area_code2,";
        sqlCmd += "office_tel_no2,";
        sqlCmd += "office_tel_ext2,";
        sqlCmd += "resident_zip,";
        sqlCmd += "trim(substrb(resident_addr1||resident_addr2||resident_addr3|| resident_addr4||resident_addr5,1,70)) h_temp_addr3,";
        sqlCmd += "card_since,";
        sqlCmd += "cellar_phone,";
        sqlCmd += "'' bb_call,";  //no column
        sqlCmd += "contactor1_relation,";
        sqlCmd += "contactor1_name,";
        sqlCmd += "contactor1_area_code,";
        sqlCmd += "contactor1_tel,";
        sqlCmd += "contactor1_ext,";
        sqlCmd += "contactor2_relation,";
        sqlCmd += "contactor2_name,";
        sqlCmd += "contactor2_area_code,";
        sqlCmd += "contactor2_tel,";
        sqlCmd += "contactor2_ext,";
        sqlCmd += "salary_code,";
        sqlCmd += "salary_holdin_flag,";
        sqlCmd += "e_mail_addr ";
        sqlCmd += " from crd_idno a  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hTempIdPSeqno);
        
        extendField = "crd_idno.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("crd_idno.h_idno_chi_name");
            hIdnoId = getValue("crd_idno.id_no");
            hIdnoBirthday = getValue("crd_idno.birthday");
            hIdnoCompanyName = getValue("crd_idno.company_name");
            hIdnoJobPosition = getValue("crd_idno.job_position");
            hIdnoSex = getValue("crd_idno.sex");
            hIdnoHomeAreaCode1 = getValue("crd_idno.home_area_code1");
            hIdnoHomeTelNo1 = getValue("crd_idno.home_tel_no1");
            hIdnoHomeTelExt1 = getValue("crd_idno.home_tel_ext1");
            hIdnoHomeAreaCode2 = getValue("crd_idno.home_area_code2");
            hIdnoHomeTelNo2 = getValue("crd_idno.home_tel_no2");
            hIdnoHomeTelExt2 = getValue("crd_idno.home_tel_ext2");
            hIdnoOfficeAreaCode1 = getValue("crd_idno.office_area_code1");
            hIdnoOfficeTelNo1 = getValue("crd_idno.office_tel_no1");
            hIdnoOfficeTelExt1 = getValue("crd_idno.office_tel_ext1");
            hIdnoOfficeAreaCode2 = getValue("crd_idno.office_area_code2");
            hIdnoOfficeTelNo2 = getValue("crd_idno.office_tel_no2");
            hIdnoOfficeTelExt2 = getValue("crd_idno.office_tel_ext2");
            hIdnoResidentZip = getValue("crd_idno.resident_zip");
            hTempAddr3 = getValue("crd_idno.h_temp_addr3");
            hIdnoCardSince = getValue("crd_idno.card_since");
            hIdnoCellarPhone = getValue("crd_idno.cellar_phone");
            hIdnoBbCall = getValue("crd_idno.bb_call");
            hIdnoContactor1Relation = getValue("crd_idno.contactor1_relation");
            hIdnoContactor1Name = getValue("crd_idno.contactor1_name");
            hIdnoContactor1AreaCode = getValue("crd_idno.contactor1_area_code");
            hIdnoContactor1Tel = getValue("crd_idno.contactor1_tel");
            hIdnoContactor1Ext = getValue("crd_idno.contactor1_ext");
            hIdnoContactor2Relation = getValue("crd_idno.contactor2_relation");
            hIdnoContactor2Name = getValue("crd_idno.contactor2_name");
            hIdnoContactor2AreaCode = getValue("crd_idno.contactor2_area_code");
            hIdnoContactor2Tel = getValue("crd_idno.contactor2_tel");
            hIdnoContactor2Ext = getValue("crd_idno.contactor2_ext");
            hIdnoSalaryCode = getValue("crd_idno.salary_code");
            hIdnoSalaryHoldinFlag = getValue("crd_idno.salary_holdin_flag");
            hIdnoEMailAddr = getValue("crd_idno.e_mail_addr");
        }
    }

    /***********************************************************************/
    void selectColLiabNego() throws Exception {
        hCcotLiabStatus = "";
        hCcotLiabDate = "";
        sqlCmd = "select liab_status,";
        sqlCmd += "decode(liab_status,'1',stop_notify_date, '2',recol_date, '3',notify_date, '4',end_date) h_ccot_liab_date ";
        sqlCmd += " from col_liab_nego ";
//        sqlCmd += "where id_no = substr(?,1,10) ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_idno_id);
        setString(1, hTempIdPSeqno);
        
        extendField = "col_liab_nego.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcotLiabStatus = getValue("col_liab_nego.liab_status");
            hCcotLiabDate = getValue("col_liab_nego.h_ccot_liab_date");
        }
    }

    /***********************************************************************/
    int selectColLiacNego() throws Exception {
        hCcotLiacStatus = "";
        hCcotLiacEndReason = "";
        hCcotLiacDate = "";
        sqlCmd = "select liac_status,";
        sqlCmd += "decode(liac_status, '4',recol_reason, '5',decode(liac_status,'','4',liac_status), '') h_ccot_liac_end_reason,";
        sqlCmd += "decode(liac_status, '1',notify_date, '2',stop_notify_date, '3',contract_date, '4',notify_date, '5',notify_date) h_ccot_liac_date ";
        sqlCmd += " from col_liac_nego  ";
//        sqlCmd += "where id_no = substr(?,1,10)  ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_idno_id);
        setString(1, hTempIdPSeqno);
        
        extendField = "col_liac_nego.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcotLiacStatus = getValue("col_liac_nego.liac_status");
            hCcotLiacEndReason = getValue("col_liac_nego.h_ccot_liac_end_reason");
            hCcotLiacDate = getValue("col_liac_nego.h_ccot_liac_date");
        } else
            return 1;
        return 0;
    }

    /***********************************************************************/
    void selectColLiacNegoHst() throws Exception {

        hCcotLiacStatus = "";
        hCcotLiacEndReason = "";
        hCcotLiacDate = "";
        sqlCmd = "select liac_status,";
        sqlCmd += "decode(liac_status, '4',recol_reason, '5',decode(liac_status,'','4',liac_status), '') h_ccot_liac_end_reason,";
        sqlCmd += "decode(liac_status, '1',notify_date, '2',stop_notify_date, '3',contract_date, '4',notify_date, '5',notify_date) h_ccot_liac_date ";
        sqlCmd += " from col_liac_nego_hst  ";
//        sqlCmd += "where id_no = substr(?,1,10)  ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and liac_status = '5' ORDER BY tran_date desc,tran_time desc ";
//        setString(1, h_idno_id);
        setString(1, hTempIdPSeqno);

        extendField = "col_liac_nego_hst.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcotLiacStatus = getValue("col_liac_nego_hst.liac_status");
            hCcotLiacEndReason = getValue("col_liac_nego_hst.h_ccot_liac_end_reason");
            hCcotLiacDate = getValue("col_liac_nego_hst.h_ccot_liac_date");
        } else
            return;
    }

    /***********************************************************************/
    void selectColLiadRenew() throws Exception {
        hCcotRenewStatus = "";
        hCcotRenewDate = "";
        sqlCmd = "select decode(renew_damage_date, '', decode(renew_status,'1','K10', '2','K20', '3','K30', '4','K40', '5','K50', '6','K60', 'K70'),'KA0') h_ccot_renew_status,";
        sqlCmd += "decode(renew_damage_date, '', decode(renew_status,'1',case_date, '2',case_date, '3',case_date, '4',deliver_date, '5',case_date, '6',case_date, case_date),renew_damage_date) h_ccot_renew_date ";
        sqlCmd += " from col_liad_renew  ";
//        sqlCmd += "where id_no = substr(?,1,10)  ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and apr_date <> '' ";
//        sqlCmd += "and recv_date = (select max(recv_date) from col_liad_renew where id_no = substr(?,1,10)  ";
        sqlCmd += "and recv_date = (select max(recv_date) from col_liad_renew where id_p_seqno = ? ";
        sqlCmd += "and apr_date <> '' ) ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_idno_id);
//        setString(2, h_idno_id);
        setString(1, hTempIdPSeqno);
        setString(2, hTempIdPSeqno);
        
        extendField = "col_liad_renew.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcotRenewStatus = getValue("col_liad_renew.h_ccot_renew_status");
            hCcotRenewDate = getValue("col_liad_renew.h_ccot_renew_date");
        }
    }

    /***********************************************************************/
    void selectColLiadLiquidate() throws Exception {
        hCcotLiquStatus = "";
        hCcotLiquDate = "";
        sqlCmd = "select lpad(court_status,2,'00') h_ccot_liqu_status,";
        sqlCmd += "case_date ";
        sqlCmd += " from col_liad_liquidate  ";
//        sqlCmd += "where id_no = substr(?,1,10)  ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and apr_date <> '' ";
//        sqlCmd += "and recv_date = (select max(recv_date) from col_liad_liquidate where id_no = substr(?,1,10)  ";
        sqlCmd += "and recv_date = (select max(recv_date) from col_liad_liquidate where id_p_seqno = ? ";
        sqlCmd += "and apr_date <> '' ) ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_idno_id);
//        setString(2, h_idno_id);
        setString(1, hTempIdPSeqno);
        setString(2, hTempIdPSeqno);
        
        extendField = "col_liad_liquidate.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcotLiquStatus = getValue("col_liad_liquidate.h_ccot_liqu_status");
            hCcotLiquDate = getValue("col_liad_liquidate.case_date");
        }
    }

    /***********************************************************************/
    void selectRskTrialIdno() throws Exception {
        hCcotTrialDate = "";
        sqlCmd = "select max(trial_date) h_ccot_trial_date ";
        sqlCmd += " from rsk_trial_idno ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hTempIdPSeqno);
        
        extendField = "rsk_trial_idno.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcotTrialDate = getValue("rsk_trial_idno.h_ccot_trial_date");
        }
    }

    /***********************************************************************/
    void selectCmsCasemaster() throws Exception {
        hCcotCsmDate = "";
        hCcotCsmCasetype = "";
//        sqlCmd = "select csm_date,";
//        sqlCmd += "csm_casetype ";
//        sqlCmd += " from cms_casemaster  ";
//        sqlCmd += "where csm_idno = substr(?,1,10)  ";
//        sqlCmd += "and csm_date = (select max(csm_Date) from cms_casemaster where csm_idno = substr(?,1,10))  ";
//        sqlCmd += "fetch first 1 row only ";
        //欄位大改了耶
        sqlCmd = "select case_date,";
        sqlCmd += "case_type ";
        sqlCmd += " from cms_casemaster  ";
        sqlCmd += "where case_idno = substr(?,1,10)  ";
        sqlCmd += "and case_date = (select max(case_date) from cms_casemaster where case_idno = substr(?,1,10))  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hIdnoId);
        setString(2, hIdnoId);
        
        extendField = "cms_casemaster.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcotCsmDate = getValue("cms_casemaster.case_date");
            hCcotCsmCasetype = getValue("cms_casemaster.case_type");
        }
    }

    /***********************************************************************/
    void genALMON05() throws Exception {
        String tmpstr = "";

        prtData05.id = hIdnoId;
        prtData05.birthday = hIdnoBirthday;
        prtData05.relaId = hRelaRelaId;
        prtData05.relaName = hRelaRelaName;
        prtData05.acctType = hAcnoAcctType;
        prtData05.companyName = hRelaCompanyName;

        hRelaCompanyZip = comc.commBig5Asc(hRelaCompanyZip); /* sunny add */
        hTempAddr4 = comc.commBig5Asc(hTempAddr4); /* sunny add */

        prtData05.companyZip = hRelaCompanyZip;
        prtData05.addr4 = hTempAddr4;
        prtData05.officeAreaCode1 = hRelaOfficeAreaCode1;
        prtData05.officeTelNo1 = hRelaOfficeTelNo1;
        prtData05.officeTelExt1 = hRelaOfficeTelExt1;
        prtData05.officeAreaCode2 = hRelaOfficeAreaCode2;
        prtData05.officeTelNo2 = hRelaOfficeTelNo2;
        prtData05.officeTelExt2 = hRelaOfficeTelExt2;
        prtData05.homeAreaCode1 = hRelaHomeAreaCode1;
        prtData05.homeTelNo1 = hRelaHomeTelNo1;
        prtData05.homeTelExt1 = hRelaHomeTelExt1;
        prtData05.homeAreaCode2 = hRelaHomeAreaCode2;
        prtData05.homeTelNo2 = hRelaHomeTelNo2;
        prtData05.homeTelExt2 = hRelaHomeTelExt2;

        hRelaResidentZip = comc.commBig5Asc(hRelaResidentZip); /* sunny add */
        hTempAddr5 = comc.commBig5Asc(hTempAddr5); /* sunny add */

        prtData05.residentZip = hRelaResidentZip;
        prtData05.addr5 = hTempAddr5;
        prtData05.cellarPhone = hRelaCellarPhone;
        prtData05.bbCall = hRelaBbCall;
        prtData05.relaSeqno = hRelaRelaSeqno;

        tmpstr = prtData05.allText();
        lpar5.add(comcr.putReport("", "", sysDate, rptSeq5++, "0", tmpstr));

        reasonCnt[5]++;
        if (hCcotFormType.equals("2"))
            reasonCnt[6]++;
    }

    /***********************************************************************/
    void genALMON04() throws Exception {
        String tmpstr = "";

        prtData04.acctHolderId = hAcnoAcctHolderId;
        prtData04.birthday = hTempBirthday;
        prtData04.companyName = hIdnoCompanyName;
        prtData04.jobPosition = hIdnoJobPosition;
        prtData04.id = hIdnoId;
        prtData04.chiName = hIdnoChiName;
        prtData04.birthday1 = hIdnoBirthday;
        prtData04.sex = hIdnoSex;
        prtData04.acctType = hAcnoAcctType;
        prtData04.officeAreaCode1 = hIdnoOfficeAreaCode1;
        prtData04.officeTelNo1 = hIdnoOfficeTelNo1;
        prtData04.officeTelExt1 = hIdnoOfficeTelExt1;
        prtData04.officeAreaCode2 = hIdnoOfficeAreaCode2;
        prtData04.officeTelNo2 = hIdnoOfficeTelNo2;
        prtData04.officeTelExt2 = hIdnoOfficeTelExt2;
        prtData04.homeAreaCode1 = hIdnoHomeAreaCode1;
        prtData04.homeTelNo1 = hIdnoHomeTelNo1;
        prtData04.homeTelExt1 = hIdnoHomeTelExt1;
        prtData04.homeAreaCode2 = hIdnoHomeAreaCode2;
        prtData04.homeTelNo2 = hIdnoHomeTelNo2;
        prtData04.homeTelExt2 = hIdnoHomeTelExt2;

        hIdnoResidentZip = comc.commBig5Asc(hIdnoResidentZip); /* sunny add */
        hTempAddr3 = comc.commBig5Asc(hTempAddr3); /* sunny add */

        prtData04.residentZip = hIdnoResidentZip;
        prtData04.addr3 = hTempAddr3;
        prtData04.cellarPhone = hIdnoCellarPhone;
        prtData04.bbCall = hIdnoBbCall;
        prtData04.csmDate = hCcotCsmDate;
        prtData04.csmCasetype = hCcotCsmCasetype;

        tmpstr = prtData04.allText();
        lpar4.add(comcr.putReport("", "", sysDate, rptSeq4++, "0", tmpstr));

        reasonCnt[4]++;
    }

    /***********************************************************************/
    void genALMON03() throws Exception {
        String tmpstr = "";

        tmpstr = String.format("%07d", comcr.str2long(hWdayThisCloseDate));
        prtData03.closeDate = tmpstr;
        prtData03.companyName = hIdnoCompanyName;
        prtData03.jobPosition = hIdnoJobPosition;
        prtData03.officeAreaCode1 = hIdnoOfficeAreaCode1;
        prtData03.officeTelNo1 = hIdnoOfficeTelNo1;
        prtData03.officeTelExt1 = hIdnoOfficeTelExt1;
        prtData03.officeAreaCode2 = hIdnoOfficeAreaCode2;
        prtData03.officeTelNo2 = hIdnoOfficeTelNo2;
        prtData03.officeTelExt2 = hIdnoOfficeTelExt2;
        prtData03.homeAreaCode1 = hIdnoHomeAreaCode1;
        prtData03.homeTelNo1 = hIdnoHomeTelNo1;
        prtData03.homeTelExt1 = hIdnoHomeTelExt1;
        prtData03.homeAreaCode2 = hIdnoHomeAreaCode2;
        prtData03.homeTelNo2 = hIdnoHomeTelNo2;
        prtData03.homeTelExt2 = hIdnoHomeTelExt2;
        prtData03.autopayAcctBank = hAcnoAutopayAcctBank;
        prtData03.autopayAcctNo = hAcnoAutopayAcctNo;
        prtData03.id = hIdnoId;
        prtData03.chiName = hIdnoChiName;
        prtData03.birthday = hIdnoBirthday;
        prtData03.sex = hIdnoSex;
        prtData03.stmtCycle = hWdayStmtCycle;

        hAcnoBillSendingZip = comc.commBig5Asc(hAcnoBillSendingZip); /* sunny add */
        hTempAddr1 = comc.commBig5Asc(hTempAddr1); /* sunny add */

        prtData03.billSendingZip = hAcnoBillSendingZip;
        prtData03.addr1 = hTempAddr1;
        prtData03.acctType = hAcnoAcctType;

        hIdnoResidentZip = comc.commBig5Asc(hIdnoResidentZip); /* sunny add */
        hTempAddr3 = comc.commBig5Asc(hTempAddr3); /* sunny add */

        prtData03.residentZip = hIdnoResidentZip;
        prtData03.addr3 = hTempAddr3;
        tmpstr = String.format("%10f", hAcnoLineOfCreditAmt);
        prtData03.lineOfCreditAmt = tmpstr;
        prtData03.stopStatus = hAcnoStopStatus;
        prtData03.blockDate = hAcnoBlockDate;
        prtData03.blockReason1 = hAcnoBlockReason;
//        prt_data03.block_reason2 = h_acno_block_reason2;
//        prt_data03.block_reason3 = h_acno_block_reason2.substring(2);
//        prt_data03.block_reason4 = h_acno_block_reason2.substring(4);
//        prt_data03.block_reason5 = h_acno_block_reason2.substring(6);
        prtData03.blockReason2 = String.format("%2.2s", hAcnoBlockReason2);
        prtData03.blockReason3 = String.format("%2.2s", comc.getSubString(hAcnoBlockReason2,2));
        prtData03.blockReason4 = String.format("%2.2s", comc.getSubString(hAcnoBlockReason2,4));
        prtData03.blockReason5 = String.format("%2.2s", comc.getSubString(hAcnoBlockReason2,6));
        prtData03.cardSince = hIdnoCardSince;
        prtData03.groupCode = hCardGroupCode;
        prtData03.cellarPhone = hIdnoCellarPhone;
        prtData03.bbCall = hIdnoBbCall;
        tmpstr = String.format("%02d", hCcbsMcode);
        prtData03.mcode = tmpstr;
        prtData03.currentCode = hCardCurrentCode;
        prtData03.noPerCollFlag = hAcnoNoPerCollFlag;
        prtData03.noTelCollFlag = hAcnoNoTelCollFlag;
        prtData03.noUnblockFlag = hAcnoNoUnblockFlag;
        prtData03.noFStopFlag = hAcnoNoFStopFlag;
        prtData03.noAdjLoc = hAcnoNoAdjLocLow;
        prtData03.noTelCollFlagV = hAcnoNoTelCollFlag;
        prtData03.rcUseIndicator = hAcnoRcUseIndicator;
        tmpstr = String.format("%14.2f", hCcotPayAmt);
        prtData03.payAmt = tmpstr;
        prtData03.orgDelinquentDate = hAcnoOrgDelinquentDate;
        prtData03.bkFlag = hCcotBkFlag;
        prtData03.fhFlag = hCcotFhFlag;
        prtData03.vipCode = hAcnoVipCode;
        prtData03.paymentNo = hAcnoPaymentNo;
        prtData03.liabStatus = hCcotLiabStatus;
        prtData03.oppostDate = hCardOppostDate;
        prtData03.liacStatus = hCcotLiacStatus;
        prtData03.renewStatus = hCcotRenewStatus;
        prtData03.liquStatus = hCcotLiquStatus;

        prtData03.liacEndReason = hCcotLiacEndReason;
        prtData03.liabDate = hCcotLiabDate;
        prtData03.renewDate = hCcotRenewDate;
        prtData03.liacDate = hCcotLiacDate;
        prtData03.linqDate = hCcotLiquDate;
        prtData03.eMailAddr1 = hIdnoEMailAddr;
        prtData03.eMailAddr2 = " ";
        prtData03.eMailAddr3 = " ";
        prtData03.statUnprintFlag = hAcnoStatUnprintFlag;
        prtData03.statUnprintSMonth = hAcnoStatUnprintSMonth;
        prtData03.statUnprintEMonth = hAcnoStatUnprintEMonth;
        prtData03.statSendPaper = hAcnoStatSendPaper;
        prtData03.statSendSMonth = hAcnoStatSendSMonth;
        prtData03.statSendEMonth = hAcnoStatSendEMonth;
        prtData03.statSendInternet = hAcnoStatSendInternet;
        prtData03.statSendSMonth2 = hAcnoStatSendSMonth2;
        prtData03.statSendEMonth2 = hAcnoStatSendEMonth2;
        prtData03.salaryCode = hIdnoSalaryCode;
        prtData03.salaryHoldinFlag = hIdnoSalaryHoldinFlag;
        prtData03.trialDate = hCcotTrialDate;
        prtData03.csmDate = hCcotCsmDate;
        prtData03.csmCasetype = hCcotCsmCasetype;
        prtData03.liaxStatus = " ";
        prtData03.liaxEndReason = " ";
        prtData03.liaxDate = " ";
        tmpstr = String.format("%03d", maxMcode);
        prtData03.maxMcode = tmpstr;

        tmpstr = prtData03.allText();
        lpar3.add(comcr.putReport("", "", sysDate, rptSeq3++, "0", tmpstr));

        reasonCnt[3]++;
        hCcotPSeqno = hAcnoPSeqno;
        hCcotCorpPSeqno = "";
        hCcotCorpNo = "";
        hCcotIdPSeqno = hAcnoIdPSeqno;
        hCcotId = hIdnoId;
        hCcotCardNo = "";
        hCcotEffectCardMark = hCardCurrentCode;
        hCcotMcode = hCcbsMcode;
        hCcotFormType = "3";
        insertColCsOut();
    }

    /***********************************************************************/
    void genALMON02() throws Exception {
        String tmpstr = "";

        prtData02.corpNo = hAcnoCorpNo;
        prtData02.id = hIdnoId;
        prtData02.chiName = hIdnoChiName;
        prtData02.birthday = hIdnoBirthday;
        prtData02.companyName = hIdnoCompanyName;
        prtData02.jobPosition = hIdnoJobPosition;
        prtData02.sex = hIdnoSex;
        prtData02.homeAreaCode1 = hIdnoHomeAreaCode1;
        prtData02.homeTelNo1 = hIdnoHomeTelNo1;
        prtData02.homeTelExt1 = hIdnoHomeTelExt1;
        prtData02.homeAreaCode2 = hIdnoHomeAreaCode2;
        prtData02.homeTelNo2 = hIdnoHomeTelNo2;
        prtData02.homeTelExt2 = hIdnoHomeTelExt2;
        prtData02.officeAreaCode1 = hIdnoOfficeAreaCode1;
        prtData02.officeTelNo1 = hIdnoOfficeTelNo1;
        prtData02.officeTelExt1 = hIdnoOfficeTelExt1;
        prtData02.officeAreaCode2 = hIdnoOfficeAreaCode2;
        prtData02.officeTelNo2 = hIdnoOfficeTelNo2;
        prtData02.officeTelExt2 = hIdnoOfficeTelExt2;
        prtData02.autopayAcctBank = hAcnoAutopayAcctBank;
        prtData02.autopayAcctNo = hAcnoAutopayAcctNo;
        prtData02.corpActFlag = hAcnoCorpActFlag;

        hAcnoBillSendingZip = comc.commBig5Asc(hAcnoBillSendingZip); /* sunny add */
        hTempAddr1 = comc.commBig5Asc(hTempAddr1); /* sunny add */

        prtData02.billSendingZip = hAcnoBillSendingZip;
        prtData02.addr1 = hTempAddr1;

        hIdnoResidentZip = comc.commBig5Asc(hIdnoResidentZip); /* sunny add */
        hTempAddr3 = comc.commBig5Asc(hTempAddr3); /* sunny add */

        prtData02.residentZip = hIdnoResidentZip;
        prtData02.addr3 = hTempAddr3;
        prtData02.acctType = hAcnoAcctType;
        prtData02.stmtCycle = hWdayStmtCycle;
        tmpstr = String.format("%10f", hAcnoLineOfCreditAmt);
        prtData02.lineOfCreditAmt = tmpstr;
        prtData02.stopStatus = hAcnoStopStatus;
        prtData02.blockDate = hAcnoBlockDate;
        prtData02.blockReason1 = hAcnoBlockReason;
//        prt_data02.block_reason2 = h_acno_block_reason2;
//        prt_data02.block_reason3 = h_acno_block_reason2.substring(2);
//        prt_data02.block_reason4 = h_acno_block_reason2.substring(4);
//        prt_data02.block_reason5 = h_acno_block_reason2.substring(6);
        prtData02.blockReason2 = String.format("%2.2s", hAcnoBlockReason2);
        prtData02.blockReason3 = String.format("%2.2s", comc.getSubString(hAcnoBlockReason2,2));
        prtData02.blockReason4 = String.format("%2.2s", comc.getSubString(hAcnoBlockReason2,4));
        prtData02.blockReason5 = String.format("%2.2s", comc.getSubString(hAcnoBlockReason2,6));
        prtData02.cardSince = hIdnoCardSince;
        prtData02.sourceCode = hCardSourceCode;
        prtData02.cellarPhone = hIdnoCellarPhone;
        prtData02.bbCall = hIdnoBbCall;
        tmpstr = String.format("%02d", hTempMcode);
        prtData02.mcode = tmpstr;
        prtData02.currentCode = hCardCurrentCode;
        prtData02.noPerCollFlag = hAcnoNoPerCollFlag;
        prtData02.noTelCollFlag = hAcnoNoTelCollFlag;
        prtData02.noUnblockFlag = hAcnoNoUnblockFlag;
        prtData02.noFStopFlag = hAcnoNoFStopFlag;
        prtData02.noAdjLoc = hAcnoNoAdjLocLow;
        prtData02.noTelCollFlagV = hAcnoNoTelCollFlag;
        tmpstr = String.format("%14.2f", hCcotPayAmt);
        prtData02.payAmt = tmpstr;
        prtData02.orgDelinquentDate = hAcnoOrgDelinquentDate;
        prtData02.bkFlag = hCcotBkFlag;
        prtData02.fhFlag = hCcotFhFlag;
        prtData02.vipCode = hAcnoVipCode;
        prtData02.paymentNo = hAcnoPaymentNo;
        prtData02.liabStatus = hCcotLiabStatus;
        prtData02.oppostDate = hCardOppostDate;
        prtData02.liacStatus = hCcotLiacStatus;
        prtData02.renewStatus = hCcotRenewStatus;
        prtData02.liqustatus = hCcotLiquStatus;

        prtData02.liacEndReason = hCcotLiacEndReason;
        prtData02.liabDate = hCcotLiabDate;
        prtData02.renewDate = hCcotRenewDate;
        prtData02.liacDate = hCcotLiacDate;
        prtData02.linqDate = hCcotLiquDate;
        prtData02.eMailAddr1 = hIdnoEMailAddr;
        prtData02.eMailAddr2 = " ";
        prtData02.emailaddr3 = " ";
        prtData02.statUnprintFlag = hAcnoStatUnprintFlag;
        prtData02.statUnprintSMonth = hAcnoStatUnprintSMonth;
        prtData02.statUnprintEMonth = hAcnoStatUnprintEMonth;
        prtData02.statSendPaper = hAcnoStatSendPaper;
        prtData02.statSendSMonth = hAcnoStatSendSMonth;
        prtData02.statSendEMonth = hAcnoStatSendEMonth;
        prtData02.statSendInternet = hAcnoStatSendInternet;
        prtData02.statSendSMonth2 = hAcnoStatSendSMonth2;
        prtData02.statSendEMonth2 = hAcnoStatSendEMonth2;
        prtData02.salaryCode = hIdnoSalaryCode;
        prtData02.salaryHoldinFlag = hIdnoSalaryHoldinFlag;
        prtData02.trialdate = hCcotTrialDate;
        prtData02.csmDate = hCcotCsmDate;
        prtData02.csmCasetype = hCcotCsmCasetype;
        prtData02.liaxStatus = " ";
        prtData02.liaxEndReason = " ";
        prtData02.liaxDate = " ";
        tmpstr = String.format("%03d", maxMcode);
        prtData02.maxMcode = tmpstr;

        tmpstr = prtData02.allText();
        lpar2.add(comcr.putReport("", "", sysDate, rptSeq2++, "0", tmpstr));

        if (!hAcnoPSeqno.equals(hAcnoAcctPSeqno))
            return;

        hCcotPSeqno = hAcnoPSeqno;
        hCcotCorpPSeqno = hCcbsCorpPSeqno;
        hCcotCorpNo = hCorpCorpNo;
        hCcotIdPSeqno = hAcnoIdPSeqno;
        hCcotId = hIdnoId;
        hCcotCardNo = "";
        hCcotEffectCardMark = hCardCurrentCode;
        hCcotMcode = hTempMcode;
        hCcotFormType = "2";
        insertColCsOut();
    }

    /***********************************************************************/
    void genALMON01() throws Exception {
        String tmpstr = "";
        prtData01.corpNo = hCorpCorpNo;
        tmpstr = String.format("%07d", comcr.str2long(hWdayThisCloseDate) - 19110000);
        prtData01.closeDate = tmpstr;
        prtData01.chiName = hCorpChiName;

        hCorpRegZip = comc.commBig5Asc(hCorpRegZip); /* sunny add */
        hTempAddr2 = comc.commBig5Asc(hTempAddr2); /* sunny add */

        prtData01.regZip = hCorpRegZip;
        prtData01.corpAddr2 = hTempAddr2;
        prtData01.idnoFlag = hTempIdnoFlag;
        prtData01.corpFlag = hTempCorpFlag;
        prtData01.stmtCycle = hWdayStmtCycle;
        prtData01.contactName = hCorpContactName;
        prtData01.acctType = hAcnoAcctType;

        hAcnoBillSendingZip = comc.commBig5Asc(hAcnoBillSendingZip); /* sunny add */
        hTempAddr1 = comc.commBig5Asc(hTempAddr1); /* sunny add */

        prtData01.billSendingZip = hAcnoBillSendingZip;
        prtData01.corpAddr1 = hTempAddr1;
        prtData01.corpTelZone1 = hCorpCorpTelZone1;
        prtData01.corpTelNo1 = hCorpCorpTelNo1;
        prtData01.corpTelExt1 = hCorpCorpTelExt1;
        prtData01.corpTelZone2 = hCorpCorpTelZone2;
        prtData01.corpTelNo2 = hCorpCorpTelNo2;
        prtData01.corpTelExt2 = hCorpCorpTelExt2;
        prtData01.autopayAcctBank = hAcnoAutopayAcctBank;
        prtData01.autopayAcctNo = hAcnoAutopayAcctNo;
        tmpstr = String.format("%10f", hAcnoLineOfCreditAmt);
        prtData01.lineOfCreditAmt = tmpstr;
        prtData01.currentCode = hCardCurrentCode;
        tmpstr = String.format("%02d", hTempMcode);
        prtData01.mcode = tmpstr;
        prtData01.chargeName = hCorpChargeName;
        tmpstr = String.format("%14.2f", hCcotPayAmt);
        prtData01.payAmt = tmpstr;
        prtData01.orgDelinquentDate = hAcnoOrgDelinquentDate;
        prtData01.vipCode = hAcnoVipCode;
        prtData01.paymentNo = hAcnoPaymentNo;
        prtData01.eMailAddr1 = hCorpEMailAddr;
        prtData01.eMailAddr2 = " ";
        prtData01.eMailAddr3 = " ";
        prtData01.statUnprintFlag = hAcnoStatUnprintFlag;
        prtData01.statUnprintSMonth = hAcnoStatUnprintSMonth;
        prtData01.statUnprintEMonth = hAcnoStatUnprintEMonth;
        prtData01.statSendPaper = hAcnoStatSendPaper;
        prtData01.statSendSMonth = hAcnoStatSendSMonth;
        prtData01.statSendEMonth = hAcnoStatSendEMonth;
        prtData01.statSendInternet = hAcnoStatSendInternet;
        prtData01.statSendSMonth2 = hAcnoStatSendSMonth2;
        prtData01.statSendEMonth2 = hAcnoStatSendEMonth2;
        tmpstr = String.format("%03d", maxMcode);
        prtData01.maxMcode = tmpstr;

        tmpstr = prtData01.allText();
        lpar1.add(comcr.putReport("", "", sysDate, rptSeq1++, "0", tmpstr));

        reasonCnt[1]++;
        hCcotPSeqno = hAcnoPSeqno;
        hCcotCorpPSeqno = hCcbsCorpPSeqno;
        hCcotCorpNo = hCorpCorpNo;
        hCcotIdPSeqno = "";
        hCcotId = "";
        hCcotCardNo = "";
        hCcotEffectCardMark = hCardCurrentCode;
        hCcotMcode = hTempMcode;
        hCcotFormType = "1";
        insertColCsOut();
    }

    /***********************************************************************/
    void insertColCsOut() throws Exception {
    	daoTable = "col_cs_out";
    	extendField = daoTable + ".";
        setValue(extendField+"stmt_cycle", hWdayStmtCycle);
        setValue(extendField+"acct_type", hCcbsAcctType);
        setValue(extendField+"p_seqno", hCcotPSeqno);
        setValue(extendField+"corp_p_seqno", hCcotCorpPSeqno);
        setValue(extendField+"corp_no", hCcotCorpNo);
        setValue(extendField+"corp_on_flag", hCcotCorpOnFlag);
        setValue(extendField+"id_p_seqno", hCcotIdPSeqno);
        setValue(extendField+"id_no", hCcotId);
        setValue(extendField+"card_no", hCcotCardNo);
        setValue(extendField+"form_type", hCcotFormType);
        setValueInt(extendField+"mcode", hCcotMcode);
        setValue(extendField+"acct_month", hWdayThisAcctMonth);
        setValue(extendField+"create_month", hWdayThisAcctMonth);
        setValue(extendField+"liab_status", hCcotLiabStatus);
        setValue(extendField+"liab_date", hCcotLiabDate);
        setValue(extendField+"liac_status", hCcotLiacStatus);
        setValue(extendField+"liac_date", hCcotLiacDate);
        setValue(extendField+"liac_end_reason", hCcotLiacEndReason);
        setValue(extendField+"renew_status", hCcotRenewStatus);
        setValue(extendField+"renew_date", hCcotRenewDate);
        setValue(extendField+"liqu_status", hCcotLiquStatus);
        setValue(extendField+"liqu_date", hCcotLiquDate);
        setValue(extendField+"trial_date", hCcotTrialDate);
        setValue(extendField+"bk_flag", hCcotBkFlag);
        setValue(extendField+"fh_flag", hCcotFhFlag);
        setValue(extendField+"csm_date", hCcotCsmDate);
        setValue(extendField+"csm_casetype", hCcotCsmCasetype);
        setValue(extendField+"dc_curr_flag", hCcotDcCurrFlag);
        setValue(extendField+"last_d_date", hCcotLastDDate);
        setValueDouble(extendField+"stmt_over_due_amt", hCcotStmtOverDueAmt);
        setValueDouble(extendField+"ttl_amt_bal", hCcotTtlAmtBal);
        setValue(extendField+"mod_time", sysDate + sysTime);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            updateColCsOut();
        }
        return;
    }

    /***********************************************************************/
    void updateColCsOut() throws Exception {
        daoTable = "col_cs_out";
        updateSQL = "mcode    = ?,";
        updateSQL += " corp_on_flag  = decode(form_type,'1',nvl(?,'N'),''),";
        updateSQL += " acct_month   = ?,";
        updateSQL += " liab_status  = ?,";
        updateSQL += " liab_date   = decode(liab_status,?,liab_date,?),";
        updateSQL += " liac_status  = ?,";
        updateSQL += " liac_date   = decode(liac_status,?,liac_date,?),";
        updateSQL += " liac_end_reason = ?,";
        updateSQL += " renew_status  = ?,";
        updateSQL += " renew_date   = decode(renew_status,?,renew_date,?),";
        updateSQL += " liqu_status  = ?,";
        updateSQL += " liqu_date   = decode(liqu_status,?,liqu_date,?),";
        updateSQL += " trial_date   = ?,";
        updateSQL += " bk_flag   = ?,";
        updateSQL += " fh_flag   = ?,";
        updateSQL += " csm_date   = ?,";
        updateSQL += " csm_casetype  = ?,";
        updateSQL += " dc_curr_flag  = ?,";
        updateSQL += " last_d_date  = ?,";
        updateSQL += " stmt_over_due_amt = ?,";
        updateSQL += " ttl_amt_bal  = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where p_seqno    = ? ";
        setInt(1, hCcotMcode);
        setString(2, hCcotCorpOnFlag);
        setString(3, hWdayThisAcctMonth);
        setString(4, hCcotLiabStatus);
        setString(5, hCcotLiabStatus);
        setString(6, hBusiBusinessDate);
        setString(7, hCcotLiacStatus);
        setString(8, hCcotLiacStatus);
        setString(9, hBusiBusinessDate);
        setString(10, hCcotLiacEndReason);
        setString(11, hCcotRenewStatus);
        setString(12, hCcotRenewStatus);
        setString(13, hBusiBusinessDate);
        setString(14, hCcotLiquStatus);
        setString(15, hCcotLiquStatus);
        setString(16, hBusiBusinessDate);
        setString(17, hCcotTrialDate);
        setString(18, hCcotBkFlag);
        setString(19, hCcotFhFlag);
        setString(20, hCcotCsmDate);
        setString(21, hCcotCsmCasetype);
        setString(22, hCcotDcCurrFlag);
        setString(23, hCcotLastDDate);
        setDouble(24, hCcotStmtOverDueAmt);
        setDouble(25, hCcotTtlAmtBal);
        setString(26, hCcotPSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_cs_out not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectColCsBase2() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "mcode ";
        sqlCmd += "from col_cs_base ";
        sqlCmd += "where stmt_cycle = ? ";
        sqlCmd += "and id_p_seqno <> '' ";
        sqlCmd += "and decode(card_indicator,'','X',card_indicator) = '2' ";
        setString(1, hWdayStmtCycle);

        openCursor();
        while (fetchTable()) {
            initData();
            hCcbsIdPSeqno = getValue("id_p_seqno");
            hCcbsPSeqno = getValue("p_seqno");
            hCcbsAcctType = getValue("acct_type");
            hCcbsMcode = getValueInt("mcode");
            hAcnoCorpNo = "";

            totalCnt++;
            if (totalCnt % 2000 == 0)
                showLogMessage("I", "", String.format("    目前處理筆數 [%d]", totalCnt));

            selectActAcno3();
            selectCrdCard1();
            if (!hCardCurrentCode.equals("Y")) {
                selectRskAcnolog1();
            } else {
                hAcnoBlockDate = "";
            }

            hTempIdPSeqno = hAcnoIdPSeqno;
            selectCrdIdno();
            selectCrdCorrelate();

            selectActAcag();
//            selectColLiabNego();
//            if (selectColLiacNego() != 0)
//                selectColLiacNegoHst();
//            selectColLiadRenew();
//            selectColLiadLiquidate();
            selectActAcctHst();

            reasonCnt[2]++;

            genALMON02();
            selectCrdRela(); /* 保證人 */
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcno3() throws Exception {
        hAcnoPSeqno = "";
        hAcnoAcctPSeqno = "";
        hAcnoAcctHolderId = "";
        hAcnoAcctType = "";
        hAcnoAcctKey = "";
        hAcnoCorpPSeqno = "";
        hAcnoCorpNo = "";
        hAcnoAcctStatus = "";
        hAcnoIdPSeqno = "";
        hAcnoCreditActNo = "";
        hAcnoBillSendingZip = "";
        hTempAddr1 = "";
        hAcnoAutopayAcctBank = "";
        hAcnoAutopayAcctNo = "";
        hAcnoStopStatus = "";
        hAcnoBlockDate = "";
        hAcnoOrgDelinquentDate = "";
        hAcnoBlockReason = "";
        hAcnoBlockReason2 = "";
        hAcnoCreateDate = "";
        hAcnoNoTelCollFlag = "";
        hAcnoNoPerCollFlag = "";
        hAcnoNoUnblockFlag = "";
        hAcnoNoFStopFlag = "";
        hAcnoNoAdjLocLow = "";
        hAcnoRcUseIndicator = "";
        hAcnoLineOfCreditAmt = 0;
        hAcnoCardIndicator = "";
        hAcnoVipCode = "";
        hAcnoPaymentNo = "";
        hAcnoCorpActFlag = "";
        
//        sqlCmd = "select a.p_seqno,";
//        sqlCmd += "a.gp_no acct_p_seqno,";
        sqlCmd = "select a.acno_p_seqno,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "c.id_no||c.id_no_code acct_holder_id,"; // acct_holder_id||acct_holder_id_code
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "d.corp_no,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.credit_act_no,";
        sqlCmd += "a.bill_sending_zip,";
        sqlCmd += "trim(substrb(bill_sending_addr1||bill_sending_addr2||bill_sending_addr3 ||bill_sending_addr4||bill_sending_addr5,1,70)) h_temp_addr1,";
        sqlCmd += "a.autopay_acct_bank,";
        sqlCmd += "a.autopay_acct_no,";
        sqlCmd += "a.stop_status,";
        sqlCmd += "e.block_date,";
        sqlCmd += "a.org_delinquent_date,";
        sqlCmd += "e.block_reason1 block_reason,";
        sqlCmd += "e.block_reason2||e.block_reason3||e.block_reason4||e.block_reason5 block_reason2,";
        sqlCmd += "a.crt_date,";
        sqlCmd += "a.no_tel_coll_flag,";
        sqlCmd += "a.no_per_coll_flag,";
        sqlCmd += "a.no_unblock_flag,";
        sqlCmd += "a.no_f_stop_flag,";
        sqlCmd += "decode(no_adj_loc_low,'Y','Y', decode(no_adj_loc_high,'Y','Y','')) h_acno_no_adj_loc_low,";
        sqlCmd += "a.rc_use_indicator,";
        sqlCmd += "a.line_of_credit_amt,";
        sqlCmd += "a.card_indicator,";
        sqlCmd += "decode(vip_code,'6S','6S','WW','5S','5S','5S', 'V0','4S','V1','4S','V2','4S','V3','4S','V4','4S', 'V5','4S','V6','4S','V7','4S','V8','4S','4S','4S',' ') h_acno_vip_code,";
        sqlCmd += "a.payment_no,";
        sqlCmd += "a.corp_act_flag ";
        sqlCmd += "from act_acno a ";
        sqlCmd += "  left join crd_idno c on a.id_p_seqno = c.id_p_seqno ";
        sqlCmd += "  left join crd_corp d on a.corp_p_seqno = d.corp_p_seqno ";
//        sqlCmd += "  left join cca_card_acct e on a.p_seqno = e.p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";
//        sqlCmd += "where a.p_seqno = ? ";
        sqlCmd += "  left join cca_card_acct e on a.acno_p_seqno = e.acno_p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";
        sqlCmd += "where a.acno_p_seqno = ? ";
        setString(1, hCcbsPSeqno);
        
        extendField = "act_acno_3.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	showLogMessage("I", "", "h_ccbs_p_seqno="+ hCcbsPSeqno);
            comcr.errRtn("select_act_acno_3 not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
//            h_acno_p_seqno = getValue("p_seqno");
//            h_acno_acct_p_seqno = getValue("acct_p_seqno");
            hAcnoPSeqno = getValue("act_acno_3.acno_p_seqno");
            hAcnoAcctPSeqno = getValue("act_acno_3.p_seqno");
            hAcnoAcctHolderId = getValue("act_acno_3.acct_holder_id");
            hAcnoAcctType = getValue("act_acno_3.acct_type");
            hAcnoAcctKey = getValue("act_acno_3.acct_key");
            hAcnoCorpPSeqno = getValue("act_acno_3.corp_p_seqno");
            hAcnoCorpNo = getValue("act_acno_3.corp_no");
            hAcnoAcctStatus = getValue("act_acno_3.acct_status");
            hAcnoIdPSeqno = getValue("act_acno_3.id_p_seqno");
            hAcnoCreditActNo = getValue("act_acno_3.credit_act_no");
            hAcnoBillSendingZip = getValue("act_acno_3.bill_sending_zip");
            hTempAddr1 = getValue("act_acno_3.h_temp_addr1");
            hAcnoAutopayAcctBank = getValue("act_acno_3.autopay_acct_bank");
            hAcnoAutopayAcctNo = getValue("act_acno_3.autopay_acct_no");
            hAcnoStopStatus = getValue("act_acno_3.stop_status");
            hAcnoBlockDate = getValue("act_acno_3.block_date");
            hAcnoOrgDelinquentDate = getValue("act_acno_3.org_delinquent_date");
            hAcnoBlockReason = getValue("act_acno_3.block_reason");
            hAcnoBlockReason2 = getValue("act_acno_3.block_reason2");
            hAcnoCreateDate = getValue("act_acno_3.crt_date");
            hAcnoNoTelCollFlag = getValue("act_acno_3.no_tel_coll_flag");
            hAcnoNoPerCollFlag = getValue("act_acno_3.no_per_coll_flag");
            hAcnoNoUnblockFlag = getValue("act_acno_3.no_unblock_flag");
            hAcnoNoFStopFlag = getValue("act_acno_3.no_f_stop_flag");
            hAcnoNoAdjLocLow = getValue("act_acno_3.h_acno_no_adj_loc_low");
            hAcnoRcUseIndicator = getValue("act_acno_3.rc_use_indicator");
            hAcnoLineOfCreditAmt = getValueDouble("act_acno_3.line_of_credit_amt");
            hAcnoCardIndicator = getValue("act_acno_3.card_indicator");
            hAcnoVipCode = getValue("act_acno_3.h_acno_vip_code");
            hAcnoPaymentNo = getValue("act_acno_3.payment_no");
            hAcnoCorpActFlag = getValue("act_acno_3.corp_act_flag");
        }
    }

    /***********************************************************************/
    void selectCrdCard1() throws Exception {
        hCardSourceCode = "";
        hCardGroupCode = "";
        hCardOppostDate = "";
        hCardCurrentCode = "";

        sqlCmd = "select max(source_code) h_card_source_code,";
        sqlCmd += "max(group_code) h_card_group_code,";
        sqlCmd += "max(oppost_date) h_card_oppost_date,";
        sqlCmd += "max(decode(current_code,'0','Y','')) h_card_current_code ";
        sqlCmd += " from crd_card  ";
//        sqlCmd += "where gp_no = ? ";
        sqlCmd += "where p_seqno = ? ";
        setString(1, hAcnoAcctPSeqno);
        
        extendField = "crd_card_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCardSourceCode = getValue("crd_card_1.h_card_source_code");
            hCardGroupCode = getValue("crd_card_1.h_card_group_code");
            hCardOppostDate = getValue("crd_card_1.h_card_oppost_date");
            hCardCurrentCode = getValue("crd_card_1.h_card_current_code");
        }
    }

    /***********************************************************************/
    void selectRskAcnolog1() throws Exception {
        List<Integer> blockInt = new ArrayList<Integer>();
        int inti = 0;
        List<String> aAclgLogDate = new ArrayList<String>();
        sqlCmd = "select ";
        sqlCmd += "distinct log_date ";
        sqlCmd += "from rsk_acnolog ";
//        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "where acno_p_seqno = ? ";
        sqlCmd += "and decode(log_date ,'','x',log_date ) <= decode(cast(? as varchar(8)),'','00000000',cast(? as varchar(8))) ";
        sqlCmd += "and decode(log_type ,'','x',log_type ) in ('3','4','5') ";
        sqlCmd += "and decode(kind_flag,'','x',kind_flag) in ('A','C') ";
        sqlCmd += "and log_not_reason = '' ";
        sqlCmd += "order by log_date desc ";
        setString(1, hAcnoAcctPSeqno);
        setString(2, hCardOppostDate);
        setString(3, hCardOppostDate);

        extendField = "rsk_acnolog_1.";
        
        int recordCnt = selectTable();
        if (recordCnt==0) return;  //phopho mod
        for (int i = 0; i < recordCnt; i++) {
            aAclgLogDate.add(getValue("rsk_acnolog_1.log_date", i));
        }

        for (inti = 0; inti < recordCnt; inti++) {
            hAclgLogDate = aAclgLogDate.get(inti);
            blockInt.add(inti, selectRskAcnolog2());
        }
        for (inti = 0; inti < recordCnt; inti++)
            if (blockInt.get(inti) == 0)
                break;

        if (inti >= recordCnt) {
            hAcnoBlockDate = aAclgLogDate.get(recordCnt - 1);
        } else {
            if (inti == 0) {
                hAcnoBlockDate = "";
            } else {
                hAcnoBlockDate = aAclgLogDate.get(inti - 1);
            }
        }
    }

    /***********************************************************************/
    int selectRskAcnolog2() throws Exception {
        int hTemp1Cnt = 0;
        int hTemp2Cnt = 0;

        sqlCmd = "select decode(trim(block_reason)||trim(block_reason2)|| trim(block_reason3)||trim(block_reason4)||trim(block_reason5),'61',0,1) h_temp_1_cnt,";
        sqlCmd += "decode(trim(block_reason)||trim(block_reason2)|| trim(block_reason3)||trim(block_reason4)||trim(block_reason5),'',0,1) h_temp_2_cnt ";
        sqlCmd += " from rsk_acnolog  ";
//        sqlCmd += "where p_seqno = ?  "; //acct_seqno
        sqlCmd += "where acno_p_seqno = ? ";
        sqlCmd += "and decode(log_date ,'','x', log_date)    = ? ";
        sqlCmd += "and decode(log_date ,'','x',log_date ) <= decode(cast(? as varchar(8)),'','00000000', ?) ";
        sqlCmd += "and decode(log_type ,'','x',log_type ) in ('3','4','5') ";
        sqlCmd += "and decode(kind_flag,'','x',kind_flag) in ('A','C') ";
        sqlCmd += "and log_not_reason = '' order by mod_time desc,decode(trim(block_reason)||trim(block_reason2)|| trim(block_reason3)||trim(block_reason4)||trim(block_reason5),'',0,'61',0,1) desc,kind_flag asc ";
        setString(1, hAcnoAcctPSeqno);
        setString(2, hAclgLogDate);
        setString(3, hCardOppostDate);
        setString(4, hCardOppostDate);
        
        extendField = "rsk_acnolog_2.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTemp1Cnt = getValueInt("rsk_acnolog_2.h_temp_1_cnt");
            hTemp2Cnt = getValueInt("rsk_acnolog_2.h_temp_2_cnt");
        }

        if (hTemp1Cnt == 1 && hTemp2Cnt == 1)
            return 1;
        return 0;
    }

    /***********************************************************************/
    void selectCrdCorrelate() throws Exception {
        hCcotBkFlag = "";
        hCcotFhFlag = "";
        sqlCmd = "select b.bk_flag,";
        sqlCmd += "b.fh_flag ";
        sqlCmd += " from crd_correlate b  ";
        sqlCmd += "where b.correlate_id = substr(?,1,10) ";
        setString(1, hIdnoId);
        
        extendField = "crd_correlate.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcotBkFlag = getValue("crd_correlate.bk_flag");
            hCcotFhFlag = getValue("crd_correlate.fh_flag");
        }
    }

    /***********************************************************************/
    void selectActAcag() throws Exception {
        double minAmount = 0;
        int inti = 0;
        hCcotPayAmt = 0;
        maxMcode = 0;

        List<String> aAcagAcctMonth = new ArrayList<String>();
        List<Integer> aTempMcode = new ArrayList<Integer>();
        List<Double> aAcagPayAmt = new ArrayList<Double>();

        sqlCmd = "select ";
        sqlCmd += "acct_month,";
        sqlCmd += "max(months_between(to_date(?,'yyyymm'),to_date(acct_month,'yyyymm'))) h_temp_mcode,";
        sqlCmd += "sum(pay_amt) h_acag_pay_amt ";
        sqlCmd += "from act_acag ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "group by acct_month ";
        sqlCmd += "order by acct_month ";
        setString(1, hWdayThisAcctMonth);
        setString(2, hAcnoPSeqno);
        
        extendField = "act_acag.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aAcagAcctMonth.add(getValue("act_acag.acct_month", i));
            aTempMcode.add(getValueInt("act_acag.h_temp_mcode", i));
            aAcagPayAmt.add(getValueDouble("act_acag.h_acag_pay_amt", i));
        }
        if (recordCnt == 0)
            return;

        for (inti = 0; inti < recordCnt; inti++)
            if (aAcagPayAmt.get(inti) != 0) {
                hCcotPayAmt = aAcagPayAmt.get(inti);
                break;
            }

        for (inti = 0; inti < recordCnt; inti++) {
            minAmount = minAmount + aAcagPayAmt.get(inti);
            if (minAmount > hAgenMixMpBalance)
                break;
        }
        if (inti >= recordCnt)
            return;

        maxMcode = aTempMcode.get(inti);
    }

    /***********************************************************************/
    void selectActAcctHst() throws Exception {
        hCcotStmtOverDueAmt = 0;
        hCcotTtlAmtBal = 0;
        sqlCmd = "select stmt_over_due_amt,";
        sqlCmd += "ttl_amt_bal ";
        sqlCmd += " from act_acct_hst  ";
        sqlCmd += "where acct_month = ?  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hWdayLastAcctMonth);
        setString(2, hAcnoAcctPSeqno);
        
        extendField = "act_acct_hst.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcotStmtOverDueAmt = getValueDouble("act_acct_hst.stmt_over_due_amt");
            hCcotTtlAmtBal = getValueDouble("act_acct_hst.ttl_amt_bal");
        }
    }

    /***********************************************************************/
    void selectCrdRela() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "UF_IDNO_ID(id_p_seqno) id,";
        sqlCmd += "rela_id,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "trim(substrb(rela_name,1,20)) h_rela_rela_name,";
        sqlCmd += "company_name,";
        sqlCmd += "company_zip,";
        sqlCmd += "trim(substrb(company_addr1||company_addr2||company_addr3||company_addr4||company_addr5,1,70)) h_temp_addr4,";
        sqlCmd += "office_area_code1,";
        sqlCmd += "office_tel_no1,";
        sqlCmd += "office_tel_ext1,";
        sqlCmd += "office_area_code2,";
        sqlCmd += "office_tel_no2,";
        sqlCmd += "office_tel_ext2,";
        sqlCmd += "home_area_code1,";
        sqlCmd += "home_tel_no1,";
        sqlCmd += "home_tel_ext1,";
        sqlCmd += "home_area_code2,";
        sqlCmd += "home_tel_no2,";
        sqlCmd += "home_tel_ext2,";
        sqlCmd += "resident_zip,";
        sqlCmd += "trim(substrb(resident_addr1||resident_addr2||resident_addr3||resident_addr4||resident_addr5,1,70)) h_temp_addr5,";
        sqlCmd += "cellar_phone,";
        // sqlCmd += "bb_call,";
        sqlCmd += "rela_seqno,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from crd_rela ";
        sqlCmd += "where rela_type = '1' ";
        sqlCmd += "and id_p_seqno = ? ";
        sqlCmd += "and acct_type = ? ";
        sqlCmd += "and (rela_name <> '' ";
        sqlCmd += "or rela_id <> '' ) ";
        setString(1, hTempIdPSeqno);
        setString(2, hCcbsAcctType);

        extendField = "crd_rela.";
        
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            initData();
            hRelaCardNo = getValue("crd_rela.card_no", i);
            hRelaId = getValue("crd_rela.id", i);
            hRelaRelaId = getValue("crd_rela.rela_id", i);
            hRelaIdPSeqno = getValue("crd_rela.id_p_seqno", i);
            hRelaRelaName = getValue("crd_rela.h_rela_rela_name", i);
            hRelaCompanyName = getValue("crd_rela.company_name", i);
            hRelaCompanyZip = getValue("crd_rela.company_zip", i);
            hTempAddr4 = getValue("crd_rela.h_temp_addr4", i);
            hRelaOfficeAreaCode1 = getValue("crd_rela.office_area_code1", i);
            hRelaOfficeTelNo1 = getValue("crd_rela.office_tel_no1", i);
            hRelaOfficeTelExt1 = getValue("crd_rela.office_tel_ext1", i);
            hRelaOfficeAreaCode2 = getValue("crd_rela.office_area_code2", i);
            hRelaOfficeTelNo2 = getValue("crd_rela.office_tel_no2", i);
            hRelaOfficeTelExt2 = getValue("crd_rela.office_tel_ext2", i);
            hRelaHomeAreaCode1 = getValue("crd_rela.home_area_code1", i);
            hRelaHomeTelNo1 = getValue("crd_rela.home_tel_no1", i);
            hRelaHomeTelExt1 = getValue("crd_rela.home_tel_ext1", i);
            hRelaHomeAreaCode2 = getValue("crd_rela.home_area_code2", i);
            hRelaHomeTelNo2 = getValue("crd_rela.home_tel_no2", i);
            hRelaHomeTelExt2 = getValue("crd_rela.home_tel_ext2", i);
            hRelaResidentZip = getValue("crd_rela.resident_zip", i);
            hTempAddr5 = getValue("crd_rela.h_temp_addr5", i);
            hRelaCellarPhone = getValue("crd_rela.cellar_phone", i);
            // h_rela_bb_call = getValue("bb_call", i);
            hRelaRelaSeqno = getValue("crd_rela.rela_seqno", i);
            hRelaRowid = getValue("crd_rela.rowid", i);

            if (hRelaRelaSeqno.length() == 0) {
                hRelaRelaSeqno = String.format("%10d", comcr.getModSeq());
                updateCrdRela();
            }

            genALMON05(); /* 保證人 */
        }
    }

    /***********************************************************************/
    void updateCrdRela() throws Exception {
        daoTable = "crd_rela";
        updateSQL = "rela_seqno = ?";
        whereStr = "where rowid = ? ";
        setString(1, hRelaRelaSeqno);
        setRowId(2, hRelaRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_rela not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectColCsBase3() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "mcode ";
        sqlCmd += "from col_cs_base ";
        sqlCmd += "where stmt_cycle = ? ";
        sqlCmd += "and id_p_seqno <> '' ";
        sqlCmd += "and decode(card_indicator,'','X',card_indicator) != '2' ";
        setString(1, hWdayStmtCycle);

        openCursor();
        while (fetchTable()) {
            initData();
            hCcbsIdPSeqno = getValue("id_p_seqno");
            hCcbsPSeqno = getValue("p_seqno");
            hCcbsAcctType = getValue("acct_type");
            hCcbsMcode = getValueInt("mcode");
            hAcnoCorpNo = "";

            totalCnt++;
            if (totalCnt % 2000 == 0)
                showLogMessage("I", "", String.format("    目前處理筆數 [%d]", totalCnt));

            selectActAcno3();
            selectCrdCard1();
            if (!hCardCurrentCode.equals("Y")) {
                selectRskAcnolog1();
            } else {
                hAcnoBlockDate = "";
            }

            hTempIdPSeqno = hAcnoIdPSeqno;
            selectCrdIdno();
            selectCrdCorrelate();

            selectActAcag();
//            selectColLiabNego();
            if (selectColLiacNego() != 0)
                selectColLiacNegoHst();
//            selectColLiadRenew();
//            selectColLiadLiquidate();
            selectActAcctHst();
            selectActAcctCurr();
//            selectRskTrialIdno();

            genALMON03(); /* 一般卡 */
            selectCrdRela(); /* 保證人 */

            hTempBirthday = hIdnoBirthday;
            selectCrdCard(); /* 附卡 */
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        sqlCmd = "select 1 h_cnt ";
        sqlCmd += " from act_acct_curr  ";
        sqlCmd += "where curr_code != '901'  ";
        sqlCmd += "and p_seqno = ?  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acct_curr.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCnt = getValueInt("act_acct_curr.h_cnt");
            hCcotDcCurrFlag = "Y";
        } else {
            hCcotDcCurrFlag = "N";
        }
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_p_seqno ";
        sqlCmd += "from crd_card ";
//        sqlCmd += "where gp_no = ? ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and id_p_seqno != major_id_p_seqno ";
        sqlCmd += "group by id_p_seqno ";
        setString(1, hCcbsPSeqno);
        
        extendField = "crd_card.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            initData();
            hCardIdPSeqno = getValue("crd_card.id_p_seqno", i);

            selectCrdIdno();
//            selectCmsCasemaster();

            genALMON04(); /* 附卡 */
        }
    }

    /***********************************************************************/
    void closeALMONFiles() throws Exception {
        String tmp1 = String.format("%15.15s%8.8s%06d", "TRAILER : END  ", hTempBusinessDate, reasonCnt[1]);
        String tmp2 = String.format("%15.15s%8.8s%06d", "TRAILER : END  ", hTempBusinessDate, reasonCnt[2] + reasonCnt[7]);
        String tmp3 = String.format("%15.15s%8.8s%06d", "TRAILER : END  ", hTempBusinessDate, reasonCnt[3]);
        String tmp4 = String.format("%15.15s%8.8s%06d", "TRAILER : END  ", hTempBusinessDate, reasonCnt[4]);
        String tmp5 = String.format("%15.15s%8.8s%06d", "TRAILER : END  ", hTempBusinessDate, reasonCnt[5]);

        lpar1.add(comcr.putReport("", "", sysDate, rptSeq1++, "0", tmp1));
        lpar2.add(comcr.putReport("", "", sysDate, rptSeq2++, "0", tmp2));
        lpar3.add(comcr.putReport("", "", sysDate, rptSeq3++, "0", tmp3));
        lpar4.add(comcr.putReport("", "", sysDate, rptSeq4++, "0", tmp4));
        lpar5.add(comcr.putReport("", "", sysDate, rptSeq5++, "0", tmp5));

        tmp1 = String.format("%15.15s%8.8s%06d", "HEADER : START  ", hTempBusinessDate, reasonCnt[1]);
        tmp2 = String.format("%15.15s%8.8s%06d", "HEADER : START  ", hTempBusinessDate, reasonCnt[2] + reasonCnt[7]);
        tmp3 = String.format("%15.15s%8.8s%06d", "HEADER : START  ", hTempBusinessDate, reasonCnt[3]);
        tmp4 = String.format("%15.15s%8.8s%06d", "HEADER : START  ", hTempBusinessDate, reasonCnt[4]);
        tmp5 = String.format("%15.15s%8.8s%06d", "HEADER : START  ", hTempBusinessDate, reasonCnt[5]);

        lpar1.set(0, comcr.putReport("", "", sysDate, rptSeq1++, "0", tmp1));
        lpar2.set(0, comcr.putReport("", "", sysDate, rptSeq2++, "0", tmp2));
        lpar3.set(0, comcr.putReport("", "", sysDate, rptSeq3++, "0", tmp3));
        lpar4.set(0, comcr.putReport("", "", sysDate, rptSeq4++, "0", tmp4));
        lpar5.set(0, comcr.putReport("", "", sysDate, rptSeq5++, "0", tmp5));

        comc.writeReport(temstr1, lpar1);
        comc.writeReport(temstr2, lpar2);
        comc.writeReport(temstr3, lpar3);
        comc.writeReport(temstr4, lpar4);
        comc.writeReport(temstr5, lpar5);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC001 proc = new ColC001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class PrtBuf01 {
        String corpNo;
        String closeDate;
        String chiName;
        String regZip;
        String corpAddr2;
        String idnoFlag;
        String corpFlag;
        String stmtCycle;
        String contactName;
        String acctType;
        String billSendingZip;
        String corpAddr1;
        String corpTelZone1;
        String corpTelNo1;
        String corpTelExt1;
        String corpTelZone2;
        String corpTelNo2;
        String corpTelExt2;
        String autopayAcctBank;
        String autopayAcctNo;
        String lineOfCreditAmt;
        String currentCode;
        String mcode;
        String chargeName;
        String payAmt;
        String orgDelinquentDate;
        String vipCode;
        String paymentNo;
        String eMailAddr1;
        String eMailAddr2;
        String eMailAddr3;
        String statUnprintFlag;
        String statUnprintSMonth;
        String statUnprintEMonth;
        String statSendPaper;
        String statSendSMonth;
        String statSendEMonth;
        String statSendInternet;
        String statSendSMonth2;
        String statSendEMonth2;
        String maxMcode;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
//            rtn += fixRight(corp_no, 9);
//            rtn += fixRight(close_date, 8);
//            rtn += fixRight(chi_name, 41);
//            rtn += fixRight(reg_zip, 6);
//            rtn += fixRight(corp_addr2, 71);
//            rtn += fixRight(idno_flag, 2);
//            rtn += fixRight(corp_flag, 2);
//            rtn += fixRight(stmt_cycle, 3);
//            rtn += fixRight(contact_name, 41);
//            rtn += fixRight(acct_type, 3);
//            rtn += fixRight(bill_sending_zip, 6);
//            rtn += fixRight(corp_addr1, 71);
//            rtn += fixRight(corp_tel_zone1, 5);
//            rtn += fixRight(corp_tel_no1, 9);
//            rtn += fixRight(corp_tel_ext1, 7);
//            rtn += fixRight(corp_tel_zone2, 5);
//            rtn += fixRight(corp_tel_no2, 9);
//            rtn += fixRight(corp_tel_ext2, 7);
//            rtn += fixRight(autopay_acct_bank, 4);
//            rtn += fixRight(autopay_acct_no, 15);
//            rtn += fixRight(line_of_credit_amt, 11);
//            rtn += fixRight(current_code, 2);
//            rtn += fixRight(mcode, 3);
//            rtn += fixRight(charge_name, 31);
//            rtn += fixRight(pay_amt, 15);
//            rtn += fixRight(org_delinquent_date, 9);
//            rtn += fixRight(vip_code, 3);
//            rtn += fixRight(payment_no, 16);
//            rtn += fixRight(e_mail_addr1, 51);
//            rtn += fixRight(e_mail_addr2, 51);
//            rtn += fixRight(e_mail_addr3, 51);
//            rtn += fixRight(stat_unprint_flag, 2);
//            rtn += fixRight(stat_unprint_s_month, 7);
//            rtn += fixRight(stat_unprint_e_month, 7);
//            rtn += fixRight(stat_send_paper, 2);
//            rtn += fixRight(stat_send_s_month, 7);
//            rtn += fixRight(stat_send_e_month, 7);
//            rtn += fixRight(stat_send_internet, 2);
//            rtn += fixRight(stat_send_s_month2, 7);
//            rtn += fixRight(stat_send_e_month2, 7);
//            rtn += fixRight(max_mcode, 4);
            
            rtn += fixLeft(corpNo, 8)+"^";
            rtn += fixLeft(closeDate, 7)+"^";
            rtn += fixLeft(chiName, 40)+"^";
            rtn += fixLeft(regZip, 5)+"^";
            rtn += fixLeft(corpAddr2, 70)+"^";
            rtn += fixLeft(idnoFlag, 1)+"^";
            rtn += fixLeft(corpFlag, 1)+"^";
            rtn += fixLeft(stmtCycle, 2)+"^";
            rtn += fixLeft(contactName, 40)+"^";
            rtn += fixLeft(acctType, 2)+"^";
            rtn += fixLeft(billSendingZip, 5)+"^";
            rtn += fixLeft(corpAddr1, 70)+"^";
            rtn += fixLeft(corpTelZone1, 4)+"^";
            rtn += fixLeft(corpTelNo1, 8)+"^";
            rtn += fixLeft(corpTelExt1, 6)+"^";
            rtn += fixLeft(corpTelZone2, 4)+"^";
            rtn += fixLeft(corpTelNo2, 8)+"^";
            rtn += fixLeft(corpTelExt2, 6)+"^";
            rtn += fixLeft(autopayAcctBank, 3)+"^";
            rtn += fixLeft(autopayAcctNo, 14)+"^";
            rtn += fixRight(lineOfCreditAmt, 10)+"^";
            rtn += fixLeft(currentCode, 1)+"^";
            rtn += fixRight(mcode, 2)+"^";
            rtn += fixLeft(chargeName, 30)+"^";
            rtn += fixRight(payAmt, 14)+"^";
            rtn += fixLeft(orgDelinquentDate, 8)+"^";
            rtn += fixLeft(vipCode, 2)+"^";
            rtn += fixLeft(paymentNo, 15)+"^";
            rtn += fixLeft(eMailAddr1, 50)+"^";
            rtn += fixLeft(eMailAddr2, 50)+"^";
            rtn += fixLeft(eMailAddr3, 50)+"^";
            rtn += fixLeft(statUnprintFlag, 1)+"^";
            rtn += fixLeft(statUnprintSMonth, 6)+"^";
            rtn += fixLeft(statUnprintEMonth, 6)+"^";
            rtn += fixLeft(statSendPaper, 1)+"^";
            rtn += fixLeft(statSendSMonth, 6)+"^";
            rtn += fixLeft(statSendEMonth, 6)+"^";
            rtn += fixLeft(statSendInternet, 1)+"^";
            rtn += fixLeft(statSendSMonth2, 6)+"^";
            rtn += fixLeft(statSendEMonth2, 6)+"^";
            rtn += fixRight(maxMcode, 3)+"^";
            return rtn;
        }

        String fixRight(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = spc + str;
            byte[] bytes = str.getBytes("MS950");
            int offset = bytes.length - len;
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, offset, vResult, 0, len);
            return new String(vResult, "MS950");
        }
        
        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);
            return new String(vResult, "MS950");
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        prtData01.corpNo = comc.subMS950String(bytes, 0, 9);
        prtData01.closeDate = comc.subMS950String(bytes, 9, 8);
        prtData01.chiName = comc.subMS950String(bytes, 17, 41);
        prtData01.regZip = comc.subMS950String(bytes, 58, 6);
        prtData01.corpAddr2 = comc.subMS950String(bytes, 64, 71);
        prtData01.idnoFlag = comc.subMS950String(bytes, 135, 2);
        prtData01.corpFlag = comc.subMS950String(bytes, 137, 2);
        prtData01.stmtCycle = comc.subMS950String(bytes, 139, 3);
        prtData01.contactName = comc.subMS950String(bytes, 142, 41);
        prtData01.acctType = comc.subMS950String(bytes, 183, 3);
        prtData01.billSendingZip = comc.subMS950String(bytes, 186, 6);
        prtData01.corpAddr1 = comc.subMS950String(bytes, 192, 71);
        prtData01.corpTelZone1 = comc.subMS950String(bytes, 263, 5);
        prtData01.corpTelNo1 = comc.subMS950String(bytes, 268, 9);
        prtData01.corpTelExt1 = comc.subMS950String(bytes, 277, 7);
        prtData01.corpTelZone2 = comc.subMS950String(bytes, 284, 5);
        prtData01.corpTelNo2 = comc.subMS950String(bytes, 289, 9);
        prtData01.corpTelExt2 = comc.subMS950String(bytes, 298, 7);
        prtData01.autopayAcctBank = comc.subMS950String(bytes, 305, 4);
        prtData01.autopayAcctNo = comc.subMS950String(bytes, 309, 15);
        prtData01.lineOfCreditAmt = comc.subMS950String(bytes, 324, 11);
        prtData01.currentCode = comc.subMS950String(bytes, 335, 2);
        prtData01.mcode = comc.subMS950String(bytes, 337, 3);
        prtData01.chargeName = comc.subMS950String(bytes, 340, 31);
        prtData01.payAmt = comc.subMS950String(bytes, 371, 15);
        prtData01.orgDelinquentDate = comc.subMS950String(bytes, 386, 9);
        prtData01.vipCode = comc.subMS950String(bytes, 395, 3);
        prtData01.paymentNo = comc.subMS950String(bytes, 398, 16);
        prtData01.eMailAddr1 = comc.subMS950String(bytes, 414, 51);
        prtData01.eMailAddr2 = comc.subMS950String(bytes, 465, 51);
        prtData01.eMailAddr3 = comc.subMS950String(bytes, 516, 51);
        prtData01.statUnprintFlag = comc.subMS950String(bytes, 567, 2);
        prtData01.statUnprintSMonth = comc.subMS950String(bytes, 569, 7);
        prtData01.statUnprintEMonth = comc.subMS950String(bytes, 576, 7);
        prtData01.statSendPaper = comc.subMS950String(bytes, 583, 2);
        prtData01.statSendSMonth = comc.subMS950String(bytes, 585, 7);
        prtData01.statSendEMonth = comc.subMS950String(bytes, 592, 7);
        prtData01.statSendInternet = comc.subMS950String(bytes, 599, 2);
        prtData01.statSendSMonth2 = comc.subMS950String(bytes, 601, 7);
        prtData01.statSendEMonth2 = comc.subMS950String(bytes, 608, 7);
        prtData01.maxMcode = comc.subMS950String(bytes, 615, 4);
    }

    /***********************************************************************/
    class PrtBuf02 {
        String corpNo;
        String id;
        String chiName;
        String birthday;
        String companyName;
        String jobPosition;
        String sex;
        String homeAreaCode1;
        String homeTelNo1;
        String homeTelExt1;
        String homeAreaCode2;
        String homeTelNo2;
        String homeTelExt2;
        String officeAreaCode1;
        String officeTelNo1;
        String officeTelExt1;
        String officeAreaCode2;
        String officeTelNo2;
        String officeTelExt2;
        String autopayAcctBank;
        String autopayAcctNo;
        String corpActFlag;
        String billSendingZip;
        String addr1;
        String residentZip;
        String addr3;
        String acctType;
        String stmtCycle;
        String lineOfCreditAmt;
        String stopStatus;
        String blockDate;
        String blockReason1;
        String blockReason2;
        String blockReason3;
        String blockReason4;
        String blockReason5;
        String cardSince;
        String sourceCode;
        String cellarPhone;
        String bbCall;
        String mcode;
        String currentCode;
        String noPerCollFlag;
        String noTelCollFlag;
        String noUnblockFlag;
        String noFStopFlag;
        String noAdjLoc;
        String noTelCollFlagV;
        String payAmt;
        String orgDelinquentDate;
        String bkFlag;
        String fhFlag;
        String vipCode;
        String paymentNo;
        String liabStatus;
        String oppostDate;
        String liacStatus;
        String renewStatus;
        String liqustatus;
        String liacEndReason;
        String liabDate;
        String renewDate;
        String liacDate;
        String linqDate;
        String eMailAddr1;
        String eMailAddr2;
        String emailaddr3;
        String statUnprintFlag;
        String statUnprintSMonth;
        String statUnprintEMonth;
        String statSendPaper;
        String statSendSMonth;
        String statSendEMonth;
        String statSendInternet;
        String statSendSMonth2;
        String statSendEMonth2;
        String salaryCode;
        String salaryHoldinFlag;
        String trialdate;
        String csmDate;
        String csmCasetype;
        String liaxStatus;
        String liaxEndReason;
        String liaxDate;
        String maxMcode;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
//            rtn += fixRight(corp_no, 9);
//            rtn += fixRight(id, 12);
//            rtn += fixRight(chi_name, 21);
//            rtn += fixRight(birthday, 9);
//            rtn += fixRight(company_name, 41);
//            rtn += fixRight(job_position, 25);
//            rtn += fixRight(sex, 2);
//            rtn += fixRight(home_area_code1, 5);
//            rtn += fixRight(home_tel_no1, 9);
//            rtn += fixRight(home_tel_ext1, 7);
//            rtn += fixRight(home_area_code2, 5);
//            rtn += fixRight(home_tel_no2, 9);
//            rtn += fixRight(home_tel_ext2, 7);
//            rtn += fixRight(office_area_code1, 5);
//            rtn += fixRight(office_tel_no1, 9);
//            rtn += fixRight(office_tel_ext1, 7);
//            rtn += fixRight(office_area_code2, 5);
//            rtn += fixRight(office_tel_no2, 9);
//            rtn += fixRight(office_tel_ext2, 7);
//            rtn += fixRight(autopay_acct_bank, 4);
//            rtn += fixRight(autopay_acct_no, 15);
//            rtn += fixRight(corp_act_flag, 2);
//            rtn += fixRight(bill_sending_zip, 6);
//            rtn += fixRight(addr1, 71);
//            rtn += fixRight(resident_zip, 6);
//            rtn += fixRight(addr3, 71);
//            rtn += fixRight(acct_type, 3);
//            rtn += fixRight(stmt_cycle, 3);
//            rtn += fixRight(line_of_credit_amt, 11);
//            rtn += fixRight(stop_status, 2);
//            rtn += fixRight(block_date, 9);
//            rtn += fixRight(block_reason1, 3);
//            rtn += fixRight(block_reason2, 3);
//            rtn += fixRight(block_reason3, 3);
//            rtn += fixRight(block_reason4, 3);
//            rtn += fixRight(block_reason5, 3);
//            rtn += fixRight(card_since, 9);
//            rtn += fixRight(source_code, 5);
//            rtn += fixRight(cellar_phone, 16);
//            rtn += fixRight(bb_call, 16);
//            rtn += fixRight(mcode, 3);
//            rtn += fixRight(current_code, 2);
//            rtn += fixRight(no_per_coll_flag, 2);
//            rtn += fixRight(no_tel_coll_flag, 2);
//            rtn += fixRight(no_unblock_flag, 2);
//            rtn += fixRight(no_f_stop_flag, 2);
//            rtn += fixRight(no_adj_loc, 2);
//            rtn += fixRight(no_tel_coll_flag_v, 2);
//            rtn += fixRight(pay_amt, 15);
//            rtn += fixRight(org_delinquent_date, 9);
//            rtn += fixRight(bk_flag, 2);
//            rtn += fixRight(fh_flag, 2);
//            rtn += fixRight(vip_code, 3);
//            rtn += fixRight(payment_no, 16);
//            rtn += fixRight(liab_status, 2);
//            rtn += fixRight(oppost_date, 9);
//            rtn += fixRight(liac_status, 2);
//            rtn += fixRight(renew_status, 4);
//            rtn += fixRight(liqu_status, 4);
//            rtn += fixRight(liac_end_reason, 3);
//            rtn += fixRight(liab_date, 9);
//            rtn += fixRight(renew_date, 9);
//            rtn += fixRight(liac_date, 9);
//            rtn += fixRight(linq_date, 9);
//            rtn += fixRight(e_mail_addr1, 51);
//            rtn += fixRight(e_mail_addr2, 51);
//            rtn += fixRight(e_mail_addr3, 51);
//            rtn += fixRight(stat_unprint_flag, 2);
//            rtn += fixRight(stat_unprint_s_month, 7);
//            rtn += fixRight(stat_unprint_e_month, 7);
//            rtn += fixRight(stat_send_paper, 2);
//            rtn += fixRight(stat_send_s_month, 7);
//            rtn += fixRight(stat_send_e_month, 7);
//            rtn += fixRight(stat_send_internet, 2);
//            rtn += fixRight(stat_send_s_month2, 7);
//            rtn += fixRight(stat_send_e_month2, 7);
//            rtn += fixRight(salary_code, 2);
//            rtn += fixRight(salary_holdin_flag, 2);
//            rtn += fixRight(trial_date, 9);
//            rtn += fixRight(csm_date, 9);
//            rtn += fixRight(csm_casetype, 6);
//            rtn += fixRight(liax_status, 2);
//            rtn += fixRight(liax_end_reason, 3);
//            rtn += fixRight(liax_date, 9);
//            rtn += fixRight(max_mcode, 4);
            
            rtn += fixLeft(corpNo, 8)+"^";
            rtn += fixLeft(id, 11)+"^";
            rtn += fixLeft(chiName, 20)+"^";
            rtn += fixLeft(birthday, 8)+"^";
            rtn += fixLeft(companyName, 40)+"^";
            rtn += fixLeft(jobPosition, 24)+"^";
            rtn += fixLeft(sex, 1)+"^";
            rtn += fixLeft(homeAreaCode1, 4)+"^";
            rtn += fixLeft(homeTelNo1, 8)+"^";
            rtn += fixLeft(homeTelExt1, 6)+"^";
            rtn += fixLeft(homeAreaCode2, 4)+"^";
            rtn += fixLeft(homeTelNo2, 8)+"^";
            rtn += fixLeft(homeTelExt2, 6)+"^";
            rtn += fixLeft(officeAreaCode1, 4)+"^";
            rtn += fixLeft(officeTelNo1, 8)+"^";
            rtn += fixLeft(officeTelExt1, 6)+"^";
            rtn += fixLeft(officeAreaCode2, 4)+"^";
            rtn += fixLeft(officeTelNo2, 8)+"^";
            rtn += fixLeft(officeTelExt2, 6)+"^";
            rtn += fixLeft(autopayAcctBank, 3)+"^";
            rtn += fixLeft(autopayAcctNo, 14)+"^";
            rtn += fixLeft(corpActFlag, 1)+"^";
            rtn += fixLeft(billSendingZip, 5)+"^";
            rtn += fixLeft(addr1, 70)+"^";
            rtn += fixLeft(residentZip, 5)+"^";
            rtn += fixLeft(addr3, 70)+"^";
            rtn += fixLeft(acctType, 2)+"^";
            rtn += fixLeft(stmtCycle, 2)+"^";
            rtn += fixRight(lineOfCreditAmt, 10)+"^";
            rtn += fixLeft(stopStatus, 1)+"^";
            rtn += fixLeft(blockDate, 8)+"^";
            rtn += fixLeft(blockReason1, 2)+"^";
            rtn += fixLeft(blockReason2, 2)+"^";
            rtn += fixLeft(blockReason3, 2)+"^";
            rtn += fixLeft(blockReason4, 2)+"^";
            rtn += fixLeft(blockReason5, 2)+"^";
            rtn += fixLeft(cardSince, 8)+"^";
            rtn += fixLeft(sourceCode, 4)+"^";
            rtn += fixLeft(cellarPhone, 15)+"^";
            rtn += fixLeft(bbCall, 15)+"^";
            rtn += fixRight(mcode, 2)+"^";
            rtn += fixLeft(currentCode, 1)+"^";
            rtn += fixLeft(noPerCollFlag, 1)+"^";
            rtn += fixLeft(noTelCollFlag, 1)+"^";
            rtn += fixLeft(noUnblockFlag, 1)+"^";
            rtn += fixLeft(noFStopFlag, 1)+"^";
            rtn += fixLeft(noAdjLoc, 1)+"^";
            rtn += fixLeft(noTelCollFlagV, 1)+"^";
            rtn += fixRight(payAmt, 14)+"^";
            rtn += fixLeft(orgDelinquentDate, 8)+"^";
            rtn += fixLeft(bkFlag, 1)+"^";
            rtn += fixLeft(fhFlag, 1)+"^";
            rtn += fixLeft(vipCode, 2)+"^";
            rtn += fixLeft(paymentNo, 15)+"^";
            rtn += fixLeft(liabStatus, 1)+"^";
            rtn += fixLeft(oppostDate, 8)+"^";
            rtn += fixLeft(liacStatus, 1)+"^";
            rtn += fixLeft(renewStatus, 3)+"^";
            rtn += fixLeft(liqustatus, 3)+"^";
            rtn += fixLeft(liacEndReason, 2)+"^";
            rtn += fixLeft(liabDate, 8)+"^";
            rtn += fixLeft(renewDate, 8)+"^";
            rtn += fixLeft(liacDate, 8)+"^";
            rtn += fixLeft(linqDate, 8)+"^";
            rtn += fixLeft(eMailAddr1, 50)+"^";
            rtn += fixLeft(eMailAddr2, 50)+"^";
            rtn += fixLeft(emailaddr3, 50)+"^";
            rtn += fixLeft(statUnprintFlag, 1)+"^";
            rtn += fixLeft(statUnprintSMonth, 6)+"^";
            rtn += fixLeft(statUnprintEMonth, 6)+"^";
            rtn += fixLeft(statSendPaper, 1)+"^";
            rtn += fixLeft(statSendSMonth, 6)+"^";
            rtn += fixLeft(statSendEMonth, 6)+"^";
            rtn += fixLeft(statSendInternet, 1)+"^";
            rtn += fixLeft(statSendSMonth2, 6)+"^";
            rtn += fixLeft(statSendEMonth2, 6)+"^";
            rtn += fixLeft(salaryCode, 1)+"^";
            rtn += fixLeft(salaryHoldinFlag, 1)+"^";
            rtn += fixLeft(trialdate, 8)+"^";
            rtn += fixLeft(csmDate, 8)+"^";
            rtn += fixLeft(csmCasetype, 5)+"^";
            rtn += fixLeft(liaxStatus, 1)+"^";
            rtn += fixLeft(liaxEndReason, 2)+"^";
            rtn += fixLeft(liaxDate, 8)+"^";
            rtn += fixRight(maxMcode, 3)+"^";;
            return rtn;
        }

        String fixRight(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = spc + str;
            byte[] bytes = str.getBytes("MS950");
            int offset = bytes.length - len;
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, offset, vResult, 0, len);
            return new String(vResult, "MS950");
        }
        
        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);
            return new String(vResult, "MS950");
        }
    }

    void splitBuf2(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        prtData02.corpNo = comc.subMS950String(bytes, 0, 9);
        prtData02.id = comc.subMS950String(bytes, 9, 12);
        prtData02.chiName = comc.subMS950String(bytes, 21, 21);
        prtData02.birthday = comc.subMS950String(bytes, 42, 9);
        prtData02.companyName = comc.subMS950String(bytes, 51, 41);
        prtData02.jobPosition = comc.subMS950String(bytes, 92, 25);
        prtData02.sex = comc.subMS950String(bytes, 117, 2);
        prtData02.homeAreaCode1 = comc.subMS950String(bytes, 119, 5);
        prtData02.homeTelNo1 = comc.subMS950String(bytes, 124, 9);
        prtData02.homeTelExt1 = comc.subMS950String(bytes, 133, 7);
        prtData02.homeAreaCode2 = comc.subMS950String(bytes, 140, 5);
        prtData02.homeTelNo2 = comc.subMS950String(bytes, 145, 9);
        prtData02.homeTelExt2 = comc.subMS950String(bytes, 154, 7);
        prtData02.officeAreaCode1 = comc.subMS950String(bytes, 161, 5);
        prtData02.officeTelNo1 = comc.subMS950String(bytes, 166, 9);
        prtData02.officeTelExt1 = comc.subMS950String(bytes, 175, 7);
        prtData02.officeAreaCode2 = comc.subMS950String(bytes, 182, 5);
        prtData02.officeTelNo2 = comc.subMS950String(bytes, 187, 9);
        prtData02.officeTelExt2 = comc.subMS950String(bytes, 196, 7);
        prtData02.autopayAcctBank = comc.subMS950String(bytes, 203, 4);
        prtData02.autopayAcctNo = comc.subMS950String(bytes, 207, 15);
        prtData02.corpActFlag = comc.subMS950String(bytes, 222, 2);
        prtData02.billSendingZip = comc.subMS950String(bytes, 224, 6);
        prtData02.addr1 = comc.subMS950String(bytes, 230, 71);
        prtData02.residentZip = comc.subMS950String(bytes, 301, 6);
        prtData02.addr3 = comc.subMS950String(bytes, 307, 71);
        prtData02.acctType = comc.subMS950String(bytes, 378, 3);
        prtData02.stmtCycle = comc.subMS950String(bytes, 381, 3);
        prtData02.lineOfCreditAmt = comc.subMS950String(bytes, 384, 11);
        prtData02.stopStatus = comc.subMS950String(bytes, 395, 2);
        prtData02.blockDate = comc.subMS950String(bytes, 397, 9);
        prtData02.blockReason1 = comc.subMS950String(bytes, 406, 3);
        prtData02.blockReason2 = comc.subMS950String(bytes, 409, 3);
        prtData02.blockReason3 = comc.subMS950String(bytes, 412, 3);
        prtData02.blockReason4 = comc.subMS950String(bytes, 415, 3);
        prtData02.blockReason5 = comc.subMS950String(bytes, 418, 3);
        prtData02.cardSince = comc.subMS950String(bytes, 421, 9);
        prtData02.sourceCode = comc.subMS950String(bytes, 430, 5);
        prtData02.cellarPhone = comc.subMS950String(bytes, 435, 16);
        prtData02.bbCall = comc.subMS950String(bytes, 451, 16);
        prtData02.mcode = comc.subMS950String(bytes, 467, 3);
        prtData02.currentCode = comc.subMS950String(bytes, 470, 2);
        prtData02.noPerCollFlag = comc.subMS950String(bytes, 472, 2);
        prtData02.noTelCollFlag = comc.subMS950String(bytes, 474, 2);
        prtData02.noUnblockFlag = comc.subMS950String(bytes, 476, 2);
        prtData02.noFStopFlag = comc.subMS950String(bytes, 478, 2);
        prtData02.noAdjLoc = comc.subMS950String(bytes, 480, 2);
        prtData02.noTelCollFlagV = comc.subMS950String(bytes, 482, 2);
        prtData02.payAmt = comc.subMS950String(bytes, 484, 15);
        prtData02.orgDelinquentDate = comc.subMS950String(bytes, 499, 9);
        prtData02.bkFlag = comc.subMS950String(bytes, 508, 2);
        prtData02.fhFlag = comc.subMS950String(bytes, 510, 2);
        prtData02.vipCode = comc.subMS950String(bytes, 512, 3);
        prtData02.paymentNo = comc.subMS950String(bytes, 515, 16);
        prtData02.liabStatus = comc.subMS950String(bytes, 531, 2);
        prtData02.oppostDate = comc.subMS950String(bytes, 533, 9);
        prtData02.liacStatus = comc.subMS950String(bytes, 542, 2);
        prtData02.renewStatus = comc.subMS950String(bytes, 544, 4);
        prtData02.liqustatus = comc.subMS950String(bytes, 548, 4);
        prtData02.liacEndReason = comc.subMS950String(bytes, 552, 3);
        prtData02.liabDate = comc.subMS950String(bytes, 555, 9);
        prtData02.renewDate = comc.subMS950String(bytes, 564, 9);
        prtData02.liacDate = comc.subMS950String(bytes, 573, 9);
        prtData02.linqDate = comc.subMS950String(bytes, 582, 9);
        prtData02.eMailAddr1 = comc.subMS950String(bytes, 591, 51);
        prtData02.eMailAddr2 = comc.subMS950String(bytes, 642, 51);
        prtData02.emailaddr3 = comc.subMS950String(bytes, 693, 51);
        prtData02.statUnprintFlag = comc.subMS950String(bytes, 744, 2);
        prtData02.statUnprintSMonth = comc.subMS950String(bytes, 746, 7);
        prtData02.statUnprintEMonth = comc.subMS950String(bytes, 753, 7);
        prtData02.statSendPaper = comc.subMS950String(bytes, 760, 2);
        prtData02.statSendSMonth = comc.subMS950String(bytes, 762, 7);
        prtData02.statSendEMonth = comc.subMS950String(bytes, 769, 7);
        prtData02.statSendInternet = comc.subMS950String(bytes, 776, 2);
        prtData02.statSendSMonth2 = comc.subMS950String(bytes, 778, 7);
        prtData02.statSendEMonth2 = comc.subMS950String(bytes, 785, 7);
        prtData02.salaryCode = comc.subMS950String(bytes, 792, 2);
        prtData02.salaryHoldinFlag = comc.subMS950String(bytes, 794, 2);
        prtData02.trialdate = comc.subMS950String(bytes, 796, 9);
        prtData02.csmDate = comc.subMS950String(bytes, 805, 9);
        prtData02.csmCasetype = comc.subMS950String(bytes, 814, 6);
        prtData02.liaxStatus = comc.subMS950String(bytes, 820, 2);
        prtData02.liaxEndReason = comc.subMS950String(bytes, 822, 3);
        prtData02.liaxDate = comc.subMS950String(bytes, 825, 9);
        prtData02.maxMcode = comc.subMS950String(bytes, 834, 4);
    }

    /***********************************************************************/
    class PrtBuf03 {
        String closeDate;
        String companyName;
        String jobPosition;
        String officeAreaCode1;
        String officeTelNo1;
        String officeTelExt1;
        String officeAreaCode2;
        String officeTelNo2;
        String officeTelExt2;
        String homeAreaCode1;
        String homeTelNo1;
        String homeTelExt1;
        String homeAreaCode2;
        String homeTelNo2;
        String homeTelExt2;
        String autopayAcctBank;
        String autopayAcctNo;
        String id;
        String chiName;
        String birthday;
        String sex;
        String stmtCycle;
        String billSendingZip;
        String addr1;
        String acctType;
        String residentZip;
        String addr3;
        String lineOfCreditAmt;
        String stopStatus;
        String blockDate;
        String blockReason1;
        String blockReason2;
        String blockReason3;
        String blockReason4;
        String blockReason5;
        String cardSince;
        String groupCode;
        String cellarPhone;
        String bbCall;
        String mcode;
        String currentCode;
        String noPerCollFlag;
        String noTelCollFlag;
        String noUnblockFlag;
        String noFStopFlag;
        String noAdjLoc;
        String noTelCollFlagV;
        String rcUseIndicator;
        String payAmt;
        String orgDelinquentDate;
        String bkFlag;
        String fhFlag;
        String vipCode;
        String paymentNo;
        String liabStatus;
        String oppostDate;
        String liacStatus;
        String renewStatus;
        String liquStatus;
        String liacEndReason;
        String liabDate;
        String renewDate;
        String liacDate;
        String linqDate;
        String eMailAddr1;
        String eMailAddr2;
        String eMailAddr3;
        String statUnprintFlag;
        String statUnprintSMonth;
        String statUnprintEMonth;
        String statSendPaper;
        String statSendSMonth;
        String statSendEMonth;
        String statSendInternet;
        String statSendSMonth2;
        String statSendEMonth2;
        String salaryCode;
        String salaryHoldinFlag;
        String trialDate;
        String csmDate;
        String csmCasetype;
        String liaxStatus;
        String liaxEndReason;
        String liaxDate;
        String maxMcode;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
//            rtn += fixRight(close_date, 8);
//            rtn += fixRight(company_name, 41);
//            rtn += fixRight(job_position, 25);
//            rtn += fixRight(office_area_code1, 5);
//            rtn += fixRight(office_tel_no1, 9);
//            rtn += fixRight(office_tel_ext1, 7);
//            rtn += fixRight(office_area_code2, 5);
//            rtn += fixRight(office_tel_no2, 9);
//            rtn += fixRight(office_tel_ext2, 7);
//            rtn += fixRight(home_area_code1, 5);
//            rtn += fixRight(home_tel_no1, 9);
//            rtn += fixRight(home_tel_ext1, 7);
//            rtn += fixRight(home_area_code2, 5);
//            rtn += fixRight(home_tel_no2, 9);
//            rtn += fixRight(home_tel_ext2, 7);
//            rtn += fixRight(autopay_acct_bank, 4);
//            rtn += fixRight(autopay_acct_no, 15);
//            rtn += fixRight(id, 12);
//            rtn += fixRight(chi_name, 21);
//            rtn += fixRight(birthday, 9);
//            rtn += fixRight(sex, 2);
//            rtn += fixRight(stmt_cycle, 3);
//            rtn += fixRight(bill_sending_zip, 6);
//            rtn += fixRight(addr1, 71);
//            rtn += fixRight(acct_type, 3);
//            rtn += fixRight(resident_zip, 6);
//            rtn += fixRight(addr3, 71);
//            rtn += fixRight(line_of_credit_amt, 11);
//            rtn += fixRight(stop_status, 2);
//            rtn += fixRight(block_date, 9);
//            rtn += fixRight(block_reason1, 3);
//            rtn += fixRight(block_reason2, 3);
//            rtn += fixRight(block_reason3, 3);
//            rtn += fixRight(block_reason4, 3);
//            rtn += fixRight(block_reason5, 3);
//            rtn += fixRight(card_since, 9);
//            rtn += fixRight(group_code, 5);
//            rtn += fixRight(cellar_phone, 16);
//            rtn += fixRight(bb_call, 16);
//            rtn += fixRight(mcode, 3);
//            rtn += fixRight(current_code, 2);
//            rtn += fixRight(no_per_coll_flag, 2);
//            rtn += fixRight(no_tel_coll_flag, 2);
//            rtn += fixRight(no_unblock_flag, 2);
//            rtn += fixRight(no_f_stop_flag, 2);
//            rtn += fixRight(no_adj_loc, 2);
//            rtn += fixRight(no_tel_coll_flag_v, 2);
//            rtn += fixRight(rc_use_indicator, 2);
//            rtn += fixRight(pay_amt, 15);
//            rtn += fixRight(org_delinquent_date, 9);
//            rtn += fixRight(bk_flag, 2);
//            rtn += fixRight(fh_flag, 2);
//            rtn += fixRight(vip_code, 3);
//            rtn += fixRight(payment_no, 15);
//            rtn += fixRight(liab_status, 2);
//            rtn += fixRight(oppost_date, 9);
//            rtn += fixRight(liac_status, 2);
//            rtn += fixRight(renew_status, 4);
//            rtn += fixRight(liqu_status, 4);
//            rtn += fixRight(liac_end_reason, 3);
//            rtn += fixRight(liab_date, 9);
//            rtn += fixRight(renew_date, 9);
//            rtn += fixRight(liac_date, 9);
//            rtn += fixRight(linq_date, 9);
//            rtn += fixRight(e_mail_addr1, 51);
//            rtn += fixRight(e_mail_addr2, 51);
//            rtn += fixRight(e_mail_addr3, 51);
//            rtn += fixRight(stat_unprint_flag, 2);
//            rtn += fixRight(stat_unprint_s_month, 7);
//            rtn += fixRight(stat_unprint_e_month, 7);
//            rtn += fixRight(stat_send_paper, 2);
//            rtn += fixRight(stat_send_s_month, 7);
//            rtn += fixRight(stat_send_e_month, 7);
//            rtn += fixRight(stat_send_internet, 2);
//            rtn += fixRight(stat_send_s_month2, 7);
//            rtn += fixRight(stat_send_e_month2, 7);
//            rtn += fixRight(salary_code, 2);
//            rtn += fixRight(salary_holdin_flag, 2);
//            rtn += fixRight(trial_date, 9);
//            rtn += fixRight(csm_date, 9);
//            rtn += fixRight(csm_casetype, 6);
//            rtn += fixRight(liax_status, 2);
//            rtn += fixRight(liax_end_reason, 3);
//            rtn += fixRight(liax_date, 9);
//            rtn += fixRight(max_mcode, 4);
            
            rtn += fixLeft(closeDate, 7)+"^";
            rtn += fixLeft(companyName, 40)+"^";
            rtn += fixLeft(jobPosition, 24)+"^";
            rtn += fixLeft(officeAreaCode1, 4)+"^";
            rtn += fixLeft(officeTelNo1, 8)+"^";
            rtn += fixLeft(officeTelExt1, 6)+"^";
            rtn += fixLeft(officeAreaCode2, 4)+"^";
            rtn += fixLeft(officeTelNo2, 8)+"^";
            rtn += fixLeft(officeTelExt2, 6)+"^";
            rtn += fixLeft(homeAreaCode1, 4)+"^";
            rtn += fixLeft(homeTelNo1, 8)+"^";
            rtn += fixLeft(homeTelExt1, 6)+"^";
            rtn += fixLeft(homeAreaCode2, 4)+"^";
            rtn += fixLeft(homeTelNo2, 8)+"^";
            rtn += fixLeft(homeTelExt2, 6)+"^";
            rtn += fixLeft(autopayAcctBank, 3)+"^";
            rtn += fixLeft(autopayAcctNo, 14)+"^";
            rtn += fixLeft(id, 11)+"^";
            rtn += fixLeft(chiName, 20)+"^";
            rtn += fixLeft(birthday, 8)+"^";
            rtn += fixLeft(sex, 1)+"^";
            rtn += fixLeft(stmtCycle, 2)+"^";
            rtn += fixLeft(billSendingZip, 5)+"^";
            rtn += fixLeft(addr1, 70)+"^";
            rtn += fixLeft(acctType, 2)+"^";
            rtn += fixLeft(residentZip, 5)+"^";
            rtn += fixLeft(addr3, 70)+"^";
            rtn += fixRight(lineOfCreditAmt, 10)+"^";
            rtn += fixLeft(stopStatus, 1)+"^";
            rtn += fixLeft(blockDate, 8)+"^";
            rtn += fixLeft(blockReason1, 2)+"^";
            rtn += fixLeft(blockReason2, 2)+"^";
            rtn += fixLeft(blockReason3, 2)+"^";
            rtn += fixLeft(blockReason4, 2)+"^";
            rtn += fixLeft(blockReason5, 2)+"^";
            rtn += fixLeft(cardSince, 8)+"^";
            rtn += fixLeft(groupCode, 4)+"^";
            rtn += fixLeft(cellarPhone, 15)+"^";
            rtn += fixLeft(bbCall, 15)+"^";
            rtn += fixRight(mcode, 2)+"^";
            rtn += fixLeft(currentCode, 1)+"^";
            rtn += fixLeft(noPerCollFlag, 1)+"^";
            rtn += fixLeft(noTelCollFlag, 1)+"^";
            rtn += fixLeft(noUnblockFlag, 1)+"^";
            rtn += fixLeft(noFStopFlag, 1)+"^";
            rtn += fixLeft(noAdjLoc, 1)+"^";
            rtn += fixLeft(noTelCollFlagV, 1)+"^";
            rtn += fixLeft(rcUseIndicator, 1)+"^";
            rtn += fixRight(payAmt, 14)+"^";
            rtn += fixLeft(orgDelinquentDate, 8)+"^";
            rtn += fixLeft(bkFlag, 1)+"^";
            rtn += fixLeft(fhFlag, 1)+"^";
            rtn += fixLeft(vipCode, 2)+"^";
            rtn += fixLeft(paymentNo, 14)+"^";
            rtn += fixLeft(liabStatus, 1)+"^";
            rtn += fixLeft(oppostDate, 8)+"^";
            rtn += fixLeft(liacStatus, 1)+"^";
            rtn += fixLeft(renewStatus, 3)+"^";
            rtn += fixLeft(liquStatus, 3)+"^";
            rtn += fixLeft(liacEndReason, 2)+"^";
            rtn += fixLeft(liabDate, 8)+"^";
            rtn += fixLeft(renewDate, 8)+"^";
            rtn += fixLeft(liacDate, 8)+"^";
            rtn += fixLeft(linqDate, 8)+"^";
            rtn += fixLeft(eMailAddr1, 50)+"^";
            rtn += fixLeft(eMailAddr2, 50)+"^";
            rtn += fixLeft(eMailAddr3, 50)+"^";
            rtn += fixLeft(statUnprintFlag, 1)+"^";
            rtn += fixLeft(statUnprintSMonth, 6)+"^";
            rtn += fixLeft(statUnprintEMonth, 6)+"^";
            rtn += fixLeft(statSendPaper, 1)+"^";
            rtn += fixLeft(statSendSMonth, 6)+"^";
            rtn += fixLeft(statSendEMonth, 6)+"^";
            rtn += fixLeft(statSendInternet, 1)+"^";
            rtn += fixLeft(statSendSMonth2, 6)+"^";
            rtn += fixLeft(statSendEMonth2, 6)+"^";
            rtn += fixLeft(salaryCode, 1)+"^";
            rtn += fixLeft(salaryHoldinFlag, 1)+"^";
            rtn += fixLeft(trialDate, 8)+"^";
            rtn += fixLeft(csmDate, 8)+"^";
            rtn += fixLeft(csmCasetype, 5)+"^";
            rtn += fixLeft(liaxStatus, 1)+"^";
            rtn += fixLeft(liaxEndReason, 2)+"^";
            rtn += fixLeft(liaxDate, 8)+"^";
            rtn += fixRight(maxMcode, 3)+"^";
            return rtn;
        }

        String fixRight(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = spc + str;
            byte[] bytes = str.getBytes("MS950");
            int offset = bytes.length - len;
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, offset, vResult, 0, len);
            return new String(vResult, "MS950");
        }
        
        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);
            return new String(vResult, "MS950");
        }
    }

    void splitBuf3(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        prtData03.closeDate = comc.subMS950String(bytes, 0, 8);
        prtData03.companyName = comc.subMS950String(bytes, 8, 41);
        prtData03.jobPosition = comc.subMS950String(bytes, 49, 25);
        prtData03.officeAreaCode1 = comc.subMS950String(bytes, 74, 5);
        prtData03.officeTelNo1 = comc.subMS950String(bytes, 79, 9);
        prtData03.officeTelExt1 = comc.subMS950String(bytes, 88, 7);
        prtData03.officeAreaCode2 = comc.subMS950String(bytes, 95, 5);
        prtData03.officeTelNo2 = comc.subMS950String(bytes, 100, 9);
        prtData03.officeTelExt2 = comc.subMS950String(bytes, 109, 7);
        prtData03.homeAreaCode1 = comc.subMS950String(bytes, 116, 5);
        prtData03.homeTelNo1 = comc.subMS950String(bytes, 121, 9);
        prtData03.homeTelExt1 = comc.subMS950String(bytes, 130, 7);
        prtData03.homeAreaCode2 = comc.subMS950String(bytes, 137, 5);
        prtData03.homeTelNo2 = comc.subMS950String(bytes, 142, 9);
        prtData03.homeTelExt2 = comc.subMS950String(bytes, 151, 7);
        prtData03.autopayAcctBank = comc.subMS950String(bytes, 158, 4);
        prtData03.autopayAcctNo = comc.subMS950String(bytes, 162, 15);
        prtData03.id = comc.subMS950String(bytes, 177, 12);
        prtData03.chiName = comc.subMS950String(bytes, 189, 21);
        prtData03.birthday = comc.subMS950String(bytes, 210, 9);
        prtData03.sex = comc.subMS950String(bytes, 219, 2);
        prtData03.stmtCycle = comc.subMS950String(bytes, 221, 3);
        prtData03.billSendingZip = comc.subMS950String(bytes, 224, 6);
        prtData03.addr1 = comc.subMS950String(bytes, 230, 71);
        prtData03.acctType = comc.subMS950String(bytes, 301, 3);
        prtData03.residentZip = comc.subMS950String(bytes, 304, 6);
        prtData03.addr3 = comc.subMS950String(bytes, 310, 71);
        prtData03.lineOfCreditAmt = comc.subMS950String(bytes, 381, 11);
        prtData03.stopStatus = comc.subMS950String(bytes, 392, 2);
        prtData03.blockDate = comc.subMS950String(bytes, 394, 9);
        prtData03.blockReason1 = comc.subMS950String(bytes, 403, 3);
        prtData03.blockReason2 = comc.subMS950String(bytes, 406, 3);
        prtData03.blockReason3 = comc.subMS950String(bytes, 409, 3);
        prtData03.blockReason4 = comc.subMS950String(bytes, 412, 3);
        prtData03.blockReason5 = comc.subMS950String(bytes, 415, 3);
        prtData03.cardSince = comc.subMS950String(bytes, 418, 9);
        prtData03.groupCode = comc.subMS950String(bytes, 427, 5);
        prtData03.cellarPhone = comc.subMS950String(bytes, 432, 16);
        prtData03.bbCall = comc.subMS950String(bytes, 448, 16);
        prtData03.mcode = comc.subMS950String(bytes, 464, 3);
        prtData03.currentCode = comc.subMS950String(bytes, 467, 2);
        prtData03.noPerCollFlag = comc.subMS950String(bytes, 469, 2);
        prtData03.noTelCollFlag = comc.subMS950String(bytes, 471, 2);
        prtData03.noUnblockFlag = comc.subMS950String(bytes, 473, 2);
        prtData03.noFStopFlag = comc.subMS950String(bytes, 475, 2);
        prtData03.noAdjLoc = comc.subMS950String(bytes, 477, 2);
        prtData03.noTelCollFlagV = comc.subMS950String(bytes, 479, 2);
        prtData03.rcUseIndicator = comc.subMS950String(bytes, 481, 2);
        prtData03.payAmt = comc.subMS950String(bytes, 483, 15);
        prtData03.orgDelinquentDate = comc.subMS950String(bytes, 498, 9);
        prtData03.bkFlag = comc.subMS950String(bytes, 507, 2);
        prtData03.fhFlag = comc.subMS950String(bytes, 509, 2);
        prtData03.vipCode = comc.subMS950String(bytes, 511, 3);
        prtData03.paymentNo = comc.subMS950String(bytes, 514, 15);
        prtData03.liabStatus = comc.subMS950String(bytes, 529, 2);
        prtData03.oppostDate = comc.subMS950String(bytes, 531, 9);
        prtData03.liacStatus = comc.subMS950String(bytes, 540, 2);
        prtData03.renewStatus = comc.subMS950String(bytes, 542, 4);
        prtData03.liquStatus = comc.subMS950String(bytes, 546, 4);
        prtData03.liacEndReason = comc.subMS950String(bytes, 550, 3);
        prtData03.liabDate = comc.subMS950String(bytes, 553, 9);
        prtData03.renewDate = comc.subMS950String(bytes, 562, 9);
        prtData03.liacDate = comc.subMS950String(bytes, 571, 9);
        prtData03.linqDate = comc.subMS950String(bytes, 580, 9);
        prtData03.eMailAddr1 = comc.subMS950String(bytes, 589, 51);
        prtData03.eMailAddr2 = comc.subMS950String(bytes, 640, 51);
        prtData03.eMailAddr3 = comc.subMS950String(bytes, 691, 51);
        prtData03.statUnprintFlag = comc.subMS950String(bytes, 742, 2);
        prtData03.statUnprintSMonth = comc.subMS950String(bytes, 744, 7);
        prtData03.statUnprintEMonth = comc.subMS950String(bytes, 751, 7);
        prtData03.statSendPaper = comc.subMS950String(bytes, 758, 2);
        prtData03.statSendSMonth = comc.subMS950String(bytes, 760, 7);
        prtData03.statSendEMonth = comc.subMS950String(bytes, 767, 7);
        prtData03.statSendInternet = comc.subMS950String(bytes, 774, 2);
        prtData03.statSendSMonth2 = comc.subMS950String(bytes, 776, 7);
        prtData03.statSendEMonth2 = comc.subMS950String(bytes, 783, 7);
        prtData03.salaryCode = comc.subMS950String(bytes, 790, 2);
        prtData03.salaryHoldinFlag = comc.subMS950String(bytes, 792, 2);
        prtData03.trialDate = comc.subMS950String(bytes, 794, 9);
        prtData03.csmDate = comc.subMS950String(bytes, 803, 9);
        prtData03.csmCasetype = comc.subMS950String(bytes, 812, 6);
        prtData03.liaxStatus = comc.subMS950String(bytes, 818, 2);
        prtData03.liaxEndReason = comc.subMS950String(bytes, 820, 3);
        prtData03.liaxDate = comc.subMS950String(bytes, 823, 9);
        prtData03.maxMcode = comc.subMS950String(bytes, 832, 4);
    }

    /***********************************************************************/
    class PrtBuf04 {
        String acctHolderId;
        String birthday;
        String companyName;
        String jobPosition;
        String id;
        String chiName;
        String birthday1;
        String sex;
        String acctType;
        String officeAreaCode1;
        String officeTelNo1;
        String officeTelExt1;
        String officeAreaCode2;
        String officeTelNo2;
        String officeTelExt2;
        String homeAreaCode1;
        String homeTelNo1;
        String homeTelExt1;
        String homeAreaCode2;
        String homeTelNo2;
        String homeTelExt2;
        String residentZip;
        String addr3;
        String cellarPhone;
        String bbCall;
        String csmDate;
        String csmCasetype;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
//            rtn += fixRight(acct_holder_id, 12);
//            rtn += fixRight(birthday, 9);
//            rtn += fixRight(company_name, 41);
//            rtn += fixRight(job_position, 25);
//            rtn += fixRight(id, 12);
//            rtn += fixRight(chi_name, 21);
//            rtn += fixRight(birthday1, 9);
//            rtn += fixRight(sex, 2);
//            rtn += fixRight(acct_type, 3);
//            rtn += fixRight(office_area_code1, 5);
//            rtn += fixRight(office_tel_no1, 9);
//            rtn += fixRight(office_tel_ext1, 7);
//            rtn += fixRight(office_area_code2, 5);
//            rtn += fixRight(office_tel_no2, 9);
//            rtn += fixRight(office_tel_ext2, 7);
//            rtn += fixRight(home_area_code1, 5);
//            rtn += fixRight(home_tel_no1, 9);
//            rtn += fixRight(home_tel_ext1, 7);
//            rtn += fixRight(home_area_code2, 5);
//            rtn += fixRight(home_tel_no2, 9);
//            rtn += fixRight(home_tel_ext2, 7);
//            rtn += fixRight(resident_zip, 6);
//            rtn += fixRight(addr3, 71);
//            rtn += fixRight(cellar_phone, 16);
//            rtn += fixRight(bb_call, 16);
//            rtn += fixRight(csm_date, 9);
//            rtn += fixRight(csm_casetype, 6);
            
            rtn += fixLeft(acctHolderId, 11)+"^";
            rtn += fixLeft(birthday, 8)+"^";
            rtn += fixLeft(companyName, 40)+"^";
            rtn += fixLeft(jobPosition, 24)+"^";
            rtn += fixLeft(id, 11)+"^";
            rtn += fixLeft(chiName, 20)+"^";
            rtn += fixLeft(birthday1, 8)+"^";
            rtn += fixLeft(sex, 1)+"^";
            rtn += fixLeft(acctType, 2)+"^";
            rtn += fixLeft(officeAreaCode1, 4)+"^";
            rtn += fixLeft(officeTelNo1, 8)+"^";
            rtn += fixLeft(officeTelExt1, 6)+"^";
            rtn += fixLeft(officeAreaCode2, 4)+"^";
            rtn += fixLeft(officeTelNo2, 8)+"^";
            rtn += fixLeft(officeTelExt2, 6)+"^";
            rtn += fixLeft(homeAreaCode1, 4)+"^";
            rtn += fixLeft(homeTelNo1, 8)+"^";
            rtn += fixLeft(homeTelExt1, 6)+"^";
            rtn += fixLeft(homeAreaCode2, 4)+"^";
            rtn += fixLeft(homeTelNo2, 8)+"^";
            rtn += fixLeft(homeTelExt2, 6)+"^";
            rtn += fixLeft(residentZip, 5)+"^";
            rtn += fixLeft(addr3, 70)+"^";
            rtn += fixLeft(cellarPhone, 15)+"^";
            rtn += fixLeft(bbCall, 15)+"^";
            rtn += fixLeft(csmDate, 8)+"^";
            rtn += fixLeft(csmCasetype, 5)+"^";
            return rtn;
        }

        String fixRight(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = spc + str;
            byte[] bytes = str.getBytes("MS950");
            int offset = bytes.length - len;
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, offset, vResult, 0, len);
            return new String(vResult, "MS950");
        }
        
        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);
            return new String(vResult, "MS950");
        }
    }

    void splitBuf4(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        prtData04.acctHolderId = comc.subMS950String(bytes, 0, 12);
        prtData04.birthday = comc.subMS950String(bytes, 12, 9);
        prtData04.companyName = comc.subMS950String(bytes, 21, 41);
        prtData04.jobPosition = comc.subMS950String(bytes, 62, 25);
        prtData04.id = comc.subMS950String(bytes, 87, 12);
        prtData04.chiName = comc.subMS950String(bytes, 99, 21);
        prtData04.birthday1 = comc.subMS950String(bytes, 120, 9);
        prtData04.sex = comc.subMS950String(bytes, 129, 2);
        prtData04.acctType = comc.subMS950String(bytes, 131, 3);
        prtData04.officeAreaCode1 = comc.subMS950String(bytes, 134, 5);
        prtData04.officeTelNo1 = comc.subMS950String(bytes, 139, 9);
        prtData04.officeTelExt1 = comc.subMS950String(bytes, 148, 7);
        prtData04.officeAreaCode2 = comc.subMS950String(bytes, 155, 5);
        prtData04.officeTelNo2 = comc.subMS950String(bytes, 160, 9);
        prtData04.officeTelExt2 = comc.subMS950String(bytes, 169, 7);
        prtData04.homeAreaCode1 = comc.subMS950String(bytes, 176, 5);
        prtData04.homeTelNo1 = comc.subMS950String(bytes, 181, 9);
        prtData04.homeTelExt1 = comc.subMS950String(bytes, 190, 7);
        prtData04.homeAreaCode2 = comc.subMS950String(bytes, 197, 5);
        prtData04.homeTelNo2 = comc.subMS950String(bytes, 202, 9);
        prtData04.homeTelExt2 = comc.subMS950String(bytes, 211, 7);
        prtData04.residentZip = comc.subMS950String(bytes, 218, 6);
        prtData04.addr3 = comc.subMS950String(bytes, 224, 71);
        prtData04.cellarPhone = comc.subMS950String(bytes, 295, 16);
        prtData04.bbCall = comc.subMS950String(bytes, 311, 16);
        prtData04.csmDate = comc.subMS950String(bytes, 327, 9);
        prtData04.csmCasetype = comc.subMS950String(bytes, 336, 6);
    }

    /***********************************************************************/
    class PrtBuf05 {
        String id;
        String birthday;
        String relaId;
        String relaName;
        String acctType;
        String companyName;
        String companyZip;
        String addr4;
        String officeAreaCode1;
        String officeTelNo1;
        String officeTelExt1;
        String officeAreaCode2;
        String officeTelNo2;
        String officeTelExt2;
        String homeAreaCode1;
        String homeTelNo1;
        String homeTelExt1;
        String homeAreaCode2;
        String homeTelNo2;
        String homeTelExt2;
        String residentZip;
        String addr5;
        String cellarPhone;
        String bbCall;
        String relaSeqno;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
//            rtn += fixRight(id, 12);
//            rtn += fixRight(birthday, 9);
//            rtn += fixRight(rela_id, 12);
//            rtn += fixRight(rela_name, 21);
//            rtn += fixRight(acct_type, 3);
//            rtn += fixRight(company_name, 41);
//            rtn += fixRight(company_zip, 6);
//            rtn += fixRight(addr4, 71);
//            rtn += fixRight(office_area_code1, 5);
//            rtn += fixRight(office_tel_no1, 9);
//            rtn += fixRight(office_tel_ext1, 7);
//            rtn += fixRight(office_area_code2, 5);
//            rtn += fixRight(office_tel_no2, 9);
//            rtn += fixRight(office_tel_ext2, 7);
//            rtn += fixRight(home_area_code1, 5);
//            rtn += fixRight(home_tel_no1, 9);
//            rtn += fixRight(home_tel_ext1, 7);
//            rtn += fixRight(home_area_code2, 5);
//            rtn += fixRight(home_tel_no2, 9);
//            rtn += fixRight(home_tel_ext2, 7);
//            rtn += fixRight(resident_zip, 6);
//            rtn += fixRight(addr5, 71);
//            rtn += fixRight(cellar_phone, 16);
//            rtn += fixRight(bb_call, 15);
//            rtn += fixRight(rela_seqno, 11);
            
            //文字靠左,數字靠右,分隔符號^
            rtn += fixLeft(id, 11)+"^";
            rtn += fixLeft(birthday, 8)+"^";
            rtn += fixLeft(relaId, 11)+"^";
            rtn += fixLeft(relaName, 20)+"^";
            rtn += fixLeft(acctType, 2)+"^";
            rtn += fixLeft(companyName, 40)+"^";
            rtn += fixLeft(companyZip, 5)+"^";
            rtn += fixLeft(addr4, 70)+"^";
            rtn += fixLeft(officeAreaCode1, 4)+"^";
            rtn += fixLeft(officeTelNo1, 8)+"^";
            rtn += fixLeft(officeTelExt1, 6)+"^";
            rtn += fixLeft(officeAreaCode2, 4)+"^";
            rtn += fixLeft(officeTelNo2, 8)+"^";
            rtn += fixLeft(officeTelExt2, 6)+"^";
            rtn += fixLeft(homeAreaCode1, 4)+"^";
            rtn += fixLeft(homeTelNo1, 8)+"^";
            rtn += fixLeft(homeTelExt1, 6)+"^";
            rtn += fixLeft(homeAreaCode2, 4)+"^";
            rtn += fixLeft(homeTelNo2, 8)+"^";
            rtn += fixLeft(homeTelExt2, 6)+"^";
            rtn += fixLeft(residentZip, 5)+"^";
            rtn += fixLeft(addr5, 70)+"^";
            rtn += fixLeft(cellarPhone, 15)+"^";
            rtn += fixLeft(bbCall, 14)+"^";
            rtn += fixLeft(relaSeqno, 10);
            return rtn;
        }

        String fixRight(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = spc + str;
            byte[] bytes = str.getBytes("MS950");
            int offset = bytes.length - len;
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, offset, vResult, 0, len);
            return new String(vResult, "MS950");
        }
        
        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);
            return new String(vResult, "MS950");
        }
    }

    void splitBuf5(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        prtData05.id = comc.subMS950String(bytes, 0, 12);
        prtData05.birthday = comc.subMS950String(bytes, 12, 9);
        prtData05.relaId = comc.subMS950String(bytes, 21, 12);
        prtData05.relaName = comc.subMS950String(bytes, 33, 21);
        prtData05.acctType = comc.subMS950String(bytes, 54, 3);
        prtData05.companyName = comc.subMS950String(bytes, 57, 41);
        prtData05.companyZip = comc.subMS950String(bytes, 98, 6);
        prtData05.addr4 = comc.subMS950String(bytes, 104, 71);
        prtData05.officeAreaCode1 = comc.subMS950String(bytes, 175, 5);
        prtData05.officeTelNo1 = comc.subMS950String(bytes, 180, 9);
        prtData05.officeTelExt1 = comc.subMS950String(bytes, 189, 7);
        prtData05.officeAreaCode2 = comc.subMS950String(bytes, 196, 5);
        prtData05.officeTelNo2 = comc.subMS950String(bytes, 201, 9);
        prtData05.officeTelExt2 = comc.subMS950String(bytes, 210, 7);
        prtData05.homeAreaCode1 = comc.subMS950String(bytes, 217, 5);
        prtData05.homeTelNo1 = comc.subMS950String(bytes, 222, 9);
        prtData05.homeTelExt1 = comc.subMS950String(bytes, 231, 7);
        prtData05.homeAreaCode2 = comc.subMS950String(bytes, 238, 5);
        prtData05.homeTelNo2 = comc.subMS950String(bytes, 243, 9);
        prtData05.homeTelExt2 = comc.subMS950String(bytes, 252, 7);
        prtData05.residentZip = comc.subMS950String(bytes, 259, 6);
        prtData05.addr5 = comc.subMS950String(bytes, 265, 71);
        prtData05.cellarPhone = comc.subMS950String(bytes, 336, 16);
        prtData05.bbCall = comc.subMS950String(bytes, 352, 15);
        prtData05.relaSeqno = comc.subMS950String(bytes, 367, 11);
    }

}

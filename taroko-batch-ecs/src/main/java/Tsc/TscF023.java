/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/10  V1.00.01    SUP       error correction                          *
*  109-11-17  V1.00.02    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊卡餘額轉置回應檔(BTRP)接收檢核*/
public class TscF023 extends AccessDAO {

    private final String progname = "悠遊卡餘額轉置回應檔(BTRP)接收檢核  109/11/17 V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps comips = new CommIps();

    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>(); // 字串陣列
    String rptName2 = ""; // ?
    int rptSeq2 = 0; // 目前行數 //++rptSeq
    String buf = ""; // 文字暫存

    String hCallBatchSeqno = "";

    String hTempNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempSystemDate = "";
    String hTempNotifyTime = "";
    String hTnlgPerformFlag = "";
    String hTnlgNotifyDate = "";
    String hTnlgCheckCode = "";
    String hTnlgProcFlag = "";
    String hTnlgRowid = "";
    String hTnlgFileName = "";
    String hIdnoChiName = "";
    String hOfficeTel = "";
    String hIdnoCellarPhone = "";
    String hCardCurrentCode = "";
    String hCgecCardNo = "";
    String hCgecPurchaseDate = "";
    String hTardBalanceDate = "";
    String hTardBlackltSDate = "";
    String hTardBlackltEDate = "";
    int hTempDiffDays = 0;
    String hCgecTscCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hPrintName = "";
    String hRptName = "";
    String hCgecBatchNo = "";
    int hCgecSeqNo = 0;
    String hCgecTransactionCode = "";
    String hCgecTscTxCode = "";
    String hCgecPurchaseTime = "";
    String hCgecMerchantNo = "";
    String hCgecMerchantCategory = "";
    String hCgecMerchantChiName = "";
    double hCgecDestinationAmt = 0;
    String hCgecDestinationCurrency = "";
    String hCgecBillDesc = "";
    String hCgecTrafficCd = "";
    String hCgecTrafficAbbr = "";
    String hCgecAddrCd = "";
    String hCgecAddrAbbr = "";
    String hCgecPostFlag = "";
    String hCgecFileName = "";
    String hCgecTscError = "";
    String hCgecTscNotiDate = "";
    String hCgecTscRespCode = "";
    String hCgecReturnSource = "";
    double hCgecServiceAmt = 0;

    int hTnlgRecordCnt = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int nUserpid = 0;
    int nRetcode = 0;
    int succCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String temstr1 = "";
    String hCgecBillType = "";
    String hSign = "";
    String hSigns = "";
    int cntM1 = 0;
    int cntP1 = 0;
    int cntM2 = 0;
    int cntP2 = 0;
    double amtM1 = 0;
    double amtP1 = 0;
    double amtM2 = 0;
    double amtP2 = 0;
    int rptCnt = 0;

    Buf1 dtl = new Buf1();

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }

            if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 3) {
                comc.errExit("Usage : TscF023 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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

            hTempNotifyDate = "";
            fileSeq = "01";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].substring(0, 1).equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8) {
                    String sgArgs0 = "";
                    sgArgs0 = args[0];
                    sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                    hTempNotifyDate = sgArgs0;
                }
                if (args[0].length() == 2) {
                    showLogMessage("I", "", String.format("參數(一) 不可兩碼 "));
                }
            }
            if (args.length == 2) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hTempNotifyDate = sgArgs0;
                if ((args[1].length() == 1) && (args[1].substring(0, 1).equals("Y")))
                    forceFlag = 1;
                if (args[1].length() == 2) {
                    String sgArgs1 = "";
                    sgArgs1 = args[1];
                    sgArgs1 = Normalizer.normalize(sgArgs1, java.text.Normalizer.Form.NFKD);
                    fileSeq = sgArgs1;
                }
                if (args[1].length() != 1 && args[1].length() != 2) {
                    showLogMessage("I", "", String.format("參數(二) 為[force_flag] or [seq(nn)]  "));
                }
            }
            if (args.length == 3) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hTempNotifyDate = sgArgs0;
                if (args[1].substring(0, 1).equals("Y"))
                    forceFlag = 1;
                if (args[2].length() != 2) {
                    showLogMessage("I", "", String.format("file seq 必須兩碼 "));
                }
                String sgArgs2 = "";
                sgArgs2 = args[2];
                sgArgs2 = Normalizer.normalize(sgArgs2, java.text.Normalizer.Form.NFKD);
                fileSeq = sgArgs2;
            }
            selectPtrBusinday();

            hTnlgFileName = String.format("BTRP.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            showLogMessage("I", "", String.format(" 處理檔案=[%s] ", hTnlgFileName));

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    updateTscNotifyLoga();
                    finalProcess();
                    return 0;
                }
            }

            deleteTscOrgdataLog();

            fileOpen();
            updateTscNotifyLoga();

            lpRtn("TSC_F023R1");

            if (rptCnt > 0) {
                buf = " 註： TSCC回覆結果為：9999，即列入失敗報表，須洽卡友至悠遊卡公司服務台辦理\n";
                lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));
                buf = "      退款金額(扣除服務費)：會有正負項交易\n";
                lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));
            }

            String szTmp1 = comcr.commFormat("3z,3z", cntM1);
            String szTmp2 = comcr.commFormat("3$,3$,3$", amtM1);
            String szTmp3 = comcr.commFormat("3z,3z", cntP1);
            String szTmp4 = comcr.commFormat("3$,3$,3$", amtP1);

            buf = "     TSCC回覆餘額轉置結果統計表";
            lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));
            buf = String.format("     處理日 : %s  \n\n", chinDate);
            lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));
            buf = String.format("     餘額轉置成功\\ 負項/退款筆數:%-7.7s 負項/退款金額合計:%-11.11s   正項/請款筆數:%-7.7s 正項/請款金額合計:%-11.11s\n",
                    szTmp1, szTmp2, szTmp3, szTmp4);
            lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

            szTmp1 = comcr.commFormat("3z,3z", cntM2);
            szTmp2 = comcr.commFormat("3$,3$,3$", amtM2);
            szTmp3 = comcr.commFormat("3z,3z", cntP2);
            szTmp4 = comcr.commFormat("3$,3$,3$", amtP2);
            buf = String.format("     餘額轉置失敗 負項/退款筆數:%-7.7s 負項/退款金額合計:%-11.11s   正項/請款筆數:%-7.7s 正項/請款金額合計:%-11.11s\n",
                    szTmp1, szTmp2, szTmp3, szTmp4);
            lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

            String temstr2 = String.format("%s/reports/TSC_F023R1_%s", comc.getECSHOME(), hTempSystemDate);
            temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
            if (lpar2.size() > 0) {
                comc.writeReport(temstr2, lpar2);
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
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8)) , ''"
                + ", to_char(decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'13'), 1 , sysdate"
                + ", sysdate-1 days) ,'yyyymmdd'), ?) h_temp_notify_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_system_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTempNotifyDate);
        setString(2, hTempNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempNotifyDate = getValue("h_temp_notify_date");
            hTempSystemDate = getValue("h_temp_system_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
        }

    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        /* proc_flag = 0:收檔中 1: 已收檔 2: 已處理 3: 已回應 */
        hTnlgPerformFlag = "";
        hTnlgNotifyDate = "";
        hTnlgCheckCode = "";
        hTnlgProcFlag = "";
        hTnlgRowid = "";

        sqlCmd = "select perform_flag,";
        sqlCmd += " notify_date,";
        sqlCmd += " check_code,";
        sqlCmd += " proc_flag,";
        sqlCmd += " rowid rowid ";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgPerformFlag = getValue("perform_flag");
            hTnlgNotifyDate = getValue("notify_date");
            hTnlgCheckCode = getValue("check_code");
            hTnlgProcFlag = getValue("proc_flag");
            hTnlgRowid = getValue("rowid");
        }

        if (!hTnlgPerformFlag.equals("Y")) {
            comcr.errRtn(String.format("通知檔收檔發生問題,[%s]暫不可處理 ,請通知相關人員處理(error)", hTnlgFileName), "",
                    hCallBatchSeqno);
        }
        if (hTnlgProcFlag.equals("0")) {
            comcr.errRtn(String.format("通知檔收檔中[%s] , 請通知相關人員處理(error) ", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (hTnlgProcFlag.toCharArray()[0] >= '2') {
            comcr.errRtn(String.format("[%s]餘額轉置回應檔已處理過,請通知相關人員處理(error) ", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (!hTnlgCheckCode.equals("0000")) {
            showLogMessage("I", "",
                    String.format("[%s]自動儲值請款檔整檔處理失敗  , 錯誤代碼[%s] ", hTnlgFileName, hTnlgCheckCode));
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void updateTscNotifyLoga() throws Exception {
        daoTable = "tsc_notify_log";
        updateSQL = " proc_flag = '2',";
        updateSQL += " proc_date = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " proc_time = to_char(sysdate, 'hh24miss'),";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where file_name = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", hTnlgFileName, hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteTscOrgdataLog() throws Exception {
        daoTable = "tsc_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();
    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";
        tmpstr1 = String.format("%s", hTnlgFileName);
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int f = openInputText(temstr1);
        if(f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫 ", temstr1), "", hCallBatchSeqno);
        }

        printHead();

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;

            initTscCgecAll();

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d] ", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            hOrgdOrgData = str600;
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1+2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hCgecTscCardNo = comc.rtrim(dtl.tscCardNo);

            if (selectTscCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmtSign));
            hSign = tmpstr1;
            if (hSign.equals("-")) {
                hCgecTransactionCode = "05";
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0201";
                insertTscOrgdataLog();
                continue;
            }
            hCgecDestinationAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            hCgecTscNotiDate = comc.rtrim(dtl.balanceDate);
            /*************************************************************************/
            hCgecPurchaseDate = hCgecTscNotiDate;
            hCgecPurchaseTime = hTempNotifyTime;
            /*************************************************************************/
            hCgecTscRespCode = comc.rtrim(dtl.respCode);
            hSigns = String.format("%s", comc.rtrim(dtl.serviceAmtSign));
            tmpstr1 = String.format("%s", comc.rtrim(dtl.serviceAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0201";
                insertTscOrgdataLog();
                continue;
            }
            hCgecServiceAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 80);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
            if (!comc.subMS950String(str600.getBytes("MS950"), 80, 96).equals(tmpstr2)) {
                hOrgdRptRespCode = "0205";
                showLogMessage("I", "", String.format("HASH values error [%s] ", hOrgdRptRespCode));
            }
            /*
             * insert_tsc_orgdata_log(); continue;
             */
            /*************************************************************************/
            hOrgdRptRespCode = "0000";

            insertTscOrgdataLog();

            if (!hCgecTscRespCode.equals("9999")) {
                if (hCgecTransactionCode.equals("06")) {
                    cntM1++;
                    amtM1 = amtM1 + hCgecDestinationAmt;
                } else {
                    cntP1++;
                    amtP1 = amtP1 + hCgecDestinationAmt;
                }
                succCnt++;
            } else {
                if (hCgecTransactionCode.equals("06")) {
                    cntM2++;
                    amtM2 = amtM2 + hCgecDestinationAmt;
                } else {
                    cntP2++;
                    amtP2 = amtP2 + hCgecDestinationAmt;
                }
            }

            if (hCgecTscRespCode.equals("9999")) {
                hIdnoChiName = "";
                hOfficeTel = "";
                hIdnoCellarPhone = "";
                hCardCurrentCode = "";
                sqlCmd = "select uf_hi_cname(b.chi_name) h_idno_chi_name,";
                sqlCmd += " b.office_area_code1||'-'||office_tel_no1||'-'||office_tel_ext1 office_tel,";
                sqlCmd += " uf_hi_telno(b.cellar_phone) h_idno_cellar_phone,";
                sqlCmd += " a.current_code ";
                sqlCmd += " from crd_idno b, crd_card a  ";
                sqlCmd += "where a.id_p_seqno = b.id_p_seqno  ";
                sqlCmd += "  and a.card_no = ? ";
                setString(1, hCgecCardNo);
                int cursorIndex = openCursor();
                while (fetchTable(cursorIndex)) {
                    hIdnoChiName = getValue("h_idno_chi_name");
                    hOfficeTel = getValue("office_tel");
                    hIdnoCellarPhone = getValue("h_idno_cellar_phone");
                    hCardCurrentCode = getValue("current_code");
                }
                closeCursor(cursorIndex);
                rptCnt++;
                tmpstr = comcr.commHiCardno(hCgecCardNo);
                buf = String.format("%-10.10s %-16.16s %-4.4s %-16.16s %-8.8s %12.0f  %13.0f %-16.16s %12.12s  %s\n",
                        hIdnoChiName, tmpstr, hCardCurrentCode, hCgecTscCardNo, hCgecTscNotiDate,
                        hCgecDestinationAmt, hCgecServiceAmt, hOfficeTel, hIdnoCellarPhone,
                        hCgecTransactionCode);
                lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));
            }
        }
        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    void printHead() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: TSC_F023R1", 1);
        String szTmp = String.format("%22s", "餘額轉置回應 9999 明細表");
        buf = comcr.insertStrCenter(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "印表日期:", 60);
        buf = comcr.insertStr(buf, chinDate, 70);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf,
                "姓      名 信用卡號         狀態 悠遊卡號         回饋日期 餘額轉置金額 餘額轉置服務費 公司電話         手機         異動", 01);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf,
                "========== ================ ==== ================ ======== ============ ============== ================ ============ ====",
                01);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

    }

    /***********************************************************************/
    int selectTscCard() throws Exception {
        hTardBalanceDate = "";
        hTardBlackltSDate = "";
        hTardBlackltEDate = "";
        hTempDiffDays = 0;

        sqlCmd = "select card_no,";
        sqlCmd += " balance_date,";
        sqlCmd += " blacklt_s_date,";
        sqlCmd += " blacklt_e_date,";
        sqlCmd += " days_between(to_date( ? ,'yyyymmdd') , to_date( ? ,'yyyymmdd')) h_temp_diff_days ";
        sqlCmd += " from tsc_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempSystemDate);
        setString(2, hCgecPurchaseDate.length() == 0 ? null : hCgecPurchaseDate);
        setString(3, hCgecTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCgecCardNo = getValue("card_no");
            hTardBalanceDate = getValue("balance_date");
            hTardBlackltSDate = getValue("blacklt_s_date");
            hTardBlackltEDate = getValue("blacklt_e_date");
            hTempDiffDays = getValueInt("h_temp_diff_days");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {
        daoTable = "tsc_orgdata_log";
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "BTRP");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_orgdata_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void initTscCgecAll() throws Exception {
        hCgecBatchNo = "";
        hCgecSeqNo = 0;
        hCgecCardNo = "";
        hCgecTscCardNo = "";
        hCgecTransactionCode = "06";
        hCgecTscTxCode = "";
        hCgecPurchaseDate = "";
        hCgecPurchaseTime = "";
        hCgecMerchantNo = "EASY8002";
        hCgecMerchantCategory = "4100";
        hCgecMerchantChiName = "";
        hCgecDestinationAmt = 0;
        hCgecDestinationCurrency = "901";
        hCgecBillDesc = "";
        hCgecTrafficCd = "";
        hCgecTrafficAbbr = "";
        hCgecAddrCd = "";
        hCgecAddrAbbr = "";
        hCgecPostFlag = "N";
        hCgecFileName = "";
        hCgecTscError = "";
        hCgecTscNotiDate = "";
        hCgecTscRespCode = "";
        hCgecReturnSource = "";
        hCgecServiceAmt = 0;
    }

    /***********************************************************************/
    void lpRtn(String lstr) throws Exception {
        String hPrintName = "";
        String hRptName = "";
        String lpStr = "";

        hPrintName = "";
        hRptName = lstr;
        sqlCmd = "select print_name ";
        sqlCmd += " from bil_rpt_prt  ";
        sqlCmd += "where report_name like ? || '%'  ";
        sqlCmd += "fetch first 1 rows only";
        setString(1, hRptName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hPrintName = getValue("print_name");
        }
        if (hPrintName.length() > 0) {
            lpStr = String.format("lp -d %s %s/reports/%s_%s", hPrintName, comc.getECSHOME(), lstr,
                    hTempSystemDate);
            lpStr = Normalizer.normalize(lpStr, java.text.Normalizer.Form.NFKD);
            // comc.systemCmd(lp_str);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF023 proc = new TscF023();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tscCardNo;
        String returnAmt;
        String destinationAmtSign;
        String destinationAmt;
        String balanceDate;
        String respCode;
        String serviceAmtSign;
        String serviceAmt;
        String filler2;
        String hashValue;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(returnAmt, 13);
            rtn += comc.fixLeft(destinationAmtSign, 1);
            rtn += comc.fixLeft(destinationAmt, 12);
            rtn += comc.fixLeft(balanceDate, 8);
            rtn += comc.fixLeft(respCode, 4);
            rtn += comc.fixLeft(serviceAmtSign, 1);
            rtn += comc.fixLeft(serviceAmt, 12);
            rtn += comc.fixLeft(filler2, 6);
            rtn += comc.fixLeft(hashValue, 16);
            rtn += comc.fixLeft(filler1, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.attri = comc.subMS950String(bytes, 1, 2);
        dtl.tscCardNo = comc.subMS950String(bytes, 3, 20);
        dtl.returnAmt = comc.subMS950String(bytes, 23, 13);
        dtl.destinationAmtSign = comc.subMS950String(bytes, 36, 1);
        dtl.destinationAmt = comc.subMS950String(bytes, 37, 12);
        dtl.balanceDate = comc.subMS950String(bytes, 49, 8);
        dtl.respCode = comc.subMS950String(bytes, 57, 4);
        dtl.serviceAmtSign = comc.subMS950String(bytes, 61, 1);
        dtl.serviceAmt = comc.subMS950String(bytes, 62, 12);
        dtl.filler2 = comc.subMS950String(bytes, 74, 6);
        dtl.hashValue = comc.subMS950String(bytes, 80, 16);
        dtl.filler1 = comc.subMS950String(bytes, 96, 2);
    }

}

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/22  V1.00.00    Wendy Lu                     program initial        *
*  112/05/09  V1.00.01    Wilson    mark update_tsc_notify_log                *
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


public class TscD033 extends AccessDAO {
    private final String progname = "悠遊VD卡餘額轉置回應檔(DCBP)媒體接收處理程式  112/05/09 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps comips = new CommIps();

    String rptName2 = "餘額轉置回應 9999 明細表";
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
    String rptId2 = "TSC_D033R1";
    int rptSeq2 = 0;
    String buf = "";
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
    String hCgprId = "";
    String hCgprIdCode = "";
    String hCgprIdPSeqno = "";
    String hCgecCardNo = "";
    String hCgecPurchaseDate = "";
    String hTardBalanceDate = "";
    int hTardBalanceRtnFee = 0;
    String hTardBlackltSDate = "";
    String hTardBlackltEDate = "";
    int hTempDiffDays = 0;
    String hTardAutoloadFlag = "";
    String hTardTscSignFlag = "";
    String hCgecTscCardNo = "";
    String hCgecTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hTempBatchSeq = "";
    int hCgecSeqNo = 0;
    String hCgecBillType = "";
    String hCgecTransactionCode = "";
    String hCgecTscTxCode = "";
    String hCgecPurchaseTime = "";
    String hCgecMerchantChiName = "";
    double hCgecDestinationAmt = 0;
    String hCgecBillDesc = "";
    String hCgecPostFlag = "";
    String hCgecTscNotiDate = "";
    String hCgecTscRespCode = "";
    double hCgecServiceAmt = 0;
    String hCardAcctType = "";
    String hCardPSeqno = "";
    String fixBillType = "";
    double hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hBiunConfFlag = "";
    double hAmt = 0;
    double hAmts = 0;
    String hPrintName = "";
    String hRptName = "";
    int hCnt = 0;
    int hErrCnt = 0;
    String hTnlhAcctType = "";
    String hTnlhAcctKey = "";
    String hTnlhCardNo = "";
    String hTnlhId = "";

    String hTnlhIdPSeqno = "";
    String hTnlhMajorChiName = "";
    double hTnlhPrebalanceAmt = 0;
    double hTnlhRemainAmt = 0;
    String hMloaBatchNo = "";
    int hMloaRec = 0;
    String hMloaAcctNo = "";
    String hTnlhProcessStatus = "";
    String hCgprOnlineMark = "";
    int hMloaPeriod = 1;
    int hMloaInterest = 0;
    double hMloaRtnRate = 0;
    double hBpcdNetTtlBp = 0;
    String hTempBatchNo = "";
    String hCgecBatchNo = "";
    String hCgecMerchantNo = "";
    String hCgecMerchantCategory = "";
    String hCgecFileName = "";
    String hCgecTscError = "";
    String hCgecReturnSource = "";
    String hCgecDestinationCurrency = "";

    int hTnlgRecordCnt = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int succCnt = 0;
    int rptCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String temstr1 = "";
    String hSign = "";
    String hSigns = "";
    int cntM1 = 0;
    int cntP1 = 0;
    int cntM2 = 0;
    int cntP2 = 0;
    double amtM1 = 0;
    double amtM2 = 0;
    double amtP1 = 0;
    double amtP2 = 0;

    Buf1 dtl = new Buf1();
    int out = -1;
    
    private String hBusinssChiDate = "";
    private String hVouchChiDate = "";
    private String hCurpId = "";
    private String hCur1Id = "";

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
                comc.errExit("Usage : TscD033 [[notify_date][force_flag]] [force_flag][seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

       
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempNotifyDate = "";
            fileSeq = "01";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8) {
                    String sgArgs0 = "";
                    sgArgs0 = args[0];
                    sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                    hTempNotifyDate = sgArgs0;
                }
                if (args[0].length() == 2) {
                    showLogMessage("I", "", String.format("參數(一) 不可兩碼"));
                }
            }
            if (args.length == 2) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hTempNotifyDate = sgArgs0;
                if ((args[1].length() == 1) && (args[1].equals("Y")))
                    forceFlag = 1;
                if (args[1].length() == 2) {
                    String sgArgs1 = "";
                    sgArgs1 = args[1];
                    sgArgs1 = Normalizer.normalize(sgArgs1, java.text.Normalizer.Form.NFKD);
                    fileSeq = sgArgs1;
                }
                if (args[1].length() != 1 && args[1].length() != 2) {
                    showLogMessage("I", "", String.format("參數(二) 為[force_flag] or [seq(nn)] "));

                }
            }
            if (args.length == 3) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hTempNotifyDate = sgArgs0;
                if (args[1].equals("Y"))
                    forceFlag = 1;
                if (args[2].length() != 2) {
                    showLogMessage("I", "", String.format("file seq 必須兩碼"));
                }
                String sgArgs2 = "";
                sgArgs2 = args[2];
                sgArgs2 = Normalizer.normalize(sgArgs2, java.text.Normalizer.Form.NFKD);
                fileSeq = sgArgs2;
            }
            selectPtrBusinday();

            tmpstr1 = String.format("DCBP.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            fixBillType = "TSCC";
            hCgecBillType = fixBillType;

            deleteBilPostcntl();
            deleteTscDccgAll();
            deleteTscDccgPre();
            deleteTscOrgdataLog();

            selectPtrBillunit();

            hPostTotRecord = hPostTotAmt = 0;
            fileOpen();
            updateTscNotifyLoga();

            backupRtn(); 

            showLogMessage("I", "",
                    String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));


            if (rptCnt > 0) {
                buf = " 註： TSCC回覆結果為：9999，即列入失敗報表，須洽卡友至悠遊卡公司服務台辦理\n";
                lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));
                buf = " 退款金額(扣除服務費)：會有正負項交易\n";
                lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));
            }

            String szTmp1 = comcr.commFormat("3z,3z", cntM1);
            String szTmp2 = comcr.commFormat("3$,3$,3$", amtM1);
            String szTmp3 = comcr.commFormat("3z,3z", cntP1);
            String szTmp4 = comcr.commFormat("3$,3$,3$", amtP1);

            buf = String.format("TSCC回覆餘額轉置結果統計表\n\n\n\n");
            lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));
            buf = String.format("處理日 : %s  \n\n", chinDate);
            lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));
            buf = String.format("餘額轉置成功\\ 負項/退款筆數:%-7.7s 負項/退款金額合計:%-11.11s   正項/請款筆數:%-7.7s 正項/請款金額合計:%-11.11s\n",
                    szTmp1, szTmp2, szTmp3, szTmp4);
            lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

            szTmp1 = comcr.commFormat("3z,3z", cntM2);
            szTmp2 = comcr.commFormat("3$,3$,3$", amtM2);
            szTmp3 = comcr.commFormat("3z,3z", cntP2);
            szTmp4 = comcr.commFormat("3$,3$,3$", amtP2);
            buf = String.format("     餘額轉置失敗 負項/退款筆數:%-7.7s 負項/退款金額合計:%-11.11s   正項/請款筆數:%-7.7s 正項/請款金額合計:%-11.11s\n",
                    szTmp1, szTmp2, szTmp3, szTmp4);
            lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

            String temstr3 = String.format("%s/reports/TscD033R1_%s", comc.getECSHOME(), hTempSystemDate);
            if (lpar2.size() > 0) {
            	;
            	//comcr.insertPtrBatchRpt(lpar2);
                //comc.writeReport(temstr3, lpar2);
            }
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
        hBusiBusinessDate = "";
        sqlCmd = "select business_date, ";
        sqlCmd += "substr(to_char(to_number(business_date)- 19110000,'0000000'),2,7) h_businss_chi_date, ";
        sqlCmd += "substr(to_char(to_number(vouch_date)- 19110000,'0000000'),2,7) h_vouch_chi_date, ";
        sqlCmd += "decode( cast(? as varchar(8))" + ", '' "
                + ",to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'13')" + ", 1" + ", sysdate "
                + ",sysdate - 1 days)" + ", 'yyyymmdd')" + ", ?) h_temp_notify_date, ";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_system_date, ";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTempNotifyDate);
        setString(2, hTempNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
        	if ("".equals(hTempNotifyDate)) {
                hBusiBusinessDate = getValue("business_date");
        	} else {
        		hBusiBusinessDate = hTempNotifyDate;
        	}
            hBusinssChiDate  = getValue("h_businss_chi_date");
            hVouchChiDate = getValue("h_vouch_chi_date");
            hTempNotifyDate = hTempNotifyDate.length() == 0 ? hBusiBusinessDate : hTempNotifyDate;
            //hTempNotifyDate = getValue("h_temp_notify_date");
            hTempSystemDate = getValue("h_temp_system_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
        }

    }

    /***********************************************************************/
    void selectPtrBillunit() throws Exception {
        hBiunConfFlag = "";

        sqlCmd = "select conf_flag ";
        sqlCmd += "from ptr_billunit ";
        sqlCmd += "where bill_unit = substr(?,1,2) ";
        setString(1, fixBillType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billunit not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
        }

    }

    /***********************************************************************/
    int selectTscOrgdataLog() throws Exception {
        sqlCmd = "select count(*) h_cnt, ";
        sqlCmd += "sum(decode(rpt_resp_code, '0000', 0, 1)) h_err_cnt ";
        sqlCmd += "from tsc_orgdata_log  ";
        sqlCmd += "where file_name  = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_orgdata_log not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
            hErrCnt = getValueInt("h_err_cnt");
        }
        return (hErrCnt);
    }

    /***********************************************************************/
    void backupRtn() throws Exception {
        tmpstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
        tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
        tmpstr2 = String.format("%s/media/tsc/backup/%s", comc.getECSHOME(), hTnlgFileName);
        comc.fileRename(tmpstr1, tmpstr2);
    }

    /***********************************************************************/
    void updateTscNotifyLoga() throws Exception {
        daoTable = "tsc_notify_log ";
        updateSQL = "proc_flag = '2', ";
        updateSQL += "proc_date = to_char(sysdate, 'yyyymmdd'), ";
        updateSQL += "proc_time = to_char(sysdate, 'hh24miss'), ";
        updateSQL += "mod_pgm = ?, ";
        updateSQL += "mod_time = sysdate ";
        whereStr = "where file_name = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
//        }

    }

    /***********************************************************************/
    void deleteBilPostcntl() throws Exception {
        daoTable = "bil_postcntl ";
        whereStr = "where this_close_date = ? ";
        whereStr += "and batch_unit = substr(?,1,2) ";
        whereStr += "and mod_pgm = ? ";
        setString(1, hTempNotifyDate);
        setString(2, fixBillType);
        setString(3, javaProgram);
        deleteTable();
    }

    /***********************************************************************/
    int deleteTscOrgdataLog() throws Exception {

        daoTable = "tsc_orgdata_log ";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();
        return (0);
    }

    /***********************************************************************/
    void deleteTscDccgAll() throws Exception {
        daoTable = "tsc_dccg_all a ";
        whereStr = "where to_number(a.tscc_data_seqno) " + "in (select b.tscc_data_seqno " + " from tsc_orgdata_log b "
                + "where b.file_name = ?  ";
        whereStr += "and b.tscc_data_seqno = to_number(a.tscc_data_seqno)) ";
        setString(1, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    void deleteTscDccgPre() throws Exception {
        daoTable = "tsc_dccg_pre a ";
        whereStr = "where to_number(a.tscc_data_seqno) in (select b.tscc_data_seqno from tsc_orgdata_log b where b.file_name  = ? ";
        whereStr += "and b.tscc_data_seqno = to_number(a.tscc_data_seqno)) ";
        setString(1, hTnlgFileName);
        deleteTable();
    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";
        tmpstr1 = String.format("%s", hTnlgFileName);
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", String.format("Open File=[%s]", tmpstr1));
        int f = openInputText(temstr1);
        if(f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }
        
        /*
        String temstr2 = String.format("%s/reports/TscD033R1_%s", comc.getECSHOME(), hTempSystemDate);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        out = openOutputText(temstr2, "MS950");
        if(out == -1)
            comcr.errRtn(temstr2, "檔案開啓失敗！", hCallBatchSeqno);
        */

        printHead();

        selectBilPostcntl();

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
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            tmpstr1 = comcr.getTSCCSeq();
            hCgecTsccDataSeqno = tmpstr1;

            hOrgdOrgData = str600;
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hCgecTscCardNo = comc.rtrim(dtl.tscCardNo);

            if (selectTscVdCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            if (selectDbcCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmtSign));
            hSign = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0201";
                insertTscOrgdataLog();
                continue;
            }
            hCgecDestinationAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hCgecDestinationAmt;
            /*************************************************************************/
            hCgecTscNotiDate = comc.rtrim(dtl.balanceDate);
            /*************************************************************************/
            hCgecPurchaseDate = hTempSystemDate;
            hCgecPurchaseTime = hTempNotifyTime;
            /*************************************************************************/
            hCgecTscRespCode = comc.rtrim(dtl.respCode);
            tmpstr1 = String.format("%s", comc.rtrim(dtl.serviceAmtSign));
            hSigns = tmpstr1;
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
            if (!comc.subMS950String(str600.getBytes("MS950"), 80, 80 + 16).equals(tmpstr2)) {
                hOrgdRptRespCode = "0205";
                showLogMessage("I", "", String.format("HASH values error [%s]", hOrgdRptRespCode));
              
            }
            /*************************************************************************/
            hOrgdRptRespCode = "0000";
            insertTscOrgdataLog();

            /* (悠遊卡退費)請款金額 */
            tmpstr1 = String.format("%s", "悠遊卡退費金額");
            hCgecSeqNo = totalCnt;

            hAmt = hCgecDestinationAmt * -1;
            hAmts = hCgecServiceAmt * -1;
            if (hSign.equals("-")) {
                tmpstr1 = String.format("%s%s%s", "悠遊卡請款金額");
                hCgecTransactionCode = "05";
                hAmt = hCgecDestinationAmt;
                hAmts = hCgecServiceAmt;
            }
            hCgecMerchantChiName =  String.format("%-40.40s", tmpstr1);
            hCgecBillDesc = hCgecMerchantChiName;
            if (hCgecTscRespCode.equals("9999")) {
                hCgecPostFlag = "Y";
            }
            
            insertTscDccgAll();
            
            /*
            if (hSign.equals("-")) {
                insertTscDccgPre();
            } else {
                insertTscDccgAll();
            }
            */

            if (hOrgdRptRespCode.equals("0000")) {
                if (hCgecTransactionCode.equals("06")) {
                    cntM1++;
                    amtM1 = amtM1 + hCgecDestinationAmt;
                } else {
                    cntP1++;
                    amtP1 = amtP1 + hCgecDestinationAmt;
                }
                succCnt++;
                updateTscVdCard();
                showLogMessage("I", "", " autoload="+ hTardAutoloadFlag+","+hTardTscSignFlag);

                if (hCgecTransactionCode.equals("06")) {
                    cntM2++;
                    amtM2 = amtM2 + hCgecDestinationAmt;
                } else {
                    cntP2++;
                    amtP2 = amtP2 + hCgecDestinationAmt;
                }
            }

            if (hCgecTscRespCode.equals("9999") || hCgecTscRespCode.equals("0000")) {
                hIdnoChiName = "";
                hOfficeTel = "";
                hIdnoCellarPhone = "";
                hCardCurrentCode = "";
                hCgprId = "";
                hCgprIdCode = "";
                sqlCmd = "select b.chi_name ";
                sqlCmd += ", b.office_area_code1||'-'||office_tel_no1||'-'|| office_tel_ext1 office_tel ";
                sqlCmd += ", b.cellar_phone ";
                sqlCmd += ", a.current_code ";
                sqlCmd += ", b.id_no ";
                sqlCmd += ", b.id_no_code ";
                sqlCmd += ", b.id_p_seqno ";
                sqlCmd += " from dbc_idno b, dbc_card a ";
                sqlCmd += "where a.id_p_seqno = b.id_p_seqno ";
                sqlCmd += "and a.card_no = ? ";
                setString(1, hCgecCardNo);
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hIdnoChiName = getValue("chi_name");
                    hOfficeTel = getValue("office_tel");
                    hIdnoCellarPhone = getValue("cellar_phone");
                    hCardCurrentCode = getValue("current_code");
                    hCgprId = getValue("id_no");
                    hCgprIdCode = getValue("id_no_code");
                    hCgprIdPSeqno = getValue("id_p_seqno");

                } else {
//                    sqlCmd = "select b.chi_name ";
//                    sqlCmd += ", b.office_area_code1||'-'||office_tel_no1||'-'||office_tel_ext1 office_tel";
//                    sqlCmd += ", b.cellar_phone ";
//                    sqlCmd += ", a.current_code ";
//                    sqlCmd += " from crd_idno b, ecs_crd_card a  ";
//                    sqlCmd += "where a.id_p_seqno = b.id_p_seqno  ";
//                    sqlCmd += "and a.card_no = ? ";
//                    setString(1, hCgecCardNo);
//                    recordCnt = selectTable();
//                    if (recordCnt > 0) {
//                        hIdnoChiName = getValue("chi_name");
//                        hOfficeTel = getValue("office_tel");
//                        hIdnoCellarPhone = getValue("cellar_phone");
//                        hCardCurrentCode = getValue("current_code");
//                    }
                }

                rptCnt++;
                buf = String.format("%-10.10s %-16.16s %-4.4s %-16.16s %-8.8s %12.0f  %13.0f %-16.16s %s\n",
                        hIdnoChiName, hCgecCardNo, hCardCurrentCode, hCgecTscCardNo, hCgecTscNotiDate,
                        hCgecDestinationAmt, hCgecServiceAmt, hOfficeTel, hIdnoCellarPhone);
                lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));
            }
        }
        
        if (totalCnt > 0)
            insertBilPostcntl();
        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    void printHead() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: TscD033R1", 1);
        String szTmp = String.format("%22s", "餘額轉置回應 9999 明細表");
        buf = comcr.insertStrCenter(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "印表日期:", 60);
        buf = comcr.insertStr(buf, chinDate, 70);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "姓名  信用卡號  狀態 悠遊卡號  通知日期 餘額轉置金額 餘額轉置服務費 公司電話  手機", 01);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf,
                "========== ================ ==== ================ ======== ============ ============== ================ ============",
                01);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

    }

    /***********************************************************************/
    void selectBilPostcntl() throws Exception {

        hTempBatchSeq = "";
        sqlCmd = "select nvl(substr( to_char( decode( max(batch_seq), 0, 0, max(batch_seq))+1, '0000'), 2, 4),'0000') h_temp_batch_seq ";
        sqlCmd += "from bil_postcntl ";
        sqlCmd += "where batch_unit = substr(?,1,2) ";
        sqlCmd += "and batch_date = ? ";
        setString(1, fixBillType);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_bil_postcntl not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempBatchSeq = getValue("h_temp_batch_seq");
        }

        hCgecBatchNo  = hBusiBusinessDate + comc.getSubString(fixBillType, 0, 2)
                         + hTempBatchSeq;
        hTempBatchNo  = hCgecBatchNo;
        showLogMessage("I", "", "888 BATCH_NO=" + hCgecBatchNo);
    }

    /***********************************************************************/
    void initTscCgecAll() throws Exception {

        hCgecSeqNo = 0;
        hCgecCardNo = "";
        hCgecTscCardNo = "";
        hCgecBillType = fixBillType;
        hCgecTransactionCode = "06";
        hCgecTscTxCode = "";
        hCgecPurchaseDate = "";
        hCgecPurchaseTime = "";
        hCgecMerchantNo = "EASY8001";
        hCgecMerchantCategory = "4100";
        hCgecMerchantChiName = "";
        hCgecDestinationAmt = 0;
        hCgecDestinationCurrency = "901";
        hCgecBillDesc = "";
        hCgecPostFlag = "N";
        hCgecFileName = "";
        hCgecTscError = "";
        hCgecTscNotiDate = "";
        hCgecTscRespCode = "";
        hCgecReturnSource = "";
        hCgecServiceAmt = 0;
    }

    /***********************************************************************/
    int selectTscVdCard() throws Exception {
        hTardBalanceDate = "";
        hTardBlackltSDate = "";
        hTardBlackltEDate = "";
        hTardAutoloadFlag = "";
        hTardTscSignFlag = "";
        hTardBalanceRtnFee = 0;
        hTempDiffDays = 0;

        sqlCmd = "select vd_card_no, ";
        sqlCmd += "balance_date, ";
        sqlCmd += "balance_rtn_fee, ";
        sqlCmd += "blacklt_s_date, ";
        sqlCmd += "blacklt_e_date, ";
        sqlCmd += "days_between(to_date( ?, 'yyyymmdd') , to_date( ?, 'yyyymmdd')) h_temp_diff_days, ";
        sqlCmd += "autoload_flag, ";
        sqlCmd += "tsc_sign_flag ";
        sqlCmd += "from tsc_vd_card ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempSystemDate);
        setString(2, hCgecPurchaseDate.length() == 0 ? null : hCgecPurchaseDate);
        setString(3, hCgecTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCgecCardNo = getValue("vd_card_no");
            hTardBalanceDate = getValue("balance_date");
            hTardBalanceRtnFee = getValueInt("balance_rtn_fee");
            hTardBlackltSDate = getValue("blacklt_s_date");
            hTardBlackltEDate = getValue("blacklt_e_date");
            hTempDiffDays = getValueInt("h_temp_diff_days");
            hTardAutoloadFlag = getValue("autoload_flag");
            hTardTscSignFlag = getValue("tsc_sign_flag");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    int selectDbcCard() throws Exception {
       
        hCgprIdPSeqno = "";
        sqlCmd = "select major_id_p_seqno ";
        sqlCmd += "from dbc_card ";
        sqlCmd += "where card_no = ? ";
        setString(1, hCgecCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCgprIdPSeqno = getValue("major_id_p_seqno");
   
        } else
            return (1);
        return (0);
    }


    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hCgecTsccDataSeqno);
        setValue("file_iden", "DCBP");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_orgdata_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_orgdata_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertTscDccgPre() throws Exception {
        if (hCgecTransactionCode.equals("06"))
            tmpstr1 = String.format("餘額轉置退款");
        else
            tmpstr1 = String.format("悠遊卡退費退費金額");
        hCgecMerchantChiName = tmpstr1;
        hCgecBillDesc = tmpstr1;
        hCgecSeqNo = totalCnt;

        setValue("card_no", hCgecCardNo);
        setValue("id_p_seqno", hCgprIdPSeqno);
        setValue("tsc_card_no", hCgecTscCardNo);
        setValue("bill_type", hCgecBillType);
        setValue("txn_code", hCgecTransactionCode);
        setValue("tsc_tx_code", hCgecTscTxCode);
        setValue("purchase_date", hCgecPurchaseDate);
        setValue("purchase_time", hCgecPurchaseTime);
        setValue("mcht_no", "EASY8001");
        setValue("mcht_category", "4100");
        setValue("mcht_chi_name", hCgecMerchantChiName);
        setValueDouble("dest_amt", hCgecDestinationAmt);
        setValue("dest_curr", "901");
        setValue("bill_desc", hCgecBillDesc);
        setValue("post_flag", "N");
        setValue("file_name", hTnlgFileName);
        setValue("tsc_error", hOrgdRptRespCode);
        setValue("crt_date", hTempSystemDate);
        setValue("tsc_noti_date", hCgecTscNotiDate);
        setValue("tsc_resp_code", hCgecTscRespCode);
        setValue("tscc_data_seqno", hCgecTsccDataSeqno);
        setValue("online_mark", hCgprOnlineMark);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_dccg_pre";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_dccg_pre duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertTscDccgAll() throws Exception {
    	
    	if (hCgecTransactionCode.equals("06"))
            tmpstr1 = String.format("悠遊卡餘額轉置退款");
        else
            tmpstr1 = String.format("悠遊卡餘額轉置負餘額");
        hCgecMerchantChiName = tmpstr1;
        hCgecBillDesc = tmpstr1;

        setValue("batch_no", hTempBatchNo );
        setValueInt("seq_no", hCgecSeqNo);
        setValue("card_no", hCgecCardNo);
        setValue("tsc_card_no", hCgecTscCardNo);
        setValue("bill_type", hCgecBillType);
        setValue("txn_code", hCgecTransactionCode);
        setValue("tsc_tx_code", hCgecTscTxCode);
        setValue("purchase_date", hCgecPurchaseDate);
        setValue("purchase_time", hCgecPurchaseTime);
        setValue("mcht_no", "EASY8001");
        setValue("mcht_category", "4100");
        setValue("mcht_chi_name", hCgecMerchantChiName);
        setValueDouble("dest_amt", hCgecDestinationAmt);
        setValue("dest_curr", "901");
        setValue("bill_desc", hCgecBillDesc);
        setValue("post_flag", hCgecPostFlag);
        setValue("file_name", hTnlgFileName);
        setValue("tsc_error", hOrgdRptRespCode);
        setValue("crt_date", hTempSystemDate);
        setValue("tscc_data_seqno", hCgecTsccDataSeqno);
        setValue("tsc_noti_date", hCgecTscNotiDate);
        setValue("tsc_resp_code", hCgecTscRespCode);
        setValueDouble("service_amt", hCgecServiceAmt);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_dccg_all";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_dccg_all duplicate!", hCgecBatchNo, hCallBatchSeqno);
        }
        if (hTardBalanceRtnFee > 0) {
            insertDbbOthexp1();
        }

    }

    /***********************************************************************/
    void insertDbbOthexp1() throws Exception {
        if (hTardBalanceRtnFee > 0) {
            hCardAcctType = "";
            hCardPSeqno = "";
            sqlCmd = "select c.acct_type ";
            sqlCmd += ",a.acct_key ";
            sqlCmd += "from dbc_card c, dba_acno a ";
            sqlCmd += "where c.card_no = ?  and c.id_p_seqno = a.id_p_seqno ";
            setString(1, hCgecCardNo);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hCardAcctType = getValue("acct_type");
                hCardPSeqno   = getValue("acct_key");
            } else {
//                sqlCmd = "select c.acct_type, ";
//                sqlCmd += " a.acct_key ";
//                sqlCmd += " from ecs_crd_card c, act_acno a ";
//                sqlCmd += "where c.card_no = ? " + "  and c.id_p_seqno = a.id_p_seqno ";
//                setString(1, hCgecCardNo);
//                recordCnt = selectTable();
//                if (notFound.equals("Y")) {
//                    comcr.errRtn("select_crd_card 3 not found!", "", hCallBatchSeqno);
//                }
//                if (recordCnt > 0) {
//                    hCardAcctType = getValue("acct_type");
//                    hCardPSeqno   = getValue("acct_key");
//                }
            }

            setValue("card_no"      , hCgecCardNo);
            setValue("bill_type"    , "OSSG");
            setValue("txn_code"     , "TS");
            setValue("purchase_date", hBusiBusinessDate);
            setValue("acct_type"    , hCardAcctType);
            setValue("p_seqno"      , hCardPSeqno );
            setValueInt("dest_amt"  , hTardBalanceRtnFee);
            setValue("dest_curr"    , "901");
            setValue("mod_time", sysDate + sysTime);
            setValue("mod_pgm", javaProgram);
            daoTable = "dbb_othexp";
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_dbb_othexp duplicate!", "", hCallBatchSeqno);
            }

        }
    }

    /***********************************************************************/
    void updateTscVdCard() throws Exception {
        daoTable = "tsc_vd_card ";
        updateSQL = "balance_rtn_date = ?, ";
        updateSQL += "balance_amt = ?, ";
        updateSQL += "balance_fee = ?, ";
        updateSQL += "mod_pgm = ?, ";
        updateSQL += "mod_time = sysdate ";
        whereStr = "where tsc_card_no = ? ";
        setString(1, hTempSystemDate);
        setDouble(2, hAmt);
        setDouble(3, hAmts);
        setString(4, javaProgram);
        setString(5, hCgecTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_vd_card not found!", "", hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        setValue("batch_date", hBusiBusinessDate);
        setValue("batch_unit", comc.getSubString(hCgecBillType, 0, 2));
        setValue("batch_seq", hTempBatchSeq);
        setValue("batch_no", hCgecBatchNo );
        setValueDouble("tot_record", hPostTotRecord);
        setValueDouble("tot_amt", hPostTotAmt);
        setValue("confirm_flag_p", hBiunConfFlag.equals("N") ? "Y" : "N");
        setValue("confirm_flag", hBiunConfFlag);
        setValue("this_close_date", hTempNotifyDate);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "bil_postcntl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_postcntl duplicate!", "", hCallBatchSeqno);
        }

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

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscD033 proc = new TscD033();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

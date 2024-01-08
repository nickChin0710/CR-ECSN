/************************************************************************************************
 *                                                                                              *
 *                              MODIFICATION LOG                                                *
 *                                                                                              *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                 *
 *  ---------  --------- ----------- ----------------------------------------------------       *
 *  112/08/25  V1.00.00    Bo Yang              program initial                                 *
 *  112/08/31  V1.00.01    Bo Yang              增加產出兌換抵用回覆檔                                                                                                    *
 *  112/09/08  V1.00.02    Grace                依user要求, 配合其習慣報表代碼, 以為ptrr0000查詢用                            *
 *  112/09/11  V1.00.02    Grace                回覆檔, 變更目錄位置 至 /crdatacrea                     *
 *             V1.00.03    Bo Yang              修改報表[信用卡號]欄位數據來源                                                                                  *
 *  112/09/13  V1.00.04    Bo Yang              讀不到信用卡號, 仍以crd_card.major_card_no帶入                            *
 *  112/09/15  V1.00.05    Grace                寫檔至 mkt_cashbak_dtl, fund_code 設定於mktm4070作業, *
 *                                              做為溢付款, 統一由帳務模組處理                                                                                  *
 *                                              帳務年月/日改以 文字檔案.txn_date(兌換日.年月/日)           *
 *  112/09/26  V1.00.06    Zuwei Su             報表增加小計和本月總計                                                                                                    *
 *  112/10/04  V1.00.07    Grace                1. 取消comcr.deletePtrBatchRpt(rptIdR1, sysDate), 因造成同一天處理多檔時, 僅保留最新資料             *  
 *                                              --> 如一天執行多檔, 或重複執行, 得手動處理mkt_cashback_dtl、ptr_batch_rpt                  *
 *                                              2. 增小計/總計筆數、當月總計起迄日期                                                                                                                                                                 *
 *  112/11/06  V1.00.08    Grace                總計欄位, 排除ActE030的資料內容                                                                                                                                                        *
 *  112/12/11  V1.00.09    Zuwei Su             處理當月多個檔案,營業日當月1日 ~ 營業日                                                                                                                                                        *
 *******************************************************************************************************************************/
package Mkt;

import Dxc.Util.SecurityUtil;
import com.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MktW005 extends AccessDAO {
    private boolean DEBUG = false;
    private final String PROGNAME = "總行會員金庫幣兌換刷卡金接收寫報表處理 112/09/13 V1.00.04";
    private CommCrd comc = new CommCrd();
    private CommDate commDate = new CommDate();
    private CommString commString = new CommString();
    private CommCrd commCrd = new CommCrd();
    private CommRoutine comr;
    private CommCrdRoutine comcr;

    //    private final String CRM_FOLDER = "D:/TCBMFT/MKTMPP/FromMPP/RD";
    private final String DATA_FORM = "RD-CARDYYYYMMDD";
    private final String FILE_FOLDER = "/media/mkt";
    private final String FILE_NAME_INFO = "MktW005_01";
    private final String FILE_NAME_EX = "MktW005_02";
    private final String FILE_NAME_RESPONSE = "BRD-CARDYYYYMMDD";
    private final String lineSeparator = System.lineSeparator();

    private String searchDate = "";		//營業日
    private String acctDate = "";		//帳務日		
    private String vouchDate = "";		//會計分錄日		
    
    List<Map<String, Object>> lparR1 = new ArrayList<>();
    List<Map<String, Object>> lparR2 = new ArrayList<>();
    //String rptIdR1 = FILE_NAME_INFO;
    //String rptIdR2 = FILE_NAME_EX;
    String rptIdR1 = "CRD120D";        //20230908, grace
    String rptIdR2 = "CRD120E";        //20230908, grace
    String rptNameR1 = "兌換入帳明細表";
    String rptNameR2 = "兌換入帳異常明細表";
    int rptSeqR1 = 0;
    int rptSeqR2 = 0;

    int totCnt = 0;
    int infoCnt = 0;
    int exCnt = 0;
    
    int sum_no_month = 0;
    int sum_amt_month = 0;

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            showLogMessage("I", "", "Usage MktW005 [business_date]");

            if (!connectDataBase()) {
//                comc.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error");
                exitProgram(1);
            }
            // =====================================
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            // get searchDate, acctDate
            selectPtrBusinday();
            acctDate = vouchDate;	//帳務日

            if (args.length >= 1) {
                searchDate = args[0];
                showLogMessage("I", "", String.format("程式參數1: [%s]", searchDate));
            } else {
                searchDate = businessDate;
            }

            if (!commDate.isDate(searchDate)) {
                showLogMessage("I", "", "請傳入參數合格值: YYYYMMDD");
                return -1;
            }
            
            // 處理當月多個檔案,營業日當月1日 ~ 營業日
            String lastDate = searchDate;
            searchDate = searchDate.substring(0, 6) + "01";
            while(searchDate.compareTo(lastDate) <= 0) {
                showLogMessage("I", "", "-----------------------------------------------------------------------------------");
                showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
                //showLogMessage("I", "", String.format("會計分錄日期[%s]", acctDate));
//                rptIdR1 += ("_" + searchDate);
//                rptIdR2 += ("_" + searchDate);
                rptIdR1 = "CRD120D_" + searchDate;
                rptIdR2 = "CRD120E_" + searchDate;
                // init
                dateTime();
                lparR1 = new ArrayList<>();
                lparR2 = new ArrayList<>();
                totCnt = 0;
                infoCnt = 0;
                exCnt = 0;
                rptSeqR1 = 0;
                rptSeqR2 = 0;
                sum_no_month = 0;
                sum_amt_month = 0;
    
                String fileName = DATA_FORM.replace("YYYYMMDD", searchDate);
                // get the name and the path of the .DAT file
                if (openFile(fileName) == 0) {
                    readFile(fileName);
                    backup(fileName);
                }
    
                showLogMessage("I", "", searchDate + "執行結束, 總計筆數=[" + totCnt + "]");
                searchDate = commDate.dateAdd(searchDate, 0, 0, 1);
            }
            return 0;
        } catch (Exception e) {
            expMethod = "mainProcess";
            expHandle(e);
            return exceptExit;
        } finally {
            finalProcess();
        }
    }

    private void selectPtrBusinday() throws Exception {
        //sqlCmd = "select BUSINESS_DATE from PTR_BUSINDAY ";
        sqlCmd = "select BUSINESS_DATE, VOUCH_DATE from PTR_BUSINDAY ";	//增vouch_date做為帳務日
        selectTable();

        if (notFound.equals("Y")) {
//            comc.errExit("執行結束, (PTR_BUSINDAY) 營業日為空!!", "");
            showLogMessage("I", "", "執行結束, (PTR_BUSINDAY) 營業日為空!!");
            exitProgram(1);
        }

        businessDate = getValue("BUSINESS_DATE");
        vouchDate = getValue("VOUCH_DATE");			//會計分錄日
    }

    //=============================================================================
    int openFile(String filename) {
        //String path = String.format("%s/%s", CRM_FOLDER, filename);
        String path = String.format("%s%s/%s", comc.getECSHOME(), FILE_FOLDER, filename);
        if (DEBUG) {
            path = String.format("%s%s/%s", comc.getECSHOME(), FILE_FOLDER, filename);
        }
        path = Normalizer.normalize(path, Normalizer.Form.NFKD);

        int rec = openInputText(path);
        if (rec == -1) {
            showLogMessage("D", "", "無檔案可處理  " + "");
            return 1;
        }

        closeInputText(rec);
        return (0);
    }

    //=============================================================================
    void readFile(String filename) throws Exception {
        showLogMessage("I", "", "                                 ");
        showLogMessage("I", "", "======== Start Read File ========");
        BufferedReader bufferedReader;
        try {
            //String tmpStr = String.format("%s/%s", CRM_FOLDER, filename);
            String tmpStr = String.format("%s%s/%s", comc.getECSHOME(), FILE_FOLDER, filename);

            if (DEBUG) {
                tmpStr = String.format("%s%s/%s", comc.getECSHOME(), FILE_FOLDER, filename);
            }
            String tempPath = SecurityUtil.verifyPath(tmpStr);
            FileInputStream fileInputStream = new FileInputStream(tempPath);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "MS950"));

            showLogMessage("I", "", "  tempPath = [" + tempPath + "]");
        } catch (FileNotFoundException exception) {
            showLogMessage("I", "", "bufferedReader exception: " + exception.getMessage());
            return;
        }

        String lineLength;
        String bufEx = ""; // 兌換入帳異常表
        String bufInfo = ""; // 兌換入帳明細表
        String bufResponse = ""; // 兌換抵用回覆檔
        int sum = 0; // 當天折抵金額小計
        int sum_no = 0; // 當天折抵金額筆數
        String acctMonth = null;
        String txnDate = null;		//明細內容的txnDate

        bufEx = headerEx(bufEx);
        bufInfo = headerInfo(bufInfo);
        while ((lineLength = bufferedReader.readLine()) != null) {
            if (lineLength.length() < 10) {
                continue;
            }
            byte[] bytes = lineLength.getBytes("MS950");
            String leadingChar = comc.subMS950String(bytes, 0, 3).trim();
            Buf1 buf1 = new Buf1();

            // 前三碼為'FEH' or 'FEE'者用於產出兌換抵用回覆檔
            if ("FEH".equals(leadingChar)) {
                buf1.dataDate = comc.subMS950String(bytes, 3, 8).trim();
                buf1.sourceId = comc.subMS950String(bytes, 11, 5).trim();
                buf1.spaceH = comc.subMS950String(bytes, 16, 140).trim();
                bufResponse += comc.fixLeft("FEH", 3);
                bufResponse += comc.fixLeft(buf1.dataDate, 8);
                bufResponse += comc.fixLeft(buf1.sourceId, 5);
                bufResponse += comc.fixLeft(buf1.spaceH, 184);
                bufResponse += lineSeparator;
                continue;
            } else if ("FEE".equals(leadingChar)) {
                buf1.spaceF = comc.subMS950String(bytes, 11, 145).trim();
                bufResponse += comc.fixLeft("FEE", 3);
                bufResponse += comc.fixLeft("00000000", 8);
                bufResponse += comc.fixLeft(buf1.spaceF, 189);
                bufResponse += lineSeparator;
                continue;
            }

            // 處理檔案前3碼 = 'FED'者
            if ("FED".equals(leadingChar)) {
                totCnt++;
                buf1.txnDate = comc.subMS950String(bytes, 3, 8).trim();
                txnDate = comc.subMS950String(bytes, 3, 8).trim();		//做為總計起迄期間用
                if (acctMonth == null && buf1.txnDate .length() > 0) {
                    acctMonth = buf1.txnDate.substring(0, 6);
                }
                buf1.txnSeq = comc.subMS950String(bytes, 11, 20).trim();
                buf1.id = comc.subMS950String(bytes, 31, 10).trim();
                buf1.redeemTimes = comc.subMS950String(bytes, 41, 10).trim();
                buf1.redeemPoint = comc.subMS950String(bytes, 51, 10).trim();
                buf1.goodsId = comc.subMS950String(bytes, 61, 10).trim();
                buf1.spaceD = comc.subMS950String(bytes, 71, 85).trim();

                if (dataCheck(buf1)) {
                    infoCnt++;
                    String temp = "";
                    temp += commCrd.fixRight(buf1.txnDate, 17);
                    temp += commCrd.fixRight(buf1.id, 30);
                    temp += commCrd.fixRight(String.valueOf(Integer.valueOf(buf1.redeemTimes)), 26);
                    sum += Integer.valueOf(buf1.redeemTimes);	//小計金額
                    sum_no += 1;								//小計筆數
                    temp += commCrd.fixRight("", 20);
                    temp += commCrd.fixLeft(buf1.cardNo, 39);
                    temp += lineSeparator;
                    bufInfo += temp;

                    for (String s : temp.split(lineSeparator)) {
                        lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, rptSeqR1++, "1", s));                        
                    }
                    //showLogMessage("I", "", String.format("寫入兌換入帳明細表, ID:[%s]", buf1.id));
                } else {
                    exCnt++;
                    String temp = "";
                    temp += commCrd.fixRight(buf1.txnDate, 17);
                    temp += commCrd.fixRight(buf1.id, 30);
                    temp += commCrd.fixRight(String.valueOf(Integer.valueOf(buf1.redeemTimes)), 26);
                    temp += commCrd.fixRight("", 20);
                    temp += commCrd.fixLeft(buf1.responseDesc, 39);
                    temp += lineSeparator;
                    bufEx += temp;

                    for (String s : temp.split(lineSeparator)) {
                        lparR2.add(comcr.putReport(rptIdR2, rptNameR2, sysDate + sysTime, rptSeqR2++, "1", s));
                    }
                    showLogMessage("I", "", String.format("寫入兌換入帳異常明細表, ID:[%s], 異常原因:[%s]", buf1.id, buf1.responseDesc));
                }

                // 產出兌換抵用回覆檔
                bufResponse += commCrd.fixLeft("FED", 3);
                bufResponse += commCrd.fixLeft(buf1.txnDate, 8);
                bufResponse += commCrd.fixLeft(buf1.txnSeq, 20);
                bufResponse += commCrd.fixLeft(buf1.id, 10);
                bufResponse += commString.lpad(buf1.redeemTimes, 10, "0");
                bufResponse += commString.lpad(buf1.redeemPoint, 10, "0");
                bufResponse += commCrd.fixLeft(buf1.goodsId, 10);
                bufResponse += commCrd.fixLeft(buf1.spaceD, 85);
                bufResponse += commCrd.fixLeft(buf1.responseCode, 4);
                bufResponse += commCrd.fixLeft(buf1.responseDesc, 40);
                bufResponse += lineSeparator;

                if (commString.empty(buf1.responseDesc)) {
                    insertMktCashbackDtl(buf1);
                }
            }
        }

        showLogMessage("I", "", "======== End Read File ========");
        showLogMessage("I", "", "                               ");
        if (totCnt == 0) {
            showLogMessage("I", "", " RD-CARDYYYYMMDD 檔案內容空檔 !! ");
        }
        if (infoCnt == 0) {
            String temp = "";
            temp += "*** 查無資料 ***";
            temp += lineSeparator;
            bufInfo += temp;
            lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, rptSeqR1++, "1", temp));
        } else {
            // 寫入小計和總計，總計從表中獲取
            String str = "";
            //str = "  小計：" + comc.fixRight(String.format("%,d", sum), 60) + "元" + lineSeparator;
            str = "  小計：" 
            	+ comc.fixRight(String.format("%,d", sum_no), 55) + "筆"
                + comc.fixRight(String.format("%,d", sum), 20) + "元" + lineSeparator;
            bufInfo += str;
            lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, rptSeqR1++, "1", str));
            // 總計，查詢當月1~當天的匯總金額
            //int endTranAmtSum = selectMktCashbackDtlSum(acctMonth);
            selectMktCashbackDtlSum(acctMonth);
            //str = "  本月總計：" + comc.fixRight(String.format("%,d", endTranAmtSum), 56) + "元" + lineSeparator;
            str = "  本月總計 "
            		//+ String.format("%,s", acctMonth+"01") + " ~ " + String.format("%,s", searchDate) + " : "
            		//+ String.format("%,d", acctMonth) + "01 ~ " + String.format("%,d", searchDate) + " : "
            		//+ String.format("%s", acctMonth) + "01 ～ " + String.format("%s", searchDate) + " : "
            		//+ acctMonth + "01 ~ " + searchDate + " : "
            		//+ commDate.toTwDate(acctMonth).substring(0, 3) + "/" +  + "01 ~ "
            		+ (Integer.parseInt(acctMonth.substring(0, 4)) - 1911) + "/" + acctMonth.substring(4, 6) + "/" + "01 ~~ "
            		+ commDate.toTwDate(txnDate).substring(0, 3) + "/" + txnDate.substring(4, 6) + "/" + txnDate.substring(6, 8) + " : " 
            		+ comc.fixRight(String.format("%,d", sum_no_month), 27) + "筆"
            		+ comc.fixRight(String.format("%,d", sum_amt_month), 20) + "元" + lineSeparator;
            bufInfo += str;
            lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, rptSeqR1++, "1", str));
        }
        if (exCnt == 0) {
            String temp = "";
            temp += "*** 查無資料 ***";
            temp += lineSeparator;
            bufEx += temp;
            lparR2.add(comcr.putReport(rptIdR2, rptNameR2, sysDate + sysTime, rptSeqR2++, "1", temp));
        }

        bufferedReader.close();

        // 寫入[兌換入帳明細表]
        String datFileInfoPath = Paths.get(comc.getECSHOME() + FILE_FOLDER, FILE_NAME_INFO).toString();
        boolean isOpenInfo = openBinaryOutput(datFileInfoPath);
        if (!isOpenInfo) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFileInfoPath));
            return;
        }
        byte[] tmpBytesInfo = bufInfo.getBytes("MS950");
        writeBinFile(tmpBytesInfo, tmpBytesInfo.length);
        closeBinaryOutput();
        showLogMessage("I", "", String.format("寫入兌換入帳明細表結束, 總計筆數[%s]", infoCnt));
        // 寫入ptr_batch_rpt
        //comcr.deletePtrBatchRpt(rptIdR1, sysDate);	//暫不delete, 做為一日執行多次檔案識別 (grace, 20231004)	
        comcr.insertPtrBatchRpt(lparR1);
        ftpProc(FILE_NAME_INFO, "NCR2EMP", "NCR2EMP");


        // 寫入[兌換入帳異常表]
        String datFileExPath = Paths.get(comc.getECSHOME() + FILE_FOLDER, FILE_NAME_EX).toString();
        boolean isOpenEx = openBinaryOutput(datFileExPath);
        if (!isOpenEx) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFileExPath));
            return;
        }
        byte[] tmpBytesEx = bufEx.getBytes("MS950");
        writeBinFile(tmpBytesEx, tmpBytesEx.length);
        closeBinaryOutput();
        showLogMessage("I", "", "============================================================");
        showLogMessage("I", "", String.format("寫入兌換入帳異常明細表結束, 總計筆數[%s]", exCnt));
        // 寫入ptr_batch_rpt
        //comcr.deletePtrBatchRpt(rptIdR2, sysDate);	//暫不delete, 做為一日執行多次檔案識別 (grace, 20231004)
        comcr.insertPtrBatchRpt(lparR2);
        ftpProc(FILE_NAME_EX, "NCR2EMP", "NCR2EMP");

        // 產出兌換抵用回覆檔
        String filenameResponse = FILE_NAME_RESPONSE.replace("YYYYMMDD", searchDate);
        String datFileResponsePath = Paths.get(comc.getECSHOME() + FILE_FOLDER, filenameResponse).toString();
        boolean isOpenResponse = openBinaryOutput(datFileResponsePath);
        if (!isOpenResponse) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFileResponsePath));
            return;
        }
        byte[] tmpBytesResponse = bufResponse.getBytes("MS950");
        writeBinFile(tmpBytesResponse, tmpBytesResponse.length);
        closeBinaryOutput();

        showLogMessage("I", "", "============================================================");
        showLogMessage("I", "", "寫入兌換抵用回覆檔結束");
        //ftpProc(filenameResponse, "NCR2TCB", "NCR2TCB");
        ftpProc(filenameResponse, "CRDATACREA", "CRDATACREA");    //20230911, grace變更目錄
    }

    private String headerInfo(String str) throws Exception {
        str += "分行代號: 3144 信用卡部" + commCrd.fixRight("總行會員點數兌換刷卡金入帳明細表", 61) + commCrd.fixRight("保存年限: 五年", 48) + lineSeparator;
        //str += "報表代號: CRD120D      科目代號:" + commCrd.fixRight(String.format("中 華 民 國: %s 年 %2$s 月 %3$s 日", commDate.toTwDate(searchDate).substring(0, 3), searchDate.substring(4, 6), searchDate.substring(6, 8)), 79) + commCrd.fixRight("第 0001 頁", 21) + lineSeparator;
        str += "報表代號: CRD120D      科目代號:" + commCrd.fixRight(String.format("中 華 民 國: %s 年 %2$s 月 %3$s 日", commDate.toTwDate(searchDate).substring(0, 3), searchDate.substring(4, 6), searchDate.substring(6, 8)), 51) + commCrd.fixRight("第 0001 頁", 49) + lineSeparator;
        str += lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;
        str += "         交易日期                    身份證號                    折抵金額                    信用卡號                    " + lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;

        for (String s : str.split(lineSeparator)) {
            lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, rptSeqR1++, "0", s));
        }

        return str;
    }

    private String headerEx(String str) throws Exception {
        str += "分行代號: 3144 信用卡部" + commCrd.fixRight("總行會員點數兌換刷卡金入帳異常明細表", 62) + commCrd.fixRight("保存年限: 五年", 47) + lineSeparator;
        //str += "報表代號: CRD120E      科目代號:" + commCrd.fixRight(String.format("中 華 民 國: %s 年 %2$s 月 %3$s 日", commDate.toTwDate(searchDate).substring(0, 3), searchDate.substring(4, 6), searchDate.substring(6, 8)), 79) + commCrd.fixRight("第 0001 頁", 21) + lineSeparator;
        str += "報表代號: CRD120E      科目代號:" + commCrd.fixRight(String.format("中 華 民 國: %s 年 %2$s 月 %3$s 日", commDate.toTwDate(searchDate).substring(0, 3), searchDate.substring(4, 6), searchDate.substring(6, 8)), 51) + commCrd.fixRight("第 0001 頁", 49) + lineSeparator;        
        str += lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;
        str += "         交易日期                    身份證號                    折抵金額                    異常原因                    " + lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;

        for (String s : str.split(lineSeparator)) {
            lparR2.add(comcr.putReport(rptIdR2, rptNameR2, sysDate + sysTime, rptSeqR2++, "0", s));
        }

        return str;
    }

    private boolean dataCheck(Buf1 buf1) throws Exception {
        if (!commString.isNumber(buf1.txnDate)) {
            buf1.responseCode = "C003";
            buf1.responseDesc = "兌換日期 非數字";
            return false;
        }

        if (!commString.isNumber(buf1.redeemTimes)) {
            buf1.responseCode = "C004";
            buf1.responseDesc = "抵用金額 非數字";
            return false;
        }

        if (!dataCheckCrdIdno(buf1)) {
            buf1.responseCode = "C001";
            buf1.responseDesc = "不存在卡人檔(crd_idno)";
            return false;
        }

        int retCode = dataCheckCrdCard(buf1);
        if (retCode == 1) {
            buf1.responseCode = "C001";
            buf1.responseDesc = "無卡片資料";
            return false;
        } else if (retCode == 2) {
            buf1.responseCode = "C002";
            buf1.responseDesc = "無流通卡資料";
            return false;
        }

        buf1.responseCode = "0000";
        buf1.responseDesc = "";
        return true;
    }

    private boolean dataCheckCrdIdno(Buf1 buf1) throws Exception {
        sqlCmd = "select id_p_seqno from crd_idno " +
                " where id_no = ? ";
        setString(1, buf1.id);

        selectTable();

        return !notFound.equals("Y");
    }

    private int dataCheckCrdCard(Buf1 buf1) throws Exception {
        int ret = 1;
        sqlCmd = "select b.major_card_no, b.p_seqno, b.id_p_seqno, b.current_code " +
                " from crd_idno a, crd_card b " +
                " where a.id_p_seqno = b.id_p_seqno " +
                " and a.id_no = ? ";
        setString(1, buf1.id);

        openCursor();
        while (fetchTable()) {
            String currentCode = getValue("current_code");
            if (currentCode.equals("0")) {
                buf1.cardNo = selectCardNo(buf1.id);
                if (commString.empty(buf1.cardNo)) {
                    buf1.cardNo = getValue("major_card_no");
                }
                buf1.pSeqno = getValue("p_seqno");
                buf1.idPSeqno = getValue("id_p_seqno");

                ret = 0;
                break;
            } else {
                ret = 2;
            }
        }
        closeCursor();

        return ret;
    }

    private String selectCardNo(String id) throws Exception {
        sqlCmd = "select distinct b.card_no " +
                " from act_debt b, " +
                "      ptr_actcode pa, " +
                "      ptr_workday pw, " +
                "      act_acno c " +
                " where b.stmt_cycle = pw.stmt_cycle " +
                "   and b.acct_month = pw.this_acct_month " + // 比對資料當月為帳務月
                "   and b.acct_code = pa.acct_code " +
                "   and b.acno_p_seqno = c.acno_p_seqno " +
                "   and decode(pa.interest_method, 'Y', b.end_bal, 0) > 0 " + // 欠款金額>0
                "   and b.card_no in ( " +
                "       select distinct b.card_no " +
                "       from crd_idno a, crd_card b " +
                "       where a.id_p_seqno = b.id_p_seqno " +
                "         and b.current_code = '0' " + // 流通卡
                "         and a.id_no = ? " + //檔案ID
                "   ) " +
                " order by b.card_no " +
                " fetch first 1 rows only ";
        setString(1, id);

        if (selectTable() > 0) {
            return getValue("card_no");
        }

        return "";
    }
    
    private int selectMktCashbackDtlSum(String acctMonth) throws Exception {
        //sqlCmd = "select sum(end_tran_amt) as end_tran_amt_sum from mkt_cashback_dtl where acct_month = ? ";
        sqlCmd = " select count(*) as sum_no_month, sum(end_tran_amt) as end_tran_amt_sum "
        		+ " from mkt_cashback_dtl "
        		+ " where acct_month = ? "
        		+ "   and acct_date < ? "
        		+ "   and end_tran_amt <> 0 "		//因有ActE030作業, 存在相同fund_code
        		+ "   and fund_code = '0501000001' ";
        setString(1, acctMonth);
        setString(2, searchDate);
        int cnt = selectTable();
        if (cnt > 0) {
        	//return getValueInt("end_tran_amt_sum");
        	sum_amt_month = getValueInt("end_tran_amt_sum");	//當月至今合計
        	sum_no_month = getValueInt("sum_no_month");			//當月筆數
        }
        return 0;
    }

    private void insertMktCashbackDtl(Buf1 buf1) throws Exception {
        dateTime();
        extendField = "fund.";
        String tranSeqno = comr.getSeqno("mkt_modseq");

        setValue("fund.tran_date", sysDate);
        setValue("fund.tran_time", sysTime);
        //setValue("fund.fund_code", "0501");
        setValue("fund.fund_code", "0501000001");	//20230915,設定同mktm4070
        setValue("fund.fund_name", "會員點數兌換刷卡金");
        setValue("fund.p_seqno", buf1.pSeqno);
        setValue("fund.id_p_seqno", buf1.idPSeqno);
        setValue("fund.acct_type", buf1.acctType);
        setValue("fund.tran_code", "1");
        setValue("fund.tran_pgm", javaProgram);
        setValue("fund.beg_tran_amt", buf1.redeemTimes);
        setValue("fund.end_tran_amt", buf1.redeemTimes);
        setValue("fund.res_s_month", "");
        setValue("fund.res_tran_amt", "0");
        setValue("fund.res_total_cnt", "0");
        setValue("fund.res_tran_cnt", "0");
        setValue("fund.res_upd_date", "");
        //setValue("fund.acct_month", businessDate.substring(0, 6));
        setValue("fund.acct_month", buf1.txnDate.substring(0, 6));	//帳務日改以檔案.txn_date(兌換日.年月)
        //setValue("fund.acct_month", acctDate.substring(0, 6));			//帳務日改以ptr_businday.vouch_date(帳務日)
        setValue("fund.effect_e_date", "");
        setValue("fund.tran_seqno", tranSeqno);
        setValue("fund.proc_month", businessDate.substring(0, 6));
        //setValue("fund.acct_date", businessDate);
        setValue("fund.acct_date", buf1.txnDate);	//帳務日改以檔案.txn_date(兌換日)
        //setValue("fund.acct_date", acctDate);	//帳務日改以ptr_businday.vouch_date(帳務日)
        setValue("fund.mod_desc", "總行會員金庫幣(MPP)兌換刷卡金");
        setValue("fund.mod_memo", "");
        setValue("fund.mod_reason", "");
        setValue("fund.case_list_flag", "N");
        setValue("fund.crt_date", sysDate);
        setValue("fund.crt_user", javaProgram);
        setValue("fund.apr_date", sysDate);
        setValue("fund.apr_user", javaProgram);
        setValue("fund.apr_flag", "Y");
        setValue("fund.mod_user", javaProgram);
        setValue("fund.mod_time", sysDate + sysTime);
        setValue("fund.mod_pgm", javaProgram);
        daoTable = "mkt_cashback_dtl";

        insertTable();
    }

    // ************************************************************************
    private void ftpProc(String filename, String systemId, String refIpCode) throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = systemId; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME() + FILE_FOLDER;    //相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;

        String hEflgRefIpCode = refIpCode; //"NCR2TCB";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

        String tmpChar = "put " + filename;

        int errCode = commFTP.ftplogName(hEflgRefIpCode, tmpChar);

        if (errCode != 0) {
            showLogMessage("I", "", "檔案傳送 " + hEflgRefIpCode + " 有誤(error), 請通知相關人員處理");
            //showLogMessage("I", "", "MktW005 執行完成 傳送EMP失敗[" + filename + "]");
            showLogMessage("I", "", "MktW005 執行完成 傳送"+ refIpCode +"失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        // 刪除檔案 put 不用刪除
        backup(filename);
    }

    /****************************************************************************/
    private void backup(String removeFileName) {
        String tmpStr1 = comc.getECSHOME() + FILE_FOLDER + "/" + removeFileName;
        String tempPath1 = SecurityUtil.verifyPath(tmpStr1);
        String tmpStr2 = comc.getECSHOME() + FILE_FOLDER + "/backup/" + String.format(removeFileName + "_%s", sysDate + sysTime);
        String tempPath2 = SecurityUtil.verifyPath(tmpStr2);

        if (!comc.fileCopy(tempPath1, tempPath2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]備份失敗!");
            return;
        }
        comc.fileDelete(tmpStr1);
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 備份至 [" + tempPath2 + "]");
    }

    public static void main(String[] args) {
        MktW005 proc = new MktW005();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }


    private class Buf1 {
        String dataDate; // 資料日期
        String sourceId; // 類型
        String spaceH; // 保留欄位 (Header)

        String txnDate;
        String txnSeq;
        String id;
        String redeemTimes;
        String redeemPoint;
        String goodsId;
        String spaceD; // 保留欄位 (Detail)
        String responseCode;      // 主機回應碼
        String responseDesc;      // 主機回應訊息

        String spaceF; // 保留欄位 (Footer)

        String pSeqno;        // 帳戶流水號
        String idPSeqno;      // 身份證號序號
        String acctType;      // 帳戶帳號類別碼
        String cardNo;        // 信用卡號
    }
}

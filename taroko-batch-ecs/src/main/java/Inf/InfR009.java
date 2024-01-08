/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/02/28  V1.00.00   Zuwei      program initial                          *
*  112/03/14  V1.00.01   Zuwei      欄位長度調整                             *
*  112/03/14  V1.00.02   Wilon      產檔路徑調整                             *
*  112/03/21  V1.00.03   Wilson     mark procFTP                             *
*  112/03/27  V1.00.04   Wilson     欄位值定義調整                           *
*  112/04/13  V1.00.05   Wilson     循環信用金額、本月消費金額調整           *
*  112/04/17  V1.00.06   Wilson     可用餘額邏輯調整                         *
*  112/04/20  V1.00.07   Zuwei      每個月的最後一天要多產生兩個檔案         *
*  112/04/21  V1.00.08   Wilson     birthday改成getSubString                  *
*  112/05/21  V1.00.09   Wilson     日期減一天                                                                                              *
*  112/05/31  V1.00.10   Wilson     WHERE條件增加串CURR_CODE                    *
*  112/07/07  V1.00.11   Wilson     調整ACT_ACCT_CURR條件                                                              *
*  112/08/03  V1.00.12   Wilson     調整讀取雙幣卡邏輯                                                                               *
*  112/08/17  V1.00.13   Ryan       selectActCurrHst by curr_code            *                                                                               *
*  112/08/22  V1.00.14   Zuwei Su   三個產出檔合併到一個method，只讀一次資料 *                                                                               *
*  112/09/07  V1.00.15   Zuwei Su   删除debug讯息，產出檔method增加close lastday的兩個檔 *                                                                               *
*  112/09/15  V1.00.16   Zuwei Su   部分SELECT SQL改成先load memory後再處理 * 
*  112/09/21  V1.00.17   Wilson     調整欄位處理邏輯                                                                                    *                                                                              *
*****************************************************************************/
package Inf;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommTxInf;
import Cca.CalBalance;

public class InfR009 extends AccessDAO {
    private static final int OUTPUT_BUFF_SIZE = 5000;
    private final String progname = "產生送CRM TCBRP18程式  112/09/21 V1.00.17";
    private static final String CRM_FOLDER = "/media/crm/";
    private static final String RM_FOLDER = "/media/crm/";
    private static final String DATA_FORM = "TCBRP18";
    private final String lineSeparator = System.lineSeparator();
    private String ctrlCodeType = "";

    CommCrd commCrd = new CommCrd();
    CommDate commDate = new CommDate();
    CalBalance calBalance = null;

    public int mainProcess(String[] args) {

        try {
            CommCrd comc = new CommCrd();
            calBalance = new CalBalance(conn, getDBalias());
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================

            // get searchDate
            String orlSearchDate = (args.length == 0) ? "" : args[0].trim();
            showLogMessage("I", "", String.format("程式參數1[%s]", orlSearchDate));
            orlSearchDate = getProgDate(orlSearchDate, "D");

            String nextDate = commDate.dateAdd(orlSearchDate, 0, 0, 1);
            boolean isLastDay = !nextDate.substring(4, 6).equals(orlSearchDate.substring(4, 6));
            
			//日期減一天
            String searchDate = commDate.dateAdd(orlSearchDate, 0, 0, -1);
            showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
           
            // convert YYYYMMDD into YYMMDD
            String fileNameDate = searchDate.substring(2);

            // get the name and the path of the .DAT file
            String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameDate,
                    CommTxInf.DAT_EXTENSION);
            String fileFolder = Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();

            // 產生主要檔案 .DAT
//            int dataCount = generateDatFile(fileFolder, datFileName)
            
            String rmfile1 = String.format("%s%s.TXT", "CRM09", orlSearchDate.substring(2,6));
            String rmfile2 = String.format("%s%s", "CRM09", orlSearchDate);
            String rmFolder = Paths.get(commCrd.getECSHOME(),RM_FOLDER).toString();

            int dataCount = generateFile(fileFolder, datFileName, isLastDay, rmFolder, rmfile1, rmfile2);
//
//            // 產生Header檔
            CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
            dateTime(); // update the system date and time
            boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate,
                    sysDate, sysTime.substring(0, 4), dataCount);
            if (isGenerated == false) {
                comc.errExit("產生HDR檔錯誤!", "");
            }

            // CR_STATUS_YYMMDD.DAT -> CR_STATUS_YYMMDD.HDR
            String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);          
            
            // run FTP
            procFTP(fileFolder, datFileName, hdrFileName);
            
            if(isLastDay) {
    			ftpProc(rmFolder,rmfile1);
    			ftpProc(rmFolder,rmfile2);
            }

            showLogMessage("I", "", "執行結束");
            return 0;
        } catch (Exception e) {
            expMethod = "mainProcess";
            expHandle(e);
            return exceptExit;
        } finally {
            finalProcess();
        }
    }

    /**
     * generate a .Dat file
     * 
     * @param fileFolder 檔案的資料夾路徑
     * @param datFileName .dat檔的檔名
     * @return the number of rows written. If the returned value is -1, it means the path or the
     *         file does not exist.
     * @throws Exception
     */
    private int generateDatFile(String fileFolder, String datFileName)
            throws Exception {

        String datFilePath = Paths.get(fileFolder, datFileName).toString();
        boolean isOpen = openBinaryOutput(datFilePath);
        if (isOpen == false) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
            return -1;
        }

        int rowCount = 0;
        int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a
                                   // specified value
        try {
            StringBuffer sb = new StringBuffer();
            showLogMessage("I", "", "開始產生.DAT檔......");

            // 處理信用卡及VD卡資料
            showLogMessage("I", "", "開始處理信用卡及VD卡資料檔......");
            selectCreditCardAndVdCardData(null, null);
            while (fetchTable()) {
                String rowOfDAT = getRowOfDAT();
                sb.append(rowOfDAT);
                rowCount++;
                countInEachBuffer++;
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                    showLogMessage("I", "",
                            String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
                    byte[] tmpBytes = sb.toString().getBytes("MS950");
                    writeBinFile(tmpBytes, tmpBytes.length);
                    sb = new StringBuffer();
                    countInEachBuffer = 0;
                }
            }
            closeCursor();
            
            // write the rest of bytes on the file 
            if (countInEachBuffer > 0) {
                showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
                byte[] tmpBytes = sb.toString().getBytes("MS950");
                writeBinFile(tmpBytes, tmpBytes.length);
            }

            if (rowCount == 0) {
                showLogMessage("I", "", "無資料可寫入.DAT檔");
            } else {
                showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
            }

        } finally {
            closeBinaryOutput();
        }

        return rowCount;
    }

    /**
     * generate a file
     * 
     * @param fileFolder 檔案的資料夾路徑
     * @param fileName .dat檔的檔名
     * @return the number of rows written. If the returned value is -1, it means the path or the
     *         file does not exist.
     * @throws Exception
     */
    private int generateRmFile(String fileFolder, String fileName)
            throws Exception {

        String datFilePath = Paths.get(fileFolder, fileName).toString();
        boolean isOpen = openBinaryOutput(datFilePath);
        if (isOpen == false) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
            return -1;
        }

        int rowCount = 0;
        int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a
                                   // specified value
        try {
            StringBuffer sb = new StringBuffer();
            showLogMessage("I", "", "開始產生資料檔......");

            // 處理信用卡及VD卡資料
            showLogMessage("I", "", "開始處理信用卡及VD卡資料檔......");
            selectCreditCardAndVdCardData(null, null);
            while (fetchTable()) {
                String rowData = getRowOfDAT();
                String[] rowDataArray= rowData.split("\006");
                rowData = String.join("!", ArrayUtils.subarray(rowDataArray, 0, 51) ) + "\n";
                sb.append(rowData);
                rowCount++;
                countInEachBuffer++;
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                    showLogMessage("I", "",
                            String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
                    byte[] tmpBytes = sb.toString().getBytes("MS950");
                    writeBinFile(tmpBytes, tmpBytes.length);
                    sb = new StringBuffer();
                    countInEachBuffer = 0;
                }
            }
            closeCursor();
            
            // write the rest of bytes on the file 
            if (countInEachBuffer > 0) {
                showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
                byte[] tmpBytes = sb.toString().getBytes("MS950");
                writeBinFile(tmpBytes, tmpBytes.length);
            }

            if (rowCount == 0) {
                showLogMessage("I", "", "無資料可寫入.DAT檔");
            } else {
                showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
            }

        } finally {
            closeBinaryOutput();
        }

        return rowCount;
    }

    /**
     * generate a file
     * 
     * @param fileFolder 檔案的資料夾路徑
     * @param fileName .dat檔的檔名
     * @return the number of rows written. If the returned value is -1, it means the path or the
     *         file does not exist.
     * @throws Exception
     */
    private int generateRmFile2(String fileFolder, String fileName)
            throws Exception {

        String datFilePath = Paths.get(fileFolder, fileName).toString();
        boolean isOpen = openBinaryOutput(datFilePath);
        if (isOpen == false) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
            return -1;
        }

        int rowCount = 0;
        int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a
                                   // specified value
        try {
            StringBuffer sb = new StringBuffer();
            showLogMessage("I", "", "開始產生資料檔......");

            // 處理信用卡及VD卡資料
            showLogMessage("I", "", "開始處理信用卡資料檔......");
            selectCreditCardAndVdCardData(" current_code=0 and IS_CREDIT_CARD=1 ", "REG_BANK_NO,STAFF_FLAG,MAJOR_ID_NO");
            while (fetchTable()) {
                String rowData = getRowOfDAT();
                String[] rowDataArray= rowData.split("\006");
                rowData = String.join("", ArrayUtils.subarray(rowDataArray, 0, 51) ) + "\n";
                sb.append(rowData);
                rowCount++;
                countInEachBuffer++;
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                    showLogMessage("I", "",
                            String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
                    byte[] tmpBytes = sb.toString().getBytes("MS950");
                    writeBinFile(tmpBytes, tmpBytes.length);
                    sb = new StringBuffer();
                    countInEachBuffer = 0;
                }
            }
            closeCursor();
            
            // write the rest of bytes on the file 
            if (countInEachBuffer > 0) {
                showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
                byte[] tmpBytes = sb.toString().getBytes("MS950");
                writeBinFile(tmpBytes, tmpBytes.length);
            }

            if (rowCount == 0) {
                showLogMessage("I", "", "無資料可寫入.DAT檔");
            } else {
                showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
            }

        } finally {
            closeBinaryOutput();
        }

        return rowCount;
    }
    
    /**
     * 合併三個產出當method，generateDatFile + generateRmFile + generateRmFile2
     * 
     * @param crmFileFolder
     * @param crmDatFileName
     * @param isLastDay
     * @param rmFolder
     * @param rmfile1
     * @param rmfile2
     * @return
     * @throws Exception
     */
    private int generateFile(String crmFileFolder, String crmDatFileName, boolean isLastDay,
            String rmFolder, String rmfile1, String rmfile2) throws Exception {
    	selectColLiacNegoFlag();
    	selectTscCard();
    	selectTscVdCard();
    	selectActDebt();
    	selectActCurrHst();
    	selectVdConsumeAmt();
    	selectCcaCardBalanceCal();
    	selectCcaAcctBalanceCal();
    	
        String datFilePath = Paths.get(crmFileFolder, crmDatFileName).toString();
        String rmFilePath1 = Paths.get(rmFolder, rmfile1).toString();
        String rmFilePath2 = Paths.get(rmFolder, rmfile2).toString();
        int crmfIdx = openBinaryOutput2(datFilePath);
        int rmf1Idx = openBinaryOutput2(rmFilePath1);
        int rmf2Idx = openBinaryOutput2(rmFilePath2);

        int rowCount = 0;
        int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a
                                   // specified value
        try {
            StringBuffer sb = new StringBuffer();
            StringBuffer rmsb1 = new StringBuffer();
            StringBuffer rmsb2 = new StringBuffer();
            showLogMessage("I", "", "開始產生.DAT檔......");

            // 處理信用卡及VD卡資料
            showLogMessage("I", "", "開始處理信用卡及VD卡資料檔......");
            selectCreditCardAndVdCardData(null, null);
            while (fetchTable()) {
                String rowOfDAT = getRowOfDAT();
                sb.append(rowOfDAT);
                
                if (isLastDay) {
                    String[] rowDataArray= rowOfDAT.split("\006");
                    String rowData = String.join("!", ArrayUtils.subarray(rowDataArray, 0, 51) ) + "\n";
                    rmsb1.append(rowData);
                    rowData = String.join("", ArrayUtils.subarray(rowDataArray, 0, 51) ) + "\n";
                    rmsb2.append(rowData);
                }
                
                rowCount++;
                countInEachBuffer++;
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                    showLogMessage("I", "",
                            String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
                    byte[] tmpBytes = sb.toString().getBytes("MS950");
                    writeBinFile2(crmfIdx, tmpBytes, tmpBytes.length);
                    
                    if (isLastDay) {
                        tmpBytes = rmsb1.toString().getBytes("MS950");
                        writeBinFile2(rmf1Idx, tmpBytes, tmpBytes.length);
                        tmpBytes = rmsb2.toString().getBytes("MS950");
                        writeBinFile2(rmf2Idx, tmpBytes, tmpBytes.length);                        
                    }
                    
                    sb = new StringBuffer();
                    rmsb1 = new StringBuffer();
                    rmsb2 = new StringBuffer();
                    countInEachBuffer = 0;
                }
            }
            closeCursor();
            
            // write the rest of bytes on the file 
            if (countInEachBuffer > 0) {
                showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
                byte[] tmpBytes = sb.toString().getBytes("MS950");
                writeBinFile(tmpBytes, tmpBytes.length);
                
                if (isLastDay) {
                    tmpBytes = rmsb1.toString().getBytes("MS950");
                    writeBinFile2(rmf1Idx, tmpBytes, tmpBytes.length);
                    tmpBytes = rmsb2.toString().getBytes("MS950");
                    writeBinFile2(rmf2Idx, tmpBytes, tmpBytes.length);
                }
            }

            if (rowCount == 0) {
                showLogMessage("I", "", "無資料可寫入.DAT檔");
            } else {
                showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
            }

        } finally {
            closeBinaryOutput2(crmfIdx);
            if (isLastDay) {
              closeBinaryOutput2(rmf1Idx);
              closeBinaryOutput2(rmf2Idx);
            }
        }

        return rowCount;
    }

    /**
     * 產生檔案
     * 
     * @return String
     * @throws Exception
     */
    private String getRowOfDAT() throws Exception {
        boolean isCreditCard = getValueInt("IS_CREDIT_CARD") == 1;
        String cardNo = getValue("CARD_NO");
        String acnoPSeqno = getValue("TMP_ACNO_P_SEQNO");
        String pSeqno = getValue("P_SEQNO");
        String idPSeqno = "";
        String idNo = getValue("ID_NO");
        String majorIdNo = getValue("MAJOR_ID_NO");
        String colLiacNegoFlag = "";
        if (isCreditCard) {
            idPSeqno = getValue("ID_P_SEQNO");
            
    		setValue("liacnego.ID_P_SEQNO",idPSeqno);
            getLoadData("liacnego.ID_P_SEQNO");
            colLiacNegoFlag = getValue("liacnego.LIAC_STATUS");
        }
        
        String electronicCode = getValue("ELECTRONIC_CODE");        
        String autoloadFlag = "";
        String lockFlag = "";
        String returnFlag = "";
        
        if(electronicCode.equals("01")) {
            if (isCreditCard) {
        		setValue("tsccard.CARD_NO",cardNo);
                getLoadData("tsccard.CARD_NO");
                autoloadFlag = getValue("tsccard.AUTOLOAD_FLAG");
                lockFlag = getValue("tsccard.LOCK_FLAG");
                returnFlag = getValue("tsccard.RETURN_FLAG");
            }
            else {
        		setValue("tscvdcard.VD_CARD_NO",cardNo);
                getLoadData("tscvdcard.VD_CARD_NO");
                autoloadFlag = getValue("tscvdcard.AUTOLOAD_FLAG");
                lockFlag = getValue("tscvdcard.LOCK_FLAG");
                returnFlag = getValue("tscvdcard.RETURN_FLAG");
            }
        }        
        
        String groupCode = getValue("GROUP_CODE");
        String newEndDate = getValue("NEW_END_DATE");
        String chiName = getValue("CHI_NAME");
        String address1 = getValue("BILL_SENDING_ADDR1") + getValue("BILL_SENDING_ADDR2")
                + getValue("BILL_SENDING_ADDR3") + getValue("BILL_SENDING_ADDR4")
                + getValue("BILL_SENDING_ADDR5");
        String address2 = "";
        byte[] addressBytes = address1.getBytes("MS950");
        if (addressBytes.length > 50) {
            address1 = new String(ArrayUtils.subarray(addressBytes, 0, 50), "MS950");
            address2 = new String(ArrayUtils.subarray(addressBytes, 50, addressBytes.length), "MS950");
        }
        String birthday = commDate.toTwDate(getValue("BIRTHDAY"));
        if(birthday.length() == 6) {
        	birthday = 0 + birthday;
        }
        String homeTel = getValue("HOME_AREA_CODE1") + '-' + getValue("HOME_TEL_NO1") + '-'
                + getValue("HOME_TEL_EXT1");
        String officeTel = getValue("OFFICE_AREA_CODE1") + '-' + getValue("OFFICE_TEL_NO1") + '-'
                + getValue("OFFICE_TEL_EXT1");
        String regBankNo = getValue("REG_BANK_NO");
        String ctrlCode = getCtrlCode();
        String currentCode = getValue("CURRENT_CODE");
        String oppostReason = getValue("OPPOST_REASON");
        String tmpCurrCode = getValue("TMP_CURR_CODE");

        StringBuffer sb = new StringBuffer();
        sb.append(commCrd.fixLeft(cardNo, 16)); // 卡號
        sb.append(commCrd.fixLeft("\006", 1));
        String cardType = "";
        double availableBalance = 0.0;
        if (groupCode.length() > 0) {
            if (groupCode.length() > 3) {
                cardType = groupCode.substring(1, 4);
            } else {
                cardType = groupCode.substring(1);
            }
        }
        sb.append(commCrd.fixLeft(cardType, 3)); // 卡別
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(idNo, 11)); // 持卡人的身份字號
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(newEndDate.substring(4,6)+newEndDate.substring(2,4), 4)); // 卡片到期月年
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 2)); // TCBRP18-POSTING-FLAG
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(chiName, 30)); // 持卡人中文戶名
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(address1, 50)); // 住址-1 (帳單寄送地)
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(address2, 50)); // 住址-2
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixRight(birthday, 7)); // 持卡人出生年月日
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(homeTel, 24)); // 住家電話號碼
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(officeTel, 24)); // 公司電話號碼
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(regBankNo, 4)); // 發卡單位
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(ctrlCode, 1)); // 控管碼
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(majorIdNo, 11)); // 正卡持卡人身份證字號
        sb.append(commCrd.fixLeft("\006", 1));
        if (isCreditCard) {
            sb.append(commCrd.fixLeft(getValue("AUTOPAY_ACCT_NO"), 16)); // 自動扣繳帳號
        } else {
            sb.append(commCrd.fixLeft("", 16)); // 自動扣繳帳號
        }
        sb.append(commCrd.fixLeft("\006", 1));
        double loopCreditAmt = 0;
        double consumeAmt = 0;
        if (isCreditCard) {
    		setValue("debt1.P_SEQNO",pSeqno);
    		setValue("debt1.CURR_CODE",tmpCurrCode);
            getLoadData("debt1.P_SEQNO,debt1.CURR_CODE");
            loopCreditAmt = getValueDouble("debt1.SUM_END_BAL");
            
    		setValue("currhst1.P_SEQNO",pSeqno);
    		setValue("currhst1.CURR_CODE",tmpCurrCode);
            getLoadData("currhst1.P_SEQNO,currhst1.CURR_CODE");
            consumeAmt = getValueDouble("currhst1.STMT_NEW_AMT");                	
        } else {
    		setValue("dbajrnl1.P_SEQNO",pSeqno);
            getLoadData("dbajrnl1.P_SEQNO");
            consumeAmt = getValueDouble("dbajrnl1.VD_CONSUME_AMT"); 
        }
        sb.append(commCrd.fixLeft(String.format("%010.2f", loopCreditAmt), 10)); // 循環信用金額
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("0", 2)); // 逾期繳款次數
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(String.format("%014.2f", consumeAmt), 14)); // 本月消費金額
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("ORI_ISSUE_DATE"), 8)); // 開戶日
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(String.format("%09d", "Y".equals(getValue("TMP_SON_CARD_FLAG")) ? getValueInt("TMP_INDIV_CRD_LMT") : getValueInt("TMP_LINE_OF_CREDIT_AMT")), 9)); // 信用額度(卡片)
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("0".equals(currentCode) ? "1" : "0", 1)); // 是否為正常卡(1=正常流通卡)
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(StringUtils.isNotEmpty(getValue("LAST_CONSUME_DATE")) ? "1" : "0", 1)); // 是否有消費(1=有消費)
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("0", 10)); // 逾期繳款金額
        sb.append(commCrd.fixLeft("\006", 1));
        String bloclCodeDate = "";
        if ("a".equals(ctrlCodeType)) {
            bloclCodeDate = getValue("OPPOST_DATE");
        }
        if ("b".equals(ctrlCodeType)) {
            bloclCodeDate = getValue("BLOCK_DATE");
            if (StringUtils.isNotEmpty(getValue("SPEC_STATUS"))) {
                bloclCodeDate = getValue("SPEC_DATE");
            }
        }
        sb.append(commCrd.fixLeft(bloclCodeDate, 8)); // BLOCL-CODE最近日期 
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("BILL_SENDING_ZIP"), 6)); // 郵遞區號(帳單寄送地)
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("Y".equals(getValue("STAFF_FLAG")) ? "1" : "0", 1)); // 員工註記(1=員工)
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 2)); // 認同卡編號
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 5)); // FOR COMBO新增的備註
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("E_MAIL_ADDR"), 30)); // E_MAIL ADDRESS
        sb.append(commCrd.fixLeft("\006", 1));
        
        String tmpAutopayType = "00";
        if (isCreditCard && getValue("AUTOPAY_INDICATOR").length() > 0) {
        	if(getValue("AUTOPAY_INDICATOR").equals("2")) {
        		tmpAutopayType = "10"; //最低
        	}
        	else {
        		tmpAutopayType = "20"; //全額
        	}
        } 
        sb.append(commCrd.fixLeft(tmpAutopayType, 2)); // 自動扣繳類別
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("CELLAR_PHONE"), 15)); // 行動電話
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("MARRIAGE"), 1)); // 婚姻狀況
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("0", 2)); // 卡片序號
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("BUSINESS_CODE"), 4)); // 行業別
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("SEX"), 1)); // 性別
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(String.format("%09d", getValueInt("TMP_LINE_OF_CREDIT_AMT")), 9)); // 客戶信用額度
        sb.append(commCrd.fixLeft("\006", 1));
        String promoterId = getValue("CLERK_ID");
        if (StringUtils.isEmpty(promoterId)) {
            promoterId = getValue("PROMOTE_EMP_NO");
        }
        if (StringUtils.isEmpty(promoterId)) {
            promoterId = getValue("MEMBER_ID");
        }
        if (StringUtils.isEmpty(promoterId)) {
            promoterId = getValue("INTRODUCE_ID");
        }
        if (StringUtils.isEmpty(promoterId)) {
            promoterId = "";
        }
        sb.append(commCrd.fixLeft(promoterId, 10)); // 推廣人員ID
        sb.append(commCrd.fixLeft("\006", 1));
        String collectionFlag = "";
        if ("3".equals(getValue("TMP_ACCT_STATUS"))) {
            collectionFlag = "3";
        }
        if ("4".equals(getValue("TMP_ACCT_STATUS"))) {
            collectionFlag = "4";
        }
        sb.append(commCrd.fixLeft(collectionFlag, 1)); // 轉催收註記
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("TMP_ACCT_NO"), 13)); // COMBO金融卡帳號
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 1)); // 委扣帳號不一致註記
        sb.append(commCrd.fixLeft("\006", 1));
        // 若(1)讀取到的CURRENT_CODE=0且LAST_CONSUME_DATE >= 系統日減6個月  放Y 否則 放N
        String cardActiveFlag = "N";
        if ("0".equals(currentCode) && getValue("LAST_CONSUME_DATE").compareTo(commDate.monthAdd(commDate.sysDate(), -6)) >= 0) {
            cardActiveFlag = "Y";
        }
        sb.append(commCrd.fixLeft(cardActiveFlag, 1)); // 活卡註記(Y=有效卡)
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 8)); // PP CARD末八碼
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("ENG_NAME"), 26)); // 持卡人英文姓名
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 1)); // PP CARD次月到期註記
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 4)); // PP CARD到期年月
        sb.append(commCrd.fixLeft("\006", 1));
        
		//可用餘額
    	if(isCreditCard){
    		if("Y".equals(getValue("TMP_SON_CARD_FLAG"))) {
        		setValue("cardbalance.CARD_NO",cardNo);
                getLoadData("cardbalance.CARD_NO");
                availableBalance = getValueDouble("cardbalance.CARD_AMT_BALANCE");     			
    		}
    		else {
        		setValue("acctbalance.ACNO_P_SEQNO",acnoPSeqno);
                getLoadData("acctbalance.ACNO_P_SEQNO");
                availableBalance = getValueDouble("acctbalance.ACCT_AMT_BALANCE");     	    			
    		}
    	}
    		
        String cardBalance = String.format("%014.2f", availableBalance) ;
        sb.append(commCrd.fixLeft(cardBalance, 14)); // 可用餘額
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(commDate.toTwDate(getValue("LAST_CONSUME_DATE")), 7)); // 最後交易日
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 1)); // 轉換CHIP註記
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 1)); // 免費保險註記
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("TMP_APPLY_ATM_FLAG"), 1)); // 預借現金註記
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("TMP_ACTIVATE_FLAG"), 1)); // 開卡狀態
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 2)); // 卡人使用之促銷辦法
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 2)); // 卡片使用之促銷辦法
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 4)); // 卡片使用之促銷辦法截止期日
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 2)); // 卡片預留之促銷辦法
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 4)); // 卡片預留之促銷辦法截止期日
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixRight(commCrd.getSubString(birthday,0,birthday.length() - 4), 3)); // 持卡人出生民國年 YYY
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(String.format("%05.2f", getValueDouble("RCRATE_YEAR")), 5)); // 卡片適用利率
        sb.append(commCrd.fixLeft("\006", 1));
        String acctType = getValue("ACCT_TYPE");
        String corpNo = "";
        if ("03".equals(acctType) || "06".equals(acctType)) {
            corpNo = getValue("TMP_CORP_NO");
        }
        sb.append(commCrd.fixLeft(corpNo, 11)); // 公司戶統編
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(regBankNo, 4)); // 原申辦分行
        sb.append(commCrd.fixLeft("\006", 1));
        
        String issueDate = getValue("ISSUE_DATE");
        String reissueDate = getValue("REISSUE_DATE");
        String changeDate = getValue("CHANGE_DATE");
        issueDate = reissueDate.compareTo(issueDate) > 0 ? reissueDate : issueDate;
        issueDate = changeDate.compareTo(issueDate) > 0 ? changeDate : issueDate;
         sb.append(commCrd.fixLeft(issueDate, 8)); // 製卡日期 (YYYYMMDD 西元年 )
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(oppostReason, 2)); // 註銷原因停卡原因
        sb.append(commCrd.fixLeft("\006", 1));
        
        String orgAccountType = "";
        if ("6673".equals(groupCode) || "6674".equals(groupCode)) {
        	if(tmpCurrCode.equals("840")) {
        		orgAccountType = "606";
        	}
        	else if(tmpCurrCode.equals("392")) {
        		orgAccountType = "607";
        	}
        	else {
        		orgAccountType = "106";
        	}           
        } else if ("01".equals(acctType)) {
            orgAccountType = "106";
        } else if ("90".equals(acctType)) {
            orgAccountType = "206";
        } else if ("03".equals(acctType) || "06".equals(acctType)) {
            orgAccountType = "306";
        }
        sb.append(commCrd.fixLeft(orgAccountType, 3)); // ORG帳戶類別
        sb.append(commCrd.fixLeft("\006", 1));
        String jobPosition = getValue("JOB_POSITION");
        if (jobPosition.length() > 7) {
            jobPosition = jobPosition.substring(0, 7);
        }
        sb.append(commCrd.fixLeft(jobPosition, 15)); // 客戶職稱
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("MARKET_AGREE_BASE"), 1)); // 拒絕行銷註記
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(colLiacNegoFlag, 1)); // 前置協商註記
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 2)); // 子女數 FOR CRM USR
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 1)); // 自有資產
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 1)); // PRE ACTION FLAG
        sb.append(commCrd.fixLeft("\006", 1));
        String preCashPer = "";
        if (isCreditCard) {
            preCashPer = String.format("%02d", (int)(100 * getValueDouble("TMP_LINE_OF_CREDIT_AMT_CASH") / getValueDouble("TMP_LINE_OF_CREDIT_AMT")));
        }
        sb.append(commCrd.fixLeft(preCashPer, 2)); // 預借現金成數
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("OTHER_CNTRY_CODE"), 2)); // 國籍別
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 8)); // 個人信用卡之公司統編
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(getValue("TMP_E_NEWS"), 1)); // EDM寄送
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 2)); // 金控資料註記 [1：要提供, 0：未通知 ]
        sb.append(commCrd.fixLeft("\006", 1));
        // 若(4)讀取到的AUTOLOAD_FLAG= Y  放11 否則  放00
        sb.append(commCrd.fixLeft("Y".equals(autoloadFlag) ? "11" : "00", 2)); // 悠遊卡自動加值開啟及啟用註記
        sb.append(commCrd.fixLeft("\006", 1));
        String lockAndReturnFlag = "";
        if ("Y".equals(lockFlag)) {
            lockAndReturnFlag = "Y".equals(returnFlag) ? "11" : "10";
        } else {
            lockAndReturnFlag = "Y".equals(returnFlag) ? "01" : "00";
        }
        sb.append(commCrd.fixLeft(lockAndReturnFlag, 2)); // 悠遊卡鎖卡及退卡註記
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(groupCode, 4)); // 團體代碼
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(currentCode, 1)); // 卡片狀況
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft(oppostReason, 2)); // 停用原因碼
        sb.append(commCrd.fixLeft("\006", 1));
        sb.append(commCrd.fixLeft("", 51)); // FILLER
        sb.append(lineSeparator);

        return sb.toString();
    }

    // 控管碼
    private String getCtrlCode() throws Exception {
        String ctrlCode = "";
        String currentCode = getValue("CURRENT_CODE");
        String oppostReason = getValue("OPPOST_REASON");
        if (StringUtils.isEmpty(currentCode)) {
            ctrlCode = "";
            ctrlCodeType = "c";
            return ctrlCode;
        }
        if (!currentCode.equals("0")) {
            String key = currentCode + "_" + oppostReason;
            ctrlCode = ctrlCodeMap.get(key);
            if (ctrlCode == null) {
                ctrlCode = "Z"; // 不等於上述值  Z
            }
            ctrlCodeType = "a";
            return ctrlCode;
        }
        ctrlCodeType = "b";
        if (StringUtils.isNotEmpty(getValue("SPEC_STATUS"))
                || StringUtils.isNotEmpty(getValue("BLOCK_REASON1"))
                || StringUtils.isNotEmpty(getValue("BLOCK_REASON2"))
                || StringUtils.isNotEmpty(getValue("BLOCK_REASON3"))
                || StringUtils.isNotEmpty(getValue("BLOCK_REASON4"))
                || StringUtils.isNotEmpty(getValue("BLOCK_REASON5"))) {
            ctrlCode = "T";
            return ctrlCode;
        }
        return ctrlCode;
    }
    // 控管碼映射表
    Map<String, String> ctrlCodeMap = new HashMap() {
        {
            put("1_A2", "A");
            put("3_J2", "B");
            put("1_10", "E");
            put("5_M2", "F");
            put("4_S1", "G");
            put("1_AL", "K");
            put("2_C1", "L");
            put("4_MO", "M");
            put("1_AJ", "N");
            put("5_O1", "O");
            put("2_S0", "S");
            put("1_AX", "X");
            // 不等於上述值  Z
        }
    };
    
    // 讀取信用卡及VD卡資料
    private void selectCreditCardAndVdCardData(String whereStr, String orderStr) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT * from (");
        sb.append(" SELECT ");
        sb.append("     1 as IS_CREDIT_CARD, ");
        sb.append("     A.CARD_NO, ");
        sb.append("     A.GROUP_CODE, ");
        sb.append("     B.ID_NO, ");
        sb.append("     B2.ID_NO AS MAJOR_ID_NO, ");
        sb.append("     A.ACNO_P_SEQNO AS TMP_ACNO_P_SEQNO, ");
        sb.append("     A.P_SEQNO, ");
        sb.append("     A.ID_P_SEQNO, ");
        sb.append("     A.MAJOR_ID_P_SEQNO, ");
        sb.append("     A.NEW_END_DATE, ");
        sb.append("     B.CHI_NAME, ");
        sb.append("     C.BILL_SENDING_ZIP, ");
        sb.append("     C.BILL_SENDING_ADDR1, ");
        sb.append("     C.BILL_SENDING_ADDR2, ");
        sb.append("     C.BILL_SENDING_ADDR3, ");
        sb.append("     C.BILL_SENDING_ADDR4, ");
        sb.append("     C.BILL_SENDING_ADDR5, ");
        sb.append("     B.BIRTHDAY, ");
        sb.append("     B.HOME_AREA_CODE1, ");
        sb.append("     B.HOME_TEL_NO1, ");
        sb.append("     B.HOME_TEL_EXT1, ");
        sb.append("     B.OFFICE_AREA_CODE1, ");
        sb.append("     B.OFFICE_TEL_NO1, ");
        sb.append("     B.OFFICE_TEL_EXT1, ");
        sb.append("     A.REG_BANK_NO, ");
        sb.append("     A.CURRENT_CODE, ");
        sb.append("     A.OPPOST_REASON, ");
        sb.append("     A.OPPOST_DATE, ");
        sb.append("     D.SPEC_STATUS, ");
        sb.append("     D.SPEC_DATE, ");
        sb.append("     E.BLOCK_REASON1, ");
        sb.append("     E.BLOCK_REASON2, ");
        sb.append("     E.BLOCK_REASON3, ");
        sb.append("     E.BLOCK_REASON4, ");
        sb.append("     E.BLOCK_REASON5, ");
        sb.append("     E.BLOCK_DATE, ");
        sb.append("     A.ORI_ISSUE_DATE, ");
        sb.append("     A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG, ");
        sb.append("     A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT, ");
        sb.append("     C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ");
        sb.append("     A.LAST_CONSUME_DATE, ");
        sb.append("     B.STAFF_FLAG, ");
        sb.append("     B.E_MAIL_ADDR, ");
        sb.append("     B.CELLAR_PHONE, ");
        sb.append("     B.MARRIAGE, ");
        sb.append("     B.BUSINESS_CODE, ");
        sb.append("     B.SEX, ");
        sb.append("     A.PROMOTE_EMP_NO, ");
        sb.append("     A.MEMBER_ID, ");
        sb.append("     A.CLERK_ID, ");
        sb.append("     A.INTRODUCE_ID, ");
        sb.append("     C.ACCT_STATUS AS TMP_ACCT_STATUS, ");
        sb.append("     A.COMBO_ACCT_NO AS TMP_ACCT_NO, ");
        sb.append("     A.ENG_NAME, ");
        sb.append("     A.APPLY_ATM_FLAG AS TMP_APPLY_ATM_FLAG, ");
        sb.append("     DECODE(A.ACTIVATE_FLAG,'2','Y','N') AS TMP_ACTIVATE_FLAG, ");
        sb.append("     A.ACCT_TYPE, ");
        sb.append("     A.CORP_NO AS TMP_CORP_NO, ");
        sb.append("     A.ISSUE_DATE, ");
        sb.append("     A.REISSUE_DATE, ");
        sb.append("     A.CHANGE_DATE, ");
        sb.append("     B.JOB_POSITION, ");
        sb.append("     B.MARKET_AGREE_BASE, ");
        sb.append("     C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH, ");
        sb.append("     B.OTHER_CNTRY_CODE, ");
        sb.append("     DECODE(B.E_NEWS,'Y','1','0') AS TMP_E_NEWS, ");
        sb.append("     A.ELECTRONIC_CODE, ");
        sb.append("     C.AUTOPAY_ACCT_NO, ");
        sb.append("     C.AUTOPAY_INDICATOR, ");
        sb.append("     C.RCRATE_YEAR, ");
        sb.append("     '901' AS TMP_CURR_CODE ");
        sb.append(" FROM ");
        sb.append("     CRD_CARD A, ");
        sb.append("     CRD_IDNO B, ");
        sb.append("     CRD_IDNO B2, ");
        sb.append("     ACT_ACNO C, ");
        sb.append("     CCA_CARD_BASE D, ");
        sb.append("     CCA_CARD_ACCT E ");
        sb.append(" WHERE ");
        sb.append("     A.ID_P_SEQNO = B.ID_P_SEQNO ");
        sb.append("     AND A.MAJOR_ID_P_SEQNO = B2.ID_P_SEQNO ");
        sb.append("     AND A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ");
        sb.append("     AND A.CARD_NO = D.CARD_NO ");
        sb.append("     AND A.ACNO_P_SEQNO = E.ACNO_P_SEQNO ");
        sb.append("     AND A.CURR_CODE = '901' ");
        sb.append(" UNION ");
        sb.append(" SELECT ");
        sb.append("     1 as IS_CREDIT_CARD, ");
        sb.append("     A.CARD_NO, ");
        sb.append("     A.GROUP_CODE, ");
        sb.append("     B.ID_NO, ");
        sb.append("     B2.ID_NO AS MAJOR_ID_NO, ");
        sb.append("     A.ACNO_P_SEQNO AS TMP_ACNO_P_SEQNO, ");
        sb.append("     A.P_SEQNO, ");
        sb.append("     A.ID_P_SEQNO, ");
        sb.append("     A.MAJOR_ID_P_SEQNO, ");
        sb.append("     A.NEW_END_DATE, ");
        sb.append("     B.CHI_NAME, ");
        sb.append("     C.BILL_SENDING_ZIP, ");
        sb.append("     C.BILL_SENDING_ADDR1, ");
        sb.append("     C.BILL_SENDING_ADDR2, ");
        sb.append("     C.BILL_SENDING_ADDR3, ");
        sb.append("     C.BILL_SENDING_ADDR4, ");
        sb.append("     C.BILL_SENDING_ADDR5, ");
        sb.append("     B.BIRTHDAY, ");
        sb.append("     B.HOME_AREA_CODE1, ");
        sb.append("     B.HOME_TEL_NO1, ");
        sb.append("     B.HOME_TEL_EXT1, ");
        sb.append("     B.OFFICE_AREA_CODE1, ");
        sb.append("     B.OFFICE_TEL_NO1, ");
        sb.append("     B.OFFICE_TEL_EXT1, ");
        sb.append("     A.REG_BANK_NO, ");
        sb.append("     A.CURRENT_CODE, ");
        sb.append("     A.OPPOST_REASON, ");
        sb.append("     A.OPPOST_DATE, ");
        sb.append("     D.SPEC_STATUS, ");
        sb.append("     D.SPEC_DATE, ");
        sb.append("     E.BLOCK_REASON1, ");
        sb.append("     E.BLOCK_REASON2, ");
        sb.append("     E.BLOCK_REASON3, ");
        sb.append("     E.BLOCK_REASON4, ");
        sb.append("     E.BLOCK_REASON5, ");
        sb.append("     E.BLOCK_DATE, ");
        sb.append("     A.ORI_ISSUE_DATE, ");
        sb.append("     A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG, ");
        sb.append("     A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT, ");
        sb.append("     C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ");
        sb.append("     A.LAST_CONSUME_DATE, ");
        sb.append("     B.STAFF_FLAG, ");
        sb.append("     B.E_MAIL_ADDR, ");
        sb.append("     B.CELLAR_PHONE, ");
        sb.append("     B.MARRIAGE, ");
        sb.append("     B.BUSINESS_CODE, ");
        sb.append("     B.SEX, ");
        sb.append("     A.PROMOTE_EMP_NO, ");
        sb.append("     A.MEMBER_ID, ");
        sb.append("     A.CLERK_ID, ");
        sb.append("     A.INTRODUCE_ID, ");
        sb.append("     C.ACCT_STATUS AS TMP_ACCT_STATUS, ");
        sb.append("     A.COMBO_ACCT_NO AS TMP_ACCT_NO, ");
        sb.append("     A.ENG_NAME, ");
        sb.append("     A.APPLY_ATM_FLAG AS TMP_APPLY_ATM_FLAG, ");
        sb.append("     DECODE(A.ACTIVATE_FLAG,'2','Y','N') AS TMP_ACTIVATE_FLAG, ");
        sb.append("     A.ACCT_TYPE, ");
        sb.append("     A.CORP_NO AS TMP_CORP_NO, ");
        sb.append("     A.ISSUE_DATE, ");
        sb.append("     A.REISSUE_DATE, ");
        sb.append("     A.CHANGE_DATE, ");
        sb.append("     B.JOB_POSITION, ");
        sb.append("     B.MARKET_AGREE_BASE, ");
        sb.append("     C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH, ");
        sb.append("     B.OTHER_CNTRY_CODE, ");
        sb.append("     DECODE(B.E_NEWS,'Y','1','0') AS TMP_E_NEWS, ");
        sb.append("     A.ELECTRONIC_CODE, ");
        sb.append("     C.AUTOPAY_ACCT_NO, ");
        sb.append("     C.AUTOPAY_INDICATOR, ");
        sb.append("     C.RCRATE_YEAR, ");
        sb.append("     F.CURR_CODE AS TMP_CURR_CODE ");
        sb.append(" FROM ");
        sb.append("     CRD_CARD A, ");
        sb.append("     CRD_IDNO B, ");
        sb.append("     CRD_IDNO B2, ");
        sb.append("     ACT_ACNO C, ");
        sb.append("     CCA_CARD_BASE D, ");
        sb.append("     CCA_CARD_ACCT E, ");
        sb.append("     ACT_ACCT_CURR F ");
        sb.append(" WHERE ");
        sb.append("     A.ID_P_SEQNO = B.ID_P_SEQNO ");
        sb.append("     AND A.MAJOR_ID_P_SEQNO = B2.ID_P_SEQNO ");
        sb.append("     AND A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ");
        sb.append("     AND A.CARD_NO = D.CARD_NO ");
        sb.append("     AND A.ACNO_P_SEQNO = E.ACNO_P_SEQNO ");
        sb.append("     AND A.P_SEQNO = F.P_SEQNO ");                              
        sb.append("     AND A.CURR_CODE = '392' ");
        sb.append("     AND F.CURR_CODE IN ('901','392') ");
        sb.append(" UNION ");
        sb.append(" SELECT ");
        sb.append("     1 as IS_CREDIT_CARD, ");
        sb.append("     A.CARD_NO, ");
        sb.append("     A.GROUP_CODE, ");
        sb.append("     B.ID_NO, ");
        sb.append("     B2.ID_NO AS MAJOR_ID_NO, ");
        sb.append("     A.ACNO_P_SEQNO AS TMP_ACNO_P_SEQNO, ");
        sb.append("     A.P_SEQNO, ");
        sb.append("     A.ID_P_SEQNO, ");
        sb.append("     A.MAJOR_ID_P_SEQNO, ");
        sb.append("     A.NEW_END_DATE, ");
        sb.append("     B.CHI_NAME, ");
        sb.append("     C.BILL_SENDING_ZIP, ");
        sb.append("     C.BILL_SENDING_ADDR1, ");
        sb.append("     C.BILL_SENDING_ADDR2, ");
        sb.append("     C.BILL_SENDING_ADDR3, ");
        sb.append("     C.BILL_SENDING_ADDR4, ");
        sb.append("     C.BILL_SENDING_ADDR5, ");
        sb.append("     B.BIRTHDAY, ");
        sb.append("     B.HOME_AREA_CODE1, ");
        sb.append("     B.HOME_TEL_NO1, ");
        sb.append("     B.HOME_TEL_EXT1, ");
        sb.append("     B.OFFICE_AREA_CODE1, ");
        sb.append("     B.OFFICE_TEL_NO1, ");
        sb.append("     B.OFFICE_TEL_EXT1, ");
        sb.append("     A.REG_BANK_NO, ");
        sb.append("     A.CURRENT_CODE, ");
        sb.append("     A.OPPOST_REASON, ");
        sb.append("     A.OPPOST_DATE, ");
        sb.append("     D.SPEC_STATUS, ");
        sb.append("     D.SPEC_DATE, ");
        sb.append("     E.BLOCK_REASON1, ");
        sb.append("     E.BLOCK_REASON2, ");
        sb.append("     E.BLOCK_REASON3, ");
        sb.append("     E.BLOCK_REASON4, ");
        sb.append("     E.BLOCK_REASON5, ");
        sb.append("     E.BLOCK_DATE, ");
        sb.append("     A.ORI_ISSUE_DATE, ");
        sb.append("     A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG, ");
        sb.append("     A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT, ");
        sb.append("     C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ");
        sb.append("     A.LAST_CONSUME_DATE, ");
        sb.append("     B.STAFF_FLAG, ");
        sb.append("     B.E_MAIL_ADDR, ");
        sb.append("     B.CELLAR_PHONE, ");
        sb.append("     B.MARRIAGE, ");
        sb.append("     B.BUSINESS_CODE, ");
        sb.append("     B.SEX, ");
        sb.append("     A.PROMOTE_EMP_NO, ");
        sb.append("     A.MEMBER_ID, ");
        sb.append("     A.CLERK_ID, ");
        sb.append("     A.INTRODUCE_ID, ");
        sb.append("     C.ACCT_STATUS AS TMP_ACCT_STATUS, ");
        sb.append("     A.COMBO_ACCT_NO AS TMP_ACCT_NO, ");
        sb.append("     A.ENG_NAME, ");
        sb.append("     A.APPLY_ATM_FLAG AS TMP_APPLY_ATM_FLAG, ");
        sb.append("     DECODE(A.ACTIVATE_FLAG,'2','Y','N') AS TMP_ACTIVATE_FLAG, ");
        sb.append("     A.ACCT_TYPE, ");
        sb.append("     A.CORP_NO AS TMP_CORP_NO, ");
        sb.append("     A.ISSUE_DATE, ");
        sb.append("     A.REISSUE_DATE, ");
        sb.append("     A.CHANGE_DATE, ");
        sb.append("     B.JOB_POSITION, ");
        sb.append("     B.MARKET_AGREE_BASE, ");
        sb.append("     C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH, ");
        sb.append("     B.OTHER_CNTRY_CODE, ");
        sb.append("     DECODE(B.E_NEWS,'Y','1','0') AS TMP_E_NEWS, ");
        sb.append("     A.ELECTRONIC_CODE, ");
        sb.append("     C.AUTOPAY_ACCT_NO, ");
        sb.append("     C.AUTOPAY_INDICATOR, ");
        sb.append("     C.RCRATE_YEAR, ");
        sb.append("     F.CURR_CODE AS TMP_CURR_CODE ");
        sb.append(" FROM ");
        sb.append("     CRD_CARD A, ");
        sb.append("     CRD_IDNO B, ");
        sb.append("     CRD_IDNO B2, ");
        sb.append("     ACT_ACNO C, ");
        sb.append("     CCA_CARD_BASE D, ");
        sb.append("     CCA_CARD_ACCT E, ");
        sb.append("     ACT_ACCT_CURR F ");
        sb.append(" WHERE ");
        sb.append("     A.ID_P_SEQNO = B.ID_P_SEQNO ");
        sb.append("     AND A.MAJOR_ID_P_SEQNO = B2.ID_P_SEQNO ");
        sb.append("     AND A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ");
        sb.append("     AND A.CARD_NO = D.CARD_NO ");
        sb.append("     AND A.ACNO_P_SEQNO = E.ACNO_P_SEQNO ");
        sb.append("     AND A.P_SEQNO = F.P_SEQNO ");                              
        sb.append("     AND A.CURR_CODE = '840' ");
        sb.append("     AND F.CURR_CODE IN ('901','840') ");
        sb.append(" UNION ");
        sb.append(" SELECT ");
        sb.append("     0 as IS_CREDIT_CARD, ");
        sb.append("     A.CARD_NO, ");
        sb.append("     A.GROUP_CODE, ");
        sb.append("     B.ID_NO, ");
        sb.append("     B.ID_NO AS MAJOR_ID_NO, ");
        sb.append("     '' AS TMP_ACNO_P_SEQNO, ");
        sb.append("     A.P_SEQNO, ");
        sb.append("     A.ID_P_SEQNO, ");
        sb.append("     A.MAJOR_ID_P_SEQNO, ");
        sb.append("     A.NEW_END_DATE, ");
        sb.append("     B.CHI_NAME, ");
        sb.append("     C.BILL_SENDING_ZIP, ");
        sb.append("     C.BILL_SENDING_ADDR1, ");
        sb.append("     C.BILL_SENDING_ADDR2, ");
        sb.append("     C.BILL_SENDING_ADDR3, ");
        sb.append("     C.BILL_SENDING_ADDR4, ");
        sb.append("     C.BILL_SENDING_ADDR5, ");
        sb.append("     B.BIRTHDAY, ");
        sb.append("     B.HOME_AREA_CODE1, ");
        sb.append("     B.HOME_TEL_NO1, ");
        sb.append("     B.HOME_TEL_EXT1, ");
        sb.append("     B.OFFICE_AREA_CODE1, ");
        sb.append("     B.OFFICE_TEL_NO1, ");
        sb.append("     B.OFFICE_TEL_EXT1, ");
        sb.append("     A.REG_BANK_NO, ");
        sb.append("     A.CURRENT_CODE, ");
        sb.append("     A.OPPOST_REASON, ");
        sb.append("     A.OPPOST_DATE, ");
        sb.append("     D.SPEC_STATUS, ");
        sb.append("     D.SPEC_DATE, ");
        sb.append("     E.BLOCK_REASON1, ");
        sb.append("     E.BLOCK_REASON2, ");
        sb.append("     E.BLOCK_REASON3, ");
        sb.append("     E.BLOCK_REASON4, ");
        sb.append("     E.BLOCK_REASON5, ");
        sb.append("     E.BLOCK_DATE, ");
        sb.append("     A.ORI_ISSUE_DATE, ");
        sb.append("     '' AS TMP_SON_CARD_FLAG, ");
        sb.append("     A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT, ");
        sb.append("     NULL AS TMP_LINE_OF_CREDIT_AMT, ");
        sb.append("     A.LAST_CONSUME_DATE, ");
        sb.append("     B.STAFF_FLAG, ");
        sb.append("     B.E_MAIL_ADDR, ");
        sb.append("     B.CELLAR_PHONE, ");
        sb.append("     B.MARRIAGE, ");
        sb.append("     B.BUSINESS_CODE, ");
        sb.append("     B.SEX, ");
        sb.append("     A.PROMOTE_EMP_NO, ");
        sb.append("     A.MEMBER_ID, ");
        sb.append("     A.CLERK_ID, ");
        sb.append("     A.INTRODUCE_ID, ");
        sb.append("     '' AS TMP_ACCT_STATUS, ");
        sb.append("     A.ACCT_NO AS TMP_ACCT_NO, ");
        sb.append("     A.ENG_NAME, ");
        sb.append("     '' AS TMP_APPLY_ATM_FLAG, ");
        sb.append("     '' AS TMP_ACTIVATE_FLAG, ");
        sb.append("     A.ACCT_TYPE, ");
        sb.append("     '' AS TMP_CORP_NO, ");
        sb.append("     A.ISSUE_DATE, ");
        sb.append("     A.REISSUE_DATE, ");
        sb.append("     A.CHANGE_DATE, ");
        sb.append("     B.JOB_POSITION, ");
        sb.append("     B.MARKET_AGREE_BASE, ");
        sb.append("     NULL AS TMP_LINE_OF_CREDIT_AMT_CASH, ");
        sb.append("     B.OTHER_CNTRY_CODE, ");
        sb.append("     DECODE(B.E_NEWS,'Y','1','0') AS TMP_E_NEWS, ");
        sb.append("     A.ELECTRONIC_CODE, ");
        sb.append("     '' AS AUTOPAY_ACCT_NO, ");
        sb.append("     '' AS AUTOPAY_INDICATOR, ");
        sb.append("     0 AS RCRATE_YEAR, ");
        sb.append("     '901' AS TMP_CURR_CODE ");
        sb.append(" FROM ");
        sb.append("     DBC_CARD A, ");
        sb.append("     DBC_IDNO B, ");
        sb.append("     DBA_ACNO C, ");
        sb.append("     CCA_CARD_BASE D, ");
        sb.append("     CCA_CARD_ACCT E ");
        sb.append(" WHERE ");
        sb.append("     A.ID_P_SEQNO = B.ID_P_SEQNO ");
        sb.append("     AND A.P_SEQNO = C.P_SEQNO ");
        sb.append("     AND A.CARD_NO = D.CARD_NO ");
        sb.append("     AND A.P_SEQNO = E.ACNO_P_SEQNO ");
        sb.append(" ) tmp  ");
        sb.append(" where 1=1 ");
        if (whereStr != null && whereStr.length() > 0) {
            sb.append(" and ").append(whereStr);
        }
        if (orderStr != null && orderStr.length() > 0) {
            sb.append(" order by ").append(orderStr);
        }

        sqlCmd = sb.toString();
//        setString(1, searchDate); // 批次處理日期
        openCursor();
    }

    // 讀取正卡人ID(限信用卡)
    private String selectMajorIdNo(String majorIdPSeqno) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT ");
        sb.append("     ID_NO tmp_id_no ");
        sb.append(" FROM ");
        sb.append("     CRD_IDNO ");
        sb.append(" WHERE ");
        sb.append("     ID_P_SEQNO = ? ");

        sqlCmd = sb.toString();
        setString(1, majorIdPSeqno);

        int cardCnt = selectTable();
        if (cardCnt > 0) {
            return getValue("tmp_id_no");
        }
        return null;
    }

    // 讀取前置協商註記(限信用卡)
    void selectColLiacNegoFlag() throws Exception {
	    daoTable    = "COL_LIAC_NEGO";
		extendField = "liacnego.";
		sqlCmd = " select ID_P_SEQNO, LIAC_STATUS ";
		sqlCmd += " from COL_LIAC_NEGO ";
	    int n = loadTable();
	    setLoadData("liacnego.ID_P_SEQNO");
	    showLogMessage("I", "","selectColLiacNegoFlag 取得= [" +n+ "]筆");    	
    }
    
    // 讀取悠遊卡相關註記
    void selectTscCard() throws Exception {
	    daoTable    = "TSC_CARD";
		extendField = "tsccard.";
		sqlCmd = " select CARD_NO,AUTOLOAD_FLAG,LOCK_FLAG,RETURN_FLAG ";
		sqlCmd += " from TSC_CARD ";
	    int n = loadTable();
	    setLoadData("tsccard.CARD_NO");
	    showLogMessage("I", "","selectTscCard 取得= [" +n+ "]筆");
    }
    
    // 讀取悠遊VD卡相關註記
    void selectTscVdCard() throws Exception {    
	    daoTable    = "TSC_VD_CARD";
		extendField = "tscvdcard.";
		sqlCmd = " select VD_CARD_NO,AUTOLOAD_FLAG,LOCK_FLAG,RETURN_FLAG ";
		sqlCmd += " from TSC_VD_CARD ";
	    int n = loadTable();
	    setLoadData("tscvdcard.VD_CARD_NO");
	    showLogMessage("I", "","selectTscVdCard 取得= [" +n+ "]筆");	  
    }
    
    // ACT_DEBT_HST {循環信用金額}
    void selectActDebt() throws Exception {
	    daoTable    = "ACT_DEBT1";
		extendField = "debt1.";
		sqlCmd = " select A.P_SEQNO,  ";
        sqlCmd += "       A.CURR_CODE, ";
        sqlCmd += "       SUM(A.DC_END_BAL) AS SUM_END_BAL ";
        sqlCmd += " from ACT_DEBT A, PTR_ACTCODE B, PTR_WORKDAY C ";
		sqlCmd += " where A.ACCT_CODE = B.ACCT_CODE ";
		sqlCmd += "   and A.STMT_CYCLE = C.STMT_CYCLE ";
		sqlCmd += "   and A.ACCT_MONTH < C.THIS_ACCT_MONTH ";
		sqlCmd += "   group by A.P_SEQNO, A.CURR_CODE ";
	    int n = loadTable();
	    setLoadData("debt1.P_SEQNO,debt1.CURR_CODE");
	    showLogMessage("I", "","selectActDebt 取得= [" +n+ "]筆");
    }
    
    //  ACT_CURR_HST {本月消費金額}
    void selectActCurrHst() throws Exception {
	    daoTable    = "ACT_CURR_HST1";
		extendField = "currhst1.";
		sqlCmd = " select A.P_SEQNO,  ";
        sqlCmd += "       A.CURR_CODE, ";
        sqlCmd += "       A.STMT_NEW_AMT ";
        sqlCmd += " from ACT_CURR_HST A,  PTR_WORKDAY B ";
		sqlCmd += " where A.ACCT_MONTH= B.LAST_ACCT_MONTH ";
		sqlCmd += "   and B.STMT_CYCLE = '01' ";
	    int n = loadTable();
	    setLoadData("currhst1.P_SEQNO,currhst1.CURR_CODE");
	    showLogMessage("I", "","selectActCurrHst 取得= [" +n+ "]筆");
    }
    
    // VD本月消費金額
    void selectVdConsumeAmt() throws Exception {
	    daoTable    = "DBA_JRNL1";
		extendField = "dbajrnl1.";
		sqlCmd = " select A.P_SEQNO,  ";
        sqlCmd += "       SUM(A.TRANSACTION_AMT) AS VD_CONSUME_AMT ";
        sqlCmd += " from DBA_JRNL A,  PTR_WORKDAY B ";
		sqlCmd += " where SUBSTR(A.ACCT_DATE, 1, 6) = B.THIS_ACCT_MONTH ";
		sqlCmd += "   and A.TRAN_CLASS ='B' ";
		sqlCmd += "   and B.STMT_CYCLE = '01' ";
		sqlCmd += "   group by A.P_SEQNO ";
	    int n = loadTable();
	    setLoadData("dbajrnl1.P_SEQNO");
	    showLogMessage("I", "","selectVdConsumeAmt 取得= [" +n+ "]筆");    	    	
    }
    
    // 可用餘額(卡片)
    void selectCcaCardBalanceCal() throws Exception {
	    daoTable    = "CCA_CARD_BALANCE_CAL";
		extendField = "cardbalance.";
		sqlCmd = " select CARD_NO,  ";
		sqlCmd += "       CARD_AMT_BALANCE ";
        sqlCmd += " from CCA_CARD_BALANCE_CAL ";
	    int n = loadTable();
	    setLoadData("cardbalance.CARD_NO");
	    showLogMessage("I", "","selectCcaCardBalanceCal 取得= [" +n+ "]筆");    	    	
    }
    
    // 可用餘額(帳戶)
    void selectCcaAcctBalanceCal() throws Exception {
	    daoTable    = "CCA_ACCT_BALANCE_CAL";
		extendField = "acctbalance.";
		sqlCmd = " select ACNO_P_SEQNO,  ";
		sqlCmd += "       ACCT_AMT_BALANCE ";
        sqlCmd += " from CCA_ACCT_BALANCE_CAL ";
	    int n = loadTable();
	    setLoadData("acctbalance.ACNO_P_SEQNO");
	    showLogMessage("I", "","selectCcaAcctBalanceCal 取得= [" +n+ "]筆");    	    	
    }

    void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "CRM"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = fileFolder;
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;

        // 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
        String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

        showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
        int errCode = commFTP.ftplogName("CRM", ftpCommand);

        if (errCode != 0) {
            showLogMessage("I", "",
                    String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
            commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
            commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
        }
    }
    
	private void ftpProc(String rmFolder,String datFileName) throws Exception {

		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

		/**********
		 * COMM_FTP common function usage
		 ****************************************/
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */
		for (int inti = 0; inti < 1; inti++) {
			commFTP.hEflgSystemId = "RM"; /* 區分不同類的 FTP 檔案-大類 (必要) */
			commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
			commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
			commFTP.hEriaLocalDir = rmFolder;
			commFTP.hEflgModPgm = javaProgram;

			showLogMessage("I", "", "mput " + datFileName + " 開始傳送....");
			int errCode = commFTP.ftplogName("RM", "mput " + datFileName);

			if (errCode != 0) {
				showLogMessage("I", "", "ERROR:無法傳送 " + datFileName + " 資料" + " errcode:" + errCode);
				if (inti == 0)
					break;
			}
		}
	}

    public static void main(String[] args) {
        InfR009 proc = new InfR009();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }
}
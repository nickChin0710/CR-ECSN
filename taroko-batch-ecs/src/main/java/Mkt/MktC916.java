/*****************************************************************************************************
 *                                                                                                   *
 *                              MODIFICATION LOG                                                     *
 *                                                                                                   *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                      *
 *  ---------  --------- ----------- --------------------------------------------------------------  *
 *  112/10/11  V1.00.00  Ryan        program initial                                                 *   
 *****************************************************************************************************/
package Mkt;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class MktC916 extends AccessDAO {
    private final String PROGNAME = "天后宮滿10萬點燈 回饋名單產生處理  112/10/11 V1.00.00";
    CommFunction comm = new CommFunction();
    private CommCrd comc = new CommCrd();
    private CommDate commDate = new CommDate();
    private CommString commStr = new CommString();
    private CommFTP commFTP = null;
    private CommRoutine comr;
    private CommCrdRoutine comcr;

    private static final String PATH_FOLDER = "/media/mkt/";
    private static final String FILE_NAME1 = "TCB_LUGANGMAZU_YYYYMMDD.TXT";
    private static final String FILE_NAME2 = "TCB_LUGANGMAZU_YYYYMMDD_mobile.TXT";
	private final static String COL_SEPERATOR = ",";
	private final static String LINE_SEPERATOR = System.lineSeparator();
    
    private String hBusinessDate = "";
    private int fptr1 = -1;
    private String activeCode = "";
    int tolCnt = 0;
 	StringBuffer sb1 = new StringBuffer();
	StringBuffer sb2 = new StringBuffer();

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            showLogMessage("I", "", "Usage MktC916 [business_date]");

            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            
            String parmDate = "";
            if (args.length >= 1) {
            	parmDate = args[0];
                showLogMessage("I", "", String.format("程式參數1: [%s]", parmDate));
                if (!commDate.isDate(parmDate)) {
                    showLogMessage("I", "", "請傳入參數合格值: YYYYMMDD");
                    return -1;
                }
                hBusinessDate = parmDate;
            }else {
                if (!commStr.right(hBusinessDate, 4).equals("0110")) {
                    showLogMessage("I", "", "營業日[" + hBusinessDate +"]非執行日，程式不執行");
                    return 0;
                }
            }
            
            showLogMessage("I", "", String.format("執行日期[%s]", hBusinessDate));
            
            int resultCnt = selectMktChannelParm();
            if(resultCnt != 1) {
                showLogMessage("I", "", "執行結束,select mkt_channel_parm not found");
            	return 0;
            }

            selectMktChannelList();
            String fileName1 = FILE_NAME1.replace("YYYYMMDD", hBusinessDate);
            String fileName2 = FILE_NAME2.replace("YYYYMMDD", hBusinessDate);
            String fileData1 = sb1.toString();
            String fileData2 = sb2.toString();
            
            writeReport(fileName1,fileData1);
            writeReport(fileName2,fileData2);

            // ==============================================
            showLogMessage("I", "", "執行結束");
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }finally {
        	finalProcess();
        }
    }

    private void selectPtrBusinday() throws Exception {
        sqlCmd = "select BUSINESS_DATE from PTR_BUSINDAY ";
        selectTable();

        if (notFound.equals("Y")) {
            comc.errExit("執行結束, 營業日為空!!", "");
        }

        hBusinessDate = getValue("BUSINESS_DATE");
    }
    
    private int selectMktChannelParm() throws Exception {
    	extendField = "parm.";
        sqlCmd = "select b.active_code, c.wf_key, c.wf_value2 ";
        sqlCmd += " from mkt_channel_parm b left join ptr_sys_parm c on c.wf_key = b.outfile_type ";
        sqlCmd += " WHERE b.lottery_type = '3' and c.wf_parm = 'INOUTFILE_PARM' and c.wf_value2 like 'TCB_LUGANGMAZU_%' ";
        sqlCmd += " AND  b.cal_def_date = ? ";
        setString(1,hBusinessDate);
        selectTable();

        if (notFound.equals("Y")) {
            return 0;
        }

        activeCode = getValue("parm.active_code");
        showLogMessage("I", "", String.format("取得活動代碼 = [%s]", activeCode));
        return 1;
    }

    private void selectMktChannelList() throws Exception {
   
        sqlCmd = " select b.CARD_NO,d.birthday, d.CHI_NAME,e.bill_sending_zip, ";
        sqlCmd += " e.bill_sending_addr1||e.bill_sending_addr2||e.bill_sending_addr3||e.bill_sending_addr4||e.bill_sending_addr5 as bill_sending_addr, ";
        sqlCmd += " d.CELLAR_PHONE,sum(b.DEST_AMT) as fund_amt ";
        sqlCmd += " from mkt_channel_list a ";
        sqlCmd += " LEFT JOIN mkt_channel_bill b ON  b.active_code = a.active_code  AND b.major_card_no = a.card_no and b.error_code = '00' ";
        sqlCmd += " LEFT JOIN crd_card C ON c.CARD_NO = b.CARD_NO ";
        sqlCmd += " LEFT JOIN CRD_IDNO d ON d.ID_P_SEQNO = c.ID_P_SEQNO ";
        sqlCmd += " LEFT JOIN act_acno e ON e.P_SEQNO = c.P_SEQNO ";
        sqlCmd += " WHERE  a.active_code = ? ";
        sqlCmd += " group by b.CARD_NO,d.birthday, d.CHI_NAME,e.bill_sending_zip, ";
        sqlCmd += " e.bill_sending_addr1||e.bill_sending_addr2||e.bill_sending_addr3||e.bill_sending_addr4||e.bill_sending_addr5, ";
        sqlCmd += " d.CELLAR_PHONE HAVING sum(b.DEST_AMT) >= 100000 ORDER by b.card_no ";
        setString(1, activeCode);
        openCursor();
        while (fetchTable()) {
            tolCnt ++;
            String cardNo = getValue("card_no");
            String birthday = getValue("birthday");
            String chiName = getValue("CHI_NAME");
            String billSendingZip = getValue("bill_sending_zip");
            String billSendingAddr = getValue("bill_sending_addr");   
            String cellarPhone = getValue("cellar_phone");
            long fundAmt = getValueLong("fund_amt");
            writeText1(cardNo,birthday,chiName,billSendingZip,billSendingAddr);
            writeText2(cardNo,birthday,chiName,billSendingZip,billSendingAddr,cellarPhone,fundAmt);
            if(tolCnt % 1000 == 0)
            	showLogMessage("I", "", String.format("data proc cnt = [%d]", tolCnt));
        }
        closeCursor();
        return;
    }

    private void writeText1(String cardNo,String birthday,String chiName,String billSendingZip,String billSendingAddr) throws UnsupportedEncodingException {
		String hiCardNo = commStr.left(cardNo, 6) + "******" + commStr.right(cardNo, 4);
        sb1.append(comc.fixLeft(hiCardNo, 16)).append(COL_SEPERATOR);
        sb1.append(comc.fixLeft(String.format("%07d", commStr.ss2int(commDate.toTwDate(birthday))), 7)).append(COL_SEPERATOR);
        sb1.append(comc.fixLeft(chiName, 20)).append(COL_SEPERATOR);
        sb1.append(comc.fixLeft(billSendingZip, 3)).append(COL_SEPERATOR);
        sb1.append(comc.fixLeft(billSendingAddr, 60)).append(LINE_SEPERATOR);
        return;
    }
    
    private void writeText2(String cardNo,String birthday,String chiName,String billSendingZip,String billSendingAddr,String cellarPhone,long fundAmt) throws UnsupportedEncodingException {
        sb2.append(comc.fixLeft(cardNo, 16)).append(COL_SEPERATOR);
        sb2.append(comc.fixLeft(String.format("%07d", commStr.ss2int(commDate.toTwDate(birthday))), 7)).append(COL_SEPERATOR);
        sb2.append(comc.fixLeft(chiName, 20)).append(COL_SEPERATOR);
        sb2.append(comc.fixLeft(billSendingZip, 3)).append(COL_SEPERATOR);
        sb2.append(comc.fixLeft(billSendingAddr, 60)).append(COL_SEPERATOR);
        sb2.append(comc.fixLeft(cellarPhone, 10)).append(COL_SEPERATOR);
        sb2.append(comc.fixLeft(String.format("%013d", fundAmt), 13)).append(LINE_SEPERATOR);
        return;
    }
    
    
    private void writeReport(String filename, String filedata) throws Exception {
        fileOpen(filename);
        showLogMessage("I", "", "開始寫入檔案: " + filename);
        writeTextFile(fptr1, filedata);
        closeOutputText(fptr1);
        ftpProc(filename);
    }

    /*******************************************************************/
    private void fileOpen(String filename) throws Exception {
        String tempStr1 = String.format("%s%s%s", comc.getECSHOME(), PATH_FOLDER, filename);
        String fileName = Normalizer.normalize(tempStr1, java.text.Normalizer.Form.NFKD);
        fptr1 = openOutputText(fileName, "MS950");

        if (fptr1 == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void ftpProc(String filename) throws Exception {
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "TOHOST"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME() + PATH_FOLDER; // 相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

        String tmpChar = "put " + filename;

        int errCode = commFTP.ftplogName("CREDITCARD", tmpChar);

        if (errCode != 0) {
            showLogMessage("I", "", "檔案傳送 " + "CREDITCARD" + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "MktC916 執行完成 傳送  CREDITCARD 失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        // 刪除檔案 put 不用刪除
        renameFile(filename);
    }

    // ************************************************************************
    private void renameFile(String removeFileName) throws Exception {
    	String tmpStr1 = Paths.get(comc.getECSHOME(),PATH_FOLDER, removeFileName).toString();
    	String tmpStr2 = Paths.get(comc.getECSHOME(),PATH_FOLDER, "/backup" , removeFileName + "." + sysDate + sysTime).toString();

        if (!comc.fileRename2(tmpStr1, tmpStr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpStr2 + "]");
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC916 proc = new MktC916();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

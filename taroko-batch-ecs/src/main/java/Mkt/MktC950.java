/*****************************************************************************************************
 *                                                                                                   *
 *                              MODIFICATION LOG                                                     *
 *                                                                                                   *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                      *
 *  ---------  --------- ----------- --------------------------------------------------------------  *
 *  112/05/22  V1.00.00    Yang Bo      program initial                                               *                                   
 *  112/07/24  V1.00.01    Ryan         增加icash天天合庫日回饋明細資料                                                                                                                      *
 *  112/09/06  V1.00.02    Bo Yang      增加”輕鬆旅遊享回饋” 之回饋明細資料產檔                                                                                                        *
 *                         Grace        調整目錄至/media/mkt/                                            *
 *  112/09/07  V1.00.03    Bo Yang      OUTFILE_FORMAT_2 & 3 產出兩種報表                                                                                                  *
 *  112/09/15  V1.00.04    Grace        調整目錄至 CREDITCARD (參考IP代碼)                                  *
 *  112/10/06  V1.00.05    Grace        OUTFILE_FORMAT_2/3/4 產出檔案已分別移至mktC916, mktC915處理                            *                          
 *  112/11/09  V1.00.06    Ryan         OUTFILE_FORMAT_7 相同activeCode只處理一次                                                                          *
 *  112/11/15  V1.00.07    Grace        漢來滿額禮, 副檔名(R_HILAIGIFT_, R_HILAIWORLD_) 經與user協議以小寫上傳         *                       
 *  112/12/19  V1.00.08  Zuwei Su    errRtn改為 show message & return 1  *  
 *****************************************************************************************************/
package Mkt;

import com.*;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class MktC950 extends AccessDAO {
    private final String PROGNAME = "行銷活動-產生檔案下載處理 (一般名單) 112/11/09 V1.00.09 ";
    CommCrd comc = new CommCrd();
    CommString comStr = new CommString();
    CommFTP commFTP = null;
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;
    MktC950.Buf1 data = new MktC950.Buf1();

    private static final String OUTFILE_FORMAT_1 = "TcbList_YYYMMDD.TXT";
    private static final String OUTFILE_FORMAT_2 = "TCB_LUGANGMAZU_YYYYMMDD.TXT";	//天后宮10萬點燈名單, , 改於mktC916處理
    private static final String OUTFILE_FORMAT_3 = "TCB_LUGANGMAZU_YYYYMMDD_mobile.TXT";	//天后宮10萬點燈名單, , 改於mktC916處理
    private static final String OUTFILE_FORMAT_4 = "R_WORLDBRD_YYYYMM.TXT";		//漢來世界卡生日禮名單, 改於mktC915處理
    private static final String OUTFILE_FORMAT_5 = "R_HILAIGIFT_YYYYMM.txt";	//漢來滿額禮電子券-500元
    private static final String OUTFILE_FORMAT_6 = "R_HILAIWORLD_YYYYMM.txt";	//漢來滿額禮電子券-800元    
    private static final String OUTFILE_FORMAT_7 = "TRANS_ZTCBBANK_YYYYMMDD_2.TXT";	//統一超商天天合庫日
    private static final String OUTFILE_FORMAT_8 = "TCB_TRAVEL_YYYYMMDD.TXT";
    //private static final String PATH_FOLDER = "/reports/";
    private static final String PATH_FOLDER = "/media/mkt/";
    private final static String LINE_SEPARATOR = System.lineSeparator();

    private String hProcDate = "";
    private String hBusinessDate = "";
    private String hBusinessMonth = "";
    private int totCnt = 0, totCnt7 = 0;
    private final Map<String, StringBuilder> docMap = new HashMap<>(6);
    private int fptr1 = -1;
    private String docName7 = "";

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================

            // 固定要做的
            if (!connectDataBase()) {
//                comc.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error" );
                return 1;
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());


            selectPtrBusinday();
            hProcDate = hBusinessDate;

            if (args.length == 1 && args[0].length() == 8) {
                hProcDate = args[0];
            }
            showLogMessage("I", "", "本日營業日期=[" + hProcDate + "]");

            selectMktNormalList();
            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /**
     * 1. 取得 mkt_normal_list 資料
     * 2. 依 mkt_normal_list.active_code 取得 mkt_channel_parm 的 outfile_type
     * 3. 依 mkt_channel_parm.outfile_type, 取得 ptr_sys_parm 對應之產檔檔名 wf_value2
     * 4. 依 mkt_normal_list.id_p_seqno, 取得 crd_idno 對應之持卡人信息 [chi_name, id_no, cellar_phone, birthday]
     * 5. 依 mkt_normal_list.p_seqno, 取得 act_acno 對應之帳號信息 [bill_sending_zip, bill_sending_addr]
     */
    private void selectMktNormalList() throws Exception {
    	HashMap<String,String> activeCodeMap = new HashMap<String,String>();
        sqlCmd = "  select a.active_code, a.id_p_seqno, a.p_seqno, a.feedback_int, ";
        sqlCmd += "        d.chi_name, d.id_no, d.cellar_phone, d.birthday, d.e_mail_addr, ";
        sqlCmd += "        e.bill_sending_zip, e.bill_sending_addr1||e.bill_sending_addr2||e.bill_sending_addr3||e.bill_sending_addr4||e.bill_sending_addr5 as bill_sending_addr, ";
        sqlCmd += "        c.wf_key as outfile_type, c.wf_value2 as outfile_name ";
        sqlCmd += " from mkt_normal_list a ";
        sqlCmd += " left join mkt_channel_parm b ";
        sqlCmd += "        on b.active_code = a.active_code ";
        sqlCmd += "       and b.cal_def_date = a.cal_def_date ";
        sqlCmd += "       and b.lottery_type = '3' ";
        sqlCmd += " left join ptr_sys_parm c ";
        sqlCmd += "        on c.wf_key = b.outfile_type ";
        sqlCmd += "       and c.wf_parm = 'INOUTFILE_PARM' ";
        sqlCmd += " left join crd_idno d ";
        sqlCmd += "        on d.id_p_seqno = a.id_p_seqno ";
        sqlCmd += " left join act_acno e ";
        sqlCmd += "        on e.p_seqno = a.p_seqno ";
        sqlCmd += " where a.cal_def_date = ? ";
        setString(1, hProcDate);
        openCursor();
        while (fetchTable()) {
            data.initData();
            data.activeCode = getValue("active_code");
            data.idPSeqno = getValue("id_p_seqno");
            data.pSeqno = getValue("p_seqno");
            data.idNo = getValue("id_no");
            data.chiName = getValue("chi_name");
            data.birthday = getValue("birthday");
            data.cellarPhone = getValue("cellar_phone");
            data.eMailAddr = getValue("e_mail_addr");
            data.billSendingZip = getValue("bill_sending_zip");
            data.billSendingAddr = getValue("bill_sending_addr");
            data.outfileType = getValue("outfile_type");
            data.outfileFormat = getValue("outfile_name");
            data.feedbackInt = getValueInt("feedback_int");

            if (comStr.empty(data.outfileFormat)) {
                showLogMessage("I", "", "ERROR : [OUTFILE_TYPE: " + data.outfileType + "] 產檔檔名為空, 無法產生檔案!");
                continue;
            }

            String docText = "";
            String docName = data.getDocName();
            switch (data.outfileFormat) {
                case OUTFILE_FORMAT_1:
                    docText = data.getDocText1();
                    break;
                // 產出兩種報表
                //天后宮10萬點燈名單, , 改於mktC916處理
                /*    
                case OUTFILE_FORMAT_2:
                case OUTFILE_FORMAT_3:
                    // 產出'TCB_LUGANGMAZU_YYYYMMDD.TXT報表
                    data.outfileFormat = OUTFILE_FORMAT_2;
                    selectMktChannelList();
                    docName = data.getDocName();
                    docText = data.getDocText2();
                    writeDataToMap(docMap, docName, docText);

                    // 產出'TCB_LUGANGMAZU_YYYYMMDD_mobile.TXT報表
                    data.outfileFormat = OUTFILE_FORMAT_3;
                    selectMktChannelList2();
                    docName = data.getDocName();
                    docText = data.getDocText3();
                    break;
                    */
                //漢來世界卡生日禮名單, 改於mktC915處理
                /*
                case OUTFILE_FORMAT_4:
                    selectMktChannelList();
                    selectCrdCard();
                    docText = data.getDocText4();
                    break;
                    */
                case OUTFILE_FORMAT_5:
                case OUTFILE_FORMAT_6:
                    selectMktChannelList3();
                    selectCrdCard();
                    docText = data.getDocText5();
                    break;
                case OUTFILE_FORMAT_7:
                	if(activeCodeMap.get(data.activeCode)!=null) break;
                	activeCodeMap.put(data.activeCode, data.activeCode);
                    docText = selectMktNormalList2();
                    docName7 = docName;
                    break;
                case OUTFILE_FORMAT_8:
                    if (selectMktChannelBill() == 0) {
                        docText = data.getDocText8();
                    }
                    break;
                default:
                    showLogMessage("I", "", "ERROR : [OUTFILE_TYPE: " + data.outfileType + "] 產檔檔名無效, 無法產生檔案!");
                    continue;
            }

            /*
             * 由於同一系統日可能存在不同的active_code
             * 即可能需要產出多種檔案
             * 則需要依據不同的產檔格式緩存各自的text body
             */
            writeDataToMap(docMap, docName, docText);

            totCnt++;
            if (totCnt % 1000 == 0) {
                showLogMessage("I", "", "已處理 [" + totCnt + "]筆");
            }
        }

        if (docName7.length() > 0) {
            String docText = data.getDocText7H();
            if (docMap.containsKey(docName7)) {
                StringBuilder builder = docMap.get(docName7);
                builder.insert(0, docText);
            }
        }
        closeCursor();
        showLogMessage("I", "", String.format("Process records = [%d]\n", totCnt));

        // 寫入檔案
        writeReport();
    }

    private void writeDataToMap(Map<String, StringBuilder> map, String docName, String docText) {
        if (map.containsKey(docName)) {
            StringBuilder builder = map.get(docName);
            builder.append(docText);
        } else {
            StringBuilder builder = new StringBuilder(docText);
            map.put(docName, builder);
        }
    }

    private void selectMktChannelList() throws Exception {
        sqlCmd = "  select card_no ";
        sqlCmd += " from mkt_channel_list ";
        sqlCmd += " where active_code = ? ";
        sqlCmd += "   and id_p_seqno = ? ";
        sqlCmd += "   and p_seqno = ? ";
        setString(1, data.activeCode);
        setString(2, data.idPSeqno);
        setString(3, data.pSeqno);

        if (selectTable() > 0) {
            data.cardNo = getValue("card_no");
        }
    }

    private void selectMktChannelList2() throws Exception {
        sqlCmd = "  select card_no, right(digits(cast(sum(fund_amt) as bigint)), 13)||'00' as fund_amt ";
        sqlCmd += " from mkt_channel_list ";
        sqlCmd += " where active_code = ? ";
        sqlCmd += "   and id_p_seqno = ? ";
        sqlCmd += "   and p_seqno = ? ";
        sqlCmd += " group by card_no ";
        setString(1, data.activeCode);
        setString(2, data.idPSeqno);
        setString(3, data.pSeqno);

        if (selectTable() > 0) {
            data.cardNo = getValue("card_no");
            data.fundAmt = getValue("fund_amt");
        }
    }

    private void selectMktChannelList3() throws Exception {
        sqlCmd = "  select card_no, sum(gift_int) as gift_int, sum(cast(gift_amt as integer)) as gift_amt ";
        sqlCmd += " from mkt_channel_list ";
        sqlCmd += " where active_code = ? ";
        sqlCmd += "   and id_p_seqno = ? ";
        sqlCmd += "   and p_seqno = ? ";
        sqlCmd += " group by card_no ";
        setString(1, data.activeCode);
        setString(2, data.idPSeqno);
        setString(3, data.pSeqno);

        if (selectTable() > 0) {
            data.cardNo = getValue("card_no");
            data.giftInt = getValue("gift_int");
            data.giftAmt = getValue("gift_amt");
        }
    }

    private void selectCrdCard() throws Exception {
        sqlCmd = "  select substr(group_code, 1, 3) as group_code ";
        sqlCmd += " from crd_card ";
        sqlCmd += " where card_no = ? ";
        setString(1, data.cardNo);

        if (selectTable() > 0) {
            data.groupCode = getValue("group_code");
        }
    }

    private String selectMktNormalList2() throws Exception {
        String docText = "";
        extendField = "normal_list2.";
        sqlCmd = "SELECT d.icash_cardno, sum(d.tx_amt) as sum_tx_amt ";
        sqlCmd += "FROM MKT_NORMAL_LIST a, CRD_CARD b, ich_card c, mkt_openpoint_data d ";
        sqlCmd += "WHERE a.ACTIVE_CODE = ? ";
        sqlCmd += "AND a.ID_P_SEQNO = b.ID_P_SEQNO ";
        sqlCmd += "And b.card_no = c.card_no ";
        sqlCmd += "And c.ich_card_no = d.ICASH_CARDNO ";
        sqlCmd += "And b.electronic_code ='03' ";
        sqlCmd += "AND substr(d.tx_date,1,6) = ? ";
        sqlCmd += "GROUP BY d.icash_cardno ";
        setString(1, data.activeCode);
        setString(2, hBusinessMonth);
        int n = selectTable();
        for (int i = 0; i < n; i++) {
            data.ichCardNo = getValue("normal_list2.icash_cardno", i);
            data.sumTxAmt = getValueDouble("normal_list2.sum_tx_amt", i);
            docText += data.getDocText7D();
            totCnt7++;
        }
        return docText;
    }

    private int selectMktChannelBill() throws Exception {
        sqlCmd = "select b.card_no, b.dest_amt, " +
                "        f.cellar_phone, f.chi_name, f.id_no as bill_id_no  " +
                " from mkt_channel_bill b " +
                " left join crd_card e " +
                "      on b.card_no = e.card_no " +			//b.card_no: 刷卡人卡號
                " left join crd_idno f " +
                "      on e.id_p_seqno = f.id_p_seqno " +	//e.id_p_seqno: 刷卡人id_p_seqno	
                " where b.error_code = '00' " +
                "   and b.active_code = ? " +
                "   and b.id_p_seqno = ? ";		
        setString(1, data.activeCode);
        setString(2, data.idPSeqno);	//主卡人

        if (selectTable() > 0) {
            data.billIdNo = getValue("bill_id_no");
            data.cardNo = getValue("card_no");
            data.destAmt = getValueDouble("dest_amt");
            data.cellarPhone = getValue("cellar_phone");
            data.chiName = getValue("chi_name");
            return 0;
        }

        return -1;
    }

    private void selectPtrBusinday() throws Exception {

        sqlCmd = "select business_date, to_char(to_date(business_date,'yyyymmdd') - 1 months ,'yyyymm') as business_month ";
        sqlCmd += "from ptr_businday fetch first 1 row only ";

        selectTable();

        if (notFound.equals("Y")) {
            showLogMessage("I", "", "select ptr_businday error!");
            exitProgram(1);
        }
        hBusinessDate = getValue("business_date");
        hBusinessMonth = getValue("business_month");
    }

    private void writeReport() throws Exception {
        // 依據不同的產檔格式, 分別產出檔案及傳送至遠端
        for (Map.Entry<String, StringBuilder> entry : docMap.entrySet()) {
            String filename = entry.getKey();

            fileOpen(filename);
            showLogMessage("I", "", "開始寫入檔案: " + filename);
            writeTextFile(fptr1, entry.getValue().toString());
            closeOutputText(fptr1);
            ftpProc(filename);
        }
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
        //commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "TOHOST"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME() + PATH_FOLDER;    //相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

        String tmpChar = "put " + filename;

        //int errCode = commFTP.ftplogName("NCR2EMP", tmpChar);
        int errCode = commFTP.ftplogName("CREDITCARD", tmpChar);	//20230915, grace

        if (errCode != 0) {
            //showLogMessage("I", "", "檔案傳送 " + "NCR2EMP" + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "檔案傳送 " + "CREDITCARD" + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "MktC950 執行完成 傳送 CREDITCARD 失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        // 刪除檔案 put 不用刪除
        renameFile(filename);
    }

    // ************************************************************************
    public void renameFile(String removeFileName) throws Exception {
        String tmpStr1 = comc.getECSHOME() + PATH_FOLDER + removeFileName;
        String tmpStr2 = comc.getECSHOME() + PATH_FOLDER + removeFileName + "." + sysDate + sysTime;

        if (!comc.fileRename2(tmpStr1, tmpStr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpStr2 + "]");
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC950 proc = new MktC950();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    class Buf1 {
        String activeCode = "";
        String outfileType = "";
        String outfileFormat = "";
        String idNo = "";
        String cardNo = "";
        String chiName = "";
        String idPSeqno = "";
        String pSeqno = "";
        String billSendingZip = "";
        String billSendingAddr = "";
        String cellarPhone = "";
        String birthday = "";
        String fundAmt = "";
        String groupCode = "";
        String giftInt = "";
        String giftAmt = "";
        String eMailAddr = "";
        String ichCardNo = "";
        double sumTxAmt = 0;
        double destAmt = 0;
        Integer feedbackInt = 0;
        String billIdNo = "";

        void initData() {
            activeCode = "";
            outfileType = "";
            outfileFormat = "";
            idNo = "";
            cardNo = "";
            chiName = "";
            idPSeqno = "";
            pSeqno = "";
            billSendingZip = "";
            billSendingAddr = "";
            cellarPhone = "";
            birthday = "";
            fundAmt = "";
            groupCode = "";
            giftInt = "";
            giftAmt = "";
            eMailAddr = "";
            ichCardNo = "";
            sumTxAmt = 0;
        }

        String getDocText1() {
            return comStr.rpad(chiName, 20) + "," + // 持卡人姓名
                    comStr.rpad(idNo, 10) + "," + // 持卡人ID
                    comStr.rpad(billSendingZip, 3) + "," + // 郵遞區號
                    comStr.rpad(billSendingAddr, 60) + "," + // 通訊地址
                    comStr.rpad(cellarPhone, 15) + // 手機號碼
                    LINE_SEPARATOR;
        }

        String getDocText2() {
            String tCardNo = !comStr.empty(cardNo)
                    ? cardNo.substring(0, 7) + "******" + cardNo.substring(12)
                    : cardNo;
            String tBirthday = !comStr.empty(birthday)
                    ? (Integer.parseInt(birthday.substring(0, 4)) - 1911) + birthday.substring(4)
                    : birthday;
            return comStr.rpad(tCardNo, 16) + "," + // 信用卡卡號
                    comStr.lpad(tBirthday, 7, "0") + "," + // 民國出生年月日
                    comStr.rpad(chiName, 20) + "," + // 持卡人姓名
                    comStr.rpad(billSendingZip, 3) + "," + // 郵遞區號
                    comStr.rpad(billSendingAddr, 60) + // 通訊地址
                    LINE_SEPARATOR;
        }

        String getDocText3() {
            String tBirthday = !comStr.empty(birthday)
                    ? (Integer.parseInt(birthday.substring(0, 4)) - 1911) + birthday.substring(4)
                    : birthday;
            return comStr.rpad(cardNo, 16) + "," + // 信用卡卡號
                    comStr.lpad(tBirthday, 7, "0") + "," + // 民國出生年月日
                    comStr.rpad(chiName, 20) + "," + // 持卡人姓名
                    comStr.rpad(billSendingZip, 3) + "," + // 郵遞區號
                    comStr.rpad(billSendingAddr, 60) + "," + // 通訊地址
                    comStr.rpad(cellarPhone, 15) + "," + // 手機號碼
                    fundAmt + // 年度累積金額
                    LINE_SEPARATOR;
        }

        String getDocText4() {
            return comStr.rpad(idNo, 10) + "," + // 持卡人ID
                    comStr.rpad(groupCode, 3) + "," + // Type (團代別,後3碼)
                    comStr.rpad(cardNo, 16) + "," + // 信用卡卡號
                    comStr.rpad(cellarPhone, 15) + "," + // 手機號碼
                    comStr.rpad(billSendingZip, 3) + "," + // 郵遞區號
                    comStr.rpad(chiName, 20) + "," + // 持卡人姓名
                    comStr.rpad(billSendingAddr, 60) + "," + // 通訊地址
                    comStr.rpad(birthday, 8) + // 出生年月日
                    LINE_SEPARATOR;
        }

        String getDocText5() {
            return comStr.rpad(idNo, 10) + "," + // 持卡人ID
                    comStr.rpad(groupCode, 3) + "," + // Type (團代別,後3碼)
                    comStr.rpad(cardNo, 16) + "," + // 信用卡卡號
                    comStr.lpad(giftInt, 4) + "," + // 漢來美食累積次數
                    comStr.lpad(giftAmt, 8) + "," + // 漢來美食累積金額
                    comStr.rpad(cellarPhone, 15) + "," + // 手機號碼
                    comStr.rpad(eMailAddr, 50) + "," + // 電子郵件
                    comStr.rpad(billSendingZip, 3) + "," + // 郵遞區號
                    comStr.rpad(chiName, 20) + "," + // 持卡人姓名
                    comStr.rpad(billSendingAddr, 60) + // 通訊地址
                    LINE_SEPARATOR;
        }

        String getDocText7D() {
            return comStr.rpad("D", 1) + // 記錄識別碼 記錄識別碼D
                    comStr.rpad("808070", 6) + // 固定值'808070'
                    comStr.rpad("70", 2) + // 固定值'70'
                    comStr.rpad(hProcDate, 8) + // 營業日(西元年月日)
                    comStr.rpad("103030", 6) + // 固定值'103030'
                    comStr.rpad("2018001954", 10) + //固定值'2018001954' 
                    comStr.rpad("01", 2) + // 固定值'01'
                    comStr.lpad(String.valueOf(400000 + totCnt7), 6) + // 由400000起, 逐筆加1;即第1筆資料, 給定400001 
                    comStr.rpad("020B", 4) + // '020B'
                    comStr.rpad(data.ichCardNo, 20, "0") + // Icash卡號, 左靠右補滿’0’; 參考[附件二] 步驟4.SQL 的 d.ich_card_no;
                    comStr.lpad(String.format("%09.2f", (data.sumTxAmt * 3) / 100), 9) +
                    comStr.rpad("0", 10, "0") + //固定值, 10個’0’
                    comStr.rpad("0", 20, "0") + //固定值, 20個’0’
                    comStr.rpad("0", 8, "0") + //固定值, 8個’0’
                    comStr.rpad("0", 10, "0") + //固定值, 10個’0’
                    comStr.rpad("00", 2) + //固定值, 2個’0’
                    comStr.rpad("0", 6, "0") + //固定值, 6個’0’
                    comStr.rpad("5", 1) + //固定值 ’5’
                    comStr.rpad("00", 2) + //固定值, 2個’0’
                    comStr.rpad("20000002", 8) + //固定值, '20000002'
                    comStr.rpad("00", 2) + //固定值, 2個’0’
                    comStr.rpad(" ", 19) + //19個空白
                    LINE_SEPARATOR;
        }

        String getDocText7H() {
            return comStr.rpad("H", 1) + // 記錄識別碼 固定值’H’
                    comStr.rpad("-20", 3) + // 類別 固定值'-20'
                    comStr.rpad("0099629524", 10) + // 身分證號 固定值'0099629524'
                    comStr.rpad(hProcDate, 8) + // 日期 營業日(西元年月日)
                    comStr.lpad(String.format("%08d", totCnt7), 8) + // 資料筆數
                    comStr.rpad(" ", 132) + // Filler 132個空白
                    LINE_SEPARATOR;
        }

        String getDocText8() {
            return comStr.rpad(idNo, 10) + "," + // 正卡ID
                    comStr.rpad(cardNo, 16) + "," + // 卡號
                    comStr.rpad(String.format("%14.2f", data.destAmt), 14) + "," + // 消費金額
                    comStr.rpad(String.valueOf(data.feedbackInt), 4) + "," + // 回饋金額
                    comStr.rpad(billIdNo, 10) + "," + // 持卡人ID
                    comStr.rpad(cellarPhone, 15) + "," + // 手機號碼
                    comStr.rpad(chiName, 20) + // 持卡人姓名
                    LINE_SEPARATOR;
        }

        String getDocName() {
            String hhProcDate = Integer.parseInt(hProcDate.substring(0, 4)) - 1911 + hProcDate.substring(4);
            return outfileFormat
                    .replaceAll("YYYYMMDD", hProcDate)
                    .replaceAll("YYYMMDD", hhProcDate)
                    .replaceAll("YYYYMM", hProcDate.substring(0, 6))
                    .replaceAll("YYYMM", hhProcDate.substring(0, 5))
                    .replaceAll("YYYY", hProcDate.substring(0, 4))
                    .replaceAll("YYY", hhProcDate.substring(0, 3));
        }
    }
}
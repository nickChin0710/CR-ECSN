/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                   DESCRIPTION               *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112/03/23  V1.00.00   Yang, Bo                program initial             *
 *  112/03/24  V1.00.01   Zuwei Su                輸出欄位調整             *
 *  112/06/16  V1.00.02   Alex                    產出日期為營業日-1天                                   *
 *****************************************************************************/
package Inf;

import com.BaseBatch;
import com.CommCrd;
import com.CommTxInf;

import java.nio.file.Paths;

public class InfS007 extends BaseBatch {
    private static final int OUTPUT_BUFF_SIZE = 5000;
    private final String PROGNAME = "已授權未請款 112-06-16 V1.00.02";
    CommCrd comc = new CommCrd();
    CommCrd commCrd = new CommCrd();
    private static final String NEW_CENTER_FOLDER = "/crdatacrea/NEWCENTER/";
    private static final String DATA_FORM = "CCTAUTX";
    private static final String WORD_SEPARATOR = "\006";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private int dataCount = 0;

    // =****************************************************************************
    public static void main(String[] args) {
        InfS007 proc = new InfS007();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }

    @Override
    protected void dataProcess(String[] args) throws Exception {
    }

    @Override
    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            String searchDate = (args.length == 0) ? "" : args[0].trim();
            showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
            if ("".equals(searchDate)) {
                // 查詢PtrBusinday
                selectPtrBusinday();
                searchDate = hBusiDate;
                searchDate = commDate.dateAdd(searchDate, 0, 0, -1);
            }            
            searchDate = getProgDate(searchDate, "D");
            showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

            String fileNameSearchDate = searchDate;
            String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
            String fileFolder = Paths.get(NEW_CENTER_FOLDER).toString();

            // 產生主要檔案 .DAT 
            int dataCount = generateDatFile(fileFolder, datFileName, searchDate);

            // 產生Header檔
            CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
            dateTime(); // update the system date and time
            boolean isGenerated = commTxInf.generateTxtCSRHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0, 4), dataCount);
            if (!isGenerated) {
                comc.errExit("產生HDR檔錯誤!", "");
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
     * @param fileFolder  檔案的資料夾路徑
     * @param datFileName .dat檔的檔名
     * @return the number of rows written. If the returned value is -1, it means the path or the file does not exist.
     * @throws Exception
     */
    private int generateDatFile(String fileFolder, String datFileName, String searchDate) throws Exception {

        String datFilePath = Paths.get(fileFolder, datFileName).toString();
        boolean isOpen = openBinaryOutput(datFilePath);
        if (isOpen == false) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
            return -1;
        }

        int rowCount = 0;
        int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
        try {
            StringBuffer sb = new StringBuffer();
            showLogMessage("I", "", "開始產生.DAT檔......");

            showLogMessage("I", "", "開始處理cca_auth_txlog檔......");
            // 讀取已授權未請款資料
            selectUnWithdrawn(searchDate);
            while (fetchTable()) {
                String rowOfDAT = getRowOfDAT();
                sb.append(rowOfDAT);
                rowCount++;
                countInEachBuffer++;
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                    showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
                    //byte[] tmpBytes = sb.toString().getBytes("MS950");
                    byte[] tmpBytes = sb.toString().getBytes();
                    writeBinFile(tmpBytes, tmpBytes.length);
                    sb = new StringBuffer();
                    countInEachBuffer = 0;
                }
            }
            closeCursor();

            // write the rest of bytes on the file 
            if (countInEachBuffer > 0) {
                showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
                //byte[] tmpBytes = sb.toString().getBytes("MS950");
                byte[] tmpBytes = sb.toString().getBytes();
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

    private void selectUnWithdrawn(String searchDate) throws Exception {
        String sql1 = "select uf_idno_id2(id_p_seqno,acct_type) as id_no, card_no, tx_date, ori_amt, auth_no, " +
                " mcc_code, auth_source, stand_in, consume_country, group_code, tx_currency" +
                " from cca_auth_txlog" +
                " where mtch_flag <> 'Y' " +
                "   and cacu_amount = 'Y' " +
                "   and tx_date = ? ";
        setString(1, searchDate);
        sqlCmd = sql1;
        openCursor();
    }

    /**
     * 產生檔案
     *
     * @return String
     * @throws Exception
     */
    private String getRowOfDAT() throws Exception {
        String idNo;
        String cardNo;
        String txDate;
        double oriAmt;
        String authNo;
        String mccCode;
        String authSource;
        String standIn;
        String groupCode;
        String txCurrency;
        String consumeCountry;
        String countryNo;

        dataCount++;
        idNo = getValue("id_no");
        cardNo = getValue("card_no");
        txDate = getValue("tx_date");
        String txDateTw = commCrd.fixRight("000" + commDate.toTwDate(txDate), 7);
        oriAmt = getValueDouble("ori_amt");
        String strOriAmt = String.format("%014.2f", oriAmt);
        authNo = getValue("auth_no");
        mccCode = getValue("mcc_code");
        authSource = getValue("auth_source");
        standIn = getValue("stand_in");
        groupCode = getValue("group_code");
        txCurrency = getValue("tx_currency");
        consumeCountry = getValue("consume_country");
        countryNo = selectCountryCode(consumeCountry);

        StringBuffer sb = new StringBuffer();
        sb.append(commCrd.fixLeft(idNo, 16));
        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append(commCrd.fixLeft(cardNo, 16));
        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append(txDateTw);
        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append(commCrd.fixLeft(strOriAmt, 14));
        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append(commCrd.fixLeft(authNo, 6));
        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append("45");
        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append(commCrd.fixLeft(mccCode+" " + authSource+" " + standIn+" " + countryNo+" " + strOriAmt+" " + txCurrency, 40));
        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append(commCrd.fixLeft("0"+mccCode, 5)); //交易金額(放清算金額)
        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append(commCrd.fixLeft(groupCode,4));
        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append(commCrd.fixLeft(txCurrency,3));
//        sb.append(commCrd.fixLeft(WORD_SEPARATOR, 1));
        sb.append(LINE_SEPARATOR);

        return sb.toString();
    }

    private String selectCountryCode(String consumeCountry) throws Exception {
        String sql1 = " select country_no from cca_country where ? in (country_code, bin_country) ";
        setString(1, empty(consumeCountry) ? "" : consumeCountry);
        sqlSelect(sql1);
        if (sqlNrow > 0) {
            return colSs("country_no");
        }
        return "";
    }
}

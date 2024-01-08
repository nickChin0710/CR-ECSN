/*****************************************************************************************************
 *                                                                                                   *
 *                              MODIFICATION LOG                                                     *
 *                                                                                                   *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                      *
 *  ---------  --------- ----------- --------------------------------------------------------------  *
 *  112/10/24  V1.00.00  Zuwei Su    program initial (雄獅活動LionTran_xxxx, LionAux)                   *
 *  112/11/02  V1.00.01  Grace Huang LionTran_xxxx產檔日期為16日, LionAux_xxxx產檔日期為25日以後的每天執行                         * 
 *  112/12/19  V1.00.02  Zuwei Su    errRtn改為 show message & return 1  *  
 *****************************************************************************************************/
package Mkt;

import com.*;

import java.text.Normalizer;

public class MktC955 extends AccessDAO {
    private final String PROGNAME = "行銷活動-產生檔案下載處理 (個別需求) 112/10/24 V1.00.00";
    CommCrd commCrd = new CommCrd();
    CommDate commDate = new CommDate();
    CommFTP commFTP = null;
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;

    private static final String PATH_FOLDER = "/media/mkt/";

    private String hBusinessDate = "";
    private String lastMonth = "";
    private int totCnt = 0;
    private int fptr1 = -1;
    
    private StringBuffer liontranStr = new StringBuffer(102400);
    private StringBuffer lionauxStr = new StringBuffer(102400);

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
//                commCrd.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error" );
                return 0;
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());


            selectPtrBusinday();

            if (args.length == 1 && args[0].length() == 8) {
                hBusinessDate = args[0];
            }
            //String lastMonth = commDate.monthAdd(hBusinessDate, -1);			//上個月
            lastMonth = commDate.monthAdd(hBusinessDate, -1);           //上個月
            showLogMessage("I", "", "本日營業日期=[" + hBusinessDate + "]");
            showLogMessage("I", "", "上個月=["+ lastMonth + "]");
            
            //檢核營業日
            String purchaseDateStart = null;
            String purchaseDateEnd = null;
        	if (hBusinessDate.substring(6).compareTo("16") == 0) { //LionTran_產檔時間16日, 
        	    purchaseDateStart = lastMonth + "01";
        	    purchaseDateEnd = commDate.dateAdd(hBusinessDate.substring(0, 6) + "01", 0, 0, -1);
        	    selectMktChannelParm(purchaseDateStart, purchaseDateEnd);
        	} else if (hBusinessDate.substring(6).compareTo("25") >= 0) {	//LionAux_產檔時間25日以後
                //purchaseDateStart = hBusinessDate.substring(6) + "25";
                purchaseDateStart = hBusinessDate.substring(0, 6) + "25";
                purchaseDateEnd = hBusinessDate;
                selectMktChannelParm(purchaseDateStart, purchaseDateEnd);
        	} else {
        		showLogMessage("I", "", "程式執行, 只得營業日為16日, 或, 25日以後 !! ");
        	}

//            selectMktChannelParm();
  
            //writeReport();	//暫包至selectMktChannelParm()內
            
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", " " );
            comcr.hCallErrorDesc = String.format("程式執行結束, 筆數 =[%d]", totCnt);
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    
    // 取得雄獅 ”活動代號”, “消費期間”、“累積最低消費金額” (mkt_channel_parm.sum_amt)、 “特店中文名稱” 參數
    private void selectMktChannelParm(String purchaseDateStart, String purchaseDateEnd ) throws Exception {
    	showLogMessage("I", "", " " );
    	showLogMessage("I", "", "===[selectMktChannelParm()] ===================================" );
    	
    	String month = purchaseDateEnd.substring(0, 6);
    	showLogMessage("I", "", "傳入參數: purchaseDateStart=["+purchaseDateStart+"], purchaseDateEnd=["+purchaseDateEnd+"], get month=["+month+"]" );
    	showLogMessage("I", "", " " );
        sqlCmd = "SELECT distinct a.ACTIVE_CODE, a.mcht_cname_sel, a.purchase_date_s, a.purchase_date_e, a.sum_amt_cond, a.sum_amt "
                + " ,b.DATA_CODE "
                + "FROM MKT_CHANNEL_PARM  a     "
                + "inner join MKT_BN_CDATA b on b.data_key = a.active_code and b.TABLE_NAME='MKT_CHANNEL_PARM' "
                + "WHERE a.ACTIVE_CODE LIKE 'LION%'  " //         --指定"雄獅旅遊活動"
//                + "AND ? >=a.purchase_date_s  " //   --營業日介於消費期間 
//                + "AND ? <=a.purchase_date_e  "
                + "AND ? >=substr(a.purchase_date_s,1,6)  " //   --營業日介於消費期間 
                + "AND ? <=substr(a.purchase_date_e,1,6)  "
                + "AND a.SUM_AMT_COND='Y' " // --勾選'累積最低金額'
                + "AND a.SUM_AMT>0        "; // --有設定'累積最低金額'
        //setString(1, hBusinessDate);        
        //setString(2, hBusinessDate);
        setString(1, month);
        setString(2, month);
        
        int cnt = selectTable();
        if (cnt == 0) {
            showLogMessage("I", "", "MKT_CHANNEL_PARM data NOT found !!! ");
            //showLogMessage("I", "", lastMonth);
            //exitProgram(1);
            return ;
        } else {
        	for (int i = 0; i < cnt; i++) {
        		showLogMessage("I", "", "GET mcht_cname_sel=[" + getValue("mcht_cname_sel", i) + "], data_code(特店)=[" + getValue("DATA_CODE", i) + "], sum_amt(累消費金額)=[" + getValueInt("sum_amt", i)+"]" );
        		selectBilBillData(purchaseDateStart, purchaseDateEnd, getValue("mcht_cname_sel", i), getValue("DATA_CODE", i), getValueInt("sum_amt", i));        		
        	}
        	//writeReport(purchaseDateEnd);	//原at selectMktChannelParm()
        	writeReport(hBusinessDate);	//原at selectMktChannelParm()
        }
    }
    
    private void selectBilBillData(String purchaseDateStart, String purchaseDateEnd, String mchtCnameSel, String dataCode, int sumAmt ) throws Exception {
        String month = purchaseDateEnd.substring(0, 6);
        String day = purchaseDateEnd.substring(6);
    	sqlCmd = "SELECT  b.CHI_NAME, b.ID_NO, a.CARD_NO, a.PURCHASE_DATE, a.CASH_PAY_AMT, a.MCHT_CHI_NAME, a.MCHT_NO "
                + "FROM bil_bill a "
                + "left JOIN CRD_IDNO b "
                + "ON a.ID_P_SEQNO=b.ID_P_SEQNO "
                // 步驟3.bil_bill.p_seqno
                + "WHERE a.acct_type = '01'  " 					// --一般卡/帳戶帳號類別碼
                + "     and a.PURCHASE_DATE >= ? " 	//  --(消費日期)/營業日-年月
                + "     and a.PURCHASE_DATE <= ? "   //  --(消費日期)/營業日-年月
        		+ "     and SUBSTR(a.ACCT_DATE,1,6) = ? " ;		//  --(帳務日期)/營業日-年月
        	if ("1".equals(mchtCnameSel)) {						//1.指定; 2.排除
                //sqlCmd += "     AND MCHT_CHI_NAME LIKE '%' || ? || '%' " ;	// (步驟2.mkt_bn_cdata.data_code) --指定
                sqlCmd += "     AND a.MCHT_CHI_NAME LIKE  ? || '%' " ; 			// (步驟2.mkt_bn_cdata.data_code) --指定
        	} else {
                //sqlCmd += "     AND NOT MCHT_CHI_NAME LIKE '%' || ? || '%' " ;	// (步驟2.mkt_bn_cdata.data_code)   --排除
                sqlCmd += "     AND NOT a.MCHT_CHI_NAME LIKE  ? || '%' " ;			// (步驟2.mkt_bn_cdata.data_code)   --排除
        	}                
        	sqlCmd += "	AND a.P_SEQNO in ( "
                + "     SELECT P_SEQNO "
                + "     FROM bil_bill "
                + "     WHERE acct_type = '01'  " 				// --一般卡/帳戶帳號類別碼
                + "     and PURCHASE_DATE >= ? "	//  --(消費日期)/營業日-年月
                + "     and PURCHASE_DATE <= ? " //  --(消費日期)/營業日-年月
        		+ "     and SUBSTR(ACCT_DATE,1,6) = ? "; 		//  --(帳務日期)/營業日-年月
        	if ("1".equals(mchtCnameSel)) {
        		//sqlCmd += "     AND MCHT_CHI_NAME LIKE '%' || ? || '%' "; // (步驟2.mkt_bn_cdata.data_code) --指定
                sqlCmd += "     AND MCHT_CHI_NAME LIKE  ? || '%' "; 		// (步驟2.mkt_bn_cdata.data_code) --指定
        	} else {
                //sqlCmd += "     AND NOT MCHT_CHI_NAME LIKE '%' || ? || '%' "; // (步驟2.mkt_bn_cdata.data_code)   --排除
                sqlCmd += "     AND NOT MCHT_CHI_NAME LIKE  ? || '%' "; 		// (步驟2.mkt_bn_cdata.data_code)   --排除
        	}
        	sqlCmd += "     GROUP BY P_SEQNO "
                + "     HAVING sum(CASH_PAY_AMT) >= ? " 					// (步驟1.“累積最低消費金額” (mkt_channel_parm.sum_amt))
                + ") "
                + "ORDER BY b.ID_NO, a.CARD_NO, a.PURCHASE_DATE ";
        setString(1, purchaseDateStart);	//作為消費日條件
        setString(2, purchaseDateEnd);	//作為帳務日條件
        setString(3, month);  //作為帳務日條件
        setString(4, dataCode);
        setString(5, purchaseDateStart);	//作為消費日條件
        setString(6, purchaseDateEnd);	//作為消費日條件
        setString(7, month);    //作為帳務日條件
        setString(8, dataCode);
        setDouble(9, sumAmt);
        
        int cnt = selectTable();
        showLogMessage("I", "", "select selectBilBillData(), records = "+cnt);
        /*
        if (cnt == 0) {
            //showLogMessage("I", "", "select selectBilBillData() error !!! ["+ i + ": " + mchtCnameSel+"/"+ dataCode + "/" + sumAmt + "]");
            showLogMessage("I", "", "select selectBilBillData() error !!! ");
            return ;
        } else {
        */
        	for (int i = 0; i < cnt; i++) {
        		totCnt++;
        		String chiName = getValue("chi_name", i); // 正卡姓名
        		String idNo = getValue("ID_NO", i); // 正卡身分證
        		String cardNo = getValue("card_no", i); // 卡號
        		String purchaseDate = getValue("PURCHASE_DATE", i); // 交易日期
        		int cashPayAmt = getValueInt("CASH_PAY_AMT", i); // 交易金額
        		String strCashPayAmt = String.format("%09d", cashPayAmt);
        		String mchtChiName = getValue("MCHT_CHI_NAME", i); // 特店中文名
        		String mchtNo = getValue("MCHT_NO", i); // 特店代號
            
        		liontranStr.append(commCrd.fixLeft(chiName, 10)).append(",");
        		liontranStr.append(commCrd.fixLeft(idNo, 10)).append(",");
        		liontranStr.append(commCrd.fixLeft(cardNo, 16)).append(",");
        		liontranStr.append(commCrd.fixLeft(purchaseDate, 8)).append(",");
        		liontranStr.append(commCrd.fixLeft(strCashPayAmt, 9)).append(",");
        		liontranStr.append(commCrd.fixLeft(mchtChiName, 30)).append(",");	//特店中文名
        		liontranStr.append(commCrd.fixLeft(mchtNo, 15)).append("\n");
            
        		lionauxStr.append(commCrd.fixLeft(idNo, 10)).append(",");
        		lionauxStr.append(commCrd.fixLeft(chiName, 10)).append(",");
        		lionauxStr.append(commCrd.fixLeft(strCashPayAmt, 9)).append("\n");
        	}
        //}
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
    }
    //--傳入hBusinessDate
    private void writeReport(String date) throws Exception {
    	showLogMessage("I", "", "========================================================= ");
    	showLogMessage("I", "", "writeReport(), 參數 date=[" + date+"]");
        // 依據不同的產檔格式, 分別產出檔案及傳送至遠端
    	if (date.substring(6).compareTo("16") == 0) {		//LionTran_產檔時間16日
    		String hBusinessTwDate = commDate.toTwDate(hBusinessDate);
    	    String filename = "LionTran_" + hBusinessTwDate +".TXT";
    	    fileOpen(filename);
    	    showLogMessage("I", "", "開始寫入LionTran檔案(上個月消費資料): " + filename);
    	    showLogMessage("I", "", " ");
    	    writeTextFile(fptr1, liontranStr.toString());
    	    closeOutputText(fptr1);
    	    ftpProc(filename);
    	}
        if (date.substring(6).compareTo("25") >= 0) {		//LionAux_產檔時間25日以後每日
            String filename1 = "LionAux_" + hBusinessDate +".TXT";
            fileOpen(filename1);
            showLogMessage("I", "", "開始寫入LionAux檔案(25日至當日): " + filename1);
            showLogMessage("I", "", " ");
            writeTextFile(fptr1, lionauxStr.toString());
            closeOutputText(fptr1);
            ftpProc(filename1);
        }
    }

    /*******************************************************************/
    private void fileOpen(String filename) throws Exception {
        String tempStr1 = String.format("%s%s%s", commCrd.getECSHOME(), PATH_FOLDER, filename);
        String fileName = Normalizer.normalize(tempStr1, java.text.Normalizer.Form.NFKD);
        fptr1 = openOutputText(fileName, "MS950");

        if (fptr1 == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void ftpProc(String filename) throws Exception {
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        //commFTP.hEflgGroupId = "TOHOST"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = commCrd.getECSHOME() + PATH_FOLDER; // 相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

        String tmpChar = "put " + filename;

        int errCode = commFTP.ftplogName("NCR2EMP", tmpChar);

        if (errCode != 0) {
            showLogMessage("I", "", "檔案傳送 " + "NCR2EMP" + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "MktC955 執行完成 傳送EMP失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        // 刪除檔案 put 不用刪除
        renameFile(filename);
    }

    // ************************************************************************
    private void renameFile(String removeFileName) throws Exception {
        String tmpStr1 = commCrd.getECSHOME() + PATH_FOLDER + removeFileName;
        String tmpStr2 = commCrd.getECSHOME() + PATH_FOLDER + removeFileName + "." + sysDate + sysTime;

        if (!commCrd.fileRename2(tmpStr1, tmpStr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpStr2 + "]");
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC955 proc = new MktC955();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

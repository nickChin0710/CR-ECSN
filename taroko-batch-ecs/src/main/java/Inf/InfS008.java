/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  111/10/12  V1.00.00   JeffKung     program initial                        *
*  112/02/21  V1.00.02   JeffKung     依據討論結論下傳txn_code及規格                      *
*  112/09/20  V1.00.03   JeffKung     將檔案產回/media/                       *
*  112/10/20  V1.00.04   JeffKung     若消費日轉民國年為負數以"0000000"下傳         *
*****************************************************************************/
package Inf;



import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxInf;



public class InfS008 extends AccessDAO {
        private static final int OUTPUT_BUFF_SIZE = 5000;
        private final String progname = "產生送客服批次檔案-信用卡當日交易資料檔程式  112/10/20 V1.00.04";
        private static final String NEWCENTER_FOLDER = "/cr/ecs/media/crm/";
        private static final String DATA_FORM = "CCTHSTX";
        //private final String lineSeparator = "\r\n";
        private final String lineSeparator = System.lineSeparator();
        
        CommCrd commCrd = new CommCrd();
        CommFunction   comm  = new CommFunction();

        public int mainProcess(String[] args) {

                try {
                        CommCrd comc = new CommCrd();
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
                        String searchDate = (args.length == 0) ? "" : args[0].trim();
                        showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
                      //取前一日的日期(日曆日)
                        if ("".equals(searchDate)) {
                            searchDate = getProgDate(searchDate, "D");
                            searchDate = comm.nextNDate(searchDate, -1); 
                        }
                        showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

                        String fileNameSearchDate = searchDate;
                        
                        // get the name and the path of the .DAT file
                        String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
                        String fileFolder =  Paths.get(NEWCENTER_FOLDER).toString();
                        
                        // 產生主要檔案 .DAT 
                        int dataCount = generateDatFile(fileFolder, datFileName,searchDate);
                        
                        // 產生Header檔
                        CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
                        dateTime(); // update the system date and time
                        boolean isGenerated = commTxInf.generateTxtCSRHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), dataCount);
                        if (isGenerated == false) {
                                comc.errExit("產生HDR檔錯誤!", "");
                        }
                        
                        String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
                        
                        // run FTP
                        procFTP(fileFolder, datFileName, hdrFileName);

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
         * @param fileFolder 檔案的資料夾路徑
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
                        
                        //處理bil_bill
                        showLogMessage("I", "", "開始處理bil_bill檔......");
                        selectBilBillData(searchDate);
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
                        
                        
                        //處理dbb_bill
                        showLogMessage("I", "", "開始處理dbb_bill檔......");
                        selectDbbBillData(searchDate);
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
                        
                        //處理cyc_pyaj
                        showLogMessage("I", "", "開始處理cyc_pyaj檔......");
                        selectCycPyajData(searchDate);
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
                        }else {
                                showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
                        }
                        
                }finally {
                        closeBinaryOutput();
                }
                
                return rowCount;
        }

        /**
         * 產生檔案
         * @return String
         * @throws Exception 
         */
        private String getRowOfDAT() throws Exception {
        	
        		String dataFrom     = "";
        		String idNo         = "";
                String acctType     = "";
                String groupCode    = "";
                String txnCode      = "";
                String cardNo       = "";
                String purchaseDate = "";
                String postDate     = "";
                String sourceAmt    = "";
                String destAmt      = "";
                String cashPayAmt   = "";
                String dcDestAmt    = "";
                String mchtCategory = "";
                String currCode     = "";
                String sourceCurr   = "";
                String mchtEngName  = "";
                String mchtChiName  = "";
                String authCode     = "";
                String mchtNo       = "";
                String contractNo   = "";
                String pSeqno       = "";
                String paymentType  = "";
                
                dataFrom     = getValue("DATA_FROM_TABLE");
                
                if ("CYC_PYAJ".equals(dataFrom)) {
                	selectCrdCard(); //取得卡號及groupCode
                	idNo         = getValue("card.ID_NO");
                	acctType     = getValue("ACCT_TYPE");
                	groupCode    = getValue("card.GROUP_CODE");
                	txnCode      = getValue("TXN_CODE");
                	cardNo       = getValue("card.CARD_NO");
                	purchaseDate = getValue("PURCHASE_DATE");
                	
                	//若是換成民國年為負數,則以"0000000"下資料
                    if ((commCrd.str2int(purchaseDate.substring(0, 4)) - 1911)< 0) {
                    	purchaseDate = "0000000"; 
                    } else {
                    	purchaseDate = (commCrd.str2int(purchaseDate.substring(0, 4)) - 1911) + purchaseDate.substring(4);
                    }
                    
                	purchaseDate = (commCrd.str2int(purchaseDate.substring(0, 4)) - 1911) + purchaseDate.substring(4);
                	postDate     = getValue("POST_DATE");
                	postDate     = (commCrd.str2int(postDate.substring(0, 4)) - 1911) + postDate.substring(4);
                	
                	paymentType  = getValue("PAYMENT_TYPE");
                	if (getValueDouble("DC_DEST_AMT") < 0) {
                		if ("DR11".equals(paymentType)) {
                			txnCode = "21";
                		} else {
                			txnCode = "2A";
                		}
                		destAmt      = String.format("%014.2f",(getValueDouble("DEST_AMT")*-1));
                    	dcDestAmt    = String.format("%014.2f",(getValueDouble("DC_DEST_AMT")*-1));
                    	sourceAmt    = String.format("%014.2f",(getValueDouble("DC_DEST_AMT")*-1));
                	} else {
                		destAmt      = String.format("%014.2f",getValueDouble("DEST_AMT"));
                    	dcDestAmt    = String.format("%014.2f",getValueDouble("DC_DEST_AMT"));
                    	sourceAmt    = String.format("%014.2f",getValueDouble("DC_DEST_AMT"));
                	}
                	
                	currCode     = getValue("CURR_CODE");
                	sourceCurr   = getValue("CURR_CODE");
                	mchtChiName  = getValue("MCHT_CHI_NAME");
                	authCode     = String.format("%06d", commCrd.str2int(getValue("AUTH_CODE")))+"00000";
                	pSeqno       = getValue("P_SEQNO");
                	
                } else {
                	idNo         = getValue("ID_NO");
                	acctType     = getValue("ACCT_TYPE");
                    groupCode    = getValue("GROUP_CODE");
                    txnCode      = getValue("TXN_CODE");
                    cardNo       = getValue("CARD_NO");
                    purchaseDate = getValue("PURCHASE_DATE");
                    
                    //若是換成民國年為負數,則以"0000000"下資料
                    if ((commCrd.str2int(purchaseDate.substring(0, 4)) - 1911)< 0) {
                    	purchaseDate = "0000000"; 
                    } else {
                    	purchaseDate = (commCrd.str2int(purchaseDate.substring(0, 4)) - 1911) + purchaseDate.substring(4);
                    }
                    
                    postDate     = getValue("POST_DATE");
            		postDate     = (commCrd.str2int(postDate.substring(0, 4)) - 1911) + postDate.substring(4);
                    destAmt      = String.format("%014.2f",getValueDouble("DEST_AMT"));
                    cashPayAmt   = String.format("%014.2f",getValueDouble("CASH_PAY_AMT"));
                    dcDestAmt    = String.format("%014.2f",getValueDouble("DC_DEST_AMT"));
                    sourceAmt    = String.format("%014.2f",getValueDouble("SOURCE_AMT"));
                    mchtCategory = getValue("MCHT_CATEGORY");
                    currCode     = getValue("CURR_CODE");
                    sourceCurr   = getValue("SOURCE_CURR");
                    mchtEngName  = getValue("MCHT_ENG_NAME");
                    mchtChiName  = getValue("MCHT_CHI_NAME");
                    authCode     = String.format("%06d", commCrd.str2int(getValue("AUTH_CODE")))+"00000";
                    mchtNo       = getValue("MCHT_NO");
                    contractNo   = getValue("CONTRACT_NO");
                    pSeqno       = getValue("P_SEQNO");
                }

                String outOrg = "";
                if ("01".equals(acctType)) {
                	outOrg = "106";
                	if ("840".equals(currCode)) {
                		outOrg = "606";
                	}
                	if ("392".equals(currCode)) {
                		outOrg = "607";
                	}
                } else if ("90".equals(acctType)) {
                	outOrg = "206";
                } else {
                	outOrg = "306";
                }
                
                String outType = commCrd.getSubString(groupCode,1,4);
                
                String outTxnCode = "";
                if ("05".equals(txnCode)) {
                	outTxnCode = "40";
                } else if ("IN".equals(txnCode)) {
                	outTxnCode = "40";
                } else if ("06".equals(txnCode)) {
                	outTxnCode = "41";
                } else if ("07".equals(txnCode)) {
                	outTxnCode = "30";
                } else if ("25".equals(txnCode)) {
                	outTxnCode = "43";
                } else if ("26".equals(txnCode)) {
                	outTxnCode = "42";
                } else if ("27".equals(txnCode)) {
                	outTxnCode = "31";
                } else if ("20".equals(txnCode)) {
                	outTxnCode = "20";
                } else if ("21".equals(txnCode)) {
                	outTxnCode = "21";
                } else if ("2A".equals(txnCode)) { //退溢繳
                	outTxnCode = "27";
                } else {
                	showLogMessage("E", "", "TXN_CODE無法對應--"+txnCode);
                	outTxnCode = txnCode;
                }

                /*
                StringBuffer sb = new StringBuffer();
                sb.append(commCrd.fixLeft(idNo, 16));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(cardNo, 16));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(postDate, 7));
                sb.append(commCrd.fixLeft("\006", 1));
                if (mchtChiName.length() > 0) {
                	sb.append(commCrd.fixLeft(mchtChiName, 40));
                } else {
                	sb.append(commCrd.fixLeft(mchtEngName, 40));
                }
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(outTxnCode, 2));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(purchaseDate, 7));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(authCode, 11));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(dcDestAmt, 14)); //交易金額(放清算金額)
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(mchtCategory, 4));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(groupCode, 4));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(currCode, 3));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(postDate, 7));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(sourceCurr, 3));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(sourceAmt, 14));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(commCrd.fixLeft(pSeqno, 10));
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(lineSeparator);
                */

                StringBuffer sb = new StringBuffer();
                sb.append(idNo);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(cardNo);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(postDate);
                sb.append(commCrd.fixLeft("\006", 1));
                if (mchtChiName.length() > 0) {
                	sb.append(mchtChiName);
                } else {
                	sb.append(mchtEngName);
                }
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(outTxnCode);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(purchaseDate);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(authCode);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(dcDestAmt); //交易金額(放清算金額)
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(mchtCategory);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(groupCode);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(currCode);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(postDate);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(sourceCurr);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(sourceAmt);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(pSeqno);
                sb.append(commCrd.fixLeft("\006", 1));
                sb.append(lineSeparator);
                
                return sb.toString();
        }

        private void selectBilBillData(String searchDate) throws Exception {
                StringBuffer sb = new StringBuffer();
                sb.append(" SELECT  ");
                sb.append(" nvl((select id_no from crd_idno b where b.id_p_seqno = a.id_p_seqno),'') as id_no,");
                sb.append(" ACCT_TYPE,GROUP_CODE,TXN_CODE,CARD_NO,PURCHASE_DATE,POST_DATE,DEST_AMT,");
                sb.append(" MCHT_CATEGORY,CURR_CODE,SOURCE_CURR,SOURCE_AMT,MCHT_ENG_NAME,MCHT_CHI_NAME,AUTH_CODE,");     
                sb.append(" CASH_PAY_AMT,MCHT_NO,CONTRACT_NO,DC_DEST_AMT,P_SEQNO,'BIL_BILL' AS DATA_FROM_TABLE "); 
                sb.append("     FROM BIL_BILL a ");
                sb.append("     WHERE 1=1 ");
                sb.append("     AND a.RSK_TYPE NOT IN ('1','2','3') ");  //落問交的資料要踢除
                sb.append("     AND a.POST_DATE = ? ");
                sb.append("     AND a.ACCT_CODE IN ('BL','CA','IT') ");       //只下本金類
                sqlCmd = sb.toString();
                setString(1, searchDate);  //批次處理日期
                openCursor();
        }

        private void selectCycPyajData(String searchDate) throws Exception {
        	StringBuffer sb = new StringBuffer();
        	sb.append(" SELECT  ");
        	sb.append(" a.P_SEQNO, ");
        	sb.append(" a.ACCT_TYPE,'' GROUP_CODE,DECODE(a.CLASS_CODE,'P','20','B','20','2A') TXN_CODE,'' CARD_NO,a.PAYMENT_DATE AS PURCHASE_DATE,a.PAYMENT_DATE AS POST_DATE,a.PAYMENT_AMT AS DEST_AMT,");
        	sb.append(" '' MCHT_CATEGORY,a.CURR_CODE,'' SOURCE_CURR,'' MCHT_ENG_NAME,b.bill_desc as MCHT_CHI_NAME,'' AUTH_CODE, a.PAYMENT_TYPE, ");
        	sb.append(" 0.0 AS CASH_PAY_AMT,'' MCHT_NO,'' as CONTRACT_NO,a.DC_PAYMENT_AMT AS DC_DEST_AMT,'CYC_PYAJ' AS DATA_FROM_TABLE ");
        	sb.append("     FROM cyc_pyaj a, ptr_payment b ");
        	sb.append("     WHERE 1=1 ");
        	sb.append("     AND a.PAYMENT_TYPE = b.PAYMENT_TYPE ");
        	sb.append("     AND a.PAYMENT_DATE = ? ");
        	sb.append("     AND a.CLASS_CODE IN ('P') ");
        	sb.append("     AND a.PAYMENT_TYPE NOT IN ('REFU','DUMY') ");

        	sqlCmd = sb.toString();
        	setString(1, searchDate); // 批次處理日期
        	openCursor();
        }
        
        private void selectDbbBillData(String searchDate) throws Exception {
        	StringBuffer sb = new StringBuffer();
        	sb.append(" SELECT  ");
        	sb.append(" nvl((select id_no from dbc_idno c where c.id_p_seqno = b.id_p_seqno),'') as id_no,");
        	sb.append(" ACCT_TYPE,GROUP_CODE,TXN_CODE,CARD_NO,PURCHASE_DATE,POST_DATE,DEST_AMT,");
        	sb.append(" MCHT_CATEGORY,'901' as CURR_CODE,SOURCE_CURR,SOURCE_AMT,MCHT_ENG_NAME,MCHT_CHI_NAME,AUTH_CODE,");
        	sb.append(" CASH_PAY_AMT,MCHT_NO,'' as CONTRACT_NO,P_SEQNO,DEST_AMT AS DC_DEST_AMT,'DBB_BILL' AS DATA_FROM_TABLE ");
        	sb.append("     FROM DBB_BILL b ");
        	sb.append("     WHERE 1=1 ");
        	sb.append("     AND b.POST_DATE = ? ");
        	sb.append("     AND b.RSK_TYPE NOT IN ('1') ");
        	sb.append("     AND b.ACCT_CODE IN ('BL','CA') "); // 只下本金類

        	sqlCmd = sb.toString();
        	setString(1, searchDate); // 批次處理日期
        	openCursor();
        }
        
        private void selectCrdCard() throws Exception {
        	
        	extendField = "card.";
        	StringBuffer sb = new StringBuffer();
        	sb.append(" SELECT  ");
        	sb.append("     CARD_NO,GROUP_CODE, ");
        	sb.append("     (SELECT ID_NO FROM CRD_IDNO WHERE ID_P_SEQNO = CRD_CARD.ID_P_SEQNO) AS ID_NO ");
        	sb.append(" FROM CRD_CARD ");
        	sb.append("     WHERE 1=1 ");
        	sb.append("     AND ACCT_TYPE = ? ");
        	sb.append("     AND P_SEQNO = ? ");
        	sb.append("     AND SUP_FLAG = '0' ");
        	sb.append(" ORDER BY DECODE(CURRENT_CODE,'0','0','1'),OPPOST_DATE DESC ");
        	sb.append(" FETCH FIRST 1 ROWS ONLY "); 

        	sqlCmd = sb.toString();
        	setString(1,getValue("acct_type"));
        	setString(2,getValue("p_seqno"));
        	
        	int cardCnt = selectTable();
        	
        	if (cardCnt==0) {
            	setValue("card.card_no","");
            	setValue("card.group_code","");
            	setValue("card.id_no","");
            }

        }

        void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
                CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
                CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

                commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
                commFTP.hEflgSystemId = "NEWCENTER"; /* 區分不同類的 FTP 檔案-大類 (必要) */
                commFTP.hEriaLocalDir = fileFolder;
                commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
                commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
                commFTP.hEflgModPgm = javaProgram;

                // 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
                String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

                showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
                int errCode = commFTP.ftplogName("NEWCENTER", ftpCommand);

                if (errCode != 0) {
                        showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
                        commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
                        commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
                }
        }
        

        public static void main(String[] args) {
                InfS008 proc = new InfS008();
                int retCode = proc.mainProcess(args);
                System.exit(retCode);
        }

}



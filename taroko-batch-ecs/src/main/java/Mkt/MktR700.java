/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/13  V1.00.00   JeffKung     program initial                        *
*****************************************************************************/
package Mkt;



import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxInf;



public class MktR700 extends AccessDAO {
        private final String progname = "產生理專信用卡交易明細資料檔程式  112/04/13 V1.00.01";
        private static final String MKT_FOLDER = "media/mkt/";
        private static final String DATA_FORM = "CP_TRANS";
        private final String lineSeparator = "\r\n";

        CommCrd comc = new CommCrd();
        CommFunction comm = new CommFunction();
        CommCrdRoutine comcr = null;

        public int mainProcess(String[] args) {

                try {
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
                        searchDate = getProgDate(searchDate, "D");
                        showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
                        
                        //若不是月底日不執行
                        if (! searchDate.equals(comm.lastdateOfmonth(searchDate))) {
                        	showLogMessage("I", "", String.format("月底日執行此程式,今日[%s]非月底日不需執行!!", searchDate));
                        	return 0;
                        }

                        int procResult = fileOpen(searchDate);
                        if (procResult != 0) {
                        	showLogMessage("E", "", "====執行有誤====");
                        } else {
                        	showLogMessage("I", "", "====執行結束====");
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

        /***********************************************************************/
        int fileOpen(String searchDate) throws Exception {
            int totalCnt = 0;
            int rowCount = 0;
        	String str600 = "";
        	String fileNameSearchDate = searchDate.substring(2);
            String inputFileName = String.format("%s/media/mkt/WMS_RM_LIST.TXT", comc.getECSHOME());
            int br = openInputText(inputFileName, "MS950");
            if(br == -1) {
            	showLogMessage("I", "", String.format("本日無上傳檔需處理,[%s]", inputFileName));
            	return -1;
            }
            
            // get the name and the path of the .DAT file
            String datFileName = String.format("%s_%s.TXT", DATA_FORM, searchDate);
            String fileFolder =  Paths.get(comc.getECSHOME(), MKT_FOLDER).toString();
            String datFilePath = Paths.get(fileFolder, datFileName).toString();
            boolean isOpen = openBinaryOutput(datFilePath);
            if (isOpen == false) {
            	showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
            	return -1;
            }
            
            while (true) {
                str600 = readTextFile(br);
                if (endFile[br].equals("Y")) break;
                
                if (str600.length() < 10) continue; //ID傳入不滿10碼,跳過
                
                byte[] bytes = str600.getBytes("MS950");
                String custId = comc.subMS950String(bytes, 0, 10).trim();
                
                String idPSeqno = selectCrdIdno(custId);
                if ("".equals(idPSeqno)) continue;   //非持卡人

                totalCnt++;
                
                // 抓取消費資料產檔 
                
                sqlCmd = "select ";
                sqlCmd += "ACCT_TYPE,GROUP_CODE,TXN_CODE,CARD_NO,PURCHASE_DATE,THIS_CLOSE_DATE AS POST_DATE,DEST_AMT, ";
                sqlCmd += "DECODE(V_CARD_NO,'','','Y') AS HCE_IND, SIGN_FLAG, ";
                sqlCmd += "MCHT_CATEGORY,CURR_CODE,SOURCE_CURR,MCHT_ENG_NAME,MCHT_CHI_NAME,AUTH_CODE, ";
                sqlCmd += "CASH_PAY_AMT,MCHT_NO,CONTRACT_NO,DC_AMOUNT AS DC_DEST_AMT,'BIL_BILL' AS DATA_FROM_TABLE ";
                sqlCmd += "FROM BIL_CURPOST a ";
                sqlCmd += "WHERE 1=1 ";
                sqlCmd += "AND a.THIS_CLOSE_DATE LIKE ? ";
                sqlCmd += "AND a.BILL_TYPE = 'FISC' ";
                sqlCmd += "AND a.ID_P_SEQNO = ? ";
                sqlCmd += "AND a.TX_CONVT_FLAG <> 'R' ";
                sqlCmd += "AND a.ACCT_CODE IN ('BL','CA') ";

                setString(1, (comc.getSubString(searchDate, 0,6) + "%"));  //批次處理日期當月1-31日的交易
                setString(2, idPSeqno);
                openCursor();

                while (fetchTable()) {
                        
                        String rowOfDAT = getRowOfDAT(custId);
                        byte[] tmpBytes = rowOfDAT.getBytes("MS950");
                        writeBinFile(tmpBytes, tmpBytes.length);

                        rowCount++;

                }
                
                closeCursor();
                
                
                if ((totalCnt % 3000) == 0)
                    showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            }
            
            closeBinaryOutput();
            closeInputText(br);
            
            if (rowCount == 0) {
                showLogMessage("I", "", "無資料可寫入.TXT檔");
            }else {
                showLogMessage("I", "", String.format("產生.TXT檔完成！，共產生%d筆資料", rowCount));
            }
            
            // 產生Header檔
            String hFileName = String.format("%s_%s.H", DATA_FORM, searchDate);
            String hFilePath = Paths.get(fileFolder, hFileName).toString();
            dateTime(); // update the system date and time
            boolean isGenerated = produceHdrFile(hFilePath, datFileName, searchDate, sysDate, sysTime, rowCount);
            if (isGenerated == false) {
            	showLogMessage("E", "","產生HDR檔錯誤!");
            	return -1;
            }
            
            // run FTP
            procFTP(fileFolder, datFileName, hFileName);
            
            return 0;
        }


        private String getRowOfDAT(String custId) throws Exception {
        	
        		String dataFrom     = "";
                String acctType     = "";
                String signFlag     = "";
                String txnCode      = "";
                String cardNo       = "";
                String purchaseDate = "";
                String postDate     = "";
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
                String majorIdNo    = "";
                String hceInd       = "";
                
                dataFrom     = getValue("DATA_FROM_TABLE");
		
				acctType = getValue("ACCT_TYPE");
				signFlag = getValue("SIGN_FLAG");
				txnCode = getValue("TXN_CODE");
				cardNo = getValue("CARD_NO");
				purchaseDate = getValue("PURCHASE_DATE");
				postDate = getValue("POST_DATE");
				destAmt = String.format("%014d", (int) (getValueDouble("DEST_AMT") * 100));
				cashPayAmt = String.format("%014d", (int) (getValueDouble("CASH_PAY_AMT") * 100));
				dcDestAmt = String.format("%014d", (int) (getValueDouble("DC_DEST_AMT") * 100));
				mchtCategory = getValue("MCHT_CATEGORY");
				currCode = getValue("CURR_CODE");
				sourceCurr = getValue("SOURCE_CURR");
				mchtEngName = getValue("MCHT_ENG_NAME");
				mchtChiName = getValue("MCHT_CHI_NAME");
				authCode = getValue("AUTH_CODE");
				mchtNo = getValue("MCHT_NO");
				contractNo = getValue("CONTRACT_NO");
				majorIdNo = getValue("MAJOR_IDNO");
				hceInd = getValue("HCE_IND");

                StringBuffer sb = new StringBuffer();
                sb.append(comc.fixLeft(custId, 11));
                sb.append(comc.fixLeft("@#", 2));
                sb.append(comc.fixLeft(purchaseDate, 8));
                sb.append(comc.fixLeft("@#", 2));
                if (mchtChiName.length() > 0) {
                	sb.append(comc.fixLeft(mchtChiName, 70));
                } else {
                	sb.append(comc.fixLeft(mchtEngName, 70));
                }
                sb.append(comc.fixLeft("@#", 2));
                sb.append(comc.fixLeft(mchtCategory, 4));
                sb.append(comc.fixLeft("@#", 2));
                sb.append(comc.fixLeft(destAmt, 14)); //交易金額(折台)
                sb.append(comc.fixLeft("@#", 2));
                
                if ("-".equals(signFlag)) {
                	sb.append(comc.fixLeft(signFlag, 1));
                } else {
                	sb.append(comc.fixLeft(" ", 1));
                }
                //sb.append(comc.fixLeft("@#", 2));  //最後一個欄位,不用再加分隔符號
                
                sb.append(lineSeparator);

                return sb.toString();
        }
        
        private String selectCrdIdno(String custId) throws Exception {
        	String idPSeqno = "";
            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT  ID_P_SEQNO ");
            sb.append(" FROM CRD_IDNO a ");
            sb.append(" WHERE 1=1 ");
            sb.append(" AND ID_NO = ? ");  
            sqlCmd = sb.toString();
            setString(1, custId);  
            int recordCnt = selectTable();
            if (recordCnt > 0) {
            	idPSeqno = getValue("id_p_seqno");
            } 
            
            return idPSeqno;
            
        }

        private void selectCrdCard() throws Exception {
        	
        	extendField = "card.";
        	StringBuffer sb = new StringBuffer();
        	sb.append(" SELECT  ");
        	sb.append("     CARD_NO,GROUP_CODE ");
        	sb.append(" FROM CRD_CARD ");
        	sb.append("     WHERE 1=1 ");
        	sb.append("     AND ACCT_TYPE = ? ");
        	sb.append("     AND P_SEQNO = ? ");
        	sb.append(" ORDER BY DECODE(CURRENT_CODE,'0','0','1'),OPPOST_DATE DESC ");
        	sb.append(" FETCH FIRST 1 ROWS ONLY "); 

        	sqlCmd = sb.toString();
        	setString(1,getValue("acct_type"));
        	setString(2,getValue("p_seqno"));
        	
        	int cardCnt = selectTable();
        	
        	if (cardCnt==0) {
            	setValue("card.card_no","");
            	setValue("card.group_code","");
            }

        }
        
    	private boolean produceHdrFile(String filePath, String fileName, String dataDate, String createDate, String createTime, int dataCount)
    			throws Exception, UnsupportedEncodingException {
    		
    		boolean isOpen = openBinaryOutput(filePath);
    		if (isOpen == false) {
    			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", filePath));
    			return false;
    		}
    		
    		try {
    			StringBuffer sb = new StringBuffer();
                sb.append(comc.fixLeft(dataDate, 8));
                sb.append(comc.fixLeft(",", 1));
                sb.append(comc.fixLeft(dataDate, 8));
                sb.append(comc.fixLeft(",", 1));
                sb.append(comc.fixLeft(fileName, 50));
                sb.append(comc.fixLeft(",", 1));
                sb.append(comc.fixLeft(createDate+createTime, 14));
                sb.append(comc.fixLeft(",", 1));
                sb.append(comc.fixLeft(String.format("%09d", dataCount), 9));
                //sb.append(comc.fixLeft(",", 1));  //最後一個欄位,不用再加分隔符號
                sb.append(lineSeparator);
                
                byte[] tmpBytes = sb.toString().getBytes("MS950");
                writeBinFile(tmpBytes, tmpBytes.length);
		
    		}finally {
    			closeBinaryOutput();
    		}

    		return true;
    	}

        void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
                CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
                CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

                commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
                commFTP.hEflgSystemId = "WMS"; /* 區分不同類的 FTP 檔案-大類 (必要) */
                commFTP.hEriaLocalDir = fileFolder;
                commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
                commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
                commFTP.hEflgModPgm = javaProgram;

                String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

                showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
                int errCode = commFTP.ftplogName("CRDATACREA", ftpCommand);

                if (errCode != 0) {
                        showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
                        commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
                        commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
                }
        }
        
        public static void main(String[] args) {
                MktR700 proc = new MktR700();
                int retCode = proc.mainProcess(args);
                System.exit(retCode);
        }

}



/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/13  V1.00.00   JeffKung     program initial                        *
*****************************************************************************/
package Inf;



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



public class InfR015 extends AccessDAO {
        private static final int OUTPUT_BUFF_SIZE = 5000;
        private final String progname = "產生送CRM-開啟行動記帳本客戶信用卡當日交易資料檔程式  112/04/13 V1.00.01";
        private static final String CRM_FOLDER = "media/crm/";
        private static final String DATA_FORM = "DAILY_TXN";
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
                        String searchNextDate = "";
                        String searchDate = (args.length == 0) ? "" : args[0].trim();
                        showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
                        
                        //取後一日的日期(日曆日)
                        if ("".equals(searchDate)) {
                            searchDate = getProgDate(searchDate, "D");
                        }
                        searchNextDate = comm.nextNDate(searchDate, 1); 
                        showLogMessage("I", "", String.format("執行(資料)日期[%s]", searchDate));
                        showLogMessage("I", "", String.format("收送檔案日期[%s]", searchNextDate));

                        
                        int procResult = fileOpen(searchDate,searchNextDate);
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
        int fileOpen(String searchDate,String searchNextDate) throws Exception {
            int totalCnt = 0;
            int rowCount = 0;
        	String str600 = "";
        	String fileNameSearchDate = searchNextDate.substring(2);
            String inputFileName = String.format("%s/media/mkt/CustomerID_%s.DAT", comc.getECSHOME(), fileNameSearchDate);
            int br = openInputText(inputFileName, "MS950");
            if(br == -1) {
            	showLogMessage("I", "", String.format("本日無上傳檔需處理,[%s]", inputFileName));
            	return -1;
            }
            
            // get the name and the path of the .DAT file
            String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
            String fileFolder =  Paths.get(comc.getECSHOME(), CRM_FOLDER).toString();
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
                sqlCmd += "ACCT_TYPE,GROUP_CODE,TXN_CODE,CARD_NO,PURCHASE_DATE,POST_DATE,DEST_AMT, ";
                sqlCmd += "(SELECT ID_NO FROM CRD_IDNO B WHERE MAJOR_ID_P_SEQNO=B.ID_P_SEQNO) AS MAJOR_IDNO, ";
                sqlCmd += "DECODE(V_CARD_NO,'','','Y') AS HCE_IND, ";
                sqlCmd += "MCHT_CATEGORY,CURR_CODE,SOURCE_CURR,MCHT_ENG_NAME,MCHT_CHI_NAME,AUTH_CODE, ";
                sqlCmd += "CASH_PAY_AMT,MCHT_NO,CONTRACT_NO,DC_DEST_AMT,'BIL_BILL' AS DATA_FROM_TABLE ";
                sqlCmd += "FROM BIL_BILL a ";
                sqlCmd += "WHERE 1=1 ";
                sqlCmd += "AND a.POST_DATE = ? ";
                sqlCmd += "AND a.ID_P_SEQNO = ? ";
                sqlCmd += "AND a.RSK_TYPE NOT IN ('1','2','3') ";
                sqlCmd += "AND a.ACCT_CODE IN ('BL','CA','IT') ";
                sqlCmd += " UNION ALL ";
                sqlCmd += "select ";
                sqlCmd += "ACCT_TYPE,GROUP_CODE,TXN_CODE,CARD_NO,PURCHASE_DATE,POST_DATE,DEST_AMT, ";
                sqlCmd += "(SELECT ID_NO FROM DBC_IDNO B1 WHERE c.ID_P_SEQNO=B1.ID_P_SEQNO) AS MAJOR_IDNO, ";
                sqlCmd += "'' AS HCE_IND, ";
                sqlCmd += "MCHT_CATEGORY,'901' AS curr_code,SOURCE_CURR,MCHT_ENG_NAME,MCHT_CHI_NAME,AUTH_CODE, ";
                sqlCmd += "CASH_PAY_AMT,MCHT_NO,'' AS CONTRACT_NO,DEST_AMT AS DC_DEST_AMT,'DBB_BILL' AS DATA_FROM_TABLE ";
                sqlCmd += "FROM DBB_BILL c ";
                sqlCmd += "WHERE 1=1 ";
                sqlCmd += "AND c.POST_DATE = ? ";
                sqlCmd += "AND c.ID_P_SEQNO = ? ";
                sqlCmd += "AND c.RSK_TYPE <> '1' ";
                sqlCmd += "AND c.ACCT_CODE IN ('BL','CA') ";
                sqlCmd += " UNION ALL ";
                sqlCmd += "select ";
                sqlCmd += "d.ACCT_TYPE,'' GROUP_CODE,DECODE(d.CLASS_CODE,'P','20','B','20','2A') TXN_CODE,'' CARD_NO,d.PAYMENT_DATE AS PURCHASE_DATE,d.PAYMENT_DATE AS POST_DATE,d.PAYMENT_AMT AS DEST_AMT, ";
                sqlCmd += "(SELECT substr(acct_key,1,10) FROM ACT_ACNO D3 WHERE D3.ACCT_TYPE = '01' AND D3.P_SEQNO=d.P_SEQNO) AS MAJOR_IDNO, ";
                sqlCmd += "'' AS HCE_IND, '' MCHT_CATEGORY,d.CURR_CODE,'' SOURCE_CURR,'' MCHT_ENG_NAME,e.bill_desc as MCHT_CHI_NAME,'' AUTH_CODE, ";
                sqlCmd += "0.0 AS CASH_PAY_AMT,'' MCHT_NO,'' as CONTRACT_NO,d.DC_PAYMENT_AMT AS DC_DEST_AMT,'CYC_PYAJ' AS DATA_FROM_TABLE ";
                sqlCmd += "FROM cyc_pyaj d, ptr_payment e ";
                sqlCmd += "WHERE 1=1 ";
                sqlCmd += "AND d.P_SEQNO IN (select p_seqno from crd_card where id_p_seqno = ? and acct_type='01') ";
                sqlCmd += "AND d.PAYMENT_TYPE = e.PAYMENT_TYPE ";
                sqlCmd += "AND d.PAYMENT_DATE = ? ";
                sqlCmd += "AND d.CLASS_CODE = 'P' ";
                sqlCmd += "AND d.PAYMENT_TYPE NOT IN ('REFU','DUMY') ";

                setString(1, searchDate);  //批次處理日期
                setString(2, idPSeqno);
                setString(3, searchDate);  //批次處理日期
                setString(4, idPSeqno);
                setString(5, idPSeqno);
                setString(6, searchDate);  //批次處理日期
                openCursor();

                while (fetchTable()) {
                        
                        String rowOfDAT = getRowOfDAT(idPSeqno);
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
                showLogMessage("I", "", "無資料可寫入.DAT檔");
            }else {
                showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
            }
            
            // 產生Header檔
            CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
            dateTime(); // update the system date and time
            boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchNextDate, sysDate, sysTime.substring(0,4), rowCount);
            if (isGenerated == false) {
            	showLogMessage("E", "","產生HDR檔錯誤!");
            	return -1;
            }
            
            String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
            
            // run FTP
            procFTP(fileFolder, datFileName, hdrFileName);
            
            return 0;
        }


        private String getRowOfDAT(String idPSeqno) throws Exception {
        	
        		String dataFrom     = "";
                String acctType     = "";
                String groupCode    = "";
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
                
                if ("CYC_PYAJ".equals(dataFrom)) {
                	selectCrdCard(idPSeqno); //取得卡號及groupCode
                	acctType     = getValue("ACCT_TYPE");
                	groupCode    = getValue("card.GROUP_CODE");
                	txnCode      = getValue("TXN_CODE");
                	cardNo       = getValue("card.CARD_NO");
                	purchaseDate = getValue("PURCHASE_DATE");
                	postDate     = getValue("POST_DATE");
                	
                	if (getValueDouble("DC_DEST_AMT") < 0) {
                		txnCode = "2A";
                		dcDestAmt    = String.format("%09d",(int) (getValueDouble("DC_DEST_AMT")*-100));
                    	destAmt    = String.format("%09d",(int) (getValueDouble("DEST_AMT")*-100));
                	} else {
                		dcDestAmt    = String.format("%09d",(int) (getValueDouble("DC_DEST_AMT")*100));
                    	destAmt    = String.format("%09d",(int) (getValueDouble("DEST_AMT")*100));
                	}
                	
                	currCode     = getValue("CURR_CODE");
                	mchtChiName  = getValue("MCHT_CHI_NAME");
                	majorIdNo    = getValue("MAJOR_IDNO");
                    hceInd       = getValue("HCE_IND");
                	
                } else {
                	acctType     = getValue("ACCT_TYPE");
                    groupCode    = getValue("GROUP_CODE");
                    txnCode      = getValue("TXN_CODE");
                    cardNo       = getValue("CARD_NO");
                    purchaseDate = getValue("PURCHASE_DATE");
                    postDate     = getValue("POST_DATE");
                    destAmt      = String.format("%09d",(int) (getValueDouble("DEST_AMT")*100));
                    cashPayAmt   = String.format("%09d",(int) (getValueDouble("CASH_PAY_AMT")*100));
                    dcDestAmt    = String.format("%09d",(int) (getValueDouble("DC_DEST_AMT")*100));
                    mchtCategory = getValue("MCHT_CATEGORY");
                    currCode     = getValue("CURR_CODE");
                    sourceCurr   = getValue("SOURCE_CURR");
                    mchtEngName  = getValue("MCHT_ENG_NAME");
                    mchtChiName  = getValue("MCHT_CHI_NAME");
                    authCode     = getValue("AUTH_CODE");
                    mchtNo       = getValue("MCHT_NO");
                    contractNo   = getValue("CONTRACT_NO");
                    majorIdNo    = getValue("MAJOR_IDNO");
                    hceInd       = getValue("HCE_IND");
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
                
                String outType = comc.getSubString(groupCode,1,4);
                
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
                } else if ("2A".equals(txnCode)) { //退溢繳
                	outTxnCode = "27";
                } else {
                	showLogMessage("E", "", "TXN_CODE無法對應--"+txnCode);
                	outTxnCode = txnCode;
                }

                StringBuffer sb = new StringBuffer();
                sb.append(comc.fixLeft(outOrg, 3));
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(outType, 3));
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(cardNo, 16));
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(purchaseDate, 8));
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(postDate, 8));
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(outTxnCode, 2));
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(destAmt, 9)); //交易金額(放台幣金額)
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(mchtCategory, 4));
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(currCode, 3));
                sb.append(comc.fixLeft("!", 1));
                if (mchtChiName.length() > 0) {
                	sb.append(comc.fixLeft(mchtChiName, 40));
                } else {
                	sb.append(comc.fixLeft(mchtEngName, 40));
                }
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(authCode, 6));
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(mchtNo, 15));
                sb.append(comc.fixLeft("!", 1));
                //分期註記
                if (contractNo.length() > 0) {
                	sb.append(comc.fixLeft("2", 1));
                } else {
                	sb.append(comc.fixLeft(" ", 1));
                }
                sb.append(comc.fixLeft("!", 1));
                //HCE註記
                sb.append(comc.fixLeft(hceInd, 1));
                sb.append(comc.fixLeft("!", 1));
                sb.append(comc.fixLeft(majorIdNo, 10));
                sb.append(comc.fixLeft("!", 1));
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

        private void selectCrdCard(String idPSeqno) throws Exception {
        	
        	extendField = "card.";
        	StringBuffer sb = new StringBuffer();
        	sb.append(" SELECT  ");
        	sb.append("     CARD_NO,GROUP_CODE ");
        	sb.append(" FROM CRD_CARD ");
        	sb.append("     WHERE 1=1 ");
        	sb.append("     AND ACCT_TYPE = ? ");
        	sb.append("     AND id_p_seqno = ? ");
        	sb.append(" ORDER BY DECODE(CURRENT_CODE,'0','0','1'),OPPOST_DATE DESC ");
        	sb.append(" FETCH FIRST 1 ROWS ONLY "); 

        	sqlCmd = sb.toString();
        	setString(1,getValue("acct_type"));
        	setString(2,idPSeqno);
        	
        	int cardCnt = selectTable();
        	
        	if (cardCnt==0) {
            	setValue("card.card_no","");
            	setValue("card.group_code","");
            }

        }

        void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
                CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
                CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

                commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
                commFTP.hEflgSystemId = "MOBILE"; /* 區分不同類的 FTP 檔案-大類 (必要) */
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
                InfR015 proc = new InfR015();
                int retCode = proc.mainProcess(args);
                System.exit(retCode);
        }

}



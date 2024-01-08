/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/16  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.03    JustinWu     change to use the file path from the database and double->BigDecimal
*  109/07/23  V1.00.02    shiyuqi      coding standard, rename field method & format                   * 
*  109-09-04  V1.00.01  yanghan        解决Portability Flaw: Locale Dependent Comparison问题    * 
*  109/09/04  V1.00.06    Zuwei        code scan issue    
*  109/09/14  V1.00.06    Zuwei        code scan issue    
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard     *
*  111/02/14  V1.00.08    Ryan         big5 to MS950                                           *
*  111/02/18  V1.00.09    JeffKung     識別碼錯誤, 不處理, 不abend                                *
*  111/03/09  V1.00.10    JeffK        trialer比對不一致時不abend, 照常處理           *
*  111/09/22  V1.00.11    JeffKung     seqno以fileDate取最大值,substring改成共用func *
*****************************************************************************/
package Bil;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Locale;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;

public class BilE142 extends AccessDAO {
    private  final String progname = "FISC- ICFnnQBD二代帳務交易查核處理 111/09/22  V1.00.11";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;

	 int mainProcess(String[] args) {
		int inputFileD = -1;
	    String fileNameD="", filePathFromDB="",  fileDate="", bankNo="";
	    String filePath = "";
	    BigDecimal recordAmount, trailerAmount; // 交易總來源金額
		Integer recordCnt, trailerRecordCnt;   // record總筆數
		Integer recordTransCnt, trailerTransCnt;   // 交易總筆數
		
	    try {
	    	recordAmount = BigDecimal.ZERO;
	    	trailerAmount = null;
	    	recordCnt = 0;
	    	trailerRecordCnt = null;
	    	recordTransCnt = 0;
	    	trailerTransCnt = null;
	    	String text = "";
	    	byte[] bytesArr = null;
	        // ====================================
	        // 固定要做的
	        dateTime();
	        setConsoleMode("Y");
	        javaProgram = this.getClass().getName();
	        showLogMessage("I", "", javaProgram + " " + progname);
	        // =====================================
	        
	        // 固定要做的
	        if (!connectDataBase()) {
	            comc.errExit("connect DataBase error", "");
	        }
	        comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
	        commTxBill = new CommTxBill(getDBconnect(), getDBalias(), false);
//	        comcr.h_call_batch_seqno = args.length > 0 ? args[args.length - 1] : "";
	        
	        
			// fileDate:傳檔日期FILE_DATE
			// filePathFromDB: ECS_FTP_LOG.local_dir 
			// fileNameD:通知檔案名稱FILE_NAME
			// comcr.h_call_batch_seqno
	        fileDate = args[0];
	        filePathFromDB = args[1];
			fileNameD = args[2];
			comcr.hCallBatchSeqno = args[3];
	        
			// select the bank number
			bankNo = commTxBill.getFiscBankNoFromPtrSysParm();
			
			// check whether file name's 2nd to 10th characters equals to the bank number
			if( ! comc.getSubString(fileNameD,1, 9).equalsIgnoreCase(bankNo) ) {
				showLogMessage("E", "", String.format("%s並非F00600000字頭!!", fileNameD));
				return -1;
			}

			//open  F00600000.ICFnnQBD.YYYYMMDDNN file ==================
			filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
			fileNameD = SecurityUtil.verifyPath(fileNameD);
			filePath = Paths.get(filePathFromDB, fileNameD ).toString();
			inputFileD  = openInputText(filePath, "MS950");
			if(inputFileD == -1) {
				showLogMessage("E", "", String.format("檔案不存在: %s", fileNameD));
	            return -1;
	        }	
			
			// check whether processing repetitively
			if(commTxBill.checkRepeatProcess(fileNameD)) {
				return -2;
			}
			   
			while(true) {
				text = readTextFile(inputFileD);
				bytesArr = text.getBytes("MS950");
				if(text.trim().length() != 0 ) {
					switch(CommTxBill.subByteToStr(bytesArr, 0, 1).toUpperCase(Locale.TAIWAN)) {			
					case "H":
						if( ! checkHeader(bytesArr, fileNameD, bankNo))
							return -1;
						break;
					case "T":
						// TRAILER
						// if the character converted from the first byte equals to T, this row is a trailer
						if(CommTxBill.subByteToStr(bytesArr, 0, 1).equalsIgnoreCase("T")) {
							trailerTransCnt = Integer.parseInt( CommTxBill.subByteToStr(bytesArr, 1, 9) );
							trailerRecordCnt = Integer.parseInt( CommTxBill.subByteToStr(bytesArr, 9, 17) );
							// 31,32byte為小數
							trailerAmount =BigDecimal.valueOf(Double.parseDouble(CommTxBill.subByteToStr(bytesArr, 17, 32)));
						}
						break;
					default: 
						// =================default begin=======================================
						switch(CommTxBill.subByteToStr(bytesArr, 4, 5)) {
						case "1":
							// RECORD1: 調單交易、費用交易、一般交易
							switch(CommTxBill.subByteToStr(bytesArr, 2, 4)) {
							case "51":
							case "52":
								// 調單交易:即財金交易代號的後二位為51、52
								recordAmount = recordAmount.add(new BigDecimal(CommTxBill.subByteToStr(bytesArr, 59, 71)));
								break;
							case "10":
							case "20":
								// 費用交易:即財金交易代號的後二位為10、20
								recordAmount = recordAmount.add(new BigDecimal(CommTxBill.subByteToStr(bytesArr, 53, 65)));
								break;
							default:
								// 一般交易:即財金交易代號的後二位不為51、52、10、20
								recordAmount = recordAmount.add(new BigDecimal(CommTxBill.subByteToStr(bytesArr, 57, 69)));
								break;
							}
							recordCnt ++;
							recordTransCnt++;
							break;
						case "2":
						case "3":
						case "4":
							// RECORD2 ,3 , or 4
							recordCnt ++;
							break;
						default:
							showLogMessage("E", "", "RECORD 識別碼錯誤 : " + CommTxBill.subByteToStr(bytesArr, 4, 5)) ;
							recordCnt ++;
							//return -1;  識別碼錯誤不處理, 僅顯示不abend
							break;
						}
						break;
						// =================default begin=======================================
					}
				
				}
				
				if (endFile[inputFileD].equals("Y")) 
	            	break;

			}		
			
			// ===================================================
			commTxBill.printDebugString("recordAmount: " + recordAmount );
			commTxBill.printDebugString("recordCnt: " + recordCnt);
			commTxBill.printDebugString("recordTransCnt: " + recordTransCnt);
			commTxBill.printDebugString("trailerAmount: " + trailerAmount );
			commTxBill.printDebugString("trailerRecordCnt: " + trailerRecordCnt);
			commTxBill.printDebugString("trailerTransCnt: " + trailerTransCnt);
			// ===================================================
			
			// check whether the amount and the counts of the data in the D and A are the same.
			if( (recordCnt != null && trailerRecordCnt != null && recordAmount != null 
					&& trailerAmount != null && recordTransCnt != null && trailerTransCnt != null) 
					&& (recordCnt.intValue() != (trailerRecordCnt.intValue())  || recordAmount.compareTo(trailerAmount) != 0 
					|| recordTransCnt.intValue() != (trailerTransCnt.intValue())) ) {
				showLogMessage("E", "", "recordAmount: " + recordAmount);
				showLogMessage("E", "", "recordCnt: " + recordCnt);
				showLogMessage("E", "", "recordTransCnt: " + recordTransCnt);
				showLogMessage("E", "", "trailerAmount: " + trailerAmount);
				showLogMessage("E", "", "trailerRecordCnt: " + trailerRecordCnt);
				showLogMessage("E", "", "trailerTransCnt: " + trailerTransCnt);
				showLogMessage("E", "", "資料不符");
				//return -1;
			}
			commTxBill.printDebugString("資料符合");
			
			// insert into BIL_FISCCTL
			insertBilFiscctl( fileNameD, fileDate, recordAmount.doubleValue(), recordTransCnt);
	        return 0;    
	    }
	    catch (Exception e) {
			expMethod = "mainProcess";  
	        expHandle(e); 
	        return  exceptExit; 
		}finally {
			finalProcess();
		}
		  
	}

	 /**
	  * check the giving header 
	  * @param bytesArr
	  * @param fileNameD
	  * @param bankNo
	  * @return
	  */
	 private boolean checkHeader(byte[] bytesArr, String fileNameD, String bankNo) {
		// check whether the record indicator equals to 'H'
			if(! CommTxBill.subByteToStr(bytesArr, 0, 1).equalsIgnoreCase("H") ) {
				showLogMessage("E", "", "HEADER的RECORD 識別碼錯誤");
				return false;
			}
			
			// check whether the characters from 10th to 17th equals to the bank number
			if( ! CommTxBill.subByteToStr(bytesArr, 9, 17).equalsIgnoreCase(bankNo)  ) {
				showLogMessage("E", "", "資料接收單位錯誤");
				return false;
			}
			
			// check 系統日期是否大於檔案產出日期
			if( ! commTxBill.compareStr(sysDate,  CommTxBill.subByteToStr(bytesArr, 21, 29) ) ) {
				showLogMessage("E", "", "檔案產出日期不能大於系統日期");
				return false;
			}
			
			// Example: file name = F00600000.ICFnnQBD.YYMMDDNN
			
			// 檔案產出類型是否與fileName的中間名稱一致
			if(! CommTxBill.subByteToStr(bytesArr, 29, 37).equalsIgnoreCase( comc.getSubString(fileNameD,10, 18) ) ) {
				showLogMessage("E", "", "檔案產出類型與fileName的中間名稱不一致");
				return false;
			}
			
			// 檔案產出序號是否與fileName的最後二碼一致
			if(! CommTxBill.subByteToStr(bytesArr, 37, 39).equalsIgnoreCase( comc.getSubString(fileNameD,25, 27) ) ) {
				showLogMessage("E", "", "檔案產出序號與fileName的最後二碼不一致");
				return false;
			}
		return true;
	}

	/**
	  * This method is to insert data into the table BIL_FISCCTL, 
	  * but before inserting, it will first delete the data in BIL_FISCCTL 
	  * for the exceptions happening before the data inserted into BIL_CURPOST
	  * @param fileNameD
	  * @param fileDate
	  * @param recordAmount
	  * @param recordTransCnt
	  * @return
	  * @throws Exception
	  */
	 private int insertBilFiscctl( String fileNameD, String fileDate, Double recordAmount, Integer recordTransCnt) throws Exception {
		 String businessDate = getBusiDate();
		 String fctlNo = commTxBill.getFctlNoFromBilFiscctl(fileNameD);

		if (!commTxBill.isEmpty(fctlNo)) {
			// delete data
			commTxBill.deleteBilFiscctl(fctlNo);
			commTxBill.deleteBilFiscdtl(fctlNo);
		}
		
		int newSeqNo = commTxBill.getMaxSeqNo(fileDate) + 1;
		daoTable = "bil_fiscctl";
		setValue("fctl_no",  fileDate + "FISC" + String.format("%04d", newSeqNo ));
		setValueInt("fctl_seq", newSeqNo);
		setValue("fctl_date",  businessDate);
		setValue("fctl_type",  "FISC");
		setValue("file_date", fileDate);
		setValue("media_name_a", "");
		setValue("media_name", fileNameD);
		setValueDouble("total_amt", recordAmount);
		setValueInt("total_cnt", recordTransCnt);
		setValue("proc_code", "");
		setValue("prog_name", javaProgram);
		setValue("mod_user", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert bil_fiscctl duplicate", "","fctl_no : ["+getValue("fctl_no") + "]" );
        }
		commTxBill.printDebugString("新增bil_fiscctl成功!!!!!!!!!!!!!!!!!!");
		return 1;
	}

	public static void main(String[] args) {
		BilE142 proc = new BilE142();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}



}

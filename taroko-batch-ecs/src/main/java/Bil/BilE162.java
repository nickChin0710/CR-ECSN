/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/30  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.03    JustinWu     change to use the file path from the database and double->BigDecimal
*  109-07-03  V1.00.04   shiyuqi       updated for project coding standard     
*  109/07/23  V1.00.05    shiyuqi      coding standard, rename field method & format                   * 
*  109/09/04  V1.00.06    Zuwei        code scan issue    
*  109/09/14  V1.00.06    Zuwei        code scan issue    
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard     *
*  110-03-03  V1.00.08    JustinWu     comment the display of the money
*  111/02/14  V1.00.09    Ryan         big5 to MS950                                           *
*  111/02/18  V1.00.10    JeffKung     不處理A檔                                                               *
*  111/09/22  V1.00.11    JeffKung     seqno以fileDate取最大值,substring改成共用func *
*****************************************************************************/
package Bil;

import java.math.BigDecimal;
import java.nio.file.Paths;


import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;


public class BilE162 extends AccessDAO {
	private final  String progname = "FISC-ICACQQN採購卡請款查核處理 111/09/22 V1.00.11";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;

	 int mainProcess(String[] args) {
		int inputFileA = -1, inputFileD = -1;
	    String fileNameD="", filePathFromDB="",  fileDate="", bankNo="";
	    String fileNameA="";
	    String filePath = "";
	    BigDecimal amountD, amountA;
		Integer cntD, cntA;
		
	    try {
	    	amountD = BigDecimal.ZERO;
	    	amountA = null;
	    	cntD = 0;
	    	cntA = null;
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
//	        comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
	        
			// fileDate:傳檔日期FILE_DATE
			// filePathFromDB: ECS_FTP_LOG.local_dir
			// fileNameD:通知檔案名稱FILE_NAME
			// comcr.hCallBatchSeqno
	        fileDate = args[0];
	        filePathFromDB = args[1];
			fileNameD = args[2];
			comcr.hCallBatchSeqno = args[3];
			
			// F00600000.XXXXXXXD.XXXXXXXX -> F00600000.XXXXXXXA.XXXXXXXX
			fileNameA = comc.getSubString(fileNameD,0, 17) + "A" + comc.getSubString(fileNameD,18);           
	        
			// select the bank number
			bankNo = commTxBill.getFiscBankNoFromPtrSysParm();
			
					
			
			//===========================================
			
			// check whether processing repetitively
			if(commTxBill.checkRepeatProcess(fileNameD)) {
				return -1;
			}
			
			int row = 1; 
			int readCnt = 0;

/*			
			//=============================================
			//open  F00600000.XXXXXXXA.XXXXX file
			filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
			fileNameA = SecurityUtil.verifyPath(fileNameA);
			filePath = Paths.get( filePathFromDB, fileNameA ).toString();
			if(openBinaryInput(filePath) == false) {
			//inputFileA  = openInputText(filePath, "big5");
			//if(inputFileA == -1) {
				showLogMessage("E", "", String.format("檔案不存在: %s", fileNameA));
	            return -1;
	        }
			
			row = 1; // 第幾列(因為此屬性檔含有兩列資料)
			readCnt = 0;
			byte[] bytes = new byte[48];
			while(true) {		
				readCnt = readBinFile(bytes);
				
				if (readCnt < 5)
	                break;
				
				//text = readTextFile(inputFileA);
				text = new String(bytes, "MS950");
				if(text.trim().length() != 0 ) {
					bytesArr = text.getBytes("MS950");
					
					if( row == 1 ) {
						// this block is the first row
						// check whether the characters from 9th to 16th equals to the bank number
						if( ! CommTxBill.subByteToStr(bytesArr, 8, 16).equalsIgnoreCase(bankNo)  ) {
							showLogMessage("E", "", "資料接收單位錯誤");
							return -1;
						}
					}else {
						// this block is the second row
						
						//showLogMessage("I", "", "A_cnt = " + CommTxBill.subByteToStr(bytesArr,26,30));  //debug
						 
						cntA = Integer.parseInt(CommTxBill.subByteToStr(bytesArr,26,30));
						// 43~44為小數點兩位
						amountA = commTxBill.getBigDecimalDividedBy100(CommTxBill.subByteToStr(bytesArr,30,44));
						
						//showLogMessage("I", "", "A_amt = " + CommTxBill.subByteToStr(bytesArr,30,44));  //debug
						
						

						commTxBill.printDebugString("======" + fileNameD + " ======");
						commTxBill.printDebugString("cntA: " + cntA);
						commTxBill.printDebugString("amountA: " + amountA);		
					}
					
				}				

				row = row + 1;  //下一行
			}
			
			closeBinaryInput();
		
			// ===================================================
*/			
			
			//=====open file and check whether the files exist============
			//open  F00600000.XXXXXXXD.XXXXX file 
			filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
			fileNameD = SecurityUtil.verifyPath(fileNameD);
			filePath = Paths.get(filePathFromDB, fileNameD ).toString();
			if(openBinaryInput(filePath) == false) {
			//inputFileD  = openInputText(filePath, "big5");
			//if(inputFileD == -1) {
				showLogMessage("E", "", String.format("檔案不存在: %s", fileNameD));
	            return -1;
	        }
			
			byte[] bytesD = new byte[400];
			readCnt = 0;
			while(true) {		
				
                readCnt = readBinFile(bytesD);
				
				if (readCnt < 5)
	                break;
				
				//text = readTextFile(inputFileA);
				text = new String(bytesD, "MS950");
				if(text.trim().length() != 0 ) {
					bytesArr = text.getBytes("MS950");
					
					String acquireIndicator = CommTxBill.subByteToStr(bytesArr, 395, 396); // 收單識別欄位

					// ICACQQND明細通知檔,內有二種資料格式,依收單識別欄位區別:
					// 收單識別欄位=1:共同供應契約收單交易,即採購卡交易
					// 收單識別欄位=2:分期付款收單交易,但本支程式應沒有這塊資料請款.若有,則為錯誤: '不應有分期付款收單交易'
					if (acquireIndicator.equals("2")) {
						showLogMessage("E", "", "不應有分期付款收單交易'");
						return -1;
					}
					
//					showLogMessage("I", "", "D_amt = " + CommTxBill.subByteToStr(bytesArr,48,60));  //ttttttt
					
					// 59~60為小數點兩位
					amountD = amountD.add(
							commTxBill.getBigDecimalDividedBy100(CommTxBill.subByteToStr(bytesArr,48,60))) ;
					cntD++;
					
				}
			}		
			
			closeBinaryInput();
			
			// ===================================================
			commTxBill.printDebugString("cntD: " + cntD );
			commTxBill.printDebugString("amountD: " + amountD);
			commTxBill.printDebugString("======" + fileNameD + " ======");
			// ===================================================

/*			
			// check whether the amount and the counts of the data in the D and A are the same.
			if( (cntA != null && cntD != null && amountA != null && amountD != null) 
					&& (cntA.intValue() != cntD.intValue()  || amountA.compareTo(amountD) != 0) ) {
				showLogMessage("E", "", "資料不符");
				return -1;
			}
			commTxBill.printDebugString("資料符合");
*/
			
			// insert into BIL_FISCCTL
			insertBilFiscctl(fileNameA, fileNameD, fileDate, amountD.doubleValue(), cntD);
			
	        return 0;    
	    }
	    catch (Exception e) {
			expMethod = "mainProcess";  
	        expHandle(e); 
	        return  exceptExit; 
		}finally {
			// close the buffers
			finalProcess();
		}
		  
	}

	 /**
	  * This method is to insert data into the table BIL_FISCCTL, 
	  * but before inserting, it will first delete the data in BIL_FISCCTL 
	  * for the exceptions happening before the data inserted into BIL_CURPOST
	  * @param fileNameA
	  * @param fileNameD
	  * @param fileDate
	  * @param amountD2
	  * @param cntD2
	  * @return
	  * @throws Exception
	  */
	 private int insertBilFiscctl(String fileNameA, String fileNameD, String fileDate, Double amountD2, Integer cntD2) throws Exception {
		 String businessDate = getBusiDate();
		 String fctlNo = commTxBill.getFctlNoFromBilFiscctl(fileNameD);
		 
		 if( ! commTxBill.isEmpty(fctlNo)) {
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
		setValue("media_name_a", fileNameA);
		setValue("media_name", fileNameD);
		setValueDouble("total_amt", amountD2);
		setValueInt("total_cnt", cntD2);
		setValue("proc_code", "");
		setValue("prog_name", javaProgram);
		setValue("mod_user", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert bil_fiscctl duplicate", "", comcr.hCallBatchSeqno);
        }
		commTxBill.printDebugString("新增bil_fiscctl成功!!!!!!!!!!!!!!!!!!");
		return 1;
	}

	public static void main(String[] args) {
		BilE162 proc = new BilE162();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}



}

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/31  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.02    JustinWu     change to use the file path from the database and double->BigDecimal
*  109/07/23  V1.00.03    shiyuqi      coding standard, rename field method & format                   * 
*  109/09/04  V1.00.06    Zuwei        code scan issue    
*  109/09/14  V1.00.06    Zuwei        code scan issue    
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard     *
*  111/02/14  V1.00.08    Ryan         big5 to MS950                                           *
*  111/02/18  V1.00.09    JeffKung     不處理A檔                                                                *
*  111/09/22  V1.00.10    JeffKung     seqno以fileDate取最大值,substring改成共用func *
*****************************************************************************/
package Bil;

import java.math.BigDecimal;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;

public class BilE152 extends AccessDAO {
	private  final String progname = "FISC-ICPTXQQ ONUS請款查核處理 111/09/22 V1.00.10";
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
	    	amountD = new BigDecimal("0.0");
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
	        commTxBill = new CommTxBill(getDBconnect(), getDBalias());
//	        comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
	        
	        
			// fileDate:傳檔日期FILE_DATE
			// filePathFromDB: ECS_FTP_LOG.local_dir
			// fileNameD:通知檔案名稱FILE_NAME
			// comcr.hCallBatchSeqno
	        fileDate = args[0];
	        filePathFromDB = args[1];
			fileNameD = args[2];
			comcr.hCallBatchSeqno = args[3];         
			
			// F00600000.ICPTXQQD.XXXXXXXX -> F00600000.ICPTXQQA.XXXXXXXX
			fileNameA = comc.getSubString(fileNameD,0, 17) + "A" + comc.getSubString(fileNameD,18);         
				        
			// select the bank number
			bankNo = commTxBill.getFiscBankNoFromPtrSysParm();
			
			// check whether file name's 2nd to 10th characters equals to the bank number
			if( ! comc.getSubString(fileNameD,1, 9).equalsIgnoreCase(bankNo) ) {
				showLogMessage("E", "", String.format("%s並非F00600000字頭!!", fileNameD));
			return -1;
			}
			
/*			
			//open  F00600000.ICPTXQQA.XXXXX file=====================
			filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
			fileNameA = SecurityUtil.verifyPath(fileNameA);
			filePath = Paths.get( filePathFromDB, fileNameA ).toString();
			inputFileA  = openInputText(filePath, "MS950");
			if(inputFileA == -1) {
				showLogMessage("E", "", String.format("檔案不存在: %s", fileNameA));
	            return -1;
	        }			
*/			
			// check whether processing repetitively
			if(commTxBill.checkRepeatProcess(fileNameD)) {
				return -1;
			}

/*
			//=============屬性檔=======================
			
			while(true) {
				text = readTextFile(inputFileA);
				bytesArr = text.getBytes("MS950");
				if(text.trim().length() != 0 ) {
					// check whether the characters from 9th to 16th equals to the bank number
					if( ! CommTxBill.subByteToStr(bytesArr, 8, 16).equalsIgnoreCase(bankNo)  ) {
						showLogMessage("E", "", "資料接收單位錯誤");
						return -1;
					}
					
					cntA = Integer.parseInt(CommTxBill.subByteToStr(bytesArr, 16, 22));
					// 33,34為小數
					amountA = commTxBill.getBigDecimalDividedBy100(CommTxBill.subByteToStr(bytesArr, 22, 34));
					
					// ===================================================
					commTxBill.printDebugString("======" + fileNameD + " ======");
					commTxBill.printDebugString("cntA: " + cntA);
					commTxBill.printDebugString("amountA: " + amountA);		
					// ===================================================
				}				
	            if (endFile[inputFileA].equals("Y")) 
	            	break;	
			}
			
			closeInputText(inputFileA);  //Jeff add 2020/7/7
			
			//=============屬性檔===================================
*/
			
			//=====open file and check whether the files exist============
			//open  F00600000.XXXXXXXD.XXXXX file 
			filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
			fileNameD = SecurityUtil.verifyPath(fileNameD);
			filePath = Paths.get(filePathFromDB, fileNameD ).toString();
			if (openBinaryInput(filePath) == false) {
				showLogMessage("E", "", String.format("檔案不存在: %s", fileNameD));
	            return -1;
	        }
			
			int readCnt = 0;
			byte[] bytes = new byte[300];

			while(true) {
				
				readCnt = readBinFile(bytes);
				if (readCnt < 5)
	                break;
				
				if (bytes.length != 0 ) {
				
					// 若交易金額延伸註記碼為Z，則交易金額為從47到57
					// 否則，則交易金額為從49到57
					if(CommTxBill.subByteToStr(bytes,76,77).equalsIgnoreCase("Z")) {
						// 56,57byte為小數
						amountD = amountD.add(
							commTxBill.getBigDecimalDividedBy100(CommTxBill.subByteToStr(bytes,46,57))) ;
					}else {
						// 56,57byte為小數
						amountD = amountD.add(
							commTxBill.getBigDecimalDividedBy100(CommTxBill.subByteToStr(bytes,48,57))) ;
					}
				 
					cntD++;
				}		
			}
			
			closeBinaryInput();    //Jeff add 2020/7/7
				
			//=============明細檔===================================
			// ===================================================
			commTxBill.printDebugString("cntD: " + (cntD) );
			commTxBill.printDebugString("amountD: " + amountD);
			commTxBill.printDebugString("======" + fileNameD + " ======");
			// ===================================================

/*			
			// check whether the amount and the counts of the data in the D and A are the same.
			if( (cntA != null && cntD != null && amountA != null && amountD != null) 
					&& (cntA.intValue() != (cntD.intValue())  ||  amountA.doubleValue() != amountD.doubleValue()) ) {
				showLogMessage("E", "", "recordAmount: " + amountD);
				showLogMessage("E", "", "recordCnt: " + cntD);
				showLogMessage("E", "", "trailerAmount: " + amountA);
				showLogMessage("E", "", "trailerRecordCnt: " + cntA);
				showLogMessage("E", "", "資料不符");
				return -1;
			}
			commTxBill.printDebugString("資料符合");
*/				
			
			// insert into BIL_FISCCTL
			insertBilFiscctl(fileNameD, fileDate, amountD.doubleValue(), cntD);
			
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
	 private int insertBilFiscctl(String fileNameD, String fileDate, Double amountD2, Integer cntD2) throws Exception {
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
		setValue("media_name_a", "");
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
		BilE152 proc = new BilE152();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}



}

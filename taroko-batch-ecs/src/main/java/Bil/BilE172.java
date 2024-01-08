/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 109/04/01  V1.00.00    JustinWu     program initial                          *
* 109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
* 109/05/26  V1.00.02    JustinWu     change to use the file path from the database and double->BigDecimal
* 109/07/03  V1.00.03    shiyuqi      updated for project coding standard     *
* 109/07/23  V1.00.04    shiyuqi      coding standard, rename field method & format  
* 109/09/04  V1.00.06    Zuwei        code scan issue    
* 109/09/14  V1.00.06    Zuwei        code scan issue    
* 109-10-19  V1.00.07    shiyuqi      updated for project coding standard     *
* 111/02/14  V1.00.08    Ryan         big5 to MS950                           *
* 111/09/22  V1.00.09    JeffKung     seqno以fileDate取最大值,substring改成共用func *
*****************************************************************************/
package Bil;

import java.math.BigDecimal;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;

public class BilE172 extends AccessDAO {
	private  final String progname = "FISC-INSTQQN分期付款請款查核處理 111/09/22 V1.00.09";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;

	 int mainProcess(String[] args) {
		int inputFileD = -1;
	    String fileNameD="", filePathFromDB="",  fileDate="";
	    String filePath = "";
	    BigDecimal amountD;
		Integer cntD;
		
	    try {
	    	amountD = BigDecimal.ZERO;
	    	cntD = 0;
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

			//=====open file and check whether the files exist============
			//open  F00600000.XXXXXXXD.XXXXX file 
			filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
			fileNameD = SecurityUtil.verifyPath(fileNameD);
			filePath = Paths.get(filePathFromDB, fileNameD ).toString();
			inputFileD  = openInputText(filePath, "MS950");
			if(inputFileD == -1) {
				showLogMessage("E", "", String.format("檔案不存在: %s", fileNameD));
	            return -1;
	        }
			
			//===========================================
			
			// check whether processing repetitively
			if(commTxBill.checkRepeatProcess(fileNameD)) {
				return -1;
			}
		
			// ===================================================
			while(true) {
				text = readTextFile(inputFileD);
				if(text.trim().length() != 0 ) {
					bytesArr = text.getBytes("MS950");
					
					//trialer不用處理
					if(CommTxBill.subByteToStr(bytesArr, 0, 1).equalsIgnoreCase("T")) {
						continue;
					}
					
					String acquireIndicator = CommTxBill.subByteToStr(bytesArr, 395, 396); // 收單識別欄位

					// ICACQQND明細通知檔,內有二種資料格式,依收單識別欄位區別:
					// 收單識別欄位=1:共同供應契約收單交易，但本支程式應沒有這塊資料請款.若有,則為錯誤: '不應有採購卡交易'
					// 收單識別欄位=2:分期付款收單交易
					if (acquireIndicator.equals("1")) {
						showLogMessage("E", "", "不應有採購卡交易'");
						return -1;
					}
					// 59,60byte為小數
					amountD = amountD.add(
							commTxBill.getBigDecimalDividedBy100(CommTxBill.subByteToStr(bytesArr,48,60)));
					cntD++;
					
				}
				
	            if (endFile[inputFileD].equals("Y")) 
	            	break;
			}		
			
			// ===================================================
			commTxBill.printDebugString("cntD+1: " + (cntD+1) );
			commTxBill.printDebugString("amountD: " + amountD);
			commTxBill.printDebugString("======" + fileNameD + " ======");
			// ===================================================
			
			// insert into BIL_FISCCTL
			insertBilFiscctl( fileNameD, fileDate, amountD.doubleValue(), cntD);
			
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
		BilE172 proc = new BilE172();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}



}

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/16  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     remove .txt String following fileName
*  109/05/26  V1.00.03    JustinWu     change to use the file path from the database and double->BigDecimal
*  109/07/23  V1.00.04    shiyuqi      coding standard, rename field method & format                   *   
*  109/09/04  V1.00.05    Zuwei        code scan issue    
*  109/09/14  V1.00.06    Zuwei        code scan issue    
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard     *
*  111/02/14  V1.00.08    Ryan         big5 to MS950                                           *
*  111/03/09  V1.00.09    JeffK        屬性檔比對不一致時不abend, 照常處理           *
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

public class BilE122 extends AccessDAO {
    private final String progname = "FISC-ICFXJQB磁條卡請款查核處理 111/09/22 V1.00.10";
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
//	        comcr.h_call_batch_seqno = args.length > 0 ? args[args.length - 1] : "";
	        
	        
			// fileDate:傳檔日期FILE_DATE
			// filePathFromDB: ECS_FTP_LOG.local_dir
			// fileNameD:通知檔案名稱FILE_NAME
			// comcr.h_call_batch_seqno
	        fileDate = args[0];
	        filePathFromDB = args[1];
			fileNameD = args[2];
			comcr.hCallBatchSeqno = args[3];
			
			// F00600000.ICFXJQBD.YYMMDDNN -> F00600000.ICFXJQBA.YYMMDDNN
			fileNameA = comc.getSubString(fileNameD,0, 17) + "A" + comc.getSubString(fileNameD,18);         
	        
			// select the bank number
			bankNo = commTxBill.getFiscBankNoFromPtrSysParm();
			
			// check whether file name's 2nd to 10th characters equals to the bank number
			if( ! comc.getSubString(fileNameD,1, 9).equalsIgnoreCase(bankNo) ) {
				showLogMessage("E", "", String.format("%s並非F00600000字頭!!", fileNameD));
				return -1;
			}
			
			//===========================================
			
			//open  F00600000.ICFXJQBD.XXXXX file=====================
			filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
			fileNameA = SecurityUtil.verifyPath(fileNameA);
			filePath = Paths.get( filePathFromDB, fileNameA ).toString();
			inputFileA  = openInputText(filePath, "MS950");
			if(inputFileA == -1) {
				showLogMessage("E", "", String.format("檔案不存在: %s", fileNameA));
	            return -1;
	        }			
			
			// check whether processing repetitively
			if(commTxBill.checkRepeatProcess(fileNameD)) {
				return -1;
			}
			
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
					amountA =commTxBill.getBigDecimalDividedBy100(CommTxBill.subByteToStr(bytesArr, 22, 34));
					
					// ===================================================
					showLogMessage("I", "","======" + fileNameA + " ======");
					showLogMessage("I", "","cntA: " + cntA);
					showLogMessage("I", "","amountA: " + amountA);
					
					//commTxBill.printDebugString("======" + fileNameD + " ======");
					//commTxBill.printDebugString("cntA: " + cntA);
					//commTxBill.printDebugString("amountA: " + amountA);		
					// ===================================================
				}				
	            if (endFile[inputFileA].equals("Y")) 
	            	break;	
			}
			
			closeInputText(inputFileA);  //Jeff add 2020/7/7
						
			//=============屬性檔===================================
			//=============明細檔===================================
			//open  F00600000.ICFXJQBD.XXXXX file ==================
			filePathFromDB = SecurityUtil.verifyPath(filePathFromDB);
			fileNameD = SecurityUtil.verifyPath(fileNameD);
			filePath = Paths.get(filePathFromDB, fileNameD ).toString();
			
			if(openBinaryInput(filePath) == false) {
				showLogMessage("E", "", String.format("檔案不存在: %s", fileNameD));
	            return -1;
	        }
			
			int readCnt = 0;
			byte[] bytes = new byte[360];

			while(true) {
				
				readCnt = readBinFile(bytes);
				if (readCnt < 5)
	                break;
				
				if (bytes.length != 0 ) {
					
						// check whether the characters from 6st to 13th equals to the bank number
						if( ! CommTxBill.subByteToStr(bytes,5,13).equalsIgnoreCase(bankNo)  ) {
							showLogMessage("E", "", "參加單位代號錯誤");
							return -1;
						}
						
						// if true表示是通知檔內第一筆美元匯率資訊,取出JCB匯率(Mid Rate)金額
						// else為非美元匯率的請款交易資料,取出購買地金額
						if( commTxBill.isCurrRateTx(CommTxBill.subByteToStr(bytes,0,4)) ) {
							// 78,79為小數
							String temp = CommTxBill.subByteToStr(bytes,67,79);
							amountD = amountD.add(commTxBill.getBigDecimalDividedBy100(temp));
							cntD++;
						}else {
							// 78,79為小數
							String temp = CommTxBill.subByteToStr(bytes,67,79);
							amountD = amountD.add(commTxBill.getBigDecimalDividedBy100(temp));
							cntD++;
						}
				}
			}		
			
			closeBinaryInput();    //Jeff add 2020/7/7
			
			//=============明細檔===================================
			// ===================================================
			showLogMessage("I", "","======" + fileNameD + " ======");
			showLogMessage("I", "","cntD: " + cntD);
			showLogMessage("I", "","amountD: " + amountD);
			
			//commTxBill.printDebugString("cntD: " + (cntD) );
			//commTxBill.printDebugString("amountD: " + amountD);
			//commTxBill.printDebugString("======" + fileNameD + " ======");
			// ===================================================
			
			// check whether the amount and the counts of the data in the D and A are the same.
			if( (cntA != null && cntD != null && amountA != null && amountD != null) 
					&& (cntA.intValue() != cntD.intValue()  || amountA.compareTo(amountD) != 0) ) {
				showLogMessage("E", "", "資料不符");
				//return -1;
			}
			commTxBill.printDebugString("資料符合");
			
			// insert into BIL_FISCCTL
			insertBilFiscctl(fileNameA, fileNameD, fileDate, amountD.doubleValue(), cntD);

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
		BilE122 proc = new BilE122();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}



}

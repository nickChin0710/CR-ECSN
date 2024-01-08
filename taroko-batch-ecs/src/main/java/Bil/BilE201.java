/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/11/23  V1.00.00    Ryan      program initial                          *
*  111/02/14  V1.00.01    Ryan      big5 to MS950                            *
*  111/12/22  V1.00.02    JeffKung  Phase3修改                                                              *
*  112/06/07  V1.00.03    JeffKung  效期格式:YYMM->MMYY                       *
*  112/08/30  V1.00.04    JeffKung  bypassIdCheck                            *
******************************************************************************/
package Bil;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;

public class BilE201 extends BilE200 {
    private  final String PROGNAME = "台產房貸保險扣款處理程式 112/08/30 V1.00.04";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;
    CommFTP commFTP = null;
    CommRoutine comr = null;
    
    boolean debug = false; 
    
    Integer recordAmount, trailerAmount; // 交易總來源金額
	Integer recordCnt, trailerRecordCnt;   // record總筆數
	Integer recordTransCnt, trailerTransCnt;   // 交易總筆數
	byte[] bytesArr = null;
	String filePathFromDB = "";
	String inputFileName = "";
	String outputFileName = "";
	String filePath = "";
	String hBusinessDate = "";
	String hPurchaseDate = "";
	String newLine="\r\n";
	int okCnt = 0;//成功筆數
	int errCnt = 0;//失敗筆數
	double okAmt = 0;//成功金額
	double errAmt = 0;//失敗金額
	int backCnt = 0;//回饋筆數
	double backAmt = 0;//回饋金額

    public int mainProcess(String[] args) {
	    try {
	    	recordAmount = 0;
	    	trailerAmount = 0;
	    	recordCnt = 0;
	    	trailerRecordCnt = 0;
	    	recordTransCnt = 0;
	    	trailerTransCnt = 0;
		    hBillUnit = "TF";
		    hBillType = "TFMI";
		    hFileType = "TFMID";
	    	
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME + "," + args.length);
            // =====================================
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            commTxBill = new CommTxBill(getDBconnect(), getDBalias());
            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            
            hBusinessDate = commTxBill.getBusiDate();
            super.hBusinessDate = hBusinessDate;
            
            for (int argi=0; argi < args.length ; argi++ ) {
            	  if (args[argi].equals("debug")) {
            		  debug = true;
            		  super.debug=true;
            	  }
            	  if (args[argi].equals("bypassID")) {
            		  super.hBypassIdCheck="Y";
            	  }
            }
            
            processStart();


            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    
    protected int inputFileProcess() throws Exception {

    	extendField = "eflog.";
        sqlCmd = " SELECT file_name, file_date, local_dir as file_path, rowid as rowid ";
        sqlCmd += " FROM ecs_ftp_log  ";
        sqlCmd += " where 1=1 ";
        sqlCmd += " and group_id in  ('ONUS01', 'ONUS11') ";
        sqlCmd += " and trans_mode='RECV'  ";
        sqlCmd += " and trans_resp_code = 'Y'  ";
        sqlCmd += " and file_name like '03557115_3144_C02_%' ";
        sqlCmd += " and proc_code in ('', '0') ";
        int cursorIndex = selectTable();
        
        if (cursorIndex==0) {
        	showLogMessage("I", "", "無需要處理的檔案資料!!");
        	return 0;
        }
        
        for (int intc = 0 ; intc < cursorIndex ; intc++ ){

        	showLogMessage("I", "", "開始讀取檔案....");
            int rc = readFile(intc);
            
            if (rc == 0) {
                showLogMessage("I", "", "開始首筆尾筆檢核....");
                rc = checkHeaderTailer();
                if (rc !=0) {
                	updateBilOnusbillExchange(fctlNo);  //將讀入的資料標記為整批處理失敗 status_code = '01' (暫定)
                }
            } else {
            	/*檔案不存在或檔案已處理過才會有error */
            	;
            }

            updateBilFiscctl(fctlNo,recordAmount.doubleValue(),recordTransCnt);   //不管檢核結果,都將實際筆數金額回寫,proc_code = "Y"
            updateEcsFtpLog(getValue("eflog.rowid",intc));  //標記已處理

            if (rc != 0 ) {
            	return -1;
            }
        }
        return 0;
    };

    private int readFile(int intc) throws Exception {
 		filePathFromDB = SecurityUtil.verifyPath(getValue("eflog.file_path",intc));
		inputFileName = SecurityUtil.verifyPath(getValue("eflog.file_name",intc));
		filePath = Paths.get(filePathFromDB,inputFileName).toString();
		String temstr = "";
		BilE200InData onusInData = null;
		int inputFileD  = openInputText(filePath, "MS950");
		if (inputFileD == -1) {
			showLogMessage("E", "", String.format("檔案不存在: %s", inputFileName));
			return -1;
        }	
		
		// check whether processing repetitively
		if(checkRepeatProcess(inputFileName)) {
			showLogMessage("E", "", String.format("檔案已處理過: %s", inputFileName));
			return -1;
		}
		
		// insert into BIL_FISCCTL  ＃＃編fctlNo
		insertBilFiscctl( inputFileName, recordAmount.doubleValue(), recordTransCnt);
		
		//開始讀檔案
        while (true) {
            temstr = readTextFile(inputFileD);
            bytesArr = temstr.getBytes("MS950");
			if(temstr.trim().length() > 5 ) {
				
				//showLogMessage("I", "", String.format("讀入資料: %s", temstr));
				
				onusInData = new BilE200InData();
				getDataD(onusInData, bytesArr);
				insertBilOnusbillExchange(onusInData, inputFileName, fctlNo);
				onusInData = null;
				
				switch(CommTxBill.subByteToStr(bytesArr, 0, 1)) {			
				case "1":
					
					if( ! checkHeader(bytesArr, inputFileName))
						return -1;
					break;
				case "3":
					// TRAILER
					if(CommTxBill.subByteToStr(bytesArr, 0, 1).equalsIgnoreCase("3")) {
						
						trailerTransCnt = comcr.str2int( CommTxBill.subByteToStr(bytesArr, 1, 9) );
						trailerRecordCnt = comcr.str2int( CommTxBill.subByteToStr(bytesArr, 1, 9) );
						trailerAmount =comcr.str2int( CommTxBill.subByteToStr(bytesArr, 9, 17) );
					}
					break;
				default: 
					// =================default begin=======================================
					recordAmount += comcr.str2int( CommTxBill.subByteToStr(bytesArr, 38, 44) );
					recordCnt ++;
					recordTransCnt++;
					break;
					// =================default begin=======================================
				}
			}
			
			if (endFile[inputFileD].equals("Y")) 
            	break;

		}	
        closeInputText(intc);
        return 0;
    }

    protected int checkHeaderTailer() throws Exception {
		// check whether the amount and the counts of the data in the D and A are the same.
		if( (recordCnt != null && trailerRecordCnt != null && recordAmount != null 
				&& trailerAmount != null && recordTransCnt != null && trailerTransCnt != null) 
				&& (recordCnt.intValue() != (trailerRecordCnt.intValue())  || recordAmount.compareTo(trailerAmount) != 0 
				|| recordTransCnt.intValue() != (trailerTransCnt.intValue())) ) {
			showLogMessage("E", "", "首筆尾筆檢核資料不符");
			showLogMessage("E", "", "recordAmount: " + recordAmount +  "recordCnt: " + recordCnt + "recordTransCnt: " + recordTransCnt);
			showLogMessage("E", "", "trailerAmount: " + trailerAmount +"trailerRecordCnt: " + trailerRecordCnt + "trailerTransCnt: " + trailerTransCnt);
			return -1;
		}
		showLogMessage("I", "", "首筆尾筆檢核資料相符!");
		showLogMessage("I", "", "recordAmount: " + recordAmount +  "recordCnt: " + recordCnt + "recordTransCnt: " + recordTransCnt);
		showLogMessage("I", "", "trailerAmount: " + trailerAmount +"trailerRecordCnt: " + trailerRecordCnt + "trailerTransCnt: " + trailerTransCnt);
		return 0;
    };

	protected int outputFileProcess() throws Exception {

		extendField = "boe.";
		sqlCmd = " select fctl_no ";
		sqlCmd += "  from BIL_ONUSBILL_EXCHANGE ";
		sqlCmd += " where 1=1 ";
		sqlCmd += " and file_date = ?  ";
		sqlCmd += " and file_type = ?  ";
		sqlCmd += " group by fctl_no ";
		setString(1, hBusinessDate);
		setString(2, this.hFileType);

		int recCnt = selectTable();
		if (recCnt == 0) {
			showLogMessage("I", "", "無資料需產生回覆檔!!");
			return 0;
		}

		for (int inti = 0; inti < recCnt; inti++) {

			daoTable = "BIL_ONUSBILL_EXCHANGE";
	    	fetchExtend = "outboe.";
	    	
			sqlCmd = " select * ";
			sqlCmd += "  from BIL_ONUSBILL_EXCHANGE ";
			sqlCmd += " where 1=1 ";
			sqlCmd += " and fctl_no = ?  ";
			sqlCmd += " order by rec_type ";

			setString(1, getValue("boe.fctl_no",inti));

			openCursor();

			int fo = 0;
			int totalCnt = 0;

			while (fetchTable()) {
				totalCnt++;
				if (totalCnt == 1)
					fo = open_file();
				sumFooter();
				writeMediaFile(fo);
			}
			
			if (totalCnt > 0) {
				closeOutputText(fo);
				procFTP(outputFileName);
			}
			closeCursor();
		}

		return 0;
	}

	/***********************************************************************/
	int open_file() throws Exception {
		outputFileName = String.format("03557115_3144_C03_%s.txt",hBusinessDate);
		String outputFilePath = String.format("%s/media/bil", comc.getECSHOME());
		outputFileName = Normalizer.normalize(outputFileName, java.text.Normalizer.Form.NFKD);
		int fo = openOutputText(outputFilePath+"/"+outputFileName,"MS950");
		if (fo == -1) {
			showLogMessage("I", "", "ERROR: " + outputFileName + "檔案開啓失敗！");
			insertEcsNotifyLog(outputFileName);
			commitDataBase();
			exitProgram(1);
		}
		return fo;
	}

	/***********************************************************************/
	void writeMediaFile(int fo) throws Exception {
		String outData = "";
		StringBuffer outStr = null;

		if (getValue("outboe.rec_type").equals("1")) {
			outStr = new StringBuffer();
			outStr.append(comc.fixLeft(getValue("outboe.row_data"), 90));
			outStr.replace(8, 9, "2");
			outData += comc.getSubString(outStr.substring(0), 0,90);
			writeTextFile(fo, outData+newLine);
			
		} else if (getValue("outboe.rec_type").equals("2")) {
			double destAmt = getValueDouble("outboe.dest_amt"); 
			double backDestAmt = 0;
			if ("00".equals(getValue("outboe.status_flag"))) {
				backDestAmt = doubleMul(destAmt,0.02);
			}
			outStr = new StringBuffer();
			outStr.append(comc.fixLeft(getValue("outboe.row_data"), 90));
			//outStr.replace(36, 38, rpad(getValue("outboe.txn_code"),2));//交易碼(維持原來的資料)
			outStr.insert(44, fixNum(Double.toString(backDestAmt),6));//回饋金額
			outStr.replace(50, 52, rpad(getValue("outboe.status_flag"),2));//狀態
			outData += comc.getSubString(outStr.substring(0), 0,90);
			writeTextFile(fo, outData+newLine);

		} else if (getValue("outboe.rec_type").equals("3")) {
			outStr = new StringBuffer();
			outStr.append(comc.fixLeft(getValue("outboe.row_data"), 90));
			outStr.replace(17, 25, fixNum(Integer.toString(okCnt),8));//成功筆數
			outStr.replace(25, 33, fixNum(Double.toString(okAmt),8));//成功金額
			outStr.replace(33, 41, fixNum(Integer.toString(backCnt),8));//回饋筆數
			outStr.replace(41, 49, fixNum(Double.toString(backAmt),8));//回饋金額
			outStr.replace(49, 57, fixNum(Integer.toString(errCnt),8));//失敗筆數
			outStr.replace(57, 65, fixNum(Double.toString(errAmt),8));//失敗金額
			outData += comc.getSubString(outStr.substring(0), 0,90);
			writeTextFile(fo, outData+newLine);
		}
	}

	 /**
	  * check whether this data has already been processed
	  **/
	public boolean checkRepeatProcess(String inputFileName) throws Exception {
	        selectSQL = " count(*) as counts";
	        daoTable = " bil_fiscctl ";
	        whereStr = " where 1=1 "
	        		+ " and fctl_type= 'ONUS' " 
	        		+ " and media_name = ? "
	        		+ " and proc_code = 'Y' "
	        		;   

	        setString(1, inputFileName);
	        selectTable();
	        if ( getValueInt("counts") > 0) {
	        	showLogMessage("E", ""
	           			, String.format("檔名:{%s, %s}已經執行處理過", "ONUS", inputFileName) );
	           	return true;
          }else {
       	   return false;
          }	       
		}
    
	 /**
	  * check the giving header 
	  * @param bytesArr
	  * @param inputFileName
	  * @param bankNo
	  * @return
	  */
	 private boolean checkHeader(byte[] bytesArr, String inputFileName) {
		// check whether the record indicator equals to '1'
			if(! CommTxBill.subByteToStr(bytesArr, 0, 1).equalsIgnoreCase("1") ) {
				showLogMessage("E", "", "HEADER的RECORD 識別碼錯誤");
				return false;
			}
			
			// 檔案產出類型是否與fileName的中間名稱一致
			/*
			if(! CommTxBill.subByteToStr(bytesArr, 1, 9).equalsIgnoreCase( inputFileName.substring(3, 11) ) ) {
				showLogMessage("E", "", "檔案產出類型與fileName的中間名稱不一致");
				return false;
			}
			*/
			
		return true;
	}
	 
	 /**
	  * This method is to insert data into the table BIL_FISCCTL, 
	  * but before inserting, it will first delete the data in BIL_FISCCTL 
	  * for the exceptions happening before the data inserted into BIL_CURPOST
	  * @param inputFileName
	  * @param fileDate
	  * @param recordAmount
	  * @param recordTransCnt
	  * @return
	  * @throws Exception
	  */
	 private int insertBilFiscctl( String inputFileName,Double recordAmount, Integer recordTransCnt) throws Exception {
		 String fctlNo = getFctlNoFromBilFiscctl(inputFileName);

		if (!commTxBill.isEmpty(fctlNo)) {
			// delete data
			commTxBill.deleteBilFiscctl(fctlNo);
		}
		
		int newSeqNo = getMaxSeqNo(hBusinessDate) + 1;
		daoTable = "bil_fiscctl";
		setValue("fctl_no",  hBusinessDate + "ONUS" + String.format("%04d", newSeqNo));
		setValueInt("fctl_seq", newSeqNo);
		setValue("fctl_date",  hBusinessDate);
		setValue("fctl_type",  "ONUS");
		setValue("file_date", hBusinessDate);
		setValue("media_name_a", "");
		setValue("media_name", inputFileName);
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
		super.fctlNo = getValue("fctl_no");
		
		return 0;
	}

		private void updateBilOnusbillExchange(String fctlNo) throws Exception {
			daoTable = " bil_onusbill_exchange ";
			updateSQL = " status_flag = '01', " + " mod_pgm = ?, " + " mod_time = sysdate ";   //status_flag : 處理結果狀態註記
			whereStr = " where fctl_no = ?  ";
			showLogMessage("E","","javaProgram:"+javaProgram);
			showLogMessage("E","","fctlNo:"+fctlNo);
			setString(1, javaProgram);
			setRowId(2, fctlNo);

			int returnInt = updateTable();
			if (returnInt == 0) {
				showLogMessage("E","","Fail to update bilOnusbillExchange, fctlNo:["+fctlNo+"]");
			}
		}

	private void updateEcsFtpLog(String rowid) throws Exception {
		daoTable = " ecs_ftp_log ";
		updateSQL = " proc_code = 'Y', " + " mod_pgm = ?, " + " mod_time = sysdate ";
		whereStr = " where rowid = ?  ";
		
		setString(1, javaProgram);
		setRowId(2, rowid);

		int returnInt = updateTable();
		if (returnInt == 0) {
			showLogMessage("E","","Fail to update EcsFtpLog.proc_code, rowid:["+rowid+"]");
		}

	}
	
	/**
	 * update bil_fiscctl set proc_code='Y'
	 * 
	 * @param inputFileName
	 * @return
	 * @throws Exception
	 */
	public int updateBilFiscctl(String fctlNo,Double recordAmount, Integer recordTransCnt) throws Exception {

		daoTable = " bil_fiscctl ";
		updateSQL = " total_amt = ? , total_cnt = ? , proc_code= 'Y' ";
		whereStr = " where 1=1 " + " and fctl_no = ? ";

		setDouble(1, recordAmount);
		setInt(2,recordTransCnt);
		setString(3, fctlNo);

		if (updateTable() == 0) {
			showLogMessage("E","","Fail to update total_amt & total_cnt From bil_fiscctl, fctl_no = [" + fctlNo + "]");
			return -1;
		}
		
		return 0;
	}
	
	 private  int getMaxSeqNo(String busiDate) throws Exception {
			
			selectSQL = " max(fctl_seq) as maxSeqNo ";
			daoTable = " bil_fiscctl ";
			whereStr = " where 1=1 " 
			        + " and fctl_date= ? " 
					+ " and fctl_type='ONUS' ";
			
			setString(1, busiDate);
			
			selectTable();
			
			// getValueInt: if the value is null, then return 0, otherwise return this value.
			return getValueInt("maxSeqNo");
		
		}
	 
		/**
		 * insert data into bil_onusbill_exchange
		 * @param onusInData
		 * @param fileName
		 * @param fctlNo
		 * @throws Exception
		 */
	private void insertBilOnusbillExchange(BilE200InData onusInData, String fileName, String fctlNo) throws Exception {
			
		daoTable = "bil_onusbill_exchange";
			
		// ====================================================
		setValue("REFERENCE_NO",commTxBill.getReferenceNo() );
		setValue("FCTL_NO", fctlNo);  
		setValue("FILE_TYPE", this.hFileType);
		setValue("FILE_DATE", hBusinessDate);
		setValue("PURCHASE_DATE", hPurchaseDate);
		setValue("BRANCH", onusInData.branch);
		setValue("ID_NO", onusInData.idNo);
		setValue("CARD_NO", onusInData.cardNo);
		setValue("EXPIRE_YYMM", onusInData.expireYYMM);
		setValue("EXPIRE_MMYY",  onusInData.expireMMYY);
		setValue("TRAN_CODE", onusInData.tranCode);
		setValue("BILL_TYPE", hBillType); 
		setValue("TXN_CODE", onusInData.txnCode);
		setValueDouble("DEST_AMT", Double.parseDouble(onusInData.destinationAmount));
		setValue("DEST_CURR", "901");
		setValue("EXTRA_INFO", onusInData.extraInfo);
		setValue("AUTH_CODE", onusInData.authCode);
		setValue("STATUS_FLAG", onusInData.statusFlag);
		setValue("POST_DATE", "");
		setValue("POST_FLAG", "");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		setValue("REC_TYPE",onusInData.recType);
		setValue("ROW_DATA",onusInData.rowData);
		setValue("MCHT_CATEGORY",onusInData.mchtCategory);
		setValue("MCHT_CHI_NAME",onusInData.mchtChiName+comcr.getSubString(onusInData.extraInfo, 0,20));
		
		// ==============================================
			
		insertTable();
	    if (dupRecord.equals("Y")) {
	    	comcr.errRtn("insert bil_onusbill_exchange duplicate", "", onusInData.cardNo);
	    }
	    
		commTxBill.printDebugString("新增bil_fiscdtl成功!!!!!!!!!!!!!!!!!!");		
	}
	/**
	 * separate each column form the bytesArr 
	 * @param onusInData
	 * @param bytesArr
	 * @throws Exception 
	 */
	private void getDataD(BilE200InData onusInData, byte[] bytesArr) throws Exception {
		if (CommTxBill.subByteToStr(bytesArr, 0, 1).equals("2"))  //明細資料
		{
			onusInData.recType = "2";
			onusInData.branch = CommTxBill.subByteToStr(bytesArr, 1,5);  // 分行代號
			onusInData.idNo = CommTxBill.subByteToStr(bytesArr, 5,16);  // 正卡人ID
			onusInData.cardNo = CommTxBill.subByteToStr(bytesArr, 16,32);  // 卡號
			onusInData.expireMMYY = CommTxBill.subByteToStr(bytesArr, 32,36);  // 效期(YYMM)
			onusInData.tranCode = CommTxBill.subByteToStr(bytesArr, 36,38);  // 交易代碼
			onusInData.destinationAmount = CommTxBill.subByteToStr(bytesArr, 38,44);  // 當地金額
			onusInData.statusFlag = CommTxBill.subByteToStr(bytesArr, 44,46);  // 扣款情形訊息
			onusInData.extraInfo = CommTxBill.subByteToStr(bytesArr, 46,66);  // 備註
			onusInData.rowData = new String(bytesArr, "MS950"); //原始檔案資料
			onusInData.mchtCategory = "6300";
			onusInData.mchtChiName = "臺產火險";
			if(onusInData.tranCode.equals("40")){
				onusInData.txnCode = "05";
			}
			if(onusInData.tranCode.equals("41")){
				onusInData.txnCode = "06";
			}
			onusInData.authCode = "";
		}
		else 	{
			onusInData.branch = ""; // 分行代號
			onusInData.idNo = ""; // 正卡人ID
			onusInData.cardNo = ""; // 卡號
			onusInData.expireYYMM = ""; // 效期(YYMM)
			onusInData.expireMMYY = ""; // 效期(MMYY)
			onusInData.tranCode = ""; // 交易代碼
			onusInData.destinationAmount = "0"; // 當地金額
			onusInData.statusFlag = ""; // 扣款情形訊息
			onusInData.extraInfo = ""; // 備註
			onusInData.mchtCategory = ""; // 特店類別碼
			onusInData.mchtChiName = ""; // 特店中文名稱
			onusInData.txnCode = "";
			onusInData.authCode = "";
			onusInData.rowData = new String(bytesArr, StandardCharsets.UTF_8); // 原始檔案資料
			if (CommTxBill.subByteToStr(bytesArr, 0, 1).equals("1"))  {  //首筆資料
				onusInData.purchaseDate = comcr.formatDate(CommTxBill.subByteToStr(bytesArr, 1,8),3);
				hPurchaseDate = onusInData.purchaseDate; 
				onusInData.recType = "1";
			} else {
				onusInData.recType = "3";
			}
		}
	}
	
	/**
	 * return fctl_no, if this method cannot find fctl_no, then return ""
	 * 
	 * @param inputFileName
	 * @return
	 * @throws Exception
	 */
	public String getFctlNoFromBilFiscctl(String inputFileName) throws Exception {
		selectSQL = " fctl_no ";
		daoTable = " bil_fiscctl ";
		whereStr = " where 1=1 "
				+ " and fctl_type='ONUS' " + " and media_name = ?" + " and proc_code <> 'Y' ";

		setString(1, inputFileName);

		if (selectTable() <= 0) {
			// 找不到fctl_no
			return "";
		} else {
			return getValue("fctl_no");
		}
	}
	
	void procFTP(String isFileName) throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "WFT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/bil", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "BilE201"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "put " + isFileName + " 開始傳送....");
		int err_code = commFTP.ftplogName("WFT", "put " + isFileName);

		if (err_code != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + err_code);
			insertEcsNotifyLog(isFileName);
		} else {
			comc.fileMove(String.format("%s/media/bil/%s", comc.getECSHOME() , isFileName),
					String.format("%s/media/bil/backup/%s", comc.getECSHOME() , isFileName) );
		}
	}
			
	public int insertEcsNotifyLog(String fileName) throws Exception {
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("unit_code", comr.getObjectOwner("3", javaProgram));
		setValue("obj_type", "3");
		setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_name", "媒體檔名:" + fileName);
		setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_desc2", "");
		setValue("trans_seqno", commFTP.hEflgTransSeqno);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "ecs_notify_log";

		insertTable();

		return (0);
	}
	
	public static Double doubleMul(Double v1,Double v2){

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.multiply(b2).doubleValue();

	}
	
	public static Double doubleAdd(Double v1,Double v2){

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.add(b2).doubleValue();

	}
	
	String fixNum(String adValue ,int asFormat){
		adValue = String.format("%s",Math.round(Double.parseDouble(adValue))); 
		for(int i = adValue.length() ;i<asFormat;i++){
			adValue = "0"+adValue;
		}
		return adValue;
	}
	
	String rpad(String s1, int size) {
		  return String.format("%-" + size + "s", s1);
	}
	
	void sumFooter() throws Exception{
		String statusFlag  = getValue("outboe.status_flag");
		double destAmt  = getValueDouble("outboe.dest_amt");
		if (!getValue("outboe.rec_type").equals("2")) 
				return;
		if(statusFlag.equals("00")){
			okCnt++;//成功筆數
			okAmt = doubleAdd(destAmt,okAmt);//成功金額
			
			backCnt++;//回饋筆數
			backAmt += doubleMul(destAmt,0.02);//回饋金額
			
		} else	{
			errCnt++;// 失敗筆數
			errAmt = doubleAdd(destAmt, errAmt);// 失敗金額
		}
		
	}

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilE201 proc = new BilE201();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}



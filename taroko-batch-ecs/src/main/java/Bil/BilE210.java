/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/11/23  V1.00.00    Ryan      program initial                          *
*  111/02/14  V1.00.01    Ryan      big5 to MS950                            *
*  112/03/20  V1.00.02    JeffKung  停車退費加原始消費日期
*  112/08/30  V1.00.03    JeffKung  bypassIdCheck                            *
*****************************************************************************/
package Bil;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Arrays;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;

public class BilE210 extends BilE200 {
    private  final String PROGNAME = "公用事業費用請款（含ACH/非ACH) 112/08/30 V1.00.03";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;
    CommFTP commFTP = null;
    CommRoutine comr = null;
    
    Integer recordAmount, trailerAmount; // 交易總來源金額
	Integer recordCnt, trailerRecordCnt;   // record總筆數
	Integer recordTransCnt, trailerTransCnt;   // 交易總筆數
	byte[] bytesArr = null;
	String filePathFromDB = "";
	String inputFileName = "";
	String outputFileName = "";
	String filePath = "";
	String hBusinessDate = "";
	String newLine="\r\n";

    public int mainProcess(String[] args) {
	    try {
	    	recordAmount = 0;
	    	trailerAmount = 0;
	    	recordCnt = 0;
	    	trailerRecordCnt = 0;
	    	recordTransCnt = 0;
	    	trailerTransCnt = 0;
		    hBillUnit = "CH";
		    hBillType = "CHUP";
		    hFileType = "PUCARDFL";
	    	
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
            
            for (int argi = 0; argi < args.length; argi++) {
				if (args[argi].equals("debug")) {
					super.debug = true;
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
        sqlCmd += " and file_name like 'PUCARD_IN.%' ";
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
                //showLogMessage("I", "", "開始首筆尾筆檢核....");
                //rc = checkHeaderTailer();
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
            
            //debug
            /*
            if(temstr.length() < 200 ) {
            	showLogMessage("I", "", String.format("讀入資料: %s", temstr));
            }
            */
            
			if(temstr.trim().length() > 5 ) {
				
				//showLogMessage("I", "", String.format("讀入資料: %s", temstr));
				
				onusInData = new BilE200InData();
				getDataD(onusInData, bytesArr);
				insertBilOnusbillExchange(onusInData, inputFileName, fctlNo);
				onusInData = null;
				recordAmount += comcr.str2int( CommTxBill.subByteToStr(bytesArr, 16, 25) );
				recordCnt ++;
				recordTransCnt++;
//				switch(CommTxBill.subByteToStr(bytesArr, 0, 1)) {	
//				case "1":
//					
//					if( ! checkHeader(bytesArr, inputFileName))
//						return -1;
//					break;
//				case "3":
//					// TRAILER
//					if(CommTxBill.subByteToStr(bytesArr, 0, 1).equalsIgnoreCase("3")) {
//						
//						trailerTransCnt = comcr.str2int( CommTxBill.subByteToStr(bytesArr, 1, 9) );
//						trailerRecordCnt = comcr.str2int( CommTxBill.subByteToStr(bytesArr, 1, 9) );
//						trailerAmount =comcr.str2int( CommTxBill.subByteToStr(bytesArr, 9, 17) );
//					}
//					break;
//				default: 
//					// =================default begin=======================================
//					recordAmount += comcr.str2int( CommTxBill.subByteToStr(bytesArr, 38, 44) );
//					recordCnt ++;
//					recordTransCnt++;
//					break;
					// =================default begin=======================================
//				}
			}
			
			if (endFile[inputFileD].equals("Y")) 
            	break;

		}	
        closeInputText(intc);
        return 0;
    }

//    protected int checkHeaderTailer() throws Exception {
//		// check whether the amount and the counts of the data in the D and A are the same.
//		if( (recordCnt != null && trailerRecordCnt != null && recordAmount != null 
//				&& trailerAmount != null && recordTransCnt != null && trailerTransCnt != null) 
//				&& (recordCnt.intValue() != (trailerRecordCnt.intValue())  || recordAmount.compareTo(trailerAmount) != 0 
//				|| recordTransCnt.intValue() != (trailerTransCnt.intValue())) ) {
//			showLogMessage("E", "", "首筆尾筆檢核資料不符");
//			showLogMessage("E", "", "recordAmount: " + recordAmount +  "recordCnt: " + recordCnt + "recordTransCnt: " + recordTransCnt);
//			showLogMessage("E", "", "trailerAmount: " + trailerAmount +"trailerRecordCnt: " + trailerRecordCnt + "trailerTransCnt: " + trailerTransCnt);
//			return -1;
//		}
//		showLogMessage("I", "", "首筆尾筆檢核資料相符!");
//		showLogMessage("I", "", "recordAmount: " + recordAmount +  "recordCnt: " + recordCnt + "recordTransCnt: " + recordTransCnt);
//		showLogMessage("I", "", "trailerAmount: " + trailerAmount +"trailerRecordCnt: " + trailerRecordCnt + "trailerTransCnt: " + trailerTransCnt);
//		return 0;
//    };

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
		outputFileName = String.format("PUCARD_OUT.TXT");
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
		outStr = new StringBuffer();
		outStr.append(comc.fixLeft(getValue("outboe.row_data"), 120));
		if ("06".equals(getValue("outboe.status_flag"))) {
			if ("A".equals(getValue("outboe.post_flag")) || 
				"E".equals(getValue("outboe.post_flag"))	) {
				outStr.replace(61, 63, rpad("20",2));  //卡片控管碼不符電金部所訂定之條件
				outStr.replace(63, 64, rpad(getValue("outboe.post_flag"),1));  //借用欄位
			} else {
				outStr.replace(61, 63, rpad("22",2));  //信用卡其他問題
			}
		} else if ("05".equals(getValue("outboe.status_flag"))) {
			outStr.replace(61, 63, rpad("19",2));  //超過卡片有效日期
		} else if ("02".equals(getValue("outboe.status_flag")) ||    //未開卡
				   "04".equals(getValue("outboe.status_flag")) ||    //卡號錯誤或卡號不存在
				   "09".equals(getValue("outboe.status_flag")) ) {   //持卡人ID與卡號不為同一人
			outStr.replace(61, 63, rpad("22",2));  //信用卡其他問題
		} else if ("08".equals(getValue("outboe.status_flag"))) {
			outStr.replace(61, 63, rpad("18",2));  //目前可用額度不足
		} else if ("10".equals(getValue("outboe.status_flag"))) {
			outStr.replace(61, 63, rpad("22",2));  //授權系統問題(96)
		} else {
			outStr.replace(61, 63, rpad(getValue("outboe.status_flag"),2));
		}
		outData += comc.getSubString(outStr.substring(0), 0,120);
		writeTextFile(fo, outData+newLine);

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
//	 private boolean checkHeader(byte[] bytesArr, String inputFileName) {
//		// check whether the record indicator equals to '1'
//			if(! CommTxBill.subByteToStr(bytesArr, 0, 1).equalsIgnoreCase("1") ) {
//				showLogMessage("E", "", "HEADER的RECORD 識別碼錯誤");
//				return false;
//			}
//			
//			// 檔案產出類型是否與fileName的中間名稱一致
//			/*
//			if(! CommTxBill.subByteToStr(bytesArr, 1, 9).equalsIgnoreCase( inputFileName.substring(3, 11) ) ) {
//				showLogMessage("E", "", "檔案產出類型與fileName的中間名稱不一致");
//				return false;
//			}
//			*/
//			
//		return true;
//	}
	 
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
				throw new Exception("fail to update bilOnusbillExchange!!");
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
			throw new Exception("fail to update EcsFtpLog.proc_code");
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
		setValue("PURCHASE_DATE", onusInData.purchaseDate);
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
		setValue("STATUS_FLAG", "99");
		setValue("POST_DATE", "");
		setValue("POST_FLAG", "");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		setValue("REC_TYPE",onusInData.recType);
		setValue("ROW_DATA",onusInData.rowData);
		setValue("MCHT_CATEGORY",onusInData.mchtCategory);
		setValue("MCHT_CHI_NAME",onusInData.mchtChiName);
		
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
		onusInData.recType = "2";
		onusInData.cardNo = CommTxBill.subByteToStr(bytesArr, 0,16);  // 卡號
		onusInData.destinationAmount = CommTxBill.subByteToStr(bytesArr, 16,25);  // 金額
		onusInData.statusFlag = CommTxBill.subByteToStr(bytesArr, 61,63);  // 狀況別
		onusInData.rowData = new String(bytesArr, "MS950"); //整筆資料
		onusInData.mchtCategory = "7278";
		
		/*中文名稱依不同項目組合
		String ss = CommTxBill.subByteToStr(bytesArr, 25,27);
	    String[] cde = new String[] {"01", "02", "03", "04", "05", "11"};
	    String[] txt = new String[] {"水費", "台電", "中華電信", "瓦斯費", "健保費", "媒體交換"};
		onusInData.mchtChiName = decode(ss, cde, txt);//中文名稱
		*/
		
		String ss = CommTxBill.subByteToStr(bytesArr, 99,100);
		String[] cde = new String[] {"D", "C"};
		String[] txt = new String[] {"05", "25"};
		onusInData.txnCode = decode(ss, cde, txt);//交易碼 
		onusInData.idNo = CommTxBill.subByteToStr(bytesArr, 43,53).trim();  //有可能是統編或ID
		onusInData.expireYYMM = readCardData("substring(NEW_END_DATE,3,4)",onusInData.cardNo);
		
		//重組特店中文名稱
		String payItem = CommTxBill.subByteToStr(bytesArr, 25,27);    //代繳項目
		String itemBId = CommTxBill.subByteToStr(bytesArr, 27,29);    //代繳代號(代繳項目=11時才有)
		String itemClass = CommTxBill.subByteToStr(bytesArr, 96,99);  //媒體交換代收類別
		String userNo = CommTxBill.subByteToStr(bytesArr, 29,43).trim();     //用戶號碼
		
		String corpNo = CommTxBill.subByteToStr(bytesArr, 100,110).trim();     //媒體交換發動者統編
		
		if ("05".equals(onusInData.txnCode)) {

			if ("11".equals(payItem) && ("81".equals(itemBId) || "581".equals(itemClass))) {
				onusInData.mchtChiName = "中華話費" + comc.getSubString(userNo, 4, 9) + "***"
						+ comc.getSubString(userNo, 12, 14);
				onusInData.extraInfo = "AT" + userNo;
			} else if ("11".equals(payItem) && ("82".equals(itemBId) || "582".equals(itemClass))) {
				onusInData.mchtChiName = "市水水費" + userNo;
				onusInData.extraInfo = "AW" + userNo;
			} else if ("11".equals(payItem) && ("83".equals(itemBId) || "583".equals(itemClass))) {
				onusInData.mchtChiName = "台水水費" + userNo;
				onusInData.extraInfo = "AC" + userNo;
			} else if ("11".equals(payItem) && ("84".equals(itemBId) || "584".equals(itemClass))) {
				onusInData.mchtChiName = "台電電費" + userNo;
				onusInData.extraInfo = "AE" + userNo;
			} else if ("11".equals(payItem) && ("85".equals(itemBId) || "585".equals(itemClass))) {
				onusInData.mchtChiName = "瓦斯費" + userNo;
				onusInData.extraInfo = "AB" + userNo;
			} else if ("11".equals(payItem) && ("76".equals(itemBId) || "576".equals(itemClass))) {
				onusInData.mchtChiName = "停車費" + userNo;
				onusInData.extraInfo = "AR" + userNo;
			} else if ("11".equals(payItem)) {
				onusInData.mchtChiName = "ＡＣＨ" + userNo;
				onusInData.extraInfo = "AH" + userNo;
			} else if ("04".equals(payItem) && "91".equals(itemBId)) {
				onusInData.mchtChiName = "新竹縣瓦斯費" + comc.getSubString(userNo, 4);
				onusInData.extraInfo = "UH" + userNo;
			} else if ("04".equals(payItem) && "04".equals(itemBId)) {
				onusInData.mchtChiName = "大台北瓦斯費" + comc.getSubString(userNo, 4);
				onusInData.extraInfo = "UD" + userNo;
			} else if ("01".equals(payItem)) {
				onusInData.mchtChiName = "水費" + userNo;
				onusInData.extraInfo = "UW" + userNo;
			} else if ("02".equals(payItem)) {
				onusInData.mchtChiName = "電費" + userNo;
				onusInData.extraInfo = "UE" + userNo;
			} else if ("03".equals(payItem)) {
				onusInData.mchtChiName = "中華電信費" + comc.getSubString(onusInData.idNo, 0, 5) + "***"
						+ comc.getSubString(onusInData.idNo, 8);
				onusInData.extraInfo = "UT" + userNo;
				onusInData.idNo = "";  //中華電信沒有帶入id
			} else if ("04".equals(payItem)) {
				onusInData.mchtChiName = "瓦斯費" + comc.getSubString(userNo, 4);
				onusInData.extraInfo = "UB" + userNo;
			} else if ("05".equals(payItem)) {
				onusInData.mchtChiName = "健保費" + comc.getSubString(userNo, 0, 4) + "***"
						+ comc.getSubString(userNo, 7, 10) + comc.getSubString(corpNo, 5);
				onusInData.extraInfo = "AI" + userNo;
			}
		} else {
			//退費
			if ("11".equals(payItem) && ("57".equals(itemBId) || "457".equals(itemClass))) {
				onusInData.mchtChiName = "停車退費" + userNo;
				onusInData.extraInfo = "AR" + userNo;
			} else if ("11".equals(payItem) && ("82".equals(itemBId) || "382".equals(itemClass))) {
				onusInData.mchtChiName = "瓦斯費退費" + userNo;
				onusInData.extraInfo = "AB" + userNo;
			} else if ("11".equals(payItem) && ("81".equals(itemBId) || "381".equals(itemClass))) {
				onusInData.mchtChiName = "發票中獎" + userNo;
				onusInData.extraInfo = "AC" + userNo;
			}
		}

		//停車退費要取消費日期mmdd
		if ("11".equals(payItem) && ("57".equals(itemBId) || "457".equals(itemClass))) {
			String purchaseMMDD = CommTxBill.subByteToStr(bytesArr, 120,124).trim();
			if ("0000".equals(purchaseMMDD)==false && "".equals(purchaseMMDD)==false) {
				onusInData.purchaseDate = comc.getSubString(hBusinessDate,0,4) + purchaseMMDD;
				//如果跨年,年要減1
				if (onusInData.purchaseDate.compareTo(hBusinessDate) > 0) {
					onusInData.purchaseDate = String.format("%d%s", (comc.str2int(comc.getSubString(hBusinessDate,0,4)) -1),purchaseMMDD);
				}
			} else {
				onusInData.purchaseDate = hBusinessDate;
			}
		} else {
			onusInData.purchaseDate = hBusinessDate;
		}
		
		onusInData.authCode = "";
		
	}
	
	/**
	 * return fctl_no, if this method cannot find fctl_no, then return ""
	 * 
	 * @param inputFileName
	 * @return
	 * @throws Exceptio
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
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/bil", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "BilE210"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "put " + isFileName + " 開始傳送....");
		int err_code = commFTP.ftplogName("NCR2TCB", "put " + isFileName);

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
	
	public String decode(String s1, String[] id1, String[] txt1) {
		if (s1 == null || s1.trim().length() == 0)
			return "";

		int ii = Arrays.asList(id1).indexOf(s1.trim());
		if (ii >= 0 && ii < txt1.length) {
			return txt1[ii];
		}

		return s1;
	}
	
	String readCardData(String ss,String cardNo) throws Exception{
		selectSQL = ss + " as str1 ";
		daoTable = " crd_card ";
		whereStr = " where 1=1 "
				+ " and card_no = ? ";
		setString(1, cardNo);

		if (selectTable() <= 0) {
			return "";
		} else {
			return getValue("str1");
		}
	}

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilE210 proc = new BilE210();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}



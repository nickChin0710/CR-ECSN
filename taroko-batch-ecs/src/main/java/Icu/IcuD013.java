/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/06/05  V1.00.00    JustinWu  initial                                   *
 *  109/06/29  V1.00.01    JustinWu  ACT_ACCT的acct_jrnl_bal -> CCA_CARD_ACCT的jrnl_bal 
 *  109/07/03  V1.00.02    JustinWu  change to read multiple files*
 *  109/07/03  V1.01.03    yanghan   修改了變量名稱和方法名稱            *                                                        *
 *  109/07/03  V1.01.04    JustinWu  change the program name*
 *  109/07/10  V1.01.05    JustinWu  keeping process next line of the file when error occurs
 *  109/07/13  V1.01.06    JustinWu  ++ 處理日期、總筆數、錯誤筆數
 *  109/07/14  V1.01.07    JustinWu  first check the status of data and then update or insert data
 *  109/07/15  V1.01.08    JustinWu  錯誤報表副檔名txt->TXT, insertCcaCardAcct add jrn_bal and tot_amt_consume, and 檔名的yy改為民國年後兩碼
 *  109-07-22              yanghan   修改了字段名称            *
 *  109/08/14  V1.01.09    Wilson    資料夾名稱修改為小寫                                                                              *
 *  109/08/28  V1.01.10    Wilson    讀檔規則修改                                                                                             *
 *  109-09-04  V1.01.11    yanghan   解决Portability Flaw: Locale Dependent Comparison问题    * 
 *  109/09/04  V1.01.12    Zuwei     code scan issue   
 *  109/09/16  V1.01.13   JustinWu   modify the way to get the two String of corpTel*
 *  109/09/28  V1.01.14   Wilson     讀檔不綁檔名日期                                                           *
 *  109/09/30  V1.01.15   Wilson     無檔案秀error                               *
 *  109/10/06  V1.01.16   Wilson     讀檔要綁檔名日期                                                            *
 *  109/10/12  V1.01.17   Wilson     檔名日期改營業日                                                           *
 *  109/10/16  V1.01.18   Wilson     錯誤報表FTP                                 *
 *  109-10-19  V1.00.19   shiyuqi       updated for project coding standard    *
 *  109/12/25  V1.01.20   Wilson     無檔案正常結束                                                               *
 *  110/08/20  V1.01.21   SunnyTs    將mainProcess private改 public              *
 *  110/11/25  V1.01.22   Justin     ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX               *
 *  110/12/02  V1.01.23   Justin     add commit and rollback when writing error reports *
 *  110/12/07  V1.01.24   Wilson     新增insert cca_consume                     *
 *  110/12/08  V1.01.25   Justin     add check and insert crd_file_ctl, and add sysDate to the backup file *
 *  111/01/21  V1.01.26   Justin     若異動碼為C但UPDATE CRD_CORP找不到資料時，改成走跟異動碼為A一樣的邏輯 *
 *  111/01/24  V1.01.27   Justin     修改錯誤訊息                            *
 *  111/01/26  V1.01.28   Justin     修改C找不到主檔改執行A問題              *
 *  111/02/14  V1.01.29   Justin     sort files by their modified dates      *
 *  111/02/15  V1.01.30   Justin     調整engName, chiName, chargeName切byte長度*
 *  111/02/15  V1.01.31    Ryan      big5 to MS950                                           *
 *  111/02/17  V1.01.32  Justin       fix the bug of error files             *   
 *  111/02/18  V1.01.33  Justin      調整擷取電話號碼問題                    *
 *  111/03/02  V1.01.34  Justin      刪除Err字新增處理訊息                   *
 *  111/03/03  V1.01.35  Justin      mark unnecessary code                   *
 *  111/04/07  V1.01.36  Wilson      額度異動要更新line_of_credit_amt             *   
 *  112/03/15  V1.01.37  Wilson      insert act_acno add card_indicator        *                               
 ******************************************************************************/
package Icu;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;


public class IcuD013 extends AccessDAO {
	private final String progname = "CARDLINK信用卡企業資料處理程式 112/03/15  V1.01.37";
	private String prgmId = "IcuD013";

    String queryDate = "";
    String hBusiBusinessDate = "";
    String hPrevBusiBusinessDate = "";
    String outputFileName = "";
   
	private final byte emptyByte = " ".getBytes()[0];
	private final byte[] lineSeparatorBytes = System.lineSeparator().getBytes();

	CommCrdRoutine comcr = null;
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommTxBill commTxBill;
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommFunction commFunc = new CommFunction();

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
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commTxBill = new CommTxBill(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
//			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			
			selectPtrBusinday();
			
			// 若沒有給定查詢日期，則查詢日期為系統日
            if(args.length == 0) {
//                queryDate = hBusiBusinessDate;
            	queryDate = "";
            }else
            if(args.length == 1) {
                // 檢查參數(查詢日期)
                if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
                    showLogMessage("E", "", String.format("日期格式[%s]錯誤，日期格式應為西元年yyyyMMdd", args[0]));
                    return -1;
                }
                queryDate = args[0];
            }else {
                comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
            }   
			
			// ====================================

			String text;
			final String filePathFromDb = "media/icu";
			final String bankNo = "M00600000";
			final String fileTypeName = "ICBUSQND";

			// 若查詢日(queryDate)為西元年2020年07月03日，則fileNameTemplate = M00600000.ICBUSQND.090703nn.txt，
			// 其中fileName的兩碼年份為民國年後兩碼，因此西元2020年->民國109年->09；nn為編號
//			final String fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*",
//					bankNo, fileTypeName, new CommDate().getLastTwoTWDate(queryDate), queryDate.substring(4, 8) ); // 檔案正規表達式


			/////////////////////////
			String fileNameTemplate  = ""; // String fileNameTemplate
			String fileNameTemplate2 = ""; // previous business date

			if (queryDate.length() > 0) {
				fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
						bankNo, 
						fileTypeName
						,new CommDate().getLastTwoTWDate(queryDate), 
						queryDate.substring(4, 8)); // 檔案正規表達式
				
				showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate));
			}else {
				fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
						bankNo, 
						fileTypeName
						,new CommDate().getLastTwoTWDate(hBusiBusinessDate), 
						hBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
				
				fileNameTemplate2 = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
						bankNo, 
						fileTypeName,
						new CommDate().getLastTwoTWDate(hPrevBusiBusinessDate), 
						hPrevBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
				
				showLogMessage("I", "", String.format("尋找檔案[%s] 或", fileNameTemplate));
				showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate2));
			}
			
			/////////////////////////
			
			// get the fileFolderPath such as C:\EcsWeb\media\icu
			String fileFolderPath = getFileFolderPath(comc.getECSHOME(), filePathFromDb);

			File[] fileArr = getAllTodayFiles(fileFolderPath, fileNameTemplate, fileNameTemplate2);

			// ===== for loop start====================
			
			int totalRecord = 0;
			
			ErrorFile errorFile = new ErrorFile(sysDate);
			
			if (fileArr != null)
			for (File file : fileArr) {
				
				String fileName = file.getName(); // M00600000.ICBUSQND.09070301.txt
				
				// 2021/12/08 Justin
				if (isFileCtlProcess(fileName)) {
					continue;
				}

				// open file
				int inputFileIndex = openInputText(file.getAbsolutePath(), "MS950");
				if (inputFileIndex == -1) {
					showLogMessage("E", "", String.format("檔案不存在: %s", fileName));
					return -1;
				}

				// =========while start====================
				while (true) {

					text = readTextFile(inputFileIndex);

					if (text.trim().length() != 0) {
						
						totalRecord++;

						IcuD013Data icuD013Data = getTxt(text);

						showLogMessage("D", "", "==========");
						showLogMessage("D", "", String.format("異動碼[%s],corpNo[%s]", icuD013Data.changeCode, icuD013Data.corpNo));
						switch (icuD013Data.changeCode.toUpperCase(Locale.TAIWAN)) {
						// 若“異動碼”為“C”就update crd_corp、act_acno、act_acct、cca_card_acct
						case "C":			

							if ( ! doesCrdCorpPK1Exist(icuD013Data.corpNo) ) {
								showLogMessage("I", "", "corp_no = [" + icuD013Data.corpNo + "]，異動碼為'C',但主檔無資料，改以新增處理");
								boolean result = doInsert(icuD013Data, errorFile);
								if (result == false) {
									rollbackDataBase();
									continue;
								}
								break;
							}

//							if ( ! doesActAcnoExist(icuD013Data.corpNo) ) {
//								errorFile.setErrorReason(icuD013Data.corpNo, icuD013Data.changeCode, "異動碼為'C'，但企業帳戶主檔無資料(1)");
//								continue;
//							}
							
							// select act_acno
							ActAcnoData actAcnoData = selectActAcno(icuD013Data.corpNo);

							if (actAcnoData == null) {
								errorFile.setErrorReason(icuD013Data.corpNo, icuD013Data.changeCode, "異動碼為'C'，但企業帳戶主檔無資料(2)");
								continue;
							}
							
//							if ( ! doesCcaCardAcctExist(actAcnoData.acnoPSeqno) ) {
//								errorFile.setErrorReason(icuD013Data.corpNo, icuD013Data.changeCode, "異動碼為'C'，但企業帳戶主檔無資料(1)");
//								continue;
//							}
							
							// update crd_corp
							int updateCnt = updateCrdCorp(icuD013Data);
							// 若異動碼為C但UPDATE CRD_CORP找不到資料時，改成走跟異動碼為A一樣的邏輯
							if (updateCnt == 0) {
								showLogMessage("I", "", "corp_no = [" + icuD013Data.corpNo + "]，異動碼為'C',但主檔無資料，改以新增處理");
								boolean result = doInsert(icuD013Data, errorFile);
								if (result == false) {
									rollbackDataBase();
									continue;
								}
								break;
							}
						
							// 2022/03/03 Justin
							// 2022/04/07 Wilson(額度異動要更新)
							// update act_acno
							updateActAcno(icuD013Data.corpNo, icuD013Data.lineOfCreditAmt);

//							// update cca_card_acct
//							updateCcaCardAcct(actAcnoData.acnoPSeqno, icuD013Data.corpNotReimburseAmt,
//									icuD013Data.corpRestAmtSign, icuD013Data.corpRestAmt);

							break;

						// 若“異動碼”為“A”就insert crd_corp、act_acno、act_acct、 cca_card_acct
						case "A":
							boolean result = doInsert(icuD013Data, errorFile);
							if (result == false) {
								rollbackDataBase();
								continue;
							}
							break;
						//
						}
						// end switch
					}
					
					commitDataBase();

					if (endFile[inputFileIndex].equals("Y"))
						break; // break while loop

				}
				// ========while end=============================
				
				closeInputText(inputFileIndex);
				
				insertFileCtl(fileName);
				
				moveFileToBackup(fileFolderPath, fileName);

			}
			// ===== for loop end====================
			
			commitDataBase();
			
			if (errorFile.isError) {
				produceErrorFile(errorFile, fileFolderPath, fileTypeName);
				insertFileCtl(outputFileName);		
			    procFTP();
			    renameFile1(outputFileName);
			} 

			if (fileArr == null || fileArr.length == 0) {
				
				showLogMessage("I", "", "無檔案可處理，處理日期  = " + queryDate);
//			    return -1;
			}				

			showLogMessage("I", "",String.format("處理日期：%s，　總筆數：%s，　錯誤筆數：%s", 
					queryDate, totalRecord, errorFile.isError ? errorFile.errorReasonArr.size() : 0));
				
			showLogMessage("I", "", "執行結束");
			comcr.hCallErrorDesc = "程式執行結束";
			comcr.callbatchEnd();
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}
	
	
	boolean doInsert(IcuD013Data icuD013Data, ErrorFile errorFile ) throws Exception {
		// 取得corp_p_seqno
		String corpPSeqno = getCorpPSeqno();

		if (corpPSeqno == null) {
			errorFile.setErrorReason(icuD013Data.corpNo, icuD013Data.changeCode, "無法取得統編流水號[corp_p_seqno]");
			return false;
		}
		
		if (doesCrdCorpPK1Exist(icuD013Data.corpNo) || doesCrdCorpPK2Exist(corpPSeqno)) {
			errorFile.setErrorReason(icuD013Data.corpNo, icuD013Data.changeCode, "異動碼為'A'，但企業主檔已存在");
			return false;
		}
		
		// 取得acno_p_seqno
		String acnoPSeqno = getAcnoPSeqno();

		if (acnoPSeqno == null) {
			errorFile.setErrorReason(icuD013Data.corpNo, icuD013Data.changeCode, "無法取得帳戶流水號[acno_p_seqno]");
			return false;
		}
		
		if (doesActAcnoPK1Exist(icuD013Data.corpNo, corpPSeqno) || doesActAcnoPK2Exist(acnoPSeqno)) {
			errorFile.setErrorReason(icuD013Data.corpNo, icuD013Data.changeCode, "異動碼為'A'，但企業帳戶主檔已存在");
			return false;
		}
		
		// 取得card_acct_idx
		// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//		String cardAcctIdx = getCardAcctIdx();
		String cardAcctIdx = Integer.toString(Integer.parseInt(acnoPSeqno));		

//		if (cardAcctIdx == null) {
//			errorFile.setErrorReason(icuD013Data.corpNo, icuD013Data.changeCode,
//					"無法取得授權帳戶流水號[card_acct_idx]");
//			continue;
//		}
		
		if (doesCcaCardAcctPKExist(cardAcctIdx)) {
			errorFile.setErrorReason(icuD013Data.corpNo, icuD013Data.changeCode, "異動碼為'A'，但企業授權帳戶主檔已存在");
			return false;
		}

		// insert into crd_corp
		insertCrdCorp(corpPSeqno, icuD013Data);
		
		// insert into act_acno
		insertActAcno(acnoPSeqno, corpPSeqno, icuD013Data);

		// insert into cca_card_acct
		insertCcaCardAcct(acnoPSeqno, corpPSeqno, cardAcctIdx,
				icuD013Data.corpRestAmtSign, icuD013Data.corpRestAmt, icuD013Data.corpNotReimburseAmt);
		
		// insert into cca_consume
		insertCcaConsume(acnoPSeqno, cardAcctIdx);
		
		return true;
	}
	
	/***********************************************************************/
	boolean isFileCtlProcess(String fileName) throws Exception {
		int totalCount = 0;

		sqlCmd = "select count(*) totalCount ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
		setString(1, fileName);

		int recordCnt = selectTable();

		if (recordCnt > 0)
			totalCount = getValueInt("totalCount");

		if (totalCount > 0) {
            showLogMessage("I", "", String.format("此檔案 = [" + fileName + "]已處理過不可重複處理(crd_file_ctl)"));
			return (true);
		}
		return (false);
	}
	
	/***********************************************************************/
	void insertFileCtl(String fileName) throws Exception {
		setValue("file_name", fileName);
		setValue("crt_date", sysDate);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name = ? ";
			setString(1, fileName);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", "");
			}
		}
	}
	
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";
		hPrevBusiBusinessDate = "";

		sqlCmd = " select business_date , to_char( to_date(business_date, 'yyyymmdd') - 1 DAYS , 'yyyymmdd') as prev_business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		hBusiBusinessDate = getValue("business_date");
		hPrevBusiBusinessDate = getValue("prev_business_date");
	}

	private boolean doesCcaCardAcctPKExist(String cardAcctIdx) throws Exception {
		selectSQL = " card_acct_idx ";
		daoTable = "cca_card_acct";
		whereStr = " where card_acct_idx = ?";
		setString(1, cardAcctIdx);

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return false;

		return true;
	}
	
	private boolean doesCcaCardAcctExist(String acnoPSeqno) throws Exception {
		selectSQL = " acno_p_seqno ";
		daoTable = "cca_card_acct";
		whereStr = " where acno_p_seqno = ?";
		setString(1, acnoPSeqno);

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return false;

		return true;
	}
	
	private boolean doesActAcnoExist(String corpNo) throws Exception {
		selectSQL = " acct_key ";
		daoTable = "act_acno";
		whereStr = " where acct_key = ? ";
		setString(1, corpNo);

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return false;

		return true;
	}

	private boolean doesActAcnoPK1Exist(String corpNo, String corpPSeqno) throws Exception {
		selectSQL = " acct_key, corp_p_seqno ";
		daoTable = "act_acno";
		whereStr = " where acct_key = ? "
				           + " and acct_type = ? "
				           + " and corp_p_seqno = ? ";
		setString(1, corpNo);
		setString(2, "");
		setString(3, corpPSeqno);

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return false;

		return true;
	}
	
	private boolean doesActAcnoPK2Exist(String acnoPSeqno) throws Exception {
		selectSQL = " acno_p_seqno ";
		daoTable = "act_acno";
		whereStr = " where acno_p_seqno = ? ";
		setString(1, acnoPSeqno);

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return false;

		return true;
	}

	private boolean doesCrdCorpPK1Exist(String corpNo) throws Exception {
		selectSQL = " corp_no ";
		daoTable = "crd_corp";
		whereStr = " where 1=1 " 
		                   + " and corp_no = ? ";
		setString(1, corpNo);

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return false;

		return true;
	}
	
	private boolean doesCrdCorpPK2Exist(String corpPSeqno) throws Exception {
		selectSQL = " corp_no ";
		daoTable = "crd_corp";
		whereStr = " where 1=1 " 
		                   + " and corp_p_seqno = ? ";
		setString(1, corpPSeqno);

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return false;

		return true;
	}

	/**
	 * 
	 * @param corpNo
	 * @return the object of ActAcnoData if there are selected records; null if
	 *         there is no selected record.
	 * @throws Exception
	 */
	private ActAcnoData selectActAcno(String corpNo) throws Exception {
		selectSQL = " acno_p_seqno, p_seqno ";
		daoTable = "act_acno";
		whereStr = " where 1=1 " + " and acct_key = ? ";

		setString(1, corpNo);

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return null;

		ActAcnoData actAcnoData = new ActAcnoData();
		actAcnoData.acnoPSeqno = getValue("acno_p_seqno");
		actAcnoData.pSeqno = getValue("p_seqno");

		return actAcnoData;

	}

	/**
	 * update crd_corp and then return the counts of affected records
	 * 
	 * @param icuD013Data
	 * @return
	 * @throws Exception
	 */
	private int updateCrdCorp(IcuD013Data icuD013Data) throws Exception {

		String[] telZoneAndNo = getTelZoneAndNo(icuD013Data.corpTel);

		daoTable = "crd_corp";

		updateSQL = " eng_name = ? , " + " emboss_data = ?, " + " card_since = ?, " + " corp_tel_zone1 = ?, "
				+ " corp_tel_no1 = ?, " + " chi_name = ?, " + " charge_name = ?, " + " charge_id = ?, "
				+ " mod_pgm = ?, " + " mod_time =  TIMESTAMP_FORMAT(?,'YYYYMMDDHH24MISS') ";

		int i = 1;
		setString(i++, icuD013Data.engName);
		setString(i++, icuD013Data.embossData);
		setString(i++, icuD013Data.cardSince);
		setString(i++, telZoneAndNo[0]);
		setString(i++, telZoneAndNo[1]);
		setString(i++, icuD013Data.chiName);
		setString(i++, icuD013Data.chargeName);
		setString(i++, icuD013Data.chargeId);
		setString(i++, prgmId);
		setString(i++, sysDate + sysTime);

		whereStr = " where corp_no = ?";
		setString(i++, icuD013Data.corpNo);

		int updateCnt = updateTable();

		return updateCnt;

	}

	/**
	 * 
	 * @param corpNo
	 * @param lineOfCreditAmt
	 * @return
	 * @throws Exception
	 */
	private int updateActAcno(String corpNo, String lineOfCreditAmt) throws Exception {

		daoTable = "act_acno";

		updateSQL = " line_of_credit_amt = ? , " + " mod_pgm = ?, "
				+ " mod_time = TIMESTAMP_FORMAT(?,'YYYYMMDDHH24MISS') ";

		int i = 1;
		setString(i++, lineOfCreditAmt);
		setString(i++, prgmId);
		setString(i++, sysDate + sysTime);

		whereStr = " where acct_key = ?";
		setString(i++, corpNo);

		int updateCnt = updateTable();

		return updateCnt;
	}


	private int updateCcaCardAcct(String acnoPSeqno, String corpNotReimburseAmt, String corpRestAmtSign,
			String corpRestAmt) throws Exception {

		daoTable = "cca_card_acct";

		updateSQL = " jrnl_bal = ? , " + " tot_amt_consume = ? , " + " mod_pgm = ?, "
				+ " mod_time = TIMESTAMP_FORMAT(?,'YYYYMMDDHH24MISS') ";

		int i = 1;
		setDouble(i++, getAcctJrnlBalSign(corpRestAmtSign) * new BigDecimal(corpRestAmt).doubleValue()); // 2020-06-29
																											// JustinWu
		setDouble(i++, new BigDecimal(corpNotReimburseAmt).doubleValue());
		setString(i++, prgmId);
		setString(i++, sysDate + sysTime);

		whereStr = " where acno_p_seqno = ?";
		setString(i++, acnoPSeqno);

		int updateCnt = updateTable();

		return updateCnt;
	}

	/**
	 * insert into crd_corp
	 * 
	 * @param icuD013Data
	 * @param corpPSeqno
	 * @return the value of 0 if fail to insert;
	 * @throws Exception
	 */
	private int insertCrdCorp(String corpPSeqno, IcuD013Data icuD013Data) throws Exception {

		String[] telZoneAndNo = getTelZoneAndNo(icuD013Data.corpTel);

		daoTable = "crd_corp";

		setValue("corp_p_seqno", corpPSeqno);
		setValue("corp_no", icuD013Data.corpNo);
		setValue("chi_name", icuD013Data.chiName);
		setValue("eng_name", icuD013Data.engName);
		setValue("corp_tel_zone1", telZoneAndNo[0]);
		setValue("corp_tel_no1", telZoneAndNo[1]);
		setValue("charge_id", icuD013Data.chargeId);
		setValue("charge_name", icuD013Data.chargeName);
		setValue("emboss_data", icuD013Data.embossData);
		setValue("card_since", icuD013Data.cardSince);
		setValue("crt_date", sysDate);
		setValue("crt_user", prgmId);
		setValue("apr_date", sysDate);
		setValue("apr_user", prgmId);
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);

		int returnCode = insertTable();

		return returnCode;

	}

	/**
	 * insert into act_acno
	 * 
	 * @param acnoPSeqno
	 * @param corpPSeqno
	 * @param icuD013Data
	 * @return the value of 0 if fail to insert;
	 * @throws Exception
	 */
	
	/**
	* @ClassName: IcuD013
	* @Description: insert act_acno add card_indicator
	* @Copyright : Copyright (c) DXC Corp. 2023. All Rights Reserved.
	* @Company: DXC Team.
	* @author Wilson
	* @version V1.01.37, Mar 15, 2023
	*/
	private int insertActAcno(String acnoPSeqno, String corpPSeqno, IcuD013Data icuD013Data) throws Exception {

		daoTable = "act_acno";

		setValue("acno_p_seqno", acnoPSeqno);
		setValue("p_seqno", acnoPSeqno);
		setValue("acct_key", icuD013Data.corpNo);
		setValue("acct_type", "");
		setValue("corp_p_seqno", corpPSeqno);
		setValue("corp_act_flag", "Y");
		setValue("acno_flag", "2");
		setValue("acct_status", "1");
		setValue("line_of_credit_amt", icuD013Data.lineOfCreditAmt);
		setValue("card_indicator", "2");
		setValue("apr_flag", "Y");
		setValue("apr_date", sysDate);
		setValue("apr_user", prgmId);
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("crt_user", prgmId);
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);

		int returnCode = insertTable();

		return returnCode;
	}


	/**
	 * insert into cca_card_acct
	 * 
	 * @param acnoPSeqno
	 * @param corpPSeqno
	 * @param cardAcctIdx
	 * @param corpRestAmt
	 * @param corpRestAmtSign
	 * @param corpNotReimburseAmt 
	 * @return the value of 0 if fail to insert;
	 * @throws Exception
	 */
	private int insertCcaCardAcct(String acnoPSeqno, String corpPSeqno, String cardAcctIdx, String corpRestAmtSign,
			String corpRestAmt, String corpNotReimburseAmt) throws Exception {

		daoTable = "cca_card_acct";

		setValue("acno_p_seqno", acnoPSeqno);
		setValue("p_seqno", acnoPSeqno);
		setValue("debit_flag", "N");
		setValue("acno_flag", "2");
		setValue("id_p_seqno", "");
		setValue("corp_p_seqno", corpPSeqno);
		setValue("card_acct_idx", cardAcctIdx);
		setValueDouble("jrnl_bal",
				getAcctJrnlBalSign(corpRestAmtSign) * new BigDecimal(corpRestAmt).doubleValue()); // 2020-06-29 JustinWu
		setValueDouble("tot_amt_consume", new BigDecimal(corpNotReimburseAmt).doubleValue()); // 2020-07-15 JustinWu
		setValue("crt_date", sysDate);
		setValue("crt_user", prgmId);
		setValue("apr_date", sysDate);
		setValue("apr_user", prgmId);
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);

		int returnCode = insertTable();

		return returnCode;
	}
	
	
	/**
	 * insert into cca_consume
	 * 
	 * @param acnoPSeqno
	 * @param cardAcctIdx
	 * @return the value of 0 if fail to insert;
	 * @throws Exception
	 */
	private int insertCcaConsume(String acnoPSeqno, String cardAcctIdx) throws Exception {

		daoTable = "cca_consume";

		setValue("card_acct_idx", cardAcctIdx);
		setValue("p_seqno", acnoPSeqno);
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);

		int returnCode = insertTable();

		return returnCode;
	}
	

	/**
	 * get file folder path by the project path and the file path selected from
	 * database
	 * 
	 * @param projectPath
	 * @param filePathFromDb
	 * @param fileNameAndTxt
	 * @return
	 * @throws Exception
	 */
	private String getFileFolderPath(String projectPath, String filePathFromDb) throws Exception {
		String fileFolderPath = null;

		projectPath = SecurityUtil.verifyPath(projectPath);
		if (filePathFromDb.isEmpty() || filePathFromDb == null) {
			throw new Exception("file path selected from database is error");
		}

		String[] arrFilePathFromDb = filePathFromDb.split("/");

		fileFolderPath = Paths.get(projectPath).toString();

		for (int i = 0; i < arrFilePathFromDb.length; i++)
			fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();

		return fileFolderPath;
	}

	/**
	 * 找出所有符合的字串
	 * 
	 * @param fileFolderPath
	 * @param fileNameTemplate
	 * @param fileNameTemplate2 
	 * @return
	 */
	private File[] getAllTodayFiles(String fileFolderPath, String fileNameTemplate, String fileNameTemplate2) {
		fileFolderPath = SecurityUtil.verifyPath(fileFolderPath);
		File file = new File(fileFolderPath);

		File[] files = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(fileNameTemplate) || ( fileNameTemplate2.length() > 0 && name.matches(fileNameTemplate2) );
			}
		});
		
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				long diff = f1.lastModified()-f2.lastModified();
				if(diff>0)
				  return 1;
				else if(diff==0)
		  		  return 0;
				else
				  return -1;
			}
		});

		return files;
	}

	private IcuD013Data getTxt(String text) throws UnsupportedEncodingException {
		byte[] bytesArr = text.getBytes("MS950");

		IcuD013Data d013Data = new IcuD013Data();

		d013Data.changeCode = CommTxBill.subByteToStr(bytesArr, 0, 1);
		d013Data.issueUnit = CommTxBill.subByteToStr(bytesArr, 1, 9);
		d013Data.corpNo = CommTxBill.subByteToStr(bytesArr, 9, 20);
//		d013Data.engName = CommTxBill.subByteToStr(bytesArr, 20, 50); // 資料30 bytes，但DB 25 vargraphic
		d013Data.engName = CommTxBill.subByteToStr(bytesArr, 20, 45);
		d013Data.lineOfCreditAmt = CommTxBill.subByteToStr(bytesArr, 50, 59);
		d013Data.embossData = CommTxBill.subByteToStr(bytesArr, 59, 78);
		d013Data.corpRestAmt = CommTxBill.subByteToStr(bytesArr, 78, 89);
		d013Data.corpRestAmtSign = CommTxBill.subByteToStr(bytesArr, 89, 90);
		d013Data.corpNotReimburseAmt = CommTxBill.subByteToStr(bytesArr, 90, 101);
		d013Data.cardSince = CommTxBill.subByteToStr(bytesArr, 101, 109);
		d013Data.stopUseCode = CommTxBill.subByteToStr(bytesArr, 109, 111);
		d013Data.stopUseCodeModifyingDate = CommTxBill.subByteToStr(bytesArr, 111, 119);
		d013Data.corpTel = CommTxBill.subByteToStr(bytesArr, 119, 131);
//		d013Data.chiName = CommTxBill.subByteToStr(bytesArr, 131, 211); // 資料80 bytes，但DB 50 vargraphic
		d013Data.chiName = CommTxBill.subByteToStr(bytesArr, 131, 181);
//		d013Data.chargeName = CommTxBill.subByteToStr(bytesArr, 211, 251); // 資料40 bytes，但DB 30 vargraphic
		d013Data.chargeName = CommTxBill.subByteToStr(bytesArr, 211, 241);
		d013Data.chargeId = CommTxBill.subByteToStr(bytesArr, 251, 262);
		d013Data.errorReturnCode = CommTxBill.subByteToStr(bytesArr, 262, 266);

		d013Data.corpRestAmt = commTxBill.getBigDecimalDividedBy100(d013Data.corpRestAmt).toString();
		d013Data.corpNotReimburseAmt = commTxBill.getBigDecimalDividedBy100(d013Data.corpNotReimburseAmt).toString();

		return d013Data;
	}

	/**
	 * 取得一個陣列，其中index0為區碼、index1為號碼
	 * 
	 * @param corpTel : 電話格式為 02-12345678->{02, 12345678} ; 089-123456->{089, 123456} ; 0212345678->{"",0212345678} ; 00212345678->{00, 212345678} ; ""->{"", ""}
	 * @return 一個陣列，其中index0為區碼、index1為號碼
	 */
	private String[] getTelZoneAndNo(String corpTel) {
		return commFunc.getTelZoneAndNo(corpTel);
	}

	/**
	 * 判斷字串，return正1或負1
	 * 
	 * @param icuD013Data
	 * @return acctJrnlBalSign
	 */
	private double getAcctJrnlBalSign(String corpRestAmtSign) {
		if (corpRestAmtSign.equalsIgnoreCase("C"))
			return -1.0;
		else
			return 1.0;
	}

	/**
	 * 取得corp_p_seqno
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getCorpPSeqno() throws Exception {
		selectSQL = " lpad(ecs_acno.nextval,10,'0') as corp_p_seqno ";
		daoTable = "dual";

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return null;

		return getValue("corp_p_seqno");
	}

	/**
	 * 取得acno_p_seqno
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getAcnoPSeqno() throws Exception {
		selectSQL = " lpad(ecs_acno.nextval,10,'0') as acno_p_seqno ";
		daoTable = "dual";

		int selectCnt = selectTable();

		if (selectCnt <= 0)
			return null;

		return getValue("acno_p_seqno");
	}

	// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//	/**
//	 * 取得card_acct_idx
//	 * 
//	 * @return
//	 * @throws Exception
//	 */
//	private String getCardAcctIdx() throws Exception {
//		selectSQL = " ecs_card_acct_idx.nextval as card_acct_idx ";
//		daoTable = "dual";
//
//		int selectCnt = selectTable();
//
//		if (selectCnt <= 0)
//			return null;
//
//		return getValue("card_acct_idx");
//	}

	private void produceErrorFile(ErrorFile errorFile, String inputFileFolderPath, String fileTypeName) throws Exception {

		inputFileFolderPath = SecurityUtil.verifyPath(inputFileFolderPath);
		// media/icu/error
		Path outputFileFolderPath = Paths.get(inputFileFolderPath, "error");

		// create the parent directory if parent the directory is not exist
		Files.createDirectories(outputFileFolderPath);

		// get output file name :M00600000.ICBUSQND.YYMMDDNN.TXT =>
		// ICBUSQND.ERR.YYYYMMDDNN
		
		int fileNo = 0;

        sqlCmd  = "select max(substr(file_name, 22, 2)) file_no";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += " where file_name like ?";
        sqlCmd += "  and crt_date  = ? ";
        setString(1, "ICBUSQND.ERR." + "%" + ".TXT");
        setString(2, sysDate);

		if (selectTable() > 0)
			fileNo = getValueInt("file_no");
		
		outputFileName = String.format("%s.ERR.%s%02d.TXT", fileTypeName, sysDate, fileNo + 1);
		outputFileName = SecurityUtil.verifyPath(outputFileName);

		// get output file path
		String outputFilePath = Paths.get(outputFileFolderPath.toString(), outputFileName).toString();

		int outFileIndex = openBinaryOutput2(outputFilePath);

		writeFile(errorFile, outFileIndex);

		showLogMessage("I", "", String.format("產出錯誤報表檔: %s", outputFilePath));

		closeBinaryOutput2(outFileIndex);

	}

	private void writeFile(ErrorFile errorFile, int outFileIndex) throws Exception, UnsupportedEncodingException {
		int size = errorFile.errorReasonArr.size();
		for (int i = 0; i < size; i++) {
			writeFileInCertainLength(outFileIndex, errorFile.changeCodeArr.get(i), 1);
			writeFileInCertainLength(outFileIndex, errorFile.corpNoArr.get(i), 11);
			writeFileInCertainLength(outFileIndex, errorFile.errorReasonArr.get(i), 200);
			writeFileInCertainLength(outFileIndex, errorFile.processDate, 8);
			writeFileInCertainLength(outFileIndex, System.lineSeparator(), lineSeparatorBytes.length);

		}

	}

	private void writeFileInCertainLength(int outFileIndex, String str, int targetLength) throws Exception {

		byte[] byteArr = str.getBytes("MS950");

		writeBinFile2(outFileIndex, byteArr, byteArr.length);

		int emptyLength = targetLength - byteArr.length;

		if (emptyLength == 0)
			return;

		byte[] emptyByteArr = new byte[emptyLength];
		for (int i = 0; i < emptyLength; i++) {
			emptyByteArr[i] = emptyByte;
		}

		writeBinFile2(outFileIndex, emptyByteArr, emptyLength);

	}

	private void moveFileToBackup(String fileFolderPath, String fileName) throws IOException {
		fileFolderPath = SecurityUtil.verifyPath(fileFolderPath);
		fileName = SecurityUtil.verifyPath(fileName);
		Path backupPath = Paths.get(fileFolderPath, "backup");

		// create the parent directory if parent the directory is not exist
		Files.createDirectories(backupPath);

		Path backupFilePath = Paths.get(backupPath.toString(), fileName + "." + sysDate);

		Files.move(Paths.get(fileFolderPath, fileName), backupFilePath, StandardCopyOption.REPLACE_EXISTING);

		showLogMessage("I", "", String.format("移動CARDLINK信用卡企業資料檔至 %s", backupFilePath.toString()));

	}
	
	 void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + outputFileName + " 開始傳送....");
	      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + outputFileName);
	      
	      if (err_code != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + outputFileName + " 資料"+" errcode:"+err_code);
	          insertEcsNotifyLog(outputFileName);          
	      }
	  }
	
	/****************************************************************************/
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

	/****************************************************************************/
	void renameFile1(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/error/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
		
		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	/****************************************************************************/	

	public static void main(String[] args) {
		IcuD013 proc = new IcuD013();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);

	}

}

class IcuD013Data {
	String changeCode;
	String issueUnit;
	String corpNo;
	String engName;
	String lineOfCreditAmt;
	String embossData;
	String corpRestAmt;
	String corpRestAmtSign;
	String corpNotReimburseAmt;
	String cardSince;
	String stopUseCode;
	String stopUseCodeModifyingDate;
	String corpTel;
	String chiName;
	String chargeId;
	String chargeName;
	String errorReturnCode;
}

class ErrorFile {
	boolean isError;
	ArrayList<String> changeCodeArr;
	ArrayList<String> corpNoArr;
	ArrayList<String> errorReasonArr;
	String processDate;
	
	public ErrorFile(String sysDate) {
		processDate  = sysDate;
		changeCodeArr = new ArrayList<String>();
		corpNoArr = new ArrayList<String>();
		errorReasonArr = new ArrayList<String>();
	}
	
	/**
	 * 設定錯誤原因
	 * 
	 * @param corpNo
	 * @param changeCode
	 * @param errorReason
	 */
	 void setErrorReason(String corpNo, String changeCode, String errorReason) {
		this.isError = true;
		this.errorReasonArr.add(errorReason);
		this.corpNoArr.add(corpNo);
		this.changeCodeArr.add(changeCode);
	}	 

}

class ActAcnoData {
	String acnoPSeqno;
	String pSeqno;
}
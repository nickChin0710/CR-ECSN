/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/06/03  V1.00.00   Pino        program initial                          *
 *  109/06/12  V1.00.01   Pino        voice_open_code、voice_open_code2         *
 *  109/06/17  V1.00.02   Pino        cca_card_base sup_flag                    *
 *  109/07/03  V1.00.03   Wilson      MBBBBBBBB -> M00600000                    *
 *  109-07-03  V1.01.04   yanghan     修改了變量名稱和方法名稱                                                                       *
 *  109/07/03  V1.01.04   JustinWu    change the program name                   *
 *  109/07/10  V1.01.05   Pino        程式結束筆數顯示                                                                                      *
 *  109/07/10  V1.01.06   Wilson      測試修改                                                                                                    * 
 *  109/07/10  V1.01.07   Pino        crd_card效期到月底                                                                           *
 *  109/07/17  V1.01.08   Wilson      測試修改2                                                                                                  *
 *  109-07-22  V1.01.09   yanghan     改了字段名称                                                                                             * 
 *  109-07-23  V1.01.10   wilson      測試修改3                                                                                                   * 
 *  109-07-28  V1.01.11   wilson      商務卡corp_no、corp_p_seqno寫入crd_card       * 
 *  109-07-29  V1.01.12   wilson      新增pvki insert crd_Card                   *
 *  109/07/29  V1.01.13   wilson      rollbackDataBase()                        *
 *  109/08/06  V1.01.14   Wilson      新增insert crd_seqno_log                   *
 *  109/08/11  V1.01.15   Wilson      停用碼修正為"U"                              *
 *  109/08/14  V1.01.16   Wilson      資料夾名稱修改為小寫                                                                            *
 *  109/09/01  V1.01.17   Wilson      新增insert combo_acct_no                   *
 *  109/09/18  V1.01.18   Wilson      檔案格式長度變98                                                                                    *
 *  109/09/21  V1.01.19   Wilson      拿掉檢核檔案格式長度                                                                            *
 *  109/09/28  V1.01.20   Wilson      無檔案不秀error、讀檔不綁檔名日期                                               *
 *  109/09/30  V1.01.21   Wilson      無檔案秀error                              *
 *  109/10/05  V1.01.22   Wilson      insert crd_card新增curr_code              *
 *  109/10/06  V1.01.23   Wilson      讀檔要綁檔名日期                                                                                  *
 *  109/10/12  V1.01.24   Wilson      檔名日期改營業日                                                                                  *
 *  109/10/13  V1.01.25   Wilson      新增錯誤碼11、開卡密碼更正為取民國年生日6碼                             *
 *  109/10/15  V1.01.26   Wilson      insert cca_card_base新增dc_curr_code      *
 *  109/10/16  V1.01.27   Wilson      錯誤報表FTP                                *
 *  109-10-19  V1.01.28    shiyuqi       updated for project coding standard   *
 *  109/11/09  V1.01.29   Wilson      錯誤回覆碼 = 1000 強制開卡、錯誤回覆碼 = 1999 強制關卡 *
 *  109/11/20  V1.01.30   Wilson      insert crd_card新增issue_date             *
 *  109/12/25  V1.01.31   Wilson     無檔案正常結束                                                                                          *
 *  110/01/28  V1.01.32   Wilson     新增批次停卡處理                                                                                     *  
 *  110/03/11  V1.01.33   Justin       fill zero at the right of the ROC birthday until the length of the year is three 
 *  110/09/08  V1.01.34   Wilson     商務卡補發卡的ACNO_P_SEQNO、P_SEQNO要取舊卡的值          *
 *  110/09/13  V1.01.35   Wilson     補發卡舊卡相關資料(子卡旗標、子卡額度)帶入新卡                              *
 *  110/11/15  V1.01.36   Wilson     錯誤回覆碼 = 1999時異動回活卡(current_code = 0)     *
 *  110/12/02  V1.01.37   Justin     add commit and rollback when writing error reports *
 *  110/12/03  V1.01.38   Justin     add a validation for checking date        *
 *  110/12/06  V1.01.39   Wilson     106 599特殊處理邏輯                                                                         *
 *  110/12/08  V1.00.40   Wilson     錯誤訊息調整                                                                                            *
 *  110/12/09  V1.00.41   Wilson     新增errCode = 24                           *
 *  110/12/16  V1.00.42   Wilson     insert cca_card_base新增card_indicator     * 
 *  111/01/21  V1.00.43   Justin     若異動碼為C但UPDATE CRD_CARD找不到資料時，改成走跟異動碼為A一樣的邏輯 *
 *  111/01/24  V1.00.44   Justin     修改錯誤訊息                            *
 *  111/02/14  V1.00.45   Justin     sort files by their modified dates      *
 *  111/02/14  V1.01.46   Ryan      big5 to MS950                                           *
 *  111/02/17  V1.01.47   Justin       fix the bug of error files             *
 *  111/02/17  V1.01.48   Justin       fix the bug of error files             *   
 *  111/03/01  V1.01.49   Justin     check if customerId contains "*"         *
 *  111/03/02  V1.01.50   Justin     刪除Err字新增處理訊息                   *
 *  111/03/04  V1.01.51   Justin     display data1.validDate if validate is improper *
 *  111/03/08  V1.01.52   Justin     增加商務卡同公司第二個ACCT_TYPE要新增ACNO_FLAG = '2'的帳戶相關資料 *
 *  111/03/09  V1.01.53   Justin     增加acct_type = ''                      *
 *                                   修改log訊息                             *
 *  111/03/10  V1.01.54   Justin     INSERT ACT_ACNO STMT_CYCLE 固定放'01'   *
 *  111/03/29  V1.01.55   Wilson     增加取商務卡補發卡的id_p_seqno               *
 *  111/04/20  V1.01.56   Wilson     增加取商務卡補發卡的card_acct_idx...等欄位              *
 *  111/05/23  V1.01.57   Justin     增加log訊息                             *
 *  111/06/07  V1.01.58   Justin     補發新卡相關處理增加UPDATE  ACCT_STATUS *
 *             V1.01.59   Justin     修正crd_card新卡有OLD_CARD_NO問題       *
 *  111/06/24  V1.01.60   Justin     update crd_card 新增 current_code, OPPOST_DATE, OPPOST_REASON *
 *  111/06/29  V1.01.61   Justin     舊卡戶補發新卡或申辦新卡相關處理增加UPDATE ACCT_STATUS*
 *  111/07/04  V1.01.62   Justin     修改UPDATE ACCT_STATUS條件              *
 *  111/07/06  V1.01.63   Justin     修改UPDATE ACCT_STATUS條件              *
 *  111/08/01  V1.01.64   Ryan       updateCrdCard() 增加data1.vipCode = F 的邏輯 *
 *  111/11/02  V1.01.65   Wilson     insert crd_seqno_log dup繼續執行                                *
 ******************************************************************************/

package Icu;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;

import Cca.CcaOutGoing;

/*CARDLINK信用卡客戶資料處理程式*/
public class IcuD005 extends AccessDAO {
	private static final String DATA_FILE_NAME = "ICCRDQND";
	private static final String ISSUE_UNIT_CODE = "M00600000";
	private final String progname = "CARDLINK信用卡卡片資料處理程式  111/11/02  V1.01.65";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CcaOutGoing ccaOutGoing = null;
	NormalCardLayout data1 = new NormalCardLayout();
	TPanCardLayout data2 = new TPanCardLayout();

	String prgmId = "IcuD005";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	protected final String dT1Str = "mod_code, issuer_code, card_type, customer_code, bin, card_seqno, sup_seqno, reissue_seqno, check_code, valid_date, name, cvc1, cvc2, vip_code, black_area, black_date, err_rsp, combo_acct_no";

	protected final int[] ddT1LENGTH = { 1, 8, 2, 11, 6, 7, 1, 1, 1, 4, 19, 3, 3, 1, 9, 4, 4, 13 };

	protected final String dT2Str = "mod_code, tpan_flag, tpan_name, tpan_no, valid_date, primary_card_no, primary_valid_date, space, vip_code, black_area, black_date, err_rsp, combo_acct_no";

	protected final int[] dt2Length = { 1, 1, 20, 16, 4, 16, 4, 5, 1, 9, 4, 4, 13 };

	int rptSeq1 = 0;
	String buf = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String hNcccFilename1 = "";
	String hNcccFilename2 = "";
	String queryDate = "";
	int hRecCnt1 = 0;

	String getFileName;
	String outFileName1;
	String outFileName2;
	int totalInputFile;
	int totalOutputFile1;
	int totalOutputFile2;
	int errOutputFile1;
	int errOutputFile2;
	int errOutPutFile1EachFile;
	int errOutputFile2EachFile;
	int deBug = 1;
	String errCode;
	String hBusiBusinessDate = "";
	String hPrevBusiBusinessDate = "";
	String tmpCardNo = "";
	int oldreissueSeqno = 0;
	String tmpOldCardNo = "";
	String tmpSonCardFlag = "";
	int tmpIndivCrdLmt = 0;
	String tmpAcnoPSeqnoFromCrdCard = "";
	String tmpSupFlag = "";
	String idnoChiName = "";        
	String idnoOfficeAreaCode1 = "";
	String idnoOfficeTelNo1 = "";   
	String idnoOfficeTelExt1 = "";  
	String idnoCardSince = "";      
	String idnoCompanyZip = "";     
	String idnoCompanyAddr1 = "";   
	String idnoCompanyAddr2 = "";   
	String idnoCompanyAddr3 = "";
	String idnoCompanyAddr4 = "";   
	String idnoCompanyAddr5 = "";
	String purChaseCorpPSeqno = "";
	String purChaseAcnoPSeqno = "";
	String purChaseIdPSeqno  = "";
	String purChaseCardAcctIdx = "";
	private Double lineOfCreditAmt = 0.0;
	private Double jrnlBal= 0.0;
	private Double totAmtConsume= 0.0;

	protected String[] dT1 = new String[] {};
	protected String[] dT2 = new String[] {};
	Buf1 ncccData1 = new Buf1();
	Buf2 ncccData2 = new Buf2();

	public int mainProcess(String[] args) {

		try {
			dT1 = dT1Str.split(",");
			dT2 = dT2Str.split(",");
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
			comsecr = new CommSecr(getDBconnect(), getDBalias());
			ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());

			hModUser = comc.commGetUserID();
			
            selectPtrBusinday();
			
			if (args.length == 0) {
//				queryDate = hBusiBusinessDate;
				queryDate = "";
			} else if (args.length == 1) {
				if (!new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
					showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
					return -1;
				}
				queryDate = args[0];
			} else {
				comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
			}
			
			openFile();

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束,總處理筆數:[" + totalInputFile + "],信用卡筆數:[" + totalOutputFile1 + "]信用卡錯誤筆數:["
					+ errOutputFile1 + "]" + "]虛擬卡筆數:[" + totalOutputFile2 + "]" + "]虛擬卡錯誤筆數:[" + errOutputFile2 + "]");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
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

	/************************************************************************/
	int openFile() throws Exception {
		int fileCount = 0;

		String tmpstr = String.format("%s/media/icu", comc.getECSHOME());

		List<String> listOfFiles = comc.listFsSort(tmpstr);

		// 若查詢日(queryDate)為西元年2020年07月03日，則fileNameTemplate
		// =M00600000.ICCUSQND.090703nn.txt，
		// 其中fileName的兩碼年份為民國年後兩碼，因此西元2020年->民國109年->09；nn為編號
//		final String fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", "M00600000", "ICCRDQND",
//				new CommDate().getLastTwoTWDate(queryDate), queryDate.substring(4, 8)); // 檔案正規表達式
		
		/////////////////////////
		String fileNameTemplate  = ""; // String fileNameTemplate
		String fileNameTemplate2 = ""; // previous business date

		if (queryDate.length() > 0) {
			fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
					ISSUE_UNIT_CODE, 
					DATA_FILE_NAME
					,new CommDate().getLastTwoTWDate(queryDate), 
					queryDate.substring(4, 8)); // 檔案正規表達式
			
			showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate));
		}else {
			fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
					ISSUE_UNIT_CODE, 
					DATA_FILE_NAME
					,new CommDate().getLastTwoTWDate(hBusiBusinessDate), 
					hBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
			
			fileNameTemplate2 = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
					ISSUE_UNIT_CODE, 
					DATA_FILE_NAME,
					new CommDate().getLastTwoTWDate(hPrevBusiBusinessDate), 
					hPrevBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
			
			showLogMessage("I", "", String.format("尋找檔案[%s] 或", fileNameTemplate));
			showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate2));
		}
		
		/////////////////////////

		if (listOfFiles.size() > 0)
			for (String file : listOfFiles) {
				getFileName = file;
//			if (getFileName.length() != 29)
//				continue;
//			if (!getFileName.substring(0, 19).equals("M00600000.ICCRDQND."))
//				continue;
				if (!( file.matches(fileNameTemplate) || ( fileNameTemplate2.length() > 0 && file.matches(fileNameTemplate2) ) )) 
					continue;
				if (checkFileCtl() != 0)
					continue;
				fileCount++;
				readFile(getFileName);
			}
		if (fileCount < 1) {
			showLogMessage("I", "", "無檔案可處理,處理日期 = " + queryDate);
			
//			comcr.hCallErrorDesc = "Error : 無檔案可處理";
//			comcr.errRtn("Error : 無檔案可處理", "處理日期 = " + queryDate , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	int readFile(String fileName) throws Exception {
		errOutPutFile1EachFile = 0;
		errOutputFile2EachFile = 0;
		
		String rec = "";
		String fileName2;
		int fi;
		fileName2 = comc.getECSHOME() + "/media/icu/" + fileName;

		int f = openInputText(fileName2);
		if (f == -1) {
			return 1;
		}
		closeInputText(f);

		setConsoleMode("N");
		fi = openInputText(fileName2, "MS950");
		setConsoleMode("Y");
		if (fi == -1) {
			return 1;
		}

		showLogMessage("I", "", " Process file path =[" + comc.getECSHOME() + "/media/icu ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");

		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;
				errCode = "";
				totalInputFile++;
				if (rec.substring(1, 2).equals("T")) {
					moveData2(processDataRecord(getFieldValue(rec, dt2Length), dT2));
				} else {
					moveData1(processDataRecord(getFieldValue(rec, ddT1LENGTH), dT1));
				}
				processDisplay(1000);			
		}
		
		commFTP = new CommFTP(getDBconnect(), getDBalias());
	    comr = new CommRoutine(getDBconnect(), getDBalias());

		if (errOutPutFile1EachFile > 0) {
			outPutTextFile1();
			comc.writeReport(outFileName1, lpar1, "MS950");
			insertFileCtl(hNcccFilename1);
			lpar1.clear();
			
		    procFTP(hNcccFilename1);
		    renameFile1(hNcccFilename1);
		}
		if (errOutputFile2EachFile > 0) {
			outPutTextFile2();
			comc.writeReport(outFileName2, lpar2, "MS950");
			insertFileCtl(hNcccFilename2);
			lpar2.clear();
			
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP(hNcccFilename2);
		    renameFile1(hNcccFilename2);
		}

		closeInputText(fi);

		insertFileCtl1(fileName);

		renameFile(fileName);

		return 0;
	}

	/***********************************************************************/
	private void moveData1(Map<String, Object> map) throws Exception {
		
		lineOfCreditAmt = 0.0;
		jrnlBal = 0.0;
		totAmtConsume = 0.0;
		String tmpChar = "";
		
		totalOutputFile1++;
		data1.initData();
		data1.modCode = (String) map.get("mod_code"); // 異動碼
		data1.modCode = data1.modCode.trim();
		data1.issuerCode = (String) map.get("issuer_code"); // 發卡單位代號
		data1.issuerCode = data1.issuerCode.trim();
		data1.cardType = (String) map.get("card_type"); // 卡別代號
		data1.cardType = data1.cardType.trim();
		data1.customerCode = (String) map.get("customer_code"); // 客戶代號 第11碼為延伸碼
		tmpChar = data1.customerCode.substring(10); // 延伸碼
		data1.customerCode = data1.customerCode.trim();
		data1.bin = (String) map.get("bin"); // 發卡單位BIN
		data1.bin = data1.bin.trim();
		data1.cardSeqno = (String) map.get("card_seqno"); // 卡片流水號
		data1.cardSeqno = data1.cardSeqno.trim();
		data1.supSeqno = (String) map.get("sup_seqno"); // 正附卡序號
		data1.supSeqno = data1.supSeqno.trim();
		data1.reissueSeqno = (String) map.get("reissue_seqno"); // 補發卡序號
		data1.reissueSeqno = data1.reissueSeqno.trim();
		data1.checkCode = (String) map.get("check_code"); // 卡片檢查號
		data1.checkCode = data1.checkCode.trim();
		tmpCardNo = data1.bin + data1.cardSeqno + data1.supSeqno + data1.reissueSeqno + data1.checkCode;
		data1.validDate = (String) map.get("valid_date"); // 有效日期
		data1.validDate = data1.validDate.trim();
		data1.name = (String) map.get("name"); // 凸字姓名
		data1.name = data1.name.trim();
		data1.cvc1 = (String) map.get("cvc1"); // VISA CVV /MasterCard CVC1
		data1.cvc1 = data1.cvc1.trim();
		data1.cvc2 = (String) map.get("cvc2"); // VISA CVV II/MasterCard CVC2
		data1.cvc2 = data1.cvc2.trim();
		data1.vipCode = (String) map.get("vip_code"); // 停用碼/VIP碼
		data1.vipCode = data1.vipCode.trim();
		data1.blackArea = (String) map.get("black_area"); // 黑名單地區別及JCB停掛異動碼
		data1.blackArea = data1.blackArea.trim();
		data1.blackDate = (String) map.get("black_date"); // 黑名單或停掛檔取消日期
		data1.blackDate = data1.blackDate.trim();
		data1.errRsp = (String) map.get("err_rsp"); // 錯誤回覆碼
		data1.errRsp = data1.errRsp.trim();
		data1.comboAcctNo = (String) map.get("combo_acct_no"); // COMBO金融卡帳號
		data1.comboAcctNo = data1.comboAcctNo.trim();
		
		// initialize value
		tmpSupFlag = "";
		tmpOldCardNo   = "";
		tmpSonCardFlag = "";
		tmpIndivCrdLmt = 0;
		tmpAcnoPSeqnoFromCrdCard = "";
		
		showLogMessage("I", "", "card_no = [" + tmpCardNo + "]");
		if (data1.customerCode.indexOf("*") != -1) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error : 客戶代號不可為*");
			errCode = "25";
			createErrReport1();
			errOutputFile1++;
			return;
		}

//        showLogMessage("I", "", "card_seqno = [" + data1.card_seqno + "]");
		if (!tmpChar.equals(" ") && !tmpChar.equals("R")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error : 客戶代號第11碼延伸碼不為空白 或'R'");
			errCode = "1";
			createErrReport1();
			errOutputFile1++;
			return;
		}
		
		// 2021/12/03 Justin add a validation for checking date
		if (isDateValid(data1.validDate) == false) {
			if (deBug == 1)
				showLogMessage("I", "", String.format("card_no = [%s]，Error : 信用卡有效日期[%s]錯誤", tmpCardNo, data1.validDate));
			errCode = "18";
			createErrReport1();
			errOutputFile1++;
			return;
		}
		
		switch (data1.modCode) {
		case "A":
			insertCrdCard();
			break;
		case "C":
			// 若異動碼為C但UPDATE CRD_CARD找不到資料時，改成走跟異動碼為A一樣的邏輯
			boolean result = updateCrdCard();
			if (result == false) {
				insertCrdCard();
			}
			break;
		}
		commitDataBase();
		return;
	}

	/***********************************************************************/
	private void moveData2(Map<String, Object> map) throws Exception {
		String tmpChar = "";
		totalOutputFile2++;
		data2.initData();
		data2.modCode = (String) map.get("mod_code"); // 異動碼 ACD
		data2.modCode = data2.modCode.trim();
		data2.tpanFlag = (String) map.get("tpan_flag"); // TPAN註記 固定為T
		data2.tpanFlag = data2.tpanFlag.trim();
		data2.tpanName = (String) map.get("tpan_name"); // TPAN名稱/代號
		data2.tpanName = data2.tpanName.trim();
		data2.tpanCardNo = (String) map.get("tpan_no"); // TPAN卡號
		data2.tpanCardNo = data2.tpanCardNo.trim();
		data2.validDate = (String) map.get("valid_date"); // 有效日期
		data2.validDate = data2.validDate.trim();
		data2.primaryCardNo = (String) map.get("primary_card_no"); // 主卡卡號
		data2.primaryCardNo = data2.primaryCardNo.trim();
		data2.primaryValidDate = (String) map.get("primary_valid_date"); // 主卡效期
		data2.primaryValidDate = data2.primaryValidDate.trim();
		data2.space = (String) map.get("space"); // 保留
		data2.space = data2.space.trim();
		data2.vipCode = (String) map.get("vip_code"); // 停用碼/VIP碼
		data2.vipCode = data2.vipCode.trim();
		data2.blackArea = (String) map.get("black_area"); // 黑名單地區別及JCB停掛異動碼
		data2.blackArea = data2.blackArea.trim();
		data2.blackDate = (String) map.get("black_date"); // 黑名單或停掛檔取消日期
		data2.blackDate = data2.blackDate.trim();
		data2.errRsp = (String) map.get("err_rsp"); // 錯誤回覆碼
		data2.errRsp = data2.errRsp.trim();
		
		
		// 2021/12/03 Justin add a validation for checking date
		if (isDateValid(data2.validDate) == false) {
			if (deBug == 1)
				showLogMessage("I", "", "v_card_no = [" + data2.tpanCardNo + "]，Error : HCE卡有效日期錯誤");
			errCode = "6";
			createErrReport2();
			errOutputFile2++;
			return;
		}

		switch (data2.modCode) {
		case "A":
			insertHceCard();
			break;
		case "C":
			updateHceCard();
			break;
		}
		commitDataBase();
		return;
	}
	
	/***********************************************************************/

	/**
	 * @param date This format is MMYY
	 * @return
	 */
	private boolean isDateValid(String date) {
		if (date == null || date.isEmpty()) {
			return false;
		}else {
			if (date.length() != 4) {
				return false;
			}			
			int month = Integer.parseInt(date.substring(0,2));		
			if (month < 1 || month > 12) {
				return false;
			}		
		}
		return true;
	}

	/***********************************************************************/
	int outPutTextFile1() throws Exception {
		int fileNo = 0;

		sqlCmd = "select max(substr(file_name, 26, 2)) file_no";
		sqlCmd += " from crd_file_ctl  ";
		sqlCmd += " where file_name like ?";
		sqlCmd += "  and crt_date  = ? ";
		setString(1, "ICCRDQND.CRD.ERR." + "%" + ".TXT");
		setString(2, sysDate);

		if (selectTable() > 0)
			fileNo = getValueInt("file_no");

		hNcccFilename1 = String.format("ICCRDQND.CRD.ERR.%s%02d.TXT", sysDate, fileNo + 1);
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename1 + "]");

		outFileName1 = String.format("%s/media/icu/error/%s", comc.getECSHOME(), hNcccFilename1);
		outFileName1 = Normalizer.normalize(outFileName1, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName1 + "]");

		return 0;
	}

	/***********************************************************************/
	int outPutTextFile2() throws Exception {
		int fileNo = 0;

		sqlCmd = "select max(substr(file_name, 26, 2)) file_no";
		sqlCmd += " from crd_file_ctl  ";
		sqlCmd += " where file_name like ?";
		sqlCmd += "  and crt_date  = ? ";
		setString(1, "ICCRDQND.HCE.ERR." + "%" + ".TXT");
		setString(2, sysDate);

		if (selectTable() > 0)
			fileNo = getValueInt("file_no");

		hNcccFilename2 = String.format("ICCRDQND.HCE.ERR.%s%02d.TXT", sysDate, fileNo + 1);
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename2 + "]");

		outFileName2 = String.format("%s/media/icu/error/%s", comc.getECSHOME(), hNcccFilename2);
		outFileName2 = Normalizer.normalize(outFileName2, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName2 + "]");

		return 0;
	}

	/***********************************************************************/
	int checkFileCtl() throws Exception {
		int totalCount = 0;

		sqlCmd = "select count(*) totalCount ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
//		sqlCmd += " and crt_date = to_char(sysdate,'yyyymmdd') ";
		setString(1, getFileName);
		int recordCnt = selectTable();

		if (recordCnt > 0)
			totalCount = getValueInt("totalCount");

		if (totalCount > 0) {
			showLogMessage("I", "", String.format("此檔案 = [" + getFileName + "]已處理過不可重複處理(crd_file_ctl)"));
			return (1);
		}
		return (0);
	}

	/***********************************************************************/
	void createErrReport1() throws Exception {
		errOutPutFile1EachFile++;

		ncccData1 = new Buf1();

		ncccData1.modNo = data1.modCode;
		ncccData1.idnoId = data1.customerCode;
		ncccData1.cardNo = tmpCardNo;

		switch (errCode) {
		case "1":
			ncccData1.errReason = String.format("%-200s", "客戶代號第11碼延伸碼不為空白 或'R'");
			break;
		case "2":
			ncccData1.errReason = String.format("%-200s", "異動碼為'C'，但主檔無資料(1)");
			break;
		case "3":
			ncccData1.errReason = String.format("%-200s", "異動碼為'C'，但主檔無資料(2)");
			break;
		case "4":
			ncccData1.errReason = String.format("%-200s", "停用碼為'U'，但授權卡片檔無資料");
			break;
		case "5":
			ncccData1.errReason = String.format("%-200s", "附卡，但主檔無正卡資料");
			break;
		case "6":
			ncccData1.errReason = String.format("%-200s", "附卡，但主檔無正卡人資料");
			break;
		case "7":
			ncccData1.errReason = String.format("%-200s", "主檔無卡人資料");
			break;
		case "8":
			ncccData1.errReason = String.format("%-200s", "異動碼為'A'，但卡片主檔已存在");
			break;
		case "9":
			ncccData1.errReason = String.format("%-200s", "異動碼為'A'，但授權卡片檔已存在");
			break;
		case "10":
			ncccData1.errReason = String.format("%-200s", "商務卡，但企業資料檔無資料");
			break;
		case "11":
			ncccData1.errReason = String.format("%-200s", "異動碼為'A'，卡片流水號紀錄檔已存在");
			break;
		case "12":
			ncccData1.errReason = String.format("%-200s", "異動碼為'C'，但主檔無資料(3)");
			break;
		case "13":
			ncccData1.errReason = String.format("%-200s", "停用碼為'Q'，但卡片檔無資料");
			break;
		case "14":
			ncccData1.errReason = String.format("%-200s", "商務卡補發卡，但無舊卡資料");
			break;
		case "15":
			ncccData1.errReason = String.format("%-200s", "補發卡，但無舊卡資料");
			break;
		case "16":
			ncccData1.errReason = String.format("%-200s", "附卡異動正卡新卡號找不到資料");
			break;			
		case "17":
			ncccData1.errReason = String.format("%-200s", "補發卡異動舊卡找不到資料");
			break;	
		case "18":
			ncccData1.errReason = String.format("%-200s", "信用卡有效日期錯誤");
			break;
		case "19":
			ncccData1.errReason = String.format("%-200s", "團代1599，但無卡人檔資料");
			break;
		case "20":
			ncccData1.errReason = String.format("%-200s", "團代1599或商務卡同公司多個ACCT_TYPE，但帳戶資料檔已存在");
			break;
		case "21":
			ncccData1.errReason = String.format("%-200s", "團代1599或商務卡同公司多個ACCT_TYPE，但授權帳戶資料檔已存在");
			break;
		case "22":
			ncccData1.errReason = String.format("%-200s", "團代1599或商務卡同公司多個ACCT_TYPE，但授權卡戶帳務資料檔已存在");
			break;
		case "23":
			ncccData1.errReason = String.format("%-200s", "團代1599，但企業資料檔已存在");
			break;
		case "24":
			ncccData1.errReason = String.format("%-200s", "團代1599，但主檔無卡人資料");
			break;
		case "25":
			ncccData1.errReason = String.format("%-200s", "客戶代號不可為*");
			break;
		case "26":
			ncccData1.errReason = String.format("%-200s", "停用碼為'Ｆ'，但卡片檔無活卡資料");
			break;
		}

		ncccData1.date = sysDate;

		buf = ncccData1.allText();
//        lpar1.clear();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}

	/***********************************************************************/
	void createErrReport2() throws Exception {
		errOutputFile2EachFile++;

		ncccData2 = new Buf2();

		ncccData2.modNo = data2.modCode;
		ncccData2.tpanCardNo = data2.tpanCardNo;
		ncccData2.primaryCardNo = data2.primaryCardNo;

		switch (errCode) {
		case "1":
			ncccData2.errReason = String.format("%-200s", "異動碼為'C'，但HCE卡片主檔無資料(1)");
			break;
		case "2":
			ncccData2.errReason = String.format("%-200s", "異動碼為'C'，但HCE卡片主檔無資料(2)");
			break;
		case "3":
			ncccData2.errReason = String.format("%-200s", "HCE卡在信用卡卡片檔無資料");
			break;
		case "4":
			ncccData2.errReason = String.format("%-200s", "HCE卡在信用卡卡人檔無資料");
			break;
		case "5":
			ncccData2.errReason = String.format("%-200s", "異動碼為'A'，但HCE卡片主檔已存在");
			break;
		case "6":
			ncccData2.errReason = String.format("%-200s", "HCE卡有效日期錯誤");
			break;
		}

		ncccData2.date = sysDate;

		buf = ncccData2.allText();
//        lpar2.clear();
		lpar2.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}

	/***********************************************************************/
	void insertCrdCard() throws Exception {
		boolean isCorpCard = false; //是否為商務卡
		boolean isNewApply = false; //是否為申請新卡
		
		String tmpGroupCode = "";
		String tmpCardType = "";
		String tmpCardIndicator = "";
		String tmpStmtCycle = "";
		String tmpAcctType = "";
		String tmpMajorIdPSeqno = "";
		String tmpMajorCardNo = "";
		String tmpPmId = "";
		String tmpIdPSeqno = "";
		String tmpBinType = "";
		String tmpCardNote = "";		
		String idnoBirthday = "";
		String cardMajorIdPSeqno = "";
		String cardMajorCardNo = "";
		String tmpComboIndicator = "";
		String ccaVoiceOpenCode = "";
		String ccaVoiceOpenCode2 = "";
		String tmpIcFlag = "";
		String tmpElectronicCode = "";
		String tmpAcnoPSeqno = "";
		String tmpPSeqno = "";
		String tmpAcnoFlag = "";
		String tmpCorpPSeqno = "";
		String tmpCorpNo = "";
		String tmpCorpNoCode = "";
		String tmpCardAcctIdx = "";
		String tmpPvki = "";
		String tmpCurrCode = "";		
		oldreissueSeqno = Integer.parseInt(data1.reissueSeqno) - 1;
		
		sqlCmd = "select group_code,card_type ";
		sqlCmd += " from crd_cardno_range ";
		sqlCmd += " where bin_no = ? ";
		sqlCmd += " and beg_seqno <= ? ";
		sqlCmd += " and end_seqno >= ? ";
		setString(1, data1.bin);
		setString(2, data1.cardSeqno + data1.supSeqno + data1.reissueSeqno);// 卡片流水號 +正附卡序號 +補發卡序號
		setString(3, data1.cardSeqno + data1.supSeqno + data1.reissueSeqno);// 卡片流水號 +正附卡序號 +補發卡序號
		
//		showLogMessage("I", "", "[" + data1.bin + "]");
//		showLogMessage("I", "", "[" + data1.cardSeqno + data1.supSeqno + data1.reissueSeqno + "]");
		
		
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_crd_cardno_range error[notFound]",
					data1.bin + data1.cardSeqno + data1.supSeqno + data1.reissueSeqno, "");
		}
		if (recordCnt > 0) {
			tmpGroupCode = getValue("group_code");
			tmpCardType = getValue("card_type");
		}

		selectSQL = " ic_flag ,service_code , electronic_code ";
		daoTable = "crd_item_unit";
		whereStr = "WHERE card_type     =  ? " + "  and unit_code     =  ? ";
		setString(1, tmpCardType);
		setString(2, tmpGroupCode);

		int recCnt = selectTable();

		if (notFound.equals("Y")) {
			comcr.errRtn("select_crd_item_unit error[notFound]", "卡種 = " + tmpCardType + ",認同集團碼 = " + tmpGroupCode,
					"");
		}
		if (recordCnt > 0) {
			tmpIcFlag = getValue("ic_flag");
			tmpElectronicCode = getValue("electronic_code");
		}

		selectSQL = "case when combo_indicator='' then 'N' else combo_indicator " + " end as combo_indicator ";
		daoTable = "ptr_group_code";
		whereStr = "WHERE group_code = ? ";
		setString(1, tmpGroupCode);
		recCnt = selectTable();
		if (notFound.equals("Y")) {
			String err1 = "select_ptr_group_code error = " + tmpGroupCode;
			String err2 = tmpGroupCode;
//            comcr.err_rtn(err1, err2, comcr.h_call_batch_seqno);
		}
		if (recordCnt > 0) {
			tmpComboIndicator = getValue("combo_indicator");
		}

		sqlCmd = "select a.card_indicator, ";
		sqlCmd += " a.stmt_cycle, ";
		sqlCmd += " a.acct_type ";
		sqlCmd += " from ptr_acct_type a,ptr_prod_type b ";
		sqlCmd += " where b.card_type = '' ";
		sqlCmd += " and a.acct_type = b.acct_type ";
		sqlCmd += " and b.group_code = ? ";
		setString(1, tmpGroupCode);
		recordCnt = selectTable();
		if (notFound.equals("Y")) {
			sqlCmd = "select a.card_indicator, ";
			sqlCmd += " a.stmt_cycle, ";
			sqlCmd += " a.acct_type ";
			sqlCmd += " from ptr_acct_type a,ptr_prod_type b ";
			sqlCmd += " where b.card_type = ? ";
			sqlCmd += " and a.acct_type = b.acct_type ";
			setString(1, tmpCardType);
			recordCnt = selectTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("Error : 找不到帳戶資料", "團代 = " + tmpGroupCode + "卡種 =  " + tmpCardType, "");
			} else {
				tmpCardIndicator = getValue("card_indicator");
				tmpStmtCycle = getValue("stmt_cycle");
				tmpAcctType = getValue("acct_type");
			}
		} else {
			tmpCardIndicator = getValue("card_indicator");
			tmpStmtCycle = getValue("stmt_cycle");
			tmpAcctType = getValue("acct_type");
		}
		
		if (tmpCardIndicator.equals("1")) {
			isCorpCard = false;
			if (!data1.supSeqno.equals("1")) {
				sqlCmd = "select a.major_id_p_seqno, ";
				sqlCmd += " a.card_no, ";
				sqlCmd += " a.acno_p_seqno, ";
				sqlCmd += " a.p_seqno, ";
				sqlCmd += " a.acno_flag, ";
				sqlCmd += " c.card_acct_idx ";
				sqlCmd += " from crd_card a,crd_idno b ,cca_card_base c";
				sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
				sqlCmd += " and a.card_no = c.card_no ";
				sqlCmd += " and substring(a.card_no,1,13) = ? ";
				sqlCmd += " and substring(a.card_no,14,1) = '1' ";
				sqlCmd += " and a.card_type = ? ";
				sqlCmd += " and a.current_code = '0' ";
				sqlCmd += " and a.sup_flag = '0' ";
				sqlCmd += " and a.group_code = ? ";
				sqlCmd += " fetch first 1 rows only ";
				setString(1, data1.bin + data1.cardSeqno);
				setString(2, tmpCardType);
				setString(3, tmpGroupCode);
				recordCnt = selectTable();
				if (notFound.equals("Y")) {
					if (deBug == 1)
						showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:附卡，但主檔無正卡資料");
					errCode = "5";
					createErrReport1();
					errOutputFile1++;
					return;
				}
				if (recordCnt > 0) {
					if (getValue("card_no").length() == 0) {
						if (deBug == 1)
							showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:附卡，但主檔無正卡資料");
						errCode = "5";
						createErrReport1();
						errOutputFile1++;
						return;
					}
					tmpMajorIdPSeqno = getValue("major_id_p_seqno");
					tmpMajorCardNo = getValue("card_no");
					tmpAcnoPSeqno = getValue("acno_p_seqno");
					tmpPSeqno = getValue("p_seqno");
					tmpAcnoFlag = getValue("acno_flag");
					tmpCardAcctIdx = getValue("card_acct_idx");
				}

				sqlCmd = "select id_no ";
				sqlCmd += " from crd_idno ";
				sqlCmd += " where id_p_seqno = ? ";
				setString(1, tmpMajorIdPSeqno);
				recordCnt = selectTable();
				if (notFound.equals("Y")) {
					if (deBug == 1)
						showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:附卡，但主檔無正卡人資料");
					errCode = "6";
					createErrReport1();
					errOutputFile1++;
					return;
				}
				if (recordCnt > 0) {
					tmpPmId = getValue("id_no");
				}
			}
			sqlCmd = "select id_p_seqno ";
			sqlCmd += " from crd_idno ";
			sqlCmd += " where id_no = ? ";
			sqlCmd += " fetch first 1 rows only ";
			setString(1, data1.customerCode.substring(0, 10));
			recordCnt = selectTable();
			if (notFound.equals("Y")) {
				if (deBug == 1)
					showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:主檔無卡人資料");
				errCode = "7";
				createErrReport1();
				errOutputFile1++;
				return;
			}
			if (recordCnt > 0) {
				tmpIdPSeqno = getValue("id_p_seqno");
			}

			sqlCmd = "select birthday ";
			sqlCmd += " from crd_idno ";
			sqlCmd += " where id_p_seqno = ? ";
			sqlCmd += " fetch first 1 rows only ";
			setString(1, tmpIdPSeqno);
			recordCnt = selectTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("select_crd_idno error[birthday not found]", tmpIdPSeqno, "");
			}
			if (recordCnt > 0) {
				idnoBirthday = getValue("birthday");
			}
			
			// 生日轉民國年
			idnoBirthday = String.format("%03d", Integer.valueOf(idnoBirthday.substring(0, 4)) - 1911)
					+ idnoBirthday.substring(4, 6) + idnoBirthday.substring(6, 8);
	
			ccaVoiceOpenCode = comc.transPasswd(0, idnoBirthday.substring(1));
			ccaVoiceOpenCode2 = comc.transPasswd(0, idnoBirthday.substring(1));

		} else {
			isCorpCard = true;
			
			tmpCorpNo = data1.customerCode;
			tmpCorpNoCode = "0";
			
			//20211206新增106 599特殊處理邏輯
		     if(tmpGroupCode.equals("1599")) {
		    	 int acnoCount = 0;
		    	 
		    	 sqlCmd = "select id_p_seqno ";
				 sqlCmd += " from crd_idno ";
				 sqlCmd += " where id_no = ? ";
				 sqlCmd += " fetch first 1 rows only ";
				 setString(1, data1.customerCode.substring(0, 10));
				 recordCnt = selectTable();
				 if (notFound.equals("Y")) {
					 if (deBug == 1)
				  		 showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:團代1599，但主檔無卡人資料");
					 errCode = "24";
					 createErrReport1();
					 errOutputFile1++;
					 return;
					}
					if (recordCnt > 0) {
						purChaseIdPSeqno = getValue("id_p_seqno");
						tmpIdPSeqno = purChaseIdPSeqno;
					}
		    	 		    	 
		    	 sqlCmd = "select corp_p_seqno ";
				 sqlCmd += " from crd_corp ";
				 sqlCmd += " where corp_no = ? ";
				 setString(1, data1.customerCode);
				 recordCnt = selectTable();
				 if (recordCnt > 0) {
					 purChaseCorpPSeqno = getValue("corp_p_seqno");
					 tmpCorpPSeqno = purChaseCorpPSeqno;
					 
					 sqlCmd = "select count(*) as cnt";
					 sqlCmd += " from act_acno ";
					 sqlCmd += " where corp_p_seqno = ? ";
					 sqlCmd += " and acct_type = '06' ";
					 setString(1, purChaseCorpPSeqno);
					 recordCnt = selectTable();
					 if (recordCnt > 0) {
						 acnoCount = getValueInt("cnt");
						 
						 if(acnoCount == 0) {
							 
							 getAcnoPSeqno();
								
							 insertActAcno("06");
							 insertCcaCardAcct();
							 insertCcaConsume();
						 }
					 }
				 }
				 else {
					 
					 getCorpPSeqno();
					 tmpCorpPSeqno = purChaseCorpPSeqno;
					 getCrdIdno();
					 getAcnoPSeqno();
					 
					 insertCrdCorp();
					 insertActAcno("06");
					 insertCcaCardAcct();
					 insertCcaConsume();
				 }
		     }else {
		    	 
		    	    sqlCmd = "select corp_p_seqno ";
					sqlCmd += "from crd_corp ";
					sqlCmd += "where corp_no = ? ";
					setString(1, data1.customerCode);
					recordCnt = selectTable();
					if (notFound.equals("Y")) {
						if (deBug == 1)
							showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:商務卡，但企業資料檔無資料");
						errCode = "10";
						createErrReport1();
						errOutputFile1++;
						return;
					}
					if (recordCnt > 0) {
						tmpCorpPSeqno = getValue("corp_p_seqno");
					}
					
					// 2022/03/08 Justin 增加商務卡同公司第二個ACCT_TYPE要新增ACNO_FLAG = '2'的帳戶相關資料
					sqlCmd =  "SELECT CORP_P_SEQNO ";
					sqlCmd += "FROM ACT_ACNO ";
					sqlCmd += "WHERE ACNO_FLAG = '2' AND CORP_P_SEQNO = ? AND ( ACCT_TYPE = ? OR ACCT_TYPE = '') ";
					setString(1, tmpCorpPSeqno);
					setString(2, tmpAcctType);
					recordCnt = selectTable();
					if (notFound.equals("Y") || recordCnt == 0) {
						// 有撈到資料就INSERT ACT_ACNO、CCA_CARD_ACCT、CCA_CONSUME
						sqlCmd =  "SELECT aa.line_of_credit_amt, cca.jrnl_bal, cca.tot_amt_consume "
						        + "FROM ACT_ACNO aa, CCA_CARD_ACCT cca "
						        + "WHERE aa.ACNO_FLAG = '2' "
								+ "  AND aa.CORP_P_SEQNO = ? "
								+ "  AND cca.ACNO_P_SEQNO = aa.ACNO_P_SEQNO "
								+ "  FETCH FIRST 1 ROWS ONLY ";
						setString(1, tmpCorpPSeqno);
						recordCnt = selectTable();
						if (recordCnt > 0) {
							lineOfCreditAmt = getValueDouble("line_of_credit_amt");
							jrnlBal = getValueDouble("jrnl_bal");
							totAmtConsume = getValueDouble("tot_amt_consume");
							
							getAcnoPSeqno(); //	purChaseAcnoPSeqno = 
							purChaseCorpPSeqno = tmpCorpPSeqno;
							purChaseIdPSeqno = "";
							insertActAcno(tmpAcctType);
							insertCcaCardAcct();
							insertCcaConsume();
							
							lineOfCreditAmt = 0.0;
							jrnlBal = 0.0;
							totAmtConsume = 0.0;
						}
					}
					
		     }	     
			
			//商務卡補發卡的ACNO_P_SEQNO要取舊卡的值
			if(!data1.reissueSeqno.equals("0")) {				
				
				sqlCmd = "select a.acno_p_seqno, ";
				sqlCmd += "a.p_seqno, ";
				sqlCmd += "a.id_p_seqno, ";
				sqlCmd += "a.acno_flag, ";
				sqlCmd += "b.card_acct_idx, ";
				sqlCmd += "b.voice_open_code, ";
				sqlCmd += "b.voice_open_code2 ";
				sqlCmd += "from crd_card a,cca_card_base b ";
				sqlCmd += "where a.card_no = b.card_no ";
				sqlCmd += "and a.card_no like ? ";
				setString(1, data1.bin + data1.cardSeqno + data1.supSeqno + oldreissueSeqno + "%");
				recordCnt = selectTable();
				if (notFound.equals("Y")) {
					if (deBug == 1)
						showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:商務卡補發卡，但無舊卡資料");
					errCode = "14";
					createErrReport1();
					errOutputFile1++;
					return;
				}
				if (recordCnt > 0) {
					tmpAcnoPSeqno     = getValue("acno_p_seqno");
					tmpPSeqno         = getValue("p_seqno");
					tmpIdPSeqno       = getValue("id_p_seqno");
					tmpAcnoFlag       = getValue("acno_flag");
					tmpCardAcctIdx    = getValue("card_acct_idx");
					ccaVoiceOpenCode  = getValue("voice_open_code");
					ccaVoiceOpenCode2 = getValue("voice_open_code2");
				}			
			}
			
//			sqlCmd = "select id_p_seqno ";
//		sqlCmd += " from crd_idno ";
//			sqlCmd += " where id_no = ? ";
//			sqlCmd += " fetch first 1 rows only ";
//			setString(1, data1.customerCode.substring(0, 10));
//			recordCnt = selectTable();
//			if (notFound.equals("Y")) {
//				sqlCmd = "select id_p_seqno ";
//				sqlCmd += " from crd_idno_seqno ";
//				sqlCmd += " where id_no = ? ";
//				setString(1, data1.customerCode.substring(0, 10));
//				recordCnt = selectTable();
//				if (notFound.equals("Y")) {
//					sqlCmd = "select substr(to_char(ecs_acno.nextval,'0000000000'), 2,10) as temp_x10 ";
//					sqlCmd += " from dual ";
//					recordCnt = selectTable();
//					if (notFound.equals("Y")) {
//						comcr.errRtn("Error : select_ecs_acno error[notFound]", "", "");
//					}
//					if (recordCnt > 0) {
//						tmp_id_p_seqno = getValue("temp_x10");
//						insertCrdIdnoSeqno(tmp_id_p_seqno);
//						insertCrdIdno(tmp_id_p_seqno);
//					}
//				}
//				if (recordCnt > 0) {
//					tmp_id_p_seqno = getValue("id_p_seqno");
//				}
//			}
//			if (recordCnt > 0) {
//				tmp_id_p_seqno = getValue("id_p_seqno");
//			}
		}
		sqlCmd = "select bin_type, ";
		sqlCmd += "decode(dc_curr_code , '' , '901' , dc_curr_code) as curr_code ";
		sqlCmd += " from ptr_bintable ";
		sqlCmd += " where bin_no || bin_no_2_fm || '0000' <= ? ";
		sqlCmd += "  and bin_no || bin_no_2_to || '9999' >= ? ";
		setString(1, tmpCardNo);
		setString(2, tmpCardNo);
		recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_bintable error[not found]", data1.bin, "");
		}
		if (recordCnt > 0) {
			tmpBinType = getValue("bin_type");
			tmpCurrCode = getValue("curr_code");
		}

		String sqls = "";
		switch (tmpBinType) {
		case "V":
			sqls += " visa_pvki as ecs_pvki  ";
			break;
		case "M":
			sqls += " master_pvki as ecs_pvki ";
			break;
		case "J":
			sqls += " jcb_pvki as ecs_pvki ";
			break;
		default:
			sqls += " '' as ecs_pvki ";
			break;
		}

		selectSQL = sqls;
		daoTable = " ptr_hsm_keys ";
		whereStr = " where hsm_keys_org ='00000000'  ";

		recCnt = selectTable();

		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_hsm_keys error[not find]", tmpBinType, "");
		}
		if (recordCnt > 0) {
			tmpPvki = getValue("ecs_pvki");
		}

		sqlCmd = "select card_note ";
		sqlCmd += " from ptr_card_type ";
		sqlCmd += " where card_type = ? ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1, tmpCardType);
		recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_card_type error[not found]", tmpCardType, "");
		}
		if (recordCnt > 0) {
			tmpCardNote = getValue("card_note");
		}
		if (data1.supSeqno.equals("1")) {
			tmpSupFlag = "0";
			cardMajorIdPSeqno = tmpIdPSeqno;
			cardMajorCardNo = tmpCardNo;
		} else {
			tmpSupFlag = "1";
			cardMajorIdPSeqno = tmpMajorIdPSeqno;
			cardMajorCardNo = tmpMajorCardNo;
		}
		
		String newEndDate = "20" + data1.validDate.substring(2, 4) + data1.validDate.substring(0, 2) + "01";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate parsedDate = LocalDate.parse(newEndDate, formatter);
		LocalDate lastDay = parsedDate.with(TemporalAdjusters.lastDayOfMonth());
		newEndDate = lastDay.format(formatter);

		//補發新卡相關處理
		if(!data1.reissueSeqno.equals("0")) {
			isNewApply = false;
			
			//舊卡相關資料(子卡旗標、子卡額度)要帶到新卡
			boolean result = getOldCrdCard();	
			if (result == false) {
				return;
			}
			result = updateOldCard();
			if (result == false) {
				return;
			}
		}else {
			isNewApply = true;
		}
		
		/* 2022/06/09 Justin 舊卡戶補發新卡或申辦新卡相關處理增加UPDATE ACCT_STATUS */
		/* 2022/07/05 Justin 修改UPDATE ACCT_STATUS條件*/
		updateActAcno(isCorpCard, isNewApply, cardMajorIdPSeqno, tmpAcctType, tmpCorpPSeqno, tmpAcnoPSeqnoFromCrdCard);
		
		/* insert crd_card */
		setValue("bin_no", data1.bin);
		setValue("bin_type", tmpBinType);
		setValue("card_note", tmpCardNote);
		setValue("card_no", tmpCardNo);
		setValue("id_p_seqno", tmpIdPSeqno);
		setValue("card_type", tmpCardType);
		setValue("group_code", tmpGroupCode);
		setValue("sup_flag", tmpSupFlag);
		setValue("major_id_p_seqno", cardMajorIdPSeqno);
		setValue("major_card_no", cardMajorCardNo);
		setValue("acno_p_seqno", tmpAcnoPSeqno);
		setValue("p_seqno", tmpPSeqno);
		setValue("acno_flag", tmpAcnoFlag);
		setValue("corp_p_seqno", tmpCorpPSeqno);
		setValue("corp_no", tmpCorpNo);
		setValue("corp_no_code", tmpCorpNoCode);
		setValue("current_code", "0");
		setValue("eng_name", data1.name);
		setValue("new_beg_date", sysDate.substring(0, 6) + "01");
		setValue("new_end_date", newEndDate);
		setValue("acct_type", tmpAcctType);
		setValue("combo_indicator", tmpComboIndicator);
		
		if(data1.errRsp.equals("1000")) {
			setValue("activate_type", "O");
			setValue("activate_flag", "2");
			setValue("activate_date", sysDate);
		}else {
			setValue("activate_type", "");
			setValue("activate_flag", "1");
			setValue("activate_date", "");
		}
				
		setValue("ic_flag", tmpIcFlag);
		setValue("electronic_code", tmpElectronicCode);
		setValue("unit_code", tmpGroupCode);
		setValue("pvki", tmpPvki);
		setValue("combo_acct_no", data1.comboAcctNo);
		setValue("issue_date", sysDate);
		setValue("curr_code", tmpCurrCode);
		setValue("old_card_no", tmpOldCardNo);
		setValue("son_card_flag", tmpSonCardFlag);
		setValueInt("indiv_crd_lmt", tmpIndivCrdLmt);
		setValue("crt_date", sysDate);
		setValue("crt_user", prgmId);
		setValue("apr_date", sysDate);
		setValue("apr_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		setValue("MSG_FLAG", "Y"); //  INSERT CRD_CARD時增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
		setValueInt("MSG_PURCHASE_AMT", 0); //  INSERT CRD_CARD時增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
		daoTable = "crd_card";
		insertTable();
		if (dupRecord.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:異動碼為'A'，但卡片主檔已存在");
			errCode = "8";
			createErrReport1();
			errOutputFile1++;
			rollbackDataBase();
			return;
		}

		/* insert cca_card_base */
		setValue("card_no", tmpCardNo);
		setValue("debit_flag", "N");
		setValue("bin_type", tmpBinType);
		setValue("id_p_seqno", tmpIdPSeqno);
		setValue("major_id_p_seqno", cardMajorIdPSeqno);
		setValue("acno_p_seqno", tmpAcnoPSeqno);
		setValue("p_seqno", tmpPSeqno);
		setValue("acno_flag", tmpAcnoFlag);
		setValue("acct_type", tmpAcctType);
		if (tmpSupFlag.equals("0")) { // V1.00.02 正卡
			setValue("sup_flag", "Y");
		} else if (tmpSupFlag.equals("1")) { // V1.00.02 附卡
			setValue("sup_flag", "N");
		}
		setValue("voice_open_code", ccaVoiceOpenCode); // 生日民國年後6碼
		setValue("voice_open_code2", ccaVoiceOpenCode2); // 生日民國年後6碼
		setValue("card_acct_idx", tmpCardAcctIdx);
		setValue("dc_curr_code", tmpCurrCode);
		setValue("card_indicator", tmpCardIndicator);
		setValue("mod_user", hModUser);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		setValueDouble("mod_seqno", 0);
		daoTable = "cca_card_base";
		insertTable();
		if (dupRecord.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:異動碼為'A'，但授權卡片檔已存在");
			errCode = "9";
			createErrReport1();
			errOutputFile1++;
			rollbackDataBase();
			return;
		}
		
		/**
		* @ClassName: IcuD005
		* @Description: insert crd_seqno_log dup繼續執行
		* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
		* @Company: DXC Team.
		* @author Wilson
		* @version V1.01.65, Nov 02, 2022
		*/
		
		 setValueInt("card_type_sort" , 0);
	     setValue("bin_no"            , data1.bin);
	     setValue("SEQNO"             , data1.cardSeqno + data1.supSeqno + data1.reissueSeqno + data1.checkCode);
	     setValue("card_type"         , tmpCardType );
	     setValue("group_code"        , tmpGroupCode);
	     setValue("card_flag"         , "1" );
	     setValue("reserve"           , "Y");
	     setValue("reason_code"       , "1");
	     setValue("use_date"          , sysDate);
	     setValue("use_id"            , javaProgram);
	     setValue("card_item"         , tmpGroupCode + tmpCardType);
	     setValue("unit_code"         , tmpGroupCode);
	     setValue("trans_no"          , "");
	     setValue("seqno_old"         , data1.cardSeqno + data1.supSeqno + data1.reissueSeqno);
	     setValue("CRT_DATE"          , sysDate);
	     setValue("MOD_TIME"          , sysDate + sysTime);
	     setValue("MOD_PGM"           , javaProgram);

	     daoTable = "crd_seqno_log";

	     insertTable();

	     if (dupRecord.equals("Y")) {
	    	 if (deBug == 1)
					showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，提示:異動碼為'A'，但卡片流水號紀錄檔已存在");
//				errCode = "11";
//				createErrReport1();
//				errOutputFile1++;
//				rollbackDataBase();
//				return; 
	     }
	}
	
	/***********************************************************************/
	boolean updateActAcno(boolean isCorpCard, boolean isNewApply, String majorIdPSeqno, String acctType, String corpPSeqno, String acnoPSeqno) throws Exception {
		updateSQL =  "ACCT_STATUS = '1' , ";
		updateSQL += "MOD_USER = ?, ";      
		updateSQL += "MOD_PGM = ? , ";
		updateSQL += "MOD_TIME = sysdate ";
        daoTable  = "ACT_ACNO";
        
        setString(1, javaProgram); 
        setString(2, javaProgram);
        
        /* Update acno_status 的where條件如下
	        一般卡:
	        	(1)補發卡 -> acno_p_seqno相同
	        	(2)申請新卡 -> acct_type相同 and major_id_p_seqno相同
	        商務卡:
	        	(1)補發卡 -> 分別更新(1.1)以及(1.2) -> (1.1)acno_p_seqno相同 (1.2)acno_flag = '2' and acct_type相同 and corp_p_seqno相同
	        	(2)申請新卡 -> acno_flag = '2' and acct_type相同 and corp_p_seqno相同
        */
        StringBuilder sb = new StringBuilder();
        sb.append("where ACCT_STATUS <> '1' ");
        if (isCorpCard == false) {
        	if (isNewApply == false) {
        		sb.append(" AND ACNO_P_SEQNO = ? ");
        		setString(3, acnoPSeqno);
        	} else {
        		sb.append(" AND ID_P_SEQNO =  ?  AND ACCT_TYPE = ? ");
        		setString(3, majorIdPSeqno);
                setString(4, acctType);
        	}
        }else {
			if (isNewApply == false) {
				sb.append(" AND ( ( ACNO_P_SEQNO = ? ) OR (ACCT_TYPE = ? AND CORP_P_SEQNO = ? AND ACNO_FLAG = '2' ) )");
				setString(3, acnoPSeqno);
        		setString(4, acctType);
                setString(5, corpPSeqno);
			} else {
				sb.append(" AND ACCT_TYPE = ? AND CORP_P_SEQNO = ? AND ACNO_FLAG = '2' ");
        		setString(3, acctType);
                setString(4, corpPSeqno);
			}
        }
        
        whereStr = sb.toString();

        updateTable();
        return true;
	}

	/***********************************************************************/
	boolean updateOldCard() throws Exception {
		
		//將正卡新卡號,update到附卡之正卡卡號
		boolean result = updateSupCard();
		if (result == false) {
			return false;
		}
		
		updateSQL = "new_card_no = ? , ";
		updateSQL += "reissue_date = ?, ";      
		updateSQL += "reissue_status = '3', ";
		updateSQL += "mod_pgm = ? , ";
		updateSQL += "mod_time = sysdate ";
        daoTable  = "crd_card ";
        whereStr  = "where card_no =  ? ";
        setString(1, tmpCardNo); 
        setString(2, sysDate);
        setString(3, javaProgram);
        setString(4, tmpOldCardNo);
        updateTable();
        if (notFound.equals("Y")) {
        	if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，old_card_no = [" + tmpOldCardNo + "]，Error : 補發卡異動舊卡找不到資料");
			errCode = "17";
			createErrReport1();
			errOutputFile1++;
			return false;
        }
        
        return true;
	}
	
	/***********************************************************************/
	boolean getOldCrdCard() throws Exception {
		
		sqlCmd = "select card_no as old_card_no, ";
		sqlCmd += "son_card_flag, ";
		sqlCmd += "indiv_crd_lmt, ";
		sqlCmd += "acno_p_seqno  ";  //2022/06/07 Justin 補發新卡相關處理增加UPDATE  ACCT_STATUS
		sqlCmd += "from crd_card  ";
		sqlCmd += "where card_no like ? ";
		setString(1, data1.bin + data1.cardSeqno + data1.supSeqno + oldreissueSeqno + "%");
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:補發卡，但無舊卡資料");
			errCode = "15";
			createErrReport1();
			errOutputFile1++;
			return false;
		}
		if (recordCnt > 0) {
			tmpOldCardNo   = getValue("old_card_no");
			tmpSonCardFlag = getValue("son_card_flag");
			tmpIndivCrdLmt = getValueInt("indiv_crd_lmt");
			tmpAcnoPSeqnoFromCrdCard = getValue("acno_p_seqno"); //2022/06/07 Justin 補發新卡相關處理增加UPDATE  ACCT_STATUS
		}
		return true;
	}
	
	/***********************************************************************/
	boolean updateSupCard() throws Exception{
		if(tmpSupFlag.equals("0")) {
			String chkOldMajorCardno = "";
	        String chkSupCardno = "";

	        sqlCmd = "select major_card_no, ";
	        sqlCmd += "card_no        ";
	        sqlCmd += "from crd_card  ";
	        sqlCmd += "where major_card_no = ?   ";
	        sqlCmd += "and sup_flag      = '1' ";
	        sqlCmd += "and current_code  = '0' ";
	        setString(1, tmpOldCardNo);
	        int recordCnt = selectTable();

	        for(int i = 0 ; i < recordCnt ; i++) {
	            chkOldMajorCardno = getValue("major_card_no");
	            chkSupCardno       = getValue("card_no");

	            if (chkOldMajorCardno.compareTo(tmpCardNo) != 0) {
	                
	            	if (tmpCardNo.length() > 0) {
	            		
	            		daoTable = "crd_card";
	                    updateSQL = "major_card_no =  ? , ";  
	                    updateSQL += "mod_pgm =  ? , ";
	                    updateSQL += "mod_time = sysdate ";
	                    whereStr  = "where card_no =  ?  " + " and current_code = '0' ";
	                    setString(1, tmpCardNo);
	                    setString(2, javaProgram);
	                    setString(3, chkSupCardno);
	                    updateTable();
	                    if (notFound.equals("Y")) {
	                    	if (deBug == 1)
	            				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，sup_card_no = [" + chkSupCardno + "]，Error : 附卡異動正卡新卡號找不到資料");
	            			errCode = "16";
	            			createErrReport1();
	            			rollbackDataBase();
	            			errOutputFile1++;
	            			return false;
	                    }
	                }
	            }
	        }
		}
		return true;
	}
	
	/***********************************************************************/
	void insertCrdCorp() throws Exception {
		
		setValue("corp_p_seqno", purChaseCorpPSeqno);
		setValue("corp_no", data1.customerCode);
		setValue("chi_name", idnoChiName);
		setValue("eng_name", data1.name);
		setValue("corp_tel_zone1", idnoOfficeAreaCode1);
		setValue("corp_tel_no1", idnoOfficeTelNo1);
		setValue("corp_tel_ext1", idnoOfficeTelExt1);
		setValue("charge_id", data1.customerCode.substring(0, 10));
		setValue("charge_name", "");
		setValue("emboss_data", data1.name);
		setValue("card_since", idnoCardSince);
		setValue("reg_zip", idnoCompanyZip);
		setValue("reg_addr1", idnoCompanyAddr1);
		setValue("reg_addr2", idnoCompanyAddr2);
		setValue("reg_addr3", idnoCompanyAddr3);
		setValue("reg_addr4", idnoCompanyAddr4);
		setValue("reg_addr5", idnoCompanyAddr5);
		setValue("crt_date", sysDate);
		setValue("crt_user", prgmId);
		setValue("apr_date", sysDate);
		setValue("apr_user", prgmId);
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);

		daoTable = "crd_corp";

		insertTable();

		if (dupRecord.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:團代1599，但企業資料檔已存在");
			errCode = "23";
			createErrReport1();
			errOutputFile1++;
			rollbackDataBase();
			return;
		}
			
		return;		
	}
	
	/***********************************************************************/
	void getCorpPSeqno() throws Exception {
		selectSQL = " lpad(ecs_acno.nextval,10,'0') as corp_p_seqno ";
		daoTable = "dual";

		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_corp_p_seqno error[not find]", tmpCardNo, "");
		}
		else{
			purChaseCorpPSeqno = getValue("corp_p_seqno");
		}

		return;
	}

	/***********************************************************************/	
	void getCrdIdno() throws Exception {
		sqlCmd = "select chi_name, ";
		sqlCmd += "office_area_code1, ";
		sqlCmd += "office_tel_no1,  ";
		sqlCmd += "office_tel_ext1,  ";
		sqlCmd += "card_since,  ";
		sqlCmd += "company_zip,  ";
		sqlCmd += "company_addr1,  ";
		sqlCmd += "company_addr2,  ";
		sqlCmd += "company_addr3,  ";
		sqlCmd += "company_addr4,  ";
		sqlCmd += "company_addr5  ";
		sqlCmd += "from crd_idno  ";
		sqlCmd += "where id_no = ? ";
		setString(1, data1.customerCode.substring(0, 10));
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:團代1599，但無卡人檔資料");
			errCode = "19";
			createErrReport1();
			errOutputFile1++;
			return;
		}
		
		if (recordCnt > 0) {
			idnoChiName   = getValue("chi_name");
			idnoOfficeAreaCode1 = getValue("office_area_code1");
			idnoOfficeTelNo1 = getValue("office_tel_no1");
			idnoOfficeTelExt1 = getValue("office_tel_ext1");
			idnoCardSince = getValue("card_since");
			idnoCompanyZip = getValue("company_zip");
			idnoCompanyAddr1 = getValue("company_addr1");
			idnoCompanyAddr2 = getValue("company_addr2");
			idnoCompanyAddr3 = getValue("company_addr3");
			idnoCompanyAddr4 = getValue("company_addr4");
			idnoCompanyAddr5 = getValue("company_addr5");		
		}
		
	}
	
	/***********************************************************************/
	void insertActAcno(String acctType) throws Exception {
		
		daoTable = "act_acno";

		setValue("acno_p_seqno", purChaseAcnoPSeqno);
		setValue("p_seqno", purChaseAcnoPSeqno);
		setValue("acct_key", data1.customerCode);
		// 2022/03/08 Justin ACT_ACNO的ACCT_TYPE要改成前面撈到的tmpAcctType
//		setValue("acct_type", "06");
		setValue("acct_type", acctType);
		// 2022/03/08 Justin id_p_seqno改成一律放空白
//		setValue("id_p_seqno", purChaseIdPSeqno); 
		setValue("id_p_seqno", "");
		setValue("STMT_CYCLE", "01"); // 2022/03/10 Justin STMT_CYCLE固定放'01'
		setValue("corp_p_seqno", purChaseCorpPSeqno);
		setValue("corp_act_flag", "Y");
		setValue("acno_flag", "2");
		setValue("acct_status", "1");
		setValueDouble("line_of_credit_amt", lineOfCreditAmt == null  ? 0.0 : lineOfCreditAmt);
		setValue("apr_flag", "Y");
		setValue("apr_date", sysDate);
		setValue("apr_user", prgmId);
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("crt_user", prgmId);
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);

		insertTable();

		if (dupRecord.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:團代1599或商務卡同公司多個ACCT_TYPE，但帳戶資料檔已存在");
			errCode = "20";
			createErrReport1();
			errOutputFile1++;
			rollbackDataBase();
			return;
		}
		
		return;
	}

	/***********************************************************************/
	void insertCcaCardAcct() throws Exception {
		
		purChaseCardAcctIdx = Integer.toString(Integer.parseInt(purChaseAcnoPSeqno));
		
		daoTable = "cca_card_acct";
		
    	setValue("acno_p_seqno", purChaseAcnoPSeqno);
		setValue("p_seqno", purChaseAcnoPSeqno);
		setValue("debit_flag", "N");
		setValue("acno_flag", "2");
		setValue("id_p_seqno", purChaseIdPSeqno);
		setValue("corp_p_seqno", purChaseCorpPSeqno);
		setValue("card_acct_idx", purChaseCardAcctIdx);
		setValueDouble("jrnl_bal", jrnlBal == null ? 0.0 : jrnlBal); 
		setValueDouble("tot_amt_consume", totAmtConsume == null ? 0.0 : totAmtConsume); 
		setValue("crt_date", sysDate);
		setValue("crt_user", prgmId);
		setValue("apr_date", sysDate);
		setValue("apr_user", prgmId);
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		
		insertTable();

		if (dupRecord.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:團代1599或商務卡同公司多個ACCT_TYPE，但授權帳戶資料檔已存在");
			errCode = "21";
			createErrReport1();
			errOutputFile1++;
			rollbackDataBase();
			return;
		}
		
		return;
	}	
		
	/***********************************************************************/
	void insertCcaConsume() throws Exception {
	
		daoTable = "cca_consume";

		setValue("card_acct_idx", purChaseCardAcctIdx);
		setValue("p_seqno", purChaseAcnoPSeqno);
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		
		insertTable();

		if (dupRecord.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error:團代1599或商務卡同公司多個ACCT_TYPE，但授權卡戶帳務檔資料已存在");
			errCode = "22";
			createErrReport1();
			errOutputFile1++;
			rollbackDataBase();
			return;
		}
			
		return;
	}
	
	/***********************************************************************/
	void getAcnoPSeqno() throws Exception {
		selectSQL = " lpad(ecs_acno.nextval,10,'0') as acno_p_seqno ";
		daoTable = "dual";

		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_acno_p_seqno error[not find]", tmpCardNo, "");
		}
		else{
			purChaseAcnoPSeqno = getValue("acno_p_seqno");
		}

		return;
	}
	
	/***********************************************************************/
	boolean updateCrdCard() throws Exception {
		String cardNO = tmpCardNo;
		String engName = "";
		String newEndDate = "";
		String validDate = "";
		String newBegDate = "";
		String activateType = "";
		String activateFlag = "";
		String activateDate = "";
		String acnoPSeqno = "";
		sqlCmd = "select eng_name,new_end_date,";
		sqlCmd += " new_beg_date,activate_type,";
		sqlCmd += " activate_flag,activate_date,acno_p_seqno";
		sqlCmd += " from crd_card ";
		sqlCmd += " where card_no = ? ";
		setString(1, cardNO);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "card_no = [" + cardNO + "]，異動碼為'C',但主檔無資料，改以新增處理");
			// 若異動碼為C但UPDATE CRD_CARD找不到資料時，改成走跟異動碼為A一樣的邏輯
//			errCode = "2";
//			createErrReport1();
//			errOutputFile1++;
			return false;
		}
		if (recordCnt > 0) {
			engName = getValue("eng_name");
			newEndDate = getValue("new_end_date");
			newBegDate = getValue("new_beg_date");
			activateType = getValue("activate_type");
			activateFlag = getValue("activate_flag");
			activateDate = getValue("activate_date");
			acnoPSeqno = getValue("acno_p_seqno");
		}
		if (!engName.equals(data1.name)) {
			if (updateCrdCardEngName(data1.name, cardNO) == 1) {
				return true;
			}
		}
		validDate = "20" + data1.validDate.substring(2, 4) + data1.validDate.substring(0, 2) + "01";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate parsedDate = LocalDate.parse(validDate, formatter);
		LocalDate lastDay = parsedDate.with(TemporalAdjusters.lastDayOfMonth());
		validDate = lastDay.format(formatter);

		if (!newEndDate.equals(validDate)) {
			daoTable = "crd_card";
			updateSQL = " old_beg_date = ?,";
			updateSQL += " old_end_date = ?,";
			updateSQL += " old_activate_type = ?,";
			updateSQL += " old_activate_flag = ?,";
			updateSQL += " old_activate_date = ?,";
			updateSQL += " activate_type = ?,";
			updateSQL += " activate_flag = ?,";
			updateSQL += " activate_date = ?,";
			updateSQL += " new_beg_date = ?,";
			updateSQL += " new_end_date = ?,";
			updateSQL += " CURRENT_CODE = '0' ,";  // 2022/06/24 Justin update crd_card 新增 current_code, OPPOST_DATE, OPPOST_REASON
			updateSQL += " OPPOST_DATE = '' ,";
			updateSQL += " OPPOST_REASON = '' ,";
			updateSQL += " mod_pgm  = ? ,";
			updateSQL += " mod_time  = sysdate ";
			whereStr = " where card_no = ? ";
			setString(1, newBegDate);
			setString(2, newEndDate);
			setString(3, activateType);
			setString(4, activateFlag);
			setString(5, activateDate);
			setString(6, "");
			setString(7, "1");
			setString(8, "");
			setString(9, sysDate.substring(0, 6) + "01");
			setString(10, validDate);
			setString(11, prgmId);
			setString(12, cardNO);
			updateTable();
			if (notFound.equals("Y")) {
				if (deBug == 1)
					showLogMessage("I", "", "card_no = [" + cardNO + "]，Error : 異動碼為'C'，但主檔無資料(2)");
				errCode = "3";
				createErrReport1();
				errOutputFile1++;
				rollbackDataBase();
				return true;
			}
		}
//		// 改到CrdF071做
//		if (data1.vipCode.equals("U")) {
//			daoTable = "cca_card_base";
//			updateSQL = " spec_flag = 'N',";
//			updateSQL += " spec_status  = '09',";
//			updateSQL += " spec_date  = to_char(sysdate,'yyyymmdd'),";
//			updateSQL += " spec_time  = to_char(sysdate,'hhmmss'),";
//			updateSQL += " spec_user  = ? ";
//			whereStr = " where card_no = ? ";
//			setString(1, prgmId);
//			setString(2, cardNO);
//			updateTable();
//			if (notFound.equals("Y")) {
//				if (deBug == 1)
//					showLogMessage("I", "", "card_no = [" + cardNO + "]，Error : 停用碼為'U'，但授權卡片檔無資料");
//				errCode = "4";
//				createErrReport1();
//				errOutputFile1++;
//				rollbackDataBase();
//				return true;
//			}		
//		}
//		
//		if (data1.vipCode.equals("Q")) {
//			daoTable = "crd_card";
//			updateSQL = " current_code = '1',";
//			updateSQL += " oppost_date  = to_char(sysdate,'yyyymmdd'),";
//			updateSQL += " oppost_reason  = 'Q2',";
//			updateSQL += " mod_pgm  = ?,";
//			updateSQL += " mod_time  = sysdate ";
//			whereStr = " where card_no = ? ";
//			setString(1, prgmId);
//			setString(2, cardNO);
//			updateTable();
//			if (notFound.equals("Y")) {
//				if (deBug == 1)
//					showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error : 停用碼為'Q'，但卡片檔無資料");
//				errCode = "13";
//				createErrReport1();
//				errOutputFile1++;
//				rollbackDataBase();
//				return true;
//			}		
//		}
		
        if(data1.errRsp.equals("1999")) {
        	showLogMessage("I", "", "cardlink回覆碼 = 1999，執行強制關卡");
        	daoTable = "crd_card";
        	updateSQL += " current_code  = ?,";
			updateSQL += " oppost_reason = ?,";
			updateSQL += " oppost_date   = ?,";
			updateSQL += " activate_type = ?,";
			updateSQL += " activate_flag = ?,";
			updateSQL += " activate_date = ?,";
			updateSQL += " mod_pgm  = ? ,";
			updateSQL += " mod_time  = sysdate ";
			whereStr = " where card_no = ? ";
			setString(1, "0");
			setString(2, "");
			setString(3, "");
			setString(4, "");
			setString(5, "1");
			setString(6, "");
			setString(7, prgmId);
			setString(8, cardNO);
			updateTable();
			if (notFound.equals("Y")) {
				if (deBug == 1)
					showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error : 異動碼為'C'，但主檔無資料(3)");
				errCode = "12";
				createErrReport1();
				errOutputFile1++;
				rollbackDataBase();
				return true;
			}			
		}
		if (data1.vipCode.equals("F")) {
			showLogMessage("I", "", "cardlink停用碼 = F，執行同步停用");
			daoTable = "crd_card";
			updateSQL += " current_code  = ?,";
			updateSQL += " oppost_reason = ?,";
			updateSQL += " oppost_date   = ?,";
			updateSQL += " mod_pgm  = ? ,";
			updateSQL += " mod_user  = ? ,";
			updateSQL += " mod_time  = sysdate ";
			whereStr = " where card_no = ? and current_code = '0' ";
			setString(1, "5");
			setString(2, "M2");
			setString(3, sysDate);
			setString(4, prgmId);
			setString(5, prgmId);
			setString(6, cardNO);
			updateTable();

			if (notFound.equals("Y")) {
				if (deBug == 1)
					showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，Error : 停用碼為'Ｆ'，但卡片檔無活卡資料");
				errCode = "26";
				createErrReport1();
				errOutputFile1++;
				rollbackDataBase();
				return true;
			}
			ccaOutGoing.InsertCcaOutGoing(cardNO, "5", sysDate, "M2");
		}

        return true;
	}

	/***********************************************************************/
	int updateCrdCardEngName(String engName, String cardNo) throws Exception {
		daoTable = "crd_card";
		updateSQL = " eng_name = ?,";
		updateSQL += " mod_pgm  = ? ,";
		updateSQL += " mod_time  = sysdate ";
		whereStr = " where card_no = ? ";
		setString(1, engName);
		setString(2, prgmId);
		setString(3, cardNo);
		updateTable();
		if (notFound.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "card_no = [" + cardNo + "]，Error : 異動碼為'C'，但主檔無資料(2)");
			errCode = "3";
			createErrReport1();
			errOutputFile1++;
			rollbackDataBase();
			return 1;
		}
		return 0;
	}

	/***********************************************************************/
	void insertHceCard() throws Exception {
		String tmpHceIdPSeqno = "";
		String tmpHceAcnoPSeqno = "";
		String tmpHceIdNo = "";
		String validDate = ""; // yyyymm
		String validDateLast = ""; // yyyymmdd最後一天
		validDate = "20" + data2.validDate.substring(2, 4) + data2.validDate.substring(0, 2);
		validDateLast = validDate + "01";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate parsedDate = LocalDate.parse(validDateLast, formatter);
		LocalDate lastDay = parsedDate.with(TemporalAdjusters.lastDayOfMonth());
		validDateLast = lastDay.format(formatter);
		sqlCmd = "select id_p_seqno,acno_p_seqno ";
		sqlCmd += " from crd_card ";
		sqlCmd += " where card_no = ? ";
		setString(1, data2.primaryCardNo);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "v_card_no = [" + data2.tpanCardNo + "]，Error:HCE卡在信用卡卡片檔無資料");
			errCode = "3";
			createErrReport2();
			errOutputFile2++;
			return;
		}
		if (recordCnt > 0) {
			tmpHceIdPSeqno = getValue("id_p_seqno");
			tmpHceAcnoPSeqno = getValue("acno_p_seqno");
		}

		sqlCmd = "select id_no ";
		sqlCmd += " from crd_idno ";
		sqlCmd += " where id_p_seqno = ? ";
		setString(1, tmpHceIdPSeqno);
		recordCnt = selectTable();
		if (notFound.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "v_card_no = [" + data2.tpanCardNo + "]，Error:HCE卡在信用卡卡人檔無資料");
			errCode = "4";
			createErrReport2();
			errOutputFile2++;
			return;
		}
		if (recordCnt > 0) {
			tmpHceIdNo = getValue("id_no");
		}
		/* insert hce_card */
		setValue("v_card_no", data2.tpanCardNo);
		setValue("card_no", data2.primaryCardNo);
		setValue("status_code", "0");
		setValue("crt_date", sysDate);
		setValue("new_end_date", validDateLast);
		setValue("change_date", "");
		setValue("sir_user", "TWMP");
		setValue("id_p_seqno", tmpHceIdPSeqno);
		setValue("acno_p_seqno", tmpHceAcnoPSeqno);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		daoTable = "hce_card";
		insertTable();
		if (dupRecord.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "v_card_no = [" + data2.tpanCardNo + "]，Error:異動碼為'A'，但HCE卡片主檔已存在");
			errCode = "5";
			createErrReport2();
			errOutputFile2++;
			rollbackDataBase();
			return;
		}
	}

	/***********************************************************************/
	void updateHceCard() throws Exception {
		String newEndDate = "";
		String statusCode = "";
		String validDate = ""; // yyyymm
		String validDateLast = ""; // yyyymmdd最後一天
		validDate = "20" + data2.validDate.substring(2, 4) + data2.validDate.substring(0, 2);
		validDateLast = validDate + "01";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate parsedDate = LocalDate.parse(validDateLast, formatter);
		LocalDate lastDay = parsedDate.with(TemporalAdjusters.lastDayOfMonth());
		validDateLast = lastDay.format(formatter);
		sqlCmd = "select new_end_date ";
		sqlCmd += " from hce_card ";
		sqlCmd += " where v_card_no = ? ";
		setString(1, data2.tpanCardNo);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			if (deBug == 1)
				showLogMessage("I", "", "v_card_no = [" + data2.tpanCardNo + "]，Error : 異動碼為'C'，但HCE卡片主檔無資料(1)");
			errCode = "1";
			createErrReport2();
			errOutputFile2++;
			return;
		}
		if (recordCnt > 0) {
			newEndDate = getValue("new_end_date");
			if (!newEndDate.equals(validDate)) {
				daoTable = "hce_card";
				updateSQL = " new_end_date = ?,";
				updateSQL += " mod_pgm  = ? ,";
				updateSQL += " mod_time  = sysdate ";
				whereStr = " where v_card_no = ? ";
				setString(1, validDateLast);
				setString(2, prgmId);
				setString(3, data2.tpanCardNo);
				updateTable();
				if (notFound.equals("Y")) {
					if (deBug == 1)
						showLogMessage("I", "",
								"v_card_no = [" + data2.tpanCardNo + "]，Error : 異動碼為'C'，但HCE卡片主檔無資料(1)");
					errCode = "1";
					createErrReport2();
					errOutputFile2++;
					return;
				}
			}
		}
		if (data2.vipCode.equals("U") || data2.vipCode.equals("L") || data2.vipCode.equals("C")
				|| data2.vipCode.equals("F") || data2.vipCode.equals("Q")) {
			if (data2.vipCode.equals("U")) {
				statusCode = "1";
			} else {
				statusCode = "3";
			}
			daoTable = "hce_card";
			updateSQL = " status_code = ?,";
			updateSQL += " change_date = to_char(sysdate,'yyyymmdd'),";
			updateSQL += " mod_pgm  = ? ,";
			updateSQL += " mod_time  = sysdate ";
			whereStr = " where v_card_no = ? ";
			setString(1, statusCode);
			setString(2, prgmId);
			setString(3, data2.tpanCardNo);
			updateTable();
			if (notFound.equals("Y")) {
				if (deBug == 1)
					showLogMessage("I", "", "v_card_no = [" + data2.tpanCardNo + "]，Error : 異動碼為'C'，但HCE卡片主檔無資料(2)");
				errCode = "2";
				createErrReport2();
				errOutputFile2++;
				rollbackDataBase();
				return;
			}
		}

	}

	/***********************************************************************/
	void insertFileCtl1(String fileName) throws Exception {

		setValue("file_name", fileName);
		setValue("crt_date", sysDate);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
	}

	/***********************************************************************/
	void procFTP(String hNcccFilename) throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + hNcccFilename + " 開始傳送....");
	      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + hNcccFilename);
	      
	      if (err_code != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + hNcccFilename + " 資料"+" errcode:"+err_code);
	          insertEcsNotifyLog(hNcccFilename);          
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
	void insertCrdIdnoSeqno(String idPSeqno) throws Exception {
		setValue("id_no", data1.customerCode.substring(0, 10));
		setValue("id_p_seqno", idPSeqno);
		setValue("id_flag", "");		
    	setValue("bill_apply_flag", "");
    	setValue("debit_idno_flag", "N");
		daoTable = "crd_idno_seqno";
		insertTable();
	}

	/***********************************************************************/
	void insertCrdIdno(String idPSeqno) throws Exception {
		setValue("id_no", data1.customerCode.substring(0, 10));
		setValue("id_no_code", "0");
		setValue("id_p_seqno", idPSeqno);
		setValue("crt_date", sysDate);
		setValue("crt_user", prgmId);
		setValue("apr_date", sysDate);
		setValue("apr_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		daoTable = "crd_idno";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_crd_idno error", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void insertFileCtl(String filename) throws Exception {
		setValue("file_name", filename);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", hRecCnt1);
		setValueInt("record_cnt", hRecCnt1);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = "head_cnt = ?,";
			updateSQL += " record_cnt = ?,";
			updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name = ? ";
			setInt(1, hRecCnt1);
			setInt(2, hRecCnt1);
			setString(3, filename);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
		commitDataBase();
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IcuD005 proc = new IcuD005();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String modNo;
		String idnoId;
		String cardNo;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(modNo, 1);
			rtn += fixLeft(idnoId, 11);
			rtn += fixLeft(cardNo, 16);
			rtn += fixLeft(errReason, 200);
			rtn += fixLeft(date, 8);
			return rtn;
		}

		String fixLeft(String str, int len) throws UnsupportedEncodingException {
			String spc = "";
			for (int i = 0; i < 100; i++)
				spc += " ";
			if (str == null)
				str = "";
			str = str + spc;
			byte[] bytes = str.getBytes("MS950");
			byte[] vResult = new byte[len];
			System.arraycopy(bytes, 0, vResult, 0, len);

			return new String(vResult, "MS950");
		}
	}

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		ncccData1.modNo = comc.subMS950String(bytes, 0, 1);
		ncccData1.idnoId = comc.subMS950String(bytes, 1, 12);
		ncccData1.cardNo = comc.subMS950String(bytes, 12, 28);
		ncccData1.errReason = comc.subMS950String(bytes, 28, 228);
		ncccData1.date = comc.subMS950String(bytes, 228, 236);
	}

	/***********************************************************************/
	class Buf2 {
		String modNo;
		String tpanCardNo;
		String primaryCardNo;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(modNo, 1);
			rtn += fixLeft(tpanCardNo, 16);
			rtn += fixLeft(primaryCardNo, 16);
			rtn += fixLeft(errReason, 200);
			rtn += fixLeft(date, 8);
			return rtn;
		}

		String fixLeft(String str, int len) throws UnsupportedEncodingException {
			String spc = "";
			for (int i = 0; i < 100; i++)
				spc += " ";
			if (str == null)
				str = "";
			str = str + spc;
			byte[] bytes = str.getBytes("MS950");
			byte[] vResult = new byte[len];
			System.arraycopy(bytes, 0, vResult, 0, len);

			return new String(vResult, "MS950");
		}
	}

	void splitBuf2(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		ncccData2.modNo = comc.subMS950String(bytes, 0, 1);
		ncccData2.tpanCardNo = comc.subMS950String(bytes, 1, 17);
		ncccData2.primaryCardNo = comc.subMS950String(bytes, 17, 33);
		ncccData2.errReason = comc.subMS950String(bytes, 33, 233);
		ncccData2.date = comc.subMS950String(bytes, 233, 241);
	}

	/****************************************************************************/
	String fixAllLeft(String str, int len) throws UnsupportedEncodingException {
		String spc = "";
		for (int i = 0; i < 100; i++)
			spc += "　";
		if (str == null)
			str = "";
		str = str + spc;
		byte[] bytes = str.getBytes("MS950");
		byte[] vResult = new byte[len];
		System.arraycopy(bytes, 0, vResult, 0, len);
		return new String(vResult, "MS950");
	}

	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/****************************************************************************/
	String[] getFieldValue(String rec, int[] parm) {
		int x = 0;
		int y = 0;
		byte[] bt = null;
		String[] ss = new String[parm.length];
		try {
			bt = rec.getBytes("MS950");
		} catch (Exception e) {
			showLogMessage("I", "", comc.getStackTraceString(e));
		}
		for (int i : parm) {
			try {
				ss[y] = new String(bt, x, i, "MS950");
			} catch (Exception e) {
				showLogMessage("I", "", comc.getStackTraceString(e));
			}
			y++;
			x = x + i;
		}
		return ss;
	}

	/****************************************************************************/
	private Map processDataRecord(String[] row, String[] DT) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		for (String s : DT) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}

	/***********************************************************************/
	class NormalCardLayout extends hdata.BaseBin {
		public String modCode = ""; // 異動碼
		public String issuerCode = ""; // 發卡單位代號
		public String cardType = ""; // 卡別代號
		public String customerCode = ""; // 客戶代號 第11碼為延伸碼
		public String bin = ""; // 發卡單位BIN
		public String cardSeqno = ""; // 卡片流水號
		public String supSeqno = ""; // 正附卡序號
		public String reissueSeqno = ""; // 補發卡序號
		public String checkCode = ""; // 卡片檢查號
		public String validDate = ""; // 有效日期
		public String name = ""; // 凸字姓名
		public String cvc1 = ""; // VISA CVV /MasterCard CVC1
		public String cvc2 = ""; // VISA CVV II/MasterCard CVC2
		public String vipCode = ""; // 停用碼/VIP碼
		public String blackArea = ""; // 黑名單地區別及JCB停掛異動碼
		public String blackDate = ""; // 黑名單或停掛檔取消日期
		public String errRsp = ""; // 錯誤回覆碼
		public String comboAcctNo = ""; // COMBO金融卡帳號

		@Override
		public void initData() {
			modCode = "";
			issuerCode = "";
			cardType = "";
			customerCode = "";
			bin = "";
			cardSeqno = "";
			supSeqno = "";
			reissueSeqno = "";
			checkCode = "";
			validDate = "";
			name = "";
			cvc1 = "";
			cvc2 = "";
			vipCode = "";
			blackArea = "";
			blackDate = "";
			errRsp = "";
			comboAcctNo = "";
		}

	}

	/***********************************************************************/
	class TPanCardLayout extends hdata.BaseBin {
		public String modCode = ""; // 異動碼 ACD
		public String tpanFlag = ""; // TPAN註記 固定為T
		public String tpanName = ""; // TPAN名稱/代號
		public String tpanCardNo = ""; // TPAN卡號
		public String validDate = ""; // 有效日期
		public String primaryCardNo = ""; // 主卡卡號
		public String primaryValidDate = ""; // 主卡效期
		public String space = ""; // 保留
		public String vipCode = ""; // 停用碼/VIP碼
		public String blackArea = ""; // 黑名單地區別及JCB停掛異動碼
		public String blackDate = ""; // 黑名單或停掛檔取消日期
		public String errRsp = ""; // 錯誤回覆碼
		public String comboAcctNo = ""; // COMBO金融卡帳號

		@Override
		public void initData() {
			modCode = "";
			tpanFlag = "";
			tpanName = "";
			tpanCardNo = "";
			validDate = "";
			primaryCardNo = "";
			primaryValidDate = "";
			space = "";
			vipCode = "";
			blackArea = "";
			blackDate = "";
			errRsp = "";
			comboAcctNo = "";
		}

	}
}

/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  110/02/24  V1.00.00   Wilson      program initial                          *
 *  110/03/03  V1.00.01   Wilson      因變更ID程式執行順序調整，營業日減一日                                        *
 *  110/03/10  V1.00.02   Wilson      錯誤原因新增 -> 金融帳號已存在且ID與資料庫不同                         *
 *  110/11/25  V1.00.03   Justin      ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX        *
 *  110/12/08  V1.00.04   Wilson      錯誤訊息調整                                                                                          *
 *  110/12/16  V1.00.05   Wilson      insert cca_card_base新增card_indicator    *
 *  111/01/21  V1.00.06   Justin      增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0) *
 *  111/02/14  V1.00.07    Ryan      big5 to MS950                                           *
 *  111/02/15  V1.00.08   Justin      shorten the length of English name       *
 *  111/02/17  V1.00.09   Justin       show no found messages                   *
 *  111/02/18  V1.00.10   Justin      fix the error causing by split phoneNo    *
 *  111/03/01  V1.00.11   Justin      update dba_acno act_no if 金融帳號已存在且ID與資料庫不同 *
 *  111/03/02  V1.00.12   Justin       增加顯示訊息                             *
 *  111/03/03  V1.00.13   Justin       新金融帳號使用舊ID串接                   *
 *  111/05/11  V1.00.14   Justin       update dba_acno增加acct_holder_id        *
 ******************************************************************************/
package Icu;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;


public class IcuD067 extends AccessDAO {
	private final String progname = "設一科VD數位存款製卡檔處理程式  111/05/11  V1.00.14";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;
	VDCardLayout data = new VDCardLayout();
	int debug = 0;
	String prgmId = "IcuD067";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	// col詳VD製卡檔格式
	protected final String dT1Str = " col1 ,col2 ,col3 ,col4 ,col5 ,col6 ,col7 ,col8 ,col9 ,col10 ,col11 ,col12 ,col13 ,col14 ,col15"
			+ " ,col16 ,col17 ,col18 ,col19 ,col20 ,col21 ,col22 ,col23 ,col24 ,col25 ,col26 ,col27 ,col28 ,col29 ,col30"
			+ " ,col31 ,col32 ,col33 ,col34 ,col35 ,col36 ,col37 ,col38 ,col39 ,col40 ,col41 ,col42 ,col43 ,col44 ,col45"
			+ " ,col46 ,col47 ,col48 ,col49 ,col50 ,col51 ,col52 ,col53 ,col54 ,col55 ,col56 ,col57 ,col58 ,col59 ,col60 "
			+ " ,col61 ,col62 ,col63 ,col64 ,col65 ,col66 ,col67 ,col68 ,col69 ,col70 ,col71 ,col72 ,col73 ,col74 ,col75"
			+ " ,col76 ,col77 ,col78 ,col79 ,col80 ,col81 ,col82 ,col83 ,col84 ,col85 ,col86 ,col87 ,col88 ,col89 ,col90"
			+ " ,col91 ,col92 ,col93 ,col94 ,col95 ,col96 ,col97 ,col98 ,col99 ,col100 ,col101 ,col102 ,col103 ,col104 ,col105"
			+ " ,col106 ,col107 ,col108 ,col109 ,col110 ,col111 ,col112 ,col113 ,col114 ,col115 ,col116 ,col117 ,col118 ,col119 ,col120"
			+ " ,col121 ,col122 ,col123 ,col124 ,col125 ,col126 ,col127 ,col128 ,col129 ,col130 ,col131 ,col132 ,col133 ,col134 ,col135"
			+ " ,col136 ,col137 ,col138 ,col139 ,col140 ,col141 ,col142 ,col143 ,col144 ,col145 ,col146 ,col147 ,col148 ,col149 ,col150"
			+ " ,col151 ,col152 ,col153 ,col154 ,col155 ,col156 ,col157 ,col158 ,col159 ,col160 ,col161 ,col162 ,col163 ,col164 ,col165"
			+ " ,col166 ,col167 ,col168 ,col169 ,col170 ,col171 ,col172 ,col173 ,col174 ,col175 ,col176 ,col177 ,col178 ,col179 ,col180"
			+ " ,col181 ,col182 ,col183 ,col184 ,col185 ,col186 ,col187 ,col188 ,col189 ,col190 ,col191 ,col192 ,col193 ,col194 ,col195"
			+ " ,col196 ,col197 ,col198 ,col199 ,col200 ,col201 ,col202 ,col203 ,col204 ,col205 ,col206 ,col207 ,col208 ,col209";

	protected final int[] dt1Length = { 1, 7, 1, 13, 1, 10, 1, 2, 1, 37, 1, 2, 104, 1, 8, 20, 2, 8, 10, 8, 3, 3, 1, 8,
			8, 60, 16, 16, 16, 16, 16, 16, 16, 16, 24, 24, 24, 24, 24, 24, 24, 24, 16, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2,
			2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2,
			2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 1, 4, 8, 2, 2, 2, 2, 33, 4, 96, 6, 1, 1, 19, 1, 2, 1, 2, 5,
			2, 1, 2, 2, 1, 1, 28, 1, 19, 1, 3, 1, 1, 1, 16, 1, 26, 1, 4, 3, 1, 4, 8, 2, 3, 2, 1, 3, 1, 1, 16, 1, 4, 3,
			1, 4, 3, 5, 1, 11, 7, 1, 1, 4, 3, 3, 3, 23, 1, 4, 1, 1, 3, 3, 1, 41, 3, 5, 10, 1, 10, 1, 16, 33, 26, 5, 18,
			18, 9, 9, 9, 9, 2, 4, 4, 4, 33, 3, 33, 3, 97, 3, 1, 3 };

	String fileDate = "";
	int rptSeq1 = 0;
	String buf = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String hNcccFilename = "";
	String tmpBatchno = "";
	int hRecCnt1 = 0;
	String getFileName;
	String outFileName;
	int totalInputFile;
	int totalOutputFile;
	String errCode;
	String tmpIdPSeqno = "";
	String tmpPSeqno = "";
	String tmpAcctKey = "";
	String tmpAcctType = "";
	String tmpCardIndicator = "";
	String tmpGroupCode = "";
	String tmpUnitCode = "";
	String tmpSourceCode = "";
	String tmpBinType = "";
	String tmpicFlag = "";
	String tmpElectronicCode = "";
	String tmpCardType = "";
	String tmpCardAcctIdx = "";
	String idPSeqno = "";
	String pPseqAcnoSeqno = "";
	String dbcCardExistCode;
	String cardBaseExistCode;
	String tmpOldBegDate = "";
	String tmpOldEndDate = "";
	String tmpOldActivateFlag = "";
	String tmpOldActivateType = "";
	String tmpOldActivateDate = "";
	String hBusiBusinessDate = "";
	
	protected String[] dT1 = new String[] {};
	Buf1 ncccData1 = new Buf1();

	public int mainProcess(String[] args) {

		try {
			dT1 = dT1Str.split(",");
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length > 1) {
				comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
			}
			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comsecr = new CommSecr(getDBconnect(), getDBalias());
			
			selectPtrBusinday();
			
			if (args.length == 0) {
				fileDate = hBusiBusinessDate;
			} else if (args.length == 1) {
				fileDate = args[0];
			}
			if (fileDate.length() != 8) {
				comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
			}
			
			hModUser = comc.commGetUserID();
			
			if (checkPtrHoliday() != 0) {
				exceptExit = 0;
				comc.errExit("今日為假日,不執行此程式", "");
	        }
			
			openFile();
			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束,[" + totalInputFile + "],[" + totalOutputFile + "]");
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

//		sqlCmd = " select business_date ";
		sqlCmd = " select to_char(to_date(business_date,'yyyymmdd')-1 days,'yyyymmdd') h_temp_business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		hBusiBusinessDate = getValue("h_temp_business_date");
	}

	/************************************************************************/
	  int checkPtrHoliday() throws Exception {
	      int holidayCount = 0;

	      sqlCmd = "select count(*) holidayCount ";
	      sqlCmd += " from ptr_holiday  ";
	      sqlCmd += "where holiday = ? ";
	      setString(1, fileDate);
	      int recordCnt = selectTable();      
	      if (notFound.equals("Y")) {
	          comc.errExit("select_ptr_holiday not found!", "");
	      }
	      if (recordCnt > 0) {
	          holidayCount = getValueInt("holidayCount");
	      }

	      if (holidayCount > 0) {
	          return 1;
	      } else {
	          return 0;
	      }
	  }

	  /***********************************************************************/
	int openFile() throws Exception {
		int fileCount = 0;

		String tmpstr = String.format("%s/media/icu", comc.getECSHOME());
		List<String> listOfFiles = comc.listFS(tmpstr, "", "");

		for (String file : listOfFiles) {
			getFileName = file;
			if (getFileName.length() != 31)
				continue;
			if (!getFileName.substring(0, 19).equals("VD_RS_DGT_MAKECARD_"))
				continue;
			
			if (!getFileName.substring(19, 27).equals(fileDate))
				continue;
			
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(getFileName);
		}
		if (fileCount < 1) {
//			showLogMessage("I", "", "無檔案可處理");
			
			comcr.hCallErrorDesc = "Error : 無檔案可處理";
			comcr.errRtn("Error : 無檔案可處理", "處理日期 = " + fileDate , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	int readFile(String fileName) throws Exception {
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

		sqlCmd = "select max(batchno) as tmp_batchno";
		sqlCmd += " from dbc_emboss ";
		int recordCnt = selectTable();
		tmpBatchno = getValue("tmp_batchno");
		if (tmpBatchno.subSequence(0, 8).equals(sysDate)) {
			tmpBatchno = comc.str2int(tmpBatchno) + 1 + "";
		} else {
			tmpBatchno = sysDate.substring(2, 8) + "01";
		}
		while (true) {
			rec = readTextFile(fi); // read file data
//      System.out.println(rec.length()+"rec");
			if (endFile[fi].equals("Y"))
				break;
//      if (rec.length() == 1700) {
//        System.out.println(1700+"rec");
			errCode = "";
			totalInputFile++;
			moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1));
			processDisplay(1000);
//      }
		}

		if (totalOutputFile > 0) {
			outPutTextFile();
			comc.writeReport(outFileName, lpar1, "MS950");
			hRecCnt1 = totalOutputFile;
			insertFileCtl(hNcccFilename);
			lpar1.clear();
			
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP();
		    renameFile1(hNcccFilename);
		}

		closeInputText(fi);
		hRecCnt1 = totalInputFile;
		insertFileCtl(fileName);

		renameFile(fileName);

		return 0;
	}

	/***********************************************************************/
	private void moveData(Map<String, Object> map) throws Exception {
		data.initData();
		data.cardNo = (String) map.get("col120"); // 卡號(每四位間隔一空白)
		data.cardNo = data.cardNo.substring(0, 4) + data.cardNo.substring(5, 9) + data.cardNo.substring(10, 14)
				+ data.cardNo.substring(15, 19);
		data.cardNo = data.cardNo.trim();
		if (debug == 1)
			System.out.println("卡號=" + data.cardNo);

		data.validFmYy = (String) map.get("col124"); // 有效起始年份
		data.validFmYy = data.validFmYy.trim();
		if (debug == 1)
			System.out.println("有效起始年份=" + data.validFmYy);

		data.validFmMm = (String) map.get("col122"); // 有效起始月份
		data.validFmMm = data.validFmMm.trim();
		if (debug == 1)
			System.out.println("有效起始月份=" + data.validFmMm);

		data.validToYy = (String) map.get("col128"); // 有效迄止年份
		data.validToYy = data.validToYy.trim();
		if (debug == 1)
			System.out.println("有效迄止年份=" + data.validToYy);

		data.validToMm = (String) map.get("col126"); // 有效迄止月份
		data.validToMm = data.validToMm.trim();
		if (debug == 1)
			System.out.println("有效迄止月份=" + data.validToMm);

		data.actNo = (String) map.get("col4"); // 帳號
		data.actNo = data.actNo.trim();
		if (debug == 1)
			System.out.println("帳號=" + data.actNo);

		data.idNo = (String) map.get("col186"); // ID
		data.idNo = data.idNo.trim();
		if (debug == 1)
			System.out.println("IdNo=" + data.idNo);

		data.birthday = (String) map.get("col166"); // 出生日期
		data.birthday = data.birthday.trim();
		data.birthday = String.format("%4d", Integer.valueOf(data.birthday.substring(0, 3)) + 1911) + ""
				+ data.birthday.substring(3, 5) + "" + data.birthday.substring(5, 7);
		if (debug == 1)
			System.out.println("出生日期=" + data.birthday);

		data.name = (String) map.get("col189"); // 中文姓名
		data.name = data.name.trim();
		if (debug == 1)
			System.out.println("中文姓名=" + data.name);

		data.engName = (String) map.get("col190"); // 英文姓名
		data.engName = data.engName.length() > 25 ? data.engName.substring(0,25) : data.engName;
		data.engName = data.engName.trim();
		if (debug == 1)
			System.out.println("英文姓名=" + data.engName);

		data.homePhone = (String) map.get("col192"); // 住家電話
		data.homePhone=data.homePhone.trim();
//		String[]  homePhone=data.homePhone.split("-");
		String[]  homePhone = comm.getTelZoneAndNo(data.homePhone);
		
		if (homePhone.length == 3) {
			data.homeAreaCode = homePhone[0].trim();
			if (debug == 1)
				System.out.println("住家電話區碼=" + data.homeAreaCode);

			data.homeTelNo = homePhone[1].trim();
			if (debug == 1)
				System.out.println("住家電話=" + data.homeTelNo);
		}else {
			data.homeAreaCode = "";
			data.homeTelNo = "";
		}

//		if(homePhone.length==2) {
//	        data.homeTelNo = "";
//	        if (debug == 1)
//	            System.out.println("住家電話=" + data.homeTelNo);
//		}
//		else {
//		    data.homeTelExt = homePhone[2].trim();
//		    if (debug == 1)
//			    System.out.println("住家電話分機=" + data.homeTelExt);
//		}
		
		
		data.officePhone = (String) map.get("col193"); // 公司電話
		data.officePhone=data.officePhone.trim();
//        String[]  officePhone=data.officePhone.split("-");
		String[]  officePhone= comm.getTelZoneAndNo(data.officePhone);

		if(officePhone.length==3)
		{ 
			data.officeAreaCode = officePhone[0].trim();
			if (debug == 1)
				System.out.println("公司電話區碼=" + data.officeAreaCode);

			data.officeTelNo = officePhone[1].trim();
			if (debug == 1)
				System.out.println("公司電話=" + data.officeTelNo);
			
		    data.officeTelExt = officePhone[2].trim(); 
	        if (debug == 1)
	            System.out.println("公司電話分機=" + data.officeTelExt);
		}
		else
		{
			data.officeAreaCode = "";
			data.officeTelNo = "";
			data.officeTelExt = "";
		}
	

		data.cardNumber = (String) map.get("col8"); // 卡片序號
		data.cardNumber = data.cardNumber.trim();
		if (debug == 1)
			System.out.println(" 卡片序號=" + data.cardNumber);
		// 讀取檔案
		if(selectDbaAcno() != 0)
			return;
		
		selectDbcCard();
		
		selectCcaCardBase();

		if(dbcCardExistCode.equals("Y")&&cardBaseExistCode.equals("N"))
		{
			showLogMessage("I", "", String.format("Error:該卡號[%s]已存在VD卡片資料檔中，但不存在授權卡片資料檔中", data.cardNo));
			errCode = "1";
			createErrReport();
			totalOutputFile++;
			return;
		}
		else if(dbcCardExistCode.equals("N")&&cardBaseExistCode.equals("Y"))
		{
			showLogMessage("I", "", String.format("Error:該卡號[%s]已存在授權卡片資料檔中，但不存在VD卡片資料檔中", data.cardNo));
		    errCode = "2";
		    createErrReport();
		    totalOutputFile++;
		    return;
		}
		else if(dbcCardExistCode.equals("Y")&&cardBaseExistCode.equals("Y"))
		{	
			updateDbcCard();
		}
		else 
		{
			// 檢核資料
			selectCrdCardnoRange();
			// insert或update dbc_idno
			updateDbcIdno();
			// insert dba_acno、cca_card_acct
			insertDbaAcno();
			// insert dbc_card
			insertDbcCard();
			// insert cca_card_base
			insertCcaCardBase();			
			// insert crd_seqno_log
			insertCrdSeqnoLog();
		}
		
		commitDataBase();
		return;
	}

	/***********************************************************************/
	int outPutTextFile() throws Exception {
//		int fileNo = 0;
//		
//		sqlCmd  = "select max(substr(file_name, 28, 2)) file_no";
//        sqlCmd += " from crd_file_ctl  ";
//        sqlCmd += " where file_name like ?";
//        sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
//        setString(1, "VD_RS_DGT_MAKECARD_ERR." + "%" + ".TXT");
//        
//        if (selectTable() > 0) 
//        	fileNo = getValueInt("file_no");
		
		hNcccFilename = String.format("VD_RS_DGT_MAKECARD_ERR_%s.TXT", fileDate);
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		outFileName = String.format("%s/media/icu/error/%s", comc.getECSHOME(), hNcccFilename);
		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

		return 0;
	}

	// 產生錯誤報表檔
	void createErrReport() throws Exception {

		ncccData1 = new Buf1();
		ncccData1.cardNo = data.cardNo;
		ncccData1.idNo = data.idNo;
		ncccData1.actNo = data.actNo;
		switch (errCode) {
		case "1":
			ncccData1.errReason = String.format("%-200s", "該卡號已存在VD卡片資料檔中，但不存在授權卡片資料檔中");
			break;
		case "2":
			ncccData1.errReason = String.format("%-200s", "該卡號已存在授權卡片資料檔中，但不存在VD卡片資料檔中");
			break;
		case "3":
			ncccData1.errReason = String.format("%-200s", "金融帳號已存在且ID與資料庫不同");
			break;			
		}

		ncccData1.date = sysDate;
		buf = ncccData1.allText();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}

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
	// 1.確認該金融帳號是否已存在dba_acno，且ID與資料庫的值不同
	int selectDbaAcno() throws Exception {
		String idPSeqno1 = "";
		String idNo1 = "";
		
		sqlCmd = "select id_p_seqno, p_seqno as dbaAcnoPSeqno ";
		sqlCmd += " from dba_acno ";
		sqlCmd += " where acct_no = ? ";
		setString(1, data.actNo);
		int cnt = selectTable();
		if (cnt > 0) {
			idPSeqno1 = getValue("id_p_seqno");
			
			sqlCmd = "select id_no ";
			sqlCmd += " from dbc_idno ";
			sqlCmd += " where id_p_seqno = ? ";
			setString(1, idPSeqno1);
			cnt = selectTable();
			if(cnt > 0) {
				idNo1 = getValue("id_no");
			}
									
			if(!data.idNo.equals(idNo1)) {
//				showLogMessage("I", "", String.format("Error:金融帳號[%s]已存在且ID[%s]與資料庫不同", data.actNo, data.idNo));
//				errCode = "3";
//				createErrReport();
//				totalOutputFile++;
//				return 1;
				
				showLogMessage("I", "", String.format("卡號[%s],金融帳號[%s]已存在且ID[%s]與資料庫[%s]不同，將已存在之金融帳號變更處理",data.cardNo, data.actNo, data.idNo, idNo1));
				// UPDATE 原本存在的那筆DBA_ACNO的ACCT_NO為p_seqno最後三碼+ID(10碼)
				updateDbaAcnoAcctNo(data.actNo, getValue("dbaAcnoPSeqno"), idNo1, data.idNo);
			}
		}
		return 0;
	}
	
	private void updateDbaAcnoAcctNo(String actNo, String pSeqno, String idNo, String idNoFromData) throws Exception {
		String lastThreePSeqno = pSeqno;
		if (pSeqno.length() > 3) {
			lastThreePSeqno = pSeqno.substring(pSeqno.length()-3, pSeqno.length());
		}
		String newAcctNo = lastThreePSeqno + idNo;
		
		daoTable = "dba_acno";
		updateSQL = " acct_no  = ?,";
		updateSQL += " mod_user   = 'ICU',";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') , ";
        updateSQL += " acct_holder_id  = ? ";
        whereStr = " where acct_no = ? ";
        setString(1, newAcctNo);
        setString(2, prgmId);
        setString(3, sysDate + sysTime);
        setString(4, idNoFromData);
        setString(5, actNo);
        int updateCnt = updateTable();
		if (updateCnt == 0) {
			showLogMessage("W", "", "fail to update Dba_Acno AcctNo");
		}
	}
	
	// 2.確認該卡號是否已存在dbc_card
	int selectDbcCard() throws Exception {
		int cnt = 0;
		dbcCardExistCode = "N";
		
		sqlCmd = "select count(*) as cnt";
		sqlCmd += " from dbc_card ";
		sqlCmd += " where card_no = ? ";
		setString(1, data.cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			cnt = getValueInt("cnt");
			if (cnt > 0) {
//				if (debug == 1)
//					showLogMessage("I", "", "Error:該卡號已存在VD卡片資料檔中");
//				errCode = "1";
//				createErrReport();
//				totalOutputFile++;
				dbcCardExistCode = "Y";
				return 1;
			}
		}
		return 0;
	}

	// 3.確認該卡號是否已存在cca_card_base
	int selectCcaCardBase() throws Exception {
		int cnt = 0;
		cardBaseExistCode = "N";
		
		sqlCmd = "select count(*) as cnt";
		sqlCmd += " from cca_card_base ";
		sqlCmd += " where card_no = ? ";
		setString(1, data.cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			cnt = getValueInt("cnt");
			if (cnt > 0) {
//				if (debug == 1)
//					showLogMessage("I", "", "Error:該卡號已存在授權卡片檔中");
//				errCode = "2";
//				createErrReport();
//				totalOutputFile++;
				cardBaseExistCode = "Y";
				return 1;
			}

		}
		return 0;
	}

//檢核資料
	void selectCrdCardnoRange() throws Exception {
		// 撈取團代、卡種參數
		sqlCmd = "select group_code,card_type ";
		sqlCmd += " from crd_cardno_range ";
		sqlCmd += " where bin_no = ? ";
		sqlCmd += " and beg_seqno <= ? ";
		sqlCmd += " and end_seqno >= ? ";
		setString(1, data.cardNo.substring(0, 6));
		setString(2, data.cardNo.substring(6, 15));
		setString(3, data.cardNo.substring(6, 15));
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			String err1 = "select_crd_cardno_range error[notFound]!";
			String err2 = data.cardNo;
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		} else {
			tmpGroupCode = getValue("group_code");
			tmpCardType =  getValue("card_type");
		}

		// 撈取認同集團碼、來源代號參數
		sqlCmd = "select unit_code,source_code ";
		sqlCmd += " from dbc_card_type ";
		sqlCmd += " where group_code = ? ";
		sqlCmd += " and card_type = ? ";
		setString(1, tmpGroupCode);
		setString(2, tmpCardType);
		recordCnt = selectTable();
		if (notFound.equals("Y")) {
			String err1 = "select_dbc_card_type error[notFound]!";
			String err2 = "[" + tmpGroupCode + "]" + "[" + tmpCardType + "]";
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		} else {
			tmpUnitCode = getValue("unit_code");
			tmpSourceCode = getValue("source_code");
		}
		// 撈取帳戶帳號類別碼參數
		sqlCmd = "select a.card_indicator,b.acct_type ";
		sqlCmd += " from dbp_acct_type a,dbp_prod_type b   ";
		sqlCmd += " where b.group_code = ?  ";
		sqlCmd += " and b.card_type = '' ";
		sqlCmd += " and a.acct_type = b.acct_type ";
		setString(1, tmpGroupCode);
		recordCnt = selectTable();
		if (notFound.equals("Y")) {
			sqlCmd = "select a.card_indicator,b.acct_type ";
			sqlCmd += " from dbp_acct_type a,dbp_prod_type b  ";
			sqlCmd += " where b.card_type  =  ? ";
			sqlCmd += " and  a.acct_type = b.acct_type ";
			setString(1, tmpCardType);
			recordCnt = selectTable();
			if (notFound.equals("Y")) {
				String err1 = "select_dbp_acct_type, dbp_prod_type error[notFound]!";
				String err2 = "[" + tmpGroupCode + "]" + "[" + tmpCardType + "]";
				comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
			} else {
				tmpCardIndicator = getValue("card_indicator");
				tmpAcctType = getValue("acct_type");
			}
		} else {
			tmpCardIndicator = getValue("card_indicator");
			tmpAcctType = getValue("acct_type");
		}

		// 撈取國際組織別參數
		sqlCmd = "select bin_type ";
		sqlCmd += " from ptr_bintable  ";
		sqlCmd += " where bin_no || bin_no_2_fm || '0000' <= ? ";
		sqlCmd += "  and bin_no || bin_no_2_to || '9999' >= ? ";
		setString(1, data.cardNo);
		setString(2, data.cardNo);
		recordCnt = selectTable();
		if (notFound.equals("Y")) {
			String err1 = "select_ptr_bintable error[notFound]!";
			String err2 = data.cardNo;
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		} else {
			tmpBinType = getValue("bin_type");
		}
        //撈取晶片卡註記、電子票證註記
		sqlCmd = "select ic_flag, electronic_code ";
		sqlCmd += " from crd_item_unit  ";
		sqlCmd += " where card_type  = ? ";
		sqlCmd += " and unit_code  = ? ";
		setString(1, tmpCardType);
		setString(2, tmpGroupCode);
		recordCnt = selectTable();
		if (notFound.equals("Y")) {
			String err1 = "select_crd_item_unit error[notFound]!";
			String err2 = "[" + tmpGroupCode + "]" + "[" + tmpCardType + "]";
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		} else {
			tmpicFlag = getValue("ic_flag");
			tmpElectronicCode = getValue("electronic_code");
		}

	}

	// insert或update dbc_idno
	void updateDbcIdno() throws Exception {
		// 3. 身份證是否存在dbc_idno
		sqlCmd = "select count(*) as cnt";
		sqlCmd += " from dbc_idno ";
		sqlCmd += " where id_no = ? ";
		setString(1, data.idNo);
		int recordCnt = selectTable();
		if (getValueInt("cnt") > 0) { // 4.存在更新dbc_idno
			daoTable = "dbc_idno";
			updateSQL = " birthday  = ?,";
			updateSQL += " chi_name  = ?,";
			updateSQL += " eng_name  = ?,";
			updateSQL += " home_area_code1  = ?,";
			updateSQL += " home_tel_no1 = ?,";
			updateSQL += " home_tel_ext1 = ?,";
			updateSQL += " office_area_code1 = ?,";
			updateSQL += " office_tel_no1 = ?,";
			updateSQL += " office_tel_ext1 = ?,";
			updateSQL += " MSG_FLAG = 'Y' ,";  // 增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
			updateSQL += " MSG_PURCHASE_AMT = 0 ,"; // 增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
			updateSQL += " mod_user   = 'ICU',";
            updateSQL += " mod_pgm  = ? ,";
            updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            whereStr = " where id_no = ? ";
            setString(1, data.birthday);
            setString(2, data.name);
            setString(3, data.engName);
            setString(4, data.homeAreaCode);
            setString(5, data.homeTelNo);
            setString(6, data.homeTelExt);
            setString(7, data.officeAreaCode);
            setString(8, data.officeTelNo);
            setString(9, data.officeTelExt);
            setString(10, prgmId);
            setString(11, sysDate + sysTime);
            setString(12, data.idNo);
			updateTable();
		} else { // 4.不存在插入dbc_idno
			sqlCmd = "select id_p_seqno ";
			sqlCmd += " from crd_idno_seqno ";
			sqlCmd += " where id_no = ? ";
			sqlCmd += " fetch first 1 rows only ";
			setString(1, data.idNo);
			recordCnt = selectTable();
			if (notFound.equals("Y")) {
				sqlCmd = "select substr(to_char(ecs_acno.nextval,'0000000000'), 2,10) as id_p_seqno  ";
				sqlCmd += " from dual ";
				recordCnt = selectTable();
				if (notFound.equals("Y")) {
					String err1 = "ERROR GET ID_P_SEQNO!";
					comcr.errRtn(err1, "", comcr.hCallBatchSeqno);
				} else {
					idPSeqno = getValue("id_p_seqno");
					insertCrdIdnoSeqno();
				}
			} else {
				idPSeqno = getValue("id_p_seqno");
			}
			tmpIdPSeqno = String.format("%010d", Integer.parseInt(idPSeqno));
			setValue("id_p_seqno", tmpIdPSeqno);
			setValue("id_no", data.idNo);
			setValueInt("id_no_code", 0);
			setValue("birthday", data.birthday);
			setValue("chi_name", data.name);
			setValue("eng_name", data.engName);
			setValue("home_area_code1", data.homeAreaCode);
			setValue("home_tel_no1", data.homeTelNo);
			setValue("home_tel_ext1", data.homeTelExt);
			setValue("office_area_code1", data.officeAreaCode);
			setValue("office_tel_no1", data.officeTelNo);
			setValue("office_tel_ext1", data.officeTelExt);
			setValue("MSG_FLAG", "Y"); // 增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
			setValueInt("MSG_PURCHASE_AMT", 0); // 增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
			setValue("fst_stmt_cycle", "01");
			setValue("crt_date", sysDate);
			setValue("mod_user", prgmId);
			setValue("mod_time", sysDate + sysTime);
			setValue("mod_pgm", prgmId);
			daoTable = "dbc_idno";
			insertTable();
		}
	}

//insert dba_acno、cca_card_acct:
	void insertDbaAcno() throws Exception {
		// 5帳號是否存在
		sqlCmd = "select p_seqno ";
		sqlCmd += " from dba_acno ";
		sqlCmd += " where acct_no  = ? ";
		setString(1, data.actNo);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) { // 6.帳號不存在->insert dbc_idno->insert cca_card_acct
			sqlCmd = "select ECS_ACNO.NEXTVAL acno_seqno  ";
			sqlCmd += " from dual ";
			recordCnt = selectTable();
			if (notFound.equals("Y")) {
				String err1 = "ERROR GET P_SEQNO!";
				comcr.errRtn(err1, "", comcr.hCallBatchSeqno);
			} else {
				pPseqAcnoSeqno = getValue("acno_seqno");
				tmpPSeqno = String.format("%010d", Integer.parseInt(pPseqAcnoSeqno));
			}
			sqlCmd = "select id_p_seqno ";
			sqlCmd += " from dbc_idno ";
			sqlCmd += " where id_no = ? ";
			setString(1, data.idNo);
			int recordCnts = selectTable();
			if (recordCnts > 0) {
				setValue("id_p_seqno", getValue("id_p_seqno"));
			} else {
				setValue("id_p_seqno", tmpIdPSeqno);
			}
			tmpAcctKey = String.format("%10s%1s", data.idNo, "0");
			setValue("p_seqno", tmpPSeqno);
			setValue("acct_type", tmpAcctType);
			setValue("acct_key", tmpAcctKey);
			setValue("acct_status", "1");
			setValue("acct_sub_status", "1");
			setValue("acct_holder_id", data.idNo);
			setValue("acct_holder_id_code", "0");
			setValue("card_indicator", tmpCardIndicator);
			setValue("rc_use_b_adj", "1");
			setValue("autopay_indicator", "1");
			setValue("worse_mcode", "0");
			setValue("legal_delay_code", "9");
			setValue("inst_auth_loc_amt", "0");
			setValue("special_stat_code", "5");
			setValue("acct_no", data.actNo);
			setValue("new_vdchg_flag", "Y");
			setValue("class_code", "C");
			setValue("stmt_cycle", "01");
			setValue("crt_date", sysDate);
			setValue("mod_user", prgmId);
			setValue("mod_time", sysDate + sysTime);
			setValue("mod_pgm", prgmId);
			setValue("mod_seqno", "0");
			daoTable = "dba_acno";
			insertTable();
			// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//			sqlCmd = "select substr(to_char(ecs_card_acct_idx.nextval, '0000000000'), 2,10) as new_card_acct_idx ";
//			sqlCmd += " from dual ";
//			recordCnt = selectTable();
//			if (notFound.equals("Y")) {
//				String err1 = "ERROR GET CARD_ACCT_IDX!";
//				comcr.errRtn(err1, "", comcr.hCallBatchSeqno);
//			} else {
//				tmpCardAcctIdx = getValue("new_card_acct_idx");
//			}
			tmpCardAcctIdx = Integer.toString(Integer.parseInt(tmpPSeqno));
			
			setValue("acno_p_seqno", tmpPSeqno);
			setValue("debit_flag", "Y");
			setValue("acno_flag", "1");
			setValue("acct_type", tmpAcctType);
			if (notFound.equals("Y")) {
				setValue("id_p_seqno", tmpIdPSeqno);
			} else {
				setValue("id_p_seqno", getValue("id_p_seqno"));
			}
			setValue("p_seqno", tmpPSeqno);
			setValue("card_acct_idx", tmpCardAcctIdx);
			setValue("mod_user", prgmId);
			setValue("mod_time", sysDate + sysTime);
			setValue("mod_pgm", prgmId);
			setValue("mod_seqno", "0");
			daoTable = "cca_card_acct";
			insertTable();

			setValue("card_acct_idx", tmpCardAcctIdx);
			setValue("p_seqno", tmpPSeqno);
			setValue("mod_user", prgmId);
			setValue("mod_time", sysDate + sysTime);
			setValue("mod_pgm", prgmId);
			daoTable = "cca_consume";
			insertTable();
			
		} else { // 5.帳號存在->資料是否存在
			sqlCmd = "select count(*) as cnt ";
			sqlCmd += " from cca_card_acct ";
			sqlCmd += " where acno_p_seqno   = ? ";
			setString(1, getValue("p_seqno"));
			recordCnt = selectTable();
			if (getValueInt("cnt") == 0) { // 6.資料不存在-》 insert cca_card_acct
				// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//				sqlCmd = "select substr(to_char(ecs_card_acct_idx.nextval, '0000000000'), 2,10) as new_card_acct_idx ";
//				sqlCmd += " from dual ";
//				recordCnt = selectTable();
//				if (notFound.equals("Y")) {
//					String err1 = "ERROR GET CARD_ACCT_IDX!";
//					comcr.errRtn(err1, "", comcr.hCallBatchSeqno);
//				} else {
//					tmpCardAcctIdx = getValue("new_card_acct_idx");
//				}
				tmpCardAcctIdx = Integer.toString(Integer.parseInt(getValue("p_seqno")));
				
				sqlCmd = "select p_seqno, acct_type, id_p_seqno ";
				sqlCmd += " from dba_acno ";
				sqlCmd += " where acct_no  = ? ";
				setString(1, data.actNo);
				recordCnt = selectTable();
				setValue("acno_p_seqno", getValue("p_seqno"));
				setValue("debit_flag", "Y");
				setValue("acno_flag", "1");
				setValue("acct_type", getValue("acct_type"));
				setValue("id_p_seqno", getValue("id_p_seqno"));
				setValue("p_seqno", getValue("p_seqno"));
				setValue("card_acct_idx", tmpCardAcctIdx);
				setValue("mod_user", prgmId);
				setValue("mod_time", sysDate + sysTime);
				setValue("mod_pgm", prgmId);
				setValue("mod_seqno", "0");
				daoTable = "cca_card_acct";
				insertTable();
				
				setValue("card_acct_idx", tmpCardAcctIdx);
				setValue("p_seqno", tmpPSeqno);
				setValue("mod_user", prgmId);
				setValue("mod_time", sysDate + sysTime);
				setValue("mod_pgm", prgmId);
				daoTable = "cca_consume";
				insertTable();

			}
		}

	}

	// 7.insert dbc_card
	void insertDbcCard() throws Exception {
		sqlCmd = "select id_p_seqno  ";
		sqlCmd += " from dbc_idno ";
		sqlCmd += " where id_no = ? ";
		setString(1, data.idNo);
		int recordCnts = selectTable();
		if (notFound.equals("Y")) {
			setValue("id_p_seqno", tmpIdPSeqno);
			setValue("major_id_p_seqno", tmpIdPSeqno);
		} else {
			setValue("id_p_seqno", getValue("id_p_seqno"));
			setValue("major_id_p_seqno", getValue("id_p_seqno"));
		}
		sqlCmd = "select p_seqno ";
		sqlCmd += " from dba_acno ";
		sqlCmd += " where acct_no  = ? ";
		setString(1, data.actNo);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			setValue("p_seqno", tmpPSeqno);
		} else {
			setValue("p_seqno", getValue("p_seqno"));
		}
		setValue("card_no", data.cardNo);
		setValue("group_code", tmpGroupCode);
		setValue("source_code", tmpSourceCode);
		setValue("bin_no", data.cardNo.substring(0, 6));
		setValue("bin_type", tmpBinType);
		setValue("sup_flag", "0");
		setValue("acno_flag", "1");
		setValue("major_card_no", data.cardNo);
		setValue("current_code", "0");
		setValue("new_beg_date", sysDate.substring(0, 2) + data.validFmYy + data.validFmMm + "01");
		setValue("new_end_date", sysDate.substring(0, 2) + data.validToYy + data.validToMm
				+ lastdateOfmonth(data.validToYy, data.validToMm));
		setValue("issue_date", sysDate);
		setValue("acct_type", tmpAcctType);
		setValue("activate_type", "");
		setValue("activate_flag", "1");
		setValue("activate_date", "");
		setValue("acct_no", data.actNo);
		;
		setValue("beg_bal", "0");
		setValue("end_bal", "0");
		setValue("ic_flag", tmpicFlag);
		setValue("CARD_REF_NUM", data.cardNumber);
		setValue("ELECTRONIC_CODE", tmpElectronicCode);
		setValue("stmt_cycle", "01");
		setValue("MSG_FLAG", "Y"); // 增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
		setValueInt("MSG_PURCHASE_AMT", 0); // 增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
		setValue("crt_date", sysDate);
		setValue("crt_user", "");
		setValue("apr_date", "");
		setValue("apr_user", "");
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		setValue("mod_seqno", "0");
		daoTable = "dbc_card";
		insertTable();
	}

	// 8.insert cca_card_base
	void insertCcaCardBase() throws Exception {
		sqlCmd = "select id_p_seqno ";
		sqlCmd += " from dbc_idno ";
		sqlCmd += " where id_no = ? ";
		setString(1, data.idNo);
		int recordCnts = selectTable();
		if (notFound.equals("Y")) {
			setValue("id_p_seqno", tmpIdPSeqno);
			setValue("major_id_p_seqno", tmpIdPSeqno);
		} else {
			setValue("id_p_seqno", getValue("id_p_seqno"));
			setValue("major_id_p_seqno", getValue("id_p_seqno"));
		}
		sqlCmd = "select p_seqno ";
		sqlCmd += " from dba_acno ";
		sqlCmd += " where acct_no  = ? ";
		setString(1, data.actNo);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			setValue("p_seqno ", tmpPSeqno);
			setValue("acno_p_seqno ", tmpPSeqno);
			setValue("card_acct_idx", tmpCardAcctIdx);
		} else {
			setValue("p_seqno", getValue("p_seqno"));
			setValue("acno_p_seqno", getValue("p_seqno"));
			sqlCmd = "select card_acct_idx ";
			sqlCmd += " from cca_card_acct ";
			sqlCmd += " where acno_p_seqno   = ?  ";
			setString(1, getValue("p_seqno"));
			recordCnt = selectTable();
			if (notFound.equals("Y")) {
				setValue("card_acct_idx", tmpCardAcctIdx);
			} else {
				setValue("card_acct_idx", getValue("card_acct_idx"));
			}
		}
		setValue("card_no", data.cardNo);
		setValue("debit_flag", "Y");
		setValue("bin_type", tmpBinType);
		setValue("acno_flag", "1");
		setValue("acct_type", tmpAcctType);
		setValue("sup_flag", "Y");
		setValue("card_indicator", tmpCardIndicator);
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		setValue("mod_seqno  ", "0");
		daoTable = "cca_card_base";
		insertTable();
	}
	
	// 9.insert crd_seqno_log
	void insertCrdSeqnoLog() throws Exception {
		extendField = "crsl.";
		setValueInt("crsl.card_type_sort" , 0);
		setValue("crsl.bin_no"            , data.cardNo.substring(0, 6));
		setValue("crsl.SEQNO"             , data.cardNo.substring(6));
		setValue("crsl.card_type"         , tmpCardType );
		setValue("crsl.group_code"        , tmpGroupCode);
		setValue("crsl.card_flag"         , "1" );
		setValue("crsl.reserve"           , "Y");
		setValue("crsl.reason_code"       , "1");
		setValue("crsl.use_date"          , sysDate);
		setValue("crsl.use_id"            , javaProgram);
		setValue("crsl.card_item"         , tmpGroupCode + tmpCardType);
		setValue("crsl.unit_code"         , tmpGroupCode);
		setValue("crsl.trans_no"          , "");
		setValue("crsl.seqno_old"         , data.cardNo.substring(6, 15));
		setValue("crsl.CRT_DATE"          , sysDate);
		setValue("crsl.MOD_USER"          , prgmId);
		setValue("crsl.MOD_TIME"          , sysDate + sysTime);
		setValue("crsl.MOD_PGM"           , javaProgram);

		daoTable = "crd_seqno_log";

		insertTable();

		if (dupRecord.equals("Y")) {
		    String err1 = "insert crd_seqno_log error[dupRecord]";
		    String err2 = "BIN_NO = " + data.cardNo.substring(0, 6) + "，SEQNO = " + data.cardNo.substring(6);
		    comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		}
	}
	
	void updateDbcCard() throws Exception {
		
		sqlCmd = "select new_beg_date as tmpOldBegDate, ";
		sqlCmd += " new_end_date as tmpOldEndDate , ";
		sqlCmd += " activate_flag as tmpOldActivateFlag , ";
		sqlCmd += " activate_type as tmpOldActivateType , ";
		sqlCmd += " activate_date as tmpOldActivateDate  ";
		sqlCmd += " from dbc_card ";
		sqlCmd += " where card_no = ? ";
		setString(1, data.cardNo);
		int recordCnts = selectTable();
		
		tmpOldBegDate =  getValue("tmpOldBegDate");
		tmpOldEndDate =  getValue("tmpOldEndDate");
		tmpOldActivateFlag =  getValue("tmpOldActivateFlag");
		tmpOldActivateType =  getValue("tmpOldActivateType");
		tmpOldActivateDate =  getValue("tmpOldActivateDate");
		
		daoTable = "dbc_card";
		updateSQL = " card_ref_num  = ? ,";
		updateSQL += " reissue_date  = to_char(sysdate,'yyyymmdd') ,";
		updateSQL += " reissue_status  = '3' ,";
		updateSQL += " old_beg_date  = ? ,";
		updateSQL += " old_end_date  = ? ,";		
		updateSQL += " new_beg_date  = ? ,";
		updateSQL += " new_end_date  = ? ,";
		updateSQL += " eng_name  = ? ,";
		updateSQL += " old_activate_flag  = ? ,";
		updateSQL += " old_activate_type  = ? ,";
		updateSQL += " old_activate_date  = ? ,";
		updateSQL += " activate_flag  = '1' ,";
		updateSQL += " activate_type  = '' ,";
		updateSQL += " activate_date  = '' ,";
		updateSQL += " current_code  = '0' ,";
		updateSQL += " oppost_date  = '' ,";
		updateSQL += " oppost_reason  = '' ,";
		updateSQL += " MSG_FLAG = 'Y' ,";  // 增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
		updateSQL += " MSG_PURCHASE_AMT = 0 ,"; // 增加MSG_FLAG(固定給 “Y”)、MSG_PURCHASE_AMT(固定給0)
		updateSQL += " mod_user   = 'ICU',";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " where card_no = ? ";
        setString(1, data.cardNumber);
        setString(2, tmpOldBegDate);
        setString(3, tmpOldEndDate);
        setString(4, sysDate.substring(0, 2) + data.validFmYy + data.validFmMm + "01");
        setString(5, sysDate.substring(0, 2) + data.validToYy + data.validToMm + lastdateOfmonth(data.validToYy, data.validToMm));
        setString(6, data.engName);
        setString(7, tmpOldActivateFlag);
        setString(8, tmpOldActivateType);
        setString(9, tmpOldActivateDate);  
        setString(10, prgmId);
        setString(11, sysDate + sysTime);
        setString(12, data.cardNo);
		updateTable();		
	}

	// 最後一日
	public String lastdateOfmonth(String years, String months) {
		int year = Integer.parseInt(years);
		int month = Integer.parseInt(months);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		int lastDay = cal.getMinimum(Calendar.DATE);
		cal.set(Calendar.DAY_OF_MONTH, lastDay - 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(cal.getTime());
		String lastday = date.substring(8, 10);
		return lastday;
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
				comcr.errRtn("update_crd_file_ctl not found!", "", comcr.hCallBatchSeqno);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		IcuD067 proc = new IcuD067();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	void procFTP() throws Exception {
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
	void insertCrdIdnoSeqno() throws Exception {
		
		setValue("id_no", data.idNo);
		setValue("id_p_seqno", idPSeqno);
    	setValue("id_flag", "");		
    	setValue("bill_apply_flag", "");
    	setValue("debit_idno_flag", "Y");
		daoTable = "crd_idno_seqno";
		insertTable();
	}

	/***********************************************************************/
	class Buf1 {
		String cardNo;
		String idNo;
		String actNo;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(cardNo, 16);
			rtn += fixLeft(idNo, 10);
			rtn += fixLeft(actNo, 13);
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
	class VDCardLayout extends hdata.BaseBin {
		public String cardNo = ""; // 卡號
		public String validFmYy = ""; // 有效起始年份
		public String validFmMm = ""; // 有效起始月份
		public String validToYy = ""; // 有效迄止年份
		public String validToMm = ""; // 有效迄止月份
		public String actNo = ""; // 帳號
		public String idNo = ""; // id 186
		public String birthday = ""; // BIRTH-DATE(要轉換成西元年) 166
		public String name = ""; // 姓名 189
		public String engName = ""; // 英文姓名 190
		public String homeAreaCode = ""; // 192
		public String homePhone = ""; // 192
		public String homeTelNo = ""; // 192
		public String homeTelExt = ""; // 192
		public String officePhone = "";// 193
		public String officeAreaCode = ""; // 193
		public String officeTelNo = ""; // 193
		public String officeTelExt = ""; // 193
		public String cardNumber = ""; // 卡片序號 8

		@Override
		public void initData() {

		}

	}

}

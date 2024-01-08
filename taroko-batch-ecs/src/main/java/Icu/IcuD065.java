/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR               DESCRIPTION                    *
 *  ---------  --------- ----------- ----------------------------------------  *
 * 109/06/23  V1.00.00    yanghan     program initial                          *
 * 109/07/02  V1.00.01    Pino        test&debug                               *
 *  109-07-03  V1.01.02  yanghan       修改了變量名稱和方法名稱            *                                                      *
 * 109/07/03  V1.01.02    JustinWu                  change the program name*
 *  109-07-22    yanghan       修改了字段名称            *
 *  109/08/14  V1.01.06   Wilson       資料夾名稱修改為小寫                                                                         *
 *  109/09/08  V1.01.07   Wilson       測試修改                                                                                               *
 *  109/09/11  V1.01.08   Wilson       新增卡片序號                                                                                        *
 *  109/09/17  V1.01.09   Wilson       開卡註記調整                                                                                        *
 *  109/09/29  V1.01.10   Wilson       無檔案不秀error、讀檔不綁檔名日期                                            *
 *  109/10/19  V1.01.11   Wilson       錯誤報表FTP                                *
 *  109-10-19  V1.00.12    shiyuqi       updated for project coding standard     *
 *  110/12/08  V1.00.13   Wilson       錯誤訊息調整                                                                                          *
 *  111/02/14  V1.01.13    Ryan      big5 to MS950                                           *
 ******************************************************************************/

package Icu;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import java.text.SimpleDateFormat;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;

public class IcuD065 extends AccessDAO {
	private final String progname = "設一科VD續卡製卡檔處理程式 111/02/14 V1.00.14";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;
	VDCardLayout data = new VDCardLayout();

	int debug = 0;

	String prgmId = "IcuD065";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;

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
	int recno = 0;
	int hRecCnt1 = 0;

	String getFileName;
	String outFileName;
	int totalInputFile;
	int totalOutputFile;
	String errCode;

	protected String[] dT1 = new String[] {};
	Buf1 data1 = new Buf1();

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
			
//			if (args.length == 0) {
//				fileDate = sysDate;
//			} else if (args.length == 1) {
//				fileDate = args[0];
//			}
//			if (fileDate.length() != 8) {
//				comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
//			}
			
			hModUser = comc.commGetUserID();
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
	int openFile() throws Exception {
		int fileCount = 0;

		String tmpstr = String.format("%s/media/icu", comc.getECSHOME());
//        System.out.println("tmpstr="+tmpstr);
		List<String> listOfFiles = comc.listFS(tmpstr, "", "");

		for (String file : listOfFiles) {
			getFileName = file;
			if (getFileName.length() != 26)
				continue;
			if (!getFileName.substring(0, 14).equals("VD_T_MAKECARD_"))
				continue;
			
//			if (!getFileName.substring(14, 22).equals(fileDate))
//				continue;
			
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(getFileName);
		}
		if (fileCount < 1) {
			showLogMessage("I", "", "無檔案可處理");
			
//			comcr.hCallErrorDesc = "Error : 無檔案可處理";
//			comcr.errRtn("Error : 無檔案可處理", "", comcr.hCallBatchSeqno);
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
			if (endFile[fi].equals("Y"))
				break;
//            if (rec.length() == 1700) {
			errCode = "";
			totalInputFile++;
			moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1));
			processDisplay(1000);
//            }
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

	// 根據年月返回當前月份的最後一天
	public String lastdateOfmonth(String years, String months) {
		int year = Integer.parseInt(years);
		int month = Integer.parseInt(months);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		int lastDay = cal.getMinimum(Calendar.DATE);
		cal.set(Calendar.DAY_OF_MONTH, lastDay - 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(cal.getTime());
	}

	/***********************************************************************/
	private void moveData(Map<String, Object> map) throws Exception {
		String tmpChar = "";
		data.initData();
		data.cardNo = (String) map.get("col120"); // 卡號(每四位間隔一空白)
		data.cardNo = data.cardNo.substring(0, 4) + data.cardNo.substring(5, 9) + data.cardNo.substring(10, 14)
				+ data.cardNo.substring(15, 19);
		data.cardNo = data.cardNo.trim();
		if (debug == 1)
			System.out.println("卡號=" + data.cardNo);

		data.cardNo2 = (String) map.get("col188"); // 卡號 錯誤報告需要
		data.cardNo2 = data.cardNo2.trim();
		if (debug == 1)
			System.out.println("卡號2=" + data.cardNo2);

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

		data.id = (String) map.get("col186"); // 身份證ID
		data.id = data.id.trim();
		if (debug == 1)
			System.out.println("身份證ID=" + data.id);
		
		data.cardNumber = (String) map.get("col8"); // 卡片序號
		data.cardNumber = data.cardNumber.trim();
		if (debug == 1)
			System.out.println(" 卡片序號=" + data.cardNumber);

		if (selectDbcCard() == 1)
			return;
		if (selectCcaCardBase() == 1)
			return;
		// update dbc_card
		updateDbcCard();
		commitDataBase();
		return;
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
	int outPutTextFile() throws Exception {
//		int fileNo = 0;
//		
//		  sqlCmd  = "select max(substr(file_name, 27, 2)) file_no";
//	      sqlCmd += " from crd_file_ctl  ";
//	      sqlCmd += " where file_name like ?";
//	      sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
//	      setString(1, "VD_T_MAKECARD_ERR." + "%" + ".TXT");
//	      
//	      if (selectTable() > 0) 
//	      	fileNo = getValueInt("file_no");  

		hNcccFilename = String.format("VD_T_MAKECARD_ERR_%s.TXT", getFileName.substring(14, 22));
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		outFileName = String.format("%s/media/icu/error/%s", comc.getECSHOME(), hNcccFilename);
		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

		return 0;
	}

	/***********************************************************************/
	void createErrReport() throws Exception {
		data1 = new Buf1();
		data1.actNo = data.actNo;
		data1.cardNo = data.cardNo2;
		data1.id = data.id;
		switch (errCode) {
		case "1":
			data1.errReason = String.format("%-200s", "該卡號不存在VD卡片資料檔中");
			break;
		case "2":
			data1.errReason = String.format("%-200s", "該卡號不存在授權卡片檔中");
			break;
		}

		data1.date = sysDate;

		buf = data1.allText();
//        lpar1.clear();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}

	/***********************************************************************/
	int selectDbcCard() throws Exception {// 確認該卡號是否存在dbc_card
		int cnt = 0;
		sqlCmd = "select count(*) as cnt";
		sqlCmd += " from dbc_card ";
		sqlCmd += " where card_no = ? ";
		setString(1, data.cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			cnt = getValueInt("cnt");
			if (cnt == 0) {
				if (debug == 1)
					showLogMessage("I", "", "Error:該卡號不存在VD卡片資料檔中");
				errCode = "1";
				createErrReport();// 產生錯誤報告
				totalOutputFile++;
				return 1;
			}
		}
		return 0;
	}

	/***********************************************************************/
	int selectCcaCardBase() throws Exception {// 確認該卡號是否已存在cca_card_base
		int cnt = 0;
		sqlCmd = "select count(*) as cnt";
		sqlCmd += " from cca_card_base ";
		sqlCmd += " where card_no = ? ";
		setString(1, data.cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			cnt = getValueInt("cnt");
			if (cnt == 0) {
				if (debug == 1)
					showLogMessage("I", "", "Error:該卡號不存在授權卡片檔中");
				errCode = "2";
				createErrReport();
				totalOutputFile++;
				return 1;
			}

		}
		return 0;
	}

	int updateDbcCard() throws Exception { // update dbc_card
		String oldBegDate = null, oldEndDate = null, activateType = null, activateFlag = null, activateDate = null;
		// 查詢出原有dbccard 內容
		sqlCmd = "select new_beg_date,new_end_date,activate_type,activate_flag,activate_date";
		sqlCmd += " from dbc_card ";
		sqlCmd += " where card_no = ? ";
		setString(1, data.cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			oldBegDate = getValue("new_beg_date");
			oldEndDate = getValue("new_end_date");
			activateType = getValue("activate_type");
			activateFlag = getValue("activate_flag");
			activateDate = getValue("activate_date");
		}
		// update dbc_card
		daoTable = "dbc_card";
		updateSQL = "old_beg_date  = ?,";
		updateSQL += " old_end_date  = ?,";
		updateSQL += " old_activate_type = ?,";
		updateSQL += " old_activate_flag = ?,";
		updateSQL += " old_activate_date  = ?,";

		updateSQL += " activate_type   = '',";
		updateSQL += " activate_flag   = '1',";//
		updateSQL += " activate_date   = '',";
		updateSQL += " new_beg_date   = ?,";// 6
		updateSQL += " new_end_date    = ?,";// 7
		updateSQL += " change_date    = ?,";// 8
		updateSQL += " change_status = '3',";
		updateSQL += " card_ref_num = ?,";
		updateSQL += " mod_time     = sysdate,";
	      updateSQL += " mod_pgm      = ? ";
	      whereStr = "where card_no = ? ";//9
	      setString(1,oldBegDate );
	      setString(2,oldEndDate );
	      setString(3,activateType );
	      setString(4,activateFlag );
	      setString(5,activateDate );
	      
	      setString(6,sysDate.substring(0, 2) + data.validFmYy + data.validFmMm + "01");
	      setString(7,lastdateOfmonth(sysDate.substring(0, 2)+data.validToYy,data.validToMm));
	      setString(8,sysDate );
	      setString(9, data.cardNumber);
	      setString(10, prgmId);
	      setString(11,data.cardNo );
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_dbc_card not found!", "", hCallBatchSeqno);
		}
		return 0;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IcuD065 proc = new IcuD065();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String cardNo;
		String id;
		String actNo;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(cardNo, 16);
			rtn += fixLeft(id, 10);
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

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		data1.cardNo = comc.subMS950String(bytes, 12, 28);
		data1.errReason = comc.subMS950String(bytes, 28, 228);
		data1.date = comc.subMS950String(bytes, 228, 236);
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
	class VDCardLayout extends hdata.BaseBin {
		public String cardNo = ""; // 卡號
		public String validFmYy = ""; // 有效起始年份
		public String validFmMm = ""; // 有效起始月份
		public String validToYy = ""; // 有效迄止年份
		public String validToMm = ""; // 有效迄止月份
		public String actNo = ""; // 帳號
		public String id = ""; // 身份證id
		public String cardNo2 = ""; // 卡號 格式二 第二個卡號 產生錯誤報表檔中需要使用
		public String cardNumber = ""; // 卡片序號

		@Override
		public void initData() {
		
		}

	}
}

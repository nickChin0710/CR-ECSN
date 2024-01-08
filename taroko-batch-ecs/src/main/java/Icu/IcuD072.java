/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/06/23  V1.00.00    Brian     program initial                           *
 *  109-07-21  V1.00.01  yanghan       修改了變量名稱和方法名稱            *
 *  109-07-22    yanghan       修改了字段名称            *
 *  109-08-12  V1.00.03  Wilson      修改讀取方式                                                                                             *
 *  109/08/14  V1.00.04  Wilson      資料夾名稱修改為小寫                                                                              *
 *  109/09/29  V1.00.05  Wilson      營業日才執行                                                                                            *
 *  109/10/12  V1.00.06  Wilson      檔名日期改營業日                                                                                     *
 *  109/10/19  V1.00.07  Wilson      錯誤報表FTP                                  *
 *  109-10-19  V1.00.08   shiyuqi       updated for project coding standard     *
 *  111/02/14  V1.00.09    Ryan      big5 to MS950                                           *
 *  111/02/22  V1.00.10  Justin      增加錯誤訊息                              *
 *  111/11/30  V1.00.11  Wilson      新增updateTscVdCard，將舊悠遊卡號停用                                 *
 *  111/12/14  V1.00.12  Wilson      製卡類別為R(其他補發卡)才做updateTscVdCard        *
 ******************************************************************************/
package Icu;


import java.io.UnsupportedEncodingException;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*設一科悠遊VD卡製卡回饋檔處理程式*/
public class IcuD072 extends AccessDAO {
	private final String progname = "設一科悠遊VD卡製卡回饋檔處理程式 111/12/14  V1.00.12";

	int debug = 0;
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;

	Buf1 readData = new Buf1();
	int linecnt = 0;
	int writeCnt = 0;
	String queryDate = "";	
	String hBusiBusinessDate = "";
	String fileName1 = "";
	
	String fileFolderPath = comc.getECSHOME() + "/media/icu/";

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname + "," + args.length);
			// =====================================

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			
            selectPtrBusinday();
			
			// 若沒有給定查詢日期，則查詢日期為系統日
		      if(args.length == 0) {
		          queryDate = hBusiBusinessDate;
		      }else
		      if(args.length == 1) {
		          if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
		              showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
		              return -1;
		          }
		          queryDate = args[0];
		      }else {
		          comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
		      }           
			
			if (checkPtrHoliday() != 0) {
				exceptExit = 0;
				comc.errExit("今日為假日,不執行此程式", "");
	        }

			readFile();

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", String.format("程式執行結束, 讀取[%d]筆資料, 寫入錯誤資料[%d]筆", linecnt, writeCnt));
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

		sqlCmd = " select business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		hBusiBusinessDate = getValue("business_date");
	}

	/************************************************************************/
	 int checkPtrHoliday() throws Exception {
	      int holidayCount = 0;

	      sqlCmd = "select count(*) holidayCount ";
	      sqlCmd += " from ptr_holiday  ";
	      sqlCmd += "where holiday = ? ";
	      setString(1, queryDate);
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
	private void readFile() throws Exception {

		String readstr = "";
		String errRsn = "";
		final String fileName = String.format("TSC_VD_MAKECARD_RTN_%s.TXT", queryDate);
		String inFilename = String.format(comc.getECSHOME() + "/media/icu/TSC_VD_MAKECARD_RTN_%s.TXT", queryDate);
//		boolean br = openBinaryInput(inFilename);
//		if (br == false) {
//			comcr.errRtn("File not exist", inFilename, "");
//		}
		
		showLogMessage("I", "", String.format("讀取檔案[%s]", inFilename));
		
		// 2020-08-12
		int in = openInputText(inFilename);
	    if (in == -1) {
	    	comcr.errRtn("無檔案可處理" , "處理日期 = " + queryDate , "");
		}
		
	    fileName1 = String.format("TSC_VD_MAKECARD_RTN_ERR_%s.TXT", queryDate);
		String outFilename = String.format(comc.getECSHOME() + "/media/icu/error/TSC_VD_MAKECARD_RTN_ERR_%s.TXT", queryDate);
		int out = openOutputText(outFilename, "MS950");

		/* read input file */
//		byte[] bytes = new byte[200];
//		int readlen = 0;
//		while ((readlen = readBinFile(bytes)) > 0) {
//			readstr = new String(bytes, 0, readlen, "big5");
		
	    while (true) {
	        readstr =  readTextFile(in);
	        
	        if (endFile[in].equals("Y"))
	  			break; // break while loop
		
			if (debug == 1)
				showLogMessage("I", "", "readstr=[" + readstr + "]" + linecnt);

			linecnt++;

			splitBuf1(readstr);

			if (readData.yuCardStaus.equals("OK") == false) {
				continue;
			}

			if (selectTscVdCard() > 0) {
				writeCnt++;
				errRsn = "該悠遊卡號已存在悠遊VD卡片資料檔中";
				showLogMessage("E", "", String.format("該悠遊卡號[%s]已存在悠遊VD卡片資料檔中", readData.svcPid));
				writeTextFile(out, String.format("%-16.16s%-16.16s%s%-8.8s\n", readData.svcPid, readData.cardNo,
						comc.fixLeft(errRsn, 200), sysDate));
				continue;
			}
			if (selectDbcCard() == 0) {
				writeCnt++;
				errRsn = "該信用卡號不存在VD卡片資料檔中";
				showLogMessage("E", "", String.format("該信用卡號[%s]不存在VD卡片資料檔中", readData.cardNo));
				writeTextFile(out, String.format("%16.16s%16.16s%s%8.8s\n", readData.svcPid, readData.cardNo,
						comc.fixLeft(errRsn, 200), sysDate));
				continue;
			}

		
			if(readData.yuMakeKind.equals("R")) {
				updateTscVdCard();
			}
			
			insertTscVdCard();

		}
	    closeInputText(in);

		closeOutputText(out);
		
		renameFile(fileName);
		
		if(writeCnt > 0) {
	    	commFTP = new CommFTP(getDBconnect(), getDBalias());
	        comr = new CommRoutine(getDBconnect(), getDBalias());
	        procFTP();
	        renameFile1(fileName1);
	    }

	}

	/***********************************************************************/
	private int selectTscVdCard() throws Exception {

		sqlCmd = " select count(*) as cnt ";
		sqlCmd += "  from tsc_vd_card ";
		sqlCmd += " where tsc_card_no = ? ";
		setString(1, readData.svcPid);
		selectTable();
		return getValueInt("cnt");

	}

	/***********************************************************************/
	private int selectDbcCard() throws Exception {

		sqlCmd = " select count(*) as cnt ";
		sqlCmd += "  from dbc_card ";
		sqlCmd += " where card_no = ? ";
		setString(1, readData.cardNo);
		selectTable();
		return getValueInt("cnt");
	}

	/***********************************************************************/
	 /**
		* @ClassName: IcuD072
		* @Description: updateTscVdCard 將舊悠遊卡號停用
		* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
		* @Company: DXC Team.
		* @author Wilson
		* @version V1.00.11, Nov 30, 2022
		*/
	  void updateTscVdCard() throws Exception {
	      daoTable   = "tsc_vd_card ";
	      updateSQL  = "current_code = '4', ";
	      updateSQL += "oppost_date = ?, ";
	      updateSQL += "tsc_oppost_date = ?, ";
	      updateSQL += "mod_time = sysdate, ";
	      updateSQL += "mod_pgm = ? ";
	      whereStr   = "where vd_card_no = ? ";
	      whereStr  += "and current_code = '0' ";
	      whereStr  += "and tsc_card_no <> ? ";
	      setString(1, sysDate);
	      setString(2, sysDate);
	      setString(3, javaProgram);
	      setString(4, readData.cardNo);
	      setString(5, readData.svcPid);
	      updateTable();
	  }
	  /***********************************************************************/
	private void insertTscVdCard() throws Exception {
		String tmpOldTscCardNo = "";
		sqlCmd = "  select tsc_card_no ";
		sqlCmd += "   from tsc_vd_card   ";
		sqlCmd += "  where vd_card_no  = ?   ";
		sqlCmd += "    and crt_date in ( select max(crt_date) from tsc_vd_card  ";
		sqlCmd += "                       where vd_card_no  = ?) ";
		setString(1, readData.cardNo);
		setString(2, readData.cardNo);
		if (selectTable() > 0) {
			tmpOldTscCardNo = getValue("tsc_card_no");
		} else {

			sqlCmd = "   select old_card_no ";
			sqlCmd += "    from dbc_card  ";
			sqlCmd += "   where card_no  = ? ";
			setString(1, readData.cardNo);
			if (selectTable() > 0) {
				String tmpOldCardNo = getValue("old_card_no");

				sqlCmd = "  select tsc_card_no ";
				sqlCmd += "   from tsc_vd_card   ";
				sqlCmd += "  where vd_card_no  = ?  ";
				sqlCmd += "    and crt_date in ( select max(crt_date)  ";
				sqlCmd += "                        from tsc_vd_card where vd_card_no  = ?) ";
				setString(1, tmpOldCardNo);
				setString(2, tmpOldCardNo);
				if (selectTable() > 0) {
					tmpOldTscCardNo = getValue("tsc_card_no");
				}
			}
		}

		String tmpTscEmbossRsn = "";
		String tmpAutoloadFlag = "";
		String tmpNewEndDate = "";

		/* tsc_emboss_rsn */
		if (readData.yuMakeKind.equals("N")) {
			tmpTscEmbossRsn = "1";
		} else if (readData.yuMakeKind.equals("C")) {
			tmpTscEmbossRsn = "3";
		} else if (readData.yuMakeKind.equals("R")) {
			tmpTscEmbossRsn = "5";
		}
		/* autoload_flag */
		if (readData.yuAutoloadFlg.equals("2")) {
			tmpAutoloadFlag = "N";
		} else if (readData.yuAutoloadFlg.equals("4")) {
			tmpAutoloadFlag = "Y";
		}
		/* new_end_date */
		tmpNewEndDate = comm.lastdateOfmonth("20" + readData.expDate);

		setValue("tsc_card_no", readData.svcPid);
		setValue("vd_card_no", readData.cardNo);
		setValue("acct_no", readData.acnoNo);
		setValue("tsc_emboss_rsn", tmpTscEmbossRsn);
		setValue("current_code", "0");
		setValue("crt_date", sysDate);
		setValue("new_beg_date", sysDate.substring(0, 6) + "01");
		setValue("new_end_date", tmpNewEndDate);
		setValue("tsc_amt", readData.yuAmtWallet);
		setValue("tsc_pledge_amt", readData.yuAmtDeposit);
		setValue("ic_seq_no", readData.yuChipSeq);
		setValue("isam_seq_no", readData.isamChipSeq);
		setValue("isam_batch_no", readData.isamMakeBat);
		setValue("isam_batch_seq", readData.isamMakeSeq);
		setValue("autoload_amt", readData.yuAutoloadAmt);
		setValue("autoload_flag", tmpAutoloadFlag);
		setValue("old_tsc_card_no", tmpOldTscCardNo);
		setValue("combine_flag", "N");
		setValue("tsc_sign_flag", "Y");
		setValue("tsc_sign_date", sysDate);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "tsc_vd_card";
		insertTable();

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IcuD072 proc = new IcuD072();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	  void renameFile(String removeFileName) throws Exception {
			String tmpstr1 = fileFolderPath + removeFileName;
			String tmpstr2 = fileFolderPath +"backup/" + removeFileName + "." + sysDate;

			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}

	  /****************************************************************************/
		void procFTP() throws Exception {
			  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
		      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		      commFTP.hEflgModPgm = javaProgram;
		      

		      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
		      showLogMessage("I", "", "mput " + fileName1 + " 開始傳送....");
		      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + fileName1);
		      
		      if (err_code != 0) {
		          showLogMessage("I", "", "ERROR:無法傳送 " + fileName1 + " 資料"+" errcode:"+err_code);
		          insertEcsNotifyLog(fileName1);          
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
	class Buf1 {
		String idno;/* 001_011 持卡人ID */
		String svcPid;/* 012_027 悠遊卡號 */
		String expDate;/* 028_031 悠遊卡有效期(信用卡有效期 YYMM) */
		String cardType;/* 032_034 信用卡別 */
		String cardNo;/* 035_050 信用卡號 */
		String acnoNo;/* 051_063 金融卡帳號(前13碼) */
		String acnoCdn;/* 064_065 金融卡帳號(後2碼) */
		String pidDate;/* 066_073 製卡日 */
		String yuChipSeq;/* 074_093 悠遊卡晶片序號 */
		String yuAmtWallet;/* 094_098 悠遊卡錢包金額 */
		String yuAmtDeposit;/* 099_103 悠遊卡押金 */
		String isamChipSeq;/* 104_111 ISAM晶片序號 */
		String isamMakeBat;/* 112_121 ISAM製卡批號 */
		String isamMakeSeq;/* 122_126 ISAM製卡序號 */
		String yuAutoloadAmt;/* 127_130 悠遊卡AUTOLOAD金額 */
		String manufacturer;/* 131_140 卡廠名稱代碼 */
		String yuMakeStaus;/* 141_141 製卡狀態 */
		String yuCardStaus;/* 142_151 卡片狀態 */
		String yuMakeKind;/* 152_152 製卡類別 */
		String name;/* 153_172 持卡人姓名 */
		String yuAutoloadFlg;/* 173_173 自動加值功能開啟預設值(2: 預設關閉 disable, 4: 預設開啟 enable) */
		String filler;/* 174_200 保留 */
	}

	/***********************************************************************/
	private void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		readData.idno = comc.subMS950String(bytes, 0, 11).trim();
		readData.svcPid = comc.subMS950String(bytes, 11, 16).trim();
		readData.expDate = comc.subMS950String(bytes, 27, 4).trim();
		readData.cardType = comc.subMS950String(bytes, 31, 3).trim();
		readData.cardNo = comc.subMS950String(bytes, 34, 16).trim();
		readData.acnoNo = comc.subMS950String(bytes, 50, 13).trim();
		readData.acnoCdn = comc.subMS950String(bytes, 63, 2).trim();
		readData.pidDate = comc.subMS950String(bytes, 65, 8).trim();
		readData.yuChipSeq = comc.subMS950String(bytes, 73, 20).trim();
		readData.yuAmtWallet = comc.subMS950String(bytes, 93, 5).trim();
		readData.yuAmtDeposit = comc.subMS950String(bytes, 98, 5).trim();
		readData.isamChipSeq = comc.subMS950String(bytes, 103, 8).trim();
		readData.isamMakeBat = comc.subMS950String(bytes, 111, 10).trim();
		readData.isamMakeSeq = comc.subMS950String(bytes, 121, 5).trim();
		readData.yuAutoloadAmt = comc.subMS950String(bytes, 126, 4).trim();
		readData.manufacturer = comc.subMS950String(bytes, 130, 10).trim();
		readData.yuMakeStaus = comc.subMS950String(bytes, 140, 1).trim();
		readData.yuCardStaus = comc.subMS950String(bytes, 141, 10).trim();
		readData.yuMakeKind = comc.subMS950String(bytes, 151, 1).trim();
		readData.name = comc.subMS950String(bytes, 152, 20).trim();
		readData.yuAutoloadFlg = comc.subMS950String(bytes, 172, 1).trim();
		readData.filler = comc.subMS950String(bytes, 173, 27).trim();
	}

}

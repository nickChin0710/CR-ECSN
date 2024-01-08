/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/07/01  V1.00.00   Shiyuqi      新增                                                                                                      *
 *  109-07-03  V1.01.02  yanghan       修改了變量名稱和方法名稱            *                                                                             *
 *  109/07/03  V1.01.01    JustinWu                  change the program name*
 *  109-07-22    yanghan       修改了字段名称            *
 *  109/08/14  V1.01.04   Wilson       資料夾名稱修改為小寫                                                                       *
 *  109/08/20  V1.01.05   Wilson       測試修改                                                                                              *
 *  109/10/12  V1.01.06   Wilson      檔名日期改營業日                                                                                  *
 *  109-10-19  V1.00.07    shiyuqi       updated for project coding standard     *
 *  109/10/20  V1.00.08   Wilson       錯誤報表FTP                                *
 *  109/10/23  V1.00.09   Wilson       錯誤報表檔檔名調整                                                                            *
 *  110/12/08  V1.00.10   Wilson       錯誤訊息調整                                                                                      *
 *  111/02/14  V1.00.11    Ryan      big5 to MS950                                           *
 ******************************************************************************/
package Icu;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
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
import com.CommTxBill;


public class IcuD077 extends AccessDAO {
	private final String progname = "CARDLINK一卡通要求列入黑名單功能檔(B2I003)處理程式 111/02/14 V1.00.11";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;
	VDCardLayout data = new VDCardLayout();
	int debug = 1;
	String prgmId = "IcuD077";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	// col詳VD製卡檔格式
	// col1資料文本 col2卡片外顯卡號 col3预留 col4换行
	protected final String dataStr = " col1 ,col2 ,col3  ";
	protected final int[] dataLENGTH = { 1, 11, 28 };

	String fileDate = "";
	int rptSeq1 = 0;
	String buf = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String hNcccFilename = "";
	int recno = 0;
	int hRecCnt1 = 0;
	String getFileName;
	String outFileName;
	int totalInputFile;
	int totalOutputFile;
	String errCode;
	String hBusiBusinessDate = "";

	protected String[] dataStr1 = new String[] {};
	Buf1 ncccData1 = new Buf1();

	public int mainProcess(String[] args) {

		try {
			dataStr1 = dataStr.split(",");
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
	int openFile() throws Exception {
		int fileCount = 0;

		String tmpstr = String.format("%s/media/icu", comc.getECSHOME());
		List<String> listOfFiles = comc.listFS(tmpstr, "", "");

		for (String file : listOfFiles) {
			getFileName = file;
			if (getFileName.length() != 25)
				continue;
			if (!getFileName.substring(0, 11).equals("B2I003_0006"))
				continue;
			if (!getFileName.substring(11, 19).equals(fileDate))
				continue;
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(getFileName);
		}
		if (fileCount < 1) {
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

		while (true) {
			rec = readTextFile(fi); // read file data
			System.out.println(rec.length() + "rec");
			if (endFile[fi].equals("Y"))
				break;
			
			if (rec.trim().length() != 0) {
			   
//				System.out.println(40 + "rec");
				errCode = "";
				    
				byte[] bytesArr = rec.getBytes("MS950");
					
				String label = CommTxBill.subByteToStr(bytesArr, 0, 1);

				if (label.equals("H"))
					continue;
					
				if (label.equals("T"))
					break;
				    
				totalInputFile++;
				moveData(processDataRecord(getFieldValue(rec, dataLENGTH), dataStr1));
				processDisplay(1000);			    
			}
		}

		if (totalOutputFile > 0) {
			outPutTextFile();
			comc.writeReport(outFileName, lpar1, "MS950");
			hRecCnt1 = totalOutputFile;
			lpar1.clear();
			
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP();
		    renameFile1(hNcccFilename);
		}

		closeInputText(fi);
		hRecCnt1 = totalInputFile;
		renameFile(fileName);

		return 0;
	}

	/***********************************************************************/
	private void moveData(Map<String, Object> map) throws Exception {
		data.ipsCardNo = (String) map.get("col2"); // 卡號
		data.ipsCardNo = data.ipsCardNo.trim();
		if (debug == 1)
			System.out.println("卡號=" + data.ipsCardNo);
		selectIpsCard();
		commitDataBase();
		return;
	}

	/***********************************************************************/
	int outPutTextFile() throws Exception {
		hNcccFilename = String.format("B2I003_ERR_0006%s", fileDate);
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		outFileName = String.format("%s/media/icu/error/%s", comc.getECSHOME(), hNcccFilename);
		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

		return 0;
	}

	// 產生錯誤報表檔
	void createErrReport() throws Exception {

		ncccData1 = new Buf1();
		ncccData1.ipsCardNo = data.ipsCardNo;
		switch (errCode) {
		case "1":
			ncccData1.errReason = String.format("%-200s", "一卡通外顯卡號不存在");
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
		sqlCmd += " and crt_date = to_char(sysdate,'yyyymmdd') ";
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
	// 1.判斷卡片外顯卡號是否存在ips_card
	int selectIpsCard() throws Exception {
		int cnt = 0;
		sqlCmd = "select count(*) as cnt";
		sqlCmd += " from ips_card ";
		sqlCmd += " where ips_card_no  = ? ";
		setString(1, data.ipsCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			cnt = getValueInt("cnt");
			if (cnt > 0) {
				updateIpsCard();
			} else {
				if (debug == 1)
					showLogMessage("I", "", "Error: 一卡通外顯卡號不存在");
				errCode = "1";
				createErrReport();
				totalOutputFile++;
				return 1;
			}
		}
		return 0;
	}

	// update ips_card
	void updateIpsCard() throws Exception {
		daoTable = "ips_card";
		updateSQL = " blacklt_flag  = 'Y',";
		updateSQL += " blacklt_date  = to_char(sysdate,'yyyymmdd') , ";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
	    updateSQL += " mod_pgm = ? ";
	    whereStr = "where ips_card_no  = ? ";
	    setString(1, sysDate + sysTime);
	    setString(2,prgmId);
	    setString(3, data.ipsCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_ips_card not found!", "", hCallBatchSeqno);
		}
	}

	public static void main(String[] args) throws Exception {
		IcuD077 proc = new IcuD077();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String ipsCardNo;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(ipsCardNo, 11);
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
	private Map processDataRecord(String[] row, String[] dt) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		for (String s : dt) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}

	/***********************************************************************/
	class VDCardLayout extends hdata.BaseBin {
		public String ipsCardNo = ""; // 卡片外顯卡號

		@Override
		public void initData() {

		}

	}

}

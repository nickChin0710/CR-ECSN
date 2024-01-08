/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  110/08/16  V1.00.00   JustinWu     program initial                        *
*  111/10/18  V1.00.01   Ryan         add getAcctTypeToOrg() Mothod          *
*  111/11/16  V1.00.02   Sunny        配合CRM測試調整Header格式                                *
*  112/03/20  V1.00.03   JeffKung     增加CSR的Header格式(UTF8)                *
*  112/09/19  V1.00.04   Ryan         add deleteFile()                       *
*****************************************************************************/
package com;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Dxc.Util.SecurityUtil;

public class CommTxInf extends AccessDAO {
	
	public  static final String HDR_EXTENSION = ".HDR";
	public  static final String DAT_EXTENSION = ".DAT";
	CommCrd commCrd = new CommCrd();
	public CommTxInf(Connection conn[], String[] dbAlias) throws Exception {
		super.conn = conn;
		setDBalias(dbAlias);
		setSubParm(dbAlias);
	}
	
	
	/**
	 * 產生Header檔案LAYOUT(*.HDR)共用生成程式
	 * @param fileFolder 產生檔案資料夾
	 * @param fileName 檔名(.DAT結尾)
	 * @param dataDate 資料日期(YYYYMMDD)
	 * @param createDate 資料日期(建立檔案一的日期YYYYMMDD)
	 * @param createTime 產出時分(建立檔案一的時分HHmm)
	 * @param dataCount 資料筆數
	 * @return true if the file is generated successfully, else false is returned.
	 * @throws Exception 
	 */
	public boolean generateTxtCrmHdr(String fileFolder, String fileName, String dataDate, String createDate, String createTime,
			int dataCount) throws Exception {
		
		boolean isOk = checkHdrParameters(fileName, dataDate, createDate, createTime);
		if (isOk == false) {
			showLogMessage("E", "", "[checkHdrParameters] HDR參數錯誤");
			return false;
		}
		
		// get the name of a output file
		String outFileName = fileName.replace(DAT_EXTENSION, HDR_EXTENSION);
		String filePath = Paths.get(fileFolder, outFileName).toString();
		
		boolean isGenerated = produceHdrFile(filePath, fileName, dataDate, createDate, createTime, dataCount);
		if (isGenerated == false) {
			showLogMessage("E", "", "[checkHdrParameters] 產生HDR檔錯誤");
			return false;
		}
		
		return true;
	}


	/**111.11.07 CRM調整HDR規格：
	 *  產生檔案，如下格式<br>
	 *  下傳檔名			X(32)	01-32	如CCTCUS_YYMMDD.DAT<br>
	 *  資料日期			X(08)	33-40	西元YYYYMMDD<br>
	* FILLER			X(02)	41-42	西元YYYYMMDD<br>
	 *  產出日期			X(08)	43-50	西元YYYYMMDD<br>
	 *  產出時分			X(04)	51-54	HHMM<br>
	 *  下傳檔名資料筆數    	X(10)	55-64	<br>
	 * @param filePath
	 * @param fileName
	 * @param dataDate
	 * @param createDate
	 * @param createTime
	 * @param dataCount
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	private boolean produceHdrFile(String filePath, String fileName, String dataDate, String createDate, String createTime, int dataCount)
			throws Exception, UnsupportedEncodingException {
		
		boolean isOpen = openBinaryOutput(filePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", filePath));
			return false;
		}
		
		try {
			CommCrd commCrd = new CommCrd();
			writeBinFile(commCrd.fixLeft(fileName, 32).getBytes("MS950"), 32); 
			writeBinFile(commCrd.fixLeft(dataDate, 8).getBytes("MS950"), 8);
			writeBinFile(commCrd.fixLeft("00", 2).getBytes("MS950"), 2);
			writeBinFile(commCrd.fixLeft(createDate, 8).getBytes("MS950"), 8);
			writeBinFile(commCrd.fixLeft(createTime, 4).getBytes("MS950"), 4);
			writeBinFile(String.format("%010d", dataCount).getBytes("MS950"), 10);		
		}finally {
			closeBinaryOutput();
		}

		return true;
	}

	/**CRM原始HDR規格
	 *  產生檔案，如下格式<br>
	 *  下傳檔名			X(30)	01-30	如CCTCUS_YYMMDD.DAT<br>
	 *  資料日期			X(08)	31-38	西元YYYYMMDD<br>
	 *  產出日期			X(08)	39-46	西元YYYYMMDD<br>
	 *  產出時分			X(04)	47-50	HHMM<br>
	 *  下傳檔名資料筆數	X(10)	51-60	<br>
	 *  FILLER				X(20)	61-80	
	 * @param filePath
	 * @param fileName
	 * @param dataDate
	 * @param createDate
	 * @param createTime
	 * @param dataCount
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
/*
	private boolean produceHdrFile_bk(String filePath, String fileName, String dataDate, String createDate, String createTime, int dataCount)
			throws Exception, UnsupportedEncodingException {
		
		boolean isOpen = openBinaryOutput(filePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", filePath));
			return false;
		}
		
		try {
			CommCrd commCrd = new CommCrd();		
			writeBinFile(commCrd.fixLeft(fileName, 30).getBytes("MS950"), 30); 
			writeBinFile(commCrd.fixLeft(dataDate, 8).getBytes("MS950"), 8);
			writeBinFile(commCrd.fixLeft(createDate, 8).getBytes("MS950"), 8);
			writeBinFile(commCrd.fixLeft(createTime, 4).getBytes("MS950"), 4);
			writeBinFile(String.format("%010d", dataCount).getBytes("MS950"), 10);
			writeBinFile(commCrd.fixLeft(" ", 20).getBytes("MS950"), 20);
		}finally {
			closeBinaryOutput();
		}

		return true;
	}
*/
	/**
	 * 產生Header檔案LAYOUT(*.HDR)共用生成程式
	 * @param fileFolder 產生檔案資料夾
	 * @param fileName 檔名(.DAT結尾)
	 * @param dataDate 資料日期(YYYYMMDD)
	 * @param createDate 資料日期(建立檔案一的日期YYYYMMDD)
	 * @param createTime 產出時分(建立檔案一的時分HHmm)
	 * @param dataCount 資料筆數
	 * @return true if the file is generated successfully, else false is returned.
	 * @throws Exception 
	 */
	public boolean generateTxtCSRHdr(String fileFolder, String fileName, String dataDate, String createDate, String createTime,
			int dataCount) throws Exception {
		
		boolean isOk = checkHdrParameters(fileName, dataDate, createDate, createTime);
		if (isOk == false) {
			showLogMessage("E", "", "[checkHdrParameters] HDR參數錯誤");
			return false;
		}
		
		// get the name of a output file
		String outFileName = fileName.replace(DAT_EXTENSION, HDR_EXTENSION);
		String filePath = Paths.get(fileFolder, outFileName).toString();
		
		boolean isGenerated = produceCSRHdrFile(filePath, fileName, dataDate, createDate, createTime, dataCount);
		if (isGenerated == false) {
			showLogMessage("E", "", "[checkHdrParameters] 產生HDR檔錯誤");
			return false;
		}
		
		return true;
	}


	/**112.03.20 CSR的HDR規格：
	 *  產生檔案，如下格式<br>
	 *  下傳檔名			X(32)	01-32	如CCTCUS_YYMMDD.DAT<br>
	 *  資料日期			X(08)	33-40	西元YYYYMMDD<br>
	* FILLER			X(02)	41-42	西元YYYYMMDD<br>
	 *  產出日期			X(08)	43-50	西元YYYYMMDD<br>
	 *  產出時分			X(04)	51-54	HHMM<br>
	 *  下傳檔名資料筆數    	X(10)	55-64	<br>
	 * @param filePath
	 * @param fileName
	 * @param dataDate
	 * @param createDate
	 * @param createTime
	 * @param dataCount
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	private boolean produceCSRHdrFile(String filePath, String fileName, String dataDate, String createDate, String createTime, int dataCount)
			throws Exception, UnsupportedEncodingException {
		
		boolean isOpen = openBinaryOutput(filePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", filePath));
			return false;
		}
		
		try {
			CommCrd commCrd = new CommCrd();
			StringBuffer sb = new StringBuffer();
			sb.append(commCrd.fixLeft(fileName, 32)); 
			sb.append(commCrd.fixLeft(dataDate, 8));
			sb.append(commCrd.fixLeft("00", 2));
			sb.append(commCrd.fixLeft(createDate, 8));
			sb.append(commCrd.fixLeft(createTime, 4));
			sb.append(String.format("%010d", dataCount));
			sb.append("\n");
			byte[] tmpBytes = sb.toString().getBytes();
            writeBinFile(tmpBytes, tmpBytes.length);
	
		}finally {
			closeBinaryOutput();
		}

		return true;
	}

	/**
	 * verify HDR parameters
	 * @param fileName 檔名(.DAT結尾)
	 * @param dataDate 資料日期(YYYYMMDD)
	 * @param createDate 資料日期(建立檔案一的日期YYYYMMDD)
	 * @param createTime 產出時分(建立檔案一的時分HHmm)
	 * @return
	 */
	private boolean checkHdrParameters(String fileName, String dataDate, String createDate, String createTime) {
		if (fileName.indexOf(DAT_EXTENSION) == -1) {
			showLogMessage("E", "", String.format("此檔名不符合格式[%s]，檔名要以.DAT結尾", fileName));
			return false;
		}
		
		if (dataDate == null || dataDate.trim().length() != 8) {
			showLogMessage("E", "", String.format("資料日期不符合格式[%s]，日期格式為YYYYMMDD", dataDate));
			return false;
		}
		
		if (createDate == null || createDate.trim().length() != 8) {
			showLogMessage("E", "", String.format("資料日期不符合格式[%s]，日期格式為YYYYMMDD", createDate));
			return false;
		}
		
		if (createTime == null || createTime.trim().length() != 4) {
			showLogMessage("E", "", String.format("資料時分不符合格式[%s]，日期格是為HHmm", createTime));
			return false;}	
		
		return true;
	}
	

	/**
	   *  產生銀行別欄位使用
	 * @return 銀行別
	 */
	public String getAcctTypeToOrg(String acctType,String currCode) {
		String outOrg = "";
		if("01".equals(acctType)) {
			outOrg = "106";
			if("840".equals(currCode)) {
				outOrg = "606";
			}
			if("392".equals(currCode)) {
				outOrg = "607";
			}
		}else if("90".equals(acctType)) {
			outOrg = "206";
		}else {
			outOrg = "306";
		}
		
		return outOrg;
	}
	
	/****
	 * 刪除保留n代以外的檔案
	 * @param fileCnt 保留n代
	 * @param filePath 路徑例如/media/crm/
	 * @param fileName 檔名例如CCTCUS_*.DAT,CCTCUS_*.HDR 
	 * @return
	 * @throws Exception 
	 */
	public int deleteFile(int fileCnt , String filePath , String ...fileName) throws Exception {
		String pathTmp = Paths.get(commCrd.getECSHOME(), filePath).toString();
		for (int i = 0; i < fileName.length; i++) {
			int deleteCnt = 0;
			String start = "";
			String end = "";
			String[] fileNames = fileName[i].split("\\*", -1);
			if(fileNames.length > 1) {
				start = fileNames[0];
				end = fileNames[1];
			}else {
				start = fileNames[0];
			}
			List<String> listTmp = listFsSort(pathTmp, start, end);
			if (listTmp == null || listTmp.size() == 0) {
				showLogMessage("I", "", "無符合檔案" + fileName[i] + " ,[" + pathTmp + "]");
				continue;
			}
			showLogMessage("I", "", "符合檔案" + fileName[i] + " ,筆數 =[ " + listTmp.size() + "]");
			for (int ii = 0; ii < listTmp.size(); ii++) {
				String path = Paths.get(pathTmp, listTmp.get(ii).toString()).toString();
				if (ii < fileCnt) {
					showLogMessage("I", "", "已保留檔案=[" + path + "]");
					continue;
				}
				if (commCrd.fileDelete(path) == true) {
//			            showLogMessage("I", "", "刪除檔案" + path);
					deleteCnt++;
				}
			}
			showLogMessage("I", "", "刪除檔案筆數=[" + deleteCnt + "]");
		}
		return 1;
	}
	 
	/***
	 * 按照最後修改時間排序
	 * @param path 
	 * @param start
	 * @param end 
	 * @return
	 * @throws Exception
	 */
	public List<String> listFsSort(String path, String start, String end){
		List<String> temp = new ArrayList<String>();
		File folder = new File(SecurityUtil.verifyPath(path));
		if(folder.exists() == false) {
			showLogMessage("I", "", "查無檔案目錄[" + path + "]");
			return null;
		}
		File[] listFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(start) && name.endsWith(end);
			}
		});
		Arrays.sort(listFiles, new Comparator<File>() {
			public int compare(File file1, File file2) {
				return Long.compare(file2.lastModified(), file1.lastModified());
			}
		});
		
		for (File file : listFiles) {
			if (file.isFile() == false)
				continue;
			temp.add(file.getName());
		}
		return temp;
	}
}

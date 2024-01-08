/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  111/09/13  V1.00.00  Ryan      Initial                                   *
****************************************************************************/

package Crd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommString;

import Crd.CrdT001;
/*轉入分行地址相關資料處理程式*/
public class CrdT001 extends AccessDAO {
private String progname = "轉入分行地址相關資料處理程式  111/09/13  V1.00.00";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;

	String filePath = comc.getECSHOME() + "/media/crd/";
	String fileName = "branchaddr.txt";
	
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	HashMap<String, String> addrMap = null;
	
	String cityChkStr = ",基隆市,台北市,新北市";

	protected final String dT1Str = "branch_no, chi_name, branch_addr";

    protected final int[] dt1Length = { 4, 30, 100};
	
	String hModUser = "";
	String prgmId = "";

	int totalInputFile = 0;
	int totalOutputFile = 0;
	
    String tmpBranchNo = "";
    String tmpBranchName = "";
    String tmpBranchAddr = "";
    String tmpSouthFlag = "";
    String tmpChiAddr1 = "";
    String tmpChiAddr2 = "";
    String tmpChiAddr3 = "";
    String tmpChiAddr4 = "";
    String tmpChiAddr5 = "";

	protected String[] dT1 = new String[] {};
	
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

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comsecr = new CommSecr(getDBconnect(), getDBalias());

			hModUser = comc.commGetUserID();
			prgmId = javaProgram;

            readFile();

			// ==============================================
			// 固定要做的
            showLogMessage("I", "", "執行結束,[ 總筆數 : "+ totalInputFile +"],[ 錯誤筆數 : "+ totalOutputFile +"]");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}


	/**********************************************************************/
	int readFile() throws Exception {
		String rec = "";
		String fileText = "";
		int fi;
		fileText = filePath + fileName;

		int f = openInputText(fileText);
		if (f == -1) {
			showLogMessage("I", "", "無檔案可處理");
			return 1;
		}
		System.out.println("====Start Read File ====");

		closeInputText(f);

		setConsoleMode("N");
		fi = openInputText(fileText, "MS950");
		setConsoleMode("Y");
		if (fi == -1) {
			return 1;
		}

		showLogMessage("I", "", " Process file path =[" + filePath + " ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");

		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;

			totalInputFile++;
			moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1));
			processDisplay(1000);
		}

		closeInputText(fi);
		renameFile(fileName);

		return 0;
	}

	/***********************************************************************/
    private void moveData(Map<String, Object> map) throws Exception {       
    	initData();
    	addrMap = new HashMap<String, String>();
    	
    	tmpBranchNo = (String) map.get("branch_no"); //分行代號
    	tmpBranchNo = tmpBranchNo.trim();
        
    	tmpBranchName = (String) map.get("chi_name"); //中文全名
    	tmpBranchName = tmpBranchName.trim();
        
        tmpBranchAddr = (String) map.get("branch_addr"); //分行地址
        tmpBranchAddr = tmpBranchAddr.trim();

        addrMap = parseByAddress(tmpBranchAddr);
        if(addrMap == null) {
        	showLogMessage("I", "", String.format("分行地址格式有誤 !! ,[%s]", tmpBranchAddr));
			totalOutputFile ++;
			return;
        }
        
        tmpChiAddr1 = addrMap.get("city") == null ? "":addrMap.get("city").toString();
        tmpChiAddr2 = addrMap.get("region") == null ? "":addrMap.get("region").toString();
        tmpChiAddr3 = addrMap.get("village") == null ? "":addrMap.get("village").toString();
        tmpChiAddr4 = addrMap.get("neighbor") == null ? "":addrMap.get("neighbor").toString();
        tmpChiAddr5 = addrMap.get("others") == null ? "":addrMap.get("others").toString();    
        tmpSouthFlag = commString.pos(cityChkStr, tmpChiAddr1) > 0 ? "N" : "Y";
        updateGenBrn();
    }

	/***********************************************************************/
	void updateGenBrn() throws Exception {
		int i = 1;
		daoTable = "GEN_BRN";
		updateSQL = " SOUTH_FLAG = ?,";
		updateSQL += " CHI_ADDR_1 = ?,";
		updateSQL += " CHI_ADDR_2 = ?,";
		updateSQL += " CHI_ADDR_3 = ?,";
		updateSQL += " CHI_ADDR_4 = ?,";
		updateSQL += " CHI_ADDR_5 = ?,";
		updateSQL += " MOD_USER  = ? ,";
        updateSQL += " MOD_PGM  = ? ,";
        updateSQL += " MOD_TIME  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " WHERE BRANCH = ?  ";  
        setString(i++, tmpSouthFlag);
        setString(i++, tmpChiAddr1);
        setString(i++, tmpChiAddr2);
        setString(i++, tmpChiAddr3);
        setString(i++, tmpChiAddr4);
        setString(i++, tmpChiAddr5);
        setString(i++, prgmId);
        setString(i++, prgmId);
        setString(i++, sysDate + sysTime);
        setString(i++, tmpBranchNo);

		updateTable();
		
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("UPDATE GEN_BRN NOT FOUND !! ,BRANCH = [%s]", tmpBranchNo));
			totalOutputFile ++;
			return;
		}
		commitDataBase();
	}

	/***********************************************************************/
	void insertFileCtl() throws Exception {
		setValue("file_name", fileName);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", totalInputFile);
		setValueInt("record_cnt", totalInputFile);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = "head_cnt = ?,";
			updateSQL += " record_cnt = ?,";
			updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name = ? ";
			setInt(1, totalInputFile);
			setInt(2, totalInputFile);
			setString(3, fileName);
			updateTable();
			if (notFound.equals("Y")) {
				showLogMessage("I", "", "update_crd_file_ctl not found!");
			}
		}
	}

	/****************************************************************************/	
	public static void main(String[] args) throws Exception {
		CrdT001 proc = new CrdT001();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	
	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = filePath + removeFileName;
		String tmpstr2 = filePath +"backup/" + removeFileName + "." + sysDate + sysTime;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	
	/****************************************************************************/
	private HashMap<String, String> parseByAddress(String address) {
		HashMap<String, String> map = new HashMap<String, String>();
		String pattern = "(?<city>\\D+?[縣市])(?<region>\\D+?(市區|鎮區|鎮市|[鄉鎮市區]))?(?<village>\\D+?[村里])?(?<neighbor>\\d+[鄰])?(?<road>\\D+?(村路|[路街道段]))?(?<section>\\D?段)?(?<lane>\\d+巷)?(?<alley>\\d+弄)?(?<no>\\d+號?)?(?<seq>-\\d+?(號))?(?<floor>\\d+樓)?(?<others>.+)?";

		// Create a Pattern object
		Pattern pattern1 = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher match = pattern1.matcher(address);
		if (match.find()) {
			// City 為縣市，Region為鄉鎮市區，Village為村里，Neighbor為鄰，other為其他
			map.put("city", match.group("city"));
			map.put("region", match.group("region"));
			map.put("village", match.group("village"));
			map.put("neighbor", match.group("neighbor"));

			map.put("others",
					(match.group("road") == null ? "" : match.group("road"))
							+ (match.group("section") == null ? "" : match.group("section"))
							+ (match.group("lane") == null ? "" : match.group("lane"))
							+ (match.group("alley") == null ? "" : match.group("alley"))
							+ (match.group("no") == null ? "" : match.group("no"))
							+ (match.group("seq") == null ? "" : match.group("seq"))
							+ (match.group("floor") == null ? "" : match.group("floor"))
							+ (match.group("others") == null ? "" : match.group("others")));

		} else {
			return null;
		}

		return map;

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
	
	private Map<String,Object> processDataRecord(String[] row, String[] dt) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		for (String s : dt) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}
	
	
	/***********************************************************************/
	private void initData() {
		tmpBranchNo = "";
		tmpBranchName = "";
		tmpBranchAddr = "";
		tmpSouthFlag = "";
		tmpChiAddr1 = "";
		tmpChiAddr2 = "";
		tmpChiAddr3 = "";
		tmpChiAddr4 = "";
		tmpChiAddr5 = "";
	}
}
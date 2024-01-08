/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  112/03/28  V1.00.00  Ryan      Initial                                   *
****************************************************************************/

package Etb;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommString;

/*0H90拒絕行銷註記相關檔案處理程式*/
public class EtbA003 extends AccessDAO {
private String progname = "0H90拒絕行銷註記相關檔案處理程式  112/03/28  V1.00.00";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
    private final String FILE_NAME_A01N = "0H90_A01N_YYYYMMDD.TXT";
    private final String FILE_NAME_B06Y = "0H90_B06Y_YYYYMMDD.TXT";
    private final String FILE_NAME_B06N = "0H90_B06N_YYYYMMDD.TXT";
    private final String FILE_NAME_A01Y = "0H90_A01Y_YYYYMMDD.TXT";
    private final String FILE_PATH = "/media/crd/";

	protected final String dT1Str = "id_no, col70";

    protected final int[] dt1Length = { 10, 70};
	
	String hModUser = "";
	String prgmId = "";

	int totalInputFile = 0;
	int updateNotFundCnt = 0;
	
    String tmpIdNo = "";
    String businessDate = "";

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
			
		   if(args.length == 0) {
				 businessDate = sysDate;
           }else
           if(args.length == 1) {
               // 檢查參數(查詢日期)
               if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
                   showLogMessage("E", "", String.format("日期格式[%s]錯誤，日期格式應為西元年yyyyMMdd", args[0]));
                   return -1;
               }
               businessDate = args[0];
           }else {
               comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
           }   
		   
	       showLogMessage("I", "", String.format("本日系統日 : [ %s]", businessDate));

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comsecr = new CommSecr(getDBconnect(), getDBalias());

			hModUser = comc.commGetUserID();
			prgmId = javaProgram;

			//讀取「拒絕行銷」為N檔
            readFile(FILE_NAME_A01N);
            //讀取「信用卡業務」為Y檔
            readFile(FILE_NAME_B06Y);
            //讀取「信用卡業務」為N檔
            readFile(FILE_NAME_B06N);
            //讀取「拒絕行銷」為Y檔
            readFile(FILE_NAME_A01Y);

			// ==============================================
			// 固定要做的
            showLogMessage("I", "", "");
            showLogMessage("I", "", String.format("執行結束,[ 總筆數 : %s]", totalInputFile));
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}


	/**********************************************************************/
	int readFile(String fileName) throws Exception {
		String rec = "";
		int fi;
		String refileName = fileName.replace("YYYYMMDD", businessDate);
		String filePath = String.format("%s%s%s", comc.getECSHOME(), FILE_PATH ,refileName);
		filePath = Normalizer.normalize(filePath, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "");
    	showLogMessage("I", "", String.format("正在讀取檔案 = [%s]", filePath));
    	
		int f = openInputText(filePath ,"MS950");
		if (f == -1) {
			showLogMessage("I", "", "無檔案可處理");
			return 1;
		}

		closeInputText(f);

		setConsoleMode("N");
		fi = openInputText(filePath, "MS950");
		setConsoleMode("Y");
		if (fi == -1) {
			return 1;
		}
		showLogMessage("I", "", "");
    	showLogMessage("I", "", String.format("正在處理檔案 = [%s]", refileName));
    	int inputFileCnt = 0;
		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;
			inputFileCnt++;
			totalInputFile++;
			moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1),fileName);
			processDisplay(1000);
		}
        showLogMessage("I", "", "");
    	showLogMessage("I", "", String.format("[%s]檔案已處理結束 ,筆數 = [%s]", refileName ,inputFileCnt));
		
		closeInputText(fi);
		renameFile(refileName);

		return 0;
	}

	/***********************************************************************/
    private void moveData(Map<String, Object> map,String fileName) throws Exception {       
    	initData();
    	
    	tmpIdNo = (String) map.get("id_no"); //分行代號
    	tmpIdNo = tmpIdNo.trim();
    	
    	//處理「拒絕行銷」為N檔
    	if(FILE_NAME_A01N.equals(fileName)) {
    		updateCrdIdno("2","Y","B");
    		updateDbcIdno("2","Y","B");
    	}
    	
    	//處理「信用卡業務」為Y檔
    	if(FILE_NAME_B06Y.equals(fileName)) {
    		updateCrdIdno("2","Y","B");
    		updateDbcIdno("2","Y","B");
    	}
    	
    	//處理「信用卡業務」為N檔
    	if(FILE_NAME_B06N.equals(fileName)) {
    		updateCrdIdno("0","N","B");
    		updateDbcIdno("2","Y","B");
    	}
           
      	//處理「拒絕行銷」為Y檔
    	if(FILE_NAME_A01Y.equals(fileName)) {
    		updateCrdIdno("0","N","B");
    		updateDbcIdno("2","Y","B");
    	}
    }

	/***********************************************************************/
	void updateCrdIdno(String marketAgreeBase,String acceptCallSell,String callSellFromMark) throws Exception {
		int i = 1;
		daoTable = "CRD_IDNO";
		updateSQL = " MARKET_AGREE_BASE = ?,";
		updateSQL += " ACCEPT_CALL_SELL = ?,";
		updateSQL += " CALL_SELL_FROM_MARK = ?,";
		updateSQL += " CALL_SELL_CHG_DATE = ?,";
		updateSQL += " MOD_USER  = ? ,";
        updateSQL += " MOD_PGM  = ? ,";
        updateSQL += " MOD_TIME  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " WHERE ID_NO = ?  ";  
        setString(i++, marketAgreeBase);
        setString(i++, acceptCallSell);
        setString(i++, callSellFromMark);
        setString(i++, businessDate);
        setString(i++, prgmId);
        setString(i++, prgmId);
        setString(i++, sysDate + sysTime);
        setString(i++, tmpIdNo);

		updateTable();
		
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("UPDATE CRD_IDNO NOT FOUND !! ,ID_NO = [%s]", tmpIdNo));
			return;
		}
		commitDataBase();
	}

	/***********************************************************************/
	void updateDbcIdno(String marketAgreeBase,String acceptCallSell,String callSellFromMark) throws Exception {
		int i = 1;
		daoTable = "DBC_IDNO";
		updateSQL = " MARKET_AGREE_BASE = ?,";
		updateSQL += " ACCEPT_CALL_SELL = ?,";
		updateSQL += " CALL_SELL_FROM_MARK = ?,";
		updateSQL += " CALL_SELL_CHG_DATE = ?,";
		updateSQL += " MOD_USER  = ? ,";
        updateSQL += " MOD_PGM  = ? ,";
        updateSQL += " MOD_TIME  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " WHERE ID_NO = ?  ";  
        setString(i++, marketAgreeBase);
        setString(i++, acceptCallSell);
        setString(i++, callSellFromMark);
        setString(i++, businessDate);
        setString(i++, prgmId);
        setString(i++, prgmId);
        setString(i++, sysDate + sysTime);
        setString(i++, tmpIdNo);

		updateTable();
		
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("UPDATE DBC_IDNO NOT FOUND !! ,ID_NO = [%s]", tmpIdNo));
			return;
		}
		commitDataBase();
	}

	/****************************************************************************/	
	public static void main(String[] args) throws Exception {
		EtbA003 proc = new EtbA003();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	
	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = String.format("%s%s%s",comc.getECSHOME(),FILE_PATH,removeFileName);
		String tmpstr2 = String.format("%s%sbackup/%s.%s", comc.getECSHOME(),FILE_PATH,removeFileName,sysDate+sysTime);
		
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
		tmpIdNo = "";
	}
}
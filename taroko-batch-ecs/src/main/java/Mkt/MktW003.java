package Mkt;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112-08-09  V0.00.00     Ryan         initial                               *
* 112-09-05  V0.00.01     Grace        取消日期=15的限制                                                                      *
* 113-01-03  V0.00.02     Grace        配合利害卡, 變更檔名為 EA_DIGITAL_OPEN_694.TXT *
* 113-01-03  V0.00.02     Ryan         移除insertMktEadigitalOpen dupRecord 的log ,新增dupRecord 筆數log顯示*
*******************************************************************************/

import com.CommCrd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import Dxc.Util.SecurityUtil;

public class MktW003 extends AccessDAO {
	private String PROGNAME = "卡娜環保綠數位申辦名單檔 接收寫檔處理 112/08/09 V0.00.00";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommDate zzdate = new CommDate();
	private static final String FOLDER = "/media/mkt";
	//private static final String FILE_NAME = "EA_DIGITAL_OPEN.TXT";
	private static final String FILE_NAME = "EA_DIGITAL_OPEN_694.TXT";
	int totalCnt = 0;
	private String lineLength = "";
	private String idNo = "";
	private String applyDate = "";
	private String busiDate = "";
	private int dupRecordCnt = 0;
	
//=**************************************************************************** 
	public static void main(String[] args) {
		MktW003 proc = new MktW003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
	}

//=============================================================================
	public int mainProcess(String[] args){
		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			// =====================================

			// 固定要做的
			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());
			
			busiDate = getBusiDate();
			String parm1 = "";
			if(args.length == 1) {
				if(!zzdate.isDate(args[0])) {
					comc.errExit("參數日期格式輸入錯誤", "");
				}
				parm1 = args[0];
				busiDate = parm1;
			}

			showLogMessage("I", "", String.format("程式參數1[%s]", parm1));
			/*
			if(!"15".equals(zzstr.right(busiDate, 2))) {
				showLogMessage("I", "", "本日營業日非15日,程式不執行  [" + busiDate + "]");
				return 0;
			}
			*/
			showLogMessage("I", "", "本日營業日 =  [" + busiDate + "]");

			String fileName = FILE_NAME;

			if (openFile(fileName) == 0) {
				readFile(fileName);
				renameFile(fileName);
				commitDataBase();
			}
			showLogMessage("I", "", "資料處理筆數 =  [" + totalCnt + "]");
			showLogMessage("I", "", "insert MKT_EADIGITAL_OPEN dupRecord 筆數 = [" + dupRecordCnt +"]");
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}finally {
			finalProcess();
		}
	}

//=============================================================================
	int openFile(String fileName) throws Exception {
	    String path = String.format("%s%s/%s", comc.getECSHOME(), FOLDER ,fileName);
	    path = Normalizer.normalize(path, java.text.Normalizer.Form.NFKD);
	    int f = openInputText(path); 
        if (f == -1) {
        	showLogMessage("D", "", "無檔案可處理  " + "");
        	return 1;
        }
        closeInputText(f);
		return (0);
	}

//=============================================================================
	int readFile(String fileName) throws Exception {
		System.out.println("");
		showLogMessage("I","","====Start Read File ====");
		BufferedReader br = null;
		try {
			String tmpStr = String.format("%s%s/%s", comc.getECSHOME(), FOLDER ,fileName);
			String tempPath = SecurityUtil.verifyPath(tmpStr);
			FileInputStream fis = new FileInputStream(new File(tempPath));
			br = new BufferedReader(new InputStreamReader(fis, "MS950"));

			System.out.println("   tempPath = [" + tempPath + "]");

		} catch (FileNotFoundException exception) {
			System.out.println("bufferedReader exception: " + exception.getMessage());
			return 1;
		}

		while ((lineLength = br.readLine()) != null) {
			if(lineLength.length()<18)
				continue;
			totalCnt ++;
			byte[] bytes = lineLength.getBytes("MS950");
			idNo = comc.subMS950String(bytes, 0, 10).trim();//身分證號碼
			applyDate = zzdate.tw2adDate(comc.subMS950String(bytes, 11, 7).trim());//申辦日期 轉西元

			if(totalCnt % 5000 == 0) {
				showLogMessage("I", "", "read row =  [" + totalCnt + "]");
				commitDataBase();
			}

			insertMktEadigitalOpen();
		}
		br.close();
		return 0;
	}
	
//=============================================================================	
    int insertMktEadigitalOpen() throws Exception {
		setValue("ID_NO", idNo);
		setValue("APPLY_DATE", applyDate);
		setValue("PROC_YYYYMM", zzstr.left(busiDate, 6));
		setValue("MOD_USER",javaProgram);
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		daoTable = "MKT_EADIGITAL_OPEN";

		insertTable();
		if("Y".equals(dupRecord)) {
			dupRecordCnt++;
			return (1);
		}
		return (0);
	}

//=============================================================================
	void renameFile(String fileName) throws Exception {
		String tmpStr1 = String.format("%s%s/%s", comc.getECSHOME(), FOLDER ,fileName);
		String tempPath1 = SecurityUtil.verifyPath(tmpStr1);
		String tmpStr2 = String.format("%s%s/backup/%s_%s", comc.getECSHOME(), FOLDER ,fileName,sysDate+sysTime);
		String tempPath2 = SecurityUtil.verifyPath(tmpStr2);

		if (!comc.fileMove(tempPath1, tempPath2)) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 備份至 [" + tempPath2 + "]");
	}
	
}

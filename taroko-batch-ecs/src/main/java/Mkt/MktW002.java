package Mkt;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112-06-28  V0.00.00     Ryan     initial                                   *
*****************************************************************************/

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

public class MktW002 extends AccessDAO {
	private String PROGNAME = "數位帳戶開戶ID檔(G檔) 接收寫檔處理 112/06/28 V0.00.00";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommDate zzdate = new CommDate();
	private static final String FOLDER = "/media/mkt";
	private static final String FILE_NAME = "DIGACCT_ID_YYYYMMDD";
	int totalCnt = 0;
	private String lineLength = "";
	private String idNo = "";
	private String digitalActno = "";
	private String openDate = "";
	private String procDate = "";
	private String busiDate = "";
	
//=**************************************************************************** 
	public static void main(String[] args) {
		MktW002 proc = new MktW002();
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
			showLogMessage("I", "", "本日營業日 =  [" + busiDate + "]");

			String fileName = FILE_NAME.replace("YYYYMMDD", busiDate);

			if (openFile(fileName) == 0) {
				readFile(fileName);
				renameFile(fileName);
			}
			showLogMessage("I", "", "資料處理筆數 =  [" + totalCnt + "]");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
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
			if(lineLength.length()<37)
				continue;
			totalCnt ++;
			byte[] bytes = lineLength.getBytes("MS950");
			procDate = comc.subMS950String(bytes, 0, 6).trim();//資料提供年月
			idNo = comc.subMS950String(bytes, 6, 10).trim();//正卡人身分證字號
			digitalActno = comc.subMS950String(bytes, 16, 13).trim();//數位存款帳號
			openDate = comc.subMS950String(bytes, 29, 8).trim();//開戶日

			if(totalCnt % 5000 == 0)
				showLogMessage("I", "", "read row =  [" + totalCnt + "]");
			
			insertMktDigitalactOpen();
		}
		br.close();
		return 0;
	}
	
//=============================================================================		
	int deleteMktPbmbatm() throws Exception {
        daoTable = "MKT_DIGITALACT_OPEN";
        whereStr = "WHERE PROC_YYYYMM = ? ";
        setString(1, zzstr.left(busiDate, 6));
        deleteTable();
        return 0;
	}
	
//=============================================================================	
    int insertMktDigitalactOpen() throws Exception {
		setValue("ID_NO", idNo);
		setValue("DIGITAL_ACTNO", digitalActno);
		setValue("OPEN_DATE", openDate);
		setValue("PROC_YYYYMM", procDate );
		setValue("MOD_USER",javaProgram);
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		daoTable = "MKT_DIGITALACT_OPEN";

		insertTable();
		if("Y".equals(dupRecord)) {
			showLogMessage("I", "", String.format("insert mkt_digitalact_open dupRecord ,ID_NO = [%s] "
					+ ",DIGITAL_ACTNO = [%s] ,OPEN_DATE = [%s] ,PROC_YYYYMM = [%s]"
					,idNo,digitalActno,openDate,procDate));
			return (1);
		}
		commitDataBase();
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

package Mkt;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112-05-29  V0.00.01     Ryan     initial                                   *
* 112-06-19  V0.00.02     Ryan     modify                                   *
* 112-06-30  V0.00.03     Ryan     增加存款帳號                                                                                              *
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

public class MktFr01 extends AccessDAO {
	private String PROGNAME = "接收 ap1 產出近1/3/6個月平均存款餘額處理程式 112/06/30 V0.00.03";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommDate zzdate = new CommDate();
	private static final String FOLDER = "/media/mkt";
	private static final String FILE_NAME = "PBMB_ATM_YYYYMMDD";
	int totalCnt = 0;
	private String lineLength = "";
	private String dataMonth = "";
	private String idNo = "";
	private String cardNo = "";
	private String acctNo = "";
	private long atmTxtimes = 0;
	private long atmCbfee = 0;
	private long avg1mAmt = 0;
	private long avg3mAmt = 0;
	private long avg6mAmt = 0;
	private String procDate = "";
	
//=**************************************************************************** 
	public static void main(String[] args) {
		MktFr01 proc = new MktFr01();
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

			procDate = sysDate;
			String parm1 = "";
			if (args.length == 1) {
				parm1 = args[0];
				if (parm1.length() != 8 || !zzdate.isDate(parm1)) {
					showLogMessage("I", "", "請傳入參數合格值yyyymmdd[" + parm1 + "]");
					return 1;
				}
				procDate = parm1;
			}

			if (!"01".equals(zzstr.right(procDate, 2))) {
				showLogMessage("I", "", "非每月1日，程式不執行[" + procDate + "]");
				return 0;
			}

			showLogMessage("I", "", "傳入參數日期 = [" + parm1 + "]");
			showLogMessage("I", "", "取得處理日 =  [" + procDate + "]");

			String fileName = FILE_NAME.replace("YYYYMMDD", procDate);

			int rtnCode = 0;
			rtnCode = openFile(fileName);
			if (rtnCode  == 0) {
				rtnCode = readFile(fileName);
				renameFile(fileName);
			}
			finalProcess();
			return rtnCode;
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
        	showLogMessage("D", "", "無檔案可處理,連繫ap1業務人員傳檔  " + "");
        	return 7;
        }
        closeInputText(f);
		return (0);
	}

//=============================================================================
	int readFile(String fileName) throws Exception {
		System.out.println("");
		System.out.println("====Start Read File ====");
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
			if(lineLength.length()<61)
				continue;
			totalCnt ++;
			byte[] bytes = lineLength.getBytes("MS950");
			idNo = comc.subMS950String(bytes, 6, 10).trim();
//			dataType = comc.subMS950String(bytes, 16, 1).trim();
//			cardNo = comc.subMS950String(bytes, 17, 16).trim();
			acctNo = comc.subMS950String(bytes, 16, 13).trim();
			avg1mAmt = ss2Long(comc.subMS950String(bytes, 29, 12).trim());
			avg3mAmt = ss2Long(comc.subMS950String(bytes, 41, 12).trim());
			avg6mAmt = ss2Long(comc.subMS950String(bytes, 53, 12).trim());
			atmTxtimes = ss2Long(comc.subMS950String(bytes, 65, 4).trim());
			atmCbfee = ss2Long(comc.subMS950String(bytes, 69, 5).trim());
			if(totalCnt == 1) {
				dataMonth = comc.subMS950String(bytes, 0, 6).trim();
				String procMonth = zzdate.monthAdd(procDate, -1);
				if(!procMonth.equals(dataMonth)) {
					showLogMessage("I", "", "PBMB_ATM_YYYYMMDD資料日期有誤,日期應為["+procMonth+"] ,連繫ap1業務人員傳檔");
					br.close();
					return 7;
				}
				deleteMktPbmbatm();
			}
			insertMktPbmbatm();
		}
		br.close();
		if(totalCnt==0) {
			showLogMessage("I", "", "PBMB_ATM_YYYYMMDD資料內容空檔,連繫ap1業務人員傳檔");
			return 7;
		}
		return 0;
	}
	
//=============================================================================		
	int deleteMktPbmbatm() throws Exception {
        daoTable = "MKT_PBMBATM";
        whereStr = "WHERE DATA_MONTH = ? ";
        setString(1, dataMonth);
        deleteTable();
        commitDataBase();
        return 0;
	}
	
//=============================================================================	
    int insertMktPbmbatm() throws Exception {
		setValue("DATA_MONTH", dataMonth);
		setValue("ID_NO", idNo);
		setValue("CARD_NO", cardNo );
		setValue("ACCT_NO", acctNo );
		setValueLong("AVG1M_AMT", avg1mAmt);
		setValueLong("AVG3M_AMT", avg3mAmt);
		setValueLong("AVG6M_AMT", avg6mAmt);
		setValueLong("ATM_TXTIMES", atmTxtimes);
		setValueLong("ATM_CBFEE", atmCbfee);
		setValue("MOD_USER",javaProgram);
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		daoTable = "MKT_PBMBATM";

		insertTable();
		if("Y".equals(dupRecord)) {
			showLogMessage("I", "", "insert mkt_pbmbatm dupRecord DATA_MONTH = [" + dataMonth + "] ,ID_NO = [" + idNo + "] ,ACCT_NO = [" + acctNo + "]");
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

		if (!comc.fileCopy(tempPath1, tempPath2)) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]備份失敗!");
			return;
		}
		comc.fileDelete(tempPath1);
		showLogMessage("I", "", "檔案 [" + fileName + "] 備份至 [" + tempPath2 + "]");
	}
	
	long ss2Long(String param) {
		try {
			param = param.trim().replaceAll(",", "");
			if (zzstr.empty(param) || !zzstr.isNumber(param)) {
				return 0;
			}
			return Long.parseLong(param);
		} catch (Exception ex) {
			return 0;
		}
	}
}

package Cyc;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 111-04-15  V0.00.01     Ryan     initial                                   *
*****************************************************************************/

import com.CommCrd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.List;

import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

import Dxc.Util.SecurityUtil;

public class CycG020 extends BaseBatch {
	private String PROGNAME = "接收銀行利害關係人名單作業(IPP02F01.TXT) 112/11/28 V0.00.01";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommDate zzdate = new CommDate();
	private final String FILE_NAME = "IPP02F01.TXT";
	private final String FILE_PATH = "/media/cyc/";
	private final String CRDATAUPLOAD = "/crdataupload/";
	int commit = 1;
	private String lineLength = "";
	private String hCorrelateId = "";
	private String hRelateStatus = "";

//=**************************************************************************** 
	public static void main(String[] args) {
		CycG020 proc = new CycG020();
		proc.mainProcess(args);
		proc.systemExit();
	}

//=============================================================================
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(PROGNAME);
		dbConnect();
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		getFile();
		openFile();
		sqlCommit(commit);
		endProgram();
	}

//=============================================================================
	int openFile() throws Exception {
		String file = Paths.get(comc.getECSHOME(),FILE_PATH, FILE_NAME).toString();
		if (openBinaryInput(file) == false) {
			showLogMessage("I", "", "ERROR : 檔案檔案不存在[" + file + "]");
			return 0;
		}
		readFile(file);
		renameFile();

		return (0);
	}

//=============================================================================
	void readFile(String tempPath) throws Exception {
		System.out.println("");
		System.out.println("====Start Read File ====");
		BufferedReader br = null;
		try {
			FileInputStream fis = new FileInputStream(new File(tempPath));
			br = new BufferedReader(new InputStreamReader(fis, "MS950"));
		} catch (FileNotFoundException exception) {
			showLogMessage("I", "", "ERROR : [" + exception.getMessage() + "]");
			return;
		}
		deleteCrdCorrelate();
		
		while ((lineLength = br.readLine()) != null) {
			hCorrelateId = "";
			hRelateStatus = "";
			totalCnt++;
			byte[] bytes = lineLength.getBytes("MS950");
			if (bytes.length < 12)
				continue;
			if ((totalCnt % 5000) == 0) {
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
			}
			hCorrelateId = comc.subMS950String(bytes, 0, 10).trim();
			hRelateStatus = comc.subMS950String(bytes, 11, 1).trim();
			
			insertCrdCorrelate();
		}
		if (totalCnt == 0) {
			showLogMessage("D", "", "無檔案可處理  " + "");
			commit = 0;
		}
		br.close();
	}
	
    private void deleteCrdCorrelate() throws Exception {
        daoTable = "CRD_CORRELATE";
        whereStr = "WHERE CORRELATE_ID_CODE = '' ";
        deleteTable();

    }

//=============================================================================

	void insertCrdCorrelate() throws Exception {
		daoTable = "CRD_CORRELATE";
		setValue("CRT_DATE",sysDate);
		setValue("CORRELATE_ID",hCorrelateId);
		setValue("CORRELATE_ID_CODE","");
		setValue("RELATE_STATUS",hRelateStatus);
		setValue("FH_FLAG","");
		setValue("BK_FLAG","Y");
		setValue("ACCT_TYPE",hCorrelateId.length() == 10 ? "01" : "03");
		setValue("MOD_USER",javaProgram);
		setValue("MOD_TIME",sysDate + sysTime);
		setValue("MOD_PGM",javaProgram);
		insertTable();
		if("Y".equals(dupRecord)) {
			showLogMessage("I", "", String.format("insertCrdCorrelate dupRecord ,CORRELATE_ID = [%s]", hCorrelateId));
		}
	}

//=============================================================================
	void renameFile() throws Exception {
		String temstr1 = Paths.get(comc.getECSHOME(),FILE_PATH, FILE_NAME).toString();
		String tmpstr2 =  Paths.get(comc.getECSHOME(),FILE_PATH,String.format("/backup/%s.%s", FILE_NAME,sysDate+sysTime)).toString();
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		tmpstr2 = Normalizer.normalize(tmpstr2, java.text.Normalizer.Form.NFKD);
		if (comc.fileMove(temstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + FILE_NAME + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + FILE_NAME + "] 已移至 [" + tmpstr2 + "]");
	}
	
	void getFile() throws Exception {
		String temstr1 = Paths.get(comc.getECSHOME(),FILE_PATH, FILE_NAME).toString();
		String temstr2 = Paths.get(CRDATAUPLOAD, FILE_NAME).toString();
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
		comc.fileMove(temstr2, temstr1);
	}
}

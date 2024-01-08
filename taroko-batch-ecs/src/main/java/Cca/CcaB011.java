package Cca;

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
import java.util.List;

import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

import Dxc.Util.SecurityUtil;

public class CcaB011 extends BaseBatch {
	private String PROGNAME = "VD沖正回饋檔處理 111/04/15 V0.00.01";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommDate zzdate = new CommDate();
	private String isFileName = "VD_REVERSE_RESP.";
	int commit = 1;
	private String lineLength = "";
	private String hCardNo = "";
	private String hTxSeq = "";
	private String hSeqNo = "";
	private String hRespCode = "";

//=**************************************************************************** 
	public static void main(String[] args) {
		CcaB011 proc = new CcaB011();
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

		openFile();
		sqlCommit(commit);
		endProgram();
	}

//=============================================================================
	int openFile() throws Exception {
		int fileCount = 0;

		List<String> listOfFiles = comc.listFS("/crdataupload/", "", "");

		for (String file : listOfFiles) {
			if (file.length() < 24)
				continue;
			if (!file.substring(0, 16).equals(isFileName))
				continue;
			fileCount++;
			readFile(file);
			renameFile(file);
		}
		if (fileCount < 1) {
			showLogMessage("D", "", "無檔案可處理  " + "");
		}
		return (0);
	}

//=============================================================================
	void readFile(String fileName) throws Exception {
		System.out.println("");
		System.out.println("====Start Read File ====");
		BufferedReader br = null;
		try {
			String tmpStr = String.format("/crdataupload/%s", fileName);
			String tempPath = SecurityUtil.verifyPath(tmpStr);
			FileInputStream fis = new FileInputStream(new File(tempPath));
			br = new BufferedReader(new InputStreamReader(fis, "MS950"));

			System.out.println("   tempPath = [" + tempPath + "]");

		} catch (FileNotFoundException exception) {
			System.out.println("bufferedReader exception: " + exception.getMessage());
			return;
		}

		while ((lineLength = br.readLine()) != null) {
			byte[] bytes = lineLength.getBytes("MS950");
			hSeqNo = comc.subMS950String(bytes, 0, 1).trim();
			if (!hSeqNo.equals("2"))
				continue;
			hRespCode = comc.subMS950String(bytes, 28, 4).trim();
//			showLogMessage("I", "", "hRespCode = [" + hRespCode + "]");
			
			if(!hRespCode.equals("0000")) {
				continue;
			}
			hCardNo = comc.subMS950String(bytes, 35, 37).trim();
			hTxSeq = comc.subMS950String(bytes, 117, 10).trim();

			hCardNo = zzstr.mid(hCardNo, 0, 16);
			hTxSeq = zzstr.mid(hTxSeq, 4, 6);

			showLogMessage("I", "", "hCardNo = [" + hCardNo + "]");
			showLogMessage("I", "", "hTxSeq = [" + hTxSeq + "]");
			
			updateReversalLog();
		}
		br.close();

	}

//=============================================================================

	void updateReversalLog() throws Exception {
		sqlCmd = " update cca_auth_txlog set cacu_amount = 'N',cacu_cash = 'N' ,logic_del = 'R' ,reversal_flag = 'Y' ,unlock_flag = 'R' ";
		sqlCmd	+= ",chg_date = to_char(sysdate,'yyyymmdd') ,chg_time = to_char(sysdate,'hh24miss') ,chg_user = 'CcaB011' ,mod_time = sysdate ";
		sqlCmd  += " where cacu_amount = 'Y' and reversal_flag = 'N' and card_no = ? and substring(tx_seq,5,6) = ? ";
		sqlExec(new Object[] { hCardNo, hTxSeq});
		if (sqlNrow > 0) {
			totalCnt ++;
			commitDataBase();
		}
	}

//=============================================================================
	void renameFile(String fileName) throws Exception {
		String tmpstr1 = String.format("/crdataupload/%s", fileName);
		String tmpstr2 = String.format("/crdataupload/backup/%s", fileName);

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
	}
}

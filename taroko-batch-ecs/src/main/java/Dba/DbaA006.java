/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-07-22  V1.00     yanghan    修改了變量名稱和方法名稱                                                                   *
* 109-09-11  V1.01     Alex       檔名修正                                                                                                 *
* 109-10-19  V1.00.02  shiyuqi    updated for project coding standard        *
* 110-03-17  V1.00.03  Alex		      修正改以 Card_no,ref_no update cca_auth_txlog *
* 111-03-23  V1.00.04  JeffKung   調整至換日後執行                                                                *
* 111-03-28  V1.00.05  JeffKung   解圈失敗也異動cca_auth_txlog                            *
* 111-04-11  V1.00.06  Alex       update cca_auth_txlog 時 where 增加 tx_seq    *
******************************************************************************/
package Dba;

import com.CommCrd;
import com.CommFunction;
import com.BaseBatch;
import com.CommDate;
import com.CommString;

public class DbaA006 extends BaseBatch {
	private final String progname = "收批次解圈回覆檔 110/04/11  V1.00.06";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	private int iiFileNum = 0;
	String fileName = "";
	String hFileDate = "";
	double hTotFileCnt = 0;
	double hTotFileAmt = 0;
	double hFileAmt = 0;
	String hTxSeq = "";
	String hTxCode = "";
	String hDatnDeductSeq = "";
	String hDeductProcCode = "";
	String hRefNo = "";
	String hCardNo = "";
	public static void main(String[] args) {
		DbaA006 proc = new DbaA006();
//		proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : DbaA006 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		} else {
			//hBusiDate = comc.getBusiDate();
			hBusiDate = comm.nextNDate(hBusiDate, -1);  //改為營業日前一天
		}
		
		printf("-->參數: 處理檔案日期=[" + hBusiDate + "]");
			
		    
		dateTime();

		fileName = "VDD01_RSP." + hBusiDate;
		checkOpen();
		processData();
		endProgram();
	}

	void processData() throws Exception {
		String firstStr = "";
		double procAmt = 0.0, procCnt = 0 , tlAmt = 0.0 , tlCnt = 0;
		while (true) {
			String fileData = readTextFile(iiFileNum);
			if (endFile[iiFileNum].equals("Y")) {
				break;
			}
			if (empty(fileData))
				break;
			firstStr = "";
			initData();
			firstStr = commString.bbMid(fileData, 0,1);
			if (firstStr.equals("1")) {
				// --首筆資料
				splitData(fileData, firstStr);				
				if (eqIgno(hFileDate, hBusiDate) == false) {
					errmsg("檔案日期不符");
					errExit(1);
				}
			} else if (firstStr.equals("2")) {
				// --明細資料				
				splitData(fileData, firstStr);
				tlCnt ++ ;
				tlAmt += hFileAmt;
				if (eqIgno(hTxCode, "D01") == false )
					continue;
				totalCnt++;
				procAmt += hFileAmt;
				
				getRefNo();
				
				if(hRefNo.isEmpty() || hCardNo.isEmpty()) {
					showLogMessage("I", "", "查無此筆資料 Ddeduct_seq = " + hDatnDeductSeq);
					continue;
				}
				updateDbaAcaj();
				updateCcaAuthTxlog();
				
			} else if (firstStr.equals("3")) {
				// --尾筆資料
				splitData(fileData, firstStr);
				if (hTotFileCnt != tlCnt) {
					sqlRollback();
					errmsg("檔案筆數不符");
					errExit(1);
				}
				if (hTotFileAmt != tlAmt) {
					sqlRollback();
					errmsg("檔案金額不符");
					errExit(1);
				}
			}
		}

		commitDataBase();
		closeInputText(iiFileNum);
		renameFile();
	}

	void updateCcaAuthTxlog() throws Exception {

		daoTable = "cca_auth_txlog";
		updateSQL += " unlock_flag = 'E' , ";
		updateSQL += " cacu_amount ='N' , ";
		updateSQL += " mod_pgm = ? , ";
		updateSQL += " mod_time = sysdate , ";
		updateSQL += " mod_user = 'system' , ";
		updateSQL += " mod_seqno = nvl(mod_seqno,0)+1  ";
		whereStr = " where card_no = ? and ref_no = ? and tx_seq = ? ";
		if (eqIgno(hDeductProcCode, "00") == false)
		{
			setString(1, "DbaA006ERR");
		} else {
			setString(1, "DbaA006");
		}
		setString(2, hCardNo);
		setString(3, hRefNo);
		setString(4, hTxSeq);
		
		int r = updateTable();
		if (r <= 0) {
			errmsg("查無該筆資料 Ref = [%s]", hRefNo);
			//errExit(1);
		}

	}

	void updateDbaAcaj() throws Exception {
		daoTable = "dba_acaj";
		updateSQL = "   proc_flag = 'Y', ";
		updateSQL += "  deduct_proc_date = ?, ";
		updateSQL += "  deduct_proc_time = to_char(sysdate,'hh24miss'), ";
		updateSQL += "  deduct_proc_code = ?,  ";
		updateSQL += "  mod_time = sysdate, ";
		updateSQL += "  mod_pgm = 'DbaA006' ";
		whereStr = " where deduct_seq = ? ";
		setString(1, hBusiDate);
		setString(2, hDeductProcCode);
		setString(3, hDatnDeductSeq);

		updateTable();

		if (notFound.equals("Y")) {
			errmsg("查無該筆圈存序號 [%s]", hTxSeq);
			//errExit(1);
		}
	}

	void splitData(String lsFileData, String procLine) throws Exception {
		byte[] bytes = lsFileData.getBytes("MS950");
		if (procLine.equals("1")) {
			hFileDate = comc.subMS950String(bytes, 4, 8).trim();
		} else if (procLine.equals("2")) {
			hTxCode = comc.subMS950String(bytes, 4, 3).trim();
			hTxSeq = comc.subMS950String(bytes, 7, 10).trim();
			hFileAmt = commString.ss2Num(comc.subMS950String(bytes, 25, 12).trim());
			hDatnDeductSeq = comc.subMS950String(bytes, 66, 15).trim();
			hDeductProcCode = comc.subMS950String(bytes, 91, 2).trim();
		} else if (procLine.equals("3")) {
			hFileDate = comc.subMS950String(bytes, 4, 8).trim();
			hTotFileCnt = commString.ss2Num(comc.subMS950String(bytes, 12, 10).trim());
			hTotFileAmt = commString.ss2Num(comc.subMS950String(bytes, 22, 14).trim());
		}
	}

	void initData() {
		hFileAmt = 0;
		hTxSeq = "";
		hTxCode = "";
		hDatnDeductSeq = "";
		hDeductProcCode = "";
		hRefNo = "";
		hCardNo = "";
	}

	void checkOpen() throws Exception {
		String lsFile = String.format("%s/media/dba/%s", getEcsHome(), fileName);

		iiFileNum = openInputText(lsFile);
		if (iiFileNum == -1) {
			showLogMessage("I", "", "無檔案可處理 !");
//			errmsg("在程式執行目錄下沒有權限讀寫資料 [%s]", fileName);
//			errExit(1);
			okExit(0);	
		}
		return;
	}
	
	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/dba/%s", getEcsHome(), fileName);
		String tmpstr2 = String.format("%s/media/dba/backup/%s", getEcsHome(), fileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
	}
	
	void getRefNo() throws Exception {
		
		String sql1 = "select reference_no , card_no from dba_acaj where deduct_seq = ? ";		
		sqlSelect(sql1,new Object[] {hDatnDeductSeq});
		if(sqlNrow >0) {
			hRefNo =  colSs("reference_no");
			hCardNo = colSs("card_no");
		}
		
		return ;
	}
	
}

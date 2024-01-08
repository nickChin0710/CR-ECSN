/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/07/05  V1.00.01   JeffKung     program initial                        *
*****************************************************************************/
package Tmp;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxInf;

public class TmpB012 extends AccessDAO {
	private final String progname = "處理分期專款專用上傳資料檔程式  112/07/05 V1.00.01";

	CommCrd comc = new CommCrd();
	CommFunction comm = new CommFunction();
	CommCrdRoutine comcr = null;

	String cardNo = "";
	String purchaseDate = "";
	long totAmt = 0;
	String authCode = "";
	int totTerm = 0;
	String procDate = "";
	
	int updateCnt = 0;

	public int mainProcess(String[] args) {

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			searchDate = getProgDate(searchDate, "D");
			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

			if (processFileCTR(searchDate) != 0) {
				showLogMessage("E", "", "====處理CTR.TXT檔,執行有誤====");
			} else {
				showLogMessage("I", "", "====處理CTR.TXT檔,執行結束====");
			}

			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}

	/***********************************************************************/
	int processFileCTR(String searchDate) throws Exception {
		int totalCnt = 0;
		int rowCount = 0;
		String str600 = "";
		String fileName = "CTR.TXT";
		String inputFileName = String.format("%s/CTR.TXT", "/crdataupload/");
		int br = openInputText(inputFileName, "MS950");
		if (br == -1) {
			showLogMessage("I", "", String.format("本日無上傳檔需處理,[%s]", inputFileName));
			return -1;
		}

		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;

			if (str600.length() < 16)
				continue; // 傳入不滿16碼,跳過

			byte[] bytes = str600.getBytes("MS950");
			cardNo = comc.subMS950String(bytes, 0, 16).trim();
			purchaseDate = comc.subMS950String(bytes, 16, 8).trim();
			totAmt = Long.parseLong(comc.subMS950String(bytes, 26, 10).trim());
			authCode = comc.subMS950String(bytes, 40, 6).trim();
			totTerm = Integer.parseInt(comc.subMS950String(bytes, 49, 2).trim());
			procDate = comc.subMS950String(bytes, 66, 8).trim();
			
			totalCnt++;

			selectBilContract();

		}

		closeInputText(br);

		showLogMessage("I", "", String.format("處理CTR.TXT檔完成！，共異動%d筆資料", updateCnt));

		return 0;
	}

	private String selectBilContract() throws Exception {
		String result = "";
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT card_no, rowid as rowid ");
		sb.append(" FROM bil_contract a ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND card_no = ? ");
		sb.append(" AND spec_flag <> 'Y' ");
		sb.append(" AND mcht_no = '106000000002' ");
		sb.append(" AND purchase_date = ? ");
		sb.append(" AND tot_amt = ? ");
		sb.append(" AND auth_code = ? ");
		sb.append(" AND install_tot_term = ? ");
		sb.append(" AND new_proc_date = ? ");
		sb.append(" fetch first 1 rows only ");
		sqlCmd = sb.toString();
		setString(1, cardNo);
		setString(2, purchaseDate);
		setLong(3, totAmt);
		setString(4, authCode);
		setInt(5, totTerm);
		setString(6, procDate);
		
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			result="Y";
			updateBilContract(getValue("rowid"));
		} else {
			showLogMessage("I", "", String.format("NotFound bil_contract, card_no=[%s],total_amt=[%d],total_term[%d]", cardNo,totAmt,totTerm));
		}

		return result;

	}

	// ************************************************************************
		void updateBilContract(String rowid) throws Exception {
			updateSQL = "spec_flag = 'Y' "; 
			daoTable  = "bil_contract";
			whereStr  = "WHERE  rowid   = ? ";
			setRowId(1, rowid);

			updateTable();
			if (!notFound.equals("Y"))
				updateCnt++;

		}

	public static void main(String[] args) {
		TmpB012 proc = new TmpB012();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}

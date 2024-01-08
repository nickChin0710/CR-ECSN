/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/07/04  V1.00.01   JeffKung     program initial                        *
*****************************************************************************/
package Mkt;

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

public class MktR710 extends AccessDAO {
	private final String progname = "處理財管上傳資料檔程式  112/07/04 V1.00.01";
	private static final String MKT_FOLDER = "media/mkt/";

	CommCrd comc = new CommCrd();
	CommFunction comm = new CommFunction();
	CommCrdRoutine comcr = null;

	String aumidDataMonth = "";
	String aumidDataType = "";
	String aumidIdPSeqno = "";
	String aumidIdNo = "";

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

			// 若不是月底日不執行
			if (!searchDate.equals(comm.lastdateOfmonth(searchDate))) {
				showLogMessage("I", "", String.format("月底日執行此程式,今日[%s]非月底日不需執行!!", searchDate));
				return 0;
			}

			if (processFileWealthVip(searchDate) != 0) {
				showLogMessage("E", "", "====處理WEALTHVIP_New.txt檔,執行有誤====");
			} else {
				showLogMessage("I", "", "====處理WEALTHVIP_New.txt檔,執行結束====");
			}

			if (processFilePbCust(searchDate) != 0) {
				showLogMessage("E", "", "====處理PBS_CUS_yyyymm.txt檔,執行有誤====");
			} else {
				showLogMessage("I", "", "====處理PBS_CUS_yyyymm.txt檔,執行結束====");
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
	int processFileWealthVip(String searchDate) throws Exception {
		int totalCnt = 0;
		int rowCount = 0;
		String str600 = "";
		String fileName = "WEALTHVIP_New.txt";
		String inputFileName = String.format("%s/media/mkt/WEALTHVIP_New.txt", comc.getECSHOME());
		int br = openInputText(inputFileName, "MS950");
		if (br == -1) {
			showLogMessage("I", "", String.format("本日無上傳檔需處理,[%s]", inputFileName));
			return -1;
		}

		String custId = "";
		String vipLevel = "";
		String idPSeqno = "";

		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;

			if (str600.length() < 14)
				continue; // 傳入不滿14碼,跳過

			byte[] bytes = str600.getBytes("MS950");
			custId = comc.subMS950String(bytes, 0, 10).trim();
			vipLevel = comc.subMS950String(bytes, 11, 2).trim();

			//showLogMessage("I", "", String.format("Debug-vipLevel=[%s]", vipLevel));

			totalCnt++;

			if ((totalCnt % 3000) == 0)
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

			idPSeqno = selectCrdIdno(custId);
			if ("".equals(idPSeqno))
				continue; // 非持卡人

			if ("PL".equals(vipLevel) == false)
				continue; // PL:白金貴賓戶

			rowCount++;

			aumidDataMonth = comc.getSubString(searchDate, 0, 6);
			aumidDataType = "V1";
			aumidIdPSeqno = idPSeqno;
			aumidIdNo = custId;

			insertCycAfeeAUMID();

		}

		closeInputText(br);

		showLogMessage("I", "", String.format("處理WEALTHVIP_New.txt檔完成！，共新增%d筆資料", rowCount));

		backupRtn(fileName);
		return 0;
	}

	/***********************************************************************/
	int processFilePbCust(String searchDate) throws Exception {
		int totalCnt = 0;
		int rowCount = 0;
		String str600 = "";
		String fileName = String.format("PBS_CUS_%s.txt",comc.getSubString(searchDate, 0, 6));
		String inputFileName = String.format("%s/media/mkt/PBS_CUS_%s.txt", comc.getECSHOME(),
				comc.getSubString(searchDate, 0, 6));
		int br = openInputText(inputFileName, "MS950");
		if (br == -1) {
			showLogMessage("I", "", String.format("本日無上傳檔需處理,[%s]", inputFileName));
			return -1;
		}

		String custId = "";
		String idPSeqno = "";

		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;

			if (str600.length() < 10)
				continue; // 傳入不滿10碼,跳過

			byte[] bytes = str600.getBytes("MS950");
			custId = comc.subMS950String(bytes, 0, 10).trim();

			totalCnt++;
			
			if ((totalCnt % 3000) == 0)
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

			idPSeqno = selectCrdIdno(custId);
			if ("".equals(idPSeqno))
				continue; // 非持卡人

			rowCount++;

			aumidDataMonth = comc.getSubString(searchDate, 0, 6);
			aumidDataType = "V3";
			aumidIdPSeqno = idPSeqno;
			aumidIdNo = custId;

			insertCycAfeeAUMID();

		}

		closeInputText(br);

		showLogMessage("I", "", String.format("處理PBS_CUS_yyyymm.txt檔完成！，共新增%d筆資料", rowCount));

		backupRtn(fileName);
		return 0;
	}

	private String selectCrdIdno(String custId) throws Exception {
		String idPSeqno = "";
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT  ID_P_SEQNO ");
		sb.append(" FROM CRD_IDNO a ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND ID_NO = ? ");
		sqlCmd = sb.toString();
		setString(1, custId);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			idPSeqno = getValue("id_p_seqno");
		}

		return idPSeqno;

	}

	int insertCycAfeeAUMID() throws Exception {

		setValue("DATA_MONTH", aumidDataMonth);
		setValue("DATA_TYPE", aumidDataType);
		setValue("ID_P_SEQNO", aumidIdPSeqno);
		setValue("ID_NO", aumidIdNo);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);

		daoTable = "cyc_afee_aumid";

		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", " insert_cyc_afee error(dupRecord) :id_no=[" + aumidIdNo + "]");
		}

		return (0);
	}
	
	/***********************************************************************/
    void backupRtn(String fileName) throws Exception {
        String tmpstr1 = String.format("%s/media/mkt/%s", comc.getECSHOME(), fileName);
        String tmpstr2 = String.format("%s/media/mkt/backup/%s.%s", comc.getECSHOME(), fileName, sysDate);
        comc.fileCopy(tmpstr1, tmpstr2);
    }

	public static void main(String[] args) {
		MktR710 proc = new MktR710();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}

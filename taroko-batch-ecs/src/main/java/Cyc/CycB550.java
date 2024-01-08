/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/04/17  V1.00.01  JeffKung    program initial                           *
******************************************************************************/

package Cyc;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxInf;

public class CycB550 extends AccessDAO {
	private final String progname = "產生送郵件年費優惠到期提醒通知程式  112/04/17 V1.00.01";
	private static final String DAT_FOLDER = "media/cyc/";
	private static final String DATA_FORM = "ANNUAL_FEE_LIST";
	private final String lineSeparator = "\r\n";
	String keepIdNo            = "";
	String hBusiBusinessDate   = "";
	String hWdayStmtCycle      = "";
    String hWdayThisAcctMonth  = "";
    String hWdayLastAcctMonth  = "";

	CommCrd comc = new CommCrd();
	CommFunction comm = new CommFunction();

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
			// =====================================

			hBusiBusinessDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", hBusiBusinessDate));
			
			selectPtrBusinday();

            if (selectPtrWorkday()!=0)
            {
                showLogMessage("I","","本日非關帳日次一日, 不需執行");
                return(0);
            }

			// convert YYYYMMDD
			String fileNameSearchDate = hBusiBusinessDate;
			String datFileName = "";
			String fileFolder = "";
			int dataCount = 0;

			//get the name and the path of the DAT file
			datFileName = String.format("%s_%s.%s", DATA_FORM, fileNameSearchDate, "dat");
			fileFolder = Paths.get(comc.getECSHOME(), DAT_FOLDER).toString();
			dataCount = generateDatFile(fileFolder, datFileName, fileNameSearchDate);
			procFTP(fileFolder, datFileName);
			
			showLogMessage("I", "", "執行結束");
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}
	
    // ************************************************************************
    public void  selectPtrBusinday() throws Exception
    {
        daoTable  = "PTR_BUSINDAY";
        whereStr  = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if ( notFound.equals("Y") )
        {
            showLogMessage("I","","select ptr_businday error!" );
            exitProgram(1);
        }

        if (hBusiBusinessDate.length()==0)
            hBusiBusinessDate   =  getValue("BUSINESS_DATE");
        showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
    }
    // ************************************************************************
    public int  selectPtrWorkday() throws Exception
    {
    	
    	selectSQL = "";
        daoTable  = "ptr_workday";
        whereStr  = "where this_close_date = ? ";

        setString(1,comm.lastDate(hBusiBusinessDate));

        int recCnt = selectTable();

        if ( notFound.equals("Y") ) return(1);

        hWdayStmtCycle      =  getValue("STMT_CYCLE");
        hWdayThisAcctMonth  =  getValue("this_acct_month");
        hWdayLastAcctMonth  =  getValue("last_acct_month");

        return(0);
    }

    private int generateDatFile(String fileFolder, String datFileName, String searchDate) throws Exception {

		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}

		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生dat檔......");
			selectCycAfeeData(searchDate);
			while (fetchTable()) {
				
				if (getRowOfDAT()==0) {
					rowCount++;
				};

			}
			closeCursor();

			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入dat檔");
			} else {
				showLogMessage("I", "", String.format("產生dat檔完成！，共產生%d筆資料", rowCount));
			}

		} finally {
			closeBinaryOutput();
		}

		return rowCount;
	}

	/**
	 * 產生檔案.dat
	 * 
	 * @return String
	 * @throws Exception
	 */
	private int getRowOfDAT() throws Exception {

		String cardNo = "";

		StringBuffer sb = null;

		selectCrdIdno();
		if ("".equals(getValue("idno.e_mail_addr"))) {
			return -1;  //沒有留email addr不用產生資料
		}
		
		sb = new StringBuffer();
		sb.append(comc.fixLeft("00", 2));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(getValue("idno.id_no"), 11));  //正卡ID
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft("02", 2));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(getValue("idno.e_mail_addr"), 50));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft("合作金庫信用卡年費優惠到期提醒通知", 40));
		sb.append(comc.fixLeft("",10));

		sb.append("\r\n");
		
		byte[] tmpBytes = sb.toString().getBytes("MS950");
		writeBinFile(tmpBytes, tmpBytes.length);

		cardNo = getValue("CARD_NO");

		sb = new StringBuffer();
		sb.append(comc.fixLeft("01", 2));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft("親愛的卡友提醒您：<BR><BR>", 26));
		sb.append(comc.fixLeft("您的合庫信用卡末六碼", 20));
		sb.append(comc.fixLeft(comc.getSubString(cardNo, 10), 6));
		sb.append(comc.fixLeft("尚未符合年費優惠資格，請您詳見本行官網【利", 42));
		sb.append(comc.fixLeft("率與費用說明】，並於本月底前完成年費優惠條", 42));
		sb.append(comc.fixLeft("件，若有疑問請撥打卡片背面電話<BR><BR>", 40));
		sb.append(comc.fixLeft("謹慎理財信用至上，循環利率４．１５％～１４．７５％。", 52)); 

		sb.append("\r\n");

		tmpBytes = sb.toString().getBytes("MS950");
		writeBinFile(tmpBytes, tmpBytes.length);
		return 0;
	}
	

	private void selectCycAfeeData(String searchDate) throws Exception {

		selectSQL = "a.card_no,a.card_fee_date,a.id_p_seqno ";
        daoTable  = "cyc_afee a,crd_card b ";
        whereStr  = "where a.stmt_cycle           = ? "
                  + "and   a.card_no = b.card_no "
        		  + "and   b.card_note <> 'I' "              //排除無限卡及世界卡
                  + "and   a.maintain_code         != 'Y' "
                  + "and   a.data_type              = '2' "  //非首年年費
                  + "and   a.card_fee_date          = ? "    //開戶年月
                  + "and   a.rcv_annual_fee         > 0 ";

        setString(1 , hWdayStmtCycle);
        setString(2 , hWdayThisAcctMonth);

        openCursor();
	}

	private void selectCrdIdno() throws Exception {

		extendField = "idno.";
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT  ");
		sb.append("  ID_NO,E_MAIL_ADDR ");
		sb.append(" FROM CRD_IDNO ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND ID_P_SEQNO = ? ");

		sqlCmd = sb.toString();
		setString(1, getValue("id_p_seqno"));

		int cardCnt = selectTable();

		if (cardCnt == 0) {
			setValue("idno.e_mail_addr", "");
			setValue("idno.id_no", "");
		}

	}

	void procFTP(String fileFolder, String datFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		String ftpCommand = String.format("put %s ", datFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CRDATACREA", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	public static void main(String[] args) {
		CycB550 proc = new CycB550();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}

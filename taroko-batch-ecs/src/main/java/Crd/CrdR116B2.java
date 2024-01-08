/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/09/13  V1.00.00   Wilson   program initial                            *
*****************************************************************************/
package Crd;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;

public class CrdR116B2 extends AccessDAO {
	private final String PROGNAME = "產生財管白金貴賓客戶頂級卡推展信用卡情況明細表檔(各分行)程式  112/09/13  V1.00.00";
	private CommCrd comc = new CommCrd();
	private CommCrd commCrd = new CommCrd();
	private CommDate commDate = new CommDate();
	private CommCrdRoutine comcr = null;

	private final int OUTPUT_BUFF_SIZE = 55;
	private final String CRD_FOLDER = String.format("%s/media/crd", comc.getECSHOME());
	private final String MKT_FOLDER = String.format("%s/media/mkt", comc.getECSHOME());
	private final String DATA_FORM = "RCRD116B2";
	private String lineSeparator = "\n";

	String hCallBatchSeqno = "";
	private String searchDate = "";
	private String searchDateTw = "";
	private String searchDateTwYear = "";
	private String searchDateMonth = "";
	private String searchDateDay = "";

	String hChiYymmdd = "";
	String hBegDate = "";
	String hEndDate = "";
	String hBegDateBil = "";
	String hEndDateBil = "";
	int totalCnt = 0;
	int pageCnt1 = 0;
	int gdcTotalCnt = 0;

	Map<String, String> branchMap = new HashMap<>();

	public int mainProcess(String[] args) {

		try {
//            calBalance = new CalBalance(conn, getDBalias());
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			showLogMessage("I", "", "Usage CrdR116B2 [business_date]");

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			// =====================================

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			// get searchDate

			if (args.length >= 1) {
				searchDate = args[0];
				showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			} else {
				searchDate = new SimpleDateFormat("YYYYMMdd").format(new Date());
			}
//            searchDate = getProgDate(searchDate, "D");
			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
			searchDateTw = StringUtils.leftPad(commDate.toTwDate(searchDate), 7, "0");
			searchDateTwYear = searchDateTw.substring(0, searchDateTw.length() - 4);
			searchDateMonth = searchDateTw.substring(searchDateTw.length() - 4).substring(0, 2);
			searchDateDay = searchDateTw.substring(searchDateTw.length() - 2);

			selectPtrBusinday();

			// get the name and the path of the .DAT file
			String filename = String.format("%s.1.TXT", DATA_FORM);
			String fileFolder = Paths.get(CRD_FOLDER).toString();

			// 產生主要檔案
			int dataCount = generateFile(fileFolder, filename);

			// 產生Header檔
//            CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
//            dateTime(); // update the system date and time
//            boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, filename, searchDate,
//                    sysDate, sysTime.substring(0, 4), dataCount);
//            if (isGenerated == false) {
//                comc.errExit("產生HDR檔錯誤!", "");
//            }

			// CR_STATUS_YYMMDD.DAT -> CR_STATUS_YYMMDD.HDR
//            String hdrFileName =
//                    filename.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);

			// run FTP
			procFTP(fileFolder, filename, filename);

			// backup
			backup(filename);

			showLogMessage("I", "", "執行結束,筆數=[" + totalCnt + "]");
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}

	// (1)讀取理財客戶未持有本行信用卡明細檔資料並存到暫存資料表
	private List<WealthvipNew> readWealthvipNewFile() throws Exception {
		String filename = "WEALTHVIP_New.txt";
		String file = Paths.get(MKT_FOLDER, filename).toString();
		int fi = openInputText(file, "MS950");
		if (fi == -1) {
			showLogMessage("E", "", "讀取 WEALTHVIP_New.txt 失敗......");
			exitProgram(-1);
		}
		List<WealthvipNew> list = new ArrayList<>();
		while (true) {
			String line = readTextFile(fi);
			if (endFile[fi].equals("Y")) {
				break;
			}
			if (line == null || line.length() == 0) {
				continue;
			}
			String[] dataArray = line.split("\\|");
			WealthvipNew data = new WealthvipNew();
			data.idNo = dataArray[0];
			data.vipLevel = dataArray[1];
			// 過濾 VIP_LEVEL =‘PL’資料
			if (!"PL".equals(data.vipLevel)) {
				continue;
			}
			data.avgWth = dataArray[2];
			data.procId = dataArray[3];
			data.procBranch = dataArray[4];
			data.branchName = dataArray[5];
			data.phone = dataArray[6];
			data.mobile = dataArray[7];
			data.email = dataArray[8];
			data.cusName = dataArray[9];
			data.mailAddress = dataArray[10];
			data.fullChiName = branchMap.get(data.procBranch);
			list.add(data);
		}

		String backupFile = Paths.get(MKT_FOLDER, "backup", filename.replace(".txt", "_" + searchDate + ".txt"))
				.toString();
		commCrd.fileCopy(file, backupFile);

		return list;
	}

	/**
	 * generate a .Dat file
	 * 
	 * @param fileFolder  檔案的資料夾路徑
	 * @param datFileName .dat檔的檔名
	 * @return the number of rows written. If the returned value is -1, it means the
	 *         path or the file does not exist.
	 * @throws Exception
	 */
	private int generateFile(String fileFolder, String datFileName) throws Exception {

		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}

		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a
									// specified value
		try {
			StringBuffer sb = new StringBuffer(10240);
			showLogMessage("I", "", "開始產生.TXT檔......");

			// 處理產生財管白金貴賓客戶頂級卡推展信用卡情況明細表檔
			showLogMessage("I", "", "開始處理產生財管白金貴賓客戶頂級卡推展信用卡情況明細表檔......");
			// 讀取分行基本資料
			selectGenBrnData();
			// 讀取理財客戶未持有本行信用卡明細檔資料
			List<WealthvipNew> list = readWealthvipNewFile();
			// 按branch code 排序
			list.sort((a, b) -> a.procBranch.compareTo(b.procBranch));

			String lastBranch = "";
			int lineCnt = 0;
			int gdcCnt = 0;

			for (WealthvipNew wealthvipNew : list) {
				String branch = wealthvipNew.procBranch;
				// 讀取金鑽卡申辦註記及金鑽卡相關資料
				selectGoldDiamondCard(wealthvipNew);
				// 讀取持有其他卡註記
				selectOtherCardFlag(wealthvipNew);
				// 讀取員工註記
				selectUserFlag(wealthvipNew);
				// 讀取全新戶註記
				selectNewAccountFlag(wealthvipNew);

				if (!lastBranch.equals(branch)) {
					if (lastBranch.length() > 0) {
						countInEachBuffer += tail(sb, lineCnt, gdcCnt);
					}
					lastBranch = branch;
					pageCnt1 = 1;
					countInEachBuffer += header(sb, wealthvipNew);
					lineCnt = 0;
					gdcCnt = 0;
				}
				rowCount++;
				lineCnt++;
				if ("Y".equals(wealthvipNew.gdcApplyFlag)) {
					gdcCnt++;
					gdcTotalCnt++;
				}
				String rowOfDAT = getRowOfDAT(wealthvipNew);
				sb.append(rowOfDAT);
				countInEachBuffer++;
				totalCnt++;

				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                	pageCnt1++;
                	sb.append(lineSeparator);
                	header(sb, wealthvipNew);
					
//					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes("MS950");
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
			}
			if (lastBranch.length() > 0) {
				countInEachBuffer += tail(sb, lineCnt, gdcCnt);
			}
			// 統計
			totalTail(sb);

			// write the rest of bytes on the file
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes("MS950");
				writeBinFile(tmpBytes, tmpBytes.length);
			}

			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入.DAT檔");
			} else {
				showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
			}

		} finally {
			closeBinaryOutput();
		}

		return rowCount;
	}

	private void selectPtrBusinday() throws Exception {
		sqlCmd = "select to_char(add_months(to_date(?,'yyyymmdd'),-5),'yyyymm')||'01' h_beg_date_bil ";
		sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date_bil ";
		sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
		sqlCmd += "      , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
		sqlCmd += " from dual ";
		setString(1, searchDate);
		setString(2, searchDate);
		setString(3, searchDate);
		setString(4, searchDate);

		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hBegDateBil = getValue("h_beg_date_bil");
			hEndDateBil = getValue("h_end_date_bil");
			hBegDate = getValue("h_beg_date");
			hEndDate = getValue("h_end_date");
		}

		hChiYymmdd = commDate.toTwDate(searchDate);
		showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]", searchDate, hChiYymmdd, hBegDateBil,
				hEndDateBil, hBegDate, hEndDate));
	}

	// (2)讀取暫存資料表並依照分行代號歸類
	private void selectGenBrnData() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT                       ");
		sb.append("     B.BRANCH,                ");
		sb.append("     B.FULL_CHI_NAME          ");
		sb.append(" FROM                         ");
		sb.append("     GEN_BRN B                ");
		sb.append(" ORDER BY                     ");
		sb.append("     B.BRANCH                 ");

		sqlCmd = sb.toString();
		openCursor();
		branchMap = new LinkedHashMap<>();
		while (fetchTable()) {
			String branch = getValue("BRANCH");
			String fullChiName = getValue("FULL_CHI_NAME");
			branchMap.put(branch, fullChiName);
		}
		closeCursor();
	}

	// (3)讀取金鑽卡申辦註記及金鑽卡相關資料
	public void selectGoldDiamondCard(WealthvipNew wealthvipNew) throws Exception {
		sqlCmd = " SELECT                      " + "     CRD_CARD.CARD_NO,                "
				+ "     CRD_CARD.ORI_ISSUE_DATE,         " + "     CRD_CARD.ACTIVATE_FLAG,          "
				+ "     CRD_CARD.LAST_CONSUME_DATE       " + " FROM                        "
				+ "     CRD_CARD, CRD_IDNO                " + " WHERE  CRD_CARD.ID_P_SEQNO = CRD_IDNO.ID_P_SEQNO "
				+ " AND CRD_IDNO.ID_NO = ?   " + " AND CRD_CARD.GROUP_CODE = '1622' " + " ORDER BY                    "
				+ "     ISSUE_DATE DESC         " + " FETCH FIRST 1 ROWS ONLY     ";

		setString(1, wealthvipNew.idNo);// '暫存資料表的ID_NO')
		int recCnt = selectTable();

		if (notFound.equals("Y")) {
			// 金鑽卡申辦註記 = N
			wealthvipNew.gdcApplyFlag = "N";
		} else {
			wealthvipNew.gdcApplyFlag = "Y";
			wealthvipNew.cardNo = getValue("CARD_NO");
			wealthvipNew.oriIssueDate = getValue("ORI_ISSUE_DATE");
			wealthvipNew.activateFlag = getValue("ACTIVATE_FLAG");
			wealthvipNew.lastConsumeDate = getValue("LAST_CONSUME_DATE");

			if (comcr.str2long(wealthvipNew.lastConsumeDate) >= comcr.str2long(hBegDateBil)) {
				wealthvipNew.activeCardFlag = "Y";
			} else {
				wealthvipNew.activeCardFlag = "N";
			}
		}
	}

	// 讀取持有其他卡註記
	public void selectOtherCardFlag(WealthvipNew wealthvipNew) throws Exception {
		sqlCmd = " SELECT COUNT(*) as other_cnt                                                "
				+ "        FROM CRD_CARD                                                  "
				+ " WHERE ID_P_SEQNO = (SELECT ID_P_SEQNO FROM CRD_IDNO WHERE ID_NO = ? ) "
				+ " AND GROUP_CODE <> '1622'                                              ";

		setString(1, wealthvipNew.idNo);
		int recCnt = selectTable();

		if (getValueInt("other_cnt") > 0) {
			// 持有其他卡註記為Y
			wealthvipNew.otherCardFlag = "Y";
		} else {
			wealthvipNew.otherCardFlag = "N";
		}
	}

	// 讀取員工註記
	public void selectUserFlag(WealthvipNew wealthvipNew) throws Exception {
		sqlCmd = " SELECT STAFF_FLAG    " + "       FROM CRD_IDNO " + "WHERE ID_NO = ?      ";

		setString(1, wealthvipNew.idNo);
		int recCnt = selectTable();

		if (notFound.equals("Y") || !"Y".equals(getValue("STAFF_FLAG"))) {
			// 員工註記為N
			wealthvipNew.userFlag = "N";
		} else {
			// 有讀取到資料且STAFF_FLAG為Y則員工註記為Y
			wealthvipNew.userFlag = "Y";
		}
	}

	// 讀取全新戶註記
	public void selectNewAccountFlag(WealthvipNew wealthvipNew) throws Exception {
		sqlCmd = " SELECT COUNT(*) as new_account_cnt " + "  FROM CRD_CARD,CRD_IDNO "
				+ "WHERE ((CRD_CARD.CURRENT_CODE = '0') OR (CRD_CARD.CURRENT_CODE <> '0' AND CRD_CARD.OPPOST_DATE < ?)) "
				+ "AND CRD_CARD.ID_P_SEQNO = CRD_IDNO.ID_P_SEQNO " + "AND CRD_IDNO.ID_NO = ? ";

		String month = commDate.monthAdd(searchDate, -6);
		setString(1, month);
		setString(2, wealthvipNew.idNo);
		int recCnt = selectTable();

		// COUNT(*) > 0則全新戶註記為N，否則為Y
		if (getValueInt("new_account_cnt") > 0) {
			wealthvipNew.newAccountFlag = "N";
		} else {
			wealthvipNew.newAccountFlag = "Y";
		}
	}

	private int header(StringBuffer sb, WealthvipNew wealthvipNew) throws Exception {
		String branch = wealthvipNew.procBranch;
		String fullChiName = wealthvipNew.fullChiName;
		String str = "";
		String temp = "";

		str = commCrd.fixLeft(branch, 10) + commCrd.fixLeft("CRD116B", 16)
				+ commCrd.fixLeft(searchDateTw + "財管白金貴賓戶申辦VISA金鑽無限卡推卡明細表", 88) + commCrd.fixLeft("N", 8);
		sb.append(str).append(lineSeparator);
		sb.append(commCrd.fixLeft(commCrd.fixRight("合作金庫商業銀行", 70), 132)).append(lineSeparator);
		str = commCrd.fixLeft("分行代號: 3144  信用卡部", 54) + commCrd.fixLeft("財管白金貴賓戶申辦VISA金鑽無限卡推卡明細表", 62)
				+ commCrd.fixLeft("保存年限: 二年", 16);
		sb.append(str).append(lineSeparator);
		String strDate = "中華民國 " + searchDateTwYear + " 年 " + searchDateMonth + " 月 " + searchDateDay + " 日";
		temp = String.format("%04d", pageCnt1);
		str = "報表代號: " + commCrd.fixLeft("CRD116B", 15) + commCrd.fixLeft("科目代號: ", 29) + commCrd.fixLeft(strDate, 56)
				+ commCrd.fixRight("第" + temp + "頁", 18);
		sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
		sb.append(commCrd.fixLeft("分行代號: " + branch + "  " + fullChiName, 132)).append(lineSeparator);
		sb.append(
				"====================================================================================================================================")
				.append(lineSeparator);
		str = " 等級  姓名        身份證       金鑽卡  持有其   開戶日     開卡   有效卡  全新戶  最後消費日  行動電話      住家電話        員工";
		sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
		str = "                                申辦    他卡                註記   註記     註記                                             註記";
		sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
		sb.append(
				"====================================================================================================================================")
				.append(lineSeparator);

		return 8;
	}

	private int tail(StringBuffer sb, int lineCnt, int gdcCnt) throws Exception {
		int line = 0;
		String str = "";
		if (lineCnt == 0) {
			sb.append(lineSeparator);
			str = "查無資料";
			sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
			sb.append(lineSeparator);
			line += 3;
		}
		sb.append(
				"====================================================================================================================================")
				.append(lineSeparator);
		sb.append(lineSeparator);
//        str = "    備註: （１）狀態為『重設密碼』者，請併客戶填具之『全球金融網（ＥＯＩ）最高權限管理者重設密碼申請書』一併保存。";
//        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
//        str = "          （２）若無交易資料亦須列印歸檔。";
//        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
//        sb.append(lineSeparator);
//        str = "製表單位: 資訊部                                                  經辦:                          核章: ";
//        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
		if (lineCnt > 0) {
			str = "合計:      " + lineCnt + "  筆      已申辦卡片: " + gdcCnt + "筆";
			sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
			sb.append(lineSeparator);
			line += 1;
		}
		line += 1;
		return line;
	}

	private int totalTail(StringBuffer sb) throws Exception {
		int line = 0;
		String str = "";
//        str = "    備註: （１）狀態為『重設密碼』者，請併客戶填具之『全球金融網（ＥＯＩ）最高權限管理者重設密碼申請書』一併保存。";
//        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
//        str = "          （２）若無交易資料亦須列印歸檔。";
//        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
//        sb.append(lineSeparator);
//        str = "製表單位: 資訊部                                                  經辦:                          核章: ";
//        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
		sb.append(lineSeparator);
		str = "統計:     " + totalCnt + "  筆      已申辦卡片: " + gdcTotalCnt + "筆";
		sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
		line += 2;
		return line;
	}

	/**
	 * 
	 * 產生檔案
	 * 
	 * @return String
	 * @throws Exception
	 */
	private String getRowOfDAT(WealthvipNew wealthvipNew) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(commCrd.fixLeft("", 2)); // 空白
		sb.append(commCrd.fixLeft(wealthvipNew.vipLevel, 5)); // 等級 (2)讀取到的VIP_LEVEL
		sb.append(commCrd.fixLeft(wealthvipNew.cusName, 12)); // 姓名 (2)讀取到的CUS_NAM
		sb.append(commCrd.fixLeft(wealthvipNew.idNo, 15)); // 身份證 (2)讀取到的ID_NO
		sb.append(commCrd.fixLeft("Y".equals(wealthvipNew.gdcApplyFlag) ? "Y" : "", 9)); // 金鑽卡申辦
		sb.append(commCrd.fixLeft("Y".equals(wealthvipNew.otherCardFlag) ? "Y" : "", 6)); // 持有其他卡
		sb.append(commCrd.fixLeft("Y".equals(wealthvipNew.gdcApplyFlag) ? wealthvipNew.oriIssueDate : "", 12)); // 開戶日
		String str = "";
		if ("Y".equals(wealthvipNew.gdcApplyFlag)) {
			if ("2".equals(wealthvipNew.activateFlag)) {
				str = "Y";
			}
		}
		sb.append(commCrd.fixLeft(str, 8)); // 開卡註記
		sb.append(commCrd.fixLeft(
				"Y".equals(wealthvipNew.gdcApplyFlag) && "Y".equals(wealthvipNew.activeCardFlag) ? "Y" : "", 8)); // 有效卡註記
		sb.append(commCrd.fixLeft("Y".equals(wealthvipNew.newAccountFlag) ? wealthvipNew.newAccountFlag : "", 7)); // 全新戶註記
		sb.append(commCrd.fixLeft("Y".equals(wealthvipNew.gdcApplyFlag) ? wealthvipNew.lastConsumeDate : "", 11)); // 最後消費日
		sb.append(commCrd.fixLeft(wealthvipNew.mobile, 14)); // 行動電話
		sb.append(commCrd.fixLeft(wealthvipNew.phone, 18)); // 住家電話
		sb.append(commCrd.fixLeft("Y".equals(wealthvipNew.userFlag) ? "Y" : "", 10)); // 員工註記
		sb.append(lineSeparator);

		return sb.toString();
	}

	void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "BREPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;
//        commFTP.hEriaLocalDir = String.format("%s/media/crm", comc.getECSHOME());
//        commFTP.hEriaRemoteDir = "crdatacrea/NEWCENTER";

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("BREPORT", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
//            commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
//            commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	/****************************************************************************/
	private void backup(String removeFileName) throws Exception {
		String tmpstr1 = CRD_FOLDER + "/" + removeFileName;
		String backupFilename = String.format(DATA_FORM + "_%s.1.TXT", searchDate);
		String tmpstr2 = CRD_FOLDER + "/backup/" + backupFilename;

		if (commCrd.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	class WealthvipNew {
		String idNo = "";
		String vipLevel = "";
		String avgWth = "";
		String procId = "";
		String procBranch = "";
		String branchName = "";
		String phone = "";
		String mobile = "";
		String email = "";
		String cusName = "";
		String mailAddress = "";
		// extends
		String fullChiName = "";
		String cardNo = "";
		String oriIssueDate = "";
		String activateFlag = "";
		String lastConsumeDate = "";
		String gdcApplyFlag = ""; // 金鑽卡申辦註記
		String otherCardFlag = ""; // 持有其他卡
		String activeCardFlag = ""; // 有效卡註記
		String newAccountFlag = ""; // 全新戶註記
		String userFlag = ""; // 員工註記
	}

	public static void main(String[] args) {
		CrdR116B2 proc = new CrdR116B2();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}

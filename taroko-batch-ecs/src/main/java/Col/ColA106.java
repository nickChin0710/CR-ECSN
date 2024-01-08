/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/04/07  V1.00.00    Rou        program initial                          *
*  109/12/07  V1.00.01    shiyuqi       updated for project coding standard   *
*  110/09/22  V1.00.02    Sunny      增加寫入錯誤報表ptr_batch_rpt                *
*  112/05/29  V1.00.03    Sunny      調整受理申請資料重複送判斷的條件                                *
*  112/06/02  V1.00.04    Sunny      調整轉入全檔參數NOCHECK條件判斷                               *
*  112/06/11  V1.00.05    Sunny      調整轉入順序，如為4/5/6視為同一層級，依日期依序轉入  *
*  112/06/20  V1.00.06    Sunny      調整轉入順序，依日期再依狀態排序轉入                         *
*  112/06/21  V1.00.07    Sunny      增加NOCHECK判斷，減少查核的條件                              *
******************************************************************************/

package Col;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;

import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA106 extends AccessDAO {
	private String progname = "OA前置協商主檔轉入處理程式   112/06/20  V1.00.06";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	String hCallBatchSeqno = "";
	String hBusiBusinessDate = "";

	String noCheckFlag="";
	String hclntfiledate = "";
	String hClntId = "";
	String hClntIdPSeqno = "";
	String hClntLiacStatus = "";
	String hClntLiacTxnCode = "";
	String hClntNotifyDate= "";
	String hClntApplyDate = "";
	String hClntDelay1Month = "";
	String hClntDelay2Month = "";
	String hClntCreditCardFlag = "";
	String hClntRowid = "";
	String hClnoLiacSeqno = "";
	String hClnoApplyDate = "";
	String hClnoCreditCardFlag = "";
	String hClnoLiacStatus = "";
	String hClnoRowid = "";
	String hClntProcFlag = "";
	String hClnoAcctStatusApply = "";
	String hClnoMCodeApply = "";
	String hAcctStatusFlag = "";

	String tmpstr = "";
	int errCnt = 0;
	int insertCnt = 0;
	int updateCnt = 0;
	long totalCnt = 0;
	long totalIdCnt = 0;

	/* error report */
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	String rptName1 = "ColA106R1";
	String rptDesc1 = "前置債協入主檔處理結果報表";
	String buf = "";
	String szTmp = "";
	String errStr = "";
	int rptSeq1 = 0;
	int pageCnt = 0;
	int lineCnt = 0;
	int warnFlag = 0;

	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (comm.isAppActive(javaProgram)) {
				comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
			}

			// 檢查參數
			if (args.length != 0 && args.length != 1 && args.length != 2) {
				comc.errExit("Usage : ColA106 [file_date]", "");
			}

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			// =====================================
			/*
			hclntfiledate = "";
            if ((args.length >= 1) && (args[0].length() == 8)) {
                String sGArgs0 = "";
                sGArgs0 = args[0];
                sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                hclntfiledate = sGArgs0;
            }
            
         // 若第二個參數為「NOCHECK」，就表示不檢核資料，直接轉入
			if (args.length > 1) {
				String sGArgs1 = "";
                sGArgs1 = args[1];
                noCheckFlag = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);;
				System.out.println(String.format("有開啟第二個參數[%s]，若為「NOCHECK」則表示不檢核資料順序直接轉入", noCheckFlag));
				System.out.println("");
			}
			*/
			
			// get searchDate
			String paramet2 = "";
			
			//若第二個參數為「NOCHECK(大寫)」，則代表不檢核資料，直接轉入。
			String parm1 = (args.length>=1 && args[0].length()==8) ? args[0].trim() 
					: (args.length>=1 && "NOCHECK".equals(args[0])?args[0].trim() :"");
			showLogMessage("I", "", String.format("程式參數1 = [%s]", parm1));

			String parm2 = (args.length==2 && args[1].length()==8) ? args[1].trim() 
					: (args.length==2 && "NOCHECK".equals(args[1])?args[1].trim() :"");
			showLogMessage("I", "", String.format("程式參數2 = [%s]", parm2));

			if(parm1.equals("NOCHECK")) {
				hclntfiledate = parm2;
				paramet2 = parm1;
			}else {
				hclntfiledate = parm1;
				paramet2 = parm2;
			}
			
			//判斷是否不檢核資料
			noCheckFlag = paramet2;
//			hclntfiledate = getProgDate(hclntfiledate, "D");

			selectPtrBusinday();
			if (hclntfiledate.length() == 0)
				hclntfiledate = hBusiBusinessDate;
			
			
			showLogMessage("I", "", String.format("執行日期 [%s]", hclntfiledate));

			// if(errCnt>0)
			printHeader(); // 寫出錯誤報表檔頭
			selectColLiacNegoT(); // 依ID distinct 進行查詢，計算ID數。
		//	selectColLiacNegoT1(); // 再依每一個ID的細節資料(可能多筆不同的狀態)，進行處理。
			printTailer(); // 寫出錯誤報表檔尾

			comcr.insertPtrBatchRpt(lpar1); /* 寫入ptr_batch_rpt online報表 */

			showLogMessage("I", "", String.format("Total process record[%d] ID cnt[%d]", totalCnt, totalIdCnt));

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";
		sqlCmd = "select business_date ";
		sqlCmd += " from ptr_businday ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
		}

	}

	/***********************************************************************/
	void selectColLiacNegoT() throws Exception {

		sqlCmd = "select ";
		sqlCmd += "distinct id_no, ";
		sqlCmd += "id_p_seqno ";
		sqlCmd += "from col_liac_nego_t ";
		sqlCmd += "where file_date = ? ";	    
		//sqlCmd += "and id_no = 'A101290386' "; //sunny test
		//sqlCmd += "order by liac_status, liac_txn_code,apply_date ";
		//sqlCmd += "order by file_date,liac_status,liac_txn_code,apply_date desc";
		setString(1, hclntfiledate);

		openCursor();
		while (fetchTable()) {
			hClntId = getValue("id_no");
			hClntIdPSeqno = getValue("id_p_seqno");

			showLogMessage("I", "", String.format("Process ID = [%s] , id_p_seqno = [%s]", hClntId, hClntIdPSeqno));

			totalIdCnt++;
			selectColLiacNegoT1();
		}
		closeCursor();
	}

	/***********************************************************************/
	void selectColLiacNegoT1() throws Exception {
		int int1a = 0;

		sqlCmd = "select ";
		sqlCmd += "liac_status,";
		sqlCmd += "liac_txn_code,";
		sqlCmd += "id_no,"; // sunny add
		sqlCmd += "notify_date,";
		sqlCmd += "apply_date,";
		sqlCmd += "delay_1_month,";
		sqlCmd += "delay_2_month,";
		sqlCmd += "credit_card_flag,";
		sqlCmd += "rowid as rowid ";
		sqlCmd += "from col_liac_nego_t ";
		sqlCmd += "where file_date = ? ";
		sqlCmd += "and id_p_seqno = ? ";
		sqlCmd += " order by notify_date,decode(liac_status,'4','4','5','4','6','4',liac_status), liac_txn_code";
		//sqlCmd += " order by file_date,decode(liac_status,'4','4','5','4','6','4',liac_status), liac_txn_code,notify_date";
		//sqlCmd += " order by file_date,liac_status, liac_txn_code,apply_date";
		setString(1, hclntfiledate);
		setString(2, hClntIdPSeqno);

		extendField = "col_liac_nego_t_1.";

		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hClntLiacStatus = getValue("col_liac_nego_t_1.liac_status", i);
			hClntLiacTxnCode = getValue("col_liac_nego_t_1.liac_txn_code", i);
			hClntId = getValue("col_liac_nego_t_1.id_no", i); // sunny add
			hClntNotifyDate = getValue("col_liac_nego_t_1.notify_date", i);
			hClntApplyDate = getValue("col_liac_nego_t_1.apply_date", i);
			hClntDelay1Month = getValue("col_liac_nego_t_1.delay_1_month", i);
			hClntDelay2Month = getValue("col_liac_nego_t_1.delay_2_month", i);
			hClntCreditCardFlag = getValue("col_liac_nego_t_1.credit_card_flag", i);
			hClntRowid = getValue("col_liac_nego_t_1.rowid", i);
			hClntProcFlag = "Y";

			totalCnt++;
			if ((totalCnt % 1000) == 0) {
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
			}

			/*
			 * 判斷col_liac_nego主檔， int1a為0，表ID有存在資料，判斷狀態決定是否UPDATE int1a為1，表ID未存在資料，則新增資料
			 */
			int1a = selectColLiacNego();/* 1:主檔無ID資料 */

//			showLogMessage("I", "",
//					String.format("處理ID[%s] 暫存檔狀態[%s] 主檔狀態[%s]", hClntId, hClntLiacStatus, hClnoLiacStatus));

			/* 查詢col_liac_nego主檔此ID無資料，直接insert */
			
			//(hClntLiacStatus.equals("1")) &&

			if (int1a != 0 && (noCheckFlag.equals("NOCHECK"))) {
				
//				showLogMessage("I", "",
//						String.format("此ID前協主檔無資料，直接INSERT，處理ID[%s] 暫存檔狀態[%s] 主檔狀態[%s]", hClntId, hClntLiacStatus, hClnoLiacStatus));

				errStr = "新增成功，前協主檔無資料－NOCHECK處理模式";
				showLogMessage("I", "", String.format("處理完成 = ID[%s] 暫存檔狀態[%s] 主檔狀態[%s] 狀態日期[%s] [%s]", hClntId, hClntLiacStatus ,hClnoLiacStatus,hClntNotifyDate,errStr));
								
				insertColLiacNego();
				insertColLiacRemod();
				if (hClntCreditCardFlag.equals("Y"))
					insertColLiacDebt();
				insertColLiacNegoHst();
				deleteColLiacNegoT();
				/* sunny check Result */
				printSucess(); // 寫入報表明細(成功)

				continue;
			}

//			/* 查詢col_liac_nego主檔此ID有資料，確認狀態 */
//			if (int1a == 0) {
//				showLogMessage("I", "", String.format("col_liac_nego 已存在資料，flag=[%s]", int1a));
//
//				// 比字串大小，如果hClntLiacStatus的值小於hClnoLiacStatus的值，就insert data
//				if (hClntLiacStatus.compareTo(hClnoLiacStatus) < 0) {
//					updateColLiacNego();
//					insertColLiacRemod();
//					insertColLiacNegoHst();
//				} else {
////					showLogMessage("I", "", String.format("處理ID[%s] Clnt-Status[%s] Clno-Status[%s] 檔案狀態有誤", hClntId,
////							hClntLiacStatus, hClnoLiacStatus));
//					errStr = "檔案狀態有誤";
//					showLogMessage("I", "", String.format("處理ID[%s] Clnt-Status[%s] Clno-Status[%s] [%s]", hClntId,hClntLiacStatus, hClnoLiacStatus,errStr));
//					errCnt++;
//					printDetail(); // 寫入錯誤報表明細
//				}
//			}

			// 舊的程式
//            if ((int1a == 0) && (!hClntApplyDate.equals(hClnoApplyDate)) && (!hClntLiacStatus.equals("1"))) {
//                showLogMessage("I", "", String.format("ERROR 0:ID[%s][%s]->[%s] 受理申請日期資料不一致", hClntId, hClnoApplyDate,
//                        hClntApplyDate));
//                errCnt++;
//                deleteColLiacNegoT();
//                continue;
//            }
            
			if ((hClntLiacTxnCode.equals("A")) && (hClntLiacStatus.toCharArray()[0] < '7')) {
				if (int1a == 1 && (!noCheckFlag.equals("NOCHECK"))) {
					showLogMessage("I", "", String.format("col_liac_nego 主檔無資料，flag=[%s]", int1a));
					if (!hClntLiacStatus.equals("1")) {
						// showLogMessage("I", "", String.format("ERROR A:ID[%s] 無受理申請資料", hClntId));
						errStr = "無受理申請資料";
						showLogMessage("I", "", String.format("ERROR A:ID[%s] [%s]", hClntId, errStr));
						errCnt++;
						printDetail(); // 寫入錯誤報表明細
						deleteColLiacNegoT();
						continue;
					}
					insertColLiacNego();
					insertColLiacRemod();
					if (hClntCreditCardFlag.equals("Y"))
						insertColLiacDebt();
					insertColLiacNegoHst();
					deleteColLiacNegoT();

					/* sunny check Result */
					printSucess(); // 寫入報表明細(成功)

					continue;
				}
                  //hClntApplyDate.equals(hClnoApplyDate)
				if ((hClntLiacStatus.equals("1") && hClnoLiacStatus.equals("1") && hClntApplyDate.compareTo(hClnoApplyDate) <= 0)) {
					// showLogMessage("I", "", String.format("ERROR 1-1:ID[%s] 受理申請資料重複送",
					// hClntId));
					errStr = "受理申請資料重複送";
					showLogMessage("I", "", String.format("ERROR 1-1:ID[%s] T-Status[%s] T-applyDate[%s] M-Status[%s] M-ApplyDate[%s] ErrDesc[%s]",
							hClntId,hClntLiacStatus,hClntApplyDate,hClnoLiacStatus,hClnoApplyDate,errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("1")) && (hClnoLiacStatus.equals("2"))) {
					// showLogMessage("I", "", String.format("ERROR 1-2:ID[%s] 已停催又收到受理申請資料",
					// hClntId));
					errStr = "受理申請資料重複送";
					showLogMessage("I", "", String.format("ERROR 1-2:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("1")) && (hClnoLiacStatus.equals("3")) && (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 1-3:ID[%s] 協商成功又收到受理申請資料",
					// hClntId));
					errStr = "協商成功又收到受理申請資料";
					showLogMessage("I", "", String.format("ERROR 1-3:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("1")) && (hClnoLiacStatus.equals("4"))&& (!noCheckFlag.equals("NOCHECK"))
						&& (!hClntApplyDate.equals(hClnoApplyDate))) {
					deleteColLiacNego1();
					insertColLiacNego();
					insertColLiacRemod();
					if (hClntCreditCardFlag.equals("Y"))
						insertColLiacDebt();
					insertColLiacNegoHst();
					deleteColLiacNegoT();

					/* sunny check Result */
					printSucess(); // 寫入報表明細(成功)

					continue;
				}
				if ((hClntLiacStatus.equals("1")) && (hClnoLiacStatus.equals("5"))&& (!noCheckFlag.equals("NOCHECK"))
						&& (!hClntApplyDate.equals(hClnoApplyDate))) {
					deleteColLiacNego1();
					insertColLiacNego();
					insertColLiacRemod();
					if (hClntCreditCardFlag.equals("Y"))
						insertColLiacDebt();
					insertColLiacNegoHst();
					deleteColLiacNegoT();

					/* sunny test */
					errStr = "成功";
					showLogMessage("I", "", String.format("處理完成 = ID[%s] [%s]", hClntId, errStr));
					printDetail();

					continue;
				}
				/*
				if ((hClntLiacStatus.equals("1")) && (hClnoLiacStatus.equals("6") && (!noCheckFlag.equals("NOCHECK")))) {
					// showLogMessage("I", "", String.format("ERROR 1-6:ID[%s] 還清結案卻收到受理申請資料",
					// hClntId));
					errStr = "還清結案卻收到受理申請資料";
					showLogMessage("I", "", String.format("ERROR 1-6:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				*/
				//hClntNotifyDate
				if ((hClntLiacStatus.equals("2")) && (hClnoLiacStatus.equals("2"))&& (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 2-2:ID[%s] 停催資料重複送", hClntId));
					errStr = "停催資料重複送";
					showLogMessage("I", "", String.format("ERROR 2-2:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("2")) && (hClnoLiacStatus.equals("3"))&& (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 2-3:ID[%s] 協商成功又收到停催資料",
					// hClntId));
					errStr = "協商成功又收到停催資料";
					showLogMessage("I", "", String.format("ERROR 2-3:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("2")) && (hClnoLiacStatus.equals("4"))&& (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 2-4:ID[%s] 復催又收到停催資料",
					// hClntId));
					errStr = "復催又收到停催資料";
					showLogMessage("I", "", String.format("ERROR 2-4:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("2")) && (hClnoLiacStatus.equals("5")) && (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 2-5:ID[%s] 毀諾又收到停催資料",
					// hClntId));
					errStr = "毀諾又收到停催資料";
					showLogMessage("I", "", String.format("ERROR 2-5:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("2")) && (hClnoLiacStatus.equals("6")) && (!noCheckFlag.equals("NOCHECK"))) {
					errStr = "還清又收到停催資料";
					showLogMessage("I", "", String.format("ERROR 2-6:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("3")) && (hClnoLiacStatus.equals("1")) && (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 3-1:ID[%s] 尚未停催卻收到協商成功資料",
					// hClntId));
					errStr = "尚未停催卻收到協商成功資料";
					showLogMessage("I", "", String.format("ERROR 3-1:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("3")) && (hClnoLiacStatus.equals("3")) && (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 3-3:ID[%s] 協商成功資料重複送",
					// hClntId));
					errStr = "協商成功資料重複送";
					showLogMessage("I", "", String.format("ERROR 3-3:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("3")) && (hClnoLiacStatus.equals("4"))&& (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 3-4:ID[%s] 復催結案卻收到協商成功資料",
					// hClntId));
					errStr = "復催結案卻收到協商成功資料";
					showLogMessage("I", "", String.format("ERROR 3-4:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("3")) && (hClnoLiacStatus.equals("5"))&& (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 3-5:ID[%s] 毀諾結案卻收到協商成功資料",
					// hClntId));
					errStr = "復催結案卻收到協商成功資料";
					showLogMessage("I", "", String.format("ERROR 3-5:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("3")) && (hClnoLiacStatus.equals("6"))&& (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 3-6:ID[%s] 還清結案卻收到協商成功資料",
					// hClntId));
					errStr = "還清結案卻收到協商成功資料";
					showLogMessage("I", "", String.format("ERROR 3-6:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("4")) && (hClnoLiacStatus.equals("4"))&& (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 4-4:ID[%s] 復催資料重複送", hClntId));
					errStr = "復催資料重複送";
					showLogMessage("I", "", String.format("ERROR 4-4:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("5")) && (hClnoLiacStatus.equals("5"))&& (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 5-5:ID[%s] 毀諾資料重複送", hClntId));
					errStr = "毀諾資料重複送";
					showLogMessage("I", "", String.format("ERROR 5-5:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}
				if ((hClntLiacStatus.equals("6")) && (hClnoLiacStatus.equals("6"))&& (!noCheckFlag.equals("NOCHECK"))) {
					// showLogMessage("I", "", String.format("ERROR 6-6:ID[%s] 還清資料重複送", hClntId));
					errStr = "還清資料重複送";
					showLogMessage("I", "", String.format("ERROR 6-6:ID[%s] [%s]", hClntId, errStr));
					errCnt++;
					printDetail(); // 寫入錯誤報表明細
					deleteColLiacNegoT();
					continue;
				}

				// 且通過檢核後，進行updateColLiacNego()，狀態不為6還清結案者，才會新增insertColLiacRemod()
				updateColLiacNego();
				if (!hClntLiacStatus.equals("6")) {
					insertColLiacRemod();
				}
			}
//            if ((hClntLiacTxnCode.equals("C")) && (hClntLiacStatus.toCharArray()[0] < '6')) {
//                if (int1a == 1) {
//                    if (!hClntLiacStatus.equals("1")) {
//                        showLogMessage("I", "", String.format("ERROR A:ID[%s] 無受理申請資料", hClntId));
//                        errCnt++;
//                        deleteColLiacNegoT();
//                        continue;
//                    }
//                    insertColLiacNego();
//                    insertColLiacRemod();
//                    if (hClntCreditCardFlag.equals("Y"))
//                        insertColLiacDebt();
//                    insertColLiacNegoHst();
//                    deleteColLiacNegoT();
//                    continue;
//                }
//
//                if ((hClntLiacStatus.equals("1"))
//                		&& (hClnoLiacStatus.equals("4"))
//                        && (!hClntApplyDate.equals(hClnoApplyDate))) {
//                    deleteColLiacNego1();
//                    insertColLiacNego();
//                    insertColLiacRemod();
//                    if (hClntCreditCardFlag.equals("Y"))
//                        insertColLiacDebt();
//                    insertColLiacNegoHst();
//                    deleteColLiacNegoT();
//                    continue;
//                }
//
//                if ((hClntLiacStatus.toCharArray()[0] < hClnoLiacStatus.toCharArray()[0])) {
//                    showLogMessage("I", "", String.format("WARNING A:ID[%s] [%s]==>[%s] 舊狀態送異動資料", hClntId, hClntLiacStatus,
//                            hClnoLiacStatus));
//                    hClntProcFlag = "X";
//                } else {
//                	updateColLiacNego();
//                    if (!hClntLiacStatus.equals("6")) {
//                    	if ((!hClntLiacStatus.equals(hClnoLiacStatus)))
//                            insertColLiacRemod();
//                    }
//                }
//            }

			if (int1a == 1) {
				showLogMessage("I", "", String.format("WARNING X:ID[%s] status[%s] 主檔已無資料", hClntId, hClntLiacStatus));
				deleteColLiacNegoT();
				continue;
			}

			insertColLiacNegoHst();
			deleteColLiacNegoT();

			/* sunny check Result */
			printUpdateSucess(); // 寫入報表明細(更新成功)
		}
	}

	/***********************************************************************/
	/*
	 * 查詢col_liac_nego主檔此ID是否已存在資料 int1a為0，表已存在資料 int1a為1，表未存在資料
	 */
	int selectColLiacNego() throws Exception {
		hClnoLiacSeqno = "";
		hClnoApplyDate = "";
		hClnoCreditCardFlag = "";
		hClnoLiacStatus = "";
		hClnoRowid = "";
		hClnoAcctStatusApply = "";
		hClnoMCodeApply = "";

		sqlCmd = "select liac_seqno,";
		sqlCmd += "apply_date,";
		sqlCmd += "credit_card_flag,";
		sqlCmd += "liac_status,";
		sqlCmd += "rowid as rowid ";
		sqlCmd += " from col_liac_nego  ";
		sqlCmd += "where id_p_seqno = ? ";
		setString(1, hClntIdPSeqno);

		extendField = "col_liac_nego.";

		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hClnoLiacSeqno = getValue("col_liac_nego.liac_seqno");
			hClnoApplyDate = getValue("col_liac_nego.apply_date");
			hClnoCreditCardFlag = getValue("col_liac_nego.credit_card_flag");
			hClnoLiacStatus = getValue("col_liac_nego.liac_status");
			hClnoRowid = getValue("col_liac_nego.rowid");
		} else {
			return 1; /* 未存在資料 */
		}
		return 0; /* 已存在資料 */
	}

	/***********************************************************************/
	void deleteColLiacNego1() throws Exception {
		daoTable = "col_liac_nego";
		whereStr = "where rowid = ? ";
		setRowId(1, hClnoRowid);
		deleteTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("delete_col_liac_nego not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void insertColLiacNego() throws Exception {
		tmpstr = String.format("%010.0f", comcr.getCOLSeq());
		hClnoLiacSeqno = tmpstr;

		if (hClntLiacStatus.equals("1")) {
			fGetAcno();
		}

		sqlCmd = "insert into col_liac_nego ";
		sqlCmd += "(liac_seqno,";
		sqlCmd += "file_date,";
		sqlCmd += "liac_status,";
		sqlCmd += "query_date,";
		sqlCmd += "notify_date,";
		sqlCmd += "id_p_seqno,";
		sqlCmd += "id_no,";
		sqlCmd += "chi_name,";
		sqlCmd += "bank_code,";
		sqlCmd += "bank_name,";
		sqlCmd += "acct_status_apply,";
		sqlCmd += "m_code_apply,";
		sqlCmd += "apply_date,";
		sqlCmd += "nego_s_date,";
		sqlCmd += "stop_notify_date,";
		sqlCmd += "interest_base_date,";
		sqlCmd += "recol_reason,";
		sqlCmd += "credit_flag,";
		sqlCmd += "no_credit_flag,";
		sqlCmd += "cash_card_flag,";
		sqlCmd += "credit_card_flag,";
		sqlCmd += "contract_date,";
		sqlCmd += "liac_remark,";
		sqlCmd += "end_date,";
		sqlCmd += "end_reason,";
		sqlCmd += "liac_txn_code,";
		sqlCmd += "reg_bank_no,";
		sqlCmd += "id_data_date,";
		sqlCmd += "court_agree_date,";
		sqlCmd += "case_status,";
		sqlCmd += "crt_date,";
		sqlCmd += "crt_time,";
		sqlCmd += "proc_flag,";
		sqlCmd += "proc_date,";
		sqlCmd += "mod_time,";
		sqlCmd += "mod_pgm)";
		sqlCmd += " select ";
		sqlCmd += "?,";
		sqlCmd += "file_date,";
		sqlCmd += "liac_status,";
		sqlCmd += "query_date,";
		sqlCmd += "notify_date,";
		sqlCmd += "id_p_seqno,";
		sqlCmd += "id_no,";
		sqlCmd += "chi_name,";
		sqlCmd += "bank_code,";
		sqlCmd += "bank_name,";
		sqlCmd += "?,";
		sqlCmd += "?,";
		sqlCmd += "apply_date,";
		sqlCmd += "nego_s_date,";
		sqlCmd += "stop_notify_date,";
		sqlCmd += "interest_base_date,";
		sqlCmd += "recol_reason,";
		sqlCmd += "credit_flag,";
		sqlCmd += "no_credit_flag,";
		sqlCmd += "cash_card_flag,";
		sqlCmd += "credit_card_flag,";
		sqlCmd += "contract_date,";
		sqlCmd += "liac_remark,";
		sqlCmd += "end_date,";
		sqlCmd += "end_reason,";
		sqlCmd += "decode(liac_txn_code,'A','A','C','C','A'),";
		sqlCmd += "reg_bank_no,";
		sqlCmd += "id_data_date,";
		sqlCmd += "court_agree_date,";
		sqlCmd += "case_status,";
		sqlCmd += "crt_date,";
		sqlCmd += "crt_time,";
		sqlCmd += "proc_flag,";
		sqlCmd += "proc_date,";
		sqlCmd += "mod_time,";
		sqlCmd += "mod_pgm ";
		sqlCmd += "from col_liac_nego_t where rowid = ? ";
		setString(1, hClnoLiacSeqno);
		setString(2, hClnoAcctStatusApply);
		setString(3, hClnoMCodeApply);
		setRowId(4, hClntRowid);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_" + daoTable + "duplicate!", "", hCallBatchSeqno);
		} else
			showLogMessage("I", "", "insert col_liac_nego success !");

	}

	/***********************************************************************/

	void insertColLiacDebt() throws Exception {
//    	c.	insert_col_liac_debt()中，增加【AUTH_UNCAP_AMT(已授權未請款金額)】欄位，並設定預設值為0。
//    	d.	insert_col_liac_debt()中，增加【ACCT_STATUS_FLAG (帳戶狀態是否有呆帳戶)】欄位。欄位值邏輯，以id_p_seqno查詢 act_acno, act_acct。取得act_acno.acct_status 是否為4-呆帳。
		selectAcctStatusFlag();

		sqlCmd = "insert into col_liac_debt ";
		sqlCmd += "(liac_seqno,";
		sqlCmd += "file_date,";
		sqlCmd += "id_p_seqno,";
		sqlCmd += "id_no,";
		sqlCmd += "notify_date,";
		sqlCmd += "chi_name,";
		sqlCmd += "acct_status_flag,"; // add
		sqlCmd += "bank_code,";
		sqlCmd += "bank_name,";
		sqlCmd += "apply_date,";
		sqlCmd += "interest_base_date,";
		sqlCmd += "in_end_bal,";
		sqlCmd += "out_end_bal,";
		sqlCmd += "lastest_pay_amt,";
		sqlCmd += "auth_uncap_amt,"; // add
		sqlCmd += "in_end_bal_new,";
		sqlCmd += "out_end_bal_new,";
		sqlCmd += "lastest_pay_amt_new,";
		sqlCmd += "from_type,";
		sqlCmd += "create_date,";
		sqlCmd += "create_time,";
		sqlCmd += "proc_flag,";
		sqlCmd += "proc_date,";
		sqlCmd += "mod_time,";
		sqlCmd += "mod_pgm)";
		sqlCmd += " select ";
		sqlCmd += "?,";
		sqlCmd += "file_date,";
		sqlCmd += "id_p_seqno,";
		sqlCmd += "id_no,";
		sqlCmd += "notify_date,";
		sqlCmd += "chi_name,";
		sqlCmd += "?,"; // add acct_status_flag
		sqlCmd += "bank_code,";
		sqlCmd += "bank_name,";
		sqlCmd += "apply_date,";
		sqlCmd += "interest_base_date,";
		sqlCmd += "0,";
		sqlCmd += "0,";
		sqlCmd += "0,";
		sqlCmd += "0,"; // add auth_uncap_amt
		sqlCmd += "0,";
		sqlCmd += "0,";
		sqlCmd += "0,";
		sqlCmd += "'2',";
		sqlCmd += "to_char(sysdate,'yyyymmdd'),";
		sqlCmd += "to_char(sysdate,'hh24miss'),";
		sqlCmd += "'0',";
		sqlCmd += "to_char(sysdate,'yyyymmdd'),";
		sqlCmd += "sysdate,";
		sqlCmd += "? ";
		sqlCmd += "from col_liac_nego_t where rowid =? ";
		setString(1, hClnoLiacSeqno);
		setString(2, hAcctStatusFlag);
		setString(3, javaProgram);
		setRowId(4, hClntRowid);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
		} else
			showLogMessage("I", "", "insert col_liac_debt success !");
	}

	/***********************************************************************/
	void selectAcctStatusFlag() throws Exception {
		int hCnt = 0;
		hAcctStatusFlag = "";

		sqlCmd = "select count(a.acno_p_seqno) hcnt from act_acno a, act_acct c ";
		sqlCmd += "where a.acno_p_seqno = c.p_seqno ";
		sqlCmd += "and a.id_p_seqno = ? ";
		sqlCmd += "and a.acct_status='4' and c.acct_jrnl_bal > 0 ";
		setString(1, hClntIdPSeqno);

		extendField = "acct_status_flag.";

		selectTable();
		hCnt = getValueInt("acct_status_flag.hcnt");
		if (hCnt > 0) {
			hAcctStatusFlag = "Y";
		} else
			hAcctStatusFlag = "N";
	}

	/***********************************************************************/
	void updateColLiacNego() throws Exception {
		// decode的參數項目不符會導致null,造成存檔失敗
		daoTable = "col_liac_nego a";
		updateSQL = "( file_date,";
		updateSQL += " apply_date,"; //debug
		updateSQL += " liac_status,";
		updateSQL += " query_date,";
		updateSQL += " notify_date,";
		updateSQL += " id_p_seqno,";
		updateSQL += " id_no,";
		updateSQL += " chi_name,";
		updateSQL += " bank_code,";
		updateSQL += " bank_name,";
		updateSQL += " nego_s_date,";
		updateSQL += " stop_notify_date,";
		updateSQL += " interest_base_date,";
		updateSQL += " recol_reason,";
		updateSQL += " credit_flag,";
		updateSQL += " no_credit_flag,";
		updateSQL += " cash_card_flag,";
		updateSQL += " credit_card_flag,";
		updateSQL += " contract_date,";
		updateSQL += " liac_remark,";
		updateSQL += " end_date,";
		updateSQL += " end_reason,";
		updateSQL += " liac_txn_code,";
		updateSQL += " reg_bank_no,";
		updateSQL += " crt_date,";
		updateSQL += " crt_time,";
		updateSQL += " proc_flag,";
		updateSQL += " proc_date,";
		updateSQL += " mod_time,";
		updateSQL += " mod_pgm ) = (select ";
		updateSQL += " file_date,";
		// updateSQL += " apply_date,";
		updateSQL += " decode(cast(? as varchar(1)),'1',apply_date,a.apply_date),";
		updateSQL += " liac_status,";
		updateSQL += " query_date,";
		updateSQL += " notify_date,";
		updateSQL += " id_p_seqno,";
		updateSQL += " id_no,";
		updateSQL += " chi_name,";
		updateSQL += " bank_code,";
		updateSQL += " bank_name,";
		//updateSQL += " decode(cast(? as varchar(1)),'1','','2',nego_s_date,a.nego_s_date),";
		//updateSQL += " decode(cast(? as varchar(1)),'1','','2',stop_notify_date,a.stop_notify_date),";
		updateSQL += " decode(cast(? as varchar(1)),'1','','2',interest_base_date,a.interest_base_date),";
		updateSQL += " decode(cast(? as varchar(1)),'1','','2',stop_notify_date,a.stop_notify_date),";
		updateSQL += " decode(cast(? as varchar(1)),'1',interest_base_date,a.interest_base_date),";
		updateSQL += " decode(cast(? as varchar(1)),'4',recol_reason,'5',recol_reason,'6',recol_reason,''),"; //6 結案/結清也要放值
		updateSQL += " decode(cast(? as varchar(1)),'1',credit_flag,'2',credit_flag,a.credit_flag),";
		updateSQL += " decode(cast(? as varchar(1)),'1',no_credit_flag,'2',no_credit_flag,a.no_credit_flag),";
		updateSQL += " decode(cast(? as varchar(1)),'1',cash_card_flag,'2',cash_card_flag,a.cash_card_flag),";
		updateSQL += " decode(cast(? as varchar(1)),'1',credit_card_flag,'2',credit_card_flag,a.credit_card_flag),";
		updateSQL += " decode(cast(? as varchar(1)),'1','','2','','3',contract_date,'4',a.contract_date,'5',a.contract_date,'6',a.contract_date,''),"; // decode修正
		updateSQL += " liac_remark,";
		updateSQL += " decode(cast(? as varchar(1)),'6',end_date,''),";  //TCB 6.結案結清時才放值
		updateSQL += " decode(cast(? as varchar(1)),'6',end_reason,''),";//TCB 6.結案結清時才放值
		updateSQL += " decode(liac_txn_code,'A','A','C','C','A'),";
		updateSQL += " decode(cast(? as varchar(1)),'1','1','','2',reg_bank_no,a.reg_bank_no,''),"; // decode修正
		updateSQL += " crt_date,";
		updateSQL += " crt_time,";
		updateSQL += " proc_flag,";
		updateSQL += " proc_date,";
		updateSQL += " sysdate,";
		updateSQL += " ? ";
		updateSQL += " from col_liac_nego_t where rowid = ?) ";
		whereStr = " where id_p_seqno = ? and liac_seqno = ? ";
		setString(1, hClntLiacStatus);
		setString(2, hClntLiacStatus);
		setString(3, hClntLiacStatus);
		setString(4, hClntLiacStatus);
		setString(5, hClntLiacStatus);
		setString(6, hClntLiacStatus);
		setString(7, hClntLiacStatus);
		setString(8, hClntLiacStatus);
		setString(9, hClntLiacStatus);
		setString(10, hClntLiacStatus);
		setString(11, hClntLiacStatus);
		setString(12, hClntLiacStatus);
		setString(13, hClntLiacStatus);
		setString(14, javaProgram);
		setRowId(15, hClntRowid);
		setString(16, hClntIdPSeqno);
		setString(17, hClnoLiacSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_col_liac_nego a not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void insertColLiacRemod() throws Exception {
		daoTable = "col_liac_remod";
		extendField = daoTable + ".";
		setValue(extendField + "liac_seqno", hClnoLiacSeqno);
		setValue(extendField + "liac_status", hClntLiacStatus);
		setValue(extendField + "id_p_seqno", hClntIdPSeqno);
		setValue(extendField + "id_no", hClntId);
		setValue(extendField + "proc_flag", "N");
		setValue(extendField + "mod_time", sysDate + sysTime);
		setValue(extendField + "mod_pgm", javaProgram);

		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_col_liac_remod duplicate!", "", hCallBatchSeqno);
		} else
			showLogMessage("I", "", "insert col_liac_remod success !");
	}

	/***********************************************************************/
	void insertColLiacNegoHst() throws Exception {
		sqlCmd = "insert into col_liac_nego_hst ";
		sqlCmd += "(liac_seqno,";
		sqlCmd += "tran_date,";
		sqlCmd += "tran_time,";
		sqlCmd += "file_date,";
		sqlCmd += "liac_status,";
		sqlCmd += "query_date,";
		sqlCmd += "notify_date,";
		sqlCmd += "id_p_seqno,";
		sqlCmd += "id_no,";
		sqlCmd += "chi_name,";
		sqlCmd += "bank_code,";
		sqlCmd += "bank_name,";
		sqlCmd += "acct_status_apply,";
		sqlCmd += "m_code_apply,";
		sqlCmd += "apply_date,";
		sqlCmd += "nego_s_date,";
		sqlCmd += "stop_notify_date,";
		sqlCmd += "interest_base_date,";
		sqlCmd += "recol_reason,";
		sqlCmd += "credit_flag,";
		sqlCmd += "no_credit_flag,";
		sqlCmd += "cash_card_flag,";
		sqlCmd += "credit_card_flag,";
		sqlCmd += "contract_date,";
		sqlCmd += "liac_remark,";
		sqlCmd += "end_date,";
		sqlCmd += "end_reason,";
		sqlCmd += "liac_txn_code,";
		sqlCmd += "reg_bank_no,";
		sqlCmd += "id_data_date,";
		sqlCmd += "court_agree_date,";
		sqlCmd += "case_status,";
		sqlCmd += "delay_agree_date,";
		sqlCmd += "delay_1_month,";
		sqlCmd += "delay_2_month,";
		sqlCmd += "delay_reason,";
		sqlCmd += "delay_desc,";
		sqlCmd += "crt_date,";
		sqlCmd += "crt_time,";
		sqlCmd += "proc_flag,";
		sqlCmd += "proc_date,";
		sqlCmd += "mod_time,";
		sqlCmd += "mod_pgm)";
		sqlCmd += " select ";
		sqlCmd += "?,";
		sqlCmd += "to_char(sysdate,'yyyymmdd'),";
		sqlCmd += "to_char(sysdate,'hh24miss'),";
		sqlCmd += "file_date,";
		sqlCmd += "liac_status,";
		sqlCmd += "query_date,";
		sqlCmd += "notify_date,";
		sqlCmd += "id_p_seqno,";
		sqlCmd += "id_no,";
		sqlCmd += "chi_name,";
		sqlCmd += "bank_code,";
		sqlCmd += "bank_name,";
		sqlCmd += "?,";
		sqlCmd += "?,";
		sqlCmd += "apply_date,";
		sqlCmd += "nego_s_date,";
		sqlCmd += "stop_notify_date,";
		sqlCmd += "interest_base_date,";
		sqlCmd += "recol_reason,";
		sqlCmd += "credit_flag,";
		sqlCmd += "no_credit_flag,";
		sqlCmd += "cash_card_flag,";
		sqlCmd += "credit_card_flag,";
		sqlCmd += "contract_date,";
		sqlCmd += "liac_remark,";
		sqlCmd += "end_date,";
		sqlCmd += "end_reason,";
		sqlCmd += "decode(liac_txn_code,'A','A','C','C','A'),";
		sqlCmd += "reg_bank_no,";
		sqlCmd += "id_data_date,";
		sqlCmd += "court_agree_date,";
		sqlCmd += "case_status,";
		sqlCmd += "delay_agree_date,";
		sqlCmd += "delay_1_month,";
		sqlCmd += "delay_2_month,";
		sqlCmd += "delay_reason,";
		sqlCmd += "delay_desc,";
		sqlCmd += "crt_date,";
		sqlCmd += "crt_time,";
		sqlCmd += "?,";
		sqlCmd += "proc_date,";
		sqlCmd += "mod_time,";
		sqlCmd += "mod_pgm ";
		sqlCmd += "from col_liac_nego_t where rowid = ? ";
		setString(1, hClnoLiacSeqno);
		setString(2, hClnoAcctStatusApply);
		setString(3, hClnoMCodeApply);
		setString(4, hClntProcFlag);
		setRowId(5, hClntRowid);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_" + daoTable + "duplicate!", "", hCallBatchSeqno);
		} else
			showLogMessage("I", "", "insert col_liac_nego_hst success !");
	}

	/***********************************************************************/
	void deleteColLiacNegoT() throws Exception {
		daoTable = "col_liac_nego_t";
		whereStr = "where rowid = ? ";
		setRowId(1, hClntRowid);
		deleteTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("delete_col_liac_nego_t not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void printHeader() throws Exception {

		buf = "";
		pageCnt++;
		buf = comcr.insertStr(buf, "報表名稱: " + rptName1, 1);
		buf = comcr.insertStr(buf, rptDesc1, 47);
		buf = comcr.insertStr(buf, "頁    次:", 93);
		szTmp = String.format("%4d", pageCnt);
		buf = comcr.insertStr(buf, szTmp, 101);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "印表日期:", 93);
		buf = comcr.insertStr(buf, chinDate, 101);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "轉入日期:", 1);
		szTmp = String.format("%8d", comcr.str2long(hclntfiledate));		
		//szTmp = String.format("%8d", comcr.str2long(hBusiBusinessDate));
		buf = comcr.insertStr(buf, szTmp, 10);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "身份證號", 1);
		buf = comcr.insertStr(buf, "主檔狀態", 14);
		buf = comcr.insertStr(buf, "暫存檔狀態", 26);
		buf = comcr.insertStr(buf, "狀態日期", 38);
		buf = comcr.insertStr(buf, "處理結果", 50);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		// buf = "\n";

		// 表頭分隔線=====
		buf = "";
		for (int i = 0; i < 80; i++) {
			buf += "=";
		}
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
	}

	/***********************************************************************/
	void printDetail() throws Exception {
		String tmpstr = "";

		lineCnt++;
//		if (lineCnt >= 31) {
//			printHeader();
//			lineCnt = 0;
//		}
		if (lineCnt > 25) {
			lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", "##PPP"));
			lineCnt = 0;
		}
		if (lineCnt == 0) {
			printHeader();
		}

		buf = "";
		buf = comcr.insertStr(buf, hClntId, 1);
		buf = comcr.insertStr(buf, hClnoLiacStatus, 14); /* 主檔狀態 */
		buf = comcr.insertStr(buf, hClntLiacStatus, 26); /* 暫存檔狀態 */
		//buf = comcr.insertStr(buf, hClntApplyDate,  36); /* 申請日期 */
		buf = comcr.insertStr(buf, hClntNotifyDate,  38); /* 狀態日期 */
		buf = comcr.insertStr(buf, errStr, 50);           /* 錯誤訊息*/
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
	}

	/***********************************************************************/
	void printTailer() throws Exception {
		buf = "\n";
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "新  增: ", 10);
		szTmp = comcr.commFormat("3z,3z,3z", insertCnt);
		buf = comcr.insertStr(buf, szTmp, 20);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
		
		buf = "";
		buf = comcr.insertStr(buf, "更  新: ", 10);
		szTmp = comcr.commFormat("3z,3z,3z", updateCnt);
		buf = comcr.insertStr(buf, szTmp, 20);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
		
		buf = "";
		buf = comcr.insertStr(buf, "失  敗: ", 10);
		szTmp = comcr.commFormat("3z,3z,3z", errCnt);
		buf = comcr.insertStr(buf, szTmp, 20);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
	}

	/***********************************************************************/

	void printSucess() throws Exception {
		errStr = "新增成功";
		showLogMessage("I", "", String.format("處理完成 = ID[%s] status[%s] [%s]", hClntId, hClntLiacStatus ,errStr));
		insertCnt++;
		printDetail();
	}

	/***********************************************************************/

	void printUpdateSucess() throws Exception {
		errStr = "更新成功";
		showLogMessage("I", "", String.format("處理完成 = ID[%s] status[%s] [%s]", hClntId, hClntLiacStatus ,errStr));
		updateCnt++;
		printDetail();
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		ColA106 proc = new ColA106();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	void fGetAcno() throws Exception {

		if (hClntIdPSeqno.equals(""))
			return;

		sqlCmd = "select max(acct_status) acct_status, ";
		sqlCmd += "max(int_rate_mcode) int_rate_mcode ";
		sqlCmd += "from act_acno ";
		sqlCmd += "where id_p_seqno = ? and acno_flag='1'"; //前置協商判斷，只抓一般卡的部分
		setString(1, hClntIdPSeqno);

		extendField = "act_acno.";

		if (selectTable() > 0) {
			hClnoAcctStatusApply = getValue("act_acno.acct_status");
			int mcode = 0;
			mcode = getValueInt("act_acno.int_rate_mcode");
			if (mcode > 99)
				mcode = 99;
			hClnoMCodeApply = String.format("%02d", mcode);
		}
	}

}

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR                       DESCRIPTION              *
*  --------  -------------------  ------------------------------------------ *
* 112/07/10  V1.00.01    sunny     program initial            *
* 112/07/12  V1.00.02    Ryan      modify                                    *
* 112/07/13  V1.00.03    Ryan      調整繳款截止日-x天                                                         *
* 112/07/15  V1.00.04    sunny     調整繳款截止日-x天處理方式且產生big5格式                    *
* 112/07/19  V1.00.05    sunny     縮小查詢範圍，改善效能& min_pay改抓stmt_mp       *
* 112/08/27  V1.00.06    sunny     更新提醒繳款email發送訊息文字(台幣)                             *
* 112/09/09  V1.00.07    sunny     修正程式處理催呆戶判斷的問題                                         *
*****************************************************************************/
package Col;

import java.nio.file.Paths;
import java.util.HashMap;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;

public class ColD005 extends AccessDAO {
	public final boolean debug = false;
	public final boolean debug2 = false;
	public final boolean debug3 = false;
	private static final int OUTPUT_BUFF_SIZE = 1000;
	private String progname = "篩選 MCode=M0,產生提醒繳款EMAIL通知(繳款截止日-x天工作日) 112/08/27  V1.00.06";
	private static final String DATA_FOLDER = "/media/col/";
	private static final String DATA_FORM = "CARDM07";
	private static final String CRDATACREA = "CRDATACREA";
	private final static String COL_SEPERATOR = "|&";
	private final static String LINE_SEPERATOR = System.lineSeparator();

//	CommCrd commCrd = new CommCrd();
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate comDate = new CommDate();
	CommString comStr = new CommString();
	String hCallBatchSeqno = "";
	private String hBusiBusinessDate = "";

	private long totalCnt = 0;
	private long prsCount = 0;
	
	private long acctType06Cnt  = 0;
	private long negoCnt = 0;
	private long cpb1Cnt = 0;
	private long cpb2Cnt = 0;
	private long cpb3Cnt = 0;
	private long cpb4Cnt = 0;
	private long liadCnt = 0;

	private String hWdayStmtCycle = "";
	private String hTempThisLastpayDate = "";
	private String hWdayLastAcctMonth = "";
	private String hTempNewSendDate = "";
	private String hCorpIdNo = "";
	private String hAcnoCorpIdPSeqno = ""; //20230715 add
	private String hAcnoAcctType="";       //20230715 adds
	private String hIdnoEMailAddr = "";
	private String hAcnoGpNo = "";
	private double hAcctMinPayBal = 0;
	private double hAcctTtlAmtBal = 0;
	private double hApdlPayAmt = 0;
	private double hAibmTxnAmt = 0;
	private double hTempEndBalAf = 0;
	private double hAchtStmtThisTtlAmt = 0;
	private double hStmtMp = 0;
	private int sycleRecordCnt = 0;
	private int hRunday = 0;
    private String newBusiBusinessDate = "";
	
	// file
	String buf = "";
	String temstr = "";
	int out = -1;

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname + "," + args.length);
			// =====================================

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

//			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

//			comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

			if (args.length > 2) {
				comcr.errRtn("Usage : ColD005 [business_date][runday]", "", "");
			}

			selectPtrBusinday();

			if (args.length > 0 && args[0].length() == 8) {
				hBusiBusinessDate = args[0];
			} else if (args.length > 0 && args[0].length() != 8) {
				hRunday = comStr.ss2int(args[0]);
			}
			
			if (args.length > 1 )
				hRunday = comStr.ss2int(args[1]);


			showLogMessage("I", "", String.format("business_date=[%s]", hBusiBusinessDate));
			showLogMessage("I", "", String.format("runday=[%d] \n", hRunday));
			showLogMessage("I", "", String.format("本程式為繳款截止日[%d]之營業日執行\n", hRunday));
			
			selectPtrWorkday();

			// 產生檔案名稱
			String datFileName = String.format("%s__%s.dat", DATA_FORM, hBusiBusinessDate);

			String fileFolder = Paths.get(comc.getECSHOME(), DATA_FOLDER).toString();

			// 產生主要檔案 .DAT
			int rowCnt = generateDatFile(fileFolder, datFileName);

			//有資料才執行
			if (rowCnt > 0 ) {
				procFTP(datFileName, fileFolder);
				moveFile(datFileName, fileFolder);

			}

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", String.format("\n產生總筆數=[%d]", rowCnt));
			comcr.hCallErrorDesc = "程式執行結束";
			comcr.callbatchEnd();
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***************************************************************************/
	private void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = "select business_date ";
		sqlCmd += "from   ptr_businday ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}

		hBusiBusinessDate = getValue("business_date");
	}

	/*************************************************************************/

	private void selectPtrWorkday() throws Exception {
		extendField = "workday.";
		sqlCmd = "SELECT  stmt_cycle,this_lastpay_date,last_acct_month ";
		sqlCmd += "  from  ptr_workday ";
		sqlCmd += " where  1=1 ";
		sqlCmd += " order  by stmt_cycle ";
		sycleRecordCnt = selectTable();
	}
	
	/*************************************************************************/

	/***
	 * 代入cycle查詢act_acno現行payment_rate1 等於 0A~0E 的族群
	 * 
	 * @throws Exception
	 */
	
	private void selectActAcno() throws Exception {
		fetchExtend = "acno.";
		selectSQL = "a.acno_p_seqno, a.p_seqno, a.id_p_seqno, a.acct_type,"
				+ "decode( a.acno_flag,'1',b.id_p_seqno,c.corp_p_seqno) as corp_id_p_seqno, "
				+ "decode( a.acno_flag,'1',b.id_no,c.corp_no) as corp_id_no, "
				+ "decode( a.acno_flag,'1',b.e_mail_addr,c.e_mail_addr) as e_mail_addr, "
				+ "a.acno_flag ";
		daoTable = "act_acno a,act_acct d "
				+ "left join crd_idno b on b.id_p_seqno = a.id_p_seqno "
				+ "left join crd_corp c on a.corp_p_seqno = c.corp_p_seqno ";
		whereStr = "where a.p_seqno = d.p_seqno "
				+ "and a.stmt_cycle = ? and   a.payment_rate1 >= '0A' and  a.payment_rate1 <= '0E' " // rate1='0A'~'0E'
				+ "and a.acct_status < '3' " //不含催呆戶
				+ "and d.min_pay_bal > 0 "; //縮小查詢範圍，改善效能
	
		/*
		selectSQL = "a.acno_p_seqno, a.p_seqno, a.id_p_seqno, a.acct_type,"
				+ "decode( a.acno_flag,'1',b.id_p_seqno,c.corp_p_seqno) as corp_id_p_seqno, "
				+ "decode( a.acno_flag,'1',b.id_no,c.corp_no) as corp_id_no, "
				+ "decode( a.acno_flag,'1',b.e_mail_addr,c.e_mail_addr) as e_mail_addr, "
				+ "a.acno_flag ";
		daoTable = "act_acno a left join crd_idno b on b.id_p_seqno = a.id_p_seqno "
				+ " left join crd_corp c on a.corp_p_seqno = c.corp_p_seqno ";
		whereStr = "where a.stmt_cycle = ? and   a.payment_rate1 >= '0A' and  a.payment_rate1 <= '0E' "; // rate1='0A'~'0E'		
	*/
		
		if(debug)
		{
//		whereStr += "and a.acct_type!='06' "; //排除採購卡，寫在下面java判斷式中
//		whereStr += "or a.payment_rate1 <= '02' "; // sunny test
//		whereStr += "and a.acno_flag<>'1'";
//	    whereStr += "and a.p_seqno='0007574491' ";// sunny test
		}
		setString(1, hWdayStmtCycle);
		openCursor();
	}
	
	/*************************************************************************/
	private int processData() throws Exception {
		hAcnoCorpIdPSeqno = getValue("acno.corp_id_p_seqno");
		hAcnoAcctType = getValue("acno.acct_type");
		hAcnoGpNo = getValue("acno.p_seqno");
		hCorpIdNo = getValue("acno.corp_id_no");
		hIdnoEMailAddr = getValue("acno.e_mail_addr");
		
		int rCnt1=0;
		
		totalCnt++;
		if (totalCnt % 5000 == 0)
			showLogMessage("I", "", "    讀取筆數 =[" + totalCnt + "]");
       
		// *******************************************************
		//排除政府採購卡
		if (hAcnoAcctType.equals("06"))
		{
			 acctType06Cnt++;
			 if(debug3)
			 showLogMessage("I", "", "    排除政府採購卡 corp_id_p_seqno =[" + hAcnoCorpIdPSeqno + "]");
			 return 1;
		}
		// *******************************************************
		//排除協商成功戶--前置協商
				 		
		 setValue("nego.id_p_seqno",hAcnoCorpIdPSeqno);
         rCnt1 = getLoadData("nego.id_p_seqno");
         if (rCnt1!=0)
         {        	 
        	 negoCnt++;
        	 if(debug3)
        		 showLogMessage("I", "", "    為協商成功戶-前置協商，予以排除 corp_id_p_seqno =[" + hAcnoCorpIdPSeqno + "]");
        	 return 1;
         }
                  
         
       // *******************************************************  
       //排除協商成功戶--公會協商
				
         rCnt1 = 0;
         
 		 setValue("cpb1.id_p_seqno",hAcnoCorpIdPSeqno);
          rCnt1 = getLoadData("cpb1.id_p_seqno");
          if (rCnt1!=0)
          {
        	 cpb1Cnt++;
        	 if(debug3)
        		 showLogMessage("I", "", "    為協商成功戶-公會協商，予以排除 corp_id_p_seqno =[" + hAcnoCorpIdPSeqno + "]");
         	 return 1;
          }                   
          
      // *******************************************************
      // 排除協商成功戶--個別協商—個人
         
         setValue("cpb2.id_p_seqno",hAcnoCorpIdPSeqno);
         rCnt1 = getLoadData("cpb2.id_p_seqno");
         if (rCnt1!=0) 
         {
        	 cpb2Cnt++;
        	 if(debug3)
        		 showLogMessage("I", "", "    為協商成功戶-個別協商(個人)，予以排除 corp_id_p_seqno =[" + hAcnoCorpIdPSeqno + "]");
        	 return 1;
         }                
         
      // *******************************************************
      // 排除協商成功戶--個別協商—公司   
         setValue("cpb3.corp_p_seqno", hAcnoCorpIdPSeqno);
         setValue("cpb3.acct_type"   , hAcnoAcctType);
         rCnt1 = getLoadData("cpb3.corp_p_seqno,cpb3.acct_type");
         if (rCnt1!=0) 
         {
        	 cpb3Cnt++;
        	 if(debug3)
        		 showLogMessage("I", "", "    為協商成功戶-個別協商(公司)，予以排除 corp_id_p_seqno =[" + hAcnoCorpIdPSeqno + "]");
        	 return 1;
         } 
         
      // *******************************************************
      // 排除協商成功戶--前置調解
         setValue("cpb4.id_p_seqno",hAcnoCorpIdPSeqno);
         rCnt1 = getLoadData("cpb4.id_p_seqno");
         if (rCnt1!=0) 
         {
        	 cpb4Cnt++;
        	 if(debug3)
        	 showLogMessage("I", "", "    為協商成功戶--前置調解，予以排除 corp_id_p_seqno =[" + hAcnoCorpIdPSeqno + "]");
        	 return 1;
         } 
       
      // *******************************************************
 		//排除協商--有更生清算
 				 		
 		 setValue("liad.id_p_seqno",hAcnoCorpIdPSeqno);
          rCnt1 = getLoadData("liad.id_p_seqno");
          if (rCnt1!=0)
          {
        	  liadCnt++;
        	  if(debug3)
        	  showLogMessage("I", "", "    為協商戶-更生清算，予以排除 corp_id_p_seqno =[" + hAcnoCorpIdPSeqno + "]");
         	 return 1;
          }
       // *******************************************************
         if(debug)
         showLogMessage("I", "", "    非協商成功戶 corp_id_p_seqno =[" + hAcnoCorpIdPSeqno + "]");
         
		/*--min-pay is 0---------------------------------------------*/
		selectActAcct();
		selectActPayDetail();
		selectActPayIbm();
				

		/*--排除帳單僅有年費者-------------------------------------------*/
		selectActAcctHst();

		// 如帳單應繳總額=年費金額，則跳過不處理 (先留著判斷，未來不需要再MARK)		
//		if (hAchtStmtThisTtlAmt == hTempEndBalAf)
//		return 1;
		
		if (debug2) 
		{
			if (hAcctTtlAmtBal < 0)
			if(debug)
			showLogMessage("I", "",
					"selectActAcctHst() CorpIdNo=[" + hCorpIdNo + "] EMailAddr=[" + hIdnoEMailAddr + "] "
					+"hst_last_acct_month=[" + hWdayLastAcctMonth + "]" + " hst_stmt_this_ttl_amt=[" + hAchtStmtThisTtlAmt + "] "
					+ "Acct_ttl_amt_bal=[" + hAcctTtlAmtBal + "] AF=[" + hTempEndBalAf + "]");
							
		}
		
		// 計算最低金額餘額 – 第5點取得之繳款金額，若hAcctMinPayBal<0則令hAcctMinPayBal=0
		hAcctMinPayBal = hAcctMinPayBal - hApdlPayAmt - hAibmTxnAmt;
		if (hAcctMinPayBal < 0)
			hAcctMinPayBal = 0;
		
		// 計算總應繳餘額 – 第5點取得之繳款金額，若hAcctTtlAmtBal<0則令hAcctTtlAmtBal=0
		hAcctTtlAmtBal = hAcctTtlAmtBal - hApdlPayAmt - hAibmTxnAmt;
		if (hAcctTtlAmtBal < 0)
			hAcctTtlAmtBal = 0;
		
		// 判斷最低金額餘額若等於0，則此筆跳過
		if (hAcctMinPayBal == 0) {
			if (debug2)
				//showLogMessage("I", "", "-- min pay is 0 not send");
				showLogMessage("I", "" ,String.format("min pay is 0，corp_id_no[%s] email[%s]", hCorpIdNo, hIdnoEMailAddr));
			return 1;
		}

		// 判斷act_acct_hst最低金額餘額若等於0，則此筆跳過
				if (hStmtMp == 0) {
					if (debug)
						showLogMessage("I", "" ,String.format("[debug] act_acct_hst min pay is 0，corp_id_no[%s] email[%s]", hCorpIdNo, hIdnoEMailAddr));
					return 1;
				}
				
		//corp_id_no為空值或無email，則此筆跳過
		if(comStr.empty(hCorpIdNo)||comStr.empty(hIdnoEMailAddr))
		{
			if (debug)
			showLogMessage("I", "" ,String.format("無email，corp_id_no[%s] email[%s]", hCorpIdNo, hIdnoEMailAddr));
			return 1;
		}
		
		if (debug)
		showLogMessage("I", "" ,String.format("corp_id_no[%s] email[%s]", hCorpIdNo, hIdnoEMailAddr));

		return 0;
	}



	/***
	 * 取得act_acct 最低應繳餘額及總應繳餘額
	 * 
	 * @throws Exception
	 */
	private void selectActAcct() throws Exception {
		sqlCmd = "select min_pay_bal, ";
		sqlCmd += "       ttl_amt_bal ";
		sqlCmd += "from   act_acct ";
		sqlCmd += "where  p_seqno = ? ";
		setString(1, hAcnoGpNo);

		extendField = "act_acct.";

		if (selectTable() > 0) {
			hAcctMinPayBal = getValueDouble("act_acct.min_pay_bal");
			hAcctTtlAmtBal = getValueDouble("act_acct.ttl_amt_bal");
		}
	}

	/***
	 * 查詢是否有繳款尚未沖銷的金額
	 * 
	 * @throws Exception
	 */
	private void selectActPayDetail() throws Exception {
		sqlCmd = "select sum(pay_amt) as h_apdl_pay_amt ";
		sqlCmd += "from   act_pay_detail d,act_pay_batch b ";
		sqlCmd += "where  b.batch_no = d.batch_no ";
		sqlCmd += "and    p_seqno = ? ";
		setString(1, hAcnoGpNo);

		extendField = "act_pay_detail.";

		if (selectTable() > 0) {
			hApdlPayAmt = getValueDouble("act_pay_detail.h_apdl_pay_amt");
		}
	}

	/*************************************************************************/
	private void selectActPayIbm() throws Exception {
		sqlCmd = "select sum(txn_amt) as h_aibm_txn_amt ";
		sqlCmd += "from   act_pay_ibm ";
		sqlCmd += "where  decode(proc_mark, '',' ', proc_mark) !='Y' ";
		sqlCmd += "and    p_seqno = ? ";
		setString(1, hAcnoGpNo);

		extendField = "act_pay_ibm.";

		if (selectTable() > 0) {
			hAibmTxnAmt = getValueDouble("act_pay_ibm.h_aibm_txn_amt");
		}
	}

	/***
	 * 查詢帳單歷史檔的年費餘額(未入帳+已入帳)，帳單應繳總額
	 * 
	 * @throws Exception
	 */
	private void selectActAcctHst() throws Exception {
		sqlCmd = "select (unbill_end_bal_af+billed_end_bal_af) as h_temp_end_bal_af,stmt_mp, "; //原min_pay(上期mp)改成STMT_MP(本期mp)
		sqlCmd += " stmt_this_ttl_amt ";
		sqlCmd += "from   act_acct_hst  ";
		sqlCmd += "where  p_seqno = ? ";
		sqlCmd += "and    decode(acct_month, '','x', acct_month) = ? ";
		setString(1, hAcnoGpNo);
		setString(2, hWdayLastAcctMonth);

		extendField = "act_acct_hst.";

		if (selectTable() > 0) {
			hTempEndBalAf = getValueDouble("act_acct_hst.h_temp_end_bal_af");
			hAchtStmtThisTtlAmt = getValueDouble("act_acct_hst.stmt_this_ttl_amt");
			hStmtMp = getValueDouble("act_acct_hst.stmt_mp");
		}
	}
	
	
	/************************************************************************/
	 void loadColLiacNego() throws Exception // 前置協商
	 {
	  extendField = "nego.";
	  selectSQL = "id_p_seqno,"
	            + "liac_status";
	  daoTable  = "col_liac_nego";
	  whereStr  = "where liac_status in ('1','2','3') ";

	  int  n = loadTable();
	  setLoadData("nego.id_p_seqno");
	  showLogMessage("I","","Load col_liac_nego  Count: ["+n+"]");
	 }
	 /************************************************************************/
	 void loadColLiadNego() throws Exception // 更生清算
	 {
	  extendField = "liad.";
	  selectSQL = "id_p_seqno,"
	            + "liad_status";
	  daoTable  = "col_liad_renewliqui";
	  whereStr  = "";

	  int  n = loadTable();
	  setLoadData("liad.id_p_seqno");
	  showLogMessage("I","","Load col_liad_renewliqui  Count: ["+n+"]");
	 }
	/************************************************************************/
	 void loadColCpbdue1() throws Exception // 公會協商
	 {
	  extendField = "cpb1.";
	  selectSQL = "distinct cpbdue_id_p_seqno as id_p_seqno";
	  daoTable  = "col_cpbdue";
	  whereStr  = "where cpbdue_type='1' "
	            + "and   cpbdue_bank_type in ('1','2','3') "
	            + "and   cpbdue_acct_type='01' ";
		 
	  int  n = loadTable();
	  setLoadData("cpb1.id_p_seqno");
	  showLogMessage("I","","Load col_cpbfue1  Count: ["+n+"]");
	 }
	 /************************************************************************/
	 void loadColCpbdue2() throws Exception // 個別協商—個人 
	 {
	  extendField = "cpb2.";
	  selectSQL = "distinct cpbdue_id_p_seqno as id_p_seqno";
	  daoTable  = "col_cpbdue";
	  whereStr  = "where cpbdue_type='2' "             
	            + "and   cpbdue_tcb_type in ('1','2','3') "
	            + "and   cpbdue_acct_type='01' ";

	  int  n = loadTable();
	  setLoadData("cpb2.id_p_seqno");
	  showLogMessage("I","","Load col_cpbfue2  Count: ["+n+"]");
	 }
	
	 /************************************************************************/
	 void loadColCpbdue3() throws Exception // 個別協商—公司 
	 {
	  extendField = "cpb3.";
	  selectSQL = "cpbdue_id_p_seqno as corp_p_seqno,"
	            + "cpbdue_acct_type  as acct_type";
	  daoTable  = "col_cpbdue";
	  whereStr  = "where cpbdue_type='2' "
	            + "and cpbdue_tcb_type in ('1','2','3') "
	            + "and cpbdue_acct_type='03' "
	            + "group by cpbdue_id_p_seqno,cpbdue_acct_type "
	            + "order by cpbdue_id_p_seqno,cpbdue_acct_type ";

	  int  n = loadTable();
	  setLoadData("cpb3.corp_p_seqno,cpb3.acct_type");
	  showLogMessage("I","","Load col_cpbfue3  Count: ["+n+"]");
	 }
	
	 /************************************************************************/
	 void loadColCpbdue4() throws Exception // 前置調解
	 {
	  extendField = "cpb4.";
	  selectSQL = "distinct cpbdue_id_p_seqno as id_p_seqno";
	  daoTable  = "col_cpbdue";
	  whereStr  = "where cpbdue_type='3' "
	            + "and cpbdue_medi_type in ('1','2','3') "
	            + "and cpbdue_acct_type='01' ";

	  int  n = loadTable();
	  setLoadData("cpb4.id_p_seqno");
	  showLogMessage("I","","Load col_cpbfue4  Count: ["+n+"]");
	 }
	 
	 /************************************************************************/
	/**
	 * DETAIL-DATA
	 * 
	 * @return String
	 * @throws Exception
	 */
	private String getRowOfDetail00() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("00", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(hCorpIdNo, 11));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("02", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(hIdnoEMailAddr, 50)); // 新系統可允許到50bytes
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("合作金庫銀行信用卡繳款提醒函，若已繳費請無需理會", 50));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("親愛的客戶您好！", 30));
		sb.append(comc.fixLeft(COL_SEPERATOR, 19));
		sb.append(comc.fixLeft(" ", 60));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/*****************************************************************************/
	private String getRowOfDetail01() throws Exception {
		String thisLastpayDate = comDate.toTwDate(hTempThisLastpayDate);
		String dateY = comStr.left(thisLastpayDate, 3);
		String dateM = comStr.mid(thisLastpayDate,3,2);
		String dateD = comStr.right(thisLastpayDate, 2);
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("01", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(String.format("感謝您使用合庫信用卡，您本月份信用卡繳款截止日為%03d年%s月%s日，應繳總額為台幣 %11s元，最低應繳金額為台幣 %11s元，提醒您按時繳款。<P>本行每月會以ｅ－ｍａｉｌ提醒您繳納信用卡帳款，"
				,comStr.ss2int(dateY) ,dateM ,dateD,comStr.numFormat(hAchtStmtThisTtlAmt, "#,###"),comStr.numFormat(hStmtMp, "#,###")),210));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/*****************************************************************************/
	private String getRowOfDetail02() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("02", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("如您已繳納，請忽略本通知。", 210));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/*****************************************************************************/
	private String getRowOfDetail03() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("03", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(
				"若您欲查詢帳款繳納方式，請<a href='https://www.tcb-bank.com.tw/personal-banking/credit-card/common-services/faq'>按此查詢</a>【常見問題／項目篩選下拉選擇「帳單相關」→ 「帳款繳付方式」】。",
				210));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/*****************************************************************************/
	private String getRowOfDetail04() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("04", 2));
		sb.append(COL_SEPERATOR);
		sb.append(
				comc.fixLeft("<P><font color=\"red\"><u>**台端已辦理帳戶自動轉帳繳款，請於繳款截止日前一營業日，將應扣繳金額存入您自動轉帳帳戶中，以期能順利扣款**", 210));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/*****************************************************************************/

	/**
	 * generate file
	 * 
	 * @param fileFolder  檔案的資料夾路徑
	 * @param datFileName 檔名
	 * @return the number of rows written. If the returned value is -1, it means the
	 *         path or the file does not exist.
	 * @throws Exception
	 */
	private int generateDatFile(String fileFolder, String datFileName) throws Exception {

		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		String rowOfTXT = "";
		StringBuffer sb = new StringBuffer();
		showLogMessage("I", "", "判斷是否為執行日，開始產生檔案............");
		try {
			for (int i = 0; i < sycleRecordCnt; i++) {
				hWdayStmtCycle = getValue("workday.stmt_cycle", i);
				hTempThisLastpayDate = getValue("workday.this_lastpay_date", i);
				hWdayLastAcctMonth = getValue("workday.last_acct_month", i);
				hTempNewSendDate = getValue("workday.new_send_date", i);
				
				newBusiBusinessDate = comcr.increaseDays(hTempThisLastpayDate, hRunday);
	        	  
	        	showLogMessage("I", "", "取得cycle["+hWdayStmtCycle+"],前次帳單月份["+hWdayLastAcctMonth+"],繳款截止日["+hTempThisLastpayDate+"] " + hRunday + "日後的工作日為["+newBusiBusinessDate+"]");

				if(!newBusiBusinessDate.equals(hBusiBusinessDate))
					continue;
								
				showLogMessage("I", "", String.format("今日為程式執行日期(cycle[%s]繳款截止日[%d]天) ,[%s]",hWdayStmtCycle,hRunday,newBusiBusinessDate));

				showLogMessage("I", "", "開始讀取資料......");
				
				//讀取協商申請中&成功戶--有前置協商狀態
				loadColLiacNego();
				//讀取協商申請中&成功戶--公會協商
				loadColCpbdue1();
				//讀取協商申請中&成功戶--個別協商—個人
				loadColCpbdue2();
				//讀取協商申請中&成功戶--個別協商—公司
				loadColCpbdue3();
				//讀取協商申請中&成功戶--前置調解
				loadColCpbdue4();
				//讀取協商--有更生清算狀態
				loadColLiadNego();
				   
				selectActAcno();
				while (fetchTable()) {
					initData();
					int resultCode = processData();
					if (resultCode == 1)
						continue;
					rowOfTXT = getRowOfDetail00();
					rowOfTXT += getRowOfDetail01();
					rowOfTXT += getRowOfDetail02();
					rowOfTXT += getRowOfDetail03();
					rowOfTXT += getRowOfDetail04();
					sb.append(rowOfTXT);
					rowCount++;
					countInEachBuffer++;
					if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
						showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
						byte[] tmpBytes = sb.toString().getBytes("MS950");
						writeBinFile(tmpBytes, tmpBytes.length);
						sb = new StringBuffer();
						countInEachBuffer = 0;
					}
				}
				closeCursor();
			}
			// write the rest of bytes on the file
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("排除政府採購卡[%d]筆資料", acctType06Cnt));
				showLogMessage("I", "", String.format("排除協商申請中/成功戶--前置調解[%d]筆資料", negoCnt));
				showLogMessage("I", "", String.format("排除協商申請中/成功戶--公會協商[%d]筆資料", cpb1Cnt));
				showLogMessage("I", "", String.format("排除協商申請中/成功戶--個別協商—個人[%d]筆資料", cpb2Cnt));
				showLogMessage("I", "", String.format("排除協商申請中/成功戶--個別協商—公司[%d]筆資料", cpb3Cnt));
				showLogMessage("I", "", String.format("排除協商申請中/成功戶--前置調解[%d]筆資料", cpb4Cnt));
				showLogMessage("I", "", String.format("排除協商--有更生清算[%d]筆資料", liadCnt));				
				showLogMessage("I", "", String.format("將剩下的[%d]筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes("MS950");
				writeBinFile(tmpBytes, tmpBytes.length);
			}

			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入檔案");
			} else {
				showLogMessage("I", "", String.format("產生檔案完成！，共產生%d筆資料", rowCount));
			}
		} finally {
			closeBinaryOutput();
		}
		return rowCount;
	}

	/*****************************************************************************/

	void procFTP(String hdrFileName, String fileFolder) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = CRDATACREA; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		String ftpCommand = String.format("mput %s", hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName(CRDATACREA, ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void moveFile(String datFileName1, String fileFolder1) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder1, "/backup", datFileName1).toString();

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName1 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}
	
	void initData() {
		 hCorpIdNo = "";
		 hIdnoEMailAddr = "";
		 hAcnoGpNo = "";
		 hAcctMinPayBal = 0;
		 hAcctTtlAmtBal = 0;
		 hApdlPayAmt = 0;
		 hAibmTxnAmt = 0;
		 hTempEndBalAf = 0;
		 hAchtStmtThisTtlAmt = 0;
		 hStmtMp = 0;
	}

	/*****************************************************************************/
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ColD005 proc = new ColD005();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}

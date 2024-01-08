/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/05/23  V1.00.00  Ryan       1st ver                                    *
* 112/07/04  V1.00.01  Ryan       modify                                     *
* 112/09/20  V1.00.02  Ryan       onlyaddon_calcond = Y & atmibwf_cond = Y 才計算ATM手續費加碼回饋              *
* 112/09/25  V1.00.03  Ryan       group by keep p_seqno, major_id_p_seqno, acct_type 在寫到combosum             *
* 112/12/15  V1.00.04  Holmes     fix getBegTranAmtRate bug ,change procBonus as procCashAmt and calculate logic*        
******************************************************************************/
package Cyc;

import com.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("unchecked")
public class CycA315 extends AccessDAO {
	private final String PROGNAME = "COMBO現金回饋批次產生處理 112/12/15  V1.00.04";
	private final static int COMMIT_CNT = 2000;
	CommFunction comm = new CommFunction();
	CommString commStr = new CommString();
	CommDate commDate = new CommDate();
	CommDCFund comDCF = null;
	CommRoutine comr = null;
	HashMap<String,String> tmpMap = new HashMap<String,String>();
	String hBusinessDate = "";
	String fundCode = "";
	String parmPSeqno = "";
	double begTranAmtRate = 0;
	int parmCnt = 0;
	int bonuCnt = 0;
	double begTranAmt = 0;
	String tranSeqno = "";
	double destAmt = 0;
	double sumDestAmt = 0;
	String atmibwfCondFlag = "";
	boolean DEBUG1 = false;
	boolean DEBUG2 = false;
	String feedbackType = "";
	int cycleFlag = 0;
	StringBuffer fundCodeBuf = new StringBuffer();

	long totalCnt = 0, updateCnt = 0;
	int inti;

	String pSeqno ="";
	String idPSeqno ="";
	String acctType  ="";
	String majorCardNo ="";
	
// ************************************************************************
	public static void main(String[] args) throws Exception {
		CycA315 proc = new CycA315();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
		return;
	}

// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);

			if (comm.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式已有另依程式啟動中, 不執行..");
				return (0);
			}

			if (args.length > 4) {
				showLogMessage("I", "", "請輸入參數:");
				showLogMessage("I", "", "PARM 1 : [feedbackType]");
				showLogMessage("I", "", "PARM 2 : [business_date]");
				showLogMessage("I", "", "PARM 3 : [fund_code]");
				showLogMessage("I", "", "PARM 4 : [p_seqno]");
				return (1);
			}

			if (args.length == 0 || (!args[0].equals("1") &&
						!args[0].equals("2"))) {
				showLogMessage("I","","請傳入回饋方式 : 1.每月 2.帳單週期 ");
				return(1);
			}  
		
			feedbackType = args[0];
			
			if (args.length >= 2) {
				hBusinessDate = args[1];
			}
			if (args.length >= 3) {
				fundCode = args[2];
			}
			if (args.length == 4) {
				parmPSeqno = args[3];
			}

			if (!connectDataBase())
				return (1);

			comDCF = new CommDCFund(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();

			cycleFlag = selectPtrWorkday();
//			if (cycleFlag == 1) {
//				showLogMessage("I", "", "本程式只在每月01日關帳日執行,本日為" + hBusinessDate + "日..");
//				return (0);
//			}
			
		   if ( (feedbackType.equals("2")) && !( cycleFlag == 0 ) 
			    && (!"01".equals(commStr.right(hBusinessDate , 2))) )   {
			       showLogMessage("I","","回饋方式 : 2.帳單週期 ,本日不是1號關帳日,不需執行");
			       return(0);
		   }
		   if ((feedbackType.equals("1")) &&  (cycleFlag == 0) )   {
			       showLogMessage("I","","回饋方式 : 1.每月指定日 ,本日是關帳日,不需執行 ");
			       return(0);
		   }  

			showLogMessage("I", "", "this_acct_month[" + getValue("wday.this_acct_month") + "]");
			showLogMessage("I", "", "stmt_cycle[" + getValue("wday.stmt_cycle") + "]");
			
			
			showLogMessage("I", "", "=========================================");
			showLogMessage("I", "", "載入參數資料");
			if (selectPtrComboFundp() != 0) {
				showLogMessage("I", "", "ComBo基金參數無設定資料可執行!");
				return (0);
			}
			
			showLogMessage("I", "", "****************************");
			showLogMessage("I", "", "刪除暫存 MKT_ADDON_COMBSUM ,MKT_ADDON_ATMCBFEE 資料 .....");
			deleteTmpData();
			
			showLogMessage("I", "", "刪除歷史 MKT_ADDON_COMBSUM ,MKT_ADDON_ATMCBFEE 資料 .....");
			deleteMktAddonAtmcbfeeHis();
			deleteMktAddonCombsumHis();			
			
			showLogMessage("I", "", "=========================================");
			showLogMessage("I", "", "載入參數MKT_PARM_DATA,MKT_PARM_CDATA,mkt_mchtgp_data 明細資料");
			loadMktParmData();
			loadMktMchtgpData();
			loadMktParmCdata();

			showLogMessage("I", "", "****************************");
			showLogMessage("I", "", "處理 BIL_BILL .....");
			selectBilBill();
			showLogMessage("I", "", "處理 [" + totalCnt + "] 筆");

//			commitDataBase();
			showLogMessage("I", "", "****************************");
			showLogMessage("I", "", "處理存款餘額回饋加碼 ,計算現金回饋作業 .....");
			selectMktAddonComboSum();

//			commitDataBase();
			showLogMessage("I", "", "****************************");
			showLogMessage("I", "", "處理ATM手續費回饋加碼 ,進行加碼寫檔作業 .....");
			selectMktPbmbAtm8();
			
//			commitDataBase();
			showLogMessage("I", "", "****************************");
			showLogMessage("I", "", "處理ATM手續費回饋加碼 ,計算現金回饋作業 .....");
			selectMktAddonCbfee();
			
		    showLogMessage("I","","處理 ["+totalCnt+"] 筆");
			showLogMessage("I","","=========================================");	
			finalProcess();  			
			return (0);
		}
		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		} 
//		finally {
//			finalProcess();
//		}

	} // End of mainProcess
// ************************************************************************

	void selectPtrBusinday() throws Exception {
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		int recordCnt = selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		}

		if (hBusinessDate.length() == 0)
			hBusinessDate = getValue("BUSINESS_DATE");
		showLogMessage("I", "", "本日營業日 : [" + hBusinessDate + "]");
	}

// ************************************************************************ 
	int selectPtrWorkday() throws Exception {
		extendField = "wday.";
		selectSQL = "stmt_cycle," + "this_acct_month";
		daoTable = "ptr_workday";
		whereStr = "where this_close_date = ? and stmt_cycle = '01'";

		setString(1, hBusinessDate);

		int recCnt = selectTable();

		if (notFound.equals("Y") && feedbackType.equals("1")) {
			setValue("wday.stmt_cycle","01");
			setValue("wday.this_acct_month",commStr.left(hBusinessDate, 6));
			return (1);
		}else if(notFound.equals("Y")) {
			return (1);
		}
		

		return (0);
	}

// ************************************************************************
	int selectPtrComboFundp() throws Exception {
		extendField = "parm.";
		selectSQL = "";
		daoTable = "PTR_COMBO_FUNDP";
		whereStr = "WHERE apr_flag    = 'Y' " + "AND   (stop_flag  = 'N' " + " OR    (stop_flag = 'Y' "
				+ "  and   ? < stop_date)) and  feedback_type = ?  ";

		int ii = 1;
		setString(ii++ , hBusinessDate);
		setString(ii++ , feedbackType);
		
	    if("1".equals(feedbackType)) {
		   CommString coms = new CommString();
		   whereStr += " and card_feed_run_day = ? " ;
		   setInt(ii++ ,coms.ss2int(coms.right(hBusinessDate , 2)));
	    }	
	    
		if (fundCode.length() != 0) {
			whereStr = whereStr + "and   fund_code = ? ";
			setString(ii++ , fundCode);
		}
		

		whereStr = whereStr + "order by fund_code ";

		parmCnt = selectTable();

		showLogMessage("I", "", "參數檢核筆數 [" + parmCnt + "] 筆");

		if (parmCnt == 0)
			return (1);

		for (int i = 0; i < parmCnt; i++)
//			if (i == parmCnt -1  ) {
//			   fundCodeBuf.append("'").append((getValue("parm.fund_code", i))).append("'");
//			}else {
			fundCodeBuf.append("'").append((getValue("parm.fund_code", i))).append("',");
//			}
		showLogMessage("I", "", "fundCodeBuf: " + fundCodeBuf.toString());
		showLogMessage("I", "", "ComBo存款餘額設定參數,符合條件有 " + parmCnt + " 筆");
		return (0);
	}

// ************************************************************************
	void selectBilBill() throws Exception {
		daoTable = "bil_bill";
		whereStr = "where acct_code in ('BL','IT','ID','CA','OT','AO') " 
				+ "and   rsk_type not in ('1','2','3') "
				+ "and   merge_flag       != 'Y'  " 
				+ "and   curr_code    = '901' " 
				+ "and   dest_amt != 0 "
				+ "and   acct_month    = ?  " 
				+ "and   stmt_cycle  = ? " 
				+ "order by major_card_no ";
		setString(1, getValue("wday.this_acct_month"));
		setString(2, getValue("wday.stmt_cycle"));

		if (parmPSeqno.length() != 0) {
			whereStr = whereStr + "AND   p_seqno   = ? ";
			setString(3, parmPSeqno);
		}

		openCursor();
		totalCnt = 0;
		int[] currCnt = new int[parmCnt];
		double[] parmAmt = new double[parmCnt];
		for (int inti = 0; inti < parmCnt; inti++) parmAmt[inti] = 0;
		double[][] parmArr = new double[parmCnt][20];
		for (int inti = 0; inti < parmCnt; inti++) {
			for (int intk = 0; intk < 20; intk++) parmArr[inti][intk] = 0;
		}

//  sumDestAmt = new HashMap<String,Double>();
		while (fetchTable()) {
		    if ((!majorCardNo.equals(getValue("major_card_no"))) && (majorCardNo.length()!=0)) {
//			if ((!pSeqno.equals(getValue("P_SEQNO"))) && (pSeqno.length()!=0)) {		    	
//		    	procCashAmt(parmAmt);
		        for (int inti=0;inti<parmCnt;inti++) {
		        	if (parmAmt[inti] != 0 ) procCashAmt(parmAmt,inti);
		            parmAmt[inti]=0;
		        }
		    }
		    
		    majorCardNo = getValue("MAJOR_CARD_NO") ;	
		    pSeqno    = getValue("p_seqno");
		    idPSeqno = getValue("major_id_p_seqno");
		    acctType  = getValue("acct_type");	
						
			String acqId = "";
			if (getValue("acq_member_id").length() != 0)
				acqId = comm.fillZero(getValue("acq_member_id"), 8);

			if (getValue("group_code").length() == 0)
				setValue("group_code", "0000");
			
		    if (getValue("mcht_country").length()==3) 
		        setValue("mcht_country" , getValue("mcht_country").substring(0,2));  
		    
		    if (getValue("mcht_country").equals("")) 
		        setValue("mcht_country" , "TW");		    
			
			
			for (int inti = 0; inti < parmCnt; inti++) {
				
				//If PTR_COMBO_FUNDP.onlyaddon_calcond='Y' , 本筆參數 contunue不計算
				if("Y".equals(getValue("parm.onlyaddon_calcond",inti))) {
					continue;
				}
				
				parmArr[inti][0]++;
//				showLogMessage("I", "", "STEP 1  p_seqno [" + getValue("p_seqno") + "]  幣別[" + getValue("curr_code")
//							+ "]  基金[" + getValue("parm.fund_code", inti) + "]");

				parmArr[inti][1]++;

				setValue("data_key", getValue("parm.fund_code", inti));

				// 帳戶類別篩選
				if (selectMktParmData(getValue("acct_type"), "", getValue("parm.acct_type_sel", inti), "3" ,2) != 0)
					continue;
				parmArr[inti][2]++;

				// 特店篩選
				if (selectMktParmData(getValue("mcht_no"), acqId, getValue("parm.merchant_sel", inti), "4",3) != 0)
					continue;
				parmArr[inti][3]++;

				// 判斷特店中英名稱
				if (selectMktParmCdata(getValue("parm.mcht_cname_sel", inti),
						getValue("parm.mcht_ename_sel", inti)) != 0)
					continue;

				parmArr[inti][4]++;

				// 特店群組篩選
				if (selectMktMchtgpData(getValue("mcht_no"), "", getValue("parm.mcht_group_sel", inti), "6") != 0)
					continue;
				parmArr[inti][5]++;

				// 一般消費群組篩選
				if (selectMktMchtgpData(getValue("ECS_CUS_MCHT_NO"), "", getValue("parm.platform_kind_sel", inti),
						"P") != 0)
					continue;
				parmArr[inti][6]++;

				// 團代/卡種篩選
				if (selectMktParmData(getValue("group_code"), getValue("card_type"),
						getValue("parm.group_card_sel", inti), "1" ,3) != 0)
					continue;
				parmArr[inti][7]++;

				// 團體代號篩選
				if (selectMktParmData(getValue("group_code"), "", getValue("parm.group_code_sel", inti), "2" ,2) != 0)
					continue;
				parmArr[inti][8]++;

				// 國內外消費篩選: foreign_code
				if (!getValue("parm.foreign_code", inti).equals("3")) {
					if (getValue("parm.foreign_code", inti).equals("2") && getValue("mcht_country").equals("TW")) {
						continue;
					}
				}
				if (getValue("parm.foreign_code", inti).equals("1") && !getValue("mcht_country").equals("TW")) {
					continue;
				}

				parmArr[inti][9]++;

				// 消費六大本金進行篩選
				if ((!getValue("parm.bl_cond", inti).equals("Y")) && (getValue("acct_code").equals("BL")))
					continue;
				if ((!getValue("parm.it_cond", inti).equals("Y")) && (getValue("acct_code").equals("IT")))
					continue;
				if ((!getValue("parm.ca_cond", inti).equals("Y")) && (getValue("acct_code").equals("CA")))
					continue;
				if ((!getValue("parm.id_cond", inti).equals("Y")) && (getValue("acct_code").equals("ID")))
					continue;
				if ((!getValue("parm.ao_cond", inti).equals("Y")) && (getValue("acct_code").equals("AO")))
					continue;
				if ((!getValue("parm.ot_cond", inti).equals("Y")) && (getValue("acct_code").equals("OT")))
					continue;

				parmAmt[inti] = parmAmt[inti] + getValueDouble("dest_amt");

				if (DEBUG2) {
					showLogMessage("I", "", "STEP 2 parmamt[" + inti + "] = " + parmAmt[inti] + "]");
				}


				currCnt[inti]++;

			}
//			String tmpKey = String.format("%s_%s_%s", getValue("p_seqno"),getValue("major_id_p_seqno"),getValue("acct_type"));
//			if (tmpMap.get(tmpKey) == null && getValue("p_seqno").length() != 0 
//					&& getValue("major_id_p_seqno").length() != 0 && getValue("acct_type").length() != 0) {
//				procCashAmt(parmAmt);
//				
//				for (int inti = 0; inti < parmCnt; inti++) {
//					parmAmt[inti] = 0;
//				}
//				tmpMap.put(tmpKey, tmpKey);
//			}

//			if(totalCnt%COMMIT_CNT==0)
//				commitDataBase();
			
		    totalCnt++;			
		}
		closeCursor();
		for (int inti = 0; inti < parmCnt; inti++) {
			showLogMessage("I", "", "    [" + String.format("%03d", inti) + "] 基金 [" + getValue("parm.fund_code", inti)
					+ "]  [" + currCnt[inti] + "] 筆");

			if (fundCode.length() != 0)
				for (int intk = 0; intk < 20; intk++) {
					if (parmArr[inti][intk] == 0)
						continue;
					showLogMessage("I", "", " 測試絆腳石 :[" + inti + "][" + intk + "] = [" + parmArr[inti][intk] + "]");
				}

		}
        for (int inti=0;inti<parmCnt;inti++) {
        	if (parmAmt[inti] != 0 ) procCashAmt(parmAmt ,inti);
            parmAmt[inti]=0;
        }		
		showLogMessage("I", "", "=========================================");
		return;
	}

//************************************************************************
	double getBegTranAmtRate(double avg3mAmt , int inti) {
		for (int i = 1; i <= 5; i++) {
			if (avg3mAmt >= getValueDouble("parm.save_s_amt_" + i, inti) && avg3mAmt <= getValueDouble("parm.save_e_amt_" + i, inti)) {
				begTranAmtRate = getValueDouble("parm.save_rate_" + i, inti);
				break;
			}
		}
		if (begTranAmtRate <= 0.1) 
			begTranAmtRate = 0.1;
		return begTranAmtRate;
	}

// ************************************************************************
	int selectCrdCard(String majorCardNo) throws Exception {
		extendField = "card.";
    	selectSQL = "b.id_no ,a.combo_acct_no ,a.acct_type ,a.group_code ,a.stmt_cycle ";
		daoTable = "crd_card a , crd_idno b ";
//		whereStr = "WHERE  a.acct_type = '01' " + "AND    a.id_p_seqno = b.id_p_seqno  " + "AND  a.card_no = ? ";
		whereStr = "WHERE  a.id_p_seqno = b.id_p_seqno  " + "AND  a.card_no = ? ";

		setString(1, majorCardNo);

		int n = selectTable();
		if (n == 0) {
			showLogMessage("I", "", "select crd_card not found ,card_no[" + majorCardNo + "]");
		}
		return n;
	}

//************************************************************************
	int selectMktPbmbatm(String idNo, String comboAcctNo) throws Exception {
		extendField = "pbmbatm.";
		selectSQL = "acct_no,avg3m_amt,data_month,avg6m_amt,atm_txtimes ";
		daoTable = "MKT_PBMBATM ";
		whereStr = "where data_month = ? " + "and acct_no = ? " + "and id_no = ? ";

		setString(1, commDate.monthAdd(hBusinessDate, -1));
		setString(2, comboAcctNo);
		setString(3, idNo);

		int n = selectTable();

		return n;
	}

	/***
	 * 7.存款餘額回饋加碼 ,計算現金回饋作業
	 * 
	 * @throws Exception
	 */
	void selectMktAddonComboSum() throws Exception {
		int insertCnt = 0;
		int ii = 1;
		extendField = "q_combsum.";
//		selectSQL = "a.*,b.fund_name,b.effect_months ";
		selectSQL = "a.* ";
//		daoTable = "MKT_ADDON_COMBSUM a left join ptr_combo_fundp b on a.fund_code = b.fund_code ";
		daoTable = "MKT_ADDON_COMBSUM a " ;
		whereStr = "WHERE a.proc_date = ? and a.proc_flag = 'N' ";
		setString(ii++, hBusinessDate);
		if(!commStr.empty(fundCode)) {
			whereStr += " and a.fund_code = ? ";
			setString(ii++, fundCode);
		}
		if(!commStr.empty(parmPSeqno)) {
			whereStr += " and a.p_seqno = ? ";
			setString(ii++, parmPSeqno);
		}
		int n = selectTable();
		for (int i = 0; i < n; i++) {
			String fundCodeFlag = "";
			int x = 0;
			for(; x<parmCnt; x++){
				if(getValue("parm.fund_code",x).equals(getValue("q_combsum.fund_code",i))){
					fundCodeFlag = "Y";
					break;
				}
			}
			if(!"Y".equals(fundCodeFlag)) {
//				return;
				continue;
				
			}
			
			insertMktCashbackDtl(getValueLong("q_combsum.cashback",i),getValue("q_combsum.acct_type",i)
					,getValue("q_combsum.id_p_seqno",i),getValue("q_combsum.p_seqno",i),x);
//			insertCycFundDtl(getValueLong("q_combsum.cashback",i),x);
			updateMktAddonCombsum(i);
			insertCnt++;
//			if(insertCnt % COMMIT_CNT == 0)
//				commitDataBase();
		}
		showLogMessage("I", "", "已處理[" + insertCnt + "]筆");
	}
	
	/***
	 * 8.ATM手續費回饋加碼 ,進行加碼寫檔作業
	 * @throws Exception 
	 */
	void selectMktPbmbAtm8() throws Exception {
		int insertCnt = 0;
		int i = 1;
		for (int inti = 0; inti < parmCnt; inti++) {
			if (!"Y".equals(getValue("parm.atmibwf_cond", inti))
					&& !"Y".equals(getValue("parm.onlyaddon_calcond", inti))) {
				continue;
			}
			fetchExtend = "Q_PBMBATM.";
			sqlCmd = "SELECT DISTINCT M.ID_NO,M.ACCT_NO,M.AVG3M_AMT,M.AVG6M_AMT,M.ATM_TXTIMES,M.ATM_CBFEE,C.MAJOR_CARD_NO,C.MAJOR_ID_P_SEQNO,C.P_SEQNO,C.STMT_CYCLE,C.ACCT_TYPE,C.GROUP_CODE,M.DATA_MONTH ";
			sqlCmd += " FROM MKT_PBMBATM M,( SELECT B.ID_NO,A.COMBO_ACCT_NO,A.MAJOR_CARD_NO,A.MAJOR_ID_P_SEQNO,A.P_SEQNO,A.STMT_CYCLE,A.ACCT_TYPE,A.GROUP_CODE FROM CRD_CARD A,CRD_IDNO B ";
			sqlCmd += " WHERE A.ID_P_SEQNO = B.ID_P_SEQNO  AND A.GROUP_CODE IN (SELECT DATA_CODE FROM MKT_PARM_DATA WHERE TABLE_NAME = 'PTR_COMBO_FUNDP' and DATA_TYPE = '2' and DATA_KEY in ";
			sqlCmd += " ( select fund_code from PTR_COMBO_FUNDP WHERE apr_flag = 'Y' AND (stop_flag = 'N' OR (stop_flag = 'Y' and ? < stop_date)) and atmibwf_cond = 'Y' ";
			sqlCmd += " and fund_code = ? ";
			setString(i++, hBusinessDate);
			setString(i++, getValue("parm.fund_code", inti));

			sqlCmd += " )) AND A.SUP_FLAG = '0' AND A.CURRENT_CODE = '0' ";
			if (parmPSeqno.length() != 0) {
				sqlCmd += " and A.p_seqno = ? ";
				setString(i++, parmPSeqno);
			}
			sqlCmd += " ) C WHERE M.DATA_MONTH = ? ";
			setString(i++, commDate.monthAdd(hBusinessDate, -1));
			sqlCmd += " AND C.ID_NO= M.ID_NO AND C.COMBO_ACCT_NO = M.ACCT_NO Order by ID_NO ";

			openCursor();
			while (fetchTable()) {
				insertMktAddonAtmcbfee(getValue("parm.FUND_CODE", inti));
				insertCnt++;
//				if (insertCnt % COMMIT_CNT == 0)
//					commitDataBase();
			}
		}
		closeCursor();
		showLogMessage("I", "", "已處理[" + insertCnt + "]筆");
	}
	
	/***
	 * 9.	ATM手續費回饋加碼 ,計算現金回饋作業
	 * @throws Exception 
	 */
	void selectMktAddonCbfee() throws Exception {
		int insertCnt = 0;
		String[] parmFundCode = new String[parmCnt];
		for (int inti = 0; inti < parmCnt; inti++) {
			parmFundCode[inti] = getValue("parm.fund_code",inti);
		}
		fetchExtend = "Q_ATMCBFEE.";
		sqlCmd = "SELECT *  FROM MKT_ADDON_ATMCBFEE WHERE PROC_DATE = ? and  PROC_FLAG = 'N' ";
		setString(1,hBusinessDate);
		openCursor();
		while (fetchTable()) {
			insertMktCashbackDtl9(parmFundCode);
//			insertCycFundDtl9();
			updateMktAddonAtmcbfee();
			insertCnt ++;
//			if(insertCnt % COMMIT_CNT == 0)
//				commitDataBase();
		}
		closeCursor();
		showLogMessage("I", "", "已處理[" + insertCnt + "]筆");
	}

// ************************************************************************
//	int insertCycFundDtl(long cashback, int inti) throws Exception {
//		dateTime();
//		extendField = "fdtl.";
//		setValue("fdtl.business_date", hBusinessDate);
//		setValue("fdtl.curr_code", "901");
//		setValue("fdtl.create_date", sysDate);
//		setValue("fdtl.create_time", sysTime);
//		setValue("fdtl.id_p_seqno", getValue("id_p_seqno"));
//		setValue("fdtl.p_seqno", pSeqno);
//		setValue("fdtl.acct_type", getValue("acct_type"));
//		setValue("fdtl.card_no", majorCardNo);
//		setValue("fdtl.fund_code", getValue("parm.fund_code", inti).substring(0, 4));
//		setValue("fdtl.vouch_type", "3");
//		// '1':single record, '2':fund_code+id '3':fund_code */
//		if (cashback >= 0) {
//			setValue("fdtl.tran_code", "1");
//			setValue("fdtl.cd_kind", "H001"); // 新增
//		} else {
//			setValue("fdtl.tran_code", "7");
//			setValue("fdtl.cd_kind", "H003");
//		}
//		// 0-移轉 1-新增 2-贈與 3-調整 4-使用 5-匯入 6-移除 7-扣回
//		setValue("fdtl.memo1_type", "1");
//		/* fund_code 必須有值 */
//		setValueLong("fdtl.fund_amt", Math.abs(cashback));
//		setValueInt("fdtl.other_amt", 0);
//		setValue("fdtl.proc_flag", "N");
//		setValue("fdtl.proc_date", "");
//		setValue("fdtl.execute_date", hBusinessDate);
//		setValue("fdtl.fund_cnt", "1");
//		setValue("fdtl.mod_user", javaProgram);
//		setValue("fdtl.mod_time", sysDate + sysTime);
//		setValue("fdtl.mod_pgm", javaProgram);
//		daoTable = "cyc_fund_dtl";
//		insertTable();
//		return (0);
//
//	}

//*************************************************************************
	int insertMktAddonCombsum(int begTranAmt, int inti) throws Exception {
		dateTime();
		extendField = "a_combsum.";
		setValue("a_combsum.proc_date", hBusinessDate);
		setValue("a_combsum.cycle", "2".equals(getValue("parm.feedback_type", inti))?"C":"M");
		setValue("a_combsum.fund_code", getValue("parm.fund_code", inti));
//		setValue("a_combsum.p_seqno", getValue("p_seqno"));
		setValue("a_combsum.p_seqno", pSeqno);
		setValue("a_combsum.acct_month", commStr.left(hBusinessDate, 6));
//		setValue("a_combsum.id_p_seqno", getValue("major_id_p_seqno"));
		setValue("a_combsum.id_p_seqno", idPSeqno);
//		setValue("a_combsum.acct_type", getValue("acct_type"));
		setValue("a_combsum.acct_type", acctType);
//		setValue("a_combsum.stmt_cycle", getValue("stmt_cycle"));
		setValue("a_combsum.stmt_cycle", getValue("card.stmt_cycle"));
//		setValue("a_combsum.major_card_no", getValue("major_card_no"));
		setValue("a_combsum.major_card_no", majorCardNo);
//		setValue("a_combsum.group_code", getValue("group_code"));
		setValue("a_combsum.group_code", getValue("card.group_code"));
		setValue("a_combsum.data_month", commDate.monthAdd(hBusinessDate, -1));
		setValueInt("a_combsum.cashback", begTranAmt);
		setValue("a_combsum.proc_flag", "N");
		setValue("a_combsum.memo", String.format("(%s,%s,%s)",getValue("pbmbatm.acct_no"), getValueLong("pbmbatm.avg6m_amt"),getValueLong("pbmbatm.atm_txtimes")));
		setValue("a_combsum.mod_user", javaProgram);
		setValue("a_combsum.mod_time", sysDate+sysTime);
		setValue("a_combsum.mod_pgm", javaProgram);
		daoTable = "MKT_ADDON_COMBSUM";
		insertTable();
		return (0);
	}
	
// ************************************************************************
	int insertMktCashbackDtl(long cashback, String acctType,String idPSeqno,String pSeqno, int inti) throws Exception {
		if(cashback == 0) {
			return 0;
		}
		
		dateTime();
		extendField = "mcdl.";
		tranSeqno = comr.getSeqno("mkt_modseq");

		setValue("mcdl.tran_date", sysDate);
		setValue("mcdl.tran_time", sysTime);
		setValue("mcdl.fund_code", getValue("parm.fund_code", inti));
		setValue("mcdl.fund_name", getValue("parm.fund_name", inti));
		setValue("mcdl.p_seqno", pSeqno);
		setValue("mcdl.acct_type", acctType);
		setValue("mcdl.id_p_seqno", idPSeqno);
		if (cashback > 0) {
			setValue("mcdl.tran_code", "1");
			setValue("mcdl.mod_desc", " COMBO卡回饋刷卡金");
			setValue("mcdl.mod_memo", "");
		} else {
			setValue("mcdl.tran_code", "7");
			setValue("mcdl.mod_desc", " COMBO卡回饋刷卡金系統扣回");
			setValue("mcdl.mod_memo", "");
		}
		setValue("mcdl.tran_pgm", javaProgram);
		setValueLong("mcdl.beg_tran_amt", cashback);
		setValueLong("mcdl.end_tran_amt", cashback);
		setValueInt("mcdl.res_tran_amt", 0);
		setValueInt("mcdl.res_total_cnt", 0);
		setValueInt("mcdl.res_tran_cnt", 0);
		setValue("mcdl.res_s_month", "");
		setValue("mcdl.res_upd_date", "");
		if (cashback < 0)
			setValue("mcdl.effect_e_date", "");
		else if (getValueInt("parm.effect_months", inti) == 0)
			setValue("mcdl.effect_e_date", "");
		else
			setValue("mcdl.effect_e_date",
					commDate.dateAdd(hBusinessDate, 0, getValueInt("parm.effect_months", inti), 0));
		setValue("mcdl.tran_seqno", tranSeqno);
		setValue("mcdl.proc_month", hBusinessDate.substring(0, 6));
		setValue("mcdl.acct_date", hBusinessDate);
		setValue("mcdl.mod_reason", "");
		setValue("mcdl.case_list_flag", "N");
		setValue("mcdl.crt_user", javaProgram);
		setValue("mcdl.crt_date", sysDate);
		setValue("mcdl.apr_date", sysDate);
		setValue("mcdl.apr_user", javaProgram);
		setValue("mcdl.apr_flag", "Y");
		setValue("mcdl.mod_user", javaProgram);
		setValue("mcdl.mod_time", sysDate + sysTime);
		setValue("mcdl.mod_pgm", javaProgram);
		daoTable = "mkt_cashback_dtl";
		insertTable();

		return (0);
	}

// ************************************************************************
	int insertMktAddonAtmcbfee(String fundCode) throws Exception {
		dateTime();
		int discountNum = getDiscountNum(getValueLong("Q_PBMBATM.AVG3M_AMT"));
		int cashback = discountNum > getValueInt("Q_PBMBATM.ATM_TXTIMES") ? getValueInt("Q_PBMBATM.ATM_CBFEE") * getValueInt("Q_PBMBATM.ATM_TXTIMES") : discountNum * getValueInt("Q_PBMBATM.ATM_CBFEE"); 
		String cycle = "2".equals(getValue("parm.feedback_type",inti))?"C":"M";
		extendField = "cbdtl.";
		setValue("cbdtl.PROC_DATE", hBusinessDate);
		setValue("cbdtl.CYCLE",cycle);
		setValue("cbdtl.FUND_CODE", fundCode);
		setValue("cbdtl.P_SEQNO", getValue("Q_PBMBATM.P_SEQNO"));
		setValue("cbdtl.ACCT_MONTH", commStr.left(hBusinessDate, 6));
		setValue("cbdtl.ID_P_SEQNO", getValue("Q_PBMBATM.MAJOR_ID_P_SEQNO"));
		setValue("cbdtl.ACCT_TYPE", getValue("Q_PBMBATM.ACCT_TYPE"));
		setValue("cbdtl.STMT_CYCLE", getValue("Q_PBMBATM.STMT_CYCLE"));
		setValue("cbdtl.MAJOR_CARD_NO", getValue("Q_PBMBATM.MAJOR_CARD_NO"));
		setValue("cbdtl.GROUPCODES", getValue("Q_PBMBATM.GROUP_CODE"));
		setValue("cbdtl.DATA_MONTH", getValue("Q_PBMBATM.DATA_MONTH"));
		setValueInt("cbdtl.CASHBACK", cashback);
		setValue("cbdtl.PROC_FLAG", "N");
		setValue("cbdtl.MEMO", String.format("(%s,%s,%s)", getValue("Q_PBMBATM.ACCT_NO"),getValue("Q_PBMBATM.AVG6M_AMT"),getValue("Q_PBMBATM.ATM_TXTIMES")));
		setValue("cbdtl.MOD_USER", javaProgram);
		setValue("cbdtl.MOD_TIME", sysDate + sysTime);
		setValue("cbdtl.MOD_PGM", javaProgram);

		daoTable = "MKT_ADDON_ATMCBFEE";
		insertTable();
		if ("Y".equals(dupRecord)) {
			daoTable = "MKT_ADDON_ATMCBFEE";
			updateSQL = " PROC_DATE     = ? ";
			updateSQL += ",ID_P_SEQNO     = ? ";
			updateSQL += ",ACCT_TYPE     = ? ";
			updateSQL += ",STMT_CYCLE     = ? ";
			updateSQL += ",MAJOR_CARD_NO     = ? ";
			updateSQL += ",GROUPCODES     = GROUPCODES || ? ";
			updateSQL += ",DATA_MONTH     = ? ";
			updateSQL += ",CASHBACK     = CASHBACK + ? ";
			updateSQL += ",PROC_FLAG     = ? ";
			updateSQL += ",MEMO     = MEMO || ? ";
			updateSQL += ",MOD_USER     = ? ";
			updateSQL += ",MOD_TIME     = sysdate ";
			updateSQL += ",MOD_PGM     = ? ";
			whereStr = "where PROC_DATE = ? " + " and CYCLE = ?" + " and FUND_CODE = ? " + " and P_SEQNO = ? " + " and ACCT_MONTH = ? and PROC_FLAG = ? " ;

			setString(1, hBusinessDate);
			setString(2, getValue("Q_PBMBATM.ID_P_SEQNO"));
			setString(3, getValue("Q_PBMBATM.ACCT_TYPE"));
			setString(4, getValue("Q_PBMBATM.STMT_CYCLE"));
			setString(5, getValue("Q_PBMBATM.MAJOR_CARD_NO"));
			setString(6, getValue("Q_PBMBATM.GROUP_CODE"));
			setString(7, getValue("Q_PBMBATM.DATA_MONTH"));
			setInt(8, cashback);
			setString(9, "N");
			setString(10, String.format("(%s,%s,%s)", getValue("Q_PBMBATM.ACCT_NO"),getValueLong("Q_PBMBATM.AVG6M_AMT"),getValueInt("Q_PBMBATM.ATM_TXTIMES")));
			setString(11, javaProgram);
			setString(12, javaProgram);
			setString(13, hBusinessDate);
			setString(14, cycle);
			setString(15, fundCode);
			setString(16, getValue("Q_PBMBATM.P_SEQNO"));
			setString(17, commStr.left(hBusinessDate, 6));
			setString(18, "N");
			updateTable();
		}
		return (0);
	}

//************************************************************************
	int insertMktCashbackDtl9(String[] parmFundCode) throws Exception {
		if(getValueInt("Q_ATMCBFEE.cashback") == 0) {
			return 0;
		}

		tranSeqno = comr.getSeqno("mkt_modseq");
		int parmIndex = Arrays.asList(parmFundCode).indexOf(getValue("Q_ATMCBFEE.fund_code"));
		int effectMonths = parmIndex >= 0 ? getValueInt("parm.effect_months",parmIndex) : 0;
		dateTime();
		extendField = "mcdl9.";
		setValue("mcdl9.tran_date", sysDate);
		setValue("mcdl9.tran_time", sysTime);
		setValue("mcdl9.fund_code", getValue("Q_ATMCBFEE.fund_code"));
		setValue("mcdl9.fund_name", parmIndex >= 0 ? getValue("parm.fund_name",parmIndex) : "");
		setValue("mcdl9.p_seqno", getValue("Q_ATMCBFEE.p_seqno"));
		setValue("mcdl9.acct_type", getValue("Q_ATMCBFEE.acct_type"));
		setValue("mcdl9.id_p_seqno", getValue("Q_ATMCBFEE.id_p_seqno"));
		if (getValueInt("Q_ATMCBFEE.cashback") > 0) {
			setValue("mcdl9.tran_code", "2");
			setValue("mcdl9.mod_desc", "回饋加碼ATM跨行手續費刷卡金");
			setValue("mcdl9.mod_memo", "");
		} else {
			setValue("mcdl9.tran_code", "7");
			setValue("mcdl9.mod_desc", "回饋加碼ATM跨行手續費刷卡金系統扣回");
			setValue("mcdl9.mod_memo", "");
		}
		setValue("mcdl9.tran_pgm", javaProgram);
		setValueInt("mcdl9.beg_tran_amt", getValueInt("Q_ATMCBFEE.cashback"));
		setValueInt("mcdl9.end_tran_amt", getValueInt("Q_ATMCBFEE.cashback"));
		setValueInt("mcdl9.res_tran_amt", 0);
		setValueInt("mcdl9.res_total_cnt", 0);
		setValueInt("mcdl9.res_tran_cnt", 0);
		setValue("mcdl9.res_s_month", "");
		setValue("mcdl9.res_upd_date", "");
		setValueInt("mcdl9.effect_months", effectMonths);
		if (getValueInt("CASHBACK") < 0)
			setValue("mcdl9.effect_e_date", "");
		else if (effectMonths == 0)
			setValue("mcdl9.effect_e_date", "");
		else
			setValue("mcdl9.effect_e_date",
					commDate.dateAdd(hBusinessDate, 0, effectMonths, 0));
		setValue("mcdl9.tran_seqno", tranSeqno);
		setValue("mcdl9.proc_month", hBusinessDate.substring(0, 6));
		setValue("mcdl9.acct_date", hBusinessDate);
		setValue("mcdl9.mod_reason", "");
		setValue("mcdl9.case_list_flag", "N");
		setValue("mcdl9.crt_user", javaProgram);
		setValue("mcdl9.crt_date", sysDate);
		setValue("mcdl9.apr_date", sysDate);
		setValue("mcdl9.apr_user", javaProgram);
		setValue("mcdl9.apr_flag", "Y");
		setValue("mcdl9.mod_user", javaProgram);
		setValue("mcdl9.mod_time", sysDate + sysTime);
		setValue("mcdl9.mod_pgm", javaProgram);
		daoTable = "mkt_cashback_dtl";
		insertTable();
		return (0);

	}

//************************************************************************
	int insertCycFundDtl9() throws Exception {
		dateTime();
		extendField = "fdtl9.";
		setValue("fdtl9.business_date", hBusinessDate);
		setValue("fdtl9.curr_code", "901");
		setValue("fdtl9.create_date", sysDate);
		setValue("fdtl9.create_time", sysTime);
		setValue("fdtl9.id_p_seqno", getValue("Q_ATMCBFEE.id_p_seqno"));
		setValue("fdtl9.p_seqno", getValue("Q_ATMCBFEE.p_seqno"));
		setValue("fdtl9.acct_type", getValue("Q_ATMCBFEE.acct_type"));
		setValue("fdtl9.card_no", getValue("Q_ATMCBFEE.major_card_no"));
		setValue("fdtl9.fund_code", commStr.left(getValue("Q_ATMCBFEE.fund_code"), 4));
		setValue("fdtl9.vouch_type", "3");
		// '1':single record, '2':fund_code+id '3':fund_code */
		if (getValueInt("Q_ATMCBFEE.cashback") >= 0) {
			setValue("fdtl9.tran_code", "2");// 贈與
			setValue("fdtl9.cd_kind", "H001");
		} else {
			setValue("fdtl9.tran_code", "7");
			setValue("fdtl9.cd_kind", "H003");
		}
		// 0-移轉 1-新增 2-贈與 3-調整 4-使用 5-匯入 6-移除 7-扣回
		setValue("fdtl9.memo1_type", "1");
		/* fund_code 必須有值 */
		setValueInt("fdtl9.fund_amt", Math.abs(getValueInt("Q_ATMCBFEE.cashback")));
		setValueInt("fdtl9.other_amt", 0);
		setValue("fdtl9.proc_flag", "N");
		setValue("fdtl9.proc_date", "");
		setValue("fdtl9.execute_date", hBusinessDate);
		setValue("fdtl9.fund_cnt", "1");
		setValue("fdtl9.mod_user", javaProgram);
		setValue("fdtl9.mod_time", sysDate + sysTime);
		setValue("fdtl9.mod_pgm", javaProgram);
		daoTable = "cyc_fund_dtl";
		insertTable();
		return (0);

	}
	
	void updateMktAddonAtmcbfee() throws Exception {
		daoTable = "MKT_ADDON_ATMCBFEE";
		updateSQL = " PROC_FLAG     = ? ";
		updateSQL += ",MOD_USER     = ? ";
		updateSQL += ",MOD_TIME     = sysdate ";
		updateSQL += ",MOD_PGM     = ? ";
		whereStr = "where PROC_DATE     = ? " + "and   CYCLE = ? " 
		+ "and  FUND_CODE = ? and P_SEQNO = ? and ACCT_MONTH = ? ";

		setString(1, "Y");
		setString(2, javaProgram);
		setString(3, javaProgram);
		setString(4, getValue("Q_ATMCBFEE.PROC_DATE"));
		setString(5, getValue("Q_ATMCBFEE.CYCLE"));
		setString(6, getValue("Q_ATMCBFEE.FUND_CODE"));
		setString(7, getValue("Q_ATMCBFEE.P_SEQNO"));
		setString(8, getValue("Q_ATMCBFEE.ACCT_MONTH"));
		updateTable();
	}
	
	void updateMktAddonCombsum(int inti) throws Exception {
		daoTable = "MKT_ADDON_COMBSUM";
		updateSQL = " PROC_FLAG     = ? ";
		updateSQL += ",MOD_USER     = ? ";
		updateSQL += ",MOD_TIME     = sysdate ";
		updateSQL += ",MOD_PGM     = ? ";
		whereStr = "where PROC_DATE     = ? " + "and   CYCLE = ? " 
		+ "and  FUND_CODE = ? and P_SEQNO = ? and ACCT_MONTH = ? and MAJOR_CARD_NO = ? ";
		setString(1, "Y");
		setString(2, javaProgram);
		setString(3, javaProgram);
		setString(4, getValue("q_combsum.PROC_DATE", inti));
		setString(5, getValue("q_combsum.CYCLE", inti));
		setString(6, getValue("q_combsum.FUND_CODE", inti));
		setString(7, getValue("q_combsum.P_SEQNO", inti));
		setString(8, getValue("q_combsum.ACCT_MONTH", inti));
		setString(9, getValue("q_combsum.MAJOR_CARD_NO", inti));
		updateTable();
	}

//************************************************************************
	int procCashAmt(double[] parmAmt , int inti ) throws Exception {

		int cnt = selectCrdCard( majorCardNo );
		if (cnt <= 0) {
			showLogMessage("I","","Warm :[Card No="+ majorCardNo + "is not found in crd_card ]");
			return 0;
		}
		selectMktPbmbatm(getValue("card.id_no"), getValue("card.combo_acct_no"));
		if (getValue("card.combo_acct_no").trim().length()==0) {
 	        showLogMessage("I","","Warm :[Card No="+ majorCardNo + "combo_acct_no is empty ]");
 	        return 0; 
		}   
		double avg3mAmt = getValueDouble("pbmbatm.avg3m_amt");			
//		for (int inti = 0; inti < parmCnt; inti++) {
//			if (parmAmt[inti] == 0)
//				continue;

			double exchgRate = 0.1;
			exchgRate = getBegTranAmtRate(avg3mAmt,inti);	
			
            //無條件捨去小數
			int begTranAmtTmp = (int) Math.floor( parmAmt[inti] * exchgRate / 100);

			if (getValueInt("parm.feedback_lmt", inti) != 0)
				if (Math.abs(begTranAmtTmp) > getValueInt("parm.feedback_lmt", inti)) {
					if (begTranAmtTmp < 0) {
						begTranAmtTmp = -Math.abs(getValueInt("parm.feedback_lmt", inti));
					} else {
						begTranAmtTmp = Math.abs(getValueInt("parm.feedback_lmt", inti));
					}
				}
			if (begTranAmtTmp != 0) {
				insertMktAddonCombsum(begTranAmtTmp , inti);
			}
//		}
		return (0);
	}

// ************************************************************************
	void loadMktMchtgpData() throws Exception {
		extendField = "mcht.";
		selectSQL = "b.data_key," + "b.data_type," + "a.data_code," + "a.data_code2 ";
		daoTable = "mkt_mchtgp_data a , MKT_PARM_DATA b";
		whereStr = " WHERE a.TABLE_NAME = 'MKT_MCHT_GP' " + " and b.TABLE_NAME = 'PTR_COMBO_FUNDP' "
				+ "and   a.data_key   = b.data_code " + "and   a.data_type  = '1' "
				+ "and  b.data_type in ('6','P') " + "and  b.data_key in ("
				+ commStr.left(fundCodeBuf.toString(), fundCodeBuf.length() - 1) + ") ";

		whereStr = whereStr + "order by b.data_key,b.data_type";

		int n = loadTable();

		setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

		showLogMessage("I", "", "Load mkt_mchtgp_data Count: [" + n + "]");
	}

	void loadMktParmCdata() throws Exception {
		extendField = "datc.";
		selectSQL = "data_key," + "data_type," + "data_code";
		daoTable = "mkt_parm_cdata";
		whereStr = "WHERE TABLE_NAME = 'PTR_COMBO_FUNDP'" + " and data_type in ('A','B') " + "and data_key in ("
				+ commStr.left(fundCodeBuf.toString(), fundCodeBuf.length() - 1) + ") "
				// 例: 'L010000001' , ' L010000002', ' L010000003'
				+ "order by data_key,data_type,data_code ";

		int n = loadTable();

		setLoadData("datc.data_key,datc.data_type");

		showLogMessage("I", "", "Load mkt_parm_cdata Count: [" + n + "]");
	}

	// ************************************************************************
	int selectMktParmCdata(String cnameSel, String enameSel) throws Exception {
		if (cnameSel.equals("0") && enameSel.equals("0"))
			return (0);

		int okFlag = 0;
		int cnt1 = 0;
		// 特店中文名稱條件
		if ((!cnameSel.equals("0")) && (getValue("mcht_chi_name").length() != 0)) { // 特店中文名稱

			setValue("datc.data_key", getValue("data_key"));
			setValue("datc.data_type", "A");
			cnt1 = getLoadData("datc.data_key,datc.data_type");
		}

		for (int inti = 0; inti < cnt1; inti++) {
			int indexInt = getValue("mcht_chi_name").indexOf(getValue("datc.data_code", inti));
			if (indexInt != -1) {
				if (cnameSel.equals("1")) { // 指定
					okFlag = 1; // 指定 ok , 1st round pass  ok
				} else {// 排除
					okFlag = 2; // 排除則不再判斷
					break;
				}
			}
		}

		if (okFlag != 0) {
			return (0);
		}
		// 特店英文名稱條件
		if ((!enameSel.equals("0")) && (getValue("mcht_eng_name").length() != 0)) {// 特店英文名稱
			setValue("datc.data_key", getValue("data_key"));
			setValue("datc.data_type", "B");
			cnt1 = getLoadData("datc.data_key,datc.data_type");
		}

		for (int inti = 0; inti < cnt1; inti++) {
			int indexInt = getValue("mcht_eng_name").toUpperCase(Locale.TAIWAN)
					.indexOf(getValue("datc.data_code", inti).toUpperCase(Locale.TAIWAN));
			if (indexInt != -1) {
				if (enameSel.equals("1")) { // 指定
					okFlag = 1; // 指定 ok , 1st round pass  ok
				} else {// 排除
					okFlag = 2; // 排除則不再判斷
					break;
				}
			}
		}
		if (okFlag != 0) {
			return (0);
		}
		return (1);
	}

	// ************************************************************************
	int selectMktMchtgpData(String col1, String col2, String sel, String dataType) throws Exception {
		if (sel.equals("0"))
			return (0);

		setValue("mcht.data_key", getValue("data_key"));
		setValue("mcht.data_type", dataType);
		setValue("mcht.data_code", col1);

		int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
		int okFlag = 0;
		for (int inti = 0; inti < cnt1; inti++) {
			if ("P".equals(dataType)) {
				okFlag = 1;
				break;
			} else {
				if ((getValue("mcht.data_code2", inti).length() == 0)
						|| ((getValue("mcht.data_code2", inti).length() != 0)
								&& (getValue("mcht.data_code2", inti).equals(col2)))) {
					okFlag = 1;
					break;
				}
			}

		}

		if (sel.equals("1")) {
			if (okFlag == 0)
				return (1);
			return (0);
		} else {
			if (okFlag == 0)
				return (0);
			return (1);
		}
	}

	// ************************************************************************
	int selectMktParmData(String col1, String sel, String dataType, int dataNum) throws Exception {
		return selectMktParmData(col1, "", "", sel, dataType, dataNum);
	}

	// ************************************************************************
	int selectMktParmData(String col1, String col2, String sel, String dataType, int dataNum) throws Exception {
		return selectMktParmData(col1, col2, "", sel, dataType, dataNum);
	}

	// ************************************************************************
	int selectMktParmData(String col1, String col2, String col3, String sel, String dataType, int dataNum)
			throws Exception {
		if (sel.equals("0"))
			return (0);

		setValue("data.data_key", getValue("data_key"));
		setValue("data.data_type", dataType);

		int cnt1 = 0;
		if (dataNum == 2) {
			cnt1 = getLoadData("data.data_key,data.data_type");
		} else {
			setValue("data.data_code", col1);
			cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
		}

		int okFlag = 0;
		for (int intm = 0; intm < cnt1; intm++) {
			if (dataNum == 2) {
				if ((col1.length() != 0) && (getValue("data.data_code", intm).length() != 0)
						&& (!getValue("data.data_code", intm).equals(col1)))
					continue;

				if ((col2.length() != 0) && (getValue("data.data_code2", intm).length() != 0)
						&& (!getValue("data.data_code2", intm).equals(col2)))
					continue;

				if ((col3.length() != 0) && (getValue("data.data_code3", intm).length() != 0)
						&& (!getValue("data.data_code3", intm).equals(col3)))
					continue;
			} else {
				if (col2.length() != 0) {
					if ((getValue("data.data_code2", intm).length() != 0)
							&& (!getValue("data.data_code2", intm).equals(col2)))
						continue;
				}
				if (col3.length() != 0) {
					if ((getValue("data.data_code3", intm).length() != 0)
							&& (!getValue("data.data_code3", intm).equals(col3)))
						continue;
				}
			}

			okFlag = 1;
			break;
		}

		if (sel.equals("1")) {
			if (okFlag == 0)
				return (1);
			return (0);
		} else {
			if (okFlag == 0)
				return (0);
			return (1);
		}
	}

	// ************************************************************************
	void loadMktParmData() throws Exception {
		extendField = "data.";
		selectSQL = "data_key," + "data_type," + "data_code," + "data_code2";
		daoTable = "MKT_PARM_DATA";
		whereStr = "WHERE TABLE_NAME = 'PTR_COMBO_FUNDP' " + "and data_key in ("
				+ commStr.left(fundCodeBuf.toString(), fundCodeBuf.length() - 1) + ") ";

		whereStr = whereStr + "order by data_key,data_type,data_code,data_code2";

		int n = loadTable();
		setLoadData("data.data_key,data.data_type,data.data_code");
		setLoadData("data.data_key,data.data_type");

		showLogMessage("I", "", "Load MKT_PARM_DATA Count: [" + n + "]");
	}

// ************************************************************************
	double commAmt(double val, double rnd, int n) throws Exception {
		if (n == 1)
			return new BigDecimal(val).add(BigDecimal.valueOf(rnd)).doubleValue();
		if (n == 2)
			return new BigDecimal(val).multiply(BigDecimal.valueOf(rnd)).setScale(0, BigDecimal.ROUND_DOWN)
					.doubleValue();

		return 0;
	}

// ************************************************************************
	void loadPtrCurrcode() throws Exception {
		extendField = "pcde.";
		selectSQL = "curr_code," + "curr_amt_dp";
		daoTable = "ptr_currcode";
		whereStr = "where bill_sort_seq!=''";

		int n = loadTable();

		setLoadData("pcde.curr_code");

		showLogMessage("I", "", "Load ptr_currcode Count: [" + n + "]");
	}
	
	void deleteTmpData() throws Exception {
		for (int inti = 0; inti < parmCnt; inti++) {
			deleteMktAddonAtmcbfee(getValue("parm.fund_code", inti));
			deleteMktAddonCombsum(getValue("parm.fund_code", inti));
		}
	}
	
	// ************************************************************************
	void deleteMktAddonAtmcbfee(String fundCode) throws Exception {
		daoTable = "MKT_ADDON_ATMCBFEE";
		whereStr = "WHERE proc_flag = 'N' and PROC_DATE = ? and cycle = ? and fund_code = ? ";
		if(parmPSeqno.length()>0) {
			whereStr += " and p_seqno = ? and proc_flag = 'N' ";
			setString(4, parmPSeqno);
		}
		setString(1, hBusinessDate);
		setString(2, commStr.right(hBusinessDate, 2).equals(getValue("wday.stmt_cycle"))?"C":"M");
		setString(3, fundCode);
		deleteTable();

		return;
	}
	
	// ************************************************************************
	void deleteMktAddonCombsum(String fundCode) throws Exception {
		daoTable = "MKT_ADDON_COMBSUM";
		whereStr = "WHERE PROC_DATE = ? and cycle = ? and fund_code = ? and proc_flag = 'N' ";
		if(parmPSeqno.length()>0) {
			whereStr += " and p_seqno = ? ";
			setString(4, parmPSeqno);
		}
		setString(1, hBusinessDate);
		setString(2, commStr.right(hBusinessDate, 2).equals(getValue("wday.stmt_cycle"))?"C":"M");
		setString(3, fundCode);
		deleteTable();

		return;
	}
	
	// ************************************************************************
	void deleteMktAddonAtmcbfeeHis() throws Exception {
		daoTable = "MKT_ADDON_ATMCBFEE";
		whereStr = "WHERE PROC_DATE < ?  ";

		setString(1, comm.nextMonthDate(hBusinessDate,-6));		
		int n = deleteTable();
		if (n>0) 
			showLogMessage("I","","MKT_ADDON_ATMCBFEE 歷史資料 [" + n + "] records");		

		return;
	}
	
	// ************************************************************************
	void deleteMktAddonCombsumHis() throws Exception {
		daoTable = "MKT_ADDON_COMBSUM";
		whereStr = "WHERE PROC_DATE < ?  ";

		setString(1, comm.nextMonthDate(hBusinessDate,-6));		
		int n = deleteTable();
		if (n>0) 
			showLogMessage("I","","MKT_ADDON_COMBSUM 歷史資料 [" + n + "] records");		

		return;
	}	
// ************************************************************************
	int getDiscountNum(long avg3mAmt) {
		
		int discountNum = 0;
		if (avg3mAmt > 50000) {
			discountNum = 4;
		}
		if (avg3mAmt > 200000) {
			discountNum = 6;
		}
		if (avg3mAmt > 2000000) {
			discountNum = 10;
		}
		return discountNum;
	}

} // End of class FetchSample

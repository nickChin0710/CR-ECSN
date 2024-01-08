/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 111-12-20  V1.00.00     Ryan     initial                                            *
* 112-05-12  V1.00.01     Ryan     差別利率調整,新增UP_DOWN_FLAG欄位                                                                     *
* 112-11-13  V1.00.02     Ryan     調整持卡年限欄位                                                                                                              *
 **************************************************************************************/

package Cyc;

import java.util.HashMap;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommDate;


public class CycR150 extends AccessDAO {
	private final String progname = "每季產生第二段差別利率處理程式 112/11/13 V.00.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	CommString comStr = new CommString();
	String modUser = "";
	String modPgm = "";
	String modTime = "";
	int totalCnt = 0;
	int readCnt = 0;
	StringBuffer  procDates  = new StringBuffer();
	HashMap<String,Integer> ptrRcrateMap = new  HashMap<String,Integer>();
	HashMap<Integer,Double> ratingMap = new  HashMap<Integer,Double>();
	
//	private String hCardSince = "";
	private String oriIssueDate = "";
	private String hRunMonth = "";
	private int hRunDay = 0;
	private int hPenaltyMonth = 0;
	private int hPenaltyMonth2 = 0;
	private int hUseRcmonth = 0;
	private String skipCntFlag = ""; 
	
	/**********TABLE : CYC_DIFF_RCRATE************/
	private String hIsStaff = "";
	private int hFlowCount = 0;
	private String hIsConform = "";
	private String hIsStatus = "";
	private String hIsBlock = ""; 
	private String hCreditRating = "";
	private double hCardDuration = 0;
	private int hPenaltyCount3 = 0;
	private int hPenaltyCount6 = 0;
	private String hIsRc = "";
	private double hRcRateOri = 0;
	private String hUpDown = "";
	private double hRcRateBefroe = 0;
	private double hRcRateAfter = 0;
	private double hRevolveIntRate = 0;
	private String hRevolveRateSMonth = "";
	private String hRevolveRateEMonth = "";
	private String hAcctMonth = "";
	private String hUpDownFlag = "";
	
	/**********TABLE : ACT_ACNO************/
	private String hIdPSeqno = "";
	private String hAcnoPSeqno = "";
	private String hAcctStatus = "";
	

	public int mainProcess(String[] args) {

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			showLogMessage("I", "","-->connect DB: " + getDBalias()[0]);

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			modPgm = javaProgram;
			modTime = sysDate + sysTime;
			comr = new CommRoutine(getDBconnect(), getDBalias());
			getBusiDate();
			if(args.length==1&&args[0].length()==8) {
				businessDate = args[0];
			}
			selectCycRcrateParm();
			if(comStr.pos(procDates.toString(),businessDate)<0) {
				showLogMessage("I", "", String.format("營業日[%s],非執行日",businessDate));
				return(0);
			}
			getPtrRcrateMap();
			selectActAcno();
			commitDataBase();
			showLogMessage("I", "", String.format("程式處理結果 ,筆數 = %s", totalCnt));
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
	
	
	void selectActAcno() throws Exception {
		sqlCmd = " select id_p_seqno,acno_p_seqno,acct_status,revolve_int_rate  ";
		sqlCmd += " from act_acno  ";
		sqlCmd += " where acct_type = '01' ";
		this.openCursor();

		while (fetchTable()) {
			readCnt++;
			initData();
			hIdPSeqno = getValue("id_p_seqno");
			hAcnoPSeqno = getValue("acno_p_seqno");
			hAcctStatus = getValue("acct_status");
			hRcRateOri = getValueDouble("revolve_int_rate");
			procData();
			
		}
		this.closeCursor();
	}

	void insertCycDiffRcrate() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
			daoTable = "cyc_diff_rcrate";
			setValue("acct_month", hAcctMonth);
			setValue("acno_p_seqno", hAcnoPSeqno);
			setValue("acct_type", "01");
			setValue("is_staff", hIsStaff);
			setValueInt("flow_count", hFlowCount);
			setValue("is_status", hIsStatus);
			setValue("is_block", hIsBlock);
			setValue("is_conform", hIsConform);
			setValue("credit_rating", hCreditRating);
			setValueDouble("card_duration", hCardDuration);
			setValueInt("penalty_count", hPenaltyCount3);
			setValueInt("penalty_count2", hPenaltyCount6);
			setValue("is_rc", hIsRc);
			setValueDouble("rc_rate_ori", hRcRateOri);
			setValue("up_down", hUpDown);
			setValueDouble("rc_rate_befroe", hRcRateBefroe);
			setValueDouble("rc_rate_after", hRcRateAfter);
			setValueDouble("revolve_int_rate", hRevolveIntRate);
			setValue("revolve_rate_s_month", hRevolveRateSMonth);
			setValue("revolve_rate_e_month", hRevolveRateEMonth);
			setValue("diff_step", "10");
			setValue("mod_time", modTime);
			setValue("mod_pgm", modPgm);
			setValue("up_down_flag",hUpDownFlag);
			try {
				insertTable();
				if(dupRecord.equals("Y")) {
//					showLogMessage("I", "", "insert hce_apply_data duplicate");
//					return;
					int i=1;
					daoTable = "cyc_diff_rcrate";
					updateSQL+= " acct_type = '01' "; 
					updateSQL+= " ,is_staff = ? "; 
					updateSQL+= " ,flow_count = ? "; 
					updateSQL+= " ,is_status = ? "; 
					updateSQL+= " ,is_block = ? "; 
					updateSQL+= " ,is_conform = ? "; 
					updateSQL+= " ,credit_rating = ? "; 
					updateSQL+= " ,card_duration = ? "; 
					updateSQL+= " ,penalty_count = ? "; 
					updateSQL+= " ,penalty_count2 = ? "; 
					updateSQL+= " ,is_rc = ? "; 
					updateSQL+= " ,rc_rate_ori = ? "; 
					updateSQL+= " ,up_down = ? "; 
					updateSQL+= " ,rc_rate_befroe = ? "; 
					updateSQL+= " ,rc_rate_after = ? "; 
					updateSQL+= " ,revolve_int_rate = ? "; 
					updateSQL+= " ,revolve_rate_s_month = ? "; 
					updateSQL+= " ,revolve_rate_e_month = ? "; 
					updateSQL+= " ,diff_step = '10' "; 
					updateSQL+= " ,mod_time = sysdate "; 
					updateSQL+= " ,mod_pgm = ? "; 
					updateSQL+= " ,up_down_flag = ? "; 
					whereStr = " where acct_month = ? and acno_p_seqno = ?";
					setString(i++, hIsStaff);
					setInt(i++, hFlowCount);
					setString(i++, hIsStatus);
					setString(i++, hIsBlock);
					setString(i++, hIsConform);
					setString(i++, hCreditRating);
					setDouble(i++, hCardDuration);
					setInt(i++, hPenaltyCount3);
					setInt(i++, hPenaltyCount6);
					setString(i++, hIsRc);
					setDouble(i++, hRcRateOri);
					setString(i++, hUpDown);
					setDouble(i++, hRcRateBefroe);
					setDouble(i++, hRcRateAfter);
					setDouble(i++, hRevolveIntRate);
					setString(i++, hRevolveRateSMonth);
					setString(i++, hRevolveRateEMonth);
					setString(i++, modPgm);
					setString(i++, hUpDownFlag);
					setString(i++, hAcctMonth);
					setString(i++, hAcnoPSeqno);
					updateTable();
					if(notFound.equals("Y")) {
						showLogMessage("E", "", "update cyc_diff_rcrate not fuond ");
						return;
					}
				}
			} catch (Exception ex) {
				showLogMessage("E", "", "insert cyc_diff_rcrate error ," + ex.getMessage());
				return;
			}
		totalCnt ++;
	}

	
	//A.是否行員:
	private void selectCrdIdno() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
		sqlCmd = " select staff_flag,credit_level_old,card_since from crd_idno where 1=1 and id_p_seqno = ? ";
		setString(1, hIdPSeqno);
		selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("select crd_idno not found ,id_p_seqno = [%s]",hIdPSeqno));
			skipCntFlag = "Y";
			return;
		}
		hIsStaff = getValue("staff_flag");
		hCreditRating = getValue("credit_level_old");
//		hCardSince = getValue("card_since");
		if(hIsStaff.equals("Y")) {
			hIsConform = "N";
			skipCntFlag = "Y";
		}
	}
	
	//B.目前流通卡數
	private void selectCrdCard() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
		sqlCmd = " select count(*) card_cnt,min(ori_issue_date) as ori_issue_date from crd_card where 1=1 and acno_p_seqno = ? and current_code = '0'";
		setString(1, hAcnoPSeqno);
		selectTable();
		hFlowCount = getValueInt("card_cnt");
		oriIssueDate = getValue("ori_issue_date");
		
		if(hFlowCount==0) {
			hIsConform = "N";
			skipCntFlag = "Y";
		}
	}
	
	//C.帳戶往來狀態是否符合
	private void getIsStatus() {
		if(skipCntFlag.equals("Y"))
			return;
		if(comStr.pos(",3,4,5", hAcctStatus)>0) {
			hIsStatus = "N";
			hIsConform = "N";
			skipCntFlag = "Y";
		}
	}
	
	//D.帳戶是否凍結
	private void selectCcaCardAcct() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
		sqlCmd = " select block_reason1,block_reason2,block_reason3,block_reason4,block_reason5 from cca_card_acct where 1=1 and acno_p_seqno = ? ";
		setString(1, hAcnoPSeqno);
		selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("select cca_card_acct not found ,acno_p_seqno = [%s]",hAcnoPSeqno));
			skipCntFlag = "Y";
			return;
		}
		String blockReason1 = getValue("block_reason1");
		String blockReason2 = getValue("block_reason2");
		String blockReason3 = getValue("block_reason3");
		String blockReason4 = getValue("block_reason4");
		String blockReason5 = getValue("block_reason5");
		if(!comStr.empty(blockReason1 + blockReason2 + blockReason3 + blockReason4 + blockReason5)) {
			hIsBlock = "Y";
			hIsConform = "N";
			skipCntFlag = "Y";
		}
	}
	
	//持卡年限
	private void getCardDuration(){
		if(skipCntFlag.equals("Y"))
			return;
		double years = 0;
		double days = 0;
		if(!comStr.empty(oriIssueDate)&&commDate.isDate(oriIssueDate))
			days = commDate.daysBetween(oriIssueDate, sysDate);
		if(days>0) {
			years = comStr.numScale(days/365,2);
		}
		hCardDuration = years;
	}
	
	//利率差異化明細檔cycm0200
	private void selectCycRcrateParm() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
		sqlCmd = " select run_month,run_day,penalty_month,penalty_month2,use_rcmonth from cyc_rcrate_parm where 1=1 ";
		selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select cyc_rcrate_parm not found!");
			skipCntFlag = "Y";
			return;
		}
		hPenaltyMonth = getValueInt("penalty_month");
		hPenaltyMonth2 = getValueInt("penalty_month2");
		hUseRcmonth = getValueInt("use_rcmonth");
		hRunMonth = getValue("run_month");
		hRunDay = getValueInt("run_day");

		String sysDateY = comStr.left(sysDate,4);
		for(int i = 0 ;i<12 ;i++) {
			char ch = hRunMonth.charAt(i);
			String charStr = String.valueOf(ch);
			if(charStr.equals("Y")) {
				procDates.append(",");
				procDates.append(sysDateY);
				procDates.append(String.format("%02d", i+1));
				procDates.append(String.format("%02d", hRunDay));
			}
		}
	}
	
	//違約情形-最近(3-參數)個月
	//違約情形-最近(6-參數)個月
	private void selectActPenaltyLog() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
		sqlCmd = " select (select count(*)  from act_penalty_log where 1=1 and p_seqno = ? and months_between(to_date(acct_month,'yyyymm'),to_date(business_date,'yyyymm')) <= ?) as penalty_Count3 ";
		sqlCmd += ",(select count(*)  from act_penalty_log where 1=1 and p_seqno = ? and months_between(to_date(acct_month,'yyyymm'),to_date(business_date,'yyyymm')) <= ? ) as penalty_Count6 ";
	    sqlCmd += " from ptr_businday ";
	    setString(1,hAcnoPSeqno);
	    setInt(2,hPenaltyMonth);
	    setString(3,hAcnoPSeqno);
		setInt(4,hPenaltyMonth2);
		selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("select cyc_rcrate_parm not found ,p_seqno=[%s] ,hPenaltyMonth=[%s] ,hPenaltyMonth2=[%s]",hAcnoPSeqno,hPenaltyMonth,hPenaltyMonth2));
			skipCntFlag = "Y";
			return;
		}
		hPenaltyCount3 =  getValueInt("penalty_Count3");
		hPenaltyCount6 =  getValueInt("penalty_Count6");
	}
	
	//是否動用循環息
	private void selectActIntr() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
		sqlCmd = " select nvl(sum(INTEREST_AMT),0) as sum_interest_amt from act_intr where 1=1 and p_seqno = ? ";
		sqlCmd += " and months_between(to_date(acct_month,'yyyymm'),to_date(to_char(sysdate,'yyyymm'),'yyyymm')) <= ? ";
		setString(1,hAcnoPSeqno);
		setInt(2,hUseRcmonth);
		selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("select act_intr not found, p_seqno=[%s] ,hUseRcmonth = [%s]",hAcnoPSeqno,hUseRcmonth));
			skipCntFlag = "Y";
			return;
		}
		double sumInterestAmt = getValueDouble("sum_interest_amt");
		if(sumInterestAmt > 0) {
			hIsRc = "Y";
		}
	}
	
	private void getPtrRcrateMap() throws Exception {
		sqlCmd = " select credit_rating,holding_period,rating,rcrate_day from ptr_rcrate where 1=1 ";
		int n = selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "getPtrRcrateMap not found!");
			skipCntFlag = "Y";
			return;
		}

		for(int i=0 ; i<n ;i++) {
			String creditRating = getValue("credit_rating",i);
			String holdingPeriod = getValue("holding_period",i);
			int rating = getValueInt("rating",i);
			double rcrateDay = getValueDouble("rcrate_day",i);
			String mapKey = creditRating + "|" + holdingPeriod;
			ratingMap.put(rating, rcrateDay);
			ptrRcrateMap.put(mapKey, rating);
		}
		
	}
	
	//處理流程說明-調升、調降的利率欄位取得
	private void selectPtrRcrate() throws Exception {
		if(skipCntFlag.equals("Y"))
			return;
		int rating = 0;
		hRcRateAfter = hRcRateOri;
		String mapKey = hCreditRating + "|" + (hCardDuration>=5 ? ">=N" : "<N");
		Integer mapValue = ptrRcrateMap.get(mapKey);
		if(mapValue == null){
//			showLogMessage("I", "", "select ptr_rcrate not found!");
//			skipCntFlag = "Y";
			return;
		}
		rating = mapValue.intValue();
		hRcRateBefroe = ratingMap.get(mapValue);
		
		if(hPenaltyCount3 > 0) {
			hUpDown = "U";
			rating += 3;
			getRcRateAfter(rating);
			return;
		}
		if(hPenaltyCount6 == 0) {
			hUpDown = "D";
			rating -= 1;
			getRcRateAfter(rating);
		}
	}
	
	//異動後利率
	private void getRcRateAfter(int rating) throws Exception{

		for(int i=0; i<ratingMap.size();i++) {
			int ii = 0;
			if(hUpDown.equals("U")) {
				ii = rating-i;
			}
			if(hUpDown.equals("D")) {
				ii = rating+i;
			}
			if(ratingMap.get(ii) == null) {
				continue;
			}
			hRcRateAfter = ratingMap.get(ii).doubleValue();
			return;
		}
		
	}
	
	//有調整的最終的利率
	private void getRevolveIntRate() throws Exception{
		if(skipCntFlag.equals("Y"))
			return;
		Double mapValue;
		double rcrateDay;
		
		if(hUpDown.equals("U")) {//升3
			mapValue = ratingMap.get(11);//取得級數11
			if(mapValue == null) {
				showLogMessage("I", "", "getRevolveIntRate not found!");
				skipCntFlag = "Y";
				return;
			}
			rcrateDay = mapValue.doubleValue();
			
			//[異動後利率] > [原利率] = [原利率]  UP_DOWN_FLAG='A'
			if(hRcRateAfter > hRcRateOri) {
				hRevolveIntRate = hRcRateOri;
				hUpDownFlag = "A";
				return;
			}
			
			//[異動後利率] <= [原利率] = [異動後利率]
			if(hRcRateAfter <= hRcRateOri) {
				hRevolveIntRate = hRcRateAfter;
				return;
			}
			
			//[異動前利率] = 15%用級數11讀取  and [原利率] < 15%用級數11讀取  = [異動後利率]  UP_DOWN_FLAG='E'
			if(hRcRateBefroe == rcrateDay && hRcRateOri < rcrateDay) {
				hRevolveIntRate = hRcRateAfter;
				hUpDownFlag = "E";
				return;
			}

		}
		if(hUpDown.equals("D")) {//降1
			mapValue = ratingMap.get(5);//取得級數5
			if(mapValue == null) {
				showLogMessage("I", "", "getRevolveIntRate not found!");
				skipCntFlag = "Y";
				return;
			}
			rcrateDay = mapValue.doubleValue();
			
			if(hRcRateBefroe<rcrateDay && hRcRateOri<rcrateDay) {
				//[異動前利率] > [原利率] = [原利率] UP_DOWN_FLAG='C'
				if(hRcRateBefroe > hRcRateOri) {
					hRevolveIntRate = hRcRateOri;
					hUpDownFlag = "C";
					return;
				}
				
				//[異動前利率] <= [原利率] = [異動後利率] UP_DOWN_FLAG='D'
				if(hRcRateBefroe <= hRcRateOri) {
					hRevolveIntRate = hRcRateAfter;
					hUpDownFlag = "D";
					return;
				}
			}
			if(hRcRateBefroe>rcrateDay || hRcRateOri>rcrateDay) {
				//[異動後利率] >= [原利率] = [異動後利率]
				if(hRcRateAfter >= hRcRateOri) {
					hRevolveIntRate = hRcRateAfter;
					return;
				}
				
				//[異動後利率] < [原利率] = [原利率]  UP_DOWN_FLAG='B'
				if(hRcRateAfter < hRcRateOri) {
					hRevolveIntRate = hRcRateOri;
					hUpDownFlag = "B";
					return;
				}
			}
		}
	}
	
	private void getRevolveRateMonth() {
		String businessMonth = comStr.left(businessDate, 6);
		hRevolveRateSMonth = commDate.monthAdd(businessMonth, 4);
		hRevolveRateEMonth = commDate.monthAdd(businessMonth, 6);
		hAcctMonth = hRevolveRateSMonth;
	}

	private void procData() throws Exception {
		selectCrdIdno();//A
		selectCrdCard();//B
		getIsStatus();//C
		selectCcaCardAcct();//D
		getCardDuration();
		selectActPenaltyLog();
		selectActIntr();
		selectPtrRcrate();
		getRevolveIntRate();
		getRevolveRateMonth();
		insertCycDiffRcrate();
		
		if ((readCnt % 50000) == 0) {
			showLogMessage("I", "","  Read w/ ROW " + readCnt);
			commitDataBase();
		}
	}
	
	/***********************************************************************/
	public void initData() {
		skipCntFlag = "";
		hIdPSeqno = "";
		hAcnoPSeqno = "";
		hIsStaff = "";
		hFlowCount = 0;
		hIsConform = "Y";
		hAcctStatus = "";
		hIsStatus = "Y";
		hIsBlock = "N"; 
		hCreditRating = "";
//		hCardSince = "";
		oriIssueDate = "";
		hCardDuration = 0;
		hPenaltyCount3 = 0;
		hPenaltyCount6 = 0;
		hIsRc = "N";
		hRcRateOri = 0;
		hUpDown = "";
		hRcRateBefroe = 0;
		hRcRateAfter = 0;
		hRevolveIntRate = 0;
		hRevolveRateSMonth = "";
		hRevolveRateEMonth = "";
		hAcctMonth = "";
		hUpDownFlag = "";
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CycR150 proc = new CycR150();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}

package Rsk;

import com.BaseBatch;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class RskP900 extends BaseBatch {
	
	private final String progname = "爭議款起帳 112-07-27 V1.00.03";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	CommCrdRoutine comcr = null;
	
	//--會計科目
	String acNo = "";
	String dbCr = "";
	double genAmount = 0.0;
	int dbcrSeq = 0;
	String stdVouchCd = "";
	String vouchDate = "";
	String lastVouchCd = "";
	String lastCloResult = "";
	boolean startVouch = false;
	String genMemo = "";
	
	String ctrlSeqno = "";
	String cardNo = "";
	double prbAmount = 0.0;
	String backFlag = "";
	String cloResult = "";
	String backStatus = "";	
	String referenceNo = "";
	String acctCode = "";	
	
	
	public static void main(String[] args) {
		RskP900 proc = new RskP900();
		proc.mainProcess(args);
		proc.systemExit();
	}	
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP110 [business_date]");
			errExit(1);
		}

		dbConnect();
		
		dateTime();
		
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}
		
		comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
		
		//--取得啟帳日
		selectPtrBusindayVouch();
		
		//--新增已覆核起帳 - 信用卡
		procData1Credit();
		
		//--新增已覆核回存起帳 - VD
		procData1VD();
		
		//--處理結案-信用卡
		procData2Credit();
		
		//--處理結案-VD
		procData2VD();
		
		//--寫入總帳-新增列問交
		proRskGenDetail1();
		//--寫入總帳-結案
		proRskGenDetail2();
		
		endProgram();
	}
	
	void selectPtrBusindayVouch() throws Exception {
		
		sqlCmd = "select vouch_date from ptr_businday where 1=1 ";
		sqlSelect();
		if(sqlNrow<=0) {
			vouchDate = hBusiDate;
			return ;
		}
		vouchDate = colSs("vouch_date");
		
	}
	
	void procData1Credit() throws Exception {
		
		sqlCmd = " select ctrl_seqno , card_no , prb_amount , reference_no "
			   + " from rsk_problem where add_apr_date = ? and debit_flag <> 'Y' "
			   ;
		
		setString(1,hBusiDate);
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			ctrlSeqno = colSs("ctrl_seqno");
			cardNo = colSs("card_no");
			prbAmount = colNum("prb_amount");
			referenceNo = colSs("reference_no");
			acctCode = getAcctCodeDebt();
			getGenData("A011",0);
			
			//--處理海外手續費
			procOverSeaBill("A011");						
		}
		sqlCommit();
		closeCursor();		
	}
	
	int tidDBill = -1;
	void procOverSeaDBill(String cd) throws Exception {
		if (tidDBill <=0) {			
			sqlCmd = " select reference_no , dest_amt , acct_code from dbb_bill where reference_no_original = ? ";
			tidDBill =ppStmtCrt("ti-S-dbbBill","");
		}
		
		setString(1,referenceNo);
		
		sqlSelect(tidDBill);
		if (sqlNrow <=0) {				
			return ;
		}
		
		referenceNo = colSs("reference_no");
		prbAmount = colNum("dest_amt");
		acctCode = colSs("acct_code");
		getGenData(cd,0);
		return ;
	}
	
	int tidDBill2 = -1;
	String getAcctCodeDebtVD() throws Exception {
		if (tidDBill2 <=0) {			
			sqlCmd = " select acct_code from dbb_bill where reference_no = ? ";
			tidDBill2 =ppStmtCrt("ti-S-dbbBill2","");
		}
		
		setString(1,referenceNo);
		
		sqlSelect(tidDBill2);
		if (sqlNrow <=0) {				
			return "";
		}
		
		return colSs("acct_code");
	}
	
	int tidBill = -1;
	void procOverSeaBill(String cd) throws Exception {
		if (tidBill <=0) {			
			sqlCmd = " select reference_no , dest_amt , acct_code from bil_bill where reference_no_original = ? ";
			tidBill =ppStmtCrt("ti-S-bilBill","");
		}
		
		setString(1,referenceNo);
		
		sqlSelect(tidBill);
		if (sqlNrow <=0) {				
			return ;
		}
		
		referenceNo = colSs("reference_no");
		prbAmount = colNum("dest_amt");
		acctCode = colSs("acct_code");
		getGenData(cd,0);
		return ;
	}
	
	int tidBill2 = -1;
	String getAcctCodeDebt() throws Exception {
		if (tidBill2 <=0) {			
			sqlCmd = " select acct_code from bil_bill where reference_no = ? ";
			tidBill2 =ppStmtCrt("ti-S-bilBill2","");
		}
		
		setString(1,referenceNo);
		
		sqlSelect(tidBill2);
		if (sqlNrow <=0) {				
			return "";
		}
		
		return colSs("acct_code");
	}
	
//	int tidDebtHst = -1;
//	String getAcctCodeDebtHst() throws Exception {
//		if (tidDebtHst <=0) {			
//			sqlCmd = " select acct_code from act_debt_hst where reference_no = ? ";
//			tidDebtHst =ppStmtCrt("ti-S-actDebtHst","");
//		}
//		
//		setString(1,referenceNo);
//		
//		sqlSelect(tidDebtHst);
//		if (sqlNrow <=0) {				
//			return "";
//		}
//		
//		return colSs("acct_code");
//	}
	
	void procData1VD() throws Exception {
		
		sqlCmd = " select ctrl_seqno , card_no , prb_amount , reference_no "
			   + " from rsk_problem where add_apr_date = ? and back_flag ='Y' "
			   ;
		
		setString(1,hBusiDate);
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			ctrlSeqno = colSs("ctrl_seqno");
			cardNo = colSs("card_no");
			prbAmount = colNum("prb_amount");
			referenceNo = colSs("reference_no");
			acctCode = getAcctCodeDebtVD();
			getGenData("D005",0);
			//--處理海外手續費
			procOverSeaDBill("D005");
		}
		sqlCommit();
		closeCursor();		
	}
	
	void procData2Credit() throws Exception {
		
		sqlCmd = " select ctrl_seqno , card_no , prb_amount , back_flag , back_status , clo_result , reference_no "
			   + " from rsk_problem where close_apr_date = ? and debit_flag <>'Y' ";
		
		setString(1,hBusiDate);
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			ctrlSeqno = colSs("ctrl_seqno");
			cardNo = colSs("card_no");
			prbAmount = colNum("prb_amount");
			backFlag = colSs("back_flag");
			cloResult = colSs("clo_result");
			backStatus = colSs("back_status");
			referenceNo = colSs("reference_no");
			acctCode = getAcctCodeDebt();
			
			if("14".equals(cloResult)) {
				getGenData("A012",0);
			} else if("18".equals(cloResult)) {
				getGenData("A018",0);
			} else if("21".equals(cloResult)) {
				getGenData("A016",0);
			} else if("|34,44,45".indexOf(cloResult) >= 0) {
				getGenData("A016",0);
				procOverSeaBill("A016");
			} else if("02".equals(cloResult)) {
				getGenData("A012",0);
			} else if("06".equals(cloResult)) {
				getGenData("A018",0);
			} else if("07".equals(cloResult)) {
				getGenData("A016",0);				
			} else if("08".equals(cloResult)) {
				getGenData("A011",0);				
			}
		}
		sqlCommit();
		closeCursor();		
		
	}
	
	void procData2VD() throws Exception {
		
		sqlCmd = " select ctrl_seqno , card_no , prb_amount , back_flag , back_status , clo_result , reference_no "
			   + " from rsk_problem where close_apr_date = ? and debit_flag ='Y' ";
		
		setString(1,hBusiDate);
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			ctrlSeqno = colSs("ctrl_seqno");
			cardNo = colSs("card_no");
			prbAmount = colNum("prb_amount");
			backFlag = colSs("back_flag");
			cloResult = colSs("clo_result");
			backStatus = colSs("back_status");
			referenceNo = colSs("reference_no");
			acctCode = getAcctCodeDebtVD();
			if("D6".equals(cloResult)) {
				if("S".equals(backStatus) == false)
					continue;
				
				getGenData("D008",0);
				procOverSeaDBill("D008");
			}	else if("D8".equals(cloResult)) {
				if("S".equals(backStatus)) {
					getGenData("D006",0);
				}	else	{
					getGenData("D003",0);
					procOverSeaDBill("D003");
				}								
			}	else if("D7".equals(cloResult)) {
				continue;
			}	else if("D9".equals(cloResult)) {
				if("S".equals(backStatus)) {
					getGenData("D007",0);
				}	else	{
					getGenData("D004",0);
					procOverSeaDBill("D004");
				}			
			}						
			
		}
		sqlCommit();
		closeCursor();		
		
	}
	
	void proRskGenDetail1() throws Exception {		
		sqlCmd = " select sum(prb_amount) as gen_amount , std_vouch_cd , ac_no "
			   + " from rsk_gen_detail where gen_date = ? and std_vouch_cd in ('A011','D005') "
			   + " group by std_vouch_cd , ac_no "
			   + " order by std_vouch_cd "
			   ;
			
		setString(1,vouchDate);
			
		openCursor();
			
		while(fetchTable()) {
			initDataGen();
			genAmount = colNum("gen_amount");			
			stdVouchCd = colSs("std_vouch_cd");
			acNo = colSs("ac_no");
			
			if(lastVouchCd.equals(stdVouchCd) == false) {
				lastVouchCd = stdVouchCd;
				startVouch = false ;
			}
			
			getGenData(stdVouchCd,1);										
		}
		sqlCommit();
		closeCursor();
	}
	
	void proRskGenDetail2() throws Exception {
		lastCloResult = "" ;
		lastVouchCd = "";
		startVouch = false;
		
		sqlCmd = " select sum(A.prb_amount) as gen_amount , A.std_vouch_cd , A.ac_no , B.clo_result "
			   + " from rsk_gen_detail A join rsk_problem B on A.ctrl_seqno = B.ctrl_seqno "
			   + " where A.gen_date = ? and A.std_vouch_cd not in ('A011','D005') "
			   + " group by A.std_vouch_cd , A.ac_no , B.clo_result "
			   + " order by B.clo_result Asc , A.std_vouch_cd Asc "
			   ;
		
		setString(1,vouchDate);
		
		openCursor();
		
		while(fetchTable()) {
			initDataGen();
			genAmount = colNum("gen_amount");			
			stdVouchCd = colSs("std_vouch_cd");
			acNo = colSs("ac_no");
			cloResult = colSs("clo_result");
			
			if(lastCloResult.equals(cloResult) == false || lastVouchCd.equals(stdVouchCd) == false) {
				lastCloResult = cloResult;
				lastVouchCd = stdVouchCd;
				startVouch = false ;
			}
			
			getGenData(stdVouchCd,1);										
		}
		sqlCommit();
		closeCursor();
	}
	
	int tidGen = -1;
	void getGenData(String cd , int type) throws Exception {
		if (tidGen <=0) {			
			sqlCmd = " select ac_no , dbcr , dbcr_seq from gen_sys_vouch where std_vouch_cd = ? order by dbcr_seq Asc ";
			tidGen =ppStmtCrt("ti-S-genSysVouch","");
		}
		
		setString(1,cd);
		
		sqlSelect(tidGen);
		if (sqlNrow <=0) {				
			return ;
		}
						
		for(int ii=0 ; ii< sqlNrow ;ii++) {						
			if(type == 0) {
				acNo = colSs(ii,"ac_no");
				dbCr = colSs(ii,"dbcr");
				dbcrSeq = colInt(ii,"dbcr_seq");
				
				if("A011".equals(cd)) {					
					if(dbcrSeq == 1) {
						//--借:其他應收款-國際卡待查帳款
						if("PF".equals(acctCode))
							continue;
					}	else if(dbcrSeq == 2) {
						//--借:信用卡手續費收入-什項	
						if("PF".equals(acctCode) == false)
							continue;
					}	else if(dbcrSeq == 3) {
						//--貸:應收信用卡款項-信用卡墊款
						if("PF".equals(acctCode) || "IT".equals(acctCode))
							continue;
					}	else if(dbcrSeq == 4) {
						//--貸:應收信用卡款項-分期付款
						if("IT".equals(acctCode) == false)
							continue;
					}	else if(dbcrSeq == 5) {
						//--貸:應收信用卡款項-信用卡手續費	
						if("PF".equals(acctCode) == false)
							continue;
					}
				}	else if("A016".equals(cd)) {
					if("21".equals(cloResult)) {
						if(dbcrSeq == 1) {
							//--借:應收信用卡款項-信用卡墊款
							if("IT".equals(acctCode))
								continue;
						}	else if(dbcrSeq == 2) {
							//--借:應收信用卡款項-分期付款
							if("IT".equals(acctCode) == false)
								continue;
						}	else if(dbcrSeq == 3) {
							//--退貨沒有手續費
							continue;
						}	else if(dbcrSeq == 5) {
							//--手續費貸方跳過
							continue;
						}
					}	else if("|34,44,45,07,08".indexOf(cloResult) >= 0) {
						if(dbcrSeq == 1) {
							//--借:應收信用卡款項-信用卡墊款
							if("IT".equals(acctCode) || "PF".equals(acctCode))
								continue;
						}	else if(dbcrSeq == 2) {
							//--借:應收信用卡款項-分期付款
							if("IT".equals(acctCode) == false)
								continue;
						}	else if(dbcrSeq == 3) {
							//--借:應收信用卡款項-信用卡手續費
							if("PF".equals(acctCode) == false)
								continue;
						}	else if(dbcrSeq == 4) {
							//--貸:其他應收款-國際卡待查帳款
							if("PF".equals(acctCode))
								continue;
						}	else if(dbcrSeq == 5) {
							//--貸:信用卡手續費收入-什項
							if("PF".equals(acctCode) == false)
								continue;
						}
					}	
				}	else if("D005".equals(cd)) {
					if(dbcrSeq == 1) {
						//--借:其他應收款-國際卡待查帳款
						if("PF".equals(acctCode))
							continue;
					}	else if(dbcrSeq == 2) {
						//--借:信用卡手續費收入-什項
						if("PF".equals(acctCode) == false)
							continue;
					}	else if(dbcrSeq == 3) {
						//--貸:應收信用卡款項-信用卡手續費	
						if("PF".equals(acctCode) == false)
							continue;					
					}	else if(dbcrSeq == 4) {
						//--貸:聯行往來
						if("PF".equals(acctCode))
							continue;
					}
				}	else if("D003".equals(cd)) {
					if(dbcrSeq == 1) {
						//--借:其他應付款-國際卡沖帳款
						if("PF".equals(acctCode))
							continue;
					}	else if(dbcrSeq == 2) {
						//--借:信用卡手續費收入-什項
						if("PF".equals(acctCode) == false)
							continue;
					}	else if(dbcrSeq == 3) {
						//--貸:聯行往來
						if("PF".equals(acctCode))
							continue;					
					}	else if(dbcrSeq == 4) {
						//--貸:應收信用卡款項-信用卡手續費	
						if("PF".equals(acctCode) == false)
							continue;
					}
				}	else if("D004".equals(cd)) {
					if(dbcrSeq == 1) {
						//--借:其他應收款-信用卡疑義帳款
						if("PF".equals(acctCode))
							continue;
					}	else if(dbcrSeq == 2) {
						//--借:信用卡手續費收入-什項
						if("PF".equals(acctCode) == false)
							continue;
					}	else if(dbcrSeq == 3) {
						//--貸:應收信用卡款項-信用卡手續費
						if("PF".equals(acctCode) == false)
							continue;					
					}	else if(dbcrSeq == 4) {
						//--貸:聯行往來	
						if("PF".equals(acctCode))
							continue;
					}
				}	else if("D008".equals(cd)) {
					if(dbcrSeq == 1) {
						//--借:聯行往來
						if("PF".equals(acctCode))
							continue;
					}	else if(dbcrSeq == 2) {
						//--借:應收信用卡款項-信用卡手續費
						if("PF".equals(acctCode) == false)
							continue;
					}	else if(dbcrSeq == 3) {
						//--貸:信用卡手續費收入-什項
						if("PF".equals(acctCode) == false)
							continue;					
					}	else if(dbcrSeq == 4) {
						//--貸:其他應收款-國際卡待查帳款
						if("PF".equals(acctCode))
							continue;
					}
				}
				
				insertRskGenDetail(cd);
			}	else if(type == 1) {
				if(eqIgno(acNo,colSs(ii,"ac_no")) == false)
					continue;
				acNo = colSs(ii,"ac_no");
				dbCr = colSs(ii,"dbcr");
				dbcrSeq = colInt(ii,"dbcr_seq");
				
				if(genAmount != 0) {
					if(startVouch == false) {
						comcr.startVouch("1", cd); // 在進入routin前指定hVouchCdKind
						startVouch = true;
					}
						
					String tmpstr = String.format("RskP900_%s.%s_%s","R01", cloResult, "901");  
					comcr.hGsvhModPgm = tmpstr;
					comcr.hGsvhModWs = "RSKP900R01";
					comcr.hGsvhMemo1 = getMemo(cd,acNo,cloResult);
					if(comcr.detailVouch(acNo, dbcrSeq, genAmount, "901") != 0) {					
						showLogMessage("E", "", "call detail_vouch error, AcNo=[" + acNo + "]");
						return;
					}
				}
			}			
		}				
	}
	
	void initData() {
		ctrlSeqno = "";
		cardNo = "";
		prbAmount = 0.0;
		backFlag = "";
		cloResult = "";
		backStatus = "";
		referenceNo = "";
		acctCode = "";		
	}
	
	String getMemo(String cd,String acNo,String result) {
		
		if(result.isEmpty()) {
			//--新增列問交
			if("A011".equals(cd)) {
				if("130970182".equals(acNo) || "130270032".equals(acNo) || "130270059".equals(acNo)) 
					return "待查帳款轉出";
								
				if("420610014".equals(acNo) || "130270024".equals(acNo))
					return "待查帳款轉出(手續費減免)";
				
			}	else if("D005".equals(cd)) {
				if("130970182".equals(acNo) || "196930103".equals(acNo))
					return "VD待查帳款轉出(回存)";
				
				if("420610014".equals(acNo) || "130270024".equals(acNo))
					return "VD待查帳款轉出(手續費減免)";
				
			}
		}	else if("14".equals(result)) {
			return "常董會通過轉銷損失";
		}	else if("18".equals(result)) {
			return "C/B成功問交結案";
		}	else if("21".equals(result)) {
			return "特店退款問交結案";
		}	else if("34".equals(result) || "44".equals(result)) {
			if("130270032".equals(acNo) || "130970182".equals(acNo)) {
				return "持卡人需付本金與利息";
			}	else if("130270024".equals(acNo) || "420610014".equals(acNo)) {
				return "問交結案手續費還原";
			}	else if("130270059".equals(acNo)) {
				return "持卡人需付本金與利息(分期款)";
			}
		}	else if("45".equals(result)) {
			if("130270032".equals(acNo) || "130970182".equals(acNo)) {
				return "持卡人需付本金";
			}	else if("130270024".equals(acNo) || "420610014".equals(acNo)) {
				return "問交結案手續費還原";
			}	else if("130270059".equals(acNo)) {
				return "持卡人需付本金(分期款)";
			}
		}	else if("D8".equals(result)) {
			if("D003".equals(cd)) {
				if("230970575".equals(acNo) || "196930103".equals(acNo)) {
					return "VD C/B成功回存";
				}	else if("420610014".equals(acNo) || "130270024".equals(acNo)) {
					return "VD C/B成功手續費回存";
				}
			}	else if("D006".equals(cd)) {
				return "VD C/B成功問交結案";
			}			
		}	else if("D9".equals(result)) {
			if("D004".equals(cd)) {
				if("130970221".equals(acNo) || "196930103".equals(acNo)) {
					return "VD問交結案評估損失(回存)";
				}	else if("420610014".equals(acNo) || "130270024".equals(acNo)) {
					return "VD問交結案評估損失(手續費回存)";
				}
			}	else if("D007".equals(cd)) {
				return "常董會通過轉銷損失";
			}
		}	else if("07".equals(result)) {
			return "特店退錯款沖回不合格款項";
		}	else if("08".equals(result)) {
			return "退貨收回(負項)不合格款項";
		}	else if("02".equals(result)) {
			return "常董會通過轉銷損失(非本行卡)";
		}	else if("06".equals(result)) {
			return "C/B 成功，沖銷不合格款項";
		}
		
		return "";
	}
	
	void initDataGen() {
		acNo = "";
		dbCr = "";
		genAmount = 0;
		dbcrSeq = 0;
		cloResult = "";
		genMemo = "";		
	}
	
	void insertRskGenDetail(String vouchCd) throws Exception {
		daoTable = "rsk_gen_detail";
		setValue("ctrl_seqno",ctrlSeqno);
		setValue("card_no",cardNo);
		setValueDouble("prb_amount",prbAmount);
		setValue("gen_date",vouchDate);
		setValue("std_vouch_cd",vouchCd);
		setValue("ac_no",acNo);
		setValue("dbcr",dbCr);
		setValue("crt_date",sysDate);
		setValue("crt_time",sysTime);
		setValue("crt_user","ecs");
		setValue("mod_user","ecs");
		setValue("mod_time",sysDate + sysTime);
		setValue("mod_pgm","RskP900");
		setValueInt("mod_seqno",1);
		insertTable();		
	}
	
}

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/04/10  V1.00.00    Wendy     program initial                          *
*  109/07/22  V0.00.01    Zuwei     coding standard, rename field method & format  *
*  109/09/16  V0.00.02    Sunny     增加前協相關的Mothod                         *
*  111/10/18  V0.00.03    Ryan      增加InfR005相關的Mothod                         *
*  111/12/30  V0.00.04    Ryan      增加ColC020相關的Mothod                         *
*  112/01/04  V0.00.05    Ryan      增加ColC023相關的Mothod                         *
*  112/04/26  V0.00.06    Ryan      增加InfC055相關的Mothod                         *
*  112/07/19  V0.00.07    Ryan      增加SMS共用變數筆數,時間                                                                              *
*****************************************************************************/
package com;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;


public class CommCol extends AccessDAO{
	public static final int  SMS_CONTROL_CNT = 3500;
    public static final String  SEND_SMS_TIME_AM = "090000";
    public static final String  SEND_SMS_TIME_PM = "140000";
	 String[] DBNAME = new String[10];
     CommString commStr = new CommString();
	 public CommCol(Connection conn[],String[] dbAlias) throws Exception
	 {
		 
	   super.conn  = conn;
	   setDBalias(dbAlias);
	   setSubParm(dbAlias);
	   DBNAME[0]=dbAlias[0];

	 }
	
	
	/**
	 * 取得公司戶項下個卡加總欠款金額
	 * @return
	 * @throws Exception
	 */
	public int getCorpAcctJrnlBal(String corpPSeqno, String acctType) throws Exception {
		
		
		int acctJrnlBal = 0;
						
        sqlCmd = "select sum(ACCT_JRNL_BAL) as h_ACCT_JRNL_BAL from ACT_ACCT ";
        sqlCmd += "where corp_p_seqno = ? ";
        sqlCmd += "and acct_type = ? ";
        
        setString(1, corpPSeqno);
        setString(2, acctType);
        
        selectTable();
        if (notFound.equals("Y")) {
        	throw new Exception("select_acct_jrnl_bal error");
        }
        
        acctJrnlBal = getValueInt("h_ACCT_JRNL_BAL");

        
		return acctJrnlBal;
	}
	
	/**
	 * 取得公司項下所有個卡(p_seqno)最大的  mcode
	 * @return
	 * @throws Exception
	 */
	public int getCorpMaxMcode(String corpPSeqno, String acctType) throws Exception {
		
		
		int mcode = 0;
						
        sqlCmd = "select max(INT_RATE_MCODE) as h_MCODE from ACT_ACNO ";
        sqlCmd += "where corp_p_seqno = ? ";
        sqlCmd += "and acct_type = ? ";
        sqlCmd += "group by corp_p_seqno, acct_type";
        
        setString(1, corpPSeqno);
        setString(2, acctType);
        
        selectTable();
        if (notFound.equals("Y")) {
        	throw new Exception("select_int_rate_mcode error");
        }
        
        mcode = getValueInt("h_MCODE");

        
		return mcode;
	}
	
	/**
	 * 取得公司項下所有個卡p_seqno
	 * @return
	 * @throws Exception
	 */
	public String [] getCorpPseqno(String corpPSeqno, String acctType) throws Exception {
		String[] pSeqnoArr;
        sqlCmd = "select distinct P_SEQNO as h_P_SEQNO from ACT_ACNO ";
        sqlCmd += "where corp_p_seqno = ? ";
        sqlCmd += "and acct_type = ? ";
        
        setString(1, corpPSeqno);
        setString(2, acctType);
        
        int count = selectTable();
        if (notFound.equals("Y")) {
        	throw new Exception("select_p_seqno error");
        }
        pSeqnoArr = new String[count];; 
        for(int i = 0; i < count ; i++ ) {
        	pSeqnoArr[i] = getValue("h_P_SEQNO",i);
        }
            
		return pSeqnoArr;
	}

	/**
	 *  前置協商，以id_p_seqno,file_date,status查詢col_liac_nego_t暫存檔中是否已有資料。
	 * @return 0 有資料 -1 表沒有資料
	 * @throws Exception
	 */
	
	//public String selectColLiacNegoT(String hIdnoIdPSeqno,String hEflgFileDate,String fileStatus) throws Exception {
	public String selectColLiacNegoT(String hIdnoIdPSeqno,String fileStatus) throws Exception {
    			
    	sqlCmd = "select liac_status ";
        sqlCmd += "from col_liac_nego_t ";
        sqlCmd += "where id_p_seqno = ? ";		 
        //sqlCmd += "and file_date = ? ";
        sqlCmd += "and liac_txn_code = 'A' ";
    	sqlCmd += "and liac_status = ?";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hIdnoIdPSeqno);
        //setString(2, hEflgFileDate);
        setString(2, fileStatus);
        
        int recordCnt = selectTable();
 
        if (recordCnt == 0)
          return "-1"; /*無資料*/
        else
          return "0"; /*有資料*/
  }
	
	/**
	 * 以id_no查詢crd_idno變更ID檔的id_p_seqno、chi_name
	 * @return 0 回覆id_p_seqno -1 無資料
	 * @throws Exception
	 */
	
	public String selectCrdIdno(String hIdNo) throws Exception {
	    	
		String hIdnoIdPSeqno="";

		sqlCmd  = "select id_p_seqno, chi_name ";
	    sqlCmd += "from crd_idno ";
	    sqlCmd += "where id_no = ? ";
	    setString(1, hIdNo);
	        
	    int recordCnt = selectTable();
	    if (recordCnt > 0) {   		
    		hIdnoIdPSeqno = getValue("id_p_seqno");
    	//	hIdnoChiName = getValue("chi_name");
     		return hIdnoIdPSeqno;
	    }
	    else{
	    	return "-1";	
	    }
	}
	
	/**
	 * 以id_no查詢crd_card的卡片數(正卡人)，只統計一般卡
	 * @return 0 有資料 -1 無資料
	 * @throws Exception
	 */
	
	public String selectCrdCardCnt(String hIdPSeqno) throws Exception {
	    	
		String hIdnoIdPSeqno="";

		sqlCmd  = "select card_no ";
	    sqlCmd += "from crd_card ";
	    sqlCmd += "where major_id_p_seqno = ? and acct_type='01' ";
	    setString(1, hIdPSeqno);
	        
	    int recordCnt = selectTable();
	    if (recordCnt > 0) {   		
	    //	hIdnoIdPSeqno = getValue("id_p_seqno");
    	//	hIdnoChiName = getValue("chi_name");
     		return "1"; //有資料
	    }
	    else{
	    	return "0";	
	    }
	}
	
	/**
	 * 以id_no查詢crd_idno變更ID檔的中文姓名
	 * @return 0 回覆中文姓名 -1 無資料
	 * @throws Exception
	 */
	 
	public String selectCrdIdnoName(String hIdNo) throws Exception {
    	
		String hIdnoChiName="";

		sqlCmd  = "select id_p_seqno, chi_name ";
	    sqlCmd += "from crd_idno ";
	    sqlCmd += "where id_no = ? ";
	    setString(1, hIdNo);
	        
	    int recordCnt = selectTable();
	    if (recordCnt > 0) {   		
    		hIdnoChiName = getValue("chi_name");
    		return hIdnoChiName;
	    }
	    else{
	    	return "-1";	
	    }
	}
	
	/**
	 *  以id_no查詢crd_chg_id變更ID檔的id_p_seqno及姓名。
	 * @return 0 回覆id_p_seqno 1 無資料
	 * @throws Exception
	 */
	
	public String selectCrdChgId(String hOldIdNo) throws Exception {
		
		String hIdnoIdPSeqno="";
		
    	sqlCmd  = "select a.id_p_seqno, a.chi_name ";
    	sqlCmd += "from crd_chg_id a, crd_idno b ";
    	sqlCmd += "where a.id_no = b.id_no ";
    	sqlCmd += "and a.old_id_no = ? ";
    	setString(1, hOldIdNo);
    	int recordCnt = selectTable();
    	 if (recordCnt > 0) {   		
     		hIdnoIdPSeqno = getValue("old_id_p_seqno");
     		return hIdnoIdPSeqno;
 	    }
 	    else{
 	    	return "-1";	
 	    }    	
	}
	
	/**
	 *  以id_no查詢crd_chg_id變更ID檔的id_p_seqno及姓名。
	 * @return 0 回覆id_p_seqno 1 無資料
	 * @throws Exception
	 */
	
	public String selectCrdCardCNT(String hOldIdNo) throws Exception {
		
		String hIdnoIdPSeqno="";
		
    	sqlCmd  = "select a.id_p_seqno, a.chi_name ";
    	sqlCmd += "from crd_chg_id a, crd_idno b ";
    	sqlCmd += "where a.id_no = b.id_no ";
    	sqlCmd += "and a.old_id_no = ? ";
    	setString(1, hOldIdNo);
    	int recordCnt = selectTable();
    	 if (recordCnt > 0) {   		
     		hIdnoIdPSeqno = getValue("old_id_p_seqno");
     		return hIdnoIdPSeqno;
 	    }
 	    else{
 	    	return "-1";	
 	    }    	
	}
	
	/**
	 *  抓取歸戶的act_acag，取得lastpay_date，再以最後lastpay_date與營業日相比較，計算逾期天數，最後回傳值得到天數。
	 * @param pSeqno p_seqno
	 * @return 逾期天數
	 * @throws Exception
	 */
	public int getDelayDay(String pSeqno) throws Exception {
		int delayDay = 0;
		String lastpayDate = "";
		String businessDate = "";
    	sqlCmd  = "select lastpay_date ";
    	sqlCmd += "from act_acag ";
    	sqlCmd += "where p_seqno = ? ";
    	sqlCmd += "order by acct_month desc ";
    	sqlCmd += "fetch first 1 rows only";
    	setString(1, pSeqno);
    	int recordCnt = selectTable();
    	if (recordCnt > 0) {   		
			lastpayDate = getValue("lastpay_date");
			if(new CommString().empty(lastpayDate))
				return 0;
			businessDate = getBusiDate();
			sqlCmd = "select abs(days(to_date(?,'yyyymmdd')) - days(to_date(?,'yyyymmdd'))) delay_day from dual";
			setString(1, businessDate);
			setString(2, lastpayDate);
			selectTable();
			if (!notFound.equals("Y")) {
				delayDay = getValueInt("delay_day");
			}
    	}
		return delayDay;
	}

	/**
	 *  將天數轉換成逾期區間。
	 * @param delayDay 逾期天數
	 * @return 逾期區間
	 */
	public String getDelayDayRange(int delayDay){
		String rangeDayStr = "";
		int d =  delayDay > 0   ? 1 : 0;
			d += delayDay > 15  ? 1 : 0;
			d += delayDay > 30  ? 1 : 0;
			d += delayDay > 60  ? 1 : 0;
			d += delayDay > 90  ? 1 : 0;
			d += delayDay > 120 ? 1 : 0;
			d += delayDay > 150 ? 1 : 0;
			d += delayDay > 180 ? 1 : 0;
			d += delayDay > 210 ? 1 : 0;
		switch(d) {
		case 1:
			rangeDayStr = "1-15";
			break;
		case 2:
			rangeDayStr = "15-30";
			break;
		case 3:
			rangeDayStr = "30-60";
			break;
		case 4:
			rangeDayStr = "60-90";
			break;
		case 5:
			rangeDayStr = "90-120";
			break;
		case 6:
			rangeDayStr = "120-150";
			break;
		case 7:
			rangeDayStr = "150-180";
			break;
		case 8:
			rangeDayStr = "180-210";
			break;
		case 9:
			rangeDayStr = "> 210";
			break;
		}
		
		return rangeDayStr;
	}
	
	/**
	 *  檢核是否為當月最後一天營業日。
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean isLastBusinday(String searchDate) throws Exception {
//		StringBuffer sqlStr = null;
		String lastDate =  lastdateOfmonth(searchDate);
//		String lastBusinessDate = "";
//		int day = 0;
//		while(true) {
//			sqlStr = new StringBuffer();
//			sqlStr.append("select count(*) as cnt ");
//			sqlStr.append(",(select to_char(to_date(?,'YYYYMMDD') - ");
//			sqlStr.append(day);
//			sqlStr.append(" day,'YYYYMMDD') from dual) as last_business_Date ");
//			sqlStr.append("from ptr_holiday ");
//			sqlStr.append("where holiday = (select to_char(to_date(?,'YYYYMMDD') - ");
//			sqlStr.append(day);
//			sqlStr.append(" day,'YYYYMMDD') from dual) ");
//			sqlCmd = sqlStr.toString();
//			setString(1, lastDate);
//			setString(2, lastDate);
//			selectTable();
//			int cnt = getValueInt("cnt");
//			if(cnt == 0) {
//				lastBusinessDate = getValue("last_business_Date");
//				break;
//			}
//			day++;
//		}
//		if(lastBusinessDate.equals(searchDate)) {
		if(lastDate.equals(searchDate)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 取得當月最後一天
	 * @return 當月最後一天
	 * @throws Exception 
	 */
	public String lastdateOfmonth(String date) throws Exception {
		date = date.trim();
		if(date.length()<6||date.length() == 0)
			date = getBusiDate();
		if (date.length() == 6)
			date = date + "01";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate parsedDate = LocalDate.parse(date, formatter);
		LocalDate lastDay = parsedDate.with(TemporalAdjusters.lastDayOfMonth());
		return lastDay.format(formatter);
	}
	
	
	/**
	 * 查詢act_debt此歸戶下當下現欠金額最大的卡號
	 * 依金額由大至小排序，SNO為1表示金額最大
	 * 當acct_type=01時，可能會有雙幣卡，需要依幣別分開計算最大現欠餘額
	 * 並取得卡號，目前最多可能會有3筆，台幣(901)、美金(840)、日元(392)。
	 * @return curr_code,card_no
	 * @throws Exception 
	 */
	public String[] selectMaxCardDebtAmt(String pSeqno) throws Exception {
		String[] debtArray = {"",""}; 
		sqlCmd = "select acct_type,p_seqno,curr_code,card_no,sum(end_bal) as sum_end_bal ";
		sqlCmd += "from act_debt ";
		sqlCmd += "Where p_seqno = ? group by acct_type,p_seqno,curr_code,card_no order by sum_end_bal desc ";
		setString(1,pSeqno);
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			debtArray[0] = getValue("curr_code");
			debtArray[1] = getValue("card_no");
		}
		
		return debtArray;
	}
	
	
	/**
	 *以【最大欠款卡號】取得代表當前卡號所屬之受理行。
	 * @return reg_bank_no
	 * @throws Exception 
	 */
	public String selectCardRiskBankNo(String cardNo) throws Exception {
		String regBankNo = "";
		sqlCmd = "Select reg_bank_no from crd_card where card_no = ? ";
		setString(1,cardNo);
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			regBankNo = getValue("reg_bank_no");
		}
		
		return regBankNo;
	}
	
	
	/**
	 *以【最大欠款卡號】取得核卡分行
	 *當一般卡且發卡日大於等於95.1.1，則令核卡分行為3144【表卡務中心於95.1.1建立】，其他一律視同原卡片受理行。
	 * @return reg_bank_no
	 * @throws Exception 
	 */
	public String selectCardIssueRegBankNo(String cardNo) throws Exception {
		String regBankNo = "";
		sqlCmd = "select case when acct_type='01' and issue_date >='20060101' then '3144' else reg_bank_no end as db_reg_bank_no ";
		sqlCmd += "from crd_card where card_no = ? ";
		setString(1,cardNo);
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			regBankNo = getValue("db_reg_bank_no");
		}
		
		return regBankNo;
	}
	
	
	/**
	 * 取得受理行中文名稱
	 * @return full_chi_name
	 * @throws Exception 
	 */
	public String getBranchChiName(String bankNo) throws Exception {
		String fullChiName = "";
		sqlCmd = "select full_chi_name from gen_brn where branch = ? ";
		setString(1,bankNo);
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			fullChiName = getValue("full_chi_name");
		}
		
		return fullChiName;
	}
	
	/***
	 *   一般卡歸戶層換算逾期天數
	 *   1轉30
		 2轉60
                         3轉90
                         4轉120
                         5轉150
                         6轉180
         7(含以上)轉210
                        當act_acno.max(int_rate_mcode)=0時，
                        需要增加判斷mp餘額，
                        若餘額大於0且執行日(營業日)大於繳款寬限日，
                        則逾期天數=15，其餘則視同逾期天數=0。
	 * @param mcode
	 * @param pSeqno
	 * @return
	 * @throws Exception 
	 */
	public String getPSeqnoDelayDay(String mcode,String pSeqno,String searchDate ) throws Exception {
		String delayDay = "0";
		String thisDelaypayDate = "";
		double minPayBal = 0;
		if(commStr.pos(",1,2,3,4,5,6", mcode)>0) {
			delayDay = commStr.decode(mcode,",1,2,3,4,5,6", ",30,60,90,120,150,180");
		}else if(commStr.ss2int(mcode)>=7) {
			delayDay = "210";
		}else if("0".equals(mcode)) {
			extendField = "DELAYPAY_DAY.";
			sqlCmd = "SELECT A.MIN_PAY_BAL,B.THIS_DELAYPAY_DATE  ";
			sqlCmd += "FROM ACT_ACCT A,PTR_WORKDAY B,ACT_ACNO C WHERE C.STMT_CYCLE = B.STMT_CYCLE ";
			sqlCmd += " AND A.P_SEQNO = C.P_SEQNO AND A.P_SEQNO = ? ";
			setString(1,pSeqno);
			int recordCnt = selectTable();
			if(recordCnt > 0) {
				minPayBal = getValueDouble("DELAYPAY_DAY.MIN_PAY_BAL");
				thisDelaypayDate = getValue("DELAYPAY_DAY.THIS_DELAYPAY_DATE");
			}
			
			if(minPayBal>0 && searchDate.compareTo(thisDelaypayDate)>0) {
				delayDay = "15";
			}
		}
		
		return delayDay;
	}
	
	
	/***
	 *   商務卡帳戶層換算逾期天數
	 *   1轉30
		 2轉60
                         3轉90
                         4轉120
                         5轉150
                         6轉180
         7(含以上)轉210
                        當act_acno.max(int_rate_mcode)=0時，
                        需要增加判斷mp餘額，
                        若餘額大於0且執行日(營業日)大於繳款寬限日，
                        則逾期天數=15，其餘則視同逾期天數=0。
	 * @param mcode
	 * @param corpPSeqno
	 * @param searchDate(執行日期)
	 * @return
	 * @throws Exception 
	 */
	public String getCorpPSeqnoDelayDay(String mcode, String corpPSeqno,String searchDate) throws Exception {
		String delayDay = "0";
		double sumMinPayBal = 0;
		String thisDelaypayDate = "";
		if(commStr.pos(",1,2,3,4,5,6", mcode)>0) {
			delayDay = commStr.decode(mcode,",1,2,3,4,5,6", ",30,60,90,120,150,180");
		}else if(commStr.ss2int(mcode)>=7) {
			delayDay = "210";
		}else if("0".equals(mcode)) {
			extendField = "SUM_BAL.";
			sqlCmd = " SELECT SUM(MIN_PAY_BAL) AS SUM_MIN_PAY_BAL ";
			sqlCmd += " FROM ACT_ACCT WHERE CORP_P_SEQNO = ? ";
			setString(1,corpPSeqno);
			int recordCnt = selectTable();
			if(recordCnt > 0) {
				sumMinPayBal = getValueDouble("SUM_BAL.SUM_MIN_PAY_BAL");
			}
			if(sumMinPayBal>0) {
				thisDelaypayDate = getThisDelaypayDate(corpPSeqno);
				if(searchDate.compareTo(thisDelaypayDate)>0) {
					delayDay = "15";
				}
			}
		}
		
		return delayDay;
	}
	
	private String getThisDelaypayDate(String corpPSeqno) throws Exception {
		String thisDelaypayDate = "";
		extendField = "DELAYPAY.";
		sqlCmd = "SELECT B.THIS_DELAYPAY_DATE,A.MIN_PAY_BAL  ";
		sqlCmd += " FROM ACT_ACCT A,PTR_WORKDAY B,ACT_ACNO C ";
		sqlCmd += " WHERE C.STMT_CYCLE=B.STMT_CYCLE ";
		sqlCmd += " AND A.P_SEQNO=C.P_SEQNO ";
		sqlCmd += " AND A.ACCT_TYPE='03' ";
		sqlCmd += " AND A.CORP_P_SEQNO=? ";
		sqlCmd += " ORDER BY A.MIN_PAY_BAL DESC,B.THIS_DELAYPAY_DATE ";
		sqlCmd += " LIMIT 1 ";
		setString(1,corpPSeqno);
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			thisDelaypayDate = getValue("DELAYPAY.THIS_DELAYPAY_DATE");
		}
		return thisDelaypayDate;
	}
	
}

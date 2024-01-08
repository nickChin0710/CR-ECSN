/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/06/01  V1.00.00    ryan     program initial                           *
 *  112/07/26  V1.00.01    ryan     調整selectBilBill                           *
 *  112/08/15  V1.00.02    Grace    selectBilBill(), BIL_FISCDTL.REIMB_INFO, 除了F1, 增A1 *
 ******************************************************************************/

package Mkt;


import java.util.HashMap;
import java.util.LinkedHashMap;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/*設一科未啟用註銷作業補INSERT CCA_OUTGOING程式*/
public class MktC910 extends AccessDAO {
    private String progname = "稅務回饋資料篩選處理程式  112/08/15 V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommString comStr = new CommString();
    CommCrdRoutine comcr = null;
    private int totalCnt = 0;
    private int bilBillCnt = 0;
    private int bilBillCnt2 = 0;
    private int argCnt = 0;
    private String strDate = "";
    private int checkFailCnt = 0;
    //****MKT_TAX_PARM*****//
    private String activeCode = "";
    private String activeType = "";
    private int feedbackAllTotcnt = 0;
    private int feedbackEmpTotcnt = 0;
    private int feedbackNonempTotcnt = 0;
    private int feedbackPerempCnt = 0;
    private int feedbackPernonempCnt = 0;
    private String purchaseDateS = "";
    private String purchaseDateE = "";
    private String feedbackIdType = "";
    private int feedbackSeqno = 0;
    private long purchaseAmtS = 0;
    private long purchaseAmtE = 0;
    private String minPurchaseDateS = "";
    private String maxPurchaseDateE = "";
    private String giftType = "";
    private String calDefDate = "";
    
    //****BIL_BILL*****//
    private String purchaseDate = "";
    private String staffFlag = "";
    private long cashPayAmt = 0;
    private String idNo = "";
    private String idPSeqno = "";
    private String cardNo = "";
    
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

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            argCnt = args.length;
            strDate = comcr.getBusiDate();
            
            if(argCnt==1 && args[0].length() == 8) {
            	strDate = args[0];
            	showLogMessage("I", "", String.format("輸入參數日期=[%s]", strDate));
            }
            showLogMessage("I", "", String.format("本日營業日=[%s]", strDate));
            
            selectMin2MaxDate();
            showLogMessage("I", "", String.format("取得繳稅(刷卡)期間[%s]--[%s]",minPurchaseDateS,maxPurchaseDateE));
            showLogMessage("I", "", String.format("開始讀取bil_bill資料"));
            selectBilBill();
            selectMktTaxParm();
            
            showLogMessage("I", "", String.format("取得bil_bill,筆數=[%d]", bilBillCnt2));
            showLogMessage("I", "", String.format("讀取參數檔,筆數=[%d]", totalCnt));
            showLogMessage("I", "", String.format("數據不合格,筆數=[%d]", checkFailCnt));
            commitDataBase();
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
    
    /**
     * 取得 繳稅(刷卡)期間
     * @throws Exception *********************************************************************/
    void selectMin2MaxDate() throws Exception {
    	extendField = "def_date.";
		sqlCmd = " select min(purchase_date_s) as min_purchase_date_s,max(purchase_date_e) as max_purchase_date_e ";
		sqlCmd += " from mkt_tax_parm where cal_def_date = ? ";
		setString(1,strDate);
		selectTable();
		minPurchaseDateS = getValue("def_date.min_purchase_date_s");
		maxPurchaseDateE = getValue("def_date.max_purchase_date_e");
    }

    /***********************************************************************/
	void selectMktTaxParm() throws Exception {
		extendField = "PARM.";
		sqlCmd = " select active_code, "
				+ "active_name,"
                + "active_type,"
                + "purchase_date_s,"
                + "purchase_date_e,"
                + "feedback_all_totcnt,"
                + "feedback_emp_totcnt,"
                + "feedback_nonemp_totcnt,"
                + "feedback_peremp_cnt,"
                + "feedback_pernonemp_cnt,"
                + "purchase_amt_s,"
                + "purchase_amt_e,"
                + "feedback_id_type,"   
                + "gift_type,"
                + "cal_def_date ";
		sqlCmd += " from MKT_TAX_PARM where CAL_DEF_DATE = ? ";
		setString(1,strDate);
		int recordCnt = selectTable();
		for(int i =0 ;i<recordCnt ;i++) {
			totalCnt++;
			
			activeCode = getValue("PARM.active_code",i);
			activeType = getValue("PARM.active_type",i);
			feedbackAllTotcnt = getValueInt("PARM.feedback_all_totcnt",i);
			feedbackEmpTotcnt = getValueInt("PARM.feedback_emp_totcnt",i);
			feedbackNonempTotcnt = getValueInt("PARM.feedback_nonemp_totcnt",i);
			feedbackPerempCnt = getValueInt("PARM.feedback_peremp_cnt",i);
			purchaseDateS = getValue("PARM.purchase_date_s",i);
			purchaseDateE = getValue("PARM.purchase_date_e",i);
			feedbackIdType = getValue("PARM.feedback_id_type",i);
			feedbackPernonempCnt = getValueInt("PARM.feedback_pernonemp_cnt",i);
			purchaseAmtS = getValueLong("PARM.purchase_amt_s",i);
			purchaseAmtE = getValueLong("PARM.purchase_amt_e",i);
			calDefDate = getValue("PARM.cal_def_date",i);
			giftType = getValue("PARM.gift_type",i);
			
			showLogMessage("I", "", String.format("正在處理活動代碼=[%s] ,繳稅類別=[%s] " , activeCode ,comStr.decode(activeType,",1,2,3,4", ",綜所稅,地價稅,牌照稅,房屋稅")));
			
			deleteMktTaxFbdata();
			getBilBill();
		}
	}
	
	void selectBilBill() throws Exception{
		extendField = "BIL.";
		sqlCmd = " SELECT A.PURCHASE_DATE, A.CARD_NO, A.ID_P_SEQNO, A.MCHT_NO, A.ECS_PLATFORM_KIND, "
				+ " A.MCHT_CATEGORY, A.MCHT_CHI_NAME, A.PAYMENT_TYPE, A.CASH_PAY_AMT,B.ID_NO , B.STAFF_FLAG "
				+ " FROM BIL_BILL A LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO "
				+ " JOIN BIL_FISCDTL C ON A.REFERENCE_NO = C.ECS_REFERENCE_NO "
				+ " WHERE A.ACCT_TYPE = '01' AND A.PURCHASE_DATE >= ? AND A.PURCHASE_DATE <= ? "
				+ " AND ( SUBSTR(C.REIMB_INFO ,27 ,2) = 'A1' or SUBSTR(C.REIMB_INFO ,27 ,2) = 'F1') "
				+ " ORDER BY A.PURCHASE_DATE ASC, A.CASH_PAY_AMT DESC ";
		setString(1,minPurchaseDateS);
		setString(2,maxPurchaseDateE);
		bilBillCnt = selectTable();
	}
	
	void getBilBill() throws Exception {
		String mapKey = "";
		HashMap<String,Integer[]> map = new HashMap<String,Integer[]>();
		LinkedHashMap<String,Integer[]> map2 = new LinkedHashMap<String,Integer[]>();
		for(int index=0; index<bilBillCnt;index++) {
			String mchtNo = getValue("BIL.MCHT_NO",index);
			String mchtCategory = getValue("BIL.MCHT_CATEGORY",index);
			String mchtChiName = getValue("BIL.MCHT_CHI_NAME",index);
			String paymentType = getValue("BIL.PAYMENT_TYPE",index);
			
			if(comStr.decode(activeType, ",1,2,3,4", ",95004001,95004002,95004003,95004004").equals(mchtNo)
					|| ("9311".equals(mchtCategory) 
							&& mchtChiName.indexOf(comStr.decode(activeType, ",1,2,3,4", ",綜所稅,地價稅,牌照稅,房屋稅"))>=0)
//					|| "Q".toString().equals(paymentType)
					) {
				
				bilBillCnt2++;
				
				purchaseDate = getValue("BIL.PURCHASE_DATE",index);
				staffFlag = getValue("BIL.STAFF_FLAG",index);
				cashPayAmt = getValueLong("BIL.CASH_PAY_AMT",index);
				idNo = getValue("BIL.ID_NO",index);
				cardNo = getValue("BIL.CARD_NO",index);

				if (purchaseDateS.compareTo(purchaseDate) <= 0 && purchaseDateE.compareTo(purchaseDate) >= 0
						&& cashPayAmt >= purchaseAmtS && cashPayAmt <= purchaseAmtE && feedbackAllTotcnt > 0) {
					mapKey = "1".equals(feedbackIdType) ? idNo : cardNo;
					if(comStr.empty(mapKey)) 
						continue;
					//員工
					if("Y".equals(staffFlag) && feedbackPerempCnt > 0) {
						Integer[] mapValue = new Integer[4];
						mapKey += "#Y";
						mapValue[0] = map.get(mapKey) == null ? 1 : map.get(mapKey)[0] + 1; // 張數
						if(mapValue[0] > feedbackPerempCnt) {
							continue; //到達上限張數
						}
						mapValue[1] = index;
						mapValue[2] = 0;
						mapValue[3] = mapValue[0];
						map.put(mapKey, mapValue);
						map2.put(mapKey + index, mapValue);
					}
					
					//非員工
					if(!"Y".equals(staffFlag) && feedbackPernonempCnt > 0) {
						Integer[] mapValue = new Integer[4];
						mapKey += "#N";
						mapValue[0] = map.get(mapKey) == null ? 1 : map.get(mapKey)[0] + 1; // 張數
						if(mapValue[0] > feedbackPernonempCnt) {
							continue; //到達上限張數
						}
						mapValue[1] = index;
						mapValue[2] = 1;
						mapValue[3] = mapValue[0];
						map.put(mapKey, mapValue);
						map2.put(mapKey + index, mapValue);
					}

				} else {
					checkFailCnt++;
				}
			}
		}
		procData(map2);
		map.clear();
	}
	
	
	void procData(LinkedHashMap<String,Integer[]> map) throws Exception {
		int empTotCnt = 1;
		int nonempTotCnt = 1;
		int allTotCnt = 1;
		int index = 0;
		for(String key : map.keySet()) {
			if(map.get(key)[2].intValue() == 0 && feedbackEmpTotcnt > 0) {
				if(empTotCnt++ > feedbackEmpTotcnt) {
					continue;
				}
				
			}
			if(map.get(key)[2].intValue() == 1 && feedbackNonempTotcnt > 0) {
				if(nonempTotCnt++ > feedbackNonempTotcnt) {
					continue;
				}
			}
			
			if(allTotCnt++ > feedbackAllTotcnt) {
				continue;
			}
			
			index = map.get(key)[1];
			feedbackSeqno = map.get(key)[3];
			purchaseDate = getValue("BIL.PURCHASE_DATE",index);
			staffFlag = getValue("BIL.STAFF_FLAG",index);
			cashPayAmt = getValueLong("BIL.CASH_PAY_AMT",index);
			idNo = getValue("BIL.ID_NO",index);
			idPSeqno = getValue("BIL.ID_P_SEQNO",index);
			cardNo = getValue("BIL.CARD_NO",index);
			
			insertMktTaxFbdata();
			commitDataBase();
		}
		map.clear();
	}
	
	void deleteMktTaxFbdata() throws Exception {
		daoTable = "MKT_TAX_FBDATA";
	    whereStr = "WHERE ACTIVE_CODE = ? AND CAL_DEF_DATE = ? AND ACTIVE_TYPE = ? ";
	    setString(1, activeCode);
	    setString(2, calDefDate);
	    setString(3, activeType);
	    deleteTable();
	}

	/**
	 * @throws Exception *********************************************************************/
	void insertMktTaxFbdata() throws Exception {
		extendField = "fbdata.";
		setValue("fbdata.active_code", activeCode);
		setValue("fbdata.active_type", activeType);
		setValue("fbdata.pay_yyyy", comStr.left(sysDate, 4));
		setValue("fbdata.staff_flag", staffFlag);
		setValue("fbdata.purchase_date", purchaseDate);
		setValueLong("fbdata.purchase_amt", cashPayAmt);
		setValue("fbdata.feedback_id_type", feedbackIdType);
		setValue("fbdata.id_no", idNo);
		setValue("fbdata.id_p_seqno", idPSeqno);
		setValue("fbdata.card_no", cardNo);
		setValueInt("fbdata.feedback_seqno", feedbackSeqno);
		setValue("fbdata.feedback_date", calDefDate);
		setValue("fbdata.gift_type", giftType);
		setValue("fbdata.cal_def_date", calDefDate);
		setValue("fbdata.crt_date", sysDate);
		setValue("fbdata.crt_user", javaProgram);
		setValue("fbdata.mod_time", sysDate + sysTime);
		setValue("fbdata.mod_user", javaProgram);
		setValue("fbdata.mod_pgm", javaProgram);
		setValueInt("fbdata.mod_seqno", 0);
		daoTable  = "MKT_TAX_FBDATA";

		insertTable();
		if("Y".equals(dupRecord)) {
			showLogMessage("I", "", String.format("insert mkt_tax_fbdata dupRecord ,active_code=[%s] ,active_type=[%s] , pay_yyyy=[%s] ,id_p_seqno=[%s]"
					, activeCode,activeType,comStr.left(sysDate, 4),idPSeqno));
		}
	}
	
	

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC910 proc = new MktC910();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

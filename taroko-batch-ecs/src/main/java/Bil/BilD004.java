/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112/03/01  V1.00.01    JeffKung   initial draft                            *
* 112/11/10  V1.00.02    JeffKung   消費日以繳款截止日為判斷依據置入                       *
*****************************************************************************/

package Bil;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*分期付款轉換處理程式*/
public class BilD004 extends AccessDAO {
    private String progname = "帳單分期首期入帳寫繳款檔  112/11/10 V1.00.02 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilD004";
    String hCallBatchSeqno = "";

    String hBusiOnlineDate = "";
    String hBusiBusinessDate = "";
    String hTempSysdate = "";
    
    String hApbtBatchNo = "";
    int    hApdlSerialNo = 0;
    int    hApbTotalCnt = 0;
    double hApbTotalAmt = 0;
    
    int    totalCnt = 0;

    String hModUser = "";
    String tmpstr     = "";
    
    // *******************************************************************
    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : BilD004 ", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();

            selectBilContpost();

            showLogMessage("I", "", String.format("Total process [%d] records\n", totalCnt));
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
        sqlCmd = "select online_date,";
        sqlCmd += "business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_temp_sysdate ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusiOnlineDate = getValue("online_date");
            hBusiBusinessDate = getValue("business_date");
            hTempSysdate = getValue("h_temp_sysdate");
        }
        hModUser = comc.commGetUserID();
    }

    /***********************************************************************/
    void selectBilContpost() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "b.contract_no , ";
        sqlCmd += "b.contract_seq_no, ";
        sqlCmd += "c.trans_flag, ";
        sqlCmd += "c.stmt_inst_flag ";
        sqlCmd += " from bil_contpost b, bil_merchant c ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and b.install_curr_term = 1 ";  //首期入帳
        sqlCmd += "  and b.kind_amt = '1' ";         //本金
        sqlCmd += "  and b.post_flag != 'Y' ";
        sqlCmd += "  and b.mcht_no = c.mcht_no ";
        sqlCmd += "  and (c.trans_flag = 'Y' or c.stmt_inst_flag = 'Y') "; //帳單分期或長循分期

        openCursor();
        
        while (fetchTable()) {
        	
        	//讀出分期總金額
        	if (selectBilContract()!=0) {
        		continue; //找不到原始的分期資料
        	};
        	
        	totalCnt++;
        	if (totalCnt==1) {
        		selectActPayBatch(); //取得繳款批號
        		insertActPayBatch(); //新增一個繳款批號
        		commitDataBase();
        	}
        
            insertActPayDetail();

        }
        
        if (totalCnt>0) {
        	updateActPayBatch();
        }
        
        closeCursor();
    }
    
	/**********************************************************************/
	int selectBilContract() throws Exception {

		extendField = "contract."; 
		
		sqlCmd  = "select tot_amt,p_seqno,acno_p_seqno,card_no,acct_type,purchase_date,id_p_seqno, ";
		sqlCmd += "(SELECT b.this_lastpay_date FROM ptr_workday b WHERE b.stmt_cycle = a.stmt_cycle) AS THIS_LASTPAY_DATE ";
		sqlCmd += "from bil_contract a ";
		sqlCmd += "where contract_no = ? and contract_seq_no = ? ";
		
		setString(1, getValue("contract_no"));
		setInt(2, getValueInt("contract_seq_no"));
		
		selectTable();
		
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select_bil_contract not found!, contract_no=["+getValue("contract_no")+"],seqno=["+getValue("contract_seq_no")+"]");
			return 1;
		}
		
		return 0;
	}


	/**********************************************************************/
	void selectActPayBatch() throws Exception {
		hApbtBatchNo = "";
		sqlCmd = "select ?||'9007'||substr(to_char(to_number(decode(substr(max(batch_no),13,4),null,'0000',substr(max(batch_no),13,4)))+ 1,'0000'),2,4) h_apbt_batch_no ";
		sqlCmd += "from act_pay_batch  ";
		sqlCmd += "where batch_no like ?||'9007%'";
		setString(1, hBusiBusinessDate);
		setString(2, hBusiBusinessDate);
		int tmpInt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_act_pay_batch not found!", "", ""); // 除非DB有問題,不然不可能會發生
		}
		
		if (tmpInt > 0) {
			hApbtBatchNo = getValue("h_apbt_batch_no");
		}
	}
	
	void insertActPayBatch() throws Exception {

		setValue("batch_no", hApbtBatchNo);
		setValueInt("batch_tot_cnt", 0);
		setValueDouble("batch_tot_amt", 0);
		setValue("crt_user", hModUser);
		setValue("crt_date", hBusiBusinessDate);
		setValue("crt_time", sysTime);
		setValue("trial_user", hModUser);
		setValue("trial_date", hBusiBusinessDate);
		setValue("trial_time", sysTime);
		setValue("confirm_user", hModUser);
		setValue("confirm_date", hBusiBusinessDate);
		setValue("confirm_time", sysTime);
		setValue("curr_code", "901");
		setValueDouble("dc_pay_amt", 0);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		daoTable = "act_pay_batch";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_pay_batch duplicate", "", hApbtBatchNo);
		}
		
	}


    /***********************************************************************/
    void insertActPayDetail() throws Exception {
      if ("Y".equals(getValue("stmt_inst_flag"))) {
        setValue("payment_type", "DEBT");
      } else {
        setValue("payment_type", "LOAN");
      }
     
      setValue("batch_no", hApbtBatchNo);
      setValue("serial_no", String.format("%05d", ++hApdlSerialNo));
      setValue("p_seqno", getValue("contract.p_seqno"));
      setValue("acno_p_seqno", getValue("contract.acno_p_seqno"));
      setValue("acct_type", getValue("contract.acct_type"));
      setValue("id_p_seqno", getValue("contract.id_p_seqno"));
      setValue("pay_card_no", getValue("contract.card_no"));
      if (getValue("contract.purchase_date").compareTo(getValue("contract.this_lastpay_date")) > 0) {
    	  setValue("pay_date", getValue("contract.this_lastpay_date"));
      } else {
    	  setValue("pay_date", getValue("contract.purchase_date")); 
      }
      setValue("curr_code", "901");
      setValueDouble("pay_amt", getValueDouble("contract.tot_amt"));
      setValueDouble("dc_pay_amt", getValueDouble("contract.tot_amt"));
      setValue("crt_user", hModUser);
      setValue("crt_date", hBusiBusinessDate);
      setValue("crt_time", sysTime);
      setValue("mod_user", hModUser);
      setValue("mod_time", sysDate + sysTime);
      setValue("mod_pgm", prgmId);
      daoTable = "act_pay_detail";
      insertTable();
      if (dupRecord.equals("Y")) {
        comcr.errRtn("insert_act_pay_detail duplicate", hApbtBatchNo + "," + hApdlSerialNo,
            comcr.hCallBatchSeqno);
      }
      
		hApbTotalCnt++;
		hApbTotalAmt += getValueDouble("contract.tot_amt");
		
    }    

    /**********************************************************************/
	void updateActPayBatch() throws Exception {
		
		daoTable = "act_pay_batch";
		updateSQL = "batch_tot_cnt=? , batch_tot_amt=? , dc_pay_amt=? ";
		whereStr = "where batch_no = ? ";
		
		setInt(1, hApbTotalCnt);
		setDouble(2,hApbTotalAmt);
		setDouble(3,hApbTotalAmt);
		setString(4, hApbtBatchNo);
		int tmpInt = updateTable();
		
		if (notFound.equals("Y")) {
			comcr.errRtn("update_act_pay_batch not found!", "", "hApbtBatchNo"); // 除非DB有問題,不然不可能會發生
		}
	}
	
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilD004 proc = new BilD004();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}

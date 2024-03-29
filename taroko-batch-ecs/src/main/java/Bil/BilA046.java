/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/06/06  V1.00.00  ryan        program initial                           *
 *  112/07/19  V1.00.01  JeffKung    add bookingDate&bookingTime               *
 ******************************************************************************/

package Bil;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class BilA046 extends AccessDAO {
    private String progname = "批次寫入簡訊發送檔處理程式  112/07/19 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommString comStr = new CommString();
    CommDate comDate = new CommDate();
    CommCrdRoutine comcr = null;
    private int totalCnt = 0;
    private int argCnt = 0;
    private String strDate = "";
    private String businessDate = "";

    private String msgDept = "";
    private String msgUserid = "";
    private String msgDesc = "";
    
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
            businessDate = comcr.getBusiDate();
            showLogMessage("I", "", String.format("本日營業日=[%s]", businessDate));
            
            if(argCnt==1 && args[0].length() == 8) {
            	businessDate = args[0];
            	showLogMessage("I", "", String.format("輸入參數日期(營業日)=[%s]", businessDate));
            }

            
            strDate = comDate.dateAdd(businessDate, 0, 0, -1);            

            showLogMessage("I", "", String.format("取得營業日前一日=[%s]", strDate));
            
            //讀取簡訊參數檔
            if(selectSmsMsgId()!=0) {
            	showLogMessage("I", "", String.format("無法取得簡訊參數檔sms_msg_id"));
            	return 0;
            }
            
            selectBilContract();

            showLogMessage("I", "", String.format("程式執行結束,筆數=[%d]", totalCnt));
           
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

	/*******************************************************************************************/
	void selectBilContract() throws Exception{
		   sqlCmd  = "select a.new_proc_date  ";
		   sqlCmd += "     , case when a.mcht_no in ('106000000001','106000000002','106000000003','106000000004','106000000005','106000000006','106000000007','106000000008','106000000009') ";
		   sqlCmd += "       then a.mcht_no else a.ptr_mcht_no end as h_mcht_no  ";
		   sqlCmd += "     , a.tot_amt ,a.id_p_seqno ,a.p_seqno ,b.id_no ,a.acct_type, a.card_no ";
		   sqlCmd += " ,b.cellar_phone ,decode(b.cellar_phone,'','N','Y') as cellphone_check_flag ";
		   sqlCmd += " ,b.chi_name ";
		   sqlCmd += "  from bil_contract a left join crd_idno b on a.ID_P_SEQNO = b.ID_P_SEQNO ";
		   sqlCmd += " where a.new_proc_date = ?  ";
		   sqlCmd += "   and a.tot_amt       > 0         ";
		   sqlCmd += "   and (a.mcht_no      like '1060000000%'  or ";
		   sqlCmd += "        a.ptr_mcht_no  like '1060000000%') ";
		   sqlCmd += " order by a.new_proc_date,h_mcht_no  ";
		   setString(1, strDate);

		this.openCursor();
		while (fetchTable()) {
			totalCnt++;
			SmsMsgDtl msgDtl =	getMsgDtlData();
			insertSmsMsgDtl(msgDtl);
		}
		
		this.closeCursor();
	}
	
	/**
	 * @throws Exception *****************************************************************************************/
	void getsMsgSeqno(SmsMsgDtl msgDtl) throws Exception {
		extendField = "seqno.";
		sqlCmd = " select lpad(to_char(ecs_modseq.nextval),10,'0') as sms_seqno from SYSIBM.SYSDUMMY1 ";
		selectTable();
		msgDtl.msgSeqno =  getValue("seqno.sms_seqno");
	}
	
	/**
	 * @throws Exception *****************************************************************************************/
	int selectSmsMsgId() throws Exception {
		extendField = "msg_id.";
		sqlCmd = " select msg_dept,msg_userid,msg_id,msg_desc from sms_msg_id where msg_pgm = 'BILA046' ";
		int n = selectTable();
		if(n > 0) {
			msgDept = getValue("msg_id.msg_dept");
			msgUserid = getValue("msg_id.msg_userid");
			msgDesc = getValue("msg_id.msg_desc");
			return 0;
		}
		return 1;
	}
	
	
	SmsMsgDtl getMsgDtlData() throws Exception {
		SmsMsgDtl msgDtl = new SmsMsgDtl();
		//取得簡訊流水號
		getsMsgSeqno(msgDtl);
		msgDtl.msgDept = msgDept;
		msgDtl.msgUserid = msgUserid;
		msgDtl.msgId = "106000000007".equals(getValue("h_mcht_no"))?"2602":"2601";
		msgDtl.msgPgm = "106000000007".equals(getValue("h_mcht_no"))?"BILA046-1":"BILA046";
		msgDtl.idPSeqno = getValue("id_p_seqno");
		msgDtl.pSeqno = getValue("p_seqno");
		msgDtl.idNo = getValue("id_no");
		msgDtl.acctType = getValue("acct_type");
		msgDtl.cardNo = getValue("card_no");
		msgDtl.cellarPhone = getValue("cellar_phone");
		msgDtl.cellphoneCheckFlag = getValue("cellphone_check_flag");
		msgDtl.chiName = getValue("chi_name");
		msgDtl.exId = "";
		//msgDtl.msgDesc = msgDesc;
		msgDtl.msgDesc = "";
		msgDtl.minPay = 0;
		msgDtl.addMode = "B";
		msgDtl.resendFlag = "N";
		msgDtl.sendFlag = "N";
		msgDtl.priorFlag = "N";
		msgDtl.createTxtDate = "";
		msgDtl.createTxtTime = "";
		msgDtl.chiNameFlag = "";
		msgDtl.procFlag = "N";
		msgDtl.sms24Flag = "";
		msgDtl.bookingDate = businessDate;
		msgDtl.bookingTime = "140000";
				
		return msgDtl;
	}
	
	/**
	 * @throws Exception *********************************************************************/
	void insertSmsMsgDtl(SmsMsgDtl msgDtl) throws Exception {
		extendField = "msg_dtl.";
		setValue("msg_dtl.MSG_SEQNO", msgDtl.msgSeqno);
		setValue("msg_dtl.MSG_DEPT", msgDtl.msgDept);
		setValue("msg_dtl.MSG_USERID", msgDtl.msgUserid);
		setValue("msg_dtl.MSG_PGM", msgDtl.msgPgm);
		setValue("msg_dtl.ID_P_SEQNO", msgDtl.idPSeqno);
		setValue("msg_dtl.P_SEQNO", msgDtl.pSeqno);
		setValue("msg_dtl.ID_NO", msgDtl.idNo);
		setValue("msg_dtl.ACCT_TYPE", msgDtl.acctType);
		setValue("msg_dtl.CARD_NO", msgDtl.cardNo);
		setValue("msg_dtl.MSG_ID", msgDtl.msgId);
		setValue("msg_dtl.CELLAR_PHONE", msgDtl.cellarPhone);
		setValue("msg_dtl.CELLPHONE_CHECK_FLAG", msgDtl.cellphoneCheckFlag);
		setValue("msg_dtl.CHI_NAME", msgDtl.chiName);
		setValue("msg_dtl.EX_ID", msgDtl.exId);
		setValue("msg_dtl.MSG_DESC", msgDtl.msgDesc);
		setValueDouble("msg_dtl.MIN_PAY", msgDtl.minPay);
		setValue("msg_dtl.ADD_MODE", msgDtl.addMode);
		setValue("msg_dtl.RESEND_FLAG", msgDtl.resendFlag);
		setValue("msg_dtl.SEND_FLAG", msgDtl.sendFlag);
		setValue("msg_dtl.PRIOR_FLAG", msgDtl.priorFlag);
		setValue("msg_dtl.CREATE_TXT_DATE", msgDtl.createTxtDate);
		setValue("msg_dtl.CREATE_TXT_TIME", msgDtl.createTxtTime);
		setValue("msg_dtl.CHI_NAME_FLAG", msgDtl.chiNameFlag);
		setValue("msg_dtl.PROC_FLAG", msgDtl.procFlag);
		setValue("msg_dtl.SMS24_FLAG", msgDtl.sms24Flag);
		setValue("msg_dtl.crt_date", sysDate);
		setValue("msg_dtl.crt_user", javaProgram);
		setValue("msg_dtl.APR_DATE", sysDate);
		setValue("msg_dtl.APR_USER", "system");
		setValue("msg_dtl.APR_FLAG", "Y");
		setValue("msg_dtl.mod_time", sysDate + sysTime);
		setValue("msg_dtl.mod_user", javaProgram);
		setValue("msg_dtl.mod_pgm", javaProgram);
		setValue("msg_dtl.booking_date", msgDtl.bookingDate);
		setValue("msg_dtl.booking_time", msgDtl.bookingTime);
		setValueInt("msg_dtl.mod_seqno", 0);
		daoTable  = "sms_msg_dtl";

		insertTable();
		
	}
	
	

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA046 proc = new BilA046();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

 class SmsMsgDtl {
		String msgSeqno = "";
		String msgDept = "";
		String msgUserid = "";
		String msgPgm = "";
		String idPSeqno = "";
		String pSeqno = "";
		String idNo = "";
		String acctType = "";
		String cardNo = "";
		String msgId = "";
		String cellarPhone = "";
		String cellphoneCheckFlag = "";
		String chiName = "";
		String exId = "";
		String msgDesc = "";
		double minPay = 0;
		String addMode = "";
		String resendFlag = "";
		String sendFlag = "";
		String priorFlag = "";
		String createTxtDate = "";
		String createTxtTime = "";
		String chiNameFlag = "";
		String procFlag = "";
		String sms24Flag = "";
		String bookingDate = "";
		String bookingTime = "";
 }

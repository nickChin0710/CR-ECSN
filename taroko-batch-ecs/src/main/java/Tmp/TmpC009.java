/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/12/05  V1.00.01   Sunny     program initial                           *
*****************************************************************************/
package Tmp;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxInf;

public class TmpC009 extends AccessDAO {
	private final String progname = "催收--處理一次性異動資料程式  112/12/05 V1.00.01";

	CommCrd comc = new CommCrd();
	CommFunction comm = new CommFunction();
	CommCrdRoutine comcr = null;

	long   hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    
    int totalCnt=0;
    int updCnt = 0;
    
    String hBusinessDate = "";
    String hSystemDate = "";
    String hProcFlag = "";
    
    String hCorpPSeqno="";
    String hAcnoPSeqno="";
    String hAcctkey="";
    String hPayByStageFlag="";
    String hCorpNo="";

    // *********************************************************
    public int mainProcess(String[] args)
    {
        try
        {
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            
            if (args.length > 2) {
                comc.errExit("Usage : TmpC009 [business_date] [proc_flag]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            if(args.length == 2)
            {
                if(args[0].length() == 8)
                {
                    hBusinessDate = args[0];
                }
                
                if(args[1].length() == 1) {
                	hProcFlag = args[1];
                }
            } else {
            	return 0;
            }
            showLogMessage("I", "", "Process_date = " + hBusinessDate + ", Proc_flag=" + hProcFlag);

            if ("0".equals(hProcFlag) || "1".equals(hProcFlag)) {
            	int updCnt = updateColLiacDebt();
            	showLogMessage("I", "", String.format("1.異動updateColLiacDebt總筆數=[%d]", updCnt));
            }
            
            if ("0".equals(hProcFlag) || "2".equals(hProcFlag)) {
            	int updCnt = updateColLiacRemod();
            	showLogMessage("I", "", String.format("2.異動updateColLiacRemod總筆數=[%d]", updCnt));
            }
            
            if ("0".equals(hProcFlag) || "3".equals(hProcFlag)) {            	
            	selectActAcnoNego1();
            	showLogMessage("I", "", String.format("3.公會個人補acno狀態--讀取筆數[%d],異動總筆數=[%d]", totalCnt,updCnt));
            }
            
            if ("0".equals(hProcFlag) || "4".equals(hProcFlag)) {            	
            	selectActAcnoNego2();
            	showLogMessage("I", "", String.format("4.個協個人補acno狀態--讀取筆數[%d],異動總筆數=[%d]", totalCnt,updCnt));
            }
            
            if ("0".equals(hProcFlag) || "5".equals(hProcFlag)) {            	
            	selectActAcnoNego3();
            	showLogMessage("I", "", String.format("5.前調個人補acno狀態--讀取筆數[%d],異動總筆數=[%d]", totalCnt,updCnt));
            }
            
            if ("0".equals(hProcFlag) || "6".equals(hProcFlag)) {            	
            	selectActAcnoNego4();
            	showLogMessage("I", "", String.format("6.個協公司補acno狀態--讀取筆數[%d],異動總筆數=[%d]", totalCnt,updCnt));
            }
//            
//            if ("0".equals(hProcFlag) || "3".equals(hProcFlag)) {
//            	int updCnt = updateBilCurpost();
//            	showLogMessage("I", "", String.format("3.異動CurrPostFlag總筆數=[%d]", updCnt));
//            }
//            
//            if ("0".equals(hProcFlag) || "4".equals(hProcFlag)) {
//            	int updCnt = updateBilContract();
//            	showLogMessage("I", "", String.format("4.異動bilContract總筆數=[%d]", updCnt));
//            }

            
            finalProcess();
            return 0;
        } catch (Exception ex)
        { expMethod = "mainProcess"; expHandle(ex); return exceptExit; }
    }
    
    /***********************************************************************/
    void commonRtn() throws Exception
    {
        sqlCmd = "select business_date,to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += "  from ptr_businday ";
        int recordCnt = selectTable();
        if(recordCnt > 0) {
            hBusinessDate = getValue("business_date");
            hSystemDate   = getValue("h_system_date");
        }

        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }
    
    /***********************************************************************/
    /*前置協商--轉檔資料處理完成後,更新為[2.已回報債權],不再重複處理*/
    
    int updateColLiacDebt() throws Exception
    {
    	int updCnt = 0;

        sqlCmd  = "update col_liac_debt ";
        sqlCmd += "set proc_date  = ?, proc_flag  = '2', mod_pgm = ?, mod_time   = sysdate ";
        sqlCmd += "where proc_flag in ('0','1','R') ";
        /* '0':default '1':已計算 '2':已產生債權回報媒體  (0.資料轉入,1.待報送,2.已報送,A.不須報送,R.待處理)*/
 
        setString(1, hBusinessDate);
        setString(2, javaProgram);

        updCnt = updateTable();

        return updCnt;
        
    }
        
    
    /***********************************************************************/
    int updateColLiacRemod() throws Exception {
    	
    	int updCnt = 0;
    	
        daoTable = "col_liac_remod";
        updateSQL = "proc_date = ?, ";
        updateSQL += "proc_flag = 'Y', ";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where proc_flag ='N' ";
        setString(1, hBusinessDate);
        setString(2, javaProgram);

        updCnt = updateTable();

        return updCnt;
    }
    
    /***********************************************************************/       
    /*公會協商--個人*/	
    private void selectActAcnoNego1() throws Exception { 

        updCnt=0;
		
    	fetchExtend = "acno.";
		selectSQL = "a.acno_p_seqno, a.p_seqno, a.id_p_seqno, a.acct_type,a.acct_key,pay_by_stage_flag ";
		daoTable = "act_acno a,act_acct d ";
		whereStr = "where 1=1 "
				+ "and a.p_seqno = d.p_seqno "
				+ "and a.acct_type='01' "
                + "and a.id_p_seqno in (select cpbdue_id_p_seqno from col_cpbdue "
		        + "where cpbdue_acct_type='01' and cpbdue_type='1' and cpbdue_curr_type in ('1','2','3')) "
		        + "and not exists (select 1 from crd_card c where c.p_seqno=a.p_seqno and a.id_p_seqno=c.major_id_p_seqno and c.acct_type='01' and c.current_code='0') ";

		openCursor();	
		
		while (fetchTable()) {
            hAcnoPSeqno = getValue("acno.acno_p_seqno");
            hAcctkey = getValue("acno.acct_key");
            hPayByStageFlag = getValue("acno.pay_by_stage_flag");
            totalCnt++;
            
            showLogMessage("I", "", String.format("公會-個人，Acctkey[%s],AcnoPSeqno[%s],PayByStageFlag[%s]", hAcctkey,hAcnoPSeqno,hPayByStageFlag));
            
            updateAcnoNego1(hAcnoPSeqno);
    	 }
        closeCursor();
    	}
	    /***********************************************************************/
        /*補註記
         * 1.不列印對帳單
         * 2.暫不電催
         * 3.暫不發催收簡訊
         * 4.暫不轉逾放
         * 5.暫不轉催收
         */
    
	    int updateAcnoNego1(String hAcnoPSeqno) throws Exception
	    {	    		    	
	        sqlCmd  = "update act_acno ";
	        sqlCmd += " set stat_unprint_flag = 'Y', stat_unprint_s_month = substring(?,1,6), stat_unprint_e_month = '999912', ";
	        sqlCmd += " no_tel_coll_flag = 'Y', no_tel_coll_s_date = ?, no_tel_coll_e_date='999912', ";
	        sqlCmd += " no_sms_flag = 'Y', no_sms_s_date = ?, no_sms_e_date='999912', ";
	        sqlCmd += " no_delinquent_flag = 'Y', no_delinquent_s_date = ?, no_delinquent_e_date='999912', ";
	        sqlCmd += " no_collection_flag = 'Y', no_collection_s_date = ?, no_collection_e_date='999912', ";
	        sqlCmd += " mod_pgm = ?, mod_time = sysdate ";
	        sqlCmd += " where 1=1 ";
	        sqlCmd += " and p_seqno = ? and acno_flag='1' ";
	        
	        setString(1, hBusinessDate);
	        setString(2, hBusinessDate);
	        setString(3, hBusinessDate);
	        setString(4, hBusinessDate);
	        setString(5, hBusinessDate);
	        setString(6, javaProgram);
	        setString(7, hAcnoPSeqno);
	        
	        updateTable();
	        
//	        showLogMessage("I", "", String.format(updCnt + "-updateAcnoNego1 -- Acctkey[%s],AcnoPSeqno[%s]", hAcctkey,hAcnoPSeqno));
	
	        return updCnt++;
	        
	    }
	   
	    /***********************************************************************/
	    /*個協--個人*/
    	private void selectActAcnoNego2() throws Exception { 

        updCnt=0;
		
    	fetchExtend = "acno.";
		selectSQL = "a.acno_p_seqno, a.p_seqno, a.id_p_seqno, a.acct_type,a.acct_key,pay_by_stage_flag ";
		daoTable = "act_acno a,act_acct d ";
		whereStr = "where 1=1 "
				+ "and a.p_seqno = d.p_seqno "
				+ "and a.acct_type='01' "
                + "and a.id_p_seqno in (select cpbdue_id_p_seqno from col_cpbdue "
		        + "where cpbdue_acct_type='01' and cpbdue_type='2' and cpbdue_curr_type in ('1','2','3')) "
		        + "and not exists (select 1 from crd_card c where c.p_seqno=a.p_seqno and a.id_p_seqno=c.major_id_p_seqno and c.acct_type='01' and c.current_code='0') ";

		openCursor();	
		
		while (fetchTable()) {
            hAcnoPSeqno = getValue("acno.acno_p_seqno");
            hAcctkey = getValue("acno.acct_key");
            hPayByStageFlag = getValue("acno.pay_by_stage_flag");
            totalCnt++;
            
            showLogMessage("I", "", String.format("個協--個人，Acctkey[%s],AcnoPSeqno[%s],PayByStageFlag[%s]", hAcctkey,hAcnoPSeqno,hPayByStageFlag));
            
            updateAcnoNego2(hAcnoPSeqno);
    	 }
        closeCursor();
    	}
	    /***********************************************************************/
        /*補註記
         * 1.不列印對帳單
         * 2.暫不電催
         * 3.暫不發催收簡訊
         * 4.暫不轉逾放
         * 5.暫不轉催收
         */
    
	    int updateAcnoNego2(String hAcnoPSeqno) throws Exception
	    {	    		    	
	        sqlCmd  = "update act_acno ";
	        sqlCmd += " set stat_unprint_flag = 'Y', stat_unprint_s_month = substring(?,1,6), stat_unprint_e_month = '999912', ";
	        sqlCmd += " no_tel_coll_flag = 'Y', no_tel_coll_s_date = ?, no_tel_coll_e_date='999912', ";
	        sqlCmd += " no_sms_flag = 'Y', no_sms_s_date = ?, no_sms_e_date='999912', ";
	        sqlCmd += " no_delinquent_flag = 'Y', no_delinquent_s_date = ?, no_delinquent_e_date='999912', ";
	        sqlCmd += " no_collection_flag = 'Y', no_collection_s_date = ?, no_collection_e_date='999912', ";
	        sqlCmd += " mod_pgm = ?, mod_time = sysdate ";
	        sqlCmd += " where 1=1 ";
	        sqlCmd += " and p_seqno = ? and acno_flag='1' ";

	        
	        setString(1, hBusinessDate);
	        setString(2, hBusinessDate);
	        setString(3, hBusinessDate);
	        setString(4, hBusinessDate);
	        setString(5, hBusinessDate);
	        setString(6, javaProgram);
	        setString(7, hAcnoPSeqno);
	        
	        updateTable();
	        	
	        return updCnt++;
	        
	    }
	    
	    /***********************************************************************/
	    /*前調--個人*/
    	private void selectActAcnoNego3() throws Exception { 

        updCnt=0;
		
    	fetchExtend = "acno.";
		selectSQL = "a.acno_p_seqno, a.p_seqno, a.id_p_seqno, a.acct_type,a.acct_key,pay_by_stage_flag ";
		daoTable = "act_acno a,act_acct d ";
		whereStr = "where 1=1 "
				+ "and a.p_seqno = d.p_seqno "
				+ "and a.acct_type='01' "
                + "and a.id_p_seqno in (select cpbdue_id_p_seqno from col_cpbdue "
		        + "where cpbdue_acct_type='01' and cpbdue_type='3' and cpbdue_curr_type in ('1','2','3')) "
		        + "and not exists (select 1 from crd_card c where c.p_seqno=a.p_seqno and a.id_p_seqno=c.major_id_p_seqno and c.acct_type='01' and c.current_code='0') ";

		openCursor();	
		
		while (fetchTable()) {
            hAcnoPSeqno = getValue("acno.acno_p_seqno");
            hAcctkey = getValue("acno.acct_key");
            hPayByStageFlag = getValue("acno.pay_by_stage_flag");
            totalCnt++;
            
            showLogMessage("I", "", String.format("個協--個人，Acctkey[%s],AcnoPSeqno[%s],PayByStageFlag[%s]", hAcctkey,hAcnoPSeqno,hPayByStageFlag));
            
            updateAcnoNego3(hAcnoPSeqno);
    	 }
        closeCursor();
    	}
	    /***********************************************************************/
        /*補註記
         * 1.不列印對帳單
         * 2.暫不電催
         * 3.暫不發催收簡訊
         * 4.暫不轉逾放
         * 5.暫不轉催收
         */
    
	    int updateAcnoNego3(String hAcnoPSeqno) throws Exception
	    {	    		    	
	        sqlCmd  = "update act_acno ";
	        sqlCmd += " set stat_unprint_flag = 'Y', stat_unprint_s_month = substring(?,1,6), stat_unprint_e_month = '999912', ";
	        sqlCmd += " no_tel_coll_flag = 'Y', no_tel_coll_s_date = ?, no_tel_coll_e_date='999912', ";
	        sqlCmd += " no_sms_flag = 'Y', no_sms_s_date = ?, no_sms_e_date='999912', ";
	        sqlCmd += " no_delinquent_flag = 'Y', no_delinquent_s_date = ?, no_delinquent_e_date='999912', ";
	        sqlCmd += " no_collection_flag = 'Y', no_collection_s_date = ?, no_collection_e_date='999912', ";
	        sqlCmd += " mod_pgm = ?, mod_time = sysdate ";
	        sqlCmd += " where 1=1 ";
	        sqlCmd += " and p_seqno = ? and acno_flag='1' ";

	        
	        setString(1, hBusinessDate);
	        setString(2, hBusinessDate);
	        setString(3, hBusinessDate);
	        setString(4, hBusinessDate);
	        setString(5, hBusinessDate);
	        setString(6, javaProgram);
	        setString(7, hAcnoPSeqno);
	        
	        updateTable();
	        	
	        return updCnt++;
	        
	    }
	   
	    /***********************************************************************/
	    /*個協--公司*/
    	private void selectActAcnoNego4() throws Exception { 

        updCnt=0;
		
    	fetchExtend = "acno.";
		selectSQL = "c.corp_no,a.acno_p_seqno, a.corp_p_seqno,a.p_seqno, a.id_p_seqno, a.acct_type,a.acct_key,pay_by_stage_flag ";
		daoTable = "act_acno a,act_acct d,crd_corp c ";
		whereStr = "where 1=1 "
				+ "and a.p_seqno = d.p_seqno "
				+ "and a.corp_p_seqno = c.corp_p_seqno "
				+ "and a.acct_type='03' "
                + "and a.corp_p_seqno in (select cpbdue_id_p_seqno from col_cpbdue "
		        + "where cpbdue_acct_type='03' and cpbdue_type='2' and cpbdue_curr_type in ('1','2','3')) "
		        + "and not exists (select 1 from crd_card c where c.p_seqno=a.p_seqno and a.corp_p_seqno=c.corp_p_seqno and c.acct_type='03' and c.current_code='0') ";

		openCursor();	
		
		while (fetchTable()) {
			hCorpNo = getValue("acno.corp_no");
            hAcnoPSeqno = getValue("acno.acno_p_seqno");
            hCorpPSeqno = getValue("acno.corp_p_seqno");
            hAcctkey = getValue("acno.acct_key");
            hPayByStageFlag = getValue("acno.pay_by_stage_flag");
            totalCnt++;
            
            showLogMessage("I", "", String.format("個協--公司，Acctkey[%s],CorpPSeqno[%S],AcnoPSeqno[%s],PayByStageFlag[%s]", hAcctkey,hCorpPSeqno,hAcnoPSeqno,hPayByStageFlag));
            
            updateAcnoNego4(hCorpPSeqno);
    	 }
        closeCursor();
    	}
	    /***********************************************************************/
        /*補註記
         * 1.不列印對帳單
         * 2.暫不電催
         * 3.暫不發催收簡訊
         * 4.暫不轉逾放
         * 5.暫不轉催收
         */
    
	    int updateAcnoNego4(String hCorpPSeqno) throws Exception
	    {	    		    	
	        sqlCmd  = "update act_acno ";
	        sqlCmd += " set stat_unprint_flag = 'Y', stat_unprint_s_month = substring(?,1,6), stat_unprint_e_month = '999912', ";
	        sqlCmd += " no_tel_coll_flag = 'Y', no_tel_coll_s_date = ?, no_tel_coll_e_date='999912', ";
	        sqlCmd += " no_sms_flag = 'Y', no_sms_s_date = ?, no_sms_e_date='999912', ";
	        sqlCmd += " no_delinquent_flag = 'Y', no_delinquent_s_date = ?, no_delinquent_e_date='999912', ";
	        sqlCmd += " no_collection_flag = 'Y', no_collection_s_date = ?, no_collection_e_date='999912', ";
	        sqlCmd += " mod_pgm = ?, mod_time = sysdate ";
	        sqlCmd += " where 1=1 ";
	        sqlCmd += " and corp_p_seqno = ? and acno_flag in ('2','3') and acct_type='03' ";

	        
	        setString(1, hBusinessDate);
	        setString(2, hBusinessDate);
	        setString(3, hBusinessDate);
	        setString(4, hBusinessDate);
	        setString(5, hBusinessDate);
	        setString(6, javaProgram);
	        setString(7, hCorpPSeqno);
	        
	        updateTable();
	        	
	        return updCnt++;
	        
	    }
	      
	    /***********************************************************************/
	public static void main(String[] args) {
		TmpC009 proc = new TmpC009();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}

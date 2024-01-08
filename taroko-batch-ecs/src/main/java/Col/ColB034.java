/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/11/25  V1.00.00    phopho     program initial                          *
*  109/12/13  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import java.text.Normalizer;

import com.*;

public class ColB034 extends AccessDAO {
    private String progname = "更生清算進度統計處理程式  109/12/13  V1.00.02   ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String hStatProcMonth = "";
    String hBusiBusinessDate = "";
    String hUserId = "";
    
    String hCdrlDbType = "";
    String hCdrlIdPSeqno = "";
    String hCdrlDbRecvDate = "";
    
    String Array1[][] = new String[10][8];
    String Array2[][] = new String[10][8];
    
    int    totalCnt = 0;
    
    public int mainProcess(String[] args) {
        try {
        	dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            
            if (comm.isAppActive(javaProgram)) {
            	exceptExit = 0;
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            
            // 檢查參數
            if (args.length != 2 && args.length != 3) {
                comc.errExit("Usage : ColB034 yyyymm user [batch_seqno]", "");
            }
            
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            //online call batch 時須記錄
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(0, 0, 0);
            
            String sGArgs1 = "";
            sGArgs1 = args[0];
            sGArgs1 = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);
            hStatProcMonth = sGArgs1;
            
        	String sGArgs2 = "";
            sGArgs2 = args[1];
            sGArgs2 = Normalizer.normalize(sGArgs2, java.text.Normalizer.Form.NFKD);
            hUserId = sGArgs2;
            if (hUserId.equals("")) hUserId = "ECS";
            
            initArray();
            selectPtrBusinday();
            
            selectColLiadRenewLiquidate();
            if (totalCnt == 0) {
            	comcr.hCallErrorDesc = "統計年月："+ hStatProcMonth +"，查無資料。";
            	if (comcr.hCallBatchSeqno.length() == 20)
                	comcr.callbatch(1, 0, 0);
            	
            	exceptExit = 0;
            	comcr.errRtn(comcr.hCallErrorDesc, "", hCallBatchSeqno);
            }
            
            deleteColLiadStatusMm();
            
            for (int ii = 0; ii < 10; ii++) {
                insertColLiadStatusMm1(ii);
            }
            for (int ii = 0; ii < 10; ii++) {
                insertColLiadStatusMm2(ii);
            }


            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
            	comcr.callbatch(1, 0, 0);
            
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
        sqlCmd = "select business_date ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }
    
    /***********************************************************************/
    void deleteColLiadStatusMm() throws Exception {
        daoTable = "col_liad_status_mm";
        whereStr = "WHERE static_ym = ? ";
        setString(1, hStatProcMonth);
        deleteTable();
    }
    
    /***********************************************************************/
    void initArray() throws Exception {
    	for (int i = 0; i < 10; i++) {
    		for (int j = 0; j < 8; j++) {
                Array1[i][j] = "";
                Array2[i][j] = "";
            }
        }
    }
    
    /***********************************************************************/
    void selectColLiadRenewLiquidate() throws Exception {
    	String startDate="", endDate = "";
    	startDate = hStatProcMonth + "01";
    	endDate   = hStatProcMonth + "31";

    	sqlCmd = " select ";
    	sqlCmd += " '1' db_type, ";
    	sqlCmd += " id_p_seqno, ";
        sqlCmd += " max(recv_date) db_recv_date ";
        sqlCmd += " from  col_liad_renew ";
        sqlCmd += " where recv_date between ? and ? ";
        sqlCmd += " group by id_p_seqno ";
        sqlCmd += " UNION ";
        sqlCmd += " select ";
    	sqlCmd += " '2', ";
    	sqlCmd += " id_p_seqno, ";
        sqlCmd += " max(recv_date) ";
        sqlCmd += " from  col_liad_liquidate ";
        sqlCmd += " where recv_date between ? and ? ";
        sqlCmd += " group by id_p_seqno ";
        sqlCmd += " order by db_type ";
        setString(1, startDate);
        setString(2, endDate);
        setString(3, startDate);
        setString(4, endDate);
        
        totalCnt=0;
        
        openCursor();
        while (fetchTable()) {
        	hCdrlDbType = getValue("db_type");
        	hCdrlIdPSeqno = getValue("id_p_seqno");
        	hCdrlDbRecvDate = getValue("db_recv_date");
        	
            selectColLiadStatusMm();
        }
        closeCursor();
    }
    
    
    /***********************************************************************/
	void selectColLiadStatusMm() throws Exception {
		/*-debt_amt-
		acct_status='1'、'2'時sum(debt_amt)，
		acct_status='3'時sum(demand_amt)，
		acct_status='4'時sum(bad_debt_amt)，金額才會正確
		*/
//		Array1[ls_status][] = {
//				0:static_ym, 
//				1:data_type,
//				2:data_status,
//				3:tol_cnt,
//				4:org_debt_amt,
//				5:org_debt_amt_bef,
//				6:bad_debt_amt,
//				7:payoff_allocate_amt
//		}

		String lsStatus="";
		int liStatus=0;
		double lmAmt=0;
		
		if (hCdrlDbType.equals("1")) {
			sqlCmd = "select renew_status, ";
			sqlCmd += " nvl(decode(acct_status,'1',debt_amt,'2',debt_amt,";
			sqlCmd += " '3',demand_amt,'4',bad_debt_amt,0),0) db_debt_amt ";
			sqlCmd += "from col_liad_renew where id_p_seqno = ? and recv_date = ? ";
			sqlCmd += "	fetch first 1 row only ";
			setString(1, hCdrlIdPSeqno);
			setString(2, hCdrlDbRecvDate);
			
			extendField = "col_liad_status_mm_1.";
			
			int selectCnt = selectTable();
			if (selectCnt > 0) {
				lsStatus = getValue("col_liad_status_mm_1.renew_status");
				lmAmt = getValueDouble("col_liad_status_mm_1.db_debt_amt");
				
				liStatus = comcr.str2int(lsStatus);
				if (Array1[liStatus][0].equals("")) {
					Array1[liStatus][0] = hStatProcMonth;
					Array1[liStatus][1] = "1";
					Array1[liStatus][2] = lsStatus;
					Array1[liStatus][3] = "0";
					Array1[liStatus][4] = "0";
				}
				Array1[liStatus][3] = String.format("%d", (comcr.str2int(Array1[liStatus][3])+1));
				Array1[liStatus][4] = String.format("%f", (comcr.str2double(Array1[liStatus][4])+lmAmt));
				
				totalCnt++;
	            if (totalCnt % 3000 == 0) {
	                showLogMessage("I", "", "select_col_liad_status_mm 目前處理筆數 =[" + totalCnt + "]");
	            }
			}
		} else if (hCdrlDbType.equals("2")) {
			sqlCmd = "select liqu_status, ";
			sqlCmd += " nvl(decode(acct_status,'1',debt_amt,'2',debt_amt,";
			sqlCmd += " '3',demand_amt,'4',bad_debt_amt,0),0) db_debt_amt ";
			sqlCmd += "from col_liad_liquidate where id_p_seqno = ? and recv_date = ? ";
			sqlCmd += "	fetch first 1 row only ";
			setString(1, hCdrlIdPSeqno);
			setString(2, hCdrlDbRecvDate);
			
			extendField = "col_liad_status_mm_2.";
			
			int selectCnt = selectTable();
			if (selectCnt > 0) {
				lsStatus = getValue("col_liad_status_mm_2.liqu_status");
				lmAmt = getValueDouble("col_liad_status_mm_2.db_debt_amt");
				
				liStatus = comcr.str2int(lsStatus);
				if (Array2[liStatus][0].equals("")) {
					Array2[liStatus][0] = hStatProcMonth;
					Array2[liStatus][1] = "2";
					Array2[liStatus][2] = lsStatus;
					Array2[liStatus][3] = "0";
					Array2[liStatus][4] = "0";
				}
				Array2[liStatus][3] = String.format("%d", (comcr.str2int(Array2[liStatus][3])+1));
				Array2[liStatus][4] = String.format("%f", (comcr.str2double(Array2[liStatus][4])+lmAmt));
				
				totalCnt++;
	            if (totalCnt % 3000 == 0) {
	                showLogMessage("I", "", "select_col_liad_status_mm 目前處理筆數 =[" + totalCnt + "]");
	            }
			}
		}
		
    }
    
    /***********************************************************************/
    void insertColLiadStatusMm1(int dataStatus) throws Exception {
    	if (Array1[dataStatus][3].equals("")) return;  //Cnt='' 無資料不存檔
    	
//    	showLogMessage("I", "", "mod_user="+ h_user_id);
    	
        dateTime();
        daoTable = "col_liad_status_mm";
        extendField = daoTable + ".";
        setValue(extendField+"static_ym", Array1[dataStatus][0]);
        setValue(extendField+"data_type", Array1[dataStatus][1]);
        setValue(extendField+"data_status", Array1[dataStatus][2]);
        setValueDouble(extendField+"tol_cnt", comcr.str2double(Array1[dataStatus][3]));
        setValueDouble(extendField+"org_debt_amt", comcr.str2double(Array1[dataStatus][4]));
        setValue(extendField+"proc_date", hBusiBusinessDate);
        setValue(extendField+"mod_user", hUserId);
        setValue(extendField+"mod_time", sysDate + sysTime);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liad_status_mm_1 duplicate!", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    void insertColLiadStatusMm2(int dataStatus) throws Exception {
    	if (Array2[dataStatus][3].equals("")) return;  //Cnt='' 無資料不存檔
    	
        dateTime();
        daoTable = "col_liad_status_mm";
        extendField = daoTable + ".";
        setValue(extendField+"static_ym", Array2[dataStatus][0]);
        setValue(extendField+"data_type", Array2[dataStatus][1]);
        setValue(extendField+"data_status", Array2[dataStatus][2]);
        setValueDouble(extendField+"tol_cnt", comcr.str2double(Array2[dataStatus][3]));
        setValueDouble(extendField+"org_debt_amt", comcr.str2double(Array2[dataStatus][4]));
        setValue(extendField+"proc_date", hBusiBusinessDate);
        setValue(extendField+"mod_user", hUserId);
        setValue(extendField+"mod_time", sysDate + sysTime);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liad_status_mm_2 duplicate!", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB034 proc = new ColB034();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

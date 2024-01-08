/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/11/01  V1.00.01    JeffKung  program initial                           *
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*長循轉換名單處理程式*/
public class BilRD114A extends AccessDAO {
    private String progname = "長循轉換名單處理程式  112/11/01  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hBusinssDate = "";

    int totalCnt = 0;
    
    public int mainProcess(String[] args) {

        try {
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
            
            if (args.length == 1 && args[0].length() == 8) {
            	hBusinssDate = args[0];
            } 
            
            if ("01".equals(comc.getSubString(hBusinssDate,6))) {
            	showLogMessage("I", "", "每月01不執行, 本日非執行日!!");
           		return 0;
           	}
            
            showLogMessage("I", "", "資料日期=[" + hBusinssDate + "]");
            
    		selectBilContract();
    		
    		//LOAN上傳檔案處理
			String isFileNames = String.format("LOAN2NCR.%8.8s", hBusinssDate);
			readFile(isFileNames);
            
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
    void commonRtn() throws Exception {
        hBusinssDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }
    }

    /***********************************************************************/
	void selectBilContract() throws Exception {
		
		double transAmt = 0;
		double transRate = 0;
		String transDate = "";
		String pSeqno = "";

		sqlCmd = " select p_seqno,tot_amt,trans_rate,new_proc_date ";
		sqlCmd += " from bil_contract ";
		sqlCmd += " where 1=1 ";
		sqlCmd += " and mcht_no = '106000000005' ";
		sqlCmd += " and ( all_post_flag <> 'Y' ";
		sqlCmd += "     or substr(last_update_date,1,6) >= ? ) ";

		setString(1, comc.getSubString(hBusinssDate, 0, 6));

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			totalCnt++;

			if (totalCnt % 5000 == 0 || totalCnt == 1) {
				showLogMessage("I", "", "Current Process record=" + totalCnt);
			}

			pSeqno = getValue("p_seqno");
			transDate = getValue("new_proc_date");
			transAmt = getValueDouble("tot_amt");
			transRate = getValueDouble("trans_rate");
			
			
			selectActAcno(pSeqno);
			selectCrdCard(pSeqno);
			// 無業績分行跳過
			if ("".equals(getValue("crdcard.reg_bank_no")))
				continue;

			// return(0)沒資料要insert
			if (selectBilRevolverList() == 0) {
				insertBilRevolverList("A",transDate,transAmt,transRate);
			} else {
				updateBilRevolverList("A",transDate,transAmt,transRate,getValue("list.rowid"));
			}

		}

		showLogMessage("I", "", "長循轉分期共處理: [" + totalCnt + "] 筆");
		closeCursor();
	}
    
    //************************************************************************
    int insertBilRevolverList(String transType, String transDate, double transAmt, double transRate) throws Exception {

    	setValue("data_date"           , hBusinssDate);
        setValue("reg_bank_no"         , getValue("crdcard.reg_bank_no"));
        setValue("id_p_seqno"          , getValue("actacno.id_p_seqno"));
        setValueDouble("revolve_amt"   , transAmt);
        setValue("trans_type"          , transType);
        setValue("trans_date"          , transDate);
        setValueDouble("trans_amt"     , transAmt);
        setValueDouble("trans_rate"    , transRate);
        setValueDouble("revolve_rate"  , getValueDouble("actacno.rcrate_year"));
        setValue("mod_time"            , sysDate + sysTime);
        setValue("mod_pgm"             , javaProgram);

        daoTable = "bil_revolver_list";

        insertTable();

        if (dupRecord.equals("Y")) {
        	showLogMessage("I", "", " insert_bil_revolver_list error(dupRecord) , id_p_seqno=[" + getValue("actacno.id_p_seqno") + "]");
        }

        return (0);
    }

	// ************************************************************************
	void updateBilRevolverList(String transType, String transDate, double transAmt, double transRate, String rowid)
			throws Exception {

		daoTable = "bil_revolver_list";
		updateSQL = "trans_type = ? , trans_date = ? , trans_amt = ? , ";
		updateSQL += "trans_rate = ? , revolve_amt = ? ";
		whereStr = "where rowid  = ? ";

		setString(1, transType);
		setString(2, transDate);
		setDouble(3, transAmt);
		setDouble(4, transRate);
		setDouble(5, transAmt);
		setRowId(6, rowid);
		updateTable();

	}
    
    /**********************************************************************/
    void selectActAcno(String pSeqno) throws Exception {
    	
    	extendField="actacno.";
        sqlCmd =  "select id_p_seqno,rcrate_year,acct_status ";
        sqlCmd += "from act_acno  ";
        sqlCmd += "where p_seqno = ? and acct_type='01' ";
        setString(1, pSeqno);
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValue("actacno.id_p_seqno","");
            setValue("actacno.acct_status","1");
            setValueDouble("actacno.rcrate_year",14.75);
        }
    }
    
    /**********************************************************************/
    void selectCrdCard(String pSeqno) throws Exception {
    	
    	extendField="crdcard.";
        sqlCmd =  "select reg_bank_no ";
        sqlCmd += "from crd_card  ";
        sqlCmd += "where p_seqno = ? and acct_type='01' ";
        sqlCmd += "and reg_bank_no <> '' and group_code not in ('1203','1204') ";
        sqlCmd += "order by ori_issue_date desc ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, pSeqno);
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValue("crdcard.reg_bank_no","");
        }
    }
    
    /**********************************************************************/
    int selectBilRevolverList() throws Exception {
    	
    	extendField="list.";
        sqlCmd =  "select id_p_seqno , rowid as rowid ";
        sqlCmd += "from bil_revolver_list  ";
        sqlCmd += "where id_p_seqno = ? and data_date like ? ";

        setString(1, getValue("actacno.id_p_seqno"));
        setString(2, (comc.getSubString(hBusinssDate,0,6)+"%"));
        
        int tmpInt = selectTable();
        if (tmpInt > 0) {
        	return 1;
        }
        return 0; //無資料要新增
    }
        
    //=============================================================================
  	void readFile(String fileName) throws Exception {

  		String readData = "";
  		String outData = "";
  		String tmpstr = "";
  		int totalCnt = 0;
  		int realCnt = 0;
  		int fieldCnt = 0;
  		
  		String idNo = "";
  		String transDate = "";
  		long   transAmt = 0;
  		int    transTerm = 0;
  		double transRate = 0;
  		
  		String procCode = "0000";

  		String lsFile = String.format("%s/media/bil/%s", comc.getECSHOME(), fileName);
  		showLogMessage("I", "", "file path = [" + lsFile + "]");
  		int iiFileNum = openInputText(lsFile,"MS950");
  		if (iiFileNum == -1) {
  			showLogMessage("I", "", String.format("無檔案可處理 [%s]", fileName));
  			return;
  		}

  		while (true) {

  			readData = readTextFile(iiFileNum).trim();
  			
  			procCode = "0000";
  			outData = readData;

  			if (endFile[iiFileNum].equals("Y"))
  				break;
  			
  			totalCnt++;
  			
  			if (readData.length() < 10) {
  				procCode = "資料格式錯誤"; 
  				continue;
  			} else {
  				realCnt++;
  				byte[] bytes = readData.getBytes("MS950");
  				idNo = comc.subMS950String(bytes, 0, 10).trim();
  				transDate = comc.subMS950String(bytes, 11, 8).trim();
  				transAmt = comc.str2long(comc.subMS950String(bytes, 20, 10).trim());
  				transTerm = comc.str2int(comc.subMS950String(bytes, 31, 2).trim());
  				transRate = comc.str2double(comc.subMS950String(bytes, 34, 5).trim());

  				if (comc.isThisDateValid(transDate, "yyyyMMdd")==false) {
  					procCode = "資料格式錯誤";  
  				} else if (transAmt == 0 || transTerm == 0) {
  					procCode = "資料格式錯誤";  
  				} else if (chkIdNo(idNo) == 1) {
  					procCode = "idNo無效"; 
  				} 

  				if (realCnt % 500 == 0) {
  					showLogMessage("I", "", "Process Count :  " + realCnt);
  				}

  				if ("0000".equals(procCode)) {
  					selectActAcno(getValue("chkidno.p_seqno"));
  					selectCrdCard(getValue("chkidno.p_seqno"));
  					// 無業績分行跳過
  					if ("".equals(getValue("crdcard.reg_bank_no")))
  						continue;

  					// return(0)沒資料要insert
  					if (selectBilRevolverList() == 0) {
  						insertBilRevolverList("B",transDate,transAmt,transRate);
  					} else {
  						updateBilRevolverList("B",transDate,transAmt,transRate,getValue("list.rowid"));
  					}
  				} else {
  					showLogMessage("I","",String.format("資料檢核有誤,idNo:[%s],錯誤原因:[%s]",
  							      idNo,procCode));
  				}
  				
  			}
  			
  		}
  		
  		closeInputText(iiFileNum);
  		showLogMessage("I", "", "檔案轉入 [" + realCnt + "] 筆");
  		renameFile(fileName);
  	}

  	/**檢查id是否有效***/
  	int chkIdNo(String idNo) throws Exception {
  		int result = 1;
  		
  		extendField="chkidno.";
  		StringBuffer sb = new StringBuffer();
  		sb.append(" SELECT p_seqno ");
  		sb.append(" FROM act_acno ");
  		sb.append(" WHERE 1=1 ");
  		sb.append(" AND acct_type = '01' ");
  		sb.append(" AND acct_key = ? ");
  		sqlCmd = sb.toString();
  		setString(1, (idNo+"0"));
  		
  		int recordCnt = selectTable();
  		if (recordCnt > 0) {
  			result=0;
  		} 
  		
  		return result;

  	}
  	
    //=============================================================================
  	void renameFile(String fileName) throws Exception {
  		String tmpstr1 = String.format("%s/media/bil/%s", comc.getECSHOME(), fileName);
  		String tmpstr2 = String.format("%s/media/bil/backup/%s.%-8.8s", comc.getECSHOME(), fileName, sysDate);

  		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
  			showLogMessage("E", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
  			return;
  		}
  		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
  	}

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRD114A proc = new BilRD114A();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

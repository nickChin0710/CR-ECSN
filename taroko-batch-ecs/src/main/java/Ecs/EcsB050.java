/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE           Version       AUTHOR               DESCRIPTION           *
 *  ---------------  ------------ ----------- -------------------------------  *
*   2021/08/19     V1.00.01    JeffKung   initial draft                        *
*   2022/07/11     V1.00.02    JeffKung   test main()                          *
 *  2023/01/31     V1.00.03    JeffKung   set error return code                *                                                                                              *
 ******************************************************************************/

package Ecs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

import dxc.util.JavaClassLoader;

/*消費回饋聯名機構處理程式*/
public class EcsB050 extends AccessDAO {

    private String PROGNAME = "ECS批次程式排程處理程式  V1.00.03 2023/01/31";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    Map<String, ThreadInfo> hThreadMap = new HashMap<>();
    List<String> shellId = new ArrayList<String>();
    
    boolean debug = false;
    
    String hCallBatchSeqno   = "";
    String hEcsBatchCtlShellId     = "";
    String hBusiBusinessDate = "";

    String hEcsBatchCtlPgmId   = "";
    String hEcsBatchCtlRowId       = "";
    String hEcsBatchCtlKeyParm1   = "";
    String hEcsBatchCtlKeyParm2   = "";
    String hEcsBatchCtlKeyParm3   = "";
    String hEcsBatchCtlKeyParm4   = "";
    String hEcsBatchCtlKeyParm5   = "";
    String hEcsBatchCtlRepeatCode = "";
    String hEcsBatchCtlCallDutyInd = "";
    String hEcsBatchCtlNormalCode  = "";
    String hEcsBatchCtlProcFlag   = "";
    String hEcsBatchCtlQueueNo    = "";
    int    hEcsBatchCtlQueueSeq   = 0;
    String hEcsBatchCtlErrorCode  = "";
    String hEcsBatchCtlErrorDesc  = "";

    public int mainProcess(String[] args) throws Exception {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            
            // 固定要做的
            if (!connectDataBase()) {
                comcr.errRtn("connect DataBase Error", "", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());   
            
            if (args.length < 1 ) {
                comcr.errRtn("Usage : EcsD050 shell_id", "", "");
            }
            
            for (int argi=0; argi < args.length ; argi++ ) {
          	  if (args[argi].equals("debug")) {
          		  debug=true;
          	  }
            }
           
            hEcsBatchCtlShellId = args[0];   //參數一定要帶入shellId

            selectPtrBusinday();

            selectEcsBatchCtl();
            commitDataBase();
            String shellExecResult = "Y";
            int    returnCode = 0;

            while(true) {
                
                shellExecResult = selectEcsBatchCtl1();
                
                if (shellExecResult.equals("E"))
                {
                	//returnCode = 1;  //換成取returnCode
                	returnCode = Integer.parseInt(hThreadMap.get(hEcsBatchCtlShellId).flag);
                	break; //程式執行失敗, 後續的程式不能執行
                }
                
                if (hThreadMap.size() == 0) {
                    break; //全部的程式執行完畢
                }
                TimeUnit.SECONDS.sleep(5);
            }

            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return returnCode;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /*******************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";

        sqlCmd = " select business_date ";
        sqlCmd += " from   ptr_businday ";
        sqlCmd += " fetch first 1 rows only ";
        if (selectTable() > 0) {
            hBusiBusinessDate = getValue("business_date");
        } else {
            comcr.errRtn("selectPtrBusinday error", "", "");
        }
    }

    /*************************************************************************/
    /*  將傳入要處理的shell Id狀態改成排入排程中準備執行                                                                                                  */
    /*************************************************************************/
    void selectEcsBatchCtl() throws Exception {

        sqlCmd += "  SELECT  pgm_id, ";
        sqlCmd += "          rowid rowid ";
        sqlCmd += "  from    ecs_batch_ctl ";
        sqlCmd += "  where   proc_flag = 'N' ";
        sqlCmd += "  and       shell_id = ? ";
        sqlCmd += "  and       batch_date = ? ";
        sqlCmd += "  order   by shell_id,pgm_id,priority_level,queue_seq";
        
        setString(1, hEcsBatchCtlShellId );
        setString(2, hBusiBusinessDate);

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {

            hEcsBatchCtlRowId = getValue("rowid");

            updateEcsBatchCtl();
        }
        closeCursor(cursorIndex);

    }

    /*************************************************************************/
    /* 檢查執行狀態                                                                                                                  */
    /*************************************************************************/
    String selectEcsBatchCtl1() throws Exception {
    	String shellExecResult = "Y";
    	if (hThreadMap.size() > 0) {
    		if (hThreadMap.get(hEcsBatchCtlShellId) != null) {
    			if (hThreadMap.get(hEcsBatchCtlShellId).flag.equals("R")) {
    				return shellExecResult;  
    			} else {
    				if (hThreadMap.get(hEcsBatchCtlShellId).flag.equals("E")){
    					shellExecResult="E";  //啟動程式錯誤, 程式終止
    				} else  {
    					if (!hThreadMap.get(hEcsBatchCtlShellId).flag.equals(hEcsBatchCtlNormalCode) && hEcsBatchCtlCallDutyInd.equals("Y") ) {
    						shellExecResult="E"; //執行失敗, return_code不等於預期的執行結果且要call duty
    					}
    				}
    					
    				if ("Y".equals(shellExecResult)){
    					updateEcsBatchCtlProcFlag("Y");  //執行成功
    					commitDataBase();
    				} else  {
						updateEcsBatchCtlProcFlag("E");  //執行失敗
						commitDataBase();
						return shellExecResult;	         //程式終止
					}
    				
    			}
            }
        } 
    	
    	//找下支要執行的程式
    	hThreadMap.clear();
		selectEcsBatchCtl2();
		commitDataBase();
		return shellExecResult;
    }


    /*************************************************************************/
    /* */
    /*************************************************************************/
    void selectEcsBatchCtl2() throws Exception {

        sqlCmd = "  SELECT  pgm_id, ";
        sqlCmd += "          ltrim(rtrim(key_parm1)) hEcsBatchCtlKeyParm1, ";
        sqlCmd += "          ltrim(rtrim(key_parm2)) hEcsBatchCtlKeyParm2, ";
        sqlCmd += "          ltrim(rtrim(key_parm3)) hEcsBatchCtlKeyParm3, ";
        sqlCmd += "          ltrim(rtrim(key_parm4)) hEcsBatchCtlKeyParm4, ";
        sqlCmd += "          ltrim(rtrim(key_parm5)) hEcsBatchCtlKeyParm5, ";
        sqlCmd += "          decode(repeat_code,'Y','Y','') hEcsBatchCtlRepeatCode, ";
        sqlCmd += "          normal_code , ";
        sqlCmd += "          call_duty_ind , ";
        sqlCmd += "          proc_flag, ";
        sqlCmd += "          queue_no,  ";
        sqlCmd += "          queue_seq, ";
        sqlCmd += "          rowid  rowid    ";
        sqlCmd += "  from    ecs_batch_ctl  ";
        sqlCmd += "  where   proc_flag between '0' and '9'  ";
        sqlCmd += "  and     shell_id = ? ";
        sqlCmd += "  and     wait_flag = 'N' ";  //暫停碼="Y"時, 代表暫停執行
        sqlCmd += "  and     batch_date = ? ";
        sqlCmd += "  order by priority_level , queue_seq ";
        setString(1, hEcsBatchCtlShellId);
        setString(2, hBusiBusinessDate);

        int recCnt = 0;
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            recCnt++;
            hEcsBatchCtlPgmId = getValue("pgm_id");
            hEcsBatchCtlKeyParm1 = getValue("hEcsBatchCtlKeyParm1");
            hEcsBatchCtlKeyParm2 = getValue("hEcsBatchCtlKeyParm2");
            hEcsBatchCtlKeyParm3 = getValue("hEcsBatchCtlKeyParm3");
            hEcsBatchCtlKeyParm4 = getValue("hEcsBatchCtlKeyParm4");
            hEcsBatchCtlKeyParm5 = getValue("hEcsBatchCtlKeyParm5");
            hEcsBatchCtlRepeatCode = getValue("hEcsBatchCtlRepeatCode");
            hEcsBatchCtlCallDutyInd = getValue("call_duty_ind");
            hEcsBatchCtlNormalCode = getValue("normal_code");
            hEcsBatchCtlProcFlag = getValue("proc_flag");
            hEcsBatchCtlQueueNo = getValue("queue_no");
            hEcsBatchCtlQueueSeq = getValueInt("queue_seq");
            hEcsBatchCtlRowId = getValue("rowid");

            /*ProcFlag == "2" 還有程式執行中 */
            if (hEcsBatchCtlProcFlag.equals("2"))  
                break;
            
            if (debug) {
            	showLogMessage("I", "", String.format("Job Shell Id[%s], Pgm Id[%s] ", hEcsBatchCtlShellId, hEcsBatchCtlPgmId));
            }
    		
    		
            /*ProcFlag == "2" 開始執行 */
            updateEcsBatchCtl2(hEcsBatchCtlRowId);  //開始執行
            commitDataBase();
            
            
            String pgName = comc.getSubString(hEcsBatchCtlPgmId, 0, 3) + "." + hEcsBatchCtlPgmId;

            //取得執行程式的參數
            List <String> list = new ArrayList <String> ();
            if (hEcsBatchCtlKeyParm1.trim().length() > 0 ) { 
            	list.add(hEcsBatchCtlKeyParm1);
            }
            if (hEcsBatchCtlKeyParm2.trim().length() > 0 ) { 
            	list.add(hEcsBatchCtlKeyParm2);
            }
            if (hEcsBatchCtlKeyParm3.trim().length() > 0 ) { 
            	list.add(hEcsBatchCtlKeyParm3);
            }
            if (hEcsBatchCtlKeyParm4.trim().length() > 0 ) { 
            	list.add(hEcsBatchCtlKeyParm4);
            }
            if (hEcsBatchCtlKeyParm5.trim().length() > 0 ) { 
            	list.add(hEcsBatchCtlKeyParm5);
            }
            //size of list  
            int list_size = list.size();  
            //creating string array  
            String[] pgArgs = new String[list_size];  
            //converting to string array  
            list.toArray(pgArgs);  
            //printing the string array  
            for(int i = 0; i < pgArgs.length; i++) {
                if (debug) {
                	showLogMessage("I", "","debug__CtlKeyParm" + i +" :"+pgArgs[i] );
            }
           
/*                
            if (hEcsBatchCtlKeyParm1.length() > 0 ) {
            	String argsString = hEcsBatchCtlKeyParm1 + (hEcsBatchCtlKeyParm2.equals("") ? "" : " " + hEcsBatchCtlKeyParm2)
                        + (hEcsBatchCtlKeyParm3.equals("") ? "" : " " + hEcsBatchCtlKeyParm3)
                        + (hEcsBatchCtlKeyParm4.equals("") ? "" : " " + hEcsBatchCtlKeyParm4)
                        + (hEcsBatchCtlKeyParm5.equals("") ? "" : " " + hEcsBatchCtlKeyParm5);
                pgArgs = argsString.split(" ");
*/                
            }

            /* 單支程式處理 */
            putThreadMap(hEcsBatchCtlShellId, hEcsBatchCtlRowId, "R");
            InThread inp = new InThread(hEcsBatchCtlShellId, pgName, pgArgs);
            inp.start();
            
            break;
        }
        closeCursor(cursorIndex);

    }

    /*************************************************************************/
    /* 取callBatch Seqno                                                                                                         */
    /*************************************************************************/
    double GetJobSeq() throws Exception {
        sqlCmd = "select seq_callbatch.nextval jobseq from dual";
        selectTable();
        return getValueInt("jobseq");
    }

    /*************************************************************************/
    /* 異動處理狀態為"0" --->    已列入排程程等待處理中 !                                                              */
    /*************************************************************************/
    void updateEcsBatchCtl() throws Exception {
        hEcsBatchCtlQueueNo = String.format("JQ%08.0f", GetJobSeq());

        daoTable = "ecs_batch_ctl";
        updateSQL   = "   queue_no      = ?,    ";
        updateSQL += "   queue_seq     = 0,    ";
        updateSQL += "   proc_flag     = '0',  ";
        updateSQL += "   proc_desc     = '已列入排程程等待處理中 !',    ";
        updateSQL += "   proc_date     = to_char(sysdate,'yyyymmdd'),   ";
        updateSQL += "   proc_time     = to_char(sysdate,'hh24miss'),   ";
        updateSQL += "   error_code    = '',  ";
        updateSQL += "   error_desc    = '',  ";
        updateSQL += "   mod_time      = sysdate,   ";
        updateSQL += "   mod_user      = ?  ";
        whereStr = "  where  rowid         = ? ";
        setString(1, hEcsBatchCtlQueueNo);
        setString(2, javaProgram);
        setRowId(3, hEcsBatchCtlRowId);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("updateEcsBatchCtl error, shellId= ", "", hEcsBatchCtlShellId);
        }
    }

    /*************************************************************************/
    void updateEcsBatchCtl2(String rid) throws Exception {
        daoTable = "ecs_batch_ctl";
        updateSQL += "  proc_flag     = '2', ";
        updateSQL += "  proc_desc     = '程式已開始執行 !', ";
        updateSQL += "  execute_date_s = to_char(sysdate,'yyyymmdd'), ";
        updateSQL += "  execute_time_s = to_char(sysdate,'hh24miss'), ";
        updateSQL += "  proc_date     = to_char(sysdate,'yyyymmdd'), ";
        updateSQL += "  proc_time     = to_char(sysdate,'hh24miss'), ";
        updateSQL += "  error_code    = '00', ";
        updateSQL += "  mod_time      = sysdate, ";
        updateSQL += "  mod_user      = ? ";
        whereStr = "  where  rowid         = ?  ";

        setString(1, javaProgram);
        setRowId(2, rid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("updateEcsBatchCtl2 error", "", hEcsBatchCtlQueueNo);
        }
    }
    
    /*****************************************************************************/
    public int updateEcsBatchCtlProcFlag(String procFlag) throws Exception {
        if (procFlag.equals("Y")) {
            updateEcsBatchCtlProcFlagY();
            commitDataBase();
        }
        else if (procFlag.equals("E")) {
            updateEcsBatchCtlProcFlagE();
            commitDataBase();
        }

        return(0);
     }
    /*************************************************************************/
    private int  updateEcsBatchCtlProcFlagY() throws Exception {
		daoTable = "ecs_batch_ctl";
		updateSQL = "proc_flag      = 'Y', ";
		updateSQL += "proc_desc      = '程式正常結束 !', ";
		updateSQL += "execute_date_e = to_char(sysdate,'yyyymmdd'), ";
		updateSQL += "execute_time_e = to_char(sysdate,'hh24miss'), ";
		updateSQL += "proc_date      = to_char(sysdate,'yyyymmdd'), ";
		updateSQL += "proc_time      = to_char(sysdate,'hh24miss'), ";
		updateSQL += "error_code     = ?, ";
		updateSQL += "error_desc     = ?, ";
		updateSQL += "mod_time       = sysdate, ";
		updateSQL += "mod_user       = ?  ";
		whereStr = " where queue_no       = ? ";
		whereStr += " and    queue_seq  = ?";
		setString(1, hEcsBatchCtlErrorCode);
		setString(2, hEcsBatchCtlErrorDesc);
		setString(3, javaProgram);
		setString(4, hEcsBatchCtlQueueNo);
		setInt(5, hEcsBatchCtlQueueSeq);
		updateTable();

		if (notFound.equals("Y")) {
			comcr.errRtn("updateEcsBatchCtlProcFlagY not Found", "", hEcsBatchCtlQueueNo);
		}
		return 0;
     }
    /*************************************************************************/
    private int  updateEcsBatchCtlProcFlagE() throws Exception  {
		daoTable = "ecs_batch_ctl";
		updateSQL = "proc_flag      = 'E', ";
		updateSQL += "proc_desc      = '系統錯誤, 請洽資訊人員 !', ";
		updateSQL += "execute_date_e = to_char(sysdate,'yyyymmdd'), ";
		updateSQL += "execute_time_e = to_char(sysdate,'hh24miss'), ";
		updateSQL += "proc_date      = to_char(sysdate,'yyyymmdd'), ";
		updateSQL += "proc_time      = to_char(sysdate,'hh24miss'), ";
		updateSQL += "error_code     = ?, ";
		updateSQL += "error_desc     = ?, ";
		updateSQL += "mod_time       = sysdate, ";
		updateSQL += "mod_user       = ? ";
		whereStr = " where queue_no       = ? ";
		whereStr += " and    queue_seq  = ?";
		setString(1, hThreadMap.get(hEcsBatchCtlShellId).flag);
		setString(2, hEcsBatchCtlErrorDesc);
		setString(3, javaProgram);
		setString(4, hEcsBatchCtlQueueNo);
		setInt(5, hEcsBatchCtlQueueSeq);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("updateEcsBatchCtlProcFlagE not Found", "", hEcsBatchCtlQueueNo);
		}
		return 0;

    }    
    /***********************************************************************/
    private void putThreadMap(String shellId, String rid, String flag) {
        ThreadInfo info = new ThreadInfo();
        info.set(rid, flag);       
        hThreadMap.put(shellId, info);
        return;
    }
    /***********************************************************************/
    private void putThreadMapFlag(String shellId, String flag) {
        ThreadInfo info = new ThreadInfo();
        String rid = hThreadMap.get(shellId).rowid;
        info.set(rid, flag);       
        hThreadMap.put(shellId, info);
        return;
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        EcsB050 proc = new EcsB050();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /*****************************************************************************/
    private class ThreadInfo {
        String rowid  = "";
        String flag = "";
        public void set(String rid, String flg) {
            this.rowid = rid;
            this.flag = flg;
        }
    }
    /*****************************************************************************/
    /* INPUT 處理程序 */
    private class InThread extends Thread {
        JavaClassLoader javaClassLoader = new JavaClassLoader();
        String shellId = "";
        String pgName = "";
        String[] pgArgs = null;
        /*****************************************************************************/
        InThread(String jobGp, String progName, String[] args) throws Exception {
            /* 建立 連線 I/O 物件 */
            this.shellId = jobGp;
            this.pgName = progName;
            this.pgArgs = args;
        }

        /*****************************************************************************/
        public void run() {

            try {
                try {
                	
                	if (debug) {
                    	showLogMessage("I", "","debug__run_pgName :"+pgName  );
                    	showLogMessage("I", "","args :"+  Arrays.toString(pgArgs) );
                    	showLogMessage("I", "","args.length:"+  pgArgs.length );
                    }
                	
                    Class<?> BoClass = javaClassLoader.getClass(pgName);
                    Method BoMethod = BoClass.getMethod("mainProcess", String[].class);
                    Object Bo = BoClass.newInstance();
                    Object retCode = BoMethod.invoke(Bo, new Object[] { pgArgs });
                    int retValue = 0;
                    if (Objects.isNull(retCode) == false) {
                    	retValue = (Integer) retCode; 
                    }
                    
                    BoMethod = null;
                    BoClass = null;
                    Bo = null;

                    if (retValue == 0) {
                    	putThreadMapFlag(shellId, "0");  //Finish
                    } else {
                    	putThreadMapFlag(shellId, String.valueOf(retValue)); //ErrorCode
                    }
                } catch (Throwable ex) {
                	showLogMessage("E", "","Throwable Exception :"+ ex.getMessage()  );
                    putThreadMapFlag(shellId, "E");    //Error: E2
                }
            } catch (Exception ex) {
                if (pgArgs != null) pgArgs = null;
            } finally {
                if (pgArgs != null) pgArgs = null;
            }

        }
    }

}

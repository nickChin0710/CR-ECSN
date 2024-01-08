/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE           Version       AUTHOR               DESCRIPTION           *
 *  ---------------  ------------ ----------- -------------------------------- *
*   2022/07/28     V1.00.01    JeffKung   initial draft                        *
 *                                                                             *
 *******************************************************************************/

package Ecs;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*消費回饋聯名機構處理程式*/
public class EcsB070 extends AccessDAO {

    private String PROGNAME = "ECS批次程式排程處理程式-指定step Rerun  V1.00.01 2022/07/28";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    boolean debug = false;
    
    String hCallBatchSeqno   = "";
    String hEcsBatchCtlShellId     = "";
    int hEcsBatchCtlSeqno = 0;
    String hBusiBusinessDate = "";

    String hEcsBatchCtlRowId       = "";

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
            
            if (args.length < 2 ) {
                comcr.errRtn("Usage : EcsD050 shell_id rerun_seqno", "", "");
            }
            
            for (int argi=0; argi < args.length ; argi++ ) {
          	  if (args[argi].equals("debug")) {
          		  debug=true;
          	  }
            }
           
            hEcsBatchCtlShellId = args[0];               //參數一定要帶入shellId
            hEcsBatchCtlSeqno = comc.str2int(args[1]);   //參數一定要帶入rerun seqno
            
            selectPtrBusinday();
            selectEcsBatchCtl();

            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
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

    	daoTable = "ecs_batch_ctl";
        sqlCmd += "  SELECT  BATCH_DATE, BATCH_TIME , SHELL_ID, PGM_ID,  ";
        sqlCmd += "          PGM_DESC, PRIORITY_LEVEL , QUEUE_NO, QUEUE_SEQ, WAIT_FLAG, ";
        sqlCmd += "          KEY_PARM1, KEY_PARM2 , KEY_PARM3, KEY_PARM4, KEY_PARM5, ";
        sqlCmd += "          REPEAT_CODE, NORMAL_CODE , CALL_DUTY_IND, RERUN_PROC, ";
        sqlCmd += "          PROC_FLAG, rowid rowid ";
        sqlCmd += "  from    ecs_batch_ctl ";
        sqlCmd += "  where   1=1 ";
        sqlCmd += "  and     shell_id = ? ";
        sqlCmd += "  and     batch_date = ? ";
        sqlCmd += "  and     priority_level >= ? ";
        sqlCmd += "  and     rerun_proc_date = '' ";
        sqlCmd += "  order by shell_id,priority_level ";

        setString(1, hEcsBatchCtlShellId );
        setString(2, hBusiBusinessDate);
        setInt(3, hEcsBatchCtlSeqno);
        
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {

           	hEcsBatchCtlRowId = getValue("rowid");
           	//已經執行成功(Y),或是執行失敗(E)才需要再多寫一筆記錄, 否則不需額外動作
           	if("Y".equals(getValue("proc_flag")) || "E".equals(getValue("proc_flag"))) {  
           		insertEcsBatchCtl();
           		updateEcsBatchCtl();
           	}

        }

        closeCursor(cursorIndex);

    }

    /*************************************************************************/
    /* 新增一筆要重覆執行的記錄                                                                                                 */
    /*************************************************************************/
    void insertEcsBatchCtl() throws Exception {
      
  	if (debug)
  		showLogMessage("I", "", "  insertEcsBatchCtl=[" + 1 + "]");

      setValue("BATCH_DATE", getValue("BATCH_DATE"));
      setValue("BATCH_TIME", getValue("BATCH_TIME"));
      setValue("SHELL_ID", getValue("SHELL_ID"));
      setValue("PGM_ID", getValue("PGM_ID"));
      setValue("PGM_DESC", getValue("PGM_DESC"));
      setValueInt("PRIORITY_LEVEL", getValueInt("PRIORITY_LEVEL"));
      setValue("QUEUE_NO", getValue("QUEUE_NO"));
      setValueInt("QUEUE_SEQ", getValueInt("QUEUE_SEQ") + 1);
      setValue("EXECUTE_DATE_S","");
      setValue("EXECUTE_TIME_S", "");
      setValue("EXECUTE_DATE_E", "");
      setValue("EXECUTE_TIME_E", "");
      setValue("ERROR_CODE", "");
      setValue("ERROR_DESC", "");
      setValue("WAIT_FLAG", getValue("WAIT_FLAG"));
      setValue("PROC_FLAG", "0");
      setValue("PROC_DESC", "已列入排程程等待處理中 !");
      setValue("PROC_DATE", sysDate);
      setValue("PROC_TIME", sysTime);
      setValue("KEY_PARM1", getValue("KEY_PARM1"));
      setValue("KEY_PARM2", getValue("KEY_PARM2"));
      setValue("KEY_PARM3", getValue("KEY_PARM3"));
      setValue("KEY_PARM4", getValue("KEY_PARM4"));
      setValue("KEY_PARM5", getValue("KEY_PARM5"));
      setValue("REPEAT_CODE", getValue("REPEAT_CODE"));
      setValue("NORMAL_CODE", getValue("NORMAL_CODE"));
      setValue("CALL_DUTY_IND", getValue("CALL_DUTY_IND"));
      setValue("RERUN_PROC", "");
      setValue("MOD_TIME", sysDate + sysTime);
      setValue("MOD_USER", javaProgram);
      setValue("MOD_PGM", javaProgram);	
      
      daoTable = "ecs_batch_ctl";
      insertTable();
      if (dupRecord.equals("Y")) {
        comcr.errRtn("insert_ecs_batch_ctl duplicate", "", "shellId=[" +getValue("SHELL_ID") + "], pgmId= ["+ getValue("PGM_ID")+ "]");
      }
    }


    /*************************************************************************/
    /* 異動Error的那一筆record , rerun= "Y"                                                                                 */
    /*************************************************************************/
    void updateEcsBatchCtl() throws Exception {
        daoTable = "ecs_batch_ctl";
        updateSQL   = "   rerun_proc      = 'Y' ,    ";
        updateSQL += "   rerun_proc_date = ? ,  ";
        updateSQL += "   mod_time      = sysdate,   ";
        updateSQL += "   mod_pgm      = ?  ";
        whereStr = "  where  rowid         = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hEcsBatchCtlRowId);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("updateEcsBatchCtl error, shellId= ", "", hEcsBatchCtlShellId);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        EcsB070 proc = new EcsB070();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/01/16  V1.00.00  Pino        program initial                           *
* 109/12/22  V1.00.01   shiyuqi       updated for project coding standard   *
******************************************************************************/

package Crd;

//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class CrdE001 extends AccessDAO {
    private String progname = "新製卡資料寫入道路救援主檔程式   109/12/22  V1.00.01 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
	Rds.RdsB020 rdsB020 = new Rds.RdsB020();

    int debug = 0;
    int debugD = 0;
    long totalCnt = 0;
    String checkHome = "";
    String hCallErrorDesc = "";
    String hBusinessDate = "";

    String prgmId = "CrdE001";
    int recordCnt = 0;
    int actCnt = 0;
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";

    String hTempUser = "";
    String hMbosBatchno = "";
    String hMbosEmbossSource = "";
    String hMbosToNcccCode = "";
    String hMbosCardType = "";
    String hMbosUnitCode = "";
    String hMbosCardNo = "";
    String hMbosApplyId = "";
    String hMbosApplyIdCode = "";
    String hMbosRoadsideAssistApply = "";
    String hMbosRowid = "";
    String hMbosInMainDate = "";
    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : CrdE001 [in_main_date][callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            hMbosInMainDate = args.length > 0 ? args[0] : sysDate;
            comcr.hCallBatchSeqno = args.length > 1 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                comcr.hCallBatchSeqno = "no-call";
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            fetchDetail();

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void fetchDetail() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "batchno,";
        sqlCmd += "emboss_source,";
        sqlCmd += "to_nccc_code,";
        sqlCmd += "card_type,";
        sqlCmd += "unit_code,";
        sqlCmd += "card_no,";
        sqlCmd += "apply_id,";
        sqlCmd += "apply_id_code,";
        sqlCmd += "roadside_assist_apply,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += "from crd_emboss ";
        sqlCmd += "where in_main_date = ? ";
        sqlCmd += " and emboss_source = '1' ";
        sqlCmd += " and roadside_assist_apply <> '' ";
        sqlCmd += " and roadside_assist_apply_post_flag <> 'Y' ";
        setString(1, hMbosInMainDate);
        recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMbosBatchno        = getValue("batchno", i);
            hMbosEmbossSource  = getValue("emboss_source", i);
            hMbosToNcccCode   = getValue("to_nccc_code", i);
            hMbosCardType      = getValue("card_type", i);
            hMbosUnitCode      = getValue("unit_code", i);
            hMbosCardNo        = getValue("card_no", i);
            hMbosApplyId       = getValue("apply_id", i);
            hMbosApplyIdCode  = getValue("apply_id_code", i);
            hMbosRoadsideAssistApply = getValue("roadside_assist_apply", i);
            hMbosRowid          = getValue("rowid", i);

            totalCnt++;

            if (debug == 1)
                showLogMessage("I", "", "Read card=[" + hMbosCardNo + "]" + totalCnt);
            
            getIdPSeqno(hMbosApplyId);
            if (debugD == 1)
                showLogMessage("I", "", "Read id_p_seqno=[" + getValue("idno.id_p_seqno") + "]" + totalCnt);
            int ReceiveRoadCar=rdsB020.ReceiveRoadCar(getValue("idno.id_p_seqno"),hMbosCardNo,hMbosRoadsideAssistApply,javaProgram);
            if(ReceiveRoadCar==0||ReceiveRoadCar==1)	
            	updateEmboss();            
        }
    }
    /***********************************************************************/
    void updateEmboss() throws Exception {
        daoTable   = "crd_emboss";
        updateSQL  = "roadside_assist_apply_post_flag = 'Y',";         
        updateSQL += "mod_time    = sysdate,";
        updateSQL += "mod_pgm     = ?";
        whereStr   = "where rowid = ? ";
        setString(1, javaProgram);
        setRowId(2, hMbosRowid);
        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_emboss not found!", "", hMbosRowid);
        }
    }
    /************************************************************************/
    void getIdPSeqno(String applyId) throws Exception
    {
    	  extendField = "idno.";
    	  selectSQL = "";
    	  daoTable  = "crd_idno";
    	  whereStr  = "where id_no  = ? ";

    	  setString(1 , applyId);
    	  selectTable();
    	  return;
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
    	CrdE001 proc = new CrdE001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}

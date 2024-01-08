/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/02/21  V1.00.00    Rou        program initial                          *
*  109/12/18  V1.00.01    shiyuqi       updated for project coding standard   *
*  112/02/08  V1.00.02    Wilson    讀取檔案路徑調整                                                                                      *
*  112/04/25  V1.00.03    Wilson    hCombRtnCode = "000" -> hRejectCode = ""  *
*  112/06/13  V1.00.04    Wilson    無檔案不當掉                                                                                             *
*  112/06/19  V1.00.05    Wilson    hRejectCode chg to hCheckCode             *
*  112/07/03  V1.00.06    Wilson    假日不執行                                                                                                 *
*  112/08/24  V1.00.07    Wilson    調整搬檔邏輯                                                                                             *
******************************************************************************/

package Crd;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*接收*/
public class CrdC007 extends AccessDAO {
    private String progname = "讀取COMBO重製清單回覆檔作業 112/08/24 V1.00.07";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    int debugD = 1;
    long totalCnt = 0;
    int tmpInt = 0;
    String checkHome = "";
    String hCallErrorDesc = "";
    String hBusinessDate = "";
    String pathName1 = "";

    String prgmId = "CrdC007";
    String rptName1 = "";
    int recordCnt = 0;
    int actCnt = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq1 = 0;
    int errCnt = 0;
    String errMsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hModWs = "";
    String hModLog = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String hTempUser = "";
    String hBusinssDate = "";
    String hSystemDate = "";
    String hCombCardNo = "";
    String hCombRtnIbmDate = "";
    String hCombBatchno = "";
    double hCombRecno = 0;
    String hCombRowid = "";
    String hCombRtnCode = "";
    String hTmpAp1ApplyDate = "";
    String hCombCard = "";
    String hCombPin = "";
    String hCombIdNo = "";
    String hCombBirth = "";
    String hCombMemo = "";
    String hCheckCode = "";
    String hInMainError = "";
    String hInMainMsg = "";
    String hMailSeqno = "";
    String hCombSavingActno = "";
    String hCombCardKind = "";
    String hCombCardSource = "";
    String hTmpOldCardNo = "";
    String hTmpCardRefNum = "";
    String ifil = "";
    int totalFile ;

    String hOAct1 = "";
    String hStatus = "";
    String temstr1 = "";
    int recCount = 0;
    int allCount = 0;
    int fi = 0;
    int insertCnt = 0;
    
    protected final String dt1Str = "card_kind, card_source, saving_actno, old_card_no, new_card_no, card_ref_num, response_date, response_code";
    		
    protected final int[] dt1Length = {1, 1, 13, 16, 16, 2, 8, 3};
    
    protected  String[] dt1 = new String[] {};
    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
        	dt1 = dt1Str.split(",");
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : CrdC007 callbatch_seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

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
                if (selectTable() > 0)
                    hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            hModPgm = javaProgram;

            commonRtn();
            
            showLogMessage("I", "", String.format("今日營業日 = [%s]", hBusinessDate));
            
            if (checkPtrHoliday() != 0) {
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
            }
            
            openFile();           

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "][" + insertCnt + "]";
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
    void commonRtn() throws Exception {
        sqlCmd = "select business_date,to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += " from ptr_businday ";
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hBusinessDate = getValue("business_date");
            hSystemDate = getValue("h_system_date");
        }
    }

    /***********************************************************************/
    int checkPtrHoliday() throws Exception {
        int hCount = 0;

        sqlCmd = "select count(*) h_count ";
        sqlCmd += " from ptr_holiday  ";
        sqlCmd += "where holiday = ? ";
        setString(1, hBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_holiday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCount = getValueInt("h_count");
        }

        if (hCount > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /***********************************************************************/
    void openFile() throws Exception {       
        ifil = comc.getECSHOME() + "/media/crd/";
        ifil = Normalizer.normalize(ifil, java.text.Normalizer.Form.NFKD);
        List<String> listOfFiles = comc.listFS(ifil, "", "");
        for (String file : listOfFiles) {
            if (!comc.getSubString(file,0,13).equals("resp_combo_s_"))
                continue;
            String getFile = file;
            iFileName = String.format(getFile);
            ifil = String.format("%s/media/crd/%s", comc.getECSHOME(), iFileName);
            showLogMessage("I", "", String.format("Open filename[%s]", iFileName));
            tmpInt = selectCrdFileCtl();
            if (tmpInt > 0) //The file already exists
                continue;
            fi = openInputText(ifil, "MS950");
            if (fi == -1) 
            	continue;          
            
            selectCrdCombo();

            return;
        }
        if (totalFile < 1) {
        	showLogMessage("I", "", "無檔案可處理");
        	return;
        	
//            comcr.hCallErrorDesc = "無檔案可處理 end";
//            comcr.errRtn("無檔案可處理 end","" , comcr.hCallBatchSeqno);
        }
    }
    
 // ************************************************************************
    public int selectCrdFileCtl() throws Exception {
        selectSQL = "count(*) as all_cnt";
        daoTable = "crd_file_ctl";
        whereStr = "WHERE file_name  = ? ";

        setString(1, iFileName);
        selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0) {
            showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + iFileName + "]");
            return 1;
        }

        return 0;
    }
    
    /***********************************************************************/
    void selectCrdCombo() throws Exception { 
        String rec = "";
        
        recCount = 0;

        totalFile ++;

        while (true) {
        	rec = readTextFile(fi);
        	if (endFile[fi].equals("Y")) 
            	break;
            totalCnt ++;
        	if(debug ==1) showLogMessage("I",""," Get Str=[" + comc.getSubString(rec,0,30) + "]" + recCount);
        	moveData(processDataRecord(getFieldValue(rec, dt1Length), dt1));
        }
        
        closeInputText(fi);  
        
        insertFileCtl();
        
        if (debug == 1)
            showLogMessage("I", "", " ALL cnt=" + allCount + "," + (recCount - 1));

        String cmdStr = String.format("mv %s %s/media/crd/backup/%s", ifil, comc.getECSHOME(), iFileName);
        String fs = ifil;
        String ft = String.format("%s/media/crd/backup/%s", comc.getECSHOME(), iFileName);
        if (comc.fileRename2(fs, ft) == false) {
            showLogMessage("I", "", "ERROR : mv 檔案=" + cmdStr);
            return;
        }
        showLogMessage("I", "", " mv 檔案=" + cmdStr);      
    }

    /***********************************************************************/
    int moveData(Map<String, Object> map) throws Exception {
        dateTime();        
        String tmpStr = "";

        hCombSavingActno = "";
        hCombCardKind = "";
        hCombCardSource = "";
        hTmpOldCardNo = "";
        hTmpCardRefNum = "";
        
        hCheckCode = "";
        hInMainError = "";
        hInMainMsg = "";
        
        tmpStr = (String) map.get("card_kind");
        hCombCardKind = tmpStr;
//        if (!hCombCardKind.equals("C"))
//        	hCheckCode = "D51";
        	
        tmpStr = (String) map.get("card_source");
        hCombCardSource = tmpStr;
//        if (!hCombCardSource.equals("S"))
//        	hCheckCode = "D52";
        
        tmpStr = (String) map.get("saving_actno");
        hCombSavingActno = tmpStr;
        
        tmpStr = (String) map.get("old_card_no");
        hTmpOldCardNo = tmpStr;
        
        tmpStr = (String) map.get("new_card_no");
        hCombCardNo = tmpStr;
        if (debug == 1)
          showLogMessage("I", "", " Read card=[" + hCombCardNo + "]");
    	 
        tmpStr = (String) map.get("card_ref_num");
        hTmpCardRefNum = tmpStr;
    	 
        tmpStr = (String) map.get("response_date");
        hTmpAp1ApplyDate = tmpStr;

        tmpStr = (String) map.get("response_code");
        hCombRtnCode = tmpStr;
 
        if (debug == 1)
            showLogMessage("I", "", " RTN_CODE=[" + hCombRtnCode + "]");
        if (hCombRtnCode.length() == 0) {
            showLogMessage("I", "", "card_no [" + hCombCardNo + "] 回饋無告知結果");
            return (0);
        }

        if ((hCombCardNo.length() > 0) && (hCombSavingActno.length() > 0)) {
            hCombRtnIbmDate = "";
            hCombBatchno = "";
            hCombRecno = 0;
            hCombRowid = "";
            sqlCmd = "select rtn_ibm_date,";
            sqlCmd += "batchno,";
            sqlCmd += "recno,";
            sqlCmd += "rowid as rowid ";
            sqlCmd += " from crd_combo  ";
            sqlCmd += "where card_no = ? ";
            setString(1, hCombCardNo);
            recordCnt = selectTable();
            if (recordCnt < 1) {
                return (1);
            }
            hCombRtnIbmDate = getValue("rtn_ibm_date");
            hCombBatchno = getValue("batchno");
            hCombRecno = getValueDouble("recno");
            hCombRowid = getValue("rowid");
            if (debug == 1)
                showLogMessage("I", "", " COMBO=[" + hCombBatchno + "]" + hCombRecno);

            if (hCombRtnIbmDate.length() >= 8) {
				stderr = String.format("cardno[%s]此筆資料IBM已回饋,不可重複寫入", hCombCardNo);
				showLogMessage("I", "", stderr);
                return (1);
            }
            updateCrdCombo();
            
if(debug == 1) showLogMessage("I", " 8881 rtn_code=", hCombRtnCode);           
            
            if (comc.getSubString(hCombRtnCode,0,3).equals("000")) {
            	hCheckCode = "";
            	hInMainError = "";
            	hInMainMsg = "";
                updateCrdEmboss();            
                insertCnt ++;
            }            
            else if (!hCombRtnCode.equals("000")) {
            	hCheckCode = hCombRtnCode;
            	hInMainError = "1";
            	selectCrdMessage();
                updateCrdEmboss();
            }
        }

        return (0);
    }
    
    /**********************************************************************/    
    void updateCrdCombo() throws Exception{
    	
    	daoTable   = "crd_combo ";
    	updateSQL += " saving_actno    = ?, ";
    	updateSQL += " old_card_no    = ?,";
    	updateSQL += " card_no    = ?,";
    	updateSQL += " rtn_ibm_date    = ?,";
    	updateSQL += " rtn_code    = ?,";
        updateSQL += " mod_pgm    = ? ";
        whereStr   = "where rowid = ? ";
        setString(1, hCombSavingActno);
        setString(2, hTmpOldCardNo);
        setString(3, hCombCardNo);
        setString(4, sysDate);
        setString(5, hCombRtnCode);
        setString(6, javaProgram);
        setRowId(7, hCombRowid);

        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_combo not found 2 !","",comcr.hCallBatchSeqno);
        }

    }
    
    /**********************************************************************/
    void updateCrdEmboss() throws Exception {
if(debug == 1)
  showLogMessage("I", " update_crd_emboss=", hCombBatchno +","+ hCombRecno +","+ hCheckCode);
        daoTable   = "crd_emboss";
        updateSQL += " check_code = ?,";
        updateSQL += " in_main_error = ?,";
        updateSQL += " in_main_msg = ?,";
        updateSQL += " card_ref_num = ?,";
        updateSQL += " mod_time    = sysdate,";
        updateSQL += " mod_pgm     = ?, ";
        updateSQL += " ap1_apply_date = ? ";
        whereStr   = "where batchno = ? ";
        whereStr  += "  and recno   = ? ";
        setString(1, hCheckCode);
        setString(2, hInMainError);
        setString(3, hInMainMsg);
        setString(4, hTmpCardRefNum);
        setString(5, javaProgram);
        setString(6, hTmpAp1ApplyDate);
        setString(7, hCombBatchno);
        setDouble(8, hCombRecno);
        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_emboss not found 3!", hCombBatchno +","+ hCombRecno
                                                          , comcr.hCallBatchSeqno);
        }    
    }

    /**********************************************************************/
     void selectCrdMessage() throws Exception { 
    	
    	sqlCmd = "select msg ";
        sqlCmd += " from crd_message  ";
        sqlCmd += "where msg_type = 'NEW_CARD' ";
        sqlCmd += "and msg_value = ? ";
        setString(1, hCheckCode);
        recordCnt = selectTable();
        if (recordCnt > 0) {
        	hInMainMsg = getValue("msg");
        }       
    }      
    
    /**********************************************************************/
    void insertFileCtl() throws Exception {
        setValue("file_name", iFileName);
        setValue("crt_date", sysDate);
        setValue("trans_in_date", sysDate);
        daoTable = "crd_file_ctl";
        insertTable();
        if (dupRecord.equals("Y")) {
            daoTable = "crd_file_ctl";
            updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
            whereStr = "where file_name = ? ";
            setString(1, iFileName);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
            }
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdC007 proc = new CrdC007();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    
    /***********************************************************************/
    private Map processDataRecord(String[] row, String[] dt) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        int j = 0;
        for (String s : dt) {
            map.put(s.trim(), row[i]);
            i++;
        }
        return map;
        
    }

   /************************************************************************/
    public String[] getFieldValue(String rec, int[] parm) {
        int x = 0;
        int y = 0;
        byte[] bt = null;
        String[] ss = new String[parm.length];
        try {
            bt = rec.getBytes("MS950");
        } catch (Exception e) {
            showLogMessage("I", "", comc.getStackTraceString(e));
        }
        for (int i : parm) {
            try {
                ss[y] = new String(bt, x, i, "MS950");
            } catch (Exception e) {
                showLogMessage("I", "", comc.getStackTraceString(e));
            }
            y++;
            x = x + i;
        }
        return ss;
    }
    
}

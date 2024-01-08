/********************************************************************************************
*                                                                                           *
*                              MODIFICATION LOG                                             *
*                                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION                          *
*  ---------  --------- ----------- ------------------------------------------------------  *            
*  106/10/31  V1.00.00    phopho     program initial                                        *
*  109/07/09  V1.00.01    phopho     CR add log to DB: ptr_batch_rpt                        *
*  109/12/10  V1.00.02    shiyuqi       updated for project coding standard   *
********************************************************************************************/

package Col;

import java.text.Normalizer;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA540 extends AccessDAO {
    private String progname = "無擔保債務-展延方案記錄媒體轉入處理程式  109/12/10  V1.00.02 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "ColA540R1";
    String rptDesc1 = "無擔保展延方案暨展延結果";
    int rptSeq1 = 0;
    String buf = "";
    String szTmp = "";
    String hCallBatchSeqno = "";
    int debug = 0;

    String hBusiBusinessDate = "";
    String hTempSystime = "";
    String hEflgProcCode = "";
    String hEflgRowid = "";
    String hEflgFileName = "";
    String hEflgFileDate = "";
    String hClltJcicCreateDate = "";
    String hClltId = "";
    String hClltIdPSeqno = "";
    String hClltChiName = "";
    String hClltBankCode = "";
    String hClltBankName = "";
    String hClltRBankCode = "";
    String hClltRBankName = "";
    String hClltApplyDate = "";
    String hClltAprDate = "";
    String hClltCloseReason = "";
    String hClltCloseDesc = "";
    String hEflgProcDesc = "";

    int forceFlag = 0;
    int totalCnt = 0;
    int errorCnt = 0;
    int warningCnt = 0;
    int addColumnInt = 0;
    int errFlag = 0;
    int warnFlag = 0;
    int lineCnt = 0;
    int pageCnt = 0;
    int recCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String temstr1 = "";
    String temstr2 = "";
    String errStr = "";
    String cmdStr = "";
    
    private int fptr1 = 0;

    public int mainProcess(String[] args) {
        try {
        	dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }

            // 檢查參數
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ColA540 file_date [force_flag] ", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
        	
            forceFlag = 0;
            if ((args.length == 2) && (args[1].equals("Y")))
                forceFlag = 1;
            hEflgFileDate = "";
            if ((args.length >= 1) && (args[0].length() == 8)) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hEflgFileDate = sgArgs0;
            }
            selectPtrBusinday();
            if (hEflgFileDate.length() == 0)
                hEflgFileDate = hBusiBusinessDate;

            tmpstr = String.format("%4.4s.%2.2s.%2.2sZ68-ZZM332.csv", hEflgFileDate, hEflgFileDate.substring(4),
                    hEflgFileDate.substring(6));
            hEflgFileName = tmpstr;

            if (debug ==0) selectEcsFtpLog();
            showLogMessage("I", "", String.format("處理檔案[%s]...", hEflgFileName));
            totalCnt = 0;
            fileOpen();
            errorCnt = warningCnt = 0;
            readFile();
            printTailer();
            comc.writeReport(temstr2, lpar1);
            if (addColumnInt == 1)
                tmpstr1 = String.format("(注意:格式變更)");
            if ((errorCnt == 0) && (warningCnt == 0)) {
                tmpstr = String.format("%s媒體共[%d]筆已轉入 ,無任何錯誤,有[%d]筆警示!", tmpstr1, totalCnt - 2, warningCnt);
                showLogMessage("I", "", String.format("%s", tmpstr));
            } else {
                rollbackDataBase();
                tmpstr = String.format("%s媒體共[%d]筆,有[%d]筆錯誤,有[%d]筆警示", tmpstr1, totalCnt - 2, errorCnt,
                        warningCnt);
                showLogMessage("I", "", String.format("%s", tmpstr));
                tmpstr = String.format("報表[%s]", temstr2);
                showLogMessage("I", "", String.format("%s", tmpstr));
            }
            hEflgProcDesc = tmpstr;
            updateEcsFtpLog();
            comcr.insertPtrBatchRpt(lpar1);  //CR-insert_ptr_batch_rpt
            
            cmdStr = String.format("mv %s/media/col/LIAC/%s %s/media/col/LIACBK/%s.%s", comc.getECSHOME(), hEflgFileName,
            		comc.getECSHOME(), hEflgFileName, hTempSystime);
            cmdStr = Normalizer.normalize(cmdStr, java.text.Normalizer.Form.NFKD);
            String fs = String.format("%s/media/col/LIAC/%s", comc.getECSHOME(), hEflgFileName);
            String ft = String.format("%s/media/col/LIACBK/%s.%s", comc.getECSHOME(), hEflgFileName, hTempSystime);
            showLogMessage("I", "", String.format("檔案  : [%s]", hEflgFileName));
            showLogMessage("I", "", String.format("  移至: [%s]", ft));
            if (comc.fileRename(fs, ft) == false) {
                showLogMessage("I", "", String.format("無法搬移[%s]", cmdStr));
            }

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

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_systime ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempSystime = getValue("h_temp_systime");
        }

    }

    /***********************************************************************/
    void selectEcsFtpLog() throws Exception {
        hEflgProcCode = "";
        sqlCmd = "select proc_code,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from ecs_ftp_log  ";
        sqlCmd += "where system_id  = 'COL_LIAC'  ";
        sqlCmd += "and trans_resp_code = 'Y'  ";
        sqlCmd += "and proc_code  in ('0','1','9','Y')  ";
        sqlCmd += "and file_name  = ? ";
        setString(1, hEflgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hEflgProcCode = getValue("proc_code");
            hEflgRowid = getValue("rowid");
        } else {
        	exceptExit = 0;
            comcr.errRtn(String.format("[%s]無轉入記錄可處理", hEflgFileName), "", hCallBatchSeqno);
        }

        if (hEflgProcCode.equals("9")) {
            showLogMessage("I", "", String.format("[%s]資料重轉入處理", hEflgFileName));
            return;
        }

        if (hEflgProcCode.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn(String.format("[%s]資料已處理完畢", hEflgFileName), "", hCallBatchSeqno);
        }

        if (hEflgProcCode.equals("1")) {
            if (forceFlag == 0) {
            	exceptExit = 0;
                comcr.errRtn(String.format("[%s]資料已轉入完畢, 不需再轉入", hEflgFileName), "", hCallBatchSeqno);
            } else {
                showLogMessage("I", "", String.format("[%s]資料強制轉入處理", hEflgFileName));
                return;
            }
        }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        temstr1 = String.format("%s/media/col/LIAC/%s", comc.getECSHOME(), hEflgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        //File f = new File(temstr1);
        //if (f.exists() == false) {
        //    comcr.err_rtn("檔案不存在：" + temstr1, "", h_call_batch_seqno);
        //}
        fptr1 = openInputText(temstr1, "MS950");
        if (fptr1 == -1) {
            comcr.errRtn(String.format("error: [%s] 檔案不存在", temstr1), "", hCallBatchSeqno);
        }

        temstr2 = String.format("%s/reports/COL_A540_%s.txt", comc.getECSHOME(), hEflgFileName);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
    }

    /***********************************************************************/
    void readFile() throws Exception {
        String str600 = "";
        String stra = "";

        printHeader();
        totalCnt = 0;
        
        //FileInputStream fis = new FileInputStream(new File(temstr1));
        //BufferedReader br = new BufferedReader(new InputStreamReader(fis, "MS950"));
        //while ((str600 = br.readLine()) != null) {
        while(true) {
            str600 = readTextFile(fptr1);
            if (endFile[fptr1].equals("Y"))
                break;

            errFlag = warnFlag = 0;

            totalCnt++;
            if (totalCnt == 2)
                continue;
            if (totalCnt == 1) {
                stra = comm.getStr(str600, 2, ",");
                recCnt = comcr.str2int(stra);
                continue;
            }

            if (str600.length() < 10)
                continue;
            stra = comm.getStr(str600, 1, ",");
            stra = comm.getDateFreeFormat(stra);  //phopho for free format date
            if (stra.trim().equals("")==false)
            if (!comm.checkDateFormat(stra, "yyyyMMdd")) {
                errStr = "[JCIC建檔通知日期格式錯誤]";
                errFlag = 1;
            }
            hClltJcicCreateDate = stra;
            stra = comm.getStr(str600, 2, ",");
            hClltId = stra;
            hClltIdPSeqno = selectCrdIdno(stra);
            stra = comm.getStr(str600, 3, ",");
            hClltChiName = stra;
            stra = comm.getStr(str600, 4, ",");
            hClltBankCode = stra;
            stra = comm.getStr(str600, 5, ",");
            hClltBankName = stra;
            stra = comm.getStr(str600, 6, ",");
            hClltRBankCode = stra;
            stra = comm.getStr(str600, 7, ",");
            hClltRBankName = stra;
            stra = comm.getStr(str600, 8, ",");
            stra = comm.getDateFreeFormat(stra);  //phopho for free format date
            if (stra.trim().equals("")==false)
            if (!comm.checkDateFormat(stra, "yyyyMMdd")) {
                errStr = "[申請日期格式錯誤]";
                errFlag = 1;
            }
            hClltApplyDate = stra;
            stra = comm.getStr(str600, 9, ",");
            stra = comm.getDateFreeFormat(stra);  //phopho for free format date
            if (stra.trim().equals("")==false)
            if (!comm.checkDateFormat(stra, "yyyyMMdd")) {
                errStr = "[申請日期格式錯誤]";
                errFlag = 1;
            }
            hClltAprDate = stra;
            stra = comm.getStr(str600, 10, ",");
            hClltCloseReason = stra;
            stra = comm.getStr(str600, 11, ",");
            hClltCloseDesc = stra;
            if ((errFlag != 0) || (warnFlag != 0)) {
                printDetail();
                if (errFlag != 0) {
                    errorCnt++;
                    continue;
                }
                if (warnFlag != 0)
                    warningCnt++;
            }

            insertColLiauNegoLst();
        }
        if (recCnt != totalCnt - 2) {
            buf = String.format("資料筆數[%d]與實際筆數不符[%d]", recCnt, totalCnt - 2);
            lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
            errorCnt++;
        }
        //br.close();
        closeInputText(fptr1);
    }

    /***********************************************************************/
    String selectCrdIdno(String asIdNo) throws Exception {
    	String outIdPSeqno = "";
        sqlCmd = "select id_p_seqno from crd_idno where id_no = ? ";
        setString(1, asIdNo);
        
        if (selectTable() > 0) {
        	outIdPSeqno = getValue("id_p_seqno");
        }

        return outIdPSeqno;
    }
    
    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        if (lineCnt >= 50) {
            printHeader();
            lineCnt = 0;
        }

        buf = "";
        buf = comcr.insertStr(buf, hClltId, 1);
        buf = comcr.insertStr(buf, hClltJcicCreateDate, 15);
        buf = comcr.insertStr(buf, hClltBankCode, 26);
        buf = comcr.insertStr(buf, hClltBankName, 40);
        buf = comcr.insertStr(buf, hClltRBankCode, 56);
        buf = comcr.insertStr(buf, hClltRBankName, 71);
        buf = comcr.insertStr(buf, hClltApplyDate, 87);
        buf = comcr.insertStr(buf, hClltAprDate, 100);
        buf = comcr.insertStr(buf, errStr, 111);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void printHeader() throws Exception {
    	
        buf = "";
        pageCnt++;
        buf = comcr.insertStr(buf, "報表名稱: ColA540R1", 1);
        buf = comcr.insertStr(buf, "無擔保展延方案暨展延結果", 50);
        buf = comcr.insertStr(buf, "頁    次:", 111);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 124);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日期:", 111);
        buf = comcr.insertStr(buf, chinDate, 121);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "轉入日期:", 1);
        szTmp = String.format("%8d", comcr.str2long(hBusiBusinessDate));
        buf = comcr.insertStr(buf, szTmp, 10);
        buf = comcr.insertStr(buf, "檔案名稱:", 41);
        buf = comcr.insertStr(buf, hEflgFileName, 51);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "JCIC建檔", 15);
        buf = comcr.insertStr(buf, "協議完成", 100);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "身份證字號", 1);
        buf = comcr.insertStr(buf, "通知日期", 15);
        buf = comcr.insertStr(buf, "受理銀行代號", 26);
        buf = comcr.insertStr(buf, "受理銀行名稱", 40);
        buf = comcr.insertStr(buf, "報送銀行代號", 56);
        buf = comcr.insertStr(buf, "報送銀行名稱", 71);
        buf = comcr.insertStr(buf, "申請日期", 87);
        buf = comcr.insertStr(buf, "/結案日期", 99);
        buf = comcr.insertStr(buf, "錯誤原因", 111);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "\n";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void insertColLiauNegoLst() throws Exception {
    	daoTable = "col_liau_nego_lst";
    	extendField = daoTable + ".";
        setValue(extendField+"file_date", hEflgFileDate);
        setValue(extendField+"jcic_create_date", hClltJcicCreateDate);
        setValue(extendField+"id_p_seqno", hClltIdPSeqno);
        setValue(extendField+"id_no", hClltId);
        setValue(extendField+"chi_name", hClltChiName);
        setValue(extendField+"bank_code", hClltBankCode);
        setValue(extendField+"bank_name", hClltBankName);
        setValue(extendField+"r_bank_code", hClltRBankCode);
        setValue(extendField+"r_bank_name", hClltRBankName);
        setValue(extendField+"apply_date", hClltApplyDate);
        setValue(extendField+"apr_date", hClltAprDate);
        setValue(extendField+"close_reason", hClltCloseReason);
        setValue(extendField+"close_desc", hClltCloseDesc);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liau_nego_lst duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void printTailer() throws Exception {
        buf = "\n";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "失  敗: ", 10);
        szTmp = comcr.commFormat("3z,3z,3z", errorCnt);
        buf = comcr.insertStr(buf, szTmp, 20);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void updateEcsFtpLog() throws Exception {
        daoTable = "ecs_ftp_log";
        updateSQL = "proc_code = decode(cast(? as integer),0,'1','9'),";
        updateSQL += " proc_desc = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm = ? ";
        whereStr = "where rowid =? ";
        setInt(1, errorCnt);
        setString(2, hEflgProcDesc);
        setString(3, javaProgram);
        setRowId(4, hEflgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ecs_ftp_log not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA540 proc = new ColA540();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/14  V1.00.00    phopho     program initial                          *
*  109/06/12  V1.00.01    phopho     CR: 審核結果1,2,3,4皆轉入                                                        *
*  109/06/18  V1.00.03    phopho     Mantis 0003654: fix bug payment_rate     *
*  109/07/09  V1.00.01    phopho     CR add log to DB: ptr_batch_rpt          *
*  109/12/10  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

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

public class ColA510 extends AccessDAO {
    private String progname = "無擔保債務-展延方案暨資格審核結果媒體轉入處理程式 109/12/10  V1.00.02  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "ColA510R1";
    String rptDesc1 = "無擔保展延方案暨資格審核結果";
    int rptSeq1 = 0;
    String buf = "";
    String szTmp = "";
    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTempSystime = "";
    String hEflgProcCode = "";
    String hEflgRowid = "";
    String hEflgFileName = "";
    String hEflgFileDate = "";
    String hCluoApplyReltCode = "";
    String hCluoJcicCreateDate = "";
    String hCluoId = "";
    String hCluoChiName = "";
    String hCluoBankCode = "";
    String hCluoBankName = "";
    String hCluoApplyDate = "";
    String hCluoDebitorAttr1 = "";
    String hCluoDebitorAttr2 = "";
    String hCluoDebitorAttr3 = "";
    String hCluoNotifyDate = "";
    String hCluoNotifyDesc = "";
    String hCluoIdPSeqno = "";
    String hCluoPSeqno = "";
    String hCluoRowid = "";
    String hClncIdCode = "";
    String hClncIdPSeqno = "";
    String hClncAcctType = "";
    String hClncPSeqno = "";
    String hClncPaymentRate = "";
    String hClncAcctJrnlBal = "";
    String hEflgProcDesc = "";
    String hTempApplyReltCode = "";
    String hTempPaymentRate = "";
    int hTempMonths = 0;

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
            if (args.length !=  0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ColA510 file_date [force_flag]", "");
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

            tmpstr = String.format("%4.4s.%2.2s.%2.2sZ68-ZZM330.csv", hEflgFileDate, hEflgFileDate.substring(4),
                    hEflgFileDate.substring(6));
            hEflgFileName = tmpstr;

            selectEcsFtpLog();
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
        fptr1 = openInputText(temstr1, "MS950");
        if (fptr1 == -1) {
            comcr.errRtn(String.format("error: [%s] 檔案不存在", temstr1), "", hCallBatchSeqno);
        }

        temstr2 = String.format("%s/reports/COL_A510_%s.txt", comc.getECSHOME(), hEflgFileName);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
    }

    /***********************************************************************/
    void readFile() throws Exception {
        String str600 = "";
        String stra = "";

        printHeader();
        totalCnt = 0;
        
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
            hCluoJcicCreateDate = stra;
            stra = comm.getStr(str600, 2, ",");
            hCluoId = stra;
            stra = comm.getStr(str600, 3, ",");
            hCluoChiName = stra;
            stra = comm.getStr(str600, 4, ",");
            hCluoBankCode = stra;
            stra = comm.getStr(str600, 5, ",");
            hCluoBankName = stra;
            stra = comm.getStr(str600, 6, ",");
            stra = comm.getDateFreeFormat(stra);  //phopho for free format date
            if (stra.trim().equals("")==false)
            if (!comm.checkDateFormat(stra, "yyyyMMdd")) {
                errStr = "[申請日期格式錯誤]";
                errFlag = 1;
            }
            hCluoApplyDate = stra;
            hCluoDebitorAttr1 = "";
            hCluoDebitorAttr2 = "";
            hCluoDebitorAttr3 = "";
            stra = comm.getStr(str600, 7, ",");
            if (stra.length() > 0) {
                tmpstr = String.format("%1.1s", stra);
                hCluoDebitorAttr1 = tmpstr;
            }
            if (stra.length() > 1) {
                tmpstr = String.format("%1.1s", comc.getSubString(stra,1));
                hCluoDebitorAttr2 = tmpstr;
            }
            if (stra.length() > 2) {
                tmpstr = String.format("%1.1s", comc.getSubString(stra,2));
                hCluoDebitorAttr3 = tmpstr;
            }
            stra = comm.getStr(str600, 8, ",");
            hCluoApplyReltCode = stra;
            stra = comm.getStr(str600, 9, ",");
            stra = comm.getDateFreeFormat(stra);  //phopho for free format date
            if (stra.trim().equals("")==false)
            if (!comm.checkDateFormat(stra, "yyyyMMdd")) {
                errStr = "[通知日期格式錯誤]";
                errFlag = 1;
            }
            hCluoNotifyDate = stra;
            stra = comm.getStr(str600, 10, ",");
            hCluoNotifyDesc = stra;
            warnFlag = checkProcStatus();

            if ((errFlag != 0) || (warnFlag != 0)) {
                printDetail();
                if (errFlag != 0) {
                    errorCnt++;
                    continue;
                }
                if (warnFlag != 0)
                    warningCnt++;
            }

            int retInt = selectColLiauNego();
            if (retInt != 0) {
                selectActAcno();
                insertColLiauNego();
                if (hCluoApplyReltCode.toCharArray()[0] <= '2')
                    insertColLiauRemod(1);

            } else {
                if (hCluoApplyReltCode.toCharArray()[0] >= hTempApplyReltCode.toCharArray()[0]) {
                    updateColLiauNego();
                }
                if (hCluoApplyReltCode.toCharArray()[0] >= '3')
                    insertColLiauRemod(3);
            }
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
    int checkProcStatus() throws Exception {
        int retInt = selectColLiauNego();
        //if ((ret_int != 0) && (!h_cluo_apply_relt_code.substring(0, 1).equals("1"))) {
        //2020.6.12 phopho CR-審核結果1,2,3,4皆轉入
        if ((retInt != 0) && (!comc.getSubString(hCluoApplyReltCode,0,1).equals("1"))
        		&& (!comc.getSubString(hCluoApplyReltCode,0,1).equals("2"))
        		&& (!comc.getSubString(hCluoApplyReltCode,0,1).equals("3"))
        		&& (!comc.getSubString(hCluoApplyReltCode,0,1).equals("4"))) {
            errStr = "[審核結果無申請]";
            return 1;
        }
        if ((retInt == 0) && (hCluoApplyReltCode.toCharArray()[0] < hTempApplyReltCode.toCharArray()[0])) {
            errStr = "[審核結果順序錯誤]";
            return 1;
        }

        if ((retInt == 0) && (hCluoApplyReltCode.toCharArray()[0] == hTempApplyReltCode.toCharArray()[0])) {
            errStr = "[資料重複轉入錯誤]";
            return 1;
        }
        return 0;
    }

    /***********************************************************************/
    void printDetail() throws Exception {

        lineCnt++;
        if (lineCnt >= 50) {
            printHeader();
            lineCnt = 0;
        }

        buf = "";
        buf = comcr.insertStr(buf, hCluoId, 1);
        buf = comcr.insertStr(buf, hCluoJcicCreateDate, 15);
        buf = comcr.insertStr(buf, hCluoBankCode, 28);
        buf = comcr.insertStr(buf, hCluoBankName, 40);
        buf = comcr.insertStr(buf, hCluoApplyDate, 55);
        buf = comcr.insertStr(buf, hCluoApplyReltCode, 71);
        buf = comcr.insertStr(buf, hCluoNotifyDate, 88);
        buf = comcr.insertStr(buf, errStr, 106);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void printHeader() throws Exception {
    	
        buf = "";
        pageCnt++;
        buf = comcr.insertStr(buf, "報表名稱: ColA510R1", 1);
        buf = comcr.insertStr(buf, "無擔保展延方案暨資格審核結果", 49);
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
        buf = comcr.insertStr(buf, "通知日期", 89);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "身份證字號", 1);
        buf = comcr.insertStr(buf, "通知日期", 15);
        buf = comcr.insertStr(buf, "銀行代號", 28);
        buf = comcr.insertStr(buf, "銀行名稱", 40);
        buf = comcr.insertStr(buf, "申請日期", 58);
        buf = comcr.insertStr(buf, "審核結果", 74);
        buf = comcr.insertStr(buf, "/結案日期", 88);
        buf = comcr.insertStr(buf, "錯誤原因", 109);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "\n";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    int selectColLiauNego() throws Exception {
        hTempApplyReltCode = "0";
        hCluoRowid = "";
        sqlCmd = "select apply_relt_code,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from col_liau_nego  ";
        sqlCmd += "where id_no = ?  ";
        sqlCmd += "and apply_date = ? ";
        setString(1, hCluoId);
        setString(2, hCluoApplyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempApplyReltCode = getValue("apply_relt_code");
            hCluoRowid = getValue("rowid");
        } else
            return 1;
        return 0;
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hCluoIdPSeqno = "";
        hCluoPSeqno = "";
        
        selectUfIdnoPseqno(); //找出 id_no 的 id_p_seqno
        if (hCluoIdPSeqno.equals("")) return;

        sqlCmd = "select ";
        sqlCmd += "a.acct_jrnl_bal,";
        sqlCmd += "b.id_p_seqno,";
        //sqlCmd += "b.acct_holder_id_code,";
        sqlCmd += "substr(b.acct_key,11,1) acct_holder_id_code,";
        //sqlCmd += "b.p_seqno,";
        sqlCmd += "b.acno_p_seqno,";
        sqlCmd += "b.acct_type,";
        sqlCmd += "decode(b.payment_rate1,'','  ',b.payment_rate1)||decode(b.payment_rate2,'','  ',b.payment_rate2)||decode(b.payment_rate3,'','  ',b.payment_rate3)||";
        sqlCmd += "decode(b.payment_rate4,'','  ',b.payment_rate4)||decode(b.payment_rate5,'','  ',b.payment_rate5)||decode(b.payment_rate6,'','  ',b.payment_rate6)||";
        sqlCmd += "decode(b.payment_rate7,'','  ',b.payment_rate7)||decode(b.payment_rate8,'','  ',b.payment_rate8)||decode(b.payment_rate9,'','  ',b.payment_rate9)||";
        sqlCmd += "decode(b.payment_rate10,'','  ',b.payment_rate10)||decode(b.payment_rate11,'','  ',b.payment_rate11)||decode(b.payment_rate12,'','  ',b.payment_rate12)||";
        sqlCmd += "decode(b.payment_rate13,'','  ',b.payment_rate13)||decode(b.payment_rate14,'','  ',b.payment_rate14)||decode(b.payment_rate15,'','  ',b.payment_rate15)||";
        sqlCmd += "decode(b.payment_rate16,'','  ',b.payment_rate16)||decode(b.payment_rate17,'','  ',b.payment_rate17)||decode(b.payment_rate18,'','  ',b.payment_rate18)||";
        sqlCmd += "decode(b.payment_rate19,'','  ',b.payment_rate19)||decode(b.payment_rate20,'','  ',b.payment_rate20)||decode(b.payment_rate21,'','  ',b.payment_rate21)||";
        sqlCmd += "decode(b.payment_rate22,'','  ',b.payment_rate22)||decode(b.payment_rate23,'','  ',b.payment_rate23)||decode(b.payment_rate24,'','  ',b.payment_rate24)||";
        sqlCmd += "decode(b.payment_rate25,'','  ',b.payment_rate25) h_temp_payment_rate,";
        sqlCmd += "months_between(to_date(?,'yyyymmdd'),to_date(?,'yyyymmdd')) h_temp_months ";
        sqlCmd += "from act_acct a,act_acno b ";
        //sqlCmd += "where a.p_seqno = b.p_seqno ";
        //sqlCmd += "and b.p_seqno = b.acct_p_seqno ";
        sqlCmd += "where a.p_seqno = b.acno_p_seqno ";
        sqlCmd += "and b.acno_flag <> 'Y' ";
        sqlCmd += "and b.id_p_seqno = ? ";
        sqlCmd += "order by a.acct_jrnl_bal desc ";
        setString(1, hBusiBusinessDate);
        setString(2, hEflgFileDate);
        setString(3, hCluoIdPSeqno);

        openCursor();
        int i = 0;
        while (fetchTable()) {
            hClncAcctJrnlBal = getValue("acct_jrnl_bal");
            hClncIdPSeqno = getValue("id_p_seqno");
            hClncIdCode = getValue("acct_holder_id_code");
            //h_clnc_p_seqno = getValue("p_seqno");
            hClncPSeqno = getValue("acno_p_seqno");
            hClncAcctType = getValue("acct_type");
            hTempPaymentRate = getValue("h_temp_payment_rate");
            hTempMonths = getValueInt("h_temp_months");
            //Mantis 0003654: fix bug payment_rate 2020.6.18 phopho
            hClncPaymentRate = comc.getSubString(hTempPaymentRate,0,24);

            if (i == 0) {
                //h_cluo_id_p_seqno = h_clnc_id_p_seqno;
                hCluoPSeqno = hClncPSeqno;
            }
            insertColLiauNegoAct();
            i++;
        }
        closeCursor();
    }
    
    /***********************************************************************/
    void selectUfIdnoPseqno() throws Exception {
        sqlCmd = "select uf_idno_pseqno(?) as id_p_seqno from dual";
        setString(1, hCluoId);
        if (selectTable() > 0) {
        	hCluoIdPSeqno = getValue("id_p_seqno");
        }
    }

    /***********************************************************************/
    void insertColLiauNegoAct() throws Exception {
    	daoTable = "col_liau_nego_act";
    	extendField = daoTable + ".";
        setValue(extendField+"id_no", hCluoId);
        setValue(extendField+"id_code", hClncIdCode);
        setValue(extendField+"apply_date", hCluoApplyDate);
        setValue(extendField+"id_p_seqno", hClncIdPSeqno);
        setValue(extendField+"acct_type", hClncAcctType);
        setValue(extendField+"p_seqno", hClncPSeqno);
        setValue(extendField+"payment_rate", hClncPaymentRate);
        setValue(extendField+"acct_jrnl_bal", hClncAcctJrnlBal);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liau_nego_act duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertColLiauNego() throws Exception {
    	daoTable = "col_liau_nego";
    	extendField = daoTable + ".";
        setValue(extendField+"file_date", hEflgFileDate);
        setValue(extendField+"liau_status", hCluoApplyReltCode);
        setValue(extendField+"jcic_create_date", hCluoJcicCreateDate);
        setValue(extendField+"id_no", hCluoId);
        setValue(extendField+"chi_name", hCluoChiName);
        setValue(extendField+"bank_code", hCluoBankCode);
        setValue(extendField+"bank_name", hCluoBankName);
        setValue(extendField+"apply_date", hCluoApplyDate);
        setValue(extendField+"apply_relt_code", hCluoApplyReltCode);
        setValue(extendField+"debitor_attr1", hCluoDebitorAttr1);
        setValue(extendField+"debitor_attr2", hCluoDebitorAttr2);
        setValue(extendField+"debitor_attr3", hCluoDebitorAttr3);
        setValue(extendField+"notify_date", hCluoNotifyDate);
        setValue(extendField+"notify_desc", hCluoNotifyDesc);
        setValue(extendField+"id_p_seqno", hCluoIdPSeqno);
        setValue(extendField+"p_seqno", hCluoPSeqno);
        setValue(extendField+"create_date", sysDate);
        setValue(extendField+"create_time", sysTime);
        setValue(extendField+"proc_flag", hCluoApplyReltCode);
        setValue(extendField+"proc_date", hBusiBusinessDate);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liau_nego duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColLiauNego() throws Exception {
        daoTable = "col_liau_nego";
        updateSQL = "file_date  = ?,";
        updateSQL += " liau_status  = decode(cast(? as varchar(1)),'1','1','2','1','3'),";
        updateSQL += " jcic_create_date = ?,";
        updateSQL += " apply_relt_code = ?,";
        updateSQL += " debitor_attr1 = ?,";
        updateSQL += " debitor_attr2 = ?,";
        updateSQL += " debitor_attr3 = ?,";
        updateSQL += " notify_date  = ?,";
        updateSQL += " notify_desc  = ?,";
        updateSQL += " crt_user1  = decode(cast(? as varchar(1)),'2','SYSTEM',crt_user1),";
        updateSQL += " crt_date1  = decode(cast(? as varchar(1)),'2',cast(? as varchar(8)),crt_date1),";
        updateSQL += " apr_user1  = decode(cast(? as varchar(1)),'2','SYSTEM',apr_user1),";
        updateSQL += " apr_date1  = decode(cast(? as varchar(1)),'2',cast(? as varchar(8)),apr_date1),";
        updateSQL += " proc_flag  = decode(cast(? as varchar(1)),'1','0','2','1','Y'),";
        updateSQL += " proc_date  = ?,";
        updateSQL += " report_date1  = decode(cast(? as varchar(3)),'017',decode(cast(? as varchar(1)),'2',cast(? as varchar(8)),''),''),";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = ? ";
        whereStr = "where rowid  = ? ";
        setString(1, hEflgFileDate);
        setString(2, hCluoApplyReltCode);
        setString(3, hCluoJcicCreateDate);
        setString(4, hCluoApplyReltCode);
        setString(5, hCluoDebitorAttr1);
        setString(6, hCluoDebitorAttr2);
        setString(7, hCluoDebitorAttr3);
        setString(8, hCluoNotifyDate);
        setString(9, hCluoNotifyDesc);
        setString(10, hCluoApplyReltCode);
        setString(11, hCluoApplyReltCode);
        setString(12, hCluoNotifyDate);
        setString(13, hCluoApplyReltCode);
        setString(14, hCluoApplyReltCode);
        setString(15, hCluoNotifyDate);
        setString(16, hCluoApplyReltCode);
        setString(17, hBusiBusinessDate);
        setString(18, hCluoBankCode);
        setString(19, hCluoApplyReltCode);
        setString(20, hBusiBusinessDate);
        setString(21, javaProgram);
        setRowId(22, hCluoRowid);
        
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liau_nego not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertColLiauRemod(int hInt) throws Exception {
    	daoTable = "col_liau_remod";
    	extendField = daoTable + ".";
    	setValue(extendField+"id_p_seqno", hCluoIdPSeqno);
    	setValue(extendField+"id_no", hCluoId);
        setValue(extendField+"apply_date", hCluoApplyDate);
        setValue(extendField+"liau_status", (hInt == 1 ? "1" : "3"));
        setValue(extendField+"proc_flag", "N");
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liau_remod duplicate!", "", hCallBatchSeqno);
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
        whereStr = "where rowid = ? ";
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
        ColA510 proc = new ColA510();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

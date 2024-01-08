/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/08/15  V1.00.00    phopho     program initial                          *
*  108/12/02  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/15  V1.00.02    shiyuqi       updated for project coding standard   *
*  109/12/30  V1.00.03    Zuwei       “89822222”改為”23317531”            *
*  110/04/06  V1.00.04    Justin     use common value                         *
******************************************************************************/

package Col;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommJcic;
import com.CommRoutine;

import hdata.jcic.JcicEnum;
import hdata.jcic.JcicHeader;
import hdata.jcic.LRPad;

public class ColC027 extends AccessDAO {
    private String progname = "JCIC界面-產生Z13個別協商媒體檔處理程式   110/04/06  V1.00.04 ";
    private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_Z13;
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    
    String hCcicInstFlag = "";
    String hCcicInstDate = "";
    String hCcicId = "";
    String hCcicIdCode = "";
    String hCcicPSeqno = "";
    String hCcicChiName = "";
    String hCcicTranType = "";
    String hCcicResidentAddr = "";
    String h_ccic_send_acct_no  = "";
    String rptName1  = "";
    String rptName2  = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
    int rptSeq1  = 0;
    int rptSeq2  = 0;
    String buf = "";
    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";
    String hCcicBirthday = "";
    String hCcicInstSeqno = "";
    String hCcicRowid = "";
    String hPrintName = "";
    String hRptName = "";
    String hChgiChiName = "";
    String hChgiCreateUser = "";
    String hChgiApprovUser = "";
    String hChgiIdPSeqno = "";
    String hChgiId = "";
    String hChgiIdCode = "";
    String hChgiPostJcicFlag = "";

    int totalAll = 0;
    int totalCount = 0;
    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hEflgTransSeqno = "";
    String hEflgModPgm = "";
    String hEriaLocalDir = "";
    String msgCode = "";
    String msgDesc = "";
    String temstr = "";
    String temstr1 = "";

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : ColC027 [business_date]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();

            if (args.length == 1)
                hBusiBusinessDate = args[0];

            if (selectColCsInstjcic0() != 0) {
            	exceptExit = 0;
                showLogMessage("I", "", String.format("本日[%s]無Z13資料可產生", hBusiBusinessDate));
            }

            checkOpen();
            
            selectColCsInstjcic();
            buf = String.format("%s%08d%208.208s", CommJcic.TAIL_LAST_MARK, totalCount," ");
            lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
            comc.writeReport(temstr, lpar1);

            printReport();
            comc.writeReport(temstr1, lpar2);
            comcr.lpRtn("COL_C027", "");
            ftpProc();

            showLogMessage("I", "", String.format("累計處理筆數 [%d]", totalAll));
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
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }
    /*************************************************************************/
    void checkOpen() throws Exception
    {
        temstr = String.format("%s/media/col/INST/%s%4.4sz.za2", comc.getECSHOME(), CommJcic.JCIC_BANK_NO, comc.getSubString(hBusiBusinessDate,4));

        JcicHeader jcicHeader = new JcicHeader();
        CommJcic commJcic = new CommJcic(getDBconnect(), getDBalias());
        commJcic.selectContactData(JCIC_TYPE);
        
        jcicHeader.setFileId(commJcic.getPadString(JCIC_TYPE.getJcicId(), 18));
        jcicHeader.setBankNo(commJcic.getPadString(CommJcic.JCIC_BANK_NO, 3));
        jcicHeader.setFiller1(commJcic.getFiller(" ", 5));
        jcicHeader.setSendDate(commJcic.getPadString(comcr.str2long(hBusiBusinessDate) - 19110000, "0", 7, LRPad.L));
        jcicHeader.setFileExt("01");
        jcicHeader.setFiller2(commJcic.getFiller(" ", 10)); 
        jcicHeader.setContactTel(commJcic.getPadString(commJcic.getContactTel(), 16));
        jcicHeader.setContactMsg(commJcic.getPadString(commJcic.getContactMsg(), 80));
        jcicHeader.setFiller3(commJcic.getFiller(" ", 79));
        jcicHeader.setLen("");
        
        buf = jcicHeader.produceStr();
//        buf = String.format("%-18.18s017%5.5s%07d01%10.10s%-16.16s%-80.80s%-79.79s", "JCIC-DAT-ZA02-V01-", " ",
//                comc.str2long(hBusiBusinessDate) - 19110000, " ", "02-23317531#2340", " ", " ");
        
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
        
        temstr1 = String.format("%s/reports/COL_C027_%s", comc.getECSHOME(), hBusiBusinessDate);
    }

    /***********************************************************************/
    int selectColCsInstjcic0() throws Exception {
        sqlCmd = "select 1 ";
        sqlCmd += " from col_cs_instjcic  ";
        sqlCmd += "where decode(proc_flag,'','N',proc_flag) = 'N' ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();

        return recordCnt == 0 ? 1 : 0;
    }

    /***********************************************************************/
    void selectColCsInstjcic() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "inst_flag,";
        sqlCmd += "inst_date,";
        sqlCmd += "id_no,";
        sqlCmd += "id_code,";
        sqlCmd += "p_seqno,";
        sqlCmd += "chi_name,";
        sqlCmd += "tran_type,";
        sqlCmd += "resident_addr,";
        sqlCmd += "send_acct_no,";
        sqlCmd += "birthday,";
        sqlCmd += "inst_seqno,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_cs_instjcic ";
        sqlCmd += "where decode(proc_flag,'','N',proc_flag) = 'N' ";
        sqlCmd += "order by inst_seqno,tran_type ";

        openCursor();
        while (fetchTable()) {
            hCcicInstFlag = getValue("inst_flag");
            hCcicInstDate = getValue("inst_date");
            hCcicId = getValue("id_no");
            hCcicIdCode = getValue("id_code");
            hCcicPSeqno = getValue("p_seqno");
            hCcicChiName = getValue("chi_name");
            hCcicTranType = getValue("tran_type");
            hCcicResidentAddr = getValue("resident_addr");
            h_ccic_send_acct_no  = getValue("send_acct_no");
            hCcicBirthday = getValue("birthday");
            hCcicInstSeqno = getValue("inst_seqno");
            hCcicRowid = getValue("rowid");

            totalAll++;
            selectCrdChgId();
            if (hChgiPostJcicFlag.equals("N")) {
                insertCrdNopassJcic();
                updateColCsInstjcic();
                continue;
            }
            buf = String.format("6%1.1s%-7.7s%-10.10s%-20.20s%07d%-66.66s%9.9s2%07d%1.1s%07d%-50.50s%33s",
                    hCcicTranType, CommJcic.JCIC_BANK_NO, hCcicId, hCcicChiName, comcr.str2long(hCcicBirthday) - 19110000,
                    hCcicResidentAddr, " ", comcr.str2long(hBusiBusinessDate) - 19110000, hCcicInstFlag,
                    comcr.str2long(hCcicInstDate) - 19110000, h_ccic_send_acct_no, " ");
            lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

            totalCount++;
            if (totalCount % 1000 == 0) {
                showLogMessage("I", "", String.format("    處理筆數 [%d]", totalCount));
            }
            updateColCsInstbase();
            updateColCsInstjcic();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectCrdChgId() throws Exception {
        hChgiCreateUser = "";
        hChgiApprovUser = "";
        hChgiChiName = "";
        hChgiId = "";
        hChgiIdCode = "";
        hChgiPostJcicFlag = "";

        sqlCmd = "select chi_name,";
        sqlCmd += "crt_user,";
        sqlCmd += "apr_user,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "id_no,";
        sqlCmd += "id_no_code,";
        sqlCmd += "decode(post_jcic_flag,'','N',post_jcic_flag) h_chgi_post_jcic_flag ";
        sqlCmd += " from crd_chg_id ";
        sqlCmd += "where old_id_no = ?  ";
        sqlCmd += "and   old_id_no_code = ? ";
        setString(1, hCcicId);
        setString(2, hCcicIdCode);
        
        extendField = "crd_chg_id.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgiChiName = getValue("crd_chg_id.chi_name");
            hChgiCreateUser = getValue("crd_chg_id.crt_user");
            hChgiApprovUser = getValue("crd_chg_id.apr_user");
            hChgiIdPSeqno = getValue("crd_chg_id.id_p_seqno");
            hChgiId = getValue("crd_chg_id.id_no");
            hChgiIdCode = getValue("crd_chg_id.id_no_code");
            hChgiPostJcicFlag = getValue("crd_chg_id.h_chgi_post_jcic_flag");
        }
    }

    /***********************************************************************/
    void insertCrdNopassJcic() throws Exception {
    	daoTable = "crd_nopass_jcic";
    	extendField = daoTable + ".";
        setValue(extendField+"old_id_no", hCcicId);
        setValue(extendField+"old_id_no_code", hCcicIdCode);
        setValue(extendField+"chi_name", hChgiChiName);
//        setValue("id_no", h_chgi_id);
        setValue(extendField+"id_p_seqno", hChgiIdPSeqno);
        setValue(extendField+"post_kind", "Z13");
        setValue(extendField+"post_jcic_date", sysDate);
        setValue(extendField+"card_no", "");
        setValue(extendField+"oppost_reason", "");
        setValue(extendField+"oppost_date", "");
        setValue(extendField+"mod_user", javaProgram);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_nopass_jcic duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColCsInstjcic() throws Exception {
        daoTable = "col_cs_instjcic";
        updateSQL = "proc_date = ?,";
        updateSQL += " proc_flag = 'Y',";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm = ? ";
        whereStr = "where rowid  = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hCcicRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_cs_instjcic not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColCsInstbase() throws Exception {
        daoTable = "col_cs_instbase";
        updateSQL = "crt_send_date = decode(cast(? as varchar(1)),'1',?,crt_send_date),";
        updateSQL += " repudiate_send_date = decode(cast(? as varchar(1)),'3',?, repudiate_send_date),";
        updateSQL += " close_send_date  = decode(cast(? as varchar(1)),'2',?,'4',?,close_send_date),";
        updateSQL += " proc_date   = ?,";
        updateSQL += " proc_flag   = decode(cast(? as varchar(1)),'2','Y','4','Y',cast(? as varchar(1))),";
        updateSQL += " mod_time   = sysdate,";
        updateSQL += " mod_pgm    = ? ";
        whereStr = "where inst_seqno   = ? ";
        setString(1, hCcicInstFlag);
        setString(2, hBusiBusinessDate);
        setString(3, hCcicInstFlag);
        setString(4, hBusiBusinessDate);
        setString(5, hCcicInstFlag);
        setString(6, hBusiBusinessDate);
        setString(7, hBusiBusinessDate);
        setString(8, hBusiBusinessDate);
        setString(9, hCcicInstFlag);
        setString(10, hCcicInstFlag);
        setString(11, javaProgram);
        setString(12, hCcicInstSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_cs_instbase not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void printReport() {
        String cDate = "";
        String temstr = "";
        String szTmp = "";
        String szTmp1 = "";

        buf = "";
        buf = comcr.insertStr(buf, " " + comcr.bankName + " ", 26);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱 : ColC027", 1);

        buf = comcr.insertStrCenter(buf, "報送JCIC個別協商(Z13)資料每日總數報表", 80);

        buf = comcr.insertStr(buf, "頁次:", 72);
        temstr = String.format("%04d", 1);
        buf = comcr.insertStr(buf, temstr, 77);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        temstr = String.format("%4.4s", hBusiBusinessDate);

        buf = "";
        buf = comcr.insertStr(buf, "單    位 :", 1);
        buf = comcr.insertStr(buf, "109", 12);
        buf = comcr.insertStr(buf, "交易日期:", 58);
        cDate = String.format("%03d年%2.2s月%2.2s日", comcr.str2long(temstr), hBusiBusinessDate.substring(4),
                hBusiBusinessDate.substring(6));
        buf = comcr.insertStr(buf, cDate, 68);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        for (int i = 0; i < 80; i++)
            buf += "-";
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        szTmp = comcr.commFormat("3z,3z", totalCount);
        szTmp1 = String.format("產生資料 : %2.2s月%2.2s日  筆 數  : %s 筆", hBusiBusinessDate.substring(4),
                hBusiBusinessDate.substring(6), szTmp);
        buf = comcr.insertStr(buf, szTmp1, 1);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        for (int i = 0; i < 80; i++)
            buf += "-";
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = comcr.insertStr(buf, "備註 1 : 每日CS傳送ECS報送資料", 1);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = comcr.insertStr(buf, "備註 2 : 資料格式依據JCIC民國98年3月版本", 1);
        lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2, "0", buf));
    }

    /***********************************************************************/
    void ftpProc() throws Exception {
        String tojcicmsg = "";
        boolean retCode ;
        // ======================================================
        // FTP

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "JCIC_FTP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "Z13"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "TOJCIC"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/col/INST", comc.getECSHOME());
        commFTP.hEflgModPgm = this.getClass().getName();
        String hEflgRefIpCode = "JCIC_FTP";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("mput %s%4.4sz.za2", CommJcic.JCIC_BANK_NO, hBusiBusinessDate.substring(4));
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        
        //phopho add 2019.5.27 無檔案的return message須自己判斷
        if (errCode == 0 && commFTP.fileList.size() == 0) {
        	showLogMessage("I", "", String.format("[%-20.20s] => [無資料可傳送]", procCode));
        }

        if (errCode != 0) {
            //showLogMessage("I", "", String.format("[%s] => [%s] error", h_eflg_ref_ip_code, commFTP.h_eflg_ftp_desc));
            showLogMessage("I", "", String.format("[%s]檔案傳送JCIC_FTP有誤(error), 請通知相關人員處理", procCode)); /* 改回JCIC_FTP */
            /*** SENDMSG ***/
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"col_c027執行完成 傳送JCIC失敗[%s]\"", temstr1);
            retCode = comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", String.format("%s [%s]", tojcicmsg, String.valueOf(retCode)));
        } else {
            /*** SENDMSG ***/
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"col_c027執行完成 傳送JCIC無誤[%s]\"", temstr1);
            retCode = comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", String.format("%s [%s]", tojcicmsg, String.valueOf(retCode)));
        }

        // ==================================================

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC027 proc = new ColC027();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

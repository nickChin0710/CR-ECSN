/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/03/26  V1.00.01    Pino      for TCB                                   *
*  109/12/23  V1.00.01   shiyuqi       updated for project coding standard   *
*  110/04/01  V1.00.02   Justin     use common value                          *
*  110/08/17  V1.00.04    Wilson    中文姓名超逾10個字之全名需求                                                             *
*  112/06/13  V1.00.05    Wilson    cntry_code宣告變數調整                                                                 *
*  112/07/05  V1.00.06    Wilson    調整FTP參數                                                                                            *
*  112/08/24  V1.00.07    Wilson    修正職業代碼問題                                                                                      *
******************************************************************************/

package Crd;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

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

//import Crd.CrdF042.buf1;
//import Crd.CrdF042.buft2;

/*產生每日人工建檔送JCIC卡戶資料*/
public class CrdF043 extends AccessDAO {
	private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_KK1;
    private String progname = "產生每日人工建檔送JCIC卡戶資料   112/08/24  V1.00.07  ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String prgmId = "CrdF043";
    String rptName1 = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq1 = 0;
    String buf = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hDay = "";
    String pNewFilename = "";
    String pFilename = "";
    String hCrtDate = "";
    String hJcidId = "";
    String hJcidIdCode = "";
    String hJcidCrtDate = "";
    String hJcidTransType = "";
    String hJcidAccountStyle = "";
    String hJcidIdPSeqno = "";
    String hJcidChiName = "";
    String hJcidEngName = "";
    String hJcidBirthday = "";
    String hJcidMailZip = "";
    String hJcidMailAddr = "";
    String hJcidResidentAddr = "";
    String hJcidResidentFlag = "";
    String hJcidTelNo = "";
    String hJcidCellarPhone = "";
    String hJcidBusinessId = "";
    String hJcidBusinessCode = "";
    String hObuId = "";
    String hJcidCompanyName = "";
    String hJcidOfficeTelNo = "";
    String hJcidJobPosition = "";
    int hJcidServiceYear = 0;
    double hJcidSalary = 0;
    String hJcidEducation = "";
    String hJcidUpdateDate = "";
    String hJcidCrtUser = "";
    String hIdnoAnnualDate = "";
    String hJcidModTime = "";
    String hJcidSex = "";
    String hJcidCntryCode = "";
    String hJcidPassportNo = "";
    String hJcidPassportDate = "";
    String hJcidRowid = "";
    String hCardIssueDate = "";
    String hJcicBusCode = "";
    String hFileName = "";
    int total = 0;
    String hContactTel = "";
    String hContactMsg = "";
    String hChgiChiName = "";
    String hChgiCreateUser = "";
    String hChgiApprovUser = "";
    String hChgiIdPSeqno = "";
    String hChgiPostJcicFlag = "";
    String hJcidIndigenousName ="";

    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hEflgTransSeqno = "";
    String hEflgModPgm = "";
    String hEriaLocalDir = "";
    String temstr = "";
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String msgCode = "";
    String msgDesc = "";
    String foreignFlag = "";
    int fileSeqno = 0;
    int totalAll = 0;
    //int err_code = 0;

    Bufh hdata = new Bufh();
    Buft htail = new Buft();
    Buf1 data = new Buf1();

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : CrdF043 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            total = 0;
            openTextFile();

            process();

            String filename = String.format("%s/media/crd/%s", comc.getECSHOME(), hFileName);
            stderr = String.format("FILENAME [%s] temstr=[%s]\n", hFileName, filename);
            showLogMessage("I", "", stderr);
            comc.writeReport(filename, lpar1, "MS950");
            
            if (total > 0) {
                ftpProc("JCIC");
                ftpProc("CRDATACREA");
                insertFileCtl();
                renameFile1(hFileName);
            } else {
                showLogMessage("I", "", String.format("NO DATA RM FILE[%s]", filename));
                comc.fileDelete(filename);
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
    void openTextFile() throws Exception {
        String tmp = "";
        String hDay = "";
        String hCrtDate = "";
        String pFilename = "";
        String pNewFilename = "";
        

        hCrtDate = "";
        hFileName = "";
        pFilename = "";
        pNewFilename = "";
        hDay = "";
        sqlCmd = "select to_char(sysdate,'mmdd') as h_day ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDay = getValue("h_day");
        }

        String filename = String.format("%s%4s", CommJcic.JCIC_BANK_NO, hDay);
        pFilename = String.format("%s%%", filename);
        hCrtDate = sysDate;
        sqlCmd = "select max(file_name) as p_new_filename ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
        sqlCmd += "and crt_date = ?  ";
        sqlCmd += "and check_code = '1' ";
        setString(1, pFilename);
        setString(2, hCrtDate);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            pNewFilename = getValue("p_new_filename");
        }
        fileSeqno = 0;
        if ((pNewFilename.length() > 0)) {
            showLogMessage("I", "", String.format("OLD_FILE_NAME[%s] ", pNewFilename));
            tmp = pNewFilename.substring(7, 8);
            fileSeqno = comcr.str2int(tmp) + 1;
            showLogMessage("I", "", String.format("SEQNO=[%d]", fileSeqno));
        } else
            fileSeqno = 1;
        filename = String.format("%s%1d.kk1", filename, fileSeqno);
        hFileName = filename;
        rptName1 = filename;
        temstr = String.format("%s/media/crd/%s", comc.getECSHOME(), hFileName);
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", String.format("FILENAME [%s] temstr=[%s]", filename, temstr));

        headerFile();
    }

    /***********************************************************************/
    void headerFile() throws Exception {
        String temp = "";
        
        JcicHeader jcicHeader = new JcicHeader();
        CommJcic commJcic = new CommJcic(getDBconnect(), getDBalias());
        commJcic.selectContactData(JCIC_TYPE);
        
        jcicHeader.setFileId(commJcic.getPadString(JCIC_TYPE.getJcicId(), 18));
        jcicHeader.setBankNo(commJcic.getPadString(CommJcic.JCIC_BANK_NO, 3));
        jcicHeader.setFiller1(commJcic.getFiller(" ", 5));
        jcicHeader.setSendDate(commJcic.getPadString(chinDate, "0", 7, LRPad.L));
        jcicHeader.setFileExt(commJcic.getPadString(fileSeqno, "0", 2, LRPad.L));
        jcicHeader.setFiller2(commJcic.getFiller(" ", 10)); 
        jcicHeader.setContactTel(commJcic.getPadString(commJcic.getContactTel(), 16));
        jcicHeader.setContactMsg(commJcic.getPadString(commJcic.getContactMsg(), 80));
//        jcicHeader.setFiller3(commJcic.getFiller(" ", 249));
//        jcicHeader.setLen(commJcic.getFiller(" ", 1));
        
        buf = jcicHeader.produceStr();
//        selectContactData();
//        hdata.fileId = JCIC_TYPE.getJcicId();
//        hdata.bankNo = CommJcic.JCIC_BANK_NO;
//        temp = String.format("%07d", comcr.str2long(chinDate));
//        hdata.sendDate = temp;
//        temp = String.format("%02d", fileSeqno);
//        hdata.fileExt = temp;
//        temp = String.format("%-16s", hContactTel);
//        hdata.contactTel = temp;
//        temp = String.format("%-80s", hContactMsg);
//        hdata.contactMsg = temp;
//        buf = hdata.allText();
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

    }

    /***********************************************************************/
    void selectContactData() throws Exception {
        hContactTel = "";
        hContactMsg = "";
        sqlCmd = "select wf_value,";
        sqlCmd += "wf_value2 ";
        sqlCmd += " from ptr_sys_parm  ";
        sqlCmd += "where wf_parm = 'JCIC_FILE'  ";
        sqlCmd += "and wf_key = 'CONTACT' ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_sys_parm not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hContactTel = getValue("wf_value");
            hContactMsg = getValue("wf_value2");
        }
    }

    /***********************************************************************/
    void process() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "b.id_no,";
        sqlCmd += "b.id_no_code,";
        sqlCmd += "a.crt_date,";
        sqlCmd += "a.trans_type,";
        sqlCmd += "a.account_style,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.chi_name,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "a.birthday,";
        sqlCmd += "a.mail_zip,";
        sqlCmd += "a.mail_addr,";
        sqlCmd += "a.resident_addr,";
        sqlCmd += "a.resident_flag,";
        sqlCmd += "a.tel_no,";
        sqlCmd += "a.cellar_phone,";
        sqlCmd += "a.business_id,";
        sqlCmd += "a.business_code,";
        sqlCmd += "a.company_name,";
        sqlCmd += "a.office_tel_no,";
        sqlCmd += "a.job_position,";
        sqlCmd += "a.service_year,";
        sqlCmd += "a.salary,";
        sqlCmd += "a.education,";
        sqlCmd += "a.update_date,";
        sqlCmd += "a.crt_user,";
        sqlCmd += "b.annual_date,";
        sqlCmd += "to_char(a.mod_time,'yyyymmdd') h_jcid_mod_time,";
        sqlCmd += "decode(a.sex,'1','M','F') h_jcid_sex,";
        sqlCmd += "a.cntry_code,";
        sqlCmd += "a.passport_no,";
        sqlCmd += "a.passport_date,";
        sqlCmd += "a.indigenous_name,";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += "from crd_idno b ,crd_jcic_idno a ";
        sqlCmd += "where a.to_jcic_date ='' ";
        sqlCmd += "and a.apr_user != '' ";
        sqlCmd += "and a.apr_date != '' ";
        sqlCmd += "and a.id_p_seqno  = b.id_p_seqno ";
        sqlCmd += "order by a.crt_date,a.id_p_seqno,a.trans_type ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hJcidId = getValue("id_no", i);
            hJcidIdCode = getValue("id_no_code", i);
            hJcidCrtDate = getValue("crt_date", i);
            hJcidTransType = getValue("trans_type", i);
            hJcidAccountStyle = getValue("account_style", i);
            hJcidIdPSeqno = getValue("id_p_seqno", i);
            hJcidChiName = getValue("chi_name", i);
            hJcidEngName = getValue("eng_name", i);
            hJcidBirthday = getValue("birthday", i);
            hJcidMailZip = getValue("mail_zip", i);
            hJcidMailAddr = getValue("mail_addr", i);
            hJcidResidentAddr = getValue("resident_addr", i);
            hJcidResidentFlag = getValue("resident_flag", i);
            hJcidTelNo = getValue("tel_no", i);
            hJcidCellarPhone = getValue("cellar_phone", i);
            hJcidBusinessId = getValue("business_id", i);
            hJcidBusinessCode = getValue("business_code", i);
            hJcidCompanyName = getValue("company_name", i);
            hJcidOfficeTelNo = getValue("office_tel_no", i);
            hJcidJobPosition = getValue("job_position", i);
            hJcidServiceYear = getValueInt("service_year", i);
            hJcidSalary = getValueDouble("salary", i);
            hJcidEducation = getValue("education", i);
            hJcidUpdateDate = getValue("update_date", i);
            hJcidCrtUser = getValue("crt_user", i);
            hIdnoAnnualDate = getValue("b.annual_date", i);
            hJcidModTime = getValue("h_jcid_mod_time", i);
            hJcidSex = getValue("h_jcid_sex", i);
            hJcidCntryCode = getValue("cntry_code", i);
            hJcidPassportNo = getValue("passport_no", i);
            hJcidPassportDate = getValue("passport_date", i);
            hJcidIndigenousName = getValue("indigenous_name", i);
            hJcidRowid = getValue("rowid", i);            

            getJcicBusinessCode();
            totalAll++;
            getCrdChgId();

            if (hChgiPostJcicFlag.equals("N")) {
                insertCrdNopassJcic();
                updateJcicIdno();
                continue;
            }
            createJcicFile();
            updateJcicIdno();
            total++;
        }
        if (total > 0)
            tailFile();
    }

    /**
     * @throws UnsupportedEncodingException
     *************************************************************************/
    void tailFile() throws UnsupportedEncodingException {

        htail.fileValue = CommJcic.TAIL_LAST_MARK;
        String temp = String.format("%08d", total);
        htail.recordCnt = temp;
        buf = htail.allText();

        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

    }

    /***********************************************************************/
    void insertCrdNopassJcic() throws Exception {
        setValue("old_id", hJcidId);
        setValue("old_id_code", hJcidIdCode);
        setValue("chi_name", hChgiChiName);
        setValue("id_p_seqno", hChgiIdPSeqno);
        setValue("post_kind", "kk1");
        setValue("post_jcic_date", sysDate);
        setValue("card_no", "");
        setValue("OPPOST_REASON", "");
        setValue("OPPOST_DATE", "");
        setValue("MOD_USER", prgmId);
        setValue("MOD_TIME", sysDate);
        setValue("MOD_PGM", prgmId);
        daoTable = "crd_nopass_jcic";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_nopass_jcic duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void getJcicBusinessCode() throws Exception {
        hJcicBusCode = "";
        sqlCmd = "select substr(rtrim(msg_value),1,4) h_jcic_bus_code ";
        sqlCmd += " from crd_message  ";
        sqlCmd += "where msg_type = 'BUS_CODE'  ";
        sqlCmd += "and msg_value = ? ";
        setString(1, hJcidBusinessCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hJcicBusCode = getValue("h_jcic_bus_code");
        }
    }

    /***********************************************************************/
    void updateJcicIdno() throws Exception {

        daoTable = "crd_jcic_idno";
        updateSQL = "to_jcic_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " jcic_filename = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm  = ?";
        whereStr = "where rowid    = ? ";
        setString(1, hFileName);
        setString(2, comc.commGetUserID());
        setString(3, prgmId);
        setRowId(4, hJcidRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_jcic_idno not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void getCrdChgId() throws Exception {
        hChgiCreateUser = "";
        hChgiApprovUser = "";
        hChgiChiName = "";
        hChgiIdPSeqno = "";
        hChgiPostJcicFlag = "";
        sqlCmd = "select chi_name,";
        sqlCmd += "crt_user,";
        sqlCmd += "apr_user,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "decode(post_jcic_flag,'','N',post_jcic_flag) h_chgi_post_jcic_flag ";
        sqlCmd += " from crd_chg_id  ";
        sqlCmd += "where old_id_p_seqno  = ?  ";
        setString(1, hJcidIdPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgiChiName = getValue("chi_name");
            hChgiCreateUser = getValue("crt_user");
            hChgiApprovUser = getValue("apr_user");
            hChgiIdPSeqno = getValue("id_p_seqno");
            hChgiPostJcicFlag = getValue("h_chgi_post_jcic_flag");
        }
    }

    /***********************************************************************/
    void insertFileCtl() throws Exception {

        setValue("file_name", hFileName);
        setValue("crt_date", sysDate);
        setValueInt("head_cnt", 1);
        setValueInt("record_cnt", total);
        setValue("check_code", "1");
        setValue("send_nccc_date", sysDate);
        daoTable = "crd_file_ctl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_file_ctl duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void createJcicFile() throws Exception {
        String engName = "";
        String tmp = "";
        String tempX07 = "";
        String tempX05 = "";
        String tempX100 = "";
        String fullChiName = hJcidChiName + hJcidIndigenousName;

        data = null;
        data = new Buf1();

        data.transType = "6";
        data.transCode = hJcidTransType;
        data.bankNo = CommJcic.JCIC_BANK_NO;
        data.accountType = hJcidAccountStyle;
        if(data.accountType.equals("C")) {
        	if(selectCrdCorp()) {
        		data.id = hObuId;
        	}else {
        		data.id = hJcidBusinessId;
        	}
        }else {
            data.id = hJcidId;
        }
        engName = hJcidEngName;
        data.engName = engName;
        data.chiName = fullChiName;
        if (hJcidBirthday.length() >= 8) {
            tmp = String.format("%07d", comcr.str2long(hJcidBirthday) - 19110000);
            data.birthday = tmp;
        }
        data.mailZip = hJcidMailZip;
        data.mailAddr = hJcidMailAddr;
        /**************************************************
         * 當無戶籍地址時,寫通訊住址 2001/11/15
         **************************************************/
        if (hJcidResidentAddr.length() <= 0) {
            data.residentAddress = hJcidMailAddr;
        } else {
            data.residentAddress = hJcidResidentAddr;
        }
        data.residentFlag = "N";
        data.homeTelNo = hJcidTelNo;
        data.cellarPhone = hJcidCellarPhone;
        data.corpNo = hJcidBusinessId;
        if (hJcidCompanyName.length() == 0) {
            hJcidCompanyName = "未填寫";
        }
        if (hJcidJobPosition.length() == 0) {
            hJcidJobPosition = "未填寫";
        }
        data.corpName = hJcidCompanyName;
        data.officeTelNo = hJcidOfficeTelNo;
        tmp = String.format("%-10.10s", hJcidJobPosition);
        data.position = tmp;
        tmp = String.format("%02d", hJcidServiceYear);
        data.serviceYear = tmp;
        tmp = String.format("%06.0f", hJcidSalary / 1000);
        if ((!hJcicBusCode.equals("1410") || !hJcicBusCode.equals("15AO") || !hJcicBusCode.equals("1700"))
                && hJcidSalary == 0)
            tmp = String.format("%06d", 100);
        data.annualIncome = tmp;

        if (hIdnoAnnualDate.length() == 0) {
            hCardIssueDate = "";
            sqlCmd = "select issue_date ";
            sqlCmd += " from crd_card  ";
            sqlCmd += "where id_p_seqno = ?  ";
            sqlCmd += "and issue_date in (select max(issue_date) from crd_card where id_p_seqno = ? )  ";
            sqlCmd += "fetch first 1 rows only ";
            setString(1, hJcidIdPSeqno);
            setString(2, hJcidIdPSeqno);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hCardIssueDate = getValue("issue_date");
            }
            hIdnoAnnualDate = hCardIssueDate;
        }

        tmp = String.format("%-4s", hJcicBusCode);
        data.businessCode = tmp;
        data.education = hJcidEducation;
        tempX07 = String.format("%07d", comcr.str2long(hIdnoAnnualDate) - 19110000);
        tempX05 = String.format("%5.5s", tempX07);
        data.annualYymm = tempX05;
        if (hJcidUpdateDate.length() >= 8) {
            tmp = String.format("%07d", comcr.str2long(hJcidUpdateDate) - 19110000);
            data.updateDate = tmp;
        }


        if (foreignFlag.equals("Y")) {
            data.sex = hJcidSex;

            tempX100 = String.format("%-2.2s", hJcidCntryCode);
            data.cntryCode = tempX100;

            tempX100 = String.format("%-20.20s", hJcidPassportNo);
            data.passportNo = tempX100;

            tempX100 = String.format("%-8.8s", hJcidPassportDate);
            data.passportDate = tempX100;
        }
        data.overChiName = "";
       if(fullChiName.length()>10) {
        	data.overChiName = fullChiName;
        }
        
        buf = data.allText();
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        return;
    }

    /***********************************************************************/
    void chkForeign() {

        foreignFlag = "Y";
        char[] asc = hJcidId.toCharArray();

        if (asc[0] >= 65 && asc[0] <= 90
                && (hJcidId.substring(1, 2).equals("1") || hJcidId.substring(1, 2).equals("2"))) {
            foreignFlag = "N";
        }

    }

    /**
     * @throws Exception
     *********************************************************************/
    void ftpProc(String refIpCode) throws Exception {
        String tojcicmsg = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        String hEflgRefIpCode  = refIpCode;
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId   = hEflgRefIpCode;  /* 區分不同類的 FTP 檔案-大類     (必要) */
        commFTP.hEflgGroupId    = "KK1";               /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "TOJCIC";            /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir   = String.format("%s/media/crd", comc.getECSHOME());
        commFTP.hEflgModPgm     = this.getClass().getName();

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("put %s", hFileName);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            showLogMessage("I","",String.format("[%s] => error_code[%d] error\n", hEflgRefIpCode, errCode));
            showLogMessage("I","",String.format("[%s]檔案傳送JCIC_FTP有誤(error), 請通知相關人員處理\n", procCode));
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"%s執行完成 傳送JCIC失敗\"", prgmId);
//          comc.systemCmd(tojcicmsg);
            showLogMessage("I","",tojcicmsg);
        } else {
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"%s執行完成 傳送JCIC無誤\"", prgmId);
//          comc.systemCmd(tojcicmsg);
            showLogMessage("I","",tojcicmsg);
        }

    }
    /***********************************************************************/
    boolean selectCrdCorp() throws Exception {
        hObuId = "";
        sqlCmd = "select obu_id ";
        sqlCmd += " from crd_corp  ";
        sqlCmd += "where corp_no = ? ";
        setString(1, hJcidBusinessId);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hObuId = getValue("obu_id");
        }
        if(hObuId.length()>0) {
        	return true;
        }else {
        	return false;
        }
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdF043 proc = new CrdF043();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    class Bufh {
        String fileId;
        String bankNo;
        String filler1;
        String sendDate;
        String fileExt;
        String filler2;
        String contactTel;
        String contactMsg;
        String filler3;
        String len;

//        String allText() throws UnsupportedEncodingException {
//            String rtn = "";
//            rtn += fixLeft(fileId    ,  18);
//            rtn += fixLeft(bankNo    ,   3);
//            rtn += fixLeft(filler1    ,   5);
//            rtn += fixLeft(sendDate  ,   7);
//            rtn += fixLeft(fileExt   ,   2);
//            rtn += fixLeft(filler2    ,  10);
//            rtn += fixLeft(contactTel,  16);
//            rtn += fixLeft(contactMsg,  80);
//            rtn += fixLeft(filler3    , 249);
//            rtn += fixLeft(len, 1);
//            return rtn;
//        }
 
    }

    class Buft {
        String fileValue;
        String recordCnt;
        String filler1;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(fileValue, 4);
            rtn += fixLeft(recordCnt, 8);
//            rtn += fixLeft(filler1, 378);
//            rtn += fixLeft(len, 1);
            return rtn;
        }

        
    }

    class Buf1 {
        String transType;
        String transCode;
        String bankNo;
        String filler1;
        String accountType;
        String id;
        String chiName;
        String engName;
        String birthday;
        String filler2;
        String mailZip;
        String mailAddr;
        String residentAddress;
        String residentFlag;
        String homeTelNo;
        String cellarPhone;
        String filler3;
        String corpNo;
        String corpName;
        String officeTelNo;
        String position;
        String serviceYear;
        String annualIncome;
        String businessCode;
        String education;
        String annualYymm;
        String filler4;
        String updateDate;
        String sex;
        String cntryCode;
        String passportNo;
        String passportDate;
        String len1;
        String overChiName;
        String len2;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(transType   ,  1);
            rtn += fixLeft(transCode   ,  1);
            rtn += fixLeft(bankNo      ,  3);
            rtn += fixLeft(filler1      ,  4);
            rtn += fixLeft(accountType ,  1);
            rtn += fixLeft(id           , 10);
            rtn += fixLeft(chiName     , 20);
            rtn += fixLeft(engName     , 20);
            rtn += fixLeft(birthday     ,  7);
            rtn += fixLeft(filler2      , 12);
            rtn += fixLeft(mailZip     ,  5);
            rtn += fixLeft(mailAddr    , 66);
            rtn += fixLeft(residentAddress, 66);
            rtn += fixLeft(residentFlag,  1);
            rtn += fixLeft(homeTelNo  , 16);
            rtn += fixLeft(cellarPhone , 16);
            rtn += fixLeft(filler3      , 12);
            rtn += fixLeft(corpNo      , 10);
            rtn += fixLeft(corpName    , 30);
            rtn += fixLeft(officeTelNo, 16);
            rtn += fixLeft(position     , 10);
            rtn += fixLeft(serviceYear ,  2);
            rtn += fixLeft(annualIncome,  6);
            rtn += fixLeft(businessCode,  4);
            rtn += fixLeft(education    ,  1);
            rtn += fixLeft(annualYymm  ,  5);
            rtn += fixLeft(filler4      ,  7);
            rtn += fixLeft(updateDate  ,  7);
            rtn += fixLeft(sex          ,  1);
            rtn += fixLeft(cntryCode   ,  2);
            rtn += fixLeft(passportNo  , 20);
            rtn += fixLeft(passportDate,  8);
            rtn += fixLeft(len1         , 10);
            rtn += fixLeft(overChiName,200);
            rtn += fixLeft(len2         , 20);
            
            return rtn;
        }

       
    }
	String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++)    spc += " ";
        if (str == null)                  str  = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }
	/****************************************************************************/
	void renameFile1(String removeFileName) throws Exception {
  		String tmpstr1 = comc.getECSHOME() + "/media/crd/" + removeFileName;
  		String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + removeFileName;
  		
  		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
  			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
  			return;
  		}
  		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
  	}

}

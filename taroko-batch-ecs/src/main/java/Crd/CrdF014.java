/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/03/25  V1.00.01    Pino      for TCB                                   *
*  109/12/22  V1.00.02    shiyuqi       updated for project coding standard   *
*  110/04/01  V1.00.03    Justin    use common value                          *
*  110/08/17  V1.00.04    Wilson    中文姓名超逾10個字之全名需求                                                             *
*  112/07/05  V1.00.05    Wilson    調整FTP參數                                                                                            *
*  112/08/24  V1.00.06    Wilson    修正職業代碼問題                                                                                      *
*  ******************************************************************************/

package Crd;

import java.io.UnsupportedEncodingException;
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

/*每週產生送JCIC信用卡戶異動資料*/
public class CrdF014 extends AccessDAO {
	private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_KK1;
	private String progname = "每日產生送JCIC信用卡戶異動資料 112/08/24  V1.00.06 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    int debug = 1;
    String                    prgmId             = "CrdF014";
    String                    rptName1           = "";
    List<Map<String, Object>> lpar1              = new ArrayList<Map<String, Object>>();
    int                       rptSeq1            = 0;
    String                    buf                = "";
    String                    stderr             = "";
    String hCallBatchSeqno = "";

    String hDay = "";
    String pNewFilename = "";
    String pFilename = "";
    String hCreateDate = "";
    String hSuccApsBatchno = "";
    String hSuccIdPSeqno = "";
    String hSuccApplyId = "";
    String hSuccApplyIdCode = "";
    String hSuccCardNo = "";
    String hSuccModTime = "";
    String hSuccRowid = "";
    String hChgiChiName = "";
    String hChgiCreateUser = "";
    String hChgiApprovUser = "";
    String hChgiIdPSeqno = "";
    // String h_chgi_id_code = "";
    String hChgiPostJcicFlag = "";
    String hCardNo = "";
    String hEngName = "";
    String hPSeqno = "";
    String hIdPSeqno = "";
    String hCorpNo = "";
    String hCorpNoCode = "";
    String hObuId = "";
    String hSupFlag = "";
    String hCardIssueDate = "";
    String hCardCurrentCode = "";
    String hCardOppostReason = "";
    String hCardOppostDate = "";
    String hChiName = "";
    String hCompanyName = "";
    String hOfficeTel = "";
    String hJobPosition = "";
    int hServiceYear = 0;
    long hAnnualIncome = 0;
    String hCellarPhone = "";
    String hHomeTel = "";
    String hTmpZip = "";
    String hTmpAddr1 = "";
    String hTmpAddr2 = "";
    String hTmpAddr3 = "";
    String hTmpAddr4 = "";
    String hTmpAddr5 = "";
    String hNation = "";
    String hEducation = "";
    String hBusinessCode = "";
    String hIdnoAnnualDate = "";
    String hIdnoPassportNo = "";
    String hIdnoPassportDate = "";
    String hIdnoOtherCntryCode = "";
    String hIdnoSex = "";
    String hBirthday = "";
    String hAcctType = "";
    String hAcnoFlag = "";
    String hAcctKey = "";
    String hCardIndicator = "";
    String hMailZip = "";
    String hMailAddr = "";
    String hJcicBusCode = "";
    String hFileName = "";
    String hResidentAddr = "";
    int    total                   = 0;
    int fileSeqno = 0;
    int totalAll = 0;
    int errCode = 0;
    String tempId = "";
    String tempIdCode = "";
    String hContactTel = "";
    String hContactMsg = "";
    String tempX07 = "";
    String tempX05 = "";
    String foreignFlag = "";
    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hEflgTransSeqno = "";
    String hEflgModPgm = "";
    String hEriaLocalDir = "";
    String tmpstr1                 = "";
    String tmpstr2                 = "";
    String msgCode = "";
    String msgDesc = "";
    String hIndigenousName ="";

    Bufh           hdata      = new Bufh();
    Buft           htail      = new Buft();
    Buf1           data       = new Buf1();
    private String hModUser = "";

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
                comc.errExit("Usage : CrdF014", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hModUser = comc.commGetUserID();
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

            showLogMessage("I", "", String.format("程式執行結束 , 總筆數:[%d],寫檔=[%d]\n", totalAll, total));

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
        String filename = "";
        String tmp = "";
        String hDy = "";
        String hCreateDate = "";
        String pFilename = "";
        String pNewFilename = "";

        hCreateDate = "";
        hFileName = "";
        pFilename = "";
        pNewFilename = "";
        hDy = "";
        sqlCmd = "select to_char(sysdate,'mmdd') h_day ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDy = getValue("h_day");
        }

        filename = String.format("%s%4s", CommJcic.JCIC_BANK_NO, hDy);
        pFilename = String.format("%s%%", filename);
        hCreateDate = sysDate;
        sqlCmd = "select max(file_name) p_new_filename ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name  like ? and crt_date = ?  ";
        sqlCmd += "  and check_code = '1' ";
        setString(1, pFilename);
        setString(2, hCreateDate);
        recordCnt = selectTable();
        fileSeqno = 0;
        if (recordCnt > 0) {
            pNewFilename = getValue("p_new_filename");

            if ((pNewFilename.length() > 0)) {
                stderr = String.format("OLD_FILE_NAME[%s] ", pNewFilename);
                showLogMessage("I", "", stderr);
                tmp = pNewFilename.substring(7, 8);
                fileSeqno = comcr.str2int(tmp) + 1;
                stderr = String.format("SEQNO=[%d]", fileSeqno);
                showLogMessage("I", "", stderr);
            } else
                fileSeqno = 1;
        } else {
            fileSeqno = 1;
        }

        filename = String.format("%s%1d.kk1", filename, fileSeqno);
        hFileName = filename;
        rptName1 = filename;

        headerFile();
    }

    /**********************************************************************/
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
        
//      hContactTel = commJcic.getContactTel();
//      hContactMsg = commJcic.getContactMsg();
//      selectContactData();
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
        sqlCmd += "  and wf_key  = 'CONTACT' ";
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
        sqlCmd  = "select '1' as tmp_type, '' as id_p_seqno, ";
        sqlCmd += "a.apply_id,";
        sqlCmd += "a.apply_id_code,";
        sqlCmd += "a.card_no,";
        sqlCmd += "to_char(a.mod_time,'yyyymmdd') h_succ_mod_time,";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += " from crd_apscdsuc a ";
        sqlCmd += "where a.to_jcic_date ='' ";
        sqlCmd += "UNION ";
        sqlCmd += "select '2' as tmp_type, a.id_p_seqno, ";

        sqlCmd += "nvl(uf_idno_id(a.id_p_seqno), '') as apply_id,";
        sqlCmd += "nvl((select id_no_code from crd_idno where crd_idno.id_p_seqno=a.id_p_seqno fetch first 1 rows only), '') as apply_id_code,";

        sqlCmd += "'' as card_no, ";
        sqlCmd += "substr(to_char(a.mod_time,'yyyymmdd'),1,8), ";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += " from crd_idno_kk1 a ";
        sqlCmd += "where a.post_flag    = 'N' ";
        sqlCmd += "  and a.mod_audcode <> 'A' ";
        sqlCmd += "UNION ";
        sqlCmd += "select '3' as tmp_type, a.id_p_seqno, ";
        sqlCmd += "nvl(uf_idno_id(a.id_p_seqno), '') as apply_id,";
        sqlCmd += "nvl((select id_no_code from crd_idno where crd_idno.id_p_seqno=a.id_p_seqno fetch first 1 rows only), '') as apply_id_code,";

        sqlCmd += "a.card_no, ";
        sqlCmd += "substr(to_char(a.mod_time,'yyyymmdd'),1,8), ";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += " from crd_card_kk1 a ";
        sqlCmd += "where a.post_flag    = 'N' ";
        sqlCmd += "  and a.mod_audcode <> 'A' ";
        sqlCmd += "order by 2,3 ";
        int cursorIndex = openCursor();

        while (fetchTable(cursorIndex)) {
            hSuccApsBatchno = getValue("tmp_type");
            hSuccIdPSeqno = getValue("id_p_seqno");
            hSuccApplyId = getValue("apply_id");
            hSuccApplyIdCode = getValue("apply_id_code");
            hSuccCardNo = getValue("card_no");
            hSuccModTime = getValue("h_succ_mod_time");
            hSuccRowid = getValue("rowid");

            totalAll++;
            
			if (debug == 1) {
				showLogMessage("I", "",
						" 888 read=[" + hSuccCardNo + "][" + hSuccIdPSeqno + "]" + hSuccApsBatchno + "," + totalAll);
				showLogMessage("I", "", "       id=[" + hSuccApplyId + "]");
			}

            if (totalAll == 1) {
                tempId = hSuccApplyId;
                tempIdCode = hSuccApplyIdCode;
            } else {
                if (tempId.equals(hSuccApplyId) && tempIdCode.equals(hSuccApplyIdCode)) {
                    updateRtn();
                    continue;
                } else {
                    tempId = hSuccApplyId;
                    tempIdCode = hSuccApplyIdCode;
                }
            }
            if (hSuccCardNo.length() == 0) {
                getCrdCardKk1();
                hSuccCardNo = hCardNo;
                if (hSuccCardNo.length() == 0) {
                    int rtn = getCrdCard();
                    if(rtn != 0)    continue;
                    hSuccCardNo = hCardNo;
                }
            }

            getOtherData();
            getCrdChgId();

            if (hChgiPostJcicFlag.equals("N")) {
                insertCrdNopassJcic();
                updateRtn();
                continue;
            }

            createJcicFile();
            updateRtn();
            total++;
        }
        closeCursor(cursorIndex);
        if (total > 0)
            tailFile();
    }

    /**
     * @throws UnsupportedEncodingException
     *************************************************************************/
    void tailFile() throws UnsupportedEncodingException {
        String temp = "";

        htail.len = "";
        htail.fileValue = CommJcic.TAIL_LAST_MARK;
        temp = String.format("%08d", total);
        htail.recordCnt = temp;
        buf = htail.allText();

        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

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
    void insertCrdNopassJcic() throws Exception {
        setValue("old_id", hSuccApplyId);
        setValue("old_id_code", hSuccApplyIdCode);
        setValue("chi_name", hChgiChiName);
        setValue("id_p_seqno", hChgiIdPSeqno);
        setValue("post_kind", "kk1");
        setValue("post_jcic_date", sysDate);
        // setValue("card_no", "");
        // setValue("OPPOST_REASON", "");
        // setValue("OPPOST_DATE", "");
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
    void updateRtn() throws Exception {
        switch (comcr.str2int(hSuccApsBatchno)) {
        case 1:
            updateCrdApscdsuc();
            break;
        case 2:
            updateCrdidnoKk1();
            break;
        case 3:
            updateCrdCardKk1();
            break;
        }
    }

    /***********************************************************************/
    void updateCrdApscdsuc() throws Exception {
        daoTable   = "crd_apscdsuc";
        updateSQL  = " to_jcic_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_user     = ?,";
        updateSQL += " mod_pgm      = ?";
        whereStr   = "where rowid   = ? ";
        setString(1, hModUser);
        setString(2, prgmId);
        setRowId(3, hSuccRowid);
        updateTable();
    }

    /***********************************************************************/
    void updateCrdidnoKk1() throws Exception {

        daoTable   = "crd_idno_kk1";
        updateSQL  = " post_date  = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " post_flag  = 'Y',";
        updateSQL += " mod_time   = sysdate,";
        updateSQL += " mod_user   = ?,";
        updateSQL += " mod_pgm    = ?";
        whereStr   = "where rowid = ? ";
        setString(1, hModUser);
        setString(2, prgmId);
        setRowId(3, hSuccRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_idno_kk1 not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateCrdCardKk1() throws Exception {
        daoTable   = "crd_card_kk1";
        updateSQL  = " post_date  = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " post_flag  = 'Y',";
        updateSQL += " mod_time   = sysdate,";
        updateSQL += " mod_user   = ?,";
        updateSQL += " mod_pgm    = ?";
        whereStr   = "where rowid = ? ";
        setString(1, hModUser);
        setString(2, prgmId);
        setRowId(3, hSuccRowid);
        updateTable();
    }

    /***********************************************************************/
    void getCrdCardKk1() throws Exception {
        hCardNo = "";
        sqlCmd  = "select card_no ";
        sqlCmd += " from crd_card_kk1  ";
        sqlCmd += "where id_p_seqno  = ?  ";
        sqlCmd += "  and post_flag   = 'N'  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, comcr.ufIdnoPseqno(hSuccApplyId));
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCardNo = getValue("card_no");
        }

    }

    /***********************************************************************/
    int  getCrdCard() throws Exception {
        hCardNo = "";
        String idPSeqno = comcr.ufIdnoPseqno(hSuccApplyId);

if(debug == 1)
   showLogMessage("I", "", "   888 get_crd_card="+ hSuccApplyId +"["+idPSeqno+"]");

        sqlCmd = "select card_no ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where id_p_seqno  = ?  ";
        sqlCmd += "  and issue_date in ( select max(issue_date) from crd_card where id_p_seqno  = ? ) ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, idPSeqno);
        setString(2, idPSeqno);
        int recordCnt = selectTable();
        if(notFound.equals("Y")) {
           sqlCmd = "select card_no ";
           sqlCmd += " from ecs_crd_card  ";
           sqlCmd += "where id_p_seqno  = ?  ";
           sqlCmd += "  and issue_date in (select max(issue_date) from ecs_crd_card where id_p_seqno  = ? ) ";
           sqlCmd += "fetch first 1 rows only ";
           setString(1, idPSeqno);
           setString(2, idPSeqno);
           recordCnt = selectTable();
           if(notFound.equals("Y")) {
//            comcr.err_rtn("select_crd_card 0 not found!", id_p_seqno, comcr.h_call_batch_seqno);
              return(1);
             }
        
        }
        if (recordCnt > 0) {
            hCardNo = getValue("card_no");
        }

        return(0);
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
        sqlCmd += "where old_id_no      = ?  ";
        sqlCmd += "  and old_id_no_code = ? ";
        setString(1, hSuccApplyId);
        setString(2, hSuccApplyIdCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgiChiName = getValue("chi_name");
            hChgiCreateUser = getValue("crt_user");
            hChgiApprovUser = getValue("apr_user");
            hChgiIdPSeqno = getValue("id_p_seqno");
            hChgiPostJcicFlag = getValue("h_chgi_post_jcic_flag");
        }
    }

    /**********************************************************************/
    void getOtherData() throws Exception {

        hEngName = "";
        hPSeqno = "";
        hIdPSeqno = "";
        hCorpNo = "";
        hCorpNoCode = "";
        hSupFlag = "";
        hCardIssueDate = "";
        hCardCurrentCode = "";
        hCardOppostReason = "";
        hCardOppostDate = "";
        sqlCmd  = "select eng_name,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_no_code,";
        sqlCmd += "sup_flag,";
        sqlCmd += "issue_date,";
        sqlCmd += "current_code,";
        sqlCmd += "oppost_reason,";
        sqlCmd += "oppost_date ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hSuccCardNo);
        int recordCnt = selectTable();
        if(notFound.equals("Y")) {
           sqlCmd  = "select eng_name,";
           sqlCmd += "acno_p_seqno ,";
           sqlCmd += "id_p_seqno,";
           sqlCmd += "corp_no,";
           sqlCmd += "corp_no_code,";
           sqlCmd += "sup_flag,";
           sqlCmd += "issue_date,";
           sqlCmd += "current_code,";
           sqlCmd += "oppost_reason,";
           sqlCmd += "oppost_date ";
           sqlCmd += " from ecs_crd_card  ";
           sqlCmd += "where card_no = ? ";
           setString(1, hSuccCardNo);
           recordCnt = selectTable();
           if (notFound.equals("Y")) {
               comcr.errRtn("select_crd_card 1 not found!", hSuccCardNo, comcr.hCallBatchSeqno);
           }
        }
        if (recordCnt > 0) {
            hEngName = getValue("eng_name");
            hPSeqno = getValue("acno_p_seqno");
            hIdPSeqno = getValue("id_p_seqno");
            hCorpNo = getValue("corp_no");
            hCorpNoCode = getValue("corp_no_code");
            hSupFlag = getValue("sup_flag");
            hCardIssueDate = getValue("issue_date");
            hCardCurrentCode = getValue("current_code");
            hCardOppostReason = getValue("oppost_reason");
            hCardOppostDate = getValue("oppost_date");
        }

        getCrdIdno();

        return;
    }

    /***********************************************************************/
    void getCrdIdno() throws Exception {

        hTmpZip = "";
        hTmpAddr1 = "";
        hTmpAddr2 = "";
        hTmpAddr3 = "";
        hTmpAddr4 = "";
        hTmpAddr5 = "";
        hChiName = "";
        hBirthday = "";
        hNation = "";
        hHomeTel = "";
        hCompanyName = "";
        hOfficeTel = "";
        hJobPosition = "";
        hServiceYear = 0;
        hAnnualIncome = 0;
        hCellarPhone = "";
        hResidentAddr = "";
        hEducation = "";
        hBusinessCode = "";
        hIdnoAnnualDate = "";
        hIdnoPassportDate = "";
        hIdnoPassportNo = "";
        hIdnoOtherCntryCode = "";
        hIdnoSex = "";
        hIndigenousName = "";
        
        sqlCmd = "select chi_name,";
        sqlCmd += "company_name,";
        sqlCmd += "(office_area_code1||office_tel_no1) as tel_office,";
        sqlCmd += "job_position,";
        sqlCmd += "service_year,";
        sqlCmd += "annual_income,";
        sqlCmd += "cellar_phone,";
        sqlCmd += "(home_area_code1||home_tel_no1) as tel_home,";
        sqlCmd += "resident_zip,";
        sqlCmd += "resident_addr1,";
        sqlCmd += "resident_addr2,";
        sqlCmd += "resident_addr3,";
        sqlCmd += "resident_addr4,";
        sqlCmd += "resident_addr5,";
        sqlCmd += "nation,";
        sqlCmd += "education,";
        sqlCmd += "business_code,";
        sqlCmd += "annual_date,";
        sqlCmd += "passport_no,";
        sqlCmd += "passport_date,";
        sqlCmd += "other_cntry_code,";
        sqlCmd += "sex,";
        sqlCmd += "birthday, ";
        sqlCmd += "indigenous_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_no  = ?  ";
        sqlCmd += "  and id_no_code = ? ";
        setString(1, hSuccApplyId);
        setString(2, hSuccApplyIdCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno 1 not found!", hSuccApplyId, comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hChiName = getValue("chi_name");
            hCompanyName = getValue("company_name");
            hOfficeTel = getValue("tel_office");
            hJobPosition = getValue("job_position");
            hServiceYear = getValueInt("service_year");
            hAnnualIncome = getValueLong("annual_income");
            hCellarPhone = getValue("cellar_phone");
            hHomeTel = getValue("tel_home");
            hTmpZip = getValue("resident_zip");
            hTmpAddr1 = getValue("resident_addr1");
            hTmpAddr2 = getValue("resident_addr2");
            hTmpAddr3 = getValue("resident_addr3");
            hTmpAddr4 = getValue("resident_addr4");
            hTmpAddr5 = getValue("resident_addr5");
            hNation = getValue("nation");
            hEducation = getValue("education");
            hBusinessCode = getValue("business_code");
            hIdnoAnnualDate = getValue("annual_date");
            hIdnoPassportNo = getValue("passport_no");
            hIdnoPassportDate = getValue("passport_date");
            hIdnoOtherCntryCode = getValue("other_cntry_code");
            hIdnoSex = getValue("sex");
            hBirthday = getValue("birthday");
            hIndigenousName = getValue("indigenous_name");
        }

        if (hIdnoAnnualDate.length() == 0) {
            hIdnoAnnualDate = hCardIssueDate;
        }
        if (hCompanyName.length() == 0) {
            hCompanyName = "未填寫";
        }
        if (hJobPosition.length() == 0) {
            hJobPosition = "未填寫";
        }

        hResidentAddr = "";
        hResidentAddr = String.format("%s%s%s%s%s", hTmpAddr1, hTmpAddr2, hTmpAddr3, hTmpAddr4, hTmpAddr5);

        hAcctType = "";
        hAcnoFlag = "";
        hAcctKey = "";
        hCardIndicator = "";
        hMailZip = "";
        hMailAddr = "";
        sqlCmd = "select acct_type,";
        sqlCmd += "acno_flag,"; //V1.00.01
        sqlCmd += "acct_key,";
        sqlCmd += "card_indicator,";
        sqlCmd += "bill_sending_zip,";
        sqlCmd += "rtrim(bill_sending_addr1)|| rtrim(bill_sending_addr2)||rtrim(bill_sending_addr3)|| "
                + "rtrim(bill_sending_addr4)||rtrim(bill_sending_addr5) h_mail_addr ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString(1, hPSeqno);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcctType = getValue("acct_type");
            hAcnoFlag = getValue("acno_flag");
            hAcctKey = getValue("acct_key");
            hCardIndicator = getValue("card_indicator");
            hMailZip = getValue("bill_sending_zip");
            hMailAddr = getValue("h_mail_addr");
        }

        getJcicBusinessCode();

        return;
    }

    /***********************************************************************/
    /***
     * 抓取JCIC之business_code 代碼
     * 
     * @throws Exception
     */
    void getJcicBusinessCode() throws Exception {
        hJcicBusCode = "";
        sqlCmd = "select substr(rtrim(msg_value),1,4) h_jcic_bus_code ";
        sqlCmd += " from crd_message  ";
        sqlCmd += "where msg_type = 'BUS_CODE'  ";
        sqlCmd += "and msg_value = ? ";
        setString(1, hBusinessCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hJcicBusCode = getValue("h_jcic_bus_code");
        }
    }
    /***********************************************************************/
    boolean selectCrdCorp() throws Exception {
        hObuId = "";
        sqlCmd = "select obu_id ";
        sqlCmd += " from crd_corp  ";
        sqlCmd += "where corp_no = ? ";
        setString(1, hCorpNo);
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
    void createJcicFile() throws Exception {
        String tmp = "";
        String tempX100 = "";
        long annualIncome = 0;
        String fullChiName = hChiName + hIndigenousName;

        data.transType = "6";
        switch (comcr.str2int(hSuccApsBatchno)) {
        case 1:
            data.transCode = "A";
            break;
        case 2:
            data.transCode = "C";
            break;
        case 3:
            data.transCode = "C";
            break;
        }
        data.bankNo = CommJcic.JCIC_BANK_NO;
        data.accountType = "M";
        if (hSupFlag.equals("1"))
            data.accountType = "S";
        /** 法人戶 **/
        if (hCardIndicator.equals("2"))
            data.accountType = "P";
        if ((hCardIndicator.equals("2")) && (hAcnoFlag.equals("2"))) //V1.00.01
            data.accountType = "C";
        if(data.accountType.equals("C")) {
        	if(selectCrdCorp()) {
        		data.applyId = hObuId;
        	}else {
        		data.applyId = hCorpNo;
        	}
        }else {
        	data.applyId = hSuccApplyId;
        }
        annualIncome = hAnnualIncome / 1000;
        data.engName = hEngName;
        data.chiName = fullChiName;
        if (hBirthday.length() >= 8) {
            tmp = String.format("%07d", comcr.str2long(hBirthday) - 19110000);
            data.birthday = tmp;
        }
        data.mailZip = hMailZip;
        data.mailAddr = hMailAddr;
        /**************************************************
         * 當無戶籍地址時,寫通訊住址 2001/11/15
         **************************************************/
        if (hResidentAddr.length() <= 0) {
            data.residentAddress = hMailAddr;
        } else {
            data.residentAddress = hResidentAddr;
        }
        data.residentFlag = "N";
        data.homeTelNo = hHomeTel;
        data.cellarPhone = hCellarPhone;
        data.corpno = hCorpNo;
        data.corpName = hCompanyName;
        data.officeTelNo = hOfficeTel;
        data.position = hJobPosition;
        tmp = String.format("%02d", hServiceYear);
        data.serviceYear = tmp;
        tmp = String.format("%06d", annualIncome);
        if ((!hJcicBusCode.equals("1410") || !hJcicBusCode.equals("15AO") || !hJcicBusCode.equals("1700"))
                && annualIncome == 0)
            tmp = String.format("%06d", 100);
        data.annualIncome = tmp;
        data.businessCode = hJcicBusCode;
        data.education = hEducation;
        tempX07 = String.format("%07d", comcr.str2long(hIdnoAnnualDate) - 19110000);
        tempX05 = String.format("%5.5s", tempX07);
        data.annualYymm = tempX05;
        if (hSuccModTime.length() >= 8) {
            tmp = String.format("%07d", comcr.str2long(hSuccModTime) - 19110000);
            data.updateDate = tmp;
        }

        chkForeign();

        if (foreignFlag.equals("Y")) {
            if (hIdnoSex.equals("2"))
                data.sex = "F";
            else
                data.sex = "M";

            if (hIdnoSex.length() == 0)
                data.sex = "M";

            if (hIdnoOtherCntryCode.length() == 0)
                tempX100 = String.format("%-2.2s", "AF");
            else
                tempX100 = String.format("%-2.2s", hIdnoOtherCntryCode);
            data.cntryCode = tempX100;

            if (hIdnoPassportNo.length() == 0)
                tempX100 = String.format("%-20.20s", "AA12345678");
            else
                tempX100 = String.format("%-20.20s", hIdnoPassportNo);
            data.passportNo = tempX100;

            if (hIdnoPassportDate.length() == 0)
                tempX100 = String.format("%-8.8s", "20090101");
            else
                tempX100 = String.format("%-8.8s", hIdnoPassportDate);
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
        char[] asc = hSuccApplyId.toCharArray();

        if(hSuccApplyId.length() == 0)
            return;
        if ((asc[0] >= 65 && asc[0] <= 90)
                && (hSuccApplyId.substring(1, 2).equals("1") || hSuccApplyId.substring(1, 2).equals("2"))) {
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
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId   = hEflgRefIpCode; /* 區分不同類的 FTP 檔案-大類     (必要) */
        commFTP.hEflgGroupId    = "KK1";              /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "TOJCIC";           /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir   = String.format("%s/media/crd", comc.getECSHOME());
        commFTP.hEflgModPgm     = this.getClass().getName();

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("put %s", hFileName);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

        if (errCode != 0) {
            showLogMessage("I","",String.format("[%s] error\n", hEflgRefIpCode));
            showLogMessage("I","",String.format("[%s]檔案傳送JCIC_FTP有誤(error),請通知相關人員處理\n",procCode));
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"%s執行完成 傳送JCIC失敗[%s]\""
                                                                    , prgmId, hFileName);
//          comc.systemCmd(tojcicmsg);
            showLogMessage("I","",tojcicmsg);
        } else {
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"%s執行完成 傳送JCIC無誤[%s]\""
                                                                    , prgmId, hFileName);
//          comc.systemCmd(tojcicmsg);
            showLogMessage("I","",tojcicmsg);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdF014 proc = new CrdF014();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
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
//            rtn += fixLeft(fileId,  18);
//            rtn += fixLeft(bankNo,   3);
//            rtn += fixLeft(filler1    ,   5);
//            rtn += fixLeft(sendDate,   7);
//            rtn += fixLeft(fileExt,   2);
//            rtn += fixLeft(filler2    ,  10);
//            rtn += fixLeft(contactTel,  16);
//            rtn += fixLeft(contactMsg,  80);
//            rtn += fixLeft(filler3    , 249);
//            rtn += fixLeft(len        ,   1);
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
//            rtn += fixLeft(filler1,  378);
//            rtn += fixLeft(len,        1);
            return rtn;
        }

    
    }

    class Buf1 {
        String transType;
        String transCode;
        String bankNo;
        String filler1;
        String accountType;
        String applyId;
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
        String corpno;
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
            rtn += fixLeft(transType,        1);
            rtn += fixLeft(transCode,        1);
            rtn += fixLeft(bankNo,           3);
            rtn += fixLeft(filler1,           4);
            rtn += fixLeft(accountType,      1);
            rtn += fixLeft(applyId,         10);
            rtn += fixLeft(chiName,         20);
            rtn += fixLeft(engName,         20);
            rtn += fixLeft(birthday,          7);
            rtn += fixLeft(filler2,          12);
            rtn += fixLeft(mailZip,          5);
            rtn += fixLeft(mailAddr,        66);
            rtn += fixLeft(residentAddress, 66);
            rtn += fixLeft(residentFlag,  1);
            rtn += fixLeft(homeTelNo, 16);
            rtn += fixLeft(cellarPhone, 16);
            rtn += fixLeft(filler3         , 12);
            rtn += fixLeft(corpno, 10);
            rtn += fixLeft(corpName, 30);
            rtn += fixLeft(officeTelNo, 16);
            rtn += fixLeft(position,         10);
            rtn += fixLeft(serviceYear,      2);
            rtn += fixLeft(annualIncome,     6);
            rtn += fixLeft(businessCode,     4);
            rtn += fixLeft(education,         1);
            rtn += fixLeft(annualYymm,       5);
            rtn += fixLeft(filler4,           7);
            rtn += fixLeft(updateDate,       7);
            rtn += fixLeft(sex,               1);
            rtn += fixLeft(cntryCode,        2);
            rtn += fixLeft(passportNo,      20);
            rtn += fixLeft(passportDate,     8);
            rtn += fixLeft(len1,             10);
            rtn += fixLeft(overChiName,   200);
            rtn += fixLeft(len2,             20);
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

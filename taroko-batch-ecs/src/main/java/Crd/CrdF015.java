/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/03/30  V1.00.01    Pino      bank_no改為006                                                                                    *
*  109/12/18  V1.00.02    Wilson    境外公司戶報送JCIC處理                                                                       *                   
*  109/12/22  V1.00.03   shiyuqi       updated for project coding standard   *
*  109/12/30  V1.00.04  yanghan       修改了部分无意义的變量名稱          *
*  110/04/01  V1.00.05    Justin    use common value                          *
*  110/08/24  V1.00.06    Wilson    停卡原因讀取cca_opp_type_reason的jcic_opp_reason*
*  112/07/05  V1.00.07    Wilson    資料別P改成C                                 *
*  112/08/10  V1.00.08    Wilson    團代1203、1204不報送                                                                              *
*  112/08/24  V1.00.09    Wilson    修正停用欄位問題                                                                                      *
*  112/12/06  V1.00.10    Wilson    crd_item_unit不判斷卡種                                                              *
******************************************************************************/

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

/*每週產生送JCIC信用卡異動資料*/
public class CrdF015 extends AccessDAO {
	private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_KK2;
	private String progname = "每日產生送JCIC信用卡異動資料   112/12/06  V1.00.10 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;

    String prgmId = "CrdF015";
    String rptName1 = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq1 = 0;
    int totalAll = 0;
    String buf = "";
    String stderr = "";
    String hModUser = "";
    String hCallBatchSeqno = "";

    String hDay = "";
    String hSysdate = "";
    String hChiDate = "";
    String pNewFilename = "";
    String pFilename = "";
    String hCreateDate = "";
    String hMbosUnitCode = "";
    String hTtttVirtualFlag = "";
    String hCardActivateFlag = "";
    String hCardOldActivateFlag = "";
    String hCardOldCardNo = "";
    String hCardChangeStatus = "";
    String hCardOldEndDate = "";
    String hCardCurrCode = "";
    String hTableFlag = "";
    String hCdjcCardNo = "";
    String hCdjcTransType = "";
    String hCdjcCurrentCode = "";
    String hCdjcOppostReason = "";
    String hCdjcOppostDate = "";
    String hCdjcPaymentDate = "";
    double hCdjcRiskAmt = 0;
    String hCdjcKk4Note = "";
    String hCdjcModTime = "";
    String hCdjcRowid = "";
    String hBillTypeFlag = "";
    String hGpNo = "";
    String hRelaId = "";
    String hPSeqno = "";
    String hGroupCode = "";
    String hCardType = "";
    String hSupFlag = "";
    String hCardIdPSeqno = "";
    String hCardCurrentCode = "";
    String hCardCorpNo = "";
    String hCardCorpNoCode = "";
    String hCardMajorIdPSeqno = "";
    String hCardMajorRelation = "";
    String hCardMajorCardNo = "";
    String hIssueDate = "";
    String hIssueDate3 = "";
    String hCardCorpActFlag = "";
    String hCardSupFlag = "";
    String hCardOppostReason = "";
    String hCardOppostDate = "";
    String hAcnoCardIndicator = "";
    double hAcnoLineOfCreditAmt = 0;
    String hAcnoDebtCloseDate = "";
    String hAcnoFlag = "";
    String hAcnoSaleDate = "";
    double hAssetValue = 0;
    String hCardSince = "";
    String hCardName = "";
    String pCardNote = "";
    String hJcicCode = "";
    String tempKind = "";
    String tempX02Kk4 = "";
    String hFileName = "";
    int hRecCount = 0;
    String hContactTel = "";
    String hContactMsg = "";
    String hChgiChiName = "";
    String hChgiCreateUser = "";
    String hChgiApprovUser = "";
    String hChgiIdPSeqno = "";
    String hChgiPostJcicFlag = "";
    String hChgiKk2Flag = "";
    String hCdjcErrorCode = "";
    String tempCardNo = "";
    String hCardNote = "";
    String hAssureFlag = "";
    String tempX04 = "";
    String tempX10 = "";
    String foreignFlag = "";
    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hEflgTransSeqno = "";
    String hEflgModPgm = "";
    String hEriaLocalDir = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String msgCode = "";
    String msgDesc = "";
    String hObuId = "";


    int fileSeqno = 0;
    int totCnt = 0;
    int errCode = 0;

    Bufh2 hdata = new Bufh2();
    Buft2 htail = new Buft2();
    Buf2 data = new Buf2();

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
                comc.errExit("Usage : CrdF015 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hRecCount = 0;
            openTextFile();

            process();

            String filename = String.format("%s/media/crd/%s", comc.getECSHOME(), hFileName);
            stderr = String.format("FILENAME [%s] temstr=[%s]\n", hFileName, filename);
            showLogMessage("I", "", stderr);
            comc.writeReport(filename, lpar1, "MS950");

            if (hRecCount > 0) {
                ftpProc("JCIC");
                ftpProc("CRDATACREA");
                insertFileCtl();
                renameFile1(hFileName);
            } else {
                comc.fileDelete(filename);
            }

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", String.format("程式執行結束 , 總筆數:[%d],寫檔=[%d]\n", totalAll, hRecCount));
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
        String hDay = "";
        String hCreateDate = "";
        String pFilename = "";
        String pNewFilename = "";

        hCreateDate = "";
        hFileName = "";
        pFilename = "";
        pNewFilename = "";
        hDay = "";
        hChiDate = "";
        sqlCmd = "select to_char(sysdate,'mmdd') h_day,";
        sqlCmd += "to_char(sysdate,'yyyymmdd')   h_sysdate, ";
        sqlCmd += "trim(to_char(to_number(to_char(sysdate,'yyyymmdd')-19110000) ,'0000000')) h_chi_date ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDay      = getValue("h_day");
            hSysdate = getValue("h_sysdate");
            hChiDate = getValue("h_chi_date");
        }

        filename = String.format("%s%4s", CommJcic.JCIC_BANK_NO, hDay);
        pFilename = String.format("%s%%", filename);
        hCreateDate = sysDate;
        sqlCmd = "select max(file_name) p_new_filename ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
        sqlCmd += "  and crt_date     = ?  ";
        sqlCmd += "  and check_code   = '2' ";
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

        filename = String.format("%s%1d.kk2", filename, fileSeqno);
        hFileName = filename;
        rptName1 = filename;

        headerFile();
    }

    /***********************************************************************/
    void headerFile() throws Exception {
//        String temp = "";
        
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
        commFTP.hEflgGroupId    = "KK2";               /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "TOJCIC";            /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir   = String.format("%s/media/crd", comc.getECSHOME());
        commFTP.hEflgModPgm     = this.getClass().getName();

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("put %s", hFileName);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s] error\n", hEflgRefIpCode));
            showLogMessage("I", "", String.format("[%s]檔案傳送JCIC有誤(error),請通知相關人員處理\n",procCode));
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"%s執行完成 傳送JCIC失敗[%s]\""
                                                                    , prgmId, hFileName);
//          comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", tojcicmsg);
        } else {
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"%s執行完成 傳送JCIC無誤[%s]\""
                                                                    , prgmId, hFileName);
//          comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", tojcicmsg);
        }
    }

    /***********************************************************************/
    void insertFileCtl() throws Exception {
        setValue("file_name", hFileName);
        setValue("crt_date", sysDate);
        setValueInt("head_cnt", 1);
        setValueInt("record_cnt", hRecCount);
        setValue("check_code", "2");
        setValue("send_nccc_date", sysDate);
        daoTable = "crd_file_ctl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_file_ctl duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void process() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "'1'  as tmp_type,";
        sqlCmd += "card_no,";
        sqlCmd += "trans_type,";
        sqlCmd += "current_code,";
        sqlCmd += "oppost_reason,";
        sqlCmd += "oppost_date,";
        sqlCmd += "payment_date,";
        sqlCmd += "risk_amt,";
        sqlCmd += "kk4_note,";
        sqlCmd += "to_char(mod_time,'yyyymmdd') h_cdjc_mod_time,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += "from crd_jcic ";
        sqlCmd += "where to_jcic_date ='' ";
        sqlCmd += "UNION ";
        sqlCmd += "select '2' as tmp_type, ";
        sqlCmd += "card_no, ";
        sqlCmd += "trans_type, ";
        sqlCmd += "current_code, ";
        sqlCmd += "oppost_reason, ";
        sqlCmd += "oppost_date, ";
        sqlCmd += "payment_date, ";
        sqlCmd += "risk_amt, ";
        sqlCmd += "kk4_note, ";
        sqlCmd += "to_char(mod_time,'yyyymmdd'),  ";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from crd_jcic_kk2 ";
        sqlCmd += "where to_jcic_date ='' ";
        sqlCmd += "order by 2,3 desc,10 ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTableFlag = getValue("tmp_type", i);
            hCdjcCardNo = getValue("card_no", i);
            hCdjcTransType = getValue("trans_type", i);
            hCdjcCurrentCode = getValue("current_code", i);
            hCdjcOppostReason = getValue("oppost_reason", i);
            hCdjcOppostDate = getValue("oppost_date", i);
            hCdjcPaymentDate = getValue("payment_date", i);
            hCdjcRiskAmt = getValueDouble("risk_amt", i);
            hCdjcKk4Note = getValue("kk4_note", i);
            hCdjcModTime = getValue("h_cdjc_mod_time", i);
            hCdjcRowid = getValue("rowid", i);
            
            totalAll++;

            sqlCmd = "select a.unit_code,";
            sqlCmd += "decode(nvl((select b.virtual_flag from crd_item_unit b where b.unit_code = a.unit_code),''),'Y','V','P') h_tttt_virtual_flag, ";
            sqlCmd += "a.activate_flag,";
            sqlCmd += "a.old_activate_flag,";
            sqlCmd += "a.old_card_no,";
            sqlCmd += "a.change_status,";
            sqlCmd += "a.old_end_date,";
            sqlCmd += "a.curr_code ";
            sqlCmd += " from crd_card a,ptr_group_card c ";
            sqlCmd += "where card_no = ?  ";
            sqlCmd += "and c.group_code  = a.group_code ";
            sqlCmd += "and c.card_type  = a.card_type  ";
            sqlCmd += "fetch first 1 rows only ";
            setString(1, hCdjcCardNo);
            if (selectTable() > 0) {
                hMbosUnitCode = getValue("unit_code");
                hTtttVirtualFlag = getValue("h_tttt_virtual_flag");
                hCardActivateFlag = getValue("activate_flag");
                hCardOldActivateFlag = getValue("old_activate_flag");
                hCardOldCardNo = getValue("old_card_no");
                hCardChangeStatus = getValue("change_status");
                hCardOldEndDate = getValue("old_end_date");
                hCardCurrCode = getValue("curr_code");
            }

            getOtherData();
            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("crd Process record=[%d]\n", totCnt));
            
            if(hGroupCode.equals("1203") || hGroupCode.equals("1204")) {
            	continue;
            }

            if (totCnt == 1) {
                tempCardNo = hCdjcCardNo;
            } else {
                if (tempCardNo.equals(hCdjcCardNo)) {
                    updateRtn();
                    continue;
                } else {
                    tempCardNo = hCdjcCardNo;
                }
            }

            tempKind = "1";
            getCrdChgId();

            /* kk2 flag(new id) Y不傳送 */
            if (hChgiKk2Flag.equals("Y")) {
                continue;
            }

            if (hChgiPostJcicFlag.equals("N")) {
                insertCrdNopassJcic();
                updateRtn();
                continue;
            }
                           
            createJcicFile();

            if (tempKind.equals("2"))
                continue;
            /* 第一次執行 且 kk4 = '--' , 只 update kk4_note = '--' */
            if (hCdjcKk4Note.length() == 0 && tempX02Kk4.equals("--")) {
                tempKind = "3";
            }
            updateRtn();
            hRecCount++;
        }
                
        if (hRecCount > 0)               
        	tailFile();
    }

    /***********************************************************************/
    void tailFile() throws UnsupportedEncodingException {
        String temp = "";

        htail.len = "";
        htail.fileValue = CommJcic.TAIL_LAST_MARK;
        temp = String.format("%08d", hRecCount);
        htail.recordCnt = temp;
        buf = htail.allText();

        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

    }

    /***********************************************************************/
    void getOtherData() throws Exception {
        double hAssetValue = 0;

        hSupFlag = "";
        hGroupCode = "";
        hCardType = "";
        hPSeqno = "";
        hGpNo = "";
        hCardIdPSeqno = "";
        hCardMajorIdPSeqno = "";
        hCardMajorCardNo = "";
        hCardCorpNo = "";
        hCardCorpNoCode = "";
        hCardMajorRelation = "";
        hIssueDate = "";
        hIssueDate3 = "";
        hCardCurrentCode = "";
        hCardCorpActFlag = "";
        hCardOppostReason = "";
        hCardOppostDate = "";


        sqlCmd = "select ";
        sqlCmd += "group_code,";
        sqlCmd += "card_type,";
        sqlCmd += "sup_flag,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "current_code,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_no_code,";
        sqlCmd += "major_id_p_seqno,";
        sqlCmd += "major_relation,";
        sqlCmd += "major_card_no,";
        sqlCmd += "issue_date,";
        sqlCmd += "to_char(add_months(to_date(issue_date,'yyyymmdd'),3),'yyyymmdd') h_issue_date_3,";
        sqlCmd += "decode(corp_act_flag,'','N',corp_act_flag) h_card_corp_act_flag,";
        sqlCmd += "sup_flag,";
        sqlCmd += "oppost_reason,";
        sqlCmd += "oppost_date ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hCdjcCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hGroupCode = getValue("group_code");
            hCardType = getValue("card_type");
            hSupFlag = getValue("sup_flag");
            hGpNo = getValue("p_seqno");
            hPSeqno = getValue("acno_p_seqno");
            hCardIdPSeqno = getValue("id_p_seqno");
            hCardCurrentCode = getValue("current_code");
            hCardCorpNo = getValue("corp_no");
            hCardCorpNoCode = getValue("corp_no_code");
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
            hCardMajorRelation = getValue("major_relation");
            hCardMajorCardNo = getValue("major_card_no");
            hIssueDate = getValue("issue_date");
            hIssueDate3 = getValue("h_issue_date_3");
            hCardCorpActFlag = getValue("h_card_corp_act_flag");
            hCardSupFlag = getValue("sup_flag");
            hCardOppostReason = getValue("oppost_reason");
            hCardOppostDate = getValue("oppost_date");
        }

        hAcnoCardIndicator = "";
        hAcnoLineOfCreditAmt = 0;
        hAcnoSaleDate = "";
        hAcnoDebtCloseDate = "";
        hAcnoFlag = "";

        sqlCmd = "select card_indicator,";
        sqlCmd += "line_of_credit_amt,";
        sqlCmd += "sale_date, ";
        sqlCmd += "debt_close_date, ";
        sqlCmd += "acno_flag ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString(1, hPSeqno);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcnoCardIndicator = getValue("card_indicator");
            hAcnoLineOfCreditAmt = getValueDouble("line_of_credit_amt");
            hAcnoSaleDate = getValue("sale_date");
            hAcnoDebtCloseDate = getValue("debt_close_date");
            hAcnoFlag = getValue("acno_flag");
        }

        convertCardId();
        hAssureFlag = "";
        hCardSince = "";
        hAssureFlag = "N";
        /*******************************************************************
         * 1. 若為商務卡default assure_flag='Y' 2. CRD_IDNO asset_value > 0 ,default
         * assure_flag='Y' 3. CRD_RELA 有帳戶之保證人 ,default assure_flag='Y'
         *******************************************************************/
        if (hAcnoCardIndicator.equals("2")) {
            hAssureFlag = "Y";
        } else {
            hAssetValue = 0;
            sqlCmd = "select asset_value,";
            sqlCmd += "card_since ";
            sqlCmd += " from crd_idno  ";
            sqlCmd += "where id_p_seqno = ? ";
            setString(1, hCardIdPSeqno);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hAssetValue = getValueDouble("asset_value");
                hCardSince = getValue("card_since");
            }
            if (hAssetValue > 0) {
                hAssureFlag = "Y";
            } else {
                hRelaId = "";
                sqlCmd = "select rela_id ";
                sqlCmd += " from crd_rela  ";
                sqlCmd += "where acno_p_seqno = ?  ";
                sqlCmd += "and rela_type = '1'  ";
                sqlCmd += "fetch first 1 rows only ";
                setString(1, hPSeqno);
                recordCnt = selectTable();
                if (recordCnt > 0) {
                    hRelaId = getValue("rela_id");
                }
                if (hRelaId.length() > 0)
                    hAssureFlag = "Y";
            }
        }
    }

    /***********************************************************************/
    void createJcicFile() throws Exception {
        String tmp = "";
        long creditAmt = 0;
        String tTransType = "";        
        
        /* 2:主卡 3:附卡 */
        if (hSupFlag.equals("0")) {
        	tTransType = "M";
        }            
        else {
        	tTransType = "S";
        }
            
        if (hAcnoCardIndicator.equals("2")) {
//            tTransType = "P";
            tTransType = "C";
        }
         
        data.transType = tTransType;

        data.transCode = hCdjcTransType;
        data.bankNo = CommJcic.JCIC_BANK_NO;
        data.cardStyle = hTtttVirtualFlag;
        if(tTransType.equals("C")) {
        	if(selectCrdCorp()) {
        		data.applyId = hObuId;
        	}else {
        		data.applyId = hCardCorpNo;
        	}
        }else {
        	data.applyId = comcr.ufIdnoId(hCardIdPSeqno);
        }
        data.cardName = hCardName;
        data.cardNote = hCardNote;
        data.cardNo = hCdjcCardNo;

        tmp = String.format("%07d", comcr.str2long(hIssueDate) - 19110000);
        data.cardSince = tmp;

        if ((!hCdjcCurrentCode.equals("0")) && (!hCardCurrentCode.equals("0"))) {
            convertCode();
            data.currentCode = hJcicCode;
            if (hCardOppostDate.length() >= 8) {
                tmp = String.format("%07d", comcr.str2long(hCardOppostDate) - 19110000);
                data.oppostDate = tmp;
                if (hCdjcOppostReason.length() > 0) {
                	String jcicOppostReason = getJcicOppostReason();
                    data.oppostReason = jcicOppostReason;
                }                    
            }
        }
        else {
        	data.currentCode = "";
        	data.oppostReason = "";
        	data.oppostDate = "";
        }
        
        if ((hSupFlag.equals("1")) && (hCardMajorCardNo.length() > 0)) {
        	data.majorCardNo = hCardMajorCardNo;
        }
            
        if (hAcnoCardIndicator.equals("2")) {
            data.majorCardNo = hCdjcCardNo;
        }
        
		if (tTransType.equals("C")) {
			data.majorCardNo = "";
		}

        if (hCardMajorRelation.length() > 0) {
        	data.relation = hCardMajorRelation;
        }
            
        if (hAcnoCardIndicator.equals("2")) {
            data.relation = "5";
        }
        
        if (tTransType.equals("C")) {
        	data.relation = "";
        }

        if (hAcnoLineOfCreditAmt >= 0) {
            creditAmt = (long) (hAcnoLineOfCreditAmt / 1000);
            tmp = String.format("%06d", creditAmt);
            data.creditAmt = tmp;
        }
        data.creditNote = "Y";
        if (hCdjcPaymentDate.length() > 0) {
            tmp = String.format("%07d", comcr.str2long(hCdjcPaymentDate) - 19110000);
            data.paymentDate = tmp;
        }
        else
        {
	 if(comc.getSubString(hCardOppostReason,0, 1).equals("U"))
	  {
           tmp = String.format("%07d", comcr.str2long(hAcnoDebtCloseDate) - 19110000);
           data.paymentDate = tmp;
	  }
	}	

        if (hCdjcRiskAmt > 0.0)
            if (((hCdjcCurrentCode.equals("2")) && (comc.getSubString(hCardOppostReason,0, 1).equals("P"))) ||
                ((hCdjcCurrentCode.equals("5")) && (comc.getSubString(hCardOppostReason,0, 1).equals("M"))) ||
                ((hCdjcCurrentCode.equals("5")) && (comc.getSubString(hCardOppostReason,0, 1).equals("N")))) {
                tmp = String.format("%06.0f", hCdjcRiskAmt / 1000);
            } else
                tmp = String.format("%6s", " ");
        else {
            if (((hCdjcCurrentCode.equals("2")) && (comc.getSubString(hCardOppostReason,0, 1).equals("P"))) ||
                ((hCdjcCurrentCode.equals("5")) && (comc.getSubString(hCardOppostReason,0, 1).equals("M"))) ||
                ((hCdjcCurrentCode.equals("5")) && (comc.getSubString(hCardOppostReason,0, 1).equals("N")))) {
                tmp = String.format("%6.6s", "000000");
            } else
                tmp = String.format("%6s", " ");
        }
        data.riskAmt = tmp;
        if (hAcnoCardIndicator.equals("2")) {
        	data.majorId = hCardCorpNo;
        }
            
        if (hSupFlag.equals("1")) {
        	data.majorId = comcr.ufIdnoId(hCardMajorIdPSeqno);
        }            
        
		if (tTransType.equals("C")) { 
			data.majorId = "";
		}

        data.transferNote = " ";
        if ((hAcnoSaleDate.length() != 0) && (hCdjcCurrentCode.equals("3"))) {
            data.transferNote = "T";
            data.oppostReason = "J";
            data.paymentDate = "       ";
        }

        if (hCdjcModTime.length() >= 8) {
            tmp = String.format("%07d", comcr.str2long(hCdjcModTime) - 19110000);
            data.updateDate = tmp;
        }

        if (hCardActivateFlag.equals("2"))
            data.openNote = "A";
        else {
            if (hCardOldCardNo.length() == 0 && hCardChangeStatus.equals("3"))
                data.openNote = "B";
            else {
                data.openNote = "C";
                if (comcr.str2long(hCardOldEndDate) >= comcr.str2long(hSysdate)) {
                 // if (h_card_old_activate_flag.equals("2"))
                    if (hCardOldActivateFlag.equals("1"))
                        data.openNote = "A";
                    else
                        data.openNote = "C";
                }
            }
        }

        hBillTypeFlag = "";
        sqlCmd = "select bill_type_flag ";
        sqlCmd += " from act_jcic_log  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and acct_month in (select max(acct_month) from act_jcic_log where p_seqno = ? )  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hGpNo);
        setString(2, hGpNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBillTypeFlag = getValue("bill_type_flag");

            tempX02Kk4 = hBillTypeFlag;
        } else {
            tempX02Kk4 = "--";
            if (comcr.str2long(hSysdate) > comcr.str2long(hIssueDate3)) {
                tempX02Kk4 = "01";
            } 
            else if (tTransType.equals("S") || tTransType.equals("P") || tTransType.equals("C")) {
            	if ((hCardCorpActFlag.equals("Y")) || (!hCardCorpActFlag.equals("Y") && hCardSupFlag.equals("1"))) {
            		tempX02Kk4 = "01";
            	}                    
            }                
        }

        if (hSupFlag.equals("1")) {
            tempX02Kk4 = "xx";
        }

        if (hCdjcKk4Note.equals("--")) {
            data.updateDate = hChiDate;
            if (tempX02Kk4.equals("--")) {
                /* 第一次以後執行 且 kk4 = '--' , 不送 JCIC 也不 update */
                tempKind = "2";
                return;
            } else
                data.transCode = "C";
        }

        data.kk4Note = tempX02Kk4;

        hRelaId = "";
        sqlCmd = "select rela_id ";
        sqlCmd += " from crd_rela  ";
        sqlCmd += "where acno_p_seqno   = ?  ";
        sqlCmd += "and rela_type  = '1'  ";
        sqlCmd += "and length(rela_id) = 10  ";
        sqlCmd += "and mod_time in (select max(mod_time) from crd_rela where acno_p_seqno   = ?  ";
        sqlCmd += "and rela_type  = '1'  ";
        sqlCmd += "and length(rela_id) = 10 )  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hPSeqno);
        setString(2, hPSeqno);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hRelaId = getValue("rela_id");
        }

        if (!hSupFlag.equals("0")) {
            hRelaId = "";
        }
        tempX10 = String.format("%10.10s", hRelaId);
        data.relaId = tempX10;
        /* '1':美元:(840)， '2':歐元(978)， '4':日圓(392) */
        tempX04 = String.format("%-4.4s", " ");
        if (hCardCurrCode.equals("840"))
            tempX04 = String.format("%-4.4s", "1");
        else if (hCardCurrCode.equals("978"))
            tempX04 = String.format("%-4.4s", "2");
        else if (hCardCurrCode.equals("392"))
            tempX04 = String.format("%-4.4s", "4");
        data.currCodeType = tempX04;

        buf = data.allText();
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

    }

    /***********************************************************************/
    void convertCode() throws Exception {
    	hJcicCode = "";

        sqlCmd = "select rtrim(map_value) h_jcic_code ";
        sqlCmd += " from crd_message  ";
        sqlCmd += "where msg_type = 'JCIC_STOP'  ";
        sqlCmd += "and msg_value = ? ";
        setString(1, hCardCurrentCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hJcicCode = getValue("h_jcic_code");
        } else {
            hJcicCode = hCardCurrentCode;
        }
    }

    /***********************************************************************/
    private String getJcicOppostReason() throws Exception {

        sqlCmd = "select jcic_opp_reason ";
        sqlCmd += " from cca_opp_type_reason  ";
        sqlCmd += "where opp_status = ?  ";
        setString(1, hCdjcOppostReason);
        int recordCnt = selectTable();
        
        if (recordCnt <= 0)
			return null;

		return getValue("jcic_opp_reason");
    }

    /***********************************************************************/
    boolean selectCrdCorp() throws Exception {
        hObuId = "";
        sqlCmd = "select obu_id ";
        sqlCmd += " from crd_corp  ";
        sqlCmd += "where corp_no = ? ";
        setString(1, hCardCorpNo);
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
    void insertCrdNopassJcic() throws Exception {
        String[] info = comcr.getIDInfo(hCardIdPSeqno);

        setValue("old_id", info[0]);
        setValue("old_id_code", info[1]);
        setValue("chi_name", hChgiChiName);
        setValue("id_p_seqno", hChgiIdPSeqno);
        setValue("post_kind", "kk2");
        setValue("post_jcic_date", sysDate);
        setValue("card_no", hCdjcCardNo);
        setValue("OPPOST_REASON", hCardOppostReason);
        setValue("OPPOST_DATE", hCardOppostDate);
        setValue("MOD_USER", "CrdF014");
        setValue("MOD_TIME", sysDate);
        setValue("MOD_PGM", "CrdF014");
        daoTable = "crd_nopass_jcic";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_nopass_jcic duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void getCrdChgId() throws Exception {
        hChgiCreateUser = "";
        hChgiApprovUser = "";
        hChgiChiName = "";
        hChgiIdPSeqno = "";
        hChgiPostJcicFlag = "";
        hChgiKk2Flag = "";

        String[] info = comcr.getIDInfo(hCardIdPSeqno);

        sqlCmd = "select chi_name,";
        sqlCmd += "crt_user,";
        sqlCmd += "apr_user,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "decode(post_jcic_flag,'','N',post_jcic_flag) h_chgi_post_jcic_flag ";
        sqlCmd += " from crd_chg_id  ";
        sqlCmd += "where old_id_no  = ?  ";
        sqlCmd += "and old_id_no_code = ? ";
        setString(1, info[0]);
        setString(2, info[1]);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgiChiName = getValue("chi_name");
            hChgiCreateUser = getValue("crt_user");
            hChgiApprovUser = getValue("apr_user");
            hChgiIdPSeqno = getValue("id_p_seqno");
            hChgiPostJcicFlag = getValue("h_chgi_post_jcic_flag");
        }

        sqlCmd = "select decode(kk2_flag,'','N',kk2_flag) h_chgi_kk2_flag ";
        sqlCmd += " from crd_chg_id  ";
        sqlCmd += "where id_no      = ?  ";
        sqlCmd += "  and id_no_code = ? ";
        setString(1, info[0]);
        setString(2, info[1]);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgiKk2Flag = getValue("h_chgi_kk2_flag");
        }
    }

    /***********************************************************************/
    void updateRtn() throws Exception {
        switch (comcr.str2int(hTableFlag)) {
        case 1:
            updateCrdJcic();
            break;
        case 2:
            updateCrdJcicKk2();
            break;
        }
    }

    /***********************************************************************/
    /***
     * 抓取JCIC之信用名稱及信用標章代號
     * 
     * @throws Exception
     */
    void convertCardId() throws Exception {
        String pCardnote = "";

        hCardName = "";
        hCardNote = "";
        pCardnote = "";
        int recordCnt = 0;

        try {
            sqlCmd = "select map_value ";
            sqlCmd += " from crd_message  ";
            sqlCmd += "where msg_type = 'JCIC_CARD'  ";
            sqlCmd += "and msg_value = ? ";
            setString(1, hCardType);
            recordCnt = selectTable();
        } catch (Exception ex) {
            recordCnt = 0;
        }

        if (recordCnt > 0) {
            hCardName = getValue("map_value");
        }

        else {
            if (!hCardType.equals("NC")) {
                hCardName = hCardType;
            } else {
                hCardName = "V";
            }
        }
        if (hAcnoCardIndicator.equals("2"))
            hCardNote = "B";
        if (hAcnoCardIndicator.equals("1")) {
            sqlCmd = "select card_note_jcic ";
            sqlCmd += " from ptr_card_type  ";
            sqlCmd += "where card_type = ? ";
            setString(1, hCardType);
            recordCnt = selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_ptr_card_type not found!", "", hCallBatchSeqno);
            }
            if (recordCnt > 0) {
                pCardnote = getValue("card_note_jcic");
            }

            hCardNote = pCardnote;
        }
    }

    /***********************************************************************/
    void updateCrdJcicKk2() throws Exception {

        String stringTmp=comc.getSubString(hCardOppostReason, 0, 1).equals("U") ? (hCdjcPaymentDate.equals("") ? hAcnoDebtCloseDate : hCdjcPaymentDate) : hCdjcPaymentDate;

if(debug == 1) showLogMessage("I", "", "   8881 payment_date=" + stringTmp);

        daoTable   = "crd_jcic_kk2";
        updateSQL  = " to_jcic_date = ?,";
        updateSQL += " kk4_note     = ?,";
        updateSQL += " payment_date = ?,";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ?";
        whereStr   = "where rowid   = ? ";
        setString(1, tempKind.equals("1") ? sysDate : "");
        setString(2, tempX02Kk4);
        setString(3, stringTmp);
        setString(4, prgmId);
        setRowId( 5, hCdjcRowid);

        updateTable();

    }

    /***********************************************************************/
    void updateCrdJcic() throws Exception {

        String stringTmp=comc.getSubString(hCardOppostReason, 0, 1).equals("U") ? (hCdjcPaymentDate.equals("") ? hAcnoDebtCloseDate : hCdjcPaymentDate) : hCdjcPaymentDate;

if(debug == 1) showLogMessage("I", "", "   8882 payment_date=" + stringTmp);

        daoTable   = "crd_jcic";
        updateSQL  = " error_code   = ?,";
        updateSQL += " to_jcic_date = ?,";
        updateSQL += " payment_date = ?,";
        updateSQL += " kk4_note     = ?,";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_user     = ?,";
        updateSQL += " mod_pgm      = ?";
        whereStr   = "where rowid   = ? ";
        setString(1, hCdjcErrorCode);
        setString(2, tempKind.equals("1") ? sysDate : "");
        setString(3, stringTmp);
        setString(4, tempX02Kk4);
        setString(5, hModUser);
        setString(6, prgmId);
        setRowId( 7, hCdjcRowid);

        updateTable();

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdF015 proc = new CrdF015();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Bufh2 {
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
//            rtn += fixLeft(fileId, 18);
//            rtn += fixLeft(bankNo, 3);
//            rtn += fixLeft(filler1, 5);
//            rtn += fixLeft(sendDate, 7);
//            rtn += fixLeft(fileExt, 2);
//            rtn += fixLeft(filler2, 10);
//            rtn += fixLeft(contactTel, 16);
//            rtn += fixLeft(contactMsg, 80);
//            rtn += fixLeft(filler3, 249);
//            rtn += fixLeft(len, 1);
//            return rtn;
//        }

        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 300; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = spc + str;
            byte[] bytes = str.getBytes("MS950");
            int offset = bytes.length - len;
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, offset, vResult, 0, len);
            return new String(vResult, "MS950");
        }
    }

    class Buft2 {
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

    class Buf2 {
        String transType;
        String transCode;
        String bankNo;
        String filler1;
        String cardStyle;
        String applyId;
        String cardName;
        String cardNote;
        String cardNo;
        String cardSince;
        String currentCode;
        String oppostReason;
        String filler2;
        String oppostDate;
        String majorCardNo;
        String relation;
        String creditAmt;
        String creditNote;
        String filler3;
        String paymentDate;
        String riskAmt;
        String majorId;
        String transferNote;
        String updateDate;
        String openNote;
        String kk4Note;
        String relaId;
        String currCodeType;
        String filler4;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(transType, 1);
            rtn += fixLeft(transCode, 1);
            rtn += fixLeft(bankNo, 3);
            rtn += fixLeft(filler1, 4);
            rtn += fixLeft(cardStyle, 1);
            rtn += fixLeft(applyId, 10);
            rtn += fixLeft(cardName, 1);
            rtn += fixLeft(cardNote, 1);
            rtn += fixLeft(cardNo, 20);
            rtn += fixLeft(cardSince, 7);
            rtn += fixLeft(currentCode, 1);
            rtn += fixLeft(oppostReason, 1);
            rtn += fixLeft(filler2, 2);
            rtn += fixLeft(oppostDate, 7);
            rtn += fixLeft(majorCardNo, 20);
            rtn += fixLeft(relation, 1);
            rtn += fixLeft(creditAmt, 6);
            rtn += fixLeft(creditNote, 1);
            rtn += fixLeft(filler3, 1);
            rtn += fixLeft(paymentDate, 7);
            rtn += fixLeft(riskAmt, 6);
            rtn += fixLeft(majorId, 10);
            rtn += fixLeft(transferNote, 1);
            rtn += fixLeft(updateDate, 7);
            rtn += fixLeft(openNote, 1);
            rtn += fixLeft(kk4Note, 2);
            rtn += fixLeft(relaId, 10);
            rtn += fixLeft(currCodeType, 4);
            rtn += fixLeft(filler4, 253);
//            rtn += fixLeft(len, 1);
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

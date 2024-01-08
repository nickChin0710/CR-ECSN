/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/03/30  V1.00.01    Pino      bank_no改為006                                                                                    *
*  109/12/18  V1.00.02    Wilson    境外公司戶報送JCIC處理                                                                       *   
*  109/12/23  V1.00.01   shiyuqi       updated for project coding standard   * 
*  110/04/01  V1.00.02   Justin     use common value                          *
*  110/08/24  V1.00.05   Wilson     停卡原因讀取cca_opp_type_reason的jcic_opp_reason* 
*  112/07/05  V1.00.06   Wilson     資料別P改成C                                 * 
*  112/08/10  V1.00.07   Wilson     團代1203、1204不報送                                                                             *
*  112/08/24  V1.00.08    Wilson    修正停用欄位問題                                                                                      *
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

/*產生每日人工建檔送JCIC信用卡資料*/
public class CrdF045 extends AccessDAO {
	private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_KK2;
    private String progname = "產生每日人工建檔送JCIC信用卡資料112/08/24  V1.00.08 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String prgmId = "CrdF045";
    String rptName1 = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq1 = 0;
    String buf = "";
    String hCallBatchSeqno = "";

    String hDay = "";
    String hSysdate = "";
    String pNewFilename = "";
    String pFilename = "";
    String hCreateDate = "";
    String hJcrdCardNo = "";
    String hJcrdCreateDate = "";
    String hJcrdTransType = "";
    String hJcrdId = "";
    String hJcrdIdPSeqno = "";
    String hJcrdIdCode = "";
    String hJcrdCorpNo = "";
    String hJcrdCorpNoCode = "";
    String hJcrdJcicCardType = "";
    String hJcrdCardType = "";
    String hJcrdCardSince = "";
    String hJcrdSupFlag = "";
    String hJcrdChiName = "";
    String hJcrdEngName = "";
    String hJcrdMajorCardNo = "";
    String hJcrdMajorRelation = "";
    String hJcrdMajorId = "";
    String hJcrdCurrentCode = "";
    String hJcrdOppostReason = "";
    String hJcrdOppostDate = "";
    long hJcrdCreditLmt = 0;
    String hJcrdCreditFlag = "";
    String hJcrdPaymentDate = "";
    double hJcrdRiskAmt = 0;
    String hJcrdDebitTransCode = "";
    String hJcrdUpdateDate = "";
    String hJcrdBillTypeFlag = "";
    String hJcrdRelaId = "";
    String hJcrdRowid = "";
    String hGroupCode = "";
    String hCardType = "";
    String hSupFlag = "";
    String hPSeqno = "";
    String hIdPSeqno = "";
    String hCurrentCode = "";
    String hCorpNo = "";
    String hCorpNoCode = "";
    String hMajorRelation = "";
    String hMajorCardNo = "";
    String hIssueDate = "";
    String hIssueDate3 = "";
    String hCardGpNo = "";
    String hOppostReason = "";
    String hOppostDate = "";
    String hCardOldActivateFlag = "";
    String hCardNewEndDate = "";
    String hCardChangeStatus = "";
    String hCardOldEndDate = "";
    String hCardCorpActFlag = "";
    String hCardActivateFlag = "";
    String hCardOldCardNo = "";
    String hCardOppostReason = "";
    String hCardOppostDate = "";
    String hCardIndicator = "";
    double hCreditAmt = 0;
    String hIsRc = "";
    double hAssetValue = 0;
    String hCardSince = "";
    String hRelaId = "";
    String hCardName = "";
    String pCardNote = "";
    String hFileName = "";
    int hRecCount = 0;
    String hContactTel = "";
    String hContactMsg = "";
    String hChgiChiName = "";
    String hChgiCrtUser = "";
    String hChgiAprUser = "";
    String hChgiIdPSeqno = "";
    String hChgiPostJcicFlag = "";
    String hChgiKk2Flag = "";
    String hAcnoFlag = "";
    String hObuId = "";

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
    String hAssureFlag = "";
    String hCardSupFlag = "";
    String hCardNote = "";
    int total = 0;
    int fileSeqno = 0;
    int totalAll = 0;
    int hTotCount = 0;
    int errCode = 0;

    Bufh2 hdata = new Bufh2();
    Buft2 htail = new Buft2();
    Buf2 data = new Buf2();
    private String hCardIdPSeqno = "";

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
                comc.errExit("Usage : CrdF045 ", "");
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
            filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
            if (hRecCount > 0) {

                comc.writeReport(filename, lpar1);
                ftpProc("JCIC");
                ftpProc("CRDATACREA");
                insertFileCtl();
                renameFile1(hFileName);
            } else {
                comc.fileDelete(filename);
                showLogMessage("I", "", "無JCIC資料產生");
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
        sqlCmd = "select to_char(sysdate,'mmdd') h_day,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_sysdate ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDay = getValue("h_day");
            hSysdate = getValue("h_sysdate");
        }

        filename = String.format("%s%4s", CommJcic.JCIC_BANK_NO, hDay);
        pFilename = String.format("%s%%", filename);
        hCreateDate = sysDate;
        sqlCmd = "select max(file_name) p_new_filename ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
        sqlCmd += "and crt_date = ?  ";
        sqlCmd += "and check_code = '2' ";
        setString(1, pFilename);
        setString(2, hCreateDate);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            pNewFilename = getValue("p_new_filename");
        }
        fileSeqno = 0;
        if ((pNewFilename.length() > 0)) {
            showLogMessage("I", "", String.format("OLD_FILE_NAME[%s] \n", pNewFilename));
            tmp = pNewFilename.substring(7, 8);
            fileSeqno = comcr.str2int(tmp) + 1;
            showLogMessage("I", "", String.format("FILE SEQNO [%d]\n", fileSeqno));
        } else
            fileSeqno = 1;
        filename = String.format("%s%1d.kk2", filename, fileSeqno);
        hFileName = filename;
        rptName1 = filename;
        temstr = String.format("%s/media/crd/%s", comc.getECSHOME(), hFileName);
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", String.format("FILENAME [%s] [%s]\n", hFileName, temstr));

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
            comcr.errRtn("無聯絡人資料 from  ptr_sys_parm!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hContactTel = getValue("wf_value");
            hContactMsg = getValue("wf_value2");
        }

        return;
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
        sqlCmd += "card_no,";
        sqlCmd += "crt_date,";
        sqlCmd += "trans_type,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "nvl(uf_idno_id(id_p_seqno), '') as id,";
        sqlCmd += "nvl((select id_no_code from crd_idno where crd_idno.id_p_seqno = id_p_seqno fetch first 1 rows only),'') as id_code,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_no_code,";
        sqlCmd += "jcic_card_type,";
        sqlCmd += "card_type,";
        sqlCmd += "card_since,";
        sqlCmd += "sup_flag,";
        sqlCmd += "chi_name,";
        sqlCmd += "eng_name,";
        sqlCmd += "m_card_no,";
        sqlCmd += "m_relation,";
        sqlCmd += "nvl(uf_idno_id(m_id_p_seqno),'') as major_id,";
        sqlCmd += "current_code,";
        sqlCmd += "oppost_reason,";
        sqlCmd += "oppost_date,";
        sqlCmd += "credit_lmt,";
        sqlCmd += "credit_flag,";
        sqlCmd += "payment_date,";
        sqlCmd += "risk_amt,";
        sqlCmd += "debit_trans_code,";
        sqlCmd += "update_date,";
        sqlCmd += "bill_type_flag,";
        sqlCmd += "rela_id,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += "from crd_jcic_card ";
        sqlCmd += "where to_jcic_date ='' ";
        sqlCmd += "and apr_user != '' ";
        sqlCmd += "and apr_date != '' ";
        sqlCmd += "order by crt_date,card_no,trans_type ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hJcrdCardNo = getValue("card_no", i);
            hJcrdCreateDate = getValue("crt_date", i);
            hJcrdTransType = getValue("trans_type", i);
            hJcrdIdPSeqno = getValue("id_p_seqno", i);
            hJcrdId = getValue("id", i);
            hJcrdIdCode = getValue("id_code", i);
            hJcrdCorpNo = getValue("corp_no", i);
            hJcrdCorpNoCode = getValue("corp_no_code", i);
            hJcrdJcicCardType = getValue("jcic_card_type", i);
            hJcrdCardType = getValue("card_type", i);
            hJcrdCardSince = getValue("card_since", i);
            hJcrdSupFlag = getValue("sup_flag", i);
            hJcrdChiName = getValue("chi_name", i);
            hJcrdEngName = getValue("eng_name", i);
            hJcrdMajorCardNo = getValue("m_card_no", i);
            hJcrdMajorRelation = getValue("m_relation", i);
            hJcrdMajorId = getValue("major_id", i);
            hJcrdCurrentCode = getValue("current_code", i);
            hJcrdOppostReason = getValue("oppost_reason", i);
            hJcrdOppostDate = getValue("oppost_date", i);
            hJcrdCreditLmt = getValueLong("credit_lmt", i);
            hJcrdCreditFlag = getValue("credit_flag", i);
            hJcrdPaymentDate = getValue("payment_date", i);
            hJcrdRiskAmt = getValueDouble("risk_amt", i);
            hJcrdDebitTransCode = getValue("debit_trans_code", i);
            hJcrdUpdateDate = getValue("update_date", i);
            hJcrdBillTypeFlag = getValue("bill_type_flag", i);
            hJcrdRelaId = getValue("rela_id", i);
            hJcrdRowid = getValue("rowid", i);

            hTotCount++;
            getOtherData();
            getCrdChgId();
            
            if(hGroupCode.equals("1203") || hGroupCode.equals("1204")) {
            	continue;
            }

            /* kk2 flag(new id) Y不傳送 */
            if (hChgiKk2Flag.equals("Y")) {
                continue;
            }

            if (hChgiPostJcicFlag.equals("N")) {
                insertCrdNopassJcic();
                updateCrdJcicCard();
                continue;
            }

            createJcicFile();
            updateCrdJcicCard();
            hRecCount++;
        }
        if (hRecCount > 0)
            tailFile();
    }

    /**
     * @throws UnsupportedEncodingException
     *************************************************************************/
    void tailFile() throws UnsupportedEncodingException {

        htail.fileValue = CommJcic.TAIL_LAST_MARK;
        String temp = String.format("%08d", hRecCount);
        htail.recordCnt = temp;
        buf = htail.allText();

        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

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
    private String getJcicOppostReason() throws Exception {
    	
        sqlCmd = "select jcic_opp_reason ";
        sqlCmd += " from cca_opp_type_reason  ";
        sqlCmd += "where opp_status = ?  ";
        setString(1, hJcrdOppostReason);
        int recordCnt = selectTable();
        
        if (recordCnt <= 0)
			return null;

		return getValue("jcic_opp_reason");
    }

    /***********************************************************************/
    void insertCrdNopassJcic() throws Exception {
        setValue("old_id", hJcrdId);
        setValue("old_id_code", hJcrdIdCode);
        setValue("chi_name", hChgiChiName);
        setValue("id_p_seqno", hChgiIdPSeqno);
        setValue("post_kind", "kk2");
        setValue("post_jcic_date", sysDate);
        setValue("card_no", hJcrdCardNo);
        setValue("OPPOST_REASON", hCardOppostReason);
        setValue("OPPOST_DATE", hCardOppostDate);
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
    void updateCrdJcicCard() throws Exception {
        daoTable = "crd_jcic_card";
        updateSQL = "to_jcic_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " jcic_filename = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm  = ?";
        whereStr = "where rowid    = ? ";
        setString(1, hFileName);
        setString(2, comc.commGetUserID());
        setString(3, prgmId);
        setRowId(4, hJcrdRowid);
        updateTable();
    }

    /***********************************************************************/
    void createJcicFile() throws Exception {
        String tmp = "";
        String tTransType = "";
        long tCreditAmt = 0;

        data = null;
        data = new Buf2();

        if (hSupFlag.equals("0")) {
        	tTransType = "M";
        }            
        else {
        	tTransType = "S";
        }            

        if (hCardIndicator.equals("2")) {
//            tTransType = "P";
        	tTransType = "C";
        }

        data.transType = tTransType;

        data.transCode = hJcrdTransType;
        data.bankNo = CommJcic.JCIC_BANK_NO;
        data.cardStyle = "P";
        if(tTransType.equals("C")) {
        	if(selectCrdCorp()) {
        		data.applyId = hObuId;
        	}else {
        		data.applyId = hCorpNo;
        	}
        }else {
        	data.applyId = hJcrdId;
        }
        data.cardName = hCardName;
        data.cardNote = hCardNote;
        data.cardNo = hJcrdCardNo;

        tmp = String.format("%07d", comcr.str2long(hJcrdCardSince) - 19110000);
        data.cardSince = tmp;
        /***********************************************************
         * 當停掛時,寫入檔內資料,oppost_date,oppost_reason
         ***********************************************************/
        if ((hJcrdCurrentCode.equals("0") == false) && (hJcrdCurrentCode.equals("0") == false)) {
            data.currentCode = hJcrdCurrentCode;
            if (hJcrdOppostDate.length() >= 8) {
                tmp = String.format("%07d", comcr.str2long(hJcrdOppostDate) - 19110000);
                data.oppostDate = tmp;
                if (hJcrdOppostReason.length() > 0) {
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
        
        if ((hSupFlag.equals("1")) && (hMajorCardNo.length() > 0)) {
        	data.majorCardNo = hMajorCardNo;
        }
            
        if (hCardIndicator.equals("2")) {
            data.majorCardNo = hJcrdCardNo;
        }
        
        if (tTransType.equals("C")) {
			data.majorCardNo = "";
		}

        if (hJcrdMajorRelation.length() > 0) {
        	data.relation = hJcrdMajorRelation;
        }
            
        if (hCardIndicator.equals("2")) {
            data.relation = "5";
        }
        
        if (tTransType.equals("C")) {
        	data.relation = "";
        }

        if (hJcrdCreditLmt >= 0) {
                tCreditAmt = hJcrdCreditLmt / 1000;
                tmp = String.format("%06d", tCreditAmt);
                data.creditAmt = tmp;
            }
            data.creditNote = "Y";
            if (hJcrdPaymentDate.length() > 0) {
                tmp = String.format("%07d", comcr.str2long(hJcrdPaymentDate) - 19110000);
                data.paymentDate = tmp;
            }
            if (hJcrdRiskAmt > 0)
                tmp = String.format("%06d", hJcrdRiskAmt / 1000);
            else
                tmp = String.format("%6s", " ");

            data.riskAmt = tmp;
            /* 商務卡放統一編號,附卡放正卡ID */
            if (hCardIndicator.equals("2")) {
                data.majorId = hCorpNo;
            }
            if (hJcrdSupFlag.equals("1")) {
            	data.majorId = hJcrdMajorId;
            }
                            
            if (tTransType.equals("C")) {  
    			data.majorId = "";
    		}

            if (hJcrdDebitTransCode.length() > 0)
                data.transferNote = hJcrdDebitTransCode;
            else
                data.transferNote = " ";

            if (hJcrdUpdateDate.length() >= 8) {
                tmp = String.format("%07d", comcr.str2long(hJcrdUpdateDate) - 19110000);
                data.updateDate = tmp;
            } else {
                tmp = String.format("%07d", comcr.str2long(chinDate));
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
                     // if (h_card_old_activate_flag.equals("2"))  // 2:開卡 1:關閉
                        if (hCardOldActivateFlag.equals("1"))
                            data.openNote = "A";
                        else
                            data.openNote = "C";
                    }
                }
            }
        data.kk4Note = hJcrdBillTypeFlag;

        String tempX10 = String.format("%10.10s", hJcrdRelaId);
        data.relaId = tempX10;

        buf = data.allText();
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        return;
    }

    /***********************************************************************/
    void getOtherData() throws Exception {
        double hAssetValue = 0;

        hSupFlag = "";
        hGroupCode = "";
        hCardType = "";
        hPSeqno = "";
        hIdPSeqno = "";
        hMajorCardNo = "";
        hCorpNo = "";
        hCorpNoCode = "";
        hMajorRelation = "";
        hIssueDate = "";
        hCurrentCode = "";
        hCardIndicator = "";
        hOppostReason = "";
        hOppostDate = "";
        hCreditAmt = 0;
        hCardActivateFlag = "";
        hCardOldCardNo = "";
        hCardOldActivateFlag = "";
        hCardNewEndDate = "";
        hCardChangeStatus = "";
        hCardOldEndDate = "";
        hCardCorpActFlag = "";
        hCardGpNo = "";
        hIssueDate3 = "";
        hCardOppostReason = "";
        hCardOppostDate = "";

        sqlCmd = "select ";
        sqlCmd += "group_code,";
        sqlCmd += "card_type,";
        sqlCmd += "sup_flag,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "current_code,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_no_code,";
        sqlCmd += "major_relation,";
        sqlCmd += "major_card_no,";
        sqlCmd += "issue_date,";
        sqlCmd += "to_char(add_months(to_date(issue_date,'yyyymmdd'),3),'yyyymmdd') h_issue_date_3,";
        sqlCmd += "p_seqno,";
        sqlCmd += "oppost_reason,";
        sqlCmd += "oppost_date,";
        sqlCmd += "old_activate_flag,";
        sqlCmd += "new_end_date,";
        sqlCmd += "change_status,";
        sqlCmd += "old_end_date,";
        sqlCmd += "corp_act_flag,";
        sqlCmd += "activate_flag,";
        sqlCmd += "old_card_no,";
        sqlCmd += "oppost_reason,";
        sqlCmd += "oppost_date ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hJcrdCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_card not found!", hJcrdCardNo, comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
        	hGroupCode = getValue("group_code");
            hCardType = getValue("card_type");
            hSupFlag = getValue("sup_flag");
            hPSeqno = getValue("acno_p_seqno");
            hIdPSeqno = getValue("id_p_seqno");
            hCurrentCode = getValue("current_code");
            hCorpNo = getValue("corp_no");
            hCorpNoCode = getValue("corp_no_code");
            hMajorRelation = getValue("major_relation");
            hMajorCardNo = getValue("major_card_no");
            hIssueDate = getValue("issue_date");
            hIssueDate3 = getValue("h_issue_date_3");
            hCardGpNo = getValue("p_seqno");
            hOppostReason = getValue("oppost_reason");
            hOppostDate = getValue("oppost_date");
            hCardOldActivateFlag = getValue("old_activate_flag");
            hCardNewEndDate = getValue("new_end_date");
            hCardChangeStatus = getValue("change_status");
            hCardOldEndDate = getValue("old_end_date");
            hCardCorpActFlag = getValue("corp_act_flag");
            hCardActivateFlag = getValue("activate_flag");
            hCardOldCardNo = getValue("old_card_no");
            hCardOppostReason = getValue("oppost_reason");
            hCardOppostDate = getValue("oppost_date");
        }

        hCardSupFlag = hSupFlag;
        hAcnoFlag = "";

        hIsRc = "";
        sqlCmd = "select card_indicator,";
        sqlCmd += "line_of_credit_amt,";
        sqlCmd += "rc_use_indicator, ";
        sqlCmd += "acno_flag ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString(1, hPSeqno);
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno not found!", hPSeqno, comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCardIndicator = getValue("card_indicator");
            hCreditAmt = getValueDouble("line_of_credit_amt");
            hIsRc = getValue("rc_use_indicator");
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
        if (hCardIndicator.equals("2")) {
            hAssureFlag = "Y";
        } else {
            hAssetValue = 0;
            sqlCmd = "select asset_value,";
            sqlCmd += "card_since ";
            sqlCmd += " from crd_idno  ";
            sqlCmd += "where id_p_seqno = ? ";
            setString(1, hIdPSeqno);
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
                sqlCmd += "and length(rela_id) = 10  ";
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
        return;
    }

    /***********************************************************************/
    void getCrdChgId() throws Exception {
        hChgiCrtUser = "";
        hChgiAprUser = "";
        hChgiChiName = "";
        hChgiIdPSeqno = "";
        hChgiPostJcicFlag = "";
        hChgiKk2Flag = "";

        sqlCmd = "select chi_name,";
        sqlCmd += "crt_user,";
        sqlCmd += "apr_user,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "decode(post_jcic_flag,'','N',post_jcic_flag) h_chgi_post_jcic_flag ";
        sqlCmd += " from crd_chg_id  ";
        sqlCmd += "where old_id_p_seqno  = ?  ";
        setString(1, hJcrdIdPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgiChiName = getValue("chi_name");
            hChgiCrtUser = getValue("crt_user");
            hChgiAprUser = getValue("apr_user");
            hChgiIdPSeqno = getValue("id_p_seqno");
            hChgiPostJcicFlag = getValue("h_chgi_post_jcic_flag");
        }

        sqlCmd = "select decode(kk2_flag,'','N',kk2_flag) h_chgi_kk2_flag ";
        sqlCmd += " from crd_chg_id  ";
        sqlCmd += "where id_p_seqno  = ?  ";
        setString(1, hCardIdPSeqno);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgiKk2Flag = getValue("h_chgi_kk2_flag");
        }
    }

    /***********************************************************************/
    /***
     * 抓取JCIC之信用名稱及信用標章代號
     * 
     * @throws Exception
     */
    void convertCardId() throws Exception {
        String pCardNote = "";

        hCardName = "";
        hCardNote = "";
        pCardNote = "";
        sqlCmd = "select map_value ";
        sqlCmd += " from crd_message  ";
        sqlCmd += "where msg_type = 'JCIC_CARD'  ";
        sqlCmd += "and msg_value = ? ";
        setString(1, hJcrdCardType);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCardName = getValue("map_value");
        } else {
            if (hCardType.substring(0, 2).equals("NC") == false) {
                hCardName = hCardType;
            } else {
                hCardName = "V";
            }
        }
        if (hCardIndicator.equals("2"))
            hCardNote = "B";
        if (hCardIndicator.equals("1")) {
            hCardNote = "";
            sqlCmd = "select card_note_jcic ";
            sqlCmd += " from ptr_card_type  ";
            sqlCmd += "where card_type = ? ";
            setString(1, hCardType);
            recordCnt = selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_ptr_card_type not found!", hCardType, comcr.hCallBatchSeqno);
            }
            if (recordCnt > 0) {
                pCardNote = getValue("card_note_jcic");
            }

            hCardNote = pCardNote;
        }

        return;
    }

    /***********************************************************************/
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
            showLogMessage("I","",String.format("[%s] => error_code[%d] error\n", hEflgRefIpCode, errCode));
            showLogMessage("I","",String.format("[%s]檔案傳送JCIC有誤(error), 請通知相關人員處理\n", procCode));
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"%s執行完成 傳送JCIC失敗\"", prgmId);
//          comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", tojcicmsg);
        } else {
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"%s執行完成 傳送JCIC無誤\"", prgmId);
//          comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", tojcicmsg);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdF045 proc = new CrdF045();
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
            rtn += fixLeft(filler4, 257);
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

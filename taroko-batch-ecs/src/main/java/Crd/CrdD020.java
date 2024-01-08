/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/06/21  V1.01.01  Lai        Initial                                    *
* 108/12/04  V1.01.02  Rou        update crd_emboss()、crd_combo()            *
* 109/03/09  V1.01.03  Wilson     新增tsc_autoload_flag                       *
* 109/04/10  V1.01.04  Wilson     mbos_combo_indicator、mbos_online_mark條件修改*
* 109/04/13  V1.01.05  Wilson     insert crd_emboss新增欄位                                                         *
* 109/04/22  V1.01.06  Wilson     mail_branch為空值才用原值取代                                                       *    
* 109/12/21  V1.00.07   shiyuqi       updated for project coding standard   *
* 110/06/24  V1.00.08  Wilson     區分branch、mail_branch                      *
* 110/09/17  V1.00.09  Wilson     新增son_card_flag、indiv_crd_lmt             *
* 112/02/08  V1.00.10  Wilson     hNewBatchno = hChgBatchno時自動+1            *
* 112/02/09  V1.00.11  Wilson     hChgBatchno = hNewBatchno時自動+1            *
* 112/05/02  V1.00.12  Wilson     insert crd_meboss add service_code         *
* 112/06/27  V1.00.13  Wilson     移除mark electronic_code_old                *
* 112/11/27  V1.00.14  Wilson     增加處理凸字第四行                                                                                     *
* 112/11/30  V1.00.15  Wilson     讀取認同集團碼                                                                                             *
* 112/12/03  V1.00.16  Wilson     online_mark空白補0                                                                            *
*****************************************************************************/
package Crd;

import com.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD020 extends AccessDAO {
    private String progname = "接收非新製卡資料轉入送製卡處理    112/12/03  V1.00.16 ";
    private Map<String, Object> resultMap;

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    int debugD = 1;

    String checkHome = "";
    String hCallErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;

    String hBatchno = "";
    String hEmbossSource = "";
    String hEmbossReason = "";
    String hSuperId = "";
    String hTransId = "";
    String mbosRowid = "";
    String combRowid = "";

    String mbosToNcccCode = "";
    String mbosComboIndicator = "";
    String mbosOnlineMark = "";
    String mbosApplyId = "";
    String mbosApplyIdCode = "";
    String mbosPmId = "";
    String mbosPmIdCode = "";
    String mbosCardNo = "";
    String mbosValidFm = "";
    String mbosOldCardNo = "";
    String mbosBatchno = "";
    //long   mbos_recno = 0;
    String mbosSourceBatchno = "";
    long mbosSourceRecno = 0;
    String mbosEmbossSource = "";
    String mbosEmbossReason = "";
    String mbosNcccType = "";
    String mbosCardType = "";
    String mbosBinNo = "";
    String mbosAcctType = "";
    String mbosGroupCode = "";
    String mbosUnitCode = "";
    String mbosServiceId = "";
    String mbosAcctKey = "";
    String mbosSupFlag = "";
    String mbosIcFlag = "";
    String mbosBranch = "";
    String mbosMailBranch = "";
    String mbosElectronicCode = "";
    String mbosElectronicCodeOld = "";
    String mbosServiceCode = "";
    String mbosRemark20 = "";
    String mbosMnoId = "";
    String mbosMsisdn = "";
    String mbosSeId = "";
    String mbosServiceType = "";
    String mbosSirNo = "";
    String mbosServiceVer = "";
    String mbosBirthday = "";
    String mbosChiName = "";
    String mbosNation = "";
    String mbosSex = "";
    String mbosMarriage = "";
    String mbosBusinessCode = "";
    String mbosEducation = "";
    String mbosRelWithPm = "";
    String mbosCurrCode = "";
    String mbosDiffCode = "";
    String mbosActNo = "";
    String mbosEmboss4ThData = "";
    String mbosMailType = "";
    String mbosRejectCode = "";
    String mbosMailZip = "";
    String mbosMailAddr1 = "";
    String mbosMailAddr2 = "";
    String mbosMailAddr3 = "";
    String mbosMailAddr4 = "";
    String mbosMailAddr5 = "";
    String mbosReasonCode = "";
    String mbosCardRefNum = "";
    String mbosTscAutoloadFlag = "";  
    String mbosSonCardFlag = "";
    int mbosIndivCrdLmt = 0;
    String gcrdCardMoldFlag = "";
    String hAcnoPSeqno = "";
    String hIdnoIdPSeqno = "";
    String hTransType = "";
    String hRelation = "";
    String hBinNo = "";
    String hCurrCode = "";
    String hDcCurrCodeGl = "";
    String hAcurAutopayAcctNo = "";
    String hRtnStr = "";
    String hRtnDesc = "";
    String ccardSpecialCardRateFlag = "";
	String ccardSpecialCardRate = "";
	String ccardCardFeeDate = "";
	String ccardFlFlag = "";
	String ccardCardRefNum = "";
	String ccardVdBankNo = "";
	String ccardCrtBankNo = "";
	String ccardMailBranch = "";
    String hNcccBatchno = "";
    String hNewBatchno = "";
    String hChgBatchno = "";
    long hNcccRecno = 0;
    long hNewRecno = 0;
    long hChgRecno = 0;
    String hEriaRefIp = "";
    String hEriaPortNo = "";
    int rtnAppc = 0;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdD020 proc = new CrdD020();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            dateTime();

            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            checkHome = comc.getECSHOME();
            showLogMessage("I", "", javaProgram + " " + progname);
            
            if (args.length != 5 && args.length != 6 && args.length != 0) {
                String err1 = "CrdD020 00092701 1 1 shu trans_id [seq_no]\n";
                String err2 = "CrdD020 批號 製卡來源 製卡原因 super_id trans_id [seq_no]";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

//            String checkHome = comc.GetECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }
            comcr.hCallRProgramCode = this.getClass().getName();
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

            hSuperId = "batch";
            hTransId = "batch";
            if (args.length != 0) {
                hBatchno = args[0];
                hEmbossSource = args[1];
                hEmbossReason = args[2];
                if(hEmbossReason.equals("0")) hEmbossReason = ""; //callbatch無法判斷參數為空白
                hSuperId = args[3];
                hTransId = args[4];
            }

            showLogMessage("I", "", "批號=" + hBatchno + " 製卡來源=" + hEmbossSource + "," + hEmbossReason);

            selectPtrBusinday();

//          tmp_int = check_process(1, "CRD_D020");
            if (tmpInt != 0) {
                String err1 = "check_process 1 error !!";
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }

            totalCnt = 0;
            getNcccBatchno();
            getChgBatchno();
//            selectEcsRefIpAddr();

            selectCrdEmbossTmp();

//          tmp_int = check_process(1, "CRD_D020");

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        }

        catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }

    } // End of mainProcess
      // ************************************************************************

    public void selectPtrBusinday() throws Exception {
        selectSQL = "business_date   , " + "to_char(sysdate,'yyyymmdd')    as SYSTEM_DATE ";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("BUSINESS_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");
    }

    // ************************************************************************
    public void selectCrdEmbossTmp() throws Exception {

        selectSQL = "   a.batchno               " + " , a.recno                 " 
                  + " , a.credit_lmt            " + " , a.emboss_source         " 
                  + " , a.emboss_reason         " + " , a.to_nccc_code          "
                  + " , a.card_type             " + " , a.bin_no                " 
                  + " , a.acct_type             " + " , a.acct_key              " 
                  + " , decode(a.unit_code,'','0000',a.unit_code) as unit_code  "
                  + " , a.card_no               " + " , a.old_card_no           " 
                  + " , a.change_reason         " + " , a.status_code           " 
		          + " , a.reason_code           " 
                  + " , a.apply_id              "
                  + " , decode(a.apply_id_code,'','0',a.apply_id_code)  as apply_id_code " 
                  + " , a.pm_id                 "
                  + " , decode(a.pm_id_code   ,'','0',a.pm_id_code)     as pm_id_code " 
                  + " , a.major_card_no         " + " , a.major_valid_fm        " 
                  + " , a.major_valid_to        " + " , a.group_code            "
                  + " , a.source_code           " + " , a.channel_code          " 
                  + " , a.corp_no               " + " , a.corp_no_code          " 
                  + " , a.chi_name              " + " , a.eng_name              "
                  + " , a.birthday              " + " , a.business_code         " 
                  + " , a.valid_fm              " + " , a.valid_to              " 
                  + " , decode(a.mail_type,'','1',a.mail_type)  as mail_type    "
                  + " , a.emboss_4th_data       " + " , a.chg_addr_flag         " 
                  + " , a.vip                   " + " , a.reg_bank_no           " 
                  + " , a.risk_bank_no          " + " , a.pvv                   "
                  + " , a.cvv                   " + " , a.cvv2                  " 
                  + " , a.pvki                  " + " , a.pin_block             " 
                  + " , a.voice_passwd          " + " , a.old_beg_date          "
                  + " , a.old_end_date          " + " , a.fee_code              " 
                  + " , a.standard_fee          " + " , a.annual_fee            " 
                  + " , a.fee_reason_code       " + " , a.nccc_type             "
                  + " , a.ic_flag               " + " , a.branch                " 
                  + " , a.mail_attach1          " + " , a.mail_attach2          " 
                  + " , a.reissue_code          " + " , b.combo_indicator       "
                  + " , decode(a.curr_code,'','901',a.curr_code)  as curr_code  " 
                  + " , a.jcic_score            " + " , a.mail_branch           "
                  + " , c.card_mold_flag        "
                  + " , a.electronic_code       " 
        		  + " , a.CARD_REF_NUM          " 
        		  + " , a.tsc_autoload_flag     "
                  + " , a.son_card_flag         "
		          + " , a.indiv_crd_lmt         "
                  + " , a.electronic_code_old   "; 
//                  + " , a.remark_20             " 
//                  + " , a.rowid      as rowid   "
        daoTable = "ptr_group_card c, crd_emboss_tmp a, ptr_group_code b ";
        whereStr = "where a.batchno       like  ? " 
                 + "  and a.emboss_source like  ? "
                 + "  and a.emboss_reason like  ? " 
                 + "  and b.group_code    = a.group_code "
                 + "  and c.group_code    = a.group_code " 
                 + "  and c.card_type     = a.card_type "
                 + "  and a.emboss_date   = '' " 
                 + "  and a.nccc_batchno  = '' " 
                 + "  and a.card_no      <> '' "
                 + "  and a.confirm_date     <> '' " + " order by a.acct_type,a.card_type "
                 + "      ,decode(a.unit_code,'','0000',a.unit_code),a.card_no ";

        setString(1, hBatchno + "%");
        setString(2, hEmbossSource + "%");
        setString(3, hEmbossReason + "%");

        openCursor();

        while (fetchTable()) {
            initRtn();

            gcrdCardMoldFlag = getValue("card_mold_flag");
            mbosComboIndicator = getValue("combo_indicator");
            mbosSourceBatchno = getValue("batchno");
            mbosSourceRecno = getValueLong("recno");
            mbosEmbossReason = getValue("emboss_reason");
            mbosEmbossSource = getValue("emboss_source");
            mbosToNcccCode = getValue("to_nccc_code");
            mbosNcccType = getValue("nccc_type");
            mbosApplyId = getValue("apply_id");
            mbosApplyIdCode = getValue("apply_id_code");
            mbosPmId = getValue("pm_id");
            mbosPmIdCode = getValue("pm_id_code");
            mbosCardNo = getValue("card_no");
            mbosValidFm = getValue("valid_fm");
            mbosOldCardNo = getValue("old_card_no");
            mbosCardType = getValue("card_type");
            mbosBinNo = getValue("bin_no");
            mbosGroupCode = getValue("group_code");
            mbosUnitCode = getValue("unit_code");
            mbosOnlineMark = getValue("online_mark");
            mbosElectronicCode = getValue("electronic_code");
            mbosElectronicCodeOld = getValue("electronic_code_old");
            mbosRemark20 = getValue("remark_20");
            mbosCurrCode = getValue("curr_code");
            mbosMailType = getValue("mail_type");
            mbosBranch     = getValue("branch");
            mbosMailBranch = getValue("mail_branch");
            mbosReasonCode = getValue("reason_code");
            mbosCardRefNum = getValue("card_ref_num");
            mbosTscAutoloadFlag = getValue("tsc_autoload_flag");
            mbosAcctType = getValue("acct_type");
            mbosAcctKey = getValue("acct_key");  
            mbosSonCardFlag = getValue("son_card_flag");
            mbosIndivCrdLmt = getValueInt("indiv_crd_lmt");
            mbosEmboss4ThData = getValue("emboss_4th_data");
            
            if(mbosAcctKey.length() < 1)
               mbosAcctKey = getValue("apply_id") + getValue("apply_id_code");
            mbosRowid = getValue("rowid");

            //mbos_recno++;


            totalCnt++;
            processDisplay(5000); // every nnnnn display message
if(debug == 1) {
  showLogMessage("I", "", "  888 Card=[" + mbosCardNo + "]"+ mbosApplyId +","+ mbosApplyIdCode);
  showLogMessage("I", "", "  888 type=[" + mbosNcccType + "]");
 }

            getPtrGroupCardDtl();

            getCrdItemUnit();

            if (mbosNcccType.compareTo("2") == 0) {
                hChgRecno++;
                if(hChgBatchno.equals(hNewBatchno)) {
                	hChgBatchno = String.format("%08d", Integer.parseInt(hChgBatchno.trim()) + 1);
                }

                hNcccBatchno = hChgBatchno;
                hNcccRecno = hChgRecno;
       //       h_nccc_recno   = mbos_recno;
            } else {
                hNewRecno++;
                if(hNewBatchno.equals(hChgBatchno)) {
                	hNewBatchno = String.format("%08d", Integer.parseInt(hNewBatchno.trim()) + 1);
                }
                
                hNcccBatchno = hNewBatchno;
                /* add by for 92/07/17 for 補發卡 */
                mbosNcccType = "3";
                hNcccRecno = hNewRecno;
            }
if(debug == 1)
   showLogMessage("I", "", " 888 FINAL batno=[" + hNcccBatchno + "]"+ hNcccRecno +","+ mbosNcccType);

            getCrdCard();
            if (getValue("pm_id").trim().compareTo(getValue("apply_id")) != 0) {
                if(mbosAcctKey.length() < 1)
                   mbosAcctKey = getValue("pm_id") + getValue("pm_id_code");
                mbosRelWithPm = hRelation;
            }
            getIdnoData();
            getMailAddr();

            rtnAppc = 0;

            if (mbosCurrCode.compareTo("901") != 0) {
                tmpInt = selectPtrCurrcode();
//                tmp_int = call_appc();
            }

            /****************************************
             * 正卡combo卡申請第三軌資料
             ****************************************/
            if (mbosComboIndicator.compareTo("N") != 0) {
                mbosActNo = getValue("card.combo_acct_no");
                mbosDiffCode = "1";
            }
            /****************************************
             * 取得法人戶最新的凸字第四行
             ****************************************/
//            if (mbosAcctType.compareTo("02") == 0) {
//                getEmbossData();
//            }

            int rtn = insertCrdEmboss();
            if (rtn == 1) {
                /*
                 * rpt_cnt++; if(rpt_cnt == 1) print_header();
                 * fprintf(fptr1,"%-10.10s %s\n",mbo1_apply_id ,mbos_card_no);
                 */
                continue;
            }

            /**********************************************
             * 緊急卡/重送件不需申請第三軌
             **********************************************/
            tmpInt = 0;
 
            if (mbosComboIndicator.compareTo("N") != 0 && mbosOnlineMark.compareTo("Y") != 0)
                tmpInt = insertCrdCombo();
            
            if (tmpInt == 0)
                updateOldCard();

            if (tmpInt == 0)
                deleteCrdEmbossTmp();
            // lai curr
        }
    }

    // ************************************************************************
    public int getPtrGroupCardDtl() throws Exception {
        selectSQL = " unit_code ";
        daoTable  = "ptr_group_card_dtl";
        whereStr  = "WHERE group_code    =  ? " 
                  + "  and card_type     =  ? ";
        setString(1, mbosGroupCode);
        setString(2, mbosCardType);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_group_card_dtl  error!!=" + mbosCardType;
            comcr.errRtn(err1, mbosGroupCode, comcr.hCallBatchSeqno);
        }

        mbosUnitCode = getValue("unit_code");

        return (0);
    }

    // ************************************************************************
    public int getCrdItemUnit() throws Exception {
        selectSQL = " ic_flag ,service_id , electronic_code ,service_code ";
        daoTable  = "crd_item_unit";
        whereStr  = "WHERE unit_code     =  ? "; 
        setString(1, mbosUnitCode);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_item_unit  error!!=" + mbosCardType;
            comcr.errRtn(err1, mbosUnitCode, comcr.hCallBatchSeqno);
        }

        mbosIcFlag = getValue("ic_flag");
        mbosServiceId = getValue("service_id");
        mbosElectronicCode = getValue("electronic_code");
        mbosServiceCode = getValue("service_code");

        return (0);
    }

    // ************************************************************************
    public int getNcccBatchno() throws Exception {
        selectSQL = "max(batchno) as max_batchno ";
        daoTable = "crd_emboss ";
        whereStr = "WHERE batchno    like  ?  " 
                 + "  and nccc_type     = '3' " 
                 + "  and to_nccc_date  = ''  ";

        setString(1, sysDate.substring(2, 8) + "%");

        tmpInt = selectTable();

        hNewBatchno = getValue("max_batchno");
if(debug == 1)
   showLogMessage("I", "", " 887 get NEW bat=[" + hNewBatchno + "]" + hNewBatchno);
        if (hNewBatchno.length() > 0) {
            selectSQL = "max(recno)   as max_recno ";
            daoTable = "crd_emboss ";
            whereStr = "WHERE batchno       =  ?  ";

            setString(1, hNewBatchno);

            tmpInt = selectTable();

            hNewRecno = getValueLong("max_recno");

            return (0);
        }
showLogMessage("I", "", " 888 NEW get bat=["+ hNewBatchno + "]" + hNewBatchno + "%");

        /* data not found */
        selectSQL = "max(batchno) as max_batchno ";
        daoTable = "crd_emboss ";
        whereStr = "WHERE batchno    like  ?  ";

        setString(1, sysDate.substring(2, 8) + "%");

        tmpInt = selectTable();

        hNewBatchno = getValue("max_batchno");
if (debug == 1)
showLogMessage("I",""," 889 NEW get bat=["+ hNewBatchno + "]D="+sysDate.substring(2, 8) + "%");

        if (hNewBatchno.length() > 0) {
            hNewBatchno = String.format("%08d", Integer.parseInt(hNewBatchno.trim()) + 1);
        } else {
            hNewBatchno = sysDate.substring(2, 8) + "01";
        }

        if (debug == 1)
            showLogMessage("I", "", " 888 return bat=[" + hNewBatchno + "]");

        return (0);
    }

    // ************************************************************************
    public int getChgBatchno() throws Exception {
        selectSQL = "max(batchno) as max_batchno ";
        daoTable  = "crd_emboss ";
        whereStr  = "WHERE batchno    like  ?  " 
                  + "  and nccc_type     = '2' " 
                  + "  and to_nccc_date  = ''  ";

        setString(1, sysDate.substring(2, 8) + "%");

        tmpInt = selectTable();

        hChgBatchno = getValue("max_batchno");
if(debug == 1)
  showLogMessage("I", "", " 888 get CHG bat=[" + hChgBatchno + "]" + hChgBatchno);
        if (hChgBatchno.length() > 0) {
            selectSQL = "max(recno)   as recno ";
            daoTable = "crd_emboss ";
            whereStr = "WHERE batchno       =  ?  ";

            setString(1, hChgBatchno);

            tmpInt = selectTable();

            hChgRecno = getValueLong("recno");

            return (0);
        }
 
        /* data not found */
        selectSQL = "max(batchno) as max_batchno ";
        daoTable = "crd_emboss ";
        whereStr = "WHERE batchno    like  ?  ";

//      setString(1, h_chg_batchno);
        setString(1, sysDate.substring(2, 8) + "%");

        tmpInt = selectTable();

        hChgBatchno = getValue("max_batchno");
if(debug == 1) showLogMessage("I", "", " 888 111 CHG bat=[" + hChgBatchno + "]");

        if (hChgBatchno.length() > 0) {
            hChgBatchno = String.format("%08d", Integer.parseInt(hChgBatchno.trim()) + 1);
        } else {
            hChgBatchno = sysDate.substring(2, 8) + "01";
        }

if(debug == 1)
   showLogMessage("I", "", " 888 return CHG bat=[" + hChgBatchno + "]");

        return (0);
    }

    // ************************************************************************
    public int getCrdCard() throws Exception {   	
    	
        extendField = "card.";
        selectSQL = "major_relation, combo_acct_no, bin_no, special_card_rate_flag, special_card_rate, "
        		  + "card_fee_date , fl_flag   , card_ref_num,  vd_bank_no,  crt_bank_no, mail_branch,"
                  + "decode(curr_code, '', '901', curr_code)  as curr_code ";
        daoTable  = "crd_card ";
        whereStr  = "where card_no    = ?  ";

        setString(1, mbosOldCardNo);

        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	ccardSpecialCardRateFlag = getValue("card.special_card_rate_flag");
        	ccardSpecialCardRate = getValue("card.special_card_rate");
        	ccardCardFeeDate = getValue("card.card_fee_date");
        	ccardFlFlag = getValue("card.fl_flag");
        	ccardCardRefNum = getValue("card.card_ref_num");
        	ccardVdBankNo = getValue("card.vd_bank_no");
        	ccardCrtBankNo = getValue("card.crt_bank_no");
        	ccardMailBranch = getValue("card.mail_branch");
        	
        }

        if (notFound.equals("Y")) {
            String err1 = "select_crd_card        error !!" + mbosOldCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hRelation = getValue("card.major_relation");
        hCurrCode = getValue("card.curr_code");
        hBinNo = getValue("card.bin_no");
        if (mbosBinNo.length() < 1)
            mbosBinNo = hBinNo;

        if (debug == 1)
            showLogMessage("I", "", " Type =[" + hCurrCode + "]id_code="+ mbosApplyIdCode);
        if (gcrdCardMoldFlag.trim().compareTo("M") == 0) {
            extendField = "mcrd.";
            selectSQL   = "mno_id       , msisdn  , se_id, " 
                        + "service_type , sir_no    ";
            daoTable    = "mob_card ";
            whereStr    = "where card_no       = ?  " 
                        + "  and new_beg_date in ( select max(new_beg_date) "
                        + "                          from mob_card       "
                        + "                         where card_no  = ? ) " 
                        + "fetch first 1 row  only ";

            setString(1, mbosOldCardNo);
            setString(2, mbosOldCardNo);

            tmpInt = selectTable();

            if (notFound.equals("Y")) {
                String err1 = "select_mob_card        error !!" + mbosOldCardNo;
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }

            mbosMnoId = getValue("mcrd.mno_id");
            mbosMsisdn = getValue("mcrd.msisdn");
            mbosSeId = getValue("mcrd.se_id");
            mbosServiceType = getValue("mcrd.service_type");
            mbosSirNo = getValue("mcrd.sir_no");

            getPtrServiceVer();
        }

        return (0);
    }

    // ************************************************************************
    public int getIdnoData() throws Exception {
        extendField = "idno.";
        selectSQL = "birthday  ,chi_name      , nation    ,    sex, "
                  + "marriage  ,business_code , education , id_p_seqno ";
        daoTable  = "crd_idno       ";
        whereStr  = "where id_no        = ?  " + "  and id_no_code   = ?  ";

        setString(1, mbosApplyId);
        setString(2, mbosApplyIdCode);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_idno        error!" + mbosApplyId +","+ mbosApplyIdCode;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        mbosBirthday = getValue("idno.birthday");
        mbosChiName = getValue("idno.chi_name");
        mbosNation = getValue("idno.nation");
        mbosSex = getValue("idno.sex");
        mbosMarriage = getValue("idno.marriage");
        mbosBusinessCode = getValue("idno.business_code");
        mbosEducation = getValue("idno.education");
        hIdnoIdPSeqno = getValue("idno.id_p_seqno");

        if (debug == 1)
            showLogMessage("I", "", " idno =[" + mbosBirthday + "]"+ mbosApplyIdCode);

        return (0);
    }

    // ************************************************************************
    public int getPtrServiceVer() throws Exception {
        selectSQL = "service_ver       ";
        daoTable  = "ptr_service_ver a, ptr_bintable b ";
        whereStr  = "where a.bin_type   = b.bin_type " 
                  + "  and b.bin_no     = ?          " 
                  + "FETCH FIRST 1 ROW ONLY";

        setString(1, mbosBinNo);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_service_ver error!" + mbosCardType;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        mbosServiceVer = getValue("service_ver");

        if (debug == 1)
            showLogMessage("I", "", " Type =[" + mbosCardType + "]=" + mbosServiceVer);

        return (0);
    }

    // ************************************************************************
    public int getMailAddr() throws Exception {
        extendField = "acno.";
        selectSQL = "acno_p_seqno            , bill_sending_zip  , " 
                  + "bill_sending_addr1 , bill_sending_addr2, "
                  + "bill_sending_addr3 , bill_sending_addr4, " + "bill_sending_addr5 ";
        daoTable  = "act_acno       ";
        whereStr  = "where acct_type    = ?  " + "  and acct_key     = ?  ";

        setString(1, mbosAcctType);
        setString(2, mbosAcctKey);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_acno        error!" + mbosAcctKey +","+ mbosAcctType;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hAcnoPSeqno = getValue("acno.acno_p_seqno");
        mbosMailZip = getValue("acno.mail_zip");
        mbosMailAddr1 = getValue("acno.mail_addr1");
        mbosMailAddr2 = getValue("acno.mail_addr2");
        mbosMailAddr3 = getValue("acno.mail_addr3");
        mbosMailAddr4 = getValue("acno.mail_addr4");
        mbosMailAddr5 = getValue("acno.mail_addr5");

        if (debug == 1)
            showLogMessage("I", "", " idno =[" + mbosBirthday + "]=");

        return (0);
    }

    // ************************************************************************
//    public int selectEcsRefIpAddr() throws Exception {
//
//        selectSQL = "ref_ip, port_no ";
//        daoTable  = "ecs_ref_ip_addr ";
//        whereStr  = "where ref_ip_code = 'APPC' ";
//
//        int recordCnt = selectTable();
//
//        if (notFound.equals("Y")) {
//            String err1 = "select_ecs_ref_ip_addr error !!" + "=APPC";
//            String err2 = "";
//            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
//        }
//
//        hEriaRefIp = getValue("ref_ip");
//        hEriaPortNo = getValue("port_no");
//
//        if (debug == 1)
//            showLogMessage("I", "", " idno =[" + mbosBirthday + "]=");
//
//        return (0);
//    }

    // ************************************************************************
    public int selectPtrCurrcode() throws Exception {

        selectSQL = "curr_code_gl    ";
        daoTable  = "ptr_currcode    ";
        whereStr  = "where curr_code  = ? ";

        setString(1, mbosCurrCode);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_currcode    error !!" + mbosCurrCode;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hDcCurrCodeGl = getValue("curr_code_gl");

        if (debug == 1)
            showLogMessage("I", "", " idno =[" + hDcCurrCodeGl + "]=");

        return (0);
    }

    // ************************************************************************
    public int getActAcctCurr() throws Exception {

        selectSQL = "autopay_acct_no ";
        daoTable  = "act_acct_curr   ";
        whereStr  = "where p_seqno   =  ? " + "  and curr_code =  ? ";

        setString(1, hAcnoPSeqno);
        setString(2, mbosCurrCode);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_act_acct_curr error !!" + hAcnoPSeqno + " " + mbosCurrCode;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hAcurAutopayAcctNo = getValue("autopay_acct_no");

        if (debug == 1)
            showLogMessage("I", "", " idno =[" + hDcCurrCodeGl + "]=");

        return (0);
    }

    // ************************************************************************
//    public int getEmbossData() throws Exception {
//
//        selectSQL = "emboss_data     ";
//        daoTable  = "crd_corp        ";
//        whereStr  = "where corp_no   =  ? ";
//
//        setString(1, getValue("corp_no"));
//
//        int recordCnt = selectTable();
//
//        if (notFound.equals("Y")) {
//            String err1 = "select_crd_corp      error !!" + getValue("corp_no");
//            String err2 = "";
//            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
//        }
//
//        mbosEmboss4ThData = getValue("emboss_data");
//
//        if (debug == 1)
//            showLogMessage("I", "", " corp =[" + mbosEmboss4ThData + "]");
//
//        return (0);
//    }

    // ************************************************************************
//    public int callAppc() throws Exception {
//
//        getActAcctCurr();
//
//        if (getValue("corp_no").length() < 1)
//            tmpChar = getValue("pm_id");
//        else
//            tmpChar = getValue("corp_no");
//
//        String sndbuf = "";
//        sndbuf = comcr.insertStr(sndbuf, "PB37", 0);
//        sndbuf = comcr.insertStr(sndbuf, String.format("%-2s"  , " "), 4);
//        sndbuf = comcr.insertStr(sndbuf, String.format("%1s"   , "C"), 6);
//        sndbuf = comcr.insertStr(sndbuf, String.format("%-10s" , tmpChar), 7);
//        sndbuf = comcr.insertStr(sndbuf, String.format("%-11s" , hAcurAutopayAcctNo), 17);
//        sndbuf = comcr.insertStr(sndbuf, String.format("%-2s"  , hDcCurrCodeGl), 28);
//        sndbuf = comcr.insertStr(sndbuf, String.format("%-16s" , mbosCardNo), 30);
//        sndbuf = comcr.insertStr(sndbuf, String.format("%-10s" , hAcnoPSeqno), 46);
//        sndbuf = comcr.insertStr(sndbuf, String.format("%-60s" , " "), 56);
//
//        String rcvbuf = comm.commAPPC(sndbuf, hEriaRefIp, Integer.parseInt(hEriaPortNo)
//                                                            , 1024);
//
//        hRtnStr = comc.getSubString(rcvbuf, 4, 6);
//        hRtnDesc = comc.getSubString(rcvbuf, 56, 116);
//        if (hRtnStr.compareTo("  ") == 0)
//            hRtnStr = "00";
//        if (debug == 1)
//            showLogMessage("I", "", " rcv=[" + hRtnStr + "]=");
//
//        insertActChkno();
//        if (rcvbuf.length() == 0 || hRtnStr.compareTo("00") != 0) {
//            return (1);
//        }
//
//        return (0);
//    }

    // ************************************************************************
    public int insertActChkno() throws Exception {

        setValue("p_seqno"           , javaProgram);
        setValue("acct_type"         , mbosAcctType);
//      setValue("acct_key"          , mbos_acct_key);
        setValue("appl_no"           , getValue("apply_no"));
        setValue("id_p_seqno"        , hIdnoIdPSeqno);
//      setValue("id"                , mbos_apply_id);
//      setValue("id_code"           , mbos_apply_id_code);
        setValue("autopay_acct_bank" , "017");
        setValue("autopay_acct_no"   , hAcurAutopayAcctNo);
        setValue("card_no"           , mbosCardNo);
        setValue("valid_flag"        , "Z");
        setValue("from_mark"         , "1");
        setValue("verify_flag"       , "Z");
        setValue("verify_date"       , sysDate);
        setValue("verify_return_code", hRtnStr);
        setValue("exec_check_flag"   , "N");
        setValue("exec_check_date"   , sysDate);
        setValue("ibm_check_flag"    , "Y");
        setValue("ibm_check_date"    , sysDate);
        setValue("ibm_return_code"   , hRtnStr);
        setValue("autopay_id_p_seqno", hIdnoIdPSeqno);
        setValue("autopay_id"        , mbosApplyId);
        setValue("autopay_id_code"   , mbosApplyIdCode);
        setValue("proc_mark"         , "Y");
        setValue("crt_date"          , sysDate);
        setValue("crt_user"          , javaProgram);
        setValue("crt_time"          , sysDate + sysTime);
        setValue("ach_check_flag"    , "Z");
        setValue("ach_send_date"     , sysDate);
        setValue("ach_rtn_date"      , sysDate);
        setValue("old_acct_bank"     , "");
        setValue("old_acct_no"       , "");
        setValue("old_acct_id"       , "");
        setValue("curr_code"         , mbosCurrCode);
        setValue("batchno"           , mbosBatchno);
        setValue("resp_desc"         , hRtnDesc);
        setValue("mod_time"          , sysDate + sysTime);
        setValue("mod_pgm"           , javaProgram);
      /*
      * setValue("autopay_dc_flag"      , h_autopay_dc_flag);
      * setValue("autopay_dc_indicator" , h_autopay_dc_indicator);
      */

        daoTable = "act_chkno ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_act_chkno     error[dupRecord]=" + mbosAcctKey;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int insertCrdEmboss() throws Exception {

        /*
         * lai test gcrd_card_mold_flag = "M";
         */

if(debug == 1)
   showLogMessage("I",""," *** INSERT MOLD=["+ gcrdCardMoldFlag +"]"
                            + hNcccBatchno +","+ hNcccRecno);

        setValue("org_emboss_data", getValue("emboss_4th_data"));
        setValueInt("auth_credit_lmt", getValueInt("credit_lmt"));
        /*****************************************************
         * 重製之緊急補發卡,online_mark='3'(2001/10/07)
         *****************************************************/
        if(mbosOnlineMark.length() == 0) {
        	mbosOnlineMark = "0";
        }
               
//        if (mbos_emboss_source.compareTo("5") == 0 && mbos_reason_code.compareTo("3") == 0)
//            mbos_online_mark = "2";
        setValue("online_mark", mbosOnlineMark);
        if (mbosEmbossSource.compareTo("7") == 0)
            mbosMailType = "2";

        if (mbosPmId.compareTo(mbosApplyId) == 0)
            mbosSupFlag = "0";
        else
            mbosSupFlag = "1";

        /**********************************************************
         * 效期起日小於系統月份,以系統月份一日為主,overwrite crd_emboss.valid_fm (2002/02/19)
         **********************************************************/
        selectSQL = " 1 as val_mon   ";
        daoTable = "dual            ";
        whereStr = "where to_char(sysdate,'yyyymm') > ? ";

        setString(1, comc.getSubString(getValue("valid_fm"),0, 6));

        int recordCnt = selectTable();

        /**** sysdate > valid_fm *****/
        if (getValueInt("val_mon") > 0)
            mbosValidFm = sysDate.substring(0, 6) + "01";

        selectSQL = " count(*) as old_cnt   ";
        daoTable  = "crd_emboss      ";
        whereStr  = "where card_no        = ? " 
                  + "  and rtn_nccc_date  = '' " 
                  + "  and reject_code    = '' ";

        setString(1, mbosOldCardNo);

        if (debug == 1)
            showLogMessage("I", "", " OLD NO=[" + mbosOldCardNo + "]");
        recordCnt = selectTable();

        /**** sysdate > valid_fm *****/
        if (getValueInt("old_cnt") > 0)
            return (1);

        if (rtnAppc > 0)
            mbosRejectCode = "98";
        /*
         * if((getValue("mail_addr1").length == 0) &&
         * (mbos_mail_type.compareTo("0") == 0 || mbos_mail_type.compareTo("1")
         * == 0 || mbos_mail_type.compareTo("2") == 0) ) {get_mail_addr();}
         */
        if (mbosMailType.compareTo("3") == 0 || mbosMailType.compareTo("4") == 0) {
            mbosMailZip = "";
            mbosMailAddr1 = "";
            mbosMailAddr2 = "";
            mbosMailAddr3 = "";
            mbosMailAddr4 = "";
            mbosMailAddr5 = "";
        }

        setValue("batchno"           , hNcccBatchno);
        setValueLong("recno"         , hNcccRecno);
        setValue("emboss_source"     , mbosEmbossSource);
        setValue("emboss_reason"     , mbosEmbossReason);
        setValue("source_batchno"    , mbosSourceBatchno);
        setValueLong("source_recno"  , mbosSourceRecno);
        setValueInt("seqno"          , 0);
        setValue("aps_batchno"       , getValue("aps_batchno"));
        setValueInt("aps_recno"      , getValueInt("aps_recno"));
        setValueInt("credit_lmt"     , getValueInt("credit_lmt"));
        setValueInt("standard_fee"   , getValueInt("standard_fee"));
        setValueInt("annual_fee"     , getValueInt("annual_fee"));
        setValueInt("auth_credit_lmt", getValueInt("credit_lmt"));
        setValue("to_nccc_code"      , mbosToNcccCode);
        setValue("nccc_type"         , mbosNcccType);
        setValue("card_type"         , mbosCardType);
        setValue("bin_no"         , mbosBinNo);
        setValue("acct_type"      , mbosAcctType);
        setValue("acct_key"       , mbosAcctKey);
        setValue("unit_code"      , mbosUnitCode);
        setValue("service_id"     , mbosServiceId);
        setValue("card_no"        , mbosCardNo);
        setValue("valid_fm"       , mbosValidFm);
        setValue("valid_to"       , getValue("valid_to"));
        setValue("major_card_no"  , getValue("major_card_no"));
        setValue("major_valid_fm" , getValue("major_valid_fm"));
        setValue("major_valid_to" , getValue("major_valid_to"));
        setValue("old_card_no"    , mbosOldCardNo);
        setValue("change_reason"  , getValue("change_reason"));
        setValue("status_code"    , getValue("status_code"));
        setValue("reason_code"    , getValue("reason_code"));
        setValue("apply_id"       , mbosApplyId);
        setValue("apply_id_code"  , mbosApplyIdCode);
        setValue("pm_id"          , mbosPmId);
        setValue("pm_id_code"     , mbosPmIdCode);
        setValue("group_code"     , mbosGroupCode);
        setValue("source_code"    , getValue("source_code"));
        setValue("channel_code"   , getValue("channel_code"));
        setValue("corp_no"        , getValue("corp_no"));
        setValue("chi_name"       , mbosChiName);
        setValue("eng_name"       , getValue("eng_name"));
        setValue("birthday"       , mbosBirthday);
        setValue("act_no"         , mbosActNo);
        setValue("sup_flag"       , mbosSupFlag);
        setValue("ic_flag"        , mbosIcFlag); 
        setValue("electronic_code", mbosElectronicCode);
        setValue("electronic_code_old", mbosElectronicCodeOld);
        setValue("remark_20"      , mbosRemark20);
        setValue("mail_zip"       , mbosMailZip);
        setValue("mail_addr1"     , mbosMailAddr1);
        setValue("mail_addr2"     , mbosMailAddr2);
        setValue("mail_addr3"     , mbosMailAddr3);
        setValue("mail_addr4"     , mbosMailAddr4);
        setValue("mail_addr5"     , mbosMailAddr5);
        if(mbosComboIndicator.equals("N")) {
        	setValue("mail_type"      , mbosMailType);
        }else {
        	setValue("mail_type"      , "4");
        	
        	if(mbosMailBranch.length() == 0)
        		mbosMailBranch = ccardMailBranch;          
        }
        setValue("mail_branch"    , mbosMailBranch);
        setValue("business_code"  , mbosBusinessCode);
        setValue("sex"            , mbosSex);
        setValue("marriage"       , mbosMarriage);
        setValue("rel_with_pm"    , mbosRelWithPm);
        setValue("education"      , mbosEducation);
        setValue("nation"         , mbosNation);
        setValue("mno_id"         , mbosMnoId);
        setValue("msisdn"         , mbosMsisdn);
        setValue("service_id"     , getValue("service_id"));
        setValue("to_vendor_date" , "29991231");
        setValue("se_id"          , mbosSeId);
        setValue("service_ver"    , mbosServiceVer);
        setValue("service_type"   , mbosServiceType);
        setValue("sir_no"         , mbosSirNo);
        setValue("combo_indicator", mbosComboIndicator);
        setValue("curr_code"      , mbosCurrCode);
        setValue("diff_code"      , mbosDiffCode);
        setValue("reject_code"    , mbosRejectCode);
        setValue("emboss_4th_data", mbosEmboss4ThData);
        setValue("crt_date"       , sysDate);
        setValue("apr_date"       , sysDate);
        setValue("mod_time"       , sysDate + sysTime);
        setValue("mod_pgm"        , javaProgram);
        setValue("branch"         , mbosBranch);
        setValue("special_card_rate_flag"	, ccardSpecialCardRateFlag);
        setValue("special_card_rate"        , ccardSpecialCardRate);
        setValue("card_fee_date"   			, ccardCardFeeDate);
        setValue("fl_flag"  			    , ccardFlFlag);
        setValue("card_ref_num"   			, ccardCardRefNum);
        setValue("tsc_autoload_flag"   	    , mbosTscAutoloadFlag);
        setValue("vd_bank_no"   	        , ccardVdBankNo);
        setValue("crt_bank_no"   	        , ccardCrtBankNo);
        setValue("son_card_flag"            , mbosSonCardFlag);
        setValueInt("indiv_crd_lmt"         , mbosIndivCrdLmt);
        setValue("service_code"             , mbosServiceCode);
       
        /*
         * no setValueInt("service_year" , mbos_service_year);
         * setValueDouble("value" , mbos_value); setValueDouble("salary" ,
         * mbos_salary); setValue("corp_act_flag" , mbos_corp_act_flag);
         * setValue("corp_assure_flag" , mbos_corp_assure_flag);
         * setValue("reg_bank_no" , mbos_reg_bank_no); setValue("risk_bank_no" ,
         * mbos_risk_bank_no); setValue("org_risk_bank_no" ,
         * mbos_org_risk_bank_no); setValue("resident_zip" , mbos_resident_zip);
         * setValue("resident_addr1" , mbos_resident_addr1);
         * setValue("resident_addr2" , mbos_resident_addr2);
         * setValue("resident_addr3" , mbos_resident_addr3);
         * setValue("resident_addr4" , mbos_resident_addr4);
         * setValue("resident_addr5" , mbos_resident_addr5);
         * setValue("company_name" , mbos_company_name); setValue("cellar_phone"
         * , mbos_cellar_phone); setValue("vip" , mbos_vip);
         * setValue("job_position" , mbos_job_position);
         * setValue("home_area_code1" , mbos_home_area_code1);
         * setValue("home_tel_no1" , mbos_home_tel_no1);
         * setValue("home_tel_ext1" , mbos_home_tel_ext1);
         * setValue("home_area_code2" , mbos_home_area_code2);
         * setValue("home_tel_no2" , mbos_home_tel_no2);
         * setValue("home_tel_ext2" , mbos_home_tel_ext2);
         * setValue("office_area_code1" , mbos_office_area_code1);
         * setValue("office_tel_no1" , mbos_office_tel_no1);
         * setValue("office_tel_ext1" , mbos_office_tel_ext1);
         * setValue("office_area_code2" , mbos_office_area_code2);
         * setValue("office_tel_no2" , mbos_office_tel_no2);
         * setValue("office_tel_ext2" , mbos_office_tel_ext2);
         * setValue("e_mail_addr" , mbos_e_mail_addr); setValue("class_code" ,
         * mbos_class_code); setValue("fee_code" , mbos_fee_code);
         * setValue("introduce_no" , mbos_introduce_no); setValue("accept_dm" ,
         * mbos_accept_dm); setValue("apply_no" , mbos_apply_no);
         * setValue("mail_no" , mbos_mail_no); setValue("mail_branch" ,
         * mbos_mail_branch); setValue("mail_proc_date" , mbos_mail_proc_date);
         * setValue("introduce_id" , mbos_introduce_id);
         * setValue("introduce_name" , mbos_introduce_name);
         * setValue("salary_code" , mbos_salary_code); setValue("student" ,
         * mbos_student); setValue("apply_id_ecode" , mbos_apply_id_ecode);
         * setValue("corp_no_ecode" , mbos_corp_no_ecode);
         * setValue("pm_id_ecode" , mbos_pm_id_ecode); setValue("police_no1" ,
         * mbos_police_no1); setValue("police_no2" , mbos_police_no2);
         * setValue("police_no3" , mbos_police_no3); setValue("pm_cash" ,
         * mbos_pm_cash); setValue("sup_cash" , mbos_sup_cash);
         * setValue("online_mark" , mbos_online_mark); setValue("error_code" ,
         * mbos_error_code); setValue("member_id" , mbos_member_id);
         * setValue("stmt_cycle" , mbos_stmt_cycle); setValue("credit_flag" ,
         * mbos_credit_flag); setValue("comm_flag" , mbos_comm_flag);
         * setValue("resident_no" , mbos_resident_no);
         * setValue("other_cntry_code" , mbos_other_cntry_code);
         * setValue("passport_no" , mbos_passport_no); setValue("staff_flag" ,
         * mbos_staff_flag); setValue("pm_birthday" , mbos_pm_birthday);
         * setValue("sup_birthday" , mbos_sup_birthday);
         * setValue("final_fee_code" , mbos_final_fee_code);
         * setValue("fee_reason_code" , mbos_fee_reason_code);
         * setValue("chg_addr_flag" , mbos_chg_addr_flag); setValue("pin_block"
         * , mbos_pin_block); setValue("pvv" , mbos_pvv); setValue("cvv" ,
         * mbos_cvv); setValue("cvv2" , mbos_cvv2); setValue("pvki" ,
         * mbos_pvki); setValue("open_passwd" , mbos_open_passwd);
         * setValue("voice_passwd" , mbos_voice_passwd); setValue("cht_passwd" ,
         * mbos_cht_passwd); setValue("cht_date" , mbos_cht_date);
         * setValue("service_code" , mbos_service_code);
         * setValue("cntl_area_code" , mbos_cntl_area_code); setValue("stock_no"
         * , mbos_stock_no); setValue("old_beg_date" , mbos_old_beg_date);
         * setValue("old_end_date" , mbos_old_end_date); setValue("nccc_type" ,
         * mbos_nccc_type); setValue("to_nccc_date" , mbos_to_nccc_date);
         * setValue("nccc_filename" , mbos_nccc_filename);
         * setValue("rtn_nccc_date" , mbos_rtn_nccc_date);
         * setValue("emboss_result" , mbos_emboss_result);
         * setValue("credit_error" , mbos_credit_error);
         * setValueInt("main_credit_lmt" , mbos_main_credit_lmt);
         * setValue("fail_proc_code" , mbos_fail_proc_code);
         * setValue("complete_code" , mbos_complete_code); setValue("mail_code"
         * , mbos_mail_code); setValue("fee_error_code" , mbos_fee_error_code);
         * setValue("fee_date" , mbos_fee_date); setValue("in_main_date" ,
         * mbos_in_main_date); setValue("in_main_error" , mbos_in_main_error);
         * setValue("in_main_msg" , mbos_in_main_msg); setValue("in_other_date"
         * , mbos_in_other_date); setValue("in_other_error" ,
         * mbos_in_other_error); setValue("in_other_msg" , mbos_in_other_msg);
         * setValue("in_auth_date" , mbos_in_auth_date);
         * setValue("in_auth_error" , mbos_in_auth_error);
         * setValue("in_auth_msg" , mbos_in_auth_msg); setValue("star_date" ,
         * mbos_star_date); setValue("prn_pin_date" , mbos_prn_pin_date);
         * setValue("prn_post_date" , mbos_prn_post_date);
         * setValue("prn_mailer_date" , mbos_prn_mailer_date);
         * setValue("prn_cardno_date" , mbos_prn_cardno_date);         
         * setValueInt("org_indiv_crd_lmt" , mbos_org_indiv_crd_lmt);
         * setValue("nccc_oth_date" , mbos_nccc_oth_date);
         * setValue("to_ibm_date" , mbos_to_ibm_date); setValue("warehouse_date"
         * , mbos_warehouse_date); setValue("org_emboss_data" ,
         * mbos_org_emboss_data); setValueInt("org_cash_lmt" ,
         * mbos_org_cash_lmt); setValueInt("cash_lmt" , mbos_cash_lmt);
         * setValue("ic_flag" , mbos_ic_flag); setValue("branch" , mbos_branch);
         * setValue("mail_attach1" , mbos_mail_attach1); setValue("mail_attach2"
         * , mbos_mail_attach2); setValue("ic_indicator" , mbos_ic_indicator);
         * setValue("ic_cvv" , mbos_ic_cvv); setValue("ic_pin" , mbos_ic_pin);
         * setValue("deriv_key" , mbos_deriv_key); setValueInt("l_offln_lmt" ,
         * mbos_l_offln_lmt); setValueInt("u_offln_lmt" , mbos_u_offln_lmt);
         * setValue("bank_actno" , mbos_bank_actno); setValue("key_type" ,
         * mbos_key_type); setValue("mail_seqno" , mbos_mail_seqno);
         * setValue("vendor" , mbos_vendor); setValue("filename" ,
         * mbos_filename); setValue("csc" , mbos_csc); setValue("chk_nccc_flag"
         * , mbos_chk_nccc_flag); setValue("barcode_num" , mbos_barcode_num);
         * setValue("fh_over_flag" , mbos_fh_over_flag); setValue("fh_flag" ,
         * mbos_fh_flag); setValue("barcode_numps" , mbos_barcode_numps);
         * setValueInt("batchno_seqno" , mbos_batchno_seqno);
         * setValue("to_ic_date" , mbos_to_ic_date); setValue("trans_cvv2" ,
         * mbos_trans_cvv2); setValue("contactor1_name" , mbos_contactor1_name);
         * setValue("contactor1_relation" , mbos_contactor1_relation);
         * setValue("contactor1_area_code" , mbos_contactor1_area_code);
         * setValue("contactor1_tel" , mbos_contactor1_tel);
         * setValue("contactor1_ext" , mbos_contactor1_ext);
         * setValue("contactor2_name" , mbos_contactor2_name);
         * setValue("contactor2_relation" , mbos_contactor2_relation);
         * setValue("contactor2_area_code" , mbos_contactor2_area_code);
         * setValue("contactor2_tel" , mbos_contactor2_tel);
         * setValue("contactor2_ext" , mbos_contactor2_ext);
         * setValue("est_graduate_month" , mbos_est_graduate_month);
         * setValue("market_agree_base" , mbos_market_agree_base);
         * setValue("vacation_code" , mbos_vacation_code);
         * setValue("market_agree_act" , mbos_market_agree_act);
         * setValue("fancy_limit_flag" , mbos_fancy_limit_flag);
         * setValue("crt_user" , mbos_crt_user); setValue("reissue_code" ,
         * mbos_reissue_code); setValue("to_vendor_date" , mbos_to_vendor_date);
         * setValue("snd_acs_flag" , mbos_snd_acs_flag);
         * setValue("proc_acs_date" , mbos_proc_acs_date);
         * setValue("acs_error_code" , mbos_acs_error_code);
         * setValue("stat_send_internet" , mbos_stat_send_internet);
         * setValue("dc_indicator" , mbos_dc_indicator); setValue("act_no_f" ,
         * mbos_act_no_f); setValue("act_no_l" , mbos_act_no_l);
         * setValue("act_no_f_ind" , mbos_act_no_f_ind); setValue("agree_l_ind"
         * , mbos_agree_l_ind); setValue("act_no_l_ind" , mbos_act_no_l_ind);
         * setValue("send_pwd_flag" , mbos_send_pwd_flag);
         * setValueDouble("jcic_score" , mbos_jcic_score);
         * setValue("activation_code" , mbos_activation_code);
         * setValue("track2_dek" , mbos_track2_dek); setValue("pin_mobile" ,
         * mbos_pin_mobile); setValue("twmp_flag" , mbos_twmp_flag);
         * setValue("crt_user_mail" , mbos_crt_user_mail);
         * setValue("apr_date_mail" , mbos_apr_date_mail);
         * setValue("apr_user_mail" , mbos_apr_user_mail);
         * setValue("mail_send_date" , mbos_mail_send_date);
         * setValue("mail_method" , mbos_mail_method); setValue("remark_mail" ,
         * mbos_remark_mail); setValue("last_mail_zip" , mbos_last_mail_zip);
         * setValue("last_mail_addr1" , mbos_last_mail_addr1);
         * setValue("last_mail_addr2" , mbos_last_mail_addr2);
         * setValue("last_mail_addr3" , mbos_last_mail_addr3);
         * setValue("last_mail_addr4" , mbos_last_mail_addr4);
         * setValue("last_mail_addr5" , mbos_last_mail_addr5);
         * setValueDouble("sms_amt" , mbos_sms_amt); lai
         */

        daoTable = "crd_emboss";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_emboss    error[dupRecord] ";
            String err2 = hNcccBatchno + ","+ hNcccRecno;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
        return 0;

    }
// ************************************************************************
public int updateCrdCombo() throws Exception 
{
if(debug == 1) showLogMessage("I", "", " update crd_combo=[" + "" + "]");

  daoTable = "crd_combo";     
  updateSQL  = "  apply_date    = to_char(sysdate,'yyyymmdd'), ";
  updateSQL += "  birthday      = ?, ";
  updateSQL += "  batchno       = ?, ";
  updateSQL += "  recno         = ?, ";
  updateSQL += "  trans_type    = ?, ";
  updateSQL += "  old_card_no   = ?, ";
  updateSQL += "  saving_actno  = ?, ";
  updateSQL += "  third_data    = '', ";
  updateSQL += "  ic_pin        = '', ";
  updateSQL += "  to_ibm_date   = ?, ";
  updateSQL += "  rtn_ibm_date  = ?, ";
  updateSQL += "  rtn_code      = ?, ";
  updateSQL += "  to_nccc_date  = '', ";
  updateSQL += "  rtn_nccc_date = '', ";
  updateSQL += "  reject_code   = '', ";
  updateSQL += "  fail_proc_code= '', ";
  updateSQL += "  fail_proc_date= '', ";
  updateSQL += "  emboss_code   = '', ";
  updateSQL += "  emboss_date   = '', ";
  updateSQL += "  end_ibm_date  = '', ";
  updateSQL += "  end_rtn_code  = '', ";
  updateSQL += "  end_rtn_date  = '', ";
  updateSQL += "  send_prn_date = '', ";
  updateSQL += "  bank_actno    = '', ";
  updateSQL += "  mod_time      = sysdate, ";
  updateSQL += "  mod_pgm       = ? ";
  whereStr = "where rowid   = ? ";
  setString( 1, mbosBirthday);
  setString( 2, hNcccBatchno);
  setLong( 3, hNcccRecno);
  setString( 4, hTransType);
  setString( 5, mbosOldCardNo);
  setString( 6, getValue("act_no"));
//  if (mbos_emboss_source.equals("3") || mbos_emboss_source.equals("4")) {
  setString( 7, "");
  setString( 8, "");
  setString( 9, "");
//  }
//  else {
//	  setString( 7, sysDate);
//	  setString( 8, sysDate);
//	  setString( 9, "000");
//  }
  setString( 10, javaProgram);
  setRowId( 11, combRowid);

  updateTable();

  if(notFound.equals("Y")) 
    {
     String err1 = "update_crd_combo 1   error[notFind]" + mbosCardNo;
     String err2 = "";
     comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
    }

  return 0;
}
// ************************************************************************
    public int updateOldCard() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " update old card =[" + "" + "]");
        if (mbosEmbossSource.compareTo("3") == 0 || mbosEmbossSource.compareTo("4") == 0) {
            updateSQL = "change_status       = '2', " + "change_date         =  ? , " + "mod_pgm             =  ? , "
                    + "mod_time            = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS')";
            daoTable = "crd_card ";
            whereStr = "where card_no = ? ";

            setString(1, sysDate);
            setString(2, javaProgram);
            setString(3, sysDate + sysTime);
            setString(4, mbosOldCardNo);

            updateTable();

            if (notFound.equals("Y")) {
                String err1 = "update_crd_card 1    error[notFind]" + mbosOldCardNo;
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }
        }
        if (mbosEmbossSource.compareTo("5") == 0 || mbosEmbossSource.compareTo("7") == 0) {
            updateSQL = "reissue_status      = '2', reissue_date        =  ? ,  mod_pgm             =  ? , "
                    + "mod_time            = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS')";
            daoTable = "crd_card ";
            whereStr = "where card_no = ? ";

            setString(1, sysDate);
            setString(2, javaProgram);
            setString(3, sysDate + sysTime);
            setString(4, mbosOldCardNo);

            updateTable();

            if (notFound.equals("Y")) {
                String err1 = "update_crd_card 2    error[notFind]" + mbosOldCardNo;
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }
        }

        return 0;
    }
// ************************************************************************
public int insertCrdCombo() throws Exception 
{

if(debug == 1) showLogMessage("I", "", " insert combo  =[" + "" + "]");

        hTransType = "";
        switch (mbosEmbossSource)
           {
            case "1":
                hTransType = "01"; /* 新製卡 */
                break;
            case "2":
                hTransType = "08"; /* 普昇金 */
                break;
            case "3":
            case "4":
                hTransType = "08"; /* 續卡  */
                break;
            case "5":
		if(mbosEmbossReason.equals("1")) hTransType = "02"; /* 掛失重製 */
		if(mbosEmbossReason.equals("2")) hTransType = "07"; /* 毀損重製 */
		if(mbosEmbossReason.equals("3")) hTransType = "04"; /* 偽卡重製 */
                break;
            default:
                showLogMessage("I", "", " mbos_emboss_source error =["+ mbosEmbossSource +"]"+ mbosEmbossReason);
           }

        selectSQL = "rowid as rowid ";
//        selectSQL = "card_no ";
        daoTable  = "crd_combo ";
        whereStr  = "where card_no    = ?  ";

        setString(1, mbosCardNo);

        int recordCnt = selectTable();

        if (recordCnt > 0) {
            combRowid = getValue("rowid");
            updateCrdCombo();
            return 0;
        }


        setValue("sup_flag", "0");
        if (getValue("pm_id").trim().compareTo(getValue("apply_id")) != 0)
            setValue("sup_flag", "1");

        setValue("card_no"      , mbosCardNo);
        setValue("apply_id"     , getValue("apply_id"));
        setValue("apply_id_code", getValue("apply_id_code"));
        setValue("birthday"     , getValue("birthday"));
        setValue("pm_id"        , getValue("pm_id"));
        setValue("pm_id_code"   , getValue("pm_id_code"));
        setValue("old_card_no"  , getValue("old_card_no"));
        setValue("apply_date"   , sysDate);
        setValue("batchno"      , hNcccBatchno);
        setValueLong("recno"    , hNcccRecno);
        setValue("trans_type"   , hTransType);
        setValue("saving_actno" , getValue("act_no"));
        setValue("crt_date"     , sysDate);
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , javaProgram);
//        if (mbos_emboss_source.equals("3") || mbos_emboss_source.equals("4")) {
       	setValue("to_ibm_date"  , "");
       	setValue("rtn_ibm_date" , "");
       	setValue("rtn_code"     , "");
//        }
//        else {
//        	setValue("to_ibm_date"  , sysDate);
//        	setValue("rtn_ibm_date" , sysDate);
//        	setValue("rtn_code"     , "000");
//        }

        daoTable = "crd_combo";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_combo     error[dupRecord]";
            String err2 = mbosCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
}
// ************************************************************************
    public int deleteCrdEmbossTmp() throws Exception {

        if (debug == 1)
            showLogMessage("I", "", " delete emap   =[" + " " + "]");

        daoTable = "crd_emboss_tmp";
        whereStr = "WHERE card_no    = ? ";
        setString(1, mbosCardNo);
//        whereStr = "WHERE rowid    = ? ";
//        setRowId(1, mbos_rowid);
        

        int recCnt = deleteTable();

        if (notFound.equals("Y")) {
            String err1 = "delete_crd_emboss_tmp error[not find]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public void initRtn() throws Exception {
        hBatchno = "";
        hEmbossSource = "";
        hSuperId = "";
        hTransId = "";
        mbosRowid = "";
        combRowid = "";

        mbosToNcccCode = "";
        mbosComboIndicator = "";
        mbosOnlineMark = "";
        mbosApplyId = "";
        mbosApplyIdCode = "";
        mbosPmId = "";
        mbosPmIdCode = "";
        mbosCardNo = "";
        mbosValidFm = "";
        mbosOldCardNo = "";
        mbosSourceBatchno = "";
        mbosSourceRecno = 0;
        mbosEmbossSource = "";
        mbosEmbossReason = "";
        mbosServiceVer = "";
        mbosCardType = "";
        mbosAcctType = "";
        mbosGroupCode = "";
        mbosUnitCode = "";
        mbosServiceId = "";
        mbosAcctKey = "";
        mbosMnoId = "";
        mbosMsisdn = "";
        mbosSeId = "";
        mbosServiceType = "";
        mbosSirNo = "";
        mbosBirthday = "";
        mbosChiName = "";
        mbosNation = "";
        mbosSex = "";
        mbosMarriage = "";
        mbosBusinessCode = "";
        mbosEducation = "";
        mbosRelWithPm = "";
        mbosCurrCode = "";
        mbosDiffCode = "";
        mbosActNo = "";
        mbosEmboss4ThData = "";
        mbosMailType = "";
        mbosRejectCode = "";
        mbosMailZip = "";
        mbosMailAddr1 = "";
        mbosMailAddr2 = "";
        mbosMailAddr3 = "";
        mbosMailAddr4 = "";
        mbosMailAddr5 = "";
        mbosReasonCode = "";
        mbosCardRefNum = "";
        mbosTscAutoloadFlag = "";
        mbosSupFlag = "";
        gcrdCardMoldFlag = "";
        hAcnoPSeqno = "";
        hIdnoIdPSeqno = "";
        hDcCurrCodeGl = "";
        hAcurAutopayAcctNo = "";

        hTransType = "";
        hRelation = "";
        hCurrCode = "";
        hBinNo = "";

    }

    // ************************************************************************
    public int checkProcess(int iType, String iProgCode) throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " check=[" + iType + "] " + iProgCode);
        if (iType == 2) {
            updateSQL = " wf_value         = 'NO' , " 
                      + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
                      + " mod_pgm          = ?   ";
            daoTable  = "ptr_sys_parm";
            whereStr  = "WHERE wf_parm     = 'CRD_BATCH' " + "  and wf_key      = ? ";

            setString(1, sysDate + sysTime);
            setString(2, javaProgram);
            setString(3, iProgCode);

            int recCnt = updateTable();
            if (notFound.equals("Y")) {
                String err1 = "update_ofw_sysparm 2 error[not find]";
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }

            return (0);
        }

        /**** 檢核是否有程式在執行當中 ***/
        selectSQL = " wf_value                ";
        daoTable = "ptr_sys_parm";
        whereStr = "WHERE wf_parm       =  'CRD_BATCH' " + "  and wf_key        =  ? ";
        setString(1, iProgCode);

        tmpInt = selectTable();

        tmpChar = getValue("wf_value");

        if (tmpChar.trim().compareTo("YES") == 0 || tmpChar.trim().compareTo("yes") == 0) {
            showLogMessage("D", "", "Error:CrdD020,不可同時執行或參數檔被鎖住");
            hCallErrorDesc = "Error:CrdD020,不可同時執行或參數檔被鎖住";
            return (1);
        } else {
            updateSQL = " wf_value         = 'YES' , " + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
                    + " mod_pgm          = ?   ";
            daoTable = "ptr_sys_parm";
            whereStr = "WHERE wf_parm     = 'CRD_BATCH' " + "  and wf_key      = ? ";

            setString(1, sysDate + sysTime);
            setString(2, javaProgram);
            setString(3, iProgCode);

            tmpInt = updateTable();
            if (notFound.equals("Y")) {
                String err1 = "update_ofw_sysparm 2 error[not find]";
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }
            commitDataBase();
        }

        return (0);
    }
    // ************************************************************************

} // End of class FetchSample
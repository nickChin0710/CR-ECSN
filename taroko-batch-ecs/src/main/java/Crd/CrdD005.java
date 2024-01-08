/**********************************************************************************
*                                                                                 *
*                              MODIFICATION LOG                                   *
*                                                                                 *
*    DATE    Version    AUTHOR              DESCRIPTION                           *
*  --------  -------------------  ------------------------------------------      *
* 106/06/28  V1.01.01  Lai        Initial              old: crd_d005              *
* 107/07/25  V1.02.01  Lai        update_diff_emboss() to_nccc_date 不update		  *
* 108/12/11  V1.03.01  Rou        Update update_crd_emboss() & update_crd_combo() *
* 109/02/11  V1.04.01  Wilson     writeCnt++、mbos_nccc_filename = "nofile"        *
* 109/02/17  V1.05.01  Wilson     重製要檢核是否送設一科成功                                                                                    *
* 109/03/27  V1.05.02  Wilson     where batchno -> rowid                          *  
* 109/12/18  V1.00.03   shiyuqi       updated for project coding standard   *
* 111/12/14  V1.00.04  Wilson     補上initRtn                                       * 
* 112/02/14  V1.00.05  Wilson     getComboData、updateCrdCombo拿掉mark               *
* 112/11/27  V1.00.06  Wilson     凸字第四行不處理                                                                                                        * 
* 112/12/03  V1.00.07  Wilson     crd_item_unit不判斷卡種                                                                                * 
***********************************************************************************/
package Crd;

import com.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD005 extends AccessDAO {
    private String progname = "產生送未開卡資料檔送NCCC   112/12/03  V1.00.07 ";
    private Map<String, Object> resultMap;

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommSecr     comsecr = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    int debug = 1;
    int debugD = 1;

    String checkHome = "";
    String hCallErrorDesc = "";
    String openErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    int writeCnt = 0;
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;

    String pathName1 = "";
    int fi, fo;
    String endflag = "\r";
    int rptSeq = 0;

    String mbosBatchno = "";
    double mbosRecno = 0;
    String mbosAcctType = "";
    String mbosEmbossSource = "";
    String mbosEmbossReason = "";
    String mbosApplyId = "";
    String mbosApplyIdCode = "";
    String mbosPmId = "";
    String mbosPmIdCode = "";
    String mbosCorpNo = "";
    String mbosBirthday = "";
    String mbosUnitCode = "";
    String mbosCardType = "";
    String mbosBinNo = "";
    String mbosGroupCode = "";
    String mbosCardNo = "";
    String mbosOldCardNo = "";
    String mbosVirtualFlag = "";
    String mbosNcccFilename = "";
    String mbosToNcccCode = "";
    String mbosChkNcccFlag = "";
    String mbosVendor = "";
    String mbosCardMoldFlag = "";
    String mbosEmboss4ThData = "";
    String mbosMajorCardNo = "";
    String mbosIcIndicator = "";
    String mbosKeyType = "";
    String mbosDerivKey = "";
    int mbosLOffLnLmt = 0;
    int mbosUOffLnLmt = 0;
    String mbosDiffCode = "";
    String mbosServiceCode = "";
    String mbosServiceType = "";
    String mbosServiceVer = "";
    String mbosServiceId = "";
    String mbosSeId = "";
    String mbosPvv = "";
    String mbosCvv = "";
    String mbosCvv2 = "";
    String mbosPvki = "";
    String mbosActivationCode = "";
    String mbosTrack2Dek = "";
    String mbosPinMobile = "";
    String mbosIcFlag = "";
    String mbosValidFm = "";
    String mbosValidTo = "";
    String mbosSupFlag = "";
    String mbosOrgEmbossData = "";
    String mbosComboIndicator = "";
    String mbosRowid = "";
    String grouEmbossData = "";

    String hBinType = "";
    String hNcccTypeNo = "";
    String hDd = "";
    String ncccFilename = "";
    String prevAcctType = "";
    String hCardIndicator = "";
    String pCheckKeyExpire = "";
    String pExpireDate = "";
    int hRecCnt1 = 0;;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdD005 proc = new CrdD005();
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

            if (args.length > 3) {
                String err1 = "CrdD005  [seq_no]\n";
                String err2 = "CrdD005  [seq_no]";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comsecr = new CommSecr(getDBconnect(), getDBalias());

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr.hCallBatchSeqno = hCallBatchSeqno;

            String checkHome = comc.getECSHOME();

            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }
            comcr.hCallRProgramCode = this.getClass().getName();
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.hCallParameterData = javaProgram;
                for (int i = 0; i < args.length; i++) {
                    comcr.hCallParameterData = comcr.hCallParameterData + " " + args[i];
                }
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

            // showLogMessage("I","", "批號=" + h_batchno + " 製卡來源=" +
            // h_emboss_source);

            dateTime();
            selectPtrBusinday();

//            if (open_text_file() != 0) {
//                String err1 = "open_text_file error !!" + open_error_desc;
//                String err2 = "";
//                comcr.err_rtn(err1, err2, h_call_batch_seqno);
//            }

            totalCnt = 0;

            selectCrdEmboss();

//            if (writeCnt > 0) {
//                write_tailer();
//                comc.writeReport(pathName1, lpar1);
//                if (DEBUG != 1) {
//                    tmp_int = comsecr.To_Encrypt(pathName1);
//                    if (tmp_int != 0) {
//                        String err1 = "Encrypt_filename    error!" + pathName1;
//                        String err2 = "";
//                        comcr.err_rtn(err1, err2, h_call_batch_seqno);
//                    }
//                }
//            }

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]["+ writeCnt + "]";
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
        selectSQL = "business_date   , " + "to_char(sysdate,'yyyymmdd')  as SYSTEM_DATE , "
                + "to_char(sysdate,'dd')        as SYSTEM_DD     ";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";
        
        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("BUSINESS_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);
        hDd = getValue("SYSTEM_DD");

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]" + hDd);
    }

    // ************************************************************************
    public int openTextFile() throws Exception {
        int val = 0;

        ncccFilename = "I02" + hDd + "MI1";
        pathName1 = checkHome + "/media/crd/NCCC/" + ncccFilename;

        if (checkFileCtl() != 0) {
            return (1);
        }
        /*
         * setConsoleMode("N"); fo = openOutputText(pathName1); if ( fi == -1 )
         * { String err1= "程式執行目錄下沒有權限讀寫資料 error !!"; String err2= pathName1;
         * comcr.err_rtn(err1 , err2 , h_call_batch_seqno); }
         * setConsoleMode("Y");
         */

        showLogMessage("I", "", " Process file=" + pathName1);

        writeHeader();

        return (0);
    }

    // ************************************************************************
    public void selectCrdEmboss() throws Exception {

        selectSQL = "   a.batchno               " + " , a.recno                 " 
                  + " , a.emboss_source         " + " , a.emboss_reason         " 
                  + " , a.acct_type             " + " , a.to_nccc_code          "
                  + " , a.card_type             " + " , a.group_code            " 
                  + " , a.bin_no                " + " , a.card_no               " 
                  + " , a.major_card_no         " + " , a.apply_id              "
                  + " , a.apply_id_code         " + " , a.valid_fm              " 
                  + " , a.valid_to              " + " , a.mail_zip              " 
                  + " , a.birthday              " + " , a.nation                "
                  + " , a.business_code         " 
                  + " , decode(a.education,'','6',a.education) as education  "
                  + " , a.act_no                " + " , a.home_area_code1       " 
                  + " , a.home_tel_no1          " + " , a.org_emboss_data       " 
                  + " , a.emboss_4th_data       " + " , a.pm_id                 "
                  + " , a.pm_id_code            " + " , a.corp_no               " 
                  + " , a.force_flag            " + " , a.service_code          " 
                  + " , a.eng_name              " + " , a.marriage              "
                  + " , a.rel_with_pm           " + " , a.unit_code             " 
                  + " , a.sex                   " + " , a.pvv                   " 
                  + " , a.cvv                   " + " , a.pvki                  "
                  + " , a.trans_cvv2            " + " , a.open_passwd           " 
                  + " , a.old_card_no           " + " , a.chi_name              "
                  + " , rtrim(a.mail_addr1)||rtrim(a.mail_addr2)||rtrim(a.mail_addr3)|| rtrim(a.mail_addr4)||rtrim(a.mail_addr5) as mail_addr "
                  + " , a.service_code          " + " , a.sup_flag              " 
                  + " , a.nccc_type             "
                  + " , a.old_end_date          " // 換卡
                  + " , a.status_code           " // 換卡
                  + " , a.reason_code           " // 換卡
                  + " , decode(b.combo_indicator , '' ,'N',b.combo_indicator) as combo_indicator "
                  + " , a.ic_flag               " + " , d.ic_kind               " 
                  + " , a.branch                " + " , a.mail_attach1          " 
                  + " , a.mail_attach2          " + " , a.se_id                 "
                  + " , a.csc                   " 
                  + " , decode(a.nccc_type     ,'1',d.new_vendor,d.mku_vendor) as vendor "
                  + " , decode(c.chk_nccc_flag , '' , 'N', c.chk_nccc_flag)    as chk_nccc_flag "
                  + " , decode(d.virtual_flag  , '' , 'N', d.virtual_flag )    as virtual_flag  "
                  + " , decode(c.card_mold_flag, '' , 'O', c.card_mold_flag)   as card_mold_flag "
                  + " , b.emboss_data           " + " , c.service_type          " 
                  + " , d.service_id            " + " , a.activation_code       " 
                  + " , a.track2_dek            " + " , a.pin_mobile            "
                  + " , a.ic_cvv                " + " , a.rowid      as rowid   ";
        daoTable = "crd_emboss a, ptr_group_code b ,ptr_group_card c,crd_item_unit d ";
        whereStr = "where a.in_main_date  = ''  "
                + "  and ((a.nccc_type in ('1','2','3') AND a.card_type in ('AC','AG')) OR "
                + "       (a.nccc_type in ('1','3') AND a.card_type not in ('AC','AG')) OR "
                + "       (a.nccc_type  = '2' and (a.pvv <> '' or a.csc <> ''  ) AND "
                + "        a.to_nccc_code  = 'Y' AND (d.chg_vendor   = '2' or  "
                + "           (d.chg_vendor <> '2' and c.chk_nccc_flag ='Y'))  " + "       ) ) "
                + "  and a.to_nccc_date  = ''  " 
                + "  and a.reject_code   = ''  " 
                + "  and a.nccc_filename = ''  "
                + "  and b.group_code    = a.group_code  " 
                + "  and c.group_code    = a.group_code  "
                + "  and c.card_type     = a.card_type   " 
                + "  and d.unit_code     = a.unit_code   "
//                + "  and d.card_type     = a.card_type   "
                + "order by vendor,a.acct_type,a.card_type,a.unit_code";

        int recordCnt = selectTable();

        for (int i = 0; i < recordCnt; i++) {
        	initRtn();

            mbosBatchno = getValue("batchno", i);
            mbosRecno = getValueDouble("recno", i);
            mbosAcctType = getValue("acct_type", i);
            mbosEmbossSource = getValue("emboss_source", i);
            mbosEmbossReason = getValue("emboss_reason", i);
            mbosApplyId = getValue("apply_id", i);
            mbosApplyIdCode = getValue("apply_id_code", i);
            mbosPmId = getValue("pm_id", i);
            mbosPmIdCode = getValue("pm_id_code", i);
            mbosBirthday = getValue("birthday", i);
            mbosCorpNo = getValue("corp_no", i);
            mbosValidFm = getValue("valid_fm", i);
            mbosValidTo = getValue("valid_to", i);
            mbosCardNo = getValue("card_no", i);
            mbosOldCardNo = getValue("old_card_no", i);
            mbosMajorCardNo = getValue("major_card_no", i);
            mbosToNcccCode = getValue("to_nccc_code", i);
            mbosGroupCode = getValue("group_code", i);
            mbosCardType = getValue("card_type", i);
            mbosBinNo = getValue("bin_no", i);
            mbosUnitCode = getValue("unit_code", i);
            mbosSupFlag = getValue("sup_flag", i);
            mbosChkNcccFlag = getValue("chk_nccc_flag", i);
            mbosVendor = getValue("vendor", i);
            mbosPvv = getValue("pvv", i);
            mbosCvv = getValue("cvv", i);
            mbosCvv2 = getValue("trans_cvv2", i);
            mbosPvki = getValue("pvki", i);
            mbosCardMoldFlag = getValue("card_mold_flag", i);
            mbosActivationCode = getValue("activation_code", i);
            mbosTrack2Dek = getValue("track2_dek", i);
            mbosPinMobile = getValue("pin_mobile", i);
            mbosServiceCode = getValue("service_code", i);
            mbosServiceType = getValue("service_type", i);
            mbosServiceId = getValue("service_id", i);
            mbosSeId = getValue("se_id", i);
            mbosIcFlag = getValue("ic_flag", i);
            mbosOrgEmbossData = getValue("org_emboss_data", i);
            mbosComboIndicator = getValue("combo_indicator", i);
            mbosRowid = getValue("rowid", i);
            grouEmbossData = getValue("emboss_data", i);

            processDisplay(5000); // every nnnnn display message
            if(debug == 1) {
               showLogMessage("I","","888 Card=["+ mbosCardNo + "]" + mbosBatchno);
               showLogMessage("I","","      id=["+ mbosApplyId + "][" + mbosValidTo);
               showLogMessage("I","","     src=["+ mbosEmbossSource +"]"+ mbosEmbossReason);
            }

            totalCnt++;

            tmpInt = selPtrBintable();

            if (mbosVirtualFlag.trim().equals("Y"))
                continue;

            // *****************************************************
            // 檢查是否須送NCCC製卡
            // *****************************************************
            if (!mbosVendor.equals("2")) {
                if (mbosChkNcccFlag.equals("N")) {
                    showLogMessage("I", "", " 是製卡廠商, 但不做NCCC檢核 ");
                    continue;
                }
            }
            /*
             * lai test mbos_card_mold_flag = "M"; mbos_ic_flag = "Y";
             * mbos_activation_code= "1"; mbos_track2_dek = "1"; mbos_pin_mobile
             * = "1"; mbos_to_nccc_code = "Y";
             */
            if (debug == 1)
                showLogMessage("D", "", " 888 MOLD1[" + mbosCardMoldFlag + "]");
            if (mbosCardMoldFlag.equals("M")) {
                if (mbosServiceType.equals("04") && mbosSeId.length() == 0)
                    continue;

                if ((mbosActivationCode.length() < 1) || (mbosTrack2Dek.length() < 1)
                        || (mbosPinMobile.length() < 1))
                    continue;

                tmpInt = selPtrServiceVer();
            }

            if (debug == 1)
                showLogMessage("D", "", " 888 MOLD[" + mbosAcctType + "]" + prevAcctType);
            if (prevAcctType.compareTo(mbosAcctType) != 0) {
                getCardIndicator();
                prevAcctType = mbosAcctType;
            }

            // *******************************************************
            // 抓取晶片卡相關資料 ,DEV_KEY expire_date到期不可送 NCCC
            // *******************************************************

//            if (mbosIcFlag.equals("Y")) {
//                tmpInt = getIccardData();
//                if (tmpInt != 0) {
//                    // *********************************************************
//                    // 製卡廠商不為NCCC, 但須做NCCC做檢核 不做任何處理93/02/10
//                    // *********************************************************
//                    /*
//                     * 送NCCC做檢核 ,均需 update to_nccc_date
//                     * if(!mbos_vendor.equals("2") ) {
//                     * if(mbos_chk_nccc_flagequals("Y") ) continue; }
//                     */
//                    updateDiffEmboss();
//                    continue;
//                }
//            }

if(debug == 1)
   showLogMessage("D", "", " 888 COMBO=" + mbosComboIndicator + "," + hCardIndicator);
            // ******************************************************
            // 不為combo卡才有凸字第四行
            // 20150413 三合一卡為N 表示不為combo卡
            // ******************************************************
//            if (mbosComboIndicator.equals("N")) {
//                switch (hCardIndicator.trim()) {
//                case "1":
//                    getEmbossData();
//                    break;
//                case "2":
//                    getBusEmbossData();
//                    break;
//                }
//            }

            int chkFlag = 0;
            // ********* 不送製卡資料,只產生凸字第四行 modify 2002/05/08 **

            /*
             * lai test mbos_emboss_source = "1"; mbos_pvv = "1"; mbos_cvv =
             * "1"; mbos_cvv2 = "1"; mbos_pvki = "1";
             */
            if (debug == 1)
                showLogMessage("D", "", " 888 to NCCC[" + mbosToNcccCode + "]");
            if (mbosToNcccCode.equals("N")) {
                if (mbosEmbossSource.equals("5")) {
                    if (mbosEmbossReason.equals("1") || mbosEmbossReason.equals("3")) {
                        chkFlag = 1;
                    }
                }
                if (mbosEmbossSource.equals("7")) {
                    chkFlag = 1;
                }
                // ******************************************************
                // 製卡廠商不為NCCC, 但須做NCCC做檢核 不做任何處理
                // ******************************************************
                /*
                 * 送NCCC做檢核 ,均需 update to_nccc_date if(!mbos_vendor.equals("2")
                 * ) { if(mbos_chk_nccc_flag.equals("Y") ) continue; }
                 */
                if (chkFlag == 1) {
                    processApscard();
                }
                // ** 不做成製卡格式 ***
                updateCrdEmboss(1);
                continue;
            }

            if (debug == 1)
                showLogMessage("D", "", " 888 pvv=" + mbosPvv + "," + mbosCvv + "," + mbosCvv2 + "," + mbosPvki);
            // **** 無PVV,CVV不可產生資料,不包括不送製卡 ********************
            if (mbosToNcccCode.equals("Y")) {
                if ((mbosPvv.length() < 1) || (mbosCvv.length() < 1) || (mbosCvv2.length() < 1)
                        || (mbosPvki.length() < 1))
                    continue;
            }

if(debug == 1)
   showLogMessage("D", "", " 888 source=["+ mbosEmbossSource +"]"+ mbosComboIndicator);

            // ********** 重製中,需告知APS舊卡號重製中 ***************
            switch (mbosEmbossSource.trim()) {
            case "1":
            case "2":
                // ****************************************
                // 1. COMBO卡產生PS13磁條第三軌資料 (正卡送)
                // 若正卡第三軌資料不存在則,附卡也不能製卡
                // 2. 第三軌不存在不送製卡 20150413 改不等於N
                // *****************************************
                if (!mbosComboIndicator.equals("N")) {
                    // **** 送IBM資料是否回饋 成功****
                	tmpInt = getComboData(mbosCardNo);
if(debug == 1) showLogMessage("D", "", " 888 get comb=["+tmpInt+"]"+mbosCardNo);
                    if (tmpInt != 0) continue;
                }
                break;
            case "3":
            case "4":
                if (!mbosComboIndicator.equals("N")) {
                    // **** 送IBM資料是否回饋 成功****/
                    if ((tmpInt = getComboData(mbosCardNo)) != 0)
                        continue;
                }
                break;
            case "5":
                if (!mbosComboIndicator.equals("N")) {
                    // *** 送IBM資料是否回饋 成功****
                    if ((tmpInt = getComboData(mbosCardNo)) != 0)
                        continue;
                }
                if (tmpInt == 0) {
                    if (!mbosVendor.equals("2")) {
                        if (mbosChkNcccFlag.equals("Y"))
                            break;
                    }
                    switch (mbosEmbossReason.trim()) {
                    case "1":
                    case "3":
                        processApscard();
                        break;
                    }
                }
                break;
            }
            // *****************************************
            // combo卡抓取到正卡第三軌資料才製卡卡片
            // *****************************************
            if (tmpInt == 0) // no use
            {
                /*
                 * 送NCCC做檢核 ,均需 update to_nccc_date if(!mbos_vendor.equals("2")
                 * ) if(mbos_chk_nccc_flag.equals("Y") ) continue;
                 */

                hRecCnt1++;
                updateCrdEmboss(2);
                if (!mbosComboIndicator.equals("N")) {
                    updateCrdCombo();
                }
            }
            
            writeCnt++;
        }

//        if (h_rec_cnt1 > 0)
//            insert_crd_file_ctl();

    }

    // ************************************************************************
    public int checkFileCtl() throws Exception {
        selectSQL = " count(*) as all_cnt ";
        daoTable = " crd_file_ctl ";
        whereStr = "WHERE file_name   = ? " + "  and crt_date    = to_char(sysdate,'yyyymmdd') ";
        setString(1, ncccFilename);

        int recCnt = selectTable();

        tmpInt = getValueInt("all_cnt");
        openErrorDesc = "";

        if (tmpInt > 0) {
            openErrorDesc = "此檔案已產生不可重複產生(crd_file_ctl)"+ ncccFilename;
            showLogMessage("D", "", " 此檔案已產生不可重複產生(crd_file_ctl) " + ncccFilename);
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int writeHeader() throws Exception {

        String buf = "";
        buf = comcr.insertStr(buf, "BK02" , 1);
        buf = comcr.insertStr(buf, sysDate, 5);
        buf = comcr.insertStr(buf, String.format("%28s", " "), 13);
        buf = comcr.insertStr(buf, endflag, 41);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        return (0);
    }

    // ************************************************************************
    public int writeTailer() throws Exception {

        String buf = "";
        buf = comcr.insertStr(buf, "T", 1);
        buf = comcr.insertStr(buf, String.format("%07d", hRecCnt1), 2);
        buf = comcr.insertStr(buf, String.format("%32s", " "), 9);
        buf = comcr.insertStr(buf, endflag, 41);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        return (0);
    }

    // ************************************************************************
    public int selPtrBintable() throws Exception {
        selectSQL = " b.bin_type      ";
        daoTable  = "ptr_bintable b ";
        whereStr  = "WHERE b.bin_no    = ? "
                  + "FETCH FIRST 1 ROW ONLY";
        setString(1, mbosBinNo);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_bintable   error[not find]";
            String err2 = mbosBinNo;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        hBinType = getValue("bin_type");
        if (debug == 1)
            showLogMessage("D", "", " 888 select type =[" + hBinType + "] ");

        return (0);
    }

    // ************************************************************************
    public int selPtrServiceVer() throws Exception {
        selectSQL = "service_ver         ";
        daoTable = "ptr_service_ver     ";
        whereStr = "WHERE bin_type  = ? ";

        setString(1, hBinType);

        int recCnt = selectTable();

        mbosServiceVer = getValue("service_ver");

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_service_ver error[notFind]";
            String err2 = hBinType;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
        if (debug == 1)
            showLogMessage("D", "", " 888 select ver  =[" + mbosServiceVer + "] ");
        return (0);
    }

    // ************************************************************************
//    public int getIccardData() throws Exception {
//        if (debug == 1)
//            showLogMessage("D", "", " 888  get iccard =[" + mbosCardType + "] ");
//        selectSQL = "  a.service_code       " + ", a.deriv_key          " 
//                  + ", a.l_offln_lmt        " + ", a.u_offln_lmt        " 
//                  + ", a.check_key_expire   " + ", b.key_type           "
//                  + ", b.ic_indicator       " + ", b.expire_date        ";
//        daoTable = " crd_item_unit a, ptr_ickey b ";
//        whereStr = "WHERE a.card_type   = ? " 
//                 + "  and a.unit_code   = ? " 
//                 + "  and b.key_type    = ? " // bin_type
//                 + "  and b.key_id      = a.key_id   ";
//
//        setString(1, mbosCardType);
//        setString(2, mbosUnitCode);
//        setString(3, hBinType);
//
//        int recCnt = selectTable();
//
//        mbosServiceCode = getValue("service_code");
//        mbosKeyType = getValue("key_type");
//        mbosDerivKey = getValue("deriv_key");
//        mbosIcIndicator = getValue("ic_indicator");
//        mbosLOffLnLmt = getValueInt("l_offln_lmt");
//        mbosUOffLnLmt = getValueInt("u_offln_lmt");
//        pCheckKeyExpire = getValue("check_key_expire");
//        pExpireDate = getValue("expire_date");
//
//        if (notFound.equals("Y")) {
//            String err1 = "select_ptr_ickey       error[notFind]";
//            String err2 = hBinType;
//            comcr.errRtn(err1, err2, hCallBatchSeqno);
//        }
//        if (debug == 1)
//            showLogMessage("D", "", " 888 select ickey=[" + pCheckKeyExpire + "] " + pExpireDate + " " + mbosValidTo);
//
//        if (pCheckKeyExpire.equals("Y") && (pExpireDate.compareTo(mbosValidTo) < 0)) {
//            return (1);
//        }
//
//        return (0);
//    }

    // ************************************************************************
    public int getCardIndicator() throws Exception {

        hCardIndicator = "";

        selectSQL = "card_indicator      ";
        daoTable  = "ptr_acct_type       ";
        whereStr  = "WHERE acct_type = ? ";

        setString(1, mbosAcctType);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_acct_type   error[notFind]";
            String err2 = mbosAcctType;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        hCardIndicator = getValue("card_indicator");

        if (debug == 1)
            showLogMessage("D", "", " 888 select acct =[" + hCardIndicator + "] ");
        return (0);
    }

    // ************************************************************************
    // A) 確定是否有凸字第四行,若有以group_code內之凸字第四行為主,
    // 若co_member_flag='Y',則為凸字第四行+crd_emboss.member_id data
    // B)若無凸字第四行,若APS有凸字第四行,則帶crd_emboss.org_emboss_data
    // ************************************************************************
//    public int getEmbossData() throws Exception {
//        // *******************************************************
//        // 新製卡及普申金才需抓取此資料 (2001/07/13)
//        // *******************************************************
//if(debug ==1)
//   showLogMessage("D", "", " 888 get emboss=" + grouEmbossData + ","+ mbosOrgEmbossData);
//
//        if (mbosEmbossSource.equals("2"))
//            return (0);
//
//        tmpChar = "";
//        if (grouEmbossData.length() > 0) {
//            tmpChar = grouEmbossData;
//        } else {
//            if (mbosOrgEmbossData.length() > 0) {
//                tmpChar = tmpChar + mbosOrgEmbossData;
//            }
//        }
//if(debug ==1)
//   showLogMessage("D", "", " 888 get emboss=" + mbosEmboss4ThData +","+ tmpChar);
//
//        mbosEmboss4ThData = tmpChar;
//
//        return (0);
//    }

    // ***********************************************************************
    // 商務卡帶出凸字第四行
    // ***********************************************************************
//    public int getBusEmbossData() throws Exception {
//        selectSQL = "emboss_data         ";
//        daoTable = "crd_corp            ";
//        whereStr = "WHERE corp_no        = ? ";
//
//        setString(1, mbosCorpNo);
//
//        int recCnt = selectTable();
//
//        tmpChar = getValue("emboss_data");
//        if (tmpChar.length() > 0) {
//            mbosEmboss4ThData = tmpChar;
//        }
//
//        return (0);
//    }

    // ************************************************************************
    public int updateCrdCombo() throws Exception {

        updateSQL = "to_nccc_date    =  ? , " + "mod_pgm         =  ? , "
                  + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable = "crd_combo ";
        whereStr = "where card_no   = ? ";

        setString(1, sysDate);
        setString(2, javaProgram);
        setString(3, sysDate + sysTime);
        setString(4, mbosCardNo);
        
        updateTable();
        
        if (notFound.equals("Y")) {
            String err1 = "update_crd_combo           error[notFind]";
            String err2 = mbosCardNo;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int updateDiffEmboss() throws Exception {
        updateSQL = "to_nccc_date    =  ? , " + "diff_code       = 'Y', " 
                  + "chk_nccc_flag   =  ? , " + "ic_indicator    =  ? , " 
                  + "key_type        =  ? , " + "deriv_key       =  ? , "
                  + "l_offln_lmt     =  ? , " + "u_offln_lmt     =  ? , " 
                  + "mod_pgm         =  ? , "
               //   + "pin_block       = '' , "
                  + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable = "crd_emboss";
        whereStr = "where rowid   = ? ";

//      setString(1, sysDate);
        setString(1, "");
        setString(2, mbosChkNcccFlag);
        setString(3, mbosIcIndicator);
        setString(4, mbosKeyType);
        setString(5, mbosDerivKey);
        setInt(6, mbosLOffLnLmt);
        setInt(7, mbosUOffLnLmt);
        setString(8, javaProgram);
        setString(9, sysDate + sysTime);
        setRowId(10, mbosRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss expire   error[notFind]";
            String err2 = mbosCardNo;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public int updateCrdEmboss(int i) throws Exception {
        int type = 0;

        if (debug == 1)
            showLogMessage("I", "", " update emboss =[" + i + "]" + mbosToNcccCode);

        if (mbosToNcccCode.equals("Y"))
            mbosNcccFilename = "nofile";
        if (!mbosEmbossSource.equals("5"))
            type = 1;
        if (mbosEmbossSource.equals("5") && mbosEmbossReason.equals("2"))
            type = 1;

        if (debug == 1)
            showLogMessage("I", "", " update name=[" + mbosNcccFilename + "]" + type);

        if (type == 0) {
            updateSQL = "to_nccc_date    =  ? , " + "nccc_filename   =  ? , " 
//                      + "emboss_4th_data =  ? , " 
            		  + "major_card_no   =  ? , " 
                      + "ic_indicator    =  ? , " + "key_type        =  ? , "
                      + "deriv_key       =  ? , " + "l_offln_lmt     =  ? , " 
                      + "u_offln_lmt     =  ? , " + "diff_code       =  ? , " 
                      + "chk_nccc_flag   =  ? , " + "vendor          =  ? , "
                      + "service_ver     =  ? , " + "service_id      =  ? , " 
                      + "service_type    =  ? , " + "mod_pgm         =  ? , " 
                      + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_emboss";
             whereStr  = "where rowid   = ? ";
//           whereStr  = "where batchno   = ? ";

            setString(1, sysDate);
            setString(2, mbosNcccFilename);
//            setString(3, mbosEmboss4ThData);
            setString(3, mbosMajorCardNo);
            setString(4, mbosIcIndicator);
            setString(5, mbosKeyType);
            setString(6, mbosDerivKey);
            setInt(7, mbosLOffLnLmt);
            setInt(8, mbosUOffLnLmt);
            setString(9, mbosDiffCode);
            setString(10, mbosChkNcccFlag);
            setString(11, mbosVendor);
            setString(12, mbosServiceVer);
            setString(13, mbosServiceId);
            setString(14, mbosServiceType);
            setString(15, javaProgram);
            setString(16, sysDate + sysTime);
            setRowId( 17, mbosRowid);
//          setString(18, mbos_batchno);
        } else {
            updateSQL = "to_nccc_date    =  ? , " + "nccc_filename   =  ? , " 
//                      + "emboss_4th_data =  ? , "
            		  + "ic_indicator    =  ? , " 
                      + "key_type        =  ? , " + "deriv_key       =  ? , "
                      + "l_offln_lmt     =  ? , " + "u_offln_lmt     =  ? , " 
                      + "diff_code       =  ? , " + "chk_nccc_flag   =  ? , " 
                      + "vendor          =  ? , " + "service_ver     =  ? , "
                      + "service_id      =  ? , " + "service_type    =  ? , " 
                      + "mod_pgm         =  ? , "
                      + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable = "crd_emboss";
            whereStr  = "where rowid   = ? ";
//          whereStr = "where batchno   = ? ";

            setString(1, sysDate);
            setString(2, mbosNcccFilename);
//            setString(3, mbosEmboss4ThData);
            setString(3, mbosIcIndicator);
            setString(4, mbosKeyType);
            setString(5, mbosDerivKey);
            setInt(6, mbosLOffLnLmt);
            setInt(7, mbosUOffLnLmt);
            setString(8, mbosDiffCode);
            setString(9, mbosChkNcccFlag);
            setString(10, mbosVendor);
            setString(11, mbosServiceVer);
            setString(12, mbosServiceId);
            setString(13, mbosServiceType);
            setString(14, javaProgram);
            setString(15, sysDate + sysTime);
            setRowId(16, mbosRowid);
//          setString(17, mbos_batchno);
        }

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss    error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public int processApscard() throws Exception {
        String apscStatusCode = "";
        String apscRowid = "";
        if (debug == 1)
            showLogMessage("D", "", " 888 process aps =[" + mbosEmbossSource + "] ");

        if (mbosEmbossSource.equals("7"))
            apscStatusCode = "4";

        if (mbosEmbossSource.equals("5")) {
            switch (mbosEmbossReason.trim()) {
            /**** 掛失重製中 ********/
            case "1":
                apscStatusCode = "4";
                break;
            /**** 偽卡重製中 ********/
            case "3":
                apscStatusCode = "3";
                break;
            }
        }

        selectSQL = "rowid               ";
        daoTable = "crd_apscard         ";
        whereStr = "WHERE card_no        = ?  " 
                 + "  and to_aps_date    = '' ";

        setString(1, mbosCardNo);

        int recCnt = selectTable();

        // tmp_char = getValue("emboss_data");
        if (!notFound.equals("Y")) // Found
        {
            apscRowid = getValue("rowid");

            updateSQL = "status_code     =  ? , " + "mod_pgm         =  ? , "
                      + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_apscard";
            whereStr  = "where rowid     = ? ";

            setString(1, apscStatusCode);
            setString(2, javaProgram);
            setString(3, sysDate + sysTime);
            setRowId(4, apscRowid);

            updateTable();

            if (notFound.equals("Y")) {
                String err1 = "update_apscard card_no     error[notFind]";
                String err2 = mbosCardNo;
                comcr.errRtn(err1, err2, hCallBatchSeqno);
            }
        } else {
            insertApscard();
        }

        return 0;
    }

    // ************************************************************************
    public int insertCrdFileCtl() throws Exception {
        setValue("file_name", ncccFilename);
        setValueInt("head_cnt", hRecCnt1);
        setValueInt("record_cnt", hRecCnt1);
        setValue("crt_date", sysDate);
        setValue("trans_in_date", sysDate);
        setValue("err_proc_code", "");

        daoTable = "crd_file_ctl";

        insertTable();

        if (dupRecord.equals("Y")) {
            updateSQL = "head_cnt        =  ? , " 
                      + "record_cnt      =  ? , " 
                      + "trans_in_date   =  ?   ";
            daoTable = "crd_file_ctl";
            whereStr = "where file_name = ? ";

            setInt(1, hRecCnt1);
            setInt(2, hRecCnt1);
            setString(3, sysDate);
            setString(4, ncccFilename);

            updateTable();

            if (notFound.equals("Y")) {
                String err1 = "update_crd_file_ctl           失敗  error[notFind]";
                String err2 = ncccFilename;
                comcr.errRtn(err1, err2, hCallBatchSeqno);
            }
        }

        return 0;
    }

    // ************************************************************************
    public int insertApscard() throws Exception {
        showLogMessage("I", "", "  888 insert aps=[" + mbosOldCardNo + "]");
        // * 附卡
        getApplyData();
        String apscSupId = "";
        String apscSupIdCode = "";
        String apscSupBirthday = "";
        String apscSupName = "";
        String apscPmId = "";
        String apscPmIdCode = "";
        String apscPmBirthday = "";
        String apscPmName = "";

        if (mbosApplyId.compareTo(mbosPmId) != 0) {
            apscSupId = mbosApplyId;
            apscSupIdCode = mbosApplyIdCode;
            apscSupBirthday = getValue("appl_birthday");
            apscSupName = getValue("appl_chi_name");
            getPmData();
            apscPmId = mbosPmId;
            apscPmIdCode = mbosPmIdCode;
            apscPmBirthday = getValue("pm_birthday");
            apscPmName = getValue("pm_chi_name");
        } else {
            apscPmId = mbosApplyId;
            apscPmIdCode = mbosApplyIdCode;
            apscPmBirthday = getValue("appl_birthday");
            apscPmName = getValue("appl_chi_name");
        }

        setValue("pm_id", apscPmId);
        setValue("pm_id_code", apscPmIdCode);
        setValue("pm_birthday", apscPmBirthday);
        setValue("pm_name", apscPmName);
        setValue("sup_id", apscSupId);
        setValue("sup_id_code", apscSupIdCode);
        setValue("sup_birthday", apscSupBirthday);
        setValue("sup_name", apscSupName);
        setValue("card_no", mbosOldCardNo);
        setValue("valid_date", getValue("old_end_date"));

        tmpInt = selCrdCard();
        setValue("reissue_date", getValue("sysDate"));
        setValue("mail_type", getValue("card.mail_type"));
        setValue("mail_branch", getValue("card.mail_branch"));
        setValue("mail_no", getValue("card.mail_no"));
        setValue("mail_date", getValue("card.mail_proc_date"));
        setValue("stop_date", getValue("card.oppost_date"));
        setValue("crt_datetime", sysDate + sysTime);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        // ***** 停用原因 ***********
        switch (getValue("card.current_code")) {
        case "1":
            setValue("stop_reason", "3");
            break;
        case "2":
            setValue("stop_reason", "2");
            break;
        case "3":
            setValue("stop_reason", "1");
            break;
        case "4":
            setValue("stop_reason", "3");
            break;
        case "5":
            setValue("stop_reason", "5");
            break;
        }

        if (mbosSupFlag.equals("1")) {
            setValue("sup_lost_status", "0");
        }

        daoTable = "crd_apscard";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_apscard   error[dupRecord]=" + mbosOldCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
        return 0;
    }

    // ************************************************************************
    public int getApplyData() throws Exception {
        selectSQL = "chi_name  as appl_chi_name , " + "birthday  as appl_birthday   ";
        daoTable = "crd_idno            ";
        whereStr = "WHERE id_no       = ? " + "  and id_no_code  = ? ";

        setString(1, mbosApplyId);
        setString(2, mbosApplyIdCode);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_apply_id from crd_idno 失敗  error[notFind]";
            String err2 = mbosApplyId;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int getPmData() throws Exception {
        selectSQL = "chi_name  as pm_chi_name , " + "birthday  as pm_birthday   ";
        daoTable = "crd_idno            ";
        whereStr = "WHERE id_no       = ? " + "  and id_no_code  = ? ";

        setString(1, mbosPmId);
        setString(2, mbosPmIdCode);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_pm_id from crd_idno 失敗  error[notFind]";
            String err2 = mbosPmId;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int selCrdCard() throws Exception {
        extendField = "card.";
        selectSQL = "mail_type    ,mail_no,        " + "mail_branch  ,mail_proc_date, "
                + "oppost_date  ,current_code    ";
        daoTable = "crd_card            ";
        whereStr = "WHERE card_no     = ? ";

        setString(1, mbosOldCardNo);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_mail  from crd_card 失敗  error[notFind]";
            String err2 = mbosOldCardNo;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int getComboData(String cardNo) throws Exception {
        extendField = "comb.";
        selectSQL = "saving_actno, third_data, sup_flag ";
        daoTable  = "crd_combo           ";
        whereStr  = "WHERE card_no      = ?  " 
                  + "  and to_ibm_date  <> '' " 
                  + "  and rtn_ibm_date <> '' "
                  + "  and rtn_code     = '000' ";
        setString(1, cardNo);

        int recCnt = selectTable();

        if (notFound.equals("Y"))
            return (1);

if(debug == 1)
   showLogMessage("D", "", " 888 get comb thi=["+getValue("comb.third_data")+"]"+getValue("comb.sup_flag"));

        return (0);
    }

    // ************************************************************************
    public void initRtn() throws Exception {

        mbosBatchno = "";
        mbosRecno = 0;
        mbosAcctType = "";
        mbosEmbossSource = "";
        mbosEmbossReason = "";
        mbosApplyId = "";
        mbosApplyIdCode = "";
        mbosBirthday = "";
        mbosPmId = "";
        mbosPmIdCode = "";
        mbosCorpNo = "";
        mbosUnitCode = "";
        mbosGroupCode = "";
        mbosCardType = "";
        mbosBinNo = "";
        mbosCardNo = "";
        mbosOldCardNo = "";
        mbosVirtualFlag = "";
        mbosNcccFilename = "";
        mbosToNcccCode = "";
        mbosChkNcccFlag = "";
        mbosVendor = "";
        mbosEmboss4ThData = "";
        mbosMajorCardNo = "";
        mbosIcIndicator = "";
        mbosKeyType = "";
        mbosDerivKey = "";
        mbosLOffLnLmt = 0;
        mbosUOffLnLmt = 0;
        mbosDiffCode = "";
        mbosServiceCode = "";
        mbosServiceType = "";
        mbosServiceVer = "";
        mbosServiceId = "";
        mbosSeId = "";
        mbosPvv = "";
        mbosCvv = "";
        mbosCvv2 = "";
        mbosPvki = "";
        mbosActivationCode = "";
        mbosTrack2Dek = "";
        mbosPinMobile = "";
        mbosIcFlag = "";
        mbosValidFm = "";
        mbosValidTo = "";
        mbosSupFlag = "";
        mbosOrgEmbossData = "";
        mbosComboIndicator = "";
        mbosRowid = "";
        grouEmbossData = "";

        hBinType = "";
        hNcccTypeNo = "";
        pExpireDate = "";
    }
    // ************************************************************************

} // End of class FetchSample

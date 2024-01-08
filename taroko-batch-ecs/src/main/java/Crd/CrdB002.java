/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/05/16  V1.01.01    Lai        Initial                                    *
* 108/11/18  V2.01.01    Pino       Initial                                    *
* 109/02/07  V2.01.02  Pino       chk_branch                                 *
* 109/10/05  V2.01.03  Wilson     ptr_bintable where 條件調整                                                    *
* 109/11/03  V2.01.04  Wilson     dc_indicator、curr_code移到CrdB005 update    *
* 109/11/25  V2.01.05  Wilson     服務碼不可為空白!                               *
* 109/12/11   V2.01.06  Justin      add chkAddrIsEmpty      *
* 109/12/17  V2.01.07    shiyuqi       updated for project coding standard   *
* 110/06/18  V2.01.08  Wilson     dc_indicator、curr_code移回                                                 *
* 110/08/18  V2.01.09  Wilson     新增introduce_id檢核                                                                      *
* 110/09/03  V2.01.10  Wilson     chkSameGroupType新增認同集團碼條件                                        *
* 111/12/13  V2.01.11  Wilson     調整年利率轉日利率公式                                                                              *
* 111/12/27  V2.01.12  Wilson     特殊團代利率算法調整                                                                                 *
* 111/12/30  V2.01.13  Wilson     卡種調整為讀ptr_group_card取得                                                 *
* 112/03/08  V2.01.14  Wilson     where條件增加reject_code <> 'Y'              *
* 112/03/09  V2.01.15  Wilson     where條件調整為reject_code = ''               *
* 112/05/18  V2.01.16  Wilson     mark chkIntroduceId                        *
* 112/08/24  V2.01.17  Wilson     mark combo卡特殊檢核邏輯                                                                 *
* 112/11/26  V2.01.18  Wilson     商務卡檢核相同統編不可存在相同的活卡                                                   *
* 112/11/30  V2.01.19  Wilson     讀取認同集團碼、效期迄日                                                                         *
* 112/12/05  V2.01.20  Wilson     逐筆commit                                  *
*****************************************************************************/
package Crd;

import com.*;

import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CrdB002 extends AccessDAO {
    private String progname = "新製卡資料檢核作業     112/12/05  V2.01.20";
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
    int totalErr = 0;
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;
    int count = 0;

    int hErrorFlag = 0;
    String emapBatchno = "";
    String emapRowid = "";
    String emapGroupCode = "";
    String emapCardType = "";
    String emapSourceCode = "";
    String emapMsisdn = "";
    String emapApplyId = "";
    String emapApplyIdCode = "";
    String emapPmId = "";
    String emapPmIdCode = "";
    String emapBirthday = "";
    String emapStmtCycle = "";
    String emapServiceType = "";
    String emapMajorCardNo = "";
    String emapValidFm = "";
    String emapValidTo = "";
    String emapMajorValidFm = "";
    String emapMajorValidTo = "";
    String emapAcctType = "";
    String emapPmBirthday = "";
    String emapCheckCode = "";
    String emapMajorChgFlag = "";
    String emapCurrCode = "";
    String emapDcIndicator = "";
    String emapActNoF = "";
    String emapComboIndicator = "";
    String emapFinalFeeCode = "";
    String emapFeeCode = "";
    String emapCorpNo = "";
    String emapFeeReasonCode = "";
    String emapSupBirthday = "";
    String emapCardNo = "";
    int emapAnnualFee = 0;
    int emapCreditLmt = 0;
    int emapStandardFee = 0;
    String emapRiskBankNo = "";
    String emapActNo = "";
    String emapCardcat = "";
    String newCardcat = "";
    String emapUnitCode = "";
    String emapIcFlag = "";
    String emapServiceCode = "";
    String emapForceFlag = "";
    double emapRevolveIntRate = 0.0;
    double emapRevolveIntRateYear = 0.0;
    double emapSpecialCardRate = 0.0;
    String emapBusinessCode = "";
    String emapRegBankNo = "";
    String emapBranch = "";
    String emapEngName = "";
    String emapNcccType = "";
    String emapBillApplyFlag = "",
    emapResidentAddr1="", 
    emapResidentAddr2="", 
    emapResidentAddr5="",
    emapMailAddr1="", 
    emapMailAddr2="", 
    emapMailAddr5="",
    emapCompanyAddr1="", 
    emapCompanyAddr2="", 
    emapCompanyAddr5="";
    String emapSupFlag = "";
    String emapIntroduceId = "";

    int tSupCd = 0;
    String ptrServiceType = "";
    String ptrCardMoldFlag = "";
    String ptrCardType = "";
    int ptrExtnYear = 0;
    int hLineOfCreditAmt = 0;
    String hNewEndDate = "";
    String cardComboAcctNo = "";
    String idnoIdPSeqno = "";
    String idnoIdNoCode = "";
    String dcCurrCodeGl = "";
    String tCardIndicator = "";
    String tAcctType = "";
    int tFirstFee = 0;
    int tOtherFee = 0;
    double tSupRate = 0.00;
    int tSupEndMonth = 0;
    double tSupEndRate = 0.00;
    
   String hEriaRefIp = "";
   String hEriaPortNo = "";
   String hRtnStr = "";
   String hRtnDesc = "";
    

    //appcbuf snd_strc = new appcbuf();
    //appcbuf rcv_strc = new appcbuf();

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdB002 proc = new CrdB002();
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

            if (args.length > 2) {
                String err1 = "nCrdB002 請輸入 : callseqno";
                String err2 = "";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                comcr.hCallParameterData = javaProgram;
                for (int i = 0; i < args.length; i++) {
                    comcr.hCallParameterData = comcr.hCallParameterData + " " + args[i];
                }
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

            dateTime();
            selectPtrBusinday();

            //select_ecs_ref_ip_addr();
            totalCnt = 0;
            totalErr = 0;
            selectCrdEmapTmp();

            comcr.hCallErrorDesc = "程式執行結束,筆數=["+totalCnt+"],Error="+totalErr;
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

//        hBusiBusinessDate = getValue("BUSINESS_DATE");
        hBusiBusinessDate = getValue("SYSTEM_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");
    }

    // ************************************************************************
    public void selectCrdEmapTmp() throws Exception {

        selectSQL = " a.batchno             ,a.recno                 , "
                + " a.apply_id            ,nvl(a.apply_id_code,'0') as apply_id_code, "
                + " a.birthday            ,a.eng_name              , "
                + " a.pm_id               ,a.pm_id_code            , "
                + " a.risk_bank_no        ,a.reg_bank_no           , "
                + " a.card_type           ,nvl(a.group_code,'0000') as group_code, "
                + " a.source_code         ,a.credit_lmt            , "
                + " a.unit_code           ,a.stmt_cycle            , "
                + " a.corp_no             ,nvl(a.corp_no_code,'0')  as corp_no_code, "
                + " a.corp_act_flag       ,a.corp_assure_flag      , "
                + " a.valid_fm            ,a.valid_to              , "
                + " a.online_mark         ,nvl(a.fee_code,'0') as fee_code    , "
                + " a.pm_birthday         ,a.major_card_no         , "
                + " a.major_valid_fm      ,a.major_valid_to        , "
                + " a.major_chg_flag      ,a.cardcat               , "
                + " a.act_no              ,a.act_no_f              , "
                + " a.act_no_l            ,a.apply_no              , "
                + " a.mno_id              ,a.service_type          , "
                + " a.msisdn              ,a.se_id                 , "
                + " a.force_flag          ,a.rowid      as rowid   , "
                + " a.revolve_int_rate_year ,a.business_code       , "
                + " a.reg_bank_no         ,a.branch                , "
                + " a.eng_name            ,a.nccc_type             , "
                + " a.acct_type           ,a.card_no               , "
                + " a.bill_apply_flag     ,a.introduce_id          , "
                + " a.resident_addr1, a.resident_addr2, a.resident_addr5,"
                + " a.mail_addr1, a.mail_addr2, a.mail_addr5,"
                + " a.company_addr1, a.company_addr2, a.company_addr5";

        daoTable = "crd_emap_tmp a";
        whereStr = "where (a.check_code = '' or a.check_code != '000') and a.apr_date != '' and reject_code = '' ";
        whereStr += " order by a.group_code,a.sup_flag,a.batchno,a.recno ";

        openCursor();

        while (fetchTable()) {
            initRtn();
            emapBatchno = getValue("batchno");
            emapGroupCode = getValue("group_code");
            if (emapGroupCode.trim().length() < 1)
                emapGroupCode = "0000";
            emapCardType = getValue("card_type");
            emapSourceCode = getValue("source_code");
            emapMsisdn = getValue("msisdn");
            emapUnitCode = getValue("unit_code");
            emapStmtCycle = getValue("stmt_cycle");
            emapApplyId = getValue("apply_id");
            emapApplyIdCode = getValue("apply_id_code");
            if (emapApplyIdCode.trim().length() < 1)
                emapApplyIdCode = "0";
            emapPmId = getValue("pm_id");
            emapPmIdCode = getValue("pm_id_code");
            emapBirthday = getValue("birthday");
            emapFeeCode = getValue("fee_code");
            if (emapFeeCode.trim().length() < 1)
                emapFeeCode = "0";
            emapCorpNo = getValue("corp_no");
            emapCreditLmt = getValueInt("credit_lmt");
            emapRiskBankNo = getValue("risk_bank_no");
            emapServiceType = getValue("service_type");
            emapActNo = getValue("act_no");
            emapActNoF = getValue("act_no_f");
            emapCardcat = getValue("cardcat");
            if (emapCardcat.length() < 2)
                emapCardcat = "01";
            emapValidFm = getValue("valid_fm");
//            emapValidTo = getValue("valid_to");
//            hNewEndDate = getValue("valid_to");
            emapForceFlag = getValue("force_flag");
            emapRowid = getValue("rowid");
            emapRevolveIntRateYear = getValueDouble("revolve_int_rate_year");
            emapBusinessCode = getValue("business_code");
            emapRegBankNo = getValue("reg_bank_no");
            emapBranch = getValue("branch");
            emapEngName = getValue("eng_name");
            emapNcccType = getValue("nccc_type");
            emapAcctType = getValue("acct_type");
            emapCardNo = getValue("card_no");
            emapBillApplyFlag = getValue("bill_apply_flag");
            emapIntroduceId = getValue("introduce_id");
            emapResidentAddr1 = getValue("resident_addr1");
            emapResidentAddr2 = getValue("resident_addr2");
            emapResidentAddr5 = getValue("resident_addr5");
            emapMailAddr1 = getValue("mail_addr1");
            emapMailAddr2 = getValue("mail_addr2");
            emapMailAddr5 = getValue("mail_addr5");
            emapCompanyAddr1 = getValue("company_addr1");
            emapCompanyAddr2 = getValue("company_addr2");
            emapCompanyAddr5 = getValue("company_addr5");

            totalCnt++;
            processDisplay(5000); // every nnnnn display message
            if (debug == 1) {
                showLogMessage("I", "", "888 apply_id = ["+ emapApplyId +"]"+" pm_id = ["+ emapPmId +"]"+totalCnt);
                showLogMessage("I", "", " group_code = ["+ emapGroupCode + "]");
                showLogMessage("I", "", " unit_code =["+ emapUnitCode + "]");
            }

            hErrorFlag = 0;

            tmpInt = selectPtrGroupCard();
            if (tmpInt > 0) {
                hErrorFlag = tmpInt;
            }
            tmpInt = selectCrdIemUnit();
            if (tmpInt > 0) {
                hErrorFlag = tmpInt;
            }
            if (debug == 1)
                showLogMessage("D", "", " 888 3.00 step=[" + hErrorFlag + "] ");
            
            selectNewExtnMm();
            
            // 檢核效期日期,是否大於系統日期
            if (hErrorFlag == 0)
                hErrorFlag = chkData();

            if (debug == 1)
                showLogMessage("D", "", " 888 3.01 step=[" + hErrorFlag + "] ");

            /*
             * lai test ptr_card_mold_flag = "M"; h_error_flag = 0; emap_msisdn
             * = "09211234"; ptr_service_type = "B"; emap_service_type = "B";
             */
            // 若為手機信用卡則做相關驗證
            /*刪除以下檢核
            if (h_error_flag == 0 && ptr_card_mold_flag.equals("M")) {
                h_error_flag = chk_msisdn();
            }
            */
            //相同ID不同生日不可進件
            if (hErrorFlag == 0)
                hErrorFlag = chkBirthday(emapApplyId, emapBirthday);

            if (debug == 1)
                showLogMessage("D", "", " 888 3.02 step=[" + hErrorFlag + "] ");
            
            //檢核年利率
            if (hErrorFlag == 0)
                hErrorFlag = chkRateYear();

            if (debug == 1)
                showLogMessage("D", "", " 888 3.03 step=[" + hErrorFlag + "] ");
            
            //檢核行業別是否存在
            if (hErrorFlag == 0)
                hErrorFlag = chkBusinessCode();

            if (debug == 1)
                showLogMessage("D", "", " 888 3.04 step=[" + hErrorFlag + "] ");
            
            //檢核發卡分行是否存在
            if (hErrorFlag == 0)
                hErrorFlag = chkRegBankNo();

            if (debug == 1)
                showLogMessage("D", "", " 888 3.05 step=[" + hErrorFlag + "] ");
            
            //檢核寄送分行是否存在
            if (hErrorFlag == 0)
            	if(emapBranch.length()>0) //V2.01.02
                hErrorFlag = chkBranch();

            if (debug == 1)
                showLogMessage("D", "", " 888 3.06 step=[" + hErrorFlag + "] ");
            
            // ID向下有同團代、卡種、認同集團碼之活卡之檢核 
            if (hErrorFlag == 0) {
            	if(emapCorpNo.length() == 0) {
            		hErrorFlag = chkGenSameGroupType();
            	}
            	else {
            		hErrorFlag = chkBusSameGroupType();
            	}
            }                

            if (debug == 1)
                showLogMessage("D", "", " 888 3.07 step=[" + hErrorFlag + "] ");
            
            // 新製卡英文名檢核 
            if (hErrorFlag == 0)
                hErrorFlag = chkEngName();

            if (debug == 1)
                showLogMessage("D", "", " 888 3.08 step=[" + hErrorFlag + "] ");
            
            // 檢核STMT_CYCLE是否存在
            if (hErrorFlag == 0 && emapStmtCycle.trim().length() > 0) {
                hErrorFlag = chkStmtCycle();
            }

            // 檢核SOURCE_CODE是否存在
//            if (hErrorFlag == 0 && emapSourceCode.trim().length() > 0) {
//                tmpInt = chkSourceCode();
//				if (tmpInt > 0) {
//					hErrorFlag = 19;
//				}
//            }
            
            // 檢核帳單寄送註記與地址
            if (hErrorFlag == 0 && emapBillApplyFlag.trim().length() > 0) {
            	switch (emapBillApplyFlag) {
            	// 同戶籍
    			case "1":
    				tmpInt = chkAddrIsEmpty(emapResidentAddr1, emapResidentAddr2, emapResidentAddr5);
    				break;
                // 同居住
    			case "2":
    				tmpInt = chkAddrIsEmpty(emapMailAddr1, emapMailAddr2, emapMailAddr5);
    				break;
    			// 同公司
    			case "3":
    				tmpInt = chkAddrIsEmpty(emapCompanyAddr1, emapCompanyAddr2, emapCompanyAddr5);
    				break;
    			default:
    				break;
    			}
            	if (tmpInt > 0) {
    				hErrorFlag = 39;
    			}
			}            

            // check 是否為附卡,並抓取正卡之效期
            // 附卡抓取正卡ACCT_TYPE,其他抓取PTR_ACCT_TYPE
            if (debug == 1)
                showLogMessage("D", "", " 888 4.0 step=[" + hErrorFlag +"]"+ emapValidTo);
            if (hErrorFlag == 0) {
                hErrorFlag = supCardProcess();
            }

            if (debug == 1)
                showLogMessage("D", "", " 888 4.1 step=[" + hErrorFlag + "] " + tSupCd);
//            if (h_error_flag == 0) {
//                emap_dc_indicator = "N";
//                if (t_sup_cd == 0)
//                    h_error_flag = chk_ptr_card_type();
//                else
//                    h_error_flag = sel_ptr_card_type();
//            }
            
            if (hErrorFlag == 0) {
                emapDcIndicator = "N";
                if (emapSupFlag.equals("0"))
                    hErrorFlag = chkPtrCardType();
                else
                    hErrorFlag = selPtrCardType();
            }

            if (debug == 1)
                showLogMessage("D", "", " 888 5.1 step=["+ hErrorFlag + "]"+" "+ emapAcctType);

            // 判斷此資料是屬於何種 acct_type
            if (hErrorFlag == 0) {
                if (emapAcctType.trim().length() == 0) {
                    hErrorFlag = getAcctType();
                    if (hErrorFlag == 0) {
                        emapAcctType = tAcctType.trim();
                    }
                } else {
                    hErrorFlag = getCardIndicator();
                    if (hErrorFlag == 0)
                        emapComboIndicator = getComboIndicator(emapGroupCode);
                }
            }
            if (debug == 1)
                showLogMessage("D", "", " 8886 ERROR=" + hErrorFlag + ", " + tCardIndicator);
            /* lai */
            /*刪除以下檢核
            if (h_error_flag == 0 && !emap_force_flag.equals("Y")) {
                h_error_flag = chk_id_birthday(emap_apply_id, emap_birthday);
            }
            */
            if(debug == 1) showLogMessage("D",""," 8887 三合一="+ hErrorFlag +","+ tCardIndicator);
            // 抓取年費資料
            // 三合一卡改combo_indicator 不等於N
            if (hErrorFlag == 0) {
                if (tCardIndicator.trim().compareTo("1") == 0) {
                    hErrorFlag = chkAnnualFee(tSupCd, emapFeeCode.trim());
                    // 三合一 歡喜卡檢查
                    if (hErrorFlag == 0 && emapComboIndicator.trim().compareTo("N") != 0) {
                        hErrorFlag = chkDupAcctNo();
                    }
                }
                if (tCardIndicator.trim().compareTo("2") == 0) {
                    hErrorFlag = chkBusAnnualFee(emapFeeCode.trim());
                    //檢核公司統編是否存在
                    hErrorFlag = chkCorpNo();
                }
                emapFinalFeeCode = emapFeeCode.trim();
            }

            if (debug == 1)
                showLogMessage("D", "", " 888 update=[" + hErrorFlag + "]");
            /*
             * lai test h_error_flag = 9;
             */
            
//            if (hErrorFlag == 0 && emapIntroduceId.trim().length() > 0)
//                hErrorFlag = chkIntroduceId();

            if (debug == 1)
                showLogMessage("D", "", " 888 9.1 step=[" + hErrorFlag + "] ");
            
            if (hErrorFlag > 0) {
                totalErr++;
                updateCrEmapErr();
            } else
                updateCrdEmapTmp();

            commitDataBase();
        }

    }

    // ************************************************************************
    public int selectPtrGroupCard() throws Exception {
        selectSQL = " service_type , " 
                  + " case when card_mold_flag ='' then 'O' else card_mold_flag  "
                  + " end as card_mold_flag, "
                  + " card_type ";
        daoTable = "ptr_group_card ";
        whereStr = "WHERE group_code =  ? ";
        setString(1, emapGroupCode);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (4);
        }

        ptrServiceType = getValue("service_type");
        ptrCardMoldFlag = getValue("card_mold_flag");
        ptrCardType = getValue("card_type");
        
        emapCardType = ptrCardType;

        // 處理 ic_flag
        tmpInt = selectPtrGroupCardDtl();
        if (tmpInt > 0)
            return (tmpInt);

        return (0);
    }
    // ************************************************************************
    public int selectCrdIemUnit() throws Exception {
        selectSQL = " extn_year " ;
        daoTable = "crd_item_unit ";
        whereStr = "WHERE unit_code =  ? ";
        setString(1, emapUnitCode);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (20);
        }

        ptrExtnYear = getValueInt("extn_year");

        return (0);
    }
    // ************************************************************************
    public int selectPtrGroupCardDtl() throws Exception {

        if (debug == 1)
        //showLogMessage("D", "", " cardcat=[" + emap_cardcat + "]");
        //String h_cardcat_code = emap_cardcat.substring(0, 2);
        //if (h_cardcat_code.trim().length() < 2)
            //h_cardcat_code = "01";

        selectSQL = " unit_code ";
        daoTable  = "ptr_group_card_dtl";
        whereStr  = "WHERE group_code      =  ? " 
                  + "  and card_type       =  ? "; 
        setString(1, emapGroupCode);
        setString(2, emapCardType);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
        	return (25);
        }
        
        emapUnitCode = getValue("unit_code");

        selectSQL = " ic_flag ,service_code , electronic_code ";
        daoTable = "crd_item_unit";
        whereStr = "WHERE unit_code     =  ? ";
        setString(1, emapUnitCode);

        recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (20);
        }

        emapIcFlag = getValue("ic_flag");
        emapServiceCode = getValue("service_code");
        newCardcat = getValue("electronic_code");
        
        if(emapServiceCode.equals("")) {
			comcr.errRtn("服務碼不可為空白!", "認同集團碼 = " + emapUnitCode + "，卡種 = " + emapCardType, comcr.hCallBatchSeqno);
		}
        
        if (emapIcFlag.trim().length() == 0)
            emapIcFlag = "N";

        return (0);
    }

    // ************************************************************************
    void selectNewExtnMm() throws Exception {
    	
    	String tmpNewExtnMm = "";
    	String tmpNewExtnMm1 = "";
    	
    	tmpNewExtnMm = hBusiChiDate.substring(0, 5);                
    	tmpNewExtnMm = String.valueOf((Long.parseLong(tmpNewExtnMm) + 191100)) + "01";
    	tmpNewExtnMm1 = selectAddMonth(tmpNewExtnMm, 1);
        String calDate = String.valueOf(comm.lastDate(tmpNewExtnMm1));    	

        //valid_to欄位 => 年分加上new_extn_mm
        sqlCmd  = "select new_extn_mm ";
        sqlCmd += " from crd_item_unit ";
        sqlCmd += "where unit_code = ?  ";
        setString(1, emapUnitCode);        
        if (selectTable() > 0) {   
        	String hNewExtnMm = getValue("new_extn_mm"); 
        	hNewExtnMm = hNewExtnMm + String.format("%0" + (5 - hNewExtnMm.length()) + "d", 0);
        	calDate = String.valueOf((Integer.parseInt(calDate) + Integer.parseInt(hNewExtnMm)));	                
        }
              
        emapValidTo = calDate;
        hNewEndDate = calDate;
    }
    // ************************************************************************
    private String selectAddMonth(String inDate, int idx) throws Exception {
        selectSQL = "to_char(add_months(to_date( ? ,'yyyymmdd'), ? ),'yyyymmdd')  as out_date";
        daoTable = "sysibm.dual";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        setString(1, inDate);
        setInt(2, idx);
        selectTable();

        return getValue("out_date");
    }

    // ************************************************************************
    public int getGroupCard(String iGroupCode, String iCardType) throws Exception {
        tFirstFee = 0;
        tOtherFee = 0;
        tSupRate = 0.00;
        tSupEndMonth = 0;
        tSupEndRate = 0.00;

        if(debug == 1)
           showLogMessage("D", "", " chk fee 1=[" + iGroupCode + "]" + "[" + iCardType + "]");
        selectSQL = " first_fee       ,other_fee          , " 
                  + " sup_rate        ,sup_end_month      , sup_end_rate ";
        daoTable = "ptr_group_card";
        whereStr = "WHERE group_code =  ? " + "  and card_type  =  ? ";
        setString(1, iGroupCode);
        setString(2, iCardType);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            selectSQL = " first_fee       ,other_fee          , "
                    + " sup_rate        ,sup_end_month      , sup_end_rate ";
            daoTable = "ptr_group_card";
            whereStr = "WHERE group_code =  ? " + "  and card_type  =  ? ";
            setString(1, "0000");
            setString(2, iCardType);

            tmpInt = selectTable();

            if (notFound.equals("Y")) {
                return (4);
            }
        }

        tFirstFee = getValueInt("first_fee");
        tOtherFee = getValueInt("other_fee");
        tSupRate = getValueDouble("sup_rate");
        tSupEndMonth = getValueInt("sup_end_month");
        tSupEndRate = getValueDouble("sup_end_rate");

        if (debug == 1)
            showLogMessage("D", "", " 888 end mon=[" + tSupEndMonth + "]");

        return (0);
    }
// ************************************************************************
//public int sel_ptr_card_type() throws Exception 
//{
//        selectSQL = "decode(dc_curr_code , '' , '901' , dc_curr_code) dc_curr_code  ";
//        daoTable  = "ptr_bintable ";
//        whereStr  = "where bin_no || bin_no_2_fm || '0000' <= ? "
//                  + "and bin_no || bin_no_2_to || '9999' >= ? ";
//        setString(1, emap_card_no);
//        setString(2, emap_card_no);
//
//        int recCnt = selectTable();
//
//        if (notFound.equals("Y")) {
//            return (26);
//        }
//
//        emap_curr_code = getValue("dc_curr_code");
//if(DEBUG == 1) showLogMessage("D", "", " 888 select type =["+emap_curr_code+"]"+emap_card_type);
//
//        return (0);
//}
// ************************************************************************
    public int chkPmIssueDate() throws Exception {

        // 檢核附卡到期日是否小於正卡效期之多少個月內,打不同之rate
        if (tSupEndMonth <= 0)
            return (0);

        hBusiBusinessDate = getValue("BUSINESS_DATE");

        selectSQL = " months_between(to_date( ? ,'yyyymmdd') , to_date( ? ,'yyyymmdd')) as h_yy";
        daoTable = "sysibm.dual";

        setString(1, hBusiBusinessDate);
        setString(2, hNewEndDate);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_dual 4   error!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        tmpInt = getValueInt("h_yy");

        if ((tmpInt > 0) && (tmpInt <= tSupEndMonth)) {
            return (1);
        }

        return (0);
    }

    // ************************************************************************
//    public int chk_ptr_card_type() throws Exception {
//        h_error_flag = sel_ptr_card_type();
//        if (DEBUG == 1)
//            showLogMessage("D", "", " 888 chk_ptr step=[" + h_error_flag + "] " + emap_curr_code);
//        if (h_error_flag > 0) {
//            return (h_error_flag);
//        }
//
//        dc_curr_code_gl = "";
//        if (emap_curr_code.trim().compareTo("901") != 0) {
//            emap_dc_indicator = "Y";
//            if (emap_act_no_f.trim().length() == 0)
//                return (8);
//
//            selectSQL = " curr_code_gl";
//            daoTable = "ptr_currcode";
//            whereStr = "WHERE curr_code   =  ? ";
//            setString(1, emap_curr_code);
//
//            int recCnt = selectTable();
//
//            if (notFound.equals("Y")) {
//                return (9);
//           }
//
//            dc_curr_code_gl = getValue("curr_code_gl");
//
        /*
         * lai call appc see b002 no use sprintf(str600 ,
         * "PB37%2.2s%1.1s%-10.10s%-11.11s%-2.2s%-16.16s%-10.10s%-1.1s%-60.60s"
         * , " ","1", h_m_emap_apply_id[row].arr , h_m_emap_act_no_f[row].arr ,
         * h_dc_curr_code_gl.arr , h_curr_code.arr , h_m_emap_batchno[row].arr ,
         * "Y" ," "); if(strcmp(rtn_str , "00" ) == 0) insert_act_chkno(2); else
         * { insert_act_chkno(1); return(13); }
         */
        
        /*
        String rcvbuf = "";
        String sndbuf = "";

        snd_strc.txn_no     = "PB37";
        snd_strc.func_code  = "C";
        if (emap_corp_no.length() <= 0)
            snd_strc.autopay_id  = emap_pm_id;
        else
            snd_strc.autopay_id  = emap_corp_no;
        snd_strc.autopay_acct_no = emap_act_no_f;
        snd_strc.curr_code       = dc_curr_code_gl;
        snd_strc.card_no         = emap_batchno;
        snd_strc.p_seqno         = emap_curr_code;

        sndbuf = snd_strc.allText();
        */

//if(DEBUG == 1) showLogMessage("I", "", "  SEND BUF=["+sndbuf+"]"+sndbuf.length());
/*  lai test 
*/
//        rcvbuf = comm.COMM_APPC(sndbuf, h_eria_ref_ip, comcr.str2int(h_eria_port_no), 1024);

//if(DEBUG == 1) showLogMessage("I", "", "  RECV BUF=["+rcvbuf+"]"+rcvbuf.length());

//        h_rtn_str  = String.format("%2.2s"  , comc.getSubString(rcvbuf, 4));
//        h_rtn_desc = String.format("%60.60s", comc.getSubString(rcvbuf, 56));
//if(DEBUG == 1) showLogMessage("I", "", "  RTN="+h_rtn_str+","+ h_rtn_desc );
/* lai test
*/
//        if (h_rtn_str.length() == 0 || h_rtn_str.equals("  "))
//            h_rtn_str = "00";
        /*刪除有關會return(13)的所有檢核判斷
        if (rcvbuf.length() == 0) {
            showLogMessage("I", "", String.format("Warring: APPC IP=[%s] PORT[%d]", h_eria_ref_ip
                              , comc.str2int(h_eria_port_no)));
            return (13);
        }
        if (h_rtn_str.equals("00") == false)
            return (13);
       */
//       }        
//       
//      return (0);
//    }
    //***********************************************************************
    void selectEcsRefIpAddr() throws Exception {

        sqlCmd = "SELECT ref_ip, ";
        sqlCmd += "       port_no ";
        sqlCmd += "  FROM ecs_ref_ip_addr ";
        sqlCmd += " WHERE ref_ip_code = 'APPC' ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ecs_ref_ip_addr not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hEriaRefIp = getValue("ref_ip");
            hEriaPortNo = getValue("port_no");
        }
if(debug == 1) showLogMessage("I", "", "  APPC Port=["+ hEriaRefIp +"]"+ hEriaPortNo);
    }
    // ************************************************************************
    public int getCardIndicator() throws Exception {
        selectSQL = " card_indicator";
        daoTable = "ptr_acct_type";
        whereStr = "WHERE acct_type  =  ? ";
        setString(1, emapAcctType);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        tCardIndicator = getValue("card_indicator");
        if (debugD == 1)
            showLogMessage("I", "", "  888 indicator = [" + tCardIndicator + "]");

        return (0);
    }

    // ************************************************************************
    public int chkPtrCardType() throws Exception {
    	  hErrorFlag = selPtrCardType();
    	  if (debug == 1)
    	      showLogMessage("D", "", " 888 chk_ptr step=[" + hErrorFlag + "] " + emapCurrCode);
    	  if (hErrorFlag > 0) {
    	      return (hErrorFlag);
    	  }

    	  if (emapCurrCode.trim().compareTo("901") != 0) {
    	      emapDcIndicator = "Y";
    	      if (emapActNoF.trim().length() == 0)
    	          return (8);

    	      selectSQL = " curr_code_gl";
    	      daoTable = "ptr_currcode";
    	      whereStr = "WHERE curr_code   =  ? ";
    	      setString(1, emapCurrCode);

    	      int recCnt = selectTable();

    	      if (notFound.equals("Y")) {
    	          return (9);
    	     }
    	 }        
    	 
    	return (0);
    	}
    	//***********************************************************************
    	public int selPtrCardType() throws Exception 
    	{
    		String tmpBinNo = "";
    		String tmpBegSeqno = "";
    		String tmpEndSeqno = "";
    		String tmpCardNoFm = "";
    		String tmpCardNoTo = "";
    		   		
    		selectSQL = "bin_no, "
          		      + "beg_seqno, "
          		      + "end_seqno ";
 	        daoTable = "crd_cardno_range ";
 	        whereStr = "WHERE group_code   =  ?  " 
 	                 + "  and card_type    =  ?  " 
 	                 + "  and card_flag    = '1' "
 	                 + "  and post_flag    = 'Y' "
 	                 + "FETCH FIRST 1 ROW ONLY   ";
 	        setString(1, emapGroupCode);
 	        setString(2, emapCardType);

 	        int recCnt1 = selectTable();
 	        tmpBinNo = getValue("bin_no");
 	        tmpBegSeqno = getValue("beg_seqno");
 	        tmpEndSeqno = getValue("end_seqno");

 	        if (notFound.equals("Y")) {
 	            return (51);
 	        }
 	        
 	        tmpCardNoFm = tmpBinNo + tmpBegSeqno + "0";
 	        tmpCardNoTo = tmpBinNo + tmpEndSeqno + "9";
    		   		
    		selectSQL = "decode(dc_curr_code , '' , '901' , dc_curr_code) dc_curr_code  ";
    	    daoTable  = "ptr_bintable ";
    	    whereStr  = "where bin_no || bin_no_2_fm || '0000' <= ? "
    	              + "and bin_no || bin_no_2_to || '9999' >= ? ";
    	    setString(1, tmpCardNoFm);
    	    setString(2, tmpCardNoTo);

    	    int recCnt2 = selectTable();

    	    if (notFound.equals("Y")) {
    	        return (26);
    	    }

    	    emapCurrCode = getValue("dc_curr_code");
    	
    	    if(debug == 1) showLogMessage("D", "", " 888 select type =["+ emapCurrCode +"]"+ emapCardType);

    	    return (0);
    	}
    	// ************************************************************************
    public int getGblAcctType(int idx) throws Exception {

        if (debugD == 1)
            showLogMessage("I", "", "  888 gbl=[" + idx + "]" + emapGroupCode + " " + emapCardType);

        selectSQL = "a.acct_type       , a.card_indicator ";
        daoTable = "ptr_acct_type a,ptr_prod_type b ";
        if (idx == 0) {
            whereStr = "WHERE b.group_code  = ? " + "  and b.card_type   = ? " 
                     + "  and a.acct_type   = b.acct_type " 
                     + "FETCH FIRST 1 ROW ONLY  ";
            setString(1, emapGroupCode);
            setString(2, emapCardType);
        } else if (idx == 1) {
            whereStr = "WHERE b.group_code  = ? " 
                     + "  and a.acct_type   = b.acct_type " + "FETCH FIRST 1 ROW ONLY  ";
            setString(1, emapGroupCode);
        } else if (idx == 2) {
            whereStr = "WHERE b.card_type   = ? " 
                     + "  and a.acct_type   = b.acct_type " + "FETCH FIRST 1 ROW ONLY  ";
            setString(1, emapCardType);
        }

        int recCnt = selectTable();

        tAcctType = getValue("acct_type");
        tCardIndicator = getValue("card_indicator");

        if (notFound.equals("Y")) {
            if (debugD == 1)
                showLogMessage("I", "", "  chk type11.222=[ " + recCnt + " ]");
            return (1);
        }

        if (debugD == 1)
            showLogMessage("I", "", "  chk type11.2=[" + getValue("acct_type") + "]");
        return (0);
    }

    // ************************************************************************
    public int getAcctType() throws Exception {
        emapComboIndicator = getComboIndicator(emapGroupCode);

        if (emapGroupCode.trim().compareTo("0000") != 0) {
            tmpInt = getGblAcctType(0);
            if (debug == 1)
                showLogMessage("D", "", " gbl rtn1=[" + tmpInt + "]");
            if (tmpInt == 0)
                return (0);
            else {
                tmpInt = getGblAcctType(1);
                if (debug == 1)
                    showLogMessage("D", "", " gbl rtn2=[" + tmpInt + "]");
                if (tmpInt == 0)
                    return (0);
            }
        }

        if (emapCardType.trim().length() > 0) {
            tmpInt = getGblAcctType(2);
            if (debug == 1)
                showLogMessage("D", "", " gbl rtn3=[" + tmpInt + "]");
            if (tmpInt == 0)
                return (0);

        }

        if (tAcctType.trim().length() < 1)
            return (1);

        return (0);
    }

    // ************************************************************************
    public int chkBusAnnualFee(String iFeeCode) throws Exception {

if(debug == 1) showLogMessage("D",""," 88873="+iFeeCode+","+ emapCorpNo);
        if (emapCorpNo.trim().length() <= 0)
            return (1);

        selectSQL = " first_fee_amt ";
        daoTable = "ptr_corp_fee";
        whereStr = "WHERE card_type  =  ? " + "  and corp_no    =  ? ";
        setString(1, emapCardType);
        setString(2, emapCorpNo);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            tmpInt = getGroupCard(emapGroupCode, emapCardType);
            if (tmpInt != 0)
                return (5);
        }

        emapStandardFee = getValueInt("first_fee_amt");

        // * 第一年免年費或終身免年費

        emapAnnualFee = emapStandardFee;
        if (iFeeCode.substring(0, 1).equals("1") || iFeeCode.substring(0, 1).equals("2")
                || iFeeCode.substring(0, 1).equals("3") || iFeeCode.substring(0, 1).equals("4")
                || iFeeCode.substring(0, 1).equals("5") || iFeeCode.substring(0, 1).equals("6")
                || iFeeCode.substring(0, 1).equals("7") || iFeeCode.substring(0, 1).equals("8")
                || iFeeCode.substring(0, 1).equals("9") || iFeeCode.substring(0, 1).equals("Z")) {
            emapAnnualFee = 0;
        }

        emapFeeReasonCode = "A";
        if (emapAnnualFee != emapStandardFee)
            emapFeeReasonCode = "B";

        return (0);
    }

    // ************************************************************************
    public int chkAnnualFee(int iSupFlg, String iFeeCode) throws Exception {

        int pAnnualFee = 0;

        emapFinalFeeCode = iFeeCode;
        // 當不同之group_code或card_type時,需從新抓取年費參數檔資料
        // 抓取年費參數檔
        tmpInt = getGroupCard(emapGroupCode, emapCardType);
        if (debug == 1)
            showLogMessage("D", "", " chk fee=[" + tmpInt + "]" + "[" + iFeeCode + "]");
        if (tmpInt != 0)
            return (4);

        if (iSupFlg == 1) // 附卡
        {
            // 檢核附卡到期日是否小於正卡效期之多少個月內,打不同之rate
            tmpInt = chkPmIssueDate();
            if (tmpInt == 1) {
                pAnnualFee = (int) (tFirstFee * (tSupEndRate / 100));
            } else {
                pAnnualFee = (int) (tFirstFee * (tSupRate / 100));
            }
        } else // 正卡
        {
            pAnnualFee = tFirstFee;
        }

        emapStandardFee = pAnnualFee;

        emapAnnualFee = emapStandardFee;
        if (iFeeCode.substring(0, 1).equals("1") || iFeeCode.substring(0, 1).equals("2")
                || iFeeCode.substring(0, 1).equals("3") || iFeeCode.substring(0, 1).equals("4")
                || iFeeCode.substring(0, 1).equals("5") || iFeeCode.substring(0, 1).equals("6")
                || iFeeCode.substring(0, 1).equals("7") || iFeeCode.substring(0, 1).equals("8")
                || iFeeCode.substring(0, 1).equals("9") || iFeeCode.substring(0, 1).equals("Z")) {
            emapAnnualFee = 0;
        }

        emapFeeReasonCode = "A";
        if (emapAnnualFee != emapStandardFee)
            emapFeeReasonCode = "B";
        if (debug == 1)
            showLogMessage("D", "", " chk fee end=[" + emapAnnualFee + "][" + emapStandardFee + "]");

        return (0);
    }

    // ************************************************************************
    private int chkData() throws Exception {
        tmpChar = getValue("eng_name");
        if (debug == 1)
            showLogMessage("D", "", " 888 eng_name=[" + tmpChar + "]");
        if (tmpChar.trim().length() <= 0) {
            return (6);
        }
        tmpChar = getValue("valid_fm");
        tmpChar1 = emapValidTo;
        if (debug == 1)
            showLogMessage("D", "", " 888 fm=[" + tmpChar + "]" + "[" + tmpChar1 + "]");
        if (comc.getSubString(tmpChar, 0, 6).compareTo(comc.getSubString(tmpChar1, 0, 6)) > 0) {
            return (23);
        }
        if (comc.getSubString(tmpChar, 0, 6).compareTo(comc.getSubString(sysDate, 0, 6)) < 0) {
            return (24);
        }
        if (comc.getSubString(tmpChar1, 0, 6).compareTo(comc.getSubString(sysDate, 0, 6)) < 0) {
            return (24);
        }

        return (0);
    }

    // ************************************************************************
    //此條件已刪除
    private int chkMsisdn() throws Exception {

        // 若手機門號欄位有值，檢查第一字元是否為0，不WORK:emap_msisdn[row].arr[0]恆為空
        if (emapMsisdn.trim().length() > 0 && !emapMsisdn.substring(0, 1).equals("0")) {
            return (14);
        }

        // 若ECS設定的此卡片se_type與APS傳入的不一致則回傳error code:14*/
        if (ptrServiceType.trim().compareTo(emapServiceType.trim()) != 0) {
            return (14);
        }

        return (0);
    }
    // ************************************************************************
    private int chkSourceCode() throws Exception 
    {
        selectSQL = "count(*) as all_cnt";
        daoTable = "ptr_src_code";
        whereStr = "WHERE source_code              = ?   ";

        setString(1, emapSourceCode);

        int recCnt = selectTable();

        if (debugD == 1) showLogMessage("I", "", "  source=["+getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") == 0)
        	return (1);

        return (0);
    }
    // ************************************************************************
    private int chkAddrIsEmpty(String addr1, String addr2, String addr5) {
    	if ( addr1 == null || addr1.isEmpty()||
    		addr2 == null || addr2.isEmpty() ||
    		addr5 == null || addr5.isEmpty()) {
			return 1;
		}
    	return 0;
    }
    // ************************************************************************
    private int chkStmtCycle() throws Exception {

        selectSQL = "count(*) as all_cnt";
        daoTable = "ptr_workday";
        whereStr = "WHERE stmt_cycle = ? ";

        setString(1, emapStmtCycle);
        int recCnt = selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") < 1) {
        	return (7);
        }

        return (0);
    }

    // ************************************************************************
    private int chkAcctType() throws Exception {

        if (debugD == 1)
            showLogMessage("I", "", "  chk type11.1=[" + emapGroupCode + "]");

        if (emapGroupCode.trim().compareTo("0000") != 0) {
            selectSQL = "a.acct_type ";
            daoTable = "ptr_acct_type a,ptr_prod_type b";
            whereStr = "WHERE b.group_code  = ? " + "  and a.acct_type   = b.acct_type "
                    + "  and (b.card_type  = '' or b.card_type = '') ";

            setString(1, emapGroupCode);
            int recCnt = selectTable();
            if (debugD == 1)
                showLogMessage("I", "", "  chk type11.210=[" + recCnt + "]");
            if (getValue("acct_type").trim().length() > 0) {
                return (0);
            }
        }

        selectSQL = "a.acct_type ";
        daoTable = "ptr_acct_type a,ptr_prod_type b";
        whereStr = "WHERE b.card_type  = ? " + "  and a.acct_type  = b.acct_type ";

        setString(1, emapCardType);
        int recCnt = selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  chk type11.211=[" + recCnt + "]");
        if (notFound.equals("Y")) {
            return (3);
        }
        if (debugD == 1)
            showLogMessage("I", "", "  chk type2=[" + emapGroupCode + "]");
        tmpChar = getValue("acct_type").trim();
        if (emapAcctType.compareTo(tmpChar.trim()) != 0) {
            return (3);
        }

        return (0);
    }

    // ************************************************************************
    private int supCardProcess() throws Exception {

        hLineOfCreditAmt = 0;
        hErrorFlag = 0;
        tSupCd = 0;
        // 檢查是否為附卡檢查,其正卡是否有效
        if (emapApplyId.trim().compareTo(emapPmId.trim()) == 0) {
if (debugD == 1) showLogMessage("I", "", "  888888 正卡=[" + emapGroupCode + "]");
            emapPmBirthday = emapBirthday;
            emapSupBirthday = "";
        } else {
            if (debugD == 1)
                showLogMessage("I", "", "  888888 附卡=[" + emapPmId + "]" + "[" + emapBirthday + "]");

            emapSupBirthday = emapBirthday;
            emapPmBirthday = "";

            tSupCd = 1;
            if (debugD == 1)
                showLogMessage("I", "", "  888888 1.1=[" + emapPmId + "]");
            hNewEndDate = "";
            hLineOfCreditAmt = 0;
            hErrorFlag = checkPmCrdValid();

            if (debugD == 1)
                showLogMessage("I", "", "  888888 1.2=[" + hErrorFlag + "]");
            if (hErrorFlag != 0) {
                // 是否附卡同時一起申請
                hErrorFlag = checkPmEmap();
            }
            if (debugD == 1)
                showLogMessage("I", "", "  888888 1.3=[" + hErrorFlag + "]");
            // totalCnt++;
            if (hErrorFlag == 0) {
                // 正卡需提前續卡
                advanceChgCard();
                if (hLineOfCreditAmt > 0)
                    emapCreditLmt = hLineOfCreditAmt;
            }
        }
      return (hErrorFlag);
    }
    // ************************************************************************
    private int checkPmCrdValid() throws Exception {

        if (debugD == 1)
            showLogMessage("I", "", "  8888 chk_pm=[" + emapPmId + "]");

        selectSQL = " card_no      ,acno_p_seqno      ,group_code  , " 
                  + " current_code ,new_beg_date ,new_end_date, "
                  + " fee_code     ,acct_type    ,id_no_code  , " + " acct_type    ";
        daoTable  = "crd_card a, crd_idno b";
        whereStr  = "WHERE b.id_no        =  ? " 
                  + "  and a.id_p_seqno   =  b.id_p_seqno " 
                  + "  and a.card_type    =  ? "
                  + "  and a.group_code   =  ? " 
                  + "  and a.current_code =  '0' ";
        setString(1, emapPmId);
        setString(2, emapCardType);
        setString(3, emapGroupCode);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (2);
        }

        emapAcctType = getValue("acct_type");
        emapValidTo = getValue("new_end_date");
        emapMajorCardNo = getValue("card_no");
        emapMajorValidFm = getValue("new_beg_date");
        emapMajorValidTo = getValue("new_end_date");
        hNewEndDate = getValue("new_end_date");
        emapPmIdCode = getValue("id_no_code");

        if (debugD == 1)
            showLogMessage("I", "", "  888888 2.2=[" + emapMajorCardNo + "]");
        tmpInt = selectCrdIdno(emapPmId, emapPmIdCode);
        if (debugD == 1)
            showLogMessage("I", "", "  888888 2.3=[" + tmpInt + "]");
        if (tmpInt > 0)
            return (tmpInt);
        emapPmBirthday = getValue("birthday");

        // 不為有效卡
        if (!getValue("current_code").trim().equals("0"))
            return (2);

        // 帳戶主檔資料找不到
        tmpInt = selectActAcno(getValue("acno_p_seqno"));
        if (tmpInt > 0)
            return (tmpInt);
        hLineOfCreditAmt = getValueInt("line_of_credit_amt");

        tmpInt = chkAcctType();
        if (tmpInt > 0)
            return (3);

        return (0);
    }

    // ************************************************************************
    // 檢核combo卡舊金融帳戶是否已截清(ACT_COMBO_M_JRNL)
    // 若未結清,則不可製卡 2002/05/15
    // ************************************************************************
    private int chkDupAcctNo() throws Exception {

        // 附卡only需檢核帳號是否與正卡相符 2005/05/19
        // 三合一卡h_card_combo_indicator 改不等於N, T為三合一卡 Y為combo卡
        if (emapApplyId.trim().compareTo(emapPmId.trim()) != 0) {
            if (emapComboIndicator.trim().compareTo("N") != 0 && emapMajorCardNo.trim().length() > 0) {
                selectSQL = " combo_acct_no ";
                daoTable = "crd_card";
                whereStr = "WHERE card_no       =  ?   and combo_acct_no =  ? FETCH FIRST 1 ROW ONLY  ";

                if (debugD == 1)
                    showLogMessage("I", "", "  888888 3.3=[" + emapPmId + "]");
                setString(1, emapMajorCardNo);
                setString(2, emapActNo);

                int recordCnt = selectTable();

                if (notFound.equals("Y")) {
                    return (27);
                }

                cardComboAcctNo = getValue("combo_acct_no");
            }
        }

        // 當為歡喜卡才往下檢查 lai
        if (emapComboIndicator.trim().compareTo("Y") != 0)
            return (0);

        // 附卡only不需檢核帳戶是否結清 2002/08/13
        if (emapApplyId.trim().compareTo(emapPmId.trim()) != 0)
            return (0);

        // 附卡only 已 return(0) , 只剩正卡
        selectCrdIdnoB(emapApplyId, emapBirthday);
        emapApplyIdCode = idnoIdNoCode.trim();
        String ckAcctKey = emapApplyId + emapApplyIdCode.trim();
//        if (idnoIdPSeqno.trim().length() > 0) // sqlca.sqlcode == 0
//        {
//            selectSQL = " cash_use_balance , " + " combo_acct_no , acno_p_seqno ";
//            daoTable = "act_combo_m_jrnl a, act_acno b";
//            whereStr = "WHERE b.acct_type  =  ? " + "  and b.acct_key   =  ? " + "  and a.p_seqno    =  b.acno_p_seqno "
//                    + "FETCH FIRST 1 ROW ONLY  ";
//
//            if (debugD == 1)
//                showLogMessage("I", "", "  888888 3.3=[" + emapAcctType + "]");
//            setString(1, emapAcctType);
//            setString(2, ckAcctKey);
//
//            int recordCnt = selectTable();
//
//            if (!notFound.equals("Y")) {
//                if (getValueDouble("cash_use_balance") > 0
//                        && getValue("combo_acct_no").trim().compareTo(emapActNo.trim()) != 0)
//                    return (27);
//            }
//
//            String ckPSeqno = getValue("acno_p_seqno");
//            // 限制一個A/C僅能有一個Fancy有效帳戶 */
//            if (chkDupAcctNo1(ckPSeqno, ckAcctKey) != 0)
//                return (27);
//
//            // 同一個A/C若停卡換存款帳號申請時，必須動用餘額為0才行 上面已有檢核
//            // 同一個存款帳號要申請不同A/C時 ，必須動用餘額為0才行 */
//            if (chkDupAcctNo2(ckPSeqno, ckAcctKey) != 0)
//                return (27);
//
//            // 除特殊設定之Group_code/Source_code外(參數1)，同一存款帳號不能同時在
//            // 二個不同的A/C存在活卡
//            if (chkDupAcctNow3(ckPSeqno, ckAcctKey) != 0)
//                return (27);
//
//        }
        if (debugD == 1)
            showLogMessage("I", "", "  888888 3.4=[" + hNewEndDate + "]");

        return (0);
    }

    // ************************************************************************
    private int chkDupAcctNo1(String ckPSeqno, String ckAcctKey) throws Exception {

        selectSQL = "count(*) as all_cnt";
        daoTable  = "crd_card";
        whereStr  = "WHERE acno_p_seqno                  = ?   " 
                  + "  and current_code             = '0' "
                  + "  and nvl(combo_indicator,'N') = 'Y' " // 僅檢查歡喜卡
                  + "  and combo_acct_no           <> '' ";

        setString(1, ckPSeqno);
        int recCnt = selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  no 1 =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0)
            return (1);

        selectSQL = "count(*) as all_cnt";
        daoTable  = "crd_emboss";
        whereStr  = "WHERE acct_type        = ?   " 
                  + "  and acct_key         = ?   "
                  + "  and in_main_date     = ''  " 
                  + "  and in_main_error    = ''  "
                  + "  and reject_code      = ''  " 
                  + "  and decode(combo_indicator,'','N',combo_indicator) = 'Y' ";// 僅檢查歡喜卡

        setString(1, emapAcctType);
        setString(2, ckPSeqno);
        recCnt = selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  no 1 =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0)
            return (1);

        return (0);
    }

    // ************************************************************************
    private int chkDupAcctNo2(String ckPSeqno, String ckAcctKey) throws Exception {

        selectSQL = "count(*) as all_cnt";
        daoTable  = " act_combo_m_jrnl";
        whereStr  = "WHERE cash_use_balance        > 0    " 
                  + "  and (p_seqno,acct_type) in        "
                  + "      (select p_seqno ,acct_type    " 
                  + "         from crd_card               "
                  + "        where combo_acct_no    = ?   " 
                  + "          and combo_indicator  = 'Y' " // 僅檢查歡喜卡
                  + "          and acct_type       <> ?   "
                  + "          and combo_acct_no   <> '') ";

        setString(1, emapActNo);
        setString(2, emapAcctType);

        int recCnt = selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  no 2 =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0)
            return (1);

        return (0);
    }

    // ************************************************************************
    private int chkDupAcctNow3(String ckPSeqno, String ckAcctKey) throws Exception {

        selectSQL = "count(*) as all_cnt";
        daoTable  = "crd_card";
        whereStr  = "WHERE combo_acct_no      = ?   " 
                  + "  and current_code       = '0' " 
                  + "  and decode(combo_indicator,'','N',combo_indicator) = 'Y' " //僅檢查歡喜卡
                  + "  and acct_type         <> ?  "
                  + "  and combo_acct_no     <> '' ";

        setString(1, emapActNo);
        setString(2, emapAcctType);

        int recCnt = selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  no 3 =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") < 1)  return (1);
      
        return (0);
    }

    // ************************************************************************
    private int checkPmEmap() throws Exception {

        selectSQL = " card_no    ,apply_id_code , birthday   , " 
                  + " valid_fm   ,valid_to      , credit_lmt , "
                  + " fee_code ";
        daoTable = "crd_emap_tmp";
        whereStr = "WHERE apply_id     =  ? " 
                 + "  and group_code   =  ? "
                 + "FETCH FIRST 1 ROW ONLY  ";

        setString(1, emapPmId);
        setString(2, emapGroupCode);

        int recordCnt = selectTable();
        if (debugD == 1)
           {
            showLogMessage("I", "", "  88888 pm 3.3=[" + emapPmId + "]" + recordCnt);
            showLogMessage("I", "", "              =[" + emapCardType + "]" + emapGroupCode);
           }

        if (notFound.equals("Y")) {
            return (2);
        }

        emapMajorCardNo = getValue("card_no");
        emapMajorValidFm = getValue("valid_fm");
        emapMajorValidTo = getValue("valid_to");
        hNewEndDate = getValue("valid_to");
        emapPmIdCode = getValue("apply_id_code");
        emapPmBirthday = getValue("birthday");

        if (debugD == 1)
           {
            showLogMessage("I", "", "  888888 card=[" + getValue("card_no") + "]");
            showLogMessage("I", "", "  888888 3.4=[" + emapMajorCardNo + "]");
            showLogMessage("I", "", "            =[" + hNewEndDate + "]");
           }

        return (0);
    }

    // ************************************************************************
    private int advanceChgCard() throws Exception {

        if (debugD == 1)
            showLogMessage("I", "", "  888888 4.1=[" + hNewEndDate + "]");

        selectSQL = " months_between(to_date( ? ,'yyyymmdd'),sysdate) as diff_month ";
        daoTable = "sysibm.dual";

        setString(1, hNewEndDate);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            return (2);
        }

        int hDiffMonth = getValueInt("diff_month");

        if ((hDiffMonth >= 0) && (hDiffMonth <= 6)) {
            selectSQL = " (to_number(substr( ?,1,4)) + ?)|| substr( ?,5,2)||'01' as end_date ";
            daoTable = "sysibm.dual";

            setString(1, hNewEndDate);
            setInt(2, ptrExtnYear);
            setString(3, hNewEndDate);

            recordCnt = selectTable();

            tmpChar = getValue("end_date");
            if (debug == 1)
                showLogMessage("D", "", " 888 4.8 step=[" + tmpChar + "] ");

            selectSQL = " to_char(last_day(to_date( ?,'yyyymmdd')),'yyyymmdd') as end_date1 ";
            daoTable = "sysibm.dual";

            setString(1, tmpChar);

            recordCnt = selectTable();

            emapMajorChgFlag = "Y";
            emapValidTo = getValue("end_date1");
            if (debug == 1)
                showLogMessage("D", "", " 888 4.9 step=[" + emapValidTo + "] ");
        }

        return (0);
    }

    // ************************************************************************
    private Map processDataRecord(String[] row, String[] dt) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        int j = 0;
        for (String s : dt) {
            map.put(s.trim(), row[i]);
            // if(DEBUG == 1) showLogMessage("D",""," Data=" + s + ":[" + row[i]
            // + "]");
            i++;
        }
        return map;
    }

    // ************************************************************************
    public int updateCrEmapErr() throws Exception {

if (debug == 1) showLogMessage("D", "", " UPADTE Err=["+ hErrorFlag +"]"+totalErr);
        
		emapCheckCode = "D"+String.format("%02d", hErrorFlag);
        
        updateSQL = " check_code       = ? , " + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
                + " mod_pgm          = ?   ";
        daoTable = "crd_emap_tmp";
        whereStr = "WHERE rowid       = ? ";

        setString(1, emapCheckCode);
        setString(2, sysDate + sysTime);
        setString(3, javaProgram);
        setRowId(4, emapRowid);

        int recCnt = updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emap_err error[not find]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int selectActAcno(String ckPSeqno) throws Exception {
        selectSQL = " line_of_credit_amt ";
        daoTable = "act_acno";
        whereStr = "WHERE acno_p_seqno    =  ? ";

        setString(1, ckPSeqno);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (2);
        }

        return (0);
    }

    // ************************************************************************
    public int selectCrdIdno(String ckId, String ckIdCode) throws Exception {
        selectSQL = " birthday   ";
        daoTable = "crd_idno";
        whereStr = "WHERE id_no      =  ? " + "  and id_no_code =  ? ";
        setString(1, ckId);
        setString(2, ckIdCode);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (2);
        }

        return (0);
    }

    // ************************************************************************
    public int chkIdBirthday(String ckId, String ckBirthday) throws Exception {

if (debug == 1) showLogMessage("D", "", " CHK BIRTH 1=[" + ckId + "]" + ckBirthday);
        selectSQL = " count(*)  as idno_cnt   ";
        daoTable = "crd_idno";
        whereStr = "WHERE id_no      =  ? " + "  and birthday   =  ? ";
        setString(1, ckId);
        setString(2, ckBirthday);

        int recCnt = selectTable();

        if (getValueInt("idno_cnt") < 1) {
            selectSQL = " count(*)  as idno_cnt1  ";
            daoTable = "crd_idno";
            whereStr = "WHERE id_no      =  ? ";
            setString(1, ckId);

            recCnt = selectTable();
if (debug == 1) showLogMessage("D", "", " CHK BIRTH 2=[" + ckId + "]" + getValueInt("idno_cnt1"));

            if (getValueInt("idno_cnt1") > 0) {
                return (15);
            }
        }

        return (0);
    }

    // ************************************************************************
    public int selectCrdIdnoB(String ckId, String ckBirthday) throws Exception {
        idnoIdPSeqno = "";
        idnoIdNoCode = "";

        selectSQL = " id_p_seqno , id_no_code ";
        daoTable = "crd_idno";
        whereStr = "WHERE id_no      =  ? " + "  and birthday   =  ? ";
        setString(1, ckId);
        setString(2, ckBirthday);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        idnoIdPSeqno = getValue("id_p_seqno");
        idnoIdNoCode = getValue("id_no_code");

        return (0);
    }
    // ************************************************************************
    public int chkBirthday(String ckId, String ckBirthday) throws Exception {
        String birthdayTmp = "";
        
        selectSQL = " birthday ";
        daoTable = "crd_idno";
        whereStr = "WHERE id_no  =  ? " ;
        setString(1, ckId);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (0);
        }
        else {
        	birthdayTmp = getValue("birthday");
        	if(birthdayTmp.equals(ckBirthday)) {
        		return (0);
        	}
        	else {
        		return (28);
        	}
        }

    }
    // ************************************************************************
    public int chkRateYear() throws Exception {
        String specialCardRateFlag = "";
        double revolveIntRateYear = 0.0;
        double revolveIntRateDay = 0.0;
        
        selectSQL = " special_card_rate_flag,revolve_int_rate_year,rcrate_day ";
        daoTable = "ptr_group_code";
        whereStr = "WHERE group_code =  ? " ;
        setString(1, emapGroupCode);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (65);
        }
        else {
        	specialCardRateFlag = getValue("special_card_rate_flag");
        	revolveIntRateYear = getValueDouble("revolve_int_rate_year");
        	revolveIntRateDay = getValueDouble("rcrate_day");
        	
        	if(specialCardRateFlag.equals("Y")) {
        		if(emapRevolveIntRateYear ==revolveIntRateYear) {
        			emapSpecialCardRate = revolveIntRateDay;
        			return (0);
        		}
        		else {
        			return(21);
        		}
        	}
        	else {
        		double rcrateDay = 0.0;
                selectSQL = " rcrate_day ";
                daoTable = "ptr_rcrate";
                whereStr = "WHERE rcrate_year  =  ? " ;
                setDouble(1, emapRevolveIntRateYear);
                
                recCnt = selectTable();

                if (notFound.equals("Y")) {
                    return (22);
                }
                else {
                	rcrateDay = getValueDouble("rcrate_day");
                	emapRevolveIntRate = rcrateDay;
                	return (0);
             
                }
        		
        	}
        }

    }
    // ************************************************************************
    public int chkBusinessCode() throws Exception {
        
        selectSQL = " count(*) as all_cnt ";
        daoTable = "crd_message";
        whereStr = "WHERE  msg_type  =  ? " + "  and msg_value  =  ? ";
        setString(1, "BUS_CODE");
        setString(2, emapBusinessCode);

        int recCnt = selectTable();
        if (getValueInt("all_cnt") == 0)
        	return (14);
        	
        return (0);

    }
    // ************************************************************************
    public int chkRegBankNo() throws Exception {
        
        selectSQL = " count(*) as all_cnt ";
        daoTable = "gen_brn";
        whereStr = "WHERE  branch  =  ? " ;
        setString(1, emapRegBankNo);

        int recCnt = selectTable();

        if (getValueInt("all_cnt") == 0)
        	return (15);
        	
        return (0);
        
    }
    // ************************************************************************
    public int chkBranch() throws Exception {
        
        selectSQL = " count(*) as all_cnt ";
        daoTable = "gen_brn";
        whereStr = "WHERE  branch  =  ? " ;
        setString(1, emapBranch);

        int recCnt = selectTable();

        if (getValueInt("all_cnt") == 0)
        	return (17);
        	
        return (0);

    }
    // ************************************************************************
    public int chkCorpNo() throws Exception {
        
        selectSQL = " count(*) as all_cnt ";
        daoTable = "crd_corp";
        whereStr = "WHERE  corp_no  =  ? " ;
        setString(1, emapCorpNo);

        int recCnt = selectTable();

        if (getValueInt("all_cnt") == 0)
        	return (16);
        	
        return (0);

    }
    // ************************************************************************
    public int chkGenSameGroupType() throws Exception {
        /*檢查是否有同卡種、團代、認同集團碼、ID,且為活卡*/
        /*20150416 需同時檢查crd_emboss是否有相同條件待製卡 有可能新制跟重制同時進來 */
    	/*20210903新增認同集團碼條件*/
    	count = 0;
        sqlCmd = "select count(*) cnt";
        sqlCmd += " from crd_card a, crd_idno b ";
        sqlCmd += "where a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += "  and a.current_code = '0' ";
        sqlCmd += "  and b.id_no = ? ";
        sqlCmd += "  and a.group_code = ? ";
        sqlCmd += "  and a.card_type = ? ";
        sqlCmd += "  and a.unit_code = ? ";
        setString(1, emapApplyId);
        setString(2, emapGroupCode);
        setString(3, emapCardType);
        setString(4, emapUnitCode);
        int recordCnt = selectTable();
        if(debug == 1) showLogMessage("I","", " 666 reject 0 cnt=["+recordCnt+"]"+ emapCardType +","+ emapGroupCode +","+ emapApplyId);
        if (notFound.equals("Y")) {}
        else{
            count = getValueInt("cnt");
            if(debug == 1) showLogMessage("I","", " 666 reject 01 cnt=["+count+"]");
            if (count == 0) {
                sqlCmd = "select count(*) cnt";
                sqlCmd += " from crd_emboss  ";
                sqlCmd += "where apply_id = ? ";
                sqlCmd += "  and group_code = ? ";
                sqlCmd += "  and card_type = ? ";
                sqlCmd += "  and unit_code = ? ";
                sqlCmd += "  and in_main_date  = '' ";
                sqlCmd += "  and in_main_error = '' ";
                sqlCmd += "  and reject_code   = '' ";
                setString(1, emapApplyId);
                setString(2, emapGroupCode);
                setString(3, emapCardType);
                setString(4, emapUnitCode);
                recordCnt = selectTable();
                if (recordCnt > 0) {
                    count = getValueInt("cnt");
                }
            }
        }

        if (recordCnt > 0) {
            /* for 新製卡檢核 */
            if (emapNcccType.equals("1")) {
            	if (count != 0) {
            		return(10);
                }
            	else {
            		return(0);
            	}
            }else {
            	return(0);
            }
        }return(0);

    }
    // ************************************************************************
    public int chkBusSameGroupType() throws Exception {
        /*檢查是否有同卡種、團代、認同集團碼、統編,且為活卡*/
        /*20150416 需同時檢查crd_emboss是否有相同條件待製卡 有可能新制跟重制同時進來 */
    	/*20210903新增認同集團碼條件*/
    	count = 0;
        sqlCmd = "select count(*) cnt";
        sqlCmd += " from crd_card a, crd_corp b, crd_idno c ";
        sqlCmd += "where a.corp_p_seqno = b.corp_p_seqno ";
        sqlCmd += "  and a.id_p_seqno = c.id_p_seqno ";
        sqlCmd += "  and a.current_code = '0' ";
        sqlCmd += "  and b.corp_no = ? ";
        sqlCmd += "  and c.id_no = ? ";
        sqlCmd += "  and a.group_code = ? ";
        sqlCmd += "  and a.card_type = ?  ";        
        sqlCmd += "  and a.unit_code = ? ";
        setString(1, emapCorpNo);
        setString(2, emapApplyId);
        setString(3, emapGroupCode);
        setString(4, emapCardType);
        setString(5, emapUnitCode);
        int recordCnt = selectTable();
        if(debug == 1) showLogMessage("I","", " 666 reject 0 cnt=["+recordCnt+"]"+ emapCardType +","+ emapGroupCode +","+ emapCorpNo+","+ emapApplyId);
        if (notFound.equals("Y")) {}
        else{
            count = getValueInt("cnt");
            if(debug == 1) showLogMessage("I","", " 666 reject 01 cnt=["+count+"]");
            if (count == 0) {
                sqlCmd = "select count(*) cnt";
                sqlCmd += " from crd_emboss  ";
                sqlCmd += "where corp_no = ? ";
                sqlCmd += "  and apply_id = ? ";
                sqlCmd += "  and group_code = ? ";
                sqlCmd += "  and card_type = ? ";
                sqlCmd += "  and unit_code = ? ";
                sqlCmd += "  and in_main_date  = '' ";
                sqlCmd += "  and in_main_error = '' ";
                sqlCmd += "  and reject_code   = '' ";                
                setString(1, emapCorpNo);
                setString(2, emapApplyId);
                setString(3, emapGroupCode);
                setString(4, emapCardType);                                
                setString(5, emapUnitCode);
                recordCnt = selectTable();
                if (recordCnt > 0) {
                    count = getValueInt("cnt");
                }
            }
        }

        if (recordCnt > 0) {
            /* for 新製卡檢核 */
            if (emapNcccType.equals("1")) {
            	if (count != 0) {
            		return(10);
                }
            	else {
            		return(0);
            	}
            }else {
            	return(0);
            }
        }return(0);

    }
    // ************************************************************************
    public int chkEngName() throws Exception {
        int englen = 0;
        for (englen = 0; englen < emapEngName.length(); englen++) {
            if (emapEngName.toCharArray()[englen] >= 65 && emapEngName.toCharArray()[englen] <= 90
                    || emapEngName.toCharArray()[englen] == 32 || emapEngName.toCharArray()[englen] == 0
                    || emapEngName.toCharArray()[englen] == 39 || emapEngName.toCharArray()[englen] == 44
                    || emapEngName.toCharArray()[englen] == 45 || emapEngName.toCharArray()[englen] == 46
                    || emapEngName.toCharArray()[englen] == 47) {
            } else {
            	return(11);
            }
        }
        return(0);
    }
    // ************************************************************************
    public int updateCrdEmapTmp() throws Exception {

        if (cardComboAcctNo.trim().length() > 0)
            emapActNo = cardComboAcctNo;

        if (emapPmIdCode.trim().length() < 1)
            emapPmIdCode = "0";

        if (emapRiskBankNo.trim().length() < 1)
            emapRiskBankNo = "009";

        // showLogMessage("I",""," UPDATE check=[" + card_combo_acct_no + "]");

        emapCheckCode = "000";

        updateSQL = " acct_type        = ? , " + " check_code       = ? , " 
                  + " pm_id_code       = ? , " + " pm_birthday      = ? , " 
                  + " sup_birthday     = ? , " + " valid_to         = ? , "
                  + " major_card_no    = ? , " + " major_valid_fm   = ? , " 
                  + " major_valid_to   = ? , " + " major_chg_flag   = ? , " 
                  + " credit_lmt       = ? , " + " standard_fee     = ? , "
                  + " annual_fee       = ? , " + " fee_code         = ? , " 
                  + " risk_bank_no     = ? , " + " final_fee_code   = ? , " 
                  + " fee_reason_code  = ? , " + " combo_indicator  = ? , "
                  + " act_no           = ? , " + " ic_flag          = ? , " 
                  + " service_code     = ? , " + " unit_code        = ? , " 
                  + " dc_indicator     = ? , " + " curr_code        = ? , "
                  + " electronic_code  = ? , " + " card_type        = ? , "
                  + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , " 
                  + " mod_pgm          = 'CrdB002',"
                  + " special_card_rate  = ? , "
                  + " revolve_int_rate = ?   ";
        daoTable  = "crd_emap_tmp";
        whereStr  = "WHERE rowid    = ? ";

//        if (emap_unit_code.length() == 0)
//            emap_unit_code = "0000";
        if (emapPmIdCode.length() == 0)
            emapPmIdCode = "0";

if (debug == 1) showLogMessage("D", "", " UUUU  unit =[" + emapUnitCode + "]");
        setString(1, emapAcctType);
        setString(2, emapCheckCode);
        setString(3, emapPmIdCode);
        setString(4, emapPmBirthday);
        setString(5, emapSupBirthday);
        setString(6, emapValidTo);
        setString(7, emapMajorCardNo);
        setString(8, emapMajorValidFm);
        setString(9, emapMajorValidTo);
        setString(10, emapMajorChgFlag);
        setInt(   11, emapCreditLmt);
        setInt(   12, emapStandardFee);
        setInt(   13, emapAnnualFee);
        setString(14, emapFeeCode);
        setString(15, emapRiskBankNo);
        setString(16, emapFinalFeeCode);
        setString(17, emapFeeReasonCode);
        setString(18, emapComboIndicator);
        setString(19, emapActNo);
        setString(20, emapIcFlag);
        setString(21, emapServiceCode);
        setString(22, emapUnitCode);
        setString(23, emapDcIndicator);
        setString(24, emapCurrCode);
        setString(25, newCardcat);
        setString(26, emapCardType);
        setString(27, sysDate + sysTime);
        setDouble(28, emapSpecialCardRate);
        setDouble(29, emapRevolveIntRate);
        setRowId( 30, emapRowid);

        int recCnt = updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emap_tmp error[not find]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    private String getComboIndicator(String groupCode) throws Exception {
        selectSQL = "case when combo_indicator='' then 'N' else combo_indicator " + " end as combo_indicator ";
        daoTable = "ptr_group_code";
        whereStr = "WHERE group_code = ? ";

        setString(1, groupCode);
        int recCnt = selectTable();
        if (notFound.equals("Y")) {
            hErrorFlag = 1;
            return "";
        }

        tmpChar = getValue("combo_indicator");
        if (tmpChar.trim().length() == 0)
            return "N";

        return tmpChar;
    }

    // ************************************************************************
//   public int chkIntroduceId() throws Exception {
//        
//        selectSQL = " count(*) as all_cnt1 ";
//        daoTable = "crd_employee";
//        whereStr = "WHERE  id  =  ? " ;
//        setString(1, emapIntroduceId);
//
//        int recCnt1 = selectTable();
//
//        if (getValueInt("all_cnt1") == 0) {
//        	
//        	selectSQL = " count(*) as all_cnt2 ";
//            daoTable = "crd_idno";
//            whereStr = "WHERE  id_no  =  ? " ;
//            setString(1, emapIntroduceId);
//
//            int recCnt2 = selectTable();
//            
//            if(getValueInt("all_cnt2") == 0){
//            	
//            	selectSQL = " count(*) as all_cnt3 ";
//                daoTable = "mkt_sale";
//                whereStr = "WHERE  sale_id  =  ? " ;
//                setString(1, emapIntroduceId);
//
//                int recCnt3 = selectTable();
//                if(getValueInt("all_cnt3") == 0) {
//                	
//                	return (50);
//                }
//            }            
//        }     	
//        		
//        return (0);
//
//    }
    // ************************************************************************
    public void initRtn() throws Exception {
        hErrorFlag = 0;
        count = 0;
        emapRowid = "";
        emapGroupCode = "";
        emapCardType = "";
        emapSourceCode = "";
        emapMsisdn = "";
        emapApplyId = "";
        emapApplyIdCode = "";
        emapPmId = "";
        emapPmIdCode = "";
        emapBirthday = "";
        emapStmtCycle = "";
        emapServiceType = "";
        emapMajorCardNo = "";
        emapValidFm = "";
        emapValidTo = "";
        emapMajorValidFm = "";
        emapMajorValidTo = "";
        emapAcctType = "";
        emapPmBirthday = "";
        emapCheckCode = "";
        emapMajorChgFlag = "";
        emapCurrCode = "";
        emapDcIndicator = "";
        emapActNoF = "";
        emapComboIndicator = "";
        emapFinalFeeCode = "";
        emapFeeCode = "";
        emapCorpNo = "";
        emapFeeReasonCode = "";
        emapSupBirthday = "";
        emapAnnualFee = 0;
        emapCreditLmt = 0;
        emapStandardFee = 0;
        emapRiskBankNo = "";
        emapActNo = "";
        emapIcFlag = "";
        emapServiceCode = "";
        emapCardcat = "";
        emapUnitCode = "";
        emapForceFlag = "";
        emapRevolveIntRate = 0.0;
        emapRevolveIntRateYear = 0.0;
        emapSpecialCardRate = 0.0;
        emapBusinessCode = "";
        emapRegBankNo = "";
        emapBranch = "";
        emapEngName = "";
        emapNcccType = "";

        ptrServiceType = "";
        ptrCardMoldFlag = "";
        ptrExtnYear = 0;
        hLineOfCreditAmt = 0;
        hNewEndDate = "";
        cardComboAcctNo = "";
    }
    
    /***********************************************************************/
    /*
     * 交易代號 X(04) 固定放 'PB37’ 交易結果 9(02) '00'表示成功 ELSE 失敗 功能別 X(01)
     * A:新增（當新增DUP時仍回應成功) D:刪除 C:檢核（提供雙幣信用卡帳號檢核及簡易台幣信用卡帳號檢核) Q:查詢（提供該帳號+幣別是否有連結）
     * ID或統編 X(10) 帳號 9(11) 幣別 9(02) 卡號 X(16) 供功能別(A)新增成功時回傳用 P_SEQNO X(10)
     * 供功能別(A)新增成功時回傳用 訊息說明 X(60) 交易失敗結果說明
     */
    class Appcbuf {
        String txnNo;
        String respCode;
        String funcCode;
        String autopayId;
        String autopayAcctNo;
        String currCode;
        String cardNo;
        String pSeqno;
        String respDesc;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(txnNo         ,  4);
            rtn += comc.fixLeft(respCode      ,  2);
            rtn += comc.fixLeft(funcCode      ,  1);
            rtn += comc.fixLeft(autopayId     , 10);
            rtn += comc.fixLeft(autopayAcctNo, 11);
            rtn += comc.fixLeft(currCode      ,  2);
            rtn += comc.fixLeft(cardNo        , 16);
            rtn += comc.fixLeft(pSeqno        , 10);
            rtn += comc.fixLeft(respDesc      , 60);    //** all len= 116
            return rtn;
        }

        void splitBuf1(String str) throws UnsupportedEncodingException {
            byte[] bytes = str.getBytes("MS950");
            txnNo          = comc.subMS950String(bytes,  0,  4);
            respCode       = comc.subMS950String(bytes,  4,  2);
            funcCode       = comc.subMS950String(bytes,  6,  1);
            autopayId      = comc.subMS950String(bytes,  7, 10);
            autopayAcctNo = comc.subMS950String(bytes, 17, 11);
            currCode       = comc.subMS950String(bytes, 28,  2);
            cardNo         = comc.subMS950String(bytes, 30, 16);
            pSeqno         = comc.subMS950String(bytes, 46, 10);
            respDesc       = comc.subMS950String(bytes, 56, 60);
        }
    }
    // ************************************************************************

} // End of class FetchSample

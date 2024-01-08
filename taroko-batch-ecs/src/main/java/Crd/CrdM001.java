/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  106/06/01  V1.00.00  Edson       program initial                          *
*  107/04/17  V1.07.01  詹曜維       BECS-1070418-027 附卡帳址抓正卡資料                                               *
*  107/11/21  V1.08.01  詹曜維       BECS-1071120-084 抓帳址判斷reissue_status        *
*  108/05/13  V1.08.01  Brian       update to V1.08.01                       *
*  108/12/31  V2.01.01  Pino      讀取新申請及掛失補發貴賓卡的資料insert到crd_emboss_pp *
*  109/01/09  V2.01.02  Pino        新增欄位apply_credit_card_no                *
*  109/01/30  V2.01.03  Pino        insert_crd_emboss_pp old_card_no         *
*  109/03/20  V2.01.04  Wilson      insert unit_code =  h_embp_group_code    *
*  109/12/24  V2.00.05   shiyuqi       updated for project coding standard   *
*  111/12/28  V2.00.06  Wilson      讀取參數調整                                                                                           *
*  112/04/23  V2.00.07  Wilson      where條件刪除bin_type                       *
*  112/04/24  V2.00.08  Wilson      where條件刪除ppcard_bin_no                  * 
*  112/04/25  V2.00.09  Wilson      VISA PP卡卡號規則調整                                                                     *
*  112/05/02  V2.00.10  Wilson      修正檢查碼邏輯                                                                                       *
*  112/05/19  V2.00.11  Wilson      讀取寄送地址條件刪除bin_type                    * 
*  112/05/25  V2.00.12  Wilson      mark 英文姓名加稱謂                                                                          *
*  112/06/14  V2.00.13  Wilson      apply_credit_card_no改成card_no            *
*  112/07/26  V2.00.14  Wilson      增加毀損補發處理                                                                                   *
*  112/07/27  V2.00.15  Wilson      修正編列卡號規則                                                                                   *
*  112/11/30  V2.00.16  Wilson      讀取認同集團碼                                                                                        *
*****************************************************************************/

package Crd;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*接收PP卡製卡資料作業 改為 接收貴賓卡製卡資料作業*/
public class CrdM001 extends AccessDAO {
    private String progname = "接收貴賓卡製卡資料作業 112/11/30  V1.00.16 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    String hTempUser = "";

    String hCallBatchSeqno = "";

    String hProcDate = "";
    String hBusinessDate = "";
    String hSystemDate = "";
    int hRecno = 0;
    String hEmbpBatchno = "";
    int hTempIndicator = 0;
    String hPplyIdPSeqno = "";
    String hPplyEngName = "";
    String hPplyBinType = "";
    String hPplyCardType = "";
    String hPplyGroupCode = "";
    String hPppmPpcardBinNo = "";
    String hPppmPpcardIcaNo = "";
    int hPppmValidMonth = 0;
    String hPplyZipCode = "";
    String hPplyMailAddr1 = "";
    String hPplyMailAddr2 = "";
    String hPplyMailAddr3 = "";
    String hPplyMailAddr4 = "";
    String hPplyMailAddr5 = "";
    String hPplyMailType = "";
    String hPplyMailBranch = "";
    String hPplyPpCardNo = "";
    String hPplyUnitCode = "";
    String hTempReissueFlag = "";
    String hPplyRowid = "";
    String hPplyVipKind = "";
    String hPplyMakecardFee = "";
    String hPplyCreditCardNo = ""; // V2.01.02
    String hTempSpecBinNo = "";
    String hPplyOppostReason = "";
    String hSex = "";
    String hTempIdPSeqno = "";
    String hEmbpPpCardNo = "";
    //String h_embp_unit_code = "";
    String hEmbpCardItem = "";
    String hEmbpCardType = "";
    String hEmbpGroupCode = "";
    String hEmbpUnitCode = "";
    int hExtnYearMonth = 0;
    String hEmbpValidTo = "";
    int hPppmPpcardSeqno = 0;
    
    String hGroupAbbrCode = "";
    int hEmbpRecno = 0;
    String hEmbpEmbossSource = "";
    String hEmbpIdPSeqno = "";
    String hEmbpSourceCode = "";
    String hEmbpEngName = "";
    String hEmbpZipCode = "";
    String hEmbpMailAddr1 = "";
    String hEmbpMailAddr2 = "";
    String hEmbpMailAddr3 = "";
    String hEmbpMailAddr4 = "";
    String hEmbpMailAddr5 = "";
    String hEmbpMailType = "";
    String hEmbpMailBranch = "";
    String hEmbpValidFm = "";
    String hEmbpInMainDate = "";
    String hEmbpInMainError = "";
    String hEmbpWhFlag = "";
    
    String systemDd = "";
    String tempX19 = "";
    int checkCode = 0;
    int totCnt = 0;

    // ********************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : CrdM001 [yyyymmdd] [batch_seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = javaProgram;
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

            commoRtn();

            hProcDate = hSystemDate;
            if (args.length > 0 && args[0].length() == 8) {
                hProcDate = args[0];
            }
            sqlCmd = "select to_char((to_date(? , 'yyyymmdd')-1 days),'yyyymmdd') as h_proc_date ";
            sqlCmd += " from dual ";
            setString(1, hProcDate);
            if (selectTable() > 0) {
                hProcDate = getValue("h_proc_date");
            }

            systemDd = String.format("%2.2s", hProcDate.substring(6));
            showLogMessage("I", "", String.format(" Process prev_date = [%s][%s]\n", hProcDate, systemDd));

            selectCrdPpcardApply();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commoRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }

        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
        }
    }

    /***********************************************************************/
    void selectCrdPpcardApply() throws Exception {
        String tempX50 = "";

        hEmbpBatchno = hProcDate;
        hRecno = 0;
        sqlCmd = "select max(nvl(recno,0)) h_recno ";
        sqlCmd += " from crd_emboss_pp  ";
        sqlCmd += "where batchno = ? ";
        setString(1, hEmbpBatchno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_emboss_pp not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hRecno = getValueInt("h_recno");
        }

        showLogMessage("I", "", String.format("Process batcn_no =[%s][%d]", hEmbpBatchno, hRecno));

        sqlCmd = "select ";
        sqlCmd += "1 h_temp_indicator,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "a.bin_type,";
        sqlCmd += "b.card_type,";
        sqlCmd += "b.group_code,";
        sqlCmd += "b.ppcard_bin_no,";
        sqlCmd += "b.ppcard_ica_no,";
        sqlCmd += "b.valid_month,";
        sqlCmd += "a.zip_code,";
        sqlCmd += "a.mail_addr1,";
        sqlCmd += "a.mail_addr2,";
        sqlCmd += "a.mail_addr3,";
        sqlCmd += "a.mail_addr4,";
        sqlCmd += "a.mail_addr5,";
        sqlCmd += "a.mail_type,";
        sqlCmd += "a.mail_branch,";
        sqlCmd += "a.pp_card_no,";
        sqlCmd += "'Y'   as reissue_flag ,";
        sqlCmd += "a.rowid  as rowid ,";
        sqlCmd += "a.vip_kind,";
        sqlCmd += "a.makecard_fee, ";
        sqlCmd += "a.card_no, ";
        sqlCmd += "substring(a.card_no,1,6) as spec_bin_no, ";
        sqlCmd += "'' as oppost_reason ";
        sqlCmd += "from mkt_ppcard_issue b ,crd_ppcard_apply a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and decode(a.proc_flag,'','N',a.proc_flag) != 'Y' ";
        sqlCmd += "  and a.apply_date  <= ? ";
        sqlCmd += "  and a.apr_flag     = 'Y' ";
        sqlCmd += "  and b.group_code   = a.group_code ";
        sqlCmd += "  and b.vip_kind     = a.vip_kind ";
        sqlCmd += "UNION ";
        sqlCmd += "select 2 h_temp_indicator, ";
        sqlCmd += "a.id_p_seqno, ";
        sqlCmd += "b.eng_name, ";
        sqlCmd += "c.bin_type, ";
        sqlCmd += "b.card_type, ";
        sqlCmd += "b.group_code, ";
        sqlCmd += "c.ppcard_bin_no, ";
        sqlCmd += "c.ppcard_ica_no, ";
        sqlCmd += "c.valid_month, ";
        sqlCmd += "b.zip_code, ";
        sqlCmd += "b.mail_addr1, ";
        sqlCmd += "b.mail_addr2, ";
        sqlCmd += "b.mail_addr3, ";
        sqlCmd += "b.mail_addr4, ";
        sqlCmd += "b.mail_addr5, ";
        sqlCmd += "'1' as mail_type, ";
        sqlCmd += "'' as mail_branch, ";
        sqlCmd += "a.pp_card_no, ";
        sqlCmd += "decode(a.reissue_flag,'','N',a.reissue_flag)  as reissue_flag, ";
        sqlCmd += "a.rowid  as rowid, ";
        sqlCmd += "a.vip_kind,";
        sqlCmd += "a.makecard_fee, ";
        sqlCmd += "b.card_no, "; // V2.01.02
        sqlCmd += "substring(a.pp_card_no,1,6) as spec_bin_no, ";
        sqlCmd += "a.oppost_reason ";
        sqlCmd += " from mkt_ppcard_issue c, crd_card_pp b, crd_ppcard_stop a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and decode(a.proc_flag  ,'','N',a.proc_flag)   != 'Y' ";
        sqlCmd += "  and decode(a.cancel_flag,'','N',a.cancel_flag) != 'Y' ";
        sqlCmd += "  and a.crt_date     <= ? ";
        sqlCmd += "  and a.apr_flag      = 'Y' ";
        sqlCmd += "  and b.pp_card_no    = a.pp_card_no ";
        sqlCmd += "  and c.group_code    = b.group_code ";
        sqlCmd += "  and c.vip_kind      = a.vip_kind ";
        setString(1, hProcDate);
        setString(2, hProcDate);
        openCursor();
        while(fetchTable()) {
            hTempIndicator  = getValueInt("h_temp_indicator");
            hPplyIdPSeqno = getValue("id_p_seqno");
            hPplyEngName   = getValue("eng_name");
            hPplyBinType   = getValue("bin_type");
            hPplyCardType  = getValue("card_type");
            hPplyGroupCode = getValue("group_code");
            hPppmPpcardBinNo = getValue("ppcard_bin_no");
            hPppmPpcardIcaNo = getValue("ppcard_ica_no");
            hPppmValidMonth   = getValueInt("valid_month");
            hPplyZipCode   = getValue("zip_code");
            hPplyMailAddr1 = getValue("mail_addr1");
            hPplyMailAddr2 = getValue("mail_addr2");
            hPplyMailAddr3 = getValue("mail_addr3");
            hPplyMailAddr4 = getValue("mail_addr4");
            hPplyMailAddr5 = getValue("mail_addr5");
            hPplyMailType  = getValue("mail_type");
            hPplyMailBranch  = getValue("mail_branch");
            hPplyPpCardNo   = getValue("pp_card_no");
//          h_pply_unit_code    = getValue("unit_code");
            hTempReissueFlag = getValue("reissue_flag");
            hPplyRowid        = getValue("rowid");
            hPplyVipKind     = getValue("vip_kind");
            hPplyMakecardFee = getValue("makecard_fee");
            hPplyCreditCardNo = getValue("card_no"); // V2.01.02
            hTempSpecBinNo = getValue("spec_bin_no");
            hPplyOppostReason = getValue("oppost_reason");
            
            hEmbpWhFlag = "N";

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1) {
                showLogMessage("I", "", String.format("Process record=[%d]", totCnt));
            }

if(debug==1) showLogMessage("I", "", "888 cnt="+ totCnt + ","+ hTempReissueFlag);

            if (!hTempReissueFlag.equals("Y")) {
                updateCrdPpcardApply();
                continue;
            }
            hSex = "";
            hTempIdPSeqno = "";
            hEmbpInMainError = "0";
            sqlCmd = "select sex,";
            sqlCmd += "case when order_card='1' then id_p_seqno else major_id_p_seqno end as id_p_seqno ";
            sqlCmd += " from ( ";
            sqlCmd += "         select x.* ";
            sqlCmd += "           from ( ";
            sqlCmd += "              select sex,b.major_id_p_seqno,a.id_p_seqno, ";
            sqlCmd += "                     case when b.major_id_p_seqno=a.id_p_seqno then 1 else 2 end as order_card ";
            sqlCmd += "                from crd_idno a,crd_card b,mkt_ppcard_apply m ";
            sqlCmd += "               where a.id_p_seqno = b.id_p_seqno ";
            sqlCmd += "                 and m.card_type = b.card_type ";
            sqlCmd += "                 and m.group_code = nvl(b.group_code,'0000') ";
            sqlCmd += "                 and (current_code = '0' or reissue_status in ('1','2')) ";
            sqlCmd += "                 and a.id_p_seqno      = ? "; 
            sqlCmd += "                 ) x ";
            sqlCmd += "             order by order_card ";
            sqlCmd += "        ) k ";
            sqlCmd += " fetch first 1 rows only ";
            setString(1, hPplyIdPSeqno);
            selectTable();
            if (notFound.equals("Y")) {
                hEmbpInMainError = "1";
            }
            hSex = getValue("sex");
            hTempIdPSeqno = getValue("id_p_seqno");

//            if (hTempIndicator == 1) {
//                tempX50 = String.format("MS. %-22.22s", hPplyEngName);
//                if (hSex.substring(0, 1).equals("1"))
//                    tempX50 = String.format("MR. %-22.22s", hPplyEngName);
//                hPplyEngName = tempX50;
//            }
            if ((hTempIndicator == 2) || (hPplyMailType.equals("3") || hPplyMailType.equals("4"))) {
                sqlCmd = "select bill_sending_zip,";
                sqlCmd += "bill_sending_addr1,";
                sqlCmd += "bill_sending_addr2,";
                sqlCmd += "bill_sending_addr3,";
                sqlCmd += "bill_sending_addr4,";
                sqlCmd += "bill_sending_addr5 ";
                sqlCmd += " from act_acno  ";
                sqlCmd += "where acno_p_seqno = (select acno_p_seqno from crd_card c, mkt_ppcard_apply m ";
                sqlCmd +=                       " where m.card_type  = c.card_type  ";
                sqlCmd += "                         and m.group_code = decode(c.group_code,'','0000',c.group_code)  ";
                sqlCmd += "                         and (current_code = '0' or reissue_status in ('1','2'))  ";
                sqlCmd += "                         and c.id_p_seqno = ?  ";
                sqlCmd += "                       fetch first 1 rows only)  ";
                sqlCmd += "fetch first 1 rows only ";
                setString(1, hTempIdPSeqno);
                selectTable();
                if (notFound.equals("Y")) {
                    hEmbpInMainError = "1";
                }
                hPplyZipCode = getValue("bill_sending_zip");
                hPplyMailAddr1 = getValue("bill_sending_addr1");
                hPplyMailAddr2 = getValue("bill_sending_addr2");
                hPplyMailAddr3 = getValue("bill_sending_addr3");
                hPplyMailAddr4 = getValue("bill_sending_addr4");
                hPplyMailAddr5 = getValue("bill_sending_addr5");
            }

            if (moveRtn(hTempIndicator) == 0)
                insertCrdEmbossPp();

            updateCrdPpcardApply();
        }
if(debug==1) showLogMessage("I", "", "888 end="+ totCnt + ","+ hTempReissueFlag);
        closeCursor();
    }

    /***********************************************************************/
    void updateCrdPpcardApply() throws Exception {

        if (hTempIndicator == 1) {
            daoTable   = "crd_ppcard_apply";
            updateSQL  = " proc_flag   = 'Y',";
            updateSQL += " proc_date   = ?, ";
            updateSQL += " pp_card_no  = ?";
            whereStr = "where rowid  = ? ";
            setString(1, hProcDate);
            setString(2, hEmbpPpCardNo);
            setRowId(3, hPplyRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_ppcard_apply not found!", "", comcr.hCallBatchSeqno);
            }
        } else {
            daoTable = "crd_ppcard_stop";
            updateSQL = " proc_flag   = 'Y',";
            updateSQL += " proc_date   = ? ";
            whereStr = "where rowid  = ? ";
            setString(1, hProcDate);
            setRowId(2, hPplyRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_ppcard_stop not found!", "", comcr.hCallBatchSeqno);
            }
        }
    }

    /***********************************************************************/
    int moveRtn(int idx) throws Exception {

        hEmbpRecno = totCnt + hRecno;
        hEmbpEmbossSource = "1";
        if (idx == 2)
            hEmbpEmbossSource = "5";
        hEmbpIdPSeqno = hPplyIdPSeqno;
        hEmbpCardType = hPplyCardType;
        hEmbpGroupCode = hPplyGroupCode;
        hEmbpValidFm = String.format("%6.6s01", hProcDate);

        hExtnYearMonth = hPppmValidMonth;
        
        selectSQL = " unit_code ";
        daoTable  = "ptr_group_card_dtl";
        whereStr  = "WHERE group_code    =  ? " 
                  + "  and card_type     =  ? ";
        setString(1, hEmbpGroupCode);
        setString(2, hEmbpCardType);

        selectTable();
        if (notFound.equals("Y")) {
            String err1 = "select_ptr_group_card_dtl  error!!=" + hEmbpCardType;
            comcr.errRtn(err1, hEmbpGroupCode, comcr.hCallBatchSeqno);
        }

        hEmbpUnitCode = getValue("unit_code");

        sqlCmd = "select card_item ";
        sqlCmd += " from crd_item_unit  ";
        sqlCmd += "where unit_code    = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hEmbpUnitCode);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_item_unit not found!", hEmbpCardType+","+hPplyGroupCode
                          , comcr.hCallBatchSeqno);
        }
        hEmbpCardItem    = getValue("card_item");

        if (comcr.str2int(systemDd) >= 25)
            hExtnYearMonth = hExtnYearMonth + 1;
        sqlCmd = "select to_char(last_day(add_months(to_date(?,'yyyymmdd') , ?)) ,'yyyymmdd') h_embp_valid_to ";
        sqlCmd += " from dual ";
        setString(1, hProcDate);
        setInt(2, hExtnYearMonth);
        if (selectTable() > 0) {
            hEmbpValidTo = getValue("h_embp_valid_to");
        }

        hEmbpEngName = hPplyEngName;
        hEmbpZipCode = hPplyZipCode;
        hEmbpMailAddr1 = hPplyMailAddr1;
        hEmbpMailAddr2 = hPplyMailAddr2;
        hEmbpMailAddr3 = hPplyMailAddr3;
        hEmbpMailAddr4 = hPplyMailAddr4;
        hEmbpMailAddr5 = hPplyMailAddr5;
        hEmbpMailType = hPplyMailType;
        hEmbpMailBranch = hPplyMailBranch;
//        h_embp_in_main_error = "0"; /* V1.08.01 marked */

        if(!hPplyOppostReason.equals("P5")) {
            selectMktPpcardIssue();

            if(debug==1) showLogMessage("I", "", "  888 chgType="+ hPplyBinType);
            if(hPplyVipKind.equals("1")) {
            	switch (chgType(hPplyBinType)) {
            	case 1:
            		/*(1).第1,3,5,7,9,11,13位 乘以2,然後個位與十位兩兩相加
            		 *(2).第2,4,6,8,10,12位數字 與 (1)數字相加 
            		 *(3).將(2)數字除以10得知 商數  
            		 *(4).將(2)數字減去(3)數字*10  
            		 *(5).10-(4)數字得到檢查碼                                                                    */
            		int[] num1 = new int[18];
            		num1[1]=comcr.str2int(hPppmPpcardBinNo.substring(0, 1));
            		num1[2]=comcr.str2int(hPppmPpcardBinNo.substring(1, 2));
            		num1[3]=comcr.str2int(hPppmPpcardBinNo.substring(2, 3));
            		num1[4]=comcr.str2int(hPppmPpcardBinNo.substring(3, 4));
            		num1[5]=comcr.str2int(hPppmPpcardBinNo.substring(4, 5));
            		num1[6]=comcr.str2int(hPppmPpcardBinNo.substring(5, 6));
            		String str1= String.format("%07d", hPppmPpcardSeqno);
            		num1[7]=comcr.str2int(str1.substring(0, 1));
            		num1[8]=comcr.str2int(str1.substring(1, 2));
            		num1[9]=comcr.str2int(str1.substring(2, 3));
            		num1[10]=comcr.str2int(str1.substring(3, 4));
            		num1[11]=comcr.str2int(str1.substring(4, 5));
            		num1[12]=comcr.str2int(str1.substring(5, 6));
            		num1[13]=comcr.str2int(str1.substring(6, 7));
            		for(int i=1;i<=13;i+=2) {
            			num1[14]+= (num1[i]*2/10+num1[i]*2%10); //num1[14]為 (1).   第1,3,5,7,9,11,13位 乘以2,然後個位與十位兩兩相加 
            		} 
        		
            		for(int i=2;i<=12;i+=2) {
            			num1[15]+= num1[i];
            		}  
            		num1[15]+= num1[14]; //num1[15]為 (2).   第2,4,6,8,10,12位數字 與 (1)數字相加 
            		num1[16] = num1[15]/10; //num1[16]為 (3).   將(2)數字除以10得知 商數
            		num1[17] = num1[15]-(num1[16]*10); //num1[17]為(4).   將(2)數字減去(3)數字*10
            		num1[0]  = 10-num1[17]; //num1[0]為(5).   10-(4)數字得到檢查碼
            		checkCode = num1[0];
            		tempX19 = String.format("%6.6s%07d%01d", hTempSpecBinNo, hPppmPpcardSeqno,checkCode);
            		break;
            	case 2:
            		/*(1).第1,3,5,7,9,11,13,15,17位 乘以2,然後個位與十位兩兩相加
            		 *(2).第2,4,6,8,10,12,14,16位數字 與 (1)數字相加 
            		 *(3).將(2)數字除以10得知 商數  
            		 *(4).將(2)數字減去(3)數字*10  
            		 *(5).10-(4)數字得到檢查碼                                                                    */
            		int[] num2 = new int[22];
            		num2[1]=comcr.str2int(hPppmPpcardBinNo.substring(0, 1));
            		num2[2]=comcr.str2int(hPppmPpcardBinNo.substring(1, 2));
            		num2[3]=comcr.str2int(hPppmPpcardBinNo.substring(2, 3));
            		num2[4]=comcr.str2int(hPppmPpcardBinNo.substring(3, 4));
            		num2[5]=comcr.str2int(hPppmPpcardBinNo.substring(4, 5));
            		num2[6]=comcr.str2int(hPppmPpcardBinNo.substring(5, 6));
            		num2[7]=comcr.str2int(hPppmPpcardIcaNo.substring(0, 1));
            		num2[8]=comcr.str2int(hPppmPpcardIcaNo.substring(1, 2));
            		num2[9]=comcr.str2int(hPppmPpcardIcaNo.substring(2, 3));
            		num2[10]=comcr.str2int(hPppmPpcardIcaNo.substring(3, 4));
            		String str2= String.format("%07d", hPppmPpcardSeqno);
            		num2[11]=comcr.str2int(str2.substring(0, 1));
            		num2[12]=comcr.str2int(str2.substring(1, 2));
            		num2[13]=comcr.str2int(str2.substring(2, 3));
            		num2[14]=comcr.str2int(str2.substring(3, 4));
            		num2[15]=comcr.str2int(str2.substring(4, 5));
            		num2[16]=comcr.str2int(str2.substring(5, 6));
            		num2[17]=comcr.str2int(str2.substring(6, 7));
            		for(int i=1;i<=17;i+=2) {
            			num2[18]+= (num2[i]*2/10+num2[i]*2%10); //num2[18]為 (1).   第1,3,5,7,9,11,13,15,17位 乘以2,然後個位與十位兩兩相加
            		} 
 
            		for(int i=2;i<=16;i+=2) {
            			num2[19]+= num2[i];
            		}  
            		num2[19]+= num2[18]; //num2[19]為 (2).   第2,4,6,8,10,12,14,16位數字 與 (1)數字相加 
            		num2[20] = num2[19]/10; //num2[20]為 (3).   將(2)數字除以10得知 商數
            		num2[21] = num2[19]-(num2[20]*10); //num2[21]為(4).   將(2)數字減去(3)數字*10
            		num2[0]  = 10-num2[21]; //num2[0]為(5).   10-(4)數字得到檢查碼
            		checkCode = num2[0];
            		tempX19 = String.format("%6.6s%4.4s%07d%01d", hPppmPpcardBinNo, hPppmPpcardIcaNo, hPppmPpcardSeqno,checkCode);
            		break;
            	case 3:
            		/*(1).第1,3,5,7,9,11位 乘以2,然後個位與十位兩兩相加
            		 *(2).第2,4,6,8,10位數字 與 (1)數字相加 
            		 *(3).將(2)數字除以10得知 商數  
            		 *(4).將(2)數字減去(3)數字*10  
            		 *(5).10-(4)數字得到檢查碼                                                                    */
            		int[] num3 = new int[16];
            		num3[1]=comcr.str2int(hPppmPpcardBinNo.substring(0, 1));
            		num3[2]=comcr.str2int(hPppmPpcardBinNo.substring(1, 2));
            		num3[3]=comcr.str2int(hPppmPpcardBinNo.substring(2, 3));
            		num3[4]=comcr.str2int(hPppmPpcardBinNo.substring(3, 4));
            		num3[5]=comcr.str2int(hPppmPpcardBinNo.substring(4, 5));
            		num3[6]=comcr.str2int(hPppmPpcardBinNo.substring(5, 6));
            		String str3= String.format("%05d", hPppmPpcardSeqno);
            		num3[7]=comcr.str2int(str3.substring(0, 1));
            		num3[8]=comcr.str2int(str3.substring(1, 2));
            		num3[9]=comcr.str2int(str3.substring(2, 3));
            		num3[10]=comcr.str2int(str3.substring(3, 4));
            		num3[11]=comcr.str2int(str3.substring(4, 5));
            		for(int i=1;i<=11;i+=2) {
            			num3[12]+= (num3[i]*2/10+num3[i]*2%10);  //num3[12]為 (1).   第1,3,5,7,9,11位 乘以2,然後個位與十位兩兩相加
            		} 

            		for(int i=2;i<=10;i+=2) {
            			num3[13]+= num3[i];
            		}  
            		num3[13]+= num3[12]; //num3[13]為 (2).   第2,4,6,8,10位數字 與 (1)數字相加 
            		num3[14] = num3[13]/10; //num3[14]為 (3).   將(2)數字除以10得知 商數
            		num3[15] = num3[13]-(num3[14]*10); //num3[15]為(4).   將(2)數字減去(3)數字*10
            		num3[0]  = 10-num3[15]; //num3[0]為(5).   10-(4)數字得到檢查碼
            		checkCode = num3[0];
            		tempX19 = String.format("%6.6s%05d%01d", hPppmPpcardBinNo, hPppmPpcardSeqno,checkCode);
            		break;
            	default:
            		return (1);
            	}
            }else if(hPplyVipKind.equals("2")) {
            		tempX19 =  String.format("%6.6s%2.2s%06d%02d ", hPppmPpcardBinNo,hPppmPpcardIcaNo,hPppmPpcardSeqno,88);
            }
            hEmbpPpCardNo = tempX19;
        }
        else {
        	hEmbpPpCardNo = hPplyPpCardNo;
        }

        return (0);
    }

    /***********************************************************************/
    void selectMktPpcardIssue() throws Exception {
        hPppmPpcardSeqno = 0;
        sqlCmd = "select max(ppcard_seqno) h_pppm_ppcard_seqno ";
        sqlCmd += " from mkt_ppcard_issue  ";
        sqlCmd += "where group_code = ? ";
        sqlCmd += "and vip_kind  = ? ";
        setString(1, hPplyGroupCode);
        setString(2, hPplyVipKind);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mkt_ppcard_issue not found!", "", comcr.hCallBatchSeqno);
        }
        hPppmPpcardSeqno = getValueInt("h_pppm_ppcard_seqno");

        hPppmPpcardSeqno = hPppmPpcardSeqno + 1;
        daoTable = "mkt_ppcard_issue";
        updateSQL = "ppcard_seqno = ?";
        whereStr = "where vip_kind  = ? "
                 + "and ppcard_bin_no  = ? ";
        setInt(1, hPppmPpcardSeqno);
        setString(2, hPplyVipKind);
        setString(3, hPppmPpcardBinNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_mkt_ppcard_issue not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertCrdEmbossPp() throws Exception {
        String hGroupAbbrCode = "";

if(debug==1) showLogMessage("I", "", "  888 insert ="+ hEmbpGroupCode );

        hGroupAbbrCode = "";
        sqlCmd = "select decode(GROUP_ABBR_CODE,'',' ',GROUP_ABBR_CODE) h_group_abbr_code ";
        sqlCmd += " from ptr_group_code  ";
        sqlCmd += "where group_code = ? ";
        setString(1, hEmbpGroupCode);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_group_code not found!", "", comcr.hCallBatchSeqno);
        }
        hGroupAbbrCode = getValue("h_group_abbr_code");

        hEmbpSourceCode = String.format("%2.2s%4.4s", hGroupAbbrCode, hEmbpGroupCode);

        setValue("batchno", hEmbpBatchno);
        setValueInt("recno", hEmbpRecno);
        setValue("emboss_source", hEmbpEmbossSource);
        setValue("pp_card_no", hEmbpPpCardNo);

        String[] info = comcr.getIDInfo(hEmbpIdPSeqno);
        setValue("id_no", info[0]);
        setValue("id_no_code", info[1]);

        setValue("card_type", hEmbpCardType);
        setValue("unit_code", hEmbpUnitCode);
        setValue("group_code", hEmbpGroupCode);
        setValue("source_code", hEmbpSourceCode);
        setValue("card_item", hEmbpCardItem);
        setValue("eng_name", hEmbpEngName);
        setValue("zip_code", hEmbpZipCode);
        setValue("mail_addr1", hEmbpMailAddr1);
        setValue("mail_addr2", hEmbpMailAddr2);
        setValue("mail_addr3", hEmbpMailAddr3);
        setValue("mail_addr4", hEmbpMailAddr4);
        setValue("mail_addr5", hEmbpMailAddr5);
        setValue("mail_type", hEmbpMailType);
        setValue("mail_branch", hEmbpMailBranch);
        setValue("valid_fm", hEmbpValidFm);
        setValue("valid_to", hEmbpValidTo);
        setValue("in_main_date", hEmbpInMainDate);
        setValue("in_main_error", hEmbpInMainError);
        setValue("wh_flag", hEmbpWhFlag);
        
        if(hEmbpEmbossSource.equals("5")) { //V2.01.03
            setValue("old_card_no", hPplyPpCardNo);
        }
        else {
            setValue("old_card_no", "");
        }
        
        if(hEmbpEmbossSource.equals("5")) {
        	if(hPplyOppostReason.equals("P1")) {
        		setValue("reissue_reason", "1");
        	}
        	else if(hPplyOppostReason.equals("P2")) {
        		setValue("reissue_reason", "3");
        	}
        	else {
        		setValue("reissue_reason", "2");
        	}            
        }
        else {
            setValue("reissue_reason", "");
        }

        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        setValue("vip_kind", hPplyVipKind);
        setValue("makecard_fee", hPplyMakecardFee);
        setValue("card_no", hPplyCreditCardNo); // V2.01.02
        daoTable = "crd_emboss_pp";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_emboss_pp duplicate!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdM001 proc = new CrdM001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    int chgType(String typ) {
        int rtn = 0;
        if (typ.equals("V"))
            rtn = 1;
        if (typ.equals("M"))
            rtn = 2;
        if (typ.equals("J"))
            rtn = 3;
        return rtn;
    }

}

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/05/23  V1.01.01  Lai        Initial                                    *
* 108/11/20  V2.01.01  Pino       check_code 3碼                                                                                  *
* 109/02/21  V2.01.02  Pino       get_card_no()change logic                  *
* 109/03/16  V2.01.03  Wilson     card_flag = '1'                            *
* 109/03/23  V2.01.04  Wilson     insert crd_seqno_log 新增 seqno_old          *
* 109/04/09  V2.01.05  Wilson     post_flag = 'Y'                            *
* 109/11/03  V2.01.06  Wilson     update crd_emap_tmp新增dc_indicator、curr_code*
* 109/12/17  V2.01.07  shiyuqi    updated for project coding standard        *
* 110/06/18  V2.01.08  Wilson     dc_indicator、curr_code移回CrdB002           *
* 112/01/31  V2.01.09  Ryan       調整編列卡號邏輯的部分                                                                             *
* 112/12/03  V2.01.10  Wilson     crd_item_unit不判斷卡種                                                                *
*****************************************************************************/
package Crd;

import com.*;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class CrdB005 extends AccessDAO {
    private String progname = "自動編列卡號處理        112/12/03  V2.01.10 ";
    private Map<String, Object> resultMap;

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommString comStr = new CommString();
    int debug = 0;
    int debugD = 0;
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
    int hErrorFlag = 0;
    int totalErr = 0;

    String emapRowid = "";
    String emapGroupCode = "";
    String emapCardType = "";
    String emapUnitCode = "";
    String emapApplyId = "";
    String emapApplyIdCode = "";
    String emapPmId = "";
    String emapPmIdCode = "";
    String emapCheckCode = "";
    String emapCardNo = "";
    String emapBinNo = "";
    String emapCardnoCode = "";
    String seqnoRowid = "";
    String emapCurrCode = "";
    String emapDcIndicator = "";
 
    String prevGroupCode = "";
    String prevCardType = "";
    String hGroupCode = "";
    String hSeqno = "";
    String hBinNo = "";
    String hBegSeqno = "";
    String hEndSeqno = "";
    String hFirstSeqno = "";
    String hRandomSeqno = "";
    int failCnt = 0;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdB005 proc = new CrdB005();
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
                String err1 = "nCrdB005 請輸入 : callseqno";
                String err2 = "";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

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
                if (selectTable() > 0)
                    hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            dateTime();
            selectPtrBusinday();

         
            while(true) {
            	tmpInt = checkProcess(1, "CrdB005");
            	if(tmpInt!=0) {
            		showLogMessage("I", "", "CrdB005,CrdC008,DbcB005,DbcC008正在執行中,sleep 120 sec 後重新執行");
            		TimeUnit.SECONDS.sleep(120);
            		continue;
            	}
            	break;
            }

            totalCnt = 0;
            totalErr = 0;
            selectCrdEmapTmp();

            tmpInt = checkProcess(2, "CrdB005");

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "],Error="+totalErr;
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
public void selectPtrBusinday() throws Exception 
{
        selectSQL = "business_date   , " + "to_char(sysdate,'yyyymmdd')    as SYSTEM_DATE ";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("BUSINESS_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
 
        hBusiChiDate = Long.toString(hLongChiDate);

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");
}
// ************************************************************************
public void selectCrdEmapTmp() throws Exception 
{
        selectSQL = " a.apply_id            ,nvl(a.apply_id_code,'0') as apply_id_code, "
                  + " a.pm_id               ,a.pm_id_code            , "
                  + " a.card_type           ,a.group_code            , "
                  + " a.unit_code           ,                          " 
                  + " a.rowid      as rowid ";
        daoTable = "crd_emap_tmp a";
        whereStr = "where a.card_no     = ''   " + "  and a.check_code  = '000'  "
                 + "order by card_type, group_code, batchno,recno ";

        openCursor();

        while (fetchTable()) {
            initRtn();

            emapGroupCode = getValue("group_code");
            if (emapGroupCode.trim().length() < 1)     emapGroupCode = "0000";
            hGroupCode = emapGroupCode;
            emapCardType = getValue("card_type");
            emapApplyId = getValue("apply_id");
            emapApplyIdCode = getValue("apply_id_code");
            if (emapApplyIdCode.trim().length() < 1)
                emapApplyIdCode = "0";
            emapPmId = getValue("pm_id");
            emapPmIdCode = getValue("pm_id_code");
            emapUnitCode = getValue("unit_code");
            emapRowid = getValue("rowid");

            totalCnt++;
            processDisplay(5000); // every nnnnn display message
            if (debug == 1) {
                showLogMessage("I", "", "8888 Beg1 id="+ emapApplyId +","+"pm=["+ emapPmId +"]");
                showLogMessage("I", "", "       group=[" + emapGroupCode + "]");
                showLogMessage("I", "", "        type=[" + emapCardType + "]");
            }

            int flag = 0;
            hErrorFlag = 0;
            
            if (prevCardType.trim().compareTo(emapCardType.trim()) != 0) {
                flag = 1;
                prevCardType = emapCardType.trim();
            }
            if (prevGroupCode.trim().compareTo(emapGroupCode.trim()) != 0) {
                flag = 1;
                prevGroupCode = emapGroupCode.trim();
            }

            if (debug == 1) showLogMessage("I", "", "8888  step 1.0=[" + flag + "]");

//??            if (flag == 1) {
//                tmp_int = get_min_card_no();
//                if(tmp_int != 0)     continue;
//            }
            if (debug == 1) showLogMessage("I", "", "8888  step 1.1=[" + tmpInt + "]");

            String swGetCard = "1";

            while (swGetCard.equals("1")) 
              {
                tmpInt = getCardNo();
                if(debug == 1)    showLogMessage("I", "", "8888  step 1.2=" + tmpInt);
                if (tmpInt == 0) {
                    int tmpInt1 = combineCardNo();
                    if (tmpInt1 == 0) {
                        swGetCard = "0"; // 正確 往下
                    }
                    else {
                    	continue;
                    }
                } else {
//??                    fail_cnt++;
                	continue;
                }
              }
                        
            if (hErrorFlag > 0) {
                totalErr++;
                updateCrdEmapErr();
            }else {
            	updateCrdEmapTmp();

            }
            
            if (debug == 1)
                showLogMessage("D", "", " 888 update=[" + tmpInt + "]");

            // 正附卡同時申請,正卡編列完成時,需寫入附卡之major_card_no
            if (emapApplyId.trim().compareTo(emapPmId.trim()) != 0) {
                writeSupData();
            }

            commitDataBase();
        }
}
// ************************************************************************
public int chkUseOrgCardnoFlag() throws Exception 
{
if(debug == 1) showLogMessage("I", "", " org=[" + emapCardType + "]" + emapGroupCode);

        selectSQL = " org_cardno_flag     ";
        daoTable  = "ptr_group_card";
        whereStr  = "WHERE group_code =  ? " 
                  + "  and card_type  =  ? ";
        setString(1, emapGroupCode);
        setString(2, emapCardType);

        selectTable();
        if (debug == 1)
            showLogMessage("I", "", " org notFound=[" + notFound + "] ");

        if (notFound.equals("Y")) {
            return (2);
        }

        tmpChar = getValue("org_cardno_flag");
        if (tmpChar.trim().compareTo("N") == 0)
            return (1);

        return (0);
}
// ************************************************************************
public int checkProcess(int iType, String iProgCode) throws Exception 
{
if(debug == 1) showLogMessage("I", "", " check=[" + iType + "] " + iProgCode);

        if (iType == 2) {
            updateSQL = " wf_value         = 'NO' , " 
                      + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
                      + " mod_pgm          = ?   ";
            daoTable  = "ptr_sys_parm";
            whereStr  = "WHERE wf_parm     = 'CRD_BATCH' " 
                      + "  and wf_key      = ? ";

            setString(1, sysDate + sysTime);
            setString(2, javaProgram);
            setString(3, iProgCode);

            updateTable();

            if (notFound.equals("Y")) {
                String err1 = "update_ptr_sys_parm 2 error[not find] + i_prog_code";
                String err2 = "";
                comcr.errRtn(err1, err2, hCallBatchSeqno);
            }

            return (0);
        }


        selectSQL = " wf_value ";
        daoTable = "ptr_sys_parm";
        whereStr = "WHERE wf_parm  =  'CRD_BATCH' " + "  and wf_key  in ('CrdB005','CrdC008','DbcB005','DbcC008') ";
        int n = selectTable();
        
        for(int i=0;i<n;i++) {
        	tmpChar = getValue("wf_value",i);
        	if (tmpChar.trim().compareTo("YES") == 0 || tmpChar.trim().compareTo("yes") == 0) {
				showLogMessage("D", "", "Error:新製卡編列卡號,不可同時執行或參數檔被鎖住");
				return (1);
        	}
        }

		updateSQL = " wf_value         = 'YES' , " + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
				+ " mod_pgm          = ?   ";
		daoTable = "ptr_sys_parm";
		whereStr = "WHERE wf_parm     = 'CRD_BATCH' " + "  and wf_key      = ? ";

		setString(1, sysDate + sysTime);
		setString(2, javaProgram);
		setString(3, iProgCode);

		updateTable();

		if (notFound.equals("Y")) {
			String err1 = "update_ptr_sys_parm 1 error[not find] + i_prog_code";
			String err2 = "";
			comcr.errRtn(err1, err2, hCallBatchSeqno);
		}

		commitDataBase();
		/*
		 * lai test
		 */
      return (0);
}
// ************************************************************************
public int getMinCardNo() throws Exception 
{

        // 抓取美一種卡種+團體代號之最小序號

        selectSQL = " min(bin_no)   as bin_no        , " 
                  + " min(seqno)    as seqno           ";
        daoTable  = "crd_seqno_log";
        whereStr  = "WHERE group_code   =  ?  " 
                  + "  and card_type    =  ?  " 
                  + "  and card_flag    = '1' "
                  + "  and reserve     <> 'Y' " 
                  + "  and card_type_sort in (select min(card_type_sort)  " 
                                             + "from crd_seqno_log        "
                                             + "WHERE group_code   =  ?   " 
                                             + "  and card_type    =  ?   " 
                                             + "  and card_flag    = '1'  "
                                             + "  and reserve     <> 'Y') " ;
        setString(1, emapGroupCode);
        setString(2, emapCardType);
        setString(3, emapGroupCode);
        setString(4, emapCardType);

        int recCnt = selectTable();

        hBinNo = getValue("bin_no");
        hFirstSeqno = getValue("seqno");

if(debug == 1) showLogMessage("I", "", " min=" + hFirstSeqno + ","+ hBinNo);

        if (notFound.equals("Y") || hFirstSeqno.trim().length() < 1) {
            // 一般卡卡號已用完 ,error
            if(emapGroupCode.trim().compareTo("0000") == 0) {
               showLogMessage("D","","Error:'0000' card_type=" + emapCardType + " 卡號已用完");
                hCallErrorDesc = "Error:'0000' card_type=" + emapCardType + " 卡號已用完";
                return (1);
            }

            // 卡號區間抓取不到,先到ptr_group_card抓取欄位org_cardno_flag
            // 檢核是否可適用一般卡號區間 :
            // A.適用一般卡號 group_code='0000'
            // B.不適用一般卡號區間,表示此group_code之卡號區間以用完
            tmpInt = chkUseOrgCardnoFlag();
            if (debug == 1)
                showLogMessage("I", "", "888 org int=[" + tmpInt + "] ");
            if (tmpInt == 0) /* 適用 */
            {
                hGroupCode = "0000";
            } else {
                showLogMessage("D", "", "Error: group,card_type [" + emapGroupCode + ","
                                      + emapCardType + "] 卡號已用完");
                return (1); /* 抓取不到卡號 */
            }

            selectSQL = " min(bin_no)   as bin_no     , " 
                      + " min(seqno)    as seqno  ";
            daoTable  = "crd_seqno_log";
            whereStr  = "WHERE group_code   =  ?  " 
                      + "  and card_type    =  ?  "  
                      + "  and card_flag    = '1' "
                      + "  and reserve     <> 'Y' " 
                      + "  and card_type_sort in (select min(card_type_sort)  " 
                                                 + "from crd_seqno_log        "
                                                 + "WHERE group_code   =  ?   " 
                                                 + "  and card_type    =  ?   " 
                                                 + "  and card_flag    = '1'  "
                                                 + "  and reserve     <> 'Y') " ;
            setString(1, hGroupCode);
            setString(2, emapCardType);
            setString(3, hGroupCode);
            setString(4, emapCardType);

            if (debug == 1) showLogMessage("I", "", "888 h_group=[" + hGroupCode + "] ");

            recCnt = selectTable();

            hBinNo = getValue("bin_no");
            hFirstSeqno = getValue("seqno");

            if (notFound.equals("Y")) {
                showLogMessage("D", "", "Error: select from crd_seqno_log  卡號已用完");
                hCallErrorDesc = "Error: select from crd_seqno_log  卡號已用完";
                return (1);
            }
        }

        return (0);
}
// ************************************************************************
public int getRandomSeqno(String begSeqno,String endSeqno ,int n) throws Exception 
{
		SecureRandom rd = SecureRandom.getInstance("SHA1PRNG");
		int hBegSeqno = Integer.parseInt(begSeqno.replaceAll(",", "").trim());
		int hEndSeqno = Integer.parseInt(endSeqno.replaceAll(",", "").trim());
		long fseqno = (long)hBegSeqno;
		if(n<=3) {
			int rnum = rd.nextInt(hEndSeqno - hBegSeqno);
			fseqno = (long)(hBegSeqno) + (long) (rnum);
		}		
//        int  rnum       = comc.getNextRandom(h_end_seqno - h_first_seqno);
       
if(debug == 1)   showLogMessage("I", "", "888 random="+hBegSeqno+","+fseqno);

        hRandomSeqno = comm.fillZero(Long.toString(fseqno), 9);
        String tmpX15 = hBinNo + hRandomSeqno;
        String hsChkDif = comm.cardChkCode(tmpX15);
        if (!comm.isNumber(hsChkDif)) {
            String err1 = "Error: 檢查碼錯誤=[" + tmpX15 + "][" + hsChkDif + "]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        int retCode = selectCrdCard0(tmpX15 + hsChkDif);
        if (retCode != 0) {
            hCallErrorDesc = "Error:此區間內有卡號已存在卡檔內!";
            showLogMessage("I", "", hCallErrorDesc);
            return (1);
        }
        hSeqno = hRandomSeqno + hsChkDif;
        return (0);
}
// ************************************************************************
public int chkCrdProhibit() throws Exception 
{
        selectSQL = " count(*) as star_cnt ";
        daoTable  = "crd_prohibit ";
        whereStr  = "WHERE card_no     = ?  ";

        setString(1, emapCardNo);

        int recCnt = selectTable();

        if (getValueInt("star_cnt") > 0)
            return (1);

        return (0);
}
// ************************************************************************
public int getCardNo() throws Exception 
{
        // 若編列卡號成功,則傳回0 */
	    //V2.01.02 
	
	    extendField = "ccrg.";
        selectSQL = " * from (select bin_no,"
        		  + "beg_seqno,"
        		  + "end_seqno,"
        		  +"(end_seqno - beg_seqno) - ( " 
        		  + "    (select count(*) " 
        		  + "     from crd_seqno_log a "
        		  + "     where a.reserve='Y' " 
        		  + "     and a.card_type = crd_cardno_range.card_type "
        		  + "     and a.group_code = crd_cardno_range.group_code and a.bin_no = crd_cardno_range.bin_no "
        		  + " and substr(a.seqno,1,9) >= crd_cardno_range.beg_seqno"
        		  + " and substr(a.seqno,1,9) <= crd_cardno_range.end_seqno ) + " 
        		  + "    (select count(*) "
        		  + "     from crd_prohibit a "
        		  + "     where substr(a.card_no,1,6) = crd_cardno_range.bin_no "
        		  + "     and substr(a.card_no,7,9) >= crd_cardno_range.beg_seqno "
        		  + "     and substr(a.card_no,7,9) <= crd_cardno_range.end_seqno) " 
        		  + ") as unuse ";
        daoTable = "crd_cardno_range";
        whereStr = "WHERE group_code   =  ?  " 
                 + " and card_type    =  ?  " 
                 + " and card_flag    = '1' "
                 + " and post_flag    = 'Y' ) "
                 + " where unuse > 0 "
                 + " order by unuse "
                 + " FETCH FIRST 1 ROW ONLY   ";
        setString(1, emapGroupCode);
        setString(2, emapCardType);

        int recCnt = selectTable();
        hBinNo = getValue("ccrg.bin_no");
        hBegSeqno = getValue("ccrg.beg_seqno");
        hEndSeqno = getValue("ccrg.end_seqno");

        if (notFound.equals("Y")) {
        	String err1 = "Error: 參數設定錯誤=[" + emapGroupCode + "][" + emapCardType + "]";
        	String err2 = "";
        	comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
        int n = 1;
        while(true) {

        	if(getRandomSeqno(hBegSeqno, hEndSeqno ,n)!=0) continue;
        
        	extendField = "cslg.";
        	selectSQL = "bin_no,"
        			  + "seqno,"
        		      + "reserve";
        	daoTable = "crd_seqno_log";
        	whereStr = "WHERE bin_no   =  ?  " 
        			 + "  and seqno    =  ?  " 
                     + "  and reserve    =  'Y'  ";

        	setString(1, hBinNo);
        	setString(2, hSeqno);
        	recCnt = selectTable();
        	if (recCnt>0) {
            	if(++n>3) {
            		hBegSeqno = String.format("%09d", comStr.ss2int(hBegSeqno) + 1);
            		if(comStr.ss2int(hBegSeqno) > comStr.ss2int(hEndSeqno)) {
            			comcr.errRtn(String.format("該卡號區間無可使用的卡號，團代 = %s ,卡種 = %s ,BIN_NO = %s ,流水號區間起 = %s  ,流水號區間迄 = %s"
            					, emapGroupCode,emapCardType,hBinNo,hBegSeqno,hEndSeqno), "", comcr.hCallBatchSeqno);
            		}
            	}
        		continue;
        	}
        	if (notFound.equals("Y")) {
                return (0);
        	}
        	
        }
}
// ************************************************************************
public int combineCardNo() throws Exception 
{
        // 組合卡號
        emapBinNo = hBinNo;
        emapCardNo = hBinNo + hSeqno;

        int tmpInt1 = chkCrdProhibit(); // > 0 禁用
        if (debug == 1)
            showLogMessage("I", "", "8888  chk bit=" + tmpInt1);
        if (tmpInt1 > 0) {
        	//update_crd_seqno_log("2");
            return (1);
        } else {
            //update_crd_seqno_log("1");
            insertCrdSeqnoLog();
        }

        return (0);
}
// ************************************************************************
public int updateCrdSeqnoLog(String iRsnCode) throws Exception 
{
        selectSQL = "card_item     ";
        daoTable  = "crd_item_unit ";
        whereStr  = "where unit_code    = ?  "; 

        setString(1, emapUnitCode);
        tmpInt = selectTable();
        String hWhtrCardItem = getValue("card_item");

        updateSQL = " reserve          = 'Y' , " 
                  + " reason_code      = ?   , " 
                  + " use_date         = ?   , "
                  + " use_id           = ?   , " 
                  + " card_item        = ?   , " 
                  + " unit_code        = ?   , " 
                  + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
                  + " mod_pgm          = ?     ";
        daoTable  = "crd_seqno_log";
        whereStr  = "WHERE rowid       = ? ";

        setString(1, iRsnCode);
        setString(2, sysDate);
        setString(3, javaProgram);
        setString(4, hWhtrCardItem);
        setString(5, emapUnitCode);
        setString(6, sysDate + sysTime);
        setString(7, javaProgram);
        setRowId( 8, seqnoRowid);

        int recCnt = updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_seqno_log   error[not find]";
            String err2 = iRsnCode;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return (0);
}
// ************************************************************************
public int updateCrdEmapErr() throws Exception {

if (debug == 1) showLogMessage("D", "", " UPADTE Err=["+ hErrorFlag +"]"+totalErr);
    
	emapCheckCode = "D"+String.format("%02d", hErrorFlag);
    
    updateSQL = " check_code       = ? , " 
              + " mod_time = timestamp_format(?,'YYYYMMDDHH24MISS') , "
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
public int updateCrdEmapTmp() throws Exception 
{

        emapCardnoCode = "0";
        if (emapCardNo.trim().length() < 1)
            emapCardnoCode = "1";

        // showLogMessage("I",""," UPDATE ID=[" + emap_apply_id + "]" + emap_card_no );

        updateSQL = " card_no          = ? , " 
                  + " bin_no           = ? , " 
                  + " cardno_code      = ? , "
                  + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , " 
                  + " mod_pgm          = 'CrdB005' ";
        daoTable  = "crd_emap_tmp";
        whereStr  = "WHERE rowid       = ? ";

        setString(1, emapCardNo);
        setString(2, emapBinNo);
        setString(3, emapCardnoCode);
        setString(4, sysDate + sysTime);
        setRowId(5, emapRowid);

        int recCnt = updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emap_tmp error[not find]";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return (0);
}
// ************************************************************************
public int writeSupData() throws Exception 
{

        selectSQL = " card_no as major_card_no ";
        daoTable  = "crd_emap_tmp";
        whereStr  = "WHERE apply_id     =  ?  " 
                  + "  and apply_id_code=  ?  " 
                  + "  and group_code   =  ?  "
                  + "  and card_type    =  ?  " 
                  + "  and check_code   = '000' " 
                  + "FETCH FIRST 1 ROW ONLY   ";
        setString(1, emapPmId);
        setString(2, emapPmIdCode);
        setString(3, emapGroupCode);
        setString(4, emapCardType);

        int recCnt = selectTable();

        // showLogMessage("I",""," major pm_id=[" + emap_pm_id + "]" +
        // getValue("major_card_no"));

        if (!notFound.equals("Y")) {
            updateSQL = " major_card_no    = ? , "
                      + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
                      + " mod_pgm          = 'CrdB005-1' ";
            daoTable = "crd_emap_tmp";
            whereStr = "WHERE rowid    = ? ";

            setString(1, getValue("major_card_no"));
            setString(2, sysDate + sysTime);
            setRowId(3, emapRowid);

            recCnt = updateTable();

            if (notFound.equals("Y")) {
                String err1 = "update_crd_emap_tmp 1 error[not find]";
                String err2 = "";
                comcr.errRtn(err1, err2, hCallBatchSeqno);
            }
        }

        return (0);
}
//************************************************************************
public void insertCrdSeqnoLog() throws Exception 
{
//2: 緊急替代用　5:HCE TPAN用 1:一般用    card_flag
//4:測試用  6:保留                        REASON_CODE
    selectSQL = "card_item     ";
    daoTable  = "crd_item_unit ";
    whereStr  = "where unit_code    = ?  "; 

    setString(1, emapUnitCode);
    tmpInt = selectTable();
    String hWhtrCardItem = getValue("card_item");
     dateTime();        
     setValueInt("card_type_sort" , 0);
     setValue("bin_no"            , hBinNo);
     setValue("SEQNO"             , hSeqno);
     setValue("card_type"         , emapCardType);
     setValue("group_code"        , emapGroupCode);
     setValue("card_flag"         , "1" );
     setValue("reserve"           , "Y");
     setValue("reason_code"       , "1");
     setValue("use_date"          , sysDate);
     setValue("use_id"            , javaProgram);
     setValue("card_item"         , hWhtrCardItem);
     setValue("unit_code"         , emapUnitCode);
     setValue("trans_no"          , "");
     setValue("seqno_old"         , hSeqno.substring(0,9));
     setValue("CRT_DATE"          , sysDate);
     setValue("MOD_TIME"          , sysDate + sysTime);
     setValue("MOD_PGM"           , javaProgram);

     daoTable = "crd_seqno_log";

     insertTable();

     if (dupRecord.equals("Y")) {
         String err1 = "crd_seqno_log       error[dupRecord]";
         String err2 = "";
         comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
     }
     return;
}
//************************************************************************
public int selectCrdCard0(String cardNo) throws Exception 
{
     selectSQL = "card_no      ";
     daoTable = "CRD_CARD";
     whereStr = "WHERE CARD_NO = ? ";

     if (debugD == 1)
         showLogMessage("I", "", "     11.1 card=[" + cardNo + "]");

     setString(1, cardNo);
     int recCnt = selectTable();
     if (recCnt > 0)
         return (1);

     selectSQL = "card_no      ";
     daoTable = "DBC_CARD";
     whereStr = "WHERE CARD_NO = ? ";
     setString(1, cardNo);
     recCnt = selectTable();
     if (recCnt > 0)
         return (1);

     return (0);
}
// ************************************************************************
public void initRtn() throws Exception 
{
        emapRowid = "";
        emapGroupCode = "";
        emapCardType = "";
        emapUnitCode = "";
        emapApplyId = "";
        emapApplyIdCode = "";
        emapPmId = "";
        emapPmIdCode = "";
        emapCheckCode = "000";
        emapCardNo = "";
        emapBinNo = "";
        emapCardnoCode = "";
        seqnoRowid = "";

        hGroupCode = "";
        hFirstSeqno = "";
        hRandomSeqno = "";
}
// ************************************************************************
} // End of class FetchSample

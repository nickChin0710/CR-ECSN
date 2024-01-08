/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/06/30  V1.01.01  Lai        Initial                                    *
* 106/11/08  V1.01.03  SUP        get_pin/pvv/cvv/icvv struct pin/pvv/cvv/csc*
* 109-07-07  V1.02.00  Ryan       ptr_keys_table change to ptr_hsm_keys      *
* 109-07-07  V1.02.00  Wilson     msg_header chagne value                    *
* 109/12/18  V1.00.02   shiyuqi       updated for project coding standard   *
* 111/12/15  V1.00.03  Wilson     M跟J的ICVV要用V的KEY                          *
* 111/12/30  V1.00.04  Wilson     移除星座卡相關邏輯判斷                                                                           *
* 112/02/07  V1.00.05  Wilson     combo卡增加判斷to_ibm_date                    *
* 112/02/14  V1.00.06  Wilson     to_ibm_date改為rtn_ibm_date                 *
* 112/06/19  V1.00.07  Wilson     where條件增加check_code = ""                 *
* 112/12/03  V1.00.08  Wilson     crd_item_unit不判斷卡種                                                                *
*****************************************************************************/
package Crd;
 
import com.*;
import bank.Auth.HSM.HsmUtil;

import java.io.*;
import java.net.Socket;
import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD003 extends AccessDAO {
    private String progname = "產生PVV,CVV,CVC2,PIN,CSC,ICVV 112/12/03  V1.00.08 ";
    private Map<String, Object> resultMap;

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    int debugD = 0;

    String errMsg = "";
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

    PvvOutbuf gpvvRep = new PvvOutbuf();
    PvvInbuf gpvvReq = new PvvInbuf();
    PinInbuf gpinReq = new PinInbuf();
    PinOutbuf gpinRep = new PinOutbuf();
    CvvInbuf gcvvReq = new CvvInbuf();
    CvvOutbuf gcvvRep = new CvvOutbuf();
    CscOutbuf gcscRep = new CscOutbuf();
    CscInbuf gcscReq = new CscInbuf();

    String mbosCardNo = "";
    String mbosEmbossSource = "";
    String mbosEmbossReason = "";
    String mbosCardType = "";
    String mbosBinNo = "";
    String mbosUnitCode = "";
    String mbosValidTo = "";
    String mbosIcFlag = "";
    String mbosRowid = "";

    String mbosPvv = "";
    String mbosCvv = "";
    String mbosTransCvv2 = "";
    String mbosCsc = "";
    String mbosPinBlock = "";
    String mbosPvki = "";
    String mbosIcvv = "";
    String tempBinType = "";
    String pctpServiceCode = "";
    String pktbEcsPvk1 = "";
    String pktbEcsPvk2 = "";

    String[] pvkKey1 = new String[6];
    String[] pvkKey2 = new String[6];
    int racalPort = 0;
    String racalServer = "";

    // SUP
    String hPktbEcsCvk1;
    String hPktbEcsCvk2;
    String hPktbEcsIcvv1;
    String hPktbEcsIcvv2;
    String realCardno;
    String hMbosValidTo;
    String hPctpServiceCode;
    String hMbosPvki;
    String hMbosCardNo;
    String hPktbEcsPvk2;
    String hPktbEcsPvk1;
    String mbosServiceCode;
    String mbosCvv2;
    String mbosComboIndicator;
    String cmbRtnIbmDate;
    
    Socket socket = null;

    // Racal
    String racalSERVER;
    int racalPORT;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdD003 proc = new CrdD003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (args.length > 2) {
                String err1 = "CrdD003  [seq_no]\n";
                String err2 = "CrdD003  [seq_no]";
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
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                comcr.hCallBatchSeqno = "no-call";
            }

            comcr.hCallRProgramCode = javaProgram;
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

            selectHsmIP();
            
            if (connectRacal() != 0) {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
                errMsg = "connect_racal           error";
                comcr.errRtn(errMsg, "", hCallBatchSeqno);
            }
            
            dateTime();
            selectPtrBusinday();

            totalCnt = 0;

            selectCrdEmboss();

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
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("BUSINESS_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");
    }

    // ************************************************************************
    public void selectCrdEmboss() throws Exception {

        int flag = 0;
        int totalNum = 0;

        selectSQL = "   decode(length(card_no),15,card_no||'0',card_no) as card_no " 
                  + " , emboss_source         " + " , emboss_reason         " 
                  + " , card_type             " + " , bin_no                "
                  + " , unit_code             " + " , valid_to              " 
                  + " , ic_flag               " + " , combo_indicator       "
                  + " , rowid        as rowid ";
        daoTable  = "crd_emboss  ";
        whereStr  = "where card_no     <> ''   " + "  and pin_block    = ''   " 
                  + "  and to_nccc_code = 'Y'  "
                  + "  and check_code  = ''   "
                  + "  and reject_code  = ''   " 
                  + "  and in_main_date = ''   ";

        openCursor();

        while (fetchTable()) {
            initRtn();

            mbosCardNo = getValue("card_no");
            mbosEmbossSource = getValue("emboss_source");
            mbosEmbossReason = getValue("emboss_reason");
            mbosCardType = getValue("card_type");
            mbosBinNo = getValue("bin_no");
            mbosUnitCode = getValue("unit_code");
            mbosValidTo = getValue("valid_to");
            mbosIcFlag = getValue("ic_flag");
            mbosComboIndicator = getValue("combo_indicator");

            mbosRowid = getValue("rowid");

            processDisplay(5000); // every nnnnn display message
            if (debug == 1) {
                showLogMessage("I", "", "\n888 Card=[" + mbosCardNo + "]");
                showLogMessage("I", "", "  888   id=[" + mbosCardType + "]");
                showLogMessage("I", "", "  888   to=[" + mbosValidTo + "]"+ mbosEmbossReason);
            }

            selectPtrHsmKeys();
            
            if(!mbosComboIndicator.equals("N")) {
            	checkCrdCombo();
            	
            	if(cmbRtnIbmDate.equals("")) {
            		continue;
            	}
            }
            
            // tmp_int = sel_ptr_bintable();

            flag = 0;

            selectPtrCardType();

            if (debug == 1)
                showLogMessage("I", "", "  888  To =[" + mbosValidTo + "]");

            totalNum = 0;
            // ********************************
            // 星座卡用舊的pin_block
            // ********************************
            int xnum = 0;
                
            for (int int1 = 2; int1 < 6; int1++) {                   
            	totalNum = totalNum + Integer.parseInt(mbosValidTo.substring(int1, int1 + 1));                
            }
                
            xnum = totalNum % 6; // 取餘數
                               
            pktbEcsPvk1 = pvkKey1[xnum];                
            pktbEcsPvk2 = pvkKey2[xnum];
                
            if (debug == 1) {
            	showLogMessage("I", "", "  888  To =[" + mbosPvki + "]" + xnum);
            }                            	
            	                
            getPin();
                
            if(debug ==1) {
            	showLogMessage("I", "", "  step 666 " );
            }
            	           
            getPvv();
            getCvv();

            if (mbosIcFlag.trim().equals("Y")) {
                getIcvv();
            }
            
            if(debug ==1) {
            	showLogMessage("I", "", "  777 ic=" + mbosIcFlag +","+ mbosIcvv);
            }

            tmpChar = comc.transPasswd(0, mbosPvv);
            mbosPvv = tmpChar;
            tmpChar = comc.transPasswd(0, mbosCvv);
            mbosCvv = tmpChar;

            if (mbosIcvv.length() > 0) {
                tmpChar = comc.transPasswd(0, mbosIcvv);
                mbosIcvv = tmpChar;
            }

            totalCnt++;

            updateCrdEmboss();
        }
    }

    // ************************************************************************
    public int selPtrBintable() throws Exception {
        selectSQL = " b.bin_type ";
        daoTable  = "ptr_bintable b ";
        whereStr  = "WHERE b.bin_no   = ?  "
                  + "FETCH FIRST 1 ROW ONLY";
        setString(1, mbosBinNo);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_card_type  error[not find]";
            String err2 = mbosCardType;
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        tempBinType = getValue("bin_type");
        if (debug == 1)
            showLogMessage("D", "", " 888 select type =[" + tempBinType + "] ");

        return (0);
    }

    // ************************************************************************
    public int selectPtrHsmKeys() throws Exception {
    	String sqls = getSqlVMJ(mbosCardNo);
        selectSQL = sqls
                  + " hsm_port1 ,hsm_ip_addr1 ,ecs_csck1       ";
        daoTable = " ptr_hsm_keys ";
        whereStr = " where hsm_keys_org ='00000000'  ";

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_hsm_keys error[not find]";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        hPktbEcsPvk1 = getValue("ecs_pvk1");
        hPktbEcsPvk2 = getValue("ecs_pvk2");
        hPktbEcsCvk1 = getValue("ecs_cvk1");
        hPktbEcsCvk2 = getValue("ecs_cvk2");
        hPktbEcsIcvv1 = getValue("ecs_icvv1");
        hPktbEcsIcvv2 = getValue("ecs_icvv2");

        pvkKey1[0] = getValue("ecs_pvk1");
        pvkKey1[1] = getValue("ecs_pvk1");
        pvkKey1[2] = getValue("ecs_pvk1");
        pvkKey1[3] = getValue("ecs_pvk1");
        pvkKey1[4] = getValue("ecs_pvk1");
        pvkKey1[5] = getValue("ecs_pvk1");
        pvkKey2[0] = getValue("ecs_pvk2");
        pvkKey2[1] = getValue("ecs_pvk2");
        pvkKey2[2] = getValue("ecs_pvk2");
        pvkKey2[3] = getValue("ecs_pvk2");
        pvkKey2[4] = getValue("ecs_pvk2");
        pvkKey2[5] = getValue("ecs_pvk2");
        racalPort = getValueInt("hsm_port1");
        racalServer = getValue("hsm_ip_addr1");
        mbosPvki = getValue("ecs_pvki");
        return (0);
    }

    // ************************************************************************
    public int checkCrdCombo() throws Exception {
    	
    	cmbRtnIbmDate = "";
    	
    	selectSQL = "rtn_ibm_date ";
        daoTable = "crd_combo";
        whereStr = "where card_no    = ?  ";

        setString(1, mbosCardNo);

        int recordCnt = selectTable();

        if(recordCnt > 0) {
        	cmbRtnIbmDate = getValue("rtn_ibm_date");
        }        
        
        return (0);
    }
    
    // ************************************************************************
    public int selectPtrCardType() throws Exception {
        /*
         * if(mbos_ic_flag.trim().equals("Y")) { }
         */
        selectSQL = "service_code ";
        daoTable = "crd_item_unit ";
        whereStr = "where unit_code = ?   ";

        setString(1, mbosUnitCode);

        int recordCnt = selectTable();

        if (recordCnt > 0) {
            pctpServiceCode = getValue("service_code");
        } else {
            String err1 = "select_crd_item_unit  error[not find]" + mbosUnitCode;
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
        if (debug == 1)
            showLogMessage("I", "", " 888 select item_unit=[" + pctpServiceCode + "]"
                              + mbosUnitCode + "," + recordCnt);

        mbosServiceCode = pctpServiceCode;
        return (0);
    }

    // ************************************************************************
     public int updateCrdEmboss() throws Exception {

        mbosTransCvv2 = comc.transPasswd(0, mbosCvv2);
        if (debug == 1)
            showLogMessage("I", "", " update emboss cvv2=" + mbosPvv + "," + mbosTransCvv2
                                                     + "," + mbosCvv2);

        updateSQL = "pvv                 =  ? , " + "cvv                 =  ? , " 
                  + "trans_cvv2          =  ? , " + "csc                 =  ? , " 
                  + "pin_block           =  ? , " + "pvki                =  ? , "
                  + "ic_cvv              =  ? , " + "mod_pgm             =  ? , "
                  + "mod_time            = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable = "crd_emboss";
        whereStr = "where rowid   = ? ";

        setString(1, mbosPvv);
        setString(2, mbosCvv);
        setString(3, mbosTransCvv2);
        setString(4, mbosCsc);
        setString(5, mbosPinBlock);
        setString(6, mbosPvki);
        setString(7, mbosIcvv);
        setString(8, javaProgram);
        setString(9, sysDate + sysTime);
        setRowId(10, mbosRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss    error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public void getPin() throws Exception {
        String tmpstr = "";
        String tmpStr1 = "";
        gpinReq.msgHeader = "BANK";
        gpinReq.cmdCode = "JA";

        tmpStr1 = mbosCardNo.substring(1, 2);
        gpinReq.cardNo = String.format("%12.12s", mbosCardNo.substring(3));

        gpinReq.pinLen = "04";
        gpinReq.delimiter = Character.toString((char) 25);
        
        gpinReq.msgEnder = "BANK";

        if (debug == 1)
            showLogMessage("I", "", "  step 1.1=" + gpinReq.allText());
        String tmp = callRacal("JA");
        if (debug == 1)
            showLogMessage("I", "", "  step 1.2=" + tmp);
        splitPinOutbuf(tmp);

        if (!gpinRep.errorCode.substring(0, 2).equals("00")) {
            socket.close();
            socket = null;
            comcr.errRtn(String.format("RECAL JA process error!", gpinRep.errorCode), "", hCallBatchSeqno);
        }
        tmpstr = String.format("%5.5s", gpinRep.pin);
        mbosPinBlock = tmpstr;
    }

    // ************************************************************************
    public void getPvv() throws Exception {
        String tmpstr = "";

        gpvvReq.msgHeader = "BANK";
        gpvvReq.cmdCode = "DG";
        gpvvReq.pvkPair1 = hPktbEcsPvk1;
        gpvvReq.pvkPair2 = hPktbEcsPvk2;
        gpvvReq.pin        = gpinRep.pin;
        gpvvReq.cardNo = String.format("%12.12s", mbosCardNo.substring(3));
        gpvvReq.pvki       = mbosPvki;
        gpvvReq.delimiter  = Character.toString((char) 25);
        gpvvReq.msgEnder = "BANK";

        if (debug == 1) showLogMessage("I", "", "  step 2.1=" + gpvvReq.allText());

        String tmp = callRacal("DG");

        if (debug == 1) showLogMessage("I", "", "  step 2.2=" + tmp);

        splitPvvOutbuf(tmp);

        if (!gpvvRep.errorCode.substring(0, 2).equals("00")) {
            socket.close();
            socket = null;
            comcr.errRtn("RECAL DG process error!", "", hCallBatchSeqno);
        }
        tmpstr = String.format("%4.4s", gpvvRep.pvv);
        mbosPvv = tmpstr;
        return;
    }

    // ************************************************************************
    public void getCvv() throws Exception {
        String realCardno = "";
        String tmpstr = "";

        gcvvReq.msgHeader = "BANK";
        gcvvReq.cmdCode = "CW";
        gcvvReq.cvkPair1 = hPktbEcsCvk1;
        gcvvReq.cvkPair2 = hPktbEcsCvk2;
        if (mbosCardNo.substring(0, 4).equals("4000")) {
            realCardno = "9000";
        } else {
            realCardno = mbosCardNo.substring(0, 4);
        }
        realCardno += mbosCardNo.substring(4);
        gcvvReq.cardNo = realCardno;
        gcvvReq.delimiter1 = ";";
        gcvvReq.expireDate = String.format("%4.4s", mbosValidTo.substring(2));
        gcvvReq.serviceCode = mbosServiceCode;
        gcvvReq.delimiter = Character.toString((char) 25);
        gcvvReq.msgEnder = "BANK";
        if (debug == 1)
            showLogMessage("I", "", "  step 3.1=" + gcvvReq.allText());
        String tmp = callRacal("CW");
        if (debug == 1)
            showLogMessage("I", "", "  step 3.2=" + tmp);
        splitcvvOutbuf(tmp);
        if (!gcvvRep.errorCode.equals("00")) {
            showLogMessage("I", "", String.format("888 buf=[%s]", gcvvReq));
            socket.close();
            socket = null;
            comcr.errRtn(String.format("RECAL CW cvv2 process error[%2.2s]!"
                         , gcvvRep.errorCode), "", hCallBatchSeqno);
        }
        mbosCvv = gcvvRep.cvv;

        tmpstr = String.format("%3.3s", gcvvRep.cvv);
        mbosCvv = tmpstr;
        /**************************************
         * cvv2 service code改為'000'
         **************************************/
        gcvvReq.serviceCode = "000";
        if (debug == 1)
            showLogMessage("I", "", "  step 4.1=" + gcvvReq.allText());
        tmp = callRacal("CW");
        if (debug == 1)
            showLogMessage("I", "", "  step 4.2=" + tmp);
        splitcvvOutbuf(tmp);
        if (debug == 1)
            showLogMessage("I", "", "  step 4.3=" + gcvvRep.errorCode);
        if (!gcvvRep.errorCode.equals("00")) {
            socket.close();
            socket = null;
            comcr.errRtn("RECAL CW cvv2 1            !", "", hCallBatchSeqno);
        }
        tmpstr = String.format("%3.3s", gcvvRep.cvv);
        mbosCvv2 = tmpstr;
        if (debug == 1)
            showLogMessage("I", "", "  step 4.4 cvv2=" + tmpstr);
    }

    // ************************************************************************
    public void getIcvv() throws Exception {
        String realCardno = "";
        String tmpstr = "";

        gcvvReq.msgHeader = "BANK";
        gcvvReq.cmdCode = "CW";
        gcvvReq.cvkPair1 = hPktbEcsIcvv1;
        gcvvReq.cvkPair2 = hPktbEcsIcvv2;
        if (mbosCardNo.substring(0, 4).equals("4000")) {
            realCardno = "9000";
        } else {
            realCardno = mbosCardNo.substring(0, 4);
        }
        realCardno += mbosCardNo.substring(4);
        gcvvReq.cardNo = realCardno;
        gcvvReq.delimiter1 = ";";
        gcvvReq.expireDate = String.format("%4.4s", mbosValidTo.substring(2));
        gcvvReq.serviceCode = "999";
        gcvvReq.delimiter = Character.toString((char) 25);
        gcvvReq.msgEnder = "BANK";
        String tmp = callRacal("CW");
        splitcvvOutbuf(tmp);
        if (!gcvvRep.errorCode.equals("00")) {
            socket.close();
            socket = null;
            comcr.errRtn("RECAL CW cvv2 process error!", "", hCallBatchSeqno);
        }
        mbosIcvv = gcvvRep.cvv;

        tmpstr = String.format("%3.3s", gcvvRep.cvv);
        mbosIcvv = tmpstr;
        return;
    }

    // ************************************************************************
    public void initRtn() throws Exception {

        mbosCardNo = "";
        mbosEmbossSource = "";
        mbosEmbossReason = "";
        mbosCardType = "";
        mbosBinNo = "";
        mbosUnitCode = "";
        mbosValidTo = "";
        mbosIcFlag = "";
        mbosComboIndicator = "";
        mbosRowid = "";

        mbosPvv = "";
        mbosCvv = "";
        mbosTransCvv2 = "";
        mbosCsc = "";
        mbosPinBlock = "";
        mbosPvki = "";
        mbosIcvv = "";
        tempBinType = "";
        pctpServiceCode = "";
        pktbEcsPvk1 = "";
        pktbEcsPvk2 = "";
    }

    // ************************************************************************
    String callRacal(String type) throws IOException {
        String sLResult = "";

        HsmUtil lHsmUtil = new HsmUtil(racalSERVER, racalPORT);

        try {
            switch (type) {
            case "JA":
                sLResult = lHsmUtil.hsmCommandJA(gpinReq.cardNo, gpinReq.pinLen);
                break;
            case "DG":
                sLResult = lHsmUtil.hsmCommandDG(gpvvReq.pvkPair1 + gpvvReq.pvkPair2, gpvvReq.pin,
                        gpvvReq.cardNo, gpvvReq.pvki);
                break;
            case "CW":
                sLResult = lHsmUtil.hsmCommandCW(gcvvReq.cardNo, gcvvReq.expireDate, gcvvReq.serviceCode,
                		gcvvReq.cvkPair1, gcvvReq.cvkPair2);
                break;
            default:
                showLogMessage("I", "", " HsmUtil =[" + type + " != 'JA','DG','CW' " + "]");
            }

            showLogMessage("I", "", "  888 HsmUtil R=[" + sLResult + "]");
            if ("00".equals(sLResult.substring(0, 2))) {
                showLogMessage("I", "", "  成功，Result== " + sLResult.substring(2, sLResult.length()) + "]");
            } else {
                showLogMessage("I", "", "  失敗，Result== " + sLResult + "]");
                comcr.errRtn("RECAL " + type + " process error!", "", hCallBatchSeqno);
            }

            return sLResult;

        } catch (Exception e) {
            showLogMessage("I", "", "  Error HsmUtil !");
            e.printStackTrace();
        }
        return sLResult;
    }

    /***********************************************************************/
    int connectRacal() {
        try {
            racalSERVER = racalServer;
            racalPORT = racalPort;
            /*
             * String host =
             * InetAddress.getByName(RACAL_SERVER).getHostAddress(); int port =
             * RACAL_PORT;
             */
            String host = racalServer;
            int port = racalPort;

            if (debug == 1)
                showLogMessage("I", "", "  888 IP=[" + host + "]" + port);

            socket = new Socket(host, port);
            if (debug == 1)
                showLogMessage("I", "", "  888 IP OK=[" + host + "]" + port);

        } catch (IOException e) {
            showLogMessage("I", "", "RACAL CONNECT error !!");
            return 1;
        }
        showLogMessage("I", "", "RACAL CONNECT ok !!");

        return (0);
    }

    /***********************************************************************/
    class PinInbuf {
        String msgHeader;
        String cmdCode;
        String cardNo;
        String pinLen;
        String delimiter;
        String msgEnder;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader, 4);
            rtn += comc.fixLeft(cmdCode, 2);
            rtn += comc.fixLeft(cardNo, 12);
            rtn += comc.fixLeft(pinLen, 2);
            rtn += comc.fixLeft(delimiter, 1);
            rtn += comc.fixLeft(msgEnder, 4);
            rtn += comc.fixLeft(filler, 75);
            return rtn;
        }
    }

    /***********************************************************************/
    class PinOutbuf {
        String msgHeader;
        String respCode;
        String errorCode;
        String pin;
        String delimiter;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader, 4);
            rtn += comc.fixLeft(respCode, 2);
            rtn += comc.fixLeft(errorCode, 2);
            rtn += comc.fixLeft(pin, 5);
            rtn += comc.fixLeft(delimiter, 1);
            rtn += comc.fixLeft(filler, 60);
            return rtn;
        }
    }

    void splitPinOutbuf(String str) throws UnsupportedEncodingException {
        byte[] bytes        = str.getBytes("MS950");
        gpinRep.errorCode = comc.subMS950String(bytes, 0, 2);
        gpinRep.pin        = comc.subMS950String(bytes, 2, 5);
        /*
         * mark gpin_rep.msg_header = comc.subMS950String(bytes, 0, 4);
         * gpin_rep.resp_code = comc.subMS950String(bytes, 4, 2);
         * gpin_rep.error_code = comc.subMS950String(bytes, 6, 2); gpin_rep.pin
         * = comc.subMS950String(bytes, 8, 5); gpin_rep.delimiter =
         * comc.subMS950String(bytes, 13, 1); gpin_rep.filler =
         * comc.subMS950String(bytes, 14, 60);
         */
    }

    /***********************************************************************/
    class PvvInbuf {
        String msgHeader;
        String cmdCode;
        String pvkPair1;
        String pvkPair2;
        String pin;
        String cardNo;
        String pvki;
        String delimiter;
        String msgEnder;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader, 4);
            rtn += comc.fixLeft(cmdCode, 2);
            rtn += comc.fixLeft(pvkPair1, 16);
            rtn += comc.fixLeft(pvkPair2, 16);
            rtn += comc.fixLeft(pin, 5);
            rtn += comc.fixLeft(cardNo, 12);
            rtn += comc.fixLeft(pvki, 1);
            rtn += comc.fixLeft(delimiter, 1);
            rtn += comc.fixLeft(msgEnder, 4);
            rtn += comc.fixLeft(filler, 39);
            return rtn;
        }
    }

    /***********************************************************************/
    class PvvOutbuf {
        String msgHeader;
        String respCode;
        String errorCode;
        String pvv;
        String delimiter;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader, 4);
            rtn += comc.fixLeft(respCode, 2);
            rtn += comc.fixLeft(errorCode, 2);
            rtn += comc.fixLeft(pvv, 4);
            rtn += comc.fixLeft(delimiter, 1);
            rtn += comc.fixLeft(filler, 61);
            return rtn;
        }
    }

    void splitPvvOutbuf(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        gpvvRep.errorCode = comc.subMS950String(bytes, 0, 2);
        gpvvRep.pvv = comc.subMS950String(bytes, 2, 4);
        /*
         * mark gpvv_rep.msg_header = comc.subMS950String(bytes, 0, 4);
         * gpvv_rep.resp_code = comc.subMS950String(bytes, 4, 2);
         * gpvv_rep.error_code = comc.subMS950String(bytes, 6, 2); gpvv_rep.pvv
         * = comc.subMS950String(bytes, 8, 4); gpvv_rep.delimiter =
         * comc.subMS950String(bytes, 12, 1); gpvv_rep.filler =
         * comc.subMS950String(bytes, 13, 61);
         */
    }

    /***********************************************************************/
    class CvvInbuf {
        String msgHeader;
        String cmdCode;
        String cvkPair1;
        String cvkPair2;
        String cardNo;
        String delimiter1;
        String expireDate;
        String serviceCode;
        String delimiter;
        String msgEnder;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader, 4);
            rtn += comc.fixLeft(cmdCode, 2);
            rtn += comc.fixLeft(cvkPair1, 16);
            rtn += comc.fixLeft(cvkPair2, 16);
            rtn += comc.fixLeft(cardNo, 16);
            rtn += comc.fixLeft(delimiter1, 1);
            rtn += comc.fixLeft(expireDate, 4);
            rtn += comc.fixLeft(serviceCode, 3);
            rtn += comc.fixLeft(delimiter, 1);
            rtn += comc.fixLeft(msgEnder, 4);
            rtn += comc.fixLeft(filler, 37);
            return rtn;
        }
    }

    /***********************************************************************/
    class CvvOutbuf {
        String msgHeader;
        String respCode;
        String errorCode;
        String cvv;
        String delimiter;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader, 4);
            rtn += comc.fixLeft(respCode, 2);
            rtn += comc.fixLeft(errorCode, 2);
            rtn += comc.fixLeft(cvv, 3);
            rtn += comc.fixLeft(delimiter, 1);
            rtn += comc.fixLeft(filler, 62);
            return rtn;
        }
    }

    void splitcvvOutbuf(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        gcvvRep.errorCode = comc.subMS950String(bytes, 0, 2);
        gcvvRep.cvv        = comc.subMS950String(bytes, 2, 3);
        /*
         * mark gcvv_rep.msg_header = comc.subMS950String(bytes, 0, 4);
         * gcvv_rep.resp_code = comc.subMS950String(bytes, 4, 2);
         * gcvv_rep.error_code = comc.subMS950String(bytes, 6, 2); gcvv_rep.cvv
         * = comc.subMS950String(bytes, 8, 3); gcvv_rep.delimiter =
         * comc.subMS950String(bytes, 11, 1); gcvv_rep.filler =
         * comc.subMS950String(bytes, 12, 62);
         */
    }

    /***********************************************************************/
    class CscInbuf {
        String msgHeader;
        String cmdCode;
        String mode;
        String flag;
        String csck1;
        String cardNo;
        String expireDate;
        String delimiter;
        String msgEnder;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader, 4);
            rtn += comc.fixLeft(cmdCode, 2);
            rtn += comc.fixLeft(mode, 1);
            rtn += comc.fixLeft(flag, 1);
            rtn += comc.fixLeft(csck1, 32);
            rtn += comc.fixLeft(cardNo, 19);
            rtn += comc.fixLeft(expireDate, 4);
            rtn += comc.fixLeft(delimiter, 1);
            rtn += comc.fixLeft(msgEnder, 4);
            rtn += comc.fixLeft(filler, 63);
            return rtn;
        }
    }

    /***********************************************************************/
    class CscOutbuf {
        String msgHeader;
        String respCode;
        String errorCode;
        String mode;
        String csc1;
        String csc2;
        String csc3;
        String delimiter;
        String msgEnder;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader, 4);
            rtn += comc.fixLeft(respCode, 2);
            rtn += comc.fixLeft(errorCode, 2);
            rtn += comc.fixLeft(mode, 1);
            rtn += comc.fixLeft(csc1, 5);
            rtn += comc.fixLeft(csc2, 4);
            rtn += comc.fixLeft(csc3, 3);
            rtn += comc.fixLeft(delimiter, 1);
            rtn += comc.fixLeft(msgEnder, 4);
            rtn += comc.fixLeft(filler, 37);
            return rtn;
        }
    }

    void splitCscOutbuf(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        gcscRep.errorCode = comc.subMS950String(bytes, 0, 2);
        gcscRep.mode = comc.subMS950String(bytes, 2, 1);
        gcscRep.csc1 = comc.subMS950String(bytes, 3, 5);
        gcscRep.csc2 = comc.subMS950String(bytes, 8, 4);
        gcscRep.csc3 = comc.subMS950String(bytes, 12, 3);
        /*
         * mark gcsc_rep.msg_header = comc.subMS950String(bytes, 0, 4);
         * gcsc_rep.resp_code = comc.subMS950String(bytes, 4, 2);
         * gcsc_rep.error_code = comc.subMS950String(bytes, 6, 2); gcsc_rep.mode
         * = comc.subMS950String(bytes, 8, 1); gcsc_rep.csc1 =
         * comc.subMS950String(bytes, 9, 5); gcsc_rep.csc2 =
         * comc.subMS950String(bytes, 14, 4); gcsc_rep.csc3 =
         * comc.subMS950String(bytes, 18, 3); gcsc_rep.delimiter =
         * comc.subMS950String(bytes, 21, 1); gcsc_rep.msg_ender =
         * comc.subMS950String(bytes, 22, 4); gcsc_rep.filler =
         * comc.subMS950String(bytes, 26, 37);
         */
    }
    
	void selectHsmIP() throws Exception {
		int hPktbRacalPort = 0;
		String hPktbRacalIpAddr = "";
		sqlCmd = "select ";
		sqlCmd += " hsm_port1,";
		sqlCmd += " hsm_ip_addr1 ";
		sqlCmd += " from ptr_hsm_keys  ";
		sqlCmd += " where hsm_keys_org ='00000000' ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hPktbRacalPort = getValueInt("hsm_port1");
			hPktbRacalIpAddr = getValue("hsm_ip_addr1");
		}
		racalPort = hPktbRacalPort;
		racalServer = hPktbRacalIpAddr;
	}
    
	String getSqlVMJ(String aCardNo) throws Exception {
		String sqls = "";
		sqlCmd = "select bin_type from ptr_bintable where bin_no || bin_no_2_fm || '0000' <= ? ";
		sqlCmd += " and bin_no || bin_no_2_to || '9999' >= ? ";
		setString(1, aCardNo);
		setString(2, aCardNo);
		selectTable();
		String aBinType = getValue("bin_type");
		switch (aBinType) {
		case "V":
			sqls = " visa_pvka as ecs_pvk1, ";
			sqls += " visa_pvkb as ecs_pvk2, ";
			sqls += " visa_cvka as ecs_cvk1, ";
			sqls += " visa_cvkb as ecs_cvk2, ";
			sqls += " visa_pvki as ecs_pvki , ";
			sqls += " visa_cvka as ecs_icvv1, ";
			sqls += " visa_cvkb as ecs_icvv2, ";
			break;
		case "M":
			sqls = " master_pvka as ecs_pvk1, ";
			sqls += " master_pvkb as ecs_pvk2, ";
			sqls += " master_cvka as ecs_cvk1, ";
			sqls += " master_cvkb as ecs_cvk2, ";
			sqls += " master_pvki as ecs_pvki, "; 
			sqls += " visa_cvka as ecs_icvv1, ";
			sqls += " visa_cvkb as ecs_icvv2, ";
			break;
		case "J":
			sqls = " jcb_pvka as ecs_pvk1, ";
			sqls += " jcb_pvkb as ecs_pvk2, ";
			sqls += " jcb_cvka as ecs_cvk1, ";
			sqls += " jcb_cvkb as ecs_cvk2, ";
			sqls += " jcb_pvki as ecs_pvki, ";
			sqls += " visa_cvka as ecs_icvv1, ";
			sqls += " visa_cvkb as ecs_icvv2, ";
			break;
		default:
			sqls = " '' as ecs_pvk1, ";
			sqls += " '' as ecs_pvk2, ";
			sqls += " '' as ecs_cvk1, ";
			sqls += " '' as ecs_cvk2, ";
			sqls += " '' as ecs_pvki, ";
			sqls += " '' as ecs_icvv1, ";
			sqls += " '' as ecs_icvv2, ";
			break;
		}

		return sqls;
	}
} // End of class FetchSample

/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*    DATE    Version    AUTHOR                       DESCRIPTION              *
*  --------  -------------------  ------------------------------------------  *
* 104/12/01  V0.01.01  Lai        RECS-1041120-024 Creation                   *
* 105/08/03  V0.01.02  林志鴻           RECS-1041120-024 漏抓 v_card_no              *
* 106/01/10  V0.02.01  Allen      BECS-1060112-003 clear buffer first         *
* 106/04/05  V0.03.01  Hesyuan    RECS-s1060314-024 member_id, barcode_type   *
* 106/08/20  V0.04.01  already    RECS-s1060807-065 JCB白名單                                   *
*  109/07/06  V0.04.02    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V0.04.03    Zuwei     coding standard, rename field method                   *
* 111/01/19   V0.04.04 Justin     fix Denial of Service: Format String
******************************************************************************/
package com;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;

import com.CommCrd;

import bank.Auth.HSM.HsmUtil;

public class HceIssue extends AccessDAO {
    
    final int DEBUG =1;
    
    private final String PROGNAME = "HCE_ISSUE";
    String hCallBatchSeqno = "";
    
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    
    DekInbuf   gdekReq1 = new DekInbuf();
    DekOutbuf  gdekRep   = new DekOutbuf();
    CvvInbuf   gcvvReq   = new CvvInbuf();
    CvvOutbuf  gcvvRep   = new CvvOutbuf();
    HceIssueH sndHead   = new HceIssueH();
    HceIssueD sndDetl   = new HceIssueD();
    

    String[] dbname = new String[10];
    
    String hTempCardNo = "";
    String hHcetCardType = "";
    String hMHcetCardType = "";
    String hHcetGroupCode = "";
    String hMHcetGroupCode = "";
    String hHcetValidTo = "";
    String hMHcetValidTo = "";
    String hHcetActivationCode = "";
    String hHcetTrack2Dek = "";
    String hHcetCardNo = "";
    String hHcetWalletId = "";
    String hHcetMemberId = "";
    String hMHcetVCardNo = "";
    String hMHcetBinType = "";
    String hHcetServiceCode = "";
    String hHcetUnitCode = "";
    String hMHcetUnitCode = "";
    String hMHcetServiceId = "";
    String hHcetEcsCvk1 = "";
    String hHcetEcsCvk2 = "";
    String hHcetMobZekKek1 = "";
    String hHcetMobZekDek1 = "";
    String hHcetMobZekKek1Chk = "";
    String hHcetMobZekDek1Chk = "";
    String hHcetMobIpAddr = "";
    int   hHcetMobIpPort = 0;
    String hHcetMobVersionId = "";
    String hHcetServiceVer = "";
    
    String hTempIcvv = "";
    String hSystemDate = "";
    
    String errMsg = "";
    String tmpstr = "";
    String tempX04 = "";
    String tempX05 = "";
    String tempX100 = "";
    String voiceCode = "";
    String swKind = "";
    String swProd = "";
    String swType = "";
    String swMemberFlag = "";
    String swDiversifyFlag = "";
    String swIvcvc3Flag = "";
    String swEffcFlag = "";
    
    String racalServer = "";
    int    racalPort   = 0;

    int totCnt;

/***********************************************************************/
    public HceIssue(Connection conn[], String[] dbAlias) throws Exception {
        // TODO Auto-generated constructor stub
        super.conn = conn;
        setDBalias(dbAlias);
        setSubParm(dbAlias);

        comcr = new CommCrdRoutine(conn , dbAlias);
        dbname[0] = dbAlias[0];

        return;
    }

    /***********************************************************************/
    public String[] runHceIssue(String iCardNo, String iSeId, String iVCode, String iSwProd, String iType, String iMemberFlag, String iMemberId, String[] sndbufH, String[] sndbufD) throws Exception {
            
            selectPtrKeysTable();

// showLogMessage("I", "", "888 CARD_No=" + i_card_no );
            
            swProd           = iSwProd;
            swType           = iType;
            swMemberFlag    = iMemberFlag;
            
            swDiversifyFlag = "0";
            swIvcvc3Flag    = "0";
            swEffcFlag      = "0";
            voiceCode        = iVCode;
            
            hTempCardNo   = iCardNo;
            hHcetCardNo   = iCardNo;
            hHcetWalletId = iSeId;
            hHcetMemberId = iMemberId;
           
            sqlCmd  = "select to_char(sysdate, 'yyyymmdd') h_system_date";
            sqlCmd += "  from dual";
            hSystemDate = getValue(hSystemDate);

            process();
    
            sndbufH[0] = new String(sndHead.allText());
            sndbufD[0] = new String(sndDetl.allText());
            
            String rtnbuf[] = new String[5];
            rtnbuf[0] = "0";
            rtnbuf[1] = sndbufH[0];
            rtnbuf[2] = sndbufD[0];
            
        return rtnbuf;
}
/***********************************************************************/
private void process() throws Exception 
{
    
    sqlCmd  = "SELECT a.bin_type";
    sqlCmd +=      ", g.service_id"; //c.service_id
    sqlCmd +=      ", a.unit_code";
    sqlCmd +=      ", a.new_end_date";
    sqlCmd +=      ", a.group_code";
    sqlCmd +=      ", a.card_type";
    sqlCmd +=      ", d.v_card_no";
    sqlCmd += "  from ptr_group_card c,  crd_card a, crd_item_unit  g  ";
    sqlCmd += "  left outer join hce_card d";
    sqlCmd += "    on a.card_no     = d.card_no ";
    sqlCmd += "   and d.status_code = '0'";
    sqlCmd += " where 1=1  ";
    sqlCmd += "   and c.group_code  = decode(a.group_code, '', '0000', a.group_code)";
    sqlCmd += "   and c.card_type   = a.card_type";
    sqlCmd += "   and a.card_no     = ?";
    sqlCmd += "   and c.card_type = g.card_type "; //find service_id from crd_item_unit
    sqlCmd += "   and a.unit_code = g.unit_code "; //find service_id from crd_item_unit
    setString(1, hTempCardNo);
    int curNum = openCursor();
    
    while(fetchTable(curNum)) {
            hMHcetBinType = getValue("bin_type");
            hMHcetServiceId = getValue("service_id");
            hMHcetUnitCode = getValue("unit_code");
            hMHcetValidTo = getValue("new_end_date");
            hMHcetGroupCode = getValue("group_code");
            hMHcetCardType = getValue("card_type");
            hMHcetVCardNo = getValue("v_card_no");

            hHcetUnitCode = hMHcetUnitCode;
            hHcetValidTo = hMHcetValidTo;
            hHcetGroupCode = hMHcetGroupCode;
            hHcetCardType = hMHcetCardType;

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Data Process record=[%d]\n", totCnt));

            selectPtrCardType();
            selectPtrServiceVer();

            swKind = "0400";
            if (hMHcetBinType.equals("M"))
                swKind = "0500";
            if (hMHcetBinType.equals("J"))
                swKind = "0300";

            createHeader();
            createDetail();

        }
    closeCursor(curNum);
}
/***********************************************************************/
private void createHeader() 
{
        
        String tempFilename;

        tempFilename = String.format("A.10017.05.3.%02d.%s", 1, hSystemDate);

        sndHead.paymentScheme = swKind;
        sndHead.fileVersion   = hHcetMobVersionId;
        sndHead.fileName      = tempFilename;

        tempX05 = String.format("%05d", 1);
        sndHead.totRec        = tempX05   ;         
        sndHead.diversifyFlag = swDiversifyFlag;
        sndHead.prodFlag      = "0";
        if(swProd.equals("P"))
            sndHead.prodFlag = "1";
        sndHead.ivcvc3Flag = swIvcvc3Flag;
        sndHead.effcFlag   = swEffcFlag;
        sndHead.fiscFlag1 = " ";
        sndHead.fiscFlag2 = " ";
        sndHead.fiscFlag3 = " ";
        sndHead.fiscFlag4 = " ";
        sndHead.len         = "  ";
}
/***********************************************************************/
private void createDetail() throws Exception 
{
        
        String tmpStr;
        String szBuffer;

        sndDetl.seId       = hHcetWalletId;
        sndDetl.serviceId  = hMHcetServiceId;
        sndDetl.serviceVer = hHcetServiceVer;
        sndDetl.msisdn      = "0";/* 0:產生新 pan , 1:續用舊 pan */
        if(swType.equals("5") && (hMHcetVCardNo.length() > 0))
            sndDetl.msisdn = "1";/* 0:產生新 pan , 1:續用舊 pan */
        sndDetl.seType     = "05";

        tempX100 = String.format("26%-6.6sFFFFFFFF", voiceCode );
        
        getKEK(hHcetMobZekKek1, tempX100);
        hHcetActivationCode   = String.format("%16.16s", gdekRep.msgData);
        sndDetl.activationCode = hHcetActivationCode;
        sndDetl.binNo          = "      ";

        /* Track2加密 */
        tempX100 = String.format("%-16.16s%1s%-4.4s%-3.3s", hHcetCardNo, "D", hHcetValidTo.substring(2), hHcetServiceCode);
        getIcvv();

        szBuffer = String.format("%5.5s%3.3s%1.1s", "00000", hTempIcvv, "8");
        tmpStr  = String.format("%24.24s%9s0000000000000000000000000000000", tempX100 , szBuffer);

        getDEK(hHcetMobZekDek1, tmpStr);
        hHcetTrack2Dek = String.format("%64.64s" , gdekRep.msgData);

        sndDetl.track2Dek    = hHcetTrack2Dek;
        sndDetl.psn           = "00";                    /* 卡片序號*/
        sndDetl.effcDate     = hHcetValidTo.substring(2);
        sndDetl.ivcvc3T1     = "    ";
        sndDetl.ivcvc3T2     = "    ";
        sndDetl.pinIvcvc3T1 = "    ";
        sndDetl.pinIvcvc3T2 = "    ";
        sndDetl.pinMobile    = "                ";
        sndDetl.tpan          = "                ";
        
        if(swType.equals("5"))
           sndDetl.tpan = hMHcetVCardNo;
        
        /* member_id */  
        sndDetl.memberId = "                              ";
        if(swMemberFlag.equals("Y"))                               
           sndDetl.memberId = hHcetMemberId;  
        
        /* barcode_type */
        sndDetl.barcodeType = "  ";
        if(swMemberFlag.equals("Y"))  
           sndDetl.barcodeType = "01";
        
        sndDetl.len = "  ";

        return;
}
/***********************************************************************/
private void selectPtrServiceVer() throws Exception 
{
        hHcetServiceVer = "";
        sqlCmd  = "select service_ver ";
        sqlCmd += "  from ptr_service_ver";
        sqlCmd += " where bin_type = ? ";
        sqlCmd += " fetch first 1 rows only";
        setString(1, hMHcetBinType);
        int recordCnt = selectTable();
        if(notFound.equals("Y")) {
            comcr.errRtn("select ptr_service_ver not found!", "", hCallBatchSeqno);
        }
        if(recordCnt > 0) {
            hHcetServiceVer = getValue("service_ver");
        }
}
/***********************************************************************/
private void selectPtrKeysTable() throws Exception 
{
        hHcetEcsCvk1         = "";
        hHcetEcsCvk2         = "";

        hHcetMobZekKek1     = "";
        hHcetMobZekKek1Chk = "";
        hHcetMobZekDek1     = "";
        hHcetMobZekDek1Chk = "";
        hHcetMobIpAddr      = "";
        hHcetMobIpPort      = 0;
        /* 台灣行動支付基碼(ZEK) KEK/LMK  A組     檢核碼
        KEK(密碼加密)           32bit       6bit
                            DEK/LMK  A組     檢核碼
        DEK(Track2加密)        32bit       6bit   */
        sqlCmd  = "select mob_version_id,";
        sqlCmd +=       " ecs_cvk1,";
        sqlCmd +=       " ecs_cvk2,";
        sqlCmd +=       " mob_zek_kek1,";
        sqlCmd +=       " mob_zek_kek1_chk,";
        sqlCmd +=       " mob_zek_dek1,";
        sqlCmd +=       " mob_zek_dek1_chk,";
        sqlCmd +=       " mob_ip_addr,";
        sqlCmd +=       " mob_ip_port ";
        sqlCmd += " from ptr_keys_table ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if(notFound.equals("Y")) {
            comcr.errRtn("select ptr_keys_table not found!", "", hCallBatchSeqno);
        }
        if(recordCnt > 0) {
            hHcetMobVersionId   = getValue("mob_version_id");
            hHcetEcsCvk1         = getValue("ecs_cvk1");
            hHcetEcsCvk2         = getValue("ecs_cvk2");
            hHcetMobZekKek1     = getValue("mob_zek_kek1");
            hHcetMobZekKek1Chk = getValue("mob_zek_kek1_chk");
            hHcetMobZekDek1     = getValue("mob_zek_dek1");
            hHcetMobZekDek1Chk = getValue("mob_zek_dek1_chk");
            hHcetMobIpAddr      = getValue("mob_ip_addr");
            hHcetMobIpPort      = getValueInt("mob_ip_port");
        }
        racalPort   = hHcetMobIpPort;
        racalServer = hHcetMobIpAddr;
   
        showLogMessage("I", "", String.format("RACLA_IP [%s]:[%d]", racalServer, racalPort));
}
/***********************************************************************/
private void selectPtrCardType() throws Exception {
        hHcetServiceCode = "";

        sqlCmd  = "select service_code ";
        sqlCmd += "  from crd_item_unit";
        sqlCmd += " where card_type = ?";
        sqlCmd += "   and unit_code = ?";
        setString(1, hHcetCardType);
        setString(2, hHcetUnitCode);
        int recordCnt = selectTable();
        if( recordCnt > 0 ) {
            hHcetServiceCode = getValue("service_code");                
        }
}

/***********************************************************************/
private String callRacal(String type) throws IOException 
{
  String sLResult = "";

  HsmUtil lHsmUtil = new HsmUtil(racalServer, racalPort);

  try {
      switch (type) {
      case "CW": sLResult = lHsmUtil.hsmCommandCW(gcvvReq.cardNo, gcvvReq.expireDate 
                           , gcvvReq.serviceCode, hHcetEcsCvk1, hHcetEcsCvk2);
                 break;
      case "M0": sLResult = lHsmUtil.hsmCommandM0(gdekReq1.modeFlag, gdekReq1.inFormat
                           , gdekReq1.outFormat, gdekReq1.keyType
                           , gdekReq1.keyKindA + gdekReq1.keyKindH, "", "", ""
                           , gdekReq1.msgLen, gdekReq1.msgData);
                 break;
//  sP_Key, sP_KeyDescriptor, sP_KeySerialNumber,         sP_Iv, 

      default: showLogMessage("I", "", " HsmUtil =[" + type + " != 'CW','M0' " + "]");
      }

      showLogMessage("I", "", "  888 HsmUtil R=[" + sLResult + "]");
      if ("00".equals(sLResult.substring(0, 2))) {
          showLogMessage("I", "", "  成功，Result== " + sLResult.substring(2, sLResult.length()) + "]");
      } else {
          showLogMessage("I", "", "  失敗，Result== " + sLResult + "]");
   //     comcr.err_rtn("RECAL " + type + " process error!", sL_Result, comcr.h_call_batch_seqno);
      }

      return sLResult;

  } catch (Exception e) {
      showLogMessage("I", "", "  Error HsmUtil !");
      e.printStackTrace();
  }
  return sLResult;
}
/***********************************************************************/
private void getIcvv() throws Exception
{
    String realCardno = "";
    gcvvReq.msgHeader = "ICBC";
    gcvvReq.cmdCode   = "CW";
    gcvvReq.cvkPair1 = hHcetEcsCvk1;
    gcvvReq.cvkPair2 = hHcetEcsCvk2;
    if(hHcetCardNo.length() > 4 && hHcetCardNo.substring(0, 4).equals("4000")) 
      {
       realCardno = "9000";
      } 
    else
      {
       realCardno = comc.getSubString(hHcetCardNo, 0, 4);
      }
       realCardno = realCardno + comc.getSubString(hHcetCardNo, 4) ;
    gcvvReq.cardNo      = realCardno;
    gcvvReq.delimiter1  = ";";
    gcvvReq.expireDate  = comc.getSubString(hHcetValidTo, 2, 2+4);
    gcvvReq.serviceCode = "999";
    gcvvReq.delimiter =   (char) 25 +"";
    gcvvReq.msgEnder    = "ICBC";
    if(DEBUG==1) 
    	showLogMessage("I", "", "real_cardno=["+realCardno+"] expire_date=["+gcvvReq.expireDate+"] delimiter=["+gcvvReq.delimiter+"]");
    
    String tmp = callRacal("CW");
    
    gcvvRep.splitCVVOutBuff(tmp);
    
    if(!gcvvRep.errorCode.equals("00")) {
       comcr.errRtn("RECAL CW cvv2 2           error", "", hCallBatchSeqno);
      }

    tmpstr = String.format("%3.3s", gcvvRep.cvv);
    hTempIcvv = tmpstr;
}
/***********************************************************************/
private void getDEK(String keyKindH , String keyVal) throws Exception 
{
  getKEK(keyKindH, keyVal);
}
/***********************************************************************/
private void getKEK(String keyKindH , String keyVal) throws Exception 
{

    gdekReq1.msgHeader = "ICBC";
    gdekReq1.cmdCode   = "M0";
    gdekReq1.modeFlag  = "00";
    gdekReq1.inFormat  = "1";
    gdekReq1.outFormat = "1";
    gdekReq1.keyType   = "00A";
    gdekReq1.keyKindA = "U";
    gdekReq1.keyKindH = keyKindH;

    tempX04 = Integer.toHexString(keyVal.length());
    while (tempX04.length() != 4)
        tempX04 = "0"+tempX04;
    /*需放key_val，16進位制0~F，故10為16進位的長度，固定4碼*/
    gdekReq1.msgLen  = tempX04.toUpperCase(); 
    gdekReq1.msgData = keyVal;
if(DEBUG==1) 
  {
   showLogMessage("I", "", " 1.M0=["+keyKindH+"] key=["+keyVal+"]");
   showLogMessage("I", "", " 2.M0=["+gdekReq1.msgData+"] x04=["+tempX04+"]"+gdekReq1.allText());
  }

    String tmp = callRacal("M0");
    gdekRep.splitDekOutBuf(tmp);
    if(!gdekRep.errorCode.equals("00")) 
      {
       comcr.errRtn("RECAL M0 process          error", "", hCallBatchSeqno);
      }

    return;
}
/***********************************************************************/
private void numToHexstr(String outStr, String inpStr) 
{
     numToHexstr(outStr, Integer.parseInt(inpStr));
}
private void numToHexstr(String outStr, int inpInt) 
{
     outStr = Integer.toHexString(inpInt);
}
/***********************************************************************/
// structure
private class DekInbuf {       /* 總長度 136    */
        String msgHeader;
        String cmdCode;    /* 固定M0 */
        String modeFlag;   /* 固定00:ECB encryption mode */
        String inFormat;   /* 固定1:Hex-Encoded Binary */
        String outFormat;  /* 固定1:Hex-Encoded Binary */
        String keyType;    /* 固定00A:ZEK(encrypted under LMK pair 30-31) */
        String keyKindA;  /*固定U:採用何種加密方式*/
        String keyKindH;  /*KEK密碼加密的KEY*/
        String msgLen;     /*加密字串長度，16進位制0~F，故10為16*/
        String msgData;    /*需加密的字串長度為16或64*/
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader  ,   4);
            rtn += comc.fixLeft(cmdCode    ,   2);
            rtn += comc.fixLeft(modeFlag   ,   2);
            rtn += comc.fixLeft(inFormat   ,   1);
            rtn += comc.fixLeft(outFormat  ,   1);
            rtn += comc.fixLeft(keyType    ,   3);
            rtn += comc.fixLeft(keyKindA  ,   1);
            rtn += comc.fixLeft(keyKindH  ,  32);
            rtn += comc.fixLeft(msgLen     ,   4);
            rtn += comc.fixLeft(msgData    ,  64);
            rtn += comc.fixLeft(filler      ,  22);
            return rtn;
        }

//        void splitDekInbuf(String str) throws UnsupportedEncodingException {
//            byte[] bytes = str.getBytes("MS950");
//            msg_header   = comc.subMS950String(bytes,  0,  4);
//            cmd_code     = comc.subMS950String(bytes,  4,  2);
//            mode_flag    = comc.subMS950String(bytes,  0,  2);
//            in_format    = comc.subMS950String(bytes,  0,  1);
//            out_format   = comc.subMS950String(bytes,  0,  1);
//            key_type     = comc.subMS950String(bytes,  0,  3);
//            key_kind_a   = comc.subMS950String(bytes,  0,  1);
//            key_kind_h   = comc.subMS950String(bytes,  0, 32);
//            msg_len      = comc.subMS950String(bytes,  0,  4);
//            msg_data     = comc.subMS950String(bytes,  0, 64);
//            filler       = comc.subMS950String(bytes,  0, 22);
//            
//        }
    }
/***********************************************************************/
private class DekOutbuf { /* 總長度 113    */
       String msgHeader;
       String respCode;
       String errorCode;
       String msgLen;
       String msgData;
       String filler;
       
       String allText() throws UnsupportedEncodingException {
           String rtn = "";
           rtn += comc.fixLeft( msgHeader  , 4);
           rtn += comc.fixLeft( respCode   , 2);
           rtn += comc.fixLeft( errorCode  , 2);
           rtn += comc.fixLeft( msgLen     , 4);
           rtn += comc.fixLeft( msgData    ,64);
           rtn += comc.fixLeft( filler      ,37);
           return rtn;
       }
       
       void splitDekOutBuf(String str) throws UnsupportedEncodingException {
           byte[] bytes = str.getBytes("MS950");
           gdekRep.errorCode = comc.subMS950String(bytes,  0,  2);
           gdekRep.msgLen    = comc.subMS950String(bytes,  2,  4);
           gdekRep.msgData   = comc.subMS950String(bytes,  6, 64);
         //  gdek_rep.msg_header = comc.subMS950String(bytes,  0,  4);
         //  gdek_rep.resp_code  = comc.subMS950String(bytes,  4,  2);
         //  gdek_rep.error_code = comc.subMS950String(bytes,  6,  2);
         //  gdek_rep.msg_len    = comc.subMS950String(bytes,  8,  4);
         //  gdek_rep.msg_data   = comc.subMS950String(bytes, 12, 64);
         //  gdek_rep.filler     = comc.subMS950String(bytes, 76, 37);
          }
    }            
/***********************************************************************/
private class CvvInbuf {
        String msgHeader;
        String cmdCode;  /* CW */
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
            rtn += comc.fixLeft( msgHeader     , 4);
            rtn += comc.fixLeft( cmdCode       , 2);
            rtn += comc.fixLeft( cvkPair1     ,16);
            rtn += comc.fixLeft( cvkPair2     ,16);
            rtn += comc.fixLeft( cardNo        ,16);
            rtn += comc.fixLeft( delimiter1    , 1);
            rtn += comc.fixLeft( expireDate    , 4);
            rtn += comc.fixLeft( serviceCode   , 3);
            rtn += comc.fixLeft( delimiter      , 1);
            rtn += comc.fixLeft( msgEnder      , 4);
            rtn += comc.fixLeft( filler         ,37);
            return rtn;
       }
        
//        void splitDekInbuf(String str) throws UnsupportedEncodingException {
//            byte[] bytes = str.getBytes("MS950");
//            msg_header   = comc.subMS950String(bytes,  0,   4);
//            cmd_code     = comc.subMS950String(bytes,  0,   2);
//            cvk_pair_1   = comc.subMS950String(bytes,  0,  16);
//            cvk_pair_2   = comc.subMS950String(bytes,  0,  16);
//            card_no      = comc.subMS950String(bytes,  0,  16);
//            delimiter_1  = comc.subMS950String(bytes,  0,   1);
//            expire_date  = comc.subMS950String(bytes,  0,   4);
//            service_code = comc.subMS950String(bytes,  0,   3);
//            delimiter    = comc.subMS950String(bytes,  0,   1);
//            msg_ender    = comc.subMS950String(bytes,  0,   4);
//            filler       = comc.subMS950String(bytes,  0,  37);
//        }
    }
/***********************************************************************/
private class CvvOutbuf {
        String msgHeader;
        String respCode;
        String errorCode;
        String cvv;
        String delimiter;
        String filler;
        
        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(msgHeader , 4);
            rtn += comc.fixLeft(respCode  , 2);
            rtn += comc.fixLeft(errorCode , 2);
            rtn += comc.fixLeft(cvv        , 3);
            rtn += comc.fixLeft(delimiter  , 1);
            rtn += comc.fixLeft(filler     ,62);
            return rtn;
        }
        
        void splitCVVOutBuff(String str) throws UnsupportedEncodingException {
            byte[] bytes = str.getBytes("MS950");
            gcvvRep.errorCode = comc.subMS950String(bytes,  0,  2);
            gcvvRep.cvv        = comc.subMS950String(bytes,  2,  3);
          //  gcvv_rep.msg_header = comc.subMS950String(bytes,  0,  4);
          //  gcvv_rep.resp_code  = comc.subMS950String(bytes,  4,  2);
          //  gcvv_rep.error_code = comc.subMS950String(bytes,  6,  2);
          //  gcvv_rep.cvv        = comc.subMS950String(bytes,  8,  3);
          //  gcvv_rep.delimiter  = comc.subMS950String(bytes, 11,  1);
          //  gcvv_rep.filler     = comc.subMS950String(bytes, 12, 62);
           }
    }
/***********************************************************************/
private class HceIssueH {
        String paymentScheme;
        String fileVersion;
        String fileName;
        String totRec;
        String diversifyFlag;
        String prodFlag;
        String ivcvc3Flag;
        String effcFlag;
        String fiscFlag1;
        String fiscFlag2;
        String fiscFlag3;
        String fiscFlag4;     /*  61   */
        String filler1;        /* 498   +  2(ODOA)   */
        String len;
        
        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(paymentScheme ,  4);
            rtn += comc.fixLeft(fileVersion   ,  4);
            rtn += comc.fixLeft(fileName      , 40);
            rtn += comc.fixLeft(totRec        ,  5);
            rtn += comc.fixLeft(diversifyFlag ,  1);
            rtn += comc.fixLeft(prodFlag      ,  1);
            rtn += comc.fixLeft(ivcvc3Flag    ,  1);
            rtn += comc.fixLeft(effcFlag      ,  1);
            rtn += comc.fixLeft(fiscFlag1    ,  1);
            rtn += comc.fixLeft(fiscFlag2    ,  1);
            rtn += comc.fixLeft(fiscFlag3    ,  1);
            rtn += comc.fixLeft(fiscFlag4    ,  1);   /*  61   */
            rtn += comc.fixLeft(filler1       ,437);   /* 498   +  2(ODOA)   */
            rtn += comc.fixLeft(len            ,  2);
            return rtn;
        }
       
//        void splitDekInbuf(String str) throws UnsupportedEncodingException {
//            byte[] bytes = str.getBytes("MS950");
//            payment_scheme = comc.subMS950String(bytes,   0,   4);
//            file_version   = comc.subMS950String(bytes,   4,   4);
//            file_name      = comc.subMS950String(bytes,   8,  40);
//            tot_rec        = comc.subMS950String(bytes,  48,   5);
//            diversify_flag = comc.subMS950String(bytes,  53,   1);
//            prod_flag      = comc.subMS950String(bytes,  54,   1);
//            ivcvc3_flag    = comc.subMS950String(bytes,  55,   1);
//            effc_flag      = comc.subMS950String(bytes,  56,   1);
//            fisc_flag_1    = comc.subMS950String(bytes,  57,   1);
//            fisc_flag_2    = comc.subMS950String(bytes,  58,   1);
//            fisc_flag_3    = comc.subMS950String(bytes,  59,   1);
//            fisc_flag_4    = comc.subMS950String(bytes,  60,   1);   /*  61   */
//            filler_1       = comc.subMS950String(bytes,  61, 437);   /* 498   +  2(ODOA) */
//            len            = comc.subMS950String(bytes, 498,   2);
//        }
    }
/***********************************************************************/
private class HceIssueD {
        String seId;
        String serviceId;
        String serviceVer;
        String msisdn;
        String seType;
        String mnoId;
        String activationCode;
        String binNo;
        String track2Dek;
        String psn;
        String effcDate;
        String ivcvc3T1;
        String ivcvc3T2;
        String pinIvcvc3T1;
        String pinIvcvc3T2;
        String pinMobile;         /*   199    */
        String tpan;
        String payemntAcctRef;
        String tpanExpDate;
        String memberId;
        String barcodeType;
        String filler1;           /*   498    */
        String len;
        
        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(seId            , 32);
            rtn += comc.fixLeft(serviceId       , 10);
            rtn += comc.fixLeft(serviceVer      ,  8);
            rtn += comc.fixLeft(msisdn           , 15);
            rtn += comc.fixLeft(seType          ,  2);
            rtn += comc.fixLeft(mnoId           ,  6);
            rtn += comc.fixLeft(activationCode  , 16);
            rtn += comc.fixLeft(binNo           ,  6);
            rtn += comc.fixLeft(track2Dek       , 64);
            rtn += comc.fixLeft(psn              ,  2);
            rtn += comc.fixLeft(effcDate        ,  6);
            rtn += comc.fixLeft(ivcvc3T1        ,  4);
            rtn += comc.fixLeft(ivcvc3T2        ,  4);
            rtn += comc.fixLeft(pinIvcvc3T1    ,  4);
            rtn += comc.fixLeft(pinIvcvc3T2    ,  4);
            rtn += comc.fixLeft(pinMobile       , 16); /*   199    */
            rtn += comc.fixLeft(tpan             , 32);
            rtn += comc.fixLeft(payemntAcctRef , 24);
            rtn += comc.fixLeft(tpanExpDate    ,  4);
            rtn += comc.fixLeft(memberId        , 30);
            rtn += comc.fixLeft(barcodeType     ,  2);
            rtn += comc.fixLeft(filler1         ,207); /*   498    */
            rtn += comc.fixLeft(len              ,  2);
            return rtn;
        }
        
//        void splitDekInbuf(String str) throws UnsupportedEncodingException {
//            byte[] bytes = str.getBytes("MS950");
//            se_id            = comc.subMS950String(bytes,   0,  32);
//            service_id       = comc.subMS950String(bytes,  32,  10);
//            service_ver      = comc.subMS950String(bytes,  42,   8);
//            msisdn           = comc.subMS950String(bytes,  50,  15);
//            se_type          = comc.subMS950String(bytes,  65,   2);
//            mno_id           = comc.subMS950String(bytes,  67,   6);
//            activation_code  = comc.subMS950String(bytes,  73,  16);
//            bin_no           = comc.subMS950String(bytes,  89,   6);
//            track2_dek       = comc.subMS950String(bytes,  95,  64);
//            psn              = comc.subMS950String(bytes, 159,   2);
//            effc_date        = comc.subMS950String(bytes, 161,   6);
//            ivcvc3_t1        = comc.subMS950String(bytes, 167,   4);
//            ivcvc3_t2        = comc.subMS950String(bytes, 171,   4);
//            pin_ivcvc3_t1    = comc.subMS950String(bytes, 175,   4);
//            pin_ivcvc3_t2    = comc.subMS950String(bytes, 179,   4);
//            pin_mobile       = comc.subMS950String(bytes, 183,  16);         /*   199    */
//            tpan             = comc.subMS950String(bytes, 199,  32);
//            payemnt_acct_ref = comc.subMS950String(bytes, 231,  24);
//            tpan_exp_date    = comc.subMS950String(bytes, 255,   4);
//            member_id        = comc.subMS950String(bytes, 259,  30);
//            barcode_type     = comc.subMS950String(bytes, 289,   2);
//            filler_1         = comc.subMS950String(bytes, 291, 207);          /*   498  */
//            len              = comc.subMS950String(bytes, 498,   2);
//        }
    }
//*****************************************************************************
}

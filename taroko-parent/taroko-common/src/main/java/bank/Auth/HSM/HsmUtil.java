/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*  109-07-22  V1.00.02  Zuwei       updated for project coding standard      *
*  111-01-19  V1.00.03  Justin      fix J2EE Bad Practices: Leftover Debug Code
******************************************************************************/
package bank.Auth.HSM;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.security.SecureRandom;

import bank.Auth.HpeUtil;

public class HsmUtil {
    String sGResponseCode = "";
    String sGReturnCode = "";
    String sGReturnMsg = "";
    String sGHsmServerIp = "";
    int nGHsmServerPort = 0;

    public HsmUtil(String sPHsmServerIp, int nPHsmServerPort) {
        this.sGHsmServerIp = sPHsmServerIp;
        this.nGHsmServerPort = nPHsmServerPort;
    }

    public String hsmCommandM4(String sPSourceModeFlag, String sPDestModeFlag,
            String sPInputFormatFlag, String sPOutputFormatFlag, String sPSourceKeyType,
            String sPSourceKey, String sPSourceKsnDesc, String sPSourceKeySerialNumber,
            String sPDestKeyType, String sPDestKey, String sPDestKsnDesc,
            String sPDestKeySerialNumber, String sPSourceIv, String sPDestIv, String sPMesgLength,
            String sPEncryptedMesg) throws Exception {
        String sLResult = "";

        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();
        sLHsmCommand = sLMsgHeader;

        String sLCommandCode = "M4";
        sLHsmCommand = sLHsmCommand + sLCommandCode;

        sLHsmCommand = sLHsmCommand + sPSourceModeFlag;
        sLHsmCommand = sLHsmCommand + sPDestModeFlag;
        sLHsmCommand = sLHsmCommand + sPInputFormatFlag;
        sLHsmCommand = sLHsmCommand + sPOutputFormatFlag;
        sLHsmCommand = sLHsmCommand + sPSourceKeyType;
        sLHsmCommand = sLHsmCommand + sPSourceKey;
        sLHsmCommand = sLHsmCommand + sPSourceKsnDesc;
        sLHsmCommand = sLHsmCommand + sPSourceKeySerialNumber;
        sLHsmCommand = sLHsmCommand + sPDestKeyType;

        sLHsmCommand = sLHsmCommand + sPDestKey;
        sLHsmCommand = sLHsmCommand + sPDestKsnDesc;
        sLHsmCommand = sLHsmCommand + sPDestKeySerialNumber;
        sLHsmCommand = sLHsmCommand + sPSourceIv;
        sLHsmCommand = sLHsmCommand + sPDestIv;

        sLHsmCommand = sLHsmCommand + sPMesgLength;
        sLHsmCommand = sLHsmCommand + sPEncryptedMesg;

        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("M5".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public byte[] hsmCommandLQReturnByteAry(String sPHashIdentifier, String sPHmacLen,
            String sPHmacKeyFormat, String sPHmacKeyLen, String sPHmacKey, String sPDelimiter,
            String sPMesgLen, String sPMesgData) throws Exception {
        byte[] lResult = null;


        ByteArrayOutputStream lByteAryOutputStream = new ByteArrayOutputStream();



        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();
        sLHsmCommand = sLMsgHeader;
        lByteAryOutputStream.write(sLMsgHeader.getBytes());



        String sLCommandCode = "LQ";
        sLHsmCommand = sLHsmCommand + sLCommandCode;
        lByteAryOutputStream.write(sLCommandCode.getBytes());

        sLHsmCommand = sLHsmCommand + sPHashIdentifier;
        lByteAryOutputStream.write(sPHashIdentifier.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacLen;
        lByteAryOutputStream.write(sPHmacLen.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacKeyFormat;
        lByteAryOutputStream.write(sPHmacKeyFormat.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacKeyLen;
        lByteAryOutputStream.write(sPHmacKeyLen.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacKey;
        byte[] lHmacKeyByteAry = HpeUtil.transHexString2ByteAry(sPHmacKey);
        lByteAryOutputStream.write(lHmacKeyByteAry);


        sLHsmCommand = sLHsmCommand + sPDelimiter;
        lByteAryOutputStream.write(sPDelimiter.getBytes());

        sLHsmCommand = sLHsmCommand + sPMesgLen;
        lByteAryOutputStream.write(sPMesgLen.getBytes());

        sLHsmCommand = sLHsmCommand + sPMesgData;
        lByteAryOutputStream.write(sPMesgData.getBytes());


        byte[] lCmdByteAry = HpeUtil.addLength2HeadOfByteAry(lByteAryOutputStream.toByteArray());
        byte[] lHsmResponseByteAry = executeHsmCmd(lCmdByteAry);

        String sLHsmResponse = new String(lHsmResponseByteAry);



        String sLResponseCode = "";
        String sLHmac = "";
        String sLHmacLen = "";
        int nLHmacBeginPos = 0;
        int nLHmacEndPos = 0;
        if (sLHsmResponse.length() > 6) {
            sLResponseCode = sLHsmResponse.substring(4, 6);
            sLHmacLen = sLHsmResponse.substring(6, 10);
            nLHmacBeginPos = 10;
        }
        if (sLHsmResponse.length() >= 8) {
            sLResponseCode = sLHsmResponse.substring(6, 8);
            sLHmacLen = sLHsmResponse.substring(8, 12);
            nLHmacBeginPos = 12;
        }
        int nLHmacDataLen = Integer.parseInt(sLHmacLen);
        nLHmacEndPos = nLHmacBeginPos + nLHmacDataLen;
        byte[] lHmacData = new byte[nLHmacDataLen];

        System.arraycopy(lHsmResponseByteAry, nLHmacBeginPos, lHmacData, 0, nLHmacDataLen);



        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        outputStream.write(sLResponseCode.getBytes());
        if ("00".equals(sLResponseCode)) {
            outputStream.write(sLHmacLen.getBytes());
            outputStream.write(lHmacData);
        }
        lResult = outputStream.toByteArray();

        return lResult;
    }

    public String hsmCommandLQ(String sPHashIdentifier, String sPHmacLen, String sPHmacKeyFormat,
            String sPHmacKeyLen, String sPHmacKey, String sPDelimiter, String sPMesgLen,
            String sPMesgData) throws Exception {
        String sLResult = "";


        ByteArrayOutputStream lByteAryOutputStream = new ByteArrayOutputStream();



        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();
        sLHsmCommand = sLMsgHeader;
        lByteAryOutputStream.write(sLMsgHeader.getBytes());



        String sLCommandCode = "LQ";
        sLHsmCommand = sLHsmCommand + sLCommandCode;
        lByteAryOutputStream.write(sLCommandCode.getBytes());

        sLHsmCommand = sLHsmCommand + sPHashIdentifier;
        lByteAryOutputStream.write(sPHashIdentifier.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacLen;
        lByteAryOutputStream.write(sPHmacLen.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacKeyFormat;
        lByteAryOutputStream.write(sPHmacKeyFormat.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacKeyLen;
        lByteAryOutputStream.write(sPHmacKeyLen.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacKey;
        byte[] lHmacKeyByteAry = HpeUtil.transHexString2ByteAry(sPHmacKey);
        lByteAryOutputStream.write(lHmacKeyByteAry);


        sLHsmCommand = sLHsmCommand + sPDelimiter;
        lByteAryOutputStream.write(sPDelimiter.getBytes());

        sLHsmCommand = sLHsmCommand + sPMesgLen;
        lByteAryOutputStream.write(sPMesgLen.getBytes());

        sLHsmCommand = sLHsmCommand + sPMesgData;
        lByteAryOutputStream.write(sPMesgData.getBytes());


        byte[] lCmdByteAry = HpeUtil.addLength2HeadOfByteAry(lByteAryOutputStream.toByteArray());
        byte[] lHsmResponseByteAry = executeHsmCmd(lCmdByteAry);

        String sLHsmResponse = new String(lHsmResponseByteAry);



        String sLResponseCode = "";
        String sLHmac = "";
        String sLHmacLen = "";
        int nLHmacBeginPos = 0;
        int nLHmacEndPos = 0;
        if (sLHsmResponse.length() > 6) {
            sLResponseCode = sLHsmResponse.substring(4, 6);
            sLHmacLen = sLHsmResponse.substring(6, 10);
            nLHmacBeginPos = 10;
        }
        if (sLHsmResponse.length() >= 8) {
            sLResponseCode = sLHsmResponse.substring(6, 8);
            sLHmacLen = sLHsmResponse.substring(8, 12);
            nLHmacBeginPos = 12;
        }
        int nLHmacDataLen = Integer.parseInt(sLHmacLen);
        nLHmacEndPos = nLHmacBeginPos + nLHmacDataLen;
        byte[] lHmacData = new byte[nLHmacDataLen];

        System.arraycopy(lHsmResponseByteAry, nLHmacBeginPos, lHmacData, 0, nLHmacDataLen);



        sLResult = sLResponseCode;
        if ("00".equals(sLResponseCode)) {
            sLResult = sLResult + sLHmacLen;
            String sLHmacHex = HpeUtil.getByteHex(lHmacData);
            sLResult = sLResult + sLHmacHex;
        }
        return sLResult;
    }

    public String hsmCommandLS(String sPHashIdentifier, String sPHmacLen, String sPHmac,
            String sPHmacKeyFormat, String sPHmacKeyLen, String sPHmacKey, String sPDelimiter,
            String sPDataLen, String sPMesgData) throws Exception {
        String sLResult = "";
        byte[] lHmacByteAry = HpeUtil.transHexString2ByteAry(sPHmac);

        ByteArrayOutputStream lByteAryOutputStream = new ByteArrayOutputStream();



        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();
        sLHsmCommand = sLMsgHeader;
        lByteAryOutputStream.write(sLMsgHeader.getBytes());



        String sLCommandCode = "LS";
        sLHsmCommand = sLHsmCommand + sLCommandCode;
        lByteAryOutputStream.write(sLCommandCode.getBytes());

        sLHsmCommand = sLHsmCommand + sPHashIdentifier;
        lByteAryOutputStream.write(sPHashIdentifier.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacLen;
        lByteAryOutputStream.write(sPHmacLen.getBytes());

        lByteAryOutputStream.write(lHmacByteAry);

        sLHsmCommand = sLHsmCommand + sPHmacKeyFormat;
        lByteAryOutputStream.write(sPHmacKeyFormat.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacKeyLen;
        lByteAryOutputStream.write(sPHmacKeyLen.getBytes());

        sLHsmCommand = sLHsmCommand + sPHmacKey;
        byte[] lHmacKeyByteAry = HpeUtil.transHexString2ByteAry(sPHmacKey);
        lByteAryOutputStream.write(lHmacKeyByteAry);


        sLHsmCommand = sLHsmCommand + sPDelimiter;
        lByteAryOutputStream.write(sPDelimiter.getBytes());

        sLHsmCommand = sLHsmCommand + sPDataLen;
        lByteAryOutputStream.write(sPDataLen.getBytes());

        sLHsmCommand = sLHsmCommand + sPMesgData;
        lByteAryOutputStream.write(sPMesgData.getBytes());


        byte[] lCmdByteAry = HpeUtil.addLength2HeadOfByteAry(lByteAryOutputStream.toByteArray());
        byte[] lHsmResponseByteAry = executeHsmCmd(lCmdByteAry);

        String sLHsmResponse = new String(lHsmResponseByteAry);
        System.out.println("###" + sLHsmResponse + "###" + lHsmResponseByteAry.length + "--");

        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("LT".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public String hsmCommandCW(String sPCardNo, String sPExpireDate, String sPServiceCode,
            String sPCsseccfgCvka, String sPCsseccfgCvkb) throws Exception {
        String sLResultCVV = "";
        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();
        sLHsmCommand = sLMsgHeader;

        String sLCommandCode = "CW";
        sLHsmCommand = sLHsmCommand + sLCommandCode;



        sLHsmCommand = sLHsmCommand + sPCsseccfgCvka + sPCsseccfgCvkb;

        sLHsmCommand = sLHsmCommand + sPCardNo;

        sLHsmCommand = sLHsmCommand + ";";
        sLHsmCommand = sLHsmCommand + sPExpireDate + sPServiceCode;

        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("CX".equals(this.sGResponseCode))) {
            sLResultCVV = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResultCVV = this.sGReturnCode;
        }
        return sLResultCVV;
    }

    public String hsmCommandJE(String sPZpk, String sPSourcePinBlock, String sPAccountNumber,
            String sPSourcePinBlockFormatCode) throws Exception {
        String sLResult = "";
        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();
        sLHsmCommand = sLMsgHeader;

        String sLCommandCode = "JE";
        sLHsmCommand = sLHsmCommand + sLCommandCode;



        sLHsmCommand = sLHsmCommand + sPZpk;

        sLHsmCommand = sLHsmCommand + sPSourcePinBlock;


        sLHsmCommand = sLHsmCommand + sPSourcePinBlockFormatCode;


        sLHsmCommand = sLHsmCommand + sPAccountNumber;

        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("JF".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public String hsmCommandM0(String sPModeFlag, String sPInputFormat, String sPOutputFormat,
            String sPKeyType, String sPKey, String sPKeyDescriptor, String sPKeySerialNumber,
            String sPIv, String sPMsgLength, String sPMsgToBeEncrypted) throws Exception {
        String sLResult = "";
        String sLHsmCommand = "";

        String sLKeyType = "FFF";
        if (!"".equals(sPKeyType)) {
            sLKeyType = sPKeyType;
        }
        String sLMsgHeader = getMsgHeader();


        String sLCommandCode = "M0";
        sLHsmCommand = sLMsgHeader + sLCommandCode + sPModeFlag + sPInputFormat
                + sPOutputFormat + sLKeyType + sPKey + sPKeyDescriptor + sPKeySerialNumber
                + sPIv + sPMsgLength + sPMsgToBeEncrypted;


        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("M1".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public String hsmCommandM2(String sPModeFlag, String sPInputFormat, String sPOutputFormat,
            String sPKey, String sPKsnDescriptor, String sPKeySerialNumber, String sPIV,
            String sPMsgLength, String sPMsgToBeDecrypted, String sPKeyType) throws Exception {
        String sLResult = "";
        String sLHsmCommand = "";

        String sLKeyType = "609";
        if (sPKeyType.length() > 0) {
            sLKeyType = sPKeyType;
        }
        String sLMsgHeader = getMsgHeader();


        String sLCommandCode = "M2";
        sLHsmCommand = sLMsgHeader + sLCommandCode + sPModeFlag + sPInputFormat
                + sPOutputFormat + sLKeyType + sPKey + sPKsnDescriptor + sPKeySerialNumber
                + sPIV + sPMsgLength + sPMsgToBeDecrypted;


        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("M3".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public String hsmCommandDG(String sPPvk, String sPPin, String sPAccountNumber,
            String sPPvki) throws Exception {
        String sLResult = "";
        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();


        String sLCommandCode = "DG";
        sLHsmCommand =
                sLMsgHeader + sLCommandCode + sPPvk + sPPin + sPAccountNumber + sPPvki;


        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("DH".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public String hsmCommandRY(String sPMode, String sPFlag, String sPCsck,
            String sPAccountNumber, String sPExpireDate, String sPServiceCode, String sP_Zmk,
            String sP5DigitCsc, String sP4DigitCsc, String sP3DigitCsc) throws Exception {
        String sLResult = "";
        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();


        String sLCommandCode = "RY";
        if ("0".equals(sPMode)) {
            sLHsmCommand = sLMsgHeader + sLCommandCode;
        } else if ("1".equals(sPMode)) {
            sLHsmCommand = sLMsgHeader + sLCommandCode + sPMode + sPFlag + sPCsck
                    + sPAccountNumber + sPExpireDate + sPServiceCode;
        } else if ("2".equals(sPMode)) {
            sLHsmCommand = sLMsgHeader + sLCommandCode + sPMode + sPFlag + sPCsck
                    + sPAccountNumber + sPExpireDate + sPServiceCode;
        } else if ("3".equals(sPMode)) {
            sLHsmCommand = sLMsgHeader + sLCommandCode + sPMode + sPFlag + sPCsck
                    + sPAccountNumber + sPExpireDate + sPServiceCode;
        } else if ("4".equals(sPMode)) {
            sLHsmCommand = sLMsgHeader + sLCommandCode + sPMode + sPFlag + sPCsck
                    + sPAccountNumber + sPExpireDate + sPServiceCode + sP5DigitCsc
                    + sP4DigitCsc + sP3DigitCsc;
        }
        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("RZ".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public String hsmCommandJA(String sPAccountNumber, String sPPinLength) throws Exception {
        String sLResult = "";
        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();


        String sLCommandCode = "JA";

        sLHsmCommand = sLMsgHeader + sLCommandCode + sPAccountNumber + sPPinLength;

        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("JB".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public String hsmCommandFA(String sPZmk, String sPZpk) throws Exception {
        String sLResult = "";
        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();


        String sLCommandCode = "FA";

        sLHsmCommand = sLMsgHeader + sLCommandCode + sPZmk + sPZpk;



        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("FB".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public String hsmCommandEC(String sPZpk, String sPPvk, String sPPinBlock,
            String sPPinBlockFormatCode, String sPPanOrToken, String sPPvki, String sPPVV)
            throws Exception {
        String sLResult = "";
        String sLHsmCommand = "";


        String sLMsgHeader = getMsgHeader();


        String sLCommandCode = "EC";

        sLHsmCommand = sLMsgHeader + sLCommandCode + sPZpk + sPPvk + sPPinBlock
                + sPPinBlockFormatCode + sPPanOrToken + sPPvki + sPPVV;

        String sLHsmResponse = executeHsmCmd(sLHsmCommand);
        getHsmExecuteResult(sLHsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("ED".equals(this.sGResponseCode))) {
            sLResult = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sLResult = this.sGReturnCode;
        }
        return sLResult;
    }

    public String hsmCommandCC(String sP_SrcZpk, String sP_DestZpk, String sP_MaxPinLength,
            String sP_SrcPinBlock, String sP_SrcPinBlockFormatCode,
            String sP_DestPinBlockFormatCode, String sP_PanOrToken) throws Exception {
        String sL_Result = "";
        String sL_HsmCommand = "";


        String sL_MsgHeader = getMsgHeader();


        String sL_CommandCode = "CC";

        sL_HsmCommand = sL_MsgHeader + sL_CommandCode + sP_SrcZpk + sP_DestZpk + sP_MaxPinLength
                + sP_SrcPinBlock + sP_SrcPinBlockFormatCode + sP_DestPinBlockFormatCode
                + sP_PanOrToken;

        String sL_HsmResponse = executeHsmCmd(sL_HsmCommand);
        getHsmExecuteResult(sL_HsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("CD".equals(this.sGResponseCode))) {
            sL_Result = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sL_Result = this.sGReturnCode;
        }
        return sL_Result;
    }

    private int getRandomNumber(int nLMaxNum) {
        int nLResult = 0;

        SecureRandom lRandomGen = new SecureRandom();

        nLResult = lRandomGen.nextInt(nLMaxNum);
        lRandomGen = null;

        return nLResult;
    }

    private String fillZeroOnLeft(double dPSrc, int nPTargetLen) {
        int nLSrc = (int) dPSrc;


        return String.format("%0" + nPTargetLen + "d", new Object[] {Integer.valueOf(nLSrc)});
    }

    private String getMsgHeader() {
        String sLTmpNo = Integer.toString(getRandomNumber(999) + getRandomNumber(999));

        String sLMsgHeader = fillZeroOnLeft(Double.parseDouble(sLTmpNo), 6);

        sLMsgHeader = sLMsgHeader.substring(2, 6);



        return sLMsgHeader;
    }

    public String hsmCommandCY(String sP_CardNo, String sP_ExpireDate, String sP_ServiceCode,
            String sP_Cvv, String sP_CsseccfgCvka, String sP_CsseccfgCvkb) throws Exception {
        String sL_Result = "";
        String sL_HsmCommand = "";


        String sL_MsgHeader = getMsgHeader();
        sL_HsmCommand = sL_MsgHeader;

        String sL_CommandCode = "CY";
        sL_HsmCommand = sL_HsmCommand + sL_CommandCode;



        sL_HsmCommand = sL_HsmCommand + sP_CsseccfgCvka + sP_CsseccfgCvkb;

        sL_HsmCommand = sL_HsmCommand + sP_Cvv + sP_CardNo;

        sL_HsmCommand = sL_HsmCommand + ";";
        sL_HsmCommand = sL_HsmCommand + sP_ExpireDate + sP_ServiceCode;

        String sL_HsmResponse = executeHsmCmd(sL_HsmCommand);
        getHsmExecuteResult(sL_HsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("CZ".equals(this.sGResponseCode))) {
            sL_Result = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sL_Result = this.sGReturnCode;
        }
        return sL_Result;
    }

    public String hsmCommandBE(String sP_Zpk, String sP_PinBlock, String sP_PinBlockFormatCode,
            String sP_AccountNumber, String sP_Pin) throws Exception {
        String sL_Result = "";
        String sL_HsmCommand = "";


        String sL_MsgHeader = getMsgHeader();
        sL_HsmCommand = sL_MsgHeader;

        String sL_CommandCode = "BE";
        sL_HsmCommand = sL_HsmCommand + sL_CommandCode;



        sL_HsmCommand = sL_HsmCommand + sP_Zpk + sP_PinBlock + sP_PinBlockFormatCode
                + sP_AccountNumber + sP_Pin;

        String sL_HsmResponse = executeHsmCmd(sL_HsmCommand);
        getHsmExecuteResult(sL_HsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("BF".equals(this.sGResponseCode))) {
            sL_Result = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sL_Result = this.sGReturnCode;
        }
        return sL_Result;
    }

    public String hsmCommandPA(String sP_Data) throws Exception {
        String sL_Result = "";
        String sL_HsmCommand = "";


        String sL_MsgHeader = getMsgHeader();
        sL_HsmCommand = sL_MsgHeader;

        String sL_CommandCode = "PA";
        sL_HsmCommand = sL_HsmCommand + sL_CommandCode;



        sL_HsmCommand = sL_HsmCommand + sP_Data;


        String sL_HsmResponse = executeHsmCmd(sL_HsmCommand);
        getHsmExecuteResult(sL_HsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("PB".equals(this.sGResponseCode))) {
            sL_Result = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sL_Result = this.sGReturnCode;
        }
        return sL_Result;
    }

    public String hsmCommandPI(String sP_DocType, String sP_AccountNumber, String sP_Pin,
            String sP_AllPrintField) throws Exception {
        String sL_Result = "";
        String sL_HsmCommand = "";


        String sL_MsgHeader = getMsgHeader();

        sL_HsmCommand = sL_MsgHeader;

        String sL_CommandCode = "PI";
        sL_HsmCommand = sL_HsmCommand + sL_CommandCode;



        sL_HsmCommand = sL_HsmCommand + sP_DocType + sP_AccountNumber + sP_Pin + sP_AllPrintField;



        String sL_HsmResponse = executeHsmCmd(sL_HsmCommand);
        getHsmExecuteResult(sL_HsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("PJ".equals(this.sGResponseCode))) {
            sL_Result = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sL_Result = this.sGReturnCode;
        }
        return sL_Result;
    }

    public String hsmCommandNG(String sP_AccountNumber, String sP_Pin) throws Exception {
        String sL_Result = "";
        String sL_HsmCommand = "";


        String sL_MsgHeader = getMsgHeader();


        String sL_CommandCode = "NG";

        sL_HsmCommand = sL_MsgHeader + sL_CommandCode + sP_AccountNumber + sP_Pin;

        String sL_HsmResponse = executeHsmCmd(sL_HsmCommand);
        getHsmExecuteResult(sL_HsmResponse);
        if (("00".equals(this.sGReturnCode)) && ("NH".equals(this.sGResponseCode))) {
            sL_Result = this.sGReturnCode + this.sGReturnMsg;
        } else {
            sL_Result = this.sGReturnCode;
        }
        return sL_Result;
    }

    private void getHsmExecuteResult(String sP_HsmResponse) {
        if (sP_HsmResponse.length() > 6) {
            this.sGResponseCode = sP_HsmResponse.substring(4, 6);
        }
        if (sP_HsmResponse.length() >= 8) {
            this.sGReturnCode = sP_HsmResponse.substring(6, 8);
        }
        String sL_ResponseHeader = sP_HsmResponse.substring(0, 8);
        if ("00".equals(this.sGReturnCode)) {
            this.sGReturnMsg =
                    sP_HsmResponse.substring(sL_ResponseHeader.length(), sP_HsmResponse.length());
        } else {
            this.sGReturnMsg = this.sGReturnCode;
        }
    }

    private byte[] executeHsmCmd(byte[] pCommandByteAry) throws Exception {
        Socket socket = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        String response = null;
        byte[] respData = new byte[1024];
        try {
            socket = new Socket(this.sGHsmServerIp, this.nGHsmServerPort);
            if (socket != null) {
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));



                out.write(pCommandByteAry);



                out.flush();



                byte[] lenData = new byte[3];

                int nL_HeadLen = in.read(lenData, 0, 2);
                if (nL_HeadLen == 2) {
                    int packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);

                    if (packetLen > respData.length) {
                        throw new RuntimeException("packet length is too long.");
                    }
                    int i = in.read(respData, 0, packetLen);
                }
                socket.close();
            }
        } catch (Exception ex) {
            if (socket != null) {
                socket.close();
            }
            System.out.println("HSM hsmCmd Error." + ex);
            throw ex;
        }
        return respData;
    }

    private String executeHsmCmd(String command) throws Exception {
        Socket socket = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        String response = null;
        try {
            socket = new Socket(this.sGHsmServerIp, this.nGHsmServerPort);
            if (socket != null) {
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));



                out.writeUTF(command);

                System.out.println("To HSM:[" + command + "]");



                out.flush();
                response = in.readUTF();

                socket.close();
            }
        } catch (Exception ex) {
            if (socket != null) {
                socket.close();
            }
            System.out.println("HSM hsmCmd Error." + ex);
            throw ex;
        }
        return response;
    }
    
//    // J2EE Bad Practices: Leftover Debug Code
//    public static void main(String[] args) {
//        try {
//            HsmUtil lHsmUtil = new HsmUtil("10.2.109.121", 1500);
//
//
//            String sLHashIdentifier = "08";
//            String sLHmacLen = "0032";
//            String sLHmacKeyFormat = "00";
//            String sLHmacKeyLen = "0040";
//            String sLHmacKey =
//                    "C1C51B1535778FF511929BDFC5EBC82018C142DFB48FDE1E506AF45831D9F9CC0E8BA5D8FADF60EB";
//            String sLDelimiter = ";";
//            String sLMesgLen = "00020";
//            String sLMesgData = "0010CAFC6D3323644EB3";
//
//
//            byte[] arrayOfByte = lHsmUtil.hsmCommandLQReturnByteAry(sLHashIdentifier, sLHmacLen,
//                    sLHmacKeyFormat, sLHmacKeyLen, sLHmacKey, sLDelimiter, sLMesgLen, sLMesgData);
//        } catch (Exception localException) {
//        }
//    }
}

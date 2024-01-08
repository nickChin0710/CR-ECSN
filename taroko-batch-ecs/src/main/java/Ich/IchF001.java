/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/12/25  V1.01.00  Lai         program initial                           *
*  109/11/29  V1.00.02  yanghan       修改了變量名稱和方法名稱                                                                              *
******************************************************************************/

package Ich;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommCpi;

public class IchF001 extends AccessDAO {
    private String progname = "卡款檔(B01B)產生  109/11/29 V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
	CommCpi        comcpi = new CommCpi();

	List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
	String rptId   = "";
    String rptName = "";
    int rptSeq = 0;
    
    int    debug = 1;
    String hTempUser = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hIcdrMediaCreateDate = "";
    String hIcdrMediaCreateTime = "";
    String hIcdrBankId     = "";
    String hIcdrGroupId    = "";
    String hIcdrCardCode   = "";
    String hIcdrCardName   = "";
    String hIcdrBarCode    = "";
    String hIcdrBinType    = "";
    String hIcdrVendorIch  = "";
    String hIcdrIchKind    = "";
    String hIcdrUnionFlag  = "";
    String hIcdrUniformNo  = "";
    int    hIcdrSeqNoCurr = 0;
    String hIcdrRowid = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";
    String tmpstr1 = "";
    String fileSeq = "";
    String hTfinFileIden = "B01B";

    int forceFlag = 0;
    int totCnt    = 0;
    int hTnlgRecordCnt = 0;
    String hHash   = "";
    String allData = "";

    buf1 detailSt = new buf1();
    
    String out = "";

//************************************************************************************
public int mainProcess(String[] args)
{
 try
  {
   // ====================================
   // 固定要做的
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I", "", javaProgram + " " + progname);
   // =====================================
   if (args.length != 0 && args.length != 1 && args.length != 2) {
       comc.errExit("Usage : IchF001 [notify_date] [force_flag (Y/N)]", "");
   }

   // 固定要做的

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }

   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

   String checkHome = comc.getECSHOME();
   if (comcr.hCallBatchSeqno.length() > 6) {
       if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
           comcr.hCallBatchSeqno = "no-call";
       }
   }

   comcr.hCallRProgramCode = this.getClass().getName();
   String hTempUser = "";
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

   
   hTnlgNotifyDate = "";
   forceFlag = 0;
   if (args.length == 1) {
       if ((args[0].length() == 1) && (args[0].equals("Y")))
           forceFlag = 1;
       if (args[0].length() == 8)
           hTnlgNotifyDate = args[0];
   }
   if (args.length == 2) {
       hTnlgNotifyDate = args[0];
       if (args[1].equals("Y"))
           forceFlag = 1;
   }
   selectPtrBusinday();

// file_seq = "01";
   tmpstr1 = String.format("BRQA_%3.3s_%8.8s_%4.4s",comc.ICH_BANK_ID3,hTnlgNotifyDate
                                                            ,hTfinFileIden);
   showLogMessage("I", "", "Process date=["+forceFlag+"]"+hTnlgNotifyDate+","+tmpstr1);

   hTnlgFileName = tmpstr1;

   if (forceFlag == 0) {
       if (selectIchNotifyLoga() != 0) {
           String errMsg = String.format("select_ich_notify_log_a error !");
           comcr.errRtn(errMsg, "",comcr.hCallBatchSeqno);
       }
   } else {
       updateIchB01bParmA();
   }

   fileOpen();

   selectIchB01bParm();

   if(totCnt > 0)
     {
      hHash  = comc.encryptSHA(allData, "SHA-1", "big5");
      tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%28.28s%-40.40s\r\n",hTfinFileIden
              ,"01", "0001", comc.ICH_BANK_ID3, String.format("%08d",totCnt) + "A", " ", hHash);

      lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
     }

   hTnlgRecordCnt = totCnt;

   fileClose();


   comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
   showLogMessage("I", "", comcr.hCallErrorDesc);

   if (comcr.hCallBatchSeqno.length() == 20)    comcr.callbatch(1, 0, 1); // 1: 結束
   
   finalProcess();
   return 0;
  } catch (Exception ex) 
      { expMethod = "mainProcess"; expHandle(ex); return exceptExit; 
      }
}
/***********************************************************************/
    void selectPtrBusinday() throws Exception {
        
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_icdr_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_icdr_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate     = getValue("business_date");
            hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
            hIcdrMediaCreateDate = getValue("h_icdr_media_create_date");
            hIcdrMediaCreateTime = getValue("h_icdr_media_create_time");
        }

    }
/***********************************************************************/
int selectIchNotifyLoga() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd  = "select media_create_date,";
        sqlCmd += " ftp_send_date ";
        sqlCmd += "  from ich_notify_log  ";
        sqlCmd += " where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }
        if (recordCnt > 0) {
            hTnlgMediaCreateDate = getValue("media_create_date");
            hTnlgFtpSendDate     = getValue("ftp_send_date");
        }

        if (hTnlgFtpSendDate.length() != 0) {
            String stderr = String.format("通知檔 [%s] 已FTP至ICH , 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            String stderr = String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateIchB01bParmA() throws Exception {
        daoTable  = "ich_b01b_parm";
        updateSQL = "proc_flag       = 'N'";
        whereStr  = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", hTnlgFileName
                                                              , comcr.hCallBatchSeqno);
        }

    }
/***********************************************************************/
void fileOpen() throws Exception 
{
  String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
  temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
  out = temstr1;


  /*selectSQL  = " count(*)   as all_cnt ";
  daoTable   = " ich_card_parm b, ich_b01b_parm a ";
  whereStr   = " where 1=1               ";
  whereStr  += "   and (a.proc_flag = 'N' or decode(a.proc_flag,'', 'N',a.proc_flag) = 'N') ";
  whereStr  += "   and b.card_code  = a.card_code ";
  
  int recCnt = selectTable();*/

  hHash = "0000000000000000000000000000000000000000";
  tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%28.28s%-40.40s\r\n",hTfinFileIden,"01"
          ,"0001",comc.ICH_BANK_ID3,"00000000A"," ",hHash);

  lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));
}
/***********************************************************************/
void fileClose() throws Exception {
    comc.writeReport(out, lpar, "big5", false);
}
/***********************************************************************/
void selectIchB01bParm() throws Exception 
{
        sqlCmd = "select ";
        sqlCmd += "a.bank_id,";
        sqlCmd += "a.group_id,";
        sqlCmd += "a.card_code,";
        sqlCmd += "a.bar_code,";
        sqlCmd += "b.bin_type,";
        sqlCmd += "b.card_name,";
        sqlCmd += "b.seq_no_curr,";
        sqlCmd += "b.vendor_ich ,";
        sqlCmd += "a.rowid as rowid1 ";
        sqlCmd += " from ich_card_parm b, ich_b01b_parm a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and decode(a.proc_flag,'','N',a.proc_flag) = 'N'  ";
        sqlCmd += "  and b.card_code  = a.card_code ";
        openCursor();
        while (fetchTable()) {
            hIcdrBankId     = getValue("bank_id");
            hIcdrGroupId    = getValue("group_id");
            hIcdrCardCode   = getValue("card_code");
            hIcdrCardName   = getValue("card_name");
            hIcdrBarCode    = getValue("bar_code");
            hIcdrBinType    = getValue("bin_type");
            hIcdrCardName   = getValue("card_name");
            hIcdrSeqNoCurr = getValueInt("seq_no_curr");
            hIcdrVendorIch  = getValue("vendor_ich");
            hIcdrRowid       = getValue("rowid1");

            hIcdrIchKind    = "01";
            hIcdrUnionFlag  = "0";
            hIcdrUniformNo  = "00000000";

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

if(debug==1) showLogMessage("I", "", "read Cnt=["+totCnt+"]"+hIcdrCardCode);

            writeRtn();

            updateIchB01bParm();
            updateIchCardParm();

        }
        closeCursor();
}
/***********************************************************************/
void writeRtn() throws Exception 
{

        String tmpstr = "";

        detailSt = new buf1();

        detailSt.type = "D";

        tmpstr = String.format("%-2.2s"  , hIcdrGroupId);
        detailSt.groupId      = tmpstr;

        tmpstr = String.format("%-2.2s"  , hIcdrCardCode.substring(4,6));
        detailSt.addYearYy   = tmpstr;

        tmpstr = String.format("%-2.2s"  , hIcdrBankId);
        detailSt.bankId       = tmpstr;

        tmpstr = String.format("%3.3s"   , hIcdrCardCode.substring(6));
        detailSt.lotNoIch    = tmpstr;

        tmpstr = String.format("%-16.16s", hIcdrBarCode);
        detailSt.barCode      = tmpstr;

        tmpstr = String.format("%-2.2s"  , hIcdrBinType);
        detailSt.binType2    = tmpstr;

        detailSt.cardName     = comcpi.commTransChinese(String.format("%-30.30s", hIcdrCardName));

        tmpstr = comm.fillZero(hIcdrVendorIch , 2);
        detailSt.vendorIch    = tmpstr;

        tmpstr = String.format("%06d"    , hIcdrSeqNoCurr);
        detailSt.seqNoCurr   = tmpstr;

        tmpstr = String.format("%-2.2s"  , hIcdrIchKind  );
        detailSt.ichKind      = tmpstr;

        tmpstr = String.format("%-1.1s"  , hIcdrUnionFlag);
        detailSt.unionFlag    = tmpstr;

        tmpstr = String.format("%-8.8s"  , hIcdrUniformNo);
        detailSt.uniformNo    = tmpstr;

        tmpstr = String.format("%-14.14s", sysDate+sysTime  );
        detailSt.sysDatetime  = tmpstr;

        detailSt.fillerEnd    = "\r\n";

        String buf = detailSt.allText();
        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        allData += buf;

if(debug==1) 
   showLogMessage("I", "", "  Write =["+buf+"]"+ buf.getBytes("big5").length+","
                                          + allData.getBytes("big5").length);

        return;
}
/***********************************************************************/
void updateIchB01bParm() throws Exception 
{
        daoTable   = "ich_b01b_parm";
        updateSQL  = " media_create_date = ?,";
        updateSQL += " media_create_time = ?,";
        updateSQL += " file_name         = ?,";
        updateSQL += " proc_flag         = 'Y',";
        updateSQL += " mod_pgm           = ?,";
        updateSQL += " mod_time          = sysdate";
        whereStr   = "where rowid        = ? ";
        setString(1, hIcdrMediaCreateDate);
        setString(2, hIcdrMediaCreateTime);
        setString(3, hTnlgFileName);
        setString(4, javaProgram);
        setRowId( 5, hIcdrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "",comcr.hCallBatchSeqno);
        }

}
/***********************************************************************/
void updateIchCardParm() throws Exception 
{
        daoTable   = "ich_card_parm";
        updateSQL  = " send_date      = ?,";
        updateSQL += " mod_pgm        = ?,";
        updateSQL += " mod_time       = sysdate";
        whereStr   = "where card_code = ? ";
        setString(1, hTnlgNotifyDate);
        setString(2, javaProgram);
        setString(3, hIcdrCardCode);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", hIcdrCardCode
                                                              , comcr.hCallBatchSeqno);
        }

}
/***********************************************************************/
public static void main(String[] args) throws Exception 
{
  IchF001 proc = new IchF001();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
}
/***********************************************************************/
    class buf1 {
        String type;
        String groupId;
        String addYearYy;
        String bankId;
        String lotNoIch;
        String barCode;
        String binType2;
        String cardName;
        String vendorIch;
        String seqNoCurr;
        String ichKind;
        String unionFlag;
        String uniformNo;
        String sysDatetime;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(type         ,  1);
            rtn += fixLeft(groupId     ,  2);
            rtn += fixLeft(addYearYy  ,  2);
            rtn += fixLeft(bankId      ,  2);
            rtn += fixLeft(lotNoIch   ,  3);
            rtn += fixLeft(barCode     , 16);
            rtn += fixLeft(binType2   ,  2);
            rtn += fixLeft(cardName    , 30);
            rtn += fixLeft(vendorIch   ,  2);
            rtn += fixLeft(seqNoCurr  ,  6);
            rtn += fixLeft(ichKind     ,  2);
            rtn += fixLeft(unionFlag   ,  1);
            rtn += fixLeft(uniformNo   ,  8);
            rtn += fixLeft(sysDatetime , 14);
            rtn += fixLeft(fillerEnd   ,  2);
            return rtn;
        }

        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            int size = (Math.floorDiv(len, 100) + 1) * 100;
            String spc = "";
            for (int i = 0; i < size; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);

            return new String(vResult, "MS950");
        }
        String fixRight(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
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

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        detailSt.type          = comc.subMS950String(bytes,  0,  1);
        detailSt.groupId      = comc.subMS950String(bytes,  1,  2);
        detailSt.addYearYy   = comc.subMS950String(bytes,  3,  2);
        detailSt.bankId       = comc.subMS950String(bytes,  5,  2);
        detailSt.lotNoIch    = comc.subMS950String(bytes,  7,  3);
        detailSt.barCode      = comc.subMS950String(bytes, 10, 16);
        detailSt.binType2    = comc.subMS950String(bytes, 26,  2);
        detailSt.cardName     = comc.subMS950String(bytes, 28, 30);
        detailSt.vendorIch    = comc.subMS950String(bytes, 58,  2);
        detailSt.seqNoCurr   = comc.subMS950String(bytes, 60,  6);
        detailSt.ichKind      = comc.subMS950String(bytes, 66,  2);
        detailSt.unionFlag    = comc.subMS950String(bytes, 68,  1);
        detailSt.uniformNo    = comc.subMS950String(bytes, 69,  8);
        detailSt.sysDatetime  = comc.subMS950String(bytes, 77, 14);
        detailSt.fillerEnd    = comc.subMS950String(bytes, 91,  2);
    }

}

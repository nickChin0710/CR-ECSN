/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/08/09 V1.01.01  Lai         program initial                            *
*  112/08/11 V1.01.02  Lai         Add FTP                                    *
*  112/08/30 V1.01.03  Zuwei Su    header控制碼修改                               *
*  112/08/31 V1.01.04  Zuwei Su    註解ftp                                      *
*  112/09/05 V1.01.04  Kirin       change 符號                                  *
*  112/10/08 V1.01.06  Zuwei Su    ID要隱碼,第4~7碼,印出*號                         *
*  112/10/20 V1.01.07  Kirin       補上第2個ID隱碼,第4~7碼,印出*號                    *
*  112/11/24 V1.01.05  Kirin       每月20日執行 &專案代碼=2023010001                 *
******************************************************************************/
package Mkt;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommDate;
import com.CommFTP;

public class MktRM180 extends AccessDAO {
    private String PROGNAME = "合作金庫及其子公司共同行銷獎勵措施員工推廣情形表  112/08/01 V1.01.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    CommDate      comDate= new CommDate();

    int    DEBUG    = 0;
    int    DEBUG_F  = 0;
    String prgmId    = "MktRM180";
    String hTempUser = "";

    int    reportPageLine = 45;
//    String pgmCd     = "2022121001";
    String pgmCd     = "2023010001";

    String rptIdR1   = "RCRM180.1";
    String rptName1  = "合作金庫及其子公司共同行銷獎勵措施員工推廣情形表";
    int    pageCnt1  = 0, lineCnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int    totCnt = 0;

    String hBusiBusinessDate = "";
    String hCallBatchSeqno   = "";
    String hChiYymmdd      = "";
    String hBegDateCur       = "";
    String hEndDateCur       = "";
    String hBegDateBil       = "";
    String hEndDateBil       = "";
    String applyDateS        = "";
    String applyDateE        = "";

    String emplId             = "";
    String emplUNitNo         = "";
    String emplName           = "";
    String emplBrnChiName     = "";
    String cardCardNo         = "";
    String cardIdNo           = "";
    String cardName           = "";
    String cardToDate         = "";

    int    brnCnt               = 0;
    int    brnApplyCnt          = 0;
    int    brnNoApplyCnt        = 0;
    int    brnEffcCnt           = 0;
    int    brnNoConsumeCnt      = 0;
    int    chkCnt               = 0;
    int    bilCnt               = 0;

    int    all0                = 0;
    int    sumAll0             = 0;
    String tempBrn              = "";
    String tempName             = "";

    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    String tmpstr1 = "";

    buft htail = new buft();
    buf1 data  = new buf1();

    private int fptr1  = -1;
    String  filenameO = "";
/***********************************************************************/
public int mainProcess(String[] args) 
{
 try 
   {
    // ====================================
    // 固定要做的
    dateTime();
    setConsoleMode("Y");
    javaProgram = this.getClass().getName();
    showLogMessage("I", "", javaProgram + " " + PROGNAME + " Args=["+args.length+"]");
 
    // 固定要做的
    if(!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }
    // =====================================
    if(args.length > 3 ) {
       comc.errExit("Usage : MktRM180 [PROGRAM_CODE] [yyyymmdd] [seq_no] ", "");
      }
/*
    if(comm.isAppActive(javaProgram))
       comc.errExit("Error!! Someone is running this program now!! =["+javaProgram+"]" , "Please wait a moment to run again!!");
*/
 
    comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    comr  = new CommRoutine(getDBconnect()   , getDBalias());
 
    hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
    if(comc.getSubString(hCallBatchSeqno,0,8).equals(comc.getSubString(comc.getECSHOME(),0,8)))
      { hCallBatchSeqno = "no-call"; 
      }
 
    String checkHome = comc.getECSHOME();
    if(hCallBatchSeqno.length() > 6) {
       if(comc.getSubString(hCallBatchSeqno,0,6).equals(comc.getSubString(checkHome,0,6))) 
         {
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

    if (args.length >  0) {
        pgmCd = args[0];
    }
    if (args.length == 2 && args[1].length() == 8) {
    	hBusiBusinessDate = args[1];
    } 
    
    checkOpen();

    selectPtrBusinday();
	if (!"20".equals(comc.getSubString(hBusiBusinessDate,6))) {
		showLogMessage("I", "", "每月20日執行, 本日非執行日!!");
		return 0;
	}
    selectMktIntrFund();
 
    selectMktTcbLifeList();
 
    if(totCnt > 0)
      {
        tempBrn  = emplUNitNo;
        tempName = emplBrnChiName;
        writeTail(2);
        finalTail();
      }
    closeOutputText(fptr1);

//    String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), rptIdR1);
    String filename = String.format("/crdatacrea/BREPORT/%s.txt", rptIdR1);
    filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
//    comc.writeReport(filename, lpar1);
    comc.fileCopy(filenameO, filename);

//    ftpRtn(filenameO);

    // move to backup
    String backupFilename = String.format("%s/media/mkt/backup/%s.txt", comc.getECSHOME(), rptIdR1);
    comc.fileMove(filenameO, backupFilename);

    // ==============================================
    // 固定要做的
    comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
    showLogMessage("I", "", comcr.hCallErrorDesc);
    if (comcr.hCallBatchSeqno.length() == 20) {
        comcr.callbatch(1, 0, 1); // 1: 結束
    }

    finalProcess();
    return 0;
  } catch (Exception ex) { expMethod = "mainProcess"; expHandle(ex); return exceptExit;
                         }
}
// ************************************************************************
public void selectMktIntrFund() throws Exception 
{
   sqlCmd  = "select apply_date_s ";
   sqlCmd += "     , apply_date_e ";
   sqlCmd += " from mkt_intr_fund ";
   sqlCmd += "where program_code = ? ";
   sqlCmd += "  and apr_flag     = 'Y' ";
   setString(1, pgmCd);

   int recordCnt = selectTable();
   if(recordCnt > 0) {
      applyDateS = getValue("apply_date_s");
      applyDateE = getValue("apply_date_e");
     }
   if (notFound.equals("Y")) {
//     comcr.errRtn("select mkt_intr_fund not found!", PgmCd , hCallBatchSeqno);
       applyDateS = "20220610";
       applyDateE = "20230609";
   }
   showLogMessage("I", "", String.format("專案代碼=[%s][%s][%s]",pgmCd,applyDateS,applyDateE));
//   hBegDateBil = comDate.dateAdd(applyDateS , 0,-6, 0).substring(0,6) + "01";
//   hEndDateBil = applyDateE;
   hBegDateBil = comDate.dateAdd(hBusiBusinessDate , 0,-2, 0).substring(0,6) + "01";
   hEndDateBil = comDate.dateAdd(hBusiBusinessDate , 0,-1, 0).substring(0,6) + "31";

   showLogMessage("I", "", String.format("    帳單最大區間[%s][%s]",hBegDateBil,hEndDateBil));
}
/***********************************************************************/
void checkOpen() throws Exception 
{
   filenameO = String.format("%s/media/mkt/%s.TXT",comc.getECSHOME(),rptIdR1);
   filenameO = Normalizer.normalize(filenameO, java.text.Normalizer.Form.NFKD);
   comc.mkdirsFromFilenameWithPath(filenameO);
   fptr1 = openOutputText(filenameO, "MS950");
   showLogMessage("I", "", String.format("Open file=[%s]",filenameO));
   if(fptr1 == -1) {
      comc.errExit("在程式執行目錄下沒有權限讀寫", filenameO);
     }
}
/***********************************************************************/
public int  selectPtrBusinday() throws Exception 
{

   sqlCmd  = "select to_char(sysdate,'yyyymmdd') as business_date";
   sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
   }
   if (recordCnt > 0) {
       hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                            : hBusiBusinessDate;
   }

   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymm')||'01' hBegDateBil ";
   sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') hEndDateBil ";
   sqlCmd += "     , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')||'01' h_beg_date ";
   sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date ";
   sqlCmd += " from dual ";
   setString(1, hBusiBusinessDate);
   setString(2, hBusiBusinessDate);
   setString(3, hBusiBusinessDate);
   setString(4, hBusiBusinessDate);

   recordCnt = selectTable();
   if(recordCnt > 0) {
      hEndDateCur = getValue("h_end_date");
      hBegDateCur = hEndDateCur.substring(0, 4) + "0101";
      hBegDateBil = getValue("hBegDateBil");
      hEndDateBil = getValue("hEndDateBil");
     }

   hChiYymmdd         = getValue("h_chi_yymmdd");
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]" , hBusiBusinessDate , hChiYymmdd, hBegDateCur, hEndDateCur , hBegDateBil, hEndDateBil));

   return 0;
}
/***********************************************************************/
int selectGenBrn()      throws Exception {
    sqlCmd = " select  ";
    sqlCmd += " a.branch             ,a.brief_chi_name        ";
    sqlCmd += "  from gen_brn  a ";
    sqlCmd += " where a.branch = ? ";

    setString(1, emplUNitNo);

    int recordCnt = selectTable();

    emplBrnChiName = getValue("brief_chi_name");

    return(0);
}
/***********************************************************************/
void selectMktTcbLifeList() throws Exception 
{
  fetchExtend = "main.";
  sqlCmd  = " select ";
  sqlCmd += "  a.card_no             ,a.id_no             , ";
  sqlCmd += "  a.introduce_id        ,a.branch            , ";
  sqlCmd += "  a.chi_name card_name  ,b.chi_name empl_name, ";
  sqlCmd += "  a.to_date1            ,b.unit_no             ";
  sqlCmd += "   from crd_employee b, mkt_tcb_life_list a  ";
  sqlCmd += "  where b.id            = a.introduce_id ";
  sqlCmd += "    and a.static_month  between ? and ?  ";
  sqlCmd += "    and a.employ_flag       <> 'Y' ";   
  sqlCmd += "    and a.market_agree_base in ('1','2') ";     // 1-同意共銷、2-同意共享
  sqlCmd += "    and b.status_id         in ('1','6') ";     // 在職
  sqlCmd += "  order by b.unit_no, a.introduce_id ";
  
  setString(1, applyDateS.substring(0,6));
  setString(2, applyDateE.substring(0,6));
  showLogMessage("I","","Read ALL Month="+applyDateS.substring(0,6)+","+applyDateE.substring(0,6));

  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt++;

     emplId              = getValue("main.introduce_id");
     emplUNitNo          = getValue("main.unit_no");
     emplName            = getValue("main.empl_name");
//   EmplBrnChiName      = getValue("main.brief_chi_name");
     cardCardNo          = getValue("main.card_no");
     cardIdNo            = getValue("main.id_no"); 
     cardName            = getValue("main.card_name");
     cardToDate          = getValue("main.to_date1");

     selectGenBrn();
if(DEBUG==1) showLogMessage("I","","Read brn="+emplUNitNo+" ID="+emplId+","+emplName+",C="+cardIdNo+",Cnt="+totCnt);

     if(totCnt % 1000 == 0 || totCnt == 1)
        showLogMessage("I","",String.format("R81A Process 1 record=[%d]\n", totCnt));

     if(totCnt == 1)   
       {
        tempBrn        = emplUNitNo;
        tempName       = emplBrnChiName;
        writeRptHead(0);
        writeHead(0);
       }
     if(tempBrn.compareTo(emplUNitNo) != 0) 
       {
        writeTail(1);

        writeRptHead(1);
        writeHead(1);

        tempBrn    = emplUNitNo;
        tempName   = emplBrnChiName;
        brnCnt     = 0;
       }


     brnCnt++;
     writeDetail();    
    }

  showLogMessage("I",""," Read end="+totCnt);

}
/***********************************************************************/
void initRtn() throws Exception 
{
     emplId             = "";
     emplUNitNo         = "";
     emplName           = "";
     emplBrnChiName     = "";
     cardCardNo         = "";
     cardIdNo           = "";
     cardName           = "";
     cardToDate         = "";
}
/***********************************************************************/
void writeRptHead(int idx) throws Exception 
{
   buf = "";
   String tmpStr1 = "";
   String tmpStr2 = "";

   tmpStr1 = tempBrn;
   tmpStr2 = hChiYymmdd+rptName1;
   if(idx ==1) tmpStr1 = emplUNitNo;
     
  
   buf = comc.fixLeft(tmpStr1, 10) + comc.fixLeft("RCRM180", 16) 
       + comc.fixLeft(tmpStr2, 88) + comc.fixLeft("N", 8);

   writeTextFile(fptr1, buf+"\n");
   buf = "";
}
/***********************************************************************/
void writeHead(int idx) throws Exception 
{
     String temp = "";

     pageCnt1++;
     if(pageCnt1 > 1)
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

if(DEBUG_F==1) showLogMessage("I","","   Write Head="+idx+",Page="+ pageCnt1+","+tempBrn+","+emplUNitNo);

     buf = "";
     if(idx ==1)
        buf = comcr.insertStr(buf, "分行代號: " +emplUNitNo+" "+comc.fixLeft(emplBrnChiName,10),  1);
     else
        buf = comcr.insertStr(buf, "分行代號: " +tempBrn +" "+comc.fixLeft(tempName,10)        ,  1);
     buf = comcr.insertStr(buf, ""           + comc.fixLeft(rptName1,50)  , 40);
     buf = comcr.insertStr(buf, "保存年限: 一年"                          ,100);
     writeFile(idx,buf);

     buf = "";
     tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                 hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
     buf = comcr.insertStr(buf, "報表代號: CRM180    科目代號:"   ,  1);
     buf = comcr.insertStr(buf, "中華民國 " + tmp                 , 52);
     temp = String.format("%4d", pageCnt1);
     buf = comcr.insertStr(buf, "頁    次:" + temp                ,100);
     writeFile(idx,buf);

     writeFile(idx," ");

     buf = "薪資薪資  推廣員工    推廣員工              同意共銷    同意共銷              提供子公司";
     writeFile(idx,buf);

     buf = "單位代號    ＩＤ      姓  名                  ＩＤ      姓  名                共同行銷日";
     writeFile(idx,buf);

     buf = "========  ==========  ====================  ==========  ====================  ==========";
     writeFile(idx,buf);

     lineCnt1 = 6;
}
/***********************************************************************/
void writeFile(int idx ,String buf) throws Exception
{
   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

   if(idx  != 2)
      writeTextFile(fptr1, buf+"\n");
}
/***********************************************************************/
 void writeTail(int idx) throws Exception 
{
  String tmp1     = "";
        
     htail.str01       = "   小  計 : " +  comcr.commFormat("3z,3z"   , brnCnt);
     buf = htail.allText();
     writeFile(1,buf);

     writeFile(1," ");

     all0 += brnCnt;
}
/***********************************************************************/
 void finalTail() throws Exception 
{

     htail.str01       = "   總  計 : " +  comcr.commFormat("3z,3z"   , all0);
     buf = htail.allText();
     writeFile(1,buf);

     writeFile(1," ");


     buf = "說 明:";
     writeFile(1,buf);

     buf = " １、本活動期間: " + applyDateS + " ～ " + applyDateE ;
     writeFile(1,buf);

     buf = " ２、推廣人員ＩＤ如為『本行員工』，則每提供每一筆同意共同行銷資料予合庫人壽，則該推廣人員可獲得『禮券５０元』乙份，";
     writeFile(1,buf); 

     buf = "     如該筆同意共同行銷資料為行員，則不列入獎勵計算。";
     writeFile(1,buf);

}
/***********************************************************************/
void writeDetail() throws Exception 
{
     String tmp = "";

if(DEBUG_F==1) showLogMessage("I","","   Write Dtl="+brnCnt+","+tempBrn+","+emplUNitNo);

    if(lineCnt1 > reportPageLine) {
        writeRptHead(2);
       writeHead(2);
      }

    data = null;
    data = new buf1();

    data.str01   = String.format("  %s" , emplUNitNo);
    data.str02   = String.format("%s"   , emplId.substring(0, 3) + "****" + emplId.substring(7));
    data.str03   = String.format("%s"   , emplName);
//  data.str04   = String.format("%s"   , cardIdNo);
    data.str04   = String.format("%s"   , cardIdNo.substring(0, 3) + "****" + cardIdNo.substring(7));
    data.str05   = String.format("%s"   , cardName);
    data.str06   = String.format("%s"   , cardToDate);

    buf = data.allText();
    writeFile(1,buf);

    lineCnt1  = lineCnt1 + 1;

    return;
}
/***********************************************************************/
private void ftpRtn(String hFileNameI) throws Exception {
    int    errCode  = 0;
    String temstr1  = "";
    String temstr2  = "";
    String procCode = "";
    String hOwsWfValue3 = "";

    CommFTP       commFTP = new CommFTP(getDBconnect()    , getDBalias());
    CommRoutine      comr = new CommRoutine(getDBconnect(), getDBalias());

//    temstr1 = String.format("%s/media/mkt/%s",comc.getECSHOME(), hFileNameI);
//    temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
//    String filename = temstr1;
    String filename = hFileNameI;

    String  hEflgRefIpCode  = "BREPORT";
    commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");
    commFTP.hEflgSystemId   = "NCR2BRN";
    commFTP.hEflgGroupId    = "CRM";
    commFTP.hEflgSourceFrom = "EcsFtpBil";
    commFTP.hEflgModPgm     = this.getClass().getName();
    commFTP.hEriaLocalDir   = String.format("%s/media/mkt", comc.getECSHOME());
    commFTP.hEriaRemoteDir = "/crdatacrea/BREPORT";
    System.setProperty("user.dir", commFTP.hEriaLocalDir);
//    filename  = String.format("%s", hFileNameI);
    procCode  = String.format("put %s", filename);
    showLogMessage("I", "", procCode + ", " + hEflgRefIpCode + " 開始上傳....");
    errCode   = commFTP.ftplogName(hEflgRefIpCode, procCode);
    if (errCode != 0) {
      String stderr = String.format("ftp_rtn=[%s]傳檔錯誤 err_code[%d]\n", procCode, errCode);
      showLogMessage("I", "", stderr);
    }
    else
    {
     backFile(filename);
    }
}
/***************************************************************************/
void backFile(String filename) throws Exception {
   String tmpstr1 = String.format("%s/media/mkt/%s", comc.getECSHOME(), filename);
   String tmpstr2 = String.format("%s/media/mkt/backup/%s_%s",comc.getECSHOME(),filename,sysDate);

   if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
       showLogMessage("I", "", "ERROR : 檔案["+tmpstr1+" to "+tmpstr2+"]備份失敗!");
       return;
   }

   comc.fileDelete(tmpstr1);
   showLogMessage("I", "", "檔案 [" +tmpstr1 + "] 已移至 [" + tmpstr2 + "]");
}
/************************************************************************/
public static void main(String[] args) throws Exception 
{
       MktRM180 proc = new MktRM180();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
  class buft 
    {
        String filler01;
        String str01;
        String str02;
        String int01;
        String int02;
        String int03;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(str01        ,  32+0);
            rtn += fixLeft(int01        ,  16+1);
            rtn += fixLeft(int02        ,  16+1);
            rtn += fixLeft(int03        ,  16+1);
            return rtn;
        }

        
    }
  class buf1 
    {
        String str01;
        String str02;
        String str03;
        String str04;
        String str05;
        String str06;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(str01        ,   8+2);
            rtn += fixLeft(str02        ,  10+2);
            rtn += fixLeft(str03        ,  20+2);
            rtn += fixLeft(str04        ,  10+2);
            rtn += fixLeft(str05        ,  20+2);
            rtn += fixLeft(str06        ,  20+2);
 //         rtn += fixLeft(len          ,  1);
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

}

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE   Version    AUTHOR                   DESCRIPTION                 *
*  --------- --------- ----------- ----------------------------------------- *
*  112/10/03 V1.01.01  Lai         program initial                           *
*  112/12/04 V1.01.06  kirin       非執行日,移除 comc.errExit--> return code非0  *
*                                                                            *
*****************************************************************************/
package Mkt;

import com.BaseBatch;
import com.CommString;

import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import Cca.CalBalance;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.commons.io.FileUtils;

public class MktR171 extends AccessDAO {
    private  String progname = "合庫人壽共銷-獎勵發放名單統計檔(Crd_card)  112/10/03 V1.01.01";
    CommCrd       comc  = new CommCrd();
    CommDate   commDate = new CommDate();
    CommString   comStr = new CommString();

    CalBalance calBalance = null;

    int    DEBUG    = 1;
    int    DEBUG_F  = 0;

    private String busiDate  = "";
    private String busiMonth = "";
    private String busiPrev6Mon   = "";
    private String busiPrevMonth  = "";
    private String busiPrev6MonD  = "";
    private String busiPrevMonthD = "";
    private int totCnt = 0;
    private int okCnt  = 0;
    String  cardIssueDateF = "";
    String  cardIssueDateT = "";
    String  cardIssueDate = "";
    String  cardCardNo    = "";
    String  cardIdPseqNo  = "";
    String  idnoIdNo      = "";
    String  runDate       = "";
    int     listCnt1      = 0;
    int     listCnt2      = 0;
    int     listCnt3      = 0;
    String  inCardNo      = "";
    String  inCurrentCode = "";
    String  inIssueDate   = "";
    String  inOppostDate  = "";
    String  hProcMark     = "";
    String  maxinIssueDate= "";

        private static final int OUTPUT_BUFF_SIZE   = 100000;
        private static final String CRM_FOLDER      = "/crdatacrea/";
        private static final String CRM_FOLDER_COPY = "/crdatacrea/CREDITCARD/";
        private static final String BACKUP_FOLDER   = "/media/mkt/backup/";
        private static final String DATA_FORM1      = "CR_TCBLIFE_YYYYMMDD.TXT";
        private static final String DATA_FORM1_COPY = "CR_TCBLIFE_YYYYMMDD.TXT";
        private final static String COL_SEPERATOR   = ",";
        private final static String LINE_SEPERATOR  = System.lineSeparator();

        int totalCnt2 = 0;
        int commit    = 1;
        String oFileName = "";
        StringBuffer dataCountBuf = new StringBuffer();
/******************************************************************************/
    public static void main(String[] args) throws Exception {
        MktR171 proc = new MktR171();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    public int mainProcess(String[] args) {
        try {
            calBalance = new CalBalance(conn, getDBalias());
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            showLogMessage("I", "", "Usage : Mktr171本程式每月20號執行[business_date] [issue_month]");

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================
            
            if (args.length == 1) {
                if(args[0].length() < 8)
                   comc.errExit(String.format("日期錯誤[%s],程式結束!!", args[0]), "");
                busiDate  = args[0];
                busiPrevMonth = commDate.dateAdd(busiDate, 0,-1, 0).substring(0, 6);

            } else {
                selectPtrBusinday();
            }
            busiMonth      = comc.getSubString(busiDate, 0, 6);
            busiPrevMonth  = busiMonth;   // 20221210~20230609 ,  20230610~20231209
            busiPrev6Mon   = commDate.dateAdd(busiDate, 0,-6, 0).substring(0, 6);
            busiPrev6MonD  = commDate.dateAdd(busiDate, 0,-6, 0).substring(0, 6) + "10";
            busiPrevMonthD = busiPrevMonth + "09";

            runDate = busiMonth + "20";
            runDate = selectPtrHoliday(runDate);
            
            if(!runDate.equals(busiDate)) {
               //comc.errExit("錯誤: 本月可執行日為=["+runDate+"],目前營業日期=["+busiDate+"]","");
               showLogMessage("I", "", "錯誤: 本月可執行日為=[" + runDate + "], 目前營業日期=["+ busiDate + "],程式結束!! process end...." );
        		 return 0; 
            }


            showLogMessage("I", "", "程式本月可執行日期=["+runDate+"],目前參數日期=["+busiDate+"]");
            showLogMessage("I", "", "程式統計前6月起日期=["+ busiPrev6MonD+"]"+"迄日期=["+ busiPrevMonthD+"] System_date="+sysDate);
            
            deleteMktTcbLifeList();
            
            processCrdCard();
            
            commitDataBase();
            // CR_TCBLIFE_YYYYMMDD.TXT
            showLogMessage("I", "", " 開始處理第一個檔=[" +DATA_FORM1 + "]");;
            checkOpen(DATA_FORM1);
            writeText(DATA_FORM1);
            String tmpstr1 = String.format("%s/%s", CRM_FOLDER, oFileName);
            String tmpstr2 = String.format("%s/media/mkt/backup/%s",comc.getECSHOME(), oFileName);

            showLogMessage("I", "", " Backup From=[" +tmpstr1+ "] To=["+ tmpstr2 +"]");;
            if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
                comc.errExit("錯誤: 檔案["+tmpstr1+" to "+tmpstr2+"] 備份失敗!","");
            }
            //copyFile(DATA_FORM1,DATA_FORM1_COPY);

            showLogMessage("I", "", "執行結束");
            return 0;
        } catch (Exception e) {
            expMethod = "mainProcess";
            expHandle(e);
            return exceptExit;
        } finally {
            finalProcess();
        }
    }
/***********************************************************************/
String selectPtrHoliday(String ibusiDate) throws Exception {
    int tempCnt = 0;

    for (int i = 0; i < 30; i++) {
        sqlCmd  = "select count(*) h_temp_cnt ";
        sqlCmd += "  from ptr_holiday  ";
        sqlCmd += " where holiday = ? ";
        setString(1, ibusiDate);
        int recordCnt = selectTable();
        tempCnt = getValueInt("h_temp_cnt");
if(DEBUG==1) showLogMessage("I",""," Date="+ibusiDate+" ,Cnt="+tempCnt);
        if (tempCnt < 1) i = 31;
        else ibusiDate = commDate.dateAdd(ibusiDate, 0, 0, 1);
    }

    return (ibusiDate);
}
/***********************************************************************/
private void selectPtrBusinday() throws Exception {
   busiDate = "";
   sqlCmd = " select business_date, "
          + " to_char(add_months(to_date(business_date, 'yyyymmdd'), -1), 'yyyymm') as h_prev_month "
          + " from ptr_businday";

   selectTable();
   if (notFound.equals("Y")) {
       comc.errExit("select_ptr_businday not found!", "");
   }
   busiDate      = getValue("business_date");
   busiPrevMonth = getValue("h_prev_month");
}
/***********************************************************************/
    private int selectMktTcbLifeList() throws Exception {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(" SELECT ");
        sb.append("     * ");
        sb.append(" FROM ");
        sb.append("     MKT_TCB_LIFE_LIST ");
        sb.append(" WHERE ");
        sb.append("     ISSUE_MONTH = ? "); // 上個月

        sqlCmd = sb.toString();
        setString(1, busiMonth); // USE_MONTH
        return selectTable();
    }
/*****************************************************************************/
    void deleteMktTcbLifeList() throws Exception {
        
        showLogMessage("I", "", "======= DELETE MKT_TCB_LIFE_LIST 當月=["+busiPrevMonth+"]");
        sqlCmd = " delete mkt_tcb_life_list " + " where static_month = ? ";
        setString(1, "299901");
        int rc = executeSqlCommand(sqlCmd);
    }
/***********************************************************************/
private void chkCrdCard() throws Exception {
   inCardNo      = "";
   inCurrentCode = "";
   inIssueDate   = "";
   inOppostDate  = "";
   
   //extendField = "card.";
   sqlCmd = " select a.card_no      as lp_card_no "
          + "      , a.current_code as lp_current_code "
          + "      , a.oppost_date  as lp_oppost_date  "
          + "      , a.issue_date   as lp_issue_date "
          + "   from crd_card a "
          + "  INNER JOIN CRD_IDNO b ON b.ID_P_SEQNO=a.ID_P_SEQNO and b.market_agree_base IN ('1','2') "
          + "  where a.id_p_seqno  = ? "
      //  + "    and a.issue_date between ? and ? "
          + "    and a.old_card_no  = ''   "
      //  + "    and a.current_code = '0'  "
          + "    and a.sup_flag     = '0'  "
          + "    and a.acct_type    = '01' "
          + "    and b.staff_flag  != 'Y'  "
          + "    and length(a.introduce_emp_no)=6 "
          + "    and a.group_code not in ('1298','1299','1545','1599') "
          + "    and a.old_card_no  = ''   "
          + "  order by a.issue_date desc ";

   setString(1, cardIdPseqNo); 
// setString(2, cardIssueDateF); 
// setString(3, cardIssueDateT); 
  
if(DEBUG_F==1) showLogMessage("I","","    chk crd_card parm="+idnoIdNo+","+cardIssueDateF+","+cardIssueDateT);
   int cardCnt = selectTable();
   if (notFound.equals("Y")) {
      // comc.errExit("select_crd_card not found!", cardIdPseqNo);
      cardCnt = 0;
   }
if(DEBUG_F==1) showLogMessage("I","","    chk crd_card cnt="+cardCnt+","+idnoIdNo+","+cardCardNo+","+cardIdPseqNo);
  
   int oppostCnt = 0;
   maxinIssueDate= "";
   hProcMark     = ""; 
   int cardCnt6  = 0;
   for (int j = 0; j < cardCnt; j++) {
        inCardNo      = getValue("lp_card_no"     , j);
        inCurrentCode = getValue("lp_current_code", j);
        inIssueDate   = getValue("lp_issue_date"  , j);
        inOppostDate  = getValue("lp_oppost_date" , j);
        if(Integer.parseInt(inIssueDate) < (Integer.parseInt(cardIssueDateF))  ||
           Integer.parseInt(inIssueDate) > (Integer.parseInt(cardIssueDateT)) )
          { continue; }
        cardCnt6++;
        chkMktTcbLifeList(j,inCardNo);
        if(listCnt1 > 0 || listCnt2 > 0 || listCnt3 > 0)   return;
        if(inOppostDate.length() > 0)   oppostCnt++;
        else { 
              if(maxinIssueDate.length() < 1) {   // 抓開戶日最晚的那一張卡
                 maxinIssueDate = inIssueDate;
                 cardCardNo     = inCardNo;
                }
              if(cardCnt  == 1 && cardCardNo.equals(inCardNo) )   hProcMark = "1";
             }
if(DEBUG_F==1) showLogMessage("I","","     2 chk crd_card cnt="+cardCnt+","+inCardNo+","+cardCardNo+",m="+hProcMark+","+inOppostDate+","+maxinIssueDate);
   }
   // 1:全新戶(含卡友原不同意,後來同意) -- 2:新戶(6個月內沒有任何停卡)  -- 3:新戶名單  
   if(cardCnt   < 2) 
      hProcMark     = "1";  
   else if(oppostCnt < 1) {
           hProcMark     = "2";  
           cardCardNo    = getValue("lp_card_no"     , 0);
     }


}
/*************************************************************************************/
    // 讀取資料
    private void processCrdCard() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT ");
        sb.append(" a.issue_date,a.card_no,a.p_seqno,a.major_card_no,a.PROMOTE_DEPT,a.PROMOTE_EMP_NO "
                + ",a.old_card_no,a.current_code,a.group_code,a.acct_type "
                + ",a.id_p_seqno,a.introduce_emp_no,a.clerk_id ");
        sb.append(",b.staff_flag,b.CHI_NAME,b.HOME_AREA_CODE1,b.HOME_TEL_NO1,b.HOME_TEL_EXT1 "
                + ",b.SEX,b.BIRTHDAY,b.ID_NO  "
                + ",b.OFFICE_AREA_CODE1,b.OFFICE_TEL_NO1,b.OFFICE_TEL_EXT1         "
                + ",b.HOME_AREA_CODE1,b.HOME_TEL_NO1,b.CELLAR_PHONE,b.E_MAIL_ADDR"
                + ",b.market_agree_base ");
        sb.append(",c.BILL_SENDING_ADDR1,c.BILL_SENDING_ADDR2,c.BILL_SENDING_ADDR3"
                + ",c.BILL_SENDING_ADDR4,c.BILL_SENDING_ADDR5,c.bill_sending_zip ");
        sb.append(" FROM CRD_CARD a ");
        sb.append(" INNER JOIN CRD_IDNO    b ON b.ID_P_SEQNO = a.ID_P_SEQNO and b.market_agree_base IN ('1','2') ");
        sb.append("INNER JOIN CRD_EMPLOYEE d ON d.EMPLOY_NO  = a.introduce_emp_no and d.status_id in ('1','6') ");
        sb.append(" LEFT JOIN ACT_ACNO     c ON c.P_SEQNO    = a.P_SEQNO ");
        sb.append(" WHERE 1=1");
        sb.append("   AND a.old_card_no  = '' "); 
        sb.append("   AND a.current_code = '0' "); 
        sb.append("   AND a.sup_flag     = '0' "); 
        sb.append("   AND a.acct_type    = '01' "); 
        sb.append("   AND a.issue_date between ? and ? ");  // 上個月 ~ 上6個月
        sb.append("   AND b.staff_flag != 'Y' ");           // 且卡友不是行員, 且推廣人員ID是行員
        sb.append("   AND length(a.introduce_emp_no)=6 ");  //-- add:20230912
        sb.append("   AND a.group_code not in ('1298','1299','1545','1599') "); 
        sb.append("   AND b.id_no not in (select f.id_no from mkt_tcblife06_h f where feedback_date between ? and ?) "); 
        sb.append("   AND b.id_no not in (select f.id_no from mkt_tcblife03_h f where feedback_date between ? and ?) "); 
        sb.append("   AND a.id_p_seqno not in (select f.id_p_seqno from mkt_tcb_life_list f where static_month between ? and ?) "); 
        
        sqlCmd = sb.toString();
        int idx_f = 1;
        setString(idx_f++, busiPrev6MonD ); 
        setString(idx_f++, busiPrevMonthD); 
        setString(idx_f++, busiPrev6MonD ); 
        setString(idx_f++, busiPrevMonthD); 
        setString(idx_f++, busiPrev6MonD ); 
        setString(idx_f++, busiPrevMonthD); 
        setString(idx_f++, busiPrev6Mon  ); 
        setString(idx_f++, busiPrevMonth ); 
        showLogMessage("I","","  open 1  Month="+ busiPrev6MonD+","+ busiPrevMonthD);
        int cnt = selectTable();
        showLogMessage("I","","  open 1  End cnt=["+ cnt+"]");
        if (cnt == 0) {
            comc.errExit("crd_card查無資料", "");
        }
if(DEBUG==1) showLogMessage("I",""," MAIN Cnt=["+cnt+"],"+busiPrevMonth);
        for (int i = 0; i < cnt; i++) {
            cardIdPseqNo  = getValue("id_p_seqno" , i); 
            idnoIdNo      = getValue("id_no"      , i);
            cardIssueDate = getValue("issue_date" , i);
            cardCardNo    = getValue("card_no"    , i);
            cardIssueDateF = commDate.dateAdd(cardIssueDate, 0,-6, 0);
            cardIssueDateT = cardIssueDate;
if(DEBUG==1) showLogMessage("I","","   MAIN card=["+cardCardNo+"],"+idnoIdNo+","+cardIdPseqNo+","+cardIssueDate);
            insertMktTcbLifeList(i);
        }
    }
/***********************************************************************************************/
private void chkMktTcbLifeList(int i, String parmCardNo) throws Exception {
   listCnt1  = 0;
   listCnt2  = 0;
   listCnt3  = 0;
   sqlCmd = " select count(*) as list_cnt1 "
          + " from mkt_tcb_life_list ";
   sqlCmd += " where id_p_seqno = ? ";
   sqlCmd += "   and static_month between ? and ? ";
   setString(1, cardIdPseqNo);
   setString(2, busiPrev6Mon ); 
   setString(3, busiPrevMonth); 

   selectTable();
   listCnt1  = getValueInt("list_cnt1");
   //**
   sqlCmd = " select count(*) as list_cnt2 "
          + " from mkt_tcblife03_h  ";
   sqlCmd += " where id_no = ? ";
   sqlCmd += "   and feedback_date between ? and ? ";
   setString(1, idnoIdNo);
   setString(2, busiPrev6Mon  + "01"); 
   setString(3, busiDate); 

   selectTable();
   listCnt2  = getValueInt("list_cnt2");
   //**
   sqlCmd = " select count(*) as list_cnt3 "
          + " from mkt_tcblife06_h  ";
   sqlCmd += " where id_no = ? ";
   sqlCmd += "   and feedback_date between ? and ? ";
   setString(1, idnoIdNo);
   setString(2, busiPrev6Mon  + "01"); 
   setString(3, busiDate); 

   selectTable();
   listCnt3  = getValueInt("list_cnt3");
}
/***********************************************************************************************/
    private void insertMktTcbLifeList(int i) throws Exception {

        chkCrdCard();
        if(hProcMark.length() < 1 && maxinIssueDate.length() > 0) {
           cardIssueDate = maxinIssueDate;
           hProcMark     = "3"; 
           return;
        }
   // 1:全新戶   -- 2:新戶(6個月內沒有任何停卡)  -- 3:新戶名單  
   //   if(hProcMark.compareTo("2") == 0)    return; 
        
        if(listCnt1 > 0 || listCnt2 > 0 || listCnt3 > 0 || hProcMark.length() < 1)   return;
if(DEBUG==1) showLogMessage("I","","    Insert No="+cardCardNo+","+idnoIdNo+","+cardIdPseqNo+",M="+hProcMark);

        daoTable = "mkt_tcb_life_list";
        setValue("p_seqno"   , getValue("p_seqno", i));              //填入【crd_card】 p_seqno
        setValue("acct_type" , getValue("acct_type", i));            //填入【crd_card】 acct_type
        setValue("id_p_seqno", getValue("id_p_seqno", i));           //填入【crd_card】 id_p_seqno
        setValue("card_no"   , cardCardNo );                         //填入【crd_card】 card_no
        setValue("major_card_no", cardCardNo );                      //填入【crd_card】 major_card_no
        setValue("issue_date"  , cardIssueDate);                     //填入【crd_card】 
        setValue("issue_month" , comc.getSubString(cardIssueDate,0,6));    
        setValue("static_month", "299901"); 
        setValue("branch"      , getValue("promote_dept"    , i));   //填入【crd_card】
        setValue("introduce_id", getValue("clerk_id"        , i));    
//      setValue("employ_mo"   , getValue("introduce_emp_no", i));    
        setValue("employ_no"   , getValue("introduce_emp_no", i));
        setValue("employ_flag" , getValue("staff_flag"      , i));    
        setValue("to_date1"    , busiDate);                          // 存入每月3號 or執行日
        setValue("to_date2"    , "");            

        setValue("crrent_code", getValue("crrent_code", i));     //填入【crd_card】 acct_type
        setValue("id_no", getValue("id_no", i));                         //【crd_idno】id_no
        setValue("market_agree_base", getValue("market_agree_base", i)); //【crd_idno】market_agree_base
        setValue("chi_name",      getValue("chi_name", i));              //【crd_idno】chi_name
        setValue("sex"     ,      getValue("sex", i));                   //【crd_idno】sex
        setValue("birthday",      getValue("birthday", i));              //【crd_idno】birthday
        setValue("office_area_code1", getValue("office_area_code1", i)); //【crd_idno】office_area_code1
        setValue("office_tel_no1",    getValue("office_tel_no1", i));    //【crd_idno】office_tel_no1
        setValue("office_tel_ext1",   getValue("office_tel_ext1", i));   //【crd_idno】office_tel_ext1
        setValue("home_area_code1",   getValue("home_area_code1", i));   //【crd_idno】home_area_code1
        setValue("home_tel_no1",      getValue("home_tel_no1", i));      //【crd_idno】home_tel_no1
        setValue("cellar_phone",      getValue("cellar_phone", i));      //【crd_idno】cellar_phone
        setValue("e_mail_addr",       getValue("e_mail_addr", i));       //【crd_idno】e_mail_addr

        setValue("bill_sending_zip",  getValue("bill_sending_zip", i));    //【act_acno】sending_zip
        setValue("bill_sending_addr1", getValue("bill_sending_addr1", i)); //【act_acno】sending_addr1
        setValue("bill_sending_addr2", getValue("bill_sending_addr2", i)); //【act_acno】sending_addr2
        setValue("bill_sending_addr3", getValue("bill_sending_addr3", i)); //【act_acno】sending_addr3
        setValue("bill_sending_addr4", getValue("bill_sending_addr4", i)); //【act_acno】sending_addr4
        setValue("bill_sending_addr5", getValue("bill_sending_addr5", i)); //【act_acno】sending_addr5
        
        setValue("proc_mark", hProcMark);          //
        setValue("mod_type" , "A");                //寫入A
        setValue("proj_code", "");                 //填入空白
        setValue("file_type", "");                 //填入空白
        setValue("send_date", "");                 //填入空白
        setValue("crt_date" , sysDate);            //寫入系統日期
        setValue("proc_date", sysDate);            //寫入系統日期
        setValue("mod_time" , sysDate+sysTime);    //寫入系統日期
        setValue("mod_pgm"  , javaProgram);        //寫入程式代號

        
        if (insertTable() > 0) {
            okCnt++;
        }
        
        if (dupRecord.equals("Y")) {
         // comc.errExit("MKT_TCB_LIFE_LIST error!", getValue("p_seqno", i));
        }
    }
/****************************************************************************/
public void checkOpen(String isFileName) throws Exception {
       oFileName = isFileName.replace("YYYYMMDD", busiDate);
       String lsTemp = "";
       lsTemp = String.format("%s%s", CRM_FOLDER, oFileName);
       showLogMessage("I","","  Open File =" +lsTemp);
       boolean isOpen = this.openBinaryOutput(lsTemp);
       if (isOpen == false) {
           comc.errExit("此路徑或檔案不存在!!!", lsTemp);
       }
}
/****************************************************************************/
void writeText(String fileName) throws Exception {
     totalCnt2 = 0;
     sqlCmd  = " SELECT a.branch, a.introduce_id ";
     sqlCmd += "      , b.chi_name , count(*) as cntList ";
     sqlCmd += "   from mkt_tcb_life_list a ";
     sqlCmd += "   left outer join crd_idno b ON b.id_no  = a.introduce_id ";
     sqlCmd += "  WHERE (a.branch,a.introduce_id) in (select d.branch, d.introduce_id ";
     sqlCmd +=               "   from mkt_tcb_life_list d ";
     sqlCmd +=               "  where d.proc_mark  = '1' ";
     sqlCmd +=               "    and d.static_month between ? and ? ";
     sqlCmd +=               "   group by d.branch, d.introduce_id) ";
     sqlCmd += "    and a.static_month  between  ? and ? ";
     sqlCmd += "    and a.proc_mark   = '1' ";
     sqlCmd += "  group by a.branch, a.introduce_id, b.chi_name ";
     sqlCmd += "  order by a.branch, a.introduce_id, b.chi_name ";
     setString(1, "299901");
     setString(2, "299901");
     setString(3, "299901");
     setString(4, "299901");

     showLogMessage("I","","  open 1  Month=[" + busiPrev6Mon+"] " + busiPrevMonth);

     this.openCursor();
     showLogMessage("I","","  open 1  End ");

     int rowCount = 0;
     int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified
     try {
         StringBuffer sb = new StringBuffer();
         showLogMessage("I", "", "開始產生.TXT檔......");
         String items = getItems();
         sb.append(items);
         while (fetchTable()) {
                 MktR171Data mktR171Data = getInfData();
                 String rowOfDAT = getRowOfDAT(mktR171Data);
                 sb.append(rowOfDAT);
                 rowCount++;
                 countInEachBuffer++;
                 if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                     showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
                     byte[] tmpBytes = sb.toString().getBytes("MS950");
                     writeBinFile(tmpBytes, tmpBytes.length);
                     sb = new StringBuffer();
                     countInEachBuffer = 0;
                 }

                 commitDataBase();
                 totalCnt2++;
         }
         // write the rest of bytes on the file
         if (countInEachBuffer > 0 || sb.length() > 0) {
                 showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
                 byte[] tmpBytes = sb.toString().getBytes("MS950");
                 writeBinFile(tmpBytes, tmpBytes.length);
         }
         if (rowCount == 0) {
                 showLogMessage("I", "", "無資料可寫入.DAT檔");
         } else {
                 showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
         }

         this.closeCursor();
         } finally { closeBinaryOutput(); }
}
/****************************************************************************/
void copyFile(String removeFileName) throws Exception {
        removeFileName = removeFileName.replace("YYYYMMDD", busiDate);
        String tmpstr1 = Paths.get(CRM_FOLDER,removeFileName).toString();
        String tmpstr2 = Paths.get(comc.getECSHOME(),BACKUP_FOLDER,String.format("%s", removeFileName)).toString();
        String tmpstr3 = Paths.get(CRM_FOLDER_COPY,removeFileName).toString();
        if (comc.fileCopy(tmpstr1, tmpstr3) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]copy失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已copy至 [" + tmpstr3 + "]");

        if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]備份失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已備份至 [" + tmpstr2 + "]");
}
/*************************************************************************************/
        private String getRowOfDAT(MktR171Data mktrR171Data) throws Exception {
                StringBuffer sb = new StringBuffer();

                sb.append(comc.fixLeft(mktrR171Data.idNo    ,  10)); // 2.7.4.1. ID:id_no
                sb.append(comc.fixLeft(COL_SEPERATOR        ,   1));
                sb.append(comc.fixLeft(mktrR171Data.chiName ,  50)); // 2.7.4.1. 姓名:CHI_NAME
                sb.append(comc.fixLeft(COL_SEPERATOR        ,   1));
                sb.append(comc.fixLeft(mktrR171Data.branch  ,   4)); // 2.7.4.2.
                sb.append(comc.fixLeft(COL_SEPERATOR        ,   1));
                sb.append(comc.fixLeft(mktrR171Data.cntList ,   5));
                sb.append(LINE_SEPERATOR);
                return sb.toString();

        }
/*************************************************************************************/
        MktR171Data getInfData() throws Exception {
                MktR171Data mktrR171Data     = new MktR171Data();
                mktrR171Data.idNo            = getValue("introduce_id");
                mktrR171Data.chiName         = getValue("chi_name");
                mktrR171Data.branch          = getValue("branch");
                mktrR171Data.cntList         = String.format("%05d", getValueInt("cntList"));
if(DEBUG==1)  showLogMessage("I","","  Read STATIC=[" + mktrR171Data.idNo+"] " + mktrR171Data.chiName+","+getValueInt("cntList")+","+getValue("branch"));
                return mktrR171Data;
        }
/*************************************************************************************/
        private String getItems() throws Exception {
                StringBuffer sb = new StringBuffer();
                sb.append(comc.fixLeft("員工ＩＤ"   , 10));
                sb.append(comc.fixLeft(COL_SEPERATOR,  1));
                sb.append(comc.fixLeft("姓名"       , 50));
                sb.append(comc.fixLeft(COL_SEPERATOR,  1));
                sb.append(comc.fixLeft("分行"       ,  4));
                sb.append(comc.fixLeft(COL_SEPERATOR,  1));
                sb.append(comc.fixLeft("筆數"       ,  5));
                sb.append(LINE_SEPERATOR);

                return sb.toString();
        }
/***** end ******************************************************************/
}
class MktR171Data {
        String idNo    = "";
        String chiName = "";
        String branch  = "";
        String cntList = "";
}

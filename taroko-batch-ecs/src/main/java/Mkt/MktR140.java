/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/31  V1.00.00  Zuwei       program initial                          *
*  112/08/21  V1.00.01  Kirin       remark sql a.PROMOTE_DEPT='28990860'     *
*  112/08/28  V1.01.02  Lai         modify                                   *
*  112/09/01  V1.01.03  kirin       change EMPLOY_NO  & Fix 09/11            *
*  112/09/01  V1.01.04  kirin       group_code not in ('1298','1299','1545','1599')*
*  112/09/18  V1.01.05  Lai         add 6 個月                                *
*  112/12/04  V1.01.06  kirin       移除 comc.errExit--> return code非0        *
*****************************************************************************/
package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import Cca.CalBalance;

public class MktR140 extends AccessDAO {
    private  String progname = "合庫人壽共銷產生名單    112/09/18  V1.01.03";
    CommCrd  commCrd  = new CommCrd();
    CommDate commDate = new CommDate();
    CalBalance calBalance = null;

    int    DEBUG    = 0;
    int    DEBUG_F  = 0;

    private String busiDate = "";
    private String busiPrev6Mon  = "";
    private String busiPrevMonth = "";
    private String busiMonth = "";
    private int totCnt = 0;
    private int okCnt  = 0;
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
/******************************************************************************/
    public static void main(String[] args) throws Exception {
        MktR140 proc = new MktR140();
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
            showLogMessage("I", "", "Usage : Mktr140本程式每月3號執行[business_date] [issue_month]");

            if (!connectDataBase()) {
                commCrd.errExit("connect DataBase error", "");
            }
            // =====================================
            
            if (args.length == 1) {
                if(args[0].length() < 8) {
                 //  commCrd.errExit(String.format("日期錯誤[%s],程式結束!!", args[0]), "");
               	 showLogMessage("I", "", "日期錯誤" + args[0] + ",程式結束!!  process end...." );
           		 return 0;
                }
                busiDate  = args[0];
                busiPrevMonth = commDate.dateAdd(busiDate, 0,-1, 0).substring(0, 6);

            } else {
                selectPtrBusinday();
            }
            busiMonth    = commCrd.getSubString(busiDate, 0, 6);
            busiPrev6Mon = commDate.dateAdd(busiDate, 0,-6, 0).substring(0, 6);

            runDate = busiMonth + "03";
            runDate = selectPtrHoliday(runDate);
            
            if(!runDate.equals(busiDate)) {
              // commCrd.errExit("錯誤: 本月可執行日為=["+runDate+"],目前營業日期=["+busiDate+"]","");
            	showLogMessage("I", "", "錯誤: 本月可執行日為=[" + runDate + "], 目前營業日期=["+ busiDate + "],程式結束!! process end...." );
          		 return 0; 
            }


            showLogMessage("I", "", "程式本月可執行日期=["+runDate+"],目前參數日期=["+busiDate+"]");
            showLogMessage("I", "", "程式統計月份=["+ busiPrevMonth+"]"+"前6月份=["+ busiPrev6Mon+"] System_date="+sysDate);
            
  //        int cnt = selectMktTcbLifeList();
  //        if (cnt > 0) { commCrd.errExit("本月已產生名單,不可重覆執行", ""); }
            
            // 刪除24個月前的數據
            deleteMktTcbLifeList();
            
            processCrdCard();
            
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
       commCrd.errExit("select_ptr_businday not found!", "");
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
/**********************刪除前24個月的數據*********************************************/
    void deleteMktTcbLifeList() throws Exception {
        String date = commDate.dateAdd(sysDate, 0, -24, 0);
        showLogMessage("I", "", "======= DELETE MKT_TCB_LIFE_LIST 前24個=["+date.substring(0, 6)+"]");
        sqlCmd = " delete mkt_tcb_life_list " + " where static_month <= ? ";
        setString(1, date.substring(0, 6));
        int rc = executeSqlCommand(sqlCmd);
        
        showLogMessage("I", "", "======= DELETE MKT_TCB_LIFE_LIST 當月=["+busiPrevMonth+"]");
        sqlCmd = " delete mkt_tcb_life_list " + " where static_month = ? ";
        setString(1, busiPrevMonth);
        rc = executeSqlCommand(sqlCmd);
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
          + "    and a.issue_date between ? and ? "
          + "    and a.old_card_no  = ''   "
      //  + "    and a.current_code = '0'  "
          + "    and a.sup_flag     = '0'  "
          + "    and a.acct_type    = '01' "
          + "    and b.staff_flag  != 'Y'  "
          + "    and length(a.introduce_emp_no)=6 "
          + "    and a.group_code not in ('1298','1299','1545','1599') "
   //  --   排除前6個月 --    20230920
          + "    and a.id_p_seqno not in (select id_p_seqno from mkt_tcblife06_h where feedback_date between ? and ? )   "
        + "    and b.id_no not in (select id_no from mkt_tcblife03_h where feedback_date between ? and ? ) "
        /*          + "    and and a.id_p_seqno not in (select id_p_seqno from mkt_tcb_life_list where static_month between ? and ? ) "
*/
          + "  order by a.issue_date desc ";

   setString(1, cardIdPseqNo); 
   setString(2, busiPrev6Mon  + "01"); 
   setString(3, busiPrevMonth + "31");
   
   setString(4, busiPrev6Mon  + "01"); 
   setString(5, busiPrevMonth + "31");
   
   setString(6, busiPrev6Mon  + "01"); 
   setString(7, busiPrevMonth + "31"); 
   /*  setString(8, busiPrev6Mon  + "01"); 
   setString(9, busiPrevMonth + "31"); 
   */

   int cardCnt = selectTable();
   if (notFound.equals("Y")) {
      // commCrd.errExit("select_crd_card not found!", cardIdPseqNo);
      cardCnt = 0;
   }
if(DEBUG_F==1) showLogMessage("I","","    chk crd_card cnt="+cardCnt+","+idnoIdNo+","+cardCardNo+","+cardIdPseqNo);
  
   int oppostCnt = 0;
   maxinIssueDate= "";
   hProcMark     = ""; 
   for (int j = 0; j < cardCnt; j++) {
        inCardNo      = getValue("lp_card_no"     , j);
        inCurrentCode = getValue("lp_current_code", j);
        inIssueDate   = getValue("lp_issue_date"  , j);
        inOppostDate  = getValue("lp_oppost_date" , j);
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
   // 1:全新戶(含卡友原不同意,後來同意) 1:全新戶(6個月內沒有任何停卡)  2:新戶名單  
   if(cardCnt   < 1) 
      hProcMark     = "1";  
   else if(oppostCnt < 1) {
           hProcMark     = "1";  
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
     // sb.append("   AND a.issue_date between ? and ? ");  // 上個月 ~ 上6個月 改為全部
        sb.append("   AND b.staff_flag != 'Y' ");           // 且卡友不是行員, 且推廣人員ID是行員
        sb.append("   AND length(a.introduce_emp_no)=6 ");  //-- add:20230912
        sb.append("   AND a.group_code not in ('1298','1299','1545','1599') "); 
        sb.append("   AND b.id_no not in (select f.id_no from mkt_tcblife06_h f where feedback_date between ? and ?) "); 
        sb.append("   AND b.id_no not in (select f.id_no from mkt_tcblife03_h f where feedback_date between ? and ?) "); 
        sb.append("   AND a.id_p_seqno not in (select f.id_p_seqno from mkt_tcb_life_list f where static_month between ? and ?) "); 
        
        sqlCmd = sb.toString();
        setString(1, busiPrev6Mon  + "01"); 
        setString(2, busiPrevMonth + "31"); 
        setString(3, busiPrev6Mon  + "01"); 
        setString(4, busiPrevMonth + "31"); 
        setString(5, busiPrev6Mon  ); 
        setString(6, busiPrevMonth ); 
     // showLogMessage("I","","  open 1  Month="+ busiPrev6Mon+"01"+","+ busiPrevMonth + "31");
        showLogMessage("I","","  open 1  Month= All");
        int cnt = selectTable();
        showLogMessage("I","","  open 1  End cnt=["+ cnt+"]");
        if (cnt == 0) {
            commCrd.errExit("crd_card查無資料", "");
        }
if(DEBUG==1) showLogMessage("I",""," MAIN Cnt=["+cnt+"],"+busiPrevMonth);
        for (int i = 0; i < cnt; i++) {
            cardIdPseqNo  = getValue("id_p_seqno" , i); 
            idnoIdNo      = getValue("id_no"      , i);
            cardIssueDate = getValue("issue_date" , i);
            cardCardNo    = getValue("card_no"    , i);
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
           hProcMark     = "2"; 
        }
        
        if(listCnt1 > 0 || listCnt2 > 0 || listCnt3 > 0 || hProcMark.length() < 1)   return;
if(DEBUG==1) showLogMessage("I","","   Insert No="+cardCardNo+","+idnoIdNo+","+cardIdPseqNo+",M="+hProcMark);

        daoTable = "mkt_tcb_life_list";
        setValue("p_seqno"   , getValue("p_seqno", i));              //填入【crd_card】 p_seqno
        setValue("acct_type" , getValue("acct_type", i));            //填入【crd_card】 acct_type
        setValue("id_p_seqno", getValue("id_p_seqno", i));           //填入【crd_card】 id_p_seqno
        setValue("card_no"   , cardCardNo );                         //填入【crd_card】 card_no
        setValue("major_card_no", cardCardNo );                      //填入【crd_card】 major_card_no
        setValue("issue_date"  , cardIssueDate);                     //填入【crd_card】 
        setValue("issue_month" , commCrd.getSubString(cardIssueDate,0,6));    
        setValue("branch"      , getValue("promote_dept"    , i));   //填入【crd_card】
        setValue("introduce_id", getValue("clerk_id"        , i));    
//      setValue("employ_mo"   , getValue("introduce_emp_no", i));    
        setValue("employ_no"   , getValue("introduce_emp_no", i));
        setValue("employ_flag" , getValue("staff_flag"      , i));    
        setValue("static_month", busiPrevMonth); 
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
            commCrd.errExit("MKT_TCB_LIFE_LIST error!", getValue("p_seqno", i));
        }
    }

}
